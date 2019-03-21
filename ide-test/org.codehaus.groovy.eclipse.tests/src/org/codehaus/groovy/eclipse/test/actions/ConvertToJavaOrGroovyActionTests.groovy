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
package org.codehaus.groovy.eclipse.test.actions

import org.codehaus.groovy.eclipse.editor.actions.RenameToGroovyAction
import org.codehaus.groovy.eclipse.editor.actions.RenameToGroovyOrJavaAction
import org.codehaus.groovy.eclipse.editor.actions.RenameToJavaAction
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.ui.IActionDelegate
import org.junit.Test

/**
 * Tests the commands RenameToGroovy and RenameToJava.
 */
final class ConvertToJavaOrGroovyActionTests extends GroovyEclipseTestSuite {

    private void waitForJobAndRefresh(IResource file) {
        Job.jobManager.join(RenameToGroovyOrJavaAction, null)
        file.parent.refreshLocal(IResource.DEPTH_INFINITE, null)
    }

    @Test
    void testRenameToGroovy() {
        IResource file = addJavaSource('class B { }', 'B', 'a').resource
        assert file.exists() : "$file.name should exist"
        StructuredSelection ss = new StructuredSelection(file)
        IActionDelegate action = new RenameToGroovyAction()
        action.selectionChanged(null, ss)
        action.run(null)
        waitForJobAndRefresh(file)
        assert !file.exists() : "$file.name should not exist"

        file = file.parent.getFile(new Path('B.groovy'))
        assert file.exists() : "$file.name should exist"
    }

    @Test
    void testRenameToJava() {
        IResource file = addGroovySource('class C { }', 'C', 'b').resource
        assert file.exists() : "$file.name should exist"
        StructuredSelection ss = new StructuredSelection(file)
        IActionDelegate action = new RenameToJavaAction()
        action.selectionChanged(null, ss)
        action.run(null)
        waitForJobAndRefresh(file)
        assert !file.exists() : "$file.name should not exist"

        file = file.parent.getFile(new Path('C.java'))
        assert file.exists() : "$file.name should exist"
    }

    @Test
    void testRenameToGroovyAndBack() {
        IResource file = addJavaSource('class D { }', 'D', 'c').resource
        assert file.exists() : "$file.name should exist"
        StructuredSelection ss = new StructuredSelection(file)
        IActionDelegate action = new RenameToGroovyAction()
        action.selectionChanged(null, ss)
        action.run(null)
        waitForJobAndRefresh(file)
        assert !file.exists() : "$file.name should not exist"

        file = file.parent.getFile(new Path('D.groovy'))
        assert file.exists() : "$file.name should exist"

        // now back again
        ss = new StructuredSelection(file)
        action = new RenameToJavaAction()
        action.selectionChanged(null, ss)
        action.run(null)
        waitForJobAndRefresh(file)
        assert !file.exists() : "$file.name should not exist"

        file = file.parent.getFile(new Path('D.java'))
        assert file.exists() : "$file.name should exist"
    }

    @Test
    void testRenameToJavaAndBack() {
        IResource file = addGroovySource('class E { }', 'E', 'd').resource
        assert file.exists() : "$file.name should exist"
        StructuredSelection ss = new StructuredSelection(file)
        IActionDelegate action = new RenameToJavaAction()
        action.selectionChanged(null, ss)
        action.run(null)
        waitForJobAndRefresh(file)
        assert !file.exists() : "$file.name should not exist"

        file = file.parent.getFile(new Path('E.java'))
        assert file.exists() : "$file.name should exist"

        // now back again
        ss = new StructuredSelection(file)
        action = new RenameToGroovyAction()
        action.selectionChanged(null, ss)
        action.run(null)
        waitForJobAndRefresh(file)
        assert !file.exists() : "$file.name should not exist"

        file = file.parent.getFile(new Path('E.groovy'))
        assert file.exists() : "$file.name should exist"
    }
}
