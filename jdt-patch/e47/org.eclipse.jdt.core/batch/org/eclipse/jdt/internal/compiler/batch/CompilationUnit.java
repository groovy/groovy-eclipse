/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.util.Util;

public class CompilationUnit implements ICompilationUnit {
	public char[] contents;
	public char[] fileName;
	public char[] mainTypeName;
	String encoding;
	public String destinationPath;
	public char[] module;
		// a specific destination path for this compilation unit; coding is
		// aligned with Main.destinationPath:
		// == null: unspecified, use whatever value is set by the enclosing
		//          context, id est Main;
		// == Main.NONE: absorbent element, do not output class files;
		// else: use as the path of the directory into which class files must
		//       be written.
	private boolean ignoreOptionalProblems;
	private CompilationUnit modCU;
	private ModuleBinding moduleBinding;

public CompilationUnit(char[] contents, String fileName, String encoding) {
	this(contents, fileName, encoding, null);
}
public CompilationUnit(char[] contents, String fileName, String encoding,
		String destinationPath) {
	this(contents, fileName, encoding, destinationPath, false, null);
}
public CompilationUnit(char[] contents, String fileName, String encoding,
		String destinationPath, boolean ignoreOptionalProblems, String modName) {
	this.contents = contents;
	if (modName != null)
		this.module = modName.toCharArray();
	char[] fileNameCharArray = fileName.toCharArray();
	switch(File.separatorChar) {
		case '/' :
			if (CharOperation.indexOf('\\', fileNameCharArray) != -1) {
				CharOperation.replace(fileNameCharArray, '\\', '/');
			}
			break;
		case '\\' :
			if (CharOperation.indexOf('/', fileNameCharArray) != -1) {
				CharOperation.replace(fileNameCharArray, '/', '\\');
			}
	}
	this.fileName = fileNameCharArray;
	int start = CharOperation.lastIndexOf(File.separatorChar, fileNameCharArray) + 1;

	int end = CharOperation.lastIndexOf('.', fileNameCharArray);
	if (end == -1) {
		end = fileNameCharArray.length;
	}

	this.mainTypeName = CharOperation.subarray(fileNameCharArray, start, end);
	this.encoding = encoding;
	this.destinationPath = destinationPath;
	this.ignoreOptionalProblems = ignoreOptionalProblems;
}
public char[] getContents() {
	if (this.contents != null)
		return this.contents;   // answer the cached source

	// otherwise retrieve it
	try {
		return Util.getFileCharContent(new File(new String(this.fileName)), this.encoding);
	} catch (IOException e) {
		this.contents = CharOperation.NO_CHAR; // assume no source if asked again
		throw new AbortCompilationUnit(null, e, this.encoding);
	}
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
 */
public char[] getFileName() {
	return this.fileName;
}
public char[] getMainTypeName() {
	return this.mainTypeName;
}
public char[][] getPackageName() {
	return null;
}
public boolean ignoreOptionalProblems() {
	return this.ignoreOptionalProblems;
}
public String toString() {
	return "CompilationUnit[" + new String(this.fileName) + "]";  //$NON-NLS-2$ //$NON-NLS-1$
}
public void setModule(CompilationUnit compilationUnit) {
	this.modCU = compilationUnit;
}
@Override
public char[] getModuleName() {
	return this.module;
}
@Override
public ModuleBinding module(LookupEnvironment rootEnvironment) {
	if (this.moduleBinding != null)
		return this.moduleBinding;
	if (this.modCU != null)
		return this.moduleBinding = this.modCU.module(rootEnvironment);
	if (CharOperation.endsWith(this.fileName, TypeConstants.MODULE_INFO_FILE_NAME)) {
		this.moduleBinding = rootEnvironment.getModule(this.module);
		if (this.moduleBinding == null)
			throw new IllegalStateException("Module should be known"); //$NON-NLS-1$
		return this.moduleBinding;
	}
	return rootEnvironment.UnNamedModule;
}
public String getDestinationPath() {
	return this.destinationPath;
}
}
