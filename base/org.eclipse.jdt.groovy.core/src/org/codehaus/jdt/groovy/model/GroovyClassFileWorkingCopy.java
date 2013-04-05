/*******************************************************************************
 * Copyright (c) 2012 VMWare, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMWare, Inc. - initial API and implementation
 *******************************************************************************/
package org.codehaus.jdt.groovy.model;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.BufferManager;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.ClassFileWorkingCopy;
import org.eclipse.jdt.internal.core.CompilationUnitElementInfo;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager.PerWorkingCopyInfo;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.util.Disassembler;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Working copy for groovy class files. Allows access to the ModuleNode for class files if the source is available. Copied from
 * {@link ClassFileWorkingCopy} Groovy changes marked
 * 
 * @author Andrew Eisenberg
 * @author Christian Dupuis
 * @created Dec 11, 2009
 */
public class GroovyClassFileWorkingCopy extends GroovyCompilationUnit {

	public ClassFile classFile;

	// GROOVY Change
	private final PerWorkingCopyInfo info;
	private CompilationUnitElementInfo elementInfo;
	private ModuleNode moduleNode;
	private ModuleNodeInfo moduleNodeInfo;

	// GROOVY End

	public GroovyClassFileWorkingCopy(ClassFile classFile, WorkingCopyOwner owner) {
		super((PackageFragment) classFile.getParent(), ((BinaryType) classFile.getType())
				.getSourceFileName(null/* no info available */), owner);
		this.classFile = classFile;
		// GROOVY Change
		info = new PerWorkingCopyInfo(this, null);
		// GROOVY End
	}

	public void commitWorkingCopy(boolean force, IProgressMonitor monitor) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, this));
	}

	public IBuffer getBuffer() throws JavaModelException {
		// GROOVY Always use the classFile's buffer
		// old
		// if (isWorkingCopy())
		// return super.getBuffer();
		// else
		// GROOVY end
		return this.classFile.getBuffer();
	}

	public char[] getContents() {
		try {
			IBuffer buffer = getBuffer();
			if (buffer == null)
				return CharOperation.NO_CHAR;
			char[] characters = buffer.getCharacters();
			if (characters == null)
				return CharOperation.NO_CHAR;
			return characters;
		} catch (JavaModelException e) {
			return CharOperation.NO_CHAR;
		}
	}

	public IPath getPath() {
		return this.classFile.getPath();
	}

	public IJavaElement getPrimaryElement(boolean checkOwner) {
		if (checkOwner && isPrimary())
			return this;
		return new ClassFileWorkingCopy(this.classFile, DefaultWorkingCopyOwner.PRIMARY);
	}

	public IResource resource(PackageFragmentRoot root) {
		if (root.isArchive())
			return root.resource(root);
		return this.classFile.resource(root);
	}

	/**
	 * @see Openable#openBuffer(IProgressMonitor, Object)
	 */
	protected IBuffer openBuffer(IProgressMonitor pm, Object info) throws JavaModelException {

		// create buffer
		IBuffer buffer = this.owner.createBuffer(this);
		if (buffer == null)
			return null;

		// set the buffer source
		if (buffer.getCharacters() == null) {
			IBuffer classFileBuffer = this.classFile.getBuffer();
			if (classFileBuffer != null) {
				buffer.setContents(classFileBuffer.getCharacters());
			} else {
				// Disassemble
				IClassFileReader reader = ToolFactory.createDefaultClassFileReader(this.classFile, IClassFileReader.ALL);
				Disassembler disassembler = new Disassembler();
				String contents = disassembler.disassemble(reader,
						Util.getLineSeparator("", getJavaProject()), ClassFileBytesDisassembler.WORKING_COPY); //$NON-NLS-1$
				buffer.setContents(contents);
			}
		}

		// add buffer to buffer cache
		BufferManager bufManager = getBufferManager();

		// GROOVY Change access to private member
		// old
		// bufManager.addBuffer(buffer);
		// new
		if (buffer.getContents() != null) {
			ReflectionUtils.executePrivateMethod(BufferManager.class,
					"addBuffer", new Class<?>[] { IBuffer.class }, bufManager, new Object[] { buffer }); //$NON-NLS-1$
		}
		// GROOVY End

		// listen to buffer changes
		buffer.addBufferChangedListener(this);

		return buffer;
	}

	protected void toStringName(StringBuffer buffer) {
		buffer.append(this.classFile.getElementName());
	}

	// GROOVY Change
	// all be a working copy
	// build structure only needs to happen once.
	@Override
	public PerWorkingCopyInfo getPerWorkingCopyInfo() {
		if (elementInfo == null) {
			try {
				elementInfo = (CompilationUnitElementInfo) createElementInfo();
				// FIXADE in E4.2, this method takes 3 args
				// when no longer supporting E3.7, then remove this
				// and uncomment the line below
				openWhenClosed(elementInfo, new NullProgressMonitor());
				// openWhenClosed(elementInfo, true, new NullProgressMonitor());
			} catch (JavaModelException e) {
				elementInfo = null;
				Activator.getDefault().getLog().log(e.getJavaModelStatus());
			}
		}
		return info;
	}

	/**
	 * Cache module node locally and not in the mapper
	 */
	@Override
	protected void maybeCacheModuleNode(PerWorkingCopyInfo perWorkingCopyInfo,
			GroovyCompilationUnitDeclaration compilationUnitDeclaration) {
		if (compilationUnitDeclaration != null) {
			moduleNode = compilationUnitDeclaration.getModuleNode();
			moduleNode.setDescription(this.name);
			JDTResolver resolver;
			if (ModuleNodeMapper.shouldStoreResovler()) {
				resolver = (JDTResolver) compilationUnitDeclaration.getCompilationUnit().getResolveVisitor();
			} else {
				resolver = null;
			}

			moduleNodeInfo = new ModuleNodeInfo(moduleNode, resolver);
		}
	}

	@Override
	public ModuleNodeInfo getModuleInfo(boolean force) {
		return moduleNodeInfo;
	}

	@Override
	public ModuleNodeInfo getNewModuleInfo() {
		return moduleNodeInfo;
	}

	/**
	 * ModuleNode is not cached in the Mapper, but rather cached locally
	 */
	@Override
	public ModuleNode getModuleNode() {
		// ensure moduleNode is initialized
		getPerWorkingCopyInfo();
		return moduleNode;
	}

	@Override
	public IResource resource() {
		return getJavaProject().getResource();
	}

	@Override
	public char[] getFileName() {
		return name.toCharArray(); 
	}

	@Override
	public boolean isOnBuildPath() {
		// a call to super.isOnBuildPath() will always return false,
		// but it should be true
		return true;
	}
	// GROOVY End
}
