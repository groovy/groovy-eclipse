/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.collector.ClassColletor;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.collector.SimpleNameCollector;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass.RenameClassProvider;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Replaces all refactored Groovy Classnames
 * 
 * @author Stefan Reinhard
 */
public class ClassUpdateRefactoring extends ASTModificationRefactoring {

	protected RenameClassProvider provider;
	protected ClassNode oldClass;
	
	public ClassUpdateRefactoring(RenameClassProvider provider) {
		super(provider.getFileProvider().getProject());
		this.provider = provider;
		oldClass = provider.getSelectedNode();
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
	throws CoreException, OperationCanceledException {
			RefactoringStatus status = new RefactoringStatus();
			if (oldClass == null) {
				status.addFatalError("ClassNode not provided");
			}
			return status;
	}

	@Override
    protected SimpleNameCollector getCollector() {
		return new ClassColletor(oldClass);
	}
	
	@Override
    protected String getNewName(SimpleName oldName) {
		return provider.getNewName();
	}

	@Override
	protected SearchPattern getSearchPattern() {
		return SearchPattern.createPattern(oldClass.getName(), 
				IJavaSearchConstants.TYPE,
				IJavaSearchConstants.REFERENCES, 
				SearchPattern.R_EXACT_MATCH);
	}
	
	@Override
	public String getName() {
		return GroovyRefactoringMessages.JavaClassUpdateRefactoring;
	}
	
}
