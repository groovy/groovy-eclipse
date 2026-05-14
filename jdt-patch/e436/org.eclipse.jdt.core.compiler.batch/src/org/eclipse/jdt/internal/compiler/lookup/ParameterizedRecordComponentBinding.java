/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:m
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.impl.Constant;

/**
 * Binding denoting a record component after type substitution got performed.
 * On parameterized type bindings, all record components got substituted, regardless whether
 * their signature did involve generics or not, so as to get the proper declaringClass for
 * these record components.
 */
public class ParameterizedRecordComponentBinding extends RecordComponentBinding {

    public RecordComponentBinding originalRecordComponent;

public ParameterizedRecordComponentBinding(ParameterizedTypeBinding parameterizedDeclaringClass, RecordComponentBinding originalRecordComponent) {
	super (originalRecordComponent.name,
            Scope.substitute(parameterizedDeclaringClass, originalRecordComponent.type), // no need to check for static as components cannot be static
            originalRecordComponent.modifiers,
            parameterizedDeclaringClass);
    this.originalRecordComponent = originalRecordComponent;
    this.tagBits = originalRecordComponent.tagBits;
    this.id = originalRecordComponent.id;
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.VariableBinding#constant()
 */
@Override
public Constant constant() {
	return this.originalRecordComponent.constant();
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.RecordComponentBinding#original()
 */
@Override
public RecordComponentBinding original() {
	return this.originalRecordComponent.original();
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.VariableBinding#constant()
 */
@Override
public void setConstant(Constant constant) {
	this.originalRecordComponent.setConstant(constant);
}
}
