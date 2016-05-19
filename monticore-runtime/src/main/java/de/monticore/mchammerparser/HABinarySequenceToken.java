package de.monticore.mchammerparser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenSource;

import java.util.List;

import org.antlr.v4.runtime.*;

import com.google.common.collect.Lists;

public class HABinarySequenceToken implements WritableToken {
	private int tokenType;
	private List<HABinaryEntry> values = Lists.newArrayList();
	
	public HABinarySequenceToken(int tokenType ) {		
		this.tokenType = tokenType;
	}
	
	public List<HABinaryEntry> getValues() {
		return values;
	}
	
	public void setValues(List<HABinaryEntry> values) {
		this.values = values;
	}
	
	public HABinaryEntry getValue(int index) {
		return values.get(index);
	}
	
	public void addValue(HABinaryEntry value) {
		this.values.add(value);
	}
	
	@Override
	public String getText() {
		String res = "";
		
		for( HABinaryEntry value : values )
		{
			res += value.getText() + " ";
		}
		
		return res;
	}
	
	@Override
	public int getType() {
		return this.tokenType;
	}
	
	@Override
	public void setType(int tokenType) {
		this.tokenType = tokenType;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if( !(object instanceof HABinarySequenceToken) )
		{
			return false;
		}
		
		HABinarySequenceToken token = (HABinarySequenceToken)object;
		
		if( tokenType != token.getType() )
			return false;
		
		
		List<HABinaryEntry> values = token.getValues();
		
		if( !(this.values.size() == values.size()) )
		{
			return false;
		}
		
		for( int i = 0; i < values.size(); i++)
		{
			if( !this.values.get(i).equals(values.get(i)) )
			{
				return false;
			}
		}
		
		return true;
	}
	
	// ------ Unsupported Methods -------

	@Override
	public int getChannel() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getCharPositionInLine() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CharStream getInputStream() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLine() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getStartIndex() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getStopIndex() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getTokenIndex() {
		throw new UnsupportedOperationException();
	}

	@Override
	public TokenSource getTokenSource() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setChannel(int arg0) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void setCharPositionInLine(int arg0) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void setLine(int arg0) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void setText(String arg0) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void setTokenIndex(int arg0) {
		throw new UnsupportedOperationException();
	}
}
