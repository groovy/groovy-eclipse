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

import junit.framework.Test;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;

public class IncrementalTests extends BuilderTests {

	public IncrementalTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(IncrementalTests.class);
	}

	/*
	 * Ensures that the source range for a duplicate secondary type error is correct
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=77283)
	 */
	public void testAddDuplicateSecondaryType() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "C",
			"package p;	\n"+
			"public class C {}	\n"+
			"class CC {}");

		fullBuild(projectPath);
		expectingNoProblems();

		IPath pathToD = env.addClass(root, "p", "D",
			"package p;	\n"+
			"public class D {}	\n"+
			"class CC {}");

		incrementalBuild(projectPath);
		expectingProblemsFor(
			pathToD,
			"Problem : The type CC is already defined [ resource : </Project/src/p/D.java> range : <37,39> category : <-1> severity : <2>]"
		);
		expectingSpecificProblemsFor(pathToD, new Problem[] {new Problem("", "The type CC is already defined", pathToD, 37, 39, -1, IMarker.SEVERITY_ERROR)});
	}

	public void testDefaultPackage() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "");

		env.addClass(projectPath, "", "A",
			"public class A {}");

		env.addClass(projectPath, "", "B",
			"public class B {}");

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "", "B",
			"public class B {A a;}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testDefaultPackage2() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "bin");

		env.addClass(projectPath, "", "A",
			"public class A {}");

		env.addClass(projectPath, "", "B",
			"public class B {}");

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "", "B",
			"public class B {A a;}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testNewJCL() {
		//----------------------------
		//           Step 1
		//----------------------------
		IPath projectPath = env.addProject("Project");

		IPath root = env.getPackageFragmentRootPath(projectPath, "");
		fullBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root, "java.lang", "Object",
			"package java.lang;\n" +
			"public class Object {\n"+
			"}\n"
			);


		incrementalBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 3
		//----------------------------
		env.addClass(root, "java.lang", "Throwable",
			"package java.lang;\n" +
			"public class Throwable {\n"+
			"}\n"
			);


		incrementalBuild();
		expectingNoProblems();
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=17329
	 */
	public void testRenameMainType() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		/* A.java */
		IPath pathToA = env.addClass(root, "p", "A",
			"package p;	\n"+
			"public class A {}");

		/* B.java */
		IPath pathToB = env.addClass(root, "p", "B",
			"package p;	\n"+
			"public class B extends A {}");

		/* C.java */
		IPath pathToC = env.addClass(root, "p", "C",
			"package p;	\n"+
			"public class C extends B {}");

		fullBuild(projectPath);
		expectingNoProblems();

		/* Touch both A and C, removing A main type */
		pathToA = env.addClass(root, "p", "A",
			"package p;	\n"+
			"public class _A {}");

		pathToC = env.addClass(root, "p", "C",
			"package p;	\n"+
			"public class C extends B { }");

		incrementalBuild(projectPath);
		expectingProblemsFor(
			new IPath[]{ pathToA, pathToB, pathToC },
			"Problem : A cannot be resolved to a type [ resource : </Project/src/p/B.java> range : <35,36> category : <40> severity : <2>]\n" +
			"Problem : The hierarchy of the type C is inconsistent [ resource : </Project/src/p/C.java> range : <25,26> category : <40> severity : <2>]\n" +
			"Problem : The public type _A must be defined in its own file [ resource : </Project/src/p/A.java> range : <25,27> category : <40> severity : <2>]"
		);
		expectingSpecificProblemFor(pathToA, new Problem("_A", "The public type _A must be defined in its own file", pathToA, 25, 27, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(pathToB, new Problem("B", "A cannot be resolved to a type", pathToB, 35, 36, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
		expectingSpecificProblemFor(pathToC, new Problem("C", "The hierarchy of the type C is inconsistent", pathToC, 25, 26, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		/* Touch both A and C, removing A main type */
		pathToA = env.addClass(root, "p", "A",
			"package p;	\n"+
			"public class A {}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=17807
	 * case 1
	 */
	public void testRemoveSecondaryType() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public class AA {}	\n"+
			"class AZ {}");

		IPath pathToAB = env.addClass(root, "p", "AB",
			"package p;	\n"+
			"public class AB extends AZ {}");

		env.addClass(root, "p", "BB",
			"package p;	\n"+
			"public class BB {	\n"+
			"	void foo(){	\n" +
			"		System.out.println(new AB());	\n" +
			"		System.out.println(new ZA());	\n" +
			"	}	\n" +
			"}");

		env.addClass(root, "p", "ZZ",
			"package p;	\n"+
			"public class ZZ {}	\n"+
			"class ZA {}");

		fullBuild(projectPath);
		expectingNoProblems();

		/* Remove AZ and touch BB */
		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public class AA {}");

		env.addClass(root, "p", "BB",
			"package p;	\n"+
			"public class BB {	\n"+
			"	void foo() {	\n" +
			"		System.out.println(new AB());	\n" +
			"		System.out.println(new ZA());	\n" +
			"	}	\n" +
			"}");

		incrementalBuild(projectPath);
		expectingProblemsFor(
			pathToAB,
			"Problem : AZ cannot be resolved to a type [ resource : </Project/src/p/AB.java> range : <36,38> category : <40> severity : <2>]"
		);
		expectingSpecificProblemFor(pathToAB, new Problem("AB", "AZ cannot be resolved to a type", pathToAB, 36, 38, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public class AA {}	\n"+
			"class AZ {}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=17807
	 * case 2
	 */
	public void testRemoveSecondaryType2() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public class AA {}	\n"+
			"class AZ {}");

		env.addClass(root, "p", "AB",
			"package p;	\n"+
			"public class AB extends AZ {}");

		IPath pathToBB = env.addClass(root, "p", "BB",
			"package p;	\n"+
			"public class BB {	\n"+
			"	void foo(){	\n" +
			"		System.out.println(new AB());	\n" +
			"		System.out.println(new ZA());	\n" +
			"	}	\n" +
			"}");

		env.addClass(root, "p", "ZZ",
			"package p;	\n"+
			"public class ZZ {}	\n"+
			"class ZA {}");

		fullBuild(projectPath);
		expectingNoProblems();

		/* Remove ZA and touch BB */
		env.addClass(root, "p", "ZZ",
			"package p;	\n"+
			"public class ZZ {}");

		pathToBB = env.addClass(root, "p", "BB",
			"package p;	\n"+
			"public class BB {	\n"+
			"	void foo() {	\n" +
			"		System.out.println(new AB());	\n" +
			"		System.out.println(new ZA());	\n" +
			"	}	\n" +
			"}");

		incrementalBuild(projectPath);
		expectingProblemsFor(
			pathToBB,
			"Problem : ZA cannot be resolved to a type [ resource : </Project/src/p/BB.java> range : <104,106> category : <40> severity : <2>]"
		);
		expectingSpecificProblemFor(pathToBB, new Problem("BB.foo()", "ZA cannot be resolved to a type", pathToBB, 104, 106, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p", "ZZ",
			"package p;	\n"+
			"public class ZZ {}	\n"+
			"class ZA {}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMoveSecondaryType() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p; \n"+
			"public class AA {} \n"+
			"class AZ {}");

		env.addClass(root, "p", "AB",
			"package p; \n"+
			"public class AB extends AZ {}");

		env.addClass(root, "p", "ZZ",
			"package p; \n"+
			"public class ZZ {}");

		fullBuild(projectPath);
		expectingNoProblems();

		/* Move AZ from AA to ZZ */
		env.addClass(root, "p", "AA",
			"package p; \n"+
			"public class AA {}");

		env.addClass(root, "p", "ZZ",
			"package p; \n"+
			"public class ZZ {} \n"+
			"class AZ {}");

		incrementalBuild(projectPath);
		expectingNoProblems();

		/* Move AZ from ZZ to AA */
		env.addClass(root, "p", "AA",
			"package p; \n"+
			"public class AA {} \n"+
			"class AZ {}");

		env.addClass(root, "p", "ZZ",
			"package p; \n"+
			"public class ZZ {}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMoveMemberType() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p; \n"+
			"public class AA {} \n"+
			"class AZ {static class M{}}");

		env.addClass(root, "p", "AB",
			"package p; \n"+
			"import p.AZ.*; \n"+
			"import p.ZA.*; \n"+
			"public class AB extends M {}");

		env.addClass(root, "p", "ZZ",
			"package p; \n"+
			"public class ZZ {} \n"+
			"class ZA {}");

		fullBuild(projectPath);
		expectingOnlySpecificProblemsFor(
			root,
			new Problem[]{
				new Problem("", "The import p.ZA is never used", new Path("/Project/src/p/AB.java"), 35, 39, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING),
			});

		/* Move M from AA to ZZ */
		env.addClass(root, "p", "AA",
			"package p; \n"+
			"public class AA {} \n"+
			"class AZ {}");

		env.addClass(root, "p", "ZZ",
			"package p; \n"+
			"public class ZZ {} \n"+
			"class ZA {static class M{}}");

		incrementalBuild(projectPath);
		expectingOnlySpecificProblemsFor(
			root,
			new Problem[]{
				new Problem("", "The import p.AZ is never used", new Path("/Project/src/p/AB.java"), 19, 23, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING),
			});

		/* Move M from ZZ to AA */
		env.addClass(root, "p", "AA",
			"package p; \n"+
			"public class AA {} \n"+
			"class AZ {static class M{}}");

		env.addClass(root, "p", "ZZ",
			"package p; \n"+
			"public class ZZ {} \n"+
			"class ZA {}");

		incrementalBuild(projectPath);
		expectingOnlySpecificProblemsFor(
			root,
			new Problem[]{
				new Problem("", "The import p.ZA is never used", new Path("/Project/src/p/AB.java"), 35, 39, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING),
			});
	}

	public void testMovePackage() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath[] exclusionPatterns = new Path[] {new Path("src2/")};
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", exclusionPatterns, null);
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src1/src2");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(src1, "p", "A",
			"package p; \n"+
			"public class A {}");

		fullBuild(projectPath);
		expectingNoProblems();

		env.removePackage(src1, "p");
		env.addClass(src2, "p", "A",
			"package p; \n"+
			"public class A {}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMovePackage2() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath src = env.addPackageFragmentRoot(projectPath, "src");
		IPath other = env.addFolder(projectPath, "other");
		env.setOutputFolder(projectPath, "bin");

		IPath classA = env.addClass(src, "p", "A",
			"package p; \n"+
			"public class A extends Missing {}");
		IPath classB = env.addClass(src, "p.q", "B",
			"package p.q; \n"+
			"public class B extends Missing {}");

		fullBuild(projectPath);
		expectingSpecificProblemFor(
			classA,
			new Problem("", "Missing cannot be resolved to a type", new Path("/Project/src/p/A.java"), 35, 42, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)
		);
		expectingSpecificProblemFor(
			classB,
			new Problem("", "Missing cannot be resolved to a type", new Path("/Project/src/p/q/B.java"), 37, 44, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)
		);

		try {
			IProject p = env.getProject(projectPath);
			IFolder pFolder = p.getWorkspace().getRoot().getFolder(classA.removeLastSegments(1));
			pFolder.move(other.append("p"), true, false, null);
		} catch (CoreException e) {
			env.handle(e);
		}

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMemberTypeFromClassFile() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "A",
			"package p; \n"+
			"public class A extends Z {M[] m;}");

		env.addClass(root, "p", "B",
			"package p; \n"+
			"public class B {A a; E e; \n"+
			"void foo() { System.out.println(a.m); }}");

		env.addClass(root, "p", "E",
			"package p; \n"+
			"public class E extends Z { \n"+
			"void foo() { System.out.println(new M()); }}");

		env.addClass(root, "p", "Z",
			"package p; \n"+
			"public class Z {static class M {}}");

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p", "B",
			"package p; \n"+
			"public class B {A a; E e; \n"+
			"void foo( ) { System.out.println(a.m); }}");

		env.addClass(root, "p", "E",
			"package p; \n"+
			"public class E extends Z { \n"+
			"void foo( ) { System.out.println(new M()); }}");

		env.addClass(root, "p", "Z",
			"package p; \n"+
			"public class Z { static class M {} }");

		int previous = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1; // reduce the lot size
		incrementalBuild(projectPath);
		org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = previous;
		expectingNoProblems();
	}

	// http://dev.eclipse.org/bugs/show_bug.cgi?id=27658
	public void testObjectWithSuperInterfaces() throws JavaModelException {
		try {
			IPath projectPath = env.addProject("Project");
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, "");

			IPath root = env.addPackageFragmentRoot(projectPath, "src");
			env.setOutputFolder(projectPath, "bin");

			env.addClass(root, "java.lang", "Object",
				"package java.lang; \n"+
				"public class Object implements I {} \n"+
				"interface I {}	\n");

			fullBuild(projectPath);

			expectingOnlySpecificProblemsFor(
				root,
				new Problem[]{
					new Problem("", "The type java.lang.Object cannot have a superclass or superinterfaces", new Path("/Project/src/java/lang/Object.java"), 33, 39, CategorizedProblem.CAT_INTERNAL, IMarker.SEVERITY_ERROR),
				});

			env.addClass(root, "p", "X",
				"package p; \n"+
				"public class X {}\n");

			incrementalBuild(projectPath);

			expectingOnlySpecificProblemsFor(
				root,
				new Problem[]{
					new Problem("", "The type java.lang.Object cannot have a superclass or superinterfaces", new Path("/Project/src/java/lang/Object.java"), 33, 39, CategorizedProblem.CAT_INTERNAL, IMarker.SEVERITY_ERROR),
				});

			env.addClass(root, "p", "Y",
				"package p; \n"+
				"public class Y extends X {}\n");

			incrementalBuild(projectPath);

			expectingOnlySpecificProblemsFor(
				root,
				new Problem[]{
					new Problem("", "The type java.lang.Object cannot have a superclass or superinterfaces", new Path("/Project/src/java/lang/Object.java"), 33, 39, CategorizedProblem.CAT_INTERNAL, IMarker.SEVERITY_ERROR),
				});

		} catch(StackOverflowError e){
			assertTrue("Infinite loop in cycle detection", false);
			e.printStackTrace();
		}
	}

	/**
	 * Bugs 6461
	 * TODO excluded test
	 */
	public void _testWrongCompilationUnitLocation() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		IPath bin = env.setOutputFolder(projectPath, "bin");
		IPath x = env.addClass(root, "", "X",
			"public class X {\n"+
			"}\n"
			);


		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(bin.append("X.class"));

		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root, "", "X",
			"package p1;\n"+
			"public class X {\n"+
			"}\n"
			);

		incrementalBuild();
		expectingProblemsFor(x, "???");
		expectingNoPresenceOf(bin.append("X.class"));
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100631
	public void testMemberTypeCollisionWithBinary() throws JavaModelException {
		int max = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		try {
			IPath projectPath = env.addProject("Project");
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, "");

			IPath root = env.addPackageFragmentRoot(projectPath, "src");
			env.setOutputFolder(projectPath, "bin");

			env.addClass(root, "", "A",
				"public class A {\n"+
				"	Object foo(B b) { return b.i; }\n" +
				"}");
			env.addClass(root, "", "B",
				"public class B {\n"+
				"	I.InnerType i;\n" +
				"}");
			env.addClass(root, "", "I",
				"public interface I {\n"+
				"	interface InnerType {}\n" +
				"}");

			fullBuild(projectPath);
			expectingNoProblems();

			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1;

			env.addClass(root, "", "A",
				"public class A {\n"+
				"	Object foo(B b) { return b.i; }\n" +
				"}");
			env.addClass(root, "", "I",
				"public interface I {\n"+
				"	interface InnerType {}\n" +
				"}");

			incrementalBuild(projectPath);
			expectingNoProblems();
		} finally {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = max;
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=191739
	public void testMemberTypeCollisionWithBinary2() throws JavaModelException {
		int max = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		try {
			IPath projectPath = env.addProject("Project");
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, "");

			IPath src1 = env.addPackageFragmentRoot(projectPath, "src1");
			IPath bin1 = env.setOutputFolder(projectPath, "bin1");

			env.addClass(src1, "p1", "NoSource",
				"package p1;	\n"+
				"import p2.Foo;\n"+
				"public class NoSource {\n"+
				"	public NoSource(Foo.Bar b) {}\n" +
				"}");

			IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "bin2");

			env.addClass(src2, "p2", "Foo",
				"package p2; \n"+
				"public class Foo {\n"+
				"	public static class Bar {\n" +
				"		public static Bar LocalBar = new Bar();\n" +
				"	}\n" +
				"}");

			env.addClass(src2, "p2", "Test",
				"package p2; \n"+
				"import p1.NoSource;\n"+
				"import p2.Foo.Bar;\n"+
				"public class Test {\n"+
				"	NoSource nosrc = new NoSource(Bar.LocalBar);\n" +
				"}");

			fullBuild(projectPath);
			expectingNoProblems();

			env.removePackageFragmentRoot(projectPath, "src1");
			env.addClassFolder(projectPath, bin1, false);

			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1;

			env.addClass(src2, "p2", "Test",
				"package p2; \n"+
				"import p1.NoSource;\n"+
				"import p2.Foo.Bar;\n"+
				"public class Test {\n"+
				"	NoSource nosrc = new NoSource(Bar.LocalBar);\n" +
				"}");

			incrementalBuild(projectPath);
			expectingNoProblems();
		} finally {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = max;
		}
	}


	public void test129316() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "");

		IPath yPath = env.addClass(projectPath, "p", "Y",
			"package p;\n" +
			"public class Y extends Z {}");

		env.addClass(projectPath, "p", "Z",
			"package p;\n" +
			"public class Z {}");

		env.addClass(projectPath, "", "X",
			"import p.Y;\n" +
			"public class X {\n" +
			"	boolean b(Object o) {\n" +
			"		return o instanceof Y;\n" +
			"    }\n" +
			"}");

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "p", "Y",
				"package p;\n" +
				"public class Y extends Zork {}");

		incrementalBuild(projectPath);
		expectingSpecificProblemFor(yPath, new Problem("Y", "Zork cannot be resolved to a type", yPath, 34, 38, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		IPath xPath = env.addClass(projectPath, "", "X",
				"public class X {\n" +
				"	boolean b(Object o) {\n" +
				"		return o instanceof p.Y;\n" +
				"    }\n" +
				"}");

		incrementalBuild(projectPath);
		expectingSpecificProblemFor(yPath, new Problem("Y", "Zork cannot be resolved to a type", yPath, 34, 38, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
		expectingNoProblemsFor(xPath);
	}

	public void testSecondaryType() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "AB",
			"public class AB { AZ z = new AA();}");

		env.addClass(root, "", "AA",
			"public class AA extends AZ {} \n"+
			"class AZ {}");

		int max = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		try {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1;
			fullBuild(projectPath);
		} finally {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = max;
		}
		expectingNoProblems();
	}

	// http://dev.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
	public void testMissingType001() throws JavaModelException {

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		IPath xPath = env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo(p2.Y y) {	\n" +
			"		y.bar(null);" +
			"	}\n" +
			"	void X() {}\n" +
			"}\n"
			);
		IPath yPath = env.addClass(root, "p2", "Y",
			"package p2;\n"+
			"public class Y {\n"+
			"	public void bar(Z z) {}\n" +
			"}\n"
			);
		fullBuild(projectPath);
		expectingSpecificProblemFor(xPath, new Problem("X", "This method has a constructor name", xPath, 73, 76, CategorizedProblem.CAT_CODE_STYLE, IMarker.SEVERITY_WARNING));
		expectingSpecificProblemFor(yPath, new Problem("Y", "Z cannot be resolved to a type", yPath, 46, 47, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p2", "Z",
			"package p2;\n"+
			"public class Z {\n"+
			"}\n"
			);
		incrementalBuild(projectPath);
		expectingSpecificProblemFor(xPath, new Problem("X", "This method has a constructor name", xPath, 73, 76, CategorizedProblem.CAT_CODE_STYLE, IMarker.SEVERITY_WARNING));
	}

	// http://dev.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
	public void testMissingType002() throws JavaModelException {

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		IPath yPath = env.addClass(root, "p2", "Y",
				"package p2;\n"+
				"public class Y {\n"+
				"	public void bar(Z z) {}\n" +
				"}\n"
				);
		fullBuild(projectPath);
		expectingSpecificProblemFor(yPath, new Problem("Y", "Z cannot be resolved to a type", yPath, 46, 47, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		IPath xPath = env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo(p2.Y y) {	\n" +
			"		y.bar(null);" +
			"	}\n" +
			"	void X() {}\n" +
			"}\n"
			);
		incrementalBuild(projectPath);
		expectingSpecificProblemFor(xPath, new Problem("X", "This method has a constructor name", xPath, 73, 76, CategorizedProblem.CAT_CODE_STYLE, IMarker.SEVERITY_WARNING));
		expectingSpecificProblemFor(yPath, new Problem("Y", "Z cannot be resolved to a type", yPath, 46, 47, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p2", "Z",
			"package p2;\n"+
			"public class Z {\n"+
			"}\n"
			);
		incrementalBuild(projectPath);
		expectingSpecificProblemFor(xPath, new Problem("X", "This method has a constructor name", xPath, 73, 76, CategorizedProblem.CAT_CODE_STYLE, IMarker.SEVERITY_WARNING));
	}

	// http://dev.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
	public void testMissingType003() throws JavaModelException {

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		IPath yPath = env.addClass(root, "p2", "Y",
				"package p2;\n"+
				"public class Y {\n"+
				"	public void bar(p1.Z z) {}\n" +
				"}\n"
				);
		fullBuild(projectPath);
		expectingSpecificProblemFor(yPath, new Problem("Y", "p1 cannot be resolved to a type", yPath, 46, 48, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		IPath xPath = env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo(p2.Y y) {	\n" +
			"		y.bar(null);" +
			"	}\n" +
			"	void X() {}\n" +
			"}\n"
			);
		incrementalBuild(projectPath);
		expectingSpecificProblemFor(xPath, new Problem("X", "This method has a constructor name", xPath, 73, 76, CategorizedProblem.CAT_CODE_STYLE, IMarker.SEVERITY_WARNING));
		expectingSpecificProblemFor(yPath, new Problem("Y", "p1.Z cannot be resolved to a type", yPath, 46, 50, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		env.addClass(root, "p1", "Z",
			"package p1;\n"+
			"public class Z {\n"+
			"}\n"
			);
		incrementalBuild(projectPath);
		expectingSpecificProblemFor(xPath, new Problem("X", "This method has a constructor name", xPath, 73, 76, CategorizedProblem.CAT_CODE_STYLE, IMarker.SEVERITY_WARNING));
	}
}
