package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class UseOfUnderscoreWithPreviewTest extends AbstractBatchCompilerTest {

	public static Test suite() {
		return buildMinimalComplianceTestSuite(UseOfUnderscoreWithPreviewTest.class, F_21);
	}

	public UseOfUnderscoreWithPreviewTest(String name) {
		super(name);
	}

	@Override
	protected Map<String, String> getCompilerOptions() {
		CompilerOptions compilerOptions = new CompilerOptions(super.getCompilerOptions());
		if (compilerOptions.sourceLevel == ClassFileConstants.JDK21) {
			compilerOptions.enablePreviewFeatures = true;
		}
		return compilerOptions.getMap();
	}

	public void testReportsUnderscoreInstanceMemberAsError() {
		String message = "As of release 21, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters";
		String errorLevel = "ERROR";

		runNegativeTest(new String[] { "A.java", """
				public class A {
					int _ = 1;
					public static void main(String[] args) {
						System.out.println("Hello, World!");
					}
				}
				""" },

				"----------\n" +
				"1. " + errorLevel + " in A.java (at line 2)\n" +
				"	int _ = 1;\n" +
				"	    ^\n" +
				message + "\n" +
				"----------\n");
	}

	public void testReportsUnicodeEscapeUnderscoreInstanceMemberAsError() {
		String message = "As of release 21, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters";
		String errorLevel = "ERROR";

		runNegativeTest(new String[] { "A.java", """
				public class A {
					int \\u005F = 1;
					public static void main(String[] args) {
						System.out.println("Hello, World!");
					}
				}
				""" },
				"----------\n" +
				"1. " + errorLevel + " in A.java (at line 2)\n" +
				"	int \\u005F = 1;\n" +
				"	    ^^^^^^\n" +
				message + "\n" +
				"----------\n");
	}

	public void testReportsUnderscoreParameterAsError() {
		String message = "As of release 21, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters";
		String errorLevel = "ERROR";

		runNegativeTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						foo(1);
					}
					public static void foo(int _) {
						System.out.println("Hello, World!");
					}
				}
				""" },
				"----------\n" +
				"1. " + errorLevel + " in A.java (at line 5)\n" +
				"	public static void foo(int _) {\n" +
				"	                           ^\n" +
				message + "\n" +
				"----------\n");
	}

	public void testReportsUnderscoreParameterAsErrorUnicodeEscape() {
		String message = "As of release 21, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters";
		String errorLevel = "ERROR";

		runNegativeTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						foo(1);
					}
					public static void foo(int \\u005F) {
						System.out.println("Hello, World!");
					}
				}
				""" },
				"----------\n" +
				"1. " + errorLevel + " in A.java (at line 5)\n" +
				"	public static void foo(int \\u005F) {\n" +
				"	                           ^^^^^^\n" +
				message + "\n" +
				"----------\n");
	}

	public void testReportsUnderscoreLocalVariableAsErrorUnicodeEscape() {
		runConformTest(new String[] { "A.java",
				"""
				public class A {
					public static void main(String[] args) {
						int \\u005F = 12;
						System.out.println("hello, world");
					}
				}
				"""},
				"hello, world", null, new String[] { "--enable-preview" });
	}

}
