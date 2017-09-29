/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Here we focus on various aspects of the runtime behavior of the generated
 * code.
 */
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

public class RuntimeTests extends AbstractRegressionTest {

public RuntimeTests(String name) {
	super(name);
}

// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
// Only the highest compliance level is run; add the VM argument
// -Dcompliance=1.4 (for example) to lower it if needed
static {
//		TESTS_NAMES = new String[] { "test0001" };
//	 	TESTS_NUMBERS = new int[] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
}

public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {
	return RuntimeTests.class;
}

// decided not to keep this active because of negative effects on the test
// series (the OOME potentially causing grief to others)
// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=217078
// memory exhaustion - try to allocate too big an instance
public void _test0001_memory_exhaustion() {
	runTest(
		new String[] { /* testFiles */
			"X.java",
			"public class X {\n" +
			"  public static void main(String args[]) {\n" +
			"    try {" +
			"      Y y = new Y(Integer.MAX_VALUE);\n" +
			"    }" +
			"    catch (OutOfMemoryError e) {\n" +
			"      System.out.println(\"SUCCESS\");\n" +
			"      return;\n" +
			"    }\n" +
			"    System.out.println(\"FAILURE\");\n" +
			"  }\n" +
			"}\n" +
			"class Y {\n" +
			"  long storage[];\n" +
			"  Y(int itemsNb) {\n" +
			"    storage = new long[itemsNb];\n" +
			"  }\n" +
			"}\n"},
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"SUCCESS" /* expectedOutputString */,
		null /* expectedErrorString - skip this because some JREs emit additional info to stderr in case of exception */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// synchronization - concurrent access to a resource with explicit and
// implicit locks
public void test0500_synchronization() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"static public void main (String args[]) {\n" +
			"  new Lock().implicitLock();\n" +
			"}\n" +
			"}\n" +
			"class Lock extends Thread {\n" +
			"  byte step = 0;\n" +
			"  void logStep(String start) {\n" +
			"    System.out.println(start + \" \" + this.step); //$NON-NLS-1$\n" +
			"  }\n" +
			"  public void run() {\n" +
			"    for (int i = 1; i < 3; i++) {\n" +
			"      logStep(\"explicit lock\"); //$NON-NLS-1$\n" +
			"      synchronized (this) {\n" +
			"        this.step++;\n" +
			"        notify();\n" +
			"        while(this.step < 2 * i) {\n" +
			"          try {\n" +
			"            wait();\n" +
			"          } catch (InterruptedException e) {\n" +
			"            System.out.println(\"EXCEPTION\"); //$NON-NLS-1$\n" +
			"          }\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  synchronized void implicitLock() {\n" +
			"      this.start();\n" +
			"      for (int i = 0; i < 2; i++) {\n" +
			"        while (this.step < 1 + i * 2) {\n" +
			"          try {\n" +
			"            wait();\n" +
			"          } catch (InterruptedException e) {\n" +
			"            System.out.println(\"EXCEPTION\"); //$NON-NLS-1$\n" +
			"          }\n" +
			"        }\n" +
			"        logStep(\"implicit lock\"); //$NON-NLS-1$\n" +
			"        this.step++;\n" +
			"        notify();\n" +
			"      }\n" +
			"      return;\n" +
			"  }\n" +
			"}\n"},
		"explicit lock 0\n" +
		"implicit lock 1\n" +
		"explicit lock 2\n" +
		"implicit lock 3"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=126712
// reflection - access to a public method of a package visible
// class through a public extending class
public void test0600_reflection() {
	runConformTest(
		true,
		new String[] {
			"X.java",
			"import java.lang.reflect.*;\n" +
			"import p.*;\n" +
			"public class X {\n" +
			"static public void main (String args[]) {\n" +
			"  Y y = new Y();\n" +
			"  try {\n" +
			"    Method foo = Y.class.getMethod(\"foo\", (Class []) null);\n" +
			"    y.foo();\n" +
			"    foo.invoke(y, (Object []) null);\n" +
			"  } catch (NoSuchMethodException e) {\n" +
			"      //ignore\n" +
			"  } catch (InvocationTargetException e) {\n" +
			"      //ignore\n" +
			"  } catch (IllegalAccessException e) {\n" +
			"    System.out.print(\"FAILURE: IllegalAccessException\");\n" +
			"  }\n" +
			"}\n" +
			"}",
			"p/Y.java",
			"package p;\n" +
			"public class Y extends Z {\n" +
			"  /* empty */\n" +
			"}\n",
			"p/Z.java",
			"package p;\n" +
			"class Z {\n" +
			"  public void foo() {\n" +
			"  System.out.println(\"SUCCESS\"); //$NON-NLS-1$\n" +
			"  }\n" +
			"}\n"},
		"",
		this.complianceLevel <= ClassFileConstants.JDK1_5 ? "SUCCESS\n" + "FAILURE: IllegalAccessException" : "SUCCESS\n" + "SUCCESS",
		"",
		JavacTestOptions.EclipseJustification.EclipseBug126712
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=126712
// reflection - access to a public field of a package visible
// class through a public extending class
public void test0601_reflection() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.lang.reflect.*;\n" +
			"import p.*;\n" +
			"public class X {\n" +
			"static public void main (String args[]) {\n" +
			"  Y y = new Y();\n" +
			"  try {\n" +
			"    Field f = Y.class.getField(\"m\");\n" +
			"    System.out.println(y.m);\n" +
			"    System.out.println(f.get(y));\n" +
			"  } catch (NoSuchFieldException e) {\n" +
			"      //ignore\n" +
			"  } catch (IllegalAccessException e) {\n" +
			"    System.out.print(\"FAILURE: IllegalAccessException\");\n" +
			"  }\n" +
			"}\n" +
			"}",
			"p/Y.java",
			"package p;\n" +
			"public class Y extends Z {\n" +
			"  /* empty */\n" +
			"}\n",
			"p/Z.java",
			"package p;\n" +
			"class Z {\n" +
			"  public String m = \"SUCCESS\";\n" +
			"}\n"},
		"SUCCESS\n" +
		"FAILURE: IllegalAccessException"
	);
}

// partial rebuild - method signature changed (return type)
public void test1000_partial_rebuild() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"static public void main(String args[]) {\n" +
			"  Z.go();\n" +
			"}\n" +
			"}\n",
			"Z.java",
			"public class Z {\n" +
			"static public void go() {\n" +
			"  int flag = 0;\n" +
			"  try {\n" +
			"    new Y().random();\n" +
			"    flag = 1;\n" +
			"  }\n" +
			"  catch (NoSuchMethodError e) {\n" +
			"    flag = 2;\n" +
			"  }\n" +
			"  catch (Throwable t) {\n" +
			"    flag = 3;\n" +
			"  }\n" +
			"  System.out.println(flag);\n" +
			"}\n" +
			"}\n",
			"Y.java",
			"public class Y {\n" +
			"java.util.Random generator = new java.util.Random();" +
			"public byte random() {\n" +
			"  return (byte) (generator.nextInt() % Byte.MAX_VALUE);\n" +
			"}\n" +
			"}\n",
			},
		"1");
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"static public void main(String args[]) {\n" +
			"  Z.go();\n" +
			"}\n" +
			"}\n",
			"Y.java",
			"public class Y {\n" +
			"java.util.Random generator = new java.util.Random();" +
			"public int random() {\n" + // random now returns an int
			"  return generator.nextInt();\n" +
			"}\n" +
			"}\n",
			},
		"2",
		null,
		false, // do not purge output directory - pick old version of Z.class
		null);
}

// partial rebuild - method signature changed (parameter type)
public void test1001_partial_rebuild() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"static public void main(String args[]) {\n" +
			"  Z.go();\n" +
			"}\n" +
			"}\n",
			"Z.java",
			"public class Z {\n" +
			"static public void go() {\n" +
			"  byte flag = 0;\n" +
			"  try {\n" +
			"    new Y().random(flag);\n" +
			"    flag = 1;\n" +
			"  }\n" +
			"  catch (NoSuchMethodError e) {\n" +
			"    flag = 2;\n" +
			"  }\n" +
			"  catch (Throwable t) {\n" +
			"    flag = 3;\n" +
			"  }\n" +
			"  System.out.println(flag);\n" +
			"}\n" +
			"}\n",
			"Y.java",
			"public class Y {\n" +
			"public int random(byte seed) {\n" +
			"  return seed++;\n" +
			"}\n" +
			"}\n",
			},
		"1");
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"static public void main(String args[]) {\n" +
			"  Z.go();\n" +
			"}\n" +
			"}\n",
			"Y.java",
			"public class Y {\n" +
			"public int random(int seed) {\n" + // seed now of type int
			"  return seed++;\n" +
			"}\n" +
			"}\n",
			},
		"2",
		null,
		false, // do not purge output directory - pick old version of Z.class
		null);
}

// partial rebuild - method signature changed (visibility)
public void test1002_partial_rebuild() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"static public void main(String args[]) {\n" +
			"  new Z().go();\n" +
			"}\n" +
			"}\n",
			"Z.java",
			"public class Z extends p.Y {\n" +
			"  class ZInner extends YInner {\n" +
			"    // empty\n" +
			"  }\n" +
			"public void go() {\n" +
			"  byte flag = 0;\n" +
			"  try {\n" +
			"    new ZInner().foo();\n" +
			"    flag = 1;\n" +
			"  }\n" +
			"  catch (IllegalAccessError e) {\n" +
			"    flag = 2;\n" +
			"  }\n" +
			"  catch (Throwable t) {\n" +
			"    flag = 3;\n" +
			"  }\n" +
			"  System.out.println(flag);\n" +
			"}\n" +
			"}\n",
			"p/Y.java",
			"package p;\n" +
			"public class Y {\n" +
			"  public class YInner {\n" +
			"    public void foo() {\n" +
			"      return;\n" +
			"    }\n" +
			"  }\n" +
			"}\n",
			},
		"1");
	this.runConformTest(
		false,
		new String[] {
			"X.java",
			"public class X {\n" +
			"static public void main(String args[]) {\n" +
			"  new Z().go();\n" +
			"}\n" +
			"}\n",
			"p/Y.java",
			"package p;\n" +
			"public class Y {\n" +
			"  public class YInner {\n" +
			"    void foo() {\n" + // now foo no more visible (package only)
			"      return;\n" +
			"    }\n" +
			"  }\n" +
			"}\n",
			},
		"",
		"2",
		"",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}

// partial rebuild - method signature changed (visibility)
public void test1003_partial_rebuild() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"static public void main(String args[]) {\n" +
			"  new Z().go();\n" +
			"}\n" +
			"}\n",
			"Z.java",
			"public class Z extends p.Y {\n" +
			"  class ZInner extends YInner {\n" +
			"    // empty\n" +
			"  }\n" +
			"public void go() {\n" +
			"  byte flag = 0;\n" +
			"  try {\n" +
			"    new ZInner().foo();\n" +
			"    flag = 1;\n" +
			"  }\n" +
			"  catch (IllegalAccessError e) {\n" +
			"    flag = 2;\n" +
			"  }\n" +
			"  catch (Throwable t) {\n" +
			"    flag = 3;\n" +
			"  }\n" +
			"  System.out.println(flag);\n" +
			"}\n" +
			"}\n",
			"p/Y.java",
			"package p;\n" +
			"public class Y {\n" +
			"  public class YInner {\n" +
			"    public void foo() {\n" +
			"      return;\n" +
			"    }\n" +
			"  }\n" +
			"}\n",
			},
		"1");
	this.runConformTest(
		false, // do not purge output directory - pick old version of Z.class
		new String[] {
			"X.java",
			"public class X {\n" +
			"static public void main(String args[]) {\n" +
			"  new Z().go();\n" +
			"}\n" +
			"}\n",
			"p/Y.java",
			"package p;\n" +
			"public class Y {\n" +
			"  public class YInner {\n" +
			"    protected void foo() {\n" +
			  // now foo no more visible (package + inheriting classes only)
			"      return;\n" +
			"    }\n" +
			"  }\n" +
			"}\n",
			},
		"",
		"2",
		"",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}

// partial rebuild - extending class now redefines extended class fields and
//                   methods
// was Compliance_1_x#test009
public void test1004_partial_rebuild() {
	this.runConformTest(
		new String[] {
			"p1/Z.java",
			"package p1; \n"+
			"public class Z {	\n" +
			"	public static void main(String[] arguments) { \n"+
			"		Y y = new Y();	\n" +
			"		System.out.print(y.field);	\n"	+
			"		System.out.print(y.staticField);	\n"	+
			"		System.out.print(y.method());	\n"	+
			"		System.out.println(y.staticMethod());	\n"	+
			"	} \n"+
			"} \n",
			"p1/X.java",
			"package p1; \n"+
			"public class X { \n"+
			"	public String field = \"X.field-\";	\n" +
			"	public static String staticField = \"X.staticField-\";	\n" +
			"	public String method(){ return \"X.method()-\";	}	\n" +
			"	public static String staticMethod(){ return \"X.staticMethod()-\";	}	\n" +
			"} \n",
			"p1/Y.java",
			"package p1; \n"+
			"public class Y extends X { \n"+
			"} \n"
		},
		"X.field-X.staticField-X.method()-X.staticMethod()-");
	String expectedOutput =
		this.complianceLevel == ClassFileConstants.JDK1_3 ?
			"X.field-X.staticField-Y.method()-X.staticMethod()-" :
			"Y.field-Y.staticField-Y.method()-Y.staticMethod()-";
	this.runConformTest(
		new String[] {
			"p1/Y.java",
			"package p1; \n"+
			"public class Y extends X { \n"+
			"	public static void main(String[] arguments) { \n"+
			"		Z.main(arguments);	\n" +
			"	}	\n" +
			"	public String field = \"Y.field-\";	\n" +
			"	public static String staticField = \"Y.staticField-\";	\n" +
			"	public String method(){ return \"Y.method()-\";	}	\n" +
			"	public static String staticMethod(){ return \"Y.staticMethod()-\";	}	\n" +
			"} \n"
		},
		expectedOutput, // expected output
		null, // use default class-path
		false, // do not flush previous output dir content
		null); // no special vm args
}

}
