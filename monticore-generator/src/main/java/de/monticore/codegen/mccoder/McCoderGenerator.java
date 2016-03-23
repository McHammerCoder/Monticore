/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mccoder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Joiner;

import de.monticore.codegen.mccoder.UsableSymbolExtractor;
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
public class McCoderGenerator
{
	public static final String PARSER_PACKAGE = "_coder";
	
	public static void generate(Scope symbolTable, File outputDirectory, ASTMCGrammar astGrammar)
	{
		
		// Initialize GeneratorHelper
		final McCoderGeneratorHelper generatorHelper = new McCoderGeneratorHelper(astGrammar, symbolTable);
		
		// Generator Setup
		final GeneratorSetup setup = new GeneratorSetup(outputDirectory);
		GlobalExtensionManagement glex = new GlobalExtensionManagement();
		glex.addGlobalValue("genHelper", generatorHelper);
		setup.setGlex(glex);
		
		// Grammar Info
		MCGrammarInfo grammarInfo = new MCGrammarInfo(generatorHelper.getGrammarSymbol());
		
		// Initialize GeneratorEngine
		final GeneratorEngine generator = new GeneratorEngine(setup);
		
		
		
		// Generate _Decoder.java
		final Path filePath = Paths.get(Names.getPathFromPackage(generatorHelper.getParserPackage()), astGrammar.getName()+"Decoder.java");
		generator.generate("coder.Decoder", filePath, astGrammar, generatorHelper);
		
		// Generate _DecoderVisitor.java
		final Path filePathDVisitor = Paths.get(Names.getPathFromPackage(generatorHelper.getParserPackage()), astGrammar.getName()+"DecoderVisitor.java");
		generator.generate("coder.DecoderVisitor", filePathDVisitor, astGrammar, generatorHelper);
		
		// Generate _Encoder.java
		final Path filePathEncoder = Paths.get(Names.getPathFromPackage(generatorHelper.getParserPackage()), astGrammar.getName()+"Encoder.java");
		generator.generate("coder.Encoder", filePathEncoder, astGrammar, new UsableSymbolExtractor(generatorHelper,grammarInfo));
	
		// Generate _EncoderVisitor.java
		final Path filePathEVisitor = Paths.get(Names.getPathFromPackage(generatorHelper.getParserPackage()), astGrammar.getName()+"EncoderVisitor.java");
		generator.generate("coder.EncoderVisitor", filePathEVisitor, astGrammar, generatorHelper);
		
		// Generate _Injector.java
		final Path filePathInjector = Paths.get(Names.getPathFromPackage(generatorHelper.getParserPackage()), astGrammar.getName()+"Injector.java");
		generator.generate("coder.Injector", filePathInjector, astGrammar, generatorHelper);
				
	}
	private McCoderGenerator() {
	    // noninstantiable
	  }
}
