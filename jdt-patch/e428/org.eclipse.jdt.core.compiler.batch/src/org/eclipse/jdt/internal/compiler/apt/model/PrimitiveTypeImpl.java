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
 *    IBM Corporation - Java 8 support
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/**
 *
 * @since 3.3
 */
public class PrimitiveTypeImpl extends TypeMirrorImpl implements PrimitiveType {

	public final static PrimitiveTypeImpl BOOLEAN = new PrimitiveTypeImpl(TypeBinding.BOOLEAN);
	public final static PrimitiveTypeImpl BYTE = new PrimitiveTypeImpl(TypeBinding.BYTE);
	public final static PrimitiveTypeImpl CHAR = new PrimitiveTypeImpl(TypeBinding.CHAR);
	public final static PrimitiveTypeImpl DOUBLE = new PrimitiveTypeImpl(TypeBinding.DOUBLE);
	public final static PrimitiveTypeImpl FLOAT = new PrimitiveTypeImpl(TypeBinding.FLOAT);
	public final static PrimitiveTypeImpl INT = new PrimitiveTypeImpl(TypeBinding.INT);
	public final static PrimitiveTypeImpl LONG = new PrimitiveTypeImpl(TypeBinding.LONG);
	public final static PrimitiveTypeImpl SHORT = new PrimitiveTypeImpl(TypeBinding.SHORT);

	/**
	 * Clients should call {@link Factory#getPrimitiveType(TypeKind)},
	 * rather than creating new objects.
	 */
	private PrimitiveTypeImpl(BaseTypeBinding binding) {
		// Primitive types do not need an environment!
		super(null, binding);
	}

	PrimitiveTypeImpl(BaseProcessingEnvImpl env, BaseTypeBinding binding) {
		// From Java 8, base type bindings can hold annotations and hence need the environment.
		super(env, binding);
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		return v.visitPrimitive(this, p);
	}

	@Override
	public TypeKind getKind() {
		return getKind((BaseTypeBinding)this._binding);
	}

	public static TypeKind getKind(BaseTypeBinding binding) {
		switch (binding.id) {
		case TypeIds.T_boolean:
			return TypeKind.BOOLEAN;
		case TypeIds.T_byte:
			return TypeKind.BYTE;
		case TypeIds.T_char:
			return TypeKind.CHAR;
		case TypeIds.T_double:
			return TypeKind.DOUBLE;
		case TypeIds.T_float:
			return TypeKind.FLOAT;
		case TypeIds.T_int:
			return TypeKind.INT;
		case TypeIds.T_long:
			return TypeKind.LONG;
		case TypeIds.T_short:
			return TypeKind.SHORT;
		default:
			throw new IllegalArgumentException("BaseTypeBinding of unexpected id " + binding.id); //$NON-NLS-1$
		}
	}

}
