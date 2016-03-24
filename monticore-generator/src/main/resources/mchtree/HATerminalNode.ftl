${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign actionsName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public class HATerminalNode extends HAParseTree implements TerminalNode
{
	
	/**
	 * Constructor for de.mchammer.tree.HATerminalNode.
	 * @param token
	 * @param tokenType
	 */
	public HATerminalNode(Token symbol) 
	{
		super(symbol);
	}

	/**
	 * @see org.antlr.v4.runtime.tree.TerminalNode#getSymbol()
	 */
	public Token getSymbol() 
	{
		return (Token) payload;
	}

}