/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.groovy.tests.search;

import org.junit.Test;

/**
 * Tests that the inferred declaration is correct.
 */
public final class DeclarationInferencingTests extends InferencingTestSuite {

    @Test
    public void testGetterAndField1() {
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "}");

        String contents = "new Other().xxx";
        int start = contents.indexOf("xxx"), end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField2() {
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "}");

        String contents = "new Other().getXxx";
        int start = contents.indexOf("getXxx"), end = start + "getXxx".length();
        assertDeclaration(contents, start, end, "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField3() {
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "}");

        String contents = "new Other().getXxx";
        int start = contents.indexOf("getXxx"), end = start + "getXxx".length();
        assertDeclaration(contents, start, end, "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField4() {
        createUnit("Other",
            "class Other {\n" +
            "  String getXxx() { null }\n" +
            "}");

        String contents = "new Other().xxx";
        int start = contents.indexOf("xxx"), end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField5() {
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "}");

        String contents = "def o = new Other(); o.xxx";
        int start = contents.indexOf("xxx"), end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField6() {
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  static Other instance() { new O() }\n" +
            "}");

        String contents = "Other.instance().xxx";
        int start = contents.indexOf("xxx"), end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField7() {
        int start, end;
        String contents =
            "class Other {\n" +
            "  private String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  void setXxx(String xxx) { this.xxx = xxx }\n" +
            "  public Other(String xxx) { /**/this.xxx = xxx }\n" +
            "  private def method() { def xyz = xxx; this.xxx }\n" +
            "}";

        start = contents.indexOf("{ xxx }") + 2; end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "xxx", DeclarationKind.FIELD);

        start = contents.indexOf("this.xxx") + 5; end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "xxx", DeclarationKind.FIELD);

        start = contents.indexOf("/**/this.xxx") + 9; end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "xxx", DeclarationKind.FIELD);

        start = contents.lastIndexOf("= xxx") + 2; end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "xxx", DeclarationKind.FIELD);

        start = contents.lastIndexOf("this.xxx") + 5; end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "xxx", DeclarationKind.FIELD);
    }

    @Test
    public void testGetterAndField8() {
        int start, end;
        String contents =
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  void meth() { def closure = { xxx; this.xxx } }\n" +
            "}";

        start = contents.indexOf("xxx;"); end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "getXxx", DeclarationKind.METHOD);

        start = contents.indexOf("this.xxx") + 5; end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField9() {
        int start, end;
        String contents =
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  int compareTo(Other that) { this.xxx <=> that.xxx }\n" +
            "}";

        start = contents.indexOf("this.xxx") + 5; end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "xxx", DeclarationKind.PROPERTY);

        start = contents.indexOf("that.xxx") + 5; end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField10() {
        createUnit("Foo",
            "class Foo {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "}");

        int start, end;
        String contents =
            "class Bar extends Foo {\n" +
            "  String yyy\n" +
            "  def meth() {\n" +
            "    yyy = xxx\n" +
            "    this.yyy = this.xxx\n" +
            "  }\n" +
            "}";

        start = contents.indexOf("yyy ="); end = start + "yyy".length();
        assertDeclaration(contents, start, end, "Bar", "yyy", DeclarationKind.FIELD);

        start = contents.indexOf("xxx"); end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Foo", "getXxx", DeclarationKind.METHOD);

        start = contents.indexOf("this.yyy") + 5; end = start + "yyy".length();
        assertDeclaration(contents, start, end, "Bar", "yyy", DeclarationKind.PROPERTY);

        start = contents.indexOf("this.xxx") + 5; end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Foo", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField11() {
        String contents =
            "class Foo {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  def yyy = new Object() {\n" +
            "    def meth() { Foo.this.xxx }\n" +
            "  }\n" +
            "}";

        int start = contents.lastIndexOf("xxx"), end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Foo", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testLocalAndFieldWithSameName() {
        String contents =
            "class Foo {\n" +
            "  String xxx\n" +
            "  void meth() {\n" +
            "    def xxx\n" +
            "    xxx = ''\n" +
            "  }\n" +
            "}";

        int start = contents.lastIndexOf("xxx"), end = start + "xxx".length();
        assertDeclaration(contents, start, end, "java.lang.Object", "xxx", DeclarationKind.VARIABLE);
    }

    @Test
    public void testParamAndFieldWithSameName() {
        String contents =
            "class Foo {\n" +
            "  String xxx\n" +
            "  void meth(def xxx) {\n" +
            "    xxx = ''\n" +
            "  }\n" +
            "}";

        int start = contents.lastIndexOf("xxx"), end = start + "xxx".length();
        assertDeclaration(contents, start, end, "java.lang.Object", "xxx", DeclarationKind.VARIABLE);
    }

    @Test
    public void testMethodAndFieldWithSameName1() {
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.field('')";
        int start = contents.lastIndexOf("field");
        int end = start + "field".length();
        assertDeclaration(contents, start, end, "A", "field", DeclarationKind.METHOD);
    }

    @Test
    public void testMethodAndFieldWithSameName2() {
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.field('').field";
        int start = contents.lastIndexOf("field");
        int end = start + "field".length();
        assertDeclaration(contents, start, end, "A", "field", DeclarationKind.PROPERTY);
    }

    @Test
    public void testMethodAndFieldWithSameName3() {
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.field";
        int start = contents.lastIndexOf("field");
        int end = start + "field".length();
        assertDeclaration(contents, start, end, "A", "field", DeclarationKind.PROPERTY);
    }

    @Test
    public void testMethodAndFieldWithSameName4() {
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
                "A a = new A()\n" +
                "a.field.field";
        int start = contents.lastIndexOf("field");
        int end = start + "field".length();
        assertUnknownConfidence(contents, start, end, "A", false);
    }

    @Test
    public void testMethodAndFieldWithSameName5() {
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
                "A a = new A()\n" +
                "a.getField()";
        int start = contents.lastIndexOf("getField");
        int end = start + "getField".length();
        assertDeclaration(contents, start, end, "A", "getField", DeclarationKind.METHOD);
    }

    @Test
    public void testMethodAndFieldWithSameName6() {
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.setField('')";
        int start = contents.lastIndexOf("setField");
        int end = start + "setField".length();
        assertDeclaration(contents, start, end, "A", "setField", DeclarationKind.METHOD);
    }

    @Test
    public void testMethodAndFieldWithSameName7() {
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.field = a.field";
        int start = contents.lastIndexOf("field");
        int end = start + "field".length();
        assertDeclaration(contents, start, end, "A", "field", DeclarationKind.PROPERTY);
    }

    @Test
    public void testMethodAndFieldWithSameName8() {
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.field = a.field()";
        int start = contents.lastIndexOf("field");
        int end = start + "field".length();
        assertDeclaration(contents, start, end, "A", "field", DeclarationKind.METHOD);
    }

    @Test
    public void testMethodAndFieldWithSameName9() {
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.field(a.field)";
        int start = contents.lastIndexOf("field");
        int end = start + "field".length();
        assertDeclaration(contents, start, end, "A", "field", DeclarationKind.PROPERTY);
    }

    @Test // GRECLIPSE-1105
    public void testFluentInterfaceWithFieldNameConflicts() {
        createUnit("A",
            "class A {\n" +
            "  String field, other\n" +
            "  String getField() { return this.field }\n" +
            "  A field(String f){this.field = f; return this;}\n" +
            "  A other(String x){this.other = x; return this;}\n" +
            "}");

        // field('f') should be the fluent method, not field property or getField method
        String contents = "new A().field('f').other('x').toString()";
        int start = contents.indexOf("other");
        int end = start + "other".length();
        assertDeclaration(contents, start, end, "A", "other", DeclarationKind.METHOD);
    }
}
