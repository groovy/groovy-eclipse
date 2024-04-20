/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

	@Override
	protected void setUp() {
		if (this.javaClassLib == null) {
			// Create name environment
			this.javaClassLib = new FileSystem(Util.getJavaClassLibs(), new String[0], null);
		}
		super.setUp();
	}
	@Override
	protected void tearDown() {
		this.verifier.shutDown();
	}
}
