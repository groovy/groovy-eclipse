/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.util.ArrayList;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IDebugRequestor;
import org.eclipse.jdt.internal.core.util.Util;

public class EfficiencyCompilerRequestor implements IDebugRequestor {
	private boolean isActive = false;

	private ArrayList<String> compiledClasses = new ArrayList<>();
	private ArrayList<String> compiledFiles = new ArrayList<>();


	public void acceptDebugResult(CompilationResult result){
		this.compiledFiles.add(new String(result.fileName));
		ClassFile[] classFiles = result.getClassFiles();
		Util.sort(classFiles, new Util.Comparer() {
			public int compare(Object a, Object b) {
				String aName = new String(((ClassFile)a).fileName());
				String bName = new String(((ClassFile)b).fileName());
				return aName.compareTo(bName);
			}
		});
		for (int i = 0; i < classFiles.length; i++) {
			String className = new String(classFiles[i].fileName());
			this.compiledClasses.add(className.replace('/', '.'));
		}
	}

	String[] getCompiledClasses(){
		return this.compiledClasses.toArray(new String[0]);
	}

	String[] getCompiledFiles(){
		return this.compiledFiles.toArray(new String[0]);
	}

	public void clearResult(){
		this.compiledClasses.clear();
		this.compiledFiles.clear();
	}

	public void reset() {
	    // do nothing by default
	}

	public void activate() {
		this.isActive = true;
	}

	public void deactivate() {
		this.isActive = false;
	}

	public boolean isActive() {
		return this.isActive;
	}
}
