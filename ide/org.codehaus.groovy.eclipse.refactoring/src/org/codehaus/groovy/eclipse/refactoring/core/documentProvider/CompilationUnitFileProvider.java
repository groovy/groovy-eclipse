/* 
 * Copyright (C) 2007, 2008 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.documentProvider;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.eclipse.refactoring.core.GroovySourceFileVisitor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author andrew
 *
 */
public class CompilationUnitFileProvider implements
        IGroovyFileProvider {

    private final IJavaProject jProject;
    private GroovyCompilationUnitDocumentProvider selectionDocument;
    private LinkedList<IGroovyDocumentProvider> documentList;

    
    public CompilationUnitFileProvider(GroovyCompilationUnitDocumentProvider docProvider) {
        this.selectionDocument = docProvider;
        this.jProject = selectionDocument.getUnit().getJavaProject();
        this.documentList = new LinkedList<IGroovyDocumentProvider>();
    }
    
    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider#getAllSourceFiles()
     */
    public List<IGroovyDocumentProvider> getAllSourceFiles() {
        if (documentList.isEmpty()) {
            List<IFile> groovySourceFiles = new GroovySourceFileVisitor(getProject()).getGroovySourceFiles();
            for(IFile source : groovySourceFiles){
                documentList.add(new GroovyCompilationUnitDocumentProvider((GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(source)));
            }
        }
        return documentList;
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider#getProject()
     */
    public IProject getProject() {
        return jProject.getProject();
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider#getSelectionDocument()
     */
    public IGroovyDocumentProvider getSelectionDocument() {
        return selectionDocument;
    }

}
