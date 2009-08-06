/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.ui;

import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * Base Class of all RefactoringWizards for the Groovy language
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class GroovyRefactoringWizard extends RefactoringWizard {
	
	protected GroovyRefactoring refactoring;
	
	public GroovyRefactoringWizard(GroovyRefactoring refactoring, int flags) {
		super(refactoring, flags);
		this.refactoring = refactoring;
		setWindowTitle(refactoring.getName());
		setDefaultPageTitle(refactoring.getName());
	}
	
	@Override
    protected void addUserInputPages() {
		for (IWizardPage page : refactoring.getPages()){
			addPage(page);
		}
	}

}
