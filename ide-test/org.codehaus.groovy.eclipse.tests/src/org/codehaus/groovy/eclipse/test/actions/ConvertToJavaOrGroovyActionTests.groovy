/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.actions

import org.codehaus.groovy.eclipse.editor.actions.RenameToGroovyAction
import org.codehaus.groovy.eclipse.editor.actions.RenameToGroovyOrJavaAction
import org.codehaus.groovy.eclipse.editor.actions.RenameToJavaAction
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.ui.IActionDelegate
import org.junit.Assert
import org.junit.Test

/**
 * Tests the commands RenameToGroovy and RenameToJava.
 */
final class ConvertToJavaOrGroovyActionTests extends GroovyEclipseTestSuite {

    @Test
    void testRenameToGroovy() {
        IResource file = addJavaSource("class Bar { }", "Bar", "foo").getResource()
        Assert.assertTrue(file.getName() + " should exist", file.exists())
        StructuredSelection ss = new StructuredSelection(file)
        IActionDelegate action = new RenameToGroovyAction()
        action.selectionChanged(null, ss)
        action.run(null)
        waitForJobAndRefresh(file)
        Assert.assertFalse(file.getName() + " should not exist", file.exists())

        file = file.getParent().getFile(new Path("Bar.groovy"))
        Assert.assertTrue(file.getName() + " should exist", file.exists())
    }

    @Test
    void testRenameToJava() {
        IResource file = addGroovySource("class Bar { }", "Bar", "foo").getResource()
        Assert.assertTrue(file.getName() + " should exist", file.exists())
        StructuredSelection ss = new StructuredSelection(file)
        IActionDelegate action = new RenameToJavaAction()
        action.selectionChanged(null, ss)
        action.run(null)
        waitForJobAndRefresh(file)
        Assert.assertFalse(file.getName() + " should not exist", file.exists())

        file = file.getParent().getFile(new Path("Bar.java"))
        Assert.assertTrue(file.getName() + " should exist", file.exists())
    }

    @Test
    void testRenameToGroovyAndBack() {
        IResource file = addJavaSource("class Bar { }", "Bar", "foo").getResource()
        Assert.assertTrue(file.getName() + " should exist", file.exists())
        StructuredSelection ss = new StructuredSelection(file)
        IActionDelegate action = new RenameToGroovyAction()
        action.selectionChanged(null, ss)
        action.run(null)
        waitForJobAndRefresh(file)
        Assert.assertFalse(file.getName() + " should not exist", file.exists())

        file = file.getParent().getFile(new Path("Bar.groovy"))
        Assert.assertTrue(file.getName() + " should exist", file.exists())

        // now back again
        ss = new StructuredSelection(file)
        action = new RenameToJavaAction()
        action.selectionChanged(null, ss)
        action.run(null)
        waitForJobAndRefresh(file)
        Assert.assertFalse(file.getName() + " should not exist", file.exists())

        file = file.getParent().getFile(new Path("Bar.java"))
        Assert.assertTrue(file.getName() + " should exist", file.exists())
    }

    @Test
    void testRenameToJavaAndBack() {
        IResource file = addGroovySource("class Bar { }", "Bar", "foo").getResource()
        Assert.assertTrue(file.getName() + " should exist", file.exists())
        StructuredSelection ss = new StructuredSelection(file)
        IActionDelegate action = new RenameToJavaAction()
        action.selectionChanged(null, ss)
        action.run(null)
        waitForJobAndRefresh(file)
        Assert.assertFalse(file.getName() + " should not exist", file.exists())

        file = file.getParent().getFile(new Path("Bar.java"))
        Assert.assertTrue(file.getName() + " should exist", file.exists())

        // now back again
        ss = new StructuredSelection(file)
        action = new RenameToGroovyAction()
        action.selectionChanged(null, ss)
        action.run(null)
        waitForJobAndRefresh(file)
        Assert.assertFalse(file.getName() + " should not exist", file.exists())

        file = file.getParent().getFile(new Path("Bar.groovy"))
        Assert.assertTrue(file.getName() + " should exist", file.exists())
    }

    private void waitForJobAndRefresh(IResource file) throws Exception {
        Job.getJobManager().join(RenameToGroovyOrJavaAction.class, null)
        file.getParent().refreshLocal(IResource.DEPTH_INFINITE, null)
    }
}
