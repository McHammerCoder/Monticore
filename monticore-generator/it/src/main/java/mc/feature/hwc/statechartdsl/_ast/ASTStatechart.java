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

package mc.feature.hwc.statechartdsl._ast;

import java.util.List;

import mc.feature.hwc.statechartdsl._ast.ASTCode;
import mc.feature.hwc.statechartdsl._ast.ASTState;
import mc.feature.hwc.statechartdsl._ast.ASTTransition;

public class ASTStatechart extends mc.feature.hwc.statechartdsl._ast.ASTStatechartTOP {
  
  protected ASTStatechart()
  {
    super();
  }
  
  protected ASTStatechart(
      List<ASTCode> userCode,
      String name,
      List<ASTState> states,
      List<ASTTransition> transitions)
  {
    super(userCode, name, states, transitions);
  }
  
  public String toString() {
    return "My statechart is " + getName();
  }
  
}
