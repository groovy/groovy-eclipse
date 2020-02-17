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

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assume.assumeTrue;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.AutoFinal}.
 */
public final class AutoFinalTests extends GroovyCompilerTestSuite {

    @Before
    public void setUp() {
        assumeTrue(isAtLeastGroovy(25));
    }

    @Test
    public void testAutoFinal1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "new Pogo().test(true)\n",

            "Pogo.groovy",
            "@groovy.transform.AutoFinal\n" +
            "class Pogo {\n" +
            "  String one, two\n" +
            "  void test(boolean flag) {\n" +
            "    flag = false\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Pogo.groovy (at line 5)\n" +
            "\tflag = false\n" +
            "\t^^^^^^^^^^^^\n" +
            "Groovy:The parameter [flag] is declared final but is reassigned\n" +
            "----------\n");
    }

    @Test
    public void testAutoFinal2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "new Pogo().test(true)\n",

            "Pogo.groovy",
            "@groovy.transform.AutoFinal\n" +
            "class Pogo {\n" +
            "  String one, two\n" +
            "  @groovy.transform.AutoFinal(enabled=false)\n" +
            "  void test(boolean flag) {\n" +
            "    flag = false\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }
}
