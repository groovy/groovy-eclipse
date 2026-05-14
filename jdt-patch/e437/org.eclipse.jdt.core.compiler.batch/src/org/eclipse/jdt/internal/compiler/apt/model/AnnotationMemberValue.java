/*******************************************************************************
 * Copyright (c) 2009, 2017 Vladimir Piskarev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.model;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;

public class AnnotationMemberValue extends AnnotationValueImpl {

	private final MethodBinding _methodBinding;

	/**
	 * @param value
	 *            The JDT representation of a compile-time constant. See
	 *            {@link org.eclipse.jdt.internal.compiler.lookup.ElementValuePair#getValue()} for possible object types:
	 *            <ul>
	 *            <li>{@link org.eclipse.jdt.internal.compiler.impl.Constant} for member
	 *            of primitive type or String</li>
	 *            <li>{@link org.eclipse.jdt.internal.compiler.lookup.TypeBinding} for a member value of type
	 *            {@link java.lang.Class}</li>
	 *            <li>{@link org.eclipse.jdt.internal.compiler.lookup.FieldBinding} for an enum constant</li>
	 *            <li>{@link org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding} for an annotation instance</li>
	 *            <li><code>Object[]</code> for a member value of array type, where the
	 *            array entries are one of the above</li>
	 *            </ul>
	 * @param methodBinding the method binding that defined this member value pair
	 */
	public AnnotationMemberValue(BaseProcessingEnvImpl env, Object value, MethodBinding methodBinding) {
		super(env, value, methodBinding.returnType);
		this._methodBinding = methodBinding;
	}

	/**
	 * @return the method binding that defined this member value pair.
	 */
	public MethodBinding getMethodBinding() {
		return this._methodBinding;
	}
}
