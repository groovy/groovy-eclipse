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
 * Test cases for {@link groovy.transform.NamedVariant}, et al.
 */
public final class NamedVariantTests extends GroovyCompilerTestSuite {

    @Test
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
            "print m(g:12, b:42, r:12)",

            "Color.groovy",
            "@groovy.transform.ToString(includeNames=true)\n" +
            "class Color {\n" +
            "  Integer r, g, b\n" +
            "}\n",
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
            "print m(r:1, g:2, b:3, a: 0)",

            "Color.groovy",
            "@groovy.transform.ToString(includeNames=true)\n" +
            "class Color {\n" +
            "  Integer r, g, b\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Color(r:1, g:2, b:3) 0");
    }

    @Test
    public void testNamedVariant3() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "print(new Color(g:12, b:42, r:12))",

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
}
