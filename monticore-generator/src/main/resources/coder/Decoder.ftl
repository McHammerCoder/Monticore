${tc.signature("genHelper")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParserPackage()};

import org.antlr.v4.runtime.*;
import java.util.Map;

/*
Visit functions need to be generated - move them to a seperate part e.g. DecodeVisitor and EncodeVisitor
*/

public class ${parserName}Decoder{

	private ${parserName}Encoder encoder = new ${parserName}Encoder();

	public void decode(CommonToken toDecode){ //Decodes a token and sets it text to the decoded variant
		try{
			String[] kw = encoder.getKeywords();
			for(int i=0; i<kw.length; i++)
				if( toDecode.getType() == encoder.lex(kw[i]).nextToken().getType())
				{
					return ;
				}
			String decodedString = toDecode.getText();
			int elength = encoder.startEncoding.length();
			@SuppressWarnings("unchecked")
			Map<String, String> map = (Map<String, String>) encoder.getEncoding(toDecode.getType());
			
			if(! (decodedString.length() < elength) )
			{
				
				for(int i = 0; i <= decodedString.length()-elength; i++)
				{
					
					for(String key: map.keySet())
					{
			
						if( encoder.startEncoding.equals(map.get(key)) )
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
		}catch(Exception e){}
	}

}

