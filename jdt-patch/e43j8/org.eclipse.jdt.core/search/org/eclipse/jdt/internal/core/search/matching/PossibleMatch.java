/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.util.Util;

public class PossibleMatch implements ICompilationUnit {

public static final String NO_SOURCE_FILE_NAME = "NO SOURCE FILE NAME"; //$NON-NLS-1$
public static final char[] NO_SOURCE_FILE = new char[0];

public IResource resource;
public Openable openable;
public MatchingNodeSet nodeSet;
public char[][] compoundName;
CompilationUnitDeclaration parsedUnit;
public SearchDocument document;
private String sourceFileName;
private char[] source;
private PossibleMatch similarMatch;

public PossibleMatch(MatchLocator locator, IResource resource, Openable openable, SearchDocument document, boolean mustResolve) {
	this.resource = resource;
	this.openable = openable;
	this.document = document;
	this.nodeSet = new MatchingNodeSet(mustResolve);
	char[] qualifiedName = getQualifiedName();
	if (qualifiedName != null)
		this.compoundName = CharOperation.splitOn('.', qualifiedName);
}
public void cleanUp() {
	this.source = null;
	if (this.parsedUnit != null) {
		this.parsedUnit.cleanUp();
		this.parsedUnit = null;
	}
	this.nodeSet = null;
}
public boolean equals(Object obj) {
	if (this.compoundName == null) return super.equals(obj);
	if (!(obj instanceof PossibleMatch)) return false;

	// By using the compoundName of the source file, multiple .class files (A, A$M...) are considered equal
	// Even .class files for secondary types and their nested types
	return CharOperation.equals(this.compoundName, ((PossibleMatch) obj).compoundName);
}
public char[] getContents() {
	char[] contents = (this.source == NO_SOURCE_FILE) ? null : this.source;
	if (this.source == null) {
		if (this.openable instanceof ClassFile) {
			String fileName = getSourceFileName();
			if (fileName == NO_SOURCE_FILE_NAME) return CharOperation.NO_CHAR;

			SourceMapper sourceMapper = this.openable.getSourceMapper();
			if (sourceMapper != null) {
				IType type = ((ClassFile) this.openable).getType();
				contents = sourceMapper.findSource(type, fileName);
			}
		} else {
			contents = this.document.getCharContents();
		}
		this.source = (contents == null) ? NO_SOURCE_FILE : contents;
	}
	return contents;
}
/**
 * The exact openable file name. In particular, will be the originating .class file for binary openable with attached
 * source.
 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
 * @see PackageReferenceLocator#isDeclaringPackageFragment(IPackageFragment, org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding)
 */
public char[] getFileName() {
	return this.openable.getElementName().toCharArray();
}
public char[] getMainTypeName() {
	// The file is no longer opened to get its name => remove fix for bug 32182
	return this.compoundName[this.compoundName.length-1];
}
public char[][] getPackageName() {
	int length = this.compoundName.length;
	if (length <= 1) return CharOperation.NO_CHAR_CHAR;
	return CharOperation.subarray(this.compoundName, 0, length - 1);
}
/*
 * Returns the fully qualified name of the main type of the compilation unit
 * or the main type of the .java file that defined the class file.
 */
private char[] getQualifiedName() {
	if (this.openable instanceof CompilationUnit) {
		// get file name
		String fileName = this.openable.getElementName(); // working copy on a .class file may not have a resource, so use the element name
		// get main type name
		char[] mainTypeName = Util.getNameWithoutJavaLikeExtension(fileName).toCharArray();
		CompilationUnit cu = (CompilationUnit) this.openable;
		return cu.getType(new String(mainTypeName)).getFullyQualifiedName().toCharArray();
	} else if (this.openable instanceof ClassFile) {
		String fileName = getSourceFileName();
		if (fileName == NO_SOURCE_FILE_NAME)
			return ((ClassFile) this.openable).getType().getFullyQualifiedName('.').toCharArray();

		// Class file may have a source file name with ".java" extension (see bug 73784)
		int index = Util.indexOfJavaLikeExtension(fileName);
		String simpleName = index==-1 ? fileName : fileName.substring(0, index);
		PackageFragment pkg = (PackageFragment) this.openable.getParent();
		return Util.concatWith(pkg.names, simpleName, '.').toCharArray();
	}
	return null;
}
PossibleMatch getSimilarMatch() {
	return this.similarMatch;
}
/*
 * Returns the source file name of the class file.
 * Returns NO_SOURCE_FILE_NAME if not found.
 */
private String getSourceFileName() {
	if (this.sourceFileName != null) return this.sourceFileName;

	this.sourceFileName = NO_SOURCE_FILE_NAME;
	if (this.openable.getSourceMapper() != null) {
		BinaryType type = (BinaryType) ((ClassFile) this.openable).getType();
		ClassFileReader reader = MatchLocator.classFileReader(type);
		if (reader != null) {
			String fileName = type.sourceFileName(reader);
			this.sourceFileName = fileName == null ? NO_SOURCE_FILE_NAME : fileName;
		}
	}
	return this.sourceFileName;
}
boolean hasSimilarMatch() {
	return this.similarMatch != null && this.source == NO_SOURCE_FILE;
}
public int hashCode() {
	if (this.compoundName == null) return super.hashCode();

	int hashCode = 0;
	for (int i = 0, length = this.compoundName.length; i < length; i++)
		hashCode += CharOperation.hashCode(this.compoundName[i]);
	return hashCode;
}
public boolean ignoreOptionalProblems() {
	return false;
}
void setSimilarMatch(PossibleMatch possibleMatch) {
	// source does not matter on similar match as it is read on
	// the first stored possible match
	possibleMatch.source = NO_SOURCE_FILE;
	this.similarMatch = possibleMatch;
}
public String toString() {
	return this.openable == null ? "Fake PossibleMatch" : this.openable.toString(); //$NON-NLS-1$
}
}
