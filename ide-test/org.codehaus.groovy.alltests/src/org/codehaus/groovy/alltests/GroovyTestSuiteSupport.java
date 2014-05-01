/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software Inc.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kris De Volder - initial API and implementation
 *******************************************************************************/
package org.codehaus.groovy.alltests;

import org.codehaus.groovy.frameworkadapter.util.ResolverActivator;

public class GroovyTestSuiteSupport {

	private static boolean initialized = false;

	public static void initializeCompilerChooser() {
		if (!initialized) {
			ResolverActivator.getDefault().initializeChooser();
			initialized = true;
		}
	}


}
