/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

import org.eclipse.jdt.internal.core.nd.field.StructDef.DeletionSemantics;

public abstract class AbstractTypeFactory<T> implements ITypeFactory<T> {
	@Override
	public void destructFields(Nd dom, long address) {
		// No nested fields by default
	}

	@Override
	public void destruct(Nd dom, long address) {
		// Nothing to destruct by default
	}

	@Override
	public boolean hasDestructor() {
		return false;
	}

	@Override
	public boolean isReadyForDeletion(Nd dom, long address) {
		return false;
	}

	@Override
	public DeletionSemantics getDeletionSemantics() {
		return DeletionSemantics.EXPLICIT;
	}
}
