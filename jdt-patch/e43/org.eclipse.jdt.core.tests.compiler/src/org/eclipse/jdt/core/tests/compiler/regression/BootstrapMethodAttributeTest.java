/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;

public class BootstrapMethodAttributeTest extends AbstractRegressionTest {
	public BootstrapMethodAttributeTest(String name) {
		super(name);
	}

	public static Class testClass() {
		return BootstrapMethodAttributeTest.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "testBug95521";
//		TESTS_NAMES = new String[] { "testBug359495" };
//		TESTS_NUMBERS = new int[] { 53 };
//		TESTS_RANGE = new int[] { 23 -1,};
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_7);
	}
	public void test001() throws Exception {

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "TestBootstrapMethodAtt.class";
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"//  (version 1.2 : 46.0, no super bit)\n" + 
					"public class test.G {\n" + 
					"  \n" + 
					"  // Method descriptor #2 ()V\n" + 
					"  // Stack: 0, Locals: 0\n" + 
					"  public static void call();\n" + 
					"    0  invokedynamic 0 dyn() : void [18]\n" + 
					"    5  return\n" + 
					"\n" + 
					"Bootstrap methods:\n" + 
					"  0 : # 17 arguments: {#1}\n" + 
					"}";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
}
