${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParseTreePackage()};

import java.util.LinkedList;
import com.upstandinghackers.hammer.*;
import de.monticore.mchammerparser.*;

/**
 * Used by the TreeFactory to create an Antlr-ParseTree from a Hammer.ParseResult
 */
public class ${grammarName}TreeHelper 
{
	public enum UserTokenTypes
	{
		UTT_Undefined(Hammer.TokenType.USER.getValue()),
<#assign iter=1>
<#list genHelper.getParserRuleNames() as ruleName>
		UTT_${ruleName}(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
</#list>
<#assign iter2=1>
<#list hammerGenerator.getLexStrings() as lexString>
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
<#list [8,16,32,64] as bits>
		UTT_UInt${bits}(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
</#list>
<#list [8,16,32,64] as bits>
		UTT_Int${bits}(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
</#list>
<#list 1..64 as bits>
		UTT_UBits${bits}(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
</#list>
<#list 1..64 as bits>
		UTT_Bits${bits}(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
</#list>	
<#list genHelper.getOffsetRulesToGenerate() as offsetProd>
		UTT_${offsetProd.getName()}(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
</#list>
		UTT_EOF(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
		UTT_Little(Hammer.TokenType.USER.getValue()+${iter}),
<#assign iter=iter+1>
		UTT_Combine(Hammer.TokenType.USER.getValue()+${iter});

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
	
	public enum TokenType
	{
<#assign iter=1>
<#list hammerGenerator.getLexStrings() as lexString>
		TT_${iter},
<#assign iter=iter+1>
</#list>
<#list genHelper.getLexerRuleNames() as lexRuleName>
		TT_${lexRuleName},
</#list>		
<#list genHelper.getBinaryRuleNames() as binRuleName>
		TT_${binRuleName},
</#list>
<#list [8,16,32,64] as bits>
		TT_UInt${bits},
</#list>
<#list [8,16,32,64] as bits>
		TT_Int${bits},
</#list>
<#list 1..64 as bits>
		TT_UBits${bits},
</#list>
<#list 1..64 as bits>
		TT_Bits${bits},
</#list>
<#list genHelper.getOffsetRulesToGenerate() as offsetProd>
		TT_${offsetProd.getName()},
</#list>
		TT_EOF
	}
	
	public static String [] TokenTypeNames =
	{
<#assign iter=1>
<#list hammerGenerator.getLexStrings() as lexString>
		"TT_${iter}",
<#assign iter=iter+1>
</#list>
<#list genHelper.getLexerRuleNames() as lexRuleName>
		"${lexRuleName}",
</#list>	
<#list genHelper.getBinaryRuleNames() as binRuleName>
		"TT_${binRuleName}",
</#list>
<#list [8,16,32,64] as bits>
		"TT_UInt${bits}",
</#list>
<#list [8,16,32,64] as bits>
		"TT_Int${bits}",
</#list>
<#list 1..64 as bits>
		"TT_UBits${bits}",
</#list>
<#list 1..64 as bits>
		"TT_Bits${bits}",
</#list>
<#list genHelper.getOffsetRulesToGenerate() as offsetProd>
		"TT_${offsetProd.getName()}",
</#list>
		"TT_END"
	};
	
	public static String [] Literals =
	{
<#list hammerGenerator.getLexStrings() as lexString>
		"'${lexString}'",
</#list>
		""
	};
	
	public enum RuleType
	{
<#list genHelper.getParserRuleNames() as parserRuleName>
		RT_${parserRuleName},
</#list>
		RT_Undefined
	}
	
	public static String [] RuleTypeNames =
	{
<#list genHelper.getParserRuleNames() as parserRuleName>
		"${parserRuleName}",
</#list>
		"Undefined"
	};
	
	public static boolean isBinary( int type )
	{
<#assign binaryTypeLowerBound = 1>
<#list hammerGenerator.getLexStrings() as lexString>
	<#assign binaryTypeLowerBound = binaryTypeLowerBound+1>
</#list>
<#list genHelper.getLexerRuleNames() as lexRuleName>
	<#assign binaryTypeLowerBound = binaryTypeLowerBound+1>
</#list>
		return ( type >= ${binaryTypeLowerBound} );
	}
	
	public static boolean isBinaryProd( int type )
	{
<#list genHelper.getBinaryRuleNames() as binRuleName>
	<#assign binaryTypeLowerBound = binaryTypeLowerBound+1>
</#list>
		return ( type < ${binaryTypeLowerBound} ) && isBinary(type);
	}
}