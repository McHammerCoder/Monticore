${tc.signature("genHelper")}
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>

import org.antlr.runtime.tree.ParseTree;

import com.upstandinghackers.hammer.*;
import com.upstandinghackers.hammer.Hammer.TokenType;

public class ${parserName}Parser
{
	// Load Hammer Library via JNI
	static {
		System.loadLibrary("jhammer");
		System.loadLibrary("jhammer_actions");
	}
	
	com.upstandinghackers.hammer.Parser parser = Hammer.nothingP();

	/**
	 * parses a binary input
	 * @param bytes DNS-message
	 * @return Antlr-ParseTree
	 */
	public ParseResult parse( byte[] bytes )
	{
		return Hammer.parse(parser, bytes, bytes.length);
	}
}