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
	
	@Override
	public boolean deepEquals(HAParseTree parseTree)
	{
		if( !(parseTree instanceof HARuleNode) ||
			!((HARuleContext)getRuleContext()).equals(((HARuleNode)parseTree).getRuleContext()) )
			return false;			
		
		if( childs.size() == parseTree.getChildCount() )
		{
			for(int i = 0; i < childs.size(); i++)
			{
				HAParseTree child = (HAParseTree) childs.get(i);
				if( !child.deepEquals((HAParseTree)parseTree.getChild(i)) )
					return false;
			}
			
			return true;
		}
		else
			return false;
	}
}