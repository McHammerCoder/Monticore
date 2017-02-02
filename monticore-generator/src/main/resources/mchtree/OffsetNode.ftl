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
		HABinaryEntry value;
	
		public PT${prod.getName()} build() 
		{
			HAOffsetToken token = new HAOffsetToken(	${grammarName}TreeHelper.TokenType.TT_${prod.getName()}.ordinal()+1,
														value  );
			token.setLocal(${prod.isLocal()?c});

			return new PT${prod.getName()}(token);
		}

		public Builder value(HABinaryEntry value) 
		{
			this.value = value;
			return this;
		} 
	}
}