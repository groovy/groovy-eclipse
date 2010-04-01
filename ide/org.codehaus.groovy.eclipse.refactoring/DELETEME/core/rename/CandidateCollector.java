/* 
 * Copyright (C) 2007, 2008 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.rename;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.ITextSelection;

/**
 * Collects Groovy and Java Candidates
 * @author Stefan Reinhard
 *
 */
public class CandidateCollector {
	
	protected ITextSelection selection;
	protected GroovyCompilationUnit unit;
	
	protected ISourceReference refactoringTarget;
	private boolean isValid = true;
	
	public CandidateCollector(GroovyCompilationUnit unit, ITextSelection selection) {
		this.unit = unit;
		this.selection = selection;
	}
	
	
	/**
	 * The selected node.  May be null if the selection is not a valid 
	 * refactoring target
	 * @return
	 */
    public ISourceReference getRefactoringTarget() {
        if (isValid && refactoringTarget == null) {
            try {
                IJavaElement[] element = unit.codeSelect(selection.getOffset(), selection.getLength());
                if (element != null && element.length > 0 && element[0] instanceof ISourceReference) {
                    refactoringTarget = (ISourceReference) element[0];
                } else {
                    isValid = false;
                }
            } catch (JavaModelException e) {
                GroovyCore.logException("Exception finding element at offset " + 
                        selection.getOffset() + " in compilation unit " + unit.getElementName(), e);
            }
        }
        return refactoringTarget;
    }
}
