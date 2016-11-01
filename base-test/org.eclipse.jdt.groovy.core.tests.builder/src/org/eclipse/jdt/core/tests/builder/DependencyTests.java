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
package org.eclipse.jdt.core.tests.builder;

import java.util.Collections;
import java.util.Map;

import junit.framework.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;

public class DependencyTests extends BuilderTests {
	public DependencyTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(DependencyTests.class);
	}

	public void testAbstractMethod() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "Indicted",
			"package p1;\n"+
			"public abstract class Indicted {\n"+
			"}\n"
			);

		IPath collaboratorPath =  env.addClass(root, "p2", "Collaborator",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Collaborator extends Indicted{\n"+
			"}\n"
			);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "Indicted",
			"package p1;\n"+
			"public abstract class Indicted {\n"+
			"   public abstract void foo();\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(collaboratorPath);
		expectingOnlySpecificProblemFor(collaboratorPath, new Problem("Collaborator", "The type Collaborator must implement the inherited abstract method Indicted.foo()", collaboratorPath, 38, 50, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=168208
	public void testCaseInvariantType() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		org.eclipse.jdt.core.IJavaProject p = env.getJavaProject("Project");
		Map<String, String> options = p.getOptions(true);
		options.put(org.eclipse.jdt.core.JavaCore.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER, org.eclipse.jdt.core.JavaCore.DISABLED);
		p.setOptions(options);

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n" +
			"	class Node {}\n" +
			"}"
		);

		env.addClass(root, "p1", "Bb",
			"package p1;\n"+
			"class Bb {}"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n" +
			"	class node {}\n" +
			"}"
		);

		env.addClass(root, "p1", "Bb",
			"package p1;\n"+
			"class BB {}"
		);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testExactMethodDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public int i(int i) {return 1;};\n"+
			"}\n"
		);

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class B extends A{\n"+
			"}\n"
		);

		IPath cPath =  env.addClass(root, "p3", "C",
			"package p3;\n"+
			"public class C extends p2.B{\n"+
			"	int j = i(1);\n"+
			"}\n"
		);

		IPath dPath =  env.addClass(root, "p3", "D",
			"package p3;\n"+
			"public class D extends p2.B{\n"+
			"	public class M {\n"+
			"		int j = i(1);\n"+
			"	}\n"+
			"}\n"
		);

		IPath xPath =  env.addClass(root, "p4", "X",
			"package p4;\n"+
			"public class X {\n"+
			"	int foo(p3.C c) { return c.i(1); }\n"+
			"}\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, dPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The method i(int) is undefined for the type C", cPath, 50, 51, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(dPath, new Problem("D", "The method i(int) is undefined for the type D.M", dPath, 69, 70, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i(int) is undefined for the type C", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class B extends A{\n"+
			"	protected int i(long l) throws Exception {return 1;};\n"+
			"}\n"
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, dPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "Default constructor cannot handle exception type Exception thrown by implicit super constructor. Must define an explicit constructor", cPath, 50, 54, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(dPath, new Problem("D", "Default constructor cannot handle exception type Exception thrown by implicit super constructor. Must define an explicit constructor", dPath, 69, 73, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i(long) from the type B is not visible", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public int i(int i) {return 1;};\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testExactMethodVisibility() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public int i() {return 1;};\n"+
			"}\n"
		);

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class B extends A{\n"+
			"}\n"
		);

		IPath cPath =  env.addClass(root, "p3", "C",
			"package p3;\n"+
			"public class C extends p2.B{\n"+
			"	int j = i();\n"+
			"}\n"
		);

		IPath dPath =  env.addClass(root, "p3", "D",
			"package p3;\n"+
			"public class D extends p2.B{\n"+
			"	public class M {\n"+
			"		int j = i();\n"+
			"	}\n"+
			"}\n"
		);

		IPath xPath =  env.addClass(root, "p4", "X",
			"package p4;\n"+
			"public class X {\n"+
			"	int foo(p3.C c) { return c.i(); }\n"+
			"}\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	int i() {return 1;};\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, dPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The method i() from the type A is not visible", cPath, 50, 51, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(dPath, new Problem("D", "The method i() from the type A is not visible", dPath, 69, 70, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i() from the type A is not visible", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	protected int i() {return 1;};\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {xPath});
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i() from the type A is not visible", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public int i() {return 1;};\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testExternalJarChanged() throws CoreException, java.io.IOException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		IPath root = env.getPackageFragmentRootPath(projectPath, "");
		IPath classTest = env.addClass(root, "p", "X",
			"package p;\n"+
			"public class X {\n" +
			"  void foo() {\n" +
			"    new q.Y().bar();\n" +
			"  }\n" +
			"}"
		);
		String externalJar = Util.getOutputDirectory() + java.io.File.separator + "test.jar";
		Util.createJar(
			new String[] {
				"q/Y.java",
				"package q;\n" +
				"public class Y {\n" +
				"}"
			},
			Collections.<String, String>emptyMap(),
			externalJar
		);
		long lastModified = new java.io.File(externalJar).lastModified();
		env.addExternalJar(projectPath, externalJar);

		// build -> expecting problems
		fullBuild();
		expectingProblemsFor(
			classTest,
			"Problem : The method bar() is undefined for the type Y [ resource : </Project/p/X.java> range : <57,60> category : <50> severity : <2>]"
		);

		try {
			Thread.sleep(1000);
		} catch(InterruptedException e) {
		}
		// fix jar
		Util.createJar(
			new String[] {
				"q/Y.java",
				"package q;\n" +
				"public class Y {\n" +
				"  public void bar() {\n" +
				"  }\n" +
				"}"
			},
			Collections.<String, String>emptyMap(),
			externalJar
		);
		new java.io.File(externalJar).setLastModified(lastModified + 1000); // to be sure its different
		// add new class to trigger an incremental build
		env.getProject(projectPath).touch(null);

		// incremental build should notice jar file has changed & do a full build
		incrementalBuild();
		expectingNoProblems();
	}

	public void testFieldDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public int i;\n"+
			"}\n"
		);

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class B extends A{\n"+
			"}\n"
		);

		IPath cPath =  env.addClass(root, "p3", "C",
			"package p3;\n"+
			"public class C extends p2.B{\n"+
			"	int j = i;\n"+
			"}\n"
		);

		IPath xPath =  env.addClass(root, "p4", "X",
			"package p4;\n"+
			"public class X {\n"+
			"	int foo(p3.C c) { return c.i; }\n"+
			"}\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "i cannot be resolved", cPath, 50, 51, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(xPath, new Problem("X", "c.i cannot be resolved or is not a field", xPath, 55, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public int i;\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testFieldVisibility() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public int i;\n"+
			"}\n"
		);

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class B extends A{\n"+
			"}\n"
		);

		IPath cPath =  env.addClass(root, "p3", "C",
			"package p3;\n"+
			"public class C extends p2.B{\n"+
			"	int j = i;\n"+
			"}\n"
		);

		IPath xPath =  env.addClass(root, "p4", "X",
			"package p4;\n"+
			"public class X {\n"+
			"	int foo(p3.C c) { return c.i; }\n"+
			"}\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	int i;\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The field A.i is not visible", cPath, 50, 51, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(xPath, new Problem("X", "The field A.i is not visible", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	protected int i;\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {xPath});
		expectingSpecificProblemFor(xPath, new Problem("X", "The field A.i is not visible", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public int i;\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	// 77272
	public void testInterfaceDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "Vehicle",
			"package p1;\n"+
			"public interface Vehicle {}\n"
		);

		env.addClass(root, "p1", "Car",
			"package p1;\n"+
			"public interface Car extends Vehicle {}\n"
		);

		env.addClass(root, "p1", "CarImpl",
			"package p1;\n"+
			"public class CarImpl implements Car {}\n"
		);

		IPath testPath = env.addClass(root, "p1", "Test",
			"package p1;\n"+
			"public class Test { public Vehicle createVehicle() { return new CarImpl(); } }\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "Car",
			"package p1;\n"+
			"public interface Car {}\n"
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(testPath);
		expectingSpecificProblemFor(testPath, new Problem("Test", "Type mismatch: cannot convert from CarImpl to Vehicle", testPath, 72, 85, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p1", "Car",
			"package p1;\n"+
			"public interface Car extends Vehicle {}\n"
		);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMemberTypeDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public class M { public int i; };\n"+
			"}\n"
		);

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class B extends A{\n"+
			"}\n"
		);

		IPath cPath =  env.addClass(root, "p3", "C",
			"package p3;\n"+
			"public class C extends p2.B{\n"+
			"	M m;\n"+
			"}\n"
		);

		IPath xPath =  env.addClass(root, "p4", "X",
			"package p4;\n"+
			"public class X {\n"+
			"	int foo(p3.C.M m) { return m.i; }\n"+
			"}\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "M cannot be resolved to a type", cPath, 42, 43, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(xPath, new Problem("X", "p3.C.M cannot be resolved to a type", xPath, 38, 44, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public class M { public int i; };\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMemberTypeVisibility() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public class M { public int i; };\n"+
			"}\n"
		);

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class B extends A{\n"+
			"}\n"
		);

		IPath cPath =  env.addClass(root, "p3", "C",
			"package p3;\n"+
			"public class C extends p2.B{\n"+
			"	M m;\n"+
			"}\n"
		);

		IPath xPath =  env.addClass(root, "p4", "X",
			"package p4;\n"+
			"public class X {\n"+
			"	int foo(p3.C.M m) { return m.i; }\n"+
			"}\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	class M { public int i; };\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The type M is not visible", cPath, 42, 43, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(xPath, new Problem("X", "The type p3.C.M is not visible", xPath, 38, 44, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	protected class M { public int i; };\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {xPath});
		expectingSpecificProblemFor(xPath, new Problem("X", "The type p3.C.M is not visible", xPath, 38, 44, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public class M { public int i; };\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMethodDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public int i(A a) {return 1;};\n"+
			"}\n"
		);

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class B extends A{\n"+
			"}\n"
		);

		IPath cPath =  env.addClass(root, "p3", "C",
			"package p3;\n"+
			"public class C extends p2.B{\n"+
			"	int j = i(this);\n"+
			"}\n"
		);

		IPath dPath =  env.addClass(root, "p3", "D",
			"package p3;\n"+
			"public class D extends p2.B{\n"+
			"	public class M {\n"+
			"		int j = i(new D());\n"+
			"	}\n"+
			"}\n"
		);

		IPath xPath =  env.addClass(root, "p4", "X",
			"package p4;\n"+
			"public class X {\n"+
			"	int foo(p3.C c) { return c.i(c); }\n"+
			"}\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, dPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The method i(C) is undefined for the type C", cPath, 50, 51, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(dPath, new Problem("D", "The method i(D) is undefined for the type D.M", dPath, 69, 70, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i(C) is undefined for the type C", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class B extends A{\n"+
			"	public int i(B b) {return 1;};\n"+
			"}\n"
		);

		incrementalBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public int i(A a) {return 1;};\n"+
			"}\n"
			);

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class B extends A{\n"+
			"}\n"
		);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMethodVisibility() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public int i(A a) {return 1;};\n"+
			"}\n"
		);

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class B extends A{\n"+
			"}\n"
		);

		IPath cPath =  env.addClass(root, "p3", "C",
			"package p3;\n"+
			"public class C extends p2.B{\n"+
			"	int j = i(this);\n"+
			"}\n"
		);

		IPath dPath =  env.addClass(root, "p3", "D",
			"package p3;\n"+
			"public class D extends p2.B{\n"+
			"	public class M {\n"+
			"		int j = i(new D());\n"+
			"	}\n"+
			"}\n"
		);

		IPath xPath =  env.addClass(root, "p4", "X",
			"package p4;\n"+
			"public class X {\n"+
			"	int foo(p3.C c) { return c.i(c); }\n"+
			"}\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	int i(A a) {return 1;};\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, dPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The method i(A) from the type A is not visible", cPath, 50, 51, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(dPath, new Problem("D", "The method i(A) from the type A is not visible", dPath, 69, 70, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i(A) from the type A is not visible", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class B extends A{\n"+
			"	protected int i(B b) {return 1;};\n"+
			"}\n"
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {xPath});
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i(B) from the type B is not visible", xPath, 57, 58, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n"+
			"	public int i(A a) {return 1;};\n"+
			"}\n"
			);

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class B extends A{\n"+
			"}\n"
		);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMissingClassFile() throws JavaModelException {
		IPath project1Path = env.addProject("Project1");
		env.addExternalJars(project1Path, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project1Path,"");

		IPath root1 = env.addPackageFragmentRoot(project1Path, "src");
		env.setOutputFolder(project1Path, "bin");

		env.addClass(root1, "p1", "MissingClass",
			"package p1;\n"+
			"public class MissingClass {}"
		);

		IPath project2Path = env.addProject("Project2");
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project2Path,"");

		IPath root2 = env.addPackageFragmentRoot(project2Path, "src");
		env.setOutputFolder(project2Path, "bin");

		env.addClass(root2, "p2", "A",
			"package p2;\n"+
			"import p1.MissingClass;\n" +
			"public class A {\n"+
			"	public void foo(MissingClass data) {}\n"+
			"	public void foo(String data) {}\n"+
			"}\n"
		);

		IPath project3Path = env.addProject("Project3");
		env.addExternalJars(project3Path, Util.getJavaClassLibs());
		env.addRequiredProject(project3Path, project2Path);
		// missing required Project1 so MissingClass cannot be found

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project3Path,"");

		IPath root3 = env.addPackageFragmentRoot(project3Path, "src");
		env.setOutputFolder(project3Path, "bin");

		IPath bPath = env.addClass(root3, "p3", "B",
			"package p3;\n"+
			"import p2.A;\n" +
			"public class B {\n"+
			"	public static void main(String[] args) {\n" +
			"		new A().foo(new String());\n" +
			"	}\n" +
			"}\n"
		);

		fullBuild();
		expectingOnlyProblemsFor(new IPath[] {project3Path, bPath});
		expectingSpecificProblemFor(project3Path, new Problem("Project3", "The project was not built since its build path is incomplete. Cannot find the class file for p1.MissingClass. Fix the build path then try building this project", project3Path, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(bPath, new Problem("B", "The type p1.MissingClass cannot be resolved. It is indirectly referenced from required .class files", bPath, 86, 111, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR));

		env.addClass(root2, "p2", "A",
			"package p2;\n"+
			"public class A {\n"+
			"	public void foo(String data) {}\n"+
			"}\n"
		);

		incrementalBuild();
		expectingNoProblems();
	}

	// 181269
	public void testSecondaryTypeDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A extends Secondary {}\n"+
			"class Secondary {}\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		IPath typePath = env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A extends Secondary {}\n"
		);

		incrementalBuild(projectPath);
		expectingSpecificProblemFor(typePath, new Problem("A", "Secondary cannot be resolved to a type", typePath, 35, 44, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
	}

	// 72468
	public void testTypeDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {}\n"
		);

		IPath bPath = env.addClass(root, "p2", "B",
			"package p2;\n"+
			"public class B extends p1.A{}\n"
		);

		IPath cPath = env.addClass(root, "p3", "C",
			"package p3;\n"+
			"public class C extends p2.B{}\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"class Deleted {}\n"
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {bPath, cPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "p1.A cannot be resolved to a type", bPath, 35, 39, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(cPath, new Problem("C", "The hierarchy of the type C is inconsistent", cPath, 25, 26, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"public class B {}\n"
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	// 72468
	public void testTypeVisibility() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {}\n"
		);

		IPath bPath = env.addClass(root, "p2", "B",
			"package p2;\n"+
			"public class B extends p1.A{}\n"
		);

		IPath cPath = env.addClass(root, "p3", "C",
			"package p3;\n"+
			"public class C extends p2.B{}\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"class A {}\n"
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {bPath, cPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "The type p1.A is not visible", bPath, 35, 39, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(cPath, new Problem("C", "The hierarchy of the type C is inconsistent", cPath, 25, 26, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"public class B {}\n"
			);

		incrementalBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p2", "B",
			"package p2;\n"+
			"public class B extends p1.A{}\n"
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {bPath, cPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "The type p1.A is not visible", bPath, 35, 39, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(cPath, new Problem("C", "The hierarchy of the type C is inconsistent", cPath, 25, 26, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {}\n"
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	// 79163
	public void testTypeVisibility2() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		IPath aPath = env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {\n" +
			"	void foo() { p2.FooFactory.createFoo().foo(); }\n" +
			"	void foos() { p2.FooFactory.createFoos().clone(); }\n" +
			"}\n"
		);

		// Foo & Foos are not public to get visibility problems
		env.addClass(root, "p2", "Foo",
			"package p2;\n"+
			"class Foo { public void foo() {} }\n"
		);
		env.addClass(root, "p2", "Foos",
			"package p2;\n"+
			"class Foos {}\n"
		);

		env.addClass(root, "p2", "FooFactory",
			"package p2;\n"+
			"public class FooFactory {\n" +
			"	public static Foo createFoo() { return null; }\n" +
			"	public static Foos[] createFoos() { return null; }\n" +
			"}\n"
		);

		fullBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {aPath});
		expectingSpecificProblemFor(aPath, new Problem("A", "The type Foo is not visible", aPath, 43, 68, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(aPath, new Problem("A", "The type Foos is not visible", aPath, 93, 119, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p2", "Foo",
			"package p2;\n"+
			"public class Foo { public void foo() {} }\n"
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {aPath});
		expectingSpecificProblemFor(aPath, new Problem("A", "The type Foos is not visible", aPath, 93, 119, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p2", "Foos",
			"package p2;\n"+
			"public class Foos { }\n"
		);

		incrementalBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p2", "Foo",
			"package p2;\n"+
			"class Foo { public void foo() {} }\n"
		);
		env.addClass(root, "p2", "Foos",
			"package p2;\n"+
			"class Foos {}\n"
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {aPath});
		expectingSpecificProblemFor(aPath, new Problem("A", "The type Foo is not visible", aPath, 43, 68, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(aPath, new Problem("A", "The type Foos is not visible", aPath, 93, 119, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
	}

	public void testTypeVariable() throws JavaModelException {
		if ((AbstractCompilerTest.getPossibleComplianceLevels() & AbstractCompilerTest.F_1_5) == 0) return;

		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A<T> {}\n"
		);

		IPath bPath = env.addClass(root, "p2", "B",
			"package p2;\n"+
			"public class B<T> extends p1.A<T> {}\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		IPath aPath = env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A {}\n"
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {bPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "The type A is not generic; it cannot be parameterized with arguments <T>", bPath, 38, 42, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A<T extends Comparable> {}\n"
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {aPath, bPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "Bound mismatch: The type T is not a valid substitute for the bounded parameter <T extends Comparable> of the type A<T>", bPath, 43, 44, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(aPath, new Problem("A", "Comparable is a raw type. References to generic type Comparable<T> should be parameterized", aPath, 37, 47, CategorizedProblem.CAT_UNCHECKED_RAW, IMarker.SEVERITY_WARNING));

		env.addClass(root, "p1", "A",
			"package p1;\n"+
			"public class A<T> {}\n"
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
			M1Path, 190, 200, CategorizedProblem.CAT_DEPRECATION, IMarker.SEVERITY_WARNING));
	expectingSpecificProblemFor(M1Path,
		new Problem("",	"The method foo() from the type N1.N2.N3 is deprecated",
			M1Path, 215, 222, CategorizedProblem.CAT_DEPRECATION, IMarker.SEVERITY_WARNING));
	M1Path = env.addClass(rootPath, "p", "M1", M1Contents);
	incrementalBuild(projectPath);
	expectingOnlyProblemsFor(new IPath[] {M1Path});
	expectingSpecificProblemFor(M1Path,
		new Problem("", "The type N1.N2.N3 is deprecated",
			M1Path, 190, 200, CategorizedProblem.CAT_DEPRECATION, IMarker.SEVERITY_WARNING));
	expectingSpecificProblemFor(M1Path,
		new Problem("",	"The method foo() from the type N1.N2.N3 is deprecated",
			M1Path, 215, 222, CategorizedProblem.CAT_DEPRECATION, IMarker.SEVERITY_WARNING));
}
}
