/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import java.lang.ref.SoftReference;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

/**
 * An ICompilationUnit that retrieves its contents using an IFile
 */
public class ResourceCompilationUnit implements ICompilationUnit {

	private final IFile file;
	private volatile SoftReference<char[]> contentRef;
	private final char[] fileName;
	private final char[] mainTypeName;
	private final char[] module;
	public ResourceCompilationUnit(IFile file, char[] mod) {
		this.file = file;
		this.module = mod;
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

	@Override
	public char[] getContents() {
		SoftReference<char[]> cr = this.contentRef;
		if (cr != null) {
			char[] cachedContents = cr.get();
			if (cachedContents != null) {
				return cachedContents;
			}
		}
		char[] contents;
		try {
			contents = Util.getResourceContentsAsCharArray(this.file);
		} catch (CoreException e) {
			contents = CharOperation.NO_CHAR;
		}
		 // softly cache the result:
		this.contentRef = new SoftReference<>(contents);
		return contents;
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

	@Override
	public char[] getModuleName() {
		return this.module;
	}
}
