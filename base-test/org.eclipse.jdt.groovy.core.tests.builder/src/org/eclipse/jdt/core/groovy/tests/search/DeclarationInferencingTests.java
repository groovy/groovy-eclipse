/*
 * Copyright 2009-2022 the original author or authors.
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

import static org.junit.Assert.assertEquals;

import org.codehaus.groovy.ast.MethodNode;
import org.junit.Test;

/**
 * Tests that the inferred declaration is correct.
 */
public final class DeclarationInferencingTests extends InferencingTestSuite {

    private void assertKnown(String source, String target, String declaringType, String declarationName, DeclarationKind declarationKind) {
        int offset = source.lastIndexOf(target);
        assertDeclaration(source, offset, offset + target.length(), declaringType, declarationName, declarationKind);
    }

    private void assertUnknown(String source, String target) {
        int offset = source.lastIndexOf(target);
        assertUnknownConfidence(source, offset, offset + target.length());
    }

    //--------------------------------------------------------------------------

    @Test
    public void testCategoryMethod0() {
        String contents = "new Object().@with";
        assertUnknown(contents, "with");
    }

    @Test
    public void testCategoryMethod1() {
        String contents = "new Object().with {\n}";
        assertKnown(contents, "with", "org.codehaus.groovy.runtime.DefaultGroovyMethods", "with", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField1() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "}");
        //@formatter:on

        String contents = "new Other().xxx";
        assertKnown(contents, "xxx", "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField1a() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "}");
        //@formatter:on

        String contents = "new Other().@xxx";
        assertKnown(contents, "xxx", "Other", "xxx", DeclarationKind.FIELD);
    }

    @Test
    public void testGetterAndField2() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "}");
        //@formatter:on

        String contents = "new Other().getXxx()";
        assertKnown(contents, "getXxx", "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField2a() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "}");
        //@formatter:on

        String contents = "new Other().getXxx";
        assertUnknown(contents, "getXxx");
    }

    @Test
    public void testGetterAndField2b() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "}");
        //@formatter:on

        String contents = "new Other().@getXxx";
        assertUnknown(contents, "getXxx");
    }

    @Test
    public void testGetterAndField2c() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "}");
        //@formatter:on

        String contents = "new Other().&getXxx";
        assertKnown(contents, "getXxx", "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField3() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "}");
        //@formatter:on

        String contents = "new Other().getXxx()";
        assertKnown(contents, "getXxx", "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField3a() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "}");
        //@formatter:on

        String contents = "new Other().getXxx";
        assertUnknown(contents, "getXxx");
    }

    @Test
    public void testGetterAndField3b() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "}");
        //@formatter:on

        String contents = "new Other().@getXxx";
        assertUnknown(contents, "getXxx");
    }

    @Test
    public void testGetterAndField3c() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "}");
        //@formatter:on

        String contents = "new Other().&getXxx";
        assertKnown(contents, "getXxx", "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField4() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String getXxx() { null }\n" +
            "}");
        //@formatter:on

        String contents = "new Other().xxx";
        assertKnown(contents, "xxx", "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField4a() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String getXxx() { null }\n" +
            "}");
        //@formatter:on

        String contents = "new Other().@xxx";
        assertUnknown(contents, "xxx");
    }

    @Test
    public void testGetterAndField5() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "}");
        //@formatter:on

        String contents = "def o = new Other(); o.xxx";
        assertKnown(contents, "xxx", "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField5a() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "}");
        //@formatter:on

        String contents = "def o = new Other(); o.@xxx";
        assertKnown(contents, "xxx", "Other", "xxx", DeclarationKind.FIELD);
    }

    @Test
    public void testGetterAndField6() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  static Other instance() { new O() }\n" +
            "}");
        //@formatter:on

        String contents = "Other.instance().xxx";
        assertKnown(contents, "xxx", "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField6a() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  static Other instance() { new O() }\n" +
            "}");
        //@formatter:on

        String contents = "Other.instance().@xxx";
        assertKnown(contents, "xxx", "Other", "xxx", DeclarationKind.FIELD);
    }

    @Test
    public void testGetterAndField7() {
        //@formatter:off
        String contents =
            "class Other {\n" +
            "  private String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  void setXxx(String xxx) { this.xxx = xxx }\n" +
            "  public Other(String xxx) { /**/this.xxx = xxx }\n" +
            "  private def method() { def xyz = xxx; this.xxx }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("xxx", contents.indexOf("getXxx"));
        assertDeclaration(contents, offset, offset + 3, "Other", "xxx", DeclarationKind.FIELD);

        offset = contents.indexOf("this.xxx") + 5;
        assertDeclaration(contents, offset, offset + 3, "Other", "xxx", DeclarationKind.FIELD);

        offset = contents.indexOf("/**/this.xxx") + 9;
        assertDeclaration(contents, offset, offset + 3, "Other", "xxx", DeclarationKind.FIELD);

        offset = contents.lastIndexOf("= xxx") + 2;
        assertDeclaration(contents, offset, offset + 3, "Other", "xxx", DeclarationKind.FIELD);

        offset = contents.lastIndexOf("this.xxx") + 5;
        assertDeclaration(contents, offset, offset + 3, "Other", "xxx", DeclarationKind.FIELD);
    }

    @Test
    public void testGetterAndField7a() {
        //@formatter:off
        String contents =
            "class Other {\n" +
            "  private String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  void setXxx(String xxx) { this.@xxx = xxx }\n" +
            "  public Other(String xxx) { /**/this.@xxx = xxx }\n" +
            "  private def method() { def xyz = xxx; this.@xxx }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("this.@xxx") + 6;
        assertDeclaration(contents, offset, offset + 3, "Other", "xxx", DeclarationKind.FIELD);

        offset = contents.indexOf("/**/this.@xxx") + 10;
        assertDeclaration(contents, offset, offset + 3, "Other", "xxx", DeclarationKind.FIELD);

        offset = contents.lastIndexOf("this.@xxx") + 6;
        assertDeclaration(contents, offset, offset + 3, "Other", "xxx", DeclarationKind.FIELD);
    }

    @Test
    public void testGetterAndField8() {
        //@formatter:off
        String contents =
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  void meth() { def closure = { xxx; this.xxx } }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("xxx;");
        assertDeclaration(contents, offset, offset + 3, "Other", "getXxx", DeclarationKind.METHOD);

        offset = contents.lastIndexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField8a() {
        //@formatter:off
        String contents =
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  void meth() { def closure = { this.@xxx } }\n" +
            "}";
        //@formatter:on

        assertKnown(contents, "xxx", "Other", "xxx", DeclarationKind.FIELD);
    }

    @Test
    public void testGetterAndField9() {
        //@formatter:off
        String contents =
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  int compareTo(Other that) { this.xxx <=> that.xxx }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("this.xxx") + 5;
        assertDeclaration(contents, offset, offset + 3, "Other", "xxx", DeclarationKind.PROPERTY);

        offset = contents.indexOf("that.xxx") + 5;
        assertDeclaration(contents, offset, offset + 3, "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField9a() {
        //@formatter:off
        String contents =
            "class Other {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  int compareTo(Other that) { this.@xxx <=> that.@xxx }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("this.@xxx") + 6;
        assertDeclaration(contents, offset, offset + 3, "Other", "xxx", DeclarationKind.FIELD);

        offset = contents.indexOf("that.@xxx") + 6;
        assertDeclaration(contents, offset, offset + 3, "Other", "xxx", DeclarationKind.FIELD);
    }

    @Test
    public void testGetterAndField10() {
        //@formatter:off
        createUnit("Foo",
            "class Foo {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "}");

        String contents =
            "class Bar extends Foo {\n" +
            "  String yyy\n" +
            "  def meth() {\n" +
            "    yyy = xxx\n" +
            "    this.yyy = this.xxx\n" +
            "  }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("yyy =");
        assertDeclaration(contents, offset, offset + 3, "Bar", "yyy", DeclarationKind.FIELD);

        offset = contents.indexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Foo", "getXxx", DeclarationKind.METHOD);

        offset = contents.lastIndexOf("yyy");
        assertDeclaration(contents, offset, offset + 3, "Bar", "yyy", DeclarationKind.PROPERTY);

        offset = contents.lastIndexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Foo", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField10a() {
        //@formatter:off
        createUnit("Foo",
            "class Foo {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "}");

        String contents =
            "class Bar extends Foo {\n" +
            "  String yyy\n" +
            "  def meth() {\n" +
            "    this.@yyy = this.@xxx\n" +
            "  }\n" +
            "}";
        //@formatter:on

        assertKnown(contents, "yyy", "Bar", "yyy", DeclarationKind.FIELD);

        assertUnknown(contents, "xxx");
    }

    @Test
    public void testGetterAndField10b() {
        //@formatter:off
        createUnit("Foo",
            "class Foo {\n" +
            "  protected String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "}");

        String contents =
            "class Bar extends Foo {\n" +
            "  protected String yyy\n" +
            "  def meth() {\n" +
            "    this.@yyy = super.@xxx\n" +
            "  }\n" +
            "}";
        //@formatter:on

        assertKnown(contents, "yyy", "Bar", "yyy", DeclarationKind.FIELD);

        assertKnown(contents, "xxx", "Foo", "xxx", DeclarationKind.FIELD);
    }

    @Test
    public void testGetterAndField11() {
        //@formatter:off
        String contents =
            "class Foo {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  def yyy = new Object() {\n" +
            "    def meth() { Foo.this.xxx }\n" +
            "  }\n" +
            "}";
        //@formatter:on

        assertKnown(contents, "xxx", "Foo", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField11a() {
        //@formatter:off
        String contents =
            "class Foo {\n" +
            "  String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  def yyy = new Object() {\n" +
            "    def meth() { Foo.this.@xxx }\n" +
            "  }\n" +
            "}";
        //@formatter:on

        assertKnown(contents, "xxx", "Foo", "xxx", DeclarationKind.FIELD);
    }

    @Test
    public void testGetterAndField12() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  private static String xxx\n" +
            "  static String getXxx() { xxx }\n" +
            "}");
        //@formatter:on

        String contents = "Other.xxx";

        assertKnown(contents, "xxx", "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField12a() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  private static String xxx\n" +
            "  static String getXxx() { xxx }\n" +
            "}");
        //@formatter:on

        String contents = "Other.@xxx";

        assertKnown(contents, "xxx", "Other", "xxx", DeclarationKind.FIELD);
    }

    @Test
    public void testGetterAndField13() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  private String xxx\n" +
            "  static String getXxx() { null }\n" +
            "}");
        //@formatter:on

        String contents = "Other.xxx";

        assertKnown(contents, "xxx", "Other", "getXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField13a() {
        //@formatter:off
        createUnit("Other",
            "class Other {\n" +
            "  private String xxx\n" +
            "  static String getXxx() { null }\n" +
            "}");
        //@formatter:on

        String contents = "Other.@xxx";

        assertUnknown(contents, "xxx");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1130
    public void testGetterAndField14() {
        //@formatter:off
        createUnit("Foo",
            "class Foo {\n" +
            "  String getXxx() {\n" +
            "  }\n" +
            "}");

        String contents =
            "class Bar extends Foo {\n" +
            "  private String xxx\n" +
            "  void meth() {\n" +
            "    xxx\n" +
            "    this.xxx\n" +
            "  }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("xxx", contents.indexOf("meth"));
        assertDeclaration(contents, offset, offset + 3, "Bar", "xxx", DeclarationKind.FIELD);

        offset = contents.lastIndexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Bar", "xxx", DeclarationKind.FIELD);
    }

    @Test
    public void testGetterAndField14a() {
        //@formatter:off
        createJavaUnit("Foo",
            "interface Foo {\n" +
            "  default String getXxx() {\n" +
            "    return \"string\";\n" +
            "  }\n" +
            "}");

        String contents =
            "class Bar implements Foo {\n" +
            "  private String xxx\n" +
            "  void meth() {\n" +
            "    xxx\n" +
            "    this.xxx\n" +
            "  }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("xxx", contents.indexOf("meth"));
        assertDeclaration(contents, offset, offset + 3, "Bar", "xxx", DeclarationKind.FIELD);

        offset = contents.lastIndexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Bar", "xxx", DeclarationKind.FIELD);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1130
    public void testGetterAndField15() {
        //@formatter:off
        createUnit("Foo",
            "class Foo {\n" +
            "  boolean isXxx() {\n" +
            "  }\n" +
            "}");

        String contents =
            "class Bar extends Foo {\n" +
            "  private boolean xxx\n" +
            "  void meth() {\n" +
            "    xxx\n" +
            "    this.xxx\n" +
            "  }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("xxx", contents.indexOf("meth"));
        assertDeclaration(contents, offset, offset + 3, "Bar", "xxx", DeclarationKind.FIELD);

        offset = contents.lastIndexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Bar", "xxx", DeclarationKind.FIELD);
    }

    @Test
    public void testGetterAndField15a() {
        //@formatter:off
        createJavaUnit("Foo",
            "interface Foo {\n" +
            "  default boolean isXxx() {\n" +
            "    return false;" +
            "  }\n" +
            "}");

        String contents =
            "class Bar implements Foo {\n" +
            "  private boolean xxx\n" +
            "  void meth() {\n" +
            "    xxx\n" +
            "    this.xxx\n" +
            "  }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("xxx", contents.indexOf("meth"));
        assertDeclaration(contents, offset, offset + 3, "Bar", "xxx", DeclarationKind.FIELD);

        offset = contents.lastIndexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Bar", "xxx", DeclarationKind.FIELD);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1131
    public void testGetterAndField16() {
        //@formatter:off
        String contents =
            "import groovy.transform.PackageScope\n" +
            "class Foo {\n" +
            "  def a\n" +
            "  private b\n" +
            "  public  c\n" +
            "  protected d\n" +
            "  @PackageScope e\n" +
            "  def getA() { 'A' }\n" +
            "  def getB() { 'B' }\n" +
            "  def getC() { 'C' }\n" +
            "  def getD() { 'D' }\n" +
            "  def getE() { 'E' }\n" +
            "}\n" +
            "class Bar extends Foo {\n" +
            "  void meth() {\n" +
            "    super.a\n" +
            "    super.b\n" +
            "    super.c\n" +
            "    super.d\n" +
            "    super.e\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = contents.lastIndexOf("a");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getA", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("b");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getB", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("c");
        assertDeclaration(contents, offset, offset + 1, "Foo", "c", DeclarationKind.FIELD);

            offset = contents.lastIndexOf("d");
        assertDeclaration(contents, offset, offset + 1, "Foo", "d", DeclarationKind.FIELD);

            offset = contents.lastIndexOf("e");
        assertDeclaration(contents, offset, offset + 1, "Foo", "e", DeclarationKind.FIELD);
    }

    @Test
    public void testGetterAndField16a() {
        //@formatter:off
        String contents =
            "import groovy.transform.PackageScope\n" +
            "class Foo {\n" +
            "  def a\n" +
            "  private b\n" +
            "  public  c\n" +
            "  protected d\n" +
            "  @PackageScope e\n" +
            "  def getA() { 'A' }\n" +
            "  def getB() { 'B' }\n" +
            "  def getC() { 'C' }\n" +
            "  def getD() { 'D' }\n" +
            "  def getE() { 'E' }\n" +
            "}\n" +
            "class Bar extends Foo {\n" +
            "  void meth() {\n" +
            "    { ->\n" +
            "      super.a\n" +
            "      super.b\n" +
            "      super.c\n" +
            "      super.d\n" +
            "      super.e\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = contents.lastIndexOf("a");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getA", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("b");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getB", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("c");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getC", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("d");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getD", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("e");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getE", DeclarationKind.METHOD);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1132
    public void testGetterAndField17() {
        //@formatter:off
        String contents =
            "import groovy.transform.PackageScope\n" +
            "class Foo {\n" +
            "  def a\n" +
            "  private b\n" +
            "  public  c\n" +
            "  protected d\n" +
            "  @PackageScope e\n" +
            "  def getA() { 'A' }\n" +
            "  def getB() { 'B' }\n" +
            "  def getC() { 'C' }\n" +
            "  def getD() { 'D' }\n" +
            "  def getE() { 'E' }\n" +
            "  class Bar {\n" +
            "    void meth() {\n" +
            "      a\n" +
            "      b\n" +
            "      c\n" +
            "      d\n" +
            "      e\n" +
            "      this.a\n" +
            "      this.b\n" +
            "      this.c\n" +
            "      this.d\n" +
            "      this.e\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = contents.indexOf("a", contents.indexOf("meth"));
        assertDeclaration(contents, offset, offset + 1, "Foo", "getA", DeclarationKind.METHOD);

            offset = contents.indexOf("b", offset);
        assertDeclaration(contents, offset, offset + 1, "Foo", "getB", DeclarationKind.METHOD);

            offset = contents.indexOf("c", offset);
        assertDeclaration(contents, offset, offset + 1, "Foo", "getC", DeclarationKind.METHOD);

            offset = contents.indexOf("d", offset);
        assertDeclaration(contents, offset, offset + 1, "Foo", "getD", DeclarationKind.METHOD);

            offset = contents.indexOf("e", offset);
        assertDeclaration(contents, offset, offset + 1, "Foo", "getE", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("a");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getA", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("b");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getB", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("c");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getC", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("d");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getD", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("e");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getE", DeclarationKind.METHOD);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1133
    public void testGetterAndField17b() {
        //@formatter:off
        String contents =
            "import groovy.transform.PackageScope\n" +
            "class Foo {\n" +
            "  class Bar {}\n" +
            "  def a\n" +
            "  private b\n" +
            "  public  c\n" +
            "  protected d\n" +
            "  @PackageScope e\n" +
            "  def getA() { 'A' }\n" +
            "  def getB() { 'B' }\n" +
            "  def getC() { 'C' }\n" +
            "  def getD() { 'D' }\n" +
            "  def getE() { 'E' }\n" +
            "}\n" +
            "class Baz extends Foo.Bar {\n" +
            "  Baz() { super(new Foo()) }\n" + // seems odd, but does work
            "  void meth() {\n" +
            "    a\n" +
            "    b\n" +
            "    c\n" +
            "    d\n" +
            "    e\n" +
            "    this.a\n" +
            "    this.b\n" +
            "    this.c\n" +
            "    this.d\n" +
            "    this.e\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = contents.indexOf("a", contents.indexOf("meth"));
        assertDeclaration(contents, offset, offset + 1, "Foo", "getA", DeclarationKind.METHOD);

            offset = contents.indexOf("b", offset);
        assertDeclaration(contents, offset, offset + 1, "Foo", "getB", DeclarationKind.METHOD);

            offset = contents.indexOf("c", offset);
        assertDeclaration(contents, offset, offset + 1, "Foo", "getC", DeclarationKind.METHOD);

            offset = contents.indexOf("d", offset);
        assertDeclaration(contents, offset, offset + 1, "Foo", "getD", DeclarationKind.METHOD);

            offset = contents.indexOf("e", offset);
        assertDeclaration(contents, offset, offset + 1, "Foo", "getE", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("a");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getA", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("b");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getB", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("c");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getC", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("d");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getD", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("e");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getE", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndField17c() {
        //@formatter:off
        String contents =
            "import groovy.transform.PackageScope\n" +
            "class Foo {\n" +
            "  class Bar {}\n" +
            "  def a\n" +
            "  private b\n" +
            "  public  c\n" +
            "  protected d\n" +
            "  @PackageScope e\n" +
            "  def getA() { 'A' }\n" +
            "  def getB() { 'B' }\n" +
            "  def getC() { 'C' }\n" +
            "  def getD() { 'D' }\n" +
            "  def getE() { 'E' }\n" +
            "}\n" +
            "class Baz extends Foo.Bar {\n" +
            "  Baz() { super(new Foo()) }\n" + // seems odd, but does work
            "  void meth() {\n" +
            "    super.a\n" +
            "    super.b\n" +
            "    super.c\n" +
            "    super.d\n" +
            "    super.e\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = contents.lastIndexOf("a");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getA", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("b");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getB", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("c");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getC", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("d");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getD", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("e");
        assertDeclaration(contents, offset, offset + 1, "Foo", "getE", DeclarationKind.METHOD);
    }

    @Test
    public void testSetterAndField1() {
        //@formatter:off
        String contents =
            "class Foo {\n" +
            "  String xxx\n" +
            "  void setXxx(String xxx) { this.xxx = xxx }\n" +
            "  void meth() { def closure = { xxx = ''; this.xxx = '' } }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("xxx", contents.indexOf("meth"));
        assertDeclaration(contents, offset, offset + 3, "Foo", "setXxx", DeclarationKind.METHOD);

        offset = contents.lastIndexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Foo", "setXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testSetterAndField2() {
        //@formatter:off
        createUnit("Foo",
            "class Foo {\n" +
            "  String xxx\n" +
            "  void setXxx(String xxx) { this.xxx = xxx }\n" +
            "}");

        String contents =
            "class Bar extends Foo {\n" +
            "  String yyy\n" +
            "  def meth() {\n" +
            "    xxx = yyy\n" +
            "    this.xxx = this.yyy\n" +
            "  }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Foo", "setXxx", DeclarationKind.METHOD);

        offset = contents.indexOf("yyy", contents.indexOf("meth"));
        assertDeclaration(contents, offset, offset + 3, "Bar", "yyy", DeclarationKind.FIELD);

        offset = contents.lastIndexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Foo", "setXxx", DeclarationKind.METHOD);

        offset = contents.lastIndexOf("yyy");
        assertDeclaration(contents, offset, offset + 3, "Bar", "yyy", DeclarationKind.PROPERTY);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1130
    public void testSetterAndField3() {
        //@formatter:off
        createUnit("Foo",
            "class Foo {\n" +
            "  void setXxx(String xxx) {\n" +
            "  }\n" +
            "}");

        String contents =
            "class Bar extends Foo {\n" +
            "  private String xxx\n" +
            "  void meth() {\n" +
            "    xxx = 'varX'\n" +
            "    this.xxx = 'propX'\n" +
            "  }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("xxx", contents.indexOf("meth"));
        assertDeclaration(contents, offset, offset + 3, "Bar", "xxx", DeclarationKind.FIELD);

        offset = contents.lastIndexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Bar", "xxx", DeclarationKind.FIELD);
    }

    @Test
    public void testSetterAndField3a() {
        //@formatter:off
        createJavaUnit("Foo",
            "interface Foo {\n" +
            "  default void setXxx(String xxx) {\n" +
            "  }\n" +
            "}");

        String contents =
            "class Bar implements Foo {\n" +
            "  private String xxx\n" +
            "  void meth() {\n" +
            "    xxx = 'varX'\n" +
            "    this.xxx = 'propX'\n" +
            "  }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("xxx", contents.indexOf("meth"));
        assertDeclaration(contents, offset, offset + 3, "Bar", "xxx", DeclarationKind.FIELD);

        offset = contents.lastIndexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Bar", "xxx", DeclarationKind.FIELD);
    }

    @Test
    public void testGetterAndSetterOverloads1() {
        //@formatter:off
        createUnit("Foo",
            "class Foo {\n" +
            "  def xxx\n" +
            "}");

        String contents =
            "class Bar extends Foo {\n" +
            "  def getXxx() {\n" +
            "    this.xxx\n" + // recursive!
            "  }\n" +
            "  void setXxx(value) {\n" +
            "    this.xxx = value\n" + // recursive!
            "  }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Bar", "getXxx", DeclarationKind.METHOD);

        offset = contents.lastIndexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Bar", "setXxx", DeclarationKind.METHOD);
    }

    @Test
    public void testGetterAndSetterOverloads2() {
        //@formatter:off
        createUnit("Foo",
            "class Foo {\n" +
            "  def xxx\n" +
            "}");

        String contents =
            "class Bar extends Foo {\n" +
            "  def getXxx() {\n" +
            "    super.xxx\n" +
            "  }\n" +
            "  void setXxx(value) {\n" +
            "    super.xxx = value\n" +
            "  }\n" +
            "}";
        //@formatter:on

        int offset = contents.indexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Foo", "xxx", DeclarationKind.PROPERTY);

        offset = contents.lastIndexOf("xxx");
        assertDeclaration(contents, offset, offset + 3, "Foo", "xxx", DeclarationKind.PROPERTY);
    }

    @Test
    public void testLocalAndFieldWithSameName() {
        //@formatter:off
        String contents =
            "class Foo {\n" +
            "  String xxx\n" +
            "  void meth() {\n" +
            "    def xxx\n" +
            "    xxx = ''\n" +
            "  }\n" +
            "}";
        //@formatter:on

        assertKnown(contents, "xxx", "Foo", "xxx", DeclarationKind.VARIABLE);
    }

    @Test
    public void testParamAndFieldWithSameName() {
        //@formatter:off
        String contents =
            "class Foo {\n" +
            "  String xxx\n" +
            "  void meth(def xxx) {\n" +
            "    xxx = ''\n" +
            "  }\n" +
            "}";
        //@formatter:on

        assertKnown(contents, "xxx", "Foo", "xxx", DeclarationKind.VARIABLE);
    }

    @Test
    public void testMethodAndFieldWithSameName1() {
        //@formatter:off
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.field('')";
        //@formatter:on

        assertKnown(contents, "field", "A", "field", DeclarationKind.PROPERTY);
    }

    @Test
    public void testMethodAndFieldWithSameName2() {
        //@formatter:off
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.field('').field";
        //@formatter:on

        assertUnknown(contents, "field");
    }

    @Test
    public void testMethodAndFieldWithSameName3() {
        //@formatter:off
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.field";
        //@formatter:on

        assertKnown(contents, "field", "A", "field", DeclarationKind.PROPERTY);
    }

    @Test
    public void testMethodAndFieldWithSameName4() {
        //@formatter:off
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.field.field";
        //@formatter:on

        assertUnknown(contents, "field");
    }

    @Test
    public void testMethodAndFieldWithSameName5() {
        //@formatter:off
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.getField()";
        //@formatter:on

        assertKnown(contents, "getField", "A", "getField", DeclarationKind.METHOD);
    }

    @Test
    public void testMethodAndFieldWithSameName6() {
        //@formatter:off
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.setField('')";
        //@formatter:on

        assertKnown(contents, "setField", "A", "setField", DeclarationKind.METHOD);
    }

    @Test
    public void testMethodAndFieldWithSameName7() {
        //@formatter:off
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.field = a.field";
        //@formatter:on

        assertKnown(contents, "field", "A", "field", DeclarationKind.PROPERTY);
    }

    @Test
    public void testMethodAndFieldWithSameName8() {
        //@formatter:off
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.field = a.field()";
        //@formatter:on

        assertKnown(contents, "field", "A", "field", DeclarationKind.METHOD);
    }

    @Test
    public void testMethodAndFieldWithSameName9() {
        //@formatter:off
        createUnit("A",
            "class A {\n" +
            "  String field\n" +
            "  public A field(){}\n" +
            "}");

        String contents =
            "A a = new A()\n" +
            "a.field(a.field)";
        //@formatter:on

        assertKnown(contents, "field", "A", "field", DeclarationKind.PROPERTY);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/967
    public void testJavaInterfaceWithDefaultMethod1() {
        //@formatter:off
        createJavaUnit("Face",
            "public interface Face {\n" +
            "  default void meth() {\n" +
            "  }\n" +
            "}");

        String contents =
            "class Impl implements Face {\n" +
            "}\n" +
            "new Impl().meth()";
        //@formatter:on

        assertKnown(contents, "meth", "Face", "meth", DeclarationKind.METHOD);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/967
    public void testJavaInterfaceWithDefaultMethod2() {
        //@formatter:off
        createJavaUnit("Face",
            "public interface Face {\n" +
            "  default void meth() {\n" +
            "  }\n" +
            "}");
        createJavaUnit("Impl",
            "public class Impl implements Face {\n" +
            "}");
        //@formatter:on

        String contents = "new Impl().meth()";

        assertKnown(contents, "meth", "Face", "meth", DeclarationKind.METHOD);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1047
    public void testJavaInterfaceWithDefaultMethod3() {
        //@formatter:off
        String contents =
            "List<String> list = []\n" +
            "list.toArray { n ->\n" +
            "  new String[n]\n" +
            "}\n";
        //@formatter:on

        int offset = contents.indexOf("toArray");
        try { // Java 11 adds default method toArray(IntFunction) to the Collection interface
            java.util.Collection.class.getDeclaredMethod("toArray", java.util.function.IntFunction.class);
            MethodNode method = assertDeclaration(contents, offset, offset + 7, "java.util.Collection<java.lang.String>", "toArray", DeclarationKind.METHOD);
            assertEquals("java.util.function.IntFunction<T[]>", printTypeName(method.getParameters()[0].getType()));
            assertType(contents, "n", "java.lang.Integer");
        } catch (Exception e) {
            MethodNode method = assertDeclaration(contents, offset, offset + 7, "java.util.List<java.lang.String>", "toArray", DeclarationKind.METHOD);
            assertEquals("T[]", printTypeName(method.getParameters()[0].getType()));
        }
    }

    @Test // GROOVY-10592
    public void testJavaInterfaceWithStaticMethod() {
        //@formatter:off
        createJavaUnit("I",
            "public interface I {\n" +
            "  static C getP() {\n" +
            "  }\n" +
            "  class C {\n" +
            "  }\n" +
            "}");

        String contents =
            "I.getP();" +
            "I.p"; // XXX
        //@formatter:on

        assertKnown(contents, "getP", "I", "getP", DeclarationKind.METHOD);
        assertUnknown(contents, "p");
    }

    @Test // GRECLIPSE-1105
    public void testFluentInterfaceWithFieldNameConflicts() {
        //@formatter:off
        createUnit("A",
            "class A {\n" +
            "  String field, other\n" +
            "  String getField() { return this.field }\n" +
            "  A field(String f){this.field = f; return this;}\n" +
            "  A other(String x){this.other = x; return this;}\n" +
            "}");
        //@formatter:on

        // field('f') should be the fluent method, not field property or getField method
        String contents = "new A().field('f').other('x').toString()";

        assertKnown(contents, "other", "A", "other", DeclarationKind.METHOD);
    }
}
