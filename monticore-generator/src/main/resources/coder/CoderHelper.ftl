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
import de.monticore.codegen.mccoder.*;

/*
Contains everything extracted from the MC4 Grammar AST
-Keywords
-UsableSymbols
Has basic conversion functions to create the full list of keywords
*/
public class ${parserName}CoderHelper{

	private ArrayList<Range> ranges = new ArrayList<Range>();
	private Set<String> kws = new HashSet<String>();
	private int types = ${genHelper.getTokenTypes()};
	private ArrayList<Encoding> customEncodings = new ArrayList<Encoding>();

	public ${parserName}CoderHelper(){
		initiateKWAndUS();
		initiateCustomEncodings();
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

	private void initiateCustomEncodings(){
	<#list genHelper.getEncodingTablesToGenerate() as encoding>
		<#list coderGenerator.createCustomEncodingCode(encoding) as custom>
			${custom}
		</#list>
		</#list>
	}
	
		
	public String[] getKeywords(){
	
		HashSet<String> list = new HashSet<String>();
		for(String todo : kws){
			for(int i = 0; i < todo.length()-1; i++){
				
				list.add(todo.substring(0, i+1));
			}
			for(int i = 0; i< todo.length(); i++){
				list.add(todo.substring(i, todo.length()));
			}
		}
		return list.toArray(new String[kws.size()]);

	}

	public int getKeywordsLength(){
		return kws.size();

	}

	public String[] getFreeSymbols(){
		
		return Range.union(ranges);
		
	}
	
	public ArrayList<Encoding> getCustomEncodings(){
		return customEncodings;
	}
	
	public int getTypes()
	{
		return types;
	}
}
