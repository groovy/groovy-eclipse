/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Description of a bootstrap method table entry as specified in the JVM specifications.
 *
 * This interface may be implemented by clients.
 *
 * @since 3.8
 */
public interface IBootstrapMethodsEntry {

	int getBootstrapMethodReference();
	int[] getBootstrapArguments();
}
