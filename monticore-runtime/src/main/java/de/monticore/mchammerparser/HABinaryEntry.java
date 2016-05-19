package de.monticore.mchammerparser;

public class HABinaryEntry 
{
	private long value;
	private int bitCount;
	private boolean signed;
	
	public HABinaryEntry(long value, int bitCount, boolean signed)
	{
		this.value = value;
		this.bitCount = bitCount;
		this.signed = signed;
	}
	
	public long getValue()
	{
		return value;
	}
	
	public boolean isSigned()
	{
		return signed;
	}
	
	public int getBitCount()
	{
		return bitCount;
	}
	
	public void setValue(long value)
	{
		this.value = value;
	}
	
	public void setSigned(boolean signed)
	{
		this.signed = signed;
	}
	
	public void setBitCount(int bitCount)
	{
		this.bitCount = bitCount;
	}
	
	public String getText()
	{
		return ((signed)?"uint ":"int ")
				+ bitCount + "=" + value;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if( !(object instanceof HABinaryEntry) )
			return false;
		
		HABinaryEntry entry = (HABinaryEntry) object;
		
		if( this.value == entry.getValue() &&
			this.bitCount == entry.getBitCount() &&
			this.signed == entry.isSigned() )
			return true;
		else
			return false;
	}
}
