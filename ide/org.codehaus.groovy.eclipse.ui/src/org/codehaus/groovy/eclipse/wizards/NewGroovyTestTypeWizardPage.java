 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.junit.util.JUnitStatus;
import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageOne;
import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageTwo;

/**
 * @author Andrew Eisenberg
 * @created Jul 22, 2009
 *
 */
public class NewGroovyTestTypeWizardPage extends NewTestCaseWizardPageOne {

    private static final String DOT_GROOVY = ".groovy";
    private static final String GROOVY_TEST_CASE = "groovy.util.GroovyTestCase";

    public NewGroovyTestTypeWizardPage(NewTestCaseWizardPageTwo page2) {
        super(page2);
    }
    
    /**
     * The de
     */
    @Override
    protected void initTypePage(IJavaElement elem) {
        super.initTypePage(elem);
        setSuperClass(GROOVY_TEST_CASE, true);
    }

    @Override
    protected String getCompilationUnitName(String typeName) {
        return typeName + DOT_GROOVY;
    }

    @Override
    public void setJUnit4(boolean isJUnit4, boolean isEnabled) {
        super.setJUnit4(isJUnit4, isEnabled);
        if (!isJUnit4) {
            setSuperClass(GROOVY_TEST_CASE, true);
        }
    }

    /**
     * Ensure that GroovyTestCase is seen as OK 
     * to have in the super class field even if
     * JUnit 3 is not yet on the classpath
     */
    @Override
    protected IStatus superClassChanged() {
        // replaces the super class validation of of the normal type wizard
        if (isJUnit4()) {
            return super.superClassChanged();
        }

        String superClassName= getSuperClass();
        if (GROOVY_TEST_CASE.equals(superClassName)) {
            return new JUnitStatus();
        }

        return super.superClassChanged();
    }
}
