/*******************************************************************************
 * Copyright (c) 2007, 2014 BEA Systems, Inc. and others.
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
 *    IBM Corporation - Java 8 support
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;

/**
 * Implementation of ArrayType, which represents an array of some type.
 */
public class ArrayTypeImpl extends TypeMirrorImpl implements ArrayType {

	ArrayTypeImpl(BaseProcessingEnvImpl env, ArrayBinding binding) {
		super(env, binding);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.ArrayType#getComponentType()
	 */
	@Override
	public TypeMirror getComponentType() {
		return this._env.getFactory().newTypeMirror(((ArrayBinding)this._binding).elementsType());
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeMirror#accept(javax.lang.model.type.TypeVisitor, java.lang.Object)
	 */
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitArray(this, p);
	}

	@Override
	protected AnnotationBinding[] getAnnotationBindings() {
		AnnotationBinding[] oldies = ((ArrayBinding)this._binding).getTypeAnnotations();
		AnnotationBinding[] newbies = Binding.NO_ANNOTATIONS;
		// Strip out the annotations on sub arrays
		for (int i = 0, length = oldies == null ? 0 : oldies.length; i < length; i++) {
			if (oldies[i] == null) {
				System.arraycopy(oldies, 0, newbies = new AnnotationBinding[i], 0, i);
				return newbies;
			}
		}
		return newbies;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeMirror#getKind()
	 */
	@Override
	public TypeKind getKind() {
		return TypeKind.ARRAY;
	}
}
