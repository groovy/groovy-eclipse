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

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.eclipse.codebrowsing.selection.IsSameExpression
import org.junit.Test

/**
 * Tests that the {@link IsSameExpression} is working correctly
 *
 * Note that there is a limitation in how {@link FindAllOccurrencesVisitor}
 * works. This is described in the comments of the class under test.
 */
final class IsSameExpressionTests extends CheckerTestSuite {

    @Test
    void testIsSame1() {
        checkTwoExpressions('foo(666, FOO)', 'foo(666, FOO)', true)
    }

    @Test
    void testIsSame2() {
        checkTwoExpressions('"FOO"', '"FOO "', false)
    }

    @Test
    void testIsSame3() {
        checkTwoExpressions('parent*.action', 'parent   *.   //  \n\n /**/ action', true)
    }

    @Test
    void testIsSame4() {
        checkTwoExpressions('[\'cat\', \'elephant\']*.size() == [3, 8]', '[\'cat\', \'elephant\']*.size() == [3, 8]', true)
    }

    @Test
    void testIsSame5() {
        checkTwoExpressions('[\'cat\', \'elephant\']*.size() == [3, 8]', '[\'cat\', \'elephant\']*.size() != [3, 8]', false)
    }

    @Test
    void testIsSame6() {
        checkTwoExpressions('println x.@field', 'println x.@   field', true)
    }

    @Test
    void testIsSame7() {
        checkTwoExpressions('user.male ? "male" : "female"', 'user.male ? \'male\' : \'female\'', true)
    }

    @Test
    void testIsSame8() {
        checkTwoExpressions('user.name ?: "Anonymous"', 'user.name ?: \'Anonymous\'', true)
    }

    @Test
    void testIsSame9() {
        checkTwoExpressions('"$foo   "', '"$foo   "', true)
    }

    @Test
    void testIsSame10() {
        checkTwoExpressions('"$foo   "', '"$foo"', false)
    }

    private void checkTwoExpressions(String first, String second, boolean isSame) {
        Expression firstExpr = getLastExpression(createModuleFromText(first))
        Expression secondExpr = getLastExpression(createModuleFromText(second))
        assert new IsSameExpression().isSame(firstExpr, secondExpr) == isSame : createMsg(first, second, isSame)
    }

    private String createMsg(String first, String second, boolean isSame) {
        String end = "\nFirst expression:\n${first}\n\nSecond expression:\n${second}"
        return (isSame ? "Expressions should have been the same" : "Expressions should not have been the same") + end
    }
}
