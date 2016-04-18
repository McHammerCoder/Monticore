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


public class ${parserName}Encoder{

	private ArrayList<${parserName}Encoding> allEncodings = new ArrayList<${parserName}Encoding>();
	private Map<String, String> encodingMap = new HashMap<String, String>(); 
	private Boolean[] hasEncodingArray = new Boolean[(${genHelper.getTokenTypes()}+1)]; //Should be sum of types+1
	private ${parserName}CoderHelper coderHelper = new ${parserName}CoderHelper();
	public ${parserName}Encoder(){
		fillAllEncodings();
	}
	

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
		for(${parserName}Encoding encoding : allEncodings){
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
		if(!hasEncoding(receivedtoken.getType())){
			System.out.println("Problem with token: " + originaltext + " terminating.");
			System.exit(2);		
			return false;
		}
		return false;
	}
	
	public boolean hasEncoding(int type){
		
		return hasEncodingArray[type];

	}
	
	
	public boolean isKeyword(String toCheck){ //This returns if a string matches a keyword.
		String[] allKW = coderHelper.getKeywords();
		String res="";
		for(int i=0; i<allKW.length; i++){
			res+= allKW[i];
			}
		return res.contains(toCheck);

	}


	public boolean typeCheck(int type, String string){
		${parserName}AntlrLexer lexer = lex(string);
		Token nextToken =lexer.nextToken();

		if(type == nextToken.getType() && lexer.nextToken().getType() == Token.EOF){
			return true;
			}
			return false;
	}


	public String[] getUsableSymbols(){
		//Each usable symbol should be lexable. No usable symbol should be a keyword.
		String[] alphanumeric = coderHelper.getFreeSymbols();
		String[] keyword = coderHelper.getKeywords();
		String[] usableSymbols = new String[alphanumeric.length];
		for(int i=0; i< alphanumeric.length; i++){
			for(int j=0; j<keyword.length; j++){
				if(alphanumeric[i].equals(keyword[j])){ 
				break;
			}
		}

			${parserName}AntlrLexer lexer = lex(alphanumeric[i]);
			lexer.removeErrorListeners(); //Removes strange error output in the console - we dont need it!
			if(lexer.nextToken().getType() != Token.EOF){
				usableSymbols[i] = alphanumeric[i];
			 }
		}
		usableSymbols = Arrays.stream(usableSymbols).filter(s -> (s != null && s.length() > 0)).toArray(String[]::new); 
	return usableSymbols;
	}


	private boolean createEncoding(String[] kw, String[] usableSymbols, int type){ //Should create a Map with different encodings
		/*
		Example encoding if a \nin JSsimple
		var -> bcbbbbbb
		=   -> bccbbbbb
		,   -> bcccbbbb
		"   -> bccccbbb
		;   -> bcccccbb
		WS  -> bccccccb
		b   -> bccccccc
		Binär codieren könnte das ganze kleiner machen
		*/
		//int length0 = kw.length;
		//int length1 = 1; //makes for a total length of kw.length+1!
		Map<String, String> encodingMap = new HashMap<String, String>();
		String startEncoding = new String();
		//i == first
		//z == second
		for(int i=0; i< usableSymbols.length; i++){

		String encoding = usableSymbols[i];
			for(int z=i+1; z < usableSymbols.length && z != i; z=(z+1)%usableSymbols.length){ //Second
				

				for(int j=0; j <= (kw.length); j++){


					encoding += convertToString(j, usableSymbols[i], usableSymbols[z], (int) Math.ceil((Math.log10(kw.length+1)/Math.log10(2)))); //[log_2(kw.length+1)]

					if(j != kw.length && !isKeyword(encoding) && typeCheck(type,encoding)){
					encodingMap.put(kw[j], encoding);//Save encoding and kw[j] to map
					//System.out.println(kw[j] + " = " + encoding);
					}
					else if(isKeyword(encoding) || !typeCheck(type,encoding)){ //Our created encoding contains a keyword reset and try again
						encodingMap.clear();
						break;
					}
					else if(j == kw.length){
						//System.out.println(usableSymbols[i] + " = " + encoding);
				
						encodingMap.put(usableSymbols[i], encoding); //Last save the encoding for the start
						startEncoding = encoding;
						//System.out.println("THE GENERATED ENCODING WAS FOR TYPE: " + type);
						allEncodings.add(new ${parserName}Encoding(type, encodingMap , startEncoding));
						printEncoding(encodingMap, type);
						return true;
					}
					encoding = usableSymbols[i]; //Reset first symbol for encoding
				}
			}
		}

		return false;
	}

	private String convertToString( int number, String first, String second, int length)
	{
		String res = new String();
		int highestBit = 1 << length;
		for( int i = 0; i < (length+1); i++ )
		{
			if( ( (number & (highestBit >> i)) >> (length-i) ) == 1 )
			{
				res += first;
			}
			else
			{
				res += second;
			}
		}
		return res;
	}
	
	public void fillAllEncodings(){
		
		String[] usableSymb = getUsableSymbols();
		String[] kw = coderHelper.getKeywords();
		for(int j = (kw.length+1) ; j<=${genHelper.getTokenTypes()}; j++){
			hasEncodingArray[j] = createEncoding(kw, usableSymb, (j));
			if(!hasEncodingArray[j]) System.out.println("NO ENCODING FOUND FOR TYPE: " + j);
					
		
		}
		/*for(Encoding encodingMap : allEncodings){
			printEncoding(encodingMap.getMap(), encodingMap.getType());
		}*/

	}


	public ${parserName}Encoding getEncoding(int type) { //Returns the map if none exists one is created
		for(${parserName}Encoding encodingMap : allEncodings){
			if(encodingMap.getMap().size() != 0 && type == encodingMap.getType()) {
				return encodingMap;
			}	
		}
		System.out.println("NO SUCH MAP WAS FOUND: " + type + "\n Something went wrong terminating.");
		System.exit(4);
		return null;
	}

	public void printEncoding(Map <String,String> map, int type ){
		System.out.println("GENERATED (EN/DE)CODING FOR TOKEN TYPE: " + type);
		if(map.size() != 0){
			for(String key:map.keySet()){		
				System.out.println(key + " = " + map.get(key));
			
			}
		}		
	}


	public void encode (CommonToken toEncode){ //Encodes a token and sets it text to the encoded variant

			String encodedString = toEncode.getText(); //CAREFUL can cause problems use decode method
			@SuppressWarnings("unchecked")
			${parserName}Encoding encoding = getEncoding(toEncode.getType());
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
