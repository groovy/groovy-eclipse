/*
 * Copyright 2009-2026 the original author or authors.
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

import org.junit.Test;

public final class TraitInferencingTests extends InferencingTestSuite {

    private void assertDeclType(String source, String target, String type) {
        assertDeclaringType(source, target, type);
    }

    private void assertExprType(String source, String target, String type) {
        assertType(source, target, type);
    }

    //--------------------------------------------------------------------------

    @Test
    public void testProperty1() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "  void meth() {\n" +
            "    println number\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty2() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "  void meth() {\n" +
            "    number = 42\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty3() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "  void meth() {\n" +
            "    println this.number\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty4() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "  void meth() {\n" +
            "    this.number = 42\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty5() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "  void meth() {\n" +
            "    println getNumber()\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "getNumber", "T");
        assertExprType(source, "getNumber", "java.lang.Number");
    }

    @Test
    public void testProperty6() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "  void meth() {\n" +
            "    setNumber(42)\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "setNumber", "T");
        assertExprType(source, "setNumber", "java.lang.Void");
    }

    @Test
    public void testProperty7() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    println number\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty8() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    number = 42\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty9() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    println this.number\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty10() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    this.number = 42\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty11() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    println getNumber()\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "getNumber", "T");
        assertExprType(source, "getNumber", "java.lang.Number");
    }

    @Test
    public void testProperty12() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    setNumber(42)\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "setNumber", "T");
        assertExprType(source, "setNumber", "java.lang.Void");
    }

    @Test
    public void testProperty13() {
        //@formatter:off
        String source =
            "trait A {\n" +
            "  Number number\n" +
            "}\n" +
            "trait B {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "  def n = number\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "B");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty14() {
        //@formatter:off
        String source =
            "trait A {\n" +
            "  Number number\n" +
            "}\n" +
            "trait B extends A {\n" +
            "}\n" +
            "class C implements B {\n" +
            "  def n = number\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "A");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1361
    public void testProperty15() {
        createUnit("T",
            "trait A {\n" +
            "  Number number\n" +
            "}\n" +
            "trait B extends A {\n" +
            "}\n");

        //@formatter:off
        String source =
            "class C implements B {\n" +
            "  def n = number\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "A");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1361
    public void testProperty16() {
        createUnit("A",
            "trait A {\n" +
            "  Number number\n" +
            "}\n");
        createUnit("B",
            "trait B extends A {\n" +
            "}\n");

        //@formatter:off
        String source =
            "class C implements B {\n" +
            "  def n = number\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "A");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9255
    public void testProperty17() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  Number number = 42\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    println T.super.number\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty18() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    T.super.number = 42\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Void");
    }

    @Test
    public void testProperty19() {
        createUnit("T",
            "trait T {\n" +
            "  Number number\n" +
            "}\n");

        //@formatter:off
        String source =
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    def n = number\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty20() {
        createUnit("T",
            "trait T {\n" +
            "  Number number\n" +
            "}\n");

        //@formatter:off
        String source =
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    number = 42\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty21() {
        createUnit("T",
            "trait T {\n" +
            "  Number number\n" +
            "}\n");

        //@formatter:off
        String source =
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    def n = getNumber()\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "getNumber", "T");
        assertExprType(source, "getNumber", "java.lang.Number");
    }

    @Test
    public void testProperty22() {
        createUnit("T",
            "trait T {\n" +
            "  Number number\n" +
            "}\n");

        //@formatter:off
        String source =
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    setNumber(42)\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "setNumber", "T");
        assertExprType(source, "setNumber", "java.lang.Void");
    }

    @Test
    public void testProperty23() {
        createUnit("T",
            "trait T {\n" +
            "  Number[] numbers\n" +
            "}\n");

        //@formatter:off
        String source =
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    def anArray = numbers\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "numbers", "T");
        assertExprType(source, "numbers", "java.lang.Number[]");
        assertExprType(source, "anArray", "java.lang.Number[]");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1599
    public void testProperty24() {
        createUnit("T",
            "import java.beans.BeanInfo\n" +
            "trait T {\n" +
            "  Map<BeanInfo,Number[]> numbers\n" +
            "}\n");

        //@formatter:off
        String source =
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    def anArray = numbers['k']\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "numbers", "T");
        assertExprType(source, "numbers", "java.util.Map<java.beans.BeanInfo,java.lang.Number[]>");
        assertExprType(source, "anArray", "java.lang.Number[]");
    }

    @Test
    public void testProperty25() {
        createUnit("T",
            "trait T {\n" +
            "  static Number number\n" +
            "}\n");

        //@formatter:off
        String source =
            "class C implements T {\n" +
            "  def m() {\n" +
            "    number\n" +
            "  }\n" +
            "  static sm(C c, T t) {\n" +
            "    c.number\n" +
            "    t.number\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = source.indexOf("number");
        assertDeclaringType(source, offset, offset + 6, "T");
        assertType/*     */(source, offset, offset + 6, "java.lang.Number");

        /**/offset = source.indexOf("c.number") + 2;
        assertDeclaringType(source, offset, offset + 6, "T");
        assertType/*     */(source, offset, offset + 6, "java.lang.Number");

        /**/offset = source.indexOf("t.number") + 2;
        assertDeclaringType(source, offset, offset + 6, "T");
        assertType/*     */(source, offset, offset + 6, "java.lang.Number");
    }

    @Test
    public void testProperty26() {
        createUnit("T",
            "trait T {\n" +
            "  static final Number number = 1\n" +
            "}\n");

        //@formatter:off
        String source =
            "class C implements T {\n" +
            "  def m() {\n" +
            "    number\n" +
            "  }\n" +
            "  static sm(C c, T t) {\n" +
            "    c.number\n" +
            "    t.number\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = source.indexOf("number");
        assertDeclaringType(source, offset, offset + 6, "T");
        assertType/*     */(source, offset, offset + 6, "java.lang.Number");

        /**/offset = source.indexOf("c.number") + 2;
        assertDeclaringType(source, offset, offset + 6, "T");
        assertType/*     */(source, offset, offset + 6, "java.lang.Number");

        /**/offset = source.indexOf("t.number") + 2;
        assertDeclaringType(source, offset, offset + 6, "T");
        assertType/*     */(source, offset, offset + 6, "java.lang.Number");
    }

    @Test
    public void testPublicField1() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  public String field\n" +
            "  void m() {\n" +
            "    field\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "field", "T");
        assertExprType(source, "field", "java.lang.String");
    }

    @Test
    public void testPublicField2() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  public String field\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void m() {\n" +
            "    T__field\n" +
            "  }\n" +
            "}";
        //@formatter:on

        assertDeclType(source, "T__field", "T");
        assertExprType(source, "T__field", "java.lang.String");
    }

    @Test
    public void testPublicStaticField() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  public static String field\n" +
            "  void m() {\n" +
            "    field\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "field", "T");
        assertExprType(source, "field", "java.lang.String");
    }

    @Test
    public void testPublicStaticFinalField() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  public static final String field = 'value'\n" +
            "  void m() {\n" +
            "    field\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "field", "T");
        assertExprType(source, "field", "java.lang.String");
    }

    @Test
    public void testPrivateField1() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  private String field\n" +
            "  void m() {\n" +
            "    field\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "field", "T");
        assertExprType(source, "field", "java.lang.String");
    }

    @Test
    public void testPrivateField2() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  private String field\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void m() {\n" +
            "    T__field\n" +
            "  }\n" +
            "}";
        //@formatter:on

        assertDeclType(source, "T__field", "T");
        assertExprType(source, "T__field", "java.lang.String");
    }

    @Test
    public void testPrivateStaticField() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  private static String field\n" +
            "  void m() {\n" +
            "    field\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "field", "T");
        assertExprType(source, "field", "java.lang.String");
    }

    @Test
    public void testPrivateStaticFinalField() {
        //@formatter:off
        String source =
            "trait T {\n" +
            "  private static final String field = 'value'\n" +
            "  void m() {\n" +
            "    field\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "field", "T");
        assertExprType(source, "field", "java.lang.String");
    }

    //

    @Test
    public void testPublicMethod1() {
        //@formatter:off
        String source =
            "trait Auditable {\n" +
            "  boolean check() {\n" +
            "    true\n" +
            "  }\n" +
            "  boolean audit() {\n" +
            "    if (check()) {\n" +
            "      ;\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "check", "Auditable");
        assertExprType(source, "check", "java.lang.Boolean");
    }

    @Test
    public void testPublicMethod2() {
        //@formatter:off
        String contents =
            "trait A {\n" +
            "  void method() {}\n" +
            "}\n" +
            "trait B {\n" +
            "  void method() {}\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "  void something() {\n" +
            "    method()\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(contents, "method", "B");
        assertExprType(contents, "method", "java.lang.Void");
    }

    @Test
    public void testPublicMethod3() {
        //@formatter:off
        String contents =
            "trait A {\n" +
            "  void method() {}\n" +
            "}\n" +
            "trait B {\n" +
            "  void method() {}\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "  void something() {\n" +
            "    A.super.method()\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(contents, "method", "A");
        assertExprType(contents, "method", "java.lang.Void");
    }

    @Test
    public void testPublicMethod4() {
        //@formatter:off
        String contents =
            "trait A {\n" +
            "  void method() {}\n" +
            "}\n" +
            "trait B {\n" +
            "  void method() {}\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "  void something() {\n" +
            "    B.super.method()\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(contents, "method", "B");
        assertExprType(contents, "method", "java.lang.Void");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8587
    public void testPublicMethod5() {
        //@formatter:off
        String contents =
            "trait A {\n" +
            "  void method() {}\n" +
            "}\n" +
            "trait B extends A {\n" +
            "}\n" +
            "class C implements B {\n" +
            "  @Override\n" +
            "  void method() {\n" +
            "    B.super.method()\n" + // B$Trait$Helper.method(this)
            "  }\n" +
            "}\n";
        //@formatter:on

        if (isAtLeastGroovy(50)) {
            assertDeclType(contents, "method", "A");
            assertExprType(contents, "method", "java.lang.Void");
        } else {
            int offset = contents.lastIndexOf("method");
            assertUnknownConfidence(contents, offset, offset + 6);
        }
    }

    @Test
    public void testPublicMethod6() {
        //@formatter:off
        String contents =
            "trait A {\n" +
            "  void m() {}\n" +
            "}\n" +
            "trait B {\n" +
            "  void m() {}\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "}\n" +
            "trait T {\n" +
            "  void m() {}\n" +
            "}\n" +
            "class D extends C implements T {\n" +
            "  void test() {\n" +
            "    m()\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(contents, "m", "T");
        assertExprType(contents, "m", "java.lang.Void");
    }

    @Test
    public void testPublicMethod7() {
        //@formatter:off
        String source =
            "trait A<B> {\n" +
            "  B m() {\n" +
            "  }\n" +
            "}\n" +
            "class C<T> implements A<T> {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked p() {\n" +
            "  def x = new C<Number>().m()\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "m", "A");
        assertExprType(source, "m", "java.lang.Number");
        assertExprType(source, "x", "java.lang.Number");
    }

    @Test
    public void testPublicStaticMethod1() {
        //@formatter:off
        String contents =
            "trait T {\n" +
            "  static m() {}\n" +
            "  void x() {\n" +
            "    T.m()\n" +
            "    m()\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = contents.indexOf("T.m()") + 2;
        assertUnknownConfidence(contents, offset, offset + 1);

        /**/offset = contents.lastIndexOf("m()");
        assertDeclaringType(contents, offset, offset + 1, "T");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8854
    public void testPublicStaticMethod2() {
        //@formatter:off
        String contents =
            "trait T {\n" +
            "  static m() {}\n" +
            "  static x() {\n" +
            "    T.m()\n" +
            "    m()\n" + // this.m($static$self)
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = contents.indexOf("T.m()") + 2;
        assertUnknownConfidence(contents, offset, offset + 1);

        /**/offset = contents.lastIndexOf("m()");
        assertDeclaringType(contents, offset, offset + 1, "T");
    }

    @Test
    public void testPublicStaticMethod3() {
        //@formatter:off
        String contents =
            "trait T {\n" +
            "  static m() {}\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void x() {\n" +
            "    T.m()\n" +
            "    C.m()\n" +
            "    m()\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = contents.indexOf("T.m()") + 2;
        assertUnknownConfidence(contents, offset, offset + 1);

        /**/offset = contents.indexOf("C.m()") + 2;
        assertDeclaringType(contents, offset, offset + 1, "T");

        /**/offset = contents.lastIndexOf("m()");
        assertDeclaringType(contents, offset, offset + 1, "T");
    }

    @Test
    public void testPublicStaticMethod4() {
        createUnit("T",
            "trait T {\n" +
            "  static m() {}\n" +
            "}\n");

        //@formatter:off
        String contents =
            "class C implements T {\n" +
            "  void x() {\n" +
            "    T.m()\n" +
            "    C.m()\n" +
            "    m()\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = contents.indexOf("T.m()") + 2;
        assertUnknownConfidence(contents, offset, offset + 1);

        /**/offset = contents.indexOf("C.m()") + 2;
        assertDeclaringType(contents, offset, offset + 1, "T");

        /**/offset = contents.lastIndexOf("m()");
        assertDeclaringType(contents, offset, offset + 1, "T");
    }

    @Test
    public void testPublicStaticMethod5() {
        //@formatter:off
        String contents =
            "trait T {\n" +
            "  static m() {}\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "T.m()\n" +
            "C.m()\n";
        //@formatter:on

        int offset = contents.indexOf("T.m()") + 2;
        assertUnknownConfidence(contents, offset, offset + 1);

        /**/offset = contents.indexOf("C.m()") + 2;
        assertDeclaringType(contents, offset, offset + 1, "T");
    }

    @Test
    public void testPublicStaticMethod6() {
        createUnit("T",
            "trait T {\n" +
            "  static m() {}\n" +
            "}\n");

        //@formatter:off
        String contents =
            "class C implements T {\n" +
            "}\n" +
            "T.m()\n" +
            "C.m()\n";
        //@formatter:on

        int offset = contents.indexOf("T.m()") + 2;
        assertUnknownConfidence(contents, offset, offset + 1);

        /**/offset = contents.indexOf("C.m()") + 2;
        assertDeclaringType(contents, offset, offset + 1, "T");
    }

    @Test
    public void testPublicStaticMethod7() {
        //@formatter:off
        String contents =
            "trait T {\n" +
            "  static m() {'T'}\n" +
            "}\n" +
            "class C {\n" +
            "  def m() {'C'}\n" +
            "}\n" +
            "class D extends C implements T {\n" +
            "}\n" +
            "new D().m()\n";
        //@formatter:on

        assertDeclType(contents, "m", "T");
    }

    @Test
    public void testPublicStaticMethod8() {
        createUnit("T",
            "trait T {\n" +
            "  static m() {'T'}\n" +
            "}\n");
        incrementalBuild();

        //@formatter:off
        String contents =
            "class C {\n" +
            "  def m() {'C'}\n" +
            "}\n" +
            "class D extends C implements T {\n" +
            "}\n" +
            "new D().m()\n";
        //@formatter:on

        assertDeclType(contents, "m", "T");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-10106
    public void testPublicStaticMethod9() {
        for (String mods : new String[] {"", "@groovy.transform.TypeChecked ", "@groovy.transform.CompileStatic "}) {
            //@formatter:off
            String contents =
                "class C {\n" +
                "}\n" +
                mods + "trait T {\n" +
                "  static void m(C c) {\n" +
                "  }\n" +
                "  final C c = new C().tap {\n" +
                "    m(it)\n" +
                "  }\n" +
                "}\n";
            //@formatter:on

            assertDeclType(contents, "m", "T");
            assertDeclType(contents, "tap", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1509
    public void testPublicStaticMethod10() {
        //@formatter:off
        String contents =
            "trait T {\n" +
            "  static getX() {}\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "T.x\n" +
            "C.x\n";
        //@formatter:on

        int offset = contents.indexOf("T.x") + 2;
        assertUnknownConfidence(contents, offset, offset + 1);

        /**/offset = contents.indexOf("C.x") + 2;
        assertDeclaringType(contents, offset, offset + 1, "T");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1540
    public void testPublicStaticMethod11() {
        //@formatter:off
        String contents =
            "trait T {\n" +
            "  static m() {\n" +
            "    this.m()\n" +
            "    def that = this\n" +
            "    that.m()\n" +
            "  }\n" +
            "  def foo() {\n" +
            "    this.m()\n" +
            "    def that = this\n" +
            "    that.m()\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = contents.indexOf("this.m()") + 5;
        assertDeclaringType(contents, offset, offset + 1, "T");

        /**/offset = contents.indexOf("that.m()") + 5;
        assertDeclaringType(contents, offset, offset + 1, "T");

        /**/offset = contents.lastIndexOf("this.m()") + 5;
        assertDeclaringType(contents, offset, offset + 1, "T");

        /**/offset = contents.lastIndexOf("that.m()") + 5;
        assertDeclaringType(contents, offset, offset + 1, "T");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8272
    public void testPublicStaticSuperMethod1() {
        //@formatter:off
        String source =
            "trait Checkable {\n" +
            "  static boolean check() {\n" +
            "    true\n" +
            "  }\n" +
            "}\n" +
            "trait Auditable extends Checkable {\n" +
            "  boolean audit() {\n" +
            "    if (check()) {\n" + // this.check((Class)$self.getClass())
            "      ;\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "check", "Checkable");
        assertExprType(source, "check", "java.lang.Boolean");
    }

    @Test
    public void testPublicStaticSuperMethod2() {
        //@formatter:off
        String source =
            "trait Checkable {\n" +
            "  static boolean check() {\n" +
            "    true\n" +
            "  }\n" +
            "}\n" +
            "trait Auditable extends Checkable {\n" +
            "  boolean audit() {\n" +
            "    if (Checkable.super.check()) {\n" +
            "      ;\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "check", "Checkable");
        assertExprType(source, "check", "java.lang.Boolean");
    }

    @Test
    public void testPrivateMethod1() {
        //@formatter:off
        String source =
            "trait Auditable {\n" +
            "  private boolean check() {\n" +
            "    true\n" +
            "  }\n" +
            "  boolean audit() {\n" +
            "    if (check()) {\n" +
            "      ;\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "check", "Auditable");
        assertExprType(source, "check", "java.lang.Boolean");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8859
    public void testPrivateMethod2() {
        //@formatter:off
        String contents =
            "trait A {\n" +
            "  private void method() {}\n" +
            "}\n" +
            "trait B {\n" +
            "  private void method() {}\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "  void something() {\n" +
            "    method()\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = contents.lastIndexOf("method");
        assertUnknownConfidence(contents, offset, offset + 6);
    }

    @Test
    public void testPrivateMethod3() {
        //@formatter:off
        String contents =
            "trait A {\n" +
            "  private void method() {}\n" +
            "}\n" +
            "trait B {\n" +
            "  private void method() {}\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "  void something() {\n" +
            "    A.super.method()\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(contents, "method", "A");
        assertExprType(contents, "method", "java.lang.Void");
    }

    @Test
    public void testPrivateMethod4() {
        //@formatter:off
        String contents =
            "trait A {\n" +
            "  private void method() {}\n" +
            "}\n" +
            "trait B {\n" +
            "  private void method() {}\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "  void something() {\n" +
            "    B.super.method()\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(contents, "method", "B");
        assertExprType(contents, "method", "java.lang.Void");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-7486
    public void testPrivateMethod5() {
        //@formatter:off
        String contents =
            "trait T {\n" +
            "  private String f(String s) { s }\n" +
            "  void test() {\n" +
            "    ['x'].collect { String s -> f(s) }\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(contents, "f", "T");
        assertExprType(contents, "f", "java.lang.String");

        assertDeclType(contents, "collect", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8854
    public void testPrivateStaticMethod() {
        //@formatter:off
        String source =
            "trait Auditable {\n" +
            "  private static boolean check() {\n" +
            "    true\n" +
            "  }\n" +
            "  boolean audit() {\n" +
            "    if (check()) {\n" + // this.check((Class)$self.getClass())
            "      ;\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        assertDeclType(source, "check", "Auditable");
        assertExprType(source, "check", "java.lang.Boolean");
    }
}
