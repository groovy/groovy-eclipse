/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.env;

public interface IGenericMethod {
/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 */
// We have added AccDeprecated
int getModifiers();

boolean isConstructor();

/**
 * Answer the names of the argument
 * or null if the argument names are not available.
 */

char[][] getArgumentNames();
}
