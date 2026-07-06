/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
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
package org.eclipse.jdt.internal.codeassist.impl;

import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.IElementInfo;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ResolvedSourceType;

public class AssistSourceType extends ResolvedSourceType {
	private final Map<JavaElement, Binding> bindingCache;
	private final Map<IJavaElement, IElementInfo> infoCache;

	private String uniqueKey;
	private boolean isResolved;

	public AssistSourceType(JavaElement parent, String name, Map<JavaElement, Binding> bindingCache, Map<IJavaElement, IElementInfo> infoCache) {
		this(parent, name, bindingCache, infoCache, 1);
	}

	public AssistSourceType(JavaElement parent, String name, Map<JavaElement, Binding> bindingCache, Map<IJavaElement, IElementInfo> infoCache, int occurrenceCount) {
		super(parent, name, null, occurrenceCount);
		this.bindingCache = bindingCache;
		this.infoCache = infoCache;
	}

	@Override
	public IElementInfo getElementInfo(IProgressMonitor monitor) throws JavaModelException {
		return this.infoCache.get(this);
	}

	@Override
	public String getFullyQualifiedParameterizedName() throws JavaModelException {
		if (isResolved()) {
			return getFullyQualifiedParameterizedName(getFullyQualifiedName('.'), this.getKey());
		}
		return getFullyQualifiedName('.', true/*show parameters*/);
	}

	@Override
	public String getKey() {
		if (this.uniqueKey == null) {
			Binding binding = this.bindingCache.get(this);
			if (binding != null) {
				this.isResolved = true;
				this.uniqueKey = new String(binding.computeUniqueKey());
			} else {
				this.isResolved = false;
				try {
					this.uniqueKey = getKey(this, false/*don't open*/);
				} catch (JavaModelException e) {
					// happen only if force open is true
					return null;
				}
			}
		}
		return this.uniqueKey;
	}

	@Override
	public boolean isResolved() {
		getKey();
		return this.isResolved;
	}

	@Override
	protected void toStringInfo(int tab, StringBuilder buffer, Object info,boolean showResolvedInfo) {
		super.toStringInfo(tab, buffer, info, showResolvedInfo && isResolved());
	}

	@Override
	public AssistAnnotation getAnnotation(String annotationName) {
		return new AssistAnnotation(this, annotationName, this.infoCache);
	}

	@Override
	public AssistSourceField getField(String fieldName) {
		return new AssistSourceField(this, fieldName, this.bindingCache, this.infoCache);
	}

	@Override
	public AssistInitializer getInitializer(int count) {
		return new AssistInitializer(this, count, this.bindingCache, this.infoCache);
	}

	@Override
	public AssistSourceMethod getMethod(String selector, String[] parameterTypeSignatures) {
		return new AssistSourceMethod(this, selector, parameterTypeSignatures, this.bindingCache, this.infoCache);
	}

	@Override
	public IType getType(String typeName) {
		return new AssistSourceType(this, typeName, this.bindingCache, this.infoCache);
	}

	@Override
	public AssistSourceType getType(String typeName, int count) {
		return new AssistSourceType(this, typeName, this.bindingCache, this.infoCache, count);
	}

	@Override
	public AssistTypeParameter getTypeParameter(String typeParameterName) {
		return new AssistTypeParameter(this, typeParameterName, this.infoCache);
	}
}
