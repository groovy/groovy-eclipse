/*
 * Copyright 2009-2016 the original author or authors.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.groovy.tests.compiler.ReconcilerUtils;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

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
 *
 * @author Andy CLement
 * @since 2.5.1
 */
public class FullProjectTests extends GroovierBuilderTests {

    public FullProjectTests(String name) {
        super(name);
    }

    public static Test suite() {
        return buildTestSuite(FullProjectTests.class);
    }

    // Transforms during reconciling tests
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

        //@formatter:off
        env.addGroovyClass(root, "", "Foo",
                "@Singleton\n"+
                "class Foo {\n"+
                "  void mone() {}\n"+
                "}\n"
            );
        //@formatter:on

        incrementalBuild(projectPath);

        IJavaProject p = env.getJavaProject(projectPath);
        ICompilationUnit icu = ReconcilerUtils.getWorkingCopy(p, "Foo.groovy");
        icu.becomeWorkingCopy(null);

        List<ClassNode> classes = ((GroovyCompilationUnit) icu).getModuleNode().getClasses();
        ClassNode cn = classes.get(0);
        assertDoesNotContainMethod(cn, "getInstance");
    }

    public void testReconcilingWithTransforms_singletonallowed() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 20) {
            return;
        }
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        setTransformsOption(env.getJavaProject(projectPath), "Singleton");
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        //@formatter:off
        env.addGroovyClass(root, "", "Foo",
                "@Singleton\n"+
                "class Foo {\n"+
                "  void mone() {}\n"+
                "}\n"
            );
        //@formatter:on

        incrementalBuild(projectPath);

        IJavaProject p = env.getJavaProject(projectPath);
        ICompilationUnit icu = ReconcilerUtils.getWorkingCopy(p, "Foo.groovy");
        icu.becomeWorkingCopy(null);

        List<ClassNode> classes = ((GroovyCompilationUnit) icu).getModuleNode().getClasses();
        ClassNode cn = classes.get(0);
        assertContainsMethod(cn, "getInstance");
    }

    public void testReconcilingWithTransforms_singletonallowedspecialchar() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 20) {
            return;
        }
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        setTransformsOption(env.getJavaProject(projectPath), "Singleton$");
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        //@formatter:off
        env.addGroovyClass(root, "", "Foo",
                "@Singleton\n"+
                "class Foo {\n"+
                "  void mone() {}\n"+
                "}\n"
            );
        //@formatter:on

        incrementalBuild(projectPath);

        IJavaProject p = env.getJavaProject(projectPath);
        ICompilationUnit icu = ReconcilerUtils.getWorkingCopy(p, "Foo.groovy");
        icu.becomeWorkingCopy(null);

        List<ClassNode> classes = ((GroovyCompilationUnit) icu).getModuleNode().getClasses();
        ClassNode cn = classes.get(0);
        assertContainsMethod(cn, "getInstance");
    }

    public void testReconcilingWithTransforms_multipleButOnlyOneAllowed() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);

        setTransformsOption(env.getJavaProject(projectPath), "Singleton");
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        //@formatter:off
        env.addGroovyClass(root, "", "Foo",
                "@Singleton\n"+
                "class Foo {\n"+
                "  @Delegate Bar b = new BarImpl();\n"+
                "  void mone() {}\n"+
                "}\n"+
                "interface Bar { void method(); }\n"+
                "class BarImpl implements Bar { void method() {};}\n"
            );
        //@formatter:on

        incrementalBuild(projectPath);

        IJavaProject p = env.getJavaProject(projectPath);
        ICompilationUnit icu = ReconcilerUtils.getWorkingCopy(p, "Foo.groovy");
        icu.becomeWorkingCopy(null);

        List<ClassNode> classes = ((GroovyCompilationUnit) icu).getModuleNode().getClasses();
        ClassNode cn = classes.get(0);
        assertContainsMethod(cn, "getInstance");
        assertDoesNotContainMethod(cn, "method");
    }

    public void testReconcilingWithTransforms_multipleAndBothAllowed() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        setTransformsOption(env.getJavaProject(projectPath), "Singleton,Delegate");
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        //@formatter:off
        env.addGroovyClass(root, "", "Foo",
                "@Singleton\n"+
                "class Foo {\n"+
                "  @Delegate Bar b = new BarImpl();\n"+
                "  void mone() {}\n"+
                "}\n"+
                "interface Bar { void method(); }\n"+
                "class BarImpl implements Bar { void method() {};}\n"
            );
        //@formatter:on

        incrementalBuild(projectPath);

        IJavaProject p = env.getJavaProject(projectPath);
        ICompilationUnit icu = ReconcilerUtils.getWorkingCopy(p, "Foo.groovy");
        icu.becomeWorkingCopy(null);

        List<ClassNode> classes = ((GroovyCompilationUnit) icu).getModuleNode().getClasses();
        ClassNode cn = classes.get(0);
        assertContainsMethod(cn, "getInstance");
        assertContainsMethod(cn, "method");
    }

    public void testReconcilingWithTransforms_compileStatic() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 20) {
            return;
        }
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        IJavaProject ijp = env.getJavaProject(projectPath);

        // this setting is irrelevant, the ASTTransformationCodeCollectorVisitor.isAllowed always lets it through
        // setTransformsOption(ijp, "groovy.transform.CompileStatic");

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        //@formatter:off
        env.addGroovyClass(root, "", "Foo",
                "@groovy.transform.CompileStatic\n"+
                "class Foo {\n"+
                "  void xxx(int i) { xxx('abc');}\n"+
                "}\n"
            );
        //@formatter:on

        incrementalBuild(projectPath);
        System.err.println("now reconciling");
        ICompilationUnit icu = ReconcilerUtils.getWorkingCopy(ijp, "Foo.groovy");
        PR pr = new PR();
        icu.becomeWorkingCopy(pr, null);
        assertContains(pr.problems, "Cannot find matching method Foo#xxx");
    }

    public void testReconcilingWithTransforms_typeChecked() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 20) {
            return;
        }
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        IJavaProject ijp = env.getJavaProject(projectPath);

        // this setting is irrelevant, the ASTTransformationCodeCollectorVisitor.isAllowed always lets it through
        // setTransformsOption(ijp, "groovy.transform.TypeChecked");

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        //@formatter:off
        env.addGroovyClass(root, "", "Foo",
                "@groovy.transform.TypeChecked\n"+
                "class Foo {\n"+
                "  void xxx(int i) { xxx('abc');}\n"+
                "}\n"
            );
        //@formatter:on

        incrementalBuild(projectPath);

        ICompilationUnit icu = ReconcilerUtils.getWorkingCopy(ijp, "Foo.groovy");
        PR pr = new PR();
        icu.becomeWorkingCopy(pr, null);
        assertContains(pr.problems, "Cannot find matching method Foo#xxx");
    }

    static class PR implements IProblemRequestor {
        public String problems = "";

        public void acceptProblem(IProblem problem) {
            problems = problems + "\n" + problem.toString();
        }

        public void beginReporting() {
        }

        public void endReporting() {
        }

        public boolean isActive() {
            return true;
        }
    }

    public void testReconcilingWithTransforms_multipleAndWildcard() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
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

        //@formatter:off
        env.addGroovyClass(root, "", "Foo",
                "@Singleton\n"+
                "class Foo {\n"+
                "  @Delegate Bar b = new BarImpl();\n"+
                "  void mone() {}\n"+
                "}\n"+
                "interface Bar { void method(); }\n"+
                "class BarImpl implements Bar { void method() {};}\n"
            );
        //@formatter:on

        incrementalBuild(projectPath);

        ICompilationUnit icu = ReconcilerUtils.getWorkingCopy(p, "Foo.groovy");
        icu.becomeWorkingCopy(null);

        List<ClassNode> classes = ((GroovyCompilationUnit) icu).getModuleNode().getClasses();
        ClassNode cn = classes.get(0);
        assertContainsMethod(cn, "getInstance");
        assertContainsMethod(cn, "method");
    }

    public static void assertContainsMethod(ClassNode cn, String methodname) {
        for (MethodNode mn : cn.getMethods()) {
            if (mn.getName().equals(methodname)) {
                return;
            }
        }
        fail("Did not find method named '" + methodname + "' in class '" + cn.getName() + "'");
    }

    public static void assertContains(String data, String expected) {
        if (data.indexOf(expected) != -1) {
            return;
        }
        fail("Expected '" + expected + "' in data '" + data + "'");
    }

    public static void assertDoesNotContainMethod(ClassNode cn, String methodname) {
        for (MethodNode mn : cn.getMethods()) {
            if (mn.getName().equals(methodname)) {
                fail("Found method named '" + methodname + "' in class '" + cn.getName() + "'");
            }
        }
    }

    private void setTransformsOption(IJavaProject javaproject, String transformsSpec) {
        Map<String, String> m = new HashMap<String, String>();
        m.put(CompilerOptions.OPTIONG_GroovyTransformsToRunOnReconcile, transformsSpec);
        javaproject.setOptions(m);
    }
}
