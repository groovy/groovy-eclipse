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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DeprecatedTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test008a" };
}

protected char[][] invisibleType;

public DeprecatedTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

@Override
protected void tearDown() throws Exception {
	this.invisibleType = null;
	super.tearDown();
}

@Override
protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths, Map<String, String> options) {
	// constructs a name environment that is able to hide a type of name 'this.invisibleType':
	this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
	return new InMemoryNameEnvironment(testFiles, getClassLibs(classPaths == null, options)) {
		@Override
		public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
			if (DeprecatedTest.this.invisibleType != null && CharOperation.equals(DeprecatedTest.this.invisibleType, compoundTypeName))
				return null;
			return super.findType(compoundTypeName);
		}
		@Override
		public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
			if (DeprecatedTest.this.invisibleType != null && DeprecatedTest.this.invisibleType.length == packageName.length+1) {
				char[][] packName = CharOperation.subarray(DeprecatedTest.this.invisibleType, 0, DeprecatedTest.this.invisibleType.length-1);
				if (CharOperation.equals(packageName, packName)) {
					char[] simpleName = DeprecatedTest.this.invisibleType[DeprecatedTest.this.invisibleType.length-1];
					if (CharOperation.equals(simpleName, typeName))
						return null;
				}
			}
			return super.findType(typeName, packageName);
		}
	};
}

public void test001() {
	this.runNegativeTest(new String[] {
		"p/B.java",
		"package p;\n" +
		"class B extends A {\n" +
		"    float x = super.x;\n" +
		"}\n",

		"p/A.java",
		"package p;\n" +
		"class A {\n" +
		"    /** @deprecated */\n" +
		"    int x = 1;\n" +
		"}\n",
	},
	"----------\n" +
	"1. WARNING in p\\B.java (at line 3)\n" +
	"	float x = super.x;\n" +
	"	      ^\n" +
	"The field B.x is hiding a field from type A\n" +
	"----------\n" +
	"2. WARNING in p\\B.java (at line 3)\n" +
	"	float x = super.x;\n" +
	"	                ^\n" +
	"The field A.x is deprecated\n" +
	"----------\n"
	);
}
public void test002() {
	this.runNegativeTest(new String[] {
		"p/C.java",
		"package p;\n" +
		"class C {\n" +
		"    static int x = new A().x;\n" +
		"}\n",

		"p/A.java",
		"package p;\n" +
		"class A {\n" +
		"    /** @deprecated */\n" +
		"    int x = 1;\n" +
		"}\n",

	},
		"----------\n" +
		"1. WARNING in p\\C.java (at line 3)\n" +
		"	static int x = new A().x;\n" +
		"	                       ^\n" +
		"The field A.x is deprecated\n" +
		"----------\n"
	);
}
public void test003() {
	this.runNegativeTest(new String[] {
		"p/Top.java",
		"package p;\n" +
		"public class Top {\n" +
		"  \n" +
		"  class M1 {\n" +
		"    class M2 {}\n" +
		"  };\n" +
		"  \n" +
		"  static class StaticM1 {\n" +
		"    static class StaticM2 {\n" +
		"      class NonStaticM3{}};\n" +
		"  };\n" +
		"  \n" +
		"public static void main(String argv[]){\n" +
		"  Top tip = new Top();\n" +
		"  System.out.println(\"Still alive 0\");\n" +
		"  tip.testStaticMember();\n" +
		"  System.out.println(\"Still alive 1\");\n" +
		"  tip.testStaticMember1();\n" +
		"  System.out.println(\"Still alive 2\");\n" +
		"  tip.testStaticMember2();\n" +
		"  System.out.println(\"Still alive 3\");\n" +
		"  tip.testStaticMember3();\n" +
		"  System.out.println(\"Still alive 4\");\n" +
		"  tip.testStaticMember4();\n" +
		"  System.out.println(\"Completed\");\n" +
		"}\n" +
		"  void testMember(){\n" +
		"    new M1().new M2();}\n" +
		"  void testStaticMember(){\n" +
		"    new StaticM1().new StaticM2();}\n" +
		"  void testStaticMember1(){\n" +
		"    new StaticM1.StaticM2();}\n" +
		"  void testStaticMember2(){\n" +
		"    new StaticM1.StaticM2().new NonStaticM3();}\n" +
		"  void testStaticMember3(){\n" +
		"    // define an anonymous subclass of the non-static M3\n" +
		"    new StaticM1.StaticM2().new NonStaticM3(){};\n" +
		"  }   \n" +
		"  void testStaticMember4(){\n" +
		"    // define an anonymous subclass of the non-static M3\n" +
		"    new StaticM1.StaticM2().new NonStaticM3(){\n" +
		"      Object hello(){\n" +
		"        return new StaticM1.StaticM2().new NonStaticM3();\n" +
		"      }};\n" +
		"      \n" +
		"  }    \n" +
		"}\n",
		},
		"----------\n" +
		"1. ERROR in p\\Top.java (at line 30)\n" +
		"	new StaticM1().new StaticM2();}\n" +
		"	^^^^^^^^^^^^^^\n" +
		"Illegal enclosing instance specification for type Top.StaticM1.StaticM2\n" +
		"----------\n" +
		"2. WARNING in p\\Top.java (at line 42)\n" +
		"	Object hello(){\n" +
		"	       ^^^^^^^\n" +
		"The method hello() from the type new Top.StaticM1.StaticM2.NonStaticM3(){} is never used locally\n" +
		"----------\n");
}
/**
 * Regression test for PR #1G9ES9B
 */
public void test004() {
	this.runNegativeTest(new String[] {
		"p/Warning.java",
		"package p;\n" +
		"import java.util.Date;\n" +
		"public class Warning {\n" +
		"public Warning() {\n" +
		"     super();\n" +
		"     Date dateObj = new Date();\n" +
		"     dateObj.UTC(1,2,3,4,5,6);\n" +
		"}\n" +
		"}\n",
		},
		"----------\n" +
		"1. WARNING in p\\Warning.java (at line 7)\n" +
		"	dateObj.UTC(1,2,3,4,5,6);\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The static method UTC(int, int, int, int, int, int) from the type Date should be accessed in a static way\n" +
		"----------\n" +
		"2. WARNING in p\\Warning.java (at line 7)\n" +
		"	dateObj.UTC(1,2,3,4,5,6);\n" +
		"	        ^^^^^^^^^^^^^^^^\n" +
		"The method UTC(int, int, int, int, int, int) from the type Date is deprecated\n" +
		"----------\n");
}
public void test005() {
	this.runConformTest(
		new String[] {
			"X.java",
		  "public class X {\n"
			+ "/**\n"
			+ " * @deprecated\n"
			+ " */\n"
			+ " 	public static class Y {\n"
			+ "	}\n" +
			"   public static void main(String[] args) {	\n" +
			"        System.out.print(\"SUCCESS\");	\n" +
			"	}	\n"
			+ "}"
		},
		"SUCCESS", // expected output
		null,
		true, // flush previous output dir content
		null, // special vm args
		null,  // custom options
		null); // custom requestor
	this.runNegativeTest(
		new String[] {
			"A.java",
			"public class A extends X.Y {}"
		},
		"----------\n" +
		"1. WARNING in A.java (at line 1)\n" +
		"	public class A extends X.Y {}\n" +
		"	                         ^\n" +
		"The type X.Y is deprecated\n" +
		"----------\n",// expected output
		null,
		false, // flush previous output dir content
		null);  // custom options
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=40839
public void test006() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	/**\n" +
			"	  @deprecated\n" +
			"	 */\n" +
			"	; // line comment\n" +
			"	static int i;\n" +
			"   public static void main(String[] args) {	\n" +
			"        System.out.print(\"SUCCESS\");	\n" +
			"	}	\n" +
			"}"
		},
		"SUCCESS", // expected output
		null,
		true, // flush previous output dir content
		null, // special vm args
		null,  // custom options
		null); // custom requestor
		runConformTest(
			// test directory preparation
	 		false /* do not flush output directory */,
			new String[] { /* test files */
				"A.java",
				"public class A {\n" +
				"   public static void main(String[] args) {	\n" +
				"        System.out.print(X.i);	\n" +
				"	}	\n" +
				"}"
			},
			// compiler results
			"" /* expected compiler log */,
			// runtime results
			"0" /* expected output string */,
			"" /* expected error string */,
			// javac options
			JavacTestOptions.DEFAULT /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=88124
public void test007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"/**\n" +
			" * @deprecated\n" +
			" */\n" +
			"public class X {\n" +
			"}\n",

			"Y.java",
			"/**\n" +
			" * @deprecated\n" +
			" */\n" +
			"public class Y {\n" +
			"  Zork z;\n" +
			"  X x;\n" +
			"  X foo() {\n" +
			"    X x; // unexpected deprecated warning here\n" +
			"  }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 5)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"2. WARNING in Y.java (at line 8)\n" +
		"	X x; // unexpected deprecated warning here\n" +
		"	  ^\n" +
		"The local variable x is hiding a field from type Y\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=88124 - variation
public void test008() {
	this.runConformTest(
		new String[] {
			"X.java",
			"/**\n" +
			" * @deprecated\n" +
			" */\n" +
			"public class X {\n" +
			"}\n",
		},
		"");
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"/**\n" +
			" * @deprecated\n" +
			" */\n" +
			"public class Y {\n" +
			"  Zork z;\n" +
			"  void foo() {\n" +
			"    X x; // unexpected deprecated warning here\n" +
			"  }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 5)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",// expected output
		null,
		false, // flush previous output dir content
		null);  // custom options
}
// variation of test008 on behalf of Bug 526335 - [9][hovering] Deprecation warning should show the new 'since' deprecation value
// verify that we don't attempt to access java.lang.Deprecated in a 1.4 based compilation.
public void test008a() throws IOException {
	String jarPath = LIB_DIR+File.separator+"p008a"+File.separator+"x.jar";
	Util.createJar(new String[] {
			"X.java",
			"package p008a;\n" +
			"@Deprecated\n" +
			"public class X {\n" +
			"}\n",
		},
		jarPath,
		CompilerOptions.getFirstSupportedJavaVersion());

	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"  void foo() {\n" +
			"    p008a.X x;\n" +
			"  }\n" +
			"}\n",
		};
	String[] libs = getDefaultClassPaths();
	libs = Arrays.copyOf(libs, libs.length+1);
	libs[libs.length-1] = jarPath;
	runner.classLibraries = libs;
	runner.expectedCompilerLog =
		"----------\n" +
		"1. WARNING in Y.java (at line 3)\n" +
		"	p008a.X x;\n" +
		"	      ^\n" +
		"The type X is deprecated\n" +
		"----------\n";
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		// simulate we were running on a JRE without java.lang.Deprecated
		this.invisibleType = TypeConstants.JAVA_LANG_DEPRECATED;
	}
	runner.runWarningTest();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=88124 - variation
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"/**\n" +
			" * @deprecated\n" +
			" */\n" +
			"public class X {\n" +
			"}\n",

			"Y.java",
			"/**\n" +
			" * @deprecated\n" +
			" */\n" +
			"public class Y {\n" +
			"  Zork z;\n" +
			"  void foo() {\n" +
			"    X x; // unexpected deprecated warning here\n" +
			"  }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 5)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=88187
public void test010() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
            "X.java",
            "/**\n" +
            " * @deprecated\n" +
            " */\n" +
            "public class X {\n" +
            "        /**\n" +
            "         * @see I2#foo()\n" +
            "         */\n" +
            "        I1 foo() {\n" +
            "                return null;\n" +
            "        }\n" +
            "       Zork z;\n" +
            "}\n",
			"I1.java",
			"/**\n" +
			" * @deprecated\n" +
			" */\n" +
			"public interface I1 {\n" +
			"		 // empty block\n" +
			"}\n",
			"I2.java",
			"/**\n" +
			" * @deprecated\n" +
			" */\n" +
			"public interface I2 {\n" +
			"		 I1 foo(); // unexpected warning here\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",
		null,
		true,
		customOptions);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=123522
public void test011() {
	this.runNegativeTest(
		new String[] {
				"p1/X.java", // =================
				"package p1;\n" +
				"import p2.I;\n" +
				"/** @deprecated */\n" +
				"public class X {\n" +
				"	Zork z;\n" +
				"}\n", // =================
				"p2/I.java", // =================
				"package p2;\n" +
				"/** @deprecated */\n" +
				"public interface I {\n" +
				"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in p1\\X.java (at line 5)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}

// @deprecated upon locals do not influence the deprecation diagnostic
// JLS3 9.6
public void test012() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation,
		CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode,
		CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
            "X.java",
			"public class X {\n" +
			"    void foo() {\n" +
			"        /** @deprecated */\n" +
			"        int i1 = Y.m;\n" +
			"    }\n" +
			"    /** @deprecated */\n" +
			"    void bar() {\n" +
			"        int i1 = Y.m;\n" +
			"    }\n" +
			"}\n",
            "Y.java",
			"public class Y {\n" +
			"    /** @deprecated */\n" +
			"    static int m;\n" +
			"}\n",	},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			// compiler results
			"----------\n" + /* expected compiler log */
			"1. ERROR in X.java (at line 4)\n" +
			"	int i1 = Y.m;\n" +
			"	           ^\n" +
			"The field Y.m is deprecated\n" +
			"----------\n",
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// @deprecated upon locals do not influence the deprecation diagnostic
// JLS3 9.6
// @Deprecated variant
public void test013() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportDeprecation,
			CompilerOptions.ERROR);
		customOptions.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode,
			CompilerOptions.IGNORE);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */,
			new String[] { /* test files */
	            "X.java",
				"public class X {\n" +
				"    void foo() {\n" +
				"        @Deprecated\n" +
				"        int i1 = Y.m;\n" +
				"    }\n" +
				"    @Deprecated\n" +
				"    void bar() {\n" +
				"        int i1 = Y.m;\n" +
				"    }\n" +
				"}\n",
	            "Y.java",
				"public class Y {\n" +
				"    @Deprecated\n" +
				"    static int m;\n" +
				"}\n",
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			// compiler results
			"----------\n" + /* expected compiler log */
			"1. ERROR in X.java (at line 4)\n" +
			"	int i1 = Y.m;\n" +
			"	           ^\n" +
			"The field Y.m is deprecated\n" +
			"----------\n",
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159243
public void test014() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"/**\n" +
			" * @deprecated\n" +
			" */\n" +
			"public class X {\n" +
			"}\n",
			"Y.java",
			"import p.X;\n" +
			"public class Y {\n" +
			"  Zork z;\n" +
			"  void foo() {\n" +
			"    X x;\n" +
			"    X[] xs = { x };\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    p.X x;\n" +
			"    p.X[] xs = { x };\n" +
			"  }\n" +
			"}\n",
		},
		(this.complianceLevel >= ClassFileConstants.JDK9 ? "" :
		"----------\n" +
		"1. WARNING in Y.java (at line 1)\n" +
		"	import p.X;\n" +
		"	       ^^^\n" +
		"The type X is deprecated\n"
		) +
		"----------\n" +
		"2. ERROR in Y.java (at line 3)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"2. WARNING in Y.java (at line 5)\n" +
		"	X x;\n" +
		"	^\n" +
		"The type X is deprecated\n" +
		"----------\n" +
		"3. WARNING in Y.java (at line 6)\n" +
		"	X[] xs = { x };\n" +
		"	^\n" +
		"The type X is deprecated\n" +
		"----------\n" +
		"4. WARNING in Y.java (at line 9)\n" +
		"	p.X x;\n" +
		"	  ^\n" +
		"The type X is deprecated\n" +
		"----------\n" +
		"5. WARNING in Y.java (at line 10)\n" +
		"	p.X[] xs = { x };\n" +
		"	  ^\n" +
		"The type X is deprecated\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159709
// the order of the CUs must not modify the behavior, see also test016
public void test015() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"p/M1.java",
			"package p;\n" +
			"public class M1 {\n" +
			"  void bar() {\n" +
			"    a.N1.N2.N3 m = null;\n" +
			"    m.foo();\n" +
			"  }\n" +
			"}\n",
			"a/N1.java",
			"package a;\n" +
			"public class N1 {\n" +
			"  /** @deprecated */\n" +
			"  public class N2 {" +
			"    public class N3 {" +
			"      public void foo() {}" +
			"    }" +
			"  }" +
			"}\n",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in p\\M1.java (at line 4)\n" +
		"	a.N1.N2.N3 m = null;\n" +
		"	     ^^\n" +
		"The type N1.N2 is deprecated\n" +
		"----------\n" +
		"2. ERROR in p\\M1.java (at line 4)\n" +
		"	a.N1.N2.N3 m = null;\n" +
		"	        ^^\n" +
		"The type N1.N2.N3 is deprecated\n" +
		"----------\n" +
		"3. ERROR in p\\M1.java (at line 5)\n" +
		"	m.foo();\n" +
		"	  ^^^^^\n" +
		"The method foo() from the type N1.N2.N3 is deprecated\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);

}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159709
public void test016() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"a/N1.java",
			"package a;\n" +
			"public class N1 {\n" +
			"  /** @deprecated */\n" +
			"  public class N2 {" +
			"    public class N3 {" +
			"      public void foo() {}" +
			"    }" +
			"  }" +
			"}\n",
			"p/M1.java",
			"package p;\n" +
			"public class M1 {\n" +
			"  void bar() {\n" +
			"    a.N1.N2.N3 m = null;\n" +
			"    m.foo();\n" +
			"  }\n" +
			"}\n",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in p\\M1.java (at line 4)\n" +
		"	a.N1.N2.N3 m = null;\n" +
		"	     ^^\n" +
		"The type N1.N2 is deprecated\n" +
		"----------\n" +
		"2. ERROR in p\\M1.java (at line 4)\n" +
		"	a.N1.N2.N3 m = null;\n" +
		"	        ^^\n" +
		"The type N1.N2.N3 is deprecated\n" +
		"----------\n" +
		"3. ERROR in p\\M1.java (at line 5)\n" +
		"	m.foo();\n" +
		"	  ^^^^^\n" +
		"The method foo() from the type N1.N2.N3 is deprecated\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);

}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159709
// variant: self-contained case, hence no report
public void test017() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"a/N1.java",
			"package a;\n" +
			"public class N1 {\n" +
			"  /** @deprecated */\n" +
			"  public class N2 {" +
			"    public class N3 {" +
			"      public void foo() {}" +
			"    }" +
			"  }" +
			"  void bar() {\n" +
			"    a.N1.N2.N3 m = null;\n" +
			"    m.foo();\n" +
			"  }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		customOptions,
		null,
		false);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159709
// variant: using a binary class
// **
public void test018() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"a/N1.java",
			"package a;\n" +
			"public class N1 {\n" +
			"  /** @deprecated */\n" +
			"  public class N2 {" +
			"    public class N3 {" +
			"      public void foo() {}" +
			"    }" +
			"  }" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		customOptions,
		null,
		false);
	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"p/M1.java",
			"package p;\n" +
			"public class M1 {\n" +
			"  void bar() {\n" +
			"    a.N1.N2.N3 m = null;\n" +
			"    m.foo();\n" +
			"  }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in p\\M1.java (at line 4)\n" +
		"	a.N1.N2.N3 m = null;\n" +
		"	     ^^\n" +
		"The type N1.N2 is deprecated\n" +
		"----------\n" +
		"2. ERROR in p\\M1.java (at line 4)\n" +
		"	a.N1.N2.N3 m = null;\n" +
		"	        ^^\n" +
		"The type N1.N2.N3 is deprecated\n" +
		"----------\n" +
		"3. ERROR in p\\M1.java (at line 5)\n" +
		"	m.foo();\n" +
		"	  ^^^^^\n" +
		"The method foo() from the type N1.N2.N3 is deprecated\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191909 (1.4 variant)
public void test019() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"test1/E01.java",
			"package test1;\n" +
			"public class E01 {\n" +
			"	/** @deprecated */\n" +
			"	public static int x = 5, y= 10;\n" +
			"}",
			"test1/E02.java",
			"package test1;\n" +
			"public class E02 {\n" +
			"	public void foo() {\n" +
			"		System.out.println(E01.x);\n" +
			"		System.out.println(E01.y);\n" +
			"	}\n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in test1\\E02.java (at line 4)\n" +
		"	System.out.println(E01.x);\n" +
		"	                       ^\n" +
		"The field E01.x is deprecated\n" +
		"----------\n" +
		"2. ERROR in test1\\E02.java (at line 5)\n" +
		"	System.out.println(E01.y);\n" +
		"	                       ^\n" +
		"The field E01.y is deprecated\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=300031
public void test020() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"a.b.c.d/Deprecated.java",
			"package a.b.c.d;\n" +
			"public class Deprecated {\n" +
			"	/** @deprecated */\n" +
			"	public class Inner {\n" +
			"		/** @deprecated */\n" +
			"		public class Inn {\n" +
			"		}\n" +
			"	}\n" +
			"	/** @deprecated */\n" +
			"	public Deprecated foo(){ return null;}\n" +
			"	/** @deprecated */\n" +
			"	public Deprecated goo(){ return null;}\n" +
			"	/** @deprecated */\n" +
			"	public static Deprecated bar(){ return null;}\n" +
			"}\n",
			"a.b.c.d.e/T.java",
			"package a.b.c.d.e;\n" +
			"import a.b.c.d.Deprecated;\n" +
			"public class T {\n" +
			"	a.b.c.d.Deprecated f;\n" +
			"	a.b.c.d.Deprecated.Inner.Inn g;\n" +
			"	Deprecated.Inner i;\n" +
			"	public void m() {\n" +
			"		f.foo().goo();\n" +
			"		a.b.c.d.Deprecated.bar();\n" +
			"	}\n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in a.b.c.d.e\\T.java (at line 5)\n" +
		"	a.b.c.d.Deprecated.Inner.Inn g;\n" +
		"	                   ^^^^^\n" +
		"The type Deprecated.Inner is deprecated\n" +
		"----------\n" +
		"2. ERROR in a.b.c.d.e\\T.java (at line 5)\n" +
		"	a.b.c.d.Deprecated.Inner.Inn g;\n" +
		"	                         ^^^\n" +
		"The type Deprecated.Inner.Inn is deprecated\n" +
		"----------\n" +
		"3. ERROR in a.b.c.d.e\\T.java (at line 6)\n" +
		"	Deprecated.Inner i;\n" +
		"	           ^^^^^\n" +
		"The type Deprecated.Inner is deprecated\n" +
		"----------\n" +
		"4. ERROR in a.b.c.d.e\\T.java (at line 8)\n" +
		"	f.foo().goo();\n" +
		"	  ^^^^^\n" +
		"The method foo() from the type Deprecated is deprecated\n" +
		"----------\n" +
		"5. ERROR in a.b.c.d.e\\T.java (at line 8)\n" +
		"	f.foo().goo();\n" +
		"	        ^^^^^\n" +
		"The method goo() from the type Deprecated is deprecated\n" +
		"----------\n" +
		"6. ERROR in a.b.c.d.e\\T.java (at line 9)\n" +
		"	a.b.c.d.Deprecated.bar();\n" +
		"	                   ^^^^^\n" +
		"The method bar() from the type Deprecated is deprecated\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void testJEP211_1() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/C1.java",
			"""
			package p1;
			@Deprecated public class C1 {}
			""",
			"Test.java",
			"""
			import p1.C1;
			public class Test {
				C1 c;
			}
			"""
		};
	runner.expectedCompilerLog =
			this.complianceLevel < ClassFileConstants.JDK9 ?
			"""
			----------
			1. WARNING in Test.java (at line 1)
				import p1.C1;
				       ^^^^^
			The type C1 is deprecated
			----------
			2. WARNING in Test.java (at line 3)
				C1 c;
				^^
			The type C1 is deprecated
			----------
			"""
			:
			"""
			----------
			1. WARNING in Test.java (at line 3)
				C1 c;
				^^
			The type C1 is deprecated
			----------
			""";
	runner.runWarningTest();
}
public static Class testClass() {
	return DeprecatedTest.class;
}
}
