/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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

public class FlowAnalysisTest extends AbstractRegressionTest {
	
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
		"Switch case may be entered by falling through previous case\n" + 
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
		"Switch case may be entered by falling through previous case\n" + 
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
		"Switch case may be entered by falling through previous case\n" + 
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
		"Switch case may be entered by falling through previous case\n" + 
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
		"Switch case may be entered by falling through previous case\n" + 
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
		"Switch case may be entered by falling through previous case\n" + 
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
		"Switch case may be entered by falling through previous case\n" + 
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
		"1. ERROR in X.java (at line 10)\n" + 
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
	this.runNegativeTest(
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
		"----------\n" + 
		"1. ERROR in X.java (at line 13)\n" + 
		"	X() {\n" + 
		"	^^^\n" + 
		"The blank final field blank may not have been initialized\n" + 
		"----------\n",
		JavacTestOptions.EclipseHasABug.EclipseBug235781);
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
	if (complianceLevel <= ClassFileConstants.JDK1_3) {
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
		"1. ERROR in X.java (at line 5)\n" + 
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
		"1. ERROR in X.java (at line 6)\n" + 
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
		"1. ERROR in X.java (at line 5)\n" + 
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
		"1. ERROR in X.java (at line 7)\n" + 
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
		"",
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
public void _test050_definite_assigment_and_if_true() {
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
public static Class testClass() {
	return FlowAnalysisTest.class;
}
}

