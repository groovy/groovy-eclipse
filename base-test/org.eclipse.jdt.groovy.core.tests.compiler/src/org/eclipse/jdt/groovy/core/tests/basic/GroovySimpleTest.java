/*******************************************************************************
 * Copyright (c) 2009-2011 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.groovy.core.tests.basic;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.jdt.groovy.internal.compiler.ast.AliasImportReference;
import org.codehaus.jdt.groovy.internal.compiler.ast.EventListener;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyClassScope;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.codehaus.jdt.groovy.internal.compiler.ast.IGroovyDebugRequestor;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.omg.CORBA.Environment;


public class GroovySimpleTest extends AbstractRegressionTest {

	public GroovySimpleTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildUniqueComplianceTestSuite(testClass(),ClassFileConstants.JDK1_5);
//		return buildUniqueComplianceTestSuite(testClass(),F_1_5);
//		return buildAllCompliancesTestSuite(testClass());
//		return buildMinimalComplianceTestSuite(testClass(),F_1_5);
	} 
	
	
	protected void setUp() throws Exception {
		super.setUp();
		GroovyCompilationUnitDeclaration.defaultCheckGenerics=true;
		GroovyParser.debugRequestor = new DebugRequestor();
		complianceLevel = ClassFileConstants.JDK1_5;
	}

	public static Class testClass() {
		return GroovySimpleTest.class;
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		GroovyCompilationUnitDeclaration.defaultCheckGenerics=false;
		GroovyParser.debugRequestor = null; 
	}

	/** 
     * Include the groovy runtime jars on the classpath that is used.
     * Other classpath issues can be seen in TestVerifier/VerifyTests and only when
     * the right prefixes are registered in there will it use the classloader with this
     * classpath rather than the one it conjures up just to load the built code.
     */
    protected String[] getDefaultClassPaths() {
        String[] cps = super.getDefaultClassPaths();
        String[] newcps = new String[cps.length+3];
        System.arraycopy(cps,0,newcps,0,cps.length);
        try {
        	URL groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-1.8.2.jar");
        	if (groovyJar==null) {
        		groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-1.7.10.jar");
            	if (groovyJar==null) {
            		groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-1.6.7.jar");
            	}
        	}
            newcps[newcps.length-1] = FileLocator.resolve(groovyJar).getFile();
        	URL asmJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/asm-3.2.jar");
        	if (asmJar==null) {
        		asmJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/asm-2.2.3.jar");
        	}
            newcps[newcps.length-2] = FileLocator.resolve(asmJar).getFile();
	        // FIXASC think more about why this is here... the tests that need it specify the option but that is just for
	        // the groovy class loader to access it.  The annotation within this jar needs to be resolvable by the compiler when
	        // building the annotated source - and so I suspect that the groovyclassloaderpath does need merging onto the project
	        // classpath for just this reason, hmm.
	        newcps[newcps.length-3] = FileLocator.resolve(Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry("astTransformations/transforms.jar")).getFile();
	        // newcps[newcps.length-4] = new File("astTransformations/spock-core-0.1.jar").getAbsolutePath();
        } catch (IOException e) {
            fail("IOException thrown " + e.getMessage());
        }
        return newcps;
    }
    
    // demonstrates the incorrect use of closure syntax on groovy 1.6 that compiles OK.
    // On 1.7 it is recognized as incorrect (it is too similar to the inner class syntax)
    public void testClosureSyntax() {
    	if (GroovyUtils.isGroovy16()) {
	    	this.runConformTest(new String[]{
		    	"Foo.groovy",
		    	"class A {\n"+
		    	"	A(closure) {\n"+
		    	"		closure()\n"+
		    	"	}\n"+
		    	"}\n"+
		    	"abc = {println 'abc'\n"+
		    	"}\n"+
		
		    	"new A({\n"+
		    	"	abc()\n"+
		    	"}) //works properly\n"+
		
		    	"new A() {   \n"+
		    	"	abc()\n"+
		    	"} // throw error: unexpected token: abc at line: 13, column: 5\n"},
		    	"abc\n" + 
				"abc"
	    		); 
    	} else {
    		this.runNegativeTest(new String[]{
    	    	"Foo.groovy",
    	    	"class A {\n"+
    	    	"	A(closure) {\n"+
    	    	"		closure()\n"+
    	    	"	}\n"+
    	    	"}\n"+
    	    	"abc = {println 'abc'\n"+
    	    	"}\n"+

    	    	"new A({\n"+
    	    	"	abc()\n"+
    	    	"}) //works properly\n"+

    	    	"new A() {   \n"+
    	    	"	abc()\n"+
    	    	"} // throw error: unexpected token: abc at line: 13, column: 5\n"},
    	    	"----------\n" + 
    			"1. ERROR in Foo.groovy (at line 12)\n" + 
    			"	abc()\n" + 
    			"	^\n" + 
    			"Groovy:unexpected token: abc @ line 12, column 2.\n" + 
    			"----------\n"); 
    	}
    }
    
    public void testDuplicateClassesUnnecessaryExceptions() {
    	this.runNegativeTest(new String[]{
    			"A.groovy",
    			"class Foo {}\n"+
    			"class Foo {}"},
    			"----------\n" + 
    			"1. ERROR in A.groovy (at line 2)\n" + 
    			"	class Foo {}\n" + 
    			"	^\n" + 
    			"Groovy:Invalid duplicate class definition of class Foo : The source A.groovy contains at least two definitions of the class Foo.\n" + 
    			"----------\n" + 
    			"2. ERROR in A.groovy (at line 2)\n" + 
    			"	class Foo {}\n" + 
    			"	      ^^^\n" + 
    			"The type Foo is already defined\n" + 
    			"----------\n");
    }
   
    public void testGenericParamUsage() {
    	this.runConformTest(new String[]{
    			"A.groovy",
    			"class Foo {\n"+
    			"  public void m(List<String> ls) {}\n"+
    			"}"},"");
    }
    
    public void testGenericParamUsage2() {
    	this.runConformTest(new String[]{
    			"A.groovy",
    		    "class Inferer {\n"+
    			"\n"+
    			"public void m(List<String> ls) {\n"+
    			"	}\n"+
    			"}\n"
    			},"");
    }
    
    public void testStaticProperty() {
    	this.runConformTest(new String[]{
    			"A.groovy",
    			"class Super {" +
    			"  def static getSql() { return 'abc'; }\n" +
    			"}\n"+
    			"class Sub extends Super {\n" +
    			"  def static m() {\n" +
    			"    sql.charAt(0)\n"+
    			"  }" +
    			"}\n"},"");
    }
    
//    public void testClash_GRE1076() {
//    	runConformTest(new String[]{
//    			"com/foo/Bar/config.groovy",
//    			"package com.foo.Bar\n"+
//    			"print 'abc'\n",
//    			"com/foo/Bar.java",
//    			"package com.foo;\n"+
//    			"class Bar {}\n"
//    	},"abc");
//    }
//    
//    public void testClash_GRE1076_2() {
//    	runConformTest(new String[]{
//    			"com/foo/Bar.java",
//    			"package com.foo;\n"+
//    			"public class Bar { \n"+
//    			"  public static void main(String[] argv) {\n"+
//    			"    System.out.println(\"def\");\n"+
//    			"  }\n"+
//    			"}\n",
//    			"com/foo/Bar/config.groovy",
//    			"package com.foo.Bar\n"+
//    			"print 'abc'\n"
//    	},"def");
//    }

    public void testUnreachable_1047() {
    	this.runConformTest(new String[]{
    			"MyException.java",
    			"class MyException extends Exception {\n"+
    			"  private static final long serialVersionUID = 1L;\n"+
    			"}",
    			"CanThrowException.groovy",
    			"\n"+
    			"public interface CanThrowException {\n"+
    			"  void thisCanThrowException() throws MyException\n"+
    			"}\n",
    			"ShouldCatchException.java",
    			"\n"+
    			"class ShouldCatchException {\n"+
    			"  private CanThrowException thing;\n"+
    			"  \n"+
    			"  public void doIt() {\n"+
    			"    try {\n"+
    			"      thing.thisCanThrowException();\n"+
    			"    } catch( MyException e ) {\n"+
    			"      System.out.println(\"Did we catch it?\");\n"+
    			"    }\n"+
    			"  }\n"+
    			"}\n"
    	});
    }
    
    public void testUnreachable_1047_2() {
    	this.runConformTest(new String[]{
    			"MyException.java",
    			"class MyException extends Exception {\n"+
    			"  private static final long serialVersionUID = 1L;\n"+
    			"}",
    			"CanThrowException.groovy",
    			"\n"+
    			
    			"public class CanThrowException {\n"+
    			"  public CanThrowException() throws MyException {\n"+
    			"    throw new MyException();\n"+
    			"  }\n"+
    			"}\n",
    			"ShouldCatchException.java",
    			"\n"+
    			"class ShouldCatchException {\n"+
    			"  \n"+
    			"  public void doIt() {\n"+
    			"    try {\n"+
    			"      new CanThrowException();\n"+
    			"    } catch( MyException e ) {\n"+
    			"      System.out.println(\"Did we catch it?\");\n"+
    			"    }\n"+
    			"  }\n"+
    			"}\n"
    	});
    }
    

    /**
     * This test is looking at what happens when a valid type is compiled ahead of two problem types (problematic
     * since they both declare the same class).  Although the first file gets through resolution OK, re-resolution
     * is attempted because the SourceUnit isn't tagged as having succeeded that phase - the exception thrown
     * for the real problem jumps over the tagging process. 
     */
    public void testDuplicateClassesUnnecessaryExceptions_GRE796() {
    	this.runNegativeTest(new String[]{
    			"spring/resources.groovy",
    			"foo = {}\n",
    			"A.groovy",
    			"class Foo {}\n",
    			"Foo.groovy",
    			"class Foo {}",
    			},
    			"----------\n" + 
    			"1. ERROR in Foo.groovy (at line 1)\n" + 
    			"	class Foo {}\n" + 
    			"	 ^\n" + 
    			"Groovy:Invalid duplicate class definition of class Foo : The sources Foo.groovy and A.groovy are containing both a class of the name Foo.\n" + 
    			"----------\n" + 
    			"2. ERROR in Foo.groovy (at line 1)\n" + 
    			"	class Foo {}\n" + 
    			"	      ^^^\n" + 
    			"The type Foo is already defined\n" + 
    			"----------\n");
    }
    
//    public void testDuplicateClassesUnnecessaryExceptions_GRE796_2() {
//    	this.runNegativeTest(new String[]{
//    			"spring/resources.groovy",
//    			"foo = {}\n",
//    			"a/Foo.groovy",
//    			"//package a\n"+
//    			"//class Foo {}\n",
//    			"a/Foo.groovy",
//    			"package a\n"+
//    			"class Foo {}",
//    			},
//    			"----------\n" + 
//    			"1. ERROR in a\\Foo.groovy\n" + 
//    			"Groovy:Found unexpected MOP methods in the class node for Foo(super$3$getProperty)\n" + 
//    			"----------\n" + 
//    			"----------\n" + 
//    			"1. ERROR in a\\Foo.groovy (at line 2)\n" + 
//    			"	class Foo {}\n" + 
//    			"	^\n" + 
//    			"Groovy:Invalid duplicate class definition of class a.Foo : The source a"+File.separator+"Foo.groovy contains at least two definitions of the class a.Foo.\n" + 
//    			"----------\n");
//    }
    
    public void testAnnos_GRE697() {
	    	this.runConformTest(new String[]{
	    			"A.groovy",
	    			
	    			"@B\n"+
	    			"class A { \n"+
	    			"  public static void main(String[]argv) {print 'abc';}\n"+
	    			"}\n"+
	    			"@interface B {\n"+
	    			"   String value() default \"\"\n"+
	    			"}",
	    	},"abc");
    }
    
    public void testGroovyColon_GRE801() {
    	this.runNegativeTest(new String[]{
    			"A.groovy",
    		    "httpClientControl.demand.generalConnection(1..1) = {->\n"+
    		    "currHttp\n"+
    		    "} \n"},
    		    "----------\n" + 
    			"1. ERROR in A.groovy (at line 1)\n" + 
    			"	httpClientControl.demand.generalConnection(1..1) = {->\n" + 
    			"	                                                  ^\n" + 
    			"Groovy:\"httpClientControl.demand.generalConnection((1..1))\" is a method call expression, but it should be a variable expression at line: 1 column: 50. File: A.groovy @ line 1, column 50.\n" + 
    			"----------\n");
    }
    
    // temporary removal (19th May 2011) - I think the 'shield' interface we have is hopefully sufficient.  Only problem may be recompiled references
    // in types we don't patch (basically outside of the generated parser).  Lets see what the build machine makes of it.
//    public void testTokens() {
//    	if (GroovyTokenTypes.ONE_NL_KEEP<GroovyTokenTypes.BIG_SUFFIX) {
//    		fail("Reorder tokens to make the new one last!");
//    	}
//    }


    public void testStaticOuter_GRE944() {
    	if (GroovyUtils.GROOVY_LEVEL < 18) {
	    	this.runConformTest(new String[]{
	    			"A.groovy",
	    			"static class A {\n"+
	    			"  public static void main(String[]argv) {print 'abc';}\n"+
	    			"}\n",
	    	},"abc");
    	} else {
	    	this.runNegativeTest(new String[]{
	    			"A.groovy",
	    			"static class A {\n"+
	    			"  public static void main(String[]argv) {print 'abc';}\n"+
	    			"}\n",
	    	},"----------\n" + 
			"1. ERROR in A.groovy (at line 1)\n" + 
			"	static class A {\n" + 
			"	 ^\n" + 
			"Groovy:The class \'A\' has an incorrect modifier static.\n" + 
			"----------\n");
    	}
    }
    
    public void testCrashRatherThanError_GRE986() {
    	this.runNegativeTest(new String[]{
    			"A.groovy",
    			"hello \\u\n"+
    			"class Foo {}\n"
    	},"----------\n" + 
		"1. ERROR in A.groovy (at line 1)\n" + 
		"	hello \\u\n" + 
		"	       ^\n" + 
		"Groovy:Did not find four digit hex character code. line: 1 col:7 @ line 1, column 7.\n" + 
		"----------\n");
    }
    
    public void testAmbiguous_GRE945_gu() {
    	this.runConformTest(new String[]{
    			"Code.groovy",
    			"import bug.factory.*\n"+
    			"class Code {\n"+
    			"  static Factory fact = new Factory()\n"+
    			"  public static void main(String[]argv) {\n"+
    			"    fact.foo()\n"+
    			"  }\n"+
    			"}\n",

    			"Factory.groovy",
    			"package bug.factory\n"+
    			"class Factory { static foo() { print 'abc'}}\n",
    			
    		},"abc");
    }
    
    public void testAmbiguous_GRE945_jl() {
    	this.runConformTest(new String[]{
    			"Code.groovy",
    			"import bug.factory.*\n"+
    			"class Code {\n"+
    			"  static StringBuffer fact = new StringBuffer()\n"+
    			"  public static void main(String[]argv) {\n"+
    			"    print fact.foo()\n"+
    			"  }\n"+
    			"}\n",

    			"StringBuffer.groovy",
    			"package bug.factory\n"+
    			"class StringBuffer { static String foo() { return 'abc'}}\n",
    			
    		},"abc");
    }
    
    public void testAmbiguous_GRE945_bothFromSource() {
    	this.runConformTest(new String[]{
    			"Code.groovy",
    			"import a.*\n"+
    			"import b.*\n"+
    			"class Code {\n"+
    			"  static A fact = new A()\n"+
    			"  public static void main(String[]argv) {\n"+
    			"    print fact.foo()\n"+
    			"  }\n"+
    			"}\n",

    			"a/A.groovy",
    			"package a\n"+
    			"class A { static String foo() { return 'abc'}}\n",
    			
    			"b/A.groovy",
    			"package b\n"+
    			"class A { static String foo() { return 'def'}}\n",
    			
    		},"abc");
    }
    
    public void testAmbiguous_GRE945_bothFromSource_2() {
    	this.runConformTest(new String[]{
    			"Code.groovy",
    			"import b.*\n"+
    			"import a.*\n"+
    			"class Code {\n"+
    			"  static A fact = new A()\n"+
    			"  public static void main(String[]argv) {\n"+
    			"    print fact.foo()\n"+
    			"  }\n"+
    			"}\n",

    			"a/A.groovy",
    			"package a\n"+
    			"class A { static String foo() { return 'abc'}}\n",
    			
    			"b/A.groovy",
    			"package b\n"+
    			"class A { static String foo() { return 'def'}}\n",
    			
    		},"def");
    }
    
    public void testAmbiguous_GRE945_bothFromSource_3() {
    	this.runConformTest(new String[]{
    			"Code.groovy",
    			"import b.*\n"+
    			"import a.*\n"+
    			"class Code {\n"+
    			"  static Process fact = new Process()\n"+
    			"  public static void main(String[]argv) {\n"+
    			"    print fact.foo()\n"+
    			"  }\n"+
    			"}\n",

    			"a/Process.groovy",
    			"package a\n"+
    			"class Process { static String foo() { return 'abc'}}\n",
    			
    			"b/Process.groovy",
    			"package b\n"+
    			"class Process { static String foo() { return 'def'}}\n",
    			
    		},"def");
    }
    
    public void testAmbiguous_GRE945_ju() {
    	this.runConformTest(new String[]{
    			"Code.groovy",
    			"import bug.factory.*\n"+
    			"class Code {\n"+
    			"  static List fact = new List()\n"+
    			"  public static void main(String[]argv) {\n"+
    			"    fact.foo()\n"+
    			"  }\n"+
    			"}\n",

    			"List.groovy",
    			"package bug.factory\n"+
    			"class List { static foo() { print 'abc'}}\n",
    			
    		},"abc");
    }
    
    public void testAmbiguous_GRE945_jn() {
    	this.runConformTest(new String[]{
    			"Code.groovy",
    			"import bug.factory.*\n"+
    			"class Code {\n"+
    			"  static Socket fact = new Socket()\n"+
    			"  public static void main(String[]argv) {\n"+
    			"    fact.foo()\n"+
    			"  }\n"+
    			"}\n",

    			"Socket.groovy",
    			"package bug.factory\n"+
    			"class Socket { static foo() { print 'abc'}}\n",
    			
    		},"abc");
    }
    
    public void testAmbiguous_GRE945_gl() {
    	this.runConformTest(new String[]{
    			"Code.groovy",
    			"import bug.factory.*\n"+
    			"class Code {\n"+
    			"  static Tuple fact = new Tuple()\n"+
    			"  public static void main(String[]argv) {\n"+
    			"    fact.foo()\n"+
    			"  }\n"+
    			"}\n",

    			"Tuple.groovy",
    			"package bug.factory\n"+
    			"class Tuple { static foo() { print 'abc'}}\n",
    			
    		},"abc");
    }
    
    public void testAmbiguous_GRE945_ji() {
    	this.runConformTest(new String[]{
    			"Code.groovy",
    			"import bug.factory.*\n"+
    			"class Code {\n"+
    			"  static Serializable fact = new Serializable()\n"+
    			"  public static void main(String[]argv) {\n"+
    			"    fact.foo()\n"+
    			"  }\n"+
    			"}\n",

    			"Serializable.groovy",
    			"package bug.factory\n"+
    			"class Serializable { static foo() { print 'abc'}}\n",
    			
    		},"abc");
    }

    public void testStaticOuter_GRE944_2() {
    	if (GroovyUtils.GROOVY_LEVEL<18) {
	    	this.runConformTest(new String[]{
	    			"B.java",
	    			"public class B {\n"+
	    			"  public static void main(String[] argv) {\n" +
	    			"    new A.C().foo();\n" +
	    			"  }\n" +
	    			"}\n",
	    			"A.groovy",
	    			"static class A {\n"+
	    			"  static class C {\n"+
	    			"  public void foo() {print 'abcd';}\n"+
	    			"  }\n"+
	    			"}\n",
	    	},"abcd");
    	} else {
    		this.runNegativeTest(new String[]{
	    			"B.java",
	    			"public class B {\n"+
	    			"  public static void main(String[] argv) {\n" +
	    			"    new A.C().foo();\n" +
	    			"  }\n" +
	    			"}\n",
	    			"A.groovy",
	    			"static class A {\n"+
	    			"  static class C {\n"+
	    			"  public void foo() {print 'abcd';}\n"+
	    			"  }\n"+
	    			"}\n",
	    	},"----------\n" + 
			"1. ERROR in A.groovy (at line 1)\n" + 
			"	static class A {\n" + 
			"	 ^\n" + 
			"Groovy:The class \'A\' has an incorrect modifier static.\n" + 
			"----------\n");
    	}
    }
    
    public void testEnumStatic_GRE974() {
    	this.runConformTest(new String[]{
    			"A.groovy",
    			"package be.flow\n"+
    			"\n"+
    			"enum C1{\n"+
    			"	TEST_C1\n"+
    			"}	\n"+
    			"\n"+
    			"class A {\n"+
    			"	public enum C2{\n"+
    			"		TEST_C2\n"+
    			"	}\n"+
    			"}",
    			
    			"B.groovy",
    			"package be.flow\n"+
    			"\n"+
    			"import static be.flow.C1.TEST_C1;\n"+
    			"import static be.flow.A.C2.*;\n"+
    			"\n"+
    			"class B {\n"+
    			"	\n"+
    			"	B(){\n"+
    			"		super(TEST_C2)\n"+
    			"	}\n"+
    			"	\n"+
    			"	void doIt(){\n"+
    			"		println(be.flow.C1.TEST_C1);\n"+
    			"		println(be.flow.A.C2.TEST_C2);\n"+
    			"		println(TEST_C2);\n"+
    			"	}\n"+
    			"	\n"+
    			"}",
    			
    			"D.groovy",
    			"package be.flow\n"+
    			"\n"+
    			"import static be.flow.C1.TEST_C1;\n"+
    			"import static be.flow.A.C2.*;\n"+
    			"\n"+
    			"class D {\n"+
    			"	\n"+
    			"	static void doIt(){\n"+
    			"		println(be.flow.C1.TEST_C1);\n"+
    			"		println(be.flow.A.C2.TEST_C2);\n"+
    			"		println(TEST_C2);\n"+
    			"	}\n"+
    			"	\n"+
    			"}"
    			},"");
    }
    

    public void testGRE830() {
	    	this.runNegativeTest(new String[]{
	    			"AnnotationDouble.groovy",
	    			"import static java.lang.annotation.ElementType.FIELD;\n"+
	    			"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n"+
	    			"import java.lang.annotation.Retention;\n"+
	    			"import java.lang.annotation.Target;\n"+
	    			"\n"+
	    			"@Target(FIELD)\n"+
	    			"@Retention(RUNTIME)\n"+
	    			"public @interface AnnotationDouble { String value(); double width() default 5.0d; }\n",
	    			"AnnotationDoubleTest.groovy",
	    			"class AnnotationDoubleTest {\n"+
	    			"\n"+
	    			"class FooWithAnnotation { @AnnotationDouble(value=\"test\", width=1.0) double value; }\n"+
	    			"\n"+
	    			"def test = new AnnotationDoubleTest()\n"+
	    			"}\n"
	    	},"----------\n" + 
			"1. ERROR in AnnotationDoubleTest.groovy (at line 3)\n" + 
			"	class FooWithAnnotation { @AnnotationDouble(value=\"test\", width=1.0) double value; }\n" + 
			"	                                                                ^\n" + 
			"Groovy:Attribute \'width\' should have type \'java.lang.Double\'; but found type \'java.math.BigDecimal\' in @AnnotationDouble\n" + 
			"----------\n");
    }

    public void testGRE830_2() {
    	this.runNegativeTest(new String[]{
    			"AnnotationDouble.groovy",
    			"import static java.lang.annotation.ElementType.FIELD;\n"+
    			"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n"+
    			"import java.lang.annotation.Retention;\n"+
    			"import java.lang.annotation.Target;\n"+
    			"\n"+
    			"@Target(FIELD)\n"+
    			"@Retention(RUNTIME)\n"+
    			"public @interface AnnotationDouble { String value(); double width() default 5.0d; }\n",
    			"AnnotationDoubleTest.groovy",
    			"class AnnotationDoubleTest {\n"+
    			"\n"+
    			"class FooWithAnnotation { @AnnotationDouble(value=\"test\", width=1.0) double value; }\n"+
    			"\n"+
    			"def test = new AnnotationDoubleTest()\n"+
    			"}\n"
    	},"----------\n" + 
		"1. ERROR in AnnotationDoubleTest.groovy (at line 3)\n" + 
		"	class FooWithAnnotation { @AnnotationDouble(value=\"test\", width=1.0) double value; }\n" + 
		"	                                                                ^\n" + 
		"Groovy:Attribute \'width\' should have type \'java.lang.Double\'; but found type \'java.math.BigDecimal\' in @AnnotationDouble\n" + 
		"----------\n");
    }
    
    public void testRecovery_GRE766() {
	    	this.runNegativeTest(new String[]{
	    			"A.groovy",	
	    			"public class SwingIt {\n"+
				"	def swingit() {\n"+
				"		swing.actions() {\n"+
				"			echoAction= swing.action(name: 'Echo back',\n"+
				"			enabled: bind(source: model, sourceProperty: 'loggedin'),\n"+
				"			closure: { controller.setEchoBack(it.source.selected) })\n"+
				"		}\n"+
				"	}\n" 
	    	},
	    	"----------\n" + 
		"1. ERROR in A.groovy (at line 8)\n" + 
		"	}\n" + 
		"\n" + 
		"	 ^\n" + 
		"Groovy:unexpected token:  @ line 8, column 3.\n" + 
		"----------\n");
	    // missing end curly, but that shouldn't cause us to discard what we successfully parsed
		ModuleNode mn = getModuleNode("A.groovy");
		assertNotNull(mn);
		ClassNode cn = (ClassNode)mn.getClasses().get(0);
		assertNotNull(cn);
		assertTrue(cn.getName().equals("SwingIt")); // should be SwingIt
		MethodNode methodNode = cn.getDeclaredMethod("swingit",Parameter.EMPTY_ARRAY);
		assertNotNull(methodNode);
  }
    
    public void testConstAnnotationValue_GRE629() {
	    	this.runConformTest(new String[]{
	    			"Const.java",
	    			"public class Const {\n"+
	    			"private final static String instance= \"abc\";\n"+
	    			"  public static void main(String[]argv) {\n"+
	    			"    System.out.println(XXX.class.getAnnotation(Anno.class));\n"+
	    			"  }\n"+
	    			"}",
	    			"B.groovy",
	    			"import java.lang.annotation.*\n"+
	    			"@Anno(Const.instance)\n"+
	    			"class XXX {}\n"+
	    			"@Retention(RetentionPolicy.RUNTIME)\n"+
	    			"@interface Anno { String value(); }\n"},
	    			"@Anno(value=abc)");
    }
    
    public void testVarargs_GRE925() {
    	this.runConformTest(new String[]{
    			"Test.java",
    			"class Test {\n"+
    			"  void method(String[] x) {}\n"+
    			"  public static void main(String []argv) {}\n"+
    			"}",
    			"SubTest.groovy",
    			"class SubTest extends Test {\n"+
    			" void method(String[] x) { super.method(x); }\n"+
    			"}"
    	});
    }
    
    public void testPrimitiveLikeTypeNames_GRE891() {
	    	this.runConformTest(new String[]{
	    			"Foo.java",
	    			"public class Foo {\n"+
	    			"public static void main(String[] args) {\n"+
	    			"  Z[][] zs = new Z().zzz();\n"+
	    			"  System.out.println(\"works\");\n"+
	    			"  }\n"+
	    			"}",
	    			"Z.groovy",
	    			"class Z {\n"+
	    			"   Z[][] zzz() { null }\n"+
	    			"}\n"},"works");
    	// return type is a single char type (not in package and not primitive)
    	assertEquals("[[LZ;",getReturnTypeOfMethod("Z.groovy","zzz"));
    }
    
    public void testPrimitiveLikeTypeNames_GRE891_2() {
    	this.runConformTest(new String[]{
    			"Foo.java",
    			"public class Foo {\n"+
    			"public static void main(String[] args) {\n"+
    			"  int[][] zs = new Z().zzz();\n"+
    			"  System.out.println(\"works\");\n"+
    			"  }\n"+
    			"}",
    			"Z.groovy",
    			"class Z {\n"+
    			"   int[][] zzz() { null }\n"+
    			"}\n"},"works");
    	// return type is a primitive
    	assertEquals("[[I",getReturnTypeOfMethod("Z.groovy","zzz"));
    }
    
    public void testPrimitiveLikeTypeNames_GRE891_3() {
    	runConformTest(new String[]{
    			"Foo.java",
    			"public class Foo {\n"+
    			"public static void main(String[] args) {\n"+
    			"  java.lang.String[][] zs = new Z().zzz();\n"+
    			"  System.out.println(\"works\");\n"+
    			"  }\n"+
    			"}",
    			"Z.groovy",
    			"class Z {\n"+
    			"   java.lang.String[][] zzz() { null }\n"+
    			"}\n"},"works");   	
    	// return type is a qualified java built in type
    	assertEquals("[[Ljava.lang.String;",getReturnTypeOfMethod("Z.groovy","zzz"));    	 
    } 
    
    public void testPrimitiveLikeTypeNames_GRE891_4() {
    	runConformTest(new String[]{
    			"pkg/Foo.java",
    			"package pkg;\n"+
    			"public class Foo {\n"+
    			"public static void main(String[] args) {\n"+
    			"  pkg.H[][] zs = new Z().zzz();\n"+
    			"  System.out.println(\"works\");\n"+
    			"  }\n"+
    			"}",
    			"Y.groovy",
    			"package pkg\n"+
    			"class H {}\n",
    			"Z.groovy",
    			"package pkg\n"+
    			"class Z {\n"+
    			"   H[][] zzz() { null }\n"+
    			"}\n"},"works");
    	
    	// return type is a single char groovy type from a package
    	assertEquals("[[Lpkg.H;",getReturnTypeOfMethod("Z.groovy","zzz"));
    } 

    public void testPrimitiveLikeTypeNames_GRE891_5() {
    	runConformTest(new String[]{
    			"pkg/Foo.java",
    			"package pkg;\n"+
    			"public class Foo {\n"+
    			"public static void main(String[] args) {\n"+
    			"  H[][] zs = new Z().zzz();\n"+
    			"  System.out.println(\"works\");\n"+
    			"  }\n"+
    			"}",
    			"Y.java",
    			"package pkg;\n"+
    			"class H {}\n",
    			"Z.groovy",
    			"package pkg;\n"+
    			"class Z {\n"+
    			"   H[][] zzz() { null }\n"+
    			"}\n"},"works");
       	// return type is a single char java type from a package
     	assertEquals("[[Lpkg.H;",getReturnTypeOfMethod("Z.groovy","zzz"));
    } 
    
    /**
     * Find the named file (which should have just been compiled) and for the named method determine 
     * the ClassNode for the return type and return the name of the classnode.
     */
    public String getReturnTypeOfMethod(String filename,String methodname) {
		ModuleNode mn = getModuleNode(filename);      
	    ClassNode cn = (ClassNode)mn.getClasses().get(0);
	    assertNotNull(cn);
	    MethodNode methodNode = cn.getMethod(methodname,new Parameter[]{});
	    assertNotNull(methodNode);
	    ClassNode returnType = methodNode.getReturnType();
	    return returnType.getName();
    }

    
 /*   public void testOverridingFinalMethod_() {
    	this.runConformTest(new String[]{
    			"Base.groovy",
    			"class Base {\n"+
    			"  final getFinalProperty() { 1 }\n"+
    			" }\n"+
    			"class Child extends Base {\n"+
    			"  def finalProperty = 32 \n"+
    			"  public static void main(String []argv) {\n"+
    			"    print new Child().getFinalProperty();\n"+
    			" }\n"+
    			"}\n"},"");
    }*/
    
    public void testMixedModeInnerProperties_GRE597() {
    	this.runConformTest(new String[]{
    			"groovy/JoinGroovy.groovy",
    			"package groovy\n"+
    			"\n"+
    			"class JointGroovy {\n"+
    			"StaticInner property\n"+
    			"\n"+
    			" static class StaticInner {\n"+
    			"  NonStaticInner property2\n"+
    			"\n"+
    			"  class NonStaticInner {\n"+
    			"    Closure property3 = {}\n"+
    			"  }\n"+
    			" }\n"+
                "}",
                "groovy/JointJava.java",
                "package groovy;\n"+
                "\n"+
    			"import groovy.lang.Closure;\n"+
    			"\n"+
    			"public class JointJava {\n"+
    			"    public void method() {\n"+
    			"        Closure closure = new JointGroovy().getProperty().getProperty2().getProperty3();\n"+
    			"    }\n"+
    			"}\n"},"");
    }
    
    public void testGrabScriptAndImports_GRE680() {
	    this.runConformTest(new String[]{"Script.groovy",
			    "import org.mortbay.jetty.Server\n"+
			    "import org.mortbay.jetty.servlet.*\n"+  
			    "import groovy.servlet.*\n"+
			    "\n"+
			    "@Grab(group = 'org.mortbay.jetty', module = 'jetty-embedded', version = '6.1.0')\n"+
			    "def runServer(duration) {  }\n"+
			    "runServer(10000)\n"},""
	    );
    }
    
    public void testMixedModeInnerProperties2_GRE597() {
    	this.runConformTest(new String[]{
    			"groovy/JoinGroovy.groovy",
    			"package groovy\n"+
    			"\n"+
    			"class JointGroovy {\n"+
    			"StaticInner property\n"+
    			"\n"+
    			" }\n"+
    			// now the inner is not an inner (like the previous test) but the property3 still is
    			" class StaticInner {\n"+
    			"  NonStaticInner property2\n"+
    			"\n"+
    			"  class NonStaticInner {\n"+
    			"    Closure property3 = {}\n"+
    			"  }\n"+
                "}",
                "groovy/JointJava.java",
                "package groovy;\n"+
                "\n"+
    			"import groovy.lang.Closure;\n"+
    			"\n"+
    			"public class JointJava {\n"+
    			"    public void method() {\n"+
    			"        Closure closure = new JointGroovy().getProperty().getProperty2().getProperty3();\n"+
    			"    }\n"+
    			"}\n"},"");
    }
    
    public void testNewRuleInLatestGroovy() {
//    	if (isGroovy16()) { // FIXASC should also break in 17b2
    	if (GroovyUtils.GROOVY_LEVEL<18) {
    		// Why no duplicate type exception here on < 1.8? (Move enum and Move for script name)
	    	this.runNegativeTest(new String[]{
	    			"Move.groovy",
	    			"enum Move { ROCK, PAPER, SCISSORS }\n"+
					"\n"+
					"final static BEATS = [\n"+
					"   [Move.ROCK,     Move.SCISSORS],\n"+
					"   [Move.PAPER,    Move.ROCK],\n"+
					"   [Move.SCISSORS, Move.PAPER]\n"+
					"].asImmutable()"
	    	},
	    	"----------\n" + 
			"1. ERROR in Move.groovy (at line 3)\n" + 
			"	final static BEATS = [\n" + 
			"	^\n" + 
			"Groovy:Variable definition has an incorrect modifier \'static\'. at line: 3 column: 1. File: Move.groovy @ line 3, column 1.\n" + 
			"----------\n");
    	} else {
	    	this.runNegativeTest(new String[]{
	    			"Move2.groovy",
	    			"enum Move { ROCK, PAPER, SCISSORS }\n"+
					"\n"+
					"final static BEATS = [\n"+
					"   [Move.ROCK,     Move.SCISSORS],\n"+
					"   [Move.PAPER,    Move.ROCK],\n"+
					"   [Move.SCISSORS, Move.PAPER]\n"+
					"].asImmutable()"
	    	},
	    	"----------\n" + 
			"1. ERROR in Move2.groovy (at line 3)\n" + 
			"	final static BEATS = [\n" + 
			"	^\n" + 
			"Groovy:Modifier 'static' not allowed here.\n" + 
			"----------\n");
    	}
    }
	
	// WMTW: What makes this work: the groovy compiler is delegated to for .groovy files
	public void testStandaloneGroovyFile() {
		this.runConformTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"public class X {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    print \"success\"\n" + 
			"  }\n"+
			"}\n",
		},"success");		
	}
	
	public void testTurningOffGenericsWarnings() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTIONG_GroovyFlags,"0");

//		runConformTest(new String[] {
//				"Assertions.groovy",
//				"import spock.lang.*\n"+
//				"@Speck\n"+
//				"class Assertions {\n"+
////				"  public static void main(String[] argv) { new Assertions().comparingXandY();}\n"+
//				"  def comparingXandY() {\n"+
//				"    def x = 1\n"+
//				"    def y = 2\n"+
//				"    \n"+
////				" print 'a'\n"+
//				"    expect:\n"+
//				"    x < y    // OK\n"+
//				"    x == y   // BOOM!\n"+
//				" }\n"+
//				"}"},
//				"----------\n" + 
//				"1. ERROR in Assertions.groovy (at line 4)\n" + 
//				"	public static void main(String[] argv) {\n" + 
//				"	^^\n" + 
//				"Groovy:Feature methods must not be static @ line 4, column 2.\n" + 
//				"----------\n",
//				null,
//				true,
//				null,
//				options,
//				null);
//		this.runNegativeTest(new String[] {
//			"p/X.groovy",
//			"package p;\n" + 
//			"public class X {\n" + 
//			"  List l = new ArrayList();\n" + 
//			"  public static void main(String[] argv) {\n"+
//			"    print 'success'\n"+
//			"  }\n"+
//			"}\n"},
//			"----------\n" + 
//			"1. ERROR in Assertions.groovy (at line 4)\n" + 
//			"	public static void main(String[] argv) {\n" + 
//			"	^^\n" + 
//			"Groovy:Feature methods must not be static @ line 4, column 2.\n" + 
//			"----------\n",
//			null,
//			true,
//			null,
//			options,
//			null);	
	}
	
	public void testRecursion_GR531() {
		this.runNegativeTest(new String[] {
			"XXX.groovy",
			"class XXX extends XXX {\n" + 
			"}\n",
		},"----------\n" + 
		"1. ERROR in XXX.groovy (at line 1)\n" + 
		"	class XXX extends XXX {\n" + 
		"	 ^\n" + 
		"Groovy:Cyclic inheritance involving XXX in class XXX\n" + 
		"----------\n" + 
		"2. ERROR in XXX.groovy (at line 1)\n" + 
		"	class XXX extends XXX {\n" + 
		"	                  ^^^\n" + 
		"Cycle detected: the type XXX cannot extend/implement itself or one of its own member types\n" + 
		"----------\n");		
	}

	public void testRecursion_GR531_2() {
		this.runNegativeTest(new String[] {
			"XXX.groovy",
			"class XXX extends XXX {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    print \"success\"\n" + 
			"  }\n"+
			"}\n",
		},"----------\n" + 
		"1. ERROR in XXX.groovy (at line 1)\n" + 
		"	class XXX extends XXX {\n" + 
		"	 ^\n" + 
		"Groovy:Cyclic inheritance involving XXX in class XXX\n" + 
		"----------\n" + 
		"2. ERROR in XXX.groovy (at line 1)\n" + 
		"	class XXX extends XXX {\n" + 
		"	                  ^^^\n" + 
		"Cycle detected: the type XXX cannot extend/implement itself or one of its own member types\n" + 
		"----------\n");		
	}
	
	public void testRecursion_GR531_3() {
		this.runNegativeTest(new String[] {
			"XXX.groovy",
			"class XXX extends YYY {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    print \"success\"\n" + 
			"  }\n"+
			"}\n",
			"YYY.groovy",
			"class YYY extends XXX {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    print \"success\"\n" + 
			"  }\n"+
			"}\n"
		},"----------\n" + 
		"1. ERROR in XXX.groovy (at line 1)\n" + 
		"	class XXX extends YYY {\n" + 
		"	      ^^^\n" + 
		"The hierarchy of the type XXX is inconsistent\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. ERROR in YYY.groovy (at line 1)\n" + 
		"	class YYY extends XXX {\n" + 
		"	 ^\n" + 
		"Groovy:Cyclic inheritance involving YYY in class YYY\n" + 
		"----------\n" + 
		"2. ERROR in YYY.groovy (at line 1)\n" + 
		"	class YYY extends XXX {\n" + 
		"	                  ^^^\n" + 
		"Cycle detected: a cycle exists in the type hierarchy between YYY and XXX\n" + 
		"----------\n");		
	}

	public void testRecursion_GR531_4() {
		this.runNegativeTest(new String[] {
			"XXX.groovy",
			"interface XXX extends XXX {\n" + 
			"}\n",
		},"----------\n" + 
		"1. ERROR in XXX.groovy (at line 1)\n" + 
		"	interface XXX extends XXX {\n" + 
		"	 ^\n" + 
		"Groovy:Cyclic inheritance involving XXX in interface XXX\n" + 
		"----------\n" + 
		"2. ERROR in XXX.groovy (at line 1)\n" + 
		"	interface XXX extends XXX {\n" + 
		"	                      ^^^^\n" + 
		"Cycle detected: the type XXX cannot extend/implement itself or one of its own member types\n" + 
		"----------\n");		
	}


//	public void testTargetMetaAnnotation() {
//		this.runNegativeTest(new String[] {
//			"Anno.java",
//			"import java.lang.annotation.*;\n" + 
//			"@Target(ElementType.METHOD) @interface Anno {}\n",
//			"Bar.groovy",
//			"@Anno\n"+
//			"class Bar {}\n"
//		},"xx");		
//	}
	
	/**
	 * The groovy object method augmentation (in GroovyClassScope) should only apply to types directly implementing GroovyObject, rather than
	 * adding them all the way down the hierarchy.  This mirrors what happens in the compiler.
	 */
	/**
	 * First a class extending another.  The superclass gets augmented but not the subclass.
	 */
	public void testClassHierarchiesAndGroovyObjectMethods() {
		try {
			GroovyClassScope.debugListener = new EventListener("augment");
			this.runConformTest(new String[] {
				"Foo.groovy",
				"class Foo {\n"+
				"  static main(args) { print 'abc'} \n"+
				"}\n" + 
				"class Two extends Foo {\n" +
				"  public void m() {\n"+
				"    Object o = getMetaClass();\n"+
				"  }\n"+
				"}\n",
			},"abc");	
			assertEventCount(1,GroovyClassScope.debugListener);
			assertEvent("augment: type Foo having GroovyObject methods added",GroovyClassScope.debugListener);
			System.err.println(GroovyClassScope.debugListener.toString());
		} finally {
			GroovyClassScope.debugListener=null;
		}
	}
	
	/**
	 * Now a class implementing an interface.  The subclass gets augmented because the superclass did not.
	 */
	public void testClassHierarchiesAndGroovyObjectMethods2() {
		try {
			GroovyClassScope.debugListener = new EventListener("augment");
			this.runConformTest(new String[] {
				"Foo.groovy",
				"class Foo implements One {\n" +
				"  public void m() {\n"+
				"    Object o = getMetaClass();\n"+
				"  }\n"+
				"  static main(args) { print 'abc'} \n"+
				"}\n"+
				"interface One {\n"+
				"}\n",
			},"abc");		
			assertEventCount(1,GroovyClassScope.debugListener);
			assertEvent("augment: type Foo having GroovyObject methods added",GroovyClassScope.debugListener);
			System.err.println(GroovyClassScope.debugListener.toString());
		} finally {
			GroovyClassScope.debugListener=null;
		}
	}
	
	/**
	 * Now a class extending a java type which extends a base groovy class.  Super groovy type should get them.
	 * 
	 * This looks odd to me, not sure why Foo and One both get the methods when One inherits them through Foo - 
	 * perhaps the java type in the middle makes a difference.  Anyway by augmenting both of these we
	 * are actually doing the same as groovyc, and that is the main thing.
	 */
	public void testClassHierarchiesAndGroovyObjectMethods3() {
		try {
			GroovyClassScope.debugListener = new EventListener();
			this.runConformTest(new String[] {
				"Foo.groovy",
				"class Foo extends Two {\n" +
				"  public void m() {\n"+
				"    Object o = getMetaClass();\n"+
				"  }\n"+
				"  static main(args) { print 'abc'} \n"+
				"}\n"+
				"class One {\n"+
				"}\n",
				"Two.java",
				"class Two extends One {}\n",
			},"abc");		
			assertEventCount(2,GroovyClassScope.debugListener);
			assertEvent("augment: type One having GroovyObject methods added",GroovyClassScope.debugListener);
			assertEvent("augment: type Foo having GroovyObject methods added",GroovyClassScope.debugListener);
		} finally {
			GroovyClassScope.debugListener=null;
		}
	}
	
	
	public void testStandaloneGroovyFile2() {
		this.runConformTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"public class X {\n" + 
//			"  public static void main(String[] args) {\n"+
			"  static main(args) {\n"+
			"    print \"success\"\n" + 
			"  }\n"+
			"}\n",
		},"success");	
		checkGCUDeclaration("X.groovy", 		
				"package p;\n" + 
				"public class X {\n" + 
				"  public X() {\n" + 
				"  }\n" + 
				// for: public static void main(String[] args) {
//				"  public static void main(public String... args) {\n" + 
				// for: static main(args) {
				"  public static void main(public java.lang.String... args) {\n" + 
				"  }\n" + 
				"}\n"
		);
	}

	public void testParentIsObject_GRE528() {
		this.runConformTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"public class X {\n" + 
			"  static main(args) {\n"+
			"    print \"success\"\n" + 
			"  }\n"+
			"}\n",
		},"success");	
		checkGCUDeclaration("X.groovy", 		
				"package p;\n" + 
				"public class X {\n" + 
				"  public X() {\n" + 
				"  }\n" + 
				"  public static void main(public java.lang.String... args) {\n" + 
				"  }\n" + 
				"}\n"
		);
	}
	
	public void testInnerTypes_1() {
		this.runConformTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"public class X {\n" + 
				" class Inner {}\n"+
				"  static main(args) {\n"+
				"    print \"success\"\n" + 
				"  }\n"+
				"}\n",
			},"success");	
		
			checkGCUDeclaration("X.groovy", 		
					"package p;\n" + 
					"public class X {\n" +
					"  public class Inner {\n" +
					"    public Inner() {\n"+
					"    }\n"+
					"  }\n"+
					"  public X() {\n" + 
					"  }\n" + 
					"  public static void main(public java.lang.String... args) {\n" + 
					"  }\n" + 
					"}\n"
			);
	}

	
	public void testBrokenPackage() {
		this.runNegativeTest(new String[] {
				"Foo.groovy",
				"package \n"+
				"class Name { }\n"},
				"----------\n" + 
				"1. ERROR in Foo.groovy (at line 1)\n" + 
				"	package \n" + 
				"	 ^\n" + 
				"Groovy:Invalid package specification @ line 1, column 1.\n" + 
				"----------\n");
	}
	
	// can be uncommented when we have picked up the fix for GROOVY-4219
//	public void testGRE637() {
//		this.runConformTest(new String[]{
//				"de/brazzy/nikki/Texts.java",
//				"package de.brazzy.nikki;\n"+
//				"\n"+
//				"public final class Texts { \n"+
//				"	public static class Image {\n"+
//				"		public static final String ORDERED_BY_FILENAME = \"image.sortedby.filename\";\n"+
//				"		public static final String ORDERED_BY_TIME = \"image.sortedby.time\";\n"+
//				"}}\n",
//				"de/brazzy/nikki/model/Image.groovy",
//				"package de.brazzy.nikki.model;\n"+
//				"\n"+
//				"class Image implements Serializable{\n"+
//				"    def fileName\n"+
//				"    def time\n"+
//				"}\n",
//				"de/brazzy/nikki/model/ImageSortField.groovy",
//				"package de.brazzy.nikki.model\n"+
//				"\n"+
//				"import de.brazzy.nikki.Texts\n"+
//				"import de.brazzy.nikki.model.Image\n"+
//				"\n"+
//				"enum ImageSortField {\n"+
//				"    FILENAME(field: Image.metaClass.fileName, name: Texts.Image.ORDERED_BY_FILENAME),\n"+
//				"    TIME(field: Image.metaClass.time, name: Texts.Image.ORDERED_BY_TIME)\n"+
//				"\n"+
//				"    def field\n"+
//				"    def name\n"+
//				"\n"+
//				"    public String toString(){\n"+
//				"        name\n"+
//				"    }\n"+
//				"}\n"
//		});
//
//	}
	
	
	public void testParsingRecovery_GRE494() {
		this.runNegativeTest(new String[] {
			"Simple.groovy",
			"class Simple {\n"+
			"	\n"+
			"	def getNumber() {\n"+
			"		return 42\n"+
			"	}\n"+
			"\n"+
			"	asdf\n"+
			"		\n"+
			"	static main(args) {\n"+
			"		print new Simple().getNumber()\n"+
			"	}\n"+
			"}\n"},
			"----------\n" + 
			"1. ERROR in Simple.groovy (at line 7)\n" + 
			"	asdf\n" + 
			"	^\n" + 
			"Groovy:unexpected token: asdf @ line 7, column 2.\n" + 
			"----------\n");
		checkGCUDeclaration("Simple.groovy",
				"public class Simple {\n" + 
				"  private java.lang.Object asdf;\n"+
				"  public Simple() {\n" + 
				"  }\n" + 
				"  public java.lang.Object getNumber() {\n" + 
				"  }\n" + 
				"  public static void main(public java.lang.String... args) {\n" + 
				"  }\n" + 
				"}\n");
	}

	public void testParsingRecovery_GRE494_2() {
		this.runNegativeTest(new String[] {
			"MyDomainClass.groovy",
			"class MyDomainClass {\n"+
			"  int intField\n"+
			"\n"+
			"belo\n"+
			"\n"+
			"}\n"},
			"----------\n" + 
			"1. ERROR in MyDomainClass.groovy (at line 4)\n" + 
			"	belo\n" + 
			"	^\n" + 
			"Groovy:unexpected token: belo @ line 4, column 1.\n" + 
			"----------\n");
		checkGCUDeclaration("MyDomainClass.groovy",
				"public class MyDomainClass {\n" + 
				"  private int intField;\n" + 
				"  private java.lang.Object belo;\n"+
				"  public MyDomainClass() {\n" + 
				"  }\n" + 
				"}\n");
	}	
	
	/*
	public void testParsingMissingCurlyRecovery1_GRE468() {
		if (isGroovy16()) return; // not valid on 1.6 - doesn't have a fixed parser
		this.runNegativeTest(new String[] {
				"XXX.groovy",
				"class F { int y() }\n"
				},
				"----------\n" + 
				"1. ERROR in XXX.groovy (at line 1)\n" + 
				"	class F { int y() }\n" + 
				"	               ^\n" + 
				"Groovy:Method body missing @ line 1, column 15.\n" + 
				"----------\n"
				);
		checkGCUDeclaration("XXX.groovy",
				"public class F extends java.lang.Object {\n" + 
				"  public F() {\n" + 
				"  }\n" + 
				"  public int y() {\n" + 
				"  }\n" + 
				"}\n");
	}
	
	public void testParsingMissingCurlyRecovery2_GRE468() {
		if (isGroovy16()) return; // not valid on 1.6 - doesn't have a fixed parser
		this.runNegativeTest(new String[] {
				"XXX.groovy",
				"class F { int y() { }\n"
				},
				"----------\n" + 
				"1. ERROR in XXX.groovy (at line 1)\n" + 
				"	class F { int y() }\n" + 
				"	           ^\n" + 
				"Groovy:You defined a method without body. Try adding a body, or declare it abstract. at line: 1 column: 11. File: XXX.groovy @ line 1, column 11.\n" + 
				"----------\n"
				);
		checkGCUDeclaration("XXX.groovy",
				"public class C extends java.lang.Object {\n" + 
				"  public C() {\n" + 
				"  }\n" + 
				"  public void m() {\n" + 
				"  }\n" + 
				"}\n");
	}*/
	
	/**
	 * Simple case of a new reference missing () in a method body
	 */
	public void testParsingNewRecovery1_GRE468() {
		this.runNegativeTest(new String[] {
				"XXX.groovy",
				"class C {\n"+
				"  public void m() {\n"+
				"  new Earth\n"+
				"  }\n"+
				"}"
				},
				"----------\n" + 
				"1. ERROR in XXX.groovy (at line 3)\n" + 
				"	new Earth\n" + 
				"	^\n" + 
				"Groovy:unable to resolve class Earth \n" + 
				"----------\n" + 
				"2. ERROR in XXX.groovy (at line 3)\n" + 
				"	new Earth\n" + 
				"	    ^\n" + 
				"Groovy:expecting \'(\' or \'[\' after type name to continue new expression @ line 3, column 7.\n" + 
				"----------\n"
				);
		checkGCUDeclaration("XXX.groovy",
				"public class C {\n" + 
				"  public C() {\n" + 
				"  }\n" + 
				"  public void m() {\n" + 
				"  }\n" + 
				"}\n");
	}
	
	
	/**
	 * Simple case of a new reference missing () followed by valid code
	 */
	public void testParsingNewRecovery2_GRE468() {
		this.runNegativeTest(new String[] {
				"XXX.groovy",
				"class C {\n"+
				"  public void m() {\n"+
				"  new Earth\n"+
				"  def a = 42\n"+
				"  print a\n"+
				"  }\n"+
				"}"
				},
				"----------\n" + 
				"1. ERROR in XXX.groovy (at line 3)\n" + 
				"	new Earth\n" + 
				"	^\n" + 
				"Groovy:unable to resolve class Earth \n" + 
				"----------\n" + 
				"2. ERROR in XXX.groovy (at line 3)\n" + 
				"	new Earth\n" + 
				"	    ^\n" + 
				"Groovy:expecting \'(\' or \'[\' after type name to continue new expression @ line 3, column 7.\n" + 
				"----------\n"
				);
		checkGCUDeclaration("XXX.groovy",
				"public class C {\n" + 
				"  public C() {\n" + 
				"  }\n" + 
				"  public void m() {\n" + 
				"  }\n" + 
				"}\n");
	}
	
	public void testRecoveryParameterCompletion() {
		this.runNegativeTest(new String[]{
				"XXX.groovy",
				"package com.example.foo\n"+
				"class Foo {\n"+
				"public void foo(XMLConstants\n"+
				"}\n"},
				"----------\n" + 
				"1. ERROR in XXX.groovy (at line 3)\n" + 
				"	public void foo(XMLConstants\n" + 
				"	                ^\n" + 
				"Groovy:unexpected token: XMLConstants @ line 3, column 17.\n" + 
				"----------\n" + 
				"2. ERROR in XXX.groovy (at line 4)\n" + 
				"	}\n" + 
				"\n" + 
				"	 ^\n" + 
				"Groovy:unexpected token:  @ line 4, column 2.\n" + 
				"----------\n");
		checkGCUDeclaration("XXX.groovy",
				"public class Foo {\n" + 
				"  public Foo() {\n" + 
				"  }\n" + 
				"}\n");
		}
	
	/**
	 * Missing type name for new call
	 */
	public void testParsingNewRecovery3_GRE468() {
		this.runNegativeTest(new String[] {
				"Foo.groovy",
				"new\n"+
				"def a = 5\n"},
				"----------\n" + 
				"1. ERROR in Foo.groovy (at line 1)\n" + 
				"	new\n" + 
				"	 ^\n" + 
				"Groovy:missing type for constructor call @ line 1, column 1.\n" + 
				"----------\n");
		checkGCUDeclaration("Foo.groovy",
				"public class Foo extends groovy.lang.Script {\n" + 
				"  public Foo() {\n" + 
				"  }\n" + 
				"  public Foo(public groovy.lang.Binding context) {\n" + 
				"  }\n" + 
				"  public static void main(public java.lang.String... args) {\n" + 
				"  }\n" + 
				"  public java.lang.Object run() {\n" + 
				"  }\n" + 
				"}\n");
	}
	
	public void testParsingNewRecovery4_GRE468() {
//		if (isGroovy16()) return; // not valid on 1.6 - doesn't have a fixed parser
		this.runNegativeTest(new String[] {
				"XXX.groovy",
				"class C {\n"+
				"  public void m() {\n"+
				"  new Earth\n"+
				"     new Air\n"+
				"    new\n"+
				" new Fire\n"+
				" def leppard = 'cool'\n"+
				"  }\n"+
				"}"
				},
				"----------\n" + 
				"1. ERROR in XXX.groovy (at line 3)\n" + 
				"	new Earth\n" + 
				"	^\n" + 
				"Groovy:unable to resolve class Earth \n" + 
				"----------\n" + 
				"2. ERROR in XXX.groovy (at line 3)\n" + 
				"	new Earth\n" + 
				"	    ^\n" + 
				"Groovy:expecting \'(\' or \'[\' after type name to continue new expression @ line 3, column 7.\n" + 
				"----------\n" + 
				"3. ERROR in XXX.groovy (at line 4)\n" + 
				"	new Air\n" + 
				"	^\n" + 
				"Groovy:unable to resolve class Air \n" + 
				"----------\n" + 
				"4. ERROR in XXX.groovy (at line 4)\n" + 
				"	new Air\n" + 
				"	    ^\n" + 
				"Groovy:expecting \'(\' or \'[\' after type name to continue new expression @ line 4, column 10.\n" + 
				"----------\n" + 
				"5. ERROR in XXX.groovy (at line 5)\n" + 
				"	new\n" + 
				"	^\n" + 
				"Groovy:missing type for constructor call @ line 5, column 5.\n" + 
				"----------\n" + 
				"6. ERROR in XXX.groovy (at line 6)\n" + 
				"	new Fire\n" + 
				"	^\n" + 
				"Groovy:unable to resolve class Fire \n" + 
				"----------\n" + 
				"7. ERROR in XXX.groovy (at line 6)\n" + 
				"	new Fire\n" + 
				"	    ^\n" + 
				"Groovy:expecting \'(\' or \'[\' after type name to continue new expression @ line 6, column 6.\n" + 
				"----------\n"
				);
		checkGCUDeclaration("XXX.groovy",
				"public class C {\n" + 
				"  public C() {\n" + 
				"  }\n" + 
				"  public void m() {\n" + 
				"  }\n" + 
				"}\n");
	}
	
	/**
	 * Missing type name for new call
	 */
	public void testParsingNewRecovery5_GRE468() {
		this.runNegativeTest(new String[] {
				"Foo.groovy",
				"class C { \n"+
				" static {\n"+
				"new\n"+
				"def a = 5\n"+
				"}\n"+
				"}"},
				"----------\n" + 
				"1. ERROR in Foo.groovy (at line 3)\n" + 
				"	new\n" + 
				"	^\n" + 
				"Groovy:missing type for constructor call @ line 3, column 1.\n" + 
				"----------\n");
		checkGCUDeclaration("Foo.groovy",
				"public class C {\n" + 
				"  public C() {\n" + 
				"  }\n" + 
				"  static void <clinit>() {\n" + 
				"  }\n" + 
				"}\n");
	}
	
//	public void testOverriding_GRE440() {
//		if (isGroovy16()) return; // not valid on 1.6 - doesn't have a fixed parser
//		this.runNegativeTest(new String[] {
//				"Foo.groovy",
//				"class Foo { \n"+
//				" static void main(args) {}\n"+
//				"}\n",
//				"Goo.java",
//				"class Goo extends Foo { \n"+
//				"  public static void main(String[] argv) {}\n"+
//				"}\n",				
//				},
//				"xxxx");
//		checkGCUDeclaration("Foo.groovy",
//				"yyy");
//	}
//	
//	public void testOverriding_GRE440_2() {
//		if (isGroovy16()) return; // not valid on 1.6 - doesn't have a fixed parser
//		this.runNegativeTest(new String[] {
//				"Foo.java",
//				"class Foo { \n"+
//				" void main(String... ss) {}\n"+
//				"}\n",
//				"Goo.java",
//				"class Goo extends Foo { \n"+
//				"  void main(String[] ss) {}\n"+
//				"}\n",				
//				},
//				"xxxx");
//		checkGCUDeclaration("Foo.groovy",
//				"yyy");
//	}

	public void testParsingNewRecovery6_GRE468() {
		this.runNegativeTest(new String[] {
				"XXX.groovy",
				"class Sample {\n"+
				"  def x = new A\n"+
				"}"
		},
		"----------\n" + 
		"1. ERROR in XXX.groovy (at line 2)\n" + 
		"	def x = new A\n" + 
		"	        ^\n" + 
		"Groovy:unable to resolve class A \n" + 
		"----------\n" + 
		"2. ERROR in XXX.groovy (at line 2)\n" + 
		"	def x = new A\n" + 
		"	            ^\n" + 
		"Groovy:expecting \'(\' or \'[\' after type name to continue new expression @ line 2, column 15.\n" + 
		"----------\n"
		);
		checkGCUDeclaration("XXX.groovy",
				"public class Sample {\n" + 
				"  private java.lang.Object x;\n" + 
				"  public Sample() {\n" + 
				"  }\n" + 
		"}\n");
	}
	
	public void testParsingNewRecovery7_GRE468() {
		this.runNegativeTest(new String[] {
				"XXX.groovy",
				"import javax.swing.text.html.HTML\n"+
				"HTML h\n"+
				"new Earth"
		},
		"----------\n" + 
		"1. ERROR in XXX.groovy (at line 3)\n" + 
		"	new Earth\n" + 
		"	^\n" + 
		"Groovy:unable to resolve class Earth \n" + 
		"----------\n" + 
		"2. ERROR in XXX.groovy (at line 3)\n" + 
		"	new Earth\n" + 
		"	    ^\n" + 
		"Groovy:expecting \'(\' or \'[\' after type name to continue new expression @ line 3, column 5.\n" + 
		"----------\n"
		);
		checkGCUDeclaration("XXX.groovy",null);
	}
	
	public void testParsingNewRecovery8_GRE468() {
//		if (isGroovy16()) return; // not valid on 1.6 - doesn't have a fixed parser
		this.runNegativeTest(new String[] {
				"XXX.groovy",
				"import javax.swing.text.html.HTML\n"+
				"HTML h\n"+
				"new String()\n" 
		},"");
		checkGCUDeclaration("XXX.groovy",
				"import javax.swing.text.html.HTML;\n" + 
				"public class XXX extends groovy.lang.Script {\n" + 
				"  public XXX() {\n" + 
				"  }\n" + 
				"  public XXX(public groovy.lang.Binding context) {\n" + 
				"  }\n" + 
				"  public static void main(public java.lang.String... args) {\n" + 
				"  }\n" + 
				"  public java.lang.Object run() {\n" + 
				"  }\n" + 
		"}\n");
	}
	
//	public void testUnrecoverableErrors_GRE949() {
//		this.runNegativeTest(new String[] {
//				"Foo.groovy",
//				"package a\n" +
 //       		"import javax.swing.text.html.HTML; \n" +
 //       		"   void nuthin() {\n" +
 //       		"         if (! (this instanceof HTML/*_*/) {\n" +
 //       		"            \n" +
 //       		"         }\n" +
 //"    } "/
//		},"----------\n" + 
	//	"1. ERROR in Foo.groovy (at line 4)\n" + 
		//"	if (! (this instanceof HTML/*_*/) {\n" + 
//		"	                       ^\n" + 
	//	"Groovy:unable to resolve class HTML \n" + 
		//"----------\n" + 
//		"2. ERROR in Foo.groovy (at line 7)\n" + 
//		"	} \n" + 
//		"	^\n" + 
//		"Groovy:expecting \')\', found \'}\' @ line 7, column 5.\n" + 
//		"----------\n");
//		this.runNegativeTest(new String[] {
//				"Foo.groovy",
//				"package com.foo\n"+
//				"\n"+
//				"import javax.swing.text.html.HTML\n"+
//				"\n"+
//				"void nuthin() {\n"+
//				"if (! (this instanceof HTMLAccessibleContext/*_*/) {\n"+
//				"\n"+
//				"}\n"+
//				"}"
//				},"----------\n" + 
//				"1. ERROR in Foo.groovy (at line 6)\n" + 
//				"	if (! (this instanceof HTMLAccessibleContext/_/) {\n" + 
//				"	                                            ^\n" + 
//				"Groovy:unexpected token: / @ line 6, column 45.\n" + 
//				"----------\n");
//		ModuleNode mn = getModuleNode("Foo.groovy");
//		assertTrue(mn.encounteredUnrecoverableError());
//	}
	
	public void testUnrecoverableErrors_GRE755_2() {
		this.runNegativeTest(new String[] {
			"Bar.groovy",
			"class Bar {\n"+
			"  def x=\"\n"+
			"}\n" 
		},"----------\n" + 
		"1. ERROR in Bar.groovy (at line 2)\n" + 
		"	def x=\"\n" + 
		"}\n" + 
		"	       ^\n" + 
		"Groovy:expecting anything but \'\'\\n\'\'; got it anyway @ line 2, column 10.\n" + 
		"----------\n");
		ModuleNode mn = getModuleNode("Bar.groovy");
		assertTrue(mn.encounteredUnrecoverableError());
	}
	
	public void testUnrecoverableErrors_GRE755_3() {
		this.runNegativeTest(new String[] {
			"Bar.groovy",
			"package a\n"+
			"\n"+
			"def foo(Nuthin\n" 
		},"----------\n" + 
		"1. ERROR in Bar.groovy (at line 3)\n" + 
		"	def foo(Nuthin\n" + 
		"	        ^\n" + 
		"Groovy:unexpected token: Nuthin @ line 3, column 9.\n" + 
		"----------\n");
		ModuleNode mn = getModuleNode("Bar.groovy");
		assertTrue(mn.encounteredUnrecoverableError());
	}
		
	// variations: 'import' 'import static' 'import ' 'import static ' 'import com.' 'import static com.'
	/*
	public void testParsingNewRecoveryImports1_GRE538() {
		this.runNegativeTest(new String[] {
			"XXX.groovy",
			"import\n"+
			"\n"+
			"class Wibble {}\n" 
		},"----------\n" + 
		"1. ERROR in XXX.groovy (at line 1)\n" + 
		"	import\n" + 
		"	 ^\n" + 
		"Groovy:Invalid import specification @ line 1, column 1.\n" + 
		"----------\n");
		// import statement is not mapped from groovy to JDT world so does not appear in the declaration here
		checkGCUDeclaration("XXX.groovy",
				"public class Wibble {\n" + 
				"  public Wibble() {\n" + 
				"  }\n" + 
				"}\n");
		// check it made it through the parse though
		ModuleNode mn = getModuleNode("XXX.groovy");
		assertEquals(1,mn.getImports().size());
		ClassNode cn = ((ImportNode)mn.getImports().get(0)).getType();
		assertNull(cn);
	}

	public void testParsingNewRecoveryImports2_GRE538() {
		this.runNegativeTest(new String[] {
			"XXX.groovy",
			"import \n"+
			"\n"+
			"class Wibble {}\n" 
		},"----------\n" + 
		"1. ERROR in XXX.groovy (at line 1)\n" + 
		"	import \n" + 
		"	 ^\n" + 
		"Groovy:Invalid import specification @ line 1, column 1.\n" + 
		"----------\n");
		// import statement is not mapped from groovy to JDT world so does not appear in the declaration here
		checkGCUDeclaration("XXX.groovy",
				"public class Wibble {\n" + 
				"  public Wibble() {\n" + 
				"  }\n" + 
				"}\n");
		// check it made it through the parse though
		ModuleNode mn = getModuleNode("XXX.groovy");
		assertEquals(1,mn.getImports().size());
		ClassNode cn = ((ImportNode)mn.getImports().get(0)).getType();
		assertNull(cn);
	}
	
	public void testParsingNewRecoveryImports3_GRE538() {
		this.runNegativeTest(new String[] {
			"XXX.groovy",
			"import static \n"+
			"\n"+
			"class Wibble {}\n" 
		},"----------\n" + 
		"1. ERROR in XXX.groovy (at line 1)\n" + 
		"	import static \n" + 
		"	 ^\n" + 
		"Groovy:Invalid import specification @ line 1, column 8.\n" + 
		"----------\n");
		// import statement is not mapped from groovy to JDT world so does not appear in the declaration here
		checkGCUDeclaration("XXX.groovy",
				"public class Wibble {\n" + 
				"  public Wibble() {\n" + 
				"  }\n" + 
				"}\n");
		// check it made it through the parse though
		ModuleNode mn = getModuleNode("XXX.groovy");
		assertEquals(1,mn.getImports().size());
		ClassNode cn = ((ImportNode)mn.getImports().get(0)).getType();
		assertNull(cn);
	}

	public void testParsingNewRecoveryImports4_GRE538() {
		this.runNegativeTest(new String[] {
			"XXX.groovy",
			"import com.\n"+
			"\n"+
			"class Wibble {}\n" 
		},"----------\n" + 
		"1. ERROR in XXX.groovy (at line 1)\n" + 
		"	import\n" + 
		"	 ^\n" + 
		"Groovy:Invalid import specification @ line 1, column 1.\n" + 
		"----------\n");
		// import statement is not mapped from groovy to JDT world so does not appear in the declaration here
		checkGCUDeclaration("XXX.groovy",
				"public class Wibble {\n" + 
				"  public Wibble() {\n" + 
				"  }\n" + 
				"}\n");
		// check it made it through the parse though
		ModuleNode mn = getModuleNode("XXX.groovy");
		assertEquals(1,mn.getImports().size());
		ClassNode cn = ((ImportNode)mn.getImports().get(0)).getType();
		assertNull(cn);
	}
	
	public void testParsingNewRecoveryImports5_GRE538() {
		this.runNegativeTest(new String[] {
			"XXX.groovy",
			"import static com.\n"+
			"\n"+
			"class Wibble {}\n" 
		},"----------\n" + 
		"1. ERROR in XXX.groovy (at line 1)\n" + 
		"	import\n" + 
		"	 ^\n" + 
		"Groovy:Invalid import specification @ line 1, column 1.\n" + 
		"----------\n");
		// import statement is not mapped from groovy to JDT world so does not appear in the declaration here
		checkGCUDeclaration("XXX.groovy",
				"public class Wibble {\n" + 
				"  public Wibble() {\n" + 
				"  }\n" + 
				"}\n");
		// check it made it through the parse though
		ModuleNode mn = getModuleNode("XXX.groovy");
		assertEquals(1,mn.getImports().size());
		ClassNode cn = ((ImportNode)mn.getImports().get(0)).getType();
		assertNull(cn);
	}
	 */
	public void testAliasing_GRE473() { 
		this.runConformTest(new String[] {
				"Foo.groovy",
				"import java.util.regex.Pattern as JavaPattern\n"+
				"class Pattern {JavaPattern javaPattern}\n"+
				"def p = new Pattern(javaPattern:~/\\d+/)\n"+
				"assert \"123\" ==~ p.javaPattern\n"+
				"print 'success '\n"+
				"print '['+p.class.package+']['+JavaPattern.class.package.name+']'\n"},
				"success [null][java.util.regex]");
	}
	
	public void testAliasing_GRE473_2() {
		this.runNegativeTest(new String[] {
				"Foo.groovy",
				"import java.util.regex.Pattern\n"+
				"class Pattern {Pattern javaPattern}\n"+
				"def p = new Pattern(javaPattern:~/\\d+/)\n"+
				"assert \"123\" ==~ p.javaPattern\n"+
				"print 'success'\n"},
				"----------\n" + 
				"1. ERROR in Foo.groovy (at line 1)\n" + 
				"	import java.util.regex.Pattern\n" + 
				"	                       ^^^^^^^\n" + 
				"The import java.util.regex.Pattern conflicts with a type defined in the same file\n" + 
				"----------\n");
	}

	public void testBrokenPackage2() {
		this.runNegativeTest(new String[] {
				"Foo.groovy",
				"package ;\n"+
				"class Name { }\n"},
				"----------\n" + 
				"1. ERROR in Foo.groovy (at line 1)\n" + 
				"	package ;\n" + 
				"	 ^\n" + 
				"Groovy:Invalid package specification @ line 1, column 1.\n" + 
				"----------\n");
	}

	// does the second error now get reported after the package problem
	public void testBrokenPackage3() {
		this.runNegativeTest(new String[] {
				"Foo.groovy",
				"package ;\n"+
				"class Name { \n" +
				"  asdf\n"+
				"}\n"},
				"----------\n" + 
				"1. ERROR in Foo.groovy (at line 1)\n" + 
				"	package ;\n" + 
				"	 ^\n" + 
				"Groovy:Invalid package specification @ line 1, column 1.\n" + 
				"----------\n" + 
				"2. ERROR in Foo.groovy (at line 3)\n" + 
				"	asdf\n" + 
				"	^\n" + 
				"Groovy:unexpected token: asdf @ line 3, column 3.\n" + 
				"----------\n");
	}
		
	public void testGenericsPositions_GRE267_1() {
		this.runConformTest(new String[] {
			"X.groovy",
			"class X {\n" + 
			"  Set<?> setone;\n"+
			"  Set<? extends Serializable> settwo;\n"+
			"  Set<? super Number> setthree;\n"+
			"  public static void main(String[]argv){ print 'y';}\n"+
			"}\n",
			
			// this Java class is for comparison - breakpoint on building type bindings and you can check the decls
			"Y.java",
			"import java.util.*;\n"+
			"class Y {\n" + 
			"  Set<?> a;\n"+
			"  Set<? extends java.io.Serializable> b;\n"+
			"  Set<? super Number> c;\n"+
			"}\n",
		},"y");		

		GroovyCompilationUnitDeclaration decl = getCUDeclFor("X.groovy");
		
		FieldDeclaration fDecl = null;
		
		fDecl = grabField(decl,"setone");
		assertEquals("(12>14)Set<(16>16)?>",stringify(fDecl.type));
		
		fDecl = grabField(decl,"settwo");
		assertEquals("(29>31)Set<(33>54)? extends (43>54)Serializable>",stringify(fDecl.type));
		
		fDecl = grabField(decl,"setthree");
		assertEquals("(67>69)Set<(71>84)? super (79>84)Number>",stringify(fDecl.type));
	}
	

	

	public void testGenericsPositions_GRE267_2() {
		this.runConformTest(new String[] {
			"X.groovy",
			"class X {\n" + 
			"  Set<?> setone;\n"+
			"  Set<? extends java.io.Serializable> settwo;\n"+
			"  Set<? super java.lang.Number> setthree;\n"+
			"  public static void main(String[]argv){ print 'y';}\n"+
			"}\n",
		},"y");		

		GroovyCompilationUnitDeclaration decl = getCUDeclFor("X.groovy");
		
		FieldDeclaration fDecl = null;
		
		fDecl = grabField(decl,"setone");
		assertEquals("(12>14)Set<(16>16)?>",stringify(fDecl.type));
		
		fDecl = grabField(decl,"settwo");
		assertEquals("(29>31)Set<(33>62)? extends (43>62)(43>46)java.(48>49)io.(51>62)Serializable>",stringify(fDecl.type));
		
		fDecl = grabField(decl,"setthree");
		assertEquals("(75>77)Set<(79>102)? super (87>102)(87>90)java.(92>95)lang.(97>102)Number>",stringify(fDecl.type));
	}
	
	public void testEnumPositions_GRE1072() {
		this.runConformTest(new String[] {
			"X.groovy",
			"enum Color {\n" + 
			"  /** hello */\n"+
			"  RED,\n"+
			"  GREEN,\n"+
			"  BLUE\n"+
			"}\n",
		},"");		

		GroovyCompilationUnitDeclaration decl = getCUDeclFor("X.groovy");
		
		FieldDeclaration fDecl = null;
		
		fDecl = grabField(decl,"RED");
		if (GroovyUtils.GROOVY_LEVEL<18) {
			assertEquals("RED sourceStart>sourceEnd:30>32 declSourceStart>declSourceEnd:30>31 modifiersSourceStart=0 endPart1Position:30",stringifyFieldDecl(fDecl));
		} else {
			assertEquals("RED sourceStart>sourceEnd:30>32 declSourceStart>declSourceEnd:15>31 modifiersSourceStart=30 endPart1Position:30",stringifyFieldDecl(fDecl));
		
		}
		
		fDecl = grabField(decl,"GREEN");
		if (GroovyUtils.GROOVY_LEVEL<18) {
			assertEquals("GREEN sourceStart>sourceEnd:37>41 declSourceStart>declSourceEnd:37>40 modifiersSourceStart=0 endPart1Position:37",stringifyFieldDecl(fDecl));
		} else {
			assertEquals("GREEN sourceStart>sourceEnd:37>41 declSourceStart>declSourceEnd:37>40 modifiersSourceStart=37 endPart1Position:37",stringifyFieldDecl(fDecl));
		}
		fDecl = grabField(decl,"BLUE");
		if (GroovyUtils.GROOVY_LEVEL<18) {
			assertEquals("BLUE sourceStart>sourceEnd:46>49 declSourceStart>declSourceEnd:46>48 modifiersSourceStart=0 endPart1Position:46",stringifyFieldDecl(fDecl));
		} else {
			assertEquals("BLUE sourceStart>sourceEnd:46>49 declSourceStart>declSourceEnd:46>48 modifiersSourceStart=46 endPart1Position:46",stringifyFieldDecl(fDecl));
		}
	}
	
	static class Requestor implements ICompilerRequestor {

		public void acceptResult(CompilationResult result) {
			int stop =1;
		}
		
	}
	public void testEnumValues_GRE1071() {
		runConformTest(new String[]{
				"X.groovy",
				"enum H {\n"+
				"  RED,\n"+
				"  BLUE\n"+
				"}"},"");

		assertEquals("[LH;",getReturnTypeOfMethod("X.groovy", "values"));
	}

	private String stringifyFieldDecl(FieldDeclaration fDecl) {
		StringBuffer sb = new StringBuffer();
		sb.append(fDecl.name);
		sb.append(" sourceStart>sourceEnd:"+fDecl.sourceStart+">"+fDecl.sourceEnd);
		sb.append(" declSourceStart>declSourceEnd:"+fDecl.declarationSourceStart+">"+fDecl.declarationSourceEnd);
		sb.append(" modifiersSourceStart="+fDecl.modifiersSourceStart); // first char of decls modifiers
		sb.append(" endPart1Position:"+fDecl.endPart1Position); // char after type decl ('int x,y' is space)
		return sb.toString();
	}

	
	public void testGenericsPositions_GRE267_3() {
		this.runConformTest(new String[] {
			"X.groovy",
			"class X {\n" + 
			"  Set<?> setone;\n"+
			"  Set<String[]> settwo;\n"+
			"  Set<String[][]> setthree;\n"+
			"  Set<java.lang.Number[][][]> setfour;\n"+
			"  public static void main(String[]argv){ print 'y';}\n"+
			"}\n",
			"Y.java",
			"import java.util.*;\n"+
			"class Y {\n" + 
			"  Set<String[]> a;\n"+
			"  Set<String[][]> b;\n"+
			"  Set<java.lang.Number[][][]> c;\n"+ 
			"}\n",
		},"y");		

		GroovyCompilationUnitDeclaration decl = getCUDeclFor("X.groovy");
		
		FieldDeclaration fDecl = null;
		
		fDecl = grabField(decl,"setone");
		assertEquals("(12>14)Set<(16>16)?>",stringify(fDecl.type));
		
		fDecl = grabField(decl,"settwo");
		assertEquals("(29>31)Set<(33>40 ose:38)String[]>",stringify(fDecl.type));
		
		fDecl = grabField(decl,"setthree");
		assertEquals("(53>55)Set<(57>66 ose:62)String[][]>",stringify(fDecl.type));
		
		fDecl = grabField(decl,"setfour");
		assertEquals("(81>83)Set<(85>106)(85>88)java.(90>93)lang.(95>100)Number[][][]>",stringify(fDecl.type));
	}
	

	
	
	// FIXASC appears to be a groovy bug - the java.util.Set is missing generics info - as if it had none
//	public void testGenericsPositions_4_GRE267() {
//		this.runConformTest(new String[] { 
//			"X.groovy",
//			"class X {\n" + 
////			"  java.util.Set<?> setone;\n"+
////			"  java.util.Set<? extends Serializable> settwo;\n"+
////			"  java.util.Set<? super Number> setthree;\n"+
//			"  public static void main(String[]argv){ print 'y';}\n"+
//			"}\n",
//		},"y");		
//
//		GroovyCompilationUnitDeclaration decl = getDecl("X.groovy");
//		
//		FieldDeclaration fDecl = null;
//		
//		fDecl = grabField(decl,"setone");
//		assertEquals("x",stringify(fDecl.type));
//		
////		fDecl = grabField(decl,"settwo");
////		assertEquals("y",stringify(fDecl.type));
////		
////		fDecl = grabField(decl,"setthree");
////		assertEquals("z",stringify(fDecl.type));
//	}
	
//	public void testGenericsPositions_5_GRE267() {
//		this.runConformTest(new String[] {
//			"X.groovy",
//			"class X {\n" + 
//			"  java.util.Set<?> setone;\n"+
//			"  java.util.Set<? extends java.io.Serializable> settwo;\n"+
//			"  java.util.Set<? super java.lang.Number> setthree;\n"+
//			"  public static void main(String[]argv){ print 'y';}\n"+
//			"}\n",
//		},"y");		
//
//		GroovyCompilationUnitDeclaration decl = getDecl("X.groovy");
//		
//		FieldDeclaration fDecl = null;
//		
//		fDecl = grabField(decl,"setone");
//		assertEquals("(12>14)Set<(16>16)?>",stringify(fDecl.type));
//		
//		fDecl = grabField(decl,"settwo");
//		assertEquals("(29>31)Set<(33>33)? extends (43>61)(43>47)java.(48>50)io.(51>61)Serializable>",stringify(fDecl.type));
//		
//		fDecl = grabField(decl,"setthree");
//		assertEquals("(67>69)Set<(71>71)? super (79>84)Number>",stringify(fDecl.type));
//	}

	// FIXASC check tests after porting to recent 1.7 compiler
//	// Multiple generified components in a reference
//	public void testGenericsPositions_6_GRE267() {
//		this.runConformTest(new String[] {
//			"X.groovy",
//			"class X {\n" + 
//			"  One<String,Integer>.Two<Boolean> whoa;\n"+
////			"  java.util.Set<? extends java.io.Serializable> settwo;\n"+
////			"  java.util.Set<? super java.lang.Number> setthree;\n"+
//			"  public static void main(String[]argv){ print 'y';}\n"+
//			"}\n",
//			"One.java",
//			"public class One<A,B> {\n"+
//			"	  class Two<C> {\n"+
//			"	  }\n"+
//			"	}\n"
//		},"y");		
//
//		GroovyCompilationUnitDeclaration decl = getDecl("X.groovy");
//		
//		FieldDeclaration fDecl = null;
//		
//		fDecl = grabField(decl,"one");
//		assertEquals("(12>14)Set<(16>16)?>",stringify(fDecl.type));
//		
//		fDecl = grabField(decl,"settwo");
//		assertEquals("(29>31)Set<(33>33)? extends (43>61)(43>47)java.(48>50)io.(51>61)Serializable>",stringify(fDecl.type));
//		
//		fDecl = grabField(decl,"setthree");
//		assertEquals("(67>69)Set<(71>71)? super (79>84)Number>",stringify(fDecl.type));
//	}

//	public void testGenericsPositions_7_GRE267() {
//		this.runConformTest(new String[] {
//			"X.groovy",
//			"class X {\n" + 
//			"  java.util.Set<?> setone;\n"+
//			"  java.util.Set<String[]> settwo;\n"+
//			"  java.util.Set<java.lang.Number[][][]> setthree;\n"+
//			"  public static void main(String[]argv){ print 'y';}\n"+
//			"}\n",
//		},"y");		
//
//		GroovyCompilationUnitDeclaration decl = getDecl("X.groovy");
//		
//		FieldDeclaration fDecl = null;
//		
//		fDecl = grabField(decl,"setone");
//		assertEquals("(12>14)Set<(16>16)?>",stringify(fDecl.type));
//		
//		fDecl = grabField(decl,"settwo");
//		assertEquals("(29>31)Set<(33>33)? extends (43>54)Serializable>",stringify(fDecl.type));
//		
//		fDecl = grabField(decl,"setthree");
//		assertEquals("(67>69)Set<(71>71)? super (79>84)Number>",stringify(fDecl.type));
//	}
	
//	public void testGenericsPositions_8_GRE267() {
//		this.runConformTest(new String[] {
//			"X.groovy",
//			"class X {\n" + 
//			"  Set<Map.Entry<String,List<String>>> foo;\n"+
//			"  public static void main(String[]argv){ print 'y';}\n"+
//			"}\n",
//		},"y");		
//	
//		GroovyCompilationUnitDeclaration decl = getDecl("X.groovy");
//		
//		FieldDeclaration fDecl = null;
//		
//		fDecl = grabField(decl,"foo");
//		assertEquals("(12>14)Set<(16>16)?>",stringify(fDecl.type));
//	}
	
//	public void testGenericsPositions_9_GRE267() {
//		this.runConformTest(new String[] {
//			"X.groovy",
//			"class X {\n" + 
//			"  Map.Entry<String,List<String>> foo;\n"+
//			"  public static void main(String[]argv){ print 'y';}\n"+
//			"}\n",
//		},"y");		
//	
//		GroovyCompilationUnitDeclaration decl = getDecl("X.groovy");
//		
//		FieldDeclaration fDecl = null;
//		
//		fDecl = grabField(decl,"foo");
//		assertEquals("(12>14)Set<(16>16)?>",stringify(fDecl.type));
//	}

	public void testAbstractCovariance_GRE272() {
		this.runNegativeTest(new String[] {
				"A.java",
				"public class A {}",
				
				"AA.java",
				"public class AA extends A{}",

				"I.java",
				"public interface I { A getA();}",
				
				"Impl.java",
				"public class Impl implements I { public AA getA() {return null;}}",
				
				"GImpl.groovy",
				"class GImpl extends Impl {}"
		},"");
	}

	// If GroovyFoo is processed *before* FooBase then the MethodVerifier15 
	// hasn't had a chance to run on FooBase and create the synthetic bridge method
	public void testAbstractCovariance_GRE272_2() {
		this.runNegativeTest(new String[] {
				"test/Bar.java",
				"package test;\n"+
				"public class Bar extends BarBase {}",

				"test/BarBase.java",
				"package test;\n"+
				"abstract public class BarBase {}",
				
				"test/GroovyFoo.groovy",
				"package test;\n"+
				"class GroovyFoo extends FooBase {}",

				"test/FooBase.java",
				"package test;\n"+
				"public class FooBase implements IFoo { public Bar foo() {return null;}}",
								

				"test/IFoo.java",
				"package test;\n"+
				"public interface IFoo { BarBase foo();}",
		},"");
	}

	public void testAbstractCovariance_GRE272_3() {
		this.runNegativeTest(new String[] {
				"test/IFoo.java",
				"package test;\n"+
				"public interface IFoo { BarBase foo();}",

				"test/GroovyFoo.groovy",
				"package test;\n"+
				"class GroovyFoo extends FooBase {}",

				"test/FooBase.java",
				"package test;\n"+
				"public class FooBase implements IFoo { public Bar foo() {return null;}}",


				"test/BarBase.java",
				"package test;\n"+
				"abstract public class BarBase {}",
				
				"test/Bar.java",
				"package test;\n"+
				"public class Bar extends BarBase {}",

		},"");
	}

	public void testAbstractCovariance_GRE272_4() {
		this.runNegativeTest(new String[] {
				"test/IFoo.java",
				"package test;\n"+
				"public interface IFoo { BarBase foo();}",

				"test/FooBase.java",
				"package test;\n"+
				"public class FooBase implements IFoo { public Bar foo() {return null;}}",

				"test/BarBase.java",
				"package test;\n"+
				"abstract public class BarBase {}",
				
				"test/Bar.java",
				"package test;\n"+
				"public class Bar extends BarBase {}",
				
				"test/GroovyFoo.groovy",
				"package test;\n"+
				"class GroovyFoo extends FooBase {}",
		},"");
	}
	
	public void testIncorrectReturnType_GRE292() {
		this.runNegativeTest(new String[] {
				"Voidy.groovy",
				"public class VoidReturnTestCase {\n"+
				"\n"+
				"  void returnSomething() { \n"+
				"    return true && false   \n" +
				"  }\n"+
				"\n"+
				"}\n"},
				"----------\n" + 
				"1. ERROR in Voidy.groovy (at line 4)\n" + 
				"	return true && false   \n" + 
				"	^^^^^^^^^^^^^^^^^^^^\n" + 
				"Groovy:Cannot use return statement with an expression on a method that returns void\n" + 
				"----------\n");
	}
	
	public void testIncorrectReturnType_GRE292_3() {
		this.runNegativeTest(new String[] {
				"Voidy.groovy",
				"public class VoidReturnTestCase {\n"+
				"\n"+
				"  void returnSomething() { \n"+
				"    return true\n" +
				"  }\n"+
				"\n"+
				"}\n"},
				"----------\n" + 
				"1. ERROR in Voidy.groovy (at line 4)\n" + 
				"	return true\n" + 
				"	^^^^^^^^^^^\n" + 
				"Groovy:Cannot use return statement with an expression on a method that returns void\n" + 
				"----------\n");
	}


	public void testIncorrectReturnType_GRE292_2() {
		this.runNegativeTest(new String[] {
				"Voidy.groovy",
				"public class VoidReturnTestCase {\n"+
				"\n"+
				"  void returnSomething() { return true }\n"+
				"\n"+
				"}\n"},
				"----------\n"+
				"1. ERROR in Voidy.groovy (at line 3)\n" + 
				"	void returnSomething() { return true }\n" + 
				"	                         ^^^^^^^^^^^\n" + 
				"Groovy:Cannot use return statement with an expression on a method that returns void\n" + 
				"----------\n");
	}
	
	public void testIncorrectReturnType_GRE292_4() {
		this.runNegativeTest(new String[] {
				"Voidy.groovy",
				"public class VoidReturnTestCase {\n"+
				"\n"+
				" void returnSomething() { return 375+26 }\n"+
				"\n"+
				"}\n"},
				"----------\n" + 
				"1. ERROR in Voidy.groovy (at line 3)\n" + 
				"	void returnSomething() { return 375+26 }\n" + 
				"	                         ^^^^^^^^^^^^^\n" + 
				"Groovy:Cannot use return statement with an expression on a method that returns void\n" + 
				"----------\n");
	}

	public void testMissingTypesForGeneratedBindingsGivesNPE_GRE273() {
		this.runNegativeTest(new String[] {
			"X.groovy",
			"import java.util.Map\n"+
			"import org.andrill.coretools.data.edit.Command\n"+
			"import org.andrill.coretools.data.edit.EditableProperty\n"+
			"import org.andrill.coretools.data.Model\n"+
			"import org.andrill.coretools.data.ModelCollection\n"+
			"import org.andrill.coretools.data.edit.commands.CompositeCommand\n"+
			"\n"+
			"class GProperty implements EditableProperty {\n"+
			"def source\n"+
			"String name\n"+
			"String widgetType\n"+
			"Map widgetProperties = [:]\n"+
			"Map constraints = [:]\n"+
			"def validators = []\n"+
			"Command command\n"+
			"\n"+
			"String getValue() {\n"+
			"if (source instanceof Model) { return source.modelData[name] } else { return (source.\"$name\" as String) }\n"+
			"}\n"+
			"\n"+
			"boolean isValid(String newValue) {\n"+
			"try {\n"+
			"return validators.inject(true) { prev, cur -> prev && cur.call([newValue, source]) }\n"+
			"} catch (e) { return false }\n"+
			"}\n"+
			"\n"+
			"Command getCommand(String newValue) {\n"+
			"if (constraints?.linkTo && source instanceof Model) {\n"+
			"def value = source.\"$name\"\n"+
			"def links = source.collection.models.findAll { it.class == source.class && it?.\"${constraints.linkTo}\" == value }\n"+
			"if (links) {\n"+
			"def commands = []\n"+
			"commands << new GCommand(source: source, prop: name, value: newValue)\n"+
			"links.each { commands << new GCommand(source: it, prop: constraints.linkTo, value: newValue) }\n"+
			"return new CompositeCommand(\"Change $name\", (commands as Command[]))\n"+
			"} else { return new GCommand(source: source, prop: name, value: newValue) }\n"+
			"} else { return new GCommand(source: source, prop: name, value: newValue) }\n"+
			"}\n"+
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.groovy (at line 2)\n" + 
			"	import org.andrill.coretools.data.edit.Command\n" + 
			"	                                       ^\n" + 
			"Groovy:unable to resolve class org.andrill.coretools.data.edit.Command\n" + 
			"----------\n" + 
			"2. ERROR in X.groovy (at line 3)\n" + 
			"	import org.andrill.coretools.data.edit.EditableProperty\n" + 
			"	                                       ^\n" + 
			"Groovy:unable to resolve class org.andrill.coretools.data.edit.EditableProperty\n" + 
			"----------\n" + 
			"3. ERROR in X.groovy (at line 4)\n" + 
			"	import org.andrill.coretools.data.Model\n" + 
			"	                                  ^\n" + 
			"Groovy:unable to resolve class org.andrill.coretools.data.Model\n" + 
			"----------\n" + 
			"4. ERROR in X.groovy (at line 5)\n" + 
			"	import org.andrill.coretools.data.ModelCollection\n" + 
			"	                                  ^\n" + 
			"Groovy:unable to resolve class org.andrill.coretools.data.ModelCollection\n" + 
			"----------\n" + 
			"5. ERROR in X.groovy (at line 6)\n" + 
			"	import org.andrill.coretools.data.edit.commands.CompositeCommand\n" + 
			"	                                                ^\n" + 
			"Groovy:unable to resolve class org.andrill.coretools.data.edit.commands.CompositeCommand\n" + 
			"----------\n" + 
			"6. ERROR in X.groovy (at line 8)\n" + 
			"	class GProperty implements EditableProperty {\n" + 
			"	      ^^^^^^^^^\n" + 
			"Groovy:You are not allowed to implement the class \'org.andrill.coretools.data.edit.EditableProperty\', use extends instead.\n" + 
			"----------\n" + 
			"7. WARNING in X.groovy (at line 12)\n" + 
			"	Map widgetProperties = [:]\n" + 
			"	^^^\n" + 
			"Map is a raw type. References to generic type Map<K,V> should be parameterized\n" + 
			"----------\n" + 
			"8. WARNING in X.groovy (at line 13)\n" + 
			"	Map constraints = [:]\n" + 
			"	^^^\n" + 
			"Map is a raw type. References to generic type Map<K,V> should be parameterized\n" + 
			"----------\n" + 
//			"9. ERROR in X.groovy (at line 15)\n" + 
//			"	Command command\n" + 
//			"	^\n" + 
//			"Groovy:unable to resolve class org.andrill.coretools.data.edit.Command \n" + 
//			"----------\n" + 
			"9. ERROR in X.groovy (at line 33)\n" + 
			"	commands << new GCommand(source: source, prop: name, value: newValue)\n" + 
			"	            ^\n" + 
			"Groovy:unable to resolve class GCommand \n" + 
			"----------\n" + 
			"10. ERROR in X.groovy (at line 34)\n" + 
			"	links.each { commands << new GCommand(source: it, prop: constraints.linkTo, value: newValue) }\n" + 
			"	                         ^\n" + 
			"Groovy:unable to resolve class GCommand \n" + 
			"----------\n" + 
			"11. ERROR in X.groovy (at line 36)\n" + 
			"	} else { return new GCommand(source: source, prop: name, value: newValue) }\n" + 
			"	                ^\n" + 
			"Groovy:unable to resolve class GCommand \n" + 
			"----------\n" + 
			"12. ERROR in X.groovy (at line 37)\n" + 
			"	} else { return new GCommand(source: source, prop: name, value: newValue) }\n" + 
			"	                ^\n" + 
			"Groovy:unable to resolve class GCommand \n" + 
			"----------\n");
	}
	
	public void testMissingTypesForGeneratedBindingsGivesNPE_GRE273_2() {
		this.runNegativeTest(new String[] {
				"A.groovy",
				"class A {\n"+
				"  String s;"+
				"  String getS(String foo) { return null;}\n"+
				"}"
		},"");
	}
	

	public void testAbstractClass_GRE274() {
		this.runNegativeTest(new String[] {
			"p/Foo.groovy",
			"class Foo {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    new C();\n"+
			"  }\n"+
			"}\n",

			"p/C.java",
			"package p;\n" + 
			"public abstract class C {\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in p\\Foo.groovy (at line 3)\n" + 
		"	new C();\n" + 
		"	^\n" + 
		"Groovy:unable to resolve class C \n" + 
		"----------\n");		
	}

	
	public void testAbstractClass_GRE274_2() {
		this.runNegativeTest(new String[] {
			"p/Foo.groovy",
			"class Foo {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    new Wibble();\n"+
			"  }\n"+
			"}\n",

			"Wibble.groovy",
			"@SuppressWarnings(\"cast\")\n"+
			"public class Wibble implements Comparable<String> {\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in Wibble.groovy (at line 2)\n" + 
		"	public class Wibble implements Comparable<String> {\n" + 
		"	             ^^^^^^\n" + 
		"Groovy:Can\'t have an abstract method in a non-abstract class. The class \'Wibble\' must be declared abstract or the method \'int compareTo(java.lang.Object)\' must be implemented.\n" + 
		"----------\n");		
	}

	// FIXASC line number wrong for the errors
	public void testGenericsAndGroovyJava_GRE278_1() {
		this.runNegativeTest(new String[] {
			"p/Field.java",
			"package test;\n"+
			"public interface Field<T> extends Comparable<T> {\n"+
			"    public String getFieldTypeName();\n"+
			"    public String getName();\n"+
			"    public T getValue();\n"+
			"    public void setValue(T o);\n"+
			"}\n",
			
			"p/Structure.java",
			"package test;\n"+
			"import java.util.Map;\n"+
			"import java.nio.ByteBuffer;\n"+
			"public interface Structure extends Map<String, Field<?>> {\n"+
			"   public void reset();\n"+
			"	public void setup(ByteBuffer clientBuff);\n"+
			"}\n",
			
			"p/StructureBase.groovy",
			"package test;\n"+
			"\n"+
			"import java.nio.ByteBuffer;\n"+
			"\n"+
			"@SuppressWarnings(\"rawtypes\")\n"+
			"public class StructureBase implements Structure {\n"+
			"\n"+
			"	protected final Structure str = null;\n"+
			"	\n"+
			"	StructureBase(Structure struct){\n"+
			"		this.str = struct;\n"+
			"	}\n"+
			"	\n"+
			"	public void clear() {\n"+
			"		str.clear()\n"+
			"	}\n"+
			"\n"+
			"	public boolean containsKey(Object arg0) {\n"+
			"		return str.containsKey(arg0);\n"+
			"	}\n"+
			"\n"+
			"	public boolean containsValue(Object arg0) {\n"+
			"		return str.containsValue(arg0);\n"+
			"	}\n"+
			"\n"+
			"	public Set<java.util.Map.Entry<String, Field<?>>> entrySet() {\n"+
			"		return Collections.unmodifiableSet(str.entrySet());\n"+
			"	}\n"+
			"\n"+
			"	public Field<?> get(Object arg0) {\n"+
			"		return str.get(arg0);\n"+
			"	}\n"+
			"\n"+
			"	public boolean isEmpty() {\n"+
			"		return str.isEmpty();\n"+
			"	}\n"+
			"\n"+
			"	public Set<String> keySet() {\n"+
			"		return Collections.unmodifiableSet(str.keySet());\n"+
			"	}\n"+
			"\n"+
			"	public Field<?> put(String arg0, Field<?> arg1) {\n"+
			"		return str.put(arg0, arg1);\n"+
			"	}\n"+
			"	\n"+
			"	public Object put(Object key, Object value) {\n"+
			"		return str.put(key, value)\n"+
			"	}\n"+
			"\n"+
			"	public void putAll(Map<? extends String, ? extends Field<?>> arg0) {\n"+
			"		str.putAll(arg0);\n"+
			"	}\n"+
			"\n"+
			"	public Field<?> remove(Object key) {\n"+
			"		return str.remove(key);\n"+
			"	}\n"+
			"\n"+
			"	public int size() {\n"+
			"		return str.size();\n"+
			"	}\n"+
			"\n"+
			"	public Collection<Field<?>> values() {\n"+
			"		return Collections.unmodifiableCollection(str.values());\n"+
			"	}\n"+
			"//	\n"+
			"//	public void reset(){\n"+
			"//		str.reset();\n"+
			"//	}\n"+
			"//	\n"+
			"//	public void setup(ByteBuffer buff) {\n"+
			"//		str.setup(buff);\n"+
			"//	}\n"+
			"	\n"+
			"}\n"+
			"\n",
			
			"p/StructureBaseTest.groovy",
			"package test;\n"+
			"\n"+
			"public class StructureBaseTest {\n"+
			"\n"+
			"	public static void main(String[] args) {\n"+
			"		 Structure str = new StructureBase(new TestStructure());\n"+
			"		 str.put('test', new TestField());\n"+
			"			def content = str.get('test');\n"+
			"			if (!TestField.FIELD_NAME.equals(str.get('test').name)) {\n"+
			"				System.out.println(\"Failed\");\n"+
			"			}\n"+
			"	}\n"+
			"}\n",
			
			"p/TestField.java",
			"package test;\n"+
			"\n"+
			"public class TestField implements Field<String> {\n"+
			"\n"+
			"	public static final String FIELD_NAME = \"Test\";\n"+
			"	private StringBuilder buffer = new StringBuilder();\n"+
			"	private String value = null;\n"+
//			"	@Override\n"+
			"	public String getFieldTypeName() {\n"+
			"		return String.class.getSimpleName();\n"+
			"	}\n"+
			"\n"+
//			"	@Override\n"+
			"	public String getName() {\n"+
			"		return FIELD_NAME;\n"+
			"	}\n"+
			"\n"+
//			"	@Override\n"+
			"	public String getValue() {\n"+
			"		if (null == value)\n"+
			"			value = buffer.toString();\n"+
			"		return value;\n"+
			"	}\n"+
			"\n"+
//			"	@Override\n"+
			"	public void setValue(String o) {\n"+
			"		value = o; \n"+
			"		buffer.replace(0, buffer.length(), o);\n"+
			"	}\n"+
			"\n"+
//			"	@Override\n"+
			"	public int compareTo(String arg0) {\n"+
			"		return getValue().compareTo(arg0);\n"+
			"	}\n"+
			"\n"+
			"}\n",
			
			"p/TestStructure.java",
			"package test;\n"+
			"import java.nio.ByteBuffer;\n"+
			"import java.util.HashMap;\n"+
			"import java.util.Map;\n"+
			"\n"+
			"\n"+
			"@SuppressWarnings(\"serial\")\n"+
			"public class TestStructure extends HashMap<String, Field<?>> implements Structure {\n"+
			"\n"+
//			"	@Override\n"+
			"	public void reset() {\n"+
			"		// TODO Auto-generated method stub\n"+
			"\n"+
			"	}\n"+
			"\n"+
//			"	@Override\n"+
			"	public void setup(ByteBuffer clientBuff) {\n"+
			"		// TODO Auto-generated method stub\n"+
			"\n"+
			"	}\n"+
			"\n"+
//			"	@Override\n"+
			"	public Field<?> put(String arg0, Field<?> arg1) {\n"+
			"		return super.put(arg0, arg1);\n"+
			"}\n"+
			"\n"+
//			"	@Override\n"+
			"	public void putAll(Map<? extends String, ? extends Field<?>> arg0) {\n"+
			"		super.putAll(arg0);\n"+
			"	}\n"+
			"\n"+
			"}\n",

		},"----------\n" + 
		"1. ERROR in p\\StructureBase.groovy (at line 6)\n" + 
		"	public class StructureBase implements Structure {\n" + 
		"	             ^^^^^^^^^^^^^\n" + 
		"Groovy:Can\'t have an abstract method in a non-abstract class. The class \'test.StructureBase\' must be declared abstract or the method \'void setup(java.nio.ByteBuffer)\' must be implemented.\n" + 
		"----------\n" + 
		"2. ERROR in p\\StructureBase.groovy (at line 6)\n" + 
		"	public class StructureBase implements Structure {\n" + 
		"	             ^^^^^^^^^^^^^\n" + 
		"Groovy:Can\'t have an abstract method in a non-abstract class. The class \'test.StructureBase\' must be declared abstract or the method \'void reset()\' must be implemented.\n" + 
		"----------\n");		
	}

	public void testGenericsAndGroovyJava_GRE278_2() {
		this.runConformTest(new String[] {
			"Main.java",
			"public class Main {\n"+
			"  public static void main(String[]argv) {\n"+
			"    test.StructureBaseTest.main(argv);\n"+
			"  }\n"+
			"}\n",
			
			"p/Field.java",
			"package test;\n"+
			"public interface Field<T> extends Comparable<T> {\n"+
			"    public String getFieldTypeName();\n"+
			"    public String getName();\n"+
			"    public T getValue();\n"+
			"    public void setValue(T o);\n"+
			"}\n",
			
			"p/Structure.java",
			"package test;\n"+
			"import java.util.Map;\n"+
			"import java.nio.ByteBuffer;\n"+
			"public interface Structure extends Map<String, Field<?>> {\n"+
			"   public void reset();\n"+
			"	public void setup(ByteBuffer clientBuff);\n"+
			"}\n",
			
			"p/StructureBase.groovy",
			"package test;\n"+
			"\n"+
			"import java.nio.ByteBuffer;\n"+
			"\n"+
			"@SuppressWarnings(\"unchecked\")\n"+
			"public class StructureBase implements Structure {\n"+
			"\n"+
			"	protected final Structure str = null;\n"+
			"	\n"+
			"	StructureBase(Structure struct){\n"+
			"		this.str = struct;\n"+
			"	}\n"+
			"	\n"+
			"	public void clear() {\n"+
			"		str.clear()\n"+
			"	}\n"+
			"\n"+
			"	public boolean containsKey(Object arg0) {\n"+
			"		return str.containsKey(arg0);\n"+
			"	}\n"+
			"\n"+
			"	public boolean containsValue(Object arg0) {\n"+
			"		return str.containsValue(arg0);\n"+
			"	}\n"+
			"\n"+
			"	public Set<java.util.Map.Entry<String, Field<?>>> entrySet() {\n"+
			"		return Collections.unmodifiableSet(str.entrySet());\n"+
			"	}\n"+
			"\n"+
			"	public Field<?> get(Object arg0) {\n"+
			"		return str.get(arg0);\n"+
			"	}\n"+
			"\n"+
			"	public boolean isEmpty() {\n"+
			"		return str.isEmpty();\n"+
			"	}\n"+
			"\n"+
			"	public Set<String> keySet() {\n"+
			"		return Collections.unmodifiableSet(str.keySet());\n"+
			"	}\n"+
			"\n"+
			"	public Field<?> put(String arg0, Field<?> arg1) {\n"+
			"		return str.put(arg0, arg1);\n"+
			"	}\n"+
			"	\n"+
			"	public Object put(Object key, Object value) {\n"+
			"		return str.put(key, value)\n"+
			"	}\n"+
			"\n"+
			"	public void putAll(Map<? extends String, ? extends Field<?>> arg0) {\n"+
			"		str.putAll(arg0);\n"+
			"	}\n"+
			"\n"+
			"	public Field<?> remove(Object key) {\n"+
			"		return str.remove(key);\n"+
			"	}\n"+
			"\n"+
			"	public int size() {\n"+
			"		return str.size();\n"+
			"	}\n"+
			"\n"+
			"	public Collection<Field<?>> values() {\n"+
			"		return Collections.unmodifiableCollection(str.values());\n"+
			"	}\n"+
			"\n"+
			"	public void reset(){\n"+
			"		str.reset();\n"+
			"	}\n"+
			"\n"+
			"	public void setup(ByteBuffer buff) {\n"+
			"		str.setup(buff);\n"+
			"	}\n"+
			"\n"+
			"}\n"+
			"\n",
			
			"p/StructureBaseTest.groovy",
			"package test;\n"+
			"\n"+
			"public class StructureBaseTest {\n"+
			"\n"+
			"	public static void main(String[] args) {\n"+
			"		 Structure str = new StructureBase(new TestStructure());\n"+
			"		 str.put('test', new TestField());\n"+
			"			def content = str.get('test');\n"+
			"			if (!TestField.FIELD_NAME.equals(str.get('test').name)) {\n"+
			"				System.out.println(\"Failed\");\n"+
			"			}\n"+
			"	}\n"+
			"}\n",
			
			"p/TestField.java",
			"package test;\n"+
			"\n"+
			"public class TestField implements Field<String> {\n"+
			"\n"+
			"	public static final String FIELD_NAME = \"Test\";\n"+
			"	private StringBuilder buffer = new StringBuilder();\n"+
			"	private String value = null;\n"+
			"	public String getFieldTypeName() {\n"+
			"		return String.class.getSimpleName();\n"+
			"	}\n"+
			"\n"+
			"	public String getName() {\n"+
			"		return FIELD_NAME;\n"+
			"	}\n"+
			"\n"+
			"	public String getValue() {\n"+
			"		if (null == value)\n"+
			"			value = buffer.toString();\n"+
			"		return value;\n"+
			"	}\n"+
			"\n"+
			"	public void setValue(String o) {\n"+
			"		value = o; \n"+
			"		buffer.replace(0, buffer.length(), o);\n"+
			"	}\n"+
			"\n"+
			"	public int compareTo(String arg0) {\n"+
			"		return getValue().compareTo(arg0);\n"+
			"	}\n"+
			"\n"+
			"}\n",
			
			"p/TestStructure.java",
			"package test;\n"+
			"import java.nio.ByteBuffer;\n"+
			"import java.util.HashMap;\n"+
			"import java.util.Map;\n"+
			"\n"+
			"\n"+
			"@SuppressWarnings(\"serial\")\n"+
			"public class TestStructure extends HashMap<String, Field<?>> implements Structure {\n"+
			"\n"+
			"	public void reset() {\n"+
			"		// TODO Auto-generated method stub\n"+
			"\n"+
			"	}\n"+
			"\n"+
			"	public void setup(ByteBuffer clientBuff) {\n"+
			"		// TODO Auto-generated method stub\n"+
			"\n"+
			"	}\n"+
			"\n"+
			"	public Field<?> put(String arg0, Field<?> arg1) {\n"+
			"		return super.put(arg0, arg1);\n"+
			"}\n"+
			"\n"+
			"	public void putAll(Map<? extends String, ? extends Field<?>> arg0) {\n"+
			"		super.putAll(arg0);\n"+
			"	}\n"+
			"\n"+
			"}\n",

		},"");		
	}
	
	public void testGenericsAndGroovyJava_GRE278_3() {
		this.runConformTest(new String[] {
			"Main.java",
			"public class Main {\n"+
			"  public static void main(String[]argv) {\n"+
			"    test.StructureBaseTest.main(argv);\n"+
			"  }\n"+
			"}\n",
			
			"p/Field.java",
			"package test;\n"+
			"public interface Field<T extends java.io.Serializable> extends Comparable<T> {\n"+
			"    public String getFieldTypeName();\n"+
			"    public String getName();\n"+
			"    public T getValue();\n"+
			"    public void setValue(T o);\n"+
			"}\n",
			
			"p/Structure.java",
			"package test;\n"+
			"import java.util.Map;\n"+
			"import java.nio.ByteBuffer;\n"+
			"public interface Structure extends Map<String, Field<?>> {\n"+
			"   public void reset();\n"+
			"	public void setup(ByteBuffer clientBuff);\n"+
			"}\n",
			
			"p/StructureBase.groovy",
			"package test;\n"+
			"\n"+
			"import java.nio.ByteBuffer;\n"+
			"\n"+
			"@SuppressWarnings(\"unchecked\")\n"+
			"public class StructureBase implements Structure {\n"+
			"\n"+
			"	protected final Structure str = null;\n"+
			"	\n"+
			"	StructureBase(Structure struct){\n"+
			"		this.str = struct;\n"+
			"	}\n"+
			"	\n"+
			"	public void clear() {\n"+
			"		str.clear()\n"+
			"	}\n"+
			"\n"+
			"	public boolean containsKey(Object arg0) {\n"+
			"		return str.containsKey(arg0);\n"+
			"	}\n"+
			"\n"+
			"	public boolean containsValue(Object arg0) {\n"+
			"		return str.containsValue(arg0);\n"+
			"	}\n"+
			"\n"+
			"	public Set<java.util.Map.Entry<String, Field<?>>> entrySet() {\n"+
			"		return Collections.unmodifiableSet(str.entrySet());\n"+
			"	}\n"+
			"\n"+
			"	public Field<?> get(Object arg0) {\n"+
			"		return str.get(arg0);\n"+
			"	}\n"+
			"\n"+
			"	public boolean isEmpty() {\n"+
			"		return str.isEmpty();\n"+
			"	}\n"+
			"\n"+
			"	public Set<String> keySet() {\n"+
			"		return Collections.unmodifiableSet(str.keySet());\n"+
			"	}\n"+
			"\n"+
			"	public Field<?> put(String arg0, Field<?> arg1) {\n"+
			"		return str.put(arg0, arg1);\n"+
			"	}\n"+
			"	\n"+
			"	public Object put(Object key, Object value) {\n"+
			"		return str.put(key, value)\n"+
			"	}\n"+
			"\n"+
			"	public void putAll(Map<? extends String, ? extends Field<?>> arg0) {\n"+
			"		str.putAll(arg0);\n"+
			"	}\n"+
			"\n"+
			"	public Field<?> remove(Object key) {\n"+
			"		return str.remove(key);\n"+
			"	}\n"+
			"\n"+
			"	public int size() {\n"+
			"		return str.size();\n"+
			"	}\n"+
			"\n"+
			"	public Collection<Field<?>> values() {\n"+
			"		return Collections.unmodifiableCollection(str.values());\n"+
			"	}\n"+
			"\n"+
			"	public void reset(){\n"+
			"		str.reset();\n"+
			"	}\n"+
			"\n"+
			"	public void setup(ByteBuffer buff) {\n"+
			"		str.setup(buff);\n"+
			"	}\n"+
			"\n"+
			"}\n"+
			"\n",
			
			"p/StructureBaseTest.groovy",
			"package test;\n"+
			"\n"+
			"public class StructureBaseTest {\n"+
			"\n"+
			"	public static void main(String[] args) {\n"+
			"		 Structure str = new StructureBase(new TestStructure());\n"+
			"		 str.put('test', new TestField());\n"+
			"			def content = str.get('test');\n"+
			"			if (!TestField.FIELD_NAME.equals(str.get('test').name)) {\n"+
			"				System.out.println(\"Failed\");\n"+
			"			} else {\n"+
			"				System.out.println(\"Success\");\n"+
			"			}\n"+
			"	}\n"+
			"}\n",
			
			"p/TestField.java",
			"package test;\n"+
			"\n"+
			"public class TestField implements Field<String> {\n"+
			"\n"+
			"	public static final String FIELD_NAME = \"Test\";\n"+
			"	private StringBuilder buffer = new StringBuilder();\n"+
			"	private String value = null;\n"+
			"	public String getFieldTypeName() {\n"+
			"		return String.class.getSimpleName();\n"+
			"	}\n"+
			"\n"+
			"	public String getName() {\n"+
			"		return FIELD_NAME;\n"+
			"	}\n"+
			"\n"+
			"	public String getValue() {\n"+
			"		if (null == value)\n"+
			"			value = buffer.toString();\n"+
			"		return value;\n"+
			"	}\n"+
			"\n"+
			"	public void setValue(String o) {\n"+
			"		value = o; \n"+
			"		buffer.replace(0, buffer.length(), o);\n"+
			"	}\n"+
			"\n"+
			"	public int compareTo(String arg0) {\n"+
			"		return getValue().compareTo(arg0);\n"+
			"	}\n"+
			"\n"+
			"}\n",
			
			"p/TestStructure.java",
			"package test;\n"+
			"import java.nio.ByteBuffer;\n"+
			"import java.util.HashMap;\n"+
			"import java.util.Map;\n"+
			"\n"+
			"\n"+
			"@SuppressWarnings(\"serial\")\n"+
			"public class TestStructure extends HashMap<String, Field<?>> implements Structure {\n"+
			"\n"+
			"	public void reset() {\n"+
			"		// TODO Auto-generated method stub\n"+
			"\n"+
			"	}\n"+
			"\n"+
			"	public void setup(ByteBuffer clientBuff) {\n"+
			"		// TODO Auto-generated method stub\n"+
			"\n"+	
			"	}\n"+
			"\n"+
			"	public Field<?> put(String arg0, Field<?> arg1) {\n"+
			"		return super.put(arg0, arg1);\n"+
			"}\n"+
			"\n"+
			"	public void putAll(Map<? extends String, ? extends Field<?>> arg0) {\n"+
			"		super.putAll(arg0);\n"+
			"	}\n"+
			"\n"+
			"}\n",

		},"Success");		
	}
	
	public void testGenericsAndGroovyJava_GRE278_4() {
		this.runConformTest(new String[] {
			"Main.groovy",
			"public class Main {\n"+
			"  public static void main(String[]argv) {\n"+
			"    def content = new StructureBase().get('test');\n"+
			"    print 'test';\n"+
			"  }\n"+
			"}\n",
			
			"MyMap.java",
			"public interface MyMap<A,B> {" +
			"   B get(Object key);\n" +
			" }\n",

			"Structure.java",
			"public interface Structure extends MyMap<String, Integer> {\n"+
			"}\n",
			
			"StructureBase.groovy",
			"public class StructureBase implements Structure {\n"+
			"	public Integer get(Object key) {\n"+
			"		return null;\n"+
			"	}\n"+
			"}\n",
			
		},"test");		
	}


	public void testConstructorsForEnumWrong_GRE285() {
		this.runNegativeTest(new String[] {
				"TestEnum.groovy",
				"enum TestEnum {\n"+
				"\n"+
				"VALUE1(1, 'foo'),\n"+
				"VALUE2(2)\n"+
				"\n"+
				"private final int _value\n"+
				"private final String _description\n"+
				"\n"+
				"private TestEnum(int value, String description = null) {\n"+
				"	_value = value\n"+
				"	_description = description\n"+
				"}\n"+
				"\n"+
				"String getDescription() { _description }\n"+
				"\n"+
				"int getValue() { _value }\n"+
				"}"
				},"");
	}

//	public void testCrashingOnBadCode_GRE290() {
//		this.runNegativeTest(new String[] {
//			"Moo.groovy",
//			"package com.omxgroup.scripting;\n"+
//			"\n"+
//			"public class Moo {\n"+
//			"public static def moo() { println this.class }\n"+
//			"}\n"
//		},
//		"----------\n" + 
//		"1. ERROR in Moo.groovy (at line 4)\n" + 
//		"	public static def moo() { println this.class }\n" + 
//		"	                                  ^\n" + 
//		"Groovy:class is declared in a dynamic context, but you tried to access it from a static context.\n" + 
//		"----------\n" + 
//		"2. ERROR in Moo.groovy (at line 4)\n" + 
//		"	public static def moo() { println this.class }\n" + 
//		"	                                  ^\n" + 
//		"Groovy:Non-static variable \'this\' cannot be referenced from the static method moo.\n" + 
//		"----------\n");
//	}
	
	public void testCrashingOnBadCode_GRE290_2() {
		this.runNegativeTest(new String[] {
			"Moo.groovy",
			"public class Moo {\n"+
			"\n"+
			"public Moo processMoo(final moo) {\n"+
			"final moo = processMoo(moo)\n"+
			"return moo\n"+
			"}\n"+
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Moo.groovy (at line 4)\n" + 
		"	final moo = processMoo(moo)\n" + 
		"	      ^\n" + 
		"Groovy:The current scope already contains a variable of the name moo\n" + 
		"----------\n"+
		"2. ERROR in Moo.groovy (at line 4)\n" + 
		"	final moo = processMoo(moo)\n" + 
		"	      ^\n" + 
		"Groovy:The current scope already contains a variable of the name moo\n" + 
		"----------\n"
		);
	}

	// 'this' by itself isn't an error
	public void testCrashingOnBadCode_GRE290_3() {
		this.runNegativeTest(new String[] {
			"Moo.groovy",
			"package com.omxgroup.scripting;\n"+
			"\n"+
			"public class Moo {\n"+
			"public static def moo() { println this }\n"+
			"}\n"
		},
		"");
	}

	public void testCrashingOnBadCode_GRE290_4() {
		this.runNegativeTest(new String[] {
			"p/X.groovy",
			"words: [].each { final item ->\n" + 
			"  break words\n"+
			"  }\n"
		},"----------\n" + 
		"1. ERROR in p\\X.groovy (at line 2)\n" + 
		"	break words\n" + 
		"	^\n" + 
		"Groovy:the break statement with named label is only allowed inside loops\n" + 
		"----------\n");		
	}
	
	public void testNPE_GRE291() {
		this.runNegativeTest(new String[] {
				"ContinueTestCase.groovy",
			"public class ContinueTestCase {\n" + 
			"\n" + 
			"	public ContinueTestCase() {\n" + 
			"		continue;\n" + 
			"	}\n" + 
			"}\n" },
			"----------\n" + 
			"1. ERROR in ContinueTestCase.groovy (at line 4)\n" + 
			"	continue;\n" + 
			"	^\n" + 
			"Groovy:the continue statement is only allowed inside loops\n" + 
			"----------\n");
	}

	public void testMissingContext_GRE308() {
		this.runNegativeTest(new String[] {
			"DibDabs.groovy",
			"	def run(n) {\n"+
			"\n"+		
			"		  OtherGroovy.iterate (3) {\n"+
			"		  print it*2\n"+
			"	  }  \n"+
			"//		  		NOT RECORDED AGAINST THIS FILE??\n"+
			"		  int i        "},
			"----------\n" + 
			"1. ERROR in DibDabs.groovy (at line 7)\n" + 
			"	int i        \n" + 
			"	            ^\n" + 
			"Groovy:expecting \'}\', found \'\' @ line 7, column 17.\n" + 
			"----------\n");
		}

    // FIXASC less than ideal underlining for error location
	public void testMissingContext_GRE308_2() {
		this.runNegativeTest(new String[] {
			"DibDabs.groovy",
			"	def run(n) {\n"+
			"\n"+		
			"		  OtherGroovy.iterate (3) {\n"+
			"		  print it*2\n"+
			"	  }  \n"+
			"//		  		NOT RECORDED AGAINST THIS FILE??\n"+
			"		  int i        \n"},
			"----------\n" + 
			"1. ERROR in DibDabs.groovy (at line 7)\n" + 
			"	int i        \n" + 
			"\n" + 
			"	             ^\n" + 
			"Groovy:expecting \'}\', found \'\' @ line 7, column 18.\n" + 
			"----------\n");
		}
	
	
	// a valid script, no '.' after session2
	public void testInvalidScripts_GRE323_1() {
		this.runNegativeTest(new String[] {
			"Two.groovy",
			"def moo(closure) {\n" + 
			"  closure();\n" + 
			"}\n" + 
			"\n" + 
			"moo {\n" + 
			"  final session2 = null\n" + 
		    "  \n" + 
			"  // Define scenarios\n" + 
			"  def secBoardRep = session2\n" + 
			"  def x\n" + 
			"}\n"
		},"");		
	}

	public void testInvalidScripts_GRE323_1b() {
		this.runConformTest(new String[] {
			"Two.groovy",
			"def moo(closure) {\n" + 
			"  closure();\n" + 
			"}\n" + 
			"\n" + 
			"moo {\n" + 
			"  final session2 = null\n" + 
		    "  \n" + 
			"  // Define scenarios\n" + 
			"  def secBoardRep = session2\n" + 
			"  def x\n" + 
			"}\n"
		},"");		
	}

	// '.' added, command line gives:
	//	One.groovy: 10: expecting '}', found 'x' @ line 10, column 7.
	//    def x
	//        ^
	public void testInvalidScripts_GRE323_2() {
		if (GroovyUtils.GROOVY_LEVEL<18) {
			this.runNegativeTest(new String[] {
				"One.groovy",
				"def moo(closure) {\n" + 
				"  closure();\n" + 
				"}\n" + 
				"\n" + 
				"moo {\n" + 
				"  final session2 = null\n" + 
			    "  \n" + 
				"  // Define scenarios\n" + 
				"  def secBoardRep = session2.\n" + 
				"  def x\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in One.groovy (at line 10)\n" + 
			"	def x\n" + 
			"	    ^\n" + 
			"Groovy:expecting \'}\', found \'x\' @ line 10, column 7.\n" + 
			"----------\n");
		} else {
			// command expression syntax now allows this but it looks weird as 
			this.runConformTest(new String[] {
					"One.groovy",
					"def moo(closure) {\n" + 
					"  closure();\n" + 
					"}\n" + 
					"\n" + 
					"moo {\n" + 
					"  final session2 = null\n" + 
				    "  \n" + 
					"  // Define scenarios\n" + 
					"  def secBoardRep = session2.\n" + 
					"  def x\n" + 
					"}\n"
				},
				"");	
		}
	}
	
	// removed surrounding method
	public void testInvalidScripts_GRE323_3() {
		this.runNegativeTest(new String[] {
			"Three.groovy",
			"def moo(closure) {\n" + 
			"  closure();\n" + 
			"}\n" + 
			"\n" + 
			"  final session2 = null\n" + 
		    "  \n" + 
			"  // Define scenarios\n" + 
			"  def secBoardRep = session2\n" + 
			"  def x\n"
		},"");		
	}

	public void testInvalidScripts_GRE323_3b() {
		this.runConformTest(new String[] {
			"Three.groovy",
			"def moo(closure) {\n" + 
			"  closure();\n" + 
			"}\n" + 
			"\n" + 
			"  final session2 = null\n" + 
		    "  \n" + 
			"  // Define scenarios\n" + 
			"  def secBoardRep = session2\n" + 
			"  def x\n"
		},"");		
	}
	
	// no assignment for session2
	public void testInvalidScripts_GRE323_4() {
		this.runNegativeTest(new String[] {
			"Four.groovy",
			"def moo(closure) {\n" + 
			"  closure();\n" + 
			"}\n" + 
			"\n" + 
			"moo {\n" + 
			"  final session2 = null\n" + 
		    "  \n" + 
			"  // Define scenarios\n" + 
			"  session2.\n" + 
			"  def x\n"+
			"}\n"
		},"");
	}

	public void testInvalidScripts_GRE323_4b() {
		this.runConformTest(new String[] {
			"Run.java",
			"public class Run {\n"+
			"  public static void main(String[]argv) {\n"+
			"   try {\n"+
			"    Four.main(null);\n"+
			"   } catch (Throwable t) {\n"+
			"    System.out.println(t.getMessage());\n"+
			"   }\n"+
			"}\n"+
			"}",
			"Four.groovy",
			"def moo(closure) {\n" + 
			"  closure();\n" + 
			"}\n" + 
			"\n" + 
			"moo {\n" + 
			"  final session2 = null\n" + 
		    "  \n" + 
			"  // Define scenarios\n" + 
			"  session2.\n" + 
			"  def x\n"+
			"}\n"
		},"No such property: x for class: Four");
// actual exception:
//		"[ERR]:groovy.lang.MissingPropertyException: No such property: x for class: Four\n"+
//		"	at org.codehaus.groovy.runtime.ScriptBytecodeAdapter.unwrap(ScriptBytecodeAdapter.java:49)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.PogoGetPropertySite.getProperty(PogoGetPropertySite.java:49)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callGroovyObjectGetProperty(AbstractCallSite.java:241)\n"+
//		"	at Four$_run_closure1.doCall(Four.groovy:9)\n"+
//		"	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"+
//		"	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"+
//		"	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"+
//		"	at java.lang.reflect.Method.invoke(Method.java:585)\n"+
//		"	at org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:86)\n"+
//		"	at groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:234)\n"+
//		"	at org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeMethod(ClosureMetaClass.java:272)\n"+
//		"	at groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:880)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.PogoMetaClassSite.callCurrent(PogoMetaClassSite.java:66)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCallCurrent(CallSiteArray.java:44)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:143)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:151)\n"+
//		"	at Four$_run_closure1.doCall(Four.groovy)\n"+
//		"	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"+
//		"	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"+
//		"	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"+
//		"	at java.lang.reflect.Method.invoke(Method.java:585)\n"+
//		"	at org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:86)\n"+
//		"	at groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:234)\n"+
//		"	at org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeMethod(ClosureMetaClass.java:272)\n"+
//		"	at groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:880)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.PogoMetaClassSite.call(PogoMetaClassSite.java:39)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCall(CallSiteArray.java:40)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:117)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:121)\n"+
//		"	at Four.moo(Four.groovy:2)\n"+
//		"	at Four$moo.callCurrent(Unknown Source)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCallCurrent(CallSiteArray.java:44)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:143)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:151)\n"+
//		"	at Four.run(Four.groovy:5)\n"+
//		"	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"+
//		"	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"+
//		"	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"+
//		"	at java.lang.reflect.Method.invoke(Method.java:585)\n"+
//		"	at org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:86)\n"+
//		"	at groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:234)\n"+
//		"	at groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:1049)\n"+
//		"	at groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:880)\n"+
//		"	at org.codehaus.groovy.runtime.InvokerHelper.invokePogoMethod(InvokerHelper.java:745)\n"+
//		"	at org.codehaus.groovy.runtime.InvokerHelper.invokeMethod(InvokerHelper.java:728)\n"+
//		"	at org.codehaus.groovy.runtime.InvokerHelper.runScript(InvokerHelper.java:383)\n"+
//		"	at org.codehaus.groovy.runtime.InvokerHelper$runScript.call(Unknown Source)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCall(CallSiteArray.java:40)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:117)\n"+
//		"	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:129)\n"+
//		"	at Four.main(Four.groovy)\n"+
//		"	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"+
//		"	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"+
//		"	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"+
//		"	at java.lang.reflect.Method.invoke(Method.java:585)\n"+
//		"	at \n"
	}
	
	public void testInvalidScripts_GRE323_5() {
		this.runNegativeTest(new String[] {
			"Five.groovy",
			"def moo(closure) {\n" + 
			"  closure();\n" + 
			"}\n" + 
			"\n" + 
			"moo {\n" + 
			"  final session2 = [\"def\": { println \"DEF\" }]\n" + 
		    "  \n" + 
		    "  final x = 1\n"+
			"  // Define scenarios\n" + 
			"  session2.\n" + 
			"  def x\n"+
			"}\n"
		},"");		
	}

	public void testInvalidScripts_GRE323_5b() {
		this.runConformTest(new String[] {
			"Five.groovy",
			"def moo(closure) {\n" + 
			"  closure();\n" + 
			"}\n" + 
			"\n" + 
			"moo {\n" + 
			"  final session2 = [\"def\": { println \"DEF\" }]\n" + 
		    "  \n" + 
		    "  final x = 1\n"+
			"  // Define scenarios\n" + 
			"  session2.\n" + 
			"  def x\n"+
			"}\n"
		},"DEF");		
	}
	
	public void testInvalidScripts_GRE323_6() {
		if (GroovyUtils.GROOVY_LEVEL<18) {
			this.runNegativeTest(new String[] {
				"Six.groovy",
				"def moo(closure) {\n" + 
				"  closure();\n" + 
				"}\n" + 
				"\n" + 
				"moo {\n" + 
				"  final session2 = [\"def\": { println \"DEF\" }]\n" + 
			    "  \n" + 
			    "  final x = 1\n"+
				"  // Define scenarios\n" + 
				"  final y = session2.def x\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Six.groovy (at line 10)\n" + 
			"	final y = session2.def x\n" + 
			"	                       ^\n" + 
			"Groovy:expecting \'}\', found \'x\' @ line 10, column 26.\n" + 
			"----------\n");		
		} else {
			this.runConformTest(new String[] {
					"Six.groovy",
					"def moo(closure) {\n" + 
					"  closure();\n" + 
					"}\n" + 
					"\n" + 
					"moo {\n" + 
					"  final session2 = [\"def\": { println \"DEF\" }]\n" + 
				    "  \n" + 
				    "  final x = 1\n"+
					"  // Define scenarios\n" + 
					"  final y = session2.def x\n" + 
					"}\n"
				},"DEF");		
		}
	}
	
	public void testBridgeMethods_GRE336() {
		this.runNegativeTest(new String[] {
			"my/example/EnumBooleanMap.java",
			"package my.example;\n"+
			"\n"+
			"import java.util.EnumMap;\n"+
			"\n"+
			"@SuppressWarnings(\"serial\")\n"+
			"public class EnumBooleanMap<E extends Enum<E>> extends EnumMap<E, Boolean> {\n"+
			"	\n"+
			"	public EnumBooleanMap(Class<E> keyType) {\n"+
			"		super(keyType);\n"+
			"	}\n"+
			"\n"+
			"	public EnumBooleanMap(EnumBooleanMap<E> m) {\n"+
			"		super(m);\n"+
			"	}\n"+
			"\n"+
			"	@Override\n"+
			"	public Boolean get(Object key) {\n"+
			"		Boolean value = super.get(key);\n"+
			"		return value != null ? value : false;\n"+
			"	}\n"+
			"}\n"
		},"");
	}

	public void testInnerTypeReferencing_GRE339() {
		this.runConformTest(new String[] {
				"Script.groovy",
				"class Script {\n"+
				"  public static void main(String[] argv) {\n"+
				"    print Outer.Inner.VAR\n"+
				"  }\n"+
				"}",
				"Outer.java",
				"public class Outer {\n"+
				"	  static class Inner {\n"+
				"	    static String VAR = \"value\";\n"+
				"	  }\n"+
				"	}\n"},
			"value");
	}
	
	// interface
	public void testInnerTypeReferencing_GRE339_2() {
		this.runConformTest(new String[] {
				"Script.groovy",
				"class Script {\n"+
				"  public static void main(String[] argv) {\n"+
				"    print Outer.Inner.VAR\n"+
				"  }\n"+
				"}",
				"Outer.java",
				"public interface Outer {\n"+
				"	  interface Inner {\n"+
				"	    static String VAR = \"value\";\n"+
				"	  }\n"+
				"	}\n"},
			"value");
	}
	
	// pure script
	public void testInnerTypeReferencing_GRE339_3() {
		this.runConformTest(new String[] {
				"script.groovy",
				"print Outer.Inner.VAR\n",
				"Outer.java",
				"public interface Outer {\n"+
				"	  interface Inner {\n"+
				"	    static String VAR = \"value\";\n"+
				"	  }\n"+
				"	}\n"},
			"value");
	}
	

	public void testStaticProperties_GRE364() {
		this.runConformTest(new String[] {
				"Foo.groovy",
				"public class Foo { static String fubar }\n",
				"Bar.java",
				"public class Bar {\n"+
				"	  String fubar = Foo.getFubar();\n"+
				"	}\n"},
			"");
	}

	public void testStaticProperties_GRE364_2() {
		this.runConformTest(new String[] {
				"Bar.java",
				"public class Bar {\n"+
				"	  String fubar = Foo.getFubar();\n"+
				"	}\n",
				"Foo.groovy",
				"public class Foo { static String fubar }\n"},
			"");
	}
	

	public void testTransientMethod_GRE370() {
		this.runConformTest(new String[] {
				"Foo.groovy",
				"public class Foo {\n"+
				"  public transient void foo() {}\n"+
				"}\n"},
			"");
	}
	
	public void testNotSeriousEnough_GRE396() {
		this.runNegativeTest(new String[]{
			
			"TrivialBugTest.groovy",
			"package org.sjb.sjblib.cmdline;\n"+
			"public class TrivialBugTest {\n"+
			"	void func2() {\n"+
			"		tb = new TrivialBug()\n"+
			"	}\n"+
			"}\n",
			"TrivialBug.groovy",
			"package org.sjb.sjblib.cmdline;\n"+
			"public class TrivialBug {\n"+
			"	void func() {\n"+
			"		return 5\n"+
			"	}\n"+
			"}\n",},
			"----------\n" + 
			"1. ERROR in TrivialBug.groovy (at line 4)\n" + 
			"	return 5\n" + 
			"	^^^^^^^^\n" + 
			"Groovy:Cannot use return statement with an expression on a method that returns void\n" + 
			"----------\n"
		);
	}
	
	public void testNotSeriousEnough_GRE396_2() {
		this.runNegativeTest(new String[]{
			"TrivialBug.groovy",
			"package org.sjb.sjblib.cmdline;\n"+
			"public class TrivialBug {\n"+
			"	void func() {\n"+
			"		return 5\n"+
			"	}\n"+
			"}\n",
			"TrivialBugTest.groovy",
			"package org.sjb.sjblib.cmdline;\n"+
			"public class TrivialBugTest {\n"+
			"	void func2() {\n"+
			"		tb = new TrivialBug()\n"+
			"	}\n"+
			"}\n",},
			"----------\n" + 
			"1. ERROR in TrivialBug.groovy (at line 4)\n" + 
			"	return 5\n" + 
			"	^^^^^^^^\n" + 
			"Groovy:Cannot use return statement with an expression on a method that returns void\n" + 
			"----------\n"
		);
	}
	
	public void testStarImports_GRE421() {
		this.runConformTest(new String[]{
				"Wibble.groovy",
				"import a.b.c.*;\n"+
				"class Wibble {\n"+
				"	 Process process = new Process()\n"+
				"  public static void main(String[] argv) { print new Wibble().process.class}\n"+
				"}\n",
				"a/b/c/Process.java",
				"package a.b.c;\n"+
				"public class Process {}\n"},
				"class a.b.c.Process");
	}
	

	// ---

	// The getter for 'description' implements the interface
	public void testImplementingAnInterfaceViaProperty() {
		this.runConformTest(new String[] {
			"a/b/c/C.groovy",
			"package a.b.c;\n" + 
			"import p.q.r.I;\n"+
			"public class C implements I {\n" + 
			"  String description;\n"+
			"  public static void main(String[] argv) {\n"+
			"    print \"success\"\n" + 
			"  }\n"+
			"}\n",
			
			"p/q/r/I.groovy",
			"package p.q.r;\n" + 
			"public interface I {\n" + 
			"  String getDescription();\n"+
			"}\n",
		},"success");		
	}
	
	// Referencing from a groovy to a java type where the reference is through a member, not the hierarchy
	public void testReferencingOtherTypesInSamePackage() {
		this.runConformTest(new String[] {
			"a/b/c/C.groovy",
			"package a.b.c;\n" +
			"public class C {\n" + 
			"  D description;\n"+
			"  public static void main(String[] argv) {\n"+
			"    print \"success\"\n" + 
			"  }\n"+
			"}\n",
			
			"a/b/c/D.java",
			"package a.b.c;\n" + 
			"public class D {\n" + 
			"  String getDescription() { return null;}\n"+
			"}\n",
		},"success");		
	}

	
	// Ensures that the Point2D.Double reference is resolved in the context of X and not Y (if Y is used then the import isn't found)
	public void testMemberTypeResolution() {
		this.runConformTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"import java.awt.geom.Point2D;\n"+
			"public class X {\n" + 
			"  public void foo() {\n"+
            "    Object o = new Point2D.Double(p.x(),p.y());\n"+
			"  }\n"+
			"  public static void main(String[] argv) {\n"+
			"    print \"success\"\n" + 
			"  }\n"+
			"}\n",
			"p/Y.groovy",
			"package p;\n" + 
			"public class Y {\n" + 
			"  public void foo() {\n"+
			"  }\n"+
			"  public static void main(String[] argv) {\n"+
			"    print \"success\"\n" + 
			"  }\n"+
			"}\n",
		},"success");		
	}

	public void testFailureWhilstAttemptingToReportError() {
		this.runNegativeTest(new String[] {
			"T.groovy",
			"public class T{\n"+
			"	def x () {\n"+
			"		this \"\"\n"+
			"	}\n"+
			"}\n"
		},"----------\n" + 
		"1. ERROR in T.groovy (at line 3)\n" + 
		"	this \"\"\n" + 
		"	     ^\n" + 
		"Groovy:Constructor call must be the first statement in a constructor. at line: 3 column: 8. File: T.groovy @ line 3, column 8.\n" + 
		"----------\n");
	}
	
	public void testExtendingInterface1() {
		this.runNegativeTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"public class X extends I {\n" + 
			"}\n",
			"p/I.groovy",
			"package p\n"+
			"interface I {}",
		},		
		"----------\n" + 
		"1. ERROR in p\\X.groovy (at line 2)\n" + 
		"	public class X extends I {\n" + 
		"	             ^\n" + 
		"Groovy:You are not allowed to extend the interface \'p.I\', use implements instead.\n" + 
		"----------\n");
	}
	

	public void testProtectedType() {
		this.runConformTest(new String[] {
			"p/Y.groovy",
			"package p;\n" + 
			"class Y {\n" + 
			"  public static void main(String[]argv) {\n"+
			"    new X().main(argv);\n"+
			"  }\n"+
			"}\n",
			"p/X.groovy",
			"package p;\n" + 
			"protected class X {\n" + 
			"  public static void main(String[]argv) {\n"+
			"    print \"success\"\n"+
			"  }\n"+
			"}\n",
		},		
		"success");
	}
	

	public void testEnums() {
		this.runConformTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"public enum X {\n" + 
			"}\n",
		},		
		"");
	}
/*
    public void testEnums2() {
		try {
		JDTResolver.recordInstances = true;
		this.runConformTest(new String[] {
    			"EE.groovy",
    			"enum EE {A,B,C;}\n",
    			"Foo.java",
    			"public class Foo<E extends Foo<E>> implements Comparable<E> {" +
    			"  public int compareTo(E b) { return 0;}\n"+
    			"}\n" +
    			"\n",
    			"Goo.java",
    			"public class Goo<X extends Goo<X>> extends Foo<X> {}\n",
    			"Bar.groovy",
    			"abstract class Bar extends Goo<Bar> {" +
    			"  int compareTo(Bar b) { return 0;}\n"+
    			"  EE getEnum() { return null; }\n"+
    			"}\n"},"");

		// Check on the state of Comparable		
		JDTResolver jr = (JDTResolver)JDTResolver.instances.get(0);
		JDTClassNode classnode = jr.getCachedNode("java.lang.Comparable<E>");
//		assertNotNull(classnode);
		// Should have one method
//		List methods = classnode.getMethods();
//		assertEquals(1,methods.size());
//		assertEquals("int compareTo(java.lang.Object)",((MethodNode)methods.get(0)).getTypeDescriptor());
		
//		classnode.lazyClassInit();
	} finally {
		JDTResolver.instances.clear();
		JDTResolver.recordInstances=false;
	}
//    	this.runConformTest(new String[]{
//    			"Foo.groovy",
//    			"class Foo<E extends Foo<E>> implements Comparable<E> {" +
//    			"  int compareTo(Object b) { return 0;}\n"+
//    			"}\n" +
//    			"\n",
//    			"Bar.groovy",
//    			"abstract class Bar extends Foo<Bar> {" +
//    			"  int compareTo(Bar b) { return 0;}\n"+
//    			"}\n"},"");
    }
//    public void testJDTClassNode_633() {
//		try {
//			JDTResolver.recordInstances = true;
//			this.runConformTest(new String[] {
//				"p/Run.groovy",
//				"package p;\n"+
//				"import static p.q.r.Colour.*;\n"+
//				"import p.q.r.Colour2;\n"+
//				"public class Run {\n" + 
//				"  public static void main(String[] argv) {\n"+
//				"    System.out.print(Red);\n"+
//				"    System.out.print(Green);\n"+
//				"    System.out.print(Blue);\n"+
//				"   Colour2 c2 = new Colour2();\n"+
//				"   int i = c2.compareTo('abc');\n"+
//				"  }\n"+
//				"}\n",
//	
//				"p/q/r/Colour.java",
//				"package p.q.r;\n" + 
//				"enum Colour { Red,Green,Blue; }\n",
//	
//				"p/q/r/Colour2.java",
//				"package p.q.r;\n" + 
//				"public class Colour2 implements Comparable<String> { \n"+
//				"  public int compareTo(String s) { return 0; } \n"+
//					"}\n",
//				},"RedGreenBlue");		 
//			
//			// Check on the state of Comparable		
//			JDTClassNode classnode = ((JDTResolver)JDTResolver.instances.get(0)).getCachedNode("java.lang.Comparable<T>");
//			assertNotNull(classnode);
//			// Should have one method
//			List methods = classnode.getMethods();
//			assertEquals(1,methods.size());
//			assertEquals("int compareTo(java.lang.Object)",((MethodNode)methods.get(0)).getTypeDescriptor());
//		} finally {
//			JDTResolver.instances.clear();
//			JDTResolver.recordInstances=false;
//		}
//	}*/
	public void testNonTerminalMissingImport() {
		this.runNegativeTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"import a.b.c.D;\n"+
			"public class X {\n" + 
			"  public static void main(String[]argv) {\n"+
			"    print \"success\"\n"+
			"  }\n"+
			"}\n",
		},		
		"----------\n" + 
		"1. ERROR in p\\X.groovy (at line 2)\n" + 
		"	import a.b.c.D;\n" + 
		"	             ^\n" + 
		"Groovy:unable to resolve class a.b.c.D\n" + 
		"----------\n");
	}

	public void testTypeLevelAnnotations01() {
		this.runConformTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"@Anno\n"+
			"public class X {\n" +
			"  public static void main(String[]argv) {\n"+
			"    print \"success\"\n"+
			"  }\n"+
			"}\n",

			"p/Anno.java",
			"package p;\n"+
			"import java.lang.annotation.*;\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n"+
			"@interface Anno {}\n",
		},		
		"success");

		String expectedOutput = "public @Anno class X {";
		checkGCUDeclaration("X.groovy",expectedOutput);
		
		expectedOutput = 
			"@p.Anno\n" + 
			"public class p.X implements groovy.lang.GroovyObject {\n";
		checkDisassemblyFor("p/X.class", expectedOutput);
	}
	
	public void testMethodLevelAnnotations() {
		this.runConformTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"public class X {\n" +
			"  @Anno\n"+
			"  public static void main(String[]argv) {\n"+
			"    print \"success\"\n"+
			"  }\n"+
			"}\n",

			"p/Anno.java",
			"package p;\n"+
			"import java.lang.annotation.*;\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n"+
			"@interface Anno {}\n",
		},		
		"success");

		String expectedOutput = "public static @Anno void main(public String... argv) {";
		checkGCUDeclaration("X.groovy",expectedOutput);
				
		expectedOutput = 
			//"  // Method descriptor #46 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 3, Locals: 2\n" + 
			"  @p.Anno\n" + 
			"  public static void main(java.lang.String... argv);\n";
		checkDisassemblyFor("p/X.class", expectedOutput);
	}

	public void testFieldLevelAnnotations01() throws Exception {
		runConformTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"public class X {\n" +
			"  @Anno\n"+
			"  String s\n"+
			"  public static void main(String[]argv) {\n"+
			"    print \"success\"\n"+
			"  }\n"+
			"}\n",

			"p/Anno.java",
			"package p;\n"+
			"import java.lang.annotation.*;\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n"+
			"@interface Anno {}\n",

		},
		"success");
		
		String expectedOutput = "private @Anno String s;";
		checkGCUDeclaration("X.groovy",expectedOutput);
		
		expectedOutput = 
			//"  // Field descriptor #11 Ljava/lang/String;\n" + 
			"  @p.Anno\n" + 
			"  private java.lang.String s;\n";
		checkDisassemblyFor("p/X.class", expectedOutput);
	}	
	
	public void testFieldLevelAnnotations_classretention() throws Exception {
		runConformTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"public class X {\n" +
			"  @Anno\n"+
			"  String s\n"+
			"  public static void main(String[]argv) {\n"+
			"    print \"success\"\n"+
			"  }\n"+
			"}\n",

			"p/Anno.java",
			"package p;\n"+
			"import java.lang.annotation.*;\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n"+
			"@interface Anno {}\n",

		},
		"success");
		
		String expectedOutput = 
			//"  // Field descriptor #11 Ljava/lang/String;\n" + 
			"  @p.Anno\n" + 
			"  private java.lang.String s;\n";
		checkDisassemblyFor("p/X.class", expectedOutput);
	}	

	public void testFieldLevelAnnotations_sourceretention() throws Exception {
		runConformTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"public class X {\n" +
			"  @Anno\n"+
			"  String s\n"+
			"  public static void main(String[]argv) {\n"+
			"    print \"success\"\n"+
			"  }\n"+
			"}\n",

			"p/Anno.java",
			"package p;\n"+
			"import java.lang.annotation.*;\n"+
			"@Retention(RetentionPolicy.SOURCE)\n"+
			"@interface Anno {}\n",

		},
		"success");
		
		String expectedOutput = 
			//"  // Field descriptor #9"+  descriptor number varies across compilers (1.6/1.7)
			"Ljava/lang/String;\n" + 
			"  private java.lang.String s;\n";
		checkDisassemblyFor("p/X.class", expectedOutput);
	}	
	
	// Default retention is class
	public void testFieldLevelAnnotations_defaultretention() throws Exception {
		runConformTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"public class X {\n" +
			"  @Anno\n"+
			"  String s\n"+
			"  public static void main(String[]argv) {\n"+
			"    print \"success\"\n"+
			"  }\n"+
			"}\n",

			"p/Anno.java",
			"package p;\n"+
			"@interface Anno {}\n",

		},
		"success");
		
		String expectedOutput = 
//			"  // Field descriptor #9 "+ // descriptor number varies across compiler versions
			"Ljava/lang/String;\n" + 
			"  private java.lang.String s;\n";
		checkDisassemblyFor("p/X.class", expectedOutput);
	}	
	
	public void testFieldLevelAnnotations_delegate() throws Exception {
		runConformTest(new String[] {
			"Bar.groovy",
			"class Bar {\n"+
			" public void m() {\n"+
			"Object o = new Other().me;\n"+
			"}}",
				
			"Other.groovy",
			"public class Other {\n" +
			"  public @Anno Date me\n"+
			"}\n",

			"Anno.java",
			"import java.lang.annotation.*;\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n"+
			"@interface Anno {}\n",

		},
		"");

		checkGCUDeclaration("Other.groovy",
				"public class Other {\n" + 
				"  public @Anno Date me;\n" + 
				"  public Other() {\n" + 
				"  }\n" + 
				"}\n");
	}	
	
	public void testHalfFinishedGenericsProgram() {
		this.runNegativeTest(new String[] {
			"Demo.groovy",
			"public class Demo {\n"+
			"\n"+
			"List myList;\n"+
			"\n"+
			"			def funkyMethod(Map map) {\n"+
			"				print \"Groovy!\"\n"+
			"		}\n"+
			"	}\n"+
			"\n"+
			"class MyMap<K,V> extends Map {\n"+
			"\n"+
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in Demo.groovy (at line 3)\n" + 
		"	List myList;\n" + 
		"	^^^^\n" + 
		"List is a raw type. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"2. WARNING in Demo.groovy (at line 5)\n" + 
		"	def funkyMethod(Map map) {\n" + 
		"	                ^^^\n" + 
		"Map is a raw type. References to generic type Map<K,V> should be parameterized\n" + 
		"----------\n" + 
		"3. ERROR in Demo.groovy (at line 10)\n" + 
		"	class MyMap<K,V> extends Map {\n" + 
		"	      ^^^^^\n" + 
		"Groovy:You are not allowed to extend the interface \'java.util.Map\', use implements instead.\n" + 
		"----------\n" + 
		"4. WARNING in Demo.groovy (at line 10)\n" + 
		"	class MyMap<K,V> extends Map {\n" + 
		"	                         ^^^\n" + 
		"Map is a raw type. References to generic type Map<K,V> should be parameterized\n" + 
		"----------\n");
	}
	
	public void testHalfFinishedGenericsProgramWithCorrectSuppression() {
		this.runNegativeTest(new String[] {
			"Demo.groovy",
			"public class Demo {\n"+
			"\n"+
			"@SuppressWarnings(\"rawtypes\")\n"+ // should cause no warnings
			"List myList;\n"+
			"}\n"
		},"");
	}

	public void testHalfFinishedGenericsProgramWithCorrectSuppressionAtTheTypeLevel() {
		this.runNegativeTest(new String[] {
			"Demo.groovy",
			"@SuppressWarnings(\"rawtypes\")\n"+ // should cause no warnings
			"public class Demo {\n"+
			"\n"+
			"List myList;\n"+
			"}\n"
		},"");
	}


	public void testHalfFinishedGenericsProgramWithUnnecessarySuppression() {
		this.runNegativeTest(new String[] {
			"Demo.groovy",
			"public class Demo {\n"+
			"\n"+
			"@SuppressWarnings(\"unchecked\")\n"+ // unnecessary suppression
			"List<String> myList;\n"+
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in Demo.groovy (at line 3)\n" + 
		"	@SuppressWarnings(\"unchecked\")\n" + 
		"	                  ^^^^^^^^^^^\n" + 
		"Unnecessary @SuppressWarnings(\"unchecked\")\n" + 
		"----------\n");
	}

	public void testHalfFinishedGenericsProgramWithSuppressionValueSpeltWrong() {
		this.runNegativeTest(new String[] {
			"Demo.groovy",
			"public class Demo {\n"+
			"\n"+
			"@SuppressWarnings(\"unchecked2\")\n"+ // spelt wrong
			"List<String> myList;\n"+
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in Demo.groovy (at line 3)\n" + 
		"	@SuppressWarnings(\"unchecked2\")\n" + 
		"	                  ^^^^^^^^^^^^\n" + 
		"Unsupported @SuppressWarnings(\"unchecked2\")\n" + 
		"----------\n");
	}
	
	public void testHalfFinishedGenericsProgramWithMultipleSuppressionValues() {
		this.runNegativeTest(new String[] {
			"Demo.groovy",
			"public class Demo {\n"+
			"\n"+
			"@SuppressWarnings([\"rawtypes\",\"cast\"])\n"+
			"List myList;\n"+
			"}\n"
		},"");
	}

	public void testHalfFinishedGenericsProgramWithMultipleSuppressionValuesWithOneSpeltWrong() {
		this.runNegativeTest(new String[] {
			"Demo.groovy",
			"public class Demo {\n"+
			"\n"+
			"@SuppressWarnings([\"rawtypes\",\"cast2\"])\n"+
			"List myList;\n"+
			"}\n"
		},"----------\n" + 
		"1. WARNING in Demo.groovy (at line 3)\n" + 
		"	@SuppressWarnings([\"rawtypes\",\"cast2\"])\n" + 
		"	                              ^^^^^^^\n" + 
		"Unsupported @SuppressWarnings(\"cast2\")\n" + 
		"----------\n");
	}


	public void testTypeClash() {
		this.runNegativeTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"public class X {\n" +
			"  public static void main(String[]argv) {\n"+
			"    print \"success\"\n"+
			"  }\n"+
			"}\n",

			"p/X.java",
			"package p;\n"+
			"public class X {}\n"
		},
		"----------\n" + 
		"1. ERROR in p\\X.groovy (at line 2)\n" + 
		"	public class X {\n" + 
		"	             ^\n" + 
		"The type X is already defined\n" + 
		"----------\n");
	}
	
	public void testCallStaticMethodFromGtoJ() {
		this.runConformTest(new String[] {
			"p/Foo.groovy",
			"package p;\n" + 
			"public class Foo {\n" +
			"  public static void main(String[]argv) {\n"+
			"    X.run()\n"+
			"  }\n"+
			"}\n",

			"p/X.java",
			"package p;\n"+
			"public class X {\n"+
			"  public static void run() {\n"+
			"    System.out.println(\"success\");\n"+
			"  }\n"+
			"}\n"
		},
		"success");
	}

	public void testCallStaticMethodFromJtoG() {
		this.runConformTest(new String[] {
			"p/Foo.java",
			"package p;\n" + 
			"public class Foo {\n" +
			"  public static void main(String[]argv) {\n"+
			"    X.run();\n"+
			"  }\n"+
			"}\n",

			"p/X.groovy",
			"package p;\n"+
			"public class X {\n"+
			"  public static void run() {\n"+
			"    System.out.println(\"success\");\n"+
			"  }\n"+
			"}\n"
		},
		"success");
	}
	

	public void testNotMakingInterfacesImplementGroovyObject() {
		this.runConformTest(new String[] {
			"p/X.java",
			"package p;\n"+
			"public class X implements I {\n"+
			"  public static void main(String[] argv) {\n"+
			"    System.out.println(\"success\");\n"+
			"  }\n"+
			"}\n",
			"p/I.groovy",
			"package p;\n" + 
			"public interface I {\n" +
			"}\n",
		},
		"success");
	}

	public void testConstructorLevelAnnotations01() {
		this.runConformTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"public class X {\n" +
			"  @Anno\n"+
			"  X(String s) {}\n"+
			"  public static void main(String[]argv) {\n"+
			"    print \"success\"\n"+
			"  }\n"+
			"}\n",

			"p/Anno.java",
			"package p;\n"+
			"import java.lang.annotation.*;\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n"+
			"@interface Anno {}\n",
		},		
		"success");
		
		String expectedOutput = "public @Anno X(public String s) {";
		checkGCUDeclaration("X.groovy",expectedOutput);
		
		expectedOutput = 
			//"  // Method descriptor #18 (Ljava/lang/String;)V\n" + 
			(GroovyUtils.GROOVY_LEVEL<18?
			"  // Stack: 3, Locals: 3\n":
			"  // Stack: 2, Locals: 4\n")+
			"  @p.Anno\n" + 
			"  public X(java.lang.String s);\n";
		checkDisassemblyFor("p/X.class", expectedOutput);		
	}
	

	public void testAnnotations04_defaultParamMethods() {
		this.runConformTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"public class X {\n" +
			"  @Anno\n"+
			"  public void foo(String s = \"abc\") {}\n"+
			"  public static void main(String[]argv) {\n"+
			"    print \"success\"\n"+
			"  }\n"+
			"}\n",

			"p/Anno.java",
			"package p;\n"+
			"import java.lang.annotation.*;\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n"+
			"@interface Anno {}\n",
		},		
		"success");
		String expectedOutput = "public @Anno void foo() {";
		checkGCUDeclaration("X.groovy",expectedOutput);
		
		expectedOutput = "public @Anno void foo(public String s) {";
		checkGCUDeclaration("X.groovy",expectedOutput);
	}

	public void testTypeLevelAnnotations_SingleMember() {
		this.runConformTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(Target.class)\n"+
				"public class X {\n" +
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Target.java",
				"package p;\n"+
				"class Target { }",
			},		
			"success");
		String expectedOutput = "public @Anno(Target.class) class X";
		checkGCUDeclaration("X.groovy",expectedOutput);
	}
	
	// All types in groovy with TYPE specified for Target and obeyed
	public void testAnnotationsTargetType() {
		this.runConformTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(p.Foo.class)\n"+
				"public class X {\n" +
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.groovy",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@Target([ElementType.TYPE])\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Foo.groovy",
				"package p;\n"+
				"class Foo { }",
			},		
			"success"
			);
	}
	// All groovy but annotation can only be put on METHOD - that is violated by class X
	public void testAnnotationsTargetType02() {
		this.runNegativeTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(p.Foo.class)\n"+
				"public class X {\n" +
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.groovy",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@Target([ElementType.METHOD])\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Foo.groovy",
				"package p;\n"+
				"class Foo { }",
			},		
			"----------\n" + 
			"1. ERROR in p\\X.groovy (at line 2)\n" + 
			"	@Anno(p.Foo.class)\n" + 
			"	 ^^^^\n" + 
			"Groovy:Annotation @p.Anno is not allowed on element TYPE\n" + 
			"----------\n"
			);
	}
	
	// All groovy but annotation can only be put on FIELD - that is violated by class X
	public void testAnnotationsTargetType03() {
		this.runNegativeTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(p.Foo.class)\n"+
				"public class X {\n" +
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.groovy",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@Target([ElementType.FIELD])\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Foo.groovy",
				"package p;\n"+
				"class Foo { }",
			},		
			"----------\n" + 
			"1. ERROR in p\\X.groovy (at line 2)\n" + 
			"	@Anno(p.Foo.class)\n" + 
			"	 ^^^^\n" + 
			"Groovy:Annotation @p.Anno is not allowed on element TYPE\n" + 
			"----------\n"
			);
	}

	// All groovy but annotation can only be put on FIELD or METHOD - that is violated by class X
	public void testAnnotationsTargetType04() {
		this.runNegativeTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(p.Foo.class)\n"+
				"public class X {\n" +
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.groovy",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@Target([ElementType.FIELD,ElementType.METHOD])\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Foo.groovy",
				"package p;\n"+
				"class Foo { }",
			},		
			"----------\n" + 
			"1. ERROR in p\\X.groovy (at line 2)\n" + 
			"	@Anno(p.Foo.class)\n" + 
			"	 ^^^^\n" + 
			"Groovy:Annotation @p.Anno is not allowed on element TYPE\n" + 
			"----------\n"
			);
	}

	// Two types in groovy, one in java with TYPE specified for Target and obeyed
	public void testAnnotationsTargetType05() {
		this.runConformTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(p.Foo.class)\n"+
				"public class X {\n" +
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.groovy",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@Target([ElementType.TYPE])\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Foo.java",
				"package p;\n"+
				"class Foo { }",
			},		
			"success"
			);
	}

	// 2 groovy, 1 java but annotation can only be put on METHOD - that is violated by class X
	public void testAnnotationsTargetType06() {
		this.runNegativeTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(p.Foo.class)\n"+
				"public class X {\n" +
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.groovy",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@Target([ElementType.METHOD])\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Foo.java",
				"package p;\n"+
				"class Foo { }",
			},		
			"----------\n" + 
			"1. ERROR in p\\X.groovy (at line 2)\n" + 
			"	@Anno(p.Foo.class)\n" + 
			"	 ^^^^\n" + 
			"Groovy:Annotation @p.Anno is not allowed on element TYPE\n" + 
			"----------\n"
			);
	}
	
	// 2 groovy, 1 java but annotation can only be put on FIELD - that is violated by class X
	public void testAnnotationsTargetType07() {
		this.runNegativeTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(p.Foo.class)\n"+
				"public class X {\n" +
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.groovy",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@Target([ElementType.FIELD])\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Foo.groovy",
				"package p;\n"+
				"class Foo { }",
			},		
			"----------\n" + 
			"1. ERROR in p\\X.groovy (at line 2)\n" + 
			"	@Anno(p.Foo.class)\n" + 
			"	 ^^^^\n" + 
			"Groovy:Annotation @p.Anno is not allowed on element TYPE\n" + 
			"----------\n"
			);
	}

	// 2 groovy, 1 java but annotation can only be put on FIELD or METHOD - that is violated by class X
	public void testAnnotationsTargetType08() {
		this.runNegativeTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(p.Foo.class)\n"+
				"public class X {\n" +
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.groovy",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@Target([ElementType.FIELD,ElementType.METHOD])\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Foo.java",
				"package p;\n"+
				"class Foo { }",
			},		
			"----------\n" + 
			"1. ERROR in p\\X.groovy (at line 2)\n" + 
			"	@Anno(p.Foo.class)\n" + 
			"	 ^^^^\n" + 
			"Groovy:Annotation @p.Anno is not allowed on element TYPE\n" + 
			"----------\n"
			);
	}
	
	// 1 groovy, 2 java with TYPE specified for Target and obeyed
	public void testAnnotationsTargetType09() {
		this.runConformTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(p.Foo.class)\n"+
				"public class X {\n" +
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@Target({ElementType.TYPE})\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Foo.java",
				"package p;\n"+
				"class Foo { }",
			},		
			"success"
			);
	}

	// 1 groovy, 2 java but annotation can only be put on METHOD - that is violated by class X
	public void testAnnotationsTargetType10() {
		this.runNegativeTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(p.Foo.class)\n"+
				"public class X {\n" +
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@Target({ElementType.METHOD})\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Foo.java",
				"package p;\n"+
				"class Foo { }",
			},		
			"----------\n" + 
			"1. ERROR in p\\X.groovy (at line 2)\n" + 
			"	@Anno(p.Foo.class)\n" + 
			"	 ^^^^\n" + 
			"Groovy:Annotation @p.Anno is not allowed on element TYPE\n" + 
			"----------\n"
			);
	}
	
	// 1 groovy, 2 java but annotation can only be put on FIELD - that is violated by class X
	public void testAnnotationsTargetType11() {
		this.runNegativeTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(p.Foo.class)\n"+
				"public class X {\n" +
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@Target({ElementType.FIELD})\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Foo.groovy",
				"package p;\n"+
				"class Foo { }",
			},		
			"----------\n" + 
			"1. ERROR in p\\X.groovy (at line 2)\n" + 
			"	@Anno(p.Foo.class)\n" + 
			"	 ^^^^\n" + 
			"Groovy:Annotation @p.Anno is not allowed on element TYPE\n" + 
			"----------\n"
			);
	}

	// 1 groovy, 2 java but annotation can only be put on FIELD or METHOD - that is violated by class X
	public void testAnnotationsTargetType12() {
		this.runNegativeTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(p.Foo.class)\n"+
				"public class X {\n" +
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@Target({ElementType.FIELD,ElementType.METHOD})\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Foo.java",
				"package p;\n"+
				"class Foo { }",
			},		
			"----------\n" + 
			"1. ERROR in p\\X.groovy (at line 2)\n" + 
			"	@Anno(p.Foo.class)\n" + 
			"	 ^^^^\n" + 
			"Groovy:Annotation @p.Anno is not allowed on element TYPE\n" + 
			"----------\n"
			);
	}

	// FIXASC groovy bug?  Why didn't it complain that String doesn't meet the bound - at the moment letting JDT complain...
	public void testWildcards01() {
		this.runNegativeTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(String.class)\n"+
				"public class X {\n" +
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.groovy",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@interface Anno { Class<? extends Number> value(); }\n",
			},		
			"----------\n" + 
			"1. ERROR in p\\X.groovy (at line 2)\n" + 
			"	@Anno(String.class)\n" + 
			"	      ^^^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Class<String> to Class<? extends Number>\n" + 
			"----------\n");		
	}
	
	public void testWildcards02() {
		this.runNegativeTest(new String[] {
				"p/X.java",
				"package p;\n" + 
				"@Anno(String.class)\n"+
				"public class X {\n" +
				"  public void foo(String s) {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    System.out.println(\"success\");\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@interface Anno { Class<? extends Number> value(); }\n",
			},		
			"----------\n" + 
			"1. ERROR in p\\X.java (at line 2)\n" + 
			"	@Anno(String.class)\n" + 
			"	      ^^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Class<String> to Class<? extends Number>\n" + 
			"----------\n");		
	}

	public void testWildcards03() {
		this.runNegativeTest(new String[] {
				"p/X.java",
				"package p;\n" + 
				"@Anno(String.class)\n"+
				"public class X {\n" +
				"  public void foo(String s) {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    System.out.println(\"success\");\n"+
				"  }\n"+
				"}\n",

				"p/Anno.groovy",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@interface Anno { Class<? extends Number> value(); }\n",
			},		
			"----------\n" + 
			"1. ERROR in p\\X.java (at line 2)\n" + 
			"	@Anno(String.class)\n" + 
			"	      ^^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Class<String> to Class<? extends Number>\n" + 
			"----------\n");		
	}
	
	public void testWildcards04() {
		this.runNegativeTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(String.class)\n"+
				"public class X {\n" +
				"  public void foo(String s) {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    System.out.println(\"success\");\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@interface Anno { Class<? extends Number> value(); }\n",
			},		
			"----------\n" + 
			"1. ERROR in p\\X.groovy (at line 2)\n" + 
			"	@Anno(String.class)\n" + 
			"	      ^^^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Class<String> to Class<? extends Number>\n" + 
			"----------\n");		
	}

	
	public void testWildcards05() {
		this.runConformTest(new String[] {
				"p/X.java",
				"package p;\n" + 
				"@Anno(Integer.class)\n"+
				"public class X {\n" +
				"  public void foo(String s) {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    System.out.println(\"success\");\n"+
				"  }\n"+
				"}\n",

				"p/Anno.groovy",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@interface Anno { Class<? extends Number> value(); }\n",
			},		
			"success");		
	}
	
	public void testWildcards06() {
		this.runConformTest(new String[] {
				"p/X.java",
				"package p;\n" + 
				"@Anno(Number.class)\n"+
				"public class X {\n" +
				"  public void foo(String s) {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    System.out.println(\"success\");\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@interface Anno { Class<? super Integer> value(); }\n",
			},"success");		
	}
	
	// bounds violation: String does not meet '? super Integer'
	public void testWildcards07() {
		this.runNegativeTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(String.class)\n"+
				"public class X {\n" +
				"  public static void main(String[]argv) {\n"+
				"    System.out.println(\"success\");\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@interface Anno { Class<? super Integer> value(); }\n",
			},"----------\n" + 
			"1. ERROR in p\\X.groovy (at line 2)\n" + 
			"	@Anno(String.class)\n" + 
			"	      ^^^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Class<String> to Class<? super Integer>\n" + 
			"----------\n");		
	}

	// double upper bounds
	public void testWildcards08() {
		this.runNegativeTest(new String[] {
				"p/X.java",
				"package p;\n" + 
				"public class X {\n" +
				"  public static void main(String[]argv) {\n"+
				"    Object o = new Wibble<Integer>().run();\n"+
				"    System.out.println(\"success\");\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"class Wibble<T extends Number & I> { Class<T> run() { return null;} }\n",
				
				"p/I.java",
				"package p;\n"+
				"interface I {}\n",
			},"----------\n" + 
			"1. ERROR in p\\X.java (at line 4)\n" + 
			"	Object o = new Wibble<Integer>().run();\n" + 
			"	                      ^^^^^^^\n" + 
			"Bound mismatch: The type Integer is not a valid substitute for the bounded parameter <T extends Number & I> of the type Wibble<T>\n" + 
			"----------\n");		
	}
	
	// double upper bounds
	public void testWildcards09() {
		this.runNegativeTest(new String[] {
				"p/X.java",
				"package p;\n" + 
				"public class X {\n" +
				"  public static void main(String[]argv) {\n"+
				"    Object o = new Wibble<Integer>().run();\n"+
				"    System.out.println(\"success\");\n"+
				"  }\n"+
				"}\n",

				"p/Anno.groovy",
				"package p;\n"+
				"class Wibble<T extends Number & I> { Class<T> run() { return null;} }\n",
				
				"p/I.java",
				"package p;\n"+
				"interface I {}\n",
			},"----------\n" + 
			"1. ERROR in p\\X.java (at line 4)\n" + 
			"	Object o = new Wibble<Integer>().run();\n" + 
			"	                      ^^^^^^^\n" + 
			"Bound mismatch: The type Integer is not a valid substitute for the bounded parameter <T extends Number & I> of the type Wibble<T>\n" + 
			"----------\n");		
	}
	
	// FIXASC groovy bug? Why does groovy not care about bounds violation?
//	public void testWildcards10() {
//		this.runNegativeTest(new String[] {
//				"p/X.groovy",
//				"package p;\n" + 
//				"public class X {\n" +
//				"  public static void main(String[]argv) {\n"+
//				"    Object o = new Wibble<Integer>().run();\n"+
//				"    System.out.println(\"success\");\n"+
//				"  }\n"+
//				"}\n",
//
//				"p/Anno.groovy",
//				"package p;\n"+
//				"class Wibble<T extends Number & I> { Class<T> run() { return null;} }\n",
//				
//				"p/I.java",
//				"package p;\n"+
//				"interface I {}\n",
//			},"----------\n" + 
//			"1. ERROR in p\\X.java (at line 4)\n" + 
//			"	Object o = new Wibble<Integer>().run();\n" + 
//			"	                      ^^^^^^^\n" + 
//			"Bound mismatch: The type Integer is not a valid substitute for the bounded parameter <T extends Number & I> of the type Wibble<T>\n" + 
//			"----------\n");		
//	}
	
	public void testWildcards11() {
		this.runNegativeTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"public class X extends Wibble<Foo> {\n" +
				"  public static void main(String[]argv) {\n"+
				"    System.out.println(\"success\");\n"+
				"  }\n"+
				"}\n",

				"p/Anno.groovy",
				"package p;\n"+
				"class Wibble<T extends Number & I> { Class<T> run() { return null;} }\n",
				
				"p/I.java",
				"package p;\n"+
				"interface I {}\n",

				"p/Foo.java",
				"package p;\n"+
				"class Foo implements I {}\n",
			},"----------\n" + 
			"1. ERROR in p\\X.groovy (at line 2)\n" + 
			"	public class X extends Wibble<Foo> {\n" + 
			"	                       ^\n" + 
			"Groovy:The type Foo is not a valid substitute for the bounded parameter <T extends java.lang.Number & p.I>\n" + 
			"----------\n");		
	}

	// FIXASC groovy bug? why doesn't it complain - the type parameter doesn't meet the secondary upper bound
//	public void testWildcards12() {
//		this.runNegativeTest(new String[] {
//				"p/X.groovy",
//				"package p;\n" + 
//				"public class X extends Wibble<Integer> {\n" +
//				"  public static void main(String[]argv) {\n"+
//				"    System.out.println(\"success\");\n"+
//				"  }\n"+
//				"}\n",
//
//				"p/Anno.groovy",
//				"package p;\n"+
//				"class Wibble<T extends Number & I> { Class<T> run() { return null;} }\n",
//				
//				"p/I.java",
//				"package p;\n"+
//				"interface I {}\n",
//
//				"p/Foo.java",
//				"package p;\n"+
//				"class Foo implements I {}\n",
//			},"----------\n" + 
//			"1. ERROR in p\\X.groovy (at line 2)\n" + 
//			"	public class X extends Wibble<Foo> {\n" + 
//			"	               ^^\n" + 
//			"Groovy:The type Foo is not a valid substitute for the bounded parameter <T extends java.lang.Number & p.I>\n" + 
//			"----------\n");		
//	}
	
	public void testTypeLevelAnnotations_SingleMember02() {
		this.runConformTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"@Anno(p.Target.class)\n"+
				"public class X {\n" +
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Target.java",
				"package p;\n"+
				"class Target { }",
			},		
			"success");
		String expectedOutput = "public @Anno(p.Target.class) class X";
		checkGCUDeclaration("X.groovy",expectedOutput);
	}
	
	public void testMethodLevelAnnotations_SingleMember() {
		this.runConformTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"public class X {\n" +
				"  @Anno(Target.class)\n"+
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Target.java",
				"package p;\n"+
				"class Target { }",
			},		
			"success");
		String expectedOutput = "public @Anno(Target.class) void foo(public String s) {";
		checkGCUDeclaration("X.groovy",expectedOutput); 
	}
	
	// FIXASC1) flesh out annotation value types for transformation in JDTAnnotationNode - might as well complete it
	public void testMethodLevelAnnotations_SingleMember02() {
		this.runConformTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"public class X {\n" +
				"  @Anno(p.Target.class)\n"+
				"  public void foo(String s = \"abc\") {}\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Target.java",
				"package p;\n"+
				"class Target { }",
			},		
			"success");
		String expectedOutput = "public @Anno(p.Target.class) void foo(public String s) {";
		checkGCUDeclaration("X.groovy",expectedOutput); 
	}
	
	public void testFieldLevelAnnotations_SingleMember() {
		this.runConformTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"public class X {\n" +
				"  @Anno(Target.class)\n"+
				"  public int foo = 5\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Target.java",
				"package p;\n"+
				"class Target { }",
			},		
			"success");
		String expectedOutput = "public @Anno(Target.class) int foo";
		checkGCUDeclaration("X.groovy",expectedOutput); 
	}
	
	public void testAnnotations10_singleMemberAnnotationField() {
		this.runConformTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"public class X {\n" +
				"  @Anno(p.Target.class)\n"+
				"  public int foo = 5\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@interface Anno { Class<?> value(); }\n",

				"p/Target.java",
				"package p;\n"+
				"class Target { }",
			},		
			"success");
		String expectedOutput = "public @Anno(p.Target.class) int foo";
		checkGCUDeclaration("X.groovy",expectedOutput); 
	}
	
	public void testAnnotations11_singleMemberAnnotationFailure() {
		this.runNegativeTest(new String[] {
				"p/X.groovy",
				"package p;\n" + 
				"public class X {\n" +
				"  @Anno(IDontExist.class)\n"+
				"  public int foo = 5\n"+
				"  public static void main(String[]argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",

				"p/Anno.java",
				"package p;\n"+
				"import java.lang.annotation.*;\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@interface Anno { Class<?> value(); }\n",
			},		
			"----------\n" +
			"1. ERROR in p\\X.groovy (at line 3)\n" +
			"	@Anno(IDontExist.class)\n" +
			"	      ^\n" +
			(GroovyUtils.isGroovy16()?
			"Groovy:unable to find class for enum\n":
			"Groovy:unable to find class 'IDontExist.class' for annotation attribute constant\n") +
			"----------\n" +
			"2. ERROR in p\\X.groovy (at line 3)\n" +
			"	@Anno(IDontExist.class)\n" +
			"	      ^\n" +
			(GroovyUtils.GROOVY_LEVEL<18?
			"Groovy:Only classes can be used for attribute 'value' in @p.Anno\n":
			"Groovy:Only classes and closures can be used for attribute 'value' in @p.Anno\n"
		    )+
			"----------\n");
	}
	
	public void testExtendingInterface2() {
		this.runNegativeTest(new String[] {
			"p/X.groovy",
			"package p;\n" + 
			"public class X extends List<String> {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    print \"success\"\n" + 
			"  }\n"+
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in p\\X.groovy (at line 2)\n" + 
		"	public class X extends List<String> {\n" + 
		"	             ^\n" + 
		"Groovy:You are not allowed to extend the interface \'java.util.List\', use implements instead.\n" + 
		"----------\n");		
	}

	// WMTW: the type declaration building code creates the correct representation of A and adds the default constructor
	public void testExtendingGroovyWithJava1() {
		this.runConformTest(new String[] {
			// p.B
			"p/B.java",
			"package p;\n"+
			"public class B extends A {\n"+
			"  public static void main(String[] argv) {\n"+
			"    new B();\n"+
			"    System.out.println(\"success\");\n"+
			"  }\n"+
			"}\n",
			// p.A
			"p/A.groovy",
			"package p;\n" + 
			"public class A {\n" + 
			"}\n",
		},"success");		
	}
	
	// WMTW: a JDT resolver is plugged into groovy so it can see Java types
	// details: 
	// 1. needed the lookupenvironment to flow down to the groovyparser (through initializeParser) as groovy will need it
	// for resolution of JDT types
	// 2. needed to subclass ResolveVisitor - trying just to override resolve(ClassNode) right now
	// 3. needed to build JDTClassNode and needed it to initialize the superclass field
	public void testExtendingJavaWithGroovy1() {
		this.runConformTest(new String[] {
			// p.B
			"p/B.groovy",
			"package p;\n" + 
			"public class B extends A {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    new B();\n"+
			"    System.out.println(\"success\");\n"+
			"  }\n"+
			"}\n",
			// p.A
			"p/A.java",
			"package p;\n"+
			"public class A {\n"+
			"}\n",
		},"success");		
	}
	
	public void testCallingGenericConstructors() {
		this.runConformTest(new String[] {
			// p.B
			"p/B.groovy",
			"package p;\n" + 
			"public class B extends A {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    new A(35);\n"+
			"    System.out.println(\"success\");\n"+
			"  }\n"+
			"}\n",
			// p.A
			"p/A.java",
			"package p;\n"+
			"public class A {\n"+
			"  public <T> A(T t) {}\n"+
			"}\n",
		},"success");		
	}

	public void testExtendingJavaWithGroovyAndThenJava() {
		this.runConformTest(new String[] {
			// p.C
			"p/C.java",
			"package p;\n" + 
			"public class C extends B {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    new C();\n"+
			"    System.out.println(\"success\");\n"+
			"  }\n"+
			"}\n",				
			// p.B
			"p/B.groovy",
			"package p;\n" + 
			"public class B extends A {\n" + 
			"}\n",
			// p.A
			"p/A.java",
			"package p;\n"+
			"public class A {\n"+
			"}\n",
		},"success");		
	}
	
	// Groovy is allowed to have a public class like this in a file with a different name
	public void testPublicClassInWrongFile() {
		this.runConformTest(new String[] {
			"pkg/One.groovy",
			"package pkg;\n" + 
			"public class One {" +
			"  public static void main(String[]argv) { print \"success\";}\n" +
			"}\n"+
			"public class Two {" +
			"  public static void main(String[]argv) { print \"success\";}\n" +
			"}\n", 
		},"success");		
	}

	// WMTW: having a callback registered with groovy class generation that tracks which class file is created for which module node
	// details:
	// the groovy compilationunit provides a way to ask for the generated classes but it doesnt give a way to tell why they arose
	// (which sourceunit caused them to come into existence).  I am using the callback mechanism to track this information, but I worry
	// that we are causing groovy to perhaps do things too many times.  It also feels a little wierd that driving any single file through
	// to CLASSGEN drives them all through.  It isn't necessarily a problem, but it conflicts with the model of dealing with one file at
	// a time...
	public void testBuildingTwoGroovyFiles() {
		this.runConformTest(new String[] {
			// pkg.One
			"pkg/One.groovy",
			"package pkg;\n" + 
			"class One {" +
			"  public static void main(String[]argv) { print \"success\";}\n" +
			"}\n", 
			// pkg.Two
			"pkg/Two.groovy",
			"package pkg;\n" + 
			"class Two {}\n"
		},"success");		
	}
	
	public void testExtendingGroovyInterfaceWithJava() {
		this.runConformTest(new String[] {
				// pkg.C
				"pkg/C.java",
				"package pkg;\n" + 
				"public class C extends groovy.lang.GroovyObjectSupport implements I {" +
				"  public static void main(String[]argv) {\n"+
				"    I i = new C();\n"+
				"    System.out.println( \"success\");" +
				"  }\n" +
				"}\n", 
				// pkg.I
				"pkg/I.groovy",
				"package pkg;\n" + 
				"interface I {}\n"
			},"success");		
	}

	public void testExtendingJavaInterfaceWithGroovy() {
		this.runConformTest(new String[] {
				// pkg.C
				"pkg/C.groovy",
				"package pkg;\n" + 
				"public class C implements I {" +
				"  public static void main(String[]argv) {\n"+
				"    I i = new C();\n"+
				"    System.out.println( \"success\");" +
				"  }\n" +
				"}\n", 
				// pkg.I
				"pkg/I.java",
				"package pkg;\n" + 
				"interface I {}\n"
			},"success");		
	}

	// WMTM: the fix for the previous code that tracks why classes are generated
	public void testExtendingJavaWithGroovyAndThenJavaAndThenGroovy() {
		this.runConformTest(new String[] {
			// p.D
			"p/D.groovy",
			"package p;\n" + 
			"class D extends C {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    new C();\n"+
			"    print \"success\"\n"+
			"  }\n"+
			"}\n",				
			// p.C
			"p/C.java",
			"package p;\n" + 
			"public class C extends B {}\n", 
			// p.B
			"p/B.groovy",
			"package p;\n" + 
			"public class B extends A {}\n",
			// p.A
			"p/A.java",
			"package p;\n"+
			"public class A {}\n"
		},"success");		
	}

	// GroovyBug: this surfaced the problem that the generics declarations are checked before resolution is complete - 
	// had to change CompilationUnit so that resolve and checkGenerics are different stages in the SEMANTIC_ANALYSIS phase
	// otherwise it depends on whether the super type is resolved before the subtype has its generic decl checked
	public void testGroovyGenerics() {
		// Example of how you would only do a test at a particular level:
//		if (this.complianceLevel<ClassFileConstants.JDK1_5) {
//			// normally at <1.5 we would do a negative test, checking for error
//			fail();
//		} else {
			this.runConformTest(new String[] {
					// p.B
					"p/B.groovy",
					"package p;\n" + 
					"public class B extends A<String> {\n" + 
					"  public static void main(String[] argv) {\n"+
					"    new B();\n"+
					"    System.out.println( \"success\");\n"+
					"  }\n"+
					"}\n",				
					// p.A
					"p/A.groovy",
					"package p;\n" + 
					"public class A<T> {}\n",
				},"success");	
//		}
	}

	// WMTW: JDT ClassNode builds a correct groovy representation of the A type
	public void testExtendingGenerics_GroovyExtendsJava() {
		this.runConformTest(new String[] {
				// p.B
				"p/B.groovy",
				"package p;\n" + 
				"public class B extends A<String> {\n" + 
				"  public static void main(String[] argv) {\n"+
				"    new B();\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				
				// p.A
				"p/A.java",
				"package p;\n" + 
				"public class A<T> {public void set(T t) { }}\n", 
			},"success");	
	}

	// test when the upper bound is not just 'Object'
	// WMTW: notice I and Impl are classes and not interfaces, because right now only the superclass stuff is set up correctly for nodes.
	// In order for no error to occur we have to override getUnresolvedSuperClass() in our JDTClassNode so that the code in 
	// GenericsVisitor.checkGenericsUsage() correctly determines Impl isDerivedFrom I
	// the rule seems to be coming out that there is no redirection from JDTClassNode, they are absolute
	public void testExtendingGenerics_GroovyExtendsJava2() {
		this.runConformTest(new String[] {
				// p.B
				"p/B.groovy",
				"package p;\n" + 
				"public class B extends A<Impl> {\n" + 
				"  public static void main(String[] argv) {\n"+
				"    new B();\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				
				// p.I
				"p/I.java","package p; class I {}",
				// p.Impl
				"p/Impl.java","package p; class Impl extends I {}",
				// p.A
				"p/A.java",
				"package p;\n" + 
				"public class A<T extends I> {}\n", 
			},"success");	
	}
	
	// Test: the declaration of B violates the 'T extends I' specification of A
	// SEE GRE430
//	public void testExtendingGenerics_GroovyExtendsJava3_ERROR() {
//		this.runNegativeTest(new String[] {
//				// p.B
//				"p/B.groovy",
//				"package p;\n" + 
//				"public class B extends A<String> {\n" + 
//				"  public static void main(String[] argv) {\n"+
//				"    System.out.println( \"success\");\n"+
//				"  }\n"+
//				"}\n",				
//				
//				// p.I
//				"p/I.java","package p; interface I {}",
//				
//				// p.Impl
//				"p/Impl.java","package p; class Impl implements I {}",
//				
//				// p.A
//				"p/A.java",
//				"package p;\n" + 
//				"public class A<T extends I> {}\n", 
//			},
//			"----------\n" + 
//			"1. ERROR in p\\B.groovy (at line 2)\n" + 
//			"	public class B extends A<String> {\n" + 
//			"	                       ^\n" + 
//			"Groovy:The type String is not a valid substitute for the bounded parameter <T extends p.I>\n" + 
//			"----------\n" 
//		);			
//	}
	
	// see comments in worklog on 8-Jun-09
	// note this also tests that qualified references are converted correctly (generics info intact)
	public void testExtendingGenerics_GroovyExtendsJava4() {
		this.runConformTest(new String[] {
				"p/B.groovy",
				"package p;\n" + 
				"public class B extends java.util.ArrayList<String> {\n" + 
				"  public static void main(String[] argv) {\n"+
				"    B b = new B();\n"+
				"    b.add(\"abc\");\n"+
				"    print(b.get(0));\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"  void print(String msg) { print msg; }\n"+
				"}\n",				
			},"abcsuccess");	
	}
	
	public void testExtendingGenerics_GroovyExtendsJava5() {
		this.runConformTest(new String[] {
				"p/B.groovy",
				"package p;\n" + 
				"public class B extends java.util.ArrayList<String> {\n" + 
				"  public static void main(String[] argv) {\n"+
				"    new B();\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				
			},"success");	
	}

	public void testExtendingGenerics_GroovyExtendsJava5a() {
		this.runConformTest(new String[] {
				"p/B.groovy",
				"package p;\n" + 
				"public class B extends ArrayList<String> {\n" + 
				"  public static void main(String[] argv) {\n"+
				"    new B();\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				
			},"success");	
	}

	public void testExtendingGenerics_GroovyExtendsJava6() {
		this.runConformTest(new String[] {
				"p/B.groovy",
				"package p;\n" + 
				"public class B extends A<String> {\n" + 
				"  public static void main(String[] argv) {\n"+
				"    new B();\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/A.java",
				"package p;\n" + 
				"public class A<T> {public void set(T t) { }}\n", 
			},"success");	
	}

	public void testExtendingGenerics_GroovyExtendsJava7() {
		this.runConformTest(new String[] {
				"p/B.groovy",
				"package p;\n" + 
				"public class B extends q.A<String> {\n" + 
				"  public static void main(String[] argv) {\n"+
				"    new B().set(\"abc\");\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"q/A.java",
				"package q;\n" + 
				"public class A<T> {public void set(T t) { }}\n", 
			},"success");	
	}

	// arrays
	public void testExtendingGenerics_GroovyExtendsJava8() {
		this.runConformTest(new String[] {
				// p.B
				"p/B.groovy",
				"package p;\n" + 
				"public class B extends A<int[]> {\n" + 
				"  public static void main(String[] argv) {\n"+
				"    new B().foo([1,2,3]);\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				
				"p/A.java",
				"package p;\n" + 
				"public class A<T extends Object> {\n"+
				"  public void foo(T t) {}\n"+
				"}\n", 
			},"success");	
	}
	
	public void testExtendingGenerics_GroovyExtendsJava9() {
		this.runConformTest(new String[] {
				// p.B
				"p/B.groovy",
				"package p;\n" + 
				"public class B extends C {\n" + 
				"  public static void main(String[] argv) {\n"+
				"    new B().foo([1,2,3]);\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",			
				
				"p/C.java",
				"package p;\n"+
				"public class C extends A<int[]> {}\n",
				
				"p/A.java",
				"package p;\n" + 
				"public class A<T extends Object> {\n"+
				"  public void foo(T t) {}\n"+
				"}\n", 
			},"success");	
	}
	
	public void testExtendingGenerics_GroovyExtendsJava10() {
		this.runConformTest(new String[] {
				"p/B.groovy",
				"package p;\n" + 
				"public class B extends C<String> {\n" + 
				"  public static void main(String[] argv) {\n"+
				"    new B().foo([1,2,3],\"hello\");\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",			
				
				"p/C.java",
				"package p;\n"+
				"public class C<Q> extends A<int[],Q> {}\n",
				
				"p/A.java",
				"package p;\n" + 
				"public class A<T extends Object,R> {\n"+
				"  public void foo(T t, R r) {}\n"+
				"}\n", 
			},"success");	
	}
	
	// WMTW: GroovyCompilationUnit builds a correct representation of the groovy type A
	public void testExtendingGenerics_JavaExtendsGroovy() {
		this.runConformTest(new String[] {
				// p.B
				"p/B.java",
				"package p;\n" + 
				"public class B extends A<String> {\n" + 
				"  public static void main(String[] argv) {\n"+
				"    new B();\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",
				// p.A
				"p/A.groovy",
				"package p;\n" + 
				"public class A<T> {}\n", 
			},"success");
	}
	
	// WMTW: JDTClassNode correctly initializes interfaces based on binding interfaces
	// It needs the interface set for Impl to be defined correctly so that groovy can determine Impl extends I
	// test when the upper bound is not just 'Object'
	public void testExtendingGenerics_GroovyExtendsJava3() {
		this.runConformTest(new String[] {
				// p.B
				"p/B.groovy",
				"package p;\n" + 
				"public class B extends A<Impl> {\n" + 
				"  public static void main(String[] argv) {\n"+
				"    new B();\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				
				// p.I
				"p/I.groovy","package p; interface I {}",
				// p.Impl
				"p/Impl.groovy","package p; class Impl implements I {}",
				// p.A
				"p/A.java",
				"package p;\n" + 
				"public class A<T extends I> {}\n", 
			},"success");	
	}
	
	// TODO create more variations around mixing types up (including generics bounds)
	// variation of above - the interface type is a java file and not a groovy file
	public void testExtendingGenerics_GroovyExtendsJava3a() {
		this.runConformTest(new String[] {
				// p.B
				"p/B.groovy",
				"package p;\n" + 
				"public class B extends A<Impl> {\n" + 
				"  public static void main(String[] argv) {\n"+
				"    new B();\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				
				// p.I
				"p/I.java","package p; interface I {}",
				// p.Impl
				"p/Impl.java","package p; class Impl implements I {}",
				// p.A
				"p/A.java",
				"package p;\n" + 
				"public class A<T extends I> {}\n", 
			},"success");	
	}
	
	public void testImplementingInterface1() {
		this.runNegativeTest(new String[] {
				"p/C.java",
				"package p;\n" + 
				"public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",
				
				"p/I.groovy",
				"package p;\n"+
				"public interface I {\n" + 
				"  void m();\n"+
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in p\\C.java (at line 2)\n" + 
			"	public class C extends groovy.lang.GroovyObjectSupport implements I {\n" + 
			"	             ^\n" + 
			"The type C must implement the inherited abstract method I.m()\n" + 
			"----------\n");			
	}

	public void testImplementingInterface2() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" + 
				"public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
				"  public void m() {}\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.groovy",
				"package p;\n"+
				"public interface I {\n" + 
				"  void m();\n"+
				"}\n",
			},"success");
	}

	
	public void testImplementingInterface3() {
		this.runNegativeTest(new String[] {
				"p/C.java",
				"package p;\n" + 
				"public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
				"  void m() {}\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.groovy",
				"package p;\n"+
				"public interface I {\n" + 
				"  void m();\n"+
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in p\\C.java (at line 3)\n" + 
			"	void m() {}\n" + 
			"	     ^^^\n" + 
			"Cannot reduce the visibility of the inherited method from I\n" + 
			"----------\n");
	}
	
	public void testImplementingInterface4() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" + 
				"public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
				"  public String m() { return \"\";}\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.groovy",
				"package p;\n"+
				"public interface I {\n" + 
				"  String m();\n"+
				"}\n",
			},"success");
	}
	
	// WMTW: Groovy compilation unit scope adds the extra default import for java.util so List can be seen
	public void testImplementingInterface_JavaExtendingGroovyAndImplementingMethod() {
		this.runNegativeTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"import java.util.List;\n"+
				"public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
				"  public String m() { return \"\";}\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.groovy",
				"package p;\n"+
				"public interface I {\n" + 
				"  List m();\n"+
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in p\\C.java (at line 4)\n" + 
			"	public String m() { return \"\";}\n" + 
			"	       ^^^^^^\n" + 
			"The return type is incompatible with I.m()\n" + 
			"----------\n"+ 
			// this verifies the position report for the error against the return value of the method
			"----------\n" + 
			"1. WARNING in p\\I.groovy (at line 3)\n" + 
			"	List m();\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n");
	}

	public void testFieldPositioning01() {
		this.runNegativeTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"public class C {\n"+
				"  List aList;\n"+
				"}\n",				
			},
			"----------\n" + 
			"1. WARNING in p\\C.groovy (at line 3)\n" + 
			"	List aList;\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n");
	}
	
	// FIXASC poor positional error for invalid field name - this test needs sorting out
//	public void testFieldPositioning02() {
//		this.runNegativeTest(new String[] {
//				"p/C.groovy",
//				"package p;\n" +
//				"public class C {\n"+
//				"  List<String> class;\n"+
//				"}\n",				
//			},
//			"----------\n");
//	}
	
	public void testImplementingInterface_JavaExtendingGroovyAndImplementingMethod_ArrayReferenceReturnType() {
		this.runNegativeTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"import java.util.List;\n"+
				"public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
				"  public String m() { return \"\";}\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.groovy",
				"package p;\n"+
				"public interface I {\n" + 
				"  List[] m();\n"+
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in p\\C.java (at line 4)\n" + 
			"	public String m() { return \"\";}\n" + 
			"	       ^^^^^^\n" + 
			"The return type is incompatible with I.m()\n" + 
			"----------\n"+ 
			// this verifies the position report for the error against the return value of the method
			"----------\n" + 
			"1. WARNING in p\\I.groovy (at line 3)\n" + 
			"	List[] m();\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n");
	}
	

	public void testImplementingInterface_JavaExtendingGroovyAndImplementingMethod_QualifiedArrayReferenceReturnType() {
		this.runNegativeTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"import java.util.List;\n"+
				"public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
				"  public String m() { return \"\";}\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.groovy",
				"package p;\n"+
				"public interface I {\n" + 
				"  java.util.List[] m();\n"+
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in p\\C.java (at line 4)\n" + 
			"	public String m() { return \"\";}\n" + 
			"	       ^^^^^^\n" + 
			"The return type is incompatible with I.m()\n" + 
			"----------\n"+ 
			// this verifies the position report for the error against the return value of the method
			"----------\n" + 
			"1. WARNING in p\\I.groovy (at line 3)\n" + 
			"	java.util.List[] m();\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n");
	}
	
	// FIXASC not yet sure on my position with letting JDT report warnings like generics usage - seems a nice 'bonus' for groovy tooling, hmmm

	
	public void testImplementingInterface_JavaExtendingGroovyAndImplementingMethod_ParamPosition() {
		this.runNegativeTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"import java.util.List;\n"+
				"public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
				"  public void m(String s) { }\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.groovy",
				"package p;\n"+
				"public interface I {\n" + 
				"  void m(List l);\n"+
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in p\\C.java (at line 3)\n" + 
			"	public class C extends groovy.lang.GroovyObjectSupport implements I {\n" + 
			"	             ^\n" + 
			"The type C must implement the inherited abstract method I.m(List)\n" + 
			"----------\n" + 
			// this verifies the position report for the error against the method parameter
			"----------\n" + 
			"1. WARNING in p\\I.groovy (at line 3)\n" + 
			"	void m(List l);\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n");
	}

	public void testImplementingInterface_JavaExtendingGroovyAndImplementingMethod_QualifiedParamPosition() {
		this.runNegativeTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"import java.util.List;\n"+
				"public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
				"  public void m(String s) { }\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.groovy",
				"package p;\n"+
				"public interface I {\n" + 
				"  void m(java.util.List l);\n"+
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in p\\C.java (at line 3)\n" + 
			"	public class C extends groovy.lang.GroovyObjectSupport implements I {\n" + 
			"	             ^\n" + 
			"The type C must implement the inherited abstract method I.m(List)\n" + 
			"----------\n" + 
			// this verifies the position report for the error against the method parameter+ 
			"----------\n" + 
			"1. WARNING in p\\I.groovy (at line 3)\n" + 
			"	void m(java.util.List l);\n" + 
			"	       ^^^^^^^^^^^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n");
	}


	public void testImplementingInterface_JavaExtendingGroovyGenericType() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"import java.util.List;\n"+
				"public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
				"  public List m() { return null;}\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.groovy",
				"package p;\n"+
				"public interface I {\n" + 
				"  List m();\n"+
				"}\n",
			},"success");
	}
	
	public void testImplementingInterface_JavaGenericsIncorrectlyExtendingGroovyGenerics() {
		this.runNegativeTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"import java.util.List;\n"+
				"public class C extends groovy.lang.GroovyObjectSupport implements I<String> {\n"+
				"  public List<String> m() { return null;}\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.groovy",
				"package p;\n"+
				"public interface I<T extends Number> {\n" + 
				"  List<T> m();\n"+
				"}\n",
			},		
			"----------\n" + 
			"1. ERROR in p\\C.java (at line 3)\n" + 
			"	public class C extends groovy.lang.GroovyObjectSupport implements I<String> {\n" + 
			"	                                                                    ^^^^^^\n" + 
			"Bound mismatch: The type String is not a valid substitute for the bounded parameter <T extends Number> of the type I<T>\n" + 
			"----------\n");
	}

	public void testImplementingInterface_GroovyGenericsIncorrectlyExtendingJavaGenerics() {
		this.runNegativeTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"public class C implements I<String> {\n"+
				"  public List<String> m() { return null;}\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.groovy",
				"package p;\n"+
				"import java.util.List;\n"+
				"public interface I<T extends Number> {\n" + 
				"  List<T> m();\n"+
				"}\n",
			},		
			"----------\n" + 
			"1. ERROR in p\\C.groovy (at line 2)\n" + 
			"	public class C implements I<String> {\n" + 
			"	                          ^\n" + 
			"Groovy:The type String is not a valid substitute for the bounded parameter <T extends java.lang.Number>\n" + 
			"----------\n");
	}
	

	public void testImplementingInterface_MethodWithParameters_GextendsJ() {
		this.runConformTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"public class C implements I<Integer> {\n"+
				"  public void m(String s) { }\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.java",
				"package p;\n"+
				"public interface I<T extends Number> {\n" + 
				"  void m(String s);\n"+
				"}\n",
			},
			"success");
	}
	
	// Test that the alias is recognized when referenced as superclass
	// WMTW: the code Scope.getShortNameFor()
	public void testImportAliasingGoober() {
		this.runConformTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"import java.util.HashMap as Goober;\n"+
				"public class C extends Goober {\n"+
				"  public static void main(String[] argv) {\n"+
				"    print 'q.A.run'\n"+
				"  }\n"+
				"}\n",				
		},
		"q.A.run");		
	}
	
	public void testImportAliasing() {
		this.runConformTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"import q.A as AA;\n"+
				"import r.A as AB;\n"+
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    callitOne(new AA());\n"+
				"    callitTwo(new AB());\n"+
				"  }\n"+
				"  public static void callitOne(AA a) { a.run();}\n"+
				"  public static void callitTwo(AB a) { a.run();}\n"+
				"}\n",				

				"q/A.java",
				"package q;\n"+
				"public class A {\n" + 
				"  public static void run() { System.out.print(\"q.A.run \");}\n"+
				"}\n",
				
				"r/A.java",
				"package r;\n"+
				"public class A {\n" + 
				"  public static void run() { System.out.print(\"r.A.run\");}\n"+
				"}\n",
			},
			"q.A.run r.A.run");		
	}
	
	public void testImportAliasingAndOldReference() {
		this.runNegativeTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"import q.A as AA;\n"+
				"import r.A as AB;\n"+
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    callitOne(new AA());\n"+
				"  }\n"+
				"  public static void callitOne(A a) { }\n"+ // no A imported!
				"}\n",				

				"q/A.java",
				"package q;\n"+
				"public class A {\n" + 
				"  public static void run() { System.out.print(\"q.A.run \");}\n"+
				"}\n",
				
				"r/A.java",
				"package r;\n"+
				"public class A {\n" + 
				"  public static void run() { System.out.print(\"r.A.run\");}\n"+
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in p\\C.groovy (at line 8)\n" + 
			"	public static void callitOne(A a) { }\n" + 
			"	                             ^\n" + 
			"Groovy:unable to resolve class A \n" + 
			"----------\n");		
	}
	
	public void testImportInnerInner01() {
		this.runConformTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"public class C {\n"+
				"  private static Wibble.Inner.Inner2 wibbleInner = new G();\n"+
				"  public static void main(String[] argv) {\n"+
				"    wibbleInner.run();\n"+
				"  }\n"+
				"}\n"+
				"class G extends Wibble.Inner.Inner2  {}",

				"p/Wibble.java",
				"package p;\n"+
				"public class Wibble {\n" + 
				"  public static class Inner {\n"+
				"    public static class Inner2 {\n"+
				"      public static void run() { System.out.print(\"p.Wibble.Inner.Inner2.run \");}\n"+
				"    }\n"+
				"  }\n"+				
				"}\n",
			},
			"p.Wibble.Inner.Inner2.run");		
	}

	public void testImportInnerClass01_JavaCase() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"import x.y.z.Wibble.Inner;\n"+
				"\n"+
				"public class C {\n"+
				"  private static Inner wibbleInner = new Inner();\n"+
				"  public static void main(String[] argv) {\n"+
				"    wibbleInner.run();\n"+
				"  }\n"+
				"}\n",				

				"x/y/z/Wibble.java",
				"package x.y.z;\n"+
				"public class Wibble {\n" + 
				"  public static class Inner {\n"+
				"    public static void run() { System.out.print(\"q.A.run \");}\n"+
				"  }\n"+				
				"}\n",
			},
			"q.A.run");		
	}
	
	// FIXASC need to look at all other kinds of import - statics/double nested static classes/etc

	public void testImportInnerClass01_GroovyCase() {
		this.runConformTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"import x.y.z.Wibble.Inner\n"+
				"\n"+
				"public class C {\n"+
				"  private static Inner wibbleInner = new Inner();\n"+
				"  public static void main(String[] argv) {\n"+
				"    wibbleInner.run();\n"+
				"  }\n"+
				"}\n",				

				"x/y/z/Wibble.java",
				"package x.y.z;\n"+
				"public class Wibble {\n" + 
				"  public static class Inner {\n"+
				"    public static void run() { System.out.print(\"q.A.run \");}\n"+
				"  }\n"+				
				"}\n",
			},
			"q.A.run");		
	}
	
	public void testImportInnerClass() {
		this.runConformTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"import x.y.z.Wibble.Inner /*as WibbleInner*/;\n"+
				"public class C {\n"+
				"  private static Inner wibbleInner = new Inner();\n"+
				"  public static void main(String[] argv) {\n"+
				"    wibbleInner.run();\n"+
				"  }\n"+
				//"  public static void callitOne(WibbleInner a) { a.run();}\n"+
				"}\n",				

				"x/y/z/Wibble.java",
				"package x.y.z;\n"+
				"public class Wibble {\n" + 
				"  public static class Inner {\n"+
				"    public void run() { System.out.print(\"run\");}\n"+
				"  }\n"+				
				"}\n",
			},
			"run");		
	}
		
	public void testImportAliasingInnerClass() {
		this.runConformTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"import x.y.z.Wibble.Inner as WibbleInner;\n"+
				"public class C {\n"+
				"  private static WibbleInner wibbleInner = new WibbleInner();\n"+
				"  public static void main(String[] argv) {\n"+
				"   wibbleInner.run();\n"+
				"  }\n"+
				"}\n",				

				"x/y/z/Wibble.java",
				"package x.y.z;\n"+
				"public class Wibble {\n" + 
				"  public static class Inner {\n"+
				"    public void run() { System.out.print(\"run \");}\n"+
				"  }\n"+				
				"}\n",
			},
			"run");		
	}

	public void testImplementingInterface_MethodWithParameters_JextendsG() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C extends groovy.lang.GroovyObjectSupport implements I<Integer> {\n"+
				"  public void m(String s) { }\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.groovy",
				"package p;\n"+
				"public interface I<T extends Number> {\n" + 
				"  void m(String s);\n"+
				"}\n",
			},
			"success");
	}

	public void testImplementingInterface_MethodWithParameters2_JextendsG() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C extends groovy.lang.GroovyObjectSupport implements I<Integer> {\n"+
				"  public void m(String s, Integer i) { }\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.groovy",
				"package p;\n"+
				"public interface I<T extends Number> {\n" + 
				"  void m(String s, Integer i);\n"+
				"}\n",
			},
			"success");
	}

	public void testImplementingInterface_MethodWithParameters2_GextendsJ() {
		this.runConformTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"public class C implements I<Integer> {\n"+
				"  public void m(String s, Integer i) { return null;}\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.java",
				"package p;\n"+
				"public interface I<T extends Number> {\n" + 
				"  void m(String s, Integer i);\n"+
				"}\n",
			},
			"success");
	}

	public void testImplementingInterface_MethodWithParameters3_GextendsJ() {
		this.runConformTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"public class C implements I<Integer> {\n"+
				"  public void m(String s, Integer i) { return null;}\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.java",
				"package p;\n"+
				"public interface I<T extends Number> {\n" + 
				"  void m(String s, T t);\n"+
				"}\n",
			},
			"success");
	}

	public void testImplementingInterface_MethodWithParameters3_JextendsG() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C extends groovy.lang.GroovyObjectSupport implements I<Integer> {\n"+
				"  public void m(String s, Integer i) { }\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.println( \"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/I.groovy",
				"package p;\n"+
				"public interface I<T extends Number> {\n" + 
				"  void m(String s, T t);\n"+
				"}\n",
			},
			"success");
	}
	
	public void testCallingMethods_JcallingG() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    new GClass().run();\n"+
				"  }\n"+
				"}\n",				

				"p/GClass.groovy",
				"package p;\n"+
				"public class GClass {\n" + 
				"  void run() {\n" +
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",
			},
			"success");
	}

	public void testCallingMethods_GcallingJ() {
		this.runConformTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    new OtherClass().run();\n"+
				"  }\n"+
				"}\n",				

				"p/OtherClass.java",
				"package p;\n"+
				"public class OtherClass {\n" + 
				"  void run() {\n" +
				"    System.out.println(\"success\");\n"+
				"  }\n"+
				"}\n",
			},
			"success");
	}

	public void testReferencingFields_JreferingToG() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    OtherClass oClass = new OtherClass();\n"+
				"    System.out.println(oClass.message);\n"+
				"  }\n"+
				"}\n",				

				"p/OtherClass.groovy",
				"package p;\n"+
				"public class OtherClass {\n" + 
				"  public String message =\"success\";\n"+
				"}\n",
			},
			"success");
	}

	public void testReferencingFields_GreferingToJ() {
		this.runConformTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    OtherClass oClass = new OtherClass();\n"+
				"    System.out.println(oClass.message);\n"+
				"  }\n"+
				"}\n",				

				"p/OtherClass.java",
				"package p;\n"+
				"public class OtherClass {\n" + 
				"  public String message =\"success\";\n"+
				"}\n",
			},
			"success");
	}

	public void testCallingConstructors_JcallingG() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    OtherClass oClass = new OtherClass();\n"+
				"    System.out.println(\"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/OtherClass.groovy",
				"package p;\n"+
				"public class OtherClass {\n" + 
				"  public OtherClass() {\n"+
				"  }\n"+
				"}\n",
			},
			"success");
	}
	
	public void testCallingConstructors_GcallingJ() {
		this.runConformTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    OtherClass oClass = new OtherClass();\n"+
				"    System.out.println(\"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/OtherClass.java",
				"package p;\n"+
				"public class OtherClass {\n" + 
				"  public OtherClass() {\n"+
				"  }\n"+
				"}\n",
			},
			"success");
	}
	
	public void testReferencingFieldsGenerics_JreferingToG() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    OtherClass oClass = new OtherClass();\n"+
				"    for (String message: oClass.messages) {\n"+
				"      System.out.print(message);\n"+
				"    }\n"+
				"  }\n"+
				"}\n",				

				"p/OtherClass.groovy",
				"package p;\n"+
				"public class OtherClass {\n" + 
				"  public List<String> messages = new ArrayList<String>();\n"+ // auto imports of java.util
				"  public OtherClass() {\n"+
				"    messages.add(\"hello\");\n"+
				"    messages.add(\" \");\n"+
				"    messages.add(\"world\");\n"+
				"    messages.add(\"\\n\");\n"+
				"  }\n"+
				"}\n",
			},
			"hello world");
	}
	
	public void testReferencingFieldsGenerics_GreferingToJ() {
		this.runConformTest(new String[] {
				"p/C.groovy",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    OtherClass oClass = new OtherClass();\n"+
				"    for (String message: oClass.messages) {\n"+
				"      System.out.print(message);\n"+
				"    }\n"+
				"  }\n"+
				"}\n",				

				"p/OtherClass.java",
				"package p;\n"+
				"import java.util.*;\n"+
				"public class OtherClass {\n" + 
				"  public List<String> messages = new ArrayList<String>();\n"+ // auto imports of java.util
				"  public OtherClass() {\n"+
				"    messages.add(\"hello\");\n"+
				"    messages.add(\" \");\n"+
				"    messages.add(\"world\");\n"+
				"    messages.add(\"\\n\");\n"+
				"  }\n"+
				"}\n",
			},
			"hello world");
	}
	
	public void testGroovyObjectsAreGroovyAtCompileTime() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    groovy.lang.GroovyObject oClass = new OtherClass();\n"+
				"    System.out.println(\"success\");\n"+
				"  }\n"+
				"}\n",

				"p/OtherClass.groovy",
				"package p;\n"+
				"import java.util.*;\n"+
				"public class OtherClass {\n" + 
				"}\n",
			},
			"success");
	}
	
	public void testCallGroovyObjectMethods_invokeMethod() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    groovy.lang.GroovyObject oClass = new OtherClass();\n"+
				"    String s = (String)oClass.invokeMethod(\"toString\",null);\n"+
				"    System.out.println(s);\n"+
				"  }\n"+
				"}\n",

				"p/OtherClass.groovy",
				"package p;\n"+
				"import java.util.*;\n"+
				"public class OtherClass {\n" + 
				"  String toString() { return \"success\";}\n"+
				"}\n",
			},
			"success");
	}

	public void testGroovyObjectsAreGroovyAtRunTime() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    OtherClass oClass = new OtherClass();\n"+
				"    System.out.println(oClass instanceof groovy.lang.GroovyObject);\n"+
				"  }\n"+
				"}\n",				

				"p/OtherClass.groovy",
				"package p;\n"+
				"import java.util.*;\n"+
				"public class OtherClass {\n" + 
				"}\n",
			},
			"true");
	}
	
	public void testGroovyBug() {
		this.runConformTest(new String[] {
				"p/A.groovy",
				"package p;\n" +
				"public class A<T> { public static void main(String[]argv) { print \"a\";}}\n",

				"p/B.groovy",
				"package p;\n" +
				"public class B extends A<String> {}",

			},
			"a");
	}

	public void testGroovyBug2() {
		this.runConformTest(new String[] {

				"p/B.groovy",
				"package p;\n" +
				"public class B extends A<String> {public static void main(String[]argv) { print \"a\";}}",

				"p/A.groovy",
				"package p;\n" +
				"public class A<T> { }\n",
			},
			"a");
	}


	// was worried <clinit> would surface in list of methods used to build the type declaration, but that doesn't appear to be the case
	public void testExtendingGroovyObjects_clinit() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    OtherClass oClass = new OtherClass();\n"+
				"    System.out.println(\"success\");\n"+
				"  }\n"+
				"}\n",				

				"p/OtherClass.groovy",
				"package p;\n"+
				"public class OtherClass {\n" + 
				"  { int i = 5; }\n" +
				"}\n",
			},
			"success");
	}

	public void testGroovyPropertyAccessors_ErrorCases1() {
		// check no duplicate created for 'String getProp'
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    G o = new G();\n"+
				"    System.out.print(o.getProp());\n"+
				"  }\n"+
				"}\n",				

				"p/G.groovy",
				"package p;\n"+
				"public class G {\n" + 
				"  String prop = 'foo'\n"+
				"  String getProp() { return prop; }\n"+
				"}\n",
			},
			"foo");
	}
	

	public void testGroovyPropertyAccessors_ErrorCases2() {
		// check no duplicate created for 'boolean isProp'
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    G o = new G();\n"+
				"    System.out.print(o.isProp());\n"+
				"  }\n"+
				"}\n",				

				"p/G.groovy",
				"package p;\n"+
				"public class G {\n" + 
				"  boolean prop = false\n"+
				"  boolean isProp() { return prop; }\n"+
				"}\n",
			},
			"false");
	}
	
	public void testGroovyPropertyAccessors_ErrorCases3() {
		// although there is a getProp already defined, it takes a parameter
		// so a new one should still be generated
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    G o = new G();\n"+
				"    System.out.print(o.getProp());\n"+
				"  }\n"+
				"}\n",				

				"p/G.groovy",
				"package p;\n"+
				"public class G {\n" + 
				"  String prop = 'foo'\n"+
				"  String getProp(String s) { return prop; }\n"+
				"}\n",
			},
			"foo");
	}
	
	public void testGroovyPropertyAccessors_ErrorCases4() {
		// although there is a setProp already defined, it takes no parameters
		// so a new one should still be generated
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    G o = new G();\n"+
				"    o.setProp(\"abc\");\n"+
				"    System.out.print(\"abc\");\n"+
				"  }\n"+
				"}\n",				

				"p/G.groovy",
				"package p;\n"+
				"public class G {\n" + 
				"  String prop = 'foo'\n"+
				"  void setProp() { }\n"+
				"}\n",
			},
			"abc");
	}

	// testcase commented out - seems groovyc doesn't allow this - fails to generate the setProp(H)...
//	public void testGroovyPropertyAccessors_ErrorCases5() {
//		// although there is a setProp already defined, it takes a parameter
//		// of a different type to the property type
//		this.runConformTest(new String[] {
//				"p/C.java",
//				"package p;\n" +
//				"public class C {\n"+
//				"  public static void main(String[] argv) {\n"+
//				"    G o = new G();\n"+
//				"    o.setProp(new H());\n"+
//				"    System.out.print(\"abc\");\n"+
//				"  }\n"+
//				"}\n",				
//				
//				"p/H.java",
//				"package p;\n"+
//				"class H{}\n",
//				
//				"p/J.java",
//				"package p;\n"+
//				"class J{}\n",
//				
//				"p/G.groovy",
//				"package p;\n"+
//				"public class G {\n" + 
//				"  H prop\n"+
//				"  void setProp(J b) { }\n"+
//				"}\n",
//			},
//			"abc");
//	}
//	see reasoning on previous testcase
//	public void testGroovyPropertyAccessors_ErrorCases6() {
//		// although there is a setProp already defined, it takes a parameter
//		// of a different type to the property type
//		this.runConformTest(new String[] {
//				"p/C.java",
//				"package p;\n" +
//				"public class C {\n"+
//				"  public static void main(String[] argv) {\n"+
//				"    G o = new G();\n"+
//				"    o.setProp(\"abc\");\n"+
//				"    System.out.print(\"abc\");\n"+
//				"  }\n"+
//				"}\n",				
//
//				"p/G.groovy",
//				"package p;\n"+
//				"public class G {\n" + 
//				"  String prop = 'foo'\n"+
//				"  void setProp(boolean b) { }\n"+
//				"}\n",
//			},
//			"abc");
//	}
	
	public void testGroovyPropertyAccessors() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    G o = new G();\n"+
				"    System.out.print(o.isB());\n"+
				"    System.out.print(o.getB());\n"+
				"  }\n"+
				"}\n",				

				"p/G.groovy",
				"package p;\n"+
				"public class G {\n" + 
				"  boolean b\n"+
				"}\n",
			},
			"falsefalse");
	}



	public void testGroovyPropertyAccessors_Set() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    G o = new G();\n"+
				"    System.out.print(o.getB());\n"+
				"    o.setB(true);\n"+
				"    System.out.print(o.getB());\n"+
				"  }\n"+
				"}\n",				

				"p/G.groovy",
				"package p;\n"+
				"public class G {\n" + 
				"  boolean b\n"+
				"}\n",
			},
			"falsetrue");
	}
	
	

	public void testGroovyPropertyAccessorsGenerics() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    G o = new G();\n"+
				"    for (Integer s: o.getB()) {\n"+
				"      System.out.print(s);\n"+				
				"    }\n"+
				"  }\n"+
				"}\n",				

				"p/G.groovy",
				"package p;\n"+
				"public class G {\n" + 
				"  List<Integer> b = [1,2,3]\n"+
				"}\n",
			},
			"123");
	}

	public void testDefaultValueMethods() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    G o = new G();\n"+
				"    o.m(\"abc\",3);\n"+
				"    o.m(\"abc\");\n"+
				"  }\n"+
				"}\n",				

				"p/G.groovy",
				"package p;\n"+
				"public class G {\n" + 
				"  public void m(String s,Integer i=3) { print s }\n"+
				"}\n",
			},
			"abcabc");
		String expectedOutput = 
			"package p;\n" + 
			"public class G {\n" + 
			"  public G() {\n" + 
			"  }\n" + 
			"  public void m(public String s, public Integer i) {\n" + 
			"  }\n" + 
			"  public void m(public String s) {\n" + 
			"  }\n" + 
			"}\n";
		checkGCUDeclaration("G.groovy",expectedOutput);
		expectedOutput =
			"  \n" + 
			"  public void m(String s, Integer i);\n" + 
			"  \n";
		checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
		expectedOutput =
			"  \n" + 
			"  public void m(String s);\n" + 
			"  \n";
		checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
	}

	public void testDefaultValueMethods02() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    G o = new G();\n"+
				"    String str=\"xyz\";\n"+
				"    o.m(str,1,str,str,4.0f,str);\n"+
				"    o.m(str,1,str,str,str);\n"+
				"    o.m(str,1,str,str);\n"+
				"    o.m(str,str,str);\n"+
				"  }\n"+
				"}\n",				

				"p/G.groovy",
				"package p;\n"+
				"public class G {\n" + 
				"  public void m(String s, Integer i=3, String j=\"abc\", String k, float f = 3.0f, String l) { print s+f }\n"+
				"}\n",
			},
			"xyz4.0xyz3.0xyz3.0xyz3.0");

		String expectedOutput = 
			"package p;\n" + 
			"public class G {\n" + 
			"  public G() {\n" + 
			"  }\n" + 
			"  public void m(public String s, public Integer i, public String j, public String k, public float f, public String l) {\n" + 
			"  }\n" + 
			"  public void m(public String s, public Integer i, public String j, public String k, public String l) {\n" + 
			"  }\n" + 
			"  public void m(public String s, public Integer i, public String k, public String l) {\n" + 
			"  }\n" + 
			"  public void m(public String s, public String k, public String l) {\n" + 
			"  }\n" + 
			"}\n";
		checkGCUDeclaration("G.groovy", expectedOutput);
		
		expectedOutput = 
			"  \n" + 
			"  public void m(String s, Integer i, String j, String k, float f, String l);\n" + 
			"  \n";
		checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
		expectedOutput = 
			"  \n" + 
			"  public void m(String s, Integer i, String j, String k, String l);\n" + 
			"  \n";
		checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
		expectedOutput = 
			"  \n" + 
			"  public void m(String s, Integer i, String k, String l);\n" + 
			"  \n";
		checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
		expectedOutput = 
			"  \n" + 
			"  public void m(String s, String k, String l);\n" + 
			"  \n";
		checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
	}

	
	public void testDefaultValueConstructors() {
		this.runConformTest(new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n"+
				"  public static void main(String[] argv) {\n"+
				"    G o = new G(2,\"abc\");\n"+
				"    o.print();\n"+
				"    o = new G(3);\n"+
				"    o.print();\n"+
				"  }\n"+
				"}\n",				

				"p/G.groovy",
				"package p;\n"+
				"public class G {\n" + 
				"  def msg\n"+
				"  public G(Integer i, String m=\"abc\") {this.msg = m;}\n"+
				"  public void print(int i=3) { print msg }\n"+
				"}\n",
			},
			"abcabc");
		String expectedOutput=
			"package p;\n" + 
			"public class G {\n" + 
			"  private java.lang.Object msg;\n" + 
			"  public G(public Integer i, public String m) {\n" + 
			"  }\n" + 
			"  public G(public Integer i) {\n" + 
			"  }\n" + 
			"  public void print(public int i) {\n" + 
			"  }\n" + 
			"  public void print() {\n" + 
			"  }\n" + 
			"}\n";
		checkGCUDeclaration("G.groovy", expectedOutput );
		expectedOutput =
			"  \n" + 
			"  public G(Integer i, String m);\n" + 
			"  \n";
		checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
		expectedOutput =
			"  \n" + 
			"  public G(Integer i);\n" + 
			"  \n";
		checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
	}
	
	// FIXASC1) groovy bug? Crashes groovy, it doesn't seem to correctly handle clashing constructor variants
//	public void testDefaultValueConstructors02() {
//		this.runConformTest(new String[] {
//				"p/C.java",
//				"package p;\n" +
//				"public class C {\n"+
//				"  public static void main(String[] argv) {\n"+
//				"    G o = new G(2,\"abc\");\n"+
//				"    o.print();\n"+
//				"    o = new G(3);\n"+
//				"    o.print();\n"+
//				"  }\n"+
//				"}\n",				
//
//				"p/G.groovy",
//				"package p;\n"+
//				"public class G {\n" + 
//				"  def msg\n"+
//				"  public G(Integer i) {}\n"+
//				"  public G(Integer i, String m=\"abc\") {this.msg = m;}\n"+
//				"  public void print(int i=3) { print msg }\n"+
//				"}\n",
//			},
//			"abc");
//	}
	
	public void testAnnotationsAndMetaMethods() {
		
		this.runConformTest(new String[] {
				"p/A.java",
				"package p; public class A{ public static void main(String[]argv){}}",
				
				"p/Validateable.groovy",
				"import java.lang.annotation.Retention\n"+
				"import java.lang.annotation.RetentionPolicy\n"+
				"import java.lang.annotation.Target\n"+
				"import java.lang.annotation.ElementType\n"+
				"@Retention(RetentionPolicy.RUNTIME)\n"+
				"@Target([ElementType.TYPE])\n"+
				"public @interface Validateable { }\n",
		},"");
	}

	public void testClashingMethodsWithDefaultParams() {
		this.runNegativeTest(new String[] {
				"p/Code.groovy",
				"package p;\n"+
				"\n"+
				"class Code {\n"+
				"  public void m(String s) {}\n"+
				"  public void m(String s, Integer i =3) {}\n"+
				"}\n",
		},
		"----------\n" + 
		"1. ERROR in p\\Code.groovy (at line 5)\n" + 
		"	public void m(String s, Integer i =3) {}\n" + 
		"	^\n" + 
		"Groovy:The method with default parameters \"void m(java.lang.String, java.lang.Integer)\" defines a method \"void m(java.lang.String)\" that is already defined.\n"+ 
		"----------\n"
		);
	}
	
	public void testCallingJavaFromGroovy1() throws Exception {
		this.runConformTest(new String[] {
				"p/Code.groovy",
				"package p;\n"+
				"class Code {\n"+
				"  public static void main(String[] argv) {\n"+
				"    new J().run();\n"+
				"    print new J().name;\n"+
				"  }\n"+
				"}\n",
				
				"p/J.java",
				"package p;\n"+
				"public class J {\n"+
				"  public String name = \"name\";\n"+
				"  public void run() { System.out.print(\"success\"); }\n"+
				"}\n",
		},"successname");
		//checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p/Code.class", "Code", "");
	}

	public void testCallingJavaFromGroovy2() throws Exception {
		this.runConformTest(new String[] {
				"p/Code.groovy",
				"package p;\n"+
				"@Wibble(value=4)\n"+
				"class Code {\n"+
				"  public static void main(String[] argv) {\n"+
				"    new J().run();\n"+
				"  }\n"+
				"}\n",
				
				"p/J.java",
				"package p;\n"+
				"public class J {\n"+
				"  public String name = \"name\";\n"+
				"  public void run() { System.out.print(\"success\"); }\n"+
				"}\n",
				
				"p/Wibble.java",
				"package p;\n"+
				"public @interface Wibble {\n"+
				"  int value() default 3;\n"+
				"}\n",
		},"success");
	}
	
	public void testGenericFields_JcallingG() {
		this.runConformTest(new String[] {
				"p/Code.java",
				"package p;\n"+
				"public class Code extends G<String> {\n"+
				"  public static void main(String[] argv) {\n"+
				"    Code c = new Code();\n"+
				"    c.setField(\"success\");\n"+
				"    System.out.print(c.getField());\n"+
				"  }\n"+
				"}\n",
				
				"p/G.groovy",
				"package p;\n"+
				"class G<T> { T field; }"
		},"success");
	}

	public void testGenericFields_GcallingJ() {
		this.runConformTest(new String[] {
				"p/Code.groovy",
				"package p;\n"+
				"public class Code extends G<String> {\n"+
				"  public static void main(String[] argv) {\n"+
				"    Code c = new Code();\n"+
				"    c.field=\"success\";\n"+
				"    System.out.print(c.field);\n"+
				"  }\n"+
				"}\n",
				
				"p/G.java",
				"package p;\n"+
				"class G<T> { public T field; }" // TODO why must this be public for the groovy code to see it?  If non public should it be instead defined as a property on the JDTClassNode rather than a field?
		},"success");
	}

	public void testExtendingRawJavaType() {
		this.runConformTest(new String[] {
				"p/Foo.groovy",
				"package p;\n"+
				"public class Foo extends Supertype {\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.print(\"success\");\n"+
				"  }\n"+
				"}\n",
				
				"p/Supertype.java",
				"package p;\n"+
				"class Supertype<T> extends Supertype2 { }",
				
				"p/Supertype2.java",
				"package p;\n"+
				"class Supertype2<T> { }"
		},"success");
	}
	
	public void testTypeVariableBoundIsRawType() {
		this.runConformTest(new String[] {
				"p/Foo.groovy",
				"package p;\n"+
				"public class Foo extends Supertype {\n"+
				"  public static void main(String[] argv) {\n"+
				"    System.out.print(\"success\");\n"+
				"  }\n"+
				"}\n",
				
				"p/Supertype.java",
				"package p;\n"+
				"class Supertype<T extends Supertype2> { }",
				
				"p/Supertype2.java",
				"package p;\n"+
				"class Supertype2<T> { }"
		},"success");
	}
	
	public void testEnum() {
		this.runConformTest(new String[] {
				"p/Foo.groovy",
				"package p;\n"+
				"public class Foo /*extends Supertype<Goo>*/ {\n"+
				"  public static void main(String[] argv) {\n"+
				"    print Goo.R\n"+
				"  }\n"+
				"}\n",
				
				"p/Goo.java",
				"package p;\n"+
				"enum Goo { R,G,B; }",
			},"R");
	}
	
	// Type already implements invokeMethod(String,Object) - should not be an error, just don't add the method
	public void testDuplicateGroovyObjectMethods() {
		this.runConformTest(new String[] {
				"p/Foo.groovy",
				"package p;\n"+
				"public class Foo /*extends Supertype<Goo>*/ {\n"+
				" public Object invokeMethod(String s, Object o) {\n" +
				" return o;}\n"+
				"  public static void main(String[] argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",
			},"success");
	}

	public void testDuplicateGroovyObjectMethods2() {
		this.runConformTest(new String[] {
				"p/Foo.groovy",
				"package p;\n"+
				"public class Foo /*extends Supertype<Goo>*/ {\n"+
				"  public MetaClass getMetaClass() {return null;}\n"+
				"  public void setMetaClass(MetaClass mc) {}\n"+
				"  public Object getProperty(String propertyName) {return null;}\n"+
				"  public void setProperty(String propertyName,Object newValue) {}\n"+
				"  public static void main(String[] argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n",
			},"success");
	}
	

	public void testTwoTopLevelTypesInAFile() {
		this.runConformTest(new String[] {
				"p/First.groovy",
				"package p;\n"+
				"public class First {\n"+
				"  public static void main(String[] argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"}\n"+
				"class Second {\n"+
				"}\n",
			},"success");
	}

	public void testImports1() {
		this.runConformTest(new String[] {
				"p/First.groovy",
				"package p;\n"+
				"import java.util.regex.Pattern\n"+
				"public class First {\n"+
				"  public static void main(String[] argv) {\n"+
				"    Pattern p = Pattern.compile(\".\")\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"  public Pattern getPattern() { return null;}\n"+
				"}\n",
			},"success");
	}
	
	public void testImports2() {
		this.runConformTest(new String[] {
				"p/First.groovy",
				"package p;\n"+
				"import java.util.regex.Pattern\n"+
				"public class First {\n"+
				"  public static void main(String[] argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"  public File getFile() { return null;}\n"+ // java.io.File should be picked up magically
				"}\n",
			},"success");
	}

	public void testImportsBigDecimal1() {
		this.runConformTest(new String[] {
				"p/First.groovy",
				"package p;\n"+
				"import java.util.regex.Pattern\n"+
				"public class First {\n"+
				"  public static void main(String[] argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"  public BigDecimal getFile() { return null;}\n"+ // java.io.File should be picked up magically
				"}\n",
			},"success");
	}


	// this version has no imports of its own, that can make a difference
	public void testImportsBigDecimal2() {
		this.runConformTest(new String[] {
				"p/First.groovy",
				"package p;\n"+
				"public class First {\n"+
				"  public static void main(String[] argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"  public BigDecimal getFile() { return null;}\n"+ // java.io.File should be picked up magically
				"}\n",
			},"success");
	}

	public void testImportsBigInteger1() {
		this.runConformTest(new String[] {
				"p/First.groovy",
				"package p;\n"+
				"import java.util.regex.Pattern\n"+
				"public class First {\n"+
				"  public static void main(String[] argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"  public BigInteger getFile() { return null;}\n"+ // java.io.File should be picked up magically
				"}\n",
			},"success");
	}
	
	// Check we can refer to BigInteger from a static constant
	public void testImportsBigInteger3() {
		this.runConformTest(new String[] {
				"p/First.groovy",
				"package p;\n"+
				"import java.util.regex.Pattern\n"+
				"public class First {\n"+
				"  private final static BigInteger bd = new BigInteger(\"3\");\n"+
				"  public static void main(String[] argv) {\n"+
				"    print bd+\"success\"\n"+
				"  }\n"+
				"  public BigDecimal getFile() { return null;}\n"+ // java.io.File should be picked up magically
				"}\n",
			},"3success");
	}

	public void testMultipleTypesInOneFile01() {
		this.runConformTest(new String[] {
				"p/Foo.groovy",
				"package p;\n"+
				"class Foo {\n"+
				"  public static void main(String[] argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+				
				"}\n"+
				"class Goo {}\n"
			},"success");
	}

	// Refering to the secondary type from the primary (but internally to a method)
	public void testMultipleTypesInOneFile02() {
		this.runConformTest(new String[] {
				"p/Foo.groovy",
				"package p;\n"+
				"class Foo {\n"+
				"  public static void main(String[] argv) {\n"+
				"    new Goo();\n"+
				"    print \"success\"\n"+
				"  }\n"+				
				"}\n"+
				"class Goo {}\n"
			},"success");
	}

	// Refering to the secondary type from the primary - from a method param
	public void testMultipleTypesInOneFile03() {
		this.runConformTest(new String[] {
				"p/Foo.groovy",
				"package p;\n"+
				"class Foo {\n"+
				"  public static void main(String[] argv) {\n"+
				"    new Foo().runnit(new Goo());\n"+
				"  }\n"+			
				"  public void runnit(Goo g) {"+
				"    print \"success\"\n"+
				"  }\n"+			
				"}\n"+
				"class Goo {}\n"
			},"success");
	}


	// Refering to the secondary type from the primary - from a method
	public void testJDKClasses() {
		this.runConformTest(new String[] {
				"p/Foo.groovy",
				"package p;\n"+
				"class Foo {\n"+
				"  public static void main(String[] argv) {\n"+
				"    new Foo().runnit(new Goo());\n"+
				"  }\n"+			
				"  public void runnit(Goo g) {"+
				"    print \"success\"\n"+
				"  }\n"+			
				"}\n"+
				"class Goo {}\n"
			},"success");
	}


	
	public void testImportsBigInteger2() {
		this.runConformTest(new String[] {
				"p/First.groovy",
				"package p;\n"+
				"public class First {\n"+
				"  public static void main(String[] argv) {\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"  public BigInteger getFile() { return null;}\n"+
				"}\n",
			},"success");
	}

	// Test that the (package visible) source type in another package is visible to a groovy type
	public void testVisibility() {
		this.runConformTest(new String[] {
				"p/First.groovy",
				"package p;\n"+
				"import q.Second;\n"+
				"public class First {\n"+
				"  public static void main(String[] argv) {\n"+
				"    new First().getIt();\n"+
				"    print \"success\"\n"+
				"  }\n"+
				"  public Second getIt() { return null;}\n"+
				"}\n",
				"q/Second.java",
				"package q;\n"+
				"class Second {}\n",
			},"success");
	}


	public void testClosuresBasic() {
		this.runConformTest(new String[] {
			"Coroutine.groovy",
			"def iterate(n, closure) {\n"+
			"  1.upto(n) {\n" + 
			"    closure(it);\n" + 
			"  }\n"+
			"}\n"+
			"iterate (3) {\n"+
			"  print it\n"+
			"}\n"
		},		
		"123");
	}
	
	
	public void testGrab() {
		this.runConformTest(new String[]{
				"Printer.groovy",
				"import groovy.lang.Grab;\n"+
				"\n"+
				"@Grab(group=\"joda-time\", module=\"joda-time\", version=\"1.6\")\n"+
				"def printDate() {\n"+
				"      def dt = new org.joda.time.DateTime()\n"+
				"}\n"+
				"printDate()"},"");
	}
	
	public void testScriptWithError() {
		this.runNegativeTest(new String[] {
			"Foo.groovy",
			"print Coolio!",
		},		
		"----------\n" + 
		"1. ERROR in Foo.groovy (at line 1)\n" + 
		"	print Coolio!\n" + 
		"	            ^\n" + 
		"Groovy:expecting EOF, found \'!\' @ line 1, column 13.\n" + 
		"----------\n"
		);
	}

	public void testScript() {
		this.runConformTest(new String[] {
			"Foo.groovy",
			"print 'Coolio'\n",
		},"Coolio");
	}
	
	public void testScriptCallJava() {
		this.runConformTest(new String[] {
			"Foo.groovy",
			"print SomeJava.constant\n",
			"SomeJava.java",
			"class SomeJava { static String constant = \"abc\";}"
		},"abc");
	}
	
	/**
	 * COOL!!!  The getInstance() method is added by a late AST Transformation made due to the Singleton annotation - and yet
	 * still it is referencable from Java.  This is not possible with normal joint compilation.
	 * currently have to 'turn on' support in GroovyClassScope.getAnyExtraMethods() - still thinking about this stuff...
	 */
//	public void testJavaAccessingTransformedGroovy_Singleton() {
//		this.runConformTest(new String[] {
//				"Goo.groovy",
//				"class Goo {\n"+
//				"  public static void main(String[] argv) {\n"+
//				"    Run.main(argv);\n"+
//				"  }\n"+			
//				"}\n",
//
//				"Run.java",
//				"public class Run {\n"+
//				"  public static void main(String[] argv) {\n"+
//				"    System.out.println(Wibble.getInstance().field);\n"+
//				"  }\n"+			
//				"}\n",
//				
//				"Wibble.groovy",
//				"@Singleton class Wibble {" +
//				"  public String field = 'abc';\n"+
//				"}\n"
//			},"abc");
//	}

	public void testBuiltInTransforms_Singleton() {
		this.runConformTest(new String[] {
			"Goo.groovy",
			"class Goo {\n"+
			"  public static void main(String[] argv) {\n"+
			"    Run.main(argv);\n"+
			"  }\n"+			
			"}\n",

			"Run.groovy",
			"public class Run {\n"+
			"  public static void main(String[] argv) {\n"+
			"    System.out.println(Wibble.getInstance().field);\n"+
			"  }\n"+			
			"}\n",
			
			"Wibble.groovy",
			"@Singleton class Wibble {" +
			"  public String field = 'abcd';\n"+
			"}\n"
		},"abcd");
	}
	
	// lazy option set in Singleton
	public void testBuiltInTransforms_Singleton2() {
		this.runConformTest(new String[] {
			"Goo.groovy", 
			"class Goo {\n"+
			"  public static void main(String[] argv) {\n"+
			"    Run.main(argv);\n"+
			"  }\n"+			
			"}\n",

			"Run.groovy",
			"public class Run {\n"+
			"  public static void main(String[] argv) {\n"+
			"    Wibble.run();\n"+
			"    System.out.print(\"running \");\n"+
			"    System.out.print(Wibble.getInstance().field);\n"+
			"  }\n"+			
			"}\n",
			
			"Wibble.groovy",
			"@Singleton(lazy=false) class Wibble {" +
			"  public String field = 'abcd';\n"+
			"  private Wibble() { print \"ctor \";}\n"+
			"  static void run() {}\n"+
			"}\n"
		},"ctor running abcd");
	}
	
	public void testBuiltInTransforms_Singleton3() {
		this.runConformTest(new String[] {
			"Goo.groovy", 
			"class Goo {\n"+
			"  public static void main(String[] argv) {\n"+
			"    Run.main(argv);\n"+
			"  }\n"+			
			"}\n",

			"Run.groovy",
			"public class Run {\n"+
			"  public static void main(String[] argv) {\n"+
			"    Wibble.run();\n"+
			"    System.out.print(\"running \");\n"+
			"    System.out.print(Wibble.getInstance().field);\n"+
			"  }\n"+			
			"}\n",
			
			"Wibble.groovy",
			"@Singleton(lazy=true) class Wibble {" +
			"  public String field = 'abcd';\n"+
			"  private Wibble() { print \"ctor \";}\n"+
			"  static void run() {}\n"+
			"}\n"
		},"running ctor abcd");
	}

	// http://groovy.codehaus.org/Category+and+Mixin+transformations
	public void testBuiltInTransforms_Category1() {
		this.runConformTest(new String[] {
			"Demo.groovy",
			"	use(NumberCategory) {\n"+
			"	    def dist = 300.meters\n"+
			"\n"+
			"	    assert dist instanceof Distance\n"+
			"	    assert dist.toString() == \"300m\"\n"+
			"  print dist.toString()\n"+
			"	}\n",
			
			"Distance.groovy",
			"	final class Distance {\n"+
			"	    def number\n"+
			"	    String toString() { \"${number}m\" }\n"+
			"	}\n",
			
			"NumberCategory.groovy",
			"	class NumberCategory {\n"+
			"	    static Distance getMeters(Number self) {\n"+
			"	        new Distance(number: self)\n"+
			"	    }\n"+
			"	}\n"+
			"\n",
			
		},"300m");
	}
	
	// http://groovy.codehaus.org/Category+and+Mixin+transformations
	public void testBuiltInTransforms_Category2() {
		this.runConformTest(new String[] {
			"Demo.groovy",
			"	use(NumberCategory) {\n"+
			"	    def dist = 300.meters\n"+
			"\n"+
			"	    assert dist instanceof Distance\n"+
			"	    assert dist.toString() == \"300m\"\n"+
			"  print dist.toString()\n"+
			"	}\n",
			
			"Distance.groovy",
			"	final class Distance {\n"+
			"	    def number\n"+
			"	    String toString() { \"${number}m\" }\n"+
			"	}\n",
			
			"NumberCategory.groovy",
			"	@Category(Number) class NumberCategory {\n"+
			"	    Distance getMeters() {\n"+
			"	        new Distance(number: this)\n"+
			"	    }\n"+
			"	}\n"+
			"\n",
			
		},"300m");
	}
	
	// http://groovy.codehaus.org/Category+and+Mixin+transformations
	public void testBuiltInTransforms_Category3() {
		this.runConformTest(new String[] {
			"Foo.groovy",
			"assert new Plane().fly() ==\n"+
			"       \"I'm the Concorde and I fly!\"\n"+
			"assert new Submarine().dive() ==\n"+
			"       \"I'm the Yellow Submarine and I dive!\"\n"+
			"\n"+
			"assert new JamesBondVehicle().fly() ==\n"+
			"       \"I'm the James Bond's vehicle and I fly!\"\n"+
			"assert new JamesBondVehicle().dive() ==\n"+
			"       \"I'm the James Bond's vehicle and I dive!\"\n"+
			"print new JamesBondVehicle().dive();\n",

			"FlyingAbility.groovy",
			"@Category(Vehicle) class FlyingAbility {\n"+
			"    def fly() { \"I'm the ${name} and I fly!\" }\n"+
			"}\n",
			
			"DivingAbility.groovy",		
			"@Category(Vehicle) class DivingAbility {\n"+
			"    def dive() { \"I'm the ${name} and I dive!\" }\n"+
			"}\n",
			
			"Vehicle.java",
			"interface Vehicle {\n"+
			"    String getName();\n"+
			"}\n",
			
			"Submarine.groovy",
			"@Mixin(DivingAbility)\n"+
			"class Submarine implements Vehicle {\n"+
			"    String getName() { \"Yellow Submarine\" }\n"+
			"}\n",
			
			"Plane.groovy",
			"@Mixin(FlyingAbility)\n"+
			"class Plane implements Vehicle {\n"+
			"    String getName() { \"Concorde\" }\n"+
			"}\n",

			"JamesBondVehicle.groovy",
			"@Mixin([DivingAbility, FlyingAbility])\n"+
			"class JamesBondVehicle implements Vehicle {\n"+
			"    String getName() { \"James Bond's vehicle\" }\n"+
			"}\n",
		},"I'm the James Bond's vehicle and I dive!");
	}
	// http://groovy.codehaus.org/PackageScope+transformation
	// Adjust the visibility of a property so instead of private it is package default
	public void testBuiltInTransforms_PackageScope() {
		this.runConformTest(new String[] {
			"Goo.groovy",
			"class Goo {\n"+
			"  public static void main(String[] argv) {\n"+
			"    q.Run.main(argv);\n"+
			"  }\n"+			
			"}\n",

			"q/Run.groovy",
			"package q;\n"+
			"import q.Wibble;\n"+
			"public class Run {\n"+
			"  public static void main(String[] argv) throws Exception {\n"+
			"    Wibble w = new Wibble();\n"+
			"    System.out.print(Wibble.class.getDeclaredField(\"field\").getModifiers());\n"+
			"    System.out.print(Wibble.class.getDeclaredField(\"field2\").getModifiers());\n"+
			"  }\n"+			
			"}\n",
			
			"q/Wibble.groovy",
			"package q\n"+
			"class Wibble {" +
			"  String field = 'abcd';\n"+
			"  @PackageScope String field2 = 'abcd';\n"+
			"}\n"
		},"20"); // 0x2 = private 0x0 = default (so field2 has had private vis removed by annotation)
	}
	
	public void testGroovyAnnotation() {
		runConformTest(new String[] {
				"Foo.groovy",
				"@interface A {}\n",
				
				"Goo.groovy",
				"@A class Goo {}\n"
		},"");
		String expectedContents = 
			"// Compiled from Foo.groovy (version 1.5 : 49.0, no super bit)\n" + 
			"public abstract @interface A extends java.lang.annotation.Annotation {\n" + 
//			(GroovyUtils.GROOVY_LEVEL<18?"":
				"\n"+
				"  Inner classes:\n" + 
				"    [inner class info: #9 A$1, outer class info: #2 A\n" + 
				"     inner name: #10 1, accessflags: 4128 default]\n" +"}";
		checkDisassemblyFor("A.class",expectedContents);
	
	}

	
	// FIXASC1) test mechanism for running JUnit based tests not yet in place for these Spock tests

//	public void testTransforms_Spock() {
//		Map options = getCompilerOptions();
//		options.put(CompilerOptions.OPTIONG_GroovyClassLoaderPath, new File("astTransformations/spock-core-0.1.jar").getAbsolutePath());
//
//		runConformTest(new String[] {
//				"Assertions.groovy",
//				"import spock.lang.*\n"+
//				"@Speck\n"+
//				"class Assertions {\n"+
////				"  public static void main(String[] argv) { new Assertions().comparingXandY();}\n"+
//				"  def comparingXandY() {\n"+
//				"    def x = 1\n"+
//				"    def y = 2\n"+
//				"    \n"+
////				" print 'a'\n"+
//				"    expect:\n"+
//				"    x < y    // OK\n"+
//				"    x == y   // BOOM!\n"+
//				" }\n"+
//				"}"},
//				"----------\n" + 
//				"1. ERROR in Assertions.groovy (at line 4)\n" + 
//				"	public static void main(String[] argv) {\n" + 
//				"	^^\n" + 
//				"Groovy:Feature methods must not be static @ line 4, column 2.\n" + 
//				"----------\n",
//				null,
//				true,
//				null,
//				options,
//				null);
//		
//		// FIXASC1) make negative test for this:
////		
////		runConformTest(new String[] {
////				"Assertions.groovy",
////				"import spock.lang.*\n"+
////				"@Speck\n"+
////				"class Assertions {\n"+
////				" public static void main(String[] argv) {\n"+
////				//				"  def \"comparing x and y\"() {\n"+
////				"    def x = 1\n"+
////				"    def y = 2\n"+
////				"    \n"+
//////				" print 'a'\n"+
////				"    expect:\n"+
////				"    x < y    // OK\n"+
////				"    x == y   // BOOM!\n"+
////				" }\n"+
////				"}"},
////				"----------\n" + 
////				"1. ERROR in Assertions.groovy (at line 4)\n" + 
////				"	public static void main(String[] argv) {\n" + 
////				"	^^\n" + 
////				"Groovy:Feature methods must not be static @ line 4, column 2.\n" + 
////				"----------\n",
////				null,
////				true,
////				null,
////				options,
////				null);
//	}
//				
//	public void testTransforms_Spock2() {
//		Map options = getCompilerOptions();
//		options.put(CompilerOptions.OPTIONG_GroovyClassLoaderPath, new File("astTransformations/spock-core-0.1.jar").getAbsolutePath());
//		
//		runConformTest(new String[] {
//			"HelloSpock.groovy",
//			"import org.junit.runner.RunWith\n"+
//			"import spock.lang.*\n"+
//			"@Speck\n"+
//			"@RunWith(Sputnik)\n"+
//			"class HelloSpock {\n"+
//			"  def \"can you figure out what I'm up to?\"() {\n"+
//			"  print 'xxx'\n"+
//			"    expect:\n"+
//			"    name.size() == size\n"+
//			"\n"+
//			"    where:\n"+
//			"    name << ['Kirk', 'Spock', 'Scotty']\n"+
//			"    size << [4, 5, 6, 7]\n"+
//			"  }\n"+
//			"}"
//		},"",
//		null,
//		true,
//		null,
//		options,
//		null);
//
//		String expectedOutput = "public @Anno(p.Target.class) int foo";
//		checkGCUDeclaration("HelloSpock.groovy",expectedOutput); 
//	}
	
	

	
	public void testTransforms_BasicLogging() throws IOException {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTIONG_GroovyClassLoaderPath, FileLocator.resolve(Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry("astTransformations/transforms.jar")).getFile());
		options.put(CompilerOptions.OPTIONG_GroovyProjectName, "Test");
		// From: http://svn.codehaus.org/groovy/trunk/groovy/groovy-core/src/examples/transforms/local
		runConformTest(new String[] {
			"examples/local/LoggingExample.groovy",
			"package examples.local\n"+
			"\n"+
			"/**\n"+
			"* Demonstrates how a local transformation works. \n"+
			"* \n"+
			"* @author Hamlet D'Arcy\n"+
			"*/ \n"+
			"\n"+
			"def greet() {\n"+
			"    println \"Hello World\"\n"+
			"}\n"+
			"    \n"+
			"@WithLogging    //this should trigger extra logging\n"+
			"def greetWithLogging() {\n"+
			"    println \"Hello World\"\n"+
			"}\n"+
			"    \n"+
			"// this prints out a simple Hello World\n"+
			"greet()\n"+
			"\n"+
			"// this prints out Hello World along with the extra compile time logging\n"+
			"greetWithLogging()\n"+
			"\n"+
			"\n"+
			"//\n"+
			"// The rest of this script is asserting that this all works correctly. \n"+
			"//\n"+
			"\n"+
			"def oldOut = System.out\n"+
			"// redirect standard out so we can make assertions on it\n"+
			"def standardOut = new ByteArrayOutputStream();\n"+
			"System.setOut(new PrintStream(standardOut)); \n"+
			"  \n"+
			"greet()\n"+
			"assert \"Hello World\" == standardOut.toString(\"ISO-8859-1\").trim()\n"+
			"\n"+
			"// reset standard out and redirect it again\n"+
			"standardOut.close()\n"+
			"standardOut = new ByteArrayOutputStream();\n"+
			"System.setOut(new PrintStream(standardOut)); \n"+
			"\n"+
			"greetWithLogging()\n"+
			"def result = standardOut.toString(\"ISO-8859-1\").split('\\n')\n"+
			"assert \"Starting greetWithLogging\"  == result[0].trim()\n"+
			"assert \"Hello World\"                == result[1].trim()\n"+
			"assert \"Ending greetWithLogging\"    == result[2].trim()\n"+
			"\n"+
			"System.setOut(oldOut);\n"+
			"print 'done'\n"+
			"\n",
			
//			"examples/local/WithLogging.groovy",
//			"package examples.local\n"+
//			"import java.lang.annotation.Retention\n"+
//			"import java.lang.annotation.Target\n"+
//			"import org.codehaus.groovy.transform.GroovyASTTransformationClass\n"+
//			"import java.lang.annotation.ElementType\n"+
//			"import java.lang.annotation.RetentionPolicy\n"+
//			"\n"+
//			"/**\n"+
//			"* This is just a marker interface that will trigger a local transformation. \n"+
//			"* The 3rd Annotation down is the important one: @GroovyASTTransformationClass\n"+
//			"* The parameter is the String form of a fully qualified class name. \n"+
//			"*\n"+
//			"* @author Hamlet D'Arcy\n"+
//			"*/ \n"+
//			"@Retention(RetentionPolicy.SOURCE)\n"+
//			"@Target([ElementType.METHOD])\n"+
//			"@GroovyASTTransformationClass([\"examples.local.LoggingASTTransformation\"])\n"+
//			"public @interface WithLogging {\n"+
//			"}\n",
		},"Hello World\n" + 
		"Starting greetWithLogging\n" + 
		"Hello World\n" + 
		"Ending greetWithLogging\n" + 
		"done",
		null,
		true,
		null,
		options,
		null);
	}
	
	

	// Testcode based on article: http://www.infoq.com/articles/groovy-1.5-new
	// The groups of tests are loosely based on the article contents - but what is really exercised here is the accessibility of
	// the described constructs across the Java/Groovy divide.
	
	// Variable arguments
	public void testInvokingVarargs01_JtoG() {
		this.runConformTest(new String[] {
			"p/Run.java",
			"package p;\n"+
			"public class Run {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    X x = new X();\n"+
			"    x.callit();\n"+
			"    x.callit(1);\n"+
			"    x.callit(1,2);\n"+
			"    x.callit2();\n"+
			"    x.callit2(1);\n"+
			"    x.callit2(1,2);\n"+
			"    x.callit3();\n"+
			"    x.callit3(\"abc\");\n"+
			"    x.callit3(\"abc\",\"abc\");\n"+
			"  }\n"+
			"}\n",

			"p/X.groovy",
			"package p;\n" + 
			"public class X {\n" + 
			"  public void callit(int... is) { print is.length; }\n"+
			"  public void callit2(Integer... is) { print is.length; }\n"+
			"  public void callit3(String... ss) { print ss.length; }\n"+
			"}\n",
		},"012012012");		
	}

	public void testInvokingVarargs01_GtoJ() {
		this.runConformTest(new String[] {
			"p/Run.groovy",
			"package p;\n"+
			"public class Run {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    X x = new X();\n"+
			"    x.callit('abc');\n"+
			"    x.callit('abc',1);\n"+
			"    x.callit('abc',1,2);\n"+
			"    x.callit2(3);\n"+
			"    x.callit2(4,1);\n"+
			"    x.callit2(1,1,2);\n"+
			"    x.callit3('abc');\n"+
			"    x.callit3('abc',\"abc\");\n"+
			"    x.callit3('abc',\"abc\",\"abc\");\n"+
			"  }\n"+
			"}\n",

			"p/X.java",
			"package p;\n" + 
			"public class X {\n" + 
			"  public void callit(String a, int... is) { System.out.print(is.length); }\n"+
			"  public void callit2(int a, Integer... is) { System.out.print(is.length); }\n"+
			"  public void callit3(String s, String... ss) { System.out.print(ss.length); }\n"+
			"}\n",
		},"012012012");		
	}

	// In these two cases the methods also take other values
	public void testInvokingVarargs02_JtoG() {
		this.runConformTest(new String[] {
			"p/Run.java",
			"package p;\n"+
			"public class Run {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    X x = new X();\n"+
			"    x.callit(\"abc\");\n"+
			"    x.callit(\"abc\",1);\n"+
			"    x.callit(\"abc\",1,2);\n"+
			"    x.callit2(3);\n"+
			"    x.callit2(4,1);\n"+
			"    x.callit2(1,1,2);\n"+
			"    x.callit3(\"abc\");\n"+
			"    x.callit3(\"abc\",\"abc\");\n"+
			"    x.callit3(\"abc\",\"abc\",\"abc\");\n"+
			"  }\n"+
			"}\n",

			"p/X.groovy",
			"package p;\n" + 
			"public class X {\n" + 
			"  public void callit(String a, int... is) { print is.length; }\n"+
			"  public void callit2(int a, Integer... is) { print is.length; }\n"+
			"  public void callit3(String s, String... ss) { print ss.length; }\n"+
			"}\n",
		},"012012012");		
	}

	// Groovy doesn't care about '...' and will consider [] as varargs
	public void testInvokingVarargs03_JtoG() {
		this.runConformTest(new String[] {
			"p/Run.java",
			"package p;\n"+
			"public class Run {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    X x = new X();\n"+
			"    x.callit(\"abc\");\n"+
			"    x.callit(\"abc\",1);\n"+
			"    x.callit(\"abc\",1,2);\n"+
			"    x.callit2(3);\n"+
			"    x.callit2(4,1);\n"+
			"    x.callit2(1,1,2);\n"+
			"    x.callit3(\"abc\");\n"+
			"    x.callit3(\"abc\",\"abc\");\n"+
			"    x.callit3(\"abc\",\"abc\",\"abc\");\n"+
			"  }\n"+
			"}\n",

			"p/X.groovy",
			"package p;\n" + 
			"public class X {\n" + 
			"  public void callit(String a, int[] is) { print is.length; }\n"+
			"  public void callit2(int a, Integer[] is) { print is.length; }\n"+
			"  public void callit3(String s, String[] ss) { print ss.length; }\n"+
			"}\n",
		},"012012012");		
	}

	
	public void testInvokingVarargs02_GtoJ() {
		this.runConformTest(new String[] {
			"p/Run.groovy",
			"package p;\n"+
			"public class Run {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    X x = new X();\n"+
			"    x.callit();\n"+
			"    x.callit(1);\n"+
			"    x.callit(1,2);\n"+
			"    x.callit2();\n"+
			"    x.callit2(1);\n"+
			"    x.callit2(1,2);\n"+
			"    x.callit3();\n"+
			"    x.callit3(\"abc\");\n"+
			"    x.callit3(\"abc\",\"abc\");\n"+
			"  }\n"+
			"}\n",

			"p/X.java",
			"package p;\n" + 
			"public class X {\n" + 
			"  public void callit(int... is) { System.out.print(is.length); }\n"+
			"  public void callit2(Integer... is) { System.out.print(is.length); }\n"+
			"  public void callit3(String... ss) { System.out.print(ss.length); }\n"+
			"}\n",
		},"012012012");		
	}

	public void testInvokingVarargsCtors01_JtoG() {
		this.runConformTest(new String[] {
			"p/Run.java",
			"package p;\n"+
			"public class Run {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    X x = null;\n"+
			"    x = new X();\n"+
			"    x = new X(1);\n"+
			"    x = new X(1,2);\n"+
			"    x = new X(\"abc\");\n"+
			"    x = new X(\"abc\",1);\n"+
			"    x = new X(\"abc\",1,2);\n"+
			"  }\n"+
			"}\n",

			"p/X.groovy",
			"package p;\n" + 
			"public class X {\n" + 
			"  public X(int... is) { print is.length; }\n"+
			"  public X(String s, int... is) { print is.length; }\n"+
			"}\n",
		},"012012");		
	}
	
	public void testInvokingVarargsCtors01_GtoJ() {
		this.runConformTest(new String[] {
			"p/Run.groovy",
			"package p;\n"+
			"public class Run {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    X x = null;\n"+
			"    x = new X();\n"+
			"    x = new X(1);\n"+
			"    x = new X(1,2);\n"+
			"    x = new X(\"abc\");\n"+
			"    x = new X(\"abc\",1);\n"+
			"    x = new X(\"abc\",1,2);\n"+
			"  }\n"+
			"}\n",

			"p/X.java",
			"package p;\n" + 
			"public class X {\n" + 
			"  public X(int... is) { System.out.print(is.length); }\n"+
			"  public X(String s, int... is) { System.out.print(is.length); }\n"+
			"}\n",
		},"012012");		
	}

	public void testPositions() {
		this.runNegativeTest(new String[] {
			"One.groovy",
			"class One {\n" + 
			"		/*a*/			Stack plates;\n"+
			"  /*b*/ Stack plates2;\n"+
			"}\n",
		},"----------\n" + 
		"1. WARNING in One.groovy (at line 2)\n" + 
		"	/*a*/			Stack plates;\n" + 
		"	     			^^^^^\n" + 
		"Stack is a raw type. References to generic type Stack<E> should be parameterized\n" + 
		"----------\n" + 
		"2. WARNING in One.groovy (at line 3)\n" + 
		"	/*b*/ Stack plates2;\n" + 
		"	      ^^^^^\n" + 
		"Stack is a raw type. References to generic type Stack<E> should be parameterized\n" + 
		"----------\n");		

	}
	
	public void testPositions_2() {
		this.runNegativeTest(new String[] {
			"One.groovy",
			"class One {\r\n" + 
			"		/*a*/			Stack plates;\r\n"+
			"  /*b*/ Stack plates2;\n"+
			"}\n",
		},"----------\n" + 
		"1. WARNING in One.groovy (at line 2)\n" + 
		"	/*a*/			Stack plates;\n" + 
		"	     			^^^^^\n" + 
		"Stack is a raw type. References to generic type Stack<E> should be parameterized\n" + 
		"----------\n" + 
		"2. WARNING in One.groovy (at line 3)\n" + 
		"	/*b*/ Stack plates2;\n" + 
		"	      ^^^^^\n" + 
		"Stack is a raw type. References to generic type Stack<E> should be parameterized\n" + 
		"----------\n");		
	}
	
	// FIXASC test varargs with default parameter values (methods/ctors)

	// static imports
	public void testStaticImports_JtoG() {
		this.runConformTest(new String[] {
			"p/Run.java",
			"package p;\n"+
			"import static p.q.r.Colour.*;\n"+
			"public class Run {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    System.out.print(Red);\n"+
			"    System.out.print(Green);\n"+
			"    System.out.print(Blue);\n"+
			"  }\n"+
			"}\n",

			"p/q/r/Colour.groovy",
			"package p.q.r;\n" + 
			"enum Colour { Red,Green,Blue; }\n",
		},"RedGreenBlue");		
	}
	
	public void testStaticImports_GtoJ() {
		this.runConformTest(new String[] {
			"p/Run.groovy",
			"package p;\n"+
			"import static p.q.r.Colour.*;\n"+
			"public class Run {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    System.out.print(Red);\n"+
			"    System.out.print(Green);\n"+
			"    System.out.print(Blue);\n"+
			"  }\n"+
			"}\n",

			"p/q/r/Colour.java",
			"package p.q.r;\n" + 
			"enum Colour { Red,Green,Blue; }\n",
		},"RedGreenBlue");		
	}
	
	public void testJDTClassNode_633() {
		try {
			JDTResolver.recordInstances = true;
			this.runConformTest(new String[] {
				"p/Run.groovy",
				"package p;\n"+
				"import static p.q.r.Colour.*;\n"+
				"import p.q.r.Colour2;\n"+
				"public class Run {\n" + 
				"  public static void main(String[] argv) {\n"+
				"    System.out.print(Red);\n"+
				"    System.out.print(Green);\n"+
				"    System.out.print(Blue);\n"+
				"   Colour2 c2 = new Colour2();\n"+
				"   int i = c2.compareTo('abc');\n"+
				"  }\n"+
				"}\n",
	
				"p/q/r/Colour.java",
				"package p.q.r;\n" + 
				"enum Colour { Red,Green,Blue; }\n",
	
				"p/q/r/Colour2.java",
				"package p.q.r;\n" + 
				"public class Colour2 implements Comparable<String> { \n"+
				"  public int compareTo(String s) { return 0; } \n"+
					"}\n",
				},"RedGreenBlue");		 
			
			// Check on the state of Comparable		
			JDTClassNode classnode = ((JDTResolver)JDTResolver.instances.get(0)).getCachedNode("java.lang.Comparable<T>");
			assertNotNull(classnode);
			// Should have one method
			List methods = classnode.getMethods();
			assertEquals(1,methods.size());
			assertEquals("int compareTo(java.lang.Object)",((MethodNode)methods.get(0)).getTypeDescriptor());
		} finally {
			JDTResolver.instances.clear();
			JDTResolver.recordInstances=false;
		}
	}
	
	public void testStaticImports2_GtoJ() {
		this.runConformTest(new String[] {
			"p/Run.java",
			"package p;\n"+
			"import static p.q.r.Colour.*;\n"+
			"public class Run {\n" + 
			"  public static void main(String[] argv) {\n"+
			"    Red.printme();\n"+
			"  }\n"+
			"}\n",

			"p/q/r/Colour.groovy",
			"package p.q.r;\n" + 
			"enum Colour { Red,Green,Blue; \n" +
			"  void printme() {\n"+
			"    println \"${name()}\";\n" +
         	"  }\n"+
         	"}\n",
		},"Red");		
	}
	
	public void testStaticImportsAliasing_G() {
		this.runConformTest(new String[] {
			"p/Run.groovy",
			"package p;\n"+
			"import static java.lang.Math.PI\n"+
			"import static java.lang.Math.sin as sine\n"+
			"import static java.lang.Math.cos as cosine\n"+
			"\n"+
			" print sine(PI / 6) + cosine(PI / 3)"
		},"1.0");		
	}
	

	public void testSecondaryTypeTagging() {
		this.runConformTest(new String[] {
			"Run.groovy",
			"class Run { public static void main(String[]argv) {print '1.0';} }\n"+
			"class B {}\n"+
			"class C {}\n"+
			"class D {}\n"
		},"1.0");		
		GroovyCompilationUnitDeclaration gcud = getCUDeclFor("Run.groovy");
		TypeDeclaration[] tds = gcud.types;
		assertFalse((tds[0].bits&ASTNode.IsSecondaryType)!=0);
		assertTrue((tds[1].bits&ASTNode.IsSecondaryType)!=0);
		assertTrue((tds[2].bits&ASTNode.IsSecondaryType)!=0);
		assertTrue((tds[3].bits&ASTNode.IsSecondaryType)!=0);
		
		this.runConformTest(new String[] {
				"Run2.groovy",
				"class B {}\n"+
				"class Run2 { public static void main(String[]argv) {print '1.0';} }\n"+
				"class C {}\n"+
				"class D {}\n"
			},"1.0");		
			gcud = getCUDeclFor("Run2.groovy");
			tds = gcud.types;
			assertTrue((tds[0].bits&ASTNode.IsSecondaryType)!=0);
			assertFalse((tds[1].bits&ASTNode.IsSecondaryType)!=0);
			assertTrue((tds[2].bits&ASTNode.IsSecondaryType)!=0);
			assertTrue((tds[3].bits&ASTNode.IsSecondaryType)!=0);
	}
	
	// Test 'import static a.B.FOO'
	public void testImportStatic1() {
		this.runConformTest(new String[] {
			"b/Run.groovy",
			"package b\n"+
			"import static a.B.FOO\n"+
			"class Run { public static void main(String[]argv) { print FOO;} }\n",
			"a/B.groovy",
			"package a\n"+
			"class B { public static String FOO='abc';}\n",
		},"abc");		
		GroovyCompilationUnitDeclaration gcud = getCUDeclFor("Run.groovy");
		ImportReference[] irs = gcud.imports;
		assertEquals("a.B.FOO",irs[0].toString().trim());
		assertTrue(irs[0].isStatic());
	}
	
	// Test 'import static a.B.*'
	public void testImportStatic2() {
		this.runConformTest(new String[] {
			"b/Run.groovy",
			"package b\n"+
			"import static a.B.*\n"+
			"class Run { public static void main(String[]argv) { print FOO;} }\n",
			"a/B.groovy",
			"package a\n"+
			"class B { public static String FOO='abc';}\n",
		},"abc");		
		GroovyCompilationUnitDeclaration gcud = getCUDeclFor("Run.groovy");
		ImportReference[] irs = gcud.imports;
		assertEquals("a.B.*",irs[0].toString().trim());
		assertTrue(irs[0].isStatic());
	}
	
	// Test 'import static a.B.FOO as Wibble'
	public void testImportStatic3() {
		this.runConformTest(new String[] {
			"b/Run.groovy",
			"package b\n"+
			"import static a.B.FOO as Wibble\n"+
			"class Run { public static void main(String[]argv) { print Wibble;} }\n",
			"a/B.groovy",
			"package a\n"+
			"class B { public static String FOO='abc';}\n",
		},"abc");		
		GroovyCompilationUnitDeclaration gcud = getCUDeclFor("Run.groovy");
		ImportReference[] irs = gcud.imports;
		assertTrue(irs[0] instanceof AliasImportReference);
		assertEquals("a.B.Wibble",irs[0].toString().trim()); // FIXASC hmmm, why isn't that a.B.FOO (ie. declared long name)
		assertTrue(irs[0].isStatic());
		assertEquals("Wibble",new String(((AliasImportReference)irs[0]).getSimpleName()));
	}
	
	// Parser should correctly parse this code, but 
	// should return with an error
	public void testSafeDereferencingParserRecovery() {
	    this.runNegativeTest(new String[] {
	            "Run.groovy",
	            "public class SomeClass {\n" +
	            "  int someProperty\n" +
	            "  void someMethod() {\n" +
	            "    someProperty?.\n" +
	            "  }\n" +
	            "}"
	    }, 
	    "----------\n" + 
		"1. ERROR in Run.groovy (at line 5)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Groovy:unexpected token: } @ line 5, column 3.\n" + 
		"----------\n"); 

		String expectedOutput = 
			"public class SomeClass {\n" + 
			"  private int someProperty;\n" + 
			"  public SomeClass() {\n" + 
			"  }\n" + 
			"  public void someMethod() {\n" + 
			"  }\n" + 
			"}\n";
		checkGCUDeclaration("Run.groovy",expectedOutput);
	}


    
    public void testParsingBlankImport_538() throws Exception {
    	if (GroovyUtils.GROOVY_LEVEL<18) {
    		return;
    	}
        this.runNegativeTest(new String[]{
                "A.groovy", 
                "import " 
        },
        "----------\n" + 
		"1. ERROR in A.groovy (at line 1)\n" + 
		"	import \n" + 
		"	 ^\n" + 
		"Groovy:Invalid import specification @ line 1, column 1.\n" + 
		"----------\n");
        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());
        
        List imports = mn.getImports();
        ImportNode brokenImportNode =(ImportNode) imports.get(0);
        assertEquals(2,brokenImportNode.getStart());
        assertEquals(0,brokenImportNode.getEnd());
        assertEquals("java.lang.Object",brokenImportNode.getType().getName());
        
        ClassNode cn = (ClassNode)mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }
    
    public void testParsingDotTerminatedImport_538() throws Exception {    	
    	if (GroovyUtils.GROOVY_LEVEL<18) {
			return;
		}
        this.runNegativeTest(new String[]{
                "A.groovy", 
                "import foo." 
        },
        "----------\n" + 
		"1. ERROR in A.groovy (at line 1)\n" + 
		"	import foo.\n" + 
		"	        ^\n" + 
		"Groovy:Invalid import  @ line 1, column 8.\n" + 
		"----------\n");
        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List imports = mn.getStarImports();
        ImportNode brokenImportNode =(ImportNode) imports.get(0);
        assertEquals("foo.",brokenImportNode.getPackageName());
        
        ClassNode cn = (ClassNode)mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }
    
    public void testParsingBlankImportStatic_538() throws Exception {
    	if (GroovyUtils.GROOVY_LEVEL<18) {
			return;
		}

    	this.runNegativeTest(new String[]{
                "A.groovy", 
                "import static \n" 
        },
        "----------\n" + 
		"1. ERROR in A.groovy (at line 1)\n" + 
		"	import static \n" + 
		"	 ^\n" + 
		"Groovy:Invalid import static specification @ line 1, column 1.\n" + 
		"----------\n");
        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());
        
        Map imports = mn.getStaticImports();
        ImportNode brokenImportNode =(ImportNode)imports.get("");
        assertEquals("java.lang.Object",brokenImportNode.getType().getName());
        
        ClassNode cn = (ClassNode)mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }
    
    public void testParsingDotTerminatedImportStatic_538() throws Exception {
    	if (GroovyUtils.GROOVY_LEVEL<18) {
			return;
		}

    	this.runNegativeTest(new String[]{
                "A.groovy", 
                "import static foo.Bar." 
        },
        "----------\n" + 
		"1. ERROR in A.groovy (at line 1)\n" + 
		"	import static foo.Bar.\n" + 
		"	 ^\n" + 
		"Groovy:unable to resolve class foo.Bar\n" + 
		"----------\n" + 
		"2. ERROR in A.groovy (at line 1)\n" + 
		"	import static foo.Bar.\n" + 
		"	               ^\n" + 
		"Groovy:Invalid import  @ line 1, column 15.\n" + 
		"----------\n");
        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        Map imports = mn.getStaticStarImports();
        ImportNode brokenImportNode =(ImportNode) imports.get("foo.Bar");
        assertEquals("foo.Bar",brokenImportNode.getType().getName());
        
        ClassNode cn = (ClassNode)mn.getClasses().get(0);
        assertNotNull(cn);
        assertEquals("A",cn.getName());
    }
    
    public void testParsingDotTerminatedImportFollowedByClassDeclaration_538() throws Exception {
    	if (GroovyUtils.GROOVY_LEVEL<18) {
			return;
		}
    	this.runNegativeTest(new String[]{
                "A.groovy", 
                "import foo.\n"+
                "\n"+
                "class Wibble {}\n"
        },
        "----------\n" + 
		"1. ERROR in A.groovy (at line 1)\n" + 
		"	import foo.\n" + 
		"	        ^\n" + 
		"Groovy:Invalid import  @ line 1, column 8.\n" + 
		"----------\n");
        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List imports = mn.getStarImports();
        ImportNode brokenImportNode =(ImportNode) imports.get(0);
        assertEquals("foo.",brokenImportNode.getPackageName());
        
        ClassNode cn = (ClassNode)mn.getClasses().get(0);
        assertNotNull(cn);
        assertEquals("Wibble",cn.getName());
    }
    
    public void testParsingDotTerminatedImportFollowedByModifierAndClassDeclaration_538() throws Exception {
    	if (GroovyUtils.GROOVY_LEVEL<18) {
			return;
		}

    	this.runNegativeTest(new String[]{
                "A.groovy", 
                "import foo.\n"+
                "\n"+
                "public class Wibble {}\n"
        },
        "----------\n" + 
		"1. ERROR in A.groovy (at line 1)\n" + 
		"	import foo.\n" + 
		"	        ^\n" + 
		"Groovy:Invalid import  @ line 1, column 8.\n" + 
		"----------\n");
        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List imports = mn.getStarImports();
        ImportNode brokenImportNode =(ImportNode) imports.get(0);
        assertEquals("foo.",brokenImportNode.getPackageName());
        
        ClassNode cn = (ClassNode)mn.getClasses().get(0);
        assertNotNull(cn);
        assertEquals("Wibble",cn.getName());
    }
    
    public void testParsingBlankImportFollowedByClassDeclaration_538() throws Exception {
    	if (GroovyUtils.GROOVY_LEVEL<18) {
			return;
		}

    	this.runNegativeTest(new String[]{
                "A.groovy", 
                "import\n"+
                "\n"+
                "public class Wibble {}\n"
        },
        "----------\n" + 
		"1. ERROR in A.groovy (at line 1)\n" + 
		"	import\n" + 
		"	 ^\n" + 
		"Groovy:Invalid import specification @ line 1, column 1.\n" + 
		"----------\n");
        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List imports = mn.getImports();
        ImportNode brokenImportNode =(ImportNode) imports.get(0);
        assertEquals("java.lang.Object",brokenImportNode.getType().getName());
        
        ClassNode cn = (ClassNode)mn.getClasses().get(0);
        assertNotNull(cn);
        assertEquals("Wibble",cn.getName());
    }
    
    public void testParsingIncompleteClassDeclaration_495() throws Exception {
    	if (GroovyUtils.GROOVY_LEVEL<18) {
			return;
		}

    	this.runNegativeTest(new String[]{
                "A.groovy", 
                "class Bar {}\n"+
                "class FooTest extends Bar { }\n" + 
                "class BBB extends FooTes" 
        },
        "----------\n" + 
		"1. ERROR in A.groovy (at line 3)\n" + 
		"	class BBB extends FooTes\n" + 
		"	^\n" + 
		"Groovy:unable to resolve class FooTes \n" + 
		"----------\n" + 
		"2. ERROR in A.groovy (at line 3)\n" + 
		"	class BBB extends FooTes\n" + 
		"	                       ^\n" + 
		"Groovy:Malformed class declaration @ line 3, column 24.\n" + 
		"----------\n");
        // missing end curly, but that shouldn't cause us to discard what we successfully parsed
        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        List l = mn.getClasses();
        for (int i=0;i<l.size();i++) {
        	System.out.println(l.get(i));
        }
        assertFalse(mn.encounteredUnrecoverableError());
        ClassNode cn = (ClassNode)mn.getClasses().get(2);
        assertNotNull(cn);
        assertEquals("FooTest",cn.getName());
        cn = (ClassNode)mn.getClasses().get(1);
        assertNotNull(cn);
        assertEquals("BBB",cn.getName());
    }


	// FIXASC what does this actually mean to groovy?  from GrailsPluginUtils
//  static Resource[] getPluginXmlMetadata(String pluginsDirPath) {
//      return getPluginXmlMetadata(pluginsDirPath, DEFAULT_RESOURCE_RESOLVER)
//  }
//  static synchronized Resource[] getPluginXmlMetadata( String pluginsDirPath,
//                                          Closure resourceResolver = DEFAULT_RESOURCE_RESOLVER) {


//	// FIXASC testcase for this. infinite loops (B extends B<String>
//	public void testInfiniteLoop() {
//		this.runConformTest(new String[] {
//		"p/B.groovy",
//		"package p;\n" + 
//		"class B extends B<String> {\n" + 
//		"  public static void main(String[] argv) {\n"+
//		"    new B();\n"+
//		"    print \"success\"\n"+
//		"  }\n"+
//		"}\n",				
////	
////		"p/A.java",
////		"package p;\n" + 
////		"public class A<T> {}\n", 
//		},
//		"");
//	}

	private void checkDisassemblyFor(String filename, String expectedOutput) {
		checkDisassemblyFor(filename, expectedOutput, ClassFileBytesDisassembler.DETAILED);
	}
    /**
     * Check the disassembly of a .class file for a particular piece of text
     */
	private void checkDisassemblyFor(String filename, String expectedOutput, int detail) {
		try {
			File f = new File(OUTPUT_DIR + File.separator + filename);
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String result = disassembler.disassemble(classFileBytes, "\n", detail);
			int index = result.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 3));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	private void checkGCUDeclaration(String filename, String expectedOutput) {
		GroovyCompilationUnitDeclaration decl = (GroovyCompilationUnitDeclaration)((DebugRequestor)GroovyParser.debugRequestor).declarations.get(filename);
		String declarationContents = decl.print();
		if (expectedOutput==null || expectedOutput.length()==0) {
			System.out.println(Util.displayString(declarationContents,2));
		} else {
			int foundIndex = declarationContents.indexOf(expectedOutput);
			if (foundIndex==-1) {
				fail("Did not find expected output:\n"+expectedOutput+"\nin actual output:\n"+declarationContents);
			}
		}
	}
	
	/* for import recovery tests, if they get activated */
	private ModuleNode getModuleNode(String filename) {
		GroovyCompilationUnitDeclaration decl = (GroovyCompilationUnitDeclaration)((DebugRequestor)GroovyParser.debugRequestor).declarations.get(filename);
		if (decl!=null) {
			return decl.getModuleNode();
		} else {
			return null;
		}
	}
	
	private GroovyCompilationUnitDeclaration getCUDeclFor(String filename) {
		return (GroovyCompilationUnitDeclaration)((DebugRequestor)GroovyParser.debugRequestor).declarations.get(filename);
	}

	static class DebugRequestor implements IGroovyDebugRequestor {

		Map declarations;
		Map types;
		
		public DebugRequestor() {
			declarations = new HashMap();
		}

		public void acceptCompilationUnitDeclaration(GroovyCompilationUnitDeclaration gcuDeclaration) {
			System.out.println(gcuDeclaration);
			String filename = new String(gcuDeclaration.getFileName());
			filename=filename.substring(filename.lastIndexOf(File.separator)+1); // Filename now being just X.groovy or Foo.java
			declarations.put(filename,gcuDeclaration);
		}
		
	}
	

	private String stringify(TypeReference type) {
		StringBuffer sb = new StringBuffer();
		stringify(type,sb);
		return sb.toString();		
	}
	private void stringify(TypeReference type, StringBuffer sb) {		
		if (type.getClass()==ParameterizedSingleTypeReference.class) {
			ParameterizedSingleTypeReference pstr = (ParameterizedSingleTypeReference)type;
			sb.append("("+pstr.sourceStart+">"+pstr.sourceEnd+")").append(pstr.token);
			TypeReference[] typeArgs = pstr.typeArguments;
			sb.append("<");
			for (int t=0;t<typeArgs.length;t++) {
				stringify(typeArgs[t],sb);
			}
			sb.append(">");
		} else if (type.getClass()==ParameterizedQualifiedTypeReference.class) {
			ParameterizedQualifiedTypeReference pqtr = (ParameterizedQualifiedTypeReference)type;
			sb.append("("+type.sourceStart+">"+type.sourceEnd+")");
			long[] positions = pqtr.sourcePositions;
			TypeReference[][] allTypeArgs = pqtr.typeArguments;
			for (int i=0;i<pqtr.tokens.length;i++) {
				if (i>0) {
					sb.append('.');
				}
				sb.append("("+(int)(positions[i]>>>32)+">"+(int)(positions[i]&0x00000000FFFFFFFFL)+")").append(pqtr.tokens[i]);
				if (allTypeArgs[i]!=null) {
					sb.append("<");
					for (int t=0;t<allTypeArgs[i].length;t++) {
						stringify(allTypeArgs[i][t],sb);
					}
					sb.append(">");
				}
			}
			
		} else if (type.getClass()==ArrayTypeReference.class) {
			ArrayTypeReference atr = (ArrayTypeReference)type;
			// for a reference 'String[]' sourceStart='S' sourceEnd=']' originalSourceEnd='g'
			sb.append("("+atr.sourceStart+">"+atr.sourceEnd+" ose:"+atr.originalSourceEnd+")").append(atr.token);
			for (int d=0;d<atr.dimensions;d++) {
				sb.append("[]");
			}			
		} else if (type.getClass()==Wildcard.class) {
			Wildcard w = (Wildcard)type;
			if (w.kind== Wildcard.UNBOUND) {
				sb.append("("+type.sourceStart+">"+type.sourceEnd+")").append('?');
			} else if (w.kind==Wildcard.SUPER) {
				sb.append("("+type.sourceStart+">"+type.sourceEnd+")").append("? super ");
				stringify(w.bound,sb);
			} else if (w.kind==Wildcard.EXTENDS) {
				sb.append("("+type.sourceStart+">"+type.sourceEnd+")").append("? extends ");
				stringify(w.bound,sb);
			}
		} else if (type.getClass()== SingleTypeReference.class) {
			sb.append("("+type.sourceStart+">"+type.sourceEnd+")").append(((SingleTypeReference)type).token);
		} else if (type instanceof ArrayQualifiedTypeReference) {
			ArrayQualifiedTypeReference aqtr = (ArrayQualifiedTypeReference)type;
			sb.append("("+type.sourceStart+">"+type.sourceEnd+")");
			long[] positions = aqtr.sourcePositions;
			for (int i=0;i<aqtr.tokens.length;i++) {
				if (i>0) {
					sb.append('.');
				}
				sb.append("("+(int)(positions[i]>>>32)+">"+(int)(positions[i]&0x00000000FFFFFFFFL)+")").append(aqtr.tokens[i]);
			}
			for (int i=0;i<aqtr.dimensions();i++) { sb.append("[]"); }
		} else if (type.getClass()== QualifiedTypeReference.class) {
			QualifiedTypeReference qtr = (QualifiedTypeReference)type;
			sb.append("("+type.sourceStart+">"+type.sourceEnd+")");
			long[] positions = qtr.sourcePositions;
			for (int i=0;i<qtr.tokens.length;i++) {
				if (i>0) {
					sb.append('.');
				}
				sb.append("("+(int)(positions[i]>>>32)+">"+(int)(positions[i]&0x00000000FFFFFFFFL)+")").append(qtr.tokens[i]);
			}
		} else {
			throw new RuntimeException("Dont know how to print "+type.getClass());
		}
	}


	private FieldDeclaration grabField(GroovyCompilationUnitDeclaration decl, String fieldname) {
		FieldDeclaration[] fDecls = decl.types[0].fields;
		for (int i=0;i<fDecls.length;i++) { 
			if (new String(fDecls[i].name).equals(fieldname)) { 
				return fDecls[i];
			}
		}
		return null;
	}

	private void assertEventCount(int expectedCount, EventListener listener) {
		if (listener.eventCount()!=expectedCount) {
			fail("Expected "+expectedCount+" events but found "+listener.eventCount()+"\nEvents:\n"+listener.toString());
		}
	}

	private void assertEvent(String eventText, EventListener listener) {
		boolean found = false;
		Iterator eventIter = listener.getEvents().iterator();
		while (eventIter.hasNext()) {
			String s = (String) eventIter.next();
			if (s.equals(eventText)) {
				found=true;
				break;
			}
		}
		if (!found) {
			fail("Expected event '"+eventText+"'\nEvents:\n"+listener.toString());
		}
	}

}