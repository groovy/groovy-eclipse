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

import static org.hamcrest.core.IsInstanceOf.instanceOf;

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
    public void testClassReference4a() {
        String contents = "String?.dyn";
        assertType(contents, 0, 6, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference5() {
        String contents = "String.@dyn";
        assertType(contents, 0, 6, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference6() {
        String contents = "String.&dyn";
        assertType(contents, 0, 6, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference7() {
        String contents = "String.class.dyn";
        assertType(contents, 0, 12, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference8() {
        String contents = "String.class.@dyn";
        assertType(contents, 0, 12, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference9() {
        String contents = "String.class.&dyn";
        assertType(contents, 0, 12, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference10() {
        String contents = "String.getClass()"; // same as "(String.class).getClass()"
        assertKnown(contents, "getClass", "java.lang.Object", "java.lang.Class<?>");
    }

    @Test
    public void testClassReference11() {
        String contents = "String.class.getCanonicalName()";
        assertKnown(contents, "getCanonicalName", "java.lang.Class", "java.lang.String");
    }

    @Test
    public void testClassReference12() {
        String contents = "String.getCanonicalName()";
        assertKnown(contents, "getCanonicalName", "java.lang.Class", "java.lang.String");
    }

    @Test
    public void testClassReference13() {
        String contents = "String.class.canonicalName";
        assertKnown(contents, "canonicalName", "java.lang.Class", "java.lang.String");
    }

    @Test
    public void testClassReference14() {
        String contents = "String.canonicalName";
        assertKnown(contents, "canonicalName", "java.lang.Class", "java.lang.String");
    }

    @Test // Class members may be accessed directly from static initializers
    public void testClassReference15() {
        String contents = "class S { static { getCanonicalName() } }";
        assertKnown(contents, "getCanonicalName", "java.lang.Class", "java.lang.String");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/538
    public void testClassReference16() {
        String contents = "String.getMethod('toLowerCase')";
        assertKnown(contents, "getMethod", "java.lang.Class", "java.lang.reflect.Method");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/538
    public void testClassReference16a() {
        String contents = "String.class.getMethod('toLowerCase')";
        assertKnown(contents, "getMethod", "java.lang.Class", "java.lang.reflect.Method");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/538
    public void testClassReference17() {
        String contents = "String.getConstructor()";
        assertKnown(contents, "getConstructor", "java.lang.Class", "java.lang.reflect.Constructor<java.lang.String>");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/538
    public void testClassReference17a() {
        String contents = "String.class.getConstructor()";
        assertKnown(contents, "getConstructor", "java.lang.Class", "java.lang.reflect.Constructor<java.lang.String>");
    }

    //

    @Test
    public void testStaticReference1() {
        String contents =
            "class GGG {\n" +
            "  static int length\n" +
            "}\n" +
            "GGG.length";
        ASTNode decl = assertKnown(contents, "length", "GGG", "java.lang.Integer");
        Assert.assertThat(decl, instanceOf(PropertyNode.class));
    }

    @Test
    public void testStaticReference1a() {
        String contents =
            "class GGG {\n" +
            "  static int length\n" +
            "  static int getLength() { length }\n" +
            "}\n" +
            "GGG.length";
        ASTNode decl = assertKnown(contents, "length", "GGG", "java.lang.Integer");
        Assert.assertThat(decl, instanceOf(MethodNode.class)); // not FieldNode
    }

    @Test
    public void testStaticReference1b() {
        String contents =
            "class GGG {\n" +
            "  static int length\n" +
            "  static int getLength() { length }\n" +
            "}\n" +
            "new GGG().length";
        ASTNode decl = assertKnown(contents, "length", "GGG", "java.lang.Integer");
        Assert.assertThat(decl, instanceOf(MethodNode.class)); // not FieldNode
    }

    @Test // GRECLIPSE-1544
    public void testStaticReference2() {
        String contents = "package p\n" +
            "@groovy.transform.TypeChecked\n" +
            "public class BugClass {\n" +
            "  static BugClass getInstance() { null }\n" +
            "  void showBug() {\n" +
            "    BugClass.getInstance();  \n" +
            "  }\n" +
            "}";
        assertKnown(contents, "getInstance", "p.BugClass", "p.BugClass");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/525
    public void testStaticReference3() {
        // name clashes with methods available from java.lang.Class and org.codehaus.groovy.runtime.DefaultGroovyMethods
        String contents = "String[] arr; String str = Arrays.toString(arr)";
        assertKnown(contents, "toString", "java.util.Arrays", "java.lang.String");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/525
    public void testStaticReference4() {
        // name clashes with methods available from java.lang.Class and org.codehaus.groovy.runtime.DefaultGroovyMethods
        String contents = "def stringify = Arrays.&toString";
        assertKnown(contents, "toString", "java.util.Arrays", "java.lang.String");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/595
    public void testStaticReference5() {
        String contents =
            "class Chars {\n" +
            "  static boolean equals(char[] a, char[] b) {\n" +
            "  }\n" +
            "  static void meth(char[] x, char[] y) {\n" +
            "    if (Chars.equals(x, y)) {\n" + // name clashes with method available from java.lang.Object
            "    }\n" +
            "  }\n" +
            "}\n";
        assertKnown(contents, "equals", "Chars", "java.lang.Boolean");
    }

    @Test
    public void testStaticReference6() {
        String contents = "def search = Arrays.&binarySearch";
        assertKnown(contents, "binarySearch", "java.util.Arrays", "java.lang.Integer");
    }

    @Test
    public void testStaticReference7() {
        String contents = "Arrays.&mixin";
        assertUnknown(contents, "mixin");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/646
    public void testStaticReference8() {
        String contents = "class Unit {}\n" +
            "class Assert {\n" +
            "  protected void assertType(String src, String expected) {}\n" +
            "  protected void assertType(String src, int off, int len, String expected) {}\n" +
            "  public static void assertType(Unit src, int off, int len, String expected) {}\n" +
            "}\n" +
            "Unit unit = null; int offset = 0" +
            "Assert.assertType(unit, offset, offset + 'string'.length(), 'java.util.Collection')";

        assertKnown(contents, "assertType", "Assert", "java.lang.Void");
    }

    @Test
    public void testStaticReference9() {
        String contents =
            "class Static {\n" +
            "  static def foo\n" +
            "  static {\n" +
            "    foo\n" +
            "  }\n" +
            "}\n";
        ASTNode decl = assertKnown(contents, "foo", "Static", "java.lang.Object");
        Assert.assertThat(decl, instanceOf(FieldNode.class)); // not MethodNode
    }

    @Test
    public void testStaticReference9a() {
        String contents =
            "class Static {\n" +
            "  static def foo\n" +
            "  static def method() {\n" +
            "    foo\n" +
            "  }\n" +
            "}\n";
        ASTNode decl = assertKnown(contents, "foo", "Static", "java.lang.Object");
        Assert.assertThat(decl, instanceOf(FieldNode.class)); // not MethodNode
    }

    @Test
    public void testStaticReference10() {
        String contents =
            "class Static {\n" +
            "  static def foo\n" +
            "  static def getFoo() {}\n" +
            "  static {\n" +
            "    foo\n" +
            "  }\n" +
            "}\n";
        ASTNode decl = assertKnown(contents, "foo", "Static", "java.lang.Object");
        Assert.assertThat(decl, instanceOf(FieldNode.class)); // not MethodNode
    }

    @Test
    public void testStaticReference10a() {
        String contents =
            "class Static {\n" +
            "  static def foo\n" +
            "  static def getFoo() {}\n" +
            "  static def method() {\n" +
            "    foo\n" +
            "  }\n" +
            "}\n";
        ASTNode decl = assertKnown(contents, "foo", "Static", "java.lang.Object");
        Assert.assertThat(decl, instanceOf(FieldNode.class)); // not MethodNode
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
        String contents = "class GGG { int length }\nGGG.length";
        assertUnknown(contents, "length");
    }

    @Test
    public void testNonStaticReference4() {
        String contents = "class GGG { int length }\nGGG.@length";
        assertUnknown(contents, "length");
    }

    @Test
    public void testNonStaticReference5() {
        String contents = "class GGG { int length() { } }\nGGG.length()";
        assertUnknown(contents, "length");
    }

    @Test
    public void testNonStaticReference6() {
        String contents = "class GGG { def length = { } }\nGGG.length()";
        assertUnknown(contents, "length");
    }

    @Test
    public void testNonStaticReference7() {
        String contents = "class GGG { int length() { } \nstatic {\nlength() } }";
        assertUnknown(contents, "length");
    }

    @Test
    public void testNonStaticReference8() {
        String contents = "class GGG { def length = { } \nstatic {\nlength() } }";
        assertUnknown(contents, "length");
    }

    //

    @Test
    public void testStaticImport1() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");

        String contents = "import static p.Other.FOO";
        assertKnown(contents, "FOO", "p.Other", "java.lang.Integer");
    }

    @Test
    public void testStaticImport2() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");

        String contents = "import static p.Other.FOO as BAR";
        assertKnown(contents, "FOO", "p.Other", "java.lang.Integer");
    }

    @Test
    public void testStaticImport3() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");

        String contents = "import static p.Other.FOO\nFOO";
        assertKnown(contents, "FOO", "p.Other", "java.lang.Integer");
    }

    @Test
    public void testStaticImport4() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");

        String contents = "import static p.Other.FOO as BAR\nFOO";
        assertUnknown(contents, "FOO");
    }

    @Test
    public void testStaticImport5() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");

        String contents = "import static p.Other.FO";
        assertUnknown(contents, "FO");
    }

    @Test
    public void testStaticImport6() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");

        String contents = "import static p.Other.BAR\nBAR";
        int offset = contents.indexOf("BAR");
        assertType(contents, offset, offset + "BAR".length(), "java.lang.Boolean");
        offset = contents.lastIndexOf("BAR");
        assertType(contents, offset, offset + "BAR".length(), "java.lang.Boolean");
    }

    @Test
    public void testStaticImport7() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");

        String contents = "import static p.Other.FOO\nFOO";
        assertKnown(contents, "p.Other", "p.Other", "p.Other");
    }

    @Test
    public void testStaticImport8() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");

        String contents = "import static p.Other.FOO as BAR\nFOO";
        assertKnown(contents, "p.Other", "p.Other", "p.Other");
    }

    @Test
    public void testStaticImport9() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");

        String contents = "import static p.Other.*\nFOO";
        assertKnown(contents, "p.Other", "p.Other", "p.Other");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/539
    public void testStaticImport10() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static void dump(Object o) { } }");

        String contents = "import static p.Other.dump\n";
        assertKnown(contents, "dump", "p.Other", "java.lang.Void");
    }

    @Test // GRECLIPSE-1371
    public void testStaticImport11() throws Exception {
        String contents =
            "import static Boolean.TRUE\n" +
            "class StaticImportStaticField {\n" +
            "  static boolean FLAG = TRUE\n" +
            "}\n";
        int offset = contents.lastIndexOf("TRUE");
        assertType(contents, offset, offset + 4, "java.lang.Boolean");
    }

    @Test // GRECLIPSE-1363
    public void testStaticImport12() throws Exception {
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
    public void testStaticImport13() throws Exception {
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
    public void testStaticImport14() throws Exception {
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
    public void testStaticImport15() throws Exception {
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
}
