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

package mc.feature.lexerformat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import org.junit.Test;

import de.monticore.antlr4.MCConcreteParser.ParserExecution;
import mc.GeneratorIntegrationsTest;
import mc.feature.lexerformat.lexerformat._ast.ASTTest;
import mc.feature.lexerformat.lexerformat._ast.ASTTest2;
import mc.feature.lexerformat.lexerformat._parser.LexerFormatParser;

public class LexerTest extends GeneratorIntegrationsTest {
  
  @Test
  public void test0() throws IOException {
    
    LexerFormatParser p = new LexerFormatParser();
    Optional<ASTTest> ast = p.parseTest(new StringReader("007"));
    assertTrue(ast.isPresent());
    
    int r = ast.get().getA();
    assertEquals(7, r);
  }
  
  @Test
  public void test1() throws IOException {
    
    LexerFormatParser p = new LexerFormatParser();
    Optional<ASTTest> ast = p.parseTest(new StringReader("on"));
    assertTrue(ast.isPresent());

    boolean r = ast.get().isB();
    assertEquals(true, r);
  }
  
  @Test
  public void test1a() throws IOException { 
    LexerFormatParser p = new LexerFormatParser();
    Optional<ASTTest> ast = p.parseTest(new StringReader("start"));
    assertTrue(ast.isPresent());
    
    boolean r = ast.get().isB();
    assertEquals(true, r);
  }
  
  @Test
  public void test1b() throws IOException { 
    LexerFormatParser p = new LexerFormatParser();
    Optional<ASTTest> ast = p.parseTest(new StringReader("stop"));
    assertTrue(ast.isPresent());

    boolean r = ast.get().isB();
    assertEquals(false, r);
  }
  
  @Test
  public void test1c() throws IOException {   
    LexerFormatParser p = new LexerFormatParser();
    Optional<ASTTest> ast = p.parseTest(new StringReader("off"));
    assertTrue(ast.isPresent());

    boolean r = ast.get().isB();
    assertEquals(false, r);
  }
  
  @Test
  public void test2() throws IOException {
    LexerFormatParser p = new LexerFormatParser();
    Optional<ASTTest> ast = p.parseTest(new StringReader("a"));
    assertTrue(ast.isPresent());

    char r = ast.get().getC();
    assertEquals('a', r);
  }
  

  @Test
  public void test3() throws IOException {
    LexerFormatParser p = new LexerFormatParser();
    Optional<ASTTest> ast = p.parseTest(new StringReader("99.5"));
    assertTrue(ast.isPresent());

    float r = ast.get().getD();
    assertEquals(99.5f, r, 0);   
  }
  
  @Test
  public void test4() throws IOException {    
    LexerFormatParser p = new LexerFormatParser();
    Optional<ASTTest> ast = p.parseTest(new StringReader("*"));
    assertTrue(ast.isPresent());

    int r = ast.get().getE();
    assertEquals(-1, r);
  }
  
  @Test
  public void test5() throws IOException {    
    LexerFormatParser p = new LexerFormatParser();
    Optional<ASTTest2> ast = p.parseTest2(new StringReader("1;1"));
    assertTrue(ast.isPresent());
  }
  
  @Test
  public void test6() throws IOException {    
    LexerFormatParser p = new LexerFormatParser();
    Optional<ASTTest> ast = p.parseTest(new StringReader("<<ddfdfd>>"));
    assertTrue(ast.isPresent());
  }
  
  @Test
  public void test7() throws IOException {    
    LexerFormatParser p = new LexerFormatParser();
    p.setParserTarget(ParserExecution.EOF);
    Optional<ASTTest> ast = p.parseTest(new StringReader("<<ddfd>>fd>>"));
    assertTrue(p.hasErrors());
  }
}
