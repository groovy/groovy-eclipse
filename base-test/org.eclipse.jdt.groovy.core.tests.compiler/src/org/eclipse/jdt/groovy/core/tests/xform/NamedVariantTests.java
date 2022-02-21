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

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.NamedVariant}, et al.
 */
public final class NamedVariantTests extends GroovyCompilerTestSuite {

    private static final String COLOR_CLASS =
        "@groovy.transform.ToString(includeNames=true)\n" +
        "class Color {\n" +
        "  int r, g, b\n" +
        "}\n";

    @Test // GROOVY-9183
    public void testNamedVariant1() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import groovy.transform.*\n" +
            "\n" +
            "@NamedVariant\n" +
            "String m(@NamedDelegate Color c) {\n" +
            "  return c\n" +
            "}\n" +
            "\n" +
            "print m(g:12, b:42, r:12)\n",

            "Color.groovy",
            COLOR_CLASS,
        };
        //@formatter:on

        runConformTest(sources, "Color(r:12, g:12, b:42)");
    }

    @Test
    public void testNamedVariant2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import groovy.transform.*\n" +
            "\n" +
            "@NamedVariant\n" +
            "String m(@NamedDelegate Color color, @NamedParam(value='a', required=true) int alpha) {\n" +
            "  return [color, alpha].join(' ')\n" +
            "}\n" +
            "\n" +
            "print m(r:1, g:2, b:3, a: 0)\n",

            "Color.groovy",
            COLOR_CLASS,
        };
        //@formatter:on

        runConformTest(sources, "Color(r:1, g:2, b:3) 0");
    }

    @Test
    public void testNamedVariant3() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import groovy.transform.*\n" +
            "\n" +
            "@NamedVariant\n" +
            "String m(@NamedDelegate Color color, @NamedParam(value='a', type=Number) alpha) {\n" +
            "  return [color, alpha].join(' ')\n" +
            "}\n" +
            "print m(r:1, g:2, b:3, a: 0.0)\n",

            "Color.groovy",
            COLOR_CLASS,
        };
        //@formatter:on

        runConformTest(sources, "Color(r:1, g:2, b:3) 0.0");
    }

    @Test
    public void testNamedVariant4() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import groovy.transform.*\n" +
            "\n" +
            "@NamedVariant\n" +
            "String m(@NamedDelegate Color color, Number alpha/*=4.5*/) {\n" + // TODO: GROOVY-10498
            "  return [color, alpha].join(' ')\n" +
            "}\n" +
            "print m(r:1, g:2, b:3, 0.0)\n",

            "Color.groovy",
            COLOR_CLASS,
        };
        //@formatter:on

        runConformTest(sources, "Color(r:1, g:2, b:3) 0.0");
    }

    @Test // GROOVY-9158, GROOVY-10176, GROOVY-10484
    public void testNamedVariant5() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import groovy.transform.*\n" +
            "\n" +
            "@NamedVariant(autoDelegate=false)\n" +
            "String m(Color color, int alpha = 0) {\n" + // color:required, alpha:optional
            "  return [color, alpha].join(' ')\n" +
            "}\n" +
            "\n" +
            "@groovy.transform.TypeChecked void test() {\n" +
            "  print m(color: new Color(r:1,g:2,b:3))\n" +
            "}\n" +
            "test()\n",

            "Color.groovy",
            COLOR_CLASS,
        };
        //@formatter:on

        runConformTest(sources, "Color(r:1, g:2, b:3) 0");
    }

    @Test // GROOVY-10261
    public void testNamedVariant6() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import groovy.transform.*\n" +
            "\n" +
            "@NamedVariant\n" +
            "Color color(int r=10, int g=20, int b=30) {\n" +
            "  new Color(r:r, g:g, b:b)\n" +
            "}\n" +
            "print color()\n" +
            "print color(r:128,b:128)\n",

            "Color.groovy",
            COLOR_CLASS,
        };
        //@formatter:on

        runConformTest(sources, "Color(r:10, g:20, b:30)Color(r:128, g:20, b:128)");
    }

    @Test
    public void testNamedVariant7() {
        assumeTrue(isAtLeastGroovy(30));

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import groovy.transform.*\n" +
            "\n" +
            "@NamedVariant(coerce=true)\n" +
            "Color color(int r=10, int g=20, int b=30) {\n" +
            "  new Color(r:r, g:g, b:b)\n" +
            "}\n" +
            "print color()\n" +
            "print color(r:128,b:'128')\n",

            "Color.groovy",
            COLOR_CLASS,
        };
        //@formatter:on

        runConformTest(sources, "Color(r:10, g:20, b:30)Color(r:128, g:20, b:128)");
    }

    @Test
    public void testNamedVariant8() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import groovy.transform.*\n" +
            "\n" +
            "@NamedVariant\n" +
            "void test(@NamedDelegate Color c, @NamedParam int b) {\n" +
            "}\n" +
            "test([:])\n",

            "Color.groovy",
            COLOR_CLASS,
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 3)\n" +
            "\t@NamedVariant\n" +
            "\t^\n" +
            "Groovy:Error during @NamedVariant processing. Duplicate property 'b' found.\n" +
            "----------\n");
    }

    @Test
    public void testNamedVariant9() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "print(new Color(g:12, b:42, r:12))\n",

            "Color.groovy",
            "import groovy.transform.*\n" +
            "import groovy.transform.options.*\n" +
            "\n" +
            "@ToString(includeNames=true)\n" +
            "class Color {\n" +
            "  final Integer r, g, b\n" +
            "  \n" +
            "  @NamedVariant @VisibilityOptions(Visibility.PUBLIC)\n" +
            "  private Color(@NamedParam Integer r, @NamedParam Integer g, @NamedParam Integer b) {\n" +
            "    this.r = r\n" +
            "    this.g = g\n" +
            "    this.b = b\n" +
            "  }\n" +
            "  \n" +
            "  public static final Color BLACK = new Color(0, 0, 0)\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Color(r:12, g:12, b:42)");
    }

    @Test // GROOVY-10497
    public void testNamedVariant10() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import groovy.transform.*\n" +
            "\n" +
            "@NamedVariant\n" +
            "void test(int i, int j = 42) {\n" +
            "  print \"$i $j\"\n" +
            "}\n" +
            "test(i:0,j:null)\n",
        };
        //@formatter:on

        runConformTest(sources, "0 0");
    }

    @Test // GROOVY-10497
    public void testNamedVariant11() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import groovy.transform.*\n" +
            "\n" +
            "@NamedVariant\n" +
            "void test(int i, Integer j = 42) {\n" +
            "  print \"$i $j\"\n" +
            "}\n" +
            "test(i:0,j:null)\n",
        };
        //@formatter:on

        runConformTest(sources, "0 null");
    }

    @Test // GROOVY-10500
    public void testNamedVariant12() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import groovy.transform.*\n" +
            "\n" +
            "@NamedVariant(autoDelegate=true)\n" +
            "void test(Pogo p) {\n" +
            "  print p\n" +
            "}\n" +
            "test([:])\n",

            "Pogo.groovy",
            "@groovy.transform.ToString\n" +
            "class Pogo {\n" +
            "  private static V = 42\n" +
            "  Number n = V\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Pogo(42)");
    }
}
