/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.eval;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.eval.ICodeSnippetRequestor;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;
import org.eclipse.jdt.internal.eval.IRequestor;

public class RequestorWrapper implements IRequestor {

	ICodeSnippetRequestor requestor;

public RequestorWrapper(ICodeSnippetRequestor requestor) {
	this.requestor = requestor;
}
/**
 * @see ICodeSnippetRequestor
 */
public boolean acceptClassFiles(ClassFile[] classFiles, char[] codeSnippetClassName) {
	int length = classFiles.length;
	byte[][] classFileBytes = new byte[length][];
	String[][] compoundNames = new String[length][];
	for (int i = 0; i < length; i++) {
		ClassFile classFile = classFiles[i];
		classFileBytes[i] = classFile.getBytes();
		char[][] classFileCompundName = classFile.getCompoundName();
		int length2 = classFileCompundName.length;
		String[] compoundName = new String[length2];
		for (int j = 0; j < length2; j++){
			compoundName[j] = new String(classFileCompundName[j]);
		}
		compoundNames[i] = compoundName;
	}
	return this.requestor.acceptClassFiles(classFileBytes, compoundNames, codeSnippetClassName == null ? null : new String(codeSnippetClassName));
}
/**
 * @see ICodeSnippetRequestor
 */
public void acceptProblem(CategorizedProblem problem, char[] fragmentSource, int fragmentKind) {
	try {
		IMarker marker = ResourcesPlugin.getWorkspace().getRoot().createMarker(IJavaModelMarker.TRANSIENT_PROBLEM);
		marker.setAttribute(IJavaModelMarker.ID, problem.getID());
		marker.setAttribute(IMarker.CHAR_START, problem.getSourceStart());
		marker.setAttribute(IMarker.CHAR_END, problem.getSourceEnd() + 1);
		marker.setAttribute(IMarker.LINE_NUMBER, problem.getSourceLineNumber());
		//marker.setAttribute(IMarker.LOCATION, "#" + problem.getSourceLineNumber());
		marker.setAttribute(IMarker.MESSAGE, problem.getMessage());
		marker.setAttribute(IMarker.SEVERITY, (problem.isWarning() ? IMarker.SEVERITY_WARNING : IMarker.SEVERITY_ERROR));
		marker.setAttribute(IMarker.SOURCE_ID, JavaBuilder.SOURCE_ID);
		this.requestor.acceptProblem(marker, new String(fragmentSource), fragmentKind);
	} catch (CoreException e) {
		e.printStackTrace();
	}
}
}
