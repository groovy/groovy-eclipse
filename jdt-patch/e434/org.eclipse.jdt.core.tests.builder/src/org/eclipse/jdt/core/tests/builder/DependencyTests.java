/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.Test;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DependencyTests extends BuilderTests {
	static {
//		TESTS_NAMES = new String[] {"testMissingClassFile"};
	}
	public DependencyTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(DependencyTests.class);
	}

	public void testAbstractMethod() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Indicted", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public abstract class Indicted {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		IPath collaboratorPath =  env.addClass(root, "p2", "Collaborator", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Collaborator extends Indicted{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "Indicted", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public abstract class Indicted {\n"+ //$NON-NLS-1$
			"   public abstract void foo();\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(collaboratorPath);
		expectingOnlySpecificProblemFor(collaboratorPath, new Problem("Collaborator", "The type Collaborator must implement the inherited abstract method Indicted.foo()", collaboratorPath, 38, 50, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=168208
	public void testCaseInvariantType() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		org.eclipse.jdt.core.IJavaProject p = env.getJavaProject("Project");
		java.util.Map options = p.getOptions(true);
		options.put(org.eclipse.jdt.core.JavaCore.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER, org.eclipse.jdt.core.JavaCore.DISABLED);
		p.setOptions(options);

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n" +
			"	class Node {}\n" +
			"}" //$NON-NLS-1$
		);

		env.addClass(root, "p1", "Bb", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"class Bb {}" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n" +
			"	class node {}\n" +
			"}" //$NON-NLS-1$
		);

		env.addClass(root, "p1", "Bb", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"class BB {}" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testExactMethodDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i(int i) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	int j = i(1);\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath dPath =  env.addClass(root, "p3", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class D extends p2.B{\n"+ //$NON-NLS-1$
			"	public class M {\n"+ //$NON-NLS-1$
			"		int j = i(1);\n"+ //$NON-NLS-1$
			"	}\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C c) { return c.i(1); }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, dPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The method i(int) is undefined for the type C", cPath, 50, 51, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(dPath, new Problem("D", "The method i(int) is undefined for the type D.M", dPath, 69, 70, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i(int) is undefined for the type C", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"	protected int i(long l) throws Exception {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, dPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "Default constructor cannot handle exception type Exception thrown by implicit super constructor. Must define an explicit constructor", cPath, 50, 54, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(dPath, new Problem("D", "Default constructor cannot handle exception type Exception thrown by implicit super constructor. Must define an explicit constructor", dPath, 69, 73, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i(long) from the type B is not visible", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i(int i) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testExactMethodVisibility() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i() {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	int j = i();\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath dPath =  env.addClass(root, "p3", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class D extends p2.B{\n"+ //$NON-NLS-1$
			"	public class M {\n"+ //$NON-NLS-1$
			"		int j = i();\n"+ //$NON-NLS-1$
			"	}\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C c) { return c.i(); }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	int i() {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, dPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The method i() from the type A is not visible", cPath, 50, 51, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(dPath, new Problem("D", "The method i() from the type A is not visible", dPath, 69, 70, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i() from the type A is not visible", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	protected int i() {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {xPath});
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i() from the type A is not visible", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i() {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testExternalJarChanged() throws CoreException, java.io.IOException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		IPath root = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		IPath classTest = env.addClass(root, "p", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n"+ //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"  void foo() {\n" + //$NON-NLS-1$
			"    new q.Y().bar();\n" + //$NON-NLS-1$
			"  }\n" + //$NON-NLS-1$
			"}" //$NON-NLS-1$
		);
		String externalJar = Util.getOutputDirectory() + java.io.File.separator + "test.jar"; //$NON-NLS-1$
		Util.createJar(
			new String[] {
				"q/Y.java", //$NON-NLS-1$
				"package q;\n" + //$NON-NLS-1$
				"public class Y {\n" + //$NON-NLS-1$
				"}" //$NON-NLS-1$
			},
			new java.util.HashMap(),
			externalJar
		);
		env.addExternalJar(projectPath, externalJar);

		// build -> expecting problems
		fullBuild();
		expectingProblemsFor(
			classTest,
			"Problem : The method bar() is undefined for the type Y [ resource : </Project/p/X.java> range : <57,60> category : <50> severity : <2>]"
		);

		// fix jar
		Util.createJar(
			new String[] {
				"q/Y.java", //$NON-NLS-1$
				"package q;\n" + //$NON-NLS-1$
				"public class Y {\n" + //$NON-NLS-1$
				"  public void bar() {\n" + //$NON-NLS-1$
				"  }\n" + //$NON-NLS-1$
				"}" //$NON-NLS-1$
			},
			new java.util.HashMap(),
			externalJar
		);
		// add new class to trigger an incremental build
		env.getProject(projectPath).touch(null);

		// incremental build should notice jar file has changed & do a full build
		incrementalBuild();
		expectingNoProblems();
	}

	public void testFieldDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	int j = i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C c) { return c.i; }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "i cannot be resolved to a variable", cPath, 50, 51, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "i cannot be resolved or is not a field", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testFieldVisibility() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	int j = i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C c) { return c.i; }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	int i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The field A.i is not visible", cPath, 50, 51, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "The field A.i is not visible", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	protected int i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {xPath});
		expectingSpecificProblemFor(xPath, new Problem("X", "The field A.i is not visible", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	// 77272
	public void testInterfaceDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Vehicle", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public interface Vehicle {}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p1", "Car", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public interface Car extends Vehicle {}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p1", "CarImpl", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class CarImpl implements Car {}\n" //$NON-NLS-1$
		);

		IPath testPath = env.addClass(root, "p1", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Test { public Vehicle createVehicle() { return new CarImpl(); } }\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "Car", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public interface Car {}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(testPath);
		expectingSpecificProblemFor(testPath, new Problem("Test", "Type mismatch: cannot convert from CarImpl to Vehicle", testPath, 72, 85, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "Car", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public interface Car extends Vehicle {}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMemberTypeDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public class M { public int i; };\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	M m;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C.M m) { return m.i; }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "M cannot be resolved to a type", cPath, 42, 43, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "p3.C.M cannot be resolved to a type", xPath, 38, 44, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public class M { public int i; };\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMemberTypeVisibility() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public class M { public int i; };\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	M m;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C.M m) { return m.i; }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	class M { public int i; };\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The type M is not visible", cPath, 42, 43, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "The type p3.C.M is not visible", xPath, 38, 44, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	protected class M { public int i; };\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {xPath});
		expectingSpecificProblemFor(xPath, new Problem("X", "The type p3.C.M is not visible", xPath, 38, 44, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public class M { public int i; };\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMethodDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i(A a) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	int j = i(this);\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath dPath =  env.addClass(root, "p3", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class D extends p2.B{\n"+ //$NON-NLS-1$
			"	public class M {\n"+ //$NON-NLS-1$
			"		int j = i(new D());\n"+ //$NON-NLS-1$
			"	}\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C c) { return c.i(c); }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, dPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The method i(C) is undefined for the type C", cPath, 50, 51, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(dPath, new Problem("D", "The method i(D) is undefined for the type D.M", dPath, 69, 70, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i(C) is undefined for the type C", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"	public int i(B b) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i(A a) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMethodVisibility() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i(A a) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	int j = i(this);\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath dPath =  env.addClass(root, "p3", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class D extends p2.B{\n"+ //$NON-NLS-1$
			"	public class M {\n"+ //$NON-NLS-1$
			"		int j = i(new D());\n"+ //$NON-NLS-1$
			"	}\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C c) { return c.i(c); }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	int i(A a) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, dPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The method i(A) from the type A is not visible", cPath, 50, 51, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(dPath, new Problem("D", "The method i(A) from the type A is not visible", dPath, 69, 70, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i(A) from the type A is not visible", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"	protected int i(B b) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {xPath});
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i(B) from the type B is not visible", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i(A a) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMissingClassFile() throws JavaModelException {
		IPath project1Path = env.addProject("Project1", "1.8"); // tolerance of missing types not fully implemented below 1.8
		env.addExternalJars(project1Path, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project1Path,""); //$NON-NLS-1$

		IPath root1 = env.addPackageFragmentRoot(project1Path, "src"); //$NON-NLS-1$
		env.setOutputFolder(project1Path, "bin"); //$NON-NLS-1$

		env.addClass(root1, "p1", "MissingClass", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class MissingClass {}" //$NON-NLS-1$
		);

		IPath project2Path = env.addProject("Project2", "1.8"); // tolerance of missing types not fully implemented below 1.8
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project2Path,""); //$NON-NLS-1$

		IPath root2 = env.addPackageFragmentRoot(project2Path, "src"); //$NON-NLS-1$
		env.setOutputFolder(project2Path, "bin"); //$NON-NLS-1$

		env.addClass(root2, "p2", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.MissingClass;\n" +
			"public class A {\n"+ //$NON-NLS-1$
			"	public void foo(MissingClass data) {}\n"+ //$NON-NLS-1$
			"	public void foo(String data) {}\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath project3Path = env.addProject("Project3", "1.8"); // tolerance of missing types not fully implemented below 1.8
		env.addExternalJars(project3Path, Util.getJavaClassLibs());
		env.addRequiredProject(project3Path, project2Path);
		// missing required Project1 so MissingClass cannot be found

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project3Path,""); //$NON-NLS-1$

		IPath root3 = env.addPackageFragmentRoot(project3Path, "src"); //$NON-NLS-1$
		env.setOutputFolder(project3Path, "bin"); //$NON-NLS-1$

		IPath bPath = env.addClass(root3, "p3", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"import p2.A;\n" +
			"public class B {\n"+ //$NON-NLS-1$
			"	public static void main(String[] args) {\n" + //$NON-NLS-1$
			"		new A().foo(new B());\n" + 		// applicability test would like to see MissingClass
			"		new A().foo(new String());\n" + // exact match to fully resolved method
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild();
		expectingOnlyProblemsFor(new IPath[] {bPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "The method foo(MissingClass) from the type A refers to the missing type MissingClass", bPath, 94, 97, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root2, "p2", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public void foo(Object data) {}\n"+ //$NON-NLS-1$
			"	public void foo(String data) {}\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild();
		expectingNoProblems();
	}

	// 181269
	public void testSecondaryTypeDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A extends Secondary {}\n"+ //$NON-NLS-1$
			"class Secondary {}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		IPath typePath = env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A extends Secondary {}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingSpecificProblemFor(typePath, new Problem("A", "Secondary cannot be resolved to a type", typePath, 35, 44, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	// 72468
	public void testTypeDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {}\n" //$NON-NLS-1$
		);

		IPath bPath = env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class B extends p1.A{}\n" //$NON-NLS-1$
		);

		IPath cPath = env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"class Deleted {}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {bPath, cPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "p1.A cannot be resolved to a type", bPath, 35, 39, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(cPath, new Problem("C", "The hierarchy of the type C is inconsistent", cPath, 25, 26, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class B {}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	// 72468
	public void testTypeVisibility() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {}\n" //$NON-NLS-1$
		);

		IPath bPath = env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class B extends p1.A{}\n" //$NON-NLS-1$
		);

		IPath cPath = env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"class A {}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {bPath, cPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "The type p1.A is not visible", bPath, 35, 39, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(cPath, new Problem("C", "The hierarchy of the type C is inconsistent", cPath, 25, 26, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class B {}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class B extends p1.A{}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {bPath, cPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "The type p1.A is not visible", bPath, 35, 39, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(cPath, new Problem("C", "The hierarchy of the type C is inconsistent", cPath, 25, 26, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	// 79163
	public void testTypeVisibility2() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath aPath = env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n" +  //$NON-NLS-1$
			"	void foo() { p2.FooFactory.createFoo().foo(); }\n" +  //$NON-NLS-1$
			"	void foos() { p2.FooFactory.createFoos().clone(); }\n" +  //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		// Foo & Foos are not public to get visibility problems
		env.addClass(root, "p2", "Foo", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"class Foo { public void foo() {} }\n" //$NON-NLS-1$
		);
		env.addClass(root, "p2", "Foos", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"class Foos {}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "FooFactory", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class FooFactory {\n" +  //$NON-NLS-1$
			"	public static Foo createFoo() { return null; }\n" +  //$NON-NLS-1$
			"	public static Foos[] createFoos() { return null; }\n" +  //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {aPath});
		expectingSpecificProblemFor(aPath, new Problem("A", "The type Foo is not visible", aPath, 43, 68, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(aPath, new Problem("A", "The type Foos is not visible", aPath, 93, 119, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "Foo", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Foo { public void foo() {} }\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {aPath});
		expectingSpecificProblemFor(aPath, new Problem("A", "The type Foos is not visible", aPath, 93, 119, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "Foos", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Foos { }\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p2", "Foo", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"class Foo { public void foo() {} }\n" //$NON-NLS-1$
		);
		env.addClass(root, "p2", "Foos", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"class Foos {}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {aPath});
		expectingSpecificProblemFor(aPath, new Problem("A", "The type Foo is not visible", aPath, 43, 68, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(aPath, new Problem("A", "The type Foos is not visible", aPath, 93, 119, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testTypeVariable() throws JavaModelException {
		IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A<T> {}\n" //$NON-NLS-1$
		);

		IPath bPath = env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class B<T> extends p1.A<T> {}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		IPath aPath = env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {bPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "The type A is not generic; it cannot be parameterized with arguments <T>", bPath, 38, 42, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A<T extends Comparable> {}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {aPath, bPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "Bound mismatch: The type T is not a valid substitute for the bounded parameter <T extends Comparable> of the type A<T>", bPath, 43, 44, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(aPath, new Problem("A", "Comparable is a raw type. References to generic type Comparable<T> should be parameterized", aPath, 37, 47, CategorizedProblem.CAT_UNCHECKED_RAW, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A<T> {}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159709
// Full build and incremental build behave differently for deprecation
// warnings, which is unexpected. Guard test for DeprecatedTest#test015 (the
// builder is not the cause of the bug, but we want to ensure that the end to
// end behavior is OK).
public void test0100() throws JavaModelException {
	IPath projectPath = env.addProject("P");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	IPath rootPath = env.getPackageFragmentRootPath(projectPath, "");
	env.addClass(rootPath, "a", "N1",
		"package a;\n" +
		"public class N1 {\n" +
		"  public void foo() {}\n" +
		"  /** @deprecated */\n" +
		"  public class N2 {" +
		"    public void foo() {}" +
		"    public class N3 {" +
		"      public void foo() {}" +
		"    }" +
		"  }" +
		"  void bar() {}\n" +
		"}\n"
	);
	String M1Contents =
		"package p;\n" +
		"public class M1 {\n" +
		"  public void foo() {}\n" +
		"  /** @deprecated */\n" +
		"  public class M2 {" +
		"    public void foo() {}" +
		"    public class M3 {" +
		"      public void foo() {}" +
		"    }" +
		"  }" +
		"  void bar() {\n" +
		"    a.N1.N2.N3 m = null;\n" +
		"    m.foo();\n" +
		"  }\n" +
		"}\n";
	IPath M1Path = env.addClass(rootPath, "p", "M1", M1Contents);
	fullBuild(projectPath);
	expectingOnlyProblemsFor(new IPath[] {M1Path});
	expectingSpecificProblemFor(M1Path,
		new Problem("", "The type N1.N2.N3 is deprecated",
			M1Path, 198, 200, CategorizedProblem.CAT_DEPRECATION, IMarker.SEVERITY_WARNING));
	expectingSpecificProblemFor(M1Path,
		new Problem("",	"The method foo() from the type N1.N2.N3 is deprecated",
			M1Path, 217, 222, CategorizedProblem.CAT_DEPRECATION, IMarker.SEVERITY_WARNING));
	M1Path = env.addClass(rootPath, "p", "M1", M1Contents);
	incrementalBuild(projectPath);
	expectingOnlyProblemsFor(new IPath[] {M1Path});
	expectingSpecificProblemFor(M1Path,
		new Problem("", "The type N1.N2.N3 is deprecated",
			M1Path, 198, 200, CategorizedProblem.CAT_DEPRECATION, IMarker.SEVERITY_WARNING));
	expectingSpecificProblemFor(M1Path,
		new Problem("",	"The method foo() from the type N1.N2.N3 is deprecated",
			M1Path, 217, 222, CategorizedProblem.CAT_DEPRECATION, IMarker.SEVERITY_WARNING));
}
}
