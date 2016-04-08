${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

package ${genHelper.getParseTreePackage()};

import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CommonTokenFactory;

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
			int tt = tok.getTokenTypeInternal();
			if(tt == Hammer.TokenType.NONE.getValue())
			{	
    		}
			else if(tt == Hammer.TokenType.BYTES.getValue())
			{
				byte[] bytes = tok.getBytesValue(); 
				for( byte b : bytes )
				{
					return new HATerminalNode(fac.create(1, Byte.toString(b)));
				}    			
			}
			else if(tt == Hammer.TokenType.SINT.getValue())
			{
				return new HATerminalNode(fac.create(2, ""+(char)tok.getSIntValue()));
			}
			else if(tt == Hammer.TokenType.UINT.getValue())
			{
				return new HATerminalNode(fac.create(3, ""+(char)tok.getUIntValue()));
			}
			else if(tt == Hammer.TokenType.SEQUENCE.getValue())
			{
				ParsedToken[] seq = tok.getSeqValue();
				HAParseTree pt = new HARuleNode( new HARuleContext( ${grammarName}TreeHelper.RuleType.RT_Undefined.ordinal() ) );
				
				for( int i = seq.length-1; i >= 0; i-- )
				{
					HAParseTree child = generateParseTree(seq[i]);
				   
					if( child.getPayload() instanceof HARuleContext && 
					  ((HARuleContext)child.getPayload()).getRuleIndex() == ${grammarName}TreeHelper.RuleType.RT_Undefined.ordinal() )
					{   
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
			}
			else if(tt == Hammer.TokenType.ERR.getValue())
			{
				System.out.println("An error occured!"); 
			}
			else if(tt >= Hammer.TokenType.USER.getValue())
			{
				if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_Undefined.getValue())
				{
					return buildRuleTree(tok, ${grammarName}TreeHelper.RuleType.RT_Undefined.ordinal());
				}
<#list genHelper.getParserRuleNames() as ruleName>
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_${ruleName}.getValue())
				{
					return buildRuleTree(tok, ${grammarName}TreeHelper.RuleType.RT_${ruleName}.ordinal());
				}
</#list>
<#assign iter=1>
<#list genHelper.getLexStrings() as lexString>
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_${iter}.getValue())
				{
					return buildStringTree(tok, ${grammarName}TreeHelper.TokenType.TT_${iter}.ordinal()+1);
				}
<#assign iter=iter+1>
</#list>
<#list genHelper.getLexerRuleNames() as lexRuleName>
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_${lexRuleName}.getValue())
				{
					return buildStringTree(tok, ${grammarName}TreeHelper.TokenType.TT_${lexRuleName}.ordinal()+1);
				}
</#list>
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_EOF.getValue())
				{
					return buildStringTree(tok, ${grammarName}TreeHelper.TokenType.TT_EOF.ordinal()+1);
				}
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_UInt8.getValue())
				{
					return buildIntTree(tok, ${grammarName}TreeHelper.TokenType.TT_UInt8);
				}
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_UInt16.getValue())
				{
					return buildIntTree(tok, ${grammarName}TreeHelper.TokenType.TT_UInt16);
				}
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_UInt32.getValue())
				{
					return buildIntTree(tok, ${grammarName}TreeHelper.TokenType.TT_UInt32);
				}
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_UInt64.getValue())
				{
					return buildIntTree(tok, ${grammarName}TreeHelper.TokenType.TT_UInt64);
				}
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_Int8.getValue())
				{
					return buildIntTree(tok, ${grammarName}TreeHelper.TokenType.TT_Int8);
				}
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_Int16.getValue())
				{
					return buildIntTree(tok, ${grammarName}TreeHelper.TokenType.TT_Int16);
				}
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_Int32.getValue())
				{
					return buildIntTree(tok, ${grammarName}TreeHelper.TokenType.TT_Int32);
				}
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_Int64.getValue())
				{
					return buildIntTree(tok, ${grammarName}TreeHelper.TokenType.TT_Int64);
				}
				else
				{
					System.out.println("User"); 
				}
			}    		
		}

		return new HATerminalNode(fac.create(0, ""));    	
	}
	
	private static HAParseTree buildRuleTree(ParsedToken tok, int tokenType)
	{
		ParsedToken[] seq = tok.getSeqValue();
		HAParseTree pt = new HARuleNode( new HARuleContext( tokenType ) );
	       
		for( int i = seq.length-1; i >= 0; i-- )
		{
			HAParseTree child = generateParseTree(seq[i]);
			
			if( child.getPayload() instanceof HARuleContext && 
			   ((HARuleContext)child.getPayload()).getRuleIndex() == ${grammarName}TreeHelper.RuleType.RT_Undefined.ordinal() )
			{
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
	}
	
	private static HAParseTree buildStringTree(ParsedToken tok, int tokenType)
	{
		CommonTokenFactory fac = new CommonTokenFactory();
		
		ParsedToken[] seq = tok.getSeqValue();
		    
		String text = new String();
		for( int i = 0; i < seq.length; i++ )
		{
			HAParseTree child = generateParseTree(seq[i]);
			
			text += child.getText();
		}
		
		HAParseTree pt = new HATerminalNode( fac.create(tokenType, text) );
		   
		return pt;
	}
	
	private static HAParseTree buildIntTree(ParsedToken tok, ${grammarName}TreeHelper.TokenType tokenType)
	{
		CommonTokenFactory fac = new CommonTokenFactory();
		
		HAParseTree pt;
		switch(tokenType)
		{
		case TT_UInt8:
			pt = new HATerminalNode( new HABinaryToken(tokenType.ordinal()+1, tok.getUIntValue(), 8, true)  );
			break;
		case TT_UInt16:
			pt = new HATerminalNode( new HABinaryToken(tokenType.ordinal()+1, tok.getUIntValue(), 16, true)  );
			break;
		case TT_UInt32:
			pt = new HATerminalNode( new HABinaryToken(tokenType.ordinal()+1, tok.getUIntValue(), 32, true)  );
			break;
		case TT_UInt64:
			pt = new HATerminalNode( new HABinaryToken(tokenType.ordinal()+1, tok.getUIntValue(), 64, true)  );
			break;
		case TT_Int8:
			pt = new HATerminalNode( new HABinaryToken(tokenType.ordinal()+1, tok.getSIntValue(), 8, true)  );
			break;
		case TT_Int16:
			pt = new HATerminalNode( new HABinaryToken(tokenType.ordinal()+1, tok.getSIntValue(), 16, true) );
			break;
		case TT_Int32:
			pt = new HATerminalNode( new HABinaryToken(tokenType.ordinal()+1, tok.getSIntValue(), 32, true) );
			break;
		case TT_Int64:
			pt = new HATerminalNode( new HABinaryToken(tokenType.ordinal()+1, tok.getSIntValue(), 64, true) );
			break;
		default:
			pt = new HATerminalNode( fac.create(tokenType.ordinal()+1, "INVALID_INT_VALUE") );
		}
		   
		return pt;
	}
}