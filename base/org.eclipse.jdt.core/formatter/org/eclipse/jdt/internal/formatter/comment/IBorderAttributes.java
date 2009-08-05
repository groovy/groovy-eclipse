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

package org.eclipse.jdt.internal.formatter.comment;

/**
 * Comment region border attributes.
 *
 * @since 3.0
 */
public interface IBorderAttributes {

	/** Region has lower border attribute */
	public static final int BORDER_LOWER= 1 << 0;

	/** Region has upper border attribute */
	public static final int BORDER_UPPER= 1 << 1;
}
