package de.monticore.mchammerparser;

import java.util.List;
import java.util.LinkedList;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.RuleNode;

public class HAFileNode extends HAParseTree implements RuleNode
{	
	private List<Long> offsets = new LinkedList<Long>();
	
	/**
	 * Constructor for de.mchammer.rulecontexts.RNMessage.
	 * @param payload
	 */
	public HAFileNode(RuleContext payload)
	{
		super(payload);
	}
	
	public HAFileNode(Object payload, List<HAParseTree> childs)
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
	
	public void addChild(ParseTree parseTree, long offset)
	{
		childs.add(parseTree);
		offsets.add(offset);
	}
	
	public long getOffset(ParseTree parseTree)
	{
		return offsets.get(childs.indexOf(parseTree));
	}
	
	@Override
	public boolean deepEquals(HAParseTree parseTree)
	{
		if( !(parseTree instanceof HAFileNode) ||
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