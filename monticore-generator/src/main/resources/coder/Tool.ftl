${tc.signature("genHelper")}
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>

/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */

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
import org.antlr.v4.runtime.tree.gui.TreeViewer;

import java.nio.file.Files;
import com.upstandinghackers.hammer.ParseResult;

import ${genHelper.getGNameToLower()}._mch_parser.tree.*;
import ${genHelper.getGNameToLower()}._mch_parser.*;
import ${genHelper.getGNameToLower()}._coder.*;


public class ${parserName}Tool 
{	
	public static void main (String [] args) throws IOException
	{
		System.out.println("${parserName}Tool :D");
		
		String inputFile = null;
		//if ( args.length>0 ) 
		{
			/*inputFile = args[0];
			
			Path path = Paths.get(inputFile);
			byte[] data = Files.readAllBytes(path);*/
			
			String msg = "<p><b>Text</b></p>";        
	        char [] carr = msg.toCharArray();
	        byte [] message = new byte [carr.length*2];
	        
	        for( int i = 0; i < carr.length; i++ )
	        {
	        	int cVal = carr[i];
	        	System.out.println("cVal: " + cVal);
	        	message[2*i] = (byte) (cVal >> 8);
	        	message[2*i+1] = (byte) cVal;
	        }
			
			${parserName}Parser parser = new ${parserName}Parser();
			ParseResult res = parser.parse(message);
			
			if( res != null )
			{
				ParseTree pt = ${parserName}TreeConverter.create(res);
				
				System.out.println(pt.getText());
				
				${parserName}Injector injector = new ${parserName}Injector();
				ParseTreeWalker walker = new ParseTreeWalker();
				walker.walk(injector, pt); 
				
				// Encode
				System.out.println();
				System.out.println("Encoded Template:");
				${parserName}EncoderVisitor encoder = new ${parserName}EncoderVisitor();
				walker.walk(encoder, pt); 
				System.out.println();
				
				System.out.println(pt.getText());
				
				// Decode
				System.out.println();
				System.out.println("Decode Template:");
				${parserName}DecoderVisitor decoder = new ${parserName}DecoderVisitor();
				walker.walk(decoder, pt); 
				System.out.println();
				
				System.out.println(pt.getText());
				
				displayParseTree(pt);
			}
			else
			{
				System.out.println("FAILED");
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
        frame.setSize(1900,600);
        frame.setVisible(true);
        
        while(frame.isVisible());
    }
}
