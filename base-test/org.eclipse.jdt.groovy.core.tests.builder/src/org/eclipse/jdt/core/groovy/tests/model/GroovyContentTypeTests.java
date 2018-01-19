/*
 * Copyright 2009-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.groovy.tests.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import junit.framework.AssertionFailedError;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.groovy.tests.builder.BuilderTestSuite;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.internal.core.util.Util;
import org.junit.Test;

/**
 * Tests for checking that groovy files are not sent to compiler when project has no groovy nature.
 */
public final class GroovyContentTypeTests extends BuilderTestSuite {

    @Test
    public void testContentTypes() {
        char[][] javaLikeExtensions = Util.getJavaLikeExtensions();
        char[][] groovyLikeExtensions = ContentTypeUtils.getGroovyLikeExtensions();
        char[][] javaButNotGroovyExtensions = ContentTypeUtils.getJavaButNotGroovyLikeExtensions();

        String message = "Invalid number of extensions found:" +
            "\njavaLike: " + charCharToString(javaLikeExtensions) +
            "\ngroovyLike: " + charCharToString(groovyLikeExtensions) +
            "\njavaNotGroovyLike: " + charCharToString(javaButNotGroovyExtensions);
        assertEquals(message, javaLikeExtensions.length, groovyLikeExtensions.length + javaButNotGroovyExtensions.length);

        charCharContains(javaLikeExtensions, "java");
        charCharContains(javaLikeExtensions, "javatest");
        charCharContains(javaLikeExtensions, "groovy");
        charCharContains(javaLikeExtensions, "groovytest");

        charCharNoContains(groovyLikeExtensions, "java");
        charCharNoContains(groovyLikeExtensions, "javatest");
        charCharContains(groovyLikeExtensions, "groovy");
        charCharContains(groovyLikeExtensions, "groovytest");

        charCharContains(javaButNotGroovyExtensions, "java");
        charCharContains(javaButNotGroovyExtensions, "javatest");
        charCharNoContains(javaButNotGroovyExtensions, "groovy");
        charCharNoContains(javaButNotGroovyExtensions, "groovytest");
    }

    @Test
    public void testJavaOnlyProject() {
        runMultipleTimes(proj -> {
            env.removeGroovyNature(proj.getName());
            env.fullBuild();

            checkJavaProject(proj);
        });
    }

    @Test
    public void testGroovyProject() {
        runMultipleTimes(proj -> {
            env.addGroovyJars(proj.getFullPath());
            env.fullBuild();

            checkGroovyProject(proj);
        });
    }

    @Test // a groovy project that is converted back to a plain java project will not have its groovy files compiled
    public void testGroovyThenJavaProject() {
        runMultipleTimes(proj -> {
            env.addGroovyJars(proj.getFullPath());
            env.fullBuild();

            checkGroovyProject(proj);

            env.removeGroovyNature(proj.getName());
            fullBuild();

            checkJavaProject(proj);
        });
    }

    @Test // a groovy project that is converted back to a plain java project will not have its groovy files compiled
    public void testJavaThenGroovyProject() {
        runMultipleTimes(proj -> {
            env.removeGroovyNature(proj.getName());
            env.fullBuild();

            checkJavaProject(proj);

            env.addGroovyNature(proj.getName());
            env.addGroovyJars(proj.getFullPath());
            fullBuild();

            checkGroovyProject(proj);
        });
    }

    //--------------------------------------------------------------------------

    private void checkGroovyProject(IProject proj) throws CoreException {
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

    private void checkJavaProject(IProject proj) throws CoreException {
        expectingNoProblems();

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
        expectingNoProblems();

        assertTrue(javaClass + " should exist", javaClass.exists());
        assertTrue(javaTestClass + " should exist", javaTestClass.exists());
        assertFalse(groovyClass + " should not exist", groovyClass.exists());
        assertFalse(groovyTestClass + " should not exist", groovyTestClass.exists());
    }

    private static IProject createProject() throws CoreException {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, org.eclipse.jdt.core.tests.util.Util.getJavaClassLibs());
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addClass(root, "p1", "HelloJava",
            "package p1;\n" +
            "public class HelloJava {\n" +
            "   public static void main(String[] args) {\n" +
            "      System.out.println(\"Hello world\");\n" +
            "   }\n" +
            "}\n");
        IPath javaTest = env.addClass(root, "p1", "HelloJavatest",
            "package p1;\n" +
            "public class HelloJavatest {\n" +
            "   public static void main(String[] args) {\n" +
            "      System.out.println(\"Hello world\");\n" +
            "   }\n" +
            "}\n");
        IFile javaTestFile = ResourcesPlugin.getWorkspace().getRoot().getFile(javaTest);
        javaTestFile.move(javaTestFile.getParent().getFullPath().append("HelloJavatest.javatest"), true, null);

        IPath groovy = env.addClass(root, "p1", "HelloGroovy",
            "package p1;\n" +
            "public class HelloGroovy {\n" +
            "   public static void main(String[] args) {\n" +
            "      System.out.println(\"Hello world\");\n" +
            "   }\n" +
            "}\n");
        IFile groovyFile = ResourcesPlugin.getWorkspace().getRoot().getFile(groovy);
        groovyFile.move(groovyFile.getParent().getFullPath().append("HelloGroovy.groovy"), true, null);

        IPath groovyTest = env.addClass(root, "p1", "HelloGroovytest",
            "package p1;\n" +
            "public class HelloGroovytest {\n" +
            "   public static void main(String[] args) {\n" +
            "      System.out.println(\"Hello world\");\n" +
            "   }\n" +
            "}\n");
        IFile groovyTestFile = ResourcesPlugin.getWorkspace().getRoot().getFile(groovyTest);
        groovyTestFile.move(groovyTestFile.getParent().getFullPath().append("HelloGroovytest.groovytest"), true, null);

        return env.getProject(projectPath);
    }

    private static void charCharContains(char[][] charChar, String containsStr) {
        if (CharOperation.containsEqual(charChar, containsStr.toCharArray())) {
            return; // found match
        }
        fail("Should have found '" + containsStr + "' in '" + charCharToString(charChar) + "'");
    }

    private static void charCharNoContains(char[][] charChar, String containsStr) {
        if (CharOperation.containsEqual(charChar, containsStr.toCharArray())) {
            fail("Should not have found '" + containsStr + "' in '" + charCharToString(charChar) + "'");
        }
    }

    private static String charCharToString(char[][] charChar) {
        return String.valueOf(CharOperation.concatWith(charChar, ','));
    }

    private static void runMultipleTimes(Runner runner) {
        try {
            IProject proj = createProject();
            AssertionFailedError failedAssertion = null;
            for (int attempt = 1; attempt <= 5; attempt += 1) {
                try {
                    runner.run(proj);
                    return; // success
                } catch (AssertionFailedError e) {
                    failedAssertion = e;
                    System.out.println("Launch failed on attempt " + attempt + "; retrying.");
                }
                env.waitForManualRefresh();
            }
            if (failedAssertion != null) {
                throw failedAssertion;
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    private interface Runner {
        void run(IProject p) throws Exception;
    }
}
