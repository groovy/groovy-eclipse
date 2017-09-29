/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.core.tests.util.TestVerifier;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;

public class RegressionTestSetup extends CompilerTestSetup {

	TestVerifier verifier = new TestVerifier(true);
	INameEnvironment javaClassLib;

	public RegressionTestSetup(long complianceLevel) {
		super(complianceLevel);
	}

	protected void setUp() {
		if (this.javaClassLib == null) {
			// Create name environment
			this.javaClassLib = new FileSystem(Util.getJavaClassLibs(), new String[0], null);
		}
		super.setUp();
	}
	protected void tearDown() {
		this.verifier.shutDown();
	}
}
