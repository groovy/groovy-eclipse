/*
 * Copyright 2009-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.groovy.core.tests.xform;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test cases for {@link groovy.lang.Singleton}.
 */
public final class SingletonTests extends GroovyCompilerTestSuite {

    @Test
    public void testSingleton1() {
        String[] sources = {
            "Goo.groovy",
            "class Goo {\n" +
            "  public static void main(String[] argv) {\n" +
            "    Run.main(argv)\n" +
            "  }\n" +
            "}\n",

            "Run.groovy",
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println(Wibble.getInstance().field)\n" +
            "  }\n" +
            "}\n",

            "Wibble.groovy",
            "@Singleton class Wibble {\n" +
            "  public String field = 'abcd'\n" +
            "}\n",
        };

        runConformTest(sources, "abcd");
    }

    @Test // lazy option set in Singleton
    public void testSingleton2() {
        String[] sources = {
            "Goo.groovy",
            "class Goo {\n"+
            "  public static void main(String[] argv) {\n"+
            "    Run.main(argv);\n"+
            "  }\n"+
            "}\n",

            "Run.groovy",
            "public class Run {\n"+
            "  public static void main(String[] argv) {\n"+
            "    Wibble.run();\n"+
            "    System.out.print(\"running \");\n"+
            "    System.out.print(Wibble.getInstance().field);\n"+
            "  }\n"+
            "}\n",

            "Wibble.groovy",
            "@Singleton(lazy=false, strict=false) class Wibble {" +
            "  public String field = 'abcd';\n"+
            "  private Wibble() { print \"ctor \";}\n"+
            "  static void run() {}\n"+
            "}\n",
        };

        runConformTest(sources, "ctor running abcd");
    }

    @Test
    public void testSingleton3() {
        String[] sources = {
            "Goo.groovy",
            "class Goo {\n"+
            "  public static void main(String[] argv) {\n"+
            "    Run.main(argv);\n"+
            "  }\n"+
            "}\n",

            "Run.groovy",
            "public class Run {\n"+
            "  public static void main(String[] argv) {\n"+
            "    Wibble.run();\n"+
            "    System.out.print(\"running \");\n"+
            "    System.out.print(Wibble.getInstance().field);\n"+
            "  }\n"+
            "}\n",

            "Wibble.groovy",
            "@Singleton(lazy=true, strict=false) class Wibble {" +
            "  public String field = 'abcd';\n"+
            "  private Wibble() { print \"ctor \";}\n"+
            "  static void run() {}\n"+
            "}\n",
        };

        runConformTest(sources, "running ctor abcd");
    }

    /**
     * COOL!!!  The getInstance() method is added by a late AST Transformation made due to the Singleton annotation - and yet
     * still it is referencable from Java.  This is not possible with normal joint compilation.
     * currently have to 'turn on' support in org.eclipse.jdt.internal.compiler.lookup.Scope#oneLastLook
     */
    @Test @Ignore
    public void testSingleton_JavaAccessingTransformedGroovy() {
        String[] sources = {
            "Goo.groovy",
            "class Goo {\n"+
            "  public static void main(String[] argv) {\n"+
            "    Run.main(argv);\n"+
            "  }\n"+
            "}\n",

            "Run.java",
            "public class Run {\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println(Wibble.getInstance().field);\n"+
            "  }\n"+
            "}\n",

            "Wibble.groovy",
            "@Singleton class Wibble {\n" +
            "  public final String field = 'abc'\n"+
            "}\n",
        };

        runConformTest(sources, "abc");
    }
}
