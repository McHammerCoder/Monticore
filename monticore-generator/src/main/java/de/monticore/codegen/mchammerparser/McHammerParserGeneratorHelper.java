/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mchammerparser;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import de.monticore.ast.ASTNode;
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
import de.monticore.symboltable.Scope;
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
public class McHammerParserGeneratorHelper 
{
	public static final String MONTICOREANYTHING = "MONTICOREANYTHING";
	
	private ASTMCGrammar astGrammar;
	  
	private String qualifiedGrammarName;
	
	private MCGrammarSymbol grammarSymbol;
	
	public McHammerParserGeneratorHelper(ASTMCGrammar ast, Scope symbolTable) 
	{
		Log.errorIfNull(ast);
		this.astGrammar = ast;
		this.qualifiedGrammarName = 
			astGrammar.getPackage().isEmpty() ? 
				astGrammar.getName() :
				Joiner.on('.').join(Names.getQualifiedName(astGrammar.getPackage()), astGrammar.getName());
	
		this.grammarSymbol = 			
			symbolTable.<MCGrammarSymbol> resolve(qualifiedGrammarName,
												  MCGrammarSymbol.KIND).orElse(null);
		Log.errorIfNull(grammarSymbol, 
						"0xA4034 Grammar " 
						+ qualifiedGrammarName
						+ " can't be resolved in the scope " 
						+ symbolTable);
	}
	  
	/**
	 * @return the qualified grammar's name
	 */
	public String getQualifiedGrammarName() 
	{
		return qualifiedGrammarName;
	}

	/**
	 * @return the name of the start rule
	 */	
	public String getStartRuleName() 
	{
		if (grammarSymbol.getStartRule().isPresent()) 
		{
			return grammarSymbol.getStartRule().get().getName();
		}

		return "";
	}
	
	/**
	 * @return the name of the start rule in lower case letters
	 */	
	public String getStartRuleNameLowerCase() 
	{
		if (grammarSymbol.getStartRule().isPresent()) 
		{
			return grammarSymbol.getStartRule().get().getName().toLowerCase();
		}

		return "";
	}

	/**
	 * @return the qualified name of the top ast, i.e., the ast of the start rule.
	 */
	public String getQualifiedStartRuleName()
	{
		if (grammarSymbol.getStartRule().isPresent()) 
		{
			return getASTClassName(grammarSymbol.getStartRule().get());
		}
		return "";
	}
	
	/**
	 * @return the package for the generated parser files
	 */
	public String getParserPackage() 
	{
		return getQualifiedGrammarName().toLowerCase() + "." + McHammerParserGenerator.PARSER_PACKAGE;
	}
	
	/**
	 * @return the package for the generated parser files
	 */
	public String getParseTreePackage() 
	{
		return getParserPackage() + "." + McHammerParserGenerator.PARSETREE_PACKAGE;
	}
	
	/**
	 * @return grammarSymbol
	 */
	public MCGrammarSymbol getGrammarSymbol()
	{
		return grammarSymbol;
	}
	
	public List<String> getIndirectRulesToGenerate()
	{
		List<String> prods = Lists.newArrayList();
	    Set<String> ruleNames = grammarSymbol.getRulesWithInherited().keySet();
	    
	    for( Iterator<String> i = ruleNames.iterator(); i.hasNext(); )
	    {
	    	prods.add(i.next().toLowerCase());
	    }
	    
	    return prods;
	}
	
	public List<ASTProd> getParserRulesToGenerate() 
	{
		// Iterate over all Rules
		List<ASTProd> prods = Lists.newArrayList();
		for (MCRuleSymbol ruleSymbol : grammarSymbol.getRulesWithInherited().values()) 
		{
			if (ruleSymbol.getKindSymbolRule().equals(KindSymbolRule.PARSERRULE)) 
			{
				Optional<ASTClassProd> astProd = ((MCClassRuleSymbol) ruleSymbol).getRuleNode();
				if (astProd.isPresent()) 
				{
					prods.add(astProd.get());
				}
			}
			else if (ruleSymbol.getKindSymbolRule().equals(KindSymbolRule.ENUMRULE)) 
			{
				prods.add(((MCEnumRuleSymbol) ruleSymbol).getRule());
	        }
		}
	    return prods;
	}
	
	public List<ASTLexProd> getLexerRulesToGenerate() 
	{
		// Iterate over all LexRules
		List<ASTLexProd> prods = Lists.newArrayList();
		MCLexRuleSymbol mcanything = null;
	    final Map<String, MCRuleSymbol> rules = new LinkedHashMap<>();
	    
	    // Don't use grammarSymbol.getRulesWithInherited because of changed order
	    for (final MCRuleSymbol ruleSymbol : grammarSymbol.getRules()) 
	    {
	    	rules.put(ruleSymbol.getName(), ruleSymbol);
	    }
	    for (int i = grammarSymbol.getSuperGrammars().size() - 1; i >= 0; i--)
	    {
	    	rules.putAll(grammarSymbol.getSuperGrammars().get(i).getRulesWithInherited());
	    }

	    for (Entry<String, MCRuleSymbol> ruleSymbol :rules.entrySet()) 
	    {
	    	if (ruleSymbol.getValue().getKindSymbolRule().equals(KindSymbolRule.LEXERRULE))
	    	{
	    		MCLexRuleSymbol lexRule = ((MCLexRuleSymbol) ruleSymbol.getValue());
	        
	    		// MONTICOREANYTHING must be last rule
	    		if (lexRule.getName().equals(MONTICOREANYTHING)) 
	    		{
	    			mcanything = lexRule;
	    		}
	    		else 
	    		{
	    			prods.add(lexRule.getRuleNode());
	    		}
	    	}
	    }
	    if (mcanything != null)
	    {
	    	prods.add(mcanything.getRuleNode());
	    }
	    return prods;
	}
	
	public List<String> getParserRuleNames()
	{
		// Iterate over all Rules
		List<ASTProd> prods = getParserRulesToGenerate();
		
		List<String> ruleNames = Lists.newArrayList();
	    for( ASTProd prod : prods )
	    {
	    	ruleNames.add(prod.getName());
	    }
	    return ruleNames;
	}
	
	public List<String> getLexerRuleNames()
	{
		// Iterate over all LexRules
		List<ASTLexProd> prods = getLexerRulesToGenerate();
	    
	    List<String> ruleNames = Lists.newArrayList();
	    for( ASTLexProd prod : prods )
	    {
	    	ruleNames.add(prod.getName());
	    }
	    return ruleNames;
	}
	
	public static String getASTClassName(MCRuleSymbol rule) 
	{
		return rule.getType().getQualifiedName();
	}
}

