${tc.signature("genHelper")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParserPackage()};

import org.antlr.v4.runtime.*;
import java.util.Map;
import de.monticore.codegen.mccoder.*;
/*
Visit functions need to be generated - move them to a seperate part e.g. DecodeVisitor and EncodeVisitor
*/

public class ${parserName}Decoder{

	private ${parserName}CoderHelper helper = new ${parserName}CoderHelper();
	private ${parserName}Encodings encodings = new ${parserName}Encodings();
	
	public void decode(CommonToken toDecode){ //Decodes a token and sets it text to the decoded variant
			String[] kw = helper.getKeywords();
			String decodedString = toDecode.getText();

			if(!encodings.hasEncoding(toDecode.getType()))
			{
			 return;
			}
			@SuppressWarnings("unchecked")
			Encoding encoding = encodings.getEncoding(toDecode.getType());
			Map<String, String> map = (Map<String, String>) encoding.getMap();
			String startEncoding = encoding.getStartEncoding();
			int elength = startEncoding.length();
			
			//encoder.printEncoding(map, toDecode.getType());
			if(! (decodedString.length() < elength) )
			{
				
				for(int i = 0; i <= decodedString.length()-elength; i++)
				{
					
					for(String key: map.keySet())
					{
			
						if( startEncoding.equals(map.get(key)) )
						{
							if( decodedString.substring(i).startsWith(map.get(key)) )
							{
								
								decodedString =	decodedString.substring(0, i) + key + decodedString.substring(i+map.get(key).length());
								break;
							}
						}
						else
						{
							if( decodedString.substring(i).startsWith(map.get(key)) )
							{
								decodedString =	decodedString.substring(0, i) + key + decodedString.substring(i+map.get(key).length()); //.replaceFirst(map.get(key), key);
							}
						}
					}
				}
			}

			toDecode.setText(decodedString);
	}

}

