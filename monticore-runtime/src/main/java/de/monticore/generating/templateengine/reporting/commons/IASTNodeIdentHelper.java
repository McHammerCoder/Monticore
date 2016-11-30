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

package de.monticore.generating.templateengine.reporting.commons;

import de.monticore.ast.ASTNode;

public interface IASTNodeIdentHelper {

  public static final String LAYOUT_FULL = "@%s!%s";
  
  public static final String LAYOUT_TYPE = "@!%s";
  
  default String format(String id, String type) {
    return String.format(LAYOUT_FULL, id, type);
  }
  
  default String format(String type) {
    return String.format(LAYOUT_TYPE, type);
  }
  
	public String getIdent(ASTNode ast);

}
