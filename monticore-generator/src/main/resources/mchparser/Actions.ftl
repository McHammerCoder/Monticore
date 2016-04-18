${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParserPackage()};

import ${genHelper.getParseTreePackage()}.*;

import com.upstandinghackers.hammer.ParseResult;
import com.upstandinghackers.hammer.ParsedToken;
import com.upstandinghackers.hammer.Hammer;

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
	
	public static ParsedToken actUInt(ParseResult p)
	{		
		p.getAst().setUserTokenType(Hammer.TokenType.UINT.getValue());
		
		return p.getAst();
	}

<#list genHelper.getClassRulesToGenerate() as rule>
	public static ParsedToken act${rule.getName()}(ParseResult p)
	{		
		ParsedToken ast = p.getAst();
		ast.setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_${rule.getName()}.getValue());
			
<#list hammerGenerator.getRuleAction(rule) as ruleAction>
		${ruleAction}
</#list>
			
		return ast;
	}
</#list>	
<#list genHelper.getEnumRulesToGenerate() as rule>
	public static ParsedToken act${rule.getName()}(ParseResult p)
	{		
		ParsedToken ast = p.getAst();
		ast.setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_${rule.getName()}.getValue());

		return ast;
	}
</#list>	
<#assign iter=1>
<#list genHelper.getLexStrings() as lexString>
	public static ParsedToken actTT_${iter}(ParseResult p)
	{		
		ParsedToken ast = p.getAst();
		ast.setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_${iter}.getValue());
		
		return ast;
	}
<#assign iter=iter+1>
</#list>
<#list genHelper.getLexerRulesToGenerate() as lexRule>
	public static ParsedToken act${lexRule.getName()}(ParseResult p)
	{		
		ParsedToken ast = p.getAst();
		ast.setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_${lexRule.getName()}.getValue());
		
<#list hammerGenerator.getLexAction(lexRule) as lexAction>
		${lexAction}
</#list>
		
		return ast;
	}
</#list>
<#list genHelper.getBinaryRulesToGenerate() as binaryRule>
	public static ParsedToken act${binaryRule.getName()}(ParseResult p)
	{		
		ParsedToken ast = p.getAst();
		ast.setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_${binaryRule.getName()}.getValue());
		
<#list hammerGenerator.getBinaryAction(binaryRule) as binaryAction>
		${binaryAction}
</#list>
	
		return ast;
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

<#list 1..64 as bits>
	public static ParsedToken actUBits${bits}(ParseResult p)
	{		
		p.getAst().setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_UBits${bits}.getValue());
		
		return p.getAst();
	}
</#list>

<#list 1..64 as bits>
	public static ParsedToken actBits${bits}(ParseResult p)
	{		
		p.getAst().setUserTokenType(${grammarName}TreeHelper.UserTokenTypes.UTT_Bits${bits}.getValue());
		
		return p.getAst();
	}
</#list>
}