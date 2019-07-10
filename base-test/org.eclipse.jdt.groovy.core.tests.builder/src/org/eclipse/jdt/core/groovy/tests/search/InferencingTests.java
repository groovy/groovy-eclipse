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
package org.eclipse.jdt.core.groovy.tests.search;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assume.assumeFalse;

import java.util.Comparator;
import java.util.List;

import org.codehaus.groovy.ast.MethodNode;
import org.junit.Assert;
import org.junit.Test;

public final class InferencingTests extends InferencingTestSuite {

    private void assertDeclType(String source, String target, String type) {
        final int offset = source.lastIndexOf(target);
        assertDeclaringType(source, offset, offset + target.length(), type);
    }

    private void assertExprType(String source, String target, String type) {
        final int offset = source.lastIndexOf(target);
        assertType(source, offset, offset + target.length(), type);
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
        assertExprType("(x <=> y).intValue()", "intValue", "java.lang.Integer");
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
        assertExprType("def x = '10'", "'10'", "java.lang.String");
    }

    @Test
    public void testLocalVar1() {
        String contents = "int x; x";
        assertExprType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testLocalVar2() {
        String contents = "def x; x()";
        assertExprType(contents, "x", "java.lang.Object");
    }

    @Test
    public void testLocalVar3() {
        String contents = "int x; foo(x)";
        assertExprType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testLocalVar4() {
        String contents = "int x; this.x";
        int offset = contents.lastIndexOf("x");
        assertUnknownConfidence(contents, offset, offset + 1, null, false);
    }

    @Test
    public void testLocalVar5() {
        String contents = "int x; def y = { x }";
        assertExprType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testLocalVar6() {
        String contents = "def x; def y = { this.x }";
        int offset = contents.lastIndexOf("x");
        assertUnknownConfidence(contents, offset, offset + 1, null, false);
    }

    @Test
    public void testLocalVar7() {
        String contents = "def x; def y = { this.x() }";
        int offset = contents.lastIndexOf("x");
        assertUnknownConfidence(contents, offset, offset + 1, null, false);
    }

    @Test
    public void testLocalVar8() {
        String contents = "def x; def y = { owner.x }\n";
        int offset = contents.lastIndexOf("x");
        assertUnknownConfidence(contents, offset, offset + 1, null, false);
    }

    @Test
    public void testLocalVar9() {
        String contents = "def x; def y = { owner.x() }\n";
        int offset = contents.lastIndexOf("x");
        assertUnknownConfidence(contents, offset, offset + 1, null, false);
    }

    @Test
    public void testLocalVar10() {
        String contents = "def x; def y = { delegate.x }\n";
        int offset = contents.lastIndexOf("x");
        assertUnknownConfidence(contents, offset, offset + 1, null, false);
    }

    @Test
    public void testLocalVar11() {
        String contents = "def x; def y = { delegate.x() }\n";
        int offset = contents.lastIndexOf("x");
        assertUnknownConfidence(contents, offset, offset + 1, null, false);
    }

    @Test
    public void testLocalVar12() {
        String contents = "def x; def y = { thisObject.x }\n";
        int offset = contents.lastIndexOf("x");
        assertUnknownConfidence(contents, offset, offset + 1, null, false);
    }

    @Test
    public void testLocalVar13() {
        String contents = "def x; def y = { thisObject.x() }\n";
        int offset = contents.lastIndexOf("x");
        assertUnknownConfidence(contents, offset, offset + 1, null, false);
    }

    @Test
    public void testLocalVar14() {
        String contents = "def x = predicate() ? 'literal' : something.toString(); x";
        assertExprType(contents, "x", "java.lang.String");
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
        assertExprType(contents, "x", "java.lang.String");
    }

    @Test
    public void testLocalVar27() {
        String contents = "String x = 7\n" +
            "x";
        assertExprType(contents, "x", "java.lang.String");
    }

    @Test
    public void testLocalVar28() {
        String contents = "String x\n" +
            "x = 7\n" + // GroovyCastException at runtime
            "x";
        assertExprType(contents, "x", "java.lang.String");
    }

    @Test
    public void testLocalMethod1() {
        String contents =
            "int x() {}\n" +
            "def y = {\n" +
            "  x()\n" +
            "}\n";
        int offset = contents.lastIndexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Integer");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/802
    public void testLocalMethod2() {
        String contents =
            "int x() {}\n" +
            "def y = {\n" +
            "  x\n" +
            "}\n";
        int offset = contents.lastIndexOf("x");
        assertUnknownConfidence(contents, offset, offset + 1, null, false);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/802
    public void testLocalMethod3() {
        String contents =
            "int x() {}\n" +
            "def y = {\n" +
            "  def z = x\n" +
            "}\n";
        int offset = contents.lastIndexOf("x");
        assertUnknownConfidence(contents, offset, offset + 1, null, false);
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
        int offset = contents.lastIndexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Integer");
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
        int offset = contents.lastIndexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Integer");
    }

    @Test
    public void testMatcher1() {
        String contents = "def x = \"\" =~ /pattern/\nx";
        assertExprType(contents, "x", "java.util.regex.Matcher");
    }

    @Test
    public void testMatcher2() {
        String contents = "(\"\" =~ /pattern/).hasGroup()";
        assertExprType(contents, "hasGroup", "java.lang.Boolean");
    }

    @Test
    public void testPattern1() {
        String contents = "def x = ~/pattern/\nx";
        assertExprType(contents, "x", "java.util.regex.Pattern");
    }

    @Test
    public void testPattern2() {
        String contents = "def x = \"\" ==~ /pattern/\nx";
        assertExprType(contents, "x", "java.lang.Boolean");
    }

    @Test
    public void testSpread1() {
        String contents = "def z = [1,2]*.value";
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testSpread2() {
        String contents = "[1,2,3]*.intValue()";
        assertExprType(contents, "intValue", "java.lang.Integer");
    }

    @Test
    public void testSpread3() {
        String contents = "[1,2,3]*.intValue()[0].value";
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testSpread4() {
        String contents = "[x:1,y:2,z:3]*.getKey()";
        assertExprType(contents, "getKey", "java.lang.String");
    }

    @Test
    public void testSpread5() {
        String contents = "[x:1,y:2,z:3]*.getValue()";
        assertExprType(contents, "getValue", "java.lang.Integer");
    }

    @Test
    public void testSpread6() {
        String contents = "[x:1,y:2,z:3]*.key";
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testSpread7() {
        String contents = "[x:1,y:2,z:3]*.value";
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testSpread8() {
        String contents = "[x:1,y:2,z:3]*.key[0].toLowerCase()";
        assertExprType(contents, "toLowerCase", "java.lang.String");
    }

    @Test
    public void testSpread9() {
        String contents = "[x:1,y:2,z:3]*.value[0].intValue()";
        assertExprType(contents, "intValue", "java.lang.Integer");
    }

    @Test
    public void testSpread10() {
        String contents = "[1,2,3]*.value[0].value";
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testSpread11() {
        String contents = "Set<String> strings = ['1','2','3'] as Set\n" +
            "strings*.bytes\n";
        assertExprType(contents, "bytes", "byte[]");
    }

    @Test
    public void testSpread12() {
        String contents = "Set<String> strings = ['1','2','3'] as Set\n" +
            "strings*.length()\n";
        assertExprType(contents, "length", "java.lang.Integer");
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
        assertExprType(contents, "beans", "java.util.Set<java.beans.BeanInfo>");
        assertExprType(contents, "additionalBeanInfo", "java.beans.BeanInfo[]");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/763
    public void testSpread14() {
        String contents = "def strings = [[['1','2','3']]]\n" +
            "def result = strings*.length()\n";
        assertExprType(contents, "result", "java.util.List<java.util.List<E extends java.lang.Object>>");
    }

    @Test // CommandRegistry.iterator() lacks generics
    public void testSpread15() {
        String contents =
            "import org.codehaus.groovy.tools.shell.CommandRegistry\n" +
            "def registry = new CommandRegistry()\n" +
            "def result = registry*.with {it}\n";
        assertExprType(contents, "result", "java.util.List<java.lang.Object>");
    }

    @Test
    public void testSpread16() {
        String contents =
            "import java.util.regex.Matcher\n" +
            "Matcher matcher = ('abc' =~ /./)\n" +
            "def result = matcher*.with {it}\n";
        assertExprType(contents, "result", "java.util.List<java.lang.Object>");
    }

    @Test
    public void testSpread17() {
        String contents =
            "Reader reader = null\n" +
            "def result = reader*.with {it}\n";
        assertExprType(contents, "result", "java.util.List<java.lang.String>");
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
        assertExprType(contents, "booleanValue", "java.lang.Boolean");
    }

    @Test
    public void testBoolean3() {
        String contents = "(x <= y).booleanValue()";
        assertExprType(contents, "booleanValue", "java.lang.Boolean");
    }

    @Test
    public void testBoolean4() {
        String contents = "(x >= y).booleanValue()";
        assertExprType(contents, "booleanValue", "java.lang.Boolean");
    }

    @Test
    public void testBoolean5() {
        String contents = "(x != y).booleanValue()";
        assertExprType(contents, "booleanValue", "java.lang.Boolean");
    }

    @Test
    public void testBoolean6() {
        String contents = "(x == y).booleanValue()";
        assertExprType(contents, "booleanValue", "java.lang.Boolean");
    }

    @Test
    public void testBoolean7() {
        String contents = "(x in y).booleanValue()";
        assertExprType(contents, "booleanValue", "java.lang.Boolean");
    }

    @Test
    public void testClassLiteral1() {
        String contents = "def foo = Number.class";
        assertExprType(contents, "foo", "java.lang.Class<java.lang.Number>");
    }

    @Test
    public void testClassLiteral2() {
        String contents = "def foo = java.lang.Number.class";
        assertExprType(contents, "foo", "java.lang.Class<java.lang.Number>");
    }

    @Test
    public void testClassLiteral3() {
        String contents = "def foo = Number";
        assertExprType(contents, "foo", "java.lang.Class<java.lang.Number>");
    }

    @Test
    public void testClassLiteral4() {
        String contents = "def foo = java.lang.Number";
        assertExprType(contents, "foo", "java.lang.Class<java.lang.Number>");
    }

    @Test
    public void testClassLiteral5() {
        String contents = "def foo = Map.Entry.class";
        assertExprType(contents, "foo", "java.lang.Class<java.util.Map$Entry>");
    }

    @Test
    public void testClassReference1() {
        String contents = "String.substring";
        int start = contents.indexOf("substring"), until = start + 9;
        assertDeclaringType(contents, start, until, "java.lang.String", false, true);
    }

    @Test
    public void testClassReference2() {
        String contents = "String.getPackage()";
        assertExprType(contents, "getPackage", "java.lang.Package");
        assertDeclType(contents, "getPackage", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference2a() {
        String contents = "String.package";
        assertExprType(contents, "package", "java.lang.Package");
        assertDeclType(contents, "package", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference3() {
        String contents = "String.class.getPackage()";
        assertExprType(contents, "getPackage", "java.lang.Package");
        assertDeclType(contents, "getPackage", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference3a() {
        String contents = "String.class.package";
        assertExprType(contents, "package", "java.lang.Package");
        assertDeclType(contents, "package", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference4() {
        String contents = "def clazz = String; clazz.getPackage()";
        assertExprType(contents, "getPackage", "java.lang.Package");
        assertDeclType(contents, "getPackage", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference4a() {
        String contents = "def clazz = String; clazz.package";
        assertExprType(contents, "package", "java.lang.Package");
        assertDeclType(contents, "package", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference4b() {
        String contents = "Class clazz = String; clazz.package";
        assertExprType(contents, "package", "java.lang.Package");
        assertDeclType(contents, "package", "java.lang.Class");
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

        int start = contents.indexOf("this()");
        int end = start + "this()".length();
        assertType(contents, start, end, "java.lang.Void");
        assertDeclaringType(contents, start, end, "C");
    }

    @Test
    public void testSpecialConstructor2() {
        String contents =
            "class C extends HashMap {\n" +
            "  C() {\n" +
            "    super()\n" +
            "  }\n" +
            "}";

        int start = contents.indexOf("super()");
        int end = start + "super()".length();
        assertType(contents, start, end, "java.lang.Void");
        assertDeclaringType(contents, start, end, "java.util.HashMap");
    }

    @Test
    public void testStaticMethod1() {
        String contents = "Two.x()\n class Two {\n static String x() {\n \"\" } } ";
        String expr = "x";
        assertType(contents, contents.indexOf(expr), contents.indexOf(expr)+expr.length(), "java.lang.String");
    }

    @Test
    public void testStaticMethod2() {
        String contents = "Two.x\n class Two {\n static String x() {\n \"\" } } ";
        String expr = "x";
        assertType(contents, contents.indexOf(expr), contents.indexOf(expr)+expr.length(), "java.lang.String");
    }

    @Test
    public void testStaticMethod3() {
        String contents =
            "class Two {\n" +
            "  def other() {\n" +
            "    x()\n" + // this
            "  }\n" +
            "  static String x() {\n" +
            "    \"\"\n" +
            "  }\n" +
            "}\n";
        int offset = contents.indexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.String");
    }

    @Test
    public void testStaticMethod4() {
        String contents =
            "class Two {\n" +
            "  def other() {\n" +
            "    x\n" + // this
            "  }\n" +
            "  static String x() {\n" +
            "    \"\"\n" +
            "  }\n" +
            "}\n";
        int offset = contents.indexOf("x");
        assertUnknownConfidence(contents, offset, offset + 1, null, false);
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
        String expr = "p()";
        assertDeclaringType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Parent");
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
        String expr = "p()";
        assertDeclaringType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Parent");
    }

    @Test
    public void testStaticMethod7() throws Exception {
        createUnit("foo", "Bar", "package foo\n" +
            "import java.util.regex.*\n" +
            "class Bar {\n" +
            "  static Object meth(Object o) { return o }\n" +
            "  static Pattern meth(Pattern p) { return p }\n" +
            "  static Collection meth(Collection c) { return c }\n" +
            "}");

        String staticCall = "meth([])";
        String contents = "import static foo.Bar.*; " + staticCall;
        assertType(contents, contents.indexOf(staticCall), contents.indexOf(staticCall) + staticCall.length(), "java.util.Collection");
    }

    @Test
    public void testStaticMethod8() throws Exception {
        createUnit("foo", "Bar", "package foo\n" +
            "import java.util.regex.*\n" +
            "class Bar {\n" +
            "  static Object meth(Object o) { return o }\n" +
            "  static Pattern meth(Pattern p) { return p }\n" +
            "  static Collection meth(Collection c) { return c }\n" +
            "}");

        String staticCall = "meth(~/abc/)";
        String contents = "import static foo.Bar.*; " + staticCall;
        assertType(contents, contents.indexOf(staticCall), contents.indexOf(staticCall) + staticCall.length(), "java.util.regex.Pattern");
    }

    @Test
    public void testStaticMethod9() throws Exception {
        createUnit("foo", "Bar", "package foo\n" +
            "import java.util.regex.*\n" +
            "class Bar {\n" +
            "  static Object meth(Object o) { return o }\n" +
            "  static Pattern meth(Pattern p) { return p }\n" +
            "  static Collection meth(Collection c) { return c }\n" +
            "}");

        String staticCall = "meth(compile('abc'))";
        String contents = "import static foo.Bar.*; import static java.util.regex.Pattern.*; " + staticCall;
        assertType(contents, contents.indexOf(staticCall), contents.indexOf(staticCall) + staticCall.length(), "java.util.regex.Pattern");
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
        assertExprType(contents, "this", "java.lang.Class<A>");
        assertExprType(contents, "super", "java.lang.Object");
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
        assertExprType(contents, "this", "java.lang.Class<B>");
        assertExprType(contents, "super", "java.lang.Class<A>");
    }

    @Test
    public void testSuperFieldReference1() {
        String contents =
            "class A {\n" +
            "  Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    field\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "field", "java.lang.Number");
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
        assertExprType(contents, "field", "java.lang.Number");
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
        int offset = contents.lastIndexOf("field");
        assertUnknownConfidence(contents, offset, offset + "field".length(), "A", false);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperFieldReference3a() {
        String contents =
            "class A {\n" +
            "  private String field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    this.field\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf("field");
        assertUnknownConfidence(contents, offset, offset + "field".length(), "A", false);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperFieldReference3b() {
        String contents =
            "class A {\n" +
            "  private String field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.field\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf("field");
        assertUnknownConfidence(contents, offset, offset + "field".length(), "A", false);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperFieldReference3c() {
        String contents =
            "class A {\n" +
            "  private String field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.@field\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf("field");
        assertUnknownConfidence(contents, offset, offset + "field".length(), "A", false);
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
        assertExprType(contents, "FIRST", "java.lang.Integer");
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
        assertExprType(contents, "CONST", "java.lang.Integer");
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
        assertExprType(contents, "CONST", "java.lang.Integer");
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
        assertExprType(contents, "CONST", "java.lang.Integer");
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
        assertDeclType(contents, "method", "A");
    }

    @Test
    public void testSuperClassMethod2() {
        String contents =
            "class A {\n" +
            "  void method(Runnable r) {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "}\n" +
            "new B().method() { -> }\n";
        assertDeclType(contents, "method", "A");
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
        assertDeclType(contents, "method", "A");
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
        int offset = contents.lastIndexOf("method");
        assertUnknownConfidence(contents, offset, offset + "method".length(), "A", false);
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
        int offset = contents.lastIndexOf("method");
        assertUnknownConfidence(contents, offset, offset + "method".length(), "A", false);
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
        assertDeclType(contents, "method", "A");
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
        assertDeclType(contents, "method", "A");
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
        assertExprType(contents, "super", "java.lang.Number");
        assertDeclType(contents, "intValue", "java.lang.Number");
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
        assertDeclType(contents, "run", "java.lang.Runnable");
        assertExprType(contents, "super", "java.lang.Runnable");
        assertDeclType(contents, "hashCode", "java.lang.Object");
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
        assertDeclType(contents, "hashCode", "java.lang.Object");
        assertExprType(contents, "super", "groovy.lang.GroovyObject");
    }

    @Test
    public void testFieldWithInitializer1() {
        String contents =
            "class A {\n" +
            "  def x = 9\n" +
            "}\n" +
            "new A().x";
        int offset = contents.lastIndexOf('x');
        assertType(contents, offset, offset + 1, "java.lang.Integer");
    }

    @Test
    public void testFieldWithInitializer2() {
        createUnit("A", "class A {\ndef x = 9\n}");
        String contents = "new A().x";
        int offset = contents.lastIndexOf('x');
        assertType(contents, offset, offset + 1, "java.lang.Integer");
    }

    @Test // GRECLIPSE-731
    public void testLocalWithInitializer1() {
        String contents = "def foo() {}\nString xxx = foo()\nxxx";
        int offset = contents.lastIndexOf("xxx");
        assertType(contents, offset, offset + "xxx".length(), "java.lang.String");
    }

    @Test // GRECLIPSE-731
    public void testLocalWithInitializer2() {
        String contents = "def foo() {}\ndef xxx = foo()\nxxx";
        int offset = contents.lastIndexOf("xxx");
        assertType(contents, offset, offset + "xxx".length(), "java.lang.Object");
    }

    @Test // GRECLIPSE-731
    public void testLocalWithInitializer3() {
        String contents = "String foo() {}\ndef xxx = foo()\nxxx";
        int offset = contents.lastIndexOf("xxx");
        assertType(contents, offset, offset + "xxx".length(), "java.lang.String");
    }

    @Test // GRECLIPSE-731
    public void testLocalWithInitializer4() {
        String contents = "int foo() {}\ndef xxx = foo()\nxxx";
        int offset = contents.lastIndexOf("xxx");
        assertType(contents, offset, offset + "xxx".length(), "java.lang.Integer");
    }

    @Test // GRECLIPSE-731
    public void testLocalWithInitializer5() {
        String contents = "def foo() {}\nString xxx\nxxx = foo()\nxxx";
        int offset = contents.lastIndexOf("xxx");
        assertType(contents, offset, offset + "xxx".length(), "java.lang.String");
    }

    @Test
    public void testElvisInitializer() {
        String contents = "def x = 2 ?: 1\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testTernaryInitializer() {
        String contents = "def x = true ? 2 : 1\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testRangeExpression1() {
        String contents = "def x = 0 .. 5\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "groovy.lang.Range<java.lang.Integer>");
    }

    @Test
    public void testRangeExpression2() {
        String contents = "def x = 0 ..< 5\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "groovy.lang.Range<java.lang.Integer>");
    }

    @Test
    public void testRangeExpression3() {
        String contents = "(1..10).getFrom()";
        int start = contents.lastIndexOf("getFrom");
        int end = start + "getFrom".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testRangeExpression4() {
        String contents = "(1..10).getTo()";
        int start = contents.lastIndexOf("getTo");
        int end = start + "getTo".length();
        assertType(contents, start, end, "java.lang.Integer");
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
        String contents = "(1..10).step(0, { })";
        int start = contents.lastIndexOf("step");
        int end = start + "step".length();
        assertType(contents, start, end, "java.lang.Void");
    }

    @Test
    public void testInnerClass1() {
        String contents = "class Outer { class Inner { } \nInner x }\nnew Outer().x ";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "Outer$Inner");
    }

    @Test
    public void testInnerClass2() {
        String contents = "class Outer { class Inner { class InnerInner{ } }\n Outer.Inner.InnerInner x }\nnew Outer().x ";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "Outer$Inner$InnerInner");
    }

    @Test
    public void testInnerClass3() {
        String contents = "class Outer { class Inner { def z() { \nnew Outer().x \n } } \nInner x }";
        int start = contents.indexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "Outer$Inner");
    }

    @Test
    public void testInnerClass4() {
        String contents = "class Outer { class Inner { class InnerInner { def z() { \nnew Outer().x \n } } } \nInner x }";
        int start = contents.indexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "Outer$Inner");
    }

    @Test
    public void testInnerClass5() {
        String contents = "class Outer { class Inner extends Outer { } }";
        int start = contents.lastIndexOf("Outer");
        int end = start + "Outer".length();
        assertType(contents, start, end, "Outer");
    }

    @Test
    public void testInnerClass6() {
        String contents = "class Outer extends RuntimeException { class Inner { def foo() throws Outer { } } }";
        int start = contents.lastIndexOf("Outer");
        int end = start + "Outer".length();
        assertType(contents, start, end, "Outer");
    }

    @Test
    public void testInnerClass7() {
        String contents = "class A {\n" +
            "  protected Number f\n" +
            "  protected Number m() { }\n" +
            "}\n" +
            "class AA extends A {\n" +
            "  class AAA {\n" +
            "    def x() {\n" +
            "      f  \n" +
            "      m()\n" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf('f');
        assertDeclaringType(contents, offset, offset + 1, "A");
        assertType(contents, offset, offset + 1, "java.lang.Number");
            offset = contents.lastIndexOf('m');
        assertDeclaringType(contents, offset, offset + 1, "A");
        assertType(contents, offset, offset + 1, "java.lang.Number");
    }

    @Test
    public void testInnerClass8() {
        String contents = "class A {\n" +
            "  protected Number f\n" +
            "  protected Number m() { }\n" +
            "}\n" +
            "class AA extends A {\n" +
            "  static class AAA {\n" +
            "    def x() {\n" +
            "      f  \n" +
            "      m()\n" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf('f');
        assertType(contents, offset, offset + 1, "java.lang.Object");
        assertUnknownConfidence(contents, offset, offset + 1, "A", false);
            offset = contents.lastIndexOf('m');
        assertType(contents, offset, offset + 1, "java.lang.Object");
        assertUnknownConfidence(contents, offset, offset + 1, "A", false);
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
            "}";
        int offset = contents.lastIndexOf('N');
        assertDeclaringType(contents, offset, offset + 1, "A");
        assertType(contents, offset, offset + 1, "java.lang.Number");
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
            "}";
        int offset = contents.lastIndexOf('N');
        assertDeclaringType(contents, offset, offset + 1, "A");
        assertType(contents, offset, offset + 1, "java.lang.Number");
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
            "}";
        int offset = contents.lastIndexOf('N');
        assertDeclaringType(contents, offset, offset + 1, "A");
        assertType(contents, offset, offset + 1, "java.lang.Number");
    }

    @Test
    public void testAnonInner1() {
        String contents = "def foo = new Runnable() { void run() {} }";
        int start = contents.lastIndexOf("Runnable");
        int end = start + "Runnable".length();
        assertType(contents, start, end, "java.lang.Runnable");
    }

    @Test
    public void testAnonInner2() {
        String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) {} }";
        int start = contents.lastIndexOf("Comparable");
        int end = start + "Comparable".length();
        assertType(contents, start, end, "java.lang.Comparable<java.lang.String>");
    }

    @Test
    public void testAnonInner3() {
        String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) { compareTo()} }";
        int start = contents.lastIndexOf("compareTo");
        int end = start + "compareTo".length();
        assertDeclaringType(contents, start, end, "Search$1");
    }

    @Test
    public void testAnonInner4() {
        String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) {} }\n" +
            "foo.compareTo('one', 'two')";
        int start = contents.lastIndexOf("compareTo");
        int end = start + "compareTo".length();
        assertDeclaringType(contents, start, end, "Search$1");
    }

    @Test
    public void testAnonInner5() {
        String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) {} }\n" +
            "foo = new Comparable<String>() { int compareTo(String a, String b) {} }\n" +
            "foo.compareTo('one', 'two')";
        int start = contents.lastIndexOf("compareTo");
        int end = start + "compareTo".length();
        assertDeclaringType(contents, start, end, "Search$2");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/378
    public void testAnonInner6() {
        String contents =
            "class A {\n" +
            "  protected def f\n" +
            "  protected def m() { }\n" +
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
        int offset = contents.lastIndexOf('f');
        assertDeclaringType(contents, offset, offset + 1, "A");
            offset = contents.lastIndexOf('m');
        assertDeclaringType(contents, offset, offset + 1, "A");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/383
    public void testAnonInner7() {
        String contents =
            "class A {\n" +
            "  protected def m() { }\n" +
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
        String catchString = "try {     } catch (NullPointerException e) { e }";
        int start = catchString.lastIndexOf("NullPointerException");
        int end = start + "NullPointerException".length();
        assertType(catchString, start, end, "java.lang.NullPointerException");
    }

    @Test
    public void testCatchBlock2() {
        String catchString = "try {     } catch (NullPointerException e) { e }";
        int start = catchString.lastIndexOf("e");
        int end = start + 1;
        assertType(catchString, start, end, "java.lang.NullPointerException");
    }

    @Test
    public void testCatchBlock3() {
        String catchString = "try {     } catch (NullPointerException e) { e }";
        int start = catchString.indexOf("NullPointerException e");
        int end = start + ("NullPointerException e").length();
        assertType(catchString, start, end, "java.lang.NullPointerException");
    }

    @Test
    public void testCatchBlock4() {
        String catchString2 = "try {     } catch (e) { e }";
        int start = catchString2.indexOf("e");
        int end = start + 1;
        assertType(catchString2, start, end, "java.lang.Exception");
    }

    @Test
    public void testCatchBlock5() {
        String catchString2 = "try {     } catch (e) { e }";
        int start = catchString2.lastIndexOf("e");
        int end = start + 1;
        assertType(catchString2, start, end, "java.lang.Exception");
    }

    @Test
    public void testScriptDeclaringType() {
        String contents = "other\n";
        int start = contents.lastIndexOf("other");
        int end = start + "other".length();
        assertDeclaringType(contents, start, end, "Search", false, true);
    }

    @Test
    public void testStaticImports1() {
        String contents = "import static javax.swing.text.html.HTML.NULL_ATTRIBUTE_VALUE\n" +
                          "NULL_ATTRIBUTE_VALUE";
        int start = contents.lastIndexOf("NULL_ATTRIBUTE_VALUE");
        int end = start + "NULL_ATTRIBUTE_VALUE".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testStaticImports2() {
        String contents = "import static javax.swing.text.html.HTML.getAttributeKey\n" +
                          "getAttributeKey('')";
        int start = contents.lastIndexOf("getAttributeKey");
        int end = start + "getAttributeKey('')".length();
        assertType(contents, start, end, "javax.swing.text.html.HTML$Attribute");
    }

    @Test
    public void testStaticImports3() {
        String contents = "import static javax.swing.text.html.HTML.*\n" +
                          "NULL_ATTRIBUTE_VALUE";
        int start = contents.lastIndexOf("NULL_ATTRIBUTE_VALUE");
        int end = start + "NULL_ATTRIBUTE_VALUE".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testStaticImports4() {
        String contents = "import static javax.swing.text.html.HTML.*\n" +
                          "getAttributeKey('')";
        int start = contents.lastIndexOf("getAttributeKey");
        int end = start + "getAttributeKey('')".length();
        assertType(contents, start, end, "javax.swing.text.html.HTML$Attribute");
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
        assertDeclaringType(CONTENTS_GETAT2, start, end, "GetAt", false, true);
    }

    @Test
    public void testGetAt4() {
        int start = CONTENTS_GETAT2.lastIndexOf("startsWith");
        int end = start + "startsWith".length();
        // expecting unknown confidence because getAt not explicitly defined
        assertDeclaringType(CONTENTS_GETAT2, start, end, "GetAt", false, true);
    }

    @Test // GRECLIPSE-743
    public void testGetAt5() {
        String contents = "class A { }\n new A().getAt() ";
        int start = contents.lastIndexOf("getAt");
        int end = start + "getAt".length();
        assertType(contents, start, end, "java.lang.Object");
        assertDeclaringType(contents, start, end, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test
    public void testGetAt6() {
        String contents = "class A {\n A getAt(prop) { \n new A() \n } }\n new A().getAt('x')";
        int start = contents.lastIndexOf("getAt");
        int end = start + "getAt".length();
        assertType(contents, start, end, "A");
        assertDeclaringType(contents, start, end, "A");
    }

    @Test
    public void testGetAt7() {
        String contents = "class A {\n A getAt(prop) { \n new A() \n } }\n class B extends A { }\n new B().getAt('x')";
        int start = contents.lastIndexOf("getAt");
        int end = start + "getAt".length();
        assertType(contents, start, end, "A");
        assertDeclaringType(contents, start, end, "A");
    }

    @Test // CommandRegistry.commands() returns List<Command>
    public void testGetAt8() {
        assumeFalse(isAtLeastGroovy(25));
        String contents =
            "import org.codehaus.groovy.tools.shell.CommandRegistry\n" +
            "def registry = new CommandRegistry()\n" +
            "def result = registry.commands()[0]\n";
        assertExprType(contents, "result", "org.codehaus.groovy.tools.shell.Command");
    }

    @Test // CommandRegistry.iterator() returns Iterator
    public void testGetAt9() {
        assumeFalse(isAtLeastGroovy(25));
        String contents =
            "import org.codehaus.groovy.tools.shell.CommandRegistry\n" +
            "def registry = new CommandRegistry()\n" +
            "def result = registry.iterator()[0]\n";
        assertExprType(contents, "result", "java.lang.Object");
    }

    @Test
    public void testListSort1() {
        String contents = "def list = []; list.sort()";
        int offset = contents.lastIndexOf("sort");
        assertType(contents, offset, offset + 4, "java.util.List<java.lang.Object>");
        assertDeclaringType(contents, offset, offset + 4, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/387
    public void testListSort2() {
        String contents = "def list = []; list.sort { a, b -> a <=> b }";
        int offset = contents.lastIndexOf("sort");
        assertType(contents, offset, offset + 4, "java.util.List<java.lang.Object>");
        MethodNode m = assertDeclaration(contents, offset, offset + 4, "org.codehaus.groovy.runtime.DefaultGroovyMethods", "sort", DeclarationKind.METHOD);
        Assert.assertEquals("Should resolve to sort(Iterable,Closure) since Collection version is deprecated", "java.lang.Iterable<java.lang.Object>", printTypeName(m.getParameters()[0].getType()));
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

        String contents = "def list = []; list.sort({ a, b -> a <=> b } as Comparator)";
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
        Assert.assertEquals("Should resolve to remove(int) due to return type of inner call", "int", printTypeName(m.getParameters()[0].getType()));
    }

    @Test // GRECLIPSE-1013
    public void testCategoryMethodAsProperty() {
        String contents = "''.toURL().text";
        int start = contents.indexOf("text");
        assertDeclaringType(contents, start, start + 4, "org.codehaus.groovy.runtime.ResourceGroovyMethods");
    }

    @Test
    public void testInterfaceMethodsAsProperty() throws Exception {
        createUnit("foo", "Bar", "package foo; interface Bar { def getOne() }");
        createUnit("foo", "Baz", "package foo; interface Baz extends Bar { def getTwo() }");

        String contents = "def meth(foo.Baz b) { b.one + b.two }";

        int start = contents.indexOf("one");
        assertDeclaringType(contents, start, start + 3, "foo.Bar");
        start = contents.indexOf("two");
        assertDeclaringType(contents, start, start + 3, "foo.Baz");
    }

    @Test
    public void testInterfaceMethodAsProperty2() throws Exception {
        createUnit("foo", "Bar", "package foo; interface Bar { def getOne() }");
        createUnit("foo", "Baz", "package foo; abstract class Baz implements Bar { abstract def getTwo() }");

        String contents = "def meth(foo.Baz b) { b.one + b.two }";

        int start = contents.indexOf("one");
        assertDeclaringType(contents, start, start + 3, "foo.Bar");
        start = contents.indexOf("two");
        assertDeclaringType(contents, start, start + 3, "foo.Baz");
    }

    @Test
    public void testInterfaceMethodAsProperty3() throws Exception {
        createUnit("foo", "Bar", "package foo; interface Bar { def getOne() }");
        createUnit("foo", "Baz", "package foo; abstract class Baz implements Bar { abstract def getTwo() }");

        String contents = "abstract class C extends foo.Baz { }\n def meth(C c) { c.one + c.two }";

        int start = contents.indexOf("one");
        assertDeclaringType(contents, start, start + 3, "foo.Bar");
        start = contents.indexOf("two");
        assertDeclaringType(contents, start, start + 3, "foo.Baz");
    }

    @Test
    public void testIndirectInterfaceMethod() throws Exception {
        createUnit("foo", "Bar", "package foo; interface Bar { def getOne() }");
        createUnit("foo", "Baz", "package foo; abstract class Baz implements Bar { abstract def getTwo() }");

        String contents = "abstract class C extends foo.Baz { }\n def meth(C c) { c.getOne() + c.getTwo() }";

        int start = contents.indexOf("getOne");
        assertDeclaringType(contents, start, start + 6, "foo.Bar");
        start = contents.indexOf("getTwo");
        assertDeclaringType(contents, start, start + 6, "foo.Baz");
    }

    @Test
    public void testIndirectInterfaceConstant() throws Exception {
        createUnit("I", "interface I { Number ONE = 1 }");
        createUnit("A", "abstract class A implements I { Number TWO = 2 }");

        String contents = "abstract class B extends A { }\n B b; b.ONE; b.TWO";

        int start = contents.indexOf("ONE");
        assertDeclaringType(contents, start, start + 3, "I");
        start = contents.indexOf("TWO");
        assertDeclaringType(contents, start, start + 3, "A");
    }

    @Test
    public void testObjectMethodOnInterface() {
        // Object is not in explicit type hierarchy of List
        String contents = "def meth(List list) { list.getClass() }";

        String target = "getClass", source = "java.lang.Object";
        assertDeclaringType(contents, contents.indexOf(target), contents.indexOf(target) + target.length(), source);
    }

    @Test
    public void testObjectMethodOnInterfaceAsProperty() {
        // Object is not in explicit type hierarchy of List
        String contents = "def meth(List list) { list.class }";

        String target = "class", source = "java.lang.Object";
        assertDeclaringType(contents, contents.indexOf(target), contents.indexOf(target) + target.length(), source);
    }

    @Test
    public void testMultiDecl1() {
        String contents = "def (x, y) = []\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart + 1, "java.lang.Object");
        assertType(contents, yStart, yStart + 1, "java.lang.Object");
    }

    @Test
    public void testMultiDecl2() {
        String contents = "def (x, y) = [1]\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Integer");
        assertType(contents, yStart, yStart+1, "java.lang.Integer");
    }

    @Test
    public void testMultiDecl3() {
        String contents = "def (x, y) = [1,1]\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Integer");
        assertType(contents, yStart, yStart+1, "java.lang.Integer");
    }

    @Test
    public void testMultiDecl4() {
        String contents = "def (x, y) = [1,'']\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Integer");
        assertType(contents, yStart, yStart+1, "java.lang.String");
    }

    @Test
    public void testMultiDecl6() {
        String contents = "def (x, y) = new ArrayList()\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Object");
        assertType(contents, yStart, yStart+1, "java.lang.Object");
    }

    @Test
    public void testMultiDecl7() {
        String contents = "def (x, y) = new ArrayList<Double>()\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }

    @Test
    public void testMultiDecl8() {
        String contents = "Double[] meth() { }\ndef (x, y) = meth()\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }

    @Test
    public void testMultiDecl9() {
        String contents = "List<Double> meth() { }\ndef (x, y) = meth()\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }

    @Test
    public void testMultiDecl10() {
        String contents = "List<Double> field\ndef (x, y) = field\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }

    @Test
    public void testMultiDecl11() {
        String contents = "List<Double> field\ndef x\ndef y\n (x, y)= field\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }

    @Test
    public void testMultiDecl12() {
        String contents = "def (x, y) = 1d\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }

    @Test
    public void testMultiDecl13() {
        String contents = "def (int x, float y) = [1,2]\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Integer");
        assertType(contents, yStart, yStart+1, "java.lang.Float");
    }

    @Test // GRECLIPSE-1174 groovy casting
    public void testAsExpression1() {
        String contents = "(1 as int).intValue()";
        int start = contents.lastIndexOf("intValue");
        assertType(contents, start, start+"intValue".length(), "java.lang.Integer");
    }

    @Test // GRECLIPSE-1174 groovy casting
    public void testAsExpression2() {
        String contents = "class Flar { int x\n }\n(null as Flar).x";
        int start = contents.lastIndexOf("x");
        assertType(contents, start, start+"x".length(), "java.lang.Integer");
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar1() {
        String contents = "class SettingUndeclaredProperty {\n" +
            "    public void mymethod() {\n" +
            "        doesNotExist = \"abc\"\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("doesNotExist");
        assertUnknownConfidence(contents, start, start+"doesNotExist".length(), "SettingUndeclaredProperty", false);
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar2() {
        String contents = "class SettingUndeclaredProperty {\n" +
            "     def r = {\n" +
            "        doesNotExist = 0\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("doesNotExist");
        assertUnknownConfidence(contents, start, start+"doesNotExist".length(), "SettingUndeclaredProperty", false);
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar3() {
        String contents =
            "doesNotExist";
        int start = contents.lastIndexOf("doesNotExist");
        assertUnknownConfidence(contents, start, start+"doesNotExist".length(), "Search", false);
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar4() {
        String contents = "doesNotExist = 9";
        int start = contents.lastIndexOf("doesNotExist");
        assertDeclaringType(contents, start, start+"doesNotExist".length(), "Search", false);
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar5() {
        String contents =
            "doesNotExist = 9\n" +
            "def x = {doesNotExist }";
        int start = contents.lastIndexOf("doesNotExist");
        assertDeclaringType(contents, start, start+"doesNotExist".length(), "Search", false);
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar6() {
        String contents =
            "def x = {\n" +
            "doesNotExist = 9\n" +
            "doesNotExist }";
        int start = contents.lastIndexOf("doesNotExist");
        assertDeclaringType(contents, start, start+"doesNotExist".length(), "Search", false);
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar7() {
        String contents =
            "def z() {\n" +
            "    doesNotExist = 9\n" +
            "}\n";
        int start = contents.lastIndexOf("doesNotExist");
        assertUnknownConfidence(contents, start, start+"doesNotExist".length(), "Search", false);
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
        createUnit("Foo", "class Foo { int prop }");
        String contents = "(new Foo().@prop) + (8 ?: 7)";
        assertType(contents, "java.lang.Integer");
    }

    @Test
    public void testPostfix() {
        String contents =
            "int i = 0\n" +
            "def list = [0]\n" +
            "list[i]++";
        int start = contents.lastIndexOf('i');
        assertType(contents, start, start +1, "java.lang.Integer");
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
            "    String action() { }\n" +
            "    def meth() {\n" +
            "        def x = action()\n" +
            "        x.substring()\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("substring");
        int end = start + "substring".length();
        assertType(contents, start, end, "java.lang.String");
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
        assertType(contents, start, end, "java.lang.Object"); //TODO: "java.lang.String"?

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
            "}}\n" +
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
            "}}\n" +
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
            "def str = !(val instanceof String) ? val.toString : val\n" +
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
        assertType(contents, start, end, "java.lang.Object"); //TODO: "java.lang.String"?

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

    @Test
    public void testThisInInnerClass() {
        String contents =
            "class A {\n" +
            "    String source = null\n" +
            "    def user = new Object() {\n" +
            "        def field = A.this.source\n" +
            "    }\n" +
            "}\n";
        int start = contents.lastIndexOf("source");
        int end = start + "source".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("this");
        end = start + "this".length();
        assertType(contents, start, end, "A");
    }

    @Test // GRECLIPSE-1798
    public void testFieldAndPropertyWithSameName() {
        createJavaUnit("Wrapper",
            "public class Wrapper<T> {\n" +
            "  private final T wrapped;\n" +
            "  public Wrapper(T wrapped) { this.wrapped = wrapped; }\n" +
            "  public T getWrapped() { return wrapped; }\n" +
            "}");
        createJavaUnit("MyBean",
            "public class MyBean {\n" +
            "  private Wrapper<String> foo = new Wrapper<>(\"foo\");\n" +
            "  public String getFoo() { return foo.getWrapped(); }\n" +
            "}");

        String contents =
            "class GroovyTest {\n" +
            "  static void main(String[] args) {\n" +
            "    def b = new MyBean()\n" +
            "    println b.foo.toUpperCase()\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "foo", "java.lang.String");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/355
    public void testLocalTypeAndDefaultImportCollision() {
        createJavaUnit("domain", "Calendar",
            "public class Calendar { public static Calendar instance() { return null; } }");

        String contents = "def cal = domain.Calendar.instance()";
        assertExprType(contents, "instance", "domain.Calendar");
        assertExprType(contents, "cal", "domain.Calendar");
    }

    @Test
    public void testMethodOverloadsArgumentMatching1() {
        createJavaUnit("MyEnum", "enum MyEnum { A, B }");

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
        Assert.assertTrue("Expected 'meth(String, Object, Object)' but was 'meth(String, MyEnum)' or 'meth(String, Date, Date)'",
            m.getParameters().length == 3 && m.getParameters()[2].getType().getNameWithoutPackage().equals("Object"));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/405
    public void testMethodOverloadsArgumentMatching2() {
        createJavaUnit("MyEnum", "enum MyEnum { A, B }");

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
        Assert.assertEquals("Expected 'meth(String, Date, Date)' but was 'meth(String, MyEnum)'", 3, m.getParameters().length);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/644
    public void testMethodOverloadsArgumentMatching3() {
        String contents = "Arrays.toString(new Object())";

        String target = "toString";
        int offset = contents.indexOf(target);
        MethodNode m = assertDeclaration(contents, offset, offset + target.length(), "java.util.Arrays", "toString", DeclarationKind.METHOD);

        String arrayType = m.getParameters()[0].getType().toString(false);
        Assert.assertEquals("Expected '" + target + "(Object[])' but was '" + target + "(" + arrayType + ")'", "java.lang.Object[]", arrayType);
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
        Assert.assertEquals("Expected 'setBar(int)' but was 'setBar(Date)'", "int", m.getParameters()[0].getType().toString(false));
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
        Assert.assertEquals("Expected 'setBar(int)' but was 'setBar(Date)'", "int", m.getParameters()[0].getType().toString(false));
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
        Assert.assertEquals("Expected 'setBar(Date)' but was 'setBar(int)'", "java.util.Date", m.getParameters()[0].getType().toString(false));
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
        Assert.assertEquals("Expected 'setBar(int)' but was 'setBar(Date)'", "int", m.getParameters()[0].getType().toString(false));
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
        Assert.assertEquals("Expected 'setBar(int)' but was 'setBar(Date)'", "int", m.getParameters()[0].getType().toString(false));
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
        Assert.assertEquals("Expected 'setBar(Date)' but was 'setBar(int)'", "java.util.Date", m.getParameters()[0].getType().toString(false));
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
        Assert.assertEquals("Expected 'setBar(int)' but was 'setBar(Date)'", "int", m.getParameters()[0].getType().toString(false));
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
        Assert.assertEquals("Expected 'setBar(int)' but was 'setBar(Date)'", "int", m.getParameters()[0].getType().toString(false));
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
        Assert.assertEquals("Expected 'setBar(Date)' but was 'setBar(int)'", "java.util.Date", m.getParameters()[0].getType().toString(false));
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
            "}\n" +
            "}";
        int offset = contents.indexOf("setValue");
        MethodNode m = assertDeclaration(contents, offset, offset + "setValue".length(), "Face", "setValue", DeclarationKind.METHOD);
        Assert.assertEquals("Expected 'setValue(String,boolean)'", "boolean", m.getParameters()[1].getType().toString(false));
    }
}
