/*******************************************************************************
 * Copyright (c) 2010, 2017 IBM Corporation and others.
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

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class InnerClass15Test extends AbstractRegressionTest {
public InnerClass15Test(String name) {
	super(name);
}
static {
//	TESTS_NUMBERS = new int[] { 2 };
	//TESTS_NAMES = new String[] {"testBug520874"};
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), FIRST_SUPPORTED_JAVA_VERSION);
}
@Override
protected Map<String, String> getCompilerOptions() {
	Map<String, String> options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	return options;
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312989
public void test001() {
	this.runNegativeTest(new String[] {
		"X.java",
		"class X {\n" +
		"	<X> void foo() {\n" +
		"		class X {}\n" +
		"	}\n" +
		"}",
	},
	"----------\n" +
	"1. WARNING in X.java (at line 2)\n" +
	"	<X> void foo() {\n" +
	"	 ^\n" +
	"The type parameter X is hiding the type X\n" +
	"----------\n" +
	"2. WARNING in X.java (at line 3)\n" +
	"	class X {}\n" +
	"	      ^\n" +
	"The nested type X is hiding the type parameter X of the generic method foo() of type X\n" +
	"----------\n" +
	"3. ERROR in X.java (at line 3)\n" +
	"	class X {}\n" +
	"	      ^\n" +
	"The nested type X cannot hide an enclosing type\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312989
public void test002() {
	this.runNegativeTest(new String[] {
		"X.java",
		"class X<X> {\n" +
		"	void foo() {\n" +
		"		class X {}\n" +
		"	}\n" +
		"}",
	},
	"----------\n" +
	"1. WARNING in X.java (at line 1)\n" +
	"	class X<X> {\n" +
	"	        ^\n" +
	"The type parameter X is hiding the type X<X>\n" +
	"----------\n" +
	"2. WARNING in X.java (at line 3)\n" +
	"	class X {}\n" +
	"	      ^\n" +
	"The nested type X is hiding the type parameter X of type X<X>\n" +
	"----------\n" +
	"3. ERROR in X.java (at line 3)\n" +
	"	class X {}\n" +
	"	      ^\n" +
	"The nested type X cannot hide an enclosing type\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312989
// note javac reports an error for this test, but that is
// incorrect, compare and contrast javac behavior with
// test004.
public void test003() {
	this.runNegativeTest(new String[] {
		"Y.java",
		"class Y {\n" +
		"class X {}\n" +
		"	<X> void foo() {\n" +
		"		class X {}\n" +
		"	}\n" +
		"}",
	},
	"----------\n" +
	"1. WARNING in Y.java (at line 3)\n" +
	"	<X> void foo() {\n" +
	"	 ^\n" +
	"The type parameter X is hiding the type Y.X\n" +
	"----------\n" +
	"2. WARNING in Y.java (at line 4)\n" +
	"	class X {}\n" +
	"	      ^\n" +
	"The nested type X is hiding the type parameter X of the generic method foo() of type Y\n" +
	"----------\n" +
	"3. WARNING in Y.java (at line 4)\n" +
	"	class X {}\n" +
	"	      ^\n" +
	"The type X is never used locally\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312989
public void test004() {
	this.runNegativeTest(new String[] {
		"Y.java",
		"class Y {\n" +
		"class X {}\n" +
		"   void foo() {\n" +
		"		class X {}\n" +
		"	}\n" +
		"}",
	},
	"----------\n" +
	"1. WARNING in Y.java (at line 4)\n" +
	"	class X {}\n" +
	"	      ^\n" +
	"The type X is hiding the type Y.X\n" +
	"----------\n" +
	"2. WARNING in Y.java (at line 4)\n" +
	"	class X {}\n" +
	"	      ^\n" +
	"The type X is never used locally\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test005() {
	this.runNegativeTest(new String[] {
		"p1/GreenBox.java",
		"package p1;\n" +
		"import static p1.BrownBox.*;\n" +
		"public interface GreenBox {\n" +
		"    public static class Cat extends Object {}\n" +
		"}\n",
		"p1/BrownBox.java",
		"package p1;\n" +
		"import static p1.GreenBox.*;\n" +
		"public interface BrownBox {\n" +
		"    public static class BlackCat extends Cat {}\n" +
		"}\n",
	},
	"----------\n" +
	"1. WARNING in p1\\GreenBox.java (at line 2)\n" +
	"	import static p1.BrownBox.*;\n" +
	"	              ^^^^^^^^^^^\n" +
	"The import p1.BrownBox is never used\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test006() {
	this.runNegativeTest(new String[] {
		"p1/BrownBox.java",
		"package p1;\n" +
		"import static p1.GreenBox.*;\n" +
		"public interface BrownBox {\n" +
		"    public static class BlackCat extends Cat {}\n" +
		"}\n",
		"p1/GreenBox.java",
		"package p1;\n" +
		"import static p1.BrownBox.*;\n" +
		"public interface GreenBox {\n" +
		"    public static class Cat extends Object {}\n" +
		"}\n",
	},
	"----------\n" +
	"1. WARNING in p1\\GreenBox.java (at line 2)\n" +
	"	import static p1.BrownBox.*;\n" +
	"	              ^^^^^^^^^^^\n" +
	"The import p1.BrownBox is never used\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test007() {
	this.runNegativeTest(new String[] {
		"p1/BrownBox.java",
		"package p1;\n" +
		"import static p1.GreenBox.*;\n" +
		"public interface BrownBox {\n" +
		"    public static class BlackCat extends Cat {}\n" +
		"}\n",
		"p1/GreenBox.java",
		"package p1;\n" +
		"import static p1.BrownBox.*;\n" +
		"public interface GreenBox {\n" +
		"    public static class Cat extends java.lang.Object {}\n" +
		"}\n",
	},
	"----------\n" +
	"1. WARNING in p1\\GreenBox.java (at line 2)\n" +
	"	import static p1.BrownBox.*;\n" +
	"	              ^^^^^^^^^^^\n" +
	"The import p1.BrownBox is never used\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test008() {
	this.runNegativeTest(new String[] {
		"p1/BrownBox.java",
		"package p1;\n" +
		"import static p1.GreenBox.*;\n" +
		"public interface BrownBox {\n" +
		"    public static class BlackCat extends Cat {}\n" +
		"}\n",
		"p1/GreenBox.java",
		"package p1;\n" +
		"import static p1.BrownBox.*;\n" +
		"public interface GreenBox {\n" +
		"    public static class Cat extends BlackCat {}\n" +
		"}\n",
	},
	"----------\n" +
	"1. ERROR in p1\\BrownBox.java (at line 4)\n" +
	"	public static class BlackCat extends Cat {}\n" +
	"	                    ^^^^^^^^\n" +
	"The hierarchy of the type BlackCat is inconsistent\n" +
	"----------\n" +
	"----------\n" +
	"1. ERROR in p1\\GreenBox.java (at line 4)\n" +
	"	public static class Cat extends BlackCat {}\n" +
	"	                                ^^^^^^^^\n" +
	"Cycle detected: a cycle exists in the type hierarchy between GreenBox.Cat and BrownBox.BlackCat\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test009() {
	this.runNegativeTest(new String[] {
		"p1/GreenBox.java",
		"package p1;\n" +
		"import static p1.BrownBox.*;\n" +
		"public interface GreenBox {\n" +
		"    public static class Cat extends BlackCat {}\n" +
		"}\n",
		"p1/BrownBox.java",
		"package p1;\n" +
		"import static p1.GreenBox.*;\n" +
		"public interface BrownBox {\n" +
		"    public static class BlackCat extends Cat {}\n" +
		"}\n",
	},
	"----------\n" +
	"1. ERROR in p1\\GreenBox.java (at line 4)\n" +
	"	public static class Cat extends BlackCat {}\n" +
	"	                    ^^^\n" +
	"The hierarchy of the type Cat is inconsistent\n" +
	"----------\n" +
	"----------\n" +
	"1. ERROR in p1\\BrownBox.java (at line 4)\n" +
	"	public static class BlackCat extends Cat {}\n" +
	"	                                     ^^^\n" +
	"Cycle detected: a cycle exists in the type hierarchy between BrownBox.BlackCat and GreenBox.Cat\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test0010() {
	this.runNegativeTest(new String[] {
		"p1/GreenBox.java",
		"package p1;\n" +
		"import static p1.BrownBox.*;\n" +
		"interface SuperInterface {\n" +
		"   public static class Cat extends BlackCat {}\n" +
		"}\n" +
		"public interface GreenBox {\n" +
		"}\n",
		"p1/BrownBox.java",
		"package p1;\n" +
		"import static p1.GreenBox.*;\n" +
		"public interface BrownBox {\n" +
		"    public static class BlackCat extends Cat {}\n" +
		"}\n",
	},
	"----------\n" +
	"1. ERROR in p1\\GreenBox.java (at line 4)\n" +
	"	public static class Cat extends BlackCat {}\n" +
	"	                    ^^^\n" +
	"The hierarchy of the type Cat is inconsistent\n" +
	"----------\n" +
	"----------\n" +
	"1. ERROR in p1\\BrownBox.java (at line 4)\n" +
	"	public static class BlackCat extends Cat {}\n" +
	"	                                     ^^^\n" +
	"Cat cannot be resolved to a type\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test0011() {
	this.runNegativeTest(new String[] {
		"p1/GreenBox.java",
		"package p1;\n" +
		"import static p1.BrownBox.*;\n" +
		"interface SuperInterface {\n" +
		"   public static class Cat extends BlackCat {}\n" +
		"}\n" +
		"public interface GreenBox extends SuperInterface {\n" +
		"}\n",
		"p1/BrownBox.java",
		"package p1;\n" +
		"import static p1.GreenBox.*;\n" +
		"public interface BrownBox {\n" +
		"    public static class BlackCat extends Cat {}\n" +
		"}\n",
	},
	"----------\n" +
	"1. ERROR in p1\\GreenBox.java (at line 4)\n" +
	"	public static class Cat extends BlackCat {}\n" +
	"	                    ^^^\n" +
	"The hierarchy of the type Cat is inconsistent\n" +
	"----------\n" +
	"----------\n" +
	"1. ERROR in p1\\BrownBox.java (at line 4)\n" +
	"	public static class BlackCat extends Cat {}\n" +
	"	                                     ^^^\n" +
	"Cycle detected: a cycle exists in the type hierarchy between BrownBox.BlackCat and SuperInterface.Cat\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test0012() {
	this.runNegativeTest(new String[] {
		"p1/GreenBox.java",
		"package p1;\n" +
		"import static p1.BrownBox.*;\n" +
		"interface SuperInterface {\n" +
		"   public static class Cat extends BlackCat {}\n" +
		"}\n" +
		"public interface GreenBox extends SuperInterface {\n" +
		"}\n",
		"p1/BrownBox.java",
		"package p1;\n" +
		"import static p1.GreenBox.*;\n" +
		"public interface BrownBox {\n" +
		"    public static class BlackCat extends GreenBox.Cat {}\n" +
		"}\n",
	},
	"----------\n" +
	"1. ERROR in p1\\GreenBox.java (at line 4)\n" +
	"	public static class Cat extends BlackCat {}\n" +
	"	                    ^^^\n" +
	"The hierarchy of the type Cat is inconsistent\n" +
	"----------\n" +
	"----------\n" +
	"1. ERROR in p1\\BrownBox.java (at line 4)\n" +
	"	public static class BlackCat extends GreenBox.Cat {}\n" +
	"	                                     ^^^^^^^^^^^^\n" +
	"Cycle detected: a cycle exists in the type hierarchy between BrownBox.BlackCat and SuperInterface.Cat\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test0013() {
	this.runNegativeTest(new String[] {
		"cycle/X.java",
		"package cycle;\n" +
		"class X extends Y {}\n" +
		"class Y extends X {}\n",
	},
	"----------\n" +
	"1. ERROR in cycle\\X.java (at line 2)\n" +
	"	class X extends Y {}\n" +
	"	      ^\n" +
	"The hierarchy of the type X is inconsistent\n" +
	"----------\n" +
	"2. ERROR in cycle\\X.java (at line 3)\n" +
	"	class Y extends X {}\n" +
	"	                ^\n" +
	"Cycle detected: a cycle exists in the type hierarchy between Y and X\n" +
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test0014() {
	this.runNegativeTest(new String[] {
		"cycle/X.java",
		"package cycle;\n" +
		"class X extends Y {}\n" +
		"class Y extends Z {}\n" +
		"class Z extends A {}\n" +
		"class A extends B {}\n" +
		"class B extends C {}\n" +
		"class C extends X {}\n"
	},
	"----------\n" +
	"1. ERROR in cycle\\X.java (at line 2)\n" +
	"	class X extends Y {}\n" +
	"	      ^\n" +
	"The hierarchy of the type X is inconsistent\n" +
	"----------\n" +
	"2. ERROR in cycle\\X.java (at line 3)\n" +
	"	class Y extends Z {}\n" +
	"	      ^\n" +
	"The hierarchy of the type Y is inconsistent\n" +
	"----------\n" +
	"3. ERROR in cycle\\X.java (at line 4)\n" +
	"	class Z extends A {}\n" +
	"	      ^\n" +
	"The hierarchy of the type Z is inconsistent\n" +
	"----------\n" +
	"4. ERROR in cycle\\X.java (at line 5)\n" +
	"	class A extends B {}\n" +
	"	      ^\n" +
	"The hierarchy of the type A is inconsistent\n" +
	"----------\n" +
	"5. ERROR in cycle\\X.java (at line 6)\n" +
	"	class B extends C {}\n" +
	"	      ^\n" +
	"The hierarchy of the type B is inconsistent\n" +
	"----------\n" +
	"6. ERROR in cycle\\X.java (at line 7)\n" +
	"	class C extends X {}\n" +
	"	                ^\n" +
	"Cycle detected: a cycle exists in the type hierarchy between C and X\n" +
	"----------\n");
}
public void testBug520874a() {
	if (this.complianceLevel < ClassFileConstants.JDK9)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/A.java",
			"package p;\n" +
			"class A extends C {\n" +
			"    static class B {}\n" +
			"}\n",
			"cycle/X.java",
			"package p;\n" +
			"import p.A.B;\n" +
			"class C extends B {}\n" +
			"public class X {\n" +
			"    public static void main(String argv[]) {\n" +
			"      new C();\n" +
			"    }\n" +
			"}\n",
		},
			"----------\n" +
			"1. ERROR in cycle\\A.java (at line 2)\n" +
			"	class A extends C {\n" +
			"	      ^\n" +
			"The hierarchy of the type A is inconsistent\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in cycle\\X.java (at line 3)\n" +
			"	class C extends B {}\n" +
			"	                ^\n" +
			"Cycle detected: a cycle exists in the type hierarchy between C and A\n" +
			"----------\n");
}
public void testBug520874b() {
	if (this.complianceLevel < ClassFileConstants.JDK9)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"package p;\n" +
			"import p.A.*;\n" +
			"class C extends B {}\n" +
			"public class X {\n" +
			"    public static void main(String argv[]) {\n" +
			"      new C();\n" +
			"    }\n" +
			"}\n",
			"cycle/A.java",
			"package p;\n" +
			"class A extends C {\n" +
			"    static class B {}\n" +
			"}\n"
		},
			"----------\n" +
			"1. ERROR in cycle\\X.java (at line 3)\n" +
			"	class C extends B {}\n" +
			"	      ^\n" +
			"The hierarchy of the type C is inconsistent\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in cycle\\A.java (at line 2)\n" +
			"	class A extends C {\n" +
			"	                ^\n" +
			"Cycle detected: a cycle exists in the type hierarchy between A and C\n" +
			"----------\n");
}
public void testBug520874c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"package cycle;\n" +
			"import cycle.A.B;\n" +
			"class C implements B {}\n" +
			"public class X {\n" +
			"    public static void main(String argv[]) {\n" +
			"      new C();\n" +
			"    }\n" +
			"}\n",
			"cycle/A.java",
			"package cycle;\n" +
			"class A extends C {\n" +
			"    static interface B {}\n" +
			"}\n"
		},
			"----------\n" +
			"1. ERROR in cycle\\X.java (at line 3)\n" +
			"	class C implements B {}\n" +
			"	      ^\n" +
			"The hierarchy of the type C is inconsistent\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in cycle\\A.java (at line 2)\n" +
			"	class A extends C {\n" +
			"	                ^\n" +
			"Cycle detected: a cycle exists in the type hierarchy between A and C\n" +
			"----------\n");
}
public void testBug520874d() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"package cycle;\n" +
			"import cycle.A.*;\n" +
			"class C implements B {}\n" +
			"public class X {\n" +
			"    public static void main(String argv[]) {\n" +
			"      new C();\n" +
			"    }\n" +
			"}\n",
			"cycle/A.java",
			"package cycle;\n" +
			"class A extends C {\n" +
			"    static interface B {}\n" +
			"}\n"
		},
			"----------\n" +
			"1. ERROR in cycle\\X.java (at line 3)\n" +
			"	class C implements B {}\n" +
			"	      ^\n" +
			"The hierarchy of the type C is inconsistent\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in cycle\\A.java (at line 2)\n" +
			"	class A extends C {\n" +
			"	                ^\n" +
			"Cycle detected: a cycle exists in the type hierarchy between A and C\n" +
			"----------\n");
}
public void testBug520874e() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"package cycle;\n" +
			"import cycle.A.B;\n" +
			"interface C extends B {}\n" +
			"public class X {\n" +
			"    public static void main(String argv[]) {\n" +
			"    }\n" +
			"}\n",
			"cycle/A.java",
			"package cycle;\n" +
			"class A extends C {\n" +
			"    static interface B {}\n" +
			"}\n"
		},
			"----------\n" +
			"1. ERROR in cycle\\X.java (at line 3)\n" +
			"	interface C extends B {}\n" +
			"	          ^\n" +
			"The hierarchy of the type C is inconsistent\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in cycle\\A.java (at line 2)\n" +
			"	class A extends C {\n" +
			"	                ^\n" +
			"Cycle detected: a cycle exists in the type hierarchy between A and C\n" +
			"----------\n");
}
public void testBug520874f() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"package cycle;\n" +
			"import cycle.A.*;\n" +
			"interface C extends B {}\n" +
			"public class X {\n" +
			"    public static void main(String argv[]) {\n" +
			"    }\n" +
			"}\n",
			"cycle/A.java",
			"package cycle;\n" +
			"class A extends C {\n" +
			"    static interface B {}\n" +
			"}\n"
		},
			"----------\n" +
			"1. ERROR in cycle\\X.java (at line 3)\n" +
			"	interface C extends B {}\n" +
			"	          ^\n" +
			"The hierarchy of the type C is inconsistent\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in cycle\\A.java (at line 2)\n" +
			"	class A extends C {\n" +
			"	                ^\n" +
			"Cycle detected: a cycle exists in the type hierarchy between A and C\n" +
			"----------\n");
}
public void testBug520874g() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"package cycle;\n" +
			"import cycle.A.B;\n" +
			"interface C extends B {}\n" +
			"public class X {\n" +
			"    public static void main(String argv[]) {\n" +
			"    }\n" +
			"}\n",
			"cycle/A.java",
			"package cycle;\n" +
			"interface A extends C {\n" +
			"    static interface B {}\n" +
			"}\n"
		},
			"----------\n" +
			"1. ERROR in cycle\\X.java (at line 3)\n" +
			"	interface C extends B {}\n" +
			"	          ^\n" +
			"The hierarchy of the type C is inconsistent\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in cycle\\A.java (at line 2)\n" +
			"	interface A extends C {\n" +
			"	                    ^\n" +
			"Cycle detected: a cycle exists in the type hierarchy between A and C\n" +
			"----------\n");
}
public void testBug520874h() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"package cycle;\n" +
			"import cycle.A.*;\n" +
			"interface C extends B {}\n" +
			"public class X {\n" +
			"    public static void main(String argv[]) {\n" +
			"    }\n" +
			"}\n",
			"cycle/A.java",
			"package cycle;\n" +
			"interface A extends C {\n" +
			"    static interface B {}\n" +
			"}\n"
		},
			"----------\n" +
			"1. ERROR in cycle\\X.java (at line 3)\n" +
			"	interface C extends B {}\n" +
			"	          ^\n" +
			"The hierarchy of the type C is inconsistent\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in cycle\\A.java (at line 2)\n" +
			"	interface A extends C {\n" +
			"	                    ^\n" +
			"Cycle detected: a cycle exists in the type hierarchy between A and C\n" +
			"----------\n");
}
public void testBug520874i() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"package cycle;\n" +
			"import cycle.A.*;\n" +
			"interface C extends A {}\n" +
			"public class X {\n" +
			"    public static void main(String argv[]) {\n" +
			"    }\n" +
			"}\n",
			"cycle/A.java",
			"package cycle;\n" +
			"interface A extends C {\n" +
			"    static interface B {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in cycle\\X.java (at line 3)\n" +
		"	interface C extends A {}\n" +
		"	          ^\n" +
		"The hierarchy of the type C is inconsistent\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in cycle\\A.java (at line 2)\n" +
		"	interface A extends C {\n" +
		"	                    ^\n" +
		"Cycle detected: a cycle exists in the type hierarchy between A and C\n" +
		"----------\n");
}
public void testBug526681() {
	runNegativeTest(
		new String[] {
			"p/A.java",
			"package p;\n" +
			"import p.B;\n" +
			"public class A extends B {\n" +
			"	public static abstract class C {}\n" +
			"}\n",
			"p/B.java",
			"package p;\n" +
			"import p.A.C;\n" +
			"public abstract class B extends C {}"
		},
		"----------\n" +
		"1. ERROR in p\\A.java (at line 3)\n" +
		"	public class A extends B {\n" +
		"	             ^\n" +
		"The hierarchy of the type A is inconsistent\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in p\\B.java (at line 3)\n" +
		"	public abstract class B extends C {}\n" +
		"	                                ^\n" +
		"Cycle detected: a cycle exists in the type hierarchy between B and A\n" +
		"----------\n");
}
public void testBug527731() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses diamond, 1.7-inference fails, only 1.8 is good
	runConformTest(
		new String[] {
			"OuterClass.java",
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class OuterClass<T> extends ArrayList<OuterClass.InnerTypedClass<T>> {\n" +
			"	\n" +
			"	public static interface InnerInterface {}\n" +
			"	\n" +
			"	public static class InnerTypedClass<T> implements InnerInterface {}\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		OuterClass<String> outerClass = new OuterClass<>();\n" +
			"		outerClass.add(new InnerTypedClass<>());\n" +
			"		System.out.println(outerClass);\n" +
			"	}\n" +
			"}\n"
		});
}
public static Class<InnerClass15Test> testClass() {
	return InnerClass15Test.class;
}
}
