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

import java.util.Map;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.Canonical}, at al.
 */
public final class CanonicalTests extends GroovyCompilerTestSuite {

    @Test
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
}
