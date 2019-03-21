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
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;

@SuppressWarnings("rawtypes")
public class AssistSourceMethod extends ResolvedSourceMethod {
	private Map bindingCache;
	private Map infoCache;

	private String uniqueKey;
	private boolean isResolved;

	public AssistSourceMethod(JavaElement parent, String name, String[] parameterTypes, Map bindingCache, Map infoCache) {
		super(parent, name, parameterTypes, null);
		this.bindingCache = bindingCache;
		this.infoCache = infoCache;
	}

	@Override
	public Object getElementInfo(IProgressMonitor monitor) throws JavaModelException {
		return this.infoCache.get(this);
	}

	@Override
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

	@Override
	public boolean isResolved() {
		getKey();
		return this.isResolved;
	}

	@Override
	protected void toStringInfo(int tab, StringBuffer buffer, Object info,boolean showResolvedInfo) {
		super.toStringInfo(tab, buffer, info, showResolvedInfo && isResolved());
	}

	@Override
	public IAnnotation getAnnotation(String annotationName) {
		return new AssistAnnotation(this, annotationName, this.infoCache);
	}

	@Override
	public IType getType(String typeName, int count) {
		AssistSourceType type = new AssistSourceType(this, typeName, this.bindingCache, this.infoCache);
		type.occurrenceCount = count;
		return type;
	}

	@Override
	public ITypeParameter getTypeParameter(String typeParameterName) {
		return new AssistTypeParameter(this, typeParameterName, this.infoCache);
	}
}
