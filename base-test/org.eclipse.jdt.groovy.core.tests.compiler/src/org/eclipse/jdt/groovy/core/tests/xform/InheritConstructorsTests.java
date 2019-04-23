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

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.InheritConstructors}.
 */
public final class InheritConstructorsTests extends GroovyCompilerTestSuite {

    @Test
    public void testInheritConstructors1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "new Two('foo')",

            "One.groovy",
            "class One {\n" +
            "  One(String s) {\n" +
            "    print s\n" +
            "  }\n" +
            "}\n",

            "Two.groovy",
            "@groovy.transform.InheritConstructors\n" +
            "class Two extends One {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "foo");
    }
}
