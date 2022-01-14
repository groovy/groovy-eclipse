/*
 * Copyright 2009-2022 the original author or authors.
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

import static org.codehaus.jdt.groovy.model.GroovyNature.GROOVY_NATURE

import org.codehaus.groovy.eclipse.actions.AddGroovyNatureAction
import org.codehaus.groovy.eclipse.actions.RemoveGroovyNatureAction
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.groovy.eclipse.test.SynchronizationUtils
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.jface.viewers.StructuredSelection
import org.junit.Before
import org.junit.Test

final class GroovyNatureActionTests extends GroovyEclipseTestSuite {

    private AddGroovyNatureAction addGroovyAction
    private RemoveGroovyNatureAction removeGroovyAction

    @Before
    void setUp() {
        addGroovySource('class MainClass { static void main(args){ println "Hello Groovy World" } } ', 'MainClass')
        removeNature(GROOVY_NATURE)

        addGroovyAction = new AddGroovyNatureAction()
        removeGroovyAction = new RemoveGroovyNatureAction()
        removeGroovyAction.doNotAskToRemoveJars()
    }

    /**
     * Tests the action that adds GroovyNature to a java project.  Adds the nature twice to make sure nothing goes wrong with that.
     */
    @Test
    void testAddGroovyNature() {
        addGroovyAction.selectionChanged(null, new StructuredSelection(packageFragmentRoot.javaProject))

        assert !hasGroovyNature() : 'testProject should not have Groovy nature before testing action'

        addGroovyAction.run(null)

        assert hasGroovyNature() : 'testProject should have Groovy nature after testing action'
        assert hasGroovyLibraries() : 'testProject should have Groovy jars after running remove nature action'

        addGroovyAction.run(null)

        assert hasGroovyNature() : 'testProject should still have Groovy nature after testing action twice'
        assert hasGroovyLibraries() : 'testProject should have Groovy jars after running remove nature action'
    }

    /**
     * This tests to show and assert that the GroovyNature will not be added to a non java project.
     */
    @Test
    void testGroovyNatureNotJavaProject() {
        removeNature(JavaCore.NATURE_ID)
        try {
            addGroovyAction.selectionChanged(null, new StructuredSelection(packageFragmentRoot.javaProject))

            assert !hasGroovyNature() : 'testProject should not have Groovy nature before testing action'

            addGroovyAction.run(null)

            assert !hasGroovyNature() : 'testProject should not have Groovy nature after testing action'
        } finally {
            addNature(JavaCore.NATURE_ID)
        }
    }

    @Test
    void testRemoveGroovyNature() {
        IStructuredSelection selection = new StructuredSelection(packageFragmentRoot.javaProject)
        addGroovyAction.selectionChanged(null, selection)
        removeGroovyAction.selectionChanged(null, selection)

        assert !hasGroovyNature() : 'testProject should not have Groovy nature before testing action'
        assert hasGroovyLibraries() : 'testProject should have Groovy jars after running add nature action'

        removeGroovyAction.run(null)

        assert !hasGroovyNature() : 'testProject should not have Groovy nature after running remove nature action'
        assert !hasGroovyLibraries() : 'testProject should not have Groovy jars after running remove nature action'

        addGroovyAction.run(null)

        assert hasGroovyNature() : 'testProject should have Groovy nature after testing action'
        assert hasGroovyLibraries() : 'testProject should have Groovy jars after running add nature action'

        SynchronizationUtils.waitForDSLDProcessingToComplete()
        SynchronizationUtils.waitForIndexingToComplete()

        removeGroovyAction.run(null)

        assert !hasGroovyNature() : 'testProject should not have Groovy nature after running remove nature action'
        assert !hasGroovyLibraries() : 'testProject should not have Groovy jars after running remove nature action'
    }
}
