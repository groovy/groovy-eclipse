/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package jdtIntegration.renameMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jdtIntegration.JDTRefactoring;
import jdtIntegration.RenameTestCase;

import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.helper.JavaModelSearch;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;

/**
 * @author Stefan Sidler
 * 
 */
public class RenameMethod_EclipseTestCase extends RenameTestCase{

	public RenameMethod_EclipseTestCase(String name, File fileToTest) {
		super(name, fileToTest);
	}
	
	protected void localJavaRefactoring(IMember element) {
		IMethod method = (IMethod)element;
		RefactoringContribution contrib = RefactoringCore
				.getRefactoringContribution(IJavaRefactorings.RENAME_METHOD);
		
		String newName = properties.get("newMethodName");
		JDTRefactoring.refactor(contrib, method, newName);
	}
	
	protected IMember searchVarDeclaration() throws Exception {
		if (properties.get("MethodName")!= null) {
			String returnType = properties.get("MethodName").split(" ")[0];
			String methodName = properties.get("MethodName").split(" ")[1].split("\\(")[0];
			String[] arguments = getArgumentList(properties.get("MethodName"));
		
			SearchPattern pattern = SearchPattern.createPattern(methodName, IJavaSearchConstants.METHOD,
					IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);

			JavaModelSearch methodSearch = new JavaModelSearch(testProject.getJavaProject(), pattern);
			List<IMethod> methodList = methodSearch.searchAll(IMethod.class);

			filter(methodList, returnType, arguments);
			
			if (methodList.size()>1) {
				fail("Too many elements found\n\n");
			}
			return methodList.get(0);
		} else {
			return null;
		}
		
	}
	
	private void filter(List<IMethod> methodList, String returnType, String[] arguments) {
		List<IMethod> removeList = new ArrayList<IMethod>();
		for (IMethod method : methodList) {
			if (!filterSourceFile(method) ||
				!filterReturnType(method, returnType) ||
				!filterArguments(method, arguments))
					removeList.add(method);
		}
		methodList.removeAll(removeList);
	}
	
	private boolean filterSourceFile(IMethod method) {
		String fileName = method.getParent().getElementName();
		if (!(fileName.equals(properties.get("selectionInFile"))
				|| fileName.equals(properties.get("selectionInFile").split("\\.")[0]))) {
			return false;
		}
		return true;
	}

	private boolean filterReturnType(IMethod method, String returnType){
			try {
				String fullName = getFullName(method.getReturnType());
				
				if (!fullName.equals(returnType))
					return false;
		
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		return true;
	}

	private String getFullName(String signature){
		String packageName = Signature.getSignatureQualifier(signature);
		String fullName = (packageName.trim().equals("")?"":packageName+".") + Signature.getSignatureSimpleName(signature);
		return fullName;
	}
	
	private boolean filterArguments(IMethod method, String[] arguments){
		return areTheSame(method.getParameterTypes(),arguments);
	}

	private boolean areTheSame(String[] methodArg, String[] fileArg) {
		boolean result = true;
		if (methodArg.length != fileArg.length)
			return false;
		
		for (int i = 0; i<methodArg.length; i++) {
			String fullName = getFullName(methodArg[i]);
			
			if (!fullName.equals(fileArg[i])) 
				result = false;
		}
		
		
		return result;
	}

	private String[] getArgumentList(String signature) {
		
		String arguments = signature.split("\\(")[1];
		if (arguments.length() == 1) {
			String[] a = {};
			return a;
		}
		arguments = arguments.split("\\)")[0];			   
		String[] argArray = arguments.split(",");
		for (int i=0;i<argArray.length;i++) {
			argArray[i] = argArray[i].trim();
		}
		return argArray;
	}

//	@Override
//	public void startElementRefactoring() throws Exception {
//		IMethod renameMethod = searchMethDeclaration();		
//  		if (renameMethod != null) {
//  			// Local Java Refactoring
//  			localJavaMethodRefactoring(renameMethod);
//  		} else {
//  			if (properties.get("selectionInFile").endsWith("java")) {
//  				// Remote Groovy Refactoring
//  				remoteGroovyRefactoring(properties.get("newMethodName"));
//  			} else {
//  				// Local Groovy and Remote Java Refactoring
//  				localGroovy_remoteJavaRefactoring(properties.get("newMethodName"));	
//  			}
//  		}
//	}

	@Override
	protected String getNewName() {
		return properties.get("newMethodName");
	}
}