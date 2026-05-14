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
package org.codehaus.groovy.eclipse.test.core.util

import static org.junit.Assert.assertEquals

import groovy.transform.CompileStatic

import org.codehaus.groovy.eclipse.core.ISourceBuffer
import org.codehaus.groovy.eclipse.core.impl.StringSourceBuffer
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder
import org.junit.Ignore
import org.junit.Test

@CompileStatic
final class ExpressionFinderTests {

    /**
     * Tests the given expression, assuming the cursor is at end if the test string.
     */
    private void doFind(String expected) {
        ExpressionFinder finder = new ExpressionFinder()
        StringSourceBuffer sb = new StringSourceBuffer(expected)
        assert finder.findForCompletions(sb, expected.length() - 1) == expected
    }

    /**
     * Tests the expression from a given offset in the test string.
     */
    private void doFind(String test, String expected, int offset) {
        ExpressionFinder finder = new ExpressionFinder()
        StringSourceBuffer sb = new StringSourceBuffer(test)
        assert finder.findForCompletions(sb, offset) == expected
    }

    /**
     * Tests the splitting of an expression into an expression and prefix part for completion.
     */
    private void doSplit(String test, String expr, String prefix) {
        ExpressionFinder finder = new ExpressionFinder()
        StringSourceBuffer sb = new StringSourceBuffer(test)
        String foundExpr = finder.findForCompletions(sb, test.length() - 1)
        String[] split = finder.splitForCompletion(foundExpr)
        assert split[0] == expr
        assert split[1] == prefix
    }

    /**
     * Test that splitting for completion fails as expected.
     */
    private void failSplit(String test) {
        ExpressionFinder finder = new ExpressionFinder()
        String[] split = finder.splitForCompletion(test)
        assert split.length == 2
        assert split[0] == ''
        assert split[1] == null
    }

    @Test
    void testSimple1() {
        doFind('hello')
    }

    @Test
    void testSimple2() {
        doFind('hello.name')
    }

    @Test
    void testSimple3() {
        doFind('hello.getName().')
    }

    @Test
    void testMultiple1() {
        doFind('hello.location.name')
    }

    @Test
    void testMultiple2() {
        doFind('hello.getLocation().name')
    }

    @Test
    void testMultiple3() {
        doFind('hello.location.getName().')
    }

    @Test
    void testDotted1() {
        doFind('hello.')
    }

    @Test
    void testDotted2() {
        doFind('hello.location.')
    }

    @Test
    void testComplex1() {
        doFind('10.times { println } .')
    }

    @Test
    void testComplex2() {
        doFind('a[20].')
    }

    @Test
    void testComplex3() {
        doFind('a[20].thing')
    }

    @Test @Ignore('DEFERRED: until from within method completion is implemented')
    void testComplex4() {
        doFind('method(a, b,')
    }

    @Test
    void testComplex5() {
        doFind('[1, 2, 3].collect { it.toString() } .')
    }

    @Test
    void testComplex6() {
        doFind('[1, 2, 3].collect { it.toString() }[0].')
    }

    @Test
    void testInString1() {
        // Stop when newline separates expressions.
        // Don't mess with the dot's, the auto formatter eats spaces.
        // .....0.........1.........2..
        // .....012345678901234.5678901
        doFind('println \'hello\'\n10.times', '10.tim', 21)
    }

    @Test
    void testInString2() {
        // Find funky expression.
        // .....0.........1.........2.........3.........4....
        // .....012345678901234567890123456789012345678901234
        doFind('a = [1, 2, 3]; a.collect { it.toString() }.each {', 'a.collect { it.toString() }.ea', 44)
    }

    @Test
    void testInString3() {
        // Do it though newlines.
        // .....0.........1.........2.........3.........4........
        // .....01234567890123456.78901234567890123456789012.3456
        doFind('a = [1, 2, 3]; a.\ncollect { it.toString() }\n.each {', 'a.\ncollect { it.toString() }\n.ea', 46)
    }

    @Test
    void testSplit1() {
        doSplit('hello', 'hello', null)
    }

    @Test
    void testSplit2() {
        doSplit('hello.', 'hello', '')
    }

    @Test
    void testSplit3() {
        doSplit('hello.name', 'hello', 'name')
    }

    @Test
    void testSplit4() {
        doSplit('greet().name', 'greet()', 'name')
    }

    @Test
    void testSplit5() {
        doSplit('list[10].do', 'list[10]', 'do')
    }

    @Test
    void testSplit6() {
        doSplit('list.collect { it.toString } .', 'list.collect { it.toString }', '')
    }

    @Test
    void testSplit7() {
        doSplit('list.collect { it.toString } .class', 'list.collect { it.toString }', 'class')
    }

    @Test
    void testSplit8() {
        doSplit('[].do', '[]', 'do')
    }

    @Test
    void testSplit9() {
        doSplit('\n\n\t[].do', '[]', 'do')
    }

    @Test
    void testSplit10() {
        doSplit('\n\n\t[10].do', '[10]', 'do')
    }

    @Test
    void testSplit11() {
        doSplit('\n\n\t[[]].do', '[[]]', 'do')
    }

    @Test
    void testSplit12() {
        doSplit('\n\n\t[1:1].do', '[1:1]', 'do')
    }

    @Test
    void testSplit13() {
        doSplit('[x:1,y:2,z:3]*.g', '[x:1,y:2,z:3]', 'g')
    }

    @Test
    void testSplit14() {
        doSplit('[x:1,y:2,z:3].g', '[x:1,y:2,z:3]', 'g')
    }

    @Test
    void testSplit15() {
        doSplit('[x:1,y:2,z:3]?.g', '[x:1,y:2,z:3]', 'g')
    }

    @Test
    void testFailSplit1() {
        failSplit('boo()')
    }

    @Test
    void testFailSplit2() {
        failSplit('boo[]')
    }

    @Test
    void testFailSplit3() {
        failSplit('boo{}')
    }

    @Test
    void testFailSplit4() {
        failSplit('\'boo\'')
    }

    @Test
    void testFailSplit5() {
        failSplit('//\n')
    }

    @Test
    void testFailSplit6() {
        failSplit('//fdsafdsfasddsfa\n     ')
    }

    @Test
    void testFailSplit7() {
        failSplit('//fdsafdsfasddsfa\nboo[]     ')
    }

    @Test
    void testFailSplit8() {
        failSplit('/*fdsafdsfasddsfa*/ \nboo[]     ')
    }

    @Test
    void testFailSplit9() {
        failSplit('/* // fdsafdsfasddsfa*/ \nboo[]     ')
    }

    @Test
    void testParenEOF() {
        String test = 'def b = thing()\na.'
        doFind(test, 'a.', test.length() - 1)
    }

    @Test
    void testBraceEOF() {
        String test = 'def blah() { a.'
        doFind(test, 'a.', test.length() - 1)
    }

    @Test
    void testBraceEOFNoSpace() {
        String test = 'def blah() {a.'
        doFind(test, 'a.', test.length() - 1)
    }

    @Test
    void testWithLineComment() {
        String test = '// a comment\na.'
        doFind(test, 'a.', test.length() - 1)
    }

    @Test
    void testWithLineComment2() {
        String test = '//\t\thelp.\n\t\ta.'
        doFind(test, 'a.', test.length() - 1)
    }

    @Test
    void testWithLineComment3() {
        String test = 'def a = 10\n//\t\thelp.\n\t\ta.'
        doFind(test, 'a.', test.length() - 1)
    }

    @Test
    void testWithBlockComment() {
        String test = '/* a block comment */\na.'
        doFind(test, 'a.', test.length() - 1)
    }

    @Test
    void testNewExpression() {
        doFind('new File(\'.\').')
        doFind('new File(\'.\').canon')
    }

    @Test
    void testProblem() {
        // this used to throw an exception, but should not
        ExpressionFinder finder = new ExpressionFinder()
        String test = '.'
        StringSourceBuffer sb = new StringSourceBuffer(test)
        assert finder.findForCompletions(sb, test.length() - 1) == null
    }

    @Test
    void testIdentifierExpression() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { bbbb. } }'
        String completionLocation = 'class Test { def testFunction() { bbbb.'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('bbbb.', expression)
    }

    @Test
    void testParenExpression() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { (bbbb). } }'
        String completionLocation = 'class Test { def testFunction() { (bbbb).'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('(bbbb).', expression)
    }

    @Test
    void testArrayElementExpressionInParen() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { (bbbb[10]). } }'
        String completionLocation = 'class Test { def testFunction() { (bbbb[10]).'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('(bbbb[10]).', expression)
    }

    @Test
    void testArrayElementExpression() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { bbbb[10]. } }'
        String completionLocation = 'class Test { def testFunction() { bbbb[10].'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('bbbb[10].', expression)
    }

    @Test
    void testParenInParenExpression() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { (([10])). } }'
        String completionLocation = 'class Test { def testFunction() { (([10])).'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('(([10])).', expression)
    }

    @Test
    void testParenInParenExpression2() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { (([10\n\n  ])). } }'
        String completionLocation = 'class Test { def testFunction() { (([10\n\n  ])).'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('(([10\n\n  ])).', expression)
    }

    @Test
    void testStringExpression() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { "13". } }'
        String completionLocation = 'class Test { def testFunction() { "13".'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('"13".', expression)
    }

    @Test
    void testStringExpressionAfterOtherExpressionAndNewLine() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { int a = 10\n   "13". } }'
        String completionLocation = 'class Test { def testFunction() { int a = 10\n   "13".'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('"13".', expression)
    }

    @Test
    void testStringExpressionAfterOtherExpressionAndSemi() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { int a = 10;   "13". } }'
        String completionLocation = 'class Test { def testFunction() { int a = 10;   "13".'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('"13".', expression)
    }

    @Test
    void testExpressionInGString() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { "anyString${a.}" } }'
        String completionLocation = 'class Test { def testFunction() { "anyString${a.'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('a.', expression)
    }

    @Test
    void testExpressionInParenAfterNewLine() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { int a = 100\n    (new ArrayList()). } }'
        String completionLocation = 'class Test { def testFunction() { int a = 100\n    (new ArrayList()).'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('(new ArrayList()).', expression)
    }

    @Test
    void testExpressionAfterNewLine() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { int a = 100\n    new ArrayList(). } }'
        String completionLocation = 'class Test { def testFunction() { int a = 100\n    new ArrayList().'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('new ArrayList().', expression)
    }

    @Test @Ignore
    void testNoExpression1() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() {    } }'
        String completionLocation = 'class Test { def testFunction() { '
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals(null, expression)
    }

    @Test
    void testNoExpression2() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { int a = 100    } }'
        String completionLocation = 'class Test { def testFunction() { int a = 100  '
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('', expression)
    }

    @Test
    void testNoExpression3() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { int a = 100\n    } }'
        String completionLocation = 'class Test { def testFunction() { int a = 100\n  '
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('', expression)
    }

    @Test
    void testExpressionAfterNumber() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { int a = 100\n    } }'
        String completionLocation = 'class Test { def testFunction() { int a = 100'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('100', expression)
    }

    @Test
    void testExpressionAfterStringLiteral() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { def a = "fff"\n    } }'
        String completionLocation = 'class Test { def testFunction() { int a = "fff"'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals(null, expression)
    }

    @Test
    void testExpressionInParenAfterSemi() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { int a = 100;    (new ArrayList()). } }'
        String completionLocation = 'class Test { def testFunction() { int a = 100;    (new ArrayList()).'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('(new ArrayList()).', expression)
    }

    @Test
    void testExpressionForNewGenericType() {
        ExpressionFinder finder = new ExpressionFinder()
        String source = 'class Test { def testFunction() { int a = 100;    (new ArrayList<String>()). } }'
        String completionLocation = 'class Test { def testFunction() { int a = 100;    (new ArrayList<String>()).'
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source)

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1)
        assertEquals('(new ArrayList<String>()).', expression)
    }
}
