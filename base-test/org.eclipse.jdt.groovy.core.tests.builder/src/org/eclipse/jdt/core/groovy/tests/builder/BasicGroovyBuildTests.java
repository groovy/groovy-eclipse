/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.groovy.tests.builder;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.Test;

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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.CompilerUtils;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.core.builder.AbstractImageBuilder;
import org.osgi.framework.Version;

/**
 * Basic tests for the builder - compiling and running some very simple java and
 * groovy code
 */
public class BasicGroovyBuildTests extends GroovierBuilderTests {

	public BasicGroovyBuildTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(BasicGroovyBuildTests.class);
	} 
	
	/**
	 * Testing that the classpath computation works for multi dependent
	 * projects. This classpath will be used for the ast transform loader.
	 */
	public void testMultiProjectDependenciesAndAstTransformClasspath()
			throws JavaModelException {

		// Construct ProjectA
		IPath projectAPath = env.addProject("ProjectA"); //$NON-NLS-1$
		env.addExternalJars(projectAPath, Util.getJavaClassLibs());
		fullBuild(projectAPath);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectAPath, ""); //$NON-NLS-1$
		IPath rootA = env.addPackageFragmentRoot(projectAPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectAPath, "bin"); //$NON-NLS-1$

		env.addClass(rootA, "p1", "Hello", "package p1;\n"
				+ "public class Hello {\n"
				+ "   public static void main(String[] args) {\n"
				+ "      System.out.println(\"Hello world\");\n" + "   }\n"
				+ "}\n");

		// Construct ProjectB
		IPath projectBPath = env.addProject("ProjectB"); //$NON-NLS-1$
		env.addExternalJars(projectBPath, Util.getJavaClassLibs());
		fullBuild(projectBPath);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectBPath, ""); //$NON-NLS-1$
		IPath rootB = env.addPackageFragmentRoot(projectBPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectBPath, "bin"); //$NON-NLS-1$

		env.addClass(rootB, "p1", "Hello", "package p1;\n"
				+ "public class Hello {\n"
				+ "   public static void main(String[] args) {\n"
				+ "      System.out.println(\"Hello world\");\n" + "   }\n"
				+ "}\n");

		env.addRequiredProject(projectBPath, projectAPath, new IPath[] {}/*
																		 * include
																		 * all
																		 */,
				new IPath[] {}/* exclude none */, true);

		// Construct ProjectC
		IPath projectCPath = env.addProject("ProjectC"); //$NON-NLS-1$
		env.addExternalJars(projectCPath, Util.getJavaClassLibs());
		fullBuild(projectCPath);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectCPath, ""); //$NON-NLS-1$
		IPath rootC = env.addPackageFragmentRoot(projectCPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectCPath, "bin"); //$NON-NLS-1$

		env.addClass(rootC, "p1", "Hello", "package p1;\n"
				+ "public class Hello {\n"
				+ "   public static void main(String[] args) {\n"
				+ "      System.out.println(\"Hello world\");\n" + "   }\n"
				+ "}\n");
		env.addRequiredProject(projectCPath, projectBPath, new IPath[] {}/*
																		 * include
																		 * all
																		 */,
				new IPath[] {}/* exclude none */, true);

		// Construct ProjectD
		IPath projectDPath = env.addProject("ProjectD"); //$NON-NLS-1$
		env.addExternalJars(projectDPath, Util.getJavaClassLibs());
		fullBuild(projectDPath);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectDPath, ""); //$NON-NLS-1$
		IPath rootD = env.addPackageFragmentRoot(projectDPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectDPath, "bin"); //$NON-NLS-1$

		env.addClass(rootD, "p1", "Hello", "package p1;\n"
				+ "public class Hello {\n"
				+ "   public static void main(String[] args) {\n"
				+ "      System.out.println(\"Hello world\");\n" + "   }\n"
				+ "}\n");
		env.addRequiredProject(projectDPath, projectCPath, new IPath[] {}/*
																		 * include
																		 * all
																		 */,
				new IPath[] {}/* exclude none */, true);

		// incrementalBuild(projectAPath);
		// expectingCompiledClassesV("p1.Hello");
		// expectingNoProblems();
		// executeClass(projectAPath, "p1.Hello", "Hello world", "");

		incrementalBuild(projectDPath);
		expectingCompiledClassesV("p1.Hello");
		expectingNoProblems();
//		executeClass(projectDPath, "p1.Hello", "Hello world", "");

		String classpathForProjectD = CompilerUtils.calculateClasspath(env
				.getJavaProject(projectDPath));

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

	// build hello world and run it
	public void testBuildJavaHelloWorld() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Hello", "package p1;\n"
				+ "public class Hello {\n"
				+ "   public static void main(String[] args) {\n"
				+ "      System.out.println(\"Hello world\");\n" + "   }\n"
				+ "}\n");

		incrementalBuild(projectPath);
		expectingCompiledClassesV("p1.Hello");
		expectingNoProblems();
		executeClass(projectPath, "p1.Hello", "Hello world", "");

	}
	
	public void testGenericsDefaultParams_1717() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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
		expectingCompiledClassesV("p1.Demo","p1.SomeGroovyHelper");
		expectingNoProblems();
	}

	public void testNPEAnno_1398() throws Exception {
		IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$ //$NON-NLS-2$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "", "Anno", "public @interface Anno {\n"
				+ "	String[] value();\n" + "}");

		env.addGroovyClass(root, "", "Const", "public class Const {\n"
				+ "				  private final static String instance= \"abc\";\n"
				+ "				}");

		env.addGroovyClass(root, "", "A", "@Anno(Const.instance)\n"
				+ "class A {}\n");

		incrementalBuild(projectPath);
		expectingCompiledClassesV("Anno", "Const", "A");
		expectingNoProblems();
		IPath pathToSecond = env.addGroovyClass(root, "", "A",
				"@Anno(Const.instance)\n" + "class A {}\n");
		incrementalBuild(projectPath);

		if (GroovyUtils.GROOVY_LEVEL < 18) {
			Problem[] probs = env.getProblemsFor(pathToSecond);
			boolean p1found = false;
			boolean p2found = false;
			for (int i = 0; i < probs.length; i++) {
				if (probs[i]
						.getMessage()
						.equals("Groovy:expected 'Const.instance' to be an inline constant of type java.lang.String not a property expression in @Anno")) {
					p1found = true;
				}
				if (probs[i]
						.getMessage()
						.startsWith(
								"Groovy:Attribute 'value' should have type 'java.lang.String'; but found type 'java.lang.Object' in @Anno")) {
					p2found = true;
				}
			}
			if (!p1found) {
				printProblemsFor(pathToSecond);
				fail("Didn't get expected message 'Groovy:expected 'Const.instance' to be an inline constant of type java.lang.String not a property expression in @Anno'\n");
			}
			if (!p2found) {
				printProblemsFor(pathToSecond);
				fail("Didn't get expected message 'Groovy:Attribute 'value' should have type 'java.lang.String'; but found type 'java.lang.Object' in @Anno'\n");
			}
		} else {
			expectingNoProblems();
			expectingCompiledClassesV("A");
		}

	}
	

	// bug not yet fixed
//    public void testGreclipse1506() throws Exception {
//	   	if (GroovyUtils.GROOVY_LEVEL < 20) {
//	   		return;
//	   	}
//	    IPath projectPath = env.addProject("Project","1.6"); //$NON-NLS-1$
//		env.addExternalJars(projectPath, Util.getJavaClassLibs());
//		env.addGroovyJars(projectPath);
////		URL jar = Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry("lib/slf4j-api-1.6.6.jar");
////        env.addExternalJar(projectPath, FileLocator.resolve(jar).getFile());
//		fullBuild(projectPath);
//		// remove old package fragment root so that names don't collide
//		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
//	
//		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
//		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
//		
//		JDTResolver.recordInstances = true;
//		
//		env.addGroovyClass(root, "", "LoggerTest",
//				"import groovy.transform.TypeChecked;\n"+
//				"import groovy.util.logging.Slf4j;\n"+
//				"\n"+	
//				"@Slf4j\n"+
//			    "@TypeChecked\n"+
//			    "public class LoggerTest\n"+
//			    "{\n"+
//			    "	public static void main(String... args)\n"+
//			    "	{\n"+
//			    "		println 'println'\n"+
//	//		    "		LoggerTest.log.info('Logged');\n"+
//			    "		log.info('foo')\n"+
//			    "	}\n"+
//			    "}\n" );
//	
//		incrementalBuild(projectPath);
//	//	expectingCompiledClassesV("Foo","List2");
//		expectingNoProblems();
//	}
	
//	if (GroovyUtils.GROOVY_LEVEL<20) {
//		return;
//	}
//	runNegativeTest(new String[]{
//			"Foo.groovy",
//			"import groovy.transform.CompileStatic\n"+
//			"@CompileStatic\n"+
//			"void method(String message) {\n"+
//			"   List<Integer> ls = new ArrayList<Integer>();\n"+
//			"   ls.add(123);\n"+
//			"   ls.add('abc');\n"+
//			"}"
//	},
//	"----------\n" + 
//	"1. ERROR in Foo.groovy (at line 6)\n" + 
//	"	ls.add(\'abc\');\n" + 
//	"	^"+(isGE20()?"^^^^^^^^^^^^":"")+"\n" + 
//	"Groovy:[Static type checking] - Cannot find matching method java.util.ArrayList#add(java.lang.String)"+(isGE20()?". Please check if the declared type is right and if the method exists.":"")+"\n" + 
//	"----------\n");
	
	
	
	public void testCompileStatic_1505() throws Exception {
		try {
			if (GroovyUtils.GROOVY_LEVEL < 20) {
				return; 
			}
			IPath projectPath = env.addProject("Project","1.6"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());
			env.addGroovyJars(projectPath);
			fullBuild(projectPath);
			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	
			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
			
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
//			expectingCompiledClassesV("Foo","List2");
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
			// expectingCompiledClassesV("Client");
		} finally {
			JDTResolver.recordInstances = false;
		}
	}
	
//	public void testCompileStatic_1506() throws Exception {
//		try {
//			if (GroovyUtils.GROOVY_LEVEL < 20) {
//				return;
//			}
//			IPath projectPath = env.addProject("Project","1.6"); //$NON-NLS-1$
//			env.addExternalJars(projectPath, Util.getJavaClassLibs());
//			env.addGroovyJars(projectPath);
//			fullBuild(projectPath);
//			// remove old package fragment root so that names don't collide
//			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
//	
//			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
//			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
//			
//			JDTResolver.recordInstances = true;
//			
//			env.addGroovyClass(root, "", "Foo",
//					"import groovy.transform.CompileStatic\n"+
//					"@CompileStatic\n"+
//					"void method(String message) {\n"+
//					"   Collection<Integer> cs;\n"+
////					"   List<Integer> ls = new ArrayList<Integer>();\n"+
////					"   ls.add(123);\n"+
////					"   ls.add('abc');\n"+
//					// GRECLIPSE-1511 code
//					"	List<String> second = []\n"+
//					"	List<String> artefactResources2\n"+
//					"	second.addAll(artefactResources2)\n"+
//					"}\n"+
//					"interface ListOfFile extends ArrayList<File> {\n"+
//					"}"
//					);
//	
//			incrementalBuild(projectPath);
////			expectingCompiledClassesV("Foo","List2");
//			expectingNoProblems();
//			
//			// Now compare the generics structure for List (built by jdtresolver mapping into groovy) against List2 (built by groovy)
//			
//			// Access the jdtresolver bits and pieces 
//			
//			JDTClassNode jcn = JDTResolver.getCachedNode("ListOfFile");
//			
//			assertNotNull(jcn);
//			System.out.println("JDT ClassNode="+jcn);
////			JDTClassNode jcn2 = jdtr.getCachedNode("List2");
////			System.out.println(jcn2);
//			
////			List<File> C = new ArrayList<File>();
//			ClassNode listcn = new ClassNode(java.util.Collection.class);
//			VMPluginFactory.getPlugin().setAdditionalClassInformation(listcn);
//			listcn.lazyClassInit();
//			System.out.println("Groovy ClassNode="+listcn);
//			
////			IJavaProject ijp = env.getJavaProject("Project");
////			GroovyCompilationUnit unit = (GroovyCompilationUnit) ijp.findType("Foo")
////					.getCompilationUnit();
//
//			// now find the class reference
////			ClassNode cn = unit.getModuleNode().getClasses().get(1);
////			System.out.println(cn);
//			
//			// Compare java.util.List from JDTClassNode and List2 from groovy
//			compareClassNodes(jcn.redirect(),listcn.redirect(),0);
//			MethodNode jmn = getMethodNode(jcn,"add",1); // boolean add(E)
//			MethodNode rmn = getMethodNode(listcn,"add",1);						
//			compareMethodNodes(jmn,rmn);
//			
//			jmn = getMethodNode(jcn,"addAll",1);
//			rmn = getMethodNode(listcn,"addAll",1);
//			compareMethodNodes(jmn,rmn);
//			
//			// Want to compare type information in the 
//			// env.addClass(root, "", "Client", "public class Client {\n"
//			// + "  { new Outer.Inner(); }\n" + "}\n");
//			// incrementalBuild(projectPath);
//			// expectingNoProblems();
//			// expectingCompiledClassesV("Client");
//		} finally {
//			JDTResolver.recordInstances = false;
//		}
//	}
	
	public void testCompileStatic_ArrayArray() throws Exception {
		if (GroovyUtils.GROOVY_LEVEL < 20) {
			return;
		}
		IPath projectPath = env.addProject("Project","1.6"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		
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
		expectingCompiledClassesV("IChooseImportQuery","ISourceRange","NoChoiceQuery","TypeNameMatch");
	}
	
	public void testCompileStatic_FileAddAll() throws Exception {
		try {
			if (GroovyUtils.GROOVY_LEVEL < 20) {
				return;
			}
			IPath projectPath = env.addProject("Project","1.6"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());
			env.addGroovyJars(projectPath);
			fullBuild(projectPath);
			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	
			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
			
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
			expectingCompiledClassesV("Foo");
			
			// Now compare the generics structure for List (built by jdtresolver mapping into groovy) against List2 (built by groovy)
		} finally {
			JDTResolver.recordInstances = false;
		}
	}
	
	public void testCompileStatic_ListFileArgIteratedOver() throws Exception {
		try {
			if (GroovyUtils.GROOVY_LEVEL < 20) {
				return;
			}
			IPath projectPath = env.addProject("Project","1.6"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());
			env.addGroovyJars(projectPath);
			fullBuild(projectPath);
			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	
			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
			
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
			expectingCompiledClassesV("Foo");
			
			// Now compare the generics structure for List (built by jdtresolver mapping into groovy) against List2 (built by groovy)
		} finally {
			JDTResolver.recordInstances = false;
		}
	}
	
	public void testCompileStatic_IterableParameter() throws Exception {
		try {
			if (GroovyUtils.GROOVY_LEVEL < 20) {
				return;
			}
			IPath projectPath = env.addProject("Project","1.6"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());
			env.addGroovyJars(projectPath);
			fullBuild(projectPath);
			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	
			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
			
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
			expectingCompiledClassesV("Foo");
			
			// Now compare the generics structure for List (built by jdtresolver mapping into groovy) against List2 (built by groovy)
		} finally {
			JDTResolver.recordInstances = false;
		}
	}
	
	public void testCompileStatic_BuildSettings() throws Exception {
		try {
			if (GroovyUtils.GROOVY_LEVEL < 20) {
				return;
			}
			IPath projectPath = env.addProject("Project","1.6"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());
			env.addGroovyJars(projectPath);
			fullBuild(projectPath);
			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	
			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
			
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
			expectingCompiledClassesV("BuildSettings");
			
			// Now compare the generics structure for List (built by jdtresolver mapping into groovy) against List2 (built by groovy)
		} finally {
			JDTResolver.recordInstances = false;
		}
	}
	
	private MethodNode getMethodNode(ClassNode jcn, String selector, int paramCount) {
		List<MethodNode> mns = jcn.getDeclaredMethods(selector);
		for (MethodNode mn: mns) {
			if (mn.getParameters().length==paramCount) {
				return mn;
			}
		}
		return null;
	}

	private void compareMethodNodes(MethodNode jmn, MethodNode mn) {
		System.out.println("\n\n\nComparing method nodes jmn="+jmn+" mn="+mn);
		System.out.println("Comparing return types");
		compareClassNodes(jmn.getReturnType(), mn.getReturnType(),1);
		compareParameterArrays(jmn.getParameters(),mn.getParameters(),1);
	}

	private void compareParameterArrays(Parameter[] jps,
			Parameter[] ps,int d) {
		if (ps==null) {
			if (jps!=null) {
				fail("Expected null parameters but was "+arrayToString(jps));
			}
		} else {
			if (ps.length!=jps.length) {
				fail("Expected same number of parameters, should be "+arrayToString(ps)+" but was "+arrayToString(jps));
			}
			for (int p=0;p<ps.length;p++) {
				System.out.println("Comparing parameters jp="+jps[p]+" p="+ps[p]);
				compareParameters(jps[p],ps[p],d+1);
			}
		}
	}

	private void compareParameters(Parameter jp, Parameter p,int d) {
		compareClassNodes(jp.getType(),p.getType(),d+1);
	}

	// check whether these are identical (in everything except name!)
	private void compareClassNodes(ClassNode jcn, ClassNode cn,int d) {
		System.out.println("Comparing ClassNodes\njcn="+jcn.toString()+"\n cn="+cn.toString());
		assertEquals(cn.isGenericsPlaceHolder(),jcn.isGenericsPlaceHolder());
		
		// Check GenericsType info
		GenericsType[] gt_cn = cn.getGenericsTypes();
		GenericsType[] gt_jcn = jcn.getGenericsTypes();
		if (gt_cn==null) {
			if (gt_jcn!=null) {
				fail("Should have been null but was "+arrayToString(gt_jcn));
			}
		} else {
			if (gt_jcn==null) {
				fail("Did not expect genericstypes to be null, should be "+arrayToString(gt_cn));
			}
			assertNotNull(gt_jcn);
			assertEquals(gt_cn.length,gt_jcn.length);
			for (int i=0;i<gt_cn.length;i++) {
				System.out.println("Comparing generics types information, index #"+i);
				compareGenericsTypes(gt_jcn[i],gt_cn[i],d+1);
			}
		}
	}

	private void compareGenericsTypes(GenericsType jgt, GenericsType gt,int d) {
		//			protected ClassNode[] upperBounds;
		//		    protected ClassNode lowerBound;
		//		    protected ClassNode type;
		//		    protected String name;
		//		    protected boolean placeholder;
		//		    private boolean resolved;
		//		    private boolean wildcard;
//		assertEquals(jgt.getText(),gt.getText());
		assertEquals(jgt.getName(),gt.getName());
		assertEquals(jgt.isPlaceholder(),gt.isPlaceholder());
		assertEquals(jgt.isResolved(),gt.isResolved());
		assertEquals(jgt.isWildcard(),gt.isWildcard());
		compareType(jgt.getType(),gt.getType(),d+1);
		compareUpperBounds(jgt.getUpperBounds(),gt.getUpperBounds(),d+1);
		compareLowerBound(jgt.getLowerBound(),gt.getLowerBound(),d+1);
	}

	private void compareType(ClassNode jcn, ClassNode cn,int d) {
		System.out.println("Compare type of GenericsType: jcn="+jcn+" cn="+cn);
		compareClassNodes(jcn, cn,d+1);
	}

	private String arrayToString(ClassNode[] cns) {
		if (cns==null) {
			return "NULL";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i=0;i<cns.length;i++) {
			if (i>0) sb.append(",");
			sb.append(cns[i]);
		}
		sb.append("]");
		return sb.toString();
	}

	private void compareUpperBounds(ClassNode[] jcnlist, ClassNode[] cnlist,int d) {
		System.out.println("Comparing upper bounds: jcn="+arrayToString(jcnlist)+" cn="+arrayToString(cnlist));
		if (cnlist==null) {
			if (jcnlist!=null) {
				fail("Should be null but is "+arrayToString(jcnlist));
			}
		} else {
			if (jcnlist==null) {
				fail("Array not expected to be null, should be "+arrayToString(cnlist));
			}
			assertEquals(cnlist.length,cnlist.length);
			for (int i=0;i<cnlist.length;i++) {
				compareClassNodes(jcnlist[i].redirect(), cnlist[i].redirect(),d+1);
			}
		}
	}
	
	private void compareLowerBound(ClassNode jcn,
			ClassNode cn,int d) {
		System.out.println("Comparing lower bound");
		if (jcn==null) {
			assertNull(cn);
		} else {
			assertNotNull(cn);
			compareClassNodes(jcn.redirect(), cn.redirect(),d+1);
		}
	}

	public void testInners_983() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addGroovyClass(root, "", "Outer", "class Outer {\n"
				+ "  static class Inner {}\n" + "}\n");

		env.addClass(root, "", "Client", "public class Client {\n"
				+ "  { new Outer.Inner(); }\n" + "}\n");

		incrementalBuild(projectPath);
		expectingCompiledClassesV("Client", "Outer", "Outer$Inner");
		expectingNoProblems();
		env.addClass(root, "", "Client", "public class Client {\n"
				+ "  { new Outer.Inner(); }\n" + "}\n");
		incrementalBuild(projectPath);
		expectingNoProblems();
		expectingCompiledClassesV("Client");

	}

	public void testCompileStatic() throws Exception {
		if (GroovyUtils.GROOVY_LEVEL < 20) {
			return;
		}
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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
		expectingCompiledClassesV("Outer");
		expectingNoProblems();
		// env.addClass(root, "", "Client", "public class Client {\n"
		// + "  { new Outer.Inner(); }\n" + "}\n");
		// incrementalBuild(projectPath);
		// expectingNoProblems();
		// expectingCompiledClassesV("Client");

	}
	
	
	// verify generics are correct for the 'Closure<?>' as CompileStatic will attempt an exact match
	public void testCompileStatic2() throws Exception {
		if (GroovyUtils.GROOVY_LEVEL < 20) {
			return;
		}
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addGroovyClass(root, "", "A",
				"class A {\n"+
				"	public void profile(String name, groovy.lang.Closure<?> callable) {	}\n"+
				"}\n");

		incrementalBuild(projectPath);
		expectingCompiledClassesV("A");
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
		expectingCompiledClassesV("B","B$_foo_closure1");
		expectingNoProblems();
	}
	
	public void testCompileStatic3() throws Exception {
		if (GroovyUtils.GROOVY_LEVEL < 20) {
			return;
		}
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addGroovyClass(root, "", "Foo",
				"class Foo {\n"+
				"	@groovy.transform.CompileStatic\n"+
				"	public static void main(String[] args) {\n"+
				"		((GroovyObject)new Foo());\n"+
				"	}\n"+
				"}\n");

		incrementalBuild(projectPath);
		expectingNoProblems();
		expectingCompiledClassesV("Foo");
	}
	

	public void test1167() throws Exception {
		IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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
		expectingCompiledClassesV("brooklyn.event.adapter.Foo",
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
		expectingCompiledClassesV("brooklyn.event.adapter.Foo",
				"brooklyn.event.adapter.HttpSensorAdapter");

	}

	// Activate when identified script recognition does not damage performance
	// public void testScriptSupport() throws Exception {
	//		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	// env.addExternalJars(projectPath, Util.getJavaClassLibs());
	// env.addGroovyJars(projectPath);
	// fullBuild(projectPath);
	//
	// // remove old package fragment root so that names don't collide
	//		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	//
	// // The fact that this is 'scripts' should cause us to suppress the .class
	// file
	//		IPath root = env.addPackageFragmentRoot(projectPath, "scripts"); //$NON-NLS-1$
	//		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	//
	// env.addGroovyClass(root, "p1", "Hello",
	// "package p1;\n"+
	// "public class Hello {\n"+
	// "   public static void main(String[] args) {\n"+
	// "      System.out.println(\"Hello world\");\n"+
	// "   }\n"+
	// "}\n"
	// );
	//
	// incrementalBuild(projectPath);
	// // No compiled output as it was a script
	// expectingCompiledClassesV("");
	// expectingNoProblems();
	// }

	public void testTypeDuplication_GRE796_1() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addGroovyClass(root, "p", "Foo", "package p;\n" + "class Foo{}\n");

		IPath root2 = env.addPackageFragmentRoot(projectPath, "src2"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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

	public void testTypeDuplication_GRE796_2() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addGroovyClass(root, "p", "Foo", "package p;\n" + "class Foo {}\n");

		IPath root2 = env.addPackageFragmentRoot(projectPath, "src2"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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

	// script has no package statement
	public void testClashingPackageAndType_1214() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		env.addJar(projectPath, "lib/spock-core-0.4-groovy-1.7-SNAPSHOT.jar"); //$NON-NLS-1$
		env.addJar(projectPath, "lib/junit4_4.5.0.jar"); //$NON-NLS-1$
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath cuPath = env.addClass(root, "com.acme", "Foo",
				"package com.acme;\n" + "public class Foo {}\n");

		env.addGroovyClass(root, "com.acme.Foo", "xyz", "print 'abc'");

		incrementalBuild(projectPath);
		expectingSpecificProblemFor(cuPath, new Problem("",
				"The type Foo collides with a package", cuPath, 31, 34,
				CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_WARNING));
		expectingCompiledClassesV("com.acme.Foo", "xyz");
		executeClass(projectPath, "xyz", "abc", null);
	}

	public void testSlowAnotherAttempt_GRE870() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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

		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		// lots of errors on the missing static imports
		expectingCompiledClassesV("a.b.c.d.e.f.Helper1,a.b.c.d.e.f.Helper2,a.b.c.d.e.f.Helper3,a.b.c.d.e.f.Helper4,a.b.c.d.e.f.Helper5,a.b.c.d.e.f.Helper6,a.b.c.d.e.f.Helper7,a.b.c.d.e.f.Helper8,a.b.c.d.e.f.Helper9,a.b.c.d.e.f.HelperBase,a.b.c.d.e.f.SomeHelper,a.b.c.d.e.f.SomeTests");

	}

	public void testSlow_GRE870() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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

		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		// lots of errors on the missing static imports
		expectingCompiledClassesV("Foo");

	}

	public void testReallySlow_GRE870() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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

		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		// lots of errors on the missing static imports
		expectingCompiledClassesV("");

	}

	public void testClosureBasics() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addGroovyClass(root, "", "Coroutine", "def iterate(n, closure) {\n"
				+ "  1.upto(n) {\n" + "    closure(it);\n" + "  }\n" + "}\n"
				+ "iterate (3) {\n" + "  print it*2\n" + "}\n");

		// three classes created for that:
		// GroovyClass(name=Coroutine bytes=6372),
		// GroovyClass(name=Coroutine$_run_closure1 bytes=2875),
		// GroovyClass(name=Coroutine$_iterate_closure2 bytes=3178)

		incrementalBuild(projectPath);
		expectingCompiledClassesV("Coroutine", "Coroutine$_run_closure1",
				"Coroutine$_iterate_closure2");
		expectingNoProblems();
		executeClass(projectPath, "Coroutine", "246", "");
	}

	public void testPackageNames_GRE342_1() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		// q.X declared in p.X
		IPath path = env.addGroovyClass(root, "p", "X", "package q\n"
				+ "class X {}");

		incrementalBuild(projectPath);

		expectingOnlySpecificProblemFor(
				path,
				new Problem(
						"p/X", "The declared package \"q\" does not match the expected package \"p\"", path, 8, 9, 60, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$		
	}

	public void testPackageNames_GRE342_2() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		// q.X declared in p.X
		IPath path = env.addGroovyClass(root, "p.q.r", "X", "package p.s.r.q\n"
				+ "class X {}");

		incrementalBuild(projectPath);

		expectingOnlySpecificProblemFor(
				path,
				new Problem(
						"p/q/r/X", "The declared package \"p.s.r.q\" does not match the expected package \"p.q.r\"", path, 8, 15, 60, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$		
	}

	public void testPackageNames_GRE342_3() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		// in p.q.r.X but has no package decl - should be OK
		env.addGroovyClass(root, "p.q.r", "X", "print 'abc'");

		incrementalBuild(projectPath);

		expectingNoProblems();
		executeClass(projectPath, "X", "abc", "");
	}

	public void testAnnotationCollectorMultiProject() throws Exception {
		if (GroovyUtils.GROOVY_LEVEL < 21) {
			System.out.println("Skipping test. Need groovy 2.2 or above");
			return;
		}
		Version version = JavaCore.getPlugin().getBundle().getVersion();
		if (version.compareTo(new Version("3.9.50"))<0) {
			System.out.println("Skipping test. Need greclipse for e43j8 or above");
			return;
		}
		
		// Construct 'annotation' project that defines annotation using 'AnnotationsCollector'
		IPath annotationProject = env.addProject("annotation"); //$NON-NLS-1$
		env.addExternalJars(annotationProject, Util.getJavaClassLibs());
		env.addGroovyJars(annotationProject);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(annotationProject, ""); //$NON-NLS-1$

		IPath annotationRoot = env.addPackageFragmentRoot(annotationProject, "src"); //$NON-NLS-1$
		env.setOutputFolder(annotationProject, "bin"); //$NON-NLS-1$

		
		env.addGroovyClass(annotationRoot, "com.demo", "MyAnnotation",
				"package com.demo\r\n" + 
				"\r\n" + 
				"@groovy.transform.AnnotationCollector\r\n" + 
				"@Deprecated\r\n" + 
				"@interface MyAnnotation {}\r\n"
		);
		
		// Construct 'app' project that uses the annotation
		IPath appProject = env.addProject("app"); //$NON-NLS-1$
		env.addExternalJars(appProject, Util.getJavaClassLibs());
		env.addGroovyJars(appProject);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(appProject, ""); //$NON-NLS-1$
		
		env.addRequiredProject(appProject, annotationProject);

		IPath appRoot = env.addPackageFragmentRoot(appProject, "src"); //$NON-NLS-1$
		env.setOutputFolder(appProject, "bin"); //$NON-NLS-1$
		
		env.addGroovyClass(appRoot, "com.demo", "Widget",
				"package com.demo\r\n" + 
				"\r\n" + 
				"@MyAnnotation\r\n" + 
				"class Widget {}\r\n"
		);
		
		fullBuild();
		expectedCompiledClassCount(2);
		expectingNoProblems();
	}
	
	
	public void testAnnotationCollectorIncremental() throws Exception {

		if (GroovyUtils.GROOVY_LEVEL < 21) {
			return;
		}
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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
		expectingCompiledClassesV("Book","Length","NotNull","ISBN");
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
		expectingCompiledClassesV("Book");
		expectingNoProblems();
		executeClass(projectPath, "Book", "@NotNull()\n@Length()\n", "");
	}
	
	public void testClosureIncremental() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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
		expectingCompiledClassesV("OtherGroovy",
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
		expectingCompiledClassesV("Runner", "Runner$_run_closure1");
		expectingNoProblems();
		executeClass(projectPath, "Launch", "1234", "");

		// modify how the closure is called
		env.addGroovyClass(root, "pkg", "OtherGroovy",
				"def static iterate(Integer n, closure) {\n"
						+ "  1.upto(n*2) {\n" + // change here
						"    closure(it);\n" + "  }\n" + "}\n");

		incrementalBuild(projectPath);
		expectingCompiledClassesV("OtherGroovy",
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
		expectingCompiledClassesV("OtherGroovy",
				"OtherGroovy$_iterate_closure1", "Runner",
				"Runner$_run_closure1");
		expectingNoProblems();
		executeClass(projectPath, "Launch", "12345678", "");

	}

	// http://jira.codehaus.org/browse/GRECLIPSE-558
	/**
	 * The aim of this test is to verify the processing in
	 * ASTTransformationCollectorCodeVisitor - to check it finds everything it
	 * expects.
	 */
	public void testSpock_GRE558() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		env.addJar(projectPath, "lib/spock-core-0.4-groovy-1.7-SNAPSHOT.jar"); //$NON-NLS-1$
		env.addJar(projectPath, "lib/junit4_4.5.0.jar"); //$NON-NLS-1$
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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
		expectingCompiledClassesV("MyTest");
		executeClass(projectPath, "MyTest", "success", null);
	}

	/**
	 * Testing that the transform occurs on an incremental change. The key thing
	 * being looked at here is that the incremental change is not directly to a
	 * transformed file but to a file referenced from a transformed file.
	 */
	public void testSpock_GRE605_1() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		env.addJar(projectPath, "lib/spock-core-0.4-groovy-1.7-SNAPSHOT.jar"); //$NON-NLS-1$
		env.addJar(projectPath, "lib/junit4_4.5.0.jar"); //$NON-NLS-1$
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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
		expectingCompiledClassesV("Foobar", "FoobarSpec");

		IPath workspacePath = env.getWorkspaceRootPath();
		File f = new File(workspacePath.append(
				env.getOutputLocation(projectPath)).toOSString(),
				"FoobarSpec.class");
		long filesize = f.length(); // this is 9131 for groovy 1.7.0

		env.addGroovyClass(root, "", "Foobar", "class Foobar {\n" + "\n"
				+ "def baz = 42\n" + "def quux = 36\n" + "\n" + "}\n");
		incrementalBuild(projectPath);
		expectingNoProblems();
		expectingCompiledClassesV("Foobar", "FoobarSpec");

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
	public void testSpock_GRE605_2() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		env.addJar(projectPath, "lib/spock-core-0.4-groovy-1.7-SNAPSHOT.jar"); //$NON-NLS-1$
		env.addJar(projectPath, "lib/junit4_4.5.0.jar"); //$NON-NLS-1$
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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
		expectingCompiledClassesV("Foobar", "FoobarSpec");

		// IPath workspacePath = env.getWorkspaceRootPath();
		// File f = new
		// File(workspacePath.append(env.getOutputLocation(projectPath)).toOSString(),"FoobarSpec.class");
		// long filesize = f.length(); // this is 9131 for groovy 1.7.0

		env.addGroovyClass(root, "", "Foobar", "class Foobar {\n" + "\n"
				+ "def baz = 42\n" + "def quux = 36\n" + "\n" + "}\n");
		incrementalBuild(projectPath);
		expectingNoProblems();
		expectingCompiledClassesV("Foobar", "FoobarSpec");

		// long filesizeNow = f.length(); // drops to 7002 if transform did not
		// run
		// assertEquals(filesize,filesizeNow);
	}

	// build .groovy file hello world then run it
	public void testBuildGroovyHelloWorld() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addGroovyClass(root, "p1", "Hello", "package p1;\n"
				+ "class Hello {\n" + "   static void main(String[] args) {\n"
				+ "      print \"Hello Groovy world\"\n" + "   }\n" + "}\n");

		incrementalBuild(projectPath);
		expectingCompiledClassesV("p1.Hello");
		expectingNoProblems();
		executeClass(projectPath, "p1.Hello", "Hello Groovy world", null);
	}

	// use funky main method
	public void testBuildGroovyHelloWorld2() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addGroovyClass(root, "p1", "Hello", "package p1;\n"
				+ "class Hello {\n" + "   static main(args) {\n"
				+ "      print \"Hello Groovy world\"\n" + "   }\n" + "}\n");

		incrementalBuild(projectPath);
		expectingCompiledClassesV("p1.Hello");
		expectingNoProblems();
		executeClass(projectPath, "p1.Hello", "Hello Groovy world", null);
	}

	public void testGenericMethods() throws Exception {
		IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "", "Foo", "public class Foo<T> {\n"
				+ "   public void m() {\n" + "      Bar.agent(null);\n"
				+ "   }\n" + "}\n");

		env.addGroovyClass(root, "", "Bar", "class Bar {\n"
				+ "   public static <PP> void agent(PP state) {\n" + "   }\n"
				+ "}\n");

		incrementalBuild(projectPath);
		// expectingCompiledClassesV("Foo","Bar");
		expectingNoProblems();
	}

	public void testPropertyAccessorLocationChecks() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addGroovyClass(root, "p1", "Hello", "package p1;\n"
				+ "class Hello {\n" + "  int color;\n"
				+ "   static void main(String[] args) {\n"
				+ "      print \"Hello Groovy world\"\n" + "   }\n" + "}\n");

		incrementalBuild(projectPath);
		expectingCompiledClassesV("p1.Hello");
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

	// private IJavaElement find(IJavaElement pkgFragmentRoot,String name) {
	// try {
	// IJavaElement[] kids = ((JavaElement)pkgFragmentRoot).getChildren();
	// return findChild(kids,name,0);
	// } catch (JavaModelException e) {
	// e.printStackTrace();
	// return null;
	// }
	// }

	// private IJavaElement findChild(IJavaElement[] kids, String name, int
	// depth) throws JavaModelException {
	// if (depth>10) return null;
	// for (IJavaElement kid: kids) {
	// System.out.println(kid.getElementName());
	// if (kid.getElementName().equals(name)) {
	// return kid;
	// }
	// IJavaElement found =
	// findChild(((JavaElement)kid).getChildren(),name,depth+1);
	// if (found!=null) {
	// return found;
	// }
	// }
	// return null;
	// }

	// build .groovy file hello world then run it
	public void testBuildGroovy2() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addGroovyClass(root, "p1", "Hello", "package p1;\n"
				+ "interface Hello extends java.util.List {\n" + "}\n");

		incrementalBuild(projectPath);
		expectingCompiledClassesV("p1.Hello");
		expectingNoProblems();
	}

	public void testLargeProjects_GRE1037() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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

	public void testIncrementalCompilationTheBasics() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "pkg", "Hello", "package pkg;\n"
				+ "public class Hello {\n"
				+ "   public static void main(String[] args) {\n"
				+ "      System.out.println(new GHello().run());\n" + "   }\n"
				+ "}\n");

		env.addGroovyClass(root, "pkg", "GHello", "package pkg;\n"
				+ "public class GHello {\n"
				+ "   public int run() { return 12; }\n" + "}\n");

		incrementalBuild(projectPath);
		expectingCompiledClassesV("pkg.Hello", "pkg.GHello");
		expectingNoProblems();
		executeClass(projectPath, "pkg.Hello", "12", "");

		// whitespace change to groovy file
		env.addGroovyClass(root, "pkg", "GHello", "package pkg;\n"
				+ "public class GHello {\n" + "  \n" + // new blank line
				"   public int run() { return 12; }\n" + "}\n");
		incrementalBuild(projectPath);
		expectingCompiledClassesV("pkg.GHello");
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
		expectingCompiledClassesV("pkg.GHello", "pkg.Hello");
		expectingNoProblems();

	}
	
	public void testIncrementalCompilation1594() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$


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
		expectingCompiledClassesV("testpkg.AllTests", "testpkg.TestCaseChannelPersistentStore","testpkg.TestCaseChannelPersistentStore$1");
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
//		expectingCompiledClassesV("pkg.GHello");
		expectingNoProblems();
	}

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
		expectingCompiledClassesV("pkg.Event", "pkg.EventImpl", "pkg.Intface",
				"pkg.Jaas", "pkg.GExtender");
		expectingNoProblems();

		env.addGroovyClass(root, "pkg", "GExtender", "package pkg\n"
				+ "class GExtender extends Jaas{\n" + "}\n");

		incrementalBuild(projectPath);
		expectingNoProblems();
		expectingCompiledClassesV("pkg.GExtender");
	}

	public void testIncrementalCompilationTheBasics2_changingJavaDependedUponByGroovy()
			throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "pkg", "Hello", "package pkg;\n"
				+ "public class Hello {\n"
				+ "  public int run() { return 12; }\n" + "}\n");

		env.addGroovyClass(root, "pkg", "GHello", "package pkg;\n"
				+ "public class GHello {\n"
				+ "   public static void main(String[] args) {\n"
				+ "      System.out.println(new Hello().run());\n" + "   }\n"
				+ "}\n");

		incrementalBuild(projectPath);
		expectingCompiledClassesV("pkg.Hello", "pkg.GHello");
		expectingNoProblems();
		executeClass(projectPath, "pkg.GHello", "12", "");

		// whitespace change to java file
		env.addClass(root, "pkg", "Hello", "package pkg;\n"
				+ "public class Hello {\n" + "  \n" + // new blank line
				"  public int run() { return 12; }\n" + "}\n");
		incrementalBuild(projectPath);
		expectingCompiledClassesV("pkg.Hello");
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
		expectingCompiledClassesV("pkg.GHello", "pkg.Hello");
		expectingNoProblems();
		executeClass(projectPath, "pkg.GHello", "abc", "");

	}

	public void testInnerClasses_GRE339() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "", "Outer", "public interface Outer {\n"
				+ "  interface Inner { static String VAR=\"value\";}\n" + "}\n");

		env.addGroovyClass(root, "", "script", "print Outer.Inner.VAR\n");

		incrementalBuild(projectPath);
		try { Thread.sleep(1000); } catch (Exception e) {}
		expectingCompiledClassesV("Outer", "Outer$Inner", "script");
		expectingNoProblems();
		executeClass(projectPath, "script", "value", "");

		// whitespace change to groovy file
		env.addGroovyClass(root, "", "script", "print Outer.Inner.VAR \n");

		incrementalBuild(projectPath);
		expectingCompiledClassesV("script");
		expectingNoProblems();
		executeClass(projectPath, "script", "value", null);

	}

	// TODO test for this - package disagrees with file, shouldn't npe in
	// binding locating code
	// env.addGroovyClass(root, "pkg", "GHello",
	// "package p1;\n"+
	// "public class GHello {\n"+
	// "   public int run() { return 12; }\n"+
	// "}\n"
	// );

	public void testSimpleTaskMarkerInSingleLineComment() throws Exception {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo"); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addGroovyClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n" + //$NON-NLS-1$
						"class C {\n" + "//todo nothing\n" + //$NON-NLS-1$  // 24>36 'todo nothing'
						"\n" + "//tooo two\n" + //$NON-NLS-1$
						"}"); //$NON-NLS-1$

		fullBuild(projectPath);

		Problem[] rootProblems = env.getProblemsFor(pathToA);
		for (int i = 0; i < rootProblems.length; i++) {
			System.out.println(i + "  " + rootProblems[i] + "["
					+ rootProblems[i].getMessage() + "]"
					+ rootProblems[i].getEnd());
		}
		// positions should be from the first character of the tag to the
		// character after the last in the text
		expectingOnlySpecificProblemFor(
				pathToA,
				new Problem(
						"A", toTask("todo", "nothing"), pathToA, 24, 36, -1, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		JavaCore.setOptions(options);
	}

	private String toTask(String tasktag, String message) {
		return tasktag + message;
	}

	public void testSimpleTaskMarkerInSingleLineCommentEndOfClass()
			throws Exception {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo"); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addGroovyClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n" + //$NON-NLS-1$
						"class C {\n" + "//topo nothing\n" + //$NON-NLS-1$ '/' is 22 'n' is 29 'g' is 35
						"\n" + "//todo two\n" + //$NON-NLS-1$ '/' is 38 't' is 45 'o' is 47
						"}"); //$NON-NLS-1$

		fullBuild(projectPath);

		Problem[] rootProblems = env.getProblemsFor(pathToA);
		expectingOnlySpecificProblemFor(
				pathToA,
				new Problem(
						"A", toTask("todo", "two"), pathToA, 40, 48, -1, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		JavaCore.setOptions(options);
	}

	public void testSimpleTaskMarkerInSingleLineCommentEndOfClassCaseInsensitive()
			throws Exception {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo"); //$NON-NLS-1$
		newOptions
				.put(JavaCore.COMPILER_TASK_CASE_SENSITIVE, JavaCore.DISABLED); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addGroovyClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n" + //$NON-NLS-1$
						"class C {\n" + "//TODO nothing\n" + //$NON-NLS-1$ '/' is 22 'n' is 29 'g' is 35
						"\n" + "//topo two\n" + //$NON-NLS-1$ '/' is 38 't' is 45 'o' is 47
						"}"); //$NON-NLS-1$

		fullBuild(projectPath);

		Problem[] rootProblems = env.getProblemsFor(pathToA);
		for (int i = 0; i < rootProblems.length; i++) {
			System.out.println(i + "  " + rootProblems[i] + "["
					+ rootProblems[i].getMessage() + "]"
					+ rootProblems[i].getEnd());
		}
		expectingOnlySpecificProblemFor(
				pathToA,
				new Problem(
						"A", toTask("todo", "nothing"), pathToA, 24, 36, -1, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		JavaCore.setOptions(options);
	}

	public void testTaskMarkerInMultiLineCommentButOnOneLine() throws Exception {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo"); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addGroovyClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n" + //$NON-NLS-1$
						"/*  todo nothing */\n" + //$NON-NLS-1$ 
						"public class A {\n" + //$NON-NLS-1$
						"}"); //$NON-NLS-1$

		fullBuild(projectPath);

		Problem[] rootProblems = env.getProblemsFor(pathToA);
		for (int i = 0; i < rootProblems.length; i++) {
			System.out.println(i + "  " + rootProblems[i] + "["
					+ rootProblems[i].getMessage() + "]");
		}
		expectingOnlySpecificProblemFor(
				pathToA,
				new Problem(
						"A", toTask("todo", "nothing"), pathToA, 16, 29, -1, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		JavaCore.setOptions(options);
	}

	public void testTaskMarkerInMultiLineButNoText() throws Exception {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo"); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addGroovyClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n" + //$NON-NLS-1$
						"/*  todo\n" + //$NON-NLS-1$
						" */\n" + //$NON-NLS-1$ 
						"public class A {\n" + //$NON-NLS-1$
						"}"); //$NON-NLS-1$

		fullBuild(projectPath);

		Problem[] rootProblems = env.getProblemsFor(pathToA);
		for (int i = 0; i < rootProblems.length; i++) {
			System.out.println(i + "  " + rootProblems[i] + " ["
					+ rootProblems[i].getMessage() + "]");
		}
		expectingOnlySpecificProblemFor(
				pathToA,
				new Problem(
						"A", toTask("todo", ""), pathToA, 16, 20, -1, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		JavaCore.setOptions(options);
	}

	public void testTaskMarkerInMultiLineOutsideClass() throws Exception {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo"); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addGroovyClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n" + //$NON-NLS-1$
						"/*  \n" + //$NON-NLS-1$  12
						" * todo nothing *\n" + //$NON-NLS-1$  17
						" */\n" + //$NON-NLS-1$ 
						"public class A {\n" + //$NON-NLS-1$
						"}"); //$NON-NLS-1$

		fullBuild(projectPath);

		Problem[] rootProblems = env.getProblemsFor(pathToA);
		for (int i = 0; i < rootProblems.length; i++) {
			System.out.println(i + "  " + rootProblems[i] + " ["
					+ rootProblems[i].getMessage() + "]");
		}
		expectingOnlySpecificProblemFor(
				pathToA,
				new Problem(
						"A", toTask("todo", "nothing *"), pathToA, 20, 34, -1, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		JavaCore.setOptions(options);
	}

	// task marker inside a multi line comment inside a class
	public void testTaskMarkerInMultiLineInsideClass() throws Exception {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo"); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addGroovyClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n" + //$NON-NLS-1$ -- \n is 11
						"public class A {\n" + //$NON-NLS-1$ -- \n is 28 
						"   /*  \n" + //$NON-NLS-1$ -- \n is 36
						" * todo nothing *\n" + //$NON-NLS-1$ 
						" */\n" + //$NON-NLS-1$ 
						"}"); //$NON-NLS-1$

		fullBuild(projectPath);

		Problem[] rootProblems = env.getProblemsFor(pathToA);
		for (int i = 0; i < rootProblems.length; i++) {
			System.out.println(i + "  " + rootProblems[i]);
		}
		expectingOnlySpecificProblemFor(
				pathToA,
				new Problem(
						"A", toTask("todo", "nothing *"), pathToA, 40, 54, -1, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		JavaCore.setOptions(options);
	}

	// Testing tag priority
	public void testTaskMarkerMixedPriorities() throws Exception {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX"); //$NON-NLS-1$
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW"); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addGroovyClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n" + //$NON-NLS-1$
						"//TODO normal\n" + //$NON-NLS-1$
						"public class A {\n" + //$NON-NLS-1$
						"	public void foo() {\n" + //$NON-NLS-1$
						"		//FIXME high\n" + //$NON-NLS-1$
						"	}\n" + //$NON-NLS-1$
						"	public void foo2() {\n" + //$NON-NLS-1$
						"		//XXX low\n" + //$NON-NLS-1$
						"	}\n" + //$NON-NLS-1$
						"}"); //$NON-NLS-1$

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

	public void testTaskMarkerMultipleOnOneLineInSLComment() throws Exception {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX"); //$NON-NLS-1$
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW"); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addGroovyClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n" + //$NON-NLS-1$
						"// TODO FIXME need to review the loop TODO should be done\n"
						+ //$NON-NLS-1$
						"public class A {\n" + //$NON-NLS-1$
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

	public void testTaskMarkerMultipleOnOneLineInMLComment() throws Exception {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX"); //$NON-NLS-1$
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW"); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addGroovyClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n" + //$NON-NLS-1$
						"/* TODO FIXME need to review the loop TODO should be done */\n"
						+ //$NON-NLS-1$
						"public class A {\n" + //$NON-NLS-1$
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

	// two on one line
	@SuppressWarnings("rawtypes")
	public void testTaskMarkerSharedDescription() throws Exception {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX"); //$NON-NLS-1$
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW"); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addGroovyClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n" + //$NON-NLS-1$
						"// TODO TODO need to review the loop\n" + //$NON-NLS-1$
						"public class A {\n" + //$NON-NLS-1$
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

	public void testCopyGroovyResourceNonGroovyProject_GRECLIPSE653()
			throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.removeGroovyNature("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath output = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addGroovyClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n" + //$NON-NLS-1$
						"class C { }"); //$NON-NLS-1$

		fullBuild(projectPath);

		// groovy file should be copied as-is
		IPath pathToABin = output.append(pathToA.removeFirstSegments(pathToA
				.segmentCount() - 2));
		assertTrue("File should exist " + pathToABin.toPortableString(), env
				.getWorkspace().getRoot().getFile(pathToABin).exists());

		// now check that works for incremental
		IPath pathToB = env.addGroovyClass(root, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n" + //$NON-NLS-1$
						"class D { }"); //$NON-NLS-1$
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
		assertFalse("File should not exist " + pathToBBin.toPortableString(),
				env.getWorkspace().getRoot().getFile(pathToBBin).exists());
	}

	public void testCopyResourceNonGroovyProject_GRECLIPSE653()
			throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.removeGroovyNature("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath output = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addFile(root, "A.txt", "A"); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild(projectPath);

		// file should be copied as-is
		IPath pathToABin = output.append(pathToA.removeFirstSegments(pathToA
				.segmentCount() - 1));
		assertTrue("File should exist " + pathToABin.toPortableString(), env
				.getWorkspace().getRoot().getFile(pathToABin).exists());

		// now check that works for incremental
		IPath pathToB = env.addFile(root, "B.txt", "B"); //$NON-NLS-1$ //$NON-NLS-2$
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

	public void testCopyResourceGroovyProject_GRECLIPSE653() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		env.addGroovyNature("Project");

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath output = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addFile(root, "A.txt", "A"); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild(projectPath);

		// groovy file should be copied as-is
		IPath pathToABin = output.append(pathToA.removeFirstSegments(pathToA
				.segmentCount() - 1));
		assertTrue("File should exist " + pathToABin.toPortableString(), env
				.getWorkspace().getRoot().getFile(pathToABin).exists());

		// now check that works for incremental
		IPath pathToB = env.addFile(root, "B.txt", "B"); //$NON-NLS-1$ //$NON-NLS-2$
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

	// currently failing
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

	// GRECLIPSE-1170
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

	// GRECLIPSE-1727
	public void testTraitBasics() throws Exception {
		if (GroovyUtils.GROOVY_LEVEL < 23) {
			return;
		}
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addGroovyClass(root, "p", "Named",
				"trait Named {\n" +
				"    String name() { 'name' }" +
				"}\n");

		incrementalBuild(projectPath);
		expectingCompiledClassesV("Named", "Named$Trait$Helper");
		expectingNoProblems();
	}

	public void testTraitIncremental() throws Exception {
		if (GroovyUtils.GROOVY_LEVEL < 23) {
			return;
		}
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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
		expectingCompiledClassesV("p.Named", "p.Named$Trait$Helper", "q.NamedClass", "Runner");
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
		expectingCompiledClassesV("p.Named", "p.Named$Trait$Helper", "p.Named$Trait$FieldHelper", "q.NamedClass", "Runner");
		expectingNoProblems();
		executeClass(projectPath, "Runner", "null", "");

		env.addGroovyClass(root, "", "Runner",
				"import p.Named\n" +
				"import q.NamedClass\n" +
				"Named named = new NamedClass(name: 'name')\n" +
				"print named.name()\n");

		incrementalBuild(projectPath);
		expectingCompiledClassesV("Runner");
		expectingNoProblems();
		executeClass(projectPath, "Runner", "name", "");

		env.addGroovyClass(root, "q", "NamedClass",
				"package q;\n" +
				"import p.Named;\n" +
				"public class NamedClass implements Named {\n" +
				"    String name() { \"Hello, ${name}!\" }\n" +
				"}\n");
		incrementalBuild(projectPath);
		expectingCompiledClassesV("q.NamedClass", "Runner");
		expectingNoProblems();
		executeClass(projectPath, "Runner", "Hello, name!", "");
	}

	public void testTraitBinary() throws Exception {
		if (GroovyUtils.GROOVY_LEVEL < 23) {
			return;
		}
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addGroovyClass(root, "p", "Named",
				"package p\n" +
				"trait Named {\n" +
				"    String name() { 'name' }\n" +
				"}\n");

		env.addGroovyClass(root, "q", "DefaultNamed",
				"package q;\n" +
				"public class DefaultNamed {\n" +
				"    protected String name() { 'name' }\n" +
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
		expectingCompiledClassesV("p.Named", "p.Named$Trait$Helper", "q.DefaultNamed", "r.NamedClass", "Runner");
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
		expectingCompiledClassesV("r.NamedClass", "Runner");
		expectingNoProblems();
		executeClass(projectPath, "Runner", "new name", "");

		env.addGroovyClass(root, "r", "NamedClass",
				"package r;\n" +
				"import p.Named\n" +
				"import q.DefaultNamed\n" +
				"public class NamedClass extends DefaultNamed implements Named {}\n");

		incrementalBuild(projectPath);
		expectingCompiledClassesV("r.NamedClass", "Runner");
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
		expectingCompiledClassesV("p.Named", "p.Named$Trait$Helper", "r.NamedClass", "Runner");
		expectingNoProblems();
		executeClass(projectPath, "Runner", "new name", "");
	}

	public void testGRE1773() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

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
		expectingCompiledClassesV("test.Class1", "test.Class2", "test.Class3");
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
		expectingCompiledClassesV();
		expectingProblemsFor(class1, "Problem : Groovy:unexpected token: abstract @ line 3, column 5. [ resource : </Project/src/test/Class1.groovy> range : <41,42> category : <60> severity : <2>]");
		expectingProblemsFor(class2, "Problem : Groovy:Method \'m1\' from class \'test.Class2\' does not override method from its superclass or interfaces but is annotated with @Override. [ resource : </Project/src/test/Class2.groovy> range : <48,56> category : <60> severity : <2>]");
		expectingProblemsFor(class3, "Problem : Groovy:Method \'m1\' from class \'test.Class3\' does not override method from its superclass or interfaces but is annotated with @Override. [ resource : </Project/src/test/Class3.groovy> range : <48,56> category : <60> severity : <2>]");

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
		expectingCompiledClassesV("test.Class1", "test.Class2", "test.Class3");
		expectingNoProblems();
	}

	//
	// /*
	// * Ensures that a task tag is not user editable
	// * (regression test for bug 123721 two types of 'remove' for TODO task
	// tags)
	// */
	// public void testTags3() throws CoreException {
	// Hashtable options = JavaCore.getOptions();
	//
	// try {
	// Hashtable newOptions = JavaCore.getOptions();
	//			newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX"); //$NON-NLS-1$
	//			newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW"); //$NON-NLS-1$
	//
	// JavaCore.setOptions(newOptions);
	//
	//			IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	// env.addExternalJars(projectPath, Util.getJavaClassLibs());
	//
	// // remove old package fragment root so that names don't collide
	//			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	//
	//			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	//			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	//
	//			IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
	//				"package p; \n"+ //$NON-NLS-1$
	//				"// TODO need to review\n" + //$NON-NLS-1$
	//				"public class A {\n" + //$NON-NLS-1$
	// "}");
	//
	// fullBuild(projectPath);
	// IMarker[] markers = env.getTaskMarkersFor(pathToA);
	// assertEquals("Marker should not be editable", Boolean.FALSE,
	// markers[0].getAttribute(IMarker.USER_EDITABLE));
	// } finally {
	// JavaCore.setOptions(options);
	// }
	// }
	//
	// /*
	// * http://bugs.eclipse.org/bugs/show_bug.cgi?id=92821
	// */
	// public void testUnusedImport() throws Exception {
	// Hashtable options = JavaCore.getOptions();
	// Hashtable newOptions = JavaCore.getOptions();
	// newOptions.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.WARNING);
	//
	// JavaCore.setOptions(newOptions);
	//
	//		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	// env.addExternalJars(projectPath, Util.getJavaClassLibs());
	//
	// // remove old package fragment root so that names don't collide
	//		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	//
	//		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	//		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	//
	//		env.addClass(root, "util", "MyException", //$NON-NLS-1$ //$NON-NLS-2$
	// "package util;\n" +
	// "public class MyException extends Exception {\n" +
	// "	private static final long serialVersionUID = 1L;\n" +
	// "}"
	//		); //$NON-NLS-1$
	//
	//		env.addClass(root, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
	// "package p;\n" +
	// "import util.MyException;\n" +
	// "public class Test {\n" +
	// "	/**\n" +
	// "	 * @throws MyException\n" +
	// "	 */\n" +
	// "	public void bar() {\n" +
	// "	}\n" +
	// "}"
	// );
	//
	// fullBuild(projectPath);
	// expectingNoProblems();
	//
	// JavaCore.setOptions(options);
	// }
	//
	// /*
	// * http://bugs.eclipse.org/bugs/show_bug.cgi?id=98667
	// */
	// public void test98667() throws Exception {
	//		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	// env.addExternalJars(projectPath, Util.getJavaClassLibs());
	//
	// // remove old package fragment root so that names don't collide
	//		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	//
	//		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	//		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	//
	//		env.addClass(root, "p1", "Aaa$Bbb$Ccc", //$NON-NLS-1$ //$NON-NLS-2$
	//			"package p1;\n" + //$NON-NLS-1$ 
	//			"\n" +  //$NON-NLS-1$
	//			"public class Aaa$Bbb$Ccc {\n" + //$NON-NLS-1$ 
	//			"}" //$NON-NLS-1$
	// );
	//
	// fullBuild(projectPath);
	// expectingNoProblems();
	// }
	//
	// /**
	// * @bug 164707: ArrayIndexOutOfBoundsException in JavaModelManager if
	// source level == 6.0
	// * @test Ensure that AIIOB does not longer happen with invalid source
	// level string
	// * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=164707"
	// */
	// public void testBug164707() throws Exception {
	//		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	// IJavaProject javaProject = env.getJavaProject(projectPath);
	// javaProject.setOption(JavaCore.COMPILER_SOURCE, "invalid");
	// env.addExternalJars(projectPath, Util.getJavaClassLibs());
	// fullBuild(projectPath);
	// expectingNoProblems();
	// }
	//
	// /**
	// * @bug 75471: [prefs] no re-compile when loading settings
	// * @test Ensure that changing project preferences is well taking into
	// account while rebuilding project
	// * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=75471"
	// */
	// public void _testUpdateProjectPreferences() throws Exception {
	//
	//		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	// env.addExternalJars(projectPath, Util.getJavaClassLibs());
	//
	// // remove old package fragment root so that names don't collide
	//		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	//
	//		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	//		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	//
	//		env.addClass(root, "util", "MyException", //$NON-NLS-1$ //$NON-NLS-2$
	// "package util;\n" +
	// "public class MyException extends Exception {\n" +
	// "	private static final long serialVersionUID = 1L;\n" +
	// "}"
	//		); //$NON-NLS-1$
	//
	//		IPath cuPath = env.addClass(root, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
	// "package p;\n" +
	// "import util.MyException;\n" +
	// "public class Test {\n" +
	// "}"
	// );
	//
	// fullBuild(projectPath);
	// expectingSpecificProblemFor(
	// projectPath,
	//			new Problem("", "The import util.MyException is never used", cuPath, 18, 34, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$ //$NON-NLS-2$
	//
	// IJavaProject project = env.getJavaProject(projectPath);
	// project.setOption(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.IGNORE);
	// incrementalBuild(projectPath);
	// expectingNoProblems();
	// }
	// public void _testUpdateWkspPreferences() throws Exception {
	//
	//		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	// env.addExternalJars(projectPath, Util.getJavaClassLibs());
	//
	// // remove old package fragment root so that names don't collide
	//		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	//
	//		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	//		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	//
	//		env.addClass(root, "util", "MyException", //$NON-NLS-1$ //$NON-NLS-2$
	// "package util;\n" +
	// "public class MyException extends Exception {\n" +
	// "	private static final long serialVersionUID = 1L;\n" +
	// "}"
	//		); //$NON-NLS-1$
	//
	//		IPath cuPath = env.addClass(root, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
	// "package p;\n" +
	// "import util.MyException;\n" +
	// "public class Test {\n" +
	// "}"
	// );
	//
	// fullBuild();
	// expectingSpecificProblemFor(
	// projectPath,
	//			new Problem("", "The import util.MyException is never used", cuPath, 18, 34, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$ //$NON-NLS-2$
	//
	// // Save preference
	// JavaModelManager manager = JavaModelManager.getJavaModelManager();
	// IEclipsePreferences preferences = manager.getInstancePreferences();
	// String unusedImport = preferences.get(JavaCore.COMPILER_PB_UNUSED_IMPORT,
	// null);
	// try {
	// // Modify preference
	// preferences.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.IGNORE);
	// incrementalBuild();
	// expectingNoProblems();
	// }
	// finally {
	// if (unusedImport == null) {
	// preferences.remove(JavaCore.COMPILER_PB_UNUSED_IMPORT);
	// } else {
	// preferences.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, unusedImport);
	// }
	// }
	// }
	//
	// public void testTags4() throws Exception {
	// Hashtable options = JavaCore.getOptions();
	// Hashtable newOptions = JavaCore.getOptions();
	//		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO!,TODO,TODO?"); //$NON-NLS-1$
	//		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "HIGH,NORMAL,LOW"); //$NON-NLS-1$
	//
	// JavaCore.setOptions(newOptions);
	//
	//		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	// env.addExternalJars(projectPath, Util.getJavaClassLibs());
	//
	// // remove old package fragment root so that names don't collide
	//		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	//
	//		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	//		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	//
	//		IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
	//			"package p; \n"+ //$NON-NLS-1$
	//			"// TODO! TODO? need to review the loop\n" + //$NON-NLS-1$
	//			"public class A {\n" + //$NON-NLS-1$
	// "}");
	//
	// fullBuild(projectPath);
	// IMarker[] markers = env.getTaskMarkersFor(pathToA);
	// assertEquals("Wrong size", 2, markers.length);
	//
	// try {
	// IMarker marker = markers[1];
	// Object priority = marker.getAttribute(IMarker.PRIORITY);
	// String message = (String) marker.getAttribute(IMarker.MESSAGE);
	// assertEquals("Wrong message", "TODO? need to review the loop", message);
	// assertNotNull("No task priority", priority);
	// assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_LOW),
	// priority);
	//
	// marker = markers[0];
	// priority = marker.getAttribute(IMarker.PRIORITY);
	// message = (String) marker.getAttribute(IMarker.MESSAGE);
	// assertEquals("Wrong message", "TODO! need to review the loop", message);
	// assertNotNull("No task priority", priority);
	// assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_HIGH),
	// priority);
	// } catch (CoreException e) {
	// assertTrue(false);
	// }
	// JavaCore.setOptions(options);
	// }

	// When a groovy file name clashes with an existing type
	// public void testBuildClash() throws Exception {
	//		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	// env.addExternalJars(projectPath, Util.getJavaClassLibs());
	// env.addGroovyJars(projectPath);
	// fullBuild(projectPath);
	//
	// // remove old package fragment root so that names don't collide
	//		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	//
	//		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	//		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	//
	// env.addGroovyClass(root, "", "Stack",
	// "class StackTester {\n"+
	// "   def o = new Stack();\n"+
	// "   public static void main(String[] args) {\n"+
	// "      System.out.println('>>'+new StackTester().o.getClass());\n"+
	// "      System.out.println(\"Hello world\");\n"+
	// "   }\n"+
	// "}\n"
	// );
	//
	// incrementalBuild(projectPath);
	// expectingCompiledClassesV("StackTester");
	// expectingNoProblems();
	// executeClass(projectPath, "StackTester", ">>class java.util.Stack\r\n" +
	// "Hello world\r\n", "");
	//
	//
	// env.addGroovyClass(root, "", "Stack",
	// "class StackTester {\n"+
	// "   def o = new Stack();\n"+
	// "   public static void main(String[] args) {\n"+
	// "      System.out.println('>>'+new StackTester().o.getClass());\n"+
	// "      System.out.println(\"Hello world\");\n"+
	// "   }\n"+
	// "}\n"
	// );
	//
	// incrementalBuild(projectPath);
	// expectingCompiledClassesV("StackTester");
	// expectingNoProblems();
	// executeClass(projectPath, "StackTester", ">>class java.util.Stack\r\n" +
	// "Hello world\r\n", "");
	// }

}
