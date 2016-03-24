${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

import com.upstandinghackers.hammer.ParseResult;
import com.upstandinghackers.hammer.ParsedToken;

import de.mchammer.tree.*;

/**
 * Class that contains all actions the parser might call while parsing
 */
public class ${grammarName}Actions 
{
	public static ParsedToken actUndefined(ParseResult p)
	{		
		TreeHelper.push( new TreeHelper.RuleContext(TreeHelper.RuleType.RT_Undefined) );
		
		return p.getAst();
	}

<#list genHelper.getParserRuleNames() as ruleName>
	public static ParsedToken act${ruleName}(ParseResult p)
	{		
		TreeHelper.push( new TreeHelper.RuleContext(TreeHelper.RuleType.RT_${ruleName}) );
		
		return p.getAst();
	}
</#list>	

}