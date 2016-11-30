/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mchammerparser;

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

/**
 * Main Generator Class for the MCHammerParser-Generator
 *
 * @author  (last commit) $Author$
 * @version $Revision$, $Date$
 */
public class McHammerParserGenerator
{
	public static final String PARSER_PACKAGE = "_mch_parser";
	
	public static final String PARSETREE_PACKAGE = "tree";
	
	public static final String RESOURCES_FOLDER = "resources";
	
	/* The logger name for logging from within a Groovy script. */
	static final String LOG_ID = "MCH Parser Generator";
	
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
		glex.addToGlobalVar("genHelper", generatorHelper);
		setup.setGlex(glex);
		
		// Grammar Info
		MCGrammarInfo grammarInfo = new MCGrammarInfo(generatorHelper.getGrammarSymbol());
				
		// Initialize GeneratorEngine
		final GeneratorEngine generator = new GeneratorEngine(setup);

/** TODO: Remove ! */
		// Load Antlr TokenList
/*
		de.monticore.codegen.parser.ParserGeneratorHelper tmpGenHelper = new de.monticore.codegen.parser.ParserGeneratorHelper(astGrammar, generatorHelper.getGrammarSymbol());
 		final Path tokenPath = Paths.get(outputDirectory.getPath(), Names.getPathFromPackage(tmpGenHelper.getParserPackage()), astGrammar.getName()+"AntlrLexer.tokens");
		List<String> tokens = Lists.newArrayList();
		try 
		{
			tokens = Files.readAllLines(tokenPath);
		} 
		catch (IOException e)
		{
			System.out.println("Could not load AntlrLexer.tokens!");
			//e.printStackTrace();			
		}
		
		generatorHelper.setAntlrTokens(tokens);
*/
		
		// Generate ${grammarName}Parser.java
		final Path parserPath = Paths.get(Names.getPathFromPackage(generatorHelper.getParserPackage()), astGrammar.getName()+"Parser.java");
		generator.generate("mchparser.Parser", parserPath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));
		
		// Generate ${grammarName}Actions.java
		final Path actionsPath = Paths.get(Names.getPathFromPackage(generatorHelper.getParserPackage()), astGrammar.getName()+"Actions.java");
		generator.generate("mchparser.Actions", actionsPath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));

		// Generate ${grammarName}.tokens
		final Path tokensPath = Paths.get(Names.getPathFromPackage(generatorHelper.getParserPackage()), astGrammar.getName()+".tokens");
		generator.generate("mchparser.Tokens", tokensPath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));
				
/** TODO: Move to ParseTreeGenerator */
		// Generate ${grammarName}TreeConverter.java
		final Path treeConverterPath = Paths.get(Names.getPathFromPackage(generatorHelper.getParseTreePackage()), astGrammar.getName()+"TreeConverter.java");
		generator.generate("mchtree.TreeConverter", treeConverterPath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));
		
		// Generate ${grammarName}TreeHelper.java
		final Path treeHelperPath = Paths.get(Names.getPathFromPackage(generatorHelper.getParseTreePackage()), astGrammar.getName()+"TreeHelper.java");
		generator.generate("mchtree.TreeHelper", treeHelperPath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));
		
		// Generate libjhammer_${grammarName}.c
		final Path hammerActionPath = Paths.get(RESOURCES_FOLDER, "libjhammer_"+astGrammar.getName()+".c");
		generator.generate("mchparser.com_upstandinghackers_hammer_Hammer", hammerActionPath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));
	
		// Generate ${grammarName}Hammer.java in out/resources directory (needed to seperate the actions of different parsers)
		final Path hammerExtensionPath = Paths.get(RESOURCES_FOLDER,Names.getPathFromPackage("com.upstandinghackers.hammer"), astGrammar.getName()+"Hammer.java");
		generator.generate("mchparser.HammerExtension", hammerExtensionPath, astGrammar, new Grammar2Hammer(generatorHelper,grammarInfo));
	
		// Extract Hammer resources if executed from MontiCore CLI
		Log.debug("extracting McHammerParser resources!", LOG_ID);
		final Path resourcesFolder =  Paths.get(outputDirectory.toString(),RESOURCES_FOLDER);
		List<File> fileList = extractTemporaryResources(resourcesFolder,"resources/.*");
		fileList.addAll( extractTemporaryResources(resourcesFolder,"com/upstandinghackers/hammer/.*") );
		fileList.addAll( extractTemporaryResources(resourcesFolder,"Makefile") );
				
		// Compile resources if executed from MontiCore CLI
		Log.debug("compiling McHammerParser resources!", LOG_ID);
		try
		{
			Process p = Runtime.getRuntime().exec("make all CSOURCES=\"libjhammer_" + astGrammar.getName() + ".c\" REALLY_USE_OBSOLETE_BUILD_SYSTEM=yes",null,resourcesFolder.toFile());
						
			while( p.isAlive() );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
		
	/**
	 * Extracts a files and folders (resources) from its JAR
	 * 
	 * @param outputDirectory Output directory of the extracted files
	 * @param resourcePath Path of the extracted resources inside the JAR
	 * @return A list of all extracted files
	 */
	private static List<File> extractTemporaryResources(Path outputDirectory,String resourcePath)
	{
		try
		{			
			List<String> resources = (List<String>) ResourceList.getResources(Pattern.compile(resourcePath));
			
			// Prepare buffer for data copying
	        byte[] buffer = new byte[1024];
	        int readBytes;
			
	        // Prepare temporary files
	        List<File> tempFiles = Lists.newArrayList();
	        
	        for(String resource : resources)
	        {
	        	if( !resource.endsWith("/") )
	        	{
		        	String resourceOutput = outputDirectory + "/" + resource;
		        	File tempPath = new File(resourceOutput.substring(0,resourceOutput.lastIndexOf("/")));
		        	tempPath.mkdirs();
		        	File tempFile = new File(resourceOutput);       	
		        	tempFile.createNewFile();
		        	
		        	if (!tempFile.exists()) 
		        	{
		 	            throw new FileNotFoundException("File " + tempFile.getAbsolutePath() + " does not exist.");
		 	        }
		        	
		        	// Open and check input stream
			        InputStream is = McHammerParserGenerator.class.getResourceAsStream("/" + resource);
			        if (is == null) 
			        {
			            throw new FileNotFoundException("File " + resource + " was not found inside class path.");
			        }
			        
			        // Open output stream and copy data between source file in JAR and the temporary file
			        OutputStream os = new FileOutputStream(tempFile);
			        try 
			        {
			            while ((readBytes = is.read(buffer)) != -1) 
			            {
			                os.write(buffer, 0, readBytes);
			            }
			        } 
			        finally 
			        {
			            // If read/write fails, close streams safely before throwing an exception
			            os.close();
			            is.close();
			        }
	        	}
	        }
	        
	        return tempFiles;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return new ArrayList<File>();
	}
}
