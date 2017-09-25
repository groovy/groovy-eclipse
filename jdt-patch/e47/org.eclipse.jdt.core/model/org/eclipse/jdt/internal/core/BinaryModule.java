/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager.PerProjectInfo;

public class BinaryModule extends AbstractModule {
	public BinaryModule(JavaElement parent, String name) {
		super(parent, name);
	}
	/*
	 * @see IParent#getChildren()
	 */
	public IJavaElement[] getChildren() throws JavaModelException {
		return NO_ELEMENTS;
	}
	@Override
	public boolean isBinary() {
		return true;
	}
	@Override
	public int getFlags() throws JavaModelException {
		ModuleDescriptionInfo info = (ModuleDescriptionInfo) getElementInfo();
		return info.getModifiers();
	}
	public String getKey(boolean forceOpen) throws JavaModelException {
		return getKey(this, forceOpen);
	}
	@Override
	public ISourceRange getSourceRange() throws JavaModelException {
		SourceMapper mapper= getSourceMapper();
		if (mapper != null) {
			// ensure the class file's buffer is open so that source ranges are computed
			((ModularClassFile)getClassFile()).getBuffer();

			return mapper.getSourceRange(this);
		} else {
			return SourceMapper.UNKNOWN_RANGE;
		}
	}
	public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
		JavadocContents javadocContents = getJavadocContents(monitor);
		if (javadocContents == null) return null;
		return javadocContents.getModuleDoc();
	}
	public JavadocContents getJavadocContents(IProgressMonitor monitor) throws JavaModelException {
		PerProjectInfo projectInfo = JavaModelManager.getJavaModelManager().getPerProjectInfoCheckExistence(getJavaProject().getProject());
		JavadocContents cachedJavadoc = null;
		synchronized (projectInfo.javadocCache) {
			cachedJavadoc = (JavadocContents) projectInfo.javadocCache.get(this);
		}
		
		if (cachedJavadoc != null && cachedJavadoc != BinaryType.EMPTY_JAVADOC) {
			return cachedJavadoc;
		}
		URL baseLocation= getJavadocBaseLocation();
		if (baseLocation == null) {
			return null;
		}
		StringBuffer pathBuffer = new StringBuffer(baseLocation.toExternalForm());

		if (!(pathBuffer.charAt(pathBuffer.length() - 1) == '/')) {
			pathBuffer.append('/');
		}
		pathBuffer.append(getElementName()).append(JavadocConstants.MODULE_FILE_SUFFIX);
		if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
		String contents = getURLContents(baseLocation, String.valueOf(pathBuffer));
		JavadocContents javadocContents = new JavadocContents(contents);
		synchronized (projectInfo.javadocCache) {
			projectInfo.javadocCache.put(this, javadocContents);
		}
		return javadocContents;
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
}