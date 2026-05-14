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

final class ExtractConstantTestsData {

    static int findLocation(toFind, test) {
        String contents = this."${test}In" as String
        contents.indexOf(toFind)
    }

    static final String test1In = '''\
        |package p;
        |class A{
        |\tstatic Foo
        |\tstatic Bar
        |\tint f = Foo + Bar;
        |}
        |'''.stripMargin()

    static final String test1Out = '''\
        |package p;
        |class A{
        |\tstatic Foo
        |\tstatic Bar
        |
        |\tstatic final FOO_BAR = Foo + Bar
        |\tint f = FOO_BAR;
        |}
        |'''.stripMargin()

    static final String test2In = '''\
        |package p;
        |class A{
        |\tstatic Foo
        |\tstatic Bar
        |\tint f = Foo + Bar;
        |\tint g = Foo + // some useless crap
        |\t
        |\t
        |\tBar;
        |}
        |'''.stripMargin()

    static final String test2Out = '''\
        |package p;
        |class A{
        |\tstatic Foo
        |\tstatic Bar
        |
        |\tstatic final FOO_BAR = Foo + Bar
        |\tint f = FOO_BAR;
        |\tint g = FOO_BAR;
        |}
        |'''.stripMargin()

    static final String test3In = '''\
        |package p;
        |class A{
        |\tstatic Foo
        |\tstatic Bar
        |\tstatic frax() { }
        |\tint f() {
        |\t\tFoo+Bar+A.frax()
        |\t}
        |}
        |'''.stripMargin()

    static final String test3Out = '''\
        |package p;
        |class A{
        |\tstatic Foo
        |\tstatic Bar
        |
        |\tstatic final FOO_BAR_FRAX = Foo+Bar+A.frax()
        |\tstatic frax() { }
        |\tint f() {
        |\t\tFOO_BAR_FRAX
        |\t}
        |}
        |'''.stripMargin()

    static final String test4In = '''\
        |package p;
        |class A{
        |\tclass B {
        |\t\tstatic Foo
        |\t\tstatic Bar
        |\t\tstatic frax() { }
        |\t\tint f() {
        |\t\t\tFoo+Bar+A.frax()+ 7
        |\t\t\t7 + Foo+Bar+A.frax()+ 7
        |\t\t\t7 + 7 + Foo+Bar+A.frax()+ 7 + 7
        |\t\t}
        |\t}
        |}
        |'''.stripMargin()

    static final String test4Out = '''\
        |package p;
        |class A{
        |\tclass B {
        |\t\tstatic Foo
        |\t\tstatic Bar
        |
        |\t\tstatic final FOO_BAR_FRAX = Foo+Bar+A.frax()
        |\t\tstatic frax() { }
        |\t\tint f() {
        |\t\t\tFOO_BAR_FRAX+ 7
        |\t\t\t7 + FOO_BAR_FRAX+ 7
        |\t\t\t7 + 7 + FOO_BAR_FRAX+ 7 + 7
        |\t\t}
        |\t}
        |}
        |'''.stripMargin()

    static final String test5aIn = '''\
        |package p;
        |class A{
        |\tstatic Foo
        |\tstatic Bar
        |\tstatic frax() { }
        |\tint f() {
        |\t\tFoo+Bar+A.frax()+ 7
        |\t\t7 + Foo+Bar+A.frax()+ 7
        |\t\t7 + 7 + Foo+Bar+A.frax()+ 7 + 7
        |\t}
        |}
        |'''.stripMargin()

    static final String test5aOut = '''\
        |package p;
        |class A{
        |\tstatic Foo
        |\tstatic Bar
        |
        |\tstatic final FOO_BAR_FRAX = Foo+Bar+A.frax()
        |\tstatic frax() { }
        |\tint f() {
        |\t\tFOO_BAR_FRAX+ 7
        |\t\t7 + FOO_BAR_FRAX+ 7
        |\t\t7 + 7 + FOO_BAR_FRAX+ 7 + 7
        |\t}
        |}
        |'''.stripMargin()

    static final String test6aIn = '''\
        |package p;
        |class A{
        |\tclass B {
        |\t\tstatic Foo
        |\t\tstatic Bar
        |\t\tstatic frax() { }
        |\t\tint f() {
        |\t\t\tFoo+Bar+A.frax()+ 7
        |\t\t\t7 + Foo+Bar+A.frax()+ 7
        |\t\t\t7 + 7 + Foo+Bar+A.frax()+ 7 + 7
        |\t\t}
        |\t}
        |}
        |'''.stripMargin()

    static final String test6aOut = '''\
        |package p;
        |class A{
        |\tclass B {
        |\t\tstatic Foo
        |\t\tstatic Bar
        |
        |\t\tstatic final FOO_BAR_FRAX = Foo+Bar+A.frax()
        |\t\tstatic frax() { }
        |\t\tint f() {
        |\t\t\tFOO_BAR_FRAX+ 7
        |\t\t\t7 + FOO_BAR_FRAX+ 7
        |\t\t\t7 + 7 + FOO_BAR_FRAX+ 7 + 7
        |\t\t}
        |\t}
        |}
        |'''.stripMargin()

    static final String test7In = '''\
        |package p;
        |class A {
        |\tstatic foo() {
        |\t\tdef Foo = 2
        |\t\tdef Bar = 3
        |\t\tFoo + Bar
        |\t}
        |}
        |'''.stripMargin()

    static final String test8In = '''\
        |package p;
        |class A {
        |\tstatic Foo
        |\tstatic Bar
        |\tstatic final FOO_BAR = 'Something'
        |\tstatic foo() {
        |\t\tFoo + Bar
        |\t}
        |}
        |'''.stripMargin()

    static final String test8Out = '''\
        |package p;
        |class A {
        |\tstatic Foo
        |\tstatic Bar
        |\tstatic final FOO_BAR = 'Something'
        |
        |\tstatic final FOO_BAR2 = Foo + Bar
        |\tstatic foo() {
        |\t\tFOO_BAR2
        |\t}
        |}
        |'''.stripMargin()

    static final String testNoReplaceOccurrences1In = '''\
        |package p;
        |class A{
        |\tstatic Foo
        |\tstatic Bar
        |\tstatic frax() { }
        |\tint f() {
        |\t\tFoo+Bar+A.frax()
        |\t}
        |\tint g() {
        |\t\tdef x = Foo+Bar+A.frax() + 7
        |\t}
        |}
        |'''.stripMargin()

    static final String testNoReplaceOccurrences1Out = '''\
        |package p;
        |class A{
        |\tstatic Foo
        |\tstatic Bar
        |
        |\tstatic final FOO_BAR_FRAX = Foo+Bar+A.frax()
        |\tstatic frax() { }
        |\tint f() {
        |\t\tFOO_BAR_FRAX
        |\t}
        |\tint g() {
        |\t\tdef x = Foo+Bar+A.frax() + 7
        |\t}
        |}
        |'''.stripMargin()

    static final String testQualifiedReplace1In = '''\
        |package p;
        |class A{
        |\tstatic Foo
        |\tstatic Bar
        |\tstatic frax() { }
        |\tint f() {
        |\t\tFoo+Bar+A.frax()
        |\t}
        |\tint g() {
        |\t\tdef x = Foo+Bar+A.frax() + 7
        |\t}
        |}
        |'''.stripMargin()

    static final String testQualifiedReplace1Out = '''\
        |package p;
        |class A{
        |\tstatic Foo
        |\tstatic Bar
        |
        |\tstatic final FOO_BAR_FRAX = Foo+Bar+A.frax()
        |\tstatic frax() { }
        |\tint f() {
        |\t\tA.FOO_BAR_FRAX
        |\t}
        |\tint g() {
        |\t\tdef x = A.FOO_BAR_FRAX+ 7
        |\t}
        |}
        |'''.stripMargin()
}
