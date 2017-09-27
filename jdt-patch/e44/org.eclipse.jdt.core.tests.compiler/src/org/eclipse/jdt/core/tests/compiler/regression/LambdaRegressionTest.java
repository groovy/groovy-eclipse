/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
public class LambdaRegressionTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test001"};
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public LambdaRegressionTest(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes 
public void test001() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.HashMap;\n" +
				"import java.util.Map;\n" +
				"import java.util.function.Function;\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    new X().run();\n" +
				"  }\n" +
				"  public void run() {\n" +
				"    class Inner {\n" +
				"      public Inner() {\n" +
				"        System.out.println(\"miep\");\n" +
				"      }\n" +
				"    }\n" +
				"    Map<String, Inner> map = new HashMap<>();\n" +
				"    Function<String, Inner> function = (name) -> {\n" +
				"      Inner i = map.get(name);\n" +
				"      if (i == null) {\n" +
				"        i = new Inner();\n" +
				"        map.put(name, i);\n" +
				"      }\n" +
				"      return i;\n" +
				"\n" +
				"    };\n" +
				"    function.apply(\"test\");\n" +
				"  }\n" +
				"}\n",
			},
			"miep"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes 
public void test002() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.function.Consumer;\n" +
				"@SuppressWarnings(\"all\")\n" +
				"public class X {\n" +
				"  private final String text = \"Bug?\";\n" +
				"  public static void main(String[] args) {\n" +
				"    new X().doIt();\n" +
				"  }\n" +
				"  private void doIt() {\n" +
				"    new Sub();\n" +
				"  }\n" +
				"  private class Super<T> {\n" +
				"    public Super(Consumer<T> consumer) {\n" +
				"    }\n" +
				"  }\n" +
				"  private class Sub extends Super<String> {\n" +
				"    public Sub() {\n" +
				"      super(s -> System.out.println(text));\n" +
				"      // super(s -> System.out.println(\"miep\"));\n" +
				"    }\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 17)\n" + 
			"	super(s -> System.out.println(text));\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot refer to \'this\' nor \'super\' while explicitly invoking a constructor\n" + 
			"----------\n"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes 
public void test003() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.function.Consumer;\n" +
				"@SuppressWarnings(\"all\")\n" +
				"public class X {\n" +
				"  private final String text = \"Bug?\";\n" +
				"  public static void main(String[] args) {\n" +
				"    new X().doIt();\n" +
				"  }\n" +
				"  private void doIt() {\n" +
				"    new Sub();\n" +
				"  }\n" +
				"  private class Super<T> {\n" +
				"    public Super(Consumer<T> consumer) {\n" +
				"    }\n" +
				"  }\n" +
				"  private class Sub extends Super<String> {\n" +
				"    public Sub() {\n" +
				"       super(s -> System.out.println(\"miep\"));\n" +
				"    }\n" +
				"  }\n" +
				"}\n",
			},
			""
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes
public void test004() {
	this.runConformTest(
		new String[] {
			"Y.java", 
			"import java.util.function.Supplier;\n" + 
			"class E {\n" + 
			"	E(Supplier<Object> factory) { }\n" + 
			"}\n" + 
			"public class Y extends E {\n" + 
			"	Y() {\n" + 
			"		super( () -> {\n" + 
			"			class Z extends E {\n" + 
			"				Z() {\n" + 
			"					super(() -> new Object());\n" + 
			"				}\n" + 
			"			}\n" + 
			"			return null;\n" + 
			"			});\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Y();\n" + 
			"	}\n" + 
			"}"
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448556
// [1.8][compiler] Invalid compiler error about effectively final variable outside the context of a lambda.
public void testBug4448556() {
	this.runConformTest(new String [] {
		"X.java",
		"import java.io.Serializable;\n" + 
		"import java.util.Arrays;\n" + 
		"import java.util.List;\n" + 
		"public class X {\n" + 
		"    private static final List<Integer> INTEGERS = Arrays.asList(1, 2, 3, 4);\n" + 
		"    public static void main(String[] args) {\n" + 
		"        for (int i = 0; i < INTEGERS.size(); i++) {\n" + 
		"            MyPredicate<Integer> predicate = INTEGERS.get(i)::equals;\n" + 
		"        }\n" + 
		"    }  \n" + 
		"    public interface MyPredicate<T> extends Serializable {\n" + 
		"        boolean accept(T each);\n" + 
		"    }\n" + 
		"}"
	},
	"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448556
// [1.8][compiler] Invalid compiler error about effectively final variable outside the context of a lambda.
public void testBug4448556a() {
	this.runConformTest(new String [] {
		"X.java",
		"import java.io.Serializable;\n" + 
		"import java.util.Arrays;\n" + 
		"import java.util.List;\n" + 
		"public class X {\n" + 
		"	int value = 0; \n" + 
		"    private static final List<Integer> INTEGERS = Arrays.asList(1, 2, 3, 4);\n" + 
		"    public Integer next() {\n" + 
		"    	return new Integer(++value);\n" + 
		"    }\n" + 
		"    public static void main(String[] args) {\n" + 
		"    	X t = new X();\n" + 
		"        MyPredicate<Integer> predicate = t.next()::equals;\n" + 
		"        System.out.println(\"Value \" + t.value + \" accept \" + predicate.accept(t.value));\n" + 
		"    }\n" + 
		"    public interface MyPredicate<T> extends Serializable {\n" + 
		"        boolean accept(T each);\n" + 
		"    }\n" + 
		"}"
	},
	"Value 1 accept true");
}
public static Class testClass() {
	return LambdaRegressionTest.class;
}
}
