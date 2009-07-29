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
/**
 * 
 */
package org.codehaus.groovy.eclipse.internal.corext;

import java.io.IOException;
import java.util.Set;

import org.codehaus.groovy.eclipse.core.util.SetUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContext;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaUIStatus;
import org.eclipse.jdt.internal.ui.viewsupport.ProjectTemplateStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;

/**
 * @author Thorsten Kamann <thorsten.kamann@googlemail.com>
 *
 */
public class StubUtility {
	private static final String[] EMPTY= new String[0];
	
	/**
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.codehaus.groovy.eclipse.ui.CodeGeneration#getFileComment(IJavaProject, String, String)
	 */	
	public static String getFileComment(IJavaProject project, String typeName, String packageName, String lineDelimiter) throws CoreException {
		Template template= getCodeTemplate(CodeTemplateContextType.FILECOMMENT_ID, project);
		if (template == null) {
			return null;
		}

		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), project, lineDelimiter);
		setCompilationUnitVariables(context, typeName, packageName, project.getElementName());
		context.setVariable(CodeTemplateContextType.TYPENAME, typeName);
		return evaluateTemplate(context, template);
	}	
	
	/**
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.codehaus.groovy.eclipse.ui.CodeGeneration#getTypeComment(IJavaProject, String, String, String)
	 */		
	public static String getTypeComment(IJavaProject project, String typeQualifiedName, String packageName, String[] typeParameterNames, String lineDelim) throws CoreException {
		Template template= getCodeTemplate(CodeTemplateContextType.TYPECOMMENT_ID, project);
		if (template == null) {
			return null;
		}
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), project, lineDelim);
		setCompilationUnitVariables(context, typeQualifiedName, packageName, project.getElementName());
		context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, Signature.getQualifier(typeQualifiedName));
		context.setVariable(CodeTemplateContextType.TYPENAME, Signature.getSimpleName(typeQualifiedName));

		TemplateBuffer buffer;
		try {
			buffer= context.evaluate(template);
		} catch (BadLocationException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (TemplateException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		String str= buffer.getString();
		if (Strings.containsOnlyWhitespaces(str)) {
			return null;
		}
		
		TemplateVariable position= findVariable(buffer, CodeTemplateContextType.TAGS); // look if Javadoc tags have to be added
		if (position == null) {
			return str;
		}
		
		IDocument document= new Document(str);
		int[] tagOffsets= position.getOffsets();
		for (int i= tagOffsets.length - 1; i >= 0; i--) { // from last to first
			try {
				insertTag(document, tagOffsets[i], position.getLength(), EMPTY, EMPTY, null, typeParameterNames, false, lineDelim);
			} catch (BadLocationException e) {
				throw new CoreException(JavaUIStatus.createError(IStatus.ERROR, e));
			}
		}
		return document.get();
	}
	
	/**
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.codehaus.groovy.eclipse.ui.CodeGeneration#getTypeComment(IJavaProject, String, String, String, String[], String[], String, IMethod, boolean, String)
	 * @see org.codehaus.groovy.eclipse.ui.CodeGeneration#getTypeComment(IJavaProject, String, String, String, String[], String, String)
	 */	
	public static String getMethodComment(IJavaProject javaProject, String typeName, String packageName, String methodName, String[] paramNames, String[] excTypeSig, String retTypeSig, String[] typeParameterNames, IMethod target, boolean delegate, String lineDelimiter) throws CoreException {
		String templateName= CodeTemplateContextType.METHODCOMMENT_ID;
		if (retTypeSig == null) {
			templateName= CodeTemplateContextType.CONSTRUCTORCOMMENT_ID;
		} else if (target != null) {
			if (delegate)
				templateName= CodeTemplateContextType.DELEGATECOMMENT_ID;
			else
				templateName= CodeTemplateContextType.OVERRIDECOMMENT_ID;
		}
		Template template= getCodeTemplate(templateName, javaProject);
		if (template == null) {
			return null;
		}		
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), javaProject, lineDelimiter);
		setCompilationUnitVariables(context, typeName, packageName, javaProject.getElementName());
		context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, typeName);
		context.setVariable(CodeTemplateContextType.ENCLOSING_METHOD, methodName);
				
		if (retTypeSig != null) {
			context.setVariable(CodeTemplateContextType.RETURN_TYPE, Signature.toString(retTypeSig));
		}
		if (target != null) {
			String targetTypeName= target.getDeclaringType().getFullyQualifiedName('.');
			String[] targetParamTypeNames= getParameterTypeNamesForSeeTag(target);
			if (delegate)
				context.setVariable(CodeTemplateContextType.SEE_TO_TARGET_TAG, getSeeTag(targetTypeName, methodName, targetParamTypeNames));
			else
				context.setVariable(CodeTemplateContextType.SEE_TO_OVERRIDDEN_TAG, getSeeTag(targetTypeName, methodName, targetParamTypeNames));
		}
		TemplateBuffer buffer;
		try {
			buffer= context.evaluate(template);
		} catch (BadLocationException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (TemplateException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		if (buffer == null) {
			return null;
		}
		
		String str= buffer.getString();
		if (Strings.containsOnlyWhitespaces(str)) {
			return null;
		}
		TemplateVariable position= findVariable(buffer, CodeTemplateContextType.TAGS); // look if Javadoc tags have to be added
		if (position == null) {
			return str;
		}
			
		IDocument document= new Document(str);
		String[] exceptionNames= new String[excTypeSig.length];
		for (int i= 0; i < excTypeSig.length; i++) {
			exceptionNames[i]= Signature.toString(excTypeSig[i]);
		}
		String returnType= retTypeSig != null ? Signature.toString(retTypeSig) : null;
		int[] tagOffsets= position.getOffsets();
		for (int i= tagOffsets.length - 1; i >= 0; i--) { // from last to first
			try {
				insertTag(document, tagOffsets[i], position.getLength(), paramNames, exceptionNames, returnType, typeParameterNames, false, lineDelimiter);
			} catch (BadLocationException e) {
				throw new CoreException(JavaUIStatus.createError(IStatus.ERROR, e));
			}
		}
		return document.get();
	}
	
	/**
	 * Don't use this method directly, use CodeGeneration.
	 * @see org.codehaus.groovy.eclipse.ui.CodeGeneration#getTypeComment(IJavaProject, String, String, boolean, String, String)
	 */	
	public static String getMethodBodyContent(boolean isConstructor, IJavaProject project, String destTypeName, String methodName, String bodyStatement, String lineDelimiter) throws CoreException {
		String templateName= isConstructor ? CodeTemplateContextType.CONSTRUCTORSTUB_ID : CodeTemplateContextType.METHODSTUB_ID;
		Template template= getCodeTemplate(templateName, project);
		if (template == null) {
			return bodyStatement;
		}
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), project, lineDelimiter);
		context.setVariable(CodeTemplateContextType.ENCLOSING_METHOD, methodName);
		context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, destTypeName);
		context.setVariable(CodeTemplateContextType.BODY_STATEMENT, bodyStatement);
		String str= evaluateTemplate(context, template, new String[] { CodeTemplateContextType.BODY_STATEMENT });
		if (str == null && !Strings.containsOnlyWhitespaces(bodyStatement)) {
			return bodyStatement;
		}
		return str;
	}

	
	/**
	 * @param project The current <code>IJavaProject</code>
	 * @return The String used for indentation (e.g. spaces of tab)
	 */
	public static String getIndentation(IJavaProject project){
		final int tabWidth = CodeFormatterUtil.getTabWidth(project);
		final int indentWidth = CodeFormatterUtil.getIndentWidth(project);
		int spaceEquivalents = Math.min(tabWidth, indentWidth);
		boolean useSpaces = false;
		String formatterTabChar = "tab";
		if (project == null){
			formatterTabChar = JavaCore
					.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
		}else{
			formatterTabChar = project.getOption(
					DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, true);
		}

		if (formatterTabChar.equals(JavaCore.SPACE)) {
			useSpaces = true;
		} else if (formatterTabChar.equals(JavaCore.TAB)) {
			useSpaces = false;
		} else {
			String useTabsOnlyForLeadingIndent = "false";
			if (project == null) {
				useTabsOnlyForLeadingIndent = JavaCore
						.getOption(DefaultCodeFormatterConstants.FORMATTER_USE_TABS_ONLY_FOR_LEADING_INDENTATIONS);
			} else {
				useTabsOnlyForLeadingIndent = project
						.getOption(
								DefaultCodeFormatterConstants.FORMATTER_USE_TABS_ONLY_FOR_LEADING_INDENTATIONS,
								true);
			}
			if (useTabsOnlyForLeadingIndent.equalsIgnoreCase("true")) {
				useSpaces = true;
			}
		}
		
		return useSpaces? getSpaceIndentation(spaceEquivalents): "\t";
	}
	
	/**
	 * Returns the line delimiter which is used in the specified project.
	 * 
	 * @param project the java project, or <code>null</code>
	 * @return the used line delimiter
	 */
	public static String getLineDelimiterUsed(IJavaProject project) {
		return getProjectLineDelimiter(project);
	}
	
	/**
	 * @param method The <code>IMethod</code> to get the parameter types from
	 * @return The fully qualified typenames of the parameters of the given <code>IMethod</code>
	 */
	public static String[] getParameterTypeNames(IMethod method) {
		return getParameterTypeNamesForSeeTag(method);
	}
	
	private static String getLineDelimiterPreference(IProject project) {
		IScopeContext[] scopeContext;
		if (project != null) {
			// project preference
			scopeContext= new IScopeContext[] { new ProjectScope(project) };
			String lineDelimiter= Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null, scopeContext);
			if (lineDelimiter != null)
				return lineDelimiter;
		}
		// workspace preference
		scopeContext= new IScopeContext[] { new InstanceScope() };
		String platformDefault= System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, platformDefault, scopeContext);
	}
	
	private static String getProjectLineDelimiter(IJavaProject javaProject) {
		IProject project= null;
		if (javaProject != null)
			project= javaProject.getProject();
		
		String lineDelimiter= getLineDelimiterPreference(project);
		if (lineDelimiter != null)
			return lineDelimiter;
		
		return System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private static String getSpaceIndentation(int size){
		String space = "";
		
		for (int i = 0; i < size; i++) {
			space += " ";
		}
		return space;
	}
	
	private static void setCompilationUnitVariables(CodeTemplateContext context, String fileName, String packageName, String projectName) {
	    context.setVariable(CodeTemplateContextType.FILENAME, (ContentTypeUtils.isGroovyLikeFileName(fileName) ? fileName : fileName + ".groovy"));
		context.setVariable(CodeTemplateContextType.PACKAGENAME, packageName);
		context.setVariable(CodeTemplateContextType.PROJECTNAME, projectName);
	}
	
	private static void insertTag(IDocument textBuffer, int offset, int length, String[] paramNames, String[] exceptionNames, String returnType, String[] typeParameterNames, boolean isDeprecated, String lineDelimiter) throws BadLocationException {
		IRegion region= textBuffer.getLineInformationOfOffset(offset);
		if (region == null) {
			return;
		}
		String lineStart= textBuffer.get(region.getOffset(), offset - region.getOffset());
		
		StringBuffer buf= new StringBuffer();
		for (int i= 0; i < typeParameterNames.length; i++) {
			if (buf.length() > 0) {
				buf.append(lineDelimiter).append(lineStart);
			}
			buf.append("@param <").append(typeParameterNames[i]).append('>'); //$NON-NLS-1$
		}
		for (int i= 0; i < paramNames.length; i++) {
			if (buf.length() > 0) {
				buf.append(lineDelimiter).append(lineStart);
			}
			buf.append("@param ").append(paramNames[i]); //$NON-NLS-1$
		}
		if (returnType != null && !returnType.equals("void")) { //$NON-NLS-1$
			if (buf.length() > 0) {
				buf.append(lineDelimiter).append(lineStart);
			}			
			buf.append("@return"); //$NON-NLS-1$
		}
		if (exceptionNames != null) {
			for (int i= 0; i < exceptionNames.length; i++) {
				if (buf.length() > 0) {
					buf.append(lineDelimiter).append(lineStart);
				}
				buf.append("@throws ").append(exceptionNames[i]); //$NON-NLS-1$
			}
		}		
		if (isDeprecated) {
			if (buf.length() > 0) {
				buf.append(lineDelimiter).append(lineStart);
			}
			buf.append("@deprecated"); //$NON-NLS-1$
		}
		if (buf.length() == 0 && isAllCommentWhitespace(lineStart)) {
			int prevLine= textBuffer.getLineOfOffset(offset) -1;
			if (prevLine > 0) {
				IRegion prevRegion= textBuffer.getLineInformation(prevLine);
				int prevLineEnd= prevRegion.getOffset() + prevRegion.getLength();
				// clear full line
				textBuffer.replace(prevLineEnd, offset + length - prevLineEnd, ""); //$NON-NLS-1$
				return;
			}
		}
		textBuffer.replace(offset, length, buf.toString());
	}
	
	private static boolean isAllCommentWhitespace(String lineStart) {
		for (int i= 0; i < lineStart.length(); i++) {
			char ch= lineStart.charAt(i);
			if (!Character.isWhitespace(ch) && ch != '*') {
				return false;
			}
		}
		return true;
	}
	
	private static TemplateVariable findVariable(TemplateBuffer buffer, String variable) {
		TemplateVariable[] positions= buffer.getVariables();
		for (int i= 0; i < positions.length; i++) {
			TemplateVariable curr= positions[i];
			if (variable.equals(curr.getType())) {
				return curr;
			}
		}
		return null;		
	}	
	
	private static Template getCodeTemplate(String id, IJavaProject project) {
		if (project == null)
			return JavaPlugin.getDefault().getCodeTemplateStore().findTemplateById(id);
		ProjectTemplateStore projectStore= new ProjectTemplateStore(project.getProject());
		try {
			projectStore.load();
		} catch (IOException e) {
			JavaPlugin.log(e);
		}
		return projectStore.findTemplateById(id);
	}
	
	private static String evaluateTemplate(CodeTemplateContext context, Template template) throws CoreException {
		TemplateBuffer buffer;
		try {
			buffer= context.evaluate(template);
		} catch (BadLocationException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (TemplateException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		if (buffer == null)
			return null;
		String str= buffer.getString();
		if (Strings.containsOnlyWhitespaces(str)) {
			return null;
		}
		return str;
	}
	
	private static String evaluateTemplate(CodeTemplateContext context, Template template, String[] fullLineVariables) throws CoreException {
		TemplateBuffer buffer;
		try {
			buffer= context.evaluate(template);
			if (buffer == null)
				return null;
			String str= fixEmptyVariables(buffer, fullLineVariables);
			if (Strings.containsOnlyWhitespaces(str)) {
				return null;
			}
			return str;
		} catch (BadLocationException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (TemplateException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
	}
	
	private static String fixEmptyVariables(TemplateBuffer buffer, String[] variables) throws MalformedTreeException, BadLocationException {
		IDocument doc= new Document(buffer.getString());
		int nLines= doc.getNumberOfLines();
		MultiTextEdit edit= new MultiTextEdit();
		Set< Integer > removedLines = SetUtil.set();
		for (int i= 0; i < variables.length; i++) {
			TemplateVariable position= findVariable(buffer, variables[i]); // look if Javadoc tags have to be added
			if (position == null || position.getLength() > 0) {
				continue;
			}
			int[] offsets= position.getOffsets();
			for (int k= 0; k < offsets.length; k++) {
				int line= doc.getLineOfOffset(offsets[k]);
				IRegion lineInfo= doc.getLineInformation(line);
				int offset= lineInfo.getOffset();
				String str= doc.get(offset, lineInfo.getLength());
				if (Strings.containsOnlyWhitespaces(str) && nLines > line + 1 && removedLines.add(new Integer(line))) {
					int nextStart= doc.getLineOffset(line + 1);
					edit.addChild(new DeleteEdit(offset, nextStart - offset));
				}
			}
		}
		edit.apply(doc, 0);
		return doc.get();
	}
	
	
	public static String getReturnType(IMethod method) throws CoreException{
		try {
			ASTParser parser= ASTParser.newParser(AST.JLS3);
			parser.setProject(method.getJavaProject());
			IBinding[] bindings= parser.createBindings(new IJavaElement[] { method }, null);
			if (bindings.length == 1 && bindings[0] instanceof IMethodBinding) {
				return ((IMethodBinding) bindings[0]).getReturnType().getQualifiedName();
				
			}
		} catch (IllegalStateException e) {
			// method does not exist
		}
		
		return Signature.getSignatureSimpleName(method.getReturnType());
	}
	
	/*
	 * Returns the parameters type names used in see tags. Currently, these are always fully qualified.
	 */
	private static String[] getParameterTypeNamesForSeeTag(IMethod overridden) {
		try {
			ASTParser parser= ASTParser.newParser(AST.JLS3);
			parser.setProject(overridden.getJavaProject());
			IBinding[] bindings= parser.createBindings(new IJavaElement[] { overridden }, null);
			if (bindings.length == 1 && bindings[0] instanceof IMethodBinding) {
				return getParameterTypeNamesForSeeTag((IMethodBinding) bindings[0]);
			}
		} catch (IllegalStateException e) {
			// method does not exist
		}
		// fall back code. Not good for generic methods!
		String[] paramTypes= overridden.getParameterTypes();
		String[] paramTypeNames= new String[paramTypes.length];
		for (int i= 0; i < paramTypes.length; i++) {
			paramTypeNames[i]= Signature.toString(Signature.getTypeErasure(paramTypes[i]));
		}
		return paramTypeNames;
	}
	
	
	/**
	 * Returns the parameters type names used in see tags. Currently, these are always fully qualified.
	 * @param binding The IMethodBinding to resolve the Parameter Type Names
	 */
	private static String[] getParameterTypeNamesForSeeTag(IMethodBinding binding) {
		ITypeBinding[] typeBindings= binding.getParameterTypes();
		String[] result= new String[typeBindings.length];
		for (int i= 0; i < result.length; i++) {
			ITypeBinding curr= typeBindings[i];
			if (curr.isTypeVariable()) {
				curr= curr.getErasure(); // in Javadoc only use type variable erasure
			}
			curr= curr.getTypeDeclaration(); // no parameterized types
			result[i]= curr.getQualifiedName();
		}
		return result;
	}
	
	private static String getSeeTag(String declaringClassQualifiedName, String methodName, String[] parameterTypesQualifiedNames) {
		StringBuffer buf= new StringBuffer();
		buf.append("@see "); //$NON-NLS-1$
		buf.append(declaringClassQualifiedName);
		buf.append('#'); 
		buf.append(methodName);
		buf.append('(');
		for (int i= 0; i < parameterTypesQualifiedNames.length; i++) {
			if (i > 0) {
				buf.append(", "); //$NON-NLS-1$
			}
			buf.append(parameterTypesQualifiedNames[i]);
		}
		buf.append(')');
		return buf.toString();
	}

}
