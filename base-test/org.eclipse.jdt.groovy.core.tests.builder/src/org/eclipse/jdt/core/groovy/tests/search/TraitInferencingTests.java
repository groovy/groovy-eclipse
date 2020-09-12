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

    @Test // GROOVY-9255
    public void testProperty14() {
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
    public void testProperty15() {
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
    public void testProperty16() {
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
    public void testProperty17() {
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
    public void testProperty18() {
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
    public void testProperty19() {
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

            offset = contents.lastIndexOf("m()");
        assertDeclaringType(contents, offset, offset + 1, "T");
    }

    @Test
    public void testPublicStaticMethod2() {
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

            offset = contents.indexOf("C.m()") + 2;
        assertDeclaringType(contents, offset, offset + 1, "T");

            offset = contents.lastIndexOf("m()");
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
            "}\n" +
            "T.m()\n" +
            "C.m()\n";
        //@formatter:on

        int offset = contents.indexOf("T.m()") + 2;
        assertUnknownConfidence(contents, offset, offset + 1);

            offset = contents.indexOf("C.m()") + 2;
        assertDeclaringType(contents, offset, offset + 1, "T");
    }

    @Test
    public void testPublicStaticMethod4() {
        //@formatter:off
        String contents =
            "trait T {\n" +
            "  static m() { 'T' }\n" +
            "}\n" +
            "class C {\n" +
            "  def m() { 'C' }\n" +
            "}\n" +
            "class D extends C implements T {\n" +
            "}\n" +
            "print new D().m()\n";
        //@formatter:on

        assertDeclType(contents, "m", "T");
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
            "    if (check()) {\n" +
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

    //

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

    @Test
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
            "}";
        //@formatter:on

        assertDeclType(contents, "method", "B");
        assertExprType(contents, "method", "java.lang.Void");
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
            "}";
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
            "}";
        //@formatter:on

        assertDeclType(contents, "method", "B");
        assertExprType(contents, "method", "java.lang.Void");
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
}
