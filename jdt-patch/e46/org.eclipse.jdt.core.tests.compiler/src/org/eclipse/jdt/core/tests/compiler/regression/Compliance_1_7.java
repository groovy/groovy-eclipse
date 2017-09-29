/*******************************************************************************
 * Copyright (c) 2011, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 390889 - [1.8][compiler] Evaluate options to support 1.7- projects against 1.8 JRE.
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Compliance_1_7 extends AbstractComparableTest {

public Compliance_1_7(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_7);
}
static {
// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_NAMES = new String[] { "Bug58069" };
// Numbers of tests to run: "test<number>" will be run for each number of this array
//	TESTS_NUMBERS = new int[] { 104 };
// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		TESTS_RANGE = new int[] { 85, -1 };
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=283225
public void test1() {
	this.runConformTest(
		new String[] {
			"p1/Z.java",
			"package p1;\n" +
			"import java.util.List;\n" +
			"public class Z  {\n" +
			"	@SafeVarargs\n" +
			"	public static <T> List<T> asList(T... a) {\n" + 
			"		return null;\n" + 
			"	}\n" +
			"}"
		},
		""); // no special vm args

		String computedReferences = findReferences(OUTPUT_DIR + "/p1/Z.class");
		boolean check = computedReferences.indexOf("annotationRef/SafeVarargs") >= 0;
		if (!check){
			System.out.println(computedReferences);
		}
		assertTrue("did not indexed the reference to SafeVarargs", check);
}
public void test2() {
	this.runConformTest(
		new String[] {
			"p2/Z.java",
			"package p2;\n" +
			"import java.lang.annotation.Inherited;\n" +
			"@Inherited\n" +
			"public @interface Z  {\n" +
			"}"
		},
		""); // no special vm args

		String computedReferences = findReferences(OUTPUT_DIR + "/p2/Z.class");
		boolean check = computedReferences.indexOf("annotationRef/Inherited") >= 0;
		if (!check){
			System.out.println(computedReferences);
		}
		assertTrue("did not indexed the reference to Inherited", check);
}
// Project with 1.7 compliance compiled against JRE 7, 8
// regular case
public void testBug390889_a() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_7);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_7);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_7);
	this.runConformTest(
			new String[] {
					"MyComp.java",
					"import java.util.Comparator;\n" + 
					"public class MyComp implements Comparator {\n" + 
					"	@Override\n" + 
					"	public int compare(Object o1, Object o2) {\n" + 
					"		return 0;\n" + 
					"	}\n" + 
					"}\n" + 
					"class MyStringComp implements Comparator<String> {\n" + 
					"	@Override\n" + 
					"	public int compare(String o1, String o2) {\n" + 
					"		return 0;\n" + 
					"	}\n" + 
					"}\n"
			},
			"",
			null /* no extra class libraries */,
			true /* flush output directory */,
			null,
			options,
			null/* do not perform statements recovery */);
}
// Project with 1.7 compliance compiled against JRE 8
// default method implements a regular abstract interface method
public void testBug390889_b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	runConformTest(
			new String[] {
				"I1.java",
				"interface I0 {\n" + 
				"  void foo();\n" + 
				"}\n" + 
				"public interface I1 extends I0 {\n" +
				"  @Override\n" + 
				"  default void foo() {}\n" + 
				"}\n"
			});

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_7);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_7);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_7);
	this.runConformTest(
			new String[] {
					"C1.java",
					"public class C1 implements I1 {\n" + 
					"}\n"
			},
			"",
			null /* no extra class libraries */,
			false /* don't flush output directory */,
			null,
			options,
			null/* do not perform statements recovery */);
}
// Project with 1.7 compliance compiled against JRE 7, 8
// assert that different forms of method invocation do not produce different result (as javac does)
public void testBug390889_c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	runConformTest(
			new String[] {
				"I.java",
				"interface I {\n" + 
				"  default void foo() {}\n" + 
				"}\n"
			});

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_7);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_7);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_7);
	this.runConformTest(
			new String[] {
				"CI.java",
				"public class CI implements I {\n" + 
				"	 void test(I i) {\n" + 
				"      this.foo();\n" + 
				"      i.foo();\n" + 
				"    }\n" + 
				"}\n"
			},
			"",
			null /* no extra class libraries */,
			false /* don't flush output directory */,
			null,
			options,
			null/* do not perform statements recovery */);
}
// Project with 1.7 compliance compiled against JRE 8
// assert that 1.8 constructs are not allowed at compliance 1.7
public void testBug490988() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_7);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_7);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_7);
	this.runNegativeTest(
			new String[] {
				"Thing.java",
				"import java.util.Comparator;\n" + 
				"import java.util.Iterator;\n" + 
				"public class Thing implements Iterator<Object> {\n" + 
				"    void breaking() {\n" + 
				"        Iterator.super.remove(); // not 1.7-compliant (must be an error)\n" + 
				"        Comparator.naturalOrder(); // not 1.7-compliant (bad error message)\n" + 
				"    }\n" + 
				"    @Override\n" + 
				"    public boolean hasNext() {\n" + 
				"        return false;\n" + 
				"    }\n" + 
				"    @Override\n" + 
				"    public Object next() {\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new Thing().breaking();\n" + 
				"    }\n" + 
				"}"
			},
			"----------\n" +
			"1. ERROR in Thing.java (at line 5)\n" +
			"	Iterator.super.remove(); // not 1.7-compliant (must be an error)\n" +
			"	^^^^^^^^^^^^^^\n" + 
			"Super method references to interface default methods are allowed only at source level 1.8 or above\n" +
			"----------\n" +
			"2. ERROR in Thing.java (at line 6)\n" +
			"	Comparator.naturalOrder(); // not 1.7-compliant (bad error message)\n" +
			"	           ^^^^^^^^^^^^\n" + 
			"References to interface static methods are allowed only at source level 1.8 or above\n" +
			"----------\n",
			null,
			false,
			options);
}
public static Class testClass() {
	return Compliance_1_7.class;
}
}
