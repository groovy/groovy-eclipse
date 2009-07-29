/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

public class EnclosingMethodAttributeTest extends AbstractComparableTest {
	public EnclosingMethodAttributeTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test127" };
//		TESTS_NUMBERS = new int[] { 176 };
//		TESTS_RANGE = new int[] { 169, 180 };
	}

	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {  
		return EnclosingMethodAttributeTest.class;
	}

	public void test001() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"public static void main(String[] args) throws Exception  {\n" +
				"	class MyLocal$A {\n" +
				"		class Member {\n" +
				"		}\n" +
				"	};\n" +
				"	System.out.print(MyLocal$A.Member.class.getEnclosingMethod() != null);\n" +
				"	System.out.print(MyLocal$A.Member.class.getEnclosingConstructor() != null);\n" +
				"\n" +
				"	System.out.print(MyLocal$A.class.getEnclosingMethod()!= null);\n" +
				"	System.out.print(MyLocal$A.class.getEnclosingConstructor() != null);	\n" +
				"	\n" +
				"	System.out.print(X.class.getEnclosingMethod() != null);\n" +
				"	System.out.print(X.class.getEnclosingConstructor() != null);	\n" +
				"}\n" +
				"public Object foo() {\n" +
				"	return new Object() {};\n" +
				"}\n" +
				"}"
			},
			"falsefalsetruefalsefalsefalse");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED); 
		
		String expectedOutput = "  Enclosing Method: #22  #24 X.foo()Ljava/lang/Object;\n";
			
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}		
	}
	
	public void test002() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"public static void main(String[] args) throws Exception  {\n" +
				"	class MyLocal$A {\n" +
				"		class Member {\n" +
				"			public Object foo() {\n" +
				"				return new Object() {};\n" +
				"			}\n" +
				"		}\n" +
				"	};\n" +
				"	System.out.print(MyLocal$A.Member.class.getEnclosingMethod() != null);\n" +
				"	System.out.print(MyLocal$A.Member.class.getEnclosingConstructor() != null);\n" +
				"\n" +
				"	System.out.print(MyLocal$A.class.getEnclosingMethod()!= null);\n" +
				"	System.out.print(MyLocal$A.class.getEnclosingConstructor() != null);	\n" +
				"	\n" +
				"	System.out.print(X.class.getEnclosingMethod() != null);\n" +
				"	System.out.print(X.class.getEnclosingConstructor() != null);	\n" +
				"}\n" +
				"}"
			},
			"falsefalsetruefalsefalsefalse");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  + "X$1MyLocal$A$Member$1.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED); 
		
		String expectedOutput = "  Enclosing Method: #22  #24 X$1MyLocal$A$Member.foo()Ljava/lang/Object;\n";
			
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}		
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162356
	public void test003() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.reflect.*;\n" + 
				"public class X {\n" + 
				"        public void test() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {\n" + 
				"                class LocalClass {\n" + 
				"                        public void method() {\n" + 
				"                        }\n" + 
				"                };\n" + 
				"                LocalClass localClass = new LocalClass();\n" + 
				"                Class cc = localClass.getClass();\n" + 
				"                System.out.println(\"enclosing class = \" + cc.getEnclosingClass());\n" + 
				"                System.out.println(\"enclosing method = \" + cc.getEnclosingMethod());\n" + 
				"        }\n" + 
				"        public static void main(String args[]) {\n" + 
				"                X t = new X();\n" + 
				"                try {\n" + 
				"                        t.test();\n" + 
				"                } catch (Exception e) {\n" + 
				"                        e.printStackTrace();\n" + 
				"                }\n" + 
				"        }\n" + 
				"}"
			},
			"enclosing class = class X\n" + 
			"enclosing method = public void X.test() throws java.lang.NoSuchMethodException,java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  + "X$1LocalClass.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED); 
		
		String expectedOutput =
			"  Inner classes:\n" + 
			"    [inner class info: #1 X$1LocalClass, outer class info: #0\n" + 
			"     inner name: #28 LocalClass, accessflags: 0 default]\n";
		
		// check inner classes info
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
		
		expectedOutput =
			"  Enclosing Method: #23  #25 X.test()V\n";
			
		// check enclosing method
		index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}
}
