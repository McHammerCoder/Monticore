package de.monticore.mchammerparser;

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