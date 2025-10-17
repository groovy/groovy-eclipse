/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ForStatementTest extends AbstractRegressionTest {

public ForStatementTest(String name) {
	super(name);
}

@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	return options;
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 45, 46 };
//	TESTS_RANGE = new int[] { 34, 38 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
public void test001() {
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"	public static Object m(int[] arg) {\n" +
				"		yyLoop: for (int i = 0;; ++i) {\n" +
				"			yyInner: for (;;) {\n" +
				"				switch (arg[i]) {\n" +
				"					case 0:\n" +
				"						break;\n" +
				"					case 1:\n" +
				"						continue yyInner;\n" +
				"				}\n" +
				"				if (i == 32)\n" +
				"					return arg;\n" +
				"				if (i == 12)\n" +
				"					break;\n" +
				"				continue yyLoop;\n" +
				"			}\n" +
				"			if (i == 32)\n" +
				"				return null;\n" +
				"			if (i > 7)\n" +
				"				continue yyLoop;\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}\n",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=180471
public void test002() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo2(int[] array) {\n" +
			"		for (int i = 0; i < array.length; i++) {\n" +
			"			System.out.println(i);\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");

	String expectedOutput =
		"  // Method descriptor #15 ([I)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  void foo2(int[] array);\n" +
		"     0  iconst_0\n" +
		"     1  istore_2 [i]\n" +
		"     2  iload_2 [i]\n" +
		"     3  aload_1 [array]\n" +
		"     4  arraylength\n" +
		"     5  if_icmpge 15\n" +
		"     8  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    11  iload_2 [i]\n" +
		"    12  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
		"    15  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 8, line: 4]\n" +
		"        [pc: 15, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 16] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 16] local: array index: 1 type: int[]\n" +
		"        [pc: 2, pc: 15] local: i index: 2 type: int\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=180471 - variation
public void test003() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo4(int[] array) {\n" +
			"		do {\n" +
			"			System.out.println();\n" +
			"			break;\n" +
			"		} while (array.length > 0);\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");

	String expectedOutput =
		"  // Method descriptor #15 ([I)V\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  void foo4(int[] array);\n" +
		"    0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    3  invokevirtual java.io.PrintStream.println() : void [22]\n" +
		"    6  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 4]\n" +
		"        [pc: 6, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 7] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 7] local: array index: 1 type: int[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=180471 - variation
public void test004() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo1(int[] array) {\n" +
			"		while (array.length > 0) {\n" +
			"			System.out.println();\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");

	String expectedOutput =
		"  // Method descriptor #15 ([I)V\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  void foo1(int[] array);\n" +
		"     0  aload_1 [array]\n" +
		"     1  arraylength\n" +
		"     2  ifle 11\n" +
		"     5  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"     8  invokevirtual java.io.PrintStream.println() : void [22]\n" +
		"    11  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 5, line: 4]\n" +
		"        [pc: 11, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 12] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 12] local: array index: 1 type: int[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=195317
public void test005() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		int mode = 1;\n" +
			"		loop: for (;;) {\n" +
			"			switch (mode) {\n" +
			"				case 2 :\n" +
			"					return;\n" +
			"				case 1:\n" +
			"					mode = 2;\n" +
			"					continue loop;\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  istore_1 [mode]\n" +
		"     2  iload_1 [mode]\n" +
		"     3  tableswitch default: 27\n" +
		"          case 1: 25\n" +
		"          case 2: 24\n" +
		"    24  return\n" +
		"    25  iconst_2\n" +
		"    26  istore_1 [mode]\n" +
		"    27  goto 2\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 2, line: 5]\n" +
		"        [pc: 24, line: 7]\n" +
		"        [pc: 25, line: 9]\n" +
		"        [pc: 27, line: 4]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 30] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 2, pc: 30] local: mode index: 1 type: int\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test006() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public final class X {\n" +
			"\n" +
			"  public void show() {\n" +
			"    String s1 = \"\";   String s2 = \"\";   String s3 = \"\";   String s4 = \"\";   String s5 = \"\";   String s6 = \"\";   String s7 = \"\";   String s8 = \"\";   String s9 = \"\";   String s10 = \"\";\n" +
			"    String s11 = \"\";  String s12 = \"\";  String s13 = \"\";  String s14 = \"\";  String s15 = \"\";  String s16 = \"\";  String s17 = \"\";  String s18 = \"\";  String s19 = \"\";  String s20 = \"\";\n" +
			"    String s21 = \"\";  String s22 = \"\";  String s23 = \"\";  String s24 = \"\";  String s25 = \"\";  String s26 = \"\";  String s27 = \"\";  String s28 = \"\";  String s29 = \"\";  String s30 = \"\";\n" +
			"    String s31 = \"\";  String s32 = \"\";  String s33 = \"\";  String s34 = \"\";  String s35 = \"\";  String s36 = \"\";  String s37 = \"\";  String s38 = \"\";  String s39 = \"\";  String s40 = \"\";\n" +
			"    String s41 = \"\";  String s42 = \"\";  String s43 = \"\";  String s44 = \"\";  String s45 = \"\";  String s46 = \"\";  String s47 = \"\";  String s48 = \"\";  String s49 = \"\";  String s50 = \"\";\n" +
			"    String s51 = \"\";  String s52 = \"\";  String s53 = \"\";  String s54 = \"\";  String s55 = \"\";  String s56 = \"\";  String s57 = \"\";  String s58 = \"\";  String s59 = \"\";  String s60 = \"\";\n" +
			"    String s61 = \"\";  String s62 = \"\";  String s63 = \"\";  String s64 = \"\";  String s65 = \"\";  String s66 = \"\";  String s67 = \"\";  String s68 = \"\";  String s69 = \"\";  String s70 = \"\";\n" +
			"    String s71 = \"\";  String s72 = \"\";  String s73 = \"\";  String s74 = \"\";  String s75 = \"\";  String s76 = \"\";  String s77 = \"\";  String s78 = \"\";  String s79 = \"\";  String s80 = \"\";\n" +
			"    String s81 = \"\";  String s82 = \"\";  String s83 = \"\";  String s84 = \"\";  String s85 = \"\";  String s86 = \"\";  String s87 = \"\";  String s88 = \"\";  String s89 = \"\";  String s90 = \"\";\n" +
			"    String s91 = \"\";  String s92 = \"\";  String s93 = \"\";  String s94 = \"\";  String s95 = \"\";  String s96 = \"\";  String s97 = \"\";  String s98 = \"\";  String s99 = \"\";  String s100 = \"\";\n" +
			"    String s101 = \"\"; String s102 = \"\"; String s103 = \"\"; String s104 = \"\"; String s105 = \"\"; String s106 = \"\"; String s107 = \"\"; String s108 = \"\"; String s109 = \"\"; String s110 = \"\";\n" +
			"    String s111 = \"\"; String s112 = \"\"; String s113 = \"\"; String s114 = \"\"; String s115 = \"\"; String s116 = \"\"; String s117 = \"\"; String s118 = \"\"; String s119 = \"\"; String s120 = \"\";\n" +
			"    String s121 = \"\"; String s122 = \"\"; String s123 = \"\"; String s124 = \"\"; String s125 = \"\"; String s126 = \"\"; String s127 = \"\"; String s128 = \"\"; String s129 = \"\"; String s130 = \"\";\n" +
			"    String s131 = \"\"; String s132 = \"\"; String s133 = \"\"; String s134 = \"\"; String s135 = \"\"; String s136 = \"\"; String s137 = \"\"; String s138 = \"\"; String s139 = \"\"; String s140 = \"\";\n" +
			"    String s141 = \"\"; String s142 = \"\"; String s143 = \"\"; String s144 = \"\"; String s145 = \"\"; String s146 = \"\"; String s147 = \"\"; String s148 = \"\"; String s149 = \"\"; String s150 = \"\";\n" +
			"    String s151 = \"\"; String s152 = \"\"; String s153 = \"\"; String s154 = \"\"; String s155 = \"\"; String s156 = \"\"; String s157 = \"\"; String s158 = \"\"; String s159 = \"\"; String s160 = \"\";\n" +
			"    String s161 = \"\"; String s162 = \"\"; String s163 = \"\"; String s164 = \"\"; String s165 = \"\"; String s166 = \"\"; String s167 = \"\"; String s168 = \"\"; String s169 = \"\"; String s170 = \"\";\n" +
			"    String s171 = \"\"; String s172 = \"\"; String s173 = \"\"; String s174 = \"\"; String s175 = \"\"; String s176 = \"\"; String s177 = \"\"; String s178 = \"\"; String s179 = \"\"; String s180 = \"\";\n" +
			"    String s181 = \"\"; String s182 = \"\"; String s183 = \"\"; String s184 = \"\"; String s185 = \"\"; String s186 = \"\"; String s187 = \"\"; String s188 = \"\"; String s189 = \"\"; String s190 = \"\";\n" +
			"    String s191 = \"\"; String s192 = \"\"; String s193 = \"\"; String s194 = \"\"; String s195 = \"\"; String s196 = \"\"; String s197 = \"\"; String s198 = \"\"; String s199 = \"\"; String s200 = \"\";\n" +
			"    String s201 = \"\"; String s202 = \"\"; String s203 = \"\"; String s204 = \"\"; String s205 = \"\"; String s206 = \"\"; String s207 = \"\"; String s208 = \"\"; String s209 = \"\"; String s210 = \"\";\n" +
			"    String s211 = \"\"; String s212 = \"\"; String s213 = \"\"; String s214 = \"\"; String s215 = \"\"; String s216 = \"\"; String s217 = \"\"; String s218 = \"\"; String s219 = \"\"; String s220 = \"\";\n" +
			"    String s221 = \"\"; String s222 = \"\"; String s223 = \"\"; String s224 = \"\"; String s225 = \"\"; String s226 = \"\"; String s227 = \"\"; String s228 = \"\"; String s229 = \"\"; String s230 = \"\";\n" +
			"    String s231 = \"\"; String s232 = \"\"; String s233 = \"\"; String s234 = \"\"; String s235 = \"\"; String s236 = \"\"; String s237 = \"\"; String s238 = \"\"; String s239 = \"\"; String s240 = \"\";\n" +
			"    String s241 = \"\"; String s242 = \"\"; String s243 = \"\"; String s244 = \"\"; String s245 = \"\"; String s246 = \"\"; String s247 = \"\"; String s248 = \"\"; String s249 = \"\"; String s250 = \"\";\n" +
			"    String s251 = \"\"; String s252 = \"\";\n" +
			"\n" +
			"    int size1 = 1;\n" +
			"    int size2 = 2;\n" +
			"    int size3 = 3;\n" +
			"\n" +
			"    int[][][] intArray = new int[size1][size2][size3];\n" +
			"    \n" +
			"    for (int i = 0; i < size1; i++) {\n" +
			"      for (int j = 0; j < size2; j++) {\n" +
			"        boolean on = false;\n" +
			"        for (int k = 0; k < size3; k++) {\n" +
			"          intArray[i][j][k] = on ? 0 : 1;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) {\n" +
			"    new X().show();\n" +
			"  }\n" +
			"}",
		},
		"",
		settings);

	String expectedOutput =
			"  public void show();\n" +
			"       0  ldc <String \"\"> [15]\n" +
			"       2  astore_1 [s1]\n" +
			"       3  ldc <String \"\"> [15]\n" +
			"       5  astore_2 [s2]\n" +
			"       6  ldc <String \"\"> [15]\n" +
			"       8  astore_3 [s3]\n" +
			"       9  ldc <String \"\"> [15]\n" +
			"      11  astore 4 [s4]\n" +
			"      13  ldc <String \"\"> [15]\n" +
			"      15  astore 5 [s5]\n" +
			"      17  ldc <String \"\"> [15]\n" +
			"      19  astore 6 [s6]\n" +
			"      21  ldc <String \"\"> [15]\n" +
			"      23  astore 7 [s7]\n" +
			"      25  ldc <String \"\"> [15]\n" +
			"      27  astore 8 [s8]\n" +
			"      29  ldc <String \"\"> [15]\n" +
			"      31  astore 9 [s9]\n" +
			"      33  ldc <String \"\"> [15]\n" +
			"      35  astore 10 [s10]\n" +
			"      37  ldc <String \"\"> [15]\n" +
			"      39  astore 11 [s11]\n" +
			"      41  ldc <String \"\"> [15]\n" +
			"      43  astore 12 [s12]\n" +
			"      45  ldc <String \"\"> [15]\n" +
			"      47  astore 13 [s13]\n" +
			"      49  ldc <String \"\"> [15]\n" +
			"      51  astore 14 [s14]\n" +
			"      53  ldc <String \"\"> [15]\n" +
			"      55  astore 15 [s15]\n" +
			"      57  ldc <String \"\"> [15]\n" +
			"      59  astore 16 [s16]\n" +
			"      61  ldc <String \"\"> [15]\n" +
			"      63  astore 17 [s17]\n" +
			"      65  ldc <String \"\"> [15]\n" +
			"      67  astore 18 [s18]\n" +
			"      69  ldc <String \"\"> [15]\n" +
			"      71  astore 19 [s19]\n" +
			"      73  ldc <String \"\"> [15]\n" +
			"      75  astore 20 [s20]\n" +
			"      77  ldc <String \"\"> [15]\n" +
			"      79  astore 21 [s21]\n" +
			"      81  ldc <String \"\"> [15]\n" +
			"      83  astore 22 [s22]\n" +
			"      85  ldc <String \"\"> [15]\n" +
			"      87  astore 23 [s23]\n" +
			"      89  ldc <String \"\"> [15]\n" +
			"      91  astore 24 [s24]\n" +
			"      93  ldc <String \"\"> [15]\n" +
			"      95  astore 25 [s25]\n" +
			"      97  ldc <String \"\"> [15]\n" +
			"      99  astore 26 [s26]\n" +
			"     101  ldc <String \"\"> [15]\n" +
			"     103  astore 27 [s27]\n" +
			"     105  ldc <String \"\"> [15]\n" +
			"     107  astore 28 [s28]\n" +
			"     109  ldc <String \"\"> [15]\n" +
			"     111  astore 29 [s29]\n" +
			"     113  ldc <String \"\"> [15]\n" +
			"     115  astore 30 [s30]\n" +
			"     117  ldc <String \"\"> [15]\n" +
			"     119  astore 31 [s31]\n" +
			"     121  ldc <String \"\"> [15]\n" +
			"     123  astore 32 [s32]\n" +
			"     125  ldc <String \"\"> [15]\n" +
			"     127  astore 33 [s33]\n" +
			"     129  ldc <String \"\"> [15]\n" +
			"     131  astore 34 [s34]\n" +
			"     133  ldc <String \"\"> [15]\n" +
			"     135  astore 35 [s35]\n" +
			"     137  ldc <String \"\"> [15]\n" +
			"     139  astore 36 [s36]\n" +
			"     141  ldc <String \"\"> [15]\n" +
			"     143  astore 37 [s37]\n" +
			"     145  ldc <String \"\"> [15]\n" +
			"     147  astore 38 [s38]\n" +
			"     149  ldc <String \"\"> [15]\n" +
			"     151  astore 39 [s39]\n" +
			"     153  ldc <String \"\"> [15]\n" +
			"     155  astore 40 [s40]\n" +
			"     157  ldc <String \"\"> [15]\n" +
			"     159  astore 41 [s41]\n" +
			"     161  ldc <String \"\"> [15]\n" +
			"     163  astore 42 [s42]\n" +
			"     165  ldc <String \"\"> [15]\n" +
			"     167  astore 43 [s43]\n" +
			"     169  ldc <String \"\"> [15]\n" +
			"     171  astore 44 [s44]\n" +
			"     173  ldc <String \"\"> [15]\n" +
			"     175  astore 45 [s45]\n" +
			"     177  ldc <String \"\"> [15]\n" +
			"     179  astore 46 [s46]\n" +
			"     181  ldc <String \"\"> [15]\n" +
			"     183  astore 47 [s47]\n" +
			"     185  ldc <String \"\"> [15]\n" +
			"     187  astore 48 [s48]\n" +
			"     189  ldc <String \"\"> [15]\n" +
			"     191  astore 49 [s49]\n" +
			"     193  ldc <String \"\"> [15]\n" +
			"     195  astore 50 [s50]\n" +
			"     197  ldc <String \"\"> [15]\n" +
			"     199  astore 51 [s51]\n" +
			"     201  ldc <String \"\"> [15]\n" +
			"     203  astore 52 [s52]\n" +
			"     205  ldc <String \"\"> [15]\n" +
			"     207  astore 53 [s53]\n" +
			"     209  ldc <String \"\"> [15]\n" +
			"     211  astore 54 [s54]\n" +
			"     213  ldc <String \"\"> [15]\n" +
			"     215  astore 55 [s55]\n" +
			"     217  ldc <String \"\"> [15]\n" +
			"     219  astore 56 [s56]\n" +
			"     221  ldc <String \"\"> [15]\n" +
			"     223  astore 57 [s57]\n" +
			"     225  ldc <String \"\"> [15]\n" +
			"     227  astore 58 [s58]\n" +
			"     229  ldc <String \"\"> [15]\n" +
			"     231  astore 59 [s59]\n" +
			"     233  ldc <String \"\"> [15]\n" +
			"     235  astore 60 [s60]\n" +
			"     237  ldc <String \"\"> [15]\n" +
			"     239  astore 61 [s61]\n" +
			"     241  ldc <String \"\"> [15]\n" +
			"     243  astore 62 [s62]\n" +
			"     245  ldc <String \"\"> [15]\n" +
			"     247  astore 63 [s63]\n" +
			"     249  ldc <String \"\"> [15]\n" +
			"     251  astore 64 [s64]\n" +
			"     253  ldc <String \"\"> [15]\n" +
			"     255  astore 65 [s65]\n" +
			"     257  ldc <String \"\"> [15]\n" +
			"     259  astore 66 [s66]\n" +
			"     261  ldc <String \"\"> [15]\n" +
			"     263  astore 67 [s67]\n" +
			"     265  ldc <String \"\"> [15]\n" +
			"     267  astore 68 [s68]\n" +
			"     269  ldc <String \"\"> [15]\n" +
			"     271  astore 69 [s69]\n" +
			"     273  ldc <String \"\"> [15]\n" +
			"     275  astore 70 [s70]\n" +
			"     277  ldc <String \"\"> [15]\n" +
			"     279  astore 71 [s71]\n" +
			"     281  ldc <String \"\"> [15]\n" +
			"     283  astore 72 [s72]\n" +
			"     285  ldc <String \"\"> [15]\n" +
			"     287  astore 73 [s73]\n" +
			"     289  ldc <String \"\"> [15]\n" +
			"     291  astore 74 [s74]\n" +
			"     293  ldc <String \"\"> [15]\n" +
			"     295  astore 75 [s75]\n" +
			"     297  ldc <String \"\"> [15]\n" +
			"     299  astore 76 [s76]\n" +
			"     301  ldc <String \"\"> [15]\n" +
			"     303  astore 77 [s77]\n" +
			"     305  ldc <String \"\"> [15]\n" +
			"     307  astore 78 [s78]\n" +
			"     309  ldc <String \"\"> [15]\n" +
			"     311  astore 79 [s79]\n" +
			"     313  ldc <String \"\"> [15]\n" +
			"     315  astore 80 [s80]\n" +
			"     317  ldc <String \"\"> [15]\n" +
			"     319  astore 81 [s81]\n" +
			"     321  ldc <String \"\"> [15]\n" +
			"     323  astore 82 [s82]\n" +
			"     325  ldc <String \"\"> [15]\n" +
			"     327  astore 83 [s83]\n" +
			"     329  ldc <String \"\"> [15]\n" +
			"     331  astore 84 [s84]\n" +
			"     333  ldc <String \"\"> [15]\n" +
			"     335  astore 85 [s85]\n" +
			"     337  ldc <String \"\"> [15]\n" +
			"     339  astore 86 [s86]\n" +
			"     341  ldc <String \"\"> [15]\n" +
			"     343  astore 87 [s87]\n" +
			"     345  ldc <String \"\"> [15]\n" +
			"     347  astore 88 [s88]\n" +
			"     349  ldc <String \"\"> [15]\n" +
			"     351  astore 89 [s89]\n" +
			"     353  ldc <String \"\"> [15]\n" +
			"     355  astore 90 [s90]\n" +
			"     357  ldc <String \"\"> [15]\n" +
			"     359  astore 91 [s91]\n" +
			"     361  ldc <String \"\"> [15]\n" +
			"     363  astore 92 [s92]\n" +
			"     365  ldc <String \"\"> [15]\n" +
			"     367  astore 93 [s93]\n" +
			"     369  ldc <String \"\"> [15]\n" +
			"     371  astore 94 [s94]\n" +
			"     373  ldc <String \"\"> [15]\n" +
			"     375  astore 95 [s95]\n" +
			"     377  ldc <String \"\"> [15]\n" +
			"     379  astore 96 [s96]\n" +
			"     381  ldc <String \"\"> [15]\n" +
			"     383  astore 97 [s97]\n" +
			"     385  ldc <String \"\"> [15]\n" +
			"     387  astore 98 [s98]\n" +
			"     389  ldc <String \"\"> [15]\n" +
			"     391  astore 99 [s99]\n" +
			"     393  ldc <String \"\"> [15]\n" +
			"     395  astore 100 [s100]\n" +
			"     397  ldc <String \"\"> [15]\n" +
			"     399  astore 101 [s101]\n" +
			"     401  ldc <String \"\"> [15]\n" +
			"     403  astore 102 [s102]\n" +
			"     405  ldc <String \"\"> [15]\n" +
			"     407  astore 103 [s103]\n" +
			"     409  ldc <String \"\"> [15]\n" +
			"     411  astore 104 [s104]\n" +
			"     413  ldc <String \"\"> [15]\n" +
			"     415  astore 105 [s105]\n" +
			"     417  ldc <String \"\"> [15]\n" +
			"     419  astore 106 [s106]\n" +
			"     421  ldc <String \"\"> [15]\n" +
			"     423  astore 107 [s107]\n" +
			"     425  ldc <String \"\"> [15]\n" +
			"     427  astore 108 [s108]\n" +
			"     429  ldc <String \"\"> [15]\n" +
			"     431  astore 109 [s109]\n" +
			"     433  ldc <String \"\"> [15]\n" +
			"     435  astore 110 [s110]\n" +
			"     437  ldc <String \"\"> [15]\n" +
			"     439  astore 111 [s111]\n" +
			"     441  ldc <String \"\"> [15]\n" +
			"     443  astore 112 [s112]\n" +
			"     445  ldc <String \"\"> [15]\n" +
			"     447  astore 113 [s113]\n" +
			"     449  ldc <String \"\"> [15]\n" +
			"     451  astore 114 [s114]\n" +
			"     453  ldc <String \"\"> [15]\n" +
			"     455  astore 115 [s115]\n" +
			"     457  ldc <String \"\"> [15]\n" +
			"     459  astore 116 [s116]\n" +
			"     461  ldc <String \"\"> [15]\n" +
			"     463  astore 117 [s117]\n" +
			"     465  ldc <String \"\"> [15]\n" +
			"     467  astore 118 [s118]\n" +
			"     469  ldc <String \"\"> [15]\n" +
			"     471  astore 119 [s119]\n" +
			"     473  ldc <String \"\"> [15]\n" +
			"     475  astore 120 [s120]\n" +
			"     477  ldc <String \"\"> [15]\n" +
			"     479  astore 121 [s121]\n" +
			"     481  ldc <String \"\"> [15]\n" +
			"     483  astore 122 [s122]\n" +
			"     485  ldc <String \"\"> [15]\n" +
			"     487  astore 123 [s123]\n" +
			"     489  ldc <String \"\"> [15]\n" +
			"     491  astore 124 [s124]\n" +
			"     493  ldc <String \"\"> [15]\n" +
			"     495  astore 125 [s125]\n" +
			"     497  ldc <String \"\"> [15]\n" +
			"     499  astore 126 [s126]\n" +
			"     501  ldc <String \"\"> [15]\n" +
			"     503  astore 127 [s127]\n" +
			"     505  ldc <String \"\"> [15]\n" +
			"     507  astore 128 [s128]\n" +
			"     509  ldc <String \"\"> [15]\n" +
			"     511  astore 129 [s129]\n" +
			"     513  ldc <String \"\"> [15]\n" +
			"     515  astore 130 [s130]\n" +
			"     517  ldc <String \"\"> [15]\n" +
			"     519  astore 131 [s131]\n" +
			"     521  ldc <String \"\"> [15]\n" +
			"     523  astore 132 [s132]\n" +
			"     525  ldc <String \"\"> [15]\n" +
			"     527  astore 133 [s133]\n" +
			"     529  ldc <String \"\"> [15]\n" +
			"     531  astore 134 [s134]\n" +
			"     533  ldc <String \"\"> [15]\n" +
			"     535  astore 135 [s135]\n" +
			"     537  ldc <String \"\"> [15]\n" +
			"     539  astore 136 [s136]\n" +
			"     541  ldc <String \"\"> [15]\n" +
			"     543  astore 137 [s137]\n" +
			"     545  ldc <String \"\"> [15]\n" +
			"     547  astore 138 [s138]\n" +
			"     549  ldc <String \"\"> [15]\n" +
			"     551  astore 139 [s139]\n" +
			"     553  ldc <String \"\"> [15]\n" +
			"     555  astore 140 [s140]\n" +
			"     557  ldc <String \"\"> [15]\n" +
			"     559  astore 141 [s141]\n" +
			"     561  ldc <String \"\"> [15]\n" +
			"     563  astore 142 [s142]\n" +
			"     565  ldc <String \"\"> [15]\n" +
			"     567  astore 143 [s143]\n" +
			"     569  ldc <String \"\"> [15]\n" +
			"     571  astore 144 [s144]\n" +
			"     573  ldc <String \"\"> [15]\n" +
			"     575  astore 145 [s145]\n" +
			"     577  ldc <String \"\"> [15]\n" +
			"     579  astore 146 [s146]\n" +
			"     581  ldc <String \"\"> [15]\n" +
			"     583  astore 147 [s147]\n" +
			"     585  ldc <String \"\"> [15]\n" +
			"     587  astore 148 [s148]\n" +
			"     589  ldc <String \"\"> [15]\n" +
			"     591  astore 149 [s149]\n" +
			"     593  ldc <String \"\"> [15]\n" +
			"     595  astore 150 [s150]\n" +
			"     597  ldc <String \"\"> [15]\n" +
			"     599  astore 151 [s151]\n" +
			"     601  ldc <String \"\"> [15]\n" +
			"     603  astore 152 [s152]\n" +
			"     605  ldc <String \"\"> [15]\n" +
			"     607  astore 153 [s153]\n" +
			"     609  ldc <String \"\"> [15]\n" +
			"     611  astore 154 [s154]\n" +
			"     613  ldc <String \"\"> [15]\n" +
			"     615  astore 155 [s155]\n" +
			"     617  ldc <String \"\"> [15]\n" +
			"     619  astore 156 [s156]\n" +
			"     621  ldc <String \"\"> [15]\n" +
			"     623  astore 157 [s157]\n" +
			"     625  ldc <String \"\"> [15]\n" +
			"     627  astore 158 [s158]\n" +
			"     629  ldc <String \"\"> [15]\n" +
			"     631  astore 159 [s159]\n" +
			"     633  ldc <String \"\"> [15]\n" +
			"     635  astore 160 [s160]\n" +
			"     637  ldc <String \"\"> [15]\n" +
			"     639  astore 161 [s161]\n" +
			"     641  ldc <String \"\"> [15]\n" +
			"     643  astore 162 [s162]\n" +
			"     645  ldc <String \"\"> [15]\n" +
			"     647  astore 163 [s163]\n" +
			"     649  ldc <String \"\"> [15]\n" +
			"     651  astore 164 [s164]\n" +
			"     653  ldc <String \"\"> [15]\n" +
			"     655  astore 165 [s165]\n" +
			"     657  ldc <String \"\"> [15]\n" +
			"     659  astore 166 [s166]\n" +
			"     661  ldc <String \"\"> [15]\n" +
			"     663  astore 167 [s167]\n" +
			"     665  ldc <String \"\"> [15]\n" +
			"     667  astore 168 [s168]\n" +
			"     669  ldc <String \"\"> [15]\n" +
			"     671  astore 169 [s169]\n" +
			"     673  ldc <String \"\"> [15]\n" +
			"     675  astore 170 [s170]\n" +
			"     677  ldc <String \"\"> [15]\n" +
			"     679  astore 171 [s171]\n" +
			"     681  ldc <String \"\"> [15]\n" +
			"     683  astore 172 [s172]\n" +
			"     685  ldc <String \"\"> [15]\n" +
			"     687  astore 173 [s173]\n" +
			"     689  ldc <String \"\"> [15]\n" +
			"     691  astore 174 [s174]\n" +
			"     693  ldc <String \"\"> [15]\n" +
			"     695  astore 175 [s175]\n" +
			"     697  ldc <String \"\"> [15]\n" +
			"     699  astore 176 [s176]\n" +
			"     701  ldc <String \"\"> [15]\n" +
			"     703  astore 177 [s177]\n" +
			"     705  ldc <String \"\"> [15]\n" +
			"     707  astore 178 [s178]\n" +
			"     709  ldc <String \"\"> [15]\n" +
			"     711  astore 179 [s179]\n" +
			"     713  ldc <String \"\"> [15]\n" +
			"     715  astore 180 [s180]\n" +
			"     717  ldc <String \"\"> [15]\n" +
			"     719  astore 181 [s181]\n" +
			"     721  ldc <String \"\"> [15]\n" +
			"     723  astore 182 [s182]\n" +
			"     725  ldc <String \"\"> [15]\n" +
			"     727  astore 183 [s183]\n" +
			"     729  ldc <String \"\"> [15]\n" +
			"     731  astore 184 [s184]\n" +
			"     733  ldc <String \"\"> [15]\n" +
			"     735  astore 185 [s185]\n" +
			"     737  ldc <String \"\"> [15]\n" +
			"     739  astore 186 [s186]\n" +
			"     741  ldc <String \"\"> [15]\n" +
			"     743  astore 187 [s187]\n" +
			"     745  ldc <String \"\"> [15]\n" +
			"     747  astore 188 [s188]\n" +
			"     749  ldc <String \"\"> [15]\n" +
			"     751  astore 189 [s189]\n" +
			"     753  ldc <String \"\"> [15]\n" +
			"     755  astore 190 [s190]\n" +
			"     757  ldc <String \"\"> [15]\n" +
			"     759  astore 191 [s191]\n" +
			"     761  ldc <String \"\"> [15]\n" +
			"     763  astore 192 [s192]\n" +
			"     765  ldc <String \"\"> [15]\n" +
			"     767  astore 193 [s193]\n" +
			"     769  ldc <String \"\"> [15]\n" +
			"     771  astore 194 [s194]\n" +
			"     773  ldc <String \"\"> [15]\n" +
			"     775  astore 195 [s195]\n" +
			"     777  ldc <String \"\"> [15]\n" +
			"     779  astore 196 [s196]\n" +
			"     781  ldc <String \"\"> [15]\n" +
			"     783  astore 197 [s197]\n" +
			"     785  ldc <String \"\"> [15]\n" +
			"     787  astore 198 [s198]\n" +
			"     789  ldc <String \"\"> [15]\n" +
			"     791  astore 199 [s199]\n" +
			"     793  ldc <String \"\"> [15]\n" +
			"     795  astore 200 [s200]\n" +
			"     797  ldc <String \"\"> [15]\n" +
			"     799  astore 201 [s201]\n" +
			"     801  ldc <String \"\"> [15]\n" +
			"     803  astore 202 [s202]\n" +
			"     805  ldc <String \"\"> [15]\n" +
			"     807  astore 203 [s203]\n" +
			"     809  ldc <String \"\"> [15]\n" +
			"     811  astore 204 [s204]\n" +
			"     813  ldc <String \"\"> [15]\n" +
			"     815  astore 205 [s205]\n" +
			"     817  ldc <String \"\"> [15]\n" +
			"     819  astore 206 [s206]\n" +
			"     821  ldc <String \"\"> [15]\n" +
			"     823  astore 207 [s207]\n" +
			"     825  ldc <String \"\"> [15]\n" +
			"     827  astore 208 [s208]\n" +
			"     829  ldc <String \"\"> [15]\n" +
			"     831  astore 209 [s209]\n" +
			"     833  ldc <String \"\"> [15]\n" +
			"     835  astore 210 [s210]\n" +
			"     837  ldc <String \"\"> [15]\n" +
			"     839  astore 211 [s211]\n" +
			"     841  ldc <String \"\"> [15]\n" +
			"     843  astore 212 [s212]\n" +
			"     845  ldc <String \"\"> [15]\n" +
			"     847  astore 213 [s213]\n" +
			"     849  ldc <String \"\"> [15]\n" +
			"     851  astore 214 [s214]\n" +
			"     853  ldc <String \"\"> [15]\n" +
			"     855  astore 215 [s215]\n" +
			"     857  ldc <String \"\"> [15]\n" +
			"     859  astore 216 [s216]\n" +
			"     861  ldc <String \"\"> [15]\n" +
			"     863  astore 217 [s217]\n" +
			"     865  ldc <String \"\"> [15]\n" +
			"     867  astore 218 [s218]\n" +
			"     869  ldc <String \"\"> [15]\n" +
			"     871  astore 219 [s219]\n" +
			"     873  ldc <String \"\"> [15]\n" +
			"     875  astore 220 [s220]\n" +
			"     877  ldc <String \"\"> [15]\n" +
			"     879  astore 221 [s221]\n" +
			"     881  ldc <String \"\"> [15]\n" +
			"     883  astore 222 [s222]\n" +
			"     885  ldc <String \"\"> [15]\n" +
			"     887  astore 223 [s223]\n" +
			"     889  ldc <String \"\"> [15]\n" +
			"     891  astore 224 [s224]\n" +
			"     893  ldc <String \"\"> [15]\n" +
			"     895  astore 225 [s225]\n" +
			"     897  ldc <String \"\"> [15]\n" +
			"     899  astore 226 [s226]\n" +
			"     901  ldc <String \"\"> [15]\n" +
			"     903  astore 227 [s227]\n" +
			"     905  ldc <String \"\"> [15]\n" +
			"     907  astore 228 [s228]\n" +
			"     909  ldc <String \"\"> [15]\n" +
			"     911  astore 229 [s229]\n" +
			"     913  ldc <String \"\"> [15]\n" +
			"     915  astore 230 [s230]\n" +
			"     917  ldc <String \"\"> [15]\n" +
			"     919  astore 231 [s231]\n" +
			"     921  ldc <String \"\"> [15]\n" +
			"     923  astore 232 [s232]\n" +
			"     925  ldc <String \"\"> [15]\n" +
			"     927  astore 233 [s233]\n" +
			"     929  ldc <String \"\"> [15]\n" +
			"     931  astore 234 [s234]\n" +
			"     933  ldc <String \"\"> [15]\n" +
			"     935  astore 235 [s235]\n" +
			"     937  ldc <String \"\"> [15]\n" +
			"     939  astore 236 [s236]\n" +
			"     941  ldc <String \"\"> [15]\n" +
			"     943  astore 237 [s237]\n" +
			"     945  ldc <String \"\"> [15]\n" +
			"     947  astore 238 [s238]\n" +
			"     949  ldc <String \"\"> [15]\n" +
			"     951  astore 239 [s239]\n" +
			"     953  ldc <String \"\"> [15]\n" +
			"     955  astore 240 [s240]\n" +
			"     957  ldc <String \"\"> [15]\n" +
			"     959  astore 241 [s241]\n" +
			"     961  ldc <String \"\"> [15]\n" +
			"     963  astore 242 [s242]\n" +
			"     965  ldc <String \"\"> [15]\n" +
			"     967  astore 243 [s243]\n" +
			"     969  ldc <String \"\"> [15]\n" +
			"     971  astore 244 [s244]\n" +
			"     973  ldc <String \"\"> [15]\n" +
			"     975  astore 245 [s245]\n" +
			"     977  ldc <String \"\"> [15]\n" +
			"     979  astore 246 [s246]\n" +
			"     981  ldc <String \"\"> [15]\n" +
			"     983  astore 247 [s247]\n" +
			"     985  ldc <String \"\"> [15]\n" +
			"     987  astore 248 [s248]\n" +
			"     989  ldc <String \"\"> [15]\n" +
			"     991  astore 249 [s249]\n" +
			"     993  ldc <String \"\"> [15]\n" +
			"     995  astore 250 [s250]\n" +
			"     997  ldc <String \"\"> [15]\n" +
			"     999  astore 251 [s251]\n" +
			"    1001  ldc <String \"\"> [15]\n" +
			"    1003  astore 252 [s252]\n" +
			"    1005  iconst_1\n" +
			"    1006  istore 253 [size1]\n" +
			"    1008  iconst_2\n" +
			"    1009  istore 254 [size2]\n" +
			"    1011  iconst_3\n" +
			"    1012  istore 255 [size3]\n" +
			"    1014  iload 253 [size1]\n" +
			"    1016  iload 254 [size2]\n" +
			"    1018  iload 255 [size3]\n" +
			"    1020  multianewarray int[][][] [17]\n" +
			"    1024  wide\n" +
			"    1025  astore 256 [intArray]\n" +
			"    1028  iconst_0\n" +
			"    1029  wide\n" +
			"    1030  istore 257 [i]\n" +
			"    1033  goto 1124\n" +
			"    1036  iconst_0\n" +
			"    1037  wide\n" +
			"    1038  istore 258 [j]\n" +
			"    1041  goto 1109\n" +
			"    1044  iconst_0\n" +
			"    1045  wide\n" +
			"    1046  istore 259 [on]\n" +
			"    1049  iconst_0\n" +
			"    1050  wide\n" +
			"    1051  istore 260 [k]\n" +
			"    1054  goto 1094\n" +
			"    1057  wide\n" +
			"    1058  aload 256 [intArray]\n" +
			"    1061  wide\n" +
			"    1062  iload 257 [i]\n" +
			"    1065  aaload\n" +
			"    1066  wide\n" +
			"    1067  iload 258 [j]\n" +
			"    1070  aaload\n" +
			"    1071  wide\n" +
			"    1072  iload 260 [k]\n" +
			"    1075  wide\n" +
			"    1076  iload 259 [on]\n" +
			"    1079  ifeq 1086\n" +
			"    1082  iconst_0\n" +
			"    1083  goto 1087\n" +
			"    1086  iconst_1\n" +
			"    1087  iastore\n" +
			"    1088  wide\n" +
			"    1089  iinc 260 1 [k]\n" +
			"    1094  wide\n" +
			"    1095  iload 260 [k]\n" +
			"    1098  iload 255 [size3]\n" +
			"    1100  if_icmplt 1057\n" +
			"    1103  wide\n" +
			"    1104  iinc 258 1 [j]\n" +
			"    1109  wide\n" +
			"    1110  iload 258 [j]\n" +
			"    1113  iload 254 [size2]\n" +
			"    1115  if_icmplt 1044\n" +
			"    1118  wide\n" +
			"    1119  iinc 257 1 [i]\n" +
			"    1124  wide\n" +
			"    1125  iload 257 [i]\n" +
			"    1128  iload 253 [size1]\n" +
			"    1130  if_icmplt 1036\n" +
			"    1133  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"        [pc: 37, line: 5]\n" +
			"        [pc: 77, line: 6]\n" +
			"        [pc: 117, line: 7]\n" +
			"        [pc: 157, line: 8]\n" +
			"        [pc: 197, line: 9]\n" +
			"        [pc: 237, line: 10]\n" +
			"        [pc: 277, line: 11]\n" +
			"        [pc: 317, line: 12]\n" +
			"        [pc: 357, line: 13]\n" +
			"        [pc: 397, line: 14]\n" +
			"        [pc: 437, line: 15]\n" +
			"        [pc: 477, line: 16]\n" +
			"        [pc: 517, line: 17]\n" +
			"        [pc: 557, line: 18]\n" +
			"        [pc: 597, line: 19]\n" +
			"        [pc: 637, line: 20]\n" +
			"        [pc: 677, line: 21]\n" +
			"        [pc: 717, line: 22]\n" +
			"        [pc: 757, line: 23]\n" +
			"        [pc: 797, line: 24]\n" +
			"        [pc: 837, line: 25]\n" +
			"        [pc: 877, line: 26]\n" +
			"        [pc: 917, line: 27]\n" +
			"        [pc: 957, line: 28]\n" +
			"        [pc: 997, line: 29]\n" +
			"        [pc: 1005, line: 31]\n" +
			"        [pc: 1008, line: 32]\n" +
			"        [pc: 1011, line: 33]\n" +
			"        [pc: 1014, line: 35]\n" +
			"        [pc: 1028, line: 37]\n" +
			"        [pc: 1036, line: 38]\n" +
			"        [pc: 1044, line: 39]\n" +
			"        [pc: 1049, line: 40]\n" +
			"        [pc: 1057, line: 41]\n" +
			"        [pc: 1088, line: 40]\n" +
			"        [pc: 1103, line: 38]\n" +
			"        [pc: 1118, line: 37]\n" +
			"        [pc: 1133, line: 46]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 1134] local: this index: 0 type: X\n" +
			"        [pc: 3, pc: 1134] local: s1 index: 1 type: java.lang.String\n" +
			"        [pc: 6, pc: 1134] local: s2 index: 2 type: java.lang.String\n" +
			"        [pc: 9, pc: 1134] local: s3 index: 3 type: java.lang.String\n" +
			"        [pc: 13, pc: 1134] local: s4 index: 4 type: java.lang.String\n" +
			"        [pc: 17, pc: 1134] local: s5 index: 5 type: java.lang.String\n" +
			"        [pc: 21, pc: 1134] local: s6 index: 6 type: java.lang.String\n" +
			"        [pc: 25, pc: 1134] local: s7 index: 7 type: java.lang.String\n" +
			"        [pc: 29, pc: 1134] local: s8 index: 8 type: java.lang.String\n" +
			"        [pc: 33, pc: 1134] local: s9 index: 9 type: java.lang.String\n" +
			"        [pc: 37, pc: 1134] local: s10 index: 10 type: java.lang.String\n" +
			"        [pc: 41, pc: 1134] local: s11 index: 11 type: java.lang.String\n" +
			"        [pc: 45, pc: 1134] local: s12 index: 12 type: java.lang.String\n" +
			"        [pc: 49, pc: 1134] local: s13 index: 13 type: java.lang.String\n" +
			"        [pc: 53, pc: 1134] local: s14 index: 14 type: java.lang.String\n" +
			"        [pc: 57, pc: 1134] local: s15 index: 15 type: java.lang.String\n" +
			"        [pc: 61, pc: 1134] local: s16 index: 16 type: java.lang.String\n" +
			"        [pc: 65, pc: 1134] local: s17 index: 17 type: java.lang.String\n" +
			"        [pc: 69, pc: 1134] local: s18 index: 18 type: java.lang.String\n" +
			"        [pc: 73, pc: 1134] local: s19 index: 19 type: java.lang.String\n" +
			"        [pc: 77, pc: 1134] local: s20 index: 20 type: java.lang.String\n" +
			"        [pc: 81, pc: 1134] local: s21 index: 21 type: java.lang.String\n" +
			"        [pc: 85, pc: 1134] local: s22 index: 22 type: java.lang.String\n" +
			"        [pc: 89, pc: 1134] local: s23 index: 23 type: java.lang.String\n" +
			"        [pc: 93, pc: 1134] local: s24 index: 24 type: java.lang.String\n" +
			"        [pc: 97, pc: 1134] local: s25 index: 25 type: java.lang.String\n" +
			"        [pc: 101, pc: 1134] local: s26 index: 26 type: java.lang.String\n" +
			"        [pc: 105, pc: 1134] local: s27 index: 27 type: java.lang.String\n" +
			"        [pc: 109, pc: 1134] local: s28 index: 28 type: java.lang.String\n" +
			"        [pc: 113, pc: 1134] local: s29 index: 29 type: java.lang.String\n" +
			"        [pc: 117, pc: 1134] local: s30 index: 30 type: java.lang.String\n" +
			"        [pc: 121, pc: 1134] local: s31 index: 31 type: java.lang.String\n" +
			"        [pc: 125, pc: 1134] local: s32 index: 32 type: java.lang.String\n" +
			"        [pc: 129, pc: 1134] local: s33 index: 33 type: java.lang.String\n" +
			"        [pc: 133, pc: 1134] local: s34 index: 34 type: java.lang.String\n" +
			"        [pc: 137, pc: 1134] local: s35 index: 35 type: java.lang.String\n" +
			"        [pc: 141, pc: 1134] local: s36 index: 36 type: java.lang.String\n" +
			"        [pc: 145, pc: 1134] local: s37 index: 37 type: java.lang.String\n" +
			"        [pc: 149, pc: 1134] local: s38 index: 38 type: java.lang.String\n" +
			"        [pc: 153, pc: 1134] local: s39 index: 39 type: java.lang.String\n" +
			"        [pc: 157, pc: 1134] local: s40 index: 40 type: java.lang.String\n" +
			"        [pc: 161, pc: 1134] local: s41 index: 41 type: java.lang.String\n" +
			"        [pc: 165, pc: 1134] local: s42 index: 42 type: java.lang.String\n" +
			"        [pc: 169, pc: 1134] local: s43 index: 43 type: java.lang.String\n" +
			"        [pc: 173, pc: 1134] local: s44 index: 44 type: java.lang.String\n" +
			"        [pc: 177, pc: 1134] local: s45 index: 45 type: java.lang.String\n" +
			"        [pc: 181, pc: 1134] local: s46 index: 46 type: java.lang.String\n" +
			"        [pc: 185, pc: 1134] local: s47 index: 47 type: java.lang.String\n" +
			"        [pc: 189, pc: 1134] local: s48 index: 48 type: java.lang.String\n" +
			"        [pc: 193, pc: 1134] local: s49 index: 49 type: java.lang.String\n" +
			"        [pc: 197, pc: 1134] local: s50 index: 50 type: java.lang.String\n" +
			"        [pc: 201, pc: 1134] local: s51 index: 51 type: java.lang.String\n" +
			"        [pc: 205, pc: 1134] local: s52 index: 52 type: java.lang.String\n" +
			"        [pc: 209, pc: 1134] local: s53 index: 53 type: java.lang.String\n" +
			"        [pc: 213, pc: 1134] local: s54 index: 54 type: java.lang.String\n" +
			"        [pc: 217, pc: 1134] local: s55 index: 55 type: java.lang.String\n" +
			"        [pc: 221, pc: 1134] local: s56 index: 56 type: java.lang.String\n" +
			"        [pc: 225, pc: 1134] local: s57 index: 57 type: java.lang.String\n" +
			"        [pc: 229, pc: 1134] local: s58 index: 58 type: java.lang.String\n" +
			"        [pc: 233, pc: 1134] local: s59 index: 59 type: java.lang.String\n" +
			"        [pc: 237, pc: 1134] local: s60 index: 60 type: java.lang.String\n" +
			"        [pc: 241, pc: 1134] local: s61 index: 61 type: java.lang.String\n" +
			"        [pc: 245, pc: 1134] local: s62 index: 62 type: java.lang.String\n" +
			"        [pc: 249, pc: 1134] local: s63 index: 63 type: java.lang.String\n" +
			"        [pc: 253, pc: 1134] local: s64 index: 64 type: java.lang.String\n" +
			"        [pc: 257, pc: 1134] local: s65 index: 65 type: java.lang.String\n" +
			"        [pc: 261, pc: 1134] local: s66 index: 66 type: java.lang.String\n" +
			"        [pc: 265, pc: 1134] local: s67 index: 67 type: java.lang.String\n" +
			"        [pc: 269, pc: 1134] local: s68 index: 68 type: java.lang.String\n" +
			"        [pc: 273, pc: 1134] local: s69 index: 69 type: java.lang.String\n" +
			"        [pc: 277, pc: 1134] local: s70 index: 70 type: java.lang.String\n" +
			"        [pc: 281, pc: 1134] local: s71 index: 71 type: java.lang.String\n" +
			"        [pc: 285, pc: 1134] local: s72 index: 72 type: java.lang.String\n" +
			"        [pc: 289, pc: 1134] local: s73 index: 73 type: java.lang.String\n" +
			"        [pc: 293, pc: 1134] local: s74 index: 74 type: java.lang.String\n" +
			"        [pc: 297, pc: 1134] local: s75 index: 75 type: java.lang.String\n" +
			"        [pc: 301, pc: 1134] local: s76 index: 76 type: java.lang.String\n" +
			"        [pc: 305, pc: 1134] local: s77 index: 77 type: java.lang.String\n" +
			"        [pc: 309, pc: 1134] local: s78 index: 78 type: java.lang.String\n" +
			"        [pc: 313, pc: 1134] local: s79 index: 79 type: java.lang.String\n" +
			"        [pc: 317, pc: 1134] local: s80 index: 80 type: java.lang.String\n" +
			"        [pc: 321, pc: 1134] local: s81 index: 81 type: java.lang.String\n" +
			"        [pc: 325, pc: 1134] local: s82 index: 82 type: java.lang.String\n" +
			"        [pc: 329, pc: 1134] local: s83 index: 83 type: java.lang.String\n" +
			"        [pc: 333, pc: 1134] local: s84 index: 84 type: java.lang.String\n" +
			"        [pc: 337, pc: 1134] local: s85 index: 85 type: java.lang.String\n" +
			"        [pc: 341, pc: 1134] local: s86 index: 86 type: java.lang.String\n" +
			"        [pc: 345, pc: 1134] local: s87 index: 87 type: java.lang.String\n" +
			"        [pc: 349, pc: 1134] local: s88 index: 88 type: java.lang.String\n" +
			"        [pc: 353, pc: 1134] local: s89 index: 89 type: java.lang.String\n" +
			"        [pc: 357, pc: 1134] local: s90 index: 90 type: java.lang.String\n" +
			"        [pc: 361, pc: 1134] local: s91 index: 91 type: java.lang.String\n" +
			"        [pc: 365, pc: 1134] local: s92 index: 92 type: java.lang.String\n" +
			"        [pc: 369, pc: 1134] local: s93 index: 93 type: java.lang.String\n" +
			"        [pc: 373, pc: 1134] local: s94 index: 94 type: java.lang.String\n" +
			"        [pc: 377, pc: 1134] local: s95 index: 95 type: java.lang.String\n" +
			"        [pc: 381, pc: 1134] local: s96 index: 96 type: java.lang.String\n" +
			"        [pc: 385, pc: 1134] local: s97 index: 97 type: java.lang.String\n" +
			"        [pc: 389, pc: 1134] local: s98 index: 98 type: java.lang.String\n" +
			"        [pc: 393, pc: 1134] local: s99 index: 99 type: java.lang.String\n" +
			"        [pc: 397, pc: 1134] local: s100 index: 100 type: java.lang.String\n" +
			"        [pc: 401, pc: 1134] local: s101 index: 101 type: java.lang.String\n" +
			"        [pc: 405, pc: 1134] local: s102 index: 102 type: java.lang.String\n" +
			"        [pc: 409, pc: 1134] local: s103 index: 103 type: java.lang.String\n" +
			"        [pc: 413, pc: 1134] local: s104 index: 104 type: java.lang.String\n" +
			"        [pc: 417, pc: 1134] local: s105 index: 105 type: java.lang.String\n" +
			"        [pc: 421, pc: 1134] local: s106 index: 106 type: java.lang.String\n" +
			"        [pc: 425, pc: 1134] local: s107 index: 107 type: java.lang.String\n" +
			"        [pc: 429, pc: 1134] local: s108 index: 108 type: java.lang.String\n" +
			"        [pc: 433, pc: 1134] local: s109 index: 109 type: java.lang.String\n" +
			"        [pc: 437, pc: 1134] local: s110 index: 110 type: java.lang.String\n" +
			"        [pc: 441, pc: 1134] local: s111 index: 111 type: java.lang.String\n" +
			"        [pc: 445, pc: 1134] local: s112 index: 112 type: java.lang.String\n" +
			"        [pc: 449, pc: 1134] local: s113 index: 113 type: java.lang.String\n" +
			"        [pc: 453, pc: 1134] local: s114 index: 114 type: java.lang.String\n" +
			"        [pc: 457, pc: 1134] local: s115 index: 115 type: java.lang.String\n" +
			"        [pc: 461, pc: 1134] local: s116 index: 116 type: java.lang.String\n" +
			"        [pc: 465, pc: 1134] local: s117 index: 117 type: java.lang.String\n" +
			"        [pc: 469, pc: 1134] local: s118 index: 118 type: java.lang.String\n" +
			"        [pc: 473, pc: 1134] local: s119 index: 119 type: java.lang.String\n" +
			"        [pc: 477, pc: 1134] local: s120 index: 120 type: java.lang.String\n" +
			"        [pc: 481, pc: 1134] local: s121 index: 121 type: java.lang.String\n" +
			"        [pc: 485, pc: 1134] local: s122 index: 122 type: java.lang.String\n" +
			"        [pc: 489, pc: 1134] local: s123 index: 123 type: java.lang.String\n" +
			"        [pc: 493, pc: 1134] local: s124 index: 124 type: java.lang.String\n" +
			"        [pc: 497, pc: 1134] local: s125 index: 125 type: java.lang.String\n" +
			"        [pc: 501, pc: 1134] local: s126 index: 126 type: java.lang.String\n" +
			"        [pc: 505, pc: 1134] local: s127 index: 127 type: java.lang.String\n" +
			"        [pc: 509, pc: 1134] local: s128 index: 128 type: java.lang.String\n" +
			"        [pc: 513, pc: 1134] local: s129 index: 129 type: java.lang.String\n" +
			"        [pc: 517, pc: 1134] local: s130 index: 130 type: java.lang.String\n" +
			"        [pc: 521, pc: 1134] local: s131 index: 131 type: java.lang.String\n" +
			"        [pc: 525, pc: 1134] local: s132 index: 132 type: java.lang.String\n" +
			"        [pc: 529, pc: 1134] local: s133 index: 133 type: java.lang.String\n" +
			"        [pc: 533, pc: 1134] local: s134 index: 134 type: java.lang.String\n" +
			"        [pc: 537, pc: 1134] local: s135 index: 135 type: java.lang.String\n" +
			"        [pc: 541, pc: 1134] local: s136 index: 136 type: java.lang.String\n" +
			"        [pc: 545, pc: 1134] local: s137 index: 137 type: java.lang.String\n" +
			"        [pc: 549, pc: 1134] local: s138 index: 138 type: java.lang.String\n" +
			"        [pc: 553, pc: 1134] local: s139 index: 139 type: java.lang.String\n" +
			"        [pc: 557, pc: 1134] local: s140 index: 140 type: java.lang.String\n" +
			"        [pc: 561, pc: 1134] local: s141 index: 141 type: java.lang.String\n" +
			"        [pc: 565, pc: 1134] local: s142 index: 142 type: java.lang.String\n" +
			"        [pc: 569, pc: 1134] local: s143 index: 143 type: java.lang.String\n" +
			"        [pc: 573, pc: 1134] local: s144 index: 144 type: java.lang.String\n" +
			"        [pc: 577, pc: 1134] local: s145 index: 145 type: java.lang.String\n" +
			"        [pc: 581, pc: 1134] local: s146 index: 146 type: java.lang.String\n" +
			"        [pc: 585, pc: 1134] local: s147 index: 147 type: java.lang.String\n" +
			"        [pc: 589, pc: 1134] local: s148 index: 148 type: java.lang.String\n" +
			"        [pc: 593, pc: 1134] local: s149 index: 149 type: java.lang.String\n" +
			"        [pc: 597, pc: 1134] local: s150 index: 150 type: java.lang.String\n" +
			"        [pc: 601, pc: 1134] local: s151 index: 151 type: java.lang.String\n" +
			"        [pc: 605, pc: 1134] local: s152 index: 152 type: java.lang.String\n" +
			"        [pc: 609, pc: 1134] local: s153 index: 153 type: java.lang.String\n" +
			"        [pc: 613, pc: 1134] local: s154 index: 154 type: java.lang.String\n" +
			"        [pc: 617, pc: 1134] local: s155 index: 155 type: java.lang.String\n" +
			"        [pc: 621, pc: 1134] local: s156 index: 156 type: java.lang.String\n" +
			"        [pc: 625, pc: 1134] local: s157 index: 157 type: java.lang.String\n" +
			"        [pc: 629, pc: 1134] local: s158 index: 158 type: java.lang.String\n" +
			"        [pc: 633, pc: 1134] local: s159 index: 159 type: java.lang.String\n" +
			"        [pc: 637, pc: 1134] local: s160 index: 160 type: java.lang.String\n" +
			"        [pc: 641, pc: 1134] local: s161 index: 161 type: java.lang.String\n" +
			"        [pc: 645, pc: 1134] local: s162 index: 162 type: java.lang.String\n" +
			"        [pc: 649, pc: 1134] local: s163 index: 163 type: java.lang.String\n" +
			"        [pc: 653, pc: 1134] local: s164 index: 164 type: java.lang.String\n" +
			"        [pc: 657, pc: 1134] local: s165 index: 165 type: java.lang.String\n" +
			"        [pc: 661, pc: 1134] local: s166 index: 166 type: java.lang.String\n" +
			"        [pc: 665, pc: 1134] local: s167 index: 167 type: java.lang.String\n" +
			"        [pc: 669, pc: 1134] local: s168 index: 168 type: java.lang.String\n" +
			"        [pc: 673, pc: 1134] local: s169 index: 169 type: java.lang.String\n" +
			"        [pc: 677, pc: 1134] local: s170 index: 170 type: java.lang.String\n" +
			"        [pc: 681, pc: 1134] local: s171 index: 171 type: java.lang.String\n" +
			"        [pc: 685, pc: 1134] local: s172 index: 172 type: java.lang.String\n" +
			"        [pc: 689, pc: 1134] local: s173 index: 173 type: java.lang.String\n" +
			"        [pc: 693, pc: 1134] local: s174 index: 174 type: java.lang.String\n" +
			"        [pc: 697, pc: 1134] local: s175 index: 175 type: java.lang.String\n" +
			"        [pc: 701, pc: 1134] local: s176 index: 176 type: java.lang.String\n" +
			"        [pc: 705, pc: 1134] local: s177 index: 177 type: java.lang.String\n" +
			"        [pc: 709, pc: 1134] local: s178 index: 178 type: java.lang.String\n" +
			"        [pc: 713, pc: 1134] local: s179 index: 179 type: java.lang.String\n" +
			"        [pc: 717, pc: 1134] local: s180 index: 180 type: java.lang.String\n" +
			"        [pc: 721, pc: 1134] local: s181 index: 181 type: java.lang.String\n" +
			"        [pc: 725, pc: 1134] local: s182 index: 182 type: java.lang.String\n" +
			"        [pc: 729, pc: 1134] local: s183 index: 183 type: java.lang.String\n" +
			"        [pc: 733, pc: 1134] local: s184 index: 184 type: java.lang.String\n" +
			"        [pc: 737, pc: 1134] local: s185 index: 185 type: java.lang.String\n" +
			"        [pc: 741, pc: 1134] local: s186 index: 186 type: java.lang.String\n" +
			"        [pc: 745, pc: 1134] local: s187 index: 187 type: java.lang.String\n" +
			"        [pc: 749, pc: 1134] local: s188 index: 188 type: java.lang.String\n" +
			"        [pc: 753, pc: 1134] local: s189 index: 189 type: java.lang.String\n" +
			"        [pc: 757, pc: 1134] local: s190 index: 190 type: java.lang.String\n" +
			"        [pc: 761, pc: 1134] local: s191 index: 191 type: java.lang.String\n" +
			"        [pc: 765, pc: 1134] local: s192 index: 192 type: java.lang.String\n" +
			"        [pc: 769, pc: 1134] local: s193 index: 193 type: java.lang.String\n" +
			"        [pc: 773, pc: 1134] local: s194 index: 194 type: java.lang.String\n" +
			"        [pc: 777, pc: 1134] local: s195 index: 195 type: java.lang.String\n" +
			"        [pc: 781, pc: 1134] local: s196 index: 196 type: java.lang.String\n" +
			"        [pc: 785, pc: 1134] local: s197 index: 197 type: java.lang.String\n" +
			"        [pc: 789, pc: 1134] local: s198 index: 198 type: java.lang.String\n" +
			"        [pc: 793, pc: 1134] local: s199 index: 199 type: java.lang.String\n" +
			"        [pc: 797, pc: 1134] local: s200 index: 200 type: java.lang.String\n" +
			"        [pc: 801, pc: 1134] local: s201 index: 201 type: java.lang.String\n" +
			"        [pc: 805, pc: 1134] local: s202 index: 202 type: java.lang.String\n" +
			"        [pc: 809, pc: 1134] local: s203 index: 203 type: java.lang.String\n" +
			"        [pc: 813, pc: 1134] local: s204 index: 204 type: java.lang.String\n" +
			"        [pc: 817, pc: 1134] local: s205 index: 205 type: java.lang.String\n" +
			"        [pc: 821, pc: 1134] local: s206 index: 206 type: java.lang.String\n" +
			"        [pc: 825, pc: 1134] local: s207 index: 207 type: java.lang.String\n" +
			"        [pc: 829, pc: 1134] local: s208 index: 208 type: java.lang.String\n" +
			"        [pc: 833, pc: 1134] local: s209 index: 209 type: java.lang.String\n" +
			"        [pc: 837, pc: 1134] local: s210 index: 210 type: java.lang.String\n" +
			"        [pc: 841, pc: 1134] local: s211 index: 211 type: java.lang.String\n" +
			"        [pc: 845, pc: 1134] local: s212 index: 212 type: java.lang.String\n" +
			"        [pc: 849, pc: 1134] local: s213 index: 213 type: java.lang.String\n" +
			"        [pc: 853, pc: 1134] local: s214 index: 214 type: java.lang.String\n" +
			"        [pc: 857, pc: 1134] local: s215 index: 215 type: java.lang.String\n" +
			"        [pc: 861, pc: 1134] local: s216 index: 216 type: java.lang.String\n" +
			"        [pc: 865, pc: 1134] local: s217 index: 217 type: java.lang.String\n" +
			"        [pc: 869, pc: 1134] local: s218 index: 218 type: java.lang.String\n" +
			"        [pc: 873, pc: 1134] local: s219 index: 219 type: java.lang.String\n" +
			"        [pc: 877, pc: 1134] local: s220 index: 220 type: java.lang.String\n" +
			"        [pc: 881, pc: 1134] local: s221 index: 221 type: java.lang.String\n" +
			"        [pc: 885, pc: 1134] local: s222 index: 222 type: java.lang.String\n" +
			"        [pc: 889, pc: 1134] local: s223 index: 223 type: java.lang.String\n" +
			"        [pc: 893, pc: 1134] local: s224 index: 224 type: java.lang.String\n" +
			"        [pc: 897, pc: 1134] local: s225 index: 225 type: java.lang.String\n" +
			"        [pc: 901, pc: 1134] local: s226 index: 226 type: java.lang.String\n" +
			"        [pc: 905, pc: 1134] local: s227 index: 227 type: java.lang.String\n" +
			"        [pc: 909, pc: 1134] local: s228 index: 228 type: java.lang.String\n" +
			"        [pc: 913, pc: 1134] local: s229 index: 229 type: java.lang.String\n" +
			"        [pc: 917, pc: 1134] local: s230 index: 230 type: java.lang.String\n" +
			"        [pc: 921, pc: 1134] local: s231 index: 231 type: java.lang.String\n" +
			"        [pc: 925, pc: 1134] local: s232 index: 232 type: java.lang.String\n" +
			"        [pc: 929, pc: 1134] local: s233 index: 233 type: java.lang.String\n" +
			"        [pc: 933, pc: 1134] local: s234 index: 234 type: java.lang.String\n" +
			"        [pc: 937, pc: 1134] local: s235 index: 235 type: java.lang.String\n" +
			"        [pc: 941, pc: 1134] local: s236 index: 236 type: java.lang.String\n" +
			"        [pc: 945, pc: 1134] local: s237 index: 237 type: java.lang.String\n" +
			"        [pc: 949, pc: 1134] local: s238 index: 238 type: java.lang.String\n" +
			"        [pc: 953, pc: 1134] local: s239 index: 239 type: java.lang.String\n" +
			"        [pc: 957, pc: 1134] local: s240 index: 240 type: java.lang.String\n" +
			"        [pc: 961, pc: 1134] local: s241 index: 241 type: java.lang.String\n" +
			"        [pc: 965, pc: 1134] local: s242 index: 242 type: java.lang.String\n" +
			"        [pc: 969, pc: 1134] local: s243 index: 243 type: java.lang.String\n" +
			"        [pc: 973, pc: 1134] local: s244 index: 244 type: java.lang.String\n" +
			"        [pc: 977, pc: 1134] local: s245 index: 245 type: java.lang.String\n" +
			"        [pc: 981, pc: 1134] local: s246 index: 246 type: java.lang.String\n" +
			"        [pc: 985, pc: 1134] local: s247 index: 247 type: java.lang.String\n" +
			"        [pc: 989, pc: 1134] local: s248 index: 248 type: java.lang.String\n" +
			"        [pc: 993, pc: 1134] local: s249 index: 249 type: java.lang.String\n" +
			"        [pc: 997, pc: 1134] local: s250 index: 250 type: java.lang.String\n" +
			"        [pc: 1001, pc: 1134] local: s251 index: 251 type: java.lang.String\n" +
			"        [pc: 1005, pc: 1134] local: s252 index: 252 type: java.lang.String\n" +
			"        [pc: 1008, pc: 1134] local: size1 index: 253 type: int\n" +
			"        [pc: 1011, pc: 1134] local: size2 index: 254 type: int\n" +
			"        [pc: 1014, pc: 1134] local: size3 index: 255 type: int\n" +
			"        [pc: 1028, pc: 1134] local: intArray index: 256 type: int[][][]\n" +
			"        [pc: 1033, pc: 1133] local: i index: 257 type: int\n" +
			"        [pc: 1041, pc: 1118] local: j index: 258 type: int\n" +
			"        [pc: 1049, pc: 1103] local: on index: 259 type: boolean\n" +
			"        [pc: 1054, pc: 1103] local: k index: 260 type: int\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test007() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public final class X {\n" +
			"\n" +
			"  public void show() {\n" +
			"    String s1 = \"\";   String s2 = \"\";   String s3 = \"\";   String s4 = \"\";   String s5 = \"\";   String s6 = \"\";   String s7 = \"\";   String s8 = \"\";   String s9 = \"\";   String s10 = \"\";\n" +
			"    String s11 = \"\";  String s12 = \"\";  String s13 = \"\";  String s14 = \"\";  String s15 = \"\";  String s16 = \"\";  String s17 = \"\";  String s18 = \"\";  String s19 = \"\";  String s20 = \"\";\n" +
			"    String s21 = \"\";  String s22 = \"\";  String s23 = \"\";  String s24 = \"\";  String s25 = \"\";  String s26 = \"\";  String s27 = \"\";  String s28 = \"\";  String s29 = \"\";  String s30 = \"\";\n" +
			"    String s31 = \"\";  String s32 = \"\";  String s33 = \"\";  String s34 = \"\";  String s35 = \"\";  String s36 = \"\";  String s37 = \"\";  String s38 = \"\";  String s39 = \"\";  String s40 = \"\";\n" +
			"    String s41 = \"\";  String s42 = \"\";  String s43 = \"\";  String s44 = \"\";  String s45 = \"\";  String s46 = \"\";  String s47 = \"\";  String s48 = \"\";  String s49 = \"\";  String s50 = \"\";\n" +
			"    String s51 = \"\";  String s52 = \"\";  String s53 = \"\";  String s54 = \"\";  String s55 = \"\";  String s56 = \"\";  String s57 = \"\";  String s58 = \"\";  String s59 = \"\";  String s60 = \"\";\n" +
			"    String s61 = \"\";  String s62 = \"\";  String s63 = \"\";  String s64 = \"\";  String s65 = \"\";  String s66 = \"\";  String s67 = \"\";  String s68 = \"\";  String s69 = \"\";  String s70 = \"\";\n" +
			"    String s71 = \"\";  String s72 = \"\";  String s73 = \"\";  String s74 = \"\";  String s75 = \"\";  String s76 = \"\";  String s77 = \"\";  String s78 = \"\";  String s79 = \"\";  String s80 = \"\";\n" +
			"    String s81 = \"\";  String s82 = \"\";  String s83 = \"\";  String s84 = \"\";  String s85 = \"\";  String s86 = \"\";  String s87 = \"\";  String s88 = \"\";  String s89 = \"\";  String s90 = \"\";\n" +
			"    String s91 = \"\";  String s92 = \"\";  String s93 = \"\";  String s94 = \"\";  String s95 = \"\";  String s96 = \"\";  String s97 = \"\";  String s98 = \"\";  String s99 = \"\";  String s100 = \"\";\n" +
			"    String s101 = \"\"; String s102 = \"\"; String s103 = \"\"; String s104 = \"\"; String s105 = \"\"; String s106 = \"\"; String s107 = \"\"; String s108 = \"\"; String s109 = \"\"; String s110 = \"\";\n" +
			"    String s111 = \"\"; String s112 = \"\"; String s113 = \"\"; String s114 = \"\"; String s115 = \"\"; String s116 = \"\"; String s117 = \"\"; String s118 = \"\"; String s119 = \"\"; String s120 = \"\";\n" +
			"    String s121 = \"\"; String s122 = \"\"; String s123 = \"\"; String s124 = \"\"; String s125 = \"\"; String s126 = \"\"; String s127 = \"\"; String s128 = \"\"; String s129 = \"\"; String s130 = \"\";\n" +
			"    String s131 = \"\"; String s132 = \"\"; String s133 = \"\"; String s134 = \"\"; String s135 = \"\"; String s136 = \"\"; String s137 = \"\"; String s138 = \"\"; String s139 = \"\"; String s140 = \"\";\n" +
			"    String s141 = \"\"; String s142 = \"\"; String s143 = \"\"; String s144 = \"\"; String s145 = \"\"; String s146 = \"\"; String s147 = \"\"; String s148 = \"\"; String s149 = \"\"; String s150 = \"\";\n" +
			"    String s151 = \"\"; String s152 = \"\"; String s153 = \"\"; String s154 = \"\"; String s155 = \"\"; String s156 = \"\"; String s157 = \"\"; String s158 = \"\"; String s159 = \"\"; String s160 = \"\";\n" +
			"    String s161 = \"\"; String s162 = \"\"; String s163 = \"\"; String s164 = \"\"; String s165 = \"\"; String s166 = \"\"; String s167 = \"\"; String s168 = \"\"; String s169 = \"\"; String s170 = \"\";\n" +
			"    String s171 = \"\"; String s172 = \"\"; String s173 = \"\"; String s174 = \"\"; String s175 = \"\"; String s176 = \"\"; String s177 = \"\"; String s178 = \"\"; String s179 = \"\"; String s180 = \"\";\n" +
			"    String s181 = \"\"; String s182 = \"\"; String s183 = \"\"; String s184 = \"\"; String s185 = \"\"; String s186 = \"\"; String s187 = \"\"; String s188 = \"\"; String s189 = \"\"; String s190 = \"\";\n" +
			"    String s191 = \"\"; String s192 = \"\"; String s193 = \"\"; String s194 = \"\"; String s195 = \"\"; String s196 = \"\"; String s197 = \"\"; String s198 = \"\"; String s199 = \"\"; String s200 = \"\";\n" +
			"    String s201 = \"\"; String s202 = \"\"; String s203 = \"\"; String s204 = \"\"; String s205 = \"\"; String s206 = \"\"; String s207 = \"\"; String s208 = \"\"; String s209 = \"\"; String s210 = \"\";\n" +
			"    String s211 = \"\"; String s212 = \"\"; String s213 = \"\"; String s214 = \"\"; String s215 = \"\"; String s216 = \"\"; String s217 = \"\"; String s218 = \"\"; String s219 = \"\"; String s220 = \"\";\n" +
			"    String s221 = \"\"; String s222 = \"\"; String s223 = \"\"; String s224 = \"\"; String s225 = \"\"; String s226 = \"\"; String s227 = \"\"; String s228 = \"\"; String s229 = \"\"; String s230 = \"\";\n" +
			"    String s231 = \"\"; String s232 = \"\"; String s233 = \"\"; String s234 = \"\"; String s235 = \"\"; String s236 = \"\"; String s237 = \"\"; String s238 = \"\"; String s239 = \"\"; String s240 = \"\";\n" +
			"    String s241 = \"\"; String s242 = \"\"; String s243 = \"\"; String s244 = \"\"; String s245 = \"\"; String s246 = \"\"; String s247 = \"\"; String s248 = \"\"; String s249 = \"\"; String s250 = \"\";\n" +
			"    String s251 = \"\"; String s252 = \"\";\n" +
			"\n" +
			"    int size1 = 1;\n" +
			"    int size2 = 2;\n" +
			"    int size3 = 3;\n" +
			"\n" +
			"    int[][][] intArray = new int[size1][size2][];\n" +
			"    \n" +
			"    for (int i = 0; i < size1; i++) {\n" +
			"      for (int j = 0; j < size2; j++) {\n" +
			"        boolean on = false;\n" +
			"        intArray[i][j] = new int[size3];\n" +
			"        for (int k = 0; k < size3; k++) {\n" +
			"          intArray[i][j][k] = on ? 0 : 1;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) {\n" +
			"    new X().show();\n" +
			"  }\n" +
			"}",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test008() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public final class X {\n" +
			"\n" +
			"  public void show() {\n" +
			"    String s1 = \"\";   String s2 = \"\";   String s3 = \"\";   String s4 = \"\";   String s5 = \"\";   String s6 = \"\";   String s7 = \"\";   String s8 = \"\";   String s9 = \"\";   String s10 = \"\";\n" +
			"    String s11 = \"\";  String s12 = \"\";  String s13 = \"\";  String s14 = \"\";  String s15 = \"\";  String s16 = \"\";  String s17 = \"\";  String s18 = \"\";  String s19 = \"\";  String s20 = \"\";\n" +
			"    String s21 = \"\";  String s22 = \"\";  String s23 = \"\";  String s24 = \"\";  String s25 = \"\";  String s26 = \"\";  String s27 = \"\";  String s28 = \"\";  String s29 = \"\";  String s30 = \"\";\n" +
			"    String s31 = \"\";  String s32 = \"\";  String s33 = \"\";  String s34 = \"\";  String s35 = \"\";  String s36 = \"\";  String s37 = \"\";  String s38 = \"\";  String s39 = \"\";  String s40 = \"\";\n" +
			"    String s41 = \"\";  String s42 = \"\";  String s43 = \"\";  String s44 = \"\";  String s45 = \"\";  String s46 = \"\";  String s47 = \"\";  String s48 = \"\";  String s49 = \"\";  String s50 = \"\";\n" +
			"    String s51 = \"\";  String s52 = \"\";  String s53 = \"\";  String s54 = \"\";  String s55 = \"\";  String s56 = \"\";  String s57 = \"\";  String s58 = \"\";  String s59 = \"\";  String s60 = \"\";\n" +
			"    String s61 = \"\";  String s62 = \"\";  String s63 = \"\";  String s64 = \"\";  String s65 = \"\";  String s66 = \"\";  String s67 = \"\";  String s68 = \"\";  String s69 = \"\";  String s70 = \"\";\n" +
			"    String s71 = \"\";  String s72 = \"\";  String s73 = \"\";  String s74 = \"\";  String s75 = \"\";  String s76 = \"\";  String s77 = \"\";  String s78 = \"\";  String s79 = \"\";  String s80 = \"\";\n" +
			"    String s81 = \"\";  String s82 = \"\";  String s83 = \"\";  String s84 = \"\";  String s85 = \"\";  String s86 = \"\";  String s87 = \"\";  String s88 = \"\";  String s89 = \"\";  String s90 = \"\";\n" +
			"    String s91 = \"\";  String s92 = \"\";  String s93 = \"\";  String s94 = \"\";  String s95 = \"\";  String s96 = \"\";  String s97 = \"\";  String s98 = \"\";  String s99 = \"\";  String s100 = \"\";\n" +
			"    String s101 = \"\"; String s102 = \"\"; String s103 = \"\"; String s104 = \"\"; String s105 = \"\"; String s106 = \"\"; String s107 = \"\"; String s108 = \"\"; String s109 = \"\"; String s110 = \"\";\n" +
			"    String s111 = \"\"; String s112 = \"\"; String s113 = \"\"; String s114 = \"\"; String s115 = \"\"; String s116 = \"\"; String s117 = \"\"; String s118 = \"\"; String s119 = \"\"; String s120 = \"\";\n" +
			"    String s121 = \"\"; String s122 = \"\"; String s123 = \"\"; String s124 = \"\"; String s125 = \"\"; String s126 = \"\"; String s127 = \"\"; String s128 = \"\"; String s129 = \"\"; String s130 = \"\";\n" +
			"    String s131 = \"\"; String s132 = \"\"; String s133 = \"\"; String s134 = \"\"; String s135 = \"\"; String s136 = \"\"; String s137 = \"\"; String s138 = \"\"; String s139 = \"\"; String s140 = \"\";\n" +
			"    String s141 = \"\"; String s142 = \"\"; String s143 = \"\"; String s144 = \"\"; String s145 = \"\"; String s146 = \"\"; String s147 = \"\"; String s148 = \"\"; String s149 = \"\"; String s150 = \"\";\n" +
			"    String s151 = \"\"; String s152 = \"\"; String s153 = \"\"; String s154 = \"\"; String s155 = \"\"; String s156 = \"\"; String s157 = \"\"; String s158 = \"\"; String s159 = \"\"; String s160 = \"\";\n" +
			"    String s161 = \"\"; String s162 = \"\"; String s163 = \"\"; String s164 = \"\"; String s165 = \"\"; String s166 = \"\"; String s167 = \"\"; String s168 = \"\"; String s169 = \"\"; String s170 = \"\";\n" +
			"    String s171 = \"\"; String s172 = \"\"; String s173 = \"\"; String s174 = \"\"; String s175 = \"\"; String s176 = \"\"; String s177 = \"\"; String s178 = \"\"; String s179 = \"\"; String s180 = \"\";\n" +
			"    String s181 = \"\"; String s182 = \"\"; String s183 = \"\"; String s184 = \"\"; String s185 = \"\"; String s186 = \"\"; String s187 = \"\"; String s188 = \"\"; String s189 = \"\"; String s190 = \"\";\n" +
			"    String s191 = \"\"; String s192 = \"\"; String s193 = \"\"; String s194 = \"\"; String s195 = \"\"; String s196 = \"\"; String s197 = \"\"; String s198 = \"\"; String s199 = \"\"; String s200 = \"\";\n" +
			"    String s201 = \"\"; String s202 = \"\"; String s203 = \"\"; String s204 = \"\"; String s205 = \"\"; String s206 = \"\"; String s207 = \"\"; String s208 = \"\"; String s209 = \"\"; String s210 = \"\";\n" +
			"    String s211 = \"\"; String s212 = \"\"; String s213 = \"\"; String s214 = \"\"; String s215 = \"\"; String s216 = \"\"; String s217 = \"\"; String s218 = \"\"; String s219 = \"\"; String s220 = \"\";\n" +
			"    String s221 = \"\"; String s222 = \"\"; String s223 = \"\"; String s224 = \"\"; String s225 = \"\"; String s226 = \"\"; String s227 = \"\"; String s228 = \"\"; String s229 = \"\"; String s230 = \"\";\n" +
			"    String s231 = \"\"; String s232 = \"\"; String s233 = \"\"; String s234 = \"\"; String s235 = \"\"; String s236 = \"\"; String s237 = \"\"; String s238 = \"\"; String s239 = \"\"; String s240 = \"\";\n" +
			"    String s241 = \"\"; String s242 = \"\"; String s243 = \"\"; String s244 = \"\"; String s245 = \"\"; String s246 = \"\"; String s247 = \"\"; String s248 = \"\"; String s249 = \"\"; String s250 = \"\";\n" +
			"    String s251 = \"\"; String s252 = \"\";\n" +
			"\n" +
			"    int size1 = 1;\n" +
			"    int size2 = 2;\n" +
			"    int size3 = 3;\n" +
			"\n" +
			"    String[][][] array = new String[size1][size2][size3];\n" +
			"    \n" +
			"    for (int i = 0; i < size1; i++) {\n" +
			"      for (int j = 0; j < size2; j++) {\n" +
			"        boolean on = false;\n" +
			"        for (int k = 0; k < size3; k++) {\n" +
			"          array[i][j][k] = on ? \"true\" : \"false\";\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) {\n" +
			"    new X().show();\n" +
			"  }\n" +
			"}",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test009() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public final class X {\n" +
			"\n" +
			"  public void show() {\n" +
			"    String s1 = \"\";   String s2 = \"\";   String s3 = \"\";   String s4 = \"\";   String s5 = \"\";   String s6 = \"\";   String s7 = \"\";   String s8 = \"\";   String s9 = \"\";   String s10 = \"\";\n" +
			"    String s11 = \"\";  String s12 = \"\";  String s13 = \"\";  String s14 = \"\";  String s15 = \"\";  String s16 = \"\";  String s17 = \"\";  String s18 = \"\";  String s19 = \"\";  String s20 = \"\";\n" +
			"    String s21 = \"\";  String s22 = \"\";  String s23 = \"\";  String s24 = \"\";  String s25 = \"\";  String s26 = \"\";  String s27 = \"\";  String s28 = \"\";  String s29 = \"\";  String s30 = \"\";\n" +
			"    String s31 = \"\";  String s32 = \"\";  String s33 = \"\";  String s34 = \"\";  String s35 = \"\";  String s36 = \"\";  String s37 = \"\";  String s38 = \"\";  String s39 = \"\";  String s40 = \"\";\n" +
			"    String s41 = \"\";  String s42 = \"\";  String s43 = \"\";  String s44 = \"\";  String s45 = \"\";  String s46 = \"\";  String s47 = \"\";  String s48 = \"\";  String s49 = \"\";  String s50 = \"\";\n" +
			"    String s51 = \"\";  String s52 = \"\";  String s53 = \"\";  String s54 = \"\";  String s55 = \"\";  String s56 = \"\";  String s57 = \"\";  String s58 = \"\";  String s59 = \"\";  String s60 = \"\";\n" +
			"    String s61 = \"\";  String s62 = \"\";  String s63 = \"\";  String s64 = \"\";  String s65 = \"\";  String s66 = \"\";  String s67 = \"\";  String s68 = \"\";  String s69 = \"\";  String s70 = \"\";\n" +
			"    String s71 = \"\";  String s72 = \"\";  String s73 = \"\";  String s74 = \"\";  String s75 = \"\";  String s76 = \"\";  String s77 = \"\";  String s78 = \"\";  String s79 = \"\";  String s80 = \"\";\n" +
			"    String s81 = \"\";  String s82 = \"\";  String s83 = \"\";  String s84 = \"\";  String s85 = \"\";  String s86 = \"\";  String s87 = \"\";  String s88 = \"\";  String s89 = \"\";  String s90 = \"\";\n" +
			"    String s91 = \"\";  String s92 = \"\";  String s93 = \"\";  String s94 = \"\";  String s95 = \"\";  String s96 = \"\";  String s97 = \"\";  String s98 = \"\";  String s99 = \"\";  String s100 = \"\";\n" +
			"    String s101 = \"\"; String s102 = \"\"; String s103 = \"\"; String s104 = \"\"; String s105 = \"\"; String s106 = \"\"; String s107 = \"\"; String s108 = \"\"; String s109 = \"\"; String s110 = \"\";\n" +
			"    String s111 = \"\"; String s112 = \"\"; String s113 = \"\"; String s114 = \"\"; String s115 = \"\"; String s116 = \"\"; String s117 = \"\"; String s118 = \"\"; String s119 = \"\"; String s120 = \"\";\n" +
			"    String s121 = \"\"; String s122 = \"\"; String s123 = \"\"; String s124 = \"\"; String s125 = \"\"; String s126 = \"\"; String s127 = \"\"; String s128 = \"\"; String s129 = \"\"; String s130 = \"\";\n" +
			"    String s131 = \"\"; String s132 = \"\"; String s133 = \"\"; String s134 = \"\"; String s135 = \"\"; String s136 = \"\"; String s137 = \"\"; String s138 = \"\"; String s139 = \"\"; String s140 = \"\";\n" +
			"    String s141 = \"\"; String s142 = \"\"; String s143 = \"\"; String s144 = \"\"; String s145 = \"\"; String s146 = \"\"; String s147 = \"\"; String s148 = \"\"; String s149 = \"\"; String s150 = \"\";\n" +
			"    String s151 = \"\"; String s152 = \"\"; String s153 = \"\"; String s154 = \"\"; String s155 = \"\"; String s156 = \"\"; String s157 = \"\"; String s158 = \"\"; String s159 = \"\"; String s160 = \"\";\n" +
			"    String s161 = \"\"; String s162 = \"\"; String s163 = \"\"; String s164 = \"\"; String s165 = \"\"; String s166 = \"\"; String s167 = \"\"; String s168 = \"\"; String s169 = \"\"; String s170 = \"\";\n" +
			"    String s171 = \"\"; String s172 = \"\"; String s173 = \"\"; String s174 = \"\"; String s175 = \"\"; String s176 = \"\"; String s177 = \"\"; String s178 = \"\"; String s179 = \"\"; String s180 = \"\";\n" +
			"    String s181 = \"\"; String s182 = \"\"; String s183 = \"\"; String s184 = \"\"; String s185 = \"\"; String s186 = \"\"; String s187 = \"\"; String s188 = \"\"; String s189 = \"\"; String s190 = \"\";\n" +
			"    String s191 = \"\"; String s192 = \"\"; String s193 = \"\"; String s194 = \"\"; String s195 = \"\"; String s196 = \"\"; String s197 = \"\"; String s198 = \"\"; String s199 = \"\"; String s200 = \"\";\n" +
			"    String s201 = \"\"; String s202 = \"\"; String s203 = \"\"; String s204 = \"\"; String s205 = \"\"; String s206 = \"\"; String s207 = \"\"; String s208 = \"\"; String s209 = \"\"; String s210 = \"\";\n" +
			"    String s211 = \"\"; String s212 = \"\"; String s213 = \"\"; String s214 = \"\"; String s215 = \"\"; String s216 = \"\"; String s217 = \"\"; String s218 = \"\"; String s219 = \"\"; String s220 = \"\";\n" +
			"    String s221 = \"\"; String s222 = \"\"; String s223 = \"\"; String s224 = \"\"; String s225 = \"\"; String s226 = \"\"; String s227 = \"\"; String s228 = \"\"; String s229 = \"\"; String s230 = \"\";\n" +
			"    String s231 = \"\"; String s232 = \"\"; String s233 = \"\"; String s234 = \"\"; String s235 = \"\"; String s236 = \"\"; String s237 = \"\"; String s238 = \"\"; String s239 = \"\"; String s240 = \"\";\n" +
			"    String s241 = \"\"; String s242 = \"\"; String s243 = \"\"; String s244 = \"\"; String s245 = \"\"; String s246 = \"\"; String s247 = \"\"; String s248 = \"\"; String s249 = \"\"; String s250 = \"\";\n" +
			"    String s251 = \"\"; String s252 = \"\";\n" +
			"\n" +
			"    int size1 = 1;\n" +
			"    int size2 = 2;\n" +
			"    int size3 = 3;\n" +
			"\n" +
			"    boolean[][][] array = new boolean[size1][size2][size3];\n" +
			"    \n" +
			"    for (int i = 0; i < size1; i++) {\n" +
			"      for (int j = 0; j < size2; j++) {\n" +
			"        boolean on = false;\n" +
			"        for (int k = 0; k < size3; k++) {\n" +
			"          array[i][j][k] = on ? true : false;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) {\n" +
			"    new X().show();\n" +
			"  }\n" +
			"}",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test010() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public final class X {\n" +
			"\n" +
			"  public void show() {\n" +
			"    String s1 = \"\";   String s2 = \"\";   String s3 = \"\";   String s4 = \"\";   String s5 = \"\";   String s6 = \"\";   String s7 = \"\";   String s8 = \"\";   String s9 = \"\";   String s10 = \"\";\n" +
			"    String s11 = \"\";  String s12 = \"\";  String s13 = \"\";  String s14 = \"\";  String s15 = \"\";  String s16 = \"\";  String s17 = \"\";  String s18 = \"\";  String s19 = \"\";  String s20 = \"\";\n" +
			"    String s21 = \"\";  String s22 = \"\";  String s23 = \"\";  String s24 = \"\";  String s25 = \"\";  String s26 = \"\";  String s27 = \"\";  String s28 = \"\";  String s29 = \"\";  String s30 = \"\";\n" +
			"    String s31 = \"\";  String s32 = \"\";  String s33 = \"\";  String s34 = \"\";  String s35 = \"\";  String s36 = \"\";  String s37 = \"\";  String s38 = \"\";  String s39 = \"\";  String s40 = \"\";\n" +
			"    String s41 = \"\";  String s42 = \"\";  String s43 = \"\";  String s44 = \"\";  String s45 = \"\";  String s46 = \"\";  String s47 = \"\";  String s48 = \"\";  String s49 = \"\";  String s50 = \"\";\n" +
			"    String s51 = \"\";  String s52 = \"\";  String s53 = \"\";  String s54 = \"\";  String s55 = \"\";  String s56 = \"\";  String s57 = \"\";  String s58 = \"\";  String s59 = \"\";  String s60 = \"\";\n" +
			"    String s61 = \"\";  String s62 = \"\";  String s63 = \"\";  String s64 = \"\";  String s65 = \"\";  String s66 = \"\";  String s67 = \"\";  String s68 = \"\";  String s69 = \"\";  String s70 = \"\";\n" +
			"    String s71 = \"\";  String s72 = \"\";  String s73 = \"\";  String s74 = \"\";  String s75 = \"\";  String s76 = \"\";  String s77 = \"\";  String s78 = \"\";  String s79 = \"\";  String s80 = \"\";\n" +
			"    String s81 = \"\";  String s82 = \"\";  String s83 = \"\";  String s84 = \"\";  String s85 = \"\";  String s86 = \"\";  String s87 = \"\";  String s88 = \"\";  String s89 = \"\";  String s90 = \"\";\n" +
			"    String s91 = \"\";  String s92 = \"\";  String s93 = \"\";  String s94 = \"\";  String s95 = \"\";  String s96 = \"\";  String s97 = \"\";  String s98 = \"\";  String s99 = \"\";  String s100 = \"\";\n" +
			"    String s101 = \"\"; String s102 = \"\"; String s103 = \"\"; String s104 = \"\"; String s105 = \"\"; String s106 = \"\"; String s107 = \"\"; String s108 = \"\"; String s109 = \"\"; String s110 = \"\";\n" +
			"    String s111 = \"\"; String s112 = \"\"; String s113 = \"\"; String s114 = \"\"; String s115 = \"\"; String s116 = \"\"; String s117 = \"\"; String s118 = \"\"; String s119 = \"\"; String s120 = \"\";\n" +
			"    String s121 = \"\"; String s122 = \"\"; String s123 = \"\"; String s124 = \"\"; String s125 = \"\"; String s126 = \"\"; String s127 = \"\"; String s128 = \"\"; String s129 = \"\"; String s130 = \"\";\n" +
			"    String s131 = \"\"; String s132 = \"\"; String s133 = \"\"; String s134 = \"\"; String s135 = \"\"; String s136 = \"\"; String s137 = \"\"; String s138 = \"\"; String s139 = \"\"; String s140 = \"\";\n" +
			"    String s141 = \"\"; String s142 = \"\"; String s143 = \"\"; String s144 = \"\"; String s145 = \"\"; String s146 = \"\"; String s147 = \"\"; String s148 = \"\"; String s149 = \"\"; String s150 = \"\";\n" +
			"    String s151 = \"\"; String s152 = \"\"; String s153 = \"\"; String s154 = \"\"; String s155 = \"\"; String s156 = \"\"; String s157 = \"\"; String s158 = \"\"; String s159 = \"\"; String s160 = \"\";\n" +
			"    String s161 = \"\"; String s162 = \"\"; String s163 = \"\"; String s164 = \"\"; String s165 = \"\"; String s166 = \"\"; String s167 = \"\"; String s168 = \"\"; String s169 = \"\"; String s170 = \"\";\n" +
			"    String s171 = \"\"; String s172 = \"\"; String s173 = \"\"; String s174 = \"\"; String s175 = \"\"; String s176 = \"\"; String s177 = \"\"; String s178 = \"\"; String s179 = \"\"; String s180 = \"\";\n" +
			"    String s181 = \"\"; String s182 = \"\"; String s183 = \"\"; String s184 = \"\"; String s185 = \"\"; String s186 = \"\"; String s187 = \"\"; String s188 = \"\"; String s189 = \"\"; String s190 = \"\";\n" +
			"    String s191 = \"\"; String s192 = \"\"; String s193 = \"\"; String s194 = \"\"; String s195 = \"\"; String s196 = \"\"; String s197 = \"\"; String s198 = \"\"; String s199 = \"\"; String s200 = \"\";\n" +
			"    String s201 = \"\"; String s202 = \"\"; String s203 = \"\"; String s204 = \"\"; String s205 = \"\"; String s206 = \"\"; String s207 = \"\"; String s208 = \"\"; String s209 = \"\"; String s210 = \"\";\n" +
			"    String s211 = \"\"; String s212 = \"\"; String s213 = \"\"; String s214 = \"\"; String s215 = \"\"; String s216 = \"\"; String s217 = \"\"; String s218 = \"\"; String s219 = \"\"; String s220 = \"\";\n" +
			"    String s221 = \"\"; String s222 = \"\"; String s223 = \"\"; String s224 = \"\"; String s225 = \"\"; String s226 = \"\"; String s227 = \"\"; String s228 = \"\"; String s229 = \"\"; String s230 = \"\";\n" +
			"    String s231 = \"\"; String s232 = \"\"; String s233 = \"\"; String s234 = \"\"; String s235 = \"\"; String s236 = \"\"; String s237 = \"\"; String s238 = \"\"; String s239 = \"\"; String s240 = \"\";\n" +
			"    String s241 = \"\"; String s242 = \"\"; String s243 = \"\"; String s244 = \"\"; String s245 = \"\"; String s246 = \"\"; String s247 = \"\"; String s248 = \"\"; String s249 = \"\"; String s250 = \"\";\n" +
			"    String s251 = \"\"; String s252 = \"\";\n" +
			"\n" +
			"    int size1 = 1;\n" +
			"    int size2 = 2;\n" +
			"    int size3 = 3;\n" +
			"\n" +
			"    long[][][] array = new long[size1][size2][size3];\n" +
			"    \n" +
			"    for (int i = 0; i < size1; i++) {\n" +
			"      for (int j = 0; j < size2; j++) {\n" +
			"        boolean on = false;\n" +
			"        for (int k = 0; k < size3; k++) {\n" +
			"          array[i][j][k] = on ? 234L : 12345L;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) {\n" +
			"    new X().show();\n" +
			"  }\n" +
			"}",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test011() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public final class X {\n" +
			"\n" +
			"  public void show() {\n" +
			"    String s1 = \"\";   String s2 = \"\";   String s3 = \"\";   String s4 = \"\";   String s5 = \"\";   String s6 = \"\";   String s7 = \"\";   String s8 = \"\";   String s9 = \"\";   String s10 = \"\";\n" +
			"    String s11 = \"\";  String s12 = \"\";  String s13 = \"\";  String s14 = \"\";  String s15 = \"\";  String s16 = \"\";  String s17 = \"\";  String s18 = \"\";  String s19 = \"\";  String s20 = \"\";\n" +
			"    String s21 = \"\";  String s22 = \"\";  String s23 = \"\";  String s24 = \"\";  String s25 = \"\";  String s26 = \"\";  String s27 = \"\";  String s28 = \"\";  String s29 = \"\";  String s30 = \"\";\n" +
			"    String s31 = \"\";  String s32 = \"\";  String s33 = \"\";  String s34 = \"\";  String s35 = \"\";  String s36 = \"\";  String s37 = \"\";  String s38 = \"\";  String s39 = \"\";  String s40 = \"\";\n" +
			"    String s41 = \"\";  String s42 = \"\";  String s43 = \"\";  String s44 = \"\";  String s45 = \"\";  String s46 = \"\";  String s47 = \"\";  String s48 = \"\";  String s49 = \"\";  String s50 = \"\";\n" +
			"    String s51 = \"\";  String s52 = \"\";  String s53 = \"\";  String s54 = \"\";  String s55 = \"\";  String s56 = \"\";  String s57 = \"\";  String s58 = \"\";  String s59 = \"\";  String s60 = \"\";\n" +
			"    String s61 = \"\";  String s62 = \"\";  String s63 = \"\";  String s64 = \"\";  String s65 = \"\";  String s66 = \"\";  String s67 = \"\";  String s68 = \"\";  String s69 = \"\";  String s70 = \"\";\n" +
			"    String s71 = \"\";  String s72 = \"\";  String s73 = \"\";  String s74 = \"\";  String s75 = \"\";  String s76 = \"\";  String s77 = \"\";  String s78 = \"\";  String s79 = \"\";  String s80 = \"\";\n" +
			"    String s81 = \"\";  String s82 = \"\";  String s83 = \"\";  String s84 = \"\";  String s85 = \"\";  String s86 = \"\";  String s87 = \"\";  String s88 = \"\";  String s89 = \"\";  String s90 = \"\";\n" +
			"    String s91 = \"\";  String s92 = \"\";  String s93 = \"\";  String s94 = \"\";  String s95 = \"\";  String s96 = \"\";  String s97 = \"\";  String s98 = \"\";  String s99 = \"\";  String s100 = \"\";\n" +
			"    String s101 = \"\"; String s102 = \"\"; String s103 = \"\"; String s104 = \"\"; String s105 = \"\"; String s106 = \"\"; String s107 = \"\"; String s108 = \"\"; String s109 = \"\"; String s110 = \"\";\n" +
			"    String s111 = \"\"; String s112 = \"\"; String s113 = \"\"; String s114 = \"\"; String s115 = \"\"; String s116 = \"\"; String s117 = \"\"; String s118 = \"\"; String s119 = \"\"; String s120 = \"\";\n" +
			"    String s121 = \"\"; String s122 = \"\"; String s123 = \"\"; String s124 = \"\"; String s125 = \"\"; String s126 = \"\"; String s127 = \"\"; String s128 = \"\"; String s129 = \"\"; String s130 = \"\";\n" +
			"    String s131 = \"\"; String s132 = \"\"; String s133 = \"\"; String s134 = \"\"; String s135 = \"\"; String s136 = \"\"; String s137 = \"\"; String s138 = \"\"; String s139 = \"\"; String s140 = \"\";\n" +
			"    String s141 = \"\"; String s142 = \"\"; String s143 = \"\"; String s144 = \"\"; String s145 = \"\"; String s146 = \"\"; String s147 = \"\"; String s148 = \"\"; String s149 = \"\"; String s150 = \"\";\n" +
			"    String s151 = \"\"; String s152 = \"\"; String s153 = \"\"; String s154 = \"\"; String s155 = \"\"; String s156 = \"\"; String s157 = \"\"; String s158 = \"\"; String s159 = \"\"; String s160 = \"\";\n" +
			"    String s161 = \"\"; String s162 = \"\"; String s163 = \"\"; String s164 = \"\"; String s165 = \"\"; String s166 = \"\"; String s167 = \"\"; String s168 = \"\"; String s169 = \"\"; String s170 = \"\";\n" +
			"    String s171 = \"\"; String s172 = \"\"; String s173 = \"\"; String s174 = \"\"; String s175 = \"\"; String s176 = \"\"; String s177 = \"\"; String s178 = \"\"; String s179 = \"\"; String s180 = \"\";\n" +
			"    String s181 = \"\"; String s182 = \"\"; String s183 = \"\"; String s184 = \"\"; String s185 = \"\"; String s186 = \"\"; String s187 = \"\"; String s188 = \"\"; String s189 = \"\"; String s190 = \"\";\n" +
			"    String s191 = \"\"; String s192 = \"\"; String s193 = \"\"; String s194 = \"\"; String s195 = \"\"; String s196 = \"\"; String s197 = \"\"; String s198 = \"\"; String s199 = \"\"; String s200 = \"\";\n" +
			"    String s201 = \"\"; String s202 = \"\"; String s203 = \"\"; String s204 = \"\"; String s205 = \"\"; String s206 = \"\"; String s207 = \"\"; String s208 = \"\"; String s209 = \"\"; String s210 = \"\";\n" +
			"    String s211 = \"\"; String s212 = \"\"; String s213 = \"\"; String s214 = \"\"; String s215 = \"\"; String s216 = \"\"; String s217 = \"\"; String s218 = \"\"; String s219 = \"\"; String s220 = \"\";\n" +
			"    String s221 = \"\"; String s222 = \"\"; String s223 = \"\"; String s224 = \"\"; String s225 = \"\"; String s226 = \"\"; String s227 = \"\"; String s228 = \"\"; String s229 = \"\"; String s230 = \"\";\n" +
			"    String s231 = \"\"; String s232 = \"\"; String s233 = \"\"; String s234 = \"\"; String s235 = \"\"; String s236 = \"\"; String s237 = \"\"; String s238 = \"\"; String s239 = \"\"; String s240 = \"\";\n" +
			"    String s241 = \"\"; String s242 = \"\"; String s243 = \"\"; String s244 = \"\"; String s245 = \"\"; String s246 = \"\"; String s247 = \"\"; String s248 = \"\"; String s249 = \"\"; String s250 = \"\";\n" +
			"    String s251 = \"\"; String s252 = \"\";\n" +
			"\n" +
			"    int size1 = 1;\n" +
			"    int size2 = 2;\n" +
			"    int size3 = 3;\n" +
			"\n" +
			"    double[][][] array = new double[size1][size2][size3];\n" +
			"    \n" +
			"    for (int i = 0; i < size1; i++) {\n" +
			"      for (int j = 0; j < size2; j++) {\n" +
			"        boolean on = false;\n" +
			"        for (int k = 0; k < size3; k++) {\n" +
			"          array[i][j][k] = on ? 2.0 : 3.0;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) {\n" +
			"    new X().show();\n" +
			"  }\n" +
			"}",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test012() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public final class X {\n" +
			"\n" +
			"  public void show() {\n" +
			"    String s1 = \"\";   String s2 = \"\";   String s3 = \"\";   String s4 = \"\";   String s5 = \"\";   String s6 = \"\";   String s7 = \"\";   String s8 = \"\";   String s9 = \"\";   String s10 = \"\";\n" +
			"    String s11 = \"\";  String s12 = \"\";  String s13 = \"\";  String s14 = \"\";  String s15 = \"\";  String s16 = \"\";  String s17 = \"\";  String s18 = \"\";  String s19 = \"\";  String s20 = \"\";\n" +
			"    String s21 = \"\";  String s22 = \"\";  String s23 = \"\";  String s24 = \"\";  String s25 = \"\";  String s26 = \"\";  String s27 = \"\";  String s28 = \"\";  String s29 = \"\";  String s30 = \"\";\n" +
			"    String s31 = \"\";  String s32 = \"\";  String s33 = \"\";  String s34 = \"\";  String s35 = \"\";  String s36 = \"\";  String s37 = \"\";  String s38 = \"\";  String s39 = \"\";  String s40 = \"\";\n" +
			"    String s41 = \"\";  String s42 = \"\";  String s43 = \"\";  String s44 = \"\";  String s45 = \"\";  String s46 = \"\";  String s47 = \"\";  String s48 = \"\";  String s49 = \"\";  String s50 = \"\";\n" +
			"    String s51 = \"\";  String s52 = \"\";  String s53 = \"\";  String s54 = \"\";  String s55 = \"\";  String s56 = \"\";  String s57 = \"\";  String s58 = \"\";  String s59 = \"\";  String s60 = \"\";\n" +
			"    String s61 = \"\";  String s62 = \"\";  String s63 = \"\";  String s64 = \"\";  String s65 = \"\";  String s66 = \"\";  String s67 = \"\";  String s68 = \"\";  String s69 = \"\";  String s70 = \"\";\n" +
			"    String s71 = \"\";  String s72 = \"\";  String s73 = \"\";  String s74 = \"\";  String s75 = \"\";  String s76 = \"\";  String s77 = \"\";  String s78 = \"\";  String s79 = \"\";  String s80 = \"\";\n" +
			"    String s81 = \"\";  String s82 = \"\";  String s83 = \"\";  String s84 = \"\";  String s85 = \"\";  String s86 = \"\";  String s87 = \"\";  String s88 = \"\";  String s89 = \"\";  String s90 = \"\";\n" +
			"    String s91 = \"\";  String s92 = \"\";  String s93 = \"\";  String s94 = \"\";  String s95 = \"\";  String s96 = \"\";  String s97 = \"\";  String s98 = \"\";  String s99 = \"\";  String s100 = \"\";\n" +
			"    String s101 = \"\"; String s102 = \"\"; String s103 = \"\"; String s104 = \"\"; String s105 = \"\"; String s106 = \"\"; String s107 = \"\"; String s108 = \"\"; String s109 = \"\"; String s110 = \"\";\n" +
			"    String s111 = \"\"; String s112 = \"\"; String s113 = \"\"; String s114 = \"\"; String s115 = \"\"; String s116 = \"\"; String s117 = \"\"; String s118 = \"\"; String s119 = \"\"; String s120 = \"\";\n" +
			"    String s121 = \"\"; String s122 = \"\"; String s123 = \"\"; String s124 = \"\"; String s125 = \"\"; String s126 = \"\"; String s127 = \"\"; String s128 = \"\"; String s129 = \"\"; String s130 = \"\";\n" +
			"    String s131 = \"\"; String s132 = \"\"; String s133 = \"\"; String s134 = \"\"; String s135 = \"\"; String s136 = \"\"; String s137 = \"\"; String s138 = \"\"; String s139 = \"\"; String s140 = \"\";\n" +
			"    String s141 = \"\"; String s142 = \"\"; String s143 = \"\"; String s144 = \"\"; String s145 = \"\"; String s146 = \"\"; String s147 = \"\"; String s148 = \"\"; String s149 = \"\"; String s150 = \"\";\n" +
			"    String s151 = \"\"; String s152 = \"\"; String s153 = \"\"; String s154 = \"\"; String s155 = \"\"; String s156 = \"\"; String s157 = \"\"; String s158 = \"\"; String s159 = \"\"; String s160 = \"\";\n" +
			"    String s161 = \"\"; String s162 = \"\"; String s163 = \"\"; String s164 = \"\"; String s165 = \"\"; String s166 = \"\"; String s167 = \"\"; String s168 = \"\"; String s169 = \"\"; String s170 = \"\";\n" +
			"    String s171 = \"\"; String s172 = \"\"; String s173 = \"\"; String s174 = \"\"; String s175 = \"\"; String s176 = \"\"; String s177 = \"\"; String s178 = \"\"; String s179 = \"\"; String s180 = \"\";\n" +
			"    String s181 = \"\"; String s182 = \"\"; String s183 = \"\"; String s184 = \"\"; String s185 = \"\"; String s186 = \"\"; String s187 = \"\"; String s188 = \"\"; String s189 = \"\"; String s190 = \"\";\n" +
			"    String s191 = \"\"; String s192 = \"\"; String s193 = \"\"; String s194 = \"\"; String s195 = \"\"; String s196 = \"\"; String s197 = \"\"; String s198 = \"\"; String s199 = \"\"; String s200 = \"\";\n" +
			"    String s201 = \"\"; String s202 = \"\"; String s203 = \"\"; String s204 = \"\"; String s205 = \"\"; String s206 = \"\"; String s207 = \"\"; String s208 = \"\"; String s209 = \"\"; String s210 = \"\";\n" +
			"    String s211 = \"\"; String s212 = \"\"; String s213 = \"\"; String s214 = \"\"; String s215 = \"\"; String s216 = \"\"; String s217 = \"\"; String s218 = \"\"; String s219 = \"\"; String s220 = \"\";\n" +
			"    String s221 = \"\"; String s222 = \"\"; String s223 = \"\"; String s224 = \"\"; String s225 = \"\"; String s226 = \"\"; String s227 = \"\"; String s228 = \"\"; String s229 = \"\"; String s230 = \"\";\n" +
			"    String s231 = \"\"; String s232 = \"\"; String s233 = \"\"; String s234 = \"\"; String s235 = \"\"; String s236 = \"\"; String s237 = \"\"; String s238 = \"\"; String s239 = \"\"; String s240 = \"\";\n" +
			"    String s241 = \"\"; String s242 = \"\"; String s243 = \"\"; String s244 = \"\"; String s245 = \"\"; String s246 = \"\"; String s247 = \"\"; String s248 = \"\"; String s249 = \"\"; String s250 = \"\";\n" +
			"    String s251 = \"\"; String s252 = \"\";\n" +
			"\n" +
			"    int size1 = 1;\n" +
			"    int size2 = 2;\n" +
			"    int size3 = 3;\n" +
			"\n" +
			"    float[][][] array = new float[size1][size2][size3];\n" +
			"    \n" +
			"    for (int i = 0; i < size1; i++) {\n" +
			"      for (int j = 0; j < size2; j++) {\n" +
			"        boolean on = false;\n" +
			"        for (int k = 0; k < size3; k++) {\n" +
			"          array[i][j][k] = on ? 2.0f : 3.0f;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) {\n" +
			"    new X().show();\n" +
			"  }\n" +
			"}",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test013() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public final class X {\n" +
			"\n" +
			"  public void show() {\n" +
			"    String s1 = \"\";   String s2 = \"\";   String s3 = \"\";   String s4 = \"\";   String s5 = \"\";   String s6 = \"\";   String s7 = \"\";   String s8 = \"\";   String s9 = \"\";   String s10 = \"\";\n" +
			"    String s11 = \"\";  String s12 = \"\";  String s13 = \"\";  String s14 = \"\";  String s15 = \"\";  String s16 = \"\";  String s17 = \"\";  String s18 = \"\";  String s19 = \"\";  String s20 = \"\";\n" +
			"    String s21 = \"\";  String s22 = \"\";  String s23 = \"\";  String s24 = \"\";  String s25 = \"\";  String s26 = \"\";  String s27 = \"\";  String s28 = \"\";  String s29 = \"\";  String s30 = \"\";\n" +
			"    String s31 = \"\";  String s32 = \"\";  String s33 = \"\";  String s34 = \"\";  String s35 = \"\";  String s36 = \"\";  String s37 = \"\";  String s38 = \"\";  String s39 = \"\";  String s40 = \"\";\n" +
			"    String s41 = \"\";  String s42 = \"\";  String s43 = \"\";  String s44 = \"\";  String s45 = \"\";  String s46 = \"\";  String s47 = \"\";  String s48 = \"\";  String s49 = \"\";  String s50 = \"\";\n" +
			"    String s51 = \"\";  String s52 = \"\";  String s53 = \"\";  String s54 = \"\";  String s55 = \"\";  String s56 = \"\";  String s57 = \"\";  String s58 = \"\";  String s59 = \"\";  String s60 = \"\";\n" +
			"    String s61 = \"\";  String s62 = \"\";  String s63 = \"\";  String s64 = \"\";  String s65 = \"\";  String s66 = \"\";  String s67 = \"\";  String s68 = \"\";  String s69 = \"\";  String s70 = \"\";\n" +
			"    String s71 = \"\";  String s72 = \"\";  String s73 = \"\";  String s74 = \"\";  String s75 = \"\";  String s76 = \"\";  String s77 = \"\";  String s78 = \"\";  String s79 = \"\";  String s80 = \"\";\n" +
			"    String s81 = \"\";  String s82 = \"\";  String s83 = \"\";  String s84 = \"\";  String s85 = \"\";  String s86 = \"\";  String s87 = \"\";  String s88 = \"\";  String s89 = \"\";  String s90 = \"\";\n" +
			"    String s91 = \"\";  String s92 = \"\";  String s93 = \"\";  String s94 = \"\";  String s95 = \"\";  String s96 = \"\";  String s97 = \"\";  String s98 = \"\";  String s99 = \"\";  String s100 = \"\";\n" +
			"    String s101 = \"\"; String s102 = \"\"; String s103 = \"\"; String s104 = \"\"; String s105 = \"\"; String s106 = \"\"; String s107 = \"\"; String s108 = \"\"; String s109 = \"\"; String s110 = \"\";\n" +
			"    String s111 = \"\"; String s112 = \"\"; String s113 = \"\"; String s114 = \"\"; String s115 = \"\"; String s116 = \"\"; String s117 = \"\"; String s118 = \"\"; String s119 = \"\"; String s120 = \"\";\n" +
			"    String s121 = \"\"; String s122 = \"\"; String s123 = \"\"; String s124 = \"\"; String s125 = \"\"; String s126 = \"\"; String s127 = \"\"; String s128 = \"\"; String s129 = \"\"; String s130 = \"\";\n" +
			"    String s131 = \"\"; String s132 = \"\"; String s133 = \"\"; String s134 = \"\"; String s135 = \"\"; String s136 = \"\"; String s137 = \"\"; String s138 = \"\"; String s139 = \"\"; String s140 = \"\";\n" +
			"    String s141 = \"\"; String s142 = \"\"; String s143 = \"\"; String s144 = \"\"; String s145 = \"\"; String s146 = \"\"; String s147 = \"\"; String s148 = \"\"; String s149 = \"\"; String s150 = \"\";\n" +
			"    String s151 = \"\"; String s152 = \"\"; String s153 = \"\"; String s154 = \"\"; String s155 = \"\"; String s156 = \"\"; String s157 = \"\"; String s158 = \"\"; String s159 = \"\"; String s160 = \"\";\n" +
			"    String s161 = \"\"; String s162 = \"\"; String s163 = \"\"; String s164 = \"\"; String s165 = \"\"; String s166 = \"\"; String s167 = \"\"; String s168 = \"\"; String s169 = \"\"; String s170 = \"\";\n" +
			"    String s171 = \"\"; String s172 = \"\"; String s173 = \"\"; String s174 = \"\"; String s175 = \"\"; String s176 = \"\"; String s177 = \"\"; String s178 = \"\"; String s179 = \"\"; String s180 = \"\";\n" +
			"    String s181 = \"\"; String s182 = \"\"; String s183 = \"\"; String s184 = \"\"; String s185 = \"\"; String s186 = \"\"; String s187 = \"\"; String s188 = \"\"; String s189 = \"\"; String s190 = \"\";\n" +
			"    String s191 = \"\"; String s192 = \"\"; String s193 = \"\"; String s194 = \"\"; String s195 = \"\"; String s196 = \"\"; String s197 = \"\"; String s198 = \"\"; String s199 = \"\"; String s200 = \"\";\n" +
			"    String s201 = \"\"; String s202 = \"\"; String s203 = \"\"; String s204 = \"\"; String s205 = \"\"; String s206 = \"\"; String s207 = \"\"; String s208 = \"\"; String s209 = \"\"; String s210 = \"\";\n" +
			"    String s211 = \"\"; String s212 = \"\"; String s213 = \"\"; String s214 = \"\"; String s215 = \"\"; String s216 = \"\"; String s217 = \"\"; String s218 = \"\"; String s219 = \"\"; String s220 = \"\";\n" +
			"    String s221 = \"\"; String s222 = \"\"; String s223 = \"\"; String s224 = \"\"; String s225 = \"\"; String s226 = \"\"; String s227 = \"\"; String s228 = \"\"; String s229 = \"\"; String s230 = \"\";\n" +
			"    String s231 = \"\"; String s232 = \"\"; String s233 = \"\"; String s234 = \"\"; String s235 = \"\"; String s236 = \"\"; String s237 = \"\"; String s238 = \"\"; String s239 = \"\"; String s240 = \"\";\n" +
			"    String s241 = \"\"; String s242 = \"\"; String s243 = \"\"; String s244 = \"\"; String s245 = \"\"; String s246 = \"\"; String s247 = \"\"; String s248 = \"\"; String s249 = \"\"; String s250 = \"\";\n" +
			"    String s251 = \"\"; String s252 = \"\";\n" +
			"\n" +
			"    int size1 = 1;\n" +
			"    int size2 = 2;\n" +
			"    int size3 = 3;\n" +
			"\n" +
			"    char[][][] array = new char[size1][size2][size3];\n" +
			"    \n" +
			"    for (int i = 0; i < size1; i++) {\n" +
			"      for (int j = 0; j < size2; j++) {\n" +
			"        boolean on = false;\n" +
			"        for (int k = 0; k < size3; k++) {\n" +
			"          array[i][j][k] = on ? 'c' : 'd';\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) {\n" +
			"    new X().show();\n" +
			"  }\n" +
			"}",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test014() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public final class X {\n" +
			"\n" +
			"  public void show() {\n" +
			"    String s1 = \"\";   String s2 = \"\";   String s3 = \"\";   String s4 = \"\";   String s5 = \"\";   String s6 = \"\";   String s7 = \"\";   String s8 = \"\";   String s9 = \"\";   String s10 = \"\";\n" +
			"    String s11 = \"\";  String s12 = \"\";  String s13 = \"\";  String s14 = \"\";  String s15 = \"\";  String s16 = \"\";  String s17 = \"\";  String s18 = \"\";  String s19 = \"\";  String s20 = \"\";\n" +
			"    String s21 = \"\";  String s22 = \"\";  String s23 = \"\";  String s24 = \"\";  String s25 = \"\";  String s26 = \"\";  String s27 = \"\";  String s28 = \"\";  String s29 = \"\";  String s30 = \"\";\n" +
			"    String s31 = \"\";  String s32 = \"\";  String s33 = \"\";  String s34 = \"\";  String s35 = \"\";  String s36 = \"\";  String s37 = \"\";  String s38 = \"\";  String s39 = \"\";  String s40 = \"\";\n" +
			"    String s41 = \"\";  String s42 = \"\";  String s43 = \"\";  String s44 = \"\";  String s45 = \"\";  String s46 = \"\";  String s47 = \"\";  String s48 = \"\";  String s49 = \"\";  String s50 = \"\";\n" +
			"    String s51 = \"\";  String s52 = \"\";  String s53 = \"\";  String s54 = \"\";  String s55 = \"\";  String s56 = \"\";  String s57 = \"\";  String s58 = \"\";  String s59 = \"\";  String s60 = \"\";\n" +
			"    String s61 = \"\";  String s62 = \"\";  String s63 = \"\";  String s64 = \"\";  String s65 = \"\";  String s66 = \"\";  String s67 = \"\";  String s68 = \"\";  String s69 = \"\";  String s70 = \"\";\n" +
			"    String s71 = \"\";  String s72 = \"\";  String s73 = \"\";  String s74 = \"\";  String s75 = \"\";  String s76 = \"\";  String s77 = \"\";  String s78 = \"\";  String s79 = \"\";  String s80 = \"\";\n" +
			"    String s81 = \"\";  String s82 = \"\";  String s83 = \"\";  String s84 = \"\";  String s85 = \"\";  String s86 = \"\";  String s87 = \"\";  String s88 = \"\";  String s89 = \"\";  String s90 = \"\";\n" +
			"    String s91 = \"\";  String s92 = \"\";  String s93 = \"\";  String s94 = \"\";  String s95 = \"\";  String s96 = \"\";  String s97 = \"\";  String s98 = \"\";  String s99 = \"\";  String s100 = \"\";\n" +
			"    String s101 = \"\"; String s102 = \"\"; String s103 = \"\"; String s104 = \"\"; String s105 = \"\"; String s106 = \"\"; String s107 = \"\"; String s108 = \"\"; String s109 = \"\"; String s110 = \"\";\n" +
			"    String s111 = \"\"; String s112 = \"\"; String s113 = \"\"; String s114 = \"\"; String s115 = \"\"; String s116 = \"\"; String s117 = \"\"; String s118 = \"\"; String s119 = \"\"; String s120 = \"\";\n" +
			"    String s121 = \"\"; String s122 = \"\"; String s123 = \"\"; String s124 = \"\"; String s125 = \"\"; String s126 = \"\"; String s127 = \"\"; String s128 = \"\"; String s129 = \"\"; String s130 = \"\";\n" +
			"    String s131 = \"\"; String s132 = \"\"; String s133 = \"\"; String s134 = \"\"; String s135 = \"\"; String s136 = \"\"; String s137 = \"\"; String s138 = \"\"; String s139 = \"\"; String s140 = \"\";\n" +
			"    String s141 = \"\"; String s142 = \"\"; String s143 = \"\"; String s144 = \"\"; String s145 = \"\"; String s146 = \"\"; String s147 = \"\"; String s148 = \"\"; String s149 = \"\"; String s150 = \"\";\n" +
			"    String s151 = \"\"; String s152 = \"\"; String s153 = \"\"; String s154 = \"\"; String s155 = \"\"; String s156 = \"\"; String s157 = \"\"; String s158 = \"\"; String s159 = \"\"; String s160 = \"\";\n" +
			"    String s161 = \"\"; String s162 = \"\"; String s163 = \"\"; String s164 = \"\"; String s165 = \"\"; String s166 = \"\"; String s167 = \"\"; String s168 = \"\"; String s169 = \"\"; String s170 = \"\";\n" +
			"    String s171 = \"\"; String s172 = \"\"; String s173 = \"\"; String s174 = \"\"; String s175 = \"\"; String s176 = \"\"; String s177 = \"\"; String s178 = \"\"; String s179 = \"\"; String s180 = \"\";\n" +
			"    String s181 = \"\"; String s182 = \"\"; String s183 = \"\"; String s184 = \"\"; String s185 = \"\"; String s186 = \"\"; String s187 = \"\"; String s188 = \"\"; String s189 = \"\"; String s190 = \"\";\n" +
			"    String s191 = \"\"; String s192 = \"\"; String s193 = \"\"; String s194 = \"\"; String s195 = \"\"; String s196 = \"\"; String s197 = \"\"; String s198 = \"\"; String s199 = \"\"; String s200 = \"\";\n" +
			"    String s201 = \"\"; String s202 = \"\"; String s203 = \"\"; String s204 = \"\"; String s205 = \"\"; String s206 = \"\"; String s207 = \"\"; String s208 = \"\"; String s209 = \"\"; String s210 = \"\";\n" +
			"    String s211 = \"\"; String s212 = \"\"; String s213 = \"\"; String s214 = \"\"; String s215 = \"\"; String s216 = \"\"; String s217 = \"\"; String s218 = \"\"; String s219 = \"\"; String s220 = \"\";\n" +
			"    String s221 = \"\"; String s222 = \"\"; String s223 = \"\"; String s224 = \"\"; String s225 = \"\"; String s226 = \"\"; String s227 = \"\"; String s228 = \"\"; String s229 = \"\"; String s230 = \"\";\n" +
			"    String s231 = \"\"; String s232 = \"\"; String s233 = \"\"; String s234 = \"\"; String s235 = \"\"; String s236 = \"\"; String s237 = \"\"; String s238 = \"\"; String s239 = \"\"; String s240 = \"\";\n" +
			"    String s241 = \"\"; String s242 = \"\"; String s243 = \"\"; String s244 = \"\"; String s245 = \"\"; String s246 = \"\"; String s247 = \"\"; String s248 = \"\"; String s249 = \"\"; String s250 = \"\";\n" +
			"    String s251 = \"\"; String s252 = \"\";\n" +
			"\n" +
			"    int size1 = 1;\n" +
			"    int size2 = 2;\n" +
			"    int size3 = 3;\n" +
			"\n" +
			"    byte[][][] array = new byte[size1][size2][size3];\n" +
			"    \n" +
			"    for (int i = 0; i < size1; i++) {\n" +
			"      for (int j = 0; j < size2; j++) {\n" +
			"        boolean on = false;\n" +
			"        for (int k = 0; k < size3; k++) {\n" +
			"          array[i][j][k] = on ? (byte) 1 : (byte) 0;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) {\n" +
			"    new X().show();\n" +
			"  }\n" +
			"}",
		},
		"",
		settings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=457426
public void test015() throws Exception {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public final class X {\n" +
			"\n" +
			"  public void show() {\n" +
			"    String s1 = \"\";   String s2 = \"\";   String s3 = \"\";   String s4 = \"\";   String s5 = \"\";   String s6 = \"\";   String s7 = \"\";   String s8 = \"\";   String s9 = \"\";   String s10 = \"\";\n" +
			"    String s11 = \"\";  String s12 = \"\";  String s13 = \"\";  String s14 = \"\";  String s15 = \"\";  String s16 = \"\";  String s17 = \"\";  String s18 = \"\";  String s19 = \"\";  String s20 = \"\";\n" +
			"    String s21 = \"\";  String s22 = \"\";  String s23 = \"\";  String s24 = \"\";  String s25 = \"\";  String s26 = \"\";  String s27 = \"\";  String s28 = \"\";  String s29 = \"\";  String s30 = \"\";\n" +
			"    String s31 = \"\";  String s32 = \"\";  String s33 = \"\";  String s34 = \"\";  String s35 = \"\";  String s36 = \"\";  String s37 = \"\";  String s38 = \"\";  String s39 = \"\";  String s40 = \"\";\n" +
			"    String s41 = \"\";  String s42 = \"\";  String s43 = \"\";  String s44 = \"\";  String s45 = \"\";  String s46 = \"\";  String s47 = \"\";  String s48 = \"\";  String s49 = \"\";  String s50 = \"\";\n" +
			"    String s51 = \"\";  String s52 = \"\";  String s53 = \"\";  String s54 = \"\";  String s55 = \"\";  String s56 = \"\";  String s57 = \"\";  String s58 = \"\";  String s59 = \"\";  String s60 = \"\";\n" +
			"    String s61 = \"\";  String s62 = \"\";  String s63 = \"\";  String s64 = \"\";  String s65 = \"\";  String s66 = \"\";  String s67 = \"\";  String s68 = \"\";  String s69 = \"\";  String s70 = \"\";\n" +
			"    String s71 = \"\";  String s72 = \"\";  String s73 = \"\";  String s74 = \"\";  String s75 = \"\";  String s76 = \"\";  String s77 = \"\";  String s78 = \"\";  String s79 = \"\";  String s80 = \"\";\n" +
			"    String s81 = \"\";  String s82 = \"\";  String s83 = \"\";  String s84 = \"\";  String s85 = \"\";  String s86 = \"\";  String s87 = \"\";  String s88 = \"\";  String s89 = \"\";  String s90 = \"\";\n" +
			"    String s91 = \"\";  String s92 = \"\";  String s93 = \"\";  String s94 = \"\";  String s95 = \"\";  String s96 = \"\";  String s97 = \"\";  String s98 = \"\";  String s99 = \"\";  String s100 = \"\";\n" +
			"    String s101 = \"\"; String s102 = \"\"; String s103 = \"\"; String s104 = \"\"; String s105 = \"\"; String s106 = \"\"; String s107 = \"\"; String s108 = \"\"; String s109 = \"\"; String s110 = \"\";\n" +
			"    String s111 = \"\"; String s112 = \"\"; String s113 = \"\"; String s114 = \"\"; String s115 = \"\"; String s116 = \"\"; String s117 = \"\"; String s118 = \"\"; String s119 = \"\"; String s120 = \"\";\n" +
			"    String s121 = \"\"; String s122 = \"\"; String s123 = \"\"; String s124 = \"\"; String s125 = \"\"; String s126 = \"\"; String s127 = \"\"; String s128 = \"\"; String s129 = \"\"; String s130 = \"\";\n" +
			"    String s131 = \"\"; String s132 = \"\"; String s133 = \"\"; String s134 = \"\"; String s135 = \"\"; String s136 = \"\"; String s137 = \"\"; String s138 = \"\"; String s139 = \"\"; String s140 = \"\";\n" +
			"    String s141 = \"\"; String s142 = \"\"; String s143 = \"\"; String s144 = \"\"; String s145 = \"\"; String s146 = \"\"; String s147 = \"\"; String s148 = \"\"; String s149 = \"\"; String s150 = \"\";\n" +
			"    String s151 = \"\"; String s152 = \"\"; String s153 = \"\"; String s154 = \"\"; String s155 = \"\"; String s156 = \"\"; String s157 = \"\"; String s158 = \"\"; String s159 = \"\"; String s160 = \"\";\n" +
			"    String s161 = \"\"; String s162 = \"\"; String s163 = \"\"; String s164 = \"\"; String s165 = \"\"; String s166 = \"\"; String s167 = \"\"; String s168 = \"\"; String s169 = \"\"; String s170 = \"\";\n" +
			"    String s171 = \"\"; String s172 = \"\"; String s173 = \"\"; String s174 = \"\"; String s175 = \"\"; String s176 = \"\"; String s177 = \"\"; String s178 = \"\"; String s179 = \"\"; String s180 = \"\";\n" +
			"    String s181 = \"\"; String s182 = \"\"; String s183 = \"\"; String s184 = \"\"; String s185 = \"\"; String s186 = \"\"; String s187 = \"\"; String s188 = \"\"; String s189 = \"\"; String s190 = \"\";\n" +
			"    String s191 = \"\"; String s192 = \"\"; String s193 = \"\"; String s194 = \"\"; String s195 = \"\"; String s196 = \"\"; String s197 = \"\"; String s198 = \"\"; String s199 = \"\"; String s200 = \"\";\n" +
			"    String s201 = \"\"; String s202 = \"\"; String s203 = \"\"; String s204 = \"\"; String s205 = \"\"; String s206 = \"\"; String s207 = \"\"; String s208 = \"\"; String s209 = \"\"; String s210 = \"\";\n" +
			"    String s211 = \"\"; String s212 = \"\"; String s213 = \"\"; String s214 = \"\"; String s215 = \"\"; String s216 = \"\"; String s217 = \"\"; String s218 = \"\"; String s219 = \"\"; String s220 = \"\";\n" +
			"    String s221 = \"\"; String s222 = \"\"; String s223 = \"\"; String s224 = \"\"; String s225 = \"\"; String s226 = \"\"; String s227 = \"\"; String s228 = \"\"; String s229 = \"\"; String s230 = \"\";\n" +
			"    String s231 = \"\"; String s232 = \"\"; String s233 = \"\"; String s234 = \"\"; String s235 = \"\"; String s236 = \"\"; String s237 = \"\"; String s238 = \"\"; String s239 = \"\"; String s240 = \"\";\n" +
			"    String s241 = \"\"; String s242 = \"\"; String s243 = \"\"; String s244 = \"\"; String s245 = \"\"; String s246 = \"\"; String s247 = \"\"; String s248 = \"\"; String s249 = \"\"; String s250 = \"\";\n" +
			"    String s251 = \"\"; String s252 = \"\";\n" +
			"\n" +
			"    int size1 = 1;\n" +
			"    int size2 = 2;\n" +
			"    int size3 = 3;\n" +
			"\n" +
			"    short[][][] array = new short[size1][size2][size3];\n" +
			"    \n" +
			"    for (int i = 0; i < size1; i++) {\n" +
			"      for (int j = 0; j < size2; j++) {\n" +
			"        boolean on = false;\n" +
			"        for (int k = 0; k < size3; k++) {\n" +
			"          array[i][j][k] = on ? (short) 1 : (short) 0;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) {\n" +
			"    new X().show();\n" +
			"  }\n" +
			"}",
		},
		"",
		settings);
}
public static Class testClass() {
	return ForStatementTest.class;
}
}
