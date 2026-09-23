/*
 * Copyright 2009-2026 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.builder;

import static org.eclipse.jdt.groovy.core.util.ReflectionUtils.executePrivateMethod;
import static org.junit.Assume.assumeNoException;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Gradle (aka Buildship) projects.
 */
public final class GradleBuildTests extends BuilderTestSuite {

    private static IPath[] createGradleProject(final String name, final boolean isGroovy) throws Exception {
        IPath root = env.addProject(name);

        if (!isGroovy) env.removeGroovyNature(name);

        // add Gradle preferences
        env.addFile(env.addFolder(root, ".settings"), "org.eclipse.buildship.core.prefs", """
            arguments=
            auto.sync=false
            build.scans.enabled=false
            connection.gradle.distribution=GRADLE_DISTRIBUTION(VERSION(9.7.1))
            connection.project.dir=
            eclipse.preferences.version=1
            gradle.user.home=
            java.home=
            jvm.arguments=
            offline.mode=false
            override.workspace.settings=true
            show.console.view=false
            show.executions.view=false
            """);

        // add Gradle nature
        IProject project = env.getProject(root);
        IProjectDescription description = project.getDescription();
        String[] natures = description.getNatureIds();
        natures = Arrays.copyOf(natures, natures.length + 1);
        natures[natures.length - 1] = "org.eclipse.buildship.core.gradleprojectnature";
        description.setNatureIds(natures);
        project.setDescription(description, null);

        IPath src = env.addFolder(root, "src");
        IPath main = env.addFolder(src, "main");
        IPath test = env.addFolder(src, "test");

        if (!isGroovy) {
            return new IPath[] {root, env.addFolder(main, "java"), env.addFolder(test, "java")};
        } else {
            return new IPath[] {root, env.addFolder(main, "groovy"), env.addFolder(test, "groovy"), env.addFolder(main, "java"), env.addFolder(test, "java")};
        }
    }

    private void refreshGradleProject(final IPath path) {
        //GradleCore.getWorkspace().getBuild(project).get().synchronize(new NullProgressMonitor());
        var workspace = executePrivateMethod(gradlePlugin, "getWorkspace", null);
        var build = executePrivateMethod(workspace.getClass(), "getBuild", new Class[]{IProject.class}, workspace, new Object[]{env.getProject(path)});
        build = executePrivateMethod(Optional.class, "get", build);
        executePrivateMethod(build.getClass(), "synchronize", new Class[]{IProgressMonitor.class}, build, new Object[]{new NullProgressMonitor()});

        fullBuild(path);
    }

    private Class<?> gradlePlugin;

    @Before
    public void setUp() {
        try {
            gradlePlugin = Class.forName("org.eclipse.buildship.core.GradleCore");
        } catch (Throwable t) {
            assumeNoException(t);
        }
    }

    //--------------------------------------------------------------------------

    @Test // https://github.com/groovy/groovy-eclipse/issues/1700
    public void testMultiProjectDependenciesMainAndTest() throws Exception {
        IPath[] pathsA = createGradleProject("ProjectA", false);

        env.addFile(pathsA[0], "build.gradle", """
            plugins {
                id 'java'
            }
            repositories {
                mavenCentral()
            }
            dependencies {
                implementation 'org.codehaus.groovy:groovy:3.0.25'
                implementation 'org.opentest4j:opentest4j:1.3.0'
            }
            """);

        env.addClass(pathsA[1], "p", "Transform",
            "package p;\n" +
            "import org.codehaus.groovy.ast.ASTNode;\n" +
            "import org.codehaus.groovy.control.SourceUnit;\n" +
            "import org.codehaus.groovy.transform.ASTTransformation;\n" +
            "import org.codehaus.groovy.transform.GroovyASTTransformation;\n" +
            "@GroovyASTTransformation\n" +
            "public class Transform implements ASTTransformation {\n" +
            "  public void visit(ASTNode[] nodes, SourceUnit unit) {\n" +
            "    // make reference to extra class that is on transform classpath\n" +
            "    System.out.println(org.opentest4j.MultipleFailuresError.class);\n" +
            "  }\n" +
            "}\n");

        env.addFile(
            env.addFolder(env.addFolder(pathsA[1], "META-INF"), "services"),
            "org.codehaus.groovy.transform.ASTTransformation", "p.Transform");

        refreshGradleProject(pathsA[0]);
        expectingNoProblemsFor(pathsA[0]);
        expectingCompiledClasses("p.Transform");

        //

        IPath[] pathsB = createGradleProject("ProjectB", true);

        env.addFile(pathsB[0], "build.gradle", """
            plugins {
                id 'groovy'
            }
            repositories {
                mavenCentral()
            }
            dependencies {
                testImplementation project(':ProjectA') // ProjectB:test requires ProjectA:main
            }
            """);

        env.addFile(pathsB[0], "settings.gradle", """
            include(':ProjectA')
            project(':ProjectA').projectDir = file('../ProjectA')
            """);

        // global transform from ProjectA should be applied to this groovy script
        env.addGroovyClass(pathsB[2], "q", "Test", "package q\nprint 'works'\n");

        refreshGradleProject(pathsB[0]);
        expectingNoProblemsFor(pathsB[0]);
        expectingCompiledClasses("q.Test");

        executeClass(pathsB[0], "q.Test", "works", "");
    }
}
