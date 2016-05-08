${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign grammarNameLowerCase = genHelper.getQualifiedGrammarName()?lower_case>

package ${genHelper.getParserPackage()};

import ${grammarNameLowerCase}._mch_parser.tree.*;
import com.upstandinghackers.hammer.*;
import de.monticore.mchammerparser.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.*;

public class ${grammarName}Checker {
	
	private static ${grammarName}Parser parser = new ${grammarName}Parser();
	
	public static boolean check( String tok, int type )
	{
		if( type <= ${grammarName}TreeHelper.Literals.length-1 )
		{
			if(tok.equals(${grammarName}TreeHelper.Literals[type-1].substring(1,${grammarName}TreeHelper.Literals[type-1].length()-1)))
				return true;
			else
				return false;
		}
		else
		{
			for( int i = 0; i < ${grammarName}TreeHelper.Literals.length-1; i++)
			{
				if(tok.contains(${grammarName}TreeHelper.Literals[i].substring(1,${grammarName}TreeHelper.Literals[i].length()-1)))
					return false;
			}
			
			try {
				byte [] bytes = tok.getBytes();
				
				ParseTree pt = ${grammarName}TreeConverter.create(Hammer.parse(getParserForType(type),bytes,bytes.length));
				if( pt instanceof HATerminalNode && ((HAParseTree)pt).getText().equals(tok) )
					return true;
				else
					return false;
			}
			catch(Exception ex)
			{
				return false;
			}
		}
	}
	
	private static com.upstandinghackers.hammer.Parser getParserForType(int type)
	{
		switch(type)
		{
<#assign iter=genHelper.getNumLexStrings()+1>
<#list genHelper.getLexerRuleNames() as lexRuleName>
		case ${iter}: return parser._${lexRuleName};
<#assign iter=iter+1>
</#list>
		default: return Hammer.nothingP();
		}
	}
}