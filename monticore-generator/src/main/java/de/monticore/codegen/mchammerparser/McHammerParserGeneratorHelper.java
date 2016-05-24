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
 * Helper class for the MCHammerParser-Generator
 *
 * @author  (last commit) $Author$
 * @version $Revision$, $Date$
 *
 */
public class McHammerParserGeneratorHelper 
{
	public static final String MONTICOREANYTHING = "MONTICOREANYTHING";
	
	private ASTMCGrammar astGrammar;
	  
	private String qualifiedGrammarName;
	
	private MCGrammarSymbol grammarSymbol;
	
	/* The logger name for logging from within a Groovy script. */
	static final String LOG_ID = "MCH Parser Generator Helper";
	
	/**
	 * Constructor
	 * 
	 * @param astGrammar AST of the input grammar
	 * @param symbolTable AST grammar symbols
	 */
	public McHammerParserGeneratorHelper(ASTMCGrammar astGrammar, Scope symbolTable) 
	{
		Log.errorIfNull( astGrammar,
				"MCHC0006 McHammerParserGeneratorHelper can't be initialized: the reference to the grammar ast is null");
		this.astGrammar = astGrammar;
		this.qualifiedGrammarName = 
			astGrammar.getPackage().isEmpty() ? 
				astGrammar.getName() :
				Joiner.on('.').join(Names.getQualifiedName(astGrammar.getPackage()), astGrammar.getName());
	
		this.grammarSymbol = 			
			symbolTable.<MCGrammarSymbol> resolve(qualifiedGrammarName,
												  MCGrammarSymbol.KIND).orElse(null);
		Log.errorIfNull(grammarSymbol, 
						"MCHC0007 Grammar " 
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
	 * @return classname of the given rule in the AST
	 */
	public static String getASTClassName(MCRuleSymbol rule) 
	{
	    return rule.getType().getQualifiedName();
	}
	
	/**
	 * @return the package of the generated parser files
	 */
	public String getParserPackage() 
	{
		return getQualifiedGrammarName().toLowerCase() + "." + McHammerParserGenerator.PARSER_PACKAGE;
	}
	
	/**
	 * @return the package of the generated parser files as path string (i.e.: html._mch_parser -> html/_mch_parser/)
	 */
	public String getParserPackageC()
	{
		return getParserPackage().replace(".", "/");
	}
	
	/**
	 * @return the package of the generated parsetree files
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
	
	/**
	 * @return list of all rule names which require an indirect definition in hammer
	 */
	public List<String> getIndirectRulesToGenerate()
	{
		List<String> prods = Lists.newArrayList();
	    
		// ParserRules
		List<ASTProd> parserRules = getParserRulesToGenerate();
		for(ASTProd parserRule : parserRules)
		{
			prods.add("_" + parserRule.getName());
		}
		
		// BinaryRules
		List<ASTProd> binaryRules = getBinaryRulesToGenerate() ;
		for(ASTProd binaryRule : binaryRules)
		{
			prods.add("_" + binaryRule.getName());
		}
		
		// LexerRules
		List<ASTLexProd> lexerRules = getLexerRulesToGenerate();
		for(ASTLexProd lexerRule : lexerRules)
		{
			prods.add("_" + lexerRule.getName());
		}
		
		// InterfaceRules
		List<MCRuleSymbol> interfaceRules = getInterfaceRulesToGenerate();
		for(MCRuleSymbol interfaceRule : interfaceRules)
		{
			prods.add("_" + interfaceRule.getName());
			Grammar2Hammer.addInterface(interfaceRule.getName());
		}
		
	    return prods;
	}
	
	/**
	 * @return list of all parser rules in the grammar and its inherited grammars
	 */
	public List<ASTProd> getParserRulesToGenerate() 
	{
		// Iterate over all Rules
		List<ASTProd> prods = Lists.newArrayList();
		for (MCRuleSymbol ruleSymbol : grammarSymbol.getRulesWithInherited().values()) 
		{
			// Add corresponding rules
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
	
	/**
	 * @return list of all class rules (parser rules without enums) in the grammar and its inherited grammars
	 */
	public List<ASTProd> getClassRulesToGenerate() 
	{
		// Iterate over all Rules
		List<ASTProd> prods = Lists.newArrayList();
		for (MCRuleSymbol ruleSymbol : grammarSymbol.getRulesWithInherited().values()) 
		{
			// Add corresponding rules
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
	
	/**
	 * @return list of all enum rules in the grammar and its inherited grammars
	 */
	public List<ASTProd> getEnumRulesToGenerate() 
	{
		// Iterate over all Rules
		List<ASTProd> prods = Lists.newArrayList();
		for (MCRuleSymbol ruleSymbol : grammarSymbol.getRulesWithInherited().values()) 
		{
			// Add corresponding rules
			if (ruleSymbol.getKindSymbolRule().equals(KindSymbolRule.ENUMRULE)) 
			{
				prods.add(((MCEnumRuleSymbol) ruleSymbol).getRule());
	        }
		}
	    return prods;
	}
	
	/**
	 * @return list of all binary rules in the grammar and its inherited grammars
	 */
	public List<ASTProd> getBinaryRulesToGenerate() 
	{
		// Iterate over all Rules
		List<ASTProd> prods = Lists.newArrayList();
		for (MCRuleSymbol ruleSymbol : grammarSymbol.getRulesWithInherited().values()) 
		{
			// Add corresponding rules
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
	
	/**
	 * @return list of all offset rules in the grammar and its inherited grammars
	 */
	public List<ASTProd> getOffsetRulesToGenerate() 
	{
		// Iterate over all Rules
		List<ASTProd> prods = Lists.newArrayList();
		for (MCRuleSymbol ruleSymbol : grammarSymbol.getRulesWithInherited().values()) 
		{
			// Add corresponding rules
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
	
	/**
	 * @return list of all interface rules in the grammar and its inherited grammars
	 */
	public List<MCRuleSymbol> getInterfaceRulesToGenerate() 
	{
		// Iterate over all Rules
		List<MCRuleSymbol> interfaceRules = Lists.newArrayList();
		for (MCRuleSymbol ruleSymbol : grammarSymbol.getRulesWithInherited()
		    .values()) 
		{
			// Add corresponding rules
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
	
	/**
	 * @return list of all lexer rules in the grammar and its inherited grammars
	 */
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
	    	// Add corresponding rules
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
	
	/**
	 * @return list of all parser rule names
	 */
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
		
	/**
	 * @return list of all lexer rule names
	 */
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
	
	/**
	 * @return list of all binary rule names
	 */
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
	
	/*	
	public static String getASTClassName(MCRuleSymbol rule) 
	{
		return rule.getType().getQualifiedName();
	}
	*/
	
	/**
	 * If DoNotParseEntireFile is NOT set in the grammar the generated parser will fail if the input is not parsed entirely.
	 * It is recommended NOT to set this flag as it is not compatible with the pretty printer and is considered a bad style.
	 * 
	 * @return false if DoNotParseEntireFile is set in the grammar else true
	 */
	public boolean parseEntireFile()
	{
		Optional<ASTGrammarOption> options = astGrammar.getGrammarOptions();
		if( options.isPresent() )
		{
			List<ASTHammerOption> hammerOptions = options.get().getHammerOptions();
			
			for( ASTHammerOption hammerOption : hammerOptions )
			{
				if( hammerOption.getName().equals("DoNotParseEntireFile") )
				{
					Log.info("option DoNotParseEntireFile found!", LOG_ID);
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * If ParseWithOverlapingOffsets is NOT set in the grammar the generated parser will fail if the parsed offset points to a position of the input that
	 * has already been parsed and does not allow to parse any part if the input twice through offsets.
	 * It is recommended NOT to set this flag as it is not compatible with the pretty printer and is considered a bad style.
	 * 
	 * @return false if ParseWithOverlapingOffsets is set in the grammar else true
	 */
	public boolean parseWithoutOverlapingOffsets()
	{
		Optional<ASTGrammarOption> options = astGrammar.getGrammarOptions();
		if( options.isPresent() )
		{
			List<ASTHammerOption> hammerOptions = options.get().getHammerOptions();
			
			for( ASTHammerOption hammerOption : hammerOptions )
			{
				if( hammerOption.getName().equals("ParseWithOverlapingOffsets") )
				{
					Log.info("option ParseWithOverlapingOffsets found!", LOG_ID);
					return false;
				}
			}
		}
		
		return true;
	}
}

