${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

package ${genHelper.getParseTreePackage()};

import java.util.LinkedList;
import com.upstandinghackers.hammer.*;

/**
 * Used by the TreeFactory to create an Antlr-ParseTree from a Hammer.ParseResult
 */
public class ${grammarName}TreeHelper 
{
	private static LinkedList<Context> postfixTree = new LinkedList<Context>();
	
	public static void push(Context context)
	{
		postfixTree.push(context);
	}
	
	public static Context pop()
	{
		return postfixTree.pop();
	}
	
	public static Context get()
	{
		return postfixTree.getLast();
	}
	
	public static int size()
	{
		return postfixTree.size();
	}
	
	public static interface Context
	{
		
	}
	
	public static class RuleContext implements Context
	{
		private RuleType type;
		/**
		 * @return the type
		 */
		public RuleType getType() 
		{
			return type;
		}
		
		public RuleContext(RuleType type)
		{
			this.type = type;
		}
	}
	
	public static class TokenContext implements Context
	{
		private TokenType type;
		/**
		 * @return the type
		 */
		public TokenType getType() 
		{
			return type;
		}
		
		public TokenContext(TokenType type)
		{
			this.type = type;
		}
	}
	
	public enum UserTokenTypes
	{
		UTT_Undefined(Hammer.TokenType.USER.getValue()),
<#assign iter=1>
<#list genHelper.getParserRuleNames() as ruleName>
		UTT_${ruleName}(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
</#list>
<#assign iter2=1>
<#list genHelper.getLexStrings() as lexString>
		UTT_${iter2}(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter2=iter2+1>
<#assign iter=iter+1>
</#list>
<#list genHelper.getLexerRuleNames() as lexRuleName>
		UTT_${lexRuleName}(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
</#list>	
<#list genHelper.getBinaryRuleNames() as binRuleName>
		UTT_${binRuleName}(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
</#list>	
		UTT_EOF(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>	
		UTT_UInt8(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
		UTT_UInt16(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
		UTT_UInt32(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
		UTT_UInt64(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
		UTT_Int8(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
		UTT_Int16(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
		UTT_Int32(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
		UTT_Int64(Hammer.TokenType.USER.getValue()+${iter});

		UserTokenTypes(int numValue)
		{
			this.numValue = numValue;
		}
		
		private int numValue;
		
		public int getValue()
		{
			return numValue;
		}
		
	};
	
	public enum Type
	{
		C_Token,
		C_Rule
	};
	
	public enum TokenType
	{
<#assign iter=1>
<#list genHelper.getLexStrings() as lexString>
		TT_${iter},
<#assign iter=iter+1>
</#list>
<#list genHelper.getLexerRuleNames() as lexRuleName>
		TT_${lexRuleName},
</#list>		
		TT_EOF,
		TT_UInt8,
		TT_UInt16,
		TT_UInt32,
		TT_UInt64,
		TT_Int8,
		TT_Int16,
		TT_Int32,
		TT_Int64
	}
	
	public static String [] TokenTypeNames =
	{
<#assign iter=1>
<#list genHelper.getLexStrings() as lexString>
		"TT_${iter}",
<#assign iter=iter+1>
</#list>
<#list genHelper.getLexerRuleNames() as lexRuleName>
		"${lexRuleName}",
</#list>	
		"TT_END"
	};
	
	public static String [] Literals =
	{
<#list genHelper.getLexStrings() as lexString>
		"'${lexString}'",
</#list>
		""
	};
	
	public enum RuleType
	{
<#list genHelper.getParserRuleNames() as parserRuleName>
		RT_${parserRuleName},
</#list>	
<#list genHelper.getBinaryRuleNames() as binRuleName>
		RT_${binRuleName},
</#list>
		RT_Undefined
	}
	
	public static String [] RuleTypeNames =
	{
<#list genHelper.getParserRuleNames() as parserRuleName>
		"${parserRuleName}",
</#list>
<#list genHelper.getBinaryRuleNames() as binRuleName>
		"${binRuleName}",
</#list>
		"Undefined"
	};
}