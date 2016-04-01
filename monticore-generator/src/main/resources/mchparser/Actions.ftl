${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

package ${genHelper.getParserPackage()};

import ${genHelper.getParseTreePackage()}.*;

import com.upstandinghackers.hammer.ParseResult;
import com.upstandinghackers.hammer.ParsedToken;

/**
 * Class that contains all actions the parser might call while parsing
 */
public class ${grammarName}Actions 
{
	public static ParsedToken actUndefined(ParseResult p)
	{		
		p.getAst().setUserTokenType(AutomatonTreeHelper.UserTokenTypes.UTT_Undefined.getValue());
		
		return p.getAst();
	}

<#list genHelper.getParserRuleNames() as ruleName>
	public static ParsedToken act${ruleName}(ParseResult p)
	{		
		p.getAst().setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_${ruleName}.getValue());
		
		return p.getAst();
	}
</#list>	
<#assign iter=1>
<#list genHelper.getLexStrings() as lexString>
	public static ParsedToken actTT_${iter}(ParseResult p)
	{		
		p.getAst().setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_${iter}.getValue());
		
		return p.getAst();
	}
<#assign iter=iter+1>
</#list>
<#list genHelper.getLexerRuleNames() as lexRuleName>
	public static ParsedToken act${lexRuleName}(ParseResult p)
	{		
		p.getAst().setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_${lexRuleName}.getValue());
		
		return p.getAst();
	}
</#list>
	

}