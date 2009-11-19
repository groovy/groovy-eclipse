/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.groovyRefactorings;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.WorkspaceFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.FieldDefinitionCollector;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod.RenameMethodProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.StringUtils;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.FieldPattern;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.MethodPattern;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author Stefan Reinhard
 */
public class RenameRefactoringConverter {
	
	public static GroovyRefactoring createRefactoring(IJavaElement element) {
		RefactoringProvider provider = createProvider(element);
		RenameInfo info = new RenameInfo(provider);
		return new RenameRefactoring(info, "Groovy Refactoring");
	}
	
	public static RefactoringProvider createProvider(IJavaElement element) {
		if (element instanceof IType) {
			return ClassRenameConverter.createProvider((IType)element);
		} else if (element instanceof IField) {
			return FieldRenameConverter.createProvider((IField)element);
		} else if (element instanceof IMethod) {
			return dispatchMethod(element);
		} else {
			return null;
		}
	}

	private static RefactoringProvider dispatchMethod(IJavaElement element) {
		IMethod method = (IMethod)element;
		RenameMethodProvider provider = MethodRenameConverter.createProvider(method);
		try {
			if (method.isConstructor()) {
				return ClassRenameConverter.createProvider(method.getDeclaringType());
			} else if (isGetterOrSetter(provider)) {
				return checkGetterSetterMethod(method, provider);
			}
		} catch (JavaModelException e) {}
		return provider;
	}

	private static boolean isGetterOrSetter(RenameMethodProvider provider) {
		MethodPattern p = provider.getMethodPattern();
		return (p.getMethodName().contains("set") ||
				p.getMethodName().contains("get")) &&
					!provider.hasCandidates();
	}

	private static RefactoringProvider checkGetterSetterMethod(IMethod method, RenameMethodProvider provider) {
		String capitalFieldName = method.getElementName().substring(3);
		String normalFieldName = StringUtils.uncapitalize(capitalFieldName);
		ClassNode declaring = provider.getMethodPattern().getClassType();
		WorkspaceFileProvider fileProvider = new WorkspaceFileProvider(method.getResource().getProject());
		FieldPattern capitalFieldPattern = searchField(capitalFieldName, declaring, fileProvider);
		FieldPattern normalFieldPattern = searchField(normalFieldName, declaring, fileProvider);
		if (capitalFieldPattern != null) return new RenameFieldProvider(fileProvider, capitalFieldPattern);
		if (normalFieldPattern != null) return new RenameFieldProvider(fileProvider, normalFieldPattern);
		return provider;
	}
	
	private static FieldPattern searchField(String fieldName, ClassNode declaring, WorkspaceFileProvider fileProvider) {
		FieldPattern fieldPattern = new FieldPattern(declaring,fieldName);
		List<FieldNode> fieldList = searchFieldDefinitons(fileProvider, fieldPattern);
		if (fieldList.size() == 1) {
			return new FieldPattern(fieldList.get(0));
		} else {
			return null;
		}
	}
	
	private static List<FieldNode> searchFieldDefinitons(IGroovyFileProvider files, FieldPattern pattern) {
		List<FieldNode> fieldList = new LinkedList<FieldNode>();
		for (IGroovyDocumentProvider d : files.getAllSourceFiles()) {
			FieldDefinitionCollector collector = new FieldDefinitionCollector(d.getRootNode(), pattern);
			collector.scanAST();
			fieldList.addAll(collector.getFieldDefinitions());
		}
		return fieldList;
	}
	
	protected static IGroovyFileProvider getFileProvider(IJavaElement element) {
		IProject sourceProject = element.getResource().getProject();
		return new WorkspaceFileProvider(sourceProject);
	}

}
