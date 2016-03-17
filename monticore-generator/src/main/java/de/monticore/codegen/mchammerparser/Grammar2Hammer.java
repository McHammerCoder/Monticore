/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mchammerparser;

import static de.monticore.codegen.parser.ParserGeneratorHelper.getMCRuleForThisComponent;
import static de.monticore.codegen.parser.ParserGeneratorHelper.getTmpVarNameForAntlrCode;
import static de.monticore.codegen.parser.ParserGeneratorHelper.printIteration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.monticore.ast.ASTNode;
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
public class Grammar2Hammer implements Grammar_WithConceptsVisitor
{
	
	
	private MCGrammarSymbol grammarEntry;
	
	private McHammerParserGeneratorHelper parserGeneratorHelper;
	
	private MCGrammarInfo grammarInfo;
	
	private List<String> productionHammerCode = Lists.newArrayList();
	
	private StringBuilder codeSection;
	  
	
	public Grammar2Hammer(McHammerParserGeneratorHelper parserGeneratorHelper, MCGrammarInfo grammarInfo) 
	{
		Preconditions.checkArgument(parserGeneratorHelper.getGrammarSymbol() != null);
		this.parserGeneratorHelper = parserGeneratorHelper;
		this.grammarEntry = parserGeneratorHelper.getGrammarSymbol();
		this.grammarInfo = grammarInfo;
	}
	
	@Override
	public void handle(ASTLexProd ast) 
	{
		startCodeSection("ASTLexProd");
		endCodeSection();
	}

	@Override
	public void handle(ASTClassProd ast)
	{
		startCodeSection("ASTClassProd");
		addToCodeSection("\t\t" + ast.getName() + ".bindIndirect();\n");
		endCodeSection();
	}
	
	@Override
	public void handle(ASTEnumProd ast)
	{
		startCodeSection("ASTEnumProd");
		endCodeSection();
	}
	
	@Override
	public void handle(ASTConstantGroup ast)
	{
		startCodeSection("ASTConstantGroup");
		endCodeSection();
	}
	
	@Override
	public void handle(ASTLexBlock ast) 
	{
		startCodeSection("ASTLexBlock");
		endCodeSection();
	}
	
	@Override
	public void handle(ASTLexSimpleIteration ast) 
	{
		startCodeSection("ASTLexSimpleIteration");
		endCodeSection();
	}
	
	@Override
	public void handle(ASTBlock ast) 
	{
		startCodeSection("ASTBlock");
		endCodeSection();
	}
	
	@Override
	public void visit(ASTTerminal ast) 
	{
		startCodeSection("ASTTerminal");
		endCodeSection();
	}
	
	@Override
	public void visit(ASTLexCharRange ast) 
	{
		startCodeSection("ASTLexCharRange");
		endCodeSection();
	}
	
	@Override
	public void visit(ASTLexChar ast)
	{
		startCodeSection("ASTLexChar");
		endCodeSection();
	}
	
	@Override
	public void visit(ASTLexString ast) 
	{
		startCodeSection("ASTLexString");
		endCodeSection();
	}
	
	@Override
	public void visit(ASTLexActionOrPredicate ast) 
	{
		startCodeSection("ASTLexActionOrPredicate");
		endCodeSection();
	}
	
	@Override
	public void visit(ASTLexNonTerminal ast) 
	{
		startCodeSection("ASTLexNonTerminal");
		endCodeSection();
	}
	
	@Override
	public void visit(ASTLexOption ast) 
	{
		startCodeSection("ASTLexOption");
		endCodeSection();
	}
	
	@Override
	public void visit(ASTSemanticpredicateOrAction ast) 
	{
		startCodeSection("ASTSemanticpredicateOrAction");
		endCodeSection();
	}
	
	@Override
	public void visit(ASTNonTerminal ast) 
	{
		startCodeSection("ASTNonTerminal");
		endCodeSection();
	}
	
	@Override
	public void visit(ASTEof ast)
	{
		startCodeSection("ASTEof");
		endCodeSection();
	}
	  
	@Override
	public void visit(ASTAnything ast) 
	{
		startCodeSection("ASTAnything");
		endCodeSection();
	}
	  
	@Override
	public void visit(ASTMCAnything ast) 
	{	
		startCodeSection("ASTMCAnything");
		endCodeSection();
	}
	  
	@Override
	public void visit(ASTAlt ast) 
	{
		startCodeSection("ASTAlt");
		endCodeSection();
	}
	  
	@Override
	public void endVisit(ASTAlt ast) 
	{
		startCodeSection("end ASTAlt");
		endCodeSection();
	}

	// ----------------- End of visit methods ---------------------------------------------
	
	public List<String> createHammerCode(ASTProd ast)
	{
		clearHammerCode();
		ast.accept(getRealThis());
		return getHammerCode();
	}
	
	// ----------------------------------------------------------

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
}