/*
 * Copyright 2009-2020 the original author or authors.
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
 * Test cases for {@link groovy.transform.AutoImplement}.
 */
public final class AutoImplementTests extends GroovyCompilerTestSuite {

    @Test
    public void testAutoImplement1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.AutoImplement\n" +
            "class Foo implements Iterator<String> { }\n" +
            "print new Foo().hasNext()\n",
        };
        //@formatter:on

        runConformTest(sources, "false");
    }

    @Test
    public void testAutoImplement2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.AutoImplement(exception=UnsupportedOperationException)\n" +
            "class Foo implements Iterator<String> { }\n" +
            "try { new Foo().hasNext() } catch (UnsupportedOperationException e) { print e }\n",
        };
        //@formatter:on

        runConformTest(sources, "java.lang.UnsupportedOperationException");
    }

    @Test
    public void testAutoImplement3() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.AutoImplement(exception=UnsupportedOperationException, message='not supported by Foo')\n" +
            "class Foo implements Iterator<String> { }\n" +
            "try { new Foo().hasNext() } catch (UnsupportedOperationException e) { print e }\n",
        };
        //@formatter:on

        runConformTest(sources, "java.lang.UnsupportedOperationException: not supported by Foo");
    }

    @Test
    public void testAutoImplement4() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.AutoImplement(code={ throw new IllegalStateException() })\n" +
            "class Foo implements Iterator<String> { }\n" +
            "try { new Foo().hasNext() } catch (IllegalStateException e) { print e }\n",
        };
        //@formatter:on

        runConformTest(sources, "java.lang.IllegalStateException");
    }

    @Test
    public void testAutoImplement5() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    Pogo pogo = new Pogo();\n" +
            "    System.out.print(pogo.hasNext());\n" +
            "  }\n" +
            "}\n",

            "Pogo.groovy",
            "@groovy.transform.AutoImplement\n" +
            "class Pogo implements Iterator<String> { }\n",
        };
        //@formatter:on

        runConformTest(sources, "false");
    }

    @Test // GROOVY-8270
    public void testAutoImplement6() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.AutoImplement\n" +
            "class Foo implements Comparator<String> { }\n" +
            "print new Foo().compare('bar', 'baz')\n",
        };
        //@formatter:on

        runConformTest(sources, "0");
    }

    @Test // GROOVY-9816
    public void testAutoImplement7() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface Bar {\n" +
            "  def getBaz(); void setBaz(baz)\n" +
            "}\n" +
            "@groovy.transform.AutoImplement\n" +
            "class Foo implements Bar {\n" +
            "  def baz\n" +
            "}\n" +
            "def foo = new Foo(baz: 123)\n" +
            "print foo.baz\n",
        };
        //@formatter:on

        runConformTest(sources, "123");
    }

    @Test // GROOVY-9816
    public void testAutoImplement8() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface Bar {\n" +
            "  boolean getBaz(); boolean isBaz()\n" +
            "}\n" +
            "@groovy.transform.AutoImplement\n" +
            "class Foo implements Bar {\n" +
            "  boolean baz\n" +
            "}\n" +
            "def foo = new Foo(baz: true)\n" +
            "print foo.getBaz()\n" +
            "print foo.isBaz()\n" +
            "print foo.baz\n",
        };
        //@formatter:on

        runConformTest(sources, "truetruetrue");
    }
}
