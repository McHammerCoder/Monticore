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

package de.monticore.codegen.mc2cd.manipul;

import static de.monticore.codegen.mc2cd.AttributeCategory.determineCategory;
import static de.monticore.codegen.mc2cd.TransformationHelper.typeToString;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.StreamSupport;

import com.google.common.collect.Iterables;

import de.monticore.codegen.mc2cd.AttributeCategory;
import de.monticore.codegen.mc2cd.TransformationHelper;
import de.monticore.types.types._ast.ASTSimpleReferenceType;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDAttribute;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDClass;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDInterface;
import de.monticore.utils.ASTNodes;

/**
 * Removes duplicate attributes that may result from rules having multiple nonterminals referencing
 * the same rule.
 *
 * @author Sebastian Oberhoff
 */
final class RemoveRedundantAttributesManipulation implements UnaryOperator<ASTCDCompilationUnit> {

  @Override
  public ASTCDCompilationUnit apply(ASTCDCompilationUnit cdCompilationUnit) {
    for (ASTCDClass cdClass : ASTNodes.getSuccessors(cdCompilationUnit, ASTCDClass.class)) {
      removeRedundantAttributes(cdClass.getCDAttributes());
    }
    for (ASTCDInterface cdClass : ASTNodes.getSuccessors(cdCompilationUnit, ASTCDInterface.class)) {
      removeRedundantAttributes(cdClass.getCDAttributes());
    }
    return cdCompilationUnit;
  }

  /**
   * @param cdAttributes the list of all the attributes in the class
   */
  void removeRedundantAttributes(List<ASTCDAttribute> cdAttributes) {
    Iterator<ASTCDAttribute> iterator = cdAttributes.iterator();
    while (iterator.hasNext()) {
      ASTCDAttribute inspectedAttribute = iterator.next();
      Iterable<ASTCDAttribute> remainingAttributes = Iterables.filter(cdAttributes,
          attribute -> !attribute.equals(inspectedAttribute));
      boolean isRedundant = StreamSupport.stream(remainingAttributes.spliterator(), false)
          .anyMatch(isRedundantPredicate(inspectedAttribute, remainingAttributes));
      if (isRedundant) {
        iterator.remove();
      }
    }
  }

  /**
   * Checks if the remaining attributes contain an attribute that makes the inspected attribute
   * redundant.
   *
   * @return true if another attribute with the same variable name, the same original type and an
   * equal or higher category exists
   */
  private static Predicate<ASTCDAttribute> isRedundantPredicate(ASTCDAttribute inspectedAttribute,
      Iterable<ASTCDAttribute> remainingAttributes) {
    String inspectedName = inspectedAttribute.getName();
    String inspectedType = getOriginalTypeName(inspectedAttribute);
    AttributeCategory inspectedCategory = determineCategory(inspectedAttribute);

    Predicate<ASTCDAttribute> sameName = remainingAttribute ->
        inspectedName.equalsIgnoreCase(remainingAttribute.getName());

    Predicate<ASTCDAttribute> sameType = remainingAttribute ->
        inspectedType.equals(getOriginalTypeName(remainingAttribute));

    Predicate<ASTCDAttribute> sameOrHigherCategory = remainingAttribute -> inspectedCategory
        .compareTo(AttributeCategory.determineCategory(remainingAttribute)) < 1;

    return sameName.and(sameType).and(sameOrHigherCategory);
  }

  private static String getOriginalTypeName(ASTCDAttribute cdAttribute) {
    AttributeCategory category = AttributeCategory.determineCategory(cdAttribute);
    if (category == AttributeCategory.GENERICLIST || category == AttributeCategory.OPTIONAL) {
      Optional<String> firstArgument = getFirstTypeArgument(cdAttribute);
      if (firstArgument.isPresent()) {
        return firstArgument.get();
      }
    }
    return TransformationHelper.typeToString(cdAttribute.getType());
  }

  private static Optional<String> getFirstTypeArgument(ASTCDAttribute cdAttribute) {
    // the 'List' in 'List<String>'
    ASTSimpleReferenceType outerType = (ASTSimpleReferenceType) cdAttribute
        .getType();

    if (!outerType.getTypeArguments().isPresent() || outerType
        .getTypeArguments().get().getTypeArguments().isEmpty()) {
      return Optional.empty();
    }
    // the 'String' in 'List<String>'
    ASTSimpleReferenceType typeArgument = (ASTSimpleReferenceType) outerType
        .getTypeArguments().get().getTypeArguments().get(0);

    return Optional.of(typeToString(typeArgument));
  }
}
