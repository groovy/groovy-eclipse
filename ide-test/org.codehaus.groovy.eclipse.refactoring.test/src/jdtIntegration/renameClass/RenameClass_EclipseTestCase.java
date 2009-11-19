/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration.renameClass;

import java.io.File;

import jdtIntegration.JDTRefactoring;
import jdtIntegration.RenameTestCase;

import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.helper.JavaModelSearch;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;

/**
 * @author Stefan Sidler
 */
public class RenameClass_EclipseTestCase extends RenameTestCase {

	public RenameClass_EclipseTestCase(String name, File fileToTest) {
		super(name, fileToTest);
	}

	protected void localJavaRefactoring(IMember element) {
		IType clazz = (IType)element;
		RefactoringContribution contrib = RefactoringCore
				.getRefactoringContribution(IJavaRefactorings.RENAME_TYPE);
		String newName = properties.get("newClassName");
		JDTRefactoring.refactor(contrib, clazz, newName);
	}

	protected IMember searchVarDeclaration() throws Exception {
		if (properties.get("className")!= null) {
			SearchPattern sp = SearchPattern.createPattern(properties
					.get("className"), IJavaSearchConstants.TYPE,
					IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		
			JavaModelSearch search = new JavaModelSearch(testProject.getJavaProject(), sp);
			IType type = search.searchFirst(IType.class);
			if (type != null) {
				return type;
			} else {
				throw new Exception("Element not found");
			}
		} else {
			return null;
		}
	}


	@Override
	protected String getNewName() {
		return properties.get("newClassName");
	}

}
