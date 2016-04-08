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
		p.getAst().setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_Undefined.getValue());
		
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

	public static ParsedToken actEOF(ParseResult p)
	{		
		p.getAst().setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_EOF.getValue());
		
		return p.getAst();
	}
	
	public static ParsedToken actUInt8(ParseResult p)
	{		
		p.getAst().setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_UInt8.getValue());
		
		return p.getAst();
	}
	
	public static ParsedToken actUInt16(ParseResult p)
	{		
		p.getAst().setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_UInt16.getValue());
		
		return p.getAst();
	}
	
	public static ParsedToken actUInt32(ParseResult p)
	{		
		p.getAst().setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_UInt32.getValue());
		
		return p.getAst();
	}
	
	public static ParsedToken actUInt64(ParseResult p)
	{		
		p.getAst().setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_UInt64.getValue());
		
		return p.getAst();
	}
	
	public static ParsedToken actInt8(ParseResult p)
	{		
		p.getAst().setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_Int8.getValue());
		
		return p.getAst();
	}
	
	public static ParsedToken actInt16(ParseResult p)
	{		
		p.getAst().setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_Int16.getValue());
		
		return p.getAst();
	}
	
	public static ParsedToken actInt32(ParseResult p)
	{		
		p.getAst().setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_Int32.getValue());
		
		return p.getAst();
	}
	
	public static ParsedToken actInt64(ParseResult p)
	{		
		p.getAst().setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_Int64.getValue());
		
		return p.getAst();
	}
}