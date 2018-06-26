/*
 * Copyright 2009-2018 the original author or authors.
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
package org.eclipse.jdt.groovy.core.tests.xform;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assume.assumeTrue;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.CompileStatic} and {@link groovy.transform.CompileDynamic}.
 */
public final class StaticCompilationTests extends GroovyCompilerTestSuite {

    @Test
    public void testCompileStatic1() {
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.CompileStatic\n"+
            "@CompileStatic\n"+
            "void method(String message) {\n"+
            "   List<Integer> ls = new ArrayList<Integer>();\n"+
            "   ls.add(123);\n"+
            "   ls.add('abc');\n"+
            "}\n",
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 6)\n" +
            "\tls.add(\'abc\');\n" +
            "\t^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call java.util.ArrayList <Integer>#add(java.lang.Integer) with arguments [java.lang.String] \n" +
            "----------\n");
    }

    /**
     * Testing the code in the StaticTypeCheckingSupport.checkCompatibleAssignmentTypes.
     *
     * That method does a lot of == testing against ClassNode constants, which may not work so well for us.
     */
    @Test
    public void testCompileStatic2() {
        String[] sources = {
            "One.groovy",
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
            "   }\n"+
            "}\n",
        };

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic3() {
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.CompileStatic;\n"+
            "\n"+
            "@CompileStatic void test() {\n"+
            "   int littleInt = 3\n"+
            "   Integer objectInt = littleInt\n"+
            "}\n",
        };

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic4() {
        // verify generics are correct for the 'Closure<?>' as CompileStatic will attempt an exact match
        String[] sources = {
            "A.groovy",
            "class A {\n"+
            "  public void profile(String name, groovy.lang.Closure<?> callable) { }\n"+
            "}\n",

            "B.groovy",
            "@groovy.transform.CompileStatic\n"+
            "class B extends A {\n"+
            "  def foo() {\n"+
            "    profile('creating plugin manager with classes') {\n"+
            "      println 'abc'\n"+
            "    }\n"+
            "  }\n"+
            "}\n",
        };

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic5() {
        String[] sources = {
            "FlowTyping.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class FlowTyping {\n" +
            "  private Number number\n" +
            "  BigDecimal method() {\n" +
            "    return (number == null || number instanceof BigDecimal) \\\n" +
            "      ? (BigDecimal) number : new BigDecimal(number.toString())\n" +
            "  }\n" +
            "}\n",
        };

        runNegativeTest(sources, "");
    }

    @Test // GROOVY-8337
    public void testCompileStatic6() {
        String[] sources = {
            "FlowTyping.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class FlowTyping {\n" +
            "  private Number number;\n" +
            "  private BigDecimal method() {\n" +
            "    return (number == null || number instanceof BigDecimal) ? number : new BigDecimal(number.toString());\n" +
            "  }\n" +
            "}\n",
        };

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic7() {
        String[] sources = {
            "BridgeMethod.groovy",
            "@groovy.transform.CompileStatic\n" +
            "int compare(Integer integer) {\n" +
            "  if (integer.compareTo(0) == 0)\n" +
            "    return 0\n" +
            "  return 1\n" +
            "}\n",
        };

        runNegativeTest(sources, "");
    }

    @Test // GROOVY-8509
    public void testCompileStatic8() {
        String[] sources = {
            "p/Foo.groovy",
            "package p\n" +
            "class Foo {\n" +
            "  protected void m() {}\n" +
            "}\n",

            "p/Bar.groovy",
            "package p\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Bar {\n" +
            "  void testM(Foo f) {\n" +
            "    f.m()\n" +
            "  }\n" +
            "}\n",
        };

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic9() {
        assumeTrue(isAtLeastGroovy(25));

        String[] sources = {
            "q/Foo.groovy",
            "package q\n" +
            "class Foo {\n" +
            "  protected void m() {}\n" +
            "}\n",

            "r/Bar.groovy",
            "package r\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Bar {\n" +
            "  void testM(q.Foo f) {\n" +
            "    f.m()\n" +
            "  }\n" +
            "}\n",
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in r\\Bar.groovy (at line 5)\n" +
            "\tf.m()\n" +
            "\t^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method q.Foo#m(). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic1505() {
        String[] sources = {
            "DynamicQuery.groovy",
            "import groovy.transform.TypeChecked\n"+
            "@TypeChecked\n"+
            "class DynamicQuery {\n"+
            "  public static void main(String[]argv) {\n"+
            "    new DynamicQuery().foo(null);\n"+
            "  }\n"+
            "  private foo(Map sumpin) {\n"+
            "    Map foo\n"+
            "    foo.collect{ Map.Entry it -> it.key }\n"+
            "    print 'abc';\n"+
            "  }\n"+
            "}\n",
        };

        runConformTest(sources, "abc");
    }

    @Test @Ignore("VM argument not accepted on CI server")
    public void testCompileStatic1506() {
        String[] sources = {
            "LoggerTest.groovy",
            "import groovy.transform.*\n"+
            "import groovy.util.logging.*\n"+
            "@CompileStatic @Log\n"+
            "class LoggerTest {\n"+
            "  static void main(String... args) {\n"+
            "    LoggerTest.log.info('one')\n"+
            "    log.info('two')\n"+
            "  }\n"+
            "}\n",
        };
        vmArguments = new String[] {"-Djava.util.logging.SimpleFormatter.format=%4$s %5$s%6$s%n"};

        runConformTest(sources, "", "INFO one\nINFO two");
    }

    @Test
    public void testCompileStatic1511() {
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.CompileStatic\n"+
            "def meth() {\n"+
            "   List<String> second = []\n"+
            "   List<String> artefactResources2 = []\n"+
            "   second.addAll(artefactResources2)\n"+
            "   println 'abc'\n"+
            "}\n"+
            "meth();\n",
        };

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic1514() {
        String[] sources = {
            "C.groovy",
            "@SuppressWarnings('rawtypes')\n"+
            "@groovy.transform.CompileStatic\n"+
            "class C {\n"+
            "  def xxx(List list) {\n"+
            "    list.unique().each { }\n"+
            "  }\n"+
            "}\n",
        };

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic1515() {
        String[] sources = {
            "C.groovy",
            "import groovy.transform.CompileStatic;\n" +
            "import java.util.regex.Pattern\n" +
            "@CompileStatic\n" +
            "class C {\n" +
            "  void validate() {\n" +
            "    for (String validationKey : [:].keySet()) {\n" +
            "      String regex\n" +
            "      Pattern pattern = ~regex\n" + // NPE on this bitwise negation
            "    }\n" +
            "  }\n" +
            "}\n",
        };

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic1521() {
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.CompileStatic\n"+
            "class Foo {\n"+
            "  enum Status { ON, OFF }\n"+
            "}\n",
        };

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileDynamic() {
        String[] sources = {
            "A.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A {\n" +
            "  int prop\n" +
            "  int computeStatic(int input) {\n" +
            "    prop + input\n" +
            "  }\n" +
            "  @groovy.transform.CompileDynamic\n" +
            "  int computeDynamic(int input) {\n" +
            "    missing(prop, input)\n" +
            "  }\n" +
            "}\n",
        };

        runNegativeTest(sources, "");
    }
}
