/*******************************************************************************
 * Copyright (c) 2013, 2019 GoPivotal, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *			Bug 407191 - [1.8] Binary access support for type annotations
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import junit.framework.Test;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.codegen.AnnotationTargetTypeConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;

@SuppressWarnings({ "rawtypes" })
public class ClassFileReaderTest_1_8 extends AbstractRegressionTest {
	static {
//		TESTS_NAMES = new String[] { "testGH2625" };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	public static Class testClass() {
		return ClassFileReaderTest_1_8.class;
	}

	public ClassFileReaderTest_1_8(String name) {
		super(name);
	}

	// Needed to run tests individually from JUnit
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.complianceLevel = ClassFileConstants.JDK1_8;
	}

	public void test001_classTypeParameter() throws Exception {
		String source =
			"import java.lang.annotation.*;\n" +
			"public class X<@Foo T1,@Bar(iii=99) T2> {}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Foo {\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Bar {\n" +
			"        int iii() default -1;\n" +
			"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);

		IBinaryTypeAnnotation[] typeAnnotations = classFileReader.getTypeAnnotations();
		assertEquals(2,typeAnnotations.length);

		assertEquals("@LFoo; CLASS_TYPE_PARAMETER(type_parameter_index=0)", printTypeAnnotation(typeAnnotations[0]));
		assertEquals("@LBar;(iii=(int)99) CLASS_TYPE_PARAMETER(type_parameter_index=1)", printTypeAnnotation(typeAnnotations[1]));
	}

	public void test001a_classTypeParameterDifferingRetentions() throws Exception {
		String source =
			"import java.lang.annotation.*;\n" +
			"public class X<@Foo T1,@Bar(iii=99) T2> {}\n" +
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Foo {\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Bar {\n" +
			"        int iii() default -1;\n" +
			"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);

		IBinaryTypeAnnotation[] typeAnnotations = classFileReader.getTypeAnnotations();
		assertEquals(2,typeAnnotations.length);

		assertEquals("@LBar;(iii=(int)99) CLASS_TYPE_PARAMETER(type_parameter_index=1)", printTypeAnnotation(typeAnnotations[0]));
		assertEquals("@LFoo; CLASS_TYPE_PARAMETER(type_parameter_index=0)", printTypeAnnotation(typeAnnotations[1]));
	}

	public void test002_methodTypeParameter() throws Exception {
		String source =
			"import java.lang.annotation.*;\n" +
			"public class X {\n" +
			"	<@Foo T1, @Bar(3) T2> void foo(T1 t1,T2 t2) {}\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Foo {\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Bar {\n" +
			"        int value() default -1;\n" +
			"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader cfr = getInternalClassFile("", "X", "X", source);

		IBinaryMethod method = getMethod(cfr,"foo");
		assertNotNull(method);
		IBinaryTypeAnnotation[] typeAnnotations = method.getTypeAnnotations();
		assertNotNull(typeAnnotations);
		assertEquals(2,typeAnnotations.length);
		assertEquals("@LFoo; METHOD_TYPE_PARAMETER(type_parameter_index=0)",printTypeAnnotation(typeAnnotations[0]));
		assertEquals("@LBar;(value=(int)3) METHOD_TYPE_PARAMETER(type_parameter_index=1)",printTypeAnnotation(typeAnnotations[1]));
	}

	public void test003_classExtends() throws Exception {
		this.complianceLevel = ClassFileConstants.JDK1_8;
		String source =
			"import java.lang.annotation.*;\n" +
			"public class X extends @Foo @Bar(iii=34) Object implements java.io.@Bar(iii=1) Serializable {\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Foo {\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Bar {\n" +
			"        int iii() default -1;\n" +
			"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);

		IBinaryTypeAnnotation[] typeAnnotations = classFileReader.getTypeAnnotations();
		assertEquals(3,typeAnnotations.length);
		assertEquals("@LFoo; CLASS_EXTENDS(type_index=-1)", printTypeAnnotation(typeAnnotations[0]));
		assertEquals("@LBar;(iii=(int)34) CLASS_EXTENDS(type_index=-1)", printTypeAnnotation(typeAnnotations[1]));
		assertEquals("@LBar;(iii=(int)1) CLASS_EXTENDS(type_index=0)", printTypeAnnotation(typeAnnotations[2]));
	}

	public void test004_classExtends() throws Exception {
		String source =
			"import java.lang.annotation.*;\n" +
			"public class X extends Y<@Foo String,@Bar Integer> implements I<@Foo String> {\n" +
			"}\n" +
			"class Y<T1, T2> {}\n" +
			"interface I<T1> {}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Foo {\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Bar {\n" +
			"        int iii() default -1;\n" +
			"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);

		IBinaryTypeAnnotation[] typeAnnotations = classFileReader.getTypeAnnotations();
		assertEquals(3,typeAnnotations.length);
		assertEquals("@LFoo; CLASS_EXTENDS(type_index=-1), location=[TYPE_ARGUMENT(0)]", printTypeAnnotation(typeAnnotations[0]));
		assertEquals("@LBar; CLASS_EXTENDS(type_index=-1), location=[TYPE_ARGUMENT(1)]", printTypeAnnotation(typeAnnotations[1]));
		assertEquals("@LFoo; CLASS_EXTENDS(type_index=0), location=[TYPE_ARGUMENT(0)]", printTypeAnnotation(typeAnnotations[2]));
	}

	public void test005_classTypeParameterBound() throws Exception {
		String source =
			"import java.lang.annotation.*;\n" +
			"public class X<U, T extends Y<@Foo String @Bar(1)[][]@Bar(2)[]> & @Bar(3) Cloneable> {}\n" +
			"class Y<T> {}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Foo {\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Bar {\n" +
			"        int value() default -1;\n" +
			"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);

		IBinaryTypeAnnotation[] typeAnnotations = classFileReader.getTypeAnnotations();
		assertEquals(4,typeAnnotations.length);
		assertEquals("@LFoo; CLASS_TYPE_PARAMETER_BOUND(type_parameter_index=1, bound_index=0), location=[TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]", printTypeAnnotation(typeAnnotations[0]));
		assertEquals("@LBar;(value=(int)1) CLASS_TYPE_PARAMETER_BOUND(type_parameter_index=1, bound_index=0), location=[TYPE_ARGUMENT(0)]", printTypeAnnotation(typeAnnotations[1]));
		assertEquals("@LBar;(value=(int)2) CLASS_TYPE_PARAMETER_BOUND(type_parameter_index=1, bound_index=0), location=[TYPE_ARGUMENT(0), ARRAY, ARRAY]", printTypeAnnotation(typeAnnotations[2]));
		assertEquals("@LBar;(value=(int)3) CLASS_TYPE_PARAMETER_BOUND(type_parameter_index=1, bound_index=1)", printTypeAnnotation(typeAnnotations[3]));
	}

	public void test006_methodTypeParameterBound() throws Exception {
		String source =
			"import java.lang.annotation.*;\n" +
			"public class X{\n" +
			"	<T extends Y<@Foo Z @Bar(1)[][]@Bar(2)[]> & @Bar(3) Cloneable> void foo(T t) {}\n" +
			"}\n" +
			"class Y<T> {}\n" +
			"class Z {}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Foo {\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Bar {\n" +
			"        int value() default -1;\n" +
			"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader cfr = getInternalClassFile("", "X", "X", source);

		IBinaryMethod method = getMethod(cfr,"foo");
		assertNotNull(method);
		IBinaryTypeAnnotation[] typeAnnotations = method.getTypeAnnotations();
		assertNotNull(typeAnnotations);
		assertEquals(4,typeAnnotations.length);
		assertEquals("@LFoo; METHOD_TYPE_PARAMETER_BOUND(type_parameter_index=0, bound_index=0), location=[TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]",printTypeAnnotation(typeAnnotations[0]));
		assertEquals("@LBar;(value=(int)1) METHOD_TYPE_PARAMETER_BOUND(type_parameter_index=0, bound_index=0), location=[TYPE_ARGUMENT(0)]", printTypeAnnotation(typeAnnotations[1]));
		assertEquals("@LBar;(value=(int)2) METHOD_TYPE_PARAMETER_BOUND(type_parameter_index=0, bound_index=0), location=[TYPE_ARGUMENT(0), ARRAY, ARRAY]", printTypeAnnotation(typeAnnotations[2]));
		assertEquals("@LBar;(value=(int)3) METHOD_TYPE_PARAMETER_BOUND(type_parameter_index=0, bound_index=1)", printTypeAnnotation(typeAnnotations[3]));
	}

	public void test007_field() throws Exception {
		String source =
			"import java.lang.annotation.*;\n" +
			"import java.util.Map;\n" +
			"public class X{\n" +
			"	@Foo Map<@Bar(1) String, @Bar(2) String @Bar(3)[] @Bar(4)[] @Bar(5)[]> field3;\n" +
			"}\n" +
			"class Y<T> {}\n" +
			"class Z {}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Foo {\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Bar {\n" +
			"        int value() default -1;\n" +
			"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader cfr = getInternalClassFile("", "X", "X", source);

		IBinaryField field = getField(cfr,"field3");
		assertNotNull(field);
		IBinaryTypeAnnotation[] typeAnnotations = field.getTypeAnnotations();
		assertNotNull(typeAnnotations);
		assertEquals(6,typeAnnotations.length);
		assertEquals("@LFoo; FIELD",printTypeAnnotation(typeAnnotations[0]));
		assertEquals("@LBar;(value=(int)1) FIELD, location=[TYPE_ARGUMENT(0)]", printTypeAnnotation(typeAnnotations[1]));
		assertEquals("@LBar;(value=(int)2) FIELD, location=[TYPE_ARGUMENT(1), ARRAY, ARRAY, ARRAY]", printTypeAnnotation(typeAnnotations[2]));
		assertEquals("@LBar;(value=(int)3) FIELD, location=[TYPE_ARGUMENT(1)]", printTypeAnnotation(typeAnnotations[3]));
		assertEquals("@LBar;(value=(int)4) FIELD, location=[TYPE_ARGUMENT(1), ARRAY]", printTypeAnnotation(typeAnnotations[4]));
		assertEquals("@LBar;(value=(int)5) FIELD, location=[TYPE_ARGUMENT(1), ARRAY, ARRAY]", printTypeAnnotation(typeAnnotations[5]));
	}

	public void test008_methodReturn() throws Exception {
		String source =
			"import java.lang.annotation.*;\n" +
			"import java.util.Map;\n" +
			"public class X{\n" +
			"	@Bar(3) @Foo int foo() {\n" +
			"		return 1;\n" +
			"	}\n" +
			"	@Bar(3) int @Foo [] foo2() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Foo {\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Bar {\n" +
			"        int value() default -1;\n" +
			"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader cfr = getInternalClassFile("", "X", "X", source);

		IBinaryMethod method = getMethod(cfr,"foo");
		assertNotNull(method);
		IBinaryTypeAnnotation[] typeAnnotations = method.getTypeAnnotations();
		assertNotNull(typeAnnotations);
		assertEquals(2,typeAnnotations.length);
		assertEquals("@LBar;(value=(int)3) METHOD_RETURN",printTypeAnnotation(typeAnnotations[0]));
		assertEquals("@LFoo; METHOD_RETURN", printTypeAnnotation(typeAnnotations[1]));

		method = getMethod(cfr,"foo2");
		assertNotNull(method);
		typeAnnotations = method.getTypeAnnotations();
		assertNotNull(typeAnnotations);
		assertEquals(2,typeAnnotations.length);
		assertEquals("@LBar;(value=(int)3) METHOD_RETURN, location=[ARRAY]",printTypeAnnotation(typeAnnotations[0]));
		assertEquals("@LFoo; METHOD_RETURN", printTypeAnnotation(typeAnnotations[1]));
	}

	public void test009_methodReceiver() throws Exception {
		String source =
			"import java.lang.annotation.*;\n" +
			"import java.util.Map;\n" +
			"public class X{\n" +
			"	void foo(@Bar(3) X this) {}\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Foo {\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Bar {\n" +
			"        int value() default -1;\n" +
			"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader cfr = getInternalClassFile("", "X", "X", source);

		IBinaryMethod method = getMethod(cfr,"foo");
		assertNotNull(method);
		IBinaryTypeAnnotation[] typeAnnotations = method.getTypeAnnotations();
		assertNotNull(typeAnnotations);
		assertEquals(1,typeAnnotations.length);
		assertEquals("@LBar;(value=(int)3) METHOD_RECEIVER", printTypeAnnotation(typeAnnotations[0]));
	}

	public void test010_methodFormalParameter() throws Exception {
		String source =
			"import java.lang.annotation.*;\n" +
			"import java.util.Map;\n" +
			"public class X{\n" +
			"	void foo(@Bar(3) String s, @Foo int i) {}\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Foo {\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Bar {\n" +
			"        int value() default -1;\n" +
			"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader cfr = getInternalClassFile("", "X", "X", source);

		IBinaryMethod method = getMethod(cfr,"foo");
		assertNotNull(method);
		IBinaryTypeAnnotation[] typeAnnotations = method.getTypeAnnotations();
		assertNotNull(typeAnnotations);
		assertEquals(2,typeAnnotations.length);
		assertEquals("@LBar;(value=(int)3) METHOD_FORMAL_PARAMETER(method_formal_parameter_index=0)",printTypeAnnotation(typeAnnotations[0]));
		assertEquals("@LFoo; METHOD_FORMAL_PARAMETER(method_formal_parameter_index=1)",printTypeAnnotation(typeAnnotations[1]));
	}

	public void test011_throws() throws Exception {
		String source =
			"import java.lang.annotation.*;\n" +
			"import java.util.Map;\n" +
			"public class X{\n" +
			"	void foo() throws @Foo Exception, @Bar(1) Throwable {}\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Foo {\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Bar {\n" +
			"        int value() default -1;\n" +
			"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader cfr = getInternalClassFile("", "X", "X", source);

		IBinaryMethod method = getMethod(cfr,"foo");
		assertNotNull(method);
		IBinaryTypeAnnotation[] typeAnnotations = method.getTypeAnnotations();
		assertNotNull(typeAnnotations);
		assertEquals(2,typeAnnotations.length);
		assertEquals("@LFoo; THROWS(throws_type_index=0)",printTypeAnnotation(typeAnnotations[0]));
		assertEquals("@LBar;(value=(int)1) THROWS(throws_type_index=1)",printTypeAnnotation(typeAnnotations[1]));
	}
	public void test012_annotationMethodReturn() throws Exception {
		String source =
			"import java.lang.annotation.*;\n" +
			"import java.util.Map;\n" +
			"public @interface X{\n" +
			"	@Bar(3) @Foo int foo();\n" +
			"	@Bar(3) int @Foo [] foo2();\n" +
			"	@Bar(7) @Foo String value() default \"aaa\";\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Foo {\n" +
			"}\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface Bar {\n" +
			"        int value() default -1;\n" +
			"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader cfr = getInternalClassFile("", "X", "X", source);

		IBinaryMethod method = getMethod(cfr,"foo");
		assertNotNull(method);
		IBinaryTypeAnnotation[] typeAnnotations = method.getTypeAnnotations();
		assertNotNull(typeAnnotations);
		assertEquals(2,typeAnnotations.length);
		assertEquals("@LBar;(value=(int)3) METHOD_RETURN",printTypeAnnotation(typeAnnotations[0]));
		assertEquals("@LFoo; METHOD_RETURN", printTypeAnnotation(typeAnnotations[1]));

		method = getMethod(cfr,"foo2");
		assertNotNull(method);
		typeAnnotations = method.getTypeAnnotations();
		assertNotNull(typeAnnotations);
		assertEquals(2,typeAnnotations.length);
		assertEquals("@LBar;(value=(int)3) METHOD_RETURN, location=[ARRAY]",printTypeAnnotation(typeAnnotations[0]));
		assertEquals("@LFoo; METHOD_RETURN", printTypeAnnotation(typeAnnotations[1]));

		method = getMethod(cfr,"value");
		assertNotNull(method);
		typeAnnotations = method.getTypeAnnotations();
		assertNotNull(typeAnnotations);
		assertEquals(2,typeAnnotations.length);
		assertEquals("@LBar;(value=(int)7) METHOD_RETURN",printTypeAnnotation(typeAnnotations[0]));
		assertEquals("@LFoo; METHOD_RETURN", printTypeAnnotation(typeAnnotations[1]));
		assertEquals(((org.eclipse.jdt.internal.compiler.impl.Constant)method.getDefaultValue()).stringValue(), "aaa");
	}

	public void testBug548596() {
		/*-
		 * Test548596.jar contains classes for the following kotlin code (compiled with kotlin 1.3.21):
		 * package k;
		 *	class A {
		 *	    class B {
		 *	        class C {
		 *	            //
		 *	        }
		 *	    }
		 *	}
		 */
		String[] libs = getDefaultClassPaths();
		int len = libs.length;
		System.arraycopy(libs, 0, libs = new String[len+1], 0, len);
		libs[libs.length-1] = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test548596.jar";

		runConformTest(
			new String[] {
				"j/Usage.java",
				"package j;\n" +
				"\n" +
				"import k.A.B.C;\n" +
				"\n" +
				"public class Usage {\n" +
				"    C c;\n" +
				"}"
			},
			"",
			libs,
			false,
			null
		);
	}

	public void testGH2625() {
		String[] libs = getDefaultClassPaths();
		int len = libs.length;
		System.arraycopy(libs, 0, libs = new String[len+1], 0, len);
		// in this jar the num_parameters field of RuntimeInvisibleParameterAnnotations has been manually set to 2
		// (annotations on the 3-arg method a(Function,Object,long)):
		libs[libs.length-1] = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "TestGH2625.jar";

		runConformTest(
			new String[] {
				"Test.java",
				"""
				import a.A;
				public class Test {
					void test(A a) {
						a.m(null, null, 0L);
					}
				}
				"""
			},
			"",
			libs,
			false,
			null
		);
	}

	/**
	 * Produce a nicely formatted type annotation for testing. Exercises the API for type annotations.
	 * Output examples:<br>
	 * <tt>@Foo(id=34) CLASS_EXTENDS, type_index=-1, location=[ARRAY, INNER_TYPE, TYPE_ARGUMENT(0)]</tt><br>
	 */
	private String printTypeAnnotation(IBinaryTypeAnnotation typeAnnotation) {
		StringBuilder sb = new StringBuilder();
		// The annotation:
		IBinaryAnnotation annotation = typeAnnotation.getAnnotation();
		sb.append('@').append(annotation.getTypeName());
		IBinaryElementValuePair[] pairs = annotation.getElementValuePairs();
		if (pairs.length != 0) {
			sb.append('(');
			for (int i = 0; i < pairs.length; i++) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append(pairs[i].getName()).append('=').append(pairs[i].getValue());
			}
			sb.append(')');
		}
		sb.append(' ');

		// target type
		int targetType = typeAnnotation.getTargetType();
		switch (targetType) {
			case AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER:
				sb.append("CLASS_TYPE_PARAMETER(type_parameter_index=").append(typeAnnotation.getTypeParameterIndex()).append(')');
				break;
			case AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER:
				sb.append("METHOD_TYPE_PARAMETER(type_parameter_index=").append(typeAnnotation.getTypeParameterIndex()).append(')');
				break;
			case AnnotationTargetTypeConstants.CLASS_EXTENDS:
				sb.append("CLASS_EXTENDS(type_index=").append((short)typeAnnotation.getSupertypeIndex()).append(')');
				break;
			case AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER_BOUND:
				sb.append("CLASS_TYPE_PARAMETER_BOUND(type_parameter_index=").
					append(typeAnnotation.getTypeParameterIndex()).
					append(", bound_index=").append(typeAnnotation.getBoundIndex()).
					append(')');
				break;
			case AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER_BOUND:
				sb.append("METHOD_TYPE_PARAMETER_BOUND(type_parameter_index=").
					append(typeAnnotation.getTypeParameterIndex()).
					append(", bound_index=").append(typeAnnotation.getBoundIndex()).
					append(')');
				break;
			case AnnotationTargetTypeConstants.FIELD:
				sb.append("FIELD");
				break;
			case AnnotationTargetTypeConstants.METHOD_RETURN:
				sb.append("METHOD_RETURN");
				break;
			case AnnotationTargetTypeConstants.METHOD_RECEIVER:
				sb.append("METHOD_RECEIVER");
				break;
			case AnnotationTargetTypeConstants.METHOD_FORMAL_PARAMETER :
				sb.append("METHOD_FORMAL_PARAMETER(method_formal_parameter_index=").
					append(typeAnnotation.getMethodFormalParameterIndex()).append(')');
				break;
			case AnnotationTargetTypeConstants.THROWS :
				sb.append("THROWS(throws_type_index=").
					append(typeAnnotation.getThrowsTypeIndex()).append(')');
				break;
			default: throw new IllegalStateException("nyi "+targetType);
		}

		// location
		int[] typepath = typeAnnotation.getTypePath();

		if (typepath != IBinaryTypeAnnotation.NO_TYPE_PATH) {
			sb.append(", location=["); //$NON-NLS-1$
			for (int i = 0, max = typepath.length; i < max; i += 2) {
				if (i > 0) {
					sb.append(", "); //$NON-NLS-1$
				}
				switch (typepath[i]) {
					case 0:
						sb.append("ARRAY"); //$NON-NLS-1$
						break;
					case 1:
						sb.append("INNER_TYPE"); //$NON-NLS-1$
						break;
					case 2:
						sb.append("WILDCARD"); //$NON-NLS-1$
						break;
					case 3:
						sb.append("TYPE_ARGUMENT(").append(typepath[i+1]).append(')'); //$NON-NLS-1$
						break;
				}
			}
			sb.append(']');
		}
		return sb.toString();
	}

	private IBinaryMethod getMethod(ClassFileReader cfr,String methodname) {
		IBinaryMethod[] methods = cfr.getMethods();
		if (methods == null) {
			return null;
		}
		char[] methodnameAsCharArray = methodname.toCharArray();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (CharOperation.equals(methods[i].getSelector(),methodnameAsCharArray)) {
				return methods[i];
			}
		}
		return null;
	}

	private IBinaryField getField(ClassFileReader cfr,String fieldname) {
		IBinaryField[] fields = cfr.getFields();
		if (fields == null) {
			return null;
		}
		char[] fieldnameAsCharArray = fieldname.toCharArray();
		for (int i = 0, max = fields.length; i < max; i++) {
			if (CharOperation.equals(fields[i].getName(),fieldnameAsCharArray)) {
				return fields[i];
			}
		}
		return null;
	}

}
