${tc.signature("genHelper")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign packageName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getCoderPackage()};


import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.*;


public class ${parserName}EncoderVisitor implements ParseTreeListener {

	private ${parserName}Encoder encoder = new ${parserName}Encoder();
	private boolean foundException = false;
	
	public boolean foundException(){
		return foundException;
	}
	public void clearException(){
		foundException=false;
	}
	public void visitTerminal(TerminalNode node) {
		Token token = (Token)node.getPayload();
	
		try{
			if(!encoder.check(token)) {
				encoder.encode(token);
			}
		}
		catch(Exception e)
		{
			foundException = true;
			e.printStackTrace();
		}
	}	
	
	@Override public void enterEveryRule(ParserRuleContext ctx) { }

	@Override public void exitEveryRule(ParserRuleContext ctx) { }

	@Override public void visitErrorNode(ErrorNode node) { }
	

}
