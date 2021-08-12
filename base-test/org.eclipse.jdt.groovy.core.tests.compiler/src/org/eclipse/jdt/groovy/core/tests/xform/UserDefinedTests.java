/*
 * Copyright 2009-2021 the original author or authors.
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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Test;

/**
 * Test cases for user-defined AST transformations.
 */
public final class UserDefinedTests extends GroovyCompilerTestSuite {

    @Test
    public void testWithLogging() throws Exception {
        java.net.URL bundleEntry = Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry("astTransformations/transforms.jar");
        cpAdditions = new String[] {FileLocator.toFileURL(bundleEntry).getPath()};

        //@formatter:off
        String[] sources = {
            "LoggingExample.groovy",
            "void greet() {\n" +
            "  println 'one'\n" +
            "}\n" +
            "\n" +
            "@examples.local.WithLogging // this should trigger extra logging\n" +
            "void greetWithLogging() {\n" +
            "  println 'two'\n" +
            "}\n" +
            "\n" +
            "greet()\n" +
            "\n" +
            "greetWithLogging()\n",
        };
        //@formatter:on

        runConformTest(sources, "one\nStarting greetWithLogging\ntwo\nEnding greetWithLogging");
    }
}
