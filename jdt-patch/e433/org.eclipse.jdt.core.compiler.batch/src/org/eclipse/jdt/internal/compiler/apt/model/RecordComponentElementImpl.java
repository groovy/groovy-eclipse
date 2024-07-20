/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM Corporation.
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

package org.eclipse.jdt.internal.compiler.apt.model;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.RecordComponentBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public class RecordComponentElementImpl extends VariableElementImpl implements RecordComponentElement {

	protected RecordComponentElementImpl(BaseProcessingEnvImpl env, RecordComponentBinding binding) {
		super(env, binding);
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.RECORD_COMPONENT;
	}

	@Override
	public ExecutableElement getAccessor() {
		RecordComponentBinding comp = (RecordComponentBinding) this._binding;
		ReferenceBinding binding = comp.declaringRecord;
		MethodBinding accessor = binding.getRecordComponentAccessor(comp.name);
		if (accessor != null) {
			return new ExecutableElementImpl(this._env, accessor);
		}
		return null;
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> visitor, P param) {
		return visitor.visitRecordComponent(this, param);
	}
}
