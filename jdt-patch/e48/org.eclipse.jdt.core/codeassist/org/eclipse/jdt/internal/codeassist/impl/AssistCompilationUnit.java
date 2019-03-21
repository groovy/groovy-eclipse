/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.impl;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaElementInfo;
import org.eclipse.jdt.internal.core.PackageFragment;

@SuppressWarnings("rawtypes")
public class AssistCompilationUnit extends CompilationUnit {
	private Map infoCache;
	private Map bindingCache;
	public AssistCompilationUnit(ICompilationUnit compilationUnit, WorkingCopyOwner owner, Map bindingCache, Map infoCache) {
		super((PackageFragment)compilationUnit.getParent(), compilationUnit.getElementName(), owner);
		this.bindingCache = bindingCache;
		this.infoCache = infoCache;
	}

	@Override
	public Object getElementInfo(IProgressMonitor monitor) throws JavaModelException {
		return this.infoCache.get(this);
	}

	@Override
	public IImportContainer getImportContainer() {
		return new AssistImportContainer(this, this.infoCache);
	}

	@Override
	public IPackageDeclaration getPackageDeclaration(String pkg) {
		return new AssistPackageDeclaration(this, pkg, this.infoCache);
	}

	@Override
	public IType getType(String typeName) {
		return new AssistSourceType(this, typeName, this.bindingCache, this.infoCache);
	}

	@Override
	public boolean hasChildren() throws JavaModelException {
		JavaElementInfo info = (JavaElementInfo)this.infoCache.get(this);
		return info.getChildren().length > 0;
	}
}
