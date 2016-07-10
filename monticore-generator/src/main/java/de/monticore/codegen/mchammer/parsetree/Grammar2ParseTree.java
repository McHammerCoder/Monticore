/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mchammer.parsetree;

import static de.monticore.codegen.parser.ParserGeneratorHelper.getMCRuleForThisComponent;
import static de.monticore.codegen.parser.ParserGeneratorHelper.getTmpVarNameForAntlrCode;
import static de.monticore.codegen.parser.ParserGeneratorHelper.printIteration;

import java.util.*;

import org.apache.commons.lang3.StringEscapeUtils;

import org.codehaus.groovy.tools.shell.IO;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.upstandinghackers.hammer.Hammer;

import de.monticore.ast.ASTNode;
import de.monticore.codegen.mchammerparser.GrammarAnalyzer;
import de.monticore.codegen.mchammerparser.McHammerParserGeneratorHelper;
import de.monticore.codegen.parser.ParserGeneratorHelper;
import de.monticore.codegen.parser.antlr.ASTConstructionActions;
import de.monticore.codegen.parser.antlr.AttributeCardinalityConstraint;
import de.monticore.grammar.DirectLeftRecursionDetector;
import de.monticore.grammar.HelperGrammar;
import de.monticore.grammar.MCGrammarInfo;
import de.monticore.grammar.grammar._ast.ASTAlt;
import de.monticore.grammar.grammar._ast.ASTAnything;
import de.monticore.grammar.grammar._ast.ASTBinaryAlt;
import de.monticore.grammar.grammar._ast.ASTBinaryBlock;
import de.monticore.grammar.grammar._ast.ASTBinaryComponent;
import de.monticore.grammar.grammar._ast.ASTBinaryLength;
import de.monticore.grammar.grammar._ast.ASTBinaryData;
import de.monticore.grammar.grammar._ast.ASTBinaryLengthValue;
import de.monticore.grammar.grammar._ast.ASTBinaryNonTerminal;
import de.monticore.grammar.grammar._ast.ASTBinaryNRepeat;
import de.monticore.grammar.grammar._ast.ASTBinaryProd;
import de.monticore.grammar.grammar._ast.ASTBinarySimpleIteration;
import de.monticore.grammar.grammar._ast.ASTBits;
import de.monticore.grammar.grammar._ast.ASTUBits;
import de.monticore.grammar.grammar._ast.ASTBlock;
import de.monticore.grammar.grammar._ast.ASTClassProd;
import de.monticore.grammar.grammar._ast.ASTConstant;
import de.monticore.grammar.grammar._ast.ASTConstantGroup;
import de.monticore.grammar.grammar._ast.ASTConstantsGrammar;
import de.monticore.grammar.grammar._ast.ASTEnumProd;
import de.monticore.grammar.grammar._ast.ASTEof;
import de.monticore.grammar.grammar._ast.ASTGrammarNode;
import de.monticore.grammar.grammar._ast.ASTInterfaceProd;
import de.monticore.grammar.grammar._ast.ASTLexActionOrPredicate;
import de.monticore.grammar.grammar._ast.ASTLexAlt;
import de.monticore.grammar.grammar._ast.ASTLexBlock;
import de.monticore.grammar.grammar._ast.ASTLexChar;
import de.monticore.grammar.grammar._ast.ASTLexCharRange;
import de.monticore.grammar.grammar._ast.ASTLexComponent;
import de.monticore.grammar.grammar._ast.ASTLexNonTerminal;
import de.monticore.grammar.grammar._ast.ASTLexOption;
import de.monticore.grammar.grammar._ast.ASTLexProd;
import de.monticore.grammar.grammar._ast.ASTLexSimpleIteration;
import de.monticore.grammar.grammar._ast.ASTLexString;
import de.monticore.grammar.grammar._ast.ASTMCAnything;
import de.monticore.grammar.grammar._ast.ASTNonTerminal;
import de.monticore.grammar.grammar._ast.ASTOptionValue;
import de.monticore.grammar.grammar._ast.ASTProd;
import de.monticore.grammar.grammar._ast.ASTRuleComponent;
import de.monticore.grammar.grammar._ast.ASTRuleReference;
import de.monticore.grammar.grammar._ast.ASTSemanticpredicateOrAction;
import de.monticore.grammar.grammar._ast.ASTTerminal;
import de.monticore.grammar.grammar._ast.ASTUInt8;
import de.monticore.grammar.grammar._ast.ASTUInt16;
import de.monticore.grammar.grammar._ast.ASTUInt32;
import de.monticore.grammar.grammar._ast.ASTUInt64;
import de.monticore.grammar.grammar._ast.ASTInt8;
import de.monticore.grammar.grammar._ast.ASTInt16;
import de.monticore.grammar.grammar._ast.ASTInt32;
import de.monticore.grammar.grammar._ast.ASTInt64;
import de.monticore.grammar.grammar._ast.ASTOffset;
import de.monticore.grammar.grammar._ast.ASTOffsetProd;
import de.monticore.grammar.grammar._ast.GrammarNodeFactory;
import de.monticore.grammar.grammar_withconcepts._ast.ASTAction;
import de.monticore.grammar.grammar_withconcepts._visitor.Grammar_WithConceptsVisitor;
import de.monticore.grammar.prettyprint.Grammar_WithConceptsPrettyPrinter;
import de.monticore.java.javadsl._ast.ASTBlockStatement;
import de.monticore.languages.grammar.MCAttributeSymbol;
import de.monticore.languages.grammar.MCGrammarSymbol;
import de.monticore.languages.grammar.MCRuleComponentSymbol;
import de.monticore.languages.grammar.MCRuleSymbol;
import de.monticore.languages.grammar.MCRuleSymbol.KindSymbolRule;
import de.monticore.languages.grammar.MCTypeSymbol;
import de.monticore.languages.grammar.MCTypeSymbol.KindType;
import de.monticore.prettyprint.IndentPrinter;
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
public class Grammar2ParseTree implements Grammar_WithConceptsVisitor
{
	private MCGrammarSymbol grammarEntry;
	
	private McHammerParserGeneratorHelper parserGeneratorHelper;
	
	private static Grammar_WithConceptsPrettyPrinter prettyPrinter;
	
	private MCGrammarInfo grammarInfo;
	
	private List<String> productionHammerCode = Lists.newArrayList();
	
	private StringBuilder codeSection;
	
	private String indent = "\t";
	
	private GrammarAnalyzer grammarAnalyzer = new GrammarAnalyzer();
	
	private List<String> lexStrings = Lists.newArrayList();
	
	public Grammar2ParseTree(McHammerParserGeneratorHelper parserGeneratorHelper, MCGrammarInfo grammarInfo) 
	{
		Preconditions.checkArgument(parserGeneratorHelper.getGrammarSymbol() != null);
		this.parserGeneratorHelper = parserGeneratorHelper;
		this.grammarEntry = parserGeneratorHelper.getGrammarSymbol();
		this.grammarInfo = grammarInfo;
		
		// Find all DataFields in the grammar
		List<ASTProd> rules = parserGeneratorHelper.getParserRulesToGenerate();
		rules.addAll( parserGeneratorHelper.getBinaryRulesToGenerate() );
		
		for( ASTProd rule : rules )
		{			
			lexStrings.addAll(grammarAnalyzer.containsLexStrings(rule));
		}
	}
	
	// ----------------- End of constructor ---------------------------------------------

	
	// ----------------- End of visit methods ---------------------------------------------
	
	public List<String> getInterfaces(ASTProd ast)
	{
		List<String> res = Lists.newArrayList();
		
		if(ast instanceof ASTClassProd)
		{
			List<ASTRuleReference> interfaces = ((ASTClassProd)ast).getSuperInterfaceRule();
			for(int i = 0; i < interfaces.size(); i++)
			{
				res.add("PT" + interfaces.get(i).getName() + ((i < interfaces.size()-1) ? "," : "" ));
			}
		}
		
		return res;
	}
	
	public boolean hasInterfaces(ASTProd ast)
	{
		return !getInterfaces(ast).isEmpty();
	}
	
	public List<String> getSuperClass(ASTProd ast)
	{
		List<String> res = Lists.newArrayList();
		
		if(ast instanceof ASTClassProd)
		{
			List<ASTRuleReference> interfaces = ((ASTClassProd)ast).getSuperRule();
			for(int i = 0; i < interfaces.size(); i++)
			{
				res.add("PT" + interfaces.get(i).getName() + ((i < interfaces.size()-1) ? "," : "" ));
			}
		}
		
		return res;
	}
	
	public boolean hasSuperClass(ASTProd ast)
	{
		return !getSuperClass(ast).isEmpty();
	}
	
	public List<String> getTypeConversion(ASTProd ast)
	{
		List<String> res = Lists.newArrayList();
		
		if(ast instanceof ASTBinaryProd)
		{
			ASTBinaryProd astBin = (ASTBinaryProd) ast;
			if(astBin.getVariable().isPresent())
			{
				String variable = astBin.getVariable().get();
				String variableFirstUp = variable.substring(0, 1).toUpperCase() + variable.substring(1);
				
				if(!astBin.getType().isEmpty())
				{
					String type = "";
					
					for(String pType : astBin.getType())
					{
						type += pType + ".";
					}
					
					type = type.substring(0,type.length()-1);
					
					String method = indent + "public " + type + " get" + variableFirstUp + "() {";
					if(astBin.getBlock().isPresent())
					{
						StringBuffer buffer = new StringBuffer();
					    for (ASTBlockStatement action: ((ASTAction) astBin.getBlock().get()).getBlockStatements()) {
					    	buffer.append(getPrettyPrinter().prettyprint(action));
					    }
							
					    method += "\n" + indent + indent + buffer.toString();
					}
					else if(type.equals("String"))
					{
						method += "\n" + indent + indent + "return new String( new " + parserGeneratorHelper.getQualifiedGrammarName().toLowerCase() + "._coder.pp." + parserGeneratorHelper.getQualifiedGrammarName() + "PP().prettyPrint(this) );";
					}
					else if(type.equals("char"))
					{
						method += "\n" + indent + indent + "return new String( new " + parserGeneratorHelper.getQualifiedGrammarName().toLowerCase() + "._coder.pp." + parserGeneratorHelper.getQualifiedGrammarName() + "PP().prettyPrint(this) ).charAt(0);";
					}
					else if( type.equals("float") || type.equals("double")  || type.equals("int")
							 || type.equals("long") || type.equals("short") || type.equals("byte") )
					{
						
						method += "\n" + indent + indent + "long res = 0;";
						method += "\n" + indent + indent + "HABinarySequenceToken token = (HABinarySequenceToken) getSymbol();";
						method += "\n" + indent + indent + "List<HABinaryEntry> entries = token.getValues();";
						method += "\n" + indent + indent + "for( HABinaryEntry entry : entries )";
						method += "\n" + indent + indent + "{";
						method += "\n" + indent + indent + "\tres = (res << entry.getBitCount()) + (entry.getValue() & (Long.MAX_VALUE >> (64-entry.getBitCount()-1)));"; 
						method += "\n" + indent + indent + "\tSystem.out.println(\"res = \" + res);";
						method += "\n" + indent + indent + "}";
						
						if(type.equals("int"))
							method += "\n" + indent + indent + "return (int) res;";
						else if(type.equals("long"))
							method += "\n" + indent + indent + "return res;";
						else if(type.equals("byte"))
							method += "\n" + indent + indent + "return (byte)res;";
						else if(type.equals("short"))
							method += "\n" + indent + indent + "return (short)res;";
						else if(type.equals("double"))
							method += "\n" + indent + indent + "return Double.longBitsToDouble(res);";
						else if(type.equals("float"))
							method += "\n" + indent + indent + "return Float.intBitsToFloat((int)res);";
					}
					method += "\n" + indent + "}";
					
					res.add(method);
				}
				else
				{
					String method = indent + "public " + variable + " get" + variableFirstUp + "() {";
					if(variable.equals("String"))
					{
						method += "\n" + indent + indent + "return new String( new " + parserGeneratorHelper.getQualifiedGrammarName().toLowerCase() + "._coder.pp." + parserGeneratorHelper.getQualifiedGrammarName() + "PP().prettyPrint(this) );";
					}
					else if(variable.equals("char"))
					{
						method += "\n" + indent + indent + "return new String( new " + parserGeneratorHelper.getQualifiedGrammarName().toLowerCase() + "._coder.pp." + parserGeneratorHelper.getQualifiedGrammarName() + "PP().prettyPrint(this) ).charAt(0);";
					}
					else 
					{
						method += "\n" + indent + indent + "long res = 0;";
						method += "\n" + indent + indent + "HABinarySequenceToken token = (HABinarySequenceToken) getSymbol();";
						method += "\n" + indent + indent + "List<HABinaryEntry> entries = token.getValues();";
						method += "\n" + indent + indent + "for( HABinaryEntry entry : entries )";
						method += "\n" + indent + indent + "{";
						method += "\n" + indent + indent + "\tres = (res << entry.getBitCount()) + (entry.getValue() & (Long.MAX_VALUE >> (64-entry.getBitCount()-1)));"; 
						method += "\n" + indent + indent + "\tSystem.out.println(\"res = \" + res);";
						method += "\n" + indent + indent + "}";
						
						if(variable.equals("int"))
							method += "\n" + indent + indent + "return (int) res;";
						else if(variable.equals("long"))
							method += "\n" + indent + indent + "return res;";
						else if(variable.equals("byte"))
							method += "\n" + indent + indent + "return (byte)res;";
						else if(variable.equals("short"))
							method += "\n" + indent + indent + "return (short)res;";
						else if(variable.equals("double"))
							method += "\n" + indent + indent + "return Double.longBitsToDouble(res);";
						else if(variable.equals("float"))
							method += "\n" + indent + indent + "return Float.intBitsToFloat((int)res);";
					}
					method += "\n" + indent + "}";
					
					res.add(method);
				}
			}
		}
		
		return res;
	}
	
	// ----------------- End of codegen methods ---------------------------------------------
	
	/**
	 * Gets PrettyPrinter for ASTGrammar
	 * 
	 * @return
	 */
	public static Grammar_WithConceptsPrettyPrinter getPrettyPrinter() {
		if (prettyPrinter == null) {
    		prettyPrinter = new Grammar_WithConceptsPrettyPrinter(new IndentPrinter());
    	}
    	return prettyPrinter;
	}
	
	/**
	 * Gets the antlr code (for printing)
	 * 
	 * @return
	 */
	private List<String> getHammerCode()
	{
		return ImmutableList.copyOf(productionHammerCode);
	}
	
	/**
	 * Adds the given code to antlr code
	 * 
	 *@param code
	 */
	private void addToHammerCode(String code) 
	{
		productionHammerCode.add(code);
	}
	
	/**
	 * Adds the given code to antlr code
	 * 
	 * @param code
	 */
	private void addToHammerCode(StringBuilder code) 
	{
	    addToHammerCode(code.toString());
	}
	
	/**
	 * Clears antlr code
	 */
	private void clearHammerCode() 
	{
		resetIndent();
		productionHammerCode.clear();
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
		addToHammerCode(codeSection);
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
		codeSection = new StringBuilder("\n // Start of '" + text + "'\n");
	}
	  
	/**
	 * Ends antlr code for the given production
	 *	 
	 * @param ast
	 */
	private void endCodeSection(ASTNode ast) 
	{
		codeSection.append("// End of '" + ast.getClass().getSimpleName() + "'\n");
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
		indent = "\t";
	}
}
