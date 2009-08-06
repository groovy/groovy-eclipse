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

package org.codehaus.groovy.eclipse.refactoring.core.rename;

import org.codehaus.groovy.eclipse.refactoring.core.RefactoringInfo;
import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class RenameInfo extends RefactoringInfo {
	
	protected IRenameProvider provider;

	public RenameInfo(RefactoringProvider provider) {
		super(provider);
		this.provider = (IRenameProvider) provider;
	}

	public void setNewName(String newName) {
		provider.setNewName(newName);
	}
	
	public String getOldName(){
		return provider.getOldName();
	}
	
	public void checkUserInput(RefactoringStatus status, String text) {
		provider.checkUserInput(status, text);
	}
}
