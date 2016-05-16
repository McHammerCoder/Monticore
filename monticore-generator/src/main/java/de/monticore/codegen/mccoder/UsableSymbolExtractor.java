/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mccoder;

import static de.monticore.codegen.parser.ParserGeneratorHelper.getMCRuleForThisComponent;
import static de.monticore.codegen.parser.ParserGeneratorHelper.getTmpVarNameForAntlrCode;
import static de.monticore.codegen.parser.ParserGeneratorHelper.printIteration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.monticore.grammar.grammar._ast.ASTEncodeTableProd;
import de.monticore.grammar.grammar._ast.ASTEncodeTableEntry;
import de.monticore.languages.grammar.MCEncodeTableRuleSymbol;
import de.monticore.ast.ASTNode;
import de.monticore.codegen.mccoder.McCoderGeneratorHelper;
import de.monticore.codegen.parser.ParserGeneratorHelper;
import de.monticore.codegen.parser.antlr.ASTConstructionActions;
import de.monticore.codegen.parser.antlr.AttributeCardinalityConstraint;
import de.monticore.grammar.DirectLeftRecursionDetector;
import de.monticore.grammar.HelperGrammar;
import de.monticore.grammar.MCGrammarInfo;
import de.monticore.grammar.grammar._ast.ASTAlt;
import de.monticore.grammar.grammar._ast.ASTAnything;
import de.monticore.grammar.grammar._ast.ASTBlock;
import de.monticore.grammar.grammar._ast.ASTClassProd;
import de.monticore.grammar.grammar._ast.ASTConstant;
import de.monticore.grammar.grammar._ast.ASTConstantGroup;
import de.monticore.grammar.grammar._ast.ASTEnumProd;
import de.monticore.grammar.grammar._ast.ASTEof;
import de.monticore.grammar.grammar._ast.ASTLexActionOrPredicate;
import de.monticore.grammar.grammar._ast.ASTLexAlt;
import de.monticore.grammar.grammar._ast.ASTLexBlock;
import de.monticore.grammar.grammar._ast.ASTLexChar;
import de.monticore.grammar.grammar._ast.ASTLexCharRange;
import de.monticore.grammar.grammar._ast.ASTLexNonTerminal;
import de.monticore.grammar.grammar._ast.ASTLexOption;
import de.monticore.grammar.grammar._ast.ASTLexProd;
import de.monticore.grammar.grammar._ast.ASTLexSimpleIteration;
import de.monticore.grammar.grammar._ast.ASTLexString;
import de.monticore.grammar.grammar._ast.ASTMCAnything;
import de.monticore.grammar.grammar._ast.ASTNonTerminal;
import de.monticore.grammar.grammar._ast.ASTOptionValue;
import de.monticore.grammar.grammar._ast.ASTProd;
import de.monticore.grammar.grammar._ast.ASTSemanticpredicateOrAction;
import de.monticore.grammar.grammar._ast.ASTTerminal;
import de.monticore.grammar.grammar._ast.GrammarNodeFactory;
import de.monticore.grammar.grammar_withconcepts._ast.ASTAction;
import de.monticore.grammar.grammar_withconcepts._visitor.Grammar_WithConceptsVisitor;
import de.monticore.languages.grammar.MCAttributeSymbol;
import de.monticore.languages.grammar.MCGrammarSymbol;
import de.monticore.languages.grammar.MCRuleComponentSymbol;
import de.monticore.languages.grammar.MCRuleSymbol;
import de.monticore.languages.grammar.MCRuleSymbol.KindSymbolRule;
import de.monticore.languages.grammar.MCTypeSymbol;
import de.monticore.languages.grammar.MCTypeSymbol.KindType;
import de.monticore.languages.grammar.PredicatePair;
import de.monticore.symboltable.Symbol;
import de.monticore.utils.ASTNodes;
import de.se_rwth.commons.logging.Log;

/**
 * TODO: Write me!
 *
 * @author  (last commit) $Author$
 * @version $Revision$, $Date$
 * @since   TODO: add version number
 *
 */
public class UsableSymbolExtractor implements Grammar_WithConceptsVisitor{
private MCGrammarSymbol grammarEntry;
	
	private McCoderGeneratorHelper parserGeneratorHelper;
	private MCGrammarInfo grammarInfo;
	
	private List<String> productionUsableSymbolsCode = Lists.newArrayList();
	public static Map<String, Map<String, String>> customEncodingMap = Maps.newHashMap();
	private StringBuilder codeSection;
	
	private List<String> customEncoding = new ArrayList<String>();
	private Boolean negate = false;
	private String indent = "\t";
	
	private ArrayList<Range> ranges = new ArrayList<Range>();
	private Set<String> kws = new HashSet<String>();
	
	public UsableSymbolExtractor(McCoderGeneratorHelper parserGeneratorHelper, MCGrammarInfo grammarInfo) 
	{
		Preconditions.checkArgument(parserGeneratorHelper.getGrammarSymbol() != null);
		this.parserGeneratorHelper = parserGeneratorHelper;
		this.grammarEntry = parserGeneratorHelper.getGrammarSymbol();
		this.grammarInfo = grammarInfo;
	}
	public Map<String, Map<String, String>> getCustomEncodingMap(){
		return customEncodingMap;
	}
	
	@Override
	public void visit(ASTLexProd ast) 
	{
		addToCodeSection(indent + "/*ASTLexProd " + ast.getName() + "*/\n");
	}
	
	@Override
	public void visit(ASTLexBlock ast) 
	{
		addToCodeSection(indent + "/*ASTLEXBLOCK "+ ast.isNegate() + " */ \n");
		negate = ast.isNegate();
	}
	@Override
	public void visit(ASTLexCharRange ast) 
	{
		Boolean local = ast.isNegate();
		if(negate){
			if(local){
				local = false;
			}
			else{
				local = true;
			}
		}
		addToCodeSection(indent + "ranges.add(new " + "Range(" + "'" + ast.getLowerChar() + "'" + " ,"  + "'" + ast.getUpperChar() + "'" + " , " + local + "));\n" );
		ranges.add(new Range(StringEscapeUtils.unescapeJava(ast.getLowerChar()).charAt(0), StringEscapeUtils.unescapeJava(ast.getUpperChar()).charAt(0), ast.isNegate() ));
	}

	@Override
	public void visit(ASTLexChar ast)
	{
		Boolean local = ast.isNegate();
		if(negate){
			if(local){
				local = false;
			}
			else{
				local = true;
			}
		}
		addToCodeSection(indent + "ranges.add(new " + "Range(" + "'" + ast.getChar() + "'" + " ,"  + "'" + ast.getChar() + "'" +  " , "  + negate + "));\n" );
		ranges.add(new Range(StringEscapeUtils.unescapeJava(ast.getChar()).charAt(0), StringEscapeUtils.unescapeJava(ast.getChar()).charAt(0), ast.isNegate() ));
	}
	
	@Override
	public void visit(ASTTerminal ast) 
	{
		addToCodeSection(indent + "kws.add(new String(\"" + ast.getName() +  "\"));\n" );
		kws.add(new String(ast.getName()));
	}
	
	@Override
	public void visit(ASTLexString ast) 
	{
		addToCodeSection(indent + "kws.add(new String(\"" + ast.getString() +  "\"));\n" );
		kws.add(new String(ast.getString()));
	}
	
	
	@Override
	public void handle(ASTEncodeTableProd ast)
	{
		
		
		String name = ast.getName().substring(0,ast.getName().length()-3);
		
		name = resolveName(name);
		
		String left = "";
		String right = "";
		String startEncoding = "";
		List<ASTEncodeTableEntry> entries = ast.getEncodeTableEntries();
		Map<String, String> map = Maps.newHashMap();
	
		for(ASTEncodeTableEntry entry:entries)
		{		
			if(entry.getChar().isPresent())
			{
			left = entry.getChar().get();
			}
			else if(entry.getString().isPresent())
			{
			left = entry.getString().get();	
			}
			
			right = entry.getReplacement();
			map.put(left, right);
			if(right.substring(0, 1).contains(left)){
				startEncoding = right;
			}
		}
		if(startEncoding.equals("")){
			System.err.println("NO START ENCODING FOUND FOR " + ast.getName().substring(0,ast.getName().length()-3));
			System.exit(2);
		}
		addToCustomEncoding("Map<String, String> map" + name + " = new HashMap<String, String>();");
		addEncodingMapToCodeSection(map, name);
		addToCustomEncoding("customEncodings.add(new Encoding(" + name + ", " + "map" + name + ", " + "\""+startEncoding + "\"" +"));\n");
		customEncodingMap.put(name, map);
	}
	

	// ----------------- End of visit methods ---------------------------------------------
	
	public List<String> createUsableSymbolsCode(ASTProd ast)
	{
		clearUsableSymbolsCode();
		startCodeSection(ast);
		ast.accept(getRealThis());
		endCodeSection(ast);
		return getUsableSymbolsCode();
	}
	public List<String> createCustomEncodingCode(ASTProd ast)
	{
		customEncoding.clear();
		//startCodeSection(ast);
		ast.accept(getRealThis());
		//endCodeSection(ast);
		return getCustomeEncoding();
	}
	// ----------------------------------------------------------

	/**
	 * Gets the antlr code (for printing)
	 * 
	 * @return
	 */
	private List<String> getUsableSymbolsCode()
	{
		return ImmutableList.copyOf(productionUsableSymbolsCode);
	}
	
	/**
	 * Adds the given code to antlr code
	 * 
	 *@param code
	 */
	private void addToUsableSymbolsCode(String code) 
	{
		productionUsableSymbolsCode.add(code);
	}
	
	private void addToCustomEncoding(String code) 
	{
		customEncoding.add(code);
	}
	
	public List<String> getCustomeEncoding(){
		return customEncoding;
	}
	/**
	 * Adds the given code to antlr code
	 * 
	 * @param code
	 */
	private void addToUsableSymbolsCode(StringBuilder code) 
	{
	    addToUsableSymbolsCode(code.toString());
	}
	
	/**
	 * Clears antlr code
	 */
	private void clearUsableSymbolsCode() 
	{
		resetIndent();
		productionUsableSymbolsCode.clear();
	}
	
	/**
	 * Starts codeSection of the parser code
	 */
	private void startCodeSection() 
	{
		codeSection = new StringBuilder();
	}
	  
	/**
	 * Adds the current code codeSection to antlr
	 */
	private void endCodeSection() 
	{
		addToUsableSymbolsCode(codeSection);
		codeSection = new StringBuilder();
	}
	  
	/**
	 * Starts antlr code for the given production
	 * 
	 * @param ast
	 */
	private void startCodeSection(ASTNode ast) 
	{
		startCodeSection(ast.getClass().getSimpleName());
	}
	  
	/**
	 * Starts antlr code for the production with the given name
	 */
	private void startCodeSection(String text) 
	{
		codeSection = new StringBuilder("\n \t // Start of '" + text + "'\n");
	}
	  
	/**
	 * Ends antlr code for the given production
	 *	 
	 * @param ast
	 */
	private void endCodeSection(ASTNode ast) 
	{
		codeSection.append("\t  // End of '" + ast.getClass().getSimpleName() + "'\n");
		endCodeSection();
	}
	  
	/**
	 * Adds the given code to the current codeSection
	 */
	private void addToCodeSection(String... code) 
	{
		Arrays.asList(code).forEach(s -> codeSection.append(s));
	}
	  
	/**
	 * @return codeSection
	 */
	public StringBuilder getCodeSection() 
	{
		return this.codeSection;
	}
	
	private void increaseIndent()
	{
		indent += "  ";
	}
	
	private void decreaseIndent()
	{
		indent = indent.substring(0,indent.length()-2);
	}
	
	private void resetIndent()
	{
		indent = "\t\t";
	}
	public String[] getKws(){
		return kws.toArray(new String[kws.size()]);	}
	
	public String[] getRanges(){
		return Range.union(ranges);
	}
	
	public void printTable(){
		for(String c:customEncodingMap.keySet())
		{
			System.out.println("Encoding Table " + c);
			Map<String,String> map = customEncodingMap.get(c);
			for(String m:map.keySet())
			{
				System.out.println(m + " -> " + map.get(m));
			}
			
		}
		
	}
	public void addEncodingMapToCodeSection (Map<String, String> map, String type){
		for(String key: map.keySet()){
			addToCustomEncoding("map" + type + ".put("  + "\"" + key  + "\"" + ", " + "\"" + map.get(key) + "\"" + ");");
		}
	}
	
	private String resolveName(String name){
		Map<String, String> resolved = parserGeneratorHelper.getResolvedTypes();
		for(String r: resolved.keySet()){
			if(r.equals(name)){
				return resolved.get(r);
			}
		}
		System.err.println("TOKEN NAME "+ name + " CANNOT BE RESOLVED");
		return null;
	}

}