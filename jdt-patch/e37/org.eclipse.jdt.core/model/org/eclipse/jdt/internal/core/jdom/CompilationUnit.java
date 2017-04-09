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
	this.fContents = contents;
	this.fFileName = filename;

	String file = new String(filename);
	int start = file.lastIndexOf("/") + 1; //$NON-NLS-1$
	if (start == 0 || start < file.lastIndexOf("\\")) //$NON-NLS-1$
		start = file.lastIndexOf("\\") + 1; //$NON-NLS-1$

	int end = file.lastIndexOf("."); //$NON-NLS-1$
	if (end == -1)
		end = file.length();

	this.fMainTypeName = file.substring(start, end).toCharArray();
}
public char[] getContents() {
	return this.fContents;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
 */
public char[] getFileName() {
	return this.fFileName;
}
public char[] getMainTypeName() {
	return this.fMainTypeName;
}
public char[][] getPackageName() {
	return null;
}
public String toString() {
	return "CompilationUnit[" + new String(this.fFileName) + "]";  //$NON-NLS-2$ //$NON-NLS-1$
}
}
