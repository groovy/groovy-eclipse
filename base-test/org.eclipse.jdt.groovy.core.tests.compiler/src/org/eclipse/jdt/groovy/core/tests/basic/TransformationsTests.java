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
package org.eclipse.jdt.groovy.core.tests.basic;

import java.net.URL;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.osgi.framework.Version;

public final class TransformationsTests extends AbstractGroovyRegressionTest {

    public static Test suite() {
        return buildMinimalComplianceTestSuite(TransformationsTests.class, F_1_5);
    }

    public TransformationsTests(String name) {
        super(name);
    }

    private String getJarPath(String entry) throws Exception {
        URL url = Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry(entry);
        return FileLocator.resolve(url).getFile();
    }

    //

    public void testAnnotationCollector() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return;

        String[] sources = {
            "Book.groovy",
            "import java.lang.reflect.*;\n" +
            "class Book {\n" +
            "  @ISBN String isbn;\n" +
            "  public static void main(String []argv) {\n" +
            "    Field f = Book.class.getDeclaredField('isbn');\n" +
            "    Object[] os = f.getDeclaredAnnotations();\n" +
            "    for (Object o: os) {\n" +
            "      System.out.print(o);\n" +
            "    }\n" +
            "  }\n" +
            "}",

            "NotNull.java",
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME) public @interface NotNull {\n" +
            "}",

            "Length.java",
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME) public @interface Length {\n" +
            "  int value() default 0;\n" +
            "}",

            "ISBN.groovy",
            "@NotNull @Length @groovy.transform.AnnotationCollector\n" +
            "public @interface ISBN {\n" +
            "}"
        };

        runConformTest(sources, "@NotNull()@Length(value=0)");
    }

    public void testBuiltInTransforms_Singleton() {
        String[] sources = {
            "Goo.groovy",
            "class Goo {\n" +
            "  public static void main(String[] argv) {\n" +
            "    Run.main(argv)\n" +
            "  }\n" +
            "}",

            "Run.groovy",
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println(Wibble.getInstance().field)\n" +
            "  }\n" +
            "}",

            "Wibble.groovy",
            "@Singleton class Wibble {\n" +
            "  public String field = 'abcd'\n" +
            "}"
        };

        runConformTest(sources, "abcd");
    }

    // lazy option set in Singleton
    public void testBuiltInTransforms_Singleton2() {
        //This test breaks on Groovy < 2.2.1 because the 'strict' flag was introduced in that version.
        if (GroovyUtils.GROOVY_LEVEL < 22) return;

        String[] sources = {
            "Goo.groovy",
            "class Goo {\n"+
            "  public static void main(String[] argv) {\n"+
            "    Run.main(argv);\n"+
            "  }\n"+
            "}\n",

            "Run.groovy",
            "public class Run {\n"+
            "  public static void main(String[] argv) {\n"+
            "    Wibble.run();\n"+
            "    System.out.print(\"running \");\n"+
            "    System.out.print(Wibble.getInstance().field);\n"+
            "  }\n"+
            "}\n",

            "Wibble.groovy",
            "@Singleton(lazy=false, strict=false) class Wibble {" +
            "  public String field = 'abcd';\n"+
            "  private Wibble() { print \"ctor \";}\n"+
            "  static void run() {}\n"+
            "}\n"
        };

        runConformTest(sources, "ctor running abcd");
    }

    public void testBuiltInTransforms_Singleton3() {
        //This test breaks on Groovy < 2.2.1 because the 'strict' flag was introduced in that version.
        if (GroovyUtils.GROOVY_LEVEL < 22) return;

        String[] sources = {
            "Goo.groovy",
            "class Goo {\n"+
            "  public static void main(String[] argv) {\n"+
            "    Run.main(argv);\n"+
            "  }\n"+
            "}\n",

            "Run.groovy",
            "public class Run {\n"+
            "  public static void main(String[] argv) {\n"+
            "    Wibble.run();\n"+
            "    System.out.print(\"running \");\n"+
            "    System.out.print(Wibble.getInstance().field);\n"+
            "  }\n"+
            "}\n",

            "Wibble.groovy",
            "@Singleton(lazy=true, strict=false) class Wibble {" +
            "  public String field = 'abcd';\n"+
            "  private Wibble() { print \"ctor \";}\n"+
            "  static void run() {}\n"+
            "}\n"
        };

        runConformTest(sources, "running ctor abcd");
    }

    public void testBuiltInTransforms_Category1() {
        String[] sources = {
            "Demo.groovy",
            "   use(NumberCategory) {\n"+
            "       def dist = 300.meters\n"+
            "\n"+
            "       assert dist instanceof Distance\n"+
            "       assert dist.toString() == \"300m\"\n"+
            "  print dist.toString()\n"+
            "}",

            "Distance.groovy",
            "   final class Distance {\n"+
            "       def number\n"+
            "       String toString() { \"${number}m\" }\n"+
            "}",

            "NumberCategory.groovy",
            "   class NumberCategory {\n"+
            "       static Distance getMeters(Number self) {\n"+
            "           new Distance(number: self)\n"+
            "       }\n"+
            "}"
        };

        runConformTest(sources, "300m");
    }

    public void testBuiltInTransforms_Category2() {
        String[] sources = {
            "Demo.groovy",
            "   use(NumberCategory) {\n"+
            "       def dist = 300.meters\n"+
            "\n"+
            "       assert dist instanceof Distance\n"+
            "       assert dist.toString() == \"300m\"\n"+
            "  print dist.toString()\n"+
            "   }\n",

            "Distance.groovy",
            "   final class Distance {\n"+
            "       def number\n"+
            "       String toString() { \"${number}m\" }\n"+
            "   }\n",

            "NumberCategory.groovy",
            "   @Category(Number) class NumberCategory {\n"+
            "       Distance getMeters() {\n"+
            "           new Distance(number: this)\n"+
            "       }\n"+
            "   }\n"+
            "\n"
        };

        runConformTest(sources, "300m");
    }

    public void testBuiltInTransforms_Category3() {
        String[] sources = {
            "Foo.groovy",
            "assert new Plane().fly() ==\n"+
            "       \"I'm the Concorde and I fly!\"\n"+
            "assert new Submarine().dive() ==\n"+
            "       \"I'm the Yellow Submarine and I dive!\"\n"+
            "\n"+
            "assert new JamesBondVehicle().fly() ==\n"+
            "       \"I'm the James Bond's vehicle and I fly!\"\n"+
            "assert new JamesBondVehicle().dive() ==\n"+
            "       \"I'm the James Bond's vehicle and I dive!\"\n"+
            "print new JamesBondVehicle().dive();\n",

            "FlyingAbility.groovy",
            "@Category(Vehicle) class FlyingAbility {\n"+
            "    def fly() { \"I'm the ${name} and I fly!\" }\n"+
            "}\n",

            "DivingAbility.groovy",
            "@Category(Vehicle) class DivingAbility {\n"+
            "    def dive() { \"I'm the ${name} and I dive!\" }\n"+
            "}\n",

            "Vehicle.java",
            "interface Vehicle {\n"+
            "    String getName();\n"+
            "}\n",

            "Submarine.groovy",
            "@Mixin(DivingAbility)\n"+
            "class Submarine implements Vehicle {\n"+
            "    String getName() { \"Yellow Submarine\" }\n"+
            "}\n",

            "Plane.groovy",
            "@Mixin(FlyingAbility)\n"+
            "class Plane implements Vehicle {\n"+
            "    String getName() { \"Concorde\" }\n"+
            "}\n",

            "JamesBondVehicle.groovy",
            "@Mixin([DivingAbility, FlyingAbility])\n"+
            "class JamesBondVehicle implements Vehicle {\n"+
            "    String getName() { \"James Bond's vehicle\" }\n"+
            "}\n"
        };

        runConformTest(sources, "I'm the James Bond's vehicle and I dive!");
    }

    public void testBuiltInTransforms_PackageScope() {
        // http://groovy.codehaus.org/PackageScope+transformation
        // Adjust the visibility of a property so instead of private it is package default
        String[] sources = {
            "Goo.groovy",
            "class Goo {\n"+
            "  public static void main(String[] argv) {\n"+
            "    q.Run.main(argv);\n"+
            "  }\n"+
            "}\n",

            "q/Run.groovy",
            "package q;\n"+
            "import q.Wibble;\n"+
            "public class Run {\n"+
            "  public static void main(String[] argv) throws Exception {\n"+
            "    Wibble w = new Wibble();\n"+
            "    System.out.print(Wibble.class.getDeclaredField(\"field\").getModifiers());\n"+
            "    System.out.print(Wibble.class.getDeclaredField(\"field2\").getModifiers());\n"+
            "  }\n"+
            "}\n",

            "q/Wibble.groovy",
            "package q\n"+
            "class Wibble {" +
            "  String field = 'abcd';\n"+
            "  @groovy.transform.PackageScope String field2 = 'abcd';\n"+
            "}\n"
        };

        runConformTest(sources, "20"); // 0x2 = private 0x0 = default (so field2 has had private vis removed by annotation);
    }

    // not a great test, needs work
    public void testCategory_STS3822() {
        if (Platform.getBundle("org.eclipse.jdt.core").getVersion().compareTo(Version.parseVersion("3.10")) < 0) return;

        String[] sources = {
            "bad.groovy",
            "@Category(C.class) \n"+
            "@ScriptMixin(C.class)\n"+
            "class Bad {\n"+
            "  @Override\n"+
            "  public String toString()\n"+
            "  { return \"Bad [takeI()=\" + takeI() + \"]\"; }\n"+
            "}\n"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in bad.groovy (at line 1)\n" +
            "\t@Category(C.class) \n" +
            "\t  ^"+(GroovyUtils.isAtLeastGroovy(20)?"^^^^^^^":"")+"\n" +
            "Groovy:@groovy.lang.Category must define \'value\' which is the class to apply this category to @ line 1, column 2.\n" +
            "----------\n" +
            "2. ERROR in bad.groovy (at line 1)\n" +
            "\t@Category(C.class) \n" +
            "\t          ^\n" +
            "Groovy:unable to find class \'C.class\' for annotation attribute constant\n" +
            "----------\n" +
            "3. ERROR in bad.groovy (at line 1)\n" +
            "\t@Category(C.class) \n" +
            "\t           ^"+(GroovyUtils.isAtLeastGroovy(20)?"^^^^^^":"")+"\n" +
            "Groovy:Only classes and closures can be used for attribute \'value\' in @groovy.lang.Category\n" +
            "----------\n" +
            "4. ERROR in bad.groovy (at line 2)\n" +
            "\t@ScriptMixin(C.class)\n" +
            "\t ^^^^^^^^^^^\n" +
            "Groovy:unable to resolve class ScriptMixin ,  unable to find class for annotation\n" +
            "----------\n" +
            "5. ERROR in bad.groovy (at line 2)\n" +
            "\t@ScriptMixin(C.class)\n" +
            "\t ^"+(GroovyUtils.isAtLeastGroovy(20)?"^^^^^^^^^^":"")+"\n" +
            "Groovy:class ScriptMixin is not an annotation in @ScriptMixin\n" +
            "----------\n" +
            "6. ERROR in bad.groovy (at line 2)\n" +
            "\t@ScriptMixin(C.class)\n" +
            "\t             ^\n" +
            "Groovy:unable to find class \'C.class\' for annotation attribute constant\n" +
            "----------\n" +
            (!GroovyUtils.isAtLeastGroovy(20)?"":
            "7. ERROR in bad.groovy (at line 4)\n" +
            "\t@Override\n" +
            "\t ^^^^^^^^\n" +
            "Groovy:Method \'toString\' from class \'Bad\' does not override method from its superclass or interfaces but is annotated with @Override.\n" +
            "----------\n"));
    }

    public void testDelegate() {
        String[] sources = {
            "Bar.groovy",
            "class Foo { @Delegate URL myUrl }\n" +
            "\n" +
            "print Foo.class.getDeclaredMethod('getContent', Class[].class)"
        };

        runConformTest(sources, "public final java.lang.Object Foo.getContent(java.lang.Class[]) throws java.io.IOException");
    }

    public void testImmutable() {
        String[] sources = {
            "c/Main.java",
            "package c;\n" +
            "public class Main {\n" +
            "  public static void main(String[] args) {" +
            "  }\n" +
            "}\n",

            "a/SomeId.groovy",
            "package a;\n" +
            "import groovy.transform.Immutable\n" +
            "@Immutable\n" +
            "class SomeId {\n" +
            "  UUID id\n" +
            "}\n",

            "b/SomeValueObject.groovy",
            "package b;\n" +
            "import groovy.transform.Immutable\n" +
            "import a.SomeId\n" +
            "@Immutable\n" +
            "class SomeValueObject {\n" +
            "  SomeId id\n" +
            "}\n"
        };

        runConformTest(sources);

        GroovyCompilationUnitDeclaration unit = getCUDeclFor("SomeValueObject.groovy");
        ClassNode classNode = unit.getCompilationUnit().getClassNode("b.SomeValueObject");
        FieldNode field = classNode.getField("id");
        ClassNode type = field.getType();
        List<AnnotationNode> annotations = type.getAnnotations(ClassHelper.make(groovy.transform.Immutable.class));
        assertEquals(1, annotations.size());
    }

    /**
     * COOL!!!  The getInstance() method is added by a late AST Transformation made due to the Singleton annotation - and yet
     * still it is referencable from Java.  This is not possible with normal joint compilation.
     * currently have to 'turn on' support in GroovyClassScope.getAnyExtraMethods() - still thinking about this stuff...
     */
    public void _testJavaAccessingTransformedGroovy_Singleton() {
        String[] sources = {
            "Goo.groovy",
            "class Goo {\n"+
            "  public static void main(String[] argv) {\n"+
            "    Run.main(argv);\n"+
            "  }\n"+
            "}\n",

            "Run.java",
            "public class Run {\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println(Wibble.getInstance().field);\n"+
            "  }\n"+
            "}\n",

            "Wibble.groovy",
            "@Singleton class Wibble {" +
            "  public String field = 'abc';\n"+
            "}\n"
        };

        runConformTest(sources, "abc");
    }

    public void testTypeChecked1() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;

        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.TypeChecked\n"+
            "@TypeChecked\n"+
            "void method(String message) {\n"+
            "   if (rareCondition) {\n"+
            "        println \"Did you spot the error in this ${message.toUppercase()}?\"\n"+
            "   }\n"+
            "}"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 4)\n" +
            "\tif (rareCondition) {\n" +
            "\t    ^"+(GroovyUtils.isAtLeastGroovy(20)?"^^^^^^^^^^^^":"")+"\n" +
            "Groovy:[Static type checking] - The variable [rareCondition] is undeclared.\n" +
            "----------\n" +
            "2. ERROR in Foo.groovy (at line 5)\n" +
            "\tprintln \"Did you spot the error in this ${message.toUppercase()}?\"\n" +
            "\t                                         ^"+(GroovyUtils.isAtLeastGroovy(20)?"^^^^^^^^^^^^^^^^^^^^^^":"")+"\n" +
            "Groovy:[Static type checking] - Cannot find matching method java.lang.String#toUppercase()"+(GroovyUtils.isAtLeastGroovy(20)?". Please check if the declared type is right and if the method exists.":"")+"\n" +
            "----------\n");
    }

    public void testTypeChecked2() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;

        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.TypeChecked\n"+
            "@TypeChecked\n"+
            "void method(String message) {\n"+
            "   List<Integer> ls = new ArrayList<Integer>();\n"+
            "   ls.add(123);\n"+
            "   ls.add('abc');\n"+
            "}"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 6)\n" +
            "\tls.add(\'abc\');\n" +
            "\t^" + (GroovyUtils.isAtLeastGroovy(20) ? "^^^^^^^^^^^^" : "") + "\n" +
            (GroovyUtils.isAtLeastGroovy(23) ? "Groovy:[Static type checking] - Cannot call java.util.ArrayList <Integer>#add(java.lang.Integer) with arguments [java.lang.String] ":
            "Groovy:[Static type checking] - Cannot find matching method java.util.ArrayList#add(java.lang.String)" + (GroovyUtils.isAtLeastGroovy(20) ? ". Please check if the declared type is right and if the method exists." : "")) + "\n" +
            "----------\n");
    }

    public void testTypeChecked3() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return;

        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class Foo {" +
            "  def method() {\n" +
            "    Set<java.beans.BeanInfo> defs = []\n" +
            "    defs*.additionalBeanInfo\n" +
            "  }\n" +
            "}"
        };

        runConformTest(sources);
    }

    // https://issues.apache.org/jira/browse/GROOVY-8033
    public void _testTypeChecked4() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return;

        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class Foo {" +
            "  static def method() {\n" + // static method alters type checking
            "    Set<java.beans.BeanInfo> defs = []\n" +
            "    defs*.additionalBeanInfo\n" +
            "  }\n" +
            "}"
        };

        runConformTest(sources);
    }

    public void testCompileStatic1() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;

        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.CompileStatic\n"+
            "@CompileStatic\n"+
            "void method(String message) {\n"+
            "   List<Integer> ls = new ArrayList<Integer>();\n"+
            "   ls.add(123);\n"+
            "   ls.add('abc');\n"+
            "}"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 6)\n" +
            "\tls.add(\'abc\');\n" +
            "\t^"+(GroovyUtils.isAtLeastGroovy(20)?"^^^^^^^^^^^^":"")+"\n" +
            (GroovyUtils.isAtLeastGroovy(23)?
            "Groovy:[Static type checking] - Cannot call java.util.ArrayList <Integer>#add(java.lang.Integer) with arguments [java.lang.String] \n":
            "Groovy:[Static type checking] - Cannot find matching method java.util.ArrayList#add(java.lang.String)"+(GroovyUtils.isAtLeastGroovy(20)?". Please check if the declared type is right and if the method exists.":""))+(GroovyUtils.isAtLeastGroovy(23)?"":"\n") +
            "----------\n");
    }

    /**
     * Testing the code in the StaticTypeCheckingSupport.checkCompatibleAssignmentTypes.
     *
     * That method does a lot of equality by == testing against classnode constants, which doesn't work so well for us...
     */
    public void testCompileStatic2() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;

        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.CompileStatic;\n"+
            "\n"+
            "import java.util.Properties;\n"+
            "\n"+
            "class One { \n"+
            "   @CompileStatic\n"+
            "   private String getPropertyValue(String propertyName, Properties props, String defaultValue) {\n"+
            "       // First check whether we have a system property with the given name.\n"+
            "       def value = getValueFromSystemOrBuild(propertyName, props)\n"+
            "\n"+
            "       // Return the BuildSettings value if there is one, otherwise\n"+
            "       // use the default.\n"+
            "       return value != null ? value : defaultValue \n"+
            "   }\n"+
            "\n"+
            "   @CompileStatic\n"+
            "   private getValueFromSystemOrBuild(String propertyName, Properties props) {\n"+
            "       def value = System.getProperty(propertyName)\n"+
            "       if (value != null) return value\n"+
            "\n"+
            "       // Now try the BuildSettings config.\n"+
            "       value = props[propertyName]\n"+
            "       return value\n"+
            "   } \n"+
            "}  \n"
        };

        runConformTest(sources);
    }

    public void testCompileStatic3() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;

        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.CompileStatic;\n"+
            "\n"+
            "@CompileStatic void test() {\n"+
            "   int littleInt = 3\n"+
            "   Integer objectInt = littleInt\n"+
            "}\n"
        };

        runConformTest(sources);
    }

    public void testCompileStatic_1511() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;

        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.CompileStatic\n"+
            "def meth() {\n"+
            "   List<String> second = []\n"+
            "   List<String> artefactResources2 = []\n"+
            "   second.addAll(artefactResources2)\n"+
            "   println 'abc'\n"+
            "}\n"+
            "meth();"
        };

        runConformTest(sources, "abc");
    }

    public void testCompileStatic_1505() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;

        String[] sources = {
            "DynamicQuery.groovy",
            "import groovy.transform.TypeChecked\n"+
            "@TypeChecked\n"+
            "class DynamicQuery {\n"+
            "   public static void main(String[]argv) {\n"+
            "     new DynamicQuery().foo(null);\n"+
            "   }\n"+
            "   private foo(Map sumpin){\n"+
            "       Map foo\n"+
            "       foo.collect{ Map.Entry it ->it.key}\n"+
            "       print 'abc';\n"+
            "   }\n"+
            "}\n"
        };

        runConformTest(sources, "abc");
    }

    public void _testCompileStatic_1506() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;

        String[] sources = {
            "LoggerTest.groovy",
            "import groovy.transform.TypeChecked\n"+
            "import groovy.util.logging.*\n"+
            "@Slf4j\n"+
            "@TypeChecked\n"+
            "   public class LoggerTest {\n"+
            "       public static void main(String... args) {\n"+
            "           println \"println\"\n"+
            "           LoggerTest.log.info(\"Logged\");\n"+
            "           log.info(\"foo\")\n"+
            "       }\n"+
            "   }\n"
        };

        runConformTest(sources);
    }

    public void testCompileStatic4() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;

        // verify generics are correct for the 'Closure<?>' as CompileStatic will attempt an exact match
        String[] sources = {
            "A.groovy",
            "class A {\n"+
            "   public void profile(String name, groovy.lang.Closure<?> callable) { }\n"+
            "}\n",

            "B.groovy",
            "@groovy.transform.CompileStatic\n"+
            "class B extends A {\n"+
            "\n"+
            "   def foo() {\n"+
            "       profile(\"creating plugin manager with classes\") {\n"+
            "           System.out.println('abc');\n"+
            "       }\n"+
            "   }\n"+
            "\n"+
            "}\n"
        };

        runConformTest(sources);
    }

    public void testCompileDynamic() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return;

        String[] sources = {
            "A.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A {\n" +
            "    int prop\n" +
            "    int computeStatic(int input) {\n" +
            "        prop + input\n" +
            "    }\n" +
            "    @groovy.transform.CompileDynamic\n" +
            "    int computeDynamic(int input) {\n" +
            "        missing(prop, input)\n" +
            "    }\n" +
            "}"
        };

        runConformTest(sources);
    }

    public void testTransforms_BasicLogging() throws Exception {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyClassLoaderPath, getJarPath("astTransformations/transforms.jar"));
        options.put(CompilerOptions.OPTIONG_GroovyProjectName, "Test");

        // From: http://svn.codehaus.org/groovy/trunk/groovy/groovy-core/src/examples/transforms/local
        String[] sources = {
            "examples/local/LoggingExample.groovy",
            "package examples.local\n"+
            "\n"+
            "/**\n"+
            "* Demonstrates how a local transformation works. \n"+
            "* \n"+
            "* @author Hamlet D'Arcy\n"+
            "*/ \n"+
            "\n"+
            "def greet() {\n"+
            "    println \"Hello World\"\n"+
            "}\n"+
            "    \n"+
            "@WithLogging    //this should trigger extra logging\n"+
            "def greetWithLogging() {\n"+
            "    println \"Hello World\"\n"+
            "}\n"+
            "    \n"+
            "// this prints out a simple Hello World\n"+
            "greet()\n"+
            "\n"+
            "// this prints out Hello World along with the extra compile time logging\n"+
            "greetWithLogging()\n"+
            "\n"+
            "\n"+
            "//\n"+
            "// The rest of this script is asserting that this all works correctly. \n"+
            "//\n"+
            "\n"+
            "def oldOut = System.out\n"+
            "// redirect standard out so we can make assertions on it\n"+
            "def standardOut = new ByteArrayOutputStream();\n"+
            "System.setOut(new PrintStream(standardOut)); \n"+
            "  \n"+
            "greet()\n"+
            "assert \"Hello World\" == standardOut.toString(\"ISO-8859-1\").trim()\n"+
            "\n"+
            "// reset standard out and redirect it again\n"+
            "standardOut.close()\n"+
            "standardOut = new ByteArrayOutputStream();\n"+
            "System.setOut(new PrintStream(standardOut)); \n"+
            "\n"+
            "greetWithLogging()\n"+
            "def result = standardOut.toString(\"ISO-8859-1\").split('\\n')\n"+
            "assert \"Starting greetWithLogging\"  == result[0].trim()\n"+
            "assert \"Hello World\"                == result[1].trim()\n"+
            "assert \"Ending greetWithLogging\"    == result[2].trim()\n"+
            "\n"+
            "System.setOut(oldOut);\n"+
            "print 'done'\n"+
            "\n",

//          "examples/local/WithLogging.groovy",
//          "package examples.local\n"+
//          "import java.lang.annotation.Retention\n"+
//          "import java.lang.annotation.Target\n"+
//          "import org.codehaus.groovy.transform.GroovyASTTransformationClass\n"+
//          "import java.lang.annotation.ElementType\n"+
//          "import java.lang.annotation.RetentionPolicy\n"+
//          "\n"+
//          "/**\n"+
//          "* This is just a marker interface that will trigger a local transformation. \n"+
//          "* The 3rd Annotation down is the important one: @GroovyASTTransformationClass\n"+
//          "* The parameter is the String form of a fully qualified class name. \n"+
//          "*\n"+
//          "* @author Hamlet D'Arcy\n"+
//          "*/ \n"+
//          "@Retention(RetentionPolicy.SOURCE)\n"+
//          "@Target([ElementType.METHOD])\n"+
//          "@GroovyASTTransformationClass([\"examples.local.LoggingASTTransformation\"])\n"+
//          "public @interface WithLogging {\n"+
//          "}\n"
        };

        runConformTest(sources,
            "Hello World\n" +
            "Starting greetWithLogging\n" +
            "Hello World\n" +
            "Ending greetWithLogging\n" +
            "done",
            null,
            true,
            null,
            options,
            null);
    }

    public void testTransforms_AtLog() {
        // See
        // https://jira.codehaus.org/browse/GRECLIPSE-1503
        // https://jira.codehaus.org/browse/GROOVY-5736
        String[] sources = {
            "examples/local/Log4jExample.groovy",
            "package examples.local\n" +
            "import groovy.util.logging.*\n" +
            "@Log4j\n" +
            "class Log4jExample {\n" +
            "  def meth() {\n" +
            "    logger.info('yay!')\n" +
            "  }\n" +
            "}",

            "examples/local/Slf4JExample.groovy",
            "package examples.local\n" +
            "import groovy.util.logging.*\n" +
            "@Slf4j\n" +
            "class Slf4jExample {\n" +
            "  def meth() {\n" +
            "    logger.info('yay!')\n" +
            "  }\n" +
            "}",

            "examples/local/LoggingExample.groovy",
            "package examples.local\n" +
            "import groovy.util.logging.*\n" +
            "@Log\n" +
            "class LoggingExample {\n" +
            "  def meth() {\n" +
            "    logger.info('yay!')\n" +
            "  }\n" +
            "}",

            "examples/local/CommonsExample.groovy",
            "package examples.local\n" +
            "import groovy.util.logging.*\n" +
            "@Commons\n" +
            "class CommonsExample {\n" +
            "  def meth() {\n" +
            "    logger.info('yay!')\n" +
            "  }\n" +
            "}"
        };

        runConformTest(sources);
    }

    public void testJDTClassNode_1731() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return;

        // Testcode based on article: http://www.infoq.com/articles/groovy-1.5-new
        // The groups of tests are loosely based on the article contents - but what is really exercised here is the accessibility of
        // the described constructs across the Java/Groovy divide.

        String[] sources = {
            "c/Main.java",
            "package c;\n" +
            "import java.lang.reflect.Method;\n" +
            "import a.SampleAnnotation;\n" +
            "import b.Sample;\n" +
            "public class Main {\n" +
            "    public static void main(String[] args) throws Exception {" +
            "        Method method = Sample.class.getMethod(\"doSomething\");\n" +
            "        SampleAnnotation annotation = method.getAnnotation(SampleAnnotation.class);\n" +
            "        System.out.print(annotation);\n" +
            "    }\n" +
            "}\n",

            "a/SampleAnnotation.java",
            "package a;\n" +
            "import java.lang.annotation.ElementType;\n" +
            "import java.lang.annotation.Retention;\n" +
            "import java.lang.annotation.RetentionPolicy;\n" +
            "import java.lang.annotation.Target;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target({ElementType.METHOD})\n" +
            "public @interface SampleAnnotation {}\n",

            "a/DelegateInOtherProject.java",
            "package a;\n" +
            "public class DelegateInOtherProject {\n" +
            "    @SampleAnnotation\n" +
            "    public void doSomething() {}\n" +
            "}\n",

            "b/Sample.groovy",
            "package b\n" +
            "import groovy.transform.CompileStatic\n" +
            "import a.DelegateInOtherProject;\n" +
            "@CompileStatic\n" +
            "class Sample {\n" +
            "    @Delegate(methodAnnotations = true)\n" +
            "    DelegateInOtherProject delegate\n" +
            "}\n",

            "b/Delegated.groovy",
            "package b\n" +
            "import groovy.transform.CompileStatic\n" +
            "import a.SampleAnnotation;\n" +
            "@CompileStatic\n" +
            "class Delegated {\n" +
            "    @SampleAnnotation\n" +
            "    def something() {}\n" +
            "}\n"
        };

        runConformTest(sources, "@a.SampleAnnotation()");
    }

    public void _testGrab() {
        String[] sources = {
            "Printer.groovy",
            "@Grab('joda-time:joda-time:1.6')\n"+
            "def printDate() {\n"+
            "  def dt = new org.joda.time.DateTime()\n"+
            "}\n"+
            "printDate()"
        };

        runConformTest(sources);
    }

    /**
     * Improving grab, this program has a broken grab. Without changes we get a 'general error' recorded on the first line of the source file (big juicy exception)
     * General error during conversion: Error grabbing Grapes -- [unresolved dependency: org.aspectj#aspectjweaver;1.6.11x: not found] java.lang.RuntimeException: Error grabbing
     * Grapes -- [unresolved dependency: org.aspectj#aspectjweaver;1.6.11x: not found] at sun.reflect.GeneratedConstructorAccessor48.newInstance(Unknown Source) at
     * sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:27) at java.lang.reflect.Constructor.newInstance(Constructor.java:513) at
     * org.codehaus.groovy.reflection.CachedConstructor.invoke(CachedConstructor.java:77) at ...
     * With grab improvements we get two errors - the missing dependency and the missing type (which is at the right version of that dependency!)
     */
    public void _testGrabWithErrors() {
        String[] sources = {
            "Grab1.groovy",
            "@Grapes([\n"+
            "  @Grab(group='joda-time', module='joda-time', version='1.6'),\n"+
            "  @Grab(group='org.aspectj', module='aspectjweaver', version='1.6.11x')\n"+
            "])\n" +
            "class C {\n"+
            "  def printDate() {\n"+
            "    def dt = new org.joda.time.DateTime()\n"+
            "    def world = new org.aspectj.weaver.bcel.BcelWorld()\n"+
            "    print dt\n"+
            "  }\n"+
            "  public static void main(String[] argv) {\n"+
            "    new C().printDate()\n"+
            "  }\n"+
            "}\n"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Grab1.groovy (at line 3)\n" +
            "\t@Grab(group='org.aspectj', module='aspectjweaver', version='1.6.11x')\n" +
            "\t ^^^\n" +
            "Groovy:Error grabbing Grapes -- [unresolved dependency: org.aspectj#aspectjweaver;1.6.11x: not found]\n" +
            "----------\n" +
            "2. ERROR in Grab1.groovy (at line 8)\n" +
            "\tdef world = new org.aspectj.weaver.bcel.BcelWorld()\n" +
            "\t                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:unable to resolve class org.aspectj.weaver.bcel.BcelWorld \n" +
            "----------\n");
    }

    public void _testGrabScriptAndImports_GRE680() {
        String[] sources = {
            "Script.groovy",
            "import org.mortbay.jetty.Server\n"+
            "import org.mortbay.jetty.servlet.*\n"+
            "import groovy.servlet.*\n"+
            "\n"+
            "@Grab(group='org.mortbay.jetty', module='jetty-embedded', version='6.1.0')\n"+
            "def runServer(duration) { }\n"+
            "runServer(10000)"
        };

        runConformTest(sources);
    }

    public void testGreclipse1506() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;

        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.TypeChecked;\n"+
            "import groovy.util.logging.Log;\n"+
            "\n"+
            "@TypeChecked @Log\n"+
            "public class LoggerTest {\n"+
            "  public static void main(String... args) {\n"+
            "    println 'println'\n"+
            "    log.info('foo')\n"+
            "  }\n"+
            "}"
        };

        runConformTest(sources);
    }

    public void testGreclipse1514() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;

        String[] sources = {
            "C.groovy",
            "@SuppressWarnings(\"rawtypes\")\n"+
            "@groovy.transform.CompileStatic\n"+
            "class C {\n"+
            "  def xxx(List list) {\n"+
            "    list.unique().each { }\n"+
            "  }\n"+
            "}\n"
        };

        runConformTest(sources);
    }

    public void testGreclipse1515() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;

        String[] sources = {
            "C.groovy",
            "import groovy.transform.CompileStatic;\n" +
            "import java.util.regex.Pattern\n" +
            "\n" +
            "@CompileStatic\n" +
            "class C {\n" +
            "  void validate() {\n" +
            "    for (String validationKey : [:].keySet()) {\n" +
            "      String regex\n" +
            "      Pattern pattern = ~regex\n" + // NPE on this bitwise negation
            "    }\n" +
            "  }\n" +
            "}"
        };

        runConformTest(sources);
    }

    public void testGreclipse1521() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;

        String[] sources = {
            "Foo.groovy",
            "\n"+
            "@groovy.transform.CompileStatic\n"+
            "class Foo {\n"+
            "  enum Status { ON, OFF }\n"+
            "}"
        };

        runConformTest(sources);
    }
}
