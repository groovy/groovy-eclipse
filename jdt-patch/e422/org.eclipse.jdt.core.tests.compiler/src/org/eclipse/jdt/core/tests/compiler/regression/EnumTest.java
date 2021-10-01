/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class EnumTest extends AbstractComparableTest {

	String reportMissingJavadocComments = null;

	public EnumTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test187" };
//		TESTS_NUMBERS = new int[] { 185 };
//		TESTS_RANGE = new int[] { 21, 50 };
	}
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return EnumTest.class;
	}

	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
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

// test simple valid enum and its usage
public void test000() {
	runConformTest(
		new String[] {
			"e/X.java",
			"package e;\n" +
				"import e.T;\n" +
				"import static e.T.*;\n" +
				"\n" +
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"    	System.out.print(\"JDTCore team:\");\n" +
				"    	T oldest = null;\n" +
				"    	int maxAge = Integer.MIN_VALUE;\n" +
				"    	for (T t : T.values()) {\n" +
				"            if (t == YODA) continue;// skip YODA\n" +
				"            t.setRole(t.isManager());\n" +
				"			 if (t.age() > maxAge) {\n" +
				"               oldest = t;\n" +
				"               maxAge = t.age();\n" +
				"            }\n" +
				"            System.out.print(\" \"+ t + ':'+t.age()+':'+location(t)+':'+t.role);\n" +
				"        }\n" +
				"        System.out.println(\" WINNER is:\" + T.valueOf(oldest.name()));\n" +
				"    }\n" +
				"\n" +
				"   private enum Location { SNZ, OTT }\n" +
				"\n" +
				"    private static Location location(T t) {\n" +
				"        switch(t) {\n" +
				"          case PHILIPPE:  \n" +
				"          case DAVID:\n" +
				"          case JEROME:\n" +
				"          case FREDERIC:\n" +
				"          	return Location.SNZ;\n" +
				"          case OLIVIER:\n" +
				"          case KENT:\n" +
				"            return Location.OTT;\n" +
				"          default:\n" +
				"            throw new AssertionError(\"Unknown team member: \" + t);\n" +
				"        }\n" +
				"    }\n" +
				"}\n",
			"e/T.java",
			"package e;\n" +
				"public enum T {\n" +
				"	PHILIPPE(37) {\n" +
				"		public boolean isManager() {\n" +
				"			return true;\n" +
				"		}\n" +
				"	},\n" +
				"	DAVID(27),\n" +
				"	JEROME(33),\n" +
				"	OLIVIER(35),\n" +
				"	KENT(40),\n" +
				"	YODA(41),\n" +
				"	FREDERIC;\n" +
				"	final static int OLD = 41;\n" +
				"\n" +
				"   enum Role { M, D }\n" +
				"\n" +
				"   int age;\n" +
				"	Role role;\n" +
				"\n" +
				"	T() { this(OLD); }\n" +
				"	T(int age) {\n" +
				"		this.age = age;\n" +
				"	}\n" +
				"	public int age() { return this.age; }\n" +
				"	public boolean isManager() { return false; }\n" +
				"	void setRole(boolean mgr) {\n" +
				"		this.role = mgr ? Role.M : Role.D;\n" +
				"	}\n" +
				"}\n"
		},
		"JDTCore team: PHILIPPE:37:SNZ:M DAVID:27:SNZ:D JEROME:33:SNZ:D OLIVIER:35:OTT:D KENT:40:OTT:D FREDERIC:41:SNZ:D WINNER is:FREDERIC"
	);
}
// check assignment to enum constant is disallowed
public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X { \n" +
			"	BLEU, \n" +
			"	BLANC, \n" +
			"	ROUGE;\n" +
			"	static {\n" +
			"		BLEU = null;\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	BLEU = null;\n" +
		"	^^^^\n" +
		"The final field X.BLEU cannot be assigned\n" +
		"----------\n");
}
// check diagnosis for duplicate enum constants
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X { \n" +
			"	\n" +
			"	BLEU, \n" +
			"	BLANC, \n" +
			"	ROUGE,\n" +
			"	BLEU;\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	BLEU, \n" +
		"	^^^^\n" +
		"Duplicate field X.BLEU\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	BLEU;\n" +
		"	^^^^\n" +
		"Duplicate field X.BLEU\n" +
		"----------\n");
}
// check properly rejecting enum constant modifiers
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X { \n" +
			"	\n" +
			"	public BLEU, \n" +
			"	transient BLANC, \n" +
			"	ROUGE, \n" +
			"	abstract RED {\n" +
			"		void test() {}\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public BLEU, \n" +
		"	       ^^^^\n" +
		"Illegal modifier for the enum constant BLEU; no modifier is allowed\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	transient BLANC, \n" +
		"	          ^^^^^\n" +
		"Illegal modifier for the enum constant BLANC; no modifier is allowed\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
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
			"public enum X { \n" +
			"	\n" +
			"	BLEU,\n" +
			"	BLANC,\n" +
			"	ROUGE;\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(BLEU);\n" +
			"	}\n" +
			"	\n" +
			"}\n"
		},
		"BLEU");
}
// check method override diagnosis (with no enum constants)
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X { \n" +
			"	;\n" +
			"	protected Object clone() { return this; }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	protected Object clone() { return this; }\n" +
		"	                 ^^^^^^^\n" +
		"Cannot override the final method from Enum<X>\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 3)\n" +
		"	protected Object clone() { return this; }\n" +
		"	                 ^^^^^^^\n" +
		"The method clone() of type X should be tagged with @Override since it actually overrides a superclass method\n" +
		"----------\n");
}
// check generated #values() method
public void test006() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X { \n" +
			"	\n" +
			"	BLEU,\n" +
			"	BLANC,\n" +
			"	ROUGE;\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		for(X x: X.values()) {\n" +
			"			System.out.print(x);\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"}\n"
		},
		"BLEUBLANCROUGE");
}
// tolerate user definition for $VALUES
public void test007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X { \n" +
			"	\n" +
			"	BLEU,\n" +
			"	BLANC,\n" +
			"	ROUGE;\n" +
			"	\n" +
			"   int $VALUES;\n" +
			"	public static void main(String[] args) {\n" +
			"		for(X x: X.values()) {\n" +
			"			System.out.print(x);\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"}\n"
		},
		"BLEUBLANCROUGE");
}
// reject user definition for #values()
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X { \n" +
			"	\n" +
			"	BLEU,\n" +
			"	BLANC,\n" +
			"	ROUGE;\n" +
			"	\n" +
			"   void dup() {} \n" +
			"   void values() {} \n" +
			"   void dup() {} \n" +
			"   void values() {} \n" +
			"   Missing dup() {} \n" +
			"	public static void main(String[] args) {\n" +
			"		for(X x: X.values()) {\n" +
			"			System.out.print(x);\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	void dup() {} \n" +
		"	     ^^^^^\n" +
		"Duplicate method dup() in type X\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	void values() {} \n" +
		"	     ^^^^^^^^\n" +
		"The enum X already defines the method values() implicitly\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	void dup() {} \n" +
		"	     ^^^^^\n" +
		"Duplicate method dup() in type X\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 10)\n" +
		"	void values() {} \n" +
		"	     ^^^^^^^^\n" +
		"The enum X already defines the method values() implicitly\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 11)\n" +
		"	Missing dup() {} \n" +
		"	^^^^^^^\n" +
		"Missing cannot be resolved to a type\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 11)\n" +
		"	Missing dup() {} \n" +
		"	        ^^^^^\n" +
		"Duplicate method dup() in type X\n" +
		"----------\n");
}
// switch on enum
public void test009() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"	\n" +
			"	BLEU,\n" +
			"	BLANC,\n" +
			"	ROUGE;\n" +
			"	\n" +
			"	//void values() {}\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = BLEU;\n" +
			"		switch(x) {\n" +
			"			case BLEU :\n" +
			"				System.out.println(\"SUCCESS\");\n" +
			"				break;\n" +
			"			case BLANC :\n" +
			"			case ROUGE :\n" +
			"				System.out.println(\"FAILED\");\n" +
			"				break;\n" +
			"           default: // nop\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"}"
		},
		"SUCCESS");
}
// duplicate switch case
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"	\n" +
			"	BLEU,\n" +
			"	BLANC,\n" +
			"	ROUGE;\n" +
			"	\n" +
			"	//void values() {}\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = BLEU;\n" +
			"		switch(x) {\n" +
			"			case BLEU :\n" +
			"				break;\n" +
			"			case BLEU :\n" +
			"			case BLANC :\n" +
			"			case ROUGE :\n" +
			"				System.out.println(\"FAILED\");\n" +
			"				break;\n" +
			"           default: // nop\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	case BLEU :\n" +
		"	^^^^^^^^^\n" +
		"Duplicate case\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	case BLEU :\n" +
		"	^^^^^^^^^\n" +
		"Duplicate case\n" +
		"----------\n");
}
// reject user definition for #values()
public void test011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X { \n" +
			"	\n" +
			"	BLEU,\n" +
			"	BLANC,\n" +
			"	ROUGE;\n" +
			"	\n" +
			"   void values() {} \n" +
			"   void values() {} \n" +
			"	public static void main(String[] args) {\n" +
			"		for(X x: X.values()) {\n" +
			"			System.out.print(x);\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	void values() {} \n" +
		"	     ^^^^^^^^\n" +
		"The enum X already defines the method values() implicitly\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	void values() {} \n" +
		"	     ^^^^^^^^\n" +
		"The enum X already defines the method values() implicitly\n" +
		"----------\n");
}
// check abstract method diagnosis
public void test012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X implements Runnable { \n" +
			"	\n" +
			"	BLEU,\n" +
			"	BLANC,\n" +
			"	ROUGE;\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public enum X implements Runnable { \n" +
		"	            ^\n" +
		"The type X must implement the inherited abstract method Runnable.run()\n" +
		"----------\n");
}
// check enum constants with wrong arguments
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X { \n" +
			"	\n" +
			"	BLEU(10),\n" +
			"	BLANC(20),\n" +
			"	ROUGE(30);\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	BLEU(10),\n" +
		"	^^^^\n" +
		"The constructor X(int) is undefined\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	BLANC(20),\n" +
		"	^^^^^\n" +
		"The constructor X(int) is undefined\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 5)\n" +
		"	ROUGE(30);\n" +
		"	^^^^^\n" +
		"The constructor X(int) is undefined\n" +
		"----------\n");
}
// check enum constants with extra arguments
public void test014() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X { \n" +
			"	\n" +
			"	BLEU(10),\n" +
			"	BLANC(20),\n" +
			"	ROUGE(30);\n" +
			"\n" +
			"	int val;\n" +
			"	X(int val) {\n" +
			"		this.val = val;\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		for(X x: values()) {\n" +
			"			System.out.print(x.val);\n" +
			"		}\n" +
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
			"public enum X { \n" +
			"	\n" +
			"	BLEU(10),\n" +
			"	BLANC(),\n" +
			"	ROUGE(30);\n" +
			"\n" +
			"	int val;\n" +
			"	X(int val) {\n" +
			"		this.val = val;\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		for(X x: values()) {\n" +
			"			System.out.print(x.val);\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	BLANC(),\n" +
		"	^^^^^\n" +
		"The constructor X() is undefined\n" +
		"----------\n");
}
// check enum constants with wrong arguments
public void test016() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
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
			"	X(int val) {\n" +
			"		this.val = val;\n" +
			"	}\n" +
			"	String foo() {  // outer\n" +
			"		return this.name();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		for(X x: values()) {\n" +
			"			System.out.print(x.foo());\n" +
			"		}\n" +
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
			"public enum X {\n" +
			"	\n" +
			"	BLEU()\n" +
			"}\n"
		},
		"");
}
// cannot extend enums
public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"	BLEU()\n" +
			"}\n" +
			"\n" +
			"class XX extends X implements X {\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	class XX extends X implements X {\n" +
		"	                 ^\n" +
		"The type X cannot be the superclass of XX; a superclass must be a class\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	class XX extends X implements X {\n" +
		"	                              ^\n" +
		"The type X cannot be a superinterface of XX; a superinterface must be an interface\n" +
		"----------\n");
}
// 74851
public void test019() {
	this.runConformTest(
		new String[] {
			"MonthEnum.java",
			"public enum MonthEnum {\n" +
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
			"    public enum Rank {FIRST,SECOND,THIRD}\n" +
			"    public void setRank(Rank rank){}\n" +
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
			"    public static enum Rank {FIRST,SECOND,THIRD;\n" +
			"            void bar() { foo(); } \n" +
			"    }\n" +
			"    public void setRank(Rank rank){}\n" +
			"    void foo() {}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in Foo.java (at line 3)\n" +
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
			"	enum MX { BLEU, BLANC, ROUGE }\n" +
			"	\n" +
			"	void foo(MX e) {\n" +
			"		switch(e) {\n" +
			"			case MX.BLEU : break;\n" +
			"			case MX.BLANC : break;\n" +
			"			case MX.ROUGE : break;\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	case MX.BLEU : break;\n" +
		"	     ^^^^^^^\n" +
		"The qualified case label X.MX.BLEU must be replaced with the unqualified enum constant BLEU\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	case MX.BLANC : break;\n" +
		"	     ^^^^^^^^\n" +
		"The qualified case label X.MX.BLANC must be replaced with the unqualified enum constant BLANC\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	case MX.ROUGE : break;\n" +
		"	     ^^^^^^^^\n" +
		"The qualified case label X.MX.ROUGE must be replaced with the unqualified enum constant ROUGE\n" +
		"----------\n");
}

// 77212
public void test023() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public enum RuleType{ SUCCESS, FAILURE }\n" +
			"	public static void main(String[] args) {\n" +
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
			"public final enum X {\n" +
			"	FOO() {}\n" +
			"}\n" +
			"\n",
		},
	"----------\n" +
	"1. ERROR in X.java (at line 1)\n" +
	"	public final enum X {\n" +
	"	                  ^\n" +
	"Illegal modifier for the enum X; only public is permitted\n" +
	"----------\n");
}

// values is using arraycopy instead of clone
public void test025() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"	SUC, CESS;\n" +
			"	public static void main(String[] args) {\n" +
			"		for (X x : values()) {\n" +
			"			System.out.print(x.name());\n" +
			"		}\n" +
			"	}\n" +
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
			"	enum Couleur { BLEU, BLANC, ROUGE }\n" +
			"}\n" +
			"\n" +
			"class Y {\n" +
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
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 18)\n" +
		"	FOO();\n" +
		"	^^^\n" +
		"The method FOO() is undefined for the type X.Y\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 19)\n" +
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
			"public class X extends Enum {\n" +
			"}",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public class X extends Enum {\n" +
		"	                       ^^^^\n" +
		"Enum is a raw type. References to generic type Enum<E> should be parameterized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X extends Enum {\n" +
		"	                       ^^^^\n" +
		"The type X may not subclass Enum explicitly\n" +
		"----------\n");
}

// Javadoc in enum (see bug 78018)
public void test033() {
	this.runConformTest(
		new String[] {
			"E.java",
			"	/**\n" +
				"	 * Valid javadoc\n" +
				"	 * @author ffr\n" +
				"	 */\n" +
				"public enum E {\n" +
				"	/** Valid javadoc */\n" +
				"	TEST,\n" +
				"	/** Valid javadoc */\n" +
				"	VALID;\n" +
				"	/** Valid javadoc */\n" +
				"	public void foo() {}\n" +
				"}\n"
		}
	);
}
public void test034() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"	/**\n" +
				"	 * Invalid javadoc\n" +
				"	 * @exception NullPointerException Invalid tag\n" +
				"	 * @throws NullPointerException Invalid tag\n" +
				"	 * @return Invalid tag\n" +
				"	 * @param x Invalid tag\n" +
				"	 */\n" +
				"public enum E { TEST, VALID }\n"
		},
		"----------\n" +
			"1. ERROR in E.java (at line 3)\n" +
			"	* @exception NullPointerException Invalid tag\n" +
			"	   ^^^^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"2. ERROR in E.java (at line 4)\n" +
			"	* @throws NullPointerException Invalid tag\n" +
			"	   ^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"3. ERROR in E.java (at line 5)\n" +
			"	* @return Invalid tag\n" +
			"	   ^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"4. ERROR in E.java (at line 6)\n" +
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
			"E.java",
			"	/**\n" +
				"	 * @see \"Valid normal string\"\n" +
				"	 * @see <a href=\"http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html\">Valid URL link reference</a>\n" +
				"	 * @see Object\n" +
				"	 * @see #TEST\n" +
				"	 * @see E\n" +
				"	 * @see E#TEST\n" +
				"	 */\n" +
				"public enum E { TEST, VALID }\n"
		}
	);
}
public void test036() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"	/**\n" +
				"	 * @see \"invalid\" no text allowed after the string\n" +
				"	 * @see <a href=\"invalid\">invalid</a> no text allowed after the href\n" +
				"	 * @see\n" +
				"	 * @see #VALIDE\n" +
				"	 */\n" +
				"public enum E { TEST, VALID }\n"
		},
		"----------\n" +
			"1. ERROR in E.java (at line 2)\n" +
			"	* @see \"invalid\" no text allowed after the string\n" +
			"	                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Unexpected text\n" +
			"----------\n" +
			"2. ERROR in E.java (at line 3)\n" +
			"	* @see <a href=\"invalid\">invalid</a> no text allowed after the href\n" +
			"	                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Unexpected text\n" +
			"----------\n" +
			"3. ERROR in E.java (at line 4)\n" +
			"	* @see\n" +
			"	   ^^^\n" +
			"Javadoc: Missing reference\n" +
			"----------\n" +
			"4. ERROR in E.java (at line 5)\n" +
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
			"E.java",
			"	/**\n" +
				"	 * Value test: {@value #TEST}\n" +
				"	 * or: {@value E#TEST}\n" +
				"	 */\n" +
				"public enum E { TEST, VALID }\n"
		}
	);
}
public void test038() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.runNegativeTest(
		new String[] {
			"E.java",
			"public enum E { TEST, VALID;\n" +
			"	public void foo() {}\n" +
			"}"
		},
		"----------\n" +
			"1. ERROR in E.java (at line 1)\n" +
			"	public enum E { TEST, VALID;\n" +
			"	            ^\n" +
			"Javadoc: Missing comment for public declaration\n" +
			"----------\n" +
			"2. ERROR in E.java (at line 1)\n" +
			"	public enum E { TEST, VALID;\n" +
			"	                ^^^^\n" +
			"Javadoc: Missing comment for public declaration\n" +
			"----------\n" +
			"3. ERROR in E.java (at line 1)\n" +
			"	public enum E { TEST, VALID;\n" +
			"	                      ^^^^^\n" +
			"Javadoc: Missing comment for public declaration\n" +
			"----------\n" +
			"4. ERROR in E.java (at line 2)\n" +
			"	public void foo() {}\n" +
			"	            ^^^^^\n" +
			"Javadoc: Missing comment for public declaration\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test039() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"public enum E {\n" +
				"	/**\n" +
				"	 * @exception NullPointerException Invalid tag\n" +
				"	 * @throws NullPointerException Invalid tag\n" +
				"	 * @return Invalid tag\n" +
				"	 * @param x Invalid tag\n" +
				"	 */\n" +
				"	TEST,\n" +
				"	VALID;\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in E.java (at line 3)\n" +
			"	* @exception NullPointerException Invalid tag\n" +
			"	   ^^^^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"2. ERROR in E.java (at line 4)\n" +
			"	* @throws NullPointerException Invalid tag\n" +
			"	   ^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"3. ERROR in E.java (at line 5)\n" +
			"	* @return Invalid tag\n" +
			"	   ^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"4. ERROR in E.java (at line 6)\n" +
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
			"E.java",
			"public enum E {\n" +
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
				"}\n"
		}
	);
}
public void test041() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"public enum E {\n" +
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
				"}\n"
		},
		"----------\n" +
		"1. ERROR in E.java (at line 3)\n" +
		"	* @see e\n" +
		"	       ^\n" +
		"Javadoc: e cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in E.java (at line 4)\n" +
		"	* @see #VALIDE\n" +
		"	        ^^^^^^\n" +
		"Javadoc: VALIDE cannot be resolved or is not a field\n" +
		"----------\n" +
		"3. ERROR in E.java (at line 8)\n" +
		"	* @see E#test\n" +
		"	         ^^^^\n" +
		"Javadoc: test cannot be resolved or is not a field\n" +
		"----------\n" +
		"4. ERROR in E.java (at line 9)\n" +
		"	* @see EUX\n" +
		"	       ^^^\n" +
		"Javadoc: EUX cannot be resolved to a type\n" +
		"----------\n" +
		"5. ERROR in E.java (at line 13)\n" +
		"	* @param obj the object\n" +
		"	         ^^^\n" +
		"Javadoc: Parameter obj is not declared\n" +
		"----------\n" +
		"6. ERROR in E.java (at line 14)\n" +
		"	* @return\n" +
		"	   ^^^^^^\n" +
		"Javadoc: Description expected after @return\n" +
		"----------\n" +
		"7. ERROR in E.java (at line 15)\n" +
		"	* @see Objet\n" +
		"	       ^^^^^\n" +
		"Javadoc: Objet cannot be resolved to a type\n" +
		"----------\n" +
		"8. ERROR in E.java (at line 17)\n" +
		"	public String val(Object x) { return x.toString(); }\n" +
		"	                         ^\n" +
		"Javadoc: Missing tag for parameter x\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test042() {
	this.runConformTest(
		new String[] {
			"E.java",
			"public enum E {\n" +
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
				"}\n"
		}
	);
}

// External javadoc references to enum
public void test043() {
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
public void test044() {
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
			"public enum X\n" +
			"{\n" +
			"  FIRST,\n" +
			"  SECOND,\n" +
			"  THIRD;\n" +
			"\n" +
			"  static {\n" +
			"    for (X t : values()) {\n" +
			"      System.out.print(t.name());\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  X() {\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) {\n" +
			"  }\n" +
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
			"public enum X {\n" +
			"  a(1);\n" +
			"  X(int i) {\n" +
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
			"public enum X {\n" +
			"	;\n" +
			"	X() {\n" +
			"		super();\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	super();\n" +
		"	^^^^^^^^\n" +
		"Cannot invoke super constructor from enum constructor X()\n" +
		"----------\n"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77211
 */
public void test048() {
	this.runConformTest(
		new String[] {
			"StopLight.java",
			"public enum StopLight{\n" +
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
			"public abstract enum X {}"
		},
	"----------\n" +
	"1. ERROR in X.java (at line 1)\n" +
	"	public abstract enum X {}\n" +
	"	                     ^\n" +
	"Illegal modifier for the enum X; only public is permitted\n" +
	"----------\n"
	);
}

public void test050() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X {}"
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
			"public enum X {\n" +
			"	BLEU (0) {\n" +
			"	}\n" +
			"	;\n" +
			"	X() {\n" +
			"		this(0);\n" +
			"	}\n" +
			"	X(int i) {\n" +
			"	}\n" +
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
			"public enum X {\n" +
			"	A\n" +
			"	;\n" +
			"	\n" +
			"	public abstract void foo();\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
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
			"public enum X {\n" +
			"	A () { public void foo() {} }\n" +
			"	;\n" +
			"	\n" +
			"	public abstract void foo();\n" +
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
			"public enum X {\n" +
			"	A() {}\n" +
			"	;\n" +
			"	\n" +
			"	public abstract void foo();\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
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
			"public enum X {\n" +
			"	;\n" +
			"	\n" +
			"	public abstract void foo();\n" +
			"}\n"
		},
	"----------\n" +
	"1. ERROR in X.java (at line 4)\n" +
	"	public abstract void foo();\n" +
	"	                     ^^^^^\n" +
	"The enum X can only define the abstract method foo() if it also defines enum constants with corresponding implementations\n" +
	"----------\n"
	);
}
// TODO (philippe) enum cannot be declared as local type

// TODO (philippe) check one cannot redefine Enum incorrectly

// TODO (philippe) check enum syntax recovery
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78914 - variation
 */
public void test056() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"    PLUS {\n" +
			"        double eval(double x, double y) { return x + y; }\n" +
			"    };\n" +
			"\n" +
			"    // Perform the arithmetic X represented by this constant\n" +
			"    abstract double eval(double x, double y);\n" +
			"}"
		},
		""
	);
	String expectedOutput =
		"// Signature: Ljava/lang/Enum<LX;>;\n" +
		"public abstract enum X {\n";

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
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
			"    enum Color { RED, GREEN };\n" +
			"    public static void main(String[] args) {\n" +
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
			"enum X { a }\n" +
			"class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		test(X.a, 9);\n" +
			"		test2(X.a, 3);\n" +
			"	}\n" +
			"	static void test(X x, int a) {\n" +
			"		if (x == a) a++; // incomparable types: X and int\n" +
			"		switch(x) {\n" +
			"			case a : System.out.println(a); // prints \'9\'\n" +
			"           default: // nop\n" +
			"		}\n" +
			"	}\n" +
			"	static void test2(X x, final int aa) {\n" +
			"		switch(x) {\n" +
			"			case aa : // unqualified enum constant error\n" +
			"				System.out.println(a); // cannot find a\n" +
			"           default: // nop\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	if (x == a) a++; // incomparable types: X and int\n" +
		"	    ^^^^^^\n" +
		"Incompatible operand types X and int\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 16)\n" +
		"	case aa : // unqualified enum constant error\n" +
		"	     ^^\n" +
		"aa cannot be resolved or is not a field\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 17)\n" +
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
			"public enum X {\n" +
			"	MONDAY {\n" +
			"		public void foo() {\n" +
			"		}\n" +
			"	};\n" +
			"	private X() {\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"	  System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}\n"
		},
		"SUCCESS");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=81589
 */
public void test060() {
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
			"public enum X {\n" +
			"	A, B, C;\n" +
			"	public static final X D = null;\n" +
			"}\n" +
			"\n" +
			"class A {\n" +
			"	private void foo(X x) {\n" +
			"		switch (x) {\n" +
			"			case D:\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 8)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The enum constant A needs a corresponding case label in this enum switch on X\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 8)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The enum constant B needs a corresponding case label in this enum switch on X\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 8)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The enum constant C needs a corresponding case label in this enum switch on X\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 9)\n" +
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
			"public enum X {\n" +
			"	A, B, C;\n" +
			"	public static final X D = null;\n" +
			"}\n" +
			"\n" +
			"class A {\n" +
			"	private void foo(X x) {\n" +
			"		switch (x) {\n" +
			"			case X.D:\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 8)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The switch over the enum type X should have a default case\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 8)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The enum constant A needs a corresponding case label in this enum switch on X\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 8)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The enum constant B needs a corresponding case label in this enum switch on X\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 8)\n" +
		"	switch (x) {\n" +
		"	        ^\n" +
		"The enum constant C needs a corresponding case label in this enum switch on X\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 9)\n" +
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
			"  enum Option { ALPHA, BRAVO  };\n" +
			"  void method1(Option item) {\n" +
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
			"public enum X implements B {\n" +
			"\n" +
			"	C1 {\n" +
			"		public void test() {};\n" +
			"	},\n" +
			"	C2 {\n" +
			"		public void test() {};\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"interface B {\n" +
			"	public void test();\n" +
			"	\n" +
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
			"public enum X {\n" +
			"  A;\n" +
			"  private void foo() {\n" +
			"    X e= new X() {\n" +
			"    };\n" +
			"  }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	X e= new X() {\n" +
		"	         ^\n" +
		"Cannot instantiate the type X\n" +
		"----------\n");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83860
 */
public void test066() {
    this.runConformTest(
        new String[] {
            "X.java",
            "public enum X {\n" +
            "    SUCCESS (0) {};\n" +
            "    private X(int i) {}\n" +
            "    public static void main(String[] args) {\n" +
            "       for (X x : values()) {\n" +
            "           System.out.print(x);\n" +
            "       }\n" +
            "    }\n" +
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
            "public enum X {\n" +
            "    ONE, TWO, THREE;\n" +
            "    abstract int getSquare();\n" +
            "    abstract int getSquare();\n" +
            "}",
        },
        "----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	ONE, TWO, THREE;\n" +
		"	^^^\n" +
		"The enum constant ONE must implement the abstract method getSquare()\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	ONE, TWO, THREE;\n" +
		"	     ^^^\n" +
		"The enum constant TWO must implement the abstract method getSquare()\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 2)\n" +
		"	ONE, TWO, THREE;\n" +
		"	          ^^^^^\n" +
		"The enum constant THREE must implement the abstract method getSquare()\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 3)\n" +
		"	abstract int getSquare();\n" +
		"	             ^^^^^^^^^^^\n" +
		"Duplicate method getSquare() in type X\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 4)\n" +
		"	abstract int getSquare();\n" +
		"	             ^^^^^^^^^^^\n" +
		"Duplicate method getSquare() in type X\n" +
		"----------\n");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83648
 */
public void test068() {
    this.runNegativeTest(
        new String[] {
            "X.java",
            "public enum X {\n" +
            "    A(1, 3), B(1, 3), C(1, 3) { }\n" +
            "   	;\n" +
            "    public X(int i, int j) { }\n" +
            "}",
        },
        "----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	public X(int i, int j) { }\n" +
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
            "public enum X {\n" +
            "    A(1, 3), B(1, 3), C(1, 3) { }\n" +
            "   	;\n" +
            "    protected X(int i, int j) { }\n" +
            "}",
        },
        "----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	protected X(int i, int j) { }\n" +
		"	          ^^^^^^^^^^^^^^^\n" +
		"Illegal modifier for the enum constructor; only private is permitted.\n" +
		"----------\n");
}

public void test070() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"    PLUS {\n" +
			"        double eval(double x, double y) { return x + y; }\n" +
			"    };\n" +
			"\n" +
			"    // Perform the arithmetic X represented by this constant\n" +
			"    abstract double eval(double x, double y);\n" +
			"}"
		},
		""
	);
	String expectedOutput =
		"  // Method descriptor #18 (Ljava/lang/String;I)V\n" +
		"  // Stack: 3, Locals: 3\n" +
		"  private X(java.lang.String arg0, int arg1);\n" +
		"    0  aload_0 [this]\n" +
		"    1  aload_1 [arg0]\n" +
		"    2  iload_2 [arg1]\n" +
		"    3  invokespecial java.lang.Enum(java.lang.String, int) [25]\n" +
		"    6  return\n";

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
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
			"X1.java",
			"public enum X1 implements I {\n" +
			"	;\n" +
			"}\n" +
			"interface I {}\n"
		},
		""
	);
	this.runConformTest( // no methods to implement with constant
		new String[] {
			"X1a.java",
			"public enum X1a implements I {\n" +
			"	A;\n" +
			"}\n" +
			"interface I {}\n"
		},
		""
	);
	this.runConformTest( // no methods to implement with constant body
		new String[] {
			"X1b.java",
			"public enum X1b implements I {\n" +
			"	A() { void random() {} };\n" +
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
			"X2.java",
			"public enum X2 implements I {\n" +
			"	;\n" +
			"	public void test() {}\n" +
			"}\n" +
			"interface I { void test(); }\n"
		},
		""
	);
	this.runConformTest( // implement inherited method with constant
		new String[] {
			"X2a.java",
			"public enum X2a implements I {\n" +
			"	A;\n" +
			"	public void test() {}\n" +
			"}\n" +
			"interface I { void test(); }\n"
		},
		""
	);
	this.runConformTest( // implement inherited method with constant body
		new String[] {
			"X2b.java",
			"public enum X2b implements I {\n" +
			"	A() { public void test() {} };\n" +
			"	public void test() {}\n" +
			"}\n" +
			"interface I { void test(); }\n"
		},
		""
	);
	this.runConformTest( // implement inherited method with random constant body
		new String[] {
			"X2c.java",
			"public enum X2c implements I {\n" +
			"	A() { void random() {} };\n" +
			"	public void test() {}\n" +
			"}\n" +
			"interface I { void test(); }\n"
		},
		""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test073() {
	this.runNegativeTest( // implement inherited method but as abstract
		new String[] {
			"X3.java",
			"public enum X3 implements I {\n" +
			"	;\n" +
			"	public abstract void test();\n" +
			"}\n" +
			"interface I { void test(); }\n"
		},
		"----------\n" +
		"1. ERROR in X3.java (at line 3)\n" +
		"	public abstract void test();\n" +
		"	                     ^^^^^^\n" +
		"The enum X3 can only define the abstract method test() if it also defines enum constants with corresponding implementations\n" +
		"----------\n"
		// X3 is not abstract and does not override abstract method test() in X3
	);
	this.runNegativeTest( // implement inherited method as abstract with constant
		new String[] {
			"X3a.java",
			"public enum X3a implements I {\n" +
			"	A;\n" +
			"	public abstract void test();\n" +
			"}\n" +
			"interface I { void test(); }\n"
		},
		"----------\n" +
		"1. ERROR in X3a.java (at line 2)\n" +
		"	A;\n" +
		"	^\n" +
		"The enum constant A must implement the abstract method test()\n" +
		"----------\n"
		// X3a is not abstract and does not override abstract method test() in X3a
	);
	this.runConformTest( // implement inherited method as abstract with constant body
		new String[] {
			"X3b.java",
			"public enum X3b implements I {\n" +
			"	A() { public void test() {} };\n" +
			"	public abstract void test();\n" +
			"}\n" +
			"interface I { void test(); }\n"
		},
		""
	);
	this.runNegativeTest( // implement inherited method as abstract with random constant body
		new String[] {
			"X3c.java",
			"public enum X3c implements I {\n" +
			"	A() { void random() {} };\n" +
			"	public abstract void test();\n" +
			"}\n" +
			"interface I { void test(); }\n"
		},
		"----------\n" +
		"1. ERROR in X3c.java (at line 2)\n" +
		"	A() { void random() {} };\n" +
		"	^\n" +
		"The enum constant A must implement the abstract method test()\n" +
		"----------\n"
		// <anonymous X3c$1> is not abstract and does not override abstract method test() in X3c
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test074() {
	this.runNegativeTest( // define abstract method
		new String[] {
			"X4.java",
			"public enum X4 {\n" +
			"	;\n" +
			"	public abstract void test();\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X4.java (at line 3)\n" +
		"	public abstract void test();\n" +
		"	                     ^^^^^^\n" +
		"The enum X4 can only define the abstract method test() if it also defines enum constants with corresponding implementations\n" +
		"----------\n"
		// X4 is not abstract and does not override abstract method test() in X4
	);
	this.runNegativeTest( // define abstract method with constant
		new String[] {
			"X4a.java",
			"public enum X4a {\n" +
			"	A;\n" +
			"	public abstract void test();\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X4a.java (at line 2)\n" +
		"	A;\n" +
		"	^\n" +
		"The enum constant A must implement the abstract method test()\n" +
		"----------\n"
		// X4a is not abstract and does not override abstract method test() in X4a
	);
	this.runConformTest( // define abstract method with constant body
		new String[] {
			"X4b.java",
			"public enum X4b {\n" +
			"	A() { public void test() {} };\n" +
			"	public abstract void test();\n" +
			"}\n"
		},
		""
	);
	this.runNegativeTest( // define abstract method with random constant body
		new String[] {
			"X4c.java",
			"public enum X4c {\n" +
			"	A() { void random() {} };\n" +
			"	public abstract void test();\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X4c.java (at line 2)\n" +
		"	A() { void random() {} };\n" +
		"	^\n" +
		"The enum constant A must implement the abstract method test()\n" +
		"----------\n"
		// <anonymous X4c$1> is not abstract and does not override abstract method test() in X4c
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test075() {
	this.runNegativeTest( // do not implement inherited method
		new String[] {
			"X5.java",
			"public enum X5 implements I {\n" +
			"	;\n" +
			"}\n" +
			"interface I { void test(); }\n"
		},
		"----------\n" +
		"1. ERROR in X5.java (at line 1)\n" +
		"	public enum X5 implements I {\n" +
		"	            ^^\n" +
		"The type X5 must implement the inherited abstract method I.test()\n" +
		"----------\n"
		// X5 is not abstract and does not override abstract method test() in I
	);
	this.runNegativeTest( // do not implement inherited method & have constant with no body
		new String[] {
			"X5a.java",
			"public enum X5a implements I {\n" +
			"	A;\n" +
			"}\n" +
			"interface I { void test(); }\n"
		},
		"----------\n" +
		"1. ERROR in X5a.java (at line 1)\n" +
		"	public enum X5a implements I {\n" +
		"	            ^^^\n" +
		"The type X5a must implement the inherited abstract method I.test()\n" +
		"----------\n"
		// X5a is not abstract and does not override abstract method test() in I
	);
	this.runConformTest( // do not implement inherited method & have constant with body
		new String[] {
			"X5b.java",
			"public enum X5b implements I {\n" +
			"	A() { public void test() {} };\n" +
			"	;\n" +
			"}\n" +
			"interface I { void test(); }\n"
		},
		""
	);
	this.runNegativeTest( // do not implement inherited method & have constant with random body
		new String[] {
			"X5c.java",
			"public enum X5c implements I {\n" +
			"	A() { void random() {} };\n" +
			"	;\n" +
			"	private X5c() {}\n" +
			"}\n" +
			"interface I { void test(); }\n"
		},
		"----------\n" +
		"1. ERROR in X5c.java (at line 2)\n" +
		"	A() { void random() {} };\n" +
		"	^\n" +
		"The enum constant A must implement the abstract method test()\n" +
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
			"	public static void main(String[] args) { ((I) E.A).foo(); }\n" +
			"}\n" +
			"interface I { I foo(); }\n" +
			"enum E implements I {\n" +
			"	A;\n" +
			"	public E foo() {\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n"
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
			"		E.A.bar();\n" +
			"	}\n" +
			"}\n" +
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
			"		E.A.bar();\n" +
			"	}\n" +
			"}\n" +
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
			"}\n"
		},
		"SUCCESS"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85397
public void test079() throws Exception {
	String op =
			this.complianceLevel < ClassFileConstants.JDK17 ?
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	private strictfp X() {}\n" +
					"	                 ^^^\n" +
					"Illegal modifier for the constructor in type X; only public, protected & private are permitted\n" +
					"----------\n" :
					"----------\n" +
					"1. WARNING in X.java (at line 3)\n" +
					"	private strictfp X() {}\n" +
					"	        ^^^^^^^^\n" +
					"Floating-point expressions are always strictly evaluated from source level 17. Keyword \'strictfp\' is not required.\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 3)\n" +
					"	private strictfp X() {}\n" +
					"	                 ^^^\n" +
					"Illegal modifier for the constructor in type X; only public, protected & private are permitted\n" +
					"----------\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"	A, B;\n" +
			"	private strictfp X() {}\n" +
			"}\n"
		},
		op
	);
	this.runConformTest(
		new String[] {
			"X.java",
			"public strictfp enum X {\n" +
			"	A, B;\n" +
			"	private X() {}\n" +
			"}\n"
		},
		""
	);

	String[] expectedOutputs =
			this.complianceLevel < ClassFileConstants.JDK17 ?
					new String[] {
							"  private strictfp X(java.lang.String arg0, int arg1);\n",
							"  public static strictfp X[] values();\n",
							"  public static strictfp X valueOf(java.lang.String arg0);\n"
						} :
			new String[] {
		"  private X(java.lang.String arg0, int arg1);\n",
		"  public static X[] values();\n",
		"  public static X valueOf(java.lang.String arg0);\n"
	};

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
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
			"public enum X implements TestInterface {\n" +
			"	TEST {\n" +
			"		public int test() {\n" +
			"			return 42;\n" +
			"		}\n" +
			"	},\n" +
			"	ENUM {\n" +
			"		public int test() {\n" +
			"			return 37;\n" +
			"		}\n" +
			"	};\n" +
			"} \n"
		},
		""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87818
public void test081() {
	String expectedErrorMessage = this.complianceLevel < ClassFileConstants.JDK16 ?
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	enum E {}\n" +
			"	     ^\n" +
			"The member enum E can only be defined inside a top-level class or interface or in a static context\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n"
			:
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n";
		this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo() {\n" +
			"		enum E {}\n" +
			"	    Zork();\n" +
			"	}\n" +
			"}"
		},
		expectedErrorMessage);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88223
public void test082() {
	if ( this.complianceLevel < ClassFileConstants.JDK16) {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	class Y {\n" +
			"		enum E {}\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	enum E {}\n" +
		"	     ^\n" +
		"The member enum E must be defined inside a static member type\n" +
		"----------\n");
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	class Y {\n" +
					"		enum E {}\n" +
					"	}\n" +
					"}"
				},
				"");
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static class Y {\n" +
			"		enum E {}\n" +
			"	}\n" +
			"}"
		},
		"");
	if ( this.complianceLevel < ClassFileConstants.JDK16) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	void foo() {\n" +
					"		class Local {\n" +
					"			enum E {}\n" +
					"		}\n" +
					"	}\n" +
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	enum E {}\n" +
				"	     ^\n" +
				"The member enum E can only be defined inside a top-level class or interface or in a static context\n" +
				"----------\n");
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	void foo() {\n" +
					"		class Local {\n" +
					"			enum E {}\n" +
					"		}\n" +
					"	}\n" +
					"}"
				},
				"");
	}
}


// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87998 - check no emulation warning
public void test083() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"	INPUT {\n" +
			"		@Override\n" +
			"		public X getReverse() {\n" +
			"			return OUTPUT;\n" +
			"		}\n" +
			"	},\n" +
			"	OUTPUT {\n" +
			"		@Override\n" +
			"		public X getReverse() {\n" +
			"			return INPUT;\n" +
			"		}\n" +
			"	},\n" +
			"	INOUT {\n" +
			"		@Override\n" +
			"		public X getReverse() {\n" +
			"			return INOUT;\n" +
			"		}\n" +
			"	};\n" +
			"	X(){}\n" +
			"  Zork z;\n" +
			"	public abstract X getReverse();\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 21)\n" +
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
			"public enum X {\n" +
			"	INPUT {\n" +
			"		@Override\n" +
			"		public X getReverse() {\n" +
			"			return OUTPUT;\n" +
			"		}\n" +
			"	},\n" +
			"	OUTPUT {\n" +
			"		@Override\n" +
			"		public X getReverse() {\n" +
			"			return INPUT;\n" +
			"		}\n" +
			"	},\n" +
			"	INOUT {\n" +
			"		@Override\n" +
			"		public X getReverse() {\n" +
			"			return INOUT;\n" +
			"		}\n" +
			"	};\n" +
			"	X(){}\n" +
			"	public abstract X getReverse();\n" +
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
		"  // Method descriptor #20 (Ljava/lang/String;I)V\n" +
		"  // Stack: 3, Locals: 3\n" +
		"  private X(java.lang.String arg0, int arg1);\n";

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
			"	enum Test1 {\n" +
			"		test11, test12\n" +
			"	};\n" +
			"	enum Test2 {\n" +
			"		test21, test22\n" +
			"	};\n" +
			"\n" +
			"	void foo1(Test1 t1, Test2 t2) {\n" +
			"		boolean b = t1 == t2;\n" +
			"	}\n" +
			"	void foo2(Test1 t1, Object t2) {\n" +
			"		boolean b = t1 == t2;\n" +
			"	}\n" +
			"	void foo3(Test1 t1, Enum t2) {\n" +
			"		boolean b = t1 == t2;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean booleanTest = (Test1.test11 == Test2.test22);\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	boolean b = t1 == t2;\n" +
		"	            ^^^^^^^^\n" +
		"Incompatible operand types X.Test1 and X.Test2\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 15)\n" +
		"	void foo3(Test1 t1, Enum t2) {\n" +
		"	                    ^^^^\n" +
		"Enum is a raw type. References to generic type Enum<E> should be parameterized\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 19)\n" +
		"	boolean booleanTest = (Test1.test11 == Test2.test22);\n" +
		"	                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Incompatible operand types X.Test1 and X.Test2\n" +
		"----------\n");
}
public void test086() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	enum Test1 {\n" +
			"		V;\n" +
			"		static int foo = 0;\n" +
			"	}\n" +
			"}\n",
		},
		"");
}
public void test087() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	enum Test1 {\n" +
			"		V;\n" +
			"		interface Foo {}\n" +
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
			"\n" +
			"	enum Test1 {\n" +
			"		V;\n" +
			"	}\n" +
			"	Object foo() {\n" +
			"		return this;\n" +
			"	}\n" +
			"\n" +
			"	static class Sub extends X {\n" +
			"		@Override\n" +
			"		Test1 foo() {\n" +
			"			return Test1.V;\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"");
}
public void test089() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"	enum Test1 {\n" +
			"		V;\n" +
			"		protected final Test1 clone() { return V; }\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	protected final Test1 clone() { return V; }\n" +
		"	                      ^^^^^^^\n" +
		"Cannot override the final method from Enum<X.Test1>\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	protected final Test1 clone() { return V; }\n" +
		"	                      ^^^^^^^\n" +
		"The method clone() of type X.Test1 should be tagged with @Override since it actually overrides a superclass method\n" +
		"----------\n");
}
public void test090() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"	enum Test1 {\n" +
			"		V;\n" +
			"		public Test1 foo() { return V; }\n" +
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
		"1. WARNING in X.java (at line 5)\n" +
		"	public Test1 foo() { return V; }\n" +
		"	             ^^^^^\n" +
		"The method foo() of type X.Test1 should be tagged with @Override since it actually overrides a superclass method\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
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
			"	enum Test1 {\n" +
			"		V;\n" +
			"		void foo() {}\n" +
			"	}\n" +
			"	class Member<E extends Test1> {\n" +
			"		void bar(E e) {\n" +
			"			e.foo();\n" +
			"		}\n" +
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
			"	enum Test1 {\n" +
			"		V;\n" +
			"		void foo() {}\n" +
			"	}\n" +
			"	class Member<E extends Object & Test1> {\n" +
			"		void bar(E e) {\n" +
			"			e.foo();\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	class Member<E extends Object & Test1> {\n" +
		"	                                ^^^^^\n" +
		"The type X.Test1 is not an interface; it cannot be specified as a bounded parameter\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
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
			"	enum Test1 {\n" +
			"		V;\n" +
			"		void foo() {}\n" +
			"	}\n" +
			"	class Member<E extends Test1> {\n" +
			"		E e;\n" +
			"		void bar(Member<? extends Test1> me) {\n" +
			"		}\n" +
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
			"public enum X {\n" +
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
		"// Signature: Ljava/lang/Enum<LX;>;\n" +
		"public final enum X {\n" +
		"  \n" +
		"  // Field descriptor #6 [LX;\n" +
		"  private static final synthetic X[] ENUM$VALUES;\n" +
		"  \n" +
		"  // Method descriptor #8 ()V\n" +
		"  // Stack: 1, Locals: 0\n" +
		"  static {};\n" +
		"    0  iconst_0\n" +
		"    1  anewarray X [1]\n" +
		"    4  putstatic X.ENUM$VALUES : X[] [10]\n" +
		"    7  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 1]\n" +
		"  \n" +
		"  // Method descriptor #15 (Ljava/lang/String;I)V\n" +
		"  // Stack: 3, Locals: 3\n" +
		"  private X(java.lang.String arg0, int arg1);\n" +
		"    0  aload_0 [this]\n" +
		"    1  aload_1 [arg0]\n" +
		"    2  iload_2 [arg1]\n" +
		"    3  invokespecial java.lang.Enum(java.lang.String, int) [16]\n" +
		"    6  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 1]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 7] local: this index: 0 type: X\n" +
		"  \n" +
		"  // Method descriptor #21 ()[LX;\n" +
		"  // Stack: 5, Locals: 3\n" +
		"  public static X[] values();\n" +
		"     0  getstatic X.ENUM$VALUES : X[] [10]\n" +
		"     3  dup\n" +
		"     4  astore_0\n" +
		"     5  iconst_0\n" +
		"     6  aload_0\n" +
		"     7  arraylength\n" +
		"     8  dup\n" +
		"     9  istore_1\n" +
		"    10  anewarray X [1]\n" +
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
		"  // Method descriptor #29 (Ljava/lang/String;)LX;\n" +
		"  // Stack: 2, Locals: 1\n" +
		"  public static X valueOf(java.lang.String arg0);\n" +
		"     0  ldc <Class X> [1]\n" +
		"     2  aload_0 [arg0]\n" +
		"     3  invokestatic java.lang.Enum.valueOf(java.lang.Class, java.lang.String) : java.lang.Enum [30]\n" +
		"     6  checkcast X [1]\n" +
		"     9  areturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 1]\n";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}
public void test095() { // check missing abstract cases from multiple interfaces
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X implements I, J { \n" +
			"	ROUGE;\n" +
			"}\n" +
			"interface I { void foo(); }\n" +
			"interface J { void foo(); }\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public enum X implements I, J { \n" +
		"	            ^\n" +
		"The type X must implement the inherited abstract method J.foo()\n" +
		"----------\n");
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X implements I, J { \n" +
			"	ROUGE;\n" +
			"	public void foo() {}\n" +
			"}\n" +
			"interface I { void foo(int i); }\n" +
			"interface J { void foo(); }\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public enum X implements I, J { \n" +
		"	            ^\n" +
		"The type X must implement the inherited abstract method I.foo(int)\n" +
		"----------\n");
}
public void test096() { // check for raw vs. parameterized parameter types
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X implements I { \n" +
			"	ROUGE;\n" +
			"	public void foo(A a) {}\n" +
			"}\n" +
			"interface I { void foo(A<String> a); }\n" +
			"class A<T> {}\n"
		},
		"");
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X implements I { \n" +
			"	ROUGE { public void foo(A a) {} }\n" +
			"	;\n" +
			"}\n" +
			"interface I { void foo(A<String> a); }\n" +
			"class A<T> {}\n"
		},
		"");
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X implements I { \n" +
			"	ROUGE;\n" +
			"	public void foo(A<String> a) {}\n" +
			"}\n" +
			"interface I { void foo(A a); }\n" +
			"class A<T> {}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public enum X implements I { \n" +
		"	            ^\n" +
		"The type X must implement the inherited abstract method I.foo(A)\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	public void foo(A<String> a) {}\n" +
		"	            ^^^^^^^^^^^^^^^^\n" +
		"Name clash: The method foo(A<String>) of type X has the same erasure as foo(A) of type I but does not override it\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 5)\n" +
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
			"	void foo(Colors color) {\n" +
			"		switch (color) {\n" +
			"			case BLUE:\n" +
			"			case RED:\n" +
			"				break;\n" +
			"			default: // nop\n" +
			"		} \n" +
			"	}\n" +
			"}\n" +
			"/**\n" +
			" * Enumeration of some basic colors.\n" +
			" */\n" +
			"enum Colors {\n" +
			"	BLACK,\n" +
			"	WHITE,\n" +
			"	RED  \n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in E.java (at line 6)\n" +
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
			"	void foo(Colors color) {\n" +
			"		switch (color) {\n" +
			"		} \n" +
			"	}\n" +
			"}\n" +
			"/**\n" +
			" * Enumeration of some basic colors.\n" +
			" */\n" +
			"enum Colors {\n" +
			"	BLACK,\n" +
			"	WHITE,\n" +
			"	RED;  \n" +
			"  Zork z;\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in E.java (at line 5)\n" +
		"	switch (color) {\n" +
		"	        ^^^^^\n" +
		"The enum constant BLACK needs a corresponding case label in this enum switch on Colors\n" +
		"----------\n" +
		"2. WARNING in E.java (at line 5)\n" +
		"	switch (color) {\n" +
		"	        ^^^^^\n" +
		"The enum constant RED needs a corresponding case label in this enum switch on Colors\n" +
		"----------\n" +
		"3. WARNING in E.java (at line 5)\n" +
		"	switch (color) {\n" +
		"	        ^^^^^\n" +
		"The enum constant WHITE needs a corresponding case label in this enum switch on Colors\n" +
		"----------\n" +
		"4. ERROR in E.java (at line 16)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89274
public void test099() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class A<T> {\n" +
			"	enum E {\n" +
			"		v1, v2;\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"public class X extends A<Integer> {\n" +
			"	void a(A.E e) {\n" +
			"		b(e); // no unchecked warning\n" +
			"	}\n" +
			"\n" +
			"	void b(E e) {\n" +
			"		A<Integer>.E e1 = e;\n" +
			"	}\n" +
			"}\n",
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
public void test100() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"\n" +
			"	anEnumValue {\n" +
			"		private final X thisOne = anEnumValue;\n" +
			"\n" +
			"		@Override String getMessage() {\n" +
			"			return \"Here is what thisOne gets assigned: \" + thisOne;\n" +
			"		}\n" +
			"	};\n" +
			"\n" +
			"	abstract String getMessage();\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(anEnumValue.getMessage());\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	private final X thisOne = anEnumValue;\n" +
		"	                          ^^^^^^^^^^^\n" +
		"Cannot refer to the static enum field X.anEnumValue within an initializer\n" +
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
			"enum BugDemo {\n" +
			"  CONSTANT(new Foo() {\n" +
			"    public boolean bar() {\n" +
			"      Zork z;\n" +
			"      return true;\n" +
			"    }\n" +
			"  });\n" +
			"  BugDemo(Foo foo) {\n" +
			"  }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
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
			"class Foo <U extends Foo<U>> {\n" +
			"}\n" +
			"class Bar extends Foo {\n" +
			"}\n",
        },
		"----------\n" +
		"1. ERROR in X.java (at line 15)\n" +
		"	Foo<Bar> foo = null;\n" +
		"	    ^^^\n" +
		"Bound mismatch: The type Bar is not a valid substitute for the bounded parameter <U extends Foo<U>> of the type Foo<U>\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 19)\n" +
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
            "  public enum EnumClass implements EnumInterface<String> {\n" +
            "    ENUM1 { public String getMethod() { return \"ENUM1\";} },\n" +
            "    ENUM2 { public String getMethod() { return \"ENUM2\";} };\n" +
            "  }\n" +
            "}\n" +
            "}\n",
        },
        "----------\n" +
        "1. ERROR in BadEnum.java (at line 10)\n" +
        "	}\n" +
        "	^\n" +
        "Syntax error on token \"}\", delete this token\n" +
        "----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90215
public void test104() {
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
public void test105() {
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
public void test106() {
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
public void test107() {
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
public void test108() {
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
public void test109() {
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
public void test110() {
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
public void test111() {
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
public void test112() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
			"com/annot/Foo.java",
			"package com.annot;\n" +
			"\n" +
			"import static com.annot.TestType.*;\n" +
			"\n" +
			"public class Foo {\n" +
			"	@Test(type=PERFORMANCE)\n" +
			"	public void testBar() throws Exception {\n" +
			"		Test annotation = this.getClass().getMethod(\"testBar\").getAnnotation(Test.class);\n" +
			"		switch (annotation.type()) {\n" +
			"			case PERFORMANCE:\n" +
			"				System.out.println(PERFORMANCE);\n" +
			"				break;\n" +
			"			case CORRECTNESS:\n" +
			"				System.out.println(CORRECTNESS);\n" +
			"				break;\n" +
			"		}		\n" +
			"	}\n" +
			"}",
			"com/annot/Test.java",
			"package com.annot;\n" +
			"\n" +
			"import static com.annot.TestType.CORRECTNESS;\n" +
			"import static java.lang.annotation.ElementType.METHOD;\n" +
			"\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(METHOD)\n" +
			"public @interface Test {\n" +
			"	TestType type() default CORRECTNESS;\n" +
			"}",
			"com/annot/TestType.java",
			"package com.annot;\n" +
			"\n" +
			"public enum TestType {\n" +
			"	CORRECTNESS,\n" +
			"	PERFORMANCE\n" +
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=93789
public void test113() {
	if (this.complianceLevel >= ClassFileConstants.JDK16) {
		return;
	}
    this.runNegativeTest(
        new String[] {
            "X.java",
			"enum BugDemo {\n" +
			"	FOO() {\n" +
			"		static int bar;\n" +
			"	}\n" +
			"}\n",
        },
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	static int bar;\n" +
		"	           ^^^\n" +
		"The field bar cannot be declared static in a non-static inner type, unless initialized with a constant expression\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99428 and https://bugs.eclipse.org/bugs/show_bug.cgi?id=99655
public void test114() {
    this.runConformTest(
        new String[] {
            "EnumTest.java",
			"import java.lang.reflect.*;\n" +
			"import java.lang.annotation.*;\n" +
			"@ExpectedModifiers(Modifier.FINAL)\n" +
			"public enum EnumTest {\n" +
			"	X(255);\n" +
			"	EnumTest(int r) {}\n" +
			"	public static void main(String argv[]) throws Exception {\n" +
			"		test(\"EnumTest\");\n" +
			"		test(\"EnumTest$EnumA\");\n" +
			"		test(\"EnumTest$EnumB\");\n" +
			"		test(\"EnumTest$EnumB2\");\n" +
			"		test(\"EnumTest$EnumB3\");\n" +
			// TODO (kent) need verifier to detect when an Enum should be tagged as abstract
			//"		test(\"EnumTest$EnumC\");\n" +
			//"		test(\"EnumTest$EnumC2\");\n" +
			"		test(\"EnumTest$EnumC3\");\n" +
			"		test(\"EnumTest$EnumD\");\n" +
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
			"	@ExpectedModifiers(Modifier.FINAL|Modifier.STATIC)\n" +
			"	enum EnumA {\n" +
			"		A;\n" +
			"	}\n" +
			"	@ExpectedModifiers(Modifier.STATIC)\n" +
			"	enum EnumB {\n" +
			"		B {\n" +
			"			int value() { return 1; }\n" +
			"		};\n" +
			"		int value(){ return 0; }\n" +
			"	}\n" +
			"	@ExpectedModifiers(Modifier.STATIC)\n" +
			"	enum EnumB2 {\n" +
			"		B2 {};\n" +
			"		int value(){ return 0; }\n" +
			"	}\n" +
			"	@ExpectedModifiers(Modifier.FINAL|Modifier.STATIC)\n" +
			"	enum EnumB3 {\n" +
			"		B3;\n" +
			"		int value(){ return 0; }\n" +
			"	}\n" +
			"	@ExpectedModifiers(Modifier.STATIC)\n" +
			"	enum EnumC implements I {\n" +
			"		C {\n" +
			"			int value() { return 1; }\n" +
			"		};\n" +
			"		int value(){ return 0; }\n" +
			"		public void foo(){}\n" +
			"	}\n" +
			"	@ExpectedModifiers(Modifier.STATIC)\n" +
			"	enum EnumC2 implements I {\n" +
			"		C2 {};\n" +
			"		int value(){ return 0; }\n" +
			"		public void foo(){}\n" +
			"	}\n" +
			"	@ExpectedModifiers(Modifier.FINAL|Modifier.STATIC)\n" +
			"	enum EnumC3 implements I {\n" +
			"		C3;\n" +
			"		int value(){ return 0; }\n" +
			"		public void foo(){}\n" +
			"	}\n" +
			"	@ExpectedModifiers(Modifier.ABSTRACT|Modifier.STATIC)\n" +
			"	enum EnumD {\n" +
			"		D {\n" +
			"			int value() { return 1; }\n" +
			"		};\n" +
			"		abstract int value();\n" +
			"	}\n" +
			"}\n" +
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@interface ExpectedModifiers {\n" +
			"	int value();\n" +
			"}"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101713
public void test115() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"public enum X {\n" +
			"	VALUE;\n" +
			"\n" +
			"	static int ASD;\n" +
			"	final static int CST = 0;\n" +
			"	\n" +
			"	private X() {\n" +
			"		VALUE = null;\n" +
			"		ASD = 5;\n" +
			"		X.VALUE = null;\n" +
			"		X.ASD = 5;\n" +
			"		\n" +
			"		System.out.println(CST);\n" +
			"	}\n" +
			"}\n",
        },
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	VALUE = null;\n" +
		"	^^^^^\n" +
		"Cannot refer to the static enum field X.VALUE within an initializer\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	ASD = 5;\n" +
		"	^^^\n" +
		"Cannot refer to the static enum field X.ASD within an initializer\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	X.VALUE = null;\n" +
		"	  ^^^^^\n" +
		"Cannot refer to the static enum field X.VALUE within an initializer\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 11)\n" +
		"	X.ASD = 5;\n" +
		"	  ^^^\n" +
		"Cannot refer to the static enum field X.ASD within an initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101713 - variation
public void test116() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X { \n" +
			"	BLEU, \n" +
			"	BLANC, \n" +
			"	ROUGE;\n" +
			"	{\n" +
			"		BLEU = null;\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	BLEU = null;\n" +
		"	^^^^\n" +
		"Cannot refer to the static enum field X.BLEU within an initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101713 - variation
public void test117() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X { \n" +
			"	BLEU, \n" +
			"	BLANC, \n" +
			"	ROUGE;\n" +
			"	{\n" +
			"		X x = BLEU.BLANC; // ko\n" +
			"		X x2 = BLEU; // ko\n" +
			"	}\n" +
			"	static {\n" +
			"		X x = BLEU.BLANC; // ok\n" +
			"		X x2 = BLEU; // ok\n" +
			"	}	\n" +
			"	X dummy = BLEU; // ko\n" +
			"	static X DUMMY = BLANC; // ok\n" +
			"	X() {\n" +
			"		X x = BLEU.BLANC; // ko\n" +
			"		X x2 = BLEU; // ko\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	X x = BLEU.BLANC; // ko\n" +
		"	      ^^^^\n" +
		"Cannot refer to the static enum field X.BLEU within an initializer\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	X x = BLEU.BLANC; // ko\n" +
		"	           ^^^^^\n" +
		"Cannot refer to the static enum field X.BLANC within an initializer\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 6)\n" +
		"	X x = BLEU.BLANC; // ko\n" +
		"	           ^^^^^\n" +
		"The static field X.BLANC should be accessed in a static way\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 7)\n" +
		"	X x2 = BLEU; // ko\n" +
		"	       ^^^^\n" +
		"Cannot refer to the static enum field X.BLEU within an initializer\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 10)\n" +
		"	X x = BLEU.BLANC; // ok\n" +
		"	           ^^^^^\n" +
		"The static field X.BLANC should be accessed in a static way\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 13)\n" +
		"	X dummy = BLEU; // ko\n" +
		"	          ^^^^\n" +
		"Cannot refer to the static enum field X.BLEU within an initializer\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 16)\n" +
		"	X x = BLEU.BLANC; // ko\n" +
		"	      ^^^^\n" +
		"Cannot refer to the static enum field X.BLEU within an initializer\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 16)\n" +
		"	X x = BLEU.BLANC; // ko\n" +
		"	           ^^^^^\n" +
		"Cannot refer to the static enum field X.BLANC within an initializer\n" +
		"----------\n" +
		"9. WARNING in X.java (at line 16)\n" +
		"	X x = BLEU.BLANC; // ko\n" +
		"	           ^^^^^\n" +
		"The static field X.BLANC should be accessed in a static way\n" +
		"----------\n" +
		"10. ERROR in X.java (at line 17)\n" +
		"	X x2 = BLEU; // ko\n" +
		"	       ^^^^\n" +
		"Cannot refer to the static enum field X.BLEU within an initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102265
public void test118() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"\n" +
			"public enum X {\n" +
			"		 one,\n" +
			"		 two;\n" +
			"		 \n" +
			"		 static ArrayList someList;\n" +
			"		 \n" +
			"		 private X() {\n" +
			"		 		 if (someList == null) {\n" +
			"		 		 		 someList = new ArrayList();\n" +
			"		 		 }\n" +
			"		 }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 7)\n" +
		"	static ArrayList someList;\n" +
		"	       ^^^^^^^^^\n" +
		"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	if (someList == null) {\n" +
		"	    ^^^^^^^^\n" +
		"Cannot refer to the static enum field X.someList within an initializer\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 11)\n" +
		"	someList = new ArrayList();\n" +
		"	^^^^^^^^\n" +
		"Cannot refer to the static enum field X.someList within an initializer\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 11)\n" +
		"	someList = new ArrayList();\n" +
		"	               ^^^^^^^^^\n" +
		"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" +
		"----------\n");
}
public void test119() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"	BLEU, BLANC, ROUGE;\n" +
			"	final static int CST = 0;\n" +
			"    enum Member {\n" +
			"    	;\n" +
			"        Object obj1 = CST;\n" +
			"        Object obj2 = BLEU;\n" +
			"    }\n" +
			"}\n"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102213
public void test120() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"\n" +
			"	A() {\n" +
			"		final X a = A;\n" +
			"		final X a2 = B.A;\n" +
			"		@Override void foo() {\n" +
			"			System.out.println(String.valueOf(a));\n" +
			"			System.out.println(String.valueOf(a2));\n" +
			"		}\n" +
			"	},\n" +
			"	B() {\n" +
			"		@Override void foo(){}\n" +
			"	};\n" +
			"	abstract void foo();\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		A.foo();\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	final X a = A;\n" +
		"	            ^\n" +
		"Cannot refer to the static enum field X.A within an initializer\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	final X a2 = B.A;\n" +
		"	             ^\n" +
		"Cannot refer to the static enum field X.B within an initializer\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 5)\n" +
		"	final X a2 = B.A;\n" +
		"	               ^\n" +
		"Cannot refer to the static enum field X.A within an initializer\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 5)\n" +
		"	final X a2 = B.A;\n" +
		"	               ^\n" +
		"The static field X.A should be accessed in a static way\n" +
		"----------\n",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=92165
public void test121() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"\n" +
			"	UNKNOWN();\n" +
			"\n" +
			"	private static String error;\n" +
			"\n" +
			"	{\n" +
			"		error = \"error\";\n" +
			"	}\n" +
			"\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	error = \"error\";\n" +
		"	^^^^^\n" +
		"Cannot refer to the static enum field X.error within an initializer\n" +
		"----------\n");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105592
public void test122() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public enum State {\n" +
			"		NORMAL\n" +
			"	}\n" +
			"	public void foo() {\n" +
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
			"enum Foo {\n" +
			" A(0);\n" +
			" Foo(int x) {\n" +
			"    t[0]=x;\n" +
			" }\n" +
			" private static final int[] t = new int[12];\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in Foo.java (at line 4)\n" +
		"	t[0]=x;\n" +
		"	^\n" +
		"Cannot refer to the static enum field Foo.t within an initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=1101417
public void test124() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			" public enum X {\n" +
			"  max {\n" +
			"   { \n" +
			"     val=3;  \n" +
			"   }         \n" +
			"   @Override public String toString() {\n" +
			"     return Integer.toString(val);\n" +
			"   }\n" +
			"  }; \n" +
			"  {\n" +
			"    val=2;\n" +
			"  }\n" +
			"  private int val; \n" +
			"  public static void main(String[] args) {\n" +
			"    System.out.println(max); // 3\n" +
			"  }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	val=3;  \n" +
		"	^^^\n" +
		"Cannot make a static reference to the non-static field val\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
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
			"		int values();\n" +
			"		enum E implements I {\n" +
			"			A, B, C;\n" +
			"		}\n" +
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
			"    enum NoValues {}\n" +
			"    public static void main(String[] args) {\n" +
			"      System.out.println(\"[\"+NoValues.values().length+\"]\");\n" +
			"    }\n" +
			"  }\n"
		},
		"[0]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=126087
public void test127() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"	VALUE {\n" +
			"		void foo() {\n" +
			"		};\n" +
			"	};\n" +
			"	abstract void foo();\n" +
			"    public static void main(String[] args) {\n" +
			"      System.out.println(\"[\"+X.values().length+\"]\");\n" +
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
		"  private static final synthetic X[] ENUM$VALUES;\n";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}

	disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1.class"));
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
			"public enum X {\n" +
			"        A, B, C;\n" +
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
		"// Signature: Ljava/lang/Enum<LX;>;\n" +
		"public final enum X {\n" +
		"  \n" +
		"  // Field descriptor #6 LX;\n" +
		"  public static final enum X A;\n" +
		"  \n" +
		"  // Field descriptor #6 LX;\n" +
		"  public static final enum X B;\n" +
		"  \n" +
		"  // Field descriptor #6 LX;\n" +
		"  public static final enum X C;\n" +
		"  \n" +
		"  // Field descriptor #10 [LX;\n" +
		"  private static final synthetic X[] ENUM$VALUES;\n" +
		"  \n" +
		"  // Method descriptor #12 ()V\n" +
		"  // Stack: 4, Locals: 0\n" +
		"  static {};\n" +
		"     0  new X [1]\n" +
		"     3  dup\n" +
		"     4  ldc <String \"A\"> [14]\n" +
		"     6  iconst_0\n" +
		"     7  invokespecial X(java.lang.String, int) [15]\n" +
		"    10  putstatic X.A : X [19]\n" +
		"    13  new X [1]\n" +
		"    16  dup\n" +
		"    17  ldc <String \"B\"> [21]\n" +
		"    19  iconst_1\n" +
		"    20  invokespecial X(java.lang.String, int) [15]\n" +
		"    23  putstatic X.B : X [22]\n" +
		"    26  new X [1]\n" +
		"    29  dup\n" +
		"    30  ldc <String \"C\"> [24]\n" +
		"    32  iconst_2\n" +
		"    33  invokespecial X(java.lang.String, int) [15]\n" +
		"    36  putstatic X.C : X [25]\n" +
		"    39  iconst_3\n" +
		"    40  anewarray X [1]\n" +
		"    43  dup\n" +
		"    44  iconst_0\n" +
		"    45  getstatic X.A : X [19]\n" +
		"    48  aastore\n" +
		"    49  dup\n" +
		"    50  iconst_1\n" +
		"    51  getstatic X.B : X [22]\n" +
		"    54  aastore\n" +
		"    55  dup\n" +
		"    56  iconst_2\n" +
		"    57  getstatic X.C : X [25]\n" +
		"    60  aastore\n" +
		"    61  putstatic X.ENUM$VALUES : X[] [27]\n" +
		"    64  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 2]\n" +
		"        [pc: 39, line: 1]\n" +
		"  \n" +
		"  // Method descriptor #18 (Ljava/lang/String;I)V\n" +
		"  // Stack: 3, Locals: 3\n" +
		"  private X(java.lang.String arg0, int arg1);\n" +
		"    0  aload_0 [this]\n" +
		"    1  aload_1 [arg0]\n" +
		"    2  iload_2 [arg1]\n" +
		"    3  invokespecial java.lang.Enum(java.lang.String, int) [31]\n" +
		"    6  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 1]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 7] local: this index: 0 type: X\n" +
		"  \n" +
		"  // Method descriptor #34 ()[LX;\n" +
		"  // Stack: 5, Locals: 3\n" +
		"  public static X[] values();\n" +
		"     0  getstatic X.ENUM$VALUES : X[] [27]\n" +
		"     3  dup\n" +
		"     4  astore_0\n" +
		"     5  iconst_0\n" +
		"     6  aload_0\n" +
		"     7  arraylength\n" +
		"     8  dup\n" +
		"     9  istore_1\n" +
		"    10  anewarray X [1]\n" +
		"    13  dup\n" +
		"    14  astore_2\n" +
		"    15  iconst_0\n" +
		"    16  iload_1\n" +
		"    17  invokestatic java.lang.System.arraycopy(java.lang.Object, int, java.lang.Object, int, int) : void [35]\n" +
		"    20  aload_2\n" +
		"    21  areturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 1]\n" +
		"  \n" +
		"  // Method descriptor #42 (Ljava/lang/String;)LX;\n" +
		"  // Stack: 2, Locals: 1\n" +
		"  public static X valueOf(java.lang.String arg0);\n" +
		"     0  ldc <Class X> [1]\n" +
		"     2  aload_0 [arg0]\n" +
		"     3  invokestatic java.lang.Enum.valueOf(java.lang.Class, java.lang.String) : java.lang.Enum [43]\n" +
		"     6  checkcast X [1]\n" +
		"     9  areturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 1]\n" +
		"}";

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
				"Action.java",
				"enum Action { ONE, TWO }"
			},
			"12"
		);

	this.runConformTest(
		new String[] {
			"Action.java",
			"enum Action {ONE, TWO, THREE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"X.java",
		"12default",
		null,
		false,
		null,
		null,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=145732
public void test131() {
	this.runConformTest(
         new String[] {
        		 "X.java",
     			"public enum X {\n" +
    			"	//A,B\n" +
    			"	;\n" +
    			"	public static void main(String[] args) {\n" +
    			"		try {\n" +
    			"			System.out.println(X.valueOf(null));\n" +
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
     			"public enum X {\n" +
    			"	A,B\n" +
    			"	;\n" +
    			"	public static void main(String[] args) {\n" +
    			"		try {\n" +
    			"			System.out.println(X.valueOf(null));\n" +
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
     			"public enum X {\n" +
    			"	A, B, C;\n" +
    			"	public static void main(String[] args) {}\n" +
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
		"  // Method descriptor #12 ()V\n" +
		"  // Stack: 4, Locals: 0\n" +
		"  static {};\n" +
		"     0  new X [1]\n" +
		"     3  dup\n" +
		"     4  ldc <String \"A\"> [14]\n" +
		"     6  iconst_0\n" +
		"     7  invokespecial X(java.lang.String, int) [15]\n" +
		"    10  putstatic X.A : X [19]\n" +
		"    13  new X [1]\n" +
		"    16  dup\n" +
		"    17  ldc <String \"B\"> [21]\n" +
		"    19  iconst_1\n" +
		"    20  invokespecial X(java.lang.String, int) [15]\n" +
		"    23  putstatic X.B : X [22]\n" +
		"    26  new X [1]\n" +
		"    29  dup\n" +
		"    30  ldc <String \"C\"> [24]\n" +
		"    32  iconst_2\n" +
		"    33  invokespecial X(java.lang.String, int) [15]\n" +
		"    36  putstatic X.C : X [25]\n" +
		"    39  iconst_3\n" +
		"    40  anewarray X [1]\n" +
		"    43  dup\n" +
		"    44  iconst_0\n" +
		"    45  getstatic X.A : X [19]\n" +
		"    48  aastore\n" +
		"    49  dup\n" +
		"    50  iconst_1\n" +
		"    51  getstatic X.B : X [22]\n" +
		"    54  aastore\n" +
		"    55  dup\n" +
		"    56  iconst_2\n" +
		"    57  getstatic X.C : X [25]\n" +
		"    60  aastore\n" +
		"    61  putstatic X.ENUM$VALUES : X[] [27]\n" +
		"    64  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 2]\n" +
		"        [pc: 39, line: 1]\n";

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
			"public enum X {\n" +
			"    INITIAL ,\n" +
			"    OPENED {\n" +
			"        {\n" +
			"            System.out.printf(\"After the %s constructor\\n\",INITIAL);\n" +
			"        }\n" +
			"    }\n" +
			"}",
        },
        "----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	System.out.printf(\"After the %s constructor\\n\",INITIAL);\n" +
		"	                                               ^^^^^^^\n" +
		"Cannot refer to the static enum field X.INITIAL within an initializer\n" +
		"----------\n",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149562
// a default case is required to consider that b is initialized (in case E
// takes new values in the future)
public void test135() {
    this.runNegativeTest(
        new String[] {
            "E.java",
			"public enum E {\n" +
			"    A,\n" +
			"    B\n" +
			"}",
            "X.java",
			"public class X {\n" +
			"    boolean foo(E e) {\n" +
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
		"1. ERROR in X.java (at line 12)\n" +
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
        "    public enum MyEnum implements Interface {\n" +
        "        ;\n" +
        "\n" +
        "        MyEnum(int value) { this.value = value; }        \n" +
        "        public int value() { return this.value; }\n" +
        "\n" +
        "        private int value;\n" +
        "    }\n" +
        "\n" +
        "    public static void main(String[] args) {\n" +
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
			"public enum X {\n" +
			"	PLUS {\n" +
			"		double eval(double x, double y) {\n" +
			"			return x + y;\n" +
			"		}\n" +
			"	},\n" +
			"	MINUS {\n" +
			"		@Override\n" +
			"		abstract double eval(double x, double y);\n" +
			"	};\n" +
			"	abstract double eval(double x, double y);\n" +
			"}\n" +
			"\n", // =================
		 },
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	double eval(double x, double y) {\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The method eval(double, double) of type new X(){} should be tagged with @Override since it actually overrides a superclass method\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	MINUS {\n" +
		"	^^^^^\n" +
		"The enum constant MINUS cannot define abstract methods\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
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
			"public enum X {\n" +
			"	PLUS {\n" +
			"		double eval(double x, double y) {\n" +
			"			return x + y;\n" +
			"		}\n" +
			"	},\n" +
			"	MINUS {\n" +
			"		abstract double eval2(double x, double y);\n" +
			"	};\n" +
			"\n" +
			"	abstract double eval(double x, double y);\n" +
			"}\n" +
			"\n", // =================
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	double eval(double x, double y) {\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The method eval(double, double) of type new X(){} should be tagged with @Override since it actually overrides a superclass method\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	MINUS {\n" +
		"	^^^^^\n" +
		"The enum constant MINUS cannot define abstract methods\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	MINUS {\n" +
		"	^^^^^\n" +
		"The enum constant MINUS must implement the abstract method eval(double, double)\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 8)\n" +
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
    			"public enum X {\n" +
    			"	PLUS {/*ANONYMOUS*/}, MINUS;\n" +
    			"	void bar(X x) {\n" +
    			"		Runnable r = (Runnable)x;\n" +
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
    			"public enum X {\n" +
    			"	PLUS, MINUS;\n" +
    			"	void bar(X x) {\n" +
    			"		Runnable r = (Runnable)x;\n" +
    			"	}\n" +
    			"}", // =================
     },
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	Runnable r = (Runnable)x;\n" +
		"	             ^^^^^^^^^^^\n" +
		"Cannot cast from X to Runnable\n" +
		"----------\n");
}
public void test142() {
 this.runConformTest(
     new String[] {
    	        "X.java",
    			"enum Week {\n" +
    			"	Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday\n" +
    			"}\n" +
    			"public class X {\n" +
    			"	public static void main(String[] args) {\n" +
    			"		new X().foo();\n" +
    			"		new X().bar();		\n" +
    			"	}\n" +
    			"	void foo() {\n" +
    			"		for (Week w : Week.values())\n" +
    			"			System.out.print(w + \" \");\n" +
    			"	}\n" +
    			"	void bar() {\n" +
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
				"public enum X {\n" +
				"  A {\n" +
				"    @Override\n" +
				"    public String toString() {\n" +
				"      return a();\n" +
				"    }\n" +
				"    public abstract String a();\n" +
				"  }\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	A {\n" +
		"	^\n" +
		"The enum constant A cannot define abstract methods\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
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
				"public enum X<T> {}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public enum X<T> {}\n" +
		"	              ^\n" +
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
			"EnumA.java",
			"public enum EnumA {\n" +
			"  B1,\n" +
			"  B2;\n" +
			"  public void foo(){}\n" +
			"}",
			"ClassC.java",
			"public class ClassC {\n" +
			"  void bar() {\n" +
			"    EnumA.B1.B1.foo();\n" +
			"    EnumA.B1.B2.foo();\n" +
			"  }\n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in ClassC.java (at line 3)\n" +
		"	EnumA.B1.B1.foo();\n" +
		"	         ^^\n" +
		"The static field EnumA.B1 should be accessed in a static way\n" +
		"----------\n" +
		"2. ERROR in ClassC.java (at line 4)\n" +
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
				"	enum MyEnum {\n" +
				"		A, B\n" +
				"	}\n" +
				"	final String test;\n" +
				"	public X(MyEnum e) { // error\n" +
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
		"1. ERROR in X.java (at line 6)\n" +
		"	public X(MyEnum e) { // error\n" +
		"	       ^^^^^^^^^^^\n" +
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
				"	enum MyEnum {\n" +
				"		A, B\n" +
				"	}\n" +
				"	final String test;\n" +
				"	public X(MyEnum e) { // error\n" +
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
		"1. ERROR in X.java (at line 6)\n" +
		"	public X(MyEnum e) { // error\n" +
		"	       ^^^^^^^^^^^\n" +
		"The blank final field test may not have been initialized\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 7)\n" +
		"	switch (e) {\n" +
		"	        ^\n" +
		"The switch over the enum type X.MyEnum should have a default case\n" +
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
					"	public abstract enum E {\n" +
					"		SUCCESS;\n" +
					"	}\n" +
					"}\n"
			},
			"----------\n" +
			"1. ERROR in p\\X.java (at line 3)\n" +
			"	public abstract enum E {\n" +
			"	                     ^\n" +
			"Illegal modifier for the member enum E; only public, protected, private & static are permitted\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	runConformTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"Y.java",
			"import p.X;\n" +
			"public class Y {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(X.E.SUCCESS);\n" +
			"	}\n" +
			"}\n"
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"null" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.Excuse.JavacHasErrorsEclipseHasNone /* javac test options */); // note that Eclipse has errors for X while javac alsore reports for X - no conflict
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test148() {
	this.runNegativeTest(
			new String[] {
					"p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"	public abstract enum E implements Runnable {\n" +
					"		SUCCESS;\n" +
					"	}\n" +
					"}\n"
			},
			"----------\n" +
			"1. ERROR in p\\X.java (at line 3)\n" +
			"	public abstract enum E implements Runnable {\n" +
			"	                     ^\n" +
			"Illegal modifier for the member enum E; only public, protected, private & static are permitted\n" +
			"----------\n" +
			"2. ERROR in p\\X.java (at line 3)\n" +
			"	public abstract enum E implements Runnable {\n" +
			"	                     ^\n" +
			"The type X.E must implement the inherited abstract method Runnable.run()\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	runConformTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"Y.java",
			"import p.X;\n" +
			"public class Y {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(X.E.SUCCESS);\n" +
			"	}\n" +
			"}\n"
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"null" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.Excuse.JavacHasErrorsEclipseHasNone /* javac test options */);// see prev note
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test149() throws Exception {
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

	this.runConformTest(
			new String[] {
					"Y.java",
					"import p.X;\n" +
					"public class Y {\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(X.E.SUCCESS);\n" +
					"	}\n" +
					"}\n"
			},
			"SUCCESS",
			null,
			false,
			null);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test151() throws Exception {
	this.runConformTest(
			new String[] {
					"p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"	public enum E implements Runnable {\n" +
					"		SUCCESS {};\n" +
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
		"public abstract static enum p.X$E implements java.lang.Runnable {\n";

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
public void test152() {
	this.runConformTest(
			new String[] {
					"p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"	public enum E implements Runnable {\n" +
					"		SUCCESS {};\n" +
					"		public void run(){}\n" +
					"	}\n" +
					"}\n"
			},
			"");
	this.runConformTest(
		new String[] {
				"Y.java",
				"import p.X;\n" +
				"public class Y {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(X.E.SUCCESS);\n" +
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
				"TestEnum.java",
				"public enum TestEnum {\n" +
				"	RED, GREEN, BLUE; \n" +
				"    static int test = 0;  \n" +
				"\n" +
				"    TestEnum() {\n" +
				"        TestEnum.test=10;\n" +
				"    }\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in TestEnum.java (at line 6)\n" +
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
				"TestEnum2.java",
				"public enum TestEnum2 {\n" +
				"	; \n" +
				"   static int test = 0;  \n" +
				"	TestEnum2() {\n" +
				"        TestEnum2.test=11;\n" +
				"   }\n" +
				"}\n" +
				"class X {\n" +
				"	static int test = 0;\n" +
				"	X() {\n" +
				"		X.test = 13;\n" +
				"	}\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in TestEnum2.java (at line 5)\n" +
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
				"TestEnum.java",
				"public enum TestEnum {\n" +
				"	RED, GREEN, BLUE; \n" +
				"    static int test = 0;  \n" +
				"}\n" +
				"\n" +
				"enum TestEnum2 {\n" +
				"	; \n" +
				"    TestEnum2() {\n" +
				"        TestEnum.test=12;\n" +
				"    }\n" +
				"}\n"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test156() {
	this.runConformTest(
		new String[] {
				"TestEnum.java",
				"public enum TestEnum {\n" +
				"	RED, GREEN, BLUE; \n" +
				"    static int test = 0;  \n" +
				"\n" +
				"    TestEnum() {\n" +
				"        new Object() {\n" +
				"			{ TestEnum.test=10; }\n" +
				"		};\n" +
				"    }\n" +
				"}\n"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test157() {
	this.runNegativeTest(
		new String[] {
				"Foo.java",
				"enum Foo {\n" +
				"	ONE, TWO, THREE;\n" +
				"	static int val = 10;\n" +
				"	Foo () {\n" +
				"		this(Foo.val);\n" +
				"		System.out.println(Foo.val);\n" +
				"	}\n" +
				"	Foo(int i){}\n" +
				"	{\n" +
				"		System.out.println(Foo.val);\n" +
				"	}\n" +
				"	int field = Foo.val;\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in Foo.java (at line 5)\n" +
		"	this(Foo.val);\n" +
		"	         ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"2. ERROR in Foo.java (at line 6)\n" +
		"	System.out.println(Foo.val);\n" +
		"	                       ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"3. ERROR in Foo.java (at line 10)\n" +
		"	System.out.println(Foo.val);\n" +
		"	                       ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"4. ERROR in Foo.java (at line 12)\n" +
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
				"Foo.java",
				"enum Foo {\n" +
				"	ONE, TWO, THREE;\n" +
				"	static int val = 10;\n" +
				"	Foo () {\n" +
				"		this(val);\n" +
				"		System.out.println(val);\n" +
				"	}\n" +
				"	Foo(int i){}\n" +
				"	{\n" +
				"		System.out.println(val);\n" +
				"	}\n" +
				"	int field = val;\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in Foo.java (at line 5)\n" +
		"	this(val);\n" +
		"	     ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"2. ERROR in Foo.java (at line 6)\n" +
		"	System.out.println(val);\n" +
		"	                   ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"3. ERROR in Foo.java (at line 10)\n" +
		"	System.out.println(val);\n" +
		"	                   ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"4. ERROR in Foo.java (at line 12)\n" +
		"	int field = val;\n" +
		"	            ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test159() {
	this.runNegativeTest(
		new String[] {
				"Foo.java",
				"enum Foo {\n" +
				"	ONE, TWO, THREE;\n" +
				"	static int val = 10;\n" +
				"	Foo () {\n" +
				"		this(get().val);\n" +
				"		System.out.println(get().val);\n" +
				"	}\n" +
				"	Foo(int i){}\n" +
				"	{\n" +
				"		System.out.println(get().val);\n" +
				"	}\n" +
				"	int field = get().val;\n" +
				"	Foo get() { return ONE; }\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in Foo.java (at line 5)\n" +
		"	this(get().val);\n" +
		"	     ^^^\n" +
		"Cannot refer to an instance method while explicitly invoking a constructor\n" +
		"----------\n" +
		"2. WARNING in Foo.java (at line 5)\n" +
		"	this(get().val);\n" +
		"	           ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"3. ERROR in Foo.java (at line 5)\n" +
		"	this(get().val);\n" +
		"	           ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"4. WARNING in Foo.java (at line 6)\n" +
		"	System.out.println(get().val);\n" +
		"	                         ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"5. ERROR in Foo.java (at line 6)\n" +
		"	System.out.println(get().val);\n" +
		"	                         ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"6. WARNING in Foo.java (at line 10)\n" +
		"	System.out.println(get().val);\n" +
		"	                         ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"7. ERROR in Foo.java (at line 10)\n" +
		"	System.out.println(get().val);\n" +
		"	                         ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"8. WARNING in Foo.java (at line 12)\n" +
		"	int field = get().val;\n" +
		"	                  ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"9. ERROR in Foo.java (at line 12)\n" +
		"	int field = get().val;\n" +
		"	                  ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test160() {
	this.runNegativeTest(
		new String[] {
				"Foo.java",
				"enum Foo {\n" +
				"	ONE, TWO, THREE;\n" +
				"	static int val = 10;\n" +
				"	Foo () {\n" +
				"		this(get().val = 1);\n" +
				"		System.out.println(get().val = 2);\n" +
				"	}\n" +
				"	Foo(int i){}\n" +
				"	{\n" +
				"		System.out.println(get().val = 3);\n" +
				"	}\n" +
				"	int field = get().val = 4;\n" +
				"	Foo get() { return ONE; }\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in Foo.java (at line 5)\n" +
		"	this(get().val = 1);\n" +
		"	     ^^^\n" +
		"Cannot refer to an instance method while explicitly invoking a constructor\n" +
		"----------\n" +
		"2. WARNING in Foo.java (at line 5)\n" +
		"	this(get().val = 1);\n" +
		"	           ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"3. ERROR in Foo.java (at line 5)\n" +
		"	this(get().val = 1);\n" +
		"	           ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"4. WARNING in Foo.java (at line 6)\n" +
		"	System.out.println(get().val = 2);\n" +
		"	                         ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"5. ERROR in Foo.java (at line 6)\n" +
		"	System.out.println(get().val = 2);\n" +
		"	                         ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"6. WARNING in Foo.java (at line 10)\n" +
		"	System.out.println(get().val = 3);\n" +
		"	                         ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"7. ERROR in Foo.java (at line 10)\n" +
		"	System.out.println(get().val = 3);\n" +
		"	                         ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n" +
		"8. WARNING in Foo.java (at line 12)\n" +
		"	int field = get().val = 4;\n" +
		"	                  ^^^\n" +
		"The static field Foo.val should be accessed in a static way\n" +
		"----------\n" +
		"9. ERROR in Foo.java (at line 12)\n" +
		"	int field = get().val = 4;\n" +
		"	                  ^^^\n" +
		"Cannot refer to the static enum field Foo.val within an initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test161() {
	this.runConformTest(
		new String[] {
				"EnumTest1.java",
				"enum EnumTest1 {\n" +
				"	;\n" +
				"	static int foo = EnumTest2.bar;\n" +
				"}\n" +
				"enum EnumTest2 {\n" +
				"	;\n" +
				"	static int bar = EnumTest1.foo;\n" +
				"}\n"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239225
public void test162() {
	this.runNegativeTest(
			new String[] {
				"Status.java", // =================
				"import java.util.HashMap;\n" +
				"import java.util.Map;\n" +
				"\n" +
				"public enum Status {\n" +
				"	GOOD((byte) 0x00), BAD((byte) 0x02);\n" +
				"\n" +
				"	private static Map<Byte, Status> mapping;\n" +
				"\n" +
				"	private Status(final byte newValue) {\n" +
				"\n" +
				"		if (Status.mapping == null) {\n" +
				"			Status.mapping = new HashMap<Byte, Status>();\n" +
				"		}\n" +
				"\n" +
				"		Status.mapping.put(newValue, this);\n" +
				"	}\n" +
				"}\n", // =================
			},
			"----------\n" +
			"1. ERROR in Status.java (at line 11)\n" +
			"	if (Status.mapping == null) {\n" +
			"	           ^^^^^^^\n" +
			"Cannot refer to the static enum field Status.mapping within an initializer\n" +
			"----------\n" +
			"2. ERROR in Status.java (at line 12)\n" +
			"	Status.mapping = new HashMap<Byte, Status>();\n" +
			"	       ^^^^^^^\n" +
			"Cannot refer to the static enum field Status.mapping within an initializer\n" +
			"----------\n" +
			"3. ERROR in Status.java (at line 15)\n" +
			"	Status.mapping.put(newValue, this);\n" +
			"	       ^^^^^^^\n" +
			"Cannot refer to the static enum field Status.mapping within an initializer\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239225 - variation
public void test163() {
	this.runConformTest(
			new String[] {
				"Status.java", // =================
				"import java.util.HashMap;\n" +
				"import java.util.Map;\n" +
				"\n" +
				"enum Status {\n" +
				"	GOOD((byte) 0x00), BAD((byte) 0x02);\n" +
				"	private byte value;\n" +
				"	private static Map<Byte, Status> mapping;\n" +
				"	private Status(final byte newValue) {\n" +
				"		this.value = newValue;\n" +
				"	}\n" +
				"	static {\n" +
				"		Status.mapping = new HashMap<Byte, Status>();\n" +
				"		for (Status s : values()) {\n" +
				"			Status.mapping.put(s.value, s);\n" +
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
			"public enum X {\n" +
			"	;\n" +
			"	private X valueOf(String arg0) { return null; }\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	private X valueOf(String arg0) { return null; }\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^\n" +
		"The enum X already defines the method valueOf(String) implicitly\n" +
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
			"public enum X {\n" +
			"        ;\n" +
			"        private X valueOf(String arg0) { return null; }//9\n" +
			"        private X valueOf(String arg0) { return null; }//10\n" +
			"        private X valueOf(String arg0) { return null; }//11\n" +
			"        void foo() {\n" +
			"        	int i = valueOf(\"\");\n" +
			"        }\n" +
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
		"10. ERROR in X.java (at line 17)\n" +
		"	private X valueOf(String arg0) { return null; }//9\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^\n" +
		"The enum X already defines the method valueOf(String) implicitly\n" +
		"----------\n" +
		"11. ERROR in X.java (at line 18)\n" +
		"	private X valueOf(String arg0) { return null; }//10\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^\n" +
		"The enum X already defines the method valueOf(String) implicitly\n" +
		"----------\n" +
		"12. ERROR in X.java (at line 19)\n" +
		"	private X valueOf(String arg0) { return null; }//11\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^\n" +
		"The enum X already defines the method valueOf(String) implicitly\n" +
		"----------\n" +
		"13. ERROR in X.java (at line 21)\n" +
		"	int i = valueOf(\"\");\n" +
		"	        ^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from X to int\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251814
public void test166() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"public enum X {\n" +
			"        ;\n" +
			"        private int valueOf(String arg0) { return 0; }//11\n" +
			"        void foo() {\n" +
			"        	int i = valueOf(\"\");\n" +
			"        }\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	private int valueOf(String arg0) { return 0; }//11\n" +
		"	            ^^^^^^^^^^^^^^^^^^^^\n" +
		"The enum X already defines the method valueOf(String) implicitly\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	int i = valueOf(\"\");\n" +
		"	        ^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from X to int\n" +
		"----------\n",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	// check for presence of #valueOf(...) in problem type
	String expectedOutput =
		"public final enum X {\n" +
		"  \n" +
		"  // Method descriptor #6 (Ljava/lang/String;I)V\n" +
		"  // Stack: 3, Locals: 3\n" +
		"  private X(java.lang.String arg0, int arg1);\n" +
		"     0  new java.lang.Error [8]\n" +
		"     3  dup\n" +
		"     4  ldc <String \"Unresolved compilation problems: \\n\\tThe enum X already defines the method valueOf(String) implicitly\\n\\tType mismatch: cannot convert from X to int\\n\"> [10]\n" +
		"     6  invokespecial java.lang.Error(java.lang.String) [12]\n" +
		"     9  athrow\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 10] local: this index: 0 type: X\n" +
		"  \n" +
		"  // Method descriptor #20 ()V\n" +
		"  // Stack: 3, Locals: 1\n" +
		"  void foo();\n" +
		"     0  new java.lang.Error [8]\n" +
		"     3  dup\n" +
		"     4  ldc <String \"Unresolved compilation problem: \\n\\tType mismatch: cannot convert from X to int\\n\"> [21]\n" +
		"     6  invokespecial java.lang.Error(java.lang.String) [12]\n" +
		"     9  athrow\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 5]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 10] local: this index: 0 type: X\n" +
		"}";

	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251814 - variation
public void test167() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"public enum X {\n" +
			"    ;\n" +
			"    static int valueOf(String arg0) { return 0; }//9\n" +
			"    void foo() {\n" +
			"    	int i = X.valueOf(\"\");\n" +
			"    }\n" +
			"}\n",
			"Other.java",// =================
			"public class Other {\n" +
			"    void foo() {\n" +
			"    	int i = X.valueOf(\"\");\n" +
			"    }\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	static int valueOf(String arg0) { return 0; }//9\n" +
		"	           ^^^^^^^^^^^^^^^^^^^^\n" +
		"The enum X already defines the method valueOf(String) implicitly\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	int i = X.valueOf(\"\");\n" +
		"	        ^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from X to int\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in Other.java (at line 3)\n" +
		"	int i = X.valueOf(\"\");\n" +
		"	        ^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from X to int\n" +
		"----------\n",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	// check consistency of problem when incremental compiling against X problemType
	this.runNegativeTest(
		new String[] {
				"Other.java",// =================
				"public class Other {\n" +
				"    void foo() {\n" +
				"    	int i = X.valueOf(\"\");\n" +
				"    }\n" +
				"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in Other.java (at line 3)\n" +
		"	int i = X.valueOf(\"\");\n" +
		"	          ^^^^^^^\n" +
		"The method valueOf(Class<T>, String) in the type Enum<X> is not applicable for the arguments (String)\n" +
		"----------\n",
		null,
		false, // flush output
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
			"enum BadEnum {\n" +
			"    CRAZY(CRAZY), // <-- illegal forward reference reported by all compilers\n" +
			"    IMPOSSIBLE(BadEnum.IMPOSSIBLE); // <-- illegal forward reference (javac 1.6 only)\n" +
			"    private BadEnum(BadEnum self) {\n" +
			"    }\n" +
			"}\n" +
			"public class X {\n" +
			"    X x1 = new X(x1);//1 - WRONG\n" +
			"    static X X2 = new X(X.X2);//2 - OK\n" +
			"    X x3 = new X(this.x3);//3 - OK\n" +
			"    X(X x) {}\n" +
			"    X(int i) {}\n" +
			"    static int VALUE() { return 13; }\n" +
			"    int value() { return 14; }\n" +
			"}\n" +
			"class Y extends X {\n" +
			"    X x1 = new X(x1);//6 - WRONG\n" +
			"    static X X2 = new X(Y.X2);//7 - OK\n" +
			"    X x3 = new X(this.x3);//8 - OK\n" +
			"    Y(Y y) { super(y); }\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	CRAZY(CRAZY), // <-- illegal forward reference reported by all compilers\n" +
		"	      ^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	IMPOSSIBLE(BadEnum.IMPOSSIBLE); // <-- illegal forward reference (javac 1.6 only)\n" +
		"	                   ^^^^^^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 8)\n" +
		"	X x1 = new X(x1);//1 - WRONG\n" +
		"	             ^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 17)\n" +
		"	X x1 = new X(x1);//6 - WRONG\n" +
		"	  ^^\n" +
		"The field Y.x1 is hiding a field from type X\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 17)\n" +
		"	X x1 = new X(x1);//6 - WRONG\n" +
		"	             ^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 18)\n" +
		"	static X X2 = new X(Y.X2);//7 - OK\n" +
		"	         ^^\n" +
		"The field Y.X2 is hiding a field from type X\n" +
		"----------\n" +
		"7. WARNING in X.java (at line 19)\n" +
		"	X x3 = new X(this.x3);//8 - OK\n" +
		"	  ^^\n" +
		"The field Y.x3 is hiding a field from type X\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=255452 - variation
public void test169() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"enum BadEnum {\n" +
			"    NOWAY(BadEnum.NOWAY.CONST),\n" +
			"    INVALID(INVALID.CONST),\n" +
			"    WRONG(WRONG.VALUE()),\n" +
			"    ILLEGAL(ILLEGAL.value());\n" +
			"    final static int CONST = 12;\n" +
			"    private BadEnum(int i) {\n" +
			"    }\n" +
			"    static int VALUE() { return 13; }\n" +
			"    int value() { return 14; }\n" +
			"}\n" +
			"public class X {\n" +
			"    final static int CONST = 12;\n" +
			"    X x4 = new X(x4.CONST);//4 - WRONG\n" +
			"    X x5 = new X(x5.value());//5 - WRONG\n" +
			"    X(int i) {}\n" +
			"    static int VALUE() { return 13; }\n" +
			"    int value() { return 14; }\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	NOWAY(BadEnum.NOWAY.CONST),\n" +
		"	              ^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 2)\n" +
		"	NOWAY(BadEnum.NOWAY.CONST),\n" +
		"	                    ^^^^^\n" +
		"The static field BadEnum.CONST should be accessed in a static way\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	INVALID(INVALID.CONST),\n" +
		"	        ^^^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 3)\n" +
		"	INVALID(INVALID.CONST),\n" +
		"	                ^^^^^\n" +
		"The static field BadEnum.CONST should be accessed in a static way\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 4)\n" +
		"	WRONG(WRONG.VALUE()),\n" +
		"	      ^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 4)\n" +
		"	WRONG(WRONG.VALUE()),\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"The static method VALUE() from the type BadEnum should be accessed in a static way\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 5)\n" +
		"	ILLEGAL(ILLEGAL.value());\n" +
		"	        ^^^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 14)\n" +
		"	X x4 = new X(x4.CONST);//4 - WRONG\n" +
		"	             ^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"9. WARNING in X.java (at line 14)\n" +
		"	X x4 = new X(x4.CONST);//4 - WRONG\n" +
		"	                ^^^^^\n" +
		"The static field X.CONST should be accessed in a static way\n" +
		"----------\n" +
		"10. ERROR in X.java (at line 15)\n" +
		"	X x5 = new X(x5.value());//5 - WRONG\n" +
		"	             ^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=263877
public void test170() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"enum Days {\n" +
			"    Monday(\"Mon\", Days.OFFSET + 0),    // should not complain\n" +
			"    Tuesday(\"Tue\", Days.Wednesday.hashCode()),   // should complain since enum constant\n" +
			"    Wednesday(\"Wed\", OFFSET + 2);   // should complain since unqualified\n" +
			"    public static final int OFFSET = 0;  // cannot move this above, else more errors\n" +
			"    Days(String abbr, int index) {\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"class X {\n" +
			"    public static final int FOO = X.OFFSET + 0;\n" +
			"    public static final int BAR = OFFSET + 1;\n" +
			"    public static final int OFFSET = 0;  // cannot move this above, else more errors\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	Tuesday(\"Tue\", Days.Wednesday.hashCode()),   // should complain since enum constant\n" +
		"	                    ^^^^^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	Wednesday(\"Wed\", OFFSET + 2);   // should complain since unqualified\n" +
		"	                 ^^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 12)\n" +
		"	public static final int BAR = OFFSET + 1;\n" +
		"	                              ^^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=267670. Make sure we don't emit any unused
// warnings about enumerators. Since these could be used in indirect ways not obvious.
public void test171() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"public class X { \n" +
			"    private enum Colors {\n" +
			"	     BLEU,\n" +
			"	     BLANC,\n" +
			"	     ROUGE\n" +
			"	 }\n" +
			"	public static void main(String[] args) {\n" +
			"		for (Colors c: Colors.values()) {\n" +
			"           System.out.print(c);\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		null, customOptions,
		"",
		"BLEUBLANCROUGE", null,
		JavacTestOptions.DEFAULT);
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
			"    private enum Greet {\n" +
			"	     HELLO, HOWDY, BONJOUR; \n" +
			"    }\n" +
			"	 private enum Colors {\n" +
			"        RED, BLACK, BLUE;\n"+
			"    }\n" +
            "   private enum Complaint {" +
            "       WARNING, ERROR, FATAL_ERROR, PANIC;\n" +
            "   }\n" +
			"	public static void main(String[] args) {\n" +
			"		Greet g = Greet.valueOf(\"HELLO\");\n" +
		    "		System.out.print(g);\n" +
		    "       Colors c = Enum.valueOf(Colors.class, \"RED\");\n" +
		    "		System.out.print(c);\n" +
		    "   }\n" +
			"}\n"
		},
		null, customOptions,
		"----------\n" +
		"1. WARNING in X.java (at line 8)\n" +
		"	private enum Complaint {       WARNING, ERROR, FATAL_ERROR, PANIC;\n" +
		"	             ^^^^^^^^^\n" +
		"The type X.Complaint is never used locally\n" +
		"----------\n",
		"HELLORED", null,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=273990
public void test173() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"public enum E {\n" +
			"	A(E.STATIK);\n" +
			"	private static int STATIK = 1;\n" +
			"	private E(final int i) {}\n" +
			"}\n",
			"E2.java",
			"public enum E2 {\n" +
			"	A(E2.STATIK);\n" +
			"	static int STATIK = 1;\n" +
			"	private E2(final int i) {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in E.java (at line 2)\n" +
		"	A(E.STATIK);\n" +
		"	    ^^^^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in E2.java (at line 2)\n" +
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
			"interface S {}\n" +
			"enum A implements S {\n" +
			"	L;\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) throws Exception {\n" +
			"		i(A.L);\n" +
			"	}\n" +
			"	static void i(Enum<? extends S> enumConstant) {\n" +
			"		Map m = new HashMap();\n" +
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
			"interface S {}\n" +
			"enum A implements S {\n" +
			"	L, M, N, O;\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) throws Exception {\n" +
			"		i(new Enum[] {A.L, A.M, A.N, A.O});\n" +
			"	}\n" +
			"	static void i(Enum[] tab) {\n" +
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
			"public enum X {\n" +
			"	A(\"\"), B(\"SUCCESS\"), C(\"Hello\");\n" +
			"	\n" +
			"	String message;\n" +
			"	\n" +
			"	X(@Deprecated String s) {\n" +
			"		this.message = s;\n" +
			"	}\n" +
			"	@Override\n" +
			"	public String toString() {\n" +
			"		return this.message;\n" +
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
			"Y.java",
			"public class Y {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(X.B);\n" +
			"	}\n" +
			"}"
		},
		null,
		options,
		"",
		"SUCCESS",
		"",
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285701
public void test177() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"	A(\"\", 0, \"A\"), B(\"SUCCESS\", 0, \"B\"), C(\"Hello\", 0, \"C\");\n" +
			"	\n" +
			"	private String message;\n" +
			"	private int index;\n" +
			"	private String name;\n" +
			"	\n" +
			"	X(@Deprecated String s, int i, @Deprecated String name) {\n" +
			"		this.message = s;\n" +
			"		this.index = i;\n" +
			"		this.name = name;\n" +
			"	}\n" +
			"	@Override\n" +
			"	public String toString() {\n" +
			"		return this.message + this.name;\n" +
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
			"Y.java",
			"public class Y {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(X.B);\n" +
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
public void test178() {
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
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public enum Y {\n" +
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
			"test180/package-info.java",
			"@p.Annot(state=p.MyEnum.OK)\n" +
			"package test180;",
			"test180/Test.java",
			"package test180;\n" +
			"import p.MyEnum;\n" +
			"import p.Annot;\n" +
			"@Annot(state=MyEnum.OK)\n" +
			"public class Test {}",
		},
		""
	);
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	JavacTestOptions.Excuse excuse = JavacTestOptions.Excuse.JavacHasErrorsEclipseHasNone;
	if(this.complianceLevel >= ClassFileConstants.JDK16) {
		excuse = null;
	}
	this.runConformTest(
		false,
		new String[] {
			"X.java",
			"import test180.Test;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(Test.class);\n" +
			"	}\n" +
			"}"
		},
		null,
		options,
		"",
		"class test180.Test",
		"",
		excuse);
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
			"test180/package-info.java",
			"@p.Annot(state=p.MyEnum.OK)\n" +
			"package test180;",
			"test180/Test.java",
			"package test180;\n" +
			"import p.MyEnum;\n" +
			"import p.Annot;\n" +
			"@Annot(state=MyEnum.OK)\n" +
			"public class Test {}",
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
	JavacTestOptions.Excuse excuse = JavacTestOptions.Excuse.JavacHasErrorsEclipseHasNone;
	if(this.complianceLevel >= ClassFileConstants.JDK16) {
		excuse = null;
	}
	this.runConformTest(
		false,
		new String[] {
			"X.java",
			"import test180.Test;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(Test.class);\n" +
			"	}\n" +
			"}"
		},
		null,
		options,
		"",
		"class test180.Test",
		"",
		excuse);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=300133
public void test181() {
	this.runConformTest(
		new String[] {
				"X.java",
				"public enum X {\n" +
				"	A {\n" +
				"		@Override\n" +
				"		public Object foo(final String s) {\n" +
				"			class Local {\n" +
				"				public String toString() {\n" +
				"					return s;\n" +
				"				}\n" +
				"			}\n" +
				"			return new Local();\n" +
				"		}\n" +
				"	};\n" +
				"	public abstract Object foo(String s);\n" +
				"	public static void main(String... args) {\n" +
				"		 System.out.println(A.foo(\"SUCCESS\"));\n" +
				"	}\n" +
				"}"
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test182() throws Exception {
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
			"		int n = 0;\n" +
			"		for (E e : E.values()) {\n" +
			"			if (e.val() == E.VALUES[n++] ) {\n" +
			"				System.out.print(e.val());\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}",
			"E.java",
			"enum E {\n" +
			"	a1(1), a2(2);\n" +
			"	static int[] VALUES = { 1, 2 };\n" +
			"	private int value;\n" +
			"	E(int v) {\n" +
			"		this.value = v;\n" +
			"	}\n" +
			"	public int val() {\n" +
			"		return this.value;\n" +
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
			"		int n = 0;\n" +
			"		for (E e : E.values()) {\n" +
			"			if (e.val() == E.VALUES[n++] ) {\n" +
			"				System.out.print(e.val());\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}",
			"E.java",
			"enum E {\n" +
			"	a1(1), a2(2);\n" +
			"	static int[] VALUES = { 1, 2 };\n" +
			"	private int value;\n" +
			"	E(int v) {\n" +
			"		this.value = v;\n" +
			"	}\n" +
			"	public int val() {\n" +
			"		return this.value;\n" +
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
			"		int n = 0;\n" +
			"		for (E e : E.values()) {\n" +
			"			if (e.val() == E.VALUES[n++] ) {\n" +
			"				System.out.print(e.val());\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}",
			"E.java",
			"enum E {\n" +
			"	a1(1), a2(2);\n" +
			"	static int[] VALUES = { 1, 2 };\n" +
			"	private int value;\n" +
			"	E(int v) {\n" +
			"		this.value = v;\n" +
			"	}\n" +
			"	public int val() {\n" +
			"		return this.value;\n" +
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
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"  A, B;\n" +
			"  private X() throws Exception {\n" +
			"  }\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	A, B;\n" +
		"	^\n" +
		"Unhandled exception type Exception\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	A, B;\n" +
		"	   ^\n" +
		"Unhandled exception type Exception\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test186() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_MISSING_ENUM_CASE_DESPITE_DEFAULT, JavaCore.ENABLED);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"enum X {\n" +
			"  A, B;\n" +
			"}\n" +
			"public class Y {\n" +
			"    void test(X x) {\n" +
			"        switch (x) {\n" +
			"			case A: System.out.println(\"A\"); break;\n" +
			" 			default : System.out.println(\"unknown\"); break;\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in Y.java (at line 6)\n" +
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
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_MISSING_ENUM_CASE_DESPITE_DEFAULT, JavaCore.ENABLED);
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.ERROR);
	this.runConformTest(
		new String[] {
			"Y.java",
			"enum X {\n" +
			"  A, B;\n" +
			"}\n" +
			"public class Y {\n" +
			"    void test(X x) {\n" +
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
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_MISSING_ENUM_CASE_DESPITE_DEFAULT, JavaCore.ENABLED);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"enum X {\n" +
			"  A, B;\n" +
			"}\n" +
			"public class Y {\n" +
			"    void test(X x) {\n" +
			"        switch (x) {\n" +
			"			case A: System.out.println(\"A\"); break;\n" +
			"           //$CASES-OMITTED$\n" + // not strong enough to suppress the warning if default: is missing
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in Y.java (at line 6)\n" +
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
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.ERROR);
	options.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
	this.runConformTest(
		new String[] {
			"Y.java",
			"enum X {\n" +
			"  A, B;\n" +
			"}\n" +
			"public class Y {\n" +
			"    @SuppressWarnings(\"incomplete-switch\")\n" +
			"    void test(X x) {\n" +
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
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"enum X {\n" +
			"  A, B;\n" +
			"}\n" +
			"public class Y {\n" +
			"    void test(X x) {\n" +
			"        switch (x) {\n" +
			"			case A: System.out.println(\"A\"); break;\n" +
			"			case B: System.out.println(\"B\"); break;\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in Y.java (at line 6)\n" +
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
	Map options = getCompilerOptions();
	//options.put(JavaCore.COMPILER_PB_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"enum X {\n" +
			"  A, B;\n" +
			"}\n" +
			"public class Y {\n" +
			"    int test(X x) {\n" +
			"        switch (x) {\n" +
			"			case A: return 1;\n" +
			"			case B: return 2;\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 5)\n" +
		"	int test(X x) {\n" +
		"	    ^^^^^^^^^\n" +
		"This method must return a result of type int. Note that a problem regarding missing 'default:' on 'switch' has been suppressed, which is perhaps related to this problem\n" +
		"----------\n",
		null, // classlibs
		true, // flush
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433060 [1.8][compiler] enum E<T>{I;} causes NPE in AllocationExpression.checkTypeArgumentRedundancy
public void test433060() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_REDUNDANT_TYPE_ARGUMENTS, JavaCore.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X<T> {\n" +
			"	OBJ;\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public enum X<T> {\n" +
		"	              ^\n" +
		"Syntax error, enum declaration cannot have type parameters\n" +
		"----------\n",
		null,
		true,
		options);
}
public void test434442() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	this.runConformTest(new String[] {
			"X.java",
			"interface I {\n" +
			"	public enum Letter {\n" +
			"  		A, B;\n" +
			"	}\n" +
			"  public default void test(Letter letter) {\n" +
			"    switch (letter) {\n" +
			"      case A:\n" +
			"        System.out.println(\"A\");\n" +
			"        break;\n" +
			"      case B:\n" +
			"        System.out.println(\"B\");\n" +
			"        break;\n" +
			"    }\n" +
			"  }\n" +
			"}\n" +
			"\n" +
			"public class X implements I {\n" +
			"  public static void main(String[] args) {\n" +
			"	  try{\n" +
			"		  X x = new X();\n" +
			"		  x.test(Letter.A);\n" +
			"	  }\n" +
			"    catch (Exception e) {\n" +
			"      e.printStackTrace();\n" +
			"    }\n" +
			"  }\n" +
			"} \n" +
			"\n"
	});
}
public void test476281() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	this.runConformTest(new String[] {
			"LambdaEnumLocalClassBug.java",
			"public enum LambdaEnumLocalClassBug {\n" +
			"  A(() -> {\n" +
			"    class Foo {\n" +
			"    }\n" +
			"    new Foo();\n" +
			"    System.out.println(\"Success\");\n" +
			"  })\n" +
			";\n" +
			"  private final Runnable runnable;\n" +
			"  private LambdaEnumLocalClassBug(Runnable runnable) {\n" +
			"    this.runnable = runnable;\n" +
			"  }\n" +
			"  public static void main(String[] args) {\n" +
			"    A.runnable.run();\n" +
			"  }\n" +
			"}"},
			"Success");
}
public void test476281a() {
	this.runConformTest(new String[] {
			"Test.java",
			"public enum Test {\n" +
			"  B(new Runnable() {\n" +
			"	public void run() {\n" +
			"		//\n" +
			"		class Foo {\n" +
			"			\n" +
			"		}\n" +
			"		new Foo();\n" +
			"    System.out.println(\"Success\");\n" +
			"	}\n" +
			"});\n" +
			"  private final Runnable runnable;\n" +
			"  private Test(Runnable runnable) {\n" +
			"    this.runnable = runnable;\n" +
			"  }\n" +
			"  public static void main(String[] args) {\n" +
			"    B.runnable.run();\n" +
			"  }\n" +
			"}"},
			"Success");
}
public void testBug388314() throws Exception {
	this.runConformTest(
			new String[] {
					"p/Nullable.java",
					"package p;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"import java.lang.annotation.*;\n" +
					"@Documented\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@Target(value = { FIELD, LOCAL_VARIABLE, METHOD, PARAMETER })\n" +
					"public @interface Nullable {\n" +
					"	// Nothing to do.\n" +
					"}",
					"p/EnumWithNullable.java",
					"package p;\n" +
					"public enum EnumWithNullable {\n" +
					"	A;\n" +
					"\n" +
					"	@Nullable\n" +
					"	private final Object b;\n" +
					"\n" +
					"	private EnumWithNullable(@Nullable Object b) {\n" +
					"		this.b = b;\n" +
					"	}\n" +
					"\n" +
					"	private EnumWithNullable() {\n" +
					"		this(null);\n" +
					"	}\n" +
					"}\n"
			},
			"");

	String expectedOutput =
		"  // Method descriptor #27 (Ljava/lang/String;ILjava/lang/Object;)V\n" +
		"  // Stack: 3, Locals: 4\n" +
		"  private EnumWithNullable(java.lang.String arg0,  int arg1, @p.Nullable java.lang.Object b);\n";

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"p" + File.separator + "EnumWithNullable.class"));
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
}
