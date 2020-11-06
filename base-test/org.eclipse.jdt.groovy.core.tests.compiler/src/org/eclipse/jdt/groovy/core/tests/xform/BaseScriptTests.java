/*
 * Copyright 2009-2020 the original author or authors.
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

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.BaseScript}.
 */
public final class BaseScriptTests extends GroovyCompilerTestSuite {

    @Test
    public void testBaseScript1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "print new GroovyShell().evaluate('''\n" +
            "  @BaseScript(Foo)\n" +
            "  import groovy.transform.BaseScript\n" +
            "  abstract class Foo extends Script {\n" +
            "    def test() { 'works' }\n" +
            "  }\n" +
            "  test()\n" +
            "''')\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testBaseScript2() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "abstract class Foo extends Script {\n" +
            "}\n",

            "Main.groovy",
            "@groovy.transform.BaseScript(Foo) foo\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 1)\n" +
            "\t@groovy.transform.BaseScript(Foo) foo\n" +
            "\t                             ^^^\n" +
            "Groovy:Annotation @BaseScript cannot have member 'value' if used on a declaration.\n" +
            "----------\n");
    }

    @Test
    public void testBaseScript3() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "print new GroovyShell().evaluate('''\n" +
            "  abstract class Foo extends Script {\n" +
            "    def test() { 'works' }\n" +
            "  }\n" +
            "  @groovy.transform.BaseScript Foo foo\n" +
            "  test()\n" +
            "''')\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testBaseScript4() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "print new GroovyShell().evaluate('''\n" +
            "  abstract class Foo extends Script {\n" +
            "    abstract def bar()\n" +
            "    def run() {\n" +
            "      'before' + bar() + '-after'\n" +
            "    }\n" +
            "  }\n" +
            "  @groovy.transform.BaseScript Foo foo\n" +
            "  return '-during'\n" +
            "''')\n",
        };
        //@formatter:on

        runConformTest(sources, "before-during-after");
    }

    @Test
    public void testBaseScript5() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "abstract class Foo {\n" +
            "}\n",

            "Main.groovy",
            "@groovy.transform.BaseScript Foo foo\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 1)\n" +
            "\t@groovy.transform.BaseScript Foo foo\n" +
            "\t^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:Declared type Foo -> Foo does not extend groovy.lang.Script class!\n" +
            "----------\n");
    }
}
