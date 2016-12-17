// GROOVY PATCHED
/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 458577 - IClassFile.getWorkingCopy() may lead to NPE in BecomeWorkingCopyOperation
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *								Bug 462768 - [null] NPE when using linked folder for external annotations
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.codehaus.jdt.groovy.integration.LanguageSupportFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationDecorator;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationProvider;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.nd.java.JavaNames;
import org.eclipse.jdt.internal.core.nd.java.model.BinaryTypeDescriptor;
import org.eclipse.jdt.internal.core.nd.java.model.BinaryTypeFactory;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @see IClassFile
 */

@SuppressWarnings({"rawtypes"})
public class ClassFile extends Openable implements IClassFile, SuffixConstants {

	protected String name;
	protected BinaryType binaryType = null;

	private IPath externalAnnotationBase;

/*
 * Creates a handle to a class file.
 */
protected ClassFile(PackageFragment parent, String nameWithoutExtension) {
	super(parent);
	this.name = nameWithoutExtension;
}

/*
 * @see IClassFile#becomeWorkingCopy(IProblemRequestor, WorkingCopyOwner, IProgressMonitor)
 */
public ICompilationUnit becomeWorkingCopy(IProblemRequestor problemRequestor, WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException {
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	CompilationUnit workingCopy = new ClassFileWorkingCopy(this, owner == null ? DefaultWorkingCopyOwner.PRIMARY : owner);
	JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo = manager.getPerWorkingCopyInfo(workingCopy, false/*don't create*/, true /*record usage*/, null/*no problem requestor needed*/);
	if (perWorkingCopyInfo == null) {
		// close cu and its children
		close();

		BecomeWorkingCopyOperation operation = new BecomeWorkingCopyOperation(workingCopy, problemRequestor);
		operation.runOperation(monitor);

		return workingCopy;
	}
	return perWorkingCopyInfo.workingCopy;
}

/**
 * Creates the children elements for this class file adding the resulting
 * new handles and info objects to the newElements table. Returns true
 * if successful, or false if an error is encountered parsing the class file.
 *
 * @see Openable
 * @see Signature
 */
@Override
protected boolean buildStructure(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws JavaModelException {
	IBinaryType typeInfo = getBinaryTypeInfo();
	if (typeInfo == null) {
		// The structure of a class file is unknown if a class file format errors occurred
		//during the creation of the diet class file representative of this ClassFile.
		info.setChildren(new IJavaElement[] {});
		return false;
	}

	// Make the type
	IType type = getType();
	info.setChildren(new IJavaElement[] {type});
	newElements.put(type, typeInfo);

	// Read children
	((ClassFileInfo) info).readBinaryChildren(this, (HashMap) newElements, typeInfo);

	return true;
}
/**
 * @see ICodeAssist#codeComplete(int, ICompletionRequestor)
 * @deprecated
 */
@Deprecated
public void codeComplete(int offset, ICompletionRequestor requestor) throws JavaModelException {
	codeComplete(offset, requestor, DefaultWorkingCopyOwner.PRIMARY);
}
/**
 * @see ICodeAssist#codeComplete(int, ICompletionRequestor, WorkingCopyOwner)
 * @deprecated
 */
@Deprecated
public void codeComplete(int offset, ICompletionRequestor requestor, WorkingCopyOwner owner) throws JavaModelException {
	if (requestor == null) {
		throw new IllegalArgumentException("Completion requestor cannot be null"); //$NON-NLS-1$
	}
	codeComplete(offset, new org.eclipse.jdt.internal.codeassist.CompletionRequestorWrapper(requestor), owner);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.ICodeAssist#codeComplete(int, org.eclipse.jdt.core.CompletionRequestor)
 */
public void codeComplete(int offset, CompletionRequestor requestor) throws JavaModelException {
	codeComplete(offset, requestor, DefaultWorkingCopyOwner.PRIMARY);
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.ICodeAssist#codeComplete(int, org.eclipse.jdt.core.CompletionRequestor, org.eclipse.core.runtime.IProgressMonitor)
 */
public void codeComplete(int offset, CompletionRequestor requestor, IProgressMonitor monitor) throws JavaModelException {
	codeComplete(offset, requestor, DefaultWorkingCopyOwner.PRIMARY, monitor);
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.ICodeAssist#codeComplete(int, org.eclipse.jdt.core.CompletionRequestor, org.eclipse.jdt.core.WorkingCopyOwner)
 */
public void codeComplete(int offset, CompletionRequestor requestor, WorkingCopyOwner owner) throws JavaModelException {
	codeComplete(offset, requestor, owner, null);
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.ICodeAssist#codeComplete(int, org.eclipse.jdt.core.CompletionRequestor, org.eclipse.jdt.core.WorkingCopyOwner, org.eclipse.core.runtime.IProgressMonitor)
 */
public void codeComplete(int offset, CompletionRequestor requestor, WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException {
	String source = getSource();
	if (source != null) {
		BinaryType type = (BinaryType) getType();
		BasicCompilationUnit cu =
			new BasicCompilationUnit(
				getSource().toCharArray(),
				null,
				type.sourceFileName((IBinaryType) type.getElementInfo()),
				getJavaProject()); // use project to retrieve corresponding .java IFile
		codeComplete(cu, cu, offset, requestor, owner, null/*extended context isn't computed*/, monitor);
	}
}

/**
 * @see ICodeAssist#codeSelect(int, int)
 */
public IJavaElement[] codeSelect(int offset, int length) throws JavaModelException {
	return codeSelect(offset, length, DefaultWorkingCopyOwner.PRIMARY);
}
/**
 * @see ICodeAssist#codeSelect(int, int, WorkingCopyOwner)
 */
public IJavaElement[] codeSelect(int offset, int length, WorkingCopyOwner owner) throws JavaModelException {
	IBuffer buffer = getBuffer();
	char[] contents;
	if (buffer != null && (contents = buffer.getCharacters()) != null) {
	    BinaryType type = (BinaryType) getType();
		// GROOVY edit
		//BasicCompilationUnit cu = new BasicCompilationUnit(contents, null, type.sourceFileName((IBinaryType) type.getElementInfo()));
		// handle code select for Groovy files differently
		IBinaryType typeInfo = (IBinaryType) type.getElementInfo();
		if (LanguageSupportFactory.isInterestingBinary(type, typeInfo)) {
			return LanguageSupportFactory.binaryCodeSelect(this, offset, length, owner);
		}
		BasicCompilationUnit cu = new BasicCompilationUnit(contents, null, type.sourceFileName(typeInfo));
		// GROOVY end
		return super.codeSelect(cu, offset, length, owner);
	} else {
		//has no associated souce
		return new IJavaElement[] {};
	}
}
/**
 * Returns a new element info for this element.
 */
@Override
protected Object createElementInfo() {
	return new ClassFileInfo();
}
@Override
public boolean equals(Object o) {
	if (!(o instanceof ClassFile)) return false;
	ClassFile other = (ClassFile) o;
	return this.name.equals(other.name) && this.parent.equals(other.parent);
}
public boolean existsUsingJarTypeCache() {
	if (getPackageFragmentRoot().isArchive()) {
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		IType type = getType();
		Object info = manager.getInfo(type);
		if (info == JavaModelCache.NON_EXISTING_JAR_TYPE_INFO)
			return false;
		else if (info != null)
			return true;
		// info is null
		JavaElementInfo parentInfo = (JavaElementInfo) manager.getInfo(getParent());
		if (parentInfo != null) {
			// if parent is open, this class file must be in its children
			IJavaElement[] children = parentInfo.getChildren();
			for (int i = 0, length = children.length; i < length; i++) {
				IJavaElement child = children[i];
				if (child instanceof ClassFile && this.name.equals(((ClassFile) child).name))
					return true;
			}
			return false;
		}
		try {
			info = getJarBinaryTypeInfo();
		} catch (CoreException e) {
			// leave info null
		} catch (IOException e) {
			// leave info null
		} catch (ClassFormatException e) {
			// leave info null
		}
		manager.putJarTypeInfo(type, info == null ? JavaModelCache.NON_EXISTING_JAR_TYPE_INFO : info);
		return info != null;
	} else
		return exists();
}

/**
 * Finds the deepest <code>IJavaElement</code> in the hierarchy of
 * <code>elt</elt>'s children (including <code>elt</code> itself)
 * which has a source range that encloses <code>position</code>
 * according to <code>mapper</code>.
 */
protected IJavaElement findElement(IJavaElement elt, int position, SourceMapper mapper) {
	SourceRange range = mapper.getSourceRange(elt);
	if (range == null || position < range.getOffset() || range.getOffset() + range.getLength() - 1 < position) {
		return null;
	}
	if (elt instanceof IParent) {
		try {
			IJavaElement[] children = ((IParent) elt).getChildren();
			for (int i = 0; i < children.length; i++) {
				IJavaElement match = findElement(children[i], position, mapper);
				if (match != null) {
					return match;
				}
			}
		} catch (JavaModelException npe) {
			// elt doesn't exist: return the element
		}
	}
	return elt;
}
/**
 * @see ITypeRoot#findPrimaryType()
 */
public IType findPrimaryType() {
	IType primaryType= getType();
	if (primaryType.exists()) {
		return primaryType;
	}
	return null;
}
@Override
public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
	return getType().getAttachedJavadoc(monitor);
}
/**
 * Returns the <code>ClassFileReader</code>specific for this IClassFile, based
 * on its underlying resource, or <code>null</code> if unable to create
 * the diet class file.
 * There are two cases to consider:<ul>
 * <li>a class file corresponding to an IFile resource</li>
 * <li>a class file corresponding to a zip entry in a JAR</li>
 * </ul>
 *
 * @exception JavaModelException when the IFile resource or JAR is not available
 * or when this class file is not present in the JAR
 */
public IBinaryType getBinaryTypeInfo() throws JavaModelException {
	try {
		IBinaryType info = getJarBinaryTypeInfo();
		if (info == null) {
			throw newNotPresentException();
		}
		return info;
	} catch (ClassFormatException cfe) {
		//the structure remains unknown
		if (JavaCore.getPlugin().isDebugging()) {
			cfe.printStackTrace(System.err);
		}
		return null;
	} catch (IOException ioe) {
		throw new JavaModelException(ioe, IJavaModelStatusConstants.IO_EXCEPTION);
	} catch (CoreException e) {
		if (e instanceof JavaModelException) {
			throw (JavaModelException)e;
		} else {
			throw new JavaModelException(e);
		}
	}
}

public byte[] getBytes() throws JavaModelException {
	JavaElement pkg = (JavaElement) getParent();
	if (pkg instanceof JarPackageFragment) {
		JarPackageFragmentRoot root = (JarPackageFragmentRoot) pkg.getParent();
		ZipFile zip = null;
		try {
			zip = root.getJar();
			String entryName = Util.concatWith(((PackageFragment) pkg).names, getElementName(), '/');
			ZipEntry ze = zip.getEntry(entryName);
			if (ze != null) {
				return org.eclipse.jdt.internal.compiler.util.Util.getZipEntryByteContent(ze, zip);
			}
			throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
		} catch (IOException ioe) {
			throw new JavaModelException(ioe, IJavaModelStatusConstants.IO_EXCEPTION);
		} catch (CoreException e) {
			if (e instanceof JavaModelException) {
				throw (JavaModelException)e;
			} else {
				throw new JavaModelException(e);
			}
		} finally {
			JavaModelManager.getJavaModelManager().closeZipFile(zip);
		}
	} else {
		IFile file = (IFile) resource();
		return Util.getResourceContentsAsByteArray(file);
	}
}

public String getName() {
	return this.name;
}

private IBinaryType getJarBinaryTypeInfo() throws CoreException, IOException, ClassFormatException {
	BinaryTypeDescriptor descriptor = BinaryTypeFactory.createDescriptor(this);

	if (descriptor == null) {
		return null;
	}

	IBinaryType result = BinaryTypeFactory.readType(descriptor, null);

	if (result == null) {
		return null;
	}

	// TODO(sxenos): setup the external annotation provider if the IBinaryType came from the index
	// TODO(sxenos): the old code always passed null as the third argument to setupExternalAnnotationProvider,
	// but this looks like a bug. I've preserved it for now but we need to figure out what was supposed to go
	// there.
	PackageFragment pkg = (PackageFragment) getParent();
	IJavaElement grandparent = pkg.getParent();
	if (grandparent instanceof JarPackageFragmentRoot) {
		JarPackageFragmentRoot root = (JarPackageFragmentRoot) grandparent;

		if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
			JavaProject javaProject = (JavaProject) getAncestor(IJavaElement.JAVA_PROJECT);
			IClasspathEntry entry = javaProject.getClasspathEntryFor(getPath());
			if (entry != null) {
				String entryName = new String(CharArrayUtils.concat(
						JavaNames.fieldDescriptorToBinaryName(descriptor.fieldDescriptor), SuffixConstants.SUFFIX_CLASS));
				IProject project = javaProject.getProject();
				IPath externalAnnotationPath = ClasspathEntry.getExternalAnnotationPath(entry, project, false); // unresolved for use in ExternalAnnotationTracker
				if (externalAnnotationPath != null) {
					result = setupExternalAnnotationProvider(project, externalAnnotationPath, null, result, 
						entryName.substring(0, entryName.length() - SuffixConstants.SUFFIX_CLASS.length));
				} else if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					result = new ExternalAnnotationDecorator(result, true);
				}
			}
		}
	}

	return result;
}

private IBinaryType setupExternalAnnotationProvider(IProject project, final IPath externalAnnotationPath,
		ZipFile annotationZip, IBinaryType reader, final String typeName)
{
	IBinaryType result = reader;
	// try resolve path within the workspace:
	IWorkspaceRoot root = project.getWorkspace().getRoot();
	IResource resource;
	if (externalAnnotationPath.segmentCount() == 1) {
		resource = root.getProject(externalAnnotationPath.lastSegment());
	} else {
		resource = root.getFolder(externalAnnotationPath);
		if (!resource.exists())
			resource = root.getFile(externalAnnotationPath);
	}
	String resolvedPath;
	if (resource.exists()) {
		if (resource.isVirtual()) {
			Util.log(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID,
					"Virtual resource "+externalAnnotationPath+" cannot be used as annotationpath for project "+project.getName())); //$NON-NLS-1$ //$NON-NLS-2$
			return reader;
		}
		resolvedPath = resource.getLocation().toString(); // workspace lookup succeeded -> resolve it
	} else {
		resolvedPath = externalAnnotationPath.toString(); // not in workspace, use as is
	}
	try {
		if (annotationZip == null) {
			annotationZip = ExternalAnnotationDecorator.getAnnotationZipFile(resolvedPath, new ExternalAnnotationDecorator.ZipFileProducer() {
				@Override public ZipFile produce() throws IOException {
					try {
						return JavaModelManager.getJavaModelManager().getZipFile(externalAnnotationPath); // use (absolute, but) unresolved path here
					} catch (CoreException e) {
						throw new IOException("Failed to read annotation file for "+typeName+" from "+externalAnnotationPath.toString(), e); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}});
		}

		ExternalAnnotationProvider annotationProvider = ExternalAnnotationDecorator
				.externalAnnotationProvider(resolvedPath, typeName, annotationZip);
		result = new ExternalAnnotationDecorator(reader, annotationProvider);
	} catch (IOException e) {
		Util.log(e);
		return result;
	}
	if (annotationZip == null) {
		// Additional change listening for individual types only when annotations are in individual files.
		// Note that we also listen for classes that don't yet have an annotation file, to detect its creation
		this.externalAnnotationBase = externalAnnotationPath; // remember so we can unregister later
		ExternalAnnotationTracker.registerClassFile(externalAnnotationPath, new Path(typeName), this);
	}
	return result;
}
void closeAndRemoveFromJarTypeCache() throws JavaModelException {
	super.close();
	// triggered when external annotations have changed we need to recreate this class file
	JavaModelManager.getJavaModelManager().removeFromJarTypeCache(this.binaryType);
}
@Override
public void close() throws JavaModelException {
	if (this.externalAnnotationBase != null) {
		String entryName = Util.concatWith(((PackageFragment) getParent()).names, this.name, '/');
		ExternalAnnotationTracker.unregisterClassFile(this.externalAnnotationBase, new Path(entryName));
	}
	super.close();
}
@Override
public IBuffer getBuffer() throws JavaModelException {
	IStatus status = validateClassFile();
	if (status.isOK()) {
		return super.getBuffer();
	} else {
		switch (status.getCode()) {
		case IJavaModelStatusConstants.ELEMENT_NOT_ON_CLASSPATH: // don't throw a JavaModelException to be able to open .class file outside the classpath (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=138507 )
		case IJavaModelStatusConstants.INVALID_ELEMENT_TYPES: // don't throw a JavaModelException to be able to open .class file in proj==src case without source (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=221904 )
			return null;
		default:
			throw new JavaModelException((IJavaModelStatus) status);
		}
	}
}
/**
 * @see IMember
 */
@Override
public IClassFile getClassFile() {
	return this;
}
/**
 * @see IMember#getTypeRoot()
 */
public ITypeRoot getTypeRoot() {
	return this;
}
/**
 * A class file has a corresponding resource unless it is contained
 * in a jar.
 *
 * @see IJavaElement
 */
@Override
public IResource getCorrespondingResource() throws JavaModelException {
	IPackageFragmentRoot root= (IPackageFragmentRoot)getParent().getParent();
	if (root.isArchive()) {
		return null;
	} else {
		return getUnderlyingResource();
	}
}
/**
 * @see IClassFile
 */
public IJavaElement getElementAt(int position) throws JavaModelException {
	IJavaElement parentElement = getParent();
	while (parentElement.getElementType() != IJavaElement.PACKAGE_FRAGMENT_ROOT) {
		parentElement = parentElement.getParent();
	}
	PackageFragmentRoot root = (PackageFragmentRoot) parentElement;
	SourceMapper mapper = root.getSourceMapper();
	if (mapper == null) {
		return null;
	} else {
		// ensure this class file's buffer is open so that source ranges are computed
		getBuffer();

		IType type = getType();
		return findElement(type, position, mapper);
	}
}
public IJavaElement getElementAtConsideringSibling(int position) throws JavaModelException {
	IPackageFragment fragment = (IPackageFragment)getParent();
	PackageFragmentRoot root = (PackageFragmentRoot) fragment.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
	SourceMapper mapper = root.getSourceMapper();
	if (mapper == null) {
		return null;
	} else {
		int index = this.name.indexOf('$');
		int prefixLength = index < 0 ? this.name.length() : index;

		IType type = null;
		int start = -1;
		int end = Integer.MAX_VALUE;
		IJavaElement[] children = fragment.getChildren();
		for (int i = 0; i < children.length; i++) {
			String childName = children[i].getElementName();

			int childIndex = childName.indexOf('$');
			int childPrefixLength = childIndex < 0 ? childName.indexOf('.') : childIndex;
			if (prefixLength == childPrefixLength && this.name.regionMatches(0, childName, 0, prefixLength)) {
				IClassFile classFile = (IClassFile) children[i];

				// ensure this class file's buffer is open so that source ranges are computed
				classFile.getBuffer();

				SourceRange range = mapper.getSourceRange(classFile.getType());
				if (range == SourceMapper.UNKNOWN_RANGE) continue;
				int newStart = range.getOffset();
				int newEnd = newStart + range.getLength() - 1;
				if(newStart > start && newEnd < end
						&& newStart <= position && newEnd >= position) {
					type = classFile.getType();
					start = newStart;
					end = newEnd;
				}
			}
		}
		if(type != null) {
			return findElement(type, position, mapper);
		}
		return null;
	}
}
@Override
public String getElementName() {
	return this.name + SuffixConstants.SUFFIX_STRING_class;
}
/**
 * @see IJavaElement
 */
public int getElementType() {
	return CLASS_FILE;
}
/*
 * @see JavaElement
 */
@Override
public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner owner) {
	switch (token.charAt(0)) {
		case JEM_TYPE:
			if (!memento.hasMoreTokens()) return this;
			String typeName = memento.nextToken();
			JavaElement type = new BinaryType(this, typeName);
			return type.getHandleFromMemento(memento, owner);
	}
	return null;
}
/**
 * @see JavaElement#getHandleMemento()
 */
@Override
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_CLASSFILE;
}
/*
 * @see IJavaElement
 */
public IPath getPath() {
	PackageFragmentRoot root = getPackageFragmentRoot();
	if (root.isArchive()) {
		return root.getPath();
	} else {
		return getParent().getPath().append(getElementName());
	}
}
/*
 * @see IJavaElement
 */
@Override
public IResource resource(PackageFragmentRoot root) {
	return ((IContainer) ((Openable) this.parent).resource(root)).getFile(new Path(getElementName()));
}
/**
 * @see ISourceReference
 */
public String getSource() throws JavaModelException {
	IBuffer buffer = getBuffer();
	if (buffer == null) {
		return null;
	}
	return buffer.getContents();
}
/**
 * @see ISourceReference
 */
public ISourceRange getSourceRange() throws JavaModelException {
	IBuffer buffer = getBuffer();
	if (buffer != null) {
		String contents = buffer.getContents();
		if (contents == null) return null;
		return new SourceRange(0, contents.length());
	} else {
		return null;
	}
}
/*
 * Returns the name of the toplevel type of this class file.
 */
public String getTopLevelTypeName() {
    String topLevelTypeName = getElementName();
    int firstDollar = topLevelTypeName.indexOf('$');
    if (firstDollar != -1) {
        topLevelTypeName = topLevelTypeName.substring(0, firstDollar);
    } else {
        topLevelTypeName = topLevelTypeName.substring(0, topLevelTypeName.length()-SUFFIX_CLASS.length);
    }
    return topLevelTypeName;
}
/**
 * @see IClassFile
 */
public IType getType() {
	if (this.binaryType == null) {
		this.binaryType = new BinaryType(this, getTypeName());
	}
	return this.binaryType;
}
public String getTypeName() {
	// Internal class file name doesn't contain ".class" file extension
	int lastDollar = this.name.lastIndexOf('$');
	return lastDollar > -1 ? Util.localTypeName(this.name, lastDollar, this.name.length()) : this.name;
}
/*
 * @see IClassFile
 */
public ICompilationUnit getWorkingCopy(WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException {
	CompilationUnit workingCopy = new ClassFileWorkingCopy(this, owner == null ? DefaultWorkingCopyOwner.PRIMARY : owner);
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo =
		manager.getPerWorkingCopyInfo(workingCopy, false/*don't create*/, true/*record usage*/, null/*not used since don't create*/);
	if (perWorkingCopyInfo != null) {
		return perWorkingCopyInfo.getWorkingCopy(); // return existing handle instead of the one created above
	}
	BecomeWorkingCopyOperation op = new BecomeWorkingCopyOperation(workingCopy, null);
	op.runOperation(monitor);
	return workingCopy;
}
/**
 * @see IClassFile
 * @deprecated
 */
@Deprecated
public IJavaElement getWorkingCopy(IProgressMonitor monitor, org.eclipse.jdt.core.IBufferFactory factory) throws JavaModelException {
	return getWorkingCopy(BufferFactoryWrapper.create(factory), monitor);
}
/**
 * @see Openable
 */
@Override
protected boolean hasBuffer() {
	return true;
}
@Override
public int hashCode() {
	return Util.combineHashCodes(this.name.hashCode(), this.parent.hashCode());
}
/**
 * @see IClassFile
 */
public boolean isClass() throws JavaModelException {
	return getType().isClass();
}
/**
 * @see IClassFile
 */
public boolean isInterface() throws JavaModelException {
	return getType().isInterface();
}
/**
 * Returns true - class files are always read only.
 */
@Override
public boolean isReadOnly() {
	return true;
}
private IStatus validateClassFile() {
	IPackageFragmentRoot root = getPackageFragmentRoot();
	try {
		if (root.getKind() != IPackageFragmentRoot.K_BINARY)
			return new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, root);
	} catch (JavaModelException e) {
		return e.getJavaModelStatus();
	}
	IJavaProject project = getJavaProject();
	return JavaConventions.validateClassFileName(getElementName(), project.getOption(JavaCore.COMPILER_SOURCE, true), project.getOption(JavaCore.COMPILER_COMPLIANCE, true));
}
/**
 * Opens and returns buffer on the source code associated with this class file.
 * Maps the source code to the children elements of this class file.
 * If no source code is associated with this class file,
 * <code>null</code> is returned.
 *
 * @see Openable
 */
@Override
protected IBuffer openBuffer(IProgressMonitor pm, Object info) throws JavaModelException {
	// Check the cache for the top-level type first
	IType outerMostEnclosingType = getOuterMostEnclosingType();
	IBuffer buffer = getBufferManager().getBuffer(outerMostEnclosingType.getClassFile());
	if (buffer == null) {
		SourceMapper mapper = getSourceMapper();
		IBinaryType typeInfo = info instanceof IBinaryType ? (IBinaryType) info : null;
		if (mapper != null) {
			buffer = mapSource(mapper, typeInfo, outerMostEnclosingType.getClassFile());
		}
	}
	return buffer;
}
/** Loads the buffer via SourceMapper, and maps it in SourceMapper */
private IBuffer mapSource(SourceMapper mapper, IBinaryType info, IClassFile bufferOwner) {
	char[] contents = mapper.findSource(getType(), info);
	if (contents != null) {
		// create buffer
		IBuffer buffer = BufferManager.createBuffer(bufferOwner);
		if (buffer == null) return null;
		BufferManager bufManager = getBufferManager();
		bufManager.addBuffer(buffer);

		// set the buffer source
		if (buffer.getCharacters() == null){
			buffer.setContents(contents);
		}

		// listen to buffer changes
		buffer.addBufferChangedListener(this);

		// do the source mapping
		mapper.mapSource(getOuterMostEnclosingType(), contents, info);

		return buffer;
	} else {
		// create buffer
		IBuffer buffer = BufferManager.createNullBuffer(bufferOwner);
		if (buffer == null) return null;
		BufferManager bufManager = getBufferManager();
		bufManager.addBuffer(buffer);

		// listen to buffer changes
		buffer.addBufferChangedListener(this);
		return buffer;
	}
}
/* package */ static String simpleName(char[] className) {
	if (className == null)
		return null;
	String simpleName = new String(unqualifiedName(className));
	int lastDollar = simpleName.lastIndexOf('$');
	if (lastDollar != -1)
		return Util.localTypeName(simpleName, lastDollar, simpleName.length());
	else
		return simpleName;
}

/** Returns the type of the top-level declaring class used to find the source code */
private IType getOuterMostEnclosingType() {
	IType type = getType();
	IType enclosingType = type.getDeclaringType();
	while (enclosingType != null) {
		type = enclosingType;
		enclosingType = type.getDeclaringType();
	}
	return type;
}

/**
 * Returns the Java Model representation of the given name
 * which is provided in diet class file format, or <code>null</code>
 * if the given name is <code>null</code>.
 *
 * <p><code>ClassFileReader</code> format is similar to "java/lang/Object",
 * and corresponding Java Model format is "java.lang.Object".
 */

public static char[] translatedName(char[] name) {
	if (name == null)
		return null;
	int nameLength = name.length;
	char[] newName= new char[nameLength];
	for (int i= 0; i < nameLength; i++) {
		if (name[i] == '/') {
			newName[i]= '.';
		} else {
			newName[i]= name[i];
		}
	}
	return newName;
}
/**
 * Returns the Java Model representation of the given names
 * which are provided in diet class file format, or <code>null</code>
 * if the given names are <code>null</code>.
 *
 * <p><code>ClassFileReader</code> format is similar to "java/lang/Object",
 * and corresponding Java Model format is "java.lang.Object".
 */

/* package */ static char[][] translatedNames(char[][] names) {
	if (names == null)
		return null;
	int length = names.length;
	char[][] newNames = new char[length][];
	for(int i = 0; i < length; i++) {
		newNames[i] = translatedName(names[i]);
	}
	return newNames;
}
/**
 * Returns the Java Model format of the unqualified class name for the
 * given className which is provided in diet class file format,
 * or <code>null</code> if the given className is <code>null</code>.
 * (This removes the package name, but not enclosing type names).
 *
 * <p><code>ClassFileReader</code> format is similar to "java/lang/Object",
 * and corresponding Java Model simple name format is "Object".
 */

/* package */ static char[] unqualifiedName(char[] className) {
	if (className == null)
		return null;
	int count = 0;
	for (int i = className.length - 1; i > -1; i--) {
		if (className[i] == '/') {
			char[] name = new char[count];
			System.arraycopy(className, i + 1, name, 0, count);
			return name;
		}
		count++;
	}
	return className;
}

/**
 * @see ICodeAssist#codeComplete(int, ICodeCompletionRequestor)
 * @deprecated - should use codeComplete(int, ICompletionRequestor) instead
 */
@Deprecated
public void codeComplete(int offset, final org.eclipse.jdt.core.ICodeCompletionRequestor requestor) throws JavaModelException {

	if (requestor == null){
		codeComplete(offset, (ICompletionRequestor)null);
		return;
	}
	codeComplete(
		offset,
		new ICompletionRequestor(){
			public void acceptAnonymousType(char[] superTypePackageName,char[] superTypeName, char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] completionName,int modifiers,int completionStart,int completionEnd, int relevance) {
				// ignore
			}
			public void acceptClass(char[] packageName, char[] className, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
				requestor.acceptClass(packageName, className, completionName, modifiers, completionStart, completionEnd);
			}
			public void acceptError(IProblem error) {
				// was disabled in 1.0
			}
			public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] fieldName, char[] typePackageName, char[] typeName, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
				requestor.acceptField(declaringTypePackageName, declaringTypeName, fieldName, typePackageName, typeName, completionName, modifiers, completionStart, completionEnd);
			}
			public void acceptInterface(char[] packageName,char[] interfaceName,char[] completionName,int modifiers,int completionStart,int completionEnd, int relevance) {
				requestor.acceptInterface(packageName, interfaceName, completionName, modifiers, completionStart, completionEnd);
			}
			public void acceptKeyword(char[] keywordName,int completionStart,int completionEnd, int relevance){
				requestor.acceptKeyword(keywordName, completionStart, completionEnd);
			}
			public void acceptLabel(char[] labelName,int completionStart,int completionEnd, int relevance){
				requestor.acceptLabel(labelName, completionStart, completionEnd);
			}
			public void acceptLocalVariable(char[] localVarName,char[] typePackageName,char[] typeName,int modifiers,int completionStart,int completionEnd, int relevance){
				// ignore
			}
			public void acceptMethod(char[] declaringTypePackageName,char[] declaringTypeName,char[] selector,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] returnTypePackageName,char[] returnTypeName,char[] completionName,int modifiers,int completionStart,int completionEnd, int relevance){
				// skip parameter names
				requestor.acceptMethod(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames, returnTypePackageName, returnTypeName, completionName, modifiers, completionStart, completionEnd);
			}
			public void acceptMethodDeclaration(char[] declaringTypePackageName,char[] declaringTypeName,char[] selector,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] returnTypePackageName,char[] returnTypeName,char[] completionName,int modifiers,int completionStart,int completionEnd, int relevance){
				// ignore
			}
			public void acceptModifier(char[] modifierName,int completionStart,int completionEnd, int relevance){
				requestor.acceptModifier(modifierName, completionStart, completionEnd);
			}
			public void acceptPackage(char[] packageName,char[] completionName,int completionStart,int completionEnd, int relevance){
				requestor.acceptPackage(packageName, completionName, completionStart, completionEnd);
			}
			public void acceptType(char[] packageName,char[] typeName,char[] completionName,int completionStart,int completionEnd, int relevance){
				requestor.acceptType(packageName, typeName, completionName, completionStart, completionEnd);
			}
			public void acceptVariableName(char[] typePackageName,char[] typeName,char[] varName,char[] completionName,int completionStart,int completionEnd, int relevance){
				// ignore
			}
		});
}

@Override
protected IStatus validateExistence(IResource underlyingResource) {
	// check whether the class file can be opened
	IStatus status = validateClassFile();
	if (!status.isOK())
		return status;
	if (underlyingResource != null) {
		if (!underlyingResource.isAccessible())
			return newDoesNotExistStatus();
		PackageFragmentRoot root;
		if ((underlyingResource instanceof IFolder) && (root = getPackageFragmentRoot()).isArchive()) { // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=204652
			return root.newDoesNotExistStatus();
		}
	}
	return JavaModelStatus.VERIFIED_OK;
}
public ISourceRange getNameRange() {
	return null;
}
}
