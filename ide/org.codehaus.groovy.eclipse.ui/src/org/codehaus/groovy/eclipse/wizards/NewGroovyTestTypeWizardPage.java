/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.wizards;

import org.eclipse.jdt.core.IJavaElement;
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

}
