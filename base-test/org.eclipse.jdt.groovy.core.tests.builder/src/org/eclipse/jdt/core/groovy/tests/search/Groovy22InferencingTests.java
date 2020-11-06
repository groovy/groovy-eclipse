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
package org.eclipse.jdt.core.groovy.tests.search;

import org.junit.Test;

public final class Groovy22InferencingTests extends InferencingTestSuite {

    @Test
    public void testBaseScript1() {
        //@formatter:off
        String contents =
            "abstract class Foo extends Script {\n" +
            "  Number bar\n" +
            "  Number baz() {}\n" +
            "}\n" +
            "@groovy.transform.BaseScript Foo foo\n" +
            "def x = bar\n" +
            "def y = baz()\n";
        //@formatter:on
        assertType(contents, "x", "java.lang.Number");
        assertType(contents, "y", "java.lang.Number");
    }

    @Test
    public void testBaseScript2() {
        //@formatter:off
        String contents =
            "@BaseScript(Foo)\n" +
            "import groovy.transform.BaseScript\n" +
            "abstract class Foo extends Script {\n" +
            "  Number bar\n" +
            "  Number baz() {}\n" +
            "}\n" +
            "def x = bar\n" +
            "def y = baz()\n";
        //@formatter:on
        assertType(contents, "x", "java.lang.Number");
        assertType(contents, "y", "java.lang.Number");
    }

    @Test
    public void testBaseScript3() {
        //@formatter:off
        String contents =
            "@BaseScript(Foo)\n" +
            "import groovy.transform.BaseScript\n" +
            "abstract class Foo extends Script {\n" +
            "  Number bar\n" +
            "  Number baz() {}\n" +
            "  abstract body()\n" +
            "  def run() {\n" +
            "    body()\n" +
            "  }\n" +
            "}\n" +
            "def x = bar\n" +
            "def y = baz()\n";
        //@formatter:on
        assertType(contents, "x", "java.lang.Number");
        assertType(contents, "y", "java.lang.Number");
    }

    @Test
    public void testDelegatesToGenericTypeIndex1() {
        //@formatter:off
        String contents =
            "def foo(@DelegatesTo.Target List<Number> list, @DelegatesTo(genericTypeIndex=0) Closure c) {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def bar = [1,2,3]\n" +
            "  foo(bar) { delegate }\n" +
            "}\n";
        //@formatter:on
        assertType(contents, "delegate", "java.lang.Number");
    }

    @Test
    public void testDelegatesToGenericTypeIndex2() {
        //@formatter:off
        String contents =
            "def foo(@DelegatesTo.Target Map<String, Number> map, @DelegatesTo(genericTypeIndex=1) Closure c) {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def bar = [a:1,b:2]\n" +
            "  foo(bar) { delegate }\n" +
            "}\n";
        //@formatter:on
        assertType(contents, "delegate", "java.lang.Number");
    }

    @Test
    public void testDelegatesToGenericTypeIndex3() {
        //@formatter:off
        String contents =
            "def foo(@DelegatesTo.Target Map<String, Number> map, @DelegatesTo(genericTypeIndex=10) Closure c) {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def bar = [a:1,b:2]\n" +
            "  foo(bar) { delegate }\n" +
            "}\n";
        //@formatter:on
        assertType(contents, "delegate", "java.lang.Object");
    }
}
