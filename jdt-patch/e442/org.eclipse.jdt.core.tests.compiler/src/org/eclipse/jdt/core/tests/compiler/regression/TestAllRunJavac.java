/*******************************************************************************
 * Copyright (c) 2026 GK Software and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

public class TestAllRunJavac extends TestAll {

	public TestAllRunJavac(String testName) {
		super(testName);
	}

	public static Test suite() {
		AbstractCompilerTest.testClassFilter = c -> c.getAnnotation(RunJavac.class) != null;
		return TestAll.suite();
	}
}
