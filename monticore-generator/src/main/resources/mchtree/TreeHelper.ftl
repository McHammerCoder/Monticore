${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign treeHelperName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

import java.util.LinkedList;

/**
 * Used by the TreeFactory to create an Antlr-ParseTree from a Hammer.ParseResult
 */
public class ${treeHelperName}TreeHelper 
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
	
	public enum Type
	{
		C_Token,
		C_Rule
	};
	
	public enum TokenType
	{
		// TODO
					
	}
	
	public static String [] TokenTypeNames =
	{
		// TODO
	};
	
	public enum RuleType
	{
		// TODO
	}
	
	public static String [] RuleTypeNames =
	{
		// TODO
	};
}