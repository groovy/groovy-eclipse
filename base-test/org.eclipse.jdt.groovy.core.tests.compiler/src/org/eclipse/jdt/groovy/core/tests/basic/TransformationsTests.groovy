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
package org.eclipse.jdt.groovy.core.tests.basic

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.Platform
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest
import org.eclipse.jdt.core.tests.util.GroovyUtils
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions

final class TransformationsTests extends AbstractGroovyRegressionTest {

    static junit.framework.Test suite() {
        buildMinimalComplianceTestSuite(TransformationsTests, F_1_5)
    }

    TransformationsTests(String name) {
        super(name)
    }

    void testDelegate() {
        if (GroovyUtils.GROOVY_LEVEL < 18) return

        String[] sources = [
            "Bar.groovy",
            "class Foo { @Delegate URL myUrl }\n" +
            "\n" +
            "print Foo.class.getDeclaredMethod('getContent', Class[].class)"
        ]

        runConformTest(sources, "public final java.lang.Object Foo.getContent(java.lang.Class[]) throws java.io.IOException")
    }

    void testGreclipse1514() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        String[] sources = [
            "C.groovy",
            "@SuppressWarnings(\"rawtypes\")\n"+
            "@groovy.transform.CompileStatic\n"+
            "class C {\n"+
            "  def xxx(List list) {\n"+
            "    list.unique().each { }\n"+
            "  }\n"+
            "}\n"
        ]

        runConformTest(sources)
    }

    void _testGreclipse1515() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        String[] sources = [
            "C.groovy",
            "import groovy.transform.CompileStatic;\n"+
            "import java.util.regex.Pattern\n"+
            "\n"+
            "@CompileStatic\n"+
            "class C {\n"+
            "  void validate () {\n"+
            "    for (String validationKey : keySet()) {\n"+ // Where is keySet() from?
            "      String regex\n"+
            "      Pattern pattern = ~regex\n"+
            "    }\n"+
            "  }\n"+
            "}"
        ]

        runConformTest(sources)
    }

    // not a great test, needs work
    void testBadCodeCategory_STS3822() {
        String[] sources = [
            "bad.groovy",
            "@Category(C.class) \n"+
            "@ScriptMixin(C.class)\n"+
            "class Bad {\n"+
            "  @Override\n"+
            "  public String toString()\n"+
            "  { return \"Bad [takeI()=\" + takeI() + \"]\"; }\n"+
            "}\n"
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in bad.groovy (at line 1)\n" +
            "\t@Category(C.class) \n" +
            "\t  ^^^^^^^^\n" +
            "Groovy:@groovy.lang.Category must define \'value\' which is the class to apply this category to @ line 1, column 2.\n" +
            "----------\n" +
            "2. ERROR in bad.groovy (at line 1)\n" +
            "\t@Category(C.class) \n" +
            "\t          ^\n" +
            "Groovy:unable to find class \'C.class\' for annotation attribute constant\n" +
            "----------\n" +
            "3. ERROR in bad.groovy (at line 1)\n" +
            "\t@Category(C.class) \n" +
            "\t           ^^^^^^^\n" +
            "Groovy:Only classes and closures can be used for attribute \'value\' in @groovy.lang.Category\n" +
            "----------\n" +
            "4. ERROR in bad.groovy (at line 2)\n" +
            "\t@ScriptMixin(C.class)\n" +
            "\t ^^^^^^^^^^^\n" +
            "Groovy:unable to resolve class ScriptMixin ,  unable to find class for annotation\n" +
            "----------\n" +
            "5. ERROR in bad.groovy (at line 2)\n" +
            "\t@ScriptMixin(C.class)\n" +
            "\t ^^^^^^^^^^^\n" +
            "Groovy:class ScriptMixin is not an annotation in @ScriptMixin\n" +
            "----------\n" +
            "6. ERROR in bad.groovy (at line 2)\n" +
            "\t@ScriptMixin(C.class)\n" +
            "\t             ^\n" +
            "Groovy:unable to find class \'C.class\' for annotation attribute constant\n" +
            "----------\n" +
            "7. ERROR in bad.groovy (at line 4)\n" +
            "\t@Override\n" +
            "\t ^^^^^^^^\n" +
            "Groovy:Method \'toString\' from class \'Bad\' does not override method from its superclass or interfaces but is annotated with @Override.\n" +
            "----------\n")
    }

    void testGreclipse1521() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        String[] sources = [
            "Foo.groovy",
            "\n"+
            "@groovy.transform.CompileStatic\n"+
            "class Foo {\n"+
            "  enum Status { ON, OFF}\n"+
            "}"
        ]

        runConformTest(sources)
    }

    void _testGreclipse1506() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        String[] sources = [
            "Foo.groovy",
            "import groovy.transform.TypeChecked;\n"+
            "import groovy.util.logging.Slf4j;\n"+
            "\n"+
            "@Slf4j\n"+
            "@TypeChecked\n"+
            "public class LoggerTest\n"+
            "{\n"+
            "    public static void main(String... args)\n"+
            "    {\n"+
            "        println 'println'\n"+
            "        log.info('foo')\n"+
            "    }\n"+
            "}\n"
        ]

        runConformTest(sources)
    }

    /**
     * COOL!!!  The getInstance() method is added by a late AST Transformation made due to the Singleton annotation - and yet
     * still it is referencable from Java.  This is not possible with normal joint compilation.
     * currently have to 'turn on' support in GroovyClassScope.getAnyExtraMethods() - still thinking about this stuff...
     */
    void _testJavaAccessingTransformedGroovy_Singleton() {
        String[] sources = [
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
        ]

        runConformTest(sources, "abc")
    }

    void testBuiltInTransforms_Singleton() {
        String[] sources = [
            'Goo.groovy', '''
            class Goo {
              public static void main(String[] argv) {
                Run.main(argv)
              }
            }
            ''',

            'Run.groovy', '''
            public class Run {
              public static void main(String[] argv) {
                System.out.println(Wibble.getInstance().field)
              }
            }
            ''',

            'Wibble.groovy', '''
            @Singleton class Wibble {
              public String field = 'abcd'
            }
            '''
        ]

        runConformTest(sources, "abcd")
    }

    // lazy option set in Singleton
    void testBuiltInTransforms_Singleton2() {
        //This test breaks on Groovy < 2.2.1 because the 'strict' flag was introduced in that version.
        if (GroovyUtils.GROOVY_LEVEL < 22) return

        String[] sources = [
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
        ]

        this.runConformTest(sources, "ctor running abcd")
    }

    void testBuiltInTransforms_Singleton3() {
        //This test breaks on Groovy < 2.2.1 because the 'strict' flag was introduced in that version.
        if (GroovyUtils.GROOVY_LEVEL < 22) return

        String[] sources = [
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
        ]

        this.runConformTest(sources, "running ctor abcd")
    }

    void testBuiltInTransforms_Category1() {
        String[] sources = [
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
            '       String toString() { \"${number}m\" }\n'+
            "   }\n",

            "NumberCategory.groovy",
            "   class NumberCategory {\n"+
            "       static Distance getMeters(Number self) {\n"+
            "           new Distance(number: self)\n"+
            "       }\n"+
            "   }\n"+
            "\n"
        ]

        this.runConformTest(sources, "300m")
    }

    void testBuiltInTransforms_Category2() {
        String[] sources = [
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
            '       String toString() { \"${number}m\" }\n'+
            "   }\n",

            "NumberCategory.groovy",
            "   @Category(Number) class NumberCategory {\n"+
            "       Distance getMeters() {\n"+
            "           new Distance(number: this)\n"+
            "       }\n"+
            "   }\n"+
            "\n"
        ]

        this.runConformTest(sources, "300m")
    }

    void testBuiltInTransforms_Category3() {
        String[] sources = [
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
            '    def fly() { \"I\'m the ${name} and I fly!\" }\n'+
            "}\n",

            "DivingAbility.groovy",
            "@Category(Vehicle) class DivingAbility {\n"+
            '    def dive() { \"I\'m the ${name} and I dive!\" }\n'+
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
        ]

        runConformTest(sources, "I'm the James Bond's vehicle and I dive!")
    }

    void testBuiltInTransforms_PackageScope() {
        // in a different place on 1.7
        if (GroovyUtils.GROOVY_LEVEL < 18) return

        // http://groovy.codehaus.org/PackageScope+transformation
        // Adjust the visibility of a property so instead of private it is package default
        String[] sources = [
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
        ]

        runConformTest(sources, "20"); // 0x2 = private 0x0 = default (so field2 has had private vis removed by annotation)
    }

    void testTypeChecked() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        String[] sources = [
            "Foo.groovy",
            "import groovy.transform.TypeChecked\n"+
            "@TypeChecked\n"+
            "void method(String message) {\n"+
            "   if (rareCondition) {\n"+
            '        println \"Did you spot the error in this ${message.toUppercase()}?\"\n'+
            "   }\n"+
            "}"
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 4)\n" +
            "\tif (rareCondition) {\n" +
            "\t    ^"+(GroovyUtils.isAtLeastGroovy(20)?"^^^^^^^^^^^^":"")+"\n" +
            "Groovy:[Static type checking] - The variable [rareCondition] is undeclared.\n" +
            "----------\n" +
            "2. ERROR in Foo.groovy (at line 5)\n" +
            '\tprintln \"Did you spot the error in this ${message.toUppercase()}?\"\n' +
            "\t                                         ^"+(GroovyUtils.isAtLeastGroovy(20)?"^^^^^^^^^^^^^^^^^^^^^^":"")+"\n" +
            "Groovy:[Static type checking] - Cannot find matching method java.lang.String#toUppercase()"+(GroovyUtils.isAtLeastGroovy(20)?". Please check if the declared type is right and if the method exists.":"")+"\n" +
            "----------\n")
    }

    void testTypeChecked2() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        String[] sources = [
            "Foo.groovy",
            "import groovy.transform.TypeChecked\n"+
            "@TypeChecked\n"+
            "void method(String message) {\n"+
            "   List<Integer> ls = new ArrayList<Integer>();\n"+
            "   ls.add(123);\n"+
            "   ls.add('abc');\n"+
            "}"
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 6)\n" +
            "\tls.add(\'abc\');\n" +
            "\t^" + (GroovyUtils.isAtLeastGroovy(20) ? "^^^^^^^^^^^^" : "") + "\n" +
            (GroovyUtils.isAtLeastGroovy(23) ? "Groovy:[Static type checking] - Cannot call java.util.ArrayList <Integer>#add(java.lang.Integer) with arguments [java.lang.String] ":
            "Groovy:[Static type checking] - Cannot find matching method java.util.ArrayList#add(java.lang.String)" + (GroovyUtils.isAtLeastGroovy(20) ? ". Please check if the declared type is right and if the method exists." : "")) + "\n" +
            "----------\n")
    }

    /**
     * Testing the code in the StaticTypeCheckingSupport.checkCompatibleAssignmentTypes.
     *
     * That method does a lot of equality by == testing against classnode constants, which doesn't work so well for us...
     */
    void testCompileStatic2() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        String[] sources = [
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
        ]

        runConformTest(sources)
    }

    void testCompileStatic() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        String[] sources = [
            "Foo.groovy",
            "import groovy.transform.CompileStatic\n"+
            "@CompileStatic\n"+
            "void method(String message) {\n"+
            "   List<Integer> ls = new ArrayList<Integer>();\n"+
            "   ls.add(123);\n"+
            "   ls.add('abc');\n"+
            "}"
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 6)\n" +
            "\tls.add(\'abc\');\n" +
            "\t^"+(GroovyUtils.isAtLeastGroovy(20)?"^^^^^^^^^^^^":"")+"\n" +
            (GroovyUtils.isAtLeastGroovy(23)?
            "Groovy:[Static type checking] - Cannot call java.util.ArrayList <Integer>#add(java.lang.Integer) with arguments [java.lang.String] \n":
            "Groovy:[Static type checking] - Cannot find matching method java.util.ArrayList#add(java.lang.String)"+(GroovyUtils.isAtLeastGroovy(20)?". Please check if the declared type is right and if the method exists.":""))+(GroovyUtils.isAtLeastGroovy(23)?"":"\n") +
            "----------\n")
    }

    void testCompileStatic3() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        String[] sources = [
            "Foo.groovy",
            "import groovy.transform.CompileStatic;\n"+
            "\n"+
            "@CompileStatic void test() {\n"+
            "   int littleInt = 3\n"+
            "   Integer objectInt = littleInt\n"+
            "}\n"
        ]

        runConformTest(sources)
    }

    void testCompileStatic_1511() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        String[] sources = [
            "Foo.groovy",
            "@groovy.transform.CompileStatic\n"+
            "def meth() {\n"+
            "   List<String> second = []\n"+
            "   List<String> artefactResources2 = []\n"+
            "   second.addAll(artefactResources2)\n"+
            "   println 'abc'\n"+
            "}\n"+
            "meth();"
        ]

        runConformTest(sources, "abc")
    }

    void testCompileStatic_1505() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        String[] sources = [
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
        ]

        runConformTest(sources, "abc")
    }

    void _testCompileStatic_1506() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        String[] sources = [
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
        ]

        runConformTest(sources)
    }

    void testCompileStatic4() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        // verify generics are correct for the 'Closure<?>' as CompileStatic will attempt an exact match
        String[] sources = [
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
        ]

        runConformTest(sources)
    }

    void testCompileDynamic() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return

        String[] sources = [
            'A.groovy', '''
            @groovy.transform.CompileStatic
            class A {
                int prop
                int computeStatic(int input) {
                    prop + input
                }
                @groovy.transform.CompileDynamic
                int computeDynamic(int input) {
                    missing(prop, input)
                }
            }
            '''
        ]

        runConformTest(sources)
    }

    void testTransforms_BasicLogging() {
        Map options = getCompilerOptions()
        options.put(CompilerOptions.OPTIONG_GroovyClassLoaderPath, FileLocator.resolve(Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry("astTransformations/transforms.jar")).getFile())
        options.put(CompilerOptions.OPTIONG_GroovyProjectName, "Test")

        // From: http://svn.codehaus.org/groovy/trunk/groovy/groovy-core/src/examples/transforms/local
        String[] sources = [
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
        ]

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
            null)
    }

    void testTransforms_AtLog() {
        // See
        // https://jira.codehaus.org/browse/GRECLIPSE-1503
        // https://jira.codehaus.org/browse/GROOVY-5736
        String[] sources = [
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
        ]

        runConformTest(sources)
    }

    void testJDTClassNode_1731() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return

        // Testcode based on article: http://www.infoq.com/articles/groovy-1.5-new
        // The groups of tests are loosely based on the article contents - but what is really exercised here is the accessibility of
        // the described constructs across the Java/Groovy divide.

        String[] sources = [
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
        ]

        runConformTest(sources, "@a.SampleAnnotation()")
    }

    void testImmutable_1723() {
        String[] sources = [
            "c/Main.java",
            "package c;\n" +
            "public class Main {\n" +
            "    public static void main(String[] args) {" +
            "    }\n" +
            "}\n",

            "a/SomeId.groovy",
            "package a;\n" +
            "import groovy.transform.Immutable\n" +
            "@Immutable\n" +
            "class SomeId {\n" +
            "    UUID id\n" +
            "}\n",

            "b/SomeValueObject.groovy",
            "package b;\n" +
            "import groovy.transform.Immutable\n" +
            "import a.SomeId\n" +
            "@Immutable\n" +
            "class SomeValueObject {\n" +
            "    SomeId id\n" +
            "}\n"
        ]

        runConformTest(sources)

        GroovyCompilationUnitDeclaration unit = getCUDeclFor("SomeValueObject.groovy")
        ClassNode classNode = unit.getCompilationUnit().getClassNode("b.SomeValueObject")
        FieldNode field = classNode.getField("id")
        ClassNode type = field.getType()
        List annotations = type.getAnnotations(ClassHelper.make(groovy.transform.Immutable))
        assertEquals(1, annotations.size())
    }

    void testTransforms_Gaelyk() {
        // See https://jira.codehaus.org/browse/GRECLIPSE-1639
        if (isJRELevel(AbstractCompilerTest.F_1_8) || isJRELevel(AbstractCompilerTest.F_1_7)) {
            return
        }
        float classVersion = Float.parseFloat(System.getProperty("java.class.version"))
        if (classVersion < 51.0f) {
            System.out.println("TEST DISABLED: Gaelyk requires a java.class.version of 51.0 or greater. This JRE is java.class.version " + classVersion +
                    "\nand you are running Java version " + System.getProperty("java.version"))
            return
        }
        Map options = getCompilerOptions()
        String[] defaultClassPaths = getDefaultClassPaths()
        String[] augmented = new String[defaultClassPaths.length + 2]
        System.arraycopy(defaultClassPaths, 0, augmented, 0,
                defaultClassPaths.length)
        augmented[augmented.length-1] = FileLocator.resolve(Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry("gaelyk-2.0.jar")).getFile()
        augmented[augmented.length-2] = FileLocator.resolve(Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry("appengine-api-1.0-sdk-1.8.0.jar")).getFile()

        options.put(CompilerOptions.OPTIONG_GroovyClassLoaderPath, FileLocator.resolve(Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry("gaelyk-2.0.jar")).getFile()
                + File.pathSeparator + FileLocator.resolve(Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry("appengine-api-1.0-sdk-1.8.0.jar")).getFile())
        options.put(CompilerOptions.OPTIONG_GroovyProjectName, "Test")

        String[] sources = [
            "Foo.groovy",
            "import groovyx.gaelyk.datastore.Entity\r\n" +
            "@Entity\n" +
            "class Avatar{}\n" +
            "println 'done'"
        ]

        runConformTest(sources, "done", augmented, true, null, options, null)
    }
}
