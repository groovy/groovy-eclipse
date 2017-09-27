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
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

public class InMemoryNameEnvironment implements INameEnvironment {
	INameEnvironment[] classLibs;
	HashtableOfObject compilationUnits = new HashtableOfObject();
public InMemoryNameEnvironment(String[] compilationUnits, INameEnvironment[] classLibs) {
	this.classLibs = classLibs;
	for (int i = 0, length = compilationUnits.length - 1; i < length; i += 2) {
		String fileName = compilationUnits[i];
		char[] contents = compilationUnits[i + 1].toCharArray();
		String dirName = "";
		int lastSlash = -1;
		if ((lastSlash = fileName.lastIndexOf('/')) != -1) {
			dirName = fileName.substring(0, lastSlash);
		}
		char[] packageName = dirName.replace('/', '.').toCharArray();
		char[] cuName = fileName.substring(lastSlash == -1 ? 0 : lastSlash + 1, fileName.length() - 5).toCharArray(); // remove ".java"
		HashtableOfObject cus = (HashtableOfObject)this.compilationUnits.get(packageName);
		if (cus == null) {
			cus = new HashtableOfObject();
			this.compilationUnits.put(packageName, cus);
		}
		CompilationUnit unit = new CompilationUnit(contents, fileName, null);
		cus.put(cuName, unit);
	}
}
public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
	return findType(
		compoundTypeName[compoundTypeName.length - 1],
		CharOperation.subarray(compoundTypeName, 0, compoundTypeName.length - 1));
}
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
	HashtableOfObject cus = (HashtableOfObject)this.compilationUnits.get(CharOperation.concatWith(packageName, '.'));
	if (cus == null) {
		return findTypeFromClassLibs(typeName, packageName);
	}
	CompilationUnit unit = (CompilationUnit)cus.get(typeName);
	if (unit == null) {
		return findTypeFromClassLibs(typeName, packageName);
	}
	return new NameEnvironmentAnswer(unit, null /*no access restriction*/);
}
private NameEnvironmentAnswer findTypeFromClassLibs(char[] typeName, char[][] packageName) {
	for (int i = 0; i < this.classLibs.length; i++) {
		NameEnvironmentAnswer answer = this.classLibs[i].findType(typeName, packageName);
		if (answer != null) {
			return answer;
		}
	}
	return null;
}
public boolean isPackage(char[][] parentPackageName, char[] packageName) {
	char[] pkg = CharOperation.concatWith(parentPackageName, packageName, '.');
	return
		this.compilationUnits.get(pkg) != null ||
		isPackageFromClassLibs(parentPackageName, packageName);
}
public boolean isPackageFromClassLibs(char[][] parentPackageName, char[] packageName) {
	for (int i = 0; i < this.classLibs.length; i++) {
		if (this.classLibs[i].isPackage(parentPackageName, packageName)) {
			return true;
		}
	}
	return false;
}
public void cleanup() {
	for (int i = 0, max = this.classLibs.length; i < max; i++) {
		this.classLibs[i].cleanup();
	}
	this.compilationUnits = new HashtableOfObject();
}
}
