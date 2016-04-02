${tc.signature("genHelper")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParserPackage()};


import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ${genHelper.getGNameToLower()}._parser.*;

//Create testing case on HTML and start testing! (Sensible class division should be done at this point)


public class ${parserName}Injector extends ${parserName}AntlrBaseListener {


	public void visitTerminal(TerminalNode node) {
		CommonToken token = (CommonToken)node.getPayload();
	 	if(token.getType() == 8 && token.getText().equals("replceme")){
		token.setText("clr\", newr \"=\";varbcccc"); //Simulates an injection in the token with text testing of type 8
		}	

	}
}
