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
package org.codehaus.groovy.eclipse.refactoring.test.extract

final class ExtractLocalTestsData {

    static int findLocation(toFind, test) {
        String contents = this."${test}In" as String
        contents.indexOf(toFind)
    }

    static final String test1In = '''\
        |package p
        |
        |def foo
        |def bar
        |foo(foo+bar)
        |
        |foo + bar + foo(foo+bar, foo+ bar + baz) + foo + bar
        |foo + bar
        |
        |def x() {
        |\tfoo + bar
        |}
        |def x = {
        |\tfoo + bar
        |}
        |'''.stripMargin()

    static final String test1Out = '''\
        |package p
        |
        |def foo
        |def bar
        |def fooBar = foo + bar
        |foo(fooBar)
        |
        |fooBar + foo(fooBar, fooBar + baz) + fooBar
        |fooBar
        |
        |def x() {
        |\tfoo + bar
        |}
        |def x = {
        |\tfooBar
        |}
        |'''.stripMargin()

    static final String test2In = '''\
        |package p
        |
        |foo.bar.foo.bar(foo.bar.foo.bar)
        |'''.stripMargin()

    static final String test2Out = '''\
        |package p
        |
        |def fooBar = foo.bar
        |fooBar.foo.bar(fooBar.foo.bar)
        |'''.stripMargin()

    static final String test3In = '''\
        |package p
        |
        |baz.foo.&bar
        |'''.stripMargin()

    static final String test3Out = '''\
        |package p
        |
        |def bazFooBar = baz.foo.&bar
        |bazFooBar
        |'''.stripMargin()

    static final String test4In = '''\
        |package p
        |
        |first + 1
        |first+1
        |'''.stripMargin()

    static final String test4Out = '''\
        |package p
        |
        |def first1 = first + 1
        |first1
        |first+1
        |'''.stripMargin()

    static final String test5In = '''\
        |package p
        |
        |foo + bar
        |foo + // fdsafhds
        |\tbar
        |'''.stripMargin()

    static final String test5Out = '''\
        |package p
        |
        |def fooBar = foo + bar
        |fooBar
        |fooBar
        |'''.stripMargin()

    static final String test6In = '''\
        |package p
        |
        |class Outer {
        |\tdef x() {
        |\t\tfoo + bar
        |\t}
        |}
        |'''.stripMargin()

    static final String test6Out = '''\
        |package p
        |
        |class Outer {
        |\tdef x() {
        |\t\tdef fooBar = foo + bar
        |\t\tfooBar
        |\t}
        |}
        |'''.stripMargin()

    static final String test7In = '''\
        |package p
        |
        |class Outer {
        |\tclass Inner {
        |\t\tdef x() {
        |\t\t\tfoo + bar
        |\t\t}
        |\t}
        |}
        |'''.stripMargin()

    static final String test7Out = '''\
        |package p
        |
        |class Outer {
        |\tclass Inner {
        |\t\tdef x() {
        |\t\t\tdef fooBar = foo + bar
        |\t\t\tfooBar
        |\t\t}
        |\t}
        |}
        |'''.stripMargin()

    static final String test8In = '''\
        |package p
        |
        |foo + bar
        |if (foo+bar) {
        |\twhile (foo+bar) {
        |\t\tfoo+  bar
        |\t}
        |\tfoo+bar
        |}
        |'''.stripMargin()

    static final String test8Out = '''\
        |package p
        |
        |def fooBar = foo+  bar
        |fooBar
        |if (fooBar) {
        |\twhile (fooBar) {
        |\t\tfooBar
        |\t}
        |\tfooBar
        |}
        |'''.stripMargin()

    static final String test9In = '''\
        |class Simple {
        |     def test() {
        |          def map
        |          def foo = {
        |               println map.one
        |               println map.one
        |               println map.one
        |               println map.one
        |          }
        |     }
        |}
        |'''.stripMargin()

    static final String test9Out = '''\
        |class Simple {
        |     def test() {
        |          def map
        |          def foo = {
        |               def mapOne = map.one
        |               println mapOne
        |               println mapOne
        |               println mapOne
        |               println mapOne
        |          }
        |     }
        |}
        |'''.stripMargin()

    static final String test10In = '''\
        |class Simple {
        |     def test() {
        |          model.farInstance()\u0020\u0020
        |     }
        |}
        |'''.stripMargin()

    static final String test10Out = '''\
        |class Simple {
        |     def test() {
        |          def modelFarInstance = model.farInstance()
        |          modelFarInstance
        |     }
        |}
        |'''.stripMargin()

    static final String test11In = '''\
        |class Simple {
        |     def test() {
        |          println "here"
        |     }
        |}
        |'''.stripMargin()

    static final String test11Out = '''\
        |class Simple {
        |     def test() {
        |          def println = println "here"
        |          println
        |     }
        |}
        |'''.stripMargin()

    static final String test12In = '''\
        |class Simple {
        |     def test() {
        |          println "here"
        |     }
        |}
        |'''.stripMargin()

    static final String test12Out = '''\
        |class Simple {
        |     def test() {
        |          def println = println "here"
        |          println
        |     }
        |}
        |'''.stripMargin()

    static final String test13In = '''\
        |class Foo {
        |\tint bar(int a, int b) {
        |\t\tdef aB
        |\t\ta + b
        |\t}
        |}
        |'''.stripMargin()

    static final String test13Out = '''\
        |class Foo {
        |\tint bar(int a, int b) {
        |\t\tdef aB
        |\t\tdef aB2 = a + b
        |\t\taB2
        |\t}
        |}
        |'''.stripMargin()
}
