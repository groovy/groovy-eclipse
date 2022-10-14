/*
 * Copyright 2009-2022 the original author or authors.
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
        return new IPath[] {prj, env.getPackageFragmentRootPath(prj, "src")};
    }

    //--------------------------------------------------------------------------

    @Test
    public void testReconcilingWithTransforms_single() throws Exception {
        IPath[] paths = createGroovyProject();

        //@formatter:off
        IPath foo = env.addGroovyClass(paths[1], "Foo",
            "@Singleton\n" +
            "class Foo {\n" +
            "  void mone() {}\n" +
            "}\n");
        //@formatter:on

        fullBuild(paths[0]);
        ICompilationUnit icu = env.getUnit(foo);

        ClassNode cn = ((GroovyCompilationUnit) icu).getModuleNode().getClasses().get(0);
        assertContainsMethod(cn, "getInstance");
    }

    @Test
    public void testReconcilingWithTransforms_multiple() throws Exception {
        IPath[] paths = createGroovyProject();

        //@formatter:off
        IPath foo = env.addGroovyClass(paths[1], "Foo",
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

        ClassNode cn = ((GroovyCompilationUnit) icu).getModuleNode().getClasses().get(0);
        assertContainsMethod(cn, "getInstance");
        assertContainsMethod(cn, "method");
    }

    @Test
    public void testReconcilingWithTransforms_typeChecked() throws Exception {
        IPath[] paths = createGroovyProject();

        //@formatter:off
        IPath foo = env.addGroovyClass(paths[1], "Foo",
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

        //@formatter:off
        IPath foo = env.addGroovyClass(paths[1], "Foo",
            "@groovy.transform.CompileStatic\n" +
            "class Foo {\n" +
            "  void xxx(int i) { xxx('abc') }\n" +
            "}\n");
        //@formatter:on

        fullBuild(paths[0]);
        Set<IProblem> problems = ReconcilerUtils.reconcile(env.getUnit(foo));
        assertContainsProblem(problems, "Cannot find matching method Foo#xxx");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-10075
    public void testDefaultGroovyMethodFromProjectDependency() throws Exception {
        IPath one = env.addProject("One");
        env.addGroovyJars(one);

        IPath src = env.getPackageFragmentRootPath(one, "src");
        env.addGroovyClass(src, "p", "X",
            "package p\n" +
            "class X {\n" +
            "  static String getString(Iterable<String> self) {\n" +
            "  }\n" +
            "  static <CS extends CharSequence> CharSequence getSequence(Iterable<CS> self) {\n" +
            "  }\n" +
            "}\n");
        env.addFile(env.addFolder(src, "META-INF/groovy"), "org.codehaus.groovy.runtime.ExtensionModule",
            "moduleName=ecks\n" +
            "moduleVersion=1.0\n" +
            "extensionClasses=p.X\n");

        env.fullBuild(one);
        expectingNoProblemsFor(one);

        //

        IPath two = env.addProject("Two");
        env.addGroovyJars(two);
        env.addRequiredTestProject(two, one);
        src = env.getPackageFragmentRootPath(two, "src");

        IPath bar = env.addGroovyClass(src, "foo", "Bar",
            "package foo\n" +
            "class Bar {\n" +
            "  @groovy.transform.TypeChecked\n" +
            "  void test() {\n" +
            "    List<String> strings = []\n" +
            "    strings.getSequence()\n" +
            "    strings.getString()\n" +
            "    strings.sequence\n" +
            "    strings.string\n" +
            "  }\n" +
            "}\n");

        IPath baz = env.addGroovyClass(src, "foo", "Baz",
            "package foo\n" +
            "class Baz {\n" +
            "  @groovy.transform.TypeChecked\n" +
            "  void test() {\n" +
            "    List<Number> numbers = []\n" +
            "    numbers.getSequence()\n" +
            "    numbers.getString()\n" +
            "    numbers.sequence\n" +
            "    numbers.string\n" +
            "  }\n" +
            "}\n");

        env.fullBuild(two);
        expectingNoProblemsFor(bar);
        expectingProblemsFor(baz, java.util.Arrays.asList(
            "Problem : Groovy:[Static type checking] - Cannot call <CS extends java.lang.CharSequence> p.X#getSequence(##) with arguments [java.util.ArrayList<java.lang.Number>] ##",
            "Problem : Groovy:[Static type checking] - Cannot call p.X#getString(##) with arguments [java.util.ArrayList<java.lang.Number>] ##",
            "Problem : Groovy:[Static type checking] - No such property: sequence for class: java.util.ArrayList ##",
            "Problem : Groovy:[Static type checking] - No such property: string for class: java.util.ArrayList ##"));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1409
    public void testLocalTransformationFromTestBuildPathEntry() throws Exception {
        IPath[] paths = createGroovyProject();

        env.addGroovyClass(paths[1], "p", "Main",
            "package p\n" +
            "import java.lang.annotation.*\n" +
            "import org.codehaus.groovy.ast.*\n" +
            "import org.codehaus.groovy.ast.builder.AstBuilder\n" +
            "import org.codehaus.groovy.control.CompilePhase\n" +
            "import org.codehaus.groovy.control.SourceUnit\n" +
            "import org.codehaus.groovy.transform.ASTTransformation\n" +
            "import org.codehaus.groovy.transform.GroovyASTTransformation\n" +
            "import org.codehaus.groovy.transform.GroovyASTTransformationClass\n" +
            "import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod\n" +
            "\n" +
            "@GroovyASTTransformationClass('p.MainMethodTransform')\n" +
            "@Retention(RetentionPolicy.SOURCE)\n" +
            "@Target(ElementType.METHOD)\n" +
            "@interface Main {\n" +
            "}\n" +
            "\n" +
            "@GroovyASTTransformation\n" +
            "class MainMethodTransform implements ASTTransformation {\n" +
            "  @Override\n" +
            "  void visit(ASTNode[] nodes, SourceUnit source) {\n" +
            "    if (!nodes || !nodes[0] || !nodes[1]) return\n" +
            "    if (!(nodes[1] instanceof MethodNode)) return\n" +
            "    if (!(nodes[0] instanceof AnnotationNode)) return\n" +
            "    if (nodes[0].classNode?.name != Main.name) return\n" +
            "    try {\n" +
            "      MethodNode annotatedMethod = nodes[1]\n" +
            "      addGeneratedMethod(annotatedMethod.declaringClass, newMainMethod(annotatedMethod))\n" +
            "    } catch (e) {\n" +
            "      e.printStackTrace()\n" +
            "    }\n" +
            "  }\n" +
            "  private static MethodNode newMainMethod(MethodNode target) {\n" +
            "    def packageName = target.declaringClass.packageName\n" +
            "    def   className = target.declaringClass.name\n" +
            "    def  methodName = target.name\n" +
            "    \n" +
            "    def ast = new AstBuilder().buildFromString(CompilePhase.CANONICALIZATION, false, \"\"\"\n" +
            "      ${packageName ? 'package ' + packageName : ''}\n" +
            "      \n" +
            "      class ${target.declaringClass.nameWithoutPackage} {\n" +
            "        public static void main(String[] args) {\n" +
            "          new ${className}().${methodName}()\n" +
            "        }\n" +
            "      }\n" +
            "    \"\"\")\n" +
            "    // ast[0] is BlockStatement\n" +
            "    return (ast[1] as ClassNode).getDeclaredMethods('main')[0]\n" +
            "  }\n" +
            "}\n");
        env.fullBuild(paths[0]);
        expectingNoProblemsFor(paths[0]);

        //

        IPath shm = env.addGroovyClass(env.addTestPackageFragmentRoot(paths[0], "tests"), "q", "ShouldHaveMain",
            "package q\n" +
            "import p.Main\n" +
            "class ShouldHaveMain {\n" +
            "  @Main def foo() {\n" +
            "    println 'bar'\n" +
            "  }\n" +
            "}\n");
        env.incrementalBuild(paths[0]);
        expectingNoProblemsFor(paths[0]);

        assertContainsMethod(((GroovyCompilationUnit) env.getUnit(shm)).getModuleNode().getClasses().get(0), "main");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/903
    public void testGlobalTransformationFromTestBuildPathEntry() throws Exception {
        IPath one = env.addProject("One");
        env.addGroovyJars(one);

        IPath src = env.getPackageFragmentRootPath(one, "src");
        env.addGroovyClass(src, "p", "X",
            "package p\n" +
            "import org.codehaus.groovy.ast.ASTNode\n" +
            "import org.codehaus.groovy.control.SourceUnit\n" +
            "@org.codehaus.groovy.transform.GroovyASTTransformation\n" +
            "class X implements org.codehaus.groovy.transform.ASTTransformation {\n" +
            "  void visit(ASTNode[] nodes, SourceUnit source) {\n" +
            "    throw new IllegalStateException('xform fails')\n" +
            "  }\n" +
            "}\n");
        env.addFile(env.addFolder(src, "META-INF/services"), "org.codehaus.groovy.transform.ASTTransformation", "p.X\n");
        env.fullBuild(one);
        expectingNoProblemsFor(one);

        //

        IPath two = env.addProject("Two");
        env.addGroovyJars(two);
        env.addRequiredTestProject(two, one);
        src = env.getPackageFragmentRootPath(two, "src");
        env.addGroovyClass(src, "foo", "Bar", "package foo\nclass Bar {}\n");
        IPath tests = env.addGroovyClass(env.addTestPackageFragmentRoot(two, "tests"), "foo", "Baz", "package foo\nclass Baz {}\n");

        // global transform p.X from One should not run for non-test source foo/Bar.groovy
        env.fullBuild(two);
        expectingNoProblemsFor(src);
        expectingProblemsFor(tests, java.util.Arrays.asList(
            "## exception in phase 'canonicalization' in source unit '##' xform fails" +
            " [ resource : </Two/tests/foo/Baz.groovy> range : <0,1> category : <60> severity : <2>]"));
    }
}
