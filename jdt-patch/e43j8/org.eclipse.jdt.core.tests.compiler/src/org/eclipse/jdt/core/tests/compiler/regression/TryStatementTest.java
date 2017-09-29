/*******************************************************************************
 * Copyright (c) 2003, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
 *								bug 387612 - Unreachable catch block...exception is never thrown from the try
 *     Jesper Steen Moller - Contribution for
 *								bug 404146 - [1.7][compiler] nested try-catch-finally-blocks leads to unrunnable Java byte code
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;
public class TryStatementTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "testBug387612" };
//	TESTS_NUMBERS = new int[] { 74, 75 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public TryStatementTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
protected Map getCompilerOptions() {
	Map compilerOptions = super.getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ShareCommonFinallyBlocks, CompilerOptions.ENABLED);
	return compilerOptions;
}
public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  public class X1 {\n" +
		"    public X1() throws Exception {\n" +
		"    }\n" +
		"  }\n" +
		"  public void method1(){\n" +
		"    try {\n" +
		"      new X1() {\n" +
		"      };\n" +
		"    } catch(Exception e){\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
	});
}

public void test002() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"import java.io.*;\n" +
		"import java.util.zip.*;\n" +
		"class X {\n" +
		"  void bar() throws ZipException, IOException {}\n" +
		"  void foo() {\n" +
		"    try {\n" +
		"      bar();\n" +
		"    } catch (ZipException e) {\n" +
		"    } catch (IOException e) {\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
	});
}

public void test003() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  public class A1 {\n" +
		"    public A1() throws Exception {\n" +
		"    }\n" +
		"  }\n" +
		"  public void method1(){\n" +
		"    try {\n" +
		"      new A1() {\n" +
		"      };\n" +
		"    } catch(Exception e){\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
	});
}

public void test004() {
	this.runConformTest(new String[] {
		"p/ATC.java",
		"package p;\n" +
		"public class ATC {\n" +
		"    \n" +
		"    public class B extends Exception {\n" +
		"      public B(String msg) { super(msg); }\n" +
		"    }\n" +
		"    \n" +
		"    void foo() throws ATC.B {\n" +
		"      Object hello$1 = null;\n" +
		"      try {\n" +
		"        throw new B(\"Inside foo()\");\n" +
		"      } catch(B e) {\n" +
		"        System.out.println(\"Caught B\");\n" +
		"      }    \n" +
		"    }       \n" +
		"}\n",
	});
}

public void test005() {
	this.runConformTest(new String[] {
		"p/A.java",
		"package p;\n" +
		"import java.io.IOException;\n" +
		"import java.util.Vector;\n" +
		"/**\n" +
		" * This test0 should run without producing a java.lang.ClassFormatError\n" +
		" */\n" +
		"public class A {\n" +
		"  public Vector getComponents () {\n" +
		"    try{\n" +
		"      throw new IOException();\n" +
		"    }\n" +
		"    catch (IOException ioe) {\n" +
		"    }\n" +
		"    return null;\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    new A().getComponents();\n" +
		"  }\n" +
		"}\n",
	});
}

public void test006() {
	this.runConformTest(new String[] {
		"p/T.java",
		"package p;\n" +
		"import java.lang.reflect.*;\n" +
		"public class T extends InvocationTargetException {\n" +
		"  public static void main(String[] args) {\n" +
		"    T ct = new T();\n" +
		"    ct.getTargetException();\n" +
		"  }\n" +
		"  public Throwable getTargetException() {\n" +
		"    Runnable runnable = new Runnable() {\n" +
		"      public void run() {\n" +
		"        System.out.println(\"we got here\");\n" +
		"        T.super.getTargetException();\n" +
		"      }\n" +
		"    };\n" +
		"    runnable.run();\n" +
		"    return new Throwable();\n" +
		"  }\n" +
		"}\n",
	});
}
public void test007() {
	this.runConformTest(new String[] {
		"TryFinally.java",
		"class TryFinally {	\n"+
		"	public int readFile(String filename) throws Exception {	\n"+
		"		int interfaceID = -1;	\n"+
		"		int iNdx = 0;	\n"+
		"		try {	\n"+
		"			try {	\n"+
		"				return interfaceID;	\n"+
		"			} // end try	\n"+
		"			finally {	\n"+
		"				iNdx = 1;	\n"+
		"			} // end finally	\n"+
		"		} // end try	\n"+
		"		catch (Exception ex) {	\n"+
		"			throw new Exception(\"general exception \" + ex.getMessage() + \" on processing file \" + filename);	\n"+
		"		} // end catch	\n"+
		"		finally {	\n"+
		"		} // end finally	\n"+
		"	} // end readFile method	\n"+
		"}	\n"
});
}
/*
 * 1FZR1TO: IVJCOM:WIN - Class does not compile in VAJava 3.02-Java2
 */
public void test008() {
	this.runConformTest(
		new String[] {
			"RedundantException.java",
			"import java.io.*;\n" +
			"public class RedundantException {\n" +
			"	/**\n" +
			"	     * Runs the class as an application.\n" +
			"	     */\n" +
			"	public static void main(String[] args) {\n" +
			"		RedundantException re = new RedundantException();\n" +
			"		re.catchIt();\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"	/**\n" +
			"	     * Defines a method that lists an exception twice.\n" +
			"	     * This can be buried in a much longer list.\n" +
			"	     */\n" +
			"	void throwIt() throws IOException, IOException {\n" +
			"		throw new IOException();\n" +
			"	}\n" +
			"	/**\n" +
			"	     * Catches the redundantly defined exception.\n" +
			"	     */\n" +
			"	void catchIt() {\n" +
			"		try {\n" +
			"			throwIt(); // compile error here\n" +
			"		} catch (IOException e) {\n" +
			"			System.out.println(\"Caught.\");\n" +
			"		}\n" +
			"	}\n" +
			"}"
		},
		"Caught.\n" +
		"SUCCESS");
}
public void test009() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"public void save() {\n" +
			"	int a = 3;\n" +
			"	try {\n" +
			"		Object warnings = null;\n" +
			"      	try {\n" +
			"         	Object contexts = null;\n" +
			"         	try {\n" +
			"            	System.out.println(warnings);\n" +
			"			 	return;\n" +
			"      	 	} catch (NullPointerException npe) {\n" +
			"				System.out.println(contexts);\n" +
			"               return;\n" +
			"       	}\n" +
			"		} catch (Exception e) {\n" +
			" 			return;\n" +
			"   	}\n" +
			"	} finally {\n" +
			"     	int b = 4;\n" +
			"       System.out.println(\"#save -> \" + b + a);\n" +
			"    }\n" +
			"}\n" +
			"public static void main(String[] args) {\n" +
			"	new Test().save();\n"+
			"}\n" +
			"}"
		},
		"null\n" +
		"#save -> 43");
}
public void test010() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"public void save() {\n" +
			"	int a = 3;\n" +
			"	try {\n" +
			"		Object warnings = null;\n" +
			"      	try {\n" +
			"         	Object contexts = null;\n" +
			"         	try {\n" +
			"            	System.out.println(warnings);\n" +
			"			 	return;\n" +
			"      	 	} catch (NullPointerException npe) {\n" +
			"				System.out.println(contexts);\n" +
			"               return;\n" +
			"       	}\n" +
			"		} catch (Exception e) {\n" +
			" 			return;\n" +
			"   	}\n" +
			"	} catch(Exception e){\n"+
			"		Object dummy1 = null;\n" +
			"		System.out.println(dummy1);\n" +
			"		Object dummy2 = null;\n" +
			"		System.out.println(dummy2);\n" +
			"		return;\n"+
			"	} finally {\n" +
			"     	int b = 4;\n" +
			"       System.out.println(\"#save -> \" + b + a);\n" +
			"    }\n" +
			"}\n" +
			"public static void main(String[] args) {\n" +
			"	new Test().save();\n"+
			"}\n" +
			"}"
		},
		"null\n" +
		"#save -> 43");
}

public void test011() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"public void save() {\n" +
			"	int a = 3;\n" +
			"	try {\n" +
			"		Object warnings = null;\n" +
			"      	try {\n" +
			"         	Object contexts = null;\n" +
			"         	try {\n" +
			"            	System.out.println(warnings);\n" +
			"			 	return;\n" +
			"      	 	} catch (NullPointerException npe) {\n" +
			"				System.out.println(contexts);\n" +
			"               return;\n" +
			"       	}\n" +
			"		} catch (Exception e) {\n" +
			" 			return;\n" +
			"   	}\n" +
			"	} catch(Exception e){\n"+
			"		int dummy1 = 11;\n" +
			"		System.out.println(dummy1);\n" +
			"		int dummy2 = 12;\n" +
			"		System.out.println(dummy2);\n" +
			"		return;\n"+
			"	} finally {\n" +
			"     	int b = 4;\n" +
			"       System.out.println(\"#save -> \" + b + a);\n" +
			"    }\n" +
			"}\n" +
			"public static void main(String[] args) {\n" +
			"	new Test().save();\n"+
			"}\n" +
			"}"
		},
		"null\n" +
		"#save -> 43");
}
/*
 * 4943  Verification error
 */
public void test012() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			new X().delete(args);\n" +
			"			System.out.println(\"success\");\n" +
			"		} catch (Exception e) {\n" +
			"		}\n" +
			"	}\n" +
			"	void bar(int i) {\n" +
			"	}\n" +
			"	public Object delete(String[] resources) throws IOException {\n" +
			"		try {\n" +
			"			int totalWork = 3;\n" +
			"			Object result = \"aaa\";\n" +
			"			try {\n" +
			"				return result;\n" +
			"			} catch (Exception e) {\n" +
			"				throw new IOException();\n" +
			"			} finally {\n" +
			"				bar(totalWork);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			bar(0);\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"success");
}

/*
 * 4943  Verification error
 */
public void test013() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			new X().delete(args);\n" +
			"			System.out.println(\"success\");\n" +
			"		} catch (Exception e) {\n" +
			"		}\n" +
			"	}\n" +
			"	void bar(int i) {\n" +
			"	}\n" +
			"	public Object delete(String[] resources) throws IOException {\n" +
			"		try {\n" +
			"			int totalWork = 3;\n" +
			"			Object result = \"aaa\";\n" +
			"			try {\n" +
			"				return result;\n" +
			"			} catch (Exception e) {\n" +
			"				throw new IOException();\n" +
			"			} finally {\n" +
			"				bar(totalWork);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			int totalWork = 4;\n" +
			"			bar(totalWork);\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"success");
}
public void test014() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"public void save() {\n" +
			"	int a = 3;\n" +
			"	try {\n" +
			"		Object warnings = null;\n" +
			"      	try {\n" +
			"         	int contexts = 17;\n" +
			"         	try {\n" +
			"				Object dummy = null;\n" +
			"            	System.out.println(warnings);\n" +
			"            	System.out.println(dummy);\n" +
			"			 	return;\n" +
			"      	 	} catch (NullPointerException npe) {\n" +
			"				System.out.println(contexts);\n" +
			"               return;\n" +
			"       	}\n" +
			"		} catch (Exception e) {\n" +
			" 			return;\n" +
			"   	} finally { \n" +
			"			int c = 34; \n"+
			"			System.out.println(\"#inner-finally ->\" + a + c);\n"+
			"       }\n" +
			"	} finally {\n" +
			"     	int b = 4;\n" +
			"       System.out.println(\"#save -> \" + b + a);\n" +
			"    }\n" +
			"}\n" +
			"public static void main(String[] args) {\n" +
			"	new Test().save();\n"+
			"}\n" +
			"}"
		},
		"null\n" +
		"null\n" +
		"#inner-finally ->334\n" +
		"#save -> 43");
}

public void test015() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n" +
			"import java.io.IOException;	\n" +
			"public class X {	\n" +
			"	public static void main(String args[]) {	\n" +
			"		try { 	\n" +
			"			new Object(){	\n" +
			"				{	\n" +
			"					if (true) throw new IOException();	\n" +
			"					if (true) throw new Exception();	\n" +
			"				}	\n" +
			"			};	\n" +
			"			System.out.println(\"FAILED\");	\n" +
			"		} catch(Exception e){	\n" +
			"			System.out.println(\"SUCCESS\");	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n",
		},
		"SUCCESS");
}
public void test016() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n" +
			"import java.io.IOException;	\n" +
			"public class X {	\n" +
			"	public static void main(String args[]) {	\n" +
			"		class SomeClass {	\n" +
			"			SomeClass () throws IOException {	\n" +
			"			}	\n" +
			"		}	\n" +
			"		try { 	\n" +
			"			new Object(){	\n" +
			"				{	\n" +
			"					if (true) throw new IOException();	\n" +
			"					if (true) throw new Exception();	\n" +
			"				}	\n" +
			"			};	\n" +
			"			System.out.println(\"FAILED\");	\n" +
			"		} catch(Exception e){	\n" +
			"			System.out.println(\"SUCCESS\");	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n",
		},
		"SUCCESS");
}
public void test017() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n" +
			"public class X {	\n" +
			"	public static void main(String args[]) {	\n" +
			"		try { 	\n" +
			"			new Object(){	\n" +
			"				{	\n" +
			"					foo();	\n" +
			"				}	\n" +
			"			};	\n" +
			"			System.out.println(\"FAILED\");	\n" +
			"		} catch(Exception e){	\n" +
			"			System.out.println(\"SUCCESS\");	\n" +
			"		}	\n" +
			"	}	\n" +
			"	static class AEx extends Exception {} \n" +
			"	static class BEx extends Exception {} \n" +
			"	static void foo() throws AEx, BEx {	\n" +
			"		throw new AEx();	\n"+
			"	}	\n" +
			"}	\n",
		},
		"SUCCESS");
}

// 8773 verification error
public void test018() {
	this.runConformTest(
		new String[] {
			"VerifyEr.java",
			"public class VerifyEr {	\n" +
			"  protected boolean err(boolean b) {	\n" +
			"     try {	\n" +
			"          System.out.print(\"SUCC\");	\n" +
			"     } catch (Throwable t) {	\n" +
			"          return b;	\n" +
			"     } finally {	\n" +
			"          try {	\n" +
			"               if (b) {	\n" +
			"                    return b;	\n" +
			"               }	\n" +
			"          } finally {	\n" +
			"          		System.out.println(\"ESS\");	\n" +
			"          }	\n" +
			"     }	\n" +
			"     return false;	\n" +
			"  }	\n" +
			"  public static void main(String[] args) {	\n" +
			"     new VerifyEr().err(false);	\n" +
			"  }	\n" +
			"}	\n",
		},
		"SUCCESS");
}
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=16279
 */
public void test019() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"	String logger;	\n" +
			"  public static void main(String[] args) {	\n" +
			"    new X().foo();	\n" +
			"	}	\n"+
			"	public void foo() {	\n" +
			"		try {	\n" +
			"			System.out.println(\"SUCCESS\");	\n" +
			"		} catch (Exception ce) {	\n" +
			"			String s = null;	\n" +
			"			try {	\n" +
			"				return;	\n" +
			"			} catch (Exception ex) {	\n" +
			"			}	\n" +
			"			s.hashCode();	\n" +
			"		} finally {	\n" +
			"			if (this.logger == null) {	\n" +
			"				String loggerManager = null;	\n" +
			"				System.out.println(loggerManager);	\n" +
			"			}	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n"
		},
		"SUCCESS\n" +
		"null");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=16279
 * shifting of finaly scopes against try/catch ones makes the custom ret address shifting
 * unnecessary.
 */
public void test020() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"	String logger;	\n" +
			"  public static void main(String[] args) {	\n" +
			"    new X().foo();	\n" +
			"	}	\n"+
			"	public void foo() {	\n" +
			"		try {	\n" +
			"			System.out.println(\"try1\");	\n" +
			"			try {	\n" +
			"				System.out.println(\"try2\");	\n" +
			"			} finally {	\n" +
			"				System.out.println(\"finally2\");	\n" +
			"			}	\n" +
			"		} catch (Exception ce) {	\n" +
			"			String s = null;	\n" +
			"			try {	\n" +
			"				return;	\n" +
			"			} catch (Exception ex) {	\n" +
			"			}	\n" +
			"			s.hashCode();	\n" +
			"		} finally {	\n" +
			"			System.out.println(\"finally1\");	\n" +
			"			try {	\n" +
			"				System.out.println(\"try3\");	\n" +
			"				if (this.logger == null) {	\n" +
			"					String loggerManager = null;	\n" +
			"				}	\n" +
			"			} finally {	\n" +
			"				System.out.println(\"finally3\");	\n" +
			"			}	\n" +
			"		}	\n" +
			"		int i1 = 0;	\n" +
			"		int i2 = 0;	\n" +
			"		int i3 = 0;	\n" +
			"		int i4 = 0;	\n" +
			"		int i5 = 0;	\n" +
			"		int i6 = 0;	\n" +
			"		int i7 = 0;	\n" +
			"		int i8 = 0;	\n" +
			"		int i9 = 0;	\n" +
			"	}	\n" +
			"}	\n"
		},
		"try1\n" +
		"try2\n" +
		"finally2\n" +
		"finally1\n" +
		"try3\n" +
		"finally3");
}

/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=21116
 * protected type visibility check
 */
public void test021() {
	this.runConformTest(
		new String[] {
			"pa/A.java",
			"package pa;	\n" +
			"public abstract class A {	\n" +
			"  public static void main(String[] args) {	\n" +
			"    System.out.println(\"SUCCESS\");	\n" +
			"	}	\n"+
			"	protected AIC memberA;	\n" +
			"	protected class AIC {	\n" +
			"		public void methodAIC(String parameter) {	\n" +
			"		  // ....do something	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n",
			"pb/B.java",
			"package pb;	\n" +
			"public class B extends pa.A {	\n" +
			"	private class BIC {	\n" +
			"		public void methodBIC(String param) {	\n" +
			"			memberA.methodAIC(param);	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n"
		},
		"SUCCESS");
}

/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=19916
 * nested try/synchronized statements (local var index alloc)
 */
public void test022() {
	this.runConformTest(
		new String[] {
			"pa/A.java",
			"package pa;	\n" +
			"public class A {	\n" +
			"  public static void main(String[] args) {	\n" +
			"	 new A().f();	\n" +
			"    System.out.println(\"SUCCESS\");	\n" +
			"	}	\n"+
			"	boolean b = false;	\n" +
			"	private Integer f() {	\n" +
			"		while (true) {	\n" +
			"			try {	\n" +
			"				int x = 3;	\n" +
			"				synchronized (this) {	\n" +
			"					return null;	\n" +
			"				}	\n" +
			"			} finally {	\n" +
			"				if (b)	\n" +
			"					synchronized (this) {	\n" +
			"					int y = 3;	\n" +
			"				}	\n" +
			"			}	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n"
		},
		"SUCCESS");
}

public void test023() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportHiddenCatchBlock, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			throw new BX();\n" +
			"		} catch(BX e) {\n" +
			"		} catch(AX e) {\n" +
			"		}\n" +
			"	}\n" +
			"} \n" +
			"class AX extends Exception {}\n" +
			"class BX extends AX {}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. ERROR in X.java (at line 6)\n" +
		"	} catch(AX e) {\n" +
		"	        ^^\n" +
		"Unreachable catch block for AX. Only more specific exceptions are thrown and they are handled by previous catch block(s).\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 10)\n" +
		"	class AX extends Exception {}\n" +
		"	      ^^\n" +
		"The serializable class AX does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 11)\n" +
		"	class BX extends AX {}\n" +
		"	      ^^\n" +
		"The serializable class BX does not declare a static final serialVersionUID field of type long\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

 /*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21203
 * NPE in ExceptionFlowContext
 */
public void test024() {

	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;	\n" +
			"public class X {	\n" +
			"	public void myMethod() {	\n" +
			"	    System.out.println(\"starting\");	\n" +
			"	    try {	\n" +
			"	        if (true) throw new LookupException();	\n" +
			"	    } catch(DataException de) {	\n" +
			"	       	System.out.println(\"DataException occurred\");	\n" +
			"	    } catch(LookupException le) {	\n" +
			"	       	System.out.println(\"LookupException occurred\");	\n" +
			"	    } catch(Throwable t) {	\n" +
			"	       	System.out.println(\"Throwable occurred\");	\n" +
			"	    }	\n" +
			"	    System.out.println(\"SUCCESS\");	\n" +
			"	}	\n" +
			"}	\n" +
			"class DataException extends Throwable {	\n" +
			"} 	\n" +
			"class LookupException extends DataException {	\n" +
			"}	\n"
		},
		"----------\n" +
		"1. ERROR in p\\X.java (at line 9)\n" +
		"	} catch(LookupException le) {	\n" +
		"	        ^^^^^^^^^^^^^^^\n" +
		"Unreachable catch block for LookupException. It is already handled by the catch block for DataException\n" +
		"----------\n" +
		"2. WARNING in p\\X.java (at line 17)\n" +
		"	class DataException extends Throwable {	\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"The serializable class DataException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. WARNING in p\\X.java (at line 19)\n" +
		"	class LookupException extends DataException {	\n" +
		"	      ^^^^^^^^^^^^^^^\n" +
		"The serializable class LookupException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n");
}
// 60081
public void test025() {

	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X\n" +
			"{\n" +
			"    {\n" +
			"        String licenseFileName = \"C:/Program Files/Jatt/bin/license.key\";\n" +
			"        File licenseFile = new File(licenseFileName);\n" +
			"        try {\n" +
			"            BufferedReader licenseReader = new BufferedReader(\n" +
			"                new FileReader(licenseFile));\n" +
			"            StringBuffer buf = new StringBuffer();\n" +
			"            String line = null;\n" +
			"            while ((line = licenseReader.readLine()) != null) {\n" +
			"                char[] chars = line.toCharArray();\n" +
			"                for (int i = 0; i < line.length(); i++) {\n" +
			"                    if (!Character.isSpace(line.charAt(i))) {\n" +
			"                        buf.append(line.charAt(i));\n" +
			"                    }\n" +
			"                }\n" +
			"            }\n" +
			"            \n" +
			"        } catch (FileNotFoundException e) {\n" +
			"            throw new Error(\"License file not found\", e);\n" +
			"        } catch (IOException e) {\n" +
			"            throw new Error(\"License file cannot be read\", e);\n" +
			"        }\n" +
			"    }\n" +
			"  public X()\n" +
			"  {\n" +
			"  }\n" +
			"  \n" +
			"  public X(X r) \n" +
			"  {\n" +
			"  }    \n" +
			"  public static void main(String[] args) {\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n"
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=89710
public void test026() throws Exception {

	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"\n" +
			"public class X {\n" +
			"	\n" +
			"	static private ResourceBundle bundle = null;\n" +
			"	static {\n" +
			"		int i = 0;\n" +
			"		try {\n" +
			"			bundle = foo();\n" +
			"		} catch(Throwable e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	static ResourceBundle foo() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n",
		},
		"",
		null,
		true,
		null,
		customOptions,
		null); // custom requestor

	String expectedOutput =
		"      Local variable table:\n" +
		"        [pc: 6, pc: 20] local: i index: 0 type: int\n" +
		"        [pc: 16, pc: 20] local: e index: 1 type: java.lang.Throwable\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=89710 - variation
public void test027() throws Exception {

	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"\n" +
			"public class X {\n" +
			"	\n" +
			"	void bar(boolean b) {\n" +
			"		if (b) {\n" +
			"			try {\n" +
			"				int i = 0;\n" +
			"			} catch(Exception e) {\n" +
			"				e.printStackTrace();\n" +
			"			}\n" +
			"		} else {\n" +
			"			int j = 0;\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"",
		null,
		true,
		null,
		customOptions,
		null); // custom requestor

	String expectedOutput =
		"      Local variable table:\n" +
		"        [pc: 0, pc: 20] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 20] local: b index: 1 type: boolean\n" +
		"        [pc: 10, pc: 14] local: e index: 2 type: java.lang.Exception\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=98892
public void test028() {

	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"    public static void main(String[] args) {\n" +
			"    	try {\n" +
			"	        new X().start();\n" +
			"    	} catch(Exception e) {\n" +
			"            System.out.println(\"SUCCESS\");\n" +
			"    	}\n" +
			"    }\n" +
			"    public Object start() {\n" +
			"        try {\n" +
			"            return null;\n" +
			"        } finally {\n" +
			"            System.out.print(\"ONCE:\");\n" +
			"            foo();\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    private void foo() {\n" +
			"        throw new IllegalStateException(\"Gah!\");\n" +
			"    }        \n" +
			"}\n",
		},
		"ONCE:SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=98892 - variation
public void test029() {

	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"    public static void main(String[] args) {\n" +
			"    	try {\n" +
			"	        new X().start();\n" +
			"    	} catch(Exception e) {\n" +
			"            System.out.println(\"SUCCESS\");\n" +
			"    	}\n" +
			"    }\n" +
			"    public Object start() {\n" +
			"        try {\n" +
			"            return null;\n" +
			"        } finally {\n" +
			"            System.out.print(\"ONCE:\");\n" +
			"            foo();\n" +
			"            return this;\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    private void foo() {\n" +
			"        throw new IllegalStateException(\"Gah!\");\n" +
			"    }        \n" +
			"}\n",
		},
		"ONCE:SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=98892 - variation
public void test030() {

	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"    public static void main(String[] args) {\n" +
			"    	try {\n" +
			"	        new X().start();\n" +
			"    	} catch(Exception e) {\n" +
			"            System.out.println(\"SUCCESS\");\n" +
			"    	}\n" +
			"    }\n" +
			"    public Object start() {\n" +
			"        try {\n" +
			"            Object o = null;\n" +
			"            o.toString();\n" +
			"            return null;\n" +
			"        } catch(Exception e) {\n" +
			"            System.out.print(\"EXCEPTION:\");\n" +
			"			return e;        	\n" +
			"        } finally {\n" +
			"            System.out.print(\"ONCE:\");\n" +
			"            foo();\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    private void foo() {\n" +
			"        throw new IllegalStateException(\"Gah!\");\n" +
			"    }        \n" +
			"}\n",
		},
		"EXCEPTION:ONCE:SUCCESS");
}
/*
 * Try block is never reached
 */
public void test031() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"import java.io.IOException;\n" +
			"\n" +
			"public class X {\n" +
			"	static void foo(Object o) {}\n" +
			"	\n" +
			"    public static void main(String[] args) {\n" +
			"    	try {\n" +
			"    		foo(new Object() {\n" +
			"    			public void bar() throws IOException {\n" +
			"    				bar1();\n" +
			"    			}\n" +
			"    		});\n" +
			"    	} catch(IOException e) {\n" +
			"    		e.printStackTrace();\n" +
			"    	}\n" +
			"    }\n" +
			"    \n" +
			"    static void bar1() throws IOException {}\n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 13)\n" +
		"	} catch(IOException e) {\n" +
		"	        ^^^^^^^^^^^\n" +
		"Unreachable catch block for IOException. This exception is never thrown from the try statement body\n" +
		"----------\n",
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=114855
 */
public void test032() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X\n" +
			"{\n" +
			"  static int except_count;\n" +
			"\n" +
			"  static boolean test_result = true;\n" +
			"  \n" +
			"  static Throwable all_except[] =\n" +
			"  {\n" +
			"    new AbstractMethodError(),             //  0\n" +
			"    new ArithmeticException(),             //  1\n" +
			"    new ArrayIndexOutOfBoundsException(),  //  2\n" +
			"    new ArrayStoreException(),             //  3\n" +
			"    new ClassCastException(),              //  4\n" +
			"    new ClassCircularityError(),           //  5\n" +
			"    new ClassFormatError(),                //  6\n" +
			"    new ClassNotFoundException(),          //  7\n" +
			"    new CloneNotSupportedException(),      //  8\n" +
			"    new Error(),                           //  9\n" +
			"    new Exception(),                       // 10\n" +
			"    new IllegalAccessError(),              // 11\n" +
			"    new IllegalAccessException(),          // 12\n" +
			"    new IllegalArgumentException(),        // 13\n" +
			"    new IllegalMonitorStateException(),    // 14\n" +
			"    new IllegalThreadStateException(),     // 15\n" +
			"    new IncompatibleClassChangeError(),    // 16\n" +
			"    new IndexOutOfBoundsException(),       // 17\n" +
			"    new InstantiationError(),              // 18\n" +
			"    new InstantiationException(),          // 19\n" +
			"    new InternalError(),                   // 20\n" +
			"    new InterruptedException(),            // 21\n" +
			"    new LinkageError(),                    // 22\n" +
			"    new NegativeArraySizeException(),      // 23\n" +
			"    new NoClassDefFoundError(),            // 24\n" +
			"    new NoSuchFieldError(),                // 25\n" +
			"    new NoSuchMethodError(),               // 26\n" +
			"    new NoSuchMethodException(),           // 27\n" +
			"    new NullPointerException(),            // 28\n" +
			"    new NumberFormatException(),           // 29\n" +
			"    new OutOfMemoryError(),                // 30\n" +
			"    new StackOverflowError(),              // 31\n" +
			"    new RuntimeException(),                // 32\n" +
			"    new SecurityException(),               // 33\n" +
			"    new StringIndexOutOfBoundsException(), // 34\n" +
			"    new ThreadDeath(),                     // 35\n" +
			"    new UnknownError(),                    // 36\n" +
			"    new UnsatisfiedLinkError(),            // 37\n" +
			"    new VerifyError(),                     // 38\n" +
			"  };\n" +
			"\n" +
			"  private static void check_except(int i)\n" +
			"    throws Throwable\n" +
			"  {\n" +
			"    if (except_count != i)\n" +
			"    {\n" +
			"      System.out.println(\"Error \"+except_count+\" != \"+i+\";\");\n" +
			"      test_result=false;\n" +
			"    }\n" +
			"    throw all_except[++except_count];\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) throws Throwable\n" +
			"  {\n" +
			"    try {\n" +
			"      except_count = 0;\n" +
			"      throw all_except[except_count];\n" +
			"    } catch (AbstractMethodError e0) {\n" +
			"      try {\n" +
			"        check_except(0);\n" +
			"      } catch (ArithmeticException e1) {\n" +
			"        try {\n" +
			"          check_except(1);\n" +
			"        } catch (ArrayIndexOutOfBoundsException e2) {\n" +
			"          try {\n" +
			"            check_except(2);\n" +
			"          } catch (ArrayStoreException e3) {\n" +
			"            try {\n" +
			"              check_except(3);\n" +
			"            } catch (ClassCastException e4) {\n" +
			"              try {\n" +
			"                check_except(4);\n" +
			"              } catch (ClassCircularityError e5) {\n" +
			"                try {\n" +
			"                  check_except(5);\n" +
			"                } catch (ClassFormatError e6) {\n" +
			"                  try {\n" +
			"                    check_except(6);\n" +
			"                  } catch (ClassNotFoundException e7) {\n" +
			"                    try {\n" +
			"                      check_except(7);\n" +
			"                    } catch (CloneNotSupportedException e8) {\n" +
			"                      try {\n" +
			"                        check_except(8);\n" +
			"                      } catch (Error e9) {\n" +
			"                        try {\n" +
			"                          check_except(9);\n" +
			"                        } catch (Exception e10) {\n" +
			"                          try {\n" +
			"                            check_except(10);\n" +
			"                          } catch (IllegalAccessError e11) {\n" +
			"                            try {\n" +
			"                              check_except(11);\n" +
			"                            } catch (IllegalAccessException e12) {\n" +
			"                              try {\n" +
			"                                check_except(12);\n" +
			"                              } catch (IllegalArgumentException e13) {\n" +
			"                                try {\n" +
			"                                  check_except(13);\n" +
			"                                } catch (IllegalMonitorStateException e14) {\n" +
			"                                  try {\n" +
			"                                    check_except(14);\n" +
			"                                  } catch (IllegalThreadStateException e15) {\n" +
			"                                    try {\n" +
			"                                      check_except(15);\n" +
			"                                    } catch (IncompatibleClassChangeError e16) {\n" +
			"                                      try {\n" +
			"                                        check_except(16);\n" +
			"                                      } catch (IndexOutOfBoundsException e17) {\n" +
			"                                        try {\n" +
			"                                          check_except(17);\n" +
			"                                        } catch (InstantiationError e18) {\n" +
			"                                          try {\n" +
			"                                            check_except(18);\n" +
			"                                          } catch (InstantiationException e19) {\n" +
			"                                            try {\n" +
			"                                              check_except(19);\n" +
			"                                            } catch (InternalError e20) {\n" +
			"                                              try {\n" +
			"                                                check_except(20);\n" +
			"                                              } catch (InterruptedException \n" +
			"e21) {\n" +
			"                                                try {\n" +
			"                                                  check_except(21);\n" +
			"                                                } catch (LinkageError e22) {\n" +
			"                                                  try {\n" +
			"                                                    check_except(22);\n" +
			"                                                  } catch \n" +
			"(NegativeArraySizeException e23) {\n" +
			"                                                    try {\n" +
			"                                                      check_except(23);\n" +
			"                                                    } catch \n" +
			"(NoClassDefFoundError e24) {\n" +
			"                                                      try {\n" +
			"                                                        check_except(24);\n" +
			"                                                      } catch (NoSuchFieldError \n" +
			"e25) {\n" +
			"                                                        try {\n" +
			"                                                          check_except(25);\n" +
			"                                                        } catch \n" +
			"(NoSuchMethodError e26) {\n" +
			"                                                          try {\n" +
			"                                                            check_except(26);\n" +
			"                                                          } catch \n" +
			"(NoSuchMethodException e27) {\n" +
			"                                                            try {\n" +
			"                                                              check_except(27);\n" +
			"                                                            } catch \n" +
			"(NullPointerException e28) {\n" +
			"                                                              try {\n" +
			"                                                                check_except\n" +
			"(28);\n" +
			"                                                              } catch \n" +
			"(NumberFormatException e29) {\n" +
			"                                                                try {\n" +
			"                                                                  check_except\n" +
			"(29);\n" +
			"                                                                } catch \n" +
			"(OutOfMemoryError e30) {\n" +
			"                                                                  try {\n" +
			"                                                                    check_except\n" +
			"(30);\n" +
			"                                                                  } catch \n" +
			"(StackOverflowError e31) {\n" +
			"                                                                    try {\n" +
			"                                                                      \n" +
			"check_except(31);\n" +
			"                                                                    } catch \n" +
			"(RuntimeException e32) {\n" +
			"                                                                      try {\n" +
			"                                                                        \n" +
			"check_except(32);\n" +
			"                                                                      } catch \n" +
			"(SecurityException e33) {\n" +
			"                                                                        try {\n" +
			"                                                                          \n" +
			"check_except(33);\n" +
			"                                                                        } catch \n" +
			"(StringIndexOutOfBoundsException e34) {\n" +
			"                                                                          try {\n" +
			"                                                                            \n" +
			"check_except(34);\n" +
			"                                                                          } \n" +
			"catch (ThreadDeath e35) {\n" +
			"                                                                            try \n" +
			"{\n" +
			"                                                                              \n" +
			"check_except(35);\n" +
			"                                                                            } \n" +
			"catch (UnknownError e36) {\n" +
			"                                                                              \n" +
			"try {\n" +
			"                                                                                \n" +
			"check_except(36);\n" +
			"                                                                              } \n" +
			"catch (UnsatisfiedLinkError e37) {\n" +
			"                                                                                \n" +
			"try {\n" +
			"                                                                                \n" +
			"  check_except(37);\n" +
			"                                                                                \n" +
			"} catch (VerifyError e38) {\n" +
			"                                                                                \n" +
			"  ++except_count;\n" +
			"                                                                                \n" +
			"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}\n" +
			"    System.out.print(test_result & (except_count == all_except.length));\n" +
			"  }\n" +
			"}",
		},
		"true");
}
public void test033() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	interface IActionSetContributionItem {\n" +
				"		String getActionSetId();\n" +
				"	}\n" +
				"	public interface IAction {\n" +
				"	}\n" +
				"	interface IContributionItem {\n" +
				"		String getId();\n" +
				"		boolean isSeparator();\n" +
				"		boolean isGroupMarker();\n" +
				"	}\n" +
				"    public static void findInsertionPoint(String startId,\n" +
				"            String sortId, IContributionItem[] items) {\n" +
				"        // Find the reference item.\n" +
				"        try {\n" +
				"	        int insertIndex = 0;\n" +
				"	        while (insertIndex < items.length) {\n" +
				"	            if (startId.equals(items[insertIndex].getId()))\n" +
				"	                break;\n" +
				"	            ++insertIndex;\n" +
				"	        }\n" +
				"	        if (insertIndex >= items.length)\n" +
				"	            return;\n" +
				"	\n" +
				"	        int compareMetric = 0;\n" +
				"	\n" +
				"	        // Find the insertion point for the new item.\n" +
				"	        // We do this by iterating through all of the previous\n" +
				"	        // action set contributions define within the current group.\n" +
				"	        for (int nX = insertIndex + 1; nX < items.length; nX++) {\n" +
				"	            IContributionItem item = items[nX];\n" +
				"	            if (item.isSeparator() || item.isGroupMarker()) {\n" +
				"	                // Fix for bug report 18357\n" +
				"	                break;\n" +
				"	            }\n" +
				"	            if (item instanceof IActionSetContributionItem) {\n" +
				"	                if (sortId != null) {\n" +
				"	                    String testId = ((IActionSetContributionItem) item)\n" +
				"	                            .getActionSetId();\n" +
				"	                    if (sortId.compareTo(testId) < compareMetric)\n" +
				"	                        break;\n" +
				"	                }\n" +
				"	                insertIndex = nX;\n" +
				"	            } else {\n" +
				"	                break;\n" +
				"	            }\n" +
				"	        }\n" +
				"	    } catch(Exception e) {}\n" +
				"    }\n" +
				"    \n" +
				"    public static void main(String[] args) {\n" +
				"		findInsertionPoint(\"\", \"\", null);\n" +
				"	}\n" +
				"}",
			},
			"");
	String expectedOutput =
		"  // Method descriptor #15 (Ljava/lang/String;Ljava/lang/String;[LX$IContributionItem;)V\n" +
		"  // Stack: 3, Locals: 8\n" +
		"  public static void findInsertionPoint(java.lang.String startId, java.lang.String sortId, X.IContributionItem[] items);\n" +
		"      0  iconst_0\n" +
		"      1  istore_3 [insertIndex]\n" +
		"      2  goto 26\n" +
		"      5  aload_0 [startId]\n" +
		"      6  aload_2 [items]\n" +
		"      7  iload_3 [insertIndex]\n" +
		"      8  aaload\n" +
		"      9  invokeinterface X$IContributionItem.getId() : java.lang.String [16] [nargs: 1]\n" +
		"     14  invokevirtual java.lang.String.equals(java.lang.Object) : boolean [22]\n" +
		"     17  ifeq 23\n" +
		"     20  goto 32\n" +
		"     23  iinc 3 1 [insertIndex]\n" +
		"     26  iload_3 [insertIndex]\n" +
		"     27  aload_2 [items]\n" +
		"     28  arraylength\n" +
		"     29  if_icmplt 5\n" +
		"     32  iload_3 [insertIndex]\n" +
		"     33  aload_2 [items]\n" +
		"     34  arraylength\n" +
		"     35  if_icmplt 39\n" +
		"     38  return\n" +
		"     39  iconst_0\n" +
		"     40  istore 4 [compareMetric]\n" +
		"     42  iload_3 [insertIndex]\n" +
		"     43  iconst_1\n" +
		"     44  iadd\n" +
		"     45  istore 5 [nX]\n" +
		"     47  goto 123\n" +
		"     50  aload_2 [items]\n" +
		"     51  iload 5 [nX]\n" +
		"     53  aaload\n" +
		"     54  astore 6 [item]\n" +
		"     56  aload 6 [item]\n" +
		"     58  invokeinterface X$IContributionItem.isSeparator() : boolean [28] [nargs: 1]\n" +
		"     63  ifne 134\n" +
		"     66  aload 6 [item]\n" +
		"     68  invokeinterface X$IContributionItem.isGroupMarker() : boolean [32] [nargs: 1]\n" +
		"     73  ifeq 79\n" +
		"     76  goto 134\n" +
		"     79  aload 6 [item]\n" +
		"     81  instanceof X$IActionSetContributionItem [35]\n" +
		"     84  ifeq 134\n" +
		"     87  aload_1 [sortId]\n" +
		"     88  ifnull 117\n" +
		"     91  aload 6 [item]\n" +
		"     93  checkcast X$IActionSetContributionItem [35]\n" +
		"     96  invokeinterface X$IActionSetContributionItem.getActionSetId() : java.lang.String [37] [nargs: 1]\n" +
		"    101  astore 7 [testId]\n" +
		"    103  aload_1 [sortId]\n" +
		"    104  aload 7 [testId]\n" +
		"    106  invokevirtual java.lang.String.compareTo(java.lang.String) : int [40]\n" +
		"    109  iload 4 [compareMetric]\n" +
		"    111  if_icmpge 117\n" +
		"    114  goto 134\n" +
		"    117  iload 5 [nX]\n" +
		"    119  istore_3 [insertIndex]\n" +
		"    120  iinc 5 1 [nX]\n" +
		"    123  iload 5 [nX]\n" +
		"    125  aload_2 [items]\n" +
		"    126  arraylength\n" +
		"    127  if_icmplt 50\n" +
		"    130  goto 134\n" +
		"    133  astore_3\n" +
		"    134  return\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=124853
public void test034() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	private static int scenario(){\n" +
				"		try {\n" +
				"			int i = 1;\n" +
				"			System.out.print(\"[i: \" + i+\"]\");\n" +
				"			if (i > 5) {\n" +
				"				return i;\n" +
				"			}\n" +
				"			return -i;\n" +
				"		} catch (Exception e) {\n" +
				"			System.out.print(\"[WRONG CATCH]\");\n" +
				"			return 2;\n" +
				"		} finally {\n" +
				"			System.out.print(\"[finally]\");\n" +
				"			try {\n" +
				"				throwRuntime();\n" +
				"			} finally {\n" +
				"				clean();\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	private static void throwRuntime() {\n" +
				"		throw new RuntimeException(\"error\");\n" +
				"	}\n" +
				"\n" +
				"	private static void clean() {\n" +
				"		System.out.print(\"[clean]\");\n" +
				"	}\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			scenario();\n" +
				"		} catch(Exception e){\n" +
				"			System.out.println(\"[end]\");\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"}\n",
			},
			"[i: 1][finally][clean][end]");

//	if (this.complianceLevel.compareTo(COMPLIANCE_1_6) >= 0) return;
	String expectedOutput = new CompilerOptions(getCompilerOptions()).inlineJsrBytecode
		?	"  // Method descriptor #15 ()I\n" +
			"  // Stack: 4, Locals: 4\n" +
			"  private static int scenario();\n" +
			"      0  iconst_1\n" +
			"      1  istore_0 [i]\n" +
			"      2  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"      5  new java.lang.StringBuilder [22]\n" +
			"      8  dup\n" +
			"      9  ldc <String \"[i: \"> [24]\n" +
			"     11  invokespecial java.lang.StringBuilder(java.lang.String) [26]\n" +
			"     14  iload_0 [i]\n" +
			"     15  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [29]\n" +
			"     18  ldc <String \"]\"> [33]\n" +
			"     20  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [35]\n" +
			"     23  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]\n" +
			"     26  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"     29  iload_0 [i]\n" +
			"     30  iconst_5\n" +
			"     31  if_icmple 61\n" +
			"     34  iload_0 [i]\n" +
			"     35  istore_2\n" +
			"     36  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     39  ldc <String \"[finally]\"> [47]\n" +
			"     41  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"     44  invokestatic X.throwRuntime() : void [49]\n" +
			"     47  goto 56\n" +
			"     50  astore_3\n" +
			"     51  invokestatic X.clean() : void [52]\n" +
			"     54  aload_3\n" +
			"     55  athrow\n" +
			"     56  invokestatic X.clean() : void [52]\n" +
			"     59  iload_2\n" +
			"     60  ireturn\n" +
			"     61  iload_0 [i]\n" +
			"     62  ineg\n" +
			"     63  istore_2\n" +
			"     64  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     67  ldc <String \"[finally]\"> [47]\n" +
			"     69  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"     72  invokestatic X.throwRuntime() : void [49]\n" +
			"     75  goto 84\n" +
			"     78  astore_3\n" +
			"     79  invokestatic X.clean() : void [52]\n" +
			"     82  aload_3\n" +
			"     83  athrow\n" +
			"     84  invokestatic X.clean() : void [52]\n" +
			"     87  iload_2\n" +
			"     88  ireturn\n" +
			"     89  astore_0 [e]\n" +
			"     90  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     93  ldc <String \"[WRONG CATCH]\"> [55]\n" +
			"     95  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"     98  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    101  ldc <String \"[finally]\"> [47]\n" +
			"    103  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"    106  invokestatic X.throwRuntime() : void [49]\n" +
			"    109  goto 118\n" +
			"    112  astore_3\n" +
			"    113  invokestatic X.clean() : void [52]\n" +
			"    116  aload_3\n" +
			"    117  athrow\n" +
			"    118  invokestatic X.clean() : void [52]\n" +
			"    121  iconst_2\n" +
			"    122  ireturn\n" +
			"    123  astore_1\n" +
			"    124  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    127  ldc <String \"[finally]\"> [47]\n" +
			"    129  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"    132  invokestatic X.throwRuntime() : void [49]\n" +
			"    135  goto 144\n" +
			"    138  astore_3\n" +
			"    139  invokestatic X.clean() : void [52]\n" +
			"    142  aload_3\n" +
			"    143  athrow\n" +
			"    144  invokestatic X.clean() : void [52]\n" +
			"    147  aload_1\n" +
			"    148  athrow\n" +
			"      Exception Table:\n" +
			"        [pc: 44, pc: 50] -> 50 when : any\n" +
			"        [pc: 72, pc: 78] -> 78 when : any\n" +
			"        [pc: 0, pc: 36] -> 89 when : java.lang.Exception\n" +
			"        [pc: 61, pc: 64] -> 89 when : java.lang.Exception\n" +
			"        [pc: 106, pc: 112] -> 112 when : any\n" +
			"        [pc: 0, pc: 36] -> 123 when : any\n" +
			"        [pc: 61, pc: 64] -> 123 when : any\n" +
			"        [pc: 89, pc: 98] -> 123 when : any\n" +
			"        [pc: 132, pc: 138] -> 138 when : any\n"
	: 		"  // Method descriptor #15 ()I\n" +
			"  // Stack: 4, Locals: 6\n" +
			"  private static int scenario();\n" +
			"      0  iconst_1\n" +
			"      1  istore_0 [i]\n" +
			"      2  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"      5  new java.lang.StringBuffer [22]\n" +
			"      8  dup\n" +
			"      9  ldc <String \"[i: \"> [24]\n" +
			"     11  invokespecial java.lang.StringBuffer(java.lang.String) [26]\n" +
			"     14  iload_0 [i]\n" +
			"     15  invokevirtual java.lang.StringBuffer.append(int) : java.lang.StringBuffer [29]\n" +
			"     18  ldc <String \"]\"> [33]\n" +
			"     20  invokevirtual java.lang.StringBuffer.append(java.lang.String) : java.lang.StringBuffer [35]\n" +
			"     23  invokevirtual java.lang.StringBuffer.toString() : java.lang.String [38]\n" +
			"     26  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"     29  iload_0 [i]\n" +
			"     30  iconst_5\n" +
			"     31  if_icmple 41\n" +
			"     34  iload_0 [i]\n" +
			"     35  istore_3\n" +
			"     36  jsr 69\n" +
			"     39  iload_3\n" +
			"     40  ireturn\n" +
			"     41  iload_0 [i]\n" +
			"     42  ineg\n" +
			"     43  istore_3\n" +
			"     44  jsr 69\n" +
			"     47  iload_3\n" +
			"     48  ireturn\n" +
			"     49  astore_0 [e]\n" +
			"     50  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     53  ldc <String \"[WRONG CATCH]\"> [47]\n" +
			"     55  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"     58  jsr 69\n" +
			"     61  iconst_2\n" +
			"     62  ireturn\n" +
			"     63  astore_2\n" +
			"     64  jsr 69\n" +
			"     67  aload_2\n" +
			"     68  athrow\n" +
			"     69  astore_1\n" +
			"     70  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     73  ldc <String \"[finally]\"> [49]\n" +
			"     75  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"     78  invokestatic X.throwRuntime() : void [51]\n" +
			"     81  goto 99\n" +
			"     84  astore 5\n" +
			"     86  jsr 92\n" +
			"     89  aload 5\n" +
			"     91  athrow\n" +
			"     92  astore 4\n" +
			"     94  invokestatic X.clean() : void [54]\n" +
			"     97  ret 4\n" +
			"     99  jsr 92\n" +
			"    102  ret 1\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 39] -> 49 when : java.lang.Exception\n" +
			"        [pc: 41, pc: 47] -> 49 when : java.lang.Exception\n" +
			"        [pc: 0, pc: 39] -> 63 when : any\n" +
			"        [pc: 41, pc: 47] -> 63 when : any\n" +
			"        [pc: 49, pc: 61] -> 63 when : any\n" +
			"        [pc: 78, pc: 84] -> 84 when : any\n" +
			"        [pc: 99, pc: 102] -> 84 when : any\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=124853 - variation
public void test035() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			new X().bar();\n" +
				"		} catch(Exception e){\n" +
				"			System.out.println(\"[end]\");\n" +
				"		}\n" +
				"	}\n" +
				"	Object bar() {\n" +
				"		try {\n" +
				"			System.out.print(\"[try]\");\n" +
				"			return this;\n" +
				"		} catch(Exception e){\n" +
				"			System.out.print(\"[WRONG CATCH]\");\n" +
				"		} finally {\n" +
				"			System.out.print(\"[finally]\");\n" +
				"			foo();\n" +
				"		}\n" +
				"		return this;\n" +
				"	}\n" +
				"	Object foo() {\n" +
				"		throw new RuntimeException();\n" +
				"	}\n" +
				"}\n",
			},
			"[try][finally][end]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=124853 - variation
public void test036() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			new X().bar();\n" +
				"		} catch(Exception e){\n" +
				"			System.out.println(\"[end]\");\n" +
				"		}\n" +
				"	}\n" +
				"	Object bar() {\n" +
				"		try {\n" +
				"			System.out.print(\"[try]\");\n" +
				"			throw new RuntimeException();\n" +
				"		} catch(Exception e){\n" +
				"			System.out.print(\"[catch]\");\n" +
				"			return this;\n" +
				"		} finally {\n" +
				"			System.out.print(\"[finally]\");\n" +
				"			foo();\n" +
				"		}\n" +
				"	}\n" +
				"	Object foo() {\n" +
				"		throw new RuntimeException();\n" +
				"	}\n" +
				"}\n",
			},
			"[try][catch][finally][end]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=124853 - variation
public void test037() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			scenario();\n" +
				"		} catch(Exception e){\n" +
				"			System.out.println(\"[end]\");\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	private static void scenario() throws Exception {\n" +
				"		try {\n" +
				"			System.out.print(\"[try1]\");\n" +
				"			try {\n" +
				"				System.out.print(\"[try2]\");\n" +
				"				return;\n" +
				"			} catch(Exception e) {\n" +
				"				System.out.print(\"[catch2]\");\n" +
				"			} finally {\n" +
				"				System.out.print(\"[finally2]\");\n" +
				"				throwRuntime();\n" +
				"			}\n" +
				"		} catch(Exception e) {\n" +
				"			System.out.print(\"[catch1]\");\n" +
				"			throw e;\n" +
				"		} finally {\n" +
				"			System.out.print(\"[finally1]\");\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	private static void throwRuntime() {\n" +
				"		throw new RuntimeException(\"error\");\n" +
				"	}\n" +
				"}\n",
			},
			"[try1][try2][finally2][catch1][finally1][end]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=87423
public void test038() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	int hasLoop() {\n" +
				"		int l, m, n;\n" +
				"		for (m = 0; m < 10; m++) {\n" +
				"			n = 2;\n" +
				"			try {\n" +
				"				n = 3;\n" +
				"				try {\n" +
				"					n = 4;\n" +
				"				} catch (ArithmeticException e1) {\n" +
				"					n = 11;\n" +
				"				} finally {\n" +
				"					for (l = 0; l < 10; l++) {\n" +
				"						n++;\n" +
				"					}\n" +
				"					if (n == 12) {\n" +
				"						n = 13;\n" +
				"						break;\n" +
				"					}\n" +
				"					n = 15;\n" +
				"				}\n" +
				"			} catch (OutOfMemoryError e2) {\n" +
				"				n = 18;\n" +
				"			}\n" +
				"		}\n" +
				"		return 0;\n" +
				"	}\n" +
				"\n" +
				"	public static void main(String args[]) {\n" +
				"      System.out.println(\"Loaded fine\");\n" +
				"   }\n" +
				"}\n",
			},
			"Loaded fine");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127603
public void test039() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void someMethod() {\n" +
				"		int count = 0;\n" +
				"		int code = -1;\n" +
				"		while (count < 2 && (code == -1 || code == 2)) {\n" +
				"			count++;\n" +
				"			try {\n" +
				"				{\n" +
				"					System.out.print(\"[Try:\" + count + \";\" + code+\"]\");\n" +
				"				}\n" +
				"				code = 0;\n" +
				"\n" +
				"			} finally {\n" +
				"				System.out.print(\"[Finally\" + count + \";\" + code+\"]\");\n" +
				"			}\n" +
				"		}\n" +
				"		System.out.print(\"[Outering\");\n" +
				"\n" +
				"		if (code == 0) {\n" +
				"			System.out.print(\"[Return:\" + count + \";\" + code+\"]\");\n" +
				"			return;\n" +
				"		}\n" +
				"		throw new RuntimeException(null + \"a\");\n" +
				"	}\n" +
				"\n" +
				"	public static void main(String[] args) throws Exception {\n" +
				"		for (int i = 0; i < 1; i++) {\n" +
				"			someMethod();\n" +
				"			System.out.println();\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"[Try:1;-1][Finally1;0][Outering[Return:1;0]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=128705
public void test040() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo(boolean b) {\n" +
				"		try { \n" +
				"			if (b){ \n" +
				"				int i = 0;\n" +
				"				return;\n" +
				"			} else {\n" +
				"				Object o = null;\n" +
				"				return;\n" +
				"			}\n" +
				"		} finally {\n" +
				"			System.out.println(\"done\");\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(true);\n" +
				"	}\n" +
				"}\n",
			},
			"done");

	CompilerOptions compilerOptions = new CompilerOptions(getCompilerOptions());
	String expectedOutput = !compilerOptions.inlineJsrBytecode
		?	"  // Method descriptor #15 (Z)V\n" +
			"  // Stack: 2, Locals: 5\n" +
			"  public void foo(boolean b);\n" +
			"     0  iload_1 [b]\n" +
			"     1  ifeq 10\n" +
			"     4  iconst_0\n" +
			"     5  istore_2 [i]\n" +
			"     6  jsr 23\n" +
			"     9  return\n" +
			"    10  aconst_null\n" +
			"    11  astore_2 [o]\n" +
			"    12  goto 6\n" +
			"    15  astore 4\n" +
			"    17  jsr 23\n" +
			"    20  aload 4\n" +
			"    22  athrow\n" +
			"    23  astore_3\n" +
			"    24  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    27  ldc <String \"done\"> [22]\n" +
			"    29  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
			"    32  ret 3\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 9] -> 15 when : any\n" +
			"        [pc: 10, pc: 15] -> 15 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"        [pc: 4, line: 5]\n" +
			"        [pc: 6, line: 6]\n" +
			"        [pc: 10, line: 8]\n" +
			"        [pc: 12, line: 9]\n" +
			"        [pc: 15, line: 11]\n" +
			"        [pc: 20, line: 13]\n" +
			"        [pc: 23, line: 11]\n" +
			"        [pc: 24, line: 12]\n" +
			"        [pc: 32, line: 13]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 34] local: this index: 0 type: X\n" +
			"        [pc: 0, pc: 34] local: b index: 1 type: boolean\n" +
			"        [pc: 6, pc: 10] local: i index: 2 type: int\n" +
			"        [pc: 12, pc: 15] local: o index: 2 type: java.lang.Object\n"
	: 		null;
	if (expectedOutput == null) {
		if (compilerOptions.targetJDK == ClassFileConstants.JDK1_5) {
			expectedOutput = "  // Method descriptor #15 (Z)V\n" +
			"  // Stack: 2, Locals: 4\n" +
			"  public void foo(boolean b);\n" +
			"     0  iload_1 [b]\n" +
			"     1  ifeq 15\n" +
			"     4  iconst_0\n" +
			"     5  istore_2 [i]\n" +
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     9  ldc <String \"done\"> [22]\n" +
			"    11  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
			"    14  return\n" +
			"    15  aconst_null\n" +
			"    16  astore_2 [o]\n" +
			"    17  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    20  ldc <String \"done\"> [22]\n" +
			"    22  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
			"    25  return\n" +
			"    26  astore_3\n" +
			"    27  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    30  ldc <String \"done\"> [22]\n" +
			"    32  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
			"    35  aload_3\n" +
			"    36  athrow\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 6] -> 26 when : any\n" +
			"        [pc: 15, pc: 17] -> 26 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"        [pc: 4, line: 5]\n" +
			"        [pc: 6, line: 12]\n" +
			"        [pc: 14, line: 6]\n" +
			"        [pc: 15, line: 8]\n" +
			"        [pc: 17, line: 12]\n" +
			"        [pc: 25, line: 9]\n" +
			"        [pc: 26, line: 11]\n" +
			"        [pc: 27, line: 12]\n" +
			"        [pc: 35, line: 13]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 37] local: this index: 0 type: X\n" +
			"        [pc: 0, pc: 37] local: b index: 1 type: boolean\n" +
			"        [pc: 6, pc: 15] local: i index: 2 type: int\n" +
			"        [pc: 17, pc: 26] local: o index: 2 type: java.lang.Object\n";
		} else {
			expectedOutput = "  // Method descriptor #15 (Z)V\n" +
			"  // Stack: 2, Locals: 4\n" +
			"  public void foo(boolean b);\n" +
			"     0  iload_1 [b]\n" +
			"     1  ifeq 15\n" +
			"     4  iconst_0\n" +
			"     5  istore_2 [i]\n" +
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     9  ldc <String \"done\"> [22]\n" +
			"    11  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
			"    14  return\n" +
			"    15  aconst_null\n" +
			"    16  astore_2 [o]\n" +
			"    17  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    20  ldc <String \"done\"> [22]\n" +
			"    22  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
			"    25  return\n" +
			"    26  astore_3\n" +
			"    27  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    30  ldc <String \"done\"> [22]\n" +
			"    32  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
			"    35  aload_3\n" +
			"    36  athrow\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 6] -> 26 when : any\n" +
			"        [pc: 15, pc: 17] -> 26 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"        [pc: 4, line: 5]\n" +
			"        [pc: 6, line: 12]\n" +
			"        [pc: 14, line: 6]\n" +
			"        [pc: 15, line: 8]\n" +
			"        [pc: 17, line: 12]\n" +
			"        [pc: 25, line: 9]\n" +
			"        [pc: 26, line: 11]\n" +
			"        [pc: 27, line: 12]\n" +
			"        [pc: 35, line: 13]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 37] local: this index: 0 type: X\n" +
			"        [pc: 0, pc: 37] local: b index: 1 type: boolean\n" +
			"        [pc: 6, pc: 15] local: i index: 2 type: int\n" +
			"        [pc: 17, pc: 26] local: o index: 2 type: java.lang.Object\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 15, same]\n" +
			"        [pc: 26, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n";
		}
	}

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=128705 - variation
public void test041() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo(boolean b) {\n" +
				"		try {\n" +
				"			int i = 0;\n" +
				"			return;\n" +
				"		} catch(Exception e) {\n" +
				"			return;\n" +
				"		} finally {\n" +
				"			System.out.println(\"done\");\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(true);\n" +
				"	}\n" +
				"}\n",
			},
			"done");

	CompilerOptions compilerOptions = new CompilerOptions(getCompilerOptions());
	String expectedOutput = !compilerOptions.inlineJsrBytecode
		?	"  // Method descriptor #15 (Z)V\n" +
			"  // Stack: 2, Locals: 5\n" +
			"  public void foo(boolean b);\n" +
			"     0  iconst_0\n" +
			"     1  istore_2 [i]\n" +
			"     2  jsr 18\n" +
			"     5  return\n" +
			"     6  astore_2 [e]\n" +
			"     7  goto 2\n" +
			"    10  astore 4\n" +
			"    12  jsr 18\n" +
			"    15  aload 4\n" +
			"    17  athrow\n" +
			"    18  astore_3\n" +
			"    19  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    22  ldc <String \"done\"> [22]\n" +
			"    24  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
			"    27  ret 3\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 5] -> 6 when : java.lang.Exception\n" +
			"        [pc: 0, pc: 5] -> 10 when : any\n" +
			"        [pc: 6, pc: 10] -> 10 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"        [pc: 2, line: 5]\n" +
			"        [pc: 6, line: 6]\n" +
			"        [pc: 7, line: 7]\n" +
			"        [pc: 10, line: 8]\n" +
			"        [pc: 15, line: 10]\n" +
			"        [pc: 18, line: 8]\n" +
			"        [pc: 19, line: 9]\n" +
			"        [pc: 27, line: 10]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 29] local: this index: 0 type: X\n" +
			"        [pc: 0, pc: 29] local: b index: 1 type: boolean\n" +
			"        [pc: 2, pc: 6] local: i index: 2 type: int\n" +
			"        [pc: 7, pc: 10] local: e index: 2 type: java.lang.Exception\n"
		: null;
	if (expectedOutput == null) {
		if (compilerOptions.targetJDK == ClassFileConstants.JDK1_5) {
			expectedOutput = "  // Method descriptor #15 (Z)V\n" +
			"  // Stack: 2, Locals: 4\n" +
			"  public void foo(boolean b);\n" +
			"     0  iconst_0\n" +
			"     1  istore_2 [i]\n" +
			"     2  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     5  ldc <String \"done\"> [22]\n" +
			"     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
			"    10  return\n" +
			"    11  astore_2 [e]\n" +
			"    12  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    15  ldc <String \"done\"> [22]\n" +
			"    17  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
			"    20  return\n" +
			"    21  astore_3\n" +
			"    22  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    25  ldc <String \"done\"> [22]\n" +
			"    27  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
			"    30  aload_3\n" +
			"    31  athrow\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 2] -> 11 when : java.lang.Exception\n" +
			"        [pc: 0, pc: 2] -> 21 when : any\n" +
			"        [pc: 11, pc: 12] -> 21 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"        [pc: 2, line: 9]\n" +
			"        [pc: 10, line: 5]\n" +
			"        [pc: 11, line: 6]\n" +
			"        [pc: 12, line: 9]\n" +
			"        [pc: 20, line: 7]\n" +
			"        [pc: 21, line: 8]\n" +
			"        [pc: 22, line: 9]\n" +
			"        [pc: 30, line: 10]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 32] local: this index: 0 type: X\n" +
			"        [pc: 0, pc: 32] local: b index: 1 type: boolean\n" +
			"        [pc: 2, pc: 11] local: i index: 2 type: int\n" +
			"        [pc: 12, pc: 21] local: e index: 2 type: java.lang.Exception\n";
		} else {
			expectedOutput = "  // Method descriptor #15 (Z)V\n" +
			"  // Stack: 2, Locals: 4\n" +
			"  public void foo(boolean b);\n" +
			"     0  iconst_0\n" +
			"     1  istore_2 [i]\n" +
			"     2  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     5  ldc <String \"done\"> [22]\n" +
			"     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
			"    10  return\n" +
			"    11  astore_2 [e]\n" +
			"    12  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    15  ldc <String \"done\"> [22]\n" +
			"    17  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
			"    20  return\n" +
			"    21  astore_3\n" +
			"    22  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    25  ldc <String \"done\"> [22]\n" +
			"    27  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
			"    30  aload_3\n" +
			"    31  athrow\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 2] -> 11 when : java.lang.Exception\n" +
			"        [pc: 0, pc: 2] -> 21 when : any\n" +
			"        [pc: 11, pc: 12] -> 21 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"        [pc: 2, line: 9]\n" +
			"        [pc: 10, line: 5]\n" +
			"        [pc: 11, line: 6]\n" +
			"        [pc: 12, line: 9]\n" +
			"        [pc: 20, line: 7]\n" +
			"        [pc: 21, line: 8]\n" +
			"        [pc: 22, line: 9]\n" +
			"        [pc: 30, line: 10]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 32] local: this index: 0 type: X\n" +
			"        [pc: 0, pc: 32] local: b index: 1 type: boolean\n" +
			"        [pc: 2, pc: 11] local: i index: 2 type: int\n" +
			"        [pc: 12, pc: 21] local: e index: 2 type: java.lang.Exception\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 11, same_locals_1_stack_item, stack: {java.lang.Exception}]\n" +
			"        [pc: 21, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n";
		}
	}

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=128705 - variation
public void test042() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				" public class X {\n" +
				" public static void main(String[] args) {\n" +
				"		System.out.println(new X().foo(args));\n" +
				"	}\n" +
				"	String foo(String[] args) {\n" +
				"		try {\n" +
				"			if (args == null) return \"KO\";\n" +
				"			switch(args.length) {\n" +
				"			case 0:\n" +
				"				return \"OK\";\n" +
				"			case 1:\n" +
				"				return \"KO\";\n" +
				"			case 3:\n" +
				"				return \"OK\";\n" +
				"			default:\n" +
				"				return \"KO\";\n" +
				"			}\n" +
				"		} finally {\n" +
				"			System.out.print(\"FINALLY:\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"FINALLY:OK");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).inlineJsrBytecode
		?	"  // Method descriptor #26 ([Ljava/lang/String;)Ljava/lang/String;\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  java.lang.String foo(java.lang.String[] args);\n" +
			"     0  aload_1 [args]\n" +
			"     1  ifnonnull 15\n" +
			"     4  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     7  ldc <String \"FINALLY:\"> [35]\n" +
			"     9  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]\n" +
			"    12  ldc <String \"KO\"> [40]\n" +
			"    14  areturn\n" +
			"    15  aload_1 [args]\n" +
			"    16  arraylength\n" +
			"    17  tableswitch default: 65\n" +
			"          case 0: 48\n" +
			"          case 1: 59\n" +
			"          case 2: 65\n" +
			"          case 3: 62\n" +
			"    48  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    51  ldc <String \"FINALLY:\"> [35]\n" +
			"    53  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]\n" +
			"    56  ldc <String \"OK\"> [42]\n" +
			"    58  areturn\n" +
			"    59  goto 4\n" +
			"    62  goto 48\n" +
			"    65  goto 4\n" +
			"    68  astore_2\n" +
			"    69  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    72  ldc <String \"FINALLY:\"> [35]\n" +
			"    74  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]\n" +
			"    77  aload_2\n" +
			"    78  athrow\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 4] -> 68 when : any\n" +
			"        [pc: 15, pc: 48] -> 68 when : any\n" +
			"        [pc: 59, pc: 68] -> 68 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 7]\n" +
			"        [pc: 4, line: 19]\n" +
			"        [pc: 12, line: 7]\n" +
			"        [pc: 15, line: 8]\n" +
			"        [pc: 48, line: 19]\n" +
			"        [pc: 56, line: 10]\n" +
			"        [pc: 59, line: 12]\n" +
			"        [pc: 62, line: 14]\n" +
			"        [pc: 65, line: 16]\n" +
			"        [pc: 68, line: 18]\n" +
			"        [pc: 69, line: 19]\n" +
			"        [pc: 77, line: 20]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 79] local: this index: 0 type: X\n" +
			"        [pc: 0, pc: 79] local: args index: 1 type: java.lang.String[]\n"
	: 		"  // Method descriptor #26 ([Ljava/lang/String;)Ljava/lang/String;\n" +
			"  // Stack: 2, Locals: 4\n" +
			"  java.lang.String foo(java.lang.String[] args);\n" +
			"     0  aload_1 [args]\n" +
			"     1  ifnonnull 10\n" +
			"     4  jsr 65\n" +
			"     7  ldc <String \"KO\"> [35]\n" +
			"     9  areturn\n" +
			"    10  aload_1 [args]\n" +
			"    11  arraylength\n" +
			"    12  tableswitch default: 56\n" +
			"          case 0: 44\n" +
			"          case 1: 50\n" +
			"          case 2: 56\n" +
			"          case 3: 53\n" +
			"    44  jsr 65\n" +
			"    47  ldc <String \"OK\"> [37]\n" +
			"    49  areturn\n" +
			"    50  goto 4\n" +
			"    53  goto 44\n" +
			"    56  goto 4\n" +
			"    59  astore_3\n" +
			"    60  jsr 65\n" +
			"    63  aload_3\n" +
			"    64  athrow\n" +
			"    65  astore_2\n" +
			"    66  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    69  ldc <String \"FINALLY:\"> [39]\n" +
			"    71  invokevirtual java.io.PrintStream.print(java.lang.String) : void [41]\n" +
			"    74  ret 2\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 7] -> 59 when : any\n" +
			"        [pc: 10, pc: 47] -> 59 when : any\n" +
			"        [pc: 50, pc: 59] -> 59 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 7]\n" +
			"        [pc: 10, line: 8]\n" +
			"        [pc: 44, line: 10]\n" +
			"        [pc: 50, line: 12]\n" +
			"        [pc: 53, line: 14]\n" +
			"        [pc: 56, line: 16]\n" +
			"        [pc: 59, line: 18]\n" +
			"        [pc: 63, line: 20]\n" +
			"        [pc: 65, line: 18]\n" +
			"        [pc: 66, line: 19]\n" +
			"        [pc: 74, line: 20]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 76] local: this index: 0 type: X\n" +
			"        [pc: 0, pc: 76] local: args index: 1 type: java.lang.String[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=404146 - variation without sharing of inlined escaping finally-blocks
public void test042_not_shared() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ShareCommonFinallyBlocks, CompilerOptions.DISABLED);
	customOptions.put(CompilerOptions.OPTION_InlineJsr, CompilerOptions.ENABLED);

	this.runConformTest(
			new String[] {
				"X.java",
				" public class X {\n" +
				" public static void main(String[] args) {\n" +
				"		System.out.println(new X().foo(args));\n" +
				"	}\n" +
				"	String foo(String[] args) {\n" +
				"		try {\n" +
				"			if (args == null) return \"KO\";\n" +
				"			switch(args.length) {\n" +
				"			case 0:\n" +
				"				return \"OK\";\n" +
				"			case 1:\n" +
				"				return \"KO\";\n" +
				"			case 3:\n" +
				"				return \"OK\";\n" +
				"			default:\n" +
				"				return \"KO\";\n" +
				"			}\n" +
				"		} finally {\n" +
				"			System.out.print(\"FINALLY:\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"FINALLY:OK",
			null,
			true,
			null,
			customOptions,
			null);

	String expectedOutput =
			"  // Method descriptor #26 ([Ljava/lang/String;)Ljava/lang/String;\n" + 
			"  // Stack: 2, Locals: 3\n" + 
			"  java.lang.String foo(java.lang.String[] args);\n" + 
			"      0  aload_1 [args]\n" + 
			"      1  ifnonnull 15\n" + 
			"      4  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"      7  ldc <String \"FINALLY:\"> [35]\n" + 
			"      9  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]\n" + 
			"     12  ldc <String \"KO\"> [40]\n" + 
			"     14  areturn\n" + 
			"     15  aload_1 [args]\n" + 
			"     16  arraylength\n" + 
			"     17  tableswitch default: 81\n" + 
			"          case 0: 48\n" + 
			"          case 1: 59\n" + 
			"          case 2: 81\n" + 
			"          case 3: 70\n" + 
			"     48  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     51  ldc <String \"FINALLY:\"> [35]\n" + 
			"     53  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]\n" + 
			"     56  ldc <String \"OK\"> [42]\n" + 
			"     58  areturn\n" + 
			"     59  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     62  ldc <String \"FINALLY:\"> [35]\n" + 
			"     64  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]\n" + 
			"     67  ldc <String \"KO\"> [40]\n" + 
			"     69  areturn\n" + 
			"     70  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     73  ldc <String \"FINALLY:\"> [35]\n" + 
			"     75  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]\n" + 
			"     78  ldc <String \"OK\"> [42]\n" + 
			"     80  areturn\n" + 
			"     81  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     84  ldc <String \"FINALLY:\"> [35]\n" + 
			"     86  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]\n" + 
			"     89  ldc <String \"KO\"> [40]\n" + 
			"     91  areturn\n" + 
			"     92  astore_2\n" + 
			"     93  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     96  ldc <String \"FINALLY:\"> [35]\n" + 
			"     98  invokevirtual java.io.PrintStream.print(java.lang.String) : void [37]\n" + 
			"    101  aload_2\n" + 
			"    102  athrow\n" + 
			"      Exception Table:\n" + 
			"        [pc: 0, pc: 4] -> 92 when : any\n" + 
			"        [pc: 15, pc: 48] -> 92 when : any\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 7]\n" + 
			"        [pc: 4, line: 19]\n" + 
			"        [pc: 12, line: 7]\n" + 
			"        [pc: 15, line: 8]\n" + 
			"        [pc: 48, line: 19]\n" + 
			"        [pc: 56, line: 10]\n" + 
			"        [pc: 59, line: 19]\n" + 
			"        [pc: 67, line: 12]\n" + 
			"        [pc: 70, line: 19]\n" + 
			"        [pc: 78, line: 14]\n" + 
			"        [pc: 81, line: 19]\n" + 
			"        [pc: 89, line: 16]\n" + 
			"        [pc: 92, line: 18]\n" + 
			"        [pc: 93, line: 19]\n" + 
			"        [pc: 101, line: 20]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 103] local: this index: 0 type: X\n" + 
			"        [pc: 0, pc: 103] local: args index: 1 type: java.lang.String[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}



//https://bugs.eclipse.org/bugs/show_bug.cgi?id=128705 - variation
public void test043() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void save() {\n" +
				"		int a = 3;\n" +
				"		try {\n" +
				"			Object warnings = null;\n" +
				"			Object contexts = null;\n" +
				"			try {\n" +
				"				System.out.print(warnings);\n" +
				"				return;\n" +
				"			} catch (NullPointerException npe) {\n" +
				"				System.out.print(contexts);\n" +
				"				return;\n" +
				"			} finally {\n" +
				"				System.out.print(\"#inner -> \" + a);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			return;\n" +
				"		} finally {\n" +
				"			int var = 0;\n" +
				"			System.out.println(\"#save -> \" + a);\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().save();\n" +
				"	}\n" +
				"}\n",
			},
			"null#inner -> 3#save -> 3");

	CompilerOptions compilerOptions = new CompilerOptions(getCompilerOptions());
	String expectedOutput = !compilerOptions.inlineJsrBytecode
		?	"  // Method descriptor #6 ()V\n" +
			"  // Stack: 4, Locals: 10\n" +
			"  public void save();\n" +
			"      0  iconst_3\n" +
			"      1  istore_1 [a]\n" +
			"      2  aconst_null\n" +
			"      3  astore_2 [warnings]\n" +
			"      4  aconst_null\n" +
			"      5  astore_3 [contexts]\n" +
			"      6  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"      9  aload_2 [warnings]\n" +
			"     10  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [21]\n" +
			"     13  jsr 40\n" +
			"     16  jsr 78\n" +
			"     19  return\n" +
			"     20  astore 4 [npe]\n" +
			"     22  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     25  aload_3 [contexts]\n" +
			"     26  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [21]\n" +
			"     29  goto 13\n" +
			"     32  astore 6\n" +
			"     34  jsr 40\n" +
			"     37  aload 6\n" +
			"     39  athrow\n" +
			"     40  astore 5\n" +
			"     42  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     45  new java.lang.StringBuffer [27]\n" +
			"     48  dup\n" +
			"     49  ldc <String \"#inner -> \"> [29]\n" +
			"     51  invokespecial java.lang.StringBuffer(java.lang.String) [31]\n" +
			"     54  iload_1 [a]\n" +
			"     55  invokevirtual java.lang.StringBuffer.append(int) : java.lang.StringBuffer [34]\n" +
			"     58  invokevirtual java.lang.StringBuffer.toString() : java.lang.String [38]\n" +
			"     61  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"     64  ret 5\n" +
			"     66  astore_2 [e]\n" +
			"     67  goto 16\n" +
			"     70  astore 8\n" +
			"     72  jsr 78\n" +
			"     75  aload 8\n" +
			"     77  athrow\n" +
			"     78  astore 7\n" +
			"     80  iconst_0\n" +
			"     81  istore 9 [var]\n" +
			"     83  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     86  new java.lang.StringBuffer [27]\n" +
			"     89  dup\n" +
			"     90  ldc <String \"#save -> \"> [44]\n" +
			"     92  invokespecial java.lang.StringBuffer(java.lang.String) [31]\n" +
			"     95  iload_1 [a]\n" +
			"     96  invokevirtual java.lang.StringBuffer.append(int) : java.lang.StringBuffer [34]\n" +
			"     99  invokevirtual java.lang.StringBuffer.toString() : java.lang.String [38]\n" +
			"    102  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]\n" +
			"    105  ret 7\n" +
			"      Exception Table:\n" +
			"        [pc: 6, pc: 16] -> 20 when : java.lang.NullPointerException\n" +
			"        [pc: 6, pc: 16] -> 32 when : any\n" +
			"        [pc: 20, pc: 32] -> 32 when : any\n" +
			"        [pc: 2, pc: 19] -> 66 when : java.lang.Exception\n" +
			"        [pc: 20, pc: 66] -> 66 when : java.lang.Exception\n" +
			"        [pc: 2, pc: 19] -> 70 when : any\n" +
			"        [pc: 20, pc: 70] -> 70 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 5]\n" +
			"        [pc: 4, line: 6]\n" +
			"        [pc: 6, line: 8]\n" +
			"        [pc: 13, line: 9]\n" +
			"        [pc: 20, line: 10]\n" +
			"        [pc: 22, line: 11]\n" +
			"        [pc: 29, line: 12]\n" +
			"        [pc: 32, line: 13]\n" +
			"        [pc: 37, line: 15]\n" +
			"        [pc: 40, line: 13]\n" +
			"        [pc: 42, line: 14]\n" +
			"        [pc: 64, line: 15]\n" +
			"        [pc: 66, line: 16]\n" +
			"        [pc: 67, line: 17]\n" +
			"        [pc: 70, line: 18]\n" +
			"        [pc: 75, line: 21]\n" +
			"        [pc: 78, line: 18]\n" +
			"        [pc: 80, line: 19]\n" +
			"        [pc: 83, line: 20]\n" +
			"        [pc: 105, line: 21]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 107] local: this index: 0 type: X\n" +
			"        [pc: 2, pc: 107] local: a index: 1 type: int\n" +
			"        [pc: 4, pc: 66] local: warnings index: 2 type: java.lang.Object\n" +
			"        [pc: 6, pc: 66] local: contexts index: 3 type: java.lang.Object\n" +
			"        [pc: 22, pc: 32] local: npe index: 4 type: java.lang.NullPointerException\n" +
			"        [pc: 67, pc: 70] local: e index: 2 type: java.lang.Exception\n" +
			"        [pc: 83, pc: 105] local: var index: 9 type: int\n"
		: null;

	if (expectedOutput == null) {
		if (compilerOptions.targetJDK == ClassFileConstants.JDK1_5) {
			expectedOutput = "  // Method descriptor #6 ()V\n" +
			"  // Stack: 4, Locals: 8\n" +
			"  public void save();\n" +
			"      0  iconst_3\n" +
			"      1  istore_1 [a]\n" +
			"      2  aconst_null\n" +
			"      3  astore_2 [warnings]\n" +
			"      4  aconst_null\n" +
			"      5  astore_3 [contexts]\n" +
			"      6  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"      9  aload_2 [warnings]\n" +
			"     10  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [21]\n" +
			"     13  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     16  new java.lang.StringBuilder [27]\n" +
			"     19  dup\n" +
			"     20  ldc <String \"#inner -> \"> [29]\n" +
			"     22  invokespecial java.lang.StringBuilder(java.lang.String) [31]\n" +
			"     25  iload_1 [a]\n" +
			"     26  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]\n" +
			"     29  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]\n" +
			"     32  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"     35  iconst_0\n" +
			"     36  istore 7 [var]\n" +
			"     38  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     41  new java.lang.StringBuilder [27]\n" +
			"     44  dup\n" +
			"     45  ldc <String \"#save -> \"> [44]\n" +
			"     47  invokespecial java.lang.StringBuilder(java.lang.String) [31]\n" +
			"     50  iload_1 [a]\n" +
			"     51  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]\n" +
			"     54  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]\n" +
			"     57  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]\n" +
			"     60  return\n" +
			"     61  astore 4 [npe]\n" +
			"     63  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     66  aload_3 [contexts]\n" +
			"     67  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [21]\n" +
			"     70  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     73  new java.lang.StringBuilder [27]\n" +
			"     76  dup\n" +
			"     77  ldc <String \"#inner -> \"> [29]\n" +
			"     79  invokespecial java.lang.StringBuilder(java.lang.String) [31]\n" +
			"     82  iload_1 [a]\n" +
			"     83  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]\n" +
			"     86  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]\n" +
			"     89  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"     92  iconst_0\n" +
			"     93  istore 7 [var]\n" +
			"     95  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     98  new java.lang.StringBuilder [27]\n" +
			"    101  dup\n" +
			"    102  ldc <String \"#save -> \"> [44]\n" +
			"    104  invokespecial java.lang.StringBuilder(java.lang.String) [31]\n" +
			"    107  iload_1 [a]\n" +
			"    108  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]\n" +
			"    111  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]\n" +
			"    114  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]\n" +
			"    117  return\n" +
			"    118  astore 5\n" +
			"    120  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"    123  new java.lang.StringBuilder [27]\n" +
			"    126  dup\n" +
			"    127  ldc <String \"#inner -> \"> [29]\n" +
			"    129  invokespecial java.lang.StringBuilder(java.lang.String) [31]\n" +
			"    132  iload_1 [a]\n" +
			"    133  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]\n" +
			"    136  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]\n" +
			"    139  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"    142  aload 5\n" +
			"    144  athrow\n" +
			"    145  astore_2 [e]\n" +
			"    146  iconst_0\n" +
			"    147  istore 7 [var]\n" +
			"    149  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"    152  new java.lang.StringBuilder [27]\n" +
			"    155  dup\n" +
			"    156  ldc <String \"#save -> \"> [44]\n" +
			"    158  invokespecial java.lang.StringBuilder(java.lang.String) [31]\n" +
			"    161  iload_1 [a]\n" +
			"    162  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]\n" +
			"    165  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]\n" +
			"    168  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]\n" +
			"    171  return\n" +
			"    172  astore 6\n" +
			"    174  iconst_0\n" +
			"    175  istore 7 [var]\n" +
			"    177  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"    180  new java.lang.StringBuilder [27]\n" +
			"    183  dup\n" +
			"    184  ldc <String \"#save -> \"> [44]\n" +
			"    186  invokespecial java.lang.StringBuilder(java.lang.String) [31]\n" +
			"    189  iload_1 [a]\n" +
			"    190  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]\n" +
			"    193  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]\n" +
			"    196  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]\n" +
			"    199  aload 6\n" +
			"    201  athrow\n" +
			"      Exception Table:\n" +
			"        [pc: 6, pc: 13] -> 61 when : java.lang.NullPointerException\n" +
			"        [pc: 6, pc: 13] -> 118 when : any\n" +
			"        [pc: 61, pc: 70] -> 118 when : any\n" +
			"        [pc: 2, pc: 35] -> 145 when : java.lang.Exception\n" +
			"        [pc: 61, pc: 92] -> 145 when : java.lang.Exception\n" +
			"        [pc: 118, pc: 145] -> 145 when : java.lang.Exception\n" +
			"        [pc: 2, pc: 35] -> 172 when : any\n" +
			"        [pc: 61, pc: 92] -> 172 when : any\n" +
			"        [pc: 118, pc: 146] -> 172 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 5]\n" +
			"        [pc: 4, line: 6]\n" +
			"        [pc: 6, line: 8]\n" +
			"        [pc: 13, line: 14]\n" +
			"        [pc: 35, line: 19]\n" +
			"        [pc: 38, line: 20]\n" +
			"        [pc: 60, line: 9]\n" +
			"        [pc: 61, line: 10]\n" +
			"        [pc: 63, line: 11]\n" +
			"        [pc: 70, line: 14]\n" +
			"        [pc: 92, line: 19]\n" +
			"        [pc: 95, line: 20]\n" +
			"        [pc: 117, line: 12]\n" +
			"        [pc: 118, line: 13]\n" +
			"        [pc: 120, line: 14]\n" +
			"        [pc: 142, line: 15]\n" +
			"        [pc: 145, line: 16]\n" +
			"        [pc: 146, line: 19]\n" +
			"        [pc: 149, line: 20]\n" +
			"        [pc: 171, line: 17]\n" +
			"        [pc: 172, line: 18]\n" +
			"        [pc: 174, line: 19]\n" +
			"        [pc: 177, line: 20]\n" +
			"        [pc: 199, line: 21]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 202] local: this index: 0 type: X\n" +
			"        [pc: 2, pc: 202] local: a index: 1 type: int\n" +
			"        [pc: 4, pc: 145] local: warnings index: 2 type: java.lang.Object\n" +
			"        [pc: 6, pc: 145] local: contexts index: 3 type: java.lang.Object\n" +
			"        [pc: 63, pc: 118] local: npe index: 4 type: java.lang.NullPointerException\n" +
			"        [pc: 146, pc: 172] local: e index: 2 type: java.lang.Exception\n" +
			"        [pc: 38, pc: 60] local: var index: 7 type: int\n" +
			"        [pc: 95, pc: 117] local: var index: 7 type: int\n" +
			"        [pc: 149, pc: 171] local: var index: 7 type: int\n" +
			"        [pc: 177, pc: 199] local: var index: 7 type: int\n";
		} else {
			expectedOutput = "  // Method descriptor #6 ()V\n" +
			"  // Stack: 4, Locals: 8\n" +
			"  public void save();\n" +
			"      0  iconst_3\n" +
			"      1  istore_1 [a]\n" +
			"      2  aconst_null\n" +
			"      3  astore_2 [warnings]\n" +
			"      4  aconst_null\n" +
			"      5  astore_3 [contexts]\n" +
			"      6  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"      9  aload_2 [warnings]\n" +
			"     10  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [21]\n" +
			"     13  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     16  new java.lang.StringBuilder [27]\n" +
			"     19  dup\n" +
			"     20  ldc <String \"#inner -> \"> [29]\n" +
			"     22  invokespecial java.lang.StringBuilder(java.lang.String) [31]\n" +
			"     25  iload_1 [a]\n" +
			"     26  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]\n" +
			"     29  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]\n" +
			"     32  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"     35  iconst_0\n" +
			"     36  istore 7 [var]\n" +
			"     38  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     41  new java.lang.StringBuilder [27]\n" +
			"     44  dup\n" +
			"     45  ldc <String \"#save -> \"> [44]\n" +
			"     47  invokespecial java.lang.StringBuilder(java.lang.String) [31]\n" +
			"     50  iload_1 [a]\n" +
			"     51  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]\n" +
			"     54  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]\n" +
			"     57  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]\n" +
			"     60  return\n" +
			"     61  astore 4 [npe]\n" +
			"     63  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     66  aload_3 [contexts]\n" +
			"     67  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [21]\n" +
			"     70  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     73  new java.lang.StringBuilder [27]\n" +
			"     76  dup\n" +
			"     77  ldc <String \"#inner -> \"> [29]\n" +
			"     79  invokespecial java.lang.StringBuilder(java.lang.String) [31]\n" +
			"     82  iload_1 [a]\n" +
			"     83  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]\n" +
			"     86  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]\n" +
			"     89  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"     92  iconst_0\n" +
			"     93  istore 7 [var]\n" +
			"     95  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     98  new java.lang.StringBuilder [27]\n" +
			"    101  dup\n" +
			"    102  ldc <String \"#save -> \"> [44]\n" +
			"    104  invokespecial java.lang.StringBuilder(java.lang.String) [31]\n" +
			"    107  iload_1 [a]\n" +
			"    108  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]\n" +
			"    111  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]\n" +
			"    114  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]\n" +
			"    117  return\n" +
			"    118  astore 5\n" +
			"    120  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"    123  new java.lang.StringBuilder [27]\n" +
			"    126  dup\n" +
			"    127  ldc <String \"#inner -> \"> [29]\n" +
			"    129  invokespecial java.lang.StringBuilder(java.lang.String) [31]\n" +
			"    132  iload_1 [a]\n" +
			"    133  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]\n" +
			"    136  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]\n" +
			"    139  invokevirtual java.io.PrintStream.print(java.lang.String) : void [42]\n" +
			"    142  aload 5\n" +
			"    144  athrow\n" +
			"    145  astore_2 [e]\n" +
			"    146  iconst_0\n" +
			"    147  istore 7 [var]\n" +
			"    149  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"    152  new java.lang.StringBuilder [27]\n" +
			"    155  dup\n" +
			"    156  ldc <String \"#save -> \"> [44]\n" +
			"    158  invokespecial java.lang.StringBuilder(java.lang.String) [31]\n" +
			"    161  iload_1 [a]\n" +
			"    162  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]\n" +
			"    165  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]\n" +
			"    168  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]\n" +
			"    171  return\n" +
			"    172  astore 6\n" +
			"    174  iconst_0\n" +
			"    175  istore 7 [var]\n" +
			"    177  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"    180  new java.lang.StringBuilder [27]\n" +
			"    183  dup\n" +
			"    184  ldc <String \"#save -> \"> [44]\n" +
			"    186  invokespecial java.lang.StringBuilder(java.lang.String) [31]\n" +
			"    189  iload_1 [a]\n" +
			"    190  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [34]\n" +
			"    193  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [38]\n" +
			"    196  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]\n" +
			"    199  aload 6\n" +
			"    201  athrow\n" +
			"      Exception Table:\n" +
			"        [pc: 6, pc: 13] -> 61 when : java.lang.NullPointerException\n" +
			"        [pc: 6, pc: 13] -> 118 when : any\n" +
			"        [pc: 61, pc: 70] -> 118 when : any\n" +
			"        [pc: 2, pc: 35] -> 145 when : java.lang.Exception\n" +
			"        [pc: 61, pc: 92] -> 145 when : java.lang.Exception\n" +
			"        [pc: 118, pc: 145] -> 145 when : java.lang.Exception\n" +
			"        [pc: 2, pc: 35] -> 172 when : any\n" +
			"        [pc: 61, pc: 92] -> 172 when : any\n" +
			"        [pc: 118, pc: 146] -> 172 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 5]\n" +
			"        [pc: 4, line: 6]\n" +
			"        [pc: 6, line: 8]\n" +
			"        [pc: 13, line: 14]\n" +
			"        [pc: 35, line: 19]\n" +
			"        [pc: 38, line: 20]\n" +
			"        [pc: 60, line: 9]\n" +
			"        [pc: 61, line: 10]\n" +
			"        [pc: 63, line: 11]\n" +
			"        [pc: 70, line: 14]\n" +
			"        [pc: 92, line: 19]\n" +
			"        [pc: 95, line: 20]\n" +
			"        [pc: 117, line: 12]\n" +
			"        [pc: 118, line: 13]\n" +
			"        [pc: 120, line: 14]\n" +
			"        [pc: 142, line: 15]\n" +
			"        [pc: 145, line: 16]\n" +
			"        [pc: 146, line: 19]\n" +
			"        [pc: 149, line: 20]\n" +
			"        [pc: 171, line: 17]\n" +
			"        [pc: 172, line: 18]\n" +
			"        [pc: 174, line: 19]\n" +
			"        [pc: 177, line: 20]\n" +
			"        [pc: 199, line: 21]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 202] local: this index: 0 type: X\n" +
			"        [pc: 2, pc: 202] local: a index: 1 type: int\n" +
			"        [pc: 4, pc: 145] local: warnings index: 2 type: java.lang.Object\n" +
			"        [pc: 6, pc: 145] local: contexts index: 3 type: java.lang.Object\n" +
			"        [pc: 63, pc: 118] local: npe index: 4 type: java.lang.NullPointerException\n" +
			"        [pc: 146, pc: 172] local: e index: 2 type: java.lang.Exception\n" +
			"        [pc: 38, pc: 60] local: var index: 7 type: int\n" +
			"        [pc: 95, pc: 117] local: var index: 7 type: int\n" +
			"        [pc: 149, pc: 171] local: var index: 7 type: int\n" +
			"        [pc: 177, pc: 199] local: var index: 7 type: int\n" +
			"      Stack map table: number of frames 4\n" +
			"        [pc: 61, full, stack: {java.lang.NullPointerException}, locals: {X, int, java.lang.Object, java.lang.Object}]\n" +
			"        [pc: 118, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" +
			"        [pc: 145, full, stack: {java.lang.Exception}, locals: {X, int}]\n" +
			"        [pc: 172, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n";
		}
	}

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=129305
public void test044() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		foo();\n" +
				"	}  \n" +
				"	static Object foo() {\n" +
				"		try {\n" +
				"			return null;\n" +
				"		} catch(Exception e) {\n" +
				"			return null;\n" +
				"		} finally {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");

	CompilerOptions compilerOptions = new CompilerOptions(getCompilerOptions());
	String expectedOutput = !compilerOptions.inlineJsrBytecode ?
			"  // Method descriptor #19 ()Ljava/lang/Object;\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  static java.lang.Object foo();\n" +
			"     0  jsr 15\n" +
			"     3  aconst_null\n" +
			"     4  areturn\n" +
			"     5  astore_0 [e]\n" +
			"     6  goto 0\n" +
			"     9  astore_2\n" +
			"    10  jsr 15\n" +
			"    13  aload_2\n" +
			"    14  athrow\n" +
			"    15  astore_1\n" +
			"    16  getstatic java.lang.System.out : java.io.PrintStream [22]\n" +
			"    19  ldc <String \"SUCCESS\"> [28]\n" +
			"    21  invokevirtual java.io.PrintStream.println(java.lang.String) : void [30]\n" +
			"    24  ret 1\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 3] -> 5 when : java.lang.Exception\n" +
			"        [pc: 0, pc: 3] -> 9 when : any\n" +
			"        [pc: 5, pc: 9] -> 9 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 7]\n" +
			"        [pc: 5, line: 8]\n" +
			"        [pc: 6, line: 9]\n" +
			"        [pc: 9, line: 10]\n" +
			"        [pc: 13, line: 12]\n" +
			"        [pc: 15, line: 10]\n" +
			"        [pc: 16, line: 11]\n" +
			"        [pc: 24, line: 12]\n" +
			"      Local variable table:\n" +
			"        [pc: 6, pc: 9] local: e index: 0 type: java.lang.Exception\n"
		: null;

	if (expectedOutput == null) {
		if (compilerOptions.targetJDK == ClassFileConstants.JDK1_5) {
			expectedOutput =
				"  // Method descriptor #19 ()Ljava/lang/Object;\n" +
				"  // Stack: 2, Locals: 0\n" +
				"  static java.lang.Object foo();\n" +
				"     0  getstatic java.lang.System.out : java.io.PrintStream [22]\n" +
				"     3  ldc <String \"SUCCESS\"> [28]\n" +
				"     5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [30]\n" +
				"     8  aconst_null\n" +
				"     9  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 11]\n" +
				"        [pc: 8, line: 7]\n";
		} else {
			expectedOutput = "  // Method descriptor #19 ()Ljava/lang/Object;\n" +
			"  // Stack: 2, Locals: 0\n" +
			"  static java.lang.Object foo();\n" +
			"     0  getstatic java.lang.System.out : java.io.PrintStream [22]\n" +
			"     3  ldc <String \"SUCCESS\"> [28]\n" +
			"     5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [30]\n" +
			"     8  aconst_null\n" +
			"     9  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 11]\n" +
			"        [pc: 8, line: 7]\n";
		}
	}
	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=129306
public void test045() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void save() {\n" +
				"		try {\n" +
				"			Object warnings = null;\n" +
				"			Object contexts = null;\n" +
				"			try {\n" +
				"				System.out.print(\"[try]\");\n" +
				"				System.out.print(warnings); \n" +
				"				return;\n" +
				"			} catch (NullPointerException npe) {\n" +
				"				System.out.print(\"[npe]\");\n" +
				"				System.out.print(contexts); \n" +
				"				return;\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			System.out.print(\"[e]\");\n" +
				"			return;\n" +
				"		} finally { \n" +
				"			int var = 0;\n" +
				"			System.out.print(\"[finally]\");\n" +
				"			Object o = null;\n" +
				"			o.toString();\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			new X().save();\n" +
				"		} catch(NullPointerException e) {\n" +
				"			System.out.println(\"[caught npe]\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"[try]null[finally][caught npe]");

	CompilerOptions compilerOptions = new CompilerOptions(getCompilerOptions());
	String expectedOutput = !compilerOptions.inlineJsrBytecode
		?	"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 8\n" +
			"  public void save();\n" +
			"     0  aconst_null\n" +
			"     1  astore_1 [warnings]\n" +
			"     2  aconst_null\n" +
			"     3  astore_2 [contexts]\n" +
			"     4  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     7  ldc <String \"[try]\"> [21]\n" +
			"     9  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"    12  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"    15  aload_1 [warnings]\n" +
			"    16  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [29]\n" +
			"    19  jsr 62\n" +
			"    22  return\n" +
			"    23  astore_3 [npe]\n" +
			"    24  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"    27  ldc <String \"[npe]\"> [32]\n" +
			"    29  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"    32  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"    35  aload_2 [contexts]\n" +
			"    36  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [29]\n" +
			"    39  goto 19\n" +
			"    42  astore_1 [e]\n" +
			"    43  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"    46  ldc <String \"[e]\"> [34]\n" +
			"    48  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"    51  goto 19\n" +
			"    54  astore 5\n" +
			"    56  jsr 62\n" +
			"    59  aload 5\n" +
			"    61  athrow\n" +
			"    62  astore 4\n" +
			"    64  iconst_0\n" +
			"    65  istore 6 [var]\n" +
			"    67  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"    70  ldc <String \"[finally]\"> [36]\n" +
			"    72  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"    75  aconst_null\n" +
			"    76  astore 7 [o]\n" +
			"    78  aload 7 [o]\n" +
			"    80  invokevirtual java.lang.Object.toString() : java.lang.String [38]\n" +
			"    83  pop\n" +
			"    84  ret 4\n" +
			"      Exception Table:\n" +
			"        [pc: 4, pc: 19] -> 23 when : java.lang.NullPointerException\n" +
			"        [pc: 0, pc: 22] -> 42 when : java.lang.Exception\n" +
			"        [pc: 23, pc: 42] -> 42 when : java.lang.Exception\n" +
			"        [pc: 0, pc: 22] -> 54 when : any\n" +
			"        [pc: 23, pc: 54] -> 54 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"        [pc: 2, line: 5]\n" +
			"        [pc: 4, line: 7]\n" +
			"        [pc: 12, line: 8]\n" +
			"        [pc: 19, line: 9]\n" +
			"        [pc: 23, line: 10]\n" +
			"        [pc: 24, line: 11]\n" +
			"        [pc: 32, line: 12]\n" +
			"        [pc: 39, line: 13]\n" +
			"        [pc: 42, line: 15]\n" +
			"        [pc: 43, line: 16]\n" +
			"        [pc: 51, line: 17]\n" +
			"        [pc: 54, line: 18]\n" +
			"        [pc: 59, line: 23]\n" +
			"        [pc: 62, line: 18]\n" +
			"        [pc: 64, line: 19]\n" +
			"        [pc: 67, line: 20]\n" +
			"        [pc: 75, line: 21]\n" +
			"        [pc: 78, line: 22]\n" +
			"        [pc: 84, line: 23]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 86] local: this index: 0 type: X\n" +
			"        [pc: 2, pc: 42] local: warnings index: 1 type: java.lang.Object\n" +
			"        [pc: 4, pc: 42] local: contexts index: 2 type: java.lang.Object\n" +
			"        [pc: 24, pc: 42] local: npe index: 3 type: java.lang.NullPointerException\n" +
			"        [pc: 43, pc: 54] local: e index: 1 type: java.lang.Exception\n" +
			"        [pc: 67, pc: 84] local: var index: 6 type: int\n" +
			"        [pc: 78, pc: 84] local: o index: 7 type: java.lang.Object\n"
		: null;

	if (expectedOutput == null) {
		if (compilerOptions.targetJDK == ClassFileConstants.JDK1_5) {
			expectedOutput = "  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 7\n" +
			"  public void save();\n" +
			"      0  aconst_null\n" +
			"      1  astore_1 [warnings]\n" +
			"      2  aconst_null\n" +
			"      3  astore_2 [contexts]\n" +
			"      4  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"      7  ldc <String \"[try]\"> [21]\n" +
			"      9  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"     12  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     15  aload_1 [warnings]\n" +
			"     16  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [29]\n" +
			"     19  iconst_0\n" +
			"     20  istore 5 [var]\n" +
			"     22  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     25  ldc <String \"[finally]\"> [32]\n" +
			"     27  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"     30  aconst_null\n" +
			"     31  astore 6 [o]\n" +
			"     33  aload 6 [o]\n" +
			"     35  invokevirtual java.lang.Object.toString() : java.lang.String [34]\n" +
			"     38  pop\n" +
			"     39  return\n" +
			"     40  astore_3 [npe]\n" +
			"     41  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     44  ldc <String \"[npe]\"> [38]\n" +
			"     46  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"     49  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     52  aload_2 [contexts]\n" +
			"     53  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [29]\n" +
			"     56  iconst_0\n" +
			"     57  istore 5 [var]\n" +
			"     59  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     62  ldc <String \"[finally]\"> [32]\n" +
			"     64  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"     67  aconst_null\n" +
			"     68  astore 6 [o]\n" +
			"     70  aload 6 [o]\n" +
			"     72  invokevirtual java.lang.Object.toString() : java.lang.String [34]\n" +
			"     75  pop\n" +
			"     76  return\n" +
			"     77  astore_1 [e]\n" +
			"     78  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     81  ldc <String \"[e]\"> [40]\n" +
			"     83  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"     86  iconst_0\n" +
			"     87  istore 5 [var]\n" +
			"     89  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     92  ldc <String \"[finally]\"> [32]\n" +
			"     94  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"     97  aconst_null\n" +
			"     98  astore 6 [o]\n" +
			"    100  aload 6 [o]\n" +
			"    102  invokevirtual java.lang.Object.toString() : java.lang.String [34]\n" +
			"    105  pop\n" +
			"    106  return\n" +
			"    107  astore 4\n" +
			"    109  iconst_0\n" +
			"    110  istore 5 [var]\n" +
			"    112  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"    115  ldc <String \"[finally]\"> [32]\n" +
			"    117  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"    120  aconst_null\n" +
			"    121  astore 6 [o]\n" +
			"    123  aload 6 [o]\n" +
			"    125  invokevirtual java.lang.Object.toString() : java.lang.String [34]\n" +
			"    128  pop\n" +
			"    129  aload 4\n" +
			"    131  athrow\n" +
			"      Exception Table:\n" +
			"        [pc: 4, pc: 19] -> 40 when : java.lang.NullPointerException\n" +
			"        [pc: 0, pc: 19] -> 77 when : java.lang.Exception\n" +
			"        [pc: 40, pc: 56] -> 77 when : java.lang.Exception\n" +
			"        [pc: 0, pc: 19] -> 107 when : any\n" +
			"        [pc: 40, pc: 56] -> 107 when : any\n" +
			"        [pc: 77, pc: 86] -> 107 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"        [pc: 2, line: 5]\n" +
			"        [pc: 4, line: 7]\n" +
			"        [pc: 12, line: 8]\n" +
			"        [pc: 19, line: 19]\n" +
			"        [pc: 22, line: 20]\n" +
			"        [pc: 30, line: 21]\n" +
			"        [pc: 33, line: 22]\n" +
			"        [pc: 39, line: 9]\n" +
			"        [pc: 40, line: 10]\n" +
			"        [pc: 41, line: 11]\n" +
			"        [pc: 49, line: 12]\n" +
			"        [pc: 56, line: 19]\n" +
			"        [pc: 59, line: 20]\n" +
			"        [pc: 67, line: 21]\n" +
			"        [pc: 70, line: 22]\n" +
			"        [pc: 76, line: 13]\n" +
			"        [pc: 77, line: 15]\n" +
			"        [pc: 78, line: 16]\n" +
			"        [pc: 86, line: 19]\n" +
			"        [pc: 89, line: 20]\n" +
			"        [pc: 97, line: 21]\n" +
			"        [pc: 100, line: 22]\n" +
			"        [pc: 106, line: 17]\n" +
			"        [pc: 107, line: 18]\n" +
			"        [pc: 109, line: 19]\n" +
			"        [pc: 112, line: 20]\n" +
			"        [pc: 120, line: 21]\n" +
			"        [pc: 123, line: 22]\n" +
			"        [pc: 129, line: 23]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 132] local: this index: 0 type: X\n" +
			"        [pc: 2, pc: 77] local: warnings index: 1 type: java.lang.Object\n" +
			"        [pc: 4, pc: 77] local: contexts index: 2 type: java.lang.Object\n" +
			"        [pc: 41, pc: 77] local: npe index: 3 type: java.lang.NullPointerException\n" +
			"        [pc: 78, pc: 107] local: e index: 1 type: java.lang.Exception\n" +
			"        [pc: 22, pc: 39] local: var index: 5 type: int\n" +
			"        [pc: 59, pc: 76] local: var index: 5 type: int\n" +
			"        [pc: 89, pc: 106] local: var index: 5 type: int\n" +
			"        [pc: 112, pc: 129] local: var index: 5 type: int\n" +
			"        [pc: 33, pc: 39] local: o index: 6 type: java.lang.Object\n" +
			"        [pc: 70, pc: 76] local: o index: 6 type: java.lang.Object\n" +
			"        [pc: 100, pc: 106] local: o index: 6 type: java.lang.Object\n" +
			"        [pc: 123, pc: 129] local: o index: 6 type: java.lang.Object\n";
		} else {
			expectedOutput = "  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 7\n" +
			"  public void save();\n" +
			"      0  aconst_null\n" +
			"      1  astore_1 [warnings]\n" +
			"      2  aconst_null\n" +
			"      3  astore_2 [contexts]\n" +
			"      4  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"      7  ldc <String \"[try]\"> [21]\n" +
			"      9  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"     12  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     15  aload_1 [warnings]\n" +
			"     16  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [29]\n" +
			"     19  iconst_0\n" +
			"     20  istore 5 [var]\n" +
			"     22  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     25  ldc <String \"[finally]\"> [32]\n" +
			"     27  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"     30  aconst_null\n" +
			"     31  astore 6 [o]\n" +
			"     33  aload 6 [o]\n" +
			"     35  invokevirtual java.lang.Object.toString() : java.lang.String [34]\n" +
			"     38  pop\n" +
			"     39  return\n" +
			"     40  astore_3 [npe]\n" +
			"     41  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     44  ldc <String \"[npe]\"> [38]\n" +
			"     46  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"     49  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     52  aload_2 [contexts]\n" +
			"     53  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [29]\n" +
			"     56  iconst_0\n" +
			"     57  istore 5 [var]\n" +
			"     59  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     62  ldc <String \"[finally]\"> [32]\n" +
			"     64  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"     67  aconst_null\n" +
			"     68  astore 6 [o]\n" +
			"     70  aload 6 [o]\n" +
			"     72  invokevirtual java.lang.Object.toString() : java.lang.String [34]\n" +
			"     75  pop\n" +
			"     76  return\n" +
			"     77  astore_1 [e]\n" +
			"     78  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     81  ldc <String \"[e]\"> [40]\n" +
			"     83  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"     86  iconst_0\n" +
			"     87  istore 5 [var]\n" +
			"     89  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"     92  ldc <String \"[finally]\"> [32]\n" +
			"     94  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"     97  aconst_null\n" +
			"     98  astore 6 [o]\n" +
			"    100  aload 6 [o]\n" +
			"    102  invokevirtual java.lang.Object.toString() : java.lang.String [34]\n" +
			"    105  pop\n" +
			"    106  return\n" +
			"    107  astore 4\n" +
			"    109  iconst_0\n" +
			"    110  istore 5 [var]\n" +
			"    112  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
			"    115  ldc <String \"[finally]\"> [32]\n" +
			"    117  invokevirtual java.io.PrintStream.print(java.lang.String) : void [23]\n" +
			"    120  aconst_null\n" +
			"    121  astore 6 [o]\n" +
			"    123  aload 6 [o]\n" +
			"    125  invokevirtual java.lang.Object.toString() : java.lang.String [34]\n" +
			"    128  pop\n" +
			"    129  aload 4\n" +
			"    131  athrow\n" +
			"      Exception Table:\n" +
			"        [pc: 4, pc: 19] -> 40 when : java.lang.NullPointerException\n" +
			"        [pc: 0, pc: 19] -> 77 when : java.lang.Exception\n" +
			"        [pc: 40, pc: 56] -> 77 when : java.lang.Exception\n" +
			"        [pc: 0, pc: 19] -> 107 when : any\n" +
			"        [pc: 40, pc: 56] -> 107 when : any\n" +
			"        [pc: 77, pc: 86] -> 107 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"        [pc: 2, line: 5]\n" +
			"        [pc: 4, line: 7]\n" +
			"        [pc: 12, line: 8]\n" +
			"        [pc: 19, line: 19]\n" +
			"        [pc: 22, line: 20]\n" +
			"        [pc: 30, line: 21]\n" +
			"        [pc: 33, line: 22]\n" +
			"        [pc: 39, line: 9]\n" +
			"        [pc: 40, line: 10]\n" +
			"        [pc: 41, line: 11]\n" +
			"        [pc: 49, line: 12]\n" +
			"        [pc: 56, line: 19]\n" +
			"        [pc: 59, line: 20]\n" +
			"        [pc: 67, line: 21]\n" +
			"        [pc: 70, line: 22]\n" +
			"        [pc: 76, line: 13]\n" +
			"        [pc: 77, line: 15]\n" +
			"        [pc: 78, line: 16]\n" +
			"        [pc: 86, line: 19]\n" +
			"        [pc: 89, line: 20]\n" +
			"        [pc: 97, line: 21]\n" +
			"        [pc: 100, line: 22]\n" +
			"        [pc: 106, line: 17]\n" +
			"        [pc: 107, line: 18]\n" +
			"        [pc: 109, line: 19]\n" +
			"        [pc: 112, line: 20]\n" +
			"        [pc: 120, line: 21]\n" +
			"        [pc: 123, line: 22]\n" +
			"        [pc: 129, line: 23]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 132] local: this index: 0 type: X\n" +
			"        [pc: 2, pc: 77] local: warnings index: 1 type: java.lang.Object\n" +
			"        [pc: 4, pc: 77] local: contexts index: 2 type: java.lang.Object\n" +
			"        [pc: 41, pc: 77] local: npe index: 3 type: java.lang.NullPointerException\n" +
			"        [pc: 78, pc: 107] local: e index: 1 type: java.lang.Exception\n" +
			"        [pc: 22, pc: 39] local: var index: 5 type: int\n" +
			"        [pc: 59, pc: 76] local: var index: 5 type: int\n" +
			"        [pc: 89, pc: 106] local: var index: 5 type: int\n" +
			"        [pc: 112, pc: 129] local: var index: 5 type: int\n" +
			"        [pc: 33, pc: 39] local: o index: 6 type: java.lang.Object\n" +
			"        [pc: 70, pc: 76] local: o index: 6 type: java.lang.Object\n" +
			"        [pc: 100, pc: 106] local: o index: 6 type: java.lang.Object\n" +
			"        [pc: 123, pc: 129] local: o index: 6 type: java.lang.Object\n" +
			"      Stack map table: number of frames 3\n" +
			"        [pc: 40, full, stack: {java.lang.NullPointerException}, locals: {X, java.lang.Object, java.lang.Object}]\n" +
			"        [pc: 77, full, stack: {java.lang.Exception}, locals: {X}]\n" +
			"        [pc: 107, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n";
		}
	}

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108180
public void test046() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static Object sanityCheckBug() {\n" +
				"        Object obj;\n" +
				"        try {\n" +
				"            obj = new Object();\n" +
				"            return obj;\n" +
				"        } finally {\n" +
				"             obj = null;\n" +
				"        }\n" +
				"    }\n" +
				"    public static void main(String[] arguments) {\n" +
				"		X.sanityCheckBug();\n" +
				"    }\n" +
				"}\n",
			},
			"");

	CompilerOptions compilerOptions = new CompilerOptions(getCompilerOptions());
	String expectedOutput = !compilerOptions.inlineJsrBytecode
		?	"  // Method descriptor #15 ()Ljava/lang/Object;\n" +
			"  // Stack: 2, Locals: 4\n" +
			"  public static java.lang.Object sanityCheckBug();\n" +
			"     0  new java.lang.Object [3]\n" +
			"     3  dup\n" +
			"     4  invokespecial java.lang.Object() [8]\n" +
			"     7  astore_0 [obj]\n" +
			"     8  aload_0 [obj]\n" +
			"     9  astore_3\n" +
			"    10  jsr 21\n" +
			"    13  aload_3\n" +
			"    14  areturn\n" +
			"    15  astore_2\n" +
			"    16  jsr 21\n" +
			"    19  aload_2\n" +
			"    20  athrow\n" +
			"    21  astore_1\n" +
			"    22  aconst_null\n" +
			"    23  astore_0 [obj]\n" +
			"    24  ret 1\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 13] -> 15 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 8, line: 6]\n" +
			"        [pc: 15, line: 7]\n" +
			"        [pc: 19, line: 9]\n" +
			"        [pc: 21, line: 7]\n" +
			"        [pc: 22, line: 8]\n" +
			"        [pc: 24, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 8, pc: 15] local: obj index: 0 type: java.lang.Object\n" +
			"        [pc: 24, pc: 26] local: obj index: 0 type: java.lang.Object\n"
		: null;

	if (expectedOutput == null) {
		if (compilerOptions.targetJDK == ClassFileConstants.JDK1_5) {
			expectedOutput = "  // Method descriptor #15 ()Ljava/lang/Object;\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  public static java.lang.Object sanityCheckBug();\n" +
			"     0  new java.lang.Object [3]\n" +
			"     3  dup\n" +
			"     4  invokespecial java.lang.Object() [8]\n" +
			"     7  astore_0 [obj]\n" +
			"     8  aload_0 [obj]\n" +
			"     9  astore_2\n" +
			"    10  aconst_null\n" +
			"    11  astore_0 [obj]\n" +
			"    12  aload_2\n" +
			"    13  areturn\n" +
			"    14  astore_1\n" +
			"    15  aconst_null\n" +
			"    16  astore_0 [obj]\n" +
			"    17  aload_1\n" +
			"    18  athrow\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 10] -> 14 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 8, line: 6]\n" +
			"        [pc: 10, line: 8]\n" +
			"        [pc: 12, line: 6]\n" +
			"        [pc: 14, line: 7]\n" +
			"        [pc: 15, line: 8]\n" +
			"        [pc: 17, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 8, pc: 14] local: obj index: 0 type: java.lang.Object\n" +
			"        [pc: 17, pc: 19] local: obj index: 0 type: java.lang.Object\n";
		} else {
			expectedOutput = "  // Method descriptor #15 ()Ljava/lang/Object;\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  public static java.lang.Object sanityCheckBug();\n" +
			"     0  new java.lang.Object [3]\n" +
			"     3  dup\n" +
			"     4  invokespecial java.lang.Object() [8]\n" +
			"     7  astore_0 [obj]\n" +
			"     8  aload_0 [obj]\n" +
			"     9  astore_2\n" +
			"    10  aconst_null\n" +
			"    11  astore_0 [obj]\n" +
			"    12  aload_2\n" +
			"    13  areturn\n" +
			"    14  astore_1\n" +
			"    15  aconst_null\n" +
			"    16  astore_0 [obj]\n" +
			"    17  aload_1\n" +
			"    18  athrow\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 10] -> 14 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 8, line: 6]\n" +
			"        [pc: 10, line: 8]\n" +
			"        [pc: 12, line: 6]\n" +
			"        [pc: 14, line: 7]\n" +
			"        [pc: 15, line: 8]\n" +
			"        [pc: 17, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 8, pc: 14] local: obj index: 0 type: java.lang.Object\n" +
			"        [pc: 17, pc: 19] local: obj index: 0 type: java.lang.Object\n";
		}
	}

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
public void test047() {
	if (new CompilerOptions(getCompilerOptions()).complianceLevel <= ClassFileConstants.JDK1_3) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"        public static void main(String[] args) {\n" +
					"                try {\n" +
					"					if (false) throw null;\n" +
					"					throw new Object();\n" +
					"                } catch(Object o) {\n" +
					"                }\n" +
					"        }\n" +
					"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	if (false) throw null;\n" +
				"	                 ^^^^\n" +
				"Cannot throw null as an exception\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	throw new Object();\n" +
				"	      ^^^^^^^^^^^^\n" +
				"No exception of type Object can be thrown; an exception type must be a subclass of Throwable\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	} catch(Object o) {\n" +
				"	        ^^^^^^\n" +
				"No exception of type Object can be thrown; an exception type must be a subclass of Throwable\n" +
				"----------\n");
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"                try {\n" +
			"					if (false) throw null;\n" +
			"					throw new Object();\n" +
			"                } catch(Object o) {\n" +
			"                }\n" +
			"        }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	throw new Object();\n" +
		"	      ^^^^^^^^^^^^\n" +
		"No exception of type Object can be thrown; an exception type must be a subclass of Throwable\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	} catch(Object o) {\n" +
		"	        ^^^^^^\n" +
		"No exception of type Object can be thrown; an exception type must be a subclass of Throwable\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894
public void test048() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	boolean bool() { return true; }\n" +
				"	void foo() {\n" +
				"		try {\n" +
				"			if (bool()) {\n" +
				"				return;\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"	}\n" +
				"	int foo2() {\n" +
				"		try {\n" +
				"			while (bool()) {\n" +
				"				return 0;\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"		return 1;\n" +
				"	}\n" +
				"	long foo3() {\n" +
				"		try {\n" +
				"			do {\n" +
				"				if (true) return 0L;\n" +
				"			} while (bool());\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"		return 1L;\n" +
				"	}	\n" +
				"	float foo4() {\n" +
				"		try {\n" +
				"			for (int i  = 0; bool(); i++) {\n" +
				"				return 0.0F;\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"		return 1.0F;\n" +
				"	}		\n" +
				"	double bar() {\n" +
				"		if (bool()) {\n" +
				"			if (bool())\n" +
				"				return 0.0;\n" +
				"		} else {\n" +
				"			if (bool()) {\n" +
				"				throw new NullPointerException();\n" +
				"			}\n" +
				"		}\n" +
				"		return 1.0;\n" +
				"	}\n" +
				"	void baz(int i) {\n" +
				"		if (bool()) {\n" +
				"			switch(i) {\n" +
				"				case 0 : return;\n" +
				"				default : break;\n" +
				"			}\n" +
				"		} else {\n" +
				"			bool();\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"  // Method descriptor #6 ()V\n" +
				"  // Stack: 1, Locals: 2\n" +
				"  void foo();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokevirtual X.bool() : boolean [17]\n" +
				"     4  ifeq 9\n" +
				"     7  return\n" +
				"     8  astore_1\n" +
				"     9  return\n" +
				"      Exception Table:\n" +
				"        [pc: 0, pc: 7] -> 8 when : java.lang.Exception\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 5]\n" +
				"        [pc: 7, line: 6]\n" +
				"        [pc: 8, line: 8]\n" +
				"        [pc: 9, line: 10]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 10] local: this index: 0 type: X\n" +
				"  \n" +
				"  // Method descriptor #22 ()I\n" +
				"  // Stack: 1, Locals: 2\n" +
				"  int foo2();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokevirtual X.bool() : boolean [17]\n" +
				"     4  ifeq 10\n" +
				"     7  iconst_0\n" +
				"     8  ireturn\n" +
				"     9  astore_1\n" +
				"    10  iconst_1\n" +
				"    11  ireturn\n" +
				"      Exception Table:\n" +
				"        [pc: 0, pc: 7] -> 9 when : java.lang.Exception\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 13]\n" +
				"        [pc: 7, line: 14]\n" +
				"        [pc: 9, line: 16]\n" +
				"        [pc: 10, line: 18]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 12] local: this index: 0 type: X\n" +
				"  \n" +
				"  // Method descriptor #24 ()J\n" +
				"  // Stack: 2, Locals: 1\n" +
				"  long foo3();\n" +
				"    0  lconst_0\n" +
				"    1  lreturn\n" +
				"    2  lconst_1\n" +
				"    3  lreturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 23]\n" +
				"        [pc: 2, line: 27]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 4] local: this index: 0 type: X\n" +
				"  \n" +
				"  // Method descriptor #26 ()F\n" +
				"  // Stack: 1, Locals: 2\n" +
				"  float foo4();\n" +
				"     0  iconst_0\n" +
				"     1  istore_1 [i]\n" +
				"     2  aload_0 [this]\n" +
				"     3  invokevirtual X.bool() : boolean [17]\n" +
				"     6  ifeq 12\n" +
				"     9  fconst_0\n" +
				"    10  freturn\n" +
				"    11  astore_1\n" +
				"    12  fconst_1\n" +
				"    13  freturn\n" +
				"      Exception Table:\n" +
				"        [pc: 0, pc: 9] -> 11 when : java.lang.Exception\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 31]\n" +
				"        [pc: 9, line: 32]\n" +
				"        [pc: 11, line: 34]\n" +
				"        [pc: 12, line: 36]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
				"        [pc: 2, pc: 11] local: i index: 1 type: int\n" +
				"  \n" +
				"  // Method descriptor #30 ()D\n" +
				"  // Stack: 2, Locals: 1\n" +
				"  double bar();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokevirtual X.bool() : boolean [17]\n" +
				"     4  ifeq 16\n" +
				"     7  aload_0 [this]\n" +
				"     8  invokevirtual X.bool() : boolean [17]\n" +
				"    11  ifeq 31\n" +
				"    14  dconst_0\n" +
				"    15  dreturn\n" +
				"    16  aload_0 [this]\n" +
				"    17  invokevirtual X.bool() : boolean [17]\n" +
				"    20  ifeq 31\n" +
				"    23  new java.lang.NullPointerException [31]\n" +
				"    26  dup\n" +
				"    27  invokespecial java.lang.NullPointerException() [33]\n" +
				"    30  athrow\n" +
				"    31  dconst_1\n" +
				"    32  dreturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 39]\n" +
				"        [pc: 7, line: 40]\n" +
				"        [pc: 14, line: 41]\n" +
				"        [pc: 16, line: 43]\n" +
				"        [pc: 23, line: 44]\n" +
				"        [pc: 31, line: 47]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 33] local: this index: 0 type: X\n" +
				"  \n" +
				"  // Method descriptor #35 (I)V\n" +
				"  // Stack: 1, Locals: 2\n" +
				"  void baz(int i);\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokevirtual X.bool() : boolean [17]\n" +
				"     4  ifeq 32\n" +
				"     7  iload_1 [i]\n" +
				"     8  tableswitch default: 29\n" +
				"          case 0: 28\n" +
				"    28  return\n" +
				"    29  goto 37\n" +
				"    32  aload_0 [this]\n" +
				"    33  invokevirtual X.bool() : boolean [17]\n" +
				"    36  pop\n" +
				"    37  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 50]\n" +
				"        [pc: 7, line: 51]\n" +
				"        [pc: 28, line: 52]\n" +
				"        [pc: 29, line: 55]\n" +
				"        [pc: 32, line: 56]\n" +
				"        [pc: 37, line: 58]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 38] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 38] local: i index: 1 type: int\n"
		:
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  void foo();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 9\n" +
			"     7  return\n" +
			"     8  astore_1\n" +
			"     9  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 7] -> 8 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 7, line: 6]\n" +
			"        [pc: 8, line: 8]\n" +
			"        [pc: 9, line: 10]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 10] local: this index: 0 type: X\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 8, same_locals_1_stack_item, stack: {java.lang.Exception}]\n" +
			"        [pc: 9, same]\n" +
			"  \n" +
			"  // Method descriptor #23 ()I\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  int foo2();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 10\n" +
			"     7  iconst_0\n" +
			"     8  ireturn\n" +
			"     9  astore_1\n" +
			"    10  iconst_1\n" +
			"    11  ireturn\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 7] -> 9 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 13]\n" +
			"        [pc: 7, line: 14]\n" +
			"        [pc: 9, line: 16]\n" +
			"        [pc: 10, line: 18]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 12] local: this index: 0 type: X\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 9, same_locals_1_stack_item, stack: {java.lang.Exception}]\n" +
			"        [pc: 10, same]\n" +
			"  \n" +
			"  // Method descriptor #25 ()J\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  long foo3();\n" +
			"    0  lconst_0\n" +
			"    1  lreturn\n" +
			"    2  lconst_1\n" +
			"    3  lreturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 23]\n" +
			"        [pc: 2, line: 27]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 4] local: this index: 0 type: X\n" +
			"      Stack map table: number of frames 1\n" +
			"        [pc: 2, same]\n" +
			"  \n" +
			"  // Method descriptor #27 ()F\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  float foo4();\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [i]\n" +
			"     2  aload_0 [this]\n" +
			"     3  invokevirtual X.bool() : boolean [17]\n" +
			"     6  ifeq 12\n" +
			"     9  fconst_0\n" +
			"    10  freturn\n" +
			"    11  astore_1\n" +
			"    12  fconst_1\n" +
			"    13  freturn\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 9] -> 11 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 31]\n" +
			"        [pc: 9, line: 32]\n" +
			"        [pc: 11, line: 34]\n" +
			"        [pc: 12, line: 36]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
			"        [pc: 2, pc: 11] local: i index: 1 type: int\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 11, same_locals_1_stack_item, stack: {java.lang.Exception}]\n" +
			"        [pc: 12, same]\n" +
			"  \n" +
			"  // Method descriptor #31 ()D\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  double bar();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 16\n" +
			"     7  aload_0 [this]\n" +
			"     8  invokevirtual X.bool() : boolean [17]\n" +
			"    11  ifeq 31\n" +
			"    14  dconst_0\n" +
			"    15  dreturn\n" +
			"    16  aload_0 [this]\n" +
			"    17  invokevirtual X.bool() : boolean [17]\n" +
			"    20  ifeq 31\n" +
			"    23  new java.lang.NullPointerException [32]\n" +
			"    26  dup\n" +
			"    27  invokespecial java.lang.NullPointerException() [34]\n" +
			"    30  athrow\n" +
			"    31  dconst_1\n" +
			"    32  dreturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 39]\n" +
			"        [pc: 7, line: 40]\n" +
			"        [pc: 14, line: 41]\n" +
			"        [pc: 16, line: 43]\n" +
			"        [pc: 23, line: 44]\n" +
			"        [pc: 31, line: 47]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 33] local: this index: 0 type: X\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 16, same]\n" +
			"        [pc: 31, same]\n" +
			"  \n" +
			"  // Method descriptor #36 (I)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  void baz(int i);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 32\n" +
			"     7  iload_1 [i]\n" +
			"     8  tableswitch default: 29\n" +
			"          case 0: 28\n" +
			"    28  return\n" +
			"    29  goto 37\n" +
			"    32  aload_0 [this]\n" +
			"    33  invokevirtual X.bool() : boolean [17]\n" +
			"    36  pop\n" +
			"    37  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 50]\n" +
			"        [pc: 7, line: 51]\n" +
			"        [pc: 28, line: 52]\n" +
			"        [pc: 29, line: 55]\n" +
			"        [pc: 32, line: 56]\n" +
			"        [pc: 37, line: 58]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 38] local: this index: 0 type: X\n" +
			"        [pc: 0, pc: 38] local: i index: 1 type: int\n" +
			"      Stack map table: number of frames 4\n" +
			"        [pc: 28, same]\n" +
			"        [pc: 29, same]\n" +
			"        [pc: 32, same]\n" +
			"        [pc: 37, same]\n";
	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894 - variation
public void test049() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	boolean bool() { return true; }\n" +
				"	void foo() {\n" +
				"		try {\n" +
				"			if (bool()) {\n" +
				"				throw new NullPointerException();\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"	}\n" +
				"	void foo2() {\n" +
				"		try {\n" +
				"			while (bool()) {\n" +
				"				throw new NullPointerException();\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"	}\n" +
				"	void foo3() {\n" +
				"		try {\n" +
				"			do {\n" +
				"				if (true) throw new NullPointerException();\n" +
				"			} while (bool());\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"	}	\n" +
				"	void foo4() {\n" +
				"		try {\n" +
				"			for (int i  = 0; bool(); i++) {\n" +
				"				throw new NullPointerException();\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"	}		\n" +
				"	void bar() {\n" +
				"		if (bool()) {\n" +
				"			if (bool())\n" +
				"				throw new NullPointerException();\n" +
				"		} else {\n" +
				"			if (bool()) {\n" +
				"				throw new NullPointerException();\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"	void baz(int i) {\n" +
				"		if (bool()) {\n" +
				"			switch(i) {\n" +
				"				case 0 : throw new NullPointerException();\n" +
				"				default : break;\n" +
				"			}\n" +
				"		} else {\n" +
				"			bool();\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  void foo();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 16\n" +
			"     7  new java.lang.NullPointerException [19]\n" +
			"    10  dup\n" +
			"    11  invokespecial java.lang.NullPointerException() [21]\n" +
			"    14  athrow\n" +
			"    15  astore_1\n" +
			"    16  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 15] -> 15 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 7, line: 6]\n" +
			"        [pc: 15, line: 8]\n" +
			"        [pc: 16, line: 10]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 17] local: this index: 0 type: X\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  void foo2();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 16\n" +
			"     7  new java.lang.NullPointerException [19]\n" +
			"    10  dup\n" +
			"    11  invokespecial java.lang.NullPointerException() [21]\n" +
			"    14  athrow\n" +
			"    15  astore_1\n" +
			"    16  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 15] -> 15 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 13]\n" +
			"        [pc: 7, line: 14]\n" +
			"        [pc: 15, line: 16]\n" +
			"        [pc: 16, line: 18]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 17] local: this index: 0 type: X\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  void foo3();\n" +
			"     0  new java.lang.NullPointerException [19]\n" +
			"     3  dup\n" +
			"     4  invokespecial java.lang.NullPointerException() [21]\n" +
			"     7  athrow\n" +
			"     8  astore_1\n" +
			"     9  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 8] -> 8 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 22]\n" +
			"        [pc: 8, line: 24]\n" +
			"        [pc: 9, line: 26]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 10] local: this index: 0 type: X\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  void foo4();\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [i]\n" +
			"     2  aload_0 [this]\n" +
			"     3  invokevirtual X.bool() : boolean [17]\n" +
			"     6  ifeq 18\n" +
			"     9  new java.lang.NullPointerException [19]\n" +
			"    12  dup\n" +
			"    13  invokespecial java.lang.NullPointerException() [21]\n" +
			"    16  athrow\n" +
			"    17  astore_1\n" +
			"    18  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 17] -> 17 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 29]\n" +
			"        [pc: 9, line: 30]\n" +
			"        [pc: 17, line: 32]\n" +
			"        [pc: 18, line: 34]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 19] local: this index: 0 type: X\n" +
			"        [pc: 2, pc: 17] local: i index: 1 type: int\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  void bar();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 22\n" +
			"     7  aload_0 [this]\n" +
			"     8  invokevirtual X.bool() : boolean [17]\n" +
			"    11  ifeq 37\n" +
			"    14  new java.lang.NullPointerException [19]\n" +
			"    17  dup\n" +
			"    18  invokespecial java.lang.NullPointerException() [21]\n" +
			"    21  athrow\n" +
			"    22  aload_0 [this]\n" +
			"    23  invokevirtual X.bool() : boolean [17]\n" +
			"    26  ifeq 37\n" +
			"    29  new java.lang.NullPointerException [19]\n" +
			"    32  dup\n" +
			"    33  invokespecial java.lang.NullPointerException() [21]\n" +
			"    36  athrow\n" +
			"    37  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 36]\n" +
			"        [pc: 7, line: 37]\n" +
			"        [pc: 14, line: 38]\n" +
			"        [pc: 22, line: 40]\n" +
			"        [pc: 29, line: 41]\n" +
			"        [pc: 37, line: 44]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 38] local: this index: 0 type: X\n" +
			"  \n" +
			"  // Method descriptor #31 (I)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  void baz(int i);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 39\n" +
			"     7  iload_1 [i]\n" +
			"     8  tableswitch default: 36\n" +
			"          case 0: 28\n" +
			"    28  new java.lang.NullPointerException [19]\n" +
			"    31  dup\n" +
			"    32  invokespecial java.lang.NullPointerException() [21]\n" +
			"    35  athrow\n" +
			"    36  goto 44\n" +
			"    39  aload_0 [this]\n" +
			"    40  invokevirtual X.bool() : boolean [17]\n" +
			"    43  pop\n" +
			"    44  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 46]\n" +
			"        [pc: 7, line: 47]\n" +
			"        [pc: 28, line: 48]\n" +
			"        [pc: 36, line: 51]\n" +
			"        [pc: 39, line: 52]\n" +
			"        [pc: 44, line: 54]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 45] local: this index: 0 type: X\n" +
			"        [pc: 0, pc: 45] local: i index: 1 type: int\n"
		:
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  void foo();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 16\n" +
			"     7  new java.lang.NullPointerException [19]\n" +
			"    10  dup\n" +
			"    11  invokespecial java.lang.NullPointerException() [21]\n" +
			"    14  athrow\n" +
			"    15  astore_1\n" +
			"    16  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 15] -> 15 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 7, line: 6]\n" +
			"        [pc: 15, line: 8]\n" +
			"        [pc: 16, line: 10]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 17] local: this index: 0 type: X\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 15, same_locals_1_stack_item, stack: {java.lang.Exception}]\n" +
			"        [pc: 16, same]\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  void foo2();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 16\n" +
			"     7  new java.lang.NullPointerException [19]\n" +
			"    10  dup\n" +
			"    11  invokespecial java.lang.NullPointerException() [21]\n" +
			"    14  athrow\n" +
			"    15  astore_1\n" +
			"    16  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 15] -> 15 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 13]\n" +
			"        [pc: 7, line: 14]\n" +
			"        [pc: 15, line: 16]\n" +
			"        [pc: 16, line: 18]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 17] local: this index: 0 type: X\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 15, same_locals_1_stack_item, stack: {java.lang.Exception}]\n" +
			"        [pc: 16, same]\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  void foo3();\n" +
			"     0  new java.lang.NullPointerException [19]\n" +
			"     3  dup\n" +
			"     4  invokespecial java.lang.NullPointerException() [21]\n" +
			"     7  athrow\n" +
			"     8  astore_1\n" +
			"     9  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 8] -> 8 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 22]\n" +
			"        [pc: 8, line: 24]\n" +
			"        [pc: 9, line: 26]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 10] local: this index: 0 type: X\n" +
			"      Stack map table: number of frames 1\n" +
			"        [pc: 8, same_locals_1_stack_item, stack: {java.lang.Exception}]\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  void foo4();\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [i]\n" +
			"     2  aload_0 [this]\n" +
			"     3  invokevirtual X.bool() : boolean [17]\n" +
			"     6  ifeq 18\n" +
			"     9  new java.lang.NullPointerException [19]\n" +
			"    12  dup\n" +
			"    13  invokespecial java.lang.NullPointerException() [21]\n" +
			"    16  athrow\n" +
			"    17  astore_1\n" +
			"    18  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 17] -> 17 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 29]\n" +
			"        [pc: 9, line: 30]\n" +
			"        [pc: 17, line: 32]\n" +
			"        [pc: 18, line: 34]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 19] local: this index: 0 type: X\n" +
			"        [pc: 2, pc: 17] local: i index: 1 type: int\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 17, same_locals_1_stack_item, stack: {java.lang.Exception}]\n" +
			"        [pc: 18, same]\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  void bar();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 22\n" +
			"     7  aload_0 [this]\n" +
			"     8  invokevirtual X.bool() : boolean [17]\n" +
			"    11  ifeq 37\n" +
			"    14  new java.lang.NullPointerException [19]\n" +
			"    17  dup\n" +
			"    18  invokespecial java.lang.NullPointerException() [21]\n" +
			"    21  athrow\n" +
			"    22  aload_0 [this]\n" +
			"    23  invokevirtual X.bool() : boolean [17]\n" +
			"    26  ifeq 37\n" +
			"    29  new java.lang.NullPointerException [19]\n" +
			"    32  dup\n" +
			"    33  invokespecial java.lang.NullPointerException() [21]\n" +
			"    36  athrow\n" +
			"    37  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 36]\n" +
			"        [pc: 7, line: 37]\n" +
			"        [pc: 14, line: 38]\n" +
			"        [pc: 22, line: 40]\n" +
			"        [pc: 29, line: 41]\n" +
			"        [pc: 37, line: 44]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 38] local: this index: 0 type: X\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 22, same]\n" +
			"        [pc: 37, same]\n" +
			"  \n" +
			"  // Method descriptor #32 (I)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  void baz(int i);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 39\n" +
			"     7  iload_1 [i]\n" +
			"     8  tableswitch default: 36\n" +
			"          case 0: 28\n" +
			"    28  new java.lang.NullPointerException [19]\n" +
			"    31  dup\n" +
			"    32  invokespecial java.lang.NullPointerException() [21]\n" +
			"    35  athrow\n" +
			"    36  goto 44\n" +
			"    39  aload_0 [this]\n" +
			"    40  invokevirtual X.bool() : boolean [17]\n" +
			"    43  pop\n" +
			"    44  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 46]\n" +
			"        [pc: 7, line: 47]\n" +
			"        [pc: 28, line: 48]\n" +
			"        [pc: 36, line: 51]\n" +
			"        [pc: 39, line: 52]\n" +
			"        [pc: 44, line: 54]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 45] local: this index: 0 type: X\n" +
			"        [pc: 0, pc: 45] local: i index: 1 type: int\n" +
			"      Stack map table: number of frames 4\n" +
			"        [pc: 28, same]\n" +
			"        [pc: 36, same]\n" +
			"        [pc: 39, same]\n" +
			"        [pc: 44, same]\n";
	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894 - variation
public void test050() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	boolean bool() { return true; }\n" +
				"	void foo() {\n" +
				"		check: try {\n" +
				"			if (bool()) {\n" +
				"				break check;\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"	}\n" +
				"	void foo2() {\n" +
				"		check: try {\n" +
				"			while (bool()) {\n" +
				"				break check;\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"	}\n" +
				"	void foo3() {\n" +
				"		check: try {\n" +
				"			do {\n" +
				"				if (true) break check;\n" +
				"			} while (bool());\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"	}	\n" +
				"	void foo4() {\n" +
				"		check: try {\n" +
				"			for (int i  = 0; bool(); i++) {\n" +
				"				break check;\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"	}\n" +
				"	void bar() {\n" +
				"		check: if (bool()) {\n" +
				"			if (bool())\n" +
				"				break check;\n" +
				"		} else {\n" +
				"			if (bool()) {\n" +
				"				break check;\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"	void baz(int i) {\n" +
				"		check: if (bool()) {\n" +
				"			switch(i) {\n" +
				"				case 0 : break check;\n" +
				"				default : break;\n" +
				"			}\n" +
				"		} else {\n" +
				"			bool();\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"  // Method descriptor #6 ()V\n" +
				"  // Stack: 1, Locals: 2\n" +
				"  void foo();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokevirtual X.bool() : boolean [17]\n" +
				"     4  ifeq 11\n" +
				"     7  goto 11\n" +
				"    10  astore_1\n" +
				"    11  return\n" +
				"      Exception Table:\n" +
				"        [pc: 0, pc: 7] -> 10 when : java.lang.Exception\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 5]\n" +
				"        [pc: 7, line: 6]\n" +
				"        [pc: 10, line: 8]\n" +
				"        [pc: 11, line: 10]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 12] local: this index: 0 type: X\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 1, Locals: 2\n" +
				"  void foo2();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokevirtual X.bool() : boolean [17]\n" +
				"     4  ifeq 11\n" +
				"     7  goto 11\n" +
				"    10  astore_1\n" +
				"    11  return\n" +
				"      Exception Table:\n" +
				"        [pc: 0, pc: 7] -> 10 when : java.lang.Exception\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 13]\n" +
				"        [pc: 7, line: 14]\n" +
				"        [pc: 10, line: 16]\n" +
				"        [pc: 11, line: 18]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 12] local: this index: 0 type: X\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 0, Locals: 1\n" +
				"  void foo3();\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 26]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 1, Locals: 2\n" +
				"  void foo4();\n" +
				"     0  iconst_0\n" +
				"     1  istore_1 [i]\n" +
				"     2  aload_0 [this]\n" +
				"     3  invokevirtual X.bool() : boolean [17]\n" +
				"     6  ifeq 13\n" +
				"     9  goto 13\n" +
				"    12  astore_1\n" +
				"    13  return\n" +
				"      Exception Table:\n" +
				"        [pc: 0, pc: 9] -> 12 when : java.lang.Exception\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 29]\n" +
				"        [pc: 9, line: 30]\n" +
				"        [pc: 12, line: 32]\n" +
				"        [pc: 13, line: 34]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
				"        [pc: 2, pc: 12] local: i index: 1 type: int\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  void bar();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokevirtual X.bool() : boolean [17]\n" +
				"     4  ifeq 17\n" +
				"     7  aload_0 [this]\n" +
				"     8  invokevirtual X.bool() : boolean [17]\n" +
				"    11  ifeq 24\n" +
				"    14  goto 24\n" +
				"    17  aload_0 [this]\n" +
				"    18  invokevirtual X.bool() : boolean [17]\n" +
				"    21  ifeq 24\n" +
				"    24  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 36]\n" +
				"        [pc: 7, line: 37]\n" +
				"        [pc: 14, line: 38]\n" +
				"        [pc: 17, line: 40]\n" +
				"        [pc: 24, line: 44]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 25] local: this index: 0 type: X\n" +
				"  \n" +
				"  // Method descriptor #28 (I)V\n" +
				"  // Stack: 1, Locals: 2\n" +
				"  void baz(int i);\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokevirtual X.bool() : boolean [17]\n" +
				"     4  ifeq 34\n" +
				"     7  iload_1 [i]\n" +
				"     8  tableswitch default: 31\n" +
				"          case 0: 28\n" +
				"    28  goto 39\n" +
				"    31  goto 39\n" +
				"    34  aload_0 [this]\n" +
				"    35  invokevirtual X.bool() : boolean [17]\n" +
				"    38  pop\n" +
				"    39  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 46]\n" +
				"        [pc: 7, line: 47]\n" +
				"        [pc: 28, line: 48]\n" +
				"        [pc: 31, line: 51]\n" +
				"        [pc: 34, line: 52]\n" +
				"        [pc: 39, line: 54]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 40] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 40] local: i index: 1 type: int\n"
		:
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  void foo();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 11\n" +
			"     7  goto 11\n" +
			"    10  astore_1\n" +
			"    11  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 7] -> 10 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 7, line: 6]\n" +
			"        [pc: 10, line: 8]\n" +
			"        [pc: 11, line: 10]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 12] local: this index: 0 type: X\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 10, same_locals_1_stack_item, stack: {java.lang.Exception}]\n" +
			"        [pc: 11, same]\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  void foo2();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 11\n" +
			"     7  goto 11\n" +
			"    10  astore_1\n" +
			"    11  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 7] -> 10 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 13]\n" +
			"        [pc: 7, line: 14]\n" +
			"        [pc: 10, line: 16]\n" +
			"        [pc: 11, line: 18]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 12] local: this index: 0 type: X\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 10, same_locals_1_stack_item, stack: {java.lang.Exception}]\n" +
			"        [pc: 11, same]\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  void foo3();\n" +
			"    0  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 26]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  void foo4();\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [i]\n" +
			"     2  aload_0 [this]\n" +
			"     3  invokevirtual X.bool() : boolean [17]\n" +
			"     6  ifeq 13\n" +
			"     9  goto 13\n" +
			"    12  astore_1\n" +
			"    13  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 9] -> 12 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 29]\n" +
			"        [pc: 9, line: 30]\n" +
			"        [pc: 12, line: 32]\n" +
			"        [pc: 13, line: 34]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
			"        [pc: 2, pc: 12] local: i index: 1 type: int\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 12, same_locals_1_stack_item, stack: {java.lang.Exception}]\n" +
			"        [pc: 13, same]\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  void bar();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 17\n" +
			"     7  aload_0 [this]\n" +
			"     8  invokevirtual X.bool() : boolean [17]\n" +
			"    11  ifeq 24\n" +
			"    14  goto 24\n" +
			"    17  aload_0 [this]\n" +
			"    18  invokevirtual X.bool() : boolean [17]\n" +
			"    21  ifeq 24\n" +
			"    24  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 36]\n" +
			"        [pc: 7, line: 37]\n" +
			"        [pc: 14, line: 38]\n" +
			"        [pc: 17, line: 40]\n" +
			"        [pc: 24, line: 44]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 25] local: this index: 0 type: X\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 17, same]\n" +
			"        [pc: 24, same]\n" +
			"  \n" +
			"  // Method descriptor #29 (I)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  void baz(int i);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokevirtual X.bool() : boolean [17]\n" +
			"     4  ifeq 34\n" +
			"     7  iload_1 [i]\n" +
			"     8  tableswitch default: 31\n" +
			"          case 0: 28\n" +
			"    28  goto 39\n" +
			"    31  goto 39\n" +
			"    34  aload_0 [this]\n" +
			"    35  invokevirtual X.bool() : boolean [17]\n" +
			"    38  pop\n" +
			"    39  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 46]\n" +
			"        [pc: 7, line: 47]\n" +
			"        [pc: 28, line: 48]\n" +
			"        [pc: 31, line: 51]\n" +
			"        [pc: 34, line: 52]\n" +
			"        [pc: 39, line: 54]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 40] local: this index: 0 type: X\n" +
			"        [pc: 0, pc: 40] local: i index: 1 type: int\n" +
			"      Stack map table: number of frames 4\n" +
			"        [pc: 28, same]\n" +
			"        [pc: 31, same]\n" +
			"        [pc: 34, same]\n" +
			"        [pc: 39, same]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894 - variation
public void test051() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String argv[]) {\n" +
				"		System.out.println(\"[count=\" + count() + \"]\");\n" +
				"	}\n" +
				"	static int count() {\n" +
				"		int count = 0;\n" +
				"		try {\n" +
				"			for (int i = 0;;) {\n" +
				"				count++;\n" +
				"				if (i++ > 10) \n" +
				"					break; \n" +
				"			}\n" +
				"		} catch(Exception e) {\n" +
				"		}\n" +
				"		return count;\n" +
				"	}\n" +
				"}\n",
			},
			"[count=12]");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"  // Method descriptor #32 ()I\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  static int count();\n" +
			"     0  iconst_0\n" +
			"     1  istore_0 [count]\n" +
			"     2  iconst_0\n" +
			"     3  istore_1 [i]\n" +
			"     4  iinc 0 1 [count]\n" +
			"     7  iload_1 [i]\n" +
			"     8  iinc 1 1 [i]\n" +
			"    11  bipush 10\n" +
			"    13  if_icmple 4\n" +
			"    16  goto 20\n" +
			"    19  astore_1\n" +
			"    20  iload_0 [count]\n" +
			"    21  ireturn\n" +
			"      Exception Table:\n" +
			"        [pc: 2, pc: 16] -> 19 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"        [pc: 2, line: 8]\n" +
			"        [pc: 4, line: 9]\n" +
			"        [pc: 7, line: 10]\n" +
			"        [pc: 16, line: 13]\n" +
			"        [pc: 20, line: 15]\n" +
			"      Local variable table:\n" +
			"        [pc: 2, pc: 22] local: count index: 0 type: int\n" +
			"        [pc: 4, pc: 16] local: i index: 1 type: int\n"
		:
			"  // Method descriptor #32 ()I\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  static int count();\n" +
			"     0  iconst_0\n" +
			"     1  istore_0 [count]\n" +
			"     2  iconst_0\n" +
			"     3  istore_1 [i]\n" +
			"     4  iinc 0 1 [count]\n" +
			"     7  iload_1 [i]\n" +
			"     8  iinc 1 1 [i]\n" +
			"    11  bipush 10\n" +
			"    13  if_icmple 4\n" +
			"    16  goto 20\n" +
			"    19  astore_1\n" +
			"    20  iload_0 [count]\n" +
			"    21  ireturn\n" +
			"      Exception Table:\n" +
			"        [pc: 2, pc: 16] -> 19 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"        [pc: 2, line: 8]\n" +
			"        [pc: 4, line: 9]\n" +
			"        [pc: 7, line: 10]\n" +
			"        [pc: 16, line: 13]\n" +
			"        [pc: 20, line: 15]\n" +
			"      Local variable table:\n" +
			"        [pc: 2, pc: 22] local: count index: 0 type: int\n" +
			"        [pc: 4, pc: 16] local: i index: 1 type: int\n" +
			"      Stack map table: number of frames 3\n" +
			"        [pc: 4, append: {int, int}]\n" +
			"        [pc: 19, full, stack: {java.lang.Exception}, locals: {int}]\n" +
			"        [pc: 20, same]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894 - variation
public void test052() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String argv[]) {\n" +
				"		try {\n" +
				"			for (int i = 0; i < 0; i++)\n" +
				"				do ;  while (true);\n" +
				"		} catch(Exception e) {\n" +
				"		}\n" +
				"	} \n" +
				"}\n",
			},
			"");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] argv);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [i]\n" +
			"     2  iload_1 [i]\n" +
			"     3  ifge 10\n" +
			"     6  goto 6\n" +
			"     9  astore_1\n" +
			"    10  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 9] -> 9 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"        [pc: 6, line: 5]\n" +
			"        [pc: 9, line: 6]\n" +
			"        [pc: 10, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 11] local: argv index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 9] local: i index: 1 type: int\n"
		:
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] argv);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [i]\n" +
			"     2  iload_1 [i]\n" +
			"     3  ifge 10\n" +
			"     6  goto 6\n" +
			"     9  astore_1\n" +
			"    10  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 9] -> 9 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"        [pc: 6, line: 5]\n" +
			"        [pc: 9, line: 6]\n" +
			"        [pc: 10, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 11] local: argv index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 9] local: i index: 1 type: int\n" +
			"      Stack map table: number of frames 3\n" +
			"        [pc: 6, append: {int}]\n" +
			"        [pc: 9, full, stack: {java.lang.Exception}, locals: {java.lang.String[]}]\n" +
			"        [pc: 10, same]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894 - variation
public void test053() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			final int val;\n" +
				"			for (val = 7; val > 0;) break;\n" +
				"			System.out.println(val);\n" +
				"		} catch(Exception e) {\n" +
				"		}\n" +
				"	}	\n" +
				"}\n",
			},
			"7");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 7\n" +
			"     2  istore_1 [val]\n" +
			"     3  iload_1 [val]\n" +
			"     4  ifle 7\n" +
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    10  iload_1 [val]\n" +
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    14  goto 18\n" +
			"    17  astore_1\n" +
			"    18  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 14] -> 17 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 7, line: 6]\n" +
			"        [pc: 14, line: 7]\n" +
			"        [pc: 18, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 19] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 14] local: val index: 1 type: int\n"
		:
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 7\n" +
			"     2  istore_1 [val]\n" +
			"     3  iload_1 [val]\n" +
			"     4  ifle 7\n" +
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    10  iload_1 [val]\n" +
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    14  goto 18\n" +
			"    17  astore_1\n" +
			"    18  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 14] -> 17 when : java.lang.Exception\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 7, line: 6]\n" +
			"        [pc: 14, line: 7]\n" +
			"        [pc: 18, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 19] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 14] local: val index: 1 type: int\n" +
			"      Stack map table: number of frames 3\n" +
			"        [pc: 7, append: {int}]\n" +
			"        [pc: 17, full, stack: {java.lang.Exception}, locals: {java.lang.String[]}]\n" +
			"        [pc: 18, same]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894 - variation
public void test054() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				" X parent;\n" +
				" int kind;\n" +
				" static boolean F = false;\n" +
				" public static void main(String[] args) {\n" +
				"  X x = new X();\n" +
				"  x.kind = 2; \n" +
				"  try {\n" +
				"   x.foo();\n" +
				"  } catch(NullPointerException e) { \n" +
				"   System.out.println(\"SUCCESS\");\n" +
				"   return;\n" +
				"  }\n" +
				"  System.out.println(\"FAILED\");  \n" +
				" }\n" +
				" void foo() {\n" +
				"  X x = this;\n" +
				"  done : while (true) {\n" +
				"   switch (x.kind) {\n" +
				"    case 2 :\n" +
				"     if (F) {\n" +
				"      return;\n" +
				"     }\n" +
				"     break;\n" +
				"    case 3 :\n" +
				"     break done;\n" +
				"   }\n" +
				"   x = x.parent; // should throw npe\n" +
				"  }\n" +
				" } \n" +
				"}\n",
			},
			"SUCCESS");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"  // Method descriptor #12 ()V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  void foo();\n" +
			"     0  aload_0 [this]\n" +
			"     1  astore_1 [x]\n" +
			"     2  aload_1 [x]\n" +
			"     3  getfield X.kind : int [25]\n" +
			"     6  tableswitch default: 38\n" +
			"          case 2: 28\n" +
			"          case 3: 35\n" +
			"    28  getstatic X.F : boolean [14]\n" +
			"    31  ifeq 38\n" +
			"    34  return\n" +
			"    35  goto 46\n" +
			"    38  aload_1 [x]\n" +
			"    39  getfield X.parent : X [53]\n" +
			"    42  astore_1 [x]\n" +
			"    43  goto 2\n" +
			"    46  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 17]\n" +
			"        [pc: 2, line: 19]\n" +
			"        [pc: 28, line: 21]\n" +
			"        [pc: 34, line: 22]\n" +
			"        [pc: 35, line: 26]\n" +
			"        [pc: 38, line: 27]\n" +
			"        [pc: 39, line: 28]\n" +
			"        [pc: 43, line: 18]\n" +
			"        [pc: 46, line: 30]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 47] local: this index: 0 type: X\n" +
			"        [pc: 2, pc: 47] local: x index: 1 type: X\n"
		:
			"  // Method descriptor #12 ()V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  void foo();\n" +
			"     0  aload_0 [this]\n" +
			"     1  astore_1 [x]\n" +
			"     2  aload_1 [x]\n" +
			"     3  getfield X.kind : int [25]\n" +
			"     6  tableswitch default: 38\n" +
			"          case 2: 28\n" +
			"          case 3: 35\n" +
			"    28  getstatic X.F : boolean [14]\n" +
			"    31  ifeq 38\n" +
			"    34  return\n" +
			"    35  goto 46\n" +
			"    38  aload_1 [x]\n" +
			"    39  getfield X.parent : X [55]\n" +
			"    42  astore_1 [x]\n" +
			"    43  goto 2\n" +
			"    46  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 17]\n" +
			"        [pc: 2, line: 19]\n" +
			"        [pc: 28, line: 21]\n" +
			"        [pc: 34, line: 22]\n" +
			"        [pc: 35, line: 26]\n" +
			"        [pc: 38, line: 27]\n" +
			"        [pc: 39, line: 28]\n" +
			"        [pc: 43, line: 18]\n" +
			"        [pc: 46, line: 30]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 47] local: this index: 0 type: X\n" +
			"        [pc: 2, pc: 47] local: x index: 1 type: X\n" +
			"      Stack map table: number of frames 5\n" +
			"        [pc: 2, append: {X}]\n" +
			"        [pc: 28, same]\n" +
			"        [pc: 35, same]\n" +
			"        [pc: 38, same]\n" +
			"        [pc: 46, same]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114894 - variation
public void test055() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"void foo5() {\n" +
				"  L : for (;;) {\n" +
				"    continue L; // good\n" +
				"  }\n" +
				"}\n" +
				"}\n",
			},
			"");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  void foo5();\n" +
			"    0  goto 0\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 3] local: this index: 0 type: X\n"
		:
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  void foo5();\n" +
			"    0  goto 0\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 3] local: this index: 0 type: X\n" +
			"      Stack map table: number of frames 1\n" +
			"        [pc: 0, same]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
public void _test056() {
	this.runNegativeTest(
		new String[] {
			"p/BytecodeA.java",
			"package p;\n" +
			"class BytecodeA {\n" +
			"  \n" +
			"int foo() { // good\n" +
			"  boolean b = true;\n" +
			"  if (b) {\n" +
			"    if (true)\n" +
			"      return 0;\n" +
			"  } else {\n" +
			"    if (true)\n" +
			"      return 1;\n" +
			"  }\n" +
			"  return 5;\n" +
			"}\n" +
			"int foo10() {\n" +
			"  try {\n" +
			"    //if (true)\n" +
			"      return 0;\n" +
			"  } catch (Exception e) {\n" +
			"    if (true)\n" +
			"      return 1;\n" +
			"  } finally {\n" +
			"    if (true)\n" +
			"      return 2;\n" +
			"  };\n" +
			"  return 1;\n" +
			"}   \n" +
			"int foo11() {\n" +
			"  synchronized (this) {\n" +
			"    if (true)\n" +
			"      return 1;\n" +
			"  };\n" +
			"  return 2;\n" +
			"} \n" +
			"int foo12() {\n" +
			"  for (;;)\n" +
			"    return 1;\n" +
			"}\n" +
			"int foo13() {\n" +
			"  for (;;)\n" +
			"    if (true)\n" +
			"      return 1;\n" +
			"}\n" +
			"int foo14() {\n" +
			"  for (int i = 1; i < 10; i++)\n" +
			"    if (true)\n" +
			"      return 1;\n" +
			"  return 2;\n" +
			"} \n" +
			"int foo15() {\n" +
			"  for (int i = 1; i < 10; i++)\n" +
			"    return 1;\n" +
			"  return 2;\n" +
			"}\n" +
			"int foo16() {\n" +
			"  final int i;\n" +
			"  while (true) {\n" +
			"    i = 1;\n" +
			"    if (true)\n" +
			"      break;\n" +
			"  };\n" +
			"  return 1;\n" +
			"}              \n" +
			"int foo17() {\n" +
			"  final int i;\n" +
			"  for (;;) {\n" +
			"    i = 1;\n" +
			"    if (true)\n" +
			"      break;\n" +
			"  };\n" +
			"  return 1;\n" +
			"} \n" +
			"void foo2() {\n" +
			"  L1 :;  // good\n" +
			"}\n" +
			"void foo20() {\n" +
			"  if (true)\n" +
			"    return;\n" +
			"} \n" +
			"void foo3() {\n" +
			"  L : if (true) {\n" +
			"    for (;;) {\n" +
			"      continue L; // bad\n" +
			"    }\n" +
			"  }\n" +
			"}   \n" +
			"void foo4() {\n" +
			"  L : if (true) {\n" +
			"    try {\n" +
			"      for (;;) {\n" +
			"        continue L; // bad\n" +
			"      }\n" +
			"    } finally {\n" +
			"      return;\n" +
			"    }\n" +
			"  } \n" +
			"}\n" +
			"void foo5() {\n" +
			"  L : for (;;) {\n" +
			"    continue L; // good\n" +
			"  }\n" +
			"}\n" +
			"void foo5bis() {\n" +
			"  L : K : for (;;) {\n" +
			"    continue L; // good\n" +
			"  }\n" +
			"}\n" +
			"void foo6(){\n" +
			"  int i;\n" +
			"  boolean a[] = new boolean[5];\n" +
			"  a[i=1] = i > 0; // good\n" +
			"}    \n" +
			"void foo7(){\n" +
			"  Object x[];\n" +
			"  x [1] = (x = new Object[5]); // bad\n" +
			"}    \n" +
			"void foo8() {\n" +
			"  try {\n" +
			"  } catch (java.io.IOException e) {\n" +
			"    foo(); // unreachable\n" +
			"  }\n" +
			"}\n" +
			"void foo9() {\n" +
			"  try {\n" +
			"  } catch (NullPointerException e) {\n" +
			"    foo(); // ok\n" +
			"  }\n" +
			"}\n" +
			"    public static void main(String args[]) {\n" +
			"      BytecodeA a = new BytecodeA();\n" +
			"      a.foo10();\n" +
			"    }\n" +
			"}",
		},
		"----------\n" +
		"1. WARNING in p\\BytecodeA.java (at line 74)\n" +
		"	L1 :;  // good\n" +
		"	^^\n" +
		"The label L1 is never explicitly referenced\n" +
		"----------\n" +
		"2. ERROR in p\\BytecodeA.java (at line 83)\n" +
		"	continue L; // bad\n" +
		"	^^^^^^^^^^\n" +
		"continue cannot be used outside of a loop\n" +
		"----------\n" +
		"3. ERROR in p\\BytecodeA.java (at line 91)\n" +
		"	continue L; // bad\n" +
		"	^^^^^^^^^^\n" +
		"continue cannot be used outside of a loop\n" +
		"----------\n" +
		"4. WARNING in p\\BytecodeA.java (at line 93)\n" +
		"	} finally {\n" +
		"      return;\n" +
		"    }\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^^\n" +
		"finally block does not complete normally\n" +
		"----------\n" +
		"5. WARNING in p\\BytecodeA.java (at line 104)\n" +
		"	L : K : for (;;) {\n" +
		"	    ^\n" +
		"The label K is never explicitly referenced\n" +
		"----------\n" +
		"6. ERROR in p\\BytecodeA.java (at line 105)\n" +
		"	continue L; // good\n" +
		"	^^^^^^^^^^\n" +
		"continue cannot be used outside of a loop\n" +
		"----------\n" +
		"7. ERROR in p\\BytecodeA.java (at line 115)\n" +
		"	x [1] = (x = new Object[5]); // bad\n" +
		"	^\n" +
		"The local variable x may not have been initialized\n" +
		"----------\n" +
		"8. ERROR in p\\BytecodeA.java (at line 119)\n" +
		"	} catch (java.io.IOException e) {\n" +
		"	         ^^^^^^^^^^^^^^^^^^^\n" +
		"Unreachable catch block for IOException. This exception is never thrown from the try statement body\n" +
		"----------\n");
}

// was Compliance_1_x#test007
public void test057() {
	String[] sources = new String[] {
		"p1/Test.java",
		"package p1; \n"+
		"public class Test { \n"+
		"	public static void main(String[] arguments) { \n"+
		"		try {	\n" +
		"			throw null; \n"+
		"		} catch(NullPointerException e){ 	\n" +
		"			System.out.println(\"SUCCESS\");	\n"	+
		"		}	\n" +
		"	} \n"+
		"} \n"
	};
	if (this.complianceLevel == ClassFileConstants.JDK1_3) {
		runNegativeTest(
			sources,
			"----------\n" +
			"1. ERROR in p1\\Test.java (at line 5)\n" +
			"	throw null; \n" +
			"	      ^^^^\n" +
			"Cannot throw null as an exception\n" +
			"----------\n");
	} else {
		runConformTest(
			sources,
			"SUCCESS");
	}
}
//https://bugs.eclpse.org/bugs/show_bug.cgi?id=3184
public void test058() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String args[]) {\n" +
				"		try {\n" +
				"			try {\n" +
				"				System.out.print(\"SU\");\n" +
				"			} finally {\n" +
				"				System.out.print(\"CC\");\n" +
				"			}\n" +
				"		} finally {\n" +
				"			System.out.println(\"ESS\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			"SUCCESS");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel <= ClassFileConstants.JDK1_4
		?	"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 5\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     3  ldc <String \"SU\"> [22]\n" +
			"     5  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
			"     8  goto 28\n" +
			"    11  astore_2\n" +
			"    12  jsr 17\n" +
			"    15  aload_2\n" +
			"    16  athrow\n" +
			"    17  astore_1\n" +
			"    18  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    21  ldc <String \"CC\"> [30]\n" +
			"    23  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
			"    26  ret 1\n" +
			"    28  jsr 17\n" +
			"    31  goto 53\n" +
			"    34  astore 4\n" +
			"    36  jsr 42\n" +
			"    39  aload 4\n" +
			"    41  athrow\n" +
			"    42  astore_3\n" +
			"    43  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    46  ldc <String \"ESS\"> [32]\n" +
			"    48  invokevirtual java.io.PrintStream.println(java.lang.String) : void [34]\n" +
			"    51  ret 3\n" +
			"    53  jsr 42\n" +
			"    56  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 11] -> 11 when : any\n" +
			"        [pc: 28, pc: 31] -> 11 when : any\n" +
			"        [pc: 0, pc: 34] -> 34 when : any\n" +
			"        [pc: 53, pc: 56] -> 34 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 8, line: 6]\n" +
			"        [pc: 15, line: 8]\n" +
			"        [pc: 17, line: 6]\n" +
			"        [pc: 18, line: 7]\n" +
			"        [pc: 26, line: 8]\n" +
			"        [pc: 31, line: 9]\n" +
			"        [pc: 39, line: 11]\n" +
			"        [pc: 42, line: 9]\n" +
			"        [pc: 43, line: 10]\n" +
			"        [pc: 51, line: 11]\n" +
			"        [pc: 56, line: 12]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 57] local: args index: 0 type: java.lang.String[]\n"
		:
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     3  ldc <String \"SU\"> [22]\n" +
			"     5  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
			"     8  goto 22\n" +
			"    11  astore_1\n" +
			"    12  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    15  ldc <String \"CC\"> [30]\n" +
			"    17  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
			"    20  aload_1\n" +
			"    21  athrow\n" +
			"    22  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    25  ldc <String \"CC\"> [30]\n" +
			"    27  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
			"    30  goto 44\n" +
			"    33  astore_2\n" +
			"    34  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    37  ldc <String \"ESS\"> [32]\n" +
			"    39  invokevirtual java.io.PrintStream.println(java.lang.String) : void [34]\n" +
			"    42  aload_2\n" +
			"    43  athrow\n" +
			"    44  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    47  ldc <String \"ESS\"> [32]\n" +
			"    49  invokevirtual java.io.PrintStream.println(java.lang.String) : void [34]\n" +
			"    52  return\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 11] -> 11 when : any\n" +
			"        [pc: 0, pc: 33] -> 33 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 8, line: 6]\n" +
			"        [pc: 12, line: 7]\n" +
			"        [pc: 20, line: 8]\n" +
			"        [pc: 22, line: 7]\n" +
			"        [pc: 30, line: 9]\n" +
			"        [pc: 34, line: 10]\n" +
			"        [pc: 42, line: 11]\n" +
			"        [pc: 44, line: 10]\n" +
			"        [pc: 52, line: 12]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 53] local: args index: 0 type: java.lang.String[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=183395
public void test059() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"    	try {\n" +
			"    		System.out.println(args.length);\n" +
			"    	} catch(Exception[] e) {\n" +
			"    		// ignore\n" +
			"    	}\n" +
			"    }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	} catch(Exception[] e) {\n" +
		"	        ^^^^^^^^^^^\n" +
		"No exception of type Exception[] can be thrown; an exception type must be a subclass of Throwable\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=183395
public void test060() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"    	try {\n" +
			"    		System.out.println(args.length);\n" +
			"    	} catch(int e) {\n" +
			"    		// ignore\n" +
			"    	}\n" +
			"    }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	} catch(int e) {\n" +
		"	        ^^^\n" +
		"No exception of type int can be thrown; an exception type must be a subclass of Throwable\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=190209 - variation
public void test062() throws Exception {
	if (new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_5) return; // need autoboxing
	this.runConformTest(
			new String[] {
				"X.java",
				"final public class X {\n" +
				"	final class MyClass {\n" +
				"		/** @param s */\n" +
				"		void foo(final String s) {\n" +
				"			 /* do nothing */\n" +
				"		}\n" +
				"	}\n" +
				"	Object bar() {\n" +
				"		try {\n" +
				"			final MyClass myClass = new MyClass();\n" +
				"			try {\n" +
				"				return 0;\n" +
				"			} catch (final Throwable ex) {\n" +
				"				myClass.foo(this == null ? \"\" : \"\");\n" +
				"			}\n" +
				"	\n" +
				"			return this;\n" +
				"		} finally {\n" +
				"			{ /* do nothing */ }\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().bar();\n" +
				"		System.out.print(\"SUCCESS\");\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");

	String expectedOutput =
			"  // Method descriptor #15 ()Ljava/lang/Object;\n" +
			"  // Stack: 3, Locals: 5\n" +
			"  java.lang.Object bar();\n" +
			"     0  new X$MyClass [16]\n" +
			"     3  dup\n" +
			"     4  aload_0 [this]\n" +
			"     5  invokespecial X$MyClass(X) [18]\n" +
			"     8  astore_1 [myClass]\n" +
			"     9  iconst_0\n" +
			"    10  invokestatic java.lang.Integer.valueOf(int) : java.lang.Integer [21]\n" +
			"    13  astore 4\n" +
			"    15  aload 4\n" +
			"    17  areturn\n" +
			"    18  astore_2 [ex]\n" +
			"    19  aload_1 [myClass]\n" +
			"    20  aload_0 [this]\n" +
			"    21  ifnonnull 29\n" +
			"    24  ldc <String \"\"> [27]\n" +
			"    26  goto 31\n" +
			"    29  ldc <String \"\"> [27]\n" +
			"    31  invokevirtual X$MyClass.foo(java.lang.String) : void [29]\n" +
			"    34  aload_0 [this]\n" +
			"    35  astore 4\n" +
			"    37  aload 4\n" +
			"    39  areturn\n" +
			"    40  astore_3\n" +
			"    41  aload_3\n" +
			"    42  athrow\n" +
			"      Exception Table:\n" +
			"        [pc: 9, pc: 15] -> 18 when : java.lang.Throwable\n" +
			"        [pc: 0, pc: 15] -> 40 when : any\n" +
			"        [pc: 18, pc: 37] -> 40 when : any\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=190209 - variation
public void test063() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"final public class X {\n" +
				"	final class MyClass {\n" +
				"		/** @param s */\n" +
				"		void foo(final String s) {\n" +
				"			 /* do nothing */\n" +
				"		}\n" +
				"	}\n" +
				"	void bar() {\n" +
				"		try {\n" +
				"			final MyClass myClass = new MyClass();\n" +
				"			try {\n" +
				"				return;\n" +
				"			} catch (final Throwable ex) {\n" +
				"				myClass.foo(this == null ? \"\" : \"\");\n" +
				"			}\n" +
				"			return;\n" +
				"		} finally {\n" +
				"			{ /* do nothing */ }\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().bar();\n" +
				"		System.out.print(\"SUCCESS\");\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel <= ClassFileConstants.JDK1_4
		?	"  // Method descriptor #6 ()V\n" +
			"  // Stack: 3, Locals: 5\n" +
			"  void bar();\n" +
			"     0  new X$MyClass [15]\n" +
			"     3  dup\n" +
			"     4  aload_0 [this]\n" +
			"     5  invokespecial X$MyClass(X) [17]\n" +
			"     8  astore_1 [myClass]\n" +
			"     9  jsr 21\n" +
			"    12  return\n" +
			"    13  astore 4\n" +
			"    15  jsr 21\n" +
			"    18  aload 4\n" +
			"    20  athrow\n" +
			"    21  astore_3\n" +
			"    22  ret 3\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 12] -> 13 when : any\n"
		:
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 3, Locals: 4\n" +
			"  void bar();\n" +
			"     0  new X$MyClass [15]\n" +
			"     3  dup\n" +
			"     4  aload_0 [this]\n" +
			"     5  invokespecial X$MyClass(X) [17]\n" +
			"     8  astore_1 [myClass]\n" +
			"     9  return\n" +
			"    10  return\n" +
			"    11  astore_3\n" +
			"    12  aload_3\n" +
			"    13  athrow\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 9] -> 11 when : any\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=190209 - variation
public void test064() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"final public class X {\n" +
				"	final class MyClass {\n" +
				"		/** @param s */\n" +
				"		void foo(final String s) {\n" +
				"			 /* do nothing */\n" +
				"		}\n" +
				"	}\n" +
				"	Object bar() {\n" +
				"		try {\n" +
				"			final MyClass myClass = new MyClass();\n" +
				"			try {\n" +
				"				return null;\n" +
				"			} catch (final Throwable ex) {\n" +
				"				myClass.foo(this == null ? \"\" : \"\");\n" +
				"			}\n" +
				"			return null;\n" +
				"		} finally {\n" +
				"			{ /* do nothing */ }\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().bar();\n" +
				"		System.out.print(\"SUCCESS\");\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel <= ClassFileConstants.JDK1_4
		?	"  // Method descriptor #15 ()Ljava/lang/Object;\n" +
			"  // Stack: 3, Locals: 5\n" +
			"  java.lang.Object bar();\n" +
			"     0  new X$MyClass [16]\n" +
			"     3  dup\n" +
			"     4  aload_0 [this]\n" +
			"     5  invokespecial X$MyClass(X) [18]\n" +
			"     8  astore_1 [myClass]\n" +
			"     9  jsr 22\n" +
			"    12  aconst_null\n" +
			"    13  areturn\n" +
			"    14  astore 4\n" +
			"    16  jsr 22\n" +
			"    19  aload 4\n" +
			"    21  athrow\n" +
			"    22  astore_3\n" +
			"    23  ret 3\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 12] -> 14 when : any\n"
		:	"  // Method descriptor #15 ()Ljava/lang/Object;\n" +
			"  // Stack: 3, Locals: 4\n" +
			"  java.lang.Object bar();\n" +
			"     0  new X$MyClass [16]\n" +
			"     3  dup\n" +
			"     4  aload_0 [this]\n" +
			"     5  invokespecial X$MyClass(X) [18]\n" +
			"     8  astore_1 [myClass]\n" +
			"     9  aconst_null\n" +
			"    10  areturn\n" +
			"    11  aconst_null\n" +
			"    12  areturn\n" +
			"    13  astore_3\n" +
			"    14  aload_3\n" +
			"    15  athrow\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 9] -> 13 when : any\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191865
public void test065() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo() {\n" +
			"		try {\n" +
			"			System.out.println(\"Hello\");\n" +
			"		} finally {\n" +
			"			if (true)\n" +
			"				return;\n" +
			"		}\n" +
			"		return;\n" +
			"	}\n" +
			"	void bar() {\n" +
			"		try {\n" +
			"			System.out.println(\"Hello\");\n" +
			"		} finally {\n" +
			"			return;\n" +
			"		}\n" +
			"		return;\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 14)\n" +
		"	} finally {\n" +
		"			return;\n" +
		"		}\n" +
		"	          ^^^^^^^^^^^^^^^^\n" +
		"finally block does not complete normally\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 17)\n" +
		"	return;\n" +
		"	^^^^^^^\n" +
		"Unreachable code\n" +
		"----------\n");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196653
public void test066() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void bar() {\n" +
			"		try {\n" +
			"			Zork z = null;\n" +
			"			z.foo();\n" +
			"		} catch(Zork z) {\n" +
			"			z.foo();\n" +
			"		}		\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	Zork z = null;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	} catch(Zork z) {\n" +
		"	        ^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=248319
public void test067() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(final String[] args) {\n" + 
				"		System.out.println(new X().verifyError());\n" + 
				"	}\n" + 
				"	private Object verifyError() {\n" + 
				"		try {\n" + 
				"			if (someBooleanMethod()) {\n" + 
				"				return null;\n" + 
				"			}\n" + 
				"			return getStuff();\n" + 
				"		} catch (final Exception ex) {\n" + 
				"			return null;\n" + 
				"		} finally {\n" + 
				"			while (someBooleanMethod()) {\n" + 
				"				anyMethod();\n" + 
				"			}\n" + 
				"		}\n" + 
				"	}\n" + 
				"	private void anyMethod() { /*empty*/ }\n" + 
				"	private Object getStuff() { return null; }\n" + 
				"	private boolean someBooleanMethod() { return false; }\n" + 
				"}\n" + 
				"",
			},
			"null");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340485
public void test068() {
	this.runConformTest(
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
					"        } catch(Foo e) {\n" +
					"            try { \n" +
					"                    throw e; \n" +
					"            } catch (SonOfFoo e1) {\n" +
					"                 e1.printStackTrace();\n" +
					"            } catch (DaughterOfFoo e1) {\n" +
					"                System.out.println(\"caught a daughter of foo\");\n" +
					"            } catch (Foo f) {}\n" +
					"        }\n" +
					"    }\n" +
					"}\n" +
					"class Foo extends Exception {}\n" +
					"class SonOfFoo extends Foo {}\n" +
					"class GrandSonOfFoo extends SonOfFoo {}\n" +
					"class DaughterOfFoo extends Foo {}\n" +
					"class GrandDaughterOfFoo extends DaughterOfFoo {}\n"
		}, 
		"caught a daughter of foo");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340484
public void test069() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        try {\n" +
			"            throw new DaughterOfFoo();\n" +
			"        } catch(Foo e) {\n" +
			"            try { \n" +
			"                while (true) {\n" +
			"                    throw e; \n" +
			"                }\n" +
			"            } catch (SonOfFoo e1) {\n" +
			"                 e1.printStackTrace();\n" +
			"            } catch (Foo e1) {}\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"class Foo extends Exception {}\n" +
			"class SonOfFoo extends Foo {}\n" +
			"class DaughterOfFoo extends Foo {}\n"
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
				"----------\n" + 
				"1. WARNING in X.java (at line 16)\n" + 
				"	class Foo extends Exception {}\n" + 
				"	      ^^^\n" + 
				"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 17)\n" + 
				"	class SonOfFoo extends Foo {}\n" + 
				"	      ^^^^^^^^\n" + 
				"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 18)\n" + 
				"	class DaughterOfFoo extends Foo {}\n" + 
				"	      ^^^^^^^^^^^^^\n" + 
				"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" + 
				"----------\n" :
				"----------\n" + 
				"1. ERROR in X.java (at line 10)\n" + 
				"	} catch (SonOfFoo e1) {\n" + 
				"	         ^^^^^^^^\n" + 
				"Unreachable catch block for SonOfFoo. This exception is never thrown from the try statement body\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 16)\n" + 
				"	class Foo extends Exception {}\n" + 
				"	      ^^^\n" + 
				"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 17)\n" + 
				"	class SonOfFoo extends Foo {}\n" + 
				"	      ^^^^^^^^\n" + 
				"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 18)\n" + 
				"	class DaughterOfFoo extends Foo {}\n" + 
				"	      ^^^^^^^^^^^^^\n" + 
				"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" + 
				"----------\n");
}
// precise throw computation should also take care of throws clause in 1.7. 1.6- should continue to behave as it always has.
public void test070() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void foo() throws DaughterOfFoo {\n" +
			"		try {\n" +
			"			throw new DaughterOfFoo();\n" +
			"		} catch (Foo e){\n" + 
			"			throw e;\n" +
			"           foo();\n" +
			"		}\n"+
			"	}\n"+
			"	public static void main(String[] args) {\n" + 
			"		try {\n" + 
			"			foo();\n"+
			"		} catch(Foo e) {}\n" + 
			"	}\n" + 
			"}\n"+
			"class Foo extends Exception {}\n"+
			"class SonOfFoo extends Foo {}\n"+
			"class DaughterOfFoo extends Foo {}\n"
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ? 
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	throw e;\n" + 
				"	^^^^^^^^\n" + 
				"Unhandled exception type Foo\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 7)\n" + 
				"	foo();\n" + 
				"	^^^^^^\n" + 
				"Unreachable code\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 16)\n" + 
				"	class Foo extends Exception {}\n" + 
				"	      ^^^\n" + 
				"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 17)\n" + 
				"	class SonOfFoo extends Foo {}\n" + 
				"	      ^^^^^^^^\n" + 
				"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" + 
				"----------\n" + 
				"5. WARNING in X.java (at line 18)\n" + 
				"	class DaughterOfFoo extends Foo {}\n" + 
				"	      ^^^^^^^^^^^^^\n" + 
				"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" + 
				"----------\n":
					
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	foo();\n" + 
				"	^^^^^^\n" + 
				"Unreachable code\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 16)\n" + 
				"	class Foo extends Exception {}\n" + 
				"	      ^^^\n" + 
				"The serializable class Foo does not declare a static final serialVersionUID field of type long\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 17)\n" + 
				"	class SonOfFoo extends Foo {}\n" + 
				"	      ^^^^^^^^\n" + 
				"The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 18)\n" + 
				"	class DaughterOfFoo extends Foo {}\n" + 
				"	      ^^^^^^^^^^^^^\n" + 
				"The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348369
public void test071() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		try {\n" +
			"		} catch (Exception [][][][][]  e [][][][]) {\n" +
			"		}\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	} catch (Exception [][][][][]  e [][][][]) {\n" + 
		"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"No exception of type Exception[][][][][][][][][] can be thrown; an exception type must be a subclass of Throwable\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348369
public void test072() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		try {\n" +
			"		} catch (Exception e []) {\n" +
			"		}\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	} catch (Exception e []) {\n" + 
		"	         ^^^^^^^^^^^^^^\n" + 
		"No exception of type Exception[] can be thrown; an exception type must be a subclass of Throwable\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348369
public void test073() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		try {\n" +
			"		} catch (Exception [] e) {\n" +
			"		}\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	} catch (Exception [] e) {\n" + 
		"	         ^^^^^^^^^^^^\n" + 
		"No exception of type Exception[] can be thrown; an exception type must be a subclass of Throwable\n" + 
		"----------\n");
}
// test for regression during work on bug 345305
// saw "The local variable name may not have been initialized" against last code line
public void test074() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	Class test(String name) throws ClassNotFoundException {\n" + 
			"		Class c= findClass(name);\n" + 
			"		if (c != null)\n" + 
			"			return c;\n" + 
			"		if (isExcluded(name)) {\n" + 
			"			try {\n" + 
			"				c= findClass(name);\n" + 
			"				return c;\n" + 
			"			} catch (ClassNotFoundException e) {\n" + 
			"				// keep searching\n" + 
			"			}\n" + 
			"		}\n" + 
			"		return findClass(name);\n" +
			"    }\n" + 
			"    boolean isExcluded(String name) { return false; }\n" +
			"    Class findClass(String name) throws ClassNotFoundException { return null; }\n" +
			"}\n"
		});
}

// Bug 387612 - Unreachable catch block...exception is never thrown from the try
// redundant exception in throws must not confuse downstream analysis
public void testBug387612() {
	String serialUID = "private static final long serialVersionUID=1L;";
	runNegativeTest(
		new String[] {
			"E.java",
			"public class E extends Exception {"+serialUID+"}\n",
			"E1.java",
			"public class E1 extends E {"+serialUID+"}\n",
			"E2.java",
			"public class E2 extends E {"+serialUID+"}\n",
			"E3.java",
			"public class E3 extends E {"+serialUID+"}\n",
			"A.java",
			"interface A {\n" +
			"    void foo(String a1, String a2) throws E1, E;\n" +
			"}\n",
			"B.java",
			"interface B extends A {\n" +
			"    void foo(String a1, String a2) throws E;\n" +
			"}\n",
			"Client.java",
			"public class Client {\n" +
			"    void test() {\n" +
			"        B b = new B() {\n" +
			"            public void foo(String a1, String a2) {}\n" +
			"        };\n" +
			"        try {\n" +
			"            b.foo(null, null);\n" +
			"        }\n" +
			"        catch (E1 e) {}\n" +
			"        catch (E2 e) {}\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in Client.java (at line 7)\n" + 
		"	b.foo(null, null);\n" + 
		"	^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type E\n" + 
		"----------\n");
}

// Bug 387612 - Unreachable catch block...exception is never thrown from the try
// - changed order in redundant 'throws' clause.
public void testBug387612b() {
	String serialUID = "private static final long serialVersionUID=1L;";
	runNegativeTest(
		new String[] {
			"E.java",
			"public class E extends Exception {"+serialUID+"}\n",
			"E1.java",
			"public class E1 extends E {"+serialUID+"}\n",
			"E2.java",
			"public class E2 extends E {"+serialUID+"}\n",
			"E3.java",
			"public class E3 extends E {"+serialUID+"}\n",
			"A.java",
			"interface A {\n" +
			"    void foo(String a1, String a2) throws E, E1;\n" +
			"}\n",
			"B.java",
			"interface B extends A {\n" +
			"    void foo(String a1, String a2) throws E;\n" +
			"}\n",
			"Client.java",
			"public class Client {\n" +
			"    void test() {\n" +
			"        B b = new B() {\n" +
			"            public void foo(String a1, String a2) {}\n" +
			"        };\n" +
			"        try {\n" +
			"            b.foo(null, null);\n" +
			"        }\n" +
			"        catch (E1 e) {}\n" +
			"        catch (E2 e) {}\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in Client.java (at line 7)\n" + 
		"	b.foo(null, null);\n" + 
		"	^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type E\n" + 
		"----------\n");
}

// Bug 387612 - Unreachable catch block...exception is never thrown from the try
// interface with redundant exceptions in throws is read from class file.
public void testBug387612c() {
	String serialUID = "private static final long serialVersionUID=1L;";
	runConformTest(
		new String[] {
			"E.java",
			"public class E extends Exception {"+serialUID+"}\n",
			"E1.java",
			"public class E1 extends E {"+serialUID+"}\n",
			"E2.java",
			"public class E2 extends E {"+serialUID+"}\n",
			"A.java",
			"interface A {\n" +
			"    void foo(String a1, String a2) throws E1, E;\n" +
			"}\n",
			"B.java",
			"interface B extends A {\n" +
			"    void foo(String a1, String a2) throws E;\n" +
			"}\n"
		});
	runNegativeTest(
		new String[] {
			"Client.java",
			"public class Client {\n" +
			"    void test() {\n" +
			"        B b = new B() {\n" +
			"            public void foo(String a1, String a2) {}\n" +
			"        };\n" +
			"        try {\n" +
			"            b.foo(null, null);\n" +
			"        }\n" +
			"        catch (E1 e) {}\n" +
			"        catch (E2 e) {}\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in Client.java (at line 7)\n" + 
		"	b.foo(null, null);\n" + 
		"	^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type E\n" + 
		"----------\n",
		null,
		false/*shouldFlush*/);
}

public static Class testClass() {
	return TryStatementTest.class;
}
}
