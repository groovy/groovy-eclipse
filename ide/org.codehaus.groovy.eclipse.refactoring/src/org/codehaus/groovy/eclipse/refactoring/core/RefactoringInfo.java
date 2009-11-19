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

import java.util.Observable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;


/**
 * @author Michael Klenk mklenk@hsr.ch
 */

public class RefactoringInfo extends Observable{
	
	protected RefactoringProvider provider;
	
	public RefactoringInfo(RefactoringProvider provider) {
		this.provider = provider;
	}

	public UserSelection getSelection() {
		return provider.getSelection();
	}

	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return provider.checkFinalConditions(pm);
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return provider.checkInitialConditions(pm);
	}

	public GroovyChange createGroovyChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		return provider.createGroovyChange(pm);
	}
	
	public RefactoringProvider getProvider() {
		return provider;
	}

}
