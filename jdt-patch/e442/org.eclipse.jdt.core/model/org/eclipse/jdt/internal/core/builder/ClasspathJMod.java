/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation.
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
package org.eclipse.jdt.internal.core.builder;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

public class ClasspathJMod extends ClasspathJar {

	public static char[] CLASSES = "classes".toCharArray(); //$NON-NLS-1$
	public static final String CLASSES_FOLDER = "classes/"; //$NON-NLS-1$

	ClasspathJMod(String zipFilename, long lastModified, AccessRuleSet accessRuleSet, IPath externalAnnotationPath) {
		super(zipFilename, lastModified, accessRuleSet, externalAnnotationPath, true);
	}
	@Override
	IModule initializeModule() {
		IModule mod = null;
		try (ZipFile file = new ZipFile(this.zipFilename)) {
			String fileName = CLASSES_FOLDER + IModule.MODULE_INFO_CLASS;
			ClassFileReader classfile = ClassFileReader.read(file, fileName);
			if (classfile != null) {
				mod = classfile.getModuleDeclaration();
			}
		} catch (ClassFormatException | IOException e) {
			// do nothing
		}
		return mod;
	}

	@Override
	public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName,
											boolean asBinaryOnly, Predicate<String> moduleNameFilter) {
		if (!isPackage(qualifiedPackageName, moduleName)) return null; // most common case
		if (moduleNameFilter != null && this.module != null && !moduleNameFilter.test(String.valueOf(this.module.name())))
			return null;

		try {
			qualifiedBinaryFileName = CLASSES_FOLDER + qualifiedBinaryFileName;
			IBinaryType reader = ClassFileReader.read(this.zipFile, qualifiedBinaryFileName);
			if (reader != null) {
				char[] modName = this.module == null ? null : this.module.name();
				if (reader instanceof ClassFileReader) {
					ClassFileReader classReader = (ClassFileReader) reader;
					if (classReader.moduleName == null)
						classReader.moduleName = modName;
					else
						modName = classReader.moduleName;
				}
				String fileNameWithoutExtension = qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - SuffixConstants.SUFFIX_CLASS.length);
				return createAnswer(fileNameWithoutExtension, reader, modName);
			}
		} catch (IOException | ClassFormatException e) { // treat as if class file is missing
		}
		return null;
	}
	@Override
	protected Set<String> readPackageNames() {
		final Set<String> packageSet = new HashSet<>();
		packageSet.add(""); //$NON-NLS-1$
		for (Enumeration<? extends ZipEntry> e = this.zipFile.entries(); e.hasMoreElements(); ) {
			ZipEntry entry = e.nextElement();
			char[] entryName = entry.getName().toCharArray();
			int index = CharOperation.indexOf('/', entryName);
			if (index != -1) {
				char[] folder = CharOperation.subarray(entryName, 0, index);
				if (CharOperation.equals(CLASSES, folder)) {
					char[] fileName = CharOperation.subarray(entryName, index + 1, entryName.length);
					addToPackageSet(packageSet, new String(fileName), false);
				}
			}
		}
		return packageSet;
	}
}
