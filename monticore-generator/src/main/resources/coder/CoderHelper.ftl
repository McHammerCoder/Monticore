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
import java.util.Set;
import java.util.HashSet;

public class ${parserName}CoderHelper{


	private ArrayList<${parserName}Range> ranges = new ArrayList<${parserName}Range>();
	private ArrayList<String> kws = new ArrayList<String>();

	public ${parserName}CoderHelper(){
		initiateKWAndUS();
	}

	public String[] getKeywords(){
		return kws.toArray(new String[kws.size()]);

	}

	public String[] getFreeSymbols(){
		return ${parserName}Range.union(ranges);
	}
	
	public void initiateKWAndUS(){
	
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


























}
