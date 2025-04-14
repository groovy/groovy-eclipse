/*******************************************************************************
 * Copyright (c) 2011, 2020 IBM Corporation and others.
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
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                          Bug 383624 - [1.8][compiler] Revive code generation support for type annotations (from Olivier's work)
 *                          Bug 409236 - [1.8][compiler] Type annotations on intersection cast types dropped by code generator
 *                          Bug 409246 - [1.8][compiler] Type annotations on catch parameters not handled properly
 *                          Bug 409517 - [1.8][compiler] Type annotation problems on more elaborate array references
 *                          Bug 415821 - [1.8][compiler] CLASS_EXTENDS target type annotation missing for anonymous classes
 *                          Bug 426616 - [1.8][compiler] Type Annotations, multiple problems
 *        Stephan Herrmann - Contribution for
 *							Bug 415911 - [1.8][compiler] NPE when TYPE_USE annotated method with missing return type
 *							Bug 416176 - [1.8][compiler][null] null type annotations cause grief on type variables
 *         Jesper S Moller - Contributions for
 *                          Bug 416885 - [1.8][compiler]IncompatibleClassChange error (edit)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.JavacHasABug;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TypeAnnotationTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_NAMES = new String[] { "testTypeVariable" };
	}
	public static Class testClass() {
		return TypeAnnotationTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	public TypeAnnotationTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	@Override
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
		return defaultOptions;
	}

	private static final String HELPER_CLASS =
		"import java.lang.annotation.*;\n"+
		"import java.lang.reflect.*;\n"+
		"class Helper {\n"+
		"\n"+
		// Print type annotations on super types
		"  public static void printTypeAnnotations(Class<?> clazz) {\n"+
		"    System.out.print(\"Annotations on superclass of \"+clazz.getName() +\"\\n\");\n"+
		"    AnnotatedType superat = clazz.getAnnotatedSuperclass();\n"+
		"    Helper.printAnnos(\"  \", superat.getType(),superat.getAnnotations());\n"+
		"    AnnotatedType[] superinterfaces = clazz.getAnnotatedInterfaces();\n"+
		"    if (superinterfaces.length!=0) {\n"+
		"      System.out.print(\"Annotations on superinterfaces of \"+clazz.getName() +\"\\n\");\n"+
		"      for (int j=0;j<superinterfaces.length;j++) {\n"+
		"        Helper.printAnnos(\"  \", superinterfaces[j].getType(),superinterfaces[j].getAnnotations());\n"+
		"      }\n"+
		"    }\n"+
		"  }\n"+
		// Print type annotations on a type
		"  public static void printTypeAnnotations2(Class<?> clazz) {\n"+
		"    System.out.print(clazz.getName()+\"<\");\n"+
		"    TypeVariable<?>[] tvs = clazz.getTypeParameters();\n"+
		"    for (int t=0;t<tvs.length;t++) {\n"+
		"      TypeVariable<?> tv = tvs[t];\n"+
		"      Annotation[] annos = tv.getAnnotations();\n"+
		"      for (int a=0;a<annos.length;a++) {\n"+
		"        System.out.print(toStringAnno(annos[a])+\" \");\n"+
		"      }\n"+
		"      System.out.print(tv.getName());\n"+
		"      if ((t+1)<tvs.length) System.out.print(\",\");\n"+
		"    }\n"+
		"    System.out.print(\">\\n\");\n"+
		"  }\n"+
		"  public static String toStringAnno(Annotation anno) {\n"+
		"    String s = anno.toString();\n"+
		"	 s = s.replace(\"\\\"\", \"\");\n" +
		"	 s = s.replace(\"'\", \"\");\n" +
		"    if (s.endsWith(\"()\")) return s.substring(0,s.length()-2); else return s;\n"+
		"  }\n"+
		"  \n"+
		"  public static void printAnnos(String header, Type t, Annotation[] annos) {\n"+
		"    if (annos.length==0) { System.out.print(header+t+\":no annotations\\n\"); return;} \n"+
		"    System.out.print(header+t+\":\");\n"+
		"    for (int i=0;i<annos.length;i++) {\n"+
		"      System.out.print(toStringAnno(annos[i])+\" \");\n"+
		"    }\n"+
		"    System.out.print(\"\\n\");\n"+
		"  }\n"+
		"}\n";

	// http://types.cs.washington.edu/jsr308/specification/java-annotation-design.pdf
	//		type_annotation {
	//			// New fields in JSR 308:
	//			u1 target_type; // the type of the targeted program element, see Section 3.2
	//			union {
	//				type_parameter_target;
	//				supertype_target;
	//				type_parameter_bound_target;
	//				empty_target;
	//				method_formal_parameter_target;
	//				throws_target;
	//				localvar_target;
	//				catch_target;
	//				offset_target;
	//				type_argument_target;
	//				method_reference_target;
	//			} target_info; // identifies the targeted program element, see Section 3.3
	//			type_path target_path; // identifies targeted type in a compound type (array, generic, etc.), see Section 3.4
	//			// Original fields from "annotation" structure:
	//			u2 type_index;
	//			u2 num_element_value_pairs;
	//			{
	//				u2 element_name_index;
	//				element_value value;
	//			} element_value_pairs[num_element_value_pairs];
	//			}

	public void test001_classTypeParameter() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X<@Marker T> {}",

					"Marker.java",
					"import java.lang.annotation.*;\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n"+
					"@Target(ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}",
				},
				"");
		// javac-b81: 9[0 1 0 0 0 0 13 0 0]  (13=Marker annotation)
		String expectedOutput =
			"  RuntimeVisibleTypeAnnotations: \n" +
			"    #21 @Marker(\n" +
			"      target type = 0x0 CLASS_TYPE_PARAMETER\n" +
			"      type parameter index = 0\n" +
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test002_classTypeParameter_reflection() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X<@Marker T> {\n"+
					"  public static void main(String[] argv) { Helper.printTypeAnnotations2(X.class);}\n"+
					"}",

					"Helper.java",HELPER_CLASS,
					"Marker.java",
					"import java.lang.annotation.*;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n"+
					"@Target(TYPE_PARAMETER)\n" +
					"@interface Marker {}",
				},
				"X<@Marker T>");
	}

	public void test003_classTypeParameter() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X<@A1 T1,@A2 @A3 T2> {}",

					"A1.java",
					"import java.lang.annotation.*;\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n"+
					"@Target(ElementType.TYPE_PARAMETER)\n" +
					"@interface A1 {}",

					"A2.java",
					"import java.lang.annotation.*;\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n"+
					"@Target(ElementType.TYPE_PARAMETER)\n" +
					"@interface A2 {}",

					"A3.java",
					"import java.lang.annotation.*;\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n"+
					"@Target(ElementType.TYPE_PARAMETER)\n" +
					"@interface A3 {}",

				},
				"");
		// javac-b81: 9[0 1 0 0 0 0 13 0 0]  (13=Marker)
		String expectedOutput =
				"  RuntimeVisibleTypeAnnotations: \n" +
				"    #21 @A1(\n" +
				"      target type = 0x0 CLASS_TYPE_PARAMETER\n" +
				"      type parameter index = 0\n" +
				"    )\n" +
				"    #22 @A2(\n" +
				"      target type = 0x0 CLASS_TYPE_PARAMETER\n" +
				"      type parameter index = 1\n" +
				"    )\n" +
				"    #23 @A3(\n" +
				"      target type = 0x0 CLASS_TYPE_PARAMETER\n" +
				"      type parameter index = 1\n" +
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test004_classTypeParameter_reflection() throws Exception {
		this.runConformTest(
				new String[] {
						"X.java",
						"public class X<@A1 T1,@A2 @A3 T2> {\n"+
						"    public static void main(String[] argv) { Helper.printTypeAnnotations2(X.class); }\n"+
						"}",

						"Helper.java",HELPER_CLASS,
						"A1.java",
						"import java.lang.annotation.*;\n" +
						"@Retention(RetentionPolicy.RUNTIME)\n"+
						"@Target(ElementType.TYPE_PARAMETER)\n" +
						"@interface A1 {}",
						"A2.java",
						"import java.lang.annotation.*;\n" +
						"import static java.lang.annotation.ElementType.*;\n" +
						"@Retention(RetentionPolicy.RUNTIME)\n"+
						"@Target(TYPE_PARAMETER)\n" +
						"@interface A2 {}",
						"A3.java",
						"import java.lang.annotation.*;\n" +
						"import static java.lang.annotation.ElementType.*;\n" +
						"@Retention(RetentionPolicy.RUNTIME)\n"+
						"@Target(TYPE_PARAMETER)\n" +
						"@interface A3 {}",
				},
				"X<@A1 T1,@A2 @A3 T2>");
	}

	public void test005_classTypeParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_PARAMETER)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_PARAMETER)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"X.java",
				"public class X<@A @B(3) T> {}",
		},
		"");
		String expectedOutput =
			"  RuntimeVisibleTypeAnnotations: \n" +
			"    #25 @A(\n" +
			"      target type = 0x0 CLASS_TYPE_PARAMETER\n" +
			"      type parameter index = 0\n" +
			"    )\n" +
			"  RuntimeInvisibleTypeAnnotations: \n" +
			"    #21 @B(\n" +
			"      #22 value=(int) 3 (constant type)\n" +
			"      target type = 0x0 CLASS_TYPE_PARAMETER\n" +
			"      type parameter index = 0\n" +
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test006_classTypeParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_PARAMETER)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_PARAMETER)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"X.java",
				"public class X<T1,T2,@A @B(3) T3> {}",
		},
		"");
		String expectedOutput =
			"  RuntimeVisibleTypeAnnotations: \n" +
			"    #25 @A(\n" +
			"      target type = 0x0 CLASS_TYPE_PARAMETER\n" +
			"      type parameter index = 2\n" +
			"    )\n" +
			"  RuntimeInvisibleTypeAnnotations: \n" +
			"    #21 @B(\n" +
			"      #22 value=(int) 3 (constant type)\n" +
			"      target type = 0x0 CLASS_TYPE_PARAMETER\n" +
			"      type parameter index = 2\n" +
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test007_methodTypeParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_PARAMETER)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_PARAMETER)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"X.java",
				"public class X {\n" +
				"	<@A @B(3) T> void foo(T t) {}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #27 @A(\n" +
			"        target type = 0x1 METHOD_TYPE_PARAMETER\n" +
			"        type parameter index = 0\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #23 @B(\n" +
			"        #24 value=(int) 3 (constant type)\n" +
			"        target type = 0x1 METHOD_TYPE_PARAMETER\n" +
			"        type parameter index = 0\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test008_methodTypeParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_PARAMETER)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_PARAMETER)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"X.java",
				"public class X {\n" +
				"	<T1, @A @B(3) T2> void foo(T1 t1,T2 t2) {}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #29 @A(\n" +
			"        target type = 0x1 METHOD_TYPE_PARAMETER\n" +
			"        type parameter index = 1\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #25 @B(\n" +
			"        #26 value=(int) 3 (constant type)\n" +
			"        target type = 0x1 METHOD_TYPE_PARAMETER\n" +
			"        type parameter index = 1\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test009_classExtends() throws Exception {
		this.runConformTest(
				new String[] {
					"Marker.java",
					"import java.lang.annotation.Target;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"@Target(TYPE_USE)\n" +
					"@interface Marker {}",
					"X.java",
					"public class X extends @Marker Object {}",
				},
				"");
		// javac-b81 annotation contents: len:10[0 1 16 -1 -1 0 0 17 0 0]
		String expectedOutput =
			"  RuntimeInvisibleTypeAnnotations: \n" +
			"    #17 @Marker(\n" +
			"      target type = 0x10 CLASS_EXTENDS\n" +
			"      type index = -1\n" +
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test010_classExtends() throws Exception {
		this.runConformTest(
				new String[] {
					"Marker.java",
					"import java.lang.annotation.*;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n"+
					"@Target(TYPE_USE)\n" +
					"@interface Marker {}",
					"X.java",
					"public class X extends @Marker Object {}",
				},
				"");
		// Bytes:10[0 1 16 -1 -1 0 0 17 0 0]
		String expectedOutput =
			"  RuntimeVisibleTypeAnnotations: \n" +
			"    #17 @Marker(\n" +
			"      target type = 0x10 CLASS_EXTENDS\n" +
			"      type index = -1\n" +
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test011_classExtends_reflection() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X extends @Marker Object {public static void main(String[] argv) {Helper.printTypeAnnotations(X.class);}}",
					"Helper.java",HELPER_CLASS,
					"Marker.java",
					"import java.lang.annotation.Target;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"@Target(TYPE_USE)\n" +
					"@interface Marker {}"
				},
				"Annotations on superclass of X\n"+
				"  class java.lang.Object:no annotations");
	}

	public void test012_classExtends_reflection() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X extends @Marker Object {public static void main(String[] argv) {Helper.printTypeAnnotations(X.class);}}",
					"Helper.java",HELPER_CLASS,
					"Marker.java",
					"import java.lang.annotation.*;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"@Target(TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n"+
					"@interface Marker {}"
				},
				"Annotations on superclass of X\n"+
				"  class java.lang.Object:@Marker");
	}

	public void test013_classExtends_interfaces() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String id() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X implements @A(id=\"Hello, World!\") I, @B @C('(') J {}",
		},
		"");
		// Output from javac b81 lambda
		// RuntimeVisibleTypeAnnotations
		// Bytes:28[0 2 16 0 0 0 0 13 0 1 0 14 115 0 15 16 0 1 0 0 16 0 1 0 17 67 0 18]
		// RuntimeInvisibleTypeAnnotations
		// Bytes:10[0 1 16 0 1 0 0 20 0 0]
		String expectedOutput =
			"  RuntimeVisibleTypeAnnotations: \n" +
			"    #23 @A(\n" +
			"      #24 id=\"Hello, World!\" (constant type)\n" +
			"      target type = 0x10 CLASS_EXTENDS\n" +
			"      type index = 0\n" +
			"    )\n" +
			"    #26 @C(\n" +
			"      #27 value=\'(\' (constant type)\n" +
			"      target type = 0x10 CLASS_EXTENDS\n" +
			"      type index = 1\n" +
			"    )\n" +
			"  RuntimeInvisibleTypeAnnotations: \n" +
			"    #21 @B(\n" +
			"      target type = 0x10 CLASS_EXTENDS\n" +
			"      type index = 1\n" +
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test014_classExtends_interfaces_reflection() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements @A I {public static void main(String[]argv) {Helper.printTypeAnnotations(X.class);}}",
				"Helper.java",HELPER_CLASS,
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"}\n",
				"I.java",
				"interface I {}\n"
		},
		"Annotations on superclass of X\n" +
		"  class java.lang.Object:no annotations\n" +
		"Annotations on superinterfaces of X\n" +
		"  interface I:@A");
	}

	public void test015_classExtends_interfaces_reflection() throws Exception {
		String javaVersion = System.getProperty("java.version");
		int index = javaVersion.indexOf('.');
		if (index != -1) {
			javaVersion = javaVersion.substring(0, index);
		} else {
			index = javaVersion.indexOf('-');
			if (index != -1)
				javaVersion = javaVersion.substring(0, index);
		}
		int v = Integer.parseInt(javaVersion);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements @A(id=\"Hello, World!\") I, @B @C('i') J {public static void main(String[] argv) { Helper.printTypeAnnotations(X.class);}}",
				"Helper.java",HELPER_CLASS,
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String id() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
		},
		"Annotations on superclass of X\n" +
		"  class java.lang.Object:no annotations\n" +
		"Annotations on superinterfaces of X\n" +
		"  interface I:@A(id=Hello, World!) \n" +
		"  interface J:@C(" + (v < 14 ? "value=" : "") + "i)");
	}

	public void test016_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"Y.java",
				"class Y<T> {}\n",
				"X.java",
				"public class X extends Y<@B String> {\n" +
				"}",
		},
		"");
		// javac-b81: Bytes:12[0 1 16 -1 -1 1 3 0 0 13 0 0] // type path: 1,3,0
		String expectedOutput =
				"  RuntimeVisibleTypeAnnotations: \n" +
				"    #19 @B(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = -1\n" +
				"      location = [TYPE_ARGUMENT(0)]\n" +
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test017_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"Marker.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Marker { }\n",
				"I.java",
				"interface I<T> {}\n",
				"X.java",
				"public class X implements I<@Marker String> {\n" +
				"}",
		},
		"");
		// javac-b81: Bytes:12[0 1 16 0 0 1 3 0 0 14 0 0] // type path: 1,3,0
		String expectedOutput =
				"  RuntimeVisibleTypeAnnotations: \n" +
				"    #21 @Marker(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"      location = [TYPE_ARGUMENT(0)]\n" +
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test018_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A { }\n",

				"I.java",
				"interface I<T1,T2> {}\n",

				"X.java",
				"public class X implements I<Integer, @A String> {}\n"
		},
		"");
		// javac-b81: Bytes:12[0 1 16 0 0 1 3 1 0 14 0 0] // type path: 1,3,1
		String expectedOutput =
				"  RuntimeVisibleTypeAnnotations: \n" +
				"    #21 @A(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"      location = [TYPE_ARGUMENT(1)]\n" +
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test019_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A { }\n",

				"J.java",
				"interface J<T> {}\n",

				"I.java",
				"interface I<T> {}\n",

				"X.java",
				"public class X implements I<J<@A String>> {}\n"
		},
		"");
		// javac-b81: Bytes:14[0 1 16 0 0 2 3 0 3 0 0 14 0 0] // type path: 2,3,0,3,0
		String expectedOutput =
				"  RuntimeVisibleTypeAnnotations: \n" +
				"    #21 @A(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"      location = [TYPE_ARGUMENT(0), TYPE_ARGUMENT(0)]\n" +
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test020_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A { }\n",

				"I.java",
				"interface I<T> {}\n",

				"X.java",
				"public class X implements I<@A String[]> {}\n"
		},
		"");
		// javac-b81: Bytes:14[0 1 16 0 0 2 3 0 0 0 0 14 0 0] // type path: 2,3,0,0,0
		String expectedOutput =
				"  RuntimeVisibleTypeAnnotations: \n" +
				"    #21 @A(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"      location = [TYPE_ARGUMENT(0), ARRAY]\n" +
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test021_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A { }\n",

				"I.java",
				"interface I<T> {}\n",

				"X.java",
				"public class X implements I<String @A[]> {}\n"
		},
		"");
		// javac-b81: Bytes:12[0 1 16 0 0 1 3 0 0 14 0 0] // type path: 1,3,0
		String expectedOutput =
				"  RuntimeVisibleTypeAnnotations: \n" +
				"    #21 @A(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"      location = [TYPE_ARGUMENT(0)]\n" +
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test022_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A { }\n",

				"I.java",
				"interface I<T> {}\n",

				"X.java",
				"public class X implements I<String []@A[]> {}\n"
		},
		"");
		// javac-b81: Bytes:14[0 1 16 0 0 2 3 0 0 0 0 14 0 0] // type path: 2,3,0,0,0
		String expectedOutput =
				"  RuntimeVisibleTypeAnnotations: \n" +
				"    #21 @A(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"      location = [TYPE_ARGUMENT(0), ARRAY]\n" +
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test023_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A { }\n",

				"I.java",
				"interface I<T> {}\n",

				"X.java",
				"public class X implements I<@A String [][][]> {}\n"
		},
		"");
		// javac-b81: Bytes:10[0 1 16 0 0 0 0 12 0 0] // type path: 4,3,0,0,0,0,0,0,0
		String expectedOutput =
				"  RuntimeVisibleTypeAnnotations: \n" +
				"    #21 @A(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"      location = [TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]\n" +
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	public void test024_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"I.java",
				"interface I<T> {}\n",
				"J.java",
				"interface J<U,T> {}\n",
				"X.java",
				"public class X implements I<@A(\"Hello, World!\") String>, @B J<String, @C('(') Integer> {}",
		},
		"");
		String expectedOutput =
			"  RuntimeVisibleTypeAnnotations: \n" +
			"    #25 @A(\n" +
			"      #26 value=\"Hello, World!\" (constant type)\n" +
			"      target type = 0x10 CLASS_EXTENDS\n" +
			"      type index = 0\n" +
			"      location = [TYPE_ARGUMENT(0)]\n" +
			"    )\n" +
			"    #28 @C(\n" +
			"      #26 value=\'(\' (constant type)\n" +
			"      target type = 0x10 CLASS_EXTENDS\n" +
			"      type index = 1\n" +
			"      location = [TYPE_ARGUMENT(1)]\n" +
			"    )\n" +
			"  RuntimeInvisibleTypeAnnotations: \n" +
			"    #23 @B(\n" +
			"      target type = 0x10 CLASS_EXTENDS\n" +
			"      type index = 1\n" +
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test025_classTypeParameterBound() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T extends @A String> {}",
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {}\n"
		},
		"");
		// javac-b81: Bytes:10[0 1 17 0 0 0 0 13 0 0]
		// [17 0 0] is CLASS_PARAMETER_BOUND type_parameter_index=0 bound_index=0
		String expectedOutput =
			"  RuntimeVisibleTypeAnnotations: \n" +
			"    #21 @A(\n" +
			"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND\n" +
			"      type parameter index = 0 type parameter bound index = 0\n" +
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test026_classTypeParameterBound() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"X.java",
				"public class X<T extends @A String & @B(3) Cloneable> {}",
		},
		"");
		String expectedOutput =
			"  RuntimeVisibleTypeAnnotations: \n" +
			"    #25 @A(\n" +
			"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND\n" +
			"      type parameter index = 0 type parameter bound index = 0\n" +
			"    )\n" +
			"  RuntimeInvisibleTypeAnnotations: \n" +
			"    #21 @B(\n" +
			"      #22 value=(int) 3 (constant type)\n" +
			"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND\n" +
			"      type parameter index = 0 type parameter bound index = 1\n" +
			"    )\n" ;
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test027_classTypeParameterBound_complex() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"Y.java",
				"public class Y<T> {}",
				"X.java",
				"public class X<U, T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}",
		},
		"");
		// javac-b81:
		// Bytes:28[0 2 17 1 0 1 3 0 0 13 0 0 17 1 0 4 3 0 0 0 0 0 0 0 0 14 0 0]
		// Bytes:29[0 2 17 1 0 3 3 0 0 0 0 0 0 16 0 0 17 1 1 0 0 16 0 1 0 17 73 0 18]
		String expectedOutput =
			"  RuntimeVisibleTypeAnnotations: \n" +
			"    #25 @A(\n" +
			"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND\n" +
			"      type parameter index = 1 type parameter bound index = 0\n" +
			"      location = [TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]\n" +
			"    )\n" +
			"    #26 @C(\n" +
			"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND\n" +
			"      type parameter index = 1 type parameter bound index = 0\n" +
			"      location = [TYPE_ARGUMENT(0)]\n" +
			"    )\n" +
			"  RuntimeInvisibleTypeAnnotations: \n" +
			"    #21 @B(\n" +
			"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND\n" +
			"      type parameter index = 1 type parameter bound index = 0\n" +
			"      location = [TYPE_ARGUMENT(0), ARRAY, ARRAY]\n" +
			"    )\n" +
			"    #21 @B(\n" +
			"      #22 value=(int) 3 (constant type)\n" +
			"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND\n" +
			"      type parameter index = 1 type parameter bound index = 1\n" +
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test028_methodTypeParameterBound() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"Z.java",
				"public class Z {}",
				"X.java",
				"public class X {\n" +
				"	<T extends @A Z> void foo(T t) {}\n" +
				"}",
		},
		"");
		// javac-b81: Bytes:10[0 1 18 0 0 0 0 13 0 0]
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #23 @A(\n" +
			"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" +
			"        type parameter index = 0 type parameter bound index = 0\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test029_methodTypeParameterBound() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"Z.java",
				"public class Z {}",
				"X.java",
				"public class X {\n" +
				"	<T extends @A Z & @B(3) Cloneable> void foo(T t) {}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #27 @A(\n" +
			"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" +
			"        type parameter index = 0 type parameter bound index = 0\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #23 @B(\n" +
			"        #24 value=(int) 3 (constant type)\n" +
			"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" +
			"        type parameter index = 0 type parameter bound index = 1\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test030_methodTypeParameterBound() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"Z.java",
				"public class Z {}",
				"Y.java",
				"public class Y<T> {}",
				"X.java",
				"public class X {\n" +
				"	<T extends Y<Z [][]@B[]> & Cloneable> void foo(T t) {}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #23 @B(\n" +
			"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" +
			"        type parameter index = 0 type parameter bound index = 0\n" +
			"        location = [TYPE_ARGUMENT(0), ARRAY, ARRAY]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test031_methodTypeParameterBound_complex() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"Z.java",
				"public class Z {}",
				"Y.java",
				"public class Y<T> {}",
				"X.java",
				"public class X {\n" +
				"	<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}\n" +
				"}",
		},
		"");
		// javac-b81:
		// Bytes:28[0 2 18 0 0 1 3 0 0 13 0 0 18 0 0 4 3 0 0 0 0 0 0 0 0 14 0 0]
		// Bytes:29[0 2 18 0 0 3 3 0 0 0 0 0 0 16 0 0 18 0 1 0 0 16 0 1 0 17 73 0 18]
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #27 @A(\n" +
			"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" +
			"        type parameter index = 0 type parameter bound index = 0\n" +
			"        location = [TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]\n" +
			"      )\n" +
			"      #28 @C(\n" +
			"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" +
			"        type parameter index = 0 type parameter bound index = 0\n" +
			"        location = [TYPE_ARGUMENT(0)]\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #23 @B(\n" +
			"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" +
			"        type parameter index = 0 type parameter bound index = 0\n" +
			"        location = [TYPE_ARGUMENT(0), ARRAY, ARRAY]\n" +
			"      )\n" +
			"      #23 @B(\n" +
			"        #24 value=(int) 3 (constant type)\n" +
			"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" +
			"        type parameter index = 0 type parameter bound index = 1\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test032_field() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {}\n",

				"X.java",
				"public class X {\n" +
				"	@A int field;\n" +
				"}",
		},
		"");
		// javac-b81: Bytes:8[0 1 19 0 0 7 0 0]  19 = 0x13 (FIELD)
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #8 @A(\n" +
			"        target type = 0x13 FIELD\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test033_field() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {}\n",

				"X.java",
				"public class X {\n" +
				"	java.util.List<@A String> field;\n" +
				"}",
		},
		"");
		// javac-b81: Bytes:10[0 1 19 1 3 0 0 9 0 0]
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #10 @A(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [TYPE_ARGUMENT(0)]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test034_field() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"X.java",
				"public class X {\n" +
				"	@B(3) @A int field;\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #12 @A(\n" +
			"        target type = 0x13 FIELD\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #8 @B(\n" +
			"        #9 value=(int) 3 (constant type)\n" +
			"        target type = 0x13 FIELD\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test035_field() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {}\n",

				"X.java",
				"public class X {\n" +
				"	java.util.Map<String, @A String> field;\n" +
				"}",
		},
		"");
		// javac-b81: Bytes:10[0 1 19 1 3 1 0 9 0 0]
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #10 @A(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [TYPE_ARGUMENT(1)]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test036_field() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {}\n",

				"X.java",
				"public class X {\n" +
				"	java.util.List<String[][]@A[][]> field;\n" +
				"}",
		},
		"");
		// javac-b81: Bytes:14[0 1 19 3 3 0 0 0 0 0 0 9 0 0]
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #10 @A(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [TYPE_ARGUMENT(0), ARRAY, ARRAY]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test037_field() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	java.util.List<? extends @A Number> field;\n" +
				"}",
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {}\n",
		},
		"");
		// javac-b81: Bytes:12[0 1 19 2 3 0 2 0 0 9 0 0]
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #10 @A(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [TYPE_ARGUMENT(0), WILDCARD]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test038_field() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"class AA { class BB<T> {}}" +
				"class X {\n" +
				"  AA.@A BB field;\n" +
				"}\n",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A { }\n",
		},
		"");
		String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @A(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [INNER_TYPE]\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test038a_field() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"class AA { class BB<T> {}}" +
				"class X {\n" +
				"  @B AA.@A BB[] @C[] field;\n" +
				"}\n",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A { }\n",

				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface B { }\n",

				"C.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface C { }\n",
		},
		"");

	String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @B(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY]\n" +
				"      )\n" +
				"      #9 @A(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY, INNER_TYPE]\n" +
				"      )\n" +
				"      #10 @C(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY]\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test039_field() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"X.java",
				"public class X {\n" +
				"	@A int [] @B(3) [] field;\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #12 @A(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [ARRAY, ARRAY]\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #8 @B(\n" +
			"        #9 value=(int) 3 (constant type)\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [ARRAY]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test040_field_complex() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Map;\n" +
				"import java.util.List;\n" +
				"public class X {\n" +
				"	@H String @E[] @F[] @G[] field;\n" +
				"	@A Map<@B String, @C List<@D Object>> field2;\n" +
				"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" +
				"}",
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"D.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface D {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"E.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface E {\n" +
				"	int value() default -1;\n" +
				"}",
				"F.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface F {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"G.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface G {\n" +
				"	int value() default -1;\n" +
				"}",
				"H.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface H {\n" +
				"	char value() default '-';\n" +
				"}\n",
		},
		"");
		String expectedOutput =
			"  // Field descriptor #6 [[[Ljava/lang/String;\n" +
			"  java.lang.String[][][] field;\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #11 @H(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [ARRAY, ARRAY, ARRAY]\n" +
			"      )\n" +
			"      #12 @F(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [ARRAY]\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #8 @E(\n" +
			"        target type = 0x13 FIELD\n" +
			"      )\n" +
			"      #9 @G(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [ARRAY, ARRAY]\n" +
			"      )\n" +
			"  \n" +
			"  // Field descriptor #14 Ljava/util/Map;\n" +
			"  // Signature: Ljava/util/Map<Ljava/lang/String;Ljava/util/List<Ljava/lang/Object;>;>;\n" +
			"  java.util.Map field2;\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #18 @A(\n" +
			"        target type = 0x13 FIELD\n" +
			"      )\n" +
			"      #19 @C(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [TYPE_ARGUMENT(1)]\n" +
			"      )\n" +
			"      #20 @D(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #17 @B(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [TYPE_ARGUMENT(0)]\n" +
			"      )\n" +
			"  \n" +
			"  // Field descriptor #14 Ljava/util/Map;\n" +
			"  // Signature: Ljava/util/Map<Ljava/lang/String;[[[Ljava/lang/String;>;\n" +
			"  java.util.Map field3;\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #18 @A(\n" +
			"        target type = 0x13 FIELD\n" +
			"      )\n" +
			"      #11 @H(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [TYPE_ARGUMENT(1), ARRAY, ARRAY, ARRAY]\n" +
			"      )\n" +
			"      #12 @F(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [TYPE_ARGUMENT(1), ARRAY]\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #17 @B(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [TYPE_ARGUMENT(0)]\n" +
			"      )\n" +
			"      #8 @E(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [TYPE_ARGUMENT(1)]\n" +
			"      )\n" +
			"      #9 @G(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [TYPE_ARGUMENT(1), ARRAY, ARRAY]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test041_field() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	java.lang.@H String @E[] @F[] @G[] field;\n" +
				"}",
				"E.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface E {\n" +
				"	int value() default -1;\n" +
				"}",
				"F.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface F {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"G.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface G {\n" +
				"	int value() default -1;\n" +
				"}",
				"H.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface H {\n" +
				"	char value() default '-';\n" +
				"}\n",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #11 @H(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [ARRAY, ARRAY, ARRAY]\n" +
			"      )\n" +
			"      #12 @F(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [ARRAY]\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #8 @E(\n" +
			"        target type = 0x13 FIELD\n" +
			"      )\n" +
			"      #9 @G(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [ARRAY, ARRAY]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test042_methodReturnType() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"X.java",
				"public class X {\n" +
				"	@B(3) @A(value=\"test\") int foo() {\n" +
				"		return 1;\n" +
				"	}\n" +
				"}",
		},
		"");
		// javac-b81:
		// Bytes:13[0 1 20 0 0 11 0 1 0 12 115 0 13]
		// Bytes:13[0 1 20 0 0 15 0 1 0 12 73 0 16]
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #21 @A(\n" +
			"        #18 value=\"test\" (constant type)\n" +
			"        target type = 0x14 METHOD_RETURN\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #17 @B(\n" +
			"        #18 value=(int) 3 (constant type)\n" +
			"        target type = 0x14 METHOD_RETURN\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test043_methodReceiver() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"X.java",
				"public class X {\n" +
				"	void foo(@B(3) X this) {}\n" +
				"}",
		},
		"");
		// javac-b81: Bytes:13[0 1 21 0 0 10 0 1 0 11 73 0 12]
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #16 @B(\n" +
			"        #17 value=(int) 3 (constant type)\n" +
			"        target type = 0x15 METHOD_RECEIVER\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test044_methodReceiver() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	void foo(X<@B(3) T> this) {}\n" +
				"}",
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
		},
		"");
		// javac-b81: Bytes:15[0 1 21 1 3 0 0 10 0 1 0 11 73 0 12]
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #18 @B(\n" +
			"        #19 value=(int) 3 (constant type)\n" +
			"        target type = 0x15 METHOD_RECEIVER\n" +
			"        location = [TYPE_ARGUMENT(0)]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	public void test045_methodParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	int foo(@B(3) String s) {\n" +
				"		return s.length();\n" +
				"	}\n" +
				"}",

				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
		},
		"");
		// javac-b81: Bytes:14[0 1 22 0 0 0 11 0 1 0 12 73 0 13]
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #25 @B(\n" +
			"        #26 value=(int) 3 (constant type)\n" +
			"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
			"        method parameter index = 0\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test046_methodParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	int foo(int i, double d, @B(3) String s) {\n" +
				"		return s.length();\n" +
				"	}\n" +
				"}",

				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
		},
		"");
		// javac-b81: Bytes:14[0 1 22 1 0 0 11 0 1 0 12 73 0 13]
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #29 @B(\n" +
			"        #30 value=(int) 3 (constant type)\n" +
			"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
			"        method parameter index = 2\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test047_methodParameterArray() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"X.java",
				"public class X {\n" +
				"	int foo(String @A [] @B(3) [] s) {\n" +
				"		return s.length;\n" +
				"	}\n" +
				"}",
		},
		"");
		// javac-b81:
		// Bytes:9[0 1 22 0 0 0 11 0 0]
		// Bytes:16[0 1 22 0 1 0 0 0 13 0 1 0 14 73 0 15]
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #23 @A(\n" +
			"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
			"        method parameter index = 0\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #19 @B(\n" +
			"        #20 value=(int) 3 (constant type)\n" +
			"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
			"        method parameter index = 0\n" +
			"        location = [ARRAY]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test048_throws() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n"+
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.*;\n"+
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.*;\n"+
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"E.java",
				"class E extends RuntimeException {\n" +
				"	private static final long serialVersionUID = 1L;\n" +
				"}\n",
				"E1.java",
				"class E1 extends RuntimeException {\n" +
				"	private static final long serialVersionUID = 1L;\n" +
				"}\n",
				"E2.java",
				"class E2 extends RuntimeException {\n" +
				"	private static final long serialVersionUID = 1L;\n" +
				"}\n",
				"X.java",
				"public class X {\n" +
				"	void foo() throws @A(\"Hello, World!\") E, E1, @B @C('(') E2 {}\n" +
				"}",
		},
		"");
		// javac-b81:
		// Bytes:28[0 2 23 0 0 0 0 14 0 1 0 15 115 0 16 23 0 2 0 0 17 0 1 0 15 67 0 18]
		// Bytes:10[0 1 23 0 2 0 0 20 0 0]
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #25 @A(\n" +
			"        #26 value=\"Hello, World!\" (constant type)\n" +
			"        target type = 0x17 THROWS\n" +
			"        throws index = 0\n" +
			"      )\n" +
			"      #28 @C(\n" +
			"        #26 value=\'(\' (constant type)\n" +
			"        target type = 0x17 THROWS\n" +
			"        throws index = 2\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #23 @B(\n" +
			"        target type = 0x17 THROWS\n" +
			"        throws index = 2\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	public void test049_codeblocks_localVariable() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		@B int j = 9;\n" +
				"		try {\n" +
				"			System.out.print(\"SUCCESS\" + j);\n" +
				"		} catch(@A Exception e) {\n" +
				"		}\n" +
				"		@B int k = 3;\n" +
				"		System.out.println(k);\n" +
				"	}\n" +
				"}",
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
		},
		"SUCCESS93");
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #56 @B(\n" +
			"        target type = 0x40 LOCAL_VARIABLE\n" +
			"        local variable entries:\n" +
			"          [pc: 3, pc: 39] index: 1\n" +
			"      )\n" +
			"      #56 @B(\n" +
			"        target type = 0x40 LOCAL_VARIABLE\n" +
			"        local variable entries:\n" +
			"          [pc: 31, pc: 39] index: 2\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test050_codeblocks_localVariable() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"X.java",
				"public class X {\n" +
				"	String[][] bar() {\n" +
				"		return new String[][] {};" +
				"	}\n" +
				"	void foo(String s) {\n" +
				"		@C int i;\n" +
				"		@A String [] @B(3)[] tab = bar();\n" +
				"		if (tab != null) {\n" +
				"			i = 0;\n" +
				"			System.out.println(i + tab.length);\n" +
				"		} else {\n" +
				"			System.out.println(tab.length);\n" +
				"		}\n" +
				"		i = 4;\n" +
				"		System.out.println(-i + tab.length);\n" +
				"	}\n" +
				"}",
		},
		"");
		// javac-b81:
		// Bytes:34[0 2 64 0 1 0 34 0 12 0 2 0 0 19 0 0 64 0 1 0 5 0 41 0 3 2 0 0 0 0 0 20 0 0]
		// Bytes:23[0 1 64 0 1 0 5 0 41 0 3 1 0 0 0 22 0 1 0 23 73 0 24]
		// ECJ data varies a little here as it is splitting the range
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #45 @B(\n" +
			"        #46 value=(int) 3 (constant type)\n" +
			"        target type = 0x40 LOCAL_VARIABLE\n" +
			"        local variable entries:\n" +
			"          [pc: 5, pc: 46] index: 3\n" +
			"        location = [ARRAY]\n" +
			"      )\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #49 @C(\n" +
			"        target type = 0x40 LOCAL_VARIABLE\n" +
			"        local variable entries:\n" +
			"          [pc: 11, pc: 24] index: 2\n" +
			"          [pc: 34, pc: 46] index: 2\n" +
			"      )\n" +
			"      #50 @A(\n" +
			"        target type = 0x40 LOCAL_VARIABLE\n" +
			"        local variable entries:\n" +
			"          [pc: 5, pc: 46] index: 3\n" +
			"        location = [ARRAY, ARRAY]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test051_codeblocks_resourceVariable() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"X.java",
				"import java.io.*;\n"+
				"public class X {\n" +
				"   public static void main(String[] argv) throws Exception {\n"+
				"     try (@A BufferedReader br1 = new BufferedReader(new FileReader(\"a\"));\n"+
				"          @B(99) BufferedReader br2 = new BufferedReader(new FileReader(\"b\"))) {\n"+
				"       System.out.println(br1.readLine()+br2.readLine());\n" +
				"     }\n" +
				"	}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #81 @B(\n" +
			"        #82 value=(int) 99 (constant type)\n" +
			"        target type = 0x41 RESOURCE_VARIABLE\n" +
			"        local variable entries:\n" +
			"          [pc: 39, pc: 94] index: 4\n" +
			"      )\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #85 @A(\n" +
			"        target type = 0x41 RESOURCE_VARIABLE\n" +
			"        local variable entries:\n" +
			"          [pc: 21, pc: 135] index: 3\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test052_codeblocks_exceptionParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Exception test = new Exception() {\n" +
				"			private static final long serialVersionUID = 1L;\n" +
				"			@Override\n" +
				"			public String toString() {\n" +
				"				return \"SUCCESS\";\n" +
				"			}\n" +
				"		};\n" +
				"		try {\n" +
				"			System.out.println(test);\n" +
				"		} catch(@A Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}\n" +
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
		},
		"SUCCESS");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #44 @A(\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 0\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test053_codeblocks_exceptionParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		@A Exception test = new Exception() {\n" +
				"			private static final long serialVersionUID = 1L;\n" +
				"			@Override\n" +
				"			public String toString() {\n" +
				"				return \"SUCCESS\";\n" +
				"			}\n" +
				"		};\n" +
				"		try {\n" +
				"			System.out.println(test);\n" +
				"		} catch(@A Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
		},
		"SUCCESS");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #44 @A(\n" +
			"        target type = 0x40 LOCAL_VARIABLE\n" +
			"        local variable entries:\n" +
			"          [pc: 8, pc: 24] index: 1\n" +
			"      )\n" +
			"      #44 @A(\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 0\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test054_codeblocks_exceptionParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			System.out.println(42);\n" +
				"		} catch(@B(1) RuntimeException e) {\n" +
				"			e.printStackTrace();\n" +
				"		} catch(@B(2) Throwable t) {\n" +
				"			t.printStackTrace();\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default 99;\n" +
				"}\n",
		},
		"42");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #44 @B(\n" +
			"        #45 value=(int) 1 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 0\n" +
			"      )\n" +
			"      #44 @B(\n" +
			"        #45 value=(int) 2 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 1\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test055_codeblocks_exceptionParameterMultiCatch() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"class Exc1 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"class Exc2 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"class Exc3 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			System.out.println(42);\n" +
				// @B(1) is attached to the argument, the others are attached to the type reference in the union type reference
				// During Parsing the @B(1) is moved from the argument to Exc1
				"		} catch(@B(1) Exc1 | Exc2 | @B(2) Exc3 t) {\n" +
				"			t.printStackTrace();\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default 99;\n" +
				"}\n",
		},
		"42");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #45 @B(\n" +
			"        #46 value=(int) 1 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 0\n" +
			"      )\n" +
			"      #45 @B(\n" +
			"        #46 value=(int) 2 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 2\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test056_codeblocks_instanceof() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo(Object o) {\n" +
				"		if (o instanceof @A String) {\n" +
				"			String tab = (String) o;\n" +
				"			System.out.println(tab);\n" +
				"		}\n" +
				"		System.out.println(o);\n" +
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #38 @A(\n" +
			"        target type = 0x43 INSTANCEOF\n" +
			"        offset = 1\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);

		expectedOutput = "     1  instanceof java.lang.String [16]\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test057_codeblocks_new() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X {\n" +
				"	public boolean foo(String s) {\n" +
				"		System.out.println(\"xyz\");\n" +
				"		Object o = new @B(3) Object();\n" +
				"		return true;\n" +
				"	}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #35 @B(\n" +
			"        #36 value=(int) 3 (constant type)\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 8\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test058_codeblocks_new2() throws Exception {
		this.runConformTest(
			new String[] {
				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",

				"X.java",
				"public class X {\n" +
				"	public void foo() {\n" +
				"       Outer o = new Outer();\n" +
				"       o.new @B(1) Inner();\n" +
				"	}\n" +
				"}\n" +
				"class Outer { class Inner {}}\n"
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #30 @B(\n" +
			"        #31 value=(int) 1 (constant type)\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 8\n" +
			"        location = [INNER_TYPE]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test057_codeblocks_new3_415821() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface X { }\n" +
				"\n" +
				"class Foo {}\n",
				"C.java",
				"class C { void m() { new @X Foo() {}; } }\n",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #21 @X(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 0\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "C.class", "C", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
			"  RuntimeVisibleTypeAnnotations: \n" +
			"    #28 @X(\n" +
			"      target type = 0x10 CLASS_EXTENDS\n" +
			"      type index = -1\n" +
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "C$1.class", "C$1", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test057_codeblocks_new4_415821() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface X { }\n" +
				"\n",
				"C.java",
				"class C { void m() { new @X Runnable() { public void run() {}}; } }\n",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #21 @X(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 0\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "C.class", "C", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
			"  RuntimeVisibleTypeAnnotations: \n" +
			"    #31 @X(\n" +
			"      target type = 0x10 CLASS_EXTENDS\n" +
			"      type index = 0\n" +
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "C$1.class", "C$1", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test059_codeblocks_new_newArray() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X {\n" +
				"	public boolean foo(String s) {\n" +
				"		System.out.println(\"xyz\");\n" +
				"		Object o = new @A String [1];\n" +
				"		return true;\n" +
				"	}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #37 @A(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 9\n" +
			"        location = [ARRAY]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test060_codeblocks_new_multiNewArray() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"X.java",
				"public class X {\n" +
				"	public boolean foo(String s) {\n" +
				"		System.out.println(\"xyz\");\n" +
				"		Object o = new @A String [2][3];\n" +
				"		return true;\n" +
				"	}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #37 @A(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 10\n" +
			"        location = [ARRAY, ARRAY]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test060a_codeblocks_new_newArrayWithInitializer() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public boolean foo(String s) {\n" +
				"		System.out.println(\"xyz\");\n" +
				"		X[][] x = new @A X @B [] @C[]{ { null }, { null } };\n" +
				"		return true;\n" +
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface B {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

				"C.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface C {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
		},
		"");
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #37 @A(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 9\n" +
			"        location = [ARRAY, ARRAY]\n" +
			"      )\n" +
			"      #38 @B(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 9\n" +
			"      )\n" +
			"      #39 @C(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 9\n" +
			"        location = [ARRAY]\n" +
			"      )\n" +
			"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test060b_codeblocks_new_multiNewArray() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public boolean foo(String s) {\n" +
				"		System.out.println(\"xyz\");\n" +
				"		X[][] x = new @A X @B [1] @C[2];\n" +
				"		return true;\n" +
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface B {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

				"C.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface C {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
		},
		"");
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #36 @A(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 10\n" +
			"        location = [ARRAY, ARRAY]\n" +
			"      )\n" +
			"      #37 @B(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 10\n" +
			"      )\n" +
			"      #38 @C(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 10\n" +
			"        location = [ARRAY]\n" +
			"      )\n" +
			"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test060c_codeblocks_new_multiNewArray() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public boolean foo(String s) {\n" +
				"		System.out.println(\"xyz\");\n" +
				"		X [][][] x = new @A X @B[10] @C[10] @D[];\n" +
				"		return true;\n" +
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface B {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

				"C.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface C {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

				"D.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface D {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
		},
		"");
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #36 @A(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 12\n" +
			"        location = [ARRAY, ARRAY, ARRAY]\n" +
			"      )\n" +
			"      #37 @B(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 12\n" +
			"      )\n" +
			"      #38 @C(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 12\n" +
			"        location = [ARRAY]\n" +
			"      )\n" +
			"      #39 @D(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 12\n" +
			"        location = [ARRAY, ARRAY]\n" +
			"      )\n" +
			"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test060d_codeblocks_new_arraysWithNestedTypes() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public boolean foo(String s) {\n" +
				"		System.out.println(\"xyz\");\n" +
				"		Object o = new @B(1) Outer.@B(2) Inner @B(3) [2];\n" +
				"		return true;\n" +
				"	}\n" +
				"}\n" +
				"class Outer { class Inner {}}\n",
				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default 99;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #37 @B(\n" +
			"        #38 value=(int) 1 (constant type)\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 9\n" +
			"        location = [ARRAY]\n" +
			"      )\n" +
			"      #37 @B(\n" +
			"        #38 value=(int) 2 (constant type)\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 9\n" +
			"        location = [ARRAY, INNER_TYPE]\n" +
			"      )\n" +
			"      #37 @B(\n" +
			"        #38 value=(int) 3 (constant type)\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 9\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test060e_codeblocks_new_arraysWithNestedTypes() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public boolean foo(String s) {\n" +
				"		System.out.println(\"xyz\");\n" +
				"		Object o = new @B(1) Outer.@B(2) Inner @B(3) [2] @B(4)[4];\n" +
				"		return true;\n" +
				"	}\n" +
				"}\n" +
				"class Outer { class Inner {}}\n",
				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default 99;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #37 @B(\n" +
			"        #38 value=(int) 1 (constant type)\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 10\n" +
			"        location = [ARRAY, ARRAY]\n" +
			"      )\n" +
			"      #37 @B(\n" +
			"        #38 value=(int) 2 (constant type)\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 10\n" +
			"        location = [ARRAY, ARRAY, INNER_TYPE]\n" +
			"      )\n" +
			"      #37 @B(\n" +
			"        #38 value=(int) 3 (constant type)\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 10\n" +
			"      )\n" +
			"      #37 @B(\n" +
			"        #38 value=(int) 4 (constant type)\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 10\n" +
			"        location = [ARRAY]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test060f_codeblocks_new_arraysWithQualifiedNestedTypes() throws Exception {
		this.runConformTest(
			new String[] {
				"Z.java",
				"public class Z {}",
				"X.java",
				"package org.foo.bar;\n" +
				"public class X {\n" +
				"	public boolean foo(String s) {\n" +
				"		System.out.println(\"xyz\");\n" +
				"		Object o = new org.foo.bar.@B(1) Outer.@B(2) Inner @B(3) [2] @B(4)[4];\n" +
				"		return true;\n" +
				"	}\n" +
				"}\n" +
				"class Outer { class Inner {}}\n",
				"B.java",
				"package org.foo.bar;\n" +
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default 99;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #37 @org.foo.bar.B(\n" +
			"        #38 value=(int) 1 (constant type)\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 10\n" +
			"        location = [ARRAY, ARRAY]\n" +
			"      )\n" +
			"      #37 @org.foo.bar.B(\n" +
			"        #38 value=(int) 2 (constant type)\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 10\n" +
			"        location = [ARRAY, ARRAY, INNER_TYPE]\n" +
			"      )\n" +
			"      #37 @org.foo.bar.B(\n" +
			"        #38 value=(int) 3 (constant type)\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 10\n" +
			"      )\n" +
			"      #37 @org.foo.bar.B(\n" +
			"        #38 value=(int) 4 (constant type)\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 10\n" +
			"        location = [ARRAY]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "org" + File.separator + "foo" + File.separator + "bar" + File.separator + "X.class",
				"org.foo.bar.X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test061_codeblocks_new_newArrayWithInitializer() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X {\n" +
				"	public boolean foo(String s) {\n" +
				"		System.out.println(\"xyz\");\n" +
				"		Object o = new @A String []{\"xyz\"};\n" +
				"		return true;\n" +
				"	}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #37 @A(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 9\n" +
			"        location = [ARRAY]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test062_codeblocks_newArray() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X {\n" +
				"	public boolean foo(String s) {\n" +
				"		System.out.println(\"xyz\");\n" +
				"		Object o = new String @A[1];\n" +
				"		return true;\n" +
				"	}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #37 @A(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 9\n" +
			// no type path expected here
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test062_codeblocks_newArrayWithInitializer() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X {\n" +
				"	public boolean foo(String s) {\n" +
				"		System.out.println(\"xyz\");\n" +
				"		Object o = new String @A[] { \"Hello\" };\n" +
				"		return true;\n" +
				"	}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #39 @A(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 9\n" +
			// no type path expected here
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test063_codeblocks_new_instanceof() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X {\n" +
				"	public boolean foo(Object o) {\n" +
				"		boolean b = (o instanceof @C('_') Object[]);\n" +
				"		Object o1 = new @B(3) @A(\"new Object\") Object[] {};\n" +
				"		return b;\n" +
				"	}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #24 @B(\n" +
			"        #25 value=(int) 3 (constant type)\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 6\n" +
			"        location = [ARRAY]\n" +
			"      )\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #28 @C(\n" +
			"        #25 value=\'_\' (constant type)\n" +
			"        target type = 0x43 INSTANCEOF\n" +
			"        offset = 1\n" +
			"        location = [ARRAY]\n" +
			"      )\n" +
			"      #30 @A(\n" +
			"        #25 value=\"new Object\" (constant type)\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 6\n" +
			"        location = [ARRAY]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	public void test064_codeblocks_constructorReference() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"interface MR { X process(String input); }\n"+
				"public class X<T> {\n" +
				"   public X(T t) {}\n" +
				"   public static <T> String foo(String bar) { return bar; }\n"+
				"	public void bar() {\n" +
				"       System.out.println(\"abc\");\n" +
				"       MR ref = @A X::new;\n" +
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #48 @A(\n" +
			"        target type = 0x45 CONSTRUCTOR_REFERENCE\n" +
			"        offset = 8\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	public void test065_codeblocks_methodReference() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"interface MR { String process(String input); }\n"+
				"public class X<T> {\n" +
				"   public static <T> String foo(String bar) { return bar; }\n"+
				"	public void bar() {\n" +
				"       System.out.println(\"abc\");\n" +
				"       MR ref = @A X::foo;\n" +
				"       ref.process(\"abc\");\n" +
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #47 @A(\n" +
			"        target type = 0x46 METHOD_REFERENCE\n" +
			"        offset = 8\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test066_codeblocks_methodReference() throws Exception {
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
				"X.java",
				"interface I {\n" +
				"    Object copy(int [] ia);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = @B(1) int @B(2)[]::<String>clone;\n" +
				"        i.copy(new int[10]); \n" +
				"    }\n" +
				"}\n",

				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}\n",
			};
		if (this.complianceLevel < ClassFileConstants.JDK9) { // luckily introduction of ecj warning and javac crash coincide
			runner.runConformTest();
		} else {
			runner.expectedCompilerLog =
				"----------\n" +
				"1. WARNING in X.java (at line 6)\n" +
				"	I i = @B(1) int @B(2)[]::<String>clone;\n" +
				"	                          ^^^^^^\n" +
				"Unused type arguments for the non generic method clone() of type Object; it should not be parameterized with arguments <String>\n" +
				"----------\n";
			runner.javacTestOptions = JavacHasABug.JavacThrowsAnExceptionForJava_since9_EclipseWarns;
			runner.runWarningTest();
		}

		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #30 @B(\n" +
			"        #31 value=(int) 1 (constant type)\n" +
			"        target type = 0x46 METHOD_REFERENCE\n" +
			"        offset = 0\n" +
			"        location = [ARRAY]\n" +
			"      )\n" +
			"      #30 @B(\n" +
			"        #31 value=(int) 2 (constant type)\n" +
			"        target type = 0x46 METHOD_REFERENCE\n" +
			"        offset = 0\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test067_codeblocks_constructorReferenceTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"interface MR { X process(String input); }\n" +
				"public class X<T> {\n" +
				"   public X(T s) {};\n" +
				"   public static <T> String foo(String bar) { return bar; }\n"+
				"	public void bar() {\n" +
				"       System.out.println(\"abc\");\n" +
				"       MR ref = X<String>::<@A String>new;\n" +
				"       ref.process(\"abc\");\n" +
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #53 @A(\n" +
			"        target type = 0x4a CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT\n" +
			"        offset = 8\n" +
			"        type argument index = 0\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test068_codeblocks_methodReferenceTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"interface MR { String process(String input); }\n"+
				"public class X<T> {\n" +
				"   public static <T> String foo(String bar) { return bar; }\n"+
				"	public void bar() {\n" +
				"       System.out.println(\"abc\");\n" +
				"       MR ref = X::<@A String>foo;\n" +
				"       ref.process(\"abc\");\n" +
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #47 @A(\n" +
			"        target type = 0x4b METHOD_REFERENCE_TYPE_ARGUMENT\n" +
			"        offset = 8\n" +
			"        type argument index = 0\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test069_codeblocks_cast() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo(Object o) {\n" +
				"		if (o instanceof String) {\n" +
				"			String tab = (@A String) o;\n" +
				"			System.out.println(tab);\n" +
				"		}\n" +
				"		System.out.println(o);\n" +
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

		},
		"");
		// javac-b81: Bytes:11[0 1 71 0 7 0 0 0 16 0 0]
		// relevant numbers '71 0 7 0' which mean 0x47 (CAST) at offset 7
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #38 @A(\n" +
			"        target type = 0x47 CAST\n" +
			"        offset = 8\n" +
			"        type argument index = 0\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test070_codeblocks_cast_complex() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X {\n" +
				"	public void foo(Object o) {\n" +
				"		if (o instanceof String[][]) {\n" +
				"			String[][] tab = (@C('_') @B(3) String[] @A[]) o;\n" +
				"			System.out.println(tab.length);\n" +
				"		}\n" +
				"		System.out.println(o);\n" +
				"	}\n" +
				"}",
		},
		"");
		// javac-b81:
		// Bytes:31[0 2 71 0 7 0 1 0 0 0 16 0 0 71 0 7 0 2 0 0 0 0 0 17 0 1 0 18 67 0 19]
		// Bytes:20[0 1 71 0 7 0 2 0 0 0 0 0 21 0 1 0 18 73 0 22]
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #37 @B(\n" +
			"        #38 value=(int) 3 (constant type)\n" +
			"        target type = 0x47 CAST\n" +
			"        offset = 8\n" +
			"        type argument index = 0\n" +
			"        location = [ARRAY, ARRAY]\n" +
			"      )\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #41 @C(\n" +
			"        #38 value=\'_\' (constant type)\n" +
			"        target type = 0x47 CAST\n" +
			"        offset = 8\n" +
			"        type argument index = 0\n" +
			"        location = [ARRAY, ARRAY]\n" +
			"      )\n" +
			"      #43 @A(\n" +
			"        target type = 0x47 CAST\n" +
			"        offset = 8\n" +
			"        type argument index = 0\n" +
			"        location = [ARRAY]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test070a_codeblocks_castWithIntersectionCast() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
			"import java.io.*;\n" +
				"public class X {\n" +
				"   public void foo(Object o) {\n" +
				"	  I i = (@B(1) I & J) o;\n" +
				"	  J j = (I & @B(2) J) o;\n" +
			    "   }\n" +
				"}\n" +
				"interface I {}\n" +
				"interface J {}\n",

				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default 1;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  // Method descriptor #15 (Ljava/lang/Object;)V\n" +
				"  // Stack: 1, Locals: 4\n" +
				"  public void foo(java.lang.Object o);\n" +
				"     0  aload_1 [o]\n" +
				"     1  checkcast J [16]\n" +
				"     4  checkcast I [18]\n" +
				"     7  astore_2 [i]\n" +
				"     8  aload_1 [o]\n" +
				"     9  checkcast J [16]\n" +
				"    12  checkcast I [18]\n" +
				"    15  astore_3 [j]\n" +
				"    16  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 4]\n" +
				"        [pc: 8, line: 5]\n" +
				"        [pc: 16, line: 6]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 17] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 17] local: o index: 1 type: java.lang.Object\n" +
				"        [pc: 8, pc: 17] local: i index: 2 type: I\n" +
				"        [pc: 16, pc: 17] local: j index: 3 type: J\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #27 @B(\n" +
				"        #28 value=(int) 1 (constant type)\n" +
				"        target type = 0x47 CAST\n" +
				"        offset = 4\n" +
				"        type argument index = 0\n" +
				"      )\n" +
				"      #27 @B(\n" +
				"        #28 value=(int) 2 (constant type)\n" +
				"        target type = 0x47 CAST\n" +
				"        offset = 9\n" +
				"        type argument index = 1\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test070b_codeblocks_castWithIntersectionCast() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
			"import java.io.*;\n" +
				"public class X {\n" +
				"   public void foo(Object o) {\n" +
				"     System.out.println(123);\n" +
				"	  I<String> i = (I<@B(1) String> & @B(2) J<String>) o;\n" +
			    "   }\n" +
				"}\n" +
				"interface I<T> {}\n" +
				"interface J<T> {}\n",

				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default 1;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  public void foo(java.lang.Object o);\n" +
				"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
				"     3  bipush 123\n" +
				"     5  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
				"     8  aload_1 [o]\n" +
				"     9  checkcast J [28]\n" +
				"    12  checkcast I [30]\n" +
				"    15  astore_2 [i]\n" +
				"    16  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 4]\n" +
				"        [pc: 8, line: 5]\n" +
				"        [pc: 16, line: 6]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 17] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 17] local: o index: 1 type: java.lang.Object\n" +
				"        [pc: 16, pc: 17] local: i index: 2 type: I\n" +
				"      Local variable type table:\n" +
				"        [pc: 16, pc: 17] local: i index: 2 type: I<java.lang.String>\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #39 @B(\n" +
				"        #40 value=(int) 2 (constant type)\n" +
				"        target type = 0x47 CAST\n" +
				"        offset = 9\n" +
				"        type argument index = 1\n" +
				"      )\n" +
				"      #39 @B(\n" +
				"        #40 value=(int) 1 (constant type)\n" +
				"        target type = 0x47 CAST\n" +
				"        offset = 12\n" +
				"        type argument index = 0\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test070c_codeblocks_castTwiceInExpression() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
			"import java.io.*;\n" +
				"public class X {\n" +
				"   public void foo(Object o) {\n" +
				"     System.out.println(123);\n" +
				"	  I i = (@B(1) I)(@B(2) J) o;\n" +
			    "   }\n" +
				"}\n" +
				"interface I {}\n" +
				"interface J {}\n",

				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default 1;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
				"     3  bipush 123\n" +
				"     5  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
				"     8  aload_1 [o]\n" +
				"     9  checkcast J [28]\n" +
				"    12  checkcast I [30]\n" +
				"    15  astore_2 [i]\n" +
				"    16  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 4]\n" +
				"        [pc: 8, line: 5]\n" +
				"        [pc: 16, line: 6]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 17] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 17] local: o index: 1 type: java.lang.Object\n" +
				"        [pc: 16, pc: 17] local: i index: 2 type: I\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #37 @B(\n" +
				"        #38 value=(int) 2 (constant type)\n" +
				"        target type = 0x47 CAST\n" +
				"        offset = 9\n" +
				"        type argument index = 0\n" +
				"      )\n" +
				"      #37 @B(\n" +
				"        #38 value=(int) 1 (constant type)\n" +
				"        target type = 0x47 CAST\n" +
				"        offset = 12\n" +
				"        type argument index = 0\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test070d_codeblocks_castDoubleIntersectionCastInExpression() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
			"import java.io.*;\n" +
				"public class X {\n" +
				"   public void foo(Object o) {\n" +
				"     System.out.println(123);\n" +
				"	  I i = (@B(1) I & J)(K & @B(2) L) o;\n" +
			    "   }\n" +
				"}\n" +
				"interface I {}\n" +
				"interface J {}\n" +
				"interface K {}\n" +
				"interface L {}\n",

				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default 1;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  public void foo(java.lang.Object o);\n" +
				"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
				"     3  bipush 123\n" +
				"     5  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
				"     8  aload_1 [o]\n" +
				"     9  checkcast L [28]\n" +
				"    12  checkcast K [30]\n" +
				"    15  checkcast J [32]\n" +
				"    18  checkcast I [34]\n" +
				"    21  astore_2 [i]\n" +
				"    22  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 4]\n" +
				"        [pc: 8, line: 5]\n" +
				"        [pc: 22, line: 6]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 23] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 23] local: o index: 1 type: java.lang.Object\n" +
				"        [pc: 22, pc: 23] local: i index: 2 type: I\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #41 @B(\n" +
				"        #42 value=(int) 2 (constant type)\n" +
				"        target type = 0x47 CAST\n" +
				"        offset = 9\n" +
				"        type argument index = 1\n" +
				"      )\n" +
				"      #41 @B(\n" +
				"        #42 value=(int) 1 (constant type)\n" +
				"        target type = 0x47 CAST\n" +
				"        offset = 18\n" +
				"        type argument index = 0\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test071_codeblocks_constructorInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"X.java",
				"public class X {\n" +
				"	<T> X(T t) {\n" +
				"	}\n" +
				"	public Object foo() {\n" +
				"		X x = new <@A @B(1) String>X(null);\n" +
				"		return x;\n" +
				"	}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #27 @B(\n" +
			"        #28 value=(int) 1 (constant type)\n" +
			"        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT\n" +
			"        offset = 5\n" +
			"        type argument index = 0\n" +
			"      )\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #31 @A(\n" +
			"        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT\n" +
			"        offset = 5\n" +
			"        type argument index = 0\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test072_codeblocks_constructorInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"X.java",
				"public class X {\n" +
				"	<T, U> X(T t, U u) {\n" +
				"	}\n" +
				"	public Object foo() {\n" +
				"		X x = new <@A Integer, @A String @C [] @B(1)[]>X(null, null);\n" +
				"		return x;\n" +
				"	}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #29 @B(\n" +
			"        #30 value=(int) 1 (constant type)\n" +
			"        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT\n" +
			"        offset = 6\n" +
			"        type argument index = 1\n" +
			"        location = [ARRAY]\n" +
			"      )\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #33 @A(\n" +
			"        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT\n" +
			"        offset = 6\n" +
			"        type argument index = 0\n" +
			"      )\n" +
			"      #33 @A(\n" +
			"        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT\n" +
			"        offset = 6\n" +
			"        type argument index = 1\n" +
			"        location = [ARRAY, ARRAY]\n" +
			"      )\n" +
			"      #34 @C(\n" +
			"        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT\n" +
			"        offset = 6\n" +
			"        type argument index = 1\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test073_codeblocks_constructorInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T1, T2> {\n" +
				"	public void bar() {\n" +
				"       new <String, @A T2>X();\n"+
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

		},
		"");
		// Example bytes:11[0 1 73 0 0 0 0 0 13 0 0] this would be for offset 0
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #19 @A(\n" +
			"        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT\n" +
			"        offset = 3\n" +
			"        type argument index = 1\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test074_codeblocks_constructorInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T1,T2> {\n" +
				"   public static void foo(int i) {}\n"+
				"	public void bar() {\n" +
				"       new <java.util.List<@A String>, T2>X();\n"+
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

		},
		"");
		// Example bytes:11[0 1 73 0 0 0 0 0 13 0 0] this would be for offset 0
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #23 @A(\n" +
			"        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT\n" +
			"        offset = 3\n" +
			"        type argument index = 0\n" +
			"        location = [TYPE_ARGUMENT(0)]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test075_codeblocks_constructorInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	public void bar() {\n" +
				"       new <@A T>X();\n"+
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

		},
		"");
		// Example bytes:11[0 1 73 0 0 0 0 0 13 0 0] this would be for offset 0
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #19 @A(\n" +
			"        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT\n" +
			"        offset = 3\n" +
			"        type argument index = 0\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test076_codeblocks_methodInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	static <T, U> T foo(T t, U u) {\n" +
				"		return t;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(X.<@A @B(1) String[], @C('-') X>foo(new String[]{\"SUCCESS\"}, null)[0]);\n" +
				"	}\n" +
				"}\n",
				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
		},
		"SUCCESS");
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #48 @B(\n" +
			"        #49 value=(int) 1 (constant type)\n" +
			"        target type = 0x49 METHOD_INVOCATION_TYPE_ARGUMENT\n" +
			"        offset = 13\n" +
			"        type argument index = 0\n" +
			"        location = [ARRAY]\n" +
			"      )\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #52 @A(\n" +
			"        target type = 0x49 METHOD_INVOCATION_TYPE_ARGUMENT\n" +
			"        offset = 13\n" +
			"        type argument index = 0\n" +
			"        location = [ARRAY]\n" +
			"      )\n" +
			"      #53 @C(\n" +
			"        #49 value=\'-\' (constant type)\n" +
			"        target type = 0x49 METHOD_INVOCATION_TYPE_ARGUMENT\n" +
			"        offset = 13\n" +
			"        type argument index = 1\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test077_codeblocks_methodInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T1,T2> {\n" +
				"   public static void foo(int i) {}\n"+
				"	public void bar() {\n" +
				"       X.<String, @A T2>foo(42);\n"+
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

		},
		"");
		// Example bytes:11[0 1 73 0 0 0 0 0 13 0 0] this would be for offset 0
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #24 @A(\n" +
			"        target type = 0x49 METHOD_INVOCATION_TYPE_ARGUMENT\n" +
			"        offset = 2\n" +
			"        type argument index = 1\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test078_codeblocks_methodInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T1,T2> {\n" +
				"   public static void foo(int i) {}\n"+
				"	public void bar() {\n" +
				"       X.<java.util.List<@A String>, T2>foo(42);\n"+
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",

		},
		"");
		// Example bytes:11[0 1 73 0 0 0 0 0 13 0 0] this would be for offset 0
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #24 @A(\n" +
			"        target type = 0x49 METHOD_INVOCATION_TYPE_ARGUMENT\n" +
			"        offset = 2\n" +
			"        type argument index = 0\n" +
			"        location = [TYPE_ARGUMENT(0)]\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test079_codeblocks_methodInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"   public static void foo(int i) {}\n"+
				"	public void bar() {\n" +
				"       X.<@A T>foo(42);\n"+
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n",
		},
		"");
		// Example bytes:11[0 1 73 0 0 0 0 0 13 0 0] this would be for offset 0
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #24 @A(\n" +
			"        target type = 0x49 METHOD_INVOCATION_TYPE_ARGUMENT\n" +
			"        offset = 2\n" +
			"        type argument index = 0\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	// Annotation should appear twice in this case
	public void test080_multiuseAnnotations() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	 @B(1) int foo() { return 0; }\n" +
				"}",
				"B.java",
				"import java.lang.annotation.*;\n" +
				"@Target({ElementType.METHOD, ElementType.TYPE_USE})\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default 99;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleAnnotations: \n" +
			"      #17 @B(\n" +
			"        #18 value=(int) 1 (constant type)\n" +
			"      )\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #17 @B(\n" +
			"        #18 value=(int) 1 (constant type)\n" +
			"        target type = 0x14 METHOD_RETURN\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test081_multiuseAnnotations() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"@Target({ElementType.METHOD, ElementType.TYPE_USE})\n" +
				"@interface Annot {\n" +
				"	int value() default 0;\n" +
				"}\n" +
				"public class X {\n" +
				"	@Annot(4) public String foo() { return \"hello\"; }" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeInvisibleAnnotations: \n" +
			"      #17 @Annot(\n" +
			"        #18 value=(int) 4 (constant type)\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #17 @Annot(\n" +
			"        #18 value=(int) 4 (constant type)\n" +
			"        target type = 0x14 METHOD_RETURN\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	// When not annotated with any TYPE it assumes the Java7 set (i.e. not TYPE_USE/TYPE_PARAMETER)
	public void test082_multiuseAnnotations() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"@Target({ElementType.METHOD, ElementType.TYPE_USE})\n" +
				"@interface Annot {\r\n" +
				"	int value() default 0;\r\n" +
				"}\r\n" +
				"public class X {\r\n" +
				"	@Annot(4)\r\n" +
				"	public void foo() {\r\n" +
				"	}\r\n" +
				"}",
		},
		"");
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  public void foo();\n" +
			"    0  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
			"    RuntimeInvisibleAnnotations: \n" +
			"      #16 @Annot(\n" +
			"        #17 value=(int) 4 (constant type)\n" +
			"      )\n" +
			"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	// as of https://bugs.openjdk.java.net/browse/JDK-8231435 no-@Target annotations are legal also in TYPE_USE/TYPE_PARAMETER position
	public void test083_multiuseAnnotations() throws Exception {
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"@Target({ElementType.METHOD, ElementType.TYPE_USE})\n" +
				"@interface Annot {\n" +
				"	int value() default 0;\n" +
				"}\n" +
				"public class X<@Annot(1) T> {\n" +
				"	java.lang. @Annot(2)String f;\n" +
				"	public void foo(String @Annot(3)[] args) {\n" +
				"	}\n" +
				"}\n",
			};
		runner.expectedCompilerLog = "";
		runner.runConformTest();

		String expectedOutput =
				"  // Field descriptor #6 Ljava/lang/String;\n" +
				"  java.lang.String f;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @Annot(\n" +
				"        #9 value=(int) 2 (constant type)\n" +  // <-2-
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #12 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public X();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [14]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 6]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"      Local variable type table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X<T>\n" +
				"  \n" +
				"  // Method descriptor #23 ([Ljava/lang/String;)V\n" +
				"  // Stack: 0, Locals: 2\n" +
				"  public void foo(java.lang.String[] args);\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 9]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 1] local: args index: 1 type: java.lang.String[]\n" +
				"      Local variable type table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X<T>\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @Annot(\n" +
				"        #9 value=(int) 3 (constant type)\n" +  // <-3-
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n" +
				"\n" +
				"  RuntimeInvisibleTypeAnnotations: \n" +
				"    #8 @Annot(\n" +
				"      #9 value=(int) 1 (constant type)\n" +  // <-1-
				"      target type = 0x0 CLASS_TYPE_PARAMETER\n" +
				"      type parameter index = 0\n" +
				"    )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100_pqtr() throws Exception { // PQTR (ParameterizedQualifiedTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"  java.util.@B(2) List<String> field2;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #10 @B(\n" +
					"        #11 value=(int) 2 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100a_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"  java.util.@B(2) List<String>[] field3;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #10 @B(\n" +
					"        #11 value=(int) 2 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY]\n" +
					"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100b_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"  java.util.List<@B(3) String>[] field3;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #10 @B(\n" +
					"        #11 value=(int) 3 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, TYPE_ARGUMENT(0)]\n" +
					"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100c_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"  java.util.List<String> @B(3)[] field3;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #10 @B(\n" +
					"        #11 value=(int) 3 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100d_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"  java.util.@B(2) List<@B(5) String> @B(3)[]@B(4)[] field;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #10 @B(\n" +
					"        #11 value=(int) 2 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, ARRAY]\n" +
					"      )\n" +
					"      #10 @B(\n" +
					"        #11 value=(int) 3 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"      )\n" +
					"      #10 @B(\n" +
					"        #11 value=(int) 4 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY]\n" +
					"      )\n" +
					"      #10 @B(\n" +
					"        #11 value=(int) 5 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, ARRAY, TYPE_ARGUMENT(0)]\n" +
					"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	public void test100e_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"  java.util.Map.@B(2) Entry<String,String> field;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 2 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100f_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"Foo.java",
					"class Foo {}\n",

					"Levels.java",
					"package one.two.three;\n" +
					"class Level1 { static class Level2 { class Level3 { class Level4 { class Level5<T> { } } } } }\n",

					"X.java",
					"package one.two.three;\n" +
					"class X {\n" +
					"  one.two.three.Level1.Level2.@B(2) Level3.Level4.@B(3) Level5<String> instance;\n" +
					"}\n",

					"B.java",
					"package one.two.three;\n" +
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @one.two.three.B(\n" +
				"        #11 value=(int) 2 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [INNER_TYPE]\n" +
				"      )\n" +
				"      #10 @one.two.three.B(\n" +
				"        #11 value=(int) 3 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [INNER_TYPE, INNER_TYPE, INNER_TYPE]\n" +
				"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "one" + File.separator + "two" + File.separator + "three" + File.separator + "X.class", "one.two.three.X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100g_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"Foo.java",
					"class Foo {}\n",

					"Levels.java",
					"package one.two.three;\n" +
					"class Level1 { static class Level2 { class Level3 { class Level4 { class Level5<T> { } } } } }\n",

					"X.java",
					"package one.two.three;\n" +
					"class X {\n" +
					"  one.two.three.Level1.Level2.@B(2) Level3.Level4.@B(3) Level5<String>[][] instance;\n" +
					"}\n",

					"B.java",
					"package one.two.three;\n" +
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @one.two.three.B(\n" +
				"        #11 value=(int) 2 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY, INNER_TYPE]\n" +
				"      )\n" +
				"      #10 @one.two.three.B(\n" +
				"        #11 value=(int) 3 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY, INNER_TYPE, INNER_TYPE, INNER_TYPE]\n" +
				"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "one" + File.separator + "two" + File.separator + "three" + File.separator + "X.class", "one.two.three.X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100h_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"  Level1.Level2.@B(2) Level3.Level4.@B(3) Level5<String>[][] instance;\n" +
					"}\n",

					"Levels.java",
					"class Level1 { static class Level2 { class Level3 { class Level4 { class Level5<T> { } } } } }\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 2 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY, INNER_TYPE]\n" +
				"      )\n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 3 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY, INNER_TYPE, INNER_TYPE, INNER_TYPE]\n" +
				"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100i_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"  Level1.Level2.Level3.Level4.Level5<@B(1) String>[][] instance;\n" +
					"}\n",

					"Levels.java",
					"class Level1 { static class Level2 { class Level3 { class Level4 { class Level5<T> { } } } } }\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 1 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY, INNER_TYPE, INNER_TYPE, INNER_TYPE, TYPE_ARGUMENT(0)]\n" +
				"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100j_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"  Level1.Level2.Level3<@B(1) String>.Level4.Level5<@B(2) String>[][] instance;\n" +
					"}\n",

					"Levels.java",
					"class Level1 { static class Level2 { class Level3<Q> { class Level4 { class Level5<T> { } } } } }\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 1 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY, INNER_TYPE, TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 2 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY, INNER_TYPE, INNER_TYPE, INNER_TYPE, TYPE_ARGUMENT(0)]\n" +
				"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100k_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"  Level1.@B(5) Level2.Level3<@B(1) String>.Level4.Level5<@B(2) String>[][] instance;\n" +
					"}\n",

					"Levels.java",
					"class Level1 { static class Level2 { class Level3<Q> { class Level4 { class Level5<T> { } } } } }\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 5 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY]\n" +
				"      )\n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 1 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY, INNER_TYPE, TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 2 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY, INNER_TYPE, INNER_TYPE, INNER_TYPE, TYPE_ARGUMENT(0)]\n" +
				"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test101a_qtr() throws Exception { // QTR (QualifiedTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"    com.foo.@B(2) List field2;\n" +
					"}\n",

					"List.java",
					"package com.foo;\n"+
					"public class List {}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #8 @B(\n" +
					"        #9 value=(int) 2 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	public void test101b_qtr() throws Exception { // QTR (QualifiedTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"  java.util.Map.@B(2) Entry field;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @B(\n" +
				"        #9 value=(int) 2 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test101c_qtr() throws Exception { // QTR (QualifiedTypeReference)
		this.runConformTest(
				new String[] {
					"Runner.java",
					"public class Runner {}\n",

					"B.java",
					"package one.two.three;\n" +
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",

					"X.java",
					"package one.two.three;\n" +
					"class X {\n" +
					"    one.two.three.Level1.Level2.@B(2) Level3.Level4.@B(3) Level5 instance;\n" +
					"}\n",

					"Level1.java",
					"package one.two.three;\n" +
					"public class Level1 { static class Level2 { class Level3 { class Level4 { class Level5 { } } } } }\n",
			},
			"");
			String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @one.two.three.B(\n" +
				"        #9 value=(int) 2 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [INNER_TYPE]\n" +
				"      )\n" +
				"      #8 @one.two.three.B(\n" +
				"        #9 value=(int) 3 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [INNER_TYPE, INNER_TYPE, INNER_TYPE]\n" +
				"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "one" + File.separator + "two" + File.separator + "three" + File.separator + "X.class", "one.two.three.X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test102a_str() throws Exception { // STR (SingleTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"    @B(1) X field;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #8 @B(\n" +
					"        #9 value=(int) 1 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test102b_str() throws Exception { // STR (SingleTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"    @B(1) int field;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #8 @B(\n" +
					"        #9 value=(int) 1 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test103a_atr() throws Exception { // ATR (ArrayTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"    @B(1) X[] field;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #8 @B(\n" +
					"        #9 value=(int) 1 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY]\n" +
					"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test103b_atr() throws Exception { // ATR (ArrayTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"    X @B(2)[] field;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #8 @B(\n" +
					"        #9 value=(int) 2 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test103c_atr() throws Exception { // ATR (ArrayTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"    X []@B(3)[] field;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #8 @B(\n" +
					"        #9 value=(int) 3 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY]\n" +
					"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test103d_atr() throws Exception { // ATR (ArrayTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"    X []@B(3)[][] field;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #8 @B(\n" +
					"        #9 value=(int) 3 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY]\n" +
					"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test103e_atr() throws Exception { // ATR (ArrayTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"    @B(1) int []@B(3)[][] field;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #8 @B(\n" +
					"        #9 value=(int) 1 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, ARRAY, ARRAY]\n" +
					"      )\n" +
					"      #8 @B(\n" +
					"        #9 value=(int) 3 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY]\n" +
					"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test104a_pstr() throws Exception { // PSTR (ParameterizedSingleTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"class X<T1,T2,T3> {\n" +
					"    @B(1) X<@B(2) String, @B(3) Integer, @B(4) Boolean> field;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 1 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 2 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 3 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(1)]\n" +
				"      )\n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 4 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(2)]\n" +
				"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test104b_pstr() throws Exception { // PSTR (ParameterizedSingleTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"class X<T1> {\n" +
					"    @B(1) X<@B(2) String> @B(3)[] field;\n" +
					"}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 1 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY]\n" +
				"      )\n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 3 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 2 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, TYPE_ARGUMENT(0)]\n" +
				"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test105a_aqtr() throws Exception { // AQTR (ArrayQualifiedTypeReference)
		this.runConformTest(
				new String[] {
					"Y.java",
					"class Y {}",

					"X.java",
					"package one.two.three;\n" +
					"class X<T1> {\n" +
					"    one.two.three.@B(1) List[] field;\n" +
					"}\n",

					"List.java",
					"package one.two.three;\n" +
					"class List {}\n",

					"B.java",
					"package one.two.three;\n" +
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @one.two.three.B(\n" +
				"        #9 value=(int) 1 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY]\n" +
				"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "one" + File.separator + "two" + File.separator + "three" + File.separator +"X.class",
					"one.two.three.X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test105b_aqtr() throws Exception { // AQTR (ArrayQualifiedTypeReference)
		this.runConformTest(
				new String[] {
					"Y.java",
					"class Y {}",

					"X.java",
					"package one.two.three;\n" +
					"class X<T1> {\n" +
					"    one.two.three.@B(2) List @B(3)[]@B(4)[] field;\n" +
					"}\n",

					"List.java",
					"package one.two.three;\n" +
					"class List {}\n",

					"B.java",
					"package one.two.three;\n" +
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @one.two.three.B(\n" +
				"        #9 value=(int) 2 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY]\n" +
				"      )\n" +
				"      #8 @one.two.three.B(\n" +
				"        #9 value=(int) 3 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"      #8 @one.two.three.B(\n" +
				"        #9 value=(int) 4 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY]\n" +
				"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "one" + File.separator + "two" + File.separator + "three" + File.separator +"X.class",
					"one.two.three.X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test106a_wtr() throws Exception { // WTR (WildcardTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"import java.util.List;\n" +
					"class X<T1> {\n" +
					"	 List<? extends @B(1) Number> field;\n" +
					"}\n",

					"List.java",
					"class List {}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 1 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0), WILDCARD]\n" +
				"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator +"X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test106b_wtr() throws Exception { // WTR (WildcardTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"import java.util.List;\n" +
					"class X<T1> {\n" +
					"	 List<? extends @B(1) Number[]> field;\n" +
					"}\n",

					"List.java",
					"class List {}\n",

					"B.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface B { int value() default -1; }\n",
			},
			"");
			String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @B(\n" +
				"        #11 value=(int) 1 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0), WILDCARD, ARRAY]\n" +
				"      )\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=409244, [1.8][compiler] Type annotations on redundant casts dropped.
	public void testAnnotatedRedundantCast() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"	 String s = (@NonNull String) \"Hello\";\n" +
					"}\n",

					"NonNull.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface NonNull {}\n",
			},
			"");
			String expectedOutput =
							"  // Method descriptor #8 ()V\n" +
							"  // Stack: 2, Locals: 1\n" +
							"  X();\n" +
							"     0  aload_0 [this]\n" +
							"     1  invokespecial java.lang.Object() [10]\n" +
							"     4  aload_0 [this]\n" +
							"     5  ldc <String \"Hello\"> [12]\n" +
							"     7  checkcast java.lang.String [14]\n" +
							"    10  putfield X.s : java.lang.String [16]\n" +
							"    13  return\n" +
							"      Line numbers:\n" +
							"        [pc: 0, line: 1]\n" +
							"        [pc: 4, line: 2]\n" +
							"        [pc: 13, line: 1]\n" +
							"      Local variable table:\n" +
							"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
							"    RuntimeVisibleTypeAnnotations: \n" +
							"      #23 @NonNull(\n" +
							"        target type = 0x47 CAST\n" +
							"        offset = 7\n" +
							"        type argument index = 0\n" +
							"      )\n" +
							"}";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=409244, [1.8][compiler] Type annotations on redundant casts dropped.
	public void testAnnotatedRedundantCast2() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"	 String s = (String) \"Hello\";\n" +
					"}\n",

					"NonNull.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface NonNull {}\n",
			},
			"");
			String expectedOutput =
							"  // Method descriptor #8 ()V\n" +
							"  // Stack: 2, Locals: 1\n" +
							"  X();\n" +
							"     0  aload_0 [this]\n" +
							"     1  invokespecial java.lang.Object() [10]\n" +
							"     4  aload_0 [this]\n" +
							"     5  ldc <String \"Hello\"> [12]\n" +
							"     7  putfield X.s : java.lang.String [14]\n" +
							"    10  return\n" +
							"      Line numbers:\n" +
							"        [pc: 0, line: 1]\n" +
							"        [pc: 4, line: 2]\n" +
							"        [pc: 10, line: 1]\n" +
							"      Local variable table:\n" +
							"        [pc: 0, pc: 11] local: this index: 0 type: X\n" +
							"}";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test055a_codeblocks_exceptionParameterNestedType() throws Exception {
 		this.runConformTest(
 			new String[] {
 				"X.java",
 				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"         foo();\n" +
				"		} catch(@B(1) Outer.@B(2) MyException e) {\n" +
				"			e.printStackTrace();\n" +
 				"		}\n" +
 				"	}\n" +
				"   static void foo() throws Outer.MyException {}\n" +
				"}\n" +
				"class Outer {\n" +
				"	class MyException extends Exception {\n" +
				"		private static final long serialVersionUID = 1L;\n" +
				"	}\n" +
 				"}",

				"B.java",
 				"import java.lang.annotation.*;\n" +
 				"@Target(ElementType.TYPE_USE)\n" +
 				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default 0;\n" +
 				"}\n",
 		},
 		"");
 		String expectedOutput =
 			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #30 @B(\n" +
			"        #31 value=(int) 1 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 0\n" +
			"      )\n" +
			"      #30 @B(\n" +
			"        #31 value=(int) 2 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 0\n" +
			"        location = [INNER_TYPE]\n" +
			"      )\n" +
			"  \n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
 	}

	public void test055b_codeblocks_exceptionParameterMultiCatchNestedType() throws Exception {
 		this.runConformTest(
 			new String[] {
 				"X.java",
 				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"         foo();\n" +
				"		} catch(@B(1) Outer.@B(2) MyException | @B(3) Outer2.@B(4) MyException2 e) {\n" +
				"			e.printStackTrace();\n" +
				"		}\n" +
 				"	}\n" +
				"   static void foo() throws Outer.MyException, Outer2.MyException2 {}\n" +
				"}\n" +
				"class Outer {\n" +
				"	class MyException extends Exception {\n" +
				"		private static final long serialVersionUID = 1L;\n" +
				"	}\n" +
				"}\n" +
				"class Outer2 {\n" +
				"	class MyException2 extends Exception {\n" +
				"		private static final long serialVersionUID = 1L;\n" +
				"	}\n" +
 				"}",
 				"B.java",
 				"import java.lang.annotation.*;\n" +
 				"@Target(ElementType.TYPE_USE)\n" +
 				"@Retention(RetentionPolicy.RUNTIME)\n" +
 				"@interface B {\n" +
				"	int value() default 0;\n" +
				"}\n",
 		},
 		"");
 		String expectedOutput =
 			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #34 @B(\n" +
			"        #35 value=(int) 1 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 0\n" +
			"      )\n" +
			"      #34 @B(\n" +
			"        #35 value=(int) 2 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 0\n" +
			"        location = [INNER_TYPE]\n" +
			"      )\n" +
			"      #34 @B(\n" +
			"        #35 value=(int) 3 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 1\n" +
			"      )\n" +
			"      #34 @B(\n" +
			"        #35 value=(int) 4 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 1\n" +
			"        location = [INNER_TYPE]\n" +
 			"      )\n";
 		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
 	}

	public void test055c_codeblocks_exceptionParameterMultiCatch() throws Exception {
 		this.runConformTest(
 			new String[] {
 				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"class Exc1 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"class Exc2 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"class Exc3 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
 				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			System.out.println(42);\n" +
				"		} catch(Exc1 | @B(1) Exc2 | @B(2) Exc3 t) {\n" +
				"			t.printStackTrace();\n" +
				"		}\n" +
 				"	}\n" +
 				"}",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default 99;\n" +
				"}\n",
 		},
		"42");
 		String expectedOutput =
 			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #45 @B(\n" +
			"        #46 value=(int) 1 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 1\n" +
			"      )\n" +
			"      #45 @B(\n" +
			"        #46 value=(int) 2 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 2\n" +
 			"      )\n";
 		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
 	}

	public void test055d_codeblocks_exceptionParameterMultiCatch() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"class Exc1 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"class Exc2 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"class Exc3 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			System.out.println(42);\n" +
				"		} catch(@A(1) @B(2) Exc1 | Exc2 | @A(3) @B(4) Exc3 t) {\n" +
				"			t.printStackTrace();\n" +
				"		}\n" +
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	int value() default 99;\n" +
				"}\n",

				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default 99;\n" +
				"}\n",
		},
		"42");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #45 @A(\n" +
			"        #46 value=(int) 1 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 0\n" +
			"      )\n" +
			"      #48 @B(\n" +
			"        #46 value=(int) 2 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 0\n" +
			"      )\n" +
			"      #45 @A(\n" +
			"        #46 value=(int) 3 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 2\n" +
			"      )\n" +
			"      #48 @B(\n" +
			"        #46 value=(int) 4 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 2\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test055e_codeblocks_exceptionParameterMultiCatch() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"class Exc1 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"class Exc2 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"class Exc3 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			System.out.println(42);\n" +
				"		} catch(@A(1) @B(2) Exc1 | Exc2 | @A(3) @B(4) Exc3 t) {\n" +
				"			t.printStackTrace();\n" +
				"		}\n" +
				"	}\n" +
				"}",

				"A.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface A {\n" +
				"	int value() default 99;\n" +
				"}\n",

				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default 99;\n" +
				"}\n",
		},
		"42");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #45 @A(\n" +
			"        #46 value=(int) 1 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 0\n" +
			"      )\n" +
			"      #48 @B(\n" +
			"        #46 value=(int) 2 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 0\n" +
			"      )\n" +
			"      #45 @A(\n" +
			"        #46 value=(int) 3 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 2\n" +
			"      )\n" +
			"      #48 @B(\n" +
			"        #46 value=(int) 4 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 2\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test055f_codeblocks_exceptionParameterComplex() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"class Exc1 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"class Exc2 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"class Exc3 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			System.out.println(42);\n" +
				"		} catch(@B(1) Exc1 | Exc2 | @B(2) Exc3 t) {\n" +
				"			t.printStackTrace();\n" +
				"		}\n" +
				"		try {\n" +
				"			System.out.println(43);\n" +
				"		} catch(@B(1) Exc1 t) {\n" +
				"			t.printStackTrace();\n" +
				"		}\n" +
				"		try {\n" +
				"			System.out.println(44);\n" +
				"		} catch(@B(1) Exc1 | @B(2) Exc2 t) {\n" +
				"			t.printStackTrace();\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface B {\n" +
				"	int value() default 99;\n" +
				"}\n",
		},
		"42\n43\n44");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #47 @B(\n" +
			"        #48 value=(int) 1 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 0\n" +
			"      )\n" +
			"      #47 @B(\n" +
			"        #48 value=(int) 2 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 2\n" +
			"      )\n" +
			"      #47 @B(\n" +
			"        #48 value=(int) 1 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 3\n" +
			"      )\n" +
			"      #47 @B(\n" +
			"        #48 value=(int) 1 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 4\n" +
			"      )\n" +
			"      #47 @B(\n" +
			"        #48 value=(int) 2 (constant type)\n" +
			"        target type = 0x42 EXCEPTION_PARAMETER\n" +
			"        exception table index = 5\n" +
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug415911() {
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface Marker {\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @Marker\n" +
				"    foo(String s) {\n" +
				"\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	foo(String s) {\n" +
			"	^^^^^^^^^^^^^\n" +
			"Return type for the method is missing\n" +
			"----------\n");
	}

	public void testBug426616() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n"+
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Retention(RUNTIME)\n" +
				"@Target(TYPE_USE)\n" +
				"@interface SizeHolder { Size[] value();}\n"+
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@Repeatable(SizeHolder.class)\n"+
				"@interface Size { int max(); }\n"+
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface Nonnull {}\n"+
				"\n"+
				"public class X {\n" +
				"   public static void main(String[]argv) {}\n"+
				"	public static String testArrays() {\n"+
				"		List<@Size(max = 41) CharSequence>[] @Size(max = 42) [] @Nonnull @Size(max = 43) [][] test = new @Size(max = 44) ArrayList @Size(max = 45) [10][][] @Size(max = 47) @Size(max = 48) [];\n"+
				"		return (@Size(max = 49) String) test[0][1][2][3].get(0);\n"+
				"	}\n"+
				"}",
		},
		"");
		// Javac output
		// 0: Size(45): NEW, offset=0
        // 1: SizeHolder([@Size(max=47),@Size(max=48)]): NEW, offset=0, location=[ARRAY, ARRAY, ARRAY]
        // 2: Size(44): NEW, offset=0, location=[ARRAY, ARRAY, ARRAY, ARRAY]
		// 3: Size(49): CAST, offset=6, type_index=0
        // 4: Size(42): LOCAL_VARIABLE, {start_pc=6, length=19, index=0}, location=[ARRAY]
        // 5: NonNull: LOCAL_VARIABLE, {start_pc=6, length=19, index=0}, location=[ARRAY, ARRAY]
        // 6: Size(43): LOCAL_VARIABLE, {start_pc=6, length=19, index=0}, location=[ARRAY, ARRAY]
        // 7: Size(41): LOCAL_VARIABLE, {start_pc=6, length=19, index=0}, location=[ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0)]

		String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +

				// X Maps to javac entry (2): location OK, target type OK, offset different, our offset is 2 and not 0
				"      #33 @Size(\n" +
				"        #34 max=(int) 44 (constant type)\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 2\n" +
				"        location = [ARRAY, ARRAY, ARRAY, ARRAY]\n" +
				"      )\n" +

				// X Maps to javac entry (0), location OK, target type OK, offset different, our offset is 2 and not 0
				"      #33 @Size(\n" +
				"        #34 max=(int) 45 (constant type)\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 2\n" +
				"      )\n" +

				// X Maps to javac entry (1), location OK, target type OK, offset different, our offset is 2 and not 0
				"      #37 @SizeHolder(\n" +
				"        #38 value=[\n" +
				"          annotation value =\n" +
				"              #33 @Size(\n" +
				"                #34 max=(int) 47 (constant type)\n" +
				"              )\n" +
				"          annotation value =\n" +
				"              #33 @Size(\n" +
				"                #34 max=(int) 48 (constant type)\n" +
				"              )\n" +
				"          ]\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 2\n" +
				"        location = [ARRAY, ARRAY, ARRAY]\n" +
				"      )\n" +

				// X Maps to javac entry (3), location OK, target type OK, offset different, our offset is 24 (not 6), type index OK
				"      #33 @Size(\n" +
				"        #34 max=(int) 49 (constant type)\n" +
				"        target type = 0x47 CAST\n" +
				"        offset = 24\n" +
				"        type argument index = 0\n" +
				"      )\n" +

				// Maps to javac entry (4), location OK, target type OK, lvar diff, slight position difference (we seem to have an extra CHECKCAST)
				"      #33 @Size(\n" +
				"        #34 max=(int) 42 (constant type)\n" +
				"        target type = 0x40 LOCAL_VARIABLE\n" +
				"        local variable entries:\n" +
				"          [pc: 6, pc: 28] index: 0\n" +
				"        location = [ARRAY]\n" +
				"      )\n" +

				// Maps to javac entry (5), location OK, taret type OK, lvar diff, slight position difference (we seem to have an extra CHECKCAST)
				"      #43 @Nonnull(\n" +
				"        target type = 0x40 LOCAL_VARIABLE\n" +
				"        local variable entries:\n" +
				"          [pc: 6, pc: 28] index: 0\n" +
				"        location = [ARRAY, ARRAY]\n" +
				"      )\n" +

				// Maps to javac entry (6), location OK, target type OK,  slight position difference (we seem to have an extra CHECKCAST)
				"      #33 @Size(\n" +
				"        #34 max=(int) 43 (constant type)\n" +
				"        target type = 0x40 LOCAL_VARIABLE\n" +
				"        local variable entries:\n" +
				"          [pc: 6, pc: 28] index: 0\n" +
				"        location = [ARRAY, ARRAY]\n" +
				"      )\n" +

				// Maps to javac entry (7), location OK, target type OK, slight position difference (we seem to have an extra CHECKCAST)
				"      #33 @Size(\n" +
				"        #34 max=(int) 41 (constant type)\n" +
				"        target type = 0x40 LOCAL_VARIABLE\n" +
				"        local variable entries:\n" +
				"          [pc: 6, pc: 28] index: 0\n" +
				"        location = [ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0)]\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug426616a() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n"+
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Retention(RUNTIME)\n" +
				"@Target(TYPE_USE)\n" +
				"@interface SizeHolder { Size[] value();}\n"+
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@Repeatable(SizeHolder.class)\n"+
				"@interface Size { int max(); }\n"+
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface Nonnull {}\n"+
				"\n"+
				"public class X {\n" +
				"   List<@Size(max = 41) CharSequence>[] @Size(max = 42) [] @Nonnull @Size(max = 43) [][] test = new @Size(max = 44) ArrayList @Size(max = 45) [10][][] @Size(max = 47) @Size(max = 48) [];\n" +
				"   public static void main(String[]argv) {}\n"+
				"}",
		},
		"");

		String expectedOutput =
				"  // Field descriptor #6 [[[[Ljava/util/List;\n" +
				"  // Signature: [[[[Ljava/util/List<Ljava/lang/CharSequence;>;\n" +
				"  java.util.List[][][][] test;\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @Size(\n" +
				"        #11 max=(int) 42 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY]\n" +
				"      )\n" +
				"      #13 @Nonnull(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY]\n" +
				"      )\n" +
				"      #10 @Size(\n" +
				"        #11 max=(int) 43 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY]\n" +
				"      )\n" +
				"      #10 @Size(\n" +
				"        #11 max=(int) 41 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #17 ()V\n" +
				"  // Stack: 2, Locals: 1\n" +
				"  public X();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokespecial java.lang.Object() [19]\n" +
				"     4  aload_0 [this]\n" +
				"     5  bipush 10\n" +
				"     7  anewarray java.util.ArrayList[][][] [21]\n" +
				"    10  putfield X.test : java.util.List[][][][] [23]\n" +
				"    13  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 16]\n" +
				"        [pc: 4, line: 17]\n" +
				"        [pc: 13, line: 16]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @Size(\n" +
				"        #11 max=(int) 44 (constant type)\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 7\n" +
				"        location = [ARRAY, ARRAY, ARRAY, ARRAY]\n" +
				"      )\n" +
				"      #10 @Size(\n" +
				"        #11 max=(int) 45 (constant type)\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 7\n" +
				"      )\n" +
				"      #31 @SizeHolder(\n" +
				"        #32 value=[\n" +
				"          annotation value =\n" +
				"              #10 @Size(\n" +
				"                #11 max=(int) 47 (constant type)\n" +
				"              )\n" +
				"          annotation value =\n" +
				"              #10 @Size(\n" +
				"                #11 max=(int) 48 (constant type)\n" +
				"              )\n" +
				"          ]\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 7\n" +
				"        location = [ARRAY, ARRAY, ARRAY]\n" +
				"      )\n" +
				"  \n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void testTypeVariable() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<@Missing T> {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X<@Missing T> {\n" +
			"	                ^^^^^^^\n" +
			"Missing cannot be resolved to a type\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417660, [1.8][compiler] Incorrect parsing of Annotations with array dimensions in arguments
	public void test417660() {
		this.runConformTest(
				new String[] {
					"X.java",
					"import java.lang.annotation.Documented;\n" +
					"import java.lang.annotation.ElementType;\n" +
					"import java.lang.annotation.Retention;\n" +
					"import java.lang.annotation.RetentionPolicy;\n" +
					"import java.lang.annotation.Target;\n" +
					"public class X {\n" +
					"  int bar(int [] @TakeType(int[].class)[] x) { \n" +
					"	  return x[0][0]; \n" +
					"  } \n" +
					"  public static void main(String[] args) {\n" +
					"	System.out.println(new X().bar(new int [][] { { 1234 }}));\n" +
					"  }\n" +
					"}\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@Documented\n" +
					"@interface TakeType {\n" +
					"	Class value() default int[].class;\n" +
					"}\n"
				},
				"1234");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417660, [1.8][compiler] Incorrect parsing of Annotations with array dimensions in arguments
	public void test417660b() {
		this.runConformTest(
				new String[] {
					"X.java",
					"import java.lang.annotation.Documented;\n" +
					"import java.lang.annotation.ElementType;\n" +
					"import java.lang.annotation.Retention;\n" +
					"import java.lang.annotation.RetentionPolicy;\n" +
					"import java.lang.annotation.Target;\n" +
					"public class X {\n" +
					"  int bar(int [][] @TakeType(int[].class)[][] x @TakeType(int[].class)[]) { \n" +
					"	  return x[0][0][0][0][0]; \n" +
					"  } \n" +
					"  public static void main(String[] args) {\n" +
					"	System.out.println(new X().bar(new int [][][][][] { { { { { 1234 } } } } }));\n" +
					"  }\n" +
					"}\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@Documented\n" +
					"@interface TakeType {\n" +
					"	Class value() default int[].class;\n" +
					"}\n"
				},
				"1234");
	}

	public void testAnnotatedExtendedDimensions() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	@NonNull String @Nullable [] f @NonNull [] = null;\n" +
					"	static @NonNull String @Nullable [] foo(@NonNull String @Nullable [] p @NonNull []) @NonNull [] {\n" +
					"		p = null;\n" +
					"		@NonNull String @Nullable [] l @NonNull [] = null;\n" +
					"       return p;\n" +
					"	}\n" +
					"}\n",

					"NonNull.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface NonNull {}\n",

					"Nullable.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface Nullable {}\n",
			},
			"");
			String expectedOutput =
					"  // Field descriptor #6 [[Ljava/lang/String;\n" +
					"  java.lang.String[][] f;\n" +
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #8 @NonNull(\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, ARRAY]\n" +
					"      )\n" +
					"      #9 @Nullable(\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY]\n" +
					"      )\n" +
					"      #8 @NonNull(\n" +
					"        target type = 0x13 FIELD\n" +
					"      )\n" +
					"  \n" +
					"  // Method descriptor #11 ()V\n" +
					"  // Stack: 2, Locals: 1\n" +
					"  public X();\n" +
					"     0  aload_0 [this]\n" +
					"     1  invokespecial java.lang.Object() [13]\n" +
					"     4  aload_0 [this]\n" +
					"     5  aconst_null\n" +
					"     6  putfield X.f : java.lang.String[][] [15]\n" +
					"     9  return\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 1]\n" +
					"        [pc: 4, line: 2]\n" +
					"        [pc: 9, line: 1]\n" +
					"      Local variable table:\n" +
					"        [pc: 0, pc: 10] local: this index: 0 type: X\n" +
					"  \n" +
					"  // Method descriptor #22 ([[Ljava/lang/String;)[[Ljava/lang/String;\n" +
					"  // Stack: 1, Locals: 2\n" +
					"  static java.lang.String[][] foo(java.lang.String[][] p);\n" +
					"    0  aconst_null\n" +
					"    1  astore_0 [p]\n" +
					"    2  aconst_null\n" +
					"    3  astore_1 [l]\n" +
					"    4  aload_0 [p]\n" +
					"    5  areturn\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 4]\n" +
					"        [pc: 2, line: 5]\n" +
					"        [pc: 4, line: 6]\n" +
					"      Local variable table:\n" +
					"        [pc: 0, pc: 6] local: p index: 0 type: java.lang.String[][]\n" +
					"        [pc: 4, pc: 6] local: l index: 1 type: java.lang.String[][]\n" +
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #8 @NonNull(\n" +
					"        target type = 0x40 LOCAL_VARIABLE\n" +
					"        local variable entries:\n" +
					"          [pc: 4, pc: 6] index: 1\n" +
					"        location = [ARRAY, ARRAY]\n" +
					"      )\n" +
					"      #9 @Nullable(\n" +
					"        target type = 0x40 LOCAL_VARIABLE\n" +
					"        local variable entries:\n" +
					"          [pc: 4, pc: 6] index: 1\n" +
					"        location = [ARRAY]\n" +
					"      )\n" +
					"      #8 @NonNull(\n" +
					"        target type = 0x40 LOCAL_VARIABLE\n" +
					"        local variable entries:\n" +
					"          [pc: 4, pc: 6] index: 1\n" +
					"      )\n" +
					"    RuntimeVisibleTypeAnnotations: \n" +
					"      #8 @NonNull(\n" +
					"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
					"        method parameter index = 0\n" +
					"        location = [ARRAY, ARRAY]\n" +
					"      )\n" +
					"      #9 @Nullable(\n" +
					"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
					"        method parameter index = 0\n" +
					"        location = [ARRAY]\n" +
					"      )\n" +
					"      #8 @NonNull(\n" +
					"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
					"        method parameter index = 0\n" +
					"      )\n" +
					"      #8 @NonNull(\n" +
					"        target type = 0x14 METHOD_RETURN\n" +
					"        location = [ARRAY, ARRAY]\n" +
					"      )\n" +
					"      #9 @Nullable(\n" +
					"        target type = 0x14 METHOD_RETURN\n" +
					"        location = [ARRAY]\n" +
					"      )\n" +
					"      #8 @NonNull(\n" +
					"        target type = 0x14 METHOD_RETURN\n" +
					"      )\n" +
					"}";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418347,  [1.8][compiler] Type annotations dropped during code generation.
	public void testPQTRArray() throws Exception {
		this.runConformTest(
				new String[] {
						"Outer.java",
						"public class Outer<K>  {\n" +
						"	class Inner<P> {\n" +
						"	}\n" +
						"	public @T(1) Outer<@T(2) String>.@T(3) Inner<@T(4) Integer> @T(5) [] omi @T(6) [];\n" +
						"}\n" +
						"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
						"@interface T {\n" +
						"	int value();\n" +
						"}\n",
			},
			"");
			String expectedOutput =
					"  public Outer$Inner[][] omi;\n" +
					"    RuntimeInvisibleTypeAnnotations: \n" +
					"      #10 @T(\n" +
					"        #11 value=(int) 1 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, ARRAY]\n" +
					"      )\n" +
					"      #10 @T(\n" +
					"        #11 value=(int) 3 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, ARRAY, INNER_TYPE]\n" +
					"      )\n" +
					"      #10 @T(\n" +
					"        #11 value=(int) 5 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY]\n" +
					"      )\n" +
					"      #10 @T(\n" +
					"        #11 value=(int) 6 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"      )\n" +
					"      #10 @T(\n" +
					"        #11 value=(int) 2 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, ARRAY, TYPE_ARGUMENT(0)]\n" +
					"      )\n" +
					"      #10 @T(\n" +
					"        #11 value=(int) 4 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, ARRAY, INNER_TYPE, TYPE_ARGUMENT(0)]\n" +
					"      )\n" +
					"  \n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "Outer.class", "Outer", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418347,  [1.8][compiler] Type annotations dropped during code generation.
	public void testPQTRArray2() throws Exception {
		this.runConformTest(
				new String[] {
						"Outer.java",
						"public class Outer<K1, K2>  {\n" +
						"	class Inner<P1, P2> {\n" +
						"	}\n" +
						"	public @T(1) Outer<@T(2) String, @T(3) Inner>.@T(4) Inner<@T(5) Integer, @T(6) Outer.@T(7) Inner> @T(7) [] omi @T(8) [];\n" +
						"}\n" +
						"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
						"@interface T {\n" +
						"	int value();\n" +
						"}\n",
			},
			"");
			String expectedOutput =
					"  // Field descriptor #6 [[LOuter$Inner;\n" +
					"  // Signature: [[LOuter<Ljava/lang/String;LOuter$Inner;>.Inner<Ljava/lang/Integer;LOuter$Inner;>;\n" +
					"  public Outer$Inner[][] omi;\n" +
					"    RuntimeInvisibleTypeAnnotations: \n" +
					"      #10 @T(\n" +
					"        #11 value=(int) 1 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, ARRAY]\n" +
					"      )\n" +
					"      #10 @T(\n" +
					"        #11 value=(int) 4 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, ARRAY, INNER_TYPE]\n" +
					"      )\n" +
					"      #10 @T(\n" +
					"        #11 value=(int) 7 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY]\n" +
					"      )\n" +
					"      #10 @T(\n" +
					"        #11 value=(int) 8 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"      )\n" +
					"      #10 @T(\n" +
					"        #11 value=(int) 2 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, ARRAY, TYPE_ARGUMENT(0)]\n" +
					"      )\n" +
					"      #10 @T(\n" +
					"        #11 value=(int) 3 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, ARRAY, TYPE_ARGUMENT(1), INNER_TYPE]\n" +
					"      )\n" +
					"      #10 @T(\n" +
					"        #11 value=(int) 5 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, ARRAY, INNER_TYPE, TYPE_ARGUMENT(0)]\n" +
					"      )\n" +
					"      #10 @T(\n" +
					"        #11 value=(int) 6 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, ARRAY, INNER_TYPE, TYPE_ARGUMENT(1)]\n" +
					"      )\n" +
					"      #10 @T(\n" +
					"        #11 value=(int) 7 (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [ARRAY, ARRAY, INNER_TYPE, TYPE_ARGUMENT(1), INNER_TYPE]\n" +
					"      )\n" +
					"  \n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "Outer.class", "Outer", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418347,  [1.8][compiler] Type annotations dropped during code generation.
	public void testConstructorResult() throws Exception {
		this.runConformTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.ElementType;\n" +
						"import java.lang.annotation.Target;\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface T {\n" +
						"}\n" +
						"public class X {\n" +
						"	@T X() {}\n" +
						"	class Y {\n" +
						"	 @T Y () {\n" +
						"	}\n" +
						"	}\n" +
						"}\n",
			},
			"");
			String expectedOutput =
					"  // Method descriptor #6 ()V\n" +
					"  // Stack: 1, Locals: 1\n" +
					"  X();\n" +
					"    0  aload_0 [this]\n" +
					"    1  invokespecial java.lang.Object() [8]\n" +
					"    4  return\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 7]\n" +
					"      Local variable table:\n" +
					"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
					"    RuntimeInvisibleTypeAnnotations: \n" +
					"      #15 @T(\n" +
					"        target type = 0x14 METHOD_RETURN\n" +
					"      )\n" +
					"\n";
			String expectedOutForY =
					"  // Method descriptor #8 (LX;)V\n" +
					"  // Stack: 2, Locals: 2\n" +
					"  X$Y(X arg0);\n" +
					"     0  aload_0 [this]\n" +
					"     1  aload_1 [arg0]\n" +
					"     2  putfield X$Y.this$0 : X [10]\n" +
					"     5  aload_0 [this]\n" +
					"     6  invokespecial java.lang.Object() [12]\n" +
					"     9  return\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 9]\n" +
					"        [pc: 9, line: 10]\n" +
					"      Local variable table:\n" +
					"        [pc: 0, pc: 10] local: this index: 0 type: X.Y\n" +
					"    RuntimeInvisibleTypeAnnotations: \n" +
					"      #20 @T(\n" +
					"        target type = 0x14 METHOD_RETURN\n" +
					"        location = [INNER_TYPE]\n" +
					"      )\n" +
					"\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X$Y.class", "Y", expectedOutForY, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418347,  [1.8][compiler] Type annotations dropped during code generation.
	public void test418347() throws Exception {
		this.runConformTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.*;\n" +
						"import static java.lang.annotation.ElementType.*;\n" +
						"@Target({TYPE_USE}) @interface P { }\n" +
						"@Target({TYPE_USE}) @interface O { }\n" +
						"@Target({TYPE_USE}) @interface I { }\n" +
						"public abstract class X<T> {\n" +
						"	class Y<Q> {\n" +
						"	}\n" +
						"	void foo(@P Y<P> p) {}\n" +
						"}\n",
			},
			"");
			String expectedOutput =
					"    RuntimeInvisibleTypeAnnotations: \n" +
					"      #24 @P(\n" +
					"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
					"        method parameter index = 0\n" +
					"        location = [INNER_TYPE]\n" +
					"      )\n" +
					"\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418347,  [1.8][compiler] Type annotations dropped during code generation.
	public void test418347a() throws Exception {
		this.runConformTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.*;\n" +
						"import static java.lang.annotation.ElementType.*;\n" +
						"@Target({TYPE_USE}) @interface P { }\n" +
						"@Target({TYPE_USE}) @interface O { }\n" +
						"@Target({TYPE_USE}) @interface I { }\n" +
						"public abstract class X {\n" +
						"	class Y {\n" +
						"		class Z {}\n" +
						"	}\n" +
						"	void foo(@P X.@O Y.@I Z[] p) {}\n" +
						"}\n",
			},
			"");
			String expectedOutput =
					"    RuntimeInvisibleTypeAnnotations: \n" +
					"      #19 @P(\n" +
					"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
					"        method parameter index = 0\n" +
					"        location = [ARRAY]\n" +
					"      )\n" +
					"      #20 @O(\n" +
					"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
					"        method parameter index = 0\n" +
					"        location = [ARRAY, INNER_TYPE]\n" +
					"      )\n" +
					"      #21 @I(\n" +
					"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
					"        method parameter index = 0\n" +
					"        location = [ARRAY, INNER_TYPE, INNER_TYPE]\n" +
					"      )\n" +
					"\n";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418347,  [1.8][compiler] Type annotations dropped during code generation.
	public void test418347b() throws Exception {
		this.runConformTest(
			new String[] {
					"X.java",
					"public abstract class X {\n" +
					"	java.util.List [][] l = new java.util.ArrayList @pkg.NonNull [0] @pkg.NonNull[];     \n" +
					"}\n",
					"pkg/NonNull.java",
					"package pkg;\n" +
					"import java.lang.annotation.ElementType;\n" +
					"import java.lang.annotation.Target;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"public @interface NonNull {\n" +
					"}\n"
			},
			"");
			String expectedOutput =
					"    RuntimeInvisibleTypeAnnotations: \n" +
					"      #21 @pkg.NonNull(\n" +
					"        target type = 0x44 NEW\n" +
					"        offset = 6\n" +
					"      )\n" +
					"      #21 @pkg.NonNull(\n" +
					"        target type = 0x44 NEW\n" +
					"        offset = 6\n" +
					"        location = [ARRAY]\n" +
					"      )\n" +
					"}";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=419331, [1.8][compiler] Weird error on forward reference to type annotations from type parameter declarations
	public void testForwardReference() {
		this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.JavacHasWarningsEclipseNotConfigured,
			new String[] {
				"T.java",
				"import java.lang.annotation.Annotation;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"@R(TC.class)\n" +
				"@Target(ElementType.TYPE_PARAMETER)\n" +
				"@interface T {\n" +
				"}\n" +
				"\n" +
				"interface I<@T K> {\n" +
				"}\n" +
				"\n" +
				"@Deprecated\n" +
				"@interface TC {\n" +
				"\n" +
				"}\n" +
				"\n" +
				"@Target(ElementType.ANNOTATION_TYPE)\n" +
				"@interface R {\n" +
				"    Class<? extends Annotation> value();\n" +
				"}\n",
			},
			"");
	}
	public void testHybridTargets() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target({ElementType.TYPE_USE, ElementType.PACKAGE})\n" +
				"@interface T {\n" +
				"}\n" +
				"@T\n" +
				"public class X {\n" +
				"    @T\n" +
				"    X() {}\n" +
				"    @T String x;\n" +
				"    @T \n" +
				"	int foo(@T int p) { \n" +
				"      @T int l;\n" +
				"	   return 0;\n" +
				"   }\n" +
				"}\n",
			},
			"");
		String expectedOutput =
				"  // Field descriptor #6 Ljava/lang/String;\n" +
				"  java.lang.String x;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @T(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #10 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  X();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [12]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 9]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @T(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #19 (I)I\n" +
				"  // Stack: 1, Locals: 2\n" +
				"  int foo(int p);\n" +
				"    0  iconst_0\n" +
				"    1  ireturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 14]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 2] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 2] local: p index: 1 type: int\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @T(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n" +
				"      #8 @T(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n" +
				"\n" +
				"  RuntimeInvisibleAnnotations: \n" +
				"    #8 @T(\n" +
				"    )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void testHybridTargets2() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"@Target({ ElementType.TYPE_USE, ElementType.METHOD })\n" +
				"@interface SillyAnnotation {  }\n" +
				"public class X {\n" +
				"   @SillyAnnotation\n" +
				"   X(@SillyAnnotation int x) {\n" +
				"   }\n" +
				"	@SillyAnnotation\n" +
				"	void foo(@SillyAnnotation int x) {\n" +
				"	}\n" +
				"	@SillyAnnotation\n" +
				"	String goo(@SillyAnnotation int x) {\n" +
				"		return null;\n" +
				"	}\n" +
				"	@SillyAnnotation\n" +
				"	X field;\n" +
				"}\n"
			},
			"");
		String expectedOutput =
				"  // Field descriptor #6 LX;\n" +
				"  X field;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @SillyAnnotation(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #10 (I)V\n" +
				"  // Stack: 1, Locals: 2\n" +
				"  X(int x);\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [12]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 7]\n" +
				"        [pc: 4, line: 8]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 5] local: x index: 1 type: int\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @SillyAnnotation(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n" +
				"      #8 @SillyAnnotation(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #10 (I)V\n" +
				"  // Stack: 0, Locals: 2\n" +
				"  void foo(int x);\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 11]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 1] local: x index: 1 type: int\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @SillyAnnotation(\n" +
				"      )\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @SillyAnnotation(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #23 (I)Ljava/lang/String;\n" +
				"  // Stack: 1, Locals: 2\n" +
				"  java.lang.String goo(int x);\n" +
				"    0  aconst_null\n" +
				"    1  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 14]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 2] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 2] local: x index: 1 type: int\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @SillyAnnotation(\n" +
				"      )\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @SillyAnnotation(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n" +
				"      #8 @SillyAnnotation(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void testDeprecated() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Deprecated\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface X {\n" +
				"	int value() default 0;\n" +
				"}\n"
			},
			"");
		String expectedOutput =
				"// Compiled from X.java (version 1.8 : 52.0, no super bit, deprecated)\n" +
				"abstract @interface X extends java.lang.annotation.Annotation {\n" +
				"  Constant pool:\n" +
				"    constant #1 class: #2 X\n" +
				"    constant #2 utf8: \"X\"\n" +
				"    constant #3 class: #4 java/lang/Object\n" +
				"    constant #4 utf8: \"java/lang/Object\"\n" +
				"    constant #5 class: #6 java/lang/annotation/Annotation\n" +
				"    constant #6 utf8: \"java/lang/annotation/Annotation\"\n" +
				"    constant #7 utf8: \"value\"\n" +
				"    constant #8 utf8: \"()I\"\n" +
				"    constant #9 utf8: \"AnnotationDefault\"\n" +
				"    constant #10 integer: 0\n" +
				"    constant #11 utf8: \"SourceFile\"\n" +
				"    constant #12 utf8: \"X.java\"\n" +
				"    constant #13 utf8: \"Deprecated\"\n" +
				"    constant #14 utf8: \"RuntimeVisibleAnnotations\"\n" +
				"    constant #15 utf8: \"Ljava/lang/Deprecated;\"\n" +
				"    constant #16 utf8: \"Ljava/lang/annotation/Target;\"\n" +
				"    constant #17 utf8: \"Ljava/lang/annotation/ElementType;\"\n" +
				"    constant #18 utf8: \"TYPE_USE\"\n" +
				"  \n" +
				"  // Method descriptor #8 ()I\n" +
				"  public abstract int value();\n" +
				"    Annotation Default: \n" +
				"      (int) 0 (constant type)\n" +
				"\n" +
				"  RuntimeVisibleAnnotations: \n" +
				"    #15 @java.lang.Deprecated(\n" +
				"    )\n" +
				"    #16 @java.lang.annotation.Target(\n" +
				"      #7 value=[\n" +
				"        java.lang.annotation.ElementType.TYPE_USE(enum type #17.#18)\n" +
				"        ]\n" +
				"    )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421148, [1.8][compiler] Verify error with annotated casts and unused locals.
	public void test421148() {

		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target(ElementType.TYPE_USE) @interface T {}\n" +
				"public class X {\n" +
				"	public static void main(String argv[]) {\n" +
				"		Object o = (@T Object) new Object();    \n" +
				"       System.out.println(\"OK\");\n" +
				"	}\n" +
				"}\n"
			},
			"OK",
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421620,  [1.8][compiler] wrong compile error with TYPE_USE annotation on exception
	public void test421620() {

		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Documented;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"class E1 extends Exception {\n" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n" +
				"\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@Documented\n" +
				"@interface NonCritical { }\n" +
				"public class X {\n" +
				"    @NonCritical E1 e1; // looks like this field's type binding is reused\n" +
				"//wrong error:\n" +
				"//Cannot use the parameterized type E1 either in catch block or throws clause\n" +
				"    void f1 (int a) throws /*@NonCritical*/ E1 {\n" +
				"        throw new E1();\n" +
				"    }\n" +
				"    void foo() {\n" +
				"        try {\n" +
				"            f1(0);\n" +
				"//wrong error: Unreachable catch block for E1.\n" +
				"//             This exception is never thrown from the try statement body\n" +
				"        } catch (@NonCritical final RuntimeException | @NonCritical E1 ex) {\n" +
				"            System.out.println(ex);\n" +
				"        }\n" +
				"    }\n" +
				"    public static void main(String[] args) {\n" +
				"		System.out.println(\"OK\");\n" +
				"	}\n" +
				"}\n"
			},
			"OK",
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425599, [1.8][compiler] ISE when trying to compile qualified and annotated class instance creation
	public void _test425599() {

		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"public class X {\n" +
				"    Object ax = new @A Outer().new Middle<String>();\n" +
				"    public static void main(String args[]) {\n" +
				"        System.out.println(\"OK\");\n" +
				"    }\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE) @interface A {}\n" +
				"class Outer {\n" +
				"    class Middle<E> {\n" +
				"    	class Inner<I> {}\n" +
				"    	@A Middle<Object>.@A Inner<Character> ax = new pack.@A Outer().new @A Middle<@A Object>().new @A Inner<@A Character>(null);\n" +
				"    }\n" +
				"}\n"
			},
			"OK",
			customOptions);
	}
	public void testBug485386() {
		String javaVersion = System.getProperty("java.version");
		int index = javaVersion.indexOf('.');
		if (index != -1) {
			javaVersion = javaVersion.substring(0, index);
		} else {
			index = javaVersion.indexOf('-');
			if (index != -1)
				javaVersion = javaVersion.substring(0, index);
		}
		int v = Integer.parseInt(javaVersion);
		runConformTest(
			new String[] {
				"Test.java",
				"import java.lang.annotation.*;\n" +
				"import java.lang.reflect.*;\n" +
				"\n" +
				"@Retention(value = RetentionPolicy.RUNTIME)\n" +
				"@java.lang.annotation.Target(ElementType.TYPE_USE)\n" +
				"@interface TestAnn1 {\n" +
				"  String value() default \"1\";\n" +
				"}\n" +
				"\n" +
				"public class Test {\n" +
				"\n" +
				"  class Inner {\n" +
				"    public @TestAnn1() Inner() {\n" +
				"      System.out.println(\"New\");\n" +
				"    }\n" +
				"  }\n" +
				"\n" +
				"  public void test() throws SecurityException, NoSuchMethodException {\n" +
				"    Executable f = Test.Inner.class.getDeclaredConstructor(Test.class);\n" +
				"    AnnotatedType ae = f.getAnnotatedReturnType();\n" +
				"    Object o = ae.getAnnotation(TestAnn1.class);\n" +
				"    System.out.println(o);\n" +
				"  }\n" +
				"  \n" +
				"  public static void main(String... args) throws Exception {\n" +
				"    new Test().test();\n" +
				"  }\n" +
				"}\n"
			},
			"@TestAnn1(" + (v < 14 ? "value=" : "") + decorateAnnotationValueLiteral("1") + ")");
	}
	public void testBug492322readFromClass() {
		runConformTest(
			new String[] {
				"test1/Base.java",
				"package test1;\n" +
				"\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"@Target(ElementType.TYPE_USE) @interface A2 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A3 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A4 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A5 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A6 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A7 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A8 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface B1 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface B2 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface B3 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface C1 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface C2 {}\n" +
				"\n" +
				"public abstract class Base {\n" +
				"  static public class Static {\n" +
				"   public class Middle1 {\n" +
				"     public class Middle2<M> {\n" +
				"       public class Middle3 {\n" +
				"        public class GenericInner<T> {\n" +
				"        }\n" +
				"       }\n" +
				"     }\n" +
				"   }\n" +
				"  }\n" +
				"\n" +
				"  public Object method1(Base.@A2 Static.@A3 Middle1.@A4 Middle2<Object>.@A5 Middle3.@A6 GenericInner<String> nullable) {\n" +
				"    return new Object();\n" +
				"  }\n" +
				"  public Object method2(Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Object>.@A5 Middle3.@A6 GenericInner<@B2 String> @A7 [] @A8 [] nullable) {\n" +
				"    return new Object();\n" +
				"  }\n" +
				"  public Object method3(Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Class<@C1 Object @C2 []> @B2 []>.@A5 Middle3.@A6 GenericInner<@B3 String> @A7 [] @A8 [] nullable) {\n" +
				"    return new Object();\n" +
				"  }\n" +
				"}\n" +
				"",
			}
		);

		// get compiled type via binarytypebinding
		Requestor requestor = new Requestor(false, null /*no custom requestor*/, false, /* show category */ false /* show warning token*/);
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		Compiler compiler = new Compiler(getNameEnvironment(new String[0], null), getErrorHandlingPolicy(),
				new CompilerOptions(customOptions), requestor, getProblemFactory());
		char [][] compoundName = new char [][] { "test1".toCharArray(), "Base".toCharArray()};
		ReferenceBinding type = compiler.lookupEnvironment.askForType(compoundName, compiler.lookupEnvironment.UnNamedModule);
		assertNotNull(type);
		MethodBinding[] methods1 = type.getMethods("method1".toCharArray());
		assertEquals("Base.@A2 Static.@A3 Middle1.@A4 Middle2<Object>.@A5 Middle3.@A6 GenericInner<String>",
				new String(methods1[0].parameters[0].annotatedDebugName()));

		MethodBinding[] methods2 = type.getMethods("method2".toCharArray());
		assertEquals("Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Object>.@A5 Middle3.@A6 GenericInner<@B2 String> @A7 [] @A8 []",
				new String(methods2[0].parameters[0].annotatedDebugName()));

		MethodBinding[] methods3 = type.getMethods("method3".toCharArray());
		assertEquals("Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Class<@C1 Object @C2 []> @B2 []>.@A5 Middle3.@A6 GenericInner<@B3 String> @A7 [] @A8 []",
				new String(methods3[0].parameters[0].annotatedDebugName()));
	}

	public void testBug492322readFromClassWithGenericBase() {
		runConformTest(
			new String[] {
				"test1/Base.java",
				"package test1;\n" +
				"\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"@Target(ElementType.TYPE_USE) @interface A2 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A3 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A4 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A5 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A6 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A7 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A8 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface B1 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface B2 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface B3 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface C1 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface C2 {}\n" +
				"\n" +
				"public abstract class Base<B> {\n" +
				"  static public class Static {\n" +
				"   public class Middle1 {\n" +
				"     public class Middle2<M> {\n" +
				"       public class Middle3 {\n" +
				"        public class GenericInner<T> {\n" +
				"        }\n" +
				"       }\n" +
				"     }\n" +
				"   }\n" +
				"  }\n" +
				"\n" +
				"  public Object method1(Base.@A2 Static.@A3 Middle1.@A4 Middle2<Object>.@A5 Middle3.@A6 GenericInner<String> nullable) {\n" +
				"    return new Object();\n" +
				"  }\n" +
				"  public Object method2(Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Object>.@A5 Middle3.@A6 GenericInner<@B2 String> @A7 [] @A8 [] nullable) {\n" +
				"    return new Object();\n" +
				"  }\n" +
				"  public Object method3(Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Class<@C1 Object @C2 []> @B2 []>.@A5 Middle3.@A6 GenericInner<@B3 String> @A7 [] @A8 [] nullable) {\n" +
				"    return new Object();\n" +
				"  }\n" +
				"}\n" +
				"",
			}
		);

		// get compiled type via binarytypebinding
		Requestor requestor = new Requestor(false, null /*no custom requestor*/, false, /* show category */ false /* show warning token*/);
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		Compiler compiler = new Compiler(getNameEnvironment(new String[0], null), getErrorHandlingPolicy(),
				new CompilerOptions(customOptions), requestor, getProblemFactory());
		char [][] compoundName = new char [][] { "test1".toCharArray(), "Base".toCharArray()};
		ReferenceBinding type = compiler.lookupEnvironment.askForType(compoundName, compiler.lookupEnvironment.UnNamedModule);
		assertNotNull(type);
		MethodBinding[] methods1 = type.getMethods("method1".toCharArray());
		assertEquals("Base.@A2 Static.@A3 Middle1.@A4 Middle2<Object>.@A5 Middle3.@A6 GenericInner<String>",
				new String(methods1[0].parameters[0].annotatedDebugName()));

		MethodBinding[] methods2 = type.getMethods("method2".toCharArray());
		assertEquals("Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Object>.@A5 Middle3.@A6 GenericInner<@B2 String> @A7 [] @A8 []",
				new String(methods2[0].parameters[0].annotatedDebugName()));

		MethodBinding[] methods3 = type.getMethods("method3".toCharArray());
		assertEquals("Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Class<@C1 Object @C2 []> @B2 []>.@A5 Middle3.@A6 GenericInner<@B3 String> @A7 [] @A8 []",
				new String(methods3[0].parameters[0].annotatedDebugName()));
	}
	public void testBug492322WithOldBinary() {
			// bug492322-compiled-with-4.6.jar contains classes compiled with eclipse 4.6:
			/*-
				package test1;

				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;

				@Target(ElementType.TYPE_USE) @interface A2 {}
				@Target(ElementType.TYPE_USE) @interface A3 {}
				@Target(ElementType.TYPE_USE) @interface A4 {}
				@Target(ElementType.TYPE_USE) @interface A5 {}
				@Target(ElementType.TYPE_USE) @interface A6 {}
				@Target(ElementType.TYPE_USE) @interface A7 {}
				@Target(ElementType.TYPE_USE) @interface A8 {}
				@Target(ElementType.TYPE_USE) @interface B1 {}
				@Target(ElementType.TYPE_USE) @interface B2 {}
				@Target(ElementType.TYPE_USE) @interface B3 {}
				@Target(ElementType.TYPE_USE) @interface B4 {}
				@Target(ElementType.TYPE_USE) @interface C1 {}
				@Target(ElementType.TYPE_USE) @interface C2 {}

				public abstract class Base<B> {
				  static public class Static {
				    public static class Static2<X> {
				      public class Middle1 {
				        public class Middle2<M> {
				          public class Middle3 {
				            public class GenericInner<T> {
				            }
				          }
				        }
				      }
				    }
				  }

				  public Object method1(Static.@A2 Static2<Exception>.@A3 Middle1.@A4 Middle2<Object>.@A5 Middle3.@A6 GenericInner<String> nullable) {
					  return new Object();
				  }
				  public Object method2(Static.@A2 Static2<@B1 Exception>.@A3 Middle1.@A4 Middle2<@B2 Object>.@A5 Middle3.@A6 GenericInner<@B3 String> @A7 [] @A8 [] nullable) {
				    return new Object();
				  }
				  public Object method3(Static.@A2 Static2<@B1 Exception>.@A3 Middle1.@A4 Middle2<@B2 Class<@C1 Object @C2 []> @B3 []>.@A5 Middle3.@A6 GenericInner<@B4 String> @A7 [] @A8 [] nullable) {
				    return new Object();
				  }
				}
			 */
			// get compiled type via binarytypebinding
			Requestor requestor = new Requestor(false, null /*no custom requestor*/, false, /* show category */ false /* show warning token*/);
			Map customOptions = getCompilerOptions();
			customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			String[] defaultClassPaths = getDefaultClassPaths();
			String jarpath = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "bug492322-compiled-with-4.6.jar";
			String[] paths = new String[defaultClassPaths.length + 1];
			System.arraycopy(defaultClassPaths, 0, paths, 0, defaultClassPaths.length);
			paths[defaultClassPaths.length] = jarpath;
			Compiler compiler = new Compiler(getNameEnvironment(new String[0], paths), getErrorHandlingPolicy(),
					new CompilerOptions(customOptions), requestor, getProblemFactory());
			char [][] compoundName = new char [][] { "test1".toCharArray(), "Base".toCharArray()};
			ReferenceBinding type = compiler.lookupEnvironment.askForType(compoundName, compiler.lookupEnvironment.UnNamedModule);
			assertNotNull(type);
			MethodBinding[] methods1 = type.getMethods("method1".toCharArray());
			assertEquals("Base.Static.@A2 Static2<Exception>.@A3 Middle1.@A4 Middle2<Object>.@A5 Middle3.@A6 GenericInner<String>",
					new String(methods1[0].parameters[0].annotatedDebugName()));

			MethodBinding[] methods2 = type.getMethods("method2".toCharArray());
			assertEquals("Base.Static.@A2 Static2<@B1 Exception>.@A3 Middle1.@A4 Middle2<@B2 Object>.@A5 Middle3.@A6 GenericInner<@B3 String> @A7 [] @A8 []",
					new String(methods2[0].parameters[0].annotatedDebugName()));

			MethodBinding[] methods3 = type.getMethods("method3".toCharArray());
			assertEquals("Base.Static.@A2 Static2<@B1 Exception>.@A3 Middle1.@A4 Middle2<@B2 Class<@C1 Object @C2 []> @B3 []>.@A5 Middle3.@A6 GenericInner<@B4 String> @A7 [] @A8 []",
					new String(methods3[0].parameters[0].annotatedDebugName()));
	}

	public void testBug594561_ParameterizedTypeAnnotations() {
		runConformTest(new String[] {
			"p/C.java",
			"package p;" +
			"@Deprecated\n" +
			"abstract class A<T> {}\n" +
			"class C extends A<String> {}\n",
		});

		Requestor requestor = new Requestor(false, null, false, false);
		Map<String, String> customOptions = getCompilerOptions(); customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		Compiler compiler = new Compiler(getNameEnvironment(new String[0], null), getErrorHandlingPolicy(), new CompilerOptions(customOptions), requestor, getProblemFactory());

		ReferenceBinding type = compiler.lookupEnvironment.askForType(new char[][] {"p".toCharArray(), "C".toCharArray()}, compiler.lookupEnvironment.UnNamedModule);
		assertNotNull(type);

		AnnotationBinding[] annos = type.superclass().getAnnotations();
		assertEquals(1, annos.length);
		assertEquals("java.lang.Deprecated", annos[0].getAnnotationType().debugName());
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1096
	// ECJ out of sync with JLS 9.6.4.1
	public void testGH1096() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"import java.lang.annotation.*;\n" +
					"@interface MTPA {}\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n"+
					"@interface CTPA {}\n" +
					"public class X<@CTPA K, T> {\n" +
					"    <U, @MTPA V> void m(U arg1) {}\n" +
					"}\n",
				},
				"");
		String expectedOutput =
				"RuntimeInvisibleTypeAnnotations: \n" +
				"      #24 @MTPA(\n" +
				"        target type = 0x1 METHOD_TYPE_PARAMETER\n" +
				"        type parameter index = 1\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);

		expectedOutput =
				"  RuntimeVisibleTypeAnnotations: \n" +
				"    #29 @CTPA(\n" +
				"      target type = 0x0 CLASS_TYPE_PARAMETER\n" +
				"      type parameter index = 0\n" +
				"    )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=568240
	// Method's annotation attribute is compiled as annotation on return type
	public void testBug568240() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.lang.reflect.AnnotatedType;\n" +
				"import java.util.Arrays;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"    public void test() {\n" +
				"\n" +
				"        AnnotatedType annotatedReturnType = Foo.class.getMethods()[0].getAnnotatedReturnType();\n" +
				"\n" +
				"        // @Child is an attribute of the @Ann annotation. Not a TYPE_USE annotation on the return type.\n" +
				"        if (!Arrays.asList(annotatedReturnType.getAnnotations()).isEmpty()) {\n" +
				"        	throw new Error(\"Broken\");\n" +
				"        }\n" +
				"\n" +
				"    }\n" +
				"    \n" +
				"    public static void main(String[] args) {\n" +
				"		new X().test();\n" +
				"	}\n" +
				"\n" +
				"	public static interface Foo {\n" +
				"\n" +
				"        @Ann(value = @Ann.Child(value = \"foo\"))\n" +
				"        String get();\n" +
				"\n" +
				"    }\n" +
				"\n" +
				"    @Target(ElementType.METHOD)\n" +
				"    @Retention(RetentionPolicy.RUNTIME)\n" +
				"    public static @interface Ann {\n" +
				"\n" +
				"        Child value();\n" +
				"\n" +
				"        @Retention(RetentionPolicy.RUNTIME)\n" +
				"        public static @interface Child {\n" +
				"            String value();\n" +
				"        }\n" +
				"    }\n" +
				"}\n",
			},
			"");
		}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=566803
	// field.getAnnotatedType().getAnnotations() broken in the latest ECJ version
	public void testBug566803() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.lang.reflect.AnnotatedType;\n" +
				"import java.lang.reflect.Field;\n" +
				"\n" +
				"public class X  {\n" +
				"\n" +
				"  @TestAnn1(\"1-1\")\n" +
				"  @TestAnn1(\"1-2\")\n" +
				"  public static final @TestAnnFirst long aaa = 1;\n" +
				"\n" +
				"  public void broken() {\n" +
				"	  throw new Error(\"Broken\");\n" +
				"  }\n" +
				"  \n" +
				"  public static void main(String[] args) throws NoSuchFieldException, SecurityException {\n" +
				"	new X().test();\n" +
				"}\n" +
				"  public void test() throws NoSuchFieldException, SecurityException {\n" +
				"    Field f = X.class.getDeclaredField(\"aaa\");\n" +
				"    AnnotatedType s = f.getAnnotatedType();\n" +
				"\n" +
				"    if (long.class != s.getType()) {\n" +
				"    	broken();\n" +
				"    }\n" +
				"\n" +
				"    Annotation[] as = s.getAnnotations();\n" +
				"    for (int i = 0; i < as.length; i++) {\n" +
				"      System.out.println(i + \" @\" + as[i].annotationType().getCanonicalName() + \"()\");\n" +
				"    }\n" +
				"\n" +
				"    if (1 != as.length) {\n" +
				"    	broken();\n" +
				"    }\n" +
				"    as = s.getAnnotationsByType(TestAnnFirst.class);\n" +
				"    if (1 != as.length) {\n" +
				"    	broken();\n" +
				"    }\n" +
				"  }\n" +
				"\n" +
				"  @Retention(RetentionPolicy.RUNTIME)\n" +
				"  @java.lang.annotation.Target(ElementType.TYPE_USE)\n" +
				"  public @interface TestAnnFirst {\n" +
				"  }\n" +
				"\n" +
				"  @Retention(value = RetentionPolicy.RUNTIME)\n" +
				"  @Inherited\n" +
				"  @Repeatable(TestAnn1s.class)\n" +
				"  public @interface TestAnn1 {\n" +
				"    String value() default \"1\";\n" +
				"  }\n" +
				"\n" +
				"  @Retention(value = RetentionPolicy.RUNTIME)\n" +
				"  @Inherited\n" +
				"  public @interface TestAnn1s {\n" +
				"    TestAnn1[] value();\n" +
				"  }\n" +
				"}\n",
			},
			"0 @X.TestAnnFirst()");
		}
}
