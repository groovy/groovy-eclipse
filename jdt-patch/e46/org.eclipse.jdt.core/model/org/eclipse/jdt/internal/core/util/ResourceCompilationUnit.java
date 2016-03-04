/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

/**
 * An ICompilationUnit that retrieves its contents using an IFile
 */
public class ResourceCompilationUnit implements ICompilationUnit {

	private IFile file;
	private char[] contents;
	private char[] fileName;
	private char[] mainTypeName;

	public ResourceCompilationUnit(IFile file) {
		this.file = file;

		String f = file.getFullPath().toString();
		this.fileName = f.toCharArray();
		int start = f.lastIndexOf("/") + 1; //$NON-NLS-1$
		if (start == 0 || start < f.lastIndexOf("\\")) //$NON-NLS-1$
			start = f.lastIndexOf("\\") + 1; //$NON-NLS-1$

		int end = f.lastIndexOf("."); //$NON-NLS-1$
		if (end == -1)
			end = f.length();

		this.mainTypeName = f.substring(start, end).toCharArray();
	}

	public char[] getContents() {
		if (this.contents != null)
			return this.contents;   // answer the cached source

		// otherwise retrieve it
		try {
			return (this.contents = Util.getResourceContentsAsCharArray(this.file));
		} catch (CoreException e) {
			return CharOperation.NO_CHAR;
		}
	}

	@Override
	public char[] getFileName() {
		return this.fileName;
	}

	@Override
	public char[] getMainTypeName() {
		return this.mainTypeName;
	}

	@Override
	public char[][] getPackageName() {
		return null;
	}

	@Override
	public boolean ignoreOptionalProblems() {
		return false;
	}
}
