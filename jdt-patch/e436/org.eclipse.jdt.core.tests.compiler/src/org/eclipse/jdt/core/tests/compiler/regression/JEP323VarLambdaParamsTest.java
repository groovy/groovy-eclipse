/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
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

import java.io.IOException;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JEP323VarLambdaParamsTest extends AbstractRegressionTest {

public static Class testClass() {
	return JEP323VarLambdaParamsTest.class;
}
@Override
public void initialize(CompilerTestSetup setUp) {
	super.initialize(setUp);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_11);
}

public JEP323VarLambdaParamsTest(String testName){
	super(testName);
}
static {
//	TESTS_NUMBERS = new int[] { 1 };
//	TESTS_RANGE = new int[] { 1, -1 };
//	TESTS_NAMES = new String[] { "testBug534787_positive_001" };
}
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);
	return options;
}

public void testBug534787_positive_001() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (var  x) -> {System.out.println(\"SUCCESS \" + x);};\n" +
				"        lam.apply(20);\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k);\n" +
				"}\n"
			},
			"SUCCESS 20");
}
public void testBug534787_positive_002() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (var  x, var y) -> {System.out.println(\"SUCCESS \" + (x+y));};\n" +
				"        lam.apply(20, 200);\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k, Integer l);\n" +
				"}\n"
			},
			"SUCCESS 220");
}
public void testBug534787_positive_003() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = var -> {System.out.println(\"SUCCESS \" + var);};\n" +
				"        lam.apply(10);\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k);\n" +
				"}\n"
			},
			"SUCCESS 10");
}
public void testBug534787_positive_004() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (var var) -> {System.out.println(\"SUCCESS \" + var);};\n" +
				"        lam.apply(10);\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k);\n" +
				"}\n"
			},
			"SUCCESS 10");
}
public void testBug534787_negative_001() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (var  x, y) -> {System.out.println(\"SUCCESS \" + x);};\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k, Integer z);\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	I lam = (var  x, y) -> {System.out.println(\"SUCCESS \" + x);};\n" +
			"	         ^^^\n" +
			"Syntax error on token \"var\", ( expected after this token\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	I lam = (var  x, y) -> {System.out.println(\"SUCCESS \" + x);};\n" +
			"	                  ^\n" +
			"Syntax error on token \")\", delete this token\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	I lam = (var  x, y) -> {System.out.println(\"SUCCESS \" + x);};\n" +
			"	                                                           ^\n" +
			"Syntax error, insert \")\" to complete Expression\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 3)\n" +
			"	I lam = (var  x, y) -> {System.out.println(\"SUCCESS \" + x);};\n" +
			"	                                                           ^\n" +
			"Syntax error, insert \")\" to complete Expression\n" +
			"----------\n");
}
public void testBug534787_negative_002() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (var  x, Integer y) -> {System.out.println(\"SUCCESS \" + x);};\n" +
				"        lam.apply(20, 200);\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k, Integer z);\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	I lam = (var  x, Integer y) -> {System.out.println(\"SUCCESS \" + x);};\n" +
			"	              ^\n" +
			"\'var\' cannot be mixed with non-var parameters\n" +
			"----------\n");
}
public void testBug534787_negative_003() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (Integer  x, var y) -> {System.out.println(\"SUCCESS \" + x);};\n" +
				"        lam.apply(20, 200);\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k, Integer z);\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	I lam = (Integer  x, var y) -> {System.out.println(\"SUCCESS \" + x);};\n" +
			"	                         ^\n" +
			"'var' cannot be mixed with non-var parameters\n" +
			"----------\n");
}
public void testBug534787_negative_004() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (var  x, var y, var...s) -> {System.out.println(\"SUCCESS \" + x);};\n" +
				"        lam.apply(20, 200, \"hello\");\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k, Integer z, String s);\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	I lam = (var  x, var y, var...s) -> {System.out.println(\"SUCCESS \" + x);};\n" +
			"	                              ^\n" +
			"'var' is not allowed as an element type of an array\n" +
			"----------\n");
}
public void testBug534787_negative_005() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (var  x, Integer y, var...s) -> {System.out.println(\"SUCCESS \" + x);};\n" +
				"        lam.apply(20, 200, \"hello\");\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k, Integer z, String s);\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	I lam = (var  x, Integer y, var...s) -> {System.out.println(\"SUCCESS \" + x);};\n" +
			"	              ^\n" +
			"'var' cannot be mixed with non-var parameters\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	I lam = (var  x, Integer y, var...s) -> {System.out.println(\"SUCCESS \" + x);};\n" +
			"	                                  ^\n" +
			"'var' is not allowed as an element type of an array\n" +
			"----------\n");
}

public void testBug534787_negative_006() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = var  x -> {System.out.println(\"SUCCESS \" + x);};\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k);\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	I lam = var  x -> {System.out.println(\"SUCCESS \" + x);};\n" +
			"	             ^\n" +
			"Syntax error on token \"x\", delete this token\n" +
			"----------\n");
}
public void testBug536159_01() throws IOException {
	runConformTest(new String[] {
			"X.java",
			"public class X<T> {\n"
			+ "public static void main(String[] args) {\n"
			+ "  FI x = (int i []) -> 5;\n"
			+ "    }\n"
			+ "}\n"
			+ "interface FI {\n"
			+ "  public int foo (int i []);\n"
			+ "}"
	});
}
public void testBug536159_02() throws IOException {
	runConformTest(new String[] {
			"X.java",
			"public class X<T> {\n"
			+ "public static void main(String[] args) {\n"
			+ "  FI x = (int[] i []) -> 5;\n"
			+ "    }\n"
			+ "}\n"
			+ "interface FI {\n"
			+ "  public int foo (int i [][]);\n"
			+ "}"
	});
}
public void testBug536159_03() throws IOException {
	runConformTest(new String[] {
			"X.java",
			"public class X<T> {\n"
			+ "public static void main(String[] args) {\n"
			+ "  FI x = (int i [][]) -> 5;\n"
			+ "    }\n"
			+ "}\n"
			+ "interface FI {\n"
			+ "  public int foo (int i [][]);\n"
			+ "}"
	});
}
public void testBug536159_04() throws IOException {
	runNegativeTest(new String[] {
			"X.java",
			"public class X<T> {\n"
			+ "public static void main(String[] args) {\n"
			+ "  FI x = (var i []) -> 5;\n"
			+ "    }\n"
			+ "}\n"
			+ "interface FI {\n"
			+ "  public int foo (int i []);\n"
			+ "}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	FI x = (var i []) -> 5;\n" +
			"	            ^\n" +
			"\'var\' is not allowed as an element type of an array\n" +
			"----------\n");
}
public void testBug541532_01() throws IOException {
	runConformTest(new String[] {
			"X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class X {\n" +
			"\n" +
			"	public static void foo(List<String> list) {\n" +
			"		list.stream()\n" +
			"		  .map((var s) -> s.toLowerCase())\n" +
			"		  .forEach(System.out::println);\n" +
			"\n" +
			"		list.stream()\n" +
			"		  .filter((var s) -> s.length() == 1)\n" +
			"		  .forEach(System.out::println);\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		String[] greetings = {\"hello\", \"world\"};\n" +
			"		X.foo(Arrays.asList(greetings));\n" +
			"	}\n" +
			"}\n"
		},
		"hello\nworld");
}
}