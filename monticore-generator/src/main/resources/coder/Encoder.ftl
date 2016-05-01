${tc.signature("coderGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

package ${genHelper.getParserPackage()};

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.*;
import ${genHelper.getGNameToLower()}._parser.${parserName}AntlrLexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.lang.Math;
import de.monticore.codegen.mccoder.*;

public class ${parserName}Encoder{

	
	private ${parserName}CoderHelper coderHelper = new ${parserName}CoderHelper();
	
	public ${parserName}AntlrLexer lex(String string){
		ANTLRInputStream input = new ANTLRInputStream(string);
		${parserName}AntlrLexer lexer = new ${parserName}AntlrLexer(input);
		return lexer;

	}


	  public boolean check(CommonToken receivedtoken){
	/*
	Receives a tokens text, lexes the text and compares
	the received text with the text of the first token from the lexer
	the text should be the same if there was no tampering with the token
	returnes false if it is not the same since an injection may have occured there
	*/
		 String originaltext = receivedtoken.getText();
		Token nextToken = lex(originaltext).nextToken();
		ArrayList<Encoding> allEncodings = coderHelper.getAllEncodings();
		for(Encoding encoding : allEncodings){
			//System.out.println("HERE I AM");
			if(encoding.getMap().size() != 0 && receivedtoken.getType() == encoding.getType()) {
				//System.out.println("ONCE AGAIN");
				//printEncoding(encodingMap.getMap(), encodingMap.getType());
				 Map<String, String> map = encoding.getMap();
				for( String key : map.keySet()){
				//System.out.println(encodingMap.getMap().get(encodingMap.getStartEncoding()));
					if(encoding.getStartEncoding().equals(map.get(key)) && originaltext.contains(key)){
						//System.out.println("RIGHT IN THE MIDDLE OF DANGER");
						 return false;
					}
				}
			}	
		}
		if((originaltext.equals(nextToken.getText()) && receivedtoken.getType() == nextToken.getType())){
			return true;
			}
		if(!coderHelper.hasEncoding(receivedtoken.getType())){
			System.out.println("Problem with token: " + originaltext + " terminating.");
			System.exit(2);		
			return false;
		}
		return false;
	}

	public boolean typeCheck(int type, String string){
		${parserName}AntlrLexer lexer = lex(string);
		Token nextToken =lexer.nextToken();

		if(type == nextToken.getType() && lexer.nextToken().getType() == Token.EOF){
			return true;
			}
			return false;
	}

	public void encode (CommonToken toEncode){ //Encodes a token and sets it text to the encoded variant

			String encodedString = toEncode.getText(); //CAREFUL can cause problems use decode method
			@SuppressWarnings("unchecked")
			Encoding encoding = coderHelper.getEncoding(toEncode.getType());
			Map<String, String> map = (Map<String, String>) encoding.getMap();
			String startEncoding = encoding.getStartEncoding();
			for(String key: map.keySet()){
				if(startEncoding.equals(map.get(key))){
					encodedString = encodedString.replace(key, map.get(key));
				}
			
			}
		
			for(String key: map.keySet()){
				if(!startEncoding.equals(map.get(key))){
				encodedString = encodedString.replace(key, map.get(key));
				}
			}
		
			if(typeCheck(toEncode.getType(), encodedString)){
				toEncode.setText(encodedString);
			}
			else
			{
				System.err.println("Type missmatch while encoding [exit code 2]"); //Something has gone horribly wrong
				System.exit(2);
			}
		}

}
