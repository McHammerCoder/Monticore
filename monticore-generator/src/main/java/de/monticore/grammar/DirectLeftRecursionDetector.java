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

package de.monticore.grammar;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import de.monticore.ast.ASTNode;
import de.monticore.grammar.grammar._ast.ASTAlt;
import de.monticore.grammar.grammar._ast.ASTConstant;
import de.monticore.grammar.grammar._ast.ASTNonTerminal;
import de.monticore.grammar.grammar._ast.ASTTerminal;
import de.monticore.utils.ASTNodes;

/**
 * Checks if a MC production is a left directly left recursive: e.g. of the form A -> A.*
 *
 * @author (last commit) $Author$
 * @version $Revision$, $Date$
 */
public class DirectLeftRecursionDetector {
  
  public boolean isAlternativeLeftRecursive(final ASTAlt productionAlternative,
      final ASTNonTerminal actualNonTerminal) {
    final String classProductionName = actualNonTerminal.getName();
    Collection<Class<? extends ASTNode>> types = new HashSet<>(
        Arrays.asList(ASTNonTerminal.class, ASTTerminal.class, ASTConstant.class));
    final List<ASTNode> nodes = ASTNodes.getSuccessors(productionAlternative, types);
    
    if (nodes.isEmpty()) {
      return false;
    }
    
    if (nodes.get(0) instanceof ASTNonTerminal) {
      ASTNonTerminal leftmostNonterminal = (ASTNonTerminal) nodes.get(0);
      if ((leftmostNonterminal == actualNonTerminal)
          && leftmostNonterminal.getName().equals(classProductionName)) {
        return true;
      }
    }
    
    return false;
  }
  
  public boolean isAlternativeLeftRecursive(final ASTAlt productionAlternative,
      final String classProductionName) {
    Collection<Class<? extends ASTNode>> types = new HashSet<>(
        Arrays.asList(ASTNonTerminal.class, ASTTerminal.class, ASTConstant.class));
    final List<ASTNode> nodes = ASTNodes.getSuccessors(productionAlternative, types);
    
    if (nodes.isEmpty()) {
      return false;
    }
    
    if (nodes.get(0) instanceof ASTNonTerminal) {
      ASTNonTerminal leftmostNonterminal = (ASTNonTerminal) nodes.get(0);
      if (leftmostNonterminal.getName().equals(classProductionName)) {
        return true;
      }
    }
    
    return false;
  }
}
