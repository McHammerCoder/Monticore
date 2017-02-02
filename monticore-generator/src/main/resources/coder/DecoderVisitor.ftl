${tc.signature("coderGenerator","genHelper")}
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getCoderPackage()};

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.*;

public class ${parserName}DecoderVisitor implements ParseTreeListener {

	private ${parserName}Decoder decoder = new ${parserName}Decoder();

	public void visitTerminal(TerminalNode node) {
 		Token token = node.getSymbol();
 	 	if( token instanceof CommonToken)
  	 	{
   		decoder.decode((CommonToken)token);
 		}
 }
	
	@Override public void enterEveryRule(ParserRuleContext ctx) { }

	@Override public void exitEveryRule(ParserRuleContext ctx) { }

	@Override public void visitErrorNode(ErrorNode node) { }

}

