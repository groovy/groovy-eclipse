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

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;

/**
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public interface IGroovyDocumentProvider {
		
	public abstract ModuleNode getRootNode();
	public abstract String getDocumentContent();
	public abstract IDocument getDocument();
	public abstract boolean fileExists();
	public abstract IFile getFile();
	public abstract String getName();
	public abstract boolean isReadOnly();
	public abstract GroovyCompilationUnit getUnit();

}
