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
package org.codehaus.groovy.eclipse.test.wizards

import static org.codehaus.jdt.groovy.model.GroovyNature.GROOVY_NATURE
import static org.junit.Assert.assertEquals

import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.groovy.eclipse.wizards.NewTestWizard
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants
import org.eclipse.jdt.groovy.core.util.ReflectionUtils
import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageOne
import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageTwo
import org.junit.Before
import org.junit.Test

final class NewGroovyTestWizardTests extends GroovyEclipseTestSuite {

    @Before
    void setUp() {
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE)
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, 2)
    }

    @Test
    void testCreateGroovyTestCase_NotGroovyProject() {
        removeNature(GROOVY_NATURE)
        try {
            def wizardPage = newTestWizardPage1()
            wizardPage.setTypeName('NonGroovyProjectTestCase', true)
            wizardPage.createType(new NullProgressMonitor())

            assert wizardPage.createdType == null
        } finally {
            addNature(GROOVY_NATURE)
        }
    }

    @Test
    void testCreateGroovyTestCase_YesGroovyProject() {
        def wizardPage = newTestWizardPage1()
        wizardPage.setTypeName('GroovyProjectTestCase', true)
        wizardPage.createType(new NullProgressMonitor())

        String expected = '''\
            |package test
            |
            |class GroovyProjectTestCase extends GroovyTestCase {
            |
            |}
            |'''.stripMargin()

        assert wizardPage.createdType?.elementName == 'GroovyProjectTestCase'
        assertEquals(expected, wizardPage.createdType.compilationUnit.source)
    }

    @Test
    void testCreateGroovyTestCase_SetUpAndTearDown() {
        def wizardPage = newTestWizardPage1()
        wizardPage.setTypeName('GroovyProjectTestCase', true)
        wizardPage.setStubSelection('setUp', true)
        wizardPage.setStubSelection('tearDown', true)
        wizardPage.setStubSelection('constructor', true)
        wizardPage.createType(new NullProgressMonitor())

        String expected = '''\
            |package test
            |
            |class GroovyProjectTestCase extends GroovyTestCase {
            |
            |  public GroovyProjectTestCase(String name) {
            |    super(name)
            |  }
            |
            |  protected void setUp() throws Exception {
            |  }
            |
            |  protected void tearDown() throws Exception {
            |  }
            |
            |}
            |'''.stripMargin()

        assertEquals(expected, wizardPage.createdType.compilationUnit.source)
    }

    //--------------------------------------------------------------------------

    private NewTestWizard.PageOne newTestWizardPage1() {
        def wizardPage = new NewTestWizard.PageOne(new NewTestCaseWizardPageTwo())
        wizardPage.setPackageFragmentRoot(getPackageFragmentRoot(), true)
        wizardPage.setPackageFragment(getPackageFragment('test'), true)
        wizardPage.setEnclosingTypeSelection(false, true)
        wizardPage.setJUnit4(false, true)

        wizardPage.metaClass.setStubSelection = { String which, boolean state ->
            def stubs = ReflectionUtils.getPrivateField(NewTestCaseWizardPageOne, 'fMethodStubsButtons', delegate)
            int index = ['setUpClass', 'tearDownClass', 'setUp', 'tearDown', 'constructor'].indexOf(which)
            assert stubs.isEnabled(index) : "$which checkbox is not enabled"
            stubs.setSelection(index, state)
        }

        return wizardPage
    }
}
