package de.monticore.codegen.mchammer.parsetree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.collect.Lists;

import de.monticore.grammar.grammar._ast.ASTMCGrammar;
import de.monticore.grammar.grammar._ast.ASTProd;
import de.monticore.grammar.grammar._ast.ASTAbstractProd;
import de.monticore.grammar.grammar._ast.ASTInterfaceProd;
import de.monticore.languages.grammar.MCRuleSymbol;
import de.monticore.languages.grammar.MCInterfaceOrAbstractRuleSymbol;
import de.monticore.codegen.mchammerparser.McHammerParserGeneratorHelper;
import de.monticore.generating.GeneratorEngine;
import de.monticore.generating.GeneratorSetup;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.grammar.MCGrammarInfo;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.Names;

import de.monticore.codegen.mchammerparser.*;

/**
 * <h1>Generate MCHammer ParseTree</h1>
 * Generates the parsetree classes used by the corresponding generated MCHammerParser
 * 
 * @author Tobias Bieschke
 */
public class McHammerParseTreeGenerator {

	public static final String PARSETREE_PACKAGE = "_mch_parser.tree";
	
	/**
	 * Generate the MCHammer ParseTree
	 * 
	 * @param symbolTyble  SymbolTable of the AST Grammar classes
	 * @param astGrammar AST of the Grammar
	 * @param outputDirectory directory where the generated code will be put
	 */
	public static void generate(Scope symbolTable, ASTMCGrammar astGrammar, File outputDirectory)
	{
		// Initialize GeneratorHelper
		final McHammerParserGeneratorHelper generatorHelper = new McHammerParserGeneratorHelper(astGrammar, symbolTable);
				
		// Generator Setup
		final GeneratorSetup setup = new GeneratorSetup(outputDirectory);
		
		// Glex
		GlobalExtensionManagement glex = new GlobalExtensionManagement();
		glex.addGlobalValue("genHelper", generatorHelper);
		setup.setGlex(glex);
		
		// Grammar Info
		MCGrammarInfo grammarInfo = new MCGrammarInfo(generatorHelper.getGrammarSymbol());
				
		// Initialize GeneratorEngine
		final GeneratorEngine generator = new GeneratorEngine(setup);
		
		// Generate ParseRuleNodes
		List<ASTProd> rules = generatorHelper.getParserRulesToGenerate();
		for(ASTProd rule : rules)
		{
			final Path parseTreePath = Paths.get(Names.getPathFromPackage(generatorHelper.getParseTreePackage()), "PT" + rule.getName() + ".java");
			generator.generate("mchtree.RuleNode", parseTreePath, astGrammar, new Grammar2ParseTree(generatorHelper,grammarInfo), rule);
		}
		
		// Generate Interface/AbstractRuleNodes
		List<MCRuleSymbol> interfaceRules = generatorHelper.getInterfaceRulesToGenerate();
		for(MCRuleSymbol rule : interfaceRules)
		{
			ASTProd prod = (ASTProd)rule.getAstNode().get();
			
			if( prod instanceof ASTInterfaceProd )
			{
				final Path parseTreePath = Paths.get(Names.getPathFromPackage(generatorHelper.getParseTreePackage()), "PT" + prod.getName() + ".java");
				generator.generate("mchtree.InterfaceRuleNode", parseTreePath, astGrammar, new Grammar2ParseTree(generatorHelper,grammarInfo), prod);
			}
			else if( prod instanceof ASTAbstractProd )
			{
				final Path parseTreePath = Paths.get(Names.getPathFromPackage(generatorHelper.getParseTreePackage()), "PT" + prod.getName() + ".java");
				generator.generate("mchtree.AbstractRuleNode", parseTreePath, astGrammar, new Grammar2ParseTree(generatorHelper,grammarInfo), prod);
			}
		}
		
		// Generate TerminalNodes
		rules = Lists.newArrayList();
		rules.addAll(generatorHelper.getLexerRulesToGenerate());
		for(ASTProd rule : rules)
		{
			final Path parseTreePath = Paths.get(Names.getPathFromPackage(generatorHelper.getParseTreePackage()), "PT" + rule.getName() + ".java");
			generator.generate("mchtree.TerminalNode", parseTreePath, astGrammar, new Grammar2ParseTree(generatorHelper,grammarInfo), rule);
		}
				
		
		/*
		// Generate HAParseTree.java
		final Path parseTreePath = Paths.get(Names.getPathFromPackage(generatorHelper.getParseTreePackage()), "HAParseTree.java");
		generator.generate("mchtree.HAParseTree", parseTreePath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));
		
		// Generate HARuleContext.java
		final Path ruleContextPath = Paths.get(Names.getPathFromPackage(generatorHelper.getParseTreePackage()), "HARuleContext.java");
		generator.generate("mchtree.HARuleContext", ruleContextPath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));
		
		// Generate HARuleNode.java
		final Path ruleNodePath = Paths.get(Names.getPathFromPackage(generatorHelper.getParseTreePackage()), "HARuleNode.java");
		generator.generate("mchtree.HARuleNode", ruleNodePath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));
		
		// Generate HATerminalNode.java
		final Path terminalNodePath = Paths.get(Names.getPathFromPackage(generatorHelper.getParseTreePackage()), "HATerminalNode.java");
		generator.generate("mchtree.HATerminalNode", terminalNodePath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));
		
		// Generate HABinaryToken.java
		final Path binaryTokenPath = Paths.get(Names.getPathFromPackage(generatorHelper.getParseTreePackage()), "HABinaryToken.java");
		generator.generate("mchtree.HABinaryToken", binaryTokenPath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));
		*/

	}
}
