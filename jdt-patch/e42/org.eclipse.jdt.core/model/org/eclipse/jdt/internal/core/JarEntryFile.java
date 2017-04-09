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
package org.eclipse.jdt.internal.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * A jar entry that represents a non-java file found in a JAR.
 *
 * @see IStorage
 */
public class JarEntryFile  extends JarEntryResource {
	private static final IJarEntryResource[] NO_CHILDREN = new IJarEntryResource[0];

	public JarEntryFile(String simpleName) {
		super(simpleName);
	}

	public JarEntryResource clone(Object newParent) {
		JarEntryFile file = new JarEntryFile(this.simpleName);
		file.setParent(newParent);
		return file;
	}

	public InputStream getContents() throws CoreException {
		ZipFile zipFile = null;
		try {
			zipFile = getZipFile();
			if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
				System.out.println("(" + Thread.currentThread() + ") [JarEntryFile.getContents()] Creating ZipFile on " +zipFile.getName()); //$NON-NLS-1$	//$NON-NLS-2$
			}
			String entryName = getEntryName();
			ZipEntry zipEntry = zipFile.getEntry(entryName);
			if (zipEntry == null){
				throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, entryName));
			}
			byte[] contents = Util.getZipEntryByteContent(zipEntry, zipFile);
			return new ByteArrayInputStream(contents);
		} catch (IOException e){
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		} finally {
			// avoid leaking ZipFiles
			JavaModelManager.getJavaModelManager().closeZipFile(zipFile);
		}
	}

	public IJarEntryResource[] getChildren() {
		return NO_CHILDREN;
	}

	public boolean isFile() {
		return true;
	}

	public String toString() {
		return "JarEntryFile["+getEntryName()+"]"; //$NON-NLS-2$ //$NON-NLS-1$
	}
}
