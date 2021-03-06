/*******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, 2016, MontiCore, All rights reserved.
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
 *******************************************************************************/
package de.monticore.genericgraphics.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Locator;
import org.eclipse.swt.graphics.Font;

import de.monticore.genericgraphics.model.ITextConnectionLabel;
import de.monticore.genericgraphics.view.figures.connections.locators.ConnectionLocatorPosition;


/**
 * <p>
 * A model element for connection labels that only show text.
 * </p>
 * <p>
 * Implements the {@link ITextConnectionLabel} interface, consisting of a list
 * of strings, a {@link Font} and a {@link ConnectionLocatorPosition}.
 * </p>
 * 
 * @author Tim Enger
 */
public class TextConnectionLabel extends ConnectionLabel implements ITextConnectionLabel {
  
  private List<String> texts;
  private Font font;
  
  /**
   * Constructor
   * 
   * @param position The {@link Locator}
   * @param texts The list of strings
   * @param font The {@link Font} to be used for displaying the text
   */
  public TextConnectionLabel(ConnectionLocatorPosition position, List<String> texts, Font font) {
    super(position);
    this.texts = texts;
    this.font = font;
  }
  
  /**
   * Constructor
   * 
   * @param position The {@link Locator}
   * @param text A single string
   * @param font The {@link Font} to be used for displaying the text
   */
  public TextConnectionLabel(ConnectionLocatorPosition position, String text, Font font) {
    super(position);
    texts = new ArrayList<String>();
    texts.add(text);
    this.font = font;
  }
  
  /**
   * @return The list of strings
   */
  @Override
  public List<String> getTexts() {
    return texts;
  }
  
  /**
   * @param texts The list of strings to set
   */
  @Override
  public void setTexts(List<String> texts) {
    this.texts = texts;
  }
  
  /**
   * @return The {@link Font}
   */
  @Override
  public Font getFont() {
    return font;
  }
  
  /**
   * @param font The {@link Font} to set
   */
  @Override
  public void setFont(Font font) {
    this.font = font;
  }
  
  /**
   * @return The text lines in one string separated by "\n".
   */
  public String getString() {
    StringBuilder sb = new StringBuilder();
    
    String sep = "";
    for (String s : texts) {
      sb.append(sep);
      sb.append(s);
      sep = "\n";
    }
    
    return sb.toString();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ConnectionLabel> ");
    
    sb.append("text: ");
    sb.append(texts);
    
    sb.append(" position: ");
    sb.append(getPosition());
    
    return sb.toString();
  }
  
}
