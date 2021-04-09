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

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class LeakTestsBefore9 extends AbstractLeakTest {

	public LeakTestsBefore9(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(LeakTestsBefore9.class);
	}

	String getCompatibilityLevel() {
		return CompilerOptions.VERSION_1_4;
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
