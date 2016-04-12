${tc.signature("genHelper")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParserPackage()};
import java.util.Map;
public class ${parserName}Encoding{

		private int type;
		private Map<String,String> map;
		private String startEncoding;
			

		public ${parserName}Encoding(int type, Map<String,String> map, String startEncoding){
			this.map = map;
			this.type = type;
			this.startEncoding = startEncoding;
			}

		public int getType(){
			return type;
		}
		
		public Map<String,String> getMap(){
			return map;
		}
		public String getStartEncoding(){
			return startEncoding;
		}

	}
