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
package org.codehaus.groovy.eclipse.refactoring.core;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

abstract public class SingleFileRefactoringProvider extends RefactoringProvider {
	
	protected IGroovyDocumentProvider documentProvider;
	
	public SingleFileRefactoringProvider(IGroovyDocumentProvider docProvider, UserSelection selection) {
		super(selection);
		documentProvider = docProvider;
	}
	
	public ModuleNode getRootNode() {
		return documentProvider.getRootNode();
	}
	
	public IDocument getDocument() {
		return documentProvider.getDocument();
	}

	@Override
    public IGroovyDocumentProvider getDocumentProvider() {
		return documentProvider;
	}
	
	@Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus result = new RefactoringStatus();
		if (!documentProvider.fileExists()) {
			result.addFatalError("Sourcefile " + documentProvider.getName() + " not found");
		} else if (documentProvider.isReadOnly()) {
			result.addFatalError("Soucefile " + documentProvider.getName() + " is read only");
		}
		addInitialConditionsCheckStatus(result);
		return result;
	}
}
