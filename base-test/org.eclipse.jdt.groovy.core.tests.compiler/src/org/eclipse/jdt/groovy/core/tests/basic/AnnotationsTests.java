/*
 * Copyright 2009-2016 the original author or authors.
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
package org.eclipse.jdt.groovy.core.tests.basic;

import junit.framework.Test;

import org.eclipse.jdt.core.tests.util.GroovyUtils;

public final class AnnotationsTests extends AbstractGroovyRegressionTest {

    public static Test suite() {
        return buildMinimalComplianceTestSuite(AnnotationsTests.class, F_1_5);
    }

    public AnnotationsTests(String name) {
        super(name);
    }

    public void testGroovyAnnotation() {
        String[] sources = {
            "Foo.groovy",
            "@interface A {}",

            "Bar.groovy",
            "@A class Bar {}"
        };

        runConformTest(sources);
    }

    // GRECLIPSE-697
    public void testInlineDeclaration() {
        String[] sources = {
            "A.groovy",
            "@B\n"+
            "class A { \n"+
            "  public static void main(String[]argv) {print 'abc';}\n"+
            "}\n"+
            "@interface B {\n"+
            "   String value() default \"\"\n"+
            "}"
        };

        runConformTest(sources, "abc");
    }

    public void testLongLiteral() {
        // ArrayIndexOutOfBoundsException in LongLiteral.computeConstant
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
            "}"
        };

        runConformTest(sources);
    }

    public void testBigIntegerLiteral() {
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
            "}"
        };

        // there should not be an error from the Java model -- org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration.UnitPopulator.createConstantExpression(ConstantExpression)
        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 2)\n" +
            "\t@Min(0G)\n" +
            "\t     ^" + (GroovyUtils.isAtLeastGroovy(20) ? "^" : "") + "\n" +
            "Groovy:Attribute 'value' should have type 'java.lang.Long'; but found type 'java.math.BigInteger' in @Min\n" +
            "----------\n");
    }

    public void testBigDecimalLiteral() {
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
            "}"
        };

        // there should not be an error from the Java model -- org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration.UnitPopulator.createConstantExpression(ConstantExpression)
        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 2)\n" +
            "\t@Min(1.1G)\n" +
            "\t     ^" + (GroovyUtils.isAtLeastGroovy(20) ? "^^^" : "") + "\n" +
            "Groovy:Attribute 'value' should have type 'java.lang.Double'; but found type 'java.math.BigDecimal' in @Min\n" +
            "----------\n");
    }

    public void testClassAnnotationValue() {
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
            "}"
        };

        runConformTest(sources);
    }

    public void testClassAnnotationValue2() {
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
            "}"
        };

        runConformTest(sources);
    }

    public void testClosureAnnotationValue() {
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
            "}"
        };

        runConformTest(sources);
    }

    // GRECLIPSE-629
    public void testConstAnnotationValue() {
        String[] sources = {
            "Const.java",
            "public class Const {\n" +
            "static final String instance= \"abc\";\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println(XXX.class.getAnnotation(Anno.class));\n" +
            "  }\n" +
            "}",

            "B.groovy",
            "import java.lang.annotation.*\n" +
            "@Anno(Const.instance)\n" +
            "class XXX {}\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno {\n" +
            "  String value()\n" +
            "}"
        };

        runConformTest(sources, "@Anno(value=abc)");
    }

    // GRECLIPSE-830
    public void testDoubleAttributeWithBigDecimalValue() {
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
            "class AnnotationDoubleTest {\n" +
            "  class FooWithAnnotation {\n" +
            "    @AnnotationDouble(value='test', width=1.0) double value\n" +
            "  }\n" +
            "  def test = new AnnotationDoubleTest()\n" +
            "}"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in AnnotationDoubleTest.groovy (at line 3)\n" +
            "\t@AnnotationDouble(value='test', width=1.0) double value\n" +
            "\t                                      ^" + (GroovyUtils.isAtLeastGroovy(20) ? "^^" : "") + "\n" +
            "Groovy:Attribute 'width' should have type 'java.lang.Double'; but found type 'java.math.BigDecimal' in @AnnotationDouble\n" +
            "----------\n");
    }

    public void testLocalAnnotationConstant1() {
        // there was an error because the variable expression VALUE was not recognized as constant
        // see ResolveVisitor.transformInlineConstants(Expression)
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  public static final String VALUE = 'nls'\n" +
            "  @SuppressWarnings(VALUE)\n" +
            "  def method() {\n" +
            "  }\n" +
            "}"
        };

        runConformTest(sources);
    }

    public void testLocalAnnotationConstant2() {
        // there was an error because the variable expression VALUE was not recognized as constant
        // see ResolveVisitor.transformInlineConstants(Expression)
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  public static final String VALUE = 'nls'\n" +
            "  @SuppressWarnings(value = [VALUE])\n" +
            "  def method() {\n" +
            "  }\n" +
            "}"
        };

        runConformTest(sources);
    }

    public void testImportedAnnotationConstant1() {
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
            "}"
        };

        runConformTest(sources);
    }

    public void testImportedAnnotationConstant2() {
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
            "}"
        };

        runConformTest(sources);
    }

    public void testAliasedAnnotationConstant1() {
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
            "}"
        };

        runConformTest(sources);
    }

    public void testAliasedAnnotationConstant2() {
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
            "}"
        };

        runConformTest(sources);
    }

    public void testTargetMetaAnnotation() {
        String[] sources = {
            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "@Target(ElementType.METHOD)\n" +
            "@interface Anno {\n" +
            "}",

            "Bar.groovy",
            "@Anno class Bar {}"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Bar.groovy (at line 1)\n" +
            "\t@Anno class Bar {}\n" +
            "\t ^^^^\n" +
            "Groovy:Annotation @Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    public void testTypeLevelAnnotations01() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno\n"+
            "public class X {\n" +
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno {}\n"
        };

        runConformTest(sources, "success");

        checkGCUDeclaration("X.groovy", "public @Anno class X {");

        checkDisassemblyFor("p/X.class", "@p.Anno\n" +
            "public class p.X implements groovy.lang.GroovyObject {\n");
    }

    public void testMethodLevelAnnotations() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno {}\n"
        };

        runConformTest(sources, "success");

        String expectedOutput = "public static @Anno void main(public String... argv) {";
        checkGCUDeclaration("X.groovy",expectedOutput);

        expectedOutput =
            //"  // Method descriptor #46 ([Ljava/lang/String;)V\n" +
            "  // Stack: 3, Locals: 2\n" +
            "  @p.Anno\n" +
            "  public static void main(java.lang.String... argv);\n";
        checkDisassemblyFor("p/X.class", expectedOutput);
    }

    public void testFieldLevelAnnotations01() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno\n"+
            "  String s\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno {}\n"
        };

        runConformTest(sources, "success");

        String expectedOutput = "private @Anno String s;";
        checkGCUDeclaration("X.groovy",expectedOutput);

        expectedOutput =
            //"  // Field descriptor #11 Ljava/lang/String;\n" +
            "  @p.Anno\n" +
            "  private java.lang.String s;\n";
        checkDisassemblyFor("p/X.class", expectedOutput);
    }

    public void testFieldLevelAnnotations_classRetention() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno\n"+
            "  String s\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno {}\n"
        };

        runConformTest(sources, "success");

        String expectedOutput =
            //"  // Field descriptor #11 Ljava/lang/String;\n" +
            "  @p.Anno\n" +
            "  private java.lang.String s;\n";
        checkDisassemblyFor("p/X.class", expectedOutput);
    }

    public void testFieldLevelAnnotations_sourceRetention() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno\n"+
            "  String s\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.SOURCE)\n"+
            "@interface Anno {}\n"
        };

        runConformTest(sources, "success");

        String expectedOutput =
            //"  // Field descriptor #9"+  descriptor number varies across compilers (1.6/1.7)
            "Ljava/lang/String;\n" +
            "  private java.lang.String s;\n";
        checkDisassemblyFor("p/X.class", expectedOutput);
    }

    public void testFieldLevelAnnotations_defaultRetention() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno\n"+
            "  String s\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "@interface Anno {}\n"
        };

        runConformTest(sources, "success");

        String expectedOutput =
//          "  // Field descriptor #9 "+ // descriptor number varies across compiler versions
            "Ljava/lang/String;\n" +
            "  private java.lang.String s;\n";
        checkDisassemblyFor("p/X.class", expectedOutput);
    }

    public void testFieldLevelAnnotations_delegate() {
        String[] sources = {
            "Bar.groovy",
            "class Bar {\n"+
            " public void m() {\n"+
            "Object o = new Other().me;\n"+
            "}}",

            "Other.groovy",
            "public class Other {\n" +
            "  public @Anno Date me\n"+
            "}\n",

            "Anno.java",
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno {}\n"
        };

        runConformTest(sources);

        checkGCUDeclaration("Other.groovy",
                "public class Other {\n" +
                "  public @Anno Date me;\n" +
                "  public Other() {\n" +
                "  }\n" +
                "}\n");
    }

    public void testConstructorLevelAnnotations01() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno\n"+
            "  X(String s) {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno {}\n"
        };

        runConformTest(sources, "success");

        String expectedOutput = "public @Anno X(public String s) {";
        checkGCUDeclaration("X.groovy", expectedOutput);

        expectedOutput =
            //"  // Method descriptor #18 (Ljava/lang/String;)V\n" +
            (GroovyUtils.GROOVY_LEVEL<18?
            "  // Stack: 3, Locals: 3\n":
            "  // Stack: 2, Locals: 4\n")+
            "  @p.Anno\n" +
            "  public X(java.lang.String s);\n";
        checkDisassemblyFor("p/X.class", expectedOutput);
    }

    public void testAnnotations04_defaultParamMethods() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno\n"+
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno {}\n"
        };

        runConformTest(sources, "success");

        String expectedOutput = "public @Anno void foo() {";
        checkGCUDeclaration("X.groovy",expectedOutput);

        expectedOutput = "public @Anno void foo(public String s) {";
        checkGCUDeclaration("X.groovy",expectedOutput);
    }

    public void testTypeLevelAnnotations_SingleMember() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(Target.class)\n"+
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Target.java",
            "package p;\n"+
            "class Target { }"
        };

        runConformTest(sources, "success");

        String expectedOutput = "public @Anno(Target.class) class X";
        checkGCUDeclaration("X.groovy",expectedOutput);
    }

    public void testTypeLevelAnnotations_SingleMember02() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Target.class)\n"+
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Target.java",
            "package p;\n"+
            "class Target { }"
        };

        runConformTest(sources, "success");

        String expectedOutput = "public @Anno(p.Target.class) class X";
        checkGCUDeclaration("X.groovy", expectedOutput);
    }

    public void testMethodLevelAnnotations_SingleMember() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno(Target.class)\n"+
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Target.java",
            "package p;\n"+
            "class Target { }"
        };

        runConformTest(sources, "success");

        String expectedOutput = "public @Anno(Target.class) void foo(public String s) {";
        checkGCUDeclaration("X.groovy",expectedOutput);
    }

    public void testMethodLevelAnnotations_SingleMember02() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno(p.Target.class)\n"+
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Target.java",
            "package p;\n"+
            "class Target { }"
        };

        runConformTest(sources, "success");

        String expectedOutput = "public @Anno(p.Target.class) void foo(public String s) {";
        checkGCUDeclaration("X.groovy",expectedOutput);
    }

    public void testFieldLevelAnnotations_SingleMember() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno(Target.class)\n"+
            "  public int foo = 5\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Target.java",
            "package p;\n"+
            "class Target { }"
        };

        runConformTest(sources, "success");

        String expectedOutput = "public @Anno(Target.class) int foo";
        checkGCUDeclaration("X.groovy",expectedOutput);
    }

    public void testAnnotations_singleMemberAnnotationField() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno(p.Target.class)\n"+
            "  public int foo = 5\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Target.java",
            "package p;\n"+
            "class Target { }"
        };

        runConformTest(sources, "success");

        String expectedOutput = "public @Anno(p.Target.class) int foo";
        checkGCUDeclaration("X.groovy",expectedOutput);
    }

    public void testAnnotations_singleMemberAnnotationFailure() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno(IDontExist.class)\n"+
            "  public int foo = 5\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno { Class<?> value(); }\n"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 3)\n" +
            "\t@Anno(IDontExist.class)\n" +
            "\t      ^^^^^^^^^^\n" +
            "Groovy:unable to find class 'IDontExist.class' for annotation attribute constant\n" +
            "----------\n" +
            "2. ERROR in p\\X.groovy (at line 3)\n" +
            "\t@Anno(IDontExist.class)\n" +
            "\t      ^"+(GroovyUtils.isAtLeastGroovy(20)?"^^^^^^^^^^^^^^^":"")+"\n" +
            "Groovy:Only classes and closures can be used for attribute 'value' in @p.Anno\n" +
            "----------\n");
    }

    public void testAnnotationCollector() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return;

        String[] sources = {
            "Type.groovy",
            "@Alias(includes='id')\n"+
            "class Type {\n" +
            "  String id\n" +
            "  String hidden = '456'\n" +
            "  \n" +
            "  static void main(String[] args) {\n" +
            "    print(new Type(id:'123'))\n" +
            "  }\n" +
            "}",

            "Alias.groovy",
            "import groovy.transform.*\n" +
            "@AnnotationCollector\n" +
            "@EqualsAndHashCode\n" +
            "@ToString\n" +
            "@interface Alias { }",
        };

        runConformTest(sources);
    }

    public void testAnnotationCollector2() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return;

        String[] sources = {
            "Type.groovy",
            "@Alias(includes='id')\n"+
            "class Type {\n" +
            "  String id\n" +
            "  String hidden = '456'\n" +
            "  \n" +
            "  static void main(String[] args) {\n" +
            "    print(new Type(id:'123'))\n" +
            "  }\n" +
            "}",

            "Alias.groovy",
            "import groovy.transform.*\n" +
            "@AnnotationCollector([EqualsAndHashCode, ToString])\n" +
            "@interface Alias { }",
        };

        runConformTest(sources, "Type(123)");
    }

    // All types in groovy with TYPE specified for Target and obeyed
    public void testAnnotationsTargetType() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n"+
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.groovy",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@Target([ElementType.TYPE])\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.groovy",
            "package p;\n"+
            "class Foo { }"
        };

        runConformTest(sources, "success");
    }

    // All groovy but annotation can only be put on METHOD - that is violated by class X
    public void testAnnotationsTargetType02() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n"+
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.groovy",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@Target([ElementType.METHOD])\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.groovy",
            "package p;\n"+
            "class Foo { }"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t ^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    // All groovy but annotation can only be put on FIELD - that is violated by class X
    public void testAnnotationsTargetType03() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n"+
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.groovy",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@Target([ElementType.FIELD])\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.groovy",
            "package p;\n"+
            "class Foo { }"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t ^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    // All groovy but annotation can only be put on FIELD or METHOD - that is violated by class X
    public void testAnnotationsTargetType04() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n"+
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.groovy",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@Target([ElementType.FIELD,ElementType.METHOD])\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.groovy",
            "package p;\n"+
            "class Foo { }"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t ^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    // Two types in groovy, one in java with TYPE specified for Target and obeyed
    public void testAnnotationsTargetType05() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n"+
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.groovy",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@Target([ElementType.TYPE])\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.java",
            "package p;\n"+
            "class Foo { }"
        };

        runConformTest(sources, "success");
    }

    // 2 groovy, 1 java but annotation can only be put on METHOD - that is violated by class X
    public void testAnnotationsTargetType06() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n"+
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.groovy",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@Target([ElementType.METHOD])\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.java",
            "package p;\n"+
            "class Foo { }"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t ^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    // 2 groovy, 1 java but annotation can only be put on FIELD - that is violated by class X
    public void testAnnotationsTargetType07() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n"+
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.groovy",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@Target([ElementType.FIELD])\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.groovy",
            "package p;\n"+
            "class Foo { }"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t ^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    // 2 groovy, 1 java but annotation can only be put on FIELD or METHOD - that is violated by class X
    public void testAnnotationsTargetType08() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n"+
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.groovy",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@Target([ElementType.FIELD,ElementType.METHOD])\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.java",
            "package p;\n"+
            "class Foo { }"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t ^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    // 1 groovy, 2 java with TYPE specified for Target and obeyed
    public void testAnnotationsTargetType09() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n"+
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@Target({ElementType.TYPE})\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.java",
            "package p;\n"+
            "class Foo { }"
        };

        runConformTest(sources, "success");
    }

    // 1 groovy, 2 java but annotation can only be put on METHOD - that is violated by class X
    public void testAnnotationsTargetType10() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n"+
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@Target({ElementType.METHOD})\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.java",
            "package p;\n"+
            "class Foo { }"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t ^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    // 1 groovy, 2 java but annotation can only be put on FIELD - that is violated by class X
    public void testAnnotationsTargetType11() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n"+
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@Target({ElementType.FIELD})\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.groovy",
            "package p;\n"+
            "class Foo { }"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t ^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    // 1 groovy, 2 java but annotation can only be put on FIELD or METHOD - that is violated by class X
    public void testAnnotationsTargetType12() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(p.Foo.class)\n"+
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@Target({ElementType.FIELD,ElementType.METHOD})\n"+
            "@interface Anno { Class<?> value(); }\n",

            "p/Foo.java",
            "package p;\n"+
            "class Foo { }"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(p.Foo.class)\n" +
            "\t ^^^^\n" +
            "Groovy:Annotation @p.Anno is not allowed on element TYPE\n" +
            "----------\n");
    }

    // FIXASC groovy bug?  Why didn't it complain that String doesn't meet the bound - at the moment letting JDT complain...
    public void testWildcards01() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(String.class)\n"+
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/Anno.groovy",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno { Class<? extends Number> value(); }\n"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(String.class)\n" +
            "\t      ^^^^^^^^^^^^^\n" +
            "Type mismatch: cannot convert from Class<String> to Class<? extends Number>\n" +
            "----------\n");
    }

    public void testWildcards02() {
        String[] sources = {
            "p/X.java",
            "package p;\n" +
            "@Anno(String.class)\n"+
            "public class X {\n" +
            "  public void foo(String s) {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno { Class<? extends Number> value(); }\n"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.java (at line 2)\n" +
            "\t@Anno(String.class)\n" +
            "\t      ^^^^^^^^^^^^\n" +
            "Type mismatch: cannot convert from Class<String> to Class<? extends Number>\n" +
            "----------\n");
    }

    public void testWildcards03() {
        String[] sources = {
            "p/X.java",
            "package p;\n" +
            "@Anno(String.class)\n"+
            "public class X {\n" +
            "  public void foo(String s) {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/Anno.groovy",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno { Class<? extends Number> value(); }\n"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.java (at line 2)\n" +
            "\t@Anno(String.class)\n" +
            "\t      ^^^^^^^^^^^^\n" +
            "Type mismatch: cannot convert from Class<String> to Class<? extends Number>\n" +
            "----------\n");
    }

    public void testWildcards04() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(String.class)\n"+
            "public class X {\n" +
            "  public void foo(String s) {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno { Class<? extends Number> value(); }\n"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(String.class)\n" +
            "\t      ^^^^^^^^^^^^^\n" +
            "Type mismatch: cannot convert from Class<String> to Class<? extends Number>\n" +
            "----------\n");
    }

    public void testWildcards05() {
        String[] sources = {
            "p/X.java",
            "package p;\n" +
            "@Anno(Integer.class)\n"+
            "public class X {\n" +
            "  public void foo(String s) {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/Anno.groovy",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno { Class<? extends Number> value(); }\n"
        };

        runConformTest(sources, "success");
    }

    public void testWildcards06() {
        String[] sources = {
            "p/X.java",
            "package p;\n" +
            "@Anno(Number.class)\n"+
            "public class X {\n" +
            "  public void foo(String s) {}\n"+
            "  public static void main(String[]argv) {\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno { Class<? super Integer> value(); }\n"
        };

        runConformTest(sources, "success");
    }

    // bounds violation: String does not meet '? super Integer'
    public void testWildcards07() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(String.class)\n"+
            "public class X {\n" +
            "  public static void main(String[]argv) {\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "import java.lang.annotation.*;\n"+
            "@Retention(RetentionPolicy.RUNTIME)\n"+
            "@interface Anno { Class<? super Integer> value(); }\n"
        };

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(String.class)\n" +
            "\t      ^^^^^^^^^^^^^\n" +
            "Type mismatch: cannot convert from Class<String> to Class<? super Integer>\n" +
            "----------\n");
    }

    // double upper bounds
    public void testWildcards08() {
        String[] sources = {
            "p/X.java",
            "package p;\n" +
            "public class X {\n" +
            "  public static void main(String[]argv) {\n"+
            "    Object o = new Wibble<Integer>().run();\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/Anno.java",
            "package p;\n"+
            "class Wibble<T extends Number & I> { Class<T> run() { return null;} }\n",

            "p/I.java",
            "package p;\n"+
            "interface I {}\n"
        };

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in p\\X.java (at line 4)\n" +
            "\tObject o = new Wibble<Integer>().run();\n" +
            "\t                      ^^^^^^^\n" +
            "Bound mismatch: The type Integer is not a valid substitute for the bounded parameter <T extends Number & I> of the type Wibble<T>\n" +
            "----------\n");
    }

    // double upper bounds
    public void testWildcards09() {
        String[] sources = {
            "p/X.java",
            "package p;\n" +
            "public class X {\n" +
            "  public static void main(String[]argv) {\n"+
            "    Object o = new Wibble<Integer>().run();\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/Anno.groovy",
            "package p;\n"+
            "class Wibble<T extends Number & I> { Class<T> run() { return null;} }\n",

            "p/I.java",
            "package p;\n"+
            "interface I {}\n"
        };

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in p\\X.java (at line 4)\n" +
            "\tObject o = new Wibble<Integer>().run();\n" +
            "\t                      ^^^^^^^\n" +
            "Bound mismatch: The type Integer is not a valid substitute for the bounded parameter <T extends Number & I> of the type Wibble<T>\n" +
            "----------\n");
    }

    // FIXASC groovy bug? Why does groovy not care about bounds violation?
    public void _testWildcards10() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public static void main(String[]argv) {\n"+
            "    Object o = new Wibble<Integer>().run();\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/Anno.groovy",
            "package p;\n"+
            "class Wibble<T extends Number & I> { Class<T> run() { return null;} }\n",

            "p/I.java",
            "package p;\n"+
            "interface I {}\n"
        };

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in p\\X.java (at line 4)\n" +
            "\tObject o = new Wibble<Integer>().run();\n" +
            "\t                      ^^^^^^^\n" +
            "Bound mismatch: The type Integer is not a valid substitute for the bounded parameter <T extends Number & I> of the type Wibble<T>\n" +
            "----------\n");
    }

    public void testWildcards11() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X extends Wibble<Foo> {\n" +
            "  public static void main(String[]argv) {\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/Anno.groovy",
            "package p;\n"+
            "class Wibble<T extends Number & I> { Class<T> run() { return null;} }\n",

            "p/I.java",
            "package p;\n"+
            "interface I {}\n",

            "p/Foo.java",
            "package p;\n"+
            "class Foo implements I {}\n"
        };

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\tpublic class X extends Wibble<Foo> {\n" +
            "\t                       ^^^^^^\n" +
            "Groovy:The type Foo is not a valid substitute for the bounded parameter <T extends java.lang.Number & p.I>\n" +
            "----------\n");
    }

    // FIXASC groovy bug? why doesn't it complain - the type parameter doesn't meet the secondary upper bound
    public void _testWildcards12() {
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X extends Wibble<Integer> {\n" +
            "  public static void main(String[]argv) {\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/Anno.groovy",
            "package p;\n"+
            "class Wibble<T extends Number & I> { Class<T> run() { return null;} }\n",

            "p/I.java",
            "package p;\n"+
            "interface I {}\n",

            "p/Foo.java",
            "package p;\n"+
            "class Foo implements I {}\n"
        };

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\tpublic class X extends Wibble<Foo> {\n" +
            "\t               ^^\n" +
            "Groovy:The type Foo is not a valid substitute for the bounded parameter <T extends java.lang.Number & p.I>\n" +
            "----------\n");
    }
}
