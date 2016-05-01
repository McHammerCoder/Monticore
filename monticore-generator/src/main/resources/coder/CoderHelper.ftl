${tc.signature("coderGenerator", "coder")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

package ${genHelper.getParserPackage()};

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import de.monticore.codegen.mccoder.*;

public class ${parserName}CoderHelper{

	private ArrayList<Range> ranges = new ArrayList<Range>();
	private Set<String> kws = new HashSet<String>();
	private ArrayList<Encoding> allEncodings = new ArrayList<Encoding>();
	private Boolean[] hasEncodingArray = ${coder.getHasEncoding()};
	private Map<String, String> map = new HashMap<String, String>();


	public ${parserName}CoderHelper(){
		initiateKWAndUS();
		initiateEncoding();
	}

	
	
	private void initiateKWAndUS(){
		<#list genHelper.getLexerRulesToGenerate() as lexrule>
			<#list coderGenerator.createUsableSymbolsCode(lexrule) as ranges>
				${ranges}
			</#list>
		</#list>
		<#list genHelper.getParserRulesToGenerate() as parserRule>
		<#list coderGenerator.createUsableSymbolsCode(parserRule) as parserRuleCode>
			${parserRuleCode}
		</#list>
		</#list>
	}

	private void initiateEncoding(){
	<#list coder.getCodeSection() as code>
		${code}

	</#list>
	
	}


	public Encoding getEncoding(int type){
		for(Encoding encodingMap : allEncodings){
			if(encodingMap.getMap().size() != 0 && type == encodingMap.getType()) {
				return encodingMap;
			}	
		}
		System.out.println("NO SUCH MAP WAS FOUND: " + type + "\n Something went wrong terminating.");
		System.exit(4);
		return null;
	}

	public String[] getKeywords(){
		return kws.toArray(new String[kws.size()]);

	}

	public String[] getFreeSymbols(){
		return Range.union(ranges);
	}
	
	public boolean hasEncoding(int type){
		
		return hasEncodingArray[type];

	}

	public ArrayList<Encoding> getAllEncodings(){

		return allEncodings;
	}



}
