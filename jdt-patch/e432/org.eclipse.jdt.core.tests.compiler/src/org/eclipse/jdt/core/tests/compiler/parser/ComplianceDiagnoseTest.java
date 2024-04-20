/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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
 *     Jesper S Moller - realigned with bug 399695
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import junit.framework.Test;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

@SuppressWarnings({ "rawtypes" })
public class ComplianceDiagnoseTest extends AbstractRegressionTest {
	public ComplianceDiagnoseTest(String name) {
		super(name);
	}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test0042" };
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 21, 50 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
public static Class testClass() {
	return ComplianceDiagnoseTest.class;
}
public void runComplianceParserTest(
	String[] testFiles,
	String expected13ProblemLog,
	String expected14ProblemLog,
	String expected15ProblemLog){
	if(this.complianceLevel == ClassFileConstants.JDK1_3) {
		this.runNegativeTest(testFiles, expected13ProblemLog);
	} else if(this.complianceLevel == ClassFileConstants.JDK1_4) {
		this.runNegativeTest(testFiles, expected14ProblemLog);
	} else if(this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(testFiles, expected15ProblemLog);
	}
}
public void runComplianceParserTest(
		String[] testFiles,
		String expected13ProblemLog,
		String expected14ProblemLog,
		String expected15ProblemLog,
		String expected17ProblemLog){
		if(this.complianceLevel == ClassFileConstants.JDK1_3) {
			this.runNegativeTest(testFiles, expected13ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_4) {
			this.runNegativeTest(testFiles, expected14ProblemLog);
		} else if(this.complianceLevel < ClassFileConstants.JDK1_7) {
			this.runNegativeTest(testFiles, expected15ProblemLog);
		} else {
			this.runNegativeTest(testFiles, expected17ProblemLog);
		}
	}
public void runComplianceParserTest(
		String[] testFiles,
		String expected13ProblemLog,
		String expected14ProblemLog,
		String expected15ProblemLog,
		String expected16ProblemLog,
		String expected17ProblemLog){
		if (this.complianceLevel == ClassFileConstants.JDK1_3) {
			this.runNegativeTest(testFiles, expected13ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_4) {
			this.runNegativeTest(testFiles, expected14ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_5) {
			this.runNegativeTest(testFiles, expected15ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_6) {
			this.runNegativeTest(testFiles, expected16ProblemLog);
		} else if(this.complianceLevel < ClassFileConstants.JDK1_8) {
			this.runNegativeTest(testFiles, expected17ProblemLog);
		}
	}
public void runComplianceParserTest(
		String[] testFiles,
		String expected13ProblemLog,
		String expected14ProblemLog,
		String expected15ProblemLog,
		String expected16ProblemLog,
		String expected17ProblemLog,
		String expected18ProblemLog){
		if (this.complianceLevel == ClassFileConstants.JDK1_3) {
			this.runNegativeTest(testFiles, expected13ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_4) {
			this.runNegativeTest(testFiles, expected14ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_5) {
			this.runNegativeTest(testFiles, expected15ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_6) {
			this.runNegativeTest(testFiles, expected16ProblemLog);
		} else if(this.complianceLevel < ClassFileConstants.JDK1_8) {
			this.runNegativeTest(testFiles, expected17ProblemLog);
		} else {
			this.runNegativeTest(testFiles, expected18ProblemLog);
		}
	}
public void runComplianceParserTest(
		String[] testFiles,
		String expected1_3ProblemLog,
		String expected1_4ProblemLog,
		String expected1_5ProblemLog,
		String expected1_6ProblemLog,
		String expected1_7ProblemLog,
		String expected1_8ProblemLog,
		String expected9ProblemLog,
		String expected10ProblemLog,
		String expected11ProblemLog,
		String expected12ProblemLog,
		String expected13ProblemLog,
		String expected14ProblemLog,
		String expected15ProblemLog,
		String below16ProblemLog,
		String expected16ProblemLog,
		String expected17ProblemLog,
		String expected18ProblemLog,
		String expected19ProblemLog,
		String expected20ProblemLog,
		String expected22ProblemLog
		){
		if (this.complianceLevel == ClassFileConstants.JDK1_3) {
			this.runNegativeTest(testFiles, expected1_3ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_4) {
			this.runNegativeTest(testFiles, expected1_4ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_5) {
			this.runNegativeTest(testFiles, expected1_5ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_6) {
			this.runNegativeTest(testFiles, expected1_6ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_7) {
			this.runNegativeTest(testFiles, expected1_7ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_8) {
			this.runNegativeTest(testFiles, expected1_8ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK9) {
			this.runNegativeTest(testFiles, expected9ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK10) {
			this.runNegativeTest(testFiles, expected10ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK11) {
			this.runNegativeTest(testFiles, expected11ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK12) {
			this.runNegativeTest(testFiles, expected12ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK13) {
			this.runNegativeTest(testFiles, expected13ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK14) {
			this.runNegativeTest(testFiles, expected14ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK15) {
			this.runNegativeTest(testFiles, expected15ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK16) {
			this.runNegativeTest(testFiles, expected16ProblemLog);
		} else if (this.complianceLevel == ClassFileConstants.JDK17) {
			this.runNegativeTest(testFiles, expected17ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK18) {
			this.runNegativeTest(testFiles, expected18ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK19) {
			this.runNegativeTest(testFiles, expected19ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK20) {
			this.runNegativeTest(testFiles, expected20ProblemLog);
		} else {
			this.runNegativeTest(testFiles, expected22ProblemLog);
		}
	}
public void test0001() {
	String[] testFiles = new String[] {
		"X.java",
		"import static aaa.BBB.*;\n" +
		"public class X {\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import static aaa.BBB.*;\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error, static imports are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	import static aaa.BBB.*;\n" +
		"	              ^^^\n" +
		"The import aaa cannot be resolved\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import static aaa.BBB.*;\n" +
		"	              ^^^\n" +
		"The import aaa cannot be resolved\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0002() {
	String[] testFiles = new String[] {
		"X.java",
		"import static aaa.BBB.CCC;\n" +
		"public class X {\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import static aaa.BBB.CCC;\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error, static imports are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	import static aaa.BBB.CCC;\n" +
		"	              ^^^\n" +
		"The import aaa cannot be resolved\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import static aaa.BBB.CCC;\n" +
		"	              ^^^\n" +
		"The import aaa cannot be resolved\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
// TODO: Fix this and Enable
public void test0003() {
	String[] testFiles = new String[] {
		"x/X.java", """
			package x;
			public enum X {
			}
			"""
	};

	String expected13ProblemLog = """
		----------
		1. ERROR in x\\X.java (at line 2)
			public enum X {
			       ^^^^
		Syntax error on token "enum", class expected
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0004() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(){\n" +
		"		for(String o: c) {\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	for(String o: c) {\n" +
		"	    ^^^^^^^^^^^\n" +
		"Syntax error, \'for each\' statements are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	for(String o: c) {\n" +
		"	              ^\n" +
		"c cannot be resolved to a variable\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	for(String o: c) {\n" +
		"	              ^\n" +
		"c cannot be resolved to a variable\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0005() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(Z ... arg){\n" +
		"	}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	void foo(Z ... arg){\n" +
		"	         ^^^^^^^^^\n" +
		"Syntax error, varargs are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void foo(Z ... arg){\n" +
		"	         ^\n" +
		"Z cannot be resolved to a type\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	void foo(Z ... arg){\n" +
		"	         ^\n" +
		"Z cannot be resolved to a type\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0006() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X <T1 extends String, T2> extends Y {\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X <T1 extends String, T2> extends Y {\n" +
		"	                ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X <T1 extends String, T2> extends Y {\n" +
		"	                                               ^\n" +
		"Y cannot be resolved to a type\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public class X <T1 extends String, T2> extends Y {\n" +
		"	                           ^^^^^^\n" +
		"The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X <T1 extends String, T2> extends Y {\n" +
		"	                                               ^\n" +
		"Y cannot be resolved to a type\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0007() {
	String[] testFiles = new String[] {
		"X.java",
		"public interface X <T1 extends String, T2> extends Y {\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public interface X <T1 extends String, T2> extends Y {\n" +
		"	                    ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public interface X <T1 extends String, T2> extends Y {\n" +
		"	                                                   ^\n" +
		"Y cannot be resolved to a type\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public interface X <T1 extends String, T2> extends Y {\n" +
		"	                               ^^^^^^\n" +
		"The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public interface X <T1 extends String, T2> extends Y {\n" +
		"	                                                   ^\n" +
		"Y cannot be resolved to a type\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0008() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public <T1 extends String, T2> int foo(){\n" +
		"	}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public <T1 extends String, T2> int foo(){\n" +
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public <T1 extends String, T2> int foo(){\n" +
		"	                   ^^^^^^\n" +
		"The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	public <T1 extends String, T2> int foo(){\n" +
		"	                                   ^^^^^\n" +
		"This method must return a result of type int\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0009() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public <T1 extends String, T2> X(){\n" +
		"	}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public <T1 extends String, T2> X(){\n" +
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
		"----------\n";

	String expected15ProblemLog =
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public <T1 extends String, T2> X(){\n" +
		"	                   ^^^^^^\n" +
		"The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended\n" +
		"----------\n";

	if(this.complianceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(testFiles, expected13ProblemLog);
	} else {
		runConformTest(
			true,
			testFiles,
			expected15ProblemLog,
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	}
}

public void testPatternsInCase() {
	String[] testFiles = new String[] {
		"X.java",
		"""
		public class X {
		    public static void main(String [] args) {
		        Object o = null;
		        switch (o) {
		            case X x, null:
		                break;
		            case String s, default :
		               break;
		        }
		    }
	    }
		"""
	};

	String expectedProblemLogFrom1_1_6 =
					"----------\n" +
					"1. ERROR in X.java (at line 4)\n" +
					"	switch (o) {\n" +
					"	        ^\n" +
					"Cannot switch on a value of type Object. Only convertible int values or enum variables are permitted\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 5)\n" +
					"	case X x, null:\n" +
					"	^^^^^^^^^^^^^^\n" +
					"Multi-constant case labels supported from Java 14 onwards only\n" +
					"----------\n" +
					"3. ERROR in X.java (at line 5)\n" +
					"	case X x, null:\n" +
					"	     ^^^\n" +
					"The Java feature 'Type Patterns' is only available with source level 16 and above\n" +
					"----------\n" +
					"4. ERROR in X.java (at line 5)\n" +
					"	case X x, null:\n" +
					"	     ^^^\n" +
					"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
					"----------\n" +
					"5. ERROR in X.java (at line 5)\n" +
					"	case X x, null:\n" +
					"	          ^^^^\n" +
					"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
					"----------\n" +
					"6. ERROR in X.java (at line 5)\n" +
					"	case X x, null:\n" +
					"	          ^^^^\n" +
					"Cannot mix pattern with other case labels\n" +
					"----------\n" +
					"7. ERROR in X.java (at line 5)\n" +
					"	case X x, null:\n" +
					"	          ^^^^\n" +
					"A null case label has to be either the only expression in a case label or the first expression followed only by a default\n" +
					"----------\n" +
					"8. ERROR in X.java (at line 7)\n" +
					"	case String s, default :\n" +
					"	^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Multi-constant case labels supported from Java 14 onwards only\n" +
					"----------\n" +
					"9. ERROR in X.java (at line 7)\n" +
					"	case String s, default :\n" +
					"	     ^^^^^^^^\n" +
					"The Java feature 'Type Patterns' is only available with source level 16 and above\n" +
					"----------\n" +
					"10. ERROR in X.java (at line 7)\n" +
					"	case String s, default :\n" +
					"	     ^^^^^^^^\n" +
					"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
					"----------\n" +
					"11. ERROR in X.java (at line 7)\n" +
					"	case String s, default :\n" +
					"	               ^^^^^^^\n" +
					"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
					"----------\n" +
					"12. ERROR in X.java (at line 7)\n" +
					"	case String s, default :\n" +
					"	               ^^^^^^^\n" +
					"Cannot mix pattern with other case labels\n" +
					"----------\n" +
					"13. ERROR in X.java (at line 7)\n" +
					"	case String s, default :\n" +
					"	               ^^^^^^^\n" +
					"A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default' \n" +
					"----------\n";

	String expectedProblemLogFrom7_13 =
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	switch (o) {\n" +
			"	        ^\n" +
			"Cannot switch on a value of type Object. Only convertible int values, strings or enum variables are permitted\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	^^^^^^^^^^^^^^\n" +
			"Multi-constant case labels supported from Java 14 onwards only\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	     ^^^\n" +
			"The Java feature 'Type Patterns' is only available with source level 16 and above\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	     ^^^\n" +
			"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	          ^^^^\n" +
			"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	          ^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	          ^^^^\n" +
			"A null case label has to be either the only expression in a case label or the first expression followed only by a default\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Multi-constant case labels supported from Java 14 onwards only\n" +
			"----------\n" +
			"9. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	     ^^^^^^^^\n" +
			"The Java feature 'Type Patterns' is only available with source level 16 and above\n" +
			"----------\n" +
			"10. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	     ^^^^^^^^\n" +
			"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
			"----------\n" +
			"11. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	               ^^^^^^^\n" +
			"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
			"----------\n" +
			"12. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	               ^^^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"13. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	               ^^^^^^^\n" +
			"A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default' \n" +
			"----------\n";

	String expectedProblemLogFrom14_15 =
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	switch (o) {\n" +
			"	        ^\n" +
			"Cannot switch on a value of type Object. Only convertible int values, strings or enum variables are permitted\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	     ^^^\n" +
			"The Java feature 'Type Patterns' is only available with source level 16 and above\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	     ^^^\n" +
			"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	          ^^^^\n" +
			"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	          ^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	          ^^^^\n" +
			"A null case label has to be either the only expression in a case label or the first expression followed only by a default\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	     ^^^^^^^^\n" +
			"The Java feature 'Type Patterns' is only available with source level 16 and above\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	     ^^^^^^^^\n" +
			"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
			"----------\n" +
			"9. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	               ^^^^^^^\n" +
			"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
			"----------\n" +
			"10. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	               ^^^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"11. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	               ^^^^^^^\n" +
			"A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default' \n" +
			"----------\n";

	String expectedProblemLogFrom16_20 =
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	switch (o) {\n" +
			"	        ^\n" +
			"Cannot switch on a value of type Object. Only convertible int values, strings or enum variables are permitted\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	     ^^^\n" +
			"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	          ^^^^\n" +
			"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	          ^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	          ^^^^\n" +
			"A null case label has to be either the only expression in a case label or the first expression followed only by a default\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	     ^^^^^^^^\n" +
			"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	               ^^^^^^^\n" +
			"The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	               ^^^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"9. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	               ^^^^^^^\n" +
			"A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default' \n" +
			"----------\n";

	String expectedProblemLogFrom21 =
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	          ^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	case X x, null:\n" +
			"	          ^^^^\n" +
			"A null case label has to be either the only expression in a case label or the first expression followed only by a default\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	               ^^^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	case String s, default :\n" +
			"	               ^^^^^^^\n" +
			"A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default' \n" +
			"----------\n";

	if (this.complianceLevel < ClassFileConstants.JDK1_7) {  // before switching on strings
		runNegativeTest(
			testFiles,
			expectedProblemLogFrom1_1_6);
	}
	else if (this.complianceLevel < ClassFileConstants.JDK14) { // before multi case
		runNegativeTest(
				testFiles,
				expectedProblemLogFrom7_13);
	} else if (this.complianceLevel < ClassFileConstants.JDK16) { // before type patterns
			runNegativeTest(
					testFiles,
					expectedProblemLogFrom14_15);
	} else if (this.complianceLevel < ClassFileConstants.JDK21) { // before case patterns
		runNegativeTest(
				testFiles,
				expectedProblemLogFrom16_20);
	} else {
		runNegativeTest(
				testFiles,
				expectedProblemLogFrom21);
	}
}
public void test0010() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	Z<Y1, Y2> var;\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Z<Y1, Y2> var;\n" +
		"	^\n" +
		"Z cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	Z<Y1, Y2> var;\n" +
		"	  ^^^^^^\n" +
		"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 2)\n" +
		"	Z<Y1, Y2> var;\n" +
		"	  ^^\n" +
		"Y1 cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 2)\n" +
		"	Z<Y1, Y2> var;\n" +
		"	      ^^\n" +
		"Y2 cannot be resolved to a type\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Z<Y1, Y2> var;\n" +
		"	^\n" +
		"Z cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	Z<Y1, Y2> var;\n" +
		"	  ^^\n" +
		"Y1 cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 2)\n" +
		"	Z<Y1, Y2> var;\n" +
		"	      ^^\n" +
		"Y2 cannot be resolved to a type\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0011() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public X(){\n" +
		"		<Y1, Y2>this(null);\n" +
		"	}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	<Y1, Y2>this(null);\n" +
		"	 ^^^^^^\n" +
		"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	<Y1, Y2>this(null);\n" +
		"	 ^^\n" +
		"Y1 cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	<Y1, Y2>this(null);\n" +
		"	     ^^\n" +
		"Y2 cannot be resolved to a type\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	<Y1, Y2>this(null);\n" +
		"	 ^^\n" +
		"Y1 cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	<Y1, Y2>this(null);\n" +
		"	     ^^\n" +
		"Y2 cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	<Y1, Y2>this(null);\n" +
		"	        ^^^^^^^^^^^\n" +
		"The constructor X(null) is undefined\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0012() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"  void foo() {\n" +
		"    assert true;\n" +
		"  }\n" +
		"}\n" +
		"\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	assert true;\n" +
		"	^^^^^^\n" +
		"\'assert\' should not be used as an identifier, since it is a reserved keyword from source level 1.4 on\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	assert true;\n" +
		"	^^^^^^\n" +
		"Syntax error on token \"assert\", AssignmentOperator expected after this token\n" +
		"----------\n";
	String expected14ProblemLog =
		"";

	String expected15ProblemLog =
		expected14ProblemLog;

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0013() {
	String[] testFiles = new String[] {
		"X.java",
		"import static aaa.*\n" +
		"public class X {\n" +
		"}\n" +
		"\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import static aaa.*\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Syntax error, static imports are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	import static aaa.*\n" +
		"	              ^^^\n" +
		"The import aaa cannot be resolved\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 1)\n" +
		"	import static aaa.*\n" +
		"	                  ^\n" +
		"Syntax error on token \"*\", ; expected after this token\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import static aaa.*\n" +
		"	              ^^^\n" +
		"The import aaa cannot be resolved\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	import static aaa.*\n" +
		"	                  ^\n" +
		"Syntax error on token \"*\", ; expected after this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0014() {
	String[] testFiles = new String[] {
		"X.java",
		"public enum X \n" +
		"}\n"
	};

	String expected13ProblemLog = """
		----------
		1. WARNING in X.java (at line 1)
			public enum X\s
			       ^^^^
		'enum' should not be used as an identifier, since it is a reserved keyword from source level 1.5 on
		----------
		2. ERROR in X.java (at line 2)
			}
			^
		Syntax error on token "}", ; expected
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public enum X \n" +
		"	            ^\n" +
		"Syntax error on token \"X\", { expected after this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0015() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(){\n" +
		"		for(String o: c) {\n" +
		"			#\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	for(String o: c) {\n" +
		"	    ^^^^^^^^^^^\n" +
		"Syntax error, \'for each\' statements are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0016() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(Z ... arg){\n" +
		"	}\n" +
		"	#\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	void foo(Z ... arg){\n" +
		"	         ^^^^^^^^^\n" +
		"Syntax error, varargs are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void foo(Z ... arg){\n" +
		"	         ^\n" +
		"Z cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	void foo(Z ... arg){\n" +
		"	         ^\n" +
		"Z cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0017() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X <T1 extends String, T2> extends Y {\n" +
		"	#\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X <T1 extends String, T2> extends Y {\n" +
		"	                ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X <T1 extends String, T2> extends Y {\n" +
		"	                                               ^\n" +
		"Y cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 2)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public class X <T1 extends String, T2> extends Y {\n" +
		"	                           ^^^^^^\n" +
		"The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X <T1 extends String, T2> extends Y {\n" +
		"	                                               ^\n" +
		"Y cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 2)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0018() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public <T1 extends String, T2> int foo(){\n" +
		"	}\n" +
		"	#\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public <T1 extends String, T2> int foo(){\n" +
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public <T1 extends String, T2> int foo(){\n" +
		"	                   ^^^^^^\n" +
		"The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0019() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	Z<Y1, Y2> var;\n" +
		"	#\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Z<Y1, Y2> var;\n" +
		"	^\n" +
		"Z cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	Z<Y1, Y2> var;\n" +
		"	  ^^^^^^\n" +
		"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 2)\n" +
		"	Z<Y1, Y2> var;\n" +
		"	  ^^\n" +
		"Y1 cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 2)\n" +
		"	Z<Y1, Y2> var;\n" +
		"	      ^^\n" +
		"Y2 cannot be resolved to a type\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 3)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Z<Y1, Y2> var;\n" +
		"	^\n" +
		"Z cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	Z<Y1, Y2> var;\n" +
		"	  ^^\n" +
		"Y1 cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 2)\n" +
		"	Z<Y1, Y2> var;\n" +
		"	      ^^\n" +
		"Y2 cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 3)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0020() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"  void foo() {\n" +
		"    assert true;\n" +
		"    #\n" +
		"  }\n" +
		"}\n" +
		"\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	assert true;\n" +
		"	^^^^^^\n" +
		"\'assert\' should not be used as an identifier, since it is a reserved keyword from source level 1.4 on\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	assert true;\n" +
		"	^^^^^^\n" +
		"Syntax error on token \"assert\", AssignmentOperator expected after this token\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n";
	String expected14ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n";

	String expected15ProblemLog =
		expected14ProblemLog;

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
//TODO (david) suspicious behavior
public void test0021() {
	String[] testFiles = new String[] {
		"X.java",
		"import staic aaa.*;\n" +
		"public class X {\n" +
		"}\n" +
		"\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import staic aaa.*;\n" +
		"	       ^^^^^\n" +
		"The import staic cannot be resolved\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	import staic aaa.*;\n" +
		"	             ^^^\n" +
		"Syntax error on token \"aaa\", delete this token\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import staic aaa.*;\n" +
		"	       ^^^^^\n" +
		"Syntax error on token \"staic\", static expected\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	import staic aaa.*;\n" +
		"	       ^^^^^\n" +
		"The import staic cannot be resolved\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
//TODO (david) suspicious behavior
public void test0022() {
	String[] testFiles = new String[] {
		"X.java",
		"import static aaa.*.*;\n" +
		"public class X {\n" +
		"}\n" +
		"\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import static aaa.*.*;\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Syntax error, static imports are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	import static aaa.*.*;\n" +
		"	              ^^^\n" +
		"The import aaa cannot be resolved\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 1)\n" +
		"	import static aaa.*.*;\n" +
		"	                   ^^\n" +
		"Syntax error on tokens, delete these tokens\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import static aaa.*.*;\n" +
		"	              ^^^\n" +
		"The import aaa cannot be resolved\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	import static aaa.*.*;\n" +
		"	                  ^\n" +
		"Syntax error on token \"*\", Identifier expected\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0023() {
	String[] testFiles = new String[] {
		"X.java",
		"import static for;\n" +
		"public class X {\n" +
		"}\n" +
		"\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import static for;\n" +
		"	       ^^^^^^^^^^\n" +
		"Syntax error on tokens, Name expected instead\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	import static for;\n" +
			"	              ^^^\n" +
			"Syntax error on token \"for\", invalid Name\n" +
			"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}

//TODO (david) reenable once bug is fixed
public void _test0024() {
	String[] testFiles = new String[] {
		"X.java",
		"import static {aaa};\n" +
		"public class X {\n" +
		"}\n" +
		"\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import static {aaa};\n" +
		"	       ^^^^^^^^^^^^\n" +
		"Syntax error on tokens, Name expected instead\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import static {aaa};\n" +
		"	              ^^^^^\n" +
		"Syntax error on tokens, Name expected instead\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0025() {
	String[] testFiles = new String[] {
		"x/X.java", """
			package x;
			static aaa.*;
			public class X {
			}

			"""
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in x\\X.java (at line 2)\n" +
		"	static aaa.*;\n" +
		"	^^^^^^\n" +
		"Syntax error on token \"static\", import expected\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in x\\X.java (at line 1)\n" +
		"	package x;\n" +
		"	         ^\n" +
		"Syntax error on token \";\", import expected after this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0026() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(){\n" +
		"		for(Object o ? c){\n" +
		"		}\n" +
		"	}\n" +
		"}\n" +
		"\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	for(Object o ? c){\n" +
		"	    ^^^^^^\n" +
		"Syntax error on token \"Object\", ( expected\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	for(Object o ? c){\n" +
		"	           ^^^\n" +
		"Syntax error on token(s), misplaced construct(s)\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	for(Object o ? c){\n" +
		"	                ^\n" +
		"Syntax error, insert \"AssignmentOperator Expression\" to complete Assignment\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 3)\n" +
		"	for(Object o ? c){\n" +
		"	                ^\n" +
		"Syntax error, insert \"; ; ) Statement\" to complete BlockStatements\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	for(Object o ? c){\n" +
		"	             ^\n" +
		"Syntax error on token \"?\", : expected\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0027() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(){\n" +
		"		for(Object o : switch){\n" +
		"		}\n" +
		"	}\n" +
		"}\n" +
		"\n"
	};

	String expected13ProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	for(Object o : switch){\n" +
			"	             ^\n" +
			"Syntax error on token \":\", delete this token\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	for(Object o : switch){\n" +
			"	             ^\n" +
			"Syntax error, insert \": Expression )\" to complete EnhancedForStatementHeader\n" +  // FIXME: bogus suggestion, this rule is compliance 1.5
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	for(Object o : switch){\n" +
			"	             ^\n" +
			"Syntax error, insert \"Statement\" to complete BlockStatements\n" +
			"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	for(Object o : switch){\n" +
		"	               ^^^^^^\n" +
		"Syntax error on token \"switch\", invalid Expression\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0028() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(int ... ){\n" +
		"	}\n" +
		"}\n" +
		"\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X {\n" +
		"	               ^\n" +
		"Syntax error, insert \"}\" to complete ClassBody\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void foo(int ... ){\n" +
		"	             ^^^\n" +
		"Syntax error on token \"...\", invalid VariableDeclaratorId\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error on token \"}\", delete this token\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X {\n" +
		"	               ^\n" +
		"Syntax error, insert \"}\" to complete ClassBody\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void foo(int ... ){\n" +
		"	             ^^^\n" +
		"Syntax error on token \"...\", VariableDeclaratorId expected after this token\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error on token \"}\", delete this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0029() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(int ... for){\n" +
		"	}\n" +
		"}\n" +
		"\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X {\n" +
		"	               ^\n" +
		"Syntax error, insert \"}\" to complete ClassBody\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void foo(int ... for){\n" +
		"	             ^^^^^^^\n" +
		"Syntax error on tokens, VariableDeclaratorId expected instead\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error on token \"}\", delete this token\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X {\n" +
		"	               ^\n" +
		"Syntax error, insert \"}\" to complete ClassBody\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void foo(int ... for){\n" +
		"	                 ^^^\n" +
		"Syntax error on token \"for\", invalid VariableDeclaratorId\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error on token \"}\", delete this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void _test0030() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(int .. aaa){\n" +
		"	}\n" +
		"}\n" +
		"\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X {\n" +
		"	               ^\n" +
		"Syntax error, insert \"}\" to complete ClassBody\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void foo(int .. aaa){\n" +
		"	             ^^\n" +
		"Syntax error on tokens, delete these tokens\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error on token \"}\", delete this token\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X {\n" +
		"	               ^\n" +
		"Syntax error, insert \"}\" to complete ClassBody\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void foo(int .. aaa){\n" +
		"	             ^^\n" +
		"Syntax error on tokens, delete these tokens\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error on token \"}\", delete this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0031() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(int ... aaa bbb){\n" +
		"	}\n" +
		"}\n" +
		"\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X {\n" +
		"	               ^\n" +
		"Syntax error, insert \"}\" to complete ClassBody\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void foo(int ... aaa bbb){\n" +
		"	         ^^^^^^^^^^^\n" +
		"Syntax error, varargs are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 2)\n" +
		"	void foo(int ... aaa bbb){\n" +
		"	             ^^^^^^^\n" +
		"Syntax error on token(s), misplaced construct(s)\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 4)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error on token \"}\", delete this token\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X {\n" +
		"	               ^\n" +
		"Syntax error, insert \"}\" to complete ClassBody\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void foo(int ... aaa bbb){\n" +
		"	                     ^^^\n" +
		"Syntax error on token \"bbb\", delete this token\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error on token \"}\", delete this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void _test0032() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X <T1 extends String, T2 extends Y {\n" +
		"	\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X <T1 extends String, T2 extends Y {\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error on token(s), misplaced construct(s)\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X <T1 extends String, T2 extends Y {\n" +
		"	                                              ^\n" +
		"Y cannot be resolved to a type\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public class X <T1 extends String, T2 extends Y {\n" +
		"	                           ^^^^^^\n" +
		"The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X <T1 extends String, T2 extends Y {\n" +
		"	                                              ^\n" +
		"Syntax error, insert \">\" to complete ReferenceType1\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 1)\n" +
		"	public class X <T1 extends String, T2 extends Y {\n" +
		"	                                              ^\n" +
		"Y cannot be resolved to a type\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0033() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X T1 extends String, T2> extends Y {\n" +
		"	\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X T1 extends String, T2> extends Y {\n" +
		"	               ^^\n" +
		"Syntax error on token \"T1\", delete this token\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X T1 extends String, T2> extends Y {\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error on tokens, delete these tokens\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X T1 extends String, T2> extends Y {\n" +
		"	             ^\n" +
		"Syntax error on token \"X\", < expected after this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0034() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X <T1 extnds String, T2> extends Y {\n" +
		"	\n" +
		"}\n"
	};

	String expected13ProblemLog = """
		----------
		1. ERROR in X.java (at line 1)
			public class X <T1 extnds String, T2> extends Y {
			^^^^^^^^^^^^^^^^^^
		Syntax error on token(s), misplaced construct(s)
		----------
		2. ERROR in X.java (at line 1)
			public class X <T1 extnds String, T2> extends Y {
			                   ^^^^^^^^^^^^^^^^^^
		Syntax error on tokens, ClassHeaderName expected instead
		----------
		3. ERROR in X.java (at line 1)
			public class X <T1 extnds String, T2> extends Y {
			                   ^^^^^^
		extnds cannot be resolved to a type
		----------
		""";

	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X <T1 extnds String, T2> extends Y {\n" +
		"	                   ^^^^^^\n" +
		"Syntax error on token \"extnds\", extends expected\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X <T1 extnds String, T2> extends Y {\n" +
		"	                   ^^^^^^\n" +
		"extnds cannot be resolved to a type\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0035() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X <T1 extends for, T2> extends Y {\n" +
		"	\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X <T1 extends for, T2> extends Y {\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error on tokens, delete these tokens\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X <T1 extends for, T2> extends Y {\n" +
		"	                           ^^^\n" +
		"Syntax error on token \"for\", invalid ReferenceType\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0036() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public <T1 extends String, T2> foo(){\n" +
		"	}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public <T1 extends String, T2> foo(){\n" +
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	public <T1 extends String, T2> foo(){\n" +
		"	                               ^^^^^\n" +
		"Return type for the method is missing\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public <T1 extends String, T2> foo(){\n" +
		"	                   ^^^^^^\n" +
		"The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	public <T1 extends String, T2> foo(){\n" +
		"	                               ^^^^^\n" +
		"Return type for the method is missing\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0037() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public <T1 extnds String, T2> int foo(){\n" +
		"	}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public <T1 extnds String, T2> int foo(){\n" +
		"	       ^^^\n" +
		"Syntax error on token(s), misplaced construct(s)\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	public <T1 extnds String, T2> int foo(){\n" +
		"	        ^^\n" +
		"T1 cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 2)\n" +
		"	public <T1 extnds String, T2> int foo(){\n" +
		"	                            ^\n" +
		"Syntax error on token \">\", ; expected\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public <T1 extnds String, T2> int foo(){\n" +
		"	        ^^\n" +
		"T1 cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	public <T1 extnds String, T2> int foo(){\n" +
		"	           ^^^^^^\n" +
		"Syntax error on token \"extnds\", extends expected\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0038() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public <T1 extends String T2> int foo(){\n" +
		"	}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public <T1 extends String T2> int foo(){\n" +
		"	       ^^^^^^^^^^^\n" +
		"Syntax error on token(s), misplaced construct(s)\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	public <T1 extends String T2> int foo(){\n" +
		"	                            ^\n" +
		"Syntax error on token \">\", ; expected\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public <T1 extends String T2> int foo(){\n" +
		"	                          ^^\n" +
		"Syntax error on token \"T2\", delete this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0039() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	Z Y1, Y2> var;\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Z Y1, Y2> var;\n" +
		"	^\n" +
		"Z cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	Z Y1, Y2> var;\n" +
		"	        ^\n" +
		"Syntax error on token \">\", , expected\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Z Y1, Y2> var;\n" +
		"	^\n" +
		"Z cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	Z Y1, Y2> var;\n" +
		"	        ^\n" +
		"Syntax error on token \">\", , expected\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0040() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	Z <Y1, Y2 var;\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Z <Y1, Y2 var;\n" +
		"	  ^^^^^^^\n" +
		"Syntax error on token(s), misplaced construct(s)\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	Z <Y1, Y2 var;\n" +
		"	       ^^\n" +
		"Y2 cannot be resolved to a type\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Z <Y1, Y2 var;\n" +
		"	       ^^\n" +
		"Syntax error, insert \">\" to complete ReferenceType1\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	Z <Y1, Y2 var;\n" +
		"	       ^^\n" +
		"Y2 cannot be resolved to a type\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0041() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	Z <Y1, for Y2> var;\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Z <Y1, for Y2> var;\n" +
		"	  ^^^^^^^^^^^^\n" +
		"Syntax error on tokens, delete these tokens\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Z <Y1, for Y2> var;\n" +
		"	       ^^^\n" +
		"Syntax error on token \"for\", delete this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0042() {
	String[] testFiles = new String[] {
		"X.java",
		"void ___eval() {\n" +
		"	new Runnable() {\n" +
		"		int ___run() throws Throwable {\n" +
		"			return blah;\n" +
		"		}\n" +
		"		private String blarg;\n" +
		"		public void run() {\n" +
		"		}\n" +
		"	};\n" +
		"}\n" +
		"public class X {\n" +
		"	private static int x;\n" +
		"	private String blah;\n" +
		"	public static void main(String[] args) {\n" +
		"	}\n" +
		"	public void hello() {\n" +
		"	}\n" +
		"	public boolean blah() {\n" +
		"		return false;\n" +
		"	}\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n"
	};

	String problemLog = (this.complianceLevel >= ClassFileConstants.JDK22) ?
			"""
			----------
			1. ERROR in X.java (at line 1)
				void ___eval() {
				^
			Implicitly Declared Classes and Instance Main Methods is a preview feature and disabled by default. Use --enable-preview to enable
			----------
			2. ERROR in X.java (at line 1)
				void ___eval() {
				^
			Implicitly declared class must have a candidate main method
			----------
			3. ERROR in X.java (at line 4)
				return blah;
				       ^^^^
			blah cannot be resolved to a variable
			----------
			""" :
			"""
			----------
			1. ERROR in X.java (at line 1)
				void ___eval() {
				^
			The preview feature Implicitly Declared Classes and Instance Main Methods is only available with source level 22 and above
			----------
			2. ERROR in X.java (at line 1)
				void ___eval() {
				^
			Implicitly declared class must have a candidate main method
			----------
			3. ERROR in X.java (at line 4)
				return blah;
				       ^^^^
			blah cannot be resolved to a variable
			----------
			""";

	if (this.complianceLevel < ClassFileConstants.JDK16) {
		problemLog += """
			4. ERROR in X.java (at line 14)
				public static void main(String[] args) {
				                   ^^^^^^^^^^^^^^^^^^^
			The method main cannot be declared static; static methods can only be declared in a static or top level type
			----------
			""";
	}
	runNegativeTest(testFiles, problemLog);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72942
 */
public void test0043() {
	String[] testFiles = new String[] {
		"x/X.java", """
			package x;
			public class X {
			}
			public static void foo(){}

			"""
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in x\\X.java (at line 3)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error on token \"}\", delete this token\n" +
		"----------\n" +
		"2. ERROR in x\\X.java (at line 4)\n" +
		"	public static void foo(){}\n" +
		"	                         ^\n" +
		"Syntax error, insert \"}\" to complete ClassBody\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		expected13ProblemLog;

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=62472
 */
public void test0044() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public <T> X(T t){\n" +
		"		System.out.println(t);\n" +
		"	}\n" +
		"	}\n" +
		"	public static void main(String[] args) {\n" +
		"		class Local extends X {\n" +
		"			Local() {\n" +
		"				<String>super(\"SUCCESS\");\n" +
		"			}\n" +
		"		}\n" +
		"		new Local();\n" +
		"	}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public <T> X(T t){\n" +
		"	        ^\n" +
		"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error on token \"}\", delete this token\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	<String>super(\"SUCCESS\");\n" +
		"	 ^^^^^^\n" +
		"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error on token \"}\", delete this token\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=62472
 */
public void test0045() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public void foo(){\n" +
		"	}\n" +
		"	}\n" +
		"	public void bar() {\n" +
		"	}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error on token \"}\", delete this token\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		expected13ProblemLog;

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74519
 */
public void test0046() {
	String[] testFiles = new String[] {
		"X.java",
		"public @interface X {\n" +
		"	String annName();\n" +
		"}"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public @interface X {\n" +
		"	                  ^\n" +
		"Syntax error, annotation declarations are only available if source level is 1.5 or greater\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog = "";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74519
 */
public void test0047() {
	String[] testFiles = new String[] {
		"A.java",
		"public @interface A {}",
		"X.java",
		"@A public class X {\n" +
		"}"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in A.java (at line 1)\n" +
		"	public @interface A {}\n" +
		"	                  ^\n" +
		"Syntax error, annotation declarations are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	@A public class X {\n" +
		"	^^\n" +
		"Syntax error, annotations are only available if source level is 1.5 or greater\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog = "";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0048() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(X ... arg[]){\n" +
		"	}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	void foo(X ... arg[]){\n" +
		"	         ^^^^^^^^^\n" +
		"Syntax error, varargs are only available if source level is 1.5 or greater\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	void foo(X ... arg[]){\n" +
		"	               ^^^\n" +
		"Extended dimensions are illegal for a variable argument\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0049() {
	String[] testFiles = new String[] {
		"X.java",
		"@interface MyAnn {\n" +
		"	String value1() default \"\";\n" +
		"	String value2();\n" +
		"}\n" +
		"class ZZZ {}		\n" +
		"public @MyAnn(\"\",\"\") class Test {		\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	@interface MyAnn {\n" +
		"	           ^^^^^\n" +
		"Syntax error, annotation declarations are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	public @MyAnn(\"\",\"\") class Test {		\n" +
		"	              ^^\n" +
		"Syntax error, insert \")\" to complete Modifier\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	public @MyAnn(\"\",\"\") class Test {		\n" +
		"	              ^^\n" +
		"The attribute value is undefined for the annotation type MyAnn\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 6)\n" +
		"	public @MyAnn(\"\",\"\") class Test {		\n" +
		"	                           ^^^^\n" +
		"The public type Test must be defined in its own file\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	String token = (this.complianceLevel >= ClassFileConstants.JDK21) ? "." : "<";
	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	public @MyAnn(\"\",\"\") class Test {		\n" +
		"	              ^^\n" +
		"The attribute value is undefined for the annotation type MyAnn\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	public @MyAnn(\"\",\"\") class Test {		\n" +
		"	                ^\n" +
		"Syntax error on token \",\", "+ token +" expected\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	public @MyAnn(\"\",\"\") class Test {		\n" +
		"	                           ^^^^\n" +
		"The public type Test must be defined in its own file\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0050() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(List<String>... args) {}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	void foo(List<String>... args) {}\n" +
		"	         ^^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error, varargs are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void foo(List<String>... args) {}\n" +
		"	         ^^^^\n" +
		"List cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 2)\n" +
		"	void foo(List<String>... args) {}\n" +
		"	              ^^^^^^\n" +
		"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	void foo(List<String>... args) {}\n" +
		"	         ^^^^\n" +
		"List cannot be resolved to a type\n" +
		"----------\n";
	String expected17ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	void foo(List<String>... args) {}\n" +
		"	         ^^^^\n" +
		"List cannot be resolved to a type\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 2)\n" +
		"	void foo(List<String>... args) {}\n" +
		"	                         ^^^^\n" +
		"Type safety: Potential heap pollution via varargs parameter args\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog,
		expected17ProblemLog
	);
}
public void test0051() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(java.util.List2<String>... args) {}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	void foo(java.util.List2<String>... args) {}\n" +
		"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error, varargs are only available if source level is 1.5 or greater\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void foo(java.util.List2<String>... args) {}\n" +
		"	         ^^^^^^^^^^^^^^^\n" +
		"java.util.List2 cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 2)\n" +
		"	void foo(java.util.List2<String>... args) {}\n" +
		"	                         ^^^^^^\n" +
		"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	void foo(java.util.List2<String>... args) {}\n" +
		"	         ^^^^^^^^^^^^^^^\n" +
		"java.util.List2 cannot be resolved to a type\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=154811
public void test0052() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo1() {\n" +
		"		class Y  {\n" +
		"		}\n" +
		"		void foo2() {\n" +
		"		}\n" +
		"		class Z<T> { \n" +
		"		}\n" +
		"	}\n" +
		"} \n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	void foo2() {\n" +
		"	^^^^\n" +
		"Syntax error on token \"void\", new expected\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	class Z<T> { \n" +
		"	^^^^^\n" +
		"Syntax error on token \"class\", invalid AssignmentOperator\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	class Z<T> { \n" +
		"	         ^\n" +
		"Syntax error on token \">\", ; expected\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	void foo2() {\n" +
		"	^^^^\n" +
		"Syntax error on token \"void\", new expected\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error, insert \";\" to complete Statement\n" +
		"----------\n";

	String expectedJ14ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	void foo2() {\n" +
		"	^^^^\n" +
		"Syntax error on token \"void\", record expected\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		(this.complianceLevel < ClassFileConstants.JDK14 ? expected15ProblemLog : expectedJ14ProblemLog)
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=42243
public void test0053() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		assert true;\n" +
		"	}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	assert true;\n" +
		"	^^^^^^\n" +
		"\'assert\' should not be used as an identifier, since it is a reserved keyword from source level 1.4 on\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	assert true;\n" +
		"	^^^^^^\n" +
		"Syntax error on token \"assert\", AssignmentOperator expected after this token\n" +
		"----------\n";
	String expected14ProblemLog =
		"";

	String expected15ProblemLog =
		expected14ProblemLog;

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0054() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		try (int i = 0) {};\n" +
		"	}\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (int i = 0) {};\n" +
		"	     ^^^^^^^^^\n" +
		"Resource specification not allowed here for source level below 1.7\n" +
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		expected14ProblemLog;

	String expected17ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (int i = 0) {};\n" +
		"	     ^^^\n" +
		"The resource type int does not implement java.lang.AutoCloseable\n" +
		"----------\n";
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog,
		expected17ProblemLog
	);
}
// test that use of multi-catch is flagged accordingly
public void test0055() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_7) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"import java.io.*;\n" +
		"public class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		try {\n" +
		"			System.out.println();\n" +
		"			Reader r = new FileReader(args[0]);\n" +
		"			r.read();\n" +
		"		} catch(IOException | RuntimeException e) {\n" +
		"			e.printStackTrace();\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	};

	String expected13ProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	} catch(IOException | RuntimeException e) {\n" +
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Multi-catch parameters are not allowed for source level below 1.7\n" +
			"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		expected14ProblemLog;

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
// rethrow should not be precisely computed in 1.6-
public void test0056() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		try {\n" +
		"			throw new DaughterOfFoo();\n"+
		"		} catch(Foo e) {\n" +
		"			try {\n" +
		"				throw e;\n" +
		"			} catch (SonOfFoo e1) {\n" +
		"			 	e1.printStackTrace();\n" +
		"			} catch (Foo e1) {}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"+
		"class Foo extends Exception {}\n"+
		"class SonOfFoo extends Foo {}\n"+
		"class DaughterOfFoo extends Foo {}\n"
	};

	String expected13ProblemLog =
			"----------\n" +
			"1. WARNING in X.java (at line 14)\n" +
			"	class Foo extends Exception {}\n" +
			"	      ^^^\n" +
			"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 15)\n" +
			"	class SonOfFoo extends Foo {}\n" +
			"	      ^^^^^^^^\n" +
			"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 16)\n" +
			"	class DaughterOfFoo extends Foo {}\n" +
			"	      ^^^^^^^^^^^^^\n" +
			"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" +
			"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		expected14ProblemLog;

	String expected17ProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	} catch (SonOfFoo e1) {\n" +
			"	         ^^^^^^^^\n" +
			"Unreachable catch block for SonOfFoo. This exception is never thrown from the try statement body\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 14)\n" +
			"	class Foo extends Exception {}\n" +
			"	      ^^^\n" +
			"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 15)\n" +
			"	class SonOfFoo extends Foo {}\n" +
			"	      ^^^^^^^^\n" +
			"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 16)\n" +
			"	class DaughterOfFoo extends Foo {}\n" +
			"	      ^^^^^^^^^^^^^\n" +
			"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" +
			"----------\n";
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog,
		expected17ProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383714
public void test0057() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"interface I {\n" +
		"  public default void foo() { System.out.println(); }\n" +
		"}\n"
	};

	String expectedProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public default void foo() { System.out.println(); }\n" +
			"	                    ^^^^^\n" +
			"Default methods are allowed only at source level 1.8 or above\n" +
			"----------\n";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383714
public void test0058() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"interface I {\n" +
		"  void foo(int p);\n" +
		"}\n" +
		"public class X {\n" +
		"  I i = System::exit;\n" +
		"}\n"
	};

	String expectedProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	I i = System::exit;\n" +
			"	      ^^^^^^^^^^^^\n" +
			"Method references are allowed only at source level 1.8 or above\n" +
			"----------\n";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383714
public void test0059() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"interface I {\n" +
		"  void foo(int p);\n" +
		"}\n" +
		"class Y {\n" +
		"   static void goo(int x) {\n" +
		"   }\n" +
		"}\n" +
		"public class X extends Y {\n" +
		"  I i = super::goo;\n" +
		"}\n"
	};

	String expectedProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	I i = super::goo;\n" +
			"	      ^^^^^^^^^^\n" +
			"Method references are allowed only at source level 1.8 or above\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	I i = super::goo;\n" +
			"	      ^^^^^^^^^^\n" +
			"The method goo(int) from the type Y should be accessed in a static way \n" +
			"----------\n";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383714
public void test0060() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"interface I {\n" +
		"  void foo(int p);\n" +
		"}\n" +
		"class Y {\n" +
		"   void goo(int x) {\n" +
		"   }\n" +
		"}\n" +
		"public class X extends Y {\n" +
		"  I i = new Y()::goo;\n" +
		"}\n"
	};

	String expectedProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	I i = new Y()::goo;\n" +
			"	      ^^^^^^^^^^^^\n" +
			"Method references are allowed only at source level 1.8 or above\n" +
			"----------\n";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383714
public void test0061() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"interface I {\n" +
		"  void foo(int p);\n" +
		"}\n" +
		"class Y {\n" +
		"   void goo(int x) {\n" +
		"   }\n" +
		"   Y() {}\n" +
		"   Y(int x) {}\n" +
		"}\n" +
		"public class X extends Y {\n" +
		"  I i = Y::new;\n" +
		"}\n"
	};

	String expectedProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	I i = Y::new;\n" +
			"	      ^^^^^^\n" +
			"Constructor references are allowed only at source level 1.8 or above\n" +
			"----------\n";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383714
public void test0062() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"interface I {\n" +
		"  int foo(int p);\n" +
		"}\n" +
		"public class X {\n" +
		"  I i = p -> 10 + 20 + 30;\n" +
		"}\n"
	};

	String expectedProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	I i = p -> 10 + 20 + 30;\n" +
			"	      ^^^^^^^^^^^^^^^^^\n" +
			"Lambda expressions are allowed only at source level 1.8 or above\n" +
			"----------\n";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=381358
public void test0063() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4 || this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"interface I {\n" +
		"  int foo(int p);\n" +
		"}\n" +
		"public class X<T> {\n" +
		"  I i = X<String>::foo;\n" +
		"  I i2 = (p) -> 10;\n" +
		"  public static int foo(int p) {\n" +
		"	return p;\n" +
		"  }\n" +
		"}\n"
	};

	String expectedProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	I i = X<String>::foo;\n" +
			"	      ^^^^^^^^^^^^^^\n" +
			"Method references are allowed only at source level 1.8 or above\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	I i = X<String>::foo;\n" +
			"	      ^^^^^^^^^^^^^^\n" +
			"The method foo(int) from the type X<String> should be accessed in a static way \n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	I i2 = (p) -> 10;\n" +
			"	       ^^^^^^^^^\n" +
			"Lambda expressions are allowed only at source level 1.8 or above\n" +
			"----------\n";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383913#c22
public void test0064() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] source = new String[] {
		"X.java",
		"class X {\n" +
		"	void foo(X this){}\n" +
		"}"
	};
	String expectedProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	void foo(X this){}\n" +
			"	           ^^^^\n" +
			"Explicit declaration of 'this' parameter is allowed only at source level 1.8 or above\n" +
			"----------\n";
	runComplianceParserTest(
			source,
			expectedProblemLog,
			expectedProblemLog,
			expectedProblemLog,
			expectedProblemLog,
			expectedProblemLog
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391201
public void testBug391201() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8 || this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	@Marker int foo(@Marker int p) {\n" +
		"		@Marker int i = 0;\n" +
		"		return i;\n" +
		"	}\n" +
		"	@Marker\n" +
		"	class Y {}\n" +
		"	@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
		"	@interface Marker {}" +
		"}",
		"java/lang/annotation/ElementType.java",
		"package java.lang.annotation;\n" +
		"public enum ElementType {\n" +
		"    TYPE,\n" +
		"    FIELD,\n" +
		"    METHOD,\n" +
		"    PARAMETER,\n" +
		"    CONSTRUCTOR,\n" +
		"    LOCAL_VARIABLE,\n" +
		"    ANNOTATION_TYPE,\n" +
		"    PACKAGE,\n" +
		"    TYPE_PARAMETER,\n" +
		"    TYPE_USE\n" +
		"}\n"
	};

	String expectedProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	@Marker int foo(@Marker int p) {\n" +
			"	^^^^^^^\n" +
			"Syntax error, type annotations are available only when source level is at least 1.8\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	@Marker int foo(@Marker int p) {\n" +
			"	                ^^^^^^^\n" +
			"Syntax error, type annotations are available only when source level is at least 1.8\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	@Marker int i = 0;\n" +
			"	^^^^^^^\n" +
			"Syntax error, type annotations are available only when source level is at least 1.8\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 6)\n" +
			"	@Marker\n" +
			"	^^^^^^^\n" +
			"Syntax error, type annotations are available only when source level is at least 1.8\n" +
			"----------\n";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
public void testBug399773() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_8)
		return;
	String[] testFiles = new String[] {
		"X.java",
		"interface I {\n" +
		"	void doit();\n" +
		"	default void doitalso () {}\n" +
		"}\n" +
		"interface J {\n" +
		"	void doit();\n" +
		"	default void doitalso () {}\n" +
		"}\n" +
		"public class X {\n" +
		"	Object p = (I & J) () -> {};\n" +
		"}\n" ,
	};

	String expectedProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	default void doitalso () {}\n" +
			"	             ^^^^^^^^^^^\n" +
			"Default methods are allowed only at source level 1.8 or above\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	default void doitalso () {}\n" +
			"	             ^^^^^^^^^^^\n" +
			"Default methods are allowed only at source level 1.8 or above\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	Object p = (I & J) () -> {};\n" +
			"	            ^^^^^\n" +
			"Additional bounds are not allowed in cast operator at source levels below 1.8\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 10)\n" +
			"	Object p = (I & J) () -> {};\n" +
			"	                   ^^^^^\n" +
			"Lambda expressions are allowed only at source level 1.8 or above\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 10)\n" +
			"	Object p = (I & J) () -> {};\n" +
			"	                   ^^^^^\n" +
			"The target type of this expression must be a functional interface\n" +
			"----------\n";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399778,  [1.8][compiler] Conditional operator expressions should propagate target types
public void testBug399778() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	String[] testFiles = new String[] {
		"X.java",
		"import java.util.Arrays;\n" +
		"import java.util.List;\n" +
		"public class X  {\n" +
		"		List<String> l = null == null ? Arrays.asList() : Arrays.asList(\"Hello\",\"world\");\n" +
		"}\n",
	};

	String expectedProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	List<String> l = null == null ? Arrays.asList() : Arrays.asList(\"Hello\",\"world\");\n" +
			"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from List<capture#1-of ? extends Object> to List<String>\n" +
			"----------\n";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		""   // 1.8 should compile this fine.
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399778,  [1.8][compiler] Conditional operator expressions should propagate target types
public void testBug399778a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	String[] testFiles = new String[] {
		"X.java",
		"import java.util.Arrays;\n" +
		"import java.util.List;\n" +
		"public class X  {\n" +
		"		List<String> l = (List<String>) (null == null ? Arrays.asList() : Arrays.asList(\"Hello\",\"world\"));\n" +
		"}\n",
	};

	String expectedProblemLog =
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	List<String> l = (List<String>) (null == null ? Arrays.asList() : Arrays.asList(\"Hello\",\"world\"));\n" +
			"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked cast from List<capture#1-of ? extends Object> to List<String>\n" +
			"----------\n";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog   // 1.8 also issue type safety warning.
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780: static methods in interfaces.
public void testBug399780() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"I.java",
		"interface I {\n" +
		"  public static void foo1() { System.out.println(); }\n" +
		"  public static void foo2();\n" +
		"  public abstract static void foo3();\n" +
		"}\n"
	};

	String expectedProblemLog =
			"----------\n" +
			"1. ERROR in I.java (at line 2)\n" +
			"	public static void foo1() { System.out.println(); }\n" +
			"	                   ^^^^^^\n" +
			"Static methods are allowed in interfaces only at source level 1.8 or above\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 2)\n" +
			"	public static void foo1() { System.out.println(); }\n" +
			"	                   ^^^^^^\n" +
			"Illegal modifier for the interface method foo1; only public & abstract are permitted\n" +
			"----------\n" +
			"3. ERROR in I.java (at line 3)\n" +
			"	public static void foo2();\n" +
			"	                   ^^^^^^\n" +
			"Illegal modifier for the interface method foo2; only public & abstract are permitted\n" +
			"----------\n" +
			"4. ERROR in I.java (at line 4)\n" +
			"	public abstract static void foo3();\n" +
			"	                            ^^^^^^\n" +
			"Illegal modifier for the interface method foo3; only public & abstract are permitted\n" +
			"----------\n";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=399769:  Use of '_' as identifier name should trigger a diagnostic
public void testBug399781() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"   int _;\n" +
		"	void foo(){\n" +
		"		int _   = 3;\n" +
        "		int _123 = 4;\n" +
        "		int a_   = 5;\n" +
		"	}\n" +
        "   void goo(int _) {}\n" +
		"   void zoo() {\n" +
        "      try {\n" +
		"      } catch (Exception _) {\n" +
        "      }\n" +
		"   }\n" +
		"}\n",
	};
	String problemLog = null;
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		problemLog = """
						----------
						1. WARNING in X.java (at line 4)
							int _   = 3;
							    ^
						The local variable _ is hiding a field from type X
						----------
						2. WARNING in X.java (at line 8)
							void goo(int _) {}
							             ^
						The parameter _ is hiding a field from type X
						----------
						3. WARNING in X.java (at line 11)
							} catch (Exception _) {
							                   ^
						The parameter _ is hiding a field from type X
						----------
						""";
	} else if (this.complianceLevel == ClassFileConstants.JDK1_8) {
		problemLog = """
					----------
					1. WARNING in X.java (at line 2)
						int _;
						    ^
					'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on
					----------
					2. WARNING in X.java (at line 4)
						int _   = 3;
						    ^
					'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on
					----------
					3. WARNING in X.java (at line 4)
						int _   = 3;
						    ^
					The local variable _ is hiding a field from type X
					----------
					4. WARNING in X.java (at line 8)
						void goo(int _) {}
						             ^
					'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on
					----------
					5. WARNING in X.java (at line 8)
						void goo(int _) {}
						             ^
					The parameter _ is hiding a field from type X
					----------
					6. WARNING in X.java (at line 11)
						} catch (Exception _) {
						                   ^
					'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on
					----------
					7. WARNING in X.java (at line 11)
						} catch (Exception _) {
						                   ^
					The parameter _ is hiding a field from type X
					----------
					""";
	} else if (this.complianceLevel < ClassFileConstants.JDK22) {
		problemLog = """
					----------
					1. ERROR in X.java (at line 2)
						int _;
						    ^
					'_' is a keyword from source level 9 onwards, cannot be used as identifier
					----------
					2. ERROR in X.java (at line 4)
						int _   = 3;
						    ^
					'_' is a keyword from source level 9 onwards, cannot be used as identifier
					----------
					3. WARNING in X.java (at line 4)
						int _   = 3;
						    ^
					The local variable _ is hiding a field from type X
					----------
					4. ERROR in X.java (at line 8)
						void goo(int _) {}
						             ^
					'_' is a keyword from source level 9 onwards, cannot be used as identifier
					----------
					5. WARNING in X.java (at line 8)
						void goo(int _) {}
						             ^
					The parameter _ is hiding a field from type X
					----------
					6. ERROR in X.java (at line 11)
						} catch (Exception _) {
						                   ^
					'_' is a keyword from source level 9 onwards, cannot be used as identifier
					----------
					7. WARNING in X.java (at line 11)
						} catch (Exception _) {
						                   ^
					The parameter _ is hiding a field from type X
					----------
					""";
	} else {
		problemLog = """
				----------
				1. ERROR in X.java (at line 2)
					int _;
					    ^
				As of release 22, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters
				----------
				2. ERROR in X.java (at line 8)
					void goo(int _) {}
					             ^
				As of release 22, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters
				----------
				3. WARNING in X.java (at line 8)
					void goo(int _) {}
					             ^
				The parameter _ is hiding a field from type X
				----------
				""";
	}
//	(this.complianceLevel < ClassFileConstants.JDK22) ? "" : "";
	runNegativeTest(testFiles, problemLog);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406846:  [1.8] compiler NPE for method reference/lambda code compiled with < 1.8 compliance
public void test406846() {

	if (this.complianceLevel >= ClassFileConstants.JDK1_8) // tested in LET.
		return;

	String[] testFiles = new String[] {
		"X.java",
		"import java.util.*;\n" +
		"public class X {\n" +
		"  public static <E> void printItem(E value, int index) {\n" +
		"    String output = String.format(\"%d -> %s\", index, value);\n" +
		"    System.out.println(output);\n" +
		"  }\n" +
		"  public static void main(String[] argv) {\n" +
		"    List<String> list = Arrays.asList(\"A\",\"B\",\"C\");\n" +
		"    eachWithIndex(list,X::printItem);\n" +
		"  }\n" +
		"  interface ItemWithIndexVisitor<E> {\n" +
		"    public void visit(E item, int index);\n" +
		"  }\n" +
		"  public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {\n" +
		"    for (int i = 0; i < list.size(); i++) {\n" +
		"         visitor.visit(list.get(i), i);\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
	};

	String expectedProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	eachWithIndex(list,X::printItem);\n" +
			"	                   ^^^^^^^^^^^^\n" +
			"Method references are allowed only at source level 1.8 or above\n" +
			"----------\n";
	String expected1314ProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	public static <E> void printItem(E value, int index) {\n" +
			"	               ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	String output = String.format(\"%d -> %s\", index, value);\n" +
			"	                       ^^^^^^\n" +
			"The method format(String, Object[]) in the type String is not applicable for the arguments (String, int, E)\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	List<String> list = Arrays.asList(\"A\",\"B\",\"C\");\n" +
			"	     ^^^^^^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	List<String> list = Arrays.asList(\"A\",\"B\",\"C\");\n" +
			"	                           ^^^^^^\n" +
			"The method asList(T[]) in the type Arrays is not applicable for the arguments (String, String, String)\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 9)\n" +
			"	eachWithIndex(list,X::printItem);\n" +
			"	                   ^^^^^^^^^^^^\n" +
			"Method references are allowed only at source level 1.8 or above\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 11)\n" +
			"	interface ItemWithIndexVisitor<E> {\n" +
			"	                               ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 14)\n" +
			"	public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {\n" +
			"	               ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 14)\n" +
			"	public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {\n" +
			"	                                          ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"9. ERROR in X.java (at line 14)\n" +
			"	public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {\n" +
			"	                                                                        ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n";

	runComplianceParserTest(
			testFiles,
			expected1314ProblemLog,
			expected1314ProblemLog,
			expectedProblemLog,
			expectedProblemLog,
			expectedProblemLog,
			expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401850: [1.8][compiler] Compiler fails to type poly allocation expressions in method invocation contexts
// FAIL: sub-optimal overload picked
public void test401850() {

	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"   static void foo(Object o) {\n" +
				"	   System.out.println(\"foo(Object)\");\n" +
				"   }\n" +
				"   static void foo(X<String> o) {\n" +
				"	   System.out.println(\"foo(X<String>)\");\n" +
				"   }\n" +
				"   public static void main(String[] args) { \n" +
				"      foo(new X<>()); \n" +
				"   } \n" +
				"}\n",
			},
			this.complianceLevel == ClassFileConstants.JDK1_7 ? "foo(Object)" : "foo(X<String>)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429110: [1.8][quick fix] Hovering over the error does not show the quick fix
//FAIL: sub-optimal overload picked
public void test429110() {
	if (this.complianceLevel != ClassFileConstants.JDK1_7)
		return;
	this.runNegativeTest(
			new String[] {
				"java/lang/annotation/ElementType.java",
				"package java.lang.annotation;\n" +
				"public enum ElementType {\n" +
				"    TYPE,\n" +
				"    FIELD,\n" +
				"    METHOD,\n" +
				"    PARAMETER,\n" +
				"    CONSTRUCTOR,\n" +
				"    LOCAL_VARIABLE,\n" +
				"    ANNOTATION_TYPE,\n" +
				"    PACKAGE,\n" +
				"    TYPE_PARAMETER,\n" +
				"    TYPE_USE\n" +
				"}\n",
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.util.List;\n" +
				"public class X {\n" +
				"	@Target(ElementType.TYPE_USE)\n" +
				"	static @interface NonNull { }\n" +
				"	List<@NonNull String> foo(List<@NonNull String> arg) {\n" +
				"		return arg;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	List<@NonNull String> foo(List<@NonNull String> arg) {\n" +
			"	     ^^^^^^^^\n" +
			"Syntax error, type annotations are available only when source level is at least 1.8\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	List<@NonNull String> foo(List<@NonNull String> arg) {\n" +
			"	                               ^^^^^^^^\n" +
			"Syntax error, type annotations are available only when source level is at least 1.8\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=421477: [1.8][compiler] strange error message for default method in class
public void test421477() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  default void f() {\n" +
				"  }\n" +
				"  default X() {}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	default void f() {\n" +
			"	             ^^^\n" +
			(this.complianceLevel >= ClassFileConstants.JDK1_8 ?
			"Default methods are allowed only in interfaces.\n" :
			"Illegal modifier for the method f; only public, protected, private, abstract, static, final, synchronized, native & strictfp are permitted\n")	+
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	default X() {}\n" +
			"	        ^\n" +
			"Syntax error on token \"X\", Identifier expected after this token\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=428605: [1.8] Error highlighting can be improved for default methods
public void test428605() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface X {\n" +
				"       default void f() {\n" +
				"       }\n" +
				"       static void g() {\n" +
				"       }\n" +
				"} \n"
			},
			(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	default void f() {\n" +
			"	             ^^^\n" +
			"Default methods are allowed only at source level 1.8 or above\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	static void g() {\n" +
			"	            ^^^\n" +
			"Static methods are allowed in interfaces only at source level 1.8 or above\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	static void g() {\n" +
			"	            ^^^\n" +
			"Illegal modifier for the interface method g; only public & abstract are permitted\n" +
			"----------\n" :
			""));
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=440285
// [1.8] Compiler allows array creation reference with type arguments
public void testBug440285() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	runNegativeTest(new String [] {
		"X.java",
		"import java.util.function.Function;\n" +
		"class Y{}\n" +
		"class Z{}\n" +
		"public class X {\n" +
		"	Function<Integer, int[]> m1 = int[]::<Y, Z>new;\n" +
		"	Function<Integer, int[]> m2 = int[]::<Y>new;\n" +
		"}",},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	Function<Integer, int[]> m1 = int[]::<Y, Z>new;\n" +
		"	                                      ^^^^\n" +
		"Type arguments are not allowed here\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	Function<Integer, int[]> m2 = int[]::<Y>new;\n" +
		"	                                      ^\n" +
		"Type arguments are not allowed here\n" +
		"----------\n");
}
public void testBug531714_001() {
	if (this.complianceLevel >= ClassFileConstants.JDK12)
		return;
	String[] testFiles = 			new String[] {
			"X.java",
			"public class X {\n" +
			"	static int twice(int i) {\n" +
			"		int tw = switch (i) {\n" +
			"			case 0 -> i * 0;\n" +
			"			case 1 -> 2;\n" +
			"			default -> 3;\n" +
			"		};\n" +
			"		return tw;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.print(twice(3));\n" +
			"	}\n" +
			"}\n",
	};

	String expectedProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	int tw = switch (i) {\n" +
			"			case 0 -> i * 0;\n" +
			"			case 1 -> 2;\n" +
			"			default -> 3;\n" +
			"		};\n" +
			"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Switch Expressions are supported from Java 14 onwards only\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case 0 -> i * 0;\n" +
			"	^^^^^^\n" +
			"Arrow in case statement supported from Java 14 onwards only\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	case 1 -> 2;\n" +
			"	^^^^^^\n" +
			"Arrow in case statement supported from Java 14 onwards only\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 6)\n" +
			"	default -> 3;\n" +
			"	^^^^^^^\n" +
			"Arrow in case statement supported from Java 14 onwards only\n" +
			"----------\n";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
public void testBug531714_002() {
	if (this.complianceLevel >= ClassFileConstants.JDK12)
		return;
	String[] testFiles = new String[] {
			"X.java",
			"public class X {\n" +
			"	static int twice(int i) {\n" +
			"		switch (i) {\n" +
			"			case 0 -> i * 0;\n" +
			"			case 1 -> 2;\n" +
			"			default -> 3;\n" +
			"		}\n" +
			"		return 0;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.print(twice(3));\n" +
			"	}\n" +
			"}\n",
	};

	String expectedProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case 0 -> i * 0;\n" +
			"	^^^^^^\n" +
			"Arrow in case statement supported from Java 14 onwards only\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	case 1 -> 2;\n" +
			"	^^^^^^\n" +
			"Arrow in case statement supported from Java 14 onwards only\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	default -> 3;\n" +
			"	^^^^^^^\n" +
			"Arrow in case statement supported from Java 14 onwards only\n" +
			"----------\n";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2008
// Support for identifier '_' for old compile source/target versions
public void testIssue2008() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public X(){\n" +
        "	}\n" +
		"   void _() {\n" +
		"       _();\n" +
		"   }\n" +
		"       public static void main(String [] args) {\n" +
		"           System.out.println(\"OK\");\n" +
		"       }\n" +
		"   class _ {\n" +
		"   }\n" +
		"}\n"
	};

	String expectedProblemLogUpto1_7 = "";
	String expected1_8ProblemLog = """
			----------
			1. WARNING in X.java (at line 4)
				void _() {
				     ^
			'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on
			----------
			2. WARNING in X.java (at line 5)
				_();
				^
			'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on
			----------
			3. WARNING in X.java (at line 10)
				class _ {
				      ^
			'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on
			----------
			""";
	String expected9to20ProblemLog = """
			----------
			1. ERROR in X.java (at line 4)
				void _() {
				     ^
			'_' is a keyword from source level 9 onwards, cannot be used as identifier
			----------
			2. ERROR in X.java (at line 5)
				_();
				^
			'_' is a keyword from source level 9 onwards, cannot be used as identifier
			----------
			3. ERROR in X.java (at line 10)
				class _ {
				      ^
			'_' is a keyword from source level 9 onwards, cannot be used as identifier
			----------
			""";

	String expected22ProblemLog = """
					----------
					1. ERROR in X.java (at line 4)
						void _() {
						     ^
					Syntax error on token "_", Identifier expected
					----------
					2. ERROR in X.java (at line 4)
						void _() {
						     ^
					void is an invalid type for the variable _
					----------
					3. ERROR in X.java (at line 5)
						_();
						^
					Syntax error on token "_", this expected
					----------
					4. ERROR in X.java (at line 10)
						class _ {
						      ^
					Syntax error on token "_", Identifier expected
					----------\n""";

	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		runConformTest(
			true,
			testFiles,
			expectedProblemLogUpto1_7,
			"OK", null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	} else if(this.complianceLevel == ClassFileConstants.JDK1_8) {
		runConformTest(
				true,
				testFiles,
				expected1_8ProblemLog,
				"OK", null,
				JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	} else if(this.complianceLevel < ClassFileConstants.JDK22) {
		runNegativeTest(
				testFiles,
				expected9to20ProblemLog);
	} else {
		runNegativeTest(
				testFiles,
				expected22ProblemLog);
	}
}
}
