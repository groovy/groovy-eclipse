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

import org.junit.Test;

public final class TraitInferencingTests extends InferencingTestSuite {

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
    public void testProperty1() {
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "  void meth() {\n" +
            "    println number\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty2() {
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "  void meth() {\n" +
            "    number = 42\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty3() {
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "  void meth() {\n" +
            "    println this.number\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty4() {
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "  void meth() {\n" +
            "    this.number = 42\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty5() {
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "  void meth() {\n" +
            "    println getNumber()\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "getNumber", "T");
        assertExprType(source, "getNumber", "java.lang.Number");
    }

    @Test
    public void testProperty6() {
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "  void meth() {\n" +
            "    setNumber(42)\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "setNumber", "T");
        assertExprType(source, "setNumber", "java.lang.Void");
    }

    @Test
    public void testProperty7() {
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    println number\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty8() {
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    number = 42\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty9() {
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    println this.number\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty10() {
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    this.number = 42\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Number");
    }

    @Test
    public void testProperty11() {
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    println getNumber()\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "getNumber", "T");
        assertExprType(source, "getNumber", "java.lang.Number");
    }

    @Test
    public void testProperty12() {
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    setNumber(42)\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "setNumber", "T");
        assertExprType(source, "setNumber", "java.lang.Void");
    }

    @Test
    public void testProperty13() {
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    println T.super.number\n" +
            "  }\n" +
            "}\n";

        // TODO: assertDeclType(source, "number", "T");
        // TODO: assertExprType(source, "number", "java.lang.Number");
        assertUnknownConfidence(source, source.lastIndexOf("number"), source.lastIndexOf("number") + "number".length());
    }

    @Test
    public void testProperty14() {
        String source =
            "trait T {\n" +
            "  Number number\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void meth() {\n" +
            "    T.super.number = 42\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "number", "T");
        assertExprType(source, "number", "java.lang.Void");
    }

    //

    @Test
    public void testPublicMethod1() {
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

        assertDeclType(source, "check", "Auditable");
        assertExprType(source, "check", "java.lang.Boolean");
    }

    @Test
    public void testPublicMethod2() {
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
            "}";

        assertDeclType(contents, "method", "A");
        assertExprType(contents, "method", "java.lang.Void");
    }

    @Test
    public void testPublicMethod3() {
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
            "}";

        assertDeclType(contents, "method", "A");
        assertExprType(contents, "method", "java.lang.Void");
    }

    @Test
    public void testPublicMethod4() {
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
            "}";

        assertDeclType(contents, "method", "B");
        assertExprType(contents, "method", "java.lang.Void");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8272
    public void testPublicStaticSuperMethod1() {
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

        assertDeclType(source, "check", "Checkable");
        assertExprType(source, "check", "java.lang.Boolean");
    }

    @Test
    public void testPublicStaticSuperMethod2() {
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

        assertDeclType(source, "check", "Checkable");
        assertExprType(source, "check", "java.lang.Boolean");
    }

    @Test
    public void testPublicField1() {
        String source =
            "trait T {\n" +
            "  public String field\n" +
            "  void m() {\n" +
            "    field\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "field", "T");
        assertExprType(source, "field", "java.lang.String");
    }

    @Test
    public void testPublicField2() {
        String source =
            "trait T {\n" +
            "  public String field\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void m() {\n" +
            "    T__field\n" +
            "  }\n" +
            "}";

        assertDeclType(source, "T__field", "T");
        assertExprType(source, "T__field", "java.lang.String");
    }

    @Test
    public void testPublicStaticField() {
        String source =
            "trait T {\n" +
            "  public static String field\n" +
            "  void m() {\n" +
            "    field\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "field", "T");
        assertExprType(source, "field", "java.lang.String");
    }

    @Test
    public void testPublicStaticFinalField() {
        String source =
            "trait T {\n" +
            "  public static final String field = 'value'\n" +
            "  void m() {\n" +
            "    field\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "field", "T");
        assertExprType(source, "field", "java.lang.String");
    }

    //

    @Test
    public void testPrivateMethod1() {
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

        assertDeclType(source, "check", "Auditable");
        assertExprType(source, "check", "java.lang.Boolean");
    }

    @Test
    public void testPrivateMethod2() {
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

        assertDeclType(contents, "method", "A");
        assertExprType(contents, "method", "java.lang.Void");
    }

    @Test
    public void testPrivateMethod3() {
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

        assertDeclType(contents, "method", "A");
        assertExprType(contents, "method", "java.lang.Void");
    }

    @Test
    public void testPrivateMethod4() {
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

        assertDeclType(contents, "method", "B");
        assertExprType(contents, "method", "java.lang.Void");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8854
    public void testPrivateStaticMethod() {
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

        assertDeclType(source, "check", "Auditable");
        assertExprType(source, "check", "java.lang.Boolean");
    }

    @Test
    public void testPrivateField1() {
        String source =
            "trait T {\n" +
            "  private String field\n" +
            "  void m() {\n" +
            "    field\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "field", "T");
        assertExprType(source, "field", "java.lang.String");
    }

    @Test
    public void testPrivateField2() {
        String source =
            "trait T {\n" +
            "  private String field\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void m() {\n" +
            "    T__field\n" +
            "  }\n" +
            "}";

        assertDeclType(source, "T__field", "T");
        assertExprType(source, "T__field", "java.lang.String");
    }

    @Test
    public void testPrivateStaticField() {
        String source =
            "trait T {\n" +
            "  private static String field\n" +
            "  void m() {\n" +
            "    field\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "field", "T");
        assertExprType(source, "field", "java.lang.String");
    }

    @Test
    public void testPrivateStaticFinalField() {
        String source =
            "trait T {\n" +
            "  private static final String field = 'value'\n" +
            "  void m() {\n" +
            "    field\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "field", "T");
        assertExprType(source, "field", "java.lang.String");
    }
}
