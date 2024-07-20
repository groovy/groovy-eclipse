/*******************************************************************************
 * Copyright (c) 2023 IBM corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;
public class StringConcatTest extends AbstractComparableTest {

	static {
		///	TESTS_NAMES = new String[] { "test001" };
	}

	public StringConcatTest(String name) {
		super(name);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(StringConcatTest.class, F_1_8);
	}

	private String getClassFileContents( String classFileName, int mode) throws IOException,
	ClassFormatException {
		File f = new File(OUTPUT_DIR + File.separator + classFileName);
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", mode);
		return result;
	}
	private void verifyOutput(String result, String expectedOutput, boolean positive) {
		int index = result.indexOf(expectedOutput);
		if (positive) {
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 3));
				System.out.println("...");
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
		} else {
			if (index != -1) {
				assertEquals("Unexpected contents", "", result);
			}
		}
	}
	private void verifyClassFile(String expectedOutput, String classFileName, int mode, boolean positive) throws IOException, ClassFormatException {
		if (this.complianceLevel < ClassFileConstants.JDK9)
			return;
		String result = getClassFileContents(classFileName, mode);
		verifyOutput(result, expectedOutput, positive);
	}

	public void test001() throws IOException, ClassFormatException {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "  public static void main(String[] args) {\n"
					+ "    int one = 1;\n"
					+ "    float two = 2;\n"
					+ "    double three = 3d;\n"
					+ "    long four = 4L;\n"
					+ "    byte five = (byte) 5;\n"
					+ "    short six = 6;\n"
					+ "    char seven = 7;\n"
					+ "    boolean b = false;\n"
					+ "    String s = \"one=\" + one + \", two=\" + two + \", three=\" + three +\", four=\" + four +\", five=\" + five + \", six=\" + six + \", seven=\" + seven + \", boolean b=\" + b;\n"
					+ "    s += \".\";\n"
					+ "    System.out.println(s);\n"
					+ "  }\n"
					+ "} ",
				},
				"one=1, two=2.0, three=3.0, four=4, five=5, six=6, seven=, boolean b=false."
			);
		String expectedOutput = "  // Stack: 10, Locals: 12\n" +
				"  public static void main(java.lang.String[] args);\n" +
				"     0  iconst_1\n" +
				"     1  istore_1 [one]\n" +
				"     2  fconst_2\n" +
				"     3  fstore_2 [two]\n" +
				"     4  ldc2_w <Double 3.0> [16]\n" +
				"     7  dstore_3 [three]\n" +
				"     8  ldc2_w <Long 4> [18]\n" +
				"    11  lstore 5 [four]\n" +
				"    13  iconst_5\n" +
				"    14  istore 7 [five]\n" +
				"    16  bipush 6\n" +
				"    18  istore 8 [six]\n" +
				"    20  bipush 7\n" +
				"    22  istore 9 [seven]\n" +
				"    24  iconst_0\n" +
				"    25  istore 10 [b]\n" +
				"    27  iload_1 [one]\n" +
				"    28  fload_2 [two]\n" +
				"    29  dload_3 [three]\n" +
				"    30  lload 5 [four]\n" +
				"    32  iload 7 [five]\n" +
				"    34  iload 8 [six]\n" +
				"    36  iload 9 [seven]\n" +
				"    38  iload 10 [b]\n" +
				"    40  invokedynamic 0 makeConcatWithConstants(int, float, double, long, byte, short, char, boolean) : java.lang.String [20]\n" +
				"    45  astore 11 [s]\n" +
				"    47  aload 11 [s]\n" +
				"    49  invokedynamic 1 makeConcatWithConstants(java.lang.String) : java.lang.String [24]\n" +
				"    54  astore 11 [s]\n" +
				"    56  getstatic java.lang.System.out : java.io.PrintStream [27]\n" +
				"    59  aload 11 [s]\n" +
				"    61  invokevirtual java.io.PrintStream.println(java.lang.String) : void [33]\n" +
				"    64  return\n";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM, true);
	}
	// Test string concat for a simple BinaryExpression
	public void test002() throws IOException, ClassFormatException {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "  public static void main(String[] args) {\n"
					+ "	  new X().foo();\n"
					+ "  }\n"
					+ "  public void foo() {\n"
					+ "	  int one = 1;\n"
					+ "	  String s = \"one=\" + one;\n"
					+ "	  System.out.println(s);\n"
					+ "  }\n"
					+ "}",
				},
				"one=1"
			);
		String expectedOutput = "  // Stack: 2, Locals: 3\n" +
				"  public void foo();\n" +
				"     0  iconst_1\n" +
				"     1  istore_1 [one]\n" +
				"     2  iload_1 [one]\n" +
				"     3  invokedynamic 0 makeConcatWithConstants(int) : java.lang.String [22]\n" +
				"     8  astore_2 [s]\n" +
				"     9  getstatic java.lang.System.out : java.io.PrintStream [26]\n" +
				"    12  aload_2 [s]\n" +
				"    13  invokevirtual java.io.PrintStream.println(java.lang.String) : void [32]\n" +
				"    16  return\n";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM, true);
	}
	// Test binary expression whose first operand is a field and therefore already loaded into the stack
	public void test003() throws IOException, ClassFormatException {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "	String v;\n"
					+ "  public static void main(String[] args) {\n"
					+ "	  new X().foo();\n"
					+ "  }\n"
					+ "  public void foo() {\n"
					+ "	  int one = 1;\n"
					+ "	    float two = 2;\n"
					+ "	    double three = 3d;\n"
					+ "	    long four = 4L;\n"
					+ "	    byte five = (byte) 5;\n"
					+ "	    short six = 6;\n"
					+ "	    char seven = 7;\n"
					+ "	    boolean b = false;\n"
					+ "	  v += \",one=\" + one + \", two=\" + two + \", three=\" + three +\", four=\" + four +\", five=\" + five + \", six=\" + six + \", seven=\" + seven + \", boolean b=\" + b;\n"
					+ "	  System.out.println(this.v);\n"
					+ "  }\n"
					+ "}",
				},
				"null,one=1, two=2.0, three=3.0, four=4, five=5, six=6, seven=, boolean b=false"
			);
		String expectedOutput = "  // Stack: 12, Locals: 11\n" +
				"  public void foo();\n" +
				"     0  iconst_1\n" +
				"     1  istore_1 [one]\n" +
				"     2  fconst_2\n" +
				"     3  fstore_2 [two]\n" +
				"     4  ldc2_w <Double 3.0> [24]\n" +
				"     7  dstore_3 [three]\n" +
				"     8  ldc2_w <Long 4> [26]\n" +
				"    11  lstore 5 [four]\n" +
				"    13  iconst_5\n" +
				"    14  istore 7 [five]\n" +
				"    16  bipush 6\n" +
				"    18  istore 8 [six]\n" +
				"    20  bipush 7\n" +
				"    22  istore 9 [seven]\n" +
				"    24  iconst_0\n" +
				"    25  istore 10 [b]\n" +
				"    27  aload_0 [this]\n" +
				"    28  dup\n" +
				"    29  getfield X.v : java.lang.String [28]\n" +
				"    32  invokestatic java.lang.String.valueOf(java.lang.Object) : java.lang.String [30]\n" +
				"    35  iload_1 [one]\n" +
				"    36  fload_2 [two]\n" +
				"    37  dload_3 [three]\n" +
				"    38  lload 5 [four]\n" +
				"    40  iload 7 [five]\n" +
				"    42  iload 8 [six]\n" +
				"    44  iload 9 [seven]\n" +
				"    46  iload 10 [b]\n" +
				"    48  invokedynamic 0 makeConcatWithConstants(java.lang.String, int, float, double, long, byte, short, char, boolean) : java.lang.String [36]\n" +
				"    53  putfield X.v : java.lang.String [28]\n" +
				"    56  getstatic java.lang.System.out : java.io.PrintStream [40]\n" +
				"    59  aload_0 [this]\n" +
				"    60  getfield X.v : java.lang.String [28]\n" +
				"    63  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]\n" +
				"    66  return\n";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM, true);
	}
	// Test a binary expression (with string concat) whose one operand is not a string type
	public void test004() throws IOException, ClassFormatException {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "	int count = 0;\n"
					+ "  public static void main(String[] args) {\n"
					+ "	  new X().foo();\n"
					+ "  }\n"
					+ "  public void foo() {\n"
					+ "	  System.out.println(this.toString());\n"
					+ "  }\n"
					+ "  public String toString() {\n"
					+ "	  return \"count=\" + (this.count + 1);\n"
					+ "  }\n"
					+ "} ",
				},
				"count=1"
			);
		String expectedOutput = "  // Stack: 2, Locals: 1\n" +
				"  public java.lang.String toString();\n" +
				"     0  aload_0 [this]\n" +
				"     1  getfield X.count : int [12]\n" +
				"     4  iconst_1\n" +
				"     5  iadd\n" +
				"     6  invokedynamic 0 makeConcatWithConstants(int) : java.lang.String [42]\n" +
				"    11  areturn\n";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM, true);
	}

	// Test for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1201
	public void test005() throws Exception {
		this.runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void main(String[] args) {\n"
						+ "		int first = 11;\n"
						+ "		int second = 42;\n"
						+ "		String actual =\n"
						+ "				\"X \" + 0 + \" \" +\n"
						+ "				\"a \" + first + \" \" +\n"
						+ "				\"a \" + first + \" \" +\n"
						+ "				\"X \" + 1 + \" \" +\n"
						+ "				\"b \" + second + \" \" +\n"
						+ "				\"b \" + second + \" \" +\n"
						+ "				\"b \" + second + \" \" +\n"
						+ "				\"b \" + second;\n"
						+ "		System.out.println(actual);\n"
						+ "	}\n"
						+ "}\n",
				},
				"X 0 a 11 a 11 X 1 b 42 b 42 b 42 b 42"
				);
		String expectedOutput = "  // Stack: 6, Locals: 4\n"
				+ "  public static void main(java.lang.String[] args);\n"
				+ "     0  bipush 11\n"
				+ "     2  istore_1 [first]\n"
				+ "     3  bipush 42\n"
				+ "     5  istore_2 [second]\n"
				+ "     6  iload_1 [first]\n"
				+ "     7  iload_1 [first]\n"
				+ "     8  iload_2 [second]\n"
				+ "     9  iload_2 [second]\n"
				+ "    10  iload_2 [second]\n"
				+ "    11  iload_2 [second]\n"
				+ "    12  invokedynamic 0 makeConcatWithConstants(int, int, int, int, int, int) : java.lang.String [16]\n"
				+ "    17  astore_3 [actual]\n"
				+ "    18  getstatic java.lang.System.out : java.io.PrintStream [20]\n"
				+ "    21  aload_3 [actual]\n"
				+ "    22  invokevirtual java.io.PrintStream.println(java.lang.String) : void [26]\n"
				+ "    25  return\n";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM, true);
	}
}