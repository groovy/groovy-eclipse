/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.junit.test;

import org.codehaus.groovy.eclipse.ui.GroovyResourcePropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;


/**
 * @author Andrew Eisenberg
 * @created Jun 3, 2009
 *
 * This class is no longer needed.  JUnit plugin has been removed
 *
 */
public class GroovyResourcePropertyTests extends JUnitTestCase {

    public GroovyResourcePropertyTests() {
        super("Resource Property Tests");
    }
    
    public void testNothing() {
        System.out.println("There are no more tests in this test class.");
    }
    
//    public void testPropertyTesterJava() throws Exception {
//        IPath projectPath = createGenericProject();
//
//        IPath root = projectPath.append("src");
//        
//        env.addClass(root, "p2", "Hello",
//            "package p2;\n" +
//            "import junit.framework.TestCase;\n"+
//            "public class Hello extends TestCase {\n"+
//            "   public static void testNothing() throws Exception { }\n" +
//            "}\n"
//            );
//            
//        incrementalBuild(projectPath);
//        expectingNoProblems();
//        
//        IFile helloFile = getFile(projectPath, "src/p2/Hello.java");
//        
//        GroovyResourcePropertyTester tester = new GroovyResourcePropertyTester();
//        boolean result = tester.test(helloFile, GroovyResourcePropertyTester.isJUnitTest, null, null);
//        assertTrue("Should have found test suite in " + helloFile, result);
//    }
//
//    public void testPropertyTesterJavaFalse() throws Exception {
//        IPath projectPath = createGenericProject();
//        IPath root = projectPath.append("src");
//
//        env.addClass(root, "p2", "Hello",
//            "package p2;\n" +
//            "public class Hello {\n"+
//            "   public static void testNothing() throws Exception { }\n" +
//            "}\n"
//            );
//            
//        incrementalBuild(projectPath);
//        expectingNoProblems();
//        
//        IFile helloFile = getFile(projectPath, "src/p2/Hello.java");
//        
//        GroovyResourcePropertyTester tester = new GroovyResourcePropertyTester();
//        boolean result = tester.test(helloFile, GroovyResourcePropertyTester.isJUnitTest, null, null);
//        assertFalse("Should not have found test suite in " + helloFile, result);
//    }
//    
//    public void testPropertyTesterGroovy() throws Exception {
//        IPath projectPath = createGenericProject();
//
//        IPath root = projectPath.append("src");
//        
//        env.addGroovyClass(root, "p2", "Hello",
//            "package p2;\n" +
//            "import junit.framework.TestCase;\n"+
//            "public class Hello extends TestCase {\n"+
//            "   public static void testNothing() throws Exception { }\n" +
//            "}\n"
//            );
//            
//        incrementalBuild(projectPath);
//        expectingNoProblems();
//        
//        IFile helloFile = getFile(projectPath, "src/p2/Hello.groovy");
//        
//        GroovyResourcePropertyTester tester = new GroovyResourcePropertyTester();
//        boolean result = tester.test(helloFile, GroovyResourcePropertyTester.isJUnitTest, null, null);
//        assertTrue("Should have found test suite in " + helloFile, result);
//    }
//
//    public void testPropertyTesterGroovyFalse() throws Exception {
//        IPath projectPath = createGenericProject();
//        IPath root = projectPath.append("src");
//
//        env.addGroovyClass(root, "p2", "Hello",
//            "package p2;\n" +
//            "public class Hello {\n"+
//            "   public static void testNothing() throws Exception { }\n" +
//            "}\n"
//            );
//            
//        incrementalBuild(projectPath);
//        expectingNoProblems();
//        
//        IFile helloFile = getFile(projectPath, "src/p2/Hello.groovy");
//        
//        GroovyResourcePropertyTester tester = new GroovyResourcePropertyTester();
//        boolean result = tester.test(helloFile, GroovyResourcePropertyTester.isJUnitTest, null, null);
//        assertFalse("Should not have found test suite in " + helloFile, result);
//    }
//
//    public void testPropertyTesterJavaJU4() throws Exception {
//        IPath projectPath = createGenericProject();
//    
//        IPath root = projectPath.append("src");
//        
//        env.addClass(root, "p2", "Hello",
//            "package p2;\n" +
//            "import org.junit.Test;\n"+
//            "public class Hello {\n"+
//            "   public static @Test void nothing() throws Exception { }\n" +
//            "}\n"
//            );
//            
//        incrementalBuild(projectPath);
//        expectingNoProblems();
//        
//        IFile helloFile = getFile(projectPath, "src/p2/Hello.java");
//        
//        GroovyResourcePropertyTester tester = new GroovyResourcePropertyTester();
//        boolean result = tester.test(helloFile, GroovyResourcePropertyTester.isJUnitTest, null, null);
//        assertTrue("Should have found test suite in " + helloFile, result);
//    }
//
//    public void testPropertyTesterGroovyJU4() throws Exception {
//        IPath projectPath = createGenericProject();
//    
//        IPath root = projectPath.append("src");
//        
//        env.addGroovyClass(root, "p2", "Hello",
//            "package p2;\n" +
//            "import org.junit.Test;\n"+
//            "public class Hello {\n"+
//            "   public static @Test void nothing() throws Exception { }\n" +
//            "}\n"
//            );
//            
//        incrementalBuild(projectPath);
//        expectingNoProblems();
//        
//        IFile helloFile = getFile(projectPath, "src/p2/Hello.groovy");
//        
//        GroovyResourcePropertyTester tester = new GroovyResourcePropertyTester();
//        boolean result = tester.test(helloFile, GroovyResourcePropertyTester.isJUnitTest, null, null);
//        assertTrue("Should have found test suite in " + helloFile, result);
//    }
//    
//    public void testBug212() throws Exception {
//        IPath projectPath = createGenericProject();
//        
//        IPath root = projectPath.append("src");
//        env.addGroovyClass(root, "", "T2", "" +
//        		"import org.junit.Test\n"+
//        		"public class T2 {\n"+
//        		"@Test\n"+
//        		"def void t() {\n"+
//        		"return;\n"+
//                "}\n"+
//                "}\n"
//        );
//        incrementalBuild(projectPath);
//        expectingNoProblems();
//        
//        IFile t2File = getFile(projectPath, "src/T2.groovy");
//        
//        GroovyResourcePropertyTester tester = new GroovyResourcePropertyTester();
//        boolean result = tester.test(t2File, GroovyResourcePropertyTester.isJUnitTest, null, null);
//        assertTrue("Should have found test suite in " + t2File, result);
//    }
//
}
