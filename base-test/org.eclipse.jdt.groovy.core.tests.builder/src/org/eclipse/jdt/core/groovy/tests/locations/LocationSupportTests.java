/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.groovy.tests.locations;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.antlr.LocationSupport;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.junit.Test;

public final class LocationSupportTests {

    @Test
    public void testLocationSupport() throws Exception {
        List<StringBuffer> sbuffers = new LinkedList<>();
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

    @Test
    public void testParserSourceLocationsBlock() throws Exception {
        String content = "def x = 7\n  x++\n  def y = []";
        SourceUnit sourceUnit = new SourceUnit("Foo", content,
            CompilerConfiguration.DEFAULT, new GroovyClassLoader(), new ErrorCollector(CompilerConfiguration.DEFAULT));
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

    @Test
    public void testParserSourceLocationsEmpty() throws Exception {
        String content = "";
        SourceUnit sourceUnit = new SourceUnit("Foo", content,
            CompilerConfiguration.DEFAULT, new GroovyClassLoader(), new ErrorCollector(CompilerConfiguration.DEFAULT));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        ModuleNode module = sourceUnit.getAST();

        // now check locations
        assertEquals(0, module.getStart());
        assertEquals(content.length(), module.getEnd());
    }

    @Test
    public void testParserSourceLocationsOneLine() throws Exception {
        String content = "def x = 7";
        SourceUnit sourceUnit = new SourceUnit("Foo", content,
            CompilerConfiguration.DEFAULT, new GroovyClassLoader(), new ErrorCollector(CompilerConfiguration.DEFAULT));
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

    @Test
    public void testParserSourceLocationsNewLine() throws Exception {
        String content = "def x = 7\n";
        SourceUnit sourceUnit = new SourceUnit("Foo", content,
            CompilerConfiguration.DEFAULT, new GroovyClassLoader(), new ErrorCollector(CompilerConfiguration.DEFAULT));
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

    @Test
    public void testParserSourceLocationsClass() throws Exception {
        String content = "class X {\n}";
        SourceUnit sourceUnit = new SourceUnit("Foo", content,
            CompilerConfiguration.DEFAULT, new GroovyClassLoader(), new ErrorCollector(CompilerConfiguration.DEFAULT));
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

    @Test
    public void testParserSourceLocationsMethod() throws Exception {
        String content = "def x() { \n\n\n\n\n\n\n}";
        SourceUnit sourceUnit = new SourceUnit("Foo", content,
            CompilerConfiguration.DEFAULT, new GroovyClassLoader(), new ErrorCollector(CompilerConfiguration.DEFAULT));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        ModuleNode module = sourceUnit.getAST();

        // now check locations
        assertEquals(0, module.getStart());
        assertEquals(content.length(), module.getEnd());
        assertEquals(0, ((ASTNode) module.getMethods().get(0)).getStart());
        assertEquals(content.indexOf('x'), module.getMethods().get(0).getNameStart());
        assertEquals(content.indexOf('x') + "x".length() - 1, module.getMethods().get(0).getNameEnd());
        assertEquals(content.length(), ((ASTNode) module.getMethods().get(0)).getEnd());
    }

    @Test
    public void testParserSourceLocationsMethod2() throws Exception {
        String content = "def \"x   \"    () { \n\n\n\n\n\n\n}";
        SourceUnit sourceUnit = new SourceUnit("Foo", content,
            CompilerConfiguration.DEFAULT, new GroovyClassLoader(), new ErrorCollector(CompilerConfiguration.DEFAULT));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        ModuleNode module = sourceUnit.getAST();

        // now check locations
        assertEquals(0, module.getStart());
        assertEquals(content.length(), module.getEnd());
        assertEquals(0, ((ASTNode) module.getMethods().get(0)).getStart());
        assertEquals(content.indexOf("\"x   \""), (module.getMethods().get(0)).getNameStart());
        assertEquals(content.indexOf("\"x   \"") + "\"x   \"".length() - 1, (module.getMethods().get(0)).getNameEnd());
        assertEquals(content.length(), ((ASTNode) module.getMethods().get(0)).getEnd());
    }

    @Test
    public void testParserSourceLocationsClassMethodStatement() throws Exception {
        String content = "def x = 7\n  x++\n  def y = []\ndef z() { \n\n\n\n\n\n\n}\nclass X {\n}";

        SourceUnit sourceUnit = new SourceUnit("Foo", content,
            CompilerConfiguration.DEFAULT, new GroovyClassLoader(), new ErrorCollector(CompilerConfiguration.DEFAULT));
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
        assertEquals("def x = 7\n  x++\n  def y = []\ndef z() { \n\n\n\n\n\n\n}\nclass X {\n}".length(), ((ASTNode) module.getClasses().get(1)).getEnd());
    }

    @Test
    public void testGRECLIPSE887_ImportStatements() throws Exception {
        String content = "import java.util.List\nimport java.lang.*\nimport javax.swing.text.html.HTML.A\nimport javax.swing.text.html.HTML.*";

        SourceUnit sourceUnit = new SourceUnit("Foo", content,
            CompilerConfiguration.DEFAULT, new GroovyClassLoader(), new ErrorCollector(CompilerConfiguration.DEFAULT));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        final ModuleNode module = sourceUnit.getAST();

        // now check locations
        assertEquals(0, module.getStart());
        assertEquals(content.length(), module.getEnd());
        assertEquals(0, module.getImport("List").getStart());
        assertEquals("import java.util.List".length(), module.getImport("List").getEnd());
        assertEquals("import java.util.List\n".length(), module.getStarImports().get(0).getStart());
        assertEquals("import java.util.List\nimport java.lang.*".length(), module.getStarImports().get(0).getEnd());
        assertEquals("import java.util.List\nimport java.lang.*\n".length(), module.getImport("A").getStart());
        assertEquals("import java.util.List\nimport java.lang.*\nimport javax.swing.text.html.HTML.A".length(), module.getImport("A").getEnd());
        assertEquals("import java.util.List\nimport java.lang.*\nimport javax.swing.text.html.HTML.A\n".length(), module.getStarImports().get(1).getStart());
        assertEquals("import java.util.List\nimport java.lang.*\nimport javax.swing.text.html.HTML.A\nimport javax.swing.text.html.HTML.*".length(),
            module.getStarImports().get(1).getEnd());

        // now test against the compilation unit declaration
        GroovyCompilationUnitDeclaration cud = new GroovyCompilationUnitDeclaration(null, null, -1, null, null, null) {
            {
                GroovyCompilationUnitDeclaration.UnitPopulator cup = new GroovyCompilationUnitDeclaration.UnitPopulator();
                ReflectionUtils.setPrivateField(cup.getClass(), "unitDeclaration", cup, this);
                ReflectionUtils.executePrivateMethod(cup.getClass(), "createImportDeclarations", new Class[] {ModuleNode.class}, cup, new Object[] {module});
            }
        };

        assertEquals(module.getImport("List").getStart(), cud.imports[0].declarationSourceStart);
        assertEquals(module.getImport("List").getEnd() - 1, cud.imports[0].declarationSourceEnd);
        assertEquals(module.getStarImports().get(0).getStart(), cud.imports[1].declarationSourceStart);
        assertEquals(module.getStarImports().get(0).getEnd() - 1, cud.imports[1].declarationSourceEnd);
        assertEquals(module.getImport("A").getStart(), cud.imports[2].declarationSourceStart);
        assertEquals(module.getImport("A").getEnd() - 1, cud.imports[2].declarationSourceEnd);
        assertEquals(module.getStarImports().get(1).getStart(), cud.imports[3].declarationSourceStart);
        assertEquals(module.getStarImports().get(1).getEnd() - 1, cud.imports[3].declarationSourceEnd);
    }

    @Test
    public void testUnicodeEscapes1() throws Exception {
        String escapeSequence = "/*\\u00E9*/ ";
        String content = escapeSequence + "def x = 7";

        SourceUnit sourceUnit = new SourceUnit("Foo", content,
            CompilerConfiguration.DEFAULT, new GroovyClassLoader(), new ErrorCollector(CompilerConfiguration.DEFAULT));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        ModuleNode module = sourceUnit.getAST();

        // now check locations
        assertEquals(0, module.getStart());
        assertEquals(content.length(), module.getEnd());
        assertEquals(escapeSequence.length(), ((ASTNode) module.getStatementBlock().getStatements().get(0)).getStart());
        assertEquals(content.length(), ((ASTNode) module.getStatementBlock().getStatements().get(0)).getEnd());
    }

    @Test
    public void testUnicodeEscapes2() throws Exception {
        String escapeSequence = "/*\\u00E9*/ ";
        String content = escapeSequence + "\n\n\ndef x = 7";

        SourceUnit sourceUnit = new SourceUnit("Foo", content,
            CompilerConfiguration.DEFAULT, new GroovyClassLoader(), new ErrorCollector(CompilerConfiguration.DEFAULT));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        ModuleNode module = sourceUnit.getAST();

        // now check locations
        assertEquals(0, module.getStart());
        assertEquals(content.length(), module.getEnd());
        assertEquals(content.indexOf("def"), ((ASTNode) module.getStatementBlock().getStatements().get(0)).getStart());
        assertEquals(content.length(), ((ASTNode) module.getStatementBlock().getStatements().get(0)).getEnd());
    }

    @Test
    public void testUnicodeEscapes3() throws Exception {
        String escapeSequence = "/*\\u00E9\\u00E9\\u00E9\\u00E9\\u00E9\\u00E9\\u00E9\\u00E9\\u00E9*/";
        String content = escapeSequence + "\n\n\ndef /*\\u00E9*/x = /*\\u00E9*/7";

        SourceUnit sourceUnit = new SourceUnit("Foo", content,
            CompilerConfiguration.DEFAULT, new GroovyClassLoader(), new ErrorCollector(CompilerConfiguration.DEFAULT));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        ModuleNode module = sourceUnit.getAST();

        // now check locations
        assertEquals(0, module.getStart());
        assertEquals(content.length(), module.getEnd());
        assertEquals(content.indexOf("def"), ((ASTNode) module.getStatementBlock().getStatements().get(0)).getStart());
        assertEquals(content.length(), ((ASTNode) module.getStatementBlock().getStatements().get(0)).getEnd());

        // inside the assignment
        DeclarationExpression decl = (DeclarationExpression) ((ExpressionStatement) module.getStatementBlock().getStatements().get(0)).getExpression();
        assertEquals(content.indexOf('x'), decl.getLeftExpression().getStart());
        assertEquals(content.indexOf('x') + 1, decl.getLeftExpression().getEnd());
        assertEquals(content.indexOf('7'), decl.getRightExpression().getStart());
        assertEquals(content.indexOf('7') + 1, decl.getRightExpression().getEnd());
    }
}
