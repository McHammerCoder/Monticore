/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mccoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

/**
 * TODO: Write me!
 *
 * @author  (last commit) $Author$
 * @version $Revision$, $Date$
 * @since   TODO: add version number
 *
 */
public class McCoderCoder {
	private ArrayList<Encoding> allEncodings = new ArrayList<Encoding>();
	private String[] kws;
	private int types;
	private String[] freeS;
	private Boolean[] hasEncodingArray; //Should be sum of types+1
	private ArrayList<String> codeSection = new ArrayList<String>();
	
	public McCoderCoder(int type, String[] kw, String[] fs){
		this.types = type;
		this.kws = kw;
		this.freeS = fs;
		hasEncodingArray = new Boolean[(types+1)];
		//System.out.println(Arrays.toString(kw));
		//System.out.println(Arrays.toString(fs));
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
	
	
	public boolean isKeyword(String toCheck){ //This returns if a string matches a keyword.
		String[] allKW = kws;
		String res="";
		for(int i=0; i<allKW.length; i++){
			res+= allKW[i];
			}
		return res.contains(toCheck);

	}


	public boolean typeCheck(int type, String string){
		Lexer lexer = McCoderGenerator.lex(string);
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

			Lexer lexer = McCoderGenerator.lex(alphanumeric[i]);
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
					addToCodeSection("map.put("  + "\"" + kw[j]  + "\"" + ", " + "\"" + encoding + "\"" + ");");
					//System.out.println(kw[j] + " = " + encoding);
					}
					else if(isKeyword(encoding) || !typeCheck(type,encoding)){ //Our created encoding contains a keyword reset and try again
						encodingMap.clear();
						//addToCodeSection("map.clear();");
						break;
					}
					else if(j == kw.length){
						//System.out.println(usableSymbols[i] + " = " + encoding);
				
						encodingMap.put(usableSymbols[i], encoding); //Last save the encoding for the start
						addToCodeSection("map.put("  + "\"" + usableSymbols[i]  + "\"" + ", " + "\"" + encoding + "\"" + ");");
						startEncoding = encoding;
						//System.out.println("THE GENERATED ENCODING WAS FOR TYPE: " + type);
						addToCodeSection("allEncodings.add(new Encoding(" + type + ", " + "map" + ", " + "\""+startEncoding + "\"" +"));\n");
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
		
		String[] usableSymb = getUsableSymbols();
		String[] kw = kws;
		System.out.println(kws.length + "  " + types + "INFO");
		for(int j = 0 ; j<=types; j++){
			for(;j <= kw.length; j++){
			  hasEncodingArray[j] = false;
			}
			hasEncodingArray[j] = createEncoding(kw, usableSymb, (j));
			if(!hasEncodingArray[j]){
				System.out.println("NO ENCODING FOUND FOR TYPE: " + j);
			}
			else if (hasEncodingArray[j]) {
				addToCodeSection("map.clear();");
			}
		
		}
		/*for(Encoding encodingMap : allEncodings){
			printEncoding(encodingMap.getMap(), encodingMap.getType());
		}*/

	}

	public void printEncoding(Map <String,String> map, int type ){
		System.out.println("GENERATED (EN/DE)CODING FOR TOKEN TYPE: " + type);
		if(map.size() != 0){
			for(String key:map.keySet()){		
				System.out.println(key + " = " + map.get(key));
			
			}
		}		
	}
}
