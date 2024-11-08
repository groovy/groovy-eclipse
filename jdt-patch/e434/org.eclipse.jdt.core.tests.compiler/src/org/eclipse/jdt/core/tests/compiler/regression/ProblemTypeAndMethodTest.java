/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *								bug 328281 - visibility leaks not detected when analyzing unused field in private class
 *								bug 379784 - [compiler] "Method can be static" is not getting reported
 *								bug 379834 - Wrong "method can be static" in presence of qualified super and different staticness of nested super class.
 *     Jesper S Moller <jesper@selskabet.org> - Contributions for
 *								bug 378674 - "The method can be declared as static" is wrong
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ProblemTypeAndMethodTest extends AbstractRegressionTest {
public ProblemTypeAndMethodTest(String name) {
	super(name);
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
static {
//		TESTS_NAMES = new String[] { "test376550" };
//		TESTS_NUMBERS = new int[] { 113 };
//		TESTS_RANGE = new int[] { 108, -1 };
}

public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {
	return ProblemTypeAndMethodTest.class;
}
public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        interface Moosh { void foo(); }\n" +
			"\n" +
			"        static abstract class A implements Moosh {}\n" +
			"\n" +
			"        static class W extends A {}\n" +
			"        static class Y extends A {}\n" +
			"        static class Z extends A {}\n" +
			"        public static void main(String[] args) {\n" +
			"                new W();  // throws ClassFormatError\n" +
			"        }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	static class W extends A {}\n" +
		"	             ^\n" +
		"The type X.W must implement the inherited abstract method X.Moosh.foo()\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	static class Y extends A {}\n" +
		"	             ^\n" +
		"The type X.Y must implement the inherited abstract method X.Moosh.foo()\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 8)\n" +
		"	static class Z extends A {}\n" +
		"	             ^\n" +
		"The type X.Z must implement the inherited abstract method X.Moosh.foo()\n" +
		"----------\n",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no custom options */,
		true /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false  /* do not skip javac for this peculiar test */,
		false  /* do not perform statements recovery */);
	ClassFileReader reader = getClassFileReader(OUTPUT_DIR + File.separator  +"X$W.class", "X$W");
	IBinaryMethod[] methods = reader.getMethods();
	assertEquals("Wrong size", 2, methods.length);
	int counter = 0;
	for (int i = 0; i < 2; i++) {
		IBinaryMethod method = methods[i];
		if (new String(method.getSelector()).equals("foo")) {
			counter++;
		}
	}
	assertEquals("Wrong number of foo method", 1, counter);

	reader = getClassFileReader(OUTPUT_DIR + File.separator  +"X$Y.class", "X$Y");
	methods = reader.getMethods();
	assertEquals("Wrong size", 2, methods.length);
	counter = 0;
	for (int i = 0; i < 2; i++) {
		IBinaryMethod method = methods[i];
		if (new String(method.getSelector()).equals("foo")) {
			counter++;
		}
	}
	assertEquals("Wrong number of foo method", 1, counter);

	reader = getClassFileReader(OUTPUT_DIR + File.separator  +"X$Z.class", "X$Z");
	methods = reader.getMethods();
	assertEquals("Wrong size", 2, methods.length);
	counter = 0;
	for (int i = 0; i < 2; i++) {
		IBinaryMethod method = methods[i];
		if (new String(method.getSelector()).equals("foo")) {
			counter++;
		}
	}
	assertEquals("Wrong number of foo method", 1, counter);
}

public void test002() {
	this.runNegativeTest(new String[] {
			"X.java",
			"public class X extends Zork {\n" +
			"	void foo() {\n" +
			"		Zork z = this;\n" +
			"		String s = this;\n" +
			"		Zork2 z2 = this;\n" +
			"	}\n" +
			"	Zork fz = this;\n" +
			"	String fs = this;\n" +
			"	Zork2 fz2 = this;\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X extends Zork {\n" +
		"	                       ^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	Zork z = this;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	String s = this;\n" +
		"	           ^^^^\n" +
		"Type mismatch: cannot convert from X to String\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 5)\n" +
		"	Zork2 z2 = this;\n" +
		"	^^^^^\n" +
		"Zork2 cannot be resolved to a type\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 7)\n" +
		"	Zork fz = this;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 8)\n" +
		"	String fs = this;\n" +
		"	            ^^^^\n" +
		"Type mismatch: cannot convert from X to String\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 9)\n" +
		"	Zork2 fz2 = this;\n" +
		"	^^^^^\n" +
		"Zork2 cannot be resolved to a type\n" +
		"----------\n");
}

public void test003() {
	this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" +
			"	Zork field;\n" +
			"	\n" +
			"	void foo(Y y) {\n" +
			"		Object o = y.foo();\n" +
			"		Object s = y.slot;\n" +
			"		y.bar(null);\n" +
			"		Object s2 = new Y().slot;\n" +
			"		Object f = field;\n" +
			"	}\n" +
			"}\n" +
			"class Y {\n" +
			"	Zork foo() {	return null; }\n" +
			"	void bar(Zork z) {}\n" +
			"	Zork slot;\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Zork field;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	Object o = y.foo();\n" +
		"	             ^^^\n" +
		"The method foo() from the type Y refers to the missing type Zork\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	Object s = y.slot;\n" +
		"	           ^^^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 7)\n" +
		"	y.bar(null);\n" +
		"	  ^^^\n" +
		"The method bar(Zork) from the type Y refers to the missing type Zork\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 8)\n" +
		"	Object s2 = new Y().slot;\n" +
		"	            ^^^^^^^^^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 9)\n" +
		"	Object f = field;\n" +
		"	           ^^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 13)\n" +
		"	Zork foo() {	return null; }\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 14)\n" +
		"	void bar(Zork z) {}\n" +
		"	         ^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"9. ERROR in X.java (at line 15)\n" +
		"	Zork slot;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200
public void test004() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" +
					"\n" +
					"import q.Zork;\n" +
					"\n" +
					"public class OtherFoo extends Zork{\n" +
					"	public class OtherMember extends Zork {}\n" +
					"	public Zork foo;\n" +
					"	public Zork bar() {	return null; }\n" +
					"	public void baz(Zork z) {}\n" +
					"}\n",
					"q/Zork.java", //-----------------------------------------------------------------------
					"package q;\n" +
					"public class Zork {\n" +
					"}\n",
			},
			"");
	this.runNegativeTest(
		new String[] {
				"X.java", //-----------------------------------------------------------------------
				"import p.OtherFoo;\n" +
				"import q.Zork;\n" +
				"\n" +
				"public class X {\n" +
				"	void foo() {\n" +
				"		OtherFoo ofoo;\n" +
				"		String s1 = ofoo.foo;\n" +
				"		String s2 = ofoo.bar();\n" +
				"		String s3 = ofoo.new OtherMember();\n" +
				"		ofoo.baz(this);\n" +
				"	}\n" +
				"	void bar() {\n" +
				"		OtherX ox;\n" +
				"		String s1 = ox.foo;\n" +
				"		String s2 = ox.bar();\n" +
				"		String s3 = ox.new OtherMember();\n" +
				"		ox.baz(this);\n" +
				"	}\n" +
				"}	\n" +
				"\n" +
				"class OtherX {\n" +
				"	public class OtherMember extends Zork {}\n" +
				"	public Zork foo;\n" +
				"	public Zork bar() {	return null; }\n" +
				"	public void baz(Zork z) {}\n" +
				"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	String s1 = ofoo.foo;\n" +
		"	            ^^^^^^^^\n" +
		"Type mismatch: cannot convert from Zork to String\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	String s2 = ofoo.bar();\n" +
		"	            ^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Zork to String\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	String s3 = ofoo.new OtherMember();\n" +
		"	            ^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from OtherFoo.OtherMember to String\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 10)\n" +
		"	ofoo.baz(this);\n" +
		"	     ^^^\n" +
		"The method baz(Zork) in the type OtherFoo is not applicable for the arguments (X)\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 14)\n" +
		"	String s1 = ox.foo;\n" +
		"	            ^^^^^^\n" +
		"Type mismatch: cannot convert from Zork to String\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 15)\n" +
		"	String s2 = ox.bar();\n" +
		"	            ^^^^^^^^\n" +
		"Type mismatch: cannot convert from Zork to String\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 16)\n" +
		"	String s3 = ox.new OtherMember();\n" +
		"	            ^^^^^^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from OtherX.OtherMember to String\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 17)\n" +
		"	ox.baz(this);\n" +
		"	   ^^^\n" +
		"The method baz(Zork) in the type OtherX is not applicable for the arguments (X)\n" +
		"----------\n",
		null,
		false);

	// delete binary file Zork (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q" + File.separator + "Zork.class"));

	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"import p.OtherFoo;\n" +
					"import q.Zork;\n" +
					"\n" +
					"public class X {\n" +
					"	void foo() {\n" +
					"		OtherFoo ofoo;\n" +
					"		String s1 = ofoo.foo;\n" +
					"		String s2 = ofoo.bar();\n" +
					"		String s3 = ofoo.new OtherMember();\n" +
					"		ofoo.baz(this);\n" +
					"	}\n" +
					"	void bar() {\n" +
					"		OtherX ox;\n" +
					"		String s1 = ox.foo;\n" +
					"		String s2 = ox.bar();\n" +
					"		String s3 = ox.new OtherMember();\n" +
					"		ox.baz(this);\n" +
					"	}\n" +
					"}	\n" +
					"\n" +
					"class OtherX {\n" +
					"	public class OtherMember extends Zork {}\n" +
					"	public Zork foo;\n" +
					"	public Zork bar() {	return null; }\n" +
					"	public void baz(Zork z) {}\n" +
					"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	import p.OtherFoo;\n" +
			"	^\n" +
			"The type q.Zork cannot be resolved. It is indirectly referenced from required type p.OtherFoo\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	import q.Zork;\n" +
			"	       ^^^^^^\n" +
			"The import q.Zork cannot be resolved\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	String s1 = ofoo.foo;\n" +
			"	            ^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	String s2 = ofoo.bar();\n" +
			"	                 ^^^\n" +
			"The method bar() from the type OtherFoo refers to the missing type Zork\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 9)\n" +
			"	String s3 = ofoo.new OtherMember();\n" +
			"	            ^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from OtherFoo.OtherMember to String\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 10)\n" +
			"	ofoo.baz(this);\n" +
			"	     ^^^\n" +
			"The method baz(Zork) from the type OtherFoo refers to the missing type Zork\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 14)\n" +
			"	String s1 = ox.foo;\n" +
			"	            ^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 15)\n" +
			"	String s2 = ox.bar();\n" +
			"	               ^^^\n" +
			"The method bar() from the type OtherX refers to the missing type Zork\n" +
			"----------\n" +
			"9. ERROR in X.java (at line 16)\n" +
			"	String s3 = ox.new OtherMember();\n" +
			"	            ^^^^^^^^^^^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from OtherX.OtherMember to String\n" +
			"----------\n" +
			"10. ERROR in X.java (at line 17)\n" +
			"	ox.baz(this);\n" +
			"	   ^^^\n" +
			"The method baz(Zork) from the type OtherX refers to the missing type Zork\n" +
			"----------\n" +
			"11. ERROR in X.java (at line 22)\n" +
			"	public class OtherMember extends Zork {}\n" +
			"	                                 ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"12. ERROR in X.java (at line 23)\n" +
			"	public Zork foo;\n" +
			"	       ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"13. ERROR in X.java (at line 24)\n" +
			"	public Zork bar() {	return null; }\n" +
			"	       ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"14. ERROR in X.java (at line 25)\n" +
			"	public void baz(Zork z) {}\n" +
			"	                ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test005() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" +
					"\n" +
					"import q1.q2.Zork;\n" +
					"\n" +
					"public class OtherFoo extends Zork{\n" +
					"	public class OtherMember extends Zork {}\n" +
					"	public Zork foo;\n" +
					"	public Zork bar() {	return null; }\n" +
					"	public void baz(Zork z) {}\n" +
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" +
					"public class Zork {\n" +
					"}\n",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"X.java", //-----------------------------------------------------------------------
			"public class X {\n" +
			"	void foo() {\n" +
			"		p.OtherFoo ofoo = new p.OtherFoo();\n" +
			"		Object o = ofoo.bar();\n" +
			"		q1.q2.Zork z;\n" +
			"		ofoo.bar();\n" + // not a problem
			"	}\n" +
			"}	\n",
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 4)\n" +
		"	Object o = ofoo.bar();\n" +
		"	                ^^^\n" +
		"The method bar() from the type OtherFoo refers to the missing type Zork\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	q1.q2.Zork z;\n" +
		"	^^^^^^^^^^\n" +
		"q1.q2.Zork cannot be resolved to a type\n" +
		"----------\n",
		// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test006() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" +
					"\n" +
					"import q1.q2.Zork;\n" +
					"\n" +
					"public class OtherFoo extends Zork{\n" +
					"	public class OtherMember extends Zork {}\n" +
					"	public Zork foo;\n" +
					"	public Zork bar() {	return null; }\n" +
					"	public void baz(Zork z) {}\n" +
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" +
					"public class Zork {\n" +
					"}\n",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"X.java", //-----------------------------------------------------------------------
			"import q1.q2.*;\n" +
			"public class X {\n" +
			"	void foo() {\n" +
			"		p.OtherFoo ofoo = new p.OtherFoo();\n" +
			"		ofoo.bar();\n" +
			"		Zork z;\n" +
			"	}\n" +
			"}	\n",
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 1)\n" +
		"	import q1.q2.*;\n" +
		"	       ^^\n" +
		"The import q1 cannot be resolved\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",
		// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test007() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" +
					"\n" +
					"import q1.q2.Zork;\n" +
					"\n" +
					"public class OtherFoo extends Zork{\n" +
					"	public class OtherMember extends Zork {}\n" +
					"	public Zork foo;\n" +
					"	public Zork bar() {	return null; }\n" +
					"	public void baz(Zork z) {}\n" +
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" +
					"public class Zork {\n" +
					"}\n",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"X.java", //-----------------------------------------------------------------------
			"import q1.q2.Zork;\n" +
			"public class X {\n" +
			"	void foo() {\n" +
			"		p.OtherFoo ofoo = new p.OtherFoo();\n" +
			"		Object o = ofoo.bar();\n" +
			"		Zork z;\n" +
			"	}\n" +
			"}	\n",
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 1)\n" +
		"	import q1.q2.Zork;\n" +
		"	       ^^\n" +
		"The import q1 cannot be resolved\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	Object o = ofoo.bar();\n" +
		"	                ^^^\n" +
		"The method bar() from the type OtherFoo refers to the missing type Zork\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",
		// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test008() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" +
					"\n" +
					"import q1.q2.Zork;\n" +
					"\n" +
					"public class OtherFoo extends Zork{\n" +
					"	public class OtherMember extends Zork {}\n" +
					"	public Zork foo;\n" +
					"	public Zork bar() {	return null; }\n" +
					"	public void baz(Zork z) {}\n" +
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" +
					"public class Zork {\n" +
					"}\n",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"X.java", //-----------------------------------------------------------------------
			"public class X {\n" +
			"	void foo() {\n" +
			"		q1.q2.Zork z;\n" +
			"	}\n" +
			"}	\n",
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 3)\n" +
		"	q1.q2.Zork z;\n" +
		"	^^\n" +
		"q1 cannot be resolved to a type\n" +
		"----------\n",
		// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test009() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" +
					"\n" +
					"import q1.q2.Zork;\n" +
					"\n" +
					"public class OtherFoo extends Zork{\n" +
					"	public class OtherMember extends Zork {}\n" +
					"	public Zork foo;\n" +
					"	public Zork bar() {	return null; }\n" +
					"	public void baz(Zork z) {}\n" +
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" +
					"public class Zork {\n" +
					"}\n",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"X.java", //-----------------------------------------------------------------------
			"import q1.q2.*;\n" +
			"public class X {\n" +
			"	void foo() {\n" +
			"		Zork z;\n" +
			"	}\n" +
			"}	\n",
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 1)\n" +
		"	import q1.q2.*;\n" +
		"	       ^^\n" +
		"The import q1 cannot be resolved\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",			// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test010() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" +
					"\n" +
					"import q1.q2.Zork;\n" +
					"\n" +
					"public class OtherFoo extends Zork{\n" +
					"	public class OtherMember extends Zork {}\n" +
					"	public Zork foo;\n" +
					"	public Zork bar() {	return null; }\n" +
					"	public void baz(Zork z) {}\n" +
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" +
					"public class Zork {\n" +
					"}\n",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"X.java", //-----------------------------------------------------------------------
			"import q1.q2.Zork;\n" +
			"public class X {\n" +
			"	void foo() {\n" +
			"		Zork z;\n" +
			"	}\n" +
			"}	\n",
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 1)\n" +
		"	import q1.q2.Zork;\n" +
		"	       ^^\n" +
		"The import q1 cannot be resolved\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",
		// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test011() {
	this.runNegativeTest(new String[] {
			"X.java",
			"public class  X {\n" +
			"	java[] field1;\n" +
			"	java.lang[] field2;\n" +
			"	void field3;\n" +
			"	void[] field4;\n" +
			"	\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	java[] field1;\n" +
		"	^^^^\n" +
		"java cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	java.lang[] field2;\n" +
		"	^^^^^^^^^\n" +
		"java.lang cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	void field3;\n" +
		"	     ^^^^^^\n" +
		"void is an invalid type for the variable field3\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 5)\n" +
		"	void[] field4;\n" +
		"	^^^^^^\n" +
		"void[] is an invalid type\n" +
		"----------\n");
}
public void test012() {
	String expectedResult;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedResult =
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	Class c1 = java[].class;\n" +
			"	           ^^^^\n" +
			"java cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Class c2 = java.lang[].class;\n" +
			"	           ^^^^^^^^^\n" +
			"java.lang cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	Class c4 = void[].class;\n" +
			"	           ^^^^^^\n" +
			"void[] is an invalid type\n" +
			"----------\n";
	} else {
		expectedResult =
			"----------\n" +
			"1. WARNING in X.java (at line 2)\n" +
			"	Class c1 = java[].class;\n" +
			"	^^^^^\n" +
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	Class c1 = java[].class;\n" +
			"	           ^^^^\n" +
			"java cannot be resolved to a type\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 3)\n" +
			"	Class c2 = java.lang[].class;\n" +
			"	^^^^^\n" +
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 3)\n" +
			"	Class c2 = java.lang[].class;\n" +
			"	           ^^^^^^^^^\n" +
			"java.lang cannot be resolved to a type\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 4)\n" +
			"	Class c3 = void.class;\n" +
			"	^^^^^\n" +
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 5)\n" +
			"	Class c4 = void[].class;\n" +
			"	^^^^^\n" +
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 5)\n" +
			"	Class c4 = void[].class;\n" +
			"	           ^^^^^^\n" +
			"void[] is an invalid type\n" +
			"----------\n";
	}
	this.runNegativeTest(new String[] {
			"X.java",
			"public class  X {\n" +
			"	Class c1 = java[].class;\n" +
			"	Class c2 = java.lang[].class;\n" +
			"	Class c3 = void.class;\n" +
			"	Class c4 = void[].class;\n" +
			"}\n",
		},
		expectedResult);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test013() {
	String expectedResult;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedResult =
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Class c2 = java.lang[].class;\n" +
			"	           ^^^^^^^^^\n" +
			"java.lang cannot be resolved to a type\n" +
			"----------\n";
	} else {
		expectedResult =
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Class c2 = java.lang[].class;\n" +
			"	^^^^^\n" +
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Class c2 = java.lang[].class;\n" +
			"	           ^^^^^^^^^\n" +
			"java.lang cannot be resolved to a type\n" +
			"----------\n";
	}
	this.runNegativeTest(new String[] {
			"X.java",
			"public class  X {\n" +
			"	// check if no prior reference to missing 'java'\n" +
			"	Class c2 = java.lang[].class;\n" +
			"}\n",
		},
		expectedResult);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test014() {
	String expectedResult;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedResult =
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Class c2 = java.lang[].class;\n" +
			"	           ^^^^^^^^^\n" +
			"java.lang cannot be resolved to a type\n" +
			"----------\n";
	} else {
		expectedResult =
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Class c2 = java.lang[].class;\n" +
			"	^^^^^\n" +
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Class c2 = java.lang[].class;\n" +
			"	           ^^^^^^^^^\n" +
			"java.lang cannot be resolved to a type\n" +
			"----------\n";
	}
	this.runNegativeTest(new String[] {
			"X.java",
			"public class  X {\n" +
			"	// check if no prior reference to missing 'java'\n" +
			"	Class c2 = java.lang[].class;\n" +
			"}\n",
		},
		expectedResult);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test015() {
	String expectedResult;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedResult =
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	Class a = zork1[].class;\n" +
			"	          ^^^^^\n" +
			"zork1 cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Class x = zork1.zork2[].class;	// compile time error\n" +
			"	          ^^^^^\n" +
			"zork1 cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	Class a2 = zork1.class;\n" +
			"	           ^^^^^\n" +
			"zork1 cannot be resolved to a type\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 6)\n" +
			"	Class x2 = zork1.zork2.class;	// compile time error	\n" +
			"	           ^^^^^\n" +
			"zork1 cannot be resolved to a type\n" +
			"----------\n";
	} else {
		expectedResult =
			"----------\n" +
			"1. WARNING in X.java (at line 2)\n" +
			"	Class a = zork1[].class;\n" +
			"	^^^^^\n" +
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	Class a = zork1[].class;\n" +
			"	          ^^^^^\n" +
			"zork1 cannot be resolved to a type\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 3)\n" +
			"	Class x = zork1.zork2[].class;	// compile time error\n" +
			"	^^^^^\n" +
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 3)\n" +
			"	Class x = zork1.zork2[].class;	// compile time error\n" +
			"	          ^^^^^\n" +
			"zork1 cannot be resolved to a type\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 5)\n" +
			"	Class a2 = zork1.class;\n" +
			"	^^^^^\n" +
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 5)\n" +
			"	Class a2 = zork1.class;\n" +
			"	           ^^^^^\n" +
			"zork1 cannot be resolved to a type\n" +
			"----------\n" +
			"7. WARNING in X.java (at line 6)\n" +
			"	Class x2 = zork1.zork2.class;	// compile time error	\n" +
			"	^^^^^\n" +
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 6)\n" +
			"	Class x2 = zork1.zork2.class;	// compile time error	\n" +
			"	           ^^^^^\n" +
			"zork1 cannot be resolved to a type\n" +
			"----------\n";
	}
	this.runNegativeTest(new String[] {
			"X.java",
			"public class  X {\n" +
			"	Class a = zork1[].class;\n" +
			"	Class x = zork1.zork2[].class;	// compile time error\n" +
			"	\n" +
			"	Class a2 = zork1.class;\n" +
			"	Class x2 = zork1.zork2.class;	// compile time error	\n" +
			"}\n",
		},
		expectedResult);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test016() {
	this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" +
			"	java.langz.AClass1 field1;\n" +
			"	java.langz.AClass2 field2;\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	java.langz.AClass1 field1;\n" +
		"	^^^^^^^^^^\n" +
		"java.langz cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	java.langz.AClass2 field2;\n" +
		"	^^^^^^^^^^\n" +
		"java.langz cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test017() {
	this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" +
			"	java.langz field1;\n" +
			"	java.langz.AClass2 field2;\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	java.langz field1;\n" +
		"	^^^^^^^^^^\n" +
		"java.langz cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	java.langz.AClass2 field2;\n" +
		"	^^^^^^^^^^\n" +
		"java.langz cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test018() {
	this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" +
			"	java.langz.AClass1 field1;\n" +
			"	java.langz field2;\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	java.langz.AClass1 field1;\n" +
		"	^^^^^^^^^^\n" +
		"java.langz cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	java.langz field2;\n" +
		"	^^^^^^^^^^\n" +
		"java.langz cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test019() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" +
					"\n" +
					"import q1.q2.Zork;\n" +
					"\n" +
					"public class OtherFoo extends Zork{\n" +
					"	public class OtherMember extends Zork {}\n" +
					"	public Zork foo;\n" +
					"	public Zork bar() {	return null; }\n" +
					"	public void baz(Zork z) {}\n" +
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" +
					"public class Zork {\n" +
					"}\n",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" +
					"	void foo(p.OtherFoo ofoo) {\n" + // triggers OtherFoo loading, and q1.q2 pkg creation (for unresolved binary type refs)
					"		a.b.Missing1 m1;\n" +
					"		q1.q2.Missing2 m2;\n" +
					"	}\n" +
					"}	\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	a.b.Missing1 m1;\n" +
			"	^\n" +
			"a cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	q1.q2.Missing2 m2;\n" +
			"	^^^^^^^^^^^^^^\n" +
			"q1.q2.Missing2 cannot be resolved to a type\n" +
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test020() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" +
					"\n" +
					"import q1.q2.Zork;\n" +
					"\n" +
					"public class OtherFoo extends Zork{\n" +
					"	public class OtherMember extends Zork {}\n" +
					"	public Zork foo;\n" +
					"	public Zork bar() {	return null; }\n" +
					"	public void baz(Zork z) {}\n" +
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" +
					"public class Zork {\n" +
					"}\n",
			},
			"");

	// no need to delete Zork actually - any lazy reference would cause q1.q2 to be created as a package

	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" +
					"	void foo(p.OtherFoo ofoo) {\n" + // triggers OtherFoo loading, and q1.q2 pkg creation (for unresolved binary type refs)
					"		a.b.Missing1 m1;\n" +
					"		q1.q2.Missing2 m2;\n" +
					"	}\n" +
					"}	\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	a.b.Missing1 m1;\n" +
			"	^\n" +
			"a cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	q1.q2.Missing2 m2;\n" +
			"	^^^^^^^^^^^^^^\n" +
			"q1.q2.Missing2 cannot be resolved to a type\n" +
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test021() {
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" +
					"	void foo(p.OtherFoo ofoo) {\n" +
					"		a.b.Missing1 m1;\n" +
					"		q1.q2.Missing2 m2;\n" +
					"	}\n" +
					"}	\n",
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" +
					"public class OtherFoo extends q1.q2.Zork{\n" +
					"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	a.b.Missing1 m1;\n" +
			"	^\n" +
			"a cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	q1.q2.Missing2 m2;\n" +
			"	^^\n" +
			"q1 cannot be resolved to a type\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in p\\OtherFoo.java (at line 2)\n" +
			"	public class OtherFoo extends q1.q2.Zork{\n" +
			"	                              ^^\n" +
			"q1 cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test022() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" +
					"\n" +
					"import q1.q2.Zork;\n" +
					"\n" +
					"public class OtherFoo {\n" +
					"	public Zork foo;\n" +
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" +
					"public class Zork {\n" +
					"}\n",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" +
					"	void foo(q1.q2.Missing1 m1) {\n" +
					"		a.b.Missing1 m1a;\n" +
					"		p.OtherFoo ofoo;\n" + // triggers OtherFoo loading, and q1.q2 pkg creation (for unresolved binary type refs)
					"		q1.q2.Missing1 m11;\n" +
					"	}\n" +
					"}	\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	void foo(q1.q2.Missing1 m1) {\n" +
			"	         ^^\n" +
			"q1 cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	a.b.Missing1 m1a;\n" +
			"	^\n" +
			"a cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	q1.q2.Missing1 m11;\n" +
			"	^^\n" +
			"q1 cannot be resolved to a type\n" +
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test023() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" +
					"\n" +
					"import q1.q2.Zork;\n" +
					"\n" +
					"public class OtherFoo {\n" +
					"	public Zork foo;\n" +
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" +
					"public class Zork {\n" +
					"}\n",
			},
			"");

	// leave package behind

	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" +
					"	void foo(q1.q2.Missing1 m1) {\n" +
					"		a.b.Missing1 m1a;\n" +
					"		p.OtherFoo ofoo;\n" +
					"		q1.q2.Missing1 m11;\n" +
					"	}\n" +
					"}	\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	void foo(q1.q2.Missing1 m1) {\n" +
			"	         ^^^^^^^^^^^^^^\n" +
			"q1.q2.Missing1 cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	a.b.Missing1 m1a;\n" +
			"	^\n" +
			"a cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	q1.q2.Missing1 m11;\n" +
			"	^^^^^^^^^^^^^^\n" +
			"q1.q2.Missing1 cannot be resolved to a type\n" +
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test024() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" +
					"\n" +
					"import q1.q2.Zork;\n" +
					"\n" +
					"public class OtherFoo {\n" +
					"	public Zork foo;\n" +
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" +
					"public class Zork {\n" +
					"}\n",
			},
			"");

	// delete binary folder q1/q2 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1/q2"));

	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" +
					"	void foo(q1.q2.Missing1 m1) {\n" +
					"		a.b.Missing1 m1a;\n" +
					"		p.OtherFoo ofoo;\n" + // triggers OtherFoo loading, and q1.q2 pkg creation (for unresolved binary type refs)
					"		q1.q2.Missing1 m11;\n" +
					"	}\n" +
					"}	\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	void foo(q1.q2.Missing1 m1) {\n" +
			"	         ^^^^^\n" +
			"q1.q2 cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	a.b.Missing1 m1a;\n" +
			"	^\n" +
			"a cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	q1.q2.Missing1 m11;\n" +
			"	^^^^^^^^^^^^^^\n" +
			"q1.q2.Missing1 cannot be resolved to a type\n" + // inconsistent msg from previous one (error 1)
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test025() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" +
					"\n" +
					"import q1.q2.Zork;\n" +
					"\n" +
					"public class OtherFoo {\n" +
					"	public Zork foo;\n" +
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" +
					"public class Zork {\n" +
					"}\n",
			},
			"");

	// delete binary folder q1/q2 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1/q2"));

	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" +
					"	void foo(q1.q2.Missing1 m1) {\n" +
					"		a.b.Missing1 m1a;\n" +
					"		p.OtherFoo ofoo;\n" + // triggers OtherFoo loading, and q1.q2 pkg creation (for unresolved binary type refs)
					"	}\n" +
					"}	\n",
					"Y.java", //-----------------------------------------------------------------------
					"public class Y {\n" +
					"	void foo() {\n" +
					"		q1.q2.Missing1 m11;\n" +
					"	}\n" +
					"}	\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	void foo(q1.q2.Missing1 m1) {\n" +
			"	         ^^^^^\n" +
			"q1.q2 cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	a.b.Missing1 m1a;\n" +
			"	^\n" +
			"a cannot be resolved to a type\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in Y.java (at line 3)\n" +
			"	q1.q2.Missing1 m11;\n" +
			"	^^^^^^^^^^^^^^\n" +
			"q1.q2.Missing1 cannot be resolved to a type\n" +  // inconsistent msg from previous one (error 1)
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test026() {
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" +
					"	void foo(Missing1 m1) {\n" +
					"		Missing2 m2 = m1;\n" +
					"	}\n" +
					"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	void foo(Missing1 m1) {\n" +
			"	         ^^^^^^^^\n" +
			"Missing1 cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Missing2 m2 = m1;\n" +
			"	^^^^^^^^\n" +
			"Missing2 cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test027() {
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" +
					"	void foo(X x) {\n" +
					"		new Other().m2 = x;\n" +
					"		Other other = new Other();\n" +
					"		other.m2 = x;\n" +
					"		other.m2.m3 = x;\n" +
					"	}\n" +
					"}\n" +
					"\n" +
					"class Other {\n" +
					"	Missing2 m2;\n" +
					"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new Other().m2 = x;\n" +
			"	^^^^^^^^^^^^^^\n" +
			"Missing2 cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	other.m2 = x;\n" +
			"	^^^^^^^^\n" +
			"Missing2 cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	other.m2.m3 = x;\n" +
			"	^^^^^^^^\n" +
			"Missing2 cannot be resolved to a type\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 11)\n" +
			"	Missing2 m2;\n" +
			"	^^^^^^^^\n" +
			"Missing2 cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test028() {
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" +
					"	void foo(X x) {\n" +
					"		System.out.println(new Other().m2.m3);\n" +
					"		System.out.println(new Other().m2.m3());\n" +
					"		Missing2.foo();\n" +
					"	}\n" +
					"}\n" +
					"\n" +
					"class Other {\n" +
					"	Missing2 m2;\n" +
					"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(new Other().m2.m3);\n" +
			"	                   ^^^^^^^^^^^^^^\n" +
			"Missing2 cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	System.out.println(new Other().m2.m3());\n" +
			"	                   ^^^^^^^^^^^^^^\n" +
			"Missing2 cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	Missing2.foo();\n" +
			"	^^^^^^^^\n" +
			"Missing2 cannot be resolved\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 10)\n" +
			"	Missing2 m2;\n" +
			"	^^^^^^^^\n" +
			"Missing2 cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test029() throws Exception {
	this.runNegativeTest(
			new String[] {
				"Y.java", //-----------------------------------------------------------------------
				"public class Y extends Z {\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in Y.java (at line 1)\n" +
			"	public class Y extends Z {\n" +
			"	                       ^\n" +
			"Z cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	// check Y superclass in problem classfile: shoud not be Z otherwise the class cannot load
	String expectedOutput =
		"public class Y {\n";

	File f = new File(OUTPUT_DIR + File.separator + "Y.class");
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test030() {
	this.runNegativeTest(
			new String[] {
				"Y.java", //-----------------------------------------------------------------------
				"public class Y extends Z {\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in Y.java (at line 1)\n" +
			"	public class Y extends Z {\n" +
			"	                       ^\n" +
			"Z cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X extends Y {\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X extends Y {\n" +
			"	             ^\n" +
			"The hierarchy of the type X is inconsistent\n" +
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test031() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X extends Y {\n" +
				"}\n",
				"Y.java", //-----------------------------------------------------------------------
				"public class Y extends Z {\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X extends Y {\n" +
			"	             ^\n" +
			"The hierarchy of the type X is inconsistent\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in Y.java (at line 1)\n" +
			"	public class Y extends Z {\n" +
			"	                       ^\n" +
			"Z cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test032() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"import existing.*;\n" +
				"public class X {\n" +
				"  void foo(p.Zork z) {}\n" +
				"  void bar(Zork z) {} // should bind to existing.Zork\n" +
				"}\n",
				"p/Clyde.java", //-----------------------------------------------------------------------
				"package p;\n" + // just so package p does exist
				"public class Clyde {\n" +
				"}\n",
				"existing/Zork.java", //-----------------------------------------------------------------------
				"package existing;\n" +
				"public class Zork {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	void foo(p.Zork z) {}\n" +
			"	         ^^^^^^\n" +
			"p.Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test033() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		return;
	}
	this.runNegativeTest(
			new String[] {
				"Y.java", //-----------------------------------------------------------------------
				"@Z public class Y {\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in Y.java (at line 1)\n" +
			"	@Z public class Y {\n" +
			"	 ^\n" +
			"Z cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	runConformTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"X.java", //-----------------------------------------------------------------------
			"public class X extends Y {\n" +
			"}\n",
		},
		// compiler results
		"" /* expected compiler log */,
		// runtime results
		"" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test034() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o = x1.bar();\n" +
				"	}\n" +
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	Zork bar() { return null; }	\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object o = x1.bar();\n" +
			"	              ^^^\n" +
			"The method bar() from the type X1 refers to the missing type Zork\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	Zork bar() { return null; }	\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test035() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	Zork bar() { return null; }	\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	Zork bar() { return null; }	\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o = x1.bar();\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object o = x1.bar();\n" +
			"	              ^^^\n" +
			"The method bar() from the type X1 refers to the missing type Zork\n" +
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test036() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o = x1.bar(x1);\n" +
				"	}\n" +
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	Object bar(Zork z) { return null; }	\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object o = x1.bar(x1);\n" +
			"	              ^^^\n" +
			"The method bar(Zork) from the type X1 refers to the missing type Zork\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	Object bar(Zork z) { return null; }	\n" +
			"	           ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test037() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	Object bar(Zork z) { return null; }	\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	Object bar(Zork z) { return null; }	\n" +
			"	           ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o = x1.bar(x1);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object o = x1.bar(x1);\n" +
			"	              ^^^\n" +
			"The method bar(Zork) from the type X1 refers to the missing type Zork\n" +
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test038() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o = x1.bar(x1);\n" +
				"	}\n" +
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	Object bar(Object o) throws Zork { return null; }	\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object o = x1.bar(x1);\n" +
			"	              ^^^\n" +
			"The method bar(Object) from the type X1 refers to the missing type Zork\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	Object bar(Object o) throws Zork { return null; }	\n" +
			"	                            ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test039() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	Object bar(Object o) throws Zork { return null; }	\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	Object bar(Object o) throws Zork { return null; }	\n" +
			"	                            ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o = x1.bar(x1);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object o = x1.bar(x1);\n" +
			"	              ^^^\n" +
			"The method bar(Object) from the type X1 refers to the missing type Zork\n" +
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test040() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o = new X1(x1);\n" +
				"	}\n" +
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	public X1(Zork z) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object o = new X1(x1);\n" +
			"	           ^^^^^^^^^^\n" +
			"The constructor X1(Zork) refers to the missing type Zork\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	public X1(Zork z) {}\n" +
			"	          ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test041() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	public X1(Zork z) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	public X1(Zork z) {}\n" +
			"	          ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o = new X1(x1);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object o = new X1(x1);\n" +
			"	           ^^^^^^^^^^\n" +
			"The constructor X1(Zork) refers to the missing type Zork\n" +
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test042() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o = new X1();\n" +
				"	}\n" +
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	public X1() throws Zork {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object o = new X1();\n" +
			"	           ^^^^^^^^\n" +
			"The constructor X1() refers to the missing type Zork\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	public X1() throws Zork {}\n" +
			"	                   ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test043() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	public X1() throws Zork {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	public X1() throws Zork {}\n" +
			"	                   ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o = new X1();\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object o = new X1();\n" +
			"	           ^^^^^^^^\n" +
			"The constructor X1() refers to the missing type Zork\n" +
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test044() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o = new X1(x1){};\n" +
				"	}\n" +
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	public X1(Zork z) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object o = new X1(x1){};\n" +
			"	               ^^^^^^\n" +
			"The constructor X1(Zork) refers to the missing type Zork\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	public X1(Zork z) {}\n" +
			"	          ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test045() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	public X1(Zork z) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	public X1(Zork z) {}\n" +
			"	          ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o = new X1(x1){};\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object o = new X1(x1){};\n" +
			"	               ^^^^^^\n" +
			"The constructor X1(Zork) refers to the missing type Zork\n" +
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test046() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o = new X1(){};\n" +
				"	}\n" +
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	public X1() throws Zork {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object o = new X1(){};\n" +
			"	               ^^^^\n" +
			"The constructor X1() refers to the missing type Zork\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	public X1() throws Zork {}\n" +
			"	                   ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test047() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	public X1() throws Zork {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	public X1() throws Zork {}\n" +
			"	                   ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o = new X1(){};\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object o = new X1(){};\n" +
			"	               ^^^^\n" +
			"The constructor X1() refers to the missing type Zork\n" +
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test048() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X extends X1 {\n" +
				"	X(X1 x1) {\n" +
				"		super(x1);\n" +
				"	}\n" +
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	public X1(Zork z) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	super(x1);\n" +
			"	^^^^^^^^^^\n" +
			"The constructor X1(Zork) refers to the missing type Zork\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	public X1(Zork z) {}\n" +
			"	          ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test049() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	public X1(Zork z) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	public X1(Zork z) {}\n" +
			"	          ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X extends X1 {\n" +
				"	X(X1 x1) {\n" +
				"		super(x1);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	super(x1);\n" +
			"	^^^^^^^^^^\n" +
			"The constructor X1(Zork) refers to the missing type Zork\n" +
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test050() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X extends X1 {\n" +
				"	X(X1 x1) {\n" +
				"		super();\n" +
				"	}\n" +
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	public X1() throws Zork {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"The constructor X1() refers to the missing type Zork\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	public X1() throws Zork {}\n" +
			"	                   ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test051() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	public X1() throws Zork {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X1.java (at line 2)\n" +
			"	public X1() throws Zork {}\n" +
			"	                   ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X extends X1 {\n" +
				"	X(X1 x1) {\n" +
				"		super();\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"The constructor X1() refers to the missing type Zork\n" +
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test052() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o;\n" +
				"		o = x1.next.zork;\n" +
				"		o = this.zork;\n" +
				"		o = zork;\n" +
				"		o = x1.next.zork.foo();\n" +
				"		o = this.zork.foo();\n" +
				"		o = zork.foo();\n" +
				"	}\n" +
				"	Zork zork;\n" +
				"}\n" +
				"class X1 {\n" +
				"	X1 next;\n" +
				"	Zork zork;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	o = x1.next.zork;\n" +
			"	    ^^^^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	o = this.zork;\n" +
			"	    ^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	o = zork;\n" +
			"	    ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	o = x1.next.zork.foo();\n" +
			"	    ^^^^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 8)\n" +
			"	o = this.zork.foo();\n" +
			"	    ^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 9)\n" +
			"	o = zork.foo();\n" +
			"	    ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 11)\n" +
			"	Zork zork;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 15)\n" +
			"	Zork zork;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test053() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void bar(X1 x1) {\n" +
				"		String s;\n" +
				"		s = x1.next.zork;\n" +
				"		s = this.zork;\n" +
				"		s = zork;\n" +
				"		s = x1.next.zork.foo();\n" +
				"		s = this.zork.foo();\n" +
				"		s = zork.foo();\n" +
				"	}	\n" +
				"	Zork zork;\n" +
				"}\n" +
				"class X1 {\n" +
				"	X1 next;\n" +
				"	Zork zork;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	s = x1.next.zork;\n" +
			"	    ^^^^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	s = this.zork;\n" +
			"	    ^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	s = zork;\n" +
			"	    ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	s = x1.next.zork.foo();\n" +
			"	    ^^^^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 8)\n" +
			"	s = this.zork.foo();\n" +
			"	    ^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 9)\n" +
			"	s = zork.foo();\n" +
			"	    ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 11)\n" +
			"	Zork zork;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 15)\n" +
			"	Zork zork;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test054() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void baz(X1 x1) {\n" +
				"		Zork z;\n" +
				"		z = x1.next.zork;\n" +
				"		z = this.zork;\n" +
				"		z = zork;\n" +
				"		z = x1.next.zork.foo();\n" +
				"		z = this.zork.foo();\n" +
				"		z = zork.foo();\n" +
				"	}	\n" +
				"	Zork zork;\n" +
				"}\n" +
				"class X1 {\n" +
				"	X1 next;\n" +
				"	Zork zork;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	z = x1.next.zork;\n" +
			"	    ^^^^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	z = this.zork;\n" +
			"	    ^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 6)\n" +
			"	z = zork;\n" +
			"	    ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 7)\n" +
			"	z = x1.next.zork.foo();\n" +
			"	    ^^^^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 8)\n" +
			"	z = this.zork.foo();\n" +
			"	    ^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 9)\n" +
			"	z = zork.foo();\n" +
			"	    ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 11)\n" +
			"	Zork zork;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"9. ERROR in X.java (at line 15)\n" +
			"	Zork zork;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test055() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	public X1 next;\n" +
				"	public Zork zork;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X1.java (at line 3)\n" +
			"	public Zork zork;\n" +
			"	       ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		Object o;\n" +
				"		o = x1.next.zork;\n" +
				"		o = this.zork;\n" +
				"		o = zork;\n" +
				"		o = x1.next.zork.foo();\n" +
				"		o = this.zork.foo();\n" +
				"		o = zork.foo();\n" +
				"	}\n" +
				"	Zork zork;\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	o = x1.next.zork;\n" +
			"	    ^^^^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	o = this.zork;\n" +
			"	    ^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	o = zork;\n" +
			"	    ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	o = x1.next.zork.foo();\n" +
			"	    ^^^^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 8)\n" +
			"	o = this.zork.foo();\n" +
			"	    ^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 9)\n" +
			"	o = zork.foo();\n" +
			"	    ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 11)\n" +
			"	Zork zork;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test056() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	public X1 next;\n" +
				"	public Zork zork;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X1.java (at line 3)\n" +
			"	public Zork zork;\n" +
			"	       ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void bar(X1 x1) {\n" +
				"		String s;\n" +
				"		s = x1.next.zork;\n" +
				"		s = this.zork;\n" +
				"		s = zork;\n" +
				"		s = x1.next.zork.foo();\n" +
				"		s = this.zork.foo();\n" +
				"		s = zork.foo();\n" +
				"	}	\n" +
				"	Zork zork;\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	s = x1.next.zork;\n" +
			"	    ^^^^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	s = this.zork;\n" +
			"	    ^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	s = zork;\n" +
			"	    ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	s = x1.next.zork.foo();\n" +
			"	    ^^^^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 8)\n" +
			"	s = this.zork.foo();\n" +
			"	    ^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 9)\n" +
			"	s = zork.foo();\n" +
			"	    ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 11)\n" +
			"	Zork zork;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test057() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" +
				"	public X1 next;\n" +
				"	public Zork zork;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X1.java (at line 3)\n" +
			"	public Zork zork;\n" +
			"	       ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void baz(X1 x1) {\n" +
				"		Zork z;\n" +
				"		z = x1.next.zork;\n" +
				"		z = this.zork;\n" +
				"		z = zork;\n" +
				"		z = x1.next.zork.foo();\n" +
				"		z = this.zork.foo();\n" +
				"		z = zork.foo();\n" +
				"	}	\n" +
				"	Zork zork;\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	z = x1.next.zork;\n" +
			"	    ^^^^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	z = this.zork;\n" +
			"	    ^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 6)\n" +
			"	z = zork;\n" +
			"	    ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 7)\n" +
			"	z = x1.next.zork.foo();\n" +
			"	    ^^^^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 8)\n" +
			"	z = this.zork.foo();\n" +
			"	    ^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 9)\n" +
			"	z = zork.foo();\n" +
			"	    ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 11)\n" +
			"	Zork zork;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test058() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		x1.bar().baz();\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class X1 {\n" +
				"	Zork bar(){}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	x1.bar().baz();\n" +
			"	   ^^^\n" +
			"The method bar() from the type X1 refers to the missing type Zork\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	Zork bar(){}\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test059() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		new X1(x1).baz();\n" +
				"		new X1(null).baz();\n" +
				"		new Zork().baz();\n" +
				"		new X1(x1){}.baz();\n" +
				"		new X1(null){}.baz();\n" +
				"		new Zork(){}.baz();\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class X1 {\n" +
				"	X1(Zork z) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new X1(x1).baz();\n" +
			"	^^^^^^^^^^\n" +
			"The constructor X1(Zork) refers to the missing type Zork\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	new X1(x1).baz();\n" +
			"	           ^^^\n" +
			"The method baz() is undefined for the type X1\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	new X1(null).baz();\n" +
			"	^^^^^^^^^^^^\n" +
			"The constructor X1(Zork) refers to the missing type Zork\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 4)\n" +
			"	new X1(null).baz();\n" +
			"	             ^^^\n" +
			"The method baz() is undefined for the type X1\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 5)\n" +
			"	new Zork().baz();\n" +
			"	    ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 6)\n" +
			"	new X1(x1){}.baz();\n" +
			"	    ^^^^^^\n" +
			"The constructor X1(Zork) refers to the missing type Zork\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 6)\n" +
			"	new X1(x1){}.baz();\n" +
			"	             ^^^\n" +
			"The method baz() is undefined for the type new X1(){}\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 7)\n" +
			"	new X1(null){}.baz();\n" +
			"	    ^^^^^^^^\n" +
			"The constructor X1(Zork) refers to the missing type Zork\n" +
			"----------\n" +
			"9. ERROR in X.java (at line 7)\n" +
			"	new X1(null){}.baz();\n" +
			"	               ^^^\n" +
			"The method baz() is undefined for the type new X1(){}\n" +
			"----------\n" +
			"10. ERROR in X.java (at line 8)\n" +
			"	new Zork(){}.baz();\n" +
			"	    ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"11. ERROR in X.java (at line 13)\n" +
			"	X1(Zork z) {}\n" +
			"	   ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test060() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(Zork z) {\n" +
				"		z.bar();\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	void foo(Zork z) {\n" +
			"	         ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test061() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(X1 x1) {\n" +
				"		int i = x1.next.z;\n" +
				"		int j = x1.next.zArray;\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class X1 {\n" +
				"	X1 next;\n" +
				"	Zork z;\n" +
				"	Zork[] zArray;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	int i = x1.next.z;\n" +
			"	        ^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	int j = x1.next.zArray;\n" +
			"	        ^^^^^^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 11)\n" +
			"	Zork[] zArray;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test062() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	X1 x1;\n" +
				"	void foo() {\n" +
				"		int i = x1.next.z;\n" +
				"		int j = x1.next.zArray;\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class X1 {\n" +
				"	X1 next;\n" +
				"	Zork z;\n" +
				"	Zork[] zArray;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	int i = x1.next.z;\n" +
			"	        ^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	int j = x1.next.zArray;\n" +
			"	        ^^^^^^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 11)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 12)\n" +
			"	Zork[] zArray;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test063() {
	this.runNegativeTest(
			new String[] {
				"p/X.java", //-----------------------------------------------------------------------
				"package p;\n" +
				"public class X {\n" +
				"	void foo() {\n" +
				"		int i = p.X1.z;\n" +
				"		int j = p.X1.zArray;\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class X1 {\n" +
				"	static Zork z;\n" +
				"	static Zork[] zArray;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in p\\X.java (at line 4)\n" +
			"	int i = p.X1.z;\n" +
			"	        ^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in p\\X.java (at line 5)\n" +
			"	int j = p.X1.zArray;\n" +
			"	        ^^^^^^^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in p\\X.java (at line 10)\n" +
			"	static Zork z;\n" +
			"	       ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"4. ERROR in p\\X.java (at line 11)\n" +
			"	static Zork[] zArray;\n" +
			"	       ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test064() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"import java.io.*;		\n" +
				"public class X {\n" +
				"    void foo() {\n" +
				"        Serializable[] v= new ArrayListExtra[10];\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	Serializable[] v= new ArrayListExtra[10];\n" +
			"	                      ^^^^^^^^^^^^^^\n" +
			"ArrayListExtra cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test065() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"import java.io.*;		\n" +
				"public class X {\n" +
				"    void foo() {\n" +
				"    	int l = array.length;\n" +
				"    	Object o = array[1];\n" +
				"\n" +
				"    }\n" +
				"    Zork[] array;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	int l = array.length;\n" +
			"	        ^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	Object o = array[1];\n" +
			"	           ^^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	Zork[] array;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test066() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"        void foo() {\n" +
				"                Zork z1 = null;\n" +
				"                Object o = z1;\n" +
				"                Object o1 = z1.z2;\n" +
				"                Object o2 = bar();\n" +
				"                Zork[] array = null;\n" +
				"                int length = array.length;\n" +
				"        }\n" +
				"        Zork bar() {\n" +
				"        }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Zork z1 = null;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	Object o2 = bar();\n" +
			"	            ^^^\n" +
			"The method bar() from the type X refers to the missing type Zork\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	Zork[] array = null;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 10)\n" +
			"	Zork bar() {\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test067() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4)
		return;
	this.runNegativeTest(
			new String[] {
				"E.java", //-----------------------------------------------------------------------
				"public class E<T> {\n" +
				"    class SomeType { }\n" +
				"    void foo() {\n" +
				"        E<XYX> list= new E<SomeType>();\n" +
				"        list = new E<SomeType>();\n" +
				"    }\n" +
				"    E<XYX> fList= new E<SomeType>();\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in E.java (at line 4)\n" +
			"	E<XYX> list= new E<SomeType>();\n" +
			"	  ^^^\n" +
			"XYX cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in E.java (at line 7)\n" +
			"	E<XYX> fList= new E<SomeType>();\n" +
			"	  ^^^\n" +
			"XYX cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test068() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4)
		return;
	this.runNegativeTest(
			new String[] {
				"E.java", //-----------------------------------------------------------------------
				"import java.util.Map;\n" +
				"public class E<T> {\n" +
				"    static class SomeType { }\n" +
				"    void foo() {\n" +
				"        E<Map<String, ? extends XYX>> list= new E<Map<String, ? extends SomeType>>() {\n" +
				"        };\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in E.java (at line 5)\n" +
			"	E<Map<String, ? extends XYX>> list= new E<Map<String, ? extends SomeType>>() {\n" +
			"	                        ^^^\n" +
			"XYX cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test069() {
	this.runNegativeTest(
			new String[] {
				"p/X.java", //-----------------------------------------------------------------------
				"package p;\n" +
				"public class X {\n" +
				"	IOException foo() {}\n" +
				"}\n",
				"p/Y.java", //-----------------------------------------------------------------------
				"package p;\n" +
				"import java.io.*;\n" +
				"public class Y {\n" +
				"   void foo(IOException e) {}\n" +
				"   void bar(Zork z) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in p\\X.java (at line 3)\n" +
			"	IOException foo() {}\n" +
			"	^^^^^^^^^^^\n" +
			"IOException cannot be resolved to a type\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in p\\Y.java (at line 5)\n" +
			"	void bar(Zork z) {}\n" +
			"	         ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test070() {
	this.runNegativeTest(
			new String[] {
				"p/X.java", //-----------------------------------------------------------------------
				"package p;\n" +
				"public class X {\n" +
				"	IOException foo() {}\n" +
				"}\n",
				"q/Y.java", //-----------------------------------------------------------------------
				"package q;\n" +
				"import p.*;\n" +
				"import java.io.*;\n" +
				"public class Y {\n" +
				"   void foo(IOException e) {}\n" +
				"   void bar(Zork z) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in p\\X.java (at line 3)\n" +
			"	IOException foo() {}\n" +
			"	^^^^^^^^^^^\n" +
			"IOException cannot be resolved to a type\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in q\\Y.java (at line 6)\n" +
			"	void bar(Zork z) {}\n" +
			"	         ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test071() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	IOException foo() {}\n" +
				"}\n",
				"Y.java", //-----------------------------------------------------------------------
				"import java.io.*;\n" +
				"public class Y {\n" +
				"   void foo(IOException e) {}\n" +
				"   void bar(Zork z) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	IOException foo() {}\n" +
			"	^^^^^^^^^^^\n" +
			"IOException cannot be resolved to a type\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in Y.java (at line 4)\n" +
			"	void bar(Zork z) {}\n" +
			"	         ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test072() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	public void foo() throws Foo {\n" +
				"	}\n" +
				"	public void bar() throws Zork {\n" +
				"	}	\n" +
				"}\n" +
				"\n" +
				"class Foo extends Zork {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public void foo() throws Foo {\n" +
			"	                         ^^^\n" +
			"No exception of type Foo can be thrown; an exception type must be a subclass of Throwable\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	public void bar() throws Zork {\n" +
			"	                         ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	class Foo extends Zork {\n" +
			"	                  ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test073() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	/**\n" +
				"	 * @see Foo.Private#foo()\n" +
				"	 * @param p\n" +
				"	 */\n" +
				"	void foo(Foo.Private p) {\n" +
				"		p.foo();\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class Foo {\n" +
				"	private class Private {\n" +
				"		private void foo(){}\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	void foo(Foo.Private p) {\n" +
			"	         ^^^^^^^^^^^\n" +
			"The type Foo.Private is not visible\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	p.foo();\n" +
			"	^\n" +
			"The type Foo.Private is not visible\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 12)\n" +
			"	private class Private {\n" +
			"	              ^^^^^^^\n" +
			"The type Foo.Private is never used locally\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 13)\n" +
			"	private void foo(){}\n" +
			"	             ^^^^^\n" +
			"The method foo() from the type Foo.Private is never used locally\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test074() {
	String expected = this.complianceLevel <= ClassFileConstants.JDK1_4
		? 		"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	bar1().foo();\n" +
				"	^^^^\n" +
				"The method bar1() from the type X refers to the missing type Zork\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	bar2();\n" +
				"	^^^^\n" +
				"The method bar2() from the type X refers to the missing type Zork\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	bar3(null);\n" +
				"	^^^^\n" +
				"The method bar3(Zork) from the type X refers to the missing type Zork\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 7)\n" +
				"	bar4(null,null);\n" +
				"	^^^^\n" +
				"The method bar4(Zork) from the type X refers to the missing type Zork\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 9)\n" +
				"	Zork<String> bar1() {}\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 9)\n" +
				"	Zork<String> bar1() {}\n" +
				"	     ^^^^^^\n" +
				"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 10)\n" +
				"	List<Zork> bar2() {}\n" +
				"	     ^^^^\n" +
				"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 10)\n" +
				"	List<Zork> bar2() {}\n" +
				"	     ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"9. ERROR in X.java (at line 11)\n" +
				"	void bar3(Zork<String> z) {}\n" +
				"	          ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"10. ERROR in X.java (at line 11)\n" +
				"	void bar3(Zork<String> z) {}\n" +
				"	               ^^^^^^\n" +
				"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"11. ERROR in X.java (at line 12)\n" +
				"	void bar4(Zork<String,String> z) {}\n" +
				"	          ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"12. ERROR in X.java (at line 12)\n" +
				"	void bar4(Zork<String,String> z) {}\n" +
				"	               ^^^^^^^^^^^^^\n" +
				"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
				"----------\n"
		: 		"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	bar1().foo();\n" +
				"	^^^^\n" +
				"The method bar1() from the type X refers to the missing type Zork\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	Object o = bar2();\n" +
				"	           ^^^^\n" +
				"The method bar2() from the type X refers to the missing type Zork\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	bar3(null);\n" +
				"	^^^^\n" +
				"The method bar3(Zork<String>) from the type X refers to the missing type Zork\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 7)\n" +
				"	bar4(null,null);\n" +
				"	^^^^\n" +
				"The method bar4(Zork<String,String>) from the type X refers to the missing type Zork\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 9)\n" +
				"	Zork<String> bar1() {}\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 10)\n" +
				"	List<Zork> bar2() {}\n" +
				"	     ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 11)\n" +
				"	void bar3(Zork<String> z) {}\n" +
				"	          ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 12)\n" +
				"	void bar4(Zork<String,String> z) {}\n" +
				"	          ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n";

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"import java.util.List;\n" +
				"public class X {\n" +
				"	void foo() {\n" +
				"		bar1().foo();\n" +
				"		Object o = bar2();\n" +
				"		bar3(null);\n" +
				"		bar4(null,null);\n" +
				"	}\n" +
				"	Zork<String> bar1() {}\n" +
				"	List<Zork> bar2() {}\n" +
				"	void bar3(Zork<String> z) {}\n" +
				"	void bar4(Zork<String,String> z) {}\n" +
				"}\n",//-----------------------------------------------------------------------
			},
			expected);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test075() {
	String expected = this.complianceLevel <= ClassFileConstants.JDK1_4
		? 		"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" +
				"	     ^^^\n" +
				"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" +
				"	               ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" +
				"	                    ^^^^^^^^^^^^^^^^^^^\n" +
				"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? extends Number>) o;\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? extends Number>) o;\n" +
				"	            ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? extends Number>) o;\n" +
				"	                 ^^^^^^^^^^^^^^^^^^^\n" +
				"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
				"----------\n"
		: 		"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" +
				"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Type safety: Unchecked cast from Object to Zork<?,? extends Number>\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" +
				"	               ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"4. WARNING in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? extends Number>) o;\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Type safety: Unchecked cast from Object to Zork<?,? extends Number>\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? extends Number>) o;\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Zork<capture#3-of ?,capture#4-of ? extends Number> cannot be resolved to a type\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? extends Number>) o;\n" +
				"	            ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n";

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(Object o) {\n" +
				"		Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" +
				"		String s = (Zork<?, ? extends Number>) o;\n" +
				"	}\n" +
				"}\n",//-----------------------------------------------------------------------
			},
			expected);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test076() {
	String expected = this.complianceLevel <= ClassFileConstants.JDK1_4
		? 		"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? super Number>) o;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? super Number>) o;\n" +
				"	     ^^^\n" +
				"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? super Number>) o;\n" +
				"	               ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? super Number>) o;\n" +
				"	                    ^^^^^^^^^^^^^^^^^\n" +
				"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? super Number>) o;\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? super Number>) o;\n" +
				"	            ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? super Number>) o;\n" +
				"	                 ^^^^^^^^^^^^^^^^^\n" +
				"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
				"----------\n"
		: 		"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? super Number>) o;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? super Number>) o;\n" +
				"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Type safety: Unchecked cast from Object to Zork<?,? super Number>\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? super Number>) o;\n" +
				"	               ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"4. WARNING in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? super Number>) o;\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Type safety: Unchecked cast from Object to Zork<?,? super Number>\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? super Number>) o;\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Zork<capture#3-of ?,capture#4-of ? super Number> cannot be resolved to a type\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? super Number>) o;\n" +
				"	            ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n";

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(Object o) {\n" +
				"		Zork<?,?> z = (Zork<?, ? super Number>) o;\n" +
				"		String s = (Zork<?, ? super Number>) o;\n" +
				"	}\n" +
				"}\n",//-----------------------------------------------------------------------
			},
			expected);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test077() {
	String expected = this.complianceLevel <= ClassFileConstants.JDK1_4
		? 		"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" +
				"	     ^^^\n" +
				"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" +
				"	               ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" +
				"	                    ^^^^^^^^^^^^^^^^^^^\n" +
				"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? extends Number[]>) o;\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? extends Number[]>) o;\n" +
				"	            ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? extends Number[]>) o;\n" +
				"	                 ^^^^^^^^^^^^^^^^^^^^^\n" +
				"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
				"----------\n"
		: 		"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" +
				"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Type safety: Unchecked cast from Object to Zork<?,? super Number[]>\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 3)\n" +
				"	Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" +
				"	               ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"4. WARNING in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? extends Number[]>) o;\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Type safety: Unchecked cast from Object to Zork<?,? extends Number[]>\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? extends Number[]>) o;\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Zork<capture#3-of ?,capture#4-of ? extends Number[]> cannot be resolved to a type\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 4)\n" +
				"	String s = (Zork<?, ? extends Number[]>) o;\n" +
				"	            ^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n";

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	void foo(Object o) {\n" +
				"		Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" +
				"		String s = (Zork<?, ? extends Number[]>) o;\n" +
				"	}\n" +
				"}\n",//-----------------------------------------------------------------------
			},
			expected);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220967
public void test078() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"import java.util.List;\n" +
				"interface B {\n" +
				"  B m(String seq);\n" +
				"}\n" +
				"public class X implements B {\n" +
				"	public Zork m(String arg0) {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n",//-----------------------------------------------------------------------
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	public Zork m(String arg0) {\n" +
			"	       ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220967 - variation
public void test079() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" +
				"	public Zork m(X x) {\n" +
				"		return x;\n" +
				"	}\n" +
				"}\n",//-----------------------------------------------------------------------
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public Zork m(X x) {\n" +
			"	       ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220967 - variation
public void test080() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"import java.util.List;\n" +
				"interface B {\n" +
				"  void m() throws Exception;\n" +
				"}\n" +
				"public class X implements B {\n" +
				"	public void m() throws IOException {\n" +
				"	}\n" +
				"}\n",//-----------------------------------------------------------------------
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	public void m() throws IOException {\n" +
			"	                       ^^^^^^^^^^^\n" +
			"IOException cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758
public void test081() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(	CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	runner.customOptions.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.ENABLED);
	runner.testFiles =
			new String[] {
				"com/ost/util/report/Matrix.java", // =================
				"package com.ost.util.report;\n" +
				"import java.io.Serializable;\n" +
				"import com.ost.util.report.exceptions.InvalidRowSizeException;\n" +
				"public class Matrix<T> implements Serializable {\n" +
				"	/**\n" +
				"	 * @see exceptions.InvalidRowSizeException2\n" +
				"	 */\n" +
				"	public synchronized final void addRow(Object[] row){\n" +
				"			throw new InvalidRowSizeException();\n" +
				"	}\n" +
				"}\n",
				"com/ost/util/report/FilterConstraintSpecification.java", // =================
				"package com.ost.util.report;\n" +
				"import java.io.Serializable;\n" +
				"import com.ost.util.report.exceptions.MalformedFilterConstraintSpecification;\n" +
				"public final class FilterConstraintSpecification implements Serializable, Cloneable {\n" +
				"	private final void makeConstraint(){\n" +
				"		throw new MalformedFilterConstraintSpecification();\n" +
				"	}\n" +
				"}\n",
				"com/ost/util/report/exceptions/MalformedFilterConstraintSpecification.java", // =================
				"package com.ost.util.report.exceptions;\n" +
				"public class MalformedFilterConstraintSpecification extends RuntimeException {\n" +
				"	/** Creates a new instance of MalformedFilterConstraintSpecification */\n" +
				"	public MalformedFilterConstraintSpecification() {\n" +
				"		super();\n" +
				"	}\n" +
				"	/* Creates a new instance of MalformedFilterConstraintSpecification */\n" +
				"	public MalformedFilterConstraintSpecification(String message) {\n" +
				"		super(message);\n" +
				"	}\n" +
				"}\n",
				"com/ost/util/report/exceptions/InvalidRowSizeException.java", // =================
				"package com.ost.util.report.exceptions;\n" +
				"public class InvalidRowSizeException extends RuntimeException {\n" +
				"	/** Creates a new instance of InvalidRowSizeException */\n" +
				"	public InvalidRowSizeException() {\n" +
				"		super();\n" +
				"	}\n" +
				"	/* Creates a new instance of InvalidRowSizeException */\n" +
				"	public InvalidRowSizeException(String message) {\n" +
				"		super(message);\n" +
				"	}\n" +
				"}\n"
			};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. WARNING in com\\ost\\util\\report\\Matrix.java (at line 4)\n" +
			"	public class Matrix<T> implements Serializable {\n" +
			"	             ^^^^^^\n" +
			"The serializable class Matrix does not declare a static final serialVersionUID field of type long\n" +
			"----------\n" +
			"2. ERROR in com\\ost\\util\\report\\Matrix.java (at line 6)\n" +
			"	* @see exceptions.InvalidRowSizeException2\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: exceptions cannot be resolved to a type\n" +
			"----------\n" +
			"----------\n" +
			"1. WARNING in com\\ost\\util\\report\\FilterConstraintSpecification.java (at line 4)\n" +
			"	public final class FilterConstraintSpecification implements Serializable, Cloneable {\n" +
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The serializable class FilterConstraintSpecification does not declare a static final serialVersionUID field of type long\n" +
			"----------\n" +
			"2. WARNING in com\\ost\\util\\report\\FilterConstraintSpecification.java (at line 5)\n" +
			"	private final void makeConstraint(){\n" +
			"	                   ^^^^^^^^^^^^^^^^\n" +
			"The method makeConstraint() from the type FilterConstraintSpecification is never used locally\n" +
			"----------\n" +
			"----------\n" +
			"1. WARNING in com\\ost\\util\\report\\exceptions\\MalformedFilterConstraintSpecification.java (at line 2)\n" +
			"	public class MalformedFilterConstraintSpecification extends RuntimeException {\n" +
			"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The serializable class MalformedFilterConstraintSpecification does not declare a static final serialVersionUID field of type long\n" +
			"----------\n" +
			"----------\n" +
			"1. WARNING in com\\ost\\util\\report\\exceptions\\InvalidRowSizeException.java (at line 2)\n" +
			"	public class InvalidRowSizeException extends RuntimeException {\n" +
			"	             ^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The serializable class InvalidRowSizeException does not declare a static final serialVersionUID field of type long\n" +
			"----------\n";
	runner.javacTestOptions =
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758 - variation
public void test082() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runConformTest(
			new String[] {
				"com/ost/util/report/Matrix.java", // =================
				"package com.ost.util.report;\n" +
				"import java.io.Serializable;\n" +
				"import com.ost.util.report.exceptions.InvalidRowSizeException;\n" +
				"public class Matrix<T> implements Serializable {\n" +
				"	/**\n" +
				"	 * @see exceptions.InvalidRowSizeException2\n" +
				"	 */\n" +
				"	public synchronized final void addRow(Object[] row){\n" +
				"			throw new InvalidRowSizeException();\n" +
				"	}\n" +
				"}\n",
				"com/ost/util/report/FilterConstraintSpecification.java", // =================
				"package com.ost.util.report;\n" +
				"import java.io.Serializable;\n" +
				"import com.ost.util.report.exceptions.MalformedFilterConstraintSpecification;\n" +
				"public final class FilterConstraintSpecification implements Serializable, Cloneable {\n" +
				"	private final void makeConstraint(){\n" +
				"		throw new MalformedFilterConstraintSpecification();\n" +
				"	}\n" +
				"}\n",
				"com/ost/util/report/exceptions/MalformedFilterConstraintSpecification.java", // =================
				"package com.ost.util.report.exceptions;\n" +
				"public class MalformedFilterConstraintSpecification extends RuntimeException {\n" +
				"	/** Creates a new instance of MalformedFilterConstraintSpecification */\n" +
				"	public MalformedFilterConstraintSpecification() {\n" +
				"		super();\n" +
				"	}\n" +
				"	/* Creates a new instance of MalformedFilterConstraintSpecification */\n" +
				"	public MalformedFilterConstraintSpecification(String message) {\n" +
				"		super(message);\n" +
				"	}\n" +
				"}\n",
				"com/ost/util/report/exceptions/InvalidRowSizeException.java", // =================
				"package com.ost.util.report.exceptions;\n" +
				"public class InvalidRowSizeException extends RuntimeException {\n" +
				"	/** Creates a new instance of InvalidRowSizeException */\n" +
				"	public InvalidRowSizeException() {\n" +
				"		super();\n" +
				"	}\n" +
				"	/* Creates a new instance of InvalidRowSizeException */\n" +
				"	public InvalidRowSizeException(String message) {\n" +
				"		super(message);\n" +
				"	}\n" +
				"}\n"
			},
			"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758 - variation
public void test083() {
	this.runConformTest(
			new String[] {
				"foo/X.java", // =================
				"package foo;\n" +
				"import foo.exceptions.*;\n" +
				"public class X {\n" +
				"  class exceptions {}\n" +
				"  exceptions E;\n" +
				"}\n",
				"foo/exceptions/Z.java", // =================
				"package foo.exceptions;\n" +
				"public class Z {\n" +
				"}\n"
			},
			"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758 - variation
public void test084() {
	this.runNegativeTest(
			new String[] {
				"foo/X.java", // =================
				"package foo;\n" +
				"import foo.exceptions.*;\n" +
				"public class X {\n" +
				"  exceptions E;\n" +
				"}\n" +
				"class exceptions {}\n",
				"foo/exceptions/Z.java", // =================
				"package foo.exceptions;\n" +
				"public class Z {\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in foo\\X.java (at line 2)\n" +
			"	import foo.exceptions.*;\n" +
			"	       ^^^^^^^^^^^^^^\n" +
			"The import foo.exceptions is never used\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in foo\\exceptions\\Z.java (at line 1)\n" +
			"	package foo.exceptions;\n" +
			"	        ^^^^^^^^^^^^^^\n" +
			"The package foo.exceptions collides with a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758 - variation
public void test085() {
	this.runNegativeTest(
			new String[] {
				"p/X.java", // =================
				"package p;\n" +
				"public class X extends zork.Z {\n" +
				"}\n",
				"p/Y.java", // =================
				"package p;\n" +
				"import p.zork.Z;\n" +
				"public class Y {\n" +
				"}\n",
				"p/zork/Z.java", // =================
				"package p.zork;\n" +
				"public class Z {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in p\\X.java (at line 2)\n" +
			"	public class X extends zork.Z {\n" +
			"	                       ^^^^\n" +
			"zork cannot be resolved to a type\n" +
			"----------\n" +
			"----------\n" +
			"1. WARNING in p\\Y.java (at line 2)\n" +
			"	import p.zork.Z;\n" +
			"	       ^^^^^^^^\n" +
			"The import p.zork.Z is never used\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758 - variation
public void test086() {
	this.runNegativeTest(
			new String[] {
				"p/X.java", // =================
				"package p;\n" +
				"public class X extends zork.Z {\n" +
				"}\n",
				"p/Y.java", // =================
				"package p;\n" +
				"import p.zork.*;\n" +
				"public class Y {\n" +
				"}\n",
				"p/zork/Z.java", // =================
				"package p.zork;\n" +
				"public class Z {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in p\\X.java (at line 2)\n" +
			"	public class X extends zork.Z {\n" +
			"	                       ^^^^\n" +
			"zork cannot be resolved to a type\n" +
			"----------\n" +
			"----------\n" +
			"1. WARNING in p\\Y.java (at line 2)\n" +
			"	import p.zork.*;\n" +
			"	       ^^^^^^\n" +
			"The import p.zork is never used\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758 - variation
public void test087() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
			new String[] {
				"p/X.java", // =================
				"package p;\n" +
				"public class X extends zork.Z {\n" +
				"}\n",
				"p/Y.java", // =================
				"package p;\n" +
				"import static p.zork.Z.M;\n" +
				"public class Y {\n" +
				"}\n",
				"p/zork/Z.java", // =================
				"package p.zork;\n" +
				"public class Z {\n" +
				"	public static class M {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in p\\X.java (at line 2)\n" +
			"	public class X extends zork.Z {\n" +
			"	                       ^^^^\n" +
			"zork cannot be resolved to a type\n" +
			"----------\n" +
			"----------\n" +
			"1. WARNING in p\\Y.java (at line 2)\n" +
			"	import static p.zork.Z.M;\n" +
			"	              ^^^^^^^^^^\n" +
			"The import p.zork.Z.M is never used\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758 - variation
public void test088() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
			new String[] {
				"p/X.java", // =================
				"package p;\n" +
				"public class X extends zork.Z {\n" +
				"}\n",
				"p/Y.java", // =================
				"package p;\n" +
				"import static p.zork.Z.*;\n" +
				"public class Y {\n" +
				"}\n",
				"p/zork/Z.java", // =================
				"package p.zork;\n" +
				"public class Z {\n" +
				"	static class M {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in p\\X.java (at line 2)\n" +
			"	public class X extends zork.Z {\n" +
			"	                       ^^^^\n" +
			"zork cannot be resolved to a type\n" +
			"----------\n" +
			"----------\n" +
			"1. WARNING in p\\Y.java (at line 2)\n" +
			"	import static p.zork.Z.*;\n" +
			"	              ^^^^^^^^\n" +
			"The import p.zork.Z is never used\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=245304
public void test089() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	this.runConformTest(
			new String[] {
					"com/foo/bar/baz/reporting/dom/ReportExceptionBase.java", // ================
					"package com.foo.bar.baz.reporting.dom;\n" +
					"public class ReportExceptionBase extends Exception  {\n" +
					"}\n",
					"com/foo/bar/baz/reporting/Report.java", // ================
					"package com.foo.bar.baz.reporting;\n" +
					"import com.foo.bar.baz.reporting.dom.ReportExceptionBase;\n" +
					"/**\n" +
					" * {@link dom.ReportDefs.ReportType.foo foo}\n" +
					" */\n" +
					"public abstract class Report {\n" +
					"}\n",
					"com/foo/bar/baz/reporting/Derived.java", // ================
					"package com.foo.bar.baz.reporting;\n" +
					"import com.foo.bar.baz.reporting.dom.ReportExceptionBase;\n" +
					"public class Derived {\n" +
					"  public Derived() throws ReportExceptionBase {\n" +
					"    throw new ReportExceptionBase();\n" +
					"  }\n" +
					"}\n",
			},
			"",
			null,
			true,
			null,
			options,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=247666
public void test090() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
			new String[] {
				"X.java", // =================
				"public class X {\n" +
				"<U,V extends Runnable> void foo(Zork z) {}	\n" +
				"	void bar() {\n" +
				"		foo(null);\n" +
				"	} \n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	<U,V extends Runnable> void foo(Zork z) {}	\n" +
			"	                                ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	foo(null);\n" +
			"	^^^\n" +
			"The method foo(Zork) from the type X refers to the missing type Zork\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=252288
public void test091()  throws Exception {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
		new String[] {
			"TypeUtils.java",
			"import java.util.Collection;\n" +
			"import java.util.Iterator;\n" +
			"\n" +
			"public final class TypeUtils {\n" +
			"\n" +
			"	// personal\n" +
			"\n" +
			"	private TypeUtils() {\n" +
			"	}\n" +
			"\n" +
			"	// class\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns true if a target type is exactly any one in a group of types.\n" +
			"	 * @param target Target type. Never null.\n" +
			"	 * @param types Group of types. If empty, returns false. Never null.\n" +
			"	 * @return True if the target is a valid type.\n" +
			"	 */\n" +
			"	public static boolean isIdenticalToAny(Class<?> target, Collection<Class<?>> types) {\n" +
			"		if (target == null) throw new IllegalArgumentException(\n" +
			"		\"Target is null.\");\n" +
			"\n" +
			"		if (types.contains(target)) return true;\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns true if a target type is the same or a subtype of (assignable to)\n" +
			"	 * a reference type. Convenience method for completeness. Forwards to\n" +
			"	 * Class.isAssignableFrom().\n" +
			"	 * @param target Target type. Never null.\n" +
			"	 * @param type Reference type. Never null.\n" +
			"	 * @return True if condition is met.\n" +
			"	 */\n" +
			"	public static boolean isAssignableTo(Class<?> target, Class<?> type) {\n" +
			"		return type.isAssignableFrom(target);\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns true if a target type is the same or a subtype of (assignable to)\n" +
			"	 * any one reference type.\n" +
			"	 * @param target Target type. Never null.\n" +
			"	 * @param types Reference types (Class). Never null. If empty returns false.\n" +
			"	 * @return True if condition is met.\n" +
			"	 */\n" +
			"	public static boolean isAssignableToAny(Class<?> target,\n" +
			"	Collection<Class<?>> types) {\n" +
			"		if (types.isEmpty()) return false;\n" +
			"\n" +
			"		for(Class<?> type : types) {\n" +
			"			if (type.isAssignableFrom(target)) return true;\n" +
			"		}\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns true if any one target type is the same or a subtype of\n" +
			"	 * (assignable to) a reference type.\n" +
			"	 * @param targets Target types (Class). Never null. If empty returns false.\n" +
			"	 * @param type Reference type. Never null.\n" +
			"	 * @return True if condition is met.\n" +
			"	 */\n" +
			"	public static boolean areAnyAssignableTo(Collection<Class<?>> targets,\n" +
			"	Class<?> type) {\n" +
			"		if (targets.isEmpty()) return false;\n" +
			"\n" +
			"		for(Class<?> target : targets) {\n" +
			"			if (type.isAssignableFrom(target)) return true;\n" +
			"		}\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns true if any one target type is the same or a subtype of\n" +
			"	 * (assignable to) any one reference type.\n" +
			"	 * @param targets Target types (Class). Never null. If empty returns false.\n" +
			"	 * @param types Reference types (Class). Never null. If empty returns false.\n" +
			"	 * @return True if condition is met.\n" +
			"	 */\n" +
			"	public static boolean areAnyAssignableToAny(Collection<Class<?>> targets,\n" +
			"	Collection<Class<?>> types) {\n" +
			"		if (targets.isEmpty()) return false;\n" +
			"		if (types.isEmpty()) return false;\n" +
			"\n" +
			"		for(Class<?> target : targets) {\n" +
			"			if (isAssignableToAny(target, types)) return true;\n" +
			"		}\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns true if a target object\'s type is the same or a subtype of\n" +
			"	 * (assignable to) a reference type. Convenience method for completeness.\n" +
			"	 * Forwards to Class.isInstance().\n" +
			"	 * @param target Target object. Never null.\n" +
			"	 * @param type Reference type. Never null.\n" +
			"	 * @return True if condition is met.\n" +
			"	 */\n" +
			"	public static boolean isInstanceOf(Object target, Class<?> type) {\n" +
			"		return type.isInstance(target);\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns true if a target object\'s type is the same or a subtype of\n" +
			"	 * (assignable to) any one type.\n" +
			"	 * @param target Target object. Never null.\n" +
			"	 * @param types Reference types. Never null. If empty returns false.\n" +
			"	 * @return True if condition is met.\n" +
			"	 */\n" +
			"	public static boolean isInstanceOfAny(Object target,\n" +
			"	Collection<Class<?>> types) {\n" +
			"		if (types.isEmpty()) return false;\n" +
			"\n" +
			"		for (Class<?> type : types) {\n" +
			"			if (type.isInstance(target)) return true;\n" +
			"		}\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns true if any one target object\'s type is the same or a subtype of\n" +
			"	 * (assignable to) a reference type.\n" +
			"	 * @param targets Target objects. Never null. If empty returns false.\n" +
			"	 * @param type Reference type. Never null.\n" +
			"	 * @return True if condition is met.\n" +
			"	 */\n" +
			"	public static boolean areAnyInstanceOf(Collection<Object> targets,\n" +
			"	Class<?> type) {\n" +
			"		if (targets.isEmpty()) return false;\n" +
			"\n" +
			"		for(Object target : targets) {\n" +
			"			if (type.isInstance(target)) return true;\n" +
			"		}\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns true if all target object types are the same or a subtype of\n" +
			"	 * (assignable to) a reference type.\n" +
			"	 * @param targets Target objects. Never null. If empty returns\n" +
			"	 * false.\n" +
			"	 * @param type Reference type. Never null.\n" +
			"	 * @return True if condition is met.\n" +
			"	 */\n" +
			"	public static boolean areAllInstanceOf(Collection<Object> targets,\n" +
			"	Class<?> type) {\n" +
			"		if (targets.isEmpty()) return false;\n" +
			"\n" +
			"		for(Object target : targets) {\n" +
			"			if (!type.isInstance(target)) return false;\n" +
			"		}\n" +
			"		return true;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns true if no target object types are the same or a subtype of\n" +
			"	 * (assignable to) a reference type.\n" +
			"	 * @param targets Target objects. Never null. If empty returns\n" +
			"	 * false.\n" +
			"	 * @param type Reference type. Never null.\n" +
			"	 * @return True if condition is met.\n" +
			"	 */\n" +
			"	public static boolean areNoneInstanceOf(Collection<Object> targets,\n" +
			"	Class<?> type) {\n" +
			"		if (targets.isEmpty()) return false;\n" +
			"\n" +
			"		for(Object target : targets) {\n" +
			"			if (type.isInstance(target)) return false;\n" +
			"		}\n" +
			"		return true;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns true if any one target object\'s type is the same or a subtype of\n" +
			"	 * (assignable to) any one reference type.\n" +
			"	 * @param targets Target objects. Never null. If empty returns\n" +
			"	 * false.\n" +
			"	 * @param types Reference types. Never null. If empty returns false.\n" +
			"	 * @return True if condition is met.\n" +
			"	 */\n" +
			"	public static boolean areAnyInstanceOfAny(Collection<Object> targets,\n" +
			"	Collection<Class<?>> types) {\n" +
			"		if (targets.isEmpty()) return false;\n" +
			"		if (types.isEmpty()) return false;\n" +
			"\n" +
			"		for(Object target : targets) {\n" +
			"			if (isInstanceOfAny(target, types)) return true;\n" +
			"		}\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns only those target objects whose type is identical to the included\n" +
			"	 * reference type.\n" +
			"	 * @param targets Group of target objects. If empty, returns empty. Never\n" +
			"	 * null.\n" +
			"	 * @param type Included reference type. Never null.\n" +
			"	 * @param retVal Return value object. The collection of valid target\n" +
			"	 * objects. Can be {@code targets}. Never null.\n" +
			"	 * @return Reference to retVal. Never null.\n" +
			"	 */\n" +
			"	public static Collection<Object> includeIdenticalTo(Collection<Object> targets,\n" +
			"	Class<?> type, Collection<Object> retVal) {\n" +
			"		// save targets in retVal\n" +
			"		if (targets != retVal) {\n" +
			"			retVal.clear();\n" +
			"			retVal.addAll(targets);\n" +
			"		}\n" +
			"\n" +
			"		// remove unwanted targets, by target\n" +
			"		Iterator<?> objectI = retVal.iterator();\n" +
			"		while (objectI.hasNext()) {\n" +
			"			Object object = objectI.next();\n" +
			"			if (!type.equals(object.getClass())) objectI.remove();\n" +
			"		}\n" +
			"\n" +
			"		return retVal;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns only those target objects whose type is exactly one of the\n" +
			"	 * included reference types.\n" +
			"	 * @param targets Group of target objects. If empty, returns empty. Never\n" +
			"	 * null.\n" +
			"	 * @param types Group of included reference types. If empty, returns empty.\n" +
			"	 * If null, all types are included (all targets are returned).\n" +
			"	 * @param retVal Return value object. The collection of valid target\n" +
			"	 * objects. Can be {@code targets}. Never null.\n" +
			"	 * @return Reference to retVal. Never null.\n" +
			"	 */\n" +
			"	public static Collection<Object> includeIdenticalToAny(\n" +
			"	Collection<Object> targets,\n" +
			"	Collection<Class<?>> types, Collection<Object> retVal) {\n" +
			"		// save targets in retVal\n" +
			"		if (targets != retVal) {\n" +
			"			retVal.clear();\n" +
			"			retVal.addAll(targets);\n" +
			"		}\n" +
			"\n" +
			"		if (types == null) return retVal;\n" +
			"\n" +
			"		// remove unwanted targets, by target\n" +
			"		Iterator<Object> objectI = retVal.iterator();\n" +
			"		while (objectI.hasNext()) {\n" +
			"			Object object = objectI.next();\n" +
			"			if (!isIdenticalToAny(object.getClass(), types)) objectI.remove();\n" +
			"		}\n" +
			"\n" +
			"		return retVal;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns only those target objects whose type is NOT identical to the\n" +
			"	 * excluded reference type.\n" +
			"	 * @param targets Group of target objects. If empty, returns empty. Never\n" +
			"	 * null.\n" +
			"	 * @param type The excluded reference type. Never null.\n" +
			"	 * @param retVal Return value object. The collection of valid target\n" +
			"	 * objects. Can be {@code targets}. Never null.\n" +
			"	 * @return Reference to retVal. Never null.\n" +
			"	 */\n" +
			"	public static Collection<Object> excludeIdenticalTo(\n" +
			"	Collection<Object> targets, Class<?> type,\n" +
			"	Collection<Object> retVal) {\n" +
			"		// save targets in retVal\n" +
			"		if (targets != retVal) {\n" +
			"			retVal.clear();\n" +
			"			retVal.addAll(targets);\n" +
			"		}\n" +
			"\n" +
			"		// remove unwanted targets, by target\n" +
			"		Iterator<Object> objectI = retVal.iterator();\n" +
			"		while (objectI.hasNext()) {\n" +
			"			Object object = objectI.next();\n" +
			"			if (type.equals(object.getClass())) objectI.remove();\n" +
			"		}\n" +
			"\n" +
			"		return retVal;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns only those target objects whose type is NOT exactly one of the\n" +
			"	 * excluded reference types.\n" +
			"	 * @param targets Group of target objects. If empty, returns empty. Never\n" +
			"	 * null.\n" +
			"	 * @param types Group of excluded reference types. If empty, returns empty.\n" +
			"	 * If null, no types are excluded (all targets are returned).\n" +
			"	 * @param retVal Return value object. The collection of valid target\n" +
			"	 * objects. Can be targets. Never null.\n" +
			"	 * @return Reference to retVal. Never null.\n" +
			"	 */\n" +
			"	public static Collection<Object> excludeIdenticalToAny(\n" +
			"	Collection<Object> targets, Collection<Class<?>> types,\n" +
			"	Collection<Object> retVal) {\n" +
			"		// save targets in retVal\n" +
			"		if (targets != retVal) {\n" +
			"			retVal.clear();\n" +
			"			retVal.addAll(targets);\n" +
			"		}\n" +
			"\n" +
			"		if (types == null) return retVal;\n" +
			"\n" +
			"		// remove unwanted targets, by target\n" +
			"		Iterator<Object> objectI = retVal.iterator();\n" +
			"		while (objectI.hasNext()) {\n" +
			"			Object object = objectI.next();\n" +
			"			if (isIdenticalToAny(object.getClass(), types)) objectI.remove();\n" +
			"		}\n" +
			"\n" +
			"		return retVal;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns only those target objects whose type is assignable to (an\n" +
			"	 * instance of) the included reference type.\n" +
			"	 * @param targets Group of target objects. If empty, returns empty. Never\n" +
			"	 * null.\n" +
			"	 * @param type Included reference type. Never null.\n" +
			"	 * @param retVal Return value object. The collection of valid target objects\n" +
			"	 * (Object). Can be targets. Never null.\n" +
			"	 * @return Reference to retVal. Never null.\n" +
			"	 */\n" +
			"	public static Collection<Object> includeAssignableTo(\n" +
			"	Collection<Object> targets, Class<?> type, Collection<Object> retVal) {\n" +
			"		// save targets in retVal\n" +
			"		if (targets != retVal) {\n" +
			"			retVal.clear();\n" +
			"			retVal.addAll(targets);\n" +
			"		}\n" +
			"\n" +
			"		// remove unwanted targets, by target\n" +
			"		Iterator<Object> objectI = retVal.iterator();\n" +
			"		while (objectI.hasNext()) {\n" +
			"			Object object = objectI.next();\n" +
			"			if (!type.isInstance(object)) objectI.remove();\n" +
			"		}\n" +
			"\n" +
			"		return retVal;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns only those target objects whose type is assignable to (an\n" +
			"	 * instance of) any one of the included reference types.\n" +
			"	 * @param targets Group of target objects. If empty, returns empty. Never\n" +
			"	 * null.\n" +
			"	 * @param types Group of included reference types. If empty, returns empty.\n" +
			"	 * If null, all types are included (all targets are returned).\n" +
			"	 * @param retVal Return value object. The collection of valid target\n" +
			"	 * objects. Can be targets. Never null.\n" +
			"	 * @return Reference to retVal. Never null.\n" +
			"	 */\n" +
			"	public static Collection<Object> includeAssignableToAny(\n" +
			"	Collection<Object> targets, Collection<Class<?>> types,\n" +
			"	Collection<Object> retVal) {\n" +
			"		// save targets in retVal\n" +
			"		if (targets != retVal) {\n" +
			"			retVal.clear();\n" +
			"			retVal.addAll(targets);\n" +
			"		}\n" +
			"\n" +
			"		if (types == null) return retVal;\n" +
			"\n" +
			"		// remove unwanted targets, by target\n" +
			"		Iterator<Object> objectI = retVal.iterator();\n" +
			"		while (objectI.hasNext()) {\n" +
			"			Object object = objectI.next();\n" +
			"			if (!isInstanceOfAny(object, types)) objectI.remove();\n" +
			"		}\n" +
			"\n" +
			"		return retVal;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns only those target objects whose type is NOT assignable to (an\n" +
			"	 * instance of) the excluded reference type.\n" +
			"	 * @param targets Group of target objects. If empty, returns empty. Never\n" +
			"	 * null.\n" +
			"	 * @param type The excluded reference type. Never null.\n" +
			"	 * @param retVal Return value object. The collection of valid target\n" +
			"	 * objects. Never null.\n" +
			"	 * @return Reference to retVal. Never null.\n" +
			"	 */\n" +
			"	public static Collection<Object> excludeAssignableTo(\n" +
			"	Collection<Object> targets, Class<?> type, Collection<Object> retVal) {\n" +
			"		// save targets in retVal\n" +
			"		if (targets != retVal) {\n" +
			"			retVal.clear();\n" +
			"			retVal.addAll(targets);\n" +
			"		}\n" +
			"\n" +
			"		// remove unwanted targets, by target\n" +
			"		Iterator<Object> objectI = retVal.iterator();\n" +
			"		while (objectI.hasNext()) {\n" +
			"			Object object = objectI.next();\n" +
			"			if (type.isInstance(object)) objectI.remove();\n" +
			"		}\n" +
			"\n" +
			"		return retVal;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns only those target objects whose type is NOT assignable to (an\n" +
			"	 * instance of) any one of the excluded reference types.\n" +
			"	 * @param targets Group of target objects. If empty, returns empty. Never\n" +
			"	 * null.\n" +
			"	 * @param types Group of excluded reference types. If empty, returns empty.\n" +
			"	 * If null, no types are excluded (all targets are returned).\n" +
			"	 * @param retVal Return value object. The collection of valid target\n" +
			"	 * objects. Never null.\n" +
			"	 * @return Reference to retVal. Never null.\n" +
			"	 */\n" +
			"	public static Collection<Object> excludeAssignableToAny(\n" +
			"	Collection<Object> targets, Collection<Class<?>> types,\n" +
			"	Collection<Object> retVal) {\n" +
			"		// save targets in retVal\n" +
			"		if (targets != retVal) {\n" +
			"			retVal.clear();\n" +
			"			retVal.addAll(targets);\n" +
			"		}\n" +
			"\n" +
			"		if (types == null) return retVal;\n" +
			"\n" +
			"		// remove unwanted targets, by target\n" +
			"		Iterator<Object> objectI = retVal.iterator();\n" +
			"		while (objectI.hasNext()) {\n" +
			"			Object object = objectI.next();\n" +
			"			if (isInstanceOfAny(object, types)) objectI.remove();\n" +
			"		}\n" +
			"\n" +
			"		return retVal;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns the first target object whose type is assignable to (an instance\n" +
			"	 * of) the reference type.\n" +
			"	 * @param targets Group of target objects. If empty, returns null.\n" +
			"	 * Never null.\n" +
			"	 * @param type Reference type. Never null.\n" +
			"	 * @return The result (Object, assignable instance of type). Null if none.\n" +
			"	 */\n" +
			"	public static <T extends Class<?>> T getFirstAssignableTo(\n" +
			"	Collection<Object> targets, T type) {\n" +
			"		for(Object target : targets) {\n" +
			"			if (type.isInstance(target)) return target;\n" +
			"		}\n" +
			"\n" +
			"		return null;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns the first target object whose type is exactly the specified type.\n" +
			"	 * @param targets Group of target objects (Object). If empty, returns null.\n" +
			"	 * Never null.\n" +
			"	 * @param type The type. Never null. objects (Object). Can be targets. Never\n" +
			"	 * null.\n" +
			"	 * @return The result (Object, exact instance of type). Null if none.\n" +
			"	 */\n" +
			"	public static Object getFirstIdenticalTo(Collection targets, Class type) {\n" +
			"		Iterator targetI = targets.iterator();\n" +
			"		while (targetI.hasNext()) {\n" +
			"			Object target = targetI.next();\n" +
			"			if (type.equals(target.getClass())) return target;\n" +
			"		}\n" +
			"\n" +
			"		return null;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Gets a target object T from a source object S in a group of objects, and\n" +
			"	 * returns the target objects in result group R. A group object is ignored\n" +
			"	 * if it is not a source type or, if it is a source type, its target object\n" +
			"	 * is not a target type.\n" +
			"	 * @param group Temp input group of shared exposed objects. If null, returns\n" +
			"	 * empty.\n" +
			"	 * @param sourceType Desired source object type. Never null.\n" +
			"	 * @param getter Gets a target object from a source object. Never null.\n" +
			"	 * @param targetType Desired target object type. Never null.\n" +
			"	 * @param retVal Temp output group of shared exposed target objects. Never\n" +
			"	 * null.\n" +
			"	 * @return Reference to retVal. Never null.\n" +
			"	 */\n" +
			"	public static <S,T,TT extends T,R extends Collection<? super TT>> R getAll(\n" +
			"	Collection<?> group, Class<? extends S> sourceType, Getter<S,T> getter,\n" +
			"	Class<TT> targetType, R retVal) {\n" +
			"		if (sourceType == null) throw new IllegalArgumentException(\n" +
			"		\"Source type is null.\");\n" +
			"		if (getter == null) throw new IllegalArgumentException(\n" +
			"		\"Getter is null.\");\n" +
			"		if (targetType == null) throw new IllegalArgumentException(\n" +
			"		\"Target type is null.\");\n" +
			"		if (retVal == null) throw new IllegalArgumentException(\n" +
			"		\"Return value is null.\");\n" +
			"		retVal.clear();\n" +
			"\n" +
			"		if (group == null) return retVal;\n" +
			"\n" +
			"		for (Object obj : group) {\n" +
			"			if (!sourceType.isInstance(obj)) continue; // ignore\n" +
			"			S source = (S) obj;\n" +
			"			T target = getter.getFrom(source);\n" +
			"			if (!targetType.isInstance(target)) continue; // ignore\n" +
			"			retVal.add((TT) target);\n" +
			"		}\n" +
			"\n" +
			"		return retVal;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Similar to getAll(Collection, Class, Getter, Class, Collection), but all\n" +
			"	 * target objects are returned, regardless of type, including nulls.\n" +
			"	 * @param group Temp input group of shared exposed objects. If null, returns\n" +
			"	 * empty.\n" +
			"	 * @param sourceType Desired source object type. Never null.\n" +
			"	 * @param getter Gets a target object from a source object. Never null.\n" +
			"	 * @param retVal Temp output group of shared exposed target objects. Never\n" +
			"	 * null.\n" +
			"	 * @return Reference to retVal. Never null.\n" +
			"	 */\n" +
			"	public static <S,T,R extends Collection<? super T>> R getAll(\n" +
			"	Collection<?> group, Class<? extends S> sourceType, Getter<S,T> getter,\n" +
			"	R retVal) {\n" +
			"		if (sourceType == null) throw new IllegalArgumentException(\n" +
			"		\"Source type is null.\");\n" +
			"		if (getter == null) throw new IllegalArgumentException(\n" +
			"		\"Getter is null.\");\n" +
			"		if (retVal == null) throw new IllegalArgumentException(\n" +
			"		\"Return value is null.\");\n" +
			"		retVal.clear();\n" +
			"\n" +
			"		if (group == null) return retVal;\n" +
			"\n" +
			"		for (Object obj : group) {\n" +
			"			if (!sourceType.isInstance(obj)) continue; // ignore\n" +
			"			S source = (S) obj;\n" +
			"			T target = getter.getFrom(source);\n" +
			"			retVal.add(target);\n" +
			"		}\n" +
			"\n" +
			"		return retVal;\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in TypeUtils.java (at line 441)\n" +
		"	public static <T extends Class<?>> T getFirstAssignableTo(\n" +
		"	                         ^^^^^\n" +
		"The type parameter T should not be bounded by the final type Class<?>. Final types cannot be further extended\n" +
		"----------\n" +
		"2. ERROR in TypeUtils.java (at line 444)\n" +
		"	if (type.isInstance(target)) return target;\n" +
		"	                                    ^^^^^^\n" +
		"Type mismatch: cannot convert from Object to T\n" +
		"----------\n" +
		"3. WARNING in TypeUtils.java (at line 458)\n" +
		"	public static Object getFirstIdenticalTo(Collection targets, Class type) {\n" +
		"	                                         ^^^^^^^^^^\n" +
		"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" +
		"----------\n" +
		"4. WARNING in TypeUtils.java (at line 458)\n" +
		"	public static Object getFirstIdenticalTo(Collection targets, Class type) {\n" +
		"	                                                             ^^^^^\n" +
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
		"----------\n" +
		"5. WARNING in TypeUtils.java (at line 459)\n" +
		"	Iterator targetI = targets.iterator();\n" +
		"	^^^^^^^^\n" +
		"Iterator is a raw type. References to generic type Iterator<E> should be parameterized\n" +
		"----------\n" +
		"6. ERROR in TypeUtils.java (at line 483)\n" +
		"	Collection<?> group, Class<? extends S> sourceType, Getter<S,T> getter,\n" +
		"	                                                    ^^^^^^\n" +
		"Getter cannot be resolved to a type\n" +
		"----------\n" +
		"7. WARNING in TypeUtils.java (at line 499)\n" +
		"	S source = (S) obj;\n" +
		"	           ^^^^^^^\n" +
		"Type safety: Unchecked cast from Object to S\n" +
		"----------\n" +
		"8. WARNING in TypeUtils.java (at line 502)\n" +
		"	retVal.add((TT) target);\n" +
		"	           ^^^^^^^^^^^\n" +
		"Type safety: Unchecked cast from T to TT\n" +
		"----------\n" +
		"9. ERROR in TypeUtils.java (at line 520)\n" +
		"	Collection<?> group, Class<? extends S> sourceType, Getter<S,T> getter,\n" +
		"	                                                    ^^^^^^\n" +
		"Getter cannot be resolved to a type\n" +
		"----------\n" +
		"10. WARNING in TypeUtils.java (at line 534)\n" +
		"	S source = (S) obj;\n" +
		"	           ^^^^^^^\n" +
		"Type safety: Unchecked cast from Object to S\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250297
public void test092() {
	this.runNegativeTest(
		new String[] {
			"p1/p2/X.java", // =================
			"package p1.p2;\n" +
			"public class X {\n" +
			"	public p2.p3.Z z() {return null;}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in p1\\p2\\X.java (at line 3)\n" +
		"	public p2.p3.Z z() {return null;}\n" +
		"	       ^^\n" +
		"p2 cannot be resolved to a type\n" +
		"----------\n",
		null,
		false,
		null,
		true,
		false,
		false
	);
	Runner runner = new Runner();
	runner.javacTestOptions = JavacTestOptions.SKIP; // javac did not produce p1/p2/X.class which is needed below
	runner.testFiles =
		new String[] {
			"a/b/A.java", // =================
			"package a.b;\n" +
			"public class A {\n" +
			"	p1.p2.X x;\n" +
			"	void test() { x.z(); }\n" +
			"	void foo(p2.p3.Z z) {}\n" +
			"}\n",
			"p2/p3/Z.java", // =================
			"package p2.p3;\n" +
			"public class Z {}\n"
		};
	runner.shouldFlushOutputDirectory = false;
	runner.runConformTest();
	runner.testFiles =
		new String[] {
			"a/b/A.java", // =================
			"package a.b;\n" +
			"public class A {\n" +
			"	p1.p2.X x;\n" +
			"	void test() { x.z(); }\n" +
			"	void foo(p2.p3.Z z) {}\n" +
			"}\n"
		};
	runner.runConformTest();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250297 - variation
public void test093() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
			new String[] {
				"X.java", // =================
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	void foo() {\n" +
				"		List<? extends Zork> zlist = null;\n" +
				"		bar(zlist.get(0));\n" +
				"	}\n" +
				"	<T> T bar(T t) { return t; }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	List<? extends Zork> zlist = null;\n" +
			"	               ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250297 - variation
public void test094() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
			new String[] {
				"X.java", // =================
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	void foo(boolean b, Runnable r) {\n" +
				"		bar(r);\n" +
				"	}\n" +
				"	<T> T bar(Zork z) { return z; }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	bar(r);\n" +
			"	^^^\n" +
			"The method bar(Zork) from the type X refers to the missing type Zork\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	<T> T bar(Zork z) { return z; }\n" +
			"	          ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250297 - variation
public void test095() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
			new String[] {
				"X.java", // =================
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	void foo(boolean b, Runnable r) {\n" +
				"		bar(r);\n" +
				"	}\n" +
				"	<T> bar(Zork z) { return z; }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	bar(r);\n" +
			"	^^^\n" +
			"The method bar(Runnable) is undefined for the type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	<T> bar(Zork z) { return z; }\n" +
			"	    ^^^^^^^^^^^\n" +
			"Return type for the method is missing\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	<T> bar(Zork z) { return z; }\n" +
			"	        ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=257384
public void test096() {
	this.runNegativeTest(
		new String[] {
			"p2/B.java", // =================
			"package p2;\n" +
			"import p1.A;\n" +
			"public abstract class B {\n" +
			"	public static A foo() {}\n" +
			"}\n",
			"p3/C.java", // =================
			"package p3;\n" +
			"import p1.A;\n" +
			"public abstract class C extends p2.B {\n" +
			"	public static A foo() {}\n" +
			"}\n",
			"p/D.java", // =================
			"package p;\n" +
			"public class D extends p3.C {}"
		},
		"----------\n" +
		"1. ERROR in p2\\B.java (at line 2)\n" +
		"	import p1.A;\n" +
		"	       ^^\n" +
		"The import p1 cannot be resolved\n" +
		"----------\n" +
		"2. ERROR in p2\\B.java (at line 4)\n" +
		"	public static A foo() {}\n" +
		"	              ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in p3\\C.java (at line 2)\n" +
		"	import p1.A;\n" +
		"	       ^^\n" +
		"The import p1 cannot be resolved\n" +
		"----------\n" +
		"2. ERROR in p3\\C.java (at line 4)\n" +
		"	public static A foo() {}\n" +
		"	              ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=258248
public void test097() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"public class X {\n" +
			"\n" +
			"	public static interface InnerInterface<TheTypeMirror, TheDeclaredType extends TheTypeMirror, TheClassType extends TheDeclaredType, TheInterfaceType extends TheDeclaredType, ThePrimitiveType extends TheTypeMirror, TheArrayType extends TheTypeMirror, TheTypeVariable extends TheTypeMirror, TheWildcardType extends TheTypeMirror, TheFieldDeclaration, TheTypeParameterDeclaration, TheTypeDeclaration, TheClassDeclaration extends TheTypeDeclaration> {\n" +
			"	}\n" +
			"	protected <TheTypeMirror, TheDeclaredType extends TheTypeMirror, TheClassType extends TheDeclaredType, TheInterfaceType extends TheDeclaredType, ThePrimitiveType extends TheTypeMirror, TheArrayType extends TheTypeMirror, TheTypeVariable extends TheTypeMirror, TheWildcardType extends TheTypeMirror, TheFieldDeclaration, TheTypeParameterDeclaration, TheTypeDeclaration, TheClassDeclaration extends TheTypeDeclaration, Env extends InnerInterface<TheTypeMirror, TheDeclaredType, TheClassType, TheInterfaceType, ThePrimitiveType, TheArrayType, TheTypeVariable, TheWildcardType, TheFieldDeclaration, TheTypeParameterDeclaration, TheTypeDeclaration, TheClassDeclaration>, ParamType extends TheTypeMirror> void testMethod(\n" +
			"			TheFieldDeclaratation fieldDeclaratation, Env\n" +
			"			environment) {\n" +
			"\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	TheFieldDeclaratation fieldDeclaratation, Env\n" +
		"	^^^^^^^^^^^^^^^^^^^^^\n" +
		"TheFieldDeclaratation cannot be resolved to a type\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
public void test098() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X {\n" +
			"    private class A {\n" +
			"    	public void foo(int a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"    	}\n" +
			"	    public void foo(float a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   	public void foo(boolean a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"      	public void foo(Integer a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   }\n" +
			"   private class B extends A {\n" +
			"		public void foo(int a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"		public void foo(float a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   	public void foo(double a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   	public void foo(char a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   }\n" +
			"}\n"
		};
	runner.expectedCompilerLog = isMinimumCompliant(ClassFileConstants.JDK11) ?
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	public void foo(int a) {\n" +
		"	            ^^^^^^^^^^\n" +
		"The method foo(int) from the type X.A is never used locally\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 6)\n" +
		"	public void foo(float a) {\n" +
		"	            ^^^^^^^^^^^^\n" +
		"The method foo(float) from the type X.A is never used locally\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 9)\n" +
		"	public void foo(boolean a) {\n" +
		"	            ^^^^^^^^^^^^^^\n" +
		"The method foo(boolean) from the type X.A is never used locally\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 12)\n" +
		"	public void foo(Integer a) {\n" +
		"	            ^^^^^^^^^^^^^^\n" +
		"The method foo(Integer) from the type X.A is never used locally\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 16)\n" +
		"	private class B extends A {\n" +
		"	              ^\n" +
		"The type X.B is never used locally\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 23)\n" +
		"	public void foo(double a) {\n" +
		"	            ^^^^^^^^^^^^^\n" +
		"The method foo(double) from the type X.B is never used locally\n" +
		"----------\n" +
		"7. WARNING in X.java (at line 26)\n" +
		"	public void foo(char a) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method foo(char) from the type X.B is never used locally\n" +
		"----------\n"
		:
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	public void foo(int a) {\n" +
		"	            ^^^^^^^^^^\n" +
		"The method foo(int) from the type X.A is never used locally\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 6)\n" +
		"	public void foo(float a) {\n" +
		"	            ^^^^^^^^^^^^\n" +
		"The method foo(float) from the type X.A is never used locally\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 9)\n" +
		"	public void foo(boolean a) {\n" +
		"	            ^^^^^^^^^^^^^^\n" +
		"The method foo(boolean) from the type X.A is never used locally\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 12)\n" +
		"	public void foo(Integer a) {\n" +
		"	            ^^^^^^^^^^^^^^\n" +
		"The method foo(Integer) from the type X.A is never used locally\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 16)\n" +
		"	private class B extends A {\n" +
		"	              ^\n" +
		"The type X.B is never used locally\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 16)\n" +
		"	private class B extends A {\n" +
		"	              ^\n" +
		"Access to enclosing constructor X.A() is emulated by a synthetic accessor method\n" +
		"----------\n" +
		"7. WARNING in X.java (at line 23)\n" +
		"	public void foo(double a) {\n" +
		"	            ^^^^^^^^^^^^^\n" +
		"The method foo(double) from the type X.B is never used locally\n" +
		"----------\n" +
		"8. WARNING in X.java (at line 26)\n" +
		"	public void foo(char a) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method foo(char) from the type X.B is never used locally\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
public void test099() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X {\n" +
			"    private class A {\n" +
			"    	public void foo(int a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"    	}\n" +
			"	    public void foo(float a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   	public void foo(boolean a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"      	public void foo(Integer a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   }\n" +
			"   private class B extends A {\n" +
			"		public void foo(int a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"		public void foo(float a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   	public void foo(double a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   	public void foo(char a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   }\n" +
			"   public class C extends B {\n" +
		    "		public void foo(int a) {\n" +
			"			System.out.println(\"Hello\");\n" +
			"		}\n" +
		    "		public void foo(double a) {\n" +
			"			System.out.println(\"Hello\");\n" +
			"		}\n" +
			"		public void foo(boolean a) {\n" +
			"			System.out.println(\"Hello\");\n" +
			"		}\n" +
			"		public void foo(byte a) {\n" +
			"			System.out.println(\"Hello\");\n" +
			"		}\n" +
		    "   }\n" +
			"}\n"
		};
	runner.expectedCompilerLog = isMinimumCompliant(ClassFileConstants.JDK11) ?
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	public void foo(int a) {\n" +
		"	            ^^^^^^^^^^\n" +
		"The method foo(int) from the type X.A is never used locally\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 6)\n" +
		"	public void foo(float a) {\n" +
		"	            ^^^^^^^^^^^^\n" +
		"The method foo(float) from the type X.A is never used locally\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 9)\n" +
		"	public void foo(boolean a) {\n" +
		"	            ^^^^^^^^^^^^^^\n" +
		"The method foo(boolean) from the type X.A is never used locally\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 23)\n" +
		"	public void foo(double a) {\n" +
		"	            ^^^^^^^^^^^^^\n" +
		"The method foo(double) from the type X.B is never used locally\n" +
		"----------\n"
		:
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	public void foo(int a) {\n" +
		"	            ^^^^^^^^^^\n" +
		"The method foo(int) from the type X.A is never used locally\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 6)\n" +
		"	public void foo(float a) {\n" +
		"	            ^^^^^^^^^^^^\n" +
		"The method foo(float) from the type X.A is never used locally\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 9)\n" +
		"	public void foo(boolean a) {\n" +
		"	            ^^^^^^^^^^^^^^\n" +
		"The method foo(boolean) from the type X.A is never used locally\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 16)\n" +
		"	private class B extends A {\n" +
		"	              ^\n" +
		"Access to enclosing constructor X.A() is emulated by a synthetic accessor method\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 23)\n" +
		"	public void foo(double a) {\n" +
		"	            ^^^^^^^^^^^^^\n" +
		"The method foo(double) from the type X.B is never used locally\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 30)\n" +
		"	public class C extends B {\n" +
		"	             ^\n" +
		"Access to enclosing constructor X.B() is emulated by a synthetic accessor method\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
// check independence of textual order
public void test099a() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X {\n" +
			"   public class C extends B {\n" +
		    "		public void foo(int a) {\n" +
			"			System.out.println(\"Hello\");\n" +
			"		}\n" +
		    "		public void foo(double a) {\n" +
			"			System.out.println(\"Hello\");\n" +
			"		}\n" +
			"		public void foo(boolean a) {\n" +
			"			System.out.println(\"Hello\");\n" +
			"		}\n" +
			"		public void foo(byte a) {\n" +
			"			System.out.println(\"Hello\");\n" +
			"		}\n" +
		    "   }\n" +
			"   private class B extends A {\n" +
			"		public void foo(int a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"		public void foo(float a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   	public void foo(double a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   	public void foo(char a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   }\n" +
			"   private class A {\n" +
			"    	public void foo(int a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"    	}\n" +
			"	    public void foo(float a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   	public void foo(boolean a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"      	public void foo(Integer a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   }\n" +
			"}\n"
		};
	runner.expectedCompilerLog = isMinimumCompliant(ClassFileConstants.JDK11) ?
		"----------\n" +
		"1. WARNING in X.java (at line 23)\n" +
		"	public void foo(double a) {\n" +
		"	            ^^^^^^^^^^^^^\n" +
		"The method foo(double) from the type X.B is never used locally\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 31)\n" +
		"	public void foo(int a) {\n" +
		"	            ^^^^^^^^^^\n" +
		"The method foo(int) from the type X.A is never used locally\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 34)\n" +
		"	public void foo(float a) {\n" +
		"	            ^^^^^^^^^^^^\n" +
		"The method foo(float) from the type X.A is never used locally\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 37)\n" +
		"	public void foo(boolean a) {\n" +
		"	            ^^^^^^^^^^^^^^\n" +
		"The method foo(boolean) from the type X.A is never used locally\n" +
		"----------\n"
		:
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public class C extends B {\n" +
		"	             ^\n" +
		"Access to enclosing constructor X.B() is emulated by a synthetic accessor method\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 16)\n" +
		"	private class B extends A {\n" +
		"	              ^\n" +
		"Access to enclosing constructor X.A() is emulated by a synthetic accessor method\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 23)\n" +
		"	public void foo(double a) {\n" +
		"	            ^^^^^^^^^^^^^\n" +
		"The method foo(double) from the type X.B is never used locally\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 31)\n" +
		"	public void foo(int a) {\n" +
		"	            ^^^^^^^^^^\n" +
		"The method foo(int) from the type X.A is never used locally\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 34)\n" +
		"	public void foo(float a) {\n" +
		"	            ^^^^^^^^^^^^\n" +
		"The method foo(float) from the type X.A is never used locally\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 37)\n" +
		"	public void foo(boolean a) {\n" +
		"	            ^^^^^^^^^^^^^^\n" +
		"The method foo(boolean) from the type X.A is never used locally\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
// check usage via super-call
public void test099b() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X {\n" +
			"    private class A {\n" +
			"    	public void foo(int a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"    	}\n" +
			"	    public void foo(float a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   	public void foo(boolean a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"      	public void foo(Integer a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   }\n" +
			"   private class B extends A {\n" +
			"		public void foo(int a) {\n" +
			"   		super.foo(a);\n" +
			"   	}\n" +
			"		public void foo(float a) {\n" +
			"   		super.foo(a);\n" +
			"   	}\n" +
			"   	public void foo(double a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   	public void foo(char a) {\n" +
			"   		System.out.println(\"Hello\");\n" +
			"   	}\n" +
			"   }\n" +
			"   public class C extends B {\n" +
		    "		public void foo(int a) {\n" +
			"			System.out.println(\"Hello\");\n" +
			"		}\n" +
		    "		public void foo(double a) {\n" +
			"			super.foo(a);\n" +
			"		}\n" +
			"		public void foo(boolean a) {\n" +
			"			super.foo(a);\n" +
			"		}\n" +
			"		public void foo(byte a) {\n" +
			"			System.out.println(\"Hello\");\n" +
			"		}\n" +
		    "   }\n" +
			"}\n"
		};
	if (!isMinimumCompliant(ClassFileConstants.JDK11)) {
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
		runner.expectedCompilerLog =
			"----------\n" +
			"1. WARNING in X.java (at line 16)\n" +
			"	private class B extends A {\n" +
			"	              ^\n" +
			"Access to enclosing constructor X.A() is emulated by a synthetic accessor method\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 30)\n" +
			"	public class C extends B {\n" +
			"	             ^\n" +
			"Access to enclosing constructor X.B() is emulated by a synthetic accessor method\n" +
			"----------\n";
		runner.runWarningTest();
	} else {
		runner.runConformTest();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
public void test100() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X {\n" +
			"    private class A {\n" +
			"        public void foo() {}\n" +
			"    }\n" +
			"    public class B extends A {}\n" +
			"}"
		};
	if (!isMinimumCompliant(ClassFileConstants.JDK11)) {

		runner.expectedCompilerLog =
				"----------\n" +
				"1. WARNING in X.java (at line 5)\n" +
				"	public class B extends A {}\n" +
				"	             ^\n" +
				"Access to enclosing constructor X.A() is emulated by a synthetic accessor method\n" +
				"----------\n";
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
		runner.runWarningTest();
	} else {
		runner.runConformTest();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
public void test101() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X {\n" +
			"    private class A {\n" +
			"        public void foo() {}\n" +
			"        public void foo(int a) {}\n" +
			"    }\n" +
			"    public class B extends A {}\n" +
			"}"
		};
	if (!isMinimumCompliant(ClassFileConstants.JDK11)) {
		runner.expectedCompilerLog =
				"----------\n" +
				"1. WARNING in X.java (at line 6)\n" +
				"	public class B extends A {}\n" +
				"	             ^\n" +
				"Access to enclosing constructor X.A() is emulated by a synthetic accessor method\n" +
				"----------\n";
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
		runner.runWarningTest();
	} else {
		runner.runConformTest();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
public void test102() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X {\n" +
			"    private class A {\n" +
			"        private void foo() {}\n" +
			"        private void foo(int a) {}\n" +
			"    }\n" +
			"    public class B extends A {}\n" +
			"}"
		};
	runner.expectedCompilerLog = isMinimumCompliant(ClassFileConstants.JDK11) ?
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	private void foo() {}\n" +
		"	             ^^^^^\n" +
		"The method foo() from the type X.A is never used locally\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	private void foo(int a) {}\n" +
		"	             ^^^^^^^^^^\n" +
		"The method foo(int) from the type X.A is never used locally\n" +
		"----------\n"
		:
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	private void foo() {}\n" +
		"	             ^^^^^\n" +
		"The method foo() from the type X.A is never used locally\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	private void foo(int a) {}\n" +
		"	             ^^^^^^^^^^\n" +
		"The method foo(int) from the type X.A is never used locally\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 6)\n" +
		"	public class B extends A {}\n" +
		"	             ^\n" +
		"Access to enclosing constructor X.A() is emulated by a synthetic accessor method\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
public void test103() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X {\n" +
			"    private class A {\n" +
			"        public void foo() {}\n" +
			"        public void foo(int a) {}\n" +
			"    }\n" +
			"    private class B extends A {}\n" +
			"}"
		};
	runner.expectedCompilerLog = isMinimumCompliant(ClassFileConstants.JDK11) ?
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	public void foo() {}\n" +
		"	            ^^^^^\n" +
		"The method foo() from the type X.A is never used locally\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	public void foo(int a) {}\n" +
		"	            ^^^^^^^^^^\n" +
		"The method foo(int) from the type X.A is never used locally\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 6)\n" +
		"	private class B extends A {}\n" +
		"	              ^\n" +
		"The type X.B is never used locally\n" +
		"----------\n"
		:
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	public void foo() {}\n" +
		"	            ^^^^^\n" +
		"The method foo() from the type X.A is never used locally\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	public void foo(int a) {}\n" +
		"	            ^^^^^^^^^^\n" +
		"The method foo(int) from the type X.A is never used locally\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 6)\n" +
		"	private class B extends A {}\n" +
		"	              ^\n" +
		"The type X.B is never used locally\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 6)\n" +
		"	private class B extends A {}\n" +
		"	              ^\n" +
		"Access to enclosing constructor X.A() is emulated by a synthetic accessor method\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
public void test104() {
	this.runNegativeTest(
		new String[] {
			"p/Bar.java", //-----------------------------------------------------------------------
			"package p;\n" +
			"import q.Zork;\n" +
			"public abstract class Bar {\n" +
			"	protected abstract boolean isBaz();\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in p\\Bar.java (at line 2)\n" +
		"	import q.Zork;\n" +
		"	       ^\n" +
		"The import q cannot be resolved\n" +
		"----------\n",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no custom options */,
		true /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false  /* do not skip javac for this peculiar test */,
		false  /* do not perform statements recovery */);
	Runner runner = new Runner();
	runner.shouldFlushOutputDirectory =
		false;
	runner.testFiles =
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"import p.Bar;\n" +
			"public class X extends Bar {\n" +
			"	protected boolean isBaz() {\n" +
			"		return false;\n" +
			"	}\n" +
			"}",
		};
	runner.expectedOutputString =
		"";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.JavacHasErrorsEclipseHasNone; // ecj can create .class from erroneous .java
	runner.runConformTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=243917
public void test105() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    static {\n" +
			"        System.loadLibrary(\"tpbrooktrout\");\n" +
			"    }\n" +
			"    private final int time;\n" +
			"    private int foo() { return 0;}\n" +
			"    private class Inner {}\n" +
			"    public X(int delay) {\n" +
			"        time = delay;\n" +
			"    }\n" +
			"    public native void run(Inner i);\n" +
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=245007
public void test106() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		new Listener() {\n" +
			"			  void foo(int a) { }\n" +
			"		}.bar();\n" +
			"       new Listener() {\n" +
			"			  void foo(int a) { }\n" +
			"		}.field = 10;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	new Listener() {\n" +
		"	    ^^^^^^^^\n" +
		"Listener cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	new Listener() {\n" +
		"	    ^^^^^^^^\n" +
		"Listener cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=319425
public void test107() {
	this.runNegativeTest(
		new String[] {
			"p/OuterBogus.java", //-----------------------------------------------------------------------
			"package p;\n" +
			"abstract final class OuterBogus {\n" +
			"	public static void call() {\n" +
			"		System.out.println(\"Hi. I'm outer bogus.\");\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in p\\OuterBogus.java (at line 2)\n" +
		"	abstract final class OuterBogus {\n" +
		"	                     ^^^^^^^^^^\n" +
		"The class OuterBogus can be either abstract or final, not both\n" +
		"----------\n",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no custom options */,
		true /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false  /* do not skip javac for this peculiar test */,
		false  /* do not perform statements recovery */);
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"p/Bogus.java", //-----------------------------------------------------------------------
			"package p;\n" +
			"\n" +
			"public class Bogus {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			OuterBogus.call();\n" +
			"		} catch(ClassFormatError e) {\n" +
			"			System.out.println(\"Wrong error found\");\n" +
			"		} catch(Error e) {\n" +
			"			System.out.println(\"Compilation error found\");\n" +
			"		}\n" +
			"	}\n" +
			"}",
		};
	runner.expectedOutputString =
		"Compilation error found";
	runner.shouldFlushOutputDirectory =
		false;
	runner.javacTestOptions =
		JavacTestOptions.Excuse.JavacHasErrorsEclipseHasNone; // ecj can create .class from erroneous .java
	runner.runConformTest();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=321414
public void test108() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"----------\n" +
			"1. WARNING in SyntheticConstructorTooManyArgs.java (at line 23)\n" +
			"	@SuppressWarnings(\"synthetic-access\")\n" +
			"	                  ^^^^^^^^^^^^^^^^^^\n" +
			"Unnecessary @SuppressWarnings(\"synthetic-access\")\n" +
			"----------\n"
			:
			"----------\n" +
			"1. ERROR in SyntheticConstructorTooManyArgs.java (at line 4)\n" +
			"	private A(\n" +
			"			/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f, \n" +
			"			int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f, \n" +
			"			int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f, \n" +
			"			int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f, \n" +
			"			int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f, \n" +
			"			int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f, \n" +
			"			int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f, \n" +
			"			int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f, \n" +
			"			int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f, \n" +
			"			int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f, \n" +
			"			int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf, \n" +
			"			int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf, \n" +
			"			int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf, \n" +
			"			int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf, \n" +
			"			int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef, \n" +
			"			int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd, int pfe\n" +
			"			) {}\n" +
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The synthetic method created to access A(int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int) of type SyntheticConstructorTooManyArgs.A has too many parameters\n" +
			"----------\n";
	this.runNegativeTest(
		new String[] {
			"SyntheticConstructorTooManyArgs.java", //-----------------------------------------------------------------------
			"public class SyntheticConstructorTooManyArgs {\n" +
			"\n" +
			"	static class A {\n" +
			"		private A(\n" +
			"			/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f, \n" +
			"			int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f, \n" +
			"			int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f, \n" +
			"			int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f, \n" +
			"			int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f, \n" +
			"			int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f, \n" +
			"			int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f, \n" +
			"			int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f, \n" +
			"			int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f, \n" +
			"			int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f, \n" +
			"			int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf, \n" +
			"			int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf, \n" +
			"			int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf, \n" +
			"			int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf, \n" +
			"			int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef, \n" +
			"			int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd, int pfe\n" +
			"			) {}\n" +
			"	}\n" +
			"	@SuppressWarnings(\"synthetic-access\")\n" +
			"	A a = new A(\n" +
			"		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\n" +
			"		);\n" +
			"	public static void main(String[] args) {\n" +
			"		StringBuilder params = new StringBuilder();\n" +
			"		params.append(\"/*this,*/\");\n" +
			"		for (int p = 1; p < 255; p++) {\n" +
			"			if (p > 1) {\n" +
			"				params.append(\", \");\n" +
			"				if (p % 16 == 0)\n" +
			"					params.append('\\n');\n" +
			"			}\n" +
			"			params.append(\"int p\"\n" +
			"					+ Character.forDigit(p / 16, 16)\n" +
			"					+ Character.forDigit(p % 16, 16)\n" +
			"					);\n" +
			"		}\n" +
			"		System.out.println(params);\n" +
			"		A.class.getName(); // ClassFormatError\n" +
			"	}\n" +
			"}",
		},
		errMessage);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=321414
public void test109() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runConformTest(
		new String[] {
			"SyntheticConstructorTooManyArgs.java", //-----------------------------------------------------------------------
			"public class SyntheticConstructorTooManyArgs {\n" +
			"\n" +
			"	static class A {\n" +
			"		private A foo(\n" +
			"			/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f, \n" +
			"			int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f, \n" +
			"			int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f, \n" +
			"			int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f, \n" +
			"			int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f, \n" +
			"			int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f, \n" +
			"			int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f, \n" +
			"			int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f, \n" +
			"			int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f, \n" +
			"			int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f, \n" +
			"			int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf, \n" +
			"			int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf, \n" +
			"			int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf, \n" +
			"			int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf, \n" +
			"			int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef, \n" +
			"			int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd, int pfe\n" +
			"			) { return new A();}\n" +
			"	}\n" +
			"	@SuppressWarnings(\"synthetic-access\")\n" +
			"	A a = new A().foo(\n" +
			"		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\n" +
			"		);\n" +
			"	public static void main(String[] args) {\n" +
			"		StringBuilder params = new StringBuilder();\n" +
			"		params.append(\"/*this,*/\");\n" +
			"		for (int p = 1; p < 255; p++) {\n" +
			"			if (p > 1) {\n" +
			"				params.append(\", \");\n" +
			"				if (p % 16 == 0)\n" +
			"					params.append('\\n');\n" +
			"			}\n" +
			"			params.append(\"int p\"\n" +
			"					+ Character.forDigit(p / 16, 16)\n" +
			"					+ Character.forDigit(p % 16, 16)\n" +
			"					);\n" +
			"		}\n" +
			"		System.out.println(params);\n" +
			"		A.class.getName(); // ClassFormatError\n" +
			"	}\n" +
			"}",
		},
		"/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f, \n" +
		"int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f, \n" +
		"int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f, \n" +
		"int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f, \n" +
		"int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f, \n" +
		"int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f, \n" +
		"int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f, \n" +
		"int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f, \n" +
		"int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f, \n" +
		"int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f, \n" +
		"int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf, \n" +
		"int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf, \n" +
		"int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf, \n" +
		"int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf, \n" +
		"int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef, \n" +
		"int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd, int pfe");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=321414
public void test110() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runConformTest(
		new String[] {
			"SyntheticConstructorTooManyArgs.java", //-----------------------------------------------------------------------
			"public class SyntheticConstructorTooManyArgs {\n" +
			"\n" +
			"	static class A {\n" +
			"		private static A foo(\n" +
			"			/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f, \n" +
			"			int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f, \n" +
			"			int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f, \n" +
			"			int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f, \n" +
			"			int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f, \n" +
			"			int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f, \n" +
			"			int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f, \n" +
			"			int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f, \n" +
			"			int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f, \n" +
			"			int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f, \n" +
			"			int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf, \n" +
			"			int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf, \n" +
			"			int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf, \n" +
			"			int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf, \n" +
			"			int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef, \n" +
			"			int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd, int pfe\n" +
			"			) { return new A();}\n" +
			"	}\n" +
			"	@SuppressWarnings(\"synthetic-access\")\n" +
			"	A a = A.foo(\n" +
			"		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\n" +
			"		);\n" +
			"	public static void main(String[] args) {\n" +
			"		StringBuilder params = new StringBuilder();\n" +
			"		params.append(\"/*this,*/\");\n" +
			"		for (int p = 1; p < 255; p++) {\n" +
			"			if (p > 1) {\n" +
			"				params.append(\", \");\n" +
			"				if (p % 16 == 0)\n" +
			"					params.append('\\n');\n" +
			"			}\n" +
			"			params.append(\"int p\"\n" +
			"					+ Character.forDigit(p / 16, 16)\n" +
			"					+ Character.forDigit(p % 16, 16)\n" +
			"					);\n" +
			"		}\n" +
			"		System.out.println(params);\n" +
			"		A.class.getName(); // ClassFormatError\n" +
			"	}\n" +
			"}",
		},
		"/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f, \n" +
		"int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f, \n" +
		"int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f, \n" +
		"int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f, \n" +
		"int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f, \n" +
		"int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f, \n" +
		"int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f, \n" +
		"int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f, \n" +
		"int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f, \n" +
		"int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f, \n" +
		"int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf, \n" +
		"int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf, \n" +
		"int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf, \n" +
		"int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf, \n" +
		"int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef, \n" +
		"int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd, int pfe");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=321414
public void test111() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"----------\n" +
			"1. WARNING in SyntheticConstructorTooManyArgs.java (at line 23)\n" +
			"	@SuppressWarnings(\"synthetic-access\")\n" +
			"	                  ^^^^^^^^^^^^^^^^^^\n" +
			"Unnecessary @SuppressWarnings(\"synthetic-access\")\n" +
			"----------\n"
			:
			"----------\n" +
			"1. ERROR in SyntheticConstructorTooManyArgs.java (at line 4)\n" +
			"	private A(\n" +
			"			/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f, \n" +
			"			int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f, \n" +
			"			int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f, \n" +
			"			int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f, \n" +
			"			int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f, \n" +
			"			int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f, \n" +
			"			int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f, \n" +
			"			int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f, \n" +
			"			int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f, \n" +
			"			int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f, \n" +
			"			int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf, \n" +
			"			int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf, \n" +
			"			int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf, \n" +
			"			int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf, \n" +
			"			int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef, \n" +
			"			int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd\n" +
			"			) {}\n" +
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The synthetic method created to access A(int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int) of type SyntheticConstructorTooManyArgs.A has too many parameters\n" +
			"----------\n";
	this.runNegativeTest(
		new String[] {
			"SyntheticConstructorTooManyArgs.java", //-----------------------------------------------------------------------
			"public class SyntheticConstructorTooManyArgs {\n" +
			"\n" +
			"	class A {\n" +
			"		private A(\n" +
			"			/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f, \n" +
			"			int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f, \n" +
			"			int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f, \n" +
			"			int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f, \n" +
			"			int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f, \n" +
			"			int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f, \n" +
			"			int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f, \n" +
			"			int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f, \n" +
			"			int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f, \n" +
			"			int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f, \n" +
			"			int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf, \n" +
			"			int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf, \n" +
			"			int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf, \n" +
			"			int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf, \n" +
			"			int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef, \n" +
			"			int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd\n" +
			"			) {}\n" +
			"	}\n" +
			"	@SuppressWarnings(\"synthetic-access\")\n" +
			"	A a = new A(\n" +
			"		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0\n" +
			"		);\n" +
			"	public static void main(String[] args) {\n" +
			"		StringBuilder params = new StringBuilder();\n" +
			"		params.append(\"/*this,*/\");\n" +
			"		for (int p = 1; p < 255; p++) {\n" +
			"			if (p > 1) {\n" +
			"				params.append(\", \");\n" +
			"				if (p % 16 == 0)\n" +
			"					params.append('\\n');\n" +
			"			}\n" +
			"			params.append(\"int p\"\n" +
			"					+ Character.forDigit(p / 16, 16)\n" +
			"					+ Character.forDigit(p % 16, 16)\n" +
			"					);\n" +
			"		}\n" +
			"		System.out.println(params);\n" +
			"		A.class.getName(); // ClassFormatError\n" +
			"	}\n" +
			"}",
		},
		errMessage);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=321414
public void test112() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runConformTest(
		new String[] {
			"SyntheticConstructorTooManyArgs.java", //-----------------------------------------------------------------------
			"public class SyntheticConstructorTooManyArgs {\n" +
			"\n" +
			"	class A {\n" +
			"		private A(\n" +
			"			/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f, \n" +
			"			int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f, \n" +
			"			int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f, \n" +
			"			int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f, \n" +
			"			int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f, \n" +
			"			int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f, \n" +
			"			int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f, \n" +
			"			int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f, \n" +
			"			int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f, \n" +
			"			int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f, \n" +
			"			int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf, \n" +
			"			int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf, \n" +
			"			int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf, \n" +
			"			int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf, \n" +
			"			int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef, \n" +
			"			int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc\n" +
			"			) {}\n" +
			"	}\n" +
			"	@SuppressWarnings(\"synthetic-access\")\n" +
			"	A a = new A(\n" +
			"		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,\n" +
			"		0,0,0,0,0,0,0,0,0,0,0,0,0\n" +
			"		);\n" +
			"	public static void main(String[] args) {\n" +
			"		StringBuilder params = new StringBuilder();\n" +
			"		params.append(\"/*this,*/\");\n" +
			"		for (int p = 1; p < 253; p++) {\n" +
			"			if (p > 1) {\n" +
			"				params.append(\", \");\n" +
			"				if (p % 16 == 0)\n" +
			"					params.append('\\n');\n" +
			"			}\n" +
			"			params.append(\"int p\"\n" +
			"					+ Character.forDigit(p / 16, 16)\n" +
			"					+ Character.forDigit(p % 16, 16)\n" +
			"					);\n" +
			"		}\n" +
			"		System.out.println(params);\n" +
			"		A.class.getName(); // ClassFormatError\n" +
			"	}\n" +
			"}",
		},
		"/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f, \n" +
		"int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f, \n" +
		"int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f, \n" +
		"int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f, \n" +
		"int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f, \n" +
		"int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f, \n" +
		"int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f, \n" +
		"int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f, \n" +
		"int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f, \n" +
		"int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f, \n" +
		"int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf, \n" +
		"int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf, \n" +
		"int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf, \n" +
		"int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf, \n" +
		"int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef, \n" +
		"int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=325567
public void test113() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"	public static void bar(int i) {\n" +
			"		final String before;\n" +
			"		try {\n" +
			"			before = foo();\n" +
			"		} catch (IOException e) {\n" +
			"			// ignore\n" +
			"		}\n" +
			"		B b = new B(new I() {\n" +
			"			public String bar() {\n" +
			"				return new String(before);\n" +
			"			}\n" +
			"		});\n" +
			"		try {\n" +
			"			b.toString();\n" +
			"		} catch(Exception e) {\n" +
			"			// ignore\n" +
			"		}\n" +
			"	}\n" +
			"	private static String foo() throws IOException {\n" +
			"		return null;\n" +
			"	}\n" +
			"	static class B {\n" +
			"		B(I i) {\n" +
			"			//ignore\n" +
			"		}\n" +
			"	}\n" +
			"	static interface I {\n" +
			"		String bar();\n" +
			"	}\n" +
			"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	return new String(before);\n" +
		"	                  ^^^^^^\n" +
		"The local variable before may not have been initialized\n" +
		"----------\n";
	runner.generateOutput =
		true;
	runner.runNegativeTest();

	runner = new Runner();
	runner.testFiles =
		new String[] {
			"Y.java", //-----------------------------------------------------------------------
			"public class Y {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			X.bar(3);\n" +
			"		} catch(VerifyError e) {\n" +
			"			System.out.println(\"FAILED\");\n" +
			"		}\n" +
			"	}\n" +
			"}",
		};
	runner.expectedOutputString =
		"";
	runner.shouldFlushOutputDirectory =
		false;
	runner.javacTestOptions =
		JavacTestOptions.Excuse.JavacHasErrorsEclipseHasNone; // ecj can create .class from erroneous .java
	runner.runConformTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// QualifiedNameReference, SingleNameReference and MessageSend
// Can be static warning shown
public void test114() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static int field1;\n" +
			"	public static int field2;\n" +
			"	public void bar(int i) {\n" +
			"		System.out.println(foo());\n" +
			"		foo();" +
			"		System.out.println(X.field1);\n" +
			"		System.out.println(field2);\n" +
			"		field2 = 1;\n" +
			"	}\n" +
			"	public final void bar2(int i) {\n" +
			"		System.out.println(foo());\n" +
			"		foo();" +
			"		System.out.println(X.field1);\n" +
			"		System.out.println(field2);\n" +
			"		field2 = 1;\n" +
			"	}\n" +
			"	private void bar3(int i) {\n" +
			"		System.out.println(foo());\n" +
			"		foo();" +
			"		System.out.println(X.field1);\n" +
			"		System.out.println(field2);\n" +
			"		field2 = 1;\n" +
			"	}\n" +
			"	private static String foo() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	public void bar(int i) {\n" +
		"	            ^^^^^^^^^^\n" +
		"The method bar(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	public final void bar2(int i) {\n" +
		"	                  ^^^^^^^^^^^\n" +
		"The method bar2(int) from the type X can be declared as static\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 16)\n" +
		"	private void bar3(int i) {\n" +
		"	             ^^^^^^^^^^^\n" +
		"The method bar3(int) from the type X is never used locally\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 16)\n" +
		"	private void bar3(int i) {\n" +
		"	             ^^^^^^^^^^^\n" +
		"The method bar3(int) from the type X can be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// FieldReference and MessageSend
// Can be static warning shown
public void test115() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X extends B{\n" +
			"	public static int field1;\n" +
			"	public static int field2;\n" +
			"	public void bar(int i) {\n" +
			"		System.out.println(foo());\n" +
			"		X.field2 = 2;\n" +
			"		System.out.println(field1);\n" +
			"		A a = new A();\n" +
			"		a.a1();\n" +
			"	}\n" +
			"	private static String foo() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n" +
			"class A{\n" +
			"	public void a1() {\n" +
			"	}\n" +
			"}\n" +
			"class B{\n" +
			"	public void b1(){\n" +
			"	}\n" +
			"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	public void bar(int i) {\n" +
		"	            ^^^^^^^^^^\n" +
		"The method bar(int) from the type X can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// MessageSend in different ways
public void test116a() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X extends B{\n" +
			"	public static int field1;\n" +
			"	public X xfield;\n" +
			"	public void bar1(int i) {\n" +
			"		baz();\n" +
			"	}\n" +
			"	public void bar2(int i) {\n" +
			"		this.baz();\n" +
			"	}\n" +
			"	public void bar3(int i) {\n" +
			"		this.xfield.baz();\n" +
			"	}\n" +
			"	public void bar4(int i) {\n" +
			"		xfield.baz();\n" +
			"	}\n" +
			"	public void bar5(int i) {\n" +
			"		X x = new X();\n" +
			"		x.baz();\n" +
			"	}\n" +
			"	public void bar6(int i) {\n" +
			"		A.xA.baz();\n" +
			"	}\n" +
			"	public void bar7(int i) {\n" +
			"		b1();\n" +
			"	}\n" +
			"	public void bar8(int i) {\n" +
			"		this.b1();\n" +
			"	}\n" +
			"	public void bar9(int i) {\n" +
			"		new X().b1();\n" +
			"	}\n" +
			"	public void baz() {\n" +
			"	}\n" +
			"}\n" +
			"class A{\n" +
			"	public static X xA;\n" +
			"}\n" +
			"class B{\n" +
			"	public void b1(){\n" +
			"	}\n" +
			"}",
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 16)\n" +
		"	public void bar5(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar5(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 20)\n" +
		"	public void bar6(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar6(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 29)\n" +
		"	public void bar9(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar9(int) from the type X can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// MessageSend in different ways, referencing a static method.
public void test116b() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X extends B{\n" +
			"	public static int field1;\n" +
			"	public static X xfield;\n" +
			"	public void bar1(int i) {\n" +
			"		baz();\n" +
			"	}\n" +
			"	public void bar2(int i) {\n" +
			"		this.baz();\n" +
			"	}\n" +
			"	public void bar3(int i) {\n" +
			"		this.xfield.baz();\n" +
			"	}\n" +
			"	public void bar4(int i) {\n" +
			"		xfield.baz();\n" +
			"	}\n" +
			"	public void bar5(int i) {\n" +
			"		X x = new X();\n" +
			"		x.baz();\n" +
			"	}\n" +
			"	public void bar6(int i) {\n" +
			"		A.xA.baz();\n" +
			"	}\n" +
			"	public void bar7(int i) {\n" +
			"		b1();\n" +
			"	}\n" +
			"	public void bar8(int i) {\n" +
			"		this.b1();\n" +
			"	}\n" +
			"	public void bar9(int i) {\n" +
			"		new X().b1();\n" +
			"	}\n" +
			"	public static void baz() {\n" +
			"	}\n" +
			"}\n" +
			"class A{\n" +
			"	public static X xA;\n" +
			"}\n" +
			"class B{\n" +
			"	public static void b1(){\n" +
			"	}\n" +
			"}",
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	public void bar1(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar1(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 13)\n" +
		"	public void bar4(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar4(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 16)\n" +
		"	public void bar5(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar5(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 20)\n" +
		"	public void bar6(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar6(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 23)\n" +
		"	public void bar7(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar7(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 29)\n" +
		"	public void bar9(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar9(int) from the type X can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// Referring a field in different ways, accessing non-static field.
public void test117a() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X extends B{\n" +
			"	public int field1;\n" +
			"	public X xfield;\n" +
			"	public void bar1(int i) {\n" +
			"		field1 = 1;\n" +
			"	}\n" +
			"	public void bar2(int i) {\n" +
			"		this.field1 = 1;\n" +
			"	}\n" +
			"	public void bar3(int i) {\n" +
			"		System.out.println(field1);\n" +
			"	}\n" +
			"	public void bar4(int i) {\n" +
			"		System.out.println(this.field1);\n" +
			"	}\n" +
			"	public void bar5(int i) {\n" +
			"		X x = new X();\n" +
			"		x.field1 = 1;\n" +
			"	}\n" +
			"	public void bar6(int i) {\n" +
			"		A.xA.field1 = 1;\n" +
			"	}\n" +
			"	public void bar7(int i) {\n" +
			"		b1 = 1;\n" +
			"	}\n" +
			"	public void bar8(int i) {\n" +
			"		this.b1 = 1;\n" +
			"	}\n" +
			"	public void bar9(int i) {\n" +
			"		new X().b1 = 1;\n" +
			"	}\n" +
			"	public void bar10(int i) {\n" +
			"		this.xfield.field1 = 1;\n" +
			"	}\n" +
			"	public void bar11(int i) {\n" +
			"		System.out.println(this.xfield.field1);\n" +
			"	}\n" +
			"	public void bar12(int i) {\n" +
			"		System.out.println(new X().b1);\n" +
			"	}\n" +
			"	public void bar13(int i) {\n" +
			"		System.out.println(b1);\n" +
			"	}\n" +
			"	public void bar14(int i) {\n" +
			"		System.out.println(this.b1);\n" +
			"	}\n" +
			"	public void bar15(int i) {\n" +
			"		xfield.field1 = 1;\n" +
			"	}\n" +
			"	public void bar16(int i) {\n" +
			"		System.out.println(xfield.field1);\n" +
			"	}\n" +
			"	public void baz() {\n" +
			"	}\n" +
			"}\n" +
			"class A{\n" +
			"	public static X xA;\n" +
			"}\n" +
			"class B{\n" +
			"	public int b1;\n" +
			"}",
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 16)\n" +
		"	public void bar5(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar5(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 20)\n" +
		"	public void bar6(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar6(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 29)\n" +
		"	public void bar9(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar9(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 38)\n" +
		"	public void bar12(int i) {\n" +
		"	            ^^^^^^^^^^^^\n" +
		"The method bar12(int) from the type X can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// Referring a field in different ways, accessing non-static field.
public void test117b() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X extends B{\n" +
			"	public static int field1;\n" +
			"	public static X xfield;\n" +
			"	public void bar1(int i) {\n" +
			"		field1 = 1;\n" +
			"	}\n" +
			"	public void bar2(int i) {\n" +
			"		this.field1 = 1;\n" +
			"	}\n" +
			"	public void bar3(int i) {\n" +
			"		System.out.println(field1);\n" +
			"	}\n" +
			"	public void bar4(int i) {\n" +
			"		System.out.println(this.field1);\n" +
			"	}\n" +
			"	public void bar5(int i) {\n" +
			"		X x = new X();\n" +
			"		x.field1 = 1;\n" +
			"	}\n" +
			"	public void bar6(int i) {\n" +
			"		A.xA.field1 = 1;\n" +
			"	}\n" +
			"	public void bar7(int i) {\n" +
			"		b1 = 1;\n" +
			"	}\n" +
			"	public void bar8(int i) {\n" +
			"		this.b1 = 1;\n" +
			"	}\n" +
			"	public void bar9(int i) {\n" +
			"		new X().b1 = 1;\n" +
			"	}\n" +
			"	public void bar10(int i) {\n" +
			"		this.xfield.field1 = 1;\n" +
			"	}\n" +
			"	public void bar11(int i) {\n" +
			"		System.out.println(this.xfield.field1);\n" +
			"	}\n" +
			"	public void bar12(int i) {\n" +
			"		System.out.println(new X().b1);\n" +
			"	}\n" +
			"	public void bar13(int i) {\n" +
			"		System.out.println(b1);\n" +
			"	}\n" +
			"	public void bar14(int i) {\n" +
			"		System.out.println(this.b1);\n" +
			"	}\n" +
			"	public void bar15(int i) {\n" +
			"		xfield.field1 = 1;\n" +
			"	}\n" +
			"	public void bar16(int i) {\n" +
			"		System.out.println(xfield.field1);\n" +
			"	}\n" +
			"	public void baz() {\n" +
			"	}\n" +
			"}\n" +
			"class A{\n" +
			"	public static X xA;\n" +
			"}\n" +
			"class B{\n" +
			"	public static int b1;\n" +
			"}",
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	public void bar1(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar1(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	public void bar3(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar3(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 16)\n" +
		"	public void bar5(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar5(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 20)\n" +
		"	public void bar6(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar6(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 23)\n" +
		"	public void bar7(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar7(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 29)\n" +
		"	public void bar9(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar9(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 38)\n" +
		"	public void bar12(int i) {\n" +
		"	            ^^^^^^^^^^^^\n" +
		"The method bar12(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 41)\n" +
		"	public void bar13(int i) {\n" +
		"	            ^^^^^^^^^^^^\n" +
		"The method bar13(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"9. ERROR in X.java (at line 47)\n" +
		"	public void bar15(int i) {\n" +
		"	            ^^^^^^^^^^^^\n" +
		"The method bar15(int) from the type X can potentially be declared as static\n" +
		"----------\n" +
		"10. ERROR in X.java (at line 50)\n" +
		"	public void bar16(int i) {\n" +
		"	            ^^^^^^^^^^^^\n" +
		"The method bar16(int) from the type X can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// Final class -> can be static (and not potentially be static) warning shown
public void test118() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
				"X.java",
				"final public class X {\n" +
				"	public static int field1;\n" +
				"	public static int field2;\n" +
				"	public void bar(int i) {\n" +
				"		System.out.println(foo());\n" +
				"		foo();" +
				"		System.out.println(X.field1);\n" +
				"		System.out.println(field2);\n" +
				"		field2 = 1;\n" +
				"	}\n" +
				"	public static int foo(){ return 1;}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	public void bar(int i) {\n" +
		"	            ^^^^^^^^^^\n" +
		"The method bar(int) from the type X can be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// Method of a local class -> can't be static, so no warning
// Also method with such a local class accessing a member of the outer class can't be static
public void test119() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X {\n" +
					"	public static int field1;\n" +
					"	public int field2;\n" +
					"	public void bar(int i) {\n" + 	// don't warn
					"		(new Object() {\n" +
					"			public boolean foo1() {\n" +	// don't warn for foo1
					"				return X.this.field2 == 1;\n" +
					"			}\n" +
					"		}).foo1();\n" +
					"	System.out.println(X.field1);\n" +
					"	}\n" +
					"	public void bar2(int i) {\n" + 	// warn
					"		(new Object() {\n" +
					"			public boolean foo1() {\n" +	// don't warn for foo1
					"				System.out.println(X.field1);\n" +
					"				return true;" +
					"			}\n" +
					"		}).foo1();\n" +
					"	System.out.println(X.field1);\n" +
					"	}\n" +
					"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	public void bar2(int i) {\n" +
		"	            ^^^^^^^^^^^\n" +
		"The method bar2(int) from the type X can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// Method using type parameters declared by enclosing class can't be static, so don't warn
public void test120() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	public static int field1;\n" +
				"	public int field2;\n" +
				"	public void bar(T t) {\n" + 	// don't warn
				"		X.field1 = 1;\n" +
				"		System.out.println(t);\n" +
				"	}\n" +
				"	public <E> void bar2(E e) {\n" + 	// warn
				"		X.field1 = 1;\n" +
				"		System.out.println(e);\n" +
				"	}\n" +
				"	public <E> void bar3() {\n" + 	// don't warn
				"		T a;\n" +
				"		System.out.println();\n" +
				"	}\n" +
				"	public <E,Y> void bar4() {\n" + 	// warn
				"		Y a;\n" +
				"		System.out.println();\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	public <E> void bar2(E e) {\n" +
		"	                ^^^^^^^^^\n" +
		"The method bar2(E) from the type X<T> can potentially be declared as static\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 16)\n" +
		"	public <E,Y> void bar4() {\n" +
		"	                  ^^^^^^\n" +
		"The method bar4() from the type X<T> can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// Access to super in a method disqualifies it from being static
public void test121() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X extends A{\n" +
				"	public static int field1;\n" +
				"	public int field2;\n" +
				"	public void methodA() {\n" + 	// don't warn
				"		super.methodA();\n" +
				"	}\n" +
				"	public void bar() {\n" + 	// don't warn
				"		super.fieldA = 1;\n" +
				"	}\n" +
				"	public void bar2() {\n" + 	// don't warn
				"		System.out.println(super.fieldA);\n" +
				"	}\n" +
				"	public void bar3() {\n" + 	// warn
				"		System.out.println(X.fieldA);\n" +
				"	}\n" +
				"}\n" +
				"class A{\n" +
				"	public static int fieldA;\n" +
				"   public void methodA(){\n" +
				"   }\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 13)\n" +
		"	public void bar3() {\n" +
		"	            ^^^^^^\n" +
		"The method bar3() from the type X can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// Methods of non-static member types can't be static
public void test122() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X {\n" +
				"	class A{\n" +
				"   	void methodA() {\n" +	// don't warn
				"			System.out.println();\n" +
				"		}\n" +
				"   }\n" +
				"	static class B{\n" +
				"   	void methodB() {\n" +	// warn
				"			System.out.println();\n" +
				"		}\n" +
				"   }\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	void methodB() {\n" +
		"	     ^^^^^^^^^\n" +
		"The method methodB() from the type X.B can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// If method returns type parameter not declared by it, it cannot be static
public void test123() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	<E,Y> T method1() {\n" + 	// don't warn
				"		return null;\n" +
				"	}\n" +
				"	<E,Y> E method2() {\n" + 	// warn
				"		return null;\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	<E,Y> E method2() {\n" +
		"	        ^^^^^^^^^\n" +
		"The method method2() from the type X<T> can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335845
// If method allocates an inner non-static type without an enclosing object, method can't be static
public void testBug335845a() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"	private class Bar {\n" +
				"		int a = 1;\n" +
				"	}\n" +
				"	private void foo() {\n" + 	// don't warn
				"		new Bar();\n" +
				"	}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335845
// If method allocates an inner non-static type without an enclosing object, method can't be static
public void testBug335845b() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"	private class Bar {\n" +
				"		int a = 1;\n" +
				"	}\n" +
				"	private void foo() {\n" + 	// don't warn
				"		int x = new Bar().a;\n" +
				"	}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335845
// If method allocates an inner static type without an enclosing object, method can be static
public void testBug335845c() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X {\n" +
				"	private static class Bar {\n" +
				"		int a = 1;\n" +
				"	}\n" +
				"	private void foo() {\n" + 	// warn since Bar is static
				"		new Bar();\n" +
				"		int x = new Bar().a;" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	private void foo() {\n" +
		"	             ^^^^^\n" +
		"The method foo() from the type X can be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335845
// If method allocates an inner non-static type without an enclosing object, method can't be static
public void testBug335845d() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"	private class Bar {\n" +
				"		class Bar2{}\n" +
				"	}\n" +
				"	private void foo() {\n" + 	// don't warn
				"		new Bar().new Bar2();\n" +
				"	}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335845
// If method allocates an inner static type without an enclosing object, method can be static
public void testBug335845e() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X {\n" +
				"	private class Bar {\n" +
				"		int a = 1;\n" +
				"	}\n" +
				"	private void foo() {\n" + 	// warn since Bar is allocated via Test object
				"		new X().new Bar();\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	private void foo() {\n" +
		"	             ^^^^^\n" +
		"The method foo() from the type X can be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335845
// If method allocates an inner static type without an enclosing object, method can be static
public void testBug335845f() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X {\n" +
				"	private class Bar {\n" +
				"		int a = 1;\n" +
				"	}\n" +
				"	private void foo() {\n" + 	// warn since Bar is allocated via Test object
				"		X x = new X();" +
				"		x.new Bar().a = 2;\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	private void foo() {\n" +
		"	             ^^^^^\n" +
		"The method foo() from the type X can be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335845
// If method allocates an inner static type without an enclosing object, method can be static
public void testBug335845g() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	class Bar {\n" +
				"	}\n" +
				"}"
		}
	);
	this.runNegativeTest(
		new String[] {
				"p/Y.java",
				"package p;\n" +
				"public class Y extends X {\n" +
				"	private void foo() {\n" + 	// warn since Bar is allocated via Test object
				"		new Bar();\n" +
				"	}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		false /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335780
// For this reference as an argument of a message send, method can't be static
public void test124a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"	public void method1() {\n" + 	// don't warn
				"		Foo.m(this);\n" +
				"	}\n" +
				"static class Foo{\n" +
				"	static void m(X bug) {\n" +
				"		\n" +
				"	}\n" +
				"}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335780
// For this reference as an argument of a message send, method can't be static
public void test124b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"   public static X xField;" +
				"	public void method1() {\n" + 	// don't warn
				"		Foo.m(this.xField);\n" +
				"	}\n" +
				"static class Foo{\n" +
				"	static void m(X bug) {\n" +
				"		\n" +
				"	}\n" +
				"}\n" +
				"}"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	Foo.m(this.xField);\n" +
		"	           ^^^^^^\n" +
		"The static field X.xField should be accessed in a static way\n" +
		"----------\n",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=354502
// Anonymous class instantiation of a non-static member type, method can't be static
public void test354502() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =

		new String[] {
				"X.java",
				"public class X {\n" +
				"   public abstract class Abstract{}\n" +
				"   public static abstract class Abstract2{}\n" +
				"	private void method1() {\n" + 	// don't warn
				"		new Abstract() {};\n" +
				"	}\n" +
				"	private void method2() {\n" + 	// warn
				"		new Abstract2() {};\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	private void method2() {\n" +
		"	             ^^^^^^^^^\n" +
		"The method method2() from the type X can be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=360164
public void test360164() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	this.runConformTest(
			new String[] {
					"p/B.java",
					"package p;\n" +
					"\n" +
					"public abstract class B<K,V> {\n" +
					"	 protected abstract V foo(K element);\n" +
					"}\n",
					"p/C.java",
					"package p;\n" +
					"public class C {\n" +
					"}\n",
					"p/D.java",
					"package p;\n" +
					"public class D extends E {\n" +
					"}\n",
					"p/E.java",
					"package p;\n" +
					"public abstract class E implements I {\n" +
					"}\n",
					"p/I.java",
					"package p;\n" +
					"public interface I {\n" +
					"}\n",
					"p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"	private final class A extends B<C,D>{\n" +
					"		@Override\n" +
					"		protected D foo(C c) {\n" +
					"			return null;\n" +
					"		}\n" +
					"   }\n" +
					"}\n",
			},
			"");

	// delete binary file I (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p" + File.separator + "I.class"));

	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	private final class A extends B<C,D>{\n" +
				"		@Override\n" +
				"		protected D foo(C c) {\n" +
				"            Zork z;\n" +
				"			return null;\n" +
				"		}\n" +
				"   }\n" +
				"}\n",
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		// compiler results
		"----------\n" +
		"1. WARNING in p\\X.java (at line 3)\n" +
		"	private final class A extends B<C,D>{\n" +
		"	                    ^\n" +
		"The type X.A is never used locally\n" +
		"----------\n" +
		"2. ERROR in p\\X.java (at line 6)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",
		// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// SingleNameReference, assignment of instance field inside a local class method
public void test376550_1a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"	int i = 1;\n" +
				"   public void upper1(){}\n" +
				"   public void foo(){\n" + // can't be static
				"   	class Local{\n" +
				"			int i2 = 1;\n" +
				"			void method1() {\n" + // can't be static
				"				i = 1;\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// SingleNameReference, assignment of instance field of local class inside a local class method
public void test376550_1b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	new Runner() {{
	  this.customOptions = getCompilerOptions();
	  this.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	  this.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	  this.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	  this.testFiles =
		new String[] {
				"X.java",
				"public class X {\n" +
				"	int i = 1;\n" +
				"   public void upper1(){}\n" +
				"   public void foo(){\n" + // can be static
				"   	class Local{\n" +
				"			int i2 = 1;\n" +
				"			void method2() {\n" +  // can't be static
				"				i2 = 1;\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		};
	  this.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	public void foo(){\n" +
		"	            ^^^^^\n" +
		"The method foo() from the type X can potentially be declared as static\n" +
		"----------\n";
	  this.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	}}.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// LocalDeclaration with type as a type variable binding
public void test376550_2a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X<T> {\n" +
				"   public void upper1(){}\n" +
				"   public void foo(){\n" + // can be static
				"   	class Local<K>{\n" +
				"			void method2() {\n" +  // can't be static
				"				K k;\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public void foo(){\n" +
		"	            ^^^^^\n" +
		"The method foo() from the type X<T> can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// LocalDeclaration with type as a type variable binding
public void test376550_2b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X<T> {\n" +
				"   public void upper1(){}\n" +
				"   public void foo(){\n" + // can't be static
				"   	class Local<K>{\n" +
				"			void method2() {\n" +  // can't be static
				"				T t;\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// MessageSend, calling outer class method inside a local class method
public void test376550_3a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X<T> {\n" +
				"   public void upper1(){}\n" +
				"   public void foo(){\n" + // can't be static
				"   	class Local<K>{\n" +
				"			void lower() {}\n" +
				"			void method2() {\n" +  // can't be static
				"				upper1();\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// MessageSend, calling local class method inside a local class method
public void test376550_3b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X<T> {\n" +
				"   public void upper1(){}\n" +
				"   public void foo(){\n" + // can be static
				"   	class Local<K>{\n" +
				"			void lower() {}\n" +
				"			void method2() {\n" +  // can't be static
				"				lower();\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public void foo(){\n" +
		"	            ^^^^^\n" +
		"The method foo() from the type X<T> can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// Local class instance field is an argument in messageSend in local class method
public void test376550_4a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X<T> {\n" +
				"   int i1 = 1;\n" +
				"   public void foo(){\n" + // can be static
				"   	class Local<K>{\n" +
				"			int i2 = 1;\n" +
				"			void lower(int i) {}\n" +
				"			void method2() {\n" +  // can't be static
				"				lower(i2);\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public void foo(){\n" +
		"	            ^^^^^\n" +
		"The method foo() from the type X<T> can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// Outerclass instance field is an argument in messageSend in local class method
public void test376550_4b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X<T> {\n" +
				"   int i1 = 1;\n" +
				"   public void foo(){\n" + // can't be static
				"   	class Local<K>{\n" +
				"			int i2 = 1;\n" +
				"			void lower(int i) {}\n" +
				"			void method2() {\n" +  // can't be static
				"				lower(i1);\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameReference, accessing local class instance field
public void test376550_5a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X {\n" +
				"   int i1 = 1;\n" +
				"   public void foo(){\n" + // can be static
				"   	class Local{\n" +
				"			int i2 = 1;\n" +
				"			void method2() {\n" +  // can't be static
				"				Local.this.i2 = 1;\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public void foo(){\n" +
		"	            ^^^^^\n" +
		"The method foo() from the type X can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// https://bugs.eclispe.org/379784 - [compiler] "Method can be static" is not getting reported
// Variation of the above
public void test376550_5aa() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X {\n" +
				"	int i1 = 1;\n" +
				"	public void foo(){\n" +
				"		class Local{\n" +
				"			int i2 = 1;\n" +
				"       }\n" +
				"       class Local2 extends Local {\n" +
				"			void method2() {\n" +
				"				Local2.this.i2 = 1;\n" + // required instance is of type Local (super of Local2)
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public void foo(){\n" +
		"	            ^^^^^\n" +
		"The method foo() from the type X can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameReference, accessing outer class instance field
public void test376550_5b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"   int i1 = 1;\n" +
				"   public void foo(){\n" + // can't be static
				"   	class Local{\n" +
				"			int i2 = 1;\n" +
				"			void method2() {\n" +  // can't be static
				"				X.this.i1 = 1;\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameRef.analyseCode()
public void test376550_6a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X {\n" +
				"   int i1 = 1;\n" +
				"   public void foo(){\n" + // can be static
				"   	class Local{\n" +
				"			int i2 = 1;\n" +
				"			boolean method2() {\n" +  // can't be static
				"				return Local.this.i2 == 1;\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public void foo(){\n" +
		"	            ^^^^^\n" +
		"The method foo() from the type X can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameRef.analyseCode()
public void test376550_6b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"   int i1 = 1;\n" +
				"   public void foo(){\n" + // can't be static
				"   	class Local{\n" +
				"			int i2 = 1;\n" +
				"			boolean method2() {\n" +  // can't be static
				"				return X.this.i1 == 1;\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedAllocationExpression, allocating an anonymous type without an enclosing instance of parent type
// anon. type is declared in local class
public void test376550_7a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X {\n" +
				"   abstract class AbsUp{}\n" +
				"   public void foo(){\n" + // can be static
				"   	class Local{\n" +
				"			abstract class AbsLow{}\n" +
				"			void method2() {\n" +  // can't be static
				"				new AbsLow(){};\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public void foo(){\n" +
		"	            ^^^^^\n" +
		"The method foo() from the type X can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedAllocationExpression, allocating an anonymous type without an enclosing instance of parent type
// anon. type is declared in outer class
public void test376550_7b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"   abstract class AbsUp{}\n" +
				"   public void foo(){\n" + // can't be static
				"   	class Local{\n" +
				"			abstract  class AbsLow{}\n" +
				"			void method2() {\n" +  // can't be static
				"				new AbsUp(){};\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// FieldRef, from object of a class in outer class
public void test376550_8a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X {\n" +
				"   class AbsUp{ int a;}\n" +
				"   public void foo(){\n" + // can be static
				"   	class Local{\n" +
				"			class AbsLow{  int a;}\n" +
				"			void method2() {\n" +  // can't be static
				"				int abc = new AbsLow().a;\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public void foo(){\n" +
		"	            ^^^^^\n" +
		"The method foo() from the type X can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
//FieldRef, from object of a class in local class
public void test376550_8b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"   class AbsUp{ int a;}\n" +
				"   public void foo(){\n" + // can't be static
				"   	class Local{\n" +
				"			class AbsLow{  int a;}\n" +
				"			void method2() {\n" +  // can't be static
				"				int abc = new AbsUp().a;\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameRef, accessing a field from local class field
public void test376550_9a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X {\n" +
				"   X xup;\n" +
				"	int i = 1;\n" +
				"   public void foo(){\n" + // can be static
				"   	class Local{\n" +
				"			X xdown;\n" +
				"			class AbsLow{  int a;}\n" +
				"			void method2() {\n" +  // can't be static
				"				int abc = xdown.i;\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	public void foo(){\n" +
		"	            ^^^^^\n" +
		"The method foo() from the type X can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameRef, accessing a field from local class field
public void test376550_9b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"   X xup;\n" +
				"	int i = 1;\n" +
				"   public void foo(){\n" + // can't be static
				"   	class Local{\n" +
				"			X xdown;\n" +
				"			class AbsLow{  int a;}\n" +
				"			void method2() {\n" +  // can't be static
				"				int abc = xup.i;\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameRef, accessing a field from local class field
public void test376550_10a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"public class X {\n" +
				"   X xup;\n" +
				"	int i = 1;\n" +
				"   public void foo(){\n" + // can be static
				"   	class Local{\n" +
				"			X xdown;\n" +
				"			void calc(int i1){}\n" +
				"			void method2() {\n" +  // can't be static
				"				calc(xdown.i);\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	public void foo(){\n" +
		"	            ^^^^^\n" +
		"The method foo() from the type X can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameRef, accessing a field from local class field
public void test376550_10b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"   X xup;\n" +
				"	int i = 1;\n" +
				"   public void foo(){\n" + // can't be static
				"   	class Local{\n" +
				"			X xdown;\n" +
				"			void calc(int i1){}\n" +
				"			void method2() {\n" +  // can't be static
				"				calc(xup.i);\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// bug test case
public void test376550_11() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.Collection;\n" +
				"public class X {\n" +
				"   private Object o = new Object();\n" +
				"   public final Collection<Object> go() {\n" + // can't be static
				"   	return new ArrayList<Object>() {\n" +
				"			{ add(o);}\n" +
				"		};\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog = isMinimumCompliant(ClassFileConstants.JDK11) ?
		"----------\n" +
		"1. WARNING in X.java (at line 6)\n" +
		"	return new ArrayList<Object>() {\n" +
		"	           ^^^^^^^^^^^^^^^^^^^\n" +
		"The serializable class  does not declare a static final serialVersionUID field of type long\n" +
		"----------\n"
		:
		"----------\n" +
		"1. WARNING in X.java (at line 6)\n" +
		"	return new ArrayList<Object>() {\n" +
		"	           ^^^^^^^^^^^^^^^^^^^\n" +
		"The serializable class  does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 7)\n" +
		"	{ add(o);}\n" +
		"	      ^\n" +
		"Read access to enclosing field X.o is emulated by a synthetic accessor method\n" +
		"----------\n";
	runner.runWarningTest();
}

// https://bugs.eclipse.org/376550
// https://bugs.eclipse.org/379784 - [compiler] "Method can be static" is not getting reported
// bug test case
public void test376550_11a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.Collection;\n" +
				"public class X {\n" +
				"   private Object o = new Object();\n" +
				"   public final Collection<Object> go() {\n" +// can be static
				"   	return new ArrayList<Object>() {\n" +
				"			{ add(null);}\n" +	// required instance is of type ArrayList, not X
				"		};\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	public final Collection<Object> go() {\n" +
		"	                                ^^^^\n" +
		"The method go() from the type X can be declared as static\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 6)\n" +
		"	return new ArrayList<Object>() {\n" +
		"	           ^^^^^^^^^^^^^^^^^^^\n" +
		"The serializable class  does not declare a static final serialVersionUID field of type long\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
public void test376550_12() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.Collection;\n" +
				"public class X<E> {\n" +
				"   private Object o = new Object();\n" +
				"   public final <E1> Collection<E1> go() {\n" + // CAN be static
				"   	return new ArrayList<E1>() {\n" +
				"			{ E1 e;}\n" +
				"		};\n" +
				"	}\n" +
				"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	public final <E1> Collection<E1> go() {\n" +
		"	                                 ^^^^\n" +
		"The method go() from the type X<E> can be declared as static\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 6)\n" +
		"	return new ArrayList<E1>() {\n" +
		"	           ^^^^^^^^^^^^^^^\n" +
		"The serializable class  does not declare a static final serialVersionUID field of type long\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// https://bugs.eclipse.org/379834 - Wrong "method can be static" in presence of qualified super and different staticness of nested super class.
public void test376550_13() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
				"QualifiedSuper.java",
				"public class QualifiedSuper {\n" +
				"	class InnerS {\n" +
				"		void flub() {}\n" +
				"	}\n" +
				"	static class InnerT extends InnerS {\n" +
				"		InnerT(QualifiedSuper qs) {\n" +
				"			qs.super();\n" +
				"		}\n" +
				"		final void schlumpf() {\n" +
				"			InnerT.super.flub();\n" +
				"		}\n" +
				"	}	\n" +
				"}\n"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null,
		compilerOptions /* custom options */,
		null
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=379530
public void test379530() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X<S> {\n" +
				"   S s;\n" +
				"	{\n" +
				"		 S /*[*/s/*]*/;\n" +
				"		 s= X.this.s;" +
				"	}\n" +
				"}"
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null,
		compilerOptions /* custom options */,
		null
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=393781
public void test393781() {
	Map compilerOptions = getCompilerOptions(); // OPTION_ReportRawTypeReference
	Object oldOption = compilerOptions.get(CompilerOptions.OPTION_ReportRawTypeReference);
	compilerOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	try {
		this.runNegativeTest(
				new String[] {
						"p/X.java",
						"public class X {\n" +
						"   public void foo(Map map, String str) {}\n" +
						"	public void foo1() {}\n" +
						"	public void bar(java.util.Map map) {\n" +
						"		foo(map, \"\");\n" +
						"		foo(map);\n" +
						"		foo();\n" +
						"		foo1(map, \"\");\n" +
						"	}\n" +
						"}\n" +
						"class Map {}\n"
				},
				"----------\n" +
				"1. ERROR in p\\X.java (at line 5)\n" +
				"	foo(map, \"\");\n" +
				"	^^^\n" +
				"The method foo(Map, java.lang.String) in the type X is not applicable for the arguments (java.util.Map, java.lang.String)\n" +
				"----------\n" +
				"2. ERROR in p\\X.java (at line 6)\n" +
				"	foo(map);\n" +
				"	^^^\n" +
				"The method foo(Map, String) in the type X is not applicable for the arguments (Map)\n" +
				"----------\n" +
				"3. ERROR in p\\X.java (at line 7)\n" +
				"	foo();\n" +
				"	^^^\n" +
				"The method foo(Map, String) in the type X is not applicable for the arguments ()\n" +
				"----------\n" +
				"4. ERROR in p\\X.java (at line 8)\n" +
				"	foo1(map, \"\");\n" +
				"	^^^^\n" +
				"The method foo1() in the type X is not applicable for the arguments (Map, String)\n" +
				"----------\n",
				null,
				true,
				compilerOptions /* default options */
			);
	} finally {
		compilerOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, oldOption);
	}
}
private void runStaticWarningConformTest(String fileName, String body) {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			fileName,
			body
		},
		compilerOptions /* custom options */
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
//Can be static warning shown in the wrong places, i.e. if the type parameter is used in the signature
public void test378674_comment0() {
	runStaticWarningConformTest(
		"Test.java",
		"public class Test<T> {\n" +
		"\n" +
		"    @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n" +
		"    public static void main(String[] args) {\n" +
		"        new Test().method(null);\n" +
		"    }\n" +
		"\n" +
		"    private static class SubClass<A> {\n" +
		"\n" +
		"    }\n" +
		"\n" +
		"    private void method(SubClass<T> s) {\n" +
		"        System.out.println(s);\n" +
		"    }\n" +
		"\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment1b() {
	runStaticWarningConformTest(
		"X.java",
		"import java.util.Collection;\n" +
		"class X<E>{\n" +
		"   public final <E1> Collection<E> go() {  // cannot be static\n" +
		"		return null; \n" +
		"   }\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
//Can be static warning shown in the wrong places
public void test378674_comment1c() {
	runStaticWarningConformTest(
		"X.java",
		"import java.util.Collection;\n" +
		"import java.util.ArrayList;\n" +
		"	class X<E>{\n" +
		"   public final <E1> Collection<?> go() {  // cannot be static\n" +
		"		return new ArrayList<E>(); \n" +
		"   }\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
//Can be static warning shown in the wrong places
public void test378674_comment2() {
	runStaticWarningConformTest(
		"X.java",
		"public class X<T> {\n" +
		"	public final void foo() {\n" +
		"		java.util.List<T> k;\n" +
		"	}\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment3() {
	runStaticWarningConformTest(
		"Test.java",
		"public class Test {\n" +
		"	//false positive of method can be declared static\n" +
		"	void bar() {\n" +
		"		foo(Test.this);\n" +
		"	}\n" +
		"\n" +
		"	private static void foo(Test test) {\n" +
		"		System.out.println(test.getClass().getName());\n" +
		"	}\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
//Can be static warning shown in the wrong places
public void test378674_comment5a() {
	runStaticWarningConformTest(
		"Test.java",
		"public class Test<T> {\n" +
		"\n" +
		"    @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n" +
		"    public static void main(String[] args) {\n" +
		"        new Test().method2(null);\n" +
		"    }\n" +
		"\n" +
		"    private static class SubClass<A> {\n" +
		"\n" +
		"    }\n" +
		"\n" +
		"    private void method2(SubClass<java.util.List<T>> s) {\n" +
		"        System.out.println(s);\n" +
		"    }\n" +
		"\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment5b() {
	runStaticWarningConformTest(
		"Test.java",
		"public class Test<T> {\n" +
		"\n" +
		"    @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n" +
		"    public static void main(String[] args) {\n" +
		"        new Test().method();\n" +
		"    }\n" +
		"\n" +
		"    private java.util.Collection<T> method() {\n" +
		"        return null;\n" +
		"    }\n" +
		"\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment9() {
	runStaticWarningConformTest(
		"Test.java",
		"public class Test<T> {\n" +
		"\n" +
		"    @SuppressWarnings({ \"rawtypes\" })\n" +
		"    public static void main(String[] args) {\n" +
		"        new Test().method();\n" +
		"    }\n" +
		"\n" +
		"    private java.util.Collection<? extends T> method() {\n" +
		"        return null;\n" +
		"    }\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment11() {
	runStaticWarningConformTest(
		"Test.java",
		"public class Test<T> {\n" +
		"\n" +
		"    @SuppressWarnings({ \"rawtypes\" })\n" +
		"    public static void main(String[] args) {\n" +
		"        new Test().method1();\n" +
		"        new Test().method2();\n" +
		"    }\n" +
		"\n" +
		"   private <TT extends T> TT method1() { \n" +
		"		return null;\n" +
		"	}\n" +
		"\n" +
		"   private <TT extends Object & Comparable<? super T>> TT method2() { \n" +
		"		return null;\n" +
		"	}\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment21a() {
	runStaticWarningConformTest(
		"X.java",
		"public class X<P extends Exception> {\n" +
		"	final <T> void foo(T x) throws P {\n" +
		"	}\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment21b() {
	runStaticWarningConformTest(
		"X.java",
		"public class X<P extends Exception> {\n" +
		"	final <T> void foo(T x) {\n" +
		"		Object o = (P) null;\n" +
		"	}\n" +
		"}\n"
	);
}//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment21c() {
	runStaticWarningConformTest(
		"X.java",
		"public class X<P extends Exception> {\n" +
		"	final <T> void foo(T x) {\n" +
		"		new Outer().new Inner<P>();\n" +
		"	}\n" +
		"}\n" +
		"class Outer {\n" +
		"	class Inner<Q> {}\n" +
		"}\n"
	);
}//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment21d() {
	runStaticWarningConformTest(
		"X.java",
		"public class X<P extends Exception> {\n" +
		"	final <T> void foo(T x) {\n" +
		"		class Local {\n" +
		"			P p;\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406396, Method can be static analysis misses a bunch of cases...
public void test406396() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X  {\n" +
			"	int f;\n" +
			"	void foo() {\n" +
			"		class Y {\n" +
			"			int p;\n" +
			"			{\n" +
			"				class Z {\n" +
			"					int f = p;\n" +
			"				}\n" +
			"			}\n" +
			"		};\n" +
			"	}\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	void foo() {\n" +
		"	     ^^^^^\n" +
		"The method foo() from the type X can potentially be declared as static\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	class Y {\n" +
		"	      ^\n" +
		"The type Y is never used locally\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 7)\n" +
		"	class Z {\n" +
		"	      ^\n" +
		"The type Z is never used locally\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 8)\n" +
		"	int f = p;\n" +
		"	    ^\n" +
		"The field Z.f is hiding a field from type X\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 8)\n" +
		"	int f = p;\n" +
		"	    ^\n" +
		"The value of the field Z.f is not used\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406396, Method can be static analysis misses a bunch of cases...
public void test406396a() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X  {\n" +
			"	int f;\n" +
			"	int foo() {\n" +
			"		int f = 0;\n" +
			"		return f;\n" +
			"	}\n" +
			"	int goo() {\n" +
			"		return 0;\n" +
			"	}\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	int foo() {\n" +
		"	    ^^^^^\n" +
		"The method foo() from the type X can potentially be declared as static\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	int f = 0;\n" +
		"	    ^\n" +
		"The local variable f is hiding a field from type X\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	int goo() {\n" +
		"	    ^^^^^\n" +
		"The method goo() from the type X can potentially be declared as static\n" +
		"----------\n";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug542829() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return;

	// m.Issing is a type that comes and goes:
	String nameMissing = "m/Issing.java";
	String contentMissing =
		"package m;\n" +
		"public class Issing {\n" +
		"	g.Ood getGood() { return new g.Ood(); }\n" +
		"}\n";

	Runner runner = new Runner();
	runner.generateOutput = true;
	runner.testFiles = new String[] {
		"g/Ood.java",
		"package g;\n" +
		"public class Ood {\n" +
		"	@Override public String toString() {\n" +
		"		return \"good\";\n" +
		"	}\n" +
		"}\n",
		"g/Ontainer.java",
		"package g;\n" +
		"import java.util.*;\n" +
		"import m.Issing;\n" +
		"public class Ontainer {\n" +
		"	public List<Issing> getElements() { return new ArrayList<>(); }\n" + // <= reference to m.Issing here (OK)
		"}\n",
		nameMissing,
		contentMissing
	};
	runner.expectedCompilerLog = null;
	runner.runConformTest();

	// now we break it:
	Util.delete(new File(OUTPUT_DIR + File.separator + "m" + File.separator + "Issing.class"));
	runner.shouldFlushOutputDirectory = false;

	// in this class file a MissingTypes attribute ("m/Issing") is generated:
	runner.testFiles = new String[] {
		"b/Roken.java",
		"package b;\n" +
		"import g.Ood;" +
		"public class Roken {\n" +
		"	Ood getGood(m.Issing provider) {\n" + // <= argument type is missing (we still have the qualified name, though)
		"		return provider.getGood();\n" +
		"	}\n\n" +
		"}\n"
	};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in b\\Roken.java (at line 3)\n" +
		"	Ood getGood(m.Issing provider) {\n" +
		"	            ^^^^^^^^\n" +
		"m.Issing cannot be resolved to a type\n" +
		"----------\n";
	runner.runNegativeTest();

	runner.javacTestOptions = JavacTestOptions.SKIP; // javac did not produce b/Roken.class which is needed below

	// restore the class as binary:
	runner.testFiles = new String[] {
		nameMissing,
		contentMissing
	};
	runner.expectedCompilerLog = null;
	runner.runConformTest();

	// next compilation has two references to m.Issing:
	runner.testFiles = new String[] {
		"t/Rigger.java",
		"package t;\n" +
		"import b.Roken;\n" + // <= Here we pull in the MissingTypes("m/Issing") attribute into an UnresolvedReferenceBinding
		"public class Rigger {}\n",
		"t/Est.java",
		"package t;\n" +
		"public class Est {\n" +
		"	void foo(g.Ontainer container) {\n" +
		"		for (m.Issing miss: container.getElements()) {\n" + // <= Here we resolve a qualified name from g/Ontainer.class but don't trust it!
		"			System.out.print(miss);\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	};
	runner.runConformTest();
}
public void testBug576735() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;

	String path = getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "lib576735.jar";
	String[] libs = getDefaultClassPaths();
	int len = libs.length;
	System.arraycopy(libs, 0, libs = new String[len+1], 0, len);
	libs[len] = path;
	Runner runner = new Runner();
	runner.classLibraries = libs;
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	runner.testFiles = new String[] {
			"does/not/Work.java",
			"package does.not;\n" + // force this package to exist
			"public interface Work {}",
			"p/X.java",
			"package p;\n" +
			"import does.not.Work;\n" +
			"import good.WithAnnotatedMethod;\n" +
			"import does.not.ExistAnnotation;\n" + // resolving this triggers the location-less error
			"public class X {\n" +
			"	WithAnnotatedMethod field;\n" + // trigger creation of the BTB(WithAnnotatedMethod) and the URB(does.not.ExistAnnotation)
			"	void meth0(@ExistAnnotation Work d) {\n" + // force resolving of URB(does.not.ExistAnnotation)
			"	}\n" +
			"}\n"};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in p\\X.java (at line 1)\n" +
			"	package p;\n" +
			"	^\n" +
			"The type does.not.ExistAnnotation cannot be resolved. It is indirectly referenced from required type good.WithAnnotatedMethod\n" +
			"----------\n" +
			"2. ERROR in p\\X.java (at line 4)\n" +
			"	import does.not.ExistAnnotation;\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The import does.not.ExistAnnotation cannot be resolved\n" +
			"----------\n" +
			"3. ERROR in p\\X.java (at line 7)\n" +
			"	void meth0(@ExistAnnotation Work d) {\n" +
			"	            ^^^^^^^^^^^^^^^\n" +
			"ExistAnnotation cannot be resolved to a type\n" +
			"----------\n";
	runner.runNegativeTest();
}
public void testMissingClassNeededForOverloadResolution() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/B.java",
			"""
			package p1;
			public class B {
				public void m(A a) {}
				public void m(Object s) {}
				public void m(Object s1, String s2) {}
				public void other() {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));
	runner.shouldFlushOutputDirectory = false;

	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			public class C {
				void test(B b) {
					b.other(); 		// no need to see A for other()
					b.m(this, "");	// overload selected by arity
				}
			}
			"""
		};
	runner.runConformTest();

	runner.testFiles = new String[] {
			"p2/D.java",
			"""
			package p2;
			import p1.B;
			public class D {
				void test(B b) {
					b.m(this);	// cannot select without seeing class A
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
		----------
		1. ERROR in p2\\D.java (at line 5)
			b.m(this);	// cannot select without seeing class A
			  ^
		The method m(A) from the type B refers to the missing type A
		----------
		""";
	runner.runNegativeTest();
}
public void testMissingClassNeededForOverloadResolution_pickByLateArg() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/MissingType.java",
			"""
			package p1;
			public class MissingType {}
			""",
			"p1/B.java",
			"""
			package p1;
			import java.util.Date;
			public interface B {
				public void m(String s, MissingType m, Date d);
				public void m(String s, Object o, Integer i);
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "MissingType.class"));
	runner.shouldFlushOutputDirectory = false;

	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			public class C {
				void test(B b) {
					b.m("Hello", new Object(), 42); // last arg rules out the overload with MissingType
				}
			}
			"""
		};
	runner.runConformTest();
}
public void testMissingClassNeededForOverloadResolution_ctor() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/B.java",
			"""
			package p1;
			public class B {
				public B(A a) {}
				public B(Object s) {}
				public B(Object s1, String s2) {}
				public B() {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));
	runner.shouldFlushOutputDirectory = false;

	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			public class C {
				void test(B b) {
					new B(); 			// no need to see A for B()
					new B(this, "");	// overload selected by arity
				}
			}
			"""
		};
	runner.runConformTest();

	runner.testFiles = new String[] {
			"p2/D.java",
			"""
			package p2;
			import p1.B;
			public class D {
				void test(B b) {
					new B(this);	// cannot select without seeing class A
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
		----------
		1. ERROR in p2\\D.java (at line 5)
			new B(this);	// cannot select without seeing class A
			^^^^^^^^^^^
		The constructor B(A) refers to the missing type A
		----------
		""";
	runner.runNegativeTest();
}
public void testMissingClassNeededForOverloadResolution_varargs1a() {
	// varargs arg: B vs Missing
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.testFiles = new String[] {
			"p1/A1.java",
			"""
			package p1;
			public class A1 {}
			""",
			"p1/A2.java",
			"""
			package p1;
			public class A2 {}
			""",
			"p1/B.java",
			"""
			package p1;
			public class B {
				public void m(Object o1, B... s) {}
				public void m(Object o1, A1... a) {}
				public void m(Object o1, A2... a) {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary files A1, A2 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A1.class"));
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A2.class"));
	runner.shouldFlushOutputDirectory = false;

	runner.customOptions.put(CompilerOptions.OPTION_ReportVarargsArgumentNeedCast, CompilerOptions.IGNORE);
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			public class C {
				void test(B b) {
					b.m(this); // simply ambiguous as we don't even look at A1 or A2
					b.m(this, null);
					b.m(this, b);
					b.m(this, b, b);
					b.m(this, new B[0]);
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 5)
				b.m(this); // simply ambiguous as we don't even look at A1 or A2
				  ^
			The method m(Object, B[]) is ambiguous for the type B
			----------
			2. ERROR in p2\\C.java (at line 6)
				b.m(this, null);
				  ^
			The method m(Object, A1...) from the type B refers to the missing type A1
			----------
			3. ERROR in p2\\C.java (at line 7)
				b.m(this, b);
				  ^
			The method m(Object, A1...) from the type B refers to the missing type A1
			----------
			4. ERROR in p2\\C.java (at line 8)
				b.m(this, b, b);
				  ^
			The method m(Object, A1...) from the type B refers to the missing type A1
			----------
			5. ERROR in p2\\C.java (at line 9)
				b.m(this, new B[0]);
				  ^
			The method m(Object, A1...) from the type B refers to the missing type A1
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClassNeededForOverloadResolution_varargs1a_ctor() {
	// varargs arg: B vs Missing
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.testFiles = new String[] {
			"p1/A1.java",
			"""
			package p1;
			public class A1 {}
			""",
			"p1/A2.java",
			"""
			package p1;
			public class A2 {}
			""",
			"p1/B.java",
			"""
			package p1;
			public class B {
				public B(Object o1, B... s) {}
				public B(Object o1, A1... a) {}
				public B(Object o1, A2... a) {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary files A1, A2 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A1.class"));
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A2.class"));
	runner.shouldFlushOutputDirectory = false;

	runner.customOptions.put(CompilerOptions.OPTION_ReportVarargsArgumentNeedCast, CompilerOptions.IGNORE);
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			public class C {
				void test(B b) {
					new B(this); // simply ambiguous as we don't even look at A1 or A2
					new B(this, null);
					new B(this, b);
					new B(this, b, b);
					new B(this, new B[0]);
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 5)
				new B(this); // simply ambiguous as we don't even look at A1 or A2
				^^^^^^^^^^^
			The constructor B(Object, B[]) is ambiguous
			----------
			2. ERROR in p2\\C.java (at line 6)
				new B(this, null);
				^^^^^^^^^^^^^^^^^
			The constructor B(Object, A1...) refers to the missing type A1
			----------
			3. ERROR in p2\\C.java (at line 7)
				new B(this, b);
				^^^^^^^^^^^^^^
			The constructor B(Object, A1...) refers to the missing type A1
			----------
			4. ERROR in p2\\C.java (at line 8)
				new B(this, b, b);
				^^^^^^^^^^^^^^^^^
			The constructor B(Object, A1...) refers to the missing type A1
			----------
			5. ERROR in p2\\C.java (at line 9)
				new B(this, new B[0]);
				^^^^^^^^^^^^^^^^^^^^^
			The constructor B(Object, A1...) refers to the missing type A1
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClassNeededForOverloadResolution_varargs1b() {
	// like testMissingClassNeededForOverloadResolution_varargs1a, but no preceding regular parameter
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.testFiles = new String[] {
			"p1/A1.java",
			"""
			package p1;
			public class A1 {}
			""",
			"p1/A2.java",
			"""
			package p1;
			public class A2 {}
			""",
			"p1/B.java",
			"""
			package p1;
			public class B {
				public void m(String... s) {}
				public void m(A1... a) {}
				public void m(A2... a) {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary files A1, A1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A1.class"));
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A2.class"));
	runner.shouldFlushOutputDirectory = false;

	runner.customOptions.put(CompilerOptions.OPTION_ReportVarargsArgumentNeedCast, CompilerOptions.IGNORE);
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			public class C {
				void test(B b) {
					b.m(); // simply ambiguous as we don't even look at A1 or A2
					b.m(null);
					b.m(b);
					b.m(b, b);
					b.m(new B[0]);
					b.m("");			// would like to check against A1 / A2
					b.m("", "");		// would like to check against A1 / A2
					b.m(new String[0]);	// exact match
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 5)
				b.m(); // simply ambiguous as we don't even look at A1 or A2
				  ^
			The method m(String[]) is ambiguous for the type B
			----------
			2. ERROR in p2\\C.java (at line 6)
				b.m(null);
				  ^
			The method m(A1...) from the type B refers to the missing type A1
			----------
			3. ERROR in p2\\C.java (at line 7)
				b.m(b);
				  ^
			The method m(A1...) from the type B refers to the missing type A1
			----------
			4. ERROR in p2\\C.java (at line 8)
				b.m(b, b);
				  ^
			The method m(A1...) from the type B refers to the missing type A1
			----------
			5. ERROR in p2\\C.java (at line 9)
				b.m(new B[0]);
				  ^
			The method m(A1...) from the type B refers to the missing type A1
			----------
			6. ERROR in p2\\C.java (at line 10)
				b.m("");			// would like to check against A1 / A2
				  ^
			The method m(A1...) from the type B refers to the missing type A1
			----------
			7. ERROR in p2\\C.java (at line 11)
				b.m("", "");		// would like to check against A1 / A2
				  ^
			The method m(A1...) from the type B refers to the missing type A1
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClassNeededForOverloadResolution_varargs1c() {
	// varargs arg: only missing types competing
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.testFiles = new String[] {
			"p1/A1.java",
			"""
			package p1;
			public class A1 {}
			""",
			"p1/A2.java",
			"""
			package p1;
			public class A2 {}
			""",
			"p1/B.java",
			"""
			package p1;
			public class B {
				public void m(Object o1, A1... a) {}
				public void m(Object o1, A2... a) {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary files A1, A1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A1.class"));
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A2.class"));
	runner.shouldFlushOutputDirectory = false;

	runner.customOptions.put(CompilerOptions.OPTION_ReportVarargsArgumentNeedCast, CompilerOptions.IGNORE);
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			public class C {
				void test(B b) {
					b.m(this); // simply ambiguous as we don't even look at A1 or A2
					b.m(this, null);
					b.m(this, b);
					b.m(this, b, b);
					b.m(this, new B[0]);
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 5)
				b.m(this); // simply ambiguous as we don't even look at A1 or A2
				  ^
			The method m(Object, A1[]) is ambiguous for the type B
			----------
			2. ERROR in p2\\C.java (at line 6)
				b.m(this, null);
				  ^
			The method m(Object, A1...) from the type B refers to the missing type A1
			----------
			3. ERROR in p2\\C.java (at line 7)
				b.m(this, b);
				  ^
			The method m(Object, A1...) from the type B refers to the missing type A1
			----------
			4. ERROR in p2\\C.java (at line 8)
				b.m(this, b, b);
				  ^
			The method m(Object, A1...) from the type B refers to the missing type A1
			----------
			5. ERROR in p2\\C.java (at line 9)
				b.m(this, new B[0]);
				  ^
			The method m(Object, A1...) from the type B refers to the missing type A1
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClassNeededForOverloadResolution_varargs1d() {
	// like testMissingClassNeededForOverloadResolution_varargs1c, but no preceding regular parameter
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.testFiles = new String[] {
			"p1/A1.java",
			"""
			package p1;
			public class A1 {}
			""",
			"p1/A2.java",
			"""
			package p1;
			public class A2 {}
			""",
			"p1/B.java",
			"""
			package p1;
			public class B {
				public void m(A1... a) {}
				public void m(A2... a) {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary files A1, A1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A1.class"));
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A2.class"));
	runner.shouldFlushOutputDirectory = false;

	runner.customOptions.put(CompilerOptions.OPTION_ReportVarargsArgumentNeedCast, CompilerOptions.IGNORE);
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			public class C {
				void test(B b) {
					b.m(); // simply ambiguous as we don't even look at A1 or A2
					b.m(null);
					b.m(b);
					b.m(b, b);
					b.m(new B[0]);
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 5)
				b.m(); // simply ambiguous as we don't even look at A1 or A2
				  ^
			The method m(A1[]) is ambiguous for the type B
			----------
			2. ERROR in p2\\C.java (at line 6)
				b.m(null);
				  ^
			The method m(A1...) from the type B refers to the missing type A1
			----------
			3. ERROR in p2\\C.java (at line 7)
				b.m(b);
				  ^
			The method m(A1...) from the type B refers to the missing type A1
			----------
			4. ERROR in p2\\C.java (at line 8)
				b.m(b, b);
				  ^
			The method m(A1...) from the type B refers to the missing type A1
			----------
			5. ERROR in p2\\C.java (at line 9)
				b.m(new B[0]);
				  ^
			The method m(A1...) from the type B refers to the missing type A1
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClassNeededForOverloadResolution_varargs2() {
	// different arities
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/B.java",
			"""
			package p1;
			public class B {
				public void m(Object o1, String s, A... a) {}
				public void m(Object o1) {}
				public void n(Object o1, A... a) {}
				public void n(Object o1) {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));
	runner.shouldFlushOutputDirectory = false;

	runner.customOptions.put(CompilerOptions.OPTION_ReportVarargsArgumentNeedCast, CompilerOptions.IGNORE);
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			public class C {
				void test(B b) {
					b.m(this, "");			// overload selected by arity, trailing A[] is irrelevant
					b.m(this);				// overload selected by arity, no reference to A
				}
			}
			"""
		};
	runner.runConformTest();

	runner.testFiles = new String[] {
			"p2/D.java",
			"""
			package p2;
			import p1.B;
			public class D {
				void test(B b) {
					b.n(this, null);		// passing null into A[] is not OK
					b.n(this, null, null);	// passing null into A is not OK (could potentially be accepted)
					b.n(this);				// resolvable in strict mode, ignore the varargs method
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\D.java (at line 5)
				b.n(this, null);		// passing null into A[] is not OK
				  ^
			The method n(Object, A...) from the type B refers to the missing type A
			----------
			2. ERROR in p2\\D.java (at line 6)
				b.n(this, null, null);	// passing null into A is not OK (could potentially be accepted)
				  ^
			The method n(Object, A...) from the type B refers to the missing type A
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClassNeededForOverloadResolution_varargs2_ctorOK() {
	// different arities
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/B.java",
			"""
			package p1;
			public class B {
				public B(Object o1, String s, A... a) {}
				public B(Object o1) {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));
	runner.shouldFlushOutputDirectory = false;

	runner.customOptions.put(CompilerOptions.OPTION_ReportVarargsArgumentNeedCast, CompilerOptions.IGNORE);
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			public class C {
				void test() {
					new B(this, "");			// overload selected by arity, trailing A[] is irrelevant
					new B(this);				// overload selected by arity, no reference to A
				}
			}
			"""
		};
	runner.runConformTest();
}
public void testMissingClassNeededForOverloadResolution_varargs2_qualCtorOK() {
	// different arities
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/Outer.java",
			"""
			package p1;
			public class Outer {
				public class B {
					public B(Object o1, String s, A... a) {}
					public B(Object o1) {}
				}
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));
	runner.shouldFlushOutputDirectory = false;

	runner.customOptions.put(CompilerOptions.OPTION_ReportVarargsArgumentNeedCast, CompilerOptions.IGNORE);
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.Outer;
			public class C {
				void test(Outer outer) {
					outer.new B(this, "");			// overload selected by arity, trailing A[] is irrelevant
					outer.new B(this);				// overload selected by arity, no reference to A
				}
			}
			"""
		};
	runner.runConformTest();
}
public void testMissingClassNeededForOverloadResolution_varargs2_ctorNOK() {
	// different arities
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/B.java",
			"""
			package p1;
			public class B {
				public B(Object o1, A... a) {}
				public B(Object o1) {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));
	runner.shouldFlushOutputDirectory = false;

	runner.customOptions.put(CompilerOptions.OPTION_ReportVarargsArgumentNeedCast, CompilerOptions.IGNORE);

	runner.testFiles = new String[] {
			"p2/D.java",
			"""
			package p2;
			import p1.B;
			public class D {
				void test(B b) {
					new B(this, null);		// passing null into A[] is not OK
					new B(this, null, null);	// passing null into A is not OK (could potentially be accepted)
					new B(this);				// resolvable in strict mode, ignore the varargs method
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\D.java (at line 5)
				new B(this, null);		// passing null into A[] is not OK
				^^^^^^^^^^^^^^^^^
			The constructor B(Object, A...) refers to the missing type A
			----------
			2. ERROR in p2\\D.java (at line 6)
				new B(this, null, null);	// passing null into A is not OK (could potentially be accepted)
				^^^^^^^^^^^^^^^^^^^^^^^
			The constructor B(Object, A...) refers to the missing type A
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClassNeededForOverloadResolution_varargs3() {
	// missing type in non-varargs position
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/B.java",
			"""
			package p1;
			public class B {
				public void m(A a, String... args) {}
				public void m(Object s, Number... args) {}
				public void m(Object s1, String s2) {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));
	runner.shouldFlushOutputDirectory = false;

	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			public class C {
				void test(B b) {
					b.m(this, "");	// two overloads could apply (not knowing A)
					b.m(this, 3);	// overload effectively selected by 2nd arg
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 5)
				b.m(this, "");	// two overloads could apply (not knowing A)
				  ^
			The method m(A, String...) from the type B refers to the missing type A
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClassNeededForOverloadResolution_varargs3_ctor() {
	// missing type in non-varargs position
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/B.java",
			"""
			package p1;
			public class B {
				public B(A a, String... args) {}
				public B(Object s, Number... args) {}
				public B(Object s1, String s2) {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));
	runner.shouldFlushOutputDirectory = false;

	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			public class C {
				void test(B b) {
					new B(this, "");	// two overloads could apply (not knowing A)
					new B(this, 3);		// overload effectively selected by 2nd arg
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 5)
				new B(this, "");	// two overloads could apply (not knowing A)
				^^^^^^^^^^^^^^^
			The constructor B(A, String...) refers to the missing type A
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClass_varargs4_noArg() {
	// missing type in non-varargs position
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/B.java",
			"""
			package p1;
			public class B {
				public B(A... a) {}
				public void m(A... a) {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));
	runner.shouldFlushOutputDirectory = false;

	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			public class C {
				void test(B b) {
					new B();
					b.m();
				}
			}
			"""
		};
	runner.runConformTest();
}
public void testMissingClass_returnType_OK() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {
				public A() { System.out.print("new A()"); }
			}
			""",
			"p1/B.java",
			"""
			package p1;
			public class B {
				public A m(Object o) {
					System.out.print("B.m()");
					return new A();
				}
			}
			"""
		};
	runner.runConformTest();

	// temporarily move A.class to A.moved (i.e. simulate removing it from classpath for the next compile)
	File classFileA = new File(OUTPUT_DIR, "p1" + File.separator + "A.class");
	File movedFileA = new File(OUTPUT_DIR, "p1" + File.separator + "A.moved");
	classFileA.renameTo(movedFileA);
	runner.shouldFlushOutputDirectory = false;

	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			public class C extends B {
				void test(B b) {
					b.m(this);
				}
			}
			"""
		};
	runner.expectedErrorString = "java.lang.NoClassDefFoundError: p1/A\n";
	runner.runConformTest();

	runner.testFiles = new String[] {
			"p2/Main.java",
			"""
			package p2;
			import p1.B;
			public class Main {
				public static void main(String... args) {
					new C().test(new B());
					System.out.print("SUCCESS");
				}
			}
			"""
		};
	// restore A.class and expect execution of Main to succeed
	movedFileA.renameTo(classFileA);
	runner.expectedOutputString = "B.m()new A()SUCCESS";
	runner.shouldFlushOutputDirectory = false;
	runner.expectedErrorString = null;
	runner.runConformTest();
}
public void testMissingClass_returnType_NOK() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/B.java",
			"""
			package p1;
			public class B {
				public A m(Object o) { return new A(); }
				public void n(A a) {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));

	runner.shouldFlushOutputDirectory = false;

	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			import java.util.function.Function;
			public class C extends B {
				void test(B b) {
					B b2 = b.m(this);
					b.n(b.m(this));
					boolean f = b.m(this) == b;
					f = b.m(this) instanceof B;
					Function<?,?> fun = b.m(this)::foo;
					Object o = b.m(this).new Inner();
				}
				@Override
				public B m(Object o) { return super.m(o); }
				class Inner {}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 6)
				B b2 = b.m(this);
				         ^
			The method m(Object) from the type B refers to the missing type A
			----------
			2. ERROR in p2\\C.java (at line 7)
				b.n(b.m(this));
				      ^
			The method m(Object) from the type B refers to the missing type A
			----------
			3. ERROR in p2\\C.java (at line 8)
				boolean f = b.m(this) == b;
				              ^
			The method m(Object) from the type B refers to the missing type A
			----------
			4. ERROR in p2\\C.java (at line 9)
				f = b.m(this) instanceof B;
				      ^
			The method m(Object) from the type B refers to the missing type A
			----------
			5. ERROR in p2\\C.java (at line 10)
				Function<?,?> fun = b.m(this)::foo;
				                      ^
			The method m(Object) from the type B refers to the missing type A
			----------
			7. ERROR in p2\\C.java (at line 11)
				Object o = b.m(this).new Inner();
				             ^
			The method m(Object) from the type B refers to the missing type A
			----------
			8. ERROR in p2\\C.java (at line 14)
				public B m(Object o) { return super.m(o); }
				       ^
			The return type is incompatible with B.m(Object)
			----------
			9. ERROR in p2\\C.java (at line 14)
				public B m(Object o) { return super.m(o); }
				                                    ^
			The method m(Object) from the type B refers to the missing type A
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClass_exception() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A extends Exception {}
			""",
			"p1/B.java",
			"""
			package p1;
			import p1.A;
			public class B {
				public void m() throws A {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));

	runner.shouldFlushOutputDirectory = false;
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			class C {
				void test(B b) {
					b.m();
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 5)
				b.m();
				  ^
			The method m() from the type B refers to the missing type A
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClass_exception_ctor() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A extends Exception {}
			""",
			"p1/B.java",
			"""
			package p1;
			import p1.A;
			public class B {
				public B() throws A {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));

	runner.shouldFlushOutputDirectory = false;
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			class C {
				void test() {
					new B();
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 5)
				new B();
				^^^^^^^
			The constructor B() refers to the missing type A
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClass_typeVariableBound() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/B.java",
			"""
			package p1;
			import p1.A;
			public class B {
				public void m(Number n) {} 			// would match, but ...
				public <T extends A> void m(T t) {}	// ... don't rule out method with missing type
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));

	runner.shouldFlushOutputDirectory = false;
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			class C {
				void test(B b) {
					b.m(Integer.valueOf(13));
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 5)
				b.m(Integer.valueOf(13));
				  ^
			The method m(T) from the type B refers to the missing type A
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClass_typeVariableBound_OK() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/B.java",
			"""
			package p1;
			import p1.A;
			public class B {
				public B(Number n) {} 			// would match, but ...
				public <T extends A> B(T t) {}	// ... don't rule out method with missing type
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));

	runner.shouldFlushOutputDirectory = false;
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			class C {
				void test(B b) {
					new B(Integer.valueOf(13));
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 5)
				new B(Integer.valueOf(13));
				^^^^^^^^^^^^^^^^^^^^^^^^^^
			The constructor B(T) refers to the missing type A
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClass_typeVariableBound2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/B.java",
			"""
			package p1;
			import p1.A;
			public class B<T extends A> {
				public void m(T t) {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));

	runner.shouldFlushOutputDirectory = false;
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			class C {
				void test(B<C> b) {
					b.m(this);
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 1)
				package p2;
				^
			The type p1.A cannot be resolved. It is indirectly referenced from required type p1.B
			----------
			2. ERROR in p2\\C.java (at line 4)
				void test(B<C> b) {
				            ^
			Bound mismatch: The type C is not a valid substitute for the bounded parameter <T extends A> of the type B<T>
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClass_typeVariableBound2_ctor() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/B.java",
			"""
			package p1;
			import p1.A;
			public class B<T extends A> {
				public B(T t) {}
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));

	runner.shouldFlushOutputDirectory = false;
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.B;
			class C {
				B<C> test() {
					new B<>(this);
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 1)
				package p2;
				^
			The type p1.A cannot be resolved. It is indirectly referenced from required type p1.B
			----------
			2. ERROR in p2\\C.java (at line 4)
				B<C> test() {
				  ^
			Bound mismatch: The type C is not a valid substitute for the bounded parameter <T extends A> of the type B<T>
			----------
			3. ERROR in p2\\C.java (at line 5)
				new B<>(this);
				^^^^^^^^^^^^^
			Cannot infer type arguments for B<>
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClass_samMissingParameterType_OK() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/F.java",
			"""
			package p1;
			import p1.A;
			public interface F {
				public void m(A a);
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));

	runner.shouldFlushOutputDirectory = false;
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.F;
			class C {
				F test() {
					return a -> {};
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 5)
				return a -> {};
				       ^^^^
			This lambda expression refers to the missing type A
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClass_samMissingParameterType_NOK() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/F.java",
			"""
			package p1;
			import p1.A;
			public interface F {
				public void m(A a);
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));

	runner.shouldFlushOutputDirectory = false;
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.F;
			class C {
				F test() {
					return a -> bar(a);
				}
				void bar(C c) {}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 5)
				return a -> bar(a);
				       ^^^^^^^^^^^
			This lambda expression refers to the missing type A
			----------
			""";
	runner.runNegativeTest();
}
public void testMissingClass_samMissingReturnType() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // ignore different outcome below 1.8 since PR 2543
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"p1/A.java",
			"""
			package p1;
			public class A {}
			""",
			"p1/F.java",
			"""
			package p1;
			import p1.A;
			public interface F {
				public A m();
			}
			"""
		};
	runner.runConformTest();

	// delete binary file A (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p1" + File.separator + "A.class"));

	runner.shouldFlushOutputDirectory = false;
	runner.testFiles = new String[] {
			"p2/C.java",
			"""
			package p2;
			import p1.F;
			class C {
				F test() {
					return () -> null;
				}
			}
			"""
		};
	runner.expectedCompilerLog = """
			----------
			1. ERROR in p2\\C.java (at line 5)
				return () -> null;
				       ^^^^^^^^^^
			This lambda expression refers to the missing type A
			----------
			""";
	runner.runNegativeTest();
}
public void testGH3047() throws Exception {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"resources/examples/mockito/MockingFromFinder.java",
			"""
			package examples.mockito;
			public class MockingFromFinder{}
			""",
			"resources/examples/mockito/MockingWhileAdding.java",
			"""
			package examples.mockito;
			public class MockingWhileAdding {
				public static void calculateWithAdder(int x, int y) {
					IOperation adder = new Adder()::execute;
				}
				public interface IOperation {
					int execute(int x, int y);
				}
				public static class Adder implements IOperation {
					public int execute(int x, int y) {
						return x+y;
					}
				}
			}
			"""
		};
	runner.classLibraries = new String[0];
	if (this.complianceLevel <= ClassFileConstants.JDK13) {
		runner.expectedCompilerLog =
			"""
			----------
			1. ERROR in resources\\examples\\mockito\\MockingFromFinder.java (at line 1)
				package examples.mockito;
				^
			The type java.lang.Object cannot be resolved. It is indirectly referenced from required .class files
			----------
			2. ERROR in resources\\examples\\mockito\\MockingFromFinder.java (at line 2)
				public class MockingFromFinder{}
				             ^^^^^^^^^^^^^^^^^
			Implicit super constructor Object() is undefined for default constructor. Must define an explicit constructor
			----------
			----------
			1. ERROR in resources\\examples\\mockito\\MockingWhileAdding.java (at line 1)
				package examples.mockito;
				^
			The type java.lang.Object cannot be resolved. It is indirectly referenced from required .class files
			----------
			2. ERROR in resources\\examples\\mockito\\MockingWhileAdding.java (at line 2)
				public class MockingWhileAdding {
				             ^^^^^^^^^^^^^^^^^^
			Implicit super constructor Object() is undefined for default constructor. Must define an explicit constructor
			----------
			3. ERROR in resources\\examples\\mockito\\MockingWhileAdding.java (at line 9)
				public static class Adder implements IOperation {
				                    ^^^^^
			Implicit super constructor Object() is undefined for default constructor. Must define an explicit constructor
			----------
			""";
	} else {
		runner.expectedCompilerLog =
			"""
			----------
			1. ERROR in resources\\examples\\mockito\\MockingFromFinder.java (at line 1)
				package examples.mockito;
				^
			The type java.lang.Object cannot be resolved. It is indirectly referenced from required .class files
			----------
			2. ERROR in resources\\examples\\mockito\\MockingFromFinder.java (at line 1)
				package examples.mockito;
				^
			The type java.lang.Error cannot be resolved. It is indirectly referenced from required .class files
			----------
			3. ERROR in resources\\examples\\mockito\\MockingFromFinder.java (at line 1)
				package examples.mockito;
				^
			The type java.lang.String cannot be resolved. It is indirectly referenced from required .class files
			----------
			4. ERROR in resources\\examples\\mockito\\MockingFromFinder.java (at line 2)
				public class MockingFromFinder{}
				             ^^^^^^^^^^^^^^^^^
			Implicit super constructor Object() is undefined for default constructor. Must define an explicit constructor
			----------
			----------
			1. ERROR in resources\\examples\\mockito\\MockingWhileAdding.java (at line 1)
				package examples.mockito;
				^
			The type java.lang.Object cannot be resolved. It is indirectly referenced from required .class files
			----------
			2. ERROR in resources\\examples\\mockito\\MockingWhileAdding.java (at line 2)
				public class MockingWhileAdding {
				             ^^^^^^^^^^^^^^^^^^
			Implicit super constructor Object() is undefined for default constructor. Must define an explicit constructor
			----------
			3. ERROR in resources\\examples\\mockito\\MockingWhileAdding.java (at line 9)
				public static class Adder implements IOperation {
				                    ^^^^^
			Implicit super constructor Object() is undefined for default constructor. Must define an explicit constructor
			----------
			""";
	}
	runner.runNegativeTest();
}
}
