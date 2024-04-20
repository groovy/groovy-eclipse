/*******************************************************************************
 * Copyright (c) 2006, 2023 IBM Corporation and others.
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

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 * Implementation of TypeVariable
 */
public class TypeVariableImpl extends TypeMirrorImpl implements TypeVariable {

	TypeVariableImpl(BaseProcessingEnvImpl env, TypeVariableBinding binding) {
		super(env, binding);
	}
	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeVariable#asElement()
	 */
	@Override
	public Element asElement() {
		return this._env.getFactory().newElement(this._binding);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeVariable#getLowerBound()
	 */
	@Override
	public TypeMirror getLowerBound() {
		// TODO might be more complex than this
		return this._env.getFactory().getNullType();
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeVariable#getUpperBound()
	 */
	@Override
	public TypeMirror getUpperBound() {
		TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this._binding;
		TypeBinding firstBound = typeVariableBinding.firstBound;
		ReferenceBinding[] superInterfaces = typeVariableBinding.superInterfaces;
		if (firstBound == null || superInterfaces.length == 0) {
			// no explicit bound
			return this._env.getFactory().newTypeMirror(typeVariableBinding.upperBound());
		}
		if (superInterfaces.length == 1 && TypeBinding.equalsEquals(superInterfaces[0], firstBound)) {
			// only one bound that is an interface
			return this._env.getFactory().newTypeMirror(typeVariableBinding.upperBound());
		}
		return this._env.getFactory().newTypeMirror(this._binding);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeMirror#accept(javax.lang.model.type.TypeVisitor, java.lang.Object)
	 */
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitTypeVariable(this, p);
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.TYPEVAR;
	}


}
