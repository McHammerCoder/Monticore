/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2016, MontiCore, All rights reserved.
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

package de.monticore.grammar.symboltable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.monticore.symboltable.CommonScopeSpanningSymbol;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.SymbolKind;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author  Pedram Mir Seyed Nazari
 */
public class EssentialMCGrammarSymbol extends CommonScopeSpanningSymbol {

  public static final EssentialMCGrammarKind KIND = new EssentialMCGrammarKind();

  private final List<EssentialMCGrammarSymbolReference> superGrammars = new ArrayList<>();

  /**
   * Is the grammar abstract?
   */
  private boolean isComponent = false;

  // the start production of the grammar
  private MCProdSymbol startProd;


  public EssentialMCGrammarSymbol(String name) {
    super(name, KIND);
  }

  @Override
  protected MutableScope createSpannedScope() {
    return new EssentialMCGrammarScope(Optional.empty());
  }

  public void setStartProd(MCProdSymbol startRule) {
    this.startProd = startRule;
  }

  /**
   * The start production typically is the first defined production in the grammar.
   *
   * @return the start production of the grammar, if not a component grammar
   */
  public Optional<MCProdSymbol> getStartProd() {
    return Optional.ofNullable(startProd);
  }

  /**
   * @return true, if the grammar is abstract
   */
  public boolean isComponent() {
    return isComponent;
  }

  public void setComponent(boolean isComponent) {
    this.isComponent = isComponent;
  }

  public List<EssentialMCGrammarSymbolReference> getSuperGrammars() {
    return ImmutableList.copyOf(superGrammars);
  }
  
  public List<EssentialMCGrammarSymbol> getSuperGrammarSymbols() {
    return ImmutableList.copyOf(superGrammars.stream().filter(g -> g.getReferencedSymbol() != null)
        .map(g -> g.getReferencedSymbol())
        .collect(Collectors.toList()));
  }

  public void addSuperGrammar(EssentialMCGrammarSymbolReference superGrammarRef) {
    this.superGrammars.add(Log.errorIfNull(superGrammarRef));
  }

  public Collection<MCProdSymbol> getProds() {
    return this.getSpannedScope().resolveLocally(MCProdSymbol.KIND);
  }

  public Collection<String> getProdNames() {
    final Set<String> prodNames = new LinkedHashSet<>();

    for (final MCProdSymbol prodSymbol : getProds()) {
      prodNames.add(prodSymbol.getName());
    }

    return ImmutableSet.copyOf(prodNames);
  }

  public Optional<MCProdSymbol> getProd(String prodName) {
    return this.getSpannedScope().resolveLocally(prodName, MCProdSymbol.KIND);
  }


  public Optional<MCProdSymbol> getProdWithInherited(String ruleName) {
    Optional<MCProdSymbol> mcProd = getProd(ruleName);
    Iterator<EssentialMCGrammarSymbolReference> itSuperGrammars = superGrammars.iterator();

    while (!mcProd.isPresent() && itSuperGrammars.hasNext()) {
      mcProd = itSuperGrammars.next().getReferencedSymbol().getProdWithInherited(ruleName);
    }

    return mcProd;
  }

  public Map<String, MCProdSymbol> getProdsWithInherited() {
    final Map<String, MCProdSymbol> ret = new LinkedHashMap<>();

    for (int i = superGrammars.size() - 1; i >= 0; i--) {
      final EssentialMCGrammarSymbolReference superGrammarRef = superGrammars.get(i);

      if (superGrammarRef.existsReferencedSymbol()) {
        ret.putAll(superGrammarRef.getReferencedSymbol().getProdsWithInherited());
      }
    }

    for (final MCProdSymbol prodSymbol : getProds()) {
      ret.put(prodSymbol.getName(), prodSymbol);
    }

    return ret;
  }


  public static class EssentialMCGrammarKind implements  SymbolKind {

    private static final String NAME = EssentialMCGrammarKind.class.getName();

    protected EssentialMCGrammarKind() {
    }

    @Override
    public String getName() {
      return NAME;
    }

    @Override
    public boolean isKindOf(SymbolKind kind) {
      return NAME.equals(kind.getName()) || SymbolKind.super.isKindOf(kind);
    }

  }
}
