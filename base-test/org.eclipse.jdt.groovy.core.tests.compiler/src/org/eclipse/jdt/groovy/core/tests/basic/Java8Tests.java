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
package org.eclipse.jdt.groovy.core.tests.basic;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assume.assumeTrue;

import org.junit.Before;
import org.junit.Test;

public final class Java8Tests extends GroovyCompilerTestSuite {

    @Before
    public void setUp() {
        assumeTrue(isAtLeastJava(JDK8));
    }

    @Test
    public void testDefaultAndStaticMethodInInterface() {
        //@formatter:off
        String[] sources = {
            "p/IExample.java",
            "package p;\n" +
            "public interface IExample {\n" +
            "  void testExample();\n" +
            "  static void callExample() {}\n" +
            "  default void callDefault() {}\n" +
            "}\n",

            "p/Example.groovy",
            "package p\n" +
            "class Example implements IExample {\n" +
            "  public void testExample() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testFunctionalInterfaceCoercion() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Foo {\n" +
            "  String bar\n" +
            "  def baz() {\n" +
            "    Collection<Foo> coll = []\n" +
            "    coll.removeIf { it.bar == null }\n" + // Closure should coerce to SAM type java.util.function.Predicate
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testRepeatableAnnotation1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@Annos([\n" +
            "  @Anno('one'),\n" +
            "  @Anno('two')\n" +
            "])\n" +
            "class Main {\n" +
            "}\n",

            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "\n" +
            "@Retention(RetentionPolicy.CLASS)\n" +
            "@Repeatable(Annos.class)\n" +
            "@interface Anno {\n" +
            "  String value();\n" +
            "}\n" +
            "@Retention(RetentionPolicy.CLASS)\n" +
            "@interface Annos {\n" +
            "  Anno[] value();\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        checkGCUDeclaration("Main.groovy",
            "public @Annos({@Anno(\"one\"), @Anno(\"two\")}) class Main {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "Main() {\n" +
            "  }\n" +
            "}\n");

        checkDisassemblyFor("Main.class",
            "@Annos(value={@Anno(value=\"one\"),@Anno(value=\"two\")})\n" +
            "public class Main implements groovy.lang.GroovyObject {\n");
    }

    @Test
    public void testRepeatableAnnotation2() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@Anno('one')\n" +
            "@Anno('two')\n" +
            "class Main {\n" +
            "}\n",

            "Anno.java",
            "import java.lang.annotation.*;\n" +
            "\n" +
            "@Retention(RetentionPolicy.CLASS)\n" +
            "@Repeatable(Annos.class)\n" +
            "@interface Anno {\n" +
            "  String value();\n" +
            "}\n" +
            "@Retention(RetentionPolicy.CLASS)\n" +
            "@interface Annos {\n" +
            "  Anno[] value();\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        checkGCUDeclaration("Main.groovy",
            "public @Anno(\"one\") @Anno(\"two\") class Main {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "Main() {\n" +
            "  }\n" +
            "}\n");

        checkDisassemblyFor("Main.class",
            "@Annos(value={@Anno(value=\"one\"),@Anno(value=\"two\")})\n" +
            "public class Main implements groovy.lang.GroovyObject {\n");
    }

    @Test
    public void testRepeatableAnnotation3() {
        //@formatter:off
        String[] sources = {
            "Anno.groovy",
            "import java.lang.annotation.*\n" +
            "\n" +
            "@Repeatable(Annos.class)\n" +
            "@interface Anno {\n" +
            "  String value()\n" +
            "}\n" +
            "@interface Annos {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Anno.groovy (at line 3)\n" +
            "\t@Repeatable(Annos.class)\n" +
            "\t            ^^^^^^^^^^^\n" +
            "The container annotation type @Annos must declare a member value()\n" +
            "----------\n");
    }

    @Test
    public void testNonRepeatableAnnotation() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@Anno('one')\n" +
            "@Anno('two')\n" +
            "class Main {\n" +
            "}\n",

            "Anno.java",
            "@interface Anno {\n" +
            "  String value();\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 1)\n" +
            "\t@Anno('one')\n" +
            "\t^^^^^^^^^^^^\n" +
            "Duplicate annotation of non-repeatable type @Anno. Only annotation types marked @Repeatable can be used multiple times at one target.\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 2)\n" +
            "\t@Anno('two')\n" +
            "\t^^^^^^^^^^^^\n" +
            "Duplicate annotation of non-repeatable type @Anno. Only annotation types marked @Repeatable can be used multiple times at one target.\n" +
            "----------\n");
    }
}
