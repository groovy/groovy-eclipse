/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;


import junit.framework.Test;

public class AnnotationTest_9 extends AbstractComparableTest {

    public AnnotationTest_9(String name) {
        super(name);
    }

    public static Test suite() {
        return buildMinimalComplianceTestSuite(testClass(), F_9);
    }

    public static Class<?> testClass() {
        return AnnotationTest_9.class;
    }

    public void testBug532913() throws Exception {
	    runConformTest(
	        new String[] {
	                "p/A.java",
	                "package p;\n" +
	                "@java.lang.annotation.Target({\n" + 
	                "    java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD})\n" + 
	                "@Deprecated\n" + 
	                "public @interface A {}\n",
	        },"");
	    runConformTest(
            new String[] {
                    "X.java",
                    "import p.A;\n" +
                    "class X {\n" + 
                    "  @A void foo() {}\n" + 
                    "}\n",
            },"", null, false, null);
	}
    public void testBug521054a() throws Exception {
    	this.runNegativeTest(
    		new String[] {
    				"X.java",
    				"public @interface X {\n" +
    				"	String value(X this);\n" +
    				"}\n",
    		},
    		"----------\n" + 
    		"1. ERROR in X.java (at line 2)\n" + 
    		"	String value(X this);\n" + 
    		"	       ^^^^^^^^^^^^^\n" + 
    		"Annotation attributes cannot have parameters\n" + 
    		"----------\n", 
    		null, true);
    }
    public void testBug521054b() throws Exception {
    	this.runNegativeTest(
    		new String[] {
    				"X.java",
    				"@java.lang.annotation.Repeatable(Container.class)\n" +
    				"public @interface X {\n" +
    				"	String value();\n" +
    				"}\n" +
    				"@interface Container {\n" +
    				"	X[] value(Container this);\n" +
    				"}\n",
    		},
    		"----------\n" + 
    		"1. ERROR in X.java (at line 6)\n" + 
    		"	X[] value(Container this);\n" + 
    		"	    ^^^^^^^^^^^^^^^^^^^^^\n" + 
    		"Annotation attributes cannot have parameters\n" + 
    		"----------\n", 
    		null, true);
    }
    public void testBug521054c() throws Exception {
    	this.runNegativeTest(
    		new String[] {
    				"X.java",
    				"@java.lang.annotation.Repeatable(Container.class)\n" +
    				"public @interface X {\n" +
    				"	String value(X this, int i);\n" +
    				"}\n" +
    				"@interface Container {\n" +
    				"	X[] value();\n" +
    				"}\n",
    		},
    		"----------\n" + 
    		"1. ERROR in X.java (at line 3)\n" + 
    		"	String value(X this, int i);\n" + 
    		"	       ^^^^^^^^^^^^^^^^^^^^\n" + 
    		"Annotation attributes cannot have parameters\n" + 
    		"----------\n", 
    		null, true);
    }
}
