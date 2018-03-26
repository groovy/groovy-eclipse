/*******************************************************************************
 * Copyright (c) 2018 Jesper Steen Møller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     Jesper Steen Møller - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;

import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class JEP286ReservedWordTest extends AbstractRegressionTest {

public static Class testClass() {
	return JEP286ReservedWordTest.class;
}
public void initialize(CompilerTestSetup setUp) {
	super.initialize(setUp);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}

public JEP286ReservedWordTest(String testName){
	super(testName);
}

public void test0001_class_var_warning() throws IOException {
	String classVar =
		"	public class var { public int a; };\n";	

	String classX =
			"public class X {\n" + 
			classVar +
			"	+\n" +
			"}\n";
	String errorTail =
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	+\n" +
			"	^\n" +
			"Syntax error on token \"+\", delete this token\n" + 
			"----------\n";

	if (this.complianceLevel == ClassFileConstants.JDK10) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					classX
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				classVar +
				"	             ^^^\n" +
				"'var' is not a valid type name\n" +
				errorTail);
	} else {
		this.runNegativeTest(
				new String[] {
					"X.java",
					classX
				},
				"----------\n" +
				"1. WARNING in X.java (at line 2)\n" +
				classVar +
				"	             ^^^\n" +
				"'var' should not be used as an type name, since it is a reserved word from source level 10 on\n" +
				errorTail);
	}
}
public void test0002_interface_var_warning() throws IOException {
	String interfaceVar =
		"	interface var { };\n";	

	String classX =
			"public class X {\n" + 
			interfaceVar +
			"	+\n" +
			"}\n";
	String errorTail =
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	+\n" +
			"	^\n" +
			"Syntax error on token \"+\", delete this token\n" + 
			"----------\n";

	if (this.complianceLevel == ClassFileConstants.JDK10) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					classX
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				interfaceVar +
				"	          ^^^\n" +
				"'var' is not a valid type name\n" +
				errorTail);
	} else {
		this.runNegativeTest(
				new String[] {
					"X.java",
					classX
				},
				"----------\n" +
				"1. WARNING in X.java (at line 2)\n" +
				interfaceVar +
				"	          ^^^\n" +
				"'var' should not be used as an type name, since it is a reserved word from source level 10 on\n" +
				errorTail);
	}
}
public void testBug530920() throws IOException {
	String classX = "public class X<var extends Number> { }\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			classX
		},
		this.complianceLevel == ClassFileConstants.JDK10 ?
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X<var extends Number> { }\n" +
			"	               ^^^\n" +
			"'var' is not allowed here\n"
		:
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public class X<var extends Number> { }\n" +
			"	               ^^^\n" +
			"'var' should not be used as an type name, since it is a reserved word from source level 10 on\n"
		);
}
public void testBug530920a() throws IOException {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	<var extends Number> var getNumber() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}"	
		},
		this.complianceLevel == ClassFileConstants.JDK10 ?
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	<var extends Number> var getNumber() {\n" +
			"	 ^^^\n" +
			"'var' is not allowed here\n"
		:
			"----------\n" +
			"1. WARNING in X.java (at line 2)\n" +
			"	<var extends Number> var getNumber() {\n" +
			"	 ^^^\n" +
			"'var' should not be used as an type name, since it is a reserved word from source level 10 on\n"
		);
}
}
