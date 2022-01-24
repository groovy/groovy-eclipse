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
package org.eclipse.jdt.groovy.core.tests.basic;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;

import org.junit.Ignore;
import org.junit.Test;

public final class AnnotationsTests extends GroovyCompilerTestSuite {

    @Test
    public void testBasicAnnotation() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "@interface A {}",

            "B.groovy",
            "@A class B {}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testStaticFinalField() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "print A.CONST\n",

            "A.groovy",
            "@interface A {\n" +
            "  String CONST = 'value'\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test // GRECLIPSE-697
    public void testInlineDeclaration() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "@B\n" +
            "class A {\n" +
            "  public static void main(String[]argv) {print 'abc';}\n" +
            "}\n" +
            "@interface B {\n" +
            "   String value() default \"\"\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources, "abc");

        checkGCUDeclaration("A.groovy",
            "public @B class A {\n" +
            "  public @groovy.transform.Generated A() {\n" +
            "  }\n" +
            "  public static void main(String... argv) {\n" +
            "  }\n" +
            "}\n" +
            "public @interface B extends java.lang.annotation.Annotation {\n" +
            "  public abstract String value() default \"\";\n" +
            "}\n");
    }

    @Test
    public void testIntLiteralAttributeDefault() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  int value() default 42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testIntLiteralAttributeDefault1a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  int value() default -42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "-42");
    }

    @Test
    public void testIntLiteralAttributeDefault1b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  int value() default +42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testIntLiteralAttributeDefault2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  int value() default (int)42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testIntLiteralAttributeDefault2a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  int value() default (42 as int)\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testLongLiteralAttributeDefault() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  long value() default 42L\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testLongLiteralAttributeDefault1a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  long value() default -42L\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "-42");
    }

    @Test
    public void testLongLiteralAttributeDefault1b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  long value() default +42L\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test @Ignore // https://issues.apache.org/jira/browse/GROOVY-9205
    public void testLongLiteralAttributeDefault2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  long value() default (long)42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test @Ignore // https://issues.apache.org/jira/browse/GROOVY-9205
    public void testLongLiteralAttributeDefault2a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  long value() default (42 as long)\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6025
    public void testShortLiteralAttributeDefault() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  short value() default 42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6025
    public void testShortLiteralAttributeDefault1a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  short value() default -42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "-42");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6025
    public void testShortLiteralAttributeDefault1b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  short value() default +42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6025
    public void testShortLiteralAttributeDefault2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  short value() default (short)42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6025
    public void testShortLiteralAttributeDefault2a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  short value() default (42 as short)\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6025
    public void testByteLiteralAttributeDefault() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  byte value() default 42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6025
    public void testByteLiteralAttributeDefault1a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  byte value() default -42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "-42");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6025
    public void testByteLiteralAttributeDefault1b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  byte value() default +42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6025
    public void testByteLiteralAttributeDefault2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  byte value() default (byte)42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6025
    public void testByteLiteralAttributeDefault2a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  byte value() default (42 as byte)\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6025
    public void testCharLiteralAttributeDefault() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  char value() default 42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "*");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6025
    public void testCharLiteralAttributeDefault2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  char value() default '*'\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "*");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6025
    public void testCharLiteralAttributeDefault3() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  char value() default (char)'*'\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "*");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6025
    public void testCharLiteralAttributeDefaul4() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  char value() default ('*' as char)\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "*");
    }

    @Test
    public void testFloatLiteralAttributeDefault() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  float value() default 42f\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42.0");
    }

    @Test
    public void testFloatLiteralAttributeDefault1a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  float value() default -42f\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "-42.0");
    }

    @Test
    public void testFloatLiteralAttributeDefault1b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  float value() default +42f\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42.0");
    }

    @Test @Ignore // https://issues.apache.org/jira/browse/GROOVY-9205
    public void testFloatLiteralAttributeDefault2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  float value() default (float)42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42.0");
    }

    @Test @Ignore // https://issues.apache.org/jira/browse/GROOVY-9205
    public void testFloatLiteralAttributeDefault2a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  float value() default (42 as float)\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42.0");
    }

    @Test
    public void testDoubleLiteralAttributeDefault() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  double value() default 42d\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42.0");
    }

    @Test
    public void testDoubleLiteralAttributeDefault1a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  double value() default -42d\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "-42.0");
    }

    @Test
    public void testDoubleLiteralAttributeDefault1b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  double value() default +42d\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42.0");
    }

    @Test @Ignore // https://issues.apache.org/jira/browse/GROOVY-9205
    public void testDoubleLiteralAttributeDefault2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  double value() default (double)42\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42.0");
    }

    @Test @Ignore // https://issues.apache.org/jira/browse/GROOVY-9205
    public void testDoubleLiteralAttributeDefault2a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface A {\n" +
            "  double value() default (42 as double)\n" +
            "}\n" +
            "print A.getMethod('value').defaultValue\n",
        };
        //@formatter:on

        runConformTest(sources, "42.0");
    }

    @Test @Ignore // https://issues.apache.org/jira/browse/GROOVY-9206
    public void testCharLiteralAttributeValue() {
        //@formatter:off
        String[] sources = {
            "Separator.java",
            "import java.lang.annotation.*;\n" +
            "@Target(ElementType.FIELD)\n" +
            "@interface Separator {\n" +
            "  char value();\n" +
            "}",

            "Main.groovy",
            "class Main {\n" +
            "  @Separator(';')\n" +
            "  String tokens\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // ArrayIndexOutOfBoundsException in LongLiteral.computeConstant
    public void testLongLiteralAttributeValue() {
        //@formatter:off
        String[] sources = {
            "Min.java",
            "import java.lang.annotation.*;\n" +
            "@Target(ElementType.FIELD)\n" +
            "@interface Min {\n" +
            "  long value();\n" +
            "}",

            "Main.groovy",
            "class Main {\n" +
            "  @Min(0L)\n" +
            "  Integer index\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testLongLiteralAttributeValue2() {
        //@formatter:off
        String[] sources = {
            "Min.java",
            "import java.lang.annotation.*;\n" +
            "@Target(ElementType.FIELD)\n" +
            "@interface Min {\n" +
            "  long value();\n" +
            "}",

            "Main.groovy",
            "class Main {\n" +
            "  @Min(+1L)\n" +
            "  Integer index\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testLongLiteralAttributeValue3() {
        //@formatter:off
        String[] sources = {
            "Min.java",
            "import java.lang.annotation.*;\n" +
            "@Target(ElementType.FIELD)\n" +
            "@interface Min {\n" +
            "  long value();\n" +
            "}",

            "Main.groovy",
            "class Main {\n" +
            "  @Min(-1L)\n" +
            "  Integer index\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testBigIntegerLiteralAttributeValue() {
        //@formatter:off
        String[] sources = {
            "Min.java",
            "import java.lang.annotation.*;\n" +
            "@Target(ElementType.FIELD)\n" +
            "@interface Min {\n" +
            "  long value();\n" +
            "}",

            "Main.groovy",
            "class Main {\n" +
            "  @Min(0G)\n" +
            "  Integer index\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 1)\n" +
            "\tclass Main {\n" +
            "\t^\n" +
            "Type mismatch: cannot convert from BigInteger to long\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 2)\n" +
            "\t@Min(0G)\n" +
            "\t     ^^\n" +
            "Groovy:Attribute 'value' should have type 'java.lang.Long'; but found type 'java.math.BigInteger' in @Min\n" +
            "----------\n");
    }

    @Test
    public void testBigDecimalLiteralAttributeValue() {
        //@formatter:off
        String[] sources = {
            "Min.java",
            "import java.lang.annotation.*;\n" +
            "@Target(ElementType.FIELD)\n" +
            "@interface Min {\n" +
            "  double value();\n" +
            "}",

            "Main.groovy",
            "class Main {\n" +
            "  @Min(1.1G)\n" +
            "  BigDecimal index\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 2)\n" +
            "\t@Min(1.1G)\n" +
            "\t     ^^^^\n" +
            "Groovy:Attribute 'value' should have type 'java.lang.Double'; but found type 'java.math.BigDecimal' in @Min\n" +
            "----------\n");
    }

    @Test
    public void testBooleanLiteralAttributeValue() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@SuppressWarnings(value=\"nls\", unknown=false)\n" +
            "class Main {\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 1)\n" +
            "\t@SuppressWarnings(value=\"nls\", unknown=false)\n" +
            "\t^^^^^^^^^^^^^^^^^\n" +
            "Groovy:'unknown'is not part of the annotation SuppressWarnings in @java.lang.SuppressWarnings\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 1)\n" +
            "\t@SuppressWarnings(value=\"nls\", unknown=false)\n" +
            "\t                               ^^^^^^^^^^^^^\n" +
            "The attribute unknown is undefined for the annotation type SuppressWarnings\n" +
            "----------\n" +
            "3. ERROR in Main.groovy (at line 1)\n" +
            "\t@SuppressWarnings(value=\"nls\", unknown=false)\n" +
            "\t                                       ^^^^^\n" +
            "Groovy:Unexpected type java.lang.Object in @java.lang.SuppressWarnings\n" +
            "----------\n");
    }

    @Test
    public void testClassLiteralAttributeValue1() {
        //@formatter:off
        String[] sources = {
            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target(ElementType.TYPE)\n" +
            "@interface Anno {\n" +
            "  Class<?> value();\n" +
            "}",

            "Main.groovy",
            "@Anno(URL.class)\n" +
            "class Main {\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testClassLiteralAttributeValue2() {
        //@formatter:off
        String[] sources = {
            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target(ElementType.TYPE)\n" +
            "@interface Anno {\n" +
            "  Class<?> value();\n" +
            "}",

            "Main.groovy",
            "@Anno(URL)\n" +
            "class Main {\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testClassLiteralAttributeValue3() {
        //@formatter:off
        String[] sources = {
            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target(ElementType.TYPE)\n" +
            "@interface Anno {\n" +
            "  Class<?> value();\n" +
            "}",

            "Main.groovy",
            "@Anno(\n" +
            "  java.net.URL\n" +
            ")\n" +
            "class Main {\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testClassLiteralAttributeValue4() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "@Category(String[])\n" +
            "class C {\n" +
            "  def m() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        checkGCUDeclaration("C.groovy",
            "public @Category(String[].class) class C {\n" +
            "  public @groovy.transform.Generated C() {\n" +
            "  }\n" +
            "  public static java.lang.Object m(String... $this) {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testClassLiteralAttributeValue5() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "@Category(String[].class)\n" +
            "class C {\n" +
            "  def m() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        checkGCUDeclaration("C.groovy",
            "public @Category(String[].class) class C {\n" +
            "  public @groovy.transform.Generated C() {\n" +
            "  }\n" +
            "  public static java.lang.Object m(String... $this) {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testClassLiteralAttributeValue6() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "@Category(java.lang.String[][].class)\n" +
            "class C {\n" +
            "  def m() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        checkGCUDeclaration("C.groovy",
            "public @Category(java.lang.String[][].class) class C {\n" +
            "  public @groovy.transform.Generated C() {\n" +
            "  }\n" +
            "  public static java.lang.Object m(java.lang.String[]... $this) {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testClosureExpressionAttributeValue() {
        //@formatter:off
        String[] sources = {
            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target(ElementType.TYPE)\n" +
            "@interface Anno {\n" +
            "  Class<?> value();\n" +
            "}",

            "Main.groovy",
            "@Anno(value={ println 'hello' })\n" +
            "class Main {\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1201
    public void testClosureExpressionAttributeValue2() {
        //@formatter:off
        String[] sources = {
            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target(ElementType.TYPE)\n" +
            "@interface Anno {\n" +
            "  Class<? extends Iterable<String>> value();\n" +
            "}",

            "Main.groovy",
            "@Anno(value={ return ['hello'] })\n" +
            "class Main {\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1201
    public void testClosureExpressionAttributeValue3() {
        //@formatter:off
        String[] sources = {
            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target(ElementType.TYPE)\n" +
            "@interface Anno {\n" +
            "  Class<? extends Iterable<String>>[] value();\n" +
            "}",

            "Main.groovy",
            "@Anno(value=[{ return ['hello'] }])\n" +
            "class Main {\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // GRECLIPSE-629
    public void testInlinedStaticFinalAttributeValue() {
        //@formatter:off
        String[] sources = {
            "Const.java",
            "public class Const {\n" +
            "  static final String instance= \"abc\";\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.print(XXX.class.getAnnotation(Anno.class));\n" +
            "  }\n" +
            "}",

            "B.groovy",
            "import java.lang.annotation.*\n" +
            "@Anno(Const.instance)\n" +
            "class XXX {}\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno {\n" +
            "  String value()\n" +
            "}",
        };
        //@formatter:on

        float version = Float.parseFloat(System.getProperty("java.specification.version"));
        runConformTest(sources, version < 9 ? "@Anno(value=abc)" : (version < 13 ? "@Anno(value=\"abc\")" : "@Anno(\"abc\")"));
    }

    @Test
    public void testInlinedStaticFinalAttributeValue2() {
        //@formatter:off
        String[] sources = {
            "Const.java",
            "public class Const {\n" +
            "  public static final String ABC = \"abc\";\n" +
            "}",

            "Script.groovy",
            "@SuppressWarnings(value = Const.ABC)\n" +
            "void meth() {\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in Script.groovy (at line 1)\n" +
            "\t@SuppressWarnings(value = Const.ABC)\n" +
            "\t                          ^^^^^^^^^\n" +
            "Unsupported @SuppressWarnings(\"abc\")\n" +
            "----------\n");
    }

    @Test // GRECLIPSE-830
    public void testDoubleAttributeWithBigDecimalValue() {
        //@formatter:off
        String[] sources = {
            "AnnotationDouble.groovy",
            "import java.lang.annotation.*\n" +
            "@Target(ElementType.FIELD)\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface AnnotationDouble {\n" +
            "  String value()\n" +
            "  double width() default 5.0d\n" +
            "}",

            "AnnotationDoubleTest.groovy",
            "final class AnnotationDoubleTest {\n" +
            "  class FooWithAnnotation {\n" +
            "    @AnnotationDouble(value='test', width=1.0) double value\n" +
            "  }\n" +
            "  def test = new AnnotationDoubleTest()\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in AnnotationDoubleTest.groovy (at line 3)\n" +
            "\t@AnnotationDouble(value='test', width=1.0) double value\n" +
            "\t                                      ^^^\n" +
            "Groovy:Attribute 'width' should have type 'java.lang.Double'; but found type 'java.math.BigDecimal' in @AnnotationDouble\n" +
            "----------\n");
    }

    @Test
    public void testLocalAnnotationConstant1() {
        // there was an error because the variable expression VALUE was not recognized as constant
        // see ResolveVisitor.transformInlineConstants(Expression)
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  public static final String VALUE = 'nls'\n" +
            "  @SuppressWarnings(VALUE)\n" +
            "  def method() {\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testLocalAnnotationConstant2() {
        // there was an error because the variable expression VALUE was not recognized as constant
        // see ResolveVisitor.transformInlineConstants(Expression)
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  public static final String VALUE = 'nls'\n" +
            "  @SuppressWarnings(value = [VALUE])\n" +
            "  def method() {\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testLocalAnnotationConstant3() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@SuppressWarnings(Main.VALUE)\n" +
            "class Main {\n" +
            "  public static final String VALUE = 'nls'\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testLocalAnnotationConstant3a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@SuppressWarnings(VALUE)\n" +
            "class Main {\n" +
            "  public static final String VALUE = 'nls'\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in Main.groovy (at line 1)\n" +
            "\t@SuppressWarnings(VALUE)\n" +
            "\t                  ^^^^^\n" +
            "VALUE cannot be resolved\n" +
            "----------\n");
    }

    @Test
    public void testLocalAnnotationClassLiteral() {
        //@formatter:off
        String[] sources = {
            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "@Target(ElementType.TYPE)\n" +
            "@interface Anno {\n" +
            "  Class value();\n" +
            "}",

            "Main.groovy",
            "@Anno(Main.Inner)\n" +
            "class Main {\n" +
            "  static class Inner {}\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8063
    public void testLocalAnnotationClassLiteral2() {
        //@formatter:off
        String[] sources = {
            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "@Target(ElementType.TYPE)\n" +
            "@interface Anno {\n" +
            "  Class<?> value();\n" +
            "}",

            "Main.groovy",
            "@Anno(Inner)\n" +
            "class Main {\n" +
            "  static class Inner {}\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testImportedAnnotationConstant1() {
        //@formatter:off
        String[] sources = {
            "p/I.java",
            "package p;\n" +
            "public interface I {\n" +
            "  static final String VALUE = \"nls\";\n" +
            "}",

            "Main.groovy",
            "import static p.I.VALUE\n" +
            "class Main {\n" +
            "  @SuppressWarnings(VALUE)\n" +
            "  def method() {\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testImportedAnnotationConstant2() {
        //@formatter:off
        String[] sources = {
            "p/I.java",
            "package p;\n" +
            "public interface I {\n" +
            "  static final String VALUE = \"nls\";\n" +
            "}",

            "Main.groovy",
            "import static p.I.VALUE\n" +
            "class Main {\n" +
            "  @SuppressWarnings(value=[VALUE])\n" +
            "  def method() {\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testAliasedAnnotationConstant1() {
        //@formatter:off
        String[] sources = {
            "p/I.java",
            "package p;\n" +
            "public interface I {\n" +
            "  static final String VALUE = \"xyz\";\n" +
            "}",

            "Main.groovy",
            "import static p.I.VALUE as FOO\n" +
            "class Main {\n" +
            "  @SuppressWarnings(FOO)\n" +
            "  def method() {\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testAliasedAnnotationConstant2() {
        //@formatter:off
        String[] sources = {
            "p/I.java",
            "package p;\n" +
            "public interface I {\n" +
            "  static final String VALUE = \"xyz\";\n" +
            "}",

            "Main.groovy",
            "import static p.I.VALUE as FOO\n" +
            "class Main {\n" +
            "  @SuppressWarnings(value=[FOO])\n" +
            "  def method() {\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testAliasedAnnotationClassLiteral1() {
        //@formatter:off
        String[] sources = {
            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "@Target(ElementType.TYPE)\n" +
            "@interface Anno {\n" +
            "  Class<?> value();\n" +
            "}",

            "Main.groovy",
            "import java.lang.Class as Trash\n" +
            "@Anno(Trash)\n" +
            "class Main {\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testAliasedAnnotationClassLiteral2() {
        //@formatter:off
        String[] sources = {
            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "@Target(ElementType.TYPE)\n" +
            "@interface Anno {\n" +
            "  Class<?> value();\n" +
            "}",

            "Main.groovy",
            "import java.util.regex.Pattern\n" +
            "import java.util.regex.Pattern as Regex\n" +
            "@Anno(Regex)\n" +
            "class Main {\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testAliasedAnnotationClassLiteral3() {
        //@formatter:off
        String[] sources = {
            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "@Target(ElementType.TYPE)\n" +
            "@interface Anno {\n" +
            "  Class<?> value();\n" +
            "}",

            "p/Outer.java",
            "package p;\n" +
            "public class Outer { public static class Inner {} }",

            "Main.groovy",
            "import p.Outer as Retuo\n" +
            "@Anno(Retuo.Inner)\n" +
            "class Main {\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testMissingAnnotationAttributeValue1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@SuppressWarnings\n" +
            "class Main {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 1)\n" +
            "\t@SuppressWarnings\n" +
            "\t^^^^^^^^^^^^^^^^^\n" +
            "Groovy:No explicit/default value found for annotation attribute 'value' in @java.lang.SuppressWarnings\n" +
            "----------\n");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/886
    public void testMissingAnnotationAttributeValue2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  void meth() {\n" +
            "    @SuppressWarnings\n" +
            "    def local = 'unused'\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 3)\n" +
            "\t@SuppressWarnings\n" +
            "\t^^^^^^^^^^^^^^^^^\n" +
            "Groovy:No explicit/default value found for annotation attribute 'value' in @java.lang.SuppressWarnings\n" +
            "----------\n");
    }

    @Test
    public void testTargetMetaAnnotation() {
        //@formatter:off
        String[] sources = {
            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "@Target(ElementType.METHOD)\n" +
            "@interface Anno {\n" +
            "}\n",

            "Main.groovy",
            "@Anno class Main {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 1)\n" +
            "\t@Anno class Main {}\n" +
            "\t^^^^^\n" +
            "Groovy:Annotation @Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    @Test
    public void testTypeLevelAnnotations() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno\n" +
            "public class X {\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno {}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");

        checkGCUDeclaration("X.groovy", "public @Anno class X {");

        checkDisassemblyFor("p/X.class",
            "@p.Anno\n" +
            "public class p.X implements groovy.lang.GroovyObject {\n");
    }

    @Test
    public void testFieldLevelAnnotations() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno\n" +
            "  String s\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno {}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");

        checkGCUDeclaration("X.groovy", "private @Anno String s;");

        checkDisassemblyFor("p/X.class",
            "  @p.Anno\n" +
            "  private java.lang.String s;\n");
    }

    @Test
    public void testFieldLevelAnnotations_classRetention() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno\n" +
            "  String s\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno {}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");

        String expectedOutput =
            "  @p.Anno\n" +
            "  private java.lang.String s;\n";
        checkDisassemblyFor("p/X.class", expectedOutput);
    }

    @Test
    public void testFieldLevelAnnotations_sourceRetention() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno\n" +
            "  String s\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.SOURCE)\n" +
            "@interface Anno {}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");

        String expectedOutput =
            "Ljava/lang/String;\n" +
            "  private java.lang.String s;\n";
        checkDisassemblyFor("p/X.class", expectedOutput);
    }

    @Test
    public void testFieldLevelAnnotations_defaultRetention() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno\n" +
            "  String s\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "@interface Anno {}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");

        String expectedOutput = "  @p.Anno\n" +
            "  private java.lang.String s;\n";
        checkDisassemblyFor("p/X.class", expectedOutput);
    }

    @Test
    public void testFieldLevelAnnotations_delegate() {
        //@formatter:off
        String[] sources = {
            "Bar.groovy",
            "class Bar {\n" +
            " public void m() {\n" +
            "Object o = new Other().me;\n" +
            "}}",

            "Other.groovy",
            "public class Other {\n" +
            "  public @Anno Date me\n" +
            "}\n",

            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno {}\n",
        };
        //@formatter:on

        runConformTest(sources);

        checkGCUDeclaration("Other.groovy",
            "public class Other {\n" +
            "  public @Anno Date me;\n" +
            "  public @groovy.transform.Generated Other() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testMethodLevelAnnotations() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno {}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");

        checkGCUDeclaration("X.groovy", "public static @Anno void main(String... argv) {");

        checkDisassemblyFor("p/X.class",
            "  @p.Anno\n" +
            "  public static void main(java.lang.String... argv);\n");
    }

    @Test
    public void testConstructorLevelAnnotations1() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno\n" +
            "  X(String s) {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno {}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");

        checkGCUDeclaration("X.groovy", "public @Anno X(String s) {");

        checkDisassemblyFor("p/X.class",
            "  @p.Anno\n" +
            "  public X(java.lang.String s);\n");
    }

    @Test
    public void testConstructorLevelAnnotations2() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "class C {\n" +
            "  @Deprecated\n" +
            "  C() {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        checkGCUDeclaration("C.groovy",
            "package p;\n" +
            "public class C {\n" +
            "  public @Deprecated C() {\n" +
            "  }\n" +
            "}\n"
        );

        checkDisassemblyFor("p/C.class",
            "  @java.lang.Deprecated\n" +
            "  public C();\n");
    }

    @Test
    public void testAnnotationsOnDefaultArgumentMethod() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno {}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");

        checkGCUDeclaration("X.groovy", "public @Anno void foo() {");

        checkGCUDeclaration("X.groovy", "public @Anno void foo(String s) {");
    }

    @Test
    public void testTypeLevelAnnotations_SingleMember1() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(Target.class)\n" +
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Target.java",
            "package p;\n" +
            "class Target {}",
        };
        //@formatter:on

        runConformTest(sources, "success");

        checkGCUDeclaration("X.groovy", "public @Anno(Target.class) class X");
    }

    @Test
    public void testTypeLevelAnnotations_SingleMember2() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Target.class)\n" +
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Target.java",
            "package p;\n" +
            "class Target {}",
        };
        //@formatter:on

        runConformTest(sources, "success");

        checkGCUDeclaration("X.groovy", "public @Anno(p.Target.class) class X");
    }

    @Test
    public void testMethodLevelAnnotations_SingleMember1() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno(Target.class)\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Target.java",
            "package p;\n" +
            "class Target {}",
        };
        //@formatter:on

        runConformTest(sources, "success");

        checkGCUDeclaration("X.groovy", "public @Anno(Target.class) void foo(String s) {");
    }

    @Test
    public void testMethodLevelAnnotations_SingleMember2() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno(p.Target.class)\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Target.java",
            "package p;\n" +
            "class Target {}",
        };
        //@formatter:on

        runConformTest(sources, "success");

        checkGCUDeclaration("X.groovy", "public @Anno(p.Target.class) void foo(String s) {");
    }

    @Test
    public void testFieldLevelAnnotations_SingleMember1() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno(Target.class)\n" +
            "  public int foo = 5\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Target.java",
            "package p;\n" +
            "class Target {}",
        };
        //@formatter:on

        runConformTest(sources, "success");

        checkGCUDeclaration("X.groovy", "public @Anno(Target.class) int foo");
    }

    @Test
    public void testTypeLevelAnnotations_SelfReferential1() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(X.Y)\n" +
            "class X {\n" +
            "  public static final String Y = ''\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { String value(); }\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeLevelAnnotations_SelfReferential2() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(X.Y)\n" +
            "trait X {\n" +
            "  public static final String Y = ''\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { String value(); }\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/619
    public void testTypeLevelAnnotations_SelfReferential3() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(X.Y)\n" +
            "interface X {\n" +
            "  public static final String Y = ''\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { String value(); }\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testAnnotations_singleMemberAnnotationField1() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno(p.Target.class)\n" +
            "  public int foo = 5\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Target.java",
            "package p;\n" +
            "class Target {}",
        };
        //@formatter:on

        runConformTest(sources, "success");

        checkGCUDeclaration("X.groovy", "public @Anno(p.Target.class) int foo");
    }

    @Test
    public void testAnnotations_singleMemberAnnotationFailure1() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno(IDontExist.class)\n" +
            "  public int foo = 5\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { Class<?> value(); }\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 3)\n" +
            "\t@Anno(IDontExist.class)\n" +
            "\t      ^^^^^^^^^^\n" +
            "Groovy:unable to find class 'IDontExist.class' for annotation attribute constant\n" +
            "----------\n" +
            "2. ERROR in p\\X.groovy (at line 3)\n" +
            "\t@Anno(IDontExist.class)\n" +
            "\t      ^^^^^^^^^^^^^^^^\n" +
            "Groovy:Only classes and closures can be used for attribute 'value' in @p.Anno\n" +
            "----------\n");
    }

    @Test
    public void testAnnotations_singleMemberAnnotationFailure2() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @SuppressWarnings(DoesNot.EXIST)\n" +
            "  static void main(String... args) {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 3)\n" +
            "\t@SuppressWarnings(DoesNot.EXIST)\n" +
            "\t                  ^^^^^^^\n" +
            "DoesNot cannot be resolved\n" +
            "----------\n" +
            "2. ERROR in p\\X.groovy (at line 3)\n" +
            "\t@SuppressWarnings(DoesNot.EXIST)\n" +
            "\t                  ^^^^^^^\n" +
            "Groovy:unable to find class 'DoesNot.EXIST' for annotation attribute constant\n" +
            "----------\n" +
            "3. ERROR in p\\X.groovy (at line 3)\n" +
            "\t@SuppressWarnings(DoesNot.EXIST)\n" +
            "\t                  ^^^^^^^\n" +
            "Groovy:Apparent variable 'DoesNot' was found in a static scope but doesn't refer to a local variable, static field or class.\n" +
            "----------\n" +
            "4. ERROR in p\\X.groovy (at line 3)\n" +
            "\t@SuppressWarnings(DoesNot.EXIST)\n" +
            "\t                  ^^^^^^^^^^^^^\n" +
            "Groovy:Expected 'DoesNot.EXIST' to be an inline constant of type java.lang.String not a property expression in @java.lang.SuppressWarnings\n" +
            "----------\n" +
            // this error was associated with line -1
            "5. ERROR in p\\X.groovy (at line 3)\n" +
            "\t@SuppressWarnings(DoesNot.EXIST)\n" +
            "\t                  ^^^^^^^^^^^^^\n" +
            "Groovy:Attribute 'value' should have type 'java.lang.String'; but found type 'java.lang.Object' in @java.lang.SuppressWarnings\n" +
            "----------\n");
    }

    @Test // All types in groovy with TYPE specified for Target and obeyed
    public void testAnnotationsTargetType01() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n" +
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.groovy",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target([ElementType.TYPE])\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.groovy",
            "package p;\n" +
            "class Foo {}",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test // All groovy but annotation can only be put on METHOD - that is violated by class X
    public void testAnnotationsTargetType02() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n" +
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.groovy",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target([ElementType.METHOD])\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.groovy",
            "package p;\n" +
            "class Foo {}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t^^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    @Test // All groovy but annotation can only be put on FIELD - that is violated by class X
    public void testAnnotationsTargetType03() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n" +
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.groovy",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target([ElementType.FIELD])\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.groovy",
            "package p;\n" +
            "class Foo {}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t^^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    @Test // All groovy but annotation can only be put on FIELD or METHOD - that is violated by class X
    public void testAnnotationsTargetType04() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n" +
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.groovy",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target([ElementType.FIELD,ElementType.METHOD])\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.groovy",
            "package p;\n" +
            "class Foo {}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t^^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    @Test // Two types in groovy, one in java with TYPE specified for Target and obeyed
    public void testAnnotationsTargetType05() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n" +
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.groovy",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target([ElementType.TYPE])\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.java",
            "package p;\n" +
            "class Foo {}",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test // 2 groovy, 1 java but annotation can only be put on METHOD - that is violated by class X
    public void testAnnotationsTargetType06() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n" +
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.groovy",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target([ElementType.METHOD])\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.java",
            "package p;\n" +
            "class Foo {}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t^^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    @Test // 2 groovy, 1 java but annotation can only be put on FIELD - that is violated by class X
    public void testAnnotationsTargetType07() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n" +
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.groovy",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target([ElementType.FIELD])\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.groovy",
            "package p;\n" +
            "class Foo {}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t^^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    @Test // 2 groovy, 1 java but annotation can only be put on FIELD or METHOD - that is violated by class X
    public void testAnnotationsTargetType08() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n" +
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.groovy",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target([ElementType.FIELD,ElementType.METHOD])\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.java",
            "package p;\n" +
            "class Foo {}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t^^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    @Test // 1 groovy, 2 java with TYPE specified for Target and obeyed
    public void testAnnotationsTargetType09() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n" +
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target({ElementType.TYPE})\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.java",
            "package p;\n" +
            "class Foo {}",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test // 1 groovy, 2 java but annotation can only be put on METHOD - that is violated by class X
    public void testAnnotationsTargetType10() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n" +
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target({ElementType.METHOD})\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.java",
            "package p;\n" +
            "class Foo {}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t^^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    @Test // 1 groovy, 2 java but annotation can only be put on FIELD - that is violated by class X
    public void testAnnotationsTargetType11() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n" +
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target({ElementType.FIELD})\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.groovy",
            "package p;\n" +
            "class Foo {}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t^^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    @Test // 1 groovy, 2 java but annotation can only be put on FIELD or METHOD - that is violated by class X
    public void testAnnotationsTargetType12() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n" +
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target({ElementType.FIELD,ElementType.METHOD})\n" +
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.java",
            "package p;\n" +
            "class Foo {}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t^^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    @Test
    public void testAnnotationsTargetType13() {
        //@formatter:off
        String[] sources = {
            "p/Main.groovy",
            "package p;\n" +
            "public class Main {\n" +
            "  void m(@Anno('x') String s) {}\n" +
            "  static main(args) {\n" +
            "    print getMethod('m', String).annotatedParameterTypes[0].annotations[0].value()\n" +
            "  }\n" +
            "}\n",

            "p/Anno.groovy",
            "package p\n" +
            "import java.lang.annotation.*\n" +
            "@Target(ElementType.TYPE_USE)\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { String value(); }\n",
        };
        //@formatter:on

        if (isAtLeastGroovy(40)) {
            runConformTest(sources, "x");
        } else {
            runNegativeTest(sources,
                "----------\n" +
                "1. ERROR in p\\Main.groovy (at line 3)\n" +
                "\tvoid m(@Anno('x') String s) {}\n" +
                "\t       ^^^^^\n" +
                "Groovy:Annotation @p.Anno is not allowed on element PARAMETER\n" +
                "----------\n");
        }
    }
}
