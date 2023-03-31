/*
 * Copyright 2009-2023 the original author or authors.
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
 * Test cases for {@link groovy.transform.NullCheck}.
 */
public final class NullCheckTests extends GroovyCompilerTestSuite {

    @Test
    public void testNullCheck0() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "new Pogo().test('works')\n",

            "Pogo.groovy",
            "@groovy.transform.NullCheck\n" +
            "class Pogo {\n" +
            "  void test(whatever) {\n" +
            "    print whatever\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works", "");
    }

    @Test
    public void testNullCheck1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "new Pogo().test(null)\n",

            "Pogo.groovy",
            "@groovy.transform.NullCheck\n" +
            "class Pogo {\n" +
            "  void test(whatever) {\n" +
            "    print whatever\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "", "java.lang.IllegalArgumentException: whatever cannot be null");
    }

    @Test
    public void testNullCheck2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "new Pogo().test(null)\n",

            "Pogo.groovy",
            "class Pogo {\n" +
            "  @groovy.transform.NullCheck\n" +
            "  void test(whatever) {\n" +
            "    print whatever\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "", "java.lang.IllegalArgumentException: whatever cannot be null");
    }

    @Test // GROOVY-10178
    public void testNullCheck3() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "new Pogo().test(null)\n",

            "Pogo.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Pogo {\n" +
            "  @groovy.transform.NullCheck\n" +
            "  void test(whatever) {\n" +
            "    print whatever\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "", "java.lang.IllegalArgumentException: whatever cannot be null");
    }

    @Test
    public void testNullCheck4() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "new Pogo(null)\n",

            "Pogo.groovy",
            "class Pogo {\n" +
            "  @groovy.transform.NullCheck\n" +
            "  Pogo(whatever) {\n" +
            "    print whatever\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "", "java.lang.IllegalArgumentException: whatever cannot be null");
    }
}
