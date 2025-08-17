/*******************************************************************************
 * Copyright (c) 2025 GK Software, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

/**
 * Global registry of active contexts to be used for capturing
 * in situations that have no AST location nor Scope at hand.
 */
public class CapturingContext {

	/** Global reference to an active context per thread. */
	private static ThreadLocal<CapturingContext> activeContexts = new ThreadLocal<>();

	/** Position to use for capturing: */
	private int start, end;
	/** Scope to use for capturing: */
	private Scope scope;
	/** implements a linked stack */
	private CapturingContext previous;

	/** Prevent recursive capturing */
	private boolean isCaptureInProgress;

	private CapturingContext(int start, int end, Scope scope, CapturingContext previous) {
		this.start = start;
		this.end = end;
		this.scope = scope;
		this.previous = previous;
	}

	public static void enter(int start, int end, Scope scope) {
		activeContexts.set(new CapturingContext(start, end, scope, activeContexts.get()));
	}

	public static void leave() {
		CapturingContext inst = activeContexts.get();
		activeContexts.remove();
		if (inst != null && inst.previous != null)
			activeContexts.set(inst.previous);
	}

	public static ReferenceBinding maybeCapture(ReferenceBinding type) {
		if (type instanceof ParameterizedTypeBinding ptb && !ptb.isCaptureInProgress) {
			CapturingContext inst = activeContexts.get();
			if (inst != null && !inst.isCaptureInProgress) {
				try {
					inst.isCaptureInProgress = true;
					return ptb.capture(inst.scope, inst.start, inst.end);
				} finally {
					inst.isCaptureInProgress = false;
				}
			}
		}
		return type;
	}
}
