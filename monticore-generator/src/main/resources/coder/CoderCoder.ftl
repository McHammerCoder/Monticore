${tc.signature("coderGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

package ${genHelper.getParserPackage()};

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.*;

import ${genHelper.getGNameToLower()}._parser.*;
import de.monticore.codegen.mccoder.*;

/**
 * TODO: Write me!
 *
 * @author  (last commit) $Author$
 * @version $Revision$, $Date$
 * @since   TODO: add version number
 *
 */
public class ${parserName}CoderCoder {
	private ArrayList<Encoding> allEncodings = new ArrayList<Encoding>();
	private String[] kws;
	private int types;
	private String[] freeS;
	private Boolean[] hasEncodingArray; //Should be sum of types+1
	private ArrayList<String> codeSection = new ArrayList<String>();
	
	public ${parserName}CoderCoder(int type, String[] kw, String[] fs){
		this.types = type;
		this.kws = kw;
		this.freeS = fs;
		hasEncodingArray = new Boolean[(types+1)];
		Arrays.fill(hasEncodingArray, false);
		fillAllEncodings();		
	}
	public String getHasEncoding(){
		String res = "{";
		for(int i=0; i < hasEncodingArray.length; i++){
			res += hasEncodingArray[i] + ", ";
		}
		res += hasEncodingArray[types] + "}";
		return res;
	}
	public ArrayList<String> getCodeSection() 
	{
		return this.codeSection;
	}
	
	public int getTypes(){
		return types;
	}
	
	private void addToCodeSection(String code) 
	{
		codeSection.add(code);
	}
	
	
	public boolean isKeyword(String toCheck){ //This returns true if a string matches a keyword.
		String[] allKW = kws;		
		for(int i=0; i<allKW.length; i++){
			if(toCheck.contains(kws[i])){
				//System.out.println(toCheck + " " + "CASE HAS A KEYWORD" + " " + kws[i]);
				return true;
			}
		}
		for(int i=0; i<toCheck.length(); i++){
			for(String kw:allKW){
				if(kw.startsWith(toCheck.substring(i, toCheck.length()))){
					//System.out.println(toCheck + " " + "CASE HAS A STARTKW" + " " + kw);
					return true;
				}
				if(kw.endsWith(toCheck.substring(0, i+1))){
					//System.out.println(toCheck + " " + "CASE HAS AN ENDKW "+ kw);
					return true;
				}
			}
		}
		
		
		return false;

	}


	public boolean typeCheck(int type, String string){
		Lexer lexer = lex(string);
		Token nextToken =lexer.nextToken();

		if(type == nextToken.getType() && lexer.nextToken().getType() == Token.EOF){
			return true;
			}
			return false;
	}


	public String[] getUsableSymbols(){
		//Each usable symbol should be lexable. No usable symbol should be a keyword.
		String[] alphanumeric = freeS;
		String[] keyword = kws;
		String[] usableSymbols = new String[alphanumeric.length];
		for(int i=0; i< alphanumeric.length; i++){
			for(int j=0; j<keyword.length; j++){
				if(alphanumeric[i].equals(keyword[j])){ 
				break;
			}
		}

			Lexer lexer = lex(alphanumeric[i]);
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
					//addToCodeSection("map.put("  + "\"" + kw[j]  + "\"" + ", " + "\"" + encoding + "\"" + ");");
					//System.out.println(kw[j] + " = " + encoding);
					}
					else if(isKeyword(encoding) || !typeCheck(type,encoding)){ //Our created encoding contains a keyword reset and try again
						encodingMap.clear();
						encoding = usableSymbols[i];
						//addToCodeSection("map.clear();");
						break;
					}
					else if(j == kw.length){
						//System.out.println(usableSymbols[i] + " = " + encoding);
				
						encodingMap.put(usableSymbols[i], encoding); //Last save the encoding for the start
						//CODE GENERATION
						addToCodeSection("Map<String, String> map" + type + " = new HashMap<String, String>();");
						addEncodingMapToCodeSection(encodingMap, type);
						addToCodeSection("map" + type + ".put("  + "\"" + usableSymbols[i]  + "\"" + ", " + "\"" + encoding + "\"" + ");");
						//END CODE GENERATION
						startEncoding = encoding;
						//System.out.println("THE GENERATED ENCODING WAS FOR TYPE: " + type);
						addToCodeSection("allEncodings.add(new Encoding(" + type + ", " + "map" + type + ", " + "\""+ startEncoding + "\"" +"));\n");
						allEncodings.add(new Encoding(type, encodingMap , startEncoding));
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
		
		${parserName}CoderHelper helper = new ${parserName}CoderHelper();
		ArrayList<Encoding> customEncodings = helper.getCustomEncodings();
		String[] usableSymb = getUsableSymbols();
		String[] kw = kws;
		if(kw.length == 0){
			System.out.println("NO KEYWORDS FOUND - NO ENCODING WILL BE GENERATED");
			return ;
		}
		//System.out.println(kws.length + "  " + types + "INFO");
		if(!customEncodings.isEmpty()){
			for(int j = helper.getKeywords().length+1 ; j<=types; j++){
				for(Encoding e : customEncodings){
					if(e.getType() == j){
						System.out.println("FOUND CUSTOM ENCODING FOR TYPE: " + j);
						hasEncodingArray[j] = true;
						Map<String, String> map = e.getMap();
						int numOfKws = 0;
						for(String s : map.keySet()){					
						   for(int i = 0; i< kw.length; i++){
						     if(kw[i].equals(s)){
						     	numOfKws++;
						     }
						   }
						}
						if(numOfKws != kw.length){
							System.err.println("NOT ALL KEYWORDS HAVE BEEN ENCODED");
						}
					}
				}
				if(hasEncodingArray[j] != true){
				 	hasEncodingArray[j] = createEncoding(kw, usableSymb, (j));
				 	}
					if(!hasEncodingArray[j]){
						System.out.println("NO ENCODING FOUND FOR TYPE: " + j);
					}
				}
			}

		else{
			for(int j = ((helper.getKeywords().length)+1) ; j<=types; j++){
				hasEncodingArray[j] = createEncoding(kw, usableSymb, (j));
				if(!hasEncodingArray[j]){
					System.out.println("NO ENCODING FOUND FOR TYPE: " + j);
				}
			}
		}
	}
	public void addEncodingMapToCodeSection (Map<String, String> map, int type){
			for(String key: map.keySet()){
				addToCodeSection("map" + type + ".put("  + "\"" + key  + "\"" + ", " + "\"" + map.get(key) + "\"" + ");");
			}
	}
	public void printEncoding(Map <String,String> map, int type ){
		System.out.println("GENERATED (EN/DE)CODING FOR TOKEN TYPE: " + type);
		if(map.size() != 0){
			for(String key:map.keySet()){		
				System.out.println(key + " = " + map.get(key));
			
			}
		}		
	}
	
	private ${parserName}AntlrLexer lex(String in)
	{
		ANTLRInputStream input = new ANTLRInputStream(in);
		${parserName}AntlrLexer lexer = new ${parserName}AntlrLexer(input);
		return lexer;
	}
}
