${tc.signature("genHelper")}
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getPPPackage()};

import java.util.*;
import com.google.common.collect.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.*;
import de.monticore.mchammerparser.*;

public class ${parserName}PP implements ParseTreeListener {
	
	private ArrayList<Byte> bytes = Lists.newArrayList();
	private ArrayList<Byte> result = Lists.newArrayList();
	private ParseTreeWalker walker = new ParseTreeWalker();	
	private Map<Long, HAParseTree> map = Maps.newHashMap();
	private int offset = 0;
	private int resOffset = 0;


	private void appendToResult(byte [] byteArray, int size)
	{
		int numBytes = byteArray.length + ((size > 0) ? 1 : 0);
		for( int i = 0; i <= numBytes-1; i++ )
		{
			if( i < numBytes-1)
				appendToResult( byteArray[i], 8 );
			else
				appendToResult( byteArray[i], (size%8 == 0)? 8 : size%8 );
		}
	}

	private void appendToResult(byte value, int bits)
	{
		int b = (result.size() > 0) ? result.remove(result.size()-1).byteValue() : 0;
		int v = value;
		
		int v1 = ((v << (8-bits)) >> (resOffset));
		int v2 = (v << ((8-resOffset)+(8-bits)));
		
		b = b | v1;
		resOffset = (resOffset+bits);
		
		result.add((byte)b);
		
		if( resOffset >= 8 )
		{
			result.add((byte)v2);
			resOffset %= 8;
		}	
	}

	private void append(long value, int bits)
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
	
	private void append(byte value, int bits)
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
	
	public byte[] prettyPrint(ParseTree pt){
	
		result.clear();
		resOffset = 0;
		offset = 0;

		if(pt instanceof HAFileNode){
			HAFileNode fn = (HAFileNode) pt;
			int childCount = fn.getChildCount();

			for( int i = 0; i < childCount; i++){
				HAParseTree child = (HAParseTree)fn.getChild(i);
				long offset = fn.getOffset(child);
				map.put(offset, child);
			}

			for(int i = 0; i < map.size(); ){
				long currentOffset = Long.MAX_VALUE;
				for(Long offset : map.keySet()){
					if(offset < currentOffset){
						currentOffset = offset;
					}
				}
				
				List<Long> list = Lists.newArrayList(map.keySet());
				int index = list.indexOf(new Long(currentOffset));
				HAParseTree child = Lists.newArrayList(map.values()).get(index); 
				
				walker.walk(this, child);
				if(offset == 0 && bytes.size() > 0){
					bytes.remove(bytes.size()-1);
				}
				appendToResult(toSmallByte(bytes.toArray(new Byte[bytes.size()])), (int) (getSize(child)%8));

				bytes = Lists.newArrayList();
				map.remove(currentOffset);
			}
			if(offset == 0 && result.size() > 0){
				result.remove(result.size()-1);
			}
			return toSmallByte(result.toArray(new Byte[result.size()])); 
	    }
	    else
		  {
		   walker.walk(this, pt);
		   if(offset == 0 && bytes.size() > 0){
		    bytes.remove(bytes.size()-1);
		   }
		   return toSmallByte(bytes.toArray(new Byte[bytes.size()])); 
		  }
	}
	
	private byte[] toSmallByte(Byte[] oBytes) {
	   byte[] bytes = new byte[oBytes.length];

	    for(int i = 0; i < oBytes.length; i++) {
	        bytes[i] = oBytes[i];
	    }
	
	    return bytes;
	}
	

	private long getSize(HAParseTree parseTree)
	{
		long size = 0;

		if( parseTree instanceof HATerminalNode )
		{
			Token token = ((HATerminalNode)parseTree).getSymbol();
			if( token instanceof HABinarySequenceToken )
			{
				for( HABinaryEntry bin : ((HABinarySequenceToken)token).getValues() )
				{
					size += bin.getBitCount();
				}
			}
			else if( token instanceof HAOffsetToken )
			{
				size += ((HAOffsetToken)token).getValue().getBitCount();
			}
			else
			{
				size += parseTree.getText().getBytes().length*8;
			}
		}
		else
		{
			for( int i = 0; i < parseTree.getChildCount(); i++ )
			{
				ParseTree child = parseTree.getChild(i);
				size += getSize((HAParseTree)child);
			}
		}
		
		return size;
	}


	public void visitTerminal(TerminalNode node) {
		Token token = node.getSymbol();
		if(token instanceof CommonToken){
			byte[] stringBytes = token.getText().getBytes();
			for(byte b : stringBytes){
				append(b,8);
			}		
		}
		else if(token instanceof HABinarySequenceToken){	
			List<HABinaryEntry> haBinaryEntries = ((HABinarySequenceToken) token).getValues();
			for(HABinaryEntry h: haBinaryEntries){
				int	unserBitCount = h.getBitCount();
				append(h.getValue(), unserBitCount);
			}
		}
		else if(token instanceof HAOffsetToken){
			HABinaryEntry haBinaryEntry = ((HAOffsetToken) token).getValue();
			append(haBinaryEntry.getValue(), haBinaryEntry.getBitCount());
		}
	}	
	
	@Override public void enterEveryRule(ParserRuleContext ctx) { }

	@Override public void exitEveryRule(ParserRuleContext ctx) { }

	@Override public void visitErrorNode(ErrorNode node) { }
	

}