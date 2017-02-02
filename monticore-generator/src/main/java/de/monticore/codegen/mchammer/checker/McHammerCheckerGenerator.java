/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mchammer.checker;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import de.monticore.codegen.parser.ParserGeneratorHelper;
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

import de.monticore.codegen.mchammerparser.*;

/**
 * Main Generator Class for the MCHammerParser-Generator
 *
 * @author  (last commit) $Author$
 * @version $Revision$, $Date$
 */
public class McHammerCheckerGenerator
{		
	/* The logger name for logging from within a Groovy script. */
	static final String LOG_ID = "MCH Checker Generator";
	
	/**
	 * Generates the parser files
	 * 
	 * @param symbolTable AST grammar symbols
	 * @param astGrammar AST of the input grammar
	 * @param outputDirectory Output directory for the generated parser files
	 */
	public static void generate(Scope symbolTable, ASTMCGrammar astGrammar, File outputDirectory)
	{
		// Initialize GeneratorHelper
		final McHammerParserGeneratorHelper generatorHelper = new McHammerParserGeneratorHelper(astGrammar, symbolTable);
				
		// Generator Setup
		final GeneratorSetup setup = new GeneratorSetup(outputDirectory);
		GlobalExtensionManagement glex = new GlobalExtensionManagement();
		//glex.addToGlobalVar("genHelper", generatorHelper);
		setup.setGlex(glex);
		
		// Grammar Info
		MCGrammarInfo grammarInfo = new MCGrammarInfo(generatorHelper.getGrammarSymbol());
				
		// Initialize GeneratorEngine
		final GeneratorEngine generator = new GeneratorEngine(setup);

		// Generate ${grammarName}Checker.java
		final Path checkerPath = Paths.get(Names.getPathFromPackage(generatorHelper.getParserPackage()), astGrammar.getName()+"Checker.java");
		generator.generate("mchparser.Checker", checkerPath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo),generatorHelper);

	}
}
