/*
 * Copyright 2009-2021 the original author or authors.
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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public final class StaticInferencingTests extends InferencingTestSuite {

    private ASTNode assertKnown(String source, String target, String declaringType, String expressionType) {
        int offset = source.lastIndexOf(target);
        GroovyCompilationUnit unit = createUnit(DEFAULT_UNIT_NAME, source);
        SearchRequestor requestor = doVisit(offset, offset + target.length(), unit);

        Assert.assertNotEquals("Expected token '" + target + "' at offset " + offset + " to be recognized",
            TypeLookupResult.TypeConfidence.UNKNOWN, requestor.result.confidence);
        Assert.assertEquals(declaringType, requestor.result.declaringType.getName());
        Assert.assertEquals(expressionType, printTypeName(requestor.result.type));

        return requestor.result.declaration;
    }

    private void assertUnknown(String source, String target) {
        int offset = source.lastIndexOf(target);
        assertUnknownConfidence(source, offset, offset + target.length());
    }

    //--------------------------------------------------------------------------

    @Test
    public void testClassReference1() {
        String contents = "String";
        assertType(contents, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference2() {
        String contents = "String.class";
        assertType(contents, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference3() {
        String contents = "String.";
        assertType(contents, 0, 6, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference4() {
        String contents = "String.dyn";
        assertType(contents, 0, 6, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference5() {
        String contents = "String?.dyn";
        assertType(contents, 0, 6, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference6() {
        String contents = "String.@dyn";
        assertType(contents, 0, 6, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference7() {
        String contents = "String.&dyn";
        assertType(contents, 0, 6, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference8() {
        String contents = "String.class.dyn";
        assertType(contents, 0, 12, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference9() {
        String contents = "String.class.@dyn";
        assertType(contents, 0, 12, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference10() {
        String contents = "String.class.&dyn";
        assertType(contents, 0, 12, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference11() {
        String contents = "String.getClass()"; // same as "(String.class).getClass()"
        assertKnown(contents, "getClass", "java.lang.Object", "java.lang.Class<?>");
    }

    @Test
    public void testClassReference12() {
        String contents = "String.class.getCanonicalName()";
        assertKnown(contents, "getCanonicalName", "java.lang.Class", "java.lang.String");
    }

    @Test
    public void testClassReference13() {
        String contents = "String.getCanonicalName()";
        assertKnown(contents, "getCanonicalName", "java.lang.Class", "java.lang.String");
    }

    @Test
    public void testClassReference14() {
        String contents = "String.class.canonicalName";
        assertKnown(contents, "canonicalName", "java.lang.Class", "java.lang.String");
    }

    @Test
    public void testClassReference15() {
        String contents = "String.canonicalName";
        assertKnown(contents, "canonicalName", "java.lang.Class", "java.lang.String");
    }

    @Test // Class members may be accessed directly from static initializers
    public void testClassReference16() {
        String contents = "class C { static { getCanonicalName();}}";
        assertKnown(contents, "getCanonicalName", "java.lang.Class", "java.lang.String");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/538
    public void testClassReference17() {
        String contents = "String.getMethod('toLowerCase')";
        assertKnown(contents, "getMethod", "java.lang.Class", "java.lang.reflect.Method");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/538
    public void testClassReference18() {
        String contents = "String.class.getMethod('toLowerCase')";
        assertKnown(contents, "getMethod", "java.lang.Class", "java.lang.reflect.Method");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/538
    public void testClassReference19() {
        String contents = "String.getConstructor()";
        assertKnown(contents, "getConstructor", "java.lang.Class", "java.lang.reflect.Constructor<java.lang.String>");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/538
    public void testClassReference20() {
        String contents = "String.class.getConstructor()";
        assertKnown(contents, "getConstructor", "java.lang.Class", "java.lang.reflect.Constructor<java.lang.String>");
    }

    @Test
    public void testClassReference22() {
        String contents = "String.getPackage()";
        assertType(contents, "getPackage", "java.lang.Package");
        assertDeclaringType(contents, "getPackage", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference23() {
        String contents = "String.package";
        assertType(contents, "package", "java.lang.Package");
        assertDeclaringType(contents, "package", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference24() {
        String contents = "String.class.getPackage()";
        assertType(contents, "getPackage", "java.lang.Package");
        assertDeclaringType(contents, "getPackage", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference25() {
        String contents = "String.class.package";
        assertType(contents, "package", "java.lang.Package");
        assertDeclaringType(contents, "package", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference26() {
        String contents = "def clazz = String; clazz.getPackage()";
        assertType(contents, "getPackage", "java.lang.Package");
        assertDeclaringType(contents, "getPackage", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference27() {
        String contents = "def clazz = String; clazz.package";
        assertType(contents, "package", "java.lang.Package");
        assertDeclaringType(contents, "package", "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference28() {
        String contents = "Class clazz = String; clazz.package";
        assertType(contents, "package", "java.lang.Package");
        assertDeclaringType(contents, "package", "java.lang.Class");
    }

    //

    @Test
    public void testStaticReference1() {
        String contents =
            "class C {\n" +
            "  static int length\n" +
            "}\n" +
            "C.length\n";
        ASTNode decl = assertKnown(contents, "length", "C", "java.lang.Integer");
        Assert.assertTrue(decl instanceof PropertyNode);
    }

    @Test
    public void testStaticReference2() {
        String contents =
            "class C {\n" +
            "  static int length\n" +
            "  static int getLength() { length;}\n" +
            "}\n" +
            "C.length\n";
        ASTNode decl = assertKnown(contents, "length", "C", "java.lang.Integer");
        Assert.assertTrue(decl instanceof MethodNode); // not FieldNode
    }

    @Test
    public void testStaticReference3() {
        String contents =
            "class C {\n" +
            "  static int length\n" +
            "  static int getLength() { length;}\n" +
            "}\n" +
            "new C().length\n";
        ASTNode decl = assertKnown(contents, "length", "C", "java.lang.Integer");
        Assert.assertTrue(decl instanceof MethodNode); // not FieldNode
    }

    @Test // GRECLIPSE-1544
    public void testStaticReference4() {
        String contents =
            "package p\n" +
            "class C {\n" +
            "  static C getInstance() {}\n" +
            "  void test() { C.getInstance();}\n" +
            "}\n";
        assertKnown(contents, "getInstance", "p.C", "p.C");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/525
    public void testStaticReference5() {
        // name clashes with methods available from java.lang.Class and org.codehaus.groovy.runtime.DefaultGroovyMethods
        String contents = "String[] arr; String str = Arrays.toString(arr)";
        assertKnown(contents, "toString", "java.util.Arrays", "java.lang.String");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/525
    public void testStaticReference6() {
        // name clashes with methods available from java.lang.Class and org.codehaus.groovy.runtime.DefaultGroovyMethods
        String contents = "Arrays.&toString";
        assertKnown(contents, "toString", "java.util.Arrays", "java.lang.String");
    }

    @Test
    public void testStaticReference7() {
        String contents = "Arrays.&binarySearch";
        assertKnown(contents, "binarySearch", "java.util.Arrays", "java.lang.Integer");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/595
    public void testStaticReference8() {
        String contents =
            "class C {\n" +
            "  static boolean equals(char[] a, char[] b) {\n" +
            "  }\n" +
            "  static void meth(char[] x, char[] y) {\n" +
            "    if (C.equals(x, y)) {\n" + // name clashes with method available from java.lang.Object
            "    }\n" +
            "  }\n" +
            "}\n";
        assertKnown(contents, "equals", "C", "java.lang.Boolean");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/646
    public void testStaticReference9() {
        String contents =
            "class Unit {}\n" +
            "class Assert {\n" +
            "  protected void assertType(String src, String expected) {}\n" +
            "  protected void assertType(String src, int off, int len, String expected) {}\n" +
            "  public static void assertType(Unit src, int off, int len, String expected) {}\n" +
            "}\n" +
            "Unit unit = null; int offset = 0\n" +
            "Assert.assertType(unit, offset, offset + 'string'.length(), 'java.util.Collection')\n";

        assertKnown(contents, "assertType", "Assert", "java.lang.Void");
    }

    @Test
    public void testStaticReference10() {
        String contents =
            "class C {\n" +
            "  static def foo\n" +
            "  static {\n" +
            "    foo\n" +
            "  }\n" +
            "}\n";
        ASTNode decl = assertKnown(contents, "foo", "C", "java.lang.Object");
        Assert.assertTrue(decl instanceof FieldNode); // not MethodNode
    }

    @Test
    public void testStaticReference11() {
        String contents =
            "class C {\n" +
            "  static def foo\n" +
            "  static def method() {\n" +
            "    foo\n" +
            "  }\n" +
            "}\n";
        ASTNode decl = assertKnown(contents, "foo", "C", "java.lang.Object");
        Assert.assertTrue(decl instanceof FieldNode); // not MethodNode
    }

    @Test
    public void testStaticReference12() {
        String contents =
            "class C {\n" +
            "  static def foo\n" +
            "  static def getFoo() {}\n" +
            "  static {\n" +
            "    foo\n" +
            "  }\n" +
            "}\n";
        ASTNode decl = assertKnown(contents, "foo", "C", "java.lang.Object");
        Assert.assertTrue(decl instanceof FieldNode); // not MethodNode
    }

    @Test
    public void testStaticReference13() {
        String contents =
            "class C {\n" +
            "  static def foo\n" +
            "  static def getFoo() {}\n" +
            "  static def method() {\n" +
            "    foo\n" +
            "  }\n" +
            "}\n";
        ASTNode decl = assertKnown(contents, "foo", "C", "java.lang.Object");
        Assert.assertTrue(decl instanceof FieldNode); // not MethodNode
    }

    @Test
    public void testStaticReference14() {
        String contents =
            "class C {\n" +
            "  static Number x() {\n" +
            "    42\n" +
            "  }\n" +
            "}\n" +
            "C.x()\n";
        assertType(contents, "x", "java.lang.Number");
    }

    @Test
    public void testStaticReference15() {
        String contents =
            "class C {\n" +
            "  static Number x() {\n" +
            "    42\n" +
            "  }\n" +
            "}\n" +
            "C.x\n";
        assertUnknown(contents, "x");
    }

    @Test
    public void testStaticReference16() {
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
    public void testStaticReference17() {
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
    public void testStaticReference18() {
        String contents =
            "class Parent {\n" +
            "  static p() {}\n" +
            "}\n" +
            "class Child extends Parent {\n" +
            "  def c() {\n" +
            "    p()\n" +
            "  }\n" +
            "}\n";
        assertDeclaringType(contents, "p()", "Parent");
    }

    @Test // GRECLISPE-1244
    public void testStaticReference19() {
        createUnit("Parent",
            "class Parent {\n" +
            "  static p() {}\n" +
            "}\n");
        String contents =
            "class Child extends Parent {\n" +
            "  def c() {\n" +
            "    p()\n" +
            "  }\n" +
            "}\n";
        assertDeclaringType(contents, "p()", "Parent");
    }

    @Test
    public void testStaticReference20() {
        createUnit("foo", "Bar", "package foo\n" +
            "import java.util.regex.*\n" +
            "class Bar {\n" +
            "  static Object meth(Object o) { return o;}\n" +
            "  static Pattern meth(Pattern p) { return p;}\n" +
            "  static Collection meth(Collection c) { return c;}\n" +
            "}\n");

        String contents = "import static foo.Bar.*\nmeth([])";
        assertType(contents, "meth([])", "java.util.Collection");
    }

    @Test
    public void testStaticReference21() {
        createUnit("foo", "Bar", "package foo\n" +
            "import java.util.regex.*\n" +
            "class Bar {\n" +
            "  static Object meth(Object o) { return o;}\n" +
            "  static Pattern meth(Pattern p) { return p;}\n" +
            "  static Collection meth(Collection c) { return c;}\n" +
            "}\n");

        String contents = "import static foo.Bar.*\nmeth(~/abc/)";
        assertType(contents, "meth(~/abc/)", "java.util.regex.Pattern");
    }

    @Test
    public void testStaticReference22() {
        createUnit("foo", "Bar", "package foo\n" +
            "import java.util.regex.*\n" +
            "class Bar {\n" +
            "  static Object meth(Object o) { return o;}\n" +
            "  static Pattern meth(Pattern p) { return p;}\n" +
            "  static Collection meth(Collection c) { return c;}\n" +
            "}\n");

        String contents =
            "import static foo.Bar.*\n" +
            "import static java.util.regex.Pattern.*\n" +
            "meth(compile('abc'))\n";
        assertType(contents, "meth(compile('abc'))", "java.util.regex.Pattern");
    }

    @Test
    public void testStaticReference23() {
        createUnit("foo", "Bar", "package foo\n" +
            "import java.util.regex.*\n" +
            "abstract class Bar {\n" +
            "  static Object meth(Object o) { return o;}\n" +
            "  static Pattern meth(Pattern p) { return p;}\n" +
            "  static Collection meth(Collection c) { return c;}\n" +
            "}\n");

        String contents =
            "import static foo.Bar.*\n" +
            "import static java.util.regex.Pattern.*\n" +
            "meth(compile('abc'))\n";
        assertType(contents, "meth", "java.util.regex.Pattern");
    }

    @Test
    public void testStaticReference24() {
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
            "}\n";
        assertType(contents, "meth", "java.util.regex.Pattern");
    }

    @Test
    public void testStaticReference25() {
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
            "}\n";
        assertType(contents, "meth", "java.util.regex.Pattern");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1257
    public void testStaticReference26() {
        String contents =
            "class C {\n" +
            "  static name\n" +
            "}\n" +
            "C.name\n"; // not Class#getName()
        assertKnown(contents, "name", "C", "java.lang.Object");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1257
    public void testStaticReference27() {
        String contents =
            "class C {\n" +
            "  static getName() {}\n" +
            "}\n" +
            "C.name\n"; // not Class#getName()
        assertKnown(contents, "name", "C", "java.lang.Object");
    }

    @Test
    public void testStaticReference28() {
        String contents =
            "static getStaticProperty() {}\n" +
            "static staticMethod() {\n" +
            "  getStaticProperty()\n" +
            "  staticProperty\n" +
            "}\n";
        assertKnown(contents, "getStaticProperty", "Search", "java.lang.Object");
        assertKnown(contents, "staticProperty", "Search", "java.lang.Object");
    }

    @Test
    public void testStaticReference29() {
        String contents =
            "static getStaticProperty() {}\n" +
            "def scriptMethod() {\n" +
            "  getStaticProperty()\n" +
            "  staticProperty\n" +
            "}\n";
        assertKnown(contents, "getStaticProperty", "Search", "java.lang.Object");
        assertKnown(contents, "staticProperty", "Search", "java.lang.Object");
    }

    //

    @Test // GRECLIPSE-855: should be able to find the type, but with unknown confidence
    public void testNonStaticReference1() {
        String contents = "String.length()";
        assertUnknown(contents, "length");
    }

    @Test
    public void testNonStaticReference2() {
        String contents = "String.length";
        assertUnknown(contents, "length");
    }

    @Test
    public void testNonStaticReference3() {
        String contents = "class C { int length;}\nGGG.length";
        assertUnknown(contents, "length");
    }

    @Test
    public void testNonStaticReference4() {
        String contents = "class C { int length;}\nGGG.@length";
        assertUnknown(contents, "length");
    }

    @Test
    public void testNonStaticReference5() {
        String contents = "class C { int length() {}}\nGGG.length()";
        assertUnknown(contents, "length");
    }

    @Test
    public void testNonStaticReference6() {
        String contents = "class C { def length = {}}\nGGG.length()";
        assertUnknown(contents, "length");
    }

    @Test
    public void testNonStaticReference7() {
        String contents = "class C { int length() {} \nstatic {\nlength();}}";
        assertUnknown(contents, "length");
    }

    @Test
    public void testNonStaticReference8() {
        String contents = "class C { def length = {} \nstatic {\nlength();}}";
        assertUnknown(contents, "length");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9691
    public void testNonStaticReference9() {
        //@formatter:off
        String contents =
            "void sourceSets(Closure block) {\n" +
            "}\n" +
            "sourceSets {\n" +
            "  main {\n" +
            "    java { srcDirs = [] }\n" +
            "    groovy { srcDirs = ['src/main'] }\n" +
            "  }\n" +
            "  test {\n" +
            "    java { srcDirs = [] }\n" +
            "    groovy { srcDirs = ['src/test'] }\n" +
            "  }\n" +
            "}\n";
        //@formatter:on
        int offset = contents.indexOf("main");
        assertUnknownConfidence(contents, offset, offset + 4);
    }

    //

    @Test
    public void testStaticImport1() {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() {}}");

        String contents = "import static p.Other.FOO";
        assertKnown(contents, "FOO", "p.Other", "java.lang.Integer");
    }

    @Test
    public void testStaticImport2() {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() {}}");

        String contents = "import static p.Other.FOO as BAR";
        assertKnown(contents, "FOO", "p.Other", "java.lang.Integer");
    }

    @Test
    public void testStaticImport3() {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() {}}");

        String contents = "import static p.Other.FOO\nFOO";
        assertKnown(contents, "FOO", "p.Other", "java.lang.Integer");
    }

    @Test
    public void testStaticImport4() {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() {}}");

        String contents = "import static p.Other.FOO as BAR\nFOO";
        assertUnknown(contents, "FOO");
    }

    @Test
    public void testStaticImport5() {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() {}}");

        String contents = "import static p.Other.FO";
        assertUnknown(contents, "FO");
    }

    @Test
    public void testStaticImport6() {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() {}}");

        String contents = "import static p.Other.BAR\nBAR";
        int offset = contents.indexOf("BAR");
        assertType(contents, offset, offset + "BAR".length(), "java.lang.Boolean");
        offset = contents.lastIndexOf("BAR");
        assertType(contents, offset, offset + "BAR".length(), "java.lang.Boolean");
    }

    @Test
    public void testStaticImport7() {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() {}}");

        String contents = "import static p.Other.FOO\nFOO";
        assertKnown(contents, "p.Other", "p.Other", "p.Other");
    }

    @Test
    public void testStaticImport8() {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() {}}");

        String contents = "import static p.Other.FOO as BAR\nFOO";
        assertKnown(contents, "p.Other", "p.Other", "p.Other");
    }

    @Test
    public void testStaticImport9() {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() {}}");

        String contents = "import static p.Other.*\nFOO";
        assertKnown(contents, "p.Other", "p.Other", "p.Other");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/539
    public void testStaticImport10() {
        createUnit("p", "Other", "package p\nclass Other { static void dump(Object o) {}}");

        String contents = "import static p.Other.dump\n";
        assertKnown(contents, "dump", "p.Other", "java.lang.Void");
    }

    @Test
    public void testStaticImport11() {
        String contents = "import static java.util.Map.Entry\n";
        assertType(contents, "Entry", "java.lang.Class<java.util.Map$Entry>");
    }

    @Test // GRECLIPSE-1363
    public void testStaticImport12() {
        createUnit("p", "Other",
            "package p\n" +
            "class Other {\n" +
            "  public static int CONST = 42\n" +
            "}\n");

        String contents = "p.Other.CONST\n";
        int offset = contents.lastIndexOf("CONST");
        assertType(contents, offset, offset + 5, "java.lang.Integer");
    }

    @Test @Ignore("https://issues.apache.org/jira/browse/GROOVY-7744")
    public void testStaticImport13() {
        createUnit("p", "A", "package p\nclass A {\nstatic boolean isSomething(String s) {}\n}");
        createUnit("p", "B", "package p\nclass B {\nstatic boolean isSomething(Iterable i) {}\n}");

        String contents =
            "import static p.A.isSomething\n" +
            "import static p.B.isSomething\n" +
            "class C {\n" +
            "  static boolean isSomething(Object[] a) {}\n" +
            "  static void m(String s, Iterable i, Object[] a) {\n" +
            "    isSomething(s)\n" +
            "    isSomething(i)\n" +
            "    isSomething(a)\n" +
            "  }\n" +
            "\n";
        int offset = contents.indexOf("isSomething(s)");
        assertDeclaringType(contents, offset, offset + "isSomething".length(), "p.A");
            offset = contents.indexOf("isSomething(i)");
        assertDeclaringType(contents, offset, offset + "isSomething".length(), "p.B");
            offset = contents.indexOf("isSomething(a)");
        assertDeclaringType(contents, offset, offset + "isSomething".length(), "C");
    }

    @Test @Ignore("https://issues.apache.org/jira/browse/GROOVY-7744")
    public void testStaticImport14() {
        createUnit("p", "A", "package p\nclass A {\nstatic boolean isSomething(String s) {}\n}");
        createUnit("p", "B", "package p\nclass B {\nstatic boolean isSomething(Iterable i) {}\n}");

        String contents =
            "import static p.A.*\n" +
            "import static p.B.*\n" +
            "class C {\n" +
            "  static boolean isSomething(Object[] a) {}\n" +
            "  static void m(String s, Iterable i, Object[] a) {\n" +
            "    isSomething(s)\n" +
            "    isSomething(i)\n" +
            "    isSomething(a)\n" +
            "  }\n" +
            "\n";
        int offset = contents.indexOf("isSomething(s)");
        assertDeclaringType(contents, offset, offset + "isSomething".length(), "p.A");
            offset = contents.indexOf("isSomething(i)");
        assertDeclaringType(contents, offset, offset + "isSomething".length(), "p.B");
            offset = contents.indexOf("isSomething(a)");
        assertDeclaringType(contents, offset, offset + "isSomething".length(), "C");
    }

    @Test @Ignore("https://issues.apache.org/jira/browse/GROOVY-7744")
    public void testStaticImport15() {
        createUnit("p", "A", "package p\nclass A {\nstatic boolean isSomething(String s) {}\n}");
        createUnit("p", "B", "package p\nclass B {\nstatic boolean isSomething(Iterable i) {}\n}");

        String contents =
            "import static p.A.isSomething as wasSomething\n" +
            "import static p.B.isSomething as wasSomething\n" +
            "static boolean wasSomething(Object[] a) {}\n" +
            "String s; Iterable i; Object[] a\n" +
            "wasSomething(s)\n" +
            "wasSomething(i)\n" +
            "wasSomething(a)\n";
        int offset = contents.indexOf("wasSomething(s)");
        assertDeclaringType(contents, offset, offset + "wasSomething".length(), "p.A");
            offset = contents.indexOf("wasSomething(i)");
        assertDeclaringType(contents, offset, offset + "wasSomething".length(), "p.B");
            offset = contents.indexOf("wasSomething(a)");
        assertDeclaringType(contents, offset, offset + "wasSomething".length(), DEFAULT_UNIT_NAME);
    }

    @Test // GROOVY-9382, GROOVY-10133
    public void testStaticImport16() {
        createUnit("p", "A", "package p\nclass A {\nstatic Boolean isBoolean() {}\n}");
        createUnit("p", "B", "package p\nclass B {\nstatic Integer isNumeric() {}\n}");

        String contents =
            "import static p.A.isBoolean as isUnknown1\n" +
            "import static p.B.isNumeric as isUnknown2\n" +
            "unknown1\n" +
            "unknown2\n";
        if (isAtLeastGroovy(40)) {
            assertUnknown(contents, "unknown1");
            assertUnknown(contents, "unknown2");
        } else {
            assertDeclaringType(contents, "unknown1", "p.A");
            assertDeclaringType(contents, "unknown2", "p.B");
        }
    }

    @Test
    public void testStaticImport17() {
        String contents =
            "import static javax.swing.text.html.HTML.NULL_ATTRIBUTE_VALUE\n" +
            "NULL_ATTRIBUTE_VALUE\n";
        assertType(contents, "NULL_ATTRIBUTE_VALUE", "java.lang.String");
    }

    @Test
    public void testStaticImport18() {
        String contents =
            "import static javax.swing.text.html.HTML.getAttributeKey\n" +
            "getAttributeKey('')\n";
        assertType(contents, "getAttributeKey('')", "javax.swing.text.html.HTML$Attribute");
    }

    @Test
    public void testStaticImport19() {
        String contents =
            "import static javax.swing.text.html.HTML.*\n" +
            "NULL_ATTRIBUTE_VALUE\n";
        assertType(contents, "NULL_ATTRIBUTE_VALUE", "java.lang.String");
    }

    @Test
    public void testStaticImport20() {
        String contents =
            "import static javax.swing.text.html.HTML.*\n" +
            "getAttributeKey('')\n";
        assertType(contents, "getAttributeKey('')", "javax.swing.text.html.HTML$Attribute");
    }
}
