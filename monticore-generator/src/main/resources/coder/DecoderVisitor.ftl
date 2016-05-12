${tc.signature("genHelper")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParserPackage()};

import ${genHelper.getGNameToLower()}._parser.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.*;

public class ${parserName}DecoderVisitor implements ParseTreeListener {

	private ${parserName}Decoder decoder = new ${parserName}Decoder();

	public void visitTerminal(TerminalNode node) {
		CommonToken token = (CommonToken)node.getPayload();
	 	decoder.decode(token);
	}
	
	@Override public void enterEveryRule(ParserRuleContext ctx) { }

	@Override public void exitEveryRule(ParserRuleContext ctx) { }

	@Override public void visitErrorNode(ErrorNode node) { }

}

