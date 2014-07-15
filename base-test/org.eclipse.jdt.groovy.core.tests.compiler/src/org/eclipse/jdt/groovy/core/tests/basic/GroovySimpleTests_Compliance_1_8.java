/*******************************************************************************
 * Copyright (c) 2009-2014 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.groovy.core.tests.basic;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class GroovySimpleTests_Compliance_1_8 extends AbstractGroovyRegressionTest {
	
	public static long JDK1_8 = 0x340000; /* ClassFileConstants.JDK1_8 */

	public GroovySimpleTests_Compliance_1_8(String name) {
		super(name);
	}

	public static Test suite() {
		return buildUniqueComplianceTestSuite(testClass(),
				JDK1_8);
	}

	public static Class testClass() {
		return GroovySimpleTests_Compliance_1_8.class;
	}

    protected void setUp() throws Exception {
		super.setUp();
		complianceLevel = JDK1_8;
	}

    public void testDefaultAndStaticMethodInInterface() {
		assertTrue("Groovy compiler levele is less than 2.3",
				GroovyUtils.GROOVY_LEVEL >= 23);
		assertTrue("JRE Compliance level is less than 1.8",
				isJRELevel(AbstractCompilerTest.F_1_8));
		Map customOptions= getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Source, "1.8" /* CompilerOptions.VERSION_1_8 */);		
		this.runConformTest(
				// test directory preparation
				true, /* flush output directory */
				new String[] { /* test files */
				"p/IExample.java",
				"package p;\n" + "public interface IExample {\n"
						+ "   void testExample();\n"
						+ "   static void callExample() {}\n" 
						+ "   default void callDefault() {}\n"
						+ "}\n",
				"p/ExampleGr.groovy",
				"package p\n" + "class ExampleGr implements IExample {\n"
						+ "public void testExample() {}\n" + "}\n" }, 		// compiler options
						null /* no class libraries */,
						customOptions /* custom options */,
						// compiler results
						"" /* expected compiler log */,
						// runtime results
						"" /* expected output string */,
						null /* do not check error string */,
						// javac options
						new JavacTestOptions("-source 1.8") /* javac test options */);
	}

}
