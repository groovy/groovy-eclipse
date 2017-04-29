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
package org.codehaus.groovy.eclipse.test.wizards

import static org.codehaus.jdt.groovy.model.GroovyNature.GROOVY_NATURE

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.groovy.eclipse.wizards.NewGroovyTestTypeWizardPage
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageTwo
import org.junit.Before
import org.junit.Test

final class NewGroovyTestCaseWizardTests extends GroovyEclipseTestSuite {

    @Before
    void setUp() {
    }

    private NewGroovyTestTypeWizardPage createGroovyTestTypeWizardPage() {
        NewGroovyTestTypeWizardPage wizardPage = new NewGroovyTestTypeWizardPage(new NewTestCaseWizardPageTwo())
            wizardPage.setPackageFragment(getPackageFragment('testPackage'), true)
        wizardPage.setPackageFragmentRoot(getPackageFragmentRoot(), true)
        return wizardPage
    }

    @Test
    void testAddGroovyTestCaseNonGroovyProject() {
        removeNature(GROOVY_NATURE)
        NewGroovyTestTypeWizardPage page = createGroovyTestTypeWizardPage()
        String testCaseName = 'NonGroovyProjectTestCase'
        page.setEnclosingTypeSelection(false, true)
        page.setTypeName(testCaseName, true)
        page.createType(new NullProgressMonitor())
        def type = page.createdType
        assert type == null
    }

    @Test
    void testAddGroovyTestCaseGroovyProject() {
        addNature(GROOVY_NATURE)
        NewGroovyTestTypeWizardPage page = createGroovyTestTypeWizardPage()
        String testCaseName = 'GroovyProjectTestCase'
        page.setEnclosingTypeSelection(false, true)
        page.setTypeName(testCaseName, true)
        page.createType(new NullProgressMonitor())
        def type = page.createdType
        assert type.elementName == testCaseName
    }
}
