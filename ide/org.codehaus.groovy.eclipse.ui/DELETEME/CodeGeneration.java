/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.ui;

import org.codehaus.groovy.eclipse.internal.corext.StubUtility;
import org.codehaus.groovy.eclipse.ui.ArtifactCodeGenerator.IndentationDirection;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;

/**
 * Class that offers access to the templates contained in the 'code templates' preference page.
 * 
 * @since 1.6
 */
public class CodeGeneration {
	
	/**
	 * Returns the content for a new file comment using the 'file comment' code template. The returned content is unformatted and is not indented.
	 * @param project The IJavaProject to use.
	 * @param typeName The name of the type to which the comment is added. For inner types the name must be qualified and include the outer
	 * types names (dot separated). See {@link org.eclipse.jdt.core.IType#getTypeQualifiedName(char)}.
	 * @param packageName The name of the parent package
	 * @return Returns the new content or <code>null</code> if the code template is undefined or empty. The returned content is unformatted and is not indented.
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 */	
	public static String getFileComment(IJavaProject project, String typeName, String packageName) throws CoreException {
		return StubUtility.getFileComment(project, typeName, packageName, getLineDelimiter(project));
	}

	/**
	 * Returns the content for a new type comment using the 'type comment' code template. The returned content is unformatted and is not indented.
	 * @param project The IJavaProject to use.
	 * @param typeQualifiedName The name of the type to which the comment is added. For inner types the name must be qualified and include the outer
	 * types names (dot separated). See {@link org.eclipse.jdt.core.IType#getTypeQualifiedName(char)}.
	 * @param packageName The name of the parent package
	 * @return Returns the new content or <code>null</code> if the code template is undefined or empty. The returned content is unformatted and is not indented.
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 */	
	public static String getTypeComment(IJavaProject project, String typeQualifiedName, String packageName) throws CoreException {
		return StubUtility.getTypeComment(project, typeQualifiedName, packageName, new String[0], getLineDelimiter(project));
	}
	
	/**
	 * Returns the comment for a method or constructor using the comment code templates (constructor / method / overriding method).
	 * <code>null</code> is returned if the template is empty.
	 * <p>The returned string is unformatted and not indented.
	 * 
	 * @param javaProject The IJavaProject to use
	 * @param declaringTypeName Name of the type to which the method belongs. For inner types the name must be qualified and include the outer
	 * types names (dot separated). See {@link org.eclipse.jdt.core.IType#getTypeQualifiedName(char)}.
	 * @param packageName The name of the parent package
	 * @param methodName The name of the method
	 * @param parameterNames An arry of String with the parameter names.
	 * @param exceptionTypeSigs An array of Strings with the signatures of the exception the method can throw
	 * @param returnTypeSig The signature of the return type
	 * @param target The IMethod will be overwritten of this method
	 * @param delegate Indicates that a delegation pattern should use
	 * @return Returns the constructed comment or <code>null</code> if
	 * the comment code template is empty. The returned string is unformatted and and has no indent (formatting required).
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 */
	public static String getMethodComment(IJavaProject javaProject, String declaringTypeName, String packageName, String methodName, String[] parameterNames, String[] exceptionTypeSigs, String returnTypeSig, IMethod target, boolean delegate) throws CoreException {
		return StubUtility.getMethodComment(javaProject, declaringTypeName, packageName, methodName, parameterNames, exceptionTypeSigs, returnTypeSig, new String[0], target, delegate, getLineDelimiter(javaProject));
	}
	
	/**
	 * Returns the comment for a method or constructor using the comment code templates (constructor / method / overriding method).
	 * <code>null</code> is returned if the template is empty.
	 * <p>The returned string is unformatted and not indented.
	 * 
	 * @param javaProject The IJavaProject to use
	 * @param declaringTypeName Name of the type to which the method belongs. For inner types the name must be qualified and include the outer
	 * types names (dot separated). See {@link org.eclipse.jdt.core.IType#getTypeQualifiedName(char)}.
	 * @param packageName The name of the parent package
	 * @param methodName The name of the method
	 * @param parameterNames An arry of String with the parameter names.
	 * @param returnTypeSig The signature of the return type
	 * @return Returns the constructed comment or <code>null</code> if
	 * the comment code template is empty. The returned string is unformatted and and has no indent (formatting required).
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 */
	public static String getMethodComment(IJavaProject javaProject, String declaringTypeName, String packageName, String methodName, String[] parameterNames, String returnTypeSig) throws CoreException {
		return StubUtility.getMethodComment(javaProject, declaringTypeName, packageName, methodName, parameterNames, new String[0], returnTypeSig, new String[0], null, false, getLineDelimiter(javaProject));
	}
	
	/**
	 * Returns the comment for a overridden method  using the comment code templates (constructor / method / overriding method).
	 * <code>null</code> is returned if the template is empty.
	 * <p>The returned string is unformatted and not indented.
	 * 
	 * @param javaProject The IJavaProject to use
	 * @param declaringTypeName Name of the type to which the method belongs. For inner types the name must be qualified and include the outer
	 * types names (dot separated). See {@link org.eclipse.jdt.core.IType#getTypeQualifiedName(char)}.
	 * @param packageName The name of the parent package
	 * @param methodName The name of the method
	 * @param parameterNames An arry of String with the parameter names.
	 * @param returnTypeSig The signature of the return type
	 * @return Returns the constructed comment or <code>null</code> if
	 * the comment code template is empty. The returned string is unformatted and and has no indent (formatting required).
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 */
	public static String getOverriddenMethodComment(IJavaProject javaProject, String declaringTypeName, String packageName, String methodName, String[] parameterNames, String returnTypeSig, IMethod target) throws CoreException {
		return StubUtility.getMethodComment(javaProject, declaringTypeName, packageName, methodName, parameterNames, new String[0], returnTypeSig, new String[0], target, false, getLineDelimiter(javaProject));
	}
	
	/**
	 * Returns the content of the body for a method or constructor using the method body templates.
	 * <code>null</code> is returned if the template is empty.
	 * <p>The returned string is unformatted and not indented.
	 * 
	 * @param javaProject The IJavaProject to which the method belongs. 
	 * @param declaringTypeName Name of the type to which the method belongs. For inner types the name must be qualified and include the outer
	 * types names (dot separated). See {@link org.eclipse.jdt.core.IType#getTypeQualifiedName(char)}.
	 * @param methodName Name of the method.
	 * @param isConstructor Defines if the created body is for a constructor.
	 * @param bodyStatement The code to be entered at the place of the variable ${body_statement}. 
	 * @return Returns the constructed body content or <code>null</code> if
	 * the comment code template is empty. The returned string is unformatted and and has no indent (formatting required).
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 */	
	public static String getMethodBodyContent(IJavaProject javaProject, String declaringTypeName, String methodName, boolean isConstructor, String bodyStatement) throws CoreException {
		return StubUtility.getMethodBodyContent(isConstructor, javaProject, declaringTypeName, methodName, bodyStatement, getLineDelimiter(javaProject));
	}
	
	/**
	 * @param project The current <code>IJavaProject</code>
	 * @return The String used for indentation (e.g. spaces of tab)
	 */
	public static  String getIndentation(IJavaProject project){
		return StubUtility.getIndentation(project);
	}
	
	/**
	 * @param project The current IJavaProject
	 * @return The linedelimiter. Taken from the Workspace or Project preferences
	 */
	public static String getLineDelimiter(IJavaProject project) {
		return StubUtility.getLineDelimiterUsed(project);
	}
	
	/**
	 * @param method The <code>IMethod</code> to get the parameter types from
	 * @return The fully qualified typenames of the parameters of the given <code>IMethod</code>
	 */
	public static String[] getParameterTypeNames(IMethod method) {
		return StubUtility.getParameterTypeNames(method);
	}
	
	/**
	 * 
	 * @param method The <code>IMethod</code> to get the returntype from
	 * @return The fully qualified name of the return type
	 * @throws CoreException
	 */
	public static String getReturnType(IMethod method) throws CoreException{
		return StubUtility.getReturnType(method);
	}
	
	public static void handleInheritedMethod(IJavaProject project, IMethod method, ArtifactCodeGenerator codeGenerator, ImportManager importManager, String typeName, String packageName) throws CoreException{
		String[] parameters = method.getParameterNames();
		String[] parameterTypes = CodeGeneration.getParameterTypeNames(method);
		String returnTypeName = CodeGeneration.getReturnType(method);
		String codeline = "";
		
		importManager.addImport(returnTypeName);
		
		codeGenerator
			.addCode(CodeGeneration.getOverriddenMethodComment(project, typeName, packageName, method.getElementName(), parameters, method.getReturnType(), method));
		
		codeline = "public "+ ((method.isConstructor())? "": importManager.toSimpleName(returnTypeName) + " ")+
			(method.isConstructor()? typeName: method.getElementName())+"(";

		for (int j = 0; j < parameterTypes.length; j++) {
			String pType = parameterTypes[j];
			String pName = parameters[j];
			
			importManager.addImport(pType);
			codeline += importManager.toSimpleName(pType)+" "+pName;
			if (j<method.getParameterTypes().length-1){
				codeline += ", ";
			}
		}
		codeline += "){";
	
		codeGenerator
			.addCode(codeline)
			.addCode(IndentationDirection.INDENT_RIGHT, CodeGeneration.getMethodBodyContent(project, typeName, method.getElementName(), false, ""));
			
		if (!returnTypeName.equals("void")){				
			if (importManager.isPrimitive(returnTypeName)){
				codeGenerator.addCode("return "+importManager.getPrimitiveDefaultValue(returnTypeName));
			}else{
				codeGenerator.addCode("return null");
			}
		}
		
		codeGenerator
			.addCode(IndentationDirection.INDENT_LEFT, "}")
			.addLineBreak();
	}
}
