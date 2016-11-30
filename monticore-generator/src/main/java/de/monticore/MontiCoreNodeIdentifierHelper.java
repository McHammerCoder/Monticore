/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */

package de.monticore;

import static de.monticore.generating.templateengine.reporting.commons.Layouter.nodeName;

import de.monticore.ast.ASTNode;
import de.monticore.generating.templateengine.reporting.commons.IASTNodeIdentHelper;
import de.monticore.grammar.LexNamer;
import de.monticore.grammar.grammar._ast.ASTAntlrOption;
import de.monticore.grammar.grammar._ast.ASTAttributeInAST;
import de.monticore.grammar.grammar._ast.ASTConcept;
import de.monticore.grammar.grammar._ast.ASTConstantGroup;
import de.monticore.grammar.grammar._ast.ASTFollowOption;
import de.monticore.grammar.grammar._ast.ASTGenericType;
import de.monticore.grammar.grammar._ast.ASTGrammarReference;
import de.monticore.grammar.grammar._ast.ASTITerminal;
import de.monticore.grammar.grammar._ast.ASTLexNonTerminal;
import de.monticore.grammar.grammar._ast.ASTMCGrammar;
import de.monticore.grammar.grammar._ast.ASTMethod;
import de.monticore.grammar.grammar._ast.ASTMethodParameter;
import de.monticore.grammar.grammar._ast.ASTNonTerminal;
import de.monticore.grammar.grammar._ast.ASTNonTerminalSeparator;
import de.monticore.grammar.grammar._ast.ASTProd;
import de.monticore.grammar.grammar._ast.ASTRuleReference;
import de.monticore.literals.literals._ast.ASTBooleanLiteral;
import de.monticore.literals.literals._ast.ASTCharLiteral;
import de.monticore.literals.literals._ast.ASTDoubleLiteral;
import de.monticore.literals.literals._ast.ASTFloatLiteral;
import de.monticore.literals.literals._ast.ASTIntLiteral;
import de.monticore.literals.literals._ast.ASTLongLiteral;
import de.monticore.literals.literals._ast.ASTNullLiteral;
import de.monticore.literals.literals._ast.ASTStringLiteral;
import de.monticore.types.types._ast.ASTQualifiedName;
import de.monticore.types.types._ast.ASTSimpleReferenceType;
import de.monticore.types.types._ast.ASTTypeVariableDeclaration;
import de.se_rwth.commons.Names;

/**
 * TODO: Write me!
 *
 * @author (last commit) $Author$
 * @version $Revision$, $Date$
 */
public class MontiCoreNodeIdentifierHelper implements IASTNodeIdentHelper {
  
  // ##############
  // Identifier helper for Literals (could be moved into the grammars module?)
  // TODO: discuss this (with BR?): what do we want identifiers for in the end?
  // until then they remain unused
  // ##############
  
  private String getIdentifier(ASTBooleanLiteral ast) {
    return format(Boolean.toString(ast.getValue()), nodeName(ast));
  }
  
  private String getIdentifier(ASTCharLiteral ast) {
    return format(Character.toString(ast.getValue()), nodeName(ast));
  }
  
  private String getIdentifier(ASTDoubleLiteral ast) {
    return format(Double.toString(ast.getValue()), nodeName(ast));
  }
  
  private String getIdentifier(ASTFloatLiteral ast) {
    return format(Float.toString(ast.getValue()), nodeName(ast));
  }
  
  private String getIdentifier(ASTIntLiteral ast) {
    return format(Integer.toString(ast.getValue()), nodeName(ast));
  }
  
  private String getIdentifier(ASTLongLiteral ast) {
    return format(Long.toString(ast.getValue()), nodeName(ast));
  }
  
  private String getIdentifier(ASTNullLiteral ast) {
    return format("null", nodeName(ast));
  }
  
  private String getIdentifier(ASTStringLiteral ast) {
    return format(ast.getValue(), nodeName(ast));
  }
  
  // ##############
  // Identifier helper for Types (could be moved into the grammars module?)
  // TODO: incomplete by now; only those added here which "seem" to have a name
  // ##############
  
  private String getIdentifier(ASTQualifiedName ast) {
    return format(ast.toString(), nodeName(ast));
  }
  
  private String getIdentifier(ASTSimpleReferenceType ast) {
    return format(Names.getQualifiedName(ast.getNames()), nodeName(ast));
  }
  
  private String getIdentifier(ASTTypeVariableDeclaration ast) {
    return format(ast.getName(), nodeName(ast));
  }
  
  // ##############
  // Identifier helper for Grammar
  // TODO: incomplete by now; only those added here which "seem" to have a name
  // ##############
  
  private String getIdentifier(ASTAntlrOption ast) {
    return format(ast.getName(), nodeName(ast));
  }
  
  private String getIdentifier(ASTAttributeInAST ast) {
    if (ast.nameIsPresent()) {
      return format(ast.getName().get(), nodeName(ast));
    }
    return format(nodeName(ast));
  }
  
  private String getIdentifier(ASTConcept ast) {
    return format(ast.getName(), nodeName(ast));
  }
  
  private String getIdentifier(ASTConstantGroup ast) {
    if (ast.usageNameIsPresent()) {
      return format(ast.getUsageName().get(), nodeName(ast));
    }
    return format(nodeName(ast));
  }
  
  private String getIdentifier(ASTFollowOption ast) {
    return format(ast.getProdName(), nodeName(ast));
  }
  
  private String getIdentifier(ASTGenericType ast) {
    return format(Names.getQualifiedName(ast.getNames()), nodeName(ast));
  }
  
  private String getIdentifier(ASTGrammarReference ast) {
    return format(Names.getSimpleName(ast.getNames()), nodeName(ast));
  }
  
  private String getIdentifier(ASTITerminal ast) {
    // return a regular "Name"
    String name = ast.getName();
    if ((name.length()) < 4 && !name.matches("[a-zA-Z0-9_$\\-+]*")) {
      // Replace special character by the corresponding name (; -> SEMI)
      name = createGoodName(name);
    }
    else {
      // Replace all special characters by _
      name = name.replaceAll("[^a-zA-Z0-9_$\\-+]", "_");
      if (name.matches("[0-9].*")) {
        // if the name starts with a digit ...
        name = "_".concat(name);
      }
    }
    return format(name, nodeName(ast));
  }
  
  private String getIdentifier(ASTLexNonTerminal ast) {
    return format(ast.getName(), nodeName(ast));
  }
  
  private String getIdentifier(ASTMCGrammar ast) {
    return format(ast.getName(), nodeName(ast));
  }
  
  private String getIdentifier(ASTMethod ast) {
    return format(ast.getName(), nodeName(ast));
  }
  
  private String getIdentifier(ASTMethodParameter ast) {
    return format(ast.getName(), nodeName(ast));
  }
  
  private String getIdentifier(ASTNonTerminal ast) {
    return format(ast.getName(), nodeName(ast));
  }
  
  private String getIdentifier(ASTNonTerminalSeparator ast) {
    return format(ast.getName(), nodeName(ast));
  }
  
  private String getIdentifier(ASTProd ast) {
    return format(ast.getName(), nodeName(ast));
  }
  
  private String getIdentifier(ASTRuleReference ast) {
    return format(ast.getName(), nodeName(ast));
  }
  
  /**
   * @see de.monticore.generating.templateengine.reporting.commons.IASTNodeIdentHelper#getIdent(de.monticore.ast.ASTNode)
   */
  @Override
  public String getIdent(ASTNode ast) {
    if (ast instanceof ASTBooleanLiteral) {
      return getIdentifier((ASTBooleanLiteral) ast);
    }
    else if (ast instanceof ASTCharLiteral) {
      return getIdentifier((ASTCharLiteral) ast);
    }
    else if (ast instanceof ASTDoubleLiteral) {
      return getIdentifier((ASTDoubleLiteral) ast);
    }
    else if (ast instanceof ASTFloatLiteral) {
      return getIdentifier((ASTFloatLiteral) ast);
    }
    else if (ast instanceof ASTIntLiteral) {
      return getIdentifier((ASTIntLiteral) ast);
    }
    else if (ast instanceof ASTLongLiteral) {
      return getIdentifier((ASTLongLiteral) ast);
    }
    else if (ast instanceof ASTNullLiteral) {
      return getIdentifier((ASTNullLiteral) ast);
    }
    else if (ast instanceof ASTStringLiteral) {
      return getIdentifier((ASTStringLiteral) ast);
    }
    else if (ast instanceof ASTQualifiedName) {
      return getIdentifier((ASTQualifiedName) ast);
    }
    else if (ast instanceof ASTSimpleReferenceType) {
      return getIdentifier((ASTSimpleReferenceType) ast);
    }
    else if (ast instanceof ASTTypeVariableDeclaration) {
      return getIdentifier((ASTTypeVariableDeclaration) ast);
    }
    else if (ast instanceof ASTAntlrOption) {
      return getIdentifier((ASTAntlrOption) ast);
    }
    else if (ast instanceof ASTAttributeInAST) {
      return getIdentifier((ASTAttributeInAST) ast);
    }
    else if (ast instanceof ASTConcept) {
      return getIdentifier((ASTConcept) ast);
    }
    else if (ast instanceof ASTConstantGroup) {
      return getIdentifier((ASTConstantGroup) ast);
    }
    else if (ast instanceof ASTFollowOption) {
      return getIdentifier((ASTFollowOption) ast);
    }
    else if (ast instanceof ASTGenericType) {
      return getIdentifier((ASTGenericType) ast);
    }
    else if (ast instanceof ASTGrammarReference) {
      return getIdentifier((ASTGrammarReference) ast);
    }
    else if (ast instanceof ASTITerminal) {
      return getIdentifier((ASTITerminal) ast);
    }
    else if (ast instanceof ASTLexNonTerminal) {
      return getIdentifier((ASTLexNonTerminal) ast);
    }
    else if (ast instanceof ASTMCGrammar) {
      return getIdentifier((ASTMCGrammar) ast);
    }
    else if (ast instanceof ASTMethod) {
      return getIdentifier((ASTMethod) ast);
    }
    else if (ast instanceof ASTMethodParameter) {
      return getIdentifier((ASTMethodParameter) ast);
    }
    else if (ast instanceof ASTNonTerminal) {
      return getIdentifier((ASTNonTerminal) ast);
    }
    else if (ast instanceof ASTNonTerminalSeparator) {
      return getIdentifier((ASTNonTerminalSeparator) ast);
    }
    else if (ast instanceof ASTProd) {
      return getIdentifier((ASTProd) ast);
    }
    else if (ast instanceof ASTRuleReference) {
      return getIdentifier((ASTRuleReference) ast);
    }
    return format(nodeName(ast));
  }
  
  private static String createGoodName(String x) {
    StringBuilder ret = new StringBuilder();
    
    for (int i = 0; i < x.length(); i++) {
      
      String substring = x.substring(i, i + 1);
      if (LexNamer.getGoodNames().containsKey(substring)) {
        ret.append(LexNamer.getGoodNames().get(substring));
      }
      else {
        ret.append(substring);
      }
    }
    
    return ret.toString();
    
  }
  
}
