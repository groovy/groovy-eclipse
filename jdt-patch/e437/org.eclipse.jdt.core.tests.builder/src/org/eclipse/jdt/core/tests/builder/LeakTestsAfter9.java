/*******************************************************************************
 * Copyright (c) 2019, 2021 Andrey Loskutov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class LeakTestsAfter9 extends AbstractLeakTest {

	public LeakTestsAfter9(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(LeakTestsAfter9.class);
	}

	String getCompatibilityLevel() {
		return CompilerOptions.VERSION_9;
	}

	@Override
	public void testLeaksOnCleanBuild() throws Exception {
		super.testLeaksOnCleanBuild();
	}

	@Override
	public void testLeaksOnFullBuild() throws Exception {
		super.testLeaksOnFullBuild();
	}

	@Override
	public void testLeaksOnIncrementalBuild() throws Exception {
		super.testLeaksOnIncrementalBuild();
	}
}
