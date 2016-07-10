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
	
	@Override
	public boolean equals(Object object)
	{
		if( object instanceof HARuleContext && ((HARuleContext)object).ruleIndex == ruleIndex )
			return true;
		else
			return false;
	}
}