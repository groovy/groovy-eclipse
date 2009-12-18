/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings;

import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.collector.MethodCollector;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.collector.SimpleNameCollector;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod.RenameMethodProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.MethodPattern;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Replaces all inherited method names and method calls
 * 
 * @author Stefan Reinhard
 */
public class MethodUpdateRefactoring extends ASTModificationRefactoring {
	
	private RenameMethodProvider provider;
	private MethodPattern pattern;

	public MethodUpdateRefactoring(RenameMethodProvider provider) {
		super(provider.getFileProvider().getProject());
		this.provider = provider;
		pattern = provider.getMethodPattern();
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) 
	throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if (pattern == null) {
			status.addFatalError("Method Pattern was null");
		}
		return status;
	}

	@Override
	protected SearchPattern getSearchPattern() {
		String declaringClass = pattern.getClassType().getName();
		String methodName = declaringClass+"."+pattern.getMethodName();
		return SearchPattern.createPattern(methodName, 
			IJavaSearchConstants.METHOD,
			IJavaSearchConstants.ALL_OCCURRENCES, 
			SearchPattern.R_EXACT_MATCH);
	}
	
	@Override
    protected SimpleNameCollector getCollector() {
		return new MethodCollector(pattern);
	}
	
	@Override
    protected String getNewName(SimpleName oldName) {
		return provider.getNewName();
	}
	
	@Override
	public String getName() {
		return GroovyRefactoringMessages.JavaMethodUpdateRefactoring;
	}

}
