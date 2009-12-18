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
package org.codehaus.groovy.eclipse.refactoring.core.documentProvider;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * @author andrew
 *
 */
public class GroovyCompilationUnitDocumentProvider implements
        IGroovyDocumentProvider {

    private final GroovyCompilationUnit unit;
    
    public GroovyCompilationUnitDocumentProvider(GroovyCompilationUnit unit) {
        this.unit = unit;
    }
    
    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider#fileExists()
     */
    public boolean fileExists() {
        return unit.getResource().exists();
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider#getDocument()
     */
    public IDocument getDocument() {
        return new Document(getDocumentContent());
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider#getDocumentContent()
     */
    public String getDocumentContent() {
        return new String(unit.getContents());
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider#getFile()
     */
    public IFile getFile() {
        return (IFile) unit.getResource();
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider#getName()
     */
    public String getName() {
        return unit.getElementName();
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider#getRootNode()
     */
    public ModuleNode getRootNode() {
        return unit.getModuleNode();
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider#isReadOnly()
     */
    public boolean isReadOnly() {
        return unit.isReadOnly();
    }

    
    public GroovyCompilationUnit getUnit() {
        return unit;
    }
}
