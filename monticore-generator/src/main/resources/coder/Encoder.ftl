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

	private ArrayList<${parserName}Range> ranges = new ArrayList<${parserName}Range>();
	private Map<String, String> encodingMap = new HashMap<String, String>(); 
	public String startEncoding = "";
	private int currentType;

	private int getCurrentType(){
		return currentType;
	}
	
	private void setCurrentType(int type){
		this.currentType = type;
	}
	
	public String[] initiateUsableSymbols(){
	
	<#list genHelper.getLexerRulesToGenerate() as lexrule>
		<#list coderGenerator.createUsableSymbolsCode(lexrule) as asd>
			${asd}
		</#list>
	</#list>
	return ${parserName}Range.union(ranges);	

	}


	public ${parserName}AntlrLexer lex(String string){
		ANTLRInputStream input = new ANTLRInputStream(string);
		${parserName}AntlrLexer lexer = new ${parserName}AntlrLexer(input);
		return lexer;

	}
	
	public String[] getKeywords(){
	 	Vocabulary voc = lex("").getVocabulary();
		String[] keywords; // final keywords list
		keywords = new String[lex("").getTokenNames().length]; //This is market as deprecated but no explanation or alternative has been provided
		String tmp; // temp string for filtering
		for(int i = 0; i < keywords.length ; i++){
		 tmp = voc.getLiteralName(i);
		 if (tmp == "'''"){ //Readd '
		 	keywords[i] = "'";
		 }
		 else if(tmp != null){
		 keywords[i] = tmp.replace("'", "");} //Removes ' ' surronding the keywords
		}

		keywords = Arrays.stream(keywords).filter(s -> (s != null && s.length() > 0)).toArray(String[]::new); //Filtering NULL out
		return keywords;
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

		if((originaltext.equals(nextToken.getText()) && receivedtoken.getType() == nextToken.getType())){
			return true;
			}
	
		return false;
	}

	public boolean notKeyword(String toCheck){ //This returns if a string matches a keyword.
		String[] allKW = getKeywords();
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
		//String[] alphanumeric = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
		String[] alphanumeric = initiateUsableSymbols();
		String[] keyword = getKeywords();
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


	private void createEncoding(String[] kw, String[] usableSymbols, int type) throws Exception{ //Should create a Map with different encodings
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
		
		setCurrentType(type);
		//i == first
		//z == second
		for(int i=0; i< usableSymbols.length; i++){

		String encoding = usableSymbols[i];
			for(int z=i+1; z < usableSymbols.length && z != i; z=(z+1)%usableSymbols.length){ //Second
				

				for(int j=0; j <= (kw.length); j++){


					encoding += convertToString(j, usableSymbols[i], usableSymbols[z], (int) Math.ceil((Math.log10(kw.length+1)/Math.log10(2)))); //[log_2(kw.length+1)]

					if(j != kw.length && !notKeyword(encoding) && typeCheck(type,encoding)){
					encodingMap.put(kw[j], encoding);//Save encoding and kw[j] to map
					System.out.println(kw[j] + " = " + encoding);
					}
					else if(notKeyword(encoding) || !typeCheck(type,encoding)){ //Our created encoding contains a keyword reset and try again
						encodingMap.clear();
						break;
					}
					else if(j == kw.length){
						System.out.println(usableSymbols[i] + " = " + encoding);
				
						encodingMap.put(usableSymbols[i], encoding); //Last save the encoding for the start
						startEncoding = encoding;
						System.out.println("THE GENERATED ENCODING WAS FOR TYPE: " + type);
						return ;
					}
					encoding = usableSymbols[i]; //Reset first symbol for encoding
				}
			}
		}

		throw new Exception("No viable encoding can be generated for token type: " +type);
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

	public Map<String, String> getEncoding(int type) throws Exception{ //Returns the map if none exists one is created
		if(encodingMap.size() != 0 && type == getCurrentType()) {
 			return encodingMap;
		}
		else {
			createEncoding(getKeywords(), getUsableSymbols(), type);
			return encodingMap;
		}
	}

	public boolean canBeEncoded(String toTest){ //Checks if a string can be encoded
		String[] kw = getKeywords();
		for(int i=0; i< kw.length; i++) {
			if(toTest.contains(kw[i])) {
				return true;
			}	
		}
		return false;
	}

	public void encode (CommonToken toEncode){ //Encodes a token and sets it text to the encoded variant
		try{
			String encodedString = toEncode.getText(); //CAREFUL can cause problems use decode method
			//insteadead of contains use a window
			@SuppressWarnings("unchecked")
		
			Map<String, String> map = (Map<String, String>) getEncoding(toEncode.getType());

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
		}catch(Exception e){
			System.out.println(e.getMessage());
			//System.exit(1);		
		}
	}

}
