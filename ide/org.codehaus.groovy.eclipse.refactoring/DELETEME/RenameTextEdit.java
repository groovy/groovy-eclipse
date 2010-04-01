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

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.RefactoringCodeVisitorSupport;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;

public abstract class RenameTextEdit extends RefactoringCodeVisitorSupport {

	protected MultiTextEdit edits = new MultiTextEdit();
	protected String oldName;
	protected String newName;
	protected IDocument document;
	
	public RenameTextEdit(ModuleNode rootNode, IDocument document) {
		super(rootNode);
		this.document = document;
	}
	public RenameTextEdit(IGroovyDocumentProvider docProvider, String oldName, String newName) {
		super(docProvider.getRootNode());
		this.document = docProvider.getDocument();
		this.newName = newName;
		this.oldName = oldName;
	}
	
	public ModuleNode getRootNode() {
		return this.rootNode; 
	}

	public MultiTextEdit getEdits() {
		return edits;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

}