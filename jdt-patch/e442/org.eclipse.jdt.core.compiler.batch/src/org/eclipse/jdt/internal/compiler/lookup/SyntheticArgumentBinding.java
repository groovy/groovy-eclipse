/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.lookup;

/**
 * Specific local variable location used to:
 * - either provide emulation for outer local variables used from within innerclass constructs,
 * - or provide emulation to enclosing instances.
 * - or model compact constructor arguments
 *
 * When it is mapping to an outer local variable, this actual outer local is accessible through
 * the public field #actualOuterLocalVariable. Such a synthetic argument binding will be inserted
 * in all constructors of local innertypes before the user arguments.
 */

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class SyntheticArgumentBinding extends LocalVariableBinding {

	{
		this.tagBits |= TagBits.IsArgument;
		this.useFlag = USED;
	}

	// if the argument is mapping to an outer local variable, this denotes the outer actual variable
	public LocalVariableBinding actualOuterLocalVariable;
	// if the argument has a matching synthetic field
	public FieldBinding matchingField;
	public Scope accessingScope; // scope from where the synth arg can be accessed

	public SyntheticArgumentBinding(LocalVariableBinding actualOuterLocalVariable, Scope declaringScope) {
		super(
			CharOperation.concat(TypeConstants.SYNTHETIC_OUTER_LOCAL_PREFIX, actualOuterLocalVariable.name),
			actualOuterLocalVariable.type,
			ClassFileConstants.AccFinal,
			true);
		this.actualOuterLocalVariable = actualOuterLocalVariable;
		this.accessingScope = declaringScope;
	}

	public SyntheticArgumentBinding(ReferenceBinding enclosingType) {
		super(
			CharOperation.concat(
				TypeConstants.SYNTHETIC_ENCLOSING_INSTANCE_PREFIX,
				String.valueOf(enclosingType.depth()).toCharArray()),
			enclosingType,
			ClassFileConstants.AccFinal,
			true);
	}

	public SyntheticArgumentBinding(RecordComponentBinding rcb) {
		super(rcb.name, rcb.type, rcb.modifiers, true);
		this.declaration = rcb.sourceRecordComponent();
	}
}
