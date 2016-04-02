${tc.signature("genHelper")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParserPackage()};

import ${genHelper.getGNameToLower()}._parser.*;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.*;

public class ${parserName}DecoderVisitor extends ${parserName}AntlrBaseListener {

	private ${parserName}Decoder decoder = new ${parserName}Decoder();

	public void visitTerminal(TerminalNode node) {
		CommonToken token = (CommonToken)node.getPayload();
	 		System.out.print(decoder.decode(token));
	}

}

