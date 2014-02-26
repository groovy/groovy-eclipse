/*******************************************************************************
 * Copyright (c) 2013 GK Software AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

/**
 * Generalization over TypeBounds and ConstraintFormulas which both
 * can be created during reduction.
 */
public abstract class ReductionResult {

	protected static final ReductionResult TRUE = new ReductionResult() {
		/* empty body just to make abstract class instantiable */
		public String toString() { return "TRUE"; } //$NON-NLS-1$
	};
	protected static final ReductionResult FALSE = new ReductionResult() { 
		/* empty body just to make abstract class instantiable */ 
		public String toString() { return "FALSE"; } //$NON-NLS-1$
	};
	/** Used to accept unchecked conversion to make ecj conform with javac bug https://bugs.openjdk.java.net/browse/JDK-8026527 */
	
	// Relation kinds, mimic an enum:
	protected static final int COMPATIBLE = 1;
	protected static final int SUBTYPE = 2;
	protected static final int SUPERTYPE = 3;
	protected static final int SAME = 4;
	protected static final int TYPE_ARGUMENT_CONTAINED = 5;
	protected static final int CAPTURE = 6;
	static final int EXCEPTIONS_CONTAINED = 7;
	
	protected TypeBinding right; // note that the LHS differs between sub-classes.
	protected int relation;

	protected static String relationToString(int relation) {
		switch (relation) {
			case SAME: 			return " = "; //$NON-NLS-1$
			case COMPATIBLE: 	return " \u2192 "; //$NON-NLS-1$
			case SUBTYPE: 		return " <: "; //$NON-NLS-1$
			case SUPERTYPE: 	return " :> "; //$NON-NLS-1$
			case TYPE_ARGUMENT_CONTAINED:
								return " <= "; //$NON-NLS-1$
			case CAPTURE:
								return " captureOf "; //$NON-NLS-1$
			default:
				throw new IllegalArgumentException("Unknown type relation "+relation); //$NON-NLS-1$
		}
	}
}
