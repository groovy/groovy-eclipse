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

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Test;

/**
 * Test cases for {@link org.codehaus.groovy.ast.builder.AstBuilder}.
 */
public final class AstBuilderTests extends GroovyCompilerTestSuite {

    @Test
    public void testAstBuilder1() {
        //@formatter:off
        String[] sources = {
            "Type.groovy",
            "import org.codehaus.groovy.ast.ClassNode\n" +
            "import org.codehaus.groovy.ast.MethodNode\n" +
            "import org.codehaus.groovy.ast.builder.AstBuilder\n" +
            "import static org.codehaus.groovy.control.CompilePhase.*\n" +

            "def className = 'Object', methodName = 'toString'\n" +
            "def ast = new AstBuilder().buildFromString(CANONICALIZATION, false, \"\"\"\n" +
            "  class C {\n" +
            "    public static void main(String[] args) {\n" +
            "      new ${className}().${methodName}()\n" +
            "    }\n" +
            "  }\n" +
            "\"\"\")\n" +
            "// ast[0] is BlockStatement\n" +
            "def main = (ast[1] as ClassNode).getDeclaredMethods('main')[0]\n" +
            "assert main instanceof MethodNode && main.isStatic()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }
}
