/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.codeassist.select.SelectionJavadoc;
import org.eclipse.jdt.internal.codeassist.select.SelectionParser;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

import junit.framework.Test;

/**
 * Class to test selection in Javadoc comments.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=54968"
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SelectionJavadocTest extends AbstractSelectionTest {

	String source;
	ICompilationUnit unit;
	StringBuffer result;

	public SelectionJavadocTest(String testName) {
		super(testName);
	}

	static {
//		TESTS_NUMBERS = new int[] { 9, 10 };
//		TESTS_RANGE = new int[] { 26, -1 };
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(SelectionJavadocTest.class);
	}

	class JavadocSelectionVisitor extends ASTVisitor {

		public boolean visit(ConstructorDeclaration constructor, ClassScope scope) {
			if (constructor.javadoc != null) {
				assertTrue("Invalid type for Javadoc on " + constructor, constructor.javadoc instanceof SelectionJavadoc);
				SelectionJavadocTest.this.result.append(constructor.javadoc.toString());
			}
			return super.visit(constructor, scope);
		}

		public boolean visit(FieldDeclaration field, MethodScope scope) {
			if (field.javadoc != null) {
				assertTrue("Invalid type for Javadoc on " + field, field.javadoc instanceof SelectionJavadoc);
				SelectionJavadocTest.this.result.append(field.javadoc.toString());
			}
			return super.visit(field, scope);
		}

		public boolean visit(MethodDeclaration method, ClassScope scope) {
			if (method.javadoc != null) {
				assertTrue("Invalid type for Javadoc on " + method, method.javadoc instanceof SelectionJavadoc);
				SelectionJavadocTest.this.result.append(method.javadoc.toString());
			}
			return super.visit(method, scope);
		}

		public boolean visit(TypeDeclaration type, BlockScope scope) {
			if (type.javadoc != null) {
				assertTrue("Invalid type for Javadoc on " + type, type.javadoc instanceof SelectionJavadoc);
				SelectionJavadocTest.this.result.append(type.javadoc.toString());
			}
			return super.visit(type, scope);
		}

		public boolean visit(TypeDeclaration type, ClassScope scope) {
			if (type.javadoc != null) {
				assertTrue("Invalid type for Javadoc on " + type, type.javadoc instanceof SelectionJavadoc);
				SelectionJavadocTest.this.result.append(type.javadoc.toString());
			}
			return super.visit(type, scope);
		}

		public boolean visit(TypeDeclaration type, CompilationUnitScope scope) {
			if (type.javadoc != null) {
				assertTrue("Invalid type for Javadoc on " + type, type.javadoc instanceof SelectionJavadoc);
				SelectionJavadocTest.this.result.append(type.javadoc.toString());
			}
			return super.visit(type, scope);
		}
	}

	protected void assertValid(String expected) {
		String actual = this.result.toString();
		if (!actual.equals(expected)) {
			System.out.println("Expected result for test "+testName()+":");
			System.out.println(Util.displayString(actual, 3));
			System.out.println("	source: [");
			System.out.print(Util.indentString(this.source, 2));
			System.out.println("]\n");
			assertEquals("Invalid selection node", expected, actual);
		}
	}
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.unit = null;
	}

	void setUnit(String name, String source) {
		this.source = source;
		this.unit = new CompilationUnit(source.toCharArray(), name, null);
		this.result = new StringBuffer();
	}

	/*
	 * Parse a method with selectionNode check
	 */
	protected CompilationResult  findJavadoc(String selection) {
		return findJavadoc(selection, 1);
	}

	protected CompilationResult findJavadoc(String selection, int occurences) {

		// Verify unit
		assertNotNull("Missing compilation unit!", this.unit);

		// Get selection start and end
		int selectionStart = this.source.indexOf(selection);
		int length = selection.length();
		int selectionEnd = selectionStart + length - 1;
		for (int i = 1; i < occurences; i++) {
			selectionStart = this.source.indexOf(selection, selectionEnd);
			selectionEnd = selectionStart + length - 1;
		}

		// Parse unit
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		SelectionParser parser = new SelectionParser(new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
			options,
			new DefaultProblemFactory(Locale.getDefault())));
		CompilationUnitDeclaration unitDecl = parser.dietParse(this.unit, new CompilationResult(this.unit, 0, 0, 0), selectionStart, selectionEnd);
		parser.getMethodBodies(unitDecl);

		// Visit compilation unit declaration to find javadoc
		unitDecl.traverse(new JavadocSelectionVisitor(), unitDecl.scope);

		// Return the unit declaration result
		return unitDecl.compilationResult();
	}

	@Override
	protected Map getCompilerOptions() {
	    Map optionsMap = super.getCompilerOptions();
		optionsMap.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.WARNING);
		optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.ENABLED);
	    return optionsMap;
    }

	public void test01() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/** @see #foo() */\n" +
			"	void bar() {\n" +
			"		foo();\n" +
			"	}\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		findJavadoc("foo");
		assertValid("/**<SelectOnMethod:#foo()>*/\n");
	}

	public void test02() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/** {@link #foo() foo} */\n" +
			"	void bar() {\n" +
			"		foo();\n" +
			"	}\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		findJavadoc("foo");
		assertValid("/**<SelectOnMethod:#foo()>*/\n");
	}

	public void test03() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/** @see Test */\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		findJavadoc("Test", 2);
		assertValid("/**<SelectOnType:Test>*/\n");
	}

	public void test04() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/** Javadoc {@link Test} */\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		findJavadoc("Test", 2);
		assertValid("/**<SelectOnType:Test>*/\n");
	}

	public void test05() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	int field;\n" +
			"	/** @see #field */\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		findJavadoc("field", 2);
		assertValid("/**<SelectOnField:#field>*/\n");
	}

	public void test06() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	int field;\n" +
			"	/**{@link #field}*/\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		findJavadoc("field", 2);
		assertValid("/**<SelectOnField:#field>*/\n");
	}

	public void test07() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/**\n" +
			"	 * @see Test#field\n" +
			"	 * @see #foo(int, String)\n" +
			"	 * @see Test#foo(int, String)\n" +
			"	 */\n" +
			"	void bar() {\n" +
			"		foo(0, \"\");\n" +
			"	}\n" +
			"	int field;\n" +
			"	void foo(int x, String s) {}\n" +
			"}\n"
		);
		findJavadoc("foo");
		findJavadoc("String");
		findJavadoc("Test", 2);
		findJavadoc("foo", 2);
		findJavadoc("String", 2);
		findJavadoc("Test", 3);
		findJavadoc("field");
		assertValid(
			"/**<SelectOnMethod:#foo(int , String )>*/\n" +
			"/**<SelectOnType:String>*/\n" +
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnMethod:Test#foo(int , String )>*/\n" +
			"/**<SelectOnType:String>*/\n" +
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnField:Test#field>*/\n"
		);
	}

	public void test08() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/**\n" +
			"	 * First {@link #foo(int, String)}\n" +
			"	 * Second {@link Test#foo(int, String) method foo}\n" +
			"	 * Third {@link Test#field field}\n" +
			"	 */\n" +
			"	void bar() {\n" +
			"		foo(0, \"\");\n" +
			"	}\n" +
			"	int field;\n" +
			"	void foo(int x, String s) {}\n" +
			"}\n"
		);
		findJavadoc("foo");
		findJavadoc("String");
		findJavadoc("Test", 2);
		findJavadoc("foo", 2);
		findJavadoc("String", 2);
		findJavadoc("Test", 3);
		findJavadoc("field");
		assertValid(
			"/**<SelectOnMethod:#foo(int , String )>*/\n" +
			"/**<SelectOnType:String>*/\n" +
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnMethod:Test#foo(int , String )>*/\n" +
			"/**<SelectOnType:String>*/\n" +
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnField:Test#field>*/\n"
		);
	}

	public void test09() {
		setUnit("test/junit/Test.java",
			"package test.junit;\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * @see test.junit.Test\n" +
			"	 * @see test.junit.Test#field\n" +
			"	 * @see test.junit.Test#foo(Object[] array)\n" +
			"	 */\n" +
			"	void bar() {\n" +
			"		foo(null);\n" +
			"	}\n" +
			"	int field;\n" +
			"	void foo(Object[] array) {}\n" +
			"}\n"
		);
		findJavadoc("test", 2);
		findJavadoc("junit", 2);
		findJavadoc("Test", 2);
		findJavadoc("test", 3);
		findJavadoc("junit", 3);
		findJavadoc("Test", 3);
		findJavadoc("field");
		findJavadoc("test", 4);
		findJavadoc("junit", 4);
		findJavadoc("Test", 4);
		findJavadoc("foo");
		findJavadoc("Object");
		findJavadoc("array");
		assertValid(
			"/**<SelectOnType:test>*/\n" +
			"/**<SelectOnType:test.junit>*/\n" +
			"/**<SelectOnType:test.junit.Test>*/\n" +
			"/**<SelectOnType:test>*/\n" +
			"/**<SelectOnType:test.junit>*/\n" +
			"/**<SelectOnType:test.junit.Test>*/\n" +
			"/**<SelectOnField:test.junit.Test#field>*/\n" +
			"/**<SelectOnType:test>*/\n" +
			"/**<SelectOnType:test.junit>*/\n" +
			"/**<SelectOnType:test.junit.Test>*/\n" +
			"/**<SelectOnMethod:test.junit.Test#foo(Object[] array)>*/\n" +
			"/**<SelectOnType:Object>*/\n" +
			"/**\n" +
			" */\n"
		);
	}

	public void test10() {
		setUnit("test/junit/Test.java",
			"package test.junit;\n" +
			"public class Test {\n" +
			"	/** Javadoc {@linkplain test.junit.Test}\n" +
			"	 * {@linkplain test.junit.Test#field field}\n" +
			"	 * last line {@linkplain test.junit.Test#foo(Object[] array) foo(Object[])}\n" +
			"	 */\n" +
			"	void bar() {\n" +
			"		foo(null);\n" +
			"	}\n" +
			"	int field;\n" +
			"	void foo(Object[] array) {}\n" +
			"}\n"
		);
		findJavadoc("test", 2);
		findJavadoc("junit", 2);
		findJavadoc("Test", 2);
		findJavadoc("test", 3);
		findJavadoc("junit", 3);
		findJavadoc("Test", 3);
		findJavadoc("field");
		findJavadoc("test", 4);
		findJavadoc("junit", 4);
		findJavadoc("Test", 4);
		findJavadoc("foo");
		findJavadoc("Object");
		findJavadoc("array");
		assertValid(
			"/**<SelectOnType:test>*/\n" +
			"/**<SelectOnType:test.junit>*/\n" +
			"/**<SelectOnType:test.junit.Test>*/\n" +
			"/**<SelectOnType:test>*/\n" +
			"/**<SelectOnType:test.junit>*/\n" +
			"/**<SelectOnType:test.junit.Test>*/\n" +
			"/**<SelectOnField:test.junit.Test#field>*/\n" +
			"/**<SelectOnType:test>*/\n" +
			"/**<SelectOnType:test.junit>*/\n" +
			"/**<SelectOnType:test.junit.Test>*/\n" +
			"/**<SelectOnMethod:test.junit.Test#foo(Object[] array)>*/\n" +
			"/**<SelectOnType:Object>*/\n" +
			"/**\n" +
			" */\n"
		);
	}

	public void test11() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/**\n" +
			"	 * @throws RuntimeException runtime exception\n" +
			"	 * @throws InterruptedException interrupted exception\n" +
			"	 */\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		findJavadoc("RuntimeException");
		findJavadoc("InterruptedException");
		assertValid(
			"/**<SelectOnType:RuntimeException>*/\n" +
			"/**<SelectOnType:InterruptedException>*/\n"
		);
	}

	public void test12() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/**\n" +
			"	 * @exception RuntimeException runtime exception\n" +
			"	 * @exception InterruptedException interrupted exception\n" +
			"	 */\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		findJavadoc("RuntimeException");
		findJavadoc("InterruptedException");
		assertValid(
			"/**<SelectOnType:RuntimeException>*/\n" +
			"/**<SelectOnType:InterruptedException>*/\n"
		);
	}

	public void test13() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/**\n" +
			"	 * @param xxx integer param\n" +
			"	 * @param str string param\n" +
			"	 */\n" +
			"	void foo(int xxx, String str) {}\n" +
			"}\n"
		);
		findJavadoc("xxx");
		findJavadoc("str");
		assertValid(
			"/**<SelectOnLocalVariable:xxx>*/\n" +
			"/**<SelectOnLocalVariable:str>*/\n"
		);
	}

	public void test14() {
		setUnit("Test.java",
			"/**\n" +
			" * Javadoc of {@link Test}\n" +
			" * @see Field#foo\n" +
			" */\n" +
			"public class Test {}\n" +
			"/**\n" +
			" * Javadoc on {@link Field} to test selection in javadoc field references\n" +
			" * @see #foo\n" +
			" */\n" +
			"class Field {\n" +
			"	/**\n" +
			"	 * Javadoc on {@link #foo} to test selection in javadoc field references\n" +
			"	 * @see #foo\n" +
			"	 * @see Field#foo\n" +
			"	 */\n" +
			"	int foo;\n" +
			"}\n"
		);
		findJavadoc("Field");
		findJavadoc("foo");
		findJavadoc("Field", 2);
		findJavadoc("foo", 2);
		findJavadoc("foo", 3);
		findJavadoc("foo", 4);
		findJavadoc("Field", 4);
		findJavadoc("foo", 5);
		assertValid(
			"/**<SelectOnType:Field>*/\n" +
			"/**<SelectOnField:Field#foo>*/\n" +
			"/**<SelectOnType:Field>*/\n" +
			"/**<SelectOnField:#foo>*/\n" +
			"/**<SelectOnField:#foo>*/\n" +
			"/**<SelectOnField:#foo>*/\n" +
			"/**<SelectOnType:Field>*/\n" +
			"/**<SelectOnField:Field#foo>*/\n"
		);
	}

	public void test15() {
		setUnit("Test.java",
			"/**\n" +
			" * Javadoc of {@link Test}\n" +
			" * @see Method#foo(int, String)\n" +
			" */\n" +
			"public class Test {}\n" +
			"/**\n" +
			" * Javadoc on {@link Method} to test selection in javadoc method references\n" +
			" * @see #foo(int, String)\n" +
			" */\n" +
			"class Method {\n" +
			"	/**\n" +
			"	 * Javadoc on {@link #foo(int,String)} to test selection in javadoc method references\n" +
			"	 * @see #foo(int, String)\n" +
			"	 * @see Method#foo(int, String)\n" +
			"	 */\n" +
			"	void bar() {}\n" +
			"	/**\n" +
			"	 * Method with parameter and throws clause to test selection in javadoc\n" +
			"	 * @param xxx TODO\n" +
			"	 * @param str TODO\n" +
			"	 * @throws RuntimeException blabla\n" +
			"	 * @throws InterruptedException bloblo\n" +
			"	 */\n" +
			"	void foo(int xxx, String str) throws RuntimeException, InterruptedException {}\n" +
			"}\n"
		);
		findJavadoc("Method");
		findJavadoc("foo");
		findJavadoc("Method", 2);
		findJavadoc("foo", 2);
		findJavadoc("foo", 3);
		findJavadoc("foo", 4);
		findJavadoc("Method", 4);
		findJavadoc("foo", 5);
		findJavadoc("xxx");
		findJavadoc("str");
		findJavadoc("RuntimeException");
		findJavadoc("InterruptedException");
		assertValid(
			"/**<SelectOnType:Method>*/\n" +
			"/**<SelectOnMethod:Method#foo(int , String )>*/\n" +
			"/**<SelectOnType:Method>*/\n" +
			"/**<SelectOnMethod:#foo(int , String )>*/\n" +
			"/**<SelectOnMethod:#foo(int , String )>*/\n" +
			"/**<SelectOnMethod:#foo(int , String )>*/\n" +
			"/**<SelectOnType:Method>*/\n" +
			"/**<SelectOnMethod:Method#foo(int , String )>*/\n" +
			"/**<SelectOnLocalVariable:xxx>*/\n" +
			"/**<SelectOnLocalVariable:str>*/\n" +
			"/**<SelectOnType:RuntimeException>*/\n" +
			"/**<SelectOnType:InterruptedException>*/\n"
		);
	}

	public void test16() {
		setUnit("Test.java",
			"/**\n" +
			" * Javadoc of {@link Test}\n" +
			" * @see Other\n" +
			" */\n" +
			"public class Test {}\n" +
			"/**\n" +
			" * Javadoc of {@link Other}\n" +
			" * @see Test\n" +
			" */\n" +
			"class Other {}\n"
		);
		findJavadoc("Test");
		findJavadoc("Other");
		findJavadoc("Test", 3);
		findJavadoc("Other", 2);
		assertValid(
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnType:Other>*/\n" +
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnType:Other>*/\n"
		);
	}

	public void test17() {
		setUnit("Test.java",
			"/**\n" +
			" * @see Test.Field#foo\n" +
			" */\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * @see Field#foo\n" +
			"	 */\n" +
			"	class Field {\n" +
			"		/**\n" +
			"		 * @see #foo\n" +
			"		 * @see Field#foo\n" +
			"		 * @see Test.Field#foo\n" +
			"		 */\n" +
			"		int foo;\n" +
			"	}\n" +
			"}\n"
		);
		findJavadoc("Test");
		findJavadoc("Field");
		findJavadoc("foo");
		findJavadoc("Field", 2);
		findJavadoc("foo", 2);
		findJavadoc("foo", 3);
		findJavadoc("Field", 4);
		findJavadoc("foo", 4);
		findJavadoc("Test", 3);
		findJavadoc("Field", 5);
		findJavadoc("foo", 5);
		assertValid(
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnType:Test.Field>*/\n" +
			"/**<SelectOnField:Test.Field#foo>*/\n" +
			"/**<SelectOnType:Field>*/\n" +
			"/**<SelectOnField:Field#foo>*/\n" +
			"/**<SelectOnField:#foo>*/\n" +
			"/**<SelectOnType:Field>*/\n" +
			"/**<SelectOnField:Field#foo>*/\n" +
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnType:Test.Field>*/\n" +
			"/**<SelectOnField:Test.Field#foo>*/\n"
		);
	}

	public void test18() {
		setUnit("Test.java",
			"/**\n" +
			" * @see Test.Method#foo()\n" +
			" */\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * @see Method#foo()\n" +
			"	 */\n" +
			"	class Method {\n" +
			"		/**\n" +
			"		 * @see #foo()\n" +
			"		 * @see Method#foo()\n" +
			"		 * @see Test.Method#foo()\n" +
			"		 */\n" +
			"		void foo() {}\n" +
			"	}\n" +
			"}"
		);
		findJavadoc("Test");
		findJavadoc("Method");
		findJavadoc("foo");
		findJavadoc("Method", 2);
		findJavadoc("foo", 2);
		findJavadoc("foo", 3);
		findJavadoc("Method", 4);
		findJavadoc("foo", 4);
		findJavadoc("Test", 3);
		findJavadoc("Method", 5);
		findJavadoc("foo", 5);
		assertValid(
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnType:Test.Method>*/\n" +
			"/**<SelectOnMethod:Test.Method#foo()>*/\n" +
			"/**<SelectOnType:Method>*/\n" +
			"/**<SelectOnMethod:Method#foo()>*/\n" +
			"/**<SelectOnMethod:#foo()>*/\n" +
			"/**<SelectOnType:Method>*/\n" +
			"/**<SelectOnMethod:Method#foo()>*/\n" +
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnType:Test.Method>*/\n" +
			"/**<SelectOnMethod:Test.Method#foo()>*/\n"
		);
	}

	public void test19() {
		setUnit("Test.java",
			"/**\n" +
			" * @see Test.Other\n" +
			" */\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * @see Test\n" +
			"	 * @see Other\n" +
			"	 * @see Test.Other\n" +
			"	 */\n" +
			"	class Other {}\n" +
			"}"
		);
		findJavadoc("Test");
		findJavadoc("Other");
		findJavadoc("Test", 3);
		findJavadoc("Other", 2);
		findJavadoc("Test", 4);
		findJavadoc("Other", 3);
		assertValid(
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnType:Test.Other>*/\n" +
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnType:Other>*/\n" +
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnType:Test.Other>*/\n"
		);
	}

	public void test20() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	void bar() {\n" +
			"		/**\n" +
			"		 * @see Field#foo\n" +
			"		 */\n" +
			"		class Field {\n" +
			"			/**\n" +
			"			 * @see #foo\n" +
			"			 * @see Field#foo\n" +
			"			 */\n" +
			"			int foo;\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		);
		findJavadoc("Field");
		findJavadoc("foo");
		findJavadoc("foo", 2);
		findJavadoc("Field", 3);
		findJavadoc("foo", 3);
		assertValid(
			"/**<SelectOnType:Field>*/\n" +
			"/**<SelectOnField:Field#foo>*/\n" +
			"/**<SelectOnField:#foo>*/\n" +
			"/**<SelectOnType:Field>*/\n" +
			"/**<SelectOnField:Field#foo>*/\n"
		);
	}

	public void test21() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	void bar() {\n" +
			"		/**\n" +
			"		 * @see Method#foo()\n" +
			"		 */\n" +
			"		class Method {\n" +
			"			/**\n" +
			"			 * @see #foo()\n" +
			"			 * @see Method#foo()\n" +
			"			 */\n" +
			"			void foo() {}\n" +
			"		}\n" +
			"	}\n" +
			"}"
		);
		findJavadoc("Method");
		findJavadoc("foo");
		findJavadoc("foo", 2);
		findJavadoc("Method", 3);
		findJavadoc("foo", 3);
		assertValid(
			"/**<SelectOnType:Method>*/\n" +
			"/**<SelectOnMethod:Method#foo()>*/\n" +
			"/**<SelectOnMethod:#foo()>*/\n" +
			"/**<SelectOnType:Method>*/\n" +
			"/**<SelectOnMethod:Method#foo()>*/\n"
		);
	}

	public void test22() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	void bar() {\n" +
			"		/**\n" +
			"		 * @see Test\n" +
			"		 * @see Other\n" +
			"		 */\n" +
			"		class Other {}\n" +
			"	}\n" +
			"}"
		);
		findJavadoc("Test", 2);
		findJavadoc("Other");
		assertValid(
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnType:Other>*/\n"
		);
	}

	public void test23() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	void bar() {\n" +
			"		new Object() {\n" +
			"			/**\n" +
			"			 * @see Field#foo\n" +
			"			 */\n" +
			"			class Field {\n" +
			"				/**\n" +
			"				 * @see #foo\n" +
			"				 * @see Field#foo\n" +
			"				 */\n" +
			"				int foo;\n" +
			"			}\n" +
			"		};\n" +
			"	}\n" +
			"}\n"
		);
		findJavadoc("Field");
		findJavadoc("foo");
		findJavadoc("foo", 2);
		findJavadoc("Field", 3);
		findJavadoc("foo", 3);
		assertValid(
			"/**<SelectOnType:Field>*/\n" +
			"/**<SelectOnField:Field#foo>*/\n" +
			"/**<SelectOnField:#foo>*/\n" +
			"/**<SelectOnType:Field>*/\n" +
			"/**<SelectOnField:Field#foo>*/\n"
		);
	}

	public void test24() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	void bar() {\n" +
			"		new Object() {\n" +
			"			/**\n" +
			"			 * @see Method#foo()\n" +
			"			 */\n" +
			"			class Method {\n" +
			"				/**\n" +
			"				 * @see #foo()\n" +
			"				 * @see Method#foo()\n" +
			"				 */\n" +
			"				void foo() {}\n" +
			"			}\n" +
			"		};\n" +
			"	}\n" +
			"}"
		);
		findJavadoc("Method");
		findJavadoc("foo");
		findJavadoc("foo", 2);
		findJavadoc("Method", 3);
		findJavadoc("foo", 3);
		assertValid(
			"/**<SelectOnType:Method>*/\n" +
			"/**<SelectOnMethod:Method#foo()>*/\n" +
			"/**<SelectOnMethod:#foo()>*/\n" +
			"/**<SelectOnType:Method>*/\n" +
			"/**<SelectOnMethod:Method#foo()>*/\n"
		);
	}

	public void test25() {
		setUnit("Test.java",
			"public class Test {\n" +
			"	void bar() {\n" +
			"		new Object() {\n" +
			"			/**\n" +
			"			 * @see Test\n" +
			"			 * @see Other\n" +
			"			 */\n" +
			"			class Other {}\n" +
			"		};\n" +
			"	}\n" +
			"}"
		);
		findJavadoc("Test", 2);
		findJavadoc("Other");
		assertValid(
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnType:Other>*/\n"
		);
	}

	/**
	 * @bug 192449: [javadoc][assist] SelectionJavadocParser should not report problems
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=192449"
	 */
	public void test26() {
		setUnit("Test.java",
			"/**\n" +
			" * @see \n" +
			" * @throws noException\n" +
			" * @see Test\n" +
			" * @see Other\n" +
			" */\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * @see\n" +
			"	 * @param noParam\n" +
			"	 * @throws noException\n" +
			"	 */\n" +
			"	void bar() {}\n" +
			"}"
		);

		// parse and check results
		CompilationResult compilationResult = findJavadoc("Other");
		assertEquals("SelectionJavadocParser should not report errors", "", Util.getProblemLog(compilationResult, false, false));
	}
}
