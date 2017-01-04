/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 438458 - [1.8][null] clean up handling of null type annotations wrt type variables
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

// Give it an identity of its own to discriminate the fact that this type is not annotatable and so is a singleton.
public class NullTypeBinding extends BaseTypeBinding {

	NullTypeBinding() {
		super(TypeIds.T_null, TypeConstants.NULL, new char[] { 'N' }); // N stands for null even if it is never internally used);
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