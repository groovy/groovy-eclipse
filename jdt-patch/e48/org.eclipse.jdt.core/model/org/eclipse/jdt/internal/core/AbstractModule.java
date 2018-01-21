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

public interface AbstractModule extends IModuleDescription {
	
	/**
	 * Handle for an automatic module.
	 *
	 * <p>Note, that by definition this is mostly a fake, only {@link #getElementName()} provides a useful value.</p>
	 */
	static class AutoModule extends NamedMember implements AbstractModule {
	
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
		public char getHandleMementoDelimiter() {
			return JavaElement.JEM_MODULE;
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
		public void toStringContent(StringBuffer buffer, String lineDelimiter) throws JavaModelException {
			buffer.append("automatic module "); //$NON-NLS-1$
			buffer.append(this.name);
		}
	}
	
	// "forward declaration" for a method from JavaElement:
	abstract Object getElementInfo() throws JavaModelException;

	default IModule getModuleInfo() throws JavaModelException {
		return (IModule) getElementInfo();
	}
	default IModuleReference[] getRequiredModules() throws JavaModelException {
		return getModuleInfo().requires();
	}
	default IPackageExport[] getExportedPackages() throws JavaModelException {
		return getModuleInfo().exports();
	}
	default IService[] getProvidedServices() throws JavaModelException {
		return getModuleInfo().provides();
	}
	default char[][] getUsedServices() throws JavaModelException {
		return getModuleInfo().uses();
	}
	default IPackageExport[] getOpenedPackages() throws JavaModelException {
		return getModuleInfo().opens();
	}

	default String toString(String lineDelimiter) {
		StringBuffer buffer = new StringBuffer();
		try {
			toStringContent(buffer, lineDelimiter);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buffer.toString();
	}
	default void toStringContent(StringBuffer buffer, String lineDelimiter) throws JavaModelException {
		IPackageExport[] exports = getExportedPackages();
		IModuleReference[] requires = getRequiredModules();
		buffer.append("module "); //$NON-NLS-1$
		buffer.append(getElementName()).append(' ');
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

	@Override
	default int getElementType() {
		return JAVA_MODULE;
	}
}
