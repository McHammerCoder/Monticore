${tc.signature("genHelper")}
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParserPackage()};

import java.util.*;
import com.google.common.collect.Lists;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.*;
import de.monticore.mchammerparser.*;

public class ${parserName}PP implements ParseTreeListener {
	
	private ArrayList<Byte> bytes = Lists.newArrayList();
	private ParseTreeWalker walker = new ParseTreeWalker();	
	private int offset = 0;

	private void append(long value, int bits)
	{
		int numBytes = bits/8 + ((bits%8 > 0) ? 1 : 0);
		for( int i = numBytes-1; i >= 0; i-- )
		{
			if( i < numBytes-1)
				append( (byte) (value >> i*8), 8 );
			else
				append( (byte) (value >> i*8), bits%8 );
		}
	}
	
	private void append(byte value, int bits)
	{
		int b = (bytes.size() > 0) ? bytes.remove(bytes.size()-1).byteValue() : 0;
		//System.out.print((byte)b);
		int v = value;
		
		int v1 = ((v << (8-bits)) >> (offset));
		int v2 = (v << ((8-offset)+(8-bits)));
		
		b = b | v1;
		offset = (offset+bits);
		
		//System.out.println("->" + (byte)b + "&" + (byte)v2);
		bytes.add((byte)b);
		
		if( offset >= 8 )
		{
			bytes.add((byte)v2);
			offset %= 8;
		}	
	}
	
	public byte[] prettyPrint(ParseTree pt){
		
		walker.walk(this, pt); 
		if(offset == 0 && bytes.size() > 0){
			bytes.remove(bytes.size()-1);
		}
	    return toSmallByte(bytes.toArray(new Byte[bytes.size()])); 
	}
	
	private byte[] toSmallByte(Byte[] oBytes) {
	   byte[] bytes = new byte[oBytes.length];

	    for(int i = 0; i < oBytes.length; i++) {
	        bytes[i] = oBytes[i];
	    }
	
	    return bytes;
	}
	
	public void visitTerminal(TerminalNode node) {
		Token token = node.getSymbol();
		if(token instanceof CommonToken){
			//System.out.print(token.getText());
			byte[] stringBytes = token.getText().getBytes();
			for(byte b : stringBytes){
				append(b,8);
			}		
		}
		else if(token instanceof HABinarySequenceToken){
			//System.out.print(token.getText());		
			List<HABinaryEntry> haBinaryEntries = ((HABinarySequenceToken) token).getValues();
			for(HABinaryEntry h: haBinaryEntries){
				append(h.getValue(),h.getBitCount());
			}
		}
	}	
	
	@Override public void enterEveryRule(ParserRuleContext ctx) { }

	@Override public void exitEveryRule(ParserRuleContext ctx) { }

	@Override public void visitErrorNode(ErrorNode node) { }
	

}