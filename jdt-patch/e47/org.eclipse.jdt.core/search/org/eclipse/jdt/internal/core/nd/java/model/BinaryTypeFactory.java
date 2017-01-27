/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IDependent;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.nd.IReader;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.IndexException;
import org.eclipse.jdt.internal.core.nd.indexer.Indexer;
import org.eclipse.jdt.internal.core.nd.java.JavaIndex;
import org.eclipse.jdt.internal.core.nd.java.JavaNames;
import org.eclipse.jdt.internal.core.nd.java.NdResourceFile;
import org.eclipse.jdt.internal.core.nd.java.NdType;
import org.eclipse.jdt.internal.core.nd.java.TypeRef;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;
import org.eclipse.jdt.internal.core.util.Util;

public class BinaryTypeFactory {
	public static final class NotInIndexException extends Exception {
		private static final long serialVersionUID = 2859848007651528256L;

		public NotInIndexException() {
		}
	}

	/**
	 * Returns a descriptor for the given class within the given package fragment, or null if the fragment doesn't have
	 * a location on the filesystem.
	 */
	private static BinaryTypeDescriptor createDescriptor(PackageFragment pkg, ClassFile classFile) {
		String name = classFile.getName();
		IJavaElement root = pkg.getParent();
		IPath location = JavaIndex.getLocationForElement(root);
		String entryName = Util.concatWith(pkg.names, classFile.getElementName(), '/');
		char[] fieldDescriptor = CharArrayUtils.concat(new char[] { 'L' },
				Util.concatWith(pkg.names, name, '/').toCharArray(), new char[] { ';' });
		IPath workspacePath = root.getPath();
		String indexPath;

		if (location == null) {
			return null;
		}

		if (root instanceof JarPackageFragmentRoot) {
			// The old version returned this, but it doesn't conform to the spec on IBinaryType.getFileName():
			indexPath = root.getHandleIdentifier() + IDependent.JAR_FILE_ENTRY_SEPARATOR + entryName;
			// Version that conforms to the JavaDoc spec on IBinaryType.getFileName() -- note that this breaks
			// InlineMethodTests in the JDT UI project. Need to investigate why before using it.
			//indexPath = workspacePath.toString() + IDependent.JAR_FILE_ENTRY_SEPARATOR + entryName;
		} else {
			location = location.append(entryName);
			indexPath = workspacePath.append(entryName).toString();
			workspacePath = classFile.resource().getFullPath();
		}

		return new BinaryTypeDescriptor(location.toString().toCharArray(), fieldDescriptor,
				workspacePath.toString().toCharArray(), indexPath.toCharArray());
	}

	public static BinaryTypeDescriptor createDescriptor(IClassFile classFile) {
		ClassFile concreteClass = (ClassFile)classFile;
		PackageFragment parent = (PackageFragment) classFile.getParent();

		return createDescriptor(parent, concreteClass);
	}

	public static BinaryTypeDescriptor createDescriptor(IType type) {
		return createDescriptor(type.getClassFile());
	}

	public static IBinaryType create(IClassFile classFile, IProgressMonitor monitor) throws JavaModelException, ClassFormatException {
		BinaryTypeDescriptor descriptor = createDescriptor(classFile);
		return readType(descriptor, monitor);
	}

	/**
	 * Reads the given binary type. If the type can be found in the index with a fingerprint that exactly matches
	 * the file on disk, the type is read from the index. Otherwise the type is read from disk. Returns null if
	 * no such type exists.
	 * @throws ClassFormatException 
	 */
	public static IBinaryType readType(BinaryTypeDescriptor descriptor, IProgressMonitor monitor) throws JavaModelException, ClassFormatException {

		if (JavaIndex.isEnabled()) {
			try {
				return readFromIndex(JavaIndex.getIndex(), descriptor, monitor);
			} catch (NotInIndexException e) {
				// fall back to reading the zip file, below
			}
		}

		return rawReadType(descriptor, true);
	}

	public static ClassFileReader rawReadType(BinaryTypeDescriptor descriptor, boolean fullyInitialize) throws JavaModelException, ClassFormatException {
		try {
			return rawReadTypeTestForExists(descriptor, fullyInitialize, true);
		} catch (FileNotFoundException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
	}

	/**
	 * Read the class file from disk, circumventing the index's cache. This should only be used by callers
	 * that need to read information from the class file which aren't present in the index (such as method bodies).
	 * 
	 * @return the newly-created ClassFileReader or null if the given class file does not exist.
	 * @throws ClassFormatException if the class file existed but was corrupt
	 * @throws JavaModelException if unable to read the class file due to a transient failure
	 * @throws FileNotFoundException if the file does not exist
	 */
	public static ClassFileReader rawReadTypeTestForExists(BinaryTypeDescriptor descriptor, boolean fullyInitialize,
			boolean useInvalidArchiveCache) throws JavaModelException, ClassFormatException, FileNotFoundException {
		if (descriptor == null) {
			return null;
		}
		if (descriptor.isInJarFile()) {
			ZipFile zip = null;
			try {
				zip = JavaModelManager.getJavaModelManager().getZipFile(new Path(new String(descriptor.workspacePath)),
						useInvalidArchiveCache);
				char[] entryNameCharArray = CharArrayUtils.concat(
						JavaNames.fieldDescriptorToBinaryName(descriptor.fieldDescriptor), SuffixConstants.SUFFIX_class);
				String entryName = new String(entryNameCharArray);
				ZipEntry ze = zip.getEntry(entryName);
				if (ze != null) {
					byte contents[];
					try {
						contents = org.eclipse.jdt.internal.compiler.util.Util.getZipEntryByteContent(ze, zip);
					} catch (IOException ioe) {
						throw new JavaModelException(ioe, IJavaModelStatusConstants.IO_EXCEPTION);
					}
					return new ClassFileReader(contents, descriptor.indexPath, fullyInitialize);
				}
			} catch (CoreException e) {
				throw new JavaModelException(e);
			} finally {
				JavaModelManager.getJavaModelManager().closeZipFile(zip);
			}
		} else {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(new String(descriptor.workspacePath)));
			byte[] contents;
			try (InputStream stream = file.getContents(true)) {
				contents = org.eclipse.jdt.internal.compiler.util.Util.getInputStreamAsByteArray(stream, -1);
			} catch (CoreException e) {
				IStatus status = e.getStatus();
				if (status.getCode() == IResourceStatus.RESOURCE_NOT_FOUND) {
					throw new FileNotFoundException();
				}
				throw new JavaModelException(e);
			} catch (IOException e) {
				throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
			}
			return new ClassFileReader(contents, file.getFullPath().toString().toCharArray(), fullyInitialize);
		}
		return null;
	}

	/**
	 * Tries to read the given IBinaryType from the index. The return value is lightweight and may be cached
	 * with minimal memory cost. Returns an IBinaryType if the type was found in the index and the index
	 * was up-to-date. Throws a NotInIndexException if the index does not contain an up-to-date cache of the
	 * requested file. Returns null if the index contains an up-to-date cache of the requested file and it was
	 * able to determine that the requested class does not exist in that file.
	 */
	public static IBinaryType readFromIndex(JavaIndex index, BinaryTypeDescriptor descriptor, IProgressMonitor monitor) throws JavaModelException, NotInIndexException {
		// If the new index is enabled, check if we have this class file cached in the index already		
		char[] fieldDescriptor = descriptor.fieldDescriptor;

		Nd nd = index.getNd();

		if (descriptor.location != null) {
			// Acquire a read lock on the index
			try (IReader lock = nd.acquireReadLock()) {
				try {
					TypeRef typeRef = TypeRef.create(nd, descriptor.location, fieldDescriptor);
					NdType type = typeRef.get();

					if (type == null) {
						// If we couldn't find the type in the index, determine whether the cause is
						// that the type is known not to exist or whether the resource just hasn't
						// been indexed yet

						NdResourceFile resourceFile = index.getResourceFile(descriptor.location);
						if (index.isUpToDate(resourceFile)) {
							return null;
						}
						throw new NotInIndexException();
					}
					NdResourceFile resourceFile = type.getResourceFile();
					if (index.isUpToDate(resourceFile)) {
						IndexBinaryType result = new IndexBinaryType(typeRef, descriptor.indexPath);

						// We already have the database lock open and have located the element, so we may as
						// well prefetch the inexpensive attributes.
						result.initSimpleAttributes();

						return result;
					}
					throw new NotInIndexException();
				} catch (CoreException e) {
					throw new JavaModelException(e);
				}
			} catch (IndexException e) {
				Package.log("Index corruption detected. Rebuilding index.", e); //$NON-NLS-1$
				Indexer.getInstance().requestRebuildIndex();
			}
		}

		throw new NotInIndexException();
	}
}
