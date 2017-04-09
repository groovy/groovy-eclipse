/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

/**
 * Utility class for decoding additional flags in completion proposal.
 * <p>
 * This class provides static methods only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 *
 * @see CompletionProposal#getAdditionalFlags()
 *
 * @since 3.3
 */
public final class CompletionFlags {
	/**
	 * Constant representing the absence of any flag
	 */
	public static final int Default = 0x0000;

	/**
	 * Constant representing a static import
	 */
	public static final int StaticImport = 0x0001;

	/**
	 * Not instantiable.
	 */
	private CompletionFlags() {
		// Not instantiable
	}

	/**
	 * Returns whether the given integer includes the {@link #StaticImport} flag.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the {@link #StaticImport} flag is included
	 */
	public static boolean isStaticImport(int flags) {
		return (flags & StaticImport) != 0;
	}
}
