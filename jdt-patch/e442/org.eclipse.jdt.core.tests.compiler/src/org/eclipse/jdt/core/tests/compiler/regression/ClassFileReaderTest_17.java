/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;

@SuppressWarnings({ "rawtypes" })
public class ClassFileReaderTest_17 extends AbstractRegressionTest {
	static {
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_17);
	}
	public static Class testClass() {
		return ClassFileReaderTest_17.class;
	}

	public ClassFileReaderTest_17(String name) {
		super(name);
	}

	// Needed to run tests individually from JUnit
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.complianceLevel = ClassFileConstants.JDK17;
	}

	public void testBug564227_001() throws Exception {
		String source =
				"sealed class X permits Y, Z{\n" +
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n" +
				"final class Y extends X{}\n" +
				"final class Z extends X{}\n";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		char[][] permittedSubtypesNames = classFileReader.getPermittedSubtypesNames();

		assertEquals(2, permittedSubtypesNames.length);

		char [][] expected = {"Y".toCharArray(), "Z".toCharArray()};
		assertTrue(CharOperation.equals(permittedSubtypesNames, expected));

	}
	public void testBug565782_001() throws Exception {
		String source =
				"sealed interface I {}\n"+
				"enum X implements I {\n"+
				"    ONE {};\n"+
				"    public static void main(String[] args) {\n"+
				"        System.out.println(0);\n"+
				"   }\n"+
				"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		char[][] permittedSubtypesNames = classFileReader.getPermittedSubtypesNames();

		assertEquals(1, permittedSubtypesNames.length);

		char [][] expected = {"X$1".toCharArray()};
		assertTrue(CharOperation.equals(permittedSubtypesNames, expected));

		int modifiers = classFileReader.getModifiers();
		assertTrue("sealed modifier expected", (modifiers & ExtraCompilerModifiers.AccSealed) != 0);
	}
	public void testBug565782_002() throws Exception {
		String source =
				"sealed interface I {}\n"+
				"class X {\n"+
				"	enum E implements I {\n"+
				"   	ONE {};\n"+
				"	}\n"+
				"   public static void main(String[] args) {\n"+
				"      	System.out.println(0);\n"+
				"   }\n"+
				"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X.E", "X$E", source);
		char[][] permittedSubtypesNames = classFileReader.getPermittedSubtypesNames();

		assertEquals(1, permittedSubtypesNames.length);

		char [][] expected = {"X$E$1".toCharArray()};
		assertTrue(CharOperation.equals(permittedSubtypesNames, expected));

		int modifiers = classFileReader.getModifiers();
		assertTrue("sealed modifier expected", (modifiers & ExtraCompilerModifiers.AccSealed) != 0);
	}
	public void testBug545510_1() throws Exception {
		String source =
				"strictfp class X {\n"+
				"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);

		int modifiers = classFileReader.getModifiers();
		assertTrue("strictfp modifier not expected", (modifiers & ClassFileConstants.AccStrictfp) == 0);
	}
	public void testBug545510_2() throws Exception {
		String source =
				"class X {\n"+
				"  strictfp void foo() {}\n"+
				"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		IBinaryMethod[] methods = classFileReader.getMethods();
		IBinaryMethod method = methods[1];
		int modifiers = method.getModifiers();
		assertTrue("strictfp modifier not expected", (modifiers & ClassFileConstants.AccStrictfp) == 0);
	}
	public void testBug545510_3() throws Exception {
		String source =
				"strictfp class X {\n"+
				"  void foo() {}\n"+
				"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		IBinaryMethod[] methods = classFileReader.getMethods();
		IBinaryMethod method = methods[1];
		int modifiers = method.getModifiers();
		assertTrue("strictfp modifier not expected", (modifiers & ClassFileConstants.AccStrictfp) == 0);
	}
	public void testWildcardBinding() throws Exception {
		String source =
				"public class X {    \n"
				+ "    public static void main(String[] args) {\n"
				+ "		getHasValue().addValueChangeListener(evt -> {System.out.println(\"hello\");});		\n"
				+ "    }\n"
				+ "    public static HasValue<?, ?> getHasValue() { \n"
				+ "        return new HasValue<HasValue.ValueChangeEvent<String>, String>() { \n"
				+ "			@Override\n"
				+ "			public void addValueChangeListener(\n"
				+ "					HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<String>> listener) {\n"
				+ "				listener.valueChanged(null);\n"
				+ "			}\n"
				+ "		};\n"
				+ "    }    \n"
				+ "}\n"
				+ "\n"
				+ "interface HasValue<E extends HasValue.ValueChangeEvent<V>,V> {    \n"
				+ "    public static interface ValueChangeEvent<V> {}    \n"
				+ "    public static interface ValueChangeListener<E extends HasValue.ValueChangeEvent<?>> {\n"
				+ "        void valueChanged(E event);\n"
				+ "    }    \n"
				+ "    void addValueChangeListener(HasValue.ValueChangeListener<? super E> listener);\n"
				+ "}\n";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		IBinaryMethod[] methods = classFileReader.getMethods();
		IBinaryMethod method = methods[3];
		String name = new String(method.getSelector());
		assertTrue("invalid name", "lambda$0".equals(name));
		String descriptor = new String(method.getMethodDescriptor());
		assertTrue("invalid descriptor", "(LHasValue$ValueChangeEvent;)V".equals(descriptor));
	}
}
