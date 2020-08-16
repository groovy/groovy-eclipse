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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.groovy.tests.builder.BuilderTestSuite;

public abstract class GroovyTypeRootTestSuite extends BuilderTestSuite {

    protected final IFile createProject(boolean isGroovy) throws Exception {
        IPath projectPath = env.addProject("Project");
        if (!isGroovy) {
            env.removeGroovyNature("Project");
        }

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        if (isGroovy) {
            env.addGroovyJars(projectPath);
            //@formatter:off
            IPath path = env.addGroovyClass(root, "p1", "Hello",
                "package p1;\n" +
                "public class Hello {\n" +
                "   static def main(String[] args) {\n" +
                "      print \"Hello world\"\n" +
                "   }\n" +
                "}\n");
            //@formatter:on

            fullBuild(projectPath);

            return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        }
        fullBuild(projectPath);

        return ResourcesPlugin.getWorkspace().getRoot().getFile(root);
    }

    protected final IFile createSimpleGroovyProject() throws Exception {
        return createProject(true);
    }

    protected final IFile createSimpleJavaProject() throws Exception {
        return createProject(false);
    }

    protected final IPath createEmptyGroovyProject() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addGroovyJars(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");
        IPath root = env.addPackageFragmentRoot(projectPath, "src");

        env.setOutputFolder(projectPath, "bin");
        fullBuild(projectPath);
        return root;
    }

    protected final IPath createAnnotationGroovyProject() throws Exception {
        IPath root = createEmptyGroovyProject();

        //@formatter:off
        env.addClass(root, "p", "Anno1.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno1 { Class<?> value(); }\n");
        env.addClass(root, "p", "Anno2.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno2 { }\n");
        env.addClass(root, "p", "Anno3.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno3 { String value(); }\n");
        env.addClass(root, "p", "Anno4.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno4 { Class<?> value1(); }\n");
        env.addClass(root, "p", "Target.java",
            "package p;\n" +
            "class Target { }");
        //@formatter:on

        return root;
    }
}
