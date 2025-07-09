/*******************************************************************************
 * Copyright (c) 2011, 2017 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class GrammarCoverageTests308 extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 35 };
//		TESTS_NAMES = new String [] { "testnew" };
	}
	public static Class testClass() {
		return GrammarCoverageTests308.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	public GrammarCoverageTests308(String testName){
		super(testName);
	}
	// Lone test to verify that multiple annotations of all three kinds are accepted. All other tests will use only marker annotations
	public void test000() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends @Marker @SingleMember(0) @Normal(Value = 0) Object {\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public class X extends @Marker @SingleMember(0) @Normal(Value = 0) Object {\n" +
				"	                        ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 1)\n" +
				"	public class X extends @Marker @SingleMember(0) @Normal(Value = 0) Object {\n" +
				"	                                ^^^^^^^^^^^^\n" +
				"SingleMember cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 1)\n" +
				"	public class X extends @Marker @SingleMember(0) @Normal(Value = 0) Object {\n" +
				"	                                                 ^^^^^^\n" +
				"Normal cannot be resolved to a type\n" +
				"----------\n");
	}
	// FieldDeclaration ::= Modifiersopt Type VariableDeclarators ';'
	public void test001() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    @Marker int x;\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",

					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
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
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	@Marker int x;\n" +
				"	^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// TYPE:   MethodHeaderName ::= Modifiersopt TypeParameters Type 'Identifier' '('
	public void test002() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    @Marker <T> @Marker int x() { return 10; };\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",

					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
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
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	@Marker <T> @Marker int x() { return 10; };\n" +
				"	^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 2)\n" +
				"	@Marker <T> @Marker int x() { return 10; };\n" +
				"	            ^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 3)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// TYPE:   MethodHeaderName ::= Modifiersopt Type 'Identifier' '('
	public void test003() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    @Marker int x() { return 10; };\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",

					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
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
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	@Marker int x() { return 10; };\n" +
				"	^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// FormalParameter ::= Modifiersopt Type VariableDeclaratorIdOrThis
	public void test004() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    int x(@Marker int p) { return 10; };\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",

					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
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
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	int x(@Marker int p) { return 10; };\n" +
				"	      ^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// FormalParameter ::= Modifiersopt Type PushZeroTypeAnnotations '...' VariableDeclaratorIdOrThis
	public void test005() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    int x(@Marker int ... p) { return 10; };\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",

					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
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
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	int x(@Marker int ... p) { return 10; };\n" +
				"	      ^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// FormalParameter ::= Modifiersopt Type @308... TypeAnnotations '...' VariableDeclaratorIdOrThis
	public void test006() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    int x(@Marker int [] @Marker ... p) { return 10; };\n" +
					"    Zork z;\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	int x(@Marker int [] @Marker ... p) { return 10; };\n" +
				"	       ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 2)\n" +
				"	int x(@Marker int [] @Marker ... p) { return 10; };\n" +
				"	                      ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 3)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// UnionType ::= Type
	// UnionType ::= UnionType '|' Type
	public void test007() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    int x() {\n" +
					"        try {\n" +
					"        } catch (@Marker NullPointerException | @Marker ArrayIndexOutOfBoundsException e) {\n" +
					"        }\n" +
					"        return 10;\n" +
					"    }\n" +
					"    Zork z;\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	} catch (@Marker NullPointerException | @Marker ArrayIndexOutOfBoundsException e) {\n" +
				"	          ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	} catch (@Marker NullPointerException | @Marker ArrayIndexOutOfBoundsException e) {\n" +
				"	                                         ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 8)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// LocalVariableDeclaration ::= Type PushModifiers VariableDeclarators
    // LocalVariableDeclaration ::= Modifiers Type PushRealModifiers VariableDeclarators
	public void test008() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    int x() {\n" +
					"        @Marker int p;\n" +
					"        final @Marker int q;\n" +
					"        @Marker final int r;\n" +
					"        return 10;\n" +
					"    }\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",

					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
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
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	@Marker int p;\n" +
				"	^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	final @Marker int q;\n" +
				"	      ^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	@Marker final int r;\n" +
				"	^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 8)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// Resource ::= Type PushModifiers VariableDeclaratorId EnterVariable '=' ForceNoDiet VariableInitializer RestoreDiet ExitVariableWithInitialization
	// Resource ::= Modifiers Type PushRealModifiers VariableDeclaratorId EnterVariable '=' ForceNoDiet VariableInitializer RestoreDiet ExitVariableWithInitialization
	public void test009() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    int x() {\n" +
					"        try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {\n" +
					"        }\n" +
					"        return 10;\n" +
					"    }\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",

					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
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
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {\n" +
				"	     ^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {\n" +
				"	             ^^^^^^^\n" +
				"The resource type Integer does not implement java.lang.AutoCloseable\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 3)\n" +
				"	try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {\n" +
				"	                                     ^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 3)\n" +
				"	try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {\n" +
				"	                                             ^^^^^^^\n" +
				"The resource type Integer does not implement java.lang.AutoCloseable\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 3)\n" +
				"	try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {\n" +
				"	                                                               ^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 3)\n" +
				"	try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {\n" +
				"	                                                                             ^^^^^^^\n" +
				"The resource type Integer does not implement java.lang.AutoCloseable\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 7)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// EnhancedForStatementHeaderInit ::= 'for' '(' Type PushModifiers Identifier Dimsopt
	// EnhancedForStatementHeaderInit ::= 'for' '(' Modifiers Type PushRealModifiers Identifier Dimsopt
	public void test010() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    int x() {\n" +
					"        for (@Marker int i: new int[3]) {}\n" +
					"        for (final @Marker int i: new int[3]) {}\n" +
					"        for (@Marker final int i: new int[3]) {}\n" +
					"        return 10;\n" +
					"    }\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",

					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
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
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	for (@Marker int i: new int[3]) {}\n" +
				"	     ^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	for (final @Marker int i: new int[3]) {}\n" +
				"	           ^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	for (@Marker final int i: new int[3]) {}\n" +
				"	     ^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 8)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// AnnotationMethodHeaderName ::= Modifiersopt TypeParameters Type 'Identifier' '('
	// AnnotationMethodHeaderName ::= Modifiersopt Type 'Identifier' '('
	public void test011() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public @interface X { \n" +
					"	public @Marker String value(); \n" +
					"	@Marker String value2(); \n" +
					"	@Marker public String value3(); \n" +
					"	public @Marker <T> @Marker String value4(); \n" +
					"	@Marker <T> @Marker String value5(); \n" +
					"	@Marker public <T> @Marker String value6(); \n" +
					"}\n" +

					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",

					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
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
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	public @Marker String value(); \n" +
				"	       ^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	@Marker String value2(); \n" +
				"	^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 4)\n" +
				"	@Marker public String value3(); \n" +
				"	^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 5)\n" +
				"	public @Marker <T> @Marker String value4(); \n" +
				"	       ^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 5)\n" +
				"	public @Marker <T> @Marker String value4(); \n" +
				"	                   ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 5)\n" +
				"	public @Marker <T> @Marker String value4(); \n" +
				"	                                  ^^^^^^^^\n" +
				"Annotation attributes cannot be generic\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 6)\n" +
				"	@Marker <T> @Marker String value5(); \n" +
				"	^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 6)\n" +
				"	@Marker <T> @Marker String value5(); \n" +
				"	            ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"9. ERROR in X.java (at line 6)\n" +
				"	@Marker <T> @Marker String value5(); \n" +
				"	                           ^^^^^^^^\n" +
				"Annotation attributes cannot be generic\n" +
				"----------\n" +
				"10. ERROR in X.java (at line 7)\n" +
				"	@Marker public <T> @Marker String value6(); \n" +
				"	^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"11. ERROR in X.java (at line 7)\n" +
				"	@Marker public <T> @Marker String value6(); \n" +
				"	                   ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"12. ERROR in X.java (at line 7)\n" +
				"	@Marker public <T> @Marker String value6(); \n" +
				"	                                  ^^^^^^^^\n" +
				"Annotation attributes cannot be generic\n" +
				"----------\n");
	}
	// PrimaryNoNewArray ::= PrimitiveType Dims '.' 'class'
	// PrimaryNoNewArray ::= PrimitiveType '.' 'class'
	public void test012() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X { \n" +
					"	public void value() {\n" +
					"		Object o = @Marker int.class;\n" +
					"		Object o2 = @Marker int @Marker[] [] @Marker[].class;\n" +
					"   }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	Object o = @Marker int.class;\n" +
				"	           ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	Object o2 = @Marker int @Marker[] [] @Marker[].class;\n" +
				"	            ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 4)\n" +
				"	Object o2 = @Marker int @Marker[] [] @Marker[].class;\n" +
				"	                        ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 4)\n" +
				"	Object o2 = @Marker int @Marker[] [] @Marker[].class;\n" +
				"	                                     ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// ReferenceExpression ::= PrimitiveType Dims '::' NonWildTypeArgumentsopt IdentifierOrNew
	public void test013() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"interface I {\n" +
					"    Object copy(int [] ia);\n" +
					"}\n" +
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        I i = @Marker int @Marker []::<String>clone;\n" +
					"        Zork z;\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	I i = @Marker int @Marker []::<String>clone;\n" +
				"	       ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	I i = @Marker int @Marker []::<String>clone;\n" +
				"	                   ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"3. WARNING in X.java (at line 6)\n" +
				"	I i = @Marker int @Marker []::<String>clone;\n" +
				"	                               ^^^^^^\n" +
				"Unused type arguments for the non generic method clone() of type Object; it should not be parameterized with arguments <String>\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 7)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// ArrayCreationWithoutArrayInitializer ::= 'new' PrimitiveType DimWithOrWithOutExprs
	// ArrayCreationWithArrayInitializer ::= 'new' PrimitiveType DimWithOrWithOutExprs ArrayInitializer
	public void test014() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        int i [] = new @Marker int @Marker [4];\n" +
					"        int j [] = new @Marker int @Marker [] { 10 };\n" +
					"        Zork z;\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	int i [] = new @Marker int @Marker [4];\n" +
				"	                ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	int i [] = new @Marker int @Marker [4];\n" +
				"	                            ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 4)\n" +
				"	int j [] = new @Marker int @Marker [] { 10 };\n" +
				"	                ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 4)\n" +
				"	int j [] = new @Marker int @Marker [] { 10 };\n" +
				"	                            ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 5)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// CastExpression ::= PushLPAREN PrimitiveType Dimsopt PushRPAREN InsideCastExpression UnaryExpression
	public void test015() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        int i = (@Marker int) 0;\n" +
					"        int j [] = (@Marker int @Marker []) null;\n" +
					"        Zork z;\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	int i = (@Marker int) 0;\n" +
				"	          ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	int j [] = (@Marker int @Marker []) null;\n" +
				"	             ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 4)\n" +
				"	int j [] = (@Marker int @Marker []) null;\n" +
				"	                         ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 5)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// InstanceofExpression ::= InstanceofExpression 'instanceof' ReferenceType
	public void test016() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        if (args instanceof @Readonly String) {\n" +
					"        }\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	if (args instanceof @Readonly String) {\n" +
				"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Incompatible conditional operand types String[] and String\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	if (args instanceof @Readonly String) {\n" +
				"	                     ^^^^^^^^\n" +
				"Readonly cannot be resolved to a type\n" +
				"----------\n");
	}
	// TypeArgument ::= ReferenceType
	public void test017() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends Y<@Marker Integer, String> {}\n" +
					"class Y<T, V> {\n" +
				    "    Zork z;\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public class X extends Y<@Marker Integer, String> {}\n" +
				"	                          ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// ReferenceType1 ::= ReferenceType '>'
	public void test018() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends Y<@Marker Integer> {}\n" +
					"class Y<T> {\n" +
				    "    Zork z;\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public class X extends Y<@Marker Integer> {}\n" +
				"	                          ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}

	// ReferenceType2 ::= ReferenceType '>>'
	public void test019() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X<T extends Object & Comparable<? super @Marker String>> {}\n" +
					"class Y<T> {\n" +
				    "    Zork z;\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public class X<T extends Object & Comparable<? super @Marker String>> {}\n" +
				"	                                                      ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// ReferenceType3 ::= ReferenceType '>>>'
	public void test020() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X<A extends X<X<X<@Marker String>>>> {}\n" +
					"class Y<T> {\n" +
				    "    Zork z;\n" +
					"}\n"
 				},
 				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public class X<A extends X<X<X<@Marker String>>>> {}\n" +
				"	                           ^\n" +
				"Bound mismatch: The type X<X<String>> is not a valid substitute for the bounded parameter <A extends X<X<X<String>>>> of the type X<A>\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 1)\n" +
				"	public class X<A extends X<X<X<@Marker String>>>> {}\n" +
				"	                             ^\n" +
				"Bound mismatch: The type X<String> is not a valid substitute for the bounded parameter <A extends X<X<X<String>>>> of the type X<A>\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 1)\n" +
				"	public class X<A extends X<X<X<@Marker String>>>> {}\n" +
				"	                               ^^^^^^^^^^^^^^\n" +
				"Bound mismatch: The type String is not a valid substitute for the bounded parameter <A extends X<X<X<String>>>> of the type X<A>\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 1)\n" +
				"	public class X<A extends X<X<X<@Marker String>>>> {}\n" +
				"	                                ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 3)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// WildcardBounds ::= 'extends' ReferenceType
	// WildcardBounds ::= 'super' ReferenceType
	public void test021() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" +
					"   void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" +
				"	         ^^^\n" +
				"Map cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 2)\n" +
				"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" +
				"	              ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 2)\n" +
				"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" +
				"	                              ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 2)\n" +
				"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" +
				"	                                              ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 2)\n" +
				"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" +
				"	                                                                ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 3)\n" +
				"	void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" +
				"	         ^^^\n" +
				"Map cannot be resolved to a type\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 3)\n" +
				"	void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" +
				"	              ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 3)\n" +
				"	void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" +
				"	                                ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"9. ERROR in X.java (at line 3)\n" +
				"	void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" +
				"	                                                ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"10. ERROR in X.java (at line 3)\n" +
				"	void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" +
				"	                                                                ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	// TypeParameter ::= TypeParameterHeader 'extends' ReferenceType
	public void test022() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" +
					"}\n" +
					"class Y<T> {}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" +
				"	                 ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 1)\n" +
				"	public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" +
				"	                                   ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 1)\n" +
				"	public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" +
				"	                                             ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 1)\n" +
				"	public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" +
				"	                                                         ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"5. WARNING in X.java (at line 1)\n" +
				"	public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" +
				"	                                                                          ^^^^^^^^^^^^^^^\n" +
				"The type parameter Q should not be bounded by the final type Integer. Final types cannot be further extended\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 1)\n" +
				"	public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" +
				"	                                                                           ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	// TypeParameter ::= TypeParameterHeader 'extends' ReferenceType AdditionalBoundList
	// AdditionalBound ::= '&' ReferenceType
	// TypeParameter1 ::= TypeParameterHeader 'extends' ReferenceType AdditionalBoundList1
	public void test023() throws Exception {
		this.runNegativeTest(
				new String[] {
					"I.java",
					"public interface I<U extends J<? extends I<U>>> {\n" +
					"}\n" +
					"interface J<T extends I<? extends J<T>>> {\n" +
					"}\n" +
					"class CI<U extends CJ<T, U> & @Marker J<@Marker T>,\n" +
					"			T extends CI<U, T> & @Marker I<U>>\n" +
					"	implements I<U> {\n" +
					"}\n" +
					"class CJ<T extends CI<U, T> & @Marker I<@Marker U>,\n" +
					"			U extends CJ<T, U> & J<T>>\n" +
					"	implements J<T> {\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in I.java (at line 5)\n" +
				"	class CI<U extends CJ<T, U> & @Marker J<@Marker T>,\n" +
				"	                               ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in I.java (at line 5)\n" +
				"	class CI<U extends CJ<T, U> & @Marker J<@Marker T>,\n" +
				"	                                         ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in I.java (at line 6)\n" +
				"	T extends CI<U, T> & @Marker I<U>>\n" +
				"	                      ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in I.java (at line 9)\n" +
				"	class CJ<T extends CI<U, T> & @Marker I<@Marker U>,\n" +
				"	                               ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"5. ERROR in I.java (at line 9)\n" +
				"	class CJ<T extends CI<U, T> & @Marker I<@Marker U>,\n" +
				"	                                         ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	// InstanceofExpression_NotName ::= Name 'instanceof' ReferenceType
	public void test024() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X<E> {\n" +
					"  class Y {\n" +
					"    E e;\n" +
					"    E getOtherElement(Object other) {\n" +
					"      if (!(other instanceof @Marker X<?>.Y)) {};\n" +
					"      return null;\n" +
					"    }\n" +
					"  }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	if (!(other instanceof @Marker X<?>.Y)) {};\n" +
				"	                        ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	// InstanceofExpression_NotName ::= InstanceofExpression_NotName 'instanceof' ReferenceType
	public void test025() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X<P, C> {\n" +
					"  public X() {\n" +
					"    if (!(this instanceof @Marker X)) {}\n" +
					"  }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	if (!(this instanceof @Marker X)) {}\n" +
				"	                       ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	// ReferenceExpressionTypeArgumentsAndTrunk ::= OnlyTypeArguments '.' ClassOrInterfaceType Dimsopt
	public void test026() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"interface I {\n" +
					"    void foo(Y<String>.Z z, int x);\n" +
					"}\n" +
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        I i = Y<String>.@Marker Z::foo;\n" +
					"        i.foo(new Y<String>().new Z(), 10); \n" +
					"        Zork z;\n" +
					"    }\n" +
					"}\n" +
					"class Y<T> {\n" +
					"    class Z {\n" +
					"        void foo(int x) {\n" +
					"	    System.out.println(x);\n" +
					"        }\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	I i = Y<String>.@Marker Z::foo;\n" +
				"	                 ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// ArrayCreationWithoutArrayInitializer ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs
	// ArrayCreationWithArrayInitializer ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs ArrayInitializer
	public void test027() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        X [] x = new @Marker X @Marker [5];\n" +
					"        X [] x2 = new @Marker X @Marker [] { null };\n" +
					"        Zork z;\n" +
					"    }\n" +
					"}\n"				},
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	X [] x = new @Marker X @Marker [5];\n" +
					"	              ^^^^^^\n" +
					"Marker cannot be resolved to a type\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 3)\n" +
					"	X [] x = new @Marker X @Marker [5];\n" +
					"	                        ^^^^^^\n" +
					"Marker cannot be resolved to a type\n" +
					"----------\n" +
					"3. ERROR in X.java (at line 4)\n" +
					"	X [] x2 = new @Marker X @Marker [] { null };\n" +
					"	               ^^^^^^\n" +
					"Marker cannot be resolved to a type\n" +
					"----------\n" +
					"4. ERROR in X.java (at line 4)\n" +
					"	X [] x2 = new @Marker X @Marker [] { null };\n" +
					"	                         ^^^^^^\n" +
					"Marker cannot be resolved to a type\n" +
					"----------\n" +
					"5. ERROR in X.java (at line 5)\n" +
					"	Zork z;\n" +
					"	^^^^\n" +
					"Zork cannot be resolved to a type\n" +
					"----------\n");
	}
	// CastExpression ::= PushLPAREN Name OnlyTypeArgumentsForCastExpression '.' ClassOrInterfaceType Dimsopt PushRPAREN InsideCastExpressionWithQualifiedGenerics UnaryExpressionNotPlusMinus
	public void test028() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        java.util.Map.Entry [] e = (java.util.Map<String, String>.@Marker Entry []) null;\n" +
					"    }\n" +
					"}\n"				},
					"----------\n" +
					"1. WARNING in X.java (at line 3)\n" +
					"	java.util.Map.Entry [] e = (java.util.Map<String, String>.@Marker Entry []) null;\n" +
					"	^^^^^^^^^^^^^^^^^^^\n" +
					"Map.Entry is a raw type. References to generic type Map.Entry<K,V> should be parameterized\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 3)\n" +
					"	java.util.Map.Entry [] e = (java.util.Map<String, String>.@Marker Entry []) null;\n" +
					"	                            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"The member type Map.Entry<K,V> cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type Map<String,String>\n" +
					"----------\n" +
					"3. ERROR in X.java (at line 3)\n" +
					"	java.util.Map.Entry [] e = (java.util.Map<String, String>.@Marker Entry []) null;\n" +
					"	                                                           ^^^^^^\n" +
					"Marker cannot be resolved to a type\n" +
					"----------\n");
	}
	// ReferenceType1 ::= ClassOrInterface '<' TypeArgumentList2
	public void test029() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.io.Serializable;\n" +
					"import java.util.List;\n" +
					"public class X<T extends Comparable<T> & Serializable> {\n" +
					"	void foo(List<? extends @Marker Comparable<T>> p) {} \n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	void foo(List<? extends @Marker Comparable<T>> p) {} \n" +
				"	                         ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	// ReferenceType2 ::= ClassOrInterface '<' TypeArgumentList3
	public void test030() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"class Base {\n" +
					"}\n" +
					"class Foo<U extends Base, V extends Bar<U, @Marker Foo<U, V>>> {\n" +
					"}\n" +
					"class Bar<E extends Base, F extends Foo<E, @Marker Bar<E, F>>> {\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	class Foo<U extends Base, V extends Bar<U, @Marker Foo<U, V>>> {\n" +
				"	                                           ^^^^^^^^^^^\n" +
				"Bound mismatch: The type Foo<U,V> is not a valid substitute for the bounded parameter <F extends Foo<E,Bar<E,F>>> of the type Bar<E,F>\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	class Foo<U extends Base, V extends Bar<U, @Marker Foo<U, V>>> {\n" +
				"	                                            ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	class Bar<E extends Base, F extends Foo<E, @Marker Bar<E, F>>> {\n" +
				"	                                           ^^^^^^^^^^^\n" +
				"Bound mismatch: The type Bar<E,F> is not a valid substitute for the bounded parameter <V extends Bar<U,Foo<U,V>>> of the type Foo<U,V>\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 5)\n" +
				"	class Bar<E extends Base, F extends Foo<E, @Marker Bar<E, F>>> {\n" +
				"	                                            ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	// ClassHeaderExtends ::= 'extends' ClassType
	public void test031() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends @Marker Object {\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public class X extends @Marker Object {\n" +
				"	                        ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	// ClassInstanceCreationExpression ::= 'new' OnlyTypeArguments ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' UnqualifiedClassBodyopt
	// ClassInstanceCreationExpression ::= 'new' ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' UnqualifiedClassBodyopt
	public void test032() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    X x = new @Marker X();\n" +
					"    X y = new <String> @Marker X();\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	X x = new @Marker X();\n" +
				"	           ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 3)\n" +
				"	X y = new <String> @Marker X();\n" +
				"	           ^^^^^^\n" +
				"Unused type arguments for the non generic constructor X() of type X; it should not be parameterized with arguments <String>\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 3)\n" +
				"	X y = new <String> @Marker X();\n" +
				"	                    ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	// ClassInstanceCreationExpression ::= Primary '.' 'new' OnlyTypeArguments ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
	// ClassInstanceCreationExpression ::= Primary '.' 'new' ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
	public void test033() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    class Y {\n" +
					"    }\n" +
					"    Y y1 = new @Marker X().new @Marker Y();\n" +
					"    Y y2 = new @Marker X().new <String> @Marker Y();\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	Y y1 = new @Marker X().new @Marker Y();\n" +
				"	            ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	Y y1 = new @Marker X().new @Marker Y();\n" +
				"	                            ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	Y y2 = new @Marker X().new <String> @Marker Y();\n" +
				"	            ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"4. WARNING in X.java (at line 5)\n" +
				"	Y y2 = new @Marker X().new <String> @Marker Y();\n" +
				"	                            ^^^^^^\n" +
				"Unused type arguments for the non generic constructor X.Y() of type X.Y; it should not be parameterized with arguments <String>\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 5)\n" +
				"	Y y2 = new @Marker X().new <String> @Marker Y();\n" +
				"	                                     ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	// ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
	// ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' OnlyTypeArguments ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
	public void test034() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    X x;\n" +
					"    class Y {\n" +
					"    }\n" +
					"    Y y1 = @Marker x.new @Marker Y();\n" +
					"    Y y2 = @Marker x.new <String> @Marker Y();\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	Y y1 = @Marker x.new @Marker Y();\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	Y y1 = @Marker x.new @Marker Y();\n" +
				"	                      ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	Y y2 = @Marker x.new <String> @Marker Y();\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"4. WARNING in X.java (at line 6)\n" +
				"	Y y2 = @Marker x.new <String> @Marker Y();\n" +
				"	                      ^^^^^^\n" +
				"Unused type arguments for the non generic constructor X.Y() of type X.Y; it should not be parameterized with arguments <String>\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 6)\n" +
				"	Y y2 = @Marker x.new <String> @Marker Y();\n" +
				"	                               ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	// MethodHeaderThrowsClause ::= 'throws' ClassTypeList
	// ClassTypeList -> ClassTypeElt
	// ClassTypeList ::= ClassTypeList ',' ClassTypeElt
	// ClassTypeElt ::= ClassType
	public void test035() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    void foo() throws @Marker NullPointerException, @Marker ArrayIndexOutOfBoundsException {}\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	void foo() throws @Marker NullPointerException, @Marker ArrayIndexOutOfBoundsException {}\n" +
				"	                   ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 2)\n" +
				"	void foo() throws @Marker NullPointerException, @Marker ArrayIndexOutOfBoundsException {}\n" +
				"	                                                 ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	// ClassHeaderImplements ::= 'implements' InterfaceTypeList
	// InterfaceHeaderExtends ::= 'extends' InterfaceTypeList
	// InterfaceTypeList -> InterfaceType
	// InterfaceTypeList ::= InterfaceTypeList ',' InterfaceType
	// InterfaceType ::= ClassOrInterfaceType
	public void test036() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"interface I {}\n" +
					"interface J {}\n" +
					"interface K extends @Marker I, @Marker J {}\n" +
					"interface L {}\n" +
					"public class X implements @Marker K, @Marker L {\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	interface K extends @Marker I, @Marker J {}\n" +
				"	                     ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	interface K extends @Marker I, @Marker J {}\n" +
				"	                                ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	public class X implements @Marker K, @Marker L {\n" +
				"	                           ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 5)\n" +
				"	public class X implements @Marker K, @Marker L {\n" +
				"	                                      ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	// ReferenceExpression ::= Name Dimsopt '::' NonWildTypeArgumentsopt IdentifierOrNew
	public void test037() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"interface I {\n" +
					"    void foo(int x);\n" +
					"}\n" +
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;\n" +
					"        i.foo(10); \n" +
					"        Zork z;\n" +
					"    }\n" +
					"}\n" +
					"class Y {\n" +
					"    static class Z {\n" +
					"        public static void foo(int x) {\n" +
					"	    System.out.println(x);\n" +
					"        }\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;\n" +
				"	      ^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;\n" +
				"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"The type Y.Z[][][] does not define foo(int) that is applicable here\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;\n" +
				"	       ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;\n" +
				"	                  ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 6)\n" +
				"	I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;\n" +
				"	                            ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 6)\n" +
				"	I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;\n" +
				"	                                          ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 8)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	// ReferenceExpression ::= Name BeginTypeArguments ReferenceExpressionTypeArgumentsAndTrunk '::' NonWildTypeArgumentsopt IdentifierOrNew
	public void test038() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"interface I {\n" +
					"    Y foo(int x);\n" +
					"}\n" +
					"public class X  {\n" +
					"    class Z extends Y {\n" +
					"        public Z(int x) {\n" +
					"            super(x);\n" +
					"            System.out.println();\n" +
					"        }\n" +
					"    }\n" +
					"    public static void main(String [] args) {\n" +
					"        i = @Marker W<@Marker Integer>::<@Marker String> new;\n" +
					"    }\n" +
					"}\n" +
					"class W<T> extends Y {\n" +
					"    public W(T x) {\n" +
					"        super(0);\n" +
					"        System.out.println(x);\n" +
					"    }\n" +
					"}\n" +
					"class Y {\n" +
					"    public Y(int x) {\n" +
					"        System.out.println(x);\n" +
					"    }\n" +
					"}\n"


				},
				"----------\n" +
				"1. ERROR in X.java (at line 12)\n" +
				"	i = @Marker W<@Marker Integer>::<@Marker String> new;\n" +
				"	^\n" +
				"i cannot be resolved to a variable\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 12)\n" +
				"	i = @Marker W<@Marker Integer>::<@Marker String> new;\n" +
				"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"The target type of this expression must be a functional interface\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 12)\n" +
				"	i = @Marker W<@Marker Integer>::<@Marker String> new;\n" +
				"	     ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 12)\n" +
				"	i = @Marker W<@Marker Integer>::<@Marker String> new;\n" +
				"	               ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 12)\n" +
				"	i = @Marker W<@Marker Integer>::<@Marker String> new;\n" +
				"	                                  ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	// CastExpression ::= PushLPAREN Name PushRPAREN InsideCastExpressionLL1 UnaryExpressionNotPlusMinus
	// CastExpression ::= PushLPAREN Name Dims PushRPAREN InsideCastExpression UnaryExpressionNotPlusMinus
	// CastExpression ::= PushLPAREN Name OnlyTypeArgumentsForCastExpression Dimsopt PushRPAREN InsideCastExpression UnaryExpressionNotPlusMinus
	// CastExpression ::= PushLPAREN Name OnlyTypeArgumentsForCastExpression '.' ClassOrInterfaceType Dimsopt PushRPAREN InsideCastExpressionWithQualifiedGenerics UnaryExpressionNotPlusMinus
	public void test039() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X  {\n" +
					"    Object o = (@Marker X) null;\n" +
					"    Object p = (@Marker X @Marker []) null;\n" +
					"    Object q = (@Marker java. @Marker util. @Marker List<@Marker String> []) null;\n" +
					"    Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	Object o = (@Marker X) null;\n" +
				"	             ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	Object p = (@Marker X @Marker []) null;\n" +
				"	             ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 3)\n" +
				"	Object p = (@Marker X @Marker []) null;\n" +
				"	                       ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 4)\n" +
				"	Object q = (@Marker java. @Marker util. @Marker List<@Marker String> []) null;\n" +
				"	            ^^^^^^^\n" +
				"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 4)\n" +
				"	Object q = (@Marker java. @Marker util. @Marker List<@Marker String> []) null;\n" +
				"	             ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 4)\n" +
				"	Object q = (@Marker java. @Marker util. @Marker List<@Marker String> []) null;\n" +
				"	                          ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 4)\n" +
				"	Object q = (@Marker java. @Marker util. @Marker List<@Marker String> []) null;\n" +
				"	                                         ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 4)\n" +
				"	Object q = (@Marker java. @Marker util. @Marker List<@Marker String> []) null;\n" +
				"	                                                      ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"9. ERROR in X.java (at line 5)\n" +
				"	Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;\n" +
				"	            ^^^^^^^\n" +
				"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
				"----------\n" +
				"10. ERROR in X.java (at line 5)\n" +
				"	Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;\n" +
				"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"The member type Map.Entry<K,V> cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type Map<String,String>\n" +
				"----------\n" +
				"11. ERROR in X.java (at line 5)\n" +
				"	Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;\n" +
				"	             ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"12. ERROR in X.java (at line 5)\n" +
				"	Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;\n" +
				"	                          ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"13. ERROR in X.java (at line 5)\n" +
				"	Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;\n" +
				"	                                       ^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n" +
				"14. ERROR in X.java (at line 5)\n" +
				"	Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;\n" +
				"	                                        ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"15. ERROR in X.java (at line 5)\n" +
				"	Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;\n" +
				"	                                                    ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"16. ERROR in X.java (at line 5)\n" +
				"	Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;\n" +
				"	                                                                    ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"17. ERROR in X.java (at line 5)\n" +
				"	Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;\n" +
				"	                                                                                    ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"18. ERROR in X.java (at line 5)\n" +
				"	Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;\n" +
				"	                                                                                                  ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
}
