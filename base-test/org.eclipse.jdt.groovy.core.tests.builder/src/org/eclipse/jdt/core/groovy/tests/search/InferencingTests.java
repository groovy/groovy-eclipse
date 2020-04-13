/*
 * Copyright 2009-2020 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.search;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.junit.Test;

public final class InferencingTests extends InferencingTestSuite {

    private void assertNoUnknowns(String source) {
        List<ASTNode> unknownNodes = new ArrayList<>();
        org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(createUnit(DEFAULT_UNIT_NAME, source));
        visitor.visitCompilationUnit((node, result, element) -> {
            if (result.confidence == org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence.UNKNOWN && node.getEnd() > 0) {
                unknownNodes.add(node);
            }
            return org.eclipse.jdt.groovy.search.ITypeRequestor.VisitStatus.CONTINUE;
        });
        assertTrue("Should not have found any AST nodes with unknown confidence, but found:\n" + unknownNodes, unknownNodes.isEmpty());
    }

    private void assertUnknown(String source, String target) {
        int offset = source.lastIndexOf(target);
        assertUnknownConfidence(source, offset, offset + target.length());
    }

    //--------------------------------------------------------------------------

    @Test
    public void testNumber1() {
        assertType("10", "java.lang.Integer");
    }

    @Test
    public void testNumber2() {
        // same as above, but test that whitespace is not included
        assertType("10 ", 0, 2, "java.lang.Integer");
    }

    @Test
    public void testNumber3() {
        assertType("10L", "java.lang.Long");
    }

    @Test
    public void testNumber4() {
        assertType("10++", "java.lang.Integer");
    }

    @Test
    public void testNumber5() {
        assertType("++10", "java.lang.Integer");
    }

    @Test
    public void testNumber6() {
        assertType("(x <=> y).intValue()", "intValue", "java.lang.Integer");
    }

    @Test
    public void testString1() {
        assertType("\"10\"", "java.lang.String");
    }

    @Test
    public void testString2() {
        assertType("'10'", "java.lang.String");
    }

    @Test
    public void testString3() {
        assertType("def x = '10'", "'10'", "java.lang.String");
    }

    @Test
    public void testLocalVar1() {
        String contents = "int x; x";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testLocalVar2() {
        String contents = "def x; x()";
        assertType(contents, "x", "java.lang.Object");
    }

    @Test
    public void testLocalVar3() {
        String contents = "int x; foo(x)";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testLocalVar4() {
        String contents = "int x; this.x";
        assertUnknown(contents, "x");
    }

    @Test
    public void testLocalVar5() {
        String contents = "int x; def y = { x;}";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testLocalVar6() {
        String contents = "def x; def y = { this.x;}";
        assertUnknown(contents, "x");
    }

    @Test
    public void testLocalVar7() {
        String contents = "def x; def y = { this.x();}";
        assertUnknown(contents, "x");
    }

    @Test
    public void testLocalVar8() {
        String contents = "def x; def y = { owner.x;}\n";
        assertUnknown(contents, "x");
    }

    @Test
    public void testLocalVar9() {
        String contents = "def x; def y = { owner.x();}\n";
        assertUnknown(contents, "x");
    }

    @Test
    public void testLocalVar10() {
        String contents = "def x; def y = { delegate.x;}\n";
        assertUnknown(contents, "x");
    }

    @Test
    public void testLocalVar11() {
        String contents = "def x; def y = { delegate.x();}\n";
        assertUnknown(contents, "x");
    }

    @Test
    public void testLocalVar12() {
        String contents = "def x; def y = { thisObject.x;}\n";
        assertUnknown(contents, "x");
    }

    @Test
    public void testLocalVar13() {
        String contents = "def x; def y = { thisObject.x();}\n";
        assertUnknown(contents, "x");
    }

    @Test
    public void testLocalVar14() {
        String contents = "def x = predicate() ? 'literal' : something.toString(); x";
        assertType(contents, "x", "java.lang.String");
    }

    @Test
    public void testLocalVar15() {
        String contents = "def x\n" +
            "x = 1\n" +
            "x = 1.0d\n" +
            "x = 1.00\n" +
            "x";

        int offset = contents.indexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Object");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Integer");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Double");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.math.BigDecimal");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.math.BigDecimal");
    }

    @Test
    public void testLocalVar16() {
        String contents = "def m() {\n" +
            "  def x\n" +
            "  x = 1\n" +
            "  if (predicate()) {\n" +
            "    x = 1.0d\n" +
            "    return 0\n" +
            "  }\n" +
            "  x\n" +
            "}";

        int offset = contents.indexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Object");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Integer");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Double");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Integer");
    }

    @Test
    public void testLocalVar17() {
        String contents = "def m() {\n" +
            "  def x\n" +
            "  x = 1\n" +
            "  if (predicate())\n" +
            "    return (x = 1.0d)\n" +
            "  x\n" +
            "}";

        int offset = contents.indexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Object");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Integer");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Double");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Integer");
    }

    @Test
    public void testLocalVar18() {
        String contents = "def x\n" +
            "x = [] as List\n" +
            "if (predicate()) {\n" +
            "  x = [] as Set\n" +
            "}\n" +
            "x";

        int offset = contents.indexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Object");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.util.List");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.util.Set");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.util.Collection<java.lang.Object>");
    }

    @Test
    public void testLocalVar19() {
        String contents = "def x\n" +
            "x = [] as List\n" +
            "if (predicate())\n" +
            "  x = [] as Set\n" +
            "x";

        int offset = contents.indexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Object");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.util.List");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.util.Set");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.util.Collection<java.lang.Object>");
    }

    @Test
    public void testLocalVar20() {
        String contents = "def x\n" +
            "if (predicate()) {\n" +
            "  x = new String()\n" +
            "} else {\n" +
            "  x = new StringBuffer()\n" +
            "}\n" +
            "x";

        int offset = contents.indexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Object");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.String");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.StringBuffer");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Object"); // TODO: java.lang.CharSequence
    }

    @Test
    public void testLocalVar21() {
        String contents = "def x\n" +
            "if (predicate())\n" +
            "  x = new String()\n" +
            "else\n" +
            "  x = new StringBuffer()\n" +
            "x";

        int offset = contents.indexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Object");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.String");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.StringBuffer");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Object"); // TODO: java.lang.CharSequence
    }

    @Test
    public void testLocalVar22() {
        String contents = "def x\n" +
            "x = ''\n" +
            "if (predicate()) {\n" +
            "  x = 1.0d\n" +
            "} else if (predicate2()) {\n" +
            "  x = 1.00\n" +
            "}\n" +
            "x";

        int offset = contents.indexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Object");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.String");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Double");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.math.BigDecimal");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.io.Serializable<?>"); // LUB of String, Double, and BigDecimal
    }

    @Test
    public void testLocalVar23() {
        String contents = "def x\n" +
            "x = ''\n" +
            "if (predicate())\n" +
            "  x = 1.0d\n" +
            "else if (predicate2())\n" +
            "  x = 1.00\n" +
            "x";

        int offset = contents.indexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Object");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.String");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Double");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.math.BigDecimal");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.io.Serializable<?>"); // LUB of String, Double, and BigDecimal
    }

    @Test
    public void testLocalVar24() {
        String contents = "def x\n" +
            "x = ''\n" +
            "if (predicate()) {\n" +
            "  x = 1.0d\n" +
            "} else {\n" +
            "  if (predicate2()) {\n" +
            "    x = 1.00\n" +
            "  }\n" +
            "}\n" +
            "x";

        int offset = contents.indexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Object");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.String");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Double");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.math.BigDecimal");

        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.io.Serializable<?>"); // LUB of String, Double, and BigDecimal
    }

    @Test
    public void testLocalVar25() {
        String contents = "def x\n" +
            "x = ''\n" +
            "def cl = { ->\n" +
            "  x = 1.0d\n" +
            "}\n" +
            "x\n" +
            "x = 1.0\n" +
            "x";

        int offset = contents.indexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Object");

        // line 2
        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.String");

        // line 4
        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Double");

        // line 6
        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.String");

        // line 7
        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.math.BigDecimal");

        // line 8
        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.math.BigDecimal");
    }

    @Test
    public void testLocalVar26() {
        String contents = "String x\n" +
            "x";
        assertType(contents, "x", "java.lang.String");
    }

    @Test
    public void testLocalVar27() {
        String contents = "String x = 7\n" +
            "x";
        assertType(contents, "x", "java.lang.String");
    }

    @Test
    public void testLocalVar28() {
        String contents = "String x\n" +
            "x = 7\n" + // GroovyCastException at runtime
            "x";
        assertType(contents, "x", "java.lang.String");
    }

    @Test
    public void testLocalMethod1() {
        String contents =
            "int x() {}\n" +
            "def y = {\n" +
            "  x()\n" +
            "}\n";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/802
    public void testLocalMethod2() {
        String contents =
            "int x() {}\n" +
            "def y = {\n" +
            "  x\n" +
            "}\n";
        assertUnknown(contents, "x");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/802
    public void testLocalMethod3() {
        String contents =
            "int x() {}\n" +
            "def y = {\n" +
            "  def z = x\n" +
            "}\n";
        assertUnknown(contents, "x");
    }

    @Test
    public void testLocalMethod4() {
        String contents =
            "int x() {}\n" +
            "def y = {\n" +
            "  def z = {\n" +
            "    x()\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testLocalMethod5() {
        String contents =
            "int x() {}\n" +
            "def y = {\n" +
            "  def z = {\n" +
            "    this.x()\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testMatcher1() {
        String contents = "def x = \"\" =~ /pattern/\nx";
        assertType(contents, "x", "java.util.regex.Matcher");
    }

    @Test
    public void testMatcher2() {
        String contents = "(\"\" =~ /pattern/).hasGroup()";
        assertType(contents, "hasGroup", "java.lang.Boolean");
    }

    @Test
    public void testPattern1() {
        String contents = "def x = ~/pattern/\nx";
        assertType(contents, "x", "java.util.regex.Pattern");
    }

    @Test
    public void testPattern2() {
        String contents = "def x = \"\" ==~ /pattern/\nx";
        assertType(contents, "x", "java.lang.Boolean");
    }

    @Test
    public void testSpread1() {
        String contents = "def z = [1,2]*.value";
        assertType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testSpread2() {
        String contents = "[1,2,3]*.intValue()";
        assertType(contents, "intValue", "java.lang.Integer");
    }

    @Test
    public void testSpread3() {
        String contents = "[1,2,3]*.intValue()[0].value";
        assertType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testSpread4() {
        String contents = "[x:1,y:2,z:3]*.getKey()";
        assertType(contents, "getKey", "java.lang.String");
    }

    @Test
    public void testSpread5() {
        String contents = "[x:1,y:2,z:3]*.getValue()";
        assertType(contents, "getValue", "java.lang.Integer");
    }

    @Test
    public void testSpread6() {
        String contents = "[x:1,y:2,z:3]*.key";
        assertType(contents, "key", "java.lang.String");
    }

    @Test
    public void testSpread7() {
        String contents = "[x:1,y:2,z:3]*.value";
        assertType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testSpread8() {
        String contents = "[x:1,y:2,z:3]*.key[0].toLowerCase()";
        assertType(contents, "toLowerCase", "java.lang.String");
    }

    @Test
    public void testSpread9() {
        String contents = "[x:1,y:2,z:3]*.value[0].intValue()";
        assertType(contents, "intValue", "java.lang.Integer");
    }

    @Test
    public void testSpread10() {
        String contents = "[1,2,3]*.value[0].value";
        assertType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testSpread11() {
        String contents = "Set<String> strings = ['1','2','3'] as Set\n" +
            "strings*.bytes\n";
        assertType(contents, "bytes", "byte[]");
    }

    @Test
    public void testSpread12() {
        String contents = "Set<String> strings = ['1','2','3'] as Set\n" +
            "strings*.length()\n";
        assertType(contents, "length", "java.lang.Integer");
    }

    @Test
    public void testSpread13() {
        String contents = "@groovy.transform.TypeChecked\n" +
            "class Foo {\n" +
            "  static def meth() {\n" +
            "    Set<java.beans.BeanInfo> beans = []\n" +
            "    beans*.additionalBeanInfo\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "beans", "java.util.Set<java.beans.BeanInfo>");
        assertType(contents, "additionalBeanInfo", "java.beans.BeanInfo[]");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/763
    public void testSpread14() {
        String contents = "def strings = [[['1','2','3']]]\n" +
            "def result = strings*.length()\n";
        assertType(contents, "result", "java.util.List<java.util.List>");
    }

    @Test // CommandRegistry.iterator() lacks generics
    public void testSpread15() {
        String contents =
            "import org.codehaus.groovy.tools.shell.CommandRegistry\n" +
            "def registry = new CommandRegistry()\n" +
            "def result = registry*.with {it}\n";
        assertType(contents, "result", "java.util.List<java.lang.Object>");
    }

    @Test
    public void testSpread16() {
        String contents =
            "import java.util.regex.Matcher\n" +
            "Matcher matcher = ('abc' =~ /./)\n" +
            "def result = matcher*.with {it}\n";
        assertType(contents, "result", "java.util.List<java.lang.Object>");
    }

    @Test
    public void testSpread17() {
        String contents =
            "Reader reader = null\n" +
            "def result = reader*.with {it}\n";
        assertType(contents, "result", "java.util.List<java.lang.String>");
    }

    @Test
    public void testMapLiteral() {
        assertType("[:]", "java.util.Map<java.lang.Object,java.lang.Object>");
    }

    @Test
    public void testBoolean1() {
        assertType("!x", "java.lang.Boolean");
    }

    @Test
    public void testBoolean2() {
        String contents = "(x < y).booleanValue()";
        assertType(contents, "booleanValue", "java.lang.Boolean");
    }

    @Test
    public void testBoolean3() {
        String contents = "(x <= y).booleanValue()";
        assertType(contents, "booleanValue", "java.lang.Boolean");
    }

    @Test
    public void testBoolean4() {
        String contents = "(x >= y).booleanValue()";
        assertType(contents, "booleanValue", "java.lang.Boolean");
    }

    @Test
    public void testBoolean5() {
        String contents = "(x != y).booleanValue()";
        assertType(contents, "booleanValue", "java.lang.Boolean");
    }

    @Test
    public void testBoolean6() {
        String contents = "(x == y).booleanValue()";
        assertType(contents, "booleanValue", "java.lang.Boolean");
    }

    @Test
    public void testBoolean7() {
        String contents = "(x in y).booleanValue()";
        assertType(contents, "booleanValue", "java.lang.Boolean");
    }

    @Test
    public void testClassLiteral1() {
        String contents = "def foo = Number.class";
        assertType(contents, "foo", "java.lang.Class<java.lang.Number>");
    }

    @Test
    public void testClassLiteral2() {
        String contents = "def foo = java.lang.Number.class";
        assertType(contents, "foo", "java.lang.Class<java.lang.Number>");
    }

    @Test
    public void testClassLiteral3() {
        String contents = "def foo = Number";
        assertType(contents, "foo", "java.lang.Class<java.lang.Number>");
    }

    @Test
    public void testClassLiteral4() {
        String contents = "def foo = java.lang.Number";
        assertType(contents, "foo", "java.lang.Class<java.lang.Number>");
    }

    @Test
    public void testClassLiteral5() {
        String contents = "def foo = Map.Entry.class";
        assertType(contents, "foo", "java.lang.Class<java.util.Map$Entry>");
    }

    @Test
    public void testClassReference1() {
        String contents = "String.substring";
        int start = contents.indexOf("substring"), until = start + 9;
        assertDeclaringType(contents, start, until, "java.lang.String", true);
    }

    @Test
    public void testClassReference2() {
        String contents = "String.getPackage()";
        assertType(contents, "getPackage", "java.lang.Package");
        assertDeclaringType(contents, "getPackage", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference2a() {
        String contents = "String.package";
        assertType(contents, "package", "java.lang.Package");
        assertDeclaringType(contents, "package", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference3() {
        String contents = "String.class.getPackage()";
        assertType(contents, "getPackage", "java.lang.Package");
        assertDeclaringType(contents, "getPackage", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference3a() {
        String contents = "String.class.package";
        assertType(contents, "package", "java.lang.Package");
        assertDeclaringType(contents, "package", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference4() {
        String contents = "def clazz = String; clazz.getPackage()";
        assertType(contents, "getPackage", "java.lang.Package");
        assertDeclaringType(contents, "getPackage", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference4a() {
        String contents = "def clazz = String; clazz.package";
        assertType(contents, "package", "java.lang.Package");
        assertDeclaringType(contents, "package", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference4b() {
        String contents = "Class clazz = String; clazz.package";
        assertType(contents, "package", "java.lang.Package");
        assertDeclaringType(contents, "package", "java.lang.Class");
    }

    @Test // GRECLIPSE-1229: constructors with map parameters
    public void testConstructor1() {
        String contents =
            "class O {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "new O(aa: 1, bb:8)";

        int start = contents.lastIndexOf("aa");
        int end = start + "aa".length();
        assertType(contents, start, end, "java.lang.Boolean");
        assertDeclaration(contents, start, end, "O", "aa", DeclarationKind.PROPERTY);

        start = contents.lastIndexOf("bb");
        end = start + "bb".length();
        assertType(contents, start, end, "java.lang.Integer");
        assertDeclaration(contents, start, end, "O", "bb", DeclarationKind.PROPERTY);
    }

    @Test
    public void testConstructor2() {
        String contents =
            "class O {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "new O([aa: 1, bb:8])";

        int start = contents.lastIndexOf("aa");
        int end = start + "aa".length();
        assertType(contents, start, end, "java.lang.Boolean");
        assertDeclaration(contents, start, end, "O", "aa", DeclarationKind.PROPERTY);

        start = contents.lastIndexOf("bb");
        end = start + "bb".length();
        assertType(contents, start, end, "java.lang.Integer");
        assertDeclaration(contents, start, end, "O", "bb", DeclarationKind.PROPERTY);
    }

    @Test
    public void testConstructor3() {
        String contents =
            "class O {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "new O([8: 1, bb:8])";

        int start = contents.lastIndexOf("bb");
        int end = start + "bb".length();
        assertType(contents, start, end, "java.lang.Integer");
        assertDeclaration(contents, start, end, "O", "bb", DeclarationKind.PROPERTY);
    }

    @Test
    public void testConstructor4() {
        String contents =
            "class O {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "new O([aa: 1, bb:8], 9)";

        int start = contents.lastIndexOf("aa");
        int end = start + "aa".length();
        assertType(contents, start, end, "java.lang.String");
        assertDeclaringType(contents, start, end, "null");

        start = contents.lastIndexOf("bb");
        end = start + "bb".length();
        assertType(contents, start, end, "java.lang.String");
        assertDeclaringType(contents, start, end, "null");
    }

    @Test
    public void testConstructor5() {
        String contents =
            "class O {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "new O(9, [aa: 1, bb:8])";

        int start = contents.lastIndexOf("aa");
        int end = start + "aa".length();
        assertType(contents, start, end, "java.lang.String");
        assertDeclaringType(contents, start, end, "null");

        start = contents.lastIndexOf("bb");
        end = start + "bb".length();
        assertType(contents, start, end, "java.lang.String");
        assertDeclaringType(contents, start, end, "null");
    }

    @Test
    public void testConstructor6() {
        String contents =
            "class O {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "def g = [aa: 1, bb:8]";

        int start = contents.lastIndexOf("aa");
        int end = start + "aa".length();
        assertType(contents, start, end, "java.lang.String");
        assertDeclaringType(contents, start, end, "null");

        start = contents.lastIndexOf("bb");
        end = start + "bb".length();
        assertType(contents, start, end, "java.lang.String");
        assertDeclaringType(contents, start, end, "null");
    }

    @Test
    public void testSpecialConstructor1() {
        String contents =
            "class C {\n" +
            "  C() {\n" +
            "    this()\n" +
            "  }\n" +
            "}";

        assertType(contents, "this()", "java.lang.Void");
        assertDeclaringType(contents, "this()", "C");
    }

    @Test
    public void testSpecialConstructor2() {
        String contents =
            "class C extends HashMap {\n" +
            "  C() {\n" +
            "    super()\n" +
            "  }\n" +
            "}";

        assertType(contents, "super()", "java.lang.Void");
        assertDeclaringType(contents, "super()", "java.util.HashMap");
    }

    @Test
    public void testStaticMethod1() {
        String contents = "class Two { static String x() {\"\"}}\n Two.x()";
        assertType(contents, "x", "java.lang.String");
    }

    @Test
    public void testStaticMethod2() {
        String contents = "class Two { static String x() {\"\"}}\n Two.x";
        assertType(contents, "x", "java.lang.String");
    }

    @Test
    public void testStaticMethod3() {
        String contents =
            "class Two {\n" +
            "  static Number x() {\n" +
            "  }\n" +
            "  def other() {\n" +
            "    x()\n" + // this
            "  }\n" +
            "}\n";
        assertType(contents, "x", "java.lang.Number");
    }

    @Test
    public void testStaticMethod4() {
        String contents =
            "class Two {\n" +
            "  static Number x() {\n" +
            "  }\n" +
            "  def other() {\n" +
            "    x\n" + // this
            "  }\n" +
            "}\n";
        assertUnknown(contents, "x");
    }

    @Test // GRECLISPE-1244
    public void testStaticMethod5() {
        String contents =
            "class Parent {\n" +
            "    static p() {}\n" +
            "}\n" +
            "class Child extends Parent {\n" +
            "    def c() {\n" +
            "        p()\n" +
            "    }\n" +
            "}";
        assertDeclaringType(contents, "p()", "Parent");
    }

    @Test // GRECLISPE-1244
    public void testStaticMethod6() {
        createUnit("Parent",
            "class Parent {\n" +
            "    static p() {}\n" +
            "}");
        String contents =
            "class Child extends Parent {\n" +
            "    def c() {\n" +
            "        p()\n" +
            "    }\n" +
            "}";
        assertDeclaringType(contents, "p()", "Parent");
    }

    @Test
    public void testStaticMethod7() throws Exception {
        createUnit("foo", "Bar", "package foo\n" +
            "import java.util.regex.*\n" +
            "class Bar {\n" +
            "  static Object meth(Object o) { return o;}\n" +
            "  static Pattern meth(Pattern p) { return p;}\n" +
            "  static Collection meth(Collection c) { return c;}\n" +
            "}");

        String contents = "import static foo.Bar.*\nmeth([])";
        assertType(contents, "meth([])", "java.util.Collection");
    }

    @Test
    public void testStaticMethod8() throws Exception {
        createUnit("foo", "Bar", "package foo\n" +
            "import java.util.regex.*\n" +
            "class Bar {\n" +
            "  static Object meth(Object o) { return o;}\n" +
            "  static Pattern meth(Pattern p) { return p;}\n" +
            "  static Collection meth(Collection c) { return c;}\n" +
            "}");

        String contents = "import static foo.Bar.*\nmeth(~/abc/)";
        assertType(contents, "meth(~/abc/)", "java.util.regex.Pattern");
    }

    @Test
    public void testStaticMethod9() throws Exception {
        createUnit("foo", "Bar", "package foo\n" +
            "import java.util.regex.*\n" +
            "class Bar {\n" +
            "  static Object meth(Object o) { return o;}\n" +
            "  static Pattern meth(Pattern p) { return p;}\n" +
            "  static Collection meth(Collection c) { return c;}\n" +
            "}");

        String contents =
            "import static foo.Bar.*\n" +
            "import static java.util.regex.Pattern.*\n" +
            "meth(compile('abc'))";
        assertType(contents, "meth(compile('abc'))", "java.util.regex.Pattern");
    }

    @Test
    public void testStaticMethod10() throws Exception {
        createUnit("foo", "Bar", "package foo\n" +
            "import java.util.regex.*\n" +
            "abstract class Bar {\n" +
            "  static Object meth(Object o) { return o;}\n" +
            "  static Pattern meth(Pattern p) { return p;}\n" +
            "  static Collection meth(Collection c) { return c;}\n" +
            "}");

        String contents =
            "import static foo.Bar.*\n" +
            "import static java.util.regex.Pattern.*\n" +
            "meth(compile('abc'))";
        assertType(contents, "meth", "java.util.regex.Pattern");
    }

    @Test
    public void testStaticMethod11() throws Exception {
        String contents =
            "import static java.util.regex.Pattern.*\n" +
            "import java.util.regex.*\n" +
            "abstract class Bar {\n" +
            "  static Object meth(Object o) { return o;}\n" +
            "  static Pattern meth(Pattern p) { return p;}\n" +
            "  static Collection meth(Collection c) { return c;}\n" +
            "  static main(args) {\n" +
            "    meth(compile('abc'))\n" +
            "  }\n" +
            "}";
        assertType(contents, "meth", "java.util.regex.Pattern");
    }

    @Test
    public void testStaticMethod12() throws Exception {
        String contents =
            "import static java.util.regex.Pattern.*\n" +
            "import java.util.regex.*\n" +
            "class Foo {\n" +
            "  static Object meth(Object o) { return o;}\n" +
            "  static Pattern meth(Pattern p) { return p;}\n" +
            "  static Collection meth(Collection c) { return c;}\n" +
            "}\n" +
            "abstract class Bar extends Foo {\n" +
            "  static main(args) {\n" +
            "    meth(compile('abc'))\n" +
            "  }\n" +
            "}";
        assertType(contents, "meth", "java.util.regex.Pattern");
    }

    @Test
    public void testStaticThisAndSuper1() {
        String contents =
            "class A {\n" +
            "  static main(args) {\n" +
            "    this\n" +
            "    super\n" +
            "  }\n" +
            "}";
        assertType(contents, "this", "java.lang.Class<A>");
        assertType(contents, "super", "java.lang.Object");
    }

    @Test
    public void testStaticThisAndSuper2() {
        String contents =
            "class A {\n" +
            "}\n" +
            "class B extends A {\n" +
            "  static main(args) {\n" +
            "    this\n" +
            "    super\n" +
            "  }\n" +
            "}";
        assertType(contents, "this", "java.lang.Class<B>");
        assertType(contents, "super", "java.lang.Class<A>");
    }

    @Test
    public void testSuperFieldReference1() {
        String contents =
            "class A {\n" +
            "  public Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    field\n" +
            "  }\n" +
            "}";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference1a() {
        String contents =
            "class A {\n" +
            "  public Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    this.field\n" +
            "  }\n" +
            "}";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference1b() {
        String contents =
            "class A {\n" +
            "  public Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    this.@field\n" +
            "  }\n" +
            "}";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference1c() {
        String contents =
            "class A {\n" +
            "  public Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.field\n" +
            "  }\n" +
            "}";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference1d() {
        String contents =
            "class A {\n" +
            "  public Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.@field\n" +
            "  }\n" +
            "}";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference2() {
        String contents =
            "class A {\n" +
            "  protected Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    field\n" +
            "  }\n" +
            "}";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference2a() {
        String contents =
            "class A {\n" +
            "  protected Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    this.field\n" +
            "  }\n" +
            "}";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference2b() {
        String contents =
            "class A {\n" +
            "  protected Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    this.@field\n" +
            "  }\n" +
            "}";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference2c() {
        String contents =
            "class A {\n" +
            "  protected Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.field\n" +
            "  }\n" +
            "}";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference2d() {
        String contents =
            "class A {\n" +
            "  protected Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.@field\n" +
            "  }\n" +
            "}";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperFieldReference3() {
        String contents =
            "class A {\n" +
            "  private String field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    field\n" +
            "  }\n" +
            "}";
        assertUnknown(contents, "field");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperFieldReference3a() {
        String contents =
            "class A {\n" +
            "  private Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    this.field\n" +
            "  }\n" +
            "}";
        assertUnknown(contents, "field");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperFieldReference3b() {
        String contents =
            "class A {\n" +
            "  private Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    this.@field\n" +
            "  }\n" +
            "}";
        assertUnknown(contents, "field");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperFieldReference3c() {
        String contents =
            "class A {\n" +
            "  private Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.field\n" +
            "  }\n" +
            "}";
        assertUnknown(contents, "field");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperFieldReference3d() {
        String contents =
            "class A {\n" +
            "  private Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.@field\n" +
            "  }\n" +
            "}";
        assertUnknown(contents, "field");
    }

    @Test
    public void testSuperFieldReference4() {
        String contents =
            "public interface Constants {\n" +
            "  int FIRST = 1;\n" +
            "}\n" +
            "class UsesConstants implements Constants {\n" +
            "  def x() {\n" +
            "    FIRST\n" +
            "  }\n" +
            "}";
        assertType(contents, "FIRST", "java.lang.Integer");
    }

    @Test
    public void testSuperFieldReference5() {
        createJavaUnit("foo", "Bar",
            "package foo;\n" +
            "public class Bar {\n" +
            "  public static final int CONST = 42;\n" +
            "}\n");

        String contents =
            "class Baz extends foo.Bar {\n" +
            "  Baz() {\n" +
            "    this(CONST)\n" +
            "  }\n" +
            "  Baz(int num) {\n" +
            "  }\n" +
            "}";
        assertType(contents, "CONST", "java.lang.Integer");
    }

    @Test
    public void testSuperFieldReference5a() {
        createJavaUnit("foo", "Bar",
            "package foo;\n" +
            "public class Bar {\n" +
            "  public static final int CONST = 42;\n" +
            "  public Bar(int num) {\n" +
            "  }\n" +
            "}\n");

        String contents =
            "class Baz extends foo.Bar {\n" +
            "  Baz() {\n" +
            "    super(CONST)\n" +
            "  }\n" +
            "}";
        assertType(contents, "CONST", "java.lang.Integer");
    }

    @Test
    public void testSuperFieldReference5b() {
        createJavaUnit("foo", "Bar",
            "package foo;\n" +
            "public class Bar {\n" +
            "  public static final int CONST = 42;\n" +
            "  public Bar(int num) {\n" +
            "  }\n" +
            "}\n");

        String contents =
            "class Baz extends foo.Bar {\n" +
            "  Baz(int num) {\n" +
            "    super(select(num, CONST))\n" +
            "  }\n" +
            "  private static int select(int one, int two) {\n" +
            "  }\n" +
            "}";
        assertType(contents, "CONST", "java.lang.Integer");
    }

    @Test
    public void testSuperPropertyReference1() {
        String contents =
            "class A {\n" +
            "  Number value\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    value\n" +
            "  }\n" +
            "}";
        assertType(contents, "value", "java.lang.Number");
    }

    @Test
    public void testSuperPropertyReference2() {
        String contents =
            "class A {\n" +
            "  Number value\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    this.value\n" +
            "  }\n" +
            "}";
        assertType(contents, "value", "java.lang.Number");
    }

    @Test
    public void testSuperPropertyReference3() {
        String contents =
            "class A {\n" +
            "  Number value\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    this.@value\n" + // no such field
            "  }\n" +
            "}";
        assertUnknown(contents, "value");
    }

    @Test
    public void testSuperPropertyReference4() {
        String contents =
            "class A {\n" +
            "  Number value\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.value\n" +
            "  }\n" +
            "}";
        assertType(contents, "value", "java.lang.Number");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/996
    public void testSuperPropertyReference5() {
        String contents =
            "class A {\n" +
            "  Number value\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.@value\n" + // special case access
            "  }\n" +
            "}";
        assertType(contents, "value", "java.lang.Number");
    }

    @Test
    public void testSuperClassMethod1() {
        String contents =
            "class A {\n" +
            "  void method() {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "}\n" +
            "new B().method()\n";
        assertDeclaringType(contents, "method", "A");
    }

    @Test
    public void testSuperClassMethod2() {
        String contents =
            "class A {\n" +
            "  void method(Runnable r) {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "}\n" +
            "new B().method() { ->\n" +
            "}\n";
        assertDeclaringType(contents, "method", "A");
    }

    @Test
    public void testSuperClassMethod3() {
        String contents =
            "class A {\n" +
            "  protected void method() {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "  void something() {\n" +
            "    method()\n" +
            "  }\n" +
            "}";
        assertDeclaringType(contents, "method", "A");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperClassMethod4() {
        String contents =
            "class A {\n" +
            "  private void method() {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "  void something() {\n" +
            "    method()\n" +
            "  }\n" +
            "}";
        assertUnknown(contents, "method");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperClassMethod4a() {
        String contents =
            "class A {\n" +
            "  private void method() {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "  void something() {\n" +
            "    this.method()\n" +
            "  }\n" +
            "}";
        assertUnknown(contents, "method");
    }

    @Test
    public void testSuperClassMethod4b() {
        String contents =
            "class A {\n" +
            "  private void method() {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "  void something() {\n" +
            "    super.method()\n" + // this is ok
            "  }\n" +
            "}";
        assertDeclaringType(contents, "method", "A");
    }

    @Test
    public void testSuperClassMethod4c() {
        String contents =
            "class A {\n" +
            "  private void method() {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "  void something() {\n" +
            "    super.&method\n" + // GROOVY-8999: resolves to MethodClosure, but it NPEs when called
            "  }\n" +
            "}";
        assertDeclaringType(contents, "method", "A");
    }

    @Test
    public void testSuperClassMethod5() {
        String contents =
            "static void meth() {\n" +
            "  new Number() {\n" +
            "    int intValue() {\n" +
            "      super.intValue()\n" + // resolve "super" from non-static scope within static scope
            "    }\n" +
            "  }\n" +
            "}";
        assertType(contents, "super", "java.lang.Number");
        assertDeclaringType(contents, "intValue", "java.lang.Number");
    }

    @Test
    public void testSuperClassMethod5a() {
        String contents =
            "static void meth() {\n" +
            "  new Runnable() {\n" +
            "    void run() {\n" +
            "      super.run()\n" +
            "      super.hashCode()\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertDeclaringType(contents, "run", "java.lang.Runnable");
        assertType(contents, "super", "java.lang.Runnable");
        assertDeclaringType(contents, "hashCode", "java.lang.Object");
    }

    @Test
    public void testSuperClassMethod5b() {
        String contents =
            "static void meth() {\n" +
            "  new Object() {\n" +
            "    int hashCode() {\n" +
            "      super.hashCode()\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertDeclaringType(contents, "hashCode", "java.lang.Object");
        assertType(contents, "super", "groovy.lang.GroovyObject");
    }

    @Test
    public void testClassWithInitializer1() {
        String contents =
            "class C {\n" +
            "  static int x\n" +
            "  static {\n" +
            "    x = 42\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testClassWithInitializer2() {
        String contents =
            "class C {\n" +
            "  int x\n" +
            "  {\n" +
            "    x = 42\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testClassWithInitializer3() {
        String contents =
            "class C {\n" +
            "  int x\n" +
            "}\n" +
            "new C() {\n" +
            "  {\n" +
            "    x = 42\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testFieldWithInitializer1() {
        String contents =
            "class C {\n" +
            "  def x = 42\n" +
            "  def m() {\n" +
            "    x\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testFieldWithInitializer1a() {
        String contents =
            "class C {\n" +
            "  def m() {\n" +
            "    x\n" +
            "  }\n" +
            "  def x = 42\n" +
            "}\n";
        int offset = contents.indexOf('x');
        assertType(contents, offset, offset + 1, "java.lang.Integer");
    }

    @Test
    public void testFieldWithInitializer2() {
        String contents =
            "class C {\n" +
            "  static x = 42\n" +
            "  def m() {\n" +
            "    x\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testFieldWithInitializer2a() {
        String contents =
            "class C {\n" +
            "  def m() {\n" +
            "    x\n" +
            "  }\n" +
            "  static x = 42\n" +
            "}\n";
        int offset = contents.indexOf('x');
        assertType(contents, offset, offset + 1, "java.lang.Integer");
    }

    @Test
    public void testFieldWithInitializer3() {
        String contents =
            "class C {\n" +
            "  def x\n" +
            "  C() {\n" +
            "    x = 42\n" +
            "  }\n" +
            "  def m() {\n" +
            "    x\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testFieldWithInitializer3a() {
        String contents =
            "class C {\n" +
            "  C() {\n" +
            "    x = 42\n" +
            "  }\n" +
            "  def x\n" +
            "  def m() {\n" +
            "    x\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testFieldWithInitializer4() {
        String contents =
            "class C {\n" +
            "  def x\n" +
            "  {\n" +
            "    x = 42\n" +
            "  }\n" +
            "  def m() {\n" +
            "    x\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testFieldWithInitializer5() {
        String contents =
            "class C {\n" +
            "  static x\n" +
            "  static {\n" +
            "    x = 42\n" +
            "  }\n" +
            "  def m() {\n" +
            "    x\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test // GRECLIPSE-731
    public void testLocalWithInitializer1() {
        String contents = "def foo() {}\nString xxx = foo()\nxxx";
        assertType(contents, "xxx", "java.lang.String");
    }

    @Test // GRECLIPSE-731
    public void testLocalWithInitializer2() {
        String contents = "def foo() {}\ndef xxx = foo()\nxxx";
        assertType(contents, "xxx", "java.lang.Object");
    }

    @Test // GRECLIPSE-731
    public void testLocalWithInitializer3() {
        String contents = "String foo() {}\ndef xxx = foo()\nxxx";
        assertType(contents, "xxx", "java.lang.String");
    }

    @Test // GRECLIPSE-731
    public void testLocalWithInitializer4() {
        String contents = "int foo() {}\ndef xxx = foo()\nxxx";
        assertType(contents, "xxx", "java.lang.Integer");
    }

    @Test // GRECLIPSE-731
    public void testLocalWithInitializer5() {
        String contents = "def foo() {}\nString xxx\nxxx = foo()\nxxx";
        assertType(contents, "xxx", "java.lang.String");
    }

    @Test
    public void testElvisInitializer() {
        String contents = "def x = 2 ?: 1\nx";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testTernaryInitializer() {
        String contents = "def x = true ? 2 : 1\nx";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testRangeExpression1() {
        String contents = "def x = 0 .. 5\nx";
        assertType(contents, "x", "groovy.lang.Range<java.lang.Integer>");
    }

    @Test
    public void testRangeExpression2() {
        String contents = "def x = 0 ..< 5\nx";
        assertType(contents, "x", "groovy.lang.Range<java.lang.Integer>");
    }

    @Test
    public void testRangeExpression3() {
        String contents = "(1..10).getFrom()";
        assertType(contents, "getFrom", "java.lang.Integer");
    }

    @Test
    public void testRangeExpression4() {
        String contents = "(1..10).getTo()";
        assertType(contents, "getTo", "java.lang.Integer");
    }

    @Test
    public void testRangeExpression5() {
        String contents = "(1..10).step(0)";
        int start = contents.lastIndexOf("step");
        int end = start + "step".length();
        assertType(contents, start, end, "java.util.List<java.lang.Integer>");
    }

    @Test
    public void testRangeExpression6() {
        String contents = "(1..10).step(0, {})";
        assertType(contents, "step", "java.lang.Void");
    }

    @Test
    public void testInnerClass1() {
        String contents =
            "class Outer {\n" +
            "  class Inner {}\n" +
            "  Inner x\n" +
            "}\n" +
            "new Outer().x\n";
        assertType(contents, "x", "Outer$Inner");
    }

    @Test
    public void testInnerClass2() {
        String contents =
            "class Outer {\n" +
            "  class Inner {\n" +
            "    class InnerInner {}\n" +
            "  }\n" +
            "  Outer.Inner.InnerInner x\n" +
            "}\n" +
            "new Outer().x\n";
        assertType(contents, "x", "Outer$Inner$InnerInner");
    }

    @Test
    public void testInnerClass3() {
        String contents =
            "class Outer {\n" +
            "  Inner x\n" +
            "  class Inner {\n" +
            "    def z() {\n" +
            "      new Outer().x\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "x", "Outer$Inner");
    }

    @Test
    public void testInnerClass4() {
        String contents =
            "class Outer {\n" +
            "  Inner x\n" +
            "  class Inner {\n" +
            "    class InnerInner {\n" +
            "      def z() {\n" +
            "        new Outer().x\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "x", "Outer$Inner");
    }

    @Test
    public void testInnerClass5() {
        String contents =
            "class Outer {\n" +
            "  class Inner extends Outer {}\n" +
            "}\n";
        assertType(contents, "Outer", "Outer");
    }

    @Test
    public void testInnerClass6() {
        String contents =
            "class Outer extends RuntimeException {\n" +
            "  class Inner {\n" +
            "    def foo() throws Outer {}\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "Outer", "Outer");
    }

    @Test
    public void testInnerClass7() {
        String contents = "class A {\n" +
            "  protected Number f\n" +
            "  protected Number m() {}\n" +
            "}\n" +
            "class AA extends A {\n" +
            "  class AAA {\n" +
            "    def x() {\n" +
            "      f  \n" +
            "      m()\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        assertDeclaringType(contents, "f", "A");
        assertType(contents, "f", "java.lang.Number");
        assertDeclaringType(contents, "m", "A");
        assertType(contents, "m", "java.lang.Number");
    }

    @Test
    public void testInnerClass8() {
        String contents = "class A {\n" +
            "  protected Number f\n" +
            "  protected Number m() {}\n" +
            "}\n" +
            "class AA extends A {\n" +
            "  static class AAA {\n" +
            "    def x() {\n" +
            "      f  \n" +
            "      m()\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "f", "java.lang.Object");
        assertUnknown(contents, "f");
        assertType(contents, "m", "java.lang.Object");
        assertUnknown(contents, "m");
    }

    @Test
    public void testInnerClass9() {
        String contents = "class A {\n" +
            "  public static final Number N = 1\n" +
            "}\n" +
            "class AA extends A {\n" +
            "  static class AAA {\n" +
            "    def x() {\n" +
            "      N\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        assertDeclaringType(contents, "N", "A");
        assertType(contents, "N", "java.lang.Number");
    }

    @Test
    public void testInnerClass10() {
        String contents = "class A {\n" +
            "  public static final Number N = 1\n" +
            "}\n" +
            "class AA extends A {\n" +
            "  static class AAA {\n" +
            "    def x() {\n" +
            "      def cl = { ->\n" +
            "        N\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        assertDeclaringType(contents, "N", "A");
        assertType(contents, "N", "java.lang.Number");
    }

    @Test
    public void testInnerClass11() {
        String contents = "class A {\n" +
            "  public static final Number N = 1\n" +
            "}\n" +
            "class AA extends A {\n" +
            "  static class AAA {\n" +
            "    static def x() {\n" +
            "      def cl = { ->\n" +
            "        N\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        assertDeclaringType(contents, "N", "A");
        assertType(contents, "N", "java.lang.Number");
    }

    @Test
    public void testAnonInner1() {
        String contents = "def foo = new Object() {}";
        assertType(contents, "Object", "java.lang.Object");
    }

    @Test
    public void testAnonInner2() {
        String contents = "def foo = new Runnable() { void run() {}}";
        assertType(contents, "Runnable", "java.lang.Runnable");
    }

    @Test
    public void testAnonInner3() {
        String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) {}}";
        assertType(contents, "Comparable", "java.lang.Comparable<java.lang.String>");
    }

    @Test
    public void testAnonInner4() {
        String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) { compareTo()}}";
        assertDeclaringType(contents, "compareTo", "Search$1");
    }

    @Test
    public void testAnonInner5() {
        String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) {}}\n" +
            "foo.compareTo('one', 'two')";
        assertDeclaringType(contents, "compareTo", "java.lang.Comparable<java.lang.String>");
    }

    @Test
    public void testAnonInner6() {
        String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) {}}\n" +
            "foo = new Comparable<String>() { int compareTo(String a, String b) {}}\n" +
            "foo.compareTo('one', 'two')";
        assertDeclaringType(contents, "compareTo", "java.lang.Comparable<java.lang.String>");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/378
    public void testAnonInner7() {
        String contents =
            "class A {\n" +
            "  protected def f\n" +
            "  protected def m() {}\n" +
            "}\n" +
            "class AA extends A {\n" +
            "  void init() {\n" +
            "    def whatever = new Object() {\n" +
            "      def something() {\n" +
            "        f  \n" +
            "        m()\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertDeclaringType(contents, "f", "A");
        assertDeclaringType(contents, "m", "A");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/383
    public void testAnonInner8() {
        String contents =
            "class A {\n" +
            "  protected def m() {}\n" +
            "  def p = new Object() {\n" +
            "    def meth() {\n" +
            "      m();\n" +
            "      p  ;\n" +
            "    }\n" +
            "  }\n" +
            "  void init() {\n" +
            "    def whatever = new Object() {\n" +
            "      def something() {\n" +
            "        m()\n" +
            "        p  \n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("m();");
        assertDeclaringType(contents, offset, offset + 1, "A");
            offset = contents.indexOf("p  ;");
        assertDeclaringType(contents, offset, offset + 1, "A");
            offset = contents.lastIndexOf("m");
        assertDeclaringType(contents, offset, offset + 1, "A");
            offset = contents.lastIndexOf("p");
        assertDeclaringType(contents, offset, offset + 1, "A");
    }

    @Test
    public void testCatchBlock1() {
        String catchString = "try {\n} catch (NullPointerException e) {\n e\n}";
        int start = catchString.lastIndexOf("NullPointerException");
        int end = start + "NullPointerException".length();
        assertType(catchString, start, end, "java.lang.NullPointerException");
    }

    @Test
    public void testCatchBlock2() {
        String catchString = "try {\n} catch (NullPointerException e) {\n e\n}";
        int start = catchString.lastIndexOf("e");
        int end = start + 1;
        assertType(catchString, start, end, "java.lang.NullPointerException");
    }

    @Test
    public void testCatchBlock3() {
        String catchString = "try {\n} catch (NullPointerException e) {\n e\n}";
        int start = catchString.indexOf("NullPointerException e");
        int end = start + ("NullPointerException e").length();
        assertType(catchString, start, end, "java.lang.NullPointerException");
    }

    @Test
    public void testCatchBlock4() {
        String catchString2 = "try {\n} catch (e) {\n e\n}";
        int start = catchString2.indexOf("e");
        int end = start + 1;
        assertType(catchString2, start, end, "java.lang.Exception");
    }

    @Test
    public void testCatchBlock5() {
        String catchString2 = "try {\n} catch (e) {\n e\n}";
        int start = catchString2.lastIndexOf("e");
        int end = start + 1;
        assertType(catchString2, start, end, "java.lang.Exception");
    }

    @Test
    public void testStaticImports1() {
        String contents =
            "import static javax.swing.text.html.HTML.NULL_ATTRIBUTE_VALUE\n" +
            "NULL_ATTRIBUTE_VALUE";
        assertType(contents, "NULL_ATTRIBUTE_VALUE", "java.lang.String");
    }

    @Test
    public void testStaticImports2() {
        String contents =
            "import static javax.swing.text.html.HTML.getAttributeKey\n" +
            "getAttributeKey('')";
        assertType(contents, "getAttributeKey('')", "javax.swing.text.html.HTML$Attribute");
    }

    @Test
    public void testStaticImports3() {
        String contents =
            "import static javax.swing.text.html.HTML.*\n" +
            "NULL_ATTRIBUTE_VALUE";
        assertType(contents, "NULL_ATTRIBUTE_VALUE", "java.lang.String");
    }

    @Test
    public void testStaticImports4() {
        String contents =
            "import static javax.swing.text.html.HTML.*\n" +
            "getAttributeKey('')";
        assertType(contents, "getAttributeKey('')", "javax.swing.text.html.HTML$Attribute");
    }

    private static final String CONTENTS_GETAT1 =
        "class GetAt {\n" +
        "  String getAt(foo) {}\n" +
        "}\n" +
        "\n" +
        "new GetAt()[0].startsWith()\n" +
        "GetAt g\n" +
        "g[0].startsWith()";

    private static final String CONTENTS_GETAT2 =
        "class GetAt {\n" +
        "}\n" +
        "\n" +
        "new GetAt()[0].startsWith()\n" +
        "GetAt g\n" +
        "g[0].startsWith()";

    @Test
    public void testGetAt1() {
        int start = CONTENTS_GETAT1.indexOf("startsWith");
        int end = start + "startsWith".length();
        assertDeclaringType(CONTENTS_GETAT1, start, end, "java.lang.String");
    }

    @Test
    public void testGetAt2() {
        int start = CONTENTS_GETAT1.lastIndexOf("startsWith");
        int end = start + "startsWith".length();
        assertDeclaringType(CONTENTS_GETAT1, start, end, "java.lang.String");
    }

    @Test
    public void testGetAt3() {
        int start = CONTENTS_GETAT2.indexOf("startsWith");
        int end = start + "startsWith".length();
        // expecting unknown confidence because getAt not explicitly defined
        assertDeclaringType(CONTENTS_GETAT2, start, end, "GetAt", true);
    }

    @Test
    public void testGetAt4() {
        int start = CONTENTS_GETAT2.lastIndexOf("startsWith");
        int end = start + "startsWith".length();
        // expecting unknown confidence because getAt not explicitly defined
        assertDeclaringType(CONTENTS_GETAT2, start, end, "GetAt", true);
    }

    @Test // GRECLIPSE-743
    public void testGetAt5() {
        String contents = "class A {}\n new A().getAt() ";
        int start = contents.lastIndexOf("getAt");
        int end = start + "getAt".length();
        assertType(contents, start, end, "java.lang.Object");
        assertDeclaringType(contents, start, end, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test
    public void testGetAt6() {
        String contents = "class A {\n A getAt(prop) { \n new A()\n}}\n new A().getAt('x')";
        int start = contents.lastIndexOf("getAt");
        int end = start + "getAt".length();
        assertType(contents, start, end, "A");
        assertDeclaringType(contents, start, end, "A");
    }

    @Test
    public void testGetAt7() {
        String contents = "class A {\n A getAt(prop) {\n new A()\n}}\n class B extends A {}\n new B().getAt('x')";
        int start = contents.lastIndexOf("getAt");
        int end = start + "getAt".length();
        assertType(contents, start, end, "A");
        assertDeclaringType(contents, start, end, "A");
    }

    @Test // CommandRegistry.commands() returns List<Command>
    public void testGetAt8() {
        assumeFalse(isAtLeastGroovy(25)); // requires subproject groovy-groovysh

        String contents =
            "import org.codehaus.groovy.tools.shell.CommandRegistry\n" +
            "def registry = new CommandRegistry()\n" +
            "def result = registry.commands()[0]\n";
        assertType(contents, "result", "org.codehaus.groovy.tools.shell.Command");
    }

    @Test // CommandRegistry.iterator() returns Iterator
    public void testGetAt9() {
        assumeFalse(isAtLeastGroovy(25)); // requires subproject groovy-groovysh

        String contents =
            "import org.codehaus.groovy.tools.shell.CommandRegistry\n" +
            "def registry = new CommandRegistry()\n" +
            "def result = registry.iterator()[0]\n";
        assertType(contents, "result", "java.lang.Object");
    }

    @Test
    public void testListSort1() {
        String contents =
            "def list = []\n" +
            "list.sort()\n";
        int offset = contents.lastIndexOf("sort");
        assertType(contents, offset, offset + 4, "java.util.List<java.lang.Object>");
        assertDeclaringType(contents, offset, offset + 4, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/387
    public void testListSort2() {
        String contents =
            "def list = []\n" +
            "list.sort { a, b ->\n" +
            "  a <=> b\n" +
            "}\n";
        int offset = contents.lastIndexOf("sort");
        assertType(contents, offset, offset + 4, "java.util.List<java.lang.Object>");
        MethodNode m = assertDeclaration(contents, offset, offset + 4, "org.codehaus.groovy.runtime.DefaultGroovyMethods", "sort", DeclarationKind.METHOD);
        assertEquals("Should resolve to sort(Iterable,Closure) since Collection version is deprecated", "java.lang.Iterable<java.lang.Object>", printTypeName(m.getParameters()[0].getType()));
    }

    @Test
    public void testListSort3() {
        // Java 8 adds default method sort(Comparator) to the List interface
        boolean jdkListSort;
        try {
            List.class.getDeclaredMethod("sort", Comparator.class);
            jdkListSort = true;
        } catch (Exception e) {
            jdkListSort = false;
        }

        String contents =
            "def list = []\n" +
            "list.sort({ a, b ->\n" +
            "  a <=> b\n" +
            "} as Comparator)\n";
        int offset = contents.lastIndexOf("sort");
        assertType(contents, offset, offset + 4, jdkListSort ? "java.lang.Void" : "java.util.List<java.lang.Object>");
        assertDeclaringType(contents, offset, offset + 4, jdkListSort ? "java.util.List<java.lang.Object>" : "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/368
    public void testListRemove() {
        String contents =
            "class Util {\n" +
            "  static int remove(List<?> list, Object item) {\n" +
            "    //...\n" +
            "  }\n" +
            "  void doSomething() {\n" +
            "    List a = []\n" +
            "    List b = []\n" +
            "    // List.remove(int) or List.remove(Object)\n" +
            "    a.remove(Util.remove(b, ''))\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("a.remove") + 2;
        MethodNode m = assertDeclaration(contents, offset, offset + "remove".length(), "java.util.List<java.lang.Object>", "remove", DeclarationKind.METHOD);
        assertEquals("Should resolve to remove(int) due to return type of inner call", "int", printTypeName(m.getParameters()[0].getType()));
    }

    @Test // GRECLIPSE-1013
    public void testCategoryMethodAsProperty() {
        String contents = "''.toURL().text";
        assertDeclaringType(contents, "text", "org.codehaus.groovy.runtime.ResourceGroovyMethods");
    }

    @Test
    public void testInterfaceMethodsAsProperty() throws Exception {
        createUnit("foo", "Bar", "package foo; interface Bar {\n def getOne()\n}\n");
        createUnit("foo", "Baz", "package foo; interface Baz extends Bar {\n def getTwo()\n}\n");

        String contents = "def meth(foo.Baz b) {\n b.one + b.two\n}";

        int start = contents.indexOf("one");
        assertDeclaringType(contents, start, start + 3, "foo.Bar");
        start = contents.indexOf("two");
        assertDeclaringType(contents, start, start + 3, "foo.Baz");
    }

    @Test
    public void testInterfaceMethodAsProperty2() throws Exception {
        createUnit("foo", "Bar", "package foo; interface Bar {\n def getOne()\n}\n");
        createUnit("foo", "Baz", "package foo; abstract class Baz implements Bar {\n abstract def getTwo()\n}\n");

        String contents = "def meth(foo.Baz b) {\n b.one + b.two\n}";

        int start = contents.indexOf("one");
        assertDeclaringType(contents, start, start + 3, "foo.Bar");
        start = contents.indexOf("two");
        assertDeclaringType(contents, start, start + 3, "foo.Baz");
    }

    @Test
    public void testInterfaceMethodAsProperty3() throws Exception {
        createUnit("foo", "Bar", "package foo; interface Bar {\n def getOne()\n}\n");
        createUnit("foo", "Baz", "package foo; abstract class Baz implements Bar {\n abstract def getTwo()\n}\n");

        String contents = "abstract class C extends foo.Baz {}\ndef meth(C c) {\n c.one + c.two\n}\n";

        int start = contents.indexOf("one");
        assertDeclaringType(contents, start, start + 3, "foo.Bar");
        start = contents.indexOf("two");
        assertDeclaringType(contents, start, start + 3, "foo.Baz");
    }

    @Test
    public void testIndirectInterfaceMethod() throws Exception {
        createUnit("foo", "Bar", "package foo; interface Bar {\n def getOne()\n}\n");
        createUnit("foo", "Baz", "package foo; abstract class Baz implements Bar {\n abstract def getTwo()\n}\n");

        String contents = "abstract class C extends foo.Baz {}\ndef meth(C c) {\n c.getOne() + c.getTwo()\n}\n";

        int start = contents.indexOf("getOne");
        assertDeclaringType(contents, start, start + 6, "foo.Bar");
        start = contents.indexOf("getTwo");
        assertDeclaringType(contents, start, start + 6, "foo.Baz");
    }

    @Test
    public void testIndirectInterfaceConstant() throws Exception {
        createUnit("I", "interface I {\n Number ONE = 1\n}\n");
        createUnit("A", "abstract class A implements I {\n Number TWO = 2\n}\n");

        String contents = "abstract class B extends A {}\nB b; b.ONE; b.TWO\n";

        int start = contents.indexOf("ONE");
        assertDeclaringType(contents, start, start + 3, "I");
        start = contents.indexOf("TWO");
        assertDeclaringType(contents, start, start + 3, "A");
    }

    @Test
    public void testObjectMethodOnInterface() {
        // Object is not in explicit type hierarchy of List
        String contents = "def meth(List list) {\n list.getClass()\n}\n";

        String target = "getClass", source = "java.lang.Object";
        assertDeclaringType(contents, contents.indexOf(target), contents.indexOf(target) + target.length(), source);
    }

    @Test
    public void testObjectMethodOnInterfaceAsProperty() {
        // Object is not in explicit type hierarchy of List
        String contents = "def meth(List list) {\n list.class\n}\n";

        String target = "class", source = "java.lang.Object";
        assertDeclaringType(contents, contents.indexOf(target), contents.indexOf(target) + target.length(), source);
    }

    @Test
    public void testMultiDecl1() {
        String contents = "def (x, y) = []\nx\ny";
        assertType(contents, "x", "java.lang.Object");
        assertType(contents, "y", "java.lang.Object");
    }

    @Test
    public void testMultiDecl2() {
        String contents = "def (x, y) = [1]\nx\ny";
        assertType(contents, "x", "java.lang.Integer");
        assertType(contents, "y", "java.lang.Integer");
    }

    @Test
    public void testMultiDecl3() {
        String contents = "def (x, y) = [1,1]\nx\ny";
        assertType(contents, "x", "java.lang.Integer");
        assertType(contents, "y", "java.lang.Integer");
    }

    @Test
    public void testMultiDecl4() {
        String contents = "def (x, y) = [1,'']\nx\ny";
        assertType(contents, "x", "java.lang.Integer");
        assertType(contents, "y", "java.lang.String");
    }

    @Test
    public void testMultiDecl6() {
        String contents = "def (x, y) = new ArrayList()\nx\ny";
        assertType(contents, "x", "java.lang.Object");
        assertType(contents, "y", "java.lang.Object");
    }

    @Test
    public void testMultiDecl7() {
        String contents = "def (x, y) = new ArrayList<Double>()\nx\ny";
        assertType(contents, "x", "java.lang.Double");
        assertType(contents, "y", "java.lang.Double");
    }

    @Test
    public void testMultiDecl8() {
        String contents = "Double[] meth() {}\ndef (x, y) = meth()\nx\ny";
        assertType(contents, "x", "java.lang.Double");
        assertType(contents, "y", "java.lang.Double");
    }

    @Test
    public void testMultiDecl9() {
        String contents = "List<Double> meth() {}\ndef (x, y) = meth()\nx\ny";
        assertType(contents, "x", "java.lang.Double");
        assertType(contents, "y", "java.lang.Double");
    }

    @Test
    public void testMultiDecl10() {
        String contents = "List<Double> field\ndef (x, y) = field\nx\ny";
        assertType(contents, "x", "java.lang.Double");
        assertType(contents, "y", "java.lang.Double");
    }

    @Test
    public void testMultiDecl11() {
        String contents = "List<Double> field\ndef x\ndef y\n (x, y)= field\nx\ny";
        assertType(contents, "x", "java.lang.Double");
        assertType(contents, "y", "java.lang.Double");
    }

    @Test
    public void testMultiDecl12() {
        String contents = "def (x, y) = 1d\nx\ny";
        assertType(contents, "x", "java.lang.Double");
        assertType(contents, "y", "java.lang.Double");
    }

    @Test
    public void testMultiDecl13() {
        String contents = "def (int x, float y) = [1,2]\nx\ny";
        assertType(contents, "x", "java.lang.Integer");
        assertType(contents, "y", "java.lang.Float");
    }

    @Test // GRECLIPSE-1174 groovy casting
    public void testAsExpression1() {
        String contents = "(1 as int).intValue()";
        assertType(contents, "intValue", "java.lang.Integer");
    }

    @Test // GRECLIPSE-1174 groovy casting
    public void testAsExpression2() {
        String contents = "class Flar { int x\n}\n(null as Flar).x";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar1() {
        String contents =
            "class SettingUndeclaredProperty {\n" +
            "    public void mymethod() {\n" +
            "        doesNotExist = \"abc\"\n" +
            "    }\n" +
            "}";
        assertUnknown(contents, "doesNotExist");
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar2() {
        String contents =
            "class SettingUndeclaredProperty {\n" +
            "     def r = {\n" +
            "        doesNotExist = 0\n" +
            "    }\n" +
            "}";
        assertUnknown(contents, "doesNotExist");
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar3() {
        String contents =
            "doesNotExist";
        assertUnknown(contents, "doesNotExist");
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar4() {
        String contents = "doesNotExist = 9";
        assertDeclaringType(contents, "doesNotExist", DEFAULT_UNIT_NAME);
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar5() {
        String contents =
            "doesNotExist = 9\n" +
            "def x = {doesNotExist;}";
        assertDeclaringType(contents, "doesNotExist", DEFAULT_UNIT_NAME);
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar6() {
        String contents =
            "def x = {\n" +
            "  doesNotExist = 9\n" +
            "  doesNotExist\n" +
            "}";
        assertDeclaringType(contents, "doesNotExist", DEFAULT_UNIT_NAME);
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar7() {
        String contents =
            "def z() {\n" +
            "  doesNotExist = 9\n" +
            "}\n";
        assertUnknown(contents, "doesNotExist");
    }

    @Test // nested expressions of various forms
    public void testNested1() {
        String contents =
            "(true ? 2 : 7) + 9";
        assertType(contents, "java.lang.Integer");
    }

    @Test // nested expressions of various forms
    public void testNested2() {
        String contents =
            "(true ? 2 : 7) + (true ? 2 : 7)";
        assertType(contents, "java.lang.Integer");
    }

    @Test // nested expressions of various forms
    public void testNested3() {
        String contents =
            "(8 ?: 7) + (8 ?: 7)";
        assertType(contents, "java.lang.Integer");
    }

    @Test // nested expressions of various forms
    public void testNested4() {
        createUnit("Foo", "class Foo { int prop;}");

        String contents = "(new Foo().@prop) + (8 ?: 7)";
        assertType(contents, "java.lang.Integer");
    }

    @Test
    public void testPostfix() {
        String contents =
            "int i = 0\n" +
            "def list = [0]\n" +
            "list[i]++";
        assertType(contents, "i", "java.lang.Integer");
    }

    @Test // GRECLIPSE-1302
    public void testNothingIsUnknown() {
        assertNoUnknowns(
            "1 > 4\n" +
            "1 < 1\n" +
            "1 >= 1\n" +
            "1 <= 1\n" +
            "1 <=> 1\n" +
            "1 == 1\n" +
            "[1,9][0]");
    }

    @Test
    public void testNothingIsUnknownWithCategories() {
        assertNoUnknowns(
            "class Me {\n" +
            "    def meth() {\n" +
            "        use (MeCat) {\n" +
            "            println getVal()\n" +
            "            println val\n" +
            "        }\n" +
            "    }\n" +
            "} \n" +
            "\n" +
            "class MeCat { \n" +
            "    static String getVal(Me self) {\n" +
            "        \"val\"\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "use (MeCat) {\n" +
            "    println new Me().getVal()\n" +
            "    println new Me().val\n" +
            "}\n" +
            "new Me().meth()");
    }

    @Test // GRECLIPSE-1304
    public void testNoGString() {
        assertNoUnknowns("'$'\n'${}'\n'${a}'\n'$a'");
    }

    @Test // GRECLIPSE-1341
    public void testDeclarationAtBeginningOfMethod() {
        String contents =
            "class Problem2 {\n" +
            "  String action() {}\n" +
            "  def meth() {\n" +
            "    def x = action()\n" +
            "    x.substring()\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "substring", "java.lang.String");
    }

    @Test // GRECLIPSE-1638
    public void testInstanceOf1() {
        String contents =
            "def m(Object obj) {\n" +
            "  def val = obj\n" +
            "  if (val instanceof String) {\n" +
            "    println val.trim()\n" +
            "  }\n" +
            "  val\n" +
            "}\n";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test // GRECLIPSE-1638
    public void testInstanceOf1a() {
        String contents =
            "def m(Object obj) {\n" +
            "  def val = obj\n" +
            "  if (val == null || val instanceof String) {\n" +
            "    println val.trim()\n" +
            "  }\n" +
            "  val\n" +
            "}\n";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test // GRECLIPSE-1638
    public void testInstanceOf1b() {
        String contents =
            "def m(Object obj) {\n" +
            "  def val = obj\n" +
            "  if (val != null && val instanceof String) {\n" +
            "    println val.trim()\n" +
            "  }\n" +
            "  val\n" +
            "}\n";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf2() {
        String contents =
            "def m(Object obj) {\n" +
            "  def val = obj\n" +
            "  if (!(val instanceof String)) {\n" +
            "    println val\n" +
            "  }\n" +
            "  val\n" +
            "}\n";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf2a() {
        assumeTrue(isParrotParser());

        String contents =
            "def m(Object obj) {\n" +
            "  def val = obj\n" +
            "  if (val !instanceof String) {\n" +
            "    println val\n" +
            "  }\n" +
            "  val\n" +
            "}\n";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf3() {
        String contents =
            "def m(Object obj) {\n" +
            "  def val = obj\n" +
            "  if (!(val instanceof String)) {\n" +
            "    println val\n" +
            "  } else {\n" +
            "    val\n" +
            "  }\n" +
            "  val\n" +
            "}\n";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf3a() {
        assumeTrue(isParrotParser());

        String contents =
            "def m(Object obj) {\n" +
            "  def val = obj\n" +
            "  if (val !instanceof String) {\n" +
            "    println val\n" +
            "  } else {\n" +
            "    val\n" +
            "  }\n" +
            "  val\n" +
            "}\n";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf4() {
        String contents =
            "def m(Object obj) {\n" +
            "  def val = obj\n" +
            "  if (val instanceof String) {\n" +
            "    println val.trim()\n" +
            "  }\n" +
            "  val\n" +
            "  def var = obj\n" +
            "  if (var instanceof Integer) {\n" +
            "    println var.intValue()\n" +
            "  }\n" +
            "  var\n" +
            "}\n";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("var");
        end = start + "var".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("var", end + 1);
        end = start + "var".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("var", end + 1);
        end = start + "var".length();
        assertType(contents, start, end, "java.lang.Integer");

        start = contents.indexOf("var", end + 1);
        end = start + "var".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf5() {
        String contents =
            "def val = new Object()\n" +
            "if (val instanceof Number) {\n" +
            "  val\n" +
            "} else if (val instanceof CharSequence) {\n" +
            "  val\n" +
            "}\n" +
            "val";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Number");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.CharSequence");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf6() {
        String contents =
            "def val = new Object()\n" +
            "if (val instanceof Number || val instanceof CharSequence) {\n" +
            "  println val\n" +
            "}\n" +
            "val";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf7() {
        String contents =
            "def val = new Object()\n" +
            "if (val instanceof Number) {\n" +
            "  if (val instanceof Double) {\n" +
            "    val\n" +
            "  }\n" +
            "}\n" +
            "val";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Number");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Double");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf8() {
        String contents =
            "def val = new Object()\n" +
            "if (val instanceof String) {\n" +
            "  if (val instanceof CharSequence) {\n" +
            "    val\n" +
            "  }\n" +
            "}\n" +
            "val";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf9() {
        String contents =
            "def val\n" +
            "def str = val instanceof String ? val : val.toString()\n" +
            "val";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf10() {
        String contents =
            "def val\n" +
            "def str = !(val instanceof String) ? val.toString() : val\n" +
            "val";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf10a() {
        assumeTrue(isParrotParser());

        String contents =
            "def val\n" +
            "def str = val !instanceof String ? val.toString() : val\n" +
            "val";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf11() {
        String contents =
            "def val = File.createTempDir()\n" +
            "if (!val.exists()) val = ''.toURL()\n" +
            "def str = val instanceof File ? val.canonicalPath : val.toString()";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.io.File");

        // line 2
        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.io.File");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.net.URL");

        // line 3
        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.io.Serializable");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.io.File");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.io.Serializable");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/977
    public void testInstanceOf12() {
        String contents =
            "class C {\n" +
            "  private Number value = 42\n" +
            "  boolean equals(Object that) {\n" +
            "    that instanceof C && this.value.equals(that.value)\n" +
            "  }\n" +
            "}\n";

        int offset = contents.indexOf("that instanceof");
        assertType(contents, offset, offset + 4, "java.lang.Object");

        offset = contents.indexOf("value.equals");
        assertType(contents, offset, offset + 5, "java.lang.Number");

        offset = contents.lastIndexOf("that");
        assertType(contents, offset, offset + 4, "C");

        offset = contents.lastIndexOf("value");
        assertType(contents, offset, offset + 5, "java.lang.Number");
    }

    @Test
    public void testSwitchClassCase1() {
        String contents =
            "void test(obj) {\n" +
            "  switch (obj) {\n" +
            "   case Number:\n" +
            "    obj\n" +
            "    break\n" +
            "   case String:\n" +
            "    obj\n" +
            "  }\n" +
            "  obj\n" +
            "}\n";

        int offset = contents.lastIndexOf("obj)");
        assertType(contents, offset, offset + 3, "java.lang.Object");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.Number");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.String");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.Object");
    }

    @Test
    public void testSwitchClassCase2() {
        String contents =
            "void test(obj) {\n" +
            "  switch (obj) {\n" +
            "   case Number:\n" +
            "    obj\n" +
            "    return\n" +
            "   case String:\n" +
            "    obj\n" +
            "  }\n" +
            "  obj\n" +
            "}\n";

        int offset = contents.lastIndexOf("obj)");
        assertType(contents, offset, offset + 3, "java.lang.Object");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.Number");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.String");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.Object");
    }

    @Test
    public void testSwitchClassCase3() {
        String contents =
            "void test(obj) {\n" +
            "  switch (obj) {\n" +
            "   case Number:\n" +
            "    obj\n" +
            "    throw new Exception()\n" +
            "   case String:\n" +
            "    obj\n" +
            "  }\n" +
            "  obj\n" +
            "}\n";

        int offset = contents.lastIndexOf("obj)");
        assertType(contents, offset, offset + 3, "java.lang.Object");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.Number");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.String");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.Object");
    }

    @Test
    public void testSwitchClassCase4() {
        String contents =
            "void test(obj) {\n" +
            "  for (i in 1..3) {\n" +
            "    switch (obj) {\n" +
            "     case Number:\n" +
            "      obj\n" +
            "      continue\n" +
            "     case String:\n" +
            "      obj\n" +
            "    }\n" +
            "    obj\n" +
            "  }\n" +
            "}\n";

        int offset = contents.lastIndexOf("obj)");
        assertType(contents, offset, offset + 3, "java.lang.Object");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.Number");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.String");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.Object");
    }

    @Test
    public void testSwitchClassCase5() {
        String contents =
            "void test(obj) {\n" +
            "  switch (obj) {\n" +
            "   case Number:\n" +
            "    obj\n" +
            "   case String:\n" +
            "    obj\n" +
            "  }\n" +
            "  obj\n" +
            "}\n";

        int offset = contents.lastIndexOf("obj)");
        assertType(contents, offset, offset + 3, "java.lang.Object");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.Number");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.io.Serializable");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.Object");
    }

    @Test
    public void testSwitchClassCase6() {
        String contents =
            "void test(obj) {\n" +
            "  switch (obj) {\n" +
            "   case Number:\n" +
            "    obj\n" +
            "   default:\n" +
            "    obj\n" +
            "  }\n" +
            "  obj\n" +
            "}\n";

        int offset = contents.lastIndexOf("obj)");
        assertType(contents, offset, offset + 3, "java.lang.Object");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.Number");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.Object");

        offset = contents.indexOf("obj", offset + 1);
        assertType(contents, offset, offset + 3, "java.lang.Object");
    }

    @Test
    public void testThisInInnerClass() {
        String contents =
            "class A {\n" +
            "    Number source = null\n" +
            "    def user = new Object() {\n" +
            "        def field = A.this.source\n" +
            "    }\n" +
            "}\n";

        assertType(contents, "this", "A");
        assertType(contents, "source", "java.lang.Number");
    }

    @Test // GRECLIPSE-1798
    public void testFieldAndPropertyWithSameName() {
        createJavaUnit("Wrapper",
            "public class Wrapper<T> {\n" +
            "  private final T wrapped;\n" +
            "  public Wrapper(T wrapped) { this.wrapped = wrapped;}\n" +
            "  public T getWrapped() { return wrapped;}\n" +
            "}");
        createJavaUnit("MyBean",
            "public class MyBean {\n" +
            "  private Wrapper<String> foo = new Wrapper<>(\"foo\");\n" +
            "  public String getFoo() { return foo.getWrapped();}\n" +
            "}");

        String contents =
            "final class GroovyTest {\n" +
            "  static void main(String[] args) {\n" +
            "    def b = new MyBean()\n" +
            "    println b.foo.toUpperCase()\n" +
            "  }\n" +
            "}";
        assertType(contents, "foo", "java.lang.String");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/355
    public void testLocalTypeAndDefaultImportCollision() {
        createJavaUnit("domain", "Calendar",
            "public class Calendar { public static Calendar instance() { return null;}}");

        String contents = "def cal = domain.Calendar.instance()";
        assertType(contents, "instance", "domain.Calendar");
        assertType(contents, "cal", "domain.Calendar");
    }

    @Test
    public void testMethodOverloadsArgumentMatching1() {
        createJavaUnit("MyEnum", "enum MyEnum { A, B;}");

        String contents = "class Issue405 {\n" +
            "  void meth(String s, MyEnum e) {\n" +
            "    def d1, d2\n" +
            "    switch (e) {\n" +
            "    case MyEnum.A:\n" +
            "      d1 = new Date()\n" +
            "      d2 = new Date()\n" +
            "      break\n" +
            "    case MyEnum.B:\n" +
            "      d1 = null\n" +
            "      d2 = null\n" +
            "      break\n" +
            "    }\n" +
            "    meth(s, d1, d2)\n" +
            "  }\n" +
            "  void meth(String s, Date d1, Date d2) {\n" +
            "  }\n" +
            "  void meth(String s, Object o1, Object o2) {\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("meth(s");
        MethodNode m = assertDeclaration(contents, offset, offset + 4, "Issue405", "meth", DeclarationKind.METHOD);
        assertTrue("Expected 'meth(String, Object, Object)' but was 'meth(String, MyEnum)' or 'meth(String, Date, Date)'",
            m.getParameters().length == 3 && m.getParameters()[2].getType().getNameWithoutPackage().equals("Object"));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/405
    public void testMethodOverloadsArgumentMatching2() {
        createJavaUnit("MyEnum", "enum MyEnum { A, B;}");

        String contents = "class Issue405 {\n" +
            "  void meth(String s, MyEnum e) {\n" +
            "    def d1, d2\n" +
            "    switch (e) {\n" +
            "    case MyEnum.A:\n" +
            "      d1 = new Date()\n" +
            "      d2 = new Date()\n" +
            "      break\n" +
            "    case MyEnum.B:\n" +
            "      d1 = null\n" +
            "      d2 = null\n" +
            "      break\n" +
            "    }\n" +
            "    meth(s, d1, d2)\n" +
            "  }\n" +
            "  void meth(String s, Date d1, Date d2) {\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("meth(s");
        MethodNode m = assertDeclaration(contents, offset, offset + 4, "Issue405", "meth", DeclarationKind.METHOD);
        assertEquals("Expected 'meth(String, Date, Date)' but was 'meth(String, MyEnum)'", 3, m.getParameters().length);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/644
    public void testMethodOverloadsArgumentMatching3() {
        String contents = "Arrays.toString(new Object())";

        String target = "toString";
        int offset = contents.indexOf(target);
        MethodNode m = assertDeclaration(contents, offset, offset + target.length(), "java.util.Arrays", "toString", DeclarationKind.METHOD);

        String arrayType = m.getParameters()[0].getType().toString(false);
        assertEquals("Expected '" + target + "(Object[])' but was '" + target + "(" + arrayType + ")'", "java.lang.Object[]", arrayType);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/801
    public void testMethodOverloadsArgumentMatching4() {
        String contents = "class Foo {\n" +
            "  Foo() {\n" +
            "    bar = 1\n" +
            "  }\n" +
            "  void setBar(Date date) {\n" +
            "  }\n" +
            "  void setBar(int value) {\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("bar = ");
        MethodNode m = assertDeclaration(contents, offset, offset + 3, "Foo", "setBar", DeclarationKind.METHOD);
        assertEquals("Expected 'setBar(int)' but was 'setBar(Date)'", "int", m.getParameters()[0].getType().toString(false));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/801
    public void testMethodOverloadsArgumentMatching4a() {
        String contents = "class Foo {\n" +
            "  Foo() {\n" +
            "    bar = 1\n" +
            "  }\n" +
            "  void setBar(int value) {\n" +
            "  }\n" +
            "  void setBar(Date date) {\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("bar = ");
        MethodNode m = assertDeclaration(contents, offset, offset + 3, "Foo", "setBar", DeclarationKind.METHOD);
        assertEquals("Expected 'setBar(int)' but was 'setBar(Date)'", "int", m.getParameters()[0].getType().toString(false));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/801
    public void testMethodOverloadsArgumentMatching4b() {
        String contents = "class Foo {\n" +
            "  Foo() {\n" +
            "    bar = new Date()\n" +
            "  }\n" +
            "  void setBar(int value) {\n" +
            "  }\n" +
            "  void setBar(Date date) {\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("bar = ");
        MethodNode m = assertDeclaration(contents, offset, offset + 3, "Foo", "setBar", DeclarationKind.METHOD);
        assertEquals("Expected 'setBar(Date)' but was 'setBar(int)'", "java.util.Date", m.getParameters()[0].getType().toString(false));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/801
    public void testMethodOverloadsArgumentMatching5() {
        String contents = "class Foo {\n" +
            "  Foo() {\n" +
            "    this.bar = 1\n" +
            "  }\n" +
            "  void setBar(Date date) {\n" +
            "  }\n" +
            "  void setBar(int value) {\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("bar = ");
        MethodNode m = assertDeclaration(contents, offset, offset + 3, "Foo", "setBar", DeclarationKind.METHOD);
        assertEquals("Expected 'setBar(int)' but was 'setBar(Date)'", "int", m.getParameters()[0].getType().toString(false));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/801
    public void testMethodOverloadsArgumentMatching5a() {
        String contents = "class Foo {\n" +
            "  Foo() {\n" +
            "    this.bar = 1\n" +
            "  }\n" +
            "  void setBar(int value) {\n" +
            "  }\n" +
            "  void setBar(Date date) {\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("bar = ");
        MethodNode m = assertDeclaration(contents, offset, offset + 3, "Foo", "setBar", DeclarationKind.METHOD);
        assertEquals("Expected 'setBar(int)' but was 'setBar(Date)'", "int", m.getParameters()[0].getType().toString(false));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/801
    public void testMethodOverloadsArgumentMatching5b() {
        String contents = "class Foo {\n" +
            "  Foo() {\n" +
            "    this.bar = new Date()\n" +
            "  }\n" +
            "  void setBar(int value) {\n" +
            "  }\n" +
            "  void setBar(Date date) {\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("bar = ");
        MethodNode m = assertDeclaration(contents, offset, offset + 3, "Foo", "setBar", DeclarationKind.METHOD);
        assertEquals("Expected 'setBar(Date)' but was 'setBar(int)'", "java.util.Date", m.getParameters()[0].getType().toString(false));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/801
    public void testMethodOverloadsArgumentMatching6() {
        String contents = "class Foo {\n" +
            "  void setBar(Date date) {\n" +
            "  }\n" +
            "  void setBar(int value) {\n" +
            "  }\n" +
            "  void meth() {\n" +
            "    this.with {\n" +
            "      bar = 1\n" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("bar = ");
        MethodNode m = assertDeclaration(contents, offset, offset + 3, "Foo", "setBar", DeclarationKind.METHOD);
        assertEquals("Expected 'setBar(int)' but was 'setBar(Date)'", "int", m.getParameters()[0].getType().toString(false));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/801
    public void testMethodOverloadsArgumentMatching6a() {
        String contents = "class Foo {\n" +
            "  void setBar(int value) {\n" +
            "  }\n" +
            "  void setBar(Date date) {\n" +
            "  }\n" +
            "  void meth() {\n" +
            "    this.with {\n" +
            "      bar = 1\n" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("bar = ");
        MethodNode m = assertDeclaration(contents, offset, offset + 3, "Foo", "setBar", DeclarationKind.METHOD);
        assertEquals("Expected 'setBar(int)' but was 'setBar(Date)'", "int", m.getParameters()[0].getType().toString(false));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/801
    public void testMethodOverloadsArgumentMatching6b() {
        String contents = "class Foo {\n" +
            "  void setBar(int value) {\n" +
            "  }\n" +
            "  void setBar(Date date) {\n" +
            "  }\n" +
            "  void meth() {\n" +
            "    this.with {\n" +
            "      bar = new Date()\n" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("bar = ");
        MethodNode m = assertDeclaration(contents, offset, offset + 3, "Foo", "setBar", DeclarationKind.METHOD);
        assertEquals("Expected 'setBar(Date)' but was 'setBar(int)'", "java.util.Date", m.getParameters()[0].getType().toString(false));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/892
    public void testMethodOverloadsArgumentMatching7() {
        createJavaUnit("Face",
            "interface Face {\n" +
            "  setValue(String key, int val);\n" +
            "  setValue(String key, long val);\n" +
            "  setValue(String key, double val);\n" +
            "  setValue(String key, boolean val);\n" +
            "}\n");
        createJavaUnit("Keys",
            "interface Keys {\n" +
            "  String ONE = \"one\";\n" +
            "  String TWO = \"two\";\n" +
            "}\n");

        String contents =
            "void meth(Face face) {\n" +
            "  face.with {\n" +
            "    setValue(Keys.ONE, false)\n" +
            "  }\n" +
            "}\n";
        int offset = contents.indexOf("setValue");
        MethodNode m = assertDeclaration(contents, offset, offset + "setValue".length(), "Face", "setValue", DeclarationKind.METHOD);
        assertEquals("Expected 'setValue(String,boolean)'", "boolean", m.getParameters()[1].getType().toString(false));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/975
    public void testMethodOverloadsArgumentMatching8() {
        String contents =
            "import java.util.Map.Entry\n" +
            "class C {\n" +
            "  void meth() {\n" +
            "    Number n = 0\n" +
            "  }\n" +
            "  void meth(Entry entry) {\n" +
            "    Number n = 1\n" +
            "  }\n" +
            "}\n";

        assertType(contents, "n", "java.lang.Number");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/975
    public void testMethodOverloadsArgumentMatching8a() {
        String contents =
            "import java.util.Map.Entry\n" +
            "class C {\n" +
            "  void meth() {\n" +
            "    Number n = 0\n" +
            "  }\n" +
            "  void meth(Set<Entry> entries) {\n" +
            "    Number n = 1\n" +
            "  }\n" +
            "}\n";

        assertType(contents, "n", "java.lang.Number");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/975
    public void testMethodOverloadsArgumentMatching9() {
        String contents =
            "class Outer {\n" +
            "  class Inner {\n" +
            "    Inner() {\n" +
            "      Number n = 0\n" +
            "    }\n" +
            "    Inner(Inner that) {\n" +
            "      Number n = 1\n" +
            "    }\n" +
            "  }\n" +
            "}\n";

        assertType(contents, "n", "java.lang.Number");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1024
    public void testMethodOverloadsArgumentMatching10() {
        String contents =
            "byte meth(String s) {\n" +
            "}\n" +
            "char meth(Map args) {\n" +
            "}\n" +
            "meth(name:null)\n";

        assertType(contents, "meth", "java.lang.Character");
    }
}
