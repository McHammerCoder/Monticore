${tc.signature("genHelper")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign packageName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParserPackage()};


import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;

import ${genHelper.getGNameToLower()}._parser.*;

public class ${parserName}EncoderVisitor extends ${parserName}AntlrBaseListener {

	private ${parserName}Encoder encoder = new ${parserName}Encoder();


	public void visitTerminal(TerminalNode node) {
		CommonToken token = (CommonToken)node.getPayload();
	
		if(!encoder.check(token)) {
			encoder.encode(token);
		}
	}	

}
