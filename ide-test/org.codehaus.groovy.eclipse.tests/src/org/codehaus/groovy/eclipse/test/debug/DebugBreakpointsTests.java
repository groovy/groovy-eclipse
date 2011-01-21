 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.debug;

import java.io.InputStream;
import java.net.URL;

import junit.framework.AssertionFailedError;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.debug.ui.ToggleBreakpointAdapter;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.test.Activator;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.codehaus.groovy.eclipse.test.SynchronizationUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.debug.ui.actions.ActionDelegateHelper;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;

/**
 * @author Andrew Eisenberg
 * @created Jul 24, 2009
 *
 */
public class DebugBreakpointsTests extends EclipseTestCase {
    private static final String BREAKPOINT_SCRIPT_NAME = "BreakpointTesting.groovy";
    
    private ToggleBreakpointAdapter adapter;
    
    private ICompilationUnit unit;
    
    private GroovyEditor editor;
    
    private String text;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
         GroovyRuntime.addGroovyRuntime(testProject.getProject());

         InputStream input = null;
         final URL url = Activator.bundle().getEntry(
                 "/testData/groovyfiles/" + BREAKPOINT_SCRIPT_NAME);
         try {
             input = url.openStream();
             IFile file = testProject.createGroovyTypeAndPackage("",
                     BREAKPOINT_SCRIPT_NAME, input);
             
             unit = JavaCore.createCompilationUnitFrom(file);
         } finally {
             IOUtils.closeQuietly(input);
         }
         
         try {
             input = url.openStream();
             text = IOUtils.toString(input);
         } finally {
             IOUtils.closeQuietly(input);
         }
         adapter = new ToggleBreakpointAdapter();
         
         editor = (GroovyEditor) EditorUtility.openInEditor(unit);
         
         ReflectionUtils.setPrivateField(ActionDelegateHelper.class, "fTextEditor", ActionDelegateHelper.getDefault(), editor);
         
         unit.becomeWorkingCopy(null);
         unit.makeConsistent(null);
         SynchronizationUtils.joinBackgroudActivities();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        unit.discardWorkingCopy();
        editor.close(false);
        SynchronizationUtils.joinBackgroudActivities();
    }
    
    public void testBreakpointInScript1() throws Exception {
        doBreakpointTest(1);
    }
    
    public void testBreakpointInScript2() throws Exception {
        doBreakpointTest(2);
    }

    public void testBreakpointInScript3() throws Exception {
        doBreakpointTest(3);
    }

    public void testBreakpointInScript4() throws Exception {
        doBreakpointTest(4);
    }

    public void testBreakpointInScript5() throws Exception {
        doBreakpointTest(5);
    }

    public void testBreakpointInScript6() throws Exception {
        doBreakpointTest(6);
    }

    public void testBreakpointInScript7() throws Exception {
        doBreakpointTest(7);
    }

    public void testBreakpointInScript8() throws Exception {
        doBreakpointTest(8);
    }

    public void testBreakpointInScript9() throws Exception {
        doBreakpointTest(9);
    }

    public void testBreakpointInScript10() throws Exception {
        doBreakpointTest(10);
    }

    public void testBreakpointInScript11() throws Exception {
        doBreakpointTest(11);
    }

    public void testBreakpointInScript12() throws Exception {
        doBreakpointTest(12);
    }

    public void testBreakpointInScript13() throws Exception {
        doBreakpointTest(13);
    }

    public void testBreakpointInScript14() throws Exception {
        doBreakpointTest(14);
    }
    
    public void testBreakpointInScript15() throws Exception {
        doBreakpointTest(15);
    }
    
    public void testBreakpointInScript16() throws Exception {
        doBreakpointTest(16);
    }
    
    public void testBreakpointInScript17() throws Exception {
        doBreakpointTest(17);
    }

    public void testBreakpointInScript18() throws Exception {
        doBreakpointTest(18);
    }
    
    public void testBreakpointInScript19() throws Exception {
        doBreakpointTest(19);
    }
    
    public void testBreakpointInScript20() throws Exception {
        doBreakpointTest(20);
    }
    
    public void testBreakpointInScript21() throws Exception {
        doBreakpointTest(21);
    }
    
    public void testBreakpointInScript22() throws Exception {
        doBreakpointTest(22);
    }
    
    public void testBreakpointInScript23() throws Exception {
        doBreakpointTest(23);
    }

    private void doBreakpointTest(int i) throws Exception {
        // occasional failures on build server...perform in a loop
        int count = 0;
        int maxCount = 5;
        while (count <= maxCount) {
            count++;
            try {
                innerDoBreakpointTest(i);
            } catch (AssertionFailedError err) {
                if (count >= maxCount) {
                    throw err;
                }
            }
        }
    }

    private void innerDoBreakpointTest(int i) throws CoreException {
        ITextSelection selection = new TextSelection(new Document(text), text.indexOf("// " + i)-3, 3);
        boolean canToggle = adapter.canToggleLineBreakpoints(editor, selection);
        assertTrue("Should be able to toggle breakpoint at section " + i, canToggle);
        
        int initialNumBreakpoints;
        IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
        IBreakpoint[] breakpoints = breakpointManager.getBreakpoints();
        initialNumBreakpoints = breakpoints.length;
        try {
            adapter.toggleLineBreakpoints(editor, selection);
            SynchronizationUtils.joinBackgroudActivities();
            
        } finally {
            IBreakpoint[] newBreakpoints = breakpointManager.getBreakpoints();
            assertEquals("Unexpected number of breakpoints", initialNumBreakpoints+1, newBreakpoints.length);
            for (IBreakpoint breakpoint : newBreakpoints) {
                breakpointManager.removeBreakpoint(breakpoint, true);
            }
            assertEquals("Should have deleted all breakpoints", 0, breakpointManager.getBreakpoints().length);
        }
    }
}
