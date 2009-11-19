/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings;

import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.collector.MutatorCollector;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.collector.SimpleNameCollector;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.StringUtils;
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
 * Replaces synthetic getter and setter methods for dynamic fields
 * 
 * @author Stefan Reinhard
 */
public class MutatorUpdateRefactoring extends ASTModificationRefactoring {
	
	private RenameFieldProvider provider;
	private FieldPattern fieldPattern;
	
	public MutatorUpdateRefactoring(RenameFieldProvider provider) {
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
		return SearchPattern.createPattern("*"+fieldPattern.getName(), 
				IJavaSearchConstants.METHOD,
				IJavaSearchConstants.REFERENCES, 
				SearchPattern.R_PATTERN_MATCH);
	}
	
	protected SimpleNameCollector getCollector() {
		String className = fieldPattern.getDeclaringClass().getName();
		String fieldName = fieldPattern.getName();
		return new MutatorCollector(className, fieldName);
	}
	
	protected String getNewName(SimpleName oldName) {
		String prefix = oldName.getIdentifier().substring(0, 3);
		return prefix + StringUtils.capitalize(provider.getNewName());
	}
	
	@Override
	public String getName() {
		return GroovyRefactoringMessages.JavaSyntheticFieldUpdateRefactoring;
	}

}
