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

import static org.junit.Assert.*

import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentFactory
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentKind
import org.codehaus.groovy.eclipse.codebrowsing.fragments.BinaryExpressionFragment
import org.codehaus.groovy.eclipse.codebrowsing.fragments.FragmentVisitor
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment
import org.codehaus.groovy.eclipse.codebrowsing.fragments.MethodCallFragment
import org.codehaus.groovy.eclipse.codebrowsing.fragments.PropertyExpressionFragment
import org.codehaus.groovy.eclipse.codebrowsing.fragments.SimpleExpressionASTFragment
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.junit.Test

/**
 * Tests to see that ASTFragments are created correctly.
 */
final class ASTFragmentTests extends BrowsingTestSuite {

    @Test
    void testASTFragment1() {
        IASTFragment first = createFragmentFromText('a')
        IASTFragment second = createFragmentFromText('a')
        assertEquals("Wrong number of fragments: $first", 1, first.fragmentLength())
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.SIMPLE_EXPRESSION)
    }

    @Test
    void testASTFragment2() {
        IASTFragment first = createFragmentFromText('a + b')
        IASTFragment second = createFragmentFromText('a+b')
        assertEquals("Wrong number of fragments: $first", 2, first.fragmentLength())
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.BINARY, ASTFragmentKind.SIMPLE_EXPRESSION)
    }

    @Test
    void testASTFragment3() {
        IASTFragment first = createFragmentFromText('a.b')
        IASTFragment second = createFragmentFromText('a .    b')
        assertEquals("Wrong number of fragments: $first", 2, first.fragmentLength())
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.SIMPLE_EXPRESSION)
    }

    @Test
    void testASTFragment4() {
        IASTFragment first = createFragmentFromText('a.&b')
        IASTFragment second = createFragmentFromText('a .&    b')
        assertEquals("Wrong number of fragments: $first", 2, first.fragmentLength())
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.METHOD_POINTER, ASTFragmentKind.SIMPLE_EXPRESSION)
    }

    @Test
    void testASTFragment5() {
        IASTFragment first = createFragmentFromText('a.b(f).j')
        IASTFragment second = createFragmentFromText('a .    b(f).j')
        assertEquals("Wrong number of fragments: $first", 3, first.fragmentLength())
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.METHOD_CALL, ASTFragmentKind.SIMPLE_EXPRESSION)
    }

    @Test
    void testASTFragment6() {
        IASTFragment first = createFragmentFromText('a.j.b(f)')
        IASTFragment second = createFragmentFromText('a.j.b(f)')
        assertEquals("Wrong number of fragments: $first", 3, first.fragmentLength())
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY, ASTFragmentKind.METHOD_CALL)
    }

    @Test
    void testASTFragment7() {
        IASTFragment first = createFragmentFromText('b(f).j.a')
        IASTFragment second = createFragmentFromText('b(f).j.a')
        assertEquals("Wrong number of fragments: $first", 4, first.fragmentLength()) // implicit this
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.METHOD_CALL, ASTFragmentKind.PROPERTY, ASTFragmentKind.SIMPLE_EXPRESSION)
    }

    @Test
    void testASTFragment8() {
        IASTFragment first = createFragmentFromText('b(f)')
        IASTFragment second = createFragmentFromText('b(f)')
        // two fragments because of implicit this expression
        assertEquals("Wrong number of fragments: $first", 2, first.fragmentLength())
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.METHOD_CALL)
    }

    @Test
    void testASTFragment9() {
        IASTFragment first = createFragmentFromText('b(f).b(f).b(f).&b.b.b(f).b')
        IASTFragment second = createFragmentFromText('b(f).b(f).b(f).&b.b.b(f).b')
        // extra starting fragment because of implicit this expression
        // note also that the method pointer is replaced by the method call
        assertEquals("Wrong number of fragments: $first", 8, first.fragmentLength())
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.METHOD_CALL,
            ASTFragmentKind.METHOD_CALL, ASTFragmentKind.METHOD_CALL, ASTFragmentKind.PROPERTY,
            ASTFragmentKind.PROPERTY, ASTFragmentKind.METHOD_CALL, ASTFragmentKind.SIMPLE_EXPRESSION)
    }

    @Test
    void testASTFragment10() {
        IASTFragment first = createFragmentFromText('b(f, f(h, \'fdsafd\'), f).b(f + g).b(f).&b.b.b(f).b')
        IASTFragment second = createFragmentFromText('b(f, f(h, \'fdsafd\'), f).b(f + g).b(f).&b.b.b(f).b')
        // extra starting fragment because of implicit this expression
        assertEquals("Wrong number of fragments: $first", 8, first.fragmentLength())
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.METHOD_CALL,
            ASTFragmentKind.METHOD_CALL, ASTFragmentKind.METHOD_CALL, ASTFragmentKind.PROPERTY,
            ASTFragmentKind.PROPERTY, ASTFragmentKind.METHOD_CALL, ASTFragmentKind.SIMPLE_EXPRESSION)
    }

    @Test
    void testASTSubFragment1a() {
        IASTFragment first = createFragmentFromText('a.b.c')
        String contents = 'a.b.c.d'
        IASTFragment second = createFragmentFromText(contents, 0, contents.indexOf('.d'))
        assertEquals("Wrong number of fragments: $second", 3, second.fragmentLength())
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY, ASTFragmentKind.SIMPLE_EXPRESSION)
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY, ASTFragmentKind.SIMPLE_EXPRESSION)
    }

    @Test
    void testASTSubFragment1b() {
        IASTFragment first = createFragmentFromText('a.b.c')
        String contents = 'z.a.b.c.d'
        IASTFragment second = createFragmentFromText(contents, 2, contents.indexOf('.d'))
        assertEquals("Wrong number of fragments: $second", 3, second.fragmentLength())
        // fragments should not match because property-based fragments only
        // match from the beginning
        assertFragmentDifferent(first, second)
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY, ASTFragmentKind.SIMPLE_EXPRESSION)
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY, ASTFragmentKind.SIMPLE_EXPRESSION)
    }

    @Test
    void testASTSubFragment2a() {
        IASTFragment first = createFragmentFromText('a.b.c')
        String contents = 'a.b.c.dddda'
        IASTFragment second = createFragmentFromText(contents, 0, contents.indexOf('da'))
        assertEquals("Wrong number of fragments: $second", 3, second.fragmentLength())
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY, ASTFragmentKind.SIMPLE_EXPRESSION)
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY, ASTFragmentKind.SIMPLE_EXPRESSION)
    }

    @Test
    void testASTSubFragment2b() {
        IASTFragment first = createFragmentFromText('a.b.c')
        String contents = 'zzz.a.b.c.dddda'
        IASTFragment second = createFragmentFromText(contents, 2, contents.indexOf('da'))
        assertEquals("Wrong number of fragments: $second", 3, second.fragmentLength())
        // fragments should not match because property-based fragments only
        // match from the beginning
        assertFragmentDifferent(first, second)
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY, ASTFragmentKind.SIMPLE_EXPRESSION)
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY, ASTFragmentKind.SIMPLE_EXPRESSION)
    }

    @Test
    void testASTSubFragment3() {
        IASTFragment first = createFragmentFromText('a + b - c')
        String contents = 'z + a + b - c >> d'
        IASTFragment second = createFragmentFromText(contents, 4, contents.indexOf(' >>'))
        assertEquals("Wrong number of fragments: $second", 3, second.fragmentLength())
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.BINARY, ASTFragmentKind.BINARY, ASTFragmentKind.SIMPLE_EXPRESSION)
    }

    @Test
    void testASTSubFragment4() {
        IASTFragment first = createFragmentFromText('a + b - c')
        String contents = 'zzz + a + b - c >> d'
        IASTFragment second = createFragmentFromText(contents, 4, contents.indexOf(' >>') + 2)
        assertEquals("Wrong number of fragments: $second", 3, second.fragmentLength())
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.BINARY, ASTFragmentKind.BINARY, ASTFragmentKind.SIMPLE_EXPRESSION)
    }

    @Test
    void testASTSubFragment5() {
        IASTFragment first = createFragmentFromText('foo.bar(a + b - c)')
        String contents = 'foo.bar(a + b - c).fraz'
        IASTFragment second = createFragmentFromText(contents, 0, contents.indexOf(')'))
        assertEquals("Wrong number of fragments: $second", 2, second.fragmentLength())
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.PROPERTY, ASTFragmentKind.METHOD_CALL)
    }

    @Test
    void testASTSubFragment6() {
        IASTFragment first = createFragmentFromText('c(foo.bar(foo.bar(foo.bar(bebop, foobee)))) + a + b')
        String contents = 'a + b + c(foo.bar(foo.bar(foo.bar(bebop, foobee)))) + a + b'
        IASTFragment second = createFragmentFromText(contents, contents.indexOf('c'), contents.length())
        assertEquals("Wrong number of fragments: $second", 3, second.fragmentLength())
        assertFragmentSame(first, second)
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.BINARY, ASTFragmentKind.BINARY, ASTFragmentKind.SIMPLE_EXPRESSION)
    }

    @Test
    void testMatchSubFragment1() {
        IASTFragment fragment = createFragmentFromText('a + b - c')
        IASTFragment toMatch = createFragmentFromText('a + b')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertFragmentSame(toMatch, matched)
    }

    @Test
    void testMatchSubFragment2() {
        IASTFragment fragment = createFragmentFromText('a + b - c')
        IASTFragment toMatch = createFragmentFromText('a - b')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertIsEmptyFragment(matched)
    }

    @Test
    void testMatchSubFragment3() {
        IASTFragment fragment = createFragmentFromText('a')
        IASTFragment toMatch = createFragmentFromText('b')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertIsEmptyFragment(matched)
    }

    @Test
    void testMatchSubFragment4() {
        IASTFragment fragment = createFragmentFromText('a + b -c')
        IASTFragment toMatch = createFragmentFromText('a')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertFragmentSame(toMatch, matched)
    }

    @Test
    void testMatchSubFragment5() {
        IASTFragment fragment = createFragmentFromText('a+b')
        IASTFragment toMatch = createFragmentFromText('b')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertIsEmptyFragment(matched)
    }

    @Test
    void testMatchSubFragment6() {
        IASTFragment fragment = createFragmentFromText('a.b')
        IASTFragment toMatch = createFragmentFromText('a')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertFragmentSame(toMatch, matched)
    }

    @Test
    void testMatchSubFragment7() {
        IASTFragment fragment = createFragmentFromText('a.b')
        IASTFragment toMatch = createFragmentFromText('a.b')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertFragmentSame(toMatch, matched)
    }

    @Test
    void testMatchSubFragment8() {
        IASTFragment fragment = createFragmentFromText('a.b.&c')
        IASTFragment toMatch = createFragmentFromText('a.b')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertFragmentSame(toMatch, matched)
    }

    @Test
    void testMatchSubFragment9() {
        IASTFragment fragment = createFragmentFromText('a.b.c()')
        IASTFragment toMatch = createFragmentFromText('a.b')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertFragmentSame(toMatch, matched)
    }

    @Test
    void testMatchSubFragment10() {
        IASTFragment fragment = createFragmentFromText('a.b()')
        IASTFragment toMatch = createFragmentFromText('a.b')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertIsEmptyFragment(matched)
    }

    @Test
    void testMatchSubFragment11() {
        IASTFragment fragment = createFragmentFromText('a.b.c(foo, bar, baz)')
        IASTFragment toMatch = createFragmentFromText('a.b.c(foo, bar, baz)')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertFragmentSame(toMatch, matched)
    }

    @Test
    void testMatchSubFragment12() {
        IASTFragment fragment = createFragmentFromText('a.b(foo, bar)')
        IASTFragment toMatch = createFragmentFromText('a.b(foo)')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertIsEmptyFragment(matched)
    }

    @Test
    void testMatchSubFragment13() {
        IASTFragment fragment = createFragmentFromText('a.b.c.d')
        IASTFragment toMatch = createFragmentFromText('b.c.d')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertIsEmptyFragment(matched)
    }

    @Test
    void testMatchSubFragment14() {
        IASTFragment fragment = createFragmentFromText('a().b().c().d()')
        IASTFragment toMatch = createFragmentFromText('b().c().d()')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertIsEmptyFragment(matched)
    }

    @Test
    void testMatchSubFragment15() {
        IASTFragment fragment = createFragmentFromText('b-\n\n c*a')
        IASTFragment toMatch = createFragmentFromText('b -c')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertFragmentSame(toMatch, matched)
    }

    @Test
    void testMatchSubFragment16() {
        IASTFragment fragment = createFragmentFromText('a = b- // gadsfjakfddas\n  c * d')
        IASTFragment toMatch = createFragmentFromText('a=b - c')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertFragmentSame(toMatch, matched)
    }

    @Test
    void testMatchSubFragment17() {
        IASTFragment fragment = createFragmentFromText('a = b - c * d')
        IASTFragment toMatch = createFragmentFromText('a = b - c + d')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertIsEmptyFragment(matched)
    }

    @Test
    void testMatchSubFragment18() {
        IASTFragment fragment = createFragmentFromText('a = b - c * d / e * f')
        IASTFragment toMatch = createFragmentFromText('a = b - c * d /e')
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch)
        assertFragmentSame(toMatch, matched)
    }

    //--------------------------------------------------------------------------

    private void assertIsEmptyFragment(IASTFragment fragment) {
        assertEquals("Fragment should be empty:\n${fragment}", ASTFragmentKind.EMPTY, fragment.kind())
    }

    private void assertFragmentSame(IASTFragment first, IASTFragment second) {
        assertTrue("ASTFragments should match:\n${first}\n${second}", first.matches(second))
    }

    private void assertFragmentDifferent(IASTFragment first, IASTFragment second) {
        assertFalse("ASTFragments should not match:\n${first}\n${second}", first.matches(second))
    }

    private IASTFragment createFragmentFromText(String contents) {
        GroovyCompilationUnit unit = addGroovySource(contents, nextUnitName())
        Statement statement = unit.moduleNode.statementBlock.statements.get(0)
        IASTFragment fragment = new ASTFragmentFactory().createFragment(statement.expression)
        unit.discardWorkingCopy()
        return fragment
    }

    private IASTFragment createFragmentFromText(String contents, int start, int end) {
        GroovyCompilationUnit unit = addGroovySource(contents, nextUnitName())
        return new ASTFragmentFactory().createFragment(unit.moduleNode.statementBlock.statements.get(0).expression, start, end)
    }

    private class TestFragmentVisitor extends FragmentVisitor {

        private Stack<ASTFragmentKind> expectedKinds

        void checkExpectedKinds(IASTFragment fragment, ASTFragmentKind... expectedKindsArr) {
            this.expectedKinds = new Stack<ASTFragmentKind>()
            List<ASTFragmentKind> list = Arrays.asList(expectedKindsArr)
            Collections.reverse(list)
            this.expectedKinds.addAll(list)
            fragment.accept(this)
            assert expectedKinds.isEmpty()
        }

        @Override
        boolean visit(BinaryExpressionFragment fragment) {
            assert !this.expectedKinds.isEmpty()
            assert this.expectedKinds.pop() == fragment.kind()
            return super.visit(fragment)
        }

        @Override
        boolean visit(MethodCallFragment fragment) {
            assert !this.expectedKinds.isEmpty()
            assert this.expectedKinds.pop() == fragment.kind()
            return super.visit(fragment)
        }

        @Override
        boolean visit(PropertyExpressionFragment fragment) {
            assert !this.expectedKinds.isEmpty()
            assert this.expectedKinds.pop() == fragment.kind()
            return super.visit(fragment)
        }

        @Override
        boolean visit(SimpleExpressionASTFragment fragment) {
            assert !this.expectedKinds.isEmpty()
            assert this.expectedKinds.pop() == fragment.kind()
            return super.visit(fragment)
        }
    }
}
