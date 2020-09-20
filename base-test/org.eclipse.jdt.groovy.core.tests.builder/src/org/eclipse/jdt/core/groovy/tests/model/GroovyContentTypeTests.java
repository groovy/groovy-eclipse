/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.groovy.tests.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.groovy.tests.SimpleProgressMonitor;
import org.eclipse.jdt.core.groovy.tests.builder.BuilderTestSuite;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.internal.core.util.Util;
import org.junit.Assert;
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
        Assert.assertEquals(message, javaLikeExtensions.length, groovyLikeExtensions.length + javaButNotGroovyExtensions.length);

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
    public void testJavaOnlyProject() throws Exception {
        IProject proj = createProject();
        env.removeGroovyNature(proj.getName());

        checkJavaProject(proj);
    }

    @Test
    public void testGroovyProject() throws Exception {
        IProject proj = createProject();
        env.addGroovyJars(proj.getFullPath());

        checkGroovyProject(proj);
    }

    @Test // a groovy project that is converted back to a plain java project will not have its groovy files compiled
    public void testGroovyThenJavaProject() throws Exception {
        IProject proj = createProject();
        env.addGroovyJars(proj.getFullPath());

        checkGroovyProject(proj);

        env.removeGroovyNature(proj.getName());

        checkJavaProject(proj);
    }

    @Test // a groovy project that is converted back to a plain java project will not have its groovy files compiled
    public void testJavaThenGroovyProject() throws Exception {
        IProject proj = createProject();
        env.removeGroovyNature(proj.getName());

        checkJavaProject(proj);

        env.addGroovyNature(proj.getName());
        env.addGroovyJars(proj.getFullPath());

        checkGroovyProject(proj);
    }

    //--------------------------------------------------------------------------

    private void checkGroovyProject(IProject proj) throws Exception {
        cleanBuild();
        fullBuild();
        expectingNoProblems();

        // check that all source files exist
        IFile javaClass = proj.getFile(new Path("bin/p1/HelloJava.class"));
        IFile javaTestClass = proj.getFile(new Path("bin/p1/HelloJavatest.class"));
        IFile groovyClass = proj.getFile(new Path("bin/p1/HelloGroovy.class"));
        IFile groovyTestClass = proj.getFile(new Path("bin/p1/HelloGroovytest.class"));

        Assert.assertTrue(javaClass + " should exist", javaClass.exists());
        Assert.assertTrue(javaTestClass + " should exist", javaTestClass.exists());
        Assert.assertTrue(groovyClass + " should exist", groovyClass.exists());
        Assert.assertTrue(groovyTestClass + " should exist", groovyTestClass.exists());

        // touch all source files, rebuild and make sure that the same thing holds
        IProgressMonitor monitor = new SimpleProgressMonitor("touch");
        proj.getFile(new Path("src/p1/HelloJava.java")).touch(monitor);
        proj.getFile(new Path("src/p1/HelloJavatest.javatest")).touch(monitor);
        proj.getFile(new Path("src/p1/HelloGroovy.groovy")).touch(monitor);
        proj.getFile(new Path("src/p1/HelloGroovytest.groovytest")).touch(monitor);

        incrementalBuild();
        expectingNoProblems();

        Assert.assertTrue(javaClass + " should exist", javaClass.exists());
        Assert.assertTrue(javaTestClass + " should exist", javaTestClass.exists());
        Assert.assertTrue(groovyClass + " should exist", groovyClass.exists());
        Assert.assertTrue(groovyTestClass + " should exist", groovyTestClass.exists());
    }

    private void checkJavaProject(IProject proj) throws Exception {
        cleanBuild();
        fullBuild();
        expectingNoProblems();

        // check that HelloJava.class and HelloJavatest.class exist,
        // but HelloGroovy and HelloGroovytest do not
        IFile javaClass = proj.getFile(new Path("bin/p1/HelloJava.class"));
        IFile javaTestClass = proj.getFile(new Path("bin/p1/HelloJavatest.class"));
        IFile groovyClass = proj.getFile(new Path("bin/p1/HelloGroovy.class"));
        IFile groovyTestClass = proj.getFile(new Path("bin/p1/HelloGroovytest.class"));

        Assert.assertTrue(javaClass + " should exist", javaClass.exists());
        Assert.assertTrue(javaTestClass + " should exist", javaTestClass.exists());
        Assert.assertFalse(groovyClass + " should not exist", groovyClass.exists());
        Assert.assertFalse(groovyTestClass + " should not exist", groovyTestClass.exists());

        // touch all source files, rebuild and make sure that the same thing holds
        IProgressMonitor monitor = new SimpleProgressMonitor("touch");
        proj.getFile(new Path("src/p1/HelloJava.java")).touch(monitor);
        proj.getFile(new Path("src/p1/HelloJavatest.javatest")).touch(monitor);
        proj.getFile(new Path("src/p1/HelloGroovy.groovy")).touch(monitor);
        proj.getFile(new Path("src/p1/HelloGroovytest.groovytest")).touch(monitor);

        incrementalBuild();
        expectingNoProblems();

        Assert.assertTrue(javaClass + " should exist", javaClass.exists());
        Assert.assertTrue(javaTestClass + " should exist", javaTestClass.exists());
        Assert.assertFalse(groovyClass + " should not exist", groovyClass.exists());
        Assert.assertFalse(groovyTestClass + " should not exist", groovyTestClass.exists());
    }

    private static IProject createProject() throws Exception {
        IPath projectPath = env.addProject("Project");

        IPath root = env.getPackageFragmentRootPath(projectPath, "src");

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
        Assert.fail("Should have found '" + containsStr + "' in '" + charCharToString(charChar) + "'");
    }

    private static void charCharNoContains(char[][] charChar, String containsStr) {
        if (CharOperation.containsEqual(charChar, containsStr.toCharArray())) {
            Assert.fail("Should not have found '" + containsStr + "' in '" + charCharToString(charChar) + "'");
        }
    }

    private static String charCharToString(char[][] charChar) {
        return String.valueOf(CharOperation.concatWith(charChar, ','));
    }
}
