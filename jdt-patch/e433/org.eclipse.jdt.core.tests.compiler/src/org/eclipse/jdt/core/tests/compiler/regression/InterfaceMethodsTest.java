/*******************************************************************************
 * Copyright (c) 2013, 2021 GK Software AG, IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *     Jesper S Moller - realigned with bug 399695
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

// See https://bugs.eclipse.org/380501
// Bug 380501 - [1.8][compiler] Add support for default methods (JSR 335)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class InterfaceMethodsTest extends AbstractComparableTest {

// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
	static {
//			TESTS_NAMES = new String[] { "testBug421543" };
//			TESTS_NUMBERS = new int[] { 561 };
//			TESTS_RANGE = new int[] { 1, 2049 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public static Test setUpTest(Test test) throws Exception {
		TestCase.setUpTest(test);
		RegressionTestSetup suite = new RegressionTestSetup(ClassFileConstants.JDK1_8);
		suite.addTest(test);
		return suite;
	}

	public static Class testClass() {
		return InterfaceMethodsTest.class;
	}

	public InterfaceMethodsTest(String name) {
		super(name);
	}

	// default methods with various modifiers, positive cases
	public void testModifiers1() {
		runConformTest(
		new String[] {
			"I.java",
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.METHOD) @interface Annot{}\n" +
			"public interface I {\n" +
			"    default void foo1()  {}\n" +
			"    public default void foo2() { System.exit(0); }\n" +
			"    strictfp default void foo3() {}\n" +
			"    public default strictfp void foo4() {}\n" +
			"    public default strictfp @Annot void foo5() {}\n" +
			"}\n",
		},
		"");
	}


	// default methods with various modifiers, negative cases
	public void testModifiers1a() {
		String infMod = this.complianceLevel >= ClassFileConstants.JDK9 ? " private," : "";
		String op = this.complianceLevel < ClassFileConstants.JDK17 ?
		"----------\n" +
		"1. ERROR in I.java (at line 5)\n" +
		"	public default synchronized void foo2() { System.exit(0); }\n" +
		"	                                 ^^^^^^\n" +
		"Illegal modifier for the interface method foo2; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
		"----------\n" +
		"2. ERROR in I.java (at line 7)\n" +
		"	public default strictfp synchronized void foo4() {}\n" +
		"	                                          ^^^^^^\n" +
		"Illegal modifier for the interface method foo4; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
		"----------\n" +
		"3. ERROR in I.java (at line 8)\n" +
		"	public default strictfp synchronized @Annot void foo5() {}\n" +
		"	                                                 ^^^^^^\n" +
		"Illegal modifier for the interface method foo5; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
		"----------\n" :
			"----------\n" +
			"1. ERROR in I.java (at line 5)\n" +
			"	public default synchronized void foo2() { System.exit(0); }\n" +
			"	                                 ^^^^^^\n" +
			"Illegal modifier for the interface method foo2; only public, private, abstract, default, static and strictfp are permitted\n" +
			"----------\n" +
			"2. WARNING in I.java (at line 6)\n" +
			"	strictfp default void foo3() {}\n" +
			"	^^^^^^^^\n" +
			"Floating-point expressions are always strictly evaluated from source level 17. Keyword \'strictfp\' is not required.\n" +
			"----------\n" +
			"3. WARNING in I.java (at line 7)\n" +
			"	public default strictfp synchronized void foo4() {}\n" +
			"	               ^^^^^^^^\n" +
			"Floating-point expressions are always strictly evaluated from source level 17. Keyword \'strictfp\' is not required.\n" +
			"----------\n" +
			"4. ERROR in I.java (at line 7)\n" +
			"	public default strictfp synchronized void foo4() {}\n" +
			"	                                          ^^^^^^\n" +
			"Illegal modifier for the interface method foo4; only public, private, abstract, default, static and strictfp are permitted\n" +
			"----------\n" +
			"5. WARNING in I.java (at line 8)\n" +
			"	public default strictfp synchronized @Annot void foo5() {}\n" +
			"	               ^^^^^^^^\n" +
			"Floating-point expressions are always strictly evaluated from source level 17. Keyword \'strictfp\' is not required.\n" +
			"----------\n" +
			"6. ERROR in I.java (at line 8)\n" +
			"	public default strictfp synchronized @Annot void foo5() {}\n" +
			"	                                                 ^^^^^^\n" +
			"Illegal modifier for the interface method foo5; only public, private, abstract, default, static and strictfp are permitted\n" +
			"----------\n";
		runNegativeTest(
		new String[] {
			"I.java",
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.METHOD) @interface Annot{}\n" +
			"public interface I {\n" +
			"    default void foo1()  {}\n" +
			"    public default synchronized void foo2() { System.exit(0); }\n" +
			"    strictfp default void foo3() {}\n" +
			"    public default strictfp synchronized void foo4() {}\n" +
			"    public default strictfp synchronized @Annot void foo5() {}\n" +
			"}\n"},
			op);
	}

	// default methods with various modifiers, simple syntax error blows the parser
	public void testModifiers1b() {
		runNegativeTest(
		new String[] {
			"I.java",
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.METHOD) @interface Annot{}\n" +
			"public interface I {\n" +
			"    default void foo1() { System.out.println(3); }\n" +
			"    public default void foo2() {}\n" +
			"    stritfp default void foo3() {}\n" + // typo in strictfp
			"    default public strictfp void foo4() {}\n" +
			"    public strictfp  default @Annot void foo5() {}\n" +
			"    public default <T> T foo6(T t) { return t; }\n" +
			"}\n"},
			"----------\n" +
			"1. ERROR in I.java (at line 6)\n" +
			"	stritfp default void foo3() {}\n" +
			"	^^^^^^^\n" +
			"Syntax error, insert \"Identifier (\" to complete MethodHeaderName\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 6)\n" +
			"	stritfp default void foo3() {}\n" +
			"	^^^^^^^\n" +
			"Syntax error, insert \")\" to complete MethodDeclaration\n" +
			"----------\n" +
			"3. ERROR in I.java (at line 6)\n" +
			"	stritfp default void foo3() {}\n" +
			"	^^^^^^^\n" +
			"Syntax error, insert \";\" to complete MethodDeclaration\n" +
			"----------\n" +
			(this.complianceLevel >= ClassFileConstants.JDK17 ?
					"4. WARNING in I.java (at line 7)\n" +
					"	default public strictfp void foo4() {}\n" +
					"	               ^^^^^^^^\n" +
					"Floating-point expressions are always strictly evaluated from source level 17. Keyword \'strictfp\' is not required.\n" +
					"----------\n" +
					"5. WARNING in I.java (at line 8)\n" +
					"	public strictfp  default @Annot void foo5() {}\n" +
					"	       ^^^^^^^^\n" +
					"Floating-point expressions are always strictly evaluated from source level 17. Keyword \'strictfp\' is not required.\n" +
					"----------\n" : "")
			);
	}

	// regular interface with illegal modifiers
	public void testModifiers2() {
		String infMod = this.complianceLevel >= ClassFileConstants.JDK9 ? " private," : "";
		String op = this.complianceLevel < ClassFileConstants.JDK17 ?
		"----------\n" +
		"1. ERROR in I.java (at line 5)\n" +
		"	public synchronized void foo2();\n" +
		"	                         ^^^^^^\n" +
		"Illegal modifier for the interface method foo2; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
		"----------\n" +
		"2. ERROR in I.java (at line 6)\n" +
		"	strictfp void foo3();\n" +
		"	              ^^^^^^\n" +
		"strictfp is not permitted for abstract interface method foo3\n" +
		"----------\n" +
		"3. ERROR in I.java (at line 7)\n" +
		"	public strictfp synchronized void foo4();\n" +
		"	                                  ^^^^^^\n" +
		"strictfp is not permitted for abstract interface method foo4\n" +
		"----------\n" +
		"4. ERROR in I.java (at line 7)\n" +
		"	public strictfp synchronized void foo4();\n" +
		"	                                  ^^^^^^\n" +
		"Illegal modifier for the interface method foo4; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
		"----------\n" +
		"5. ERROR in I.java (at line 8)\n" +
		"	public strictfp synchronized @Annot void foo5();\n" +
		"	                                         ^^^^^^\n" +
		"strictfp is not permitted for abstract interface method foo5\n" +
		"----------\n" +
		"6. ERROR in I.java (at line 8)\n" +
		"	public strictfp synchronized @Annot void foo5();\n" +
		"	                                         ^^^^^^\n" +
		"Illegal modifier for the interface method foo5; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
		"----------\n" :
			"----------\n" +
			"1. ERROR in I.java (at line 5)\n" +
			"	public synchronized void foo2();\n" +
			"	                         ^^^^^^\n" +
			"Illegal modifier for the interface method foo2; only public, private, abstract, default, static and strictfp are permitted\n" +
			"----------\n" +
			"2. WARNING in I.java (at line 6)\n" +
			"	strictfp void foo3();\n" +
			"	^^^^^^^^\n" +
			"Floating-point expressions are always strictly evaluated from source level 17. Keyword \'strictfp\' is not required.\n" +
			"----------\n" +
			"3. ERROR in I.java (at line 6)\n" +
			"	strictfp void foo3();\n" +
			"	              ^^^^^^\n" +
			"strictfp is not permitted for abstract interface method foo3\n" +
			"----------\n" +
			"4. WARNING in I.java (at line 7)\n" +
			"	public strictfp synchronized void foo4();\n" +
			"	       ^^^^^^^^\n" +
			"Floating-point expressions are always strictly evaluated from source level 17. Keyword \'strictfp\' is not required.\n" +
			"----------\n" +
			"5. ERROR in I.java (at line 7)\n" +
			"	public strictfp synchronized void foo4();\n" +
			"	                                  ^^^^^^\n" +
			"strictfp is not permitted for abstract interface method foo4\n" +
			"----------\n" +
			"6. ERROR in I.java (at line 7)\n" +
			"	public strictfp synchronized void foo4();\n" +
			"	                                  ^^^^^^\n" +
			"Illegal modifier for the interface method foo4; only public, private, abstract, default, static and strictfp are permitted\n" +
			"----------\n" +
			"7. WARNING in I.java (at line 8)\n" +
			"	public strictfp synchronized @Annot void foo5();\n" +
			"	       ^^^^^^^^\n" +
			"Floating-point expressions are always strictly evaluated from source level 17. Keyword \'strictfp\' is not required.\n" +
			"----------\n" +
			"8. ERROR in I.java (at line 8)\n" +
			"	public strictfp synchronized @Annot void foo5();\n" +
			"	                                         ^^^^^^\n" +
			"strictfp is not permitted for abstract interface method foo5\n" +
			"----------\n" +
			"9. ERROR in I.java (at line 8)\n" +
			"	public strictfp synchronized @Annot void foo5();\n" +
			"	                                         ^^^^^^\n" +
			"Illegal modifier for the interface method foo5; only public, private, abstract, default, static and strictfp are permitted\n" +
			"----------\n";
		runNegativeTest(
		new String[] {
			"I.java",
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.METHOD) @interface Annot{}\n" +
			"public interface I {\n" +
			"    void foo1();\n" +
			"    public synchronized void foo2();\n" +
			"    strictfp void foo3();\n" +
			"    public strictfp synchronized void foo4();\n" +
			"    public strictfp synchronized @Annot void foo5();\n" +
			"}\n"},
			op);
	}

	// default & regular methods with modifiers that are illegal even for default methods
	public void testModifiers3() {
		String infMod = this.complianceLevel >= ClassFileConstants.JDK9 ? " private," : "";
		runNegativeTest(
		new String[] {
			"I.java",
			"public interface I {\n" +
			"    native void foo1();\n" +
			"    static void foo2();\n" +
			"    native default void foo3() {}\n" +
			"    default native void foo4() {}\n" +
			"    static default void foo5() {}\n" +
			"    default static void foo6() {}\n" +
			"}\n"},
			"----------\n" +
			"1. ERROR in I.java (at line 2)\n" +
			"	native void foo1();\n" +
			"	            ^^^^^^\n" +
			"Illegal modifier for the interface method foo1; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 3)\n" +
			"	static void foo2();\n" +
			"	            ^^^^^^\n" +
			"This method requires a body instead of a semicolon\n" +
			"----------\n" +
			"3. ERROR in I.java (at line 4)\n" +
			"	native default void foo3() {}\n" +
			"	                    ^^^^^^\n" +
			"Illegal modifier for the interface method foo3; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
			"----------\n" +
			"4. ERROR in I.java (at line 5)\n" +
			"	default native void foo4() {}\n" +
			"	                    ^^^^^^\n" +
			"Illegal modifier for the interface method foo4; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
			"----------\n" +
			"5. ERROR in I.java (at line 6)\n" +
			"	static default void foo5() {}\n" +
			"	                    ^^^^^^\n" +
			"Illegal combination of modifiers for the interface method foo5; only one of abstract, default, or static permitted\n" +
			"----------\n" +
			"6. ERROR in I.java (at line 7)\n" +
			"	default static void foo6() {}\n" +
			"	                    ^^^^^^\n" +
			"Illegal combination of modifiers for the interface method foo6; only one of abstract, default, or static permitted\n" +
			"----------\n");
	}

	// if an interface methods is explicitly "abstract" it cannot have a (default) body
	public void testModifiers4() {
		runNegativeTest(
		new String[] {
			"I.java",
			"import java.lang.annotation.*;\n" +
			"public interface I {\n" +
			"    abstract void foo1();\n" + // OK
			"    public abstract default void foo2() {}\n" +
			"    default abstract void foo3() {}\n" +
			"    void foo4() { }\n" + // implicit "abstract" without "default" doesn't allow a body, either
			"    abstract static default void foo5() {}\n" + // double fault
			"}\n"},
			"----------\n" +
			"1. ERROR in I.java (at line 4)\n" +
			"	public abstract default void foo2() {}\n" +
			"	                             ^^^^^^\n" +
			"Illegal combination of modifiers for the interface method foo2; only one of abstract, default, or static permitted\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 5)\n" +
			"	default abstract void foo3() {}\n" +
			"	                      ^^^^^^\n" +
			"Illegal combination of modifiers for the interface method foo3; only one of abstract, default, or static permitted\n" +
			"----------\n" +
			"3. ERROR in I.java (at line 6)\n" +
			"	void foo4() { }\n" +
			"	     ^^^^^^\n" +
			"Abstract methods do not specify a body\n" +
			"----------\n" +
			"4. ERROR in I.java (at line 7)\n" +
			"	abstract static default void foo5() {}\n" +
			"	                             ^^^^^^\n" +
			"Illegal combination of modifiers for the interface method foo5; only one of abstract, default, or static permitted\n" +
			"----------\n");
	}

	// class implements interface with default method.
	// - no need to implement this interface method as it is not abstract
	public void testModifiers5() {
		runConformTest(
			new String[] {
				"C.java",
				"public class C implements I {\n" +
				"    public static void main(String[] args) {\n" +
				"        new C().foo();\n" +
				"    }\n" +
				"}\n",
				"I.java",
				"public interface I {\n" +
				"    default void foo() {\n" +
				"        System.out.println(\"default\");\n" +
				"    }\n" +
				"}\n"
			},
			"default"
			);
	}

	// class implements interface with default method.
	// - no need to implement this interface method as it is not abstract, but other abstract method exists
	public void testModifiers6() {
		runNegativeTest(
			new String[] {
				"I.java",
				"public interface I {\n" +
				"    default void foo() {}\n" +
				"    void bar();\n" +
				"}\n",
				"C.java",
				"public class C implements I {}\n"
			},
			"----------\n" +
			"1. ERROR in C.java (at line 1)\n" +
			"	public class C implements I {}\n" +
			"	             ^\n" +
			"The type C must implement the inherited abstract method I.bar()\n" +
			"----------\n");
	}

	// a default method has a semicolon body / an undocumented empty body
	public void testModifiers7() {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNDOCUMENTED_EMPTY_BLOCK, JavaCore.ERROR);
		runNegativeTest(
			new String[] {
				"I.java",
				"public interface I {\n" +
				"    default void foo();\n" +
				"    default void bar() {}\n" +
				"    default void zork() { /* nop */ }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in I.java (at line 2)\n" +
			"	default void foo();\n" +
			"	             ^^^^^\n" +
			"This method requires a body instead of a semicolon\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 3)\n" +
			"	default void bar() {}\n" +
			"	                   ^^\n" +
			"Empty block should be documented\n" +
			"----------\n",
			null/*classLibs*/,
			true/*shouldFlush*/,
			options);
	}

	// JLS 9.4.2  - default method cannot override method from Object
	// Bug 382355 - [1.8][compiler] Compiler accepts erroneous default method
	// new error message
	public void testObjectMethod1() {
		runNegativeTest(
			new String[] {
				"I.java",
				"public interface I {\n" +
				"    public default String toString () { return \"\";}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in I.java (at line 2)\n" +
			"	public default String toString () { return \"\";}\n" +
			"	                      ^^^^^^^^^^^\n" +
			"A default method cannot override a method from java.lang.Object \n" +
			"----------\n");
	}

	// JLS 9.4.2  - default method cannot override method from Object
	// Bug 382355 - [1.8][compiler] Compiler accepts erroneous default method
	// when using a type variable this is already reported as a name clash
	public void testObjectMethod2() {
		runNegativeTest(
			new String[] {
				"I.java",
				"public interface I<T> {\n" +
				"    public default boolean equals (T other) { return false;}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in I.java (at line 2)\n" +
			"	public default boolean equals (T other) { return false;}\n" +
			"	                       ^^^^^^^^^^^^^^^^\n" +
			"Name clash: The method equals(T) of type I<T> has the same erasure as equals(Object) of type Object but does not override it\n" +
			"----------\n");
	}

	// JLS 9.4.2  - default method cannot override method from Object
	// Bug 382355 - [1.8][compiler] Compiler accepts erroneous default method
	// one error for final method is enough
	public void testObjectMethod3() {
		runNegativeTest(
			new String[] {
				"I.java",
				"public interface I<T> {\n" +
				"    @Override\n" +
				"    default public Class<?> getClass() { return null;}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in I.java (at line 3)\n" +
			"	default public Class<?> getClass() { return null;}\n" +
			"	                        ^^^^^^^^^^\n" +
			"Cannot override the final method from Object\n" +
			"----------\n");
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// an inherited default methods clashes with another inherited method
	// simple case
	public void testInheritedDefaultOverrides01() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	String foo();\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"	default String foo() { return \"\"; }\n" +
				"}\n",
				"I3.java",
				"public interface I3 extends I1, I2 {\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in I3.java (at line 1)\n" +
			"	public interface I3 extends I1, I2 {\n" +
			"	                 ^^\n" +
			"The default method foo() inherited from I2 conflicts with another method inherited from I1\n" +
			"----------\n");
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// an inherited default methods clashes with another inherited method
	// indirect inheritance
	public void testInheritedDefaultOverrides02() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	String foo();\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"	default String foo() { return \"\"; }\n" +
				"}\n",
				"I1A.java",
				"public interface I1A extends I1 {\n" +
				"}\n",
				"I2A.java",
				"public interface I2A extends I2 {\n" +
				"}\n",
				"I3.java",
				"public interface I3 extends I1A, I2A {\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in I3.java (at line 1)\n" +
			"	public interface I3 extends I1A, I2A {\n" +
			"	                 ^^\n" +
			"The default method foo() inherited from I2 conflicts with another method inherited from I1\n" +
			"----------\n");
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// Parameterized case is already reported as a clash
	public void testInheritedDefaultOverrides03() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"import java.util.List;\n" +
				"public interface I1 {\n" +
				"	String foo(List<String> l);\n" +
				"}\n",
				"I2.java",
				"import java.util.List;\n" +
				"public interface I2 {\n" +
				"   @SuppressWarnings(\"rawtypes\")\n" +
				"	default String foo(List l) { return \"\"; }\n" +
				"}\n",
				"I3.java",
				"import java.util.List;\n" +
				"public interface I3 extends I1, I2 {\n" +
				"   @Override\n" +
				"   String foo(List<String> l);\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in I3.java (at line 4)\n" +
			"	String foo(List<String> l);\n" +
			"	       ^^^^^^^^^^^^^^^^^^^\n" +
			"Name clash: The method foo(List<String>) of type I3 has the same erasure as foo(List) of type I2 but does not override it\n" +
			"----------\n");
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// Parameterized case is already reported as a clash - inverse case of previous
	public void testInheritedDefaultOverrides04() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"import java.util.List;\n" +
				"public interface I1 {\n" +
				"	default String foo(List<String> l) { return \"\"; }\n" +
				"}\n",
				"I2.java",
				"import java.util.List;\n" +
				"public interface I2 {\n" +
				"   @SuppressWarnings(\"rawtypes\")\n" +
				"	String foo(List l);\n" +
				"}\n",
				"I3.java",
				"import java.util.List;\n" +
				"public interface I3 extends I1, I2 {\n" +
				"   @Override\n" +
				"   String foo(List<String> l);\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in I3.java (at line 4)\n" +
			"	String foo(List<String> l);\n" +
			"	       ^^^^^^^^^^^^^^^^^^^\n" +
			"Name clash: The method foo(List<String>) of type I3 has the same erasure as foo(List) of type I2 but does not override it\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=390761
	public void testDefaultNonclash() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public interface X extends Map<String, Object> {\n" +
				"   Zork z;\n" +
				"}\n" +
				"\n" +
				"interface Map<K,V> extends MapStream<K, V>  {\n" +
				"   @Override\n" +
				"	default Iterable<BiValue<K, V>> asIterable() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n" +
				"interface MapStream<K, V> {\n" +
				"	Iterable<BiValue<K, V>> asIterable();\n" +
				"}\n" +
				"\n" +
				"interface BiValue<T, U> {\n" +
				"    T getKey();\n" +
				"    U getValue();\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=390761
	public void testDefaultNonclash2() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public interface X extends Map<String, Object> {\n" +
				"   Zork z;\n" +
				"}\n" +
				"\n" +
				"interface Map<K,V> extends MapStream<K, V>  {\n" +
				"   @Override\n" +
				"	Iterable<BiValue<K, V>> asIterable();\n" +
				"}\n" +
				"interface MapStream<K, V> {\n" +
				"	default Iterable<BiValue<K, V>> asIterable() {\n" +
				"       return null;\n" +
				"   }\n" +
				"}\n" +
				"\n" +
				"interface BiValue<T, U> {\n" +
				"    T getKey();\n" +
				"    U getValue();\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}

	public void testDefaultNonclash3() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public interface X extends Map<String, Object> {\n" +
				"   Zork z;\n" +
				"}\n" +
				"\n" +
				"interface Map<K,V> extends MapStream<K, V>  {\n" +
				"   @Override\n" +
				"	default Iterable<BiValue<K, V>> asIterable() {\n" +
				"       return null;\n" +
				"   }\n" +
				"}\n" +
				"interface MapStream<K, V> {\n" +
				"	default Iterable<BiValue<K, V>> asIterable() {\n" +
				"       return null;\n" +
				"   }\n" +
				"}\n" +
				"\n" +
				"interface BiValue<T, U> {\n" +
				"    T getKey();\n" +
				"    U getValue();\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=390761
	public void testDefaultNonclash4() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public interface X extends Map<String, Object> {\n" +
				"   Zork z;\n" +
				"}\n" +
				"\n" +
				"interface Map<K,V> extends MapStream<K, V>  {\n" +
				"   @Override\n" +
				"	Iterable<BiValue<K, V>> asIterable();\n" +
				"}\n" +
				"interface MapStream<K, V> {\n" +
				"	Iterable<BiValue<K, V>> asIterable();\n" +
				"}\n" +
				"\n" +
				"interface BiValue<T, U> {\n" +
				"    T getKey();\n" +
				"    U getValue();\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420080
	public void testDefaultNonclash5() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X extends G implements I {\n" +
				"}\n" +
				"\n" +
				"interface I {\n" +
				"	default int foo (){\n" +
				"		return 0;\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class G {\n" +
				"	public int foo() {\n" +
				"		return 0;\n" +
				"	}\n" +
				"}\n"
			});
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// Don't report conflict between the same method inherited on two paths.
	public void testInheritedDefaultOverrides05() {
		runConformTest(
			new String[] {
				"StringList.java",
				"import java.util.Collection;\n" +
				"public abstract class StringList implements Collection<String> {\n" +
				"}\n"
			},
			"");
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// extract from SuperTypeTest.test013():
	public void testInheritedDefaultOverrides06() {
		runConformTest(
			new String[] {
				"IterableList.java",
				"import java.util.*;\n" +
				"public interface IterableList<E> extends Iterable<E>, List<E> {}\n" +
				"interface ListIterable<E> extends Iterable<E>, List<E> {}\n" +
				"\n"
			},
			"");
	}

	// JLS 8.1.1.1 abstract Classes
	// Default method overrides an abstract method from its super interface
	public void testAbstract01() {
		runConformTest(
			new String[] {
				"I2.java",
				"public interface I2 {\n" +
				"    void test();\n" +
				"}\n",
				"I1.java",
				"public interface I1 extends I2 {\n" +
				"    default void test() {}\n" +
				"}\n",
				"C.java",
				"public class C implements I1 {\n" +
				"}\n"
			});
	}

	// JLS 8.1.1.1 abstract Classes
	// Default method conflicts with independent interface method
	public void testAbstract02() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    void test();\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"    default void test() {}\n" +
				"}\n",
				"C.java",
				"public class C implements I1, I2 {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in C.java (at line 1)\n" +
			"	public class C implements I1, I2 {\n" +
			"	             ^\n" +
			"The default method test() inherited from I2 conflicts with another method inherited from I1\n" +
			"----------\n");
			// Note: javac first complains: C is not abstract and does not override abstract method test() in I1
			//       only when C is marked abstract does the conflict between abstract and default method surface
	}

	// JLS 8.1.1.1 abstract Classes
	// Default method conflicts independent interface method
	// same as above except for order of implements list
	public void testAbstract02a() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    void test();\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"    default void test() {}\n" +
				"}\n",
				"C.java",
				"public class C implements I2, I1 {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in C.java (at line 1)\n" +
			"	public class C implements I2, I1 {\n" +
			"	             ^\n" +
			"The default method test() inherited from I2 conflicts with another method inherited from I1\n" +
			"----------\n");
			// Note: javac first complains: C is not abstract and does not override abstract method test() in I1
			//       only when C is marked abstract does the conflict between abstract and default method surface
	}

	// JLS 8.1.1.1 abstract Classes
	// Default method does not override independent abstract method
	// class is abstract
	public void testAbstract02b() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    void test();\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"    default void test() {}\n" +
				"}\n",
				"C.java",
				"public abstract class C implements I2, I1 {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in C.java (at line 1)\n" +
			"	public abstract class C implements I2, I1 {\n" +
			"	                      ^\n" +
			"The default method test() inherited from I2 conflicts with another method inherited from I1\n" +
			"----------\n");
	}

	// same as above but only interfaces
	public void testAbstract02c() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    void test();\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"    default void test() {}\n" +
				"}\n",
				"I3.java",
				"public interface I3 extends I1, I2 {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in I3.java (at line 1)\n" +
			"	public interface I3 extends I1, I2 {\n" +
			"	                 ^^\n" +
			"The default method test() inherited from I2 conflicts with another method inherited from I1\n" +
			"----------\n");
	}

	// JLS 8.1.1.1 abstract Classes
	// Default method overrides an abstract method from its super interface - class implements both
	public void testAbstract03() {
		runConformTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    void test();\n" +
				"}\n",
				"I2.java",
				"public interface I2 extends I1 {\n" +
				"    @Override\n" +
				"    default void test() {}\n" +
				"}\n",
				"C.java",
				"public class C implements I1, I2 {\n" +
				"}\n"
			});
	}

	// JLS 8.1.1.1 abstract Classes
	// Default method overrides an abstract method from its super interface - class implements both
	// same as above except for order of implements list
	public void testAbstract03a() {
		runConformTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    void test();\n" +
				"}\n",
				"I2.java",
				"public interface I2 extends I1 {\n" +
				"    @Override\n" +
				"    default void test() {}\n" +
				"}\n",
				"C.java",
				"public class C implements I2, I1 {\n" +
				"}\n"
			});
	}

	// JLS 8.1.1.1 abstract Classes
	// default method is not inherited because a more specific abstract method is.
	public void testAbstract04() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    default void test() {}\n" +
				"}\n",
				"I2.java",
				"public interface I2 extends I1 {\n" +
				"    @Override\n" +
				"    void test();\n" +
				"}\n",
				"C.java",
				"public class C implements I2, I1 {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in C.java (at line 1)\n" +
			"	public class C implements I2, I1 {\n" +
			"	             ^\n" +
			"The type C must implement the inherited abstract method I2.test()\n" +
			"----------\n");
	}

	// JLS 8.1.1.1 abstract Classes
	// default method is not inherited because a more specific abstract method is.
	// same as above except for order of implements list
	public void testAbstract04a() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    default void test() {}\n" +
				"}\n",
				"I2.java",
				"public interface I2 extends I1 {\n" +
				"    @Override\n" +
				"    void test();\n" +
				"}\n",
				"C.java",
				"public class C implements I2, I1 {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in C.java (at line 1)\n" +
			"	public class C implements I2, I1 {\n" +
			"	             ^\n" +
			"The type C must implement the inherited abstract method I2.test()\n" +
			"----------\n");
	}

	// abstract class method trumps otherwise conflicting default methods: the conflict scenario
	public void testAbstract05() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	default String value1() { return null; }\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"	default String value1() { return \"\"; }\n" + // conflicts with other default method
				"}\n",
				"C2.java",
				"public abstract class C2 implements I1, I2 {\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in C2.java (at line 1)\n" +
			"	public abstract class C2 implements I1, I2 {\n" +
			"	                      ^^\n" +
			"Duplicate default methods named value1 with the parameters () and () are inherited from the types I2 and I1\n" +
			"----------\n");
	}

	// abstract class method trumps otherwise conflicting default methods: conflict resolved
	public void testAbstract06() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	default String value1() { return null; }\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"	default String value1() { return \"\"; }\n" + // conflicts with other default method
				"}\n",
				"C1.java",
				"public abstract class C1 {\n" +
				"	abstract Object value1();\n" + // trumps the conflicting methods (without overriding)
				"}\n",
				"C2.java",
				"public abstract class C2 extends C1 implements I1, I2 {\n" +
				"}\n",
				"C3.java",
				"public class C3 extends C2 {\n" +
				"	@Override\n" +
				"	public Object value1() { return this; } // too week, need a method returning String\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in C3.java (at line 3)\n" +
			"	public Object value1() { return this; } // too week, need a method returning String\n" +
			"	       ^^^^^^\n" +
			"The return type is incompatible with I1.value1()\n" +
			"----------\n" +
			"2. ERROR in C3.java (at line 3)\n" +
			"	public Object value1() { return this; } // too week, need a method returning String\n" +
			"	       ^^^^^^\n" +
			"The return type is incompatible with I2.value1()\n" +
			"----------\n");
	}

	// abstract class method trumps otherwise conflicting default methods: conflict resolved
	// variant: second method is not a default method
	public void testAbstract06a() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	default String value1() { return null; }\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"	String value1();\n" + // conflicts with other default method
				"}\n",
				"C1.java",
				"public abstract class C1 {\n" +
				"	abstract Object value1();\n" + // trumps the conflicting methods (without overriding)
				"}\n",
				"C2.java",
				"public abstract class C2 extends C1 implements I1, I2 {\n" +
				"}\n",
				"C3.java",
				"public class C3 extends C2 {\n" +
				"	@Override\n" +
				"	public Object value1() { return this; } // too week, need a method returning String\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in C3.java (at line 3)\n" +
			"	public Object value1() { return this; } // too week, need a method returning String\n" +
			"	       ^^^^^^\n" +
			"The return type is incompatible with I2.value1()\n" +
			"----------\n" +
			"2. ERROR in C3.java (at line 3)\n" +
			"	public Object value1() { return this; } // too week, need a method returning String\n" +
			"	       ^^^^^^\n" +
			"The return type is incompatible with I1.value1()\n" +
			"----------\n");
	}

	// abstract class method trumps otherwise conflicting default methods: conflict not resolved due to insufficient visibility
	public void testAbstract6b() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	default String value1() { return null; }\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"	default String value1() { return \"\"; }\n" + // conflicts with other default method
				"}\n",
				"p1/C1.java",
				"package p1;\n" +
				"public abstract class C1 {\n" +
				"	abstract Object value1();\n" + // trump with package visibility doesn't work
				"}\n",
				"C2.java",
				"public abstract class C2 extends p1.C1 implements I1, I2 {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in C2.java (at line 1)\n" +
			"	public abstract class C2 extends p1.C1 implements I1, I2 {\n" +
			"	                      ^^\n" +
			"Duplicate default methods named value1 with the parameters () and () are inherited from the types I2 and I1\n" +
			"----------\n");
	}

	// abstract class method trumps otherwise conflicting default method: only one default method
	public void testAbstract07() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	default String value1() { return null; }\n" +
				"}\n",
				"C1.java",
				"public abstract class C1 {\n" +
				"	abstract Object value1();\n" + // trumps the conflicting method (without overriding)
				"}\n",
				"C2.java",
				"public abstract class C2 extends C1 implements I1 {\n" +
				"}\n",
				"C3.java",
				"public class C3 extends C2 {\n" +
				"	@Override\n" +
				"	public Object value1() { return this; } // too week, need a method returning String\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in C3.java (at line 3)\n" +
			"	public Object value1() { return this; } // too week, need a method returning String\n" +
			"	       ^^^^^^\n" +
			"The return type is incompatible with I1.value1()\n" +
			"----------\n");
	}

	// class inherits two override equivalent methods,
	// must be declared abstract, although one of the methods is a default method.
	public void testAbstract08() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	default String value() { return null; }\n" +
				"}\n",
				"C1.java",
				"public abstract class C1 {\n" +
				"	public abstract String value();" +
				"}\n",
				"C2.java",
				"public class C2 extends C1 implements I1 {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in C2.java (at line 1)\n" +
			"	public class C2 extends C1 implements I1 {\n" +
			"	             ^^\n" +
			"The type C2 must implement the inherited abstract method C1.value()\n" +
			"----------\n");
	}

	// an annotation type cannot have default methods
	public void testAnnotation1() {
		runNegativeTest(
			new String[] {
				"I.java",
				"public @interface I {\n" +
				"    default String id() { return \"1\"; }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in I.java (at line 2)\n" +
			"	default String id() { return \"1\"; }\n" +
			"	^^^^^^^\n" +
			"Syntax error on token \"default\", @ expected\n" +
			"----------\n");
	}

	// basic situation similar to AmbiguousMethodTest.test009()
	public void testSuperCall1() throws Exception {
		this.runConformTest(
			new String[] {
				"OrderedSet.java",
				"import java.util.*;\n" +
				"import java.util.stream.Stream;\n" +
				"public interface OrderedSet<E> extends List<E>, Set<E> {\n" +
				"	@Override\n" +
				"	boolean add(E o);\n" +
				"	@Override\n" +
				"	default Spliterator<E> spliterator() { if (true) return List.super.spliterator(); else return Set.super.spliterator(); }\n" +
				"}\n"
			},
			""
		);
		String expectedOutput =
				"  // Method descriptor #14 ()Ljava/util/Spliterator;\n" +
				"  // Signature: ()Ljava/util/Spliterator<TE;>;\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public java.util.Spliterator spliterator();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.util.List.spliterator() : java.util.Spliterator [17]\n" +
				"    4  areturn\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "OrderedSet.class", "OrderedSet", expectedOutput);
	}

	// some illegal cases
	// - call to indirect super
	// - call to super of outer
	// - target method is not a default method
	// - attempt to use this syntax for a super-ctor call
	public void testSuperCall2() {
		this.runNegativeTest(
			new String[] {
				"T.java",
				"import java.util.*;\n" +
				"import java.util.stream.Stream;\n" +
				"public abstract class T<E> implements OrderedSet<E> {\n" +
				"	@Override\n" +
				"	public Stream<E> stream() {\n" +
				"		return List.super.stream(); // List is not a direct super interface\n" +
				"	}\n" +
				"	@Override\n" +
				"	public Stream<E> parallelStream() { return OrderedSet.super.parallelStream();}\n" + // OK
				"   class Inner {\n" +
				"		public Stream<E> stream() {\n" +
				"			return OrderedSet.super.stream(); // not a super interface of the direct enclosing class\n" +
				"		}\n" +
				"	}\n" +
				"	@Override\n" +
				"	public boolean add(E o) {\n" +
				"		OrderedSet.super.add(o); // target not a default method\n" +
				"	}\n" +
				"	T() {\n" +
				"		OrderedSet.super(); // not applicable for super ctor call\n" +
				"	}\n" +
				"}\n" +
				"interface OrderedSet<E> extends List<E>, Set<E> {\n" +
				"	@Override\n" +
				"	boolean add(E o);\n" +
				"	@Override\n" +
				"   default Spliterator<E> spliterator() { return List.super.spliterator(); }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in T.java (at line 6)\n" +
			"	return List.super.stream(); // List is not a direct super interface\n" +
			"	       ^^^^^^^^^^\n" +
			"Illegal reference to super type List, cannot bypass the more specific direct super type OrderedSet\n" +
			"----------\n" +
			"2. ERROR in T.java (at line 12)\n" +
			"	return OrderedSet.super.stream(); // not a super interface of the direct enclosing class\n" +
			"	       ^^^^^^^^^^^^^^^^\n" +
			"No enclosing instance of the type OrderedSet<E> is accessible in scope\n" +
			"----------\n" +
			"3. ERROR in T.java (at line 17)\n" +
			"	OrderedSet.super.add(o); // target not a default method\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Cannot directly invoke the abstract method add(E) for the type OrderedSet<E>\n" +
			"----------\n" +
			"4. ERROR in T.java (at line 20)\n" +
			"	OrderedSet.super(); // not applicable for super ctor call\n" +
			"	^^^^^^^^^^\n" +
			"Illegal enclosing instance specification for type Object\n" +
			"----------\n"
		);
	}

	// with execution
	public void testSuperCall3() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements I2 {\n" +
				"	@Override\n" +
				"	public void print() {\n" +
				"		I2.super.print();\n" +
				"		System.out.print(\"!\");" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		new X().print();\n" +
				"	}\n" +
				"}\n" +
				"interface I1 {\n" +
				"	default void print() {\n" +
				"		System.out.print(\"O\");\n" +
				"	}\n" +
				"}\n" +
				"interface I2 extends I1 {\n" +
				"	default void print() {\n" +
				"		I1.super.print();\n" +
				"		System.out.print(\"K\");\n" +
				"	}\n" +
				"}\n"
			},
			"OK!"
		);
	}

	// 15.12.1
	// https://bugs.eclipse.org/404649 - [1.8][compiler] detect illegal reference to indirect or redundant super
	public void testSuperCall4() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X implements I2, I1 {\n" +
				"	@Override\n" +
				"	public void print() {\n" +
				"		I1.super.print(); // illegal attempt to skip I2.print()\n" +
				"		System.out.print(\"!\");" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		new X().print();\n" +
				"	}\n" +
				"}\n" +
				"interface I1 {\n" +
				"	default void print() {\n" +
				"		System.out.print(\"O\");\n" +
				"	}\n" +
				"}\n" +
				"interface I2 extends I1 {\n" +
				"	@Override default void print() {\n" +
				"		System.out.print(\"K\");\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	I1.super.print(); // illegal attempt to skip I2.print()\n" +
			"	^^^^^^^^\n" +
			"Illegal reference to super type I1, cannot bypass the more specific direct super type I2\n" +
			"----------\n"
		);
	}

	// 15.12.1
	// https://bugs.eclipse.org/404649 - [1.8][compiler] detect illegal reference to indirect or redundant super
	public void testSuperCall5() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X implements I2, I1 {\n" +
				"	@Override\n" +
				"	public void print() {\n" +
				"		I1.super.print(); // illegal attempt to skip I2.print()\n" +
				"		System.out.print(\"!\");" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		new X().print();\n" +
				"	}\n" +
				"}\n" +
				"interface I1 {\n" +
				"	default void print() {\n" +
				"		System.out.print(\"O\");\n" +
				"	}\n" +
				"}\n" +
				"interface I2 extends I1 {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	I1.super.print(); // illegal attempt to skip I2.print()\n" +
			"	^^^^^^^^\n" +
			"Illegal reference to super type I1, cannot bypass the more specific direct super type I2\n" +
			"----------\n"
		);
	}

	// 15.12.3
	// https://bugs.eclipse.org/404649 - [1.8][compiler] detect illegal reference to indirect or redundant super
	public void testSuperCall6() {
		this.runNegativeTest(
			new String[] {
				"SuperOverride.java",
				"interface I0 {\n" +
				"	default void foo() { System.out.println(\"I0\"); }\n" +
				"}\n" +
				"\n" +
				"interface IA extends I0 {}\n" +
				"\n" +
				"interface IB extends I0 {\n" +
				"	@Override default void foo() {\n" +
				"		System.out.println(\"IB\");\n" +
				"	}\n" +
				"}\n" +
				"interface IX extends IA, IB {\n" +
				"	@Override default void foo() {\n" +
				"		IA.super.foo(); // illegal attempt to skip IB.foo()\n" +
				"	}\n" +
				"}\n" +
				"public class SuperOverride implements IX {\n" +
				"	public static void main(String[] args) {\n" +
				"		new SuperOverride().foo();\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in SuperOverride.java (at line 14)\n" +
			"	IA.super.foo(); // illegal attempt to skip IB.foo()\n" +
			"	^^^^^^^^^^^^^^\n" +
			"Illegal reference to super method foo() from type I0, cannot bypass the more specific override from type IB\n" +
			"----------\n"
		);
	}

	// Bug 401235 - [1.8][compiler] 'this' reference must be allowed in default methods and local classes
	public void testThisReference1() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements I1, I2 {\n" +
				"	@Override\n" +
				"	public String s1() { return \"O\"; }\n" +
				"	@Override\n" +
				"	public String s2() { return \"K\"; }\n" +
				"	public static void main(String... args) {\n" +
				"		X x = new X();\n" +
				"		x.print1();\n" +
				"		x.print2();\n" +
				"	}\n" +
				"}\n" +
				"interface I1 {\n" +
				"	String s1();" +
				"	default void print1() {\n" +
				"		System.out.print(this.s1());\n" + // 'this' as a receiver
				"	}\n" +
				"}\n" +
				"interface I2 {\n" +
				"	String s2();\n" +
				"	default void print2() {\n" +
				"		class Inner {\n" +
				"			String value() { return I2.this.s2(); }\n" + // qualified 'this' refering to the enclosing interface type
				"		}\n" +
				"		System.out.print(new Inner().value());\n" +
				"	}\n" +
				"}\n"
			},
			"OK"
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	// Test for different legal and illegal keywords for static and default methods in interfaces
	public void testStaticMethod01() {
		runNegativeTest(
				new String[] {
					"I.java",
					"public interface I {\n" +
					"	static void foo() {}\n" +
					"	static void foo1();\n" +
					"	public static default void foo2 () {};\n" +
					"	abstract static void foo3();\n" +
					"	abstract static void foo4() {}\n" +
					"}"
				},
				"----------\n" +
				"1. ERROR in I.java (at line 3)\n" +
				"	static void foo1();\n" +
				"	            ^^^^^^\n" +
				"This method requires a body instead of a semicolon\n" +
				"----------\n" +
				"2. ERROR in I.java (at line 4)\n" +
				"	public static default void foo2 () {};\n" +
				"	                           ^^^^^^^\n" +
				"Illegal combination of modifiers for the interface method foo2; only one of abstract, default, or static permitted\n" +
				"----------\n" +
				"3. ERROR in I.java (at line 5)\n" +
				"	abstract static void foo3();\n" +
				"	                     ^^^^^^\n" +
				"Illegal combination of modifiers for the interface method foo3; only one of abstract, default, or static permitted\n" +
				"----------\n" +
				"4. ERROR in I.java (at line 6)\n" +
				"	abstract static void foo4() {}\n" +
				"	                     ^^^^^^\n" +
				"Illegal combination of modifiers for the interface method foo4; only one of abstract, default, or static permitted\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	// Test invocation of static methods with different contexts - negative tests
	public void testStaticMethod02() {
		runNegativeTest(
				new String[] {
					"I.java",
					"public interface I {\n" +
					"	public static void foo() {\n" +
					"		bar();\n" +
					"		this.bar();\n" +
					"   }\n" +
					"	public default void bar () {\n" +
					"		this.foo();\n" +
					"	}\n" +
					"}\n" +
					"interface II extends I{\n" +
					"	public static void foobar() {\n" +
					"		super.bar();\n" +
					"   }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in I.java (at line 3)\n" +
				"	bar();\n" +
				"	^^^\n" +
				"Cannot make a static reference to the non-static method bar() from the type I\n" +
				"----------\n" +
				"2. ERROR in I.java (at line 4)\n" +
				"	this.bar();\n" +
				"	^^^^\n" +
				"Cannot use this in a static context\n" +
				"----------\n" +
				"3. ERROR in I.java (at line 7)\n" +
				"	this.foo();\n" +
				"	     ^^^\n" +
				"This static method of interface I can only be accessed as I.foo\n" +
				"----------\n" +
				"4. ERROR in I.java (at line 12)\n" +
				"	super.bar();\n" +
				"	^^^^^\n" +
				"Cannot use super in a static context\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	// Test invocation of static methods with different contexts - positive tests
	public void testStaticMethod03() throws Exception {
		runConformTest(
			new String[] {
				"C.java",
				"interface I {\n" +
				"	public static void foo() {\n" +
				"		System.out.println(\"I#foo() invoked\");\n" +
				"   }\n" +
				"}\n" +
				"interface J extends I {\n" +
				"	public static void foo() {\n" +
				"		System.out.println(\"J#foo() invoked\");\n" +
				"   }\n" +
				"	public default void bar () {\n" +
				"		foo();\n" +
				"	}\n" +
				"}\n" +
				"public class C implements J {\n" +
				"	public static void main(String[] args) {\n" +
				"		C c = new C();\n" +
				"		c.bar();\n" +
				"       J.foo();\n" +
				"       I.foo();\n" +
				"	}\n" +
				"}"
			},
			"J#foo() invoked\n" +
			"J#foo() invoked\n" +
			"I#foo() invoked");
		String expectedOutput =
				"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
				"  // Stack: 2, Locals: 2\n" +
				"  public static void main(java.lang.String[] args);\n" +
				"     0  new C [1]\n" +
				"     3  dup\n" +
				"     4  invokespecial C() [18]\n" +
				"     7  astore_1 [c]\n" +
				"     8  aload_1 [c]\n" +
				"     9  invokevirtual C.bar() : void [19]\n" +
				"    12  invokestatic J.foo() : void [22]\n" +
				"    15  invokestatic I.foo() : void [25]\n" +
				"    18  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 16]\n" +
				"        [pc: 8, line: 17]\n" +
				"        [pc: 12, line: 18]\n" +
				"        [pc: 15, line: 19]\n" +
				"        [pc: 18, line: 20]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 19] local: args index: 0 type: java.lang.String[]\n" +
				"        [pc: 8, pc: 19] local: c index: 1 type: C\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "C.class", "C", expectedOutput);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	// Test invocation of static methods with different contexts - negative tests
	public void testStaticMethod04() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X implements II {\n"
						+ "	@Override"
						+ "	public void foo() {\n"
						+ "		 bar();\n"
						+ "		 bar2();\n"
						+ "	}\n"
						+ "	public static void main(String[] args) {\n"
						+ "		bar();\n"
						+ "		II.bar();\n"
						+ "		(new X()).bar();\n"
						+ "		II.bar();\n"
						+ "		II ii = new X();\n"
						+ "		ii.bar();\n"
						+ "		ii.bar2();\n"
						+ "		I i = new X();\n"
						+ "		i.bar();\n"
						+ "      new I() {}.foo();\n"
						+ "	}\n"
						+ "}\n"
						+ "interface I {\n"
						+ "	public static void bar() {\n"
						+ "		bar2();\n"
						+ "	}\n"
						+ "	public default void bar2() {\n"
						+ "		bar();\n"
						+ "	}\n"
						+ "}\n"
						+ "interface II extends I {\n"
						+ "	public default void foo() {\n"
						+ "		bar();\n"
						+ "	}\n"
						+ "}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	bar();\n" +
				"	^^^\n" +
				"The method bar() is undefined for the type X\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	bar();\n" +
				"	^^^\n" +
				"The method bar() is undefined for the type X\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 8)\n" +
				"	II.bar();\n" +
				"	   ^^^\n" +
				"The method bar() is undefined for the type II\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 9)\n" +
				"	(new X()).bar();\n" +
				"	          ^^^\n" +
				"The method bar() is undefined for the type X\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 10)\n" +
				"	II.bar();\n" +
				"	   ^^^\n" +
				"The method bar() is undefined for the type II\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 12)\n" +
				"	ii.bar();\n" +
				"	   ^^^\n" +
				"The method bar() is undefined for the type II\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 15)\n" +
				"	i.bar();\n" +
				"	  ^^^\n" +
				"This static method of interface I can only be accessed as I.bar\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 16)\n" +
				"	new I() {}.foo();\n" +
				"	           ^^^\n" +
				"The method foo() is undefined for the type new I(){}\n" +
				"----------\n" +
				"9. ERROR in X.java (at line 21)\n" +
				"	bar2();\n" +
				"	^^^^\n" +
				"Cannot make a static reference to the non-static method bar2() from the type I\n" +
				"----------\n" +
				"10. ERROR in X.java (at line 29)\n" +
				"	bar();\n" +
				"	^^^\n" +
				"The method bar() is undefined for the type II\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	public void testStaticMethod05() {
		runNegativeTest(
				new String[] {
						"X.java",
						"interface I {\n" +
						"	static void foo(int x) { }\n" +
						"}\n" +
						"interface II extends I {\n" +
						"	static void goo(int x) {}   		// No Error.\n" +
						"}\n" +
						"interface III extends II {\n" +
						"	default void foo(int x, int y) {}   // No Error.\n" +
						"	default void goo() {}   			// No Error.\n" +
						"	default void foo(int x) {}   		// No Error.\n" +
						"	default void goo(int x) {}   		// No Error.\n" +
						"}\n" +
						"class Y {\n" +
						"	static void goo(int x) {}\n" +
						"}\n" +
						"class X extends Y {\n" +
						"	void foo(int x) {}   // No error.\n" +
						"	void goo() {}   	 // No Error.\n" +
						"	void goo(int x) {}   // Error.\n" +
						"}\n"
						},
						"----------\n" +
						"1. ERROR in X.java (at line 19)\n" +
						"	void goo(int x) {}   // Error.\n" +
						"	     ^^^^^^^^^^\n" +
						"This instance method cannot override the static method from Y\n" +
						"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	// Test that extending interfaces inherit visible fields and inner types.
	public void testStaticMethod06() {
		runConformTest(
				new String[] {
					"C.java",
					"interface I {\n" +
					"	public static String CONST = \"CONSTANT\";\n" +
					"	public static void foo(String[] args) {\n" +
					"		System.out.println(args[0]);\n" +
					"   }\n" +
					" 	public interface Inner {}\n" +
					"}\n" +
					"interface J extends I {\n" +
					"	public static void foo() {\n" +
					"		I.foo(new String[]{CONST});\n" +
					"   }\n" +
					" 	public interface InnerInner extends Inner {}\n" +
					"}\n" +
					"public class C implements J {\n" +
					"	public static void main(String[] args) {\n" +
					"       J.foo();\n" +
					"       I.foo(new String[]{\"LITERAL\"});\n" +
					"	}\n" +
					"}"
				},
				"CONSTANT\n" +
				"LITERAL");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	// Test that type parameter from enclosing type is not allowed to be referred to in static interface methods
	public void testStaticMethod07() {
		runNegativeTest(
				new String[] {
					"C.java",
					"interface I <T> {\n" +
					"	public static T foo(T t) {\n" +
					"		return t;" +
					"   }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in C.java (at line 2)\n" +
				"	public static T foo(T t) {\n" +
				"	              ^\n" +
				"Cannot make a static reference to the non-static type T\n" +
				"----------\n" +
				"2. ERROR in C.java (at line 2)\n" +
				"	public static T foo(T t) {\n" +
				"	                    ^\n" +
				"Cannot make a static reference to the non-static type T\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	public void testStaticMethod08() {
		runNegativeTest(
				new String[] {
					"C.java",
					"@interface A {\n" +
					"	static String foo() default \"Blah\";\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in C.java (at line 2)\n" +
				"	static String foo() default \"Blah\";\n" +
				"	              ^^^^^\n" +
				"Illegal modifier for the annotation attribute A.foo; only public & abstract are permitted\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	public void testStaticMethod09() {
		runNegativeTest(
				new String[] {
						"C.java",
						"interface A {\n" +
						"	static void foo() {}\n" +
						"	default void goo(A a) {\n" +
						"		a.foo();\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in C.java (at line 4)\n" +
				"	a.foo();\n" +
				"	  ^^^\n" +
				"This static method of interface A can only be accessed as A.foo\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	public void testStaticMethod10() {
		runNegativeTest(
				new String[] {
						"C.java",
						"interface A {\n" +
						"	static void foo(long x) {}\n" +
						"	static void foo(int x) {}\n" +
						"	default void goo(A a) {\n" +
						"		a.foo(10);\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in C.java (at line 5)\n" +
				"	a.foo(10);\n" +
				"	  ^^^\n" +
				"This static method of interface A can only be accessed as A.foo\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	public void testStaticMethod11() {
		runNegativeTest(
				new String[] {
						"C.java",
						"interface A<X> {\n" +
						"	void foo(X x);\n" +
						"}\n" +
						"interface B extends A<String> {\n" +
						"    static void foo(String s) {}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in C.java (at line 5)\n" +
				"	static void foo(String s) {}\n" +
				"	            ^^^^^^^^^^^^^\n" +
				"This static method cannot hide the instance method from A<String>\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	public void testStaticMethod12() {
		runNegativeTest(
				new String[] {
						"C.java",
						"interface A<X> {\n" +
						"	static void foo(String x) {}\n" +
						"}\n" +
						"interface B extends A<String> {\n" +
						"    static void foo(String s) {}\n" +
						"}\n" +
						"public class X {\n" +
						"}\n"
				},
				"----------\n" +
				"1. WARNING in C.java (at line 1)\n" +
				"	interface A<X> {\n" +
				"	            ^\n" +
				"The type parameter X is hiding the type X\n" +
				"----------\n" +
				"2. ERROR in C.java (at line 7)\n" +
				"	public class X {\n" +
				"	             ^\n" +
				"The public type X must be defined in its own file\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	public void testStaticMethod13() {
		runNegativeTest(
				new String[] {
						"C.java",
						"interface A {\n" +
						"	static void foo(String x) {\n" +
						"       System.out.println(this);\n"+
						"       System.out.println(super.hashCode());\n" +
						"   }\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in C.java (at line 3)\n" +
				"	System.out.println(this);\n" +
				"	                   ^^^^\n" +
				"Cannot use this in a static context\n" +
				"----------\n" +
				"2. ERROR in C.java (at line 4)\n" +
				"	System.out.println(super.hashCode());\n" +
				"	                   ^^^^^\n" +
				"Cannot use super in a static context\n" +
				"----------\n");
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406619, [1.8][compiler] Incorrect suggestion that method can be made static.
	public void test406619() {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
		compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface X {\n" +
				"	default int foo() {\n" +
				"		return 10;\n" +
				"	}\n" +
				"}\n"
			},
			"",
			null /* no extra class libraries */,
			true /* flush output directory */,
			compilerOptions /* custom options */
		);
	}

	// class implements interface with default method.
	// - witness for NoSuchMethodError in synthetic method (SuperMethodAccess) - turned out to be a JVM bug
	public void testSuperAccess01() {
		runConformTest(
			new String[] {
				"C.java",
				"interface I {\n" +
				"    public default void foo() {\n" +
				"        System.out.println(\"default\");\n" +
				"    }\n" +
				"}\n" +
				"public class C implements I {\n" +
				"    public static void main(String[] args) {\n" +
				"        C c = new C();\n" +
				"        c.foo();\n" +
				"    }\n" +
				"}\n"
			},
			"default"
			);
	}

	// class implements interface with default method.
	// - intermediate public interface
	public void testSuperAccess02() {
		runConformTest(
			new String[] {
				"p1/C.java",
				"package p1;\n" +
				"public class C implements p2.J {\n" +
				"    public static void main(String[] args) {\n" +
				"        C c = new C();\n" +
				"        c.foo();\n" +
				"    }\n" +
				"}\n",
				"p2/J.java",
				"package p2;\n" +
				"interface I {\n" +
				"    public default void foo() {\n" +
				"        System.out.println(\"default\");\n" +
				"    }\n" +
				"}\n" +
				"public interface J extends I {}\n"
			},
			"default");
	}

	// https://bugs.eclipse.org/421796 - Bug 421796 - [1.8][compiler] java.lang.AbstractMethodError executing default method code.
	public void testSuperAccess03() {
		runConformTest(
			new String[] {
				"X.java",
				"interface I  {\n" +
				"    void foo(); \n" +
				"}\n" +
				"\n" +
				"interface J extends I {\n" +
				"    default void foo() {\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"interface K extends J {\n" +
				"}\n" +
				"\n" +
				"public class X implements K {\n" +
				"	public static void main(String argv[]) {\n" +
				"		X test = new X();\n" +
				"		((J)test).foo();\n" +
				"		test.foo();\n" +
				"	}\n" +
				"}\n"
			});
	}

	// Variant of test MethodVerifyTest.test144() from https://bugs.eclipse.org/bugs/show_bug.cgi?id=194034
	public void testBridge01() {
		this.runNegativeTest(
			new String[] {
				"PurebredCatShopImpl.java",
				"import java.util.List;\n" +
				"interface Pet {}\n" +
				"interface Cat extends Pet {}\n" +
				"interface PetShop { default List<Pet> getPets() { return null; } }\n" +
				"interface CatShop extends PetShop {\n" +
				"	default <V extends Pet> List<? extends Cat> getPets() { return null; }\n" +
				"}\n" +
				"interface PurebredCatShop extends CatShop {}\n" +
				"class CatShopImpl implements CatShop {\n" +
				"	@Override public List<Pet> getPets() { return null; }\n" +
				"}\n" +
				"class PurebredCatShopImpl extends CatShopImpl implements PurebredCatShop {}"
			},
			"----------\n" +
			"1. ERROR in PurebredCatShopImpl.java (at line 6)\n" +
			"	default <V extends Pet> List<? extends Cat> getPets() { return null; }\n" +
			"	                                            ^^^^^^^^^\n" +
			"Name clash: The method getPets() of type CatShop has the same erasure as getPets() of type PetShop but does not override it\n" +
			"----------\n" +
			"2. WARNING in PurebredCatShopImpl.java (at line 10)\n" +
			"	@Override public List<Pet> getPets() { return null; }\n" +
			"	                 ^^^^\n" +
			"Type safety: The return type List<Pet> for getPets() from the type CatShopImpl needs unchecked conversion to conform to List<? extends Cat> from the type CatShop\n" +
			"----------\n"
		);
	}
	// yet another variant, checking that exactly one bridge method is created, so that
	// the most specific method is dynamically invoked via all declared types.
	public void testBridge02() {
		this.runConformTest(
			new String[] {
				"PurebredCatShopImpl.java",
				"import java.util.List;\n" +
				"import java.util.ArrayList;\n" +
				"interface Pet {}\n" +
				"interface Cat extends Pet {}\n" +
				"interface PetShop { default List<Pet> getPets() { return null; } }\n" +
				"interface CatShop extends PetShop {\n" +
				"	@Override default ArrayList<Pet> getPets() { return null; }\n" +
				"}\n" +
				"interface PurebredCatShop extends CatShop {}\n" +
				"class CatShopImpl implements CatShop {\n" +
				"	@Override public ArrayList<Pet> getPets() { return new ArrayList<>(); }\n" +
				"}\n" +
				"public class PurebredCatShopImpl extends CatShopImpl implements PurebredCatShop {\n" +
				"	public static void main(String... args) {\n" +
				"		PurebredCatShopImpl pcsi = new PurebredCatShopImpl();\n" +
				"		System.out.print(pcsi.getPets().size());\n" +
				"		CatShopImpl csi = pcsi;\n" +
				"		System.out.print(csi.getPets().size());\n" +
				"		CatShop cs = csi;\n" +
				"		System.out.print(cs.getPets().size());\n" +
				"		PetShop ps = cs;\n" +
				"		System.out.print(ps.getPets().size());\n" +
				"	}\n" +
				"}\n"
			},
			"0000"
		);
	}

	// modeled after org.eclipse.jdt.core.tests.compiler.regression.AmbiguousMethodTest.test081()
	// see https://bugs.eclipse.org/391376 - [1.8] check interaction of default methods with bridge methods and generics
    // see https://bugs.eclipse.org/404648 - [1.8][compiler] investigate differences between compilers re AmbiguousMethodTest
	public void _testBridge03() {
		runConformTest(
			new String[] {
				"C.java",
				"interface A<ModelType extends D, ValueType> extends\n" +
				"		I<ModelType, ValueType> {\n" +
				"\n" +
				"	@Override\n" +
				"	public default void doSet(ModelType valueGetter) {\n" +
				"		this.set((ValueType) valueGetter.getObject());\n" +
				"	}\n" +
				"\n" +
				"	@Override\n" +
				"	public default void set(Object object) {\n" +
				"		System.out.println(\"In A.set(Object)\");\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class B implements A<E, CharSequence> {\n" +
				"\n" +
				"	public void set(CharSequence string) {\n" +
				"		System.out.println(\"In B.set(CharSequence)\");\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"public class C extends B {\n" +
				"\n" +
				"	static public void main(String[] args) {\n" +
				"		C c = new C();\n" +
				"		c.run();\n" +
				"	}\n" +
				"\n" +
				"	public void run() {\n" +
				"		E e = new E<String>(String.class);\n" +
				"		this.doSet(e);\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				"\n" +
				"class D {\n" +
				"	public Object getObject() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class E<Type extends CharSequence> extends D {\n" +
				"\n" +
				"	private Class<Type> typeClass;\n" +
				"\n" +
				"	public E(Class<Type> typeClass) {\n" +
				"		this.typeClass = typeClass;\n" +
				"	}\n" +
				"\n" +
				"	@Override\n" +
				"	public Type getObject() {\n" +
				"		try {\n" +
				"			return (Type) typeClass.newInstance();\n" +
				"		} catch (Exception e) {\n" +
				"			throw new RuntimeException(e);\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				"\n" +
				"interface I<ModelType, ValueType> {\n" +
				"\n" +
				"	public void doSet(ModelType model);\n" +
				"\n" +
				"	public void set(ValueType value);\n" +
				"\n" +
				"}\n"
			},
			"In B.set(CharSequence)");
	}


    // modeled after org.eclipse.jdt.core.tests.compiler.regression.AmbiguousMethodTest.test081()
    // see https://bugs.eclipse.org/391376 - [1.8] check interaction of default methods with bridge methods and generics
    // see https://bugs.eclipse.org/404648 - [1.8][compiler] investigate differences between compilers re AmbiguousMethodTest
    public void _testBridge04() {
        runConformTest(
            new String[] {
                "C.java",
                "interface A<ModelType extends D, ValueType> extends\n" +
                "       I<ModelType, ValueType> {\n" +
                "\n" +
                "   @Override\n" +
                "   public default void doSet(ModelType valueGetter) {\n" +
                "       this.set((ValueType) valueGetter.getObject());\n" +
                "   }\n" +
                "\n" +
                "   @Override\n" +
                "   public default void set(Object object) {\n" +
                "       System.out.println(\"In A.set(Object)\");\n" +
                "   }\n" +
                "}\n" +
                "\n" +
                "interface B extends A<E, CharSequence> {\n" +
                "\n" +
                "   public default void set(CharSequence string) {\n" +
                "       System.out.println(\"In B.set(CharSequence)\");\n" +
                "   }\n" +
                "}\n" +
                "\n" +
                "public class C implements B {\n" +
                "\n" +
                "   static public void main(String[] args) {\n" +
                "       C c = new C();\n" +
                "       c.run();\n" +
                "   }\n" +
                "\n" +
                "   public void run() {\n" +
                "       E e = new E<String>(String.class);\n" +
                "       this.doSet(e);\n" +
                "   }\n" +
                "\n" +
                "}\n" +
                "\n" +
                "class D {\n" +
                "   public Object getObject() {\n" +
                "       return null;\n" +
                "   }\n" +
                "}\n" +
                "\n" +
                "class E<Type extends CharSequence> extends D {\n" +
                "\n" +
                "   private Class<Type> typeClass;\n" +
                "\n" +
                "   public E(Class<Type> typeClass) {\n" +
                "       this.typeClass = typeClass;\n" +
                "   }\n" +
                "\n" +
                "   @Override\n" +
                "   public Type getObject() {\n" +
                "       try {\n" +
                "           return (Type) typeClass.newInstance();\n" +
                "       } catch (Exception e) {\n" +
                "           throw new RuntimeException(e);\n" +
                "       }\n" +
                "   }\n" +
                "\n" +
                "}\n" +
                "\n" +
                "interface I<ModelType, ValueType> {\n" +
                "\n" +
                "   public void doSet(ModelType model);\n" +
                "\n" +
                "   public void set(ValueType value);\n" +
                "\n" +
                "}\n"
            },
            "In B.set(CharSequence)");
    }

    // test for different error messages in modifiers.
	public void test400977() {
		String infMod = this.complianceLevel >= ClassFileConstants.JDK9 ? " private," : "";
		String extra = this.complianceLevel >= ClassFileConstants.JDK17 ?
				"----------\n" +
				"2. WARNING in I.java (at line 3)\n" +
				"	public abstract default strictfp final void bar();}\n" +
				"	                        ^^^^^^^^\n" +
				"Floating-point expressions are always strictly evaluated from source level 17. Keyword \'strictfp\' is not required.\n" : "";
		int offset = this.complianceLevel >= ClassFileConstants.JDK17 ? 1 : 0;
		runNegativeTest(
		new String[] {
			"I.java",
			"public interface I {\n" +
			"    default abstract void foo();\n" +
			"    public abstract default strictfp final void bar();" +
			"}\n"},
			"----------\n" +
			"1. ERROR in I.java (at line 2)\n" +
			"	default abstract void foo();\n" +
			"	                      ^^^^^\n" +
			"Illegal combination of modifiers for the interface method foo; only one of abstract, default, or static permitted\n" +
			extra +
			"----------\n" +
			(2 + offset) + ". ERROR in I.java (at line 3)\n" +
			"	public abstract default strictfp final void bar();}\n" +
			"	                                            ^^^^^\n" +
			"strictfp is not permitted for abstract interface method bar\n" +
			"----------\n" +
			(3 + offset) + ". ERROR in I.java (at line 3)\n" +
			"	public abstract default strictfp final void bar();}\n" +
			"	                                            ^^^^^\n" +
			"Illegal combination of modifiers for the interface method bar; only one of abstract, default, or static permitted\n" +
			"----------\n" +
			(3 + offset) + ". ERROR in I.java (at line 3)\n" +
			"	public abstract default strictfp final void bar();}\n" +
			"	                                            ^^^^^\n" +
			"Illegal modifier for the interface method bar; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
			"----------\n");
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=420084,  [1.8] static interface method cannot be resolved without receiver when imported statically
	public void testBug420084() {
		runNegativeTest(
			new String[] {
				"p/J.java",
				"package p;\n" +
				"public interface J {\n" +
				"	static int foo(){return 0;}\n" +
				"}\n",
				"I.java",
				"import static p.J.foo;\n" +
				"public interface I {\n" +
				"	static int call() {\n" +
				"		return foo();\n" +
				"	}\n" +
				"}\n"
			},
			"");
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=421543, [1.8][compiler] Compiler fails to recognize default method being turned into abstract by subtytpe
	public void testBug421543() {
		runNegativeTest(
			new String[] {
				"X.java",
				"interface I  {\n" +
				"	default void foo() {}\n" +
				"}\n" +
				"interface J extends I {\n" +
				"	void foo();\n" +
				"}\n" +
				"public class X implements J {\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	void foo();\n" +
			"	     ^^^^^\n" +
			"The method foo() of type J should be tagged with @Override since it actually overrides a superinterface method\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	public class X implements J {\n" +
			"	             ^\n" +
			"The type X must implement the inherited abstract method J.foo()\n" +
			"----------\n");
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=421543, [1.8][compiler] Compiler fails to recognize default method being turned into abstract by subtytpe
	public void testBug421543a() {
		runNegativeTest(
			new String[] {
				"X.java",
				"interface I<T>  {\n" +
				"	default void foo(T t) {}\n" +
				"}\n" +
				"interface J extends I<J> {\n" +
				"	void foo(J t);\n" +
				"}\n" +
				"public class X implements J {\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	void foo(J t);\n" +
			"	     ^^^^^^^^\n" +
			"The method foo(J) of type J should be tagged with @Override since it actually overrides a superinterface method\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	public class X implements J {\n" +
			"	             ^\n" +
			"The type X must implement the inherited abstract method J.foo(J)\n" +
			"----------\n");
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=421543, [1.8][compiler] Compiler fails to recognize default method being turned into abstract by subtytpe
	public void testBug421543b() {
		runConformTest(
			new String[] {
				"X.java",
				"interface I<T>  {\n" +
				"	void foo(T t);\n" +
				"}\n" +
				"@SuppressWarnings(\"override\")\n" +
				"interface J extends I<J> {\n" +
				"	default void foo(J t) {}\n" +
				"}\n" +
				"public class X implements J {\n" +
				"}\n"
			},
			"");
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=421797, [1.8][compiler] ClassFormatError with default methods & I.super.foo() syntax
	public void testBug421797() {
		runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	int m(String s, int val);\n" +
				"	public default int foo(String s, int val) {\n" +
				"		System.out.print(s + \" from I.foo:\");\n" +
				"		return val * val; \n" +
				"	}\n" +
				"}\n" +
				"interface T extends I {\n" +
				"	public default int m(String s, int value) { \n" +
				"		I i = I.super::foo; \n" +
				"		return i.foo(s, value);\n" +
				"	}\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String argv[]) {  \n" +
				"		System.out.println(new T(){}.m(\"Hello\", 1234));\n" +
				"	}\n" +
				"}\n"
			},
			"Hello from I.foo:1522756");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422731, [1.8] Ambiguous method not reported on overridden default method
	public void test422731() throws Exception {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X extends Base implements I {\n" +
					"	public static void main(String[] args) {\n" +
					"		X x = new X();\n" +
					"		x.foo((short)5, (short)10);\n" +
					"		x.bar((short)5, (short)10);\n" +
					"	}\n" +
					"	public void foo(short s, int i) {} // No error, but should have been\n" +
					"	public void bar(short s, int i) {} // Correctly reported\n" +
					"\n" +
					"}\n" +
					"interface I {\n" +
					"	public default void foo(int i, short s) {}\n" +
					"}\n" +
					"class Base {\n" +
					"	public void bar(int i, short s) {}\n" +
					"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	x.foo((short)5, (short)10);\n" +
			"	  ^^^\n" +
			"The method foo(short, int) is ambiguous for the type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	x.bar((short)5, (short)10);\n" +
			"	  ^^^\n" +
			"The method bar(short, int) is ambiguous for the type X\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425719, [1.8][compiler] Bogus ambiguous call error from compiler
	public void test425719() throws Exception {
		this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"   default void foo(Object obj) {\n" +
					"	   System.out.println(\"interface method\");\n" +
					"   }\n" +
					"}\n" +
					"class Base {\n" +
					"    public void foo(Object obj) {\n" +
					"        System.out.println(\"class method\");\n" +
					"   }\n" +
					"}\n" +
					"public class X extends Base implements I {\n" +
					"	 public static void main(String argv[]) {\n" +
					"	    	new X().foo(null);\n" +
					"	    }\n" +
					"}\n",
			},
			"class method");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425718, [1.8] default method changes access privilege of protected overridden method from Object
	public void test425718() throws Exception {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"   default Object clone() { return null; };\n" +
					"}\n" +
					"public class X  {\n" +
					"    public void foo() {\n" +
					"        I x = new I(){};\n" +
					"        System.out.println(x.clone());\n" +
					"    }\n" +
					"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	I x = new I(){};\n" +
			"	          ^^^\n" +
			"The inherited method Object.clone() cannot hide the public abstract method in I\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	I x = new I(){};\n" +
			"	          ^^^\n" +
			"Exception CloneNotSupportedException in throws clause of Object.clone() is not compatible with I.clone()\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426318, [1.8][compiler] Bogus name clash error in the presence of default methods and varargs
	public void test426318() throws Exception {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"abstract class Y { \n" +
					"    public abstract void foo(Object[] x);\n" +
					"    public abstract void goo(Object[] x);\n" +
					"}\n" +
					"interface I {\n" +
					"   default public <T> void foo(T... x) {};\n" +
					"   public abstract <T> void goo(T ... x);\n" +
					"}\n" +
					"public abstract class X extends Y implements I { \n" +
					"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 6)\n" +
			"	default public <T> void foo(T... x) {};\n" +
			"	                                 ^\n" +
			"Type safety: Potential heap pollution via varargs parameter x\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 7)\n" +
			"	public abstract <T> void goo(T ... x);\n" +
			"	                                   ^\n" +
			"Type safety: Potential heap pollution via varargs parameter x\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424914, [1.8][compiler] No error shown for method reference with super enclosed in an interface
	public void test424914() throws Exception {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"interface A {\n" +
					"	String foo();\n" +
					"	String b = super.toString();\n" +
					"	default void fun1() {\n" +
					"		System.out.println((A) super::toString);\n" +
					"		super.toString();\n" +
					"		Object.super.toString();\n" +
					"	}\n" +
					"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	String b = super.toString();\n" +
			"	           ^^^^^\n" +
			"Cannot use super in a static context\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	System.out.println((A) super::toString);\n" +
			"	                       ^^^^^\n" +
			"super reference is illegal in interface context\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	super.toString();\n" +
			"	^^^^^\n" +
			"super reference is illegal in interface context\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	Object.super.toString();\n" +
			"	^^^^^^^^^^^^\n" +
			"No enclosing instance of the type Object is accessible in scope\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424914, [1.8][compiler] No error shown for method reference with super enclosed in an interface
	public void test424914a() throws Exception {
		this.runConformTest(
			new String[] {
					"X.java",
					"interface B {\n" +
					"	default void foo() {\n" +
					"		System.out.println(\"B.foo\");\n" +
					"	}\n" +
					"}\n" +
					"interface A extends B {\n" +
					"	default void foo() {\n" +
					"		System.out.println(\"A.foo\");\n" +
					"		B.super.foo();\n" +
					"	}\n" +
					"}\n" +
					"public class X implements A {\n" +
					"	public static void main(String[] args) {\n" +
					"		A a = new X();\n" +
					"		a.foo();\n" +
					"	}\n" +
					"}\n",
			},
			"A.foo\n" +
			"B.foo");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427478, [1.8][compiler] Wrong "Duplicate default methods" error on AbstractDoubleSpliterator
	public void test427478() throws Exception { // extracted smaller test case.
		this.runConformTest(
			new String[] {
					"X.java",
					"import java.util.function.Consumer;\n" +
					"import java.util.function.DoubleConsumer;\n" +
					"public interface X<T> {\n" +
					"    default void forEachRemaining(Consumer<? super T> action) {\n" +
					"    }\n" +
					"    public interface OfPrimitive<T, T_CONS, T_SPLITR extends OfPrimitive<T, T_CONS, T_SPLITR>> extends X<T> {\n" +
					"        default void forEachRemaining(T_CONS action) {\n" +
					"        }\n" +
					"    }\n" +
					"    public interface OfDouble extends OfPrimitive<Double, DoubleConsumer, OfDouble> {\n" +
					"        default void forEachRemaining(Consumer<? super Double> action) {\n" +
					"        }\n" +
					"        default void forEachRemaining(DoubleConsumer action) {\n" +
					"        }\n" +
					"    }\n" +
					"}\n" +
					"abstract class AbstractDoubleSpliterator implements X.OfDouble {\n" +
					"}\n",
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427478, [1.8][compiler] Wrong "Duplicate default methods" error on AbstractDoubleSpliterator
	public void test427478a() throws Exception { // full test case.
		this.runConformTest(
			new String[] {
					"Spliterator.java",
					"import java.util.function.Consumer;\n" +
					"import java.util.function.DoubleConsumer;\n" +
					"public interface Spliterator<T> {\n" +
					"    default void forEachRemaining(Consumer<? super T> action) {\n" +
					"    }\n" +
					"    public interface OfPrimitive<T, T_CONS, T_SPLITR extends OfPrimitive<T, T_CONS, T_SPLITR>>\n" +
					"            extends Spliterator<T> {\n" +
					"        // overloads Spliterator#forEachRemaining(Consumer<? super T>)\n" +
					"        default void forEachRemaining(T_CONS action) {\n" +
					"        }\n" +
					"    }\n" +
					"    public interface OfDouble extends OfPrimitive<Double, DoubleConsumer, OfDouble> {\n" +
					"        @Override // the method from Spliterator\n" +
					"        default void forEachRemaining(Consumer<? super Double> action) {\n" +
					"        }\n" +
					"        \n" +
					"        @Override // the method from OfPrimitive\n" +
					"        default void forEachRemaining(DoubleConsumer action) {\n" +
					"        }\n" +
					"    }\n" +
					"}\n" +
					"class Spliterators {\n" +
					"    /* Error on class: Duplicate default methods named forEachRemaining with\n" +
					"     * the parameters (Consumer<? super Double>) and (Consumer<? super T>) are\n" +
					"     * inherited from the types Spliterator.OfDouble and Spliterator<Double>\n" +
					"     */\n" +
					"    public abstract static class AbstractDoubleSpliterator implements Spliterator.OfDouble {\n" +
					"        /* Implementation that prevents the compile error: */\n" +
					"//        @Override // the method from Spliterator\n" +
					"//        public void forEachRemaining(Consumer<? super Double> action) {\n" +
					"//        }\n" +
					"    }\n" +
					"}\n",
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423467, [1.8][compiler] wrong error for functional interface with @Override default method
	public void test423467() throws Exception { // full test case.
		this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"    int foo(String s);\n" +
					"}\n" +
					"@FunctionalInterface\n" +
					"interface A extends I { // wrong compile error (A *is* a functional interface)\n" +
					"    @Override\n" +
					"    default int foo(String s) {\n" +
					"        return -1;\n" +
					"    }\n" +
					"    Integer foo(java.io.Serializable s);\n" +
					"}\n" +
					"public class X {\n" +
					"    A a = (s) -> 10;\n" +
					"}\n" +
					"@FunctionalInterface\n" +
					"interface B { // OK\n" +
					"    default int foo(String s) {\n" +
					"        return -1;\n" +
					"    }\n" +
					"    Integer foo(java.io.Serializable s);\n" +
					"}\n"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=438471, Java 1.8 functional interface rejected if it extends an interface which overrides another interface's method
	public void test438471() throws Exception {
		this.runConformTest(
			new String[] {
				"Bar.java",
				"@FunctionalInterface\n" +
				"public interface Bar extends Overridden {\n" +
					"	void foo();\n" +
					"	@Override\n" +
					"	default void close() {\n" +
					"		System.out.println(\"bar\");\n" +
					"	}\n" +
					"}\n" +
					"\n" +
					"interface Overridden extends AutoCloseable {\n" +
					"	// Works without this overridden method\n" +
					"	@Override\n" +
					"	void close();\n" +
					"}"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=436350, [1.8][compiler] Missing bridge method in interface results in AbstractMethodError
	public void test436350() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"    }\n" +
				"}\n" +
				"interface GenericInterface<T> {\n" +
				"	T reduce(Integer i);\n" +
				"}\n" +
				"interface DoubleInterface extends GenericInterface<Double> {\n" +
				"	default Double reduce(Integer i) {\n" +
				"		return 0.0;\n" +
				"	}\n" +
				"	double reduce(String s);\n" +
				"}\n", // =================
			},
			"");
		// 	ensure bridge methods are generated in interfaces.
		String expectedOutput =
				"  public bridge synthetic java.lang.Object reduce(java.lang.Integer arg0);\n" +
				"    0  aload_0 [this]\n" +
				"    1  aload_1 [arg0]\n" +
				"    2  invokeinterface DoubleInterface.reduce(java.lang.Integer) : java.lang.Double [24] [nargs: 2]\n" +
				"    7  areturn\n";

		File f = new File(OUTPUT_DIR + File.separator + "DoubleInterface.class");
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
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=436350, [1.8][compiler] Missing bridge method in interface results in AbstractMethodError
	public void test436350a() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Iterator;\n" +
				"import java.util.PrimitiveIterator;\n" +
				"import java.util.PrimitiveIterator.OfDouble;\n" +
				"/**\n" +
				" * @author Tobias Grasl\n" +
				" */\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		final double[] doubles = new double[]{1,2,3};\n" +
				"		OfDouble doubleIterator = new DoubleArrayIterator(doubles);\n" +
				"		Double value = new Reducer<Double>().reduce(doubleIterator, new DoubleInterface() {\n" +
				"			@Override\n" +
				"			public double reduce(OfDouble iterator_) {\n" +
				"				double sum = 0;\n" +
				"				while(iterator_.hasNext()) {\n" +
				"					sum += iterator_.nextDouble();\n" +
				"				}\n" +
				"				return sum;\n" +
				"			}\n" +
				"		});\n" +
				"		System.out.println(\"Anonymous class value: \"+value);\n" +
				"		doubleIterator = new DoubleArrayIterator(doubles);\n" +
				"		value = new Reducer<Double>().reduce(doubleIterator, (DoubleInterface) iterator_ -> {\n" +
				"			double sum = 0;\n" +
				"			while(iterator_.hasNext()) {\n" +
				"				sum += iterator_.nextDouble();\n" +
				"			}\n" +
				"			return sum;\n" +
				"		});\n" +
				"		System.out.println(\"Lambda expression value: \"+value);\n" +
				"	}\n" +
				"	private static class DoubleArrayIterator implements PrimitiveIterator.OfDouble {\n" +
				"		int index = 0;\n" +
				"		private double[] _doubles;\n" +
				"		public DoubleArrayIterator(double[] doubles_) {\n" +
				"			_doubles = doubles_;\n" +
				"		}\n" +
				"		@Override\n" +
				"		public boolean hasNext() {\n" +
				"			return index < _doubles.length;\n" +
				"		}\n" +
				"		@Override\n" +
				"		public double nextDouble() {\n" +
				"			return _doubles[index++];\n" +
				"		}\n" +
				"	};\n" +
				"	interface GenericInterface<T> {\n" +
				"		T reduce(Iterator<T> iterator_);\n" +
				"	}\n" +
				"	interface DoubleInterface extends GenericInterface<Double> {\n" +
				"		default Double reduce(Iterator<Double> iterator_) {\n" +
				"			if(iterator_ instanceof PrimitiveIterator.OfDouble) {\n" +
				"				return reduce((PrimitiveIterator.OfDouble)iterator_);\n" +
				"			}\n" +
				"			return Double.NaN;\n" +
				"		};\n" +
				"		double reduce(PrimitiveIterator.OfDouble iterator_);\n" +
				"	}\n" +
				"	static class Reducer<T> {\n" +
				"		T reduce(Iterator<T> iterator_, GenericInterface<T> reduction_) {\n" +
				"			return reduction_.reduce(iterator_);\n" +
				"		}\n" +
				"	}\n" +
				"}\n", // =================
			},
			"Anonymous class value: 6.0\n" +
			"Lambda expression value: 6.0");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=437522, [1.8][compiler] Missing compile error in Java 8 mode for Interface.super.field access
	public void testBug437522() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"interface T {\n" +
				"    int f = 0;\n" +
				"    void foo();\n" +
				"    default String def() { return \"T.def\"; }\n" +
				"}\n" +
				"class S {\n" +
				"    public static final int f = 0;\n" +
				"}\n" +
				"class C extends S implements T {\n" +
				"    @Override\n" +
				"    public void foo() {\n" +
				"        System.out.println(T.super.f); // no error in Java 8 (wrong) without fix\n" +
				"        System.out.println(T.super.def()); // new JLS8 15.12.1 form (OK)\n" +
				"        System.out.println(S.super.f); // compile error (correct)\n" +
				"    }\n" +
				"}\n" +
				"class X {\n" +
				"    T f = new T() {\n" +
				"        @Override\n" +
				"        public void foo() {\n" +
				"            System.out.println(T.super.f); // no error in Java 8 (wrong) without fix\n" +
				"        }\n" +
				"    };\n" +
				"}\n" +
				"class Y { int f = 1;}\n" +
				"class Z extends Y {\n" +
				"	int foo2() { return super.f;}\n" +
				"	static int foo() { return super.f;}\n" +
				"}\n" +
				"interface T2 { int f = 0; }\n" +
				"class X2  implements T2 {	\n" +
				"	int i = T2.super.f;\n" +
				"}\n" +
				"interface T3 { int f = 0; }\n" +
				"interface T4 extends T3 { int f = 0; }\n" +
				"class X3  implements T4 {	\n" +
				"	int i = T4.super.f;\n" +
				"}\n" +
				"interface T5 { int f = 0; }\n" +
				"class X5 implements T5 {	\n" +
				"	static int i = T5.super.f;\n" +
				"}\n" +
				"interface T6 { int f = 0; }\n" +
				"class X6 implements T6 {	\n" +
				"	static int i = T6.super.f;\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	System.out.println(T.super.f); // no error in Java 8 (wrong) without fix\n" +
			"	                   ^^^^^^^\n" +
			"No enclosing instance of the type T is accessible in scope\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 14)\n" +
			"	System.out.println(S.super.f); // compile error (correct)\n" +
			"	                   ^^^^^^^\n" +
			"No enclosing instance of the type S is accessible in scope\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 21)\n" +
			"	System.out.println(T.super.f); // no error in Java 8 (wrong) without fix\n" +
			"	                   ^^^^^^^\n" +
			"No enclosing instance of the type T is accessible in scope\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 28)\n" +
			"	static int foo() { return super.f;}\n" +
			"	                          ^^^^^\n" +
			"Cannot use super in a static context\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 32)\n" +
			"	int i = T2.super.f;\n" +
			"	        ^^^^^^^^\n" +
			"No enclosing instance of the type T2 is accessible in scope\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 37)\n" +
			"	int i = T4.super.f;\n" +
			"	        ^^^^^^^^\n" +
			"No enclosing instance of the type T4 is accessible in scope\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 41)\n" +
			"	static int i = T5.super.f;\n" +
			"	               ^^^^^^^^\n" +
			"Cannot use super in a static context\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 41)\n" +
			"	static int i = T5.super.f;\n" +
			"	               ^^^^^^^^\n" +
			"No enclosing instance of the type T5 is accessible in scope\n" +
			"----------\n" +
			"9. ERROR in X.java (at line 45)\n" +
			"	static int i = T6.super.f;\n" +
			"	               ^^^^^^^^\n" +
			"Cannot use super in a static context\n" +
			"----------\n" +
			"10. ERROR in X.java (at line 45)\n" +
			"	static int i = T6.super.f;\n" +
			"	               ^^^^^^^^\n" +
			"No enclosing instance of the type T6 is accessible in scope\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=437522, [1.8][compiler] Missing compile error in Java 8 mode for Interface.super.field access
	// Example JLS: 15.11.2-1.
	public void testBug437522a() throws Exception {
		runConformTest(
			true,
			new String[] {
				"X.java",
				"interface I  { int x = 0; }\n" +
				"class T1 implements I { int x = 1; }\n" +
				"class T2 extends T1   { int x = 2; }\n" +
				"class T3 extends T2 {\n" +
				"    int x = 3;\n" +
				"    void test() {\n" +
				"        System.out.println(\"x= \"          + x);\n" +
				"        System.out.println(\"super.x= \"    + super.x);\n" +
				"        System.out.println(\"((T2)this).x= \" + ((T2)this).x);\n" +
				"        System.out.println(\"((T1)this).x= \" + ((T1)this).x);\n" +
				"        System.out.println(\"((I)this).x= \"  + ((I)this).x);\n" +
				"    }\n" +
				"}\n" +
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        new T3().test();\n" +
				"    }\n" +
				"}\n",
			},
			null, null,
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	class T2 extends T1   { int x = 2; }\n" +
			"	                            ^\n" +
			"The field T2.x is hiding a field from type T1\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	int x = 3;\n" +
			"	    ^\n" +
			"The field T3.x is hiding a field from type T2\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 11)\n" +
			"	System.out.println(\"((I)this).x= \"  + ((I)this).x);\n" +
			"	                                                ^\n" +
			"The static field I.x should be accessed in a static way\n" +
			"----------\n",
			"x= 3\n" +
			"super.x= 2\n" +
			"((T2)this).x= 2\n"	+
			"((T1)this).x= 1\n" +
			"((I)this).x= 0",
			"", JavacTestOptions.DEFAULT);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=453552, Invalid '@FunctionalInterface error when two interfaces extend the same functional interface.
	public void test453552() throws Exception {
		this.runConformTest(
			new String[] {
				"FunctionalInterface1.java",
				"@FunctionalInterface\n" +
				"interface FunctionalInterface1 {\n" +
				"    void methodWithoutDefault();\n" +
				"}\n" +
				"@FunctionalInterface\n" +
				"interface FunctionalInterface2 extends FunctionalInterface1{}\n" +
				"@FunctionalInterface\n" +
				"interface FunctionalInterface3 extends FunctionalInterface1{}\n" +
				"@FunctionalInterface\n" +
				"interface FunctionalInterface4 extends FunctionalInterface2, FunctionalInterface3{}\n" +
				"@FunctionalInterface\n" +
				"interface RunnableFunctionalInterface extends Runnable, FunctionalInterface4{\n" +
				"	default void methodWithoutDefault(){\n" +
				"		// implements methodWithoutDefault\n" +
				"	}\n" +
				"}\n"
			});
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=453552, Invalid '@FunctionalInterface error when two interfaces extend the same functional interface.
	public void test453552_comment2() throws Exception {
		this.runConformTest(
			new String[] {
				"FI1.java",
				"interface FI1<T,R> {\n" +
				"	R call(T input);\n" +
				"}\n" +
				"interface FI2<U,V> {\n" +
				"	V call(U input);\n" +
				"}\n" +
				"@FunctionalInterface\n" +
				"interface FI3<X,Y> extends FI1<X,Y>, FI2<X,Y> {\n" +
				"	Y apply(X input);\n" +
				"\n" +
				"   @Override\n" +
				"   default Y call(X input) {\n" +
				"       return apply(input);\n" +
				"   }\n" +
				"}"
			});
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477891, [1.8] regression caused by the fix for bug 438812: order dependencies in analysis of default method inheritance
	public void test477891_comment0() throws Exception {
		this.runNegativeTest(
			new String[] {
				"D.java",
				"interface A {\n" +
				"    public default void display() {\n" +
				"        System.out.println(\"Display from A\");\n" +
				"    }\n" +
				"}\n" +
				"interface B extends A {\n" +
				"	@Override\n" +
				"    public default void display() {\n" +
				"        System.out.println(\"Display from B\");\n" +
				"    }\n" +
				"}\n" +
				"interface C extends A {\n" +
				"	@Override\n" +
				"    public void display();\n" +
				"}\n" +
				"public interface D extends B, C {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in D.java (at line 16)\n" +
			"	public interface D extends B, C {\n" +
			"	                 ^\n" +
			"The default method display() inherited from B conflicts with another method inherited from C\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477891, [1.8] regression caused by the fix for bug 438812: order dependencies in analysis of default method inheritance
	public void test477891_comment0_a() throws Exception {
		this.runNegativeTest(
			new String[] {
				"D.java",
				"interface A {\n" +
				"    public default void display() {\n" +
				"        System.out.println(\"Display from A\");\n" +
				"    }\n" +
				"}\n" +
				"interface B extends A {\n" +
				"	@Override\n" +
				"    public default void display() {\n" +
				"        System.out.println(\"Display from B\");\n" +
				"    }\n" +
				"}\n" +
				"interface C extends A {\n" +
				"	@Override\n" +
				"    public void display();\n" +
				"}\n" +
				"public interface D extends C, B {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in D.java (at line 16)\n" +
			"	public interface D extends C, B {\n" +
			"	                 ^\n" +
			"The default method display() inherited from B conflicts with another method inherited from C\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477891, [1.8] regression caused by the fix for bug 438812: order dependencies in analysis of default method inheritance
	public void test477891_comment3_a() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"import java.util.*;\n" +
				"interface Z<E> extends X<E>, Y<E> {\n" +
				"}\n" +
				"interface Y<E> extends AB<E> {\n" +
				"	@Override\n" +
				"	default Spliterator<E> spliterator() { return null; };\n" +
				"}\n" +
				"interface X<E> extends AB<E> {\n" +
				"	@Override\n" +
				"	Spliterator<E> spliterator();\n" +
				"}\n" +
				"interface AB<E> {\n" +
				"	default Spliterator<E> spliterator() { return null; }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Z.java (at line 2)\n" +
			"	interface Z<E> extends X<E>, Y<E> {\n" +
			"	          ^\n" +
			"The default method spliterator() inherited from Y<E> conflicts with another method inherited from X<E>\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477891, [1.8] regression caused by the fix for bug 438812: order dependencies in analysis of default method inheritance
	public void test477891_comment3_b() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"import java.util.*;\n" +
				"interface Z<E> extends Y<E>, X<E> {\n" +
				"}\n" +
				"interface Y<E> extends AB<E> {\n" +
				"	@Override\n" +
				"	default Spliterator<E> spliterator() { return null; };\n" +
				"}\n" +
				"interface X<E> extends AB<E> {\n" +
				"	@Override\n" +
				"	Spliterator<E> spliterator();\n" +
				"}\n" +
				"interface AB<E> {\n" +
				"	default Spliterator<E> spliterator() { return null; }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Z.java (at line 2)\n" +
			"	interface Z<E> extends Y<E>, X<E> {\n" +
			"	          ^\n" +
			"The default method spliterator() inherited from Y<E> conflicts with another method inherited from X<E>\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477891, [1.8] regression caused by the fix for bug 438812: order dependencies in analysis of default method inheritance
		public void test477891_comment3_c() throws Exception {
			this.runNegativeTest(
				new String[] {
					"Z.java",
					"import java.util.*;\n" +
					"interface Z<E> extends X<E>, Y<E> {\n" +
					"}\n" +
					"interface Y<E> extends AB<E> {\n" +
					"	@Override\n" +
					"	default Spliterator<E> spliterator() { return null; };\n" +
					"}\n" +
					"interface X<E> {\n" +
					"	Spliterator<E> spliterator();\n" +
					"}\n" +
					"interface AB<E> {\n" +
					"	default Spliterator<E> spliterator() { return null; }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in Z.java (at line 2)\n" +
				"	interface Z<E> extends X<E>, Y<E> {\n" +
				"	          ^\n" +
				"The default method spliterator() inherited from Y<E> conflicts with another method inherited from X<E>\n" +
				"----------\n");
		}
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477891, [1.8] regression caused by the fix for bug 438812: order dependencies in analysis of default method inheritance
		public void test477891_comment3_d() throws Exception {
			this.runNegativeTest(
				new String[] {
					"Z.java",
					"import java.util.*;\n" +
					"interface Z<E> extends Y<E>, X<E> {\n" +
					"}\n" +
					"interface Y<E> extends AB<E> {\n" +
					"	@Override\n" +
					"	default Spliterator<E> spliterator() { return null; };\n" +
					"}\n" +
					"interface X<E> {\n" +
					"	Spliterator<E> spliterator();\n" +
					"}\n" +
					"interface AB<E> {\n" +
					"	default Spliterator<E> spliterator() { return null; }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in Z.java (at line 2)\n" +
				"	interface Z<E> extends Y<E>, X<E> {\n" +
				"	          ^\n" +
				"The default method spliterator() inherited from Y<E> conflicts with another method inherited from X<E>\n" +
				"----------\n");
		}
	public void test458547_comment0_a() throws Exception {
		this.runNegativeTest(
			new String[] {
				"JavaTest.java",
				"public class JavaTest {\n" +
				"	interface A {\n" +
				"		default void foo() { }\n" +
				"	}\n" +
				"	interface B {\n" +
				"		void foo();\n" +
				"	}\n" +
				"	interface C {\n" +
				"		void foo();\n" +
				"	}\n" +
				"	interface D extends A, B {\n" +
				"		@Override default void foo() { }\n" +
				"	}\n" +
				"	class E implements A, B, C, D {\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in JavaTest.java (at line 14)\n" +
			"	class E implements A, B, C, D {\n" +
			"	      ^\n" +
			"The default method foo() inherited from JavaTest.D conflicts with another method inherited from JavaTest.C\n" +
			"----------\n");
	}
	public void test458547_comment0_b() throws Exception {
		this.runNegativeTest(
			new String[] {
				"JavaTest.java",
				"public class JavaTest {\n" +
				"	interface A {\n" +
				"		default void foo() { }\n" +
				"	}\n" +
				"	interface B {\n" +
				"		void foo();\n" +
				"	}\n" +
				"	interface C {\n" +
				"		void foo();\n" +
				"	}\n" +
				"	interface D extends A, B {\n" +
				"		@Override default void foo() { }\n" +
				"	}\n" +
				"	class E implements B, C, A, D {\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in JavaTest.java (at line 14)\n" +
			"	class E implements B, C, A, D {\n" +
			"	      ^\n" +
			"The default method foo() inherited from JavaTest.D conflicts with another method inherited from JavaTest.C\n" +
			"----------\n");
	}

	public void testBug539743() {
		runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	static <V> void m(V v) {\n" +
			"\n" +
			"	}\n" +
			"\n" +
			"	interface Foo {\n" +
			"		@SuppressWarnings(\"unchecked\")\n" +
			"		default <U> U f() {\n" +
			"			return (U) new Object();\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	static class Bar implements Foo {\n" +
			"		@SuppressWarnings(\"unchecked\")\n" +
			"		@Override\n" +
			"		public Object f() {\n" +
			"			m(Foo.super.f());\n" +
			"			return null;\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"");
	}
}
