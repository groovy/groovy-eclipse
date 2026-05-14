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
package org.eclipse.jdt.groovy.core.tests.xform;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assume.assumeTrue;

import java.util.Map;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.TupleConstructor}.
 */
public final class TupleConstructorTests extends GroovyCompilerTestSuite {

    @Test // https://github.com/groovy/groovy-eclipse/issues/421
    public void testTupleConstructor1() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Foo(\"one\", \"two\"));\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@groovy.transform.TupleConstructor\n" +
            "@groovy.transform.ToString\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)");
    }

    @Test
    public void testTupleConstructor2() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Foo(\"one\", \"two\"));\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "import groovy.transform.*\n" +
            "@TupleConstructor @ToString\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)");
    }

    @Test
    public void testTupleConstructor3() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Foo(\"one\", \"two\"));\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "import groovy.transform.TupleConstructor as POGO\n" +
            "import groovy.transform.*\n" +
            "@POGO @ToString\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)");
    }

    @Test
    public void testTupleConstructor4() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    normal 'groovy.transform.TupleConstructor'\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Foo(\"one\", \"two\"));\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@groovy.transform.ToString\n" +
            "@TupleConstructor\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)", options);
    }

    @Test
    public void testTupleConstructor5() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    alias 'POGO', groovy.transform.TupleConstructor\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Foo(\"one\", \"two\"));\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@groovy.transform.ToString\n" +
            "@POGO\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)", options);
    }

    @Test
    public void testTupleConstructor6() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  ast(groovy.transform.TupleConstructor)\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "Main.groovy", // TODO: java
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Foo(\"one\", \"two\"));\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@groovy.transform.ToString\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)", options);
    }

    @Test
    public void testTupleConstructor7() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Foo(\"one\"));\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@groovy.transform.TupleConstructor\n" +
            "@groovy.transform.ToString\n" +
            "class Foo {\n" +
            "  String bar\n" +
            "  public baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one)");
    }

    @Test
    public void testTupleConstructor8() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Foo(\"one\", \"two\"));\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@groovy.transform.TupleConstructor(includeFields=true)\n" +
            "@groovy.transform.ToString(includeFields=true)\n" +
            "class Foo {\n" +
            "  String bar\n" +
            "  public baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)");
    }

    @Test
    public void testTupleConstructor9() {
        //@formatter:off
        String[] sources = {
            "Main.groovy", // TODO: java
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Foo(\"one\", \"two\"));\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "class C {\n" +
            "  String bar\n" +
            "}\n" +
            "@groovy.transform.TupleConstructor(includeSuperProperties=true)\n" +
            "@groovy.transform.ToString(includeNames=true, includeSuperProperties=true)\n" +
            "class Foo extends C {\n" +
            "  String baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(baz:two, bar:one)");
    }

    @Test
    public void testTupleConstructor10() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Foo(\"one\", \"two\"));\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@groovy.transform.TupleConstructor(allProperties=true)\n" +
            "@groovy.transform.ToString(includeFields=true)\n" +
            "class Foo {\n" +
            "  String bar\n" +
            "  private baz\n" +
            "  void setBaz(value) {\n" +
            "    this.@baz = value\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)");
    }

    @Test
    public void testTupleConstructor11() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor(includeFields=true)\n" +
            "class Person {\n" +
            "  String firstName = 'John'\n" +
            "  private String lastName = 'Doe'\n" +
            "  String getLastName() { lastName }\n" +
            "}\n" +
            "def p = new Person()\n" +
            "assert p.firstName == 'John'\n" +
            "assert p.lastName == 'Doe'\n" +
            "p = new Person('Jane')\n" +
            "assert p.firstName == 'Jane'\n" +
            "assert p.lastName == 'Doe'\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTupleConstructor12() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor(force=true)\n" +
            "class Person {\n" +
            "  String firstName, lastName\n" +
            "  Person(Person that) {\n" +
            "    this.firstName = that.firstName\n" +
            "  }\n" +
            "}\n" +
            "def p = new Person('John', 'Doe')\n" +
            "assert p.firstName == 'John'\n" +
            "assert p.lastName == 'Doe'\n" +
            "p = new Person(p)\n" +
            "assert p.firstName == 'John'\n" +
            "assert p.lastName == null\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTupleConstructor13() {
        assumeTrue(isAtLeastGroovy(40)); // "defaultsMode" added for Groovy 4
        for (String value : new String[] {"groovy.transform.DefaultsMode.OFF", "DefaultsMode.OFF", "OFF"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "def p = new Person('Jane','Eyre')\n" +
                "assert p.firstName == 'Jane'\n" +
                "assert p.lastName == 'Eyre'\n",

                "Person.groovy",
                "import groovy.transform.DefaultsMode\n" +
                "import static groovy.transform.DefaultsMode.*\n" +
                "@groovy.transform.TupleConstructor(defaultsMode="+value+")\n" +
                "class Person {\n" +
                "  String firstName, lastName\n" +
                "}\n",
            };
            //@formatter:on

            runConformTest(sources);

            checkGCUDeclaration("Person.groovy", // expect single constructor
                "class Person {\n" +
                "  private String firstName;\n" +
                "  private String lastName;\n" +
                "  public @groovy.transform.Generated Person(String firstName, String lastName) {\n" +
                "  }\n" +
                "}\n");
        }
    }
}
