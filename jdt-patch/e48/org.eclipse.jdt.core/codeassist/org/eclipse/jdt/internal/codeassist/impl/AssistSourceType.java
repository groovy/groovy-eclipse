/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.impl;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ResolvedSourceType;

@SuppressWarnings("rawtypes")
public class AssistSourceType extends ResolvedSourceType {
	private Map bindingCache;
	private Map infoCache;

	private String uniqueKey;
	private boolean isResolved;

	public AssistSourceType(JavaElement parent, String name, Map bindingCache, Map infoCache) {
		super(parent, name, null);
		this.bindingCache = bindingCache;
		this.infoCache = infoCache;
	}

	public Object getElementInfo(IProgressMonitor monitor) throws JavaModelException {
		return this.infoCache.get(this);
	}

	public String getFullyQualifiedParameterizedName() throws JavaModelException {
		if (isResolved()) {
			return getFullyQualifiedParameterizedName(getFullyQualifiedName('.'), this.getKey());
		}
		return getFullyQualifiedName('.', true/*show parameters*/);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.SourceType#getKey()
	 */
	public String getKey() {
		if (this.uniqueKey == null) {
			Binding binding = (Binding) this.bindingCache.get(this);
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

	public boolean isResolved() {
		getKey();
		return this.isResolved;
	}

	protected void toStringInfo(int tab, StringBuffer buffer, Object info,boolean showResolvedInfo) {
		super.toStringInfo(tab, buffer, info, showResolvedInfo && isResolved());
	}

	public IAnnotation getAnnotation(String annotationName) {
		return new AssistAnnotation(this, annotationName, this.infoCache);
	}

	public IField getField(String fieldName) {
		return new AssistSourceField(this, fieldName, this.bindingCache, this.infoCache);
	}

	public IInitializer getInitializer(int count) {
		return new AssistInitializer(this, count, this.bindingCache, this.infoCache);
	}

	public IMethod getMethod(String selector, String[] parameterTypeSignatures) {
		return new AssistSourceMethod(this, selector, parameterTypeSignatures, this.bindingCache, this.infoCache);
	}

	public IType getType(String typeName) {
		return new AssistSourceType(this, typeName, this.bindingCache, this.infoCache);
	}

	public IType getType(String typeName, int count) {
		AssistSourceType type = new AssistSourceType(this, typeName, this.bindingCache, this.infoCache);
		type.occurrenceCount = count;
		return type;
	}

	public ITypeParameter getTypeParameter(String typeParameterName) {
		return new AssistTypeParameter(this, typeParameterName, this.infoCache);
	}
}
