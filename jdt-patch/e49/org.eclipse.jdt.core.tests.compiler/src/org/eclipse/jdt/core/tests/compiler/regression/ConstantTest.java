/*******************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation and others.
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

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ClassSignature;
import org.eclipse.jdt.internal.compiler.env.EnumConstantSignature;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.ByteConstant;
import org.eclipse.jdt.internal.compiler.impl.CharConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.LongConstant;
import org.eclipse.jdt.internal.compiler.impl.ShortConstant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.problem.ShouldNotImplement;

@SuppressWarnings({ "rawtypes" })
public class ConstantTest extends AbstractRegressionTest {

public ConstantTest(String name) {
	super(name);
}
// Use this static initializer to specify subset for tests
// All specified tests which does not belong to the class are skipped...
static {
//	TESTS_PREFIX = "testBug95521";
//	TESTS_NAMES = new String[] { "testBug83127a" };
//	TESTS_NUMBERS = new int[] { 21 };
//	TESTS_RANGE = new int[] { 23, -1 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class  X { \n" +
		"public static void main (String args []) {\n" +
		"  foo(); \n" +
		"}\n" +
		"public static void foo() {\n" +
		"  if(55f!=00000000000000000000055F)      // HERE VA/Java detects an unexpected error\n" +
		"  {\n" +
		"System.out.println(\"55f!=00000000000000000000055F\");\n" +
		"  }\n" +
		"  else\n" +
		"  {\n" +
		"System.out.println(\"55f==00000000000000000000055F\");\n" +
		"  }\n" +
		" }      \n" +
		"}\n",
	});
}

public void test002() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  public static void main (String args []) {\n" +
		"    foo();\n" +
		"  }\n" +
		"  public static void foo() {\n" +
		"    if(55f!=00000000000000000000055F)      // HERE VA/Java detects an unexpected error\n" +
		"      {\n" +
		"      System.out.println(\"55f!=00000000000000000000055F\");\n" +
		"    }\n" +
		"    else\n" +
		"    {\n" +
		"      System.out.println(\"55f==00000000000000000000055F\");\n" +
		"    }\n" +
		"  }      \n" +
		"}\n",
	});
}

public void test003() {
	this.runConformTest(new String[] {
		"p/Z.java",
		"package p;\n" +
		"public class Z {\n" +
		"  public static void main(String[] cargs) throws Exception {\n" +
		"    System.out.println(System.getProperty(\"java.vm.info\", \"J9\"));\n" +
		"    System.out.write((byte) 0x89);\n" +
		"    System.out.println();\n" +
		"    System.out.println(\"\u00E2?\u00B0\");\n" +
		"    System.out.println(Integer.toHexString(\"\u00E2?\u00B0\".charAt(0)));\n" +
		"  }\n" +
		"}\n",
	});
}

public void test004() {
	this.runConformTest(
		new String[] {
			"TempClassFormat.java",
			"/**\n" +
			" * Insert the type's description here.\n" +
			" * Creation date: (02/28/01 2:58:07 PM)\n" +
			" * @author: Administrator\n" +
			" */\n" +
			"public class TempClassFormat {\n" +
			"		// ERROR NUMBERS\n" +
			"\n" +
			"	// Blank field error numbers\n" +
			"	private static final String PERSON_ID_BLANK = \"2\";\n" +
			"	private static final String DEMOGRAPHIC_TYPE_BLANK = \"3\";\n" +
			"	private static final String EMPLOYEE_NUMBER_BLANK = \"23\";\n" +
			"	private static final String WORK_PHONE_AREA_CODE_BLANK = \"25\";\n" +
			"	private static final String WORK_PHONE1_BLANK = \"26\";\n" +
			"	private static final String WORK_PHONE2_BLANK = \"27\";\n" +
			"	private static final String WORK_ADDRESS1_BLANK = \"28\";\n" +
			"	private static final String WORK_CITY_BLANK = \"29\";\n" +
			"	private static final String WORK_STATE_BLANK = \"30\";\n" +
			"	private static final String WORK_ZIP5_BLANK = \"31\";\n" +
			"	private static final String BENEFITS_SALARY_BLANK = \"32\";\n" +
			"	private static final String TRUE_SALARY_BLANK = \"33\";\n" +
			"	private static final String PAY_FREQUENCY_BLANK = \"34\";\n" +
			"	private static final String WORK_HOURS_BLANK = \"35\";\n" +
			"	private static final String LOCATION_ID_BLANK = \"36\";\n" +
			"	private static final String SALARY_GRADE_BLANK = \"37\";\n" +
			"	private static final String DATE_OF_HIRE_BLANK = \"38\";\n" +
			"	private static final String RETIRE_VEST_PERCENT_BLANK = \"39\";\n" +
			"	private static final String JOB_CODE_BLANK = \"40\";\n" +
			"	private static final String UNION_FLAG_BLANK = \"41\";\n" +
			"	private static final String OFFICER_FLAG_BLANK = \"42\";\n" +
			"	private static final String PIN_USER_ID_BLANK = \"43\";\n" +
			"	private static final String VENDOR_EMPLOYEE_ID_BLANK = \"44\";\n" +
			"	private static final String MODIFIED_BY_BLANK = \"8\";\n" +
			"	private static final String MODIFIED_DATE_BLANK = \"9\";\n" +
			"	\n" +
			"	\n" +
			"	// Invalid field error numbers\n" +
			"	private static final String DEMOGRAPHIC_TYPE_INVALID = \"54\";\n" +
			"	private static final String EMPLOYER_ID_INVALID = \"22\";\n" +
			"	private static final String WORK_STATE_INVALID = \"70\";\n" +
			"	private static final String PAY_FREQUENCY_INVALID = \"138\";\n" +
			"	private static final String WORK_HOURS_TOO_SMALL = \"140\";\n" +
			"	private static final String DATE_OF_HIRE_INVALID = \"75\";\n" +
			"	private static final String DATE_OF_HIRE_AFTER_TODAY = \"137\";\n" +
			"	private static final String RETIRE_VEST_PERCENT_TOO_LARGE = \"77\";\n" +
			"	private static final String RETIRE_VEST_PERCENT_TOO_SMALL = \"139\";\n" +
			"	private static final String UNION_FLAG_INVALID = \"78\";\n" +
			"	private static final String OFFICER_FLAG_INVALID = \"79\";\n" +
			"	private static final String BENEFIT_GROUP_ID_INVALID = \"45\";\n" +
			"	private static final String LAST_PERSON_SEQ_NUMBER_INVALID = \"80\";\n" +
			"\n" +
			"	// Field not numeric error numbers\n" +
			"	private static final String WORK_PHONE_AREA_CODE_NOT_NUMERIC = \"67\";\n" +
			"	private static final String WORK_PHONE1_NOT_NUMERIC = \"68\";\n" +
			"	private static final String WORK_PHONE2_NOT_NUMERIC = \"69\";\n" +
			"	private static final String WORK_PHONE_EXTENSION_NOT_NUMERIC = \"109\";\n" +
			"	private static final String WORK_ZIP5_NOT_NUMERIC = \"71\";\n" +
			"	private static final String WORK_ZIP4_NOT_NUMERIC = \"46\";\n" +
			"	private static final String BENEFITS_SALARY_NOT_NUMERIC = \"72\";\n" +
			"	private static final String TRUE_SALARY_NOT_NUMERIC = \"73\";\n" +
			"	private static final String WORK_HOURS_NOT_NUMERIC = \"74\";\n" +
			"	private static final String RETIRE_VEST_PERCENT_NOT_NUMERIC = \"76\";\n" +
			"	\n" +
			"	// Field too short error numbers\n" +
			"	private static final String WORK_PHONE_AREA_CODE_TOO_SHORT = \"110\";\n" +
			"	private static final String WORK_PHONE1_TOO_SHORT = \"111\";\n" +
			"	private static final String WORK_PHONE2_TOO_SHORT = \"112\";\n" +
			"	private static final String WORK_STATE_TOO_SHORT = \"113\";\n" +
			"	private static final String WORK_ZIP5_TOO_SHORT = \"114\";\n" +
			"	private static final String WORK_ZIP4_TOO_SHORT = \"115\";\n" +
			"\n" +
			"	// Field too long error numbers\n" +
			"	private static final String PERSON_ID_TOO_LONG = \"82\";\n" +
			"	private static final String EMPLOYEE_NUMBER_TOO_LONG = \"116\";\n" +
			"	private static final String WORK_PHONE_AREA_CODE_TOO_LONG = \"117\";\n" +
			"	private static final String WORK_PHONE1_TOO_LONG = \"118\";\n" +
			"	private static final String WORK_PHONE2_TOO_LONG = \"119\";\n" +
			"	private static final String WORK_PHONE_EXTENSION_TOO_LONG = \"120\";\n" +
			"	private static final String WORK_ADDRESS1_TOO_LONG = \"121\";\n" +
			"	private static final String WORK_ADDRESS2_TOO_LONG = \"122\";\n" +
			"	private static final String WORK_CITY_TOO_LONG = \"123\";\n" +
			"	private static final String WORK_STATE_TOO_LONG = \"124\";\n" +
			"	private static final String WORK_ZIP5_TOO_LONG = \"125\";\n" +
			"	private static final String WORK_ZIP4_TOO_LONG = \"126\";\n" +
			"	private static final String BENEFITS_SALARY_TOO_LONG = \"127\";\n" +
			"	private static final String TRUE_SALARY_TOO_LONG = \"128\";\n" +
			"	private static final String WORK_HOURS_TOO_LONG = \"129\";\n" +
			"	private static final String LOCATION_ID_TOO_LONG = \"130\";\n" +
			"	private static final String SALARY_GRADE_TOO_LONG = \"131\";\n" +
			"	private static final String RETIRE_VEST_PERCENT_TOO_LONG = \"132\";\n" +
			"	private static final String JOB_CODE_TOO_LONG = \"133\";\n" +
			"	private static final String PIN_USER_ID_TOO_LONG = \"134\";\n" +
			"	private static final String VENDOR_EMPLOYEE_ID_TOO_LONG = \"135\";\n" +
			"	private static final String MODIFIED_BY_TOO_LONG = \"86\";\n" +
			"\n" +
			"	// Administrator approval error numbers\n" +
			"	private static final String EMPLOYER_ID_REQ_APPR = \"623\";\n" +
			"	private static final String EMPLOYEE_NUMBER_REQ_APPR = \"624\";\n" +
			"	private static final String STATUS_FLAG_REQ_APPR = \"625\";\n" +
			"	private static final String WORK_PHONE_AREA_CODE_REQ_APPR = \"626\";\n" +
			"	private static final String WORK_PHONE1_REQ_APPR = \"627\";\n" +
			"	private static final String WORK_PHONE2_REQ_APPR = \"628\";\n" +
			"	private static final String WORK_PHONE_EXTENSION_REQ_APPR = \"629\";\n" +
			"	private static final String WORK_ADDRESS1_REQ_APPR = \"630\";\n" +
			"	private static final String WORK_ADDRESS2_REQ_APPR = \"631\";\n" +
			"	private static final String WORK_CITY_REQ_APPR = \"632\";\n" +
			"	private static final String WORK_STATE_REQ_APPR = \"633\";\n" +
			"	private static final String WORK_ZIP5_REQ_APPR = \"634\";\n" +
			"	private static final String WORK_ZIP4_REQ_APPR = \"635\";\n" +
			"	private static final String BENEFITS_SALARY_REQ_APPR = \"636\";\n" +
			"	private static final String TRUE_SALARY_REQ_APPR = \"637\";\n" +
			"	private static final String PAY_FREQUENCY_REQ_APPR = \"638\";\n" +
			"	private static final String WORK_HOURS_REQ_APPR = \"639\";\n" +
			"	private static final String LOCATION_ID_REQ_APPR = \"640\";\n" +
			"	private static final String SALARY_GRADE_REQ_APPR = \"641\";\n" +
			"	private static final String DATE_OF_HIRE_REQ_APPR = \"642\";\n" +
			"	private static final String RETIRE_VEST_PERCENT_REQ_APPR = \"643\";\n" +
			"	private static final String JOB_CODE_REQ_APPR = \"644\";\n" +
			"	private static final String UNION_FLAG_REQ_APPR = \"645\";\n" +
			"	private static final String OFFICER_FLAG_REQ_APPR = \"646\";\n" +
			"	private static final String PIN_USER_ID_REQ_APPR = \"647\";\n" +
			"	private static final String VENDOR_EMPLOYEE_ID_REQ_APPR = \"648\";\n" +
			"	private static final String BENEFIT_GROUP_ID_REQ_APPR = \"649\";\n" +
			"	private static final String LAST_PERSON_SEQ_NBR_REQ_APPR = \"650\";\n" +
			"	\n" +
			"public static void main(String[] args) {\n" +
			"		System.out.println(\"Success\");\n" +
			"}\n" +
			"}"
		},
		"Success");
}

public void test005() {
	this.runConformTest(
		new String[] {
			"Code.java",
			"public class Code {\n" +
			"  public static final String s = \"<clinit>\";\n" +
			"  public static final String s2 = \"()V\";\n" +
			"  public Code(int i) {\n" +
			"  }\n" +
			"public static void main(String[] args) {\n" +
			"  System.out.print(s.length());\n" +
			"  System.out.println(s2.length());\n" +
			"}\n" +
			"}"
		},
		"83");
}

public void test006() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n" +
			"public class X {	\n" +
			"	X otherX;	\n" +
			"	static String STR = \"SUCCESS\";	\n" +
			"	public static void main(String args[]) {	\n" +
			"		try {	\n" +
			"			System.out.println(new X().otherX.STR);	\n" +
			"		} catch(NullPointerException e){	\n" +
			"			System.out.println(\"FAILED\");	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n",
		},
		"SUCCESS");
}

/*
 * null is not a constant
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=26585
 */
public void test007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"    public static final boolean F = false;	\n"+
			"    public static final String Str = F ? \"dummy\" : null;	\n"+
			"    public static void main(String[] args) {	\n"+
			"        if (Str == null)	\n"+
			"        	System.out.println(\"SUCCESS\");	\n"+
			"       	else	\n"+
			"        	System.out.println(\"FAILED\");	\n"+
			"    }	\n"+
			"}	\n",
		},
		"SUCCESS");
}

/*
 * null is not a constant
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=26138
 */
public void test008() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"    public static void main(String[] args) {	\n"+
			"      	System.out.println(\"SUCCESS\");	\n"+
			"	} 	\n"+
			"	void foo(){	\n"+
			"		while (null == null);	//not an inlinable constant\n"+
			"		System.out.println(\"unreachable but shouldn't be flagged\");	\n" +
			"	}	\n"+
			"}	\n",
		},
		"SUCCESS");
}

/*
 * null is not a constant
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=26138
 */
/*
 * null is not a constant
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=26138
 */
public void test009() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    public static void main(String[] args) {	\n" +
			"        if (null == null) System.out.print(\"1\");	\n" +
			"        if ((null==null ? null:null) == (null==null ? null:null))	\n" +
			"        	System.out.print(\"2\");	\n" +
			"		boolean b = (\"[\" + null + \"]\") == \"[null]\";  // cannot inline	\n" +
			"		System.out.print(\"3\");	\n" +
			"		final String s = (String) null;	\n" +
			"		if (s == null) System.out.print(\"4\");	\n" +
			"		final String s2 = (String) \"aaa\";	\n" +
			"		if (s2 == \"aaa\") System.out.println(\"5\");	\n" +
			"    }	\n" +
			"}",
		},
		"12345");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 3, Locals: 4\n" + 
		"  public static void main(java.lang.String[] args);\n" + 
		"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
		"     3  ldc <String \"1\"> [22]\n" + 
		"     5  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" + 
		"     8  aconst_null\n" + 
		"     9  goto 13\n" + 
		"    12  aconst_null\n" + 
		"    13  aconst_null\n" + 
		"    14  goto 18\n" + 
		"    17  aconst_null\n" + 
		"    18  if_acmpne 29\n" + 
		"    21  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
		"    24  ldc <String \"2\"> [30]\n" + 
		"    26  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" + 
		"    29  new java.lang.StringBuffer [32]\n" + 
		"    32  dup\n" + 
		"    33  ldc <String \"[\"> [34]\n" + 
		"    35  invokespecial java.lang.StringBuffer(java.lang.String) [36]\n" + 
		"    38  aconst_null\n" + 
		"    39  invokevirtual java.lang.StringBuffer.append(java.lang.Object) : java.lang.StringBuffer [38]\n" + 
		"    42  ldc <String \"]\"> [42]\n" + 
		"    44  invokevirtual java.lang.StringBuffer.append(java.lang.String) : java.lang.StringBuffer [44]\n" + 
		"    47  invokevirtual java.lang.StringBuffer.toString() : java.lang.String [47]\n" + 
		"    50  ldc <String \"[null]\"> [51]\n" + 
		"    52  if_acmpne 59\n" + 
		"    55  iconst_1\n" + 
		"    56  goto 60\n" + 
		"    59  iconst_0\n" + 
		"    60  istore_1 [b]\n" + 
		"    61  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
		"    64  ldc <String \"3\"> [53]\n" + 
		"    66  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" + 
		"    69  aconst_null\n" + 
		"    70  astore_2 [s]\n" + 
		"    71  aload_2 [s]\n" + 
		"    72  ifnonnull 83\n" + 
		"    75  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
		"    78  ldc <String \"4\"> [55]\n" + 
		"    80  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" + 
		"    83  ldc <String \"aaa\"> [57]\n" + 
		"    85  astore_3 [s2]\n" + 
		"    86  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
		"    89  ldc <String \"5\"> [59]\n" + 
		"    91  invokevirtual java.io.PrintStream.println(java.lang.String) : void [61]\n" + 
		"    94  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 3]\n" + 
		"        [pc: 8, line: 4]\n" + 
		"        [pc: 21, line: 5]\n" + 
		"        [pc: 29, line: 6]\n" + 
		"        [pc: 61, line: 7]\n" + 
		"        [pc: 69, line: 8]\n" + 
		"        [pc: 71, line: 9]\n" + 
		"        [pc: 83, line: 10]\n" + 
		"        [pc: 86, line: 11]\n" + 
		"        [pc: 94, line: 12]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 95] local: args index: 0 type: java.lang.String[]\n" + 
		"        [pc: 61, pc: 95] local: b index: 1 type: boolean\n" + 
		"        [pc: 71, pc: 95] local: s index: 2 type: java.lang.String\n" + 
		"        [pc: 86, pc: 95] local: s2 index: 3 type: java.lang.String\n";

	String expectedOutput15 =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 3, Locals: 4\n" + 
		"  public static void main(java.lang.String[] args);\n" + 
		"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
		"     3  ldc <String \"1\"> [22]\n" + 
		"     5  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" + 
		"     8  aconst_null\n" + 
		"     9  goto 13\n" + 
		"    12  aconst_null\n" + 
		"    13  aconst_null\n" + 
		"    14  goto 18\n" + 
		"    17  aconst_null\n" + 
		"    18  if_acmpne 29\n" + 
		"    21  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
		"    24  ldc <String \"2\"> [30]\n" + 
		"    26  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" + 
		"    29  new java.lang.StringBuilder [32]\n" + 
		"    32  dup\n" + 
		"    33  ldc <String \"[\"> [34]\n" + 
		"    35  invokespecial java.lang.StringBuilder(java.lang.String) [36]\n" + 
		"    38  aconst_null\n" + 
		"    39  invokevirtual java.lang.StringBuilder.append(java.lang.Object) : java.lang.StringBuilder [38]\n" + 
		"    42  ldc <String \"]\"> [42]\n" + 
		"    44  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [44]\n" + 
		"    47  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [47]\n" + 
		"    50  ldc <String \"[null]\"> [51]\n" + 
		"    52  if_acmpne 59\n" + 
		"    55  iconst_1\n" + 
		"    56  goto 60\n" + 
		"    59  iconst_0\n" + 
		"    60  istore_1 [b]\n" + 
		"    61  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
		"    64  ldc <String \"3\"> [53]\n" + 
		"    66  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" + 
		"    69  aconst_null\n" + 
		"    70  astore_2 [s]\n" + 
		"    71  aload_2 [s]\n" + 
		"    72  ifnonnull 83\n" + 
		"    75  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
		"    78  ldc <String \"4\"> [55]\n" + 
		"    80  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" + 
		"    83  ldc <String \"aaa\"> [57]\n" + 
		"    85  astore_3 [s2]\n" + 
		"    86  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
		"    89  ldc <String \"5\"> [59]\n" + 
		"    91  invokevirtual java.io.PrintStream.println(java.lang.String) : void [61]\n" + 
		"    94  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 3]\n" + 
		"        [pc: 8, line: 4]\n" + 
		"        [pc: 21, line: 5]\n" + 
		"        [pc: 29, line: 6]\n" + 
		"        [pc: 61, line: 7]\n" + 
		"        [pc: 69, line: 8]\n" + 
		"        [pc: 71, line: 9]\n" + 
		"        [pc: 83, line: 10]\n" + 
		"        [pc: 86, line: 11]\n" + 
		"        [pc: 94, line: 12]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 95] local: args index: 0 type: java.lang.String[]\n" + 
		"        [pc: 61, pc: 95] local: b index: 1 type: boolean\n" + 
		"        [pc: 71, pc: 95] local: s index: 2 type: java.lang.String\n" + 
		"        [pc: 86, pc: 95] local: s2 index: 3 type: java.lang.String\n";

	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		int index = actualOutput.indexOf(expectedOutput15);
		if (index == -1 || expectedOutput15.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput15, actualOutput);
		}
	} else {
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}
}

/*
 * null is not a constant
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=26138
 */
public void test010() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    public static void main(String[] args) {	\n" +
			"       if (null == null) {\n"+
			"			System.out.print(\"SUCCESS\");	\n" +
			"			return;	\n" +
			"		}	\n" +
			"		System.out.print(\"SHOULDN'T BE GENERATED\");	\n" +
			"    }	\n" +
			"}	\n",
		},
		"SUCCESS");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 1\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"     3  ldc <String \"SUCCESS\"> [22]\n" +
		"     5  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
		"     8  return\n" +
		"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    12  ldc <String \"SHOULDN\'T BE GENERATED\"> [30]\n" +
		"    14  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
		"    17  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 4]\n" +
		"        [pc: 8, line: 5]\n" +
		"        [pc: 9, line: 7]\n" +
		"        [pc: 17, line: 8]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 18] local: args index: 0 type: java.lang.String[]\n";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=30704
public void test011() {
	this.runConformTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"    public static void main(String[] args) {\n" +
			"		System.out.print((01.f == 1) && (01e0f == 1));	\n" +
			"    }\n" +
			"}",
		},
		"true");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79545
public void test012() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static String C = \"\" + +\' \';\n" +
			"    public static String I = \"\" + +32;\n" +
			"\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.print(C);\n" +
			"        System.out.print(I);\n" +
			"    }\n" +
			"}",
		},
		"3232");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=97190
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(-9223372036854775809L); // KO\n" +
			"		System.out.println(9223372036854775809L); // KO\n" +
			"		System.out.println(9223372036854775808L); // KO\n" +
			"		System.out.println(23092395825689123986L); // KO\n" +
			"		System.out.println(-9223372036854775808L); // OK\n" +
			"		System.out.println(9223372036854775807L); // OK\n" +
			"		System.out.println(2309239582568912398L); // OK\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	System.out.println(-9223372036854775809L); // KO\n" +
		"	                    ^^^^^^^^^^^^^^^^^^^^\n" +
		"The literal 9223372036854775809L of type long is out of range \n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	System.out.println(9223372036854775809L); // KO\n" +
		"	                   ^^^^^^^^^^^^^^^^^^^^\n" +
		"The literal 9223372036854775809L of type long is out of range \n" +
		"----------\n" +
		"3. ERROR in X.java (at line 5)\n" +
		"	System.out.println(9223372036854775808L); // KO\n" +
		"	                   ^^^^^^^^^^^^^^^^^^^^\n" +
		"The literal 9223372036854775808L of type long is out of range \n" +
		"----------\n" +
		"4. ERROR in X.java (at line 6)\n" +
		"	System.out.println(23092395825689123986L); // KO\n" +
		"	                   ^^^^^^^^^^^^^^^^^^^^^\n" +
		"The literal 23092395825689123986L of type long is out of range \n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110182
public void test014() throws Exception {
	if (this.complianceLevel > ClassFileConstants.JDK1_5) return;
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	X fx;\n" +
			"	final static boolean DBG = false;\n" +
			"	void foo1(X x) {\n" +
			"		if (x.DBG) {\n" +
			"			boolean b = x.DBG;\n" +
			"		}\n" +
			"		boolean bb;\n" +
			"		if (bb = x.DBG) {\n" +
			"			boolean b = x.DBG;\n" +
			"		}\n" +
			"	}\n" +
			"	void foo2(X x) {\n" +
			"		while (x.DBG) {\n" +
			"			boolean b = x.DBG;\n" +
			"		}\n" +
			"	}\n" +
			"	void foo3(X x) {\n" +
			"		for (;x.DBG;) {\n" +
			"			boolean b = x.DBG;\n" +
			"		}\n" +
			"	}\n" +
			"	void foo4(X x) {\n" +
			"		boolean b = x.DBG ? x == null : x.DBG;\n" +
			"	}\n" +
			"	void foo5() {\n" +
			"		if (this.fx.DBG) {\n" +
			"			boolean b = this.fx.DBG;\n" +
			"		}\n" +
			"	}\n" +
			"	void foo6() {\n" +
			"		while (this.fx.DBG) {\n" +
			"			boolean b = this.fx.DBG;\n" +
			"		}\n" +
			"	}\n" +
			"	void foo7() {\n" +
			"		for (;this.fx.DBG;) {\n" +
			"			boolean b = this.fx.DBG;\n" +
			"		}\n" +
			"	}\n" +
			"	void foo8() {\n" +
			"		boolean b = this.fx.DBG ? this.fx == null : this.fx.DBG;\n" +
			"	}\n" +
			"}\n",
		},
		"");
	// ensure boolean codegen got optimized (optimizedBooleanConstant)
	String expectedOutput =
		"  // Method descriptor #20 (LX;)V\n" +
		"  // Stack: 2, Locals: 4\n" +
		"  void foo1(X x);\n" +
		"    0  iconst_0\n" +
		"    1  dup\n" +
		"    2  istore_2 [bb]\n" +
		"    3  ifeq 8\n" +
		"    6  iconst_0\n" +
		"    7  istore_3\n" +
		"    8  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 9]\n" +
		"        [pc: 6, line: 10]\n" +
		"        [pc: 8, line: 12]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 9] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 9] local: x index: 1 type: X\n" +
		"        [pc: 3, pc: 9] local: bb index: 2 type: boolean\n" +
		"  \n" +
		"  // Method descriptor #20 (LX;)V\n" +
		"  // Stack: 0, Locals: 2\n" +
		"  void foo2(X x);\n" +
		"    0  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 17]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 1] local: x index: 1 type: X\n" +
		"  \n" +
		"  // Method descriptor #20 (LX;)V\n" +
		"  // Stack: 0, Locals: 2\n" +
		"  void foo3(X x);\n" +
		"    0  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 22]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 1] local: x index: 1 type: X\n" +
		"  \n" +
		"  // Method descriptor #20 (LX;)V\n" +
		"  // Stack: 1, Locals: 3\n" +
		"  void foo4(X x);\n" +
		"    0  iconst_0\n" +
		"    1  istore_2 [b]\n" +
		"    2  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 24]\n" +
		"        [pc: 2, line: 25]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 3] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 3] local: x index: 1 type: X\n" +
		"        [pc: 2, pc: 3] local: b index: 2 type: boolean\n" +
		"  \n" +
		"  // Method descriptor #12 ()V\n" +
		"  // Stack: 0, Locals: 1\n" +
		"  void foo5();\n" +
		"    0  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 30]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
		"  \n" +
		"  // Method descriptor #12 ()V\n" +
		"  // Stack: 0, Locals: 1\n" +
		"  void foo6();\n" +
		"    0  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 35]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
		"  \n" +
		"  // Method descriptor #12 ()V\n" +
		"  // Stack: 0, Locals: 1\n" +
		"  void foo7();\n" +
		"    0  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 40]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
		"  \n" +
		"  // Method descriptor #12 ()V\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  void foo8();\n" +
		"    0  iconst_0\n" +
		"    1  istore_1 [b]\n" +
		"    2  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 42]\n" +
		"        [pc: 2, line: 43]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 3] local: this index: 0 type: X\n" +
		"        [pc: 2, pc: 3] local: b index: 1 type: boolean\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110182 - variation
public void test015() throws Exception {
	if(this.complianceLevel > ClassFileConstants.JDK1_5) return;
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	X fx;\n" +
			"	final static boolean DBG = false;\n" +
			"	void foo1(X x) {\n" +
			"		if (x.DBG) {\n" +
			"			boolean b = x.DBG;\n" +
			"		}\n" +
			"		boolean bb;\n" +
			"		if (bb = x.DBG) {\n" +
			"			boolean b = x.DBG;\n" +
			"		}\n" +
			"	}\n" +
			"	void foo2(X x) {\n" +
			"		while (x.DBG) {\n" +
			"			boolean b = x.DBG;\n" +
			"		}\n" +
			"	}\n" +
			"	void foo3(X x) {\n" +
			"		for (;x.DBG;) {\n" +
			"			boolean b = x.DBG;\n" +
			"		}\n" +
			"	}\n" +
			"	void foo4(X x) {\n" +
			"		boolean b = x.DBG ? x == null : x.DBG;\n" +
			"	}\n" +
			"	void foo5() {\n" +
			"		if (this.fx.DBG) {\n" +
			"			boolean b = this.fx.DBG;\n" +
			"		}\n" +
			"	}\n" +
			"	void foo6() {\n" +
			"		while (this.fx.DBG) {\n" +
			"			boolean b = this.fx.DBG;\n" +
			"		}\n" +
			"	}\n" +
			"	void foo7() {\n" +
			"		for (;this.fx.DBG;) {\n" +
			"			boolean b = this.fx.DBG;\n" +
			"		}\n" +
			"	}\n" +
			"	void foo8() {\n" +
			"		boolean b = this.fx.DBG ? this.fx == null : this.fx.DBG;\n" +
			"	}\n" +
			"}\n",
		},
		"");
	// ensure boolean codegen got optimized (optimizedBooleanConstant)
	String expectedOutput =
		"  // Method descriptor #20 (LX;)V\n" +
		"  // Stack: 2, Locals: 4\n" +
		"  void foo1(X x);\n" +
		"    0  iconst_0\n" +
		"    1  dup\n" +
		"    2  istore_2 [bb]\n" +
		"    3  ifeq 8\n" +
		"    6  iconst_0\n" +
		"    7  istore_3\n" +
		"    8  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 9]\n" +
		"        [pc: 6, line: 10]\n" +
		"        [pc: 8, line: 12]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 9] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 9] local: x index: 1 type: X\n" +
		"        [pc: 3, pc: 9] local: bb index: 2 type: boolean\n" +
		"  \n" +
		"  // Method descriptor #20 (LX;)V\n" +
		"  // Stack: 0, Locals: 2\n" +
		"  void foo2(X x);\n" +
		"    0  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 17]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 1] local: x index: 1 type: X\n" +
		"  \n" +
		"  // Method descriptor #20 (LX;)V\n" +
		"  // Stack: 0, Locals: 2\n" +
		"  void foo3(X x);\n" +
		"    0  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 22]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 1] local: x index: 1 type: X\n" +
		"  \n" +
		"  // Method descriptor #20 (LX;)V\n" +
		"  // Stack: 1, Locals: 3\n" +
		"  void foo4(X x);\n" +
		"    0  iconst_0\n" +
		"    1  istore_2 [b]\n" +
		"    2  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 24]\n" +
		"        [pc: 2, line: 25]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 3] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 3] local: x index: 1 type: X\n" +
		"        [pc: 2, pc: 3] local: b index: 2 type: boolean\n" +
		"  \n" +
		"  // Method descriptor #12 ()V\n" +
		"  // Stack: 0, Locals: 1\n" +
		"  void foo5();\n" +
		"    0  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 30]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
		"  \n" +
		"  // Method descriptor #12 ()V\n" +
		"  // Stack: 0, Locals: 1\n" +
		"  void foo6();\n" +
		"    0  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 35]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
		"  \n" +
		"  // Method descriptor #12 ()V\n" +
		"  // Stack: 0, Locals: 1\n" +
		"  void foo7();\n" +
		"    0  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 40]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
		"  \n" +
		"  // Method descriptor #12 ()V\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  void foo8();\n" +
		"    0  iconst_0\n" +
		"    1  istore_1 [b]\n" +
		"    2  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 42]\n" +
		"        [pc: 2, line: 43]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 3] local: this index: 0 type: X\n" +
		"        [pc: 2, pc: 3] local: b index: 1 type: boolean\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110182 - variation
public void test016() throws Exception {
	if(this.complianceLevel > ClassFileConstants.JDK1_5) return;
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	X fx;\n" +
			"	final static boolean DBG = false;\n" +
			"	void foo1(X x) {\n" +
			"		boolean b;\n" +
			"		if (false ? false : x.DBG) {\n" +
			"			boolean bb = x.DBG;\n" +
			"		}\n" +
			"	}\n" +
			"	void foo2(X x) {\n" +
			"		boolean b;\n" +
			"		while (x == null ? x.DBG : x.DBG) {\n" +
			"			boolean bb = x.DBG;\n" +
			"		}\n" +
			"	}\n" +
			"	void foo3(X x) {\n" +
			"		boolean b;\n" +
			"		for (;x == null ? x.DBG : x.DBG;) {\n" +
			"			boolean bb = x.DBG;\n" +
			"		}\n" +
			"	}\n" +
			"	void foo4(X x) {\n" +
			"		boolean bb = (x == null ? x.DBG :  x.DBG) ? x == null : x.DBG;\n" +
			"	}\n" +
			"}\n",
		},
		"");
	// ensure boolean codegen got optimized (optimizedBooleanConstant)
	String expectedOutput =
		"  // Method descriptor #20 (LX;)V\n" +
		"  // Stack: 0, Locals: 2\n" +
		"  void foo1(X x);\n" +
		"    0  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 9]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 1] local: x index: 1 type: X\n" +
		"  \n" +
		"  // Method descriptor #20 (LX;)V\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  void foo2(X x);\n" +
		"    0  aload_1 [x]\n" +
		"    1  ifnonnull 4\n" +
		"    4  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 12]\n" +
		"        [pc: 4, line: 15]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 5] local: x index: 1 type: X\n" +
		"  \n" +
		"  // Method descriptor #20 (LX;)V\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  void foo3(X x);\n" +
		"    0  aload_1 [x]\n" +
		"    1  ifnonnull 4\n" +
		"    4  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 18]\n" +
		"        [pc: 4, line: 21]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 5] local: x index: 1 type: X\n" +
		"  \n" +
		"  // Method descriptor #20 (LX;)V\n" +
		"  // Stack: 1, Locals: 3\n" +
		"  void foo4(X x);\n" +
		"    0  aload_1 [x]\n" +
		"    1  ifnonnull 4\n" +
		"    4  iconst_0\n" +
		"    5  istore_2 [bb]\n" +
		"    6  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 23]\n" +
		"        [pc: 6, line: 24]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 7] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 7] local: x index: 1 type: X\n" +
		"        [pc: 6, pc: 7] local: bb index: 2 type: boolean\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117495
public void test017() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"		int x = 2;\n" +
			"       System.out.println(\"n: \"+(x > 1  ? 2 : 1.0));\n" +
			"    }\n" +
			"}",
		},
		"n: 2.0");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117495
public void test018() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"		System.out.println(\"n: \"+(true ? 2 : 1.0));\n" +
			"    }\n" +
			"}",
		},
		"n: 2.0");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=154822
// null is not a constant - again
public void test019() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    static class Enclosed {\n" +
			"		 static final String constant = \"\";\n" +
			"		 static final String notAConstant;\n" +
			"        static {\n" +
			"		     notAConstant = null;\n" +
			"        }\n" +
			"    }\n" +
			"}",
		},
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=154822
// null is not a constant - again
public void test020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    class Inner {\n" +
			"		 static final String constant = \"\";\n" +
			"		 static final String notAConstant = null;\n" +
			"    }\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	static final String notAConstant = null;\n" +
		"	                    ^^^^^^^^^^^^\n" +
		"The field notAConstant cannot be declared static in a non-static inner type, unless initialized with a constant expression\n" +
		"----------\n");
}
public void testAllConstants() {
	Constant byteConstant = ByteConstant.fromValue((byte) 1);
	Constant byteConstant2 = ByteConstant.fromValue((byte) 2);
	Constant byteConstant3 = ByteConstant.fromValue((byte) 1);
	Constant charConstant = CharConstant.fromValue('c');
	Constant charConstant2 = CharConstant.fromValue('d');
	Constant charConstant3 = CharConstant.fromValue('c');
	Constant booleanConstant = BooleanConstant.fromValue(true);
	Constant booleanConstant2 = BooleanConstant.fromValue(false);
	Constant booleanConstant3 = BooleanConstant.fromValue(true);
	Constant doubleConstant = DoubleConstant.fromValue(1.0);
	Constant doubleConstant2 = DoubleConstant.fromValue(2.0);
	Constant doubleConstant3 = DoubleConstant.fromValue(1.0);
	Constant floatConstant = FloatConstant.fromValue(1.0f);
	Constant floatConstant2 =  FloatConstant.fromValue(2.0f);
	Constant floatConstant3 =  FloatConstant.fromValue(1.0f);
	Constant intConstant = IntConstant.fromValue(20);
	Constant intConstant2 = IntConstant.fromValue(30);
	Constant intConstant3 = IntConstant.fromValue(20);
	Constant longConstant =  LongConstant.fromValue(3L);
	Constant longConstant2 =  LongConstant.fromValue(4L);
	Constant longConstant3 =  LongConstant.fromValue(3L);
	Constant shortConstant = ShortConstant.fromValue((short) 4);
	Constant shortConstant2 = ShortConstant.fromValue((short) 3);
	Constant shortConstant3 = ShortConstant.fromValue((short) 4);
	Constant stringConstant = StringConstant.fromValue("test");
	Constant stringConstant2 = StringConstant.fromValue("test2");
	Constant stringConstant3 = StringConstant.fromValue("test");
	Constant stringConstant4 = StringConstant.fromValue(null);
	Constant stringConstant5 = StringConstant.fromValue(null);
	ClassSignature classSignature = new ClassSignature("java.lang.Object".toCharArray());
	ClassSignature classSignature2 = new ClassSignature("java.lang.String".toCharArray());
	ClassSignature classSignature3 = new ClassSignature("java.lang.Object".toCharArray());
	EnumConstantSignature enumConstantSignature = new EnumConstantSignature("myEnum".toCharArray(), "C".toCharArray());
	EnumConstantSignature enumConstantSignature2 = new EnumConstantSignature("myEnum".toCharArray(), "A".toCharArray());
	EnumConstantSignature enumConstantSignature3 = new EnumConstantSignature("myEnum".toCharArray(), "C".toCharArray());
	EnumConstantSignature enumConstantSignature4 = new EnumConstantSignature("myEnum2".toCharArray(), "A".toCharArray());

	verifyConstantEqualsAndHashcode(byteConstant, byteConstant2, byteConstant3, intConstant);
	verifyConstantEqualsAndHashcode(charConstant, charConstant2, charConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(booleanConstant, booleanConstant2, booleanConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(doubleConstant, doubleConstant2, doubleConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(floatConstant, floatConstant2, floatConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(intConstant, intConstant2, intConstant3, stringConstant);
	verifyConstantEqualsAndHashcode(longConstant, longConstant2, longConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(shortConstant, shortConstant2, shortConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(stringConstant, stringConstant2, stringConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(stringConstant, stringConstant4, stringConstant3, byteConstant);
	verifyConstantEqualsAndHashcode(stringConstant4, stringConstant3, stringConstant5, byteConstant);
	verifyConstantEqualsAndHashcode(classSignature, classSignature2, classSignature3, byteConstant);
	verifyConstantEqualsAndHashcode(enumConstantSignature, enumConstantSignature2, enumConstantSignature3, byteConstant);
	verifyConstantEqualsAndHashcode(enumConstantSignature, enumConstantSignature4, enumConstantSignature3, byteConstant);
	assertNotNull(Constant.NotAConstant.toString());
	
	verifyValues(byteConstant, charConstant, booleanConstant, doubleConstant, floatConstant, intConstant, longConstant, shortConstant, stringConstant);
	// check equals between to null string constants
	assertTrue(stringConstant4.equals(stringConstant5));
}
private void verifyValues(
		Constant byteConstant,
		Constant charConstant,
		Constant booleanConstant,
		Constant doubleConstant,
		Constant floatConstant,
		Constant intConstant,
		Constant longConstant,
		Constant shortConstant,
		Constant stringConstant) {

	// byteValue()
	byteConstant.byteValue();
	charConstant.byteValue();
	try {
		booleanConstant.byteValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	doubleConstant.byteValue();
	floatConstant.byteValue();
	intConstant.byteValue();
	longConstant.byteValue();
	shortConstant.byteValue();
	try {
		stringConstant.byteValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// booleanValue()
	try {
		byteConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	try {
		charConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	booleanConstant.booleanValue();
	try {
		doubleConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	try {
		floatConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	try {
		intConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	try {
		longConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	try {
		shortConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	try {
		stringConstant.booleanValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// charValue()
	byteConstant.charValue();
	charConstant.charValue();
	try {
		booleanConstant.charValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	doubleConstant.charValue();
	floatConstant.charValue();
	intConstant.charValue();
	longConstant.charValue();
	shortConstant.charValue();
	try {
		stringConstant.charValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// doubleValue()
	byteConstant.doubleValue();
	charConstant.doubleValue();
	try {
		booleanConstant.doubleValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	doubleConstant.doubleValue();
	floatConstant.doubleValue();
	intConstant.doubleValue();
	longConstant.doubleValue();
	shortConstant.doubleValue();
	try {
		stringConstant.doubleValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// floatValue()
	byteConstant.floatValue();
	charConstant.floatValue();
	try {
		booleanConstant.floatValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	doubleConstant.floatValue();
	floatConstant.floatValue();
	intConstant.floatValue();
	longConstant.floatValue();
	shortConstant.floatValue();
	try {
		stringConstant.floatValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// intValue()
	byteConstant.intValue();
	charConstant.intValue();
	try {
		booleanConstant.intValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	doubleConstant.intValue();
	floatConstant.intValue();
	intConstant.intValue();
	longConstant.intValue();
	shortConstant.intValue();
	try {
		stringConstant.intValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// longValue()
	byteConstant.longValue();
	charConstant.longValue();
	try {
		booleanConstant.longValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	doubleConstant.longValue();
	floatConstant.longValue();
	intConstant.longValue();
	longConstant.longValue();
	shortConstant.longValue();
	try {
		stringConstant.longValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// shortValue()
	byteConstant.shortValue();
	charConstant.shortValue();
	try {
		booleanConstant.shortValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}
	doubleConstant.shortValue();
	floatConstant.shortValue();
	intConstant.shortValue();
	longConstant.shortValue();
	shortConstant.shortValue();
	try {
		stringConstant.shortValue();
		assertTrue(false);
	} catch(ShouldNotImplement e) {
		// ignore
	}

	// stringValue()
	byteConstant.stringValue();
	charConstant.stringValue();
	booleanConstant.stringValue();
	doubleConstant.stringValue();
	floatConstant.stringValue();
	intConstant.stringValue();
	longConstant.stringValue();
	shortConstant.stringValue();
	stringConstant.stringValue();
}
private void verifyConstantEqualsAndHashcode(
		Object o,
		Object o2,
		Object o3,
		Object o4) {
	assertTrue(o.equals(o));
	assertTrue(o.equals(o3));
	assertFalse(o.equals(o2));
	assertFalse(o.equals(o4));
	assertFalse(o.equals(null));
	assertFalse(o.hashCode() == o2.hashCode());
	assertNotNull(o.toString());
	
	if (o instanceof Constant) {
		assertTrue("Not the same values", ((Constant) o).hasSameValue((Constant) o3));
		assertFalse("Have same values", ((Constant) o).hasSameValue((Constant) o2));
		assertFalse("Have same values", ((Constant) o).hasSameValue((Constant) o4));
	}
}
//test corner values (max, min, -1) for longs
public void test021() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0x0L); // OK\n" +
			"		System.out.println(0x8000000000000000L); // OK\n" +
			"		System.out.println(0x8000000000000000l); // OK\n" +
			"		System.out.println(01000000000000000000000L); // OK\n" +
			"		System.out.println(01000000000000000000000l); // OK\n" +
			"		System.out.println(-9223372036854775808L); // OK\n" +
			"		System.out.println(-9223372036854775808l); // OK\n" +
			"		System.out.println(0x7fffffffffffffffL); // OK\n" +
			"		System.out.println(0x7fffffffffffffffl); // OK\n" +
			"		System.out.println(0777777777777777777777L); // OK\n" +
			"		System.out.println(0777777777777777777777l); // OK\n" +
			"		System.out.println(9223372036854775807L); // OK\n" +
			"		System.out.println(9223372036854775807l); // OK\n" +
			"		System.out.println(0xffffffffffffffffL); // OK\n" +
			"		System.out.println(0x0000000000000ffffffffffffffffL); // OK\n" +
			"		System.out.println(0xffffffffffffffffl); // OK\n" +
			"		System.out.println(01777777777777777777777L); // OK\n" +
			"		System.out.println(01777777777777777777777l); // OK\n" +
			"		System.out.println(-0x1L); // OK\n" +
			"		System.out.println(-0x1l); // OK\n" +
			"		System.out.println(0677777777777777777777L);\n" +
			"		System.out.println(0677777777777777777777l);\n" +
			"		System.out.println(0x0000000000000L); // OK\n" +
			"		System.out.println(0L); // OK\n" +
			"	}\n" +
			"}",
		},
		"0\n" + 
		"-9223372036854775808\n" + 
		"-9223372036854775808\n" + 
		"-9223372036854775808\n" + 
		"-9223372036854775808\n" + 
		"-9223372036854775808\n" + 
		"-9223372036854775808\n" + 
		"9223372036854775807\n" + 
		"9223372036854775807\n" + 
		"9223372036854775807\n" + 
		"9223372036854775807\n" + 
		"9223372036854775807\n" + 
		"9223372036854775807\n" + 
		"-1\n" + 
		"-1\n" + 
		"-1\n" + 
		"-1\n" + 
		"-1\n" + 
		"-1\n" + 
		"-1\n" + 
		"8070450532247928831\n" + 
		"8070450532247928831\n" +
		"0\n" +
		"0");
}
//test corner values (max, min, -1) for ints
public void test022() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0x0); // OK\n" +
			"		System.out.println(0x80000000); // OK\n" +
			"		System.out.println(020000000000); // OK\n" +
			"		System.out.println(-2147483648); // OK\n" +
			"		System.out.println(0x7fffffff); // OK\n" +
			"		System.out.println(017777777777); // OK\n" +
			"		System.out.println(2147483647); // OK\n" +
			"		System.out.println(0xffffffff); // OK\n" +
			"		System.out.println(0x0000000000000ffffffff); // OK\n" +
			"		System.out.println(037777777777); // OK\n" +
			"		System.out.println(-0x1); // OK\n" +
			"		System.out.println(0xDADACAFE);\n" + 
			"		System.out.println(0x0000000000000); // OK\n" +
			"	}\n" +
			"}",
		},
		"0\n" + 
		"-2147483648\n" + 
		"-2147483648\n" + 
		"-2147483648\n" + 
		"2147483647\n" + 
		"2147483647\n" + 
		"2147483647\n" + 
		"-1\n" + 
		"-1\n" + 
		"-1\n" + 
		"-1\n" +
		"-623195394\n" +
		"0");
}
public static Class testClass() {
	return ConstantTest.class;
}
}
