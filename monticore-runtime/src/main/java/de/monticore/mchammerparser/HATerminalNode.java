package de.monticore.mchammerparser;

import org.antlr.v4.runtime.CommonToken;
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

	@Override
	public boolean deepEquals(HAParseTree parseTree)
	{
		if( !(parseTree instanceof HATerminalNode) )
			return false;
		
		HATerminalNode terminalNode = (HATerminalNode)parseTree;
		Token token = getSymbol();
		Token token2 = terminalNode.getSymbol();
		
		if( token instanceof CommonToken )
		{
			return token.getText().equals(token2.getText());
		}
		else if( token instanceof HABinarySequenceToken )
		{
			return ((HAOffsetToken)token).equals(token2);
		}
		else if( token instanceof HABinarySequenceToken )
		{
			return ((HAOffsetToken)token).equals(token2);
		}
		else
		{
			return false;
		}
	}
}