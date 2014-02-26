/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

/**
 * Unchecked exception wrapping invalid input checked exception which may occur
 * when scanning original formatted source.
 *
 * @since 2.1
 */
public class AbortFormatting extends RuntimeException {

	Throwable nestedException;
	private static final long serialVersionUID = -5796507276311428526L; // backward compatible

	public AbortFormatting(String message) {
		super(message);
	}
	public AbortFormatting(Throwable nestedException) {
		super(nestedException.getMessage());
		this.nestedException = nestedException;
	}
}
