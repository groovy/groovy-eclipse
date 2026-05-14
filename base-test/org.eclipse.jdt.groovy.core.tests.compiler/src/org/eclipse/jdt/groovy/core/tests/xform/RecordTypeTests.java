/*
 * Copyright 2009-2025 the original author or authors.
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
            "print obj.s()\n" +
            "print obj\n",

            "Simple.groovy",
            "record Simple(Number n, String s) {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "1xSimple[n=1, s=x]");
    }

    @Test
    public void testRecordType3() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "def obj = new Simple(n:1, s:'x')\n" +
            "print obj.n\n" +
            "print obj.s\n" +
            "print obj\n",

            "Simple.groovy",
            "record Simple(Number n, String s) {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "1xSimple[n=1, s=x]");
    }

    @Test
    public void testRecordType4() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "print new Simple(n:1)\n",

            "Simple.groovy",
            "record Simple(Number n, String s = 'x') {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Simple[n=1, s=x]");
    }

    @Test
    public void testRecordType5() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "new Simple(n:1)\n",

            "Simple.groovy",
            "record Simple(Number n, String s) {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "", "java.lang.AssertionError: Missing required named argument 's'. Keys found: [n].");
    }

    @Test
    public void testRecordType6() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "new Simple(1,'x')\n",

            "Simple.groovy",
            "record Simple(Number n, String s) {\n" +
            "  Simple {\n" +
            "    assert n > 1\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "", "Assertion failed");
    }

    @Test // GROOVY-11041
    public void testRecordType7() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "print new Simple(1,'x').n()\n",

            "Simple.groovy",
            "record Simple(Number n, String s) {\n" +
            "  Number n() {\n" +
            "    n + 41\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testRecordType8() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.java",
            "import java.util.*;\n" +
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    Map<String,Object> map = new HashMap<>();\n" +
            "    map.put(\"n\", 12345);\n" +
            "    map.put(\"s\", \"x\");\n" +
            "    System.out.print(new Simple(map));\n" +
            "  }\n" +
            "}\n",

            "Simple.groovy",
            "record Simple(Number n, String s) {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Simple[n=12345, s=x]");
    }

    @Test
    public void testRecordType9() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.java",
            null,

            "Simple.groovy",
            "record Simple(boolean b, Number n) {\n" +
            "}\n",
        };
        //@formatter:on

        String mainDotJava = "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    Simple s = new Simple(true, 1);\n" +
            "    TODO;\n" +
            "  }\n" +
            "}\n";

        sources[1] = mainDotJava.replace("TODO", "s.b()");
        runConformTest(sources);

        sources[1] = mainDotJava.replace("TODO", "s.n()");
        runConformTest(sources);

        sources[1] = mainDotJava.replace("TODO", "s.isB()");
        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.java (at line 4)\n" +
            "\ts.isB();\n" +
            "\t  ^^^\n" +
            "The method isB() is undefined for the type Simple\n" +
            "----------\n");

        sources[1] = mainDotJava.replace("TODO", "s.getB()");
        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.java (at line 4)\n" +
            "\ts.getB();\n" +
            "\t  ^^^^\n" +
            "The method getB() is undefined for the type Simple\n" +
            "----------\n");

        sources[1] = mainDotJava.replace("TODO", "s.getN()");
        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.java (at line 4)\n" +
            "\ts.getN();\n" +
            "\t  ^^^^\n" +
            "The method getN() is undefined for the type Simple\n" +
            "----------\n");

        sources[1] = mainDotJava.replace("TODO", "s.setN(42)");
        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.java (at line 4)\n" +
            "\ts.setN(42);\n" +
            "\t  ^^^^\n" +
            "The method setN(int) is undefined for the type Simple\n" +
            "----------\n");

        // TODO: getAt(int), toList(), toMap(), size()
    }

    @Test
    public void testRecordType10() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "print(new Simple('foo'))\n",

            "Named.java",
            "interface Named { String name(); }",

            "Simple.groovy",
            "record Simple(@Override String name) implements Named {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Simple[name=foo]");

        sources[3] = "interface Named {String nameX();}";

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Simple.groovy (at line 1)\n" +
            "\trecord Simple(@Override String name) implements Named {\n" +
            "\t       ^^^^^^\n" +
            "Groovy:Can't have an abstract method in a non-abstract class. The class 'Simple' must be declared abstract or the method 'java.lang.String nameX()' must be implemented.\n" +
            "----------\n" +
            "2. ERROR in Simple.groovy (at line 1)\n" +
            "\trecord Simple(@Override String name) implements Named {\n" +
            "\t              ^^^^^^^^^\n" +
            "Groovy:Method 'name' from class 'Simple' does not override method from its superclass or interfaces but is annotated with @Override.\n" +
            "----------\n");
    }
}
