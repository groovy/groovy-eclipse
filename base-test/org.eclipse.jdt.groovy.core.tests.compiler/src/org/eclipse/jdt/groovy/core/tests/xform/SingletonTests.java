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
 * Test cases for {@link groovy.lang.Singleton}.
 */
public final class SingletonTests extends GroovyCompilerTestSuite {

    @Test
    public void testSingleton1() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String... args) {\n" +
            "    System.out.print(Wibble.getInstance().field);\n" +
            "  }\n" +
            "}\n",

            "Wibble.groovy",
            "@Singleton class Wibble {\n" +
            "  public String field = 'abcd'\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "abcd");
    }

    @Test
    public void testSingleton2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static void main(args) {\n" +
            "    print(Wibble.instance.field)\n" +
            "  }\n" +
            "}\n",

            "Wibble.groovy",
            "@Singleton class Wibble {\n" +
            "  public String field = 'abcd'\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "abcd");
    }

    @Test
    public void testSingleton3() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static void main(args) {\n" +
            "    Wibble.run()\n" +
            "    print('running ')\n" +
            "    print(Wibble.instance.field)\n" +
            "  }\n" +
            "}\n",

            "Wibble.groovy",
            "@Singleton(lazy=false, strict=false) class Wibble {" +
            "  public String field = 'abcd'\n" +
            "  private Wibble() { print 'ctor ' }\n" +
            "  static void run() {}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "ctor running abcd");
    }

    @Test // lazy option set in Singleton
    public void testSingleton4() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "public class Main {\n" +
            "  static void main(args) {\n" +
            "    Wibble.run()\n" +
            "    print('running ')\n" +
            "    print(Wibble.instance.field)\n" +
            "  }\n" +
            "}\n",

            "Wibble.groovy",
            "@Singleton(lazy=true, strict=false) class Wibble {" +
            "  public String field = 'abcd';\n" +
            "  private Wibble() { print 'ctor ' }\n" +
            "  static void run() {}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "running ctor abcd");
    }
}
