package de.monticore.mchammerparser;

public class HABinaryEntry 
{
	private long value;
	private int bitCount;
	private boolean signed;
	private boolean littleEndian = false;
	
	public HABinaryEntry(long value, int bitCount, boolean signed)
	{		
		this.value = value;
		this.bitCount = bitCount;
		this.signed = signed;
	}
	
	public long getValue()
	{
		if( littleEndian )
		{
			int bits = bitCount % 8;
			int bytes = bitCount / 8 + ((bits > 0) ? 1 : 0);
			
			long res = 0;
			
			// swap byte order
			for( int i = 0 ; i < bytes; i++ )
			{				
				if( i < bytes/2 )
				{
					long rByte = (0xff << i*8);
					res += ((value >> ((bytes-1-i*2)*8)) & rByte);
				}					
				if( i >= bytes/2 )
				{
					long rByte = (0xff << (bytes-1-i)*8);
					res += ((value & rByte) << ((bytes-(bytes-1-(i-bytes/2)*2) - ((bytes%2==1) ? 1 : 0))*8));
				}				
			}
			
			// adjust bit offset
			if( bits != 0 )
			{
				long moveBy = (res & (0xff >> (8-bits)));
				res = ((res - moveBy) >> (8-bits)) + moveBy;
			}
			
			return res;
		}
		else
			return value;
	}
	
	public long getRealValue() 
	{
		return value;
	}
	
	public void setRealValue(long value)
	{
		this.value = value;
	}
	
	public boolean isSigned()
	{
		return signed;
	}
	
	public int getBitCount()
	{
		return bitCount;
	}
	
	public boolean isLE()
	{
		return littleEndian;
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
	
	public void setLE(boolean littleEndian)
	{
		this.littleEndian = littleEndian;
	}
	
	public String getText()
	{
		return ((signed)?"uint ":"int ")
				+ bitCount + "=" + getRealValue();
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
