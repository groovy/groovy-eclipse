/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.core.compiler.CharOperation;

public class AptBinaryLocalVariableBinding extends LocalVariableBinding {
	AnnotationBinding[] annotationBindings;
	// enclosing element
	public MethodBinding methodBinding;

	public AptBinaryLocalVariableBinding(char[] name, TypeBinding type, int modifiers, AnnotationBinding[] annotationBindings, MethodBinding methodBinding) {
		super(name, type, modifiers, true);
		this.annotationBindings = annotationBindings == null ? Binding.NO_ANNOTATIONS : annotationBindings;
		this.methodBinding = methodBinding;
	}

	@Override
	public AnnotationBinding[] getAnnotations() {
		return this.annotationBindings;
	}

	@Override
	public int hashCode() {
		int result = 17;
		int c = CharOperation.hashCode(this.name);
		result = 31 * result + c;
		c = this.type.hashCode();
		result = 31 * result + c;
		c = this.modifiers;
		result = 31 * result + c;
		c = Arrays.hashCode(this.annotationBindings);
		result = 31 * result + c;
		c = this.methodBinding.hashCode();
		result = 31 * result + c;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AptBinaryLocalVariableBinding other = (AptBinaryLocalVariableBinding) obj;
		return CharOperation.equals(this.name, other.name)
				&& Objects.equals(this.type, other.type)
				&& this.modifiers==other.modifiers
				&& Arrays.equals(this.annotationBindings, other.annotationBindings)
				&& Objects.equals(this.methodBinding, other.methodBinding);
	}
}
