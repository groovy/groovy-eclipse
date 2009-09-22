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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.junit.launcher.JUnit3TestFinder;


/**
 * @author Andrew Eisenberg
 * @created Jun 3, 2009
 * 
 */
public class JUnit3TestFinderTests extends JUnitTestCase {

    public JUnit3TestFinderTests() {
        super(JUnit3TestFinderTests.class.getName());
    }

    public void testFinderWithSuite() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addGroovyClass(root, "p2", "Hello",
                "package p2;\n" +
                "import junit.framework.Test;\n"+
                "public class Hello {\n"+
                "   public static Test suite() throws Exception { }\n" +
                "}\n"
                );
        
        incrementalBuild(projectPath);
        expectingNoProblems();
        
        IFile file = getFile(projectPath, "src/p2/Hello.groovy");
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("Hello");
        assertTrue("Groovy type Hello should exist.", type.exists());
        assertTrue("Groovy type Hello should be a test suite", 
                new JUnit3TestFinder().isTest(type));
    }
    public void testFinderOfSubclass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addGroovyClass(root, "p2", "Hello",
                "package p2;\n" +
                "public class Hello extends Tester {\n"+
                "}\n"
                );
        env.addGroovyClass(root, "p2", "Tester",
                "package p2;\n" +
                "import junit.framework.TestCase\n" +
                "abstract class Tester extends TestCase {\n"+
                "}\n"
                );
        
        incrementalBuild(projectPath);
        expectingNoProblems();
        
        IFile file = getFile(projectPath, "src/p2/Hello.groovy");
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("Hello");
        assertTrue("Groovy type Hello should exist.", type.exists());
        assertTrue("Groovy type Hello should be a test suite", 
                new JUnit3TestFinder().isTest(type));
        file = getFile(projectPath, "src/p2/Tester.groovy");
        unit = JavaCore.createCompilationUnitFrom(file);
        type = unit.getType("Tester");
        assertTrue("Groovy type Tester should exist.", type.exists());
        assertFalse("Groovy type Tester should not be a test suite (it is abstract)", 
                new JUnit3TestFinder().isTest(type));
    }
    public void testFinderOfNonPublicSubclass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addGroovyClass(root, "p2", "Hello",
                "package p2;\n" +
                "class Hello extends Tester {\n"+
                "}\n"
                );
        env.addGroovyClass(root, "p2", "Tester",
                "package p2;\n" +
                "import junit.framework.TestCase\n" +
                "abstract class Tester extends TestCase {\n"+
                "}\n"
                );
        
        incrementalBuild(projectPath);
        expectingNoProblems();
        
        IFile file = getFile(projectPath, "src/p2/Hello.groovy");
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
        IType type = unit.getType("Hello");
        assertTrue("Groovy type Hello should exist.", type.exists());
        assertTrue("Groovy type Hello should be a test suite (even though it is non-public)", 
                new JUnit3TestFinder().isTest(type));
        file = getFile(projectPath, "src/p2/Tester.groovy");
        unit = JavaCore.createCompilationUnitFrom(file);
        type = unit.getType("Tester");
        assertTrue("Groovy type Tester should exist.", type.exists());
        assertFalse("Groovy type Tester should not be a test suite (it is abstract)", 
                new JUnit3TestFinder().isTest(type));
    }
    
    public void testFindAllTestSuites() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addGroovyClass(root, "p2", "Hello",
                "package p2;\n" +
                "public class Hello extends Tester {\n"+
                "}\n"
                );
        env.addGroovyClass(root, "p2", "Tester",
                "package p2;\n" +
                "import junit.framework.TestCase\n" +
                "public class Tester extends TestCase {\n"+
                "}\n"
                );
        env.addGroovyClass(root, "p2", "Hello2",
                "package p2;\n" +
                "import junit.framework.Test;\n"+
                "public class Hello2 {\n"+
                "   public static Test suite() throws Exception { }\n" +
                "}\n"
                );
        env.addGroovyClass(root, "p2", "NotATest",
                "package p2;\n" +
                "import junit.framework.TestCase\n" +
                "abstract class NotATest extends TestCase {\n"+
                "}\n"
                );
        
        incrementalBuild(projectPath);
        expectingNoProblems();
        
        Set<IType> testTypes = new HashSet<IType>();
        IProject project = getProject(projectPath);
        
        new JUnit3TestFinder().findTestsInContainer(JavaCore.create(project), testTypes, new NullProgressMonitor());
        
        assertEquals("Should have found 3 test classes", 3, testTypes.size());
        boolean testerFound = false;
        boolean helloFound = false;
        boolean hello2Found = false;
        for (IType type : testTypes) {
            if (type.getElementName().equals("Hello")) helloFound = true;
            if (type.getElementName().equals("Hello2")) hello2Found = true;
            if (type.getElementName().equals("Tester")) testerFound = true;
        }
        assertTrue("Hello should be a test type", helloFound);
        assertTrue("Hello2 should be a test type", hello2Found);
        assertTrue("Tester should be a test type", testerFound);
    }
}
