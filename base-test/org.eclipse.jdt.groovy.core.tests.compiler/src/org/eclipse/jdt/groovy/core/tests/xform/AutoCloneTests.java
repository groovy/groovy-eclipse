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
 * Test cases for {@link groovy.transform.AutoClone}.
 */
public final class AutoCloneTests extends GroovyCompilerTestSuite {

    @Test
    public void testAutoClone1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def foo1 = new Foo(one: 'x', two: 'y', timestamp: new Date())\n" +
            "def foo2 = foo1.clone()\n" +
            "def bar1 = new Bar(one: 'x', two: 'y', items: ['1', '2', '3'], timestamp: new Date())\n" +
            "def bar2 = bar1.clone()\n" +
            "\n" +
            "assert [foo1, foo1.timestamp, bar1, bar1.items, bar1.timestamp].every { it instanceof Cloneable }\n" +
            "assert !(foo1.one instanceof Cloneable)\n" +
            "assert !foo1.is(foo2)\n" +
            "assert !bar1.is(bar2)\n" +
            "assert foo1.one.is(foo2.one)\n" +
            "assert foo1.two.is(foo2.two)\n" +
            "assert !bar1.items.is(bar2.items)\n" +
            "assert bar1.items == bar2.items\n",

            "Foo.groovy",
            "@groovy.transform.AutoClone(style=groovy.transform.AutoCloneStyle.SIMPLE)\n" +
            "class Foo {\n" +
            "  String one,two\n" +
            "  Date timestamp\n" +
            "}\n",

            "Bar.groovy",
            "@groovy.transform.AutoClone(style=groovy.transform.AutoCloneStyle.SIMPLE)\n" +
            "class Bar extends Foo {\n" +
            "  List<String> items\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }
}
