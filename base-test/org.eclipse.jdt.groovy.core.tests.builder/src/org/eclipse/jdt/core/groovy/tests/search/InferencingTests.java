/*
 * Copyright 2009-2025 the original author or authors.
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
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.junit.Test;

public final class InferencingTests extends InferencingTestSuite {

    private void assertNoUnknowns(String source) {
        List<ASTNode> unknownNodes = new ArrayList<>();
        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(createUnit(DEFAULT_UNIT_NAME, source));
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
        // same as above, but ensure that whitespace isn't included
        assertType("10 ", 0, 2, "java.lang.Integer");
    }

    @Test
    public void testNumber3() {
        assertType("10L", "java.lang.Long");
    }

    @Test
    public void testNumber4() {
        assertType("-10", "java.lang.Integer");
    }

    @Test
    public void testNumber5() {
        assertType("+10", "java.lang.Integer");
    }

    @Test
    public void testNumber6() {
        assertType("10++", "java.lang.Integer");
    }

    @Test
    public void testNumber7() {
        assertType("++10", "java.lang.Integer");
    }

    @Test
    public void testNumber8() {
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
        String contents = "def x = predicate() ? 'literal' : something.toString()";
        assertType(contents, "x", "java.lang.String");

        contents = "def x = predicate() ? 'literal' : 42";
        assertType(contents, "x", "java.io.Serializable & java.lang.Comparable" +
            (Runtime.version().feature() > 11 ? " & java.lang.constant.Constable & java.lang.constant.ConstantDesc" : ""));
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
        assertType(contents, offset, offset + 1, "java.util.Collection" + (isAtLeastGroovy(50) ? "" : "<java.lang.Object>"));
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
        assertType(contents, offset, offset + 1, "java.util.Collection" + (isAtLeastGroovy(50) ? "" : "<java.lang.Object>"));
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
        assertType(contents, offset, offset + 1, "java.io.Serializable & java.lang.CharSequence" + (Runtime.version().feature() < 11 ? "" :  " & java.lang.Comparable"));
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
        assertType(contents, offset, offset + 1, "java.io.Serializable & java.lang.CharSequence" + (Runtime.version().feature() < 11 ? "" :  " & java.lang.Comparable"));
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
        assertType(contents, offset, offset + 1, "java.io.Serializable & java.lang.Comparable");
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
        assertType(contents, offset, offset + 1, "java.io.Serializable & java.lang.Comparable");
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
        assertType(contents, offset, offset + 1, "java.io.Serializable & java.lang.Comparable");
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
        String contents = "def m(n) {\n" +
            "  def x\n" +
            "  switch (n) {\n" +
            "   case 0:\n" +
            "    x = 42\n" +
            "    break\n" +
            "   case 1:\n" +
            "    x = 3.14\n" +
            "    break\n" +
            "  }\n" +
            "  x\n" +
            "}";

        // line 2
        int offset = contents.indexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Object");

        // line 5
        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Integer");

        // line 8
        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.math.BigDecimal");

        // line 11
        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Number & java.lang.Comparable");
    }

    @Test
    public void testLocalVar27() {
        String contents = "def m(n) {\n" +
            "  def x = null\n" +
            "  switch (n) {\n" +
            "   case 0:\n" +
            "    x = 42\n" +
            "    break\n" +
            "   case 1:\n" +
            "    x = 3.14\n" +
            "    break\n" +
            "   default:\n" +
            "    break" +
            "  }\n" +
            "  x\n" +
            "}";

        // line 2
        int offset = contents.indexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Object");

        // line 5
        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Integer");

        // line 8
        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.math.BigDecimal");

        // line 12
        offset = contents.indexOf("x", offset + 1);
        assertType(contents, offset, offset + 1, "java.lang.Object");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1086
    public void testLocalVar28() {
        String contents = "File findFile(param) {\n" +
            "  def file\n" +
            "  switch (param) {\n" +
            "   case String:\n" +
            "    file = new File(System.getProperty('x'), '.ext')\n" +
            "    break\n" +
            "   case Number:\n" +
            "    file = new File(System.getProperty('y'))\n" +
            "    break\n" +
            "  }\n" +
            "  file.canonicalFile\n" +
            "}";

        // line 2
        int offset = contents.indexOf("file");
        assertType(contents, offset, offset + 4, "java.lang.Object");

        // line 5
        offset = contents.indexOf("file", offset + 4);
        assertType(contents, offset, offset + 4, "java.io.File");

        // line 8
        offset = contents.indexOf("file", offset + 4);
        assertType(contents, offset, offset + 4, "java.io.File");

        // line 11
        offset = contents.indexOf("file", offset + 4);
        assertType(contents, offset, offset + 4, "java.io.File");
    }

    @Test
    public void testLocalVar29() {
        String contents = "String x\n" +
            "x";
        assertType(contents, "x", "java.lang.String");
    }

    @Test
    public void testLocalVar30() {
        String contents = "String x = 7\n" +
            "x";
        assertType(contents, "x", "java.lang.String");
    }

    @Test
    public void testLocalVar31() {
        String contents = "String x\n" +
            "x = 7\n" + // GroovyCastException at runtime
            "x";
        assertType(contents, "x", "java.lang.String");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1105
    public void testLocalVar32() {
        String contents = "void test(a) {\n" +
            "  def x = a.b\n" +
            "}";
        assertType(contents, "b", "java.lang.Object");
        assertType(contents, "x", "java.lang.Object");
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
    public void testLocalMethod6() {
        String contents =
            "int f(int x) {}\n" +
            "int g(x) {}\n" +
            "f()\n" + // no
            "g()\n"; // yes
        assertUnknown(contents, "f");
        assertType(contents, "g", "java.lang.Integer");
    }

    @Test
    public void testMatcher1() {
        String contents = "def x = ('' =~ /pattern/)";
        assertType(contents, "x", "java.util.regex.Matcher");
    }

    @Test
    public void testMatcher2() {
        String contents = "('' =~ /pattern/).hasGroup()";
        assertType(contents, "hasGroup", "java.lang.Boolean");
    }

    @Test
    public void testPattern1() {
        String contents = "def x = ~/pattern/";
        assertType(contents, "x", "java.util.regex.Pattern");
    }

    @Test
    public void testPattern2() {
        String contents = "def x = \"\" ==~ /pattern/";
        assertType(contents, "x", "java.lang.Boolean");
    }

    @Test
    public void testSpread1() {
        String contents = "def x = ['1','2']*.bytes";
        assertType(contents, "bytes", "byte[]");
        assertType(contents, "x", "java.util.List<byte[]>");
    }

    @Test
    public void testSpread2() {
        String contents = "def x = ['1','2']*.getBytes()";
        assertType(contents, "getBytes", "byte[]");
        assertType(contents, "x", "java.util.List<byte[]>");
    }

    @Test
    public void testSpread3() {
        String contents = "def x = [1,2,3]*.intValue()";
        assertType(contents, "intValue", "java.lang.Integer");
        assertType(contents, "x", "java.util.List<java.lang.Integer>");
    }

    @Test
    public void testSpread4() {
        String contents = "def x = [1,2,3]*.intValue()[0].intValue()";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testSpread5() {
        String contents = "def x = [a:1,b:2]*.getKey()";
        assertType(contents, "getKey", "java.lang.String");
        assertType(contents, "x", "java.util.List<java.lang.String>");
    }

    @Test
    public void testSpread6() {
        String contents = "def x = [a:1,b:2,c:3]*.getValue()";
        assertType(contents, "getValue", "java.lang.Integer");
        assertType(contents, "x", "java.util.List<java.lang.Integer>");
    }

    @Test
    public void testSpread7() {
        String contents = "def x = [a:1,b:2,c:3]*.key";
        assertType(contents, "key", "java.lang.String");
        assertType(contents, "x", "java.util.List<java.lang.String>");
    }

    @Test
    public void testSpread8() {
        String contents = "def x = [a:1,b:2,c:3]*.value";
        assertType(contents, "value", "java.lang.Integer");
        assertType(contents, "x", "java.util.List<java.lang.Integer>");
    }

    @Test
    public void testSpread9() {
        String contents = "def x = [a:1,b:2,c:3]*.key[0].toLowerCase()";
        assertType(contents, "x", "java.lang.String");
    }

    @Test
    public void testSpread10() {
        String contents = "def x = [a:1,b:2,c:3]*.value[0].intValue()";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testSpread11() {
        String contents = "def x = ['1','2','3']*.bytes[0].length";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testSpread12() {
        String contents = "Set<String> strings = ['1','2','3']; def x = strings*.bytes";
        assertType(contents, "x", "java.util.List<byte[]>");
        assertType(contents, "bytes", "byte[]");
    }

    @Test
    public void testSpread13() {
        String contents = "Set<String> strings = ['1','2','3']; def x = strings*.length()";
        assertType(contents, "x", "java.util.List<java.lang.Integer>");
        assertType(contents, "length", "java.lang.Integer");
    }

    @Test
    public void testSpread14() {
        String contents =
            "@groovy.transform.TypeChecked\n" +
            "void test(Set<java.beans.BeanInfo> beans) {\n" +
            "  beans*.additionalBeanInfo\n" +
            "}\n";
        assertType(contents, "beans", "java.util.Set<java.beans.BeanInfo>");
        assertType(contents, "additionalBeanInfo", "java.beans.BeanInfo[]");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1594
    public void testSpread15() {
        String contents =
            "@groovy.transform.TypeChecked\n" +
            "void test(Set<java.beans.BeanInfo> beans) {\n" +
            "  def xx = beans*.getDefaultPropertyIndex()\n" +
            "}\n";
        assertType(contents, "xx", "java.util.List<java.lang.Integer>");
        assertType(contents, "getDefaultPropertyIndex", "java.lang.Integer");
    }

    @Test // GROOVY-9021
    public void testSpread16() {
        createJavaUnit("Pojo",
            "interface Pojo {\n" +
            "  java.util.Collection<? extends java.lang.String> getStrings();\n" +
            "}\n");

        String contents =
            "@groovy.transform.TypeChecked\n" +
            "void test(Pojo pojo) {\n" +
            "  def result = pojo.strings*.bytes\n" + // exercises StaticTypeCheckingVisitor#getTypeForSpreadExpression
            "}\n";
        assertType(contents, "strings", "java.util.Collection<? extends java.lang.String>");
        assertType(contents, "result", "java.util.List<byte[]>");
    }

    @Test // GROOVY-9021
    public void testSpread17() {
        createJavaUnit("Pojo",
            "interface Pojo {\n" +
            "  java.util.List<? extends java.lang.String> getStrings();\n" +
            "}\n");

        String contents =
            "@groovy.transform.TypeChecked\n" +
            "void test(Pojo pojo) {\n" +
            "  def result = pojo.strings*.bytes\n" + // exercises StaticTypeCheckingVisitor#getTypeForListExpression
            "}\n";
        assertType(contents, "strings", "java.util.List<? extends java.lang.String>");
        assertType(contents, "result", "java.util.List<byte[]>");
    }

    @Test
    public void testSpread18() {
        createJavaUnit("Pojo",
            "interface Pojo {\n" +
            "  java.util.Map<String, ? extends java.lang.String> getStrings();\n" +
            "}\n");

        String contents =
            "@groovy.transform.TypeChecked\n" +
            "void test(Pojo pojo) {\n" +
            "  def result = pojo.strings*.value\n" + // exercises StaticTypeCheckingVisitor#getTypeForMapExpression
            "}\n";
        assertType(contents, "strings", "java.util.Map<java.lang.String,? extends java.lang.String>");
        assertType(contents, "result", "java.util.List<? extends java.lang.String>");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/763
    public void testSpread19() {
        String contents = "def strings = [[['1','2','3']]]\n" +
            "def result = strings*.length()\n";
        assertType(contents, "result", "java.util.List<java.util.List>");
    }

    @Test // CommandRegistry.iterator() lacks generics
    public void testSpread20() {
        String contents =
            "import org.codehaus.groovy.tools.shell.CommandRegistry\n" +
            "def registry = new CommandRegistry()\n" +
            "def result = registry*.with {it}\n";
        assertType(contents, "result", "java.util.List<java.lang.Object>");
    }

    @Test
    public void testSpread21() {
        String contents =
            "import java.util.regex.Matcher\n" +
            "Matcher matcher = ('abc' =~ /./)\n" +
            "def result = matcher*.with {it}\n";
        assertType(contents, "result", "java.util.List<java.lang.Object>");
    }

    @Test
    public void testSpread22() {
        String contents =
            "Reader reader = null\n" +
            "def result = reader*.with {it}\n";
        assertType(contents, "result", "java.util.List<java.lang.String>");
    }

    @Test
    public void testSpread23() {
        String contents =
            "void m(String str, List list, Number n) {}\n" +
            "void test(Tuple3<String,List,Long> tuple){\n" +
            "  m(*tuple)\n" +
            "}\n";
        assertType(contents, "m", "java.lang.Void");
    }

    @Test
    public void testMapLiteral() {
        assertType("[:]", "java.util.Map<java.lang.Object,java.lang.Object>");
    }

    @Test // GROOVY-9021
    public void testMapProperty() {
        createJavaUnit("Pojo",
            "interface Pojo {\n" +
            "  java.util.Map<String, ? extends Number> getMap();\n" +
            "}\n");

        String contents =
            "@groovy.transform.TypeChecked\n" +
            "void test(Pojo pojo) {\n" +
            "  def result = pojo.map.name\n" + // exercises StaticTypeCheckingVisitor#getTypeForMapExpression
            "}\n";
        assertType(contents, "map", "java.util.Map<java.lang.String,? extends java.lang.Number>");
        assertType(contents, "result", "java.lang.Number");
    }

    @Test // GROOVY-11357
    public void testMapProperty2() {
        createJavaUnit("p", "Pojo",
            "package p;" +
            "abstract public class Pojo {\n" +
            "  String getName() {return null;}\n" +
            "}\n");

        String contents =
            "class Pogo extends p.Pojo implements Map<String, Number> {\n" +
            "  @Delegate Map<String, Number> map" +
            "}\n" +
            "new Pogo().name\n";
        assertType(contents, "name", "java.lang.Number");
    }

    @Test
    public void testNotMapProperty() {
        String contents =
            "class Pogo extends HashMap<String,String> {\n" +
            "  private Number name\n" +
            "  void test() {\n" +
            "    def value = this.name\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "name", "java.lang.Number");
        assertType(contents, "value", "java.lang.Number");
    }

    @Test // GROOVY-5001
    public void testNotMapProperty2() {
        String contents =
            "class Pogo extends HashMap<String,String> {\n" +
            "  public Number name\n" +
            "}\n" +
            "new Pogo().name\n";
        assertType(contents, "name", isAtLeastGroovy(50) ? "java.lang.Number" : "java.lang.String");
    }

    @Test // GROOVY-11367
    public void testNotMapProperty3() {
        String contents =
            "Map map= [:]\n" +
            "map.with {  \n" +
            "  delegate  \n" +
            "  directive \n" +
            "  metaClass \n" +
            "  thisObject;\n" +
            "  { -> owner}\n" +
            "}\n";
        // DELEGATE_FIRST: Closure really comes first
        assertType(contents, "delegate", "java.util.Map");
        assertDeclaringType(contents, "delegate", "groovy.lang.Closure<V extends java.lang.Object>");
        assertType(contents, "directive", "java.lang.Integer");
        assertDeclaringType(contents, "directive", "groovy.lang.Closure");
        assertType(contents, "metaClass", "groovy.lang.MetaClass");
        assertDeclaringType(contents, "metaClass", "groovy.lang.GroovyObjectSupport");
        assertType(contents, "thisObject", DEFAULT_UNIT_NAME);
        assertDeclaringType(contents, "thisObject", "groovy.lang.Closure<V extends java.lang.Object>");
        assertType(contents, "owner", "groovy.lang.Closure"); // not DEFAULT_UNIT_NAME
        assertDeclaringType(contents, "owner", "groovy.lang.Closure<V extends java.lang.Object>");
    }

    @Test
    public void testBoolean1() {
        assertType("!x", "java.lang.Boolean");
    }

    @Test
    public void testBoolean2() {
        assertType("!!x", "java.lang.Boolean");
    }

    @Test
    public void testBoolean3() {
        String contents = "(x < y)";
        assertType(contents, "java.lang.Boolean");
    }

    @Test
    public void testBoolean4() {
        String contents = "(x <= y)";
        assertType(contents, "java.lang.Boolean");
    }

    @Test
    public void testBoolean5() {
        String contents = "(x != y)";
        assertType(contents, "java.lang.Boolean");
    }

    @Test
    public void testBoolean6() {
        String contents = "(x == y)";
        assertType(contents, "java.lang.Boolean");
    }

    @Test
    public void testBoolean7() {
        assumeTrue(isParrotParser());

        String contents = "(x === y)";
        assertType(contents, "java.lang.Boolean");
    }

    @Test
    public void testBoolean8() {
        String contents = "(x in y)";
        assertType(contents, "java.lang.Boolean");
    }

    @Test
    public void testBoolean9() {
        String contents = "(x instanceof Object)";
        assertType(contents, "java.lang.Boolean");
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

    @Test // GRECLIPSE-1229: constructors with map parameters
    public void testConstructor1() {
        String contents =
            "class C {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "new C(aa: 1, bb:8)\n";

        int start = contents.lastIndexOf("aa");
        int end = start + "aa".length();
        assertType(contents, start, end, "java.lang.Boolean");
        assertDeclaration(contents, start, end, "C", "aa", DeclarationKind.PROPERTY);

        start = contents.lastIndexOf("bb");
        end = start + "bb".length();
        assertType(contents, start, end, "java.lang.Integer");
        assertDeclaration(contents, start, end, "C", "bb", DeclarationKind.PROPERTY);
    }

    @Test
    public void testConstructor2() {
        String contents =
            "class C {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "new C([aa: 1, bb:8])\n";

        int start = contents.lastIndexOf("aa");
        int end = start + "aa".length();
        assertType(contents, start, end, "java.lang.Boolean");
        assertDeclaration(contents, start, end, "C", "aa", DeclarationKind.PROPERTY);

        start = contents.lastIndexOf("bb");
        end = start + "bb".length();
        assertType(contents, start, end, "java.lang.Integer");
        assertDeclaration(contents, start, end, "C", "bb", DeclarationKind.PROPERTY);
    }

    @Test
    public void testConstructor3() {
        String contents =
            "class C {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "new C([8: 1, bb:8])\n";

        int start = contents.lastIndexOf("bb");
        int end = start + "bb".length();
        assertType(contents, start, end, "java.lang.Integer");
        assertDeclaration(contents, start, end, "C", "bb", DeclarationKind.PROPERTY);
    }

    @Test
    public void testConstructor4() {
        String contents =
            "class C {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "new C([aa: 1, bb:8], 9)\n";

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
            "class C {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "new C(9, [aa: 1, bb:8])\n";

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
            "class C {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "def c = C[aa: 1, bb:8]\n";

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
            "}\n";
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
            "}\n";
        assertType(contents, "super()", "java.lang.Void");
        assertDeclaringType(contents, "super()", "java.util.HashMap");
    }

    @Test
    public void testStaticThisAndSuper1() {
        String contents =
            "class A {\n" +
            "  static main(args) {\n" +
            "    this\n" +
            "    super\n" +
            "  }\n" +
            "}\n";
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
            "}\n";
        assertType(contents, "this", "java.lang.Class<B>");
        assertType(contents, "super", "java.lang.Class<A>");
    }

    @Test
    public void testOtherFieldReference1() {
        for (String mods : List.of("public", "protected", "@groovy.transform.PackageScope")) {
            String contents =
                "abstract class A {\n" +
                "  " + mods + " Number field\n" +
                "}\n" +
                "void test(A a) {\n" +
                "  a.@field\n" +
                "}\n";
            assertType(contents, "field", "java.lang.Number");
        }
    }

    @Test
    public void testOtherFieldReference2() {
        for (String mods : List.of("public", "protected", "@groovy.transform.PackageScope")) {
            String contents =
                "abstract class A {\n" +
                "  " + mods + " Number field\n" +
                "}\n" +
                "void test(A a) {\n" +
                "  a.field\n" +
                "}\n";
            assertType(contents, "field", "java.lang.Number");
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1567
    public void testOtherFieldReference3() {
        String contents =
            "abstract class A {\n" +
            "  private Number field\n" +
            "}\n" +
            "void test(A a) {\n" +
            "  a.@field\n" +
            "}\n";
        assertUnknown(contents, "field");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1567
    public void testOtherFieldReference4() {
        String contents =
            "abstract class A {\n" +
            "  private Number field\n" +
            "}\n" +
            "void test(A a) {\n" +
            "  a.field\n" +
            "}\n";
        assertUnknown(contents, "field");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1567
    public void testOtherFieldReference5() {
        String contents =
            "abstract class A {\n" +
            "  private Number field\n" +
            "}\n" +
            "void test(A a) {\n" +
            "  a.with {field}\n" +
            "}\n";
        assertUnknown(contents, "field");
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
            "}\n";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference2() {
        String contents =
            "class A {\n" +
            "  public Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    this.field\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference3() {
        String contents =
            "class A {\n" +
            "  public Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    this.@field\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference4() {
        String contents =
            "class A {\n" +
            "  public Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.field\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference5() {
        String contents =
            "class A {\n" +
            "  public Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.@field\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference6() {
        String contents =
            "class A {\n" +
            "  protected Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    field\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference7() {
        String contents =
            "class A {\n" +
            "  protected Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    this.field\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference8() {
        String contents =
            "class A {\n" +
            "  protected Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    this.@field\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference9() {
        String contents =
            "class A {\n" +
            "  protected Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.field\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test
    public void testSuperFieldReference10() {
        String contents =
            "class A {\n" +
            "  protected Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.@field\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "field", "java.lang.Number");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperFieldReference11() {
        String contents =
            "class A {\n" +
            "  private String field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    field\n" +
            "  }\n" +
            "}\n";
        assertUnknown(contents, "field");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperFieldReference12() {
        String contents =
            "class A {\n" +
            "  private Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    this.field\n" +
            "  }\n" +
            "}\n";
        assertUnknown(contents, "field");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperFieldReference13() {
        String contents =
            "class A {\n" +
            "  private Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    this.@field\n" +
            "  }\n" +
            "}\n";
        assertUnknown(contents, "field");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperFieldReference14() {
        String contents =
            "class A {\n" +
            "  private Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.field\n" +
            "  }\n" +
            "}\n";
        assertUnknown(contents, "field");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperFieldReference15() {
        String contents =
            "class A {\n" +
            "  private Number field\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.@field\n" +
            "  }\n" +
            "}\n";
        assertUnknown(contents, "field");
    }

    @Test
    public void testSuperFieldReference16() {
        String contents =
            "public interface Constants {\n" +
            "  int FIRST = 1;\n" +
            "}\n" +
            "class UsesConstants implements Constants {\n" +
            "  def x() {\n" +
            "    FIRST\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "FIRST", "java.lang.Integer");
    }

    @Test
    public void testSuperFieldReference17() {
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
            "}\n";
        assertType(contents, "CONST", "java.lang.Integer");
    }

    @Test
    public void testSuperFieldReference18() {
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
            "}\n";
        assertType(contents, "CONST", "java.lang.Integer");
    }

    @Test
    public void testSuperFieldReference19() {
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
            "}\n";
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
            "}\n";
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
            "}\n";
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
            "}\n";
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
            "}\n";
        assertType(contents, "value", "java.lang.Number");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/996 and GROOVY-8999
    public void testSuperPropertyReference5() {
        String contents =
            "class A {\n" +
            "  Number value\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def method() {\n" +
            "    super.@value\n" +
            "  }\n" +
            "}\n";
        assertUnknown(contents, "value");
    }

    @Test // GROOVY-6097
    public void testSuperPropertyReference6() {
        for (String qual : List.of("", "this.", "super.")) {
            String contents =
                "class A {\n" +
                "  boolean isValue() {}\n" +
                "  boolean getValue() {}\n" +
                "}\n" +
                "class B extends A {\n" +
                "  void test() {\n" +
                "    " + qual + "value\n" +
                "  }\n" +
                "}\n";
            int offset = contents.lastIndexOf("value");
            boolean getValue = qual.startsWith("super") && !isAtLeastGroovy(40);
            assertDeclaration(contents, offset, offset + 5, "A", getValue ? "getValue" : "isValue", DeclarationKind.METHOD);
        }
    }

    @Test // GROOVY-1736
    public void testSuperPropertyReference7() {
        for (String qual : List.of("", "this.", "super.")) {
            String contents =
                "class A {\n" +
                "  boolean isValue() {}\n" +
                "}\n" +
                "class B extends A {\n" +
                "  boolean getValue() {}\n" + // TODO: warning
                "  void test() {\n" +
                "    " + qual + "value\n" +
                "  }\n" +
                "}\n";
            int offset = contents.lastIndexOf("value");
            if (qual.startsWith("super") && !isAtLeastGroovy(40)) {
                assertUnknownConfidence(contents, offset, offset + 5);
            } else {
                assertDeclaration(contents, offset, offset + 5, "A", "isValue", DeclarationKind.METHOD);
            }
        }
    }

    @Test
    public void testSuperPropertyReference8() {
        for (String qual : List.of("", "this.", "super.")) {
            String contents =
                "class A {\n" +
                "  boolean value\n" +
                "}\n" +
                "class B extends A {\n" +
                "  boolean getValue() {}\n" + // TODO: warning
                "  void test() {\n" +
                "    " + qual + "value\n" +
                "  }\n" +
                "}\n";
            int offset = contents.lastIndexOf("value");
            assertDeclaration(contents, offset, offset + 5, "A", "value", DeclarationKind.PROPERTY);
        }
    }

    @Test // isX only applies to [Bb]oolean
    public void testSuperPropertyReference9() {
        for (String qual : List.of("", "this.", "super.")) {
            String contents =
                "class A {\n" +
                "  Number isValue() {}\n" +
                "  Number getValue() {}\n" +
                "}\n" +
                "class B extends A {\n" +
                "  void test() {\n" +
                "    " + qual + "value\n" +
                "  }\n" +
                "}\n";
            int offset = contents.lastIndexOf("value");
            assertDeclaration(contents, offset, offset + 5, "A", "getValue", DeclarationKind.METHOD);
        }
    }

    @Test
    public void testSuperPropertyReference10() {
        for (String qual : List.of("", "this.", "super.")) {
            String contents =
                "class A {\n" +
                "  boolean value\n" +
                "  boolean getValue() {}\n" + // no isValue() generated
                "}\n" +
                "class B extends A {\n" +
                "  void test() {\n" +
                "    " + qual + "value\n" +
                "    " + qual + "isValue()\n" +
                "  }\n" +
                "}\n";
            assertUnknown(contents, "isValue");
            int offset = contents.lastIndexOf("value");
            assertDeclaration(contents, offset, offset + 5, "A", "getValue", DeclarationKind.METHOD);
        }
    }

    @Test // GROOVY-6097
    public void testSuperPropertyReference11() {
        for (String qual : List.of("", "this.", "super.")) {
            String contents =
                "class A {\n" +
                "  boolean value\n" +
                "  boolean isValue() {}\n" + // no getValue() generated
                "}\n" +
                "class B extends A {\n" +
                "  void test() {\n" +
                "    " + qual + "value\n" +
                "    " + qual + "getValue()\n" +
                "  }\n" +
                "}\n";
            assertUnknown(contents, "getValue");
            if (qual.startsWith("super") && !isAtLeastGroovy(40)) {
                assertUnknown(contents, "value");
            } else {
                int offset = contents.lastIndexOf("value");
                assertDeclaration(contents, offset, offset + 5, "A", "isValue", DeclarationKind.METHOD);
            }
        }
    }

    @Test
    public void testSuperPropertyReference12() {
        for (String qual : List.of("", "this.", "super.")) {
            String contents =
                "class A {\n" +
                "  Boolean value\n" +
                "  Boolean isValue() {}\n" + // getValue() generated
                "}\n" +
                "class B extends A {\n" +
                "  void test() {\n" +
                "    " + qual + "value\n" +
                "    " + qual + "getValue()\n" +
                "  }\n" +
                "}\n";
            int offset = contents.lastIndexOf("value");
            assertDeclaration(contents, offset, offset + 5, "A", "value", DeclarationKind.PROPERTY);
            /**/offset = contents.lastIndexOf("getValue");
            assertDeclaration(contents, offset, offset + 8, "A", "getValue", DeclarationKind.METHOD);
        }
    }

    @Test
    public void testSuperPropertyReference13() {
        for (String mods : List.of("", "private ", "public ", "protected ")) {
            createJavaUnit("A",
                "public abstract class A {\n" +
                "  " + mods + "int getValue() {\n" +
                "    return 0;\n" +
                "  }\n" +
                "}\n");

            String contents =
                "class B extends A {\n" +
                "  void test() {\n" +
                "    value\n" +
                "    getValue()\n" +
                "  }\n" +
                "}\n";
            if (mods.startsWith("private")) { // GROOVY-11356
                assertUnknown(contents, "value");
                assertUnknown(contents, "getValue");
            } else {
                int offset = contents.lastIndexOf("value");
                assertDeclaration(contents, offset, offset + 5, "A", "getValue", DeclarationKind.METHOD);
                /**/offset = contents.lastIndexOf("getValue");
                assertDeclaration(contents, offset, offset + 8, "A", "getValue", DeclarationKind.METHOD);
            }
        }
    }

    @Test
    public void testSuperPropertyReference14() {
        for (String mods : List.of("", "public ", "protected ")) {
            createJavaUnit("p", "A",
                "public abstract class A {\n" +
                "  " + mods + "int getValue() {\n" +
                "    return 0;\n" +
                "  }\n" +
                "}\n");

            String contents =
                "class B extends p.A {\n" +
                "  void test() {\n" +
                "    value\n" +
                "    getValue()\n" +
                "  }\n" +
                "}\n";
            if (mods.isEmpty() && isAtLeastGroovy(50)) { // GROOVY-11357
                assertUnknown(contents, "value");
                assertUnknown(contents, "getValue");
            } else {
                int offset = contents.lastIndexOf("value");
                assertDeclaration(contents, offset, offset + 5, "p.A", "getValue", DeclarationKind.METHOD);
                /**/offset = contents.lastIndexOf("getValue");
                assertDeclaration(contents, offset, offset + 8, "p.A", "getValue", DeclarationKind.METHOD);
            }
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1567
    public void testOtherPropertyReference1() {
        String contents =
            "abstract class A {\n" +
            "  private getValue() {}\n" +
            "}\n" +
            "void test(A a) {\n" +
            "  a.value\n" +
            "  a.getValue()\n" +
            "}\n";
        assertUnknown(contents, "value");
        assertUnknown(contents, "getValue");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1567
    public void testOtherPropertyReference2() {
        String contents =
            "abstract class A {\n" +
            "  private getValue() {}\n" +
            "}\n" +
            "void test(A a) {\n" +
            "  a.with {\n" +
            "    value\n" +
            "    getValue()\n" +
            "  }\n" +
            "}\n";
        assertUnknown(contents, "value");
        assertUnknown(contents, "getValue");
    }

    @Test
    public void testOtherPropertyReference3() {
        createJavaUnit("p", "A",
            "public abstract class A {\n" +
            "  int field\n" +
            "  int getValue() {\n" +
            "    return 0;\n" +
            "  }\n" +
            "}\n");

        createJavaUnit("p", "C",
            "public class C extends A {\n" +
            "}\n");

        // A and C are in same package
        // Groovy 2.4+ propagates package-private fields
        // Groovy 1.5+ propagates package-private methods

        String contents =
            "def pojo = new p.C()\n" +
            "pojo.field\n" +
            "pojo.@field\n" +
            "pojo.with{field}\n" +
            "pojo.value\n" +
            "pojo.getValue()\n";

        int offset = contents.indexOf("field");
        assertDeclaration(contents, offset, offset + 5, "p.A", "field", DeclarationKind.FIELD);
        /**/offset = contents.indexOf("@field") + 1;
        assertDeclaration(contents, offset, offset + 5, "p.A", "field", DeclarationKind.FIELD);
        /**/offset = contents.lastIndexOf("field");
        assertDeclaration(contents, offset, offset + 5, "p.A", "field", DeclarationKind.FIELD);
        /**/offset = contents.lastIndexOf("value");
        assertDeclaration(contents, offset, offset + 5, "p.A", "getValue", DeclarationKind.METHOD);
        /**/offset = contents.lastIndexOf("getValue");
        assertDeclaration(contents, offset, offset + 8, "p.A", "getValue", DeclarationKind.METHOD);
    }

    @Test // GROOVY-11357
    public void testOtherPropertyReference4() {
        createJavaUnit("p", "A",
            "public abstract class A {\n" +
            "  int field\n" +
            "  int getValue() {\n" +
            "    return 0;\n" +
            "  }\n" +
            "}\n");

        createJavaUnit("q", "C",
            "public class C extends p.A {\n" +
            "}\n");

        // A and C are not in same package
        // Groovy doesn't propagate package-private fields
        // Groovy 5+ doesn't propagate package-private methods

        String contents =
            "def pojo = new q.C()\n" +
            "pojo.field\n" +
            "pojo.@field\n" +
            "pojo.with{field}\n" +
            "pojo.value\n" +
            "pojo.getValue()\n";

        int offset = contents.indexOf("field");
        assertUnknownConfidence(contents, offset, offset + 5);
        /**/offset = contents.indexOf("@field") + 1;
        assertUnknownConfidence(contents, offset, offset + 5);
        /**/offset = contents.lastIndexOf("field");
        assertUnknownConfidence(contents, offset, offset + 5);

        if (isAtLeastGroovy(50)) {
            assertUnknown(contents, "value");
            assertUnknown(contents, "getValue");
        } else {
            /**/offset = contents.lastIndexOf("value");
            assertDeclaration(contents, offset, offset + 5, "p.A", "getValue", DeclarationKind.METHOD);
            /**/offset = contents.lastIndexOf("getValue");
            assertDeclaration(contents, offset, offset + 8, "p.A", "getValue", DeclarationKind.METHOD);
        }
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
            "  void test() {\n" +
            "    method()\n" +
            "  }\n" +
            "}\n";
        assertDeclaringType(contents, "method", "A");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperClassMethod4() {
        String contents =
            "class A {\n" +
            "  private void method() {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "  void test() {\n" +
            "    method()\n" +
            "  }\n" +
            "}\n";
        assertUnknown(contents, "method");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/815
    public void testSuperClassMethod4a() {
        String contents =
            "class A {\n" +
            "  private void method() {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "  void test() {\n" +
            "    this.method()\n" +
            "  }\n" +
            "}\n";
        assertUnknown(contents, "method");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1378
    public void testSuperClassMethod4b() {
        String contents =
            "class A {\n" +
            "  private void method() {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "  void test() {\n" +
            "    super.method()\n" + // GROOVY-9851
            "  }\n" +
            "}\n";
        if (isAtLeastGroovy(40)) {
            assertUnknown(contents, "method");
        } else {
            assertDeclaringType(contents, "method", "A");
        }
    }

    @Test
    public void testSuperClassMethod4c() {
        String contents =
            "class A {\n" +
            "  private void method() {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "  void test() {\n" +
            "    super.&method\n" + // GROOVY-9851: resolves to MethodClosure, but it fails when called
            "  }\n" +
            "}\n";
        assertUnknown(contents, "method");
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
            "}\n";
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
            "}\n";
        assertUnknown(contents, "run");
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
            "}\n";
        assertDeclaringType(contents, "hashCode", "java.lang.Object");
    }

    @Test // GROOVY-9884
    public void testSuperInterfaceMethod1() {
        String contents =
            "class C {\n" +
            "  Object getProperty(String name) {\n" +
            "    name == 'foo' ? 'bar' : super.getProperty(name)\n" +
            "  }\n" +
            "}\n";
        if (!isAtLeastGroovy(40)) {
            assertUnknown(contents, "getProperty");
        } else {
            assertType(contents, "getProperty", "java.lang.Object");
            assertDeclaringType(contents, "getProperty", "groovy.lang.GroovyObject");
        }
    }

    @Test // GROOVY-9909
    public void testSuperInterfaceMethod2() {
        String contents =
            "class C implements Comparator<String> {\n" +
            "  Comparator<String> reversed() {\n" +
            "    super.reversed()\n" +
            "  }\n" +
            "}\n";
        if (!isAtLeastGroovy(40)) {
            assertUnknown(contents, "reversed");
        } else {
            assertType(contents, "reversed", "java.util.Comparator<java.lang.String>");
            assertDeclaringType(contents, "reversed", "java.util.Comparator");
        }
    }

    @Test // super-interface static method requires qualifier
    public void testSuperInterfaceMethod3() {
        for (String qual : List.of("", "this.", "super.", "Comparator.", "Comparator.<String>")) {
            String contents =
                "class C implements Comparator<String> {\n" +
                "  static m() {\n" +
                "    " + qual + "naturalOrder()\n" +
                "  }\n" +
                "}\n";
            if (!qual.startsWith("C")) {
                assertUnknown(contents, "naturalOrder");
            } else {
                assertType(contents, "naturalOrder", "java.util.Comparator<java.lang." + (qual.endsWith(">") ? "String" : "Comparable") + ">");
            }
        }
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
    public void testFieldWithInitializer3b() {
        String contents =
            "class C {\n" +
            "  def m() {\n" +
            "    x\n" +
            "  }\n" +
            "  C() {\n" +
            "    x = 42\n" +
            "  }\n" +
            "  def x\n" +
            "}\n";
        int offset = contents.indexOf('x');
        assertType(contents, offset, offset + 1, "java.lang.Integer");
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

    @Test
    public void testFieldWithInitializer6() {
        String contents =
            "class C {\n" +
            "  def x\n" +
            "  @javax.annotation.PostConstruct\n" +
            "  void init() {\n" +
            "    x = 42\n" +
            "  }\n" +
            "  def m() {\n" +
            "    x\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testFieldWithInitializer6a() {
        String contents =
            "import javax.annotation.PostConstruct\n" +
            "class C {\n" +
            "  def x\n" +
            "  @PostConstruct\n" +
            "  void init() {\n" +
            "    x = 42\n" +
            "  }\n" +
            "  def m() {\n" +
            "    x\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testFieldWithInitializer6b() {
        String contents =
            "import javax.annotation.PostConstruct\n" +
            "class C {\n" +
            "  def m() {\n" +
            "    x\n" +
            "  }\n" +
            "  @PostConstruct\n" +
            "  void init() {\n" +
            "    x = 42\n" +
            "  }\n" +
            "  def x\n" +
            "}\n";
        int offset = contents.indexOf('x', contents.indexOf("def"));
        assertType(contents, offset, offset + 1, "java.lang.Object"); // TODO: Make independent of declaration order?
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
        String contents = "def aic = new Object() {}";
        assertType(contents, "aic", "java.lang.Object");
        assertType(contents, "Object", "java.lang.Object");
    }

    @Test
    public void testAnonInner2() {
        String contents = "def aic = new Cloneable() {}";
        assertType(contents, "aic", "java.lang.Cloneable");
        assertType(contents, "Cloneable", "java.lang.Cloneable");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1523
    public void testAnonInner3() {
        for (String mode : List.of("TypeChecked", "CompileStatic", "CompileDynamic")) {
            String contents = "@groovy.transform." + mode + " m() {\n" +
                "  def aic = new Number() {}\n" +
                "  def cia = new Cloneable() {}\n" +
                "}";
            assertType(contents, "aic", "java.lang.Number");
            assertType(contents, "cia", "java.lang.Cloneable");
        }
    }

    @Test
    public void testAnonInner4() {
        String contents = "def aic = new Comparable<String>() {\n int compareTo(String that) {}\n}";
        assertType(contents, "Comparable", "java.lang.Comparable<java.lang.String>");
    }

    @Test
    public void testAnonInner5() {
        String contents = "def aic = new Comparable<String>() {\n int compareTo(String that) {\n  compareTo('x')\n}\n}";
        assertDeclaringType(contents, "compareTo", "Search$1");
    }

    @Test
    public void testAnonInner6() {
        String contents = "def aic = new Comparable<String>() {\n int compareTo(String that) {}\n}\n" +
            "aic.compareTo('x')";
        assertDeclaringType(contents, "compareTo", "java.lang.Comparable");
    }

    @Test
    public void testAnonInner7() {
        String contents = "def aic = new Comparable<Integer>() {\n int compareTo(Integer that) {}\n}\n" +
            "aic = new Comparable<String>() {\n int compareTo(String that) {}\n}\n" +
            "aic.compareTo('x')";
        assertDeclaringType(contents, "compareTo", "java.lang.Comparable");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/378
    public void testAnonInner8() {
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
    public void testAnonInner9() {
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
        String contents = "try {\n} catch (e) {\n}\n";

        int offset = contents.lastIndexOf("e");
        assertType(contents, offset, offset + 1, "java.lang.Exception");
    }

    @Test
    public void testCatchBlock2() {
        String contents = "try {\n} catch (e) {\n e\n}\n";

        int offset = contents.lastIndexOf("e");
        assertType(contents, offset, offset + 1, "java.lang.Exception");
    }

    @Test
    public void testCatchBlock3() {
        String contents = "try {\n} catch (NullPointerException e) {\n}\n";

        int offset = contents.lastIndexOf("NullPointerException");
        assertType(contents, offset, offset + 20, "java.lang.NullPointerException");
    }

    @Test
    public void testCatchBlock4() {
        String contents = "try {\n} catch (NullPointerException e) {\n}\n";

        int offset = contents.lastIndexOf("e");
        assertType(contents, offset, offset + 1, "java.lang.NullPointerException");
    }

    @Test
    public void testCatchBlock5() {
        String contents = "try {\n} catch (NullPointerException e) {\n e\n}\n";

        int offset = contents.lastIndexOf("e");
        assertType(contents, offset, offset + 1, "java.lang.NullPointerException");
    }

    @Test
    public void testCatchBlock6() {
        String contents = "try {\n} catch (Exception | Error e) {\n}\n";

        int offset = contents.lastIndexOf("e");
        assertType(contents, offset, offset + 1, "java.lang.Exception or java.lang.Error");
    }

    private static final String CONTENTS_GETAT1 =
        "class GetAt {\n" +
        "  String getAt(foo) {}\n" +
        "}\n" +
        "\n" +
        "new GetAt()[0].startsWith('x')\n" +
        "GetAt g\n" +
        "g[0].startsWith('x')";

    private static final String CONTENTS_GETAT2 =
        "class GetAt {\n" +
        "}\n" +
        "\n" +
        "new GetAt()[0].startsWith('x')\n" +
        "GetAt g\n" +
        "g[0].startsWith('x')";

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
        String contents = "class A {}\n new A().getAt('')\n";
        int start = contents.lastIndexOf("getAt");
        int end = start + "getAt".length();
        assertType(contents, start, end, "java.lang.Object");
        assertDeclaringType(contents, start, end, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test
    public void testGetAt6() {
        String contents = "class A {\n A getAt(String property) { \n new A()\n}}\n new A().getAt('x')";
        int start = contents.lastIndexOf("getAt");
        int end = start + "getAt".length();
        assertType(contents, start, end, "A");
        assertDeclaringType(contents, start, end, "A");
    }

    @Test
    public void testGetAt7() {
        String contents = "class A {\n A getAt(String property) {\n new A()\n}}\n class B extends A {}\n new B().getAt('x')";
        int start = contents.lastIndexOf("getAt");
        int end = start + "getAt".length();
        assertType(contents, start, end, "A");
        assertDeclaringType(contents, start, end, "A");
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
        assertDeclaringType(contents, offset, offset + 4, jdkListSort ? "java.util.List" : "org.codehaus.groovy.runtime.DefaultGroovyMethods");
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
        MethodNode m = assertDeclaration(contents, offset, offset + "remove".length(), "java.util.List", "remove", DeclarationKind.METHOD);
        assertEquals("Should resolve to remove(int) due to return type of inner call", "int", printTypeName(m.getParameters()[0].getType()));
    }

    @Test // GROOVY-5136
    public void testCategoryMethod1() {
        String contents =
            "class Cat {\n" +
            "  static int f(String self, int x) {}\n" +
            "  static int g(String self, x) {}\n" +
            "}\n" +
            "use (Cat) {\n" +
            "  'x'.f()\n" + // no
            "  'x'.g()\n" + // yes
            "}\n";
        assertUnknown(contents, "f");
        assertType(contents, "g", "java.lang.Integer");
    }

    @Test // GROOVY-5245
    public void testCategoryMethod2() {
        String contents =
            "class Cat {\n" +
            "  static boolean getWorking(String self) {}\n" +
            "  static boolean isNotWorking(String self) {}\n" +
            "}\n" +
            "use (Cat) {\n" +
            "  'x'.working\n" +
            "  'x'.notWorking\n" +
            "}\n";
        assertType(contents, "working", "java.lang.Boolean");
        if (isAtLeastGroovy(40)) {
            assertType(contents, "notWorking", "java.lang.Boolean");
        } else {
            assertUnknown(contents, "notWorking");
        }
    }

    @Test // GROOVY-5245, GROOVY-10133
    public void testCategoryMethod3() {
        String contents =
            "class Cat {\n" +
            "  static boolean isAbc(self) {}\n" +
            "  static boolean getAbc(self) {}\n" +
            "}\n" +
            "use (Cat) {\n" +
            "  abc\n" +
            "}\n";
        int offset = contents.lastIndexOf("abc");
        assertDeclaration(contents, offset, offset + 3, "Cat", isAtLeastGroovy(40) ? "isAbc" : "getAbc", DeclarationKind.METHOD);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1545
    public void testCategoryMethod4() {
        String contents =
            "class Cat {\n" +
            "  static int getLength(Foo foo) {}\n" +
            "}\n" +
            "class Foo {\n" +
            "  int length() {}\n" +
            "}\n" +
            "use (Cat) {\n" +
            "  def foo = new Foo()\n" +
            "  foo.length()\n" +
            "  foo.length\n" +
            "}\n";
        int offset = contents.lastIndexOf("length()");
        assertDeclaration(contents, offset, offset + 6, "Foo", "length", DeclarationKind.METHOD);
        /**/offset = contents.lastIndexOf("length");
        assertDeclaration(contents, offset, offset + 6, "Cat", "getLength", DeclarationKind.METHOD);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1545
    public void testCategoryMethod5() {
        String contents =
            "class Cat {\n" +
            "  static int length(Foo foo) {}\n" +
            "}\n" +
            "class Foo {\n" +
            "  int getLength() {}\n" +
            "}\n" +
            "use (Cat) {\n" +
            "  def foo = new Foo()\n" +
            "  foo.length()\n" +
            "  foo.length\n" +
            "}\n";
        int offset = contents.lastIndexOf("length()");
        assertDeclaration(contents, offset, offset + 6, "Cat", "length", DeclarationKind.METHOD);
        /**/offset = contents.lastIndexOf("length");
        assertDeclaration(contents, offset, offset + 6, "Foo", "getLength", DeclarationKind.METHOD);
    }

    @Test // GROOVY-5609
    public void testVariadicCategoryMethods() {
        String contents =
            "class Cat {\n" +
            "  static <T> void foo(List<T> self, T[] tees) {\n" +
            "    print(self.size() + tees.length)\n" +
            "  }\n" +
            "  static <T> void foo(T[] self, T[] tees) {\n" +
            "    print(self.length + tees.length)\n" +
            "  }\n" +
            "}\n" +
            "use (Cat) {\n" +
            "  Integer[] array = [1,2,3]\n" + // line 10
            "  array.foo(array)\n" +
            "  array.foo(4,5,6)\n" +
            "  array.foo(4)\n" +
            "  array.foo()\n" +
            "  List<Integer> list = [1,2,3]\n" +
            "  list.foo(array)\n" +
            "  list.foo(4,5,6)\n" +
            "  list.foo(4)\n" +
            "  list.foo()\n" +
            "}\n";

        int offset = contents.indexOf("foo", contents.indexOf("use"));
        assertDeclaringType(contents, offset, offset + 3, "Cat"); // line 11

        offset = contents.indexOf("foo", offset + 3);
        assertDeclaringType(contents, offset, offset + 3, "Cat"); // line 12

        offset = contents.indexOf("foo", offset + 3);
        assertDeclaringType(contents, offset, offset + 3, "Cat"); // line 13

        offset = contents.indexOf("foo", offset + 3);
        assertDeclaringType(contents, offset, offset + 3, "Cat"); // line 14

        offset = contents.indexOf("foo", offset + 3);
        assertDeclaringType(contents, offset, offset + 3, "Cat"); // line 16

        offset = contents.indexOf("foo", offset + 3);
        assertDeclaringType(contents, offset, offset + 3, "Cat"); // line 17

        offset = contents.indexOf("foo", offset + 3);
        assertDeclaringType(contents, offset, offset + 3, "Cat"); // line 18

        offset = contents.indexOf("foo", offset + 3);
        assertDeclaringType(contents, offset, offset + 3, "Cat"); // line 19
    }

    @Test // GRECLIPSE-1013
    public void testCategoryMethodAsProperty() {
        String contents = "''.toURL().text";
        assertDeclaringType(contents, "text", "org.codehaus.groovy.runtime.ResourceGroovyMethods");
    }

    @Test
    public void testInterfaceMethodAsProperty1() {
        createUnit("foo", "Bar", "package foo; interface Bar {\n def getOne()\n}\n");
        createUnit("foo", "Baz", "package foo; interface Baz extends Bar {\n def getTwo()\n}\n");

        String contents = "def meth(foo.Baz b) {\n b.one + b.two\n}";

        int offset = contents.indexOf("one");
        assertDeclaringType(contents, offset, offset + 3, "foo.Bar");
        /**/offset = contents.indexOf("two");
        assertDeclaringType(contents, offset, offset + 3, "foo.Baz");
    }

    @Test
    public void testInterfaceMethodAsProperty2() {
        createUnit("foo", "Bar", "package foo; interface Bar {\n def getOne()\n}\n");
        createUnit("foo", "Baz", "package foo; abstract class Baz implements Bar {\n abstract def getTwo()\n}\n");

        String contents = "def meth(foo.Baz b) {\n b.one + b.two\n}";

        int offset = contents.indexOf("one");
        assertDeclaringType(contents, offset, offset + 3, "foo.Bar");
        /**/offset = contents.indexOf("two");
        assertDeclaringType(contents, offset, offset + 3, "foo.Baz");
    }

    @Test
    public void testInterfaceMethodAsProperty3() {
        createUnit("foo", "Bar", "package foo; interface Bar {\n def getOne()\n}\n");
        createUnit("foo", "Baz", "package foo; abstract class Baz implements Bar {\n abstract def getTwo()\n}\n");

        String contents = "abstract class C extends foo.Baz {}\ndef meth(C c) {\n c.one + c.two\n}\n";

        int offset = contents.indexOf("one");
        assertDeclaringType(contents, offset, offset + 3, "foo.Bar");
        /**/offset = contents.indexOf("two");
        assertDeclaringType(contents, offset, offset + 3, "foo.Baz");
    }

    @Test
    public void testInterfaceMethodAsProperty4() {
        createJavaUnit("foo", "Bar", "package foo; public interface Bar {\n default int getOne() {\n return 1;\n}\n}\n");
        createUnit("foo", "Baz", "package foo; abstract class Baz implements Bar {\n abstract def getTwo()\n}\n");

        String contents = "abstract class C extends foo.Baz {}\ndef meth(C c) {\n c.one + c.two\n}\n";

        int offset = contents.indexOf("one");
        assertDeclaringType(contents, offset, offset + 3, "foo.Bar");
        /**/offset = contents.indexOf("two");
        assertDeclaringType(contents, offset, offset + 3, "foo.Baz");
    }

    @Test // GROOVY-10592
    public void testInterfaceMethodAsProperty5() {
        createJavaUnit("foo", "Bar", "package foo; public interface Bar {\n static int getOne() {\n return 1;\n}\n}\n");
        createUnit("foo", "Baz", "package foo; abstract class Baz implements Bar {\n abstract def getTwo()\n}\n");

        String contents = "abstract class C extends foo.Baz {}\ndef meth(C c) {\n c.one + c.two\n}\n";

        if (isAtLeastGroovy(50)) {
            assertType(contents, "one", "java.lang.Integer");
            assertDeclaringType(contents, "one", "foo.Bar");
        } else {
            assertUnknown(contents, "one"); // unavailable
        }
        assertDeclaringType(contents, "two", "foo.Baz");
    }

    @Test
    public void testInterfaceMethodAsProperty6() {
        createUnit("foo", "Bar", "package foo; trait Bar {\n int getOne() {\n 1\n}\n}\n");
        createUnit("foo", "Baz", "package foo; abstract class Baz implements Bar {\n int getTwo() {\n 2\n}\n}\n");

        String contents = "class C extends foo.Baz {}\ndef meth(C c) {\n c.one + c.two\n}\n";

        int offset = contents.indexOf("one");
        assertDeclaringType(contents, offset, offset + 3, "foo.Bar");
        /**/offset = contents.indexOf("two");
        assertDeclaringType(contents, offset, offset + 3, "foo.Baz");
    }

    @Test
    public void testIndirectInterfaceMethod() {
        createUnit("foo", "Bar", "package foo; interface Bar {\n def getOne()\n}\n");
        createUnit("foo", "Baz", "package foo; abstract class Baz implements Bar {\n abstract def getTwo()\n}\n");

        String contents = "abstract class C extends foo.Baz {}\ndef meth(C c) {\n c.getOne() + c.getTwo()\n}\n";

        int offset = contents.indexOf("getOne");
        assertDeclaringType(contents, offset, offset + 6, "foo.Bar");
        /**/offset = contents.indexOf("getTwo");
        assertDeclaringType(contents, offset, offset + 6, "foo.Baz");
    }

    @Test
    public void testIndirectInterfaceConstant() {
        createUnit("I", "interface I {\n Number ONE = 1\n}\n");
        createUnit("A", "abstract class A implements I {\n Number TWO = 2\n}\n");

        String contents = "abstract class B extends A {}\nB b; b.ONE; b.TWO\n";

        int offset = contents.indexOf("ONE");
        assertDeclaringType(contents, offset, offset + 3, "I");
        /**/offset = contents.indexOf("TWO");
        assertDeclaringType(contents, offset, offset + 3, "A");
    }

    @Test
    public void testObjectMethodOnClassVar1() {
        String contents = "int m(Class c) {\n c.toString()\n}\n";

        assertDeclaringType(contents, "toString", "java.lang.Class");
    }

    @Test
    public void testObjectMethodOnClassVar2() {
        String contents = "int m(Class c) {\n c.hashCode()\n}\n";

        assertDeclaringType(contents, "hashCode", "java.lang.Object");
    }

    @Test
    public void testObjectMethodOnInterface() {
        // Object is not in explicit type hierarchy of List
        String contents = "def m(List list) {\n list.getClass()\n}\n";

        assertDeclaringType(contents, "getClass", "java.lang.Object");
    }

    @Test
    public void testObjectMethodOnInterfaceAsProperty() {
        // Object is not in explicit type hierarchy of List
        String contents = "def m(List list) {\n list.class\n}\n";

        assertDeclaringType(contents, "class", "java.lang.Object");
    }

    @Test
    public void testInvokeMethod() {
        String contents =
            "class C {\n" +
            "  def invokeMethod(String name, args) {\n" +
            "  }\n" +
            "  def method() {\n" +
            "  }\n" +
            "  void test() {\n" +
            "    this.method()\n" +
            "    this.missing()\n" +
            "  }\n" +
            "}\n";

        int offset = contents.lastIndexOf("method");
        assertDeclaration(contents, offset, offset + 6, "C", "method", DeclarationKind.METHOD);

        offset = contents.lastIndexOf("missing");
        assertDeclaration(contents, offset, offset + 7, "C", "invokeMethod", DeclarationKind.METHOD);
    }

    @Test
    public void testInvokeMethodAndMethodMissing() {
        String contents =
            "class C {\n" +
            "  def invokeMethod(String name, args) {\n" +
            "  }\n" +
            "  def methodMissing(String name, args) {\n" +
            "  }\n" +
            "  def method() {\n" +
            "  }\n" +
            "  void test() {\n" +
            "    this.method()\n" +
            "    this.missing()\n" +
            "  }\n" +
            "}\n";

        int offset = contents.lastIndexOf("method");
        assertDeclaration(contents, offset, offset + 6, "C", "method", DeclarationKind.METHOD);

        offset = contents.lastIndexOf("missing");
        assertDeclaration(contents, offset, offset + 7, "C", "methodMissing", DeclarationKind.METHOD);
    }

    @Test
    public void testInvokeMethodGroovyInterceptable() {
        String contents =
            "class C implements GroovyInterceptable {\n" +
            "  def invokeMethod(String name, args) {\n" +
            "  }\n" +
            "  def method() {\n" +
            "  }\n" +
            "  void test() {\n" +
            "    this.method()\n" +
            "    this.missing()\n" +
            "  }\n" +
            "}\n";

        int offset = contents.lastIndexOf("method");
        assertDeclaration(contents, offset, offset + 6, "C", "invokeMethod", DeclarationKind.METHOD);

        offset = contents.lastIndexOf("missing");
        assertDeclaration(contents, offset, offset + 7, "C", "invokeMethod", DeclarationKind.METHOD);
    }

    @Test
    public void testMethodMissing() {
        String contents =
            "class C {\n" +
            "  def methodMissing(String name, args) {\n" +
            "  }\n" +
            "  def method() {\n" +
            "  }\n" +
            "  void test() {\n" +
            "    this.method()\n" +
            "    this.missing()\n" +
            "  }\n" +
            "}\n";

        int offset = contents.lastIndexOf("method");
        assertDeclaration(contents, offset, offset + 6, "C", "method", DeclarationKind.METHOD);

        offset = contents.lastIndexOf("missing");
        assertDeclaration(contents, offset, offset + 7, "C", "methodMissing", DeclarationKind.METHOD);
    }

    @Test
    public void testStaticMethodMissing() {
        String contents =
            "class C {\n" +
            "  static $static_methodMissing(String name, args) {\n" +
            "  }\n" +
            "  static method() {\n" +
            "  }\n" +
            "  static test() {\n" +
            "    this.method()\n" +
            "    this.missing()\n" +
            "  }\n" +
            "}\n";

        int offset = contents.lastIndexOf("method");
        assertDeclaration(contents, offset, offset + 6, "C", "method", DeclarationKind.METHOD);

        offset = contents.lastIndexOf("missing");
        assertDeclaration(contents, offset, offset + 7, "C", "$static_methodMissing", DeclarationKind.METHOD);
    }

    @Test
    public void testPropertyMissing() {
        String contents =
            "class C {\n" +
            "  def propertyMissing(name) {\n" +
            "  }\n" +
            "  def proper\n" +
            "  void test() {\n" +
            "    this.proper\n" +
            "    this.missing\n" +
            "    this.getMissing()\n" +
            "  }\n" +
            "}\n";

        int offset = contents.lastIndexOf("proper");
        assertDeclaration(contents, offset, offset + 6, "C", "proper", DeclarationKind.PROPERTY);

        offset = contents.lastIndexOf("missing");
        assertDeclaration(contents, offset, offset + 7, "C", "missing", DeclarationKind.PROPERTY);

        offset = contents.lastIndexOf("getMissing");
        assertUnknownConfidence(contents, offset, offset + 10); // does not map to getProperty/propertyMissing
    }

    @Test
    public void testStaticPropertyMissing() {
        String contents =
            "class C {\n" +
            "  static $static_propertyMissing(name) {\n" +
            "  }\n" +
            "  static proper\n" +
            "  static test() {\n" +
            "    this.proper\n" +
            "    this.missing\n" +
            "    this.getMissing()\n" +
            "  }\n" +
            "}\n";

        int offset = contents.lastIndexOf("proper");
        assertDeclaration(contents, offset, offset + 6, "C", "proper", DeclarationKind.PROPERTY);

        offset = contents.lastIndexOf("missing");
        assertDeclaration(contents, offset, offset + 7, "C", "missing", DeclarationKind.PROPERTY);

        offset = contents.lastIndexOf("getMissing");
        assertUnknownConfidence(contents, offset, offset + 10); // does not map to $static_propertyMissing
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
        String contents = "@groovy.transform.Field List<Double> field\ndef (x, y) = field\nx\ny";
        assertType(contents, "x", "java.lang.Double");
        assertType(contents, "y", "java.lang.Double");
    }

    @Test
    public void testMultiDecl11() {
        String contents = "@groovy.transform.Field List<Double> field\ndef x\ndef y\n(x, y) = field\nx\ny";
        assertType(contents, "x", "java.lang.Double");
        assertType(contents, "y", "java.lang.Double");
    }

    @Test
    public void testMultiDecl12() {
        String contents = "def (int x, float y) = [1,2]\nx\ny";
        assertType(contents, "x", "java.lang.Integer");
        assertType(contents, "y", "java.lang.Float");
    }

    @Test
    public void testMultiDecl13() {
        String contents = "def (x, y) = Tuple.tuple((Number)1, 2d)";
        assertType(contents, "x", "java.lang.Number");
        assertType(contents, "y", "java.lang.Double");
    }

    @Test
    public void testMultiDecl14() {
        String contents = "def (x, y) = (Tuple3<Double,Number,String>)null";
        assertType(contents, "x", "java.lang.Double");
        assertType(contents, "y", "java.lang.Number");
    }

    @Test
    public void testMultiDecl15() {
        String contents = "int foo(){}\ndouble bar(){}\ndef (x, y) = [foo(), bar()]";
        assertType(contents, "x", "java.lang.Integer");
        assertType(contents, "y", "java.lang.Double");
    }

    @Test
    public void testMultiDecl16() {
        String contents = "int foo(){}\nList<Double> bar(){}\ndef (x, y) = [foo(), *bar()]";
        assertType(contents, "x", "java.lang.Integer");
        assertType(contents, "y", "java.lang.Double");
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

    @Test
    public void testInfix() {
        String contents =
            "def n = 1\n" +
            "def m = 2\n" +
            "def x = n ** m";
        assertType(contents, "x", "java.lang.Number");
    }

    @Test
    public void testPrefix() {
        String contents =
            "byte n = 1\n" +
            "def x = -(-n)";
        assertType(contents, "x", "java.lang.Byte");
    }

    @Test
    public void testPostfix() {
        String contents =
            "def item = 0\n" +
            "def list = [1]\n" +
            "def x = list[item]++";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testNested1() {
        assertType("((byte) 1 + (char) 2)", "java.lang.Integer");
    }

    @Test
    public void testNested2() {
        assertType("(((1 + 2) - 3) * 4)", "java.lang.Integer");
    }

    @Test
    public void testNested3() {
        assertType("(((1 + 2L) - 3) * 4)", "java.lang.Long");
    }

    @Test
    public void testNested4() {
        assertType("(((1 + 2f) - 3) * 4)", "java.lang.Double");
    }

    @Test
    public void testNested5() {
        assertType("(((1 + 2d) - 3) * 4)", "java.lang.Double");
    }

    @Test
    public void testNested6() {
        assertType("(((1 + 2g) - 3) * 4)", "java.math.BigInteger");
    }

    @Test
    public void testNested7() {
        assertType("(((1 + 2.0g) - 3) * 4)", "java.math.BigDecimal");
    }

    @Test
    public void testNested8() {
        assertType("(((1 + 2) - 3) / 4)", "java.math.BigDecimal");
    }

    @Test
    public void testNested9() {
        // float or double before BigDecimal or BigInteger
        assertType("(((1g + 2) - 3) / 4f)", "java.lang.Double");
    }

    @Test
    public void testNested10() {
        assertType("(true ? 2 : 7) + 9", "java.lang.Integer");
    }

    @Test
    public void testNested11() {
        assertType("(true ? 2 : 7) + (true ? 2 : 7)", "java.lang.Integer");
    }

    @Test
    public void testNested12() {
        assertType("(8 ?: 7) + (8 ?: 7)", "java.lang.Integer");
    }

    @Test
    public void testNested13() {
        createUnit("C", "class C {int n}");

        assertType("(new C().@n) + (8 ?: 7)", "java.lang.Integer");
    }

    @Test
    public void testNested14() {
        createUnit("C", "class C {double n}");

        assertType("(new C().@n) + (8 ?: 7)", "java.lang.Double");
    }

    @Test
    public void testNested15() {
        createUnit("C", "class C {String s}");

        assertType("(new C().s.length() + 2) / 3", "java.math.BigDecimal");
    }

    @Test
    public void testNested16() {
        createUnit("C", "class C {String s}");

        assertType("([ new C() ])[(new C().s.length() + 4 - 9) / 7]", "C");
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
            "  def meth() {\n" +
            "    use (MeCat) {\n" +
            "      println getVal()\n" +
            "      println val\n" +
            "    }\n" +
            "  }\n" +
            "} \n" +
            "\n" +
            "class MeCat {\n" +
            "  static String getVal(Me self) {\n" +
            "    'val'\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "use (MeCat) {\n" +
            "  println new Me().getVal()\n" +
            "  println new Me().val\n" +
            "}\n" +
            "new Me().meth()");
    }

    @Test
    public void testNonPublicCategoryMethod() {
        String contents =
            "class Cat { \n" +
            "  protected static void nope(self) {\n" +
            "  }\n" +
            "}\n" +
            "use (Cat) {\n" +
            "  this.nope()\n" +
            "}\n";
        assertUnknown(contents, "nope");
    }

    @Test
    public void testNonStaticCategoryMethod() {
        String contents =
            "class Cat { \n" +
            "  public void nope(self) {\n" +
            "  }\n" +
            "}\n" +
            "use (Cat) {\n" +
            "  this.nope()\n" +
            "}\n";
        assertUnknown(contents, "nope");
    }

    @Test
    public void testNonCategoryUseMethod() {
        String contents =
            "class C {\n" +
            "  void use(... args) {\n" +
            "    // ...\n" +
            "  }\n" +
            "}\n" +
            "void test(C c) {\n" +
            "  def x = 42\n" +
            "  c.use(x) {\n" +
            "    // ...\n" +
            "  }\n" +
            "}\n";
        //assertDeclaringType(contents, "use", "C");
        assertType(contents, "x", "java.lang.Integer");
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
            "    x.substring(0)\n" +
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
    public void testInstanceOf2() {
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
    public void testInstanceOf3() {
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
    public void testInstanceOf4() {
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
    public void testInstanceOf5() {
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
    public void testInstanceOf6() {
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
    public void testInstanceOf7() {
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
    public void testInstanceOf8() {
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
    public void testInstanceOf9() {
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
    public void testInstanceOf10() {
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
    public void testInstanceOf11() {
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
    public void testInstanceOf12() {
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
    public void testInstanceOf13() {
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
    public void testInstanceOf14() {
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
    public void testInstanceOf15() {
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
    public void testInstanceOf16() {
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
    public void testInstanceOf17() {
        String contents = // dynamic variable
            "if (xxx instanceof List) xxx.size()\n";

        int offset = contents.indexOf("xxx");
        assertType(contents, offset, offset + 3, "java.lang.Object");

        offset = contents.lastIndexOf("xxx");
        assertType(contents, offset, offset + 3, "java.lang.Object");

        //

        contents = // dynamic property
            "if (x.y instanceof List) x.y.size()\n";

        offset = contents.indexOf("y");
        assertType(contents, offset, offset + 1, "java.lang.Object");

        offset = contents.lastIndexOf("y");
        assertType(contents, offset, offset + 1, "java.lang.Object");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/977
    public void testInstanceOf18() {
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

    @Test // https://github.com/groovy/groovy-eclipse/issues/1101
    public void testInstanceOf19() {
        String contents =
            "class C {\n" +
            "  private Number one, two\n" +
            "  boolean equals(Object that) {\n" +
            "    that instanceof C && that.one == this.one && that.two == this.two\n" +
            "  }\n" +
            "}\n";

        int offset = contents.indexOf("that instanceof");
        assertType(contents, offset, offset + 4, "java.lang.Object");

        offset = contents.indexOf("that.one");
        assertType(contents, offset, offset + 4, "C");
        assertType(contents, offset + 5, offset + 8, "java.lang.Number");

        offset = contents.indexOf("that.two");
        assertType(contents, offset, offset + 4, "C");
        assertType(contents, offset + 5, offset + 8, "java.lang.Number");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1122
    public void testInstanceOf20() {
        String contents =
            "void test(flag, x) {\n" +
            "  if (flag && x instanceof java.util.regex.Matcher) {\n" +
            "    x.find()\n" +
            "  }\n" +
            "  x\n" +
            "}\n";

        int offset = contents.indexOf("x.find");
        assertType(contents, offset, offset + 1, "java.util.regex.Matcher");

        offset = contents.lastIndexOf("x");
        assertType(contents, offset, offset + 1, "java.lang.Object");
    }

    @Test
    public void testInstanceOf21() {
        String contents =
            "@groovy.transform.TypeChecked\n" +
            "void test(value) {\n" +
            "  if (value instanceof Number || value instanceof String || value instanceof Map) {\n" +
            "    value\n" +
            "  }\n" +
            "}\n";

        assertType(contents, "value", "java.lang.Object");
    }

    @Test // GROOVY-7971
    public void testInstanceOf22() {
        String contents =
            "@groovy.transform.TypeChecked\n" +
            "void test(value) {\n" +
            "  def isString = (value.class == String);\n" +
            "  if (isString || value instanceof Map) {\n" +
            "    value\n" +
            "  }\n" +
            "}\n";

        assertType(contents, "value", "java.lang.Object");
    }

    @Test // GROOVY-9769
    public void testInstanceOf23() {
        String contents =
            "interface A {}\n" +
            "interface B extends A {\n" +
            "  def foo()\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(A a) {\n" +
            "  if (a instanceof B) {\n" +
            "    a.foo()\n" +
            "  }\n" +
            "}\n";

        assertType(contents, "a", "B"); // not <UnionType:A+B>
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1534
    public void testInstanceOf24() {
        String contents =
            "void test(one, two) {\n" +
            "  if (one instanceof Cloneable && two instanceof Closeable) {\n" +
            "    print(one)\n" +
            "    print(two)\n" +
            "  }\n" +
            "}\n";

        assertType(contents, "one", "java.lang.Cloneable");
        assertType(contents, "two",   "java.io.Closeable");
        assertType("@groovy.transform.TypeChecked " + contents, "one", "java.lang.Cloneable");
        assertType("@groovy.transform.TypeChecked " + contents, "two",   "java.io.Closeable");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1534
    public void testInstanceOf25() {
        //@formatter:off
        String[][] spec = {
            {"Object"      , "Object"                          , "java.lang.Object"                                                , },
            {"String"      , "String"                          , "java.lang.String"                                                , },
            {"Object"      , "Object[]"                        , "java.lang.Object[]"                                              , },
            {"Object[]"    , "Number[]"                        , "java.lang.Number[]"                                              , },
            {"Object[]"    , "Object[][]"                      , "java.lang.Object[][]"                                            , },
            {"CharSequence", "String"                          , "java.lang.String"                                                , },
            {"CharSequence", "Cloneable"                       , "java.lang.CharSequence & java.lang.Cloneable"                    , },
            {"CharSequence", "Cloneable,Closeable"             , "java.lang.CharSequence & java.lang.Cloneable & java.io.Closeable", },
            {"Object"      , "CharSequence,Cloneable"          , "java.lang.CharSequence & java.lang.Cloneable"                    , },
            {"Object"      , "CharSequence,Cloneable,Closeable", "java.lang.CharSequence & java.lang.Cloneable & java.io.Closeable", },
            {"Number"      , "BigInteger,Cloneable"            , "java.math.BigInteger & java.lang.Cloneable"                      , },
            {"Cloneable"   , "Number"                          , "java.lang.Number & java.lang.Cloneable"                          , },
            {"Cloneable"   , "Number,Short"                    , "java.lang.Short & java.lang.Cloneable"                           , },
            {"Comparable"  , "Number,Short"                    , "java.lang.Short"                                                 , },
            {"Object"      , "Comparable,Short"                , "java.lang.Short"                                                 , },
            {"Object"      , "Comparable,Number,Short"         , "java.lang.Short"                                                 , },
            {"Object"      , "Float,Short"                     , "java.lang.Float"                                                 , "java.lang.Float & java.lang.Short"                      },
            {"Cloneable"   , "Float,Short"                     , "java.lang.Float & java.lang.Cloneable"                           , "java.lang.Float & java.lang.Short & java.lang.Cloneable"},
            {"List<java.lang.String>", "Iterable,Collection,List", "java.util.List<java.lang.String>"                              , },
        };
        //@formatter:on

        for (String[] test : spec) {
            String[] types = test[1].split(",");

            var contents = new StringBuilder("void test(").append(test[0]).append(" object) {\n ");
            for (String type : types) {
                contents.append(" if (object instanceof ").append(type).append(")");
            }
            contents.append(" object\n");
            contents.append("}\n");

            assertType(contents.toString(), "object", test[2]);
            assertType("@groovy.transform.TypeChecked " + contents, "object", test[test.length - 1]);

            //

            if (types.length > 1) { // try "if (object instanceof Type0 && object instanceof Type1)"
                contents = new StringBuilder("void test(").append(test[0]).append(" object) {\n ");
                contents.append(" if (object instanceof ").append(types[0]);
                for (int i = 1; i < types.length; i += 1) {
                    contents.append(" && object instanceof ").append(types[i]);
                }
                contents.append(") object\n");
                contents.append("}\n");

                assertType(contents.toString(), "object", test[2]);
                assertType("@groovy.transform.TypeChecked " + contents, "object", test[test.length - 1]);
            }

            //

            contents = new StringBuilder("void test(").append(test[0]).append(" object) {\n ");
            contents.append(" if (!(object instanceof ").append(types[0]).append(")");
            for (int i = 1; i < types.length; i += 1) {
                contents.append(" && !(object instanceof ").append(types[i]).append(")");
            }
            contents.append(") object; else object\n");
            contents.append("}\n");

            int offset = contents.indexOf("object;");
            var expect = test[0].startsWith("List") ? "java.util." + test[0] : "java.lang." + test[0];
            assertType(contents.toString(), offset, offset + 6, expect);
            assertType("@groovy.transform.TypeChecked " + contents, offset + 30, offset + 36, expect);
            offset = contents.lastIndexOf("object");
            assertType(contents.toString(), offset, offset + 6, test[2]);
            if (isAtLeastGroovy(50))
                assertType("@groovy.transform.TypeChecked " + contents, offset + 30, offset + 36, test[test.length - 1]);

            //

            if (isParrotParser()) {
                contents = new StringBuilder("void test(").append(test[0]).append(" object) {\n ");
                contents.append(" if (object !instanceof ").append(types[0]);
                for (int i = 1; i < types.length; i += 1) {
                    contents.append(" && object !instanceof ").append(types[i]);
                }
                contents.append(") object; else object\n");
                contents.append("}\n");

                offset = contents.indexOf("object;");
                expect = test[0].startsWith("List") ? "java.util." + test[0] : "java.lang." + test[0];
                assertType(contents.toString(), offset, offset + 6, expect);
                assertType("@groovy.transform.TypeChecked " + contents, offset + 30, offset + 36, expect);
                offset = contents.lastIndexOf("object");
                assertType(contents.toString(), offset, offset + 6, test[2]);
                if (isAtLeastGroovy(50))
                    assertType("@groovy.transform.TypeChecked " + contents, offset + 30, offset + 36, test[test.length - 1]);
            }
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1101
    public void testEqualsClassTest1() {
        String contents =
            "class C {\n" +
            "  def foo, bar, baz\n" +
            "  boolean equals(Object that) {\n" +
            "    return that.class == C &&\n" +
            "      that.foo == this.foo &&\n" +
            "      that.bar == this.bar &&\n" +
            "      that.baz == this.baz\n" +
            "  }\n" +
            "}\n";

        int offset = contents.indexOf("that.class");
        assertType(contents, offset, offset + 4, "java.lang.Object");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1101
    public void testEqualsClassTest2() {
        String contents =
            "class C {\n" +
            "  def foo, bar, baz\n" +
            "  boolean equals(Object that) {\n" +
            "    return that.getClass() == C &&\n" +
            "      that.foo == this.foo &&\n" +
            "      that.bar == this.bar &&\n" +
            "      that.baz == this.baz\n" +
            "  }\n" +
            "}\n";

        int offset = contents.indexOf("that.getClass()");
        assertType(contents, offset, offset + 4, "java.lang.Object");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");
    }

    @Test
    public void testEqualsClassTest3() {
        String contents =
            "class C {\n" +
            "  def foo, bar, baz\n" +
            "  boolean equals(Object that) {\n" +
            "    return C == that.class &&\n" +
            "      that.foo == this.foo &&\n" +
            "      that.bar == this.bar &&\n" +
            "      that.baz == this.baz\n" +
            "  }\n" +
            "}\n";

        int offset = contents.indexOf("that.class");
        assertType(contents, offset, offset + 4, "java.lang.Object");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");
    }

    @Test
    public void testEqualsClassTest4() {
        String contents =
            "class C {\n" +
            "  def foo, bar, baz\n" +
            "  boolean equals(Object that) {\n" +
            "    return C == that.getClass() &&\n" +
            "      that.foo == this.foo &&\n" +
            "      that.bar == this.bar &&\n" +
            "      that.baz == this.baz\n" +
            "  }\n" +
            "}\n";

        int offset = contents.indexOf("that.getClass()");
        assertType(contents, offset, offset + 4, "java.lang.Object");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");
    }

    @Test
    public void testEqualsClassTest5() {
        assumeTrue(isParrotParser());

        String contents =
            "class C {\n" +
            "  def foo, bar, baz\n" +
            "  boolean equals(Object that) {\n" +
            "    return that.class === C &&\n" +
            "      that.foo == this.foo &&\n" +
            "      that.bar == this.bar &&\n" +
            "      that.baz == this.baz\n" +
            "  }\n" +
            "}\n";

        int offset = contents.indexOf("that.class");
        assertType(contents, offset, offset + 4, "java.lang.Object");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");
    }

    @Test
    public void testEqualsClassTest6() {
        String contents =
            "class C {\n" +
            "  def foo, bar, baz\n" +
            "  boolean equals(Object that) {\n" +
            "    return that in C &&\n" +
            "      that.foo == this.foo &&\n" +
            "      that.bar == this.bar &&\n" +
            "      that.baz == this.baz\n" +
            "  }\n" +
            "}\n";

        int offset = contents.indexOf("that in ");
        assertType(contents, offset, offset + 4, "java.lang.Object");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");

        offset = contents.indexOf("that", offset + 4);
        assertType(contents, offset, offset + 4, "C");
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

    @Test // https://issues.apache.org/jira/browse/GROOVY-9854
    public void testSwitchClosureCase1() {
        String contents =
            "switch (123) {\n" +
            "  case {it > 10}:\n" +
            "  break\n" +
            "}\n";

        assertType(contents, "it", "java.lang.Integer");
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

    @Test // https://github.com/groovy/groovy-eclipse/issues/1090
    public void testMethodOverloadsArgumentMatching11() {
        String contents =
            "class C {\n" +
            "  C(String s, Map m) {\n" +
            "  }\n" +
            "  C(String s, ... v) {\n" +
            "  }\n" +
            "}\n" +
            "new C('')\n";

        int offset = contents.indexOf("new");
        MethodNode m = assertDeclaration(contents, offset, offset + 9, "C", "<init>", DeclarationKind.METHOD);
        assertTrue("Expected array, but was " + m.getParameters()[1].getType().getNameWithoutPackage(), m.getParameters()[1].getType().isArray());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1111
    public void testMethodOverloadsArgumentMatching12() {
        String contents = "def array = ['x'].stream().toArray(String)\n";

        assertType(contents, "array", "java.lang.String[]");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1121
    public void testMethodOverloadsArgumentMatching13() {
        String contents =
            "import org.codehaus.groovy.ast.ClassNode\n" +
            "import static java.lang.reflect.Modifier.*\n" +
            "import static org.codehaus.groovy.ast.ClassHelper.make\n" +
            "import static org.codehaus.groovy.ast.tools.GeneralUtils.*\n" +

            "void test(ClassNode node, String prefix) {\n" +
            "  def field = node.addField(prefix + 'suffix',\n" +
            "    FINAL | PRIVATE,\n" +
            "    make(Date),\n" +
            "    ctorX(make(Date))\n" +
            "  )\n" +
            "}\n";

        assertDeclaringType(contents, "ctorX", "org.codehaus.groovy.ast.tools.GeneralUtils");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1160
    public void testMethodOverloadsArgumentMatching14() {
        createJavaUnit("Face",
            "interface Face {\n" +
            "  float m(Object obj) {}\n" +
            "  double m(Object[] arr) {}\n" +
            "  <T extends Number> Number m(T num) {}\n" +
            "}\n");

        assertType("void test(Face face){face.m()}", "m", "java.lang.Double");
        assertType("void test(Face face){face.m(null)}", "m", "java.lang.Float");
        assertType("void test(Face face){face.m(1234)}", "m", "java.lang.Number");
        assertType("void test(Face face){face.m(1234, 5678)}", "m", "java.lang.Double");
        assertType("void test(Face face){face.m((Face) null)}", "m", "java.lang.Float");
    }

    @Test
    public void testMethodOverloadsArgumentMatching14a() {
        createJavaUnit("Face",
            "interface Face {\n" +
            "  float m(Object obj) {}\n" +
            "  double m(Object[] arr) {}\n" +
            "  <T extends Face> Object m(T imp) {}\n" +
            "}\n");

        assertType("void test(Face face){face.m()}", "m", "java.lang.Double");
        assertType("void test(Face face){face.m(null)}", "m", "java.lang.Float");
        assertType("void test(Face face){face.m(1234)}", "m", "java.lang.Float");
        assertType("void test(Face face){face.m(1234, 5678)}", "m", "java.lang.Double");
        assertType("void test(Face face){face.m((Face) null)}", "m", "java.lang.Object");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1220
    public void testMethodOverloadsArgumentMatching15() {
        createJavaUnit("Face",
            "interface Face {\n" +
            "  float m(int i) ;\n" +
            "  default double m(long l) {}\n" +
            "}\n");

        assertType("void test(Face face){face.m(123)}", "m", "java.lang.Float");
        assertType("void test(Face face){face.m(45L)}", "m", "java.lang.Double");
        assertType("class Impl implements Face {\n float m(int i) {}\n}\n" +
            "void test(Impl impl){impl.m(45L)}", "m", "java.lang.Double");
    }

    @Test
    public void testMethodOverloadsArgumentMatching15a() {
        createJavaUnit("Face",
            "interface Face {\n" +
            "  float m(int i) ;\n" +
            "  default double m(long l) {}\n" +
            "}\n");

        assertType("interface Next extends Face {\n float m(int i)\n}\n" +
            "void test(Next next){next.m(45L)}", "m", "java.lang.Double");
    }

    @Test
    public void testMethodOverloadsArgumentMatching16() {
        String contents =
            "class C {\n" +
            "  protected Number x\n" +
            "  String getX() {'x'}\n" +
            "  byte m(Number n) {}\n" +
            "  long m(String s) {}\n" +
            "}\n" +

            "void test(C c) {\n" +
            "  def value = c.m(c.@x)\n" +
            "}\n";

        assertType(contents, "value", "java.lang.Byte");
    }
}
