/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mccoder;

import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.monticore.grammar.grammar._ast.ASTEncodeTableProd;
import de.monticore.grammar.grammar._ast.ASTEncodeTableEntry;
import de.monticore.languages.grammar.MCEncodeTableRuleSymbol;
import de.monticore.ast.ASTNode;
import de.monticore.codegen.mccoder.McCoderGenerator;
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
public class McCoderGeneratorHelper 
{
	public static final String MONTICOREANYTHING = "MONTICOREANYTHING";
	
	private ASTMCGrammar astGrammar;
	  
	private String qualifiedGrammarName;
	
	private MCGrammarSymbol grammarSymbol;
	
	private int tokenTypes;
	
	public Map<String, String> resolvedTypes = Maps.newHashMap();;
	
	public McCoderGeneratorHelper(ASTMCGrammar ast, Scope symbolTable) 
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
	public String getCoderPackage() 
	{
		return getQualifiedGrammarName().toLowerCase() + "." + McCoderGenerator.CODER_PACKAGE;
	}
	
	
	
	public String getGNameToLower(){
		
		return getQualifiedGrammarName().toLowerCase();
		
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
	    Collection<String> ruleNames = grammarSymbol.getRuleNames();
	    
	    for( String ruleName : ruleNames )
	    {
	    	prods.add(ruleName.toLowerCase());
	    }
	    
	    return prods;
	}
	
	public List<ASTLexProd> getSingleSymbolsToGenerate()
	{
		List<ASTLexProd> prods = Lists.newArrayList();
		MCLexRuleSymbol mcanything = null;
	    final Map<String, MCRuleSymbol> rules = new LinkedHashMap<>();
	    
	    // Don't use grammarSymbol.getRulesWithInherited because of changed order
	    for (final MCRuleSymbol ruleSymbol : grammarSymbol.getRules()) {
	      rules.put(ruleSymbol.getName(), ruleSymbol);
	    }
	    for (int i = grammarSymbol.getSuperGrammars().size() - 1; i >= 0; i--) {
	      rules.putAll(grammarSymbol.getSuperGrammars().get(i).getRulesWithInherited());
	    }

	    for (Entry<String, MCRuleSymbol> ruleSymbol :rules.entrySet()) {
	      if (ruleSymbol.getValue().getKindSymbolRule().equals(KindSymbolRule.LEXERRULE)) {
	        MCLexRuleSymbol lexRule = ((MCLexRuleSymbol) ruleSymbol.getValue());
	        
	        // MONTICOREANYTHING must be last rule
	        if (lexRule.getName().equals(MONTICOREANYTHING)) {
	          mcanything = lexRule;
	        }
	        else {
	          prods.add(lexRule.getRuleNode());
	        }
	      }
	    }
	    if (mcanything != null) {
	      prods.add(mcanything.getRuleNode());
	    }
	    return prods;
		
		
	}
	public List<ASTLexProd> getLexerRulesToGenerate() {
	    // Iterate over all LexRules
	    List<ASTLexProd> prods = Lists.newArrayList();
	    MCLexRuleSymbol mcanything = null;
	    final Map<String, MCRuleSymbol> rules = new LinkedHashMap<>();
	    
	    // Don't use grammarSymbol.getRulesWithInherited because of changed order
	    for (final MCRuleSymbol ruleSymbol : grammarSymbol.getRules()) {
	      rules.put(ruleSymbol.getName(), ruleSymbol);
	    }
	    for (int i = grammarSymbol.getSuperGrammars().size() - 1; i >= 0; i--) {
	      rules.putAll(grammarSymbol.getSuperGrammars().get(i).getRulesWithInherited());
	    }

	    for (Entry<String, MCRuleSymbol> ruleSymbol :rules.entrySet()) {
	      if (ruleSymbol.getValue().getKindSymbolRule().equals(KindSymbolRule.LEXERRULE)) {
	        MCLexRuleSymbol lexRule = ((MCLexRuleSymbol) ruleSymbol.getValue());
	        
	        // MONTICOREANYTHING must be last rule
	        if (lexRule.getName().equals(MONTICOREANYTHING)) {
	          mcanything = lexRule;
	        }
	        else {
	          prods.add(lexRule.getRuleNode());
	        }
	      }
	    }
	    if (mcanything != null) {
	      prods.add(mcanything.getRuleNode());
	    }
	    return prods;
	  }
	
	public List<ASTProd> getParserRulesToGenerate() 
	{
		// Iterate over all Rules
		List<ASTProd> prods = Lists.newArrayList();
		for(MCGrammarSymbol mcgrammarsymbol : grammarSymbol.getAllSuperGrammars()){
			
			for (MCRuleSymbol ruleSymbol : mcgrammarsymbol.getRulesWithInherited().values()) 
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
		}
		
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
	
	public List<ASTProd> getEncodingTablesToGenerate() 
	{
		// Iterate over all Rules
		List<ASTProd> prods = Lists.newArrayList();
		for(MCGrammarSymbol mcgrammarsymbol : grammarSymbol.getAllSuperGrammars()){
			
			for (MCRuleSymbol ruleSymbol : mcgrammarsymbol.getRulesWithInherited().values()) 
			{
				if (ruleSymbol.getKindSymbolRule().equals(KindSymbolRule.ENCODETABLERULE)) 
				{
					Optional<ASTEncodeTableProd> astProd = ((MCEncodeTableRuleSymbol) ruleSymbol).getRuleNode();
					if (astProd.isPresent()) 
					{
						prods.add(astProd.get());
					}
				}
			}
		}
		for (MCRuleSymbol ruleSymbol : grammarSymbol.getRulesWithInherited().values()) 
		{
			if (ruleSymbol.getKindSymbolRule().equals(KindSymbolRule.ENCODETABLERULE)) 
			{
				Optional<ASTEncodeTableProd> astProd = ((MCEncodeTableRuleSymbol) ruleSymbol).getRuleNode();
				if (astProd.isPresent()) 
				{
					prods.add(astProd.get());
				}
			}
		}
	    return prods;
	}
	
	public static String getASTClassName(MCRuleSymbol rule) 
	{
		return rule.getType().getQualifiedName();
	}
	
	public int getTokenTypes()
	{
		return tokenTypes;		
	}
	public void resolveTokenTypes(List<String> tokens)
	{
		for(String token: tokens) {
			if( !token.startsWith("'") && token.contains("=") ){
					String left = token.substring(0 ,token.indexOf('='));
					String right = token.substring(token.indexOf('=')+1, token.indexOf('=')+2);
					//System.out.println(left + " LEFT | RIGHT " + right);
					resolvedTypes.put(left, right);
			}
		}
	}
	
	public Map<String, String> getResolvedTypes(){
		return resolvedTypes;
	}
	
	public void setTokenTypes(List<String> tokens)
	{
		for( String token : tokens )
		{
			if( !token.startsWith("'") )
			{
				tokenTypes++;
			}
		}
		tokenTypes -= 3;
	}
}
