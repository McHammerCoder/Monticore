/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mchammer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.monticore.generating.GeneratorEngine;
import de.monticore.generating.GeneratorSetup;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.io.paths.IterablePath;
import de.monticore.symboltable.GlobalScope;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.grammar.grammar._ast.ASTMCGrammar;

/**
 * TODO: Write me!
 *
 * @author  (last commit) $Author$
 * @version $Revision$, $Date$
 * @since   TODO: add version number
 *
 */
public class MCHammerGenerator 
{
	public static void generate(GlobalExtensionManagement glex, File outputDirectory, ASTCDCompilationUnit astClassDiagram, ASTMCGrammar ast)
	{
		final GeneratorSetup setup = new GeneratorSetup(outputDirectory);
		setup.setGlex(glex);
		
		final GeneratorEngine generator = new GeneratorEngine(setup);
		final Path simpleEncoderVisitorFilePath = Paths.get("coder", "encoderVisitor.java");
		generator.generate("testTemplates.EncoderVisitor", simpleEncoderVisitorFilePath, astClassDiagram, ast);
	}
}
