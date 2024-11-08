/*******************************************************************************
 * Copyright (c) 2001, 2017 IBM Corporation and others.
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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contributions for
 *     						bug 185682 - Increment/decrement operators mark local variables as read
 *     						bug 328281 - visibility leaks not detected when analyzing unused field in private class
 *							Bug 410218 - Optional warning for arguments of "unexpected" types to Map#get(Object), Collection#remove(Object) et al.
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/* Collects potential programming problems tests that are not segregated in a
 * dedicated test class (aka NullReferenceTest). */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ProgrammingProblemsTest extends AbstractRegressionTest {

public ProgrammingProblemsTest(String name) {
    super(name);
}

	// Static initializer to specify tests subset using TESTS_* static variables
  	// All specified tests which does not belong to the class are skipped...
  	// Only the highest compliance level is run; add the VM argument
  	// -Dcompliance=1.4 (for example) to lower it if needed
  	static {
//    	TESTS_NAMES = new String[] { "test0055" };
//		TESTS_NUMBERS = new int[] { 56 };
//  	TESTS_RANGE = new int[] { 1, -1 };
  	}

public static Test suite() {
    return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {
    return ProgrammingProblemsTest.class;
}
@Override
protected Map getCompilerOptions() {
	Map compilerOptions = super.getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal,  CompilerOptions.OPTIMIZE_OUT);
	return compilerOptions;
}
void runTest(
		String[] testFiles,
		String[] errorOptions,
		String[] warningOptions,
		String[] ignoreOptions,
		boolean expectingCompilerErrors,
		String expectedCompilerLog,
		String expectedOutputString,
		boolean forceExecution,
		String[] classLib,
		boolean shouldFlushOutputDirectory,
		String[] vmArguments,
		Map customOptions,
		ICompilerRequestor clientRequestor,
		boolean skipJavac) {
	Map compilerOptions = customOptions;
	if (errorOptions != null || warningOptions != null ||
			ignoreOptions != null) {
		if (compilerOptions == null) {
			compilerOptions = new HashMap();
		}
		if (errorOptions != null) {
		    for (int i = 0; i < errorOptions.length; i++) {
		    	compilerOptions.put(errorOptions[i], CompilerOptions.ERROR);
		    }
		}
		if (warningOptions != null) {
		    for (int i = 0; i < warningOptions.length; i++) {
		    	compilerOptions.put(warningOptions[i], CompilerOptions.WARNING);
		    }
		}
		if (ignoreOptions != null) {
		    for (int i = 0; i < ignoreOptions.length; i++) {
		    	compilerOptions.put(ignoreOptions[i], CompilerOptions.IGNORE);
		    }
		}
	}
	runTest(testFiles,
		expectingCompilerErrors,
		expectedCompilerLog,
		expectedOutputString,
		"" /* expectedErrorString */,
		forceExecution,
		classLib,
		shouldFlushOutputDirectory,
		vmArguments,
		compilerOptions,
		clientRequestor,
		skipJavac);
}

// default behavior upon unread parameters
public void test0001_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo(boolean b) {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		false /* skipJavac */);
}

// reporting unread paramaters as warning
public void test0002_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo(boolean b) {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public void foo(boolean b) {\n" +
		"	                        ^\n" +
		"The value of the parameter b is not used\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unread parameters using the Javadoc
// @param disables by default
public void test0003_unread_parameters() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"/** @param b mute warning **/\n" +
			"  public void foo(boolean b) {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unread parameters using the Javadoc
// @param disabling can be disabled
public void test0004_unread_parameters() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"/** @param b mute warning **/\n" +
			"  public void foo(boolean b) {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	public void foo(boolean b) {\n" +
		"	                        ^\n" +
		"The value of the parameter b is not used\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unread parameters using SuppressWarnings
public void test0005_unread_parameters() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"@SuppressWarnings(\"unused\")\n" + // most specific token
				"  public void foo(boolean b) {\n" +
				"  }\n" +
				"@SuppressWarnings(\"all\")\n" + // least specific token
				"  public void foo(int i) {\n" +
				"  }\n" +
				"}\n"
				},
			null /* errorOptions */,
			new String[] {
				CompilerOptions.OPTION_ReportUnusedParameter
				} /* warningOptions */,
			null /* ignoreOptions */,
			false /* expectingCompilerErrors */,
			"" /* expectedCompilerLog */,
			"" /* expectedOutputString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */,
			null /* vmArguments */,
			null /* customOptions */,
			null /* clientRequestor */,
			true /* skipJavac */);
	}
}

// reporting unread paramaters as error
public void test0006_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo(boolean b) {\n" +
			"  }\n" +
			"}\n"
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public void foo(boolean b) {\n" +
		"	                        ^\n" +
		"The value of the parameter b is not used\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// default behavior upon unnecessary declaration of thrown checked exceptions
public void test0007_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public void foo() throws IOException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		false /* skipJavac */);
}

// reporting unnecessary declaration of thrown checked exceptions as warning
public void test0008_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public void foo() throws IOException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	public void foo() throws IOException {\n" +
		"	                         ^^^^^^^^^^^\n" +
		"The declared exception IOException is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked
// exceptions using the Javadoc
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73244
// @throws disables by default
public void test0009_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"/** @throws IOException mute warning **/\n" +
			"  public void foo() throws IOException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked
// exceptions using the Javadoc
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73244
// @throws disabling can be disabled
public void test0010_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionIncludeDocCommentReference,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"/** @throws IOException mute warning **/\n" +
			"  public void foo() throws IOException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	public void foo() throws IOException {\n" +
		"	                         ^^^^^^^^^^^\n" +
		"The declared exception IOException is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked
// exceptions using SuppressWarnings
public void test0011_declared_thrown_checked_exceptions() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" +
				"public class X {\n" +
				"@SuppressWarnings(\"all\")\n" + // no specific token
				"  public void foo() throws IOException {\n" +
				"  }\n" +
				"}\n"
				},
			null /* errorOptions */,
			new String[] {
				CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
				} /* warningOptions */,
			null /* ignoreOptions */,
			false /* expectingCompilerErrors */,
			"" /* expectedCompilerLog */,
			"" /* expectedOutputString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */,
			null /* vmArguments */,
			null /* customOptions */,
			null /* clientRequestor */,
			true /* skipJavac */);
	}
}

// reporting unnecessary declaration of thrown checked exceptions as error
public void test0012_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public void foo() throws IOException {\n" +
			"  }\n" +
			"}\n"
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public void foo() throws IOException {\n" +
		"	                         ^^^^^^^^^^^\n" +
		"The declared exception IOException is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked
// exceptions using the Javadoc
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73244
// @throws disables by default, but only exact matches work
public void test0013_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"import java.io.EOFException;\n" +
			"public class X {\n" +
			"/** @throws EOFException does not mute warning for IOException **/\n" +
			"  public void foo() throws IOException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 5)\n" +
		"	public void foo() throws IOException {\n" +
		"	                         ^^^^^^^^^^^\n" +
		"The declared exception IOException is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// interaction between errors and warnings
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=203721
public void test0014_declared_thrown_checked_exceptions_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  void foo(int unused) throws IOException {}\n" +
			"}\n"
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	void foo(int unused) throws IOException {}\n" +
		"	             ^^^^^^\n" +
		"The value of the parameter unused is not used\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	void foo(int unused) throws IOException {}\n" +
		"	                            ^^^^^^^^^^^\n" +
		"The declared exception IOException is not actually thrown by the method foo(int) from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// interaction between errors and warnings
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=203721
// variant: both warnings show up
public void test0015_declared_thrown_checked_exceptions_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  void foo(int unused) throws IOException {}\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException,
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	void foo(int unused) throws IOException {}\n" +
		"	             ^^^^^^\n" +
		"The value of the parameter unused is not used\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 3)\n" +
		"	void foo(int unused) throws IOException {}\n" +
		"	                            ^^^^^^^^^^^\n" +
		"The declared exception IOException is not actually thrown by the method foo(int) from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// reporting unread paramaters as error on a constructor
public void test0016_unread_parameters_constructor() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public X(boolean b) {\n" +
			"  }\n" +
			"}\n"
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public X(boolean b) {\n" +
		"	                 ^\n" +
		"The value of the parameter b is not used\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=208001
public void test0017_shadowing_package_visible_methods() {
	runTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"  void foo() {\n" +
			"  }\n" +
			"}\n",
			"q/Y.java",
			"package q;\n" +
			"public class Y extends p.X {\n" +
			"  void foo() {\n" +
			"  }\n" +
			"}\n",
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in q\\Y.java (at line 3)\n" +
		"	void foo() {\n" +
		"	     ^^^^^\n" +
		"The method Y.foo() does not override the inherited method from X since it is private to a different package\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		new ICompilerRequestor() {
			public void acceptResult(CompilationResult result) {
				if (result.compilationUnit.getFileName()[0] == 'Y') {
					assertEquals("unexpected problems count", 1, result.problemCount);
					assertEquals("unexpected category", CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT, result.problems[0].getCategoryID());
				}
			}
		} /* clientRequestor */,
		true /* skipJavac */);
}
// default behavior upon unnecessary declaration of thrown unchecked exceptions
public void test0018_declared_thrown_unchecked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws ArithmeticException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// default behavior upon unnecessary declaration of thrown unchecked exceptions
public void test0019_declared_thrown_unchecked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws RuntimeException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// default behavior upon unnecessary declaration of Exception
public void test0020_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Exception {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// default behavior upon unnecessary declaration of Throwable
public void test0021_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Throwable {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning
public void test0022_declared_thrown_unchecked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws ArithmeticException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// the external API uses another string literal - had it wrong in first attempt
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0023_declared_thrown_unchecked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(JavaCore.COMPILER_PB_UNUSED_DECLARED_THROWN_EXCEPTION_EXEMPT_EXCEPTION_AND_THROWABLE,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Exception {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public void foo() throws Exception {\n" +
		"	                         ^^^^^^^^^\n" +
		"The declared exception Exception is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning
public void test0024_declared_thrown_unchecked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws RuntimeException {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// focused on Exception and Throwable, which are not unchecked but can catch
// unchecked exceptions
public void test0025_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Exception {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public void foo() throws Exception {\n" +
		"	                         ^^^^^^^^^\n" +
		"The declared exception Exception is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// focused on Exception and Throwable, which are not unchecked but can catch
// unchecked exceptions
public void test0026_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Throwable {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public void foo() throws Throwable {\n" +
		"	                         ^^^^^^^^^\n" +
		"The declared exception Throwable is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// disabling the reporting of unnecessary declaration of thrown unchecked
// exceptions using the Javadoc
// @throws disables by default
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0027_declared_thrown_unchecked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"/** @throws Exception mute warning **/\n" +
			"  public void foo() throws Exception {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// disabling the reporting of unnecessary declaration of thrown unchecked
// exceptions using the Javadoc
// @throws disabling can be disabled
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0028_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionIncludeDocCommentReference,
			CompilerOptions.DISABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"/** @throws Exception mute warning **/\n" +
			"  public void foo() throws Exception {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	public void foo() throws Exception {\n" +
		"	                         ^^^^^^^^^\n" +
		"The declared exception Exception is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// disabling the reporting of unnecessary declaration of thrown unchecked
// exceptions using SuppressWarnings
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0029_declared_thrown_checked_exceptions() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map customOptions = new HashMap();
		customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
				CompilerOptions.DISABLED);
		runTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"@SuppressWarnings(\"all\")\n" + // no specific token
				"  public void foo() throws Exception {\n" +
				"  }\n" +
				"}\n"
				},
			null /* errorOptions */,
			new String[] {
				CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
				} /* warningOptions */,
			null /* ignoreOptions */,
			false /* expectingCompilerErrors */,
			"" /* expectedCompilerLog */,
			"" /* expectedOutputString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */,
			null /* vmArguments */,
			customOptions,
			null /* clientRequestor */,
			true /* skipJavac */);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as error
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the error for unchecked exceptions, using Exception instead
public void test0030_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Exception {\n" +
			"  }\n" +
			"}\n"
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public void foo() throws Exception {\n" +
		"	                         ^^^^^^^^^\n" +
		"The declared exception Exception is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// disabling the reporting of unnecessary declaration of thrown unchecked
// exceptions using the Javadoc
// @throws disables by default, but only exact matches work
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0031_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport,
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"/** @throws Throwable does not mute warning for Exception **/\n" +
			"  public void foo() throws Exception {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	public void foo() throws Exception {\n" +
		"	                         ^^^^^^^^^\n" +
		"The declared exception Exception is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions
public void test0032_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Error {\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
// suppressed the warning for unchecked exceptions, using Exception instead
public void test0033_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() throws Exception {\n" +
			"    if (bar()) {\n" +
			"      throw new Exception();\n" +
			"    }\n" +
			"  }\n" +
			"  boolean bar() {\n" +
			"    return true;\n" +
			"  }\n" +
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216897
// reporting unnecessary declaration of thrown unchecked exceptions as warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
public void test0034_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static final class MyError extends Error {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"  public void foo() throws Throwable {\n" +
			"    try {\n" +
			"      bar();\n" +
			"    } catch (MyError e) {\n" +
			"    }\n" +
			"  }\n" +
			"  private void bar() {}\n" +
			"}"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" +
		"1. WARNING in X.java (at line 5)\n" +
		"	public void foo() throws Throwable {\n" +
		"	                         ^^^^^^^^^\n" +
		"The declared exception Throwable is not actually thrown by the method foo() from type X\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
public void test0035_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static final class MyError extends Error {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"  public void foo() throws Throwable {\n" +
			"    throw new MyError();\n" +
			"  }\n" +
			"}"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219461
public void test0036_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static class E1 extends Exception {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"  public static class E2 extends E1 {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"  public void foo() throws E1 {\n" +
			"    throw new E2();\n" +
			"  }\n" +
			"}"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=115814
public void test0037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b1 = args == args;\n" +
			"		boolean b2 = args != args;\n" +
			"		boolean b3 = b1 == b1;\n" +
			"		boolean b4 = b1 != b1;\n" +
			"		boolean b5 = b1 && b1;\n" +
			"		boolean b6 = b1 || b1;\n" +
			"		\n" +
			"		boolean b7 = foo() == foo();\n" +
			"		boolean b8 = foo() != foo();\n" +
			"		boolean b9 = foo() && foo();\n" +
			"		boolean b10 = foo() || foo();\n" +
			"	}\n" +
			"	static boolean foo() { return true; }\n" +
			"	Zork z;\n" +
			"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	boolean b1 = args == args;\n" +
			"	             ^^^^^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	boolean b2 = args != args;\n" +
			"	             ^^^^^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 5)\n" +
			"	boolean b3 = b1 == b1;\n" +
			"	             ^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 6)\n" +
			"	boolean b4 = b1 != b1;\n" +
			"	             ^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 7)\n" +
			"	boolean b5 = b1 && b1;\n" +
			"	             ^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 8)\n" +
			"	boolean b6 = b1 || b1;\n" +
			"	             ^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 16)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}

/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=276740"
 */
public void test0038() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b1 = 1 == 1;\n" +
			"		boolean b2 = 1 != 1;\n" +
			"		boolean b3 = 1 == 1.0;\n" +
			"		boolean b4 = 1 != 1.0;\n" +
			"		boolean b5 = 1 == 2;\n" +
			"		boolean b6 = 1 != 2;\n" +
			"		boolean b7 = 1 == 2.0;\n" +
			"		boolean b8 = 1 != 2.0;\n" +
			"       final short s1 = 1;\n" +
			"       final short s2 = 2;\n" +
			"       boolean b9 = 1 == s1;\n" +
			"       boolean b10 = 1 == s2;\n" +
			"       boolean b91 = 1 != s1;\n" +
			"       boolean b101 = 1 != s2;\n" +
			"       final long l1 = 1;\n" +
			"       final long l2 = 2;\n" +
			"       boolean b11 = 1 == l1;\n" +
			"       boolean b12 = 1 == l2;\n" +
			"       boolean b111 = 1 != l1;\n" +
			"       boolean b121 = 1 != l2;\n" +
			"       boolean b13 = s1 == l1;\n" +
			"       boolean b14 = s1 == l2;\n" +
			"       boolean b15 = s1 != l1;\n" +
			"       boolean b16 = s1 != l2;\n" +
			"	}\n" +
			"	Zork z;\n" +
			"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	boolean b1 = 1 == 1;\n" +
			"	             ^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	boolean b2 = 1 != 1;\n" +
			"	             ^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 5)\n" +
			"	boolean b3 = 1 == 1.0;\n" +
			"	             ^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 6)\n" +
			"	boolean b4 = 1 != 1.0;\n" +
			"	             ^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 13)\n" +
			"	boolean b9 = 1 == s1;\n" +
			"	             ^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 15)\n" +
			"	boolean b91 = 1 != s1;\n" +
			"	              ^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"7. WARNING in X.java (at line 19)\n" +
			"	boolean b11 = 1 == l1;\n" +
			"	              ^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"8. WARNING in X.java (at line 21)\n" +
			"	boolean b111 = 1 != l1;\n" +
			"	               ^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"9. WARNING in X.java (at line 23)\n" +
			"	boolean b13 = s1 == l1;\n" +
			"	              ^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"10. WARNING in X.java (at line 25)\n" +
			"	boolean b15 = s1 != l1;\n" +
			"	              ^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"11. ERROR in X.java (at line 28)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}

/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=276741"
 */
public void test0039() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void gain(String[] args) {\n" +
			"		boolean b1 = this == this;\n" +
			"		boolean b2 = this != this;\n" +
			"		boolean b3 = this != new X();\n" +
			"		boolean b4 = this == new X();\n" +
			"	}\n" +
			"	Zork z;\n" +
			"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	boolean b1 = this == this;\n" +
			"	             ^^^^^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	boolean b2 = this != this;\n" +
			"	             ^^^^^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
/**
 * see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=281776"
 * We now tolerate comparison of float and double entities against
 * themselves as a legitimate idiom for NaN checking.
 */
public void test0040() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
		    "    public static void main(String[] args) {\n" +
		    "        double var = Double.NaN;\n" +
		    "            if(var != var) {\n" +
		    "                  System.out.println(\"NaN\");\n" +
		    "            }\n" +
		    "            float varf = 10;\n" +
		    "            if(varf != varf) {\n" +
		    "            	System.out.println(\"NaN\");\n" +
		    "            }\n" +
		    "   }\n" +
			"	Zork z;\n" +
			"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=251227
public void test0041() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(1.0 == 1.0);\n" +
			"		System.out.println(1.0f == 1.0f);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	System.out.println(1.0 == 1.0);\n" +
		"	                   ^^^^^^^^^^\n" +
		"Comparing identical expressions\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	System.out.println(1.0f == 1.0f);\n" +
		"	                   ^^^^^^^^^^^^\n" +
		"Comparing identical expressions\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=248897
public void test0042() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	runTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"    public static void main(String[]  args) {\n" +
				"        final String var = \"Hello\";\n" +
				"        final int local = 10;\n" +
				"        @ZAnn(var + local)\n" +
				"        class X {}\n" +
				"        new X();\n" +
				"    }\n" +
				"}\n" +
				"@interface ZAnn {\n" +
				"    String value();\n" +
				"}\n"
				},
			null /* errorOptions */,
			new String[] {
				CompilerOptions.OPTION_ReportUnusedLocal
				} /* warningOptions */,
			null /* ignoreOptions */,
			false /* expectingCompilerErrors */,
			"" /* expectedCompilerLog */,
			"" /* expectedOutputString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */,
			null /* vmArguments */,
			null /* customOptions */,
			null /* clientRequestor */,
			true /* skipJavac */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=313825
public void test0043() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	void foo(int i) {\n" +
			"		foo((a));\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	foo((a));\n" +
		"	     ^\n" +
		"a cannot be resolved to a variable\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=310264
public void test0044() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"   volatile int x;\n" +
			"   int nvx;\n" +
			"	void foo(int i) {\n" +
			"		x = x;\n" +
			"       nvx = nvx;\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 6)\n" +
		"	nvx = nvx;\n" +
		"	^^^^^^^^^\n" +
		"The assignment to variable nvx has no effect\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=310264
public void test0045() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"   volatile int x = this.x;\n" +
			"   int nvx = this.nvx;\n" +
			"	void foo(int i) {\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	volatile int x = this.x;\n" +
		"	             ^^^^^^^^^^\n" +
		"The assignment to variable x has no effect\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 3)\n" +
		"	int nvx = this.nvx;\n" +
		"	    ^^^^^^^^^^^^^^\n" +
		"The assignment to variable nvx has no effect\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
public void test0046() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" +
				"    int foo() {\n" +
				"        int i=1;\n" +
				"        boolean b=false;\n" +
				"        b|=true;\n" + 			// not a relevant usage
				"        int k = 2;\n" +
				"        --k;\n" + 				// not a relevant usage
				"        k+=3;\n" + 			// not a relevant usage
				"        Integer j = 3;\n" +
				"        j++;\n" + 				// relevant because unboxing is involved
				"        i++;\n" +				// not relevant but should still not report because next is relevant
				"        return i++;\n" + 		// value after increment is used
				"    }\n" +
				"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	boolean b=false;\n" +
			"	        ^\n" +
			"The value of the local variable b is not used\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 6)\n" +
			"	int k = 2;\n" +
			"	    ^\n" +
			"The value of the local variable k is not used\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// variant with private fields instead of locals
public void test0046_field() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" +
				"    private int i=1;\n" +
				"    private boolean b=false;\n" +
				"    private int k = 2;\n" +
				"    private Integer j = 3;\n" +
				"    int foo() {\n" +
				"        b|=true;\n" + 			// not a relevant usage
				"        --k;\n" + 				// not a relevant usage
				"        k+=3;\n" + 			// not a relevant usage
				"        j++;\n" + 				// relevant because unboxing is involved
				"        return i++;\n" + 		// value after increment is used
				"    }\n" +
				"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	private boolean b=false;\n" +
			"	                ^\n" +
			"The value of the field X.b is not used\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	private int k = 2;\n" +
			"	            ^\n" +
			"The value of the field X.k is not used\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// variant with private fields instead of locals - this-qualified access
public void test0046_field_this_qualified() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" +
				"    private int i=1;\n" +
				"    private boolean b=false;\n" +
				"    private int k = 2;\n" +
				"    private Integer j = 3;\n" +
				"    int foo() {\n" +
				"        this.b|=true;\n" + 		// not a relevant usage
				"        --this.k;\n" + 			// not a relevant usage
				"        getThis().k+=3;\n" + 		// not a relevant usage
				"        this.j++;\n" + 			// relevant because unboxing is involved
				"        return this.i++;\n" + 		// value after increment is used
				"    }\n" +
				"    X getThis() { return this; }\n" +
				"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	private boolean b=false;\n" +
			"	                ^\n" +
			"The value of the field X.b is not used\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	private int k = 2;\n" +
			"	            ^\n" +
			"The value of the field X.k is not used\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// variant with private fields instead of locals - regular qualified access
public void test0046_field_qualified() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" +
				"    private int i=1;\n" +
				"    private boolean b=false;\n" +
				"    private int k = 2;\n" +
				"    private Integer j = 3;\n" +
				"    int foo(X that) {\n" +
				"        that.b|=true;\n" + 		// not a relevant usage
				"        --that.k;\n" + 			// not a relevant usage
				"        that.k+=3;\n" + 			// not a relevant usage
				"        that.j++;\n" + 			// relevant because unboxing is involved
				"        that.i++;\n"+				// not relevant but should still not report because next is relevant
				"        return that.i++;\n" + 		// value after increment is used
				"    }\n" +
				"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	private boolean b=false;\n" +
			"	                ^\n" +
			"The value of the field X.b is not used\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	private int k = 2;\n" +
			"	            ^\n" +
			"The value of the field X.k is not used\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// variant with fields inside a private type
public void test0046_field_in_private_type() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" +
				"    private class Y {\n" +
				"        int i=1;\n" +
				"        public boolean b=false;\n" +
				"        protected int k = 2;\n" +
				"        Integer j = 3;\n" +
				"    }\n" +
				"    int foo(Y y) {\n" +
				"        y.b|=true;\n" + 				// not a relevant usage
				"        --y.k;\n" + 					// not a relevant usage
				"        y.k+=3;\n" + 					// not a relevant usage
				"        y.j++;\n" + 					// relevant because unboxing is involved
				"        int result = y.i++;\n" + 	// value after increment is used
				"        y.i++;\n" +					// not relevant, but previous is
				"        return result;\n" +
				"    }\n" +
				"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	public boolean b=false;\n" +
			"	               ^\n" +
			"The value of the field X.Y.b is not used\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	protected int k = 2;\n" +
			"	              ^\n" +
			"The value of the field X.Y.k is not used\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
public void test0047() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" +
				"    void foo(int param1, int param2, Integer param3) {\n" +
				"        boolean b=false;\n" +
				"        b|=true;\n" + 			// not a relevant usage
				"        param1++;\n" + 		// not a relevant usage
				"        {\n" +
				"            int val=23;\n" +
				"            param2 += val;\n" +// not a relevant usage of param2
				"        }\n" +
				"        param3++;\n" + 		// relevant because unboxing is involved
				"    }\n" +
				"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 2)\n" +
			"	void foo(int param1, int param2, Integer param3) {\n" +
			"	             ^^^^^^\n" +
			"The value of the parameter param1 is not used\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 2)\n" +
			"	void foo(int param1, int param2, Integer param3) {\n" +
			"	                         ^^^^^^\n" +
			"The value of the parameter param2 is not used\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 3)\n" +
			"	boolean b=false;\n" +
			"	        ^\n" +
			"The value of the local variable b is not used\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// To verify that unused parameter warning is not shown for an implementing method's parameter when
// CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract is disabled
public void test0048() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.DISABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends A implements Y{\n" +
				"   public void foo(int param1, int param2, Integer param3) {\n" + // implementing method, so dont warn
				"        boolean b=false;\n" +
				"        b|=true;\n" + 			// not a relevant usage
				"        param1++;\n" + 		// not a relevant usage
				"        param2 += 1;\n" + 		// not a relevant usage
				"        param3++;\n" + 		// relevant because unboxing is involved
				"    }\n" +
				"   public void foo(int param1, int param2) {\n" + // warn
				"        boolean b=false;\n" +
				"        b|=true;\n" + 			// not a relevant usage
				"        param1++;\n" + 		// not a relevant usage
				"        param2 += 1;\n" + 		// not a relevant usage
				"    }\n" +
				"   public void bar(int param1, int param2, Integer param3) {\n" + // implementing method, so dont warn
				"        param1++;\n" + 		// not a relevant usage
				"        param2 += 1;\n" + 		// not a relevant usage
				"        param3++;\n" + 		// relevant because unboxing is involved
				"    }\n" +
				"}\n" +
				"interface Y{\n" +
				"	public void foo(int param1, int param2, Integer param3);" +
				"}\n" +
				"abstract class A{\n" +
				"	public abstract void bar(int param1, int param2, Integer param3);" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	boolean b=false;\n" +
			"	        ^\n" +
			"The value of the local variable b is not used\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 9)\n" +
			"	public void foo(int param1, int param2) {\n" +
			"	                    ^^^^^^\n" +
			"The value of the parameter param1 is not used\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 9)\n" +
			"	public void foo(int param1, int param2) {\n" +
			"	                                ^^^^^^\n" +
			"The value of the parameter param2 is not used\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 10)\n" +
			"	boolean b=false;\n" +
			"	        ^\n" +
			"The value of the local variable b is not used\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// To verify that unused parameter warning is not shown for an overriding method's parameter when
// CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete is disabled
public void test0049() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.DISABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends A {\n" +
				"   public void foo(int param1, int param2, Integer param3) {\n" + // overriding method, so dont warn
				"        boolean b=false;\n" +
				"        b|=true;\n" + 			// not a relevant usage
				"        param1++;\n" + 		// not a relevant usage
				"        param2 += 1;\n" + 		// not a relevant usage
				"        param3++;\n" + 		// relevant because unboxing is involved
				"    }\n" +
				"   public void foo(int param1, Integer param3) {\n" + // overriding method, so dont warn
				"        param1++;\n" + 		// not a relevant usage
				"        param3++;\n" + 		// relevant because unboxing is involved
				"    }\n" +
				"}\n" +
				"class A{\n" +
				"   public void foo(int param1, int param2, Integer param3) {\n" +
				"        param1 -=1;\n" + 		// not a relevant usage
				"        param2--;\n" + 		// not a relevant usage
				"        param3--;\n" + 		// relevant because unboxing is involved
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	boolean b=false;\n" +
			"	        ^\n" +
			"The value of the local variable b is not used\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 9)\n" +
			"	public void foo(int param1, Integer param3) {\n" +
			"	                    ^^^^^^\n" +
			"The value of the parameter param1 is not used\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 15)\n" +
			"	public void foo(int param1, int param2, Integer param3) {\n" +
			"	                    ^^^^^^\n" +
			"The value of the parameter param1 is not used\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 15)\n" +
			"	public void foo(int param1, int param2, Integer param3) {\n" +
			"	                                ^^^^^^\n" +
			"The value of the parameter param2 is not used\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// To verify that unused local warning is not shown for locals declared in unreachable code
public void test0050() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" +
				"    int foo() {\n" +
				"        int i=1;\n" +
				"		 if (false) {\n" +
				"        	boolean b=false;\n" + // don't complain as unused
				"        	b|=true;\n" +
				"		 }\n" + 			// not a relevant usage
				"        int k = 2;\n" +
				"        --k;\n" + 			// not a relevant usage
				"        k+=3;\n" + 		// not a relevant usage
				"        Integer j = 3;\n" +
				"        j++;\n" + 			// relevant because unboxing is involved
				"        return i++;\n" + 	// value after increment is used
				"    }\n" +
				"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	if (false) {\n" +
			"        	boolean b=false;\n" +
			"        	b|=true;\n" +
			"		 }\n" +
			"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 8)\n" +
			"	int k = 2;\n" +
			"	    ^\n" +
			"The value of the local variable k is not used\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185682
// To verify that a constructor argument is handled correctly
public void test0051() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"class X {\n" +
					"    X(int abc) {\n" +
					"        abc++;\n" +    // not a relevant usage
					"    }\n" +
					"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 2)\n" +
			"	X(int abc) {\n" +
			"	      ^^^\n" +
			"The value of the parameter abc is not used\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328281
public void test0052() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	this.runConformTest(
			new String[] {
					"X.java",
					"class X {\n" +
					"    Y y = new Y();\n" +
					"    private class Y {\n" +
					"        int abc;\n" +
					"        Y() {\n" +
					"            abc++;\n" +    // not a relevant usage
					"        }\n" +
					"    }\n" +
					"    class Z extends Y {}\n" + // makes 'abc' externally accessible
					"}"
			},
			"",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			null/*vmArguments*/,
			customOptions,
			null/*requestor*/);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328281
// multi-level inheritance
public void test0052a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
			new String[] {
					"Outer.java",
					"class Outer {\n" +
					"    private class Inner1 {\n" +
					"        int foo;\n" +
					"    }\n" +
					"    private class Inner2 extends Inner1 { }\n" +
					"    class Inner3 extends Inner2 { }\n" +  // foo is exposed here
					"}\n"
			},
			"",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			null/*vmArguments*/,
			customOptions,
			null/*requestor*/);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328281
// member type of private
public void test0052b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
			new String[] {
					"Outer.java",
					"class Outer {\n" +
					"    private class Inner1 {\n" +
					"        class Foo{}\n" +
					"    }\n" +
					"    private class Inner2 extends Inner1 { }\n" +
					"    class Inner3 extends Inner2 { }\n" +  // Foo is exposed here
					"}\n"
			},
			"",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			null/*vmArguments*/,
			customOptions,
			null/*requestor*/);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test0053() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"    int foo() {\n" +
			"        int i=1;\n" +
			"        i++;\n" + 	// value after increment is still not used
			"        return 0;\n" +
			"    }\n" +
			"}"
		},
		"",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
	String expectedOutput =
		"  // Method descriptor #15 ()I\n" +
		"  // Stack: 1, Locals: 1\n" +
		"  int foo();\n" +
		"    0  iconst_0\n" +
		"    1  ireturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 5]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 2] local: this index: 0 type: X\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test0054() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"    int foo() {\n" +
			"        int i=1;\n" +
			"        return i+=1;\n" + 	// value is used as it is returned
			"    }\n" +
			"}"
		},
		"",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
	String expectedOutput =
		"  // Method descriptor #15 ()I\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  int foo();\n" +
		"    0  iconst_1\n" +
		"    1  istore_1 [i]\n" +
		"    2  iinc 1 1 [i]\n" +
		"    5  iload_1 [i]\n" +
		"    6  ireturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 2, line: 4]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 7] local: this index: 0 type: X\n" +
		"        [pc: 2, pc: 7] local: i index: 1 type: int\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=329613
// regression caused by https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test0055() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runNegativeTest(
			new String[] {
					"test1/E.java",
					"package test1;\n" +
					"public class E {\n" +
						"    private void foo() {\n" +
						"        int a= 10;\n" +
						"        a++;\n" +
						"        a--;\n" +
						"        --a;\n" +
						"        ++a;\n" +
						"        for ( ; ; a++) {\n" +
							"        }\n" +
							"    }\n" +
							"}"
			},
			"----------\n" +
			"1. WARNING in test1\\E.java (at line 4)\n" +
			"	int a= 10;\n" +
			"	    ^\n" +
			"The value of the local variable a is not used\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test0056() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    static int foo() {\n" +
			"        int i = 2;\n" +
			"        int j = 3;\n" +
			"        return (i += j *= 3);\n" + 	// value is used as it is returned
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(foo());\n" +
			"    }\n" +
			"}"
		},
		"11",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test0057() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main (String args[]) {\n" +
			"        int i = 0;\n" +
			"        i += 4 + foo();\n" +
			"    }\n" +
			"    public static int foo() {\n" +
			"    	System.out.println(\"OK\");\n" +
			"    	return 0;\n" +
			"    }\n" +
			"}"
		},
		"OK",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336648
public void _test0058() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    void foo(String m) {\n" +
				"        final String message= m;\n" +
				"        new Runnable() {\n" +
				"            public void run() {\n" +
				"                if (\"x\".equals(message)) {\n" +
				"                    bug(); // undefined method\n" +
				"                }\n" +
				"            }\n" +
				"        }.run();\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	bug(); // undefined method\n" +
			"	^^^\n" +
			"The method bug() is undefined for the type new Runnable(){}\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=339139
// Issue local variable not used warning inside deadcode
public void test0059() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"    	Object a = null;\n" +
			"    	if (a != null){\n" +
			"        	int j = 3;\n" +
			"        	j++;\n" + 	// value is not used
			"    	}\n" +
			"    	System.out.println(\"OK\");\n" +
			"    }\n" +
			"}"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	if (a != null){\n" +
		"        	int j = 3;\n" +
		"        	j++;\n" +
		"    	}\n" +
		"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	int j = 3;\n" +
		"	    ^\n" +
		"The value of the local variable j is not used\n" +
		"----------\n",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417803,  [internal] Build a build environment compiler to warn on TypeBinding comparisons
public void test0060() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUninternedIdentityComparison, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"org/eclipse/jdt/internal/compiler/lookup/X.java",
			"package org.eclipse.jdt.internal.compiler.lookup;\n" +
			"class TypeBinding {\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void gain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void vain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void cain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in org\\eclipse\\jdt\\internal\\compiler\\lookup\\X.java (at line 7)\n" +
		"	if (t1 == t2) { \n" +
		"	    ^^^^^^^^\n" +
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" +
		"----------\n" +
		"2. ERROR in org\\eclipse\\jdt\\internal\\compiler\\lookup\\X.java (at line 9)\n" +
		"	if (t1 == t2) {\n" +
		"	    ^^^^^^^^\n" +
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" +
		"----------\n" +
		"3. ERROR in org\\eclipse\\jdt\\internal\\compiler\\lookup\\X.java (at line 16)\n" +
		"	if (t1 == t2) { \n" +
		"	    ^^^^^^^^\n" +
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" +
		"----------\n" +
		"4. ERROR in org\\eclipse\\jdt\\internal\\compiler\\lookup\\X.java (at line 18)\n" +
		"	if (t1 == t2) {\n" +
		"	    ^^^^^^^^\n" +
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" +
		"----------\n" +
		"5. ERROR in org\\eclipse\\jdt\\internal\\compiler\\lookup\\X.java (at line 28)\n" +
		"	if (t1 == t2) { \n" +
		"	    ^^^^^^^^\n" +
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" +
		"----------\n",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417803,  [internal] Build a build environment compiler to warn on TypeBinding comparisons
public void test0061() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUninternedIdentityComparison, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"org/eclipse/nonjdt/internal/compiler/lookup/X.java",
			"package org.eclipse.nonjdt.internal.compiler.lookup;\n" +
			"class TypeBinding {\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void gain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void vain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void cain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417803,  [internal] Build a build environment compiler to warn on TypeBinding comparisons
public void test0062() throws Exception {
	Map customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"org/eclipse/jdt/internal/compiler/lookup/X.java",
			"package org.eclipse.jdt.internal.compiler.lookup;\n" +
			"class TypeBinding {\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void gain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void vain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void cain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417803,  [internal] Build a build environment compiler to warn on TypeBinding comparisons
public void test0063() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUninternedIdentityComparison, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"org/eclipse/jdt/core/dom/X.java",
			"package org.eclipse.jdt.core.dom;\n" +
			"interface ITypeBinding {\n" +
			"}\n" +
			"class TypeBinding implements ITypeBinding {\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void gain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void vain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		//$IDENTITY-COMPARISON$\n" +
			"		if (t1 == t2) { \n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void cain(String[] args) {\n" +
			"		TypeBinding t1 = null, t2 = null;\n" +
			"		if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"			if (t2 == t1) {  //$IDENTITY-COMPARISON$\n" +
			"				if (t1 == t2) { //$IDENTITY-COMPARISON$\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in org\\eclipse\\jdt\\core\\dom\\X.java (at line 9)\n" +
		"	if (t1 == t2) { \n" +
		"	    ^^^^^^^^\n" +
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" +
		"----------\n" +
		"2. ERROR in org\\eclipse\\jdt\\core\\dom\\X.java (at line 11)\n" +
		"	if (t1 == t2) {\n" +
		"	    ^^^^^^^^\n" +
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" +
		"----------\n" +
		"3. ERROR in org\\eclipse\\jdt\\core\\dom\\X.java (at line 18)\n" +
		"	if (t1 == t2) { \n" +
		"	    ^^^^^^^^\n" +
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" +
		"----------\n" +
		"4. ERROR in org\\eclipse\\jdt\\core\\dom\\X.java (at line 20)\n" +
		"	if (t1 == t2) {\n" +
		"	    ^^^^^^^^\n" +
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" +
		"----------\n" +
		"5. ERROR in org\\eclipse\\jdt\\core\\dom\\X.java (at line 30)\n" +
		"	if (t1 == t2) { \n" +
		"	    ^^^^^^^^\n" +
		"The uninterned types TypeBinding and TypeBinding should not be compared using ==/!= operators.\n" +
		"----------\n",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// Collection: contains & remove & get
public void testBug410218a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"class X {\n" +
			"  void test() {\n" +
			"	Set<Short> set = new HashSet<Short>();\n" +
			"	short one = 1;\n" +
			"	set.add(one);\n" +
			"\n" +
			"	if (set.contains(\"ONE\")) // bad\n" +
			"		set.remove(\"ONE\"); // bad\n" +
			"	if (set.contains(1)) // bad\n" +
			"		set.remove(1); // bad (tries to remove \"Integer 1\")\n" +
			"	System.out.println(set); // shows that the \"Short 1\" is still in!\n" +
			"\n" +
			"	if (set.contains(one)) // ok\n" +
			"		set.remove(one); // ok\n" +
			"	if (set.contains(Short.valueOf(one))) // ok\n" +
			"		set.remove(Short.valueOf(one)); // ok\n" +
			"	System.out.println(set);\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 8)\n" +
		"	if (set.contains(\"ONE\")) // bad\n" +
		"	                 ^^^^^\n" +
		"Unlikely argument type String for contains(Object) on a Collection<Short>\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 9)\n" +
		"	set.remove(\"ONE\"); // bad\n" +
		"	           ^^^^^\n" +
		"Unlikely argument type String for remove(Object) on a Collection<Short>\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 10)\n" +
		"	if (set.contains(1)) // bad\n" +
		"	                 ^\n" +
		"Unlikely argument type int for contains(Object) on a Collection<Short>\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 11)\n" +
		"	set.remove(1); // bad (tries to remove \"Integer 1\")\n" +
		"	           ^\n" +
		"Unlikely argument type int for remove(Object) on a Collection<Short>\n" +
		"----------\n");
}
// HashSet vs. TreeSet
public void testBug410218b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"class X {\n" +
			"  <T> void test(Set<HashSet<T>> hss, TreeSet<T> ts, LinkedHashSet<T> lhs) {\n" +
			"	if (hss.contains(ts)) // bad\n" +
			"		hss.remove(ts); // bad\n" +
			"	if (hss.contains((Set<T>)ts)) // ok\n" +
			"		hss.remove((Set<T>)ts); // ok\n" +
			"	if (hss.contains(lhs)) // ok\n" +
			"		hss.remove(lhs); // ok\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	if (hss.contains(ts)) // bad\n" +
		"	                 ^^\n" +
		"Unlikely argument type TreeSet<T> for contains(Object) on a Collection<HashSet<T>>\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	hss.remove(ts); // bad\n" +
		"	           ^^\n" +
		"Unlikely argument type TreeSet<T> for remove(Object) on a Collection<HashSet<T>>\n" +
		"----------\n");
}
// HashSet vs. TreeSet or: strict
public void testBug410218b2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_COLLECTION_METHOD_ARGUMENT_TYPE_STRICT, JavaCore.ENABLED);
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"class X {\n" +
			"  <T> void test(Set<HashSet<T>> hss, TreeSet<T> ts, LinkedHashSet<T> lhs) {\n" +
			"	if (hss.contains(ts)) // bad\n" +
			"		hss.remove(ts); // bad\n" +
			"	if (hss.contains((Set<T>)ts)) // bad (because of strict check)\n" +
			"		hss.remove((Set<T>)ts); // bad (because of strict check)\n" +
			"	if (hss.contains(lhs)) // ok\n" +
			"		hss.remove(lhs); // ok\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	if (hss.contains(ts)) // bad\n" +
		"	                 ^^\n" +
		"Unlikely argument type TreeSet<T> for contains(Object) on a Collection<HashSet<T>>\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	hss.remove(ts); // bad\n" +
		"	           ^^\n" +
		"Unlikely argument type TreeSet<T> for remove(Object) on a Collection<HashSet<T>>\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 6)\n" +
		"	if (hss.contains((Set<T>)ts)) // bad (because of strict check)\n" +
		"	                 ^^^^^^^^^^\n" +
		"Unlikely argument type Set<T> for contains(Object) on a Collection<HashSet<T>>\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 7)\n" +
		"	hss.remove((Set<T>)ts); // bad (because of strict check)\n" +
		"	           ^^^^^^^^^^\n" +
		"Unlikely argument type Set<T> for remove(Object) on a Collection<HashSet<T>>\n" +
		"----------\n",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// Map: contains* & remove & get
public void testBug410218c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"class X {\n" +
			"  Number test(Map<? extends Number, Number> m, boolean f) {\n" +
			"	if (m.containsKey(\"ONE\")) // bad\n" +
			"		m.remove(\"ONE\"); // bad\n" +
			"	if (m.containsValue(\"ONE\")) // bad\n" +
			"		m.remove(\"ONE\"); // bad\n" +
			"	short one = 1;\n" +
			"	if (m.containsKey(one)) // almost ok\n" +
			"		m.remove(one); // almost ok\n" +
			"	if (m.containsValue(Short.valueOf(one))) // ok\n" +
			"		m.remove(Short.valueOf(one)); // almost ok\n" +
			"	if (f)\n" +
			"		return m.get(\"ONE\"); // bad\n" +
			"	return m.get(one);\n // almost ok\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	if (m.containsKey(\"ONE\")) // bad\n" +
		"	                  ^^^^^\n" +
		"Unlikely argument type String for containsKey(Object) on a Map<capture#1-of ? extends Number,Number>\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	m.remove(\"ONE\"); // bad\n" +
		"	         ^^^^^\n" +
		"Unlikely argument type String for remove(Object) on a Map<capture#2-of ? extends Number,Number>\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 6)\n" +
		"	if (m.containsValue(\"ONE\")) // bad\n" +
		"	                    ^^^^^\n" +
		"Unlikely argument type String for containsValue(Object) on a Map<capture#3-of ? extends Number,Number>\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 7)\n" +
		"	m.remove(\"ONE\"); // bad\n" +
		"	         ^^^^^\n" +
		"Unlikely argument type String for remove(Object) on a Map<capture#4-of ? extends Number,Number>\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 14)\n" +
		"	return m.get(\"ONE\"); // bad\n" +
		"	             ^^^^^\n" +
		"Unlikely argument type String for get(Object) on a Map<capture#9-of ? extends Number,Number>\n" +
		"----------\n");
}
// Collection: {contains,remove,retain}All, non-generic sub type of Collection, configured to be ERROR
public void testBug410218d() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_COLLECTION_METHOD_ARGUMENT_TYPE, JavaCore.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"interface NumberCollection extends Collection<Number> {}\n" +
			"class X {\n" +
			"  void test(NumberCollection numbers, List<Integer> ints, Set<String> stringSet) {\n" +
			"	if (numbers.containsAll(ints)) // ok\n" +
			"		numbers.removeAll(ints); // ok\n" +
			"	else\n" +
			"		numbers.retainAll(ints); // ok\n" +
			"\n" +
			"	numbers.removeAll(stringSet); // bad\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	numbers.removeAll(stringSet); // bad\n" +
		"	                  ^^^^^^^^^\n" +
		"Unlikely argument type Set<String> for removeAll(Collection<?>) on a Collection<Number>\n" +
		"----------\n",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
// List.indexOf: w/ and w/o @SuppressWarnings
public void testBug410218e() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_COLLECTION_METHOD_ARGUMENT_TYPE, JavaCore.WARNING);
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"class X {\n" +
			"  int test1(List<Integer> ints, Object o) {\n" +
			"	return ints.indexOf(\"ONE\"); // bad\n" +
			"  }\n" +
			"  @SuppressWarnings(\"unlikely-arg-type\")\n" +
			"  int test2(List<Integer> ints, boolean f, Object o) {\n" +
			"	if (f)\n" +
			"		return ints.indexOf(\"ONE\"); // bad but suppressed\n" +
			"	return ints.indexOf(o); // supertype\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	return ints.indexOf(\"ONE\"); // bad\n" +
		"	                    ^^^^^\n" +
		"Unlikely argument type String for indexOf(Object) on a List<Integer>\n" +
		"----------\n",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}

// Method references, equals, wildcards
public void testBug410218f() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_COLLECTION_METHOD_ARGUMENT_TYPE, JavaCore.WARNING);
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_EQUALS_ARGUMENT_TYPE, JavaCore.INFO);
	runNegativeTest(
		new String[] {
			"test/TestUnlikely.java",
			"package test;\n" +
			"\n" +
			"import java.util.Collection;\n" +
			"import java.util.Iterator;\n" +
			"import java.util.List;\n" +
			"import java.util.Map;\n" +
			"import java.util.Objects;\n" +
			"import java.util.Set;\n" +
			"import java.util.function.BiPredicate;\n" +
			"import java.util.function.Predicate;\n" +
			"\n" +
			"public class TestUnlikely {\n" +
			"	interface Interface {\n" +
			"	}\n" +
			"\n" +
			"	interface OtherInterface {\n" +
			"	}\n" +
			"\n" +
			"	static class NonFinal implements Interface {\n" +
			"	}\n" +
			"\n" +
			"	static class Sub extends NonFinal implements OtherInterface {\n" +
			"	}\n" +
			"\n" +
			"	static final class Final implements Interface {\n" +
			"	}\n" +
			"\n" +
			"	void f1(List<Interface> c, Interface i, OtherInterface o, Final f, NonFinal nf, Sub s) {\n" +
			"		c.remove(i);\n" +
			"		c.remove(o); // warning: unrelated interface\n" +
			"		c.remove(f);\n" +
			"		c.remove(nf);\n" +
			"		c.remove(s);\n" +
			"	}\n" +
			"\n" +
			"	void f2(List<OtherInterface> c, Interface i, OtherInterface o, Final f, NonFinal nf, Sub s) {\n" +
			"		c.remove(i); // warning: unrelated interface\n" +
			"		c.remove(o);\n" +
			"		c.remove(f); // warning: impossible\n" +
			"		c.remove(nf); // warning: castable, but not supertype\n" +
			"		c.remove(s);\n" +
			"	}\n" +
			"\n" +
			"	void f3(List<Final> c, Interface i, OtherInterface o, Final f, NonFinal nf, Sub s) {\n" +
			"		c.remove(i); // supertype\n" +
			"		c.remove(o); // warning: impossible\n" +
			"		c.remove(f);\n" +
			"		c.remove(nf); // warning: impossible\n" +
			"		c.remove(s); // warning: impossible\n" +
			"	}\n" +
			"\n" +
			"	void f4(List<NonFinal> c, Interface i, OtherInterface o, Final f, NonFinal nf, Sub s) {\n" +
			"		c.remove(i); // supertype\n" +
			"		c.remove(o); // warning: unrelated interface\n" +
			"		c.remove(f); // warning: impossible\n" +
			"		c.remove(nf);\n" +
			"		c.remove(s);\n" +
			"	}\n" +
			"\n" +
			"	void f5(List<Sub> c, Interface i, OtherInterface o, Final f, NonFinal nf, Sub s) {\n" +
			"		c.remove(i); // supertype\n" +
			"		c.remove(o); // supertype\n" +
			"		c.remove(f); // warning: impossible\n" +
			"		c.remove(nf); // supertype\n" +
			"		c.remove(s);\n" +
			"	}\n" +
			"\n" +
			"	<K, V> void map(Map<K, V> map, K key, V value) {\n" +
			"		map.containsKey(key);\n" +
			"		map.containsKey(value); // warning\n" +
			"		map.containsValue(key); // warning\n" +
			"		map.containsValue(value);\n" +
			"	}\n" +
			"\n" +
			"	boolean wildcards(Collection<?> c, Iterable<?> s) {\n" +
			"		for (Iterator<?> iterator = s.iterator(); iterator.hasNext();) {\n" +
			"			if (c.contains(iterator.next())) {\n" +
			"				return true;\n" +
			"			}\n" +
			"		}\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			"	<T, U extends T> boolean relatedTypeVariables(Collection<T> c, Iterable<U> s) {\n" +
			"		for (Iterator<?> iterator = s.iterator(); iterator.hasNext();) {\n" +
			"			if (c.contains(iterator.next())) {\n" +
			"				return true;\n" +
			"			}\n" +
			"		}\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			"	<T, U> boolean unrelatedTypeVariables(Collection<T> c, Iterable<U> s) {\n" +
			"		for (Iterator<U> iterator = s.iterator(); iterator.hasNext();) {\n" +
			"			if (c.contains(iterator.next())) { // warning\n" +
			"				return true;\n" +
			"			}\n" +
			"		}\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			"	void all(List<NonFinal> c, Collection<Sub> s, Set<Final> other) {\n" +
			"		c.removeAll(s);\n" +
			"		s.removeAll(c);\n" +
			"		c.removeAll(other); // warning\n" +
			"	}\n" +
			"\n" +
			"	void methodRef(Set<Interface> c, Interface i, OtherInterface o, Final f, NonFinal nf, Sub s) {\n" +
			"		Predicate<Interface> p1 = c::contains;\n" +
			"		BiPredicate<Collection<Interface>, Interface> bp1 = Collection<Interface>::contains;\n" +
			"		Predicate<OtherInterface> p2 = c::contains; // warning\n" +
			"		BiPredicate<Collection<Interface>, OtherInterface> bp2 = Collection<Interface>::contains; // warning\n" +
			"		p1.test(i);\n" +
			"		bp1.test(c, i);\n" +
			"		p2.test(o);\n" +
			"		bp2.test(c, o);\n" +
			"	}\n" +
			"\n" +
			"	void equals(String s, Integer i, Number n) {\n" +
			"		s.equals(i); // info\n" +
			"		i.equals(s); // info\n" +
			"		i.equals(n);\n" +
			"		n.equals(i);\n" +
			"\n" +
			"		Predicate<String> p1 = i::equals; // info\n" +
			"		p1.test(s);\n" +
			"\n" +
			"		BiPredicate<String, Integer> bp2 = Object::equals; // info\n" +
			"		bp2.test(s, i);\n" +
			"\n" +
			"		Objects.equals(s, i); // info\n" +
			"		Objects.equals(i, s); // info\n" +
			"		Objects.equals(n, i);\n" +
			"		Objects.equals(i, n);\n" +
			"\n" +
			"		BiPredicate<String, Integer> bp3 = Objects::equals; // info\n" +
			"		bp3.test(s, i);\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
		},
		"----------\n" +
		"1. WARNING in test\\TestUnlikely.java (at line 30)\n" +
		"	c.remove(o); // warning: unrelated interface\n" +
		"	         ^\n" +
		"Unlikely argument type TestUnlikely.OtherInterface for remove(Object) on a Collection<TestUnlikely.Interface>\n" +
		"----------\n" +
		"2. WARNING in test\\TestUnlikely.java (at line 37)\n" +
		"	c.remove(i); // warning: unrelated interface\n" +
		"	         ^\n" +
		"Unlikely argument type TestUnlikely.Interface for remove(Object) on a Collection<TestUnlikely.OtherInterface>\n" +
		"----------\n" +
		"3. WARNING in test\\TestUnlikely.java (at line 39)\n" +
		"	c.remove(f); // warning: impossible\n" +
		"	         ^\n" +
		"Unlikely argument type TestUnlikely.Final for remove(Object) on a Collection<TestUnlikely.OtherInterface>\n" +
		"----------\n" +
		"4. WARNING in test\\TestUnlikely.java (at line 40)\n" +
		"	c.remove(nf); // warning: castable, but not supertype\n" +
		"	         ^^\n" +
		"Unlikely argument type TestUnlikely.NonFinal for remove(Object) on a Collection<TestUnlikely.OtherInterface>\n" +
		"----------\n" +
		"5. WARNING in test\\TestUnlikely.java (at line 46)\n" +
		"	c.remove(o); // warning: impossible\n" +
		"	         ^\n" +
		"Unlikely argument type TestUnlikely.OtherInterface for remove(Object) on a Collection<TestUnlikely.Final>\n" +
		"----------\n" +
		"6. WARNING in test\\TestUnlikely.java (at line 48)\n" +
		"	c.remove(nf); // warning: impossible\n" +
		"	         ^^\n" +
		"Unlikely argument type TestUnlikely.NonFinal for remove(Object) on a Collection<TestUnlikely.Final>\n" +
		"----------\n" +
		"7. WARNING in test\\TestUnlikely.java (at line 49)\n" +
		"	c.remove(s); // warning: impossible\n" +
		"	         ^\n" +
		"Unlikely argument type TestUnlikely.Sub for remove(Object) on a Collection<TestUnlikely.Final>\n" +
		"----------\n" +
		"8. WARNING in test\\TestUnlikely.java (at line 54)\n" +
		"	c.remove(o); // warning: unrelated interface\n" +
		"	         ^\n" +
		"Unlikely argument type TestUnlikely.OtherInterface for remove(Object) on a Collection<TestUnlikely.NonFinal>\n" +
		"----------\n" +
		"9. WARNING in test\\TestUnlikely.java (at line 55)\n" +
		"	c.remove(f); // warning: impossible\n" +
		"	         ^\n" +
		"Unlikely argument type TestUnlikely.Final for remove(Object) on a Collection<TestUnlikely.NonFinal>\n" +
		"----------\n" +
		"10. WARNING in test\\TestUnlikely.java (at line 63)\n" +
		"	c.remove(f); // warning: impossible\n" +
		"	         ^\n" +
		"Unlikely argument type TestUnlikely.Final for remove(Object) on a Collection<TestUnlikely.Sub>\n" +
		"----------\n" +
		"11. WARNING in test\\TestUnlikely.java (at line 70)\n" +
		"	map.containsKey(value); // warning\n" +
		"	                ^^^^^\n" +
		"Unlikely argument type V for containsKey(Object) on a Map<K,V>\n" +
		"----------\n" +
		"12. WARNING in test\\TestUnlikely.java (at line 71)\n" +
		"	map.containsValue(key); // warning\n" +
		"	                  ^^^\n" +
		"Unlikely argument type K for containsValue(Object) on a Map<K,V>\n" +
		"----------\n" +
		"13. WARNING in test\\TestUnlikely.java (at line 95)\n" +
		"	if (c.contains(iterator.next())) { // warning\n" +
		"	               ^^^^^^^^^^^^^^^\n" +
		"Unlikely argument type U for contains(Object) on a Collection<T>\n" +
		"----------\n" +
		"14. WARNING in test\\TestUnlikely.java (at line 105)\n" +
		"	c.removeAll(other); // warning\n" +
		"	            ^^^^^\n" +
		"Unlikely argument type Set<TestUnlikely.Final> for removeAll(Collection<?>) on a Collection<TestUnlikely.NonFinal>\n" +
		"----------\n" +
		"15. WARNING in test\\TestUnlikely.java (at line 111)\n" +
		"	Predicate<OtherInterface> p2 = c::contains; // warning\n" +
		"	                               ^^^^^^^^^^^\n" +
		"Unlikely argument type TestUnlikely.OtherInterface for contains(Object) on a Collection<TestUnlikely.Interface>\n" +
		"----------\n" +
		"16. WARNING in test\\TestUnlikely.java (at line 112)\n" +
		"	BiPredicate<Collection<Interface>, OtherInterface> bp2 = Collection<Interface>::contains; // warning\n" +
		"	                                                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unlikely argument type TestUnlikely.OtherInterface for contains(Object) on a Collection<TestUnlikely.Interface>\n" +
		"----------\n" +
		"17. INFO in test\\TestUnlikely.java (at line 120)\n" +
		"	s.equals(i); // info\n" +
		"	         ^\n" +
		"Unlikely argument type for equals(): Integer seems to be unrelated to String\n" +
		"----------\n" +
		"18. INFO in test\\TestUnlikely.java (at line 121)\n" +
		"	i.equals(s); // info\n" +
		"	         ^\n" +
		"Unlikely argument type for equals(): String seems to be unrelated to Integer\n" +
		"----------\n" +
		"19. INFO in test\\TestUnlikely.java (at line 125)\n" +
		"	Predicate<String> p1 = i::equals; // info\n" +
		"	                       ^^^^^^^^^\n" +
		"Unlikely argument type for equals(): String seems to be unrelated to Integer\n" +
		"----------\n" +
		"20. INFO in test\\TestUnlikely.java (at line 128)\n" +
		"	BiPredicate<String, Integer> bp2 = Object::equals; // info\n" +
		"	                                   ^^^^^^^^^^^^^^\n" +
		"Unlikely argument type for equals(): Integer seems to be unrelated to String\n" +
		"----------\n" +
		"21. INFO in test\\TestUnlikely.java (at line 131)\n" +
		"	Objects.equals(s, i); // info\n" +
		"	                  ^\n" +
		"Unlikely argument type for equals(): Integer seems to be unrelated to String\n" +
		"----------\n" +
		"22. INFO in test\\TestUnlikely.java (at line 132)\n" +
		"	Objects.equals(i, s); // info\n" +
		"	                  ^\n" +
		"Unlikely argument type for equals(): String seems to be unrelated to Integer\n" +
		"----------\n" +
		"23. INFO in test\\TestUnlikely.java (at line 136)\n" +
		"	BiPredicate<String, Integer> bp3 = Objects::equals; // info\n" +
		"	                                   ^^^^^^^^^^^^^^^\n" +
		"Unlikely argument type for equals(): Integer seems to be unrelated to String\n" +
		"----------\n"
		,
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		customOptions);
}
public void testBug514956a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_COLLECTION_METHOD_ARGUMENT_TYPE, JavaCore.WARNING);
	customOptions.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.ERROR);
	runConformTest(
		new String[] {
			"Unlikely.java",
			"import java.util.Map;\n" +
			"\n" +
			"interface MApplicationElement {}\n" +
			"interface EObject {}\n" +
			"public class Unlikely {\n" +
			"	void m(Map<MApplicationElement, MApplicationElement> map, EObject key) {\n" +
			"		map.get((MApplicationElement)key);\n" +
			"	}\n" +
			"}\n"
		},
		customOptions);
}
public void testBug514956b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_EQUALS_ARGUMENT_TYPE, JavaCore.WARNING);
	customOptions.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.ERROR);
	runConformTest(
		new String[] {
			"Unlikely.java",
			"interface EObject {}\n" +
			"public class Unlikely {\n" +
			"	boolean m(EObject key) {\n" +
			"		return this.equals((Unlikely)key);\n" +
			"	}\n" +
			"}\n"
		},
		customOptions);
}
public void testBug514956c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNLIKELY_EQUALS_ARGUMENT_TYPE, JavaCore.WARNING);
	customOptions.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.ERROR);
	runNegativeTest(
		new String[] {
			"Unlikely.java",
			"interface I1 {}\n" +
			"interface I2 {}\n" +
			"interface I3 {}\n" +
			"public class Unlikely implements I1 {\n" +
			"	boolean m1(I1 i1) {\n" +
			"		return i1.equals((I1)this);\n" + // not a downcast
			"	}\n" +
			"	boolean m2(I1 i1, I2 i2) {\n" +
			"		return i1.equals((I3)i2);\n" + // cast doesn't fix a problem
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Unlikely.java (at line 6)\n" +
		"	return i1.equals((I1)this);\n" +
		"	                 ^^^^^^^^\n" +
		"Unnecessary cast from Unlikely to I1\n" +
		"----------\n" +
		"2. ERROR in Unlikely.java (at line 9)\n" +
		"	return i1.equals((I3)i2);\n" +
		"	                 ^^^^^^\n" +
		"Unnecessary cast from I2 to I3\n" +
		"----------\n" +
		"3. WARNING in Unlikely.java (at line 9)\n" +
		"	return i1.equals((I3)i2);\n" +
		"	                 ^^^^^^\n" +
		"Unlikely argument type for equals(): I3 seems to be unrelated to I1\n" +
		"----------\n",
		null, // classlibs
		false, // flush output dir
		customOptions);
}
// mixture of raw type an parametrized type
public void testBug513310() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runConformTest(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import java.util.List;\n" +
			"import java.util.Set;\n" +
			"\n" +
			"public class Test {\n" +
			"	void f(List dependencyList, Set<Object> set) {\n" +
			"		dependencyList.removeAll(set);\n" +
			"	}\n" +
			"}\n" +
			"",
		}
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/567
// Report unused variable for variables declared in instanceof pattern
public void testGH567() {
	if (this.complianceLevel < ClassFileConstants.JDK21)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" +
				"    record Point (int x, int y) {}\n" +
				"    void foo(Object o) {\n" +
				"        if (o instanceof String s) { int x; }\n" +
				"        if (o instanceof Point (int xVal, int yVal)) {}\n" +  // Should not report as unused locals in 21 - structurally required
				"        switch (o) {\n" +
				"					case String c : \n" + // Should not report as unused local in 21 - structurally required
				"						break;\n" +
				"					default :\n" +
				"							break;\n" +
				"					}" +
				"        if (o instanceof String str) {  str.length();  }\n" + // str refenced.
				"    }\n" +
				"}"
			},
			this.complianceLevel == ClassFileConstants.JDK21 ?

			"----------\n"
			+ "1. WARNING in X.java (at line 4)\n"
			+ "	if (o instanceof String s) { int x; }\n"
			+ "	                        ^\n"
			+ "The value of the local variable s is not used\n"
			+ "----------\n"
			+ "2. WARNING in X.java (at line 4)\n"
			+ "	if (o instanceof String s) { int x; }\n"
			+ "	                                 ^\n"
			+ "The value of the local variable x is not used\n"
			+ "----------\n" :
						"----------\n" +
						"1. WARNING in X.java (at line 4)\n" +
						"	if (o instanceof String s) { int x; }\n" +
						"	                        ^\n" +
						"The value of the local variable s is not used\n" +
						"----------\n" +
						"2. WARNING in X.java (at line 4)\n" +
						"	if (o instanceof String s) { int x; }\n" +
						"	                                 ^\n" +
						"The value of the local variable x is not used\n" +
						"----------\n" +
						"3. WARNING in X.java (at line 5)\n" +
						"	if (o instanceof Point (int xVal, int yVal)) {}\n" +
						"	                            ^^^^\n" +
						"The value of the local variable xVal is not used\n" +
						"----------\n" +
						"4. WARNING in X.java (at line 5)\n" +
						"	if (o instanceof Point (int xVal, int yVal)) {}\n" +
						"	                                      ^^^^\n" +
						"The value of the local variable yVal is not used\n" +
						"----------\n" +
						"5. WARNING in X.java (at line 7)\n" +
						"	case String c : \n" +
						"	            ^\n" +
						"The value of the local variable c is not used\n" +
						"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3051
// [Enhancement] Add warnings for unused patterns
public void testIssue3051() {
	if (this.complianceLevel < ClassFileConstants.JDK22)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"Unused.java",
				"""
				public class Unused {

				    public static void main(String[] args) {
				        record R(int i, long l) {}
				        Object o = null;
				        if (o instanceof String s) {

				        }
				        R r = new R(1, 1);
				        switch (r) {
				        	case R(_, long lvar) -> {}
				        	case R scpatvar -> {}
				        }
				    }
				}
				"""
			},
			"----------\n" +
			"1. WARNING in Unused.java (at line 6)\n" +
			"	if (o instanceof String s) {\n" +
			"	                        ^\n" +
			"The value of the local variable s is not used\n" +
			"----------\n" +
			"2. WARNING in Unused.java (at line 11)\n" +
			"	case R(_, long lvar) -> {}\n" +
			"	               ^^^^\n" +
			"The value of the local variable lvar is not used\n" +
			"----------\n" +
			"3. WARNING in Unused.java (at line 12)\n" +
			"	case R scpatvar -> {}\n" +
			"	       ^^^^^^^^\n" +
			"The value of the local variable scpatvar is not used\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3051
// [Enhancement] Add warnings for unused patterns
public void testIssue3051_2() {
	if (this.complianceLevel < ClassFileConstants.JDK21)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"Unused.java",
				"""
				public class Unused {

				    public static void main(String[] args) {
				        record R(int i, long l, float f) {}
				        Object o = null;
				        if (o instanceof String s) {

				        }
				        R r = new R(1, 1, 1.0f);
				        switch (r) {
				        	case R(int ivar, long lvar, float fvar) -> {
				        										System.out.println(ivar++);
				        										lvar++;
				        								}
				        	case R scpatvar -> {}
				        }
				    }
				}
				"""
			},
			this.complianceLevel == ClassFileConstants.JDK21 ?
					"----------\n" +
					"1. WARNING in Unused.java (at line 6)\n" +
					"	if (o instanceof String s) {\n" +
					"	                        ^\n" +
					"The value of the local variable s is not used\n" +
					"----------\n" :
							"----------\n" +
							"1. WARNING in Unused.java (at line 6)\n" +
							"	if (o instanceof String s) {\n" +
							"	                        ^\n" +
							"The value of the local variable s is not used\n" +
							"----------\n" +
							"2. WARNING in Unused.java (at line 11)\n" +
							"	case R(int ivar, long lvar, float fvar) -> {\n" +
							"	                      ^^^^\n" +
							"The value of the local variable lvar is not used\n" +
							"----------\n" +
							"3. WARNING in Unused.java (at line 11)\n" +
							"	case R(int ivar, long lvar, float fvar) -> {\n" +
							"	                                  ^^^^\n" +
							"The value of the local variable fvar is not used\n" +
							"----------\n" +
							"4. WARNING in Unused.java (at line 15)\n" +
							"	case R scpatvar -> {}\n" +
							"	       ^^^^^^^^\n" +
							"The value of the local variable scpatvar is not used\n" +
							"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3054
// Add warnings for structurally required but otherwise unused local variables
public void testIssue3054() {
	if (this.complianceLevel < ClassFileConstants.JDK21)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	if (this.complianceLevel > ClassFileConstants.JDK21)
		customOptions.put(CompilerOptions.OPTION_ReportUnusedExceptionParameter, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"Unused.java",
				"""
				import java.util.PriorityQueue;
				import java.util.Queue;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;

				public class Unused {
					class Order {}
					static class Resource implements AutoCloseable {
						@Override
						public void close() throws Exception {}
					}

					static int count(Iterable<Order> orders) {
					    int total = 0;
					    for (Order order : orders) // unused variable order
					        total++;
					    return total;
					}

					static void foo() {
						for (int i = 0, se = sideEffect(); i < 10; i++) {  } // unused variable se
					}

					private static int sideEffect() {
						Queue<Integer> q = new PriorityQueue<>();

						while (q.size() >= 3) {
							var x = q.remove();
							var y = q.remove(); // unused variable x
							var z = q.remove(); // unused variable y
							if (x == 10) {
							}
						}
						try {

						} catch (Exception e) { // unused variable e

						} catch (Throwable t) { // unused variable t

						}

						try (var r = new Resource()) {    // unused variable r

						} catch (Exception e) {           // unused variable e

						}

						Stream<String> stream = Stream.of("Hello", "World");
						stream.collect(Collectors.toMap(String::toUpperCase, xyz -> "NODATA")); // unused variable xyz //$NON-NLS-1$

						return 0;
					}
				}
				"""
			},
			this.complianceLevel == ClassFileConstants.JDK21 ?
					"----------\n" +
					"1. WARNING in Unused.java (at line 15)\n" +
					"	for (Order order : orders) // unused variable order\n" +
					"	           ^^^^^\n" +
					"The value of the local variable order is not used\n" +
					"----------\n" +
					"2. WARNING in Unused.java (at line 21)\n" +
					"	for (int i = 0, se = sideEffect(); i < 10; i++) {  } // unused variable se\n" +
					"	                ^^\n" +
					"The value of the local variable se is not used\n" +
					"----------\n" +
					"3. WARNING in Unused.java (at line 29)\n" +
					"	var y = q.remove(); // unused variable x\n" +
					"	    ^\n" +
					"The value of the local variable y is not used\n" +
					"----------\n" +
					"4. WARNING in Unused.java (at line 30)\n" +
					"	var z = q.remove(); // unused variable y\n" +
					"	    ^\n" +
					"The value of the local variable z is not used\n" +
					"----------\n" :
							"----------\n" +
							"1. WARNING in Unused.java (at line 15)\n" +
							"	for (Order order : orders) // unused variable order\n" +
							"	           ^^^^^\n" +
							"The value of the local variable order is not used\n" +
							"----------\n" +
							"2. WARNING in Unused.java (at line 21)\n" +
							"	for (int i = 0, se = sideEffect(); i < 10; i++) {  } // unused variable se\n" +
							"	                ^^\n" +
							"The value of the local variable se is not used\n" +
							"----------\n" +
							"3. WARNING in Unused.java (at line 29)\n" +
							"	var y = q.remove(); // unused variable x\n" +
							"	    ^\n" +
							"The value of the local variable y is not used\n" +
							"----------\n" +
							"4. WARNING in Unused.java (at line 30)\n" +
							"	var z = q.remove(); // unused variable y\n" +
							"	    ^\n" +
							"The value of the local variable z is not used\n" +
							"----------\n" +
							"5. WARNING in Unused.java (at line 36)\n" +
							"	} catch (Exception e) { // unused variable e\n" +
							"	                   ^\n" +
							"The value of the exception parameter e is not used\n" +
							"----------\n" +
							"6. WARNING in Unused.java (at line 38)\n" +
							"	} catch (Throwable t) { // unused variable t\n" +
							"	                   ^\n" +
							"The value of the exception parameter t is not used\n" +
							"----------\n" +
							"7. WARNING in Unused.java (at line 42)\n" +
							"	try (var r = new Resource()) {    // unused variable r\n" +
							"	         ^\n" +
							"The value of the local variable r is not used\n" +
							"----------\n" +
							"8. WARNING in Unused.java (at line 44)\n" +
							"	} catch (Exception e) {           // unused variable e\n" +
							"	                   ^\n" +
							"The value of the exception parameter e is not used\n" +
							"----------\n" +
							"9. WARNING in Unused.java (at line 49)\n" +
							"	stream.collect(Collectors.toMap(String::toUpperCase, xyz -> \"NODATA\")); // unused variable xyz //$NON-NLS-1$\n" +
							"	                                                     ^^^\n" +
							"The value of the lambda parameter xyz is not used\n" +
							"----------\n"
,
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3054
// Add warnings for structurally required but otherwise unused local variables
public void testIssue3054_2() {
	if (this.complianceLevel < ClassFileConstants.JDK21)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLambdaParameter, CompilerOptions.ERROR);
	this.runNegativeTest(
			new String[] {
				"Unused.java",
				"""
				import java.util.PriorityQueue;
				import java.util.Queue;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;

				public class Unused {
					class Order {}
					static class Resource implements AutoCloseable {
						@Override
						public void close() throws Exception {}
					}

					static int count(Iterable<Order> orders) {
					    int total = 0;
					    for (Order order : orders) // unused variable order
					        total++;
					    return total;
					}

					static void foo() {
						for (int i = 0, se = sideEffect(); i < 10; i++) {  } // unused variable se
					}

					private static int sideEffect() {
						Queue<Integer> q = new PriorityQueue<>();

						while (q.size() >= 3) {
							var x = q.remove();
							var y = q.remove(); // unused variable x
							var z = q.remove(); // unused variable y
							if (x == 10) {
							}
						}
						try {

						} catch (Exception e) { // unused variable e

						} catch (Throwable t) { // unused variable t

						}

						try (var r = new Resource()) {    // unused variable r

						} catch (Exception e) {           // unused variable e

						}

						Stream<String> stream = Stream.of("Hello", "World");
						stream.collect(Collectors.toMap(String::toUpperCase, xyz -> "NODATA")); // unused variable xyz //$NON-NLS-1$

						return 0;
					}
				}
				"""
			},
			this.complianceLevel == ClassFileConstants.JDK21 ?
					"----------\n" +
					"1. WARNING in Unused.java (at line 15)\n" +
					"	for (Order order : orders) // unused variable order\n" +
					"	           ^^^^^\n" +
					"The value of the local variable order is not used\n" +
					"----------\n" +
					"2. WARNING in Unused.java (at line 21)\n" +
					"	for (int i = 0, se = sideEffect(); i < 10; i++) {  } // unused variable se\n" +
					"	                ^^\n" +
					"The value of the local variable se is not used\n" +
					"----------\n" +
					"3. WARNING in Unused.java (at line 29)\n" +
					"	var y = q.remove(); // unused variable x\n" +
					"	    ^\n" +
					"The value of the local variable y is not used\n" +
					"----------\n" +
					"4. WARNING in Unused.java (at line 30)\n" +
					"	var z = q.remove(); // unused variable y\n" +
					"	    ^\n" +
					"The value of the local variable z is not used\n" +
					"----------\n" :
							"----------\n" +
							"1. WARNING in Unused.java (at line 15)\n" +
							"	for (Order order : orders) // unused variable order\n" +
							"	           ^^^^^\n" +
							"The value of the local variable order is not used\n" +
							"----------\n" +
							"2. WARNING in Unused.java (at line 21)\n" +
							"	for (int i = 0, se = sideEffect(); i < 10; i++) {  } // unused variable se\n" +
							"	                ^^\n" +
							"The value of the local variable se is not used\n" +
							"----------\n" +
							"3. WARNING in Unused.java (at line 29)\n" +
							"	var y = q.remove(); // unused variable x\n" +
							"	    ^\n" +
							"The value of the local variable y is not used\n" +
							"----------\n" +
							"4. WARNING in Unused.java (at line 30)\n" +
							"	var z = q.remove(); // unused variable y\n" +
							"	    ^\n" +
							"The value of the local variable z is not used\n" +
							"----------\n" +
							"5. WARNING in Unused.java (at line 42)\n" +
							"	try (var r = new Resource()) {    // unused variable r\n" +
							"	         ^\n" +
							"The value of the local variable r is not used\n" +
							"----------\n" +
							"6. ERROR in Unused.java (at line 49)\n" +
							"	stream.collect(Collectors.toMap(String::toUpperCase, xyz -> \"NODATA\")); // unused variable xyz //$NON-NLS-1$\n" +
							"	                                                     ^^^\n" +
							"The value of the lambda parameter xyz is not used\n" +
							"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3054
// Add warnings for structurally required but otherwise unused local variables
public void testIssue3054_3() {
	if (this.complianceLevel < ClassFileConstants.JDK21)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"Unused.java",
				"""
				import java.util.PriorityQueue;
				import java.util.Queue;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;

				public class Unused {
					class Order {}
					static class Resource implements AutoCloseable {
						@Override
						public void close() throws Exception {}
					}

					static int count(Iterable<Order> orders) {
					    int total = 0;
					    for (Order order : orders) // unused variable order
					        total++;
					    return total;
					}

					static void foo() {
						for (int i = 0, se = sideEffect(); i < 10; i++) {  } // unused variable se
					}

					private static int sideEffect() {
						Queue<Integer> q = new PriorityQueue<>();

						while (q.size() >= 3) {
							var x = q.remove();
							var y = q.remove(); // unused variable x
							var z = q.remove(); // unused variable y
							if (x == 10) {
							}
						}
						try {

						} catch (Exception e) { // unused variable e

						} catch (Throwable t) { // unused variable t

						}

						try (var r = new Resource()) {    // unused variable r

						} catch (Exception e) {           // unused variable e

						}

						Stream<String> stream = Stream.of("Hello", "World");
						stream.collect(Collectors.toMap(String::toUpperCase, xyz -> "NODATA")); // unused variable xyz //$NON-NLS-1$

						return 0;
					}
				}
				"""
			},
			this.complianceLevel == ClassFileConstants.JDK21 ?
					"----------\n" +
					"1. WARNING in Unused.java (at line 15)\n" +
					"	for (Order order : orders) // unused variable order\n" +
					"	           ^^^^^\n" +
					"The value of the local variable order is not used\n" +
					"----------\n" +
					"2. WARNING in Unused.java (at line 21)\n" +
					"	for (int i = 0, se = sideEffect(); i < 10; i++) {  } // unused variable se\n" +
					"	                ^^\n" +
					"The value of the local variable se is not used\n" +
					"----------\n" +
					"3. WARNING in Unused.java (at line 29)\n" +
					"	var y = q.remove(); // unused variable x\n" +
					"	    ^\n" +
					"The value of the local variable y is not used\n" +
					"----------\n" +
					"4. WARNING in Unused.java (at line 30)\n" +
					"	var z = q.remove(); // unused variable y\n" +
					"	    ^\n" +
					"The value of the local variable z is not used\n" +
					"----------\n" :
							"----------\n" +
							"1. WARNING in Unused.java (at line 15)\n" +
							"	for (Order order : orders) // unused variable order\n" +
							"	           ^^^^^\n" +
							"The value of the local variable order is not used\n" +
							"----------\n" +
							"2. WARNING in Unused.java (at line 21)\n" +
							"	for (int i = 0, se = sideEffect(); i < 10; i++) {  } // unused variable se\n" +
							"	                ^^\n" +
							"The value of the local variable se is not used\n" +
							"----------\n" +
							"3. WARNING in Unused.java (at line 29)\n" +
							"	var y = q.remove(); // unused variable x\n" +
							"	    ^\n" +
							"The value of the local variable y is not used\n" +
							"----------\n" +
							"4. WARNING in Unused.java (at line 30)\n" +
							"	var z = q.remove(); // unused variable y\n" +
							"	    ^\n" +
							"The value of the local variable z is not used\n" +
							"----------\n" +
							"5. WARNING in Unused.java (at line 42)\n" +
							"	try (var r = new Resource()) {    // unused variable r\n" +
							"	         ^\n" +
							"The value of the local variable r is not used\n" +
							"----------\n" +
							"6. WARNING in Unused.java (at line 49)\n" +
							"	stream.collect(Collectors.toMap(String::toUpperCase, xyz -> \"NODATA\")); // unused variable xyz //$NON-NLS-1$\n" +
							"	                                                     ^^^\n" +
							"The value of the lambda parameter xyz is not used\n" +
							"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3054
// Add warnings for structurally required but otherwise unused local variables
// Check that suppress warning works
public void testIssue3054_4() {
	if (this.complianceLevel < ClassFileConstants.JDK22)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"Unused.java",
				"""
				import java.util.PriorityQueue;
				import java.util.Queue;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				import java.util.Map;
				public class Unused {
					class Order {}
					static class Resource implements AutoCloseable {
						@Override
						public void close() throws Exception {}
					}

					static int count(Iterable<Order> orders) {
					    int total = 0;
					    for (Order order : orders) // unused variable order
					        total++;
					    return total;
					}

					static void foo() {
						for (int i = 0, se = sideEffect(); i < 10; i++) {  } // unused variable se
					}

					private static int sideEffect() {
						Queue<Integer> q = new PriorityQueue<>();

						while (q.size() >= 3) {
							var x = q.remove();
							var y = q.remove(); // unused variable x
							var z = q.remove(); // unused variable y
							if (x == 10) {
							}
						}
						try {

						} catch (Exception e) { // unused variable e

						} catch (Throwable t) { // unused variable t

						}

						try (var r = new Resource()) {    // unused variable r

						} catch (Exception e) {           // unused variable e

						}

						Stream<String> stream = Stream.of("Hello", "World");
						@SuppressWarnings("unused")
						Map<String, String> m = stream.collect(Collectors.toMap(String::toUpperCase, xyz -> "NODATA")); // unused variable xyz //$NON-NLS-1$

						return 0;
					}
				}
				"""
			},
			"----------\n" +
			"1. WARNING in Unused.java (at line 15)\n" +
			"	for (Order order : orders) // unused variable order\n" +
			"	           ^^^^^\n" +
			"The value of the local variable order is not used\n" +
			"----------\n" +
			"2. WARNING in Unused.java (at line 21)\n" +
			"	for (int i = 0, se = sideEffect(); i < 10; i++) {  } // unused variable se\n" +
			"	                ^^\n" +
			"The value of the local variable se is not used\n" +
			"----------\n" +
			"3. WARNING in Unused.java (at line 29)\n" +
			"	var y = q.remove(); // unused variable x\n" +
			"	    ^\n" +
			"The value of the local variable y is not used\n" +
			"----------\n" +
			"4. WARNING in Unused.java (at line 30)\n" +
			"	var z = q.remove(); // unused variable y\n" +
			"	    ^\n" +
			"The value of the local variable z is not used\n" +
			"----------\n" +
			"5. WARNING in Unused.java (at line 42)\n" +
			"	try (var r = new Resource()) {    // unused variable r\n" +
			"	         ^\n" +
			"The value of the local variable r is not used\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3054
// Add warnings for structurally required but otherwise unused local variables
public void testIssue3054_5() {
	if (this.complianceLevel < ClassFileConstants.JDK22)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedExceptionParameter, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"Unused.java",
				"""
				import java.util.PriorityQueue;
				import java.util.Queue;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				import java.util.Map;
				public class Unused {
					class Order {}
					static class Resource implements AutoCloseable {
						@Override
						public void close() throws Exception {}
					}

					static int count(Iterable<Order> orders) {
					    int total = 0;
					    for (Order _ : orders) // unused variable order
					        total++;
					    return total;
					}

					static void foo() {
						for (int i = 0, _ = sideEffect(); i < 10; i++) {  } // unused variable se
					}

					private static int sideEffect() {
						Queue<Integer> q = new PriorityQueue<>();

						while (q.size() >= 3) {
							var x = q.remove();
							var _ = q.remove(); // unused variable x
							var _ = q.remove(); // unused variable y
							if (x == 10) {
							}
						}
						try {

						} catch (Exception _) { // unused variable e

						} catch (Throwable _) { // unused variable t

						}

						try (var _ = new Resource()) {    // unused variable r

						} catch (Exception _) {           // unused variable e

						}

						Stream<String> stream = Stream.of("Hello", "World");

						Map<String, String> m = stream.collect(Collectors.toMap(String::toUpperCase, _ -> "NODATA")); // unused variable xyz //$NON-NLS-1$

						return 0;
					}
				}
				"""
			},
			"----------\n" +
			"1. WARNING in Unused.java (at line 50)\n" +
			"	Map<String, String> m = stream.collect(Collectors.toMap(String::toUpperCase, _ -> \"NODATA\")); // unused variable xyz //$NON-NLS-1$\n" +
			"	                    ^\n" +
			"The value of the local variable m is not used\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions);
}
}