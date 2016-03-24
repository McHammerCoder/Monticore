${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.RuleContext;

import com.upstandinghackers.hammer.*;

public class ${grammarName}TreeConverter 
{
	public static ParseTree create(ParseResult parseResult)
	{
		return generateParseTree(parseResult.getAst());
	}
	
	public static HAParseTree generateParseTree( ParsedToken tok )
    {    	
		CommonTokenFactory fac = new CommonTokenFactory();
		
    	if( tok != null )
    	{
    		switch(tok.getTokenType())
    		{
    		case NONE: System.out.println("NONE"); break;
    		case BYTES: byte[] bytes = tok.getBytesValue(); 
    		            for( byte b : bytes )
    		            {
    		            	return new HATerminalNode(fac.create(1, Byte.toString(b)));
    		            }
    		            break;
    		case SINT: return new HATerminalNode(fac.create(2, Long.toString(tok.getSIntValue())));
    		case UINT: return new HATerminalNode(fac.create(3, Long.toString(tok.getUIntValue())));
    		case SEQUENCE: ParsedToken[] seq = tok.getSeqValue();
    					   HAParseTree pt;
    					   if(${grammarName}TreeHelper.size() > 0)
    					   {
    						   ${grammarName}TreeHelper.RuleContext context = (TreeHelper.RuleContext) ${grammarName}TreeHelper.pop();
    						   pt = new HARuleNode( new HARuleContext( context.getType().ordinal() ) );
    					   } 
    					   else
    					   {
							   pt = new HARuleNode( new HARuleContext( ${grammarName}TreeHelper.RuleType.RT_Undefined.ordinal() ) ); 						   
    					   }
    						   
    					   for( int i = seq.length-1; i >= 0; i-- )
    		               {
    						   HAParseTree child = generateParseTree(seq[i]);
    						   
    						   if( child.getPayload() instanceof HARuleContext && 
    							   ((HARuleContext)child.getPayload()).getRuleIndex() == ${grammarName}TreeHelper.RuleType.RT_Undefined.ordinal() )
    						   {
    							   System.out.println("TEST TEST TEST");
    							   
    							   for( int j = child.getChildCount()-1; j >= 0; j-- )
    							   {
    								   pt.addChild((HAParseTree)child.getChild(j));
    							   }
    						   }
    						   else
    						   {
    							   pt.addChild(child);
    						   }
    		               }
    					  
    		               return pt;
    		case ERR: System.out.println("An error occured!"); break;
    		case USER: System.out.println("User"); break; //no supported jet
    		}
    	}
    	
    	return new HATerminalNode(fac.create(0, ""));    	
    }
	
	/*private static RuleContext getRuleContext(${grammarName}TreeHelper.RuleContext context)
	{
		switch(context.getType())
		{
		case RT_Message: return new RCMessage();
		default: return new RuleContext();
		}
	}*/
}