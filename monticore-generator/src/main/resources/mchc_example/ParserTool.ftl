${tc.signature("genHelper")}
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>

/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */

package ${genHelper.getParserPackage()};

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import java.lang.Exception;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.gui.TreeViewer;

import java.nio.file.Files;
import com.upstandinghackers.hammer.ParseResult;

import ${genHelper.getGNameToLower()}._mch_parser.tree.*;
import ${genHelper.getGNameToLower()}._mch_parser.*;


public class ${parserName}ParserTool 
{	
	public static void main (String [] args) throws IOException
	{
		System.out.println("${parserName}Tool :D");
		
		String inputFile = new String();
		String injection = new String();
		if ( args.length>1 ) 
		{
			// Input File
			inputFile = args[0];
			injection = args[1];
			
			Path path = Paths.get(inputFile);
			byte[] data = Files.readAllBytes(path);
			
			// Parser
			${parserName}Parser parser = new ${parserName}Parser();
			
			try 
			{
				ParseTree pt = parser.parse(data);
				
				System.out.println();
				System.out.println("ParseResult:");
				System.out.println(pt.getText());
				
				displayParseTree(pt);
			}
			catch ( Exception ex )
			{
				System.err.println(ex.getMessage());
			}
		}
	}
	
	public static void displayParseTree(ParseTree tree)
    {
        JFrame frame = new JFrame("Antlr AST");
        JPanel panel = new JPanel();
        TreeViewer viewr = new TreeViewer(Arrays.asList( ${parserName}TreeHelper.RuleTypeNames ),tree);
        viewr.setScale(1.5);//scale a little
        panel.add(viewr);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,600);
        frame.setVisible(true);
        
        while(frame.isVisible());
    }
}
