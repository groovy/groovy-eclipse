/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration.renameField;

import java.io.File;

import jdtIntegration.JDTRefactoring;
import jdtIntegration.RenameTestCase;

import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.helper.JavaModelSearch;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;

/**
 * @author Stefan Sidler
 * 
 */
public class RenameField_EclipseTestCase extends RenameTestCase {
	
	public RenameField_EclipseTestCase(String name, File fileToTest) {
		super(name, fileToTest);		
	}

	protected void localJavaRefactoring(IMember element) {
		IField field = (IField)element;
		RefactoringContribution contrib = RefactoringCore
				.getRefactoringContribution(IJavaRefactorings.RENAME_FIELD);
		
		String newName = properties.get("newFieldName");
		
		JDTRefactoring.refactor(contrib, field, newName);		
	}

	protected IMember searchVarDeclaration() throws Exception {
		if (properties.get("fieldName")!= null) {
			SearchPattern pattern = SearchPattern.createPattern(properties
					.get("fieldName"), IJavaSearchConstants.FIELD,
					IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);

			JavaModelSearch search = new JavaModelSearch(testProject.getJavaProject(), pattern);
			IField field = search.searchFirst(IField.class);
		
			if (field != null) {
				return field;
			} else {
				throw new Exception("Element not found");
			}
		} else {
			return null;
		}
	}

	@Override
	protected String getNewName() {
		return properties.get("newFieldName");
	}
}