/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java;

/**
 * Used for filtering and disambiguating bindings in the index to match the classpath.
 */
public interface ClasspathResolver {
	public static final int NOT_ON_CLASSPATH = -1;

	/**
	 * Returns the priority of the given resource file on the classpath or {@link #NOT_ON_CLASSPATH} if the given file
	 * is not onthe classpath. In the event that the same fully-qualified class name is found in multiple resource
	 * files, the one with the higher priority number is preferred.
	 */
	int resolve(NdResourceFile sourceOfReference, NdResourceFile toTest);
}
