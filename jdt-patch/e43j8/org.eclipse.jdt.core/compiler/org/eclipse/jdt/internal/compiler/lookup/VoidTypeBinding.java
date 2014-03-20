/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

// Give it an identity of its own to discriminate the fact that this type is not annotatable and so is a singleton.
public class VoidTypeBinding extends BaseTypeBinding {

	VoidTypeBinding() {
		super(TypeIds.T_void, TypeConstants.VOID, new char[] { 'V' });
	}
	
	public TypeBinding clone(TypeBinding enclosingType) {
		return this;  // enforce solitude.
	}
	
	public void setTypeAnnotations(AnnotationBinding[] annotations, boolean evalNullAnnotations) {
		return; // reject misguided attempt.
	}
	
	public TypeBinding unannotated() {
		return this;
	}
}
