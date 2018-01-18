/*******************************************************************************
 * Copyright (c) 2017, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModule.IModuleReference;
import org.eclipse.jdt.internal.compiler.env.IModule.IPackageExport;
import org.eclipse.jdt.internal.compiler.env.IModule.IService;

public abstract class AbstractModule extends NamedMember implements IModuleDescription {
	
	/**
	 * Handle for an automatic module.
	 *
	 * <p>Note, that by definition this is mostly a fake, only {@link #getElementName()} provides a useful value.</p>
	 */
	static class AutoModule extends AbstractModule {
	
		public AutoModule(JavaElement parent, String name) {
			super(parent, name);
		}
		@Override
		public IJavaElement[] getChildren() throws JavaModelException {
			return JavaElement.NO_ELEMENTS; // may later answer computed details
		}
		@Override
		public int getFlags() throws JavaModelException {
			return 0;
		}
		@Override
		public ITypeRoot getTypeRoot() {
			return null; // has no real CompilationUnit nor ClassFile
		}
		@Override
		public IModuleReference[] getRequiredModules() throws JavaModelException {
			return ModuleDescriptionInfo.NO_REQUIRES;
		}
		@Override
		protected void toStringContent(StringBuffer buffer, String lineDelimiter) throws JavaModelException {
			buffer.append("automatic module "); //$NON-NLS-1$
			buffer.append(this.name);
		}
	}
	
	protected AbstractModule(JavaElement parent, String name) {
		super(parent, name);
	}
	protected IModule getModuleInfo() throws JavaModelException {
		return (IModule) getElementInfo();
	}
	public IModuleReference[] getRequiredModules() throws JavaModelException {
		return getModuleInfo().requires();
	}
	public IPackageExport[] getExportedPackages() throws JavaModelException {
		return getModuleInfo().exports();
	}
	public IService[] getProvidedServices() throws JavaModelException {
		return getModuleInfo().provides();
	}
	public char[][] getUsedServices() throws JavaModelException {
		return getModuleInfo().uses();
	}
	public IPackageExport[] getOpenedPackages() throws JavaModelException {
		return getModuleInfo().opens();
	}
	public String getKey(boolean forceOpen) throws JavaModelException {
		return getKey(this, forceOpen);
	}
	public String toString(String lineDelimiter) {
		StringBuffer buffer = new StringBuffer();
		try {
			toStringContent(buffer, lineDelimiter);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buffer.toString();
	}
	protected void toStringContent(StringBuffer buffer, String lineDelimiter) throws JavaModelException {
		IPackageExport[] exports = getExportedPackages();
		IModuleReference[] requires = getRequiredModules();
		buffer.append("module "); //$NON-NLS-1$
		buffer.append(this.name).append(' ');
		buffer.append('{').append(lineDelimiter);
		if (exports != null) {
			for(int i = 0; i < exports.length; i++) {
				buffer.append("\texports "); //$NON-NLS-1$
				buffer.append(exports[i].toString());
				buffer.append(lineDelimiter);
			}
		}
		buffer.append(lineDelimiter);
		if (requires != null) {
			for(int i = 0; i < requires.length; i++) {
				buffer.append("\trequires "); //$NON-NLS-1$
				if (requires[i].isTransitive()) {
					buffer.append(" public "); //$NON-NLS-1$
				}
				buffer.append(requires[i].name());
				buffer.append(';').append(lineDelimiter);
			}
		}
		buffer.append(lineDelimiter).append('}').toString();
	}

	/**
	 * @see JavaElement#getHandleMemento()
	 */
	@Override
	protected char getHandleMementoDelimiter() {
		return JavaElement.JEM_MODULE;
	}
	@Override
	public int getElementType() {
		return JAVA_MODULE;
	}
}
