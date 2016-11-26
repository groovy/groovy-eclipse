/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.core.util;

import junit.framework.TestCase;
import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.impl.StringSourceBuffer;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder;

/**
 * @author Heiko Boettger
 */
public class ExpressionFinderTestCase extends TestCase {

    @Override
    protected void setUp() throws Exception {
        System.out.println("----------------------------------------");
        System.out.println("Starting: " + getName());
    }

    public void testIdentifierExpression() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { bbbb. } }";
        String completionLocation = "class Test { public testFunction() { bbbb.";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("bbbb.", expression);
    }

    public void testParenExpression() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { (bbbb). } }";
        String completionLocation = "class Test { public testFunction() { (bbbb).";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("(bbbb).", expression);
    }

    public void testArrayElementExpressionInParen() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { (bbbb[10]). } }";
        String completionLocation = "class Test { public testFunction() { (bbbb[10]).";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("(bbbb[10]).", expression);
    }

    public void testArrayElementExpression() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { bbbb[10]. } }";
        String completionLocation = "class Test { public testFunction() { bbbb[10].";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("bbbb[10].", expression);
    }

    public void testParenInParenExpression() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { (([10])). } }";
        String completionLocation = "class Test { public testFunction() { (([10])).";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("(([10])).", expression);
    }

    public void testParenInParenExpression2() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { (([10\n\n  ])). } }";
        String completionLocation = "class Test { public testFunction() { (([10\n\n  ])).";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("(([10\n\n  ])).", expression);
    }

    public void testStringExpression() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { \"13\". } }";
        String completionLocation = "class Test { public testFunction() { \"13\".";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("\"13\".", expression);
    }

    public void testStringExpressionAfterOtherExpressionAndNewLine() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { int a = 10\n   \"13\". } }";
        String completionLocation = "class Test { public testFunction() { int a = 10\n   \"13\".";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("\"13\".", expression);
    }

    public void testStringExpressionAfterOtherExpressionAndSemi() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { int a = 10;   \"13\". } }";
        String completionLocation = "class Test { public testFunction() { int a = 10;   \"13\".";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("\"13\".", expression);
    }

    public void testExpressionInGString() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { \"anyString${a.}\" } }";
        String completionLocation = "class Test { public testFunction() { \"anyString${a.";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("a.", expression);
    }

    public void testExpressionInParenAfterNewLine() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { int a = 100\n    (new ArrayList()). } }";
        String completionLocation = "class Test { public testFunction() { int a = 100\n    (new ArrayList()).";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("(new ArrayList()).", expression);
    }

    public void testExpressionAfterNewLine() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { int a = 100\n    new ArrayList(). } }";
        String completionLocation = "class Test { public testFunction() { int a = 100\n    new ArrayList().";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("new ArrayList().", expression);
    }

    public void _testNoExpression1() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() {    } }";
        String completionLocation = "class Test { public testFunction() { ";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals(null, expression);
    }

    public void testNoExpression2() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { int a = 100    } }";
        String completionLocation = "class Test { public testFunction() { int a = 100  ";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("", expression);
    }

    public void testNoExpression3() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { int a = 100\n    } }";
        String completionLocation = "class Test { public testFunction() { int a = 100\n  ";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("", expression);
    }

    public void testExpressionAfterNumber() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { int a = 100\n    } }";
        String completionLocation = "class Test { public testFunction() { int a = 100";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("100", expression);
    }

    public void testExpressionAfterStringLiteral() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { def a = \"fff\"\n    } }";
        String completionLocation = "class Test { public testFunction() { int a = \"fff\"";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals(null, expression);
    }

    public void testExpressionInParenAfterSemi() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { int a = 100;    (new ArrayList()). } }";
        String completionLocation = "class Test { public testFunction() { int a = 100;    (new ArrayList()).";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("(new ArrayList()).", expression);
    }

    public void testExpressionForNewGenericType() throws Exception {
        ExpressionFinder finder = new ExpressionFinder();
        String source = "class Test { public testFunction() { int a = 100;    (new ArrayList<String>()). } }";
        String completionLocation = "class Test { public testFunction() { int a = 100;    (new ArrayList<String>()).";
        ISourceBuffer sourceBuffer = new StringSourceBuffer(source);

        String expression = finder.findForCompletions(sourceBuffer, completionLocation.length() - 1);
        assertEquals("(new ArrayList<String>()).", expression);
    }
}
