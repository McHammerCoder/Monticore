package de.monticore.mchammerparser;

import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * TODO: Write me!
 *
 * @author  (last commit) $Author$
 * @version $Revision$, $Date$
 * @since   TODO: add version number
 *
 */
public class HAParseTree implements ParseTree
{
	ParseTree parent = null;
	List<ParseTree> childs = new LinkedList<ParseTree>();
	protected Object payload;
	
	public HAParseTree(Object payload)
	{
		this.payload = payload;
	}
	
	public void addChild(ParseTree child)
	{
		if(child instanceof HAParseTree)
		{
			((HAParseTree)child).setParent(this);
		}
		childs.add(0,child);
	}
	
	private void setParent(ParseTree parent)
	{
		this.parent = parent;
	}
	
	/**
	 * @see org.antlr.v4.runtime.tree.SyntaxTree#getSourceInterval()
	 */
	public Interval getSourceInterval() 
	{
		return Interval.INVALID;
	}

	/**
	 * @see org.antlr.v4.runtime.tree.Tree#getChildCount()
	 */
	public int getChildCount() 
	{
		return childs.size();
	}

	/**
	 * @see org.antlr.v4.runtime.tree.Tree#getPayload()
	 */
	public Object getPayload() 
	{
		return payload;
	}

	/**
	 * @see org.antlr.v4.runtime.tree.Tree#toStringTree()
	 */
	public String toStringTree() 
	{
		if( this instanceof HATerminalNode )
		{
			return "['" + ((HATerminalNode)this).getSymbol().getText() + "']";
		}
		else
		{
			String res = "[ ";
			
			for( ParseTree child : childs)
			{
				res += ((HAParseTree)child).toStringTree();
			}
		
			return res + " ]";
		}
	}

	/**
	 * @see org.antlr.v4.runtime.tree.ParseTree#accept(org.antlr.v4.runtime.tree.ParseTreeVisitor)
	 */
	public <T> T accept(ParseTreeVisitor<? extends T> arg0) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.antlr.v4.runtime.tree.ParseTree#getChild(int)
	 */
	public ParseTree getChild(int index) 
	{
		return childs.get(index);
	}

	/**
	 * @see org.antlr.v4.runtime.tree.ParseTree#getParent()
	 */
	public ParseTree getParent() 
	{
		return parent;
	}

	/**
	 * @see org.antlr.v4.runtime.tree.ParseTree#getText()
	 */
	public String getText() 
	{		
		if( this instanceof HATerminalNode )
		{
			return ((HATerminalNode)this).getSymbol().getText();
		}
		else
		{
			String res = "";
			
			for( ParseTree child : childs)
			{
				res += child.getText();
			}
		
			return res;
		}
	}

	/**
	 * @see org.antlr.v4.runtime.tree.ParseTree#toStringTree(org.antlr.v4.runtime.Parser)
	 */
	public String toStringTree(Parser arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	

}