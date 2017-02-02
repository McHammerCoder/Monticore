${tc.signature("coderGenerator","outputFolder","genHelper")}
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

package ${genHelper.getCoderPackage()};

import ${parserName?lower_case}._mch_parser.${parserName}Checker;
import de.monticore.mchammerparser.*;

import org.antlr.v4.runtime.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.lang.Math;
import de.monticore.codegen.mccoder.*;

public class ${parserName}Encoder{

	
	private ${parserName}CoderHelper coderHelper = new ${parserName}CoderHelper();
	private ${parserName}Encodings encodings = new ${parserName}Encodings();


	public ${parserName}Encoder(){
	      try
	      {
	         FileInputStream fileIn = new FileInputStream("${outputFolder}/${parserName}Encodings.ser");
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         encodings = (${parserName}Encodings) in.readObject();
	         in.close();
	         fileIn.close();
	      }catch(IOException i)
	      {
	         i.printStackTrace();
	         return;
	      }catch(ClassNotFoundException c)
	      {
	         System.err.println("[ERR] Encodings class not found, serialized object missing");
	         c.printStackTrace();
	         return;
	      }
	}

	  public boolean check(Token receivedtoken) throws Exception{
	/*
	Receives a tokens text, checks the text and compares
	the received text with the text of the first token from the checker
	the text should be the same if there was no tampering with the token.
	It also compares the type and finds if a partial keyword is at the begining or end
	returns false if it is not the same since an injection may have occured there
	*/
		String originaltext = receivedtoken.getText();
		
		if( receivedtoken instanceof CommonToken )
		{
			ArrayList<Encoding> allEncodings = encodings.getAllEncodings();
			for(Encoding encoding : allEncodings){
			
				if(encoding.getMap().size() != 0 && receivedtoken.getType() == encoding.getType()) {
					 Map<String, String> map = encoding.getMap();
					for( String key : map.keySet()){
						if(encoding.getStartEncoding().equals(map.get(key)) && originaltext.contains(key)){
							 return false;
						}
					}
				}	
			}
		}

		if(${parserName}Checker.check( receivedtoken ))
		{
			return true;
		}
		if(!encodings.hasEncoding(receivedtoken.getType())){
			System.err.println("[ERR] Token: " + originaltext + "should be encoded but no encoding is present, terminating.");
			throw new Exception();
		}
		return false;
	}

	/*
	Encodes a token and sets it text to the encoded variant
	Currently does NOT encode binary tokens
	*/
	public void encode (Token token) throws Exception{
	
		if( token instanceof HABinarySequenceToken )
			return ;
			
		CommonToken toEncode = (CommonToken) token;

		String encodedString = toEncode.getText();
		@SuppressWarnings("unchecked")
		Encoding encoding = encodings.getEncoding(toEncode.getType());
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
	
		if(${parserName}Checker.check(toEncode.getType(), encodedString)){
			toEncode.setText(encodedString);
		}
		else
		{
			 //Something has gone horribly wrong
			System.err.println("[ERR] Type missmatch while encoding");
			throw new Exception();
		}
	}

}
