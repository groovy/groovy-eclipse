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

import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.junit.wizards.NewTestCaseCreationWizard;
import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageOne;
import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageTwo;

/**
 * @author Andrew Eisenberg
 * @created Jul 22, 2009
 * 
 *
 */
public class NewGroovyTestCaseWizard extends NewTestCaseCreationWizard {

	public NewGroovyTestCaseWizard() {
		super();
		setWindowTitle("New Groovy JUnit test case"); 
    }
	
	@Override
	public void addPages() {
	    NewTestCaseWizardPageTwo fPage2= new NewTestCaseWizardPageTwo();
	    NewTestCaseWizardPageOne fPage1= new NewGroovyTestTypeWizardPage(fPage2);
        addPage(fPage1);
        fPage1.init(getSelection());
        addPage(fPage2);
        
        ReflectionUtils.setPrivateField(NewTestCaseCreationWizard.class, "fPage1", this, fPage1);
        ReflectionUtils.setPrivateField(NewTestCaseCreationWizard.class, "fPage2", this, fPage2);
	}

	
	
}
