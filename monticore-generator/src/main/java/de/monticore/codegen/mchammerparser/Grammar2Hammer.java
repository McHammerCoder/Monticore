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
import de.monticore.codegen.GeneratorHelper;
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
import de.monticore.grammar.grammar._ast.ASTOffsetProd;
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
 * Class used in the templates to generate code
 *
 * @author  (last commit) $Author$
 * @version $Revision$, $Date$
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
			
	private Set<String> lexStrings = Sets.newHashSet();
	
	private GrammarAnalyzer grammarAnalyzer = new GrammarAnalyzer();
	
	private boolean defaultLittleEndian = false;
	
	public Grammar2Hammer(McHammerParserGeneratorHelper parserGeneratorHelper, MCGrammarInfo grammarInfo) 
	{
		Preconditions.checkArgument(parserGeneratorHelper.getGrammarSymbol() != null);
		this.parserGeneratorHelper = parserGeneratorHelper;
		this.grammarEntry = parserGeneratorHelper.getGrammarSymbol();
		this.grammarInfo = grammarInfo;
		this.defaultLittleEndian = parserGeneratorHelper.defaultLittleEndian();
		
		// Find all DataFields and LexStrings in the grammar
		List<ASTProd> rules = parserGeneratorHelper.getParserRulesToGenerate();
		rules.addAll( parserGeneratorHelper.getBinaryRulesToGenerate() );
		rules.addAll( parserGeneratorHelper.getLexerRulesToGenerate() );
		
		for( ASTProd rule : rules )
		{			
			dataFields.putAll(grammarAnalyzer.containsDataFields(rule));
			lengthFields.addAll(grammarAnalyzer.containsLengthFields(rule));
			lexStrings.addAll(grammarAnalyzer.containsLexStrings(rule));
		}
	}
	
	// ----------------- Parser Rule Visitors -----------------------------------------------------

	@Override
	public void handle(ASTClassProd ast)
	{
		startCodeSection("ASTClassProd");
		addToCodeSection(indent + "_" + ast.getName() + ".bindIndirect( ");
		increaseIndent();
		
		List<String> lengthFields = grammarAnalyzer.containsLengthFields(ast);
		if(!lengthFields.isEmpty())
		{
			for(String length : lengthFields)
			{
				addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
				increaseIndent();
			}
		}
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
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
		String name = StringEscapeUtils.unescapeJava(ast.getName());
				
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.sequence( ");
		increaseIndent();
		
		for( int i = 0; i < name.length(); i++ )
		{
			String c = StringEscapeUtils.escapeJava(Character.toString(name.charAt(i)));
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
		List<String> terminals = getLexStrings();
		for( ; id < terminals.size(); id++ )
		{
			if(terminals.get(id).equals(name))
			{
				break;
			}	
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actTT_" + (id+1) + "\" )");
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
			addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
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
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.sequence( ");
		increaseIndent();
		
		for( int i = 0; i < name.length(); i++ )
		{
			char c = name.charAt(i);
			String ch = StringEscapeUtils.escapeJava(Character.toString(c));
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
		List<String> terminals = getLexStrings();
		for( ; id < terminals.size(); id++ )
		{
			String terminal = StringEscapeUtils.unescapeJava(terminals.get(id));
			if(terminal.equals(name))
			{
				break;
			}	
		}
		
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actTT_" + (id+1) + "\" )");
		
		printIterationEnd(ast.getIteration());
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
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
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
	
	// ----------------- Lexer Rule Visitors -----------------------------------------------------
	
	private boolean negated = false;
	
	@Override
	public void handle(ASTLexProd ast) 
	{
		startCodeSection("ASTLexProd");
		
		addToCodeSection(indent + "_" + ast.getName() + ".bindIndirect( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
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
			
			addToCodeSection("\n" + indent + "Hammer.intRange( uInt_8, (long)'\\u0000', (long)'\\u007F')," );
			addToCodeSection("\n" + indent + "Hammer.intRange( uInt_16, (long)'\\uc280', (long)'\\udfba')" );
			
			decreaseIndent();
			addToCodeSection("\n" + indent + "),");
			
			addToCodeSection("\n" + indent + "Hammer.choice(");
			increaseIndent();
			
			List<ASTLexAlt> alts = ast.getLexAlts();
			for( int i = 0; i < alts.size(); i++ )
			{			
				ASTLexAlt alt = alts.get(i);
				List<ASTLexComponent> components = alt.getLexComponents();
				
				components.get(0).accept(getRealThis());
				
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
				addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
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
				addToCodeSection("\n" + indent + "Hammer.butNot( Hammer.intRange( uInt_8, (long)'\\u0000', (long)'\\u007F'), Hammer.intRange( uInt_8, (long)'" + lower  + "', (long)'" + upper + "') )," );
				addToCodeSection("\n" + indent + "Hammer.intRange( uInt_16, (long)'\\uc280', (long)'\\udfba')" );
			}
			else if( lowerChar > 0xc280 )
			{
				addToCodeSection("\n" + indent + "Hammer.intRange( uInt_8, (long)'\\u0000', (long)'\\u007F')," );
				addToCodeSection("\n" + indent + "Hammer.butNot( Hammer.intRange( uInt_16, (long)'\\uc280', (long)'\\udfba'), Hammer.intRange( uInt_16, (long)'" + lower  + "', (long)'" + upper + "') )" );
			}
			else
			{
				addToCodeSection("\n" + indent + "Hammer.butNot( Hammer.intRange( uInt_16, (long)'\\uc280', (long)'\\udfba'), Hammer.intRange( uInt_16, (long)'\\uc280', (long)'" + upper + "') )," );
				addToCodeSection("\n" + indent + "Hammer.butNot( Hammer.intRange( uInt_8, (long)'\\u0000', (long)'\\u007F'), Hammer.intRange( uInt_8, (long)'" + lower  + "', (long)'\\u007F') )" );
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
			
			addToCodeSection( "\n" + indent + "Hammer.butNot( Hammer.intRange( uInt_8, (long)'\\u0000', (long)'\\u007F')"
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
		
		int id = 0;
		List<String> terminals = getLexStrings();
		for( ; id < terminals.size(); id++ )
		{
			String terminal = StringEscapeUtils.unescapeJava(terminals.get(id));
			if(terminal.equals(name))
			{
				break;
			}	
		}
		
		if( id < terminals.size() )
		{
			addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
			increaseIndent();
		}
		
		addToCodeSection("\n" + indent + "Hammer.sequence( ");
		increaseIndent();
		
		for( int i = 0; i < name.length(); i++ )
		{
			char ch = name.charAt(i);
			String cStr = StringEscapeUtils.escapeJava(Character.toString(ch));
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
		
		if( id < terminals.size() )
		{
			decreaseIndent();
			addToCodeSection("\n" + indent + ", \"actTT_" + (id+1) + "\" )");
		}
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
	
	// ----------------- Binary Rule Visitors -----------------------------------------------------
	
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
				addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
				increaseIndent();
			}
		}
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
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
		
		
		addToCodeSection("\n" + indent + "Hammer.choice( ");
		increaseIndent();
		
		List<ASTBinaryAlt> alts = ast.getBinaryAlts();
		for( int i = 0; i < alts.size(); i++ )
		{
			addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
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
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
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
	
	public void printIntParserRanged(int numBits, long lower, long upper, boolean signed, boolean little, boolean negated)
	{
		if( negated )
		{
			addToCodeSection("\n" + indent + "Hammer.butNot( ");
			increaseIndent();
			
			printIntParser(numBits, signed, little);
					
			addToCodeSection(",");
		}
		
		addToCodeSection("\n" + indent + "Hammer.intRange( ");
		increaseIndent();
		
		printIntParser(numBits, signed, little);
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", " + lower + ", " + upper + ")" );
		
		if( negated )
		{
			decreaseIndent();
			addToCodeSection("\n" + indent + ")" );			
		}
	}
	
	public void printIntParserRanged(int numBits, String lower, String upper, boolean signed, boolean little, boolean negated)
	{
		if( negated )
		{
			addToCodeSection("\n" + indent + "Hammer.butNot( ");
			increaseIndent();
			
			printIntParser(numBits, signed, little);
					
			addToCodeSection(",");
		}
		
		addToCodeSection("\n" + indent + "Hammer.intRange( ");
		increaseIndent();
		
		printIntParser(numBits, signed, little);
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", (byte)'" + lower + "', (byte)'" + upper + "')" );
		
		if( negated )
		{
			decreaseIndent();
			addToCodeSection("\n" + indent + ")" );			
		}
	}
	
	public void printIntParser(int numBits, boolean signed, boolean little)
	{
		if( little )
		{
			addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
			increaseIndent();
		}
		
		addToCodeSection("\n" + indent + (signed ? "int_" : "uInt_") + numBits );
		
		if( little )
		{
			decreaseIndent();
			addToCodeSection("\n" + indent + (signed ? ", \"actToBigS" : ", \"actToBigU") + numBits + "\") ");
		}			
	}
	
	
	@Override
	public void visit(ASTUInt8 uint8)
	{	
		boolean littleEndian = uint8.isLittle() || (defaultLittleEndian && !uint8.isBig());
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
		increaseIndent();
		
		if( uint8.isValued() )
		{
			boolean neg = uint8.isNegate();
			
			if( uint8.getValueChar().isPresent() )
			{
				String value = StringEscapeUtils.escapeJava(Character.toString(uint8.getValueChar().get().charAt(0)));
				printIntParserRanged(8, value, value, false, uint8.isLittle(), neg);
			}
			else if( uint8.getLowerChar().isPresent() && uint8.getUpperChar().isPresent() )
			{
				String lower = StringEscapeUtils.escapeJava(Character.toString(uint8.getLowerChar().get().charAt(0)));
				String upper = StringEscapeUtils.escapeJava(Character.toString(uint8.getUpperChar().get().charAt(0)));
				printIntParserRanged(8, lower, upper, false, uint8.isLittle(), neg);
			}
			else if( uint8.getValueUInt().isPresent() )
			{
				long value = uint8.getValueUInt().get().getValue();
				
				if( value > 255 )
					Log.error("Value of uint8 " + value + " too big!");
				
				printIntParserRanged(8, value, value, false, littleEndian, neg);
			}
			else if( uint8.getLowerUInt().isPresent() && uint8.getUpperUInt().isPresent() )
			{
				long lower = uint8.getLowerUInt().get().getValue();
				long upper = uint8.getUpperUInt().get().getValue();
				
				if( lower > upper )
					Log.error("Lower value of uint8 range is greater than upper value!");			
				if( lower > 255 )
					Log.error("Lower value of uint8 range " + lower + " too big!");
				if( upper > 255 )
					Log.error("Upper value of uint8 range " + upper + " too big!");
				
				printIntParserRanged(8, lower, upper, false, littleEndian, neg);
			}
		}
		else
		{
			printIntParser(8, false, littleEndian);
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actUInt8\") ");
	}
	
	@Override
	public void visit(ASTUInt16 uint16)
	{
		boolean littleEndian = uint16.isLittle() || (defaultLittleEndian && !uint16.isBig());
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
		increaseIndent();

		if( uint16.isValued() )
		{
			boolean neg = uint16.isNegate();
			
			if( uint16.getValue().isPresent() )
			{
				long value = uint16.getValue().get().getValue();
				
				if( value > 65535 )
					Log.error("Value of uint16 " + value + " too big!");
				
				printIntParserRanged(16, value, value, false, littleEndian, neg);
			}
			else if( uint16.getLower().isPresent() && uint16.getUpper().isPresent() )
			{
				long lower = uint16.getLower().get().getValue();
				long upper = uint16.getUpper().get().getValue();
				
				if( lower > upper )
					Log.error("Lower value of uint16 range is greater than upper value!");			
				if( lower > 65535 )
					Log.error("Lower value of uint16 range " + lower + " too big!");
				if( upper > 65535 )
					Log.error("Upper value of uint16 range " + upper + " too big!");
				
				printIntParserRanged(16, lower, upper, false, littleEndian, neg);
			}
		}
		else
		{
			printIntParser(16, false, littleEndian);
		}

		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actUInt16\") ");
	}
	
	@Override
	public void visit(ASTUInt32 uint32)
	{
		boolean littleEndian = uint32.isLittle() || (defaultLittleEndian && !uint32.isBig());
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
		increaseIndent();

		if( uint32.isValued() )
		{
			boolean neg = uint32.isNegate();
			
			if( uint32.getValue().isPresent() )
			{
				long value = uint32.getValue().get().getValue();
				
				if( value > 4294967295L )
					Log.error("Value of uint32 " + value + " too big!");
				
				printIntParserRanged(32, value, value, false, littleEndian, neg);
			}
			else if( uint32.getLower().isPresent() && uint32.getUpper().isPresent() )
			{
				long lower = uint32.getLower().get().getValue();
				long upper = uint32.getUpper().get().getValue();
				
				if( lower > upper )
					Log.error("Lower value of uint32 range is greater than upper value!");			
				if( lower > 4294967295L )
					Log.error("Lower value of uint32 range " + lower + " too big!");
				if( upper > 4294967295L )
					Log.error("Upper value of uint32 range " + upper + " too big!");
				
				printIntParserRanged(32, lower, upper, false, littleEndian, neg);
			}
		}
		else
		{
			printIntParser(32, false, littleEndian);
		}

		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actUInt32\") ");
	}
	
	@Override
	public void visit(ASTUInt64 uint64)
	{
		boolean littleEndian = uint64.isLittle() || (defaultLittleEndian && !uint64.isBig());
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
		increaseIndent();

		if( uint64.isValued() )
		{
			boolean neg = uint64.isNegate();
			
			if( uint64.getValue().isPresent() )
			{
				long value = uint64.getValue().get().getValue();
				
				if( value > Long.MAX_VALUE )
					Log.error("Value of uint64 " + value + " too big!");
				
				printIntParserRanged(64, value, value, false, littleEndian, neg);
			}
			else if( uint64.getLower().isPresent() && uint64.getUpper().isPresent() )
			{
				long lower = uint64.getLower().get().getValue();
				long upper = uint64.getUpper().get().getValue();
				
				if( lower > upper )
					Log.error("Lower value of uint64 range is greater than upper value!");			
				if( lower > Long.MAX_VALUE )
					Log.error("Lower value of uint64 range " + lower + " too big!");
				if( upper > Long.MAX_VALUE )
					Log.error("Upper value of uint64 range " + upper + " too big!");
				
				printIntParserRanged(64, lower, upper, false, littleEndian, neg);
			}
		}
		else
		{
			printIntParser(64, false, littleEndian);
		}

		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actUInt64\") ");
	}
	
	@Override
	public void visit(ASTInt8 int8)
	{
		boolean littleEndian = int8.isLittle() || (defaultLittleEndian && !int8.isBig());
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
		increaseIndent();

		if( int8.isValued() )
		{
			boolean neg = int8.isNegate();
			
			if( int8.getValue().isPresent() )
			{
				long value = int8.getValue().get().getValue();
				
				if( value > Byte.MAX_VALUE )
					Log.error("Value of int8 " + value + " too big!");
				
				if( value < Byte.MIN_VALUE )
					Log.error("Value of int8 " + value + " too small!");
				
				printIntParserRanged(8, value, value, false, littleEndian, neg);
			}
			else if( int8.getLower().isPresent() && int8.getUpper().isPresent() )
			{
				long lower = int8.getLower().get().getValue();
				long upper = int8.getUpper().get().getValue();
				
				if( lower > upper )
					Log.error("Lower value of int8 range is greater than upper value!");			
				
				if( lower > Byte.MAX_VALUE )
					Log.error("Lower value of int8 range " + lower + " too big!");
				if( upper > Byte.MAX_VALUE )
					Log.error("Upper value of int8 range " + upper + " too big!");
				
				if( lower < Byte.MIN_VALUE )
					Log.error("Lower value of int8 range " + lower + " too small!");
				if( upper < Byte.MIN_VALUE )
					Log.error("Upper value of int8 range " + upper + " too small!");	
				
				printIntParserRanged(8, lower, upper, false, littleEndian, neg);
			}
		}
		else
		{
			printIntParser(8, false, littleEndian);
		}

		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actInt8\") ");
	}
	
	@Override
	public void visit(ASTInt16 int16)
	{
		boolean littleEndian = int16.isLittle() || (defaultLittleEndian && !int16.isBig());
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
		increaseIndent();

		if( int16.isValued() )
		{
			boolean neg = int16.isNegate();
			
			if( int16.getValue().isPresent() )
			{
				long value = int16.getValue().get().getValue();
				
				if( value > Character.MAX_VALUE )
					Log.error("Value of int16 " + value + " too big!");
				
				if( value < Character.MIN_VALUE )
					Log.error("Value of int16 " + value + " too small!");
				
				printIntParserRanged(16, value, value, false, littleEndian, neg);
			}
			else if( int16.getLower().isPresent() && int16.getUpper().isPresent() )
			{
				long lower = int16.getLower().get().getValue();
				long upper = int16.getUpper().get().getValue();
				
				if( lower > upper )
					Log.error("Lower value of int16 range is greater than upper value!");			
				
				if( lower > Character.MAX_VALUE )
					Log.error("Lower value of int16 range " + lower + " too big!");
				if( upper > Character.MAX_VALUE )
					Log.error("Upper value of int16 range " + upper + " too big!");
				
				if( lower < Character.MIN_VALUE )
					Log.error("Lower value of int16 range " + lower + " too small!");
				if( upper < Character.MIN_VALUE )
					Log.error("Upper value of int16 range " + upper + " too small!");	
				
				printIntParserRanged(16, lower, upper, false, littleEndian, neg);
			}
		}
		else
		{
			printIntParser(16, false, littleEndian);
		}

		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actInt16\") ");
	}
	
	@Override
	public void visit(ASTInt32 int32)
	{
		boolean littleEndian = int32.isLittle() || (defaultLittleEndian && !int32.isBig());
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
		increaseIndent();

		if( int32.isValued() )
		{
			boolean neg = int32.isNegate();
			
			if( int32.getValue().isPresent() )
			{
				long value = int32.getValue().get().getValue();
				
				if( value > Integer.MAX_VALUE )
					Log.error("Value of int32 " + value + " too big!");
				
				if( value < Integer.MIN_VALUE )
					Log.error("Value of int32 " + value + " too small!");
				
				printIntParserRanged(32, value, value, false, littleEndian, neg);
			}
			else if( int32.getLower().isPresent() && int32.getUpper().isPresent() )
			{
				long lower = int32.getLower().get().getValue();
				long upper = int32.getUpper().get().getValue();
				
				if( lower > upper )
					Log.error("Lower value of int32 range is greater than upper value!");			
				
				if( lower > Integer.MAX_VALUE )
					Log.error("Lower value of int32 range " + lower + " too big!");
				if( upper > Integer.MAX_VALUE )
					Log.error("Upper value of int32 range " + upper + " too big!");
				
				if( lower < Integer.MIN_VALUE )
					Log.error("Lower value of int32 range " + lower + " too small!");
				if( upper < Integer.MIN_VALUE )
					Log.error("Upper value of int32 range " + upper + " too small!");	
				
				printIntParserRanged(32, lower, upper, false, littleEndian, neg);
			}
		}
		else
		{
			printIntParser(32, false, littleEndian);
		}

		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actInt32\") ");
	}
	
	@Override
	public void visit(ASTInt64 int64)
	{
		boolean littleEndian = int64.isLittle() || (defaultLittleEndian && !int64.isBig());
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
		increaseIndent();

		if( int64.isValued() )
		{
			boolean neg = int64.isNegate();
			
			if( int64.getValue().isPresent() )
			{
				long value = int64.getValue().get().getValue();
				
				if( value > Long.MAX_VALUE )
					Log.error("Value of int64 " + value + " too big!");
				
				if( value < Long.MIN_VALUE )
					Log.error("Value of int64 " + value + " too small!");
				
				printIntParserRanged(64, value, value, false, littleEndian, neg);
			}
			else if( int64.getLower().isPresent() && int64.getUpper().isPresent() )
			{
				long lower = int64.getLower().get().getValue();
				long upper = int64.getUpper().get().getValue();
				
				if( lower > upper )
					Log.error("Lower value of int64 range is greater than upper value!");			
				
				if( lower > Long.MAX_VALUE )
					Log.error("Lower value of int64 range " + lower + " too big!");
				if( upper > Long.MAX_VALUE )
					Log.error("Upper value of int64 range " + upper + " too big!");
				
				if( lower < Long.MIN_VALUE )
					Log.error("Lower value of int64 range " + lower + " too small!");
				if( upper < Long.MIN_VALUE )
					Log.error("Upper value of int64 range " + upper + " too small!");
				
				printIntParserRanged(64, lower, upper, false, littleEndian, neg);
			}
		}
		else
		{
			printIntParser(64, false, littleEndian);
		}

		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actInt64\") ");
	}
	
	public void printBitsRange( int numBits, long lower, long upper, boolean signed, boolean little)
	{
		addToCodeSection("\n" + indent + "Hammer.intRange("); 
		increaseIndent();
		
		printBits( numBits, signed, little );
		
		decreaseIndent();
		addToCodeSection("\n" + indent + "," + lower + ", " + upper + ")");
	}
	
	public void printBits( int numBits, boolean signed, boolean little )
	{
		if( little )
		{
			addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action(");
			increaseIndent();
		}
		
		addToCodeSection("\n" + indent + "Hammer.bits(" + numBits + (signed ? ",true)" : ",false)"));
				
		if( little )
		{
			decreaseIndent();
			addToCodeSection("\n" + indent + (signed ? ", \"actToBigS" : ", \"actToBigU") + numBits + "\") ");
		}
	}
	
	@Override
	public void visit(ASTBits bits)
	{
		int numBits = bits.getBits()+1-ASTConstantsGrammar.CONSTANT66;
		
		boolean littleEndian = bits.isLittle() || (defaultLittleEndian && !bits.isBig());
		
		if( bits.isLittle() )
		{
			addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
			increaseIndent();
		
			addToCodeSection("\n" + indent + "Hammer.sequence(");
			increaseIndent();
		}
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
		increaseIndent();
		
		if( bits.isValued() )
		{
			if( !bits.isRanged() )
			{
				long value = bits.getValue().get().getValue();
				
				printBitsRange(numBits,value,value,true, littleEndian);
			}
			else
			{
				long lower = bits.getLower().get().getValue();
				long upper = bits.getUpper().get().getValue();
				
				printBitsRange(numBits,lower,upper,true, littleEndian);
			}
		}
		else
		{
			printBits( numBits, true, littleEndian );
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actBits" + numBits + "\") ");
		
		if( bits.isLittle() )
		{
			decreaseIndent();
			addToCodeSection("\n" + indent + ")");
		
			decreaseIndent();
			addToCodeSection("\n" + indent + ", \"actLittle\") ");
		}
	}
	
	@Override
	public void visit(ASTUBits ubits)
	{
		int numBits = ubits.getBits()+1-ASTConstantsGrammar.CONSTANT2;
		
		boolean littleEndian = ubits.isLittle() || (defaultLittleEndian && !ubits.isBig());
		
		if( ubits.isLittle() )
		{
			addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
			increaseIndent();
		
			addToCodeSection("\n" + indent + "Hammer.sequence(");
			increaseIndent();
		}
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
		increaseIndent();
		
		if( ubits.isValued() )
		{
			if( !ubits.isRanged() )
			{
				long value = ubits.getValue().get().getValue();
				
				printBitsRange(numBits,value,value,false,littleEndian);
			}
			else
			{
				long lower = ubits.getLower().get().getValue();
				long upper = ubits.getUpper().get().getValue();
				
				printBitsRange(numBits,lower,upper,false,littleEndian);
			}
		}
		else
		{
			printBits( numBits, false, littleEndian );
		}
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actUBits" + numBits + "\") ");
		
		if( ubits.isLittle() )
		{
			decreaseIndent();
			addToCodeSection("\n" + indent + ")");
		
			decreaseIndent();
			addToCodeSection("\n" + indent + ", \"actLittle\") ");
		}
	}
	
	@Override
	public void handle(ASTBinaryLength ast)
	{		
		String id = ast.getId();
				
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
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
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.choice( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "Hammer.sequence() ");
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"length_" + id + "_Zero\" )");
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"actUndefined\" ),");
		
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
		increaseIndent();
		
		addToCodeSection("\n" + indent + "dataField_" + id + "_" + i);
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ", \"length_" + id + "_Data\" )");
		
		decreaseIndent();
		addToCodeSection("\n" + indent + ")");
	}
	
	@Override
	public void handle(ASTOffset ast)
	{
		addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
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
	
	/**
	 * Creates the Hammer parser code for a rule
	 * 
	 * @param ast Rule for which to create the parser code
	 * @return Hammer parser code for the given rule
	 */
	public List<String> createHammerCode(ASTProd ast)
	{		
		clearHammerCode();
		ast.accept(getRealThis());
		return getHammerCode();
	}
	
	/**
	 * Creates the Hammer parser code for an interface
	 * 
	 * @param ast Interface for which to create the parser code
	 * @return Hammer parser code for the given interface
	 */
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
	
	/**
	 * Creates the code for the offset calculation based on the parsed value
	 * 
	 * @param ast Offset for which to create the offset calculation code
	 * @return Offset calculation code
	 */
	public String createOffsetLinearMethodCode(ASTOffsetProd prod)
	{
		if(prod.isLocal())
		{
			return (prod.getA().isNegative() ? "offsetToken.getPosition()-" : "offsetToken.getPosition()+")
					 + "offsetToken.getValue().getValue()*" 
					 + prod.getA().getValue() * (prod.getA().isNegative() ? (-1) : 1 )
					 + ((prod.getSign() == ASTConstantsGrammar.PLUS) ? "+" : "-")
					 + prod.getB().getValue();
		}
		
		return (prod.getA().isNegative() ? "(bytes.length-1)*8-" : "")
			 + "offsetToken.getValue().getValue()*" 
			 + prod.getA().getValue() * (prod.getA().isNegative() ? (-1) : 1 )
			 + ((prod.getSign() == ASTConstantsGrammar.PLUS) ? "+" : "-")
			 + prod.getB().getValue();

	}
	
	/**
	 * Creates the Hammer code for the data fields
	 * 
	 * @param dataField Name of the data field
	 * @return Hammer data field code
	 */
	public List<String> createHammerDataFieldCode(String dataField)
	{
		clearHammerCode();
		
		startCodeSection("DataFields");
		
		int i = 1;
		for(ASTGrammarNode node : dataFields.get(dataField))
		{			
			addToCodeSection("\n" + indent + "dataField_" + dataField + "_" + i + ".bindIndirect( ");
			increaseIndent();
			
			addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
			increaseIndent();
			
			addToCodeSection("\n" + indent + "Hammer.choice( ");
			increaseIndent();
			
			addToCodeSection("\n" + indent + "Hammer.sequence( ");
			increaseIndent();
			
			addToCodeSection("\n" + indent + grammarEntry.getName() + "Hammer.action( ");
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
	
	/**
	 * @param ast Class Rule
	 * @return action code from grammar for given class rule
	 */
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
	
	/**
	 * @param ast Binary Rule
	 * @return action code from grammar for given binary rule
	 */
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
	
	/**
	 * @param ast Lexer Rule
	 * @return action code from grammar for given lexer rule
	 */
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
			addToCodeSection( "\n" + indent + grammarEntry.getName() + "Hammer.action( " );
			increaseIndent();
			addToCodeSection( "\n" + indent + "Hammer.many1( " );
			increaseIndent();
			break;
		case ASTConstantsGrammar.STAR:
			addToCodeSection( "\n" + indent + grammarEntry.getName() + "Hammer.action( " );
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
	
	/**
	 * Gets PrettyPrinter for ASTGrammar
	 * 
	 * @return
	 */
	public static Grammar_WithConceptsPrettyPrinter getPrettyPrinter() {
		if (prettyPrinter == null) {
    		prettyPrinter = new Grammar_WithConceptsPrettyPrinter(new IndentPrinter());
    	}
    	return prettyPrinter;
	}
	
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
	
	/**
	 * Increases the indent of the generated code
	 */
	private void increaseIndent()
	{
		indent += "  ";
	}
	
	/**
	 * Decreases the indent of the generated code
	 */
	private void decreaseIndent()
	{
		indent = indent.substring(0,indent.length()-2);
	}
	
	/**
	 * Resets the indent of the generated code to default
	 */
	private void resetIndent()
	{
		indent = "\t\t";
	}
	
	/**
	 * Adds Interface name to global interface list
	 * The list later stores the rules inherited from this interface
	 */
	public static void addInterface(String interfaceName)
	{
		interfaces.put(interfaceName, Lists.newArrayList());
	}
	
	/**
	 * @return list of all length field names
	 */
	public static List<String> getLengthFields()
	{
		List<String> list = new ArrayList<String>();
		list.addAll(lengthFields);
		return list;
	}
	
	/**
	 * @return list of all data field names
	 */
	public static List<String> getDataFields()
	{
		List<String> list = new ArrayList<String>();
		list.addAll(dataFields.keySet());
		return list;
	}
	
	/**
	 * @return list of all data field names
	 */
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
	
	/**
	 * @return list of all lexStrings
	 */
	public List<String> getLexStrings()
	{
		return Arrays.asList(lexStrings.toArray(new String [lexStrings.size()]));
	}
	
	/**
	 * @return number of all lexStrings
	 */
	public int getNumLexStrings()
	{
		return lexStrings.size();
	}
}