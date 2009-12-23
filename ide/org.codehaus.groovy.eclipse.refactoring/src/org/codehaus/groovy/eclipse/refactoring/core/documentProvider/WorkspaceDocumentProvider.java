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

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;


/**
 * Provides a Document in a Eclipse Workspace
 * 
 * @author Michael Klenk mklenk@hsr.ch
 * 
 */
public class WorkspaceDocumentProvider implements IGroovyDocumentProvider {

	private IFile file;
	private ModuleNode rootNode;

	public WorkspaceDocumentProvider(IFile file) {
		this.file = file;
	}

	public String getDocumentContent() {
		StringBuilder out = new StringBuilder();
		try {
    		InputStream in = file.getContents();
    		try {
    			byte[] b = new byte[4096];
    			for (int n; (n = in.read(b)) != -1;) {
    				out.append(new String(b, 0, n));
    			}
    		} catch (IOException e) {
                GroovyCore.logException(e.getMessage(), e);
    		} finally {
    		    try {
                    in.close();
                } catch (IOException e) {
                }
    		}
		} catch (CoreException e) {
            GroovyCore.logException(e.getMessage(), e);
		}
		return out.toString();
	}

	public ModuleNode getRootNode() {
		if (rootNode == null) {
		    ICompilationUnit unit = getUnit();
		    if (unit instanceof GroovyCompilationUnit) {
		        rootNode = ((GroovyCompilationUnit) unit) .getModuleNode();
		    }
		}
		return rootNode;
	}

    /**
     * @return
     */
    public GroovyCompilationUnit getUnit() {
        return (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
    }

	public IDocument getDocument() {
		return new Document(getDocumentContent());
	}

	public IFile getFile() {
		return file;
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider#getTargetFile()
	 */
	public IFile getTargetFile() {
	    return file;
	}

	public boolean fileExists() {
		return file.exists();
	}

	public String getName() {
		return file.getName();
	}

	public boolean isReadOnly() {
		return file.isReadOnly();
	}
}