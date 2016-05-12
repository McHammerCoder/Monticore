package de.monticore.mchammerparser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenSource;

import org.antlr.v4.runtime.*;

public class HAOffsetToken implements WritableToken 
{
	private int tokenType;
	private HABinaryEntry value;
	private long position;
	private boolean local;
	
	HAOffsetToken(	int tokenType, 
					HABinaryEntry value  ) {		
		this.tokenType = tokenType;
		this.value = value;
		this.setLocal(false);
		this.setPosition(0);
	}
	
	public HABinaryEntry getValue() {
		return value;
	}
	
	public void setValue(HABinaryEntry value) {
		this.value = value;
	}
	
	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}
	
	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}
	
	@Override
	public String getText() {
		return "[Offset]" + value.getText();
	}
	
	@Override
	public int getType() {
		return this.tokenType;
	}
	
	@Override
	public void setType(int tokenType) {
		this.tokenType = tokenType;
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
