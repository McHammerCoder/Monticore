${tc.signature("codeGenerator","prod","genHelper")}
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

public abstract class PT${prod.getName()} 
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
	public PT${prod.getName()} (Object payload, List<HAParseTree> childs)
	{
		super(payload,childs);
	}
}