${tc.signature("codeGenerator","prod")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParseTreePackage()};

import de.monticore.mchammerparser.*;
import java.util.*;
import com.google.common.collect.Lists;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

public class PT${prod.getName()} 
	extends 
		HATerminalNode
{
	List<HABinaryEntry> binaries = Lists.newArrayList();

	protected PT${prod.getName()}( List<HABinaryEntry> binaries )
	{
		super( new HARuleContext( ${grammarName}TreeHelper.RuleType.RT_${prod.getName()}.ordinal() ) );
		
		this.binaries.addAll(binaries);
	}
	
	

	public static Builder getBuilder()
	{
		return new Builder();
	}

	public static class Builder  
	{
		List<HABinaryEntry> binaries;

		public PT${prod.getName()} build() 
		{
			return new PT${prod.getName()}(this.binaries);
		}

		public Builder binary(HABinaryEntry child) 
		{
			this.binaries.add(binary);
			return this;
		}  
		
		public Builder binaries(List<HABinaryEntry> binaries) 
		{
			this.binaries.addAll(binaries);
			return this;
		}  
	}
}