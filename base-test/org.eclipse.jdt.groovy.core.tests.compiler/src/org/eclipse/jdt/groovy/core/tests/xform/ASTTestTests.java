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

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.ASTTest}.
 */
public final class ASTTestTests extends GroovyCompilerTestSuite {

    @Test
    public void testASTTest1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.ASTTest(value={\n" +
            "  Map<String, Integer> map = null\n" +
            "})\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testASTTest2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.ASTTest(value={\n" +
            "  assert true\n" +
            "})\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testASTTest3() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.ASTTest(value={\n" +
            "  assert false\n" +
            "})\n" +
            "class Main {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 1)\n" +
            "\t@groovy.transform.ASTTest(value={\n" +
            "\t^" + (isAtLeastGroovy(40) ? "" : "^^^^^^^^^^^^^^^^^^^^^^^^") + "\n" +
            "Groovy: ASTTest exception: assert false\n" +
            "----------\n");
    }

    @Test
    public void testASTTest4() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import org.codehaus.groovy.ast.*\n" +
            "@groovy.transform.ASTTest(value={\n" +
            "  assert node instanceof ClassNode\n" +
            "  assert node.name == 'Main'\n" +
            "})\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // GROOVY-10199
    public void testASTTest5() throws Exception {
        java.net.URL bundleEntry = Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry("astTransformations/transforms.jar");
        cpAdditions = new String[] {FileLocator.toFileURL(bundleEntry).getPath()};

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.ASTTest(value={\n" +
            "  print examples.local.LoggingExample\n" + // Cannot get property 'local' on null object
            "})\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }
}
