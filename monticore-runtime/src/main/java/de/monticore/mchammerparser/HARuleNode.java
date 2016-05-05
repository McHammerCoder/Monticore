package de.monticore.mchammerparser;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.RuleNode;

public class HARuleNode extends HAParseTree implements RuleNode
{	
	
	/**
	 * Constructor for de.mchammer.rulecontexts.RNMessage.
	 * @param payload
	 */
	public HARuleNode(Object payload)
	{
		super(payload);
	}

	public int getIndex()
	{
		return 0;
	}

	public RuleContext getRuleContext()
	{
		return (RuleContext) payload;
	}
}