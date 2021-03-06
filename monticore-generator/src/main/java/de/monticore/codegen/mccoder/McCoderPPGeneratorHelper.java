/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mccoder;

import static com.google.common.base.Preconditions.checkState;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import de.monticore.ast.ASTNode;
import de.monticore.codegen.mchammerparser.McHammerParserGenerator;
import de.monticore.grammar.grammar._ast.ASTBlock;
import de.monticore.grammar.grammar._ast.ASTClassProd;
import de.monticore.grammar.grammar._ast.ASTConstantGroup;
import de.monticore.grammar.grammar._ast.ASTConstantsGrammar;
import de.monticore.grammar.grammar._ast.ASTLexNonTerminal;
import de.monticore.grammar.grammar._ast.ASTLexProd;
import de.monticore.grammar.grammar._ast.ASTMCGrammar;
import de.monticore.grammar.grammar._ast.ASTNonTerminal;
import de.monticore.grammar.grammar._ast.ASTProd;
import de.monticore.grammar.grammar._ast.ASTTerminal;
import de.monticore.grammar.grammar_withconcepts._ast.ASTAction;
import de.monticore.grammar.grammar_withconcepts._ast.ASTExpressionPredicate;
import de.monticore.grammar.grammar_withconcepts._ast.ASTJavaCode;
import de.monticore.grammar.prettyprint.Grammar_WithConceptsPrettyPrinter;
import de.monticore.java.javadsl._ast.ASTBlockStatement;
import de.monticore.java.javadsl._ast.ASTClassMemberDeclaration;
import de.monticore.languages.grammar.MCClassRuleSymbol;
import de.monticore.languages.grammar.MCEnumRuleSymbol;
import de.monticore.languages.grammar.MCExternalTypeSymbol;
import de.monticore.languages.grammar.MCGrammarSymbol;
import de.monticore.languages.grammar.MCInterfaceOrAbstractRuleSymbol;
import de.monticore.languages.grammar.MCLexRuleSymbol;
import de.monticore.languages.grammar.MCRuleComponentSymbol;
import de.monticore.languages.grammar.MCRuleSymbol;
import de.monticore.languages.grammar.MCRuleSymbol.KindSymbolRule;
import de.monticore.languages.grammar.MCTypeSymbol;
import de.monticore.languages.grammar.MCTypeSymbol.KindType;
import de.monticore.languages.grammar.PredicatePair;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.JavaNamesHelper;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.StringTransformations;
import de.se_rwth.commons.logging.Log;

/**
 * TODO: Write me!
 *
 * @author  (last commit) $Author$
 * @version $Revision$, $Date$
 * @since   TODO: add version number
 *
 */
public class McCoderPPGeneratorHelper {
	private ASTMCGrammar astGrammar;
	  
	private String qualifiedGrammarName;
	
	public McCoderPPGeneratorHelper(ASTMCGrammar ast) 
	{
		Log.errorIfNull(ast);
		this.astGrammar = ast;
		this.qualifiedGrammarName = 
			astGrammar.getPackage().isEmpty() ? 
				astGrammar.getName() :
				Joiner.on('.').join(Names.getQualifiedName(astGrammar.getPackage()), astGrammar.getName());
	}
	  
	/**
	 * @return the qualified grammar's name
	 */
	public String getQualifiedGrammarName() 
	{
		return qualifiedGrammarName;
	}

	/**
	 * @return the package for the generated parser files
	 */
	public String getPPPackage() 
	{
		return getQualifiedGrammarName().toLowerCase() + "." + McCoderPPGenerator.PP_PACKAGE;
	}
	public String getGNameToLower(){
		
		return getQualifiedGrammarName().toLowerCase();
		
	}
	
}
