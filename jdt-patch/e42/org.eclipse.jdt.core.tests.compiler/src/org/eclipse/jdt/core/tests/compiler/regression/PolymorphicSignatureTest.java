/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

public class PolymorphicSignatureTest extends AbstractRegressionTest {
	public PolymorphicSignatureTest(String name) {
		super(name);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_7);
	}
	public static Class testClass() {
		return PolymorphicSignatureTest.class;
	}
	
	public void test0001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.invoke.*;\n" +
				"public class X {\n" +
				"   public static void main(String[] args) throws Throwable{\n" +
				"      MethodType mt; MethodHandle mh; \n" +
				"      MethodHandles.Lookup lookup = MethodHandles.lookup();\n" +
				"      mt = MethodType.methodType(String.class, char.class, char.class);\n"+
				"      mh = lookup.findVirtual(String.class, \"replace\", mt);\n"+
				"      String s = (String) mh.invokeExact(\"daddy\",'d','n');\n"+
				"      System.out.println(s);\n"+
				"   }\n" +
				"}\n"
			},
			"nanny");
	}
	public void test0002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import static java.lang.invoke.MethodHandles.*; \n" + 
				"import java.lang.invoke.MethodHandle;\n" + 
				"public class X {\n" + 
				"	public static void main(String[] args) throws Throwable {\n" + 
				"		MethodHandle mh = dropArguments(insertArguments(identity(int.class), 0, 42), 0, Object[].class);\n" + 
				"		int value = (int)mh.invokeExact(new Object[0]);\n" +
				"		System.out.println(value);\n"+
				"	}\n" + 
				"}"
			},
			"42");
	}
}
