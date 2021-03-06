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

/* generated from model null*/
/* generated by template symboltable.ModelingLanguage*/




package mc.embedding.external.host._symboltable;

import java.util.Optional;

import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.ResolverConfiguration;

public abstract class HostLanguageTOP extends de.monticore.CommonModelingLanguage {

  public HostLanguageTOP(String langName, String fileEnding) {
    super(langName, fileEnding);

    initResolvingFilters();
  }

  @Override
  public Optional<HostSymbolTableCreator> getSymbolTableCreator(
      ResolverConfiguration resolverConfiguration, MutableScope enclosingScope) {
    return Optional.of(new HostSymbolTableCreator(resolverConfiguration, enclosingScope));
  }

  @Override
  public HostModelLoader getModelLoader() {
    return (HostModelLoader) super.getModelLoader();
  }

  //@Override
  //protected HostModelLoader provideModelLoader() {
  //  return new HostModelLoader(this);
  //}

  protected void initResolvingFilters() {
    addResolver(new ContentResolvingFilter());
    addResolver(new HostResolvingFilter());
  }
}
