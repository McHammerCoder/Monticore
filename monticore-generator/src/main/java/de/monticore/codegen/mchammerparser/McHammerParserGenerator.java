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
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.logging.Log;
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
	public static final String PARSER_PACKAGE = "_parser";
	
	public static void generate(GlobalExtensionManagement glex, File outputDirectory, ASTMCGrammar astGrammar)
	{
		// Generator Setup
		final GeneratorSetup setup = new GeneratorSetup(outputDirectory);
		setup.setGlex(glex);
		
		// Initialize GeneratorHelper
		final McHammerParserGeneratorHelper generatorHelper = new McHammerParserGeneratorHelper(astGrammar);
		
		// Initialize GeneratorEngine
		final GeneratorEngine generator = new GeneratorEngine(setup);
		
		// Generate _MCHParser.java
		final Path filePath = Paths.get(Names.getPathFromPackage(generatorHelper.getQualifiedGrammarName()), astGrammar.getName()+"_MCHParser.java");
		generator.generate("MCHParser.Parser", filePath, astGrammar, generatorHelper);
	}
}
