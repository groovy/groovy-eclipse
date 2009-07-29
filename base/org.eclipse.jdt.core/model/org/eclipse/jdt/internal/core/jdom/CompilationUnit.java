/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.jdom;

import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

/**
 * Implements a very simple version of the ICompilationUnit.
 *
 * <p>Please do not use outside of jdom.</p>
 */
public class CompilationUnit implements ICompilationUnit {
	protected char[] fContents;
	protected char[] fFileName;
	protected char[] fMainTypeName;
public CompilationUnit(char[] contents, char[] filename) {
	fContents = contents;
	fFileName = filename;

	String file = new String(filename);
	int start = file.lastIndexOf("/") + 1; //$NON-NLS-1$
	if (start == 0 || start < file.lastIndexOf("\\")) //$NON-NLS-1$
		start = file.lastIndexOf("\\") + 1; //$NON-NLS-1$

	int end = file.lastIndexOf("."); //$NON-NLS-1$
	if (end == -1)
		end = file.length();

	fMainTypeName = file.substring(start, end).toCharArray();
}
public char[] getContents() {
	return fContents;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
 */
public char[] getFileName() {
	return fFileName;
}
public char[] getMainTypeName() {
	return fMainTypeName;
}
public char[][] getPackageName() {
	return null;
}
public String toString() {
	return "CompilationUnit[" + new String(fFileName) + "]";  //$NON-NLS-2$ //$NON-NLS-1$
}
}
