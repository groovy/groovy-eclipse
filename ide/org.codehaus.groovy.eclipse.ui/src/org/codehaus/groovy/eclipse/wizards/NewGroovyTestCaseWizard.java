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

import greclipse.org.eclipse.jdt.internal.junit.wizards.NewTestCaseCreationWizard;

import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
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
	    NewGroovyTestTypeWizardPage fPage1= new NewGroovyTestTypeWizardPage(fPage2);
        addPage(fPage1);
        fPage1.init(getSelection());
        addPage(fPage2);
        
        ReflectionUtils.setPrivateField(NewTestCaseCreationWizard.class, "fPage1", this, fPage1);
        ReflectionUtils.setPrivateField(NewTestCaseCreationWizard.class, "fPage2", this, fPage2);
	}

}
