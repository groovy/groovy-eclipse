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

import org.codehaus.groovy.eclipse.codebrowsing.selection.FindAllOccurrencesVisitor
import org.junit.Test

/**
 * Tests that the {@link FindAllOccurrencesVisitor} is working properly.
 *
 * Note that there is a limitation in how {@link FindAllOccurrencesVisitor}
 * works. This is described in the comments of the class under test.
 */
final class FindAllOccurrencesVisitorTests extends CheckerTestSuite {

    @Test
    void testFindAllOccurrences1() {
        String moduleText = 'FOO + BAR // FOO + BAR'
        String exprText = 'FOO + BAR'
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText))
    }

    @Test
    void testFindAllOccurrences2() {
        String moduleText = 'def x = FOO + BAR // FOO + BAR\n FOO + BAR'
        String exprText = 'FOO + BAR'
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText), moduleText.lastIndexOf(exprText))
    }

    @Test
    void testFindAllOccurrences3() {
        String moduleText = 'class Foo {\n def x = FOO + BAR // FOO + BAR\n }'
        String exprText = 'FOO + BAR'
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText))
    }

    @Test
    void testFindAllOccurrences4() {
        String moduleText = 'class Foo {\n def x = FOO + BAR // FOO + BAR\n def y = FOO + BAR }'
        String exprText = 'FOO + BAR'
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText), moduleText.lastIndexOf(exprText))
    }

    @Test
    void testFindAllOccurrences5() {
        String moduleText = 'class Foo {\n def x() {\n FOO + BAR // FOO + BAR \n } }'
        String exprText = 'FOO + BAR'
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText))
    }

    @Test
    void testFindAllOccurrences6() {
        String moduleText = 'class Foo {\n def x() { FOO + BAR // FOO + BAR\n}\n def y() {\n FOO + BAR \n} }'
        String exprText = 'FOO + BAR'
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText), moduleText.lastIndexOf(exprText))
    }

    @Test
    void testFindAllOccurrences7() {
        String moduleText = 'class Foo { { FOO + BAR // FOO + BAR\n}\n {\n FOO + BAR\n} }'
        String exprText = 'FOO + BAR'
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText), moduleText.lastIndexOf(exprText))
    }

    @Test
    void testFindAllOccurrences8() {
        String moduleText = 'class Foo { static FOO = 9 \n static BAR = 10 \n static { FOO + BAR // FOO + BAR \n } }'
        String exprText = 'FOO + BAR'
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText))
    }

    @Test
    void testFindAllOccurrences9() {
        String moduleText = 'class Foo { static FOO = 9 \n static BAR = 10 \n static x = FOO + BAR // FOO + BAR \n }'
        String exprText = 'FOO + BAR'
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText))
    }

    @Test
    void testFindAllOccurrences10() {
        String moduleText = 'class Foo { static FOO = 9 \n static BAR = 10 \n static x() { FOO + BAR // FOO + BAR \n } }'
        String exprText = 'FOO + BAR'
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText))
    }

    @Test
    void testFindAllOccurrences11() {
        String moduleText = 'class Foo { static FOO = 9 \n static BAR = 10 \n static x() { def x = { FOO + BAR // FOO + BAR \n } } }'
        String exprText = 'FOO + BAR'
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText))
    }

    @Test
    void testFindAllOccurrences12() {
        String moduleText = 'class Foo { static FOO = 9 \n static BAR = 10 \n def x() { schlameal ( FOO + BAR // FOO + BAR \n ) } }'
        String exprText = 'FOO + BAR'
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText))
    }

    @Test
    void testFindAllOccurrences13() {
        String moduleText = 'FOO + BAR + a + FOO + BAR + b + c + FOO + BAR'
        String exprText = 'FOO + BAR'
        int first = moduleText.indexOf(exprText)
        int second = moduleText.indexOf(exprText, first + 2)
        int third = moduleText.indexOf(exprText, second + 2)
        assertOccurrences(exprText, moduleText, first, second, third)
    }

    @Test
    void testFindAllOccurrences14() {
        String moduleText = 'FOO.BAR.FOO.BAR + FOO.BAR.FOO.BAR'
        String exprText = 'FOO.BAR'
        int first = moduleText.indexOf(exprText)
        int second = moduleText.indexOf(exprText, moduleText.indexOf('+'))
        assertOccurrences(exprText, moduleText, first, second)
    }

    @Test
    void testFindAllOccurrences15() {
        String moduleText = 'FOO.BAR.baz(FOO.BAR)'
        String exprText = 'FOO.BAR'
        int first = moduleText.indexOf(exprText)
        int second = moduleText.lastIndexOf(exprText)
        assertOccurrences(exprText, moduleText, first, second)
    }

    @Test
    void testFindAllOccurrences16() {
        String moduleText = 'FOO.BAR(FOO.BAR)'
        String exprText = 'FOO.BAR'
        int first = moduleText.lastIndexOf(exprText)
        assertOccurrences(exprText, moduleText, first)
    }

    @Test
    void testFindAllOccurrences17() {
        String moduleText = 'def BAR\nBAR(FOO.BAZ, FOO.BAZ)'
        String exprText = 'FOO.BAZ'
        int first = moduleText.indexOf(exprText)
        int second = moduleText.lastIndexOf(exprText)
        assertOccurrences(exprText, moduleText, first, second)
    }

    @Test
    void testFindAllOccurrences18() {
        String moduleText = 'FOO.BAR'
        String exprText = 'FOO'
        int first = moduleText.lastIndexOf(exprText)
        assertOccurrences(exprText, moduleText, first)
    }

    @Test
    void testFindAllOccurrences19() {
        String moduleText = 'FOO.BAR()'
        String exprText = 'FOO'
        int first = moduleText.lastIndexOf(exprText)
        assertOccurrences(exprText, moduleText, first)
    }

    @Test
    void testFindAllOccurrences20() {
        String moduleText = 'FOO.BAR()'
        String exprText = 'FOO'
        int first = moduleText.lastIndexOf(exprText)
        assertOccurrences(exprText, moduleText, first)
    }

    @Test
    void testFindAllOccurrences21() {
        String moduleText = 'FOO.BAR.baz(FOO?.BAR)'
        String exprText = 'FOO.BAR'
        int first = moduleText.indexOf(exprText)
        assertOccurrences(exprText, moduleText, first)
    }

    @Test
    void testFindAllOccurrences21a() {
        String moduleText = 'FOO?.BAR.baz(FOO.BAR)'
        String exprText = 'FOO?.BAR'
        int first = moduleText.indexOf(exprText)
        assertOccurrences(exprText, moduleText, first)
    }

    @Test
    void testFindAllOccurrences22() {
        String moduleText = 'FOO?.BAR.baz(FOO?.BAR)'
        String exprText = 'FOO?.BAR'
        int first = moduleText.indexOf(exprText)
        int second = moduleText.lastIndexOf(exprText)
        assertOccurrences(exprText, moduleText, first, second)
    }

    @Test
    void testFindAllOccurrences23() {
        String moduleText = 'FOO + BAR'
        String exprText = 'FOO'
        int first = moduleText.indexOf(exprText)
        assertOccurrences(exprText, moduleText, first)
    }

    @Test
    void testFindAllOccurrences23a() {
        String moduleText = 'FOO == BAR'
        String exprText = 'FOO'
        int first = moduleText.indexOf(exprText)
        assertOccurrences(exprText, moduleText, first)
    }

    @Test
    void testFindAllOccurrences24() {
        String moduleText = 'BAR + FOO'
        String exprText = 'FOO'
        int first = moduleText.indexOf(exprText)
        assertOccurrences(exprText, moduleText, first)
    }

    @Test
    void testFindAllOccurrences24a() {
        String moduleText = 'BAR == FOO'
        String exprText = 'FOO'
        int first = moduleText.indexOf(exprText)
        assertOccurrences(exprText, moduleText, first)
    }

    @Test
    void testFindAllOccurrences25() {
        String moduleText = 'BAR == FOO.BAR.FOO'
        String exprText = 'FOO'
        int first = moduleText.indexOf(exprText)
        assertOccurrences(exprText, moduleText, first)
    }

    @Test
    void testFindAllOccurrences25a() {
        String moduleText = 'FOO.BAR == BAR.FOO'
        String exprText = 'FOO'
        int first = moduleText.indexOf(exprText)
        assertOccurrences(exprText, moduleText, first)
    }

    private void assertOccurrences(String exprToFindText, String moduleText, Integer... startLocations) {
        def exprToFind = getLastFragment(createModuleFromText(exprToFindText))
        def foundExprs = new FindAllOccurrencesVisitor(createModuleFromText(moduleText)).findOccurrences(exprToFind)
        def createMsg = { ->
            """\
            Incorrect expressions found in:
            ${moduleText}
            Looking for:
            ${exprToFindText}
            -----
            Expecting to find expressions starting at: ${Arrays.toString(startLocations)}
            but instead found expressions starting at: [${foundExprs*.start.join(', ')}]
            """.stripIndent()
        }

        final int n = startLocations.length
        assert foundExprs.size() == n : createMsg()
        foundExprs.eachWithIndex { foundExpr, i ->
            assert foundExpr.start == startLocations[i] : createMsg()
        }
    }
}
