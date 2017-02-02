${tc.signature("codeGenerator","prod","genHelper")}
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParseTreePackage()};

import de.monticore.mchammerparser.*;
import java.util.*;
import com.google.common.collect.Lists;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

public class PT${prod.getName()} 
	extends 
		HATerminalNode
{
	protected PT${prod.getName()}( Token symbol )
	{
		super( symbol );
	}

	public static Builder getBuilder()
	{
		return new Builder();
	}

	public static class Builder  
	{
		private List<HABinaryEntry> values = Lists.newArrayList();
	
		public PT${prod.getName()} build() 
		{
			HABinarySequenceToken token = new HABinarySequenceToken( ${grammarName}TreeHelper.TokenType.TT_${prod.getName()}.ordinal()+1 );
			
			token.setValues(this.values);
			
			return new PT${prod.getName()}(token);
		}

		public Builder value(HABinaryEntry value) 
		{
			this.values.add(value);
			return this;
		} 
		
		public Builder values(List<HABinaryEntry> values) 
		{
			this.values.addAll(values);
			return this;
		} 
	}
	
<#list codeGenerator.getTypeConversion(prod) as method>
${method}	
</#list>
}