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
import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser;
import static org.junit.Assume.assumeTrue;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.RecordType}.
 */
public final class RecordTypeTests extends GroovyCompilerTestSuite {

    @Before
    public void setUp() {
        assumeTrue(isAtLeastGroovy(40));
    }

    @Test
    public void testRecordType1() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import static java.lang.reflect.Modifier.isFinal\n" +
            "assert isFinal(Simple.class.modifiers)\n" +
            "def obj = new Simple(1,'x')\n" +
            "print obj.n()\n" +
            "print obj.s\n" +
            "print obj\n",

            "Simple.groovy",
            "@groovy.transform.RecordType\n" +
            "class Simple {\n" +
            "  Integer n\n" +
            "  String s\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "1xSimple[n=1, s=x]");
    }

    @Test
    public void testRecordType2() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import static java.lang.reflect.Modifier.isFinal\n" +
            "assert isFinal(Simple.class.modifiers)\n" +
            "def obj = new Simple(1,'x')\n" +
            "print obj.n()\n" +
            "print obj.s\n" +
            "print obj\n",

            "Simple.groovy",
            "record Simple(Integer n, String s) {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "1xSimple[n=1, s=x]");
    }
}
