${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign grammarNameLowerCase = genHelper.getQualifiedGrammarName()?lower_case>

package ${genHelper.getParserPackage()};

import ${grammarNameLowerCase}._mch_parser.tree.*;
import ${grammarNameLowerCase}._coder.pp.*;
import com.upstandinghackers.hammer.*;
import de.monticore.mchammerparser.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.*;

public class ${grammarName}Checker {
	
	private static ${grammarName}Parser parser = new ${grammarName}Parser();
	
	private static ${grammarName}PP pp = new ${grammarName}PP();
	
	public static boolean check( int type, String string )
	{
		if( ${grammarName}TreeHelper.isBinary(type) )
			return false;

		CommonTokenFactory fac = new CommonTokenFactory();
		return ${grammarName}Checker.check( fac.create(type,string) );
	}
	
	public static boolean check( Token token )
	{
		int type = token.getType();
		if( token instanceof HABinarySequenceToken || token instanceof HAOffsetToken )
		{ 
			if( !${grammarName}TreeHelper.isBinaryProd(type) )
				return true;
				
			ParseTree parseTree = new HATerminalNode(token);
			byte [] bytes = pp.prettyPrint(parseTree);
			ParseResult pr = Hammer.parse(getParserForType(type),bytes,bytes.length);

			if( pr == null )
				return false;

			ParseTree pt = ${grammarName}TreeConverter.create(pr);
			byte [] bytesNew = pp.prettyPrint(pt);

			return ( pt instanceof HATerminalNode && ((HATerminalNode)pt).getSymbol().equals(token) );
		}
		else
		{
			String tok = token.getText();

			// keyword node?
			if( type <= ${grammarName}TreeHelper.Literals.length-1  )
			{
				// TODO: Find out what that is xD
				if(type == 0)
				{
					System.out.println("0 = " + token.getText()); 
					return true;
				}

				if(tok.equals(${grammarName}TreeHelper.Literals[type-1].substring(1,${grammarName}TreeHelper.Literals[type-1].length()-1)))
					return true;
				else
					return false;
			}
			else
			{
				try
				{
					// parses correctly?
					byte [] bytes = tok.getBytes();
					ParseResult pr = Hammer.parse(getParserForType(type),bytes,bytes.length);
					
					// contains invalid keyword?			
					for( int i = 0; i < ${grammarName}TreeHelper.Literals.length-1; i++)
					{
						int keywordValidCount = 0;
						String keyword = ${grammarName}TreeHelper.Literals[i].substring(1,${grammarName}TreeHelper.Literals[i].length()-1);
						
						// contains dangerous parts of keywords?
						startsOrEndsWithPartOfKeyword(tok,keyword);
						
						for( int j = 0; j < ${grammarName}TreeHelper.Literals.length-1; j++)
						{
							String kw = ${grammarName}TreeHelper.Literals[j].substring(1,${grammarName}TreeHelper.Literals[j].length()-1);

							if( kw.contains(keyword) )
							{
								keywordValidCount += countValidKeywords(pr.getAst(),j);
							}
						}

						int keywordCount = countKeywords(tok,keyword);

						if(keywordCount > keywordValidCount)
							return false;
					}

					// results in correct node?
					ParseTree pt = ${grammarName}TreeConverter.create(pr);
				
					if( pt instanceof HATerminalNode && ((HAParseTree)pt).getText().equals(tok) )
						return true;
					else
						return false;
				}
				catch(Exception ex)
				{
					// parsing failed !
					return false;
				}
			}
		}
	}
	
	private static boolean startsOrEndsWithPartOfKeyword(String text, String keyword)
	{
		if( keyword.length() <= 1 )
			return false;

		for( int i = 1; i < keyword.length(); i++ )
		{
			if( text.startsWith(keyword.substring(i)) )
				return true;
		}

		for( int i = keyword.length()-2; i >= 0; i-- )
		{
			if( text.endsWith(keyword.substring(0,i)) )
				return true;
		}

		return false;
	}
	
	private static int countKeywords(String text, String keyword)
	{
		int lastIndex = 0;
		int count = 0;

		while( lastIndex != -1 )
		{
			lastIndex = text.indexOf(keyword,lastIndex);
			
			if( lastIndex != -1 )
			{
				count++;
				lastIndex += keyword.length();
			}
		}

		return count;	
	}

	private static int countValidKeywords(ParsedToken tok, int tokenType)
	{
		int tt = tok.getTokenTypeInternal();
		
		if( tt == Hammer.TokenType.USER.getValue() + ${grammarName}TreeHelper.RuleTypeNames.length + tokenType )
		{
			return 1;
		}
		else if( ( tt >= Hammer.TokenType.USER.getValue() &&
			   tt < Hammer.TokenType.USER.getValue() + ${grammarName}TreeHelper.RuleTypeNames.length ) ||
			 tt >= Hammer.TokenType.USER.getValue() + ${grammarName}TreeHelper.RuleTypeNames.length + ${grammarName}TreeHelper.Literals.length -1 ||
			 tt == Hammer.TokenType.SEQUENCE.getValue() )
		{
			ParsedToken[] seq = tok.getSeqValue();
			int count = 0;	

			if( seq != null )
			{		
				
				for( int i = 0; i < seq.length; i++ )
				{
					count += countValidKeywords(seq[i],tokenType);
				}
			}

			return count;
		}
		else
			return 0;
	}
	
	private static com.upstandinghackers.hammer.Parser getParserForType(int type)
	{
		switch(type)
		{
<#assign iter=hammerGenerator.getNumLexStrings()+1>
<#list genHelper.getLexerRuleNames() as lexRuleName>
		case ${iter}: return parser._${lexRuleName};
<#assign iter=iter+1>
</#list>
<#list genHelper.getBinaryRuleNames() as binRuleName>
		case ${iter}: return parser._${binRuleName};
<#assign iter=iter+1>
</#list>
		default: return Hammer.epsilonP();
		}
	}
}