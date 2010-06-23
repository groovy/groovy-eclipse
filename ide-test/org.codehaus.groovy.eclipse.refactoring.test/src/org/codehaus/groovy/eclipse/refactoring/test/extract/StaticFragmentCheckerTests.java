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
package org.codehaus.groovy.eclipse.refactoring.test.extract;


import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codebrowsing.tests.AbstractCheckerTests;
import org.codehaus.groovy.eclipse.refactoring.core.extract.StaticExpressionChecker;
import org.eclipse.core.runtime.CoreException;

/**
 *
 * @author andrew
 * @created May 12, 2010
 */
public class StaticFragmentCheckerTests extends AbstractCheckerTests {

    public StaticFragmentCheckerTests() {
        super(StaticFragmentCheckerTests.class.getName());
    }

    public void testStaticExpressionChecker1() throws Exception {
        checkIsStatic("666");
    }

    public void testStaticExpressionChecker2() throws Exception {
        checkIsNotStatic("def x = 666\nx");
    }

    public void testStaticExpressionChecker3() throws Exception {
        checkIsNotStatic("def x() {}\nthis.x()");
    }

    public void testStaticExpressionChecker4() throws Exception {
        checkIsStatic("class X {\n static FOO }\n X.FOO");
    }

    public void testStaticExpressionChecker5() throws Exception {
        checkIsStatic("class X {\n static FOO() {} }\n X.FOO()");
    }

    public void testStaticExpressionChecker6() throws Exception {
        checkIsNotStatic("class X {\n static FOO() {} }\n def x\nX.FOO(x)");
    }

    public void testStaticExpressionChecker7() throws Exception {
        checkIsStatic("class X {\n static FOO() {} \n static F }\n def x\nX.FOO(X.F)");
    }

    public void testStaticExpressionChecker8() throws Exception {
        checkIsNotStatic("class X {\n static FOO() {} }\n def x = new X()\n x.FOO()");
    }

    // the expression to check is always the last expression in the module
    private void checkIsStatic(String text) throws CoreException {
        ModuleNode module = createModuleFromText(text);
        StaticExpressionChecker checker = new StaticExpressionChecker();
        boolean result = checker.doVisit(getLastExpression(module));
        assertTrue("Last expression in:\n" + text + "\nshould have been static, but was not.", result);
    }

    // the expression to check is always the last expression in the module
    private void checkIsNotStatic(String text) throws CoreException {
        ModuleNode module = createModuleFromText(text);
        StaticExpressionChecker checker = new StaticExpressionChecker();
        boolean result = checker.doVisit(getLastExpression(module));
        assertFalse("Last expression in:\n" + text + "\nshould not have been static, but was.", result);

    }
}
