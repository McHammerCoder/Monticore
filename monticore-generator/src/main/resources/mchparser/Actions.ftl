${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

import com.upstandinghackers.hammer.ParseResult;
import com.upstandinghackers.hammer.ParsedToken;

/**
 * Class that contains all actions the parser might call while parsing
 */
public class ${grammarName}Actions 
{
	public static ParsedToken actUndefined(ParseResult p)
	{		
		${grammarName}TreeHelper.push( new ${grammarName}TreeHelper.RuleContext(${grammarName}TreeHelper.RuleType.RT_Undefined) );
		
		return p.getAst();
	}

<#list genHelper.getParserRuleNames() as ruleName>
	public static ParsedToken act${ruleName}(ParseResult p)
	{		
		${grammarName}TreeHelper.push( new ${grammarName}TreeHelper.RuleContext(${grammarName}TreeHelper.RuleType.RT_${ruleName}) );
		
		return p.getAst();
	}
</#list>	

	public static ParsedToken actString(ParseResult p)
	{		
		${grammarName}TreeHelper.push( new ${grammarName}TreeHelper.TokenContext(${grammarName}TreeHelper.TokenType.TT_String) );
		
		return p.getAst();
	}

}