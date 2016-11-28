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
package org.codehaus.groovy.eclipse.test.debug

import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.eclipse.debug.ui.ToggleBreakpointAdapter
import org.codehaus.groovy.eclipse.editor.GroovyEditor
import org.codehaus.groovy.eclipse.test.EclipseTestSetup
import org.codehaus.groovy.eclipse.test.SynchronizationUtils
import org.eclipse.core.runtime.Platform
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.IBreakpointManager
import org.eclipse.debug.core.model.IBreakpoint
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.internal.debug.ui.actions.ActionDelegateHelper
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.ITextSelection
import org.eclipse.jface.text.TextSelection
import org.eclipse.ui.texteditor.ITextEditor

/**
 * @author Andrew Eisenberg
 * @created Jul 24, 2009
 */
final class DebugBreakpointsTests extends TestCase {

    static Test suite() {
        new EclipseTestSetup(new TestSuite(DebugBreakpointsTests))
    }

    @Override
    protected void tearDown() {
        ActionDelegateHelper.default.textEditor = former
        EclipseTestSetup.removeSources()
    }

    @Override
    protected void setUp() {
        URL url = Platform.getBundle('org.codehaus.groovy.eclipse.tests').getEntry('/testData/groovyfiles/BreakpointTesting.groovy')
        url.openStream().withStream {
            unit = EclipseTestSetup.addGroovySource((text = IOUtils.toString(it)), 'BreakpointTesting')
        }
        adapter = new ToggleBreakpointAdapter()
        editor = EclipseTestSetup.openInEditor(unit)
        former = ActionDelegateHelper.default.textEditor
        ActionDelegateHelper.default.textEditor = editor
    }

    private ToggleBreakpointAdapter adapter
    private ICompilationUnit unit
    private GroovyEditor editor
    private ITextEditor former
    private String text

    void testBreakpointInScript1() {
        doBreakpointTest(1)
    }

    void testBreakpointInScript2() {
        doBreakpointTest(2)
    }

    void testBreakpointInScript3() {
        doBreakpointTest(3)
    }

    void testBreakpointInScript4() {
        doBreakpointTest(4)
    }

    void testBreakpointInScript5() {
        doBreakpointTest(5)
    }

    void testBreakpointInScript6() {
        doBreakpointTest(6)
    }

    void testBreakpointInScript7() {
        doBreakpointTest(7)
    }

    void testBreakpointInScript8() {
        doBreakpointTest(8)
    }

    void testBreakpointInScript9() {
        doBreakpointTest(9)
    }

    void testBreakpointInScript10() {
        doBreakpointTest(10)
    }

    void testBreakpointInScript11() {
        doBreakpointTest(11)
    }

    void testBreakpointInScript12() {
        doBreakpointTest(12)
    }

    void testBreakpointInScript13() {
        doBreakpointTest(13)
    }

    void testBreakpointInScript14() {
        doBreakpointTest(14)
    }

    void testBreakpointInScript15() {
        doBreakpointTest(15)
    }

    void testBreakpointInScript16() {
        doBreakpointTest(16)
    }

    void testBreakpointInScript17() {
        doBreakpointTest(17)
    }

    void testBreakpointInScript18() {
        doBreakpointTest(18)
    }

    void testBreakpointInScript19() {
        doBreakpointTest(19)
    }

    void testBreakpointInScript20() {
        doBreakpointTest(20)
    }

    void testBreakpointInScript21() {
        doBreakpointTest(21)
    }

    void testBreakpointInScript22() {
        doBreakpointTest(22)
    }

    void testBreakpointInScript23() {
        doBreakpointTest(23)
    }

    private void doBreakpointTest(int i) {
        ITextSelection selection = new TextSelection(new Document(text), text.indexOf('// ' + i) - 3, 3)
        boolean canToggle = adapter.canToggleLineBreakpoints(editor, selection)
        assertTrue('Should be able to toggle breakpoint at section ' + i, canToggle)

        IBreakpointManager breakpointManager = DebugPlugin.default.breakpointManager
        IBreakpoint[] breakpoints = breakpointManager.breakpoints
        int initialNumBreakpoints = breakpoints.length
        try {
            adapter.toggleLineBreakpoints(editor, selection)
            SynchronizationUtils.joinBackgroudActivities()
        } finally {
            IBreakpoint[] newBreakpoints = breakpointManager.breakpoints
            assertEquals('Unexpected number of breakpoints', initialNumBreakpoints + 1, newBreakpoints.length)
            for (breakpoint in newBreakpoints) {
                breakpointManager.removeBreakpoint(breakpoint, true)
            }
            assertEquals('Should have deleted all breakpoints', 0, breakpointManager.breakpoints.length)
        }
    }
}
