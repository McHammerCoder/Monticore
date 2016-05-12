package de.monticore.mchammerparser;

import org.antlr.v4.runtime.*;

import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.Pair;

import java.io.Serializable;

public class HABinaryToken extends CommonToken {

	protected long value;
	protected int bits;
	protected boolean signed;
	protected boolean offset;
	protected boolean local;
	protected long position;

	public HABinaryToken(int type, long value, int bits, boolean signed) {
		super(type);
		this.channel = DEFAULT_CHANNEL;
		this.value = value;
		this.bits = bits;
		this.signed = signed;
		this.offset = false;
		this.local = false;
		this.position = 0;
	}

	public HABinaryToken(HABinaryToken oldToken) {
		super(oldToken);
		value = ((HABinaryToken)oldToken).value;
		source = ((HABinaryToken)oldToken).source;
	}

	@Override
	public String getText() {
		String res = "(";
		
		if( !isSigned() )
		{
			res += "u";
		}
		
		res += "int" + this.bits + ")" + this.value;
		
		if( offset )
		{
			res += "[offset]";
		}
		
		return res;
	}
	
	public long getValue()
	{
		return this.value;
	}
	
	public int getBits()
	{
		return this.bits;
	}
	
	public boolean isSigned()
	{
		return this.signed;
	}

	public void setValue(long value, int bits, boolean signed) {
		this.value = value;
		this.bits = bits;
		this.signed = signed;
	}
	
	public void setOffset(boolean offset)
	{
		this.offset = offset;
	}
	
	public boolean isOffset()
	{
		return this.offset;
	}
	
	public void setLocal(boolean local)
	{
		this.local = local;
	}
	
	public boolean isLocal()
	{
		return this.local;
	}
	
	public void setPosition(long position)
	{
		this.position = position;
	}
	
	public long getPosition()
	{
		return this.position;
	}
}