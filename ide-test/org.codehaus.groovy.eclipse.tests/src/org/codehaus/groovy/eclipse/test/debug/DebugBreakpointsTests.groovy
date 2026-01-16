/*
 * Copyright 2009-2026 the original author or authors.
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

import org.codehaus.groovy.eclipse.GroovyPlugin
import org.codehaus.groovy.eclipse.debug.ui.ToggleBreakpointAdapter
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.groovy.eclipse.test.SynchronizationUtils
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.IBreakpointManager
import org.eclipse.debug.core.model.IBreakpoint
import org.eclipse.jdt.internal.debug.ui.actions.ActionDelegateHelper
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.ITextSelection
import org.eclipse.jface.text.TextSelection
import org.eclipse.ui.texteditor.ITextEditor
import org.junit.After
import org.junit.Before
import org.junit.Test

final class DebugBreakpointsTests extends GroovyEclipseTestSuite {

    private ITextEditor editor, former

    @Before
    void setUp() {
        editor = openInEditor(addGroovySource('''\
            |
            |def t = [ x:1, y:2 ] // 1
            |
            |def shiftTriangle = { it ->
            |    it.x += 1 // 2
            |    it.y += 1 // 3
            |
            |    it.getX()
            |}
            |
            |t.getX() // 4
            |
            |println "Triangle is at $t.centerLocation"
            |shiftTriangle(t)
            |println "Triangle is at $t.centerLocation"
            |
            |println "Triangle is at $t.centerLocation"
            |
            |t = ""
            |
            |def x() { // 12
            |    print "Hi"  // 5
            |}
            |
            |def xx() {
            |    print "Hi"  // 16
            |}
            |
            |def p = { g -> print g } // 13
            |
            |t = [ x: 1,
            |      y: 2, // 6
            |      z:4 ] // 7
            |t = [ 1, // 8
            |      2, // 9
            |      3] // 10
            |t = []; // 11
            |
            |
            |class Class {
            |    def m() {  // 22
            |        here()
            |        here() // 14
            |        here()
            |        here()
            |    }
            |
            |    def t = { here() } // 15
            |
            |    static h = {
            |        here() // 17
            |    }
            |}
            |
            |public class Printing {
            |    Printing() {  // 21
            |        print 8  // 18
            |    }
            |
            |    static y() {  // 23
            |    }
            |    def x = {
            |        print 9  // 19
            |    }
            |    static z = {
            |        print 9  // 20
            |    }
            |}'''.stripMargin(), nextUnitName()))
        former = ActionDelegateHelper.default.textEditor
        ActionDelegateHelper.default.setTextEditor(editor)
    }

    @After
    void tearDown() {
        ActionDelegateHelper.default.setTextEditor(former)
        GroovyPlugin.default.activeWorkbenchWindow.activePage.closeAllEditors(false)
    }

    private void doBreakpointTest(int i) {
        ToggleBreakpointAdapter adapter = new ToggleBreakpointAdapter()

        Document document = editor.documentProvider.getDocument(editor.editorInput)
        ITextSelection selection = new TextSelection(document, document.get().indexOf('// ' + i) - 3, 3)

        boolean canToggle = adapter.canToggleLineBreakpoints(editor, selection)
        assert canToggle : 'Should be able to toggle breakpoint at section ' + i

        IBreakpointManager breakpointManager = DebugPlugin.default.breakpointManager
        IBreakpoint[] breakpoints = breakpointManager.breakpoints
        int initialNumBreakpoints = breakpoints.length
        try {
            adapter.toggleLineBreakpoints(editor, selection)
            SynchronizationUtils.joinBackgroundActivities()
        } finally {
            IBreakpoint[] newBreakpoints = breakpointManager.breakpoints
            assert newBreakpoints.length == initialNumBreakpoints + 1 : 'Unexpected number of breakpoints'
            for (breakpoint in newBreakpoints) {
                breakpointManager.removeBreakpoint(breakpoint, true)
            }
            assert breakpointManager.breakpoints.length == 0 : 'Should have deleted all breakpoints'
        }
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
