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
import java.util.List;
import java.util.StringTokenizer;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.CompilerUtils;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.builder.AbstractImageBuilder;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Version;

/**
 * Basic tests for the builder - compiling and running some very simple java and groovy code
 */
public final class BasicGroovyBuildTests extends BuilderTestSuite {

    @After
    public void tearDown() {
        JDTResolver.recordInstances = false;
    }

    private static MethodNode getMethodNode(ClassNode jcn, String selector, int paramCount) {
        List<MethodNode> mns = jcn.getDeclaredMethods(selector);
        for (MethodNode mn : mns) {
            if (mn.getParameters().length == paramCount) {
                return mn;
            }
        }
        return null;
    }

    private static void compareMethodNodes(MethodNode jmn, MethodNode mn) {
        System.out.println("\n\n\nComparing method nodes jmn=" + jmn + " mn=" + mn);
        System.out.println("Comparing return types");
        compareClassNodes(jmn.getReturnType(), mn.getReturnType(), 1);
        compareParameterArrays(jmn.getParameters(), mn.getParameters(), 1);
    }

    private static void compareParameterArrays(Parameter[] jps, Parameter[] ps, int d) {
        if (ps == null) {
            if (jps != null) {
                fail("Expected null parameters but was " + Arrays.toString(jps));
            }
        } else {
            if (ps.length != jps.length) {
                fail("Expected same number of parameters, should be " + Arrays.toString(ps) + " but was " + Arrays.toString(jps));
            }
            for (int p = 0; p < ps.length; p++) {
                System.out.println("Comparing parameters jp=" + jps[p] + " p=" + ps[p]);
                compareParameters(jps[p], ps[p], d + 1);
            }
        }
    }

    private static void compareParameters(Parameter jp, Parameter p,int d) {
        compareClassNodes(jp.getType(),p.getType(),d+1);
    }

    // check whether these are identical (in everything except name!)
    private static void compareClassNodes(ClassNode jcn, ClassNode cn, int d) {
        System.out.println("Comparing ClassNodes\njcn=" + jcn.toString() + "\n cn=" + cn.toString());
        assertEquals(cn.isGenericsPlaceHolder(), jcn.isGenericsPlaceHolder());

        // Check GenericsType info
        GenericsType[] gt_cn = cn.getGenericsTypes();
        GenericsType[] gt_jcn = jcn.getGenericsTypes();
        if (gt_cn == null) {
            if (gt_jcn != null) {
                fail("Should have been null but was " + Arrays.toString(gt_jcn));
            }
        } else {
            if (gt_jcn == null) {
                fail("Did not expect genericstypes to be null, should be " + Arrays.toString(gt_cn));
            }
            assertNotNull(gt_jcn);
            assertEquals(gt_cn.length, gt_jcn.length);
            for (int i = 0; i < gt_cn.length; i++) {
                System.out.println("Comparing generics types information, index #" + i);
                compareGenericsTypes(gt_jcn[i], gt_cn[i], d + 1);
            }
        }
    }

    private static void compareGenericsTypes(GenericsType jgt, GenericsType gt, int d) {
        //      assertEquals(jgt.getText(),gt.getText());
        assertEquals(jgt.getName(), gt.getName());
        assertEquals(jgt.isPlaceholder(), gt.isPlaceholder());
        assertEquals(jgt.isResolved(), gt.isResolved());
        assertEquals(jgt.isWildcard(), gt.isWildcard());
        compareType(jgt.getType(), gt.getType(), d + 1);
        compareUpperBounds(jgt.getUpperBounds(), gt.getUpperBounds(), d + 1);
        compareLowerBound(jgt.getLowerBound(), gt.getLowerBound(), d + 1);
    }

    private static void compareType(ClassNode jcn, ClassNode cn, int d) {
        System.out.println("Compare type of GenericsType: jcn=" + jcn + " cn=" + cn);
        compareClassNodes(jcn, cn, d + 1);
    }

    private static void compareUpperBounds(ClassNode[] jcnlist, ClassNode[] cnlist, int d) {
        System.out.println("Comparing upper bounds: jcn=" + Arrays.toString(jcnlist) + " cn=" + Arrays.toString(cnlist));
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

    private static void compareLowerBound(ClassNode jcn, ClassNode cn, int d) {
        System.out.println("Comparing lower bound");
        if (jcn == null) {
            assertNull(cn);
        } else {
            assertNotNull(cn);
            compareClassNodes(jcn.redirect(), cn.redirect(), d + 1);
        }
    }

    private static String toTask(String tasktag, String message) {
        return tasktag + message;
    }

    //--------------------------------------------------------------------------

    /**
     * Testing that the classpath computation works for multi dependent
     * projects. This classpath will be used for the ast transform loader.
     */
    @Test
    public void testMultiProjectDependenciesAndAstTransformClasspath() throws Exception {

        // Construct ProjectA
        IPath projectAPath = env.addProject("ProjectA");
        env.addExternalJars(projectAPath, Util.getJavaClassLibs());
        fullBuild(projectAPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectAPath, "");
        IPath rootA = env.addPackageFragmentRoot(projectAPath, "src");
        env.setOutputFolder(projectAPath, "bin");

        env.addClass(rootA, "p1", "Hello", "package p1;\n"
                + "public class Hello {\n"
                + "   public static void main(String[] args) {\n"
                + "      System.out.println(\"Hello world\");\n" + "   }\n"
                + "}\n");

        // Construct ProjectB
        IPath projectBPath = env.addProject("ProjectB");
        env.addExternalJars(projectBPath, Util.getJavaClassLibs());
        fullBuild(projectBPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectBPath, "");
        IPath rootB = env.addPackageFragmentRoot(projectBPath, "src");
        env.setOutputFolder(projectBPath, "bin");

        env.addClass(rootB, "p1", "Hello", "package p1;\n"
                + "public class Hello {\n"
                + "   public static void main(String[] args) {\n"
                + "      System.out.println(\"Hello world\");\n" + "   }\n"
                + "}\n");

        env.addRequiredProject(projectBPath, projectAPath, new IPath[] {}/* include all */,
                new IPath[] {}/* exclude none */, true);

        // Construct ProjectC
        IPath projectCPath = env.addProject("ProjectC");
        env.addExternalJars(projectCPath, Util.getJavaClassLibs());
        fullBuild(projectCPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectCPath, "");
        IPath rootC = env.addPackageFragmentRoot(projectCPath, "src");
        env.setOutputFolder(projectCPath, "bin");

        env.addClass(rootC, "p1", "Hello", "package p1;\n"
                + "public class Hello {\n"
                + "   public static void main(String[] args) {\n"
                + "      System.out.println(\"Hello world\");\n" + "   }\n"
                + "}\n");
        env.addRequiredProject(projectCPath, projectBPath, new IPath[] {}/* include all */,
                new IPath[] {}/* exclude none */, true);

        // Construct ProjectD
        IPath projectDPath = env.addProject("ProjectD");
        env.addExternalJars(projectDPath, Util.getJavaClassLibs());
        fullBuild(projectDPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectDPath, "");
        IPath rootD = env.addPackageFragmentRoot(projectDPath, "src");
        env.setOutputFolder(projectDPath, "bin");

        env.addClass(rootD, "p1", "Hello", "package p1;\n"
                + "public class Hello {\n"
                + "   public static void main(String[] args) {\n"
                + "      System.out.println(\"Hello world\");\n" + "   }\n"
                + "}\n");
        env.addRequiredProject(projectDPath, projectCPath, new IPath[] {}/* include all */,
                new IPath[] {}/* exclude none */, true);

        // incrementalBuild(projectAPath);
        // expectingCompiledClasses("p1.Hello");
        // expectingNoProblems();
        // executeClass(projectAPath, "p1.Hello", "Hello world", "");

        incrementalBuild(projectDPath);
        expectingCompiledClasses("p1.Hello");
        expectingNoProblems();
//		executeClass(projectDPath, "p1.Hello", "Hello world", "");

        String classpathForProjectD = CompilerUtils.calculateClasspath(env.getJavaProject(projectDPath));

        StringTokenizer st = new StringTokenizer(classpathForProjectD,
                File.pathSeparator);
        // look at how project A and B manifest in this classpath
        boolean foundAndCheckedA = false;
        boolean foundAndCheckedB = false;
        boolean foundAndCheckedC = false;
        while (st.hasMoreElements()) {
            String pathElement = st.nextToken();

            // ProjectA is on ProjectDs classpath indirectly. It is on ProjectBs
            // classpath and re-exported
            if (pathElement.indexOf("ProjectA") != -1) {
                // System.out.println("ProjectA element is ["+pathElement+"]");
                if (pathElement.indexOf("ProjectA") == 1) {
                    fail("Path element looks incorrect.  Path for ProjectA should be an absolute location, not ["
                            + pathElement + "]");
                }
                if (!pathElement.endsWith("bin")) {
                    fail("Expected pathelement to end with the output folder 'bin', but it did not: ["
                            + pathElement + "]");
                }
                foundAndCheckedA = true;
            }

            // ProjectB is on ProjectDs classpath indirectly. It is on ProjectCs
            // classpath and re-exported
            if (pathElement.indexOf("ProjectB") != -1) {
                System.out.println("ProjectB element is [" + pathElement + "]");
                if (pathElement.indexOf("ProjectB") == 1) {
                    fail("Path element looks incorrect.  Path for ProjectB should be an absolute location, not ["
                            + pathElement + "]");
                }
                if (!pathElement.endsWith("bin")) {
                    fail("Expected pathelement to end with the output folder 'bin', but it did not: ["
                            + pathElement + "]");
                }
                foundAndCheckedB = true;
            }
            // ProjectB is directly on ProjectCs classpath
            if (pathElement.indexOf("ProjectC") != -1) {
                System.out.println("ProjectC element is [" + pathElement + "]");
                if (pathElement.indexOf("ProjectC") == 1) {
                    fail("Path element looks incorrect.  Path for ProjectC should be an absolute location, not ["
                            + pathElement + "]");
                }
                if (!pathElement.endsWith("bin")) {
                    fail("Expected pathelement to end with the output folder 'bin', but it did not: ["
                            + pathElement + "]");
                }
                foundAndCheckedC = true;
            }
        }
        if (!foundAndCheckedC) {
            fail("Unable to check entry for ProjectC in the classpath, didn't find it:\n"
                    + classpathForProjectD);
        }
        if (!foundAndCheckedB) {
            fail("Unable to check entry for ProjectB in the classpath, didn't find it:\n"
                    + classpathForProjectD);
        }
        if (!foundAndCheckedA) {
            fail("Unable to check entry for ProjectA in the classpath, didn't find it:\n"
                    + classpathForProjectD);
        }
        // System.out.println(">>"+classpathForProjectD);
    }

    @Test
    public void testBuildJavaHelloWorld() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addClass(root, "p1", "Hello", "package p1;\n"
                + "public class Hello {\n"
                + "   public static void main(String[] args) {\n"
                + "      System.out.println(\"Hello world\");\n" + "   }\n"
                + "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("p1.Hello");
        expectingNoProblems();
        executeClass(projectPath, "p1.Hello", "Hello world", "");
    }

    @Test
    public void testGenericsDefaultParams_1717() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addClass(root, "p1", "Demo", "package p1;\n"
                + "public class Demo {\n"
                + "   public static void main(String[] args) {\n"
                + "      SomeGroovyHelper.doit(String.class,null);\n"
                + "   }\n"
                + "}\n");

        env.addGroovyClass(root, "p1", "SomeGroovyHelper", "package p1;\n"
                + "class SomeGroovyHelper {\n"
                + "   static <T> List<T> doit(Class<T> factoryClass, ClassLoader classLoader = SomeGroovyHelper.class.classLoader) {\n"
                + "      null\n"
                + "   }\n"
                + "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("p1.Demo","p1.SomeGroovyHelper");
        expectingNoProblems();
    }

    @Test
    public void testNPEAnno_1398() throws Exception {
        IPath projectPath = env.addProject("Project", "1.5");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addClass(root, "", "Anno", "public @interface Anno {\n"
                + "	String[] value();\n"
                + "}");

        env.addGroovyClass(root, "", "Const", "public class Const {\n"
                + "	public static final String instance= \"abc\";\n"
                + "}");

        incrementalBuild(projectPath);
        expectingCompiledClasses("Anno", "Const");
        expectingNoProblems();

        env.addGroovyClass(root, "", "A", "@Anno(Const.instance)\n" + "class A {}");
        incrementalBuild(projectPath);
        expectingNoProblems();
        expectingCompiledClasses("A");
    }

    @Test @Ignore
    public void testCompileStatic_1505() throws Exception {
        assumeTrue(isAtLeastGroovy(20));

        IPath projectPath = env.addProject("Project","1.6");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        JDTResolver.recordInstances = true;

        env.addGroovyClass(root, "", "Foo",
                "import groovy.transform.CompileStatic\n"+
                "@CompileStatic\n"+
                "void method(String message) {\n"+
                "   Collection<Integer> cs;\n"+
//					"   List<Integer> ls = new ArrayList<Integer>();\n"+
//					"   ls.add(123);\n"+
//					"   ls.add('abc');\n"+
                // GRECLIPSE-1511 code
                "	List<String> second = []\n"+
                "	List<String> artefactResources2\n"+
                "	second.addAll(artefactResources2)\n"+
                "}\n"
//					"interface List2<E> extends Collection<E> {\n"+
//					"  boolean add(E e);\n" +
//					"}"
                );

        incrementalBuild(projectPath);
//			expectingCompiledClasses("Foo","List2");
        expectingNoProblems();

        // Now compare the generics structure for List (built by jdtresolver mapping into groovy) against List2 (built by groovy)

        // Access the jdtresolver bits and pieces

        JDTClassNode jcn = JDTResolver.getCachedNode("java.util.Collection<E>");

        assertNotNull(jcn);
        System.out.println("JDT ClassNode="+jcn);
//			JDTClassNode jcn2 = jdtr.getCachedNode("List2");
//			System.out.println(jcn2);

        ClassNode listcn = new ClassNode(java.util.Collection.class);
        VMPluginFactory.getPlugin().setAdditionalClassInformation(listcn);
        listcn.lazyClassInit();
        System.out.println("Groovy ClassNode="+listcn);

//			IJavaProject ijp = env.getJavaProject("Project");
//			GroovyCompilationUnit unit = (GroovyCompilationUnit) ijp.findType("Foo")
//					.getCompilationUnit();

        // now find the class reference
//			ClassNode cn = unit.getModuleNode().getClasses().get(1);
//			System.out.println(cn);

        // Compare java.util.List from JDTClassNode and List2 from groovy
        compareClassNodes(jcn.redirect(),listcn.redirect(),0);
        MethodNode jmn = getMethodNode(jcn,"add",1); // boolean add(E)
        MethodNode rmn = getMethodNode(listcn,"add",1);
        compareMethodNodes(jmn,rmn);

        jmn = getMethodNode(jcn,"addAll",1);
        rmn = getMethodNode(listcn,"addAll",1);
        compareMethodNodes(jmn,rmn);

        // Want to compare type information in the
        // env.addClass(root, "", "Client", "public class Client {\n"
        // + "  { new Outer.Inner(); }\n" + "}\n");
        // incrementalBuild(projectPath);
        // expectingNoProblems();
        // expectingCompiledClasses("Client");
    }

    @Test @Ignore
    public void testCompileStatic_1506() throws Exception {
        assumeTrue(isAtLeastGroovy(20));

        IPath projectPath = env.addProject("Project","1.6");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        JDTResolver.recordInstances = true;

        env.addGroovyClass(root, "", "Foo",
                "import groovy.transform.CompileStatic\n"+
                "@CompileStatic\n"+
                "void method(String message) {\n"+
                "   Collection<Integer> cs;\n"+
//					"   List<Integer> ls = new ArrayList<Integer>();\n"+
//					"   ls.add(123);\n"+
//					"   ls.add('abc');\n"+
                // GRECLIPSE-1511 code
                "	List<String> second = []\n"+
                "	List<String> artefactResources2\n"+
                "	second.addAll(artefactResources2)\n"+
                "}\n"+
                "interface ListOfFile extends ArrayList<File> {\n"+
                "}"
                );

        incrementalBuild(projectPath);
//			expectingCompiledClasses("Foo","List2");
        expectingNoProblems();

        // Now compare the generics structure for List (built by jdtresolver mapping into groovy) against List2 (built by groovy)

        // Access the jdtresolver bits and pieces

        JDTClassNode jcn = JDTResolver.getCachedNode("ListOfFile");

        assertNotNull(jcn);
        System.out.println("JDT ClassNode="+jcn);
//			JDTClassNode jcn2 = jdtr.getCachedNode("List2");
//			System.out.println(jcn2);

//			List<File> C = new ArrayList<File>();
        ClassNode listcn = new ClassNode(java.util.Collection.class);
        VMPluginFactory.getPlugin().setAdditionalClassInformation(listcn);
        listcn.lazyClassInit();
        System.out.println("Groovy ClassNode="+listcn);

//			IJavaProject ijp = env.getJavaProject("Project");
//			GroovyCompilationUnit unit = (GroovyCompilationUnit) ijp.findType("Foo")
//					.getCompilationUnit();

        // now find the class reference
//			ClassNode cn = unit.getModuleNode().getClasses().get(1);
//			System.out.println(cn);

        // Compare java.util.List from JDTClassNode and List2 from groovy
        compareClassNodes(jcn.redirect(),listcn.redirect(),0);
        MethodNode jmn = getMethodNode(jcn,"add",1); // boolean add(E)
        MethodNode rmn = getMethodNode(listcn,"add",1);
        compareMethodNodes(jmn,rmn);

        jmn = getMethodNode(jcn,"addAll",1);
        rmn = getMethodNode(listcn,"addAll",1);
        compareMethodNodes(jmn,rmn);

        // Want to compare type information in the
        // env.addClass(root, "", "Client", "public class Client {\n"
        // + "  { new Outer.Inner(); }\n" + "}\n");
        // incrementalBuild(projectPath);
        // expectingNoProblems();
        // expectingCompiledClasses("Client");
    }

    @Test
    public void testCompileStatic_ArrayArray() throws Exception {
        assumeTrue(isAtLeastGroovy(20));

        IPath projectPath = env.addProject("Project","1.6");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        JDTResolver.recordInstances = true;

        env.addClass(root, "", "ISourceRange", "class ISourceRange {}");
        env.addClass(root, "", "TypeNameMatch",	"class TypeNameMatch {}");

        env.addClass(root, "", "IChooseImportQuery",
                "interface IChooseImportQuery {\n"+
                "    TypeNameMatch[] chooseImports(TypeNameMatch[][] matches, ISourceRange[] range);\n"+
                "}"
                );

        env.addGroovyClass(root, "", "NoChoiceQuery",
                "class NoChoiceQuery implements IChooseImportQuery {\n"+
                "    public TypeNameMatch[] chooseImports(TypeNameMatch[][] matches, ISourceRange[] range) {\n"+
                "        throw new Exception(\"Should not have a choice, but found $matches[0][0] and $matches[0][1]\")\n"+
                "        return []\n"+
                "    }\n"+
                "}"
                );

        incrementalBuild(projectPath);
        expectingNoProblems();
        expectingCompiledClasses("IChooseImportQuery","ISourceRange","NoChoiceQuery","TypeNameMatch");
    }

    @Test
    public void testCompileStatic_FileAddAll() throws Exception {
        assumeTrue(isAtLeastGroovy(20));

        IPath projectPath = env.addProject("Project","1.6");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        JDTResolver.recordInstances = true;

        env.addGroovyClass(root, "", "Foo",
                "import groovy.transform.CompileStatic\n"+
                "@CompileStatic\n"+
                "class Foo {\n"+
                "List<String> jvmArgs = new ArrayList<String>();\n"+
                " void method(String message) {\n"+
                "   List<String> cmd = ['java'];\n"+
                "	cmd.addAll(jvmArgs);\n"+
                " }\n"+
                "}\n"
                );

        incrementalBuild(projectPath);
        expectingNoProblems();
        expectingCompiledClasses("Foo");

        // Now compare the generics structure for List (built by jdtresolver mapping into groovy) against List2 (built by groovy)
    }

    @Test
    public void testCompileStatic_ListFileArgIteratedOver() throws Exception {
        assumeTrue(isAtLeastGroovy(20));

        IPath projectPath = env.addProject("Project","1.6");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        JDTResolver.recordInstances = true;

        env.addGroovyClass(root, "", "Foo",
                "import groovy.transform.CompileStatic\n"+
                "class Foo {\n"+
                "@CompileStatic\n"+
                "private populateSourceDirectories() {\n"+
                "	List<File> pluginDependencies\n"+
                "  for (zip in pluginDependencies) {\n"+
                "    registerPluginZipWithScope(zip);\n"+
                "  }\n"+
                "}\n"+
                "private void registerPluginZipWithScope(File pluginzip) {}\n"+
                "}\n"
                );

        incrementalBuild(projectPath);
        expectingNoProblems();
        expectingCompiledClasses("Foo");

        // Now compare the generics structure for List (built by jdtresolver mapping into groovy) against List2 (built by groovy)
    }

    @Test
    public void testCompileStatic_IterableParameter() throws Exception {
        assumeTrue(isAtLeastGroovy(20));

        IPath projectPath = env.addProject("Project","1.6");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        JDTResolver.recordInstances = true;

        env.addGroovyClass(root, "", "Foo",
                "import groovy.transform.CompileStatic\n"+
                "@CompileStatic\n"+
                "class Foo {\n"+
                "private populateSourceDirectories() {\n"+
                "	List<File> pluginDependencies\n"+
                "   foo(pluginDependencies);\n"+
                "}\n"+
                "private void foo(Iterable<File> iterable) {}\n"+
                "}\n"
                );

        incrementalBuild(projectPath);
        expectingNoProblems();
        expectingCompiledClasses("Foo");

        // Now compare the generics structure for List (built by jdtresolver mapping into groovy) against List2 (built by groovy)
    }

    @Test
    public void testCompileStatic_BuildSettings() throws Exception {
        assumeTrue(isAtLeastGroovy(20));

        IPath projectPath = env.addProject("Project","1.6");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        JDTResolver.recordInstances = true;

        env.addGroovyClass(root, "", "BuildSettings",
                "import groovy.transform.CompileStatic\n"+
                "\n"+
                "class BuildSettings  {\n"+
                "\n"+
                "   List<File> compileDependencies = []\n"+
                "	List<File> defaultCompileDependencies = []\n"+
                "\n"+
                "    @CompileStatic\n"+
                "    void getCompileDependencies() {\n"+
                "        compileDependencies += defaultCompileDependencies\n"+
                "    }\n"+
                "\n"+
                "}\n");

        incrementalBuild(projectPath);
        expectingNoProblems();
        expectingCompiledClasses("BuildSettings");

        // Now compare the generics structure for List (built by jdtresolver mapping into groovy) against List2 (built by groovy)
    }

    @Test
    public void testInners_983() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "", "Outer", "class Outer {\n"
                + "  static class Inner {}\n" + "}\n");

        env.addClass(root, "", "Client", "public class Client {\n"
                + "  { new Outer.Inner(); }\n" + "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("Client", "Outer", "Outer$Inner");
        expectingNoProblems();
        env.addClass(root, "", "Client", "public class Client {\n"
                + "  { new Outer.Inner(); }\n" + "}\n");
        incrementalBuild(projectPath);
        expectingNoProblems();
        expectingCompiledClasses("Client");

    }

    @Test
    public void testCompileStatic() throws Exception {
        assumeTrue(isAtLeastGroovy(20));

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "", "Outer",
                "import groovy.transform.CompileStatic;\n"
                        + "@CompileStatic \n" + "int fact(int n) {\n"
                        + "	if (n==1) {return 1;\n"
                        + "	} else {return n+fact(n-1);}\n" + "}\n");

        // env.addClass(root, "", "Client",
        // "public class Client {\n"+
        // "  { new Outer.Inner(); }\n"+
        // "}\n"
        // );

        incrementalBuild(projectPath);
        expectingCompiledClasses("Outer");
        expectingNoProblems();
        // env.addClass(root, "", "Client", "public class Client {\n"
        // + "  { new Outer.Inner(); }\n" + "}\n");
        // incrementalBuild(projectPath);
        // expectingNoProblems();
        // expectingCompiledClasses("Client");

    }

    @Test // verify generics are correct for the 'Closure<?>' as CompileStatic will attempt an exact match
    public void testCompileStatic2() throws Exception {
        assumeTrue(isAtLeastGroovy(20));

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "", "A",
                "class A {\n"+
                "	public void profile(String name, groovy.lang.Closure<?> callable) {	}\n"+
                "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("A");
        expectingNoProblems();

        env.addGroovyClass(root, "", "B",
                "@groovy.transform.CompileStatic\n"+
                "class B extends A {\n"+
                "\n"+
                "	def foo() {\n"+
                "		profile(\"creating plugin manager with classes\") {\n"+
                "			System.out.println('abc');\n"+
                "		}\n"+
                "	}\n"+
                "\n"+
                "}\n");
        incrementalBuild(projectPath);
        expectingCompiledClasses("B","B$_foo_closure1");
        expectingNoProblems();
    }

    @Test
    public void testCompileStatic3() throws Exception {
        assumeTrue(isAtLeastGroovy(20));

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "", "Foo",
                "class Foo {\n"+
                "	@groovy.transform.CompileStatic\n"+
                "	public static void main(String[] args) {\n"+
                "		((GroovyObject)new Foo());\n"+
                "	}\n"+
                "}\n");

        incrementalBuild(projectPath);
        expectingNoProblems();
        expectingCompiledClasses("Foo");
    }

    @Test
    public void testCompileStatic_MapEachClosure() throws Exception {
        assumeTrue(isAtLeastGroovy(20));

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "", "Demo",
                "@groovy.transform.CompileStatic\n"+
                "class Demo {\n"+
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

        incrementalBuild(projectPath);
        expectingNoProblems();
    }

    @Test
    public void test1167() throws Exception {
        IPath projectPath = env.addProject("Project", "1.5");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(
                root,
                "brooklyn.event.adapter",
                "HttpSensorAdapter",
                "package brooklyn.event.adapter\n"
                        + "\n"
                        + "public class HttpSensorAdapter {}\n"
                        + "\n"
                        + "public class Foo implements ValueProvider<String> {\n"
                        + "public String compute() {\n" + "  return null\n"
                        + "}\n" + "}");

        env.addGroovyClass(root, "brooklyn.event.adapter", "ValueProvider",
                "package brooklyn.event.adapter\n" + "\n"
                        + "public interface ValueProvider<T> {\n"
                        + "  public T compute();\n" + "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("brooklyn.event.adapter.Foo",
                "brooklyn.event.adapter.HttpSensorAdapter",
                "brooklyn.event.adapter.ValueProvider");
        expectingNoProblems();
        env.addGroovyClass(
                root,
                "brooklyn.event.adapter",
                "HttpSensorAdapter",
                "package brooklyn.event.adapter\n"
                        + "\n"
                        + "public class HttpSensorAdapter {}\n"
                        + "\n"
                        + "public class Foo implements ValueProvider<String> {\n"
                        + "public String compute() {\n" + "  return null\n"
                        + "}\n" + "}");
        incrementalBuild(projectPath);
        expectingNoProblems();
        expectingCompiledClasses("brooklyn.event.adapter.Foo",
                "brooklyn.event.adapter.HttpSensorAdapter");

    }

    @Test @Ignore
    public void testScriptSupport() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        // The fact that this is 'scripts' should cause us to suppress the .class file
        IPath root = env.addPackageFragmentRoot(projectPath, "scripts");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "p1", "Hello",
            "package p1;\n" +
            "public class Hello {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.println('Hello world')\n" +
            "  }\n" +
            "}");

        incrementalBuild(projectPath);
        expectingCompiledClasses("");
        expectingNoProblems();
    }

    @Test
    public void testTypeDuplication_GRE796_1() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "p", "Foo", "package p;\n" + "class Foo{}\n");

        IPath root2 = env.addPackageFragmentRoot(projectPath, "src2");
        env.setOutputFolder(projectPath, "bin");

        IPath pathToSecond = env.addGroovyClass(root2, "p", "Foo",
                "package p;\n" + "class Foo {}\n");

        incrementalBuild(projectPath);
        Problem[] probs = env.getProblemsFor(pathToSecond);
        boolean p1found = false;
        boolean p2found = false;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i].getMessage().equals("The type Foo is already defined")) {
                p1found = true;
            }
            if (probs[i].getMessage().startsWith(
                    "Groovy:Invalid duplicate class definition of class p.Foo")) {
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
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "p", "Foo", "package p;\n" + "class Foo {}\n");

        IPath root2 = env.addPackageFragmentRoot(projectPath, "src2");
        env.setOutputFolder(projectPath, "bin");

        IPath pathToSecond = env.addGroovyClass(root2, "p", "Foo",
                "package p;\n" + "class Foo {}\n");

        incrementalBuild(projectPath);
        Problem[] probs = env.getProblemsFor(pathToSecond);
        boolean p1found = false;
        boolean p2found = false;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i].getMessage().equals("The type Foo is already defined")) {
                p1found = true;
            }
            if (probs[i].getMessage().startsWith(
                    "Groovy:Invalid duplicate class definition of class p.Foo")) {
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
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath cuPath = env.addClass(root, "com.acme", "Foo",
                "package com.acme;\n" + "public class Foo {}\n");

        env.addGroovyClass(root, "com.acme.Foo", "xyz", "print 'abc'");

        incrementalBuild(projectPath);
        expectingSpecificProblemFor(cuPath, new Problem("",
                "The type Foo collides with a package", cuPath, 31, 34,
                CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_WARNING));
        expectingCompiledClasses("com.acme.Foo", "xyz");
        executeClass(projectPath, "xyz", "abc", null);
    }

    @Test
    public void testSlowAnotherAttempt_GRE870() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "a.b.c.d.e.f", "Helper1",
                "package a.b.c.d.e.f\n" + "class Helper1 {}\n");
        env.addGroovyClass(root, "a.b.c.d.e.f", "Helper2",
                "package a.b.c.d.e.f\n" + "class Helper2 {}\n");
        env.addGroovyClass(root, "a.b.c.d.e.f", "Helper3",
                "package a.b.c.d.e.f\n" + "class Helper3 {}\n");
        env.addGroovyClass(root, "a.b.c.d.e.f", "Helper4",
                "package a.b.c.d.e.f\n" + "class Helper4 {}\n");
        env.addGroovyClass(root, "a.b.c.d.e.f", "Helper5",
                "package a.b.c.d.e.f\n" + "class Helper5 {}\n");
        env.addGroovyClass(root, "a.b.c.d.e.f", "Helper6",
                "package a.b.c.d.e.f\n" + "class Helper6 {}\n");
        env.addGroovyClass(root, "a.b.c.d.e.f", "Helper7",
                "package a.b.c.d.e.f\n" + "class Helper7 {}\n");
        env.addGroovyClass(root, "a.b.c.d.e.f", "Helper8",
                "package a.b.c.d.e.f\n" + "class Helper8 {}\n");
        env.addGroovyClass(root, "a.b.c.d.e.f", "Helper9",
                "package a.b.c.d.e.f\n" + "class Helper9 {}\n");
        env.addGroovyClass(root, "a.b.c.d.e.f", "HelperBase",
                "package a.b.c.d.e.f\n" + "class HelperBase {\n"
                        + "	static final String TYPE = 'test'\n" + "}\n");
        env.addGroovyClass(root, "a.b.c.d.e.f", "SomeHelper",
                "package a.b.c.d.e.f\n"
                        + "class SomeHelper extends HelperBase {}\n");

        env.addGroovyClass(root, "a.b.c.d.e.f", "SomeTests",
                "package a.b.c.d.e.f\n" + "\n"
                        + "import static a.b.c.d.e.f.Helper1.*\n"
                        + "import static a.b.c.d.e.f.Helper2.*\n"
                        + "import static a.b.c.d.e.f.Helper3.*\n"
                        + "import static a.b.c.d.e.f.Helper4.*\n"
                        + "import static a.b.c.d.e.f.Helper5.*\n"
                        + "import static a.b.c.d.e.f.Helper6.*\n"
                        + "import static a.b.c.d.e.f.Helper7.*\n"
                        + "import static a.b.c.d.e.f.Helper8.*\n"
                        + "import static a.b.c.d.e.f.Helper9.*\n" + "\n"
                        + "import static a.b.c.d.e.f.SomeHelper.*\n" + "\n"
                        + "class SomeTests {\n" + "\n"
                        + "    public void test1() {\n"
                        + "		def details = [:]\n" + "\n"
                        + "        assert details[TYPE] == 'test' \n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test' \n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "        assert details[TYPE] == 'test'\n"
                        + "    }\n" + "}\n");

        // TODO how to create a reliable timed test? This should take about
        // 2-3seconds, not > 10 - at least on my machine ;)

        env.setOutputFolder(projectPath, "bin");

        incrementalBuild(projectPath);
        // lots of errors on the missing static imports
        expectingCompiledClasses("a.b.c.d.e.f.Helper1,a.b.c.d.e.f.Helper2,a.b.c.d.e.f.Helper3,a.b.c.d.e.f.Helper4,a.b.c.d.e.f.Helper5,a.b.c.d.e.f.Helper6,a.b.c.d.e.f.Helper7,a.b.c.d.e.f.Helper8,a.b.c.d.e.f.Helper9,a.b.c.d.e.f.HelperBase,a.b.c.d.e.f.SomeHelper,a.b.c.d.e.f.SomeTests");

    }

    @Test
    public void testSlow_GRE870() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "", "Test1", "import static some.Class0.*;\n"
                + "import static some.Class1.*;\n"
                + "import static some.Class2.*;\n"
                + "import static some.Class3.*;\n"
                + "import static some.Class4.*;\n"
                + "import static some.Class5.*;\n"
                + "import static some.Class6.*;\n"
                + "import static some.Class7.*;\n"
                + "import static some.Class8.*;\n"
                + "import static some.Class9.*;\n"
                + "import static some.Class10.*;\n" + "\n" + "class Test1 {}\n");
        env.addGroovyClass(root, "some", "Foo", "\n" + "class Foo {}\n");

        // TODO could guard on this test requiring execution in less than
        // 2mins...

        env.setOutputFolder(projectPath, "bin");

        incrementalBuild(projectPath);
        // lots of errors on the missing static imports
        expectingCompiledClasses("Foo", "Test1");

    }

    @Test
    public void testReallySlow_GRE870() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "", "Test1", "import static some.Class0.*;\n"
                + "import static some.Class1.*;\n"
                + "import static some.Class2.*;\n"
                + "import static some.Class3.*;\n"
                + "import static some.Class4.*;\n"
                + "import static some.Class5.*;\n"
                + "import static some.Class6.*;\n"
                + "import static some.Class7.*;\n"
                + "import static some.Class8.*;\n"
                + "import static some.Class9.*;\n"
                + "import static some.Class10.*;\n"
                + "import static some.Class11.*;\n"
                + "import static some.Class12.*;\n"
                + "import static some.Class13.*;\n"
                + "import static some.Class14.*;\n"
                + "import static some.Class15.*;\n"
                + "import static some.Class16.*;\n"
                + "import static some.Class17.*;\n"
                + "import static some.Class18.*;\n"
                + "import static some.Class19.*;\n"
                + "import static some.Class20.*;\n"
                + "import static some.Class21.*;\n"
                + "import static some.Class22.*;\n"
                + "import static some.Class23.*;\n"
                + "import static some.Class24.*;\n"
                + "import static some.Class25.*;\n"
                + "import static some.Class26.*;\n"
                + "import static some.Class27.*;\n"
                + "import static some.Class28.*;\n"
                + "import static some.Class29.*;\n"
                + "import static some.Class30.*;\n"
                + "import static some.Class31.*;\n"
                + "import static some.Class32.*;\n"
                + "import static some.Class33.*;\n"
                + "import static some.Class34.*;\n"
                + "import static some.Class35.*;\n"
                + "import static some.Class36.*;\n"
                + "import static some.Class37.*;\n"
                + "import static some.Class38.*;\n"
                + "import static some.Class39.*;\n"
                + "import static some.Class40.*;\n"
                + "import static some.Class41.*;\n"
                + "import static some.Class42.*;\n"
                + "import static some.Class43.*;\n"
                + "import static some.Class44.*;\n"
                + "import static some.Class45.*;\n"
                + "import static some.Class46.*;\n"
                + "import static some.Class47.*;\n"
                + "import static some.Class48.*;\n"
                + "import static some.Class49.*;\n"
                + "import static some.Class50.*;\n" + "\n" + "class Test1 {}\n"

        );

        // TODO could guard on this test requiring execution in less than
        // 2mins...

        env.setOutputFolder(projectPath, "bin");

        incrementalBuild(projectPath);
        // lots of errors on the missing static imports
        expectingCompiledClasses("Test1");

    }

    @Test
    public void testClosureBasics() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "", "Coroutine", "def iterate(n, closure) {\n"
                + "  1.upto(n) {\n" + "    closure(it);\n" + "  }\n" + "}\n"
                + "iterate (3) {\n" + "  print it*2\n" + "}\n");

        // three classes created for that:
        // GroovyClass(name=Coroutine bytes=6372),
        // GroovyClass(name=Coroutine$_run_closure1 bytes=2875),
        // GroovyClass(name=Coroutine$_iterate_closure2 bytes=3178)

        incrementalBuild(projectPath);
        expectingCompiledClasses("Coroutine", "Coroutine$_run_closure1",
                "Coroutine$_iterate_closure2");
        expectingNoProblems();
        executeClass(projectPath, "Coroutine", "246", "");
    }

    @Test
    public void testPackageNames_GRE342_1() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        // q.X declared in p.X
        IPath path = env.addGroovyClass(root, "p", "X", "package q\n"
                + "class X {}");

        incrementalBuild(projectPath);

        expectingSpecificProblemFor(path, new Problem("p/X", "The declared package \"q\" does not match the expected package \"p\"", path, 8, 9, 60, IMarker.SEVERITY_ERROR));
    }

    @Test
    public void testPackageNames_GRE342_2() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        // q.X declared in p.X
        IPath path = env.addGroovyClass(root, "p.q.r", "X", "package p.s.r.q\n" + "class X {}");

        incrementalBuild(projectPath);

        expectingSpecificProblemFor(path, new Problem("p/q/r/X", "The declared package \"p.s.r.q\" does not match the expected package \"p.q.r\"", path, 8, 15, 60, IMarker.SEVERITY_ERROR));
    }

    @Test
    public void testPackageNames_GRE342_3() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        // in p.q.r.X but has no package decl - should be OK
        env.addGroovyClass(root, "p.q.r", "X", "print 'abc'");

        incrementalBuild(projectPath);

        expectingNoProblems();
        executeClass(projectPath, "X", "abc", "");
    }

    @Test
    public void testAnnotationCollectorMultiProject() throws Exception {
        assumeTrue(isAtLeastGroovy(21));
        assumeTrue(JavaCore.getPlugin().getBundle().getVersion().compareTo(new Version("3.9.50")) >= 0);

        // Construct 'annotation' project that defines annotation using 'AnnotationsCollector'
        IPath annotationProject = env.addProject("annotation");
        env.addExternalJars(annotationProject, Util.getJavaClassLibs());
        env.addGroovyJars(annotationProject);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(annotationProject, "");

        IPath annotationRoot = env.addPackageFragmentRoot(annotationProject, "src");
        env.setOutputFolder(annotationProject, "bin");


        env.addGroovyClass(annotationRoot, "com.demo", "MyAnnotation",
                "package com.demo\r\n" +
                "\r\n" +
                "@groovy.transform.AnnotationCollector\r\n" +
                "@Deprecated\r\n" +
                "@interface MyAnnotation {}\r\n"
        );

        // Construct 'app' project that uses the annotation
        IPath appProject = env.addProject("app");
        env.addExternalJars(appProject, Util.getJavaClassLibs());
        env.addGroovyJars(appProject);
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(appProject, "");

        env.addRequiredProject(appProject, annotationProject);

        IPath appRoot = env.addPackageFragmentRoot(appProject, "src");
        env.setOutputFolder(appProject, "bin");

        env.addGroovyClass(appRoot, "com.demo", "Widget",
                "package com.demo\r\n" +
                "\r\n" +
                "@MyAnnotation\r\n" +
                "class Widget {}\r\n"
        );

        fullBuild();
        expectingCompiledClasses("com.demo.MyAnnotation", "com.demo.Widget");
        expectingNoProblems();
    }

    @Test
    public void testAnnotationCollectorIncremental() throws Exception {
        assumeTrue(isAtLeastGroovy(21));

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "", "NotNull",
                "import java.lang.annotation.*;\n"+
                "@Retention(RetentionPolicy.RUNTIME) @interface NotNull {}\n");

        env.addGroovyClass(root, "", "Length",
                "import java.lang.annotation.*;\n"+
                "@Retention(RetentionPolicy.RUNTIME) @interface Length {}\n");
        env.addGroovyClass(root, "", "ISBN",
                "import java.lang.annotation.*;\n"+
                "@NotNull @Length @groovy.transform.AnnotationCollector @interface ISBN {}\n");

        env.addGroovyClass(root, "", "Book",
                "import java.lang.annotation.Annotation;\n"+
                "import java.lang.reflect.Field;\n"+
                "\n"+
                "		class Book {\n"+
                "			@ISBN\n"+
                "			String isbn;\n"+
                "			\n"+
                "			public static void main(String[] args) {\n"+
                "				Field f = Book.class.getDeclaredField(\"isbn\");\n"+
                "				for (Annotation a: f.getDeclaredAnnotations()) {\n"+
                "					System.out.println(a);\n"+
                "				} \n"+
                "			}\n"+
                "		}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("Book","Length","NotNull","ISBN");
        expectingNoProblems();
        executeClass(projectPath, "Book", "@NotNull()\n@Length()\n", "");

        // whitespace change
        env.addGroovyClass(root, "", "Book",
                "import java.lang.annotation.Annotation;\n"+
                "import java.lang.reflect.Field;\n"+
                "\n"+
                "		class Book {  \n"+
                "			@ISBN\n"+
                "			String isbn;\n"+
                "			\n"+
                "			public static void main(String[] args) {\n"+
                "				Field f = Book.class.getDeclaredField(\"isbn\");\n"+
                "				for (Annotation a: f.getDeclaredAnnotations()) {\n"+
                "					System.out.println(a);\n"+
                "				} \n"+
                "			}\n"+
                "		}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("Book");
        expectingNoProblems();
        executeClass(projectPath, "Book", "@NotNull()\n@Length()\n", "");
    }

    @Test
    public void testClosureIncremental() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addClass(root, "", "Launch", "public class Launch {\n"
                + "  public static void main(String[]argv) {\n"
                + "    Runner.run(3);\n" + "  }\n" + "}\n");

        env.addGroovyClass(root, "", "Runner", "def static run(int n) { \n"
                + "  OtherGroovy.iterate (4) {\n" + "  print it*2\n" + "  }\n"
                + "}\n");

        // FIXASC this variant of the above seemed to crash groovy:
        // "def run(n) \n"+
        // "  OtherGroovy.iterate (3) {\n"+
        // "  print it*2\n"+
        // "  }\n");

        env.addGroovyClass(root, "pkg", "OtherGroovy",
                "def static iterate(Integer n, closure) {\n"
                        + "  1.upto(n) {\n" + "    closure(it);\n" + "  }\n"
                        + "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("OtherGroovy",
                "OtherGroovy$_iterate_closure1", "Runner",
                "Runner$_run_closure1", "Launch");
        expectingNoProblems();
        executeClass(projectPath, "Launch", "2468", "");

        // modify the body of the closure
        env.addGroovyClass(root, "", "Runner", "def static run(int n) { \n"
                + "  OtherGroovy.iterate (4) {\n" + "  print it\n" + // change
                                                                        // here
                "  }\n" + "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("Runner", "Runner$_run_closure1");
        expectingNoProblems();
        executeClass(projectPath, "Launch", "1234", "");

        // modify how the closure is called
        env.addGroovyClass(root, "pkg", "OtherGroovy",
                "def static iterate(Integer n, closure) {\n"
                        + "  1.upto(n*2) {\n" + // change here
                        "    closure(it);\n" + "  }\n" + "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("OtherGroovy",
                "OtherGroovy$_iterate_closure1");
        expectingNoProblems();
        executeClass(projectPath, "Launch", "12345678", "");

        // change the iterate method signature from Integer to int - should
        // trigger build of Runner
        env.addGroovyClass(root, "pkg", "OtherGroovy",
                "def static iterate(int n, closure) {\n" + "  1.upto(n*2) {\n" + // change
                                                                                    // here
                        "    closure(it);\n" + "  }\n" + "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("OtherGroovy",
                "OtherGroovy$_iterate_closure1", "Runner",
                "Runner$_run_closure1");
        expectingNoProblems();
        executeClass(projectPath, "Launch", "12345678", "");

    }

    /** Verify the processing in ASTTransformationCollectorCodeVisitor - to check it finds everything it expects. */
    @Test
    public void testSpock_GRE558() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        env.addJar(projectPath, "lib/junit-4.12.jar");
        env.addJar(projectPath, "lib/spock-core-0.4-groovy-1.7-SNAPSHOT.jar");
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(
                root,
                "",
                "MyTest",
                "import org.junit.runner.RunWith\n"
                        + "import spock.lang.Specification \n"
                        + "import spock.lang.Sputnik;\n"
                        + "\n"
                        + "@RunWith(Sputnik)\n"
                        + "class MyTest extends Specification {\n"
                        + "//deleting extends Specification is sufficient to remove all 3 errors,\n"
                        + "//necessary to remove model.SpecMetadata\n"
                        + "\n"
                        + "def aField; //delete line to remove the model.FieldMetadata error.\n"
                        + "\n"
                        + "def noSuchLuck() { expect: //delete line to remove model.FeatureMetadata error. \n"
                        + "  println hello }\n"
                        + "public static void main(String[] argv) { print 'success';}\n"
                        + "}");

        incrementalBuild(projectPath);
        expectingNoProblems();
        expectingCompiledClasses("MyTest");
        executeClass(projectPath, "MyTest", "success", null);
    }

    /**
     * Testing that the transform occurs on an incremental change. The key thing
     * being looked at here is that the incremental change is not directly to a
     * transformed file but to a file referenced from a transformed file.
     */
    @Test
    public void testSpock_GRE605_1() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        env.addJar(projectPath, "lib/junit-4.12.jar");
        env.addJar(projectPath, "lib/spock-core-0.4-groovy-1.7-SNAPSHOT.jar");
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "", "FoobarSpec",
                "import spock.lang.Specification\n" + "\n"
                        + "class FoobarSpec extends Specification {\n" + "	\n"
                        + "	Foobar barbar\n" + "   \n"
                        + "    def example() {\n" + "    	when: \n"
                        + "        def foobar = new Foobar()\n" + "        \n"
                        + "        then:\n" + "        foobar.baz == 42\n"
                        + "   }\n" + "    \n" + "}");

        env.addGroovyClass(root, "", "Foobar", "class Foobar {\n" + "\n"
                + "def baz = 42\n" + "//def quux = 36\n" + "\n" + "}\n");
        incrementalBuild(projectPath);
        expectingNoProblems();
        expectingCompiledClasses("Foobar", "FoobarSpec");

        IPath workspacePath = env.getWorkspaceRootPath();
        File f = new File(workspacePath.append(
                env.getOutputLocation(projectPath)).toOSString(),
                "FoobarSpec.class");
        long filesize = f.length(); // this is 9131 for groovy 1.7.0

        env.addGroovyClass(root, "", "Foobar", "class Foobar {\n" + "\n"
                + "def baz = 42\n" + "def quux = 36\n" + "\n" + "}\n");
        incrementalBuild(projectPath);
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
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        env.addJar(projectPath, "lib/junit-4.12.jar");
        env.addJar(projectPath, "lib/spock-core-0.4-groovy-1.7-SNAPSHOT.jar");
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "", "FoobarSpec",
                "import spock.lang.Specification\n" + "\n"
                        + "class FoobarSpec extends Specification {\n" + "	\n"
                        + "  Foobar foob\n" + "    def example() {\n"
                        + "    	when: \n"
                        + "        def foobar = new Foobar()\n" + "        \n"
                        + "        then:\n" + "        foobar.baz == 42\n"
                        + "   }\n" + "    \n" + "}");

        env.addGroovyClass(root, "", "Foobar", "class Foobar {\n" + "\n"
                + "def baz = 42\n" + "//def quux = 36\n" + "\n" + "}\n");
        incrementalBuild(projectPath);
        expectingNoProblems();
        expectingCompiledClasses("Foobar", "FoobarSpec");

        // IPath workspacePath = env.getWorkspaceRootPath();
        // File f = new
        // File(workspacePath.append(env.getOutputLocation(projectPath)).toOSString(),"FoobarSpec.class");
        // long filesize = f.length(); // this is 9131 for groovy 1.7.0

        env.addGroovyClass(root, "", "Foobar", "class Foobar {\n" + "\n"
                + "def baz = 42\n" + "def quux = 36\n" + "\n" + "}\n");
        incrementalBuild(projectPath);
        expectingNoProblems();
        expectingCompiledClasses("Foobar", "FoobarSpec");

        // long filesizeNow = f.length(); // drops to 7002 if transform did not
        // run
        // assertEquals(filesize,filesizeNow);
    }

    @Test // build .groovy file hello world then run it
    public void testBuildGroovyHelloWorld() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "p1", "Hello", "package p1;\n"
                + "class Hello {\n" + "   static void main(String[] args) {\n"
                + "      print \"Hello Groovy world\"\n" + "   }\n" + "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("p1.Hello");
        expectingNoProblems();
        executeClass(projectPath, "p1.Hello", "Hello Groovy world", null);
    }

    @Test // use funky main method
    public void testBuildGroovyHelloWorld2() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "p1", "Hello", "package p1;\n"
                + "class Hello {\n" + "   static main(args) {\n"
                + "      print \"Hello Groovy world\"\n" + "   }\n" + "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("p1.Hello");
        expectingNoProblems();
        executeClass(projectPath, "p1.Hello", "Hello Groovy world", null);
    }

    @Test
    public void testGenericMethods() throws Exception {
        IPath projectPath = env.addProject("Project", "1.5");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addClass(root, "", "Foo", "public class Foo<T> {\n"
                + "   public void m() {\n" + "      Bar.agent(null);\n"
                + "   }\n" + "}\n");

        env.addGroovyClass(root, "", "Bar", "class Bar {\n"
                + "   public static <PP> void agent(PP state) {\n" + "   }\n"
                + "}\n");

        incrementalBuild(projectPath);
        // expectingCompiledClasses("Foo","Bar");
        expectingNoProblems();
    }

    @Test
    public void testPropertyAccessorLocationChecks() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "p1", "Hello", "package p1;\n"
                + "class Hello {\n" + "  int color;\n"
                + "   static void main(String[] args) {\n"
                + "      print \"Hello Groovy world\"\n" + "   }\n" + "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("p1.Hello");
        expectingNoProblems();
        executeClass(projectPath, "p1.Hello", "Hello Groovy world", null);
        // IJavaProject javaProject = env.getJavaProject(projectPath);
        // IJavaElement pkgFragmentRoot = javaProject.findElement(new
        // Path("p1/Hello.groovy"));
        // System.out.println("A>"+pkgFragmentRoot);
        // IJavaElement cu = find(pkgFragmentRoot,
        // "Hello");cu.getAdapter(adapter)
        // System.out.println(cu);
    }

    @Test
    public void testBuildGroovy2() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "p1", "Hello", "package p1;\n"
                + "interface Hello extends java.util.List {\n" + "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("p1.Hello");
        expectingNoProblems();
    }

    @Test
    public void testLargeProjects_GRE1037() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        int max = AbstractImageBuilder.MAX_AT_ONCE;
        try {
            AbstractImageBuilder.MAX_AT_ONCE = 10;

            for (int i = 1; i < 10; i++) {
                env.addClass(root, "p1", "Hello" + i, "package p1;\n"
                        + "class Hello" + i + " {\n" + "}\n");
            }

            env.addGroovyClass(
                    root,
                    "p1",
                    "Foo",
                    "package p1;\n"
                            + "import p1.*;\n"
                            + "class Foo {\n"
                            + "  public static void main(String []argv) { print '12';}\n"
                            + "  void m() { Bar b = new Bar();}\n" + "}\n");

            env.addGroovyClass(root, "p1", "Bar", "package p1;\n"
                    + "class Bar {\n" + "}\n");

            incrementalBuild(projectPath);
            // see console for all the exceptions...
            // no class for p1.Foo when problem occurs:
            executeClass(projectPath, "p1.Foo", "12", "");
        } finally {
            AbstractImageBuilder.MAX_AT_ONCE = max;
        }
    }

    @Test
    public void testIncrementalCompilationTheBasics() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addClass(root, "pkg", "Hello", "package pkg;\n"
                + "public class Hello {\n"
                + "   public static void main(String[] args) {\n"
                + "      System.out.println(new GHello().run());\n" + "   }\n"
                + "}\n");

        env.addGroovyClass(root, "pkg", "GHello", "package pkg;\n"
                + "public class GHello {\n"
                + "   public int run() { return 12; }\n" + "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("pkg.Hello", "pkg.GHello");
        expectingNoProblems();
        executeClass(projectPath, "pkg.Hello", "12", "");

        // whitespace change to groovy file
        env.addGroovyClass(root, "pkg", "GHello", "package pkg;\n"
                + "public class GHello {\n" + "  \n" + // new blank line
                "   public int run() { return 12; }\n" + "}\n");
        incrementalBuild(projectPath);
        expectingCompiledClasses("pkg.GHello");
        expectingNoProblems();

        // structural change to groovy file - did the java file record its
        // dependency correctly?
        env.addGroovyClass(root, "pkg", "GHello", "package pkg;\n"
                + "public class GHello {\n"
                + "   public String run() { return \"abc\"; }\n" + // return
                                                                    // type now
                                                                    // String
                "}\n");
        incrementalBuild(projectPath);
        expectingCompiledClasses("pkg.GHello", "pkg.Hello");
        expectingNoProblems();

    }

    @Test
    public void testIncrementalCompilation1594() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");


        env.addClass(root, "testpkg", "AllTests", "package testpkg;\n"
                + "public class AllTests {\n"
                + "    TestCaseChannelPersistentStore tccps;\n"+
                "\n"+
                "public static void setupDbConnPool() throws Exception {\n"+
                "}\n"+
                "}\n");

        env.addGroovyClass(root, "testpkg", "TestCaseChannelPersistentStore", "package testpkg\n"
                + "class TestCaseChannelPersistentStore {\n"+
// This will be added in a subsequent incremental build
//				+ "public static void foo() {\n"+
//				  "  def clazz=TestCaseChannelPersistentStore.class;\n"+
//				  "}\n"+
                "\n"+
                "void testRefreshedChannelMap() {\n"+
                "    def x= new Runnable() {public void run() { print('running');}};\n"+
                "	   x.run();\n"+
                "}\n"+
                "public static void main(String[]argv) { new TestCaseChannelPersistentStore().testRefreshedChannelMap();}\n"+
                "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("testpkg.AllTests", "testpkg.TestCaseChannelPersistentStore","testpkg.TestCaseChannelPersistentStore$1");
        expectingNoProblems();
        executeClass(projectPath, "testpkg.TestCaseChannelPersistentStore", "running", "");

        env.addGroovyClass(root, "testpkg", "TestCaseChannelPersistentStore", "package testpkg\n"
                + "class TestCaseChannelPersistentStore {\n"
                + "public static void foo() {\n"+
                "  def clazz=TestCaseChannelPersistentStore.class;\n"+
                "}\n"+
                "\n"+
                "void testRefreshedChannelMap() {\n"
                + "    def x= new Runnable() {public void run() {}};\n"+
                "}\n"+
                "}\n");
        incrementalBuild();
        executeClass(projectPath, "testpkg.TestCaseChannelPersistentStore", "", "");

//		// whitespace change to groovy file
//		env.addGroovyClass(root, "testpkg", "TestCaseChannelPersistentStore", "package testpkg\n"
//				+ "class TestCaseChannelPersistentStore {\n"
//				+ "public static void foo() {\n"+
//				  "  def clazz=TestCaseChannelPersistentStore.class;\n"+
//				  "}\n"+
//				  "\n"+
//				  "void testRefreshedChannelMap() {\n"
//				+ "    def x= new Runnable() {public void run() {}};\n"+
//				  "}\n"+
//				 "}\n");

//		env.addGroovyClass(root, "pkg", "GHello", "package pkg;\n"
//				+ "public class GHello {\n" + "  \n" + // new blank line
//				"   public int run() { return 12; }\n" + "}\n");
        incrementalBuild(projectPath);
//		expectingCompiledClasses("pkg.GHello");
        expectingNoProblems();
    }

    @Test
    public void testIncrementalGenericsAndBinaryTypeBindings_GRE566() throws Exception {
        IPath projectPath = env.addProject("GRE566", "1.5");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addClass(root, "pkg", "Intface", "package pkg;\n"
                + "public interface Intface<E extends Event> {\n"
                + "   void onApplicationEvent(E event);\n" + "}\n");

        env.addClass(root, "pkg", "Event", "package pkg;\n"
                + "public class Event {}\n");

        env.addClass(root, "pkg", "EventImpl", "package pkg;\n"
                + "public class EventImpl extends Event {}\n");

        env.addClass(root, "pkg", "Jaas", "package pkg;\n"
                + "public class Jaas implements Intface<EventImpl> {\n"
                + "  public void onApplicationEvent(EventImpl ei) {}\n" + "}\n");

        env.addGroovyClass(root, "pkg", "GExtender", "package pkg;\n"
                + "class GExtender extends Jaas{\n" + "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("pkg.Event", "pkg.EventImpl", "pkg.Intface",
                "pkg.Jaas", "pkg.GExtender");
        expectingNoProblems();

        env.addGroovyClass(root, "pkg", "GExtender", "package pkg\n"
                + "class GExtender extends Jaas{\n" + "}\n");

        incrementalBuild(projectPath);
        expectingNoProblems();
        expectingCompiledClasses("pkg.GExtender");
    }

    @Test
    public void testIncrementalCompilationTheBasics2_changingJavaDependedUponByGroovy() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addClass(root, "pkg", "Hello", "package pkg;\n"
                + "public class Hello {\n"
                + "  public int run() { return 12; }\n" + "}\n");

        env.addGroovyClass(root, "pkg", "GHello", "package pkg;\n"
                + "public class GHello {\n"
                + "   public static void main(String[] args) {\n"
                + "      System.out.println(new Hello().run());\n" + "   }\n"
                + "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("pkg.Hello", "pkg.GHello");
        expectingNoProblems();
        executeClass(projectPath, "pkg.GHello", "12", "");

        // whitespace change to java file
        env.addClass(root, "pkg", "Hello", "package pkg;\n"
                + "public class Hello {\n" + "  \n" + // new blank line
                "  public int run() { return 12; }\n" + "}\n");
        incrementalBuild(projectPath);
        expectingCompiledClasses("pkg.Hello");
        expectingNoProblems();

        // structural change to groovy file - did the java file record its
        // dependency correctly?
        env.addClass(root, "pkg", "Hello", "package pkg;\n"
                + "public class Hello {\n"
                + "   public String run() { return \"abc\"; }\n" + // return
                                                                    // type now
                                                                    // String
                "}\n");
        incrementalBuild(projectPath);
        expectingCompiledClasses("pkg.GHello", "pkg.Hello");
        expectingNoProblems();
        executeClass(projectPath, "pkg.GHello", "abc", "");

    }

    @Test
    public void testInnerClasses_GRE339() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addClass(root, "", "Outer", "public interface Outer {\n"
                + "  interface Inner { static String VAR=\"value\";}\n" + "}\n");

        env.addGroovyClass(root, "", "script", "print Outer.Inner.VAR\n");

        incrementalBuild(projectPath);
        try { Thread.sleep(1000); } catch (Exception e) {}
        expectingCompiledClasses("Outer", "Outer$Inner", "script");
        expectingNoProblems();
        executeClass(projectPath, "script", "value", "");

        // whitespace change to groovy file
        env.addGroovyClass(root, "", "script", "print Outer.Inner.VAR \n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("script");
        expectingNoProblems();
        executeClass(projectPath, "script", "value", null);

    }

    @Test
    public void testSimpleTaskMarkerInSingleLineComment() throws Exception {
        Hashtable<String, String> options = JavaCore.getOptions();
        Hashtable<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");

        JavaCore.setOptions(newOptions);

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath pathToA = env.addGroovyClass(root, "p", "A",
                "package p; \n" +
                        "class C {\n" + "//todo nothing\n" + // // 24>36 'todo nothing'
                        "\n" + "//tooo two\n" +
                        "}");

        fullBuild(projectPath);

        Problem[] rootProblems = env.getProblemsFor(pathToA);
        for (int i = 0; i < rootProblems.length; i++) {
            System.out.println(i + "  " + rootProblems[i] + "["
                    + rootProblems[i].getMessage() + "]"
                    + rootProblems[i].getEnd());
        }
        // positions should be from the first character of the tag to the character after the last in the text
        expectingSpecificProblemFor(pathToA, new Problem("A", toTask("todo", "nothing"), pathToA, 24, 36, -1, IMarker.SEVERITY_ERROR));

        JavaCore.setOptions(options);
    }

    @Test
    public void testSimpleTaskMarkerInSingleLineCommentEndOfClass() throws Exception {
        Hashtable<String, String> options = JavaCore.getOptions();
        Hashtable<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");

        JavaCore.setOptions(newOptions);

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath pathToA = env.addGroovyClass(root, "p", "A",
                "package p; \n" +
                        "class C {\n" + "//topo nothing\n" + // '/' is 22 'n' is 29 'g' is 35
                        "\n" + "//todo two\n" + // '/' is 38 't' is 45 'o' is 47
                        "}");

        fullBuild(projectPath);

        expectingSpecificProblemFor(pathToA, new Problem("A", toTask("todo", "two"), pathToA, 40, 48, -1, IMarker.SEVERITY_ERROR));

        JavaCore.setOptions(options);
    }

    @Test
    public void testSimpleTaskMarkerInSingleLineCommentEndOfClassCaseInsensitive() throws Exception {
        Hashtable<String, String> options = JavaCore.getOptions();
        Hashtable<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");
        newOptions.put(JavaCore.COMPILER_TASK_CASE_SENSITIVE, JavaCore.DISABLED);

        JavaCore.setOptions(newOptions);

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath pathToA = env.addGroovyClass(root, "p", "A",
                "package p; \n" +
                        "class C {\n" + "//TODO nothing\n" + // '/' is 22 'n' is 29 'g' is 35
                        "\n" + "//topo two\n" + // '/' is 38 't' is 45 'o' is 47
                        "}");

        fullBuild(projectPath);

        Problem[] rootProblems = env.getProblemsFor(pathToA);
        for (int i = 0; i < rootProblems.length; i++) {
            System.out.println(i + "  " + rootProblems[i] + "["
                    + rootProblems[i].getMessage() + "]"
                    + rootProblems[i].getEnd());
        }
        expectingSpecificProblemFor(pathToA, new Problem("A", toTask("todo", "nothing"), pathToA, 24, 36, -1, IMarker.SEVERITY_ERROR));

        JavaCore.setOptions(options);
    }

    @Test
    public void testTaskMarkerInMultiLineCommentButOnOneLine() throws Exception {
        Hashtable<String, String> options = JavaCore.getOptions();
        Hashtable<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");

        JavaCore.setOptions(newOptions);

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath pathToA = env.addGroovyClass(root, "p", "A",
                "package p; \n" +
                        "/*  todo nothing */\n" +
                        "public class A {\n" +
                        "}");

        fullBuild(projectPath);

        expectingSpecificProblemFor(pathToA, new Problem("A", toTask("todo", "nothing"), pathToA, 16, 29, -1, IMarker.SEVERITY_ERROR));

        JavaCore.setOptions(options);
    }

    @Test
    public void testTaskMarkerInMultiLineButNoText() throws Exception {
        Hashtable<String, String> options = JavaCore.getOptions();
        Hashtable<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");

        JavaCore.setOptions(newOptions);

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath pathToA = env.addGroovyClass(root, "p", "A",
                "package p; \n" +
                        "/*  todo\n" +
                        " */\n" +
                        "public class A {\n" +
                        "}");

        fullBuild(projectPath);

        expectingSpecificProblemFor(pathToA, new Problem("A", toTask("todo", ""), pathToA, 16, 20, -1, IMarker.SEVERITY_ERROR));

        JavaCore.setOptions(options);
    }

    @Test
    public void testTaskMarkerInMultiLineOutsideClass() throws Exception {
        Hashtable<String, String> options = JavaCore.getOptions();
        Hashtable<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");

        JavaCore.setOptions(newOptions);

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath pathToA = env.addGroovyClass(root, "p", "A",
                "package p; \n" +
                        "/*  \n" + // 12
                        " * todo nothing *\n" + // 17
                        " */\n" +
                        "public class A {\n" +
                        "}");

        fullBuild(projectPath);

        expectingSpecificProblemFor(pathToA, new Problem("A", toTask("todo", "nothing *"), pathToA, 20, 34, -1, IMarker.SEVERITY_ERROR));

        JavaCore.setOptions(options);
    }

    @Test // task marker inside a multi line comment inside a class
    public void testTaskMarkerInMultiLineInsideClass() throws Exception {
        Hashtable<String, String> options = JavaCore.getOptions();
        Hashtable<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");

        JavaCore.setOptions(newOptions);

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath pathToA = env.addGroovyClass(root, "p", "A",
                "package p; \n" + // -- \n is 11
                        "public class A {\n" + // -- \n is 28
                        "   /*  \n" + // -- \n is 36
                        " * todo nothing *\n" +
                        " */\n" +
                        "}");

        fullBuild(projectPath);

        expectingSpecificProblemFor(pathToA, new Problem("A", toTask("todo", "nothing *"), pathToA, 40, 54, -1, IMarker.SEVERITY_ERROR));

        JavaCore.setOptions(options);
    }

    @Test // tag priority
    public void testTaskMarkerMixedPriorities() throws Exception {
        Hashtable<String, String> options = JavaCore.getOptions();
        Hashtable<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
        newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW");

        JavaCore.setOptions(newOptions);

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath pathToA = env.addGroovyClass(root, "p", "A",
                "package p; \n" +
                        "//TODO normal\n" +
                        "public class A {\n" +
                        "	public void foo() {\n" +
                        "		//FIXME high\n" +
                        "	}\n" +
                        "	public void foo2() {\n" +
                        "		//XXX low\n" +
                        "	}\n" +
                        "}");

        fullBuild(projectPath);
        IMarker[] markers = env.getTaskMarkersFor(pathToA);
        assertEquals("Wrong size", 3, markers.length);
        try {
            IMarker marker = markers[0];
            Object priority = marker.getAttribute(IMarker.PRIORITY);
            String message = (String) marker.getAttribute(IMarker.MESSAGE);
            assertTrue("Wrong message", message.startsWith("TODO"));
            assertNotNull("No task priority", priority);
            assertEquals("Wrong priority",
                    new Integer(IMarker.PRIORITY_NORMAL), priority);

            marker = markers[1];
            priority = marker.getAttribute(IMarker.PRIORITY);
            message = (String) marker.getAttribute(IMarker.MESSAGE);
            assertTrue("Wrong message", message.startsWith("FIXME"));
            assertNotNull("No task priority", priority);
            assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_HIGH),
                    priority);

            marker = markers[2];
            priority = marker.getAttribute(IMarker.PRIORITY);
            message = (String) marker.getAttribute(IMarker.MESSAGE);
            assertTrue("Wrong message", message.startsWith("XXX"));
            assertNotNull("No task priority", priority);
            assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_LOW),
                    priority);
        } catch (CoreException e) {
            e.printStackTrace();
            fail("Unexpected failure: "+e.toString());
        }
        JavaCore.setOptions(options);
    }

    @Test
    public void testTaskMarkerMultipleOnOneLineInSLComment() throws Exception {
        Hashtable<String, String> options = JavaCore.getOptions();
        Hashtable<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
        newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW");

        JavaCore.setOptions(newOptions);

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath pathToA = env.addGroovyClass(root, "p", "A",
                "package p; \n" +
                        "// TODO FIXME need to review the loop TODO should be done\n"
                        +
                        "public class A {\n" +
                        "}");

        fullBuild(projectPath);
        IMarker[] markers = env.getTaskMarkersFor(pathToA);
        assertEquals("Wrong size", 3, markers.length);
        try {
            IMarker marker = markers[2];
            Object priority = marker.getAttribute(IMarker.PRIORITY);
            String message = (String) marker.getAttribute(IMarker.MESSAGE);
            assertEquals("Wrong message", toTask("TODO", "should be done"),
                    message);
            assertNotNull("No task priority", priority);
            assertEquals("Wrong priority",
                    new Integer(IMarker.PRIORITY_NORMAL), priority);

            marker = markers[1];
            priority = marker.getAttribute(IMarker.PRIORITY);
            message = (String) marker.getAttribute(IMarker.MESSAGE);
            assertEquals("Wrong message",
                    toTask("FIXME", "need to review the loop"), message);
            assertNotNull("No task priority", priority);
            assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_HIGH),
                    priority);

            marker = markers[0];
            priority = marker.getAttribute(IMarker.PRIORITY);
            message = (String) marker.getAttribute(IMarker.MESSAGE);
            assertEquals("Wrong message",
                    toTask("TODO", "need to review the loop"), message);
            assertNotNull("No task priority", priority);
            assertEquals("Wrong priority",
                    new Integer(IMarker.PRIORITY_NORMAL), priority);
        } catch (CoreException e) {
            e.printStackTrace();
            fail("Unexpected failure: "+e.toString());
        }
        JavaCore.setOptions(options);
    }

    @Test
    public void testTaskMarkerMultipleOnOneLineInMLComment() throws Exception {
        Hashtable<String, String> options = JavaCore.getOptions();
        Hashtable<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
        newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW");

        JavaCore.setOptions(newOptions);

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath pathToA = env.addGroovyClass(root, "p", "A",
                "package p; \n" +
                        "/* TODO FIXME need to review the loop TODO should be done */\n"
                        +
                        "public class A {\n" +
                        "}");

        fullBuild(projectPath);
        IMarker[] markers = env.getTaskMarkersFor(pathToA);
        assertEquals("Wrong size", 3, markers.length);
        try {
            IMarker marker = markers[2];
            Object priority = marker.getAttribute(IMarker.PRIORITY);
            String message = (String) marker.getAttribute(IMarker.MESSAGE);
            assertEquals("Wrong message", toTask("TODO", "should be done"),
                    message);
            assertNotNull("No task priority", priority);
            assertEquals("Wrong priority",
                    new Integer(IMarker.PRIORITY_NORMAL), priority);

            marker = markers[1];
            priority = marker.getAttribute(IMarker.PRIORITY);
            message = (String) marker.getAttribute(IMarker.MESSAGE);
            assertEquals("Wrong message",
                    toTask("FIXME", "need to review the loop"), message);
            assertNotNull("No task priority", priority);
            assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_HIGH),
                    priority);

            marker = markers[0];
            priority = marker.getAttribute(IMarker.PRIORITY);
            message = (String) marker.getAttribute(IMarker.MESSAGE);
            assertEquals("Wrong message",
                    toTask("TODO", "need to review the loop"), message);
            assertNotNull("No task priority", priority);
            assertEquals("Wrong priority",
                    new Integer(IMarker.PRIORITY_NORMAL), priority);
        } catch (CoreException e) {
            e.printStackTrace();
            fail("Unexpected failure: "+e.toString());
        }
        JavaCore.setOptions(options);
    }

    @Test // two on one line
    public void testTaskMarkerSharedDescription() throws Exception {
        Hashtable<String, String> options = JavaCore.getOptions();
        Hashtable<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
        newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW");

        JavaCore.setOptions(newOptions);

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath pathToA = env.addGroovyClass(root, "p", "A",
                "package p; \n" +
                        "// TODO TODO need to review the loop\n" +
                        "public class A {\n" +
                        "}");

        fullBuild(projectPath);
        IMarker[] markers = env.getTaskMarkersFor(pathToA);
        assertEquals("Wrong size", 2, markers.length);
        try {
            IMarker marker = markers[1];
            Object priority = marker.getAttribute(IMarker.PRIORITY);
            String message = (String) marker.getAttribute(IMarker.MESSAGE);
            assertEquals("Wrong message",
                    toTask("TODO", "need to review the loop"), message);
            assertNotNull("No task priority", priority);
            assertEquals("Wrong priority",
                    new Integer(IMarker.PRIORITY_NORMAL), priority);

            marker = markers[0];
            priority = marker.getAttribute(IMarker.PRIORITY);
            message = (String) marker.getAttribute(IMarker.MESSAGE);
            assertEquals("Wrong message",
                    toTask("TODO", "need to review the loop"), message);
            assertNotNull("No task priority", priority);
            assertEquals("Wrong priority",
                    new Integer(IMarker.PRIORITY_NORMAL), priority);
        } catch (CoreException e) {
            e.printStackTrace();
            fail("Unexpected failure: "+e.toString());
        }
        JavaCore.setOptions(options);
    }

    @Test
    public void testCopyGroovyResourceNonGroovyProject_GRECLIPSE653() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.removeGroovyNature("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        IPath output = env.setOutputFolder(projectPath, "bin");

        IPath pathToA = env.addGroovyClass(root, "p", "A",
                "package p; \n" +
                        "class C { }");

        fullBuild(projectPath);

        // groovy file should be copied as-is
        IPath pathToABin = output.append(pathToA.removeFirstSegments(pathToA
                .segmentCount() - 2));
        assertTrue("File should exist " + pathToABin.toPortableString(), env
                .getWorkspace().getRoot().getFile(pathToABin).exists());

        // now check that works for incremental
        IPath pathToB = env.addGroovyClass(root, "p", "B",
                "package p; \n" +
                        "class D { }");
        incrementalBuild(projectPath);

        // groovy file should be copied as-is
        IPath pathToBBin = output.append(pathToB.removeFirstSegments(pathToB
                .segmentCount() - 2));
        assertTrue("File should exist " + pathToBBin.toPortableString(), env
                .getWorkspace().getRoot().getFile(pathToBBin).exists());

        // now check that bin file is deleted when deleted in source
        IFile bFile = env.getWorkspace().getRoot().getFile(pathToB);
        bFile.delete(true, null);
        incrementalBuild(projectPath);
        assertFalse("File should not exist " + pathToBBin.toPortableString(), env.getWorkspace().getRoot().getFile(pathToBBin).exists());
    }

    @Test
    public void testCopyResourceNonGroovyProject_GRECLIPSE653() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.removeGroovyNature("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        IPath output = env.setOutputFolder(projectPath, "bin");

        IPath pathToA = env.addFile(root, "A.txt", "A");

        fullBuild(projectPath);

        // file should be copied as-is
        IPath pathToABin = output.append(pathToA.removeFirstSegments(pathToA
                .segmentCount() - 1));
        assertTrue("File should exist " + pathToABin.toPortableString(), env
                .getWorkspace().getRoot().getFile(pathToABin).exists());

        // now check that works for incremental
        IPath pathToB = env.addFile(root, "B.txt", "B");
        incrementalBuild(projectPath);

        // groovy file should be copied as-is
        IPath pathToBBin = output.append(pathToB.removeFirstSegments(pathToB
                .segmentCount() - 1));
        assertTrue("File should exist " + pathToBBin.toPortableString(), env
                .getWorkspace().getRoot().getFile(pathToBBin).exists());

        // now check that bin file is deleted when deleted in source
        IFile bFile = env.getWorkspace().getRoot().getFile(pathToB);
        bFile.delete(true, null);
        incrementalBuild(projectPath);
        assertFalse("File should not exist " + pathToBBin.toPortableString(),
                env.getWorkspace().getRoot().getFile(pathToBBin).exists());
    }

    @Test
    public void testCopyResourceGroovyProject_GRECLIPSE653() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        env.addGroovyNature("Project");

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        IPath output = env.setOutputFolder(projectPath, "bin");

        IPath pathToA = env.addFile(root, "A.txt", "A");

        fullBuild(projectPath);

        // groovy file should be copied as-is
        IPath pathToABin = output.append(pathToA.removeFirstSegments(pathToA
                .segmentCount() - 1));
        assertTrue("File should exist " + pathToABin.toPortableString(), env
                .getWorkspace().getRoot().getFile(pathToABin).exists());

        // now check that works for incremental
        IPath pathToB = env.addFile(root, "B.txt", "B");
        incrementalBuild(projectPath);

        // groovy file should be copied as-is
        IPath pathToBBin = output.append(pathToB.removeFirstSegments(pathToB
                .segmentCount() - 1));
        assertTrue("File should exist " + pathToBBin.toPortableString(), env
                .getWorkspace().getRoot().getFile(pathToBBin).exists());

        // now check that bin file is deleted when deleted in source
        IFile bFile = env.getWorkspace().getRoot().getFile(pathToB);
        bFile.delete(true, null);
        incrementalBuild(projectPath);
        assertFalse("File should not exist " + pathToBBin.toPortableString(),
                env.getWorkspace().getRoot().getFile(pathToBBin).exists());
    }

    @Test
    public void testNoDoubleResolve() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        env.addGroovyNature("Project");

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(projectPath.append("src"), "p", "Groov",
                "package p\n");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) env
                .getJavaProject("Project").findType("p.Groov")
                .getCompilationUnit();
        unit.becomeWorkingCopy(null);
        ModuleNodeInfo moduleInfo = unit.getModuleInfo(true);
        JDTResolver resolver = moduleInfo.resolver;
        assertNotNull(resolver);
        resolver.currentClass = moduleInfo.module.getScriptClassDummy();
        ClassNode url = resolver.resolve("java.net.URL");
        assertNotNull("Should have found the java.net.URL ClassNode", url);
        assertEquals("Wrong classnode found", "java.net.URL", url.getName());
    }

    @Test // GRECLIPSE-1170
    public void testFieldInitializerFromOtherFile() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        env.addGroovyNature("Project");
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");
        env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");
        env.addGroovyClass(projectPath.append("src"), "p", "Other",
                "package p\nclass Other {\ndef x = 9 }");
        env.addGroovyClass(projectPath.append("src"), "p", "Target",
                "package p\nnew Other()");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) env
                .getJavaProject("Project").findType("p.Target")
                .getCompilationUnit();

        // now find the class reference
        ClassNode type = ((ConstructorCallExpression) ((ReturnStatement) unit
                .getModuleNode().getStatementBlock().getStatements().get(0))
                .getExpression()).getType();

        // now check that the field initializer exists
        Expression initialExpression = type.getField("x")
                .getInitialExpression();
        assertNotNull(initialExpression);
        assertEquals("Should have been an int",
                VariableScope.INTEGER_CLASS_NODE,
                ClassHelper.getWrapper(initialExpression.getType()));
        assertEquals("Should have been the number 9", "9",
                initialExpression.getText());

        // now check to ensure that there are no duplicate fields or properties
        int declCount = 0;
        for (FieldNode field : type.getFields()) {
            if (field.getName().equals("x"))
                declCount++;
        }
        assertEquals("Should have found 'x' field exactly one time", 1,
                declCount);

        declCount = 0;
        for (PropertyNode prop : type.getProperties()) {
            if (prop.getName().equals("x"))
                declCount++;
        }
        assertEquals("Should have found 'x' property exactly one time", 1,
                declCount);

    }

    @Test // GRECLIPSE-1727
    public void testTraitBasics() throws Exception {
        assumeTrue(isAtLeastGroovy(23));

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "p", "Named",
                "trait Named {\n" +
                "    String name() { 'name' }" +
                "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("Named", "Named$Trait$Helper");
        expectingNoProblems();
    }

    @Test
    public void testTraitIncremental() throws Exception {
        assumeTrue(isAtLeastGroovy(23));

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "p", "Named",
                "package p\n" +
                "trait Named {\n" +
                "    String name() { 'name' }\n" +
                "}\n");

        env.addGroovyClass(root, "q", "NamedClass",
                "package q;\n" +
                "import p.Named;\n" +
                "public class NamedClass implements Named {}\n");

        env.addGroovyClass(root, "", "Runner",
                "import p.Named\n" +
                "import q.NamedClass\n" +
                "Named named = new NamedClass()\n" +
                "print named.name()\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("p.Named", "p.Named$Trait$Helper", "q.NamedClass", "Runner");
        expectingNoProblems();
        executeClass(projectPath, "Runner", "name", "");

        // modify the body of the trait
        env.addGroovyClass(root, "p", "Named",
                "package p\n" +
                "trait Named {\n" +
                "    String name\n" +
                "    String name() { \"$name\" }\n" +
                "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("p.Named", "p.Named$Trait$Helper", "p.Named$Trait$FieldHelper", "q.NamedClass", "Runner");
        expectingNoProblems();
        executeClass(projectPath, "Runner", "null", "");

        env.addGroovyClass(root, "", "Runner",
                "import p.Named\n" +
                "import q.NamedClass\n" +
                "Named named = new NamedClass(name: 'name')\n" +
                "print named.name()\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("Runner");
        expectingNoProblems();
        executeClass(projectPath, "Runner", "name", "");

        env.addGroovyClass(root, "q", "NamedClass",
                "package q;\n" +
                "import p.Named;\n" +
                "public class NamedClass implements Named {\n" +
                "    String name() { \"Hello, ${name}!\" }\n" +
                "}\n");
        incrementalBuild(projectPath);
        expectingCompiledClasses("q.NamedClass", "Runner");
        expectingNoProblems();
        executeClass(projectPath, "Runner", "Hello, name!", "");
    }

    @Test
    public void testTraitBinary() throws Exception {
        assumeTrue(isAtLeastGroovy(23));

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "p", "Named",
                "package p\n" +
                "trait Named {\n" +
                "    String name() { 'name' }\n" +
                "}\n");

        env.addGroovyClass(root, "q", "DefaultNamed",
                "package q;\n" +
                "public class DefaultNamed {\n" +
                "    public String name() { 'name' }\n" +
                "}\n");

        env.addGroovyClass(root, "r", "NamedClass",
                "package r;\n" +
                "import p.Named\n" +
                "import q.DefaultNamed\n" +
                "public class NamedClass extends DefaultNamed implements Named {}\n");

        env.addGroovyClass(root, "", "Runner",
                "import r.NamedClass\n" +
                "NamedClass named = new NamedClass()\n" +
                "print named.name()\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("p.Named", "p.Named$Trait$Helper", "q.DefaultNamed", "r.NamedClass", "Runner");
        expectingNoProblems();
        executeClass(projectPath, "Runner", "name", "");

        env.addGroovyClass(root, "r", "NamedClass",
                "package r;\n" +
                "import p.Named\n" +
                "import q.DefaultNamed\n" +
                "public class NamedClass extends DefaultNamed implements Named {\n" +
                "    String name() { 'new name' }\n" +
                "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("r.NamedClass", "Runner");
        expectingNoProblems();
        executeClass(projectPath, "Runner", "new name", "");

        env.addGroovyClass(root, "r", "NamedClass",
                "package r;\n" +
                "import p.Named\n" +
                "import q.DefaultNamed\n" +
                "public class NamedClass extends DefaultNamed implements Named {}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("r.NamedClass", "Runner");
        expectingNoProblems();
        executeClass(projectPath, "Runner", "name", "");

        env.addGroovyClass(root, "p", "Named",
                "package p\n" +
                "trait Named {\n" +
                "    abstract String name()\n" +
                "}\n");

        env.addGroovyClass(root, "r", "NamedClass",
                "package r;\n" +
                "import p.Named\n" +
                "import q.DefaultNamed\n" +
                "public class NamedClass extends DefaultNamed implements Named {\n" +
                "    String name() { 'new name' }\n" +
                "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("p.Named", "p.Named$Trait$Helper", "r.NamedClass", "Runner");
        expectingNoProblems();
        executeClass(projectPath, "Runner", "new name", "");
    }

    @Test
    public void testTraitGRE1776() throws Exception {
        assumeTrue(isAtLeastGroovy(23));

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "p", "MyTrait",
                "package p\n" +
                "trait MyTrait {\n" +
                "}\n");

        env.addGroovyClass(root, "q", "MyClass",
                "package q\n" +
                "import p.MyTrait\n" +
                "public class MyClass implements MyTrait {}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("p.MyTrait", "p.MyTrait$Trait$Helper", "q.MyClass");
        expectingNoProblems();

        // modify the body of the trait
        env.addGroovyClass(root, "p", "MyTrait",
                "package p\n" +
                "trait MyTrait {\n" +
                "    def m() { 'm' }\n" +
                "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("p.MyTrait", "p.MyTrait$Trait$Helper", "q.MyClass");
        expectingNoProblems();

        // modify again the body of the trait
        env.addGroovyClass(root, "p", "MyTrait",
                "package p\n" +
                "trait MyTrait {\n" +
                "    def k() { 'm' }\n" +
                "}\n");

        incrementalBuild(projectPath);
        expectingCompiledClasses("p.MyTrait", "p.MyTrait$Trait$Helper", "q.MyClass");
        expectingNoProblems();
    }

    @Test
    public void testGRE1773() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath class1 = env.addGroovyClass(root, "test", "Class1",
                "package test\n" +
                "abstract class Class1 {\n" +
                "    abstract void m1()\n" +
                "    void m2() {}\n" +
                "    static Class1 create(String type) {\n" +
                "        switch (type) {\n" +
                "            case 'Class2':\n" +
                "                return new Class2()\n" +
                "                break\n" +
                "            case 'Class3':\n" +
                "                return new Class3()\n" +
                "            default:\n" +
                "                assert false : \"Unexpected type ${type}\"\n" +
                "        }\n" +
                "    }\n" +
                "}");

        IPath class2 = env.addGroovyClass(root, "test", "Class2",
                "package test\n" +
                "class Class2 extends Class1 {\n" +
                "    @Override\n" +
                "    public void m1() {}\n" +
                "}");

        IPath class3 = env.addGroovyClass(root, "test", "Class3",
                "package test\n" +
                "class Class3 extends Class1 {\n" +
                "    @Override\n" +
                "    public void m1() {}\n" +
                "}");

        incrementalBuild(projectPath);
        expectingCompiledClasses("test.Class1", "test.Class2", "test.Class3");
        expectingNoProblems();

        // modify the body of the abstract class to break build
        class1 = env.addGroovyClass(root, "test", "Class1",
                "package test\n" +
                "abstract class Class1 {\n" +
                "    abstract void ()\n" +
                "    void m2() {}\n" +
                "    static Class1 create(String type) {\n" +
                "        switch (type) {\n" +
                "            case 'Class2':\n" +
                "                return new Class2()\n" +
                "                break\n" +
                "            case 'Class3':\n" +
                "                return new Class3()\n" +
                "            default:\n" +
                "                assert false : \"Unexpected type ${type}\"\n" +
                "        }\n" +
                "    }\n" +
                "}");

        incrementalBuild(projectPath);
        expectingProblemsFor(class1, Arrays.asList("Problem : Groovy:unexpected token: abstract @ line 3, column 5. [ resource : </Project/src/test/Class1.groovy> range : <41,42> category : <60> severity : <2>]"));
        if (GroovyUtils.isAtLeastGroovy(20)) { // Groovy 1.8 has no @Override checking
        expectingProblemsFor(class2, Arrays.asList("Problem : Groovy:Method \'m1\' from class \'test.Class2\' does not override method from its superclass or interfaces but is annotated with @Override. [ resource : </Project/src/test/Class2.groovy> range : <48,56> category : <60> severity : <2>]"));
        expectingProblemsFor(class3, Arrays.asList("Problem : Groovy:Method \'m1\' from class \'test.Class3\' does not override method from its superclass or interfaces but is annotated with @Override. [ resource : </Project/src/test/Class3.groovy> range : <48,56> category : <60> severity : <2>]"));
        }

        // modify the body of the abstract class to fix build
        env.addGroovyClass(root, "test", "Class1",
                "package test\n" +
                "abstract class Class1 {\n" +
                "    abstract void m1()\n" +
                "    void m2() {}\n" +
                "    static Class1 create(String type) {\n" +
                "        switch (type) {\n" +
                "            case 'Class2':\n" +
                "                return new Class2()\n" +
                "                break\n" +
                "            case 'Class3':\n" +
                "                return new Class3()\n" +
                "            default:\n" +
                "                assert false : \"Unexpected type ${type}\"\n" +
                "        }\n" +
                "    }\n" +
                "}");

        incrementalBuild(projectPath);
        expectingCompiledClasses("test.Class1", "test.Class2", "test.Class3");
        expectingNoProblems();
    }

    /*
     * Ensures that a task tag is not user editable
     * (regression test for bug 123721 two types of 'remove' for TODO task tags)
     */
    @Test
    public void testTags3() throws Exception {
        Hashtable<String, String> options = JavaCore.getOptions();

        try {
            Hashtable<String, String> newOptions = JavaCore.getOptions();
            newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
            newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW");

            JavaCore.setOptions(newOptions);

            IPath projectPath = env.addProject("Project");
            env.addExternalJars(projectPath, Util.getJavaClassLibs());

            // remove old package fragment root so that names don't collide
            env.removePackageFragmentRoot(projectPath, "");

            IPath root = env.addPackageFragmentRoot(projectPath, "src");
            env.setOutputFolder(projectPath, "bin");

            IPath pathToA = env.addClass(root, "p", "A",
                "package p; \n"+
                    "// TODO need to review\n" +
                    "public class A {\n" +
                "}");

            fullBuild(projectPath);
            IMarker[] markers = env.getTaskMarkersFor(pathToA);
            assertEquals("Marker should not be editable", Boolean.FALSE,
                markers[0].getAttribute(IMarker.USER_EDITABLE));
        } finally {
            JavaCore.setOptions(options);
        }
    }

    /*
     * http://bugs.eclipse.org/bugs/show_bug.cgi?id=92821
     */
    @Test
    public void testUnusedImport() throws Exception {
        Hashtable<String, String> options = JavaCore.getOptions();
        Hashtable<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.WARNING);

        JavaCore.setOptions(newOptions);

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addClass(root, "util", "MyException",
            "package util;\n" +
                "public class MyException extends Exception {\n" +
                "	private static final long serialVersionUID = 1L;\n" +
                "}"
            );

        env.addClass(root, "p", "Test",
            "package p;\n" +
                "import util.MyException;\n" +
                "public class Test {\n" +
                "	/**\n" +
                "	 * @throws MyException\n" +
                "	 */\n" +
                "	public void bar() {\n" +
                "	}\n" +
                "}"
            );

        fullBuild(projectPath);
        expectingNoProblems();

        JavaCore.setOptions(options);
    }

    /*
     * http://bugs.eclipse.org/bugs/show_bug.cgi?id=98667
     */
    @Test
    public void test98667() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addClass(root, "p1", "Aaa$Bbb$Ccc",
            "package p1;\n" +
                "\n" +
                "public class Aaa$Bbb$Ccc {\n" +
                "}"
            );

        fullBuild(projectPath);
        expectingNoProblems();
    }

    /**
     * @bug 164707: ArrayIndexOutOfBoundsException in JavaModelManager if source level == 6.0
     * @test Ensure that AIIOB does not longer happen with invalid source level string
     * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=164707"
     */
    @Test
    public void testBug164707() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_SOURCE, "invalid");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        fullBuild(projectPath);
        expectingNoProblems();
    }

    /**
     * @bug 75471: [prefs] no re-compile when loading settings
     * @test Ensure that changing project preferences is well taking into account while rebuilding project
     * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=75471"
     */
    @Test @Ignore
    public void testUpdateProjectPreferences() throws Exception {

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addClass(root, "util", "MyException",
            "package util;\n" +
                "public class MyException extends Exception {\n" +
                "	private static final long serialVersionUID = 1L;\n" +
                "}"
            );

        IPath cuPath = env.addClass(root, "p", "Test",
            "package p;\n" +
                "import util.MyException;\n" +
                "public class Test {\n" +
                "}"
            );

        fullBuild(projectPath);
        expectingSpecificProblemFor(
            projectPath,
            new Problem("", "The import util.MyException is never used", cuPath, 18, 34, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING));

        env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.IGNORE);
        incrementalBuild(projectPath);
        expectingNoProblems();
    }

    @Test @Ignore
    public void testUpdateWkspPreferences() throws Exception {

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addClass(root, "util", "MyException",
            "package util;\n" +
                "public class MyException extends Exception {\n" +
                "	private static final long serialVersionUID = 1L;\n" +
                "}"
            );

        IPath cuPath = env.addClass(root, "p", "Test",
            "package p;\n" +
                "import util.MyException;\n" +
                "public class Test {\n" +
                "}"
            );

        fullBuild();
        expectingSpecificProblemFor(
            projectPath,
            new Problem("", "The import util.MyException is never used", cuPath, 18, 34, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING));

        // Save preference
        JavaModelManager manager = JavaModelManager.getJavaModelManager();
        String unusedImport = manager.getInstancePreferences().get(JavaCore.COMPILER_PB_UNUSED_IMPORT, null);
        try {
            // Modify preference
            manager.getInstancePreferences().put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.IGNORE);
            incrementalBuild();
            expectingNoProblems();
        }
        finally {
            if (unusedImport == null) {
                manager.getInstancePreferences().remove(JavaCore.COMPILER_PB_UNUSED_IMPORT);
            } else {
                manager.getInstancePreferences().put(JavaCore.COMPILER_PB_UNUSED_IMPORT, unusedImport);
            }
        }
    }

    @Test
    public void testTags4() throws Exception {
        Hashtable<String, String> options = JavaCore.getOptions();
        Hashtable<String, String> newOptions = JavaCore.getOptions();
        newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO!,TODO,TODO?");
        newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "HIGH,NORMAL,LOW");

        JavaCore.setOptions(newOptions);

        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        IPath pathToA = env.addClass(root, "p", "A",
            "package p; \n"+
                "// TODO! TODO? need to review the loop\n" +
                "public class A {\n" +
            "}");

        fullBuild(projectPath);
        IMarker[] markers = env.getTaskMarkersFor(pathToA);
        assertEquals("Wrong size", 2, markers.length);

        try {
            IMarker marker = markers[1];
            Object priority = marker.getAttribute(IMarker.PRIORITY);
            String message = (String) marker.getAttribute(IMarker.MESSAGE);
            assertEquals("Wrong message", "TODO? need to review the loop", message);
            assertNotNull("No task priority", priority);
            assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_LOW),
                priority);

            marker = markers[0];
            priority = marker.getAttribute(IMarker.PRIORITY);
            message = (String) marker.getAttribute(IMarker.MESSAGE);
            assertEquals("Wrong message", "TODO! need to review the loop", message);
            assertNotNull("No task priority", priority);
            assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_HIGH),
                priority);
        } catch (CoreException e) {
            assertTrue(false);
        }
        JavaCore.setOptions(options);
    }

    @Test @Ignore // When a groovy file name clashes with an existing type
    public void testBuildClash() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");

        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");

        env.addGroovyClass(root, "", "Stack",
            "class StackTester {\n"+
                "   def o = new Stack();\n"+
                "   public static void main(String[] args) {\n"+
                "      System.out.println('>>'+new StackTester().o.getClass());\n"+
                "      System.out.println(\"Hello world\");\n"+
                "   }\n"+
                "}\n"
            );

        incrementalBuild(projectPath);
        expectingCompiledClasses("StackTester");
        expectingNoProblems();
        executeClass(projectPath, "StackTester", ">>class java.util.Stack\r\n" + "Hello world\r\n", "");

        env.addGroovyClass(root, "", "Stack",
            "class StackTester {\n"+
                "   def o = new Stack();\n"+
                "   public static void main(String[] args) {\n"+
                "      System.out.println('>>'+new StackTester().o.getClass());\n"+
                "      System.out.println(\"Hello world\");\n"+
                "   }\n"+
                "}\n"
            );

        incrementalBuild(projectPath);
        expectingCompiledClasses("StackTester");
        expectingNoProblems();
        executeClass(projectPath, "StackTester", ">>class java.util.Stack\r\n" + "Hello world\r\n", "");
    }
}
