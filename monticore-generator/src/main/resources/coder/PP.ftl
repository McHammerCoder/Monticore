${tc.signature("genHelper")}
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParserPackage()};

import java.util.*;
import com.google.common.collect.Lists;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.*;
import de.monticore.mchammerparser.*;

public class ${parserName}PP implements ParseTreeListener {
	
	private ArrayList<Byte> bytes = Lists.newArrayList();
	private ParseTreeWalker walker = new ParseTreeWalker();
	
	
	
	public byte[] prettyPrint(ParseTree pt){
		
		walker.walk(this, pt); 
	    return toSmallByte(bytes.toArray(new Byte[bytes.size()])); 
	}
	
	private byte[] toSmallByte(Byte[] oBytes) {
	   byte[] bytes = new byte[oBytes.length];

	    for(int i = 0; i < oBytes.length; i++) {
	        bytes[i] = oBytes[i];
	    }
	
	    return bytes;
	}
	
	public void visitTerminal(TerminalNode node) {
		Token token = node.getSymbol();
		if(token instanceof CommonToken){
			System.out.print(token.getText());
		}
		else if(token instanceof HABinarySequenceToken){
			System.out.print(token.getText());
		}
	}	
	
	@Override public void enterEveryRule(ParserRuleContext ctx) { }

	@Override public void exitEveryRule(ParserRuleContext ctx) { }

	@Override public void visitErrorNode(ErrorNode node) { }
	

}