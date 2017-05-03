/*
 * Copyright 2009-2017 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.builder;

import static org.eclipse.jdt.core.tests.util.GroovyUtils.isAtLeastGroovy;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.groovy.tests.ReconcilerUtils;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.junit.Test;

/**
 * These tests are about building and working with complete projects.
 * <p>
 * To add a new project:
 * <ul>
 * <li>Create a folder named after the project in the testdata folder
 * <li>In there create a source.zip containing your source code (call it
 * source.zip)
 * <li>Create a folder 'lib' and copy all the dependencies the source has into
 * there (as jar files)
 * <li>It can be helpful to also create a readme.txt at the project level
 * describing where the source is from, what commit it is (e.g. git commit tag)
 * </ul>
 * <p>
 * Once setup like that it is usable for testing here.
 */
public final class FullProjectTests extends BuilderTestSuite {

    private static void assertDoesNotContainMethod(ClassNode cn, String methodname) {
        for (MethodNode mn : cn.getMethods()) {
            if (mn.getName().equals(methodname)) {
                fail("Found method named '" + methodname + "' in class '" + cn.getName() + "'");
            }
        }
    }

    private static void assertContainsMethod(ClassNode cn, String methodname) {
        for (MethodNode mn : cn.getMethods()) {
            if (mn.getName().equals(methodname)) {
                return;
            }
        }
        fail("Did not find method named '" + methodname + "' in class '" + cn.getName() + "'");
    }

    private static void assertContainsProblem(Set<IProblem> problems, String expected) {
        for (IProblem problem : problems) {
            if (problem.toString().contains(expected)) {
                return;
            }
        }
        fail("Expected '" + expected + "' in data '" + problems + "'");
    }

    private static void setTransformsOption(IJavaProject javaproject, String transformsSpec) {
        Map<String, String> m = new HashMap<String, String>();
        m.put(CompilerOptions.OPTIONG_GroovyTransformsToRunOnReconcile, transformsSpec);
        javaproject.setOptions(m);
    }

    //--------------------------------------------------------------------------

    @Test // Transforms during reconciling tests
    public void testReconcilingWithTransforms_notransformallowed() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // Slight change in behavior from around groovy 2.1.8/groovy 2.2beta2 onwards. If you don't say anything
        // they are all on. If you do say something it is obeyed. You can say '*'
        setTransformsOption(env.getJavaProject(projectPath), "Foo");

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath path = env.addGroovyClass(root, "", "Foo",
            "@Singleton\n"+
            "class Foo {\n"+
            "  void mone() {}\n"+
            "}\n"
        );

        incrementalBuild(projectPath);

        ICompilationUnit icu = env.getUnit(path);
        icu.becomeWorkingCopy(null);

        List<ClassNode> classes = ((GroovyCompilationUnit) icu).getModuleNode().getClasses();
        ClassNode cn = classes.get(0);
        assertDoesNotContainMethod(cn, "getInstance");
    }

    @Test
    public void testReconcilingWithTransforms_singletonallowed() throws Exception {
        assumeTrue(isAtLeastGroovy(20));

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        setTransformsOption(env.getJavaProject(projectPath), "Singleton");
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath path = env.addGroovyClass(root, "", "Foo",
            "@Singleton\n"+
            "class Foo {\n"+
            "  void mone() {}\n"+
            "}\n"
        );

        incrementalBuild(projectPath);

        ICompilationUnit icu = env.getUnit(path);
        icu.becomeWorkingCopy(null);

        List<ClassNode> classes = ((GroovyCompilationUnit) icu).getModuleNode().getClasses();
        ClassNode cn = classes.get(0);
        assertContainsMethod(cn, "getInstance");
    }

    @Test
    public void testReconcilingWithTransforms_singletonallowedspecialchar() throws Exception {
        assumeTrue(isAtLeastGroovy(20));

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        setTransformsOption(env.getJavaProject(projectPath), "Singleton$");
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath path = env.addGroovyClass(root, "", "Foo",
            "@Singleton\n"+
            "class Foo {\n"+
            "  void mone() {}\n"+
            "}\n"
        );

        incrementalBuild(projectPath);

        ICompilationUnit icu = env.getUnit(path);
        icu.becomeWorkingCopy(null);

        List<ClassNode> classes = ((GroovyCompilationUnit) icu).getModuleNode().getClasses();
        ClassNode cn = classes.get(0);
        assertContainsMethod(cn, "getInstance");
    }

    @Test
    public void testReconcilingWithTransforms_multipleButOnlyOneAllowed() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);

        setTransformsOption(env.getJavaProject(projectPath), "Singleton");
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath path = env.addGroovyClass(root, "", "Foo",
            "@Singleton\n"+
            "class Foo {\n"+
            "  @Delegate Bar b = new BarImpl();\n"+
            "  void mone() {}\n"+
            "}\n"+
            "interface Bar { void method(); }\n"+
            "class BarImpl implements Bar { void method() {};}\n"
        );

        incrementalBuild(projectPath);

        ICompilationUnit icu = env.getUnit(path);
        icu.becomeWorkingCopy(null);

        List<ClassNode> classes = ((GroovyCompilationUnit) icu).getModuleNode().getClasses();
        ClassNode cn = classes.get(0);
        assertContainsMethod(cn, "getInstance");
        assertDoesNotContainMethod(cn, "method");
    }

    @Test
    public void testReconcilingWithTransforms_multipleAndBothAllowed() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        setTransformsOption(env.getJavaProject(projectPath), "Singleton,Delegate");
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath path = env.addGroovyClass(root, "", "Foo",
            "@Singleton\n"+
            "class Foo {\n"+
            "  @Delegate Bar b = new BarImpl();\n"+
            "  void mone() {}\n"+
            "}\n"+
            "interface Bar { void method(); }\n"+
            "class BarImpl implements Bar { void method() {};}\n"
        );

        incrementalBuild(projectPath);

        ICompilationUnit icu = env.getUnit(path);
        icu.becomeWorkingCopy(null);

        List<ClassNode> classes = ((GroovyCompilationUnit) icu).getModuleNode().getClasses();
        ClassNode cn = classes.get(0);
        assertContainsMethod(cn, "getInstance");
        assertContainsMethod(cn, "method");
    }

    @Test
    public void testReconcilingWithTransforms_compileStatic() throws Exception {
        assumeTrue(isAtLeastGroovy(20));

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath path = env.addGroovyClass(root, "", "Foo",
            "@groovy.transform.CompileStatic\n"+
            "class Foo {\n"+
            "  void xxx(int i) { xxx('abc');}\n"+
            "}\n"
        );

        incrementalBuild(projectPath);
        ICompilationUnit icu = env.getUnit(path);
        Set<IProblem> problems = ReconcilerUtils.reconcile(icu);
        assertContainsProblem(problems, "Cannot find matching method Foo#xxx");
    }

    @Test
    public void testReconcilingWithTransforms_typeChecked() throws Exception {
        assumeTrue(isAtLeastGroovy(20));

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath path = env.addGroovyClass(root, "", "Foo",
            "@groovy.transform.TypeChecked\n"+
            "class Foo {\n"+
            "  void xxx(int i) { xxx('abc');}\n"+
            "}\n"
        );

        incrementalBuild(projectPath);

        ICompilationUnit icu = env.getUnit(path);
        Set<IProblem> problems = ReconcilerUtils.reconcile(icu);
        assertContainsProblem(problems, "Cannot find matching method Foo#xxx");
    }

    @Test
    public void testReconcilingWithTransforms_multipleAndWildcard() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);

        IJavaProject p = env.getJavaProject(projectPath);
        setTransformsOption(p, "*");

        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath path = env.addGroovyClass(root, "", "Foo",
            "@Singleton\n"+
            "class Foo {\n"+
            "  @Delegate Bar b = new BarImpl();\n"+
            "  void mone() {}\n"+
            "}\n"+
            "interface Bar { void method(); }\n"+
            "class BarImpl implements Bar { void method() {};}\n"
        );

        incrementalBuild(projectPath);

        ICompilationUnit icu = env.getUnit(path);
        icu.becomeWorkingCopy(null);

        List<ClassNode> classes = ((GroovyCompilationUnit) icu).getModuleNode().getClasses();
        ClassNode cn = classes.get(0);
        assertContainsMethod(cn, "getInstance");
        assertContainsMethod(cn, "method");
    }
}
