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
package org.eclipse.jdt.core.groovy.tests.builder;

import static org.codehaus.groovy.eclipse.core.model.RequireModuleOperation.requireModule;
import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.builder.AbstractImageBuilder;
import org.eclipse.jdt.launching.JavaRuntime;
import org.junit.After;
import org.junit.Test;
import org.osgi.framework.Version;

/**
 * Basic tests for the builder - compiling and running some very simple java and groovy code
 */
public final class BasicGroovyBuildTests extends BuilderTestSuite {

    private IPath[] createModularProject(final String name, final boolean isGroovy) throws Exception {
        assumeTrue(JavaCore.compareJavaVersions(System.getProperty("java.version"), "9") >= 0);

        IPath prjPath = env.addProject(name, "9");
        if (isGroovy) {
            env.addGroovyJars(prjPath);
        } else {
            env.removeGroovyNature(name);
        }
        IPath srcPath = env.getPackageFragmentRootPath(prjPath, "src");
        try {
            return new IPath[] {prjPath, srcPath, env.addClass(srcPath,
                "module-info", "module " + name.toLowerCase() + " {\n}\n")};
        } finally {
            assumeTrue(JavaRuntime.isModule(env.getRawClasspath(prjPath)[0], env.getJavaProject(prjPath)));
        }
    }

    private IPath[] createSimpleProject(final String name, final boolean isGroovy) throws Exception {
        IPath prjPath = env.addProject(name);
        if (isGroovy) {
            env.addGroovyJars(prjPath);
        } else {
            env.removeGroovyNature(name);
        }
        fullBuild(prjPath);

        return new IPath[] {prjPath, env.getPackageFragmentRootPath(prjPath, "src")};
    }

    private void addJUnitAndSpock(final IPath projectPath) throws Exception {
        int jUnitVersion = 4;
        String spockCorePath;
        if (isAtLeastGroovy(30)) {
            jUnitVersion = 5;
            spockCorePath = "lib/spock-core-2.0-M4-groovy-3.0.jar";
            if (isAtLeastGroovy(40)) {
                System.setProperty("spock.iKnowWhatImDoing.disableGroovyVersionCheck", "true");
            }
        } else {
            spockCorePath = "lib/spock-core-1.3-groovy-2.5.jar";
        }
        env.addJar(projectPath, spockCorePath);

        env.addEntry(projectPath, JavaCore.newContainerEntry(
            new Path("org.eclipse.jdt.junit.JUNIT_CONTAINER/" + jUnitVersion)));
    }

    // check whether these are identical (in everything except name!)
    private static void compareClassNodes(final ClassNode jcn, final ClassNode cn, final int d) {
        assertEquals(cn.isGenericsPlaceHolder(), jcn.isGenericsPlaceHolder());

        GenericsType[] cnGenerics = cn.getGenericsTypes();
        GenericsType[] jcnGenerics = jcn.getGenericsTypes();
        if (cnGenerics == null) {
            if (jcnGenerics != null) {
                fail("Should have been null but was " + Arrays.toString(jcnGenerics));
            }
        } else {
            if (jcnGenerics == null) {
                fail("Did not expect genericstypes to be null, should be " + Arrays.toString(cnGenerics));
            }
            assertNotNull(jcnGenerics);
            assertEquals(cnGenerics.length, jcnGenerics.length);
            for (int i = 0, n = cnGenerics.length; i < n; i += 1) {
                compareGenericsTypes(jcnGenerics[i], cnGenerics[i], d + 1);
            }
        }
    }

    private static void compareGenericsTypes(final GenericsType jgt, final GenericsType gt, final int d) {
        assertEquals(jgt.getName(), gt.getName());
        assertEquals(jgt.isPlaceholder(), gt.isPlaceholder());
        assertEquals(jgt.isResolved(), gt.isResolved());
        assertEquals(jgt.isWildcard(), gt.isWildcard());
        compareType(jgt.getType(), gt.getType(), d + 1);
        compareLowerBound(jgt.getLowerBound(), gt.getLowerBound(), d + 1);
        compareUpperBounds(jgt.getUpperBounds(), gt.getUpperBounds(), d + 1);
    }

    private static void compareType(final ClassNode jcn, final ClassNode cn, final int d) {
        compareClassNodes(jcn, cn, d + 1);
    }

    private static void compareLowerBound(final ClassNode jcn, final ClassNode cn, final int d) {
        if (jcn == null) {
            assertNull(cn);
        } else {
            assertNotNull(cn);
            compareClassNodes(jcn.redirect(), cn.redirect(), d + 1);
        }
    }

    private static void compareUpperBounds(final ClassNode[] jcnlist, final ClassNode[] cnlist, final int d) {
        if (cnlist == null) {
            if (jcnlist != null) {
                fail("Should be null but is " + Arrays.toString(jcnlist));
            }
        } else {
            if (jcnlist == null) {
                fail("Array not expected to be null, should be " + Arrays.toString(cnlist));
            }
            assertEquals(cnlist.length, cnlist.length);
            for (int i = 0; i < cnlist.length; i++) {
                compareClassNodes(jcnlist[i].redirect(), cnlist[i].redirect(), d + 1);
            }
        }
    }

    private static String toTask(final String tasktag, final String message) {
        return tasktag + message;
    }

    @After
    public void tearDown() {
        JDTResolver.recordInstances = false;
    }

    //--------------------------------------------------------------------------

    @Test
    public void testBuildJavaHelloWorld() throws Exception {
        IPath[] paths = createSimpleProject("Project", false);

        //@formatter:off
        env.addClass(paths[1], "p1", "Hello",
            "package p1;\n" +
            "public class Hello {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.println(\"Hello world\");\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p1.Hello");
        expectingNoProblems();
        executeClass(paths[0], "p1.Hello", "Hello world", "");
    }

    @Test // build .groovy file hello world then run it
    public void testBuildGroovyHelloWorld() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p1", "Hello",
            "package p1\n" +
            "class Hello {\n" +
            "  static void main(String[] args) {\n" +
            "    print \"Hello world\"\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p1.Hello");
        expectingNoProblems();
        executeClass(paths[0], "p1.Hello", "Hello world", null);
    }

    @Test // uses alternate main method syntax
    public void testBuildGroovyHelloWorld2() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p1", "Hello",
            "package p1\n" +
            "class Hello {\n" +
            "  static main(args) {\n" +
            "    print 'Hello world'\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p1.Hello");
        expectingNoProblems();
        executeClass(paths[0], "p1.Hello", "Hello world", null);
    }

    @Test
    public void testBuildGroovyHelloWorld3() throws Exception {
        IPath[] paths = createModularProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p1", "Hello",
            "package p1\n" +
            "class Hello {\n" +
            "  static main(args) {\n" +
            "    print 'Hello world'\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        fullBuild(paths[0]);

        expectingProblemsFor(paths[0], Arrays.asList(
            "Problem : The project was not built since its build path is incomplete. Cannot find the class file for groovy.lang.GroovyObject." +
                " Fix the build path then try building this project [ resource : </Project> range : <-1,-1> category : <10> severity : <2>]",
            "Problem : The type groovy.lang.GroovyObject cannot be resolved. It is indirectly referenced from required .class files" +
                " [ resource : </Project/src/p1/Hello.groovy> range : <0,1> category : <10> severity : <2>]"
        ));
    }

    @Test
    public void testBuildGroovyHelloWorld4() throws Exception {
        IPath[] paths = createModularProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p1", "Hello",
            "package p1\n" +
            "print 'hello world'\n");
        //@formatter:on

        fullBuild(paths[0]);

        expectingProblemsFor(paths[0], Arrays.asList(
            "Problem : The project was not built since its build path is incomplete. Cannot find the class file for groovy.lang.GroovyObject." +
                " Fix the build path then try building this project [ resource : </Project> range : <-1,-1> category : <10> severity : <2>]",
            "Problem : The type groovy.lang.GroovyObject cannot be resolved. It is indirectly referenced from required .class files" +
                " [ resource : </Project/src/p1/Hello.groovy> range : <0,1> category : <10> severity : <2>]"
        ));
    }

    @Test
    public void testBuildGroovyHelloWorld5() throws Exception {
        IPath[] paths = createModularProject("Project", true);
        requireModule(env.getJavaProject(paths[0]), "org.codehaus.groovy");

        //@formatter:off
        env.addGroovyClass(paths[1], "p1", "Hello",
            "package p1\n" +
            "print 'hello world'\n");
        //@formatter:on

        fullBuild(paths[0]);
        expectingNoProblems();
    }

    @Test
    public void testProjectCompilerConfigScript1() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addFile(paths[0], "config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    normal 'java.util.regex.Pattern'\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, "config.groovy");
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        env.addGroovyClass(paths[1], "foo", "Bar",
            "package foo\n" +
            "class Bar {\n" +
            "  Pattern baz\n" +
            "}\n");

        incrementalBuild(paths[0]);
        expectingCompiledClasses("foo.Bar");
        expectingNoProblems();

        // add file outside of source folder
        env.addFile(paths[0], "Err.groovy",
            "class Err {\n" +
            "  Pattern baz\n" +
            "}\n");

        incrementalBuild(paths[0]);
        expectingCompiledClasses();
        expectingNoProblems();

        // TODO: Can it be shown that config.groovy is not applied when parsing non-classpath resources?
    }

    @Test
    public void testProjectCompilerConfigScript2() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addFile(paths[0], "config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    staticMember 'java.util.regex.Pattern', 'compile'\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, "config.groovy");
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        env.addGroovyClass(paths[1], "foo", "Bar",
            "package foo\n" +
            "class Bar {\n" +
            "  def pattern = compile('regexp')\n" +
            "}\n");

        incrementalBuild(paths[0]);
        expectingCompiledClasses("foo.Bar");
        expectingNoProblems();
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/550
    public void testProjectBasedirAsOutputLocation() throws Exception {
        IPath prj = env.addProject("Project");
        env.removePackageFragmentRoot(prj, "src");
        env.addPackageFragmentRoot(prj, "");
        env.setOutputFolder(prj, "");
        env.addGroovyJars(prj);

        //@formatter:off
        env.addGroovyClass(prj, "p", "Script",
            "package p\n" +
            "println 'Groovy!'\n");
        //@formatter:on

        fullBuild(prj);
        expectingNoProblems();
        expectingCompiledClasses("p.Script");
        executeClass(prj, "p.Script", "Groovy!", null);
    }

    @Test
    public void testInners_983() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "Outer",
            "class Outer {\n" +
            "  static class Inner {}\n" +
            "}\n");
        //@formatter:on

        //@formatter:off
        env.addClass(paths[1], "Client",
            "public class Client {\n" +
            "  { new Outer.Inner(); }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("Client", "Outer", "Outer$Inner");
        expectingNoProblems();

        //@formatter:off
        env.addClass(paths[1], "", "Client",
            "public class Client {\n" +
            "  { new Outer.Inner(); }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("Client");
    }

    @Test
    public void testNPEAnno_1398() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addClass(paths[1], "", "Anno",
            "public @interface Anno {\n" +
            "  String[] value();\n" +
            "}");
        //@formatter:on

        //@formatter:off
        env.addGroovyClass(paths[1], "Const",
            "public class Const {\n" +
            "  public static final String instance= \"abc\";\n" +
            "}");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("Anno", "Const");
        expectingNoProblems();

        //@formatter:off
        env.addGroovyClass(paths[1], "A",
            "@Anno(Const.instance)\n" +
            "class A {}");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("A");
    }

    @Test
    public void testGenericsDefaultParams_1717() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addClass(paths[1], "p1", "Demo",
            "package p1;\n" +
            "public class Demo {\n" +
            "   public static void main(String[] args) {\n" +
            "      SomeGroovyHelper.doit(String.class);\n" +
            "   }\n" +
            "}\n");
        //@formatter:on

        //@formatter:off
        env.addGroovyClass(paths[1], "p1", "SomeGroovyHelper",
            "package p1;\n" +
            "class SomeGroovyHelper {\n" +
            "   static <T> List<T> doit(Class<T> factoryClass, ClassLoader classLoader = SomeGroovyHelper.class.classLoader) {\n" +
            "      null\n" +
            "   }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p1.Demo", "p1.SomeGroovyHelper");
        expectingNoProblems();
    }

    @Test
    public void testCompileStatic1() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "Outer",
            "import groovy.transform.CompileStatic;\n" +
            "@CompileStatic \n" +
            "int fact(int n) {\n" +
            "  if (n==1) {return 1;\n" +
            "  } else {return n+fact(n-1);}\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("Outer");
        expectingNoProblems();
    }

    @Test // verify generics are correct for the 'Closure<?>' as CompileStatic will attempt an exact match
    public void testCompileStatic2() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "A",
            "class A {\n" +
            "  public void profile(String name, groovy.lang.Closure<?> callable) {}\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("A");

        //@formatter:off
        env.addGroovyClass(paths[1], "B",
            "@groovy.transform.CompileStatic\n" +
            "class B extends A {\n" +
            "\n" +
            "  def foo() {\n" +
            "    profile(\"creating plugin manager with classes\") {\n" +
            "      System.out.println('abc');\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("B", "B$_foo_closure1");
    }

    @Test
    public void testCompileStatic3() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "Foo",
            "class Foo {\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  public static void main(String[] args) {\n" +
            "    ((GroovyObject)new Foo());\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("Foo");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/771
    public void testCompileStatic771() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addClass(env.addPackageFragmentRoot(paths[0], "src2", null, "bin2"), "foobar", "DefaultRunnable",
            "package foobar;\n" +
            "public class DefaultRunnable implements Runnable {\n" +
            "  @Override public void run() {\n" +
            "  }\n" +
            "}");
        //@formatter:on

        env.addGroovyClass(paths[1], "foobar", "UtilityClass",
            "package foobar\n" +
            "@groovy.transform.CompileStatic\n" +
            "public final class UtilityClass {\n" +
            "  public static void doIt(Runnable runner = null) {\n" +
            "    runner = runner ?: new DefaultRunnable()\n" + // DefaultRunnable is a Java type, so it's not in the CompileUnit
            "  }\n" +
            "}");

        fullBuild(paths[0]);
        expectingNoProblems();
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/863
    public void testCompileStatic_9058() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addClass(paths[1], "p", "Foo",
            "package p;\n" +
            "public class Foo {\n" +
            "  @SuppressWarnings(\"rawtypes\")\n" +
            "  public java.util.List bar() { return null; }\n" +
            "}\n");
        //@formatter:on

        //@formatter:off
        env.addGroovyClass(paths[1], "p", "Main",
            "package p\n" +
            "class Main {\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  void meth() {\n" +
            "    List<Object[]> rows = new Foo().bar()\n" +
            "    rows.each { row ->\n" + // should be Object[]
            "      def col = row[0]\n" +
            "    }\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        fullBuild(paths[0]);
        expectingNoProblems();
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9079
    public void testCompileStatic_9079() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p", "Main",
            "package p\n" +
            "class Main {\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  void meth() {\n" +
            "    java.util.concurrent.Callable<String> task = { -> '' }\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        fullBuild(paths[0]);
        expectingNoProblems();
    }

    @Test
    public void testCompileStatic_ArrayArray() throws Exception {
        JDTResolver.recordInstances = true;
        IPath[] paths = createSimpleProject("Project", true);

        env.addClass(paths[1], "", "ISourceRange",
            "class ISourceRange {}");

        env.addClass(paths[1], "", "TypeNameMatch",
            "class TypeNameMatch {}");

        //@formatter:off
        env.addClass(paths[1], "", "IChooseImportQuery",
            "interface IChooseImportQuery {\n" +
            "    TypeNameMatch[] chooseImports(TypeNameMatch[][] matches, ISourceRange[] range);\n" +
            "}");
        //@formatter:on

        //@formatter:off
        env.addGroovyClass(paths[1], "NoChoiceQuery",
            "class NoChoiceQuery implements IChooseImportQuery {\n" +
            "    public TypeNameMatch[] chooseImports(TypeNameMatch[][] matches, ISourceRange[] range) {\n" +
            "        throw new Exception(\"Should not have a choice, but found $matches[0][0] and $matches[0][1]\")\n" +
            "        return []\n" +
            "    }\n" +
            "}");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("IChooseImportQuery", "ISourceRange", "NoChoiceQuery", "TypeNameMatch");
    }

    @Test
    public void testCompileStatic_FileAddAll() throws Exception {
        JDTResolver.recordInstances = true;
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "Foo",
            "import groovy.transform.CompileStatic\n" +
            "@CompileStatic\n" +
            "class Foo {\n" +
            "List<String> jvmArgs = new ArrayList<String>();\n" +
            "  void method(String message) {\n" +
            "    List<String> cmd = ['java'];\n" +
            "    cmd.addAll(jvmArgs);\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("Foo");
        // TODO: compare the generics structure for List (built by jdtresolver mapping into groovy) against List2 (built by groovy)
    }

    @Test
    public void testCompileStatic_MapEachClosure() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "Demo",
            "@groovy.transform.CompileStatic\n" +
            "class Demo {\n" +
            "  void doit() {\n" +
            "    def c = {\n" +
            "      Map<String, String> data = [:]\n" +
            "      Map<String, Set<String>> otherData = [:]\n" +
            "      data.each { String k, String v ->\n" +
            "        def foo = otherData.get(k)\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
    }

    @Test
    public void testCompileStatic_IterableParameter() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "Foo",
            "import groovy.transform.CompileStatic\n" +
            "@CompileStatic\n" +
            "class Foo {\n" +
            "  private populateSourceDirectories() {\n" +
            "    List<File> pluginDependencies\n" +
            "    foo(pluginDependencies);\n" +
            "  }\n" +
            "  private void foo(Iterable<File> iterable) {}\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("Foo");
        // TODO: compare the generics structure for List (built by jdtresolver mapping into groovy) against List2 (built by groovy)
    }

    @Test
    public void testCompileStatic_ListFileArgIteratedOver() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "Foo",
            "import groovy.transform.CompileStatic\n" +
            "class Foo {\n" +
            "@CompileStatic\n" +
            "private populateSourceDirectories() {\n" +
            "  List<File> pluginDependencies\n" +
            "  for (zip in pluginDependencies) {\n" +
            "    registerPluginZipWithScope(zip);\n" +
            "  }\n" +
            "}\n" +
            "private void registerPluginZipWithScope(File pluginzip) {}\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("Foo");
        // TODO: compare the generics structure for List (built by jdtresolver mapping into groovy) against List2 (built by groovy)
    }

    @Test
    public void testCompileStatic_BuildSettings() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "BuildSettings",
            "import groovy.transform.CompileStatic\n" +
            "\n" +
            "class BuildSettings  {\n" +
            "\n" +
            "  List<File> compileDependencies = []\n" +
            "  List<File> defaultCompileDependencies = []\n" +
            "\n" +
            "  @CompileStatic\n" +
            "  void getCompileDependencies() {\n" +
            "    compileDependencies += defaultCompileDependencies\n" +
            "  }\n" +
            "\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("BuildSettings");
        // TODO: compare the generics structure for List (built by jdtresolver mapping into groovy) against List2 (built by groovy)
    }

    @Test
    public void test1167() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "brooklyn.event.adapter", "HttpSensorAdapter",
            "package brooklyn.event.adapter\n" +
            "\n" +
            "public class HttpSensorAdapter {}\n" +
            "\n" +
            "public class Foo implements ValueProvider<String> {\n" +
            "public String compute() {\n" +
            "  return null\n" +
            "}\n" +
            "}");
        //@formatter:on

        //@formatter:off
        env.addGroovyClass(paths[1], "brooklyn.event.adapter", "ValueProvider",
            "package brooklyn.event.adapter\n" +
            "\n" +
            "public interface ValueProvider<T> {\n" +
            "  public T compute();\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("brooklyn.event.adapter.Foo", "brooklyn.event.adapter.HttpSensorAdapter", "brooklyn.event.adapter.ValueProvider");
        expectingNoProblems();

        //@formatter:off
        env.addGroovyClass(paths[1], "brooklyn.event.adapter", "HttpSensorAdapter",
            "package brooklyn.event.adapter\n" +
            "\n" +
            "public class HttpSensorAdapter {}\n" +
            "\n" +
            "public class Foo implements ValueProvider<String> {\n" +
            "public String compute() {\n" +
            "  return null\n" +
            "}\n" +
            "}");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("brooklyn.event.adapter.Foo", "brooklyn.event.adapter.HttpSensorAdapter");
    }

    @Test
    public void testTypeDuplication_GRE796_1() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p", "Foo",
            "package p;\n" +
            "class Foo {}\n");
        //@formatter:on

        IPath root2 = env.addPackageFragmentRoot(paths[0], "src2");

        //@formatter:off
        IPath pathToSecond = env.addGroovyClass(root2, "p", "Foo",
            "package p;\n" +
            "class Foo { }\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        Problem[] probs = env.getProblemsFor(pathToSecond);
        boolean p1found = false;
        boolean p2found = false;
        for (int i = 0; i < probs.length; i += 1) {
            if (probs[i].getMessage().equals("The type Foo is already defined")) {
                p1found = true;
            }
            if (probs[i].getMessage().startsWith("Groovy:Invalid duplicate class definition of class p.Foo")) {
                p2found = true;
            }
        }
        if (!p1found) {
            printProblemsFor(pathToSecond);
            fail("Didn't get expected message 'The type Foo is already defined'\n");
        }
        if (!p2found) {
            printProblemsFor(pathToSecond);
            fail("Didn't get expected message 'Groovy:Invalid duplicate class definition of class p.Foo'\n");
        }
    }

    @Test
    public void testTypeDuplication_GRE796_2() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p", "Foo",
            "package p;\n" +
            "class Foo {}\n");
        //@formatter:on

        IPath root2 = env.addPackageFragmentRoot(paths[0], "src2");

        //@formatter:off
        IPath pathToSecond = env.addGroovyClass(root2, "p", "Foo",
            "package p;\n" +
            "class Foo { }\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        Problem[] probs = env.getProblemsFor(pathToSecond);
        boolean p1found = false;
        boolean p2found = false;
        for (int i = 0; i < probs.length; i += 1) {
            if (probs[i].getMessage().equals("The type Foo is already defined")) {
                p1found = true;
            }
            if (probs[i].getMessage().startsWith("Groovy:Invalid duplicate class definition of class p.Foo")) {
                p2found = true;
            }
        }
        if (!p1found) {
            printProblemsFor(pathToSecond);
            fail("Didn't get expected message 'The type Foo is already defined'\n");
        }
        if (!p2found) {
            printProblemsFor(pathToSecond);
            fail("Didn't get expected message 'Groovy:Invalid duplicate class definition of class p.Foo'\n");
        }
    }

    @Test // script has no package statement
    public void testClashingPackageAndType_1214() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        IPath cuPath = env.addClass(paths[1], "com.acme", "Foo",
            "package com.acme;\n" +
            "public class Foo {}\n");
        //@formatter:on

        env.addGroovyClass(paths[1], "com.acme.Foo", "xyz",
            "print 'abc'");

        boolean conflictIsError = (JavaCore.getPlugin().getBundle().getVersion().compareTo(Version.parseVersion("3.19")) >= 0);

        incrementalBuild(paths[0]);
        expectingSpecificProblemFor(cuPath, new Problem("", "The type Foo collides with a package", cuPath, 31, 34,
            CategorizedProblem.CAT_TYPE, (conflictIsError ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING)));
        expectingCompiledClasses("com.acme.Foo", "xyz");
        executeClass(paths[0], "xyz", "abc", null);
    }

    @Test
    public void testSlowAnotherAttempt_GRE870() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        env.addGroovyClass(paths[1], "a.b.c.d.e.f", "Helper1",
            "package a.b.c.d.e.f\n" +
            "class Helper1 {}\n");
        env.addGroovyClass(paths[1], "a.b.c.d.e.f", "Helper2",
            "package a.b.c.d.e.f\n" +
            "class Helper2 {}\n");
        env.addGroovyClass(paths[1], "a.b.c.d.e.f", "Helper3",
            "package a.b.c.d.e.f\n" +
            "class Helper3 {}\n");
        env.addGroovyClass(paths[1], "a.b.c.d.e.f", "Helper4",
            "package a.b.c.d.e.f\n" +
            "class Helper4 {}\n");
        env.addGroovyClass(paths[1], "a.b.c.d.e.f", "Helper5",
            "package a.b.c.d.e.f\n" +
            "class Helper5 {}\n");
        env.addGroovyClass(paths[1], "a.b.c.d.e.f", "Helper6",
            "package a.b.c.d.e.f\n" +
            "class Helper6 {}\n");
        env.addGroovyClass(paths[1], "a.b.c.d.e.f", "Helper7",
            "package a.b.c.d.e.f\n" +
            "class Helper7 {}\n");
        env.addGroovyClass(paths[1], "a.b.c.d.e.f", "Helper8",
            "package a.b.c.d.e.f\n" +
            "class Helper8 {}\n");
        env.addGroovyClass(paths[1], "a.b.c.d.e.f", "Helper9",
            "package a.b.c.d.e.f\n" +
            "class Helper9 {}\n");
        //@formatter:off
        env.addGroovyClass(paths[1], "a.b.c.d.e.f", "HelperBase",
            "package a.b.c.d.e.f\n" +
            "class HelperBase {\n" +
            "  static final String TYPE = 'test'\n" +
            "}\n");
        //@formatter:on
        env.addGroovyClass(paths[1], "a.b.c.d.e.f", "SomeHelper",
            "package a.b.c.d.e.f\n" +
            "class SomeHelper extends HelperBase {}\n");
        //@formatter:off
        env.addGroovyClass(paths[1], "a.b.c.d.e.f", "SomeChecks",
            "package a.b.c.d.e.f\n" +
            "\n" +
            "import static a.b.c.d.e.f.Helper1.*\n" +
            "import static a.b.c.d.e.f.Helper2.*\n" +
            "import static a.b.c.d.e.f.Helper3.*\n" +
            "import static a.b.c.d.e.f.Helper4.*\n" +
            "import static a.b.c.d.e.f.Helper5.*\n" +
            "import static a.b.c.d.e.f.Helper6.*\n" +
            "import static a.b.c.d.e.f.Helper7.*\n" +
            "import static a.b.c.d.e.f.Helper8.*\n" +
            "import static a.b.c.d.e.f.Helper9.*\n" +
            "\n" +
            "import static a.b.c.d.e.f.SomeHelper.*\n" +
            "\n" +
            "class SomeChecks {\n" +
            "\n" +
            "    public void test1() {\n" +
            "    def details = [:]\n" +
            "\n" +
            "        assert details[TYPE] == 'test' \n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test' \n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "        assert details[TYPE] == 'test'\n" +
            "    }\n" +
            "}\n");
        //@formatter:on

        // TODO: How to create a reliable timed test? This should take about 2-3 seconds, not more than 10 -- at least on my machine...

        incrementalBuild(paths[0]);
        // lots of errors on the missing static imports
        expectingCompiledClasses(
            "a.b.c.d.e.f.Helper1",
            "a.b.c.d.e.f.Helper2",
            "a.b.c.d.e.f.Helper3",
            "a.b.c.d.e.f.Helper4",
            "a.b.c.d.e.f.Helper5",
            "a.b.c.d.e.f.Helper6",
            "a.b.c.d.e.f.Helper7",
            "a.b.c.d.e.f.Helper8",
            "a.b.c.d.e.f.Helper9",
            "a.b.c.d.e.f.HelperBase",
            "a.b.c.d.e.f.SomeChecks",
            "a.b.c.d.e.f.SomeHelper");
    }

    @Test
    public void testSlow_GRE870() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "Test1",
            "import static some.Class0.*;\n" +
            "import static some.Class1.*;\n" +
            "import static some.Class2.*;\n" +
            "import static some.Class3.*;\n" +
            "import static some.Class4.*;\n" +
            "import static some.Class5.*;\n" +
            "import static some.Class6.*;\n" +
            "import static some.Class7.*;\n" +
            "import static some.Class8.*;\n" +
            "import static some.Class9.*;\n" +
            "import static some.Class10.*;\n" +
            "\n" +
            "class Test1 {}\n");
        //@formatter:on

        env.addGroovyClass(paths[1], "some", "Foo",
            "\n" +
            "class Foo {}\n");

        env.setOutputFolder(paths[0], "bin");

        incrementalBuild(paths[0]);
        // lots of errors on the missing static imports
        expectingCompiledClasses("Foo", "Test1");
    }

    @Test
    public void testReallySlow_GRE870() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "Test1",
            "import static some.Class0.*;\n" +
            "import static some.Class1.*;\n" +
            "import static some.Class2.*;\n" +
            "import static some.Class3.*;\n" +
            "import static some.Class4.*;\n" +
            "import static some.Class5.*;\n" +
            "import static some.Class6.*;\n" +
            "import static some.Class7.*;\n" +
            "import static some.Class8.*;\n" +
            "import static some.Class9.*;\n" +
            "import static some.Class10.*;\n" +
            "import static some.Class11.*;\n" +
            "import static some.Class12.*;\n" +
            "import static some.Class13.*;\n" +
            "import static some.Class14.*;\n" +
            "import static some.Class15.*;\n" +
            "import static some.Class16.*;\n" +
            "import static some.Class17.*;\n" +
            "import static some.Class18.*;\n" +
            "import static some.Class19.*;\n" +
            "import static some.Class20.*;\n" +
            "import static some.Class21.*;\n" +
            "import static some.Class22.*;\n" +
            "import static some.Class23.*;\n" +
            "import static some.Class24.*;\n" +
            "import static some.Class25.*;\n" +
            "import static some.Class26.*;\n" +
            "import static some.Class27.*;\n" +
            "import static some.Class28.*;\n" +
            "import static some.Class29.*;\n" +
            "import static some.Class30.*;\n" +
            "import static some.Class31.*;\n" +
            "import static some.Class32.*;\n" +
            "import static some.Class33.*;\n" +
            "import static some.Class34.*;\n" +
            "import static some.Class35.*;\n" +
            "import static some.Class36.*;\n" +
            "import static some.Class37.*;\n" +
            "import static some.Class38.*;\n" +
            "import static some.Class39.*;\n" +
            "import static some.Class40.*;\n" +
            "import static some.Class41.*;\n" +
            "import static some.Class42.*;\n" +
            "import static some.Class43.*;\n" +
            "import static some.Class44.*;\n" +
            "import static some.Class45.*;\n" +
            "import static some.Class46.*;\n" +
            "import static some.Class47.*;\n" +
            "import static some.Class48.*;\n" +
            "import static some.Class49.*;\n" +
            "import static some.Class50.*;\n" +
            "\n" +
            "class Test1 {}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("Test1");
        // lots of errors on the missing static imports
    }

    @Test
    public void testClosureBasics() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "Coroutine",
            "def iterate(n, closure) {\n" +
            "  1.upto(n) {\n" +
            "    closure(it);\n" +
            "  }\n" +
            "}\n" +
            "iterate (3) {\n" +
            "  print it*2\n" +
            "}\n");
        //@formatter:on

        // three classes created for that:
        // GroovyClass(name=Coroutine bytes=6372),
        // GroovyClass(name=Coroutine$_run_closure1 bytes=2875),
        // GroovyClass(name=Coroutine$_iterate_closure2 bytes=3178)

        incrementalBuild(paths[0]);
        expectingCompiledClasses("Coroutine", "Coroutine$_run_closure1", "Coroutine$_iterate_closure2");
        expectingNoProblems();
        executeClass(paths[0], "Coroutine", "246", "");
    }

    @Test
    public void testPackageNames1() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        IPath path = env.addGroovyClass(paths[1], "p", "X",
            "package q\n" + // q.X declared in p.X
            "class X {}");
        //@formatter:on

        incrementalBuild(paths[0]);

        expectingSpecificProblemFor(path, new Problem("p/X", "The declared package \"q\" does not match the expected package \"p\"",
            path, 8, 9, 60, IMarker.SEVERITY_ERROR));
    }

    @Test
    public void testPackageNames2() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        IPath path = env.addGroovyClass(paths[1], "p.q.r", "X",
            "package p.s.r.q\n" + // q.X declared in p.X
            "class X {}");
        //@formatter:on

        incrementalBuild(paths[0]);

        expectingSpecificProblemFor(path, new Problem("p/q/r/X", "The declared package \"p.s.r.q\" does not match the expected package \"p.q.r\"",
            path, 8, 15, 60, IMarker.SEVERITY_ERROR));
    }

    @Test
    public void testPackageNames3() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        // in p.q.r.X but has no package decl
        IPath path = env.addGroovyClass(paths[1], "p.q.r", "X",
            "print 'abc'");

        incrementalBuild(paths[0]);

        expectingSpecificProblemFor(path, new Problem("p/q/r/X", "The declared package \"\" does not match the expected package \"p.q.r\"",
            path, 0, 1, 60, IMarker.SEVERITY_ERROR));
    }

    @Test // script with no package statement should not have package problem marker
    public void testPackageNames4() throws Exception {
        IEclipsePreferences groovyPrefs = Activator.getInstancePreferences();
        groovyPrefs.putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, true);
        groovyPrefs.put(Activator.GROOVY_SCRIPT_FILTERS, "src/**/*.groovy,y");
        try {
            IPath[] paths = createSimpleProject("Project", true);

            IPath path = env.addGroovyClass(paths[1], "p.q.r", "Script",
                "print 'abc'");

            incrementalBuild(paths[0]);
            expectingNoProblemsFor(path);
        } finally {
            groovyPrefs.putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, false);
        }
    }

    @Test
    public void testAnnotationCollectorMultiProject() throws Exception {
        // Construct 'annotation' project that defines annotation using 'AnnotationsCollector'
        IPath[] paths = createSimpleProject("annotation", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "com.demo", "MyAnnotation",
            "package com.demo\r\n" +
            "\r\n" +
            "@groovy.transform.AnnotationCollector\r\n" +
            "@Deprecated\r\n" +
            "@interface MyAnnotation {}\r\n");
        //@formatter:on

        IPath annotationProject = paths[0];

        // Construct 'app' project that uses the annotation
        paths = createSimpleProject("app", true);

        env.addRequiredProject(paths[0], annotationProject);

        //@formatter:off
        env.addGroovyClass(paths[1], "com.demo", "Widget",
            "package com.demo\r\n" +
            "\r\n" +
            "@MyAnnotation\r\n" +
            "class Widget {}\r\n");
        //@formatter:on

        fullBuild();
        expectingNoProblems();
        expectingCompiledClasses("com.demo.MyAnnotation", "com.demo.MyAnnotation$CollectorHelper", "com.demo.Widget");
    }

    @Test
    public void testAnnotationCollectorIncremental() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        env.addGroovyClass(paths[1], "NotNull",
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME) @interface NotNull {}\n");

        env.addGroovyClass(paths[1], "Length",
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME) @interface Length {}\n");

        env.addGroovyClass(paths[1], "ISBN",
            "import java.lang.annotation.*;\n" +
            "@NotNull @Length @groovy.transform.AnnotationCollector @interface ISBN {}\n");

        //@formatter:off
        env.addGroovyClass(paths[1], "Book",
            "import java.lang.annotation.Annotation;\n" +
            "import java.lang.reflect.Field;\n" +
            "\n" +
            "class Book {\n" +
            "  @ISBN\n" +
            "  String isbn;\n" +
            "  \n" +
            "  public static void main(String[] args) {\n" +
            "    Field f = Book.class.getDeclaredField(\"isbn\");\n" +
            "    for (Annotation a: f.getDeclaredAnnotations()) {\n" +
            "      System.out.println(a);\n" +
            "    } \n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("Book", "Length", "NotNull", "ISBN", "ISBN$CollectorHelper");

        executeClass(paths[0], "Book", "@NotNull()\n@Length()\n", "");

        //@formatter:off
        env.addGroovyClass(paths[1], "Book",
            "import java.lang.annotation.Annotation;\n" +
            "import java.lang.reflect.Field;\n" +
            "\n" +
            "class Book {  \n" + // whitespace change
            "  @ISBN\n" +
            "  String isbn;\n" +
            "  \n" +
            "  public static void main(String[] args) {\n" +
            "    Field f = Book.class.getDeclaredField(\"isbn\");\n" +
            "    for (Annotation a: f.getDeclaredAnnotations()) {\n" +
            "      System.out.println(a);\n" +
            "    } \n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("Book");
        expectingNoProblems();
        executeClass(paths[0], "Book", "@NotNull()\n@Length()\n", "");
    }

    @Test
    public void testClosureIncremental() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addClass(paths[1], "", "Launch",
            "public class Launch {\n" +
            "  public static void main(String[] args) {\n" +
            "    Runner.run(3);\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        //@formatter:off
        env.addGroovyClass(paths[1], "Runner",
            "def static run(int n) { \n" +
            "  OtherGroovy.iterate(4) {\n" +
            "    print it*2\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        //@formatter:off
        env.addGroovyClass(paths[1], "OtherGroovy",
            "def static iterate(Integer n, closure) {\n" +
            "  1.upto(n) {\n" +
            "    closure(it);\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("OtherGroovy", "OtherGroovy$_iterate_closure1", "Runner", "Runner$_run_closure1", "Launch");
        expectingNoProblems();
        executeClass(paths[0], "Launch", "2468", "");

        //@formatter:off
        env.addGroovyClass(paths[1], "Runner",
            "def static run(int n) { \n" +
            "  OtherGroovy.iterate (4) {\n" +
            "  print it\n" + // change here
            "  }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("Runner", "Runner$_run_closure1");
        expectingNoProblems();
        executeClass(paths[0], "Launch", "1234", "");

        //@formatter:off
        env.addGroovyClass(paths[1], "OtherGroovy",
            "def static iterate(Integer n, closure) {\n" +
            "  1.upto(n*2) {\n" + // change here
            "    closure(it);\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("OtherGroovy", "OtherGroovy$_iterate_closure1");
        expectingNoProblems();
        executeClass(paths[0], "Launch", "12345678", "");

        //@formatter:off
        env.addGroovyClass(paths[1], "OtherGroovy",
            "def static iterate(int n, closure) {\n" +
            "  1.upto(n*2) {\n" + // change here
            "    closure(it);\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("OtherGroovy", "OtherGroovy$_iterate_closure1", "Runner", "Runner$_run_closure1");
        expectingNoProblems();
        executeClass(paths[0], "Launch", "12345678", "");
    }

    @Test
    public void testSpock_GRE558() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);
        addJUnitAndSpock(paths[0]);

        //@formatter:off
        env.addGroovyClass(paths[1], "MyTest",
            "final class MyTest extends spock.lang.Specification {\n" +
            "  def prop\n" +
            "  def meth() {\n" +
            "   expect:\n" +
            "    'hello' != 'world'\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    print 'success'\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("MyTest");
        executeClass(paths[0], "MyTest", "success", null);
    }

    /**
     * Testing that the transform occurs on an incremental change. The key thing
     * being looked at here is that the incremental change is not directly to a
     * transformed file but to a file referenced from a transformed file.
     */
    @Test
    public void testSpock_GRE605_1() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);
        addJUnitAndSpock(paths[0]);

        //@formatter:off
        env.addGroovyClass(paths[1], "FoobarSpec",
            "class FoobarSpec extends spock.lang.Specification {\n" +
            "  private Foobar field\n" +
            "  def example() {\n" +
            "   when: \n" +
            "    def foobar = new Foobar()\n" +
            "    \n" +
            "   then:\n" +
            "    foobar.baz == 42\n" +
            "  }\n" +
            "}");
        //@formatter:on

        //@formatter:off
        env.addGroovyClass(paths[1], "Foobar",
            "class Foobar {\n" +
            "  def baz = 42\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("Foobar", "FoobarSpec");

        IPath workspacePath = env.getWorkspaceRootPath();
        File f = new File(workspacePath.append(env.getOutputLocation(paths[0])).toOSString(), "FoobarSpec.class");
        long filesize = f.length(); // this is 9131 for groovy 1.7.0

        //@formatter:off
        env.addGroovyClass(paths[1], "Foobar",
            "class Foobar {\n" +
            "  def baz = 42\n" +
            "  def xyz = 36\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("Foobar", "FoobarSpec");

        long filesizeNow = f.length(); // drops to 7002 if transform did not run
        assertEquals(filesize, filesizeNow);
    }

    /**
     * Also found through this issue, FoobarSpec not getting a rebuild when
     * Foobar changes. This test is currently having a reference from
     * foobarspec>foobar by having a field of type Foobar. If that is removed,
     * even though there is still a reference to ctor for foobar from
     * foobarspec, there is no incremental build of foobarspec when foobar is
     * changed. I am not 100% sure if one is needed or not - possibly it is...
     * hmmm
     */
    @Test
    public void testSpock_GRE605_2() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);
        addJUnitAndSpock(paths[0]);

        //@formatter:off
        env.addGroovyClass(paths[1], "FoobarSpec",
            "class FoobarSpec extends spock.lang.Specification {\n" +
            "  private Foobar field\n" +
            "  def example() {\n" +
            "   given: \n" +
            "    def foobar = new Foobar()\n" +
            "    \n" +
            "   expect:\n" +
            "    foobar.baz == 42\n" +
            "  }\n" +
            "}");
        //@formatter:on

        //@formatter:off
        env.addGroovyClass(paths[1], "Foobar",
            "class Foobar {\n" +
            "  def baz = 42\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("Foobar", "FoobarSpec");

        //@formatter:off
        env.addGroovyClass(paths[1], "Foobar",
            "class Foobar {\n" +
            "  def baz = 42\n" +
            "  def xyz = 36\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("Foobar", "FoobarSpec");
    }

    @Test
    public void testGenericMethods() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addClass(paths[1], "", "Foo",
            "public class Foo<T> {\n" +
            "   public void m() {\n" +
            "      Bar.agent(null);\n" +
            "   }\n" +
            "}\n");
        //@formatter:on

        //@formatter:off
        env.addGroovyClass(paths[1], "Bar",
            "class Bar {\n" +
            "   public static <PP> void agent(PP state) {\n" +
            "   }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("Foo", "Bar");
        expectingNoProblems();
    }

    @Test
    public void testPropertyAccessorLocationChecks() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p1", "Hello",
            "package p1;\n" +
            "class Hello {\n" +
            "  int color;\n" +
            "   static void main(String[] args) {\n" +
            "      print \"Hello Groovy world\"\n" +
            "   }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p1.Hello");
        expectingNoProblems();
        executeClass(paths[0], "p1.Hello", "Hello Groovy world", null);
    }

    @Test
    public void testBuildGroovy2() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p1", "Hello",
            "package p1;\n" +
            "interface Hello extends java.util.List {\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p1.Hello");
        expectingNoProblems();
    }

    @Test
    public void testLargeProjects_GRE1037() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        int max = AbstractImageBuilder.MAX_AT_ONCE;
        try {
            AbstractImageBuilder.MAX_AT_ONCE = 10;

            for (int i = 1; i < 10; i += 1) {
                env.addClass(paths[1], "p1", "Hello" + i,
                    "package p1;\n" +
                    "class Hello" + i + " {\n" +
                    "}\n");
            }

            //@formatter:off
            env.addGroovyClass(paths[1], "p1", "Foo",
                "package p1;\n" +
                "import p1.*;\n" +
                "class Foo {\n" +
                "  public static void main(String []argv) { print '12';}\n" +
                "  void m() { Bar b = new Bar();}\n" +
                "}\n");
            //@formatter:on

            env.addGroovyClass(paths[1], "p1", "Bar",
                "package p1;\n" +
                "class Bar {\n" + "}\n");

            incrementalBuild(paths[0]);
            // see console for all the exceptions...
            // no class for p1.Foo when problem occurs:
            executeClass(paths[0], "p1.Foo", "12", "");
        } finally {
            AbstractImageBuilder.MAX_AT_ONCE = max;
        }
    }

    @Test
    public void testIncrementalCompilationTheBasics() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addClass(paths[1], "pkg", "Hello",
            "package pkg;\n" +
            "public class Hello {\n" +
            "   public static void main(String[] args) {\n" +
            "      System.out.println(new GHello().run());\n" +
            "   }\n" +
            "}\n");
        //@formatter:on

        //@formatter:off
        env.addGroovyClass(paths[1], "pkg", "GHello",
            "package pkg;\n" +
            "public class GHello {\n" +
            "  public int run() { return 12; }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("pkg.Hello", "pkg.GHello");
        expectingNoProblems();
        executeClass(paths[0], "pkg.Hello", "12", "");

        //@formatter:off
        env.addGroovyClass(paths[1], "pkg", "GHello",
            "package pkg;\n" +
            "public class GHello {\n" +
            "\n" + // new blank line
            "  public int run() { return 12; }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("pkg.GHello");
        expectingNoProblems();

        // structural change to Groovy file: Did the Java file record its dependency correctly?

        //@formatter:off
        env.addGroovyClass(paths[1], "pkg", "GHello",
            "package pkg;\n" +
            "public class GHello {\n" +
            "  public String run() { return \"abc\"; }\n" + // return type now String
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("pkg.GHello", "pkg.Hello");
        expectingNoProblems();
    }

    @Test
    public void testIncrementalCompilation1594() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addClass(paths[1], "testpkg", "AllTests",
            "package testpkg;\n" +
            "public final class AllTests {\n" +
            "    TestCaseChannelPersistentStore tccps;\n" +
            "\n" +
            "public static void setupDbConnPool() throws Exception {\n" +
            "}\n" +
            "}\n");
        //@formatter:on

        //@formatter:off
        env.addGroovyClass(paths[1], "testpkg", "TestCaseChannelPersistentStore",
            "package testpkg\n" +
            "class TestCaseChannelPersistentStore {\n" +
            // This will be added in a subsequent incremental build
            //"public static void foo() {\n" +
            //"  def clazz=TestCaseChannelPersistentStore.class;\n" +
            //"}\n" +
            "\n" +
            "void testRefreshedChannelMap() {\n" +
            "    def x= new Runnable() {public void run() { print('running');}};\n" +
            "     x.run();\n" +
            "}\n" +
            "public static void main(String[]argv) { new TestCaseChannelPersistentStore().testRefreshedChannelMap();}\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("testpkg.AllTests", "testpkg.TestCaseChannelPersistentStore", "testpkg.TestCaseChannelPersistentStore$1");
        expectingNoProblems();
        executeClass(paths[0], "testpkg.TestCaseChannelPersistentStore", "running", "");

        //@formatter:off
        env.addGroovyClass(paths[1], "testpkg", "TestCaseChannelPersistentStore",
            "package testpkg\n" +
            "class TestCaseChannelPersistentStore {\n" +
            "public static void foo() {\n" +
            "  def clazz=TestCaseChannelPersistentStore.class;\n" +
            "}\n" +
            "\n" +
            "void testRefreshedChannelMap() {\n" +
            "    def x= new Runnable() {public void run() {}};\n" +
            "}\n" +
            "}\n");
        //@formatter:on

        incrementalBuild();
        expectingNoProblems();
        executeClass(paths[0], "testpkg.TestCaseChannelPersistentStore", "", "");
    }

    @Test
    public void testIncrementalGenericsAndBinaryTypeBindings_GRE566() throws Exception {
        IPath[] paths = createSimpleProject("GRE566", true);

        //@formatter:off
        env.addClass(paths[1], "pkg", "Event",
            "package pkg;\n" +
            "public class Event {\n" +
            "}\n");
        //@formatter:on

        //@formatter:off
        env.addClass(paths[1], "pkg", "EventImpl",
            "package pkg;\n" +
            "public class EventImpl extends Event {\n" +
            "}\n");
        //@formatter:on

        //@formatter:off
        env.addClass(paths[1], "pkg", "Face",
            "package pkg;\n" +
            "public interface Face<E extends Event> {\n" +
            "  void onApplicationEvent(E event);\n" +
            "}\n");
        //@formatter:on

        //@formatter:off
        env.addClass(paths[1], "pkg", "Java",
            "package pkg;\n" +
            "public class Java implements Face<EventImpl> {\n" +
            "  public void onApplicationEvent(EventImpl event) {\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        //@formatter:off
        env.addGroovyClass(paths[1], "pkg", "Groovy",
            "package pkg;\n" +
            "class Groovy extends Java {\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("pkg.Event", "pkg.EventImpl", "pkg.Face", "pkg.Java", "pkg.Groovy");
        expectingNoProblems();

        //@formatter:off
        env.addGroovyClass(paths[1], "pkg", "Groovy",
            "package pkg\n" +
            "class Groovy extends Java {\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("pkg.Groovy");
        expectingNoProblems();
    }

    @Test
    public void testIncrementalCompilationTheBasics2_changingJavaDependedUponByGroovy() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addClass(paths[1], "pkg", "Hello",
            "package pkg;\n" +
            "public class Hello {\n" +
            "  public int run() { return 12; }\n" +
            "}\n");
        //@formatter:on

        //@formatter:off
        env.addGroovyClass(paths[1], "pkg", "GHello",
            "package pkg;\n" +
            "public class GHello {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.println(new Hello().run());\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("pkg.Hello", "pkg.GHello");
        expectingNoProblems();
        executeClass(paths[0], "pkg.GHello", "12", "");

        //@formatter:off
        env.addClass(paths[1], "pkg", "Hello",
            "package pkg;\n" +
            "public class Hello {\n" +
            "  \n" + // new blank line
            "  public int run() { return 12; }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("pkg.Hello");
        expectingNoProblems();

        // structural change to Groovy file: Did the Java file record its dependency correctly?

        //@formatter:off
        env.addClass(paths[1], "pkg", "Hello",
            "package pkg;\n" +
            "public class Hello {\n" +
            "  public String run() { return \"abc\"; }\n" + // return type now String
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("pkg.GHello", "pkg.Hello");
        expectingNoProblems();
        executeClass(paths[0], "pkg.GHello", "abc", "");
    }

    @Test
    public void testInnerClasses_GRE339() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addClass(paths[1], "", "Outer",
            "public interface Outer {\n" +
            "  interface Inner { static String VAR=\"value\";}\n" +
            "}\n");
        //@formatter:on

        env.addGroovyClass(paths[1], "script",
            "print Outer.Inner.VAR\n");

        incrementalBuild(paths[0]);

        expectingCompiledClasses("Outer", "Outer$Inner", "script");
        expectingNoProblems();
        executeClass(paths[0], "script", "value", "");

        // whitespace change to groovy file
        env.addGroovyClass(paths[1], "script",
            "print Outer.Inner.VAR \n");

        incrementalBuild(paths[0]);
        expectingCompiledClasses("script");
        expectingNoProblems();
        executeClass(paths[0], "script", "value", null);
    }

    @Test
    public void testSimpleTaskMarkerInSingleLineComment() throws Exception {
        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        IPath pathToA = env.addGroovyClass(paths[1], "p", "A",
            "package p; \n" +
            "class C {\n" + "//todo nothing\n" + // // 24>36 'todo nothing'
            "\n" + "//tooo two\n" +
            "}");
        //@formatter:on

        fullBuild(paths[0]);

        Problem[] rootProblems = env.getProblemsFor(pathToA);
        // positions should be from the first character of the tag to the character after the last in the text
        expectingSpecificProblemFor(pathToA, new Problem("A", toTask("todo", "nothing"), pathToA, 24, 36, -1, IMarker.SEVERITY_ERROR));
    }

    @Test
    public void testSimpleTaskMarkerInSingleLineCommentEndOfClass() throws Exception {
        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        IPath pathToA = env.addGroovyClass(paths[1], "p", "A",
            "package p; \n" +
            "class C {\n" + "//topo nothing\n" + // '/' is 22 'n' is 29 'g' is 35
            "\n" + "//todo two\n" + // '/' is 38 't' is 45 'o' is 47
            "}");
        //@formatter:on

        fullBuild(paths[0]);

        expectingSpecificProblemFor(pathToA, new Problem("A", toTask("todo", "two"), pathToA, 40, 48, -1, IMarker.SEVERITY_ERROR));
    }

    @Test
    public void testSimpleTaskMarkerInSingleLineCommentEndOfClassCaseInsensitive() throws Exception {
        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");
        newOptions.put(JavaCore.COMPILER_TASK_CASE_SENSITIVE, JavaCore.DISABLED);
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        IPath pathToA = env.addGroovyClass(paths[1], "p", "A",
            "package p; \n" +
            "class C {\n" + "//TODO nothing\n" + // '/' is 22 'n' is 29 'g' is 35
            "\n" + "//topo two\n" + // '/' is 38 't' is 45 'o' is 47
            "}");
        //@formatter:on

        fullBuild(paths[0]);

        Problem[] rootProblems = env.getProblemsFor(pathToA);
        expectingSpecificProblemFor(pathToA, new Problem("A", toTask("todo", "nothing"), pathToA, 24, 36, -1, IMarker.SEVERITY_ERROR));
    }

    @Test
    public void testTaskMarkerInMultiLineCommentButOnOneLine() throws Exception {
        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        IPath pathToA = env.addGroovyClass(paths[1], "p", "A",
            "package p; \n" +
            "/*  todo nothing */\n" +
            "public class A {\n" +
            "}");
        //@formatter:on

        fullBuild(paths[0]);

        expectingSpecificProblemFor(pathToA, new Problem("A", toTask("todo", "nothing"), pathToA, 16, 29, -1, IMarker.SEVERITY_ERROR));
    }

    @Test
    public void testTaskMarkerInMultiLineButNoText() throws Exception {
        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        IPath pathToA = env.addGroovyClass(paths[1], "p", "A",
            "package p; \n" +
            "/*  todo\n" +
            " */\n" +
            "public class A {\n" +
            "}");
        //@formatter:on

        fullBuild(paths[0]);

        expectingSpecificProblemFor(pathToA, new Problem("A", toTask("todo", ""), pathToA, 16, 20, -1, IMarker.SEVERITY_ERROR));
    }

    @Test
    public void testTaskMarkerInMultiLineOutsideClass() throws Exception {
        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        IPath pathToA = env.addGroovyClass(paths[1], "p", "A",
            "package p; \n" +
            "/*  \n" + // 12
            " * todo nothing *\n" + // 17
            " */\n" +
            "public class A {\n" +
            "}");
        //@formatter:on

        fullBuild(paths[0]);

        expectingSpecificProblemFor(pathToA, new Problem("A", toTask("todo", "nothing *"), pathToA, 20, 34, -1, IMarker.SEVERITY_ERROR));
    }

    @Test // task marker inside a multi line comment inside a class
    public void testTaskMarkerInMultiLineInsideClass() throws Exception {
        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        IPath pathToA = env.addGroovyClass(paths[1], "p", "A",
            "package p; \n" + // -- \n is 11
            "public class A {\n" + // -- \n is 28
            "   /*  \n" + // -- \n is 36
            " * todo nothing *\n" +
            " */\n" +
            "}");
        //@formatter:on

        fullBuild(paths[0]);

        expectingSpecificProblemFor(pathToA, new Problem("A", toTask("todo", "nothing *"), pathToA, 40, 54, -1, IMarker.SEVERITY_ERROR));
    }

    @Test // tag priority
    public void testTaskMarkerMixedPriorities() throws Exception {
        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
        newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW");
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        IPath pathToA = env.addGroovyClass(paths[1], "p", "A",
            "package p; \n" +
            "//TODO normal\n" +
            "public class A {\n" +
            "  public void foo() {\n" +
            "    //FIXME high\n" +
            "  }\n" +
            "  public void foo2() {\n" +
            "    //XXX low\n" +
            "  }\n" +
            "}");
        //@formatter:on

        fullBuild(paths[0]);
        IMarker[] markers = env.getTaskMarkersFor(pathToA);
        assertEquals("Wrong size", 3, markers.length);

        IMarker marker = markers[0];
        Object priority = marker.getAttribute(IMarker.PRIORITY);
        String message = (String) marker.getAttribute(IMarker.MESSAGE);
        assertTrue("Wrong message", message.startsWith("TODO"));
        assertNotNull("No task priority", priority);
        assertEquals("Wrong priority", IMarker.PRIORITY_NORMAL, priority);

        marker = markers[1];
        priority = marker.getAttribute(IMarker.PRIORITY);
        message = (String) marker.getAttribute(IMarker.MESSAGE);
        assertTrue("Wrong message", message.startsWith("FIXME"));
        assertNotNull("No task priority", priority);
        assertEquals("Wrong priority", IMarker.PRIORITY_HIGH, priority);

        marker = markers[2];
        priority = marker.getAttribute(IMarker.PRIORITY);
        message = (String) marker.getAttribute(IMarker.MESSAGE);
        assertTrue("Wrong message", message.startsWith("XXX"));
        assertNotNull("No task priority", priority);
        assertEquals("Wrong priority", IMarker.PRIORITY_LOW, priority);
    }

    @Test
    public void testTaskMarkerMultipleOnOneLineInSLComment() throws Exception {
        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
        newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW");
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        IPath pathToA = env.addGroovyClass(paths[1], "p", "A",
            "package p; \n" +
            "// TODO FIXME need to review the loop TODO should be done\n" +
            "public class A {\n" +
            "}");
        //@formatter:on

        fullBuild(paths[0]);
        IMarker[] markers = env.getTaskMarkersFor(pathToA);
        assertEquals("Wrong size", 3, markers.length);

        IMarker marker = markers[2];
        Object priority = marker.getAttribute(IMarker.PRIORITY);
        String message = (String) marker.getAttribute(IMarker.MESSAGE);
        assertEquals("Wrong message", toTask("TODO", "should be done"), message);
        assertNotNull("No task priority", priority);
        assertEquals("Wrong priority", IMarker.PRIORITY_NORMAL, priority);

        marker = markers[1];
        priority = marker.getAttribute(IMarker.PRIORITY);
        message = (String) marker.getAttribute(IMarker.MESSAGE);
        assertEquals("Wrong message", toTask("FIXME", "need to review the loop"), message);
        assertNotNull("No task priority", priority);
        assertEquals("Wrong priority", IMarker.PRIORITY_HIGH, priority);

        marker = markers[0];
        priority = marker.getAttribute(IMarker.PRIORITY);
        message = (String) marker.getAttribute(IMarker.MESSAGE);
        assertEquals("Wrong message", toTask("TODO", "need to review the loop"), message);
        assertNotNull("No task priority", priority);
        assertEquals("Wrong priority", IMarker.PRIORITY_NORMAL, priority);
    }

    @Test
    public void testTaskMarkerMultipleOnOneLineInMLComment() throws Exception {
        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
        newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW");
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        IPath pathToA = env.addGroovyClass(paths[1], "p", "A",
            "package p; \n" +
            "/* TODO FIXME need to review the loop TODO should be done */\n" +
            "public class A {\n" +
            "}");
        //@formatter:on

        fullBuild(paths[0]);
        IMarker[] markers = env.getTaskMarkersFor(pathToA);
        assertEquals("Wrong size", 3, markers.length);

        IMarker marker = markers[2];
        Object priority = marker.getAttribute(IMarker.PRIORITY);
        String message = (String) marker.getAttribute(IMarker.MESSAGE);
        assertEquals("Wrong message", toTask("TODO", "should be done"), message);
        assertNotNull("No task priority", priority);
        assertEquals("Wrong priority", IMarker.PRIORITY_NORMAL, priority);

        marker = markers[1];
        priority = marker.getAttribute(IMarker.PRIORITY);
        message = (String) marker.getAttribute(IMarker.MESSAGE);
        assertEquals("Wrong message", toTask("FIXME", "need to review the loop"), message);
        assertNotNull("No task priority", priority);
        assertEquals("Wrong priority", IMarker.PRIORITY_HIGH, priority);

        marker = markers[0];
        priority = marker.getAttribute(IMarker.PRIORITY);
        message = (String) marker.getAttribute(IMarker.MESSAGE);
        assertEquals("Wrong message", toTask("TODO", "need to review the loop"), message);
        assertNotNull("No task priority", priority);
        assertEquals("Wrong priority", IMarker.PRIORITY_NORMAL, priority);
    }

    @Test // two on one line
    public void testTaskMarkerSharedDescription() throws Exception {
        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
        newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW");
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        IPath pathToA = env.addGroovyClass(paths[1], "p", "A",
            "package p; \n" +
            "// TODO TODO need to review the loop\n" +
            "public class A {\n" + "}");
        //@formatter:on

        fullBuild(paths[0]);
        IMarker[] markers = env.getTaskMarkersFor(pathToA);
        assertEquals("Wrong size", 2, markers.length);

        IMarker marker = markers[1];
        Object priority = marker.getAttribute(IMarker.PRIORITY);
        String message = (String) marker.getAttribute(IMarker.MESSAGE);
        assertEquals("Wrong message", toTask("TODO", "need to review the loop"), message);
        assertNotNull("No task priority", priority);
        assertEquals("Wrong priority", IMarker.PRIORITY_NORMAL, priority);

        marker = markers[0];
        priority = marker.getAttribute(IMarker.PRIORITY);
        message = (String) marker.getAttribute(IMarker.MESSAGE);
        assertEquals("Wrong message", toTask("TODO", "need to review the loop"), message);
        assertNotNull("No task priority", priority);
        assertEquals("Wrong priority", IMarker.PRIORITY_NORMAL, priority);
    }

    @Test
    public void testCopyGroovyResourceNonGroovyProject_GRECLIPSE653() throws Exception {
        IPath[] paths = createSimpleProject("Project", false);

        IPath pathToA = env.addGroovyClass(paths[1], "p", "A",
            "package p; \n" +
            "class C {}");

        incrementalBuild(paths[0]);

        // groovy file should be copied as-is
        IPath pathToABin = paths[0].append("bin").append(pathToA.removeFirstSegments(pathToA.segmentCount() - 2));
        assertTrue("File should exist " + pathToABin.toPortableString(), env.getWorkspace().getRoot().getFile(pathToABin).exists());

        // now check that works for incremental
        IPath pathToB = env.addGroovyClass(paths[1], "p", "B",
            "package p; \n" +
            "class D {}");
        incrementalBuild(paths[0]);

        // groovy file should be copied as-is
        IPath pathToBBin = paths[0].append("bin").append(pathToB.removeFirstSegments(pathToB.segmentCount() - 2));
        assertTrue("File should exist " + pathToBBin.toPortableString(), env.getWorkspace().getRoot().getFile(pathToBBin).exists());

        // now check that bin file is deleted when deleted in source
        IFile bFile = env.getWorkspace().getRoot().getFile(pathToB);
        bFile.delete(true, null);
        incrementalBuild(paths[0]);
        assertFalse("File should not exist " + pathToBBin.toPortableString(), env.getWorkspace().getRoot().getFile(pathToBBin).exists());
    }

    @Test
    public void testCopyResourceNonGroovyProject_GRECLIPSE653() throws Exception {
        IPath[] paths = createSimpleProject("Project", false);

        IPath pathToA = env.addFile(paths[1], "A.txt", "A");

        fullBuild(paths[0]);

        // file should be copied as-is
        IPath pathToABin = paths[0].append("bin").append(pathToA.removeFirstSegments(pathToA.segmentCount() - 1));
        assertTrue("File should exist " + pathToABin.toPortableString(), env.getWorkspace().getRoot().getFile(pathToABin).exists());

        // now check that works for incremental
        IPath pathToB = env.addFile(paths[1], "B.txt", "B");
        incrementalBuild(paths[0]);

        // groovy file should be copied as-is
        IPath pathToBBin = paths[0].append("bin").append(pathToB.removeFirstSegments(pathToB.segmentCount() - 1));
        assertTrue("File should exist " + pathToBBin.toPortableString(), env.getWorkspace().getRoot().getFile(pathToBBin).exists());

        // now check that bin file is deleted when deleted in source
        IFile bFile = env.getWorkspace().getRoot().getFile(pathToB);
        bFile.delete(true, null);
        incrementalBuild(paths[0]);
        assertFalse("File should not exist " + pathToBBin.toPortableString(), env.getWorkspace().getRoot().getFile(pathToBBin).exists());
    }

    @Test
    public void testCopyResourceGroovyProject_GRECLIPSE653() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        IPath pathToA = env.addFile(paths[1], "A.txt", "A");

        fullBuild(paths[0]);

        // groovy file should be copied as-is
        IPath pathToABin = paths[0].append("bin").append(pathToA.removeFirstSegments(pathToA.segmentCount() - 1));
        assertTrue("File should exist " + pathToABin.toPortableString(), env.getWorkspace().getRoot().getFile(pathToABin).exists());

        // now check that works for incremental
        IPath pathToB = env.addFile(paths[1], "B.txt", "B");
        incrementalBuild(paths[0]);

        // groovy file should be copied as-is
        IPath pathToBBin = paths[0].append("bin").append(pathToB.removeFirstSegments(pathToB.segmentCount() - 1));
        assertTrue("File should exist " + pathToBBin.toPortableString(), env.getWorkspace().getRoot().getFile(pathToBBin).exists());

        // now check that bin file is deleted when deleted in source
        IFile bFile = env.getWorkspace().getRoot().getFile(pathToB);
        bFile.delete(true, null);
        incrementalBuild(paths[0]);
        assertFalse("File should not exist " + pathToBBin.toPortableString(), env.getWorkspace().getRoot().getFile(pathToBBin).exists());
    }

    @Test
    public void testNoDoubleResolve() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        env.addGroovyClass(paths[1], "p", "Script",
            "package p; println ''\n");
        incrementalBuild(paths[0]);

        IType pScript = env.getJavaProject("Project").findType("p.Script");
        assertNotNull(pScript);

        GroovyCompilationUnit unit = (GroovyCompilationUnit) pScript.getCompilationUnit();
        unit.becomeWorkingCopy(null);

        ModuleNodeInfo moduleInfo = unit.getModuleInfo(true);
        JDTResolver resolver = moduleInfo.resolver;
        assertNotNull(resolver);

        ReflectionUtils.setPrivateField(ResolveVisitor.class, "currentClass", resolver, moduleInfo.module.getScriptClassDummy());
        ClassNode url = resolver.resolve("java.net.URL");
        assertNotNull("Should have found the java.net.URL ClassNode", url);
        assertEquals("Wrong ClassNode found", "java.net.URL", url.getName());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/737
    public void testNoResolveFailurePropagation1() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p", "One",
            "package p;\n" +
            "class One {\n" +
            "  def pat(str) {\n" +
            "    Pattern.compile(str)\n" + // not resolvable
            "  }\n" +
            "}\n");
        env.addGroovyClass(paths[1], "p", "Two",
            "package p;\n" +
            "import java.util.regex.Pattern\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Two {\n" +
            "  One one\n" +
            "  def m() {\n" +
            "    Pattern pattern = null\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        fullBuild(paths[0]);
        expectingNoProblems();
    }

    @Test
    public void testNoResolveFailurePropagation2() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p", "One",
            "package p;\n" +
            "class One {\n" +
            "  def policy() {\n" +
            "    ThreadPoolExecutor.AbortPolicy\n" + // not resolvable
            "  }\n" +
            "}\n");
        env.addGroovyClass(paths[1], "p", "Two",
            "package p;\n" +
            "import java.util.concurrent.ThreadPoolExecutor\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Two {\n" +
            "  One one\n" +
            "  def m() {\n" +
            "    ThreadPoolExecutor.AbortPolicy policy = null\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        fullBuild(paths[0]);
        expectingNoProblems();
    }

    @Test // GRECLIPSE-1170
    public void testFieldInitializerFromOtherFile() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        env.addGroovyClass(paths[1], "p", "Other",
            "package p\nclass Other {\ndef x = 9\n}");
        env.addGroovyClass(paths[1], "p", "Target",
            "package p\nnew Other()");
        incrementalBuild(paths[0]);

        IType pTarget = env.getJavaProject("Project").findType("p.Target");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) pTarget.getCompilationUnit();

        // now find the class reference
        ReturnStatement returnStatement = (ReturnStatement) unit.getModuleNode().getStatementBlock().getStatements().get(0);
        ClassNode type = ((ConstructorCallExpression) returnStatement.getExpression()).getType();

        // now check that the field initializer exists
        Expression initialExpression = type.getField("x").getInitialExpression();
        assertNotNull(initialExpression);
        assertEquals("Should have been an int", VariableScope.INTEGER_CLASS_NODE, ClassHelper.getWrapper(initialExpression.getType()));
        assertEquals("Should have been the number 9", "9", initialExpression.getText());

        // now check to ensure that there are no duplicate fields or properties
        int declCount = 0;
        for (FieldNode field : type.getFields()) {
            if (field.getName().equals("x")) {
                declCount += 1;
            }
        }
        assertEquals("Should have found 'x' field exactly one time", 1, declCount);

        declCount = 0;
        for (PropertyNode prop : type.getProperties()) {
            if (prop.getName().equals("x")) {
                declCount += 1;
            }
        }
        assertEquals("Should have found 'x' property exactly one time", 1, declCount);
    }

    @Test // GRECLIPSE-1727
    public void testTraitBasics() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p", "Named",
            "package p\n" +
            "trait Named {\n" +
            "    String name() { 'name' }" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p.Named", "p.Named$Trait$Helper");
        expectingNoProblems();
    }

    @Test
    public void testTraitIncremental() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p", "Named",
            "package p\n" +
            "trait Named {\n" +
            "  String name() { 'name' }\n" +
            "}\n");
        env.addGroovyClass(paths[1], "q", "NamedClass",
            "package q;\n" +
            "import p.Named;\n" +
            "public class NamedClass implements Named {}\n");
        env.addGroovyClass(paths[1], "Runner",
            "import p.Named\n" +
            "import q.NamedClass\n" +
            "Named named = new NamedClass()\n" +
            "print named.name()\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p.Named", "p.Named$Trait$Helper", "q.NamedClass", "Runner");
        expectingNoProblems();
        executeClass(paths[0], "Runner", "name", "");

        //@formatter:off
        env.addGroovyClass(paths[1], "p", "Named",
            "package p\n" +
            "trait Named {\n" +
            "  String name\n" +
            "  String name() { \"$name\" }\n" + // modify the body of the trait
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p.Named", "p.Named$Trait$Helper", "p.Named$Trait$FieldHelper", "q.NamedClass", "Runner");
        expectingNoProblems();
        executeClass(paths[0], "Runner", "null", "");

        //@formatter:off
        env.addGroovyClass(paths[1], "Runner",
            "import p.Named\n" +
            "import q.NamedClass\n" +
            "Named named = new NamedClass(name: 'name')\n" +
            "print named.name()\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("Runner");
        expectingNoProblems();
        executeClass(paths[0], "Runner", "name", "");

        //@formatter:off
        env.addGroovyClass(paths[1], "q", "NamedClass",
            "package q;\n" +
            "import p.Named;\n" +
            "public class NamedClass implements Named {\n" +
            "    String name() { \"Hello, ${name}!\" }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("q.NamedClass", "Runner");
        expectingNoProblems();
        executeClass(paths[0], "Runner", "Hello, name!", "");
    }

    @Test
    public void testTraitBinary() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p", "Named",
            "package p\n" +
            "trait Named {\n" +
            "    String name() { 'name' }\n" +
            "}\n");
        env.addGroovyClass(paths[1], "q", "DefaultNamed",
            "package q;\n" +
            "class DefaultNamed {\n" +
            "    public String name() { 'name' }\n" +
            "}\n");
        env.addGroovyClass(paths[1], "r", "NamedClass",
            "package r;\n" +
            "import p.Named\n" +
            "import q.DefaultNamed\n" +
            "public class NamedClass extends DefaultNamed implements Named {}\n");
        env.addGroovyClass(paths[1], "Runner",
            "import r.NamedClass\n" +
            "NamedClass named = new NamedClass()\n" +
            "print named.name()\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p.Named", "p.Named$Trait$Helper", "q.DefaultNamed", "r.NamedClass", "Runner");
        expectingNoProblems();
        executeClass(paths[0], "Runner", "name", "");

        //@formatter:off
        env.addGroovyClass(paths[1], "r", "NamedClass",
            "package r;\n" +
            "import p.Named\n" +
            "import q.DefaultNamed\n" +
            "public class NamedClass extends DefaultNamed implements Named {\n" +
            "  String name() { 'new name' }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("r.NamedClass", "Runner");
        expectingNoProblems();
        executeClass(paths[0], "Runner", "new name", "");

        //@formatter:off
        env.addGroovyClass(paths[1], "r", "NamedClass",
            "package r;\n" +
            "import p.Named\n" +
            "import q.DefaultNamed\n" +
            "public class NamedClass extends DefaultNamed implements Named {}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("r.NamedClass", "Runner");
        expectingNoProblems();
        executeClass(paths[0], "Runner", "name", "");

        //@formatter:off
        env.addGroovyClass(paths[1], "p", "Named",
            "package p\n" +
            "trait Named {\n" +
            "  abstract String name()\n" +
            "}\n");
        //@formatter:on

        //@formatter:off
        env.addGroovyClass(paths[1], "r", "NamedClass",
            "package r;\n" +
            "import p.Named\n" +
            "import q.DefaultNamed\n" +
            "public class NamedClass extends DefaultNamed implements Named {\n" +
            "  String name() { 'new name' }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p.Named", "p.Named$Trait$Helper", "r.NamedClass", "Runner");
        expectingNoProblems();
        executeClass(paths[0], "Runner", "new name", "");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/733
    public void testTraitGenerics() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p", "Event",
            "package p\n" +
            "class Event<T> {\n" +
            "  Event(String id, T payload) {\n" +
            "  }\n" +
            "  Event<T> setReplyTo(Object replyTo) {\n" +
            "  }\n" +
            "}\n");
        env.addGroovyClass(paths[1], "p", "Events",
            "package p\n" +
            "@groovy.transform.CompileStatic\n" +
            "trait Events {\n" +
            "  def <E extends Event<?>> Registration<Object, Closure<E>> on(Class key, Closure consumer) {\n" +
            "  }\n" +
            "}\n" +
            "interface Registration<K, V> {}\n");
        env.addGroovyClass(paths[1], "q", "Service",
            "package q\n" +
            "class Service implements p.Events {\n" +
            "}\n");
        env.addGroovyClass(paths[1], "q", "ServiceWrapper",
            "package q\n" +
            "class ServiceWrapper {\n" +
            "  Service service\n" +
            "}\n");
        //@formatter:on

        fullBuild(paths[0]);
        expectingNoProblems();

        //@formatter:off
        env.addGroovyClass(paths[1], "q", "ServiceWrapper",
            "package q\n" +
            "class ServiceWrapper {\n" +
            "  Service service\n" +
            "  def logger\n" + // modify the body
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("q.ServiceWrapper");
        expectingNoProblems(); // not "Inconsistent classfile encountered: The undefined type parameter T is referenced from within Service"
    }

    @Test // see GroovyCompilationUnitDeclaration#processToPhase(int)
    public void testTraitGRE1776() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "p", "MyTrait",
            "package p\n" +
            "trait MyTrait {\n" +
            "}\n");
        env.addGroovyClass(paths[1], "q", "MyClass",
            "package q\n" +
            "import p.MyTrait\n" +
            "class MyClass implements MyTrait {}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p.MyTrait", "p.MyTrait$Trait$Helper", "q.MyClass");
        expectingNoProblems();

        //@formatter:off
        env.addGroovyClass(paths[1], "p", "MyTrait",
            "package p\n" +
            "trait MyTrait {\n" +
            "  def m() { 'm' }\n" + // modify the body
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p.MyTrait", "p.MyTrait$Trait$Helper", "q.MyClass");
        expectingNoProblems();

        //@formatter:off
        env.addGroovyClass(paths[1], "p", "MyTrait",
            "package p\n" +
            "trait MyTrait {\n" +
            "  def k() { 'k' }\n" + // modify the body again
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p.MyTrait", "p.MyTrait$Trait$Helper", "q.MyClass");
        expectingNoProblems();
    }

    @Test
    public void testGRE1773() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        String baseType =
            "package test\n" +
            "abstract class Class1 {\n" +
            "  abstract void m1()\n" +
            "  \n" +
            "  void m2() {}\n" +
            "  \n" +
            "  static Class1 create(String type) {\n" +
            "    switch (type) {\n" +
            "    case 'Class2':\n" +
            "      return new Class2()\n" +
            "    case 'Class3':\n" +
            "      return new Class3()\n" +
            "    default:\n" +
            "      assert false : \"Unexpected type ${type}\"\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        IPath class1 = env.addGroovyClass(paths[1], "test", "Class1", baseType);
        IPath class2 = env.addGroovyClass(paths[1], "test", "Class2",
            "package test\n" +
            "class Class2 extends Class1 {\n" +
            "  @Override\n" +
            "  public void m1() {\n" +
            "  }\n" +
            "}\n");
        IPath class3 = env.addGroovyClass(paths[1], "test", "Class3",
            "package test\n" +
            "class Class3 extends Class1 {\n" +
            "  @Override\n" +
            "  public void m1() {\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("test.Class1", "test.Class2", "test.Class3");

        // modify the body of the abstract class to break build
        class1 = env.addGroovyClass(paths[1], "test", "Class1", baseType.replace("m1", ""));

        incrementalBuild(paths[0]);
        expectingProblemsFor(class1, Arrays.asList(
            "Problem : Groovy:expecting EOF, found 'abstract'" +
            " [ resource : </Project/src/test/Class1.groovy> range : <39,40> category : <60> severity : <2>]",
            "Problem : Groovy:unexpected token: abstract" +
            " [ resource : </Project/src/test/Class1.groovy> range : <39,40> category : <60> severity : <2>]"));
        expectingProblemsFor(class2, Arrays.asList(
            "Problem : Groovy:Method \'m1\' from class \'test.Class2\' does not override method from its superclass or interfaces" +
            " but is annotated with @Override. [ resource : </Project/src/test/Class2.groovy> range : <45,54> category : <60> severity : <2>]"));
        expectingProblemsFor(class3, Arrays.asList(
            "Problem : Groovy:Method \'m1\' from class \'test.Class3\' does not override method from its superclass or interfaces" +
            " but is annotated with @Override. [ resource : </Project/src/test/Class3.groovy> range : <45,54> category : <60> severity : <2>]"));

        // modify the body of the abstract class to fix build
        env.addGroovyClass(paths[1], "test", "Class1", baseType);

        incrementalBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("test.Class1", "test.Class2", "test.Class3");
    }

    @Test // https://bugs.eclipse.org/bugs/show_bug.cgi?id=123721
    public void testTags3() throws Exception {
        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
        newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW");
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        IPath[] paths = createSimpleProject("Project", false);

        //@formatter:off
        IPath pathToA = env.addClass(paths[1], "p", "A",
            "package p; \n" +
            "// TODO need to review\n" +
            "public class A {\n" +
            "}");
        //@formatter:on

        fullBuild(paths[0]);
        IMarker[] markers = env.getTaskMarkersFor(pathToA);
        assertEquals("Marker should not be editable", Boolean.FALSE, markers[0].getAttribute(IMarker.USER_EDITABLE));
    }

    @Test // https://bugs.eclipse.org/bugs/show_bug.cgi?id=92821
    public void testUnusedImport() throws Exception {
        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.WARNING);
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        IPath[] paths = createSimpleProject("Project", false);

        //@formatter:off
        env.addClass(paths[1], "util", "MyException",
            "package util;\n" +
            "public class MyException extends Exception {\n" +
            "  private static final long serialVersionUID = 1L;\n" +
            "}");
        env.addClass(paths[1], "p", "Test",
            "package p;\n" +
            "import util.MyException;\n" +
            "public class Test {\n" +
            "  /**\n" +
            "   * @throws MyException\n" +
            "   */\n" +
            "  public void bar() {\n" +
            "  }\n" +
            "}");
        //@formatter:on

        fullBuild(paths[0]);
        expectingNoProblems();
    }

    @Test // https://bugs.eclipse.org/bugs/show_bug.cgi?id=98667
    public void test98667() throws Exception {
        IPath[] paths = createSimpleProject("Project", false);

        //@formatter:off
        env.addClass(paths[1], "p1", "Aaa$Bbb$Ccc",
            "package p1;\n" +
            "\n" +
            "public class Aaa$Bbb$Ccc {\n" +
            "}");
        //@formatter:on

        fullBuild(paths[0]);
        expectingNoProblems();
    }

    @Test // https://bugs.eclipse.org/bugs/show_bug.cgi?id=164707
    public void testBug164707() {
        IPath prj = env.addProject("Project");
        env.getJavaProject(prj).setOption(JavaCore.COMPILER_SOURCE, "invalid");
        fullBuild(prj);
        expectingNoProblems();
    }

    @Test
    public void testUpdateProjectPreferences() throws Exception {
        IPath[] paths = createSimpleProject("Project", false);

        //@formatter:off
        env.addClass(paths[1], "util", "MyException",
            "package util;\n" +
            "public class MyException extends Exception {\n" +
            "  private static final long serialVersionUID = 1L;\n" +
            "}");
        IPath cuPath = env.addClass(paths[1], "p", "Test",
            "package p;\n" +
            "import util.MyException;\n" +
            "public class Test {\n" +
            "}");
        //@formatter:on

        fullBuild(paths[0]);
        expectingSpecificProblemFor(paths[0], new Problem("",
            "The import util.MyException is never used", cuPath, 18, 34, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING));

        env.getJavaProject(paths[0]).setOption(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.IGNORE);

        fullBuild(paths[0]);
        expectingNoProblems();
    }

    @Test
    public void testUpdateWorkspacePreferences() throws Exception {
        IPath[] paths = createSimpleProject("Project", false);

        //@formatter:off
        env.addClass(paths[1], "util", "MyException",
            "package util;\n" +
            "public class MyException extends Exception {\n" +
            "  private static final long serialVersionUID = 1L;\n" +
            "}");
        IPath cuPath = env.addClass(paths[1], "p", "Test",
            "package p;\n" +
            "import util.MyException;\n" +
            "public class Test {\n" +
            "}");
        //@formatter:on

        fullBuild(paths[0]);
        expectingSpecificProblemFor(paths[0], new Problem("",
            "The import util.MyException is never used", cuPath, 18, 34, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING));

        String unusedImport = JavaModelManager.getJavaModelManager().getInstancePreferences().get(JavaCore.COMPILER_PB_UNUSED_IMPORT, null);
        try {
            JavaModelManager.getJavaModelManager().getInstancePreferences().put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.IGNORE);
            fullBuild(paths[0]);
            expectingNoProblems();
        } finally {
            if (unusedImport != null) {
                JavaModelManager.getJavaModelManager().getInstancePreferences().put(JavaCore.COMPILER_PB_UNUSED_IMPORT, unusedImport);
            } else {
                JavaModelManager.getJavaModelManager().getInstancePreferences().remove(JavaCore.COMPILER_PB_UNUSED_IMPORT);
            }
        }
    }

    @Test
    public void testTags4() throws Exception {
        Map<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO!,TODO,TODO?");
        newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "HIGH,NORMAL,LOW");
        JavaCore.setOptions((Hashtable<String, String>) newOptions);

        IPath[] paths = createSimpleProject("Project", false);

        //@formatter:off
        IPath pathToA = env.addClass(paths[1], "p", "A",
            "package p; \n" +
            "// TODO! TODO? need to review the loop\n" +
            "public class A {\n" +
            "}");
        //@formatter:on

        fullBuild(paths[0]);
        IMarker[] markers = env.getTaskMarkersFor(pathToA);
        assertEquals("Wrong size", 2, markers.length);

        IMarker marker = markers[1];
        Object priority = marker.getAttribute(IMarker.PRIORITY);
        String message = (String) marker.getAttribute(IMarker.MESSAGE);
        assertEquals("Wrong message", "TODO? need to review the loop", message);
        assertNotNull("No task priority", priority);
        assertEquals("Wrong priority", IMarker.PRIORITY_LOW, priority);

        marker = markers[0];
        priority = marker.getAttribute(IMarker.PRIORITY);
        message = (String) marker.getAttribute(IMarker.MESSAGE);
        assertEquals("Wrong message", "TODO! need to review the loop", message);
        assertNotNull("No task priority", priority);
        assertEquals("Wrong priority", IMarker.PRIORITY_HIGH, priority);
    }

    @Test // a groovy file name clashes with an existing type
    public void testBuildClash() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "Stack",
            "class StackTester {\n" +
            "  def x = new Stack()\n" +
            "  static main(args) {\n" +
            "    print(new StackTester().x.class)\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        fullBuild(paths[0]);
        expectingNoProblems();
        expectingCompiledClasses("StackTester");
        executeClass(paths[0], "StackTester", "class java.util.Stack", "");
    }

    @Test
    public void testMultiProjectDependencies() throws Exception {
        // Construct ProjectA
        IPath[] paths = createSimpleProject("ProjectA", true);

        //@formatter:off
        env.addGroovyClass(paths[1], "a", "Hello",
            "package a\n" +
            "class Hello {\n" +
            "  static void main(String[] args) {\n" +
            "    System.out.println('Hello world')\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        IPath projectA = paths[0];

        // Construct ProjectB
        paths = createSimpleProject("ProjectB", true);
        IPath projectB = paths[0];
        env.addRequiredProject(projectB, projectA, /*include all:*/new IPath[0], /*exclude none:*/new IPath[0], true);

        //@formatter:off
        env.addGroovyClass(paths[1], "b", "Hello",
            "package b\n" +
            "class Hello {\n" +
            "  static void main(String[] args) {\n" +
            "    a.Hello.main(args)\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        // Construct ProjectC
        paths = createSimpleProject("ProjectC", true);
        IPath projectC = paths[0];
        env.addRequiredProject(projectC, projectB, /*include all:*/new IPath[0], /*exclude none:*/new IPath[0], true);

        //@formatter:off
        env.addGroovyClass(paths[1], "c", "Hello",
            "package c\n" +
            "class Hello {\n" +
            "  static void main(String[] args) {\n" +
            "    b.Hello.main(args)\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        // Construct ProjectD
        paths = createSimpleProject("ProjectD", true);
        IPath projectD = paths[0];
        env.addRequiredProject(projectD, projectC, /*include all:*/new IPath[0], /*exclude none:*/new IPath[0], true);

        //@formatter:off
        env.addGroovyClass(paths[1], "d", "Hello",
            "package d\n" +
            "class Hello {\n" +
            "  static void main(String[] args) {\n" +
            "    c.Hello.main(args)\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        fullBuild();
        expectingCompiledClasses("a.Hello", "b.Hello", "c.Hello", "d.Hello");
        expectingNoProblems();
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/744
    public void testTypeAnnotation() throws Exception {
        IPath[] paths = createSimpleProject("Project", true);

        //@formatter:off
        env.addClass(paths[1], "p1", "Iterables",
            "package p1;\n" +
            "public class Iterables {\n" +
            "  public <T> @Nullable T getFirst(Iterable<? extends T> iterable, @Nullable T defaultValue) {\n" +
            "    return null;\n" +
            "  }\n" +
            "}\n");
        env.addClass(paths[1], "p1", "Multimap",
            "package p1;\n" +
            "import java.util.Collection;\n" +
            "public interface Multimap<K, V> {\n" +
            "   Collection<V> get(@Nullable K key);\n" +
            "}\n");
        env.addClass(paths[1], "p1", "Nullable",
            "package p1;\n" +
            "import java.lang.annotation.*;\n" +
            "@Documented\n" +
            "@Retention(value = RetentionPolicy.RUNTIME)\n" +
            "@Target(value = {ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})\n" +
            "public @interface Nullable {\n" +
            "}\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p1.Iterables", "p1.Multimap", "p1.Nullable");
        expectingNoProblems();

        //@formatter:off
        env.addGroovyClass(paths[1], "p2", "Script",
            "package p2\n" +
            "import p1.Iterables\n" +
            "import p1.Multimap\n" +
            "Multimap<String, String> getParams() {\n" +
            "}\n" +
            "def parseString(String string) {\n" +
            "}\n" +
            "def result = parseString(Iterables.getFirst(params.get('key'), null))\n");
        //@formatter:on

        incrementalBuild(paths[0]);
        expectingCompiledClasses("p2.Script");
        expectingNoProblems();
    }
}
