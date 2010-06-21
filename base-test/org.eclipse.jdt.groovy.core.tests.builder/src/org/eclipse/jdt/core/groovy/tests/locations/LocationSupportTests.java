/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.groovy.tests.locations;

import groovy.lang.GroovyClassLoader;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.codehaus.groovy.antlr.LocationSupport;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;

/**
 * @author Andrew Eisenberg
 * @created Jun 4, 2009
 *
 */
public class LocationSupportTests extends TestCase {
    
    public void testLocationSupport() throws Exception {
        List<StringBuffer> sbuffers = new LinkedList<StringBuffer>();
        sbuffers.add(new StringBuffer("123\n"));
        sbuffers.add(new StringBuffer("567\n"));
        sbuffers.add(new StringBuffer("90\n"));
        
        LocationSupport locations = new LocationSupport(sbuffers);
        assertEquals("Wrong offset found", 0, locations.findOffset(1, 1));
        assertEquals("Wrong offset found", 1, locations.findOffset(1, 2));
        assertEquals("Wrong offset found", 2, locations.findOffset(1, 3));
        assertEquals("Wrong offset found", 3, locations.findOffset(1, 4));
        assertEquals("Wrong offset found", 4, locations.findOffset(2, 1));
        assertEquals("Wrong offset found", 5, locations.findOffset(2, 2));
        assertEquals("Wrong offset found", 6, locations.findOffset(2, 3));
        assertEquals("Wrong offset found", 7, locations.findOffset(2, 4));
        assertEquals("Wrong offset found", 8, locations.findOffset(3, 1));
        assertEquals("Wrong offset found", 9, locations.findOffset(3, 2));
        assertEquals("Wrong offset found", 10, locations.findOffset(3, 3));
    }
    
    public void testParserSourceLocationsBlock() throws Exception {
        String content = "def x = 7\n  x++\n  def y = []";
        SourceUnit sourceUnit = new SourceUnit("Foo", content, new CompilerConfiguration(), new GroovyClassLoader(), new ErrorCollector(new CompilerConfiguration()));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        ModuleNode module = sourceUnit.getAST();
        
        // now check locations
        assertEquals(0, module.getStart());
        assertEquals(content.length(), module.getEnd());
        assertEquals(0, module.getStatementBlock().getStart());
        assertEquals(content.length(), module.getStatementBlock().getEnd());
        assertEquals("".length(), ((ASTNode) module.getStatementBlock().getStatements().get(0)).getStart());
        assertEquals("def x = 7".length(), ((ASTNode) module.getStatementBlock().getStatements().get(0)).getEnd());
        assertEquals("def x = 7\n  ".length(), ((ASTNode) module.getStatementBlock().getStatements().get(1)).getStart());
        assertEquals("def x = 7\n  x++".length(), ((ASTNode) module.getStatementBlock().getStatements().get(1)).getEnd());
        assertEquals("def x = 7\n  x++\n  ".length(), ((ASTNode) module.getStatementBlock().getStatements().get(2)).getStart());
        assertEquals("def x = 7\n  x++\n  def y = []".length(), ((ASTNode) module.getStatementBlock().getStatements().get(2)).getEnd());
    }
    
    public void testParserSourceLocationsEmpty() throws Exception {
        String content = "";
        SourceUnit sourceUnit = new SourceUnit("Foo", content, new CompilerConfiguration(), new GroovyClassLoader(), new ErrorCollector(new CompilerConfiguration()));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        ModuleNode module = sourceUnit.getAST();
        
        // now check locations
        assertEquals(0, module.getStart());
        assertEquals(content.length(), module.getEnd());
    }
    public void testParserSourceLocationsOneLine() throws Exception {
        String content = "def x = 7";
        SourceUnit sourceUnit = new SourceUnit("Foo", content, new CompilerConfiguration(), new GroovyClassLoader(), new ErrorCollector(new CompilerConfiguration()));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        ModuleNode module = sourceUnit.getAST();
        
        // now check locations
        assertEquals(0, module.getStart());
        assertEquals(content.length(), module.getEnd());
        assertEquals(0, module.getStatementBlock().getStart());
        assertEquals(content.length(), module.getStatementBlock().getEnd());
        assertEquals("".length(), ((ASTNode) module.getStatementBlock().getStatements().get(0)).getStart());
        assertEquals("def x = 7".length(), ((ASTNode) module.getStatementBlock().getStatements().get(0)).getEnd());
    }
    public void testParserSourceLocationsNewLine() throws Exception {
        String content = "def x = 7\n";
        SourceUnit sourceUnit = new SourceUnit("Foo", content, new CompilerConfiguration(), new GroovyClassLoader(), new ErrorCollector(new CompilerConfiguration()));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        ModuleNode module = sourceUnit.getAST();
        
        // now check locations
        assertEquals(0, module.getStart());
        assertEquals(content.length(), module.getEnd());
        assertEquals("".length(), module.getStatementBlock().getStart());
        assertEquals("def x = 7".length(), module.getStatementBlock().getEnd());
        assertEquals("".length(), ((ASTNode) module.getStatementBlock().getStatements().get(0)).getStart());
        assertEquals("def x = 7".length(), ((ASTNode) module.getStatementBlock().getStatements().get(0)).getEnd());
    }
    public void testParserSourceLocationsClass() throws Exception {
        String content = "class X {\n }";
        SourceUnit sourceUnit = new SourceUnit("Foo", content, new CompilerConfiguration(), new GroovyClassLoader(), new ErrorCollector(new CompilerConfiguration()));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        ModuleNode module = sourceUnit.getAST();
        
        // now check locations
        assertEquals(0, module.getStart());
        assertEquals(content.length(), module.getEnd());
        assertEquals(0, ((ASTNode) module.getClasses().get(0)).getStart());
        assertEquals(content.length(), ((ASTNode) module.getClasses().get(0)).getEnd());
    }
    
    public void testParserSourceLocationsMethod() throws Exception {
        String content = "def x() { \n\n\n\n\n\n\n}";
        SourceUnit sourceUnit = new SourceUnit("Foo", content, new CompilerConfiguration(), new GroovyClassLoader(), new ErrorCollector(new CompilerConfiguration()));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        ModuleNode module = sourceUnit.getAST();
        
        // now check locations
        assertEquals(0, module.getStart());
        assertEquals(content.length(), module.getEnd());
        assertEquals(0, ((ASTNode) module.getMethods().get(0)).getStart());
        assertEquals(content.length(), ((ASTNode) module.getMethods().get(0)).getEnd());
    }
    
    public void testParserSourceLocationsClassMethodStatement() throws Exception {
        String content = "def x = 7\n  x++\n  def y = []\ndef z() { \n\n\n\n\n\n\n}\nclass X {\n }";
        
        SourceUnit sourceUnit = new SourceUnit("Foo", content, new CompilerConfiguration(), new GroovyClassLoader(), new ErrorCollector(new CompilerConfiguration()));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        ModuleNode module = sourceUnit.getAST();
        
        // now check locations
        assertEquals(0, module.getStart());
        assertEquals(content.length(), module.getEnd());
        assertEquals(0, module.getStatementBlock().getStart());
        assertEquals("def x = 7\n  x++\n  def y = []\ndef z() { \n\n\n\n\n\n\n}".length(), module.getStatementBlock().getEnd());
        assertEquals("".length(), ((ASTNode) module.getStatementBlock().getStatements().get(0)).getStart());
        assertEquals("def x = 7".length(), ((ASTNode) module.getStatementBlock().getStatements().get(0)).getEnd());
        assertEquals("def x = 7\n  ".length(), ((ASTNode) module.getStatementBlock().getStatements().get(1)).getStart());
        assertEquals("def x = 7\n  x++".length(), ((ASTNode) module.getStatementBlock().getStatements().get(1)).getEnd());
        assertEquals("def x = 7\n  x++\n  ".length(), ((ASTNode) module.getStatementBlock().getStatements().get(2)).getStart());
        assertEquals("def x = 7\n  x++\n  def y = []".length(), ((ASTNode) module.getStatementBlock().getStatements().get(2)).getEnd());
        assertEquals("def x = 7\n  x++\n  def y = []\n".length(), ((ASTNode) module.getMethods().get(0)).getStart());
        assertEquals("def x = 7\n  x++\n  def y = []\ndef z() { \n\n\n\n\n\n\n}".length(), ((ASTNode) module.getMethods().get(0)).getEnd());
        // use index of 1 because zero index is of Foo
        assertEquals("def x = 7\n  x++\n  def y = []\ndef z() { \n\n\n\n\n\n\n}\n".length(), ((ASTNode) module.getClasses().get(1)).getStart());
        assertEquals("def x = 7\n  x++\n  def y = []\ndef z() { \n\n\n\n\n\n\n}\nclass X {\n }".length(), ((ASTNode) module.getClasses().get(1)).getEnd());

    }
}
