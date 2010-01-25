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

import java.util.Map;

import junit.framework.AssertionFailedError;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.launchers.GroovyScriptLaunchShortcut;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.codehaus.groovy.eclipse.test.TestProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTrackerExtension;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaApplicationLaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.text.IRegion;

/**
 * 
 * @author Andrew Eisenberg
 * @created Jan 4, 2010
 */
public class GroovyLauncherShortcutTests extends EclipseTestCase {
    
    class MockGroovyScriptLaunchShortcut extends GroovyScriptLaunchShortcut {
        @Override
        protected Map<String, String> createLaunchProperties(IType runType) {
            return super.createLaunchProperties(runType);
        }
    }

    class MockJavaApplicationLaunchShortcut extends JavaApplicationLaunchShortcut {
        @Override
        protected ILaunchConfiguration createConfiguration(IType type) {
            return super.createConfiguration(type);
        }
    }
    
    class ConsoleListener implements IConsoleLineTrackerExtension {
        private IConsole console;
        String text = null;

        public void consoleClosed() { 
            text = getText();
        }
        String getText() {
            if (console == null) return text;
            return console.getDocument().get();
        }
        public void dispose() {
            this.console = null;
        }
        public void init(IConsole console) { 
            this.console = console;
        }

        public void lineAppended(IRegion line) { }
        
        int getExitValue() throws DebugException {
            IProcess process = console.getProcess();
            return process.isTerminated() ? process.getExitValue() : Integer.MIN_VALUE;
        }
        
        public IConsole getConsole() {
            return console;
        }
        
        boolean isTerminated() {
            return console == null || console.getProcess().isTerminated();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DebugUIPlugin.getDefault().getPreferenceStore().setValue(IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD, MessageDialogWithToggle.NEVER);
        DebugUIPlugin.getDefault().getPreferenceStore().setValue(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT, false);
        GroovyRuntime.addGroovyRuntime(testProject.getProject());
    }
    
    public GroovyLauncherShortcutTests() {
        super(GroovyLauncherShortcutTests.class.getCanonicalName());
    }

    // single script
    public void testScriptLaunch1() throws Exception {
        ICompilationUnit unit = createGroovyCompilationUnit("Launch.groovy", "print 'test me'");
        IType launchType = unit.getType("Launch");
        launchScriptAndAssertExitValue(launchType);
    }

    // script references other script
    public void testScriptLaunch2() throws Exception {
        createGroovyCompilationUnit("Other.groovy", "class Other{ def foo() { return \"hi!\" } }");
        ICompilationUnit unit = createGroovyCompilationUnit("Launch.groovy", "print new Other().foo()");
        IType launchType = unit.getType("Launch");
        launchScriptAndAssertExitValue(launchType);
    }
    
    // script references java
    public void testScriptLaunch3() throws Exception {
        createJavaCompilationUnit("Other.java", "class Other{ String foo() { return \"hi!\"; } }");
        ICompilationUnit unit = createGroovyCompilationUnit("Launch.groovy", "print new Other().foo()");
        IType launchType = unit.getType("Launch");
        launchScriptAndAssertExitValue(launchType);
    }
    
    // script references script in other source folder
    public void testScriptLaunch4() throws Exception {
        IPackageFragmentRoot newRoot = createSourceFolder();
        IPackageFragment newFrag = createFragment(newRoot);
        createGroovyCompilationUnit(newFrag, "Other.groovy", "class Other{ def foo() { return \"hi!\" } }");
        ICompilationUnit unit = createGroovyCompilationUnit("Launch.groovy", "print new other.Other().foo()");
        IType launchType = unit.getType("Launch");
        launchScriptAndAssertExitValue(launchType);
    }

    // script references java in other source folder
    public void testScriptLaunch5() throws Exception {
        IPackageFragmentRoot newRoot = createSourceFolder();
        IPackageFragment newFrag = createFragment(newRoot);
        createJavaCompilationUnit(newFrag, "Other.java", "class Other{ String foo() { return \"hi!\"; } }");
        ICompilationUnit unit = createGroovyCompilationUnit("Launch.groovy", "print new other.Other().foo()");
        IType launchType = unit.getType("Launch");
        launchScriptAndAssertExitValue(launchType);
    }
    
    // script in non-default source folder
    public void testScriptLaunch6() throws Exception {
        IPackageFragmentRoot newRoot = createSourceFolder();
        IPackageFragment newFrag = createFragment(newRoot);
        createGroovyCompilationUnit("otherOther", "Other.groovy", "class Other{ def foo() { return \"hi!\" } }");
        ICompilationUnit unit = createGroovyCompilationUnit(newFrag, "Launch.groovy", "print new otherOther.Other().foo()");
        IType launchType = unit.getType("Launch");
        launchScriptAndAssertExitValue(launchType);
    }
   
    // script references script with non-default output folder
    public void testScriptLaunch7() throws Exception {
        IPackageFragmentRoot newRoot = createSourceFolder("otherOut");
        IPackageFragment newFrag = createFragment(newRoot);
        createGroovyCompilationUnit(newFrag, "Other.groovy", "class Other{ def foo() { return \"hi!\" } }");
        ICompilationUnit unit = createGroovyCompilationUnit("Launch.groovy", "print new other.Other().foo()");
        IType launchType = unit.getType("Launch");
        launchScriptAndAssertExitValue(launchType);
    }
    
    // script references java with non-default output folder
    public void testScriptLaunch8() throws Exception {
        IPackageFragmentRoot newRoot = createSourceFolder("otherOut");
        IPackageFragment newFrag = createFragment(newRoot);
        createJavaCompilationUnit(newFrag, "Other.java", "class Other{ String foo() { return \"hi!\"; } }");
        ICompilationUnit unit = createGroovyCompilationUnit("Launch.groovy", "print new other.Other().foo()");
        IType launchType = unit.getType("Launch");
        launchScriptAndAssertExitValue(launchType);
    }
    
    // script references script in other project
    public void testScriptLaunch9() throws Exception {
        TestProject otherProject = new TestProject("OtherProject");
        try {
            GroovyRuntime.addGroovyRuntime(otherProject.getProject());
            testProject.addProjectReference(otherProject.getJavaProject());
            otherProject.createGroovyTypeAndPackage("pack", "Other.groovy", "class Other { String foo() { return \"hi!\"; } }");
            ICompilationUnit unit = createGroovyCompilationUnit("thisPack", "Launch.groovy", "print new pack.Other().foo()");
            IType launchType = unit.getType("Launch");
            launchScriptAndAssertExitValue(launchType);
        } finally {
            otherProject.dispose();
        }
    }
    
    // script references java in other project
    public void testScriptLaunch10() throws Exception {
        TestProject otherProject = new TestProject("OtherProject");
        try {
            testProject.addProjectReference(otherProject.getJavaProject());
            otherProject.createJavaTypeAndPackage("pack", "Other.java", "public class Other { public String foo() { return \"hi!\"; } }");
            ICompilationUnit unit = createGroovyCompilationUnit("thisPack", "Launch.groovy", "print new pack.Other().foo()");
            IType launchType = unit.getType("Launch");
            launchScriptAndAssertExitValue(launchType);
        } finally {
            otherProject.dispose();
        }
    }
    
    
    /**
     * @param newRoot
     * @return
     */
    private IPackageFragment createFragment(IPackageFragmentRoot newRoot) throws Exception {
        return newRoot.createPackageFragment("other", true, null);
    }

    private IPackageFragmentRoot createSourceFolder() throws Exception {
        return testProject.createOtherSourceFolder();
    }

    private IPackageFragmentRoot createSourceFolder(String outFolder) throws Exception {
        return testProject.createOtherSourceFolder(outFolder);
    }
    
    private ICompilationUnit createGroovyCompilationUnit(IPackageFragment frag, String unitName, String contents) throws CoreException {
        IFile file = testProject.createGroovyType(frag, unitName, contents);
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
        return unit;
    }
    
    private ICompilationUnit createGroovyCompilationUnit(String unitName, String contents) throws CoreException {
        return createGroovyCompilationUnit("", unitName, contents);
    }
    private ICompilationUnit createGroovyCompilationUnit(String packageName, String unitName, String contents) throws CoreException {
        IFile file = testProject.createGroovyTypeAndPackage(packageName, unitName, contents);
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
        return unit;
    }
    
    private IType createJavaCompilationUnit(String unitName, String contents) throws CoreException {
        return testProject.createJavaTypeAndPackage("", unitName, contents);
    }
    
    private IType createJavaCompilationUnit(IPackageFragment frag, String unitName, String contents) throws CoreException {
        return testProject.createJavaType(frag, unitName, contents);
    }
    
    
    protected void launchScriptAndAssertExitValue(IType launchType) throws InterruptedException, CoreException {
        launchScriptAndAssertExitValue(launchType, 20);
    }
    protected void launchScriptAndAssertExitValue(final IType launchType, final int timeoutSeconds) throws InterruptedException, CoreException {
        
        String problems = testProject.getProblems();
        if (problems != null) {
            fail("Compile problems:\n" + problems);
        }

        Runnable runner = new Runnable() {
            public void run() {
                try {
                    MockGroovyScriptLaunchShortcut shortcut = new MockGroovyScriptLaunchShortcut();
                    ILaunchConfiguration config = shortcut.findOrCreateLaunchConfig(shortcut.createLaunchProperties(launchType), launchType.getFullyQualifiedName());
                    ConsoleListener listener = new ConsoleListener();
                    ConsoleLineTracker.setDelegate(listener);
                    assertTrue(launchType.exists());
                    DebugUIPlugin.launchInForeground(config, "run");
                    synchronized (listener) {
                        int i = 0;
                        while (!listener.isTerminated() && i < timeoutSeconds) {
                            i++;
                            System.out.println("Waiting for launch to complete " + i + " sec...");
                            listener.wait(1000);
                        }
                    }
                    
                    assertTrue("Process not terminated after timeout has been reached", listener.isTerminated());
                    assertEquals("Expecting normal exit, but found invalid exit value", 0, listener.getExitValue());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        
        AssertionFailedError currentException = null;
        for (int attempt = 0; attempt < 4; attempt++) {
            try {
                runner.run();
                
                // success
                return;
            } catch (AssertionFailedError e) {
                currentException = e;
                System.out.println("Launch failed on attempt " + attempt + " retrying."); 
            }
            
        }
        if (currentException != null) {
            throw currentException;
        }
    }
}
