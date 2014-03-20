/*******************************************************************************
 * Copyright (c) 2013 GK Software AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

/**
 * Thrown when a definite compile error is detected deep within the type inference.
 */
public class InferenceFailureException extends Exception {

	private static final long serialVersionUID = 1L;

	// TODO(stephan); add more details so that ProblemReported can eventually manufacture an appropriate message
	
	public InferenceFailureException(String message) {
		super(message);
	}
	
}
