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
package org.eclipse.jdt.groovy.core.tests.xform;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;

import java.util.Map;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.Canonical}, at al.
 */
public final class CanonicalTests extends GroovyCompilerTestSuite {

    @Test @Ignore("https://github.com/groovy/groovy-eclipse/issues/421")
    public void testCanonical1() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String... args) {\n" +
            "    System.out.print(new Foo(\"one\", \"two\"));\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@groovy.transform.Canonical\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)");
    }

    @Test
    public void testCanonical2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static void main(args) {\n" +
            "    print(new Foo('one', 'two'))\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@groovy.transform.Canonical\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)");
    }

    @Test
    public void testCanonical3() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static void main(args) {\n" +
            "    print(new Foo('one', 'two'))\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "import groovy.transform.Canonical\n" +
            "@Canonical\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)");
    }

    @Test
    public void testCanonical4() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    normal 'groovy.transform.Canonical'\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static void main(args) {\n" +
            "    print(new Foo('one', 'two'))\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@Canonical\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)", options);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/957
    public void testCanonical5() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static void main(args) {\n" +
            "    print(new Foo('bar'))\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@groovy.transform.Canonical(excludes='baz')\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(bar)");
    }

    @Test
    public void testCanonical6() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.Canonical(doesNotExist=null)\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 1)\n" +
            (isAtLeastGroovy(25)
                ?
                "\t@groovy.transform.Canonical(doesNotExist=null)\n" +
                "\t^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
                "Groovy:Annotation collector got unmapped names [doesNotExist]. @ line 1, column 1.\n"
                :
                "\t@groovy.transform.Canonical(doesNotExist=null)\n" +
                "\t^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
                "Groovy:'doesNotExist'is not part of the annotation Canonical in @groovy.transform.Canonical\n" +
                "----------\n" +
                "2. ERROR in Foo.groovy (at line 1)\n" +
                "\t@groovy.transform.Canonical(doesNotExist=null)\n" +
                "\t                            ^^^^^^^^^^^^^^^^^^\n" +
                "The attribute doesNotExist is undefined for the annotation type Canonical\n" +
                "----------\n" +
                "3. ERROR in Foo.groovy (at line 1)\n" +
                "\t@groovy.transform.Canonical(doesNotExist=null)\n" +
                "\t                                         ^^^^\n" +
                "Groovy:Unexpected type java.lang.Object in @groovy.transform.Canonical\n"
            ) +
            "----------\n");
    }
}
