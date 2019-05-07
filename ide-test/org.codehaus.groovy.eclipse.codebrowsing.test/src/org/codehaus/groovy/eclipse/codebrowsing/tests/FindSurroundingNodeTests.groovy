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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser

import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.junit.Assert
import org.junit.Test

final class FindSurroundingNodeTests extends BrowsingTestSuite {

    private GroovyCompilationUnit checkRegion(String contents, Region initialRegion, Region expectedRegion) {
        GroovyCompilationUnit unit = addGroovySource(contents, nextUnitName())
        return checkRegion(contents, unit, initialRegion, expectedRegion)
    }

    private GroovyCompilationUnit checkRegion(String contents, GroovyCompilationUnit unit, Region initialRegion, Region expectedRegion) {
        FindSurroundingNode finder = new FindSurroundingNode(initialRegion)
        IASTFragment result = finder.doVisitSurroundingNode(unit.moduleNode)
        String expect = contents.substring(expectedRegion.offset, expectedRegion.end)
        String actual = contents.substring(result.start, result.end)
        Assert.assertEquals(expect, actual)
        return unit
    }

    @Test
    void testFindSurroundingNode1() {
        String contents = '''\
            |import org.codehaus.groovy.ast.ASTNode
            |class Clazz { }
            |'''.stripMargin()

        Region initialRegion = new Region(contents.indexOf('A'), 0)
        Region expectedRegion = new Region(0, 'import org.codehaus.groovy.ast.ASTNode'.length())
        checkRegion(contents, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding2() {
        String contents = '''\
            |import java.util.List
            |class Clazz { }
            |'''.stripMargin()

        Region initialRegion = new Region(contents.indexOf('C'), 0)
        Region expectedRegion = new Region(contents.indexOf('c'), 'class Clazz { }'.length())
        checkRegion(contents, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding3() {
        String contents = '''\
            |import java.util.List
            |class Clazz {
            |  def method() {
            |    def x
            |  }
            |}
            |'''.stripMargin()

        Region initialRegion = new Region(contents.indexOf('x'), 0)
        Region expectedRegion = new Region(contents.indexOf('x'), 'x'.length())
        GroovyCompilationUnit unit = checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('def x'), 'def x'.length())
        unit = checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('{\n    def x'), '{\n    def x\n  }'.length())
        unit = checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('def method()'), 'def method() {\n    def x\n  }'.length())
        unit = checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('class Clazz'), 'class Clazz {\n  def method() {\n    def x\n  }\n}'.length())
        unit = checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(0, contents.length())
        checkRegion(contents, unit, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding4() {
        String contents = '''\
            |if (true) {
            |  def x
            |} else {
            |  def y
            |}
            |'''.stripMargin()

        Region initialRegion = new Region(contents.indexOf('x'), 0)
        Region expectedRegion = new Region(contents.indexOf('x'), 'x'.length())
        GroovyCompilationUnit unit = checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('def x'), 'def x'.length())
        unit = checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('{\n  def x'), (isParrotParser() ? '{\n  def x\n}' : '{\n  def x\n} ').length())
        unit = checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(0, contents.length() - 1)
        unit = checkRegion(contents, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding5() {
        String contents = 'foo() .foo()'
        Region initialRegion = new Region(contents.indexOf('f'), 1)
        Region expectedRegion = new Region(contents.indexOf('foo'), 'foo'.length())
        GroovyCompilationUnit unit = checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('foo() '), (isParrotParser() ? 'foo()' : 'foo() ').length())
        unit = checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(0, contents.length())
        unit = checkRegion(contents, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding5a() {
        String contents = 'foo() .foo()'
        Region initialRegion = new Region(contents.lastIndexOf('f'), 1)
        Region expectedRegion = new Region(contents.lastIndexOf('foo'), 'foo'.length())
        GroovyCompilationUnit unit = checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.lastIndexOf('foo()'), 'foo()'.length())
        unit = checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(0, contents.length())
        unit = checkRegion(contents, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding6() {
        String contents = 'while(foo.bar) { }'
        Region initialRegion = new Region(contents.indexOf('f'), 0)
        Region expectedRegion = new Region(contents.indexOf('foo'), 'foo'.length())
        GroovyCompilationUnit unit = checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('foo.bar'), 'foo.bar'.length())
        unit = checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(0, contents.length())
        unit = checkRegion(contents, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding7() {
        String contents = 'while(foo.bar) { }\nclass A { \ndef x = 7 + 9}'
        Region initialRegion = new Region(contents.indexOf('7'), 0)
        Region expectedRegion = new Region(contents.indexOf('7'), '7'.length())
        GroovyCompilationUnit unit = checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('7 + 9'), '7 + 9'.length())
        unit = checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('def x = 7 + 9'), 'def x = 7 + 9'.length())
        unit = checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('class A { \ndef x = 7 + 9}'), 'class A { \ndef x = 7 + 9}'.length())
        unit = checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(0, contents.length())
        unit = checkRegion(contents, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding8a() {
        String contents = 'foo()++'
        Region initialRegion = new Region(contents.lastIndexOf('f'), 1)
        Region expectedRegion = new Region(contents.lastIndexOf('foo'), 'foo'.length())
        GroovyCompilationUnit unit = checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.lastIndexOf('foo()'), 'foo()'.length())
        unit = checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(0, contents.length())
        unit = checkRegion(contents, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding8b() {
        String contents = '++foo()'
        Region initialRegion = new Region(contents.lastIndexOf('f'), 1)
        Region expectedRegion = new Region(contents.lastIndexOf('foo'), 'foo'.length())
        GroovyCompilationUnit unit = checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.lastIndexOf('foo()'), 'foo()'.length())
        unit = checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(0, contents.length())
        unit = checkRegion(contents, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding8c() {
        String contents = '!foo()'
        Region initialRegion = new Region(contents.lastIndexOf('f'), 1)
        Region expectedRegion = new Region(contents.lastIndexOf('foo'), 'foo'.length())
        GroovyCompilationUnit unit = checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.lastIndexOf('foo()'), 'foo()'.length())
        unit = checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(0, contents.length())
        unit = checkRegion(contents, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding9a() {
        String contents = '1..9'
        Region initialRegion = new Region(contents.lastIndexOf('9'), 0)
        Region expectedRegion = new Region(contents.lastIndexOf('9'), '9'.length())
        checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(0, contents.length())
        checkRegion(contents, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding9b() {
        String contents = '1..9'
        Region initialRegion = new Region(contents.lastIndexOf('1'), 0)
        Region expectedRegion = new Region(contents.lastIndexOf('1'), 1)
        def unit = checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(0, contents.length())
        checkRegion(contents, unit, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding10() {
        String contents = 'def x = a ? b1 : b'
        Region initialRegion = new Region(contents.indexOf('a'), 0)
        Region expectedRegion = new Region(contents.indexOf('a'), 1)
        def unit = checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('a'), 'a ? b1 : b'.length())
        checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(0, contents.length())
        checkRegion(contents, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding11() {
        String contents = 'def x = a ?: b'
        Region initialRegion = new Region(contents.indexOf('a'), 0)
        Region expectedRegion = new Region(contents.indexOf('a'), 1)
        def unit = checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('a'), 'a ?: b'.length())
        checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(0, contents.length())
        checkRegion(contents, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding12() {
        String contents = 'def x = [a : b]'
        Region initialRegion = new Region(contents.indexOf('a'), 0)
        Region expectedRegion = new Region(contents.indexOf('a'), 1)
        def unit = checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('a'), 'a : b'.length())
        checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('[a'), '[a : b]'.length())
        checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(0, contents.length())
        checkRegion(contents, initialRegion, expectedRegion)
    }

    @Test
    void testFindSurrounding13() {
        String contents = '''\
            |enum E {
            |  X {
            |    def m() {
            |      def i = 1
            |    }
            |  },
            |  Y {
            |    def m() {
            |      def j = 2
            |    }
            |  };
            |  abstract def m();
            |}
            |'''.stripMargin()

        Region initialRegion = new Region(contents.indexOf('i'), 0)
        Region expectedRegion = new Region(contents.indexOf('i'), 1)
        def unit = checkRegion(contents, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('def i'), 'def i'.length())
        checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('def i'), 'def i = 1'.length())
        checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('{\n      def i'), '{\n      def i = 1\n    }'.length())
        checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('def m()'), 'def m() {\n      def i = 1\n    }'.length())
        checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('X {') + 2, '{\n    def m() {\n      def i = 1\n    }\n  }'.length())
        checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(contents.indexOf('X {'), 'X {\n    def m() {\n      def i = 1\n    }\n  }'.length())
        checkRegion(contents, unit, initialRegion, expectedRegion)

        initialRegion = expectedRegion
        expectedRegion = new Region(0, contents.length() - 1)
        checkRegion(contents, unit, initialRegion, expectedRegion)
    }
}
