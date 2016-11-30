/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mccoder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import de.monticore.grammar.grammar._ast.ASTEncodeTableProd;
import de.monticore.grammar.grammar._ast.ASTEncodeTableEntry;
import de.monticore.languages.grammar.MCEncodeTableRuleSymbol;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
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
	public static final String CODER_PACKAGE = "_coder";
	
	public static void generate(Scope symbolTable, File outputDirectory, ASTMCGrammar astGrammar)
	{ 
	    
		// Initialize GeneratorHelper
		final McCoderGeneratorHelper generatorHelper = new McCoderGeneratorHelper(astGrammar, symbolTable);
		
		// Generator Setup
		final GeneratorSetup setup = new GeneratorSetup(outputDirectory);
		GlobalExtensionManagement glex = new GlobalExtensionManagement();
		glex.addToGlobalVar("genHelper", generatorHelper);
		setup.setGlex(glex);
		
		// Grammar Info
		MCGrammarInfo grammarInfo = new MCGrammarInfo(generatorHelper.getGrammarSymbol());
		
		// Initialize GeneratorEngine
		final GeneratorEngine generator = new GeneratorEngine(setup);
		de.monticore.codegen.mchammerparser.McHammerParserGeneratorHelper tmpGenHelper = new de.monticore.codegen.mchammerparser.McHammerParserGeneratorHelper(astGrammar, symbolTable);
		final Path tokenPath = Paths.get(outputDirectory.getPath(), Names.getPathFromPackage(tmpGenHelper.getParserPackage()), astGrammar.getName()+".tokens");
		List<String> tokens = Lists.newArrayList();
		try 
		{
			tokens = Files.readAllLines(tokenPath);
		} 
		catch (IOException e)
		{
			System.err.println("[ERR] Could not load "+ astGrammar.getName() + ".tokens!");
			e.printStackTrace();			
		}
		
		generatorHelper.setTokenTypes(tokens);
		generatorHelper.resolveTokenTypes(tokens);
		String outputdir = outputDirectory.getPath() + "/" + astGrammar.getName().toLowerCase() + "/"+ CODER_PACKAGE;
		
		// Generate Decoder.java
		final Path filePath = Paths.get(Names.getPathFromPackage(generatorHelper.getCoderPackage()), astGrammar.getName()+"Decoder.java");
		generator.generate("coder.Decoder", filePath, astGrammar, new UsableSymbolExtractor(generatorHelper,grammarInfo), outputdir);
		
		// Generate DecoderVisitor.java
		final Path filePathDVisitor = Paths.get(Names.getPathFromPackage(generatorHelper.getCoderPackage()), astGrammar.getName()+"DecoderVisitor.java");
		generator.generate("coder.DecoderVisitor", filePathDVisitor, astGrammar, new UsableSymbolExtractor(generatorHelper,grammarInfo));
		
		// Generate Encoder.java
		final Path filePathEncoder = Paths.get(Names.getPathFromPackage(generatorHelper.getCoderPackage()), astGrammar.getName()+"Encoder.java");
		generator.generate("coder.Encoder", filePathEncoder, astGrammar, new UsableSymbolExtractor(generatorHelper,grammarInfo), outputdir);
	
		// Generate EncoderVisitor.java
		final Path filePathEVisitor = Paths.get(Names.getPathFromPackage(generatorHelper.getCoderPackage()), astGrammar.getName()+"EncoderVisitor.java");
		generator.generate("coder.EncoderVisitor", filePathEVisitor, astGrammar, new UsableSymbolExtractor(generatorHelper,grammarInfo));
				
		// Generate CoderHelper.java
		UsableSymbolExtractor ex = new UsableSymbolExtractor(generatorHelper,grammarInfo);
		
		final Path filePathEncodingHelper = Paths.get(Names.getPathFromPackage(generatorHelper.getCoderPackage()), astGrammar.getName()+"CoderHelper.java");
		generator.generate("coder.CoderHelper", filePathEncodingHelper, astGrammar, ex);
		
		
		final Path filePathEncodingGenerator = Paths.get(Names.getPathFromPackage(generatorHelper.getCoderPackage()), astGrammar.getName()+"EncodingGenerator.java");
		generator.generate("coder.EncodingGenerator", filePathEncodingGenerator, astGrammar, ex, outputdir);
		
		final Path filePathEncodings = Paths.get(Names.getPathFromPackage(generatorHelper.getCoderPackage()), astGrammar.getName()+"Encodings.java");
		generator.generate("coder.Encodings", filePathEncodings, astGrammar, ex);
		
		final Path filePathCoderCoder = Paths.get(Names.getPathFromPackage(generatorHelper.getCoderPackage()), astGrammar.getName()+"CoderCoder.java");
		generator.generate("coder.CoderCoder", filePathCoderCoder, astGrammar, ex);
		
	}
	private McCoderGenerator() {
	    // noninstantiable
	  }
}
