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

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindAllOccurrencesVisitor;
import org.codehaus.groovy.eclipse.codebrowsing.selection.IsSameExpression;
import org.eclipse.core.runtime.CoreException;

/**
 * Tests that the {@link IsSameExpression} is working correctly
 *
 * Note that there is a limitation in how {@link FindAllOccurrencesVisitor}
 * works. This is described in the comments of the class under test.
 *
 * @author andrew
 * @created May 12, 2010
 */
public class IsSameExpressionTests extends AbstractCheckerTests {

    public IsSameExpressionTests() {
        super(IsSameExpressionTests.class.getName());
    }

    public void testIsSame1() throws Exception {
        checkTwoExpressions("foo(666, FOO)", "foo(666, FOO)", true);
    }

    public void testIsSame2() throws Exception {
        checkTwoExpressions("\"FOO\"", "\"FOO \"", false);
    }

    public void testIsSame3() throws Exception {
        checkTwoExpressions("parent*.action", "parent   *.   //  \n\n /**/ action", true);
    }

    public void testIsSame4() throws Exception {
        checkTwoExpressions("['cat', 'elephant']*.size() == [3, 8]", "['cat', 'elephant']*.size() == [3, 8]", true);
    }

    public void testIsSame5() throws Exception {
        checkTwoExpressions("['cat', 'elephant']*.size() == [3, 8]", "['cat', 'elephant']*.size() != [3, 8]", false);
    }

    public void testIsSame6() throws Exception {
        checkTwoExpressions("println x.@field", "println x.@   field", true);
    }

    public void testIsSame7() throws Exception {
        checkTwoExpressions("user.male ? \"male\" : \"female\"", "user.male ? 'male' : 'female'", true);
    }

    public void testIsSame8() throws Exception {
        checkTwoExpressions("user.name ?: \"Anonymous\"", "user.name ?: 'Anonymous'", true);
    }

    public void testIsSame9() throws Exception {
        checkTwoExpressions("\"$foo   \"", "\"$foo   \"", true);
    }

    public void testIsSame10() throws Exception {
        checkTwoExpressions("\"$foo   \"", "\"$foo\"", false);
    }

    private void checkTwoExpressions(String first, String second, boolean isSame) throws CoreException {
        Expression firstExpr = getLastExpression(createModuleFromText(first));
        Expression secondExpr = getLastExpression(createModuleFromText(second));
        assertEquals(createMsg(first, second, isSame), isSame, new IsSameExpression().isSame(firstExpr, secondExpr));
    }

    /**
     * @param first
     * @param second
     * @param isSame
     * @return
     */
    private String createMsg(String first, String second, boolean isSame) {
        String end = "\nFirst expression:\n" + first + "\n\nSecond expression:\n" + second;
        return (isSame ? "Expressions should have been the same" : "Expressions should not have been the same") + end;
    }

}
