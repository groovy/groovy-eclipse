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

package org.codehaus.groovy.eclipse.refactoring.core.inlineMethod;

import org.codehaus.groovy.eclipse.refactoring.core.RefactoringInfo;

public class InlineMethodInfo extends RefactoringInfo {
	
	private InlineMethodProvider provider;

	public InlineMethodInfo(InlineMethodProvider provider) {
		super(provider);
		this.provider = provider;
	}
	
	public boolean isInlineAllInvocations() {
		return provider.isInlineAllInvocations();
	}

	public void setInlineAllInvocations(boolean inlineAllInvocations) {
		provider.setInlineAllInvocations(inlineAllInvocations);
	}

	public boolean isDeleteMethod() {
		return provider.isDeleteMethod();
	}

	public void setDeleteMethod(boolean deleteMethod) {
		provider.setDeleteMethod(deleteMethod);
	}

	public boolean isMethodDeclarationSelected() {
		return provider.isMethodDeclarationSelected();
	}

	public void setMethodDeclarationSelected(boolean methodDeclarationSelected) {
		provider.setMethodDeclarationSelected(methodDeclarationSelected);
	}
	
}
