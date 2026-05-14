/*
 * Copyright 2009-2026 the original author or authors.
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

import static org.junit.Assume.assumeTrue;

import org.junit.Before;
import org.junit.Test;

public final class Java9Tests extends GroovyCompilerTestSuite {

    @Before
    public void setUp() {
        assumeTrue(isAtLeastJava(JDK9));
    }

    @Test
    public void testModuleInfo() {
        //@formatter:off
        String[] sources = {
            "main.groovy",
            "print 'works'\n",

            "module-info.java",
            "module test.project {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testPrivateInterfaceMethod() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C implements I {\n" +
            "  static main(args) {\n" +
            "    I i = new C()\n" +
            "    i.proc()\n" +
            "  }\n" +
            "}\n",

            "I.java",
            "public interface I {\n" +
            "  default void proc() {\n" +
            "    System.out.print(secret());\n" +
            "  }\n" +
            "  private String secret() {\n" +
            "    return \"works\";\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }
}
