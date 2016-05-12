package de.monticore.mchammerparser;

import java.util.List;

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
	
	public HARuleNode(Object payload, List<HAParseTree> childs)
	{
		super(payload,childs);
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