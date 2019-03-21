/*
 * Copyright 2009-2018 the original author or authors.
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

import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils
import org.codehaus.groovy.eclipse.launchers.GroovyScriptLaunchShortcut
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.groovy.eclipse.test.TestProject
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.IStreamListener
import org.eclipse.debug.core.model.IStreamMonitor
import org.eclipse.debug.internal.ui.DebugUIPlugin
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.groovy.tests.SimpleProgressMonitor
import org.eclipse.jface.dialogs.MessageDialogWithToggle
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

final class GroovyLauncherShortcutTests extends GroovyEclipseTestSuite {

    @Before
    void setUp() {
        DebugUIPlugin.getDefault().preferenceStore.with {
            setValue(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT, false)
            setValue(IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD, MessageDialogWithToggle.NEVER)
        }
    }

    @After
    void tearDown() {
        DebugUIPlugin.getDefault().preferenceStore.with {
            setToDefault(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT)
            setToDefault(IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD)
        }
    }

    @Test
    void testClasspathGeneration1() {
        TestProject p4 = new TestProject('P4')
        TestProject p3 = new TestProject('P3')
        TestProject p2 = new TestProject('P2')
        TestProject p1 = new TestProject('P1')
        try {
            p4.createSourceFolder('src2', 'bin2')

            p3.addProjectReference(p4.javaProject)
            p3.createSourceFolder('src2', 'bin2')

            p2.addProjectReference(p4.javaProject)
            p2.addProjectReference(p3.javaProject)

            p1.addProjectReference(p4.javaProject)
            p1.addProjectReference(p3.javaProject)
            p1.addProjectReference(p2.javaProject)

            String classpath = new GroovyScriptLaunchShortcut().generateClasspath(p1.javaProject)

            def entries = [
                ['P1', 'src'],
                ['P2', 'src'],
                ['P3', 'src'],
                ['P3', 'src2'],
                ['P4', 'src'],
                ['P4', 'src2'],
                ['P1', 'bin'],
                ['P2', 'bin'],
                ['P3', 'bin'],
                ['P3', 'bin2'],
                ['P4', 'bin'],
                ['P4', 'bin2'],
            ]
            String expected_classpath = '"' + entries.collect { String proj, String path ->
                '${workspace_loc:' + File.separator + proj + '}' + File.separator + path
            }.join(File.pathSeparator) + '"'

            Assert.assertEquals(expected_classpath, classpath)
        } finally {
            p1.dispose()
            p2.dispose()
            p3.dispose()
            p4.dispose()
        }
    }

    @Test
    void testClasspathGeneration2() {
        TestProject p1 = new TestProject('P1a')
        TestProject p2 = new TestProject('P2a')
        try {
            IPath runtimeJarPath = CompilerUtils.exportedGroovyAllJar
            p1.addExternalLibrary(runtimeJarPath)

            IFile f1 = p1.project.getFile('empty.jar')
            f1.create(new ByteArrayInputStream(new byte[0]), false, null)
            p1.addExternalLibrary(f1.fullPath)

            IFile f2 = p2.project.getFile('empty2.jar')
            f2.create(new ByteArrayInputStream(new byte[0]), false, null)
            p1.addExternalLibrary(f2.fullPath)

            String classpath = new GroovyScriptLaunchShortcut().generateClasspath(p1.javaProject)

            String expected_classpath = '"' +
                '${workspace_loc:' + File.separator + 'P1a}' + File.separator + 'empty.jar' + File.pathSeparator +
                '${workspace_loc:' + File.separator + 'P1a}' + File.separator + 'src' + File.pathSeparator +
                '${workspace_loc:' + File.separator + 'P2a}' + File.separator + 'empty2.jar' + File.pathSeparator +
                runtimeJarPath.toPortableString().replace('/' as char, File.separatorChar) + File.pathSeparator +
                '${workspace_loc:' + File.separator + 'P1a}' + File.separator + 'bin' +
                '"'

            Assert.assertEquals(expected_classpath, classpath)
        } finally {
            p1.dispose()
            p2.dispose()
        }
    }

    //

    @Test // single script
    void testScriptLaunch1() {
        def unit = addGroovySource('print "test me"', 'Launch')
        def type = unit.getType('Launch')
        launchScriptAndAssertExitValue(type)
    }

    @Test // script references other script
    void testScriptLaunch2() {
        addGroovySource('class Other{ def foo() { return "hi!" } }', 'Other')

        def unit = addGroovySource('print new Other().foo()', 'Launch')
        def type = unit.getType('Launch')
        launchScriptAndAssertExitValue(type)
    }

    @Test // script references java
    void testScriptLaunch3() {
        addJavaSource('class Other{ String foo() { return "hi!"; } }', 'Other')

        def unit = addGroovySource('print new Other().foo()', 'Launch')
        def type = unit.getType('Launch')
        launchScriptAndAssertExitValue(type)
    }

    @Test // script references script in other source folder
    void testScriptLaunch4() {
        addGroovySource('class Other{ def foo() { return "hi!" } }', 'Other', 'other', addSourceFolder('src2'))

        def unit = addGroovySource('print new other.Other().foo()', 'Launch')
        def type = unit.getType('Launch')
        launchScriptAndAssertExitValue(type)
    }

    @Test // script references java in other source folder
    void testScriptLaunch5() {
        addGroovySource('class Other{ String foo() { return "hi!"; } }', 'Other', 'other', addSourceFolder('src2'))

        def unit = addGroovySource('print new other.Other().foo()', 'Launch')
        def type = unit.getType('Launch')
        launchScriptAndAssertExitValue(type)
    }

    @Test // script in non-default source folder
    void testScriptLaunch6() {
        addGroovySource('class Other{ def foo() { return "hi!" } }', 'Other', 'otherOther')

        def unit = addGroovySource('print new otherOther.Other().foo()', 'Launch', 'other', addSourceFolder('src2'))
        def type = unit.getType('Launch')
        launchScriptAndAssertExitValue(type)
    }

    @Test // script references script with non-default output folder
    void testScriptLaunch7() {
        addGroovySource('class Other{ def foo() { return "hi!" } }', 'Other', 'other', addSourceFolder('src2'))

        def unit = addGroovySource('print new other.Other().foo()', 'Launch')
        def type = unit.getType('Launch')
        launchScriptAndAssertExitValue(type)
    }

    @Test // script references java with non-default output folder
    void testScriptLaunch8() {
        addGroovySource('class Other{ String foo() { return "hi!"; } }', 'Other', 'other', addSourceFolder('src2'))

        def unit = addGroovySource('print new other.Other().foo()', 'Launch')
        def type = unit.getType('Launch')
        launchScriptAndAssertExitValue(type)
    }

    @Test // script references script in other project
    void testScriptLaunch9() {
        TestProject otherProject = new TestProject('OtherProject')
        try {
            otherProject.createGroovyTypeAndPackage('pack', 'Other.groovy', 'class Other { String foo() { return "hi!" } }')
            addProjectReference(otherProject.javaProject)

            def unit = addGroovySource('print new pack.Other().foo()', 'Launch')
            def type = unit.getType('Launch')
            launchScriptAndAssertExitValue(type)
        } finally {
            otherProject.dispose()
        }
    }

    @Test // script references java in other project
    void testScriptLaunch10() {
        TestProject otherProject = new TestProject('OtherProject')
        try {
            otherProject.createJavaTypeAndPackage('pack', 'Other.java', 'public class Other { public String foo() { return "hi!"; } }')
            addProjectReference(otherProject.javaProject)

            def unit = addGroovySource('print new pack.Other().foo()', 'Launch')
            def type = unit.getType('Launch')
            launchScriptAndAssertExitValue(type)
        } finally {
            otherProject.dispose()
        }
    }

    @Test @Ignore // https://github.com/groovy/groovy-eclipse/issues/779
    void testScriptLaunch11() {
        def unit = addGroovySource('''\
            @Grapes([
              @GrabConfig(systemClassLoader=true),
              @Grab('mysql:mysql-connector-java:5.1.6')
            ])
            def xxx

            println "Why won't this run?"
            '''.stripIndent(), 'Launch')
        def type = unit.getType('Launch')
        launchScriptAndAssertExitValue(type)
    }

    //

    private void launchScriptAndAssertExitValue(IType launchType, int timeoutSeconds = 20) {
        SimpleProgressMonitor spm = new SimpleProgressMonitor('Launcher test workspace build')
        ResourcesPlugin.workspace.build(IncrementalProjectBuilder.FULL_BUILD, spm)
        spm.waitForCompletion()

        /*String problems = testProject.getProblems()
        if (problems != null) {
            Assert.fail('Compile problems:\n' + problems)
        }*/

        def runner = new Runnable() {
            @Override
            void run() {
                GroovyScriptLaunchShortcut shortcut = new GroovyScriptLaunchShortcut()
                ILaunchConfiguration config = shortcut.findOrCreateLaunchConfig(shortcut.createLaunchProperties(launchType, launchType.javaProject), launchType.fullyQualifiedName)
                Assert.assertTrue(launchType.exists())
                ILaunch launch = config.launch('run', new NullProgressMonitor())
                final StringBuilder stdout = new StringBuilder()
                final StringBuilder stderr = new StringBuilder()
                launch.processes[0].streamsProxy.outputStreamMonitor.addListener(new IStreamListener() {
                    @Override
                    void streamAppended(String text, IStreamMonitor monitor) {
                        stdout.append(text)
                    }
                })
                launch.processes[0].streamsProxy.errorStreamMonitor.addListener(new IStreamListener() {
                    @Override
                    void streamAppended(String text, IStreamMonitor monitor) {
                        stderr.append(text)
                    }
                })
                synchronized (launch) {
                    int i = 0
                    println('Waiting for launch to complete ' + i + ' sec...')
                    while (!launch.isTerminated() && i < timeoutSeconds) {
                        i++
                        println('Waiting for launch to complete ' + i + ' sec...')
                        launch.wait(1000)
                    }
                }
                if (launch.isTerminated()) {
                    Assert.assertEquals(1, launch.processes.length)
                    println('Process output:')
                    println('==================')
                    println(stdout)
                    println('==================')
                    println('Process err:')
                    println('==================')
                    println(stderr)
                    println('==================')
                }
                Assert.assertTrue('Process not terminated after timeout has been reached', launch.isTerminated())
                Assert.assertEquals('Expecting normal exit, but found invalid exit value', 0, launch.processes[0].exitValue)
            }
        }

        def currentException = null
        for (attempt in 1..4) {
            try {
                runner.run()
                // success
                return
            } catch (AssertionError e) {
                currentException = e
                println('Launch failed on attempt ' + attempt + ' retrying.')
            }
        }
        if (currentException != null) {
            throw currentException
        }
    }
}
