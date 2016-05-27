${tc.signature("coderGenerator","outputFolder")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

package ${genHelper.getCoderPackage()};

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ${parserName}EncodingGenerator {
  private static ${parserName}CoderHelper helper = new ${parserName}CoderHelper();
  
	public static String[] getSiso(){
		List<String> allChars = new ArrayList<String>();
		for(char i = 0; i < Character.MAX_VALUE; i++){
			allChars.add(""+((char) i));
		}
		
		List<String> fs = Arrays.asList(helper.getFreeSymbols());
		for(String s: fs){
			if(allChars.contains(s)){
				allChars.remove(s); 
			}
		}
		return allChars.toArray(new String[0]);
	}


	public static void main(String[] args) {
		String siso = "";
		if( args.length != 0){
         siso = args[0];
         }	
            
            if(siso.equals("siso")){
            System.out.println("[INFO] LET THE MADNESS BEGIN");
            Set<String> set = new HashSet<>();
            set.addAll(Arrays.asList(helper.getKeywords()));
            set.addAll(Arrays.asList(getSiso()));	
            String[] keywords = set.toArray(new String[0]);
         	 ${parserName}CoderCoder coder = new ${parserName}CoderCoder(helper.getTypes(), keywords , helper.getFreeSymbols());
         	 
         	 ${parserName}Encodings en = new  ${parserName}Encodings();
         	 en.setHasEncodingArray(coder.hasEncodingArray);
         	 en.setAllEncodings(coder.allEncodings);
         	 try
		      {
		         FileOutputStream fileOut =
		         new FileOutputStream("${outputFolder}/${parserName}Encodings.ser");
		         ObjectOutputStream out = new ObjectOutputStream(fileOut);
		         out.writeObject(en);
		         out.close();
		         fileOut.close();
		         System.out.printf("Serialized data is saved");
		      }catch(IOException i)
		      {
		          i.printStackTrace();
		      }
            }
            else{
            ${parserName}CoderCoder coder = new ${parserName}CoderCoder(helper.getTypes(),helper.getKeywords(), helper.getFreeSymbols());
            ${parserName}Encodings en = new  ${parserName}Encodings();
         	 en.setHasEncodingArray(coder.hasEncodingArray);
         	 en.setAllEncodings(coder.allEncodings);
         	 try
		      {
		         FileOutputStream fileOut =
		         new FileOutputStream("${outputFolder}/${parserName}Encodings.ser");
		         ObjectOutputStream out = new ObjectOutputStream(fileOut);
		         out.writeObject(en);
		         out.close();
		         fileOut.close();
		         System.out.println("[INFO] Serialized data is saved");
		      }catch(IOException i)
		      {
		          i.printStackTrace();
		      }
            }
    }

}