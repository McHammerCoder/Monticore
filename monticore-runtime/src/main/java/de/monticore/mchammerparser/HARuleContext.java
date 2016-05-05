package de.monticore.mchammerparser;

import org.antlr.v4.runtime.ParserRuleContext;

public class HARuleContext extends ParserRuleContext
{
	private int ruleIndex = 0;
	
	public HARuleContext(int ruleIndex)
	{
		super();
		this.ruleIndex = ruleIndex;
	}
	
	public int getRuleIndex()
	{
		return ruleIndex;
	}
}