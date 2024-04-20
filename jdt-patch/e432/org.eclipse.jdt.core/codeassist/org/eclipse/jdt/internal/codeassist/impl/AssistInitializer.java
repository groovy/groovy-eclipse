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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.IElementInfo;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.core.Initializer;
import org.eclipse.jdt.internal.core.JavaElement;

public class AssistInitializer extends Initializer {
	private final Map<JavaElement, Binding> bindingCache;
	private final Map<IJavaElement, IElementInfo> infoCache;
	public AssistInitializer(JavaElement parent, int count, Map<JavaElement, Binding> bindingCache, Map<IJavaElement, IElementInfo> infoCache) {
		super(parent, count);
		this.bindingCache = bindingCache;
		this.infoCache = infoCache;
	}

	@Override
	public IElementInfo getElementInfo(IProgressMonitor monitor) throws JavaModelException {
		return this.infoCache.get(this);
	}

	@Override
	public AssistSourceType getType(String typeName, int count) {
		return new AssistSourceType(this, typeName, this.bindingCache, this.infoCache, count);
	}
}
