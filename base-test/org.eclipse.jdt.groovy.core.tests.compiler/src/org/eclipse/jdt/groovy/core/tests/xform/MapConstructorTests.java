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
 * Test cases for {@link groovy.transform.MapConstructor}.
 */
public final class MapConstructorTests extends GroovyCompilerTestSuite {

    @Test // https://github.com/groovy/groovy-eclipse/issues/421
    public void testMapConstructor1() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "import java.util.*;\n" +
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    Map<String,Object> map = new HashMap<>();\n" +
            "    map.put(\"bar\",\"one\");\n" +
            "    map.put(\"baz\",\"two\");\n" +
            "    System.out.print(new Foo(map));\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@groovy.transform.MapConstructor\n" +
            "@groovy.transform.ToString\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)");
    }

    @Test
    public void testMapConstructor2() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Foo());\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@groovy.transform.MapConstructor(noArg=true)\n" +
            "@groovy.transform.ToString\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(null, null)");
    }
}
