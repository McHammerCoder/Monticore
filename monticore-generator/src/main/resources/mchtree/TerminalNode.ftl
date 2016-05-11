${tc.signature("codeGenerator","prod")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParseTreePackage()};

import de.monticore.mchammerparser.*;
import java.util.*;
import com.google.common.collect.Lists;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.CommonTokenFactory;

public class PT${prod.getName()} 
	extends HATerminalNode
{
	public PT${prod.getName()} (Token symbol) 
	{
		super(symbol);
	}
	
	public static Builder getBuilder()
	{
		return new Builder();
	}

	public static class Builder  
	{
		CommonTokenFactory fac = new CommonTokenFactory();
		String text = new String();
		int tokenType = 0;


		public PT${prod.getName()} build() 
		{
			return new PT${prod.getName()}( fac.create(tokenType, text) );
		}

		public Builder text(String text) 
		{
			this.text = text;
			return this;
		}  
		
		public Builder tokenType(int tokenType) 
		{
			this.tokenType = tokenType;
			return this;
		}  
	}
}