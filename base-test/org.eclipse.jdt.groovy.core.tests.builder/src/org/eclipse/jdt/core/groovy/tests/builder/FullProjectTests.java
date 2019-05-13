/*
 * Copyright 2009-2019 the original author or authors.
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

import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.groovy.tests.ReconcilerUtils;
import org.junit.Assert;
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

    private static void assertContainsMethod(ClassNode classNode, String methodName) {
        boolean found = classNode.getMethods().stream().map(MethodNode::getName).anyMatch(methodName::equals);
        Assert.assertTrue("Did not find method named '" + methodName + "' in class '" + classNode.getName() + "'", found);
    }

    private static void assertContainsProblem(Set<IProblem> problems, String expected) {
        boolean found = problems.stream().map(IProblem::toString).anyMatch(p -> p.contains(expected));
        Assert.assertTrue("Expected '" + expected + "' in data '" + problems + "'", found);
    }

    private IPath[] createGroovyProject() throws Exception {
        IPath prj = env.addProject("Project");
        env.addGroovyJars(prj);
        env.setOutputFolder(prj, "bin");
        env.removePackageFragmentRoot(prj, "");
        return new IPath[] {prj, env.addPackageFragmentRoot(prj, "src")};
    }

    //--------------------------------------------------------------------------

    @Test
    public void testReconcilingWithTransforms_single() throws Exception {
        IPath[] paths = createGroovyProject();

        IPath foo = env.addGroovyClass(paths[1], "", "Foo",
            //@formatter:off
            "@Singleton\n" +
            "class Foo {\n" +
            "  void mone() {}\n" +
            "}\n");
            //@formatter:on

        fullBuild(paths[0]);
        ICompilationUnit icu = env.getUnit(foo);
        icu.becomeWorkingCopy(null);

        ClassNode cn = ((GroovyCompilationUnit) icu).getModuleNode().getClasses().get(0);
        assertContainsMethod(cn, "getInstance");
    }

    @Test
    public void testReconcilingWithTransforms_multiple() throws Exception {
        IPath[] paths = createGroovyProject();

        IPath foo = env.addGroovyClass(paths[1], "", "Foo",
            //@formatter:off
            "@Singleton\n" +
            "class Foo {\n" +
            "  @Delegate Bar b = new BarImpl();\n" +
            "  void mone() {}\n" +
            "}\n" +
            "interface Bar { void method(); }\n" +
            "class BarImpl implements Bar { void method() {} }\n");
            //@formatter:on

        fullBuild(paths[0]);
        ICompilationUnit icu = env.getUnit(foo);
        icu.becomeWorkingCopy(null);

        ClassNode cn = ((GroovyCompilationUnit) icu).getModuleNode().getClasses().get(0);
        assertContainsMethod(cn, "getInstance");
        assertContainsMethod(cn, "method");
    }

    @Test
    public void testReconcilingWithTransforms_typeChecked() throws Exception {
        IPath[] paths = createGroovyProject();

        IPath foo = env.addGroovyClass(paths[1], "", "Foo",
            //@formatter:off
            "@groovy.transform.TypeChecked\n" +
            "class Foo {\n" +
            "  void xxx(int i) { xxx('abc') }\n" +
            "}\n");
            //@formatter:on

        fullBuild(paths[0]);
        Set<IProblem> problems = ReconcilerUtils.reconcile(env.getUnit(foo));
        assertContainsProblem(problems, "Cannot find matching method Foo#xxx");
    }

    @Test
    public void testReconcilingWithTransforms_compileStatic() throws Exception {
        IPath[] paths = createGroovyProject();

        IPath foo = env.addGroovyClass(paths[1], "", "Foo",
            //@formatter:off
            "@groovy.transform.CompileStatic\n" +
            "class Foo {\n" +
            "  void xxx(int i) { xxx('abc') }\n" +
            "}\n");
            //@formatter:on

        fullBuild(paths[0]);
        Set<IProblem> problems = ReconcilerUtils.reconcile(env.getUnit(foo));
        assertContainsProblem(problems, "Cannot find matching method Foo#xxx");
    }
}
