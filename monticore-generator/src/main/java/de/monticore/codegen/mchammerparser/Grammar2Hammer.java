/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mchammerparser;

import static de.monticore.codegen.parser.ParserGeneratorHelper.getMCRuleForThisComponent;
import static de.monticore.codegen.parser.ParserGeneratorHelper.getTmpVarNameForAntlrCode;
import static de.monticore.codegen.parser.ParserGeneratorHelper.printIteration;

import java.util.*;

import org.apache.commons.lang3.StringEscapeUtils;

import org.codehaus.groovy.tools.shell.IO;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.upstandinghackers.hammer.Hammer;

import de.monticore.ast.ASTNode;
import de.monticore.codegen.parser.ParserGeneratorHelper;
import de.monticore.codegen.parser.antlr.ASTConstructionActions;
import de.monticore.codegen.parser.antlr.AttributeCardinalityConstraint;
import de.monticore.grammar.DirectLeftRecursionDetector;
import de.monticore.grammar.HelperGrammar;
import de.monticore.grammar.MCGrammarInfo;
import de.monticore.grammar.grammar._ast.ASTAlt;
import de.monticore.grammar.grammar._ast.ASTAnything;
import de.monticore.grammar.grammar._ast.ASTBinaryAlt;
import de.monticore.grammar.grammar._ast.ASTBinaryBlock;
import de.monticore.grammar.grammar._ast.ASTBinaryComponent;
import de.monticore.grammar.grammar._ast.ASTBinaryLength;
import de.monticore.grammar.grammar._ast.ASTBinaryData;
import de.monticore.grammar.grammar._ast.ASTBinaryLengthValue;
import de.monticore.grammar.grammar._ast.ASTBinaryNonTerminal;
import de.monticore.grammar.grammar._ast.ASTBinaryNRepeat;
import de.monticore.grammar.grammar._ast.ASTBinaryProd;
import de.monticore.grammar.grammar._ast.ASTBinarySimpleIteration;
import de.monticore.grammar.grammar._ast.ASTBits;
import de.monticore.grammar.grammar._ast.ASTUBits;
import de.monticore.grammar.grammar._ast.ASTBlock;
import de.monticore.grammar.grammar._ast.ASTClassProd;
import de.monticore.grammar.grammar._ast.ASTConstant;
import de.monticore.grammar.grammar._ast.ASTConstantGroup;
import de.monticore.grammar.grammar._ast.ASTConstantsGrammar;
import de.monticore.grammar.grammar._ast.ASTEnumProd;
import de.monticore.grammar.grammar._ast.ASTEof;
import de.monticore.grammar.grammar._ast.ASTGrammarNode;
import de.monticore.grammar.grammar._ast.ASTInterfaceProd;
import de.monticore.grammar.grammar._ast.ASTLexActionOrPredicate;
import de.monticore.grammar.grammar._ast.ASTLexAlt;
import de.monticore.grammar.grammar._ast.ASTLexBlock;
import de.monticore.grammar.grammar._ast.ASTLexChar;
import de.monticore.grammar.grammar._ast.ASTLexCharRange;
import de.monticore.grammar.grammar._ast.ASTLexComponent;
import de.monticore.grammar.grammar._ast.ASTLexNonTerminal;
import de.monticore.grammar.grammar._ast.ASTLexOption;
import de.monticore.grammar.grammar._ast.ASTLexProd;
import de.monticore.grammar.grammar._ast.ASTLexSimpleIteration;
import de.monticore.grammar.grammar._ast.ASTLexString;
import de.monticore.grammar.grammar._ast.ASTMCAnything;
import de.monticore.grammar.grammar._ast.ASTNonTerminal;
import de.monticore.grammar.grammar._ast.ASTOptionValue;
import de.monticore.grammar.grammar._ast.ASTProd;
import de.monticore.grammar.grammar._ast.ASTRuleComponent;
import de.monticore.grammar.grammar._ast.ASTRuleReference;
import de.monticore.grammar.grammar._ast.ASTSemanticpredicateOrAction;
import de.monticore.grammar.grammar._ast.ASTTerminal;
import de.monticore.grammar.grammar._ast.ASTUInt8;
import de.monticore.grammar.grammar._ast.ASTUInt16;
import de.monticore.grammar.grammar._ast.ASTUInt32;
import de.monticore.grammar.grammar._ast.ASTUInt64;
import de.monticore.grammar.grammar._ast.ASTInt8;
import de.monticore.grammar.grammar._ast.ASTInt16;
import de.monticore.grammar.grammar._ast.ASTInt32;
import de.monticore.grammar.grammar._ast.ASTInt64;
import de.monticore.grammar.grammar._ast.ASTOffset;
import de.monticore.grammar.grammar._ast.GrammarNodeFactory;
import de.monticore.grammar.grammar_withconcepts._ast.ASTAction;
import de.monticore.grammar.grammar_withconcepts._visitor.Grammar_WithConceptsVisitor;
import de.monticore.grammar.prettyprint.Grammar_WithConceptsPrettyPrinter;
import de.monticore.java.javadsl._ast.ASTBlockStatement;
import de.monticore.languages.grammar.MCAttributeSymbol;
import de.monticore.languages.grammar.MCGrammarSymbol;
import de.monticore.languages.grammar.MCRuleComponentSymbol;
import de.monticore.languages.grammar.MCRuleSymbol;
import de.monticore.languages.grammar.MCRuleSymbol.KindSymbolRule;
import de.monticore.languages.grammar.MCTypeSymbol;
import de.monticore.languages.grammar.MCTypeSymbol.KindType;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.languages.grammar.PredicatePair;
import de.monticore.symboltable.Symbol;
import de.monticore.utils.ASTNodes;
import de.se_rwth.commons.logging.Log;

/**
 * TODO: Write me!
 *
 * @author  (last commit) $Author$
 * @version $Revision$, $Date$
 * @since   TODO: add version number
 *
 */
public class Grammar2Hammer implements Grammar_WithConceptsVisitor
{
	
	
	private MCGrammarSymbol grammarEntry;
	
	private McHammerParserGeneratorHelper parserGeneratorHelper;
	
	private static Grammar_WithConceptsPrettyPrinter prettyPrinter;
	
	private MCGrammarInfo grammarInfo;
	
	private List<String> productionHammerCode = Lists.newArrayList();
	
	private StringBuilder codeSection;
	
	private String indent = "\t\t";
	  
	private static Map<String,List<String>> interfaces = Maps.newHashMap();
	
	private static Set<String> lengthFields = Sets.newHashSet();
	
	private static Map<String,Set<ASTGrammarNode>> dataFields = Maps.newHashMap();
		
	private GrammarAnalyzer grammarAnalyzer = new GrammarAnalyzer();
	
	public Grammar2Hammer(McHammerParserGeneratorHelper parserGeneratorHelper, MCGrammarInfo grammarInfo) 
	{
		Preconditions.checkArgument(parserGeneratorHelper.getGrammarSymbol() != null);
		this.parserGeneratorHelper = parserGeneratorHelper;
		this.grammarEntry = parserGeneratorHelper.getGrammarSymbol();
		this.grammarInfo = grammarInfo;
		
		// Find all DataFields in the grammar
		List<ASTProd> rules = parserGeneratorHelper.getParserRulesToGenerate();
		rules.addAll( parserGeneratorHelper.getBinaryRulesToGenerate() );
		
		for( ASTProd rule : rules )
		{
			for( String data : grammarAnalyzer.containsDataFields(rule).keySet() )
			{
				System.out.println(data);
			}
			
			dataFields.putAll(grammarAnalyzer.containsDataFields(rule));
			lengthFields.addAll(grammarAnalyzer.containsLengthFields(rule));
		}
	}

	@Override
	public void handle(ASTClassProd ast)
	{
		startCodeSection("ASTClassProd");
		addToCodeSection(indent + "_" + ast.getName() + ".bindIndirect( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.choice( ");
		increaseIndent();
		
		List<ASTRuleReference> superInterfaces = ast.getSuperInterfaceRule();
		for( ASTRuleReference i : superInterfaces )
		{
			interfaces.get(i.getName()).add(ast.getName());
		}
		
		List<ASTAlt> alts = ast.getAlts();
		for( int i = 0; i < alts.size(); i++ )
		{
			ASTAlt alt = alts.get(i);
			alt.accept(getRealThis());
			
			if( i < alts.size()-1 )
			{
				addToCodeSection(", ");
			}
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ")");
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"act" + ast.getName() + "\" )");
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ");");
		
		endCodeSection();
	}
	
	@Override
	public void handle(ASTEnumProd ast)
	{
		startCodeSection("ASTEnumProd");
		
		addToCodeSection(indent + "_" + ast.getName() + ".bindIndirect( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.choice( ");
		increaseIndent();
		
		List<ASTConstant> constants = ast.getConstants();
		for( int i = 0; i < constants.size(); i++ )
		{			
			ASTConstant constant = constants.get(i);
			constant.accept(getRealThis());
			
			if( i < constants.size()-1 )
			{
				addToCodeSection(", ");
			}
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ")");
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ");");
		
		endCodeSection();
	}
	
	@Override
	public void handle(ASTConstantGroup ast)
	{
		printIteration(ast.getIteration());
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.choice( ");
		increaseIndent();
		
		List<ASTConstant> constants = ast.getConstants();
		for( int i = 0; i < constants.size(); i++ )
		{			
			ASTConstant constant = constants.get(i);
			constant.accept(getRealThis());
			
			if( i < constants.size()-1 )
			{
				addToCodeSection(", ");
			}
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ")");
		
		decreaseIndent();
		printIterationEnd(ast.getIteration());		
	}
	
	@Override
	public void handle(ASTConstant ast)
	{
		String name = decodeString(ast.getName());
				
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.sequence( ");
		increaseIndent();
		
		for( int i = 0; i < name.length(); i++ )
		{
			String c = encodeChar(name.charAt(i));
			addToCodeSection("\n" + indent + "Hammer.intRange( uInt_8, (byte)'" + c + "', (byte)'" + c + "')");
			if( i < name.length()-1 )
			{
				addToCodeSection(", ");
			}
			else
			{
				addToCodeSection(" ");
			}
		}

		decreaseIndent();
		addToCodeSection("\n" + indent + ")");
		
		int id = 0;
		List<String> terminals = parserGeneratorHelper.getLexStrings();
		for( int i = 0; i < terminals.size(); i++ )
		{
			if(terminals.get(i).equals(name))
			{
				id = i+1;
			}			
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actTT_" + id + "\" )");
	}
	
	/**
	 * Printable start representation of iteration
	 * 
	 * @param i Value from AST
	 * @return String representing value i
	 */
	public void printIteration(int i) 
	{
		switch (i) 
		{
		case ASTConstantsGrammar.PLUS:
			addToCodeSection( "\n" + indent + "Hammer.action( " );
			increaseIndent();
			addToCodeSection( "\n" + indent + "Hammer.many1( " );
			increaseIndent();
			break;
		case ASTConstantsGrammar.STAR:
			addToCodeSection( "\n" + indent + "Hammer.action( " );
			increaseIndent();
			addToCodeSection( "\n" + indent + "Hammer.many( " );
			increaseIndent();
			break;
		case ASTConstantsGrammar.QUESTION:
			addToCodeSection( "\n" + indent + "Hammer.optional( " );
			increaseIndent();
		}
	}

	/**
	 * Printable end representation of iteration
	 * 
	 * @param i Value from AST
	 * @return String representing value i
	 */
	public void printIterationEnd(int i) 
	{
		switch (i) 
		{
		case ASTConstantsGrammar.PLUS:
		case ASTConstantsGrammar.STAR:
			decreaseIndent();
			addToCodeSection("\n" + indent + ")");
			decreaseIndent();
			addToCodeSection("\n" + indent + ", \"actUndefined\" )");
			break;
		case ASTConstantsGrammar.QUESTION:
			decreaseIndent();
			addToCodeSection("\n" + indent + ")");
			break;
		}	
	}
	
	
	
	@Override
	public void handle(ASTBlock ast) 
	{
		printIteration(ast.getIteration()); 
		
		
		addToCodeSection("\n" + indent + "Hammer.choice( ");
		increaseIndent();
		
		List<ASTAlt> alts = ast.getAlts();
		for( int i = 0; i < alts.size(); i++ )
		{
			addToCodeSection("\n" + indent + "Hammer.action( ");
			increaseIndent();
			
			ASTAlt alt = alts.get(i);
			alt.accept(getRealThis());
			
			decreaseIndent();
			addToCodeSection("\n" + indent + ", \"actUndefined\" )");
			
			if( i < alts.size()-1 )
			{
				addToCodeSection(", ");
			}
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ")");
		
		printIterationEnd(ast.getIteration()); 
	}
	
	@Override
	public void visit(ASTTerminal ast) 
	{
		String name = StringEscapeUtils.unescapeJava(ast.getName());
		
		printIteration(ast.getIteration());
		
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.sequence( ");
		increaseIndent();
		
		for( int i = 0; i < name.length(); i++ )
		{
			Character c = name.charAt(i);
			String ch = StringEscapeUtils.escapeJava(c.toString());
			addToCodeSection( "\n" 
							+ indent 
							+ "Hammer.intRange( "
							+ ((c > 0x00FF) ? "uInt_16" : "uInt_8")
							+ ", (long)'" 
							+ ch
							+ "', (long)'" 
							+ ch
							+ "')");
			if( i < name.length()-1 )
			{
				addToCodeSection(", ");
			}
			else
			{
				addToCodeSection(" ");
			}
		}

		decreaseIndent();
		addToCodeSection("\n" + indent + ")");
		
		int id = 0;
		List<String> terminals = parserGeneratorHelper.getLexStrings();
		for( int i = 0; i < terminals.size(); i++ )
		{
			if(terminals.get(i).equals(name))
			{
				id = i+1;
			}			
		}
		
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actTT_" + id + "\" )");
		
		printIterationEnd(ast.getIteration());
	}
	
	@Override
	public void handle(ASTLexProd ast) 
	{
		startCodeSection("ASTLexProd");
		
		addToCodeSection(indent + "_" + ast.getName() + ".bindIndirect( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.choice( ");
		increaseIndent();
		
		List<ASTLexAlt> alts = ast.getAlts();
		for( int i = 0; i < alts.size(); i++ )
		{
			ASTLexAlt alt = alts.get(i);
			alt.accept(getRealThis());
			
			if( i < alts.size()-1 )
			{
				addToCodeSection(", ");
			}
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ")");
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"act" + ast.getName() + "\" )");
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ");");
		
		endCodeSection();
	}

	private boolean negated = false;
	
	@Override
	public void handle(ASTLexBlock ast) 
	{
		printIteration(ast.getIteration()); 
				
		if( ast.isNegate() )
		{
			addToCodeSection("\n" + indent + "Hammer.butNot( ");
			increaseIndent();
			
			addToCodeSection("\n" + indent + "Hammer.choice(");
			increaseIndent();
			
			addToCodeSection("\n" + indent + "Hammer.intRange( uInt_8, (long)'\\u0000', (long)'\\u0079')," );
			addToCodeSection("\n" + indent + "Hammer.intRange( uInt_16, (long)'\\uc280', (long)'\\udfba')" );
			
			decreaseIndent();
			addToCodeSection("\n" + indent + "),");
			
			addToCodeSection("\n" + indent + "Hammer.choice(");
			increaseIndent();
			
			List<ASTLexAlt> alts = ast.getLexAlts();
			for( int i = 0; i < alts.size(); i++ )
			{			
				ASTLexAlt alt = alts.get(i);
				alt.accept(getRealThis());
				
				if( i < alts.size()-1 )
				{
					addToCodeSection(",");
				}
			}
			
			decreaseIndent();
			addToCodeSection("\n" + indent + ")");
			
			decreaseIndent();
			addToCodeSection("\n" + indent + ")");
		}
		else
		{
			addToCodeSection("\n" + indent + "Hammer.choice( ");
			increaseIndent();
			
			List<ASTLexAlt> alts = ast.getLexAlts();
			for( int i = 0; i < alts.size(); i++ )
			{
				addToCodeSection("\n" + indent + "Hammer.action( ");
				increaseIndent();
				
				ASTLexAlt alt = alts.get(i);
				alt.accept(getRealThis());
				
				decreaseIndent();
				addToCodeSection("\n" + indent + ", \"actUndefined\" )");
				
				if( i < alts.size()-1 )
				{
					addToCodeSection(", ");
				}
			}
			
			decreaseIndent();
			addToCodeSection("\n" + indent + ")");
		}
		
		printIterationEnd(ast.getIteration()); 
	}
		
	@Override
	public void visit(ASTLexCharRange ast) 
	{
		String lower = ast.getLowerChar();
		char lowerChar = StringEscapeUtils.unescapeJava(lower).charAt(0);
		String upper = ast.getUpperChar();
		char upperChar = StringEscapeUtils.unescapeJava(upper).charAt(0);
		
		if( ast.isNegate() )
		{
			addToCodeSection("\n" + indent + "Hammer.choice(");
			increaseIndent();
			
			if( upperChar <= 0x007F )
			{
				addToCodeSection("\n" + indent + "Hammer.butNot( Hammer.intRange( uInt_8, (long)'\\u0000', (long)'\\u0079'), Hammer.intRange( uInt_8, (long)'" + lower  + "', (long)'" + upper + "') )," );
				addToCodeSection("\n" + indent + "Hammer.intRange( uInt_16, (long)'\\uc280', (long)'\\udfba')" );
			}
			else if( lowerChar > 0xc280 )
			{
				addToCodeSection("\n" + indent + "Hammer.intRange( uInt_8, (long)'\\u0000', (long)'\\u0079')," );
				addToCodeSection("\n" + indent + "Hammer.butNot( Hammer.intRange( uInt_16, (long)'\\uc280', (long)'\\udfba'), Hammer.intRange( uInt_16, (long)'" + lower  + "', (long)'" + upper + "') )" );
			}
			else
			{
				addToCodeSection("\n" + indent + "Hammer.butNot( Hammer.intRange( uInt_16, (long)'\\uc280', (long)'\\udfba'), Hammer.intRange( uInt_16, (long)'\\uc280', (long)'" + upper + "') )," );
				addToCodeSection("\n" + indent + "Hammer.butNot( Hammer.intRange( uInt_8, (long)'\\u0000', (long)'\\u0079'), Hammer.intRange( uInt_8, (long)'" + lower  + "', (long)'\\u007F') )" );
			}
			
			decreaseIndent();
			addToCodeSection("\n" + indent + ")");
			
			
		}
		else
		{
			if( upperChar <= 0x007F )
			{
				addToCodeSection( "\n" + indent + "Hammer.intRange( uInt_8, (long)'" + lower  + "', (long)'" + upper + "')" );
			}
			else if( lowerChar > 0xc280 )
			{
				addToCodeSection( "\n" + indent + "Hammer.intRange( uInt_16, (long)'" + lower  + "', (long)'" + upper + "')" );
			}
			else
			{
				addToCodeSection("\n" + indent + "Hammer.choice(");
				increaseIndent();
				
				addToCodeSection( "\n" + indent + "Hammer.intRange( uInt_16, (long)'\\uc280', (long)'" + upper + "')," );
				addToCodeSection( "\n" + indent + "Hammer.intRange( uInt_8, (long)'" + lower  + "', (long)'\\u007F')" );
								
				decreaseIndent();
				addToCodeSection("\n" + indent + ")");
			}
		}
		
		
	}

	@Override
	public void visit(ASTLexChar ast)
	{
		String c = ast.getChar();
		char ch = StringEscapeUtils.unescapeJava(c).charAt(0);
		
		if( ast.isNegate() )
		{
			addToCodeSection("\n" + indent + "Hammer.choice(");
			increaseIndent();
			
			addToCodeSection( "\n" + indent + "Hammer.butNot( Hammer.intRange( uInt_16, (long)'\\uc280', (long)'\\udfba')"
											+ ", Hammer.intRange( uInt_16, (long)'" + c + "', (long)'" + c + "') )," );
			
			addToCodeSection( "\n" + indent + "Hammer.butNot( Hammer.intRange( uInt_8, (long)'\\u0000', (long)'\\u0079')"
											+ ", Hammer.intRange( uInt_8, (long)'" + c + "', (long)'" + c + "') )" );
			
			decreaseIndent();
			addToCodeSection("\n" + indent + ")");
		}
		else
		{
			addToCodeSection( "\n" 
							+ indent 
							+ "Hammer.intRange( "
							+ ((ch > 0x00FF) ? "uInt_16" : "uInt_8")
							+ ", (long)'" 
							+ c 
							+ "', (long)'" 
							+ c 
							+ "')" );
		}
	}
	
	@Override
	public void visit(ASTLexString ast) 
	{
		
		String name = StringEscapeUtils.unescapeJava(ast.getString());
		
		System.out.println(name);
		
		addToCodeSection("\n" + indent + "Hammer.sequence( ");
		increaseIndent();
		
		for( int i = 0; i < name.length(); i++ )
		{
			Character ch = name.charAt(i);
			String cStr = StringEscapeUtils.escapeJava(ch.toString());
			addToCodeSection( "\n" 
							+ indent 
							+ "Hammer.intRange( " 
							+ ((ch > 0x00FF) ? "uInt_16" : "uInt_8")
							+ ", (long)'" 
							+ cStr 
							+ "', (long)'" 
							+ cStr + "')");
			if( i < name.length()-1 )
			{
				addToCodeSection(", ");
			}
			else
			{
				addToCodeSection(" ");
			}
		}

		decreaseIndent();
		addToCodeSection("\n" + indent + ")");
	}
	
	@Override
	public void handle(ASTLexSimpleIteration ast)
	{
		printIteration(ast.getIteration()); 
		
		if (ast.getLexChar().isPresent()) {
			ast.getLexChar().get().accept(getRealThis());
	    }
	    else if (ast.getLexString().isPresent()) {
	    	ast.getLexString().get().accept(getRealThis());
	    } 
	    else if (ast.getLexNonTerminal().isPresent()) {
	    	ast.getLexNonTerminal().get().accept(getRealThis());
	    }
		
		printIterationEnd(ast.getIteration()); 
	}
	
	@Override
	public void visit(ASTLexActionOrPredicate ast) 
	{
		addToCodeSection("\n" + indent + "Hammer.epsilonP()");
	}
	
	@Override
	public void visit(ASTLexNonTerminal ast) 
	{		
		addToCodeSection("\n" + indent + "_" + ast.getName());
	}
	
	@Override
	public void visit(ASTLexOption ast) 
	{
		addToCodeSection("/*ASTLexOption*/");
	}
	
	@Override
	public void visit(ASTSemanticpredicateOrAction ast) 
	{
		addToCodeSection("\n" + indent + "Hammer.epsilonP()");
	}
	
	@Override
	public void visit(ASTNonTerminal ast) 
	{
		printIteration(ast.getIteration());
		
		addToCodeSection("\n" + indent + "_" + ast.getName());
		
		printIterationEnd(ast.getIteration());
	}
	
	@Override
	public void visit(ASTEof ast)
	{
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.sequence( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.choice( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.intRange( uInt_8, (byte)'\\n', (byte)'\\n'),");		
		addToCodeSection("\n" + indent + "Hammer.intRange( uInt_8, (byte)'\\r', (byte)'\\r')");
		
		decreaseIndent();
		addToCodeSection("\n" + indent + "),");
		
		addToCodeSection("\n" + indent + "Hammer.endP()");
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ")");
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actEOF\")");
	}
	  
	@Override
	public void visit(ASTAnything ast) 
	{
		addToCodeSection("/*ASTAnything*/");
	}
	  
	@Override
	public void visit(ASTMCAnything ast) 
	{	
		addToCodeSection("/*ASTMCAnything*/");
	}
	  
	@Override
	public void handle(ASTAlt alt) 
	{		
		addToCodeSection("\n" + indent + "Hammer.sequence( ");
		increaseIndent();
		
		java.util.List<de.monticore.grammar.grammar._ast.ASTRuleComponent> components = alt.getComponents();
		
		for( int i = 0; i < components.size(); i++ )
		{
			components.get(i).accept(getRealThis());
			
			if( i < components.size()-1 )
			{
				addToCodeSection(", ");
			}
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ") ");
	}
	
	@Override
	public void handle(ASTLexAlt alt)
	{
		addToCodeSection("\n" + indent + "Hammer.sequence( ");
		increaseIndent();
				
		java.util.List<de.monticore.grammar.grammar._ast.ASTLexComponent> components = alt.getLexComponents();
		
		for( int i = 0; i < components.size(); i++ )
		{
			components.get(i).accept(getRealThis());
			
			if( i < components.size()-1 )
			{
				addToCodeSection(", ");
			}
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ") ");
	}
	
	// ----------------- Binary Token Visitors -----------------------------------------------------
	
	@Override
	public void handle(ASTBinaryProd ast) 
	{
		startCodeSection("ASTBinaryProd");
		
		addToCodeSection(indent + "_" + ast.getName() + ".bindIndirect( ");
		increaseIndent();
		
		List<String> lengthFields = grammarAnalyzer.containsLengthFields(ast);
		if(!lengthFields.isEmpty())
		{
			for(String length : lengthFields)
			{
				addToCodeSection("\n" + indent + "Hammer.action( ");
				increaseIndent();
			}
		}
		
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.choice( ");
		increaseIndent();
		
		List<ASTBinaryAlt> alts = ast.getAlts();
		for( int i = 0; i < alts.size(); i++ )
		{
			ASTBinaryAlt alt = alts.get(i);
			alt.accept(getRealThis());
			
			if( i < alts.size()-1 )
			{
				addToCodeSection(", ");
			}
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ")");
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"act" + ast.getName() + "\" )");
		
		if(!lengthFields.isEmpty())
		{
			for(String length : lengthFields)
			{
				decreaseIndent();
				addToCodeSection("\n" + indent + ",\"length_" + length + "_Reset\")");
			}
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ");");
		
		endCodeSection();
	}
	
	@Override
	public void handle(ASTBinaryAlt alt)
	{
		addToCodeSection("\n" + indent + "Hammer.sequence( ");
		increaseIndent();
				
		java.util.List<de.monticore.grammar.grammar._ast.ASTBinaryComponent> components = alt.getBinaryComponents();
		
		for( int i = 0; i < components.size(); i++ )
		{
			components.get(i).accept(getRealThis());
			
			if( i < components.size()-1 )
			{
				addToCodeSection(", ");
			}
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ") ");
	}
	
	@Override
	public void handle(ASTBinaryBlock ast)
	{
		printIteration(ast.getIteration()); 
		
		if( ast.isNegate() )
		{
			negated = true;
			
			addToCodeSection("\n" + indent + "Hammer.action( ");
			increaseIndent();
			
			addToCodeSection("\n" + indent + "Hammer.sequence( ");
			increaseIndent();
			
			List<ASTBinaryAlt> alts = ast.getBinaryAlts();
			for( int i = 0; i < alts.size(); i++ )
			{
				if( i < alts.size()-1 )
				{
					addToCodeSection("\n" + indent + "Hammer.and( ");
					increaseIndent();
				}
				
				addToCodeSection("\n" + indent + "Hammer.action( ");
				increaseIndent();
				
				ASTBinaryAlt alt = alts.get(i);
				alt.accept(getRealThis());
				
				decreaseIndent();
				addToCodeSection("\n" + indent + ", \"actUndefined\" )");
				
				if( i < alts.size()-1 )
				{
					decreaseIndent();
					addToCodeSection("\n" + indent + "),");
				}
			}
			
			decreaseIndent();
			addToCodeSection("\n" + indent + ")");
			
			decreaseIndent();
			addToCodeSection("\n" + indent + ", \"actUndefined\" )");
			
			negated = false;
			
		}
		else
		{
			addToCodeSection("\n" + indent + "Hammer.choice( ");
			increaseIndent();
			
			List<ASTBinaryAlt> alts = ast.getBinaryAlts();
			for( int i = 0; i < alts.size(); i++ )
			{
				addToCodeSection("\n" + indent + "Hammer.action( ");
				increaseIndent();
				
				ASTBinaryAlt alt = alts.get(i);
				alt.accept(getRealThis());
				
				decreaseIndent();
				addToCodeSection("\n" + indent + ", \"actUndefined\" )");
				
				if( i < alts.size()-1 )
				{
					addToCodeSection(", ");
				}
			}
			
			decreaseIndent();
			addToCodeSection("\n" + indent + ")");
		}
		
		printIterationEnd(ast.getIteration()); 
	}
	
	@Override
	public void handle(ASTBinarySimpleIteration ast)
	{
		printIteration(ast.getIteration()); 
				
		if (ast.getBinaryNonTerminal().isPresent()) {
			ast.getBinaryNonTerminal().get().accept(getRealThis());
	    }
	    else if (ast.getUInt8().isPresent()) {
	    	ast.getUInt8().get().accept(getRealThis());
	    } 
	    else if (ast.getUInt16().isPresent()) {
	    	ast.getUInt16().get().accept(getRealThis());
	    } 
	    else if (ast.getUInt32().isPresent()) {
	    	ast.getUInt32().get().accept(getRealThis());
	    } 
	    else if (ast.getUInt64().isPresent()) {
	    	ast.getUInt64().get().accept(getRealThis());
	    } 
	    else if (ast.getInt8().isPresent()) {
	    	ast.getInt8().get().accept(getRealThis());
	    } 
	    else if (ast.getInt16().isPresent()) {
	    	ast.getInt16().get().accept(getRealThis());
	    } 
	    else if (ast.getInt32().isPresent()) {
	    	ast.getInt32().get().accept(getRealThis());
	    } 
	    else if (ast.getInt64().isPresent()) {
	    	ast.getInt64().get().accept(getRealThis());
	    } 
	    else if (ast.getUBits().isPresent()) {
	    	ast.getUBits().get().accept(getRealThis());
	    } 
	    else if (ast.getBits().isPresent()) {
	    	ast.getBits().get().accept(getRealThis());
	    } 
		
		printIterationEnd(ast.getIteration()); 
	}
	
	@Override
	public void visit(ASTBinaryNonTerminal ast) 
	{		
		addToCodeSection("\n" + indent + "_" + ast.getName());
	}
	
	@Override
	public void handle(ASTBinaryLengthValue ast) 
	{		
		addToCodeSection("\n" + indent + "Hammer.lengthValue( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		if (ast.getUInt8().isPresent()) {
	    	ast.getUInt8().get().accept(getRealThis());
	    } 
	    else if (ast.getUInt16().isPresent()) {
	    	ast.getUInt16().get().accept(getRealThis());
	    } 
	    else if (ast.getUInt32().isPresent()) {
	    	ast.getUInt32().get().accept(getRealThis());
	    } 
	    else if (ast.getUInt64().isPresent()) {
	    	ast.getUInt64().get().accept(getRealThis());
	    }
	    else if (ast.getUBits().isPresent()) {
	    	ast.getUBits().get().accept(getRealThis());
	    }
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actUInt\" )");
		
		addToCodeSection(",");
		ast.getRepeat().accept(getRealThis());		
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ") ");
	}
	
	@Override
	public void handle(ASTBinaryNRepeat ast) 
	{		
		addToCodeSection("\n" + indent + "Hammer.repeatN( ");
		increaseIndent();
		
		ast.getRepeat().accept(getRealThis());	
		addToCodeSection(",");
		addToCodeSection("\n" + indent + ast.getUIntValue().getValue());
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ") ");
	}
	
	@Override
	public void visit(ASTUInt8 uint8)
	{	
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		if( uint8.isRanged() )
		{
			if( negated ? !uint8.isNegate() : uint8.isNegate() )
			{
				if( uint8.getValueChar().isPresent() )
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( uInt_8, Hammer.intRange( uInt_8, (byte)'" + encodeChar(uint8.getValueChar().get().charAt(0)) + "', (byte)'" + encodeChar(uint8.getValueChar().get().charAt(0)) + "') )");
				}
				else if( uint8.getLowerChar().isPresent() && uint8.getUpperChar().isPresent() )
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( uInt_8, Hammer.intRange( uInt_8, (byte)'" + encodeChar(uint8.getLowerChar().get().charAt(0)) + "', (byte)'" + encodeChar(uint8.getUpperChar().get().charAt(0)) + "') )");
				}
				else if( uint8.getValueUInt().isPresent() )
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( uInt_8, Hammer.intRange( uInt_8, " + uint8.getValueUInt().get().getValue() + ", " + uint8.getValueUInt().get().getValue() + ") )");
				}
				else if( uint8.getLowerUInt().isPresent() && uint8.getUpperUInt().isPresent() )
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( uInt_8, Hammer.intRange( uInt_8, " + uint8.getLowerUInt().get().getValue() + ", " + uint8.getUpperUInt().get().getValue() + ") )");
				}
			}
			else
			{
				if( uint8.getValueChar().isPresent() )
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( uInt_8, (byte)'" + encodeChar(uint8.getValueChar().get().charAt(0)) + "', (byte)'" + encodeChar(uint8.getValueChar().get().charAt(0)) + "')");
				}
				else if( uint8.getLowerChar().isPresent() && uint8.getUpperChar().isPresent() )
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( uInt_8, (byte)'" + encodeChar(uint8.getLowerChar().get().charAt(0)) + "', (byte)'" + encodeChar(uint8.getUpperChar().get().charAt(0)) + "')");
				}
				else if( uint8.getValueUInt().isPresent() )
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( uInt_8, " + uint8.getValueUInt().get().getValue() + ", " + uint8.getValueUInt().get().getValue() + ")");
				}
				else if( uint8.getLowerUInt().isPresent() && uint8.getUpperUInt().isPresent() )
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( uInt_8, " + uint8.getLowerUInt().get().getValue() + ", " + uint8.getUpperUInt().get().getValue() + ")");
				}
			}
		}
		else
		{
			addToCodeSection("\n" + indent + "uInt_8");
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actUInt8\") ");
	}
	
	@Override
	public void visit(ASTUInt16 uint16)
	{
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		if( uint16.isValued() )
		{
			if( negated ? !uint16.isNegate() : uint16.isNegate() )
			{
				if( !uint16.isRanged() )
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( uInt_16, Hammer.intRange( uInt_16, " + uint16.getValue().get().getValue() + ", " + uint16.getValue().get().getValue() + ") )");
				}
				else
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( uInt_16, Hammer.intRange( uInt_16, " + uint16.getLower().get().getValue() + ", " + uint16.getUpper().get().getValue() + ") )");
				}
			}
			else
			{
				if( !uint16.isRanged() )
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( uInt_16, " + uint16.getValue().get().getValue() + ", " + uint16.getValue().get().getValue() + ")");
				}
				else
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( uInt_16, " + uint16.getLower().get().getValue() + ", " + uint16.getUpper().get().getValue() + ")");
				}
			}
		}
		else
		{
			addToCodeSection("\n" + indent + "uInt_16");
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actUInt16\") ");
	}
	
	@Override
	public void visit(ASTUInt32 uint32)
	{
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		if( uint32.isValued() )
		{
			if( negated ? !uint32.isNegate() : uint32.isNegate() )
			{
				if( !uint32.isRanged() )
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( uInt_32, Hammer.intRange( uInt_32, " + uint32.getValue().get().getValue() + ", " + uint32.getValue().get().getValue() + ") )");
				}
				else
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( uInt_32, Hammer.intRange( uInt_32, " + uint32.getLower().get().getValue() + ", " + uint32.getUpper().get().getValue() + ") )");
				}
			}
			else
			{
				if( !uint32.isRanged() )
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( uInt_32, " + uint32.getValue().get().getValue() + ", " + uint32.getValue().get().getValue() + ")");
				}
				else
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( uInt_32, " + uint32.getLower().get().getValue() + ", " + uint32.getUpper().get().getValue() + ")");
				}
			}	
		}
		else
		{
			addToCodeSection("\n" + indent + "uInt_32");
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actUInt32\") ");
	}
	
	@Override
	public void visit(ASTUInt64 uint64)
	{
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		if( uint64.isValued() )
		{
			if( negated ? !uint64.isNegate() : uint64.isNegate() )
			{
				if( !uint64.isRanged() )
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( uInt_64, Hammer.intRange( uInt_64, " + uint64.getValue().get().getValue() + ", " + uint64.getValue().get().getValue() + ") )");
				}
				else
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( uInt_64, Hammer.intRange( uInt_64, " + uint64.getLower().get().getValue() + ", " + uint64.getUpper().get().getValue() + ") )");
				}
			}
			else
			{
				if( !uint64.isRanged() )
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( uInt_64, " + uint64.getValue().get().getValue() + ", " + uint64.getValue().get().getValue() + ")");
				}
				else
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( uInt_64, " + uint64.getLower().get().getValue() + ", " + uint64.getUpper().get().getValue() + ")");
				}
			}
		}
		else
		{
			addToCodeSection("\n" + indent + "uInt_64");
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actUInt64\") ");
	}
	
	@Override
	public void visit(ASTInt8 int8)
	{
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		if( int8.isValued() )
		{
			if( negated ? !int8.isNegate() : int8.isNegate() )
			{
				if( !int8.isRanged() )
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( int_8, Hammer.intRange( int_8, " + int8.getValue().get().getValue() + ", " + int8.getValue().get().getValue() + ") )");
				}
				else
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( int_8, Hammer.intRange( int_8, " + int8.getLower().get().getValue() + ", " + int8.getUpper().get().getValue() + ") )");
				}
			}
			else
			{
				if( !int8.isRanged() )
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( int_8, " + int8.getValue().get().getValue() + ", " + int8.getValue().get().getValue() + ")");
				}
				else
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( int_8, " + int8.getLower().get().getValue() + ", " + int8.getUpper().get().getValue() + ")");
				}
			}	
		}
		else
		{
			addToCodeSection("\n" + indent + "int_8");
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actInt8\") ");
	}
	
	@Override
	public void visit(ASTInt16 int16)
	{
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();

		if( int16.isValued() )
		{
			if( negated ? !int16.isNegate() : int16.isNegate() )
			{
				if( !int16.isRanged() )
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( int_16, Hammer.intRange( int_16, " + int16.getValue().get().getValue() + ", " + int16.getValue().get().getValue() + ") )");
				}
				else
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( int_16, Hammer.intRange( int_16, " + int16.getLower().get().getValue() + ", " + int16.getUpper().get().getValue() + ") )");
				}
			}
			else
			{
				if( !int16.isRanged() )
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( int_16, " + int16.getValue().get().getValue() + ", " + int16.getValue().get().getValue() + ")");
				}
				else
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( int_16, " + int16.getLower().get().getValue() + ", " + int16.getUpper().get().getValue() + ")");
				}
			}
		}
		else
		{
			addToCodeSection("\n" + indent + "int_16");
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actInt16\") ");
	}
	
	@Override
	public void visit(ASTInt32 int32)
	{
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		if( int32.isValued() )
		{
			if( negated ? !int32.isNegate() : int32.isNegate() )
			{
				if( !int32.isRanged() )
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( int_32, Hammer.intRange( int_32, " + int32.getValue().get().getValue() + ", " + int32.getValue().get().getValue() + ") )");
				}
				else
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( int_32, Hammer.intRange( int_32, " + int32.getLower().get().getValue() + ", " + int32.getUpper().get().getValue() + ") )");
				}
			}
			else
			{

				if( !int32.isRanged() )
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( int_32, " + int32.getValue().get().getValue() + ", " + int32.getValue().get().getValue() + ")");
				}
				else
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( int_32, " + int32.getLower().get().getValue() + ", " + int32.getUpper().get().getValue() + ")");
				}
			}
		}
		else
		{
			addToCodeSection("\n" + indent + "int_32");
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actInt32\") ");
	}
	
	@Override
	public void visit(ASTInt64 int64)
	{
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		if( int64.isValued() )
		{
			if( negated ? !int64.isNegate() : int64.isNegate() )
			{
				if( !int64.isRanged() )
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( int_32, Hammer.intRange( int_64, " + int64.getValue().get().getValue() + ", " + int64.getValue().get().getValue() + ") )");
				}
				else
				{
					addToCodeSection("\n" + indent + "Hammer.butNot( int_32, Hammer.intRange( int_64, " + int64.getLower().get().getValue() + ", " + int64.getUpper().get().getValue() + ") )");
				}
			}
			else
			{
				if( !int64.isRanged() )
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( int_64, " + int64.getValue().get().getValue() + ", " + int64.getValue().get().getValue() + ")");
				}
				else
				{
					addToCodeSection("\n" + indent + "Hammer.intRange( int_64, " + int64.getLower().get().getValue() + ", " + int64.getUpper().get().getValue() + ")");
				}
			}
		}
		else
		{
			addToCodeSection("\n" + indent + "int_64");
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actInt64\") ");
	}
	
	@Override
	public void visit(ASTBits bits)
	{
		int numBits = bits.getBits()+1-ASTConstantsGrammar.CONSTANT0;
		
		
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		if( bits.isValued() )
		{
			if( !bits.isRanged() )
			{
				addToCodeSection("\n" + indent + "Hammer.intRange( Hammer.bits(" + numBits + ",true), " + bits.getValue().get().getValue() + ", " + bits.getValue().get().getValue() + ")");
			}
			else
			{
				addToCodeSection("\n" + indent + "Hammer.intRange( Hammer.bits(" + numBits + ",true), " + bits.getLower().get().getValue() + ", " + bits.getUpper().get().getValue() + ")");
			}
		}
		else
		{
			addToCodeSection("\n" + indent + "Hammer.bits(" + numBits + ",true)");
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actBits" + numBits + "\") ");
	}
	
	@Override
	public void visit(ASTUBits ubits)
	{
		int numBits = ubits.getBits()+1-ASTConstantsGrammar.CONSTANT0;
		
		
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		if( ubits.isValued() )
		{
			if( !ubits.isRanged() )
			{
				addToCodeSection("\n" + indent + "Hammer.intRange( Hammer.bits(" + numBits + ",false), " + ubits.getValue().get().getValue() + ", " + ubits.getValue().get().getValue() + ")");
			}
			else
			{
				addToCodeSection("\n" + indent + "Hammer.intRange( Hammer.bits(" + numBits + ",false), " + ubits.getLower().get().getValue() + ", " + ubits.getUpper().get().getValue() + ")");
			}
		}
		else
		{
			addToCodeSection("\n" + indent + "Hammer.bits(" + numBits + ",false)");
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actUBits" + numBits + "\") ");
	}
	
	@Override
	public void handle(ASTBinaryLength ast)
	{		
		String id = ast.getId();
				
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		if (ast.getUInt8().isPresent()) {
	    	ast.getUInt8().get().accept(getRealThis());
	    } 
	    else if (ast.getUInt16().isPresent()) {
	    	ast.getUInt16().get().accept(getRealThis());
	    } 
	    else if (ast.getUInt32().isPresent()) {
	    	ast.getUInt32().get().accept(getRealThis());
	    } 
	    else if (ast.getUInt64().isPresent()) {
	    	ast.getUInt64().get().accept(getRealThis());
	    }
	    else if (ast.getUBits().isPresent()) {
	    	ast.getUBits().get().accept(getRealThis());
	    }
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"length_" + id + "\" )");
	}
	
	@Override
	public void handle(ASTBinaryData ast)
	{		
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		ASTGrammarNode astNode = null;
		
		if (ast.getUInt8().isPresent()) {
			astNode = ast.getUInt8().get();
	    } 
	    else if (ast.getUInt16().isPresent()) {
	    	astNode = ast.getUInt16().get();
	    } 
	    else if (ast.getUInt32().isPresent()) {
	    	astNode = ast.getUInt32().get();
	    } 
	    else if (ast.getUInt64().isPresent()) {
	    	astNode = ast.getUInt64().get();
	    }
	    else if (ast.getUBits().isPresent()) {
	    	astNode = ast.getUBits().get();
	    }
	    else if (ast.getInt8().isPresent()) {
	    	astNode = ast.getInt8().get();
	    } 
	    else if (ast.getInt16().isPresent()) {
	    	astNode = ast.getInt16().get();
	    } 
	    else if (ast.getInt32().isPresent()) {
	    	astNode = ast.getInt32().get();
	    } 
	    else if (ast.getInt64().isPresent()) {
	    	astNode = ast.getInt64().get();
	    }
	    else if (ast.getBits().isPresent()) {
	    	astNode = ast.getBits().get();
	    }
	    else if (ast.getBinaryNonTerminal().isPresent()) {
	    	astNode = ast.getBinaryNonTerminal().get();
	    }
	    else {
	    	return ;
	    }
		
		String id = ast.getId();
		int i = 1;
		for( ASTGrammarNode node : dataFields.get(id) )
		{
			if(node.equals(astNode))
			{
				break;
			}
			i++;
		}
		addToCodeSection("\n" + indent + "dataField_" + id + "_" + i);
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"length_" + id + "_Data\" )");
	}
	
	@Override
	public void handle(ASTOffset ast)
	{
		addToCodeSection("\n" + indent + "Hammer.action( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.sequence( ");
		increaseIndent();
		
		if( ast.getUInt8().isPresent() )
		{
			ast.getUInt8().get().accept(getRealThis());
		}
		else if( ast.getUInt16().isPresent() )
		{
			ast.getUInt16().get().accept(getRealThis());
		}
		else if( ast.getUInt32().isPresent() )
		{
			ast.getUInt32().get().accept(getRealThis());
		}
		else if( ast.getUInt64().isPresent() )
		{
			ast.getUInt64().get().accept(getRealThis());
		}
		else if( ast.getUBits().isPresent() )
		{
			ast.getUBits().get().accept(getRealThis());
		}		
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ")");
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"act" + ast.getName() + "\" )");
	}

	// ----------------- End of visit methods ---------------------------------------------
	
	public List<String> createHammerCode(ASTProd ast)
	{		
		clearHammerCode();
		ast.accept(getRealThis());
		return getHammerCode();
	}
	
	public List<String> createHammerInterfaceCode(MCRuleSymbol ast)
	{
		clearHammerCode();
		
		startCodeSection("ASTInterfaceProd");
		
		addToCodeSection("\n" + indent + "_" + ast.getName() + ".bindIndirect( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.choice( ");
		increaseIndent();
		
		List<String> alts = interfaces.get(ast.getName());
		for(int i = 0; i < alts.size(); i++)
		{
			addToCodeSection("\n" + indent + "_" + alts.get(i));
			
			if( i < alts.size()-1 )
			{
				addToCodeSection(", ");
			}
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ")");
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ");");
		
		
		endCodeSection();
		
		return getHammerCode();
	}
	
	public List<String> createHammerDataFieldCode(String dataField)
	{
		clearHammerCode();
		
		startCodeSection("DataFields");
		
		int i = 1;
		for(ASTGrammarNode node : dataFields.get(dataField))
		{			
			addToCodeSection("\n" + indent + "dataField_" + dataField + "_" + i + ".bindIndirect( ");
			increaseIndent();
			
			addToCodeSection("\n" + indent + "Hammer.action( ");
			increaseIndent();
			
			addToCodeSection("\n" + indent + "Hammer.choice( ");
			increaseIndent();
			
			addToCodeSection("\n" + indent + "Hammer.sequence( ");
			increaseIndent();
			
			addToCodeSection("\n" + indent + "Hammer.action( ");
			increaseIndent();
			
			node.accept(getRealThis());
			
			decreaseIndent();
			addToCodeSection("\n" + indent + ", \"length_" + dataField + "_DataIter\" ),");
					
			addToCodeSection("\n" + indent + "dataField_" + dataField + "_" + i);
			
			decreaseIndent();
			addToCodeSection("\n" + indent + "),");
			
			addToCodeSection("\n" + indent + "Hammer.sequence( ");
			increaseIndent();
			
			node.accept(getRealThis());
			
			decreaseIndent();
			addToCodeSection("\n" + indent + ")");
			
			decreaseIndent();
			addToCodeSection("\n" + indent + ")");
			
			decreaseIndent();
			addToCodeSection("\n" + indent + ", \"actUndefined\" )");
			
			decreaseIndent();
			addToCodeSection("\n" + indent + ");");
			
			i++;
		}
		
		endCodeSection();
		
		return getHammerCode();
	}
	
	public static Grammar_WithConceptsPrettyPrinter getPrettyPrinter() {
		if (prettyPrinter == null) {
    		prettyPrinter = new Grammar_WithConceptsPrettyPrinter(new IndentPrinter());
    	}
    	return prettyPrinter;
	}
	
	public List<String> getRuleAction(ASTClassProd classProd)
	{
		List<String> actionCode = Lists.newArrayList();
		
		if( classProd.getAction().isPresent() )
		{			
			StringBuffer buffer = new StringBuffer();
		    for (ASTBlockStatement action: ((ASTAction) classProd.getAction().get()).getBlockStatements()) {
		    	buffer.append(getPrettyPrinter().prettyprint(action));
		    }
		    actionCode.add( buffer.toString() );
		}
		
		return actionCode;
	}
	
	public List<String> getBinaryAction(ASTBinaryProd binaryProd)
	{
		List<String> actionCode = Lists.newArrayList();
		
		if( binaryProd.getEndAction().isPresent() )
		{			
			StringBuffer buffer = new StringBuffer();
		    for (ASTBlockStatement action: ((ASTAction) binaryProd.getEndAction().get()).getBlockStatements()) {
		    	buffer.append(getPrettyPrinter().prettyprint(action));
		    }
		    actionCode.add( buffer.toString() );
		}
		
		return actionCode;
	}
	
	public List<String> getLexAction(ASTLexProd lexProd)
	{
		List<String> actionCode = Lists.newArrayList();
		
		if( lexProd.getEndAction().isPresent() )
		{			
			StringBuffer buffer = new StringBuffer();
		    for (ASTBlockStatement action: ((ASTAction) lexProd.getEndAction().get()).getBlockStatements()) {
		    	buffer.append(getPrettyPrinter().prettyprint(action));
		    }
		    actionCode.add( buffer.toString() );
		}
		
		return actionCode;
	}
	
	// ----------------------------------------------------------

	/**
	 * Gets the antlr code (for printing)
	 * 
	 * @return
	 */
	private List<String> getHammerCode()
	{
		return ImmutableList.copyOf(productionHammerCode);
	}
	
	/**
	 * Adds the given code to antlr code
	 * 
	 *@param code
	 */
	private void addToHammerCode(String code) 
	{
		productionHammerCode.add(code);
	}
	
	/**
	 * Adds the given code to antlr code
	 * 
	 * @param code
	 */
	private void addToHammerCode(StringBuilder code) 
	{
	    addToHammerCode(code.toString());
	}
	
	/**
	 * Clears antlr code
	 */
	private void clearHammerCode() 
	{
		resetIndent();
		productionHammerCode.clear();
	}
	
	/**
	 * Starts codeSection of the parser code
	 */
	private void startCodeSection() 
	{
		codeSection = new StringBuilder();
	}
	  
	/**
	 * Adds the current code codeSection to antlr
	 */
	private void endCodeSection() 
	{
		addToHammerCode(codeSection);
		codeSection = new StringBuilder();
	}
	  
	/**
	 * Starts antlr code for the given production
	 * 
	 * @param ast
	 */
	private void startCodeSection(ASTNode ast) 
	{
		startCodeSection(ast.getClass().getSimpleName());
	}
	  
	/**
	 * Starts antlr code for the production with the given name
	 */
	private void startCodeSection(String text) 
	{
		codeSection = new StringBuilder("\n // Start of '" + text + "'\n");
	}
	  
	/**
	 * Ends antlr code for the given production
	 *	 
	 * @param ast
	 */
	private void endCodeSection(ASTNode ast) 
	{
		codeSection.append("// End of '" + ast.getClass().getSimpleName() + "'\n");
		endCodeSection();
	}
	  
	/**
	 * Adds the given code to the current codeSection
	 */
	private void addToCodeSection(String... code) 
	{
		Arrays.asList(code).forEach(s -> codeSection.append(s));
	}
	  
	/**
	 * @return codeSection
	 */
	public StringBuilder getCodeSection() 
	{
		return this.codeSection;
	}
	
	private void increaseIndent()
	{
		indent += "  ";
	}
	
	private void decreaseIndent()
	{
		indent = indent.substring(0,indent.length()-2);
	}
	
	private void resetIndent()
	{
		indent = "\t\t";
	}
	
	public static void addInterface(String interfaceName)
	{
		interfaces.put(interfaceName, Lists.newArrayList());
	}
	
	private String encodeChar(char c)
	{
		switch(c)
		{
		case '\\': return "\\\\";
		case '\t': return "\\t";
		case '\r': return "\\r";
		case '\n': return "\\n";
		case '\b': return "\\b";
		case '\f': return "\\f";
		case '\'': return "\\\'";
		case '\"': return "\\\"";
		default: return "" + c;
		}
	}
	
	private String decodeString(String str)
	{
		str.replace("\\\\", "\\");
		str.replace("\\t", "\t");
		str.replace("\\r", "\r");
		str.replace("\\n", "\n");
		str.replace("\\b", "\b");
		str.replace("\\f", "\f");
		str.replace("\\\'", "\'");
		str.replace("\\\"", "\"");
				
		return str;
	}
	
	public static List<String> getLengthFields()
	{
		List<String> list = new ArrayList<String>();
		list.addAll(lengthFields);
		return list;
	}
	
	public static List<String> getDataFields()
	{
		List<String> list = new ArrayList<String>();
		list.addAll(dataFields.keySet());
		return list;
	}
	
	public static List<String> getDataFieldIndirects()
	{
		List<String> list = new ArrayList<String>();
		
		for( String dataField : dataFields.keySet() )
		{
			Set<ASTGrammarNode> grammarNodes = dataFields.get(dataField);
			for(int i = 0; i < grammarNodes.size(); i++)
			{
				list.add(dataField + "_" + (i+1) );
			}
		}
		
		return list;
	}
}