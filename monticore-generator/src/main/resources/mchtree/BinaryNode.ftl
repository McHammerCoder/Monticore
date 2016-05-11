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
<#if !codeGenerator.hasSuperClass(prod)>
		HARuleNode
<#else>
	<#list codeGenerator.getSuperClass(prod) as superClass>
		${superClass}
	</#list>
</#if>
<#if codeGenerator.hasInterfaces(prod)>
	implements
	<#list codeGenerator.getInterfaces(prod) as interface>
		${interface}
	</#list>
</#if>
{
	
	protected PT${prod.getName()}( List<HAParseTree> childs )
	{
		super( new HARuleContext( HTMLRedTreeHelper.RuleType.RT_${prod.getName()}.ordinal() ), childs );
	}

	public static Builder getBuilder()
	{
		return new Builder();
	}

	public static class Builder  
	{
		List<HAParseTree> childs = Lists.newArrayList();

		public PT${prod.getName()} build() 
		{
			return new PT${prod.getName()}(this.childs);
		}

		public Builder addChild(HAParseTree child) 
		{
			this.childs.add(child);
			return this;
		}  
		
		public Builder addChilds(List<HAParseTree> childs) 
		{
			this.childs.addAll(childs);
			return this;
		}  
	}
}