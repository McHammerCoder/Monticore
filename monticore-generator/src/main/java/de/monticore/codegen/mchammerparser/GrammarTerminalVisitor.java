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
import java.util.LinkedList;
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
import de.monticore.grammar.grammar._ast.ASTConstantsGrammar;
import de.monticore.grammar.grammar._ast.ASTEnumProd;
import de.monticore.grammar.grammar._ast.ASTEof;
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
public class GrammarTerminalVisitor implements Grammar_WithConceptsVisitor
{
	private LinkedList<String> terminals = new LinkedList<String>();
	
	@Override
	public void handle(ASTClassProd ast)
	{
		List<ASTAlt> alts = ast.getAlts();
		for( int i = 0; i < alts.size(); i++ )
		{
			ASTAlt alt = alts.get(i);
			alt.accept(getRealThis());
		}
	}
	
	@Override
	public void handle(ASTEnumProd ast)
	{
	}
	
	@Override
	public void handle(ASTConstantGroup ast)
	{
	}
	
	@Override
	public void handle(ASTBlock ast) 
	{		
		List<ASTAlt> alts = ast.getAlts();
		for( int i = 0; i < alts.size(); i++ )
		{
			ASTAlt alt = alts.get(i);
			alt.accept(getRealThis());
		}
	}
	
	@Override
	public void visit(ASTTerminal ast) 
	{
		terminals.add(ast.getName());
	}
	
	//@Override
	public void handle(ASTLexProd ast) 
	{
		List<ASTLexAlt> alts = ast.getAlts();
		for( int i = 0; i < alts.size(); i++ )
		{
			ASTLexAlt alt = alts.get(i);
			alt.accept(getRealThis());
		}
	}

	@Override
	public void handle(ASTLexBlock ast) 
	{		
		List<ASTLexAlt> alts = ast.getLexAlts();
		for( int i = 0; i < alts.size(); i++ )
		{
			ASTLexAlt alt = alts.get(i);
			alt.accept(getRealThis());
		} 
	}
	
	@Override
	public void handle(ASTLexSimpleIteration ast) 
	{
	}
	
	@Override
	public void visit(ASTLexCharRange ast) 
	{
	}

	@Override
	public void visit(ASTLexChar ast)
	{
		terminals.add(ast.getChar());
	}
	
	@Override
	public void visit(ASTLexString ast) 
	{
		terminals.add(ast.getString());
	}
	
	@Override
	public void visit(ASTLexActionOrPredicate ast) 
	{
	}
	
	@Override
	public void visit(ASTLexNonTerminal ast) 
	{
	}
	
	@Override
	public void visit(ASTLexOption ast) 
	{
	}
	
	@Override
	public void visit(ASTSemanticpredicateOrAction ast) 
	{
	}
	
	@Override
	public void visit(ASTNonTerminal ast) 
	{
	}
	
	@Override
	public void visit(ASTEof ast)
	{
	}
	  
	@Override
	public void visit(ASTAnything ast) 
	{
	}
	  
	@Override
	public void visit(ASTMCAnything ast) 
	{
	}
	  
	@Override
	public void handle(ASTAlt alt) 
	{
		List<ASTRuleComponent> components = alt.getComponents();
		
		for( int i = 0; i < components.size(); i++ )
		{
			components.get(i).accept(getRealThis());
		}
	}
	
	@Override
	public void handle(ASTLexAlt alt)
	{
		List<ASTLexComponent> components = alt.getLexComponents();
		
		for( int i = 0; i < components.size(); i++ )
		{
			components.get(i).accept(getRealThis());
		}
	}

	// ----------------- End of visit methods ---------------------------------------------
	
	public List<String> getTerminalNames(ASTProd ast)
	{
		terminals.clear();
		ast.accept(getRealThis());
		return terminals;
	}
}
