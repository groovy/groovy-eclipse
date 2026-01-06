/*******************************************************************************
 * Copyright (c) 2006, 2023 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    IBM Corporation - fix for 342598
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 * Implementation of DeclaredType, which refers to a particular usage or instance of a type.
 * Contrast with {@link javax.lang.model.element.TypeElement}, which is an element that potentially defines a family
 * of DeclaredTypes.
 */
public class DeclaredTypeImpl extends TypeMirrorImpl implements DeclaredType {

	private final ElementKind _elementKindHint;

	/* package */ DeclaredTypeImpl(BaseProcessingEnvImpl env, ReferenceBinding binding) {
		super(env, binding);
		this._elementKindHint = null;
	}

	/**
	 * Create a DeclaredType that knows in advance what kind of element to produce from asElement().
	 * This is useful in the case where the type binding is to an unresolved type, but we know
	 * from context what type it is - e.g., an annotation type.
	 */
	/* package */ DeclaredTypeImpl(BaseProcessingEnvImpl env, ReferenceBinding binding, ElementKind elementKindHint) {
		super(env, binding);
		this._elementKindHint = elementKindHint;
	}

	@Override
	public Element asElement() {
		TypeBinding prototype = null;
		if (this._binding instanceof TypeBinding) {
			prototype = ((TypeBinding) this._binding).prototype();
		}
		if (prototype != null) {
			return this._env.getFactory().newElement(prototype, this._elementKindHint);
		}
		// The JDT compiler does not distinguish between type elements and declared types
		return this._env.getFactory().newElement(this._binding, this._elementKindHint);
	}

	@Override
	public TypeMirror getEnclosingType() {
		ReferenceBinding binding = (ReferenceBinding)this._binding;
		ReferenceBinding enclosingType = binding.enclosingType();
		if (enclosingType != null) {
			return this._env.getFactory().newTypeMirror(enclosingType);
		}
		return this._env.getFactory().getNoType(TypeKind.NONE);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.lang.model.type.DeclaredType#getTypeArguments()
	 * @see javax.lang.model.element.TypeElement#getTypeParameters().
	 */
	@Override
	public List<? extends TypeMirror> getTypeArguments() {
		ReferenceBinding binding = (ReferenceBinding)this._binding;
		if (binding.isParameterizedType()) {
			ParameterizedTypeBinding ptb = (ParameterizedTypeBinding)this._binding;
			TypeBinding[] arguments = ptb.arguments;
			int length = arguments == null ? 0 : arguments.length;
			if (length == 0) return Collections.emptyList();
			List<TypeMirror> args = new ArrayList<>(length);
			for (TypeBinding arg : arguments) {
				args.add(this._env.getFactory().newTypeMirror(arg));
			}
			return Collections.unmodifiableList(args);
		}
		if (binding.isGenericType()) {
			TypeVariableBinding[] typeVariables = binding.typeVariables();
			List<TypeMirror> args = new ArrayList<>(typeVariables.length);
			for (TypeBinding arg : typeVariables) {
				args.add(this._env.getFactory().newTypeMirror(arg));
			}
			return Collections.unmodifiableList(args);
		}
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeMirror#accept(javax.lang.model.type.TypeVisitor, java.lang.Object)
	 */
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitDeclared(this, p);
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.DECLARED;
	}

	@Override
	public String toString() {
		return new String(this._binding.readableName());
	}

}
