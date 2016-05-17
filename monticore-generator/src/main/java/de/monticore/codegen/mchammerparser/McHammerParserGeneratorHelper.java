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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.monticore.MontiCoreScript;
import de.monticore.ast.ASTNode;
import de.monticore.grammar.grammar._ast.ASTBlock;
import de.monticore.grammar.grammar._ast.ASTBinaryProd;
import de.monticore.grammar.grammar._ast.ASTClassProd;
import de.monticore.grammar.grammar._ast.ASTConstantGroup;
import de.monticore.grammar.grammar._ast.ASTConstantsGrammar;
import de.monticore.grammar.grammar._ast.ASTEnumProd;
import de.monticore.grammar.grammar._ast.ASTLexNonTerminal;
import de.monticore.grammar.grammar._ast.ASTLexProd;
import de.monticore.grammar.grammar._ast.ASTMCGrammar;
import de.monticore.grammar.grammar._ast.ASTNonTerminal;
import de.monticore.grammar.grammar._ast.ASTOffsetProd;
import de.monticore.grammar.grammar._ast.ASTProd;
import de.monticore.grammar.grammar._ast.ASTTerminal;
import de.monticore.grammar.grammar._ast.ASTGrammarOption;
import de.monticore.grammar.grammar._ast.ASTHammerOption;
import de.monticore.grammar.grammar_withconcepts._ast.ASTAction;
import de.monticore.grammar.grammar_withconcepts._ast.ASTExpressionPredicate;
import de.monticore.grammar.grammar_withconcepts._ast.ASTJavaCode;
import de.monticore.grammar.prettyprint.Grammar_WithConceptsPrettyPrinter;
import de.monticore.java.javadsl._ast.ASTBlockStatement;
import de.monticore.java.javadsl._ast.ASTClassMemberDeclaration;
import de.monticore.languages.grammar.MCBinaryRuleSymbol;
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
import de.monticore.languages.grammar.MCOffsetRuleSymbol;
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
	
	/* The logger name for logging from within a Groovy script. */
	static final String LOG_ID = "MCH Parser Generator";
	
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
	public String getParserPackageC()
	{
		return getParserPackage().replace(".", "/");
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
	    
		List<ASTProd> parserRules = getParserRulesToGenerate();
	    
		for(ASTProd parserRule : parserRules)
		{
			prods.add("_" + parserRule.getName());
		}
		
		List<ASTProd> binaryRules = getBinaryRulesToGenerate() ;
	    
		for(ASTProd binaryRule : binaryRules)
		{
			prods.add("_" + binaryRule.getName());
		}
		
		
		List<ASTLexProd> lexerRules = getLexerRulesToGenerate();
	    
		for(ASTLexProd lexerRule : lexerRules)
		{
			prods.add("_" + lexerRule.getName());
		}
		
		List<MCRuleSymbol> interfaceRules = getInterfaceRulesToGenerate();
	    
		for(MCRuleSymbol interfaceRule : interfaceRules)
		{
			prods.add("_" + interfaceRule.getName());
			Grammar2Hammer.addInterface(interfaceRule.getName());
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
	
	public List<ASTProd> getClassRulesToGenerate() 
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
		}
	    return prods;
	}
	
	public List<ASTProd> getEnumRulesToGenerate() 
	{
		// Iterate over all Rules
		List<ASTProd> prods = Lists.newArrayList();
		for (MCRuleSymbol ruleSymbol : grammarSymbol.getRulesWithInherited().values()) 
		{
			if (ruleSymbol.getKindSymbolRule().equals(KindSymbolRule.ENUMRULE)) 
			{
				prods.add(((MCEnumRuleSymbol) ruleSymbol).getRule());
	        }
		}
	    return prods;
	}
	
	public List<ASTProd> getBinaryRulesToGenerate() 
	{
		// Iterate over all Rules
		List<ASTProd> prods = Lists.newArrayList();
		for (MCRuleSymbol ruleSymbol : grammarSymbol.getRulesWithInherited().values()) 
		{
			if (ruleSymbol.getKindSymbolRule().equals(KindSymbolRule.BINARYRULE)) 
			{
				Optional<ASTBinaryProd> astProd = ((MCBinaryRuleSymbol) ruleSymbol).getRuleNode();
				if (astProd.isPresent()) 
				{
					prods.add(astProd.get());
				}
			}
		}
	    return prods;
	}
	
	public List<ASTProd> getOffsetRulesToGenerate() 
	{
		// Iterate over all Rules
		List<ASTProd> prods = Lists.newArrayList();
		for (MCRuleSymbol ruleSymbol : grammarSymbol.getRulesWithInherited().values()) 
		{
			if (ruleSymbol.getKindSymbolRule().equals(KindSymbolRule.OFFSETRULE)) 
			{
				Optional<ASTOffsetProd> astProd = ((MCOffsetRuleSymbol) ruleSymbol).getRuleNode();
				if (astProd.isPresent()) 
				{
					prods.add(astProd.get());
				}
			}
		}
	    return prods;
	}
	
	public List<MCRuleSymbol> getInterfaceRulesToGenerate() 
	{
		List<MCRuleSymbol> interfaceRules = Lists.newArrayList();
		
		for (MCRuleSymbol ruleSymbol : grammarSymbol.getRulesWithInherited()
		    .values()) 
		{
			if (ruleSymbol.getKindSymbolRule().equals(KindSymbolRule.INTERFACEORABSTRACTRULE)) 
			{
				List<PredicatePair> subRules = grammarSymbol.getSubRulesForParsing(ruleSymbol.getName());
    
				if (subRules != null && !subRules.isEmpty()) 
				{
					interfaceRules.add(ruleSymbol);
				}
    		}
		}
		
		return interfaceRules;
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
	
	public List<String> getBinaryRuleNames()
	{
		// Iterate over all LexRules
		List<ASTProd> prods = getBinaryRulesToGenerate();
	    
	    List<String> ruleNames = Lists.newArrayList();
	    for( ASTProd prod : prods )
	    {
	    	ruleNames.add(prod.getName());
	    }
	    return ruleNames;
	}
	
	public static String getASTClassName(MCRuleSymbol rule) 
	{
		return rule.getType().getQualifiedName();
	}
	
/*
	List<String> lexStrings = Lists.newArrayList();
	
	public List<String> getLexStrings()
	{	    
	    return lexStrings;
	}
	
	public int getNumLexStrings()
	{	    
	    return lexStrings.size();
	}
	
	public void setAntlrTokens(List<String> tokens)
	{
		for( String token : tokens )
		{
			if( token.startsWith("'") )
			{
				lexStrings.add(token.substring(1, token.lastIndexOf("'=")));
			}
		}
	}
*/
	
	public boolean parseEntireFile()
	{
		Optional<ASTGrammarOption> options = astGrammar.getGrammarOptions();
		if( options.isPresent() )
		{
			List<ASTHammerOption> hammerOptions = options.get().getHammerOptions();
			
			for( ASTHammerOption hammerOption : hammerOptions )
			{
				if( hammerOption.getName().equals("ParseEntireFile") )
				{
					Log.info("option ParseEntireFile found!", LOG_ID);
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean parseWithoutOverlapingOffsets()
	{
		Optional<ASTGrammarOption> options = astGrammar.getGrammarOptions();
		if( options.isPresent() )
		{
			List<ASTHammerOption> hammerOptions = options.get().getHammerOptions();
			
			for( ASTHammerOption hammerOption : hammerOptions )
			{
				if( hammerOption.getName().equals("ParseWithOverlappingOffsets") )
				{
					Log.info("option ParseWithoutOverlapingOffsets found!", LOG_ID);
					return false;
				}
			}
		}
		
		return true;
	}
}

