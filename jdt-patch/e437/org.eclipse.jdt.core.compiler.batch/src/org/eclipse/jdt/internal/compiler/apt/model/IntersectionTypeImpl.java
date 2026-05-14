/*******************************************************************************
 * Copyright (c) 2024 Kamil Krzywanski and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Kamil Krzywanski - initial creation if Interesection type and Implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.Arrays;
import java.util.List;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 * Implementation of the WildcardType
 */
public class IntersectionTypeImpl extends TypeMirrorImpl implements IntersectionType {
	private final List<? extends TypeMirror> bounds;

	IntersectionTypeImpl(BaseProcessingEnvImpl env, TypeVariableBinding binding) {
		super(env, binding);
		this.bounds = Arrays.stream(binding.superInterfaces).map(referenceBinding -> this._env.getFactory().newTypeMirror(referenceBinding)).toList();
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeMirror#getKind()
	 */
	@Override
	public TypeKind getKind() {
		return TypeKind.INTERSECTION;
	}
	/* (non-Javadoc)
	 * @see javax.lang.model.type.WildcardType#getSuperBound()
	 */
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitIntersection(this, p);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.IntersectionType#getBounds()
	 */
	@Override
	public List<? extends TypeMirror> getBounds() {
		return this.bounds;
	}
}
