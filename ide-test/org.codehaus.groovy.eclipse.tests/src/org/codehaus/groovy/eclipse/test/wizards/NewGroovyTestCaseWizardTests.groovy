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

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime
import org.codehaus.groovy.eclipse.wizards.NewGroovyTestTypeWizardPage
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IPackageFragment
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageTwo
import org.junit.Before
import org.junit.Test

final class NewGroovyTestCaseWizardTests extends NewGroovyWizardTestCase {

    private IPackageFragment frag

    @Before
    void setUp() {
        frag = testProject.getSourceFolder().getPackageFragment('testPackage')
        if (frag == null) {
            frag = testProject.getSourceFolder().createPackageFragment('testPackage', false, null)
        }
    }

    private NewGroovyTestTypeWizardPage createdGroovyTestTypeWizardPage() {
        NewTestCaseWizardPageTwo pageTwo = new NewTestCaseWizardPageTwo()
        NewGroovyTestTypeWizardPage wizardPage = new NewGroovyTestTypeWizardPage(pageTwo)
        wizardPage.setPackageFragmentRoot(testProject.getSourceFolder(), true)
        wizardPage.setPackageFragment(frag, true)
        return wizardPage
    }

    @Test
    void testAddGroovyTestCaseNonGroovyProject() {
        GroovyRuntime.removeGroovyNature(testProject.getProject())
        NewGroovyTestTypeWizardPage page = createdGroovyTestTypeWizardPage()
        String testCaseName = 'NonGroovyProjectTestCase'
        page.setEnclosingTypeSelection(false, true)
        page.setTypeName(testCaseName, true)
        page.createType(new NullProgressMonitor())
        IType type = page.getCreatedType()
        assert type == null
    }

    @Test
    void testAddGroovyTestCaseGroovyProject() {
        GroovyRuntime.addGroovyNature(testProject.getProject())
        NewGroovyTestTypeWizardPage page = createdGroovyTestTypeWizardPage()
        String testCaseName = 'GroovyProjectTestCase'
        page.setEnclosingTypeSelection(false, true)
        page.setTypeName(testCaseName, true)
        page.createType(new NullProgressMonitor())
        IType type = page.getCreatedType()
        assert type.getElementName() == testCaseName
    }
}
