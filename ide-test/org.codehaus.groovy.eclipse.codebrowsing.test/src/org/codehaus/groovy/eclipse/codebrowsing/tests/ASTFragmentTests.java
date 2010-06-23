/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.codebrowsing.tests;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentFactory;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentKind;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.BinaryExpressionFragment;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.FragmentVisitor;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.MethodCallFragment;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.PropertyExpressionFragment;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.SimpleExpressionASTFragment;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;

/**
 * Tests to see that ASTFragments are created correctly
 *
 * @author andrew
 * @created Jun 4, 2010
 */
public class ASTFragmentTests extends BrowsingTestCase {

    private class TestFragmentVisitor extends FragmentVisitor {
        private Stack<ASTFragmentKind> expectedKinds;

        void checkExpectedKinds(IASTFragment fragment, ASTFragmentKind... expectedKindsArr) {
            this.expectedKinds = new Stack<ASTFragmentKind>();
            List<ASTFragmentKind> list = Arrays.asList(expectedKindsArr);
            Collections.reverse(list);
            this.expectedKinds.addAll(list);
            fragment.accept(this);
            if (!expectedKinds.isEmpty()) {
                fail();
            }
        }

        @Override
        public boolean visit(BinaryExpressionFragment fragment) {
            if (this.expectedKinds.isEmpty()) {
                fail();
            }
            if (this.expectedKinds.pop() != fragment.kind()) {
                fail();
            }
            return super.visit(fragment);
        }

        @Override
        public boolean visit(MethodCallFragment fragment) {
            if (this.expectedKinds.isEmpty()) {
                fail();
            }
            if (this.expectedKinds.pop() != fragment.kind()) {
                fail();
            }
            return super.visit(fragment);
        }

        @Override
        public boolean visit(PropertyExpressionFragment fragment) {
            if (this.expectedKinds.isEmpty()) {
                fail();
            }
            if (this.expectedKinds.pop() != fragment.kind()) {
                fail();
            }
            return super.visit(fragment);
        }

        @Override
        public boolean visit(SimpleExpressionASTFragment fragment) {
            if (this.expectedKinds.isEmpty()) {
                fail();
            }
            if (this.expectedKinds.pop() != fragment.kind()) {
                fail();
            }
            return super.visit(fragment);
        }

    }

    public ASTFragmentTests() {
        super(ASTFragmentTests.class.getName());
    }

    public void testASTFragment1() throws Exception {
        IASTFragment first = createFragmentFromText("a");
        IASTFragment second = createFragmentFromText("a");
        assertEquals("Wrong number of fragments: " + first, 1, first.fragmentLength());
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.SIMPLE_EXPRESSION);
    }

    public void testASTFragment2() throws Exception {
        IASTFragment first = createFragmentFromText("a + b");
        IASTFragment second = createFragmentFromText("a+b");
        assertEquals("Wrong number of fragments: " + first, 2, first.fragmentLength());
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.BINARY, ASTFragmentKind.SIMPLE_EXPRESSION);
    }

    public void testASTFragment3() throws Exception {
        IASTFragment first = createFragmentFromText("a.b");
        IASTFragment second = createFragmentFromText("a .    b");
        assertEquals("Wrong number of fragments: " + first, 2, first.fragmentLength());
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.SIMPLE_EXPRESSION);
    }

    public void testASTFragment4() throws Exception {
        IASTFragment first = createFragmentFromText("a.&b");
        IASTFragment second = createFragmentFromText("a .&    b");
        assertEquals("Wrong number of fragments: " + first, 2, first.fragmentLength());
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.METHOD_POINTER, ASTFragmentKind.SIMPLE_EXPRESSION);
    }

    public void testASTFragment5() throws Exception {
        IASTFragment first = createFragmentFromText("a.b(f).j");
        IASTFragment second = createFragmentFromText("a .    b(f).j");
        assertEquals("Wrong number of fragments: " + first, 3, first.fragmentLength());
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.METHOD_CALL,
                ASTFragmentKind.SIMPLE_EXPRESSION);
    }

    public void testASTFragment6() throws Exception {
        IASTFragment first = createFragmentFromText("a.j.b(f)");
        IASTFragment second = createFragmentFromText("a.j.b(f)");
        assertEquals("Wrong number of fragments: " + first, 3, first.fragmentLength());
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY,
                ASTFragmentKind.METHOD_CALL);
    }

    public void testASTFragment7() throws Exception {
        IASTFragment first = createFragmentFromText("b(f).j.a");
        IASTFragment second = createFragmentFromText("b(f).j.a");
        assertEquals("Wrong number of fragments: " + first, 4, first.fragmentLength()); // implicit
                                                                                        // this
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.METHOD_CALL,
                ASTFragmentKind.PROPERTY,
                ASTFragmentKind.SIMPLE_EXPRESSION);
    }

    public void testASTFragment8() throws Exception {
        IASTFragment first = createFragmentFromText("b(f)");
        IASTFragment second = createFragmentFromText("b(f)");
        // two fragments because of implicit this expression
        assertEquals("Wrong number of fragments: " + first, 2, first.fragmentLength());
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.METHOD_CALL);
    }

    public void testASTFragment9() throws Exception {
        IASTFragment first = createFragmentFromText("b(f).b(f).b(f).&b.b.b(f).b");
        IASTFragment second = createFragmentFromText("b(f).b(f).b(f).&b.b.b(f).b");
        // extra starting fragment because of implicit this expression
        // note also that the method pointer is replaced by the method call
        assertEquals("Wrong number of fragments: " + first, 8, first.fragmentLength());
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.METHOD_CALL,
                ASTFragmentKind.METHOD_CALL, ASTFragmentKind.METHOD_CALL, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY,
                ASTFragmentKind.METHOD_CALL, ASTFragmentKind.SIMPLE_EXPRESSION);
    }

    public void testASTFragment10() throws Exception {
        IASTFragment first = createFragmentFromText("b(f, f(h, 'fdsafd'), f).b(f + g).b(f).&b.b.b(f).b");
        IASTFragment second = createFragmentFromText("b(f, f(h, 'fdsafd'), f).b(f + g).b(f).&b.b.b(f).b");
        // extra starting fragment because of implicit this expression
        assertEquals("Wrong number of fragments: " + first, 8, first.fragmentLength());
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.METHOD_CALL,
                ASTFragmentKind.METHOD_CALL, ASTFragmentKind.METHOD_CALL, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY,
                ASTFragmentKind.METHOD_CALL, ASTFragmentKind.SIMPLE_EXPRESSION);
    }

    public void testASTSubFragment1a() throws Exception {
        IASTFragment first = createFragmentFromText("a.b.c");
        String contents = "a.b.c.d";
        IASTFragment second = createFragmentFromText(contents, 0, contents.indexOf(".d"));
		assertEquals("Wrong number of fragments: " + second, 3, second.fragmentLength());
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY,
                ASTFragmentKind.SIMPLE_EXPRESSION);
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY,
                ASTFragmentKind.SIMPLE_EXPRESSION);
    }

    public void testASTSubFragment1b() throws Exception {
        IASTFragment first = createFragmentFromText("a.b.c");
        String contents = "z.a.b.c.d";
        IASTFragment second = createFragmentFromText(contents, 2, contents.indexOf(".d"));
		assertEquals("Wrong number of fragments: " + second, 3, second.fragmentLength());
        // fragments should not match because property-based fragments only
        // match from the beginning
        assertFragmentDifferent(first, second);
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY,
                ASTFragmentKind.SIMPLE_EXPRESSION);
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY,
                ASTFragmentKind.SIMPLE_EXPRESSION);
    }

    public void testASTSubFragment2a() throws Exception {
        IASTFragment first = createFragmentFromText("a.b.c");
        String contents = "a.b.c.dddda";
        IASTFragment second = createFragmentFromText(contents, 0, contents.indexOf("da"));
		assertEquals("Wrong number of fragments: " + second, 3, second.fragmentLength());
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY,
                ASTFragmentKind.SIMPLE_EXPRESSION);
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY,
                ASTFragmentKind.SIMPLE_EXPRESSION);
    }

    public void testASTSubFragment2b() throws Exception {
        IASTFragment first = createFragmentFromText("a.b.c");
        String contents = "zzz.a.b.c.dddda";
        IASTFragment second = createFragmentFromText(contents, 2, contents.indexOf("da"));
		assertEquals("Wrong number of fragments: " + second, 3, second.fragmentLength());
        // fragments should not match because property-based fragments only
        // match from the beginning
        assertFragmentDifferent(first, second);
        new TestFragmentVisitor().checkExpectedKinds(first, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY,
                ASTFragmentKind.SIMPLE_EXPRESSION);
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.PROPERTY, ASTFragmentKind.PROPERTY,
                ASTFragmentKind.SIMPLE_EXPRESSION);
    }

    public void testASTSubFragment3() throws Exception {
        IASTFragment first = createFragmentFromText("a + b - c");
        String contents = "z + a + b - c >> d";
        IASTFragment second = createFragmentFromText(contents, 4, contents.indexOf(" >>"));
		assertEquals("Wrong number of fragments: " + second, 3, second.fragmentLength());
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.BINARY, ASTFragmentKind.BINARY,
                ASTFragmentKind.SIMPLE_EXPRESSION);
    }

    public void testASTSubFragment4() throws Exception {
        IASTFragment first = createFragmentFromText("a + b - c");
        String contents = "zzz + a + b - c >> d";
        IASTFragment second = createFragmentFromText(contents, 4, contents.indexOf(" >>") + 2);
		assertEquals("Wrong number of fragments: " + second, 3, second.fragmentLength());
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.BINARY, ASTFragmentKind.BINARY,
                ASTFragmentKind.SIMPLE_EXPRESSION);
    }

    public void testASTSubFragment5() throws Exception {
        IASTFragment first = createFragmentFromText("foo.bar(a + b - c)");
        String contents = "foo.bar(a + b - c).fraz";
        IASTFragment second = createFragmentFromText(contents, 0, contents.indexOf(")"));
		assertEquals("Wrong number of fragments: " + second, 2, second.fragmentLength());
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.PROPERTY, ASTFragmentKind.METHOD_CALL);
    }

    public void testASTSubFragment6() throws Exception {
        IASTFragment first = createFragmentFromText("c(foo.bar(foo.bar(foo.bar(bebop, foobee)))) + a + b");
        String contents = "a + b + c(foo.bar(foo.bar(foo.bar(bebop, foobee)))) + a + b";
        IASTFragment second = createFragmentFromText(contents, contents.indexOf("c"), contents.length());
		assertEquals("Wrong number of fragments: " + second, 3, second.fragmentLength());
        assertFragmentSame(first, second);
        new TestFragmentVisitor().checkExpectedKinds(second, ASTFragmentKind.BINARY, ASTFragmentKind.BINARY,
                ASTFragmentKind.SIMPLE_EXPRESSION);
    }

    public void testMatchSubFragment1() throws Exception {
        IASTFragment fragment = createFragmentFromText("a + b - c");
        IASTFragment toMatch = createFragmentFromText("a + b");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertFragmentSame(toMatch, matched);
    }

    public void testMatchSubFragment2() throws Exception {
        IASTFragment fragment = createFragmentFromText("a + b - c");
        IASTFragment toMatch = createFragmentFromText("a - b");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertIsEmptyFragment(matched);
    }

    public void testMatchSubFragment3() throws Exception {
        IASTFragment fragment = createFragmentFromText("a");
        IASTFragment toMatch = createFragmentFromText("b");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertIsEmptyFragment(matched);
    }

    public void testMatchSubFragment4() throws Exception {
        IASTFragment fragment = createFragmentFromText("a + b -c");
        IASTFragment toMatch = createFragmentFromText("a");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertFragmentSame(toMatch, matched);
    }

    public void testMatchSubFragment5() throws Exception {
        IASTFragment fragment = createFragmentFromText("a+b");
        IASTFragment toMatch = createFragmentFromText("b");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertIsEmptyFragment(matched);
    }

    public void testMatchSubFragment6() throws Exception {
        IASTFragment fragment = createFragmentFromText("a.b");
        IASTFragment toMatch = createFragmentFromText("a");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertFragmentSame(toMatch, matched);
    }

    public void testMatchSubFragment7() throws Exception {
        IASTFragment fragment = createFragmentFromText("a.b");
        IASTFragment toMatch = createFragmentFromText("a.b");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertFragmentSame(toMatch, matched);
    }

    public void testMatchSubFragment8() throws Exception {
        IASTFragment fragment = createFragmentFromText("a.b.&c");
        IASTFragment toMatch = createFragmentFromText("a.b");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertFragmentSame(toMatch, matched);
    }

    public void testMatchSubFragment9() throws Exception {
        IASTFragment fragment = createFragmentFromText("a.b.c()");
        IASTFragment toMatch = createFragmentFromText("a.b");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertFragmentSame(toMatch, matched);
    }

    public void testMatchSubFragment10() throws Exception {
        IASTFragment fragment = createFragmentFromText("a.b()");
        IASTFragment toMatch = createFragmentFromText("a.b");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertIsEmptyFragment(matched);
    }

    public void testMatchSubFragment11() throws Exception {
        IASTFragment fragment = createFragmentFromText("a.b.c(foo, bar, baz)");
        IASTFragment toMatch = createFragmentFromText("a.b.c(foo, bar, baz)");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertFragmentSame(toMatch, matched);
    }

    public void testMatchSubFragment12() throws Exception {
        IASTFragment fragment = createFragmentFromText("a.b(foo, bar)");
        IASTFragment toMatch = createFragmentFromText("a.b(foo)");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertIsEmptyFragment(matched);
    }

    public void testMatchSubFragment13() throws Exception {
        IASTFragment fragment = createFragmentFromText("a.b.c.d");
        IASTFragment toMatch = createFragmentFromText("b.c.d");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertIsEmptyFragment(matched);
    }

    public void testMatchSubFragment14() throws Exception {
        IASTFragment fragment = createFragmentFromText("a().b().c().d()");
        IASTFragment toMatch = createFragmentFromText("b().c().d()");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertIsEmptyFragment(matched);
    }

    public void testMatchSubFragment15() throws Exception {
        IASTFragment fragment = createFragmentFromText("b-\n\n c*a");
        IASTFragment toMatch = createFragmentFromText("b -c");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertFragmentSame(toMatch, matched);
    }

    public void testMatchSubFragment16() throws Exception {
        IASTFragment fragment = createFragmentFromText("a = b- // gadsfjakfddas\n  c * d");
        IASTFragment toMatch = createFragmentFromText("a=b - c");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertFragmentSame(toMatch, matched);
    }

    public void testMatchSubFragment17() throws Exception {
        IASTFragment fragment = createFragmentFromText("a = b - c * d");
        IASTFragment toMatch = createFragmentFromText("a = b - c + d");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertIsEmptyFragment(matched);
    }

    public void testMatchSubFragment18() throws Exception {
        IASTFragment fragment = createFragmentFromText("a = b - c * d / e * f");
        IASTFragment toMatch = createFragmentFromText("a = b - c * d /e");
        IASTFragment matched = fragment.findMatchingSubFragment(toMatch);
        assertFragmentSame(toMatch, matched);
    }

    private void assertIsEmptyFragment(IASTFragment fragment) {
        assertEquals("Fragment should be empty:\n" + fragment, ASTFragmentKind.EMPTY, fragment.kind());
    }

    private void assertFragmentSame(IASTFragment first, IASTFragment second) {
        if (!first.matches(second)) {
            fail("ASTFragments should match:\n" + first + "\n" + second);
        }
    }

    private void assertFragmentDifferent(IASTFragment first, IASTFragment second) {
        if (first.matches(second)) {
            fail("ASTFragments should not match:\n" + first + "\n" + second);
        }
    }

    private IASTFragment createFragmentFromText(String contents) throws Exception {
        GroovyCompilationUnit unit = getCompilationUnitFor(contents);
        Statement statement = (Statement) unit.getModuleNode().getStatementBlock().getStatements()
                .get(0);
        Expression expr = statement instanceof ReturnStatement ? ((ReturnStatement) statement).getExpression()
                : ((ExpressionStatement) statement).getExpression();
        return new ASTFragmentFactory().createFragment(expr);
    }

    private IASTFragment createFragmentFromText(String contents, int start, int end) throws Exception {
        GroovyCompilationUnit unit = getCompilationUnitFor(contents);
        return new ASTFragmentFactory().createFragment(((ReturnStatement) unit.getModuleNode().getStatementBlock().getStatements()
                .get(0)).getExpression(), start, end);
    }
}
