/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.wizards;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.wizards.NewGroovyTestTypeWizardPage;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageTwo;

/**
 *
 * @author ns
 * @created May 18, 2010
 */
public class NewGroovyTestCaseWizardTest extends AbstractNewGroovyWizardTest {

    private static final String PACKAGE_NAME = "testPackage";

    protected IPackageFragment frag = null;

    public NewGroovyTestCaseWizardTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        frag = fSourceFolder.getPackageFragment(PACKAGE_NAME);
        if (frag == null) {
            frag = fSourceFolder.createPackageFragment(PACKAGE_NAME, false, null);
        }
    }

    public void testAddGroovyTestCaseNonGroovyProject() throws Exception {
        GroovyRuntime.removeGroovyNature(fJProject.getProject());
        NewGroovyTestTypeWizardPage page = createdGroovyTestTypeWizardPage();
        String testCaseName = "NonGroovyProjectTestCase";
        page.setEnclosingTypeSelection(false, true);
        page.setTypeName(testCaseName, true);
        page.createType(new NullProgressMonitor());

        IType type = page.getCreatedType();

        assertNull(type);
    }

    public void testAddGroovyTestCaseGroovyProject() throws Exception {
        GroovyRuntime.addGroovyNature(fJProject.getProject());
        NewGroovyTestTypeWizardPage page = createdGroovyTestTypeWizardPage();
        String testCaseName = "GroovyProjectTestCase";
        page.setEnclosingTypeSelection(false, true);
        page.setTypeName(testCaseName, true);

        page.createType(new NullProgressMonitor());

        IType type = page.getCreatedType();

        assertEquals(testCaseName, type.getElementName());

    }

    protected NewGroovyTestTypeWizardPage createdGroovyTestTypeWizardPage() {
        NewTestCaseWizardPageTwo pageTwo = new NewTestCaseWizardPageTwo();
        NewGroovyTestTypeWizardPage wizardPage = new NewGroovyTestTypeWizardPage(pageTwo);
        wizardPage.setPackageFragmentRoot(fSourceFolder, true);
        wizardPage.setPackageFragment(frag, true);
        return wizardPage;
    }
}
