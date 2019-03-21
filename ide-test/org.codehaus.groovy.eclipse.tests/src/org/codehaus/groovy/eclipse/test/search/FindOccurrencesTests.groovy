/*
 * Copyright 2009-2017 the original author or authors.
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

import java.lang.reflect.Array

import org.codehaus.groovy.eclipse.search.GroovyOccurrencesFinder
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.groovy.core.util.ReflectionUtils
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

/**
 * Tests for {@link GroovyOccurrencesFinder}.
 */
public final class FindOccurrencesTests extends GroovyEclipseTestSuite {

    private void doTest(String contents, int start, int length, int... expected) {
        GroovyCompilationUnit unit = addGroovySource(contents, 'Occurrences')
        unit.becomeWorkingCopy(null)
        try {
            GroovyOccurrencesFinder finder = new GroovyOccurrencesFinder()
            finder.setGroovyCompilationUnit(unit)
            finder.initialize(null, start, length)
            Object actual = finder.getOccurrences()

            final int n = Array.getLength(actual)
            if (n != expected.length/2) {
                Assert.fail('Wrong number of occurrences found. expecting:\n' + Arrays.toString(expected) + '\nbut found:\n' + printOccurrences(actual, n))
            }
            for (int i = 0; i < n; i += 1) {
                Object o = Array.get(actual, i)
                int off = (Integer) ReflectionUtils.getPrivateField(o.getClass(), 'fOffset', o),
                    len = (Integer) ReflectionUtils.getPrivateField(o.getClass(), 'fLength', o)
                if (off != expected[i*2] || len != expected[i*2+1]) {
                    Assert.fail('Problem in Occurrence ' + i + ' expecting:\n' + Arrays.toString(expected) + '\nbut found:\n' + printOccurrences(actual, n))
                }
            }
        } finally {
            unit.discardWorkingCopy()
        }
    }

    private String printOccurrences(Object array, int length) {
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < length; i += 1) {
            sb.append(Array.get(array, i)).append('\n')
        }
        return sb.toString()
    }

    //

    @Test
    void testFindLocalOccurrences1() {
        String contents = 'def x\nx'
        doTest(contents, contents.lastIndexOf('x'), 1, contents.indexOf('x'), 1, contents.lastIndexOf('x'), 1)
    }

    @Test
    void testFindLocalOccurrences2() {
        String contents = 'def x(x) {\nx}'
        doTest(contents, contents.lastIndexOf('x'), 1, contents.indexOf('(x')+1, 1, contents.lastIndexOf('x'), 1)
    }

    @Test
    void testFindLocalOccurrences3() {
        String contents = 'nuthin\ndef x(int x) {\nx}'
        int afterParen = contents.indexOf('(')
        doTest(contents, contents.lastIndexOf('x'), 1, contents.indexOf('x', afterParen), 1, contents.lastIndexOf('x'), 1)
    }

    @Test
    void testFindLocalOccurrences4() {
        // looking for the method declaration, not the parameter
        String contents = 'nuthin\ndef x(int x) {\nx}'
        doTest(contents, contents.indexOf('x'), 1, contents.indexOf('x'), 1)
    }

    @Test
    void testFindForLoopOccurrences() {
        String contents = 'for (x in []) {\n' +
                'x }'
        doTest(contents, contents.indexOf('x'), 1, contents.indexOf('x'), 1, contents.lastIndexOf('x'), 1)
    }

    @Test @Ignore('Not working now; see GROOVY-4620 and GRECLIPSE-951')
    void testFindPrimitive() {
        String contents = 'int x(int y) {\nint z}\n int a'
        int length = 'int'.length()
        int first = contents.indexOf('int')
        int second = contents.indexOf('int', first+1)
        int third = contents.indexOf('int', second+1)
        int fourth = contents.indexOf('int', third+1)

        doTest(contents, second, length, first, length, second, length, third, length, fourth, length)
    }

    @Test
    void testFindProperty() {
        String contents = 'class X {\n' +
                'def foo\n' +
                '}\n' +
                'new X().foo\n' +
                'new X().foo()\n';  // require a new line here or else Occurrence finding will crash.  See Bug 339614

        int length = 'foo'.length()
        int first = contents.indexOf('foo')
        int second = contents.indexOf('foo', first+1)
        int third = contents.indexOf('foo', second+1)
        doTest(contents, second, length, first, length, second, length, third, length)
    }

    @Test
    void testFindField() {
        String contents = 'class X {\n' +
                'public def foo\n' +
                '}\n' +
                'new X().foo\n' +
                'new X().foo()\n';  // require a new line here or else Occurrence finding will crash.  See Bug 339614

        int length = 'foo'.length()
        int first = contents.indexOf('foo')
        int second = contents.indexOf('foo', first+1)
        int third = contents.indexOf('foo', second+1)
        doTest(contents, second, length, first, length, second, length, third, length)
    }

    @Test
    void testFindGStringOccurrences1() {
        String contents = 'def xxx\nxxx \"$xxx\"\n\"$xxx\"\n\"${xxx}\"\n' + // first three are matches
                '\"xxx\"\n\'xxx\'\n\'$xxx\'\n\'${xxx}\'' // these aren't matches
        int length = 'xxx'.length()
        int decl = contents.indexOf('xxx')
        int first = contents.indexOf('xxx', decl+1)
        int second = contents.indexOf('xxx', first+1)
        int third = contents.indexOf('xxx', second+1)
        int fourth = contents.indexOf('xxx', third+1)
        doTest(contents, decl, 1, decl, length, first, length, second, length, third, length, fourth, length)
    }

    @Test
    void testFindGStringOccurrences2() {
        String contents = 'def i\ni \"$i\"\n\"$i\"\n\"${i}\"\n' + // first three are matches
                '\"i\"\n\'i\'\n\'$i\'\n\'${i}\'' // these aren't matches
        int length = 'i'.length()
        int decl = contents.indexOf('i')
        int first = contents.indexOf('i', decl+1)
        int second = contents.indexOf('i', first+1)
        int third = contents.indexOf('i', second+1)
        int fourth = contents.indexOf('i', third+1)
        doTest(contents, decl, 1, decl, length, first, length, second, length, third, length, fourth, length)
    }

    @Test // GRECLIPSE-1031
    void testFindStaticMethods() {
        String contents =
            'class Static {\n' +
            '  static staticMethod()  { staticMethod() }\n' +
            '  static { staticMethod() }\n' +
            '  { staticMethod() }\n' +
            '  def t = staticMethod()\n' +
            '  def x() {\n' +
            '    def a = staticMethod()\n' +
            '    def b = staticMethod  \n' +
            '    Static.staticMethod 3, 4, 5\n' +
            '    Static.staticMethod(3, 4, 5)\n' +
            '  }\n' +
            '}'

        String methName = 'staticMethod'
        int len = methName.length()

        int start = contents.indexOf(methName)
        int start1 = contents.indexOf(methName)
        int start2 = contents.indexOf(methName, start1 + 1)
        int start3 = contents.indexOf(methName, start2 + 1)
        int start4 = contents.indexOf(methName, start3 + 1)
        int start5 = contents.indexOf(methName, start4 + 1)
        int start6 = contents.indexOf(methName, start5 + 1)
        int start7 = contents.indexOf(methName, start6 + 1)
        int start8 = contents.indexOf(methName, start7 + 1)
        int start9 = contents.indexOf(methName, start8 + 1)
        doTest(contents, start, len, start1, len, start2, len, start3, len, start4, len, start5, len, start6, len, start7, len, start8, len, start9, len)
    }

    @Test // GRECLIPSE-1031
    void testFindStaticMethods18() {
        String contents =
            'class Static {\n' +
            '  static staticMethod(nuthin)  { }\n' +
            '  def x() {\n' +
            '    def z = staticMethod \n' +
            '    def a = staticMethod 3, 4, 5\n' +
            '    def b = staticMethod(3, 4, 5) \n' +
            '    def c = Static.staticMethod 3, 4, 5\n' +
            '    def d = Static.staticMethod(3, 4, 5) \n' +
            '    // this one is commented out because of GRECLIPSE-4761' +
            '    // def z = staticMethod 3\n' +
            '  }\n' +
            '}'

        String methName = 'staticMethod'
        int len = methName.length()

        int start = contents.indexOf(methName)
        int start1 = contents.indexOf(methName)
        int start2 = contents.indexOf(methName, start1 + 1)
        int start3 = contents.indexOf(methName, start2 + 1)
        int start4 = contents.indexOf(methName, start3 + 1)
        int start5 = contents.indexOf(methName, start4 + 1)
        int start6 = contents.indexOf(methName, start5 + 1)
        doTest(contents, start, len, start1, len, start2, len, start3, len, start4, len, start5, len, start6, len)
    }

    @Test // GRECLIPSE-1023
    void testInnerClass() {
        String contents =
            'class Other2 {\n' +
            '        class Inner { }\n' +
            '        Other2.Inner f\n' +
            '        Inner g\n' +
            '}'

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
        String contents =
            'class Other2 {\n' +
            '        class Inner { }\n' +
            '        Other2.Inner f\n' +
            '        Inner g\n' +
            '}'

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
        String contents =
            'class Other2 {\n' +
            '        class Inner { }\n' +
            '        Other2.Inner f\n' +
            '        Inner g\n' +
            '}'

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
        addGroovySource(
                'class Other {\n' +
                '  class Inner { }\n' +
                '}', 'Other')
        String contents =
            'import Other.Inner\n' +
            'Other.Inner f\n' +
            'Inner g'

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
        String contents = 'import javax.swing.text.html.HTML\n' +
                'Map<HTML, ? extends HTML> h\n' +
                'HTML i'

        String name = 'HTML'
        int len = name.length()

        int start1 = contents.indexOf(name)
        int start2 = contents.indexOf(name, start1 + 1)
        int start3 = contents.indexOf(name, start2 + 1)
        int start4 = contents.indexOf(name, start3 + 1)
        int start = start1
        doTest(contents, start, len, start1, len, start2, len, start3, len, start4, len)
    }

    @Test// As before but use a different starting point
    void testGenerics2() {
        String contents = 'import javax.swing.text.html.HTML\n' +
        'Map<HTML, ? extends HTML> h\n' +
        'HTML i'

        String name = 'HTML'
        int len = name.length()

        int start1 = contents.indexOf(name)
        int start2 = contents.indexOf(name, start1 + 1)
        int start3 = contents.indexOf(name, start2 + 1)
        int start4 = contents.indexOf(name, start3 + 1)
        int start = start2
        doTest(contents, start, len, start1, len, start2, len, start3, len, start4, len)
    }

    @Test // As before but use a different starting point
    void testGenerics3() {
        String contents = 'import javax.swing.text.html.HTML\n' +
        'Map<HTML, ? extends HTML> h\n' +
        'HTML i'

        String name = 'HTML'
        int len = name.length()

        int start1 = contents.indexOf(name)
        int start2 = contents.indexOf(name, start1 + 1)
        int start3 = contents.indexOf(name, start2 + 1)
        int start4 = contents.indexOf(name, start3 + 1)
        int start = start4
        doTest(contents, start, len, start1, len, start2, len, start3, len, start4, len)
    }

    @Test // GRECLIPSE-1219
    void testAnnotationOnImport() {
        String contents = '@Deprecated\n' +
                'import javax.swing.text.html.HTML\n' +
                'Deprecated'

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
        String contents =
                'class LotsOfMethods { \n' +
                '  def meth() { }\n' +
                '  def meth(int a) { }\n' +
                '  def meth(String a, LotsOfMethods b) { }\n' +
                '}\n' +
                'new LotsOfMethods().meth(1)\n' +
                'new LotsOfMethods().meth(\"\", null)\n' +
                'new LotsOfMethods().meth()'
        int start = contents.indexOf('meth()')
        doTest(contents, start, 4, start, 4, contents.lastIndexOf('meth()'), 4)
    }

    @Test
    void testOverloaded2() {
        String contents =
                'class LotsOfMethods { \n' +
                '  def meth() { }\n' +
                '  def meth(int a) { }\n' +
                '  def meth(String a, LotsOfMethods b) { }\n' +
                '}\n' +
                'new LotsOfMethods().meth()\n' +
                'new LotsOfMethods().meth(\"\", null)\n' +
                'new LotsOfMethods().meth(1)\n'
        int start = contents.indexOf('meth(int')
        doTest(contents, start, 4, start, 4, contents.indexOf('meth(1)'), 4)
    }

    @Test
    void testOverloaded3() {
        String contents =
                'class LotsOfMethods { \n' +
                '  def meth() { }\n' +
                '  def meth(int a) { }\n' +
                '  def meth(String a, LotsOfMethods b) { }\n' +
                '}\n' +
                'new LotsOfMethods().meth(1)\n' +
                'new LotsOfMethods().meth()\n' +
                'new LotsOfMethods().meth(\"\", null)\n'
        int start = contents.indexOf('meth(S')
        doTest(contents, start, 4, start, 4, contents.lastIndexOf('meth'), 4)
    }

    @Test // GRECLIPSE-1573
    void testOverloaded4() {
        String contents =
                'class LotsOfMethods { \n' +
                '  def meth() { }\n' +
                '  def meth(int a) { }\n' +
                '  def meth(String a) { }\n' +
                '}\n' +
                'new LotsOfMethods().meth(1)\n' +
                'new LotsOfMethods().meth()\n' +
                'new LotsOfMethods().meth(\"\")\n'
        int start = contents.lastIndexOf('meth')
        doTest(contents, start, 4, contents.indexOf('meth(S'), 4, start, 4)
    }

    @Test
    void testOverloaded5() {
        String contents =
                'class LotsOfMethods { \n' +
                '  def meth() { }\n' +
                '  def meth(int a) { }\n' +
                '  def meth(String a) { }\n' +
                '}\n' +
                'new LotsOfMethods().meth(1)\n' +
                'new LotsOfMethods().meth()\n' +
                'new LotsOfMethods().meth(null)\n'
        int start = contents.lastIndexOf('meth')
        doTest(contents, start, 4, contents.indexOf('meth(S'), 4, start, 4)
    }

    @Test
    void testDefaultParameters1() {
        String contents = 'class Default {\n' +
                '  def meth(int a, b = 1, c = 2) { }\n' +
              //'  def meth(String a) { }\n' +
                '}\n' +
                'new Default().meth(1)\n' +
                'new Default().meth(1, 2)\n' +
                'new Default().meth(1, 2, 3)\n' +
                'new Default().meth(1, 2, 3, 4)\n' +
                'new Default().meth'
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
        doTest(contents, start, len, start1, len, start2, len, start3, len, start4, len, start5, len, start6, len)
    }

    @Test @Ignore('This doesn\'t work because inferencing engine gets confused when overloaded methods have same number of arguments')
    void testDefaultParameters1a() {
        String contents =
                'class Default {\n' +
                '  def meth(int a, b = 1, c = 2) { }\n' +
                '  def meth(String a) { }\n' +
                '}\n' +
                'new Default().meth(1)\n' +
                'new Default().meth(1, 2)\n' +
                'new Default().meth(1, 2, 3)\n' +
                'new Default().meth(1, 2, 3, 4)\n' +
                'new Default().meth'
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

    @Test @Ignore('This doesn\'t work because inferencing engine gets confused when overloaded methods have same number of arguments')
    void testDefaultParameters2() {
        String contents = 'class Default {\n' +
                '  def meth(int a, b = 1, c = 2) { }\n' +
                '  def meth(String a) { }\n' +
                '}\n' +
                'new Default().meth(1)\n' +
                'new Default().meth(1, 2)\n' +
                'new Default().meth(1, 2, 3)\n' +
                'new Default().meth(1, 2, 3, 4)\n' +
                'new Default().meth'
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

    @Test
    void testStaticImports1() {
        addGroovySource('class Other {\n  static int FOO\n static boolean BAR() { } }', 'Other', 'p')
        String contents =
                'import static p.Other.FOO\n' +
                'FOO\n' +
                'p.Other.FOO'
        int start = contents.indexOf('FOO')
        int len = 'FOO'.length()
        int start1 = start
        int start2 = contents.indexOf('FOO', start1 + 1)
        int start3 = contents.indexOf('FOO', start2 + 1)
        doTest(contents, start, len, start1, len, start2, len, start3, len)
    }

    @Test
    void testStaticImports2() {
        addGroovySource('class Other {\n  static int FOO\n static boolean BAR() { } }', 'Other', 'p')
        String contents =
                'import static p.Other.BAR\n' +
                'BAR\n' +
                'p.Other.BAR'
        int start = contents.indexOf('BAR')
        int len = 'BAR'.length()
        int start1 = start
        int start2 = contents.indexOf('BAR', start1 + 1)
        int start3 = contents.indexOf('BAR', start2 + 1)
        doTest(contents, start, len, start1, len, start2, len, start3, len)
    }

    @Test
    void testStaticImports3() {
        addGroovySource('class Other {\n  static int FOO\n static boolean BAR() { } }', 'Other', 'p')
        String contents =
                'import static p.Other.BAR\n' +
                'import p.Other\n' +
                'Other\n' +
                'p.Other.BAR'
        int start = contents.indexOf('p.Other')
        int len1 = 'p.Other'.length()
        int len = 'Other'.length()
        int start1 = start
        int start2 = contents.indexOf('Other', start1 + len1)
        int start3 = contents.indexOf('Other', start2 + 1)
        int start4 = contents.indexOf('p.Other', start3 + 1)
        doTest(contents, start, len1, start1, len1, start2, len, start3, len, start4, len1)
    }
}
