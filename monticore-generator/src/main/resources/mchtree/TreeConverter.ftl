${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParseTreePackage()};

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CommonTokenFactory;

import com.upstandinghackers.hammer.*;
import de.monticore.mchammerparser.*;

import java.util.*;
import com.google.common.collect.Lists;

public class ${grammarName}TreeConverter 
{
	private static ArrayList<Byte> bytes = Lists.newArrayList();
	private static int offset = 0;

	public static ParseTree create(ParseResult parseResult) throws MCHParserException
	{
		bytes.clear();
		offset = 0;
		return generateParseTree(parseResult.getAst());
	}
	
	public static HAParseTree generateParseTree( ParsedToken tok ) throws MCHParserException
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
					return buildRuleTreePlus(tok, ${grammarName}TreeHelper.RuleType.RT_${ruleName});
					//return buildRuleTree(tok, ${grammarName}TreeHelper.RuleType.RT_${ruleName}.ordinal());
				}
</#list>
<#list genHelper.getBinaryRuleNames() as ruleName>
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_${ruleName}.getValue())
				{
					return buildBinaryTree(tok, ${grammarName}TreeHelper.TokenType.TT_${ruleName});
					//return buildRuleTree(tok, ${grammarName}TreeHelper.RuleType.RT_${ruleName}.ordinal());
				}
</#list>
<#assign iter=1>
<#list hammerGenerator.getLexStrings() as lexString>
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_${iter}.getValue())
				{
					return buildStringTree(tok, ${grammarName}TreeHelper.TokenType.TT_${iter}.ordinal()+1);
				}
<#assign iter=iter+1>
</#list>
<#list genHelper.getLexerRuleNames() as lexRuleName>
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_${lexRuleName}.getValue())
				{
					return buildStringTreePlus(tok, ${grammarName}TreeHelper.TokenType.TT_${lexRuleName});
					//return buildStringTree(tok, ${grammarName}TreeHelper.TokenType.TT_${lexRuleName}.ordinal()+1);
				}
</#list>
<#list [8,16,32,64] as bits>
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_UInt${bits}.getValue())
				{
					return buildIntTree(tok, ${grammarName}TreeHelper.TokenType.TT_UInt${bits});
				}
</#list>
<#list [8,16,32,64] as bits>
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_Int${bits}.getValue())
				{
					return buildIntTree(tok, ${grammarName}TreeHelper.TokenType.TT_Int${bits});
				}
</#list>
<#list 1..64 as bits>
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_UBits${bits}.getValue())
				{
					return buildIntTree(tok, ${grammarName}TreeHelper.TokenType.TT_UBits${bits});
				}
</#list>
<#list 1..64 as bits>
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_Bits${bits}.getValue())
				{
					return buildIntTree(tok, ${grammarName}TreeHelper.TokenType.TT_Bits${bits});
				}
</#list>
<#list genHelper.getOffsetRulesToGenerate() as offsetProd>
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_${offsetProd.getName()}.getValue())
				{
					return buildOffsetTreePlus(tok, ${grammarName}TreeHelper.TokenType.TT_${offsetProd.getName()});
					//return buildOffsetTree(tok, ${grammarName}TreeHelper.TokenType.TT_${offsetProd.getName()});
				}
</#list>
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_EOF.getValue())
				{
					return buildStringTree(tok, ${grammarName}TreeHelper.TokenType.TT_EOF.ordinal()+1);
				}
				else if(tt == ${grammarName}TreeHelper.UserTokenTypes.UTT_Little.getValue())
				{
					HATerminalNode resTree = (HATerminalNode) generateParseTree(tok.getSeqValue()[0]);
					HABinarySequenceToken res = (HABinarySequenceToken) resTree.getSymbol();
					List<HABinaryEntry> values = res.getValues();
					for( HABinaryEntry value : values )
					{
						value.setLE(true);
					}
					res.setValues(values);
					return resTree;
				}
				else
				{
					System.out.println("User"); 
				}
			}    		
		}

		return new HATerminalNode(fac.create(0, ""));    	
	}
	
	private static HAParseTree buildRuleTreePlus(ParsedToken tok, ${grammarName}TreeHelper.RuleType ruleType) throws MCHParserException
	{
		ParsedToken[] seq = tok.getSeqValue();
		List<HAParseTree> childs = Lists.newArrayList();
	
		for( int i = 0; i < seq.length; i++ )
		{
			HAParseTree child = generateParseTree(seq[i]);
			
			if( child.getPayload() instanceof HARuleContext && 
			   ((HARuleContext)child.getPayload()).getRuleIndex() == ${grammarName}TreeHelper.RuleType.RT_Undefined.ordinal() )
			{
				for( int j = 0; j < child.getChildCount(); j++ )
				{
					childs.add((HAParseTree)child.getChild(j));
				}
			}
			else
			{
			    childs.add(child);
		    }
	    }
	
		switch(ruleType)
		{
<#list genHelper.getParserRuleNames() as ruleName>
		case RT_${ruleName}:
			return PT${ruleName}.getBuilder().addChilds(childs).build();
</#list>
		default: return null;
		}
	}
	
	private static HAParseTree buildRuleTree(ParsedToken tok, int tokenType) throws MCHParserException
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
	
	private static HAParseTree buildStringTreePlus(ParsedToken tok, ${grammarName}TreeHelper.TokenType tokenType) throws MCHParserException
	{
		ParsedToken[] seq = tok.getSeqValue();
		    
		String text = new String();
		for( int i = 0; i < seq.length; i++ )
		{
			HAParseTree child = generateParseTree(seq[i]);
			
			text += child.getText();
		}
		
		switch(tokenType)
		{
<#list genHelper.getLexerRuleNames() as lexRuleName>
		case TT_${lexRuleName}:
			return PT${lexRuleName}.getBuilder().text(text).tokenType(tokenType.ordinal()+1).build();
</#list>
		default: return null;
		}
	}
	
	private static HAParseTree buildStringTree(ParsedToken tok, int tokenType) throws MCHParserException
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
	
	private static HAParseTree buildBinaryTree(ParsedToken tok, ${grammarName}TreeHelper.TokenType tokenType) throws MCHParserException
	{		
		List<HABinaryEntry> values = Lists.newArrayList();
	
		ParsedToken[] seqArray = tok.getSeqValue();
		List<ParsedToken> seq = new ArrayList<>( Arrays.asList(seqArray) );
		    
		String text = new String();
		for( int i = 0; i < seq.size(); i++ )
		{
			HAParseTree child = generateParseTree(seq.get(i));
			
			if( child instanceof HATerminalNode )
			{
				Token symbol = ((HATerminalNode)child).getSymbol();
				
				if( symbol instanceof HABinarySequenceToken )
				{						
					List<HABinaryEntry> binValues = ((HABinarySequenceToken)symbol).getValues();
					values.addAll(binValues);
				}
			}
			else
			{
			   	ParsedToken[] seqArrayChild = seq.get(i).getSeqValue();
				List<ParsedToken> seqChild = new ArrayList<>( Arrays.asList(seqArrayChild) );
				seq.addAll(seqChild);
			}
		}
	
		switch(tokenType)
		{
<#list genHelper.getBinaryRuleNames() as ruleName>
			case TT_${ruleName}: return PT${ruleName}.getBuilder().values(values).build();
</#list>
		default:
			CommonTokenFactory fac = new CommonTokenFactory();
			return new HATerminalNode( fac.create(tokenType.ordinal()+1, "INVALID_BINARY_TOKEN") );
		}
	}
	
	private static HAParseTree buildIntTree(ParsedToken tok, ${grammarName}TreeHelper.TokenType tokenType)
	{		
		HABinarySequenceToken token = new HABinarySequenceToken(tokenType.ordinal()+1);
		switch(tokenType)
		{
<#list [8,16,32,64] as bits>
			case TT_UInt${bits}:
				token.addValue(new HABinaryEntry( tok.getUIntValue() ,${bits}, true ));
				return new HATerminalNode( token  );
</#list>
<#list [8,16,32,64] as bits>
			case TT_Int${bits}:
				token.addValue(new HABinaryEntry( tok.getSIntValue(), ${bits}, false ));
				return new HATerminalNode( token  );
</#list>
<#list 1..64 as bits>
			case TT_UBits${bits}:
				token.addValue(new HABinaryEntry( tok.getUIntValue(), ${bits}, true ));
				return new HATerminalNode( token  );
</#list>
<#list 1..64 as bits>
			case TT_Bits${bits}:
				token.addValue(new HABinaryEntry( tok.getSIntValue(), ${bits}, false ));
				return new HATerminalNode( token  );
</#list>
		default:
			CommonTokenFactory fac = new CommonTokenFactory();
			return new HATerminalNode( fac.create(tokenType.ordinal()+1, "INVALID_INT_VALUE") );
		}
	}
	
	private static HAParseTree buildOffsetTree(ParsedToken tok, ${grammarName}TreeHelper.TokenType tokenType) throws MCHParserException
	{
		CommonTokenFactory fac = new CommonTokenFactory();
		
		HAParseTree pt;
		HABinaryToken binTok;
		switch(tokenType)
		{
<#list genHelper.getOffsetRulesToGenerate() as offsetProd>
		case TT_${offsetProd.getName()}:
			pt = generateParseTree( tok.getSeqValue()[0] );
			binTok = ((HABinaryToken)((HATerminalNode)pt).getSymbol());
			binTok.setOffset(true);
			binTok.setLocal(${offsetProd.isLocal()?c});
			binTok.setType(tokenType.ordinal()+1);
			break;
</#list>
		default:
			pt = new HATerminalNode( fac.create(tokenType.ordinal()+1, "INVALID_OFFSET_VALUE") );
		}
		   
		return pt;
	}
	
	private static HAParseTree buildOffsetTreePlus(ParsedToken tok, ${grammarName}TreeHelper.TokenType tokenType) throws MCHParserException
	{
		HAParseTree pt = generateParseTree( tok.getSeqValue()[0] );
		
		HABinaryEntry value;
		HAOffsetToken token;
		switch(tokenType)
		{
<#list genHelper.getOffsetRulesToGenerate() as offsetProd>
		case TT_${offsetProd.getName()}:	
			value = ((HABinarySequenceToken) ((HATerminalNode)pt).getSymbol()).getValue(0);		
			return PT${offsetProd.getName()}.getBuilder().value( value ).build();
</#list>
		default:
			CommonTokenFactory fac = new CommonTokenFactory();
			pt = new HATerminalNode( fac.create(tokenType.ordinal()+1, "INVALID_OFFSET_VALUE") );
		}
		   
		return pt;
	}
	
	private static void append(long value, int bits)
	{
		int numBytes = bits/8 + ((bits%8 > 0) ? 1 : 0);
		for( int i = numBytes-1; i >= 0; i-- )
		{
			if( i < numBytes-1)
				append( (byte) (value >> i*8), 8 );
			else
				append( (byte) (value >> i*8), (bits%8 == 0)? 8 : bits%8 );
		}
	}
	
	private static void append(byte value, int bits)
	{
		int b = (bytes.size() > 0) ? bytes.remove(bytes.size()-1).byteValue() : 0;
		int v = value;
		
		int v1 = ((v << (8-bits)) >> (offset));
		int v2 = (v << ((8-offset)+(8-bits)));
		
		b = b | v1;
		offset = (offset+bits);
		
		bytes.add((byte)b);
		
		if( offset >= 8 )
		{
			bytes.add((byte)v2);
			offset %= 8;
		}	
	}
}