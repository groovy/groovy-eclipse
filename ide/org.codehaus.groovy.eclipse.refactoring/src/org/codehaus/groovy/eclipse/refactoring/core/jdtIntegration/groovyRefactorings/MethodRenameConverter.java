/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.groovyRefactorings;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.helper.HierarchyBuilder;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.helper.TypeResolver;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod.RenameMethodProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.MethodPattern;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author Stefan Reinhard
 */
public class MethodRenameConverter {
	
	/**
	 * Creates a <code>RenameMethodProvider</code> for a IMethod
	 * @param renamed
	 */
	public static RenameMethodProvider createProvider(IMethod renamed) {
		IGroovyFileProvider fileProvider = RenameRefactoringConverter.getFileProvider(renamed);
		MethodPattern methodPattern = createMethodPattern(renamed);
		RenameMethodProvider provider = new RenameMethodProvider(fileProvider, methodPattern);
		return provider;
	}

	private static MethodPattern createMethodPattern(IMethod renamed) {
		IType declaringType = renamed.getDeclaringType();
		String className = declaringType.getFullyQualifiedName();
		ClassNode declaringClass = ClassHelper.makeWithoutCaching(className);
		HierarchyBuilder.addHierarchyToType(declaringType, declaringClass);
		MethodNode methodNode = createMethodNode(renamed, declaringType);
		methodNode.setDeclaringClass(declaringClass);
		MethodPattern methodPattern = new MethodPattern(methodNode, declaringClass);
		return methodPattern;
	}

	private static MethodNode createMethodNode(IMethod renamed, IType declaringType) {
		String methodName = renamed.getElementName();
		Parameter[] parameters = createParameterList(renamed);
		MethodNode methodNode = null;
		try {
			String returnTypeName = TypeResolver.getFQN(renamed.getReturnType(),declaringType);
			ClassNode returnType = ClassHelper.make(returnTypeName);			
			methodNode = new MethodNode(methodName, renamed.getFlags(), returnType, parameters, null, null);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return methodNode;
	}
	
	private static Parameter[] createParameterList(IMethod renamed) {
		Parameter[] parameters = new Parameter[renamed.getNumberOfParameters()];
		int i = 0;
		for(String parameterType : renamed.getParameterTypes()) {;
				String typeName = TypeResolver.getFQN(parameterType, renamed.getDeclaringType());
				ClassNode paraType = ClassHelper.make(typeName);
				parameters[i++] = new Parameter(paraType, "");
		}
		
		return parameters;
	}

}
