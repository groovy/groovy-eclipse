/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;

@SuppressWarnings({ "rawtypes" })
public class Requestor implements ICompilerRequestor {
	public boolean hasErrors = false;
	public String outputPath;
	private final boolean forceOutputGeneration;
	public Hashtable expectedProblems = new Hashtable();
	public String problemLog = "";
	public ICompilerRequestor clientRequestor;
	public boolean showCategory = false;
	public boolean showWarningToken = false;

public Requestor(boolean forceOutputGeneration, ICompilerRequestor clientRequestor, boolean showCategory, boolean showWarningToken) {
	this.forceOutputGeneration = forceOutputGeneration;
	this.clientRequestor = clientRequestor;
	this.showCategory = showCategory;
	this.showWarningToken = showWarningToken;
}
public void acceptResult(CompilationResult compilationResult) {
	this.hasErrors |= compilationResult.hasErrors();
	this.problemLog += Util.getProblemLog(compilationResult, this.showCategory, this.showWarningToken);
	outputClassFiles(compilationResult);
	if (this.clientRequestor != null) {
		this.clientRequestor.acceptResult(compilationResult);
	}
}
protected void outputClassFiles(CompilationResult unitResult) {
	if ((unitResult != null) && (!unitResult.hasErrors() || this.forceOutputGeneration)) {
		ClassFile[] classFiles = unitResult.getClassFiles();
		if (this.outputPath != null) {
			for (int i = 0, fileCount = classFiles.length; i < fileCount; i++) {
				// retrieve the key and the corresponding classfile
				ClassFile classFile = classFiles[i];
				String relativeName =
					new String(classFile.fileName()).replace('/', File.separatorChar) + ".class";
				try {
					org.eclipse.jdt.internal.compiler.util.Util.writeToDisk(true, this.outputPath, relativeName, classFile);
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
}
