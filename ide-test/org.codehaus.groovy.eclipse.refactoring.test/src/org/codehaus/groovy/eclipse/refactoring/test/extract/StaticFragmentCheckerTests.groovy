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

import groovy.transform.CompileStatic

import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.eclipse.codebrowsing.tests.CheckerTestSuite
import org.codehaus.groovy.eclipse.refactoring.core.extract.StaticExpressionChecker
import org.junit.Test

@CompileStatic
final class StaticFragmentCheckerTests extends CheckerTestSuite {

    @Test
    void testStaticExpressionChecker1() {
        checkIsStatic('666')
    }

    @Test
    void testStaticExpressionChecker2() {
        checkIsNotStatic('def x = 666\nx')
    }

    @Test
    void testStaticExpressionChecker3() {
        checkIsNotStatic('def x() {}\nthis.x()')
    }

    @Test
    void testStaticExpressionChecker4() {
        checkIsStatic('class X {\n static FOO }\n X.FOO')
    }

    @Test
    void testStaticExpressionChecker5() {
        checkIsStatic('class X {\n static FOO() {} }\n X.FOO()')
    }

    @Test
    void testStaticExpressionChecker6() {
        checkIsNotStatic('class X {\n static FOO() {} }\n def x\nX.FOO(x)')
    }

    @Test
    void testStaticExpressionChecker7() {
        checkIsStatic('class X {\n static FOO() {} \n static F }\n def x\nX.FOO(X.F)')
    }

    @Test
    void testStaticExpressionChecker8() {
        checkIsNotStatic('class X {\n static FOO() {} }\n def x = new X()\n x.FOO()')
    }

    // the expression to check is always the last expression in the module
    private void checkIsStatic(String text) {
        ModuleNode module = createModuleFromText(text)
        StaticExpressionChecker checker = new StaticExpressionChecker()
        assert checker.doVisit(getLastExpression(module)) : "Last expression in:\n$text\nshould have been static, but was not."
    }

    // the expression to check is always the last expression in the module
    private void checkIsNotStatic(String text) {
        ModuleNode module = createModuleFromText(text)
        StaticExpressionChecker checker = new StaticExpressionChecker()
        assert !checker.doVisit(getLastExpression(module)) : "Last expression in:\n$text\nshould not have been static, but was."
    }
}
