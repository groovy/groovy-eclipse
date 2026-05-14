/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.runtime;

/**
 * A TargetException is thrown when an operation on a target has failed
 * for any reason.
 */
public class TargetException extends Exception {
private static final long serialVersionUID = 1L;
/**
 * Constructs a <code>TargetException</code> with no detail  message.
 */
public TargetException() {
	super();
}
/**
 * Constructs a <code>TargetException</code> with the specified
 * detail message.
 *
 * @param   s   the detail message.
 */
public TargetException(String s) {
	super(s);
}
}
