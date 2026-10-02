/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class UnderscoresInLiteralsTest extends AbstractRegressionTest {
	static {
//		TESTS_NUMBERS = new int[] { 24 };
	}
	public UnderscoresInLiteralsTest(String name) {
		super(name);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), FIRST_SUPPORTED_JAVA_VERSION);
	}

	public static Class testClass() {
		return UnderscoresInLiteralsTest.class;
	}

	public void test001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0b_001);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(0b_001);\n" +
			"	                   ^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0_b001);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(0_b001);\n" +
			"	                   ^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0b001_);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(0b001_);\n" +
			"	                   ^^^^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0x_11.0p33f);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(0x_11.0p33f);\n" +
			"	                   ^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0x11_.0p33f);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(0x11_.0p33f);\n" +
			"	                   ^^^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0x11._0p33f);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(0x11._0p33f);\n" +
			"	                   ^^^^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0x11.0_p33f);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(0x11.0_p33f);\n" +
			"	                   ^^^^^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0x11.0p_33f);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(0x11.0p_33f);\n" +
			"	                   ^^^^^^^^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test009() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0x11.0p33_f);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(0x11.0p33_f);\n" +
			"	                   ^^^^^^^^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0x_0001AEFBBA);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(0x_0001AEFBBA);\n" +
			"	                   ^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0_x0001AEFBBA);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(0_x0001AEFBBA);\n" +
			"	                   ^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test012() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0x0001AEFBBA_);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(0x0001AEFBBA_);\n" +
			"	                   ^^^^^^^^^^^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test013() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(_01234567);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(_01234567);\n" +
			"	                   ^^^^^^^^^\n" +
			"_01234567 cannot be resolved to a variable\n" +
			"----------\n");
	}
	public void test014() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(01234567_);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(01234567_);\n" +
			"	                   ^^^^^^^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test015() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(1_.236589954f);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(1_.236589954f);\n" +
			"	                   ^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test016() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(1._236589954f);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(1._236589954f);\n" +
			"	                   ^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(1_e2);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(1_e2);\n" +
			"	                   ^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test018() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(1e_2);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(1e_2);\n" +
			"	                   ^^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test019() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(1e2_);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(1e2_);\n" +
			"	                   ^^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test020() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(01e2_);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(01e2_);\n" +
			"	                   ^^^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test021() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(01_e2_);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(01_e2_);\n" +
			"	                   ^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n");
	}
	public void test022() {
		Map customedOptions = getCompilerOptions();
		customedOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.getFirstSupportedJavaVersion());
		customedOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.getFirstSupportedJavaVersion());
		customedOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.getFirstSupportedJavaVersion());
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0b1110000_);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(0b1110000_);\n" +
			"	                   ^^^^^^^^^^\n" +
			"Underscores have to be located within digits\n" +
			"----------\n",
			null,
			true,
			customedOptions);
	}
	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(0x1234____5678____90L);\n" +
				"	}\n" +
				"}"
			},
			"78187493520");
	}
	public void test024() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(90_00__00_0);\n" +
				"	}\n" +
				"}"
			},
			"9000000");
	}
}
