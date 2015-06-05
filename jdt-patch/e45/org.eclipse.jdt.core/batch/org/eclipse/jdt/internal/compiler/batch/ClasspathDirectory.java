/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 440687 - [compiler][batch][null] improve command line option for external annotations
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationProvider;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.Util;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ClasspathDirectory extends ClasspathLocation {

private Hashtable directoryCache;
private String[] missingPackageHolder = new String[1];
private int mode; // ability to only consider one kind of files (source vs. binaries), by default use both
private String encoding; // only useful if referenced in the source path
private Hashtable<String, Hashtable<String, String>> packageSecondaryTypes = null;

ClasspathDirectory(File directory, String encoding, int mode,
		AccessRuleSet accessRuleSet, String destinationPath) {
	super(accessRuleSet, destinationPath);
	this.mode = mode;
	try {
		this.path = directory.getCanonicalPath();
	} catch (IOException e) {
		// should not happen as we know that the file exists
		this.path = directory.getAbsolutePath();
	}
	if (!this.path.endsWith(File.separator))
		this.path += File.separator;
	this.directoryCache = new Hashtable(11);
	this.encoding = encoding;
}
String[] directoryList(String qualifiedPackageName) {
	String[] dirList = (String[]) this.directoryCache.get(qualifiedPackageName);
	if (dirList == this.missingPackageHolder) return null; // package exists in another classpath directory or jar
	if (dirList != null) return dirList;

	File dir = new File(this.path + qualifiedPackageName);
	notFound : if (dir.isDirectory()) {
		// must protect against a case insensitive File call
		// walk the qualifiedPackageName backwards looking for an uppercase character before the '/'
		int index = qualifiedPackageName.length();
		int last = qualifiedPackageName.lastIndexOf(File.separatorChar);
		while (--index > last && !ScannerHelper.isUpperCase(qualifiedPackageName.charAt(index))){/*empty*/}
		if (index > last) {
			if (last == -1) {
				if (!doesFileExist(qualifiedPackageName, Util.EMPTY_STRING))
					break notFound;
			} else {
				String packageName = qualifiedPackageName.substring(last + 1);
				String parentPackage = qualifiedPackageName.substring(0, last);
				if (!doesFileExist(packageName, parentPackage))
					break notFound;
			}
		}
		if ((dirList = dir.list()) == null)
			dirList = CharOperation.NO_STRINGS;
		this.directoryCache.put(qualifiedPackageName, dirList);
		return dirList;
	}
	this.directoryCache.put(qualifiedPackageName, this.missingPackageHolder);
	return null;
}
boolean doesFileExist(String fileName, String qualifiedPackageName) {
	String[] dirList = directoryList(qualifiedPackageName);
	if (dirList == null) return false; // most common case

	for (int i = dirList.length; --i >= 0;)
		if (fileName.equals(dirList[i]))
			return true;
	return false;
}
public List fetchLinkedJars(FileSystem.ClasspathSectionProblemReporter problemReporter) {
	return null;
}
public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	return findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, false);
}
public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
	if (!isPackage(qualifiedPackageName)) return null; // most common case

	String fileName = new String(typeName);
	boolean binaryExists = ((this.mode & BINARY) != 0) && doesFileExist(fileName + SUFFIX_STRING_class, qualifiedPackageName);
	boolean sourceExists = ((this.mode & SOURCE) != 0) && doesFileExist(fileName + SUFFIX_STRING_java, qualifiedPackageName);
	if (sourceExists && !asBinaryOnly) {
		String fullSourcePath = this.path + qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - 6)  + SUFFIX_STRING_java;
		if (!binaryExists)
			return new NameEnvironmentAnswer(new CompilationUnit(null,
					fullSourcePath, this.encoding, this.destinationPath),
					fetchAccessRestriction(qualifiedBinaryFileName));
		String fullBinaryPath = this.path + qualifiedBinaryFileName;
		long binaryModified = new File(fullBinaryPath).lastModified();
		long sourceModified = new File(fullSourcePath).lastModified();
		if (sourceModified > binaryModified)
			return new NameEnvironmentAnswer(new CompilationUnit(null,
					fullSourcePath, this.encoding, this.destinationPath),
					fetchAccessRestriction(qualifiedBinaryFileName));
	}
	if (binaryExists) {
		try {
			ClassFileReader reader = ClassFileReader.read(this.path + qualifiedBinaryFileName);
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321115, package names are to be treated case sensitive.
			String typeSearched = qualifiedPackageName.length() > 0 ? 
					qualifiedPackageName.replace(File.separatorChar, '/') + "/" + fileName //$NON-NLS-1$
					: fileName;
			if (!CharOperation.equals(reader.getName(), typeSearched.toCharArray())) {
				reader = null;
			}
			if (reader != null)
				return new NameEnvironmentAnswer(
						reader,
						fetchAccessRestriction(qualifiedBinaryFileName));
		} catch (IOException e) {
			// treat as if file is missing
		} catch (ClassFormatException e) {
			// treat as if file is missing
		}
	}
	return null;
}
public NameEnvironmentAnswer findSecondaryInClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	boolean sourceExists = isPackage(qualifiedPackageName) && ((this.mode & SOURCE) != 0) && doesFileExist( new String(typeName) + SUFFIX_STRING_java, qualifiedPackageName);
	return sourceExists ? null : findSourceSecondaryType(typeName, qualifiedPackageName, qualifiedBinaryFileName); /* only secondary types */
}

@Override
public boolean hasAnnotationFileFor(String qualifiedTypeName) {
	int pos = qualifiedTypeName.lastIndexOf('/');
	if (pos != -1 && (pos + 1 < qualifiedTypeName.length())) {
		String fileName = qualifiedTypeName.substring(pos + 1) + '.' + ExternalAnnotationProvider.ANNOTION_FILE_EXTENSION;
		return doesFileExist(fileName, qualifiedTypeName.substring(0, pos));
	}
	return false;
}


/**
 *  Add all the secondary types in the package
 */
private Hashtable<String, String> getPackageTypes(char[] typeName, String qualifiedPackageName) {
	Hashtable<String, String> packageEntry = new Hashtable<String, String>();

	String[] dirList = (String[]) this.directoryCache.get(qualifiedPackageName);
	if (dirList == this.missingPackageHolder // package exists in another classpath directory or jar 
			|| dirList == null) 
		return packageEntry;

	File dir = new File(this.path + qualifiedPackageName);
	File[] listFiles = dir.isDirectory() ? dir.listFiles() : null;
	if (listFiles == null) return packageEntry;

	for (int i = 0, l = listFiles.length; i < l; ++i) {
		File f = listFiles[i];
		if (f.isDirectory()) continue;
		String s = f.getAbsolutePath();
		if (s == null) continue;
		CompilationUnit cu = new CompilationUnit(null, s, this.encoding, this.destinationPath);
		CompilationResult compilationResult = new CompilationResult(cu.getContents(), 1, 1, 10);
		ProblemReporter problemReporter = 
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(JavaCore.getOptions()),
					new DefaultProblemFactory());
		Parser parser = new Parser(problemReporter, false);

		CompilationUnitDeclaration unit = parser.parse(cu, compilationResult);
		org.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] types = unit != null ? unit.types : null;
		if (types == null) continue;
		for (int j = 0, k = types.length; j < k; j++) {
			TypeDeclaration type = types[j];
			char[] name = type.isSecondary() ? type.name : null;  // add only secondary types
			if (name != null) 
				packageEntry.put(new String(name), s);
		}
	}
	return packageEntry;
}
private NameEnvironmentAnswer findSourceSecondaryType(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	
	if (this.packageSecondaryTypes == null) this.packageSecondaryTypes = new Hashtable<String, Hashtable<String,String>>();
	Hashtable<String, String> packageEntry = this.packageSecondaryTypes.get(qualifiedPackageName);
	if (packageEntry == null) {
		packageEntry = 	getPackageTypes(typeName, qualifiedPackageName);
		this.packageSecondaryTypes.put(qualifiedPackageName, packageEntry);
	}
	String fileName = packageEntry.get(new String(typeName));
	return fileName != null ? new NameEnvironmentAnswer(new CompilationUnit(null,
			fileName, this.encoding, this.destinationPath),
			fetchAccessRestriction(qualifiedBinaryFileName)) : null;
}


public char[][][] findTypeNames(String qualifiedPackageName) {
	if (!isPackage(qualifiedPackageName)) {
		return null; // most common case
	}
	File dir = new File(this.path + qualifiedPackageName);
	if (!dir.exists() || !dir.isDirectory()) {
		return null;
	}
	String[] listFiles = dir.list(new FilenameFilter() {
		public boolean accept(File directory1, String name) {
			String fileName = name.toLowerCase();
			return fileName.endsWith(".class") || fileName.endsWith(".java"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	});
	int length;
	if (listFiles == null || (length = listFiles.length) == 0) {
		return null;
	}
	char[][][] result = new char[length][][];
	char[][] packageName = CharOperation.splitOn(File.separatorChar, qualifiedPackageName.toCharArray());
	for (int i = 0; i < length; i++) {
		String fileName = listFiles[i];
		int indexOfLastDot = fileName.indexOf('.');
		result[i] = CharOperation.arrayConcat(packageName, fileName.substring(0, indexOfLastDot).toCharArray());
	}
	return result;
}
public void initialize() throws IOException {
	// nothing to do
}
public boolean isPackage(String qualifiedPackageName) {
	return directoryList(qualifiedPackageName) != null;
}
public void reset() {
	this.directoryCache = new Hashtable(11);
}
public String toString() {
	return "ClasspathDirectory " + this.path; //$NON-NLS-1$
}
public char[] normalizedPath() {
	if (this.normalizedPath == null) {
		this.normalizedPath = this.path.toCharArray();
		if (File.separatorChar == '\\') {
			CharOperation.replace(this.normalizedPath, '\\', '/');
		}
	}
	return this.normalizedPath;
}
public String getPath() {
	return this.path;
}
public int getMode() {
	return this.mode;
}
}
