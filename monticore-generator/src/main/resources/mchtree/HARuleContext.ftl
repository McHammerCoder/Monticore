${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

package ${genHelper.getParseTreePackage()};

import org.antlr.v4.runtime.RuleContext;

public class HARuleContext extends RuleContext
{
	private int ruleIndex = 0;
	
	public HARuleContext(int ruleIndex)
	{
		super();
		this.ruleIndex = ruleIndex;
	}
	
	public int getRuleIndex()
	{
		return ruleIndex;
	}
}