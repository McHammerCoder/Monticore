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

package de.monticore.symboltable.mocks.languages.scandentity;

import de.monticore.symboltable.CommonSymbolTableCreator;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.ResolverConfiguration;
import de.monticore.symboltable.mocks.languages.entity.EntityLanguageSymbolTableCreator;
import de.monticore.symboltable.mocks.languages.entity.asts.ASTEntity;
import de.monticore.symboltable.mocks.languages.entity.asts.ASTEntityBase;
import de.monticore.symboltable.mocks.languages.statechart.StateChartLanguageSymbolTableCreator;
import de.monticore.symboltable.mocks.languages.statechart.asts.ASTStateChartBase;
import de.monticore.ast.ASTNode;

// TODO PN implement generic CompositeSymbolTableCreator?
public class CompositeScAndEntitySymbolTableCreator extends CommonSymbolTableCreator implements
    EntityLanguageSymbolTableCreator, StateChartLanguageSymbolTableCreator {

  public CompositeScAndEntitySymbolTableCreator(ResolverConfiguration resolverConfig, MutableScope enclosingScope) {
    super(resolverConfig, enclosingScope);
  }

  @Override
  public void traverse(ASTEntity node) {
    visit(node);

    for (ASTNode child : node.get_Children()) {
      if (child instanceof ASTEntityBase) {
        ((ASTEntityBase) child).accept(this);
      }
      // TODO PN adjust to current visitor concept
      // EMBEDDING!! //
      else if (child instanceof ASTStateChartBase) {
        ((ASTStateChartBase) child).accept(this);
      }
    }

    endVisit(node);
  }


}
