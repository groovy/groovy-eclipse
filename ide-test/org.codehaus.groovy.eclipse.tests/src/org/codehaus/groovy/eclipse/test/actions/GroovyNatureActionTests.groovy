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

import org.codehaus.groovy.eclipse.actions.AddGroovyNatureAction
import org.codehaus.groovy.eclipse.actions.RemoveGroovyNatureAction
import org.codehaus.groovy.eclipse.core.builder.ConvertLegacyProject
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime
import org.codehaus.groovy.eclipse.test.EclipseTestCase
import org.eclipse.core.resources.ICommand
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.jface.viewers.StructuredSelection
import org.junit.Assert
import org.junit.Before
import org.junit.Test

final class GroovyNatureActionTests extends EclipseTestCase {

    private AddGroovyNatureAction addGroovyAction
    private RemoveGroovyNatureAction removeGroovyAction
    private ConvertLegacyProject convert

    @Before
    void setUp() {
        testProject.createGroovyTypeAndPackage("pack1", "MainClass.groovy", "class MainClass { static void main(args){ println \"Hello Groovy World\" } } ")
        GroovyRuntime.removeGroovyNature(testProject.project)
        addGroovyAction = new AddGroovyNatureAction()
        removeGroovyAction = new RemoveGroovyNatureAction()
        removeGroovyAction.doNotAskToRemoveJars()
        convert = new ConvertLegacyProject()
    }

    /**
     * Tests the action that adds GroovyNature to a java project.  Adds the nature twice to make sure nothing goes wrong with that.
     */
    @Test
    void testAddGroovyNature() {
        Assert.assertTrue("testProject must have the Java nature", testProject.getJavaProject().getProject().hasNature(JavaCore.NATURE_ID))

        IStructuredSelection selection = new StructuredSelection([testProject.getJavaProject()] as Object[])

        addGroovyAction.selectionChanged(null, selection)

        Assert.assertFalse("testProject should not have Groovy nature before testing action", hasGroovyNature())
        // groovy runtime added automatically for testProject and not removed in setUp
        //assertFalse("testProject should not have Groovy jars after running remove nature action", hasGroovyJars())

        addGroovyAction.run(null)

        Assert.assertTrue("testProject should have Groovy nature after testing action", hasGroovyNature())
        Assert.assertTrue("testProject should have Groovy jars after running remove nature action", testProject.hasGroovyContainer())

        addGroovyAction.run(null)

        Assert.assertTrue("testProject should still have Groovy nature after testing action twice", hasGroovyNature())
        Assert.assertTrue("testProject should have Groovy jars after running remove nature action", testProject.hasGroovyContainer())
    }

    /**
     * This tests to show and assert that the GroovyNature will not be added to a non java project.
     */
    @Test
    void testGroovyNatureNotJavaProject() {
        testProject.removeNature(JavaCore.NATURE_ID)
        Assert.assertFalse(testProject.getJavaProject().getProject().hasNature(JavaCore.NATURE_ID))

        IStructuredSelection selection = new StructuredSelection([testProject.getProject()] as Object[])
        addGroovyAction.selectionChanged(null, selection)

        Assert.assertFalse("testProject should not have Groovy nature before testing action", hasGroovyNature())

        addGroovyAction.run(null)

        Assert.assertFalse("testProject should not have Groovy nature after testing action", hasGroovyNature())
    }

    @Test
    void testRemoveGroovyNature() {
        Assert.assertTrue("testProject must have the Java nature", testProject.getJavaProject().getProject().hasNature(JavaCore.NATURE_ID))

        IStructuredSelection selection = new StructuredSelection([testProject.getJavaProject()] as Object[])

        addGroovyAction.selectionChanged(null, selection)
        removeGroovyAction.selectionChanged(null, selection)

        Assert.assertFalse("testProject should not have Groovy nature before testing action", hasGroovyNature())
        Assert.assertTrue("testProject should have Groovy jars after running add nature action", testProject.hasGroovyContainer())

        removeGroovyAction.run(null)

        Assert.assertFalse("testProject should not have Groovy nature after running remove nature action", hasGroovyNature())
        Assert.assertFalse("testProject should not have Groovy jars after running remove nature action", testProject.hasGroovyContainer())

        addGroovyAction.run(null)

        Assert.assertTrue("testProject should have Groovy nature after testing action", hasGroovyNature())
        Assert.assertTrue("testProject should have Groovy jars after running add nature action", testProject.hasGroovyContainer())

        removeGroovyAction.run(null)

        Assert.assertFalse("testProject should not have Groovy nature after running remove nature action", hasGroovyNature())
        Assert.assertFalse("testProject should not have Groovy jars after running remove nature action", testProject.hasGroovyContainer())
    }

    @Test
    void testConvertLegacyAction() {
        // can't add old nature since it doesn't exist
        //testProject.addNature(ConvertLegacyProject.OLD_NATURE)
        testProject.addBuilder(ConvertLegacyProject.OLD_BUILDER)
        convert.convertProject(testProject.getProject())
        Assert.assertTrue("testProject should have Groovy nature after conversion", hasGroovyNature())
        Assert.assertFalse("testProject should not have OLD Groovy nature after conversion", hasOldGroovyNature())
        Assert.assertTrue("testProject should have Java builder after conversion", hasBuilder(JavaCore.BUILDER_ID))
        Assert.assertFalse("testProject should not have OLD Groovy builder after conversion", hasBuilder(ConvertLegacyProject.OLD_BUILDER))
    }

    private boolean hasOldGroovyNature() {
        return testProject.getProject().hasNature(ConvertLegacyProject.OLD_NATURE)
    }

    private boolean hasBuilder(String builderId) {
        IProjectDescription desc = testProject.getProject().getDescription()
        ICommand[] commands = desc.getBuildSpec()
        for (ICommand command : commands) {
            if (command.getBuilderName().equals(builderId)) {
                return true
            }
        }
        return false
    }
}
