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

public interface PT${prod.getName()} 
	extends 
<#if codeGenerator.hasSuperClass(prod)>
	<#list codeGenerator.getSuperClass(prod) as superClass>
		${superClass}
	</#list>
</#if>
{
}