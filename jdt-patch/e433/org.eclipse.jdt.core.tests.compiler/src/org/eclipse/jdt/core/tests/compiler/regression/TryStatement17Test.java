/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others.
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
 *     Jesper Steen Moller - bug 404146 nested try-catch-finally-blocks leads to unrunnable Java byte code
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;
@SuppressWarnings({ "rawtypes" })
public class TryStatement17Test extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test061" };
//	TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public TryStatement17Test(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_7);
}
public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			System.out.println();\n" +
			"			Reader r = new FileReader(args[0]);\n" +
			"			r.read();\n" +
			"		} catch(IOException | FileNotFoundException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	} catch(IOException | FileNotFoundException e) {\n" +
		"	                      ^^^^^^^^^^^^^^^^^^^^^\n" +
		"The exception FileNotFoundException is already caught by the alternative IOException\n" +
		"----------\n");
}
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			System.out.println();\n" +
			"			Reader r = new FileReader(args[0]);\n" +
			"			r.read();\n" +
			"		} catch(FileNotFoundException | FileNotFoundException | IOException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	} catch(FileNotFoundException | FileNotFoundException | IOException e) {\n" +
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" +
		"The exception FileNotFoundException is already caught by the alternative FileNotFoundException\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	} catch(FileNotFoundException | FileNotFoundException | IOException e) {\n" +
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" +
		"The exception FileNotFoundException is already caught by the alternative IOException\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	} catch(FileNotFoundException | FileNotFoundException | IOException e) {\n" +
		"	                                ^^^^^^^^^^^^^^^^^^^^^\n" +
		"The exception FileNotFoundException is already caught by the alternative IOException\n" +
		"----------\n");
}
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			System.out.println();\n" +
			"			Reader r = new FileReader(args[0]);\n" +
			"			r.read();\n" +
			"		} catch(FileNotFoundException e) {" +
			"			e.printStackTrace();\n" +
			"		} catch(FileNotFoundException | IOException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	} catch(FileNotFoundException | IOException e) {\n" +
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" +
		"The exception FileNotFoundException is already caught by the alternative IOException\n" +
		"----------\n");
}
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			System.out.println();\n" +
			"			Reader r = new FileReader(args[0]);\n" +
			"			r.read();\n" +
			"		} catch(RuntimeException | Exception e) {" +
			"			e.printStackTrace();\n" +
			"		} catch(FileNotFoundException | IOException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	} catch(RuntimeException | Exception e) {			e.printStackTrace();\n" +
		"	        ^^^^^^^^^^^^^^^^\n" +
		"The exception RuntimeException is already caught by the alternative Exception\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	} catch(FileNotFoundException | IOException e) {\n" +
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" +
		"The exception FileNotFoundException is already caught by the alternative IOException\n" +
		"----------\n");
}
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			System.out.println();\n" +
			"			Reader r = new FileReader(\"Zork\");\n" +
			"			r.read();\n" +
			"		} catch(NumberFormatException | RuntimeException e) {\n" +
			"			e.printStackTrace();\n" +
			"		} catch(FileNotFoundException | IOException e) {\n" +
			"			// ignore\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	} catch(NumberFormatException | RuntimeException e) {\n" +
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" +
		"The exception NumberFormatException is already caught by the alternative RuntimeException\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 11)\n" +
		"	} catch(FileNotFoundException | IOException e) {\n" +
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" +
		"The exception FileNotFoundException is already caught by the alternative IOException\n" +
		"----------\n");
}
//Test that lub is not used for checking for checking the exceptions
public void test006() {
	this.runNegativeTest(
		new String[] {
			"X.java",

			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			throw new Foo();\n"+
			"		} catch(SonOfFoo | DaughterOfFoo e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Foo extends Exception {}\n"+
			"class SonOfFoo extends Foo {}\n"+
			"class DaughterOfFoo extends Foo {}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	throw new Foo();\n" +
		"	^^^^^^^^^^^^^^^^\n" +
		"Unhandled exception type Foo\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 10)\n" +
		"	class Foo extends Exception {}\n" +
		"	      ^^^\n" +
		"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 11)\n" +
		"	class SonOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^\n" +
		"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 12)\n" +
		"	class DaughterOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
public void test007() {
	this.runConformTest(
		new String[] {
			"X.java",

			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			throw new Foo();\n"+
			"		} catch(SonOfFoo | DaughterOfFoo e) {\n" +
			"			System.out.println(\"Caught lub\");\n" +
			"		} catch(Foo e) {\n" +
			"           System.out.println(\"Caught Foo\");\n" +
			"        }\n" +
			"	}\n" +
			"}\n" +
			"class Foo extends Exception {}\n"+
			"class SonOfFoo extends Foo {}\n"+
			"class DaughterOfFoo extends Foo {}\n"
		},
		"Caught Foo");
}
// test that lub is not used for precise rethrow
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			if (args.length == 0) throw new SonOfFoo();\n"+
			"			throw new DaughterOfFoo();\n" +
			"		} catch(SonOfFoo | DaughterOfFoo e) {\n" +
			"			try {\n" +
			"				throw e;\n" +
			"			} catch(SonOfFoo | DaughterOfFoo e1) {}\n"+
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Foo extends Exception {}\n"+
			"class SonOfFoo extends Foo {}\n"+
			"class DaughterOfFoo extends Foo {}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 13)\n" +
		"	class Foo extends Exception {}\n" +
		"	      ^^^\n" +
		"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 14)\n" +
		"	class SonOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^\n" +
		"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 15)\n" +
		"	class DaughterOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			throw new IOException();\n" +
			"		} catch(IOException | RuntimeException e) {\n" +
			"			e = new IOException();\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	e = new IOException();\n" +
		"	^\n" +
		"The parameter e of a multi-catch block cannot be assigned\n" +
		"----------\n");
}
//Test that union type checks are done for a precise throw too
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",

			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			throw new DaughterOfFoo();\n"+
			"		} catch(SonOfFoo | DaughterOfFoo e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Foo extends Exception {}\n"+
			"class SonOfFoo extends Foo {}\n"+
			"class DaughterOfFoo extends Foo {}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	} catch(SonOfFoo | DaughterOfFoo e) {\n" +
		"	        ^^^^^^^^\n" +
		"Unreachable catch block for SonOfFoo. This exception is never thrown from the try statement body\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 10)\n" +
		"	class Foo extends Exception {}\n" +
		"	      ^^^\n" +
		"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 11)\n" +
		"	class SonOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^\n" +
		"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 12)\n" +
		"	class DaughterOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
// Test that a rethrow is precisely computed
public void test011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			throw new DaughterOfFoo();\n"+
			"		} catch(Foo e) {\n" +
			"			try {\n" +
			"				throw e;\n" +
			"			} catch (SonOfFoo e1) {\n" +
			"			 	e1.printStackTrace();\n" +
			"			} catch (Foo e1) {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"+
			"class Foo extends Exception {}\n"+
			"class SonOfFoo extends Foo {}\n"+
			"class DaughterOfFoo extends Foo {}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	} catch (SonOfFoo e1) {\n" +
		"	         ^^^^^^^^\n" +
		"Unreachable catch block for SonOfFoo. This exception is never thrown from the try statement body\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 14)\n" +
		"	class Foo extends Exception {}\n" +
		"	      ^^^\n" +
		"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 15)\n" +
		"	class SonOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^\n" +
		"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 16)\n" +
		"	class DaughterOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
//Test that a rethrow is precisely computed
public void test012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			throw new DaughterOfFoo();\n"+
			"		} catch(Foo e) {\n" +
			"			try {\n" +
			"				throw e;\n" +
			"			} catch (SonOfFoo e1) {\n" +
			"			 	e1.printStackTrace();\n" +
			"			} catch (Foo e1) {}\n" +
			"			finally {" +
			"				System.out.println(\"\");}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"+
			"class Foo extends Exception {}\n"+
			"class SonOfFoo extends Foo {}\n"+
			"class DaughterOfFoo extends Foo {}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	} catch (SonOfFoo e1) {\n" +
		"	         ^^^^^^^^\n" +
		"Unreachable catch block for SonOfFoo. This exception is never thrown from the try statement body\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 15)\n" +
		"	class Foo extends Exception {}\n" +
		"	      ^^^\n" +
		"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 16)\n" +
		"	class SonOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^\n" +
		"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 17)\n" +
		"	class DaughterOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
// Test that if the rethrow argument is modified (not effectively final), then it is not precisely
// computed
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			throw new DaughterOfFoo();\n"+
			"		} catch(Foo e) {\n" +
			"			try {\n" +
			"				e = new Foo();\n" +
			"				throw e;\n" +
			"			} catch (SonOfFoo e1) {\n" +
			"			 	e1.printStackTrace();\n" +
			"			} catch (Foo e1) {}\n"+
			"		}\n" +
			"	}\n" +
			"}\n"+
			"class Foo extends Exception {}\n"+
			"class SonOfFoo extends Foo {}\n"+
			"class DaughterOfFoo extends Foo {}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 15)\n" +
		"	class Foo extends Exception {}\n" +
		"	      ^^^\n" +
		"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 16)\n" +
		"	class SonOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^\n" +
		"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 17)\n" +
		"	class DaughterOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}

// Test that if the rethrow argument is modified in a different flow (not effectively final), then also precise throw
// should not be computed
public void test014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			throw new DaughterOfFoo();\n"+
			"		} catch(Foo e) {\n" +
			"			try {\n" +
			"				boolean DEBUG = true;\n" +
			"				if (DEBUG) {\n" +
			"					throw e;\n"+
			"				}" +
			"				e = new Foo();\n" +
			"				e.printStackTrace();\n"+
			"			} catch (SonOfFoo e1) {\n" +
			"			 	e1.printStackTrace();\n" +
			"			} catch (Foo e1) {}\n"+
			"		}\n" +
			"	}\n" +
			"}\n"+
			"class Foo extends Exception {}\n"+
			"class SonOfFoo extends Foo {}\n"+
			"class DaughterOfFoo extends Foo {}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 18)\n" +
		"	class Foo extends Exception {}\n" +
		"	      ^^^\n" +
		"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 19)\n" +
		"	class SonOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^\n" +
		"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 20)\n" +
		"	class DaughterOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}

// test015 moved into org.eclipse.jdt.core.tests.compiler.regression.TryStatementTest.test070()

// Test precise rethrow works good even in nested try catch block
public void test016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			throw new DaughterOfFoo();\n"+
			"		} catch(Foo e) {\n" +
			"			try {\n" +
			"				throw new Foo();\n" +
			"			} catch (Foo e1) {\n" +
			"				try {\n" +
			"					throw e;\n" +
			"				} catch (SonOfFoo e2) {\n" +
			"			 		e1.printStackTrace();\n" +
			"				} catch (Foo e3) {}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"+
			"class Foo extends Exception {}\n"+
			"class SonOfFoo extends Foo {}\n"+
			"class DaughterOfFoo extends Foo {}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	} catch (SonOfFoo e2) {\n" +
		"	         ^^^^^^^^\n" +
		"Unreachable catch block for SonOfFoo. This exception is never thrown from the try statement body\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 18)\n" +
		"	class Foo extends Exception {}\n" +
		"	      ^^^\n" +
		"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 19)\n" +
		"	class SonOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^\n" +
		"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 20)\n" +
		"	class DaughterOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
// Test lub computation.
public void test017() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String [] args) {\n" +
			"        doSomething(false);\n" +
			"    }\n" +
			"    public static void doSomething (boolean bool) {\n" +
			"        try {\n" +
			"            if (bool)\n" +
			"                throw new GrandSonOfFoo();\n" +
			"            else \n" +
			"                throw new GrandDaughterOfFoo();\n" +
			"        } catch(SonOfFoo | DaughterOfFoo e) {\n" +
			"        	SonOfFoo s = e;\n" +
			"        	e.callableOnBothGenders();\n" +
			"        	e.callableOnlyOnMales();\n" +
			"        	e.callableOnlyOnFemales();\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"class Foo extends Exception {\n" +
			"	void callableOnBothGenders () {\n" +
			"	}\n" +
			"}\n" +
			"class SonOfFoo extends Foo {\n" +
			"	void callableOnlyOnMales() {\n" +
			"	}\n" +
			"}\n" +
			"class GrandSonOfFoo extends SonOfFoo {}\n" +
			"class DaughterOfFoo extends Foo {\n" +
			"	void callableOnlyOnFemales() {\n" +
			"	}\n" +
			"}\n" +
			"class GrandDaughterOfFoo extends DaughterOfFoo {}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	SonOfFoo s = e;\n" +
		"	             ^\n" +
		"Type mismatch: cannot convert from Foo to SonOfFoo\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	e.callableOnlyOnMales();\n" +
		"	  ^^^^^^^^^^^^^^^^^^^\n" +
		"The method callableOnlyOnMales() is undefined for the type Foo\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 15)\n" +
		"	e.callableOnlyOnFemales();\n" +
		"	  ^^^^^^^^^^^^^^^^^^^^^\n" +
		"The method callableOnlyOnFemales() is undefined for the type Foo\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 19)\n" +
		"	class Foo extends Exception {\n" +
		"	      ^^^\n" +
		"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 23)\n" +
		"	class SonOfFoo extends Foo {\n" +
		"	      ^^^^^^^^\n" +
		"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 27)\n" +
		"	class GrandSonOfFoo extends SonOfFoo {}\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"The serializable class GrandSonOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"7. WARNING in X.java (at line 28)\n" +
		"	class DaughterOfFoo extends Foo {\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"8. WARNING in X.java (at line 32)\n" +
		"	class GrandDaughterOfFoo extends DaughterOfFoo {}\n" +
		"	      ^^^^^^^^^^^^^^^^^^\n" +
		"The serializable class GrandDaughterOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
// Test explicit final modifiers
public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void foo(boolean bool) throws Foo {\n" +
			"		try {\n" +
			"			if (bool) \n" +
			"			    throw new DaughterOfFoo();\n" +
			"			else\n" +
			"			    throw new SonOfFoo();\n" +
			"		} catch (final SonOfFoo | DaughterOfFoo e){\n" +
			"			throw e;\n" +
			"		}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			foo(true);\n" +
			"		} catch(Foo e) {} \n" +
			"	}\n" +
			"}\n" +
			"class Foo extends Exception {}\n" +
			"class SonOfFoo extends Foo {}\n" +
			"class DaughterOfFoo extends Foo {}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 7)\n" +
		"	throw new SonOfFoo();\n" +
		"	^^^^^^^^^^^^^^^^^^^^^\n" +
		"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 18)\n" +
		"	class Foo extends Exception {}\n" +
		"	      ^^^\n" +
		"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 19)\n" +
		"	class SonOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^\n" +
		"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 20)\n" +
		"	class DaughterOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
// Test explicit final modifiers
public void test019() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void foo(boolean bool) throws Foo {\n" +
			"		try {\n" +
			"			if (bool) \n" +
			"			    throw new DaughterOfFoo();\n" +
			"			else\n" +
			"			    throw new SonOfFoo();\n" +
			"		} catch (final SonOfFoo | final DaughterOfFoo e){\n" +
			"			throw e;\n" +
			"		}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			foo(true);\n" +
			"		} catch(Foo e) {} \n" +
			"	}\n" +
			"}\n" +
			"class Foo extends Exception {}\n" +
			"class SonOfFoo extends Foo {}\n" +
			"class DaughterOfFoo extends Foo {}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	} catch (final SonOfFoo | final DaughterOfFoo e){\n" +
		"	                          ^^^^^\n" +
		"Syntax error on token \"final\", delete this token\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 18)\n" +
		"	class Foo extends Exception {}\n" +
		"	      ^^^\n" +
		"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 19)\n" +
		"	class SonOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^\n" +
		"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 20)\n" +
		"	class DaughterOfFoo extends Foo {}\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
// Test that for unchecked exceptions, we don't do any precise analysis.
public void test020() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
					"	public static void main(String [] args)  {\n" +
					"		try {\n" +
					"		} catch (NullPointerException s) {\n" +
					"			try {\n" +
					"				throw s;\n" +
					"			} catch (ArithmeticException e) {\n" +
					"			}\n" +
					"		} finally {\n" +
					"			System.out.println(\"All done\");\n" +
					"		}\n" +
					"	}\n" +
					"}\n"
		},
		"All done");
}
// Test multicatch behavior.
public void test021() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String[] exceptions = { \"NullPointerException\", \"ArithmeticException\",\n" +
			"				\"ArrayStoreException\", \"ArrayIndexOutOfBoundsException\" };\n" +
			"\n" +
			"		for (String exception : exceptions) {\n" +
			"			try {\n" +
			"				switch (exception) {\n" +
			"				case \"NullPointerException\":\n" +
			"					throw new NullPointerException();\n" +
			"				case \"ArithmeticException\":\n" +
			"					throw new ArithmeticException();\n" +
			"				case \"ArrayStoreException\":\n" +
			"					throw new ArrayStoreException();\n" +
			"				case \"ArrayIndexOutOfBoundsException\":\n" +
			"					throw new ArrayIndexOutOfBoundsException();\n" +
			"				}\n" +
			"			} catch (NullPointerException | ArithmeticException | ArrayStoreException | ArrayIndexOutOfBoundsException e) {\n" +
			"				System.out.println(e);\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"java.lang.NullPointerException\n" +
		"java.lang.ArithmeticException\n" +
		"java.lang.ArrayStoreException\n" +
		"java.lang.ArrayIndexOutOfBoundsException");
}
public void test022() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends Exception> {\n" +
				"public void foo(boolean bool) throws Exception {\n" +
				"	try {\n" +
	            "	if (bool)\n" +
	            "		throw new Exception();\n" +
	            "	else\n" +
	            "		throw new NullPointerException();\n" +
	        	"	} catch (T | NullPointerException e) {}\n" +
	        	"}\n" +
	        	"}\n"},
	        	"----------\n" +
    			"1. ERROR in X.java (at line 8)\n" +
    			"	} catch (T | NullPointerException e) {}\n" +
    			"	         ^\n" +
    			"Cannot use the type parameter T in a catch block\n" +
    			"----------\n"
			);
}
public void test023() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> extends Exception {\n" +
				"public void foo(boolean bool) throws Exception {\n" +
				"	try {\n" +
	            "	if (bool)\n" +
	            "		throw new Exception();\n" +
	            "	else\n" +
	            "		throw new NullPointerException();\n" +
	        	"	} catch (X<String> | NullPointerException e) {}\n" +
	        	"}\n" +
	        	"}\n"},
	        	"----------\n" +
    			"1. WARNING in X.java (at line 1)\n" +
    			"	public class X<T> extends Exception {\n" +
    			"	             ^\n" +
    			"The serializable class X does not declare a static final serialVersionUID field of type long\n" +
    			"----------\n" +
    			"2. ERROR in X.java (at line 1)\n" +
    			"	public class X<T> extends Exception {\n" +
    			"	                          ^^^^^^^^^\n" +
    			"The generic class X<T> may not subclass java.lang.Throwable\n" +
    			"----------\n" +
    			"3. ERROR in X.java (at line 8)\n" +
    			"	} catch (X<String> | NullPointerException e) {}\n" +
    			"	         ^\n" +
    			"Cannot use the parameterized type X<String> either in catch block or throws clause\n" +
    			"----------\n"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340486
public void test024() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.FileNotFoundException;\n" +
				"import java.io.IOException;\n" +
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        try {\n" +
				"            if (args.length == 0)\n" +
				"                throw new FileNotFoundException();\n" +
				"            throw new IOException();\n" +
				"        } catch(IOException | FileNotFoundException e) {\n" +
				"        }\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	} catch(IOException | FileNotFoundException e) {\n" +
			"	                      ^^^^^^^^^^^^^^^^^^^^^\n" +
			"The exception FileNotFoundException is already caught by the alternative IOException\n" +
			"----------\n");
}
public void test024a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.FileNotFoundException;\n" +
				"import java.io.IOException;\n" +
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        try {\n" +
				"            if (args.length == 0)\n" +
				"                throw new FileNotFoundException();\n" +
				"            throw new IOException();\n" +
				"        } catch(FileNotFoundException | IOException e) {\n" +
				"        }\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	} catch(FileNotFoundException | IOException e) {\n" +
			"	        ^^^^^^^^^^^^^^^^^^^^^\n" +
			"The exception FileNotFoundException is already caught by the alternative IOException\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=344824
public void test025() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        try {\n" +
				"            throw new D();\n" +
				"        } catch (F e) {\n" +
				"            try {\n" +
				"                throw e;\n" +
				"            } catch (F f) {\n" +
				"            } catch (RuntimeException | S f) {\n" +
				"            }\n" +
				"        }\n" +
				"    }\n" +
				"}\n" +
				"class F extends Exception {}\n" +
				"class S extends F {}\n" +
				"class D extends F {}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	} catch (RuntimeException | S f) {\n" +
			"	                            ^\n" +
			"Unreachable catch block for S. It is already handled by the catch block for F\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 14)\n" +
			"	class F extends Exception {}\n" +
			"	      ^\n" +
			"The serializable class F does not declare a static final serialVersionUID field of type long\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 15)\n" +
			"	class S extends F {}\n" +
			"	      ^\n" +
			"The serializable class S does not declare a static final serialVersionUID field of type long\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 16)\n" +
			"	class D extends F {}\n" +
			"	      ^\n" +
			"The serializable class D does not declare a static final serialVersionUID field of type long\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345522
public void test026() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.EOFException;\n" +
				"import java.io.FileNotFoundException;\n" +
				"public class X {\n" +
				"    X() { \n" +
				"        try {\n" +
				"            zoo();\n" +
				"        } catch (EOFException ea) {\n" +
				"        } catch (FileNotFoundException eb) {\n" +
				"        } catch (Exception ec) {\n" +
				"            throw ec;\n" +
				"        }\n" +
				"    }\n" +
				"    void zoo() throws FileNotFoundException, EOFException {\n" +
				"    }\n" +
				"}\n"
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345522
public void test026a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.EOFException;\n" +
				"import java.io.FileNotFoundException;\n" +
				"public class X {\n" +
				"    X() { \n" +
				"        try {\n" +
				"            zoo();\n" +
				"            throw new Exception();\n" +
				"        } catch (EOFException ea) {\n" +
				"        } catch (FileNotFoundException eb) {\n" +
				"        } catch (Exception ec) {\n" +
				"            throw ec;\n" +
				"        }\n" +
				"    }\n" +
				"    void zoo() throws FileNotFoundException, EOFException {\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	throw ec;\n" +
			"	^^^^^^^^^\n" +
			"Unhandled exception type Exception\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=345579
public void test027() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"    X() throws Exception {\n"+
				"        try {\n"+
				"            throw (Throwable) new Exception();\n"+
				"        } catch (Exception e) {\n"+
				"            throw e;\n"+
				"        } catch (Throwable e) {\n"+
				"        }\n"+
				"    }\n"+
				"}\n"
			},
			"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=350361
public void test028() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"    public void foo () {\n"+
				"        try {\n"+
				"            throw new Exception(); \n"+
				"        } catch (Exception e) {\n"+
				"            if (e instanceof RuntimeException) \n" +
				"            	throw (RuntimeException) e; \n"+
				"        } \n"+
				"    }\n"+
				"}\n"
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=350579
public void test029() { // with finally
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements AutoCloseable {\n" +
				"    public static void main(String[] args) {\n" +
				"        try (X x = new X();) {\n" +
				"        } catch (Exception x) {\n" +
				"        } catch (Throwable y) {\n" +
				"        } \n" +
				"        finally {\n" +
				"            System.out.println(\"Done\");\n" +
				"        }\n" +
				"    }\n" +
				"    public void close() {\n" +
				"    }\n" +
				"}\n"
			},
			"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=350579
public void test030() { // no finally
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements AutoCloseable {\n" +
				"    public static void main(String[] args) {\n" +
				"        try (X x = new X();) {\n" +
				"        } catch (Exception x) {\n" +
				"        } catch (Throwable y) {\n" +
				"        } \n" +
				"        System.out.println(\"Done\");\n" +
				"    }\n" +
				"    public void close() {\n" +
				"    }\n" +
				"}\n"
			},
			"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=350579
public void test031() { // with finally
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements AutoCloseable {\n" +
				"    public static void main(String [] args) throws XXException, YYException, ZZException {\n" +
				"        try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
				"        } catch (XException x) {\n" +
				"        } catch (YException y) {\n" +
				"        } catch (ZException z) {\n" +
				"        } finally {\n" +
				"            System.out.println(\"Done\");\n" +
				"        }\n" +
				"    }\n" +
				"    public X() throws XException {\n" +
				"        throw new XException();\n" +
				"    }\n" +
				"    public void close() throws XXException {\n" +
				"        throw new XXException();\n" +
				"    }\n" +
				"}\n" +
				"class Y implements AutoCloseable {\n" +
				"    public Y() throws YException {\n" +
				"        throw new YException();\n" +
				"    }\n" +
				"    public void close() throws YYException {\n" +
				"        throw new YYException();\n" +
				"    }\n" +
				"}\n" +
				"class Z implements AutoCloseable {\n" +
				"    public Z() throws ZException {\n" +
				"        throw new ZException();\n" +
				"    }\n" +
				"    public void close() throws ZZException {\n" +
				"        throw new ZZException();\n" +
				"    }\n" +
				"}\n" +
				"class XException extends Exception {}\n" +
				"class XXException extends Exception {}\n" +
				"class YException extends Exception {}\n" +
				"class YYException extends Exception {}\n" +
				"class ZException extends Exception {}\n" +
				"class ZZException extends Exception {}\n"
			},
			"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=350579
public void test032() { // no finally
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements AutoCloseable {\n" +
				"    public static void main(String [] args) throws XXException, YYException, ZZException {\n" +
				"        try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
				"        } catch (XException x) {\n" +
				"        } catch (YException y) {\n" +
				"        } catch (ZException z) {\n" +
				"        }\n" +
				"        System.out.println(\"Done\");\n" +
				"    }\n" +
				"    public X() throws XException {\n" +
				"        throw new XException();\n" +
				"    }\n" +
				"    public void close() throws XXException {\n" +
				"        throw new XXException();\n" +
				"    }\n" +
				"}\n" +
				"class Y implements AutoCloseable {\n" +
				"    public Y() throws YException {\n" +
				"        throw new YException();\n" +
				"    }\n" +
				"    public void close() throws YYException {\n" +
				"        throw new YYException();\n" +
				"    }\n" +
				"}\n" +
				"class Z implements AutoCloseable {\n" +
				"    public Z() throws ZException {\n" +
				"        throw new ZException();\n" +
				"    }\n" +
				"    public void close() throws ZZException {\n" +
				"        throw new ZZException();\n" +
				"    }\n" +
				"}\n" +
				"class XException extends Exception {}\n" +
				"class XXException extends Exception {}\n" +
				"class YException extends Exception {}\n" +
				"class YYException extends Exception {}\n" +
				"class ZException extends Exception {}\n" +
				"class ZZException extends Exception {}\n"
			},
			"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391092
public void testBug391092() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		try {\n" +
			"		} catch (NullPointerException  | ArrayIndexOutOfBoundsException  e []) {\n" +
			"		} catch (ClassCastException [] c) {\n" +
			"		} catch (ArrayStoreException a[]) {\n" +
			"		} catch (ArithmeticException | NegativeArraySizeException b[][] ) {\n" +
			"		} catch (ClassCastException[][] | ClassNotFoundException[] g) {\n" +
			"		}\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	} catch (NullPointerException  | ArrayIndexOutOfBoundsException  e []) {\n" +
		"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Illegal attempt to create arrays of union types\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	} catch (ClassCastException [] c) {\n" +
		"	         ^^^^^^^^^^^^^^^^^^^^^\n" +
		"No exception of type ClassCastException[] can be thrown; an exception type must be a subclass of Throwable\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	} catch (ArrayStoreException a[]) {\n" +
		"	         ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"No exception of type ArrayStoreException[] can be thrown; an exception type must be a subclass of Throwable\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 7)\n" +
		"	} catch (ArithmeticException | NegativeArraySizeException b[][] ) {\n" +
		"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Illegal attempt to create arrays of union types\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 8)\n" +
		"	} catch (ClassCastException[][] | ClassNotFoundException[] g) {\n" +
		"	         ^^^^^^^^^^^^^^^^^^^^^^\n" +
		"No exception of type ClassCastException[][] can be thrown; an exception type must be a subclass of Throwable\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 8)\n" +
		"	} catch (ClassCastException[][] | ClassNotFoundException[] g) {\n" +
		"	                                  ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"No exception of type ClassNotFoundException[] can be thrown; an exception type must be a subclass of Throwable\n" +
		"----------\n");
	}

//Bug 404146 - nested try-catch-finally-blocks leads to unrunnable Java byte code
public void testBug404146() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
					"import javax.naming.NamingException;\n" +

			"\n" +
			"public final class X {\n" +
			"\n" +
			"    public static final void illegalStackMap() {\n" +
			"        try {\n" +
			"          try {\n" +
			"            Y.decoy1();\n" +
			"          } finally {\n" +
			"            try {\n" +
			"                Y.decoy2();\n" +
			"            } catch (final IOException e) {\n" +
			"              return;\n" +
			"            }\n" +
			"          }\n" +
			"        } finally {\n" +
			"          try {\n" +
			"            Y.decoy3();\n" +
			"          } catch (final NamingException e) {\n" +
			"            return;\n" +
			"          }\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
			"Y.java",
				"import java.io.IOException;\n" +
						"import javax.naming.NamingException;\n" +
			"public final class Y {\n" +
			"\n" +
			"    public static void decoy1() {}\n" +
			"    public static void decoy2() throws IOException {}\n" +
			"    public static void decoy3() throws NamingException {}\n" +
			"}\n"
		});
}
public void testBug488569_001() {
	if (this.complianceLevel < ClassFileConstants.JDK9) {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n" +
					"    public static void main(String [] args) throws Exception {\n" +
					"    	Z z1 = new Z();\n" +
					"        try (Y y1 = new Y(); z1;) {\n" +
					"        }  \n" +
					"    }  \n" +
					"}\n" +
					"class Y implements AutoCloseable {\n" +
					"	public void close() throws Exception {\n" +
					"	}\n" +
					"}\n" +
					"\n" +
					"class Z implements AutoCloseable {\n" +
					"	public void close() throws Exception {\n" +
					"	}   \n" +
					"}\n" +
					"\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	try (Y y1 = new Y(); z1;) {\n" +
			"	                     ^^\n" +
			"Variable resource not allowed here for source level below 9\n" +
			"----------\n");
	} else {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) throws Exception {\n" +
				"    	Z z1 = new Z();\n" +
				"        try (Y y1 = new Y(); z1;) {\n" +
				"        }  \n" +
				"    }  \n" +
				"}\n" +
				"class Y implements AutoCloseable {\n" +
				"	public void close() throws Exception {\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class Z implements AutoCloseable {\n" +
				"	public void close() throws Exception {\n" +
				"	}   \n" +
				"}\n" +
				"\n"
			},
			"");

	}
}

public static Class testClass() {
	return TryStatement17Test.class;
}
}
