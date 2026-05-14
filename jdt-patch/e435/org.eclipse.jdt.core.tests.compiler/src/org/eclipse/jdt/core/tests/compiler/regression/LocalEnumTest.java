/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *		Stephan Herrmann - Contributions for
 *								Bug 365519 - editorial cleanup after bug 186342 and bug 365387
 *								Bug 265744 - Enum switch should warn about missing default
 *								Bug 374605 - Unreasonable warning for enum-based switch statements
 *								bug 388739 - [1.8][compiler] consider default methods when detecting whether a class needs to be declared abstract
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class LocalEnumTest extends AbstractComparableTest {

	String reportMissingJavadocComments = null;

	public LocalEnumTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test004" };
//		TESTS_NUMBERS = new int[] { 185 };
//		TESTS_RANGE = new int[] { 21, 50 };
	}
	public static Test suite() {
//		return buildComparableTestSuite(testClass());
		return buildMinimalComplianceTestSuite(testClass(), F_16);
	}

	public static Class testClass() {
		return LocalEnumTest.class;
	}

	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_16); // FIXME
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
		if (this.reportMissingJavadocComments != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.reportMissingJavadocComments = null;
	}

	@Override
	protected void runConformTest(String[] testFiles) {
		runConformTest(testFiles, "", getCompilerOptions());
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions());
	}
//	@Override
//	protected void runConformTest(String[] testFiles, String expectedOutput, Map customOptions) {
//		Runner runner = new Runner();
//		runner.testFiles = testFiles;
//		runner.expectedOutputString = expectedOutput;
//		runner.vmArguments = new String[] {"--enable-preview"};
//		runner.customOptions = customOptions;
//		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview("16");
//		runner.runConformTest();
//	}
//
//	@Override
//	protected void runConformTest(
//			// test directory preparation
//			boolean shouldFlushOutputDirectory,
//			String[] testFiles,
//			//compiler options
//			String[] classLibraries /* class libraries */,
//			Map customOptions /* custom options */,
//			// compiler results
//			String expectedCompilerLog,
//			// runtime results
//			String expectedOutputString,
//			String expectedErrorString,
//			// javac options
//			JavacTestOptions javacTestOptions) {
//		Runner runner = new Runner();
//		runner.testFiles = testFiles;
//		runner.expectedOutputString = expectedOutputString;
//		runner.expectedCompilerLog = expectedCompilerLog;
//		runner.expectedErrorString = expectedErrorString;
//		runner.vmArguments = new String[] {"--enable-preview"};
//		runner.customOptions = customOptions;
//		runner.javacTestOptions = javacTestOptions == null ? JavacTestOptions.forReleaseWithPreview("16") : javacTestOptions;
//		runner.runConformTest();
//	}
//
//	@Override
//	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
//		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forReleaseWithPreview("16"));
//	}
//	protected void runWarningTest(String[] testFiles, String expectedCompilerLog) {
//		runWarningTest(testFiles, expectedCompilerLog, null);
//	}
//	protected void runWarningTest(String[] testFiles, String expectedCompilerLog, Map<String, String> customOptions) {
//		runWarningTest(testFiles, expectedCompilerLog, customOptions, null);
//	}
//	protected void runWarningTest(String[] testFiles, String expectedCompilerLog,
//			Map<String, String> customOptions, String javacAdditionalTestOptions) {
//
//		Runner runner = new Runner();
//		runner.testFiles = testFiles;
//		runner.expectedCompilerLog = expectedCompilerLog;
//		runner.customOptions = customOptions;
//		runner.vmArguments = new String[] {"--enable-preview"};
//		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forReleaseWithPreview("15") :
//			JavacTestOptions.forReleaseWithPreview("16", javacAdditionalTestOptions);
//		runner.runWarningTest();
//	}

	private void verifyClassFile(String expectedOutput, String classFileName, int mode, boolean positive) throws IOException, ClassFormatException {
		String result = getClassFileContents(classFileName, mode);
		verifyOutput(result, expectedOutput, positive);
	}
	private String getClassFileContents( String classFileName, int mode) throws IOException,
	ClassFormatException {
		File f = new File(OUTPUT_DIR + File.separator + classFileName);
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", mode);
		return result;
	}
	private void verifyOutput(String result, String expectedOutput, boolean positive) {
		int index = result.indexOf(expectedOutput);
		if (positive) {
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 3));
				System.out.println("...");
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
		} else {
			if (index != -1) {
				assertEquals("Unexpected contents", "", result);
			}
		}
	}

// test simple valid enum and its usage
public void test000() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"    public static void main(String[] args) {\n"+
			"          enum Role { M, D }\n"+
			" enum T {\n"+
			"       PHILIPPE(37) {\n"+
			"               public boolean isManager() {\n"+
			"                       return true;\n"+
			"               }\n"+
			"       },\n"+
			"       DAVID(27),\n"+
			"       JEROME(33),\n"+
			"       OLIVIER(35),\n"+
			"       KENT(40),\n"+
			"       YODA(41),\n"+
			"       FREDERIC;\n"+
			"       final static int OLD = 41;\n"+
			"\n"+
			"\n"+
			"   int age;\n"+
			"       Role role;\n"+
			"\n"+
			"       T() { this(OLD); }\n"+
			"       T(int age) {\n"+
			"               this.age = age;\n"+
			"       }\n"+
			"       public int age() { return this.age; }\n"+
			"       public boolean isManager() { return false; }\n"+
			"       void setRole(boolean mgr) {\n"+
			"               this.role = mgr ? Role.M : Role.D;\n"+
			"       }\n"+
			"}\n"+
			"       System.out.print(\"JDTCore team:\");\n"+
			"       T oldest = null;\n"+
			"       int maxAge = Integer.MIN_VALUE;\n"+
			"       for (T t : T.values()) {\n"+
			"            if (t == T.YODA) continue;// skip YODA\n"+
			"            t.setRole(t.isManager());\n"+
			"                        if (t.age() > maxAge) {\n"+
			"               oldest = t;\n"+
			"               maxAge = t.age();\n"+
			"            }\n"+
			"                      Location l = switch(t) {\n"+
			"                         case PHILIPPE, DAVID, JEROME, FREDERIC-> Location.SNZ;\n"+
			"                         case OLIVIER, KENT -> Location.OTT;\n"+
			"                         default-> throw new AssertionError(\"Unknown team member: \" + t);\n"+
			"                       };\n"+
			"\n"+
			"            System.out.print(\" \"+ t + ':'+t.age()+':'+l+':'+t.role);\n"+
			"        }\n"+
			"        System.out.println(\" WINNER is:\" + T.valueOf(oldest.name()));\n"+
			"    }\n"+
			"\n"+
			"   private enum Location { SNZ, OTT }\n"+
			"}"
		},
		"JDTCore team: PHILIPPE:37:SNZ:M DAVID:27:SNZ:D JEROME:33:SNZ:D OLIVIER:35:OTT:D KENT:40:OTT:D FREDERIC:41:SNZ:D WINNER is:FREDERIC"
	);
}
// check assignment to enum constant is disallowed
public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X { \n" +
			"	public static void main(String[] args) {\n" +
			"    enum Y { \n" +
			"	   BLEU, \n" +
			"	   BLANC, \n" +
			"	   ROUGE;\n" +
			"	   static {\n" +
			"	   	 BLEU = null;\n" +
			"	   }\n" +
			"	 }\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	BLEU = null;\n" +
		"	^^^^\n" +
		"The final field Y.BLEU cannot be assigned\n" +
		"----------\n");
}
// check diagnosis for duplicate enum constants
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X { \n" +
			"	public static void main(String[] args) {\n" +
			"    enum Y { \n" +
			"		\n" +
			"		BLEU, \n" +
			"		BLANC, \n" +
			"		ROUGE,\n" +
			"		BLEU;\n" +
			"	 }\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	BLEU, \n" +
		"	^^^^\n" +
		"Duplicate field Y.BLEU\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	BLEU;\n" +
		"	^^^^\n" +
		"Duplicate field Y.BLEU\n" +
		"----------\n");
}
// check properly rejecting enum constant modifiers
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X { \n" +
			"	public static void main(String[] args) {\n" +
			"    enum Y { \n" +
			"	\n" +
			"		public BLEU, \n" +
			"		transient BLANC, \n" +
			"		ROUGE, \n" +
			"		abstract RED {\n" +
			"			void _test() {}\n" +
			"		}\n" +
			"	 }\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	public BLEU, \n" +
		"	       ^^^^\n" +
		"Illegal modifier for the enum constant BLEU; no modifier is allowed\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	transient BLANC, \n" +
		"	          ^^^^^\n" +
		"Illegal modifier for the enum constant BLANC; no modifier is allowed\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 8)\n" +
		"	abstract RED {\n" +
		"	         ^^^\n" +
		"Illegal modifier for the enum constant RED; no modifier is allowed\n" +
		"----------\n");
}
// check using an enum constant
public void test004() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"     enum Y { \n" +
			"		\n" +
			"		BLEU,\n" +
			"		BLANC,\n" +
			"		ROUGE;\n" +
			"		\n" +
			"		public static void main(String[] a) {\n" +
			"			System.out.println(BLEU);\n" +
			"		}\n" +
			"		\n" +
			"	  }\n" +
			"	  Y.main(args);\n" +
			"	}\n" +
		"}\n"
		},
		"BLEU");
}
// check method override diagnosis (with no enum constants)
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X { \n" +
			"	public static void main(String[] args) {\n" +
			"     enum Y { \n" +
			"		;\n" +
			"		protected Object clone() { return this; }\n" +
			"	  }\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	protected Object clone() { return this; }\n" +
		"	                 ^^^^^^^\n" +
		"Cannot override the final method from Enum<Y>\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	protected Object clone() { return this; }\n" +
		"	                 ^^^^^^^\n" +
		"The method clone() of type Y should be tagged with @Override since it actually overrides a superclass method\n" +
		"----------\n");
}
// check generated #values() method
public void test006() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"     enum Y { \n" +
			"		\n" +
			"		BLEU,\n" +
			"		BLANC,\n" +
			"		ROUGE;\n" +
			"		\n" +
			"		public static void main(String[] args) {\n" +
			"			for(Y y: Y.values()) {\n" +
			"				System.out.print(y);\n" +
			"			}\n" +
			"		}\n" +
			"		\n" +
			"	  }\n" +
			"	  Y.main(args);\n" +
			"	}\n" +
		"}\n"
		},
		"BLEUBLANCROUGE");
}
// tolerate user definition for $VALUES
public void test007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"     enum Y { \n" +
			"		\n" +
			"		BLEU,\n" +
			"		BLANC,\n" +
			"		ROUGE;\n" +
			"		\n" +
			"      int $VALUES;\n" +
			"		public static void main(String[] args) {\n" +
			"				for(Y y: Y.values()) {\n" +
			"					System.out.print(y);\n" +
			"			}\n" +
			"		}\n" +
			"		\n" +
			"	  }\n" +
			"	  Y.main(args);\n" +
			"	}\n" +
			"}\n"
		},
		"BLEUBLANCROUGE");
}
// reject user definition for #values()
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X { \n" +
			"	public static void main(String[] args) {\n" +
			"     enum Y { \n" +
			"		\n" +
			"		BLEU,\n" +
			"		BLANC,\n" +
			"		ROUGE;\n" +
			"		\n" +
			"      void dup() {} \n" +
			"      void values() {} \n" +
			"      void dup() {} \n" +
			"      void values() {} \n" +
			"      Missing dup() {} \n" +
			"		public static void main(String[] args) {\n" +
			"				for(Y y: Y.values()) {\n" +
			"					System.out.print(y);\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"	  }\n" +
			"	  Y.main(args);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	void dup() {} \n" +
		"	     ^^^^^\n" +
		"Duplicate method dup() in type Y\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	void values() {} \n" +
		"	     ^^^^^^^^\n" +
		"The enum Y already defines the method values() implicitly\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 11)\n" +
		"	void dup() {} \n" +
		"	     ^^^^^\n" +
		"Duplicate method dup() in type Y\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 12)\n" +
		"	void values() {} \n" +
		"	     ^^^^^^^^\n" +
		"The enum Y already defines the method values() implicitly\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 13)\n" +
		"	Missing dup() {} \n" +
		"	^^^^^^^\n" +
		"Missing cannot be resolved to a type\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 13)\n" +
		"	Missing dup() {} \n" +
		"	        ^^^^^\n" +
		"Duplicate method dup() in type Y\n" +
		"----------\n" +
		"7. WARNING in X.java (at line 14)\n" +
		"	public static void main(String[] args) {\n" +
		"	                                 ^^^^\n" +
		"The parameter args is hiding another local variable defined in an enclosing scope\n" +
		"----------\n");
}
// switch on enum
public void test009() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"     enum Y { \n" +
			"	\n" +
			"		BLEU,\n" +
			"		BLANC,\n" +
			"		ROUGE;\n" +
			"		\n" +
			"		//void values() {}\n" +
			"		\n" +
			"		public static void main(String[] args) {\n" +
			"			Y y = BLEU;\n" +
			"			switch(y) {\n" +
			"				case BLEU :\n" +
			"					System.out.println(\"SUCCESS\");\n" +
			"					break;\n" +
			"				case BLANC :\n" +
			"				case ROUGE :\n" +
			"					System.out.println(\"FAILED\");\n" +
			"					break;\n" +
			"	           default: // nop\n" +
			"   		}\n" +
			"		}\n" +
			"		\n" +
			"	  }\n" +
			"	  Y.main(args);\n" +
			"	}\n" +
			"}"
		},
		"SUCCESS");
}
// duplicate switch case
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X { \n" +
			"	public static void main(String[] args) {\n" +
			"     enum Y { \n" +
			"		\n" +
			"		BLEU,\n" +
			"		BLANC,\n" +
			"		ROUGE;\n" +
			"		\n" +
			"		//void values() {}\n" +
			"		\n" +
			"		public static void main(String[] args) {\n" +
			"			Y y = BLEU;\n" +
			"			switch(y) {\n" +
			"				case BLEU :\n" +
			"					break;\n" +
			"				case BLEU :\n" +
			"				case BLANC :\n" +
			"				case ROUGE :\n" +
			"					System.out.println(\"FAILED\");\n" +
			"					break;\n" +
			"              default: // nop\n" +
			"			}\n" +
			"		}\n" +
			"	\n" +
			"	  }\n" +
			"	  Y.main(args);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 11)\n" +
		"	public static void main(String[] args) {\n" +
		"	                                 ^^^^\n" +
		"The parameter args is hiding another local variable defined in an enclosing scope\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 16)\n" +
		"	case BLEU :\n" +
		"	     ^^^^\n" +
		"Duplicate case\n" +
		"----------\n");
}
// reject user definition for #values()
public void test011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X { \n" +
			"	public static void main(String[] args) {\n" +
			"     enum Y { \n" +
			"	\n" +
			"	BLEU,\n" +
			"	BLANC,\n" +
			"	ROUGE;\n" +
			"	\n" +
			"   void values() {} \n" +
			"   void values() {} \n" +
			"	public static void main(String[] args) {\n" +
			"		for(Y y: Y.values()) {\n" +
			"			System.out.print(y);\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"	  }\n" +
			"	  Y.main(args);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	void values() {} \n" +
		"	     ^^^^^^^^\n" +
		"The enum Y already defines the method values() implicitly\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	void values() {} \n" +
		"	     ^^^^^^^^\n" +
		"The enum Y already defines the method values() implicitly\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 11)\n" +
		"	public static void main(String[] args) {\n" +
		"	                                 ^^^^\n" +
		"The parameter args is hiding another local variable defined in an enclosing scope\n" +
		"----------\n");
}
// check abstract method diagnosis
public void testNPE012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X { \n" +
			"	public static void main(String[] args) {\n" +
			"	  enum Y implements Runnable { \n" +
			"	\n" +
			"	BLEU,\n" +
			"	BLANC,\n" +
			"	ROUGE;\n" +
			"	  }\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	enum Y implements Runnable { \n" +
		"	     ^\n" +
		"The type Y must implement the inherited abstract method Runnable.run()\n" +
		"----------\n");
}
// check enum constants with wrong arguments
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X { \n" +
			"	public static void main(String[] args) {\n" +
			"	  enum Y  { \n" +
			"	\n" +
			"	BLEU(10),\n" +
			"	BLANC(20),\n" +
			"	ROUGE(30);\n" +
			"	  }\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	BLEU(10),\n" +
		"	^^^^\n" +
		"The constructor Y(int) is undefined\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	BLANC(20),\n" +
		"	^^^^^\n" +
		"The constructor Y(int) is undefined\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	ROUGE(30);\n" +
		"	^^^^^\n" +
		"The constructor Y(int) is undefined\n" +
		"----------\n");
}
// check enum constants with extra arguments
public void test014() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"	  enum Y  { \n" +
			"	\n" +
			"	BLEU(10),\n" +
			"	BLANC(20),\n" +
			"	ROUGE(30);\n" +
			"\n" +
			"	int val;\n" +
			"	Y(int val) {\n" +
			"		this.val = val;\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		for(Y y: values()) {\n" +
			"			System.out.print(y.val);\n" +
			"		}\n" +
			"	}\n" +
			"	  }\n" +
			"	  Y.main(args);\n" +
			"	}\n" +
			"}\n"
		},
		"102030");
}
// check enum constants with wrong arguments
public void test015() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X { \n" +
			"	public static void main(String[] args) {\n" +
			"	  enum Y  { \n" +
			"	\n" +
			"	BLEU(10),\n" +
			"	BLANC(),\n" +
			"	ROUGE(30);\n" +
			"\n" +
			"	int val;\n" +
			"	Y(int val) {\n" +
			"		this.val = val;\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] a) {\n" +
			"		for(Y y: values()) {\n" +
			"			System.out.print(y.val);\n" +
			"		}\n" +
			"	}\n" +
			"	  }\n" +
			"	  Y.main(args);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	BLANC(),\n" +
		"	^^^^^\n" +
		"The constructor Y() is undefined\n" +
		"----------\n");
}
// check enum constants with wrong arguments
public void test016() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"	  enum Y  { \n" +
			"	\n" +
			"	BLEU(10) {\n" +
			"		String foo() { // inner\n" +
			"			return super.foo() + this.val;\n" +
			"		}\n" +
			"	},\n" +
			"	BLANC(20),\n" +
			"	ROUGE(30);\n" +
			"\n" +
			"	int val;\n" +
			"	Y(int val) {\n" +
			"		this.val = val;\n" +
			"	}\n" +
			"	String foo() {  // outer\n" +
			"		return this.name();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		for(Y y: values()) {\n" +
			"			System.out.print(y.foo());\n" +
			"		}\n" +
			"	}\n" +
			"	  }\n" +
			"	  Y.main(args);\n" +
			"	}\n" +
			"}\n"
		},
		"BLEU10BLANCROUGE");
}
// check enum constants with empty arguments
public void test017() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"	  enum Y  { \n" +
			"	\n" +
			"	BLEU()\n" +
			"	  }\n" +
			"	}\n" +
			"}\n"
		},
		"");
}
// cannot extend enums
public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X { \n" +
			"	public static void main(String[] args) {\n" +
			"	  enum Y  { \n" +
			"	    BLEU()\n" +
			"	  }\n" +
			"		\n" +
			"	  class XX extends Y implements Y {\n" +
			"	  }\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	class XX extends Y implements Y {\n" +
		"	                 ^\n" +
		"The type Y cannot be the superclass of XX; a superclass must be a class\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	class XX extends Y implements Y {\n" +
		"	                              ^\n" +
		"The type Y cannot be a superinterface of XX; a superinterface must be an interface\n" +
		"----------\n");
}
// 74851
public void test019() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			" enum MonthEnum {\n" +
			"    JANUARY   (30),\n" +
			"    FEBRUARY  (28),\n" +
			"    MARCH     (31),\n" +
			"    APRIL     (30),\n" +
			"    MAY       (31),\n" +
			"    JUNE      (30),\n" +
			"    JULY      (31),\n" +
			"    AUGUST    (31),\n" +
			"    SEPTEMBER (31),\n" +
			"    OCTOBER   (31),\n" +
			"    NOVEMBER  (30),\n" +
			"    DECEMBER  (31);\n" +
			"    \n" +
			"    private final int days;\n" +
			"    \n" +
			"    MonthEnum(int days) {\n" +
			"        this.days = days;\n" +
			"    }\n" +
			"    \n" +
			"    public int getDays() {\n" +
			"    	boolean leapYear = true;\n" +
			"    	switch(this) {\n" +
			"    		case FEBRUARY: if(leapYear) return days+1;\n" +
			"           default: return days;\n" +
			"    	}\n" +
			"    }\n" +
			"    \n" +
			"    public static void main(String[] args) {\n" +
			"    	System.out.println(JANUARY.getDays());\n" +
			"    }\n" +
			"    \n" +
			"	  }\n" +
			"	  MonthEnum.main(args);\n" +
			"	}\n" +
			"}\n",
		},
		"30");
}
// 74226
public void test020() {
	this.runConformTest(
		new String[] {
			"Foo.java",
			"public class Foo{\n" +
			"	 public static void main(String[] args) {\n" +
			"       enum Rank {FIRST,SECOND,THIRD}\n" +
			"	 }\n" +
			"}\n",
		},
		"");
}
// 74226 variation - check nested enum is implicitly static
public void test021() {
	this.runNegativeTest(
		new String[] {
			"Foo.java",
			"public class Foo {\n" +
			"	 public static void main(String[] args) {\n" +
			"      enum Rank {FIRST,SECOND,THIRD;\n" +
			"            void bar() { foo(); } \n" +
			"      }\n" +
			"	 }\n" +
			"    void foo() {}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in Foo.java (at line 4)\n" +
		"	void bar() { foo(); } \n" +
		"	             ^^^\n" +
		"Cannot make a static reference to the non-static method foo() from the type Foo\n" +
		"----------\n");
}
// 77151 - cannot use qualified name to denote enum constants in switch case label
public void test022() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	\n" +
			"	\n" +
			"	void foo() {\n" +
			"	    enum MX { BLEU, BLANC, ROUGE }\n" +
			"		MX e = MX.BLEU; \n" +
			"		switch(e) {\n" +
			"			case MX.BLEU : break;\n" +
			"			case MX.BLANC : break;\n" +
			"			case MX.ROUGE : break;\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	case MX.BLEU : break;\n" +
		"	     ^^^^^^^\n" +
		"The qualified case label MX.BLEU must be replaced with the unqualified enum constant BLEU\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	case MX.BLANC : break;\n" +
		"	     ^^^^^^^^\n" +
		"The qualified case label MX.BLANC must be replaced with the unqualified enum constant BLANC\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	case MX.ROUGE : break;\n" +
		"	     ^^^^^^^^\n" +
		"The qualified case label MX.ROUGE must be replaced with the unqualified enum constant ROUGE\n" +
		"----------\n");
}

// 77212
public void test023() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"	    enum RuleType{ SUCCESS, FAILURE }\n" +
			"		System.out.print(RuleType.valueOf(RuleType.SUCCESS.name()));\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}

// 77244 - cannot declare final enum
public void test024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"     final enum Y {\n" +
			"	    FOO() {}\n" +
			"     }\n" +
			"	}\n" +
			"}\n" +
			"\n",
		},
	"----------\n" +
	"1. ERROR in X.java (at line 3)\n" +
	"	final enum Y {\n" +
	"	           ^\n" +
	"Illegal modifier for local enum Y; no explicit modifier is permitted\n" +
	"----------\n");
}

// values is using arraycopy instead of clone
public void test025() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"      enum Y {\n" +
			"	SUC, CESS;\n" +
			"	public static void main(String[] args) {\n" +
			"		for (Y y : values()) {\n" +
			"			System.out.print(y.name());\n" +
			"		}\n" +
			"	}\n" +
			"	}\n" +
			"	Y.main(args);\n" +
			"}\n" +
			"}",
		},
		"SUCCESS");
}

// check enum name visibility
public void test026() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"	   enum Couleur { BLEU, BLANC, ROUGE }\n" +
			"   }\n" +
			"  class Y {\n" +
			"	void foo(Couleur c) {\n" +
			"		switch (c) {\n" +
			"			case BLEU :\n" +
			"				break;\n" +
			"			case BLANC :\n" +
			"				break;\n" +
			"			case ROUGE :\n" +
			"				break;\n" +
			"		}\n" +
			"	}\n" +
			"  }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	void foo(Couleur c) {\n" +
		"	         ^^^^^^^\n" +
		"Couleur cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	case BLEU :\n" +
		"	     ^^^^\n" +
		"BLEU cannot be resolved to a variable\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	case BLANC :\n" +
		"	     ^^^^^\n" +
		"BLANC cannot be resolved to a variable\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 12)\n" +
		"	case ROUGE :\n" +
		"	     ^^^^^\n" +
		"ROUGE cannot be resolved to a variable\n" +
		"----------\n");
}
// check enum name visibility
public void test027() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	enum Couleur { BLEU, BLANC, ROUGE }\n" +
			"	class Y {\n" +
			"		void foo(Couleur c) {\n" +
			"			switch (c) {\n" +
			"				case BLEU :\n" +
			"					break;\n" +
			"				case BLANC :\n" +
			"					break;\n" +
			"				case ROUGE :\n" +
			"					break;\n" +
			"               default: // nop\n" +
			"			}\n" +
			"		}	\n" +
			"	}\n" +
			"  }\n" +
			"}\n",
		},
		"");
}
// check enum name visibility
public void test028() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	enum Couleur { \n" +
			"		BLEU, BLANC, ROUGE;\n" +
			"		static int C = 0;\n" +
			"		static void FOO() {}\n" +
			"	}\n" +
			"	class Y {\n" +
			"		void foo(Couleur c) {\n" +
			"			switch (c) {\n" +
			"				case BLEU :\n" +
			"					break;\n" +
			"				case BLANC :\n" +
			"					break;\n" +
			"				case ROUGE :\n" +
			"					break;\n" +
			"               default: // nop\n" +
			"			}\n" +
			"			FOO();\n" +
			"			C++;\n" +
			"		}	\n" +
			"	}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 19)\n" +
		"	FOO();\n" +
		"	^^^\n" +
		"The method FOO() is undefined for the type Y\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 20)\n" +
		"	C++;\n" +
		"	^\n" +
		"C cannot be resolved to a variable\n" +
		"----------\n");
}
// check enum name visibility
public void test029() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	enum Couleur { \n" +
			"		BLEU, BLANC, ROUGE; // take precedence over toplevel BLEU type\n" +
			"	}\n" +
			"	class Y {\n" +
			"		void foo(Couleur c) {\n" +
			"			switch (c) {\n" +
			"				case BLEU :\n" +
			"					break;\n" +
			"				case BLANC :\n" +
			"					break;\n" +
			"				case ROUGE :\n" +
			"					break;\n" +
			"               default: // nop\n" +
			"			}\n" +
			"		}	\n" +
			"	}\n" +
			"  }\n" +
			"}\n" +
			"\n" +
			"class BLEU {}\n",
		},
		"");
}
// check enum name visibility
public void test030() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	enum Couleur { \n" +
			"		BLEU, BLANC, ROUGE; // take precedence over sibling constant from Color\n" +
			"	}\n" +
			"	enum Color { \n" +
			"		BLEU, BLANC, ROUGE;\n" +
			"	}\n" +
			"	class Y {\n" +
			"		void foo(Couleur c) {\n" +
			"			switch (c) {\n" +
			"				case BLEU :\n" +
			"					break;\n" +
			"				case BLANC :\n" +
			"					break;\n" +
			"				case ROUGE :\n" +
			"					break;\n" +
			"               default: // nop\n" +
			"			}\n" +
			"		}	\n" +
			"	}\n" +
			"  }\n" +
			"}\n" +
			"\n" +
			"class BLEU {}\n",
		},
		"");
}
// check enum name visibility
public void test031() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	enum Couleur { \n" +
			"		BLEU, BLANC, ROUGE; // take precedence over toplevel BLEU type\n" +
			"	}\n" +
			"	class Y implements IX, JX {\n" +
			"		void foo(Couleur c) {\n" +
			"			switch (c) {\n" +
			"				case BLEU :\n" +
			"					break;\n" +
			"				case BLANC :\n" +
			"					break;\n" +
			"				case ROUGE :\n" +
			"					break;\n" +
			"               default: // nop\n" +
			"			}\n" +
			"		}	\n" +
			"	}\n" +
			"  }\n" +
			"}\n" +
			"\n" +
			"interface IX {\n" +
			"	int BLEU = 1;\n" +
			"}\n" +
			"interface JX {\n" +
			"	int BLEU = 2;\n" +
			"}\n" +
			"class BLEU {}\n" +
			"\n",
		},
		"");
}

// check Enum cannot be used as supertype (explicitly)
public void test032() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"     class Y extends Enum {\n" +
			"	}\n" +
			"  }\n" +
			"}",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	class Y extends Enum {\n" +
		"	                ^^^^\n" +
		"Enum is a raw type. References to generic type Enum<E> should be parameterized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	class Y extends Enum {\n" +
		"	                ^^^^\n" +
		"The type Y may not subclass Enum explicitly\n" +
		"----------\n");
}

// Javadoc in enum (see bug 78018)
public void test033() {
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"	/**\n" +
				"	 * Valid javadoc\n" +
				"	 * @author ffr\n" +
				"	 */\n" +
				" enum E {\n" +
				"	/** Valid javadoc */\n" +
				"	TEST,\n" +
				"	/** Valid javadoc */\n" +
				"	VALID;\n" +
				"	/** Valid javadoc */\n" +
				"	public void foo() {}\n" +
				"	}\n" +
				"  }\n" +
				"}\n"
		}
	);
}
public void test034() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"	/**\n" +
				"	 * Invalid javadoc\n" +
				"	 * @exception NullPointerException Invalid tag\n" +
				"	 * @throws NullPointerException Invalid tag\n" +
				"	 * @return Invalid tag\n" +
				"	 * @param x Invalid tag\n" +
				"	 */\n" +
				"	 enum E { TEST, VALID }\n" +
				"  }\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	* @exception NullPointerException Invalid tag\n" +
			"	   ^^^^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	* @throws NullPointerException Invalid tag\n" +
			"	   ^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	* @return Invalid tag\n" +
			"	   ^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	* @param x Invalid tag\n" +
			"	   ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test035() {
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"	/**\n" +
				"	 * @see \"Valid normal string\"\n" +
				"	 * @see <a href=\"http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html\">Valid URL link reference</a>\n" +
				"	 * @see Object\n" +
				"	 * @see #TEST\n" +
				"	 * @see E\n" +
				"	 * @see E#TEST\n" +
				"	 */\n" +
				"    enum E { TEST, VALID }\n" +
				"  }\n" +
				"}"
		}
	);
}
public void test036() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"	/**\n" +
				"	 * @see \"invalid\" no text allowed after the string\n" +
				"	 * @see <a href=\"invalid\">invalid</a> no text allowed after the href\n" +
				"	 * @see\n" +
				"	 * @see #VALIDE\n" +
				"	 */\n" +
				"    enum E { TEST, VALID }\n" +
				"  }\n" +
				"}"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	* @see \"invalid\" no text allowed after the string\n" +
			"	                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Unexpected text\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	* @see <a href=\"invalid\">invalid</a> no text allowed after the href\n" +
			"	                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Unexpected text\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	* @see\n" +
			"	   ^^^\n" +
			"Javadoc: Missing reference\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	* @see #VALIDE\n" +
			"	        ^^^^^^\n" +
			"Javadoc: VALIDE cannot be resolved or is not a field\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test037() {
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"	/**\n" +
				"	 * Value test: {@value #TEST}\n" +
				"	 * or: {@value E#TEST}\n" +
				"	 */\n" +
				"    enum E { TEST, VALID }\n" +
				"  }\n" +
				"}"
		}
	);
}
public void test038() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    enum E { TEST, VALID;\n" +
			"	  public void foo() {}\n" +
			"  }\n" +
			"}\n" +
			"}"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X {\n" +
			"	             ^\n" +
			"Javadoc: Missing comment for public declaration\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	public static void main(String[] args) {\n" +
			"	                   ^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Missing comment for public declaration\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test039() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    enum E {\n" +
				"	/**\n" +
				"	 * @exception NullPointerException Invalid tag\n" +
				"	 * @throws NullPointerException Invalid tag\n" +
				"	 * @return Invalid tag\n" +
				"	 * @param x Invalid tag\n" +
				"	 */\n" +
				"	TEST,\n" +
				"	VALID;\n" +
				"  }\n" +
				"}\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	* @exception NullPointerException Invalid tag\n" +
			"	   ^^^^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	* @throws NullPointerException Invalid tag\n" +
			"	   ^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	* @return Invalid tag\n" +
			"	   ^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	* @param x Invalid tag\n" +
			"	   ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test040() {
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    enum E {\n" +
				"	/**\n" +
				"	 * @see E\n" +
				"	 * @see #VALID\n" +
				"	 */\n" +
				"	TEST,\n" +
				"	/**\n" +
				"	 * @see E#TEST\n" +
				"	 * @see E\n" +
				"	 */\n" +
				"	VALID;\n" +
				"	/**\n" +
				"	 * @param x the object\n" +
				"	 * @return String\n" +
				"	 * @see Object\n" +
				"	 */\n" +
				"	public String val(Object x) { return x.toString(); }\n" +
				"  }\n" +
				"}\n" +
				"}\n"
		}
	);
}
public void test041() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    enum E {\n" +
				"	/**\n" +
				"	 * @see e\n" +
				"	 * @see #VALIDE\n" +
				"	 */\n" +
				"	TEST,\n" +
				"	/**\n" +
				"	 * @see E#test\n" +
				"	 * @see EUX\n" +
				"	 */\n" +
				"	VALID;\n" +
				"	/**\n" +
				"	 * @param obj the object\n" +
				"	 * @return\n" +
				"	 * @see Objet\n" +
				"	 */\n" +
				"	public String val(Object x) { return x.toString(); }\n" +
				"  }\n" +
				"}\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	* @see e\n" +
		"	       ^\n" +
		"Javadoc: e cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	* @see #VALIDE\n" +
		"	        ^^^^^^\n" +
		"Javadoc: VALIDE cannot be resolved or is not a field\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	* @see E#test\n" +
		"	         ^^^^\n" +
		"Javadoc: test cannot be resolved or is not a field\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 11)\n" +
		"	* @see EUX\n" +
		"	       ^^^\n" +
		"Javadoc: EUX cannot be resolved to a type\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 15)\n" +
		"	* @param obj the object\n" +
		"	         ^^^\n" +
		"Javadoc: Parameter obj is not declared\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 16)\n" +
		"	* @return\n" +
		"	   ^^^^^^\n" +
		"Javadoc: Description expected after @return\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 17)\n" +
		"	* @see Objet\n" +
		"	       ^^^^^\n" +
		"Javadoc: Objet cannot be resolved to a type\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test042() {
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    enum E {\n" +
				"	/**\n" +
				"	 * Test value: {@value #TEST}\n" +
				"	 */\n" +
				"	TEST,\n" +
				"	/**\n" +
				"	 * Valid value: {@value E#VALID}\n" +
				"	 */\n" +
				"	VALID;\n" +
				"	/**\n" +
				"	 * Test value: {@value #TEST}\n" +
				"	 * Valid value: {@value E#VALID}\n" +
				"	 * @param x the object\n" +
				"	 * @return String\n" +
				"	 */\n" +
				"	public String val(Object x) { return x.toString(); }\n" +
				"  }\n" +
				"}\n" +
				"}\n"
		}
	);
}

// External javadoc references to enum
public void _NAtest043() {
	this.runConformTest(
		new String[] {
			"test/E.java",
			"package test;\n" +
				"public enum E { TEST, VALID }\n",
			"test/X.java",
			"import static test.E.TEST;\n" +
				"	/**\n" +
				"	 * @see test.E\n" +
				"	 * @see test.E#VALID\n" +
				"	 * @see #TEST\n" +
				"	 */\n" +
				"public class X {}\n"
		}
	);
}
public void _NAtest044() {
	this.runConformTest(
		new String[] {
			"test/E.java",
			"package test;\n" +
				"public enum E { TEST, VALID }\n",
			"test/X.java",
			"import static test.E.TEST;\n" +
				"	/**\n" +
				"	 * Valid value = {@value test.E#VALID}\n" +
				"	 * Test value = {@value #TEST}\n" +
				"	 */\n" +
				"public class X {}\n"
		}
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78321
 */
public void test045() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    enum Y {\n" +
			"      FIRST,\n" +
			"      SECOND,\n" +
			"      THIRD;\n" +
			"\n" +
			"      static {\n" +
			"        for (Y t : values()) {\n" +
			"          System.out.print(t.name());\n" +
			"        }\n" +
			"      }\n" +
			"\n" +
			"      Y() {}\n" +
			"      static void foo(){}\n" +
			"\n" +
			"     }\n" +
			"     Y.foo();\n" + // trigger the static block with a static method invocation
			"   }\n" +
			"}"
		},
		"FIRSTSECONDTHIRD"
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78464
 */
public void test046() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    enum Y{\n" +
			"  a(1);\n" +
			"  Y(int i) {\n" +
			"  }\n" +
			"  }\n" +
			"  }\n" +
			"}"
		},
		""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78914
 */
public void test047() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    enum Y{ \n" +
			"	;\n" +
			"	Y() {\n" +
			"		super();\n" +
			"	}\n" +
			"  }\n" +
			"   }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	super();\n" +
		"	^^^^^^^^\n" +
		"Cannot invoke super constructor from enum constructor Y()\n" +
		"----------\n"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77211
 */
public void test048() {
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    enum StopLight {\n" +
				"    RED{\n" +
			"        public StopLight next(){ return GREEN; }\n" +
			"    },\n" +
			"    GREEN{\n" +
			"        public StopLight next(){ return YELLOW; }\n" +
			"    },\n" +
			"    YELLOW{\n" +
			"        public StopLight next(){ return RED; }\n" +
			"    };\n" +
			"\n" +
			"   public abstract StopLight next();\n" +
			"   }\n" +
			"   }\n" +
			"}"
		},
		""
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78915
 */
public void test049() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    public abstract enum Y {}\n" +
			"   }\n" +
			"}"
		},
	"----------\n" +
	"1. ERROR in X.java (at line 3)\n" +
	"	public abstract enum Y {}\n" +
	"	                     ^\n" +
	"Illegal modifier for local enum Y; no explicit modifier is permitted\n" +
	"----------\n"
	);
}

public void test050() {
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"     enum Y {}\n" +
				"   }\n" +
				"}"
		},
		""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78914 - variation
 */
public void test051() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"     enum Y {\n" +
			"	BLEU (0) {\n" +
			"	}\n" +
			"	;\n" +
			"	Y() {\n" +
			"		this(0);\n" +
			"	}\n" +
			"	Y(int i) {\n" +
			"	}\n" +
			"   }\n" +
			"   }\n" +
			"}\n"
		},
		""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916
 */
public void test052() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"     enum Y {\n" +
			"	A\n" +
			"	;\n" +
			"	\n" +
			"	public abstract void foo();\n" +
			"   }\n" +
			"   }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	A\n" +
		"	^\n" +
		"The enum constant A must implement the abstract method foo()\n" +
		"----------\n"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916 - variation
 */
public void test053() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"     enum Y {\n" +
			"	A () { public void foo() {} }\n" +
			"	;\n" +
			"	\n" +
			"	public abstract void foo();\n" +
			"   }\n" +
			"   }\n" +
			"}\n"
		},
		""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916 - variation
 */
public void test054() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"     enum Y {\n" +
			"	A() {}\n" +
			"	;\n" +
			"	\n" +
			"	public abstract void foo();\n" +
			"   }\n" +
			"   }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	A() {}\n" +
		"	^\n" +
		"The enum constant A must implement the abstract method foo()\n" +
		"----------\n"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916 - variation
 */
public void test055() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"     enum Y {\n" +
			"	;\n" +
			"	\n" +
			"	public abstract void foo();\n" +
			"   }\n" +
			"   }\n" +
			"}\n"
		},
	"----------\n" +
	"1. ERROR in X.java (at line 6)\n" +
	"	public abstract void foo();\n" +
	"	                     ^^^^^\n" +
	"The enum Y can only define the abstract method foo() if it also defines enum constants with corresponding implementations\n" +
	"----------\n"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78914 - variation
 */
public void test056() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"     enum Y {\n" +
			"    PLUS {\n" +
			"        double eval(double x, double y) { return x + y; }\n" +
			"    };\n" +
			"\n" +
			"    // Perform the arithmetic X represented by this constant\n" +
			"    abstract double eval(double x, double y);\n" +
			"   }\n" +
			"   }\n" +
			"}"
		},
		""
	);
	String expectedOutput =
			"// Signature: Ljava/lang/Enum<LX$1Y;>;\n" +
			"abstract static enum X$1Y {\n" ;

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1Y.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77430
 */
public void test057() {
	this.runConformTest(
		new String[] {
			"Enum2.java",
			"public class Enum2 {\n" +
			"    public static void main(String[] args) {\n" +
			"    	enum Color { RED, GREEN };\n" +
			"        Color c= Color.GREEN;\n" +
			"        switch (c) {\n" +
			"        case RED:\n" +
			"            System.out.println(Color.RED);\n" +
			"            break;\n" +
			"        case GREEN:\n" +
			"            System.out.println(c);\n" +
			"            break;\n" +
			"        default: // nop\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"GREEN"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77430 - variation
 */
public void test058() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class A {\n" +
			"	public static void main(String[] args) {\n" +
			"	enum X { a }\n" +
			"	class B {\n" +
			"	 void _test(X x, int a) {\n" +
			"		if (x == a) a++; // incomparable types: X and int\n" +
			"		switch(x) {\n" +
			"			case a : System.out.println(a); // prints \'9\'\n" +
			"           default: // nop\n" +
			"		}\n" +
			"	}\n" +
			"	 void _test2(X x, final int aa) {\n" +
			"		switch(x) {\n" +
			"			case aa : // unqualified enum constant error\n" +
			"				System.out.println(a); // cannot find a\n" +
			"           default: // nop\n" +
			"		}\n" +
			"	}\n" +
			"	}\n" +
			"		new B()._test(X.a, 9);\n" +
			"		new B()._test2(X.a, 3);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (x == a) a++; // incomparable types: X and int\n" +
		"	    ^^^^^^\n" +
		"Incompatible operand types X and int\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	case aa : // unqualified enum constant error\n" +
		"	     ^^\n" +
		"aa cannot be resolved or is not a field\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 15)\n" +
		"	System.out.println(a); // cannot find a\n" +
		"	                   ^\n" +
		"a cannot be resolved to a variable\n" +
		"----------\n"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=81262
 */
public void test059() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"     enum Y {\n" +
			"	  	MONDAY {\n" +
			"			public void foo() {\n" +
			"		}\n" +
			"	  };\n" +
			"		private Y() {}\n" +
			"		public static void main(String[] ags) {\n" +
			"	  		System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
			"	  }\n" +
			"	  Y.main(args);\n" +
			"	}\n" +
			"}\n"
		},
		"SUCCESS");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=81589
 */
public void _NAtest060() {
	this.runNegativeTest(
		new String[] {
			"com/flarion/test/a/MyEnum.java",
			"package com.flarion.test.a;\n" +
			"public enum MyEnum {\n" +
			"\n" +
			"    First, Second;\n" +
			"    \n" +
			"}\n",
			"com/flarion/test/b/MyClass.java",
			"package com.flarion.test.b;\n" +
			"import com.flarion.test.a.MyEnum;\n" +
			"import static com.flarion.test.a.MyEnum.First;\n" +
			"import static com.flarion.test.a.MyEnum.Second;\n" +
			"public class MyClass {\n" +
			"\n" +
			"    public void myMethod() {\n" +
			"        MyEnum e = MyEnum.First;\n" +
			"        switch (e) {\n" +
			"        case First:\n" +
			"            break;\n" +
			"        case Second:\n" +
			"            break;\n" +
			"        default: // nop\n" +
			"        }\n" +
			"        throw new Exception();\n" + // fake error to cause dump of unused import warnings
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in com\\flarion\\test\\b\\MyClass.java (at line 3)\n" +
		"	import static com.flarion.test.a.MyEnum.First;\n" +
		"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The import com.flarion.test.a.MyEnum.First is never used\n" +
		"----------\n" +
		"2. WARNING in com\\flarion\\test\\b\\MyClass.java (at line 4)\n" +
		"	import static com.flarion.test.a.MyEnum.Second;\n" +
		"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The import com.flarion.test.a.MyEnum.Second is never used\n" +
		"----------\n" +
		"3. ERROR in com\\flarion\\test\\b\\MyClass.java (at line 16)\n" +
		"	throw new Exception();\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unhandled exception type Exception\n" +
		"----------\n");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82217
 */
public void test061() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"\n" +
			"class A {\n" +
			"	public void foo() {\n" +
			"		 enum X {\n" +
			"			A, B, C;\n" +
			"			public static final X D = null;\n" +
			"		}\n" +
			"		X x = X.A;\n" +
			"		switch (x) {\n" +
			"			case D:\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 9)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The enum constant A needs a corresponding case label in this enum switch on X\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 9)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The enum constant B needs a corresponding case label in this enum switch on X\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 9)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The enum constant C needs a corresponding case label in this enum switch on X\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 10)\n" +
		"	case D:\n" +
		"	     ^\n" +
		"The field X.D cannot be referenced from an enum case label; only enum constants can be used in enum switch\n" +
		"----------\n");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82217 - variation with qualified name
 */
public void test062() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"\n" +
			"class A {\n" +
			"	private void foo() {\n" +
			"		enum X {\n" +
			"			A, B, C;\n" +
			"			public static final X D = null;\n" +
			"		}\n" +
			"		X x = X.A;\n" +
			"		switch (x) {\n" +
			"			case X.D:\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 9)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The switch over the enum type X should have a default case\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 9)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The enum constant A needs a corresponding case label in this enum switch on X\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 9)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The enum constant B needs a corresponding case label in this enum switch on X\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 9)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The enum constant C needs a corresponding case label in this enum switch on X\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 10)\n" +
		"	case X.D:\n" +
		"	       ^\n" +
		"The field X.D cannot be referenced from an enum case label; only enum constants can be used in enum switch\n" +

		"----------\n",
		null, // classlibs
		true, // flush
		options);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=81945
 */
public void test063() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"  public static void main(String[] args) {\n" +
			"  enum Option { ALPHA, BRAVO  };\n" +
			"    Option item = Option.ALPHA;\n" +
			"    switch (item) {\n" +
			"    case ALPHA:      break;\n" +
			"    case BRAVO:      break;\n" +
			"    default:         break;\n" +
			"    }\n" +
			"  }\n" +
			"}\n",
		},
		"");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82590
 */
public void test064() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"interface B {\n" +
			"	public void _test();\n" +
			"	\n" +
			"	}\n" +
			"	 enum Y implements B {\n" +
			"\n" +
			"		C1 {\n" +
			"			public void _test() {};\n" +
			"		},\n" +
			"		C2 {\n" +
			"			public void _test() {};\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	}\n" +
			"}\n",
		},
		"");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83847
 */
public void test065() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 enum Y  {\n" +
			" 		A;\n" +
			"  		private void foo() {\n" +
			"    		Y e= new Y() {\n" +
			"    		};\n" +
			"  		}\n" +
			"  	  }\n" +
			"  }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	Y e= new Y() {\n" +
		"	         ^\n" +
		"Cannot instantiate the type Y\n" +
		"----------\n");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83860
 */
public void test066() {
    this.runConformTest(
        new String[] {
            "X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 enum Y  {\n" +
            "    	SUCCESS (0) {};\n" +
            "    	private Y(int i) {}\n" +
            "    	public static void main(String[] args) {\n" +
            "       	for (Y y : values()) {\n" +
            "           	System.out.print(y);\n" +
            "       	}\n" +
            "   	}\n" +
            "   }\n" +
            "   Y.main(args);\n" +
            "  }\n" +
            "}",
        },
        "SUCCESS");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83219
 */
public void test067() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 enum Y  {\n" +
            "    	ONE, TWO, THREE;\n" +
            "    	abstract int getSquare();\n" +
            "    	abstract int getSquare();\n" +
            "    }\n" +
            "  }\n" +
            "}",
        },
        "----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	ONE, TWO, THREE;\n" +
		"	^^^\n" +
		"The enum constant ONE must implement the abstract method getSquare()\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	ONE, TWO, THREE;\n" +
		"	     ^^^\n" +
		"The enum constant TWO must implement the abstract method getSquare()\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	ONE, TWO, THREE;\n" +
		"	          ^^^^^\n" +
		"The enum constant THREE must implement the abstract method getSquare()\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 5)\n" +
		"	abstract int getSquare();\n" +
		"	             ^^^^^^^^^^^\n" +
		"Duplicate method getSquare() in type Y\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 6)\n" +
		"	abstract int getSquare();\n" +
		"	             ^^^^^^^^^^^\n" +
		"Duplicate method getSquare() in type Y\n" +
		"----------\n");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83648
 */
public void test068() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 enum Y  {\n" +
            "    	A(1, 3), B(1, 3), C(1, 3) { }\n" +
            "   	;\n" +
            "    	public Y(int i, int j) { }\n" +
            "    }\n" +
            "  }\n" +
            "}",
        },
        "----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	public Y(int i, int j) { }\n" +
		"	       ^^^^^^^^^^^^^^^\n" +
		"Illegal modifier for the enum constructor; only private is permitted.\n" +
		"----------\n");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83648
 */
public void test069() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 enum Y  {\n" +
            "    	A(1, 3), B(1, 3), C(1, 3) { }\n" +
            "   	;\n" +
            "    	protected Y(int i, int j) { }\n" +
            "    }\n" +
            "  }\n" +
            "}",
        },
        "----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	protected Y(int i, int j) { }\n" +
		"	          ^^^^^^^^^^^^^^^\n" +
		"Illegal modifier for the enum constructor; only private is permitted.\n" +
		"----------\n");
}

public void test070() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 enum Y  {\n" +
			"    	PLUS {\n" +
			"        	double eval(double x, double y) { return x + y; }\n" +
			"    	};\n" +
			"\n" +
			"    	// Perform the arithmetic X represented by this constant\n" +
			"    	abstract double eval(double x, double y);\n" +
            "    }\n" +
            "  }\n" +
			"}"
		},
		""
	);
	String expectedOutput =
			"  // Method descriptor #18 (Ljava/lang/String;I)V\n" +
			"  // Stack: 3, Locals: 3\n" +
			"  private X$1Y(java.lang.String arg0, int arg1);\n" +
			"    0  aload_0 [this]\n" +
			"    1  aload_1 [arg0]\n" +
			"    2  iload_2 [arg1]\n" +
			"    3  invokespecial java.lang.Enum(java.lang.String, int) [25]\n" +
			"    6  return\n";

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1Y.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test071() {
	this.runConformTest( // no methods to implement
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    enum X1 implements I {\n" +
			"	;\n" +
            "    }\n" +
            "  }\n" +
			"}\n" +
			"interface I {}\n"
		},
		""
	);
	this.runConformTest( // no methods to implement with constant
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	enum X1a implements I {\n" +
			"		A;\n" +
            "    }\n" +
            "  }\n" +
			"}\n" +
			"interface I {}\n"
		},
		""
	);
	this.runConformTest( // no methods to implement with constant body
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"  	enum X1b implements I {\n" +
			"		A() { void random() {} };\n" +
			"	}\n" +
            "  }\n" +
			"}\n" +
			"interface I {}\n"
		},
		""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test072() {
	this.runConformTest( // implement inherited method
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"  	enum X2 implements I {\n" +
			"	;\n" +
			"		public void _test() {}\n" +
			"	}\n" +
            "  }\n" +
			"}\n" +
			"interface I { void _test(); }\n"
		},
		""
	);
	this.runConformTest( // implement inherited method with constant
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 enum X2a implements I {\n" +
			"	 	A;\n" +
			"	 public void _test() {}\n" +
			"	}\n" +
            "  }\n" +
			"}\n" +
			"interface I { void _test(); }\n"
		},
		""
	);
	this.runConformTest( // implement inherited method with constant body
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"		enum X2b implements I {\n" +
			"			A() { public void _test() {} };\n" +
			"		public void _test() {}\n" +
			"	}\n" +
            "  }\n" +
			"}\n" +
			"interface I { void _test(); }\n"
		},
		""
	);
	this.runConformTest( // implement inherited method with random constant body
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			" 	 enum X2c implements I {\n" +
			"		A() { void random() {} };\n" +
			"		public void _test() {}\n" +
			"	}\n" +
            "  }\n" +
			"}\n" +
			"interface I { void _test(); }\n"
		},
		""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test073() {
	this.runNegativeTest( // implement inherited method but as abstract
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"		enum X3 implements I {\n" +
			"	;\n" +
			"		public abstract void _test();\n" +
			"	}\n" +
            "  }\n" +
			"}\n" +
			"interface I { void _test(); }\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	public abstract void _test();\n" +
		"	                     ^^^^^^^\n" +
		"The enum X3 can only define the abstract method _test() if it also defines enum constants with corresponding implementations\n" +
		"----------\n"
		// X3 is not abstract and does not override abstract method test() in X3
	);
	this.runNegativeTest( // implement inherited method as abstract with constant
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"  	enum X3a implements I {\n" +
			"		A;\n" +
			"		public abstract void _test();\n" +
			"	}\n" +
            "  }\n" +
			"}\n" +
			"interface I { void _test(); }\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	A;\n" +
		"	^\n" +
		"The enum constant A must implement the abstract method _test()\n" +
		"----------\n"
		// X3a is not abstract and does not override abstract method test() in X3a
	);
	this.runConformTest( // implement inherited method as abstract with constant body
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	enum X3b implements I {\n" +
			"		A() { public void _test() {} };\n" +
			"		public abstract void _test();\n" +
			"	}\n" +
            "  }\n" +
			"}\n" +
			"interface I { void _test(); }\n"
		},
		""
	);
	this.runNegativeTest( // implement inherited method as abstract with random constant body
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			" 	 enum X3c implements I {\n" +
			"		A() { void random() {} };\n" +
			"	 public abstract void _test();\n" +
			"	}\n" +
            "  }\n" +
			"}\n" +
			"interface I { void _test(); }\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	A() { void random() {} };\n" +
		"	^\n" +
		"The enum constant A must implement the abstract method _test()\n" +
		"----------\n"
		// <anonymous X3c$1> is not abstract and does not override abstract method test() in X3c
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test074() {
	this.runNegativeTest( // define abstract method
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"  	enum X4 {\n" +
			"	;\n" +
			"		public abstract void _test();\n" +
			"	}\n" +
            "  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	public abstract void _test();\n" +
		"	                     ^^^^^^^\n" +
		"The enum X4 can only define the abstract method _test() if it also defines enum constants with corresponding implementations\n" +
		"----------\n"
		// X4 is not abstract and does not override abstract method test() in X4
	);
	this.runNegativeTest( // define abstract method with constant
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			" 	enum X4a {\n" +
			"		A;\n" +
			"		public abstract void _test();\n" +
			"	}\n" +
            "  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	A;\n" +
		"	^\n" +
		"The enum constant A must implement the abstract method _test()\n" +
		"----------\n"
		// X4a is not abstract and does not override abstract method test() in X4a
	);
	this.runConformTest( // define abstract method with constant body
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	enum X4b {\n" +
			"		A() { public void _test() {} };\n" +
			"		public abstract void _test();\n" +
			"	}\n" +
            "  }\n" +
			"}\n"
		},
		""
	);
	this.runNegativeTest( // define abstract method with random constant body
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"		enum X4c {\n" +
			"			A() { void random() {} };\n" +
			"		public abstract void _test();\n" +
			"	}\n" +
            "  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	A() { void random() {} };\n" +
		"	^\n" +
		"The enum constant A must implement the abstract method _test()\n" +
		"----------\n"
		// <anonymous X4c$1> is not abstract and does not override abstract method test() in X4c
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void testNPE075() {
	this.runNegativeTest( // do not implement inherited method
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"  	enum X5 implements I {\n" +
			"	;\n" +
			"	}\n" +
            "  }\n" +
			"}\n" +
			"interface I { void _test(); }\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	enum X5 implements I {\n" +
		"	     ^^\n" +
		"The type X5 must implement the inherited abstract method I._test()\n" +
		"----------\n"
		// X5 is not abstract and does not override abstract method test() in I
	);
	this.runNegativeTest( // do not implement inherited method & have constant with no body
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	enum X5a implements I {\n" +
			"		A;\n" +
			"	}\n" +
            "  }\n" +
			"}\n" +
			"interface I { void _test(); }\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	enum X5a implements I {\n" +
		"	     ^^^\n" +
		"The type X5a must implement the inherited abstract method I._test()\n" +
		"----------\n"
		// X5a is not abstract and does not override abstract method test() in I
	);
	this.runConformTest( // do not implement inherited method & have constant with body
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"  	enum X5b implements I {\n" +
			"		A() { public void _test() {} };\n" +
			"	;\n" +
			"	}\n" +
            "  }\n" +
			"}\n" +
			"interface I { void _test(); }\n"
		},
		""
	);
	this.runNegativeTest( // do not implement inherited method & have constant with random body
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	enum X5c implements I {\n" +
			"		A() { void random() {} };\n" +
			"		;\n" +
			"	private X5c() {}\n" +
			"	}\n" +
            "  }\n" +
			"}\n" +
			"interface I { void _test(); }\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	A() { void random() {} };\n" +
		"	^\n" +
		"The enum constant A must implement the abstract method _test()\n" +
		"----------\n"
		// <anonymous X5c$1> is not abstract and does not override abstract method test() in I
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
public void test076() { // bridge method needed
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"enum E implements I {\n" +
			"	A;\n" +
			"	public E foo() {\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n" +
			"	public static void main(String[] args) { ((I) E.A).foo(); }\n" +
			"}\n" +
			"interface I { I foo(); }\n"
		},
		"SUCCESS"
	);
}

public void test077() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"enum E {\n" +
			"	A {\n" +
			"		void bar() {\n" +
			"			new M();\n" +
			"		}\n" +
			"	};\n" +
			"	abstract void bar();\n" +
			"	\n" +
			"	class M {\n" +
			"		M() {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"		E.A.bar();\n" +
			"	}\n" +
			"}\n"
		},
		"SUCCESS"
	);
}

public void test078() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"enum E {\n" +
			"	A {\n" +
			"		void bar() {\n" +
			"			new X(){\n" +
			"				void baz() {\n" +
			"					new M();\n" +
			"				}\n" +
			"			}.baz();\n" +
			"		}\n" +
			"	};\n" +
			"	abstract void bar();\n" +
			"	\n" +
			"	class M {\n" +
			"		M() {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"		E.A.bar();\n" +
			"	}\n" +
			"}\n"
		},
		"SUCCESS"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85397
public void test079() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"     enum Y {\n" +
			"		A, B;\n" +
			"		private strictfp Y() {}\n" +
			"	  }\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	private strictfp Y() {}\n" +
		"	                 ^^^\n" +
		"Illegal modifier for the constructor in type Y; only public, protected & private are permitted\n" +
		"----------\n"
	);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		strictfp enum Y {\n" +
			"		A, B;\n" +
			"		private Y() {}\n" +
			"	  }\n" +
			"	}\n" +
			"}\n"
		},
		""
	);

	String[] expectedOutputs = new String[] {
		"  private strictfp X$1Y(java.lang.String arg0, int arg1);\n",
		"  public static strictfp new X(){}[] values();\n",
		"  public static strictfp new X(){} valueOf(java.lang.String arg0);\n"
	};

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1Y.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	for (int i = 0, max = expectedOutputs.length; i < max; i++) {
		String expectedOutput = expectedOutputs[i];
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=87064
public void test080() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface TestInterface {\n" +
			"	int test();\n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"   	enum Y implements TestInterface {\n" +
			"		TEST {\n" +
			"			public int test() {\n" +
			"				return 42;\n" +
			"			}\n" +
			"		},\n" +
			"		ENUM {\n" +
			"			public int test() {\n" +
			"				return 37;\n" +
			"			}\n" +
			"		};\n" +
			"	  }\n" +
			"	}\n" +
			"} \n"
		},
		""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87818
public void test081() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {}\n" +
			"	void foo() {\n" +
			"		enum E {}\n" +
			"	}\n" +
			"}"
		},
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88223
public void test082() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"	class Y {\n" +
			"		enum E {}\n" +
			"	}\n" +
			"	}\n" +
			"}"
		},
		"");
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"	 class Y {\n" +
			"		enum E {}\n" +
			"	}\n" +
			"}\n" +
			"}"
		},
		"");
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {}\n" +
			"	void foo() {\n" +
			"		class Local {\n" +
			"			enum E {}\n" +
			"		}\n" +
			"	}\n" +
			"}"
		},
		"");
}


// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87998 - check no emulation warning
public void test083() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			" 		enum Y {\n" +
			"	INPUT {\n" +
			"		@Override\n" +
			"		public Y getReverse() {\n" +
			"			return OUTPUT;\n" +
			"		}\n" +
			"	},\n" +
			"	OUTPUT {\n" +
			"		@Override\n" +
			"		public Y getReverse() {\n" +
			"			return INPUT;\n" +
			"		}\n" +
			"	},\n" +
			"	INOUT {\n" +
			"		@Override\n" +
			"		public Y getReverse() {\n" +
			"			return INOUT;\n" +
			"		}\n" +
			"	};\n" +
			"	Y(){}\n" +
			"  Zork z;\n" +
			"	public abstract Y getReverse();\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 23)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87998 - check private constructor generation
public void test084() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			" 		enum Y {\n" +
			"	INPUT {\n" +
			"		@Override\n" +
			"		public Y getReverse() {\n" +
			"			return OUTPUT;\n" +
			"		}\n" +
			"	},\n" +
			"	OUTPUT {\n" +
			"		@Override\n" +
			"		public Y getReverse() {\n" +
			"			return INPUT;\n" +
			"		}\n" +
			"	},\n" +
			"	INOUT {\n" +
			"		@Override\n" +
			"		public Y getReverse() {\n" +
			"			return INOUT;\n" +
			"		}\n" +
			"	};\n" +
			"	Y(){}\n" +
			"	public abstract Y getReverse();\n" +
			"		}\n" +
			"		}\n" +
			"}\n",
		},
		"");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1Y.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"  // Method descriptor #20 (Ljava/lang/String;I)V\n" +
		"  // Stack: 3, Locals: 3\n" +
		"  private X$1Y(java.lang.String arg0, int arg1);\n";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88625
public void test085() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"	enum Test1 {\n" +
			"		test11, test12\n" +
			"	};\n" +
			"	enum Test2 {\n" +
			"		test21, test22\n" +
			"	};\n" +
			"\n" +
			"  class Y {\n" +
			"	void foo1(Test1 t1, Test2 t2) {\n" +
			"		boolean b = t1 == t2;\n" +
			"	}\n" +
			"	void foo2(Test1 t1, Object t2) {\n" +
			"		boolean b = t1 == t2;\n" +
			"	}\n" +
			"	void foo3(Test1 t1, Enum t2) {\n" +
			"		boolean b = t1 == t2;\n" +
			"	}\n" +
			"	public void foo() {\n" +
			"		boolean booleanTest = (Test1.test11 == Test2.test22);\n" +
			"	}\n" +
			"	}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	boolean b = t1 == t2;\n" +
		"	            ^^^^^^^^\n" +
		"Incompatible operand types Test1 and Test2\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 17)\n" +
		"	void foo3(Test1 t1, Enum t2) {\n" +
		"	                    ^^^^\n" +
		"Enum is a raw type. References to generic type Enum<E> should be parameterized\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 21)\n" +
		"	boolean booleanTest = (Test1.test11 == Test2.test22);\n" +
		"	                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Incompatible operand types Test1 and Test2\n" +
		"----------\n");
}
public void test086() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"	enum Test1 {\n" +
			"		V;\n" +
			"		static int foo = 0;\n" +
			"	}\n" +
			"	}\n" +
			"}\n",
		},
		"");
}
public void _test087_more_meaningful_error_msg_required() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"	enum Test1 {\n" +
			"		V;\n" +
			"		interface Foo {}\n" +
			"	}\n" +
			"	}\n" +
			"}\n",
		},
		"");
}
public void test088() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"\n" +
			"	enum Test1 {\n" +
			"		V;\n" +
			"	}\n" +
			"	}\n" +
			"	Object foo() {\n" +
			"		return this;\n" +
			"	}\n" +
			"\n" +
			"}\n",
		},
		"");
}
public void test089() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"\n" +
			"	enum Test1 {\n" +
			"		V;\n" +
			"		protected final Test1 clone() { return V; }\n" +
			"	}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	protected final Test1 clone() { return V; }\n" +
		"	                      ^^^^^^^\n" +
		"Cannot override the final method from Enum<Test1>\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 6)\n" +
		"	protected final Test1 clone() { return V; }\n" +
		"	                      ^^^^^^^\n" +
		"The method clone() of type Test1 should be tagged with @Override since it actually overrides a superclass method\n" +
		"----------\n");
}
public void test090() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"	enum Test1 {\n" +
			"		V;\n" +
			"		public Test1 foo() { return V; }\n" +
			"	}\n" +
			"	}\n" +
			"	Zork z;\n" +
			"}\n",
			"java/lang/Object.java",
			"package java.lang;\n" +
			"public class Object {\n" +
			"	public Object foo() { return this; }\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 6)\n" +
		"	public Test1 foo() { return V; }\n" +
		"	             ^^^^^\n" +
		"The method foo() of type Test1 should be tagged with @Override since it actually overrides a superclass method\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
public void test091() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"	enum Test1 {\n" +
			"		V;\n" +
			"		void foo() {}\n" +
			"	}\n" +
			"	class Member<E extends Test1> {\n" +
			"		void bar(E e) {\n" +
			"			e.foo();\n" +
			"		}\n" +
			"	}\n" +
			"	}\n" +
			"}\n",
		},
		"");
}
public void test092() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"	enum Test1 {\n" +
			"		V;\n" +
			"		void foo() {}\n" +
			"	}\n" +
			"	class Member<E extends Object & Test1> {\n" +
			"		void bar(E e) {\n" +
			"			e.foo();\n" +
			"		}\n" +
			"	}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	class Member<E extends Object & Test1> {\n" +
		"	                                ^^^^^\n" +
		"The type Test1 is not an interface; it cannot be specified as a bounded parameter\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	e.foo();\n" +
		"	  ^^^\n" +
		"The method foo() is undefined for the type E\n" +
		"----------\n");
}
// check wildcard can extend Enum superclass
public void test093() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"	enum Test1 {\n" +
			"		V;\n" +
			"		void foo() {}\n" +
			"	}\n" +
			"	class Member<E extends Test1> {\n" +
			"		E e;\n" +
			"		void bar(Member<? extends Test1> me) {\n" +
			"		}\n" +
			"	}\n" +
			"	}\n" +
			"}\n",
		},
		"");
}
// check super bit is set
public void test094() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		enum Y {}\n" +
			"	}\n" +
			"}\n",
		},
		"");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1Y.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"// Signature: Ljava/lang/Enum<LX$1Y;>;\n" +
		"static final enum X$1Y {\n" +
		"  \n" +
		"  // Field descriptor #6 [LX$1Y;\n" +
		"  private static final synthetic X$1Y[] ENUM$VALUES;\n" +
		"  \n" +
		"  // Method descriptor #8 ()V\n" +
		"  // Stack: 1, Locals: 0\n" +
		"  static {};\n" +
		"    0  iconst_0\n" +
		"    1  anewarray X$1Y [1]\n" +
		"    4  putstatic X$1Y.ENUM$VALUES : new X(){}[] [10]\n" +
		"    7  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"  \n" +
		"  // Method descriptor #15 (Ljava/lang/String;I)V\n" +
		"  // Stack: 3, Locals: 3\n" +
		"  private X$1Y(java.lang.String arg0, int arg1);\n" +
		"    0  aload_0 [this]\n" +
		"    1  aload_1 [arg0]\n" +
		"    2  iload_2 [arg1]\n" +
		"    3  invokespecial java.lang.Enum(java.lang.String, int) [16]\n" +
		"    6  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 7] local: this index: 0 type: new X(){}\n" +
		"  \n" +
		"  // Method descriptor #21 ()[LX$1Y;\n" +
		"  // Stack: 5, Locals: 3\n" +
		"  public static new X(){}[] values();\n" +
		"     0  getstatic X$1Y.ENUM$VALUES : new X(){}[] [10]\n" +
		"     3  dup\n" +
		"     4  astore_0\n" +
		"     5  iconst_0\n" +
		"     6  aload_0\n" +
		"     7  arraylength\n" +
		"     8  dup\n" +
		"     9  istore_1\n" +
		"    10  anewarray X$1Y [1]\n" +
		"    13  dup\n" +
		"    14  astore_2\n" +
		"    15  iconst_0\n" +
		"    16  iload_1\n" +
		"    17  invokestatic java.lang.System.arraycopy(java.lang.Object, int, java.lang.Object, int, int) : void [22]\n" +
		"    20  aload_2\n" +
		"    21  areturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 1]\n" +
		"  \n" +
		"  // Method descriptor #29 (Ljava/lang/String;)LX$1Y;\n" +
		"  // Stack: 2, Locals: 1\n" +
		"  public static new X(){} valueOf(java.lang.String arg0);\n" +
		"     0  ldc <Class X$1Y> [1]\n" +
		"     2  aload_0 [arg0]\n" +
		"     3  invokestatic java.lang.Enum.valueOf(java.lang.Class, java.lang.String) : java.lang.Enum [30]\n" +
		"     6  checkcast X$1Y [1]\n" +
		"     9  areturn\n" ;


	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}
public void testNPE095() { // check missing abstract cases from multiple interfaces
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		enum Y implements I, J { \n" +
			"			ROUGE;\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"interface I { void foo(); }\n" +
			"interface J { void foo(); }\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	enum Y implements I, J { \n" +
		"	     ^\n" +
		"The type Y must implement the inherited abstract method J.foo()\n" +
		"----------\n");
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		enum Y implements I, J { \n" +
			"			ROUGE;\n" +
			"			public void foo() {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"interface I { void foo(int i); }\n" +
			"interface J { void foo(); }\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	enum Y implements I, J { \n" +
		"	     ^\n" +
		"The type Y must implement the inherited abstract method I.foo(int)\n" +
		"----------\n");
}
public void testNPE096() { // check for raw vs. parameterized parameter types
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		enum Y implements I { \n" +
			"			ROUGE;\n" +
			"			public void foo(A a) {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"interface I { void foo(A<String> a); }\n" +
			"class A<T> {}\n"
		},
		"");
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		 enum Y implements I { \n" +
			"			ROUGE { public void foo(A a) {} }\n" +
			"			;\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"interface I { void foo(A<String> a); }\n" +
			"class A<T> {}\n"
		},
		"");
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		 enum Y implements I { \n" +
			"			ROUGE;\n" +
			"			public void foo(A<String> a) {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"interface I { void foo(A a); }\n" +
			"class A<T> {}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	enum Y implements I { \n" +
		"	     ^\n" +
		"The type Y must implement the inherited abstract method I.foo(A)\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	public void foo(A<String> a) {}\n" +
		"	            ^^^^^^^^^^^^^^^^\n" +
		"Name clash: The method foo(A<String>) of type Y has the same erasure as foo(A) of type I but does not override it\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 9)\n" +
		"	interface I { void foo(A a); }\n" +
		"	                       ^\n" +
		"A is a raw type. References to generic type A<T> should be parameterized\n" +
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89982
public void test097() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"public class E {\n" +
			"	enum Numbers { ONE, TWO, THREE }\n" +
			"	static final String BLANK = \"    \";\n" +
			"	void foo() {\n" +
			"		/**\n" +
			"		 * Enumeration of some basic colors.\n" +
			"		 */\n" +
			"		enum Colors {\n" +
			"			BLACK,\n" +
			"			WHITE,\n" +
			"			RED  \n" +
			"		}\n" +
			"		Colors color = Colors.BLACK;\n" +
			"		switch (color) {\n" +
			"			case BLUE:\n" +
			"			case RED:\n" +
			"				break;\n" +
			"			default: // nop\n" +
			"		} \n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in E.java (at line 15)\n" +
		"	case BLUE:\n" +
		"	     ^^^^\n" +
		"BLUE cannot be resolved or is not a field\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89982 - variation
public void test098() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"public class E {\n" +
			"	enum Numbers { ONE, TWO, THREE }\n" +
			"	static final String BLANK = \"    \";\n" +
			"	void foo() {\n" +
			"		/**\n" +
			"		 * Enumeration of some basic colors.\n" +
			"		 */\n" +
			"		enum Colors {\n" +
			"			BLACK,\n" +
			"			WHITE,\n" +
			"			RED;  \n" +
			"  			Zork z;\n" +
			"		}\n"+
			"		Colors color = Colors.BLACK;\n" +
			"		switch (color) {\n" +
			"		} \n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in E.java (at line 12)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"2. WARNING in E.java (at line 15)\n" +
		"	switch (color) {\n" +
		"	        ^^^^^\n" +
		"The enum constant BLACK needs a corresponding case label in this enum switch on Colors\n" +
		"----------\n" +
		"3. WARNING in E.java (at line 15)\n" +
		"	switch (color) {\n" +
		"	        ^^^^^\n" +
		"The enum constant RED needs a corresponding case label in this enum switch on Colors\n" +
		"----------\n" +
		"4. WARNING in E.java (at line 15)\n" +
		"	switch (color) {\n" +
		"	        ^^^^^\n" +
		"The enum constant WHITE needs a corresponding case label in this enum switch on Colors\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89274
public void _NAtest099() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class A<T> {\n" +
			"	public static void main(String[] args) {\n" +
			"	enum E {\n" +
			"		v1, v2;\n" +
			"	}\n" +
			"	public class X extends A<Integer> {\n" +
			"		void a(A.E e) {\n" +
			"			b(e); // no unchecked warning\n" +
			"		}\n" +
			"\n" +
			"		void b(E e) {\n" +
			"			A<Integer>.E e1 = e;\n" +
			"		}\n" +
			"	}\n"+
			"}\n" +
			"}\n" +
			"\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 13)\n" +
		"	A<Integer>.E e1 = e;\n" +
		"	^^^^^^^^^^^^\n" +
		"The member type A.E cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type A<Integer>\n" +
		"----------\n");
}
/* from JLS
"It is a compile-time error to reference a static field of an enum type
that is not a compile-time constant (15.28) from constructors, instance
initializer blocks, or instance variable initializer expressions of that
type.  It is a compile-time error for the constructors, instance initializer
blocks, or instance variable initializer expressions of an enum constant e1
to refer to itself or an enum constant of the same type that is declared to
the right of e1."
	*/
public void testNPE100() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		enum Y {\n" +
			"\n" +
			"			anEnumValue {\n" +
			"				private final Y thisOne = anEnumValue;\n" +
			"\n" +
			"				@Override String getMessage() {\n" +
			"					return \"Here is what thisOne gets assigned: \" + thisOne;\n" +
			"				}\n" +
			"			};\n" +
			"\n" +
			"			abstract String getMessage();\n" +
			"\n" +
			"			public static void main(String[] arguments) {\n" +
			"				System.out.println(anEnumValue.getMessage());\n" +
			"				System.out.println(\"SUCCESS\");\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	private final Y thisOne = anEnumValue;\n" +
		"	                          ^^^^^^^^^^^\n" +
		"Cannot refer to the static enum field Y.anEnumValue within an initializer\n" +
		"----------\n",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=91761
public void test101() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface Foo {\n" +
			"  public boolean bar();\n" +
			"}\n" +
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"enum BugDemo {\n" +
			"  CONSTANT(new Foo() {\n" +
			"    public boolean bar() {\n" +
			"      Zork z;\n" +
			"      return true;\n" +
			"    }\n" +
			"  });\n" +
			"  BugDemo(Foo foo) {\n" +
			"  }\n" +
			"  }\n" +
			"  }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90775
public void test102() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"import java.util.*;\n" +
			"\n" +
			"public class X <T> {\n" +
			"	public static void main(String[] args) {\n" +
			"	enum SomeEnum {\n" +
			"		A, B;\n" +
			"		static SomeEnum foo() {\n" +
			"			return null;\n" +
			"		}\n" +
			"	}\n" +
			"	Enum<SomeEnum> e = SomeEnum.A;\n" +
			"		\n" +
			"	Set<SomeEnum> set1 = EnumSet.of(SomeEnum.A);\n" +
			"	Set<SomeEnum> set2 = EnumSet.of(SomeEnum.foo());\n" +
			"	\n" +
			"	Foo<Bar> foo = null;\n" +
			"}\n" +
			"}\n" +
			"class Foo <U extends Foo<U>> {\n" +
			"}\n" +
			"class Bar extends Foo {\n" +
			"}\n",
        },
		"----------\n" +
		"1. ERROR in X.java (at line 16)\n" +
		"	Foo<Bar> foo = null;\n" +
		"	    ^^^\n" +
		"Bound mismatch: The type Bar is not a valid substitute for the bounded parameter <U extends Foo<U>> of the type Foo<U>\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 21)\n" +
		"	class Bar extends Foo {\n" +
		"	                  ^^^\n" +
		"Foo is a raw type. References to generic type Foo<U> should be parameterized\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=93396
public void test103() {
    this.runNegativeTest(
        new String[] {
            "BadEnum.java",
            "public class BadEnum {\n" +
            "  public interface EnumInterface<T extends Object> {\n" +
            "    public T getMethod();\n" +
            "  }\n" +
			"	public static void main(String[] args) {\n" +
            "   enum EnumClass implements EnumInterface<String> {\n" +
            "    ENUM1 { public String getMethod() { return \"ENUM1\";} },\n" +
            "    ENUM2 { public String getMethod() { return \"ENUM2\";} };\n" +
            "  }\n" +
            "}\n" +
            "}\n" +
            "}\n",
        },
        "----------\n" +
        "1. ERROR in BadEnum.java (at line 12)\n" +
        "	}\n" +
        "	^\n" +
        "Syntax error on token \"}\", delete this token\n" +
        "----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90215
public void _NA_test104() {
    this.runConformTest(
        new String[] {
            "p/Placeholder.java",
			"package p;\n" +
			"\n" +
			"public class Placeholder {\n" +
			"    public static void main(String... argv) {\n" +
			"        ClassWithBadEnum.EnumClass constant = ClassWithBadEnum.EnumClass.ENUM1;\n" + // forward ref
			"        ClassWithBadEnum.main(argv);\n" +
			"	}\n" +
			"}    \n" +
			"\n",
            "p/ClassWithBadEnum.java",
			"package p;\n" +
			"\n" +
			"public class ClassWithBadEnum {\n" +
			"	public interface EnumInterface<T extends Object> {\n" +
			"	    public T getMethod();\n" +
			"	}\n" +
			"\n" +
			"	public enum EnumClass implements EnumInterface<String> {\n" +
			"		ENUM1 { public String getMethod() { return \"ENUM1\";} },\n" +
			"		ENUM2 { public String getMethod() { return \"ENUM2\";} };\n" +
			"	}\n" +
			"	private EnumClass enumVar; \n" +
			"	public EnumClass getEnumVar() {\n" +
			"		return enumVar;\n" +
			"	}\n" +
			"	public void setEnumVar(EnumClass enumVar) {\n" +
			"		this.enumVar = enumVar;\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String... argv) {\n" +
			"		int a = 1;\n" +
			"		ClassWithBadEnum badEnum = new ClassWithBadEnum();\n" +
			"		badEnum.setEnumVar(ClassWithBadEnum.EnumClass.ENUM1);\n" +
			"		// Should fail if bug manifests itself because there will be two getInternalValue() methods\n" +
			"		// one returning an Object instead of a String\n" +
			"		String s3 = badEnum.getEnumVar().getMethod();\n" +
			"		System.out.println(s3);\n" +
			"	}\n" +
			"}  \n",
        },
        "ENUM1");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void _NA_test105() {
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"package pack;\n" +
				"import static pack.Color.*;\n" +
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        Color c = BLACK;\n" +
				"        switch(c) {\n" +
				"        case BLACK:\n" +
				"            System.out.print(\"Black\");\n" +
				"            break;\n" +
				"        case WHITE:\n" +
				"            System.out.print(\"White\");\n" +
				"            break;\n" +
				"        default: // nop\n" +
				"        }\n" +
				"    }\n" +
				"}",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"Black"
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {BLACK, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"Black",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void  _NA_test106() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"package pack;\n" +
				"import static pack.Color.*;\n" +
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        Color c = BLACK;\n" +
				"        switch(c) {\n" +
				"        }\n" +
				"		 System.out.print(\"SUCCESS\");\n" +
				"    }\n" +
				"}",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"SUCCESS",
			null,
			true,
			null,
			options,
			null
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {BLACK, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"SUCCESS",
		null,
		false,
		null,
		null,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void  _NA_test107() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"package pack;\n" +
				"import static pack.Color.*;\n" +
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        Color c = BLACK;\n" +
				"        switch(c) {\n" +
				"        case BLACK:\n" +
				"            System.out.print(\"Black\");\n" +
				"            break;\n" +
				"        case WHITE:\n" +
				"            System.out.print(\"White\");\n" +
				"            break;\n" +
				"        }\n" +
				"        switch(c) {\n" +
				"        case BLACK:\n" +
				"            System.out.print(\"Black\");\n" +
				"            break;\n" +
				"        case WHITE:\n" +
				"            System.out.print(\"White\");\n" +
				"            break;\n" +
				"        }\n" +
				"    }\n" +
				"}",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"BlackBlack",
			null,
			true,
			null,
			options,
			null
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color { BLACK }"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"BlackBlack",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void  _NA_test108() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"package pack;\n" +
				"import static pack.Color.*;\n" +
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        Color c = BLACK;\n" +
				"        switch(c) {\n" +
				"        case BLACK:\n" +
				"            System.out.print(\"Black\");\n" +
				"            break;\n" +
				"        case WHITE:\n" +
				"            System.out.print(\"White\");\n" +
				"            break;\n" +
				"        default:\n" +
				"            System.out.print(\"Error\");\n" +
				"            break;\n" +
				"        }\n" +
				"    }\n" +
				"}",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"Black",
			null,
			true,
			null,
			options,
			null
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {RED, GREEN, YELLOW, BLACK, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"Black",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void  _NA_test109() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"package pack;\n" +
				"import static pack.Color.*;\n" +
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"		Color c = null;\n" +
				"		 try {\n" +
				"        	c = BLACK;\n" +
				"		} catch(NoSuchFieldError e) {\n" +
				"			System.out.print(\"SUCCESS\");\n" +
				"			return;\n" +
				"		}\n" +
				"      	switch(c) {\n" +
				"       	case BLACK:\n" +
				"          	System.out.print(\"Black\");\n" +
				"          	break;\n" +
				"       	case WHITE:\n" +
				"          	System.out.print(\"White\");\n" +
				"          	break;\n" +
				"      	}\n" +
				"    }\n" +
				"}",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"Black",
			null,
			true,
			null,
			options,
			null
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {RED, GREEN, YELLOW, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"SUCCESS",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void  _NAtest110() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"package pack;\n" +
				"import static pack.Color.*;\n" +
				"public class X {\n" +
				"	public int[] $SWITCH_TABLE$pack$Color;\n" +
				"	public int[] $SWITCH_TABLE$pack$Color() { return null; }\n" +
				"   public static void main(String[] args) {\n" +
				"        Color c = BLACK;\n" +
				"        switch(c) {\n" +
				"        case BLACK:\n" +
				"            System.out.print(\"Black\");\n" +
				"            break;\n" +
				"        case WHITE:\n" +
				"            System.out.print(\"White\");\n" +
				"            break;\n" +
				"        }\n" +
				"    }\n" +
				"}",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"Black",
			null,
			true,
			null,
			options,
			null
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {BLACK, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"Black",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void  _NA_test111() {
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"package pack;\n" +
				"import static pack.Color.*;\n" +
				"@SuppressWarnings(\"incomplete-switch\")\n" +
				"public class X {\n" +
				"	public int[] $SWITCH_TABLE$pack$Color;\n" +
				"	public int[] $SWITCH_TABLE$pack$Color() { return null; }\n" +
				"   public static void main(String[] args) {\n" +
				"        Color c = BLACK;\n" +
				"        switch(c) {\n" +
				"        case BLACK:\n" +
				"            System.out.print(\"Black\");\n" +
				"            break;\n" +
				"        case WHITE:\n" +
				"            System.out.print(\"White\");\n" +
				"            break;\n" +
				"        }\n" +
				"		 foo();\n" +
				"    }\n" +
				"   public static void foo() {\n" +
				"        Color c = BLACK;\n" +
				"        switch(c) {\n" +
				"        case BLACK:\n" +
				"            System.out.print(\"Black\");\n" +
				"            break;\n" +
				"        case WHITE:\n" +
				"            System.out.print(\"White\");\n" +
				"            break;\n" +
				"        }\n" +
				"    }\n" +
				"}",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"BlackBlack"
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {BLACK, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"BlackBlack",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97247
public void _NA_test112() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"com/annot/X.java",
			"package com.annot;\n" +
					"import java.lang.annotation.Target;\n"+
					"import java.lang.annotation.ElementType;\n"+
					"\n"+
					"public class X {\n"+
					"   public static void main(String[] args) {\n" +
					"  enum TestType {\n"+
					"    CORRECTNESS,\n"+
					"    PERFORMANCE\n"+
					"  }\n"+
					"  @Target(ElementType.METHOD)\n"+
					"  @interface Test {\n"+
					"    TestType type() default TestType.CORRECTNESS;\n"+
					"  }\n"+
					"  @Test(type=TestType.PERFORMANCE)\n"+
					"  public void _testBar() throws Exception {\n"+
					"    Test annotation = this.getClass().getMethod(\"testBar\").getAnnotation(Test.class);\n"+
					"    switch (annotation.type()) {\n"+
					"      case PERFORMANCE:\n"+
					"        System.out.println(TestType.PERFORMANCE);\n"+
					"        break;\n"+
					"      case CORRECTNESS:\n"+
					"        System.out.println(TestType.CORRECTNESS);\n"+
					"        break;\n"+
					"    }   \n"+
					"  }\n"+
					"  }\n"+
					"}"
		},
		"",
		null,
		true,
		new String[] {"--enable-preview"},
		options,
		null
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=93789
public void _test113() {
	if (this.complianceLevel >= ClassFileConstants.JDK16) {
		return;
	}
    this.runNegativeTest(
        new String[] {
            "X.java",
			"public class X {\n"+
			"   public static void main(String[] args) {\n" +
			"		enum BugDemo {\n" +
			"			FOO() {\n" +
			"				static int bar;\n" +
			"			}\n" +
			"  		}\n"+
			"  	}\n"+
			"}\n",
        },
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	static int bar;\n" +
		"	           ^^^\n" +
		"The field bar cannot be declared static in a non-static inner type, unless initialized with a constant expression\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99428 and https://bugs.eclipse.org/bugs/show_bug.cgi?id=99655
public void test114() {
    this.runConformTest(
        new String[] {
            "LocalEnumTest.java",
			"import java.lang.reflect.*;\n" +
			"import java.lang.annotation.*;\n" +
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@interface ExpectedModifiers {\n" +
			"	int value();\n" +
			"}\n" +
			"@ExpectedModifiers(Modifier.FINAL)\n" +
			"public enum LocalEnumTest {\n" +
			"	X(255);\n" +
			"	LocalEnumTest(int r) {}\n" +
			"	public static void main(String argv[]) throws Exception {\n" +
			"		test(\"LocalEnumTest\");\n" +
			"		test(\"LocalEnumTest$1EnumA\");\n" +
			"		test(\"LocalEnumTest$1EnumB\");\n" +
			"		test(\"LocalEnumTest$1EnumB2\");\n" +
			"		test(\"LocalEnumTest$1EnumB3\");\n" +
			// TODO (kent) need verifier to detect when an Enum should be tagged as abstract
			//"		test(\"LocalEnumTest$EnumC\");\n" +
			//"		test(\"LocalEnumTest$EnumC2\");\n" +
			"		test(\"LocalEnumTest$1EnumC3\");\n" +
			"		test(\"LocalEnumTest$1EnumD\");\n" +
			"		@ExpectedModifiers(Modifier.FINAL|Modifier.STATIC)\n" +
			"		enum EnumA {\n" +
			"			A;\n" +
			"		}\n" +
			"		@ExpectedModifiers(Modifier.STATIC)\n" +
			"		enum EnumB {\n" +
			"			B {\n" +
			"				int value() { return 1; }\n" +
			"			};\n" +
			"			int value(){ return 0; }\n" +
			"		}\n" +
			"		@ExpectedModifiers(Modifier.STATIC)\n" +
			"		enum EnumB2 {\n" +
			"			B2 {};\n" +
			"			int value(){ return 0; }\n" +
			"		}\n" +
			"		@ExpectedModifiers(Modifier.FINAL|Modifier.STATIC)\n" +
			"		enum EnumB3 {\n" +
			"			B3;\n" +
			"			int value(){ return 0; }\n" +
			"		}\n" +
			"		@ExpectedModifiers(Modifier.STATIC)\n" +
			"		enum EnumC implements I {\n" +
			"			C {\n" +
			"				int value() { return 1; }\n" +
			"			};\n" +
			"			int value(){ return 0; }\n" +
			"			public void foo(){}\n" +
			"		}\n" +
			"		@ExpectedModifiers(Modifier.STATIC)\n" +
			"		enum EnumC2 implements I {\n" +
			"			C2 {};\n" +
			"			int value(){ return 0; }\n" +
			"			public void foo(){}\n" +
			"		}\n" +
			"		@ExpectedModifiers(Modifier.FINAL|Modifier.STATIC)\n" +
			"		enum EnumC3 implements I {\n" +
			"			C3;\n" +
			"			int value(){ return 0; }\n" +
			"			public void foo(){}\n" +
			"		}\n" +
			"		@ExpectedModifiers(Modifier.ABSTRACT|Modifier.STATIC)\n" +
			"		enum EnumD {\n" +
			"			D {\n" +
			"				int value() { return 1; }\n" +
			"			};\n" +
			"			abstract int value();\n" +
			"		}\n" +
			"	}\n" +
			"	static void test(String className) throws Exception {\n" +
			"		Class c = Class.forName(className);\n" +
			"		ExpectedModifiers em = (ExpectedModifiers) c.getAnnotation(ExpectedModifiers.class);\n" +
			"		if (em != null) {\n" +
			"			int classModifiers = c.getModifiers();\n" +
			"			int expected = em.value();\n" +
			"			if (expected != (classModifiers & (Modifier.ABSTRACT|Modifier.FINAL|Modifier.STATIC))) {\n" +
			"				if ((expected & Modifier.ABSTRACT) != (classModifiers & Modifier.ABSTRACT))\n" +
			"					System.out.println(\"FAILED ABSTRACT: \" + className);\n" +
			"				if ((expected & Modifier.FINAL) != (classModifiers & Modifier.FINAL))\n" +
			"					System.out.println(\"FAILED FINAL: \" + className);\n" +
			"				if ((expected & Modifier.STATIC) != (classModifiers & Modifier.STATIC))\n" +
			"					System.out.println(\"FAILED STATIC: \" + className);\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101713
public void test115() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"public class X {\n" +
			"	public static void main(String argv[]) {\n" +
			"		enum Y {\n" +
			"			VALUE;\n" +
			"\n" +
			"			static int ASD;\n" +
			"			final static int CST = 0;\n" +
			"			\n" +
			"			private Y() {\n" +
			"				VALUE = null;\n" +
			"				ASD = 5;\n" +
			"				Y.VALUE = null;\n" +
			"				Y.ASD = 5;\n" +
			"				\n" +
			"				System.out.println(CST);\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
        },
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	VALUE = null;\n" +
		"	^^^^^\n" +
		"Cannot refer to the static enum field Y.VALUE within an initializer\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 11)\n" +
		"	ASD = 5;\n" +
		"	^^^\n" +
		"Cannot refer to the static enum field Y.ASD within an initializer\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 12)\n" +
		"	Y.VALUE = null;\n" +
		"	  ^^^^^\n" +
		"Cannot refer to the static enum field Y.VALUE within an initializer\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 13)\n" +
		"	Y.ASD = 5;\n" +
		"	  ^^^\n" +
		"Cannot refer to the static enum field Y.ASD within an initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101713 - variation
public void test116() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String argv[]) {\n" +
			"		enum Y {\n" +
			"			BLEU, \n" +
			"			BLANC, \n" +
			"			ROUGE;\n" +
			"			{\n" +
			"				BLEU = null;\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	BLEU = null;\n" +
		"	^^^^\n" +
		"Cannot refer to the static enum field Y.BLEU within an initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101713 - variation
public void test117() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String argv[]) {\n" +
			"		enum Y {\n" +
			"			BLEU, \n" +
			"			BLANC, \n" +
			"			ROUGE;\n" +
			"			{\n" +
			"				Y x = BLEU.BLANC; // ko\n" +
			"				Y x2 = BLEU; // ko\n" +
			"			}\n" +
			"			static {\n" +
			"				Y x = BLEU.BLANC; // ok\n" +
			"				Y x2 = BLEU; // ok\n" +
			"			}	\n" +
			"			Y dummy = BLEU; // ko\n" +
			"			static Y DUMMY = BLANC; // ok\n" +
			"			Y() {\n" +
			"				Y x = BLEU.BLANC; // ko\n" +
			"				Y x2 = BLEU; // ko\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	Y x = BLEU.BLANC; // ko\n" +
		"	      ^^^^\n" +
		"Cannot refer to the static enum field Y.BLEU within an initializer\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	Y x = BLEU.BLANC; // ko\n" +
		"	           ^^^^^\n" +
		"Cannot refer to the static enum field Y.BLANC within an initializer\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 8)\n" +
		"	Y x = BLEU.BLANC; // ko\n" +
		"	           ^^^^^\n" +
		"The static field Y.BLANC should be accessed in a static way\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 9)\n" +
		"	Y x2 = BLEU; // ko\n" +
		"	       ^^^^\n" +
		"Cannot refer to the static enum field Y.BLEU within an initializer\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 12)\n" +
		"	Y x = BLEU.BLANC; // ok\n" +
		"	           ^^^^^\n" +
		"The static field Y.BLANC should be accessed in a static way\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 15)\n" +
		"	Y dummy = BLEU; // ko\n" +
		"	          ^^^^\n" +
		"Cannot refer to the static enum field Y.BLEU within an initializer\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 18)\n" +
		"	Y x = BLEU.BLANC; // ko\n" +
		"	      ^^^^\n" +
		"Cannot refer to the static enum field Y.BLEU within an initializer\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 18)\n" +
		"	Y x = BLEU.BLANC; // ko\n" +
		"	           ^^^^^\n" +
		"Cannot refer to the static enum field Y.BLANC within an initializer\n" +
		"----------\n" +
		"9. WARNING in X.java (at line 18)\n" +
		"	Y x = BLEU.BLANC; // ko\n" +
		"	           ^^^^^\n" +
		"The static field Y.BLANC should be accessed in a static way\n" +
		"----------\n" +
		"10. ERROR in X.java (at line 19)\n" +
		"	Y x2 = BLEU; // ko\n" +
		"	       ^^^^\n" +
		"Cannot refer to the static enum field Y.BLEU within an initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102265
public void test118() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String argv[]) {\n" +
			"		enum Y {\n" +
			"			 one,\n" +
			"			 two;\n" +
			"			 \n" +
			"			 static ArrayList someList;\n" +
			"			 \n" +
			"			 private Y() {\n" +
			"			 		 if (someList == null) {\n" +
			"			 		 		 someList = new ArrayList();\n" +
			"			 		 }\n" +
			"			 }\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 9)\n" +
		"	static ArrayList someList;\n" +
		"	       ^^^^^^^^^\n" +
		"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	if (someList == null) {\n" +
		"	    ^^^^^^^^\n" +
		"Cannot refer to the static enum field Y.someList within an initializer\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 13)\n" +
		"	someList = new ArrayList();\n" +
		"	^^^^^^^^\n" +
		"Cannot refer to the static enum field Y.someList within an initializer\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 13)\n" +
		"	someList = new ArrayList();\n" +
		"	               ^^^^^^^^^\n" +
		"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" +
		"----------\n");
}
public void test119() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String argv[]) {\n" +
			"		enum Y {\n" +
			"			BLEU, BLANC, ROUGE;\n" +
			"			final static int CST = 0;\n" +
			"          		enum Member {\n" +
			"          			;\n" +
			"             	 	Object obj1 = CST;\n" +
			"              		Object obj2 = BLEU;\n" +
			"          		}\n" +
			"       	}\n" +
			"     }\n" +
			"}\n"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102213
public void test120() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String argv[]) {\n" +
			"		enum Y {\n" +
			"\n" +
			"			A() {\n" +
			"				final Y a = A;\n" +
			"				final Y a2 = B.A;\n" +
			"				@Override void foo() {\n" +
			"					System.out.println(String.valueOf(a));\n" +
			"					System.out.println(String.valueOf(a2));\n" +
			"				}\n" +
			"			},\n" +
			"			B() {\n" +
			"				@Override void foo(){}\n" +
			"			};\n" +
			"			abstract void foo();\n" +
			"			\n" +
			"			public static void main(String[] args) {\n" +
			"				A.foo();\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	final Y a = A;\n" +
		"	            ^\n" +
		"Cannot refer to the static enum field Y.A within an initializer\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	final Y a2 = B.A;\n" +
		"	             ^\n" +
		"Cannot refer to the static enum field Y.B within an initializer\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	final Y a2 = B.A;\n" +
		"	               ^\n" +
		"Cannot refer to the static enum field Y.A within an initializer\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 7)\n" +
		"	final Y a2 = B.A;\n" +
		"	               ^\n" +
		"The static field Y.A should be accessed in a static way\n" +
		"----------\n",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=92165
public void test121() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X { \n" +
					"	public static void main(String[] args) {\n" +
							"enum Y {\n" +
							"\n" +
							"	UNKNOWN();\n" +
							"\n" +
							"	private static String error;\n" +
							"\n" +
							"	{\n" +
							"		error = \"error\";\n" +
							"	}\n" +
							"}\n" +
						"}\n" +
			"\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	error = \"error\";\n" +
		"	^^^^^\n" +
		"Cannot refer to the static enum field Y.error within an initializer\n" +
		"----------\n");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105592
public void test122() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo() {\n" +
			"		enum State {\n" +
			"			NORMAL\n" +
					"}\n" +
			"		State state = State.NORMAL;\n" +
			"		switch (state) {\n" +
			"		case (NORMAL) :\n" +
			"			System.out.println(State.NORMAL);\n" +
			"			break;\n" +
			"       default: // nop\n" +
			"		}\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	case (NORMAL) :\n" +
		"	     ^^^^^^^^\n" +
		"Enum constants cannot be surrounded by parenthesis\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110403
public void test123() {
	this.runNegativeTest(
		new String[] {
			"Foo.java",
			"public class Foo { \n" +
					"	public static void main(String[] args) {\n" +
							"enum Y {\n" +
							" A(0);\n" +
							" Y(int x) {\n" +
							"    t[0]=x;\n" +
							" }\n" +
							" private static final int[] t = new int[12];\n" +
							"}\n" +
						"}\n" +
						"\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Foo.java (at line 6)\n" +
		"	t[0]=x;\n" +
		"	^\n" +
		"Cannot refer to the static enum field Y.t within an initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=1101417
public void test124() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			" public class X {\n" +
			"	public void foo() {\n" +
			"		enum Y {\n" +
			"		max {\n" +
			"		{ \n" +
			"				val=3;  \n" +
			"			}         \n" +
			"			@Override public String toString() {\n" +
			"				return Integer.toString(val);\n" +
			"			}\n" +
			"		}; \n" +
			"		{\n" +
			"			val=2;\n" +
			"		}\n" +
			"		private int val; \n" +
			"		}\n" +
			"}\n" +
			"  public static void main(String[] args) {\n" +
			"  }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	val=3;  \n" +
		"	^^^\n" +
		"Cannot make a static reference to the non-static field val\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	return Integer.toString(val);\n" +
		"	                        ^^^\n" +
		"Cannot make a static reference to the non-static field val\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=112231
public void test125() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"\n" +
			"public class X {\n" +
			"	interface I {\n" +
			"		default int values(){\n" +
				"		enum E implements I {\n" +
				"			A, B, C;\n" +
				"		}\n" +
				"	}\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	enum E implements I {\n" +
		"	     ^\n" +
		"This static method cannot hide the instance method from X.I\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=126087
public void test126() {
	this.runConformTest(
		new String[] {
			"X.java",
			"  public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"      enum NoValues {}\n" +
			"      System.out.println(\"[\"+NoValues.values().length+\"]\");\n" +
			"    }\n" +
			"  }\n"
		},
		"[0]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=126087
// Commented and created https://bugs.eclipse.org/bugs/show_bug.cgi?id=570106
public void test127() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"		enum Y {\n" +
				"			VALUE {\n" +
				"				void foo() {\n" +
				"				};\n" +
				"			};\n" +
				"			abstract void foo();\n" +
				"		}\n" +
				"      System.out.println(\"[\"+Y.values().length+\"]\");\n" +
				"    }\n" +
				"}"
		},
		"[1]");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
			"// Compiled from X.java (version 16 : 60.0, super bit)\n" +
			"public class X {\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public X();\n" +
			"    0  aload_0 [this]\n" +
			"    1  invokespecial java.lang.Object() [8]\n" +
			"    4  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
			"  \n" +
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     3  invokestatic X$1Y.values() : X$1Y[] [22]\n" +
			"     6  arraylength\n" +
			"     7  invokedynamic 0 makeConcatWithConstants(int) : java.lang.String [28]\n" +
			"    12  invokevirtual java.io.PrintStream.println(java.lang.String) : void [32]\n" +
			"    15  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 10]\n" +
			"        [pc: 15, line: 11]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #23 X$1Y, outer class info: #0\n" +
			"     inner name: #52 Y, accessflags: 17416 abstract static],\n" +
			"    [inner class info: #53 java/lang/invoke/MethodHandles$Lookup, outer class info: #55 java/lang/invoke/MethodHandles\n" +
			"     inner name: #57 Lookup, accessflags: 25 public static final]\n" +
			"\n" +
			"Nest Members:\n" +
			"   #23 X$1Y,\n" +
			"   #59 X$1Y$1\n" +
			"Bootstrap methods:\n" +
			"  0 : # 48 invokestatic java/lang/invoke/StringConcatFactory.makeConcatWithConstants:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#49 []\n" +
			"}";

	int index = actualOutput.indexOf(expectedOutput);

	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}

	disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1Y$1.class"));
	actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	expectedOutput =
		"ENUM$VALUES";

	index = actualOutput.indexOf(expectedOutput);
	if (index != -1) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index != -1) {
		assertTrue("Must not have field ENUM$VALUES", false);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127766
public void test128() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);

	this.runNegativeTest(
         new String[] {
        		 "X.java",
        		 "public class X {\n" +
        		 "	public static void main( String[] args) {\n" +
        		 "		Enum e = new Enum(\"foo\", 2) {\n" +
        		 "			public int compareTo( Object o) {\n" +
        		 "				return 0;\n" +
        		 "			}\n" +
        		 "		};\n" +
        		 "		System.out.println(e);\n" +
        		 "	}\n" +
        		 "}",
         },
         "----------\n" +
         "1. ERROR in X.java (at line 3)\n" +
         "	Enum e = new Enum(\"foo\", 2) {\n" +
         "	             ^^^^\n" +
         "The type new Enum(){} may not subclass Enum explicitly\n" +
         "----------\n",
         null,
         true,
         options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=141155
public void test129() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"  public class X {\n" +
			"    public void foo(){\n" +
			"		enum Y {\n" +
			"        A, B, C;\n" +
			"		}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"    }\n" +
			"}\n",
		},
		"");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"public class X {\n"
		+ "  \n"
		+ "  // Method descriptor #6 ()V\n"
		+ "  // Stack: 1, Locals: 1\n"
		+ "  public X();\n"
		+ "    0  aload_0 [this]\n"
		+ "    1  invokespecial java.lang.Object() [8]\n"
		+ "    4  return\n"
		+ "      Line numbers:\n"
		+ "        [pc: 0, line: 1]\n"
		+ "      Local variable table:\n"
		+ "        [pc: 0, pc: 5] local: this index: 0 type: X\n"
		+ "  \n"
		+ "  // Method descriptor #6 ()V\n"
		+ "  // Stack: 0, Locals: 1\n"
		+ "  public void foo();\n"
		+ "    0  return\n"
		+ "      Line numbers:\n"
		+ "        [pc: 0, line: 6]\n"
		+ "      Local variable table:\n"
		+ "        [pc: 0, pc: 1] local: this index: 0 type: X\n"
		+ "  \n"
		+ "  // Method descriptor #16 ([Ljava/lang/String;)V\n"
		+ "  // Stack: 0, Locals: 1\n"
		+ "  public static void main(java.lang.String[] args);\n"
		+ "    0  return\n"
		+ "      Line numbers:\n"
		+ "        [pc: 0, line: 8]\n"
		+ "      Local variable table:\n"
		+ "        [pc: 0, pc: 1] local: args index: 0 type: java.lang.String[]\n"
		+ "\n"
		+ "  Inner classes:\n"
		+ "    [inner class info: #22 X$1Y, outer class info: #0\n"
		+ "     inner name: #24 Y, accessflags: 16408 static final]\n"
		+ "\n"
		+ "Nest Members:\n"
		+ "   #22 X$1Y\n"
		+ "}";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=141810
public void test130() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   public static void main(String[] args) {\n" +
				"      enum Action {ONE, TWO}\n" +
				"      for(Action a : Action.values()) {\n" +
				"         switch(a) {\n" +
				"         case ONE:\n" +
				"            System.out.print(\"1\");\n" +
				"            break;\n" +
				"         case TWO:\n" +
				"            System.out.print(\"2\");\n" +
				"            break;\n" +
				"         default:\n" +
				"            System.out.print(\"default\");\n" +
				"         }\n" +
				"      }\n" +
				"   }\n" +
				"}",
			},
			"12"
		);

	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"   public static void main(String[] args) {\n" +
				"      enum Action {ONE, TWO, THREE}\n" +
				"      for(Action a : Action.values()) {\n" +
				"         switch(a) {\n" +
				"         case ONE:\n" +
				"            System.out.print(\"1\");\n" +
				"            break;\n" +
				"         case TWO:\n" +
				"            System.out.print(\"2\");\n" +
				"            break;\n" +
				"         default:\n" +
				"            System.out.print(\"default\");\n" +
				"         }\n" +
				"      }\n" +
				"   }\n" +
				"}",
			},
			"12default"
		);


}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=145732
public void test131() {
	this.runConformTest(
         new String[] {
        		 "X.java",
     			"public class X {\n" +
    			"	//A,B\n" +
    			"	;\n" +
    			"	public static void main(String[] args) {\n" +
    			"		enum Y { }\n " +
    			"		try {\n" +
    			"			System.out.println(Y.valueOf(null));\n" +
    			"		} catch (NullPointerException e) {\n" +
    			"			System.out.println(\"NullPointerException\");\n" +
    			"		} catch (IllegalArgumentException e) {\n" +
    			"			System.out.println(\"IllegalArgumentException\");\n" +
    			"		}\n" +
    			"	}\n" +
    			"}\n",
         },
         "NullPointerException");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=145732 - variation
public void test132() {
	this.runConformTest(
         new String[] {
        		 "X.java",
     			"public class X {\n" +
    			"	public static void main(String[] args) {\n" +
    			"		enum Y {\n" +
    			"			A,B\n" +
    			"			;\n" +
    			"		}\n" +
    			"		try {\n" +
    			"			System.out.println(Y.valueOf(null));\n" +
    			"		} catch (NullPointerException e) {\n" +
    			"			System.out.println(\"NullPointerException\");\n" +
    			"		} catch (IllegalArgumentException e) {\n" +
    			"			System.out.println(\"IllegalArgumentException\");\n" +
    			"		}\n" +
    			"	}\n" +
    			"}\n",
         },
         "NullPointerException");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=147747
public void test133() throws Exception {
	this.runConformTest(
         new String[] {
        		"X.java",
     			"public class X {\n" +
    			"	public static void main(String[] args) {\n" +
    			"		enum Y {\n" +
    			"			A, B, C;\n" +
    			"		}\n" +
    			"	}\n" +
    			"}\n",
         },
         "");
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"public class X {\n"
		+ "  \n"
		+ "  // Method descriptor #6 ()V\n"
		+ "  // Stack: 1, Locals: 1\n"
		+ "  public X();\n"
		+ "    0  aload_0 [this]\n"
		+ "    1  invokespecial java.lang.Object() [8]\n"
		+ "    4  return\n"
		+ "      Line numbers:\n"
		+ "        [pc: 0, line: 1]\n"
		+ "      Local variable table:\n"
		+ "        [pc: 0, pc: 5] local: this index: 0 type: X\n"
		+ "  \n"
		+ "  // Method descriptor #15 ([Ljava/lang/String;)V\n"
		+ "  // Stack: 0, Locals: 1\n"
		+ "  public static void main(java.lang.String[] args);\n"
		+ "    0  return\n"
		+ "      Line numbers:\n"
		+ "        [pc: 0, line: 6]\n"
		+ "      Local variable table:\n"
		+ "        [pc: 0, pc: 1] local: args index: 0 type: java.lang.String[]\n"
		+ "\n"
		+ "  Inner classes:\n"
		+ "    [inner class info: #21 X$1Y, outer class info: #0\n"
		+ "     inner name: #23 Y, accessflags: 16408 static final]\n"
		+ "\n"
		+ "Nest Members:\n"
		+ "   #21 X$1Y\n"
		+ "}";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149042
public void test134() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"public class X {\n" +
		  	"	public static void main(String[] args) {\n" +
	    	"		enum Y {\n" +
			"    		INITIAL ,\n" +
			"    		OPENED {\n" +
			"        	{\n" +
			"            	System.out.printf(\"After the %s constructor\\n\",INITIAL);\n" +
			"        	}\n" +
			"		}\n" +
			"    }\n" +
			"	}\n" +
			"}",
        },
        "----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	System.out.printf(\"After the %s constructor\\n\",INITIAL);\n" +
		"	                                               ^^^^^^^\n" +
		"Cannot refer to the static enum field Y.INITIAL within an initializer\n" +
		"----------\n",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149562
// a default case is required to consider that b is initialized (in case E
// takes new values in the future)
public void test135() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"public class X {\n" +
			"    boolean foo() {\n" +
			"		enum E {\n" +
			"    		A,\n" +
			"    		B\n" +
			"		}\n" +
			"		 E e = E.A;\n" +
			"        boolean b;\n" +
			"        switch (e) {\n" +
			"          case A:\n" +
			"              b = true;\n" +
			"              break;\n" +
			"          case B:\n" +
			"              b = false;\n" +
			"              break;\n" +
			"        }\n" +
			"        return b;\n" +
			"    }\n" +
			"}",
        },
        "----------\n" +
		"1. ERROR in X.java (at line 17)\n" +
		"	return b;\n" +
		"	       ^\n" +
		"The local variable b may not have been initialized. Note that a problem regarding missing 'default:' on 'switch' has been suppressed, which is perhaps related to this problem\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=151368
public void test136() {
 this.runConformTest(
     new String[] {
        "X.java",
        "import p.BeanName;\n" +
		"public class X {\n" +
		"	Object o = BeanName.CreateStepApiOperation;\n" +
		"}",
		"p/BeanName.java",
		"package p;\n" +
		"public enum BeanName {\n" +
		"\n" +
		"    //~ Enum constants ---------------------------------------------------------\n" +
		"\n" +
		"    AbortAllJobsOperation,\n" +
		"    AbortJobApiOperation,\n" +
		"    AbortStepOperation,\n" +
		"    AclVoter,\n" +
		"    AcquireNamedLockApiOperation,\n" +
		"    AuthenticationManager,\n" +
		"    BeginStepOperation,\n" +
		"    CloneApiOperation,\n" +
		"    CommanderDao,\n" +
		"    CommanderServer,\n" +
		"    ConfigureQuartzOperation,\n" +
		"    CreateAclEntryApiOperation,\n" +
		"    CreateActualParameterApiOperation,\n" +
		"    CreateFormalParameterApiOperation,\n" +
		"    CreateProcedureApiOperation,\n" +
		"    CreateProjectApiOperation,\n" +
		"    CreateResourceApiOperation,\n" +
		"    CreateScheduleApiOperation,\n" +
		"    CreateStepApiOperation,\n" +
		"    DeleteAclEntryApiOperation,\n" +
		"    DeleteActualParameterApiOperation,\n" +
		"    DeleteFormalParameterApiOperation,\n" +
		"    DeleteJobApiOperation,\n" +
		"    DeleteProcedureApiOperation,\n" +
		"    DeleteProjectApiOperation,\n" +
		"    DeletePropertyApiOperation,\n" +
		"    DeleteResourceApiOperation,\n" +
		"    DeleteScheduleApiOperation,\n" +
		"    DeleteStepApiOperation,\n" +
		"    DispatchApiRequestOperation,\n" +
		"    DumpStatisticsApiOperation,\n" +
		"    ExpandJobStepAction,\n" +
		"    ExportApiOperation,\n" +
		"    FinishStepOperation,\n" +
		"    GetAccessApiOperation,\n" +
		"    GetAclEntryApiOperation,\n" +
		"    GetActualParameterApiOperation,\n" +
		"    GetActualParametersApiOperation,\n" +
		"    GetFormalParameterApiOperation,\n" +
		"    GetFormalParametersApiOperation,\n" +
		"    GetJobDetailsApiOperation,\n" +
		"    GetJobInfoApiOperation,\n" +
		"    GetJobStatusApiOperation,\n" +
		"    GetJobStepDetailsApiOperation,\n" +
		"    GetJobStepStatusApiOperation,\n" +
		"    GetJobsApiOperation,\n" +
		"    GetProcedureApiOperation,\n" +
		"    GetProceduresApiOperation,\n" +
		"    GetProjectApiOperation,\n" +
		"    GetProjectsApiOperation,\n" +
		"    GetPropertiesApiOperation,\n" +
		"    GetPropertyApiOperation,\n" +
		"    GetResourceApiOperation,\n" +
		"    GetResourcesApiOperation,\n" +
		"    GetResourcesInPoolApiOperation,\n" +
		"    GetScheduleApiOperation,\n" +
		"    GetSchedulesApiOperation,\n" +
		"    GetStepApiOperation,\n" +
		"    GetStepsApiOperation,\n" +
		"    GetVersionsApiOperation,\n" +
		"    GraphWorkflowApiOperation,\n" +
		"    HibernateFlushListener,\n" +
		"    ImportApiOperation,\n" +
		"    IncrementPropertyApiOperation,\n" +
		"    InvokeCommandOperation,\n" +
		"    InvokePostProcessorOperation,\n" +
		"    LoginApiOperation,\n" +
		"    LogManager,\n" +
		"    LogMessageApiOperation,\n" +
		"    ModifyAclEntryApiOperation,\n" +
		"    ModifyActualParameterApiOperation,\n" +
		"    ModifyFormalParameterApiOperation,\n" +
		"    ModifyProcedureApiOperation,\n" +
		"    ModifyProjectApiOperation,\n" +
		"    ModifyPropertyApiOperation,\n" +
		"    ModifyResourceApiOperation,\n" +
		"    ModifyScheduleApiOperation,\n" +
		"    ModifyStepApiOperation,\n" +
		"    MoveStepApiOperation,\n" +
		"    PauseSchedulerApiOperation,\n" +
		"    QuartzQueue,\n" +
		"    QuartzScheduler,\n" +
		"    ReleaseNamedLockApiOperation,\n" +
		"    ResourceInvoker,\n" +
		"    RunProcedureApiOperation,\n" +
		"    RunQueryApiOperation,\n" +
		"    SaxReader,\n" +
		"    ScheduleStepsOperation,\n" +
		"    SessionCache,\n" +
		"    SetJobNameApiOperation,\n" +
		"    SetPropertyApiOperation,\n" +
		"    SetStepStatusAction,\n" +
		"    StartWorkflowOperation,\n" +
		"    StateRefreshOperation,\n" +
		"    StepCompletionPrecondition,\n" +
		"    StepOutcomePrecondition,\n" +
		"    StepScheduler,\n" +
		"    TemplateOperation,\n" +
		"    TimeoutWatchdog,\n" +
		"    UpdateConfigurationOperation,\n" +
		"    Workspace,\n" +
		"    XmlRequestHandler;\n" +
		"\n" +
		"    //~ Static fields/initializers ---------------------------------------------\n" +
		"\n" +
		"    public static final int MAX_BEAN_NAME_LENGTH = 33;\n" +
		"\n" +
		"    //~ Methods ----------------------------------------------------------------\n" +
		"\n" +
		"    /**\n" +
		"     * Get this bean name as a property name, i.e. uncapitalized.\n" +
		"     *\n" +
		"     * @return String\n" +
		"     */\n" +
		"    public String getPropertyName()\n" +
		"    {\n" +
		"        return null;\n" +
		"    }\n" +
		"}\n", // =================,
     },
	"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156540
public void test137() {
 this.runConformTest(
     new String[] {
        "X.java",
        "public class X {\n" +
        "\n" +
        "    interface Interface {\n" +
        "        public int value();\n" +
        "    }\n" +
        "\n" +
        "\n" +
        "    public static void main(String[] args) {\n" +
        "    	enum MyEnum implements Interface {\n" +
        "        ;\n" +
        "\n" +
        "        	MyEnum(int value) { this.value = value; }        \n" +
        "       	public int value() { return this.value; }\n" +
        "        	private int value;\n" +
        "    	}\n" +
        "        System.out.println(MyEnum.values().length);\n" +
        "    }\n" +
        "}", // =================,
     },
	"0");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156591
public void test138() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"    	enum Y {\n" +
			"			PLUS {\n" +
			"				double eval(double x, double y) {\n" +
			"					return x + y;\n" +
			"				}\n" +
			"			},\n" +
			"			MINUS {\n" +
			"				@Override\n" +
			"				abstract double eval(double x, double y);\n" +
			"			};\n" +
			"			abstract double eval(double x, double y);\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"\n", // =================
		 },
		"----------\n" +
		"1. WARNING in X.java (at line 5)\n" +
		"	double eval(double x, double y) {\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The method eval(double, double) of type new Y(){} should be tagged with @Override since it actually overrides a superclass method\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	MINUS {\n" +
		"	^^^^^\n" +
		"The enum constant MINUS cannot define abstract methods\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 11)\n" +
		"	abstract double eval(double x, double y);\n" +
		"	                ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The method eval cannot be abstract in the enum constant MINUS\n" +
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156591 - variation
public void test139() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"    	enum Y {\n" +
			"			PLUS {\n" +
			"				double eval(double x, double y) {\n" +
			"					return x + y;\n" +
			"				}\n" +
			"			},\n" +
			"			MINUS {\n" +
			"				abstract double eval2(double x, double y);\n" +
			"			};\n" +
			"\n" +
			"			abstract double eval(double x, double y);\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"\n", // =================
		},
		"----------\n" +
		"1. WARNING in X.java (at line 5)\n" +
		"	double eval(double x, double y) {\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The method eval(double, double) of type new Y(){} should be tagged with @Override since it actually overrides a superclass method\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	MINUS {\n" +
		"	^^^^^\n" +
		"The enum constant MINUS cannot define abstract methods\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	MINUS {\n" +
		"	^^^^^\n" +
		"The enum constant MINUS must implement the abstract method eval(double, double)\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 10)\n" +
		"	abstract double eval2(double x, double y);\n" +
		"	                ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The method eval2 cannot be abstract in the enum constant MINUS\n" +
		"----------\n"
	);
}
//check final modifier
public void test140() {
 this.runConformTest(
     new String[] {
    	        "X.java",
    			"public class X {\n" +
    			"	void bar(X x) {\n" +
    			"		enum Y {\n" +
    			"			PLUS {/*ANONYMOUS*/}, MINUS;\n" +
    			"		}\n" +
    			"		Y y = Y.PLUS;\n" +
    			"		Runnable r = (Runnable)y;\n" +
    			"	}\n" +
    			"}", // =================
     },
	"");
}
//check final modifier
public void test141() {
 this.runNegativeTest(
     new String[] {
    	        "X.java",
    			"public class X {\n" +
    			"	void bar(X x) {\n" +
       			"		enum Y {\n" +
    			"			PLUS, MINUS;\n" +
    			"		}\n" +
    			"		Y y = Y.PLUS;\n" +

    			"		Runnable r = (Runnable)y;\n" +
    			"	}\n" +
    			"}", // =================
     },
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	Runnable r = (Runnable)y;\n" +
		"	             ^^^^^^^^^^^\n" +
		"Cannot cast from Y to Runnable\n" +
		"----------\n");
}
public void test142() {
 this.runConformTest(
     new String[] {
    	        "X.java",
    			"public class X {\n" +
    			"	public static void main(String[] args) {\n" +
    			"		enum Week {\n" +
    			"			Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday\n" +
    			"		}\n" +
    			"		for (Week w : Week.values())\n" +
    			"			System.out.print(w + \" \");\n" +
    			"		for (Week w : java.util.EnumSet.range(Week.Monday, Week.Friday)) {\n" +
    			"			System.out.print(w + \" \");\n" +
    			"		}\n" +
    			"	}\n" +
    			"}\n", // =================
     },
     "Monday Tuesday Wednesday Thursday Friday Saturday Sunday Monday Tuesday Wednesday Thursday Friday");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=166866
public void test143() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
			   	"	public static void main(String[] args) {\n" +
		    	"		enum Y {\n" +
				"  			A {\n" +
				"    			@Override\n" +
				"    			public String toString() {\n" +
				"      				return a();\n" +
				"    			}\n" +
				"    			public abstract String a();\n" +
				"  			}\n" +
				"		}\n" +
				"	}\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	A {\n" +
		"	^\n" +
		"The enum constant A cannot define abstract methods\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	public abstract String a();\n" +
		"	                       ^^^\n" +
		"The method a cannot be abstract in the enum constant A\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=186822
public void test144() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X{\n" +
				"	enum Y<T> {}\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	enum Y<T> {}\n" +
		"	       ^\n" +
		"Syntax error, enum declaration cannot have type parameters\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=186822
public void test145() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"ClassC.java",
			"public class ClassC {\n" +
			"  void bar() {\n" +
			"	 enum EnumA {\n" +
			"  		B1,\n" +
			"  		B2;\n" +
			"  		public void foo(){}\n" +
			"	 }\n" +
			"    EnumA.B1.B1.foo();\n" +
			"    EnumA.B1.B2.foo();\n" +
			"  }\n" +
			"}",
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in ClassC.java (at line 8)\n" +
		"	EnumA.B1.B1.foo();\n" +
		"	         ^^\n" +
		"The static field EnumA.B1 should be accessed in a static way\n" +
		"----------\n" +
		"2. ERROR in ClassC.java (at line 9)\n" +
		"	EnumA.B1.B2.foo();\n" +
		"	         ^^\n" +
		"The static field EnumA.B2 should be accessed in a static way\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=207915
public void test146() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"	final String test;\n" +
				"	public X() { // error\n" +
				"		enum MyEnum {\n" +
				"			A, B\n" +
				"		}\n" +
				"		MyEnum e = MyEnum.A;\n" +
				"		switch (e) {\n" +
				"			case A:\n" +
				"				test = \"a\";\n" +
				"				break;\n" +
				"			case B:\n" +
				"				test = \"a\";\n" +
				"				break;\n" +
				"			// default: test = \"unknown\"; // enabling this line fixes above error\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public X() { // error\n" +
		"	       ^^^\n" +
		"The blank final field test may not have been initialized. Note that a problem regarding missing 'default:' on 'switch' has been suppressed, which is perhaps related to this problem\n" +
		"----------\n");
}
// normal error when other warning is enabled
public void test146b() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"	final String test;\n" +
				"	public X() { // error\n" +
				"		enum MyEnum {\n" +
				"			A, B\n" +
				"		}\n" +
				"		MyEnum e = MyEnum.A;\n" +
				"		switch (e) {\n" +
				"			case A:\n" +
				"				test = \"a\";\n" +
				"				break;\n" +
				"			case B:\n" +
				"				test = \"a\";\n" +
				"				break;\n" +
				"			// default: test = \"unknown\"; // enabling this line fixes above error\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public X() { // error\n" +
		"	       ^^^\n" +
		"The blank final field test may not have been initialized\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 8)\n" +
		"	switch (e) {\n" +
		"	        ^\n" +
		"The switch over the enum type MyEnum should have a default case\n" +
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502
public void test147() {
	this.runNegativeTest(
			new String[] {
					"p/X.java",
					"package p;\n" +
					"public class X {\n" +
				   	"	public static void main(String[] args) {\n" +
					"		public abstract enum E {\n" +
					"			SUCCESS;\n" +
					"		}\n" +
					"	}\n" +
					"}\n"
			},
			"----------\n" +
			"1. ERROR in p\\X.java (at line 4)\n" +
			"	public abstract enum E {\n" +
			"	                     ^\n" +
			"Illegal modifier for local enum E; no explicit modifier is permitted\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test148() {
	this.runNegativeTest(
			new String[] {
					"p/X.java",
					"package p;\n" +
					"public class X {\n" +
				   	"	public static void main(String[] args) {\n" +
					"		abstract enum E implements Runnable {\n" +
					"			SUCCESS;\n" +
					"		}\n" +
					"	}\n" +
					"}\n"
			},
			"----------\n" +
			"1. ERROR in p\\X.java (at line 4)\n" +
			"	abstract enum E implements Runnable {\n" +
			"	              ^\n" +
			"Illegal modifier for local enum E; no explicit modifier is permitted\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void _NA_test149() throws Exception {
	this.runConformTest(
			new String[] {
					"p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"	public enum E implements Runnable {\n" +
					"		SUCCESS;\n" +
					"		public void run(){}\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		Class<E> c = E.class;\n" +
					"		System.out.println(c.getName() + \":\" + X.E.SUCCESS);\n" +
					"	}\n" +
					"}\n"
			},
			"p.X$E:SUCCESS");

	String expectedOutput =
		"// Signature: Ljava/lang/Enum<Lp/X$E;>;Ljava/lang/Runnable;\n" +
		"public static final enum p.X$E implements java.lang.Runnable {\n";

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"p" + File.separator + "X$E.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test150() throws Exception {
	this.runConformTest(
			new String[] {
					"p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		enum E implements Runnable {\n" +
					"			SUCCESS;\n" +
					"			public void run(){}\n" +
					"		}\n" +
					"		Class<E> c = E.class;\n" +
					"		System.out.println(c.getName() + \":\" + E.SUCCESS);\n" +
					"	}\n" +
					"}\n"
			},
			"p.X$1E:SUCCESS");

}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test151() throws Exception {
	this.runConformTest(
			new String[] {
					"p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		enum E implements Runnable {\n" +
					"			SUCCESS {};\n" +
					"			public void run(){}\n" +
					"		}\n" +
					"		Class<E> c = E.class;\n" +
					"		System.out.println(c.getName() + \":\" + E.SUCCESS);\n" +
					"	}\n" +
					"}\n"
			},
			"p.X$1E:SUCCESS");

	String expectedOutput =
		"// Signature: Ljava/lang/Enum<Lp/X$1E;>;Ljava/lang/Runnable;\n"
		+ "abstract static enum p.X$1E implements java.lang.Runnable {\n"
		+ "  \n"
		+ "  // Field descriptor #8 Lp/X$1E;\n"
		+ "  public static final enum p.X$1E SUCCESS;\n"
		+ "  \n"
		+ "  // Field descriptor #10 [Lp/X$1E;\n"
		+ "  private static final synthetic p.X$1E[] ENUM$VALUES;\n"
		+ "  \n"
		+ "  // Method descriptor #12 ()V\n"
		+ "  // Stack: 4, Locals: 0\n"
		+ "  static {};\n"
		+ "     0  new p.X$1E$1 [14]\n"
		+ "     3  dup\n"
		+ "     4  ldc <String \"SUCCESS\"> [16]\n"
		+ "     6  iconst_0\n"
		+ "     7  invokespecial p.X$1E$1(java.lang.String, int) [17]\n"
		+ "    10  putstatic p.X$1E.SUCCESS : new p.X(){} [21]\n"
		+ "    13  iconst_1\n"
		+ "    14  anewarray p.X$1E [1]\n"
		+ "    17  dup\n"
		+ "    18  iconst_0\n"
		+ "    19  getstatic p.X$1E.SUCCESS : new p.X(){} [21]\n"
		+ "    22  aastore\n"
		+ "    23  putstatic p.X$1E.ENUM$VALUES : new p.X(){}[] [23]\n"
		+ "    26  return\n"
		+ "      Line numbers:\n"
		+ "        [pc: 0, line: 5]\n"
		+ "        [pc: 13, line: 4]\n"
		+ "  \n"
		+ "  // Method descriptor #20 (Ljava/lang/String;I)V\n"
		+ "  // Stack: 3, Locals: 3\n"
		+ "  private X$1E(java.lang.String arg0, int arg1);\n"
		+ "    0  aload_0 [this]\n"
		+ "    1  aload_1 [arg0]\n"
		+ "    2  iload_2 [arg1]\n"
		+ "    3  invokespecial java.lang.Enum(java.lang.String, int) [27]\n"
		+ "    6  return\n"
		+ "      Line numbers:\n"
		+ "        [pc: 0, line: 4]\n"
		+ "      Local variable table:\n"
		+ "        [pc: 0, pc: 7] local: this index: 0 type: new p.X(){}\n"
		+ "  \n"
		+ "  // Method descriptor #12 ()V\n"
		+ "  // Stack: 0, Locals: 1\n"
		+ "  public void run();\n"
		+ "    0  return\n"
		+ "      Line numbers:\n"
		+ "        [pc: 0, line: 6]\n"
		+ "      Local variable table:\n"
		+ "        [pc: 0, pc: 1] local: this index: 0 type: new p.X(){}\n"
		+ "  \n"
		+ "  // Method descriptor #31 ()[Lp/X$1E;\n"
		+ "  // Stack: 5, Locals: 3\n"
		+ "  public static new p.X(){}[] values();\n"
		+ "     0  getstatic p.X$1E.ENUM$VALUES : new p.X(){}[] [23]\n"
		+ "     3  dup\n"
		+ "     4  astore_0\n"
		+ "     5  iconst_0\n"
		+ "     6  aload_0\n"
		+ "     7  arraylength\n"
		+ "     8  dup\n"
		+ "     9  istore_1\n"
		+ "    10  anewarray p.X$1E [1]\n"
		+ "    13  dup\n"
		+ "    14  astore_2\n"
		+ "    15  iconst_0\n"
		+ "    16  iload_1\n"
		+ "    17  invokestatic java.lang.System.arraycopy(java.lang.Object, int, java.lang.Object, int, int) : void [32]\n"
		+ "    20  aload_2\n"
		+ "    21  areturn\n"
		+ "      Line numbers:\n"
		+ "        [pc: 0, line: 1]\n"
		+ "  \n"
		+ "  // Method descriptor #39 (Ljava/lang/String;)Lp/X$1E;\n"
		+ "  // Stack: 2, Locals: 1\n"
		+ "  public static new p.X(){} valueOf(java.lang.String arg0);\n"
		+ "     0  ldc <Class p.X$1E> [1]\n"
		+ "     2  aload_0 [arg0]\n"
		+ "     3  invokestatic java.lang.Enum.valueOf(java.lang.Class, java.lang.String) : java.lang.Enum [40]\n"
		+ "     6  checkcast p.X$1E [1]\n"
		+ "     9  areturn\n"
		+ "      Line numbers:\n"
		+ "        [pc: 0, line: 1]\n"
		+ "\n"
		+ "  Inner classes:\n"
		+ "    [inner class info: #1 p/X$1E, outer class info: #0\n"
		+ "     inner name: #54 E, accessflags: 17416 abstract static],\n"
		+ "    [inner class info: #14 p/X$1E$1, outer class info: #0\n"
		+ "     inner name: #0, accessflags: 16392 static]\n"
		+ "  Enclosing Method: #48  #50 p/X.main([Ljava/lang/String;)V\n"
		+ "\n"
		+ "Nest Host: #48 p/X\n"
		+ "}";

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"p" + File.separator + "X$1E.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test152() {
	this.runConformTest(
		new String[] {
				"Y.java",
				"public class Y {\n" +
				"	public static void main(String[] args) {\n" +
				"		enum E implements Runnable {\n" +
				"			SUCCESS {};\n" +
				"			public void run(){}\n" +
				"		}\n" +
				"		System.out.println(E.SUCCESS);\n" +
				"	}\n" +
				"}\n"
		},
		"SUCCESS",
		null,
		false,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109
public void test153() {
	this.runNegativeTest(
		new String[] {
				"Y.java",
				"public class Y {\n" +
				"	public static void main(String[] args) {\n" +
				"		enum TestEnum {\n" +
				"			RED, GREEN, BLUE; \n" +
				"    		static int test = 0;  \n" +
				"\n" +
				"    		TestEnum() {\n" +
				"        		TestEnum.test=10;\n" +
				"    		}\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 8)\n" +
		"	TestEnum.test=10;\n" +
		"	         ^^^^\n" +
		"Cannot refer to the static enum field TestEnum.test within an initializer\n" +
		"----------\n",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test154() {
	this.runNegativeTest(
		new String[] {
				"Y.java",
				"public class Y {\n" +
				"	public static void main(String[] args) {\n" +
				"		enum TestEnum2 {\n" +
				"			; \n" +
				"   		static int test = 0;  \n" +
				"			TestEnum2() {\n" +
				"        		TestEnum2.test=11;\n" +
				"   		}\n" +
				"		}\n" +
				"	}\n" +
				"}\n" +
				"class X {\n" +
				"	static int test = 0;\n" +
				"	X() {\n" +
				"		X.test = 13;\n" +
				"	}\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 7)\n" +
		"	TestEnum2.test=11;\n" +
		"	          ^^^^\n" +
		"Cannot refer to the static enum field TestEnum2.test within an initializer\n" +
		"----------\n",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test155() {
	this.runConformTest(
		new String[] {
				"Y.java",
				"public class Y {\n" +
				"	public static void main(String[] args) {\n" +
				"		enum TestEnum {\n" +
				"			RED, GREEN, BLUE; \n" +
				"    		static int test = 0;  \n" +
				"		}\n" +
				"\n" +
				"		enum TestEnum2 {\n" +
				"			; \n" +
				"    		TestEnum2() {\n" +
				"        		TestEnum.test=12;\n" +
				"    		}\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test156() {
	this.runConformTest(
		new String[] {
				"Y.java",
				"public class Y {\n" +
				"	public static void main(String[] args) {\n" +
				"		enum TestEnum {\n" +
				"			RED, GREEN, BLUE; \n" +
				"    		static int test = 0;  \n" +
				"\n" +
				"    		TestEnum() {\n" +
				"       		 new Object() {\n" +
				"				{ TestEnum.test=10; }\n" +
				"				};\n" +
				"			}\n" +
				"		}\n" +
				"    }\n" +
				"}\n"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test157() {
	this.runNegativeTest(
		new String[] {
				"Y.java",
				"public class Y {\n" +
				"	public static void main(String[] args) {\n" +
				"		enum Foo {\n" +
				"			ONE, TWO, THREE;\n" +
				"			static int val = 10;\n" +
				"			Foo () {\n" +
				"				this(Foo.val);\n" +
				"				System.out.println(Foo.val);\n" +
				"			}\n" +
				"			Foo(int i){}\n" +
				"			{\n" +
				"				System.out.println(Foo.val);\n" +
				"			}\n" +
				"			int field = Foo.val;\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 7)\n" +
		"	this(Foo.val);\n" +
		"	         ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"2. ERROR in Y.java (at line 8)\n" +
		"	System.out.println(Foo.val);\n" +
		"	                       ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"3. ERROR in Y.java (at line 12)\n" +
		"	System.out.println(Foo.val);\n" +
		"	                       ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"4. ERROR in Y.java (at line 14)\n" +
		"	int field = Foo.val;\n" +
		"	                ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test158() {
	this.runNegativeTest(
		new String[] {
				"Y.java",
				"public class Y {\n" +
				"	public static void main(String[] args) {\n" +
				"		enum Foo {\n" +
				"			ONE, TWO, THREE;\n" +
				"			static int val = 10;\n" +
				"			Foo () {\n" +
				"				this(val);\n" +
				"				System.out.println(val);\n" +
				"			}\n" +
				"			Foo(int i){}\n" +
				"			{\n" +
				"				System.out.println(val);\n" +
				"			}\n" +
				"			int field = val;\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 7)\n" +
		"	this(val);\n" +
		"	     ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"2. ERROR in Y.java (at line 8)\n" +
		"	System.out.println(val);\n" +
		"	                   ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"3. ERROR in Y.java (at line 12)\n" +
		"	System.out.println(val);\n" +
		"	                   ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"4. ERROR in Y.java (at line 14)\n" +
		"	int field = val;\n" +
		"	            ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test159() {
	this.runNegativeTest(
		new String[] {
				"Y.java",
				"public class Y {\n" +
				"	public static void main(String[] args) {\n" +
				"		enum Foo {\n" +
				"			ONE, TWO, THREE;\n" +
				"			static int val = 10;\n" +
				"			Foo () {\n" +
				"				this(get().val);\n" +
				"				System.out.println(get().val);\n" +
				"			}\n" +
				"			Foo(int i){}\n" +
				"			{\n" +
				"				System.out.println(get().val);\n" +
				"			}\n" +
				"			int field = get().val;\n" +
				"			Foo get() { return ONE; }\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 7)\n" +
		"	this(get().val);\n" +
		"	     ^^^\n" +
		"Cannot refer to an instance method while explicitly invoking a constructor\n" +
		"----------\n" +
		"2. WARNING in Y.java (at line 7)\n" +
		"	this(get().val);\n" +
		"	           ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"3. ERROR in Y.java (at line 7)\n" +
		"	this(get().val);\n" +
		"	           ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"4. WARNING in Y.java (at line 8)\n" +
		"	System.out.println(get().val);\n" +
		"	                         ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"5. ERROR in Y.java (at line 8)\n" +
		"	System.out.println(get().val);\n" +
		"	                         ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"6. WARNING in Y.java (at line 12)\n" +
		"	System.out.println(get().val);\n" +
		"	                         ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"7. ERROR in Y.java (at line 12)\n" +
		"	System.out.println(get().val);\n" +
		"	                         ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"8. WARNING in Y.java (at line 14)\n" +
		"	int field = get().val;\n" +
		"	                  ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"9. ERROR in Y.java (at line 14)\n" +
		"	int field = get().val;\n" +
		"	                  ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test160() {
	this.runNegativeTest(
		new String[] {
				"Y.java",
				"public class Y {\n" +
				"	public static void main(String[] args) {\n" +
				"	enum Foo {\n" +
				"		ONE, TWO, THREE;\n" +
				"		static int val = 10;\n" +
				"		Foo () {\n" +
				"			this(get().val = 1);\n" +
				"			System.out.println(get().val = 2);\n" +
				"		}\n" +
				"		Foo(int i){}\n" +
				"		{\n" +
				"			System.out.println(get().val = 3);\n" +
				"		}\n" +
				"		int field = get().val = 4;\n" +
				"		Foo get() { return ONE; }\n" +
				"	}\n" +
				"	}\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 7)\n" +
		"	this(get().val = 1);\n" +
		"	     ^^^\n" +
		"Cannot refer to an instance method while explicitly invoking a constructor\n" +
		"----------\n" +
		"2. WARNING in Y.java (at line 7)\n" +
		"	this(get().val = 1);\n" +
		"	           ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"3. ERROR in Y.java (at line 7)\n" +
		"	this(get().val = 1);\n" +
		"	           ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"4. WARNING in Y.java (at line 8)\n" +
		"	System.out.println(get().val = 2);\n" +
		"	                         ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"5. ERROR in Y.java (at line 8)\n" +
		"	System.out.println(get().val = 2);\n" +
		"	                         ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"6. WARNING in Y.java (at line 12)\n" +
		"	System.out.println(get().val = 3);\n" +
		"	                         ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"7. ERROR in Y.java (at line 12)\n" +
		"	System.out.println(get().val = 3);\n" +
		"	                         ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"8. WARNING in Y.java (at line 14)\n" +
		"	int field = get().val = 4;\n" +
		"	                  ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"9. ERROR in Y.java (at line 14)\n" +
		"	int field = get().val = 4;\n" +
		"	                  ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void _NA_test161() {
	this.runConformTest(
		new String[] {
				"LocalEnumTest1.java",
				"enum LocalEnumTest1 {\n" +
				"	;\n" +
				"	static int foo = LocalEnumTest2.bar;\n" +
				"}\n" +
				"enum LocalEnumTest2 {\n" +
				"	;\n" +
				"	static int bar = LocalEnumTest1.foo;\n" +
				"}\n"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239225
public void test162() {
	this.runNegativeTest(
			new String[] {
				"X.java", // =================
				"import java.util.HashMap;\n" +
				"import java.util.Map;\n" +
				"\n" +
				"public class X { \n" +
				"	public static void main(String[] args) {\n" +
				"		enum Status {\n" +
				"			GOOD((byte) 0x00), BAD((byte) 0x02);\n" +
				"\n" +
				"			private static Map<Byte, Status> mapping;\n" +
				"\n" +
				"			private Status(final byte newValue) {\n" +
				"\n" +
				"				if (Status.mapping == null) {\n" +
				"					Status.mapping = new HashMap<Byte, Status>();\n" +
				"				}\n" +
				"\n" +
				"				Status.mapping.put(newValue, this);\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}\n", // =================
			},
			"----------\n" +
			"1. ERROR in X.java (at line 13)\n" +
			"	if (Status.mapping == null) {\n" +
			"	           ^^^^^^^\n" +
			"Cannot refer to the static enum field Status.mapping within an initializer\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 14)\n" +
			"	Status.mapping = new HashMap<Byte, Status>();\n" +
			"	       ^^^^^^^\n" +
			"Cannot refer to the static enum field Status.mapping within an initializer\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 17)\n" +
			"	Status.mapping.put(newValue, this);\n" +
			"	       ^^^^^^^\n" +
			"Cannot refer to the static enum field Status.mapping within an initializer\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239225 - variation
public void test163() {
	this.runConformTest(
			new String[] {
				"X.java", // =================
				"import java.util.HashMap;\n" +
				"import java.util.Map;\n" +
				"\n" +
				"public class X { \n" +
				"	public static void main(String[] args) {\n" +
				"		enum Status {\n" +
				"			GOOD((byte) 0x00), BAD((byte) 0x02);\n" +
				"			private byte value;\n" +
				"			private static Map<Byte, Status> mapping;\n" +
				"			private Status(final byte newValue) {\n" +
				"				this.value = newValue;\n" +
				"			}\n" +
				"			static {\n" +
				"				Status.mapping = new HashMap<Byte, Status>();\n" +
				"				for (Status s : values()) {\n" +
				"					Status.mapping.put(s.value, s);\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}\n", // =================
			},
			"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251523
public void test164() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		enum Y {\n" +
			"			;\n" +
			"			private Y valueOf(String arg0) { return null; }\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	private Y valueOf(String arg0) { return null; }\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^\n" +
		"The enum Y already defines the method valueOf(String) implicitly\n" +
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251523 - variation
public void test165() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"class Other {\n" +
			"	int dupField;//1\n" +
			"	int dupField;//2\n" +
			"	int dupField;//3\n" +
			"	int dupField;//4\n" +
			"	void dupMethod(int i) {}//5\n" +
			"	void dupMethod(int i) {}//6\n" +
			"	void dupMethod(int i) {}//7\n" +
			"	void dupMethod(int i) {}//8\n" +
			"	void foo() {\n" +
			"		int i = dupMethod(dupField);\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		enum Y {\n" +
			"       	;\n" +
			"        	private Y valueOf(String arg0) { return null; }//9\n" +
			"        	private Y valueOf(String arg0) { return null; }//10\n" +
			"        	private Y valueOf(String arg0) { return null; }//11\n" +
			"       	void foo() {\n" +
			"        		int i = valueOf(\"\");\n" +
			"        	}\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int dupField;//1\n" +
		"	    ^^^^^^^^\n" +
		"Duplicate field Other.dupField\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	int dupField;//2\n" +
		"	    ^^^^^^^^\n" +
		"Duplicate field Other.dupField\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	int dupField;//3\n" +
		"	    ^^^^^^^^\n" +
		"Duplicate field Other.dupField\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 5)\n" +
		"	int dupField;//4\n" +
		"	    ^^^^^^^^\n" +
		"Duplicate field Other.dupField\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 6)\n" +
		"	void dupMethod(int i) {}//5\n" +
		"	     ^^^^^^^^^^^^^^^^\n" +
		"Duplicate method dupMethod(int) in type Other\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 7)\n" +
		"	void dupMethod(int i) {}//6\n" +
		"	     ^^^^^^^^^^^^^^^^\n" +
		"Duplicate method dupMethod(int) in type Other\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 8)\n" +
		"	void dupMethod(int i) {}//7\n" +
		"	     ^^^^^^^^^^^^^^^^\n" +
		"Duplicate method dupMethod(int) in type Other\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 9)\n" +
		"	void dupMethod(int i) {}//8\n" +
		"	     ^^^^^^^^^^^^^^^^\n" +
		"Duplicate method dupMethod(int) in type Other\n" +
		"----------\n" +
		"9. ERROR in X.java (at line 11)\n" +
		"	int i = dupMethod(dupField);\n" +
		"	        ^^^^^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from void to int\n" +
		"----------\n" +
		"10. ERROR in X.java (at line 19)\n" +
		"	private Y valueOf(String arg0) { return null; }//9\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^\n" +
		"The enum Y already defines the method valueOf(String) implicitly\n" +
		"----------\n" +
		"11. ERROR in X.java (at line 20)\n" +
		"	private Y valueOf(String arg0) { return null; }//10\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^\n" +
		"The enum Y already defines the method valueOf(String) implicitly\n" +
		"----------\n" +
		"12. ERROR in X.java (at line 21)\n" +
		"	private Y valueOf(String arg0) { return null; }//11\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^\n" +
		"The enum Y already defines the method valueOf(String) implicitly\n" +
		"----------\n" +
		"13. ERROR in X.java (at line 23)\n" +
		"	int i = valueOf(\"\");\n" +
		"	        ^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Y to int\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251814
public void test166() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		enum Y {\n" +
			"        	;\n" +
			"       	private int valueOf(String arg0) { return 0; }//11\n" +
			"        	void foo() {\n" +
			"        		int i = valueOf(\"\");\n" +
			"        	}\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	private int valueOf(String arg0) { return 0; }//11\n" +
		"	            ^^^^^^^^^^^^^^^^^^^^\n" +
		"The enum Y already defines the method valueOf(String) implicitly\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	int i = valueOf(\"\");\n" +
		"	        ^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Y to int\n" +
		"----------\n",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	// check for presence of #valueOf(...) in problem type
	String expectedPartialOutput =
		"  public static void main(java.lang.String[] arg0);\n" +
		"     0  new java.lang.Error [16]\n" +
		"     3  dup\n" +
		"     4  ldc <String \"Unresolved compilation problems: \\n\\tThe enum Y already defines the method valueOf(String) implicitly\\n\\tType mismatch: cannot convert from Y to int\\n\"> [18]\n" +
		"     6  invokespecial java.lang.Error(java.lang.String) [20]\n" +
		"     9  athrow\n" ;
	verifyClassFile(expectedPartialOutput, "X.class", ClassFileBytesDisassembler.SYSTEM, true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251814 - variation
public void test167() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		enum Y {\n" +
			"    		;\n" +
			"    		static int valueOf(String arg0) { return 0; }//9\n" +
			"   		void foo() {\n" +
			"    			int i = Y.valueOf(\"\");\n" +
			"   		}\n" +
			"		}\n" +
			"		class Other {\n" +
			"    		void foo() {\n" +
			"    			int i = Y.valueOf(\"\");\n" +
			"    		}\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	static int valueOf(String arg0) { return 0; }//9\n" +
		"	           ^^^^^^^^^^^^^^^^^^^^\n" +
		"The enum Y already defines the method valueOf(String) implicitly\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	int i = Y.valueOf(\"\");\n" +
		"	        ^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Y to int\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	int i = Y.valueOf(\"\");\n" +
		"	        ^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Y to int\n" +
		"----------\n",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=255452
public void test168() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		enum BadEnum {\n" +
			"    		CRAZY(CRAZY), // <-- illegal forward reference reported by all compilers\n" +
			"    		IMPOSSIBLE(BadEnum.IMPOSSIBLE); // <-- illegal forward reference (javac 1.6 only)\n" +
			"    		private BadEnum(BadEnum self) {\n" +
			"    		}\n" +
			"		}\n" +
			"		class A {\n" +
			"    		A x1 = new A(x1);//1 - WRONG\n" +
			"    		static A X2 = new A(A.X2);//2 - OK\n" +
			"    		A x3 = new A(this.x3);//3 - OK\n" +
			"    		A(A x) {}\n" +
			"    		A(int i) {}\n" +
			"    		int VALUE() { return 13; }\n" +
			"    		int value() { return 14; }\n" +
			"		}\n" +
			"		class Y extends A {\n" +
			"    		A x1 = new A(x1);//6 - WRONG\n" +
			"    		static A X2 = new A(Y.X2);//7 - OK\n" +
			"    		A x3 = new A(this.x3);//8 - OK\n" +
			"    		Y(Y y) { super(y); }\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	CRAZY(CRAZY), // <-- illegal forward reference reported by all compilers\n" +
		"	      ^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	IMPOSSIBLE(BadEnum.IMPOSSIBLE); // <-- illegal forward reference (javac 1.6 only)\n" +
		"	                   ^^^^^^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	A x1 = new A(x1);//1 - WRONG\n" +
		"	             ^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 19)\n" +
		"	A x1 = new A(x1);//6 - WRONG\n" +
		"	  ^^\n" +
		"The field Y.x1 is hiding a field from type A\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 19)\n" +
		"	A x1 = new A(x1);//6 - WRONG\n" +
		"	             ^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 20)\n" +
		"	static A X2 = new A(Y.X2);//7 - OK\n" +
		"	         ^^\n" +
		"The field Y.X2 is hiding a field from type A\n" +
		"----------\n" +
		"7. WARNING in X.java (at line 21)\n" +
		"	A x3 = new A(this.x3);//8 - OK\n" +
		"	  ^^\n" +
		"The field Y.x3 is hiding a field from type A\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=255452 - variation
public void test169() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		enum BadEnum {\n" +
			"    		NOWAY(BadEnum.NOWAY.CONST),\n" +
			"    		INVALID(INVALID.CONST),\n" +
			"    		WRONG(WRONG.VALUE()),\n" +
			"    		ILLEGAL(ILLEGAL.value());\n" +
			"    		final static int CONST = 12;\n" +
			"    		private BadEnum(int i) {\n" +
			"    		}\n" +
			"    		int VALUE() { return 13; }\n" +
			"    		int value() { return 14; }\n" +
			"		}\n" +
			"		class Y {\n" +
			"    		final static int CONST = 12;\n" +
			"    		Y x4 = new Y(x4.CONST);//4 - WRONG\n" +
			"    		Y x5 = new Y(x5.value());//5 - WRONG\n" +
			"   		Y(int i) {}\n" +
			"    		int VALUE() { return 13; }\n" +
			"    		int value() { return 14; }\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	NOWAY(BadEnum.NOWAY.CONST),\n" +
		"	              ^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	NOWAY(BadEnum.NOWAY.CONST),\n" +
		"	                    ^^^^^\n" +
		"The static field BadEnum.CONST should be accessed in a static way\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 5)\n" +
		"	INVALID(INVALID.CONST),\n" +
		"	        ^^^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 5)\n" +
		"	INVALID(INVALID.CONST),\n" +
		"	                ^^^^^\n" +
		"The static field BadEnum.CONST should be accessed in a static way\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 6)\n" +
		"	WRONG(WRONG.VALUE()),\n" +
		"	      ^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 7)\n" +
		"	ILLEGAL(ILLEGAL.value());\n" +
		"	        ^^^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 16)\n" +
		"	Y x4 = new Y(x4.CONST);//4 - WRONG\n" +
		"	             ^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"8. WARNING in X.java (at line 16)\n" +
		"	Y x4 = new Y(x4.CONST);//4 - WRONG\n" +
		"	                ^^^^^\n" +
		"The static field Y.CONST should be accessed in a static way\n" +
		"----------\n" +
		"9. ERROR in X.java (at line 17)\n" +
		"	Y x5 = new Y(x5.value());//5 - WRONG\n" +
		"	             ^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=263877
public void test170() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"public class X {\n" +
			"   public static final int FOO = X.OFFSET + 0;\n" +
			"   public static final int BAR = OFFSET + 1;\n" +
			"   public static final int OFFSET = 0;  // cannot move this above, else more errors\n" +
			"	public static void main(String[] args) {\n" +
			"		enum Days {\n" +
			"    		Monday(\"Mon\", Days.OFFSET + 0),    // should not complain\n" +
			"    		Tuesday(\"Tue\", Days.Wednesday.hashCode()),   // should complain since enum constant\n" +
			"    		Wednesday(\"Wed\", OFFSET + 2);   // should complain since unqualified\n" +
			"    		public static final int OFFSET = 0;  // cannot move this above, else more errors\n" +
			"   		Days(String abbr, int index) {\n" +
			"    		}\n" +
			"		}\n" +
			"\n" +
			"	}\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public static final int BAR = OFFSET + 1;\n" +
		"	                              ^^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	Tuesday(\"Tue\", Days.Wednesday.hashCode()),   // should complain since enum constant\n" +
		"	                    ^^^^^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	Wednesday(\"Wed\", OFFSET + 2);   // should complain since unqualified\n" +
		"	                 ^^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 10)\n" +
		"	public static final int OFFSET = 0;  // cannot move this above, else more errors\n" +
		"	                        ^^^^^^\n" +
		"The field Days.OFFSET is hiding a field from type X\n" +
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=267670. Make sure we don't emit any unused
// warnings about enumerators. Since these could be used in indirect ways not obvious.
public void test171() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"    	enum Colors {\n" +
			"	    	BLEU,\n" +
			"	    	BLANC,\n" +
			"	     	ROUGE\n" +
			"	 	}\n" +
			"		for (Colors c: Colors.values()) {\n" +
			"           System.out.print(c);\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		null, options,
		"",
		"BLEUBLANCROUGE", null, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=267670. Make sure we don't emit any unused
// warnings about enumerators. Since these could be used in indirect ways not obvious. This
// test also verifies that while we don't complain about individual enumerators not being used
// we DO complain if the enumeration type itself is not used.
public void test172() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"   	enum Greet {\n" +
			"	    	HELLO, HOWDY, BONJOUR; \n" +
			"   	}\n" +
			"		enum Colors {\n" +
			"       	RED, BLACK, BLUE;\n"+
			"   	}\n" +
            "   	enum Complaint {" +
            "       	WARNING, ERROR, FATAL_ERROR, PANIC;\n" +
            "   	}\n" +
			"		Greet g = Greet.valueOf(\"HELLO\");\n" +
		    "		System.out.print(g);\n" +
		    "       Colors c = Enum.valueOf(Colors.class, \"RED\");\n" +
		    "		System.out.print(c);\n" +
		    "   }\n" +
			"}\n"
		},
		null, customOptions,
		"----------\n" +
		"1. WARNING in X.java (at line 9)\n" +
		"	enum Complaint {       	WARNING, ERROR, FATAL_ERROR, PANIC;\n" +
		"	     ^^^^^^^^^\n" +
		"The type Complaint is never used locally\n" +
		"----------\n",
		"HELLORED", null,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=273990
public void test173() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		enum E {\n" +
			"			A(E.STATIK);\n" +
			"			private static int STATIK = 1;\n" +
			"			private E(final int i) {}\n" +
			"		}\n" +
			"		enum E2 {\n" +
			"			A(E2.STATIK);\n" +
			"			static int STATIK = 1;\n" +
			"			private E2(final int i) {}\n" +
			"		}\n" +
			"   }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	A(E.STATIK);\n" +
		"	    ^^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	A(E2.STATIK);\n" +
		"	     ^^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=278562
public void test174() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.HashMap;\n" +
			"import java.util.Map;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) throws Exception {\n" +
			"		interface S {}\n" +
			"		enum A implements S {\n" +
			"			L;\n" +
			"		}\n" +
			"		Enum<? extends S> enumConstant = A.L;\n" +
			"		Map<String, Enum> m = new HashMap<>();\n" +
			"		for (Enum e : enumConstant.getDeclaringClass().getEnumConstants()) {\n" +
			"			m.put(e.name(), e);\n" +
			"		}\n" +
			"		System.out.print(1);\n" +
			"	}\n" +
			"}\n"
		},
		"1"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=278562
public void test175() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.HashMap;\n" +
			"import java.util.Map;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) throws Exception {\n" +
			"		interface S {}\n" +
			"		enum A implements S {\n" +
			"			L, M, N, O;\n" +
			"		}\n" +
			"		Enum[] tab = new Enum[] {A.L, A.M, A.N, A.O};\n" +
			"		Map m = new HashMap();\n" +
			"		for (Enum s : tab) {\n" +
			"			m.put(s.name(), s);\n" +
			"		}\n" +
			"		System.out.print(1);\n" +
			"	}\n" +
			"}\n"
		},
		"1"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285701
public void test176() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) throws Exception {\n" +
			"		enum Y {\n" +
			"			A(\"\"), B(\"SUCCESS\"), C(\"Hello\");\n" +
			"\n" +
			"			String message;\n" +
			"\n" +
			"			Y(@Deprecated String s) {\n" +
			"				this.message = s;\n" +
			"			}\n" +
			"			@Override\n" +
			"			public String toString() {\n" +
			"				return this.message;\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		""
	);
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) throws Exception {\n" +
				"		enum Y {\n" +
				"			A(\"\"), B(\"SUCCESS\"), C(\"Hello\");\n" +
				"\n" +
				"			String message;\n" +
				"\n" +
				"			Y(@Deprecated String s) {\n" +
				"				this.message = s;\n" +
				"			}\n" +
				"			@Override\n" +
				"			public String toString() {\n" +
				"				return this.message;\n" +
				"			}\n" +
				"		}\n" +
				"		System.out.println(Y.B);\n" +
				"	}\n" +
				"}\n"
		},
		null,
		options,
		"",
		"SUCCESS",
		"",
		JavacTestOptions.Excuse.JavacHasWarningsEclipseNotConfigured);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285701
public void test177() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) throws Exception {\n" +
			"		enum Y {\n" +
			"			A(\"\", 0, \"A\"), B(\"SUCCESS\", 0, \"B\"), C(\"Hello\", 0, \"C\");\n" +
			"\n" +
			"			private String message;\n" +
			"			private int index;\n" +
			"			private String name;\n" +
			"\n" +
			"			Y(@Deprecated String s, int i, @Deprecated String name) {\n" +
			"				this.message = s;\n" +
			"				this.index = i;\n" +
			"				this.name = name;\n" +
			"			}\n" +
			"			@Override\n" +
			"			public String toString() {\n" +
			"				return this.message + this.name;\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		""
	);
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) throws Exception {\n" +
			"		enum Y {\n" +
			"			A(\"\", 0, \"A\"), B(\"SUCCESS\", 0, \"B\"), C(\"Hello\", 0, \"C\");\n" +
			"\n" +
			"			private String message;\n" +
			"			private int index;\n" +
			"			private String name;\n" +
			"\n" +
			"			Y(@Deprecated String s, int i, @Deprecated String name) {\n" +
			"				this.message = s;\n" +
			"				this.index = i;\n" +
			"				this.name = name;\n" +
			"			}\n" +
			"			@Override\n" +
			"			public String toString() {\n" +
			"				return this.message + this.name;\n" +
			"			}\n" +
			"		}\n" +
			"		System.out.println(Y.B);\n" +
			"	}\n" +
			"}\n"
		},
		null,
		options,
		"",
		"SUCCESSB",
		"",
		JavacTestOptions.Excuse.JavacHasWarningsEclipseNotConfigured);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285701
// static local enums are not allowed. Removing static is the same as test177
public void _NA_test178() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static enum Y {\n" +
			"		A(\"\", 0, \"A\"), B(\"SUCCESS\", 0, \"B\"), C(\"Hello\", 0, \"C\");\n" +
			"		\n" +
			"		private String message;\n" +
			"		private int index;\n" +
			"		private String name;\n" +
			"		Y(@Deprecated String s, int i, @Deprecated String name) {\n" +
			"			this.message = s;\n" +
			"			this.index = i;\n" +
			"			this.name = name;\n" +
			"		}\n" +
			"		@Override\n" +
			"		public String toString() {\n" +
			"			return this.message + this.name;\n" +
			"		}\n" +
			"	}\n" +
			"}"
		},
		""
	);
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
			"Z.java",
			"public class Z {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(X.Y.B);\n" +
			"	}\n" +
			"}"
		},
		null,
		options,
		"",
		"SUCCESSB",
		"",
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285701
public void test179() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
			"Z.java",
			"public class Z {\n" +
			"	public static void main(String[] args) {\n" +
			"		class X {\n" +
			"			enum Y {\n" +
			"				A(\"\", 0, \"A\"), B(\"SUCCESS\", 0, \"B\"), C(\"Hello\", 0, \"C\");\n" +
			"		\n" +
			"				private String message;\n" +
			"				private int index;\n" +
			"				private String name;\n" +
			"				Y(@Deprecated String s, int i, @Deprecated String name) {\n" +
			"					this.message = s;\n" +
			"					this.index = i;\n" +
			"					this.name = name;\n" +
			"				}\n" +
			"				@Override\n" +
			"				public String toString() {\n" +
			"					return this.message + this.name;\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"		System.out.println(X.Y.B);\n" +
			"	}\n" +
			"}"
		},
		null,
		options,
		"",
		"SUCCESSB",
		"",
		JavacTestOptions.Excuse.JavacHasWarningsEclipseNotConfigured);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=289892
public void test180() {
	this.runConformTest(
		new String[] {
			"p/package-info.java",
			"@p.Annot(state=p.MyEnum.BROKEN)\n" +
			"package p;",
			"p/Annot.java",
			"package p;\n" +
			"@Annot(state=MyEnum.KO)\n" +
			"public @interface Annot {\n" +
			"	MyEnum state() default MyEnum.KO;\n" +
			"}",
			"p/MyEnum.java",
			"package p;\n" +
			"@Annot(state=MyEnum.KO)\n" +
			"public enum MyEnum {\n" +
			"	WORKS, OK, KO, BROKEN, ;\n" +
			"}",
		},
		""
	);
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
			"X.java",
			"import p.MyEnum;\n" +
			"import p.Annot;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		@Annot(state=MyEnum.KO)\n" +
			"		enum LocalEnum {\n" +
			"			A, B, ;\n" +
			"		}\n" +
			"		System.out.print(LocalEnum.class);\n" +
			"	}\n" +
			"}"
		},
		null,
		options,
		"",
		"class X$1LocalEnum",
		"",
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=289892
// in interaction with null annotations
// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365519#c4 item (6)
public void test180a() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
	this.runConformTest(
		new String[] {
			"p/package-info.java",
			"@p.Annot(state=p.MyEnum.BROKEN)\n" +
			"package p;",
			"p/Annot.java",
			"package p;\n" +
			"@Annot(state=MyEnum.KO)\n" +
			"public @interface Annot {\n" +
			"	MyEnum state() default MyEnum.KO;\n" +
			"}",
			"p/MyEnum.java",
			"package p;\n" +
			"@Annot(state=MyEnum.KO)\n" +
			"public enum MyEnum {\n" +
			"	WORKS, OK, KO, BROKEN, ;\n" +
			"}",
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
			"X.java",
			"import p.MyEnum;\n" +
			"import p.Annot;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		@Annot(state=MyEnum.OK)\n" +
			"		enum LocalEnum {\n" +
			"			A, B, ;\n" +
			"		}\n" +
			"		System.out.print(LocalEnum.class);\n" +
			"	}\n" +
			"}"
		},
		null,
		options,
		"",
		"class X$1LocalEnum",
		"",
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=300133
public void test181() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(
		new String[] {
				"B.java",
				"public class B {\n" +
				"	enum X {\n" +
				"		A {\n" +
				"			@Override\n" +
				"			public Object foo(final String s) {\n" +
				"				class Local {\n" +
				"					public String toString() {\n" +
				"						return s;\n" +
				"					}\n" +
				"				}\n" +
				"				return new Local();\n" +
				"			}\n" +
				"		};\n" +
				"		public abstract Object foo(String s);\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		 System.out.println(X.A.foo(\"SUCCESS\"));\n" +
				"	}\n" +
				"}"
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test182() throws Exception {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String argv[])   {\n" +
			"		foo();\n" +
			"	}\n" +
			"	public static void foo() {\n" +
			"		enum E {\n" +
			"			a1(1), a2(2);\n" +
			"			static int[] VALUES = { 1, 2 };\n" +
			"			private int value;\n" +
			"			E(int v) {\n" +
			"				this.value = v;\n" +
			"			}\n" +
			"			public int val() {\n" +
			"				return this.value;\n" +
			"			}\n" +
			"		}\n" +
			"		int n = 0;\n" +
			"		for (E e : E.values()) {\n" +
			"			if (e.val() == E.VALUES[n++] ) {\n" +
			"				System.out.print(e.val());\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}"
		},
		"12",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test183() throws Exception {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String argv[]) {\n" +
			"	}\n" +
			"	static {\n" +
			"		enum E {\n" +
			"			a1(1), a2(2);\n" +
			"			static int[] VALUES = { 1, 2 };\n" +
			"			private int value;\n" +
			"			E(int v) {\n" +
			"				this.value = v;\n" +
			"			}\n" +
			"			public int val() {\n" +
			"				return this.value;\n" +
			"			}\n" +
			"		}\n" +
			"		int n = 0;\n" +
			"		for (E e : E.values()) {\n" +
			"			if (e.val() == E.VALUES[n++] ) {\n" +
			"				System.out.print(e.val());\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}"

		},
		"12",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test184() throws Exception {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String argv[]) {\n" +
			"		new X();\n" +
			"	}\n" +
			"	X() {\n" +
			"		enum E {\n" +
			"			a1(1), a2(2);\n" +
			"			static int[] VALUES = { 1, 2 };\n" +
			"			private int value;\n" +
			"			E(int v) {\n" +
			"				this.value = v;\n" +
			"			}\n" +
			"			public int val() {\n" +
			"				return this.value;\n" +
			"			}\n" +
			"		}\n" +
			"		int n = 0;\n" +
			"		for (E e : E.values()) {\n" +
			"			if (e.val() == E.VALUES[n++] ) {\n" +
			"				System.out.print(e.val());\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}"
		},
		"12",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=
public void test185() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X{\n" +
			"	public static void main(String argv[]) {\n" +
			"		enum Y{\n" +
			"  			A, B;\n" +
			"  			private Y() throws Exception {\n" +
			"  			}\n" +
			"		}\n"+
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	A, B;\n" +
		"	^\n" +
		"Unhandled exception type Exception\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	A, B;\n" +
		"	   ^\n" +
		"Unhandled exception type Exception\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test186() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_MISSING_ENUM_CASE_DESPITE_DEFAULT, JavaCore.ENABLED);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"    void _test(boolean val) {\n" +
			"		 enum X {\n" +
			"		   A, B;\n" +
			"		 }\n" +
			"		 X x= val? X.A : X.B;\n" +
			"        switch (x) {\n" +
			"			case A: System.out.println(\"A\"); break;\n" +
			" 			default : System.out.println(\"unknown\"); break;\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in Y.java (at line 7)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The enum constant B should have a corresponding case label in this enum switch on X. To suppress this problem, add a comment //$CASES-OMITTED$ on the line above the 'default:'\n" +
		"----------\n",
		null, // classlibs
		true, // flush
		options, // customOptions
		false /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false /* do not skip javac for this peculiar test */,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		false /* performStatementsRecovery */
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test187() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_MISSING_ENUM_CASE_DESPITE_DEFAULT, JavaCore.ENABLED);
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.ERROR);
	this.runConformTest(
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"    void _test(boolean val) {\n" +
			"		 enum X {\n" +
			"		   A, B;\n" +
			"		 }\n" +
			"		 X x= val? X.A : X.B;\n" +
			"        switch (x) {\n" +
			"			case A: System.out.println(\"A\");\n" +
			"           //$FALL-THROUGH$\n" +
			"           //$CASES-OMITTED$\n" +
			" 			default : System.out.println(\"unknown\"); break;\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"",
		null, // classlibs
		true, // flush
		null, // vmArgs
		options,
		null /*requestor*/);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test187a() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_MISSING_ENUM_CASE_DESPITE_DEFAULT, JavaCore.ENABLED);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"    void _test(boolean val) {\n" +
			"		 enum X {\n" +
			"		   A, B;\n" +
			"		 }\n" +
			"		 X x= val? X.A : X.B;\n" +
			"        switch (x) {\n" +
			"			case A: System.out.println(\"A\"); break;\n" +
			"           //$CASES-OMITTED$\n" + // not strong enough to suppress the warning if default: is missing
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in Y.java (at line 7)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The enum constant B needs a corresponding case label in this enum switch on X\n" +
		"----------\n",
		null, // classlibs
		true, // flush
		options, // customOptions
		false /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false /* do not skip javac for this peculiar test */,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		false /* performStatementsRecovery */
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test187b() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.ERROR);
	options.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
	this.runConformTest(
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"    @SuppressWarnings(\"incomplete-switch\")\n" +
			"    void _test(boolean val) {\n" +
			"		 enum X {\n" +
			"		   A, B;\n" +
			"		 }\n" +
			"		 X x= val? X.A : X.B;\n" +
			"        switch (x) {\n" +
			"			case A: System.out.println(\"A\"); break;\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"",
		null, // classlibs
		true, // flush
		null, // vmArgs
		options,
		null /*requestor*/);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test188() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"    void _test(boolean val) {\n" +
			"		 enum X {\n" +
			"		   A, B;\n" +
			"		 }\n" +
			"		 X x= val? X.A : X.B;\n" +
			"        switch (x) {\n" +
			"			case A: System.out.println(\"A\"); break;\n" +
			"			case B: System.out.println(\"B\"); break;\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in Y.java (at line 7)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The switch over the enum type X should have a default case\n" +
		"----------\n",
		null, // classlibs
		true, // flush
		options, // customOptions
		false /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false /* do not skip javac for this peculiar test */,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		false /* performStatementsRecovery */
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test189() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	//options.put(JavaCore.COMPILER_PB_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"    public int test(boolean val) {\n" +
			"		 enum X {\n" +
			"		   A, B;\n" +
			"		 }\n" +
			"		 X x= val? X.A : X.B;\n" +
			"        switch (x) {\n" +
			"			case A: return 1;\n" +
			"			case B: return 2;\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 2)\n" +
		"	public int test(boolean val) {\n" +
		"	           ^^^^^^^^^^^^^^^^^\n" +
		"This method must return a result of type int. Note that a problem regarding missing 'default:' on 'switch' has been suppressed, which is perhaps related to this problem\n" +
		"----------\n",
		null, // classlibs
		true, // flush
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433060 [1.8][compiler] enum E<T>{I;} causes NPE in AllocationExpression.checkTypeArgumentRedundancy
public void test433060() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_REDUNDANT_TYPE_ARGUMENTS, JavaCore.ERROR);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"public class Y{\n" +
			"	public static void main(String argv[]) {\n" +
			"		enum X<T> {\n" +
			"			OBJ;\n" +
			"		}\n" +
			"	}\n" +
			"}"

		},
		"----------\n" +
		"1. ERROR in Y.java (at line 3)\n" +
		"	enum X<T> {\n" +
		"	       ^\n" +
		"Syntax error, enum declaration cannot have type parameters\n" +
		"----------\n",
		null,
		true,
		options);
}
public void test434442() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(new String[] {
			"Y.java",
			"public class Y {\n" +
			"  public static void main(String[] args) {\n" +
			"		enum Letter {\n" +
			"  			A, B;\n" +
			"		}\n" +
			"		interface I {\n" +
			"  			public default void test(Letter letter) {\n" +
			"    			switch (letter) {\n" +
			"      				case A:\n" +
			"        				System.out.print(\"A\");\n" +
			"        				break;\n" +
			"      				case B:\n" +
			"        				System.out.print(\"B\");\n" +
			"        				break;\n" +
			"    			}\n" +
			"  			}\n" +
			"		}\n" +
			"		class X implements I {\n" +
			"  		}\n" +
			"		try{\n" +
			"			X x = new X();\n" +
			"			x.test(Letter.A);\n" +
			"	  	}\n" +
			"    	catch (Exception e) {\n" +
			"      		e.printStackTrace();\n" +
			"    	}\n" +
			"  }\n" +
			"} \n" +
			"\n"
	}, "A");
}
public void test476281() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(new String[] {
			"Y.java",
			"public class Y {\n" +
			"  public static void main(String[] args) {\n" +
			"		enum LambdaEnumLocalClassBug {\n" +
			"  			A(() -> {\n" +
			"    			class Foo {\n" +
			"    			}\n" +
			"    			new Foo();\n" +
			"    			System.out.println(\"Success\");\n" +
			"  			})\n" +
			"			;\n" +
			"  			private final Runnable runnable;\n" +
			"  			private LambdaEnumLocalClassBug(Runnable runnable) {\n" +
			"    			this.runnable = runnable;\n" +
			"  			}\n" +
			"		}\n" +
			"    	LambdaEnumLocalClassBug.A.runnable.run();\n" +
			"  }\n" +
			"} \n"
			},
			"Success");
}
public void test476281a() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(new String[] {
			"Y.java",
			"public class Y {\n" +
			"  public static void main(String[] args) {\n" +
			"		enum Test {\n" +
			"  			B(new Runnable() {\n" +
			"				public void run() {\n" +
			"					//\n" +
			"					class Foo {\n" +
			"					\n" +
			"					}\n" +
			"					new Foo();\n" +
			"   		 		System.out.println(\"Success\");\n" +
			"				}\n" +
			"			});\n" +
			"  			private final Runnable runnable;\n" +
			"  			private Test(Runnable runnable) {\n" +
			"    			this.runnable = runnable;\n" +
			"  			}\n" +
			"		}\n" +
			"    	Test.B.runnable.run();\n" +
			"  }\n" +
			"} \n"
			},
			"Success");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=566758
public void test566758() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	<T> void m(T t) {\n" +
			"		interface Y {\n" +
			"			T foo(); // T should not be allowed\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	T foo(); // T should not be allowed\n" +
		"	^\n" +
		"Cannot make a static reference to the non-static type T\n" +
		"----------\n",
		null,
		true);
}
}
