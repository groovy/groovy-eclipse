/*******************************************************************************
 * Copyright (c) 2013, 2020 GoPivotal, Inc and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *			Bug 407191 - [1.8] Binary access support for type annotations
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;

@SuppressWarnings({ "rawtypes" })
public class ClassFileReaderTest_15 extends AbstractRegressionTest {
	static {
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_15);
	}
	public static Class testClass() {
		return ClassFileReaderTest_15.class;
	}

	public ClassFileReaderTest_15(String name) {
		super(name);
	}

	// Needed to run tests individually from JUnit
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.complianceLevel = ClassFileConstants.JDK15;
		this.enablePreview = true;
	}

	public void testBug564227_001() throws Exception {
		String source =
				"sealed class X permits Y, Z{\n" +
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n" +
				"final class Y extends X{}\n" +
				"final class Z extends X{}\n";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		char[][] permittedSubtypesNames = classFileReader.getPermittedSubtypeNames();

		assertEquals(2, permittedSubtypesNames.length);

		char [][] expected = {"Y".toCharArray(), "Z".toCharArray()};
		assertTrue(CharOperation.equals(permittedSubtypesNames, expected));

	}
	public void testBug565782_001() throws Exception {
		String source =
				"sealed interface I {}\n"+
				"enum X implements I {\n"+
				"    ONE {};\n"+
				"    public static void main(String[] args) {\n"+
				"        System.out.println(0);\n"+
				"   }\n"+
				"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		char[][] permittedSubtypesNames = classFileReader.getPermittedSubtypeNames();

		assertEquals(1, permittedSubtypesNames.length);

		char [][] expected = {"X$1".toCharArray()};
		assertTrue(CharOperation.equals(permittedSubtypesNames, expected));

		int modifiers = classFileReader.getModifiers();
		assertTrue("sealed modifier expected", (modifiers & ExtraCompilerModifiers.AccSealed) != 0);
	}
	public void testBug565782_002() throws Exception {
		String source =
				"sealed interface I {}\n"+
				"class X {\n"+
				"	enum E implements I {\n"+
				"   	ONE {};\n"+
				"	}\n"+
				"   public static void main(String[] args) {\n"+
				"      	System.out.println(0);\n"+
				"   }\n"+
				"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X.E", "X$E", source);
		char[][] permittedSubtypesNames = classFileReader.getPermittedSubtypeNames();

		assertEquals(1, permittedSubtypesNames.length);

		char [][] expected = {"X$E$1".toCharArray()};
		assertTrue(CharOperation.equals(permittedSubtypesNames, expected));

		int modifiers = classFileReader.getModifiers();
		assertTrue("sealed modifier expected", (modifiers & ExtraCompilerModifiers.AccSealed) != 0);
	}
}
