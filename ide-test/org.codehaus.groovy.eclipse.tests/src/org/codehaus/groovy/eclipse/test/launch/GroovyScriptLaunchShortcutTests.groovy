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
package org.codehaus.groovy.eclipse.test.launch

import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils
import org.codehaus.groovy.eclipse.launchers.GroovyScriptLaunchShortcut
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.groovy.eclipse.test.TestProject
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.Adapters
import org.eclipse.debug.internal.ui.DebugUIPlugin
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants
import org.eclipse.jface.dialogs.MessageDialogWithToggle
import org.junit.After
import org.junit.Before
import org.junit.Test

final class GroovyScriptLaunchShortcutTests extends GroovyEclipseTestSuite {

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

            p2.addProjectReference(p3.javaProject)
            p2.addProjectReference(p4.javaProject)

            p1.addProjectReference(p2.javaProject)
            p1.addProjectReference(p3.javaProject)
            p1.addProjectReference(p4.javaProject)

            def classpath = buildScriptClasspath(p1.javaProject)

            def entries = [
                ['P1', 'bin' ],
                ['P1', 'src' ],
                ['P2', 'bin' ],
                ['P2', 'src' ],
                ['P3', 'bin' ],
                ['P3', 'src' ],
                ['P3', 'bin2'],
                ['P3', 'src2'],
                ['P4', 'bin' ],
                ['P4', 'src' ],
                ['P4', 'bin2'],
                ['P4', 'src2'],
            ]
            String expected_classpath = entries.collect { String proj, String path ->
                '${workspace_loc:' + proj + '}' + File.separator + path
            }.join(File.pathSeparator)

            assert classpath.endsWith(expected_classpath)
            assert classpath.contains(CompilerUtils.exportedGroovyAllJar.toOSString())
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
            // this should not produce a duplicate entry in classpath
            p1.addExternalLibrary(CompilerUtils.exportedGroovyAllJar)

            IFile f1 = p1.project.getFile('empty.jar')
            f1.create(new ByteArrayInputStream(new byte[0]), false, null)
            p1.addExternalLibrary(f1.fullPath)

            IFile f2 = p2.project.getFile('empty2.jar')
            f2.create(new ByteArrayInputStream(new byte[0]), false, null)
            p1.addExternalLibrary(f2.fullPath)

            String classpath = buildScriptClasspath(p1.javaProject)

            String expected_classpath = [
                '${workspace_loc:' + 'P1a}' + File.separator + 'bin',
                '${workspace_loc:' + 'P1a}' + File.separator + 'src',
                '${workspace_loc:' + 'P1a}' + File.separator + 'empty.jar',
                '${workspace_loc:' + 'P2a}' + File.separator + 'empty2.jar'
            ].join(File.pathSeparator)

            assert classpath.endsWith(expected_classpath)
            assert classpath.contains(CompilerUtils.exportedGroovyAllJar.toOSString())
        } finally {
            p1.dispose()
            p2.dispose()
        }
    }

    //

    @Test // single script
    void testScriptLaunch1() {
        def unit = addGroovySource('println "hello world"', 'Launch')
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
            otherProject.createJavaTypeAndPackage('pack', 'Other.java', '''\
                |class Other {
                |  String foo() {
                |    return "hi!";
                |  }
                |}
                |'''.stripMargin())
            addProjectReference(otherProject.javaProject)

            def unit = addGroovySource('print new pack.Other().foo()', 'Launch', 'pack')
            def type = unit.getType('Launch')
            launchScriptAndAssertExitValue(type)
        } finally {
            otherProject.dispose()
        }
    }

    @Test // type implements Runnable
    void testScriptLaunch11() {
        def unit = addGroovySource('''\
            |class Runner implements Runnable {
            |  void run() {
            |    println "hello world"
            |  }
            |}
            |'''.stripMargin(), 'Runner')
        def type = unit.getType('Runner')
        launchScriptAndAssertExitValue(type)
    }

    @Test // type is JUnit test
    void testScriptLaunch12() {
        addJUnit(4)
        def unit = addGroovySource('''\
            |import org.junit.*
            |
            |final class SomeTests {
            |  @Test
            |  void testSomething() {
            |    Assert.assertTrue(true)
            |  }
            |}
            |'''.stripMargin(), 'SomeTests')
        def type = unit.getType('SomeTests')
        launchScriptAndAssertExitValue(type)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/779
    void testScriptLaunch13() {
        def unit = addGroovySource('''\
            |@Grapes([
            |  @GrabConfig(systemClassLoader=true),
            |  @Grab('mysql:mysql-connector-java:5.1.6')
            |])
            |def xxx
            |
            |println "Why won't this run?"
            |'''.stripMargin(), 'Launch')
        def type = unit.getType('Launch')
        launchScriptAndAssertExitValue(type)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1163
    void testScriptLaunch14() {
        def file = addPlainText('''\
            |println 'hello world'
            |'''.stripMargin(), '../Launch.groovy') // '..' for root of project
        def type =  Adapters.adapt(file, GroovyCompilationUnit).getType('Launch')
        launchScriptAndAssertExitValue(type)
    }

    //--------------------------------------------------------------------------

    private String buildScriptClasspath(IJavaProject javaProject) {
        Map properties = new GroovyScriptLaunchShortcut().createLaunchProperties(null, javaProject)
        def arguments = properties.get(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS)
        def classpath = (arguments =~ /--classpath "(.*?)" /)[0][1]
    }

    private void launchScriptAndAssertExitValue(IType runType, int timeoutSeconds = 20) {
        ResourcesPlugin.workspace.build(IncrementalProjectBuilder.FULL_BUILD, null)

        def shortcut = new GroovyScriptLaunchShortcut()
        def config = shortcut.findOrCreateLaunchConfig(shortcut.createLaunchProperties(runType, runType.javaProject), runType.fullyQualifiedName)
        def launch = config.launch('run', null)
        def stdout = new StringBuilder(), stderr = new StringBuilder()
        launch.processes[0].streamsProxy.outputStreamMonitor.addListener { text, monitor ->
            stdout.append(text)
        }
        launch.processes[0].streamsProxy.errorStreamMonitor.addListener { text, monitor ->
            stderr.append(text)
        }
        synchronized (launch) {
            int i = 0
            while (!launch.isTerminated() && i < timeoutSeconds) {
                println "Waiting for launch to complete $i sec..."
                launch.wait(1000)
                i += 1
            }
        }
        if (launch.isTerminated()) {
            assert launch.processes.length == 1
            println('Process output:')
            println('==================')
            println(stdout)
            println('==================')
            println('Process err:')
            println('==================')
            println(stderr)
            println('==================')
        }
        assert launch.isTerminated() : 'Process not terminated after timeout has been reached'
        assert launch.processes[0].exitValue == 0 : "Expecting normal exit, but found exit value ${launch.processes[0].exitValue}"
    }
}
