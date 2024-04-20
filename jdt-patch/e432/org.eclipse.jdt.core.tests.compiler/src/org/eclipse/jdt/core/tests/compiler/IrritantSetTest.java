/*******************************************************************************
 * Copyright (c) 2024 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.compiler.impl.IrritantSet;

import junit.framework.Test;
import junit.framework.TestSuite;

public class IrritantSetTest extends TestCase {

	public IrritantSetTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(IrritantSetTest.class.getPackageName());
		suite.addTest(new TestSuite(IrritantSetTest.class));
		return suite;
	}

	public void testGroup4() {
		if (IrritantSet.GROUP_MAX <= 4) {
			System.out.println("IrritantSetTest.testGroup4 will trigger once IrritantSet.GROUP_MAX exceeds 4.");
			return;
		}
		@SuppressWarnings("unused") // dead code as of now
		int singleIrritant = ( 4 << IrritantSet.GROUP_SHIFT /* group4 */) + 42;
		IrritantSet irritantSet = new IrritantSet(singleIrritant);
		assertTrue(irritantSet.isSet(singleIrritant));
	}
}
