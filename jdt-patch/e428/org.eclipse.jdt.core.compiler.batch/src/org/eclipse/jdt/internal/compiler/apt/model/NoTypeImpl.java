/*******************************************************************************
 * Copyright (c) 2007, 2015 BEA Systems, Inc. and others
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
 *    IBM Corporation - Java 8 support
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * An implementation of NoType, which is used to represent certain pseudo-types.
 * @see NoType
 */
public class NoTypeImpl extends TypeMirrorImpl implements NoType, NullType
{
	private final TypeKind _kind;

	public static final NoType NO_TYPE_NONE = new NoTypeImpl(TypeKind.NONE);
	public static final NoType NO_TYPE_VOID = new NoTypeImpl(TypeKind.VOID, TypeBinding.VOID);
	public static final NoType NO_TYPE_PACKAGE = new NoTypeImpl(TypeKind.PACKAGE);
	public static final NullType NULL_TYPE = new NoTypeImpl(TypeKind.NULL, TypeBinding.NULL);
	public static final Binding NO_TYPE_BINDING = new Binding() {
		@Override
		public int kind() {
			throw new IllegalStateException();
		}

		@Override
		public char[] readableName() {
			throw new IllegalStateException();
		}
	};

	public NoTypeImpl(TypeKind kind) {
		super(null, NO_TYPE_BINDING);
		this._kind = kind;
	}
	public NoTypeImpl(TypeKind kind, Binding binding) {
		super(null, binding);
		this._kind = kind;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		switch(this.getKind())
		{
			case NULL :
				return v.visitNull(this, p);
			default:
				return v.visitNoType(this, p);
		}
	}

	@Override
	public TypeKind getKind()
	{
		return this._kind;
	}

	@Override
	public String toString()
	{
		switch (this._kind) {
		default:
		case NONE:
			return "none"; //$NON-NLS-1$
		case NULL:
			return "null"; //$NON-NLS-1$
		case VOID:
			return "void"; //$NON-NLS-1$
		case PACKAGE:
			return "package"; //$NON-NLS-1$
		case MODULE:
			return "module"; //$NON-NLS-1$
		}
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		return Factory.EMPTY_ANNOTATION_MIRRORS;
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
		return (A[]) Array.newInstance(annotationType, 0);
	}

}
