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

package org.eclipse.jdt.core.groovy.tests.model;

import java.util.Arrays;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.internal.core.util.Util;



/**
 * @author Andrew Eisenberg
 * @created Jul 7, 2009
 * Tests for checking that groovy files are not sent
 * to compiler when project has no groovy nature
 */
public class GroovyContentTypeTests extends BuilderTests {
    public GroovyContentTypeTests(String name) {
        super(name);
    }
	public static Test suite() {
		return buildTestSuite(GroovyContentTypeTests.class);
	}
	
	public void testContentTypes() throws Exception {
	    Runnable runner = new Runnable() {
            
            public void run() {
                char[][] groovyLikeExtensions = ContentTypeUtils.getGroovyLikeExtensions();
                char[][] javaLikeExtensions = Util.getJavaLikeExtensions();
                char[][] javaButNotGroovyExtensions = ContentTypeUtils.getJavaButNotGroovyLikeExtensions();
                
                assertEquals("Invalid number of extensions found:\njavaLike: " + 
                        charCharToString(javaLikeExtensions) + "\ngroovyLike: " + 
                        charCharToString(groovyLikeExtensions) + "\njavaNotGroovyLike: " + 
                        charCharToString(javaButNotGroovyExtensions), javaLikeExtensions.length, 
                        groovyLikeExtensions.length + javaButNotGroovyExtensions.length);
                
                charCharContains(groovyLikeExtensions, "groovy");
                charCharContains(groovyLikeExtensions, "groovytest");

                charCharContains(javaButNotGroovyExtensions, "java");
                charCharContains(javaButNotGroovyExtensions, "javatest");
                
                charCharContains(javaLikeExtensions, "groovy");
                charCharContains(javaLikeExtensions, "groovytest");
                charCharContains(javaLikeExtensions, "java");
                charCharContains(javaLikeExtensions, "javatest");
                
                charCharNoContains(groovyLikeExtensions, "java");
                charCharNoContains(groovyLikeExtensions, "javatest");

                charCharNoContains(javaButNotGroovyExtensions, "groovy");
                charCharNoContains(javaButNotGroovyExtensions, "groovytest");
            }
        };
        runMultipleTimes(runner);
    }
	
	public void testJavaOnlyProject() throws Exception {
	    final IProject proj = createProject();
        Runnable runner = new Runnable() {
            public void run() {
                try {
                    env.removeGroovyNature("Project");
                    env.fullBuild();
                    
                    checkJavaProject(proj);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        runMultipleTimes(runner);
	}

	public void testGroovyProject() throws Exception {
	    final IProject proj = createProject();
        Runnable runner = new Runnable() {
            public void run() {
                try {
                    env.addGroovyJars(proj.getFullPath());
                    env.fullBuild();
                    
                    checkGroovyProject(proj);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        runMultipleTimes(runner);
    }

	
    // Tests that a groovy project that is converted back to a plain java 
    // project will not have its groovy files compiled
    public void testGroovyThenJavaProject() throws Exception {
        final IProject proj = createProject();
        env.addGroovyJars(proj.getFullPath());
        env.fullBuild();
        Runnable runner = new Runnable() {
            public void run() {
                try {
                    checkGroovyProject(proj);
                    env.removeGroovyNature(proj.getName());
                    fullBuild();
                    checkJavaProject(proj);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        runMultipleTimes(runner);
    }
    
    // Tests that a groovy project that is converted back to a plain java 
    // project will not have its groovy files compiled
    public void testJavaThenGroovyProject() throws Exception {
        final IProject proj = createProject();
        Runnable runner = new Runnable() {
            public void run() {
                try {
                    env.removeGroovyNature(proj.getName());
                    env.fullBuild();
                    checkJavaProject(proj);
                    
                    env.addGroovyNature(proj.getName());
                    env.addGroovyJars(proj.getFullPath());
                    fullBuild();
                    checkGroovyProject(proj);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        runMultipleTimes(runner);
    }
    
    void checkGroovyProject(IProject proj) throws CoreException {
        expectingNoProblems();
    
        // check that all source files exist
        IFile javaClass = proj.getFile(new Path("bin/p1/HelloJava.class"));
        IFile javaTestClass = proj.getFile(new Path("bin/p1/HelloJavatest.class"));
        IFile groovyClass = proj.getFile(new Path("bin/p1/HelloGroovy.class"));
        IFile groovyTestClass = proj.getFile(new Path("bin/p1/HelloGroovytest.class"));
        
        assertTrue(javaClass + " should exist", javaClass.exists());
        assertTrue(javaTestClass + " should exist", javaTestClass.exists());
        assertTrue(groovyClass + " should exist", groovyClass.exists());
        assertTrue(groovyTestClass + " should exist", groovyTestClass.exists());
        
        // touch all source files, rebuild and make sure that the same thing holds
        proj.getFile(new Path("src/p1/HelloJava.java")).touch(null);
        proj.getFile(new Path("src/p1/HelloJavatest.javatest")).touch(null);
        proj.getFile(new Path("src/p1/HelloGroovy.groovy")).touch(null);
        proj.getFile(new Path("src/p1/HelloGroovytest.groovytest")).touch(null);
        
        env.incrementalBuild();
        expectingNoProblems();
    
        assertTrue(javaClass + " should exist", javaClass.exists());
        assertTrue(javaTestClass + " should exist", javaTestClass.exists());
        assertTrue(groovyClass + " should exist", groovyClass.exists());
        assertTrue(groovyTestClass + " should exist", groovyTestClass.exists());
    }
    void checkJavaProject(IProject proj) throws CoreException {
        expectingNoProblems();
        
        // force waiting for build to complete
        ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
        
        // check that HelloJava.class and HelloJavatest.class exist,
        // but HelloGroovy and HelloGroovytest do not
        IFile javaClass = proj.getFile(new Path("bin/p1/HelloJava.class"));
        IFile javaTestClass = proj.getFile(new Path("bin/p1/HelloJavatest.class"));
        IFile groovyClass = proj.getFile(new Path("bin/p1/HelloGroovy.class"));
        IFile groovyTestClass = proj.getFile(new Path("bin/p1/HelloGroovytest.class"));
        
        assertTrue(javaClass + " should exist", javaClass.exists());
        assertTrue(javaTestClass + " should exist", javaTestClass.exists());
        assertFalse(groovyClass + " should not exist", groovyClass.exists());
        assertFalse(groovyTestClass + " should not exist", groovyTestClass.exists());
        
        // touch all source files, rebuild and make sure that the same thing holds
        proj.getFile(new Path("src/p1/HelloJava.java")).touch(null);
        proj.getFile(new Path("src/p1/HelloJavatest.javatest")).touch(null);
        proj.getFile(new Path("src/p1/HelloGroovy.groovy")).touch(null);
        proj.getFile(new Path("src/p1/HelloGroovytest.groovytest")).touch(null);
        
        env.incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
    
        assertTrue(javaClass + " should exist", javaClass.exists());
        assertTrue(javaTestClass + " should exist", javaTestClass.exists());
        assertFalse(groovyClass + " should not exist", groovyClass.exists());
        assertFalse(groovyTestClass + " should not exist", groovyTestClass.exists());
    }
    IProject createProject() throws Exception {
        IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
        env.addExternalJars(projectPath, org.eclipse.jdt.core.tests.util.Util.getJavaClassLibs());
        env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
        
        IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
        env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

        env.addClass(root, "p1", "HelloJava",
                "package p1;\n"+
                "public class HelloJava {\n"+
                "   public static void main(String[] args) {\n"+
                "      System.out.println(\"Hello world\");\n"+
                "   }\n"+
                "}\n"
                );

        IPath javaTest = env.addClass(root, "p1", "HelloJavatest",
                "package p1;\n"+
                "public class HelloJavatest {\n"+
                "   public static void main(String[] args) {\n"+
                "      System.out.println(\"Hello world\");\n"+
                "   }\n"+
                "}\n"
                );
        IFile javaTestFile = getFile(javaTest);
        javaTestFile.move(javaTestFile.getParent().getFullPath().append("HelloJavatest.javatest"), true, null);
        
        IPath groovy = env.addClass(root, "p1", "HelloGroovy",
                "package p1;\n"+
                "public class HelloGroovy {\n"+
                "   public static void main(String[] args) {\n"+
                "      System.out.println(\"Hello world\");\n"+
                "   }\n"+
                "}\n"
                );
        IFile groovyFile = getFile(groovy);
        groovyFile.move(groovyFile.getParent().getFullPath().append("HelloGroovy.groovy"), true, null);

        IPath groovyTest = env.addClass(root, "p1", "HelloGroovytest",
                "package p1;\n"+
                "public class HelloGroovytest {\n"+
                "   public static void main(String[] args) {\n"+
                "      System.out.println(\"Hello world\");\n"+
                "   }\n"+
                "}\n"
                );
        IFile groovyTestFile = getFile(groovyTest);
        groovyTestFile.move(groovyTestFile.getParent().getFullPath().append("HelloGroovytest.groovytest"), true, null);

        return env.getProject(projectPath);
    }

    void charCharContains(char[][] charChar, String containsStr) {
        char[] contains = containsStr.toCharArray();
        for (char[] chars : charChar) {
            if (chars.length == contains.length) {
                for (int i = 0; i < chars.length; i++) {
                    if (chars[i] != contains[i]) {
                        continue;
                    }
                }
                // found match
                return;
            }
        }
        fail("Should have found '" + new String(contains) + "' in '" + charCharToString(charChar) + "'");
    }
    void charCharNoContains(char[][] charChar, String containsStr) {
        char[] contains = containsStr.toCharArray();
        for (char[] chars : charChar) {
            if (Arrays.equals(chars, contains)) {
                // found match
                fail("Should not have found '" + new String(contains) + "' in '" + charCharToString(charChar) + "'");
            }
        }
    }
	
	String charCharToString(char[][] charChar) {
	    StringBuffer sb = new StringBuffer();
	    for (char[] chars : charChar) {
            for (char c : chars) {
                sb.append(c);
            }
            sb.append(" :: ");
        }
	    return sb.toString();
	}
	
    IFile getFile(IPath path) {
        return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
    }
    
    private void runMultipleTimes(Runnable runner) {
        AssertionFailedError currentException = null;
        for (int attempt = 0; attempt < 4; attempt++) {
            try {
                runner.run();
                
                // success
                return;
            } catch (AssertionFailedError e) {
                currentException = e;
                System.out.println("Launch failed on attempt " + attempt + " retrying."); 
            }
            
        }
        if (currentException != null) {
            throw currentException;
        }

    }

}
