/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for
 *     							bug 236385 - [compiler] Warn for potential programming problem if an object is created but not used
 *      						bug 349326 - [1.7] new warning for missing try-with-resources
 *      						bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
 *								bug 383690 - [compiler] location of error re uninitialized final field should be aligned
 *								bug 391517 - java.lang.VerifyError on code that runs correctly in Eclipse 3.7 and eclipse 3.6
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class FlowAnalysisTest extends AbstractRegressionTest {
static {
//	TESTS_NAMES = new String[] { "testBug380313" };
//	TESTS_NUMBERS = new int[] { 43 };
}
public FlowAnalysisTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public void test001() {
	this.runNegativeTest(new String[] {
		"X.java", // =================
		"public class X {\n" +
		"	public String foo(int i) {\n" +
		"		if (true) {\n" +
		"			return null;\n" +
		"		}\n" +
		"		if (i > 0) {\n" +
		"			return null;\n" +
		"		}\n" +
		"	}	\n" +
		"}\n",
	},
	"----------\n" + 
	"1. ERROR in X.java (at line 2)\n" + 
	"	public String foo(int i) {\n" + 
	"	              ^^^^^^^^^^\n" + 
	"This method must return a result of type String\n" + 
	"----------\n" + 
	"2. WARNING in X.java (at line 6)\n" + 
	"	if (i > 0) {\n" + 
	"			return null;\n" + 
	"		}\n" + 
	"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
	"Dead code\n" + 
	"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test002() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public void test() {\n" +
			"        int c1, c2;\n" +
			"        while ((char) (c1 = 0) == 1) {}\n" +
			"        if (c1 == 0) {} // silent\n" +
			"        if (c2 == 0) {} // complain\n" +
			"    }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (c2 == 0) {} // complain\n" +
		"	    ^^\n" +
		"The local variable c2 may not have been initialized\n" +
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test003() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public void test() {\n" +
			"        int c1, c2;\n" +
			"        while ((char) (c1 = 0) == 1) ;\n" +
			"        if (c1 == 0) {} // silent\n" +
			"        if (c2 == 0) {} // complain\n" +
			"    }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (c2 == 0) {} // complain\n" +
		"	    ^^\n" +
		"The local variable c2 may not have been initialized\n" +
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test004() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public void test() {\n" +
			"        int c1, c2;\n" +
			"        for (;(char) (c1 = 0) == 1;) ;\n" +
			"        if (c1 == 0) {} // silent\n" +
			"        if (c2 == 0) {} // complain\n" +
			"    }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (c2 == 0) {} // complain\n" +
		"	    ^^\n" +
		"The local variable c2 may not have been initialized\n" +
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test005() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public void test() {\n" +
			"        int c1, c2;\n" +
			"        do ; while ((char) (c1 = 0) == 1);\n" +
			"        if (c1 == 0) {} // silent\n" +
			"        if (c2 == 0) {} // complain\n" +
			"    }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (c2 == 0) {} // complain\n" +
		"	    ^^\n" +
		"The local variable c2 may not have been initialized\n" +
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// basic scenario
public void test006() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"    public void test(int p) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            System.out.println(0); // silent because first case\n" +
			"        case 1:\n" +
			"            System.out.println(1); // complain: possible fall-through\n" +
			"            break;\n" +
			"        case 2:\n" +
			"            System.out.println(3); // silent because of break\n" +
			"            return;\n" +
			"        case 3:                            // silent because of return\n" +
			"        case 4:                            // silent because grouped cases\n" +
			"        default:\n" +
			"            System.out.println(\"default\"); //$NON-NLS-1$\n" +
			"        }\n" +
			"    }\n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 6)\n" +
		"	case 1:\n" +
		"	^^^^^^\n" +
		"Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// SuppressWarnings effect - explicit fallthrough token
public void test007() {
	if (this.complianceLevel == ClassFileConstants.JDK1_5) {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.WARNING);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    @SuppressWarnings(\"fallthrough\")\n" +
				"    public void test(int p) {\n" +
				"        switch (p) {\n" +
				"        case 0:\n" +
				"            System.out.println(0); // silent because first case\n" +
				"        case 1:\n" +
				"            System.out.println(1); // silent because of SuppressWarnings\n" +
				"        }\n" +
				"    }\n" +
				"    void foo() {\n" +
				"		Zork z;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null, true, options);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// deep return (1) - fake reachable is seen as reachable
public void test008() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"    public void test(int p) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            System.out.println(0);\n" +
			"            if (true) {\n" +
			"              return;\n" +
			"            }\n" +
			"        case 1:\n" +
			"            System.out.println(1);\n" +
			"        }\n" +
			"    }\n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 9)\n" +
		"	case 1:\n" +
		"	^^^^^^\n" +
		"Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// deep return (2)
public void test009() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public void test(int p, boolean b) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            System.out.println(0);\n" +
			"            if (b) {\n" +
			"              return;\n" +
			"            }\n" +
			"            else {\n" +
			"              return;\n" +
			"            }\n" +
			"        case 1:\n" +
			"            System.out.println(1);\n" +
			"        }\n" +
			"    }\n" +
			"}"
		},
		"",
		null, true, null, options, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// deep return (3), limit: cannot recognize that we won't return
public void test010() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"    public void test(int p, boolean b) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            System.exit(0);\n" +
			"        case 1:\n" + // complain
			"            System.out.println(1);\n" +
			"        }\n" +
			"    }\n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 6)\n" +
		"	case 1:\n" +
		"	^^^^^^\n" +
		"Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// SuppressWarnings effect - implicit, using all token
public void test011() {
	if (this.complianceLevel == ClassFileConstants.JDK1_5) {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.WARNING);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    @SuppressWarnings(\"all\")\n" +
				"    public void test(int p) {\n" +
				"        switch (p) {\n" +
				"        case 0:\n" +
				"            System.out.println(0); // silent because first case\n" +
				"        case 1:\n" +
				"            System.out.println(1); // silent because of SuppressWarnings\n" +
				"        }\n" +
				"    }\n" +
				"	Zork z;\n" + // complain on Zork (unknown type)
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null, true, options);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127730
// [compiler] skip fall-through case warning when the fall-through is documented
// skip because of comment
public void _test012() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public void test(int p) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            System.out.println(0); // silent because first case\n" +
			"            // on purpose fall-through\n" +
			"        case 1:\n" +
			"            System.out.println(1); // silent because of comment alone on its line above \n" +
			"        }\n" +
			"    }\n" +
			"}"
		},
		"",
		null, true, null, options, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127730
// [compiler] skip fall-through case warning when the fall-through is documented
// skip because of comment - default label
public void _test013() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public void test(int p) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            System.out.println(0); // silent because first case\n" +
			"            // on purpose fall-through\n" +
			"        default:\n" +
			"            System.out.println(1); // silent because of comment alone on its line above \n" +
			"        }\n" +
			"    }\n" +
			"}"
		},
		"",
		null, true, null, options, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// basic scenario: default label
public void test014() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"    public void test(int p) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            System.out.println(0); // silent because first case\n" +
						// note: the comment above is not alone on its line, hence it does not
						// protect against fall-through diagnostic
			"        default:\n" +
			"            System.out.println(1); // complain: possible fall-through\n" +
			"        }\n" +
			"    }\n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 6)\n" +
		"	default:\n" +
		"	^^^^^^^\n" +
		"Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// skip because of comment - variants
public void test015() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"    public void test(int p) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            System.out.println(0); // silent because first case\n" +
			"            // on purpose fall-through\n" +
			"\n" + // extraneous line breaks fall-through protection
			"        case 1:\n" +
			"            System.out.println(1); // silent because of comment alone on its line above \n" +
			"        }\n" +
			"    }\n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 8)\n" +
		"	case 1:\n" +
		"	^^^^^^\n" +
		"Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// skip because of comment - variants
public void test016() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"    public void test(int p) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            System.out.println(0); // silent because first case\n" +
			"            // on purpose fall-through\n" +
			"            /* other comment */\n" + // non-single line comment breaks fall-through protection
			"        case 1:\n" +
			"            System.out.println(1); // silent because of comment alone on its line above \n" +
			"        }\n" +
			"    }\n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 8)\n" +
		"	case 1:\n" +
		"	^^^^^^\n" +
		"Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127730
// [compiler] skip fall-through case warning when the fall-through is documented
// skip because of comment - variants
public void _test017() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public void test(int p) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            System.out.println(0);\n" +
			"// on purpose fall-through\n" + // very beginning of line
			"        case 1:\n" +
			"            System.out.println(1); // silent because of comment alone on its line above \n" +
			"        }\n" +
			"    }\n" +
			"}"
		},
		"",
		null, true, null, options, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127730
// [compiler] skip fall-through case warning when the fall-through is documented
// skip because of comment - variants
public void _test018() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public void test(int p) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            System.out.println(0);\n" +
			"            //\n" + // empty line comment alone upon its line
			"        case 1:\n" +
			"            System.out.println(1); // silent because of comment alone on its line above \n" +
			"        }\n" +
			"    }\n" +
			"}"
		},
		"",
		null, true, null, options, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// conditioned break
public void test019() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"    public void test(int p, boolean b) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            if (b) {\n" +
			"              break;\n" +
			"            }\n" +
			"        case 1:\n" +
			"            System.out.println(1); // silent because of comment alone on its line above \n" +
			"        }\n" +
			"    }\n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 8)\n" +
		"	case 1:\n" +
		"	^^^^^^\n" +
		"Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// default reporting is ignore
public void test020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public void test(int p) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            System.out.println(0); // silent because first case\n" +
			"        case 1:\n" +
			"            System.out.println(1); // silent because default level is ignore\n" +
			"        }\n" +
			"    }\n" +
			"	Zork z;\n" + // complain on Zork (unknown type)
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// problem category
public void test021() {
	if (ProblemReporter.getProblemCategory(ProblemSeverities.Warning, IProblem.FallthroughCase) !=
			CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM) {
		fail("bad category for fall-through case problem");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128840
public void test022() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportEmptyStatement, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		if (true)\n" +
			"            ;\n" +
			"        else\n" +
			"            ;\n" +
			"	}\n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 4)\n" +
		"	;\n" +
		"	^\n" +
		"Empty control-flow statement\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	;\n" +
		"	^\n" +
		"Empty control-flow statement\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test023() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		final X x;\n" +
			"		while (true) {\n" +
			"			if (true) {\n" +
			"				break;\n" +
			"			}\n" +
			"			x = new X();\n" +
			"		}\n" +
			"		x.foo();\n" +
			"	}\n" +
			"	public void foo() {\n" +
			"	}\n" +
			"}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 8)\n" + 
		"	x = new X();\n" + 
		"	^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 10)\n" + 
		"	x.foo();\n" + 
		"	^\n" + 
		"The local variable x may not have been initialized\n" + 
		"----------\n",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=132974
public void test024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo(boolean b) {\n" +
			"    final Object l;\n" +
			"    do {\n" +
			"      if (b) {\n" +
			"        l = new Object();\n" +
			"        break;\n" +
			"      }\n" +
			"    } while (false);\n" +
			"    l.toString();\n" +
			"  }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	l.toString();\n" +
		"	^\n" +
		"The local variable l may not have been initialized\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=135602
public void test025() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.print(\"[starting]\");\n" +
			"		X l = new X();\n" +
			"		l.testLoop();\n" +
			"		System.out.println(\"[finished]\");\n" +
			"	}\n" +
			"\n" +
			"	public void testLoop() {\n" +
			"		int loops = 0;\n" +
			"\n" +
			"		do {\n" +
			"			System.out.print(\"[Loop \" + loops + \"]\");\n" +
			"			if (loops > 2) {\n" +
			"				return;\n" +
			"			}\n" +
			"\n" +
			"			if (loops < 4) {\n" +
			"				++loops;\n" +
			"				continue; \n" +
			"			}\n" +
			"		} while (false);\n" +
			"	}\n" +
			"\n" +
			"}\n"
		},
		"[starting][Loop 0][finished]");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=137298
public void test026() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o1) {\n" +
			"    int a00, a01, a02, a03, a04, a05, a06, a07, a08, a09;\n" +
			"    int a10, a11, a12, a13, a14, a15, a16, a17, a18, a19;\n" +
			"    int a20, a21, a22, a23, a24, a25, a26, a27, a28, a29;\n" +
			"    int a30, a31, a32, a33, a34, a35, a36, a37, a38, a39;\n" +
			"    int a40, a41, a42, a43, a44, a45, a46, a47, a48, a49;\n" +
			"    int a50, a51, a52, a53, a54, a55, a56, a57, a58, a59;\n" +
			"    int a60, a61, a62, a63, a64, a65, a66, a67, a68, a69;\n" +
			"    String s;\n" +
			"    Object o2 = o1;\n" +
			"    if (o2 == null) {\n" +
			"      s = \"\";\n" +
			"    }\n" +
			"    System.out.println(s);\n" +
			"  }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 15)\n" +
		"	System.out.println(s);\n" +
		"	                   ^\n" +
		"The local variable s may not have been initialized\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// Non-recursive approach for deep binary expressions. Check that the
// flow analysis doesn't break.
public void test027() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String args[]) {\n" +
			"    String s;\n" +
			"    if (args.length == 0) {\n" +
			"      s = \"s\";\n" +
			"    } else {\n" +
			"      s = args[0];\n" +
			"    }\n" +
			"    System.out.println(s + \"-\" + s + \"-\" + s + \"-\" +\n" +
			"                       s + \"-\" + s + \"-\" + s + \"-\" +\n" +
			"                       s + \"-\" + s + \"-\" + s + \"-\" +\n" +
			"                       s + \"-\" + s + \"-\" + s + \"-\" +\n" +
			"                       s + \"-\" + s + \"-\" + s + \"-\");\n" +
			"  }\n" +
			"}"
		},
		"s-s-s-s-s-s-s-s-s-s-s-s-s-s-s-");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155423
public void test028() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"   {\n" +
			"      if (true) throw new NullPointerException();\n" +
			"   }\n" +
			"}\n" // =================
		},
		"");
	// check no default return opcode is appended
	String expectedOutput =
		"  public X();\n" +
		"     0  aload_0 [this]\n" +
		"     1  invokespecial java.lang.Object() [8]\n" +
		"     4  new java.lang.NullPointerException [10]\n" +
		"     7  dup\n" +
		"     8  invokespecial java.lang.NullPointerException() [12]\n" +
		"    11  athrow\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 1]\n" +
		"        [pc: 4, line: 3]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 12] local: this index: 0 type: X\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155423 - variation
public void test029() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"   {\n" +
			"      if (true) throw new NullPointerException();\n" +
			"   }\n" +
			"   X() {\n" +
			"      System.out.println();\n" +
			"   }\n" +
			"}\n", // =================
		},
		"");
	// check no default return opcode is appended
	String expectedOutput =
		"  // Method descriptor #6 ()V\n" +
		"  // Stack: 2, Locals: 1\n" +
		"  X();\n" +
		"     0  aload_0 [this]\n" +
		"     1  invokespecial java.lang.Object() [8]\n" +
		"     4  new java.lang.NullPointerException [10]\n" +
		"     7  dup\n" +
		"     8  invokespecial java.lang.NullPointerException() [12]\n" +
		"    11  athrow\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 5]\n" +
		"        [pc: 4, line: 3]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 12] local: this index: 0 type: X\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155423 - variation
public void test030() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"class Y {\n" +
			"	Y(Object o) {\n" +
			"		System.out.print(o);\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"public class X extends Y {\n" +
			"	{\n" +
			"		if (true)\n" +
			"			throw new NullPointerException();\n" +
			"	}\n" +
			"\n" +
			"	X() {\n" +
			"		super(new Object() {\n" +
			"			public String toString() {\n" +
			"				return \"SUCCESS:\";\n" +
			"			}\n" +
			"		});\n" +
			"		System.out.println();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			new X();\n" +
			"		} catch(NullPointerException e) {\n" +
			"			System.out.println(\"caught:NPE\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"SUCCESS:caught:NPE");
	// check no default return opcode is appended
	String expectedOutput =
		"  // Method descriptor #6 ()V\n" +
		"  // Stack: 3, Locals: 1\n" +
		"  X();\n" +
		"     0  aload_0 [this]\n" +
		"     1  new X$1 [8]\n" +
		"     4  dup\n" +
		"     5  invokespecial X$1() [10]\n" +
		"     8  invokespecial Y(java.lang.Object) [12]\n" +
		"    11  new java.lang.NullPointerException [15]\n" +
		"    14  dup\n" +
		"    15  invokespecial java.lang.NullPointerException() [17]\n" +
		"    18  athrow\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 14]\n" +
		"        [pc: 11, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 19] local: this index: 0 type: X\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155423 - variation
public void test031() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class Y {\n" +
			"	Y(Object o) {\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"public class X extends Y {\n" +
			"	final int blank;\n" +
			"	{\n" +
			"		if (true)\n" +
			"			throw new NullPointerException();\n" +
			"	}\n" +
			"\n" +
			"	X() {\n" +
			"		super(new Object() {});\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155423 - variation
public void test032() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class Y {\n" +
			"	Y(int i) {\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"public class X extends Y {\n" +
			"	final int blank;\n" +
			"	{\n" +
			"		if (true)\n" +
			"			throw new NullPointerException();\n" +
			"	}\n" +
			"\n" +
			"	X() {\n" +
			"		super(blank = 0);\n" +
			"	}\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 14)\n" +
		"	super(blank = 0);\n" +
		"	      ^^^^^\n" +
		"Cannot refer to an instance field blank while explicitly invoking a constructor\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155423 - variation
public void test033() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class Y {\n" +
			"	Y(int i) {\n" +
			"	}\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	final int blank;\n" +
			"	{\n" +
			"		if (true)\n" +
			"			throw new NullPointerException();\n" +
			"	}\n" +
			"	X() {\n" +
			"		super(0);\n" +
			"		blank = 0;\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162918
public void test034() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo1() {\n" +
			"    switch (1) {\n" +
			"    case 0:\n" +
			"      final int i = 1;\n" +
			"    case i: // should complain: i not initialized\n" +
			"      System.out.println(i); // should complain: i not initialized\n" +
			"    }\n" +
			"  }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	case i: // should complain: i not initialized\n" +
		"	     ^\n" +
		"The local variable i may not have been initialized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	System.out.println(i); // should complain: i not initialized\n" +
		"	                   ^\n" +
		"The local variable i may not have been initialized\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162918
// variant
public void test035() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo2() {\n" +
			"    switch (1) {\n" +
			"    case 0:\n" +
			"      int j = 0;\n" +
			"    case 1:\n" +
			"      System.out.println(j); // should complain: j not initialized\n" +
			"    }\n" +
			"  }\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	System.out.println(j); // should complain: j not initialized\n" +
		"	                   ^\n" +
		"The local variable j may not have been initialized\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162918
// variant - not a flow analysis issue per se, contrast with 34 and 35 above
public void test036() {
	String src =
		"public class X {\n" +
		"  void foo3() {\n" +
		"    switch (1) {\n" +
		"    case 0:\n" +
		"      class Local {\n" +
		"      }\n" +
		"      ;\n" +
		"    case 1:\n" +
		"      new Local();\n" +  // complain for compliance >= 1.4
		"    }\n" +
		"  }\n" +
		"}";
	if (this.complianceLevel <= ClassFileConstants.JDK1_3) {
		this.runConformTest(
				new String[] {
					"X.java",
					src
				},
				""
			);
	} else {
		this.runNegativeTest(
			new String[] {
				"X.java",
				src
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	new Local();\n" +
			"	    ^^^^^\n" +
			"Local cannot be resolved to a type\n" +
			"----------\n");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166641
public void test037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    if (false) {\n" +
			"      String s;\n" +
			"      System.out.println(s);\n" +
			"    }\n" +
			"  }\n" +
			"}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	if (false) {\n" + 
		"      String s;\n" + 
		"      System.out.println(s);\n" + 
		"    }\n" + 
		"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	System.out.println(s);\n" + 
		"	                   ^\n" + 
		"The local variable s may not have been initialized\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166641
// variant: the declaration is outside of the fake reachable block
public void test038() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    String s;\n" +
			"    if (false) {\n" +
			"      System.out.println(s);\n" +
			"    }\n" +
			"  }\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166641
// variant with deeper nesting
public void test039() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    if (false) {\n" +
			"      String s;\n" +
			"      if (System.out != null) {\n" +
			"        System.out.println(s);\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	if (false) {\n" + 
		"      String s;\n" + 
		"      if (System.out != null) {\n" + 
		"        System.out.println(s);\n" + 
		"      }\n" + 
		"    }\n" + 
		"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 6)\n" + 
		"	System.out.println(s);\n" + 
		"	                   ^\n" + 
		"The local variable s may not have been initialized\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166641
// variant - checking duplicate initialization of final variables
public void test040() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    final String s = \"\";\n" +
			"    if (false) {\n" +
			"      s = \"\";\n" +
			"    }\n" +
			"  }\n" +
			"}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 4)\n" + 
		"	if (false) {\n" + 
		"      s = \"\";\n" + 
		"    }\n" + 
		"	           ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	s = \"\";\n" + 
		"	^\n" + 
		"The final local variable s cannot be assigned. It must be blank and not using a compound assignment\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166641
// variant - checking duplicate initialization of final variables
public void test041() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    final String s;\n" +
			"    s = \"\";\n" +
			"    if (false) {\n" +
			"      s = \"\";\n" +
			"    }\n" +
			"  }\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166641
// variant - checking duplicate initialization of final variables
public void test042() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    final String s;\n" +
			"    if (false) {\n" +
			"      s = \"\";\n" +
			"    }\n" +
			"    s = \"\";\n" +
			"  }\n" +
			"}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 4)\n" + 
		"	if (false) {\n" + 
		"      s = \"\";\n" + 
		"    }\n" + 
		"	           ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	s = \"\";\n" + 
		"	^\n" + 
		"The final local variable s may already have been assigned\n" + 
		"----------\n");
}
// switch and definite assignment
public void test043() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public abstract class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    for (int i = 0; i < 3; i++) {\n" +
			"      System.out.print(i);\n" +
			"      switch (i) {\n" +
			"        case 1:\n" +
			"          final int j;\n" +
			"          j = 1;\n" +
			"          System.out.println(j);\n" +
			"          break;\n" +
			"        case 2:\n" +
			"          j = 2;\n" +
			"          System.out.println(j);\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n",
		},
		"011\n22");
}
// switch and definite assignment
public void test044() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public abstract class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    for (int i = 0; i < 3; i++) {\n" +
			"      System.out.print(i);\n" +
			"      switch (i) {\n" +
			"        case 1:\n" +
			"          final int j = 1;\n" +
			"          System.out.println(j);\n" +
			"          break;\n" +
			"        case 2:\n" +
			"          j = 2;\n" +
			"          System.out.println(j);\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	j = 2;\n" +
		"	^\n" +
		"The final local variable j cannot be assigned. It must be blank and not using a compound assignment\n" +
		"----------\n");
}
// switch and definite assignment
// **
public void test045() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public abstract class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    switch (args.length) {\n" +
			"      case 1:\n" +
			"        final int j = 1;\n" +
			"      case 2:\n" +
			"        switch (5) {\n" +
			"          case j:\n" +
			"        }\n" +
			"    }\n" +
			"  }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	case j:\n" +
		"	     ^\n" +
		"The local variable j may not have been initialized\n" +
		"----------\n");
}
// for and definite assignment
public void test046() {
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"public abstract class X {\n" +
			"  public static void main(String args[]) {\n" +
			"    for (final int i; 0 < (i = 1); i = i + 1) {\n" +
			"      System.out.println(i);\n" +
			"      break;\n" +
			"    }\n" +
			"  }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	for (final int i; 0 < (i = 1); i = i + 1) {\n" + 
		"	                               ^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n",
		"1",
		"",
		JavacTestOptions.JavacHasABug.JavacBug4660984);
}
// do while and named labels
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// variant
public void test047() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() {\n" +
			"    done: do\n" +
			"      break done;\n" +
			"    while (false);\n" +
			"    System.out.println();\n" +
			"  }\n" +
			"}\n",
		},
		"");
}
// labeled loop
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200158
// contrast this with test049
public void test048() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  private static final boolean b = false;\n" +
			"  public Object foo() {\n" +
			"    if (b) {\n" +
			"      label: while (bar()) {\n" +
			"      }\n" +
			"      return null;\n" +
			"    }\n" +
			"    return null;\n" +
			"  }\n" +
			"  boolean bar() {\n" +
			"    return false;\n" +
			"  }\n" +
			"}\n"
			},
		false /* expectingCompilerErrors */,
		"----------\n" + 
		"1. WARNING in X.java (at line 5)\n" + 
		"	label: while (bar()) {\n" + 
		"	^^^^^\n" + 
		"The label label is never explicitly referenced\n" + 
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		"" /* expectedErrorString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// labeled loop
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200158
// variant: this one passes
public void test049() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  private static final boolean b = false;\n" +
			"  public Object foo() {\n" +
			"    if (b) {\n" +
			"      while (bar()) {\n" +
			"      }\n" +
			"      return null;\n" +
			"    }\n" +
			"    return null;\n" +
			"  }\n" +
			"  boolean bar() {\n" +
			"    return false;\n" +
			"  }\n" +
			"}\n"
			},
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		"" /* expectedErrorString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=235781
public void test050_definite_assigment_and_if_true() {
	runConformTest(
		// test directory preparation
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"  final int i;\n" +
			"  X() {\n" +
			"    if (true) {\n" +
			"      throw new NullPointerException();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=235781
// variant
public void test051_definite_assigment_and_if_true() {
	runConformTest(
		// test directory preparation
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"  X() {\n" +
			"    final int i;\n" +
			"    if (true) {\n" +
			"      throw new NullPointerException();\n" +
			"    }\n" +
			"    System.out.println(i);\n" +
			"  }\n" +
			"}\n"
		}
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399
public void test052() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"public class X {\n" + 
			"	void foo(boolean b) {\n" + 
			"		if (b && false) {\n" + 
			"			int i = 0; // deadcode\n" + 
			"			return;  // 1\n" + 
			"		}\n" + 
			"		return;\n" +
			"		return;\n" +
			"	}\n" +
			"}	\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	if (b && false) {\n" + 
		"			int i = 0; // deadcode\n" + 
		"			return;  // 1\n" + 
		"		}\n" + 
		"	                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	return;\n" + 
		"	^^^^^^^\n" + 
		"Unreachable code\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399 - variation
public void test053() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"public class X {\n" + 
			"	void foo(boolean b) {\n" + 
			"		if (false && b) {\n" + 
			"			int j = 0; // deadcode\n" + 
			"			return; // 2\n" + 
			"		}\n" + 
			"		return;\n" +
			"		return;\n" +
			"	}\n" +
			"}	\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	if (false && b) {\n" + 
		"	             ^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 3)\n" + 
		"	if (false && b) {\n" + 
		"			int j = 0; // deadcode\n" + 
		"			return; // 2\n" + 
		"		}\n" + 
		"	                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 8)\n" + 
		"	return;\n" + 
		"	^^^^^^^\n" + 
		"Unreachable code\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399 - variation
public void test054() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"public class X {\n" + 
			"	void foo(boolean b) {\n" + 
			"		while (true) {\n" + 
			"			if (true) break;\n" + 
			"			int k = 0; // deadcode\n" + 
			"		}\n" + 
			"		return;\n" +
			"		return;\n" +
			"	}\n" +
			"}	\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 5)\n" + 
		"	int k = 0; // deadcode\n" + 
		"	^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	return;\n" + 
		"	^^^^^^^\n" + 
		"Unreachable code\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399 - variation
public void test055() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"public class X {\n" + 
			"	void foo(boolean b) {\n" + 
			"		if (true || b) {\n" + 
			"			int l = 0; // deadcode\n" + 
			"			return; // 2a\n" + 
			"		}		\n" + 
			"		return;\n" +
			"		return;\n" +
			"	}\n" +
			"}	\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	if (true || b) {\n" + 
		"	            ^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 7)\n" + 
		"	return;\n" + 
		"	^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 8)\n" + 
		"	return;\n" + 
		"	^^^^^^^\n" + 
		"Unreachable code\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399 - variation
public void test056() {
	if (this.complianceLevel < ClassFileConstants.JDK1_4) {
		runNegativeTest(
			new String[] { /* test files */
				"X.java",
				"public class X {\n" + 
				"	void bar() {\n" + 
				"		return;\n" + 
				"		{\n" + 
				"			return; // 3\n" + 
				"		}\n" + 
				"	}\n" + 
				"	void baz() {\n" + 
				"		return;\n" + 
				"		{\n" + 
				"		}\n" + 
				"	}	\n" + 
				"	void baz2() {\n" + 
				"		return;\n" + 
				"		; // 4\n" + 
				"	}	\n" + 
				"}	\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	{\n" + 
			"			return; // 3\n" + 
			"		}\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unreachable code\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	{\n" + 
			"		}\n" + 
			"	^^^^^\n" + 
			"Unreachable code\n" + 
			"----------\n");
		return;
	}
	runNegativeTest(
			new String[] { /* test files */
				"X.java",
				"public class X {\n" + 
				"	void bar() {\n" + 
				"		return;\n" + 
				"		{\n" + 
				"			return; // 3\n" + 
				"		}\n" + 
				"	}\n" + 
				"	void baz() {\n" + 
				"		return;\n" + 
				"		{\n" + 
				"		}\n" + 
				"	}	\n" + 
				"	void baz2() {\n" + 
				"		return;\n" + 
				"		; // 4\n" + 
				"	}	\n" + 
				"}	\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	{\n" + 
			"			return; // 3\n" + 
			"		}\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unreachable code\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	{\n" + 
			"		}\n" + 
			"	^^^^^\n" + 
			"Unreachable code\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 15)\n" + 
			"	; // 4\n" + 
			"	^\n" + 
			"Unreachable code\n" + 
			"----------\n");	
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110544
public void test057() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"public class X {\n" + 
			"	void foo(int x, int[] array) {\n" + 
			"		for (int i = 0; \n" + 
			"		     i < array.length; \n" + 
			"		     i++) {//dead code\n" + 
			"			if (x == array[i])\n" + 
			"				return;\n" + 
			"			else\n" + 
			"				break;\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 5)\n" + 
		"	i++) {//dead code\n" + 
		"	^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 9)\n" + 
		"	break;\n" + 
		"	^^^^^^\n" + 
		"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399 - variation
public void test058() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"public class X {\n" + 
			"	void foo() {\n" + 
			"		if (false) {\n" + 
			"			class Local {\n" + 
			"				int i = 12;\n" +
			"				{   i++; }\n" +
			"				void method() {\n" + 
			"					if (false)\n" + 
			"						System.out.println();\n" + 
			"					return;\n" + 
			"					return;\n" + 
			"				}\n" + 
			"			}\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	if (false) {\n" + 
		"			class Local {\n" + 
		"				int i = 12;\n" + 
		"				{   i++; }\n" + 
		"				void method() {\n" + 
		"					if (false)\n" + 
		"						System.out.println();\n" + 
		"					return;\n" + 
		"					return;\n" + 
		"				}\n" + 
		"			}\n" + 
		"		}\n" + 
		"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 4)\n" + 
		"	class Local {\n" + 
		"	      ^^^^^\n" + 
		"The type Local is never used locally\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 7)\n" + 
		"	void method() {\n" + 
		"	     ^^^^^^^^\n" + 
		"The method method() from the type Local is never used locally\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 11)\n" + 
		"	return;\n" + 
		"	^^^^^^^\n" + 
		"Unreachable code\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399 - variation
public void test059() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"public class X {\n" + 
			"	void foo(boolean b) {\n" + 
			"		int i = false && b ? 0 : 1;\n" + 
			"		if (false) {\n" + 
			"			int j = false && b ? 0 : 1;\n" + 
			"		}\n" + 
			"		return;\n" + 
			"		return;\n" + 
			"	}\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	int i = false && b ? 0 : 1;\n" + 
		"	                 ^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 3)\n" + 
		"	int i = false && b ? 0 : 1;\n" + 
		"	                     ^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 4)\n" + 
		"	if (false) {\n" + 
		"			int j = false && b ? 0 : 1;\n" + 
		"		}\n" + 
		"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 8)\n" + 
		"	return;\n" + 
		"	^^^^^^^\n" + 
		"Unreachable code\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399 - variation
public void test060() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"public class X {\n" + 
			"	static final boolean DEBUG = false;\n" + 
			"	static final int DEBUG_LEVEL = 0;\n" + 
			"	boolean check() { return true; }\n" + 
			"	void foo(boolean b) {\n" + 
			"		if (DEBUG)\n" + 
			"			System.out.println(\"fake reachable1\"); //$NON-NLS-1$\n" + 
			"		if (DEBUG && b)\n" + 
			"			System.out.println(\"fake reachable2\"); //$NON-NLS-1$\n" + 
			"		if (DEBUG && check())\n" + 
			"			System.out.println(\"fake reachable3\"); //$NON-NLS-1$\n" + 
			"		if (b && DEBUG)\n" + 
			"			System.out.println(\"fake reachable4\"); //$NON-NLS-1$\n" + 
			"		if (check() && DEBUG)\n" + 
			"			System.out.println(\"fake reachable5\"); //$NON-NLS-1$\n" + 
			"		if (DEBUG_LEVEL > 1) \n" + 
			"			System.out.println(\"fake reachable6\"); //$NON-NLS-1$\n" + 
			"		return;\n" + 
			"		return;\n" + 
			"	}\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 8)\n" + 
		"	if (DEBUG && b)\n" + 
		"	             ^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 9)\n" + 
		"	System.out.println(\"fake reachable2\"); //$NON-NLS-1$\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 10)\n" + 
		"	if (DEBUG && check())\n" + 
		"	             ^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 11)\n" + 
		"	System.out.println(\"fake reachable3\"); //$NON-NLS-1$\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"5. WARNING in X.java (at line 13)\n" + 
		"	System.out.println(\"fake reachable4\"); //$NON-NLS-1$\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"6. WARNING in X.java (at line 15)\n" + 
		"	System.out.println(\"fake reachable5\"); //$NON-NLS-1$\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"7. WARNING in X.java (at line 17)\n" + 
		"	System.out.println(\"fake reachable6\"); //$NON-NLS-1$\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"8. ERROR in X.java (at line 19)\n" + 
		"	return;\n" + 
		"	^^^^^^^\n" + 
		"Unreachable code\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=265962
public void test061() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"        private static final boolean isIS() {\n" + 
			"                return System.currentTimeMillis()<0 ;\n" + 
			"        }\n" + 
			"        public static void main(String[] args) {\n" + 
			"                do {\n" + 
			"                        return;\n" + 
			"                } while(isIS() && false);\n" + 
			"        }\n" + 
			"}\n", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  public static void main(java.lang.String[] args);\n" + 
		"    0  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 7]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 1] local: args index: 0 type: java.lang.String[]\n";

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

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=265962 - variation
public void test062() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"public class X {\n" + 
			"        private static final boolean isIS() {\n" + 
			"                return System.currentTimeMillis()<0 ;\n" + 
			"        }\n" + 
			"        public static void main(String[] args) {\n" + 
			"                do {\n" + 
			"                        return;\n" + 
			"                } while(isIS() && false);\n" + 
			"                return;\n" + 
			"        }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	return;\n" + 
		"	^^^^^^^\n" + 
		"Unreachable code\n" + 
		"----------\n");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236385
public void test063() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"   boolean bar() { return false; } \n" +
			"	public void foo() {" +
			"		if (bar())\n" +
			"			new IllegalArgumentException(\"You must not bar!\");\n" +
			"	}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	new IllegalArgumentException(\"You must not bar!\");\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The allocated object is never used\n" + 
		"----------\n",
		null /* classLibraries */,
		true /* shouldFlushOutputDirectory */,
		compilerOptions);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236385
// non-throwable type
public void test064() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"   boolean bar() { return false; } \n" +
			"	public void foo() {" +
			"		if (bar())\n" +
			"			new String(\"You must not bar!\");\n" +
			"	}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	new String(\"You must not bar!\");\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The allocated object is never used\n" + 
		"----------\n",
		null /* classLibraries */,
		true /* shouldFlushOutputDirectory */,
		compilerOptions);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236385
// warning suppressed
public void test065() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.WARNING);
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"   boolean bar() { return false; }\n" +
			"   @SuppressWarnings(\"unused\")\n" +
			"	public void foo() {" +
			"		if (bar())\n" +
			"			new IllegalArgumentException(\"You must not bar!\");\n" +
			"	}\n" +
			"}",
		},
		"" /* expectedOutputString */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */, 
		null /* vmArguments */, 
		compilerOptions /* customOptions */,
		null /* clientRequestor */);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236385
// warning ignored (default)
public void test066() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"   boolean bar() { return false; }\n" +
			"	public void foo() {" +
			"		if (bar())\n" +
			"			new IllegalArgumentException(\"You must not bar!\");\n" +
			"	}\n" +
			"}",
		},
		"" /* expectedOutputString */);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236385
// instance is assigned
public void test067() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"   boolean bar() { return false; }\n" +
			"   Throwable t;\n" +
			"	public void foo() {" +
			"		t = new IllegalArgumentException(\"You must not bar!\");\n" +
			"	}\n" +
			"}",
		},
		"" /* expectedOutputString */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */, 
		null /* vmArguments */, 
		compilerOptions /* customOptions */,
		null /* clientRequestor */);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236385
// method invoked
public void test068() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"   boolean bar() { return false; }\n" +
			"	public void foo() {" +
			"		if (bar())\n" +
			"			new IllegalArgumentException(\"You must not bar!\").printStackTrace();\n" +
			"	}\n" +
			"}",
		},
		"" /* expectedOutputString */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */, 
		null /* vmArguments */, 
		compilerOptions /* customOptions */,
		null /* clientRequestor */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236385
//anonymous type
public void test069() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"   boolean bar() { return false; } \n" +
			"	public void foo() {" +
			"		if (bar())\n" +
			"			new Object() {};\n" +
			"	}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	new Object() {};\n" + 
		"	^^^^^^^^^^^^^^^\n" + 
		"The allocated object is never used\n" + 
		"----------\n",
		null /* classLibraries */,
		true /* shouldFlushOutputDirectory */,
		compilerOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322154
public void test070() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"public final class X {\n" +
			"    private X (){\n" +
			"        boolean flagSet = true;\n" +
			"        Object first = true ? null : \"\";        \n" +
			"        Object second = flagSet || first == null ? null :\n" +
			"            new Object() {};\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 4)\n" + 
		"	Object first = true ? null : \"\";        \n" + 
		"	                             ^^\n" + 
		"Dead code\n" + 
		"----------\n",
		null /* classLibraries */,
		true /* shouldFlushOutputDirectory */,
		compilerOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324154
public void test071() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"  static {\n" +
			"    try {\n" +
			"      while(true) {\n" +
			"          if (true)\n" +
			"              throw new NumberFormatException();\n" +
			"          else\n" +
			"              throw new IOException();\n" +
			"      }\n" +
			"    } catch(IOException e ) {\n" +
			"        // empty\n" +
			"    } \n" +
			"  } \n" +
			"}\n",
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 9)\n" + 
		"	throw new IOException();\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 9)\n" + 
		"	throw new IOException();\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338234
// Warn uninitialized variable in deadcode if deadcode has been inferred
// by null analysis
public void testBug338234a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" + 
			"        int i;\n" + 
			"        String str = null;\n" + 
			"        if (str != null)\n" + 
			"            i++;    \n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 6)\n" + 
		"	i++;    \n" + 
		"	^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 6)\n" + 
		"	i++;    \n" + 
		"	^\n" + 
		"The local variable i may not have been initialized\n" + 
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338234
// Don't warn uninitialized variable in deadcode if deadcode has not been inferred
// by null analysis
public void testBug338234b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" + 
			"        int i;\n" + 
			"        l: {\n" +
			"			if(false)\n" +
			"				break l;\n" + 
			"        	return;\n" +
			"		 }\n" + 
			"        i++;    \n" + 
			"    }\n" + 
			"}\n"
		},
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338234
// Warn uninitialized field in deadcode if deadcode has been inferred
// by null analysis
public void testBug338234c() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public final int field1;\n" +
			"    {\n" + 
			"        int i;\n" + 
			"        String str = null;\n" +
			"		 if(str != null)\n" +
			"			i = field1;\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public final int field1;\n" + 
		"	                 ^^^^^^\n" + 
		"The blank final field field1 may not have been initialized\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 7)\n" + 
		"	i = field1;\n" + 
		"	^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 7)\n" + 
		"	i = field1;\n" + 
		"	    ^^^^^^\n" + 
		"The blank final field field1 may not have been initialized\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338234
// Warn uninitialized field in deadcode if deadcode has been inferred
// by null analysis
public void testBug338234d() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo(boolean b) {\n" +
			"        int i;\n" +
			"		 String str = null;\n" + 
			"        if(b){\n" +
			"		 	if(str == null)\n" +
			"				return;\n" +
			"		 } else {\n" +
			"			i = 2;\n" +
			"		 }\n" +
			"		 i++;\n" + 
			"    }\n" + 
			"}\n"
		}, 
		"----------\n" + 
		"1. ERROR in X.java (at line 11)\n" + 
		"	i++;\n" + 
		"	^\n" + 
		"The local variable i may not have been initialized\n" + 
		"----------\n");
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// variant < 1.7 using Closeable: not closed
public void testCloseable1() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.File;\n" + 
				"import java.io.FileReader;\n" + 
				"import java.io.IOException;\n" + 
				"public class X {\n" +
				"    void foo() throws IOException {\n" +
				"        File file = new File(\"somefile\");\n" + 
				"        FileReader fileReader = new FileReader(file); // not closed\n" + 
				"        char[] in = new char[50];\n" + 
				"        fileReader.read(in);\n" + 
				"    }\n" + 
				"}\n"
			}, 
			"----------\n" + 
			"1. WARNING in X.java (at line 7)\n" + 
			"	FileReader fileReader = new FileReader(file); // not closed\n" + 
			"	           ^^^^^^^^^^\n" + 
			"Resource leak: 'fileReader' is never closed\n" + 
			"----------\n",
			null, true, options);	
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// variant < 1.7 using Closeable: resource is closed, cannot suggest try-with-resources < 1.7
public void testCloseable2() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.File;\n" + 
				"import java.io.FileReader;\n" + 
				"import java.io.IOException;\n" + 
				"public class X {\n" +
				"    void foo() throws IOException {\n" +
				"        File file = new File(\"somefile\");\n" + 
				"        FileReader fileReader = new FileReader(file); // not closed\n" + 
				"        char[] in = new char[50];\n" + 
				"        fileReader.read(in);\n" +
				"        fileReader.close();\n" + 
				"    }\n" + 
				"}\n"
			}, 
			"",
			null, true, null, options, null);	
}
// Bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
// return/break/continue inside anonymous class inside try-catch inside initializer
public void testLocalClassInInitializer1() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    static {\n" +
				"        final int i=4;\n" +
				"        try {\n" +
				"            Runnable runner = new Runnable() {\n" +
				"                public void run() {\n" +
				"                    switch (i) {" +
				"                        case 4: break;\n" +
				"                    }\n" +
				"                    int j = i;\n" +
				"                    while (j++ < 10) {\n" +
				"                        if (j == 2) continue;\n" +
				"                        if (j == 4) break;\n" +
				"                        if (j == 6) return;\n" +
				"                    }\n" +
				"                }\n" +
				"            };\n" +
				"        } catch (RuntimeException re) {}\n" +
				"    }\n" +
				"}\n"
			}, 
			"");
}
// Bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
// break/continue illegally inside anonymous class inside loop (loop is out of scope for break/continue)
public void testLocalClassInInitializer2() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    void f () {\n" +
				"        while (true) {\n" +
				"            class Inner1 {\n" +
				"                { if (true) break; }\n" +
				"            }\n" +
				"            new Inner1();\n" +
				"        }\n" +
				"    } \n" +
				"    void g () {\n" +
				"        outer: for (int i=1;true;i++) {\n" +
				"            class Inner2 {\n" +
				"                int j = 3;\n" +
				"                void foo () {\n" +
				"                  if (2 == j) continue outer;\n" +
				"                  else continue;\n" +
				"                }\n" +
				"            }\n" +
				"            new Inner2().foo();\n" +
				"        }\n" +
				"    } \n" +
				"}\n"
			}, 
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	{ if (true) break; }\n" + 
			"	            ^^^^^^\n" + 
			"break cannot be used outside of a loop or a switch\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 11)\n" + 
			"	outer: for (int i=1;true;i++) {\n" + 
			"	^^^^^\n" + 
			"The label outer is never explicitly referenced\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 15)\n" + 
			"	if (2 == j) continue outer;\n" + 
			"	            ^^^^^^^^^^^^^^^\n" + 
			"The label outer is missing\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 16)\n" + 
			"	else continue;\n" + 
			"	     ^^^^^^^^^\n" + 
			"continue cannot be used outside of a loop\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380313
// Verify that the code runs fine with all compliance levels.
public void testBug380313() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"public void foo() throws Exception {\n" + 
				"        int i = 1;\n" + 
				"        int j = 2;\n" + 
				"        try {\n" + 
				"            if ((bar() == 1)) {\n" + 
				"                if ((i == 1)) {\n" + 
				"                    int n = bar();\n" + 
				"                    if (n == 35) {\n" + 
				"                        j = 2;\n" + 
				"                    } else {\n" + 
				"                        if (bar() > 0)\n" + 
				"                            return;\n" + 
				"                    }\n" + 
				"                } else {\n" + 
				"                    throw new Exception();\n" + 
				"                }\n" + 
				"            } else {\n" + 
				"                throw new Exception();\n" + 
				"            }\n" + 
				"            if (bar() == 0)\n" + 
				"                return;\n" + 
				"        } finally {\n" + 
				"            bar();\n" + 
				"        }\n" + 
				"    }\n" + 
				"\n" + 
				"    private int bar() {\n" + 
				"        return 0;\n" + 
				"    }\n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"    }\n" +
				"}\n"
			}, 
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380313
// try with resources
// Verify that the code runs fine with all compliance levels.
public void testBug380313b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.FileInputStream;\n" +
				"import java.io.IOException;\n" +
				"public class X {\n" +
				"public void foo() throws Exception {\n" + 
				"        int i = 1;\n" + 
				"        try {\n" + 
				"            try (FileInputStream fis = new FileInputStream(\"\")) {\n" +
				"				 if (i == 2)" + 
				"                	return;\n" + 
				" 			 }\n" + 
				"            if (i == 35) \n" + 
				"                return;\n" + 
				"        } catch(IOException e) {\n" + 
				"            bar();\n" + 
				"        } finally {\n" + 
				"            bar();\n" + 
				"        }\n" + 
				"    }\n" + 
				"\n" + 
				"    private int bar() {\n" + 
				"        return 0;\n" + 
				"    }\n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"    }\n" +
				"}\n"
			}, 
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380750
// verify that s0 is not reported as uninitialized
public void testBug380750() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void foo(String[] args) {\n" + 
				"		String s0;\n" + 
				"		for(String s : singleton(s0=\"\")) {\n" + 
				"			System.out.println(s);\n" + 
				"		}\n" + 
				"		System.out.println(s0);\n" + 
				"	}\n" + 
				"	String[] singleton(String s) {\n" + 
				"		return new String[] {s};\n" + 
				"	}\n" + 
				"}\n"
			}, 
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391517
// java.lang.VerifyError on code that runs correctly in Eclipse 3.7 and eclipse 3.6
public void testBug391517() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.PrintWriter;\n" + 
				"\n" + 
				"public class X {\n" + 
				"\n" + 
				"	private static final int CONSTANT = 0;\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		// TODO Auto-generated method stub\n" + 
				"\n" + 
				"	}\n" + 
				"\n" + 
				"	static void addStackTrace(String prefix) {\n" + 
				"		if (CONSTANT == 0) {\n" + 
				"			return;\n" + 
				"		}\n" + 
				"		PrintWriter pw = null;\n" + 
				"		new Exception().printStackTrace(pw);\n" + 
				"		if (bar() == null) {\n" + 
				"			System.out.println();\n" + 
				"		}\n" + 
				"	}\n" + 
				"\n" + 
				"	static Object bar() {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"}"
			}, 
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=415997
// Bug 415997 - java.lang.VerifyError: Expecting a stackmap frame at branch target 
public void testBug415997a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Object o = null;\n" +
			"		if (o == null)\n" +
			"			if (true)\n" +
			"				return;\n" +
			"	}\n" +
			"}"
		},
		"");
}
public void testBug415997b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Object o = null;\n" +
			"		if (o == null) {}\n" +
			"		else\n" +
			"			if (true)\n" +
			"				return;\n" +
			"	}\n" +
			"}"
		},
		"");
}
public void testBug415997c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) throws Exception {\n" +
			"		System.out.println(ParseExpr11());\n" +
			"	}\n" +
			"	static final public Object ParseExpr11() throws Exception {\n" +
			"		Object expr;\n" +
			"		Object op = null;\n" +
			"		expr = ParseVarExpr();\n" +
			"		if (op == null) {\n" +
			"			if (true)\n" +
			"				return expr;\n" +
			"		}\n" +
			"		{\n" +
			"			throw new Exception(\"++/-- not supported in TUD Bantam Java.\");\n" +
			"		}\n" +
			"	}\n" +
			"	private static Object ParseVarExpr() {\n" +
			"		// TODO Auto-generated method stub\n" +
			"		return \"test\";\n" +
			"	}\n" +
			"}"
		},
		"test");
}
public void testBug499809() {
	this.runConformTest(
		new String[] {
			"Foo.java",
			"public class Foo {\n" + 
			"	static void foo( ) {\n" + 
			"		String _a, _b, _c, _d, _e, _f, _g, _h, _i, _j, _k, _l, _m, _n, _o, _p, _q, _r, _s, _t, _u, _v, _w, _x, _y, _z, a, b,\n" + 
			"		c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, s0, s1, s2, s3, s4, s5, s6, s7;\n" + 
			"		Object ob = new Object();\n" + 
			"		int int1 = 0, int2 = 2, int3, int4;\n" + 
			"		if (ob != null) {\n" + 
			"			int4 = 1;\n" + 
			"		}\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		System.out.println(\"Done\");\n" + 
			"	}\n" + 
			"}\n"
		},
		"Done");
}
public void testBug499809a() {
	this.runConformTest(
		new String[] {
			"Foo.java",
			"public class Foo {\n" + 
			"	static void foo( ) {\n" + 
			"		String _a, _b, _c, _d, _e, _f, _g, _h, _i, _j, _k, _l, _m, _n, _o, _p, _q, _r, _s, _t, _u, _v, _w, _x, _y, _z, a, b,\n" + 
			"		c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, s0, s1, s2, s3, s4, s5, s6, s7;\n" + 
			"		Object ob = new Object();\n" + 
			"		int int1 = 0, int2 = 2, int3, int4;\n" +  
			"		if (ob == null) {\n" + 
			"			int1 = 1;\n" + 
			"		} else {\n" + 
			"			int4 = 1;\n" +
			"		}\n" +
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		System.out.println(\"Done\");\n" + 
			"	}\n" + 
			"}\n"
		},
		"Done");
}
public static Class testClass() {
	return FlowAnalysisTest.class;
}
}

