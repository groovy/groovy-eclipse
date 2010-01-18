/* 
 * Copyright (C) 2007, 2008 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core;

import org.codehaus.groovy.eclipse.refactoring.core.rename.IRenameProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author andrew
 *
 */
public class EmptyRefactoringProvider extends RefactoringProvider implements IRenameProvider {

    private final String oldName;
    private String newName;
    
    /**
     * @param elementName
     */
    public EmptyRefactoringProvider(String oldName) {
        this.oldName = oldName;
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider#addInitialConditionsCheckStatus(org.eclipse.ltk.core.refactoring.RefactoringStatus)
     */
    @Override
    public void addInitialConditionsCheckStatus(RefactoringStatus status) {

    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider#checkFinalConditions(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
            throws CoreException, OperationCanceledException {
        return RefactoringStatus.create(Status.OK_STATUS);
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider#checkInitialConditions(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
            throws CoreException, OperationCanceledException {
        return RefactoringStatus.create(Status.OK_STATUS);
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider#createGroovyChange(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public GroovyChange createGroovyChange(IProgressMonitor pm)
            throws CoreException, OperationCanceledException {
        return new GroovyChange("No Groovy Changes");
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.rename.IRenameProvider#checkUserInput(org.eclipse.ltk.core.refactoring.RefactoringStatus, java.lang.String)
     */
    public void checkUserInput(RefactoringStatus status, String text) {
        
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.rename.IRenameProvider#getNewName()
     */
    public String getNewName() {
        return newName;
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.rename.IRenameProvider#getOldName()
     */
    public String getOldName() {
        return oldName;
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.eclipse.refactoring.core.rename.IRenameProvider#setNewName(java.lang.String)
     */
    public void setNewName(String newName) {
        this.newName = newName;
    }

}
