/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.search

import groovy.test.NotYetImplemented

import org.codehaus.groovy.eclipse.search.GroovyOccurrencesFinder
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.junit.Assert
import org.junit.Test

/**
 * Tests for {@link GroovyOccurrencesFinder}.
 */
final class FindOccurrencesTests extends GroovyEclipseTestSuite {

    private void doTest(String contents, int start, int length, int... expected) {
        GroovyCompilationUnit unit = addGroovySource(contents)
        unit.becomeWorkingCopy(null)
        try {
            def actual = new GroovyOccurrencesFinder().with {
                groovyCompilationUnit = unit
                initialize(null, start, length)
                return occurrences
            }

            int n = actual.length
            if (n != expected.length / 2) {
                Assert.fail('Wrong number of occurrences found. expecting:\n' + Arrays.toString(expected) + '\nbut found:\n' + printOccurrences(actual, n))
            }
            for (int i = 0; i < n; i += 1) {
                def o = actual[i]
                int off = o.offset, len = o.length
                if (off != expected[2 * i] || len != expected[(2 * i) + 1]) {
                    Assert.fail('Problem in Occurrence ' + i + ' expecting:\n' + Arrays.toString(expected) + '\nbut found:\n' + printOccurrences(actual, n))
                }
            }
        } finally {
            unit.discardWorkingCopy()
        }
    }

    private static String printOccurrences(array, int length) {
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < length; i += 1) {
            sb.append(array[i]).append('\n')
        }
        return sb.toString()
    }

    //--------------------------------------------------------------------------

    @Test
    void testFindLocalOccurrences1() {
        //@formatter:off
        String contents = '''\
            def x
            x
            '''.stripIndent()
        //@formatter:on
        doTest(contents, contents.lastIndexOf('x'), 1, contents.indexOf('x'), 1, contents.lastIndexOf('x'), 1)
    }

    @Test
    void testFindLocalOccurrences2() {
        //@formatter:off
        String contents = '''\
            def x(x) {
              x
            }
            '''.stripIndent()
        //@formatter:on
        doTest(contents, contents.lastIndexOf('x'), 1, contents.indexOf('(x') + 1, 1, contents.lastIndexOf('x'), 1)
    }

    @Test
    void testFindLocalOccurrences3() {
        //@formatter:off
        String contents = '''\
            nuthin
            def x(int x) {
            x
            }
            '''.stripIndent()
        //@formatter:on
        int afterParen = contents.indexOf('(')
        doTest(contents, contents.lastIndexOf('x'), 1, contents.indexOf('x', afterParen), 1, contents.lastIndexOf('x'), 1)
    }

    @Test // looking for the method declaration, not the parameter
    void testFindLocalOccurrences4() {
        //@formatter:off
        String contents = '''\
            nuthin
            def x(int x) {
              x
            }
            '''.stripIndent()
        //@formatter:on
        doTest(contents, contents.indexOf('x'), 1, contents.indexOf('x'), 1)
    }

    @Test
    void testFindForLoopOccurrences() {
        //@formatter:off
        String contents = '''\
            for (x in []) {
              x
            }
            '''.stripIndent()
        //@formatter:on
        doTest(contents, contents.indexOf('x'), 1, contents.indexOf('x'), 1, contents.lastIndexOf('x'), 1)
    }

    @Test @NotYetImplemented // Not working now; see GROOVY-4620 and GRECLIPSE-951
    void testFindPrimitive() {
        //@formatter:off
        String contents = '''\
            int x(int y) {
              int z
            }
            int a
            '''.stripIndent()
        //@formatter:on

        int length = 'int'.length()
        int first  = contents.indexOf('int')
        int second = contents.indexOf('int', first + 1)
        int third  = contents.indexOf('int', second + 1)
        int fourth = contents.indexOf('int', third + 1)
        doTest(contents, second, length, first, length, second, length, third, length, fourth, length)
    }

    @Test
    void testFindField() {
        //@formatter:off
        String contents = '''\
            class Foo {
              public def bar
            }
            new Foo().bar
            new Foo().bar()
            '''.stripIndent()
        //@formatter:on

        int length = 'bar'.length()
        int first  = contents.indexOf('bar')
        int second = contents.indexOf('bar', first + 1)
        int third  = contents.indexOf('bar', second + 1)
        doTest(contents, second, length, first, length, second, length, third, length)
    }

    @Test
    void testFindProperty() {
        //@formatter:off
        String contents = '''\
            class Foo {
              def bar
            }
            new Foo().bar
            new Foo().bar()
            '''.stripIndent()
        //@formatter:on

        int length = 'foo'.length()
        int first  = contents.indexOf('bar')
        int second = contents.indexOf('bar', first + 1)
        int third  = contents.indexOf('bar', second + 1)
        doTest(contents, second, length, first, length, second, length, third, length)
    }

    @Test
    void testFindPseudoProperty1() {
        //@formatter:off
        String contents = '''\
            class Foo {
              def getBar() {
              }
              void doBaz() {
                bar
              }
            }
            '''.stripIndent()
        //@formatter:on

        int first  = contents.indexOf('getBar')
        int second = contents.indexOf('bar', first + 1)
        doTest(contents, first, 6, first, 6, second, 3)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/986
    void testFindPseudoProperty1a() {
        //@formatter:off
        String contents = '''\
            @groovy.transform.CompileStatic
            class Foo {
              def getBar() {
              }
              void doBaz() {
                bar
              }
            }
            '''.stripIndent()
        //@formatter:on

        int first  = contents.indexOf('getBar')
        int second = contents.indexOf('bar', first + 1)
        doTest(contents, first, 6, first, 6, second, 3)
    }

    @Test
    void testFindPseudoProperty2() {
        //@formatter:off
        String contents = '''\
            class Foo {
              void setBar(value) {
              }
              void doBaz() {
                bar = null
              }
            }
            '''.stripIndent()
        //@formatter:on

        int first  = contents.indexOf('setBar')
        int second = contents.indexOf('bar', first + 1)
        doTest(contents, first, 6, first, 6, second, 3)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/986
    void testFindPseudoProperty2a() {
        //@formatter:off
        String contents = '''\
            @groovy.transform.CompileStatic
            class Foo {
              void setBar(value) {
              }
              void doBaz() {
                bar = null
              }
            }
            '''.stripIndent()
        //@formatter:on

        int first  = contents.indexOf('setBar')
        int second = contents.indexOf('bar', first + 1)
        doTest(contents, first, 6, first, 6, second, 3)
    }

    @Test
    void testFindPseudoProperty3() {
        //@formatter:off
        String contents = '''\
            class Foo {
              void setBar(value) {
              }
              void doBaz() {
                bar // ambiguous reference
              }
            }
            '''.stripIndent()
        //@formatter:on

        int first  = contents.indexOf('setBar')
        int second = contents.indexOf('bar', first + 1)
        doTest(contents, first, 6, first, 6, second, 3)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/986
    void testFindPseudoProperty3a() {
        //@formatter:off
        String contents = '''\
            @groovy.transform.CompileStatic
            class Foo {
              void setBar(value) {
              }
              void doBaz() {
                bar // ambiguous reference
              }
            }
            '''.stripIndent()
        //@formatter:on

        int first  = contents.indexOf('setBar')
        int second = contents.indexOf('bar', first + 1)
        doTest(contents, first, 6, first, 6, second, 3)
    }

    @Test
    void testFindGStringOccurrences1() {
        //@formatter:off
        String contents = '''\
            |def xxx
            |xxx "$xxx"
            |"$xxx"
            |"${xxx}"
            |"xxx"
            |'xxx'
            |'$xxx'
            |'${xxx}'
            |'''.stripMargin()
        //@formatter:on

        int length = 'xxx'.length()
        int decl   = contents.indexOf('xxx')
        int first  = contents.indexOf('xxx', decl + 1)
        int second = contents.indexOf('xxx', first + 1)
        int third  = contents.indexOf('xxx', second + 1)
        int fourth = contents.indexOf('xxx', third + 1)
        doTest(contents, decl, 1, decl, length, first, length, second, length, third, length, fourth, length)
    }

    @Test
    void testFindGStringOccurrences2() {
        //@formatter:off
        String contents = '''\
            |def i
            |i "$i"
            |"$i"
            |"${i}"
            |"i"
            |'i'
            |'$i'
            |'${i}'
            |'''.stripMargin()
        //@formatter:on

        int length = 'i'.length()
        int decl   = contents.indexOf('i')
        int first  = contents.indexOf('i', decl + 1)
        int second = contents.indexOf('i', first + 1)
        int third  = contents.indexOf('i', second + 1)
        int fourth = contents.indexOf('i', third + 1)
        doTest(contents, decl, 1, decl, length, first, length, second, length, third, length, fourth, length)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/986
    void testFindGStringOccurrences3() {
        //@formatter:off
        String contents = '''\
            @groovy.transform.CompileStatic
            class Foo {
              def getBar() {
              }
              String toString() {
                return "$bar"
              }
            }
            '''.stripIndent()
        //@formatter:on

        int first  = contents.indexOf('getBar')
        int second = contents.indexOf('bar', first + 1)
        doTest(contents, first, 6, first, 6, second, 3)
    }

    @Test // GRECLIPSE-1031
    void testFindStaticMethods1() {
        //@formatter:off
        String contents = '''\
            class Static {
              static staticMethod()  { staticMethod() }
              static { staticMethod() }
              { staticMethod() }
              def t = staticMethod()
              def x() {
                def a = staticMethod()
                Static.&staticMethod
                Static.staticMethod 1, 2, 3
                Static.staticMethod(1, 2, 3)
              }
            }
            '''.stripIndent()
        //@formatter:on

        def target = 'staticMethod'
        int length = target.length()
        int start1 = contents.indexOf(target)
        int start2 = contents.indexOf(target, start1 + 1)
        int start3 = contents.indexOf(target, start2 + 1)
        int start4 = contents.indexOf(target, start3 + 1)
        int start5 = contents.indexOf(target, start4 + 1)
        int start6 = contents.indexOf(target, start5 + 1)
        int start7 = contents.indexOf(target, start6 + 1)
        int start8 = contents.indexOf(target, start7 + 1)
        int start9 = contents.indexOf(target, start8 + 1)
        doTest(contents, start1, length, start1, length, start2, length, start3, length, start4, length, start5, length, start6, length, start7, length/*, start8, length, start9, length*/)
    }

    @Test // GRECLIPSE-1031
    void testFindStaticMethods2() {
        //@formatter:off
        String contents = '''\
            class Static {
              static staticMethod(... args)  { }
              def x() {
                def z = staticMethod
                def a = staticMethod 1, 2, 3
                def b = staticMethod(1, 2, 3)
                def c = Static.staticMethod 1, 2, 3
                def d = Static.staticMethod(1, 2, 3)
              }
            }
            '''.stripIndent()
        //@formatter:on

        def target = 'staticMethod'
        int length = target.length()
        int start1 = contents.indexOf(target)
        int start2 = contents.indexOf(target, start1 + 1)
        int start3 = contents.indexOf(target, start2 + 1)
        int start4 = contents.indexOf(target, start3 + 1)
        int start5 = contents.indexOf(target, start4 + 1)
        int start6 = contents.indexOf(target, start5 + 1)
        doTest(contents, start1, length, start1, length, /*start2, length,*/ start3, length, start4, length, start5, length, start6, length)
    }

    @Test // GRECLIPSE-1023
    void testInnerClass() {
        //@formatter:off
        String contents = '''\
            class Other2 {
              class Inner { }
              Other2.Inner f
              Inner g
            }
            '''.stripIndent()
        //@formatter:on

        String className = 'Inner'
        int len = className.length()
        int start = contents.indexOf(className)
        int start1 = contents.indexOf(className)
        int start2 = contents.indexOf(className, start1 + 1)
        int start3 = contents.indexOf(className, start2 + 1)
        doTest(contents, start, len, start1, len, start2, len, start3, len)
    }

    @Test // GRECLIPSE-1023; try different starting point
    void testInnerClass2() {
        //@formatter:off
        String contents = '''\
            class Other2 {
              class Inner { }
              Other2.Inner f
              Inner g
            }
            '''.stripIndent()
        //@formatter:on

        String className = 'Inner'
        int len = className.length()
        int start1 = contents.indexOf(className)
        int start2 = contents.indexOf(className, start1 + 1)
        int start3 = contents.indexOf(className, start2 + 1)
        int start = start2
        doTest(contents, start, len, start1, len, start2, len, start3, len)
    }

    @Test // GRECLIPSE-1023; try different starting point
    void testInnerClass3() {
        //@formatter:off
        String contents = '''\
            class Other2 {
              class Inner { }
              Other2.Inner f
              Inner g
            }
            '''.stripIndent()
        //@formatter:on

        String className = 'Inner'
        int len = className.length()
        int start1 = contents.indexOf(className)
        int start2 = contents.indexOf(className, start1 + 1)
        int start3 = contents.indexOf(className, start2 + 1)
        int start = start3
        doTest(contents, start, len, start1, len, start2, len, start3, len)
    }

    @Test // GRECLIPSE-1023; inner class in other file
    void testInnerClass4() {
        //@formatter:off
        addGroovySource '''\
            class Other {
              class Inner { }
            }
            '''.stripIndent(), 'Other'

        String contents = '''\
            |import Other.Inner
            |Other.Inner f
            |Inner g
            |'''.stripMargin()
        //@formatter:on

        String className = 'Inner'
        int len = className.length()
        int start1 = contents.indexOf(className)
        int start2 = contents.indexOf(className, start1 + 1)
        int start3 = contents.indexOf(className, start2 + 1)
        int start = start1
        doTest(contents, start, len, start1, len, start2, len, start3, len)
    }

    @Test
    void testGenerics1() {
        //@formatter:off
        String contents = '''\
            |import javax.swing.text.html.HTML
            |Map<HTML, ? extends HTML> h
            |HTML i
            |'''.stripMargin()
        //@formatter:on

        String name = 'HTML'
        int length = name.length()
        int offset = contents.indexOf(name)
        int start1 = contents.indexOf('javax')
        int start2 = contents.indexOf(name, offset + 1)
        int start3 = contents.indexOf(name, start2 + 1)
        int start4 = contents.indexOf(name, start3 + 1)
        doTest(contents, offset, length, start1, 'javax.swing.text.html.HTML'.length(), start2, length, start3, length, start4, length)
    }

    @Test // different starting point
    void testGenerics2() {
        //@formatter:off
        String contents = '''\
            |import javax.swing.text.html.HTML
            |Map<HTML, ? extends HTML> h
            |HTML i
            |'''.stripMargin()
        //@formatter:on

        String name = 'HTML'
        int length = name.length()
        int start1 = contents.indexOf('javax')
        int start2 = contents.indexOf(name, contents.indexOf(name) + 1)
        int start3 = contents.indexOf(name, start2 + 1)
        int start4 = contents.indexOf(name, start3 + 1)
        doTest(contents, start2, length, start1, 'javax.swing.text.html.HTML'.length(), start2, length, start3, length, start4, length)
    }

    @Test // different starting point
    void testGenerics3() {
        //@formatter:off
        String contents = '''\
            |import javax.swing.text.html.HTML
            |Map<HTML, ? extends HTML> h
            |HTML i
            |'''.stripMargin()
        //@formatter:on

        String name = 'HTML'
        int length = name.length()
        int start1 = contents.indexOf('javax')
        int start2 = contents.indexOf(name, contents.indexOf(name) + 1)
        int start3 = contents.indexOf(name, start2 + 1)
        int start4 = contents.indexOf(name, start3 + 1)
        doTest(contents, start4, length, start1, 'javax.swing.text.html.HTML'.length(), start2, length, start3, length, start4, length)
    }

    @Test // GRECLIPSE-1219
    void testAnnotationOnImport() {
        //@formatter:off
        String contents = '''\
            |@Deprecated
            |import javax.swing.text.html.HTML
            |Deprecated
            |'''.stripMargin()
        //@formatter:on

        String name = 'Deprecated'
        int len = name.length()

        int start1 = contents.indexOf(name)
        int start2 = contents.indexOf(name, start1 + 1)
        int start = start2
        doTest(contents, start, len, start1, len, start2, len)
    }

    @Test // shuold not find occurrences in string literals
    void testLiterals1() {
        String contents = '\'fff\''

        String name = '\'fff\''
        int len = name.length()

        int start = contents.indexOf(name)
        doTest(contents, start, len)
    }

    @Test // shuold not find occurrences in multi-line string literals
    void testLiterals2() {
        String contents = '\'\'\'fff\'\'\''

        String name = '\'\'\'fff\'\'\''
        int len = name.length()

        int start = contents.indexOf(name)
        doTest(contents, start, len)
    }

    @Test // shuold not find occurrences in number literals
    void testLiterals3() {
        String contents = '\'\'\'fff\'\'\''

        String name = '\'\'\'fff\'\'\''
        int len = name.length()

        int start = contents.indexOf(name)
        doTest(contents, start, len)
    }

    @Test
    void testOverloaded1() {
        //@formatter:off
        String contents = '''\
            class LotsOfMethods {
              def meth() { }
              def meth(int a) { }
              def meth(String a, LotsOfMethods b) { }
            }
            new LotsOfMethods().meth(1)
            new LotsOfMethods().meth('', null)
            new LotsOfMethods().meth()
            '''.stripIndent()
        //@formatter:on
        int start = contents.indexOf('meth()')
        doTest(contents, start, 4, start, 4, contents.lastIndexOf('meth()'), 4)
    }

    @Test
    void testOverloaded2() {
        //@formatter:off
        String contents = '''\
            class LotsOfMethods {
              def meth() { }
              def meth(int a) { }
              def meth(String a, LotsOfMethods b) { }
            }
            new LotsOfMethods().meth()
            new LotsOfMethods().meth('', null)
            new LotsOfMethods().meth(1)
            '''.stripIndent()
        //@formatter:on
        int start = contents.indexOf('meth(int')
        doTest(contents, start, 4, start, 4, contents.indexOf('meth(1)'), 4)
    }

    @Test
    void testOverloaded3() {
        //@formatter:off
        String contents = '''\
            class LotsOfMethods {
              def meth() { }
              def meth(int a) { }
              def meth(String a, LotsOfMethods b) { }
            }
            new LotsOfMethods().meth(1)
            new LotsOfMethods().meth()
            new LotsOfMethods().meth('', null)
            '''.stripIndent()
        //@formatter:on
        int start = contents.indexOf('meth(S')
        doTest(contents, start, 4, start, 4, contents.lastIndexOf('meth'), 4)
    }

    @Test // GRECLIPSE-1573
    void testOverloaded4() {
        //@formatter:off
        String contents = '''\
            class LotsOfMethods {
              def meth() { }
              def meth(int a) { }
              def meth(String a) { }
            }
            new LotsOfMethods().meth(1)
            new LotsOfMethods().meth()
            new LotsOfMethods().meth('')
            '''.stripIndent()
        //@formatter:on
        int start = contents.lastIndexOf('meth')
        doTest(contents, start, 4, contents.indexOf('meth(S'), 4, start, 4)
    }

    @Test
    void testOverloaded5() {
        //@formatter:off
        String contents = '''\
            class LotsOfMethods {
              def meth() { }
              def meth(int a) { }
              def meth(String a) { }
            }
            new LotsOfMethods().meth(1)
            new LotsOfMethods().meth()
            new LotsOfMethods().meth(null)
            '''.stripIndent()
        //@formatter:on
        int start = contents.lastIndexOf('meth')
        doTest(contents, start, 4, contents.indexOf('meth(S'), 4, start, 4)
    }

    @Test
    void testDefaultParameters1() {
        //@formatter:off
        String contents = '''\
            class Default {
              def meth(int a, b = 2, c = 3) { }
            }
            new Default().meth(1)
            new Default().meth(1, 2)
            new Default().meth(1, 2, 3)
            new Default().meth(1, 2, 3, 4)
            new Default().meth
            '''.stripIndent()
        //@formatter:on

        // test the first method declaration
        // should match on all
        int start = contents.indexOf('meth')
        int len = 'meth'.length()

        int dontCare = contents.indexOf('meth', start)
        int start1 = dontCare
        int start2 = contents.indexOf('meth', start1 + 1)
        int start3 = contents.indexOf('meth', start2 + 1)
        int start4 = contents.indexOf('meth', start3 + 1)
        int start5 = contents.indexOf('meth', start4 + 1)
        int start6 = contents.indexOf('meth', start5 + 1)
        doTest(contents, start, len, start1, len, start2, len, start3, len, start4, len/*, start5, len, start6, len*/)
    }

    @Test @NotYetImplemented // This doesn't work because inferencing engine gets confused when overloaded methods have same number of arguments
    void testDefaultParameters1a() {
        //@formatter:off
        String contents = '''\
            class Default {
              def meth(int a, b = 1, c = 2) { }
              def meth(String a) { }
            }
            new Default().meth(1)
            new Default().meth(1, 2)
            new Default().meth(1, 2, 3)
            new Default().meth(1, 2, 3, 4)
            new Default().meth
            '''.stripIndent()
        //@formatter:on

        // test the first method declaration
        // should match on all
        int start = contents.indexOf('meth')
        int len = 'meth'.length()

        int dontCare = contents.indexOf('meth', start + 1)
        int start1 = dontCare
        int start2 = contents.indexOf('meth', start1 + 1)
        int start3 = contents.indexOf('meth', start2 + 1)
        int start4 = contents.indexOf('meth', start3 + 1)
        int start5 = contents.indexOf('meth', start4 + 1)
        int start6 = contents.indexOf('meth', start5 + 1)
        doTest(contents, start, len, start1, len, start2, len, start3, len, start4, len, start5, len, start6, len)
    }

    @Test @NotYetImplemented // This doesn't work because inferencing engine gets confused when overloaded methods have same number of arguments
    void testDefaultParameters2() {
        //@formatter:off
        String contents = '''\
            class Default {
              def meth(int a, b = 1, c = 2) { }
              def meth(String a) { }
            }
            new Default().meth(1)
            new Default().meth(1, 2)
            new Default().meth(1, 2, 3)
            new Default().meth(1, 2, 3, 4)
            new Default().meth
            '''.stripIndent()
        //@formatter:on

        // test the second method declaration
        // should match on
        int start = contents.indexOf('meth')
        start = contents.indexOf('meth', start + 1)
        int len = 'meth'.length()

        int start1 = start
        int start2 = contents.indexOf('meth', start1 + 1)
        int start3 = contents.indexOf('meth', start2 + 1)
        int start4 = contents.indexOf('meth', start3 + 1)
        int start5 = contents.indexOf('meth', start4 + 1)
        int start6 = contents.indexOf('meth', start5 + 1)
        doTest(contents, start, len, start1, len, start2, len, start5, len, start6, len)
    }

    @Test // on-demand imports should not be seen as Object
    void testStarImports() {
        //@formatter:off
        String contents = '''\
            |import java.lang.Object
            |import java.lang.*
            |import foo.bar.*
            |Object object
            |'''.stripMargin()
        //@formatter:on

        int offset = contents.lastIndexOf('Object'), length = 'Object'.length()
        doTest(contents, offset, length, contents.indexOf('java.lang.Object'), 16, offset, length)
    }

    @Test // GRECLIPSE-1363
    void testStaticImports1() {
        addGroovySource '''\
            class Other {
                public static int FOO
            }
            ''', 'Other', 'p'

        //@formatter:off
        String contents = '''\
            |import static p.Other.FOO
            |FOO
            |p.Other.FOO
            |'''.stripMargin()
        //@formatter:on

        int offset = contents.indexOf('FOO')
        int length = 'FOO'.length()
        int start1 = offset
        int start2 = contents.indexOf('FOO', offset + 1)
        int start3 = contents.lastIndexOf('FOO')
        doTest(contents, offset, length, start1, length, start2, length, start3, length)
    }

    @Test // GRECLIPSE-1363
    void testStaticImports2() {
        addGroovySource '''\
            class Other {
                public static int FOO
            }
            ''', 'Other', 'p'

        //@formatter:off
        String contents = '''\
            |import static p.Other.FOO as BAR
            |BAR
            |p.Other.FOO
            |'''.stripMargin()
        //@formatter:on

        int offset = contents.indexOf('FOO')
        int length = 'FOO'.length()
        int start1 = offset
        int start2 = contents.lastIndexOf('BAR')
        int start3 = contents.lastIndexOf('FOO')
        doTest(contents, offset, length, start1, length, start2, length, start3, length)
    }

    @Test
    void testStaticImports3() {
        addGroovySource '''\
            class Other {
                static int FOO
                static boolean BAR() {}
            }
            ''', 'Other', 'p'

        //@formatter:off
        String contents = '''\
            |import static p.Other.BAR
            |BAR()
            |p.Other.BAR()
            |'''.stripMargin()
        //@formatter:on

        int offset = contents.indexOf('BAR')
        int length = 'BAR'.length()
        int start1 = offset
        int start2 = contents.indexOf('BAR', start1 + 1)
        int start3 = contents.indexOf('BAR', start2 + 1)
        doTest(contents, offset, length, start1, length, start2, length, start3, length)
    }

    @Test
    void testStaticImports4() {
        addGroovySource '''\
            class Other {
                static int FOO
                static boolean BAR() {}
            }
            ''', 'Other', 'p'

        //@formatter:off
        String contents = '''\
            |import static p.Other.BAR
            |import p.Other
            |Other
            |p.Other.BAR
            |'''.stripMargin()
        //@formatter:on

        int offset = contents.indexOf('p.Other')
        int length = 'p.Other'.length()
        int start1 = offset
        int start2 = contents.indexOf('p.Other', start1 + length)
        int start3 = contents.indexOf('Other', start2 + length)
        int start4 = contents.lastIndexOf('p.Other')
        doTest(contents, offset, length, start1, length, start2, length, start3, 'Other'.length(), start4, length)
    }
}
