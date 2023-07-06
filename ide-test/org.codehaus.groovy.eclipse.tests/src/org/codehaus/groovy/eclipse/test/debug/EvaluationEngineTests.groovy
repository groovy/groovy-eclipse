/*
 * Copyright 2009-2023 the original author or authors.
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

import static org.eclipse.debug.core.DebugPlugin.getDefault as getDebugPlugin
import static org.eclipse.jdt.debug.core.JDIDebugModel.createLineBreakpoint
import static org.eclipse.jdt.internal.debug.core.JDIDebugPlugin.getDefault as getJdiPlugin
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.*

import java.util.concurrent.SynchronousQueue
import java.util.concurrent.TimeUnit

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.debug.core.DebugEvent
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchManager
import org.eclipse.debug.core.model.IThread
import org.eclipse.jdt.debug.core.IJavaDebugTarget
import org.eclipse.jdt.debug.eval.IEvaluationResult
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils
import org.junit.Before
import org.junit.Test

final class EvaluationEngineTests extends GroovyEclipseTestSuite {

    @Before
    void setUp() {
        def debugUI = org.eclipse.debug.internal.ui.DebugUIPlugin.getDefault()
        debugUI.preferenceStore.setValue('org.eclipse.debug.ui.switch_perspective_on_suspend', 'never')
    }

    @Test
    void testEvalSnippet1() {
        def (launch, thread) = runToLine(3, '''\
            |public class Main {
            |  public static void main(String[] args) {
            |    System.out.println("hello world");
            |  }
            |}
            |'''.stripMargin())

        try {
            IEvaluationResult result = evaluate('args[0]', thread)
            assert !result.hasErrors() : result.errorMessages[0]
            assert result.value.valueString == 'foo'
        } finally {
            launch.terminate()
        }
    }

    @Test
    void testEvalSnippet2() {
        def (launch, thread) = runToLine(3, '''\
            |public class Main {
            |  public static void main(String[] args) {
            |    System.out.println("hello world");
            |  }
            |}
            |'''.stripMargin())

        try {
            IEvaluationResult result = evaluate('args.first()', thread)
            assert !result.hasErrors() : result.errorMessages[0]
            assert result.value.valueString == 'foo'
        } finally {
            launch.terminate()
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1491
    void testEvalSnippet3() {
        def (launch, thread) = runToLine(4, '''\
            |public class Main {
            |  public static void main(String[] args) {
            |    int i = 123;
            |    System.out.print("");
            |  }
            |}
            |'''.stripMargin())

        try {
            IEvaluationResult result = evaluate('i - 81', thread)
            assert !result.hasErrors() : result.errorMessages[0]
            assert result.value.intValue == 42
        } finally {
            launch.terminate()
        }
    }

    //--------------------------------------------------------------------------

    IEvaluationResult evaluate(String snippet, IThread thread) {
        assert thread.isSuspended()
        def result = new SynchronousQueue<IEvaluationResult>()
        def engine = jdiPlugin.getEvaluationEngine(packageFragmentRoot.javaProject, (IJavaDebugTarget) thread.debugTarget)
        engine.evaluate(snippet, thread.topStackFrame, result.&put, DebugEvent.EVALUATION, false)
        result.poll(5, TimeUnit.SECONDS)
    }

    Tuple2<ILaunch, IThread> runToLine(int line, String name = 'Main', String text) {
        def unit = addJavaSource(text, name); buildProject()
        def type = unit.getType(name)

        def info = [:]; def range = type.sourceRange
        BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(info, type, range.offset, range.offset + range.length)
        def bp = createLineBreakpoint(unit.underlyingResource, type.fullyQualifiedName, line, -1, -1, 0, true, info)
        try {
            def confType = debugPlugin.launchManager.getLaunchConfigurationType(ID_JAVA_APPLICATION)

            def workCopy = confType.newInstance(null, debugPlugin.launchManager.generateLaunchConfigurationName(name))
            workCopy.setAttribute(ATTR_MODULE_NAME, packageFragmentRoot.moduleDescription?.elementName ?: '')
            workCopy.setAttribute(ATTR_PROJECT_NAME, unit.javaProject.elementName)
            workCopy.setAttribute(ATTR_MAIN_TYPE_NAME, type.fullyQualifiedName)
            workCopy.setAttribute(ATTR_PROGRAM_ARGUMENTS, 'foo bar baz')
            workCopy.setAttribute(ATTR_EXCLUDE_TEST_CODE, true)
            workCopy.setAttribute(ILaunchManager.ATTR_PRIVATE, true)
            workCopy./**/setMappedResources(unit.underlyingResource)

            def waiter = new DebugEventHandler()

            def launch = workCopy.launch('debug', null)
            try {
                synchronized (waiter) {
                    waiter.wait(9999)
                }
                return new Tuple2(launch, waiter.thread)
            } catch (e) {
                launch.terminate(); throw e
            }
        } finally {
            bp.delete()
        }
    }

    static class DebugEventHandler implements org.eclipse.debug.core.IDebugEventSetListener {

        IThread thread

        DebugEventHandler() {
            debugPlugin.addDebugEventListener(this)
        }

        @Override
        synchronized void handleDebugEvents(DebugEvent[] events) {
            for (event in events) {
                if (event.kind == DebugEvent.SUSPEND && event.source instanceof IThread) {
                    debugPlugin.removeDebugEventListener(this)
                    this.thread = event.source
                    notifyAll()
                    return
                }
            }
        }
    }
}
