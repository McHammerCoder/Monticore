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

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
//import org.antlr.v4.runtime.tree.gui.TreeViewer;

import java.nio.file.Files;
import com.upstandinghackers.hammer.ParseResult;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import ${genHelper.getGNameToLower()}._mch_parser.tree.*;
import ${genHelper.getGNameToLower()}._mch_parser.*;
import ${genHelper.getGNameToLower()}._coder.*;
import ${genHelper.getGNameToLower()}._coder.pp.*;

public class ${parserName}Tool 
{	
	public static void main (String [] args) throws IOException
	{
		System.out.println("${parserName}Tool :D");
		
		String inputFile = new String();
		String injection = new String();
		String log = new String();
		if ( args.length>1 ) 
		{
			// Input File
			inputFile = args[0];
			injection = args[1];
			if(args.length >2){
				log = args[2];
			}
			
			Path path = Paths.get(inputFile);
			byte[] data = Files.readAllBytes(path);
			
			try 
			{
				// Parser
				${parserName}Parser parser = new ${parserName}Parser();
				ParseTree pt = parser.parse(data);

				// Injector
				${parserName}Injector injector = new ${parserName}Injector(injection);
				ParseTreeWalker walker = new ParseTreeWalker();
				walker.walk(injector, pt); 
				
				System.out.println();
				System.out.println("Injection:");
				System.out.println(pt.getText());
				
				// Encode
				System.out.println();
				System.out.println("Encoded Template:");
				${parserName}EncoderVisitor encoder = new ${parserName}EncoderVisitor();
				long startTime = System.currentTimeMillis();
				walker.walk(encoder, pt); 
				long stopTime = System.currentTimeMillis();
	   			long elapsedTime = stopTime - startTime;
	   			//String s = String.valueOf(elapsedTime);
	   			if(log.equals("log")){toFile(String.valueOf(elapsedTime),"encodingTime.txt");}
	    		
				System.out.println();
				
				System.out.println(pt.getText());
				
				${parserName}PP pp = new ${parserName}PP();
				pt = parser.parse(pp.prettyPrint(pt));

				System.out.println();
				System.out.println("ReparseResult:");
				System.out.println(pt.getText());
					
				// Decode
				System.out.println();
				System.out.println("Decode Template:");
				${parserName}DecoderVisitor decoder = new ${parserName}DecoderVisitor();
				long startTime1 = System.currentTimeMillis();
				walker.walk(decoder, pt); 
				long stopTime1 = System.currentTimeMillis();
	   			long elapsedTime1 = stopTime1 - startTime1;
	   			//String s = String.valueOf(elapsedTime);
	   			if(log.equals("log")){toFile(String.valueOf(elapsedTime1),"decodingTime.txt");}
				System.out.println();
				
				System.out.println(pt.getText());
				
				//displayParseTree(pt);
			
			}
			catch ( Exception ex )
			{
				System.err.println(ex.getMessage());
			}
					
		}
	}
	private static void toFile(String content, String filename){
	 

		try
		{
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    fw.write(content + "\n");//appends the string to the file
		    fw.close();
		}
		catch(IOException ioe)
		{
		    System.err.println("IOException: " + ioe.getMessage());
		}
	 }
	 
	/*public static void displayParseTree(ParseTree tree)
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
    }*/
}
