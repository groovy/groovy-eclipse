/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.debug

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.eclipse.debug.ui.ValidBreakpointLocationFinder
import org.eclipse.jface.text.Document
import org.junit.Test

/**
 * Tests that breakpoint locations are as expected.
 */
final class BreakpointLocationTests extends BreakpointTestSuite {

    private void doBreakpointTest(int i) {
        def document = new Document(String.valueOf(unit.contents))
        int location = document.get().indexOf('// ' + i) - 3
        int line = document.getLineOfOffset(location) + 1
        ValidBreakpointLocationFinder finder = new ValidBreakpointLocationFinder(line)
        ASTNode node = finder.findValidBreakpointLocation(unit.moduleNode)
        assert node != null : 'Could not find a breakpoint for line ' + line
        assert node.getLineNumber() == line : 'Wrong expected line number'
    }

    @Test
    void testBreakpointInScript1() {
        doBreakpointTest(1)
    }

    @Test
    void testBreakpointInScript2() {
        doBreakpointTest(2)
    }

    @Test
    void testBreakpointInScript3() {
        doBreakpointTest(3)
    }

    @Test
    void testBreakpointInScript4() {
        doBreakpointTest(4)
    }

    @Test
    void testBreakpointInScript5() {
        doBreakpointTest(5)
    }

    @Test
    void testBreakpointInScript6() {
        doBreakpointTest(6)
    }

    @Test
    void testBreakpointInScript7() {
        doBreakpointTest(7)
    }

    @Test
    void testBreakpointInScript8() {
        doBreakpointTest(8)
    }

    @Test
    void testBreakpointInScript9() {
        doBreakpointTest(9)
    }

    @Test
    void testBreakpointInScript10() {
        doBreakpointTest(10)
    }

    @Test
    void testBreakpointInScript11() {
        doBreakpointTest(11)
    }

    @Test
    void testBreakpointInScript12() {
        doBreakpointTest(12)
    }

    @Test
    void testBreakpointInScript13() {
        doBreakpointTest(13)
    }

    @Test
    void testBreakpointInScript14() {
        doBreakpointTest(14)
    }

    @Test
    void testBreakpointInScript15() {
        doBreakpointTest(15)
    }

    @Test
    void testBreakpointInScript16() {
        doBreakpointTest(16)
    }

    @Test
    void testBreakpointInScript17() {
        doBreakpointTest(17)
    }

    @Test
    void testBreakpointInScript18() {
        doBreakpointTest(18)
    }

    @Test
    void testBreakpointInScript19() {
        doBreakpointTest(19)
    }

    @Test
    void testBreakpointInScript20() {
        doBreakpointTest(20)
    }

    @Test
    void testBreakpointInScript21() {
        doBreakpointTest(21)
    }

    @Test
    void testBreakpointInScript22() {
        doBreakpointTest(22)
    }

    @Test
    void testBreakpointInScript23() {
        doBreakpointTest(23)
    }
}
