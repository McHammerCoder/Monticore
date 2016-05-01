package de.monticore.codegen.mchammerparser;

import static de.monticore.codegen.parser.ParserGeneratorHelper.getMCRuleForThisComponent;
import static de.monticore.codegen.parser.ParserGeneratorHelper.getTmpVarNameForAntlrCode;
import static de.monticore.codegen.parser.ParserGeneratorHelper.printIteration;

import java.util.*;

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

public class GrammarAnalyzer implements Grammar_WithConceptsVisitor {
	
	private List<String> lengthFields = Lists.newArrayList();
	
	private Map<String,Set<ASTGrammarNode>> dataFields = Maps.newHashMap();
	
	public GrammarAnalyzer() 
	{
	}
	
	@Override
	public void visit(ASTBinaryProd ast) 
	{
	}
	
	@Override
	public void visit(ASTBinaryAlt alt)
	{
	}
	
	@Override
	public void visit(ASTBinaryBlock ast)
	{
	}
	
	@Override
	public void visit(ASTBinarySimpleIteration ast)
	{
	}
	
	@Override
	public void visit(ASTBinaryNonTerminal ast) 
	{		
	}
	
	@Override
	public void visit(ASTBinaryLengthValue ast) 
	{	
	}
	
	@Override
	public void visit(ASTBinaryNRepeat ast) 
	{	
	}
	
	@Override
	public void visit(ASTUInt8 uint8)
	{	
	}
	
	@Override
	public void visit(ASTUInt16 uint16)
	{
	}
	
	@Override
	public void visit(ASTUInt32 uint32)
	{
	}
	
	@Override
	public void visit(ASTUInt64 uint64)
	{
	}
	
	@Override
	public void visit(ASTInt8 int8)
	{
	}
	
	@Override
	public void visit(ASTInt16 int16)
	{
	}
	
	@Override
	public void visit(ASTInt32 int32)
	{
	}
	
	@Override
	public void visit(ASTInt64 int64)
	{
	}
	
	@Override
	public void visit(ASTBits bits)
	{
	}
	
	@Override
	public void visit(ASTUBits ubits)
	{
	}
	
	@Override
	public void visit(ASTBinaryLength ast)
	{	
		lengthFields.add(ast.getId());
	}
	
	@Override
	public void visit(ASTBinaryData ast)
	{		
		ASTGrammarNode repeatable = null;
		
		if (ast.getUInt8().isPresent()) {
			repeatable = ast.getUInt8().get();
	    } 
	    else if (ast.getUInt16().isPresent()) {
	    	repeatable = ast.getUInt16().get();
	    } 
	    else if (ast.getUInt32().isPresent()) {
	    	repeatable = ast.getUInt32().get();
	    } 
	    else if (ast.getUInt64().isPresent()) {
	    	repeatable = ast.getUInt64().get();
	    }
	    else if (ast.getUBits().isPresent()) {
	    	repeatable = ast.getUBits().get();
	    }
	    else if (ast.getInt8().isPresent()) {
			repeatable = ast.getInt8().get();
	    } 
	    else if (ast.getInt16().isPresent()) {
	    	repeatable = ast.getInt16().get();
	    } 
	    else if (ast.getInt32().isPresent()) {
	    	repeatable = ast.getInt32().get();
	    } 
	    else if (ast.getInt64().isPresent()) {
	    	repeatable = ast.getInt64().get();
	    }
	    else if (ast.getBits().isPresent()) {
	    	repeatable = ast.getBits().get();
	    }
	    else if (ast.getBinaryNonTerminal().isPresent()) {
	    	repeatable = ast.getBinaryNonTerminal().get();
	    }
	    else {
	    	return ;
	    }
		
		String id = ast.getId();
		if( !dataFields.containsKey(id) )
		{
			dataFields.put(id,Sets.newHashSet());
		}
		
		dataFields.get(ast.getId()).add(repeatable);
	}
	
	// --------------------------------------------------------------
	
	public List<String> containsLengthFields(ASTProd ast)
	{
		lengthFields.clear();
		ast.accept(getRealThis());
		return lengthFields;
	}
	
	public Map<String,Set<ASTGrammarNode>> containsDataFields(ASTProd ast)
	{
		dataFields.clear();
		ast.accept(getRealThis());
		return dataFields;
	}
}
