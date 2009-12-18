/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings;

import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.collector.FieldCollector;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.collector.SimpleNameCollector;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.FieldPattern;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Replaces field access expressions for normal fields
 * 
 * @author Stefan Reinhard
 */
public class FieldUpdateRefactoring extends ASTModificationRefactoring {
	
	private RenameFieldProvider provider;
	private FieldPattern fieldPattern;
	
	public FieldUpdateRefactoring(RenameFieldProvider provider) {
		super(provider.getFileProvider().getProject());
		this.provider = provider;
		fieldPattern = provider.getFieldPattern();
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) 
	throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if (fieldPattern == null) {
			status.addFatalError("FieldPattern not provided");
		}
		return status;
	}
	
	@Override
	protected SearchPattern getSearchPattern() {
		return SearchPattern.createPattern(fieldPattern.getFullyQualifiedName(), 
				IJavaSearchConstants.FIELD,
				IJavaSearchConstants.REFERENCES, 
				SearchPattern.R_EXACT_MATCH);
	}
	
	@Override
    protected SimpleNameCollector getCollector() {
		return new FieldCollector(fieldPattern);
	}
	
	@Override
    protected String getNewName(SimpleName oldName) {
		return provider.getNewName();
	}
	
	@Override
	public String getName() {
		return GroovyRefactoringMessages.JavaFieldUpdateRefactoring;
	}

}
