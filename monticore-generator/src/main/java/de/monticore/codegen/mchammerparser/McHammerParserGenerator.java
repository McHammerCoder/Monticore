/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mchammerparser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Joiner;

import de.monticore.generating.GeneratorEngine;
import de.monticore.generating.GeneratorSetup;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.io.paths.IterablePath;
import de.monticore.languages.grammar.MCGrammarSymbol;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.Scope;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.logging.Log;
import de.monticore.grammar.MCGrammarInfo;
import de.monticore.grammar.grammar._ast.ASTMCGrammar;

/**
 * TODO: Write me!
 *
 * @author  (last commit) $Author$
 * @version $Revision$, $Date$
 * @since   TODO: add version number
 *
 */
public class McHammerParserGenerator
{
	public static final String PARSER_PACKAGE = "_mch_parser";
	
	public static final String PARSETREE_PACKAGE = ".tree";
	
	public static void generate(Scope symbolTable, ASTMCGrammar astGrammar, File outputDirectory)
	{
		// Initialize GeneratorHelper
		final McHammerParserGeneratorHelper generatorHelper = new McHammerParserGeneratorHelper(astGrammar, symbolTable);
				
		// Generator Setup
		final GeneratorSetup setup = new GeneratorSetup(outputDirectory);
		GlobalExtensionManagement glex = new GlobalExtensionManagement();
		glex.addGlobalValue("genHelper", generatorHelper);
		setup.setGlex(glex);
		
		// Grammar Info
		MCGrammarInfo grammarInfo = new MCGrammarInfo(generatorHelper.getGrammarSymbol());
				
		// Initialize GeneratorEngine
		final GeneratorEngine generator = new GeneratorEngine(setup);
		
		// Generate Parser.java
		final Path parserPath = Paths.get(Names.getPathFromPackage(generatorHelper.getParserPackage()), astGrammar.getName()+"Parser.java");
		generator.generate("mchparser.Parser", parserPath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));
	
		// Generate Actions.java
		final Path actionsPath = Paths.get(Names.getPathFromPackage(generatorHelper.getParserPackage()), astGrammar.getName()+"Actions.java");
		generator.generate("mchparser.Actions", actionsPath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));
		
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
		
		// Generate TreeConverter.java
		final Path treeConverterPath = Paths.get(Names.getPathFromPackage(generatorHelper.getParseTreePackage()), astGrammar.getName()+"TreeConverter.java");
		generator.generate("mchtree.TreeConverter", treeConverterPath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));
		
		// Generate TreeHelper.java
		final Path treeHelperPath = Paths.get(Names.getPathFromPackage(generatorHelper.getParseTreePackage()), astGrammar.getName()+"TreeHelper.java");
		generator.generate("mchtree.TreeHelper", treeHelperPath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));
	}
}
