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

package org.codehaus.groovy.eclipse.refactoring.core.documentProvider;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.eclipse.refactoring.core.GroovySourceFileVisitor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * class returns a collection of all groovy source files in 
 * the current workspace
 * 
 * @author reto kleeb
 */
public class WorkspaceFileProvider implements IGroovyFileProvider{
	
	private LinkedList<IGroovyDocumentProvider> documentList;
	private IProject groovyProject;
	private WorkspaceDocumentProvider selectionDocument;
	
	public WorkspaceFileProvider(WorkspaceDocumentProvider docProvider) {
		this.selectionDocument = docProvider;
		groovyProject = selectionDocument.getFile().getProject();
		documentList = new LinkedList<IGroovyDocumentProvider>();
	}
	
	public WorkspaceFileProvider(IProject project) {
		groovyProject = project;
		documentList = new LinkedList<IGroovyDocumentProvider>();
		getAllSourceFiles();
	}
	
	public WorkspaceFileProvider(IProject project,
			WorkspaceDocumentProvider docProvider) {
		groovyProject = project;
		documentList = new LinkedList<IGroovyDocumentProvider>();
		selectionDocument = docProvider;
	}

	public List<IGroovyDocumentProvider> getAllSourceFiles(){
		if (documentList.isEmpty()) {
			List<IFile> groovySourceFiles = new GroovySourceFileVisitor(groovyProject).getGroovySourceFiles();
			for(IFile source : groovySourceFiles){
				documentList.add(new GroovyCompilationUnitDocumentProvider((GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(source)));
//				documentList.add(new WorkspaceDocumentProvider(source));
				if (selectionDocument == null) {
//				    selectionDocument = new WorkspaceDocumentProvider(source);
				}
			}
		}
		return documentList;
	}

	public IProject getProject() {
		return groovyProject;
	}

	public IGroovyDocumentProvider getSelectionDocument() {
		return selectionDocument;
	}
}
