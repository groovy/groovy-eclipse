/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for
 *								Bug 186342 - [compiler][null] Using annotations for null checking
 *								Bug 429958 - [1.8][null] evaluate new DefaultLocation attribute of @NonNullByDefault
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

/**
 * Pseudo method binding used to wrapper a real method, and expose less exceptions than original.
 * For other protocols, it should delegate to original method
 */
public class MostSpecificExceptionMethodBinding  extends MethodBinding {

	private MethodBinding originalMethod;
	
	public MostSpecificExceptionMethodBinding (MethodBinding originalMethod, ReferenceBinding[] mostSpecificExceptions) {
		super(
				originalMethod.modifiers, 
				originalMethod.selector, 
				originalMethod.returnType, 
				originalMethod.parameters, 
				mostSpecificExceptions, 
				originalMethod.declaringClass);
		this.originalMethod = originalMethod;
		this.parameterNonNullness = originalMethod.parameterNonNullness;
		this.defaultNullness = originalMethod.defaultNullness;
	}
	
	public MethodBinding original() {
		return this.originalMethod.original();
	}
}
