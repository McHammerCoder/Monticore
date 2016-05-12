${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleName()>

package com.upstandinghackers.hammer;

public class ${grammarName}Hammer extends Hammer
{
	public static native Parser action(Parser p, String a);
}