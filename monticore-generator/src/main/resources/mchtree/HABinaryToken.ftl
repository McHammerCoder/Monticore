${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

package ${genHelper.getParseTreePackage()};

import org.antlr.v4.runtime.*;

import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.Pair;

import java.io.Serializable;

public class HABinaryToken extends CommonToken {

	protected long value;
	protected int bits;
	protected boolean signed;

	public HABinaryToken(int type, long value, int bits, boolean signed) {
		super(type);
		this.channel = DEFAULT_CHANNEL;
		this.value = value;
		this.bits = bits;
		this.signed = signed;
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
}