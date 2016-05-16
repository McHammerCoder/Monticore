${tc.signature("genHelper")}
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParserPackage()};


import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//import ${genHelper.getGNameToLower()}._parser.*;

//Create testing case on HTML and start testing! (Sensible class division should be done at this point)


public class ${parserName}Injector  implements ParseTreeListener  {

	private String injection = new String();

	${parserName}Injector(String injection)
	{
		this.injection = injection;
	}
	
	public void visitTerminal(TerminalNode node) {
		CommonToken token = (CommonToken)node.getPayload();
	 	if(token.getText().contains("Text")){
		token.setText(token.getText().replace("Text",injection)); //Simulates an injection in the token with text testing of type 8
		}	

	}
	@Override public void enterEveryRule(ParserRuleContext ctx) { }

	@Override public void exitEveryRule(ParserRuleContext ctx) { }

	@Override public void visitErrorNode(ErrorNode node) { }
}
