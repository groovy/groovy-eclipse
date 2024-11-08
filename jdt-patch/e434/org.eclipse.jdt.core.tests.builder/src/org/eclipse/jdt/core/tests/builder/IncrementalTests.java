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

import java.util.Hashtable;
import junit.framework.Test;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({"rawtypes", "unchecked"})
public class IncrementalTests extends BuilderTests {

	static {
//		TESTS_NAMES = new String [] { "testBinaryInnerRecordClass" };
	}
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
		env.removeProject(projectPath);
	}

	public void testDefaultPackage() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, ""); //$NON-NLS-1$

		env.addClass(projectPath, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}"); //$NON-NLS-1$

		env.addClass(projectPath, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B {A a;}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
		env.removeProject(projectPath);
	}

	public void testDefaultPackage2() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(projectPath, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}"); //$NON-NLS-1$

		env.addClass(projectPath, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B {A a;}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
		env.removeProject(projectPath);
	}

	public void testNewJCL() {
		//----------------------------
		//           Step 1
		//----------------------------
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$

		IPath root = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		fullBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root, "java.lang", "Object", //$NON-NLS-1$ //$NON-NLS-2$
			"package java.lang;\n" + //$NON-NLS-1$
			"public class Object {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);


		incrementalBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 3
		//----------------------------
		env.addClass(root, "java.lang", "Throwable", //$NON-NLS-1$ //$NON-NLS-2$
			"package java.lang;\n" + //$NON-NLS-1$
			"public class Throwable {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);


		incrementalBuild();
		expectingNoProblems();
		env.removeProject(projectPath);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=17329
	 */
	public void testRenameMainType() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		/* A.java */
		IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class A {}"); //$NON-NLS-1$

		/* B.java */
		IPath pathToB = env.addClass(root, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class B extends A {}"); //$NON-NLS-1$

		/* C.java */
		IPath pathToC = env.addClass(root, "p", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class C extends B {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		/* Touch both A and C, removing A main type */
		pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class _A {}"); //$NON-NLS-1$

		pathToC = env.addClass(root, "p", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class C extends B { }"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingProblemsFor(
			new IPath[]{ pathToA, pathToB, pathToC },
			"Problem : A cannot be resolved to a type [ resource : </Project/src/p/B.java> range : <35,36> category : <40> severity : <2>]\n" +
			"Problem : The hierarchy of the type C is inconsistent [ resource : </Project/src/p/C.java> range : <25,26> category : <40> severity : <2>]\n" +
			"Problem : The public type _A must be defined in its own file [ resource : </Project/src/p/A.java> range : <25,27> category : <40> severity : <2>]"
		);
		expectingSpecificProblemFor(pathToA, new Problem("_A", "The public type _A must be defined in its own file", pathToA, 25, 27, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(pathToB, new Problem("B", "A cannot be resolved to a type", pathToB, 35, 36, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(pathToC, new Problem("C", "The hierarchy of the type C is inconsistent", pathToC, 25, 26, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		/* Touch both A and C, removing A main type */
		pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class A {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
		env.removeProject(projectPath);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=17807
	 * case 1
	 */
	public void testRemoveSecondaryType() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AA {}	\n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		IPath pathToAB = env.addClass(root, "p", "AB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AB extends AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "BB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class BB {	\n"+ //$NON-NLS-1$
			"	void foo(){	\n" + //$NON-NLS-1$
			"		System.out.println(new AB());	\n" + //$NON-NLS-1$
			"		System.out.println(new ZA());	\n" + //$NON-NLS-1$
			"	}	\n" + //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class ZZ {}	\n"+ //$NON-NLS-1$
			"class ZA {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		/* Remove AZ and touch BB */
		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AA {}"); //$NON-NLS-1$

		env.addClass(root, "p", "BB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class BB {	\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		System.out.println(new AB());	\n" + //$NON-NLS-1$
			"		System.out.println(new ZA());	\n" + //$NON-NLS-1$
			"	}	\n" + //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingProblemsFor(
			pathToAB,
			"Problem : AZ cannot be resolved to a type [ resource : </Project/src/p/AB.java> range : <36,38> category : <40> severity : <2>]"
		);
		expectingSpecificProblemFor(pathToAB, new Problem("AB", "AZ cannot be resolved to a type", pathToAB, 36, 38, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AA {}	\n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
		env.removeProject(projectPath);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=17807
	 * case 2
	 */
	public void testRemoveSecondaryType2() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AA {}	\n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "AB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AB extends AZ {}"); //$NON-NLS-1$

		IPath pathToBB = env.addClass(root, "p", "BB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class BB {	\n"+ //$NON-NLS-1$
			"	void foo(){	\n" + //$NON-NLS-1$
			"		System.out.println(new AB());	\n" + //$NON-NLS-1$
			"		System.out.println(new ZA());	\n" + //$NON-NLS-1$
			"	}	\n" + //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class ZZ {}	\n"+ //$NON-NLS-1$
			"class ZA {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		/* Remove ZA and touch BB */
		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class ZZ {}"); //$NON-NLS-1$

		pathToBB = env.addClass(root, "p", "BB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class BB {	\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		System.out.println(new AB());	\n" + //$NON-NLS-1$
			"		System.out.println(new ZA());	\n" + //$NON-NLS-1$
			"	}	\n" + //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingProblemsFor(
			pathToBB,
			"Problem : ZA cannot be resolved to a type [ resource : </Project/src/p/BB.java> range : <104,106> category : <40> severity : <2>]"
		);
		expectingSpecificProblemFor(pathToBB, new Problem("BB.foo()", "ZA cannot be resolved to a type", pathToBB, 104, 106, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class ZZ {}	\n"+ //$NON-NLS-1$
			"class ZA {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
		env.removeProject(projectPath);
	}

	public void testMoveSecondaryType() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {} \n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "AB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AB extends AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		/* Move AZ from AA to ZZ */
		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {} \n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();

		/* Move AZ from ZZ to AA */
		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {} \n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
		env.removeProject(projectPath);
	}

	public void testMoveMemberType() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {} \n"+ //$NON-NLS-1$
			"class AZ {static class M{}}"); //$NON-NLS-1$

		env.addClass(root, "p", "AB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"import p.AZ.*; \n"+ //$NON-NLS-1$
			"import p.ZA.*; \n"+ //$NON-NLS-1$
			"public class AB extends M {}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {} \n"+ //$NON-NLS-1$
			"class ZA {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingOnlySpecificProblemsFor(
			root,
			new Problem[]{
				new Problem("", "The import p.ZA is never used", new Path("/Project/src/p/AB.java"), 35, 39, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			});

		/* Move M from AA to ZZ */
		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {} \n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {} \n"+ //$NON-NLS-1$
			"class ZA {static class M{}}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingOnlySpecificProblemsFor(
			root,
			new Problem[]{
				new Problem("", "The import p.AZ is never used", new Path("/Project/src/p/AB.java"), 19, 23, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			});

		/* Move M from ZZ to AA */
		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {} \n"+ //$NON-NLS-1$
			"class AZ {static class M{}}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {} \n"+ //$NON-NLS-1$
			"class ZA {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingOnlySpecificProblemsFor(
			root,
			new Problem[]{
				new Problem("", "The import p.ZA is never used", new Path("/Project/src/p/AB.java"), 35, 39, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			});
		env.removeProject(projectPath);
	}

	public void testMovePackage() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath[] exclusionPatterns = new Path[] {new Path("src2/")}; //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", exclusionPatterns, null); //$NON-NLS-1$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src1/src2"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(src1, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class A {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		env.removePackage(src1, "p"); //$NON-NLS-1$
		env.addClass(src2, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class A {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
		env.removeProject(projectPath);
	}

	public void testMovePackage2() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath other = env.addFolder(projectPath, "other"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath classA = env.addClass(src, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class A extends Missing {}"); //$NON-NLS-1$
		IPath classB = env.addClass(src, "p.q", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.q; \n"+ //$NON-NLS-1$
			"public class B extends Missing {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingSpecificProblemFor(
			classA,
			new Problem("", "Missing cannot be resolved to a type", new Path("/Project/src/p/A.java"), 35, 42, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		);
		expectingSpecificProblemFor(
			classB,
			new Problem("", "Missing cannot be resolved to a type", new Path("/Project/src/p/q/B.java"), 37, 44, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		env.removeProject(projectPath);
	}

	public void testMemberTypeFromClassFile() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class A extends Z {M[] m;}"); //$NON-NLS-1$

		env.addClass(root, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class B {A a; E e; \n"+ //$NON-NLS-1$
			"void foo() { System.out.println(a.m); }}"); //$NON-NLS-1$

		env.addClass(root, "p", "E", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class E extends Z { \n"+ //$NON-NLS-1$
			"void foo() { System.out.println(new M()); }}"); //$NON-NLS-1$

		env.addClass(root, "p", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class Z {static class M {}}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class B {A a; E e; \n"+ //$NON-NLS-1$
			"void foo( ) { System.out.println(a.m); }}"); //$NON-NLS-1$

		env.addClass(root, "p", "E", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class E extends Z { \n"+ //$NON-NLS-1$
			"void foo( ) { System.out.println(new M()); }}"); //$NON-NLS-1$

		env.addClass(root, "p", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class Z { static class M {} }"); //$NON-NLS-1$

		int previous = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1; // reduce the lot size
		incrementalBuild(projectPath);
		org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = previous;
		expectingNoProblems();
		env.removeProject(projectPath);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=372418
	public void testMemberTypeOfOtherProject() throws JavaModelException {
		IPath projectPath1 = env.addProject("Project1", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
		env.addExternalJars(projectPath1, Util.getJavaClassLibs());

		IPath projectPath2 = env.addProject("Project2", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
		env.addExternalJars(projectPath2, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath1, ""); //$NON-NLS-1$

		IPath root1 = env.addPackageFragmentRoot(projectPath1, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath1, "bin"); //$NON-NLS-1$

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath2, ""); //$NON-NLS-1$

		IPath root2 = env.addPackageFragmentRoot(projectPath2, "src"); //$NON-NLS-1$
		IPath output2 = env.setOutputFolder(projectPath2, "bin"); //$NON-NLS-1$

		env.addClassFolder(projectPath1, output2, true);
		env.addRequiredProject(projectPath2, projectPath1);

		env.addClass(root1, "pB", "BaseClass", //$NON-NLS-1$ //$NON-NLS-2$
			"package pB; \n"+ //$NON-NLS-1$
			"public class BaseClass {\n" + //$NON-NLS-1$
			"  public static class Builder <T> {\n"+ //$NON-NLS-1$
			"    public Builder(T t) {\n" + //$NON-NLS-1$
			"    }\n" + //$NON-NLS-1$
			"  }\n" + //$NON-NLS-1$
			"}\n");//$NON-NLS-1$

		env.addClass(root1, "pR", "ReferencingClass", //$NON-NLS-1$ //$NON-NLS-2$
				"package pR; \n"+ //$NON-NLS-1$
				"import pD.DerivedClass.Builder;\n"+ //$NON-NLS-1$
				"public class ReferencingClass {\n" + //$NON-NLS-1$
				"   Builder<String> builder = new Builder<String>(null);\n" + //$NON-NLS-1$
				"}\n"); //$NON-NLS-1$

		env.addClass(root2, "pD", "DerivedClass", //$NON-NLS-1$ //$NON-NLS-2$
				"package pD; \n"+ //$NON-NLS-1$
				"public class DerivedClass extends pB.BaseClass {\n" + //$NON-NLS-1$
				"  public static class Builder<T> extends pB.BaseClass.Builder <T> {\n"+ //$NON-NLS-1$
				"    public Builder(T t) {\n" + //$NON-NLS-1$
				"		super(t);\n" + //$NON-NLS-1$
				"    }\n" + //$NON-NLS-1$
				"  }\n" + //$NON-NLS-1$
				"}\n"); //$NON-NLS-1$

		int previous = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		fullBuild();
		org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1; // reduce the lot size
		cleanBuild();
		fullBuild();
		cleanBuild("Project1"); //$NON-NLS-1$
		fullBuild(projectPath1);
		org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = previous;
		expectingNoProblems();
		env.removeProject(projectPath1);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=377401
	public void test$InTypeName() throws JavaModelException {
		IPath projectPath1 = env.addProject("Project1", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$ //$NON-NLS-2$
		env.addExternalJars(projectPath1, Util.getJavaClassLibs());

		IPath projectPath2 = env.addProject("Project2", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$ //$NON-NLS-2$
		env.addExternalJars(projectPath2, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath1, ""); //$NON-NLS-1$

		IPath root1 = env.addPackageFragmentRoot(projectPath1, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath1, "bin"); //$NON-NLS-1$

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath2, ""); //$NON-NLS-1$

		IPath root2 = env.addPackageFragmentRoot(projectPath2, "src"); //$NON-NLS-1$
		IPath output2 = env.setOutputFolder(projectPath2, "bin"); //$NON-NLS-1$

		env.addClassFolder(projectPath1, output2, true);
		env.addRequiredProject(projectPath2, projectPath1);

		env.addClass(root1, "pB", "Builder$a", //$NON-NLS-1$ //$NON-NLS-2$
			"package pB; \n"+ //$NON-NLS-1$
			"public class Builder$a<T> {\n" + //$NON-NLS-1$
			"    public Builder$a(T t) {\n" + //$NON-NLS-1$
			"    }\n" + //$NON-NLS-1$
			"}\n");//$NON-NLS-1$

		env.addClass(root1, "pR", "ReferencingClass", //$NON-NLS-1$ //$NON-NLS-2$
				"package pR; \n"+ //$NON-NLS-1$
				"import pD.DerivedClass$a;\n"+ //$NON-NLS-1$
				"public class ReferencingClass {\n" + //$NON-NLS-1$
				"   DerivedClass$a<String> builder = new DerivedClass$a<String>(null);\n" + //$NON-NLS-1$
				"}\n"); //$NON-NLS-1$

		env.addClass(root2, "pD", "DerivedClass$a", //$NON-NLS-1$ //$NON-NLS-2$
				"package pD; \n"+ //$NON-NLS-1$
				"public class DerivedClass$a<T> extends pB.Builder$a<T> {\n" + //$NON-NLS-1$
				"    public DerivedClass$a(T t) {\n" + //$NON-NLS-1$
				"		super(t);\n" + //$NON-NLS-1$
				"    }\n" + //$NON-NLS-1$
				"}\n"); //$NON-NLS-1$

		int previous = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		fullBuild();
		org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1; // reduce the lot size
		cleanBuild();
		fullBuild();
		cleanBuild("Project1"); //$NON-NLS-1$
		fullBuild(projectPath1);
		org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = previous;
		expectingNoProblems();
		env.removeProject(projectPath1);
	}

	// http://dev.eclipse.org/bugs/show_bug.cgi?id=27658
	public void testObjectWithSuperInterfaces() throws JavaModelException {
		try {
			IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

			env.addClass(root, "java.lang", "Object", //$NON-NLS-1$ //$NON-NLS-2$
				"package java.lang; \n"+ //$NON-NLS-1$
				"public class Object implements I {} \n"+ //$NON-NLS-1$
				"interface I {}	\n");	//$NON-NLS-1$

			fullBuild(projectPath);

			expectingOnlySpecificProblemsFor(
				root,
				new Problem[]{
					new Problem("", "The type java.lang.Object cannot have a superclass or superinterfaces", new Path("/Project/src/java/lang/Object.java"), 33, 39, CategorizedProblem.CAT_INTERNAL, IMarker.SEVERITY_ERROR), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				});

			env.addClass(root, "p", "X", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n"+ //$NON-NLS-1$
				"public class X {}\n"); //$NON-NLS-1$

			incrementalBuild(projectPath);

			expectingOnlySpecificProblemsFor(
				root,
				new Problem[]{
					new Problem("", "The type java.lang.Object cannot have a superclass or superinterfaces", new Path("/Project/src/java/lang/Object.java"), 33, 39, CategorizedProblem.CAT_INTERNAL, IMarker.SEVERITY_ERROR), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				});

			env.addClass(root, "p", "Y", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n"+ //$NON-NLS-1$
				"public class Y extends X {}\n"); //$NON-NLS-1$

			incrementalBuild(projectPath);

			expectingOnlySpecificProblemsFor(
				root,
				new Problem[]{
					new Problem("", "The type java.lang.Object cannot have a superclass or superinterfaces", new Path("/Project/src/java/lang/Object.java"), 33, 39, CategorizedProblem.CAT_INTERNAL, IMarker.SEVERITY_ERROR), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				});
			env.removeProject(projectPath);

		} catch(StackOverflowError e){
			assertTrue("Infinite loop in cycle detection", false); //$NON-NLS-1$
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
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		IPath x = env.addClass(root, "", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"public class X {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);


		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(bin.append("X.class")); //$NON-NLS-1$

		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root, "", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild();
		expectingProblemsFor(x, "???");
		expectingNoPresenceOf(bin.append("X.class")); //$NON-NLS-1$
		env.removeProject(projectPath);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100631
	public void testMemberTypeCollisionWithBinary() throws JavaModelException {
		int max = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		try {
			IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

			env.addClass(root, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"public class A {\n"+ //$NON-NLS-1$
				"	Object foo(B b) { return b.i; }\n" + //$NON-NLS-1$
				"}");	//$NON-NLS-1$
			env.addClass(root, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
				"public class B {\n"+ //$NON-NLS-1$
				"	I.InnerType i;\n" + //$NON-NLS-1$
				"}");	//$NON-NLS-1$
			env.addClass(root, "", "I", //$NON-NLS-1$ //$NON-NLS-2$
				"public interface I {\n"+ //$NON-NLS-1$
				"	interface InnerType {}\n" + //$NON-NLS-1$
				"}");	//$NON-NLS-1$

			fullBuild(projectPath);
			expectingNoProblems();

			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1;

			env.addClass(root, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"public class A {\n"+ //$NON-NLS-1$
				"	Object foo(B b) { return b.i; }\n" + //$NON-NLS-1$
				"}");	//$NON-NLS-1$
			env.addClass(root, "", "I", //$NON-NLS-1$ //$NON-NLS-2$
				"public interface I {\n"+ //$NON-NLS-1$
				"	interface InnerType {}\n" + //$NON-NLS-1$
				"}");	//$NON-NLS-1$

			incrementalBuild(projectPath);
			expectingNoProblems();
			env.removeProject(projectPath);
		} finally {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = max;
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=191739
	public void testMemberTypeCollisionWithBinary2() throws JavaModelException {
		int max = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		try {
			IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

			IPath src1 = env.addPackageFragmentRoot(projectPath, "src1"); //$NON-NLS-1$
			IPath bin1 = env.setOutputFolder(projectPath, "bin1"); //$NON-NLS-1$

			env.addClass(src1, "p1", "NoSource", //$NON-NLS-1$ //$NON-NLS-2$
				"package p1;	\n"+
				"import p2.Foo;\n"+ //$NON-NLS-1$
				"public class NoSource {\n"+ //$NON-NLS-1$
				"	public NoSource(Foo.Bar b) {}\n" + //$NON-NLS-1$
				"}");	//$NON-NLS-1$

			IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "bin2"); //$NON-NLS-1$ //$NON-NLS-2$

			env.addClass(src2, "p2", "Foo", //$NON-NLS-1$ //$NON-NLS-2$
				"package p2; \n"+
				"public class Foo {\n"+ //$NON-NLS-1$
				"	public static class Bar {\n" + //$NON-NLS-1$
				"		public static Bar LocalBar = new Bar();\n" + //$NON-NLS-1$
				"	}\n" + //$NON-NLS-1$
				"}");	//$NON-NLS-1$

			env.addClass(src2, "p2", "Test", //$NON-NLS-1$ //$NON-NLS-2$
				"package p2; \n"+
				"import p1.NoSource;\n"+ //$NON-NLS-1$
				"import p2.Foo.Bar;\n"+ //$NON-NLS-1$
				"public class Test {\n"+ //$NON-NLS-1$
				"	NoSource nosrc = new NoSource(Bar.LocalBar);\n" + //$NON-NLS-1$
				"}");	//$NON-NLS-1$

			fullBuild(projectPath);
			expectingNoProblems();

			env.removePackageFragmentRoot(projectPath, "src1"); //$NON-NLS-1$
			env.addClassFolder(projectPath, bin1, false);

			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1;

			env.addClass(src2, "p2", "Test", //$NON-NLS-1$ //$NON-NLS-2$
				"package p2; \n"+
				"import p1.NoSource;\n"+ //$NON-NLS-1$
				"import p2.Foo.Bar;\n"+ //$NON-NLS-1$
				"public class Test {\n"+ //$NON-NLS-1$
				"	NoSource nosrc = new NoSource(Bar.LocalBar);\n" + //$NON-NLS-1$
				"}");	//$NON-NLS-1$

			incrementalBuild(projectPath);
			expectingNoProblems();
			env.removeProject(projectPath);
		} finally {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = max;
		}
	}


	public void test129316() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, ""); //$NON-NLS-1$

		IPath yPath = env.addClass(projectPath, "p", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" +
			"public class Y extends Z {}"); //$NON-NLS-1$

		env.addClass(projectPath, "p", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" +
			"public class Z {}"); //$NON-NLS-1$

		env.addClass(projectPath, "", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"import p.Y;\n" +
			"public class X {\n" +
			"	boolean b(Object o) {\n" +
			"		return o instanceof Y;\n" +
			"    }\n" +
			"}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "p", "Y", //$NON-NLS-1$ //$NON-NLS-2$
				"package p;\n" +
				"public class Y extends Zork {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingSpecificProblemFor(yPath, new Problem("Y", "Zork cannot be resolved to a type", yPath, 34, 38, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		IPath xPath = env.addClass(projectPath, "", "X", //$NON-NLS-1$ //$NON-NLS-2$
				"public class X {\n" +
				"	boolean b(Object o) {\n" +
				"		return o instanceof p.Y;\n" +
				"    }\n" +
				"}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingSpecificProblemFor(yPath, new Problem("Y", "Zork cannot be resolved to a type", yPath, 34, 38, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingNoProblemsFor(xPath);
		env.removeProject(projectPath);
	}

	public void testSecondaryType() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "", "AB", //$NON-NLS-1$ //$NON-NLS-2$
			"public class AB { AZ z = new AA();}"); //$NON-NLS-1$

		env.addClass(root, "", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"public class AA extends AZ {} \n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		int max = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		try {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1;
			fullBuild(projectPath);
		} finally {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = max;
		}
		expectingNoProblems();
		env.removeProject(projectPath);
	}

	// http://dev.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
	public void testMissingType001() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath xPath = env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo(p2.Y y) {	\n" + //$NON-NLS-1$
			"		y.bar(null);" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	void X() {}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		IPath yPath = env.addClass(root, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Y {\n"+ //$NON-NLS-1$
			"	public void bar(Z z) {}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		fullBuild(projectPath);
		expectingSpecificProblemFor(xPath, new Problem("X", "This method has a constructor name", xPath, 73, 76, CategorizedProblem.CAT_CODE_STYLE, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(yPath, new Problem("Y", "Z cannot be resolved to a type", yPath, 46, 47, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Z {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		incrementalBuild(projectPath);
		expectingSpecificProblemFor(xPath, new Problem("X", "This method has a constructor name", xPath, 73, 76, CategorizedProblem.CAT_CODE_STYLE, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$ //$NON-NLS-2$
		env.removeProject(projectPath);
	}

	// http://dev.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
	public void testMissingType002() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath yPath = env.addClass(root, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
				"package p2;\n"+ //$NON-NLS-1$
				"public class Y {\n"+ //$NON-NLS-1$
				"	public void bar(Z z) {}\n" + //$NON-NLS-1$
				"}\n" //$NON-NLS-1$
				);
		fullBuild(projectPath);
		expectingSpecificProblemFor(yPath, new Problem("Y", "Z cannot be resolved to a type", yPath, 46, 47, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		IPath xPath = env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo(p2.Y y) {	\n" + //$NON-NLS-1$
			"		y.bar(null);" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	void X() {}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		incrementalBuild(projectPath);
		expectingSpecificProblemFor(xPath, new Problem("X", "This method has a constructor name", xPath, 73, 76, CategorizedProblem.CAT_CODE_STYLE, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(yPath, new Problem("Y", "Z cannot be resolved to a type", yPath, 46, 47, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Z {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		incrementalBuild(projectPath);
		expectingSpecificProblemFor(xPath, new Problem("X", "This method has a constructor name", xPath, 73, 76, CategorizedProblem.CAT_CODE_STYLE, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$ //$NON-NLS-2$
		env.removeProject(projectPath);
	}

	// http://dev.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
	public void testMissingType003() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath yPath = env.addClass(root, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
				"package p2;\n"+ //$NON-NLS-1$
				"public class Y {\n"+ //$NON-NLS-1$
				"	public void bar(p1.Z z) {}\n" + //$NON-NLS-1$
				"}\n" //$NON-NLS-1$
				);
		fullBuild(projectPath);
		expectingSpecificProblemFor(yPath, new Problem("Y", "p1 cannot be resolved to a type", yPath, 46, 48, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		IPath xPath = env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo(p2.Y y) {	\n" + //$NON-NLS-1$
			"		y.bar(null);" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	void X() {}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		incrementalBuild(projectPath);
		expectingSpecificProblemFor(xPath, new Problem("X", "This method has a constructor name", xPath, 73, 76, CategorizedProblem.CAT_CODE_STYLE, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(yPath, new Problem("Y", "p1.Z cannot be resolved to a type", yPath, 46, 50, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Z {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		incrementalBuild(projectPath);
		expectingSpecificProblemFor(xPath, new Problem("X", "This method has a constructor name", xPath, 73, 76, CategorizedProblem.CAT_CODE_STYLE, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$ //$NON-NLS-2$
		env.removeProject(projectPath);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334377
	public void testBug334377() throws JavaModelException {
		int max = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		Hashtable options = null;
		try {
			options = JavaCore.getOptions();
			Hashtable newOptions = JavaCore.getOptions();
			newOptions.put(JavaCore.COMPILER_COMPLIANCE, CompilerOptions.getFirstSupportedJavaVersion());
			newOptions.put(JavaCore.COMPILER_SOURCE, CompilerOptions.getFirstSupportedJavaVersion());
			newOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion());
			JavaCore.setOptions(newOptions);

			IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

			env.addClass(root, "", "Upper",
				"public abstract class Upper<T>  {\n" +
				"    public static enum Mode {IN,OUT;}\n" +
				"}\n");
			env.addClass(root, "", "Lower",
				"public class Lower extends Upper<Lower> {};\n");
			env.addClass(root, "", "Bug",
					"public class Bug {\n" +
					"    Upper.Mode m1;\n" +
					"    void usage(){\n" +
					"        Lower.Mode m3;\n" +
					"        if (m1 == null){\n" +
					"            m3 = Lower.Mode.IN;\n" +
					"        } else {\n" +
					"            m3 = m1;\n" +
					"        }\n" +
					"        Lower.Mode m2 = (m1 == null ?  Lower.Mode.IN : m1);\n" +
					"        System.out.println(m2);\n" +
					"        System.out.println(m3);\n" +
					"    }\n" +
					"}\n");

			fullBuild(projectPath);
			expectingNoProblems();
			env.addClass(root, "", "Bug",
					"public class Bug {\n" +
					"    Upper.Mode m1;\n" +
					"    void usage(){\n" +
					"        Lower.Mode m3;\n" +
					"        if (m1 == null){\n" +
					"            m3 = Lower.Mode.IN;\n" +
					"        } else {\n" +
					"            m3 = m1;\n" +
					"        }\n" +
					"        Lower.Mode m2 = (m1 == null ?  Lower.Mode.IN : m1);\n" +
					"        System.out.println(m2);\n" +
					"        System.out.println(m3);\n" +
					"    }\n" +
					"}\n");

			incrementalBuild(projectPath);
			expectingNoProblems();
			env.removeProject(projectPath);
		} finally {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = max;
			JavaCore.setOptions(options);
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=364450
	// Incremental build should not generate buildpath error
	// NOT generated by full build.
	public void testBug364450() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath wPath = env.addClass(projectPath, "w", "W", //$NON-NLS-1$ //$NON-NLS-2$
			"package w;\n" +
			"public class W {\n" +
			"	private w.I i;}"); //$NON-NLS-1$

		IPath aPath = env.addClass(projectPath, "a", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package a;\n" +
			"import w.I;\n" +
			"import w.W;\n" +
			"public class A {}"); //$NON-NLS-1$
		env.waitForManualRefresh();
		fullBuild(projectPath);
		env.waitForAutoBuild();
		expectingSpecificProblemFor(wPath, new Problem("W", "w.I cannot be resolved to a type", wPath, 37, 40, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(aPath, new Problem("A", "The import w.I cannot be resolved", aPath, 18, 21, CategorizedProblem.CAT_IMPORT, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		aPath = env.addClass(projectPath, "a", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package a;\n" +
				"import w.I;\n" +
				"import w.W;\n" +
				"public class A {}"); //$NON-NLS-1$

		env.waitForManualRefresh();
		incrementalBuild(projectPath);
		env.waitForAutoBuild();
		expectingSpecificProblemFor(aPath, new Problem("A", "The import w.I cannot be resolved", aPath, 18, 21, CategorizedProblem.CAT_IMPORT, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		env.removeProject(projectPath);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=520640
	public void testRemovePackageInDependencyProject() throws JavaModelException {
		IPath projectPath1 = env.addProject("Project1");
		env.addExternalJars(projectPath1, Util.getJavaClassLibs());

		IPath projectPath2 = env.addProject("Project2");
		env.addExternalJars(projectPath2, Util.getJavaClassLibs());


		env.addRequiredProject(projectPath1, projectPath2);

		env.addPackage(projectPath2, "emptypackage");

		fullBuild();
		expectingNoProblems();

		env.removePackage(projectPath2, "emptypackage");
		incrementalBuild();
		expectingNoProblems();

		env.removeProject(projectPath2);
		env.removeProject(projectPath1);
	}

	public void testBug526376() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "A",
			"package p;	\n"+
			"public class A extends B {}	\n");

		fullBuild(projectPath);

		env.addClass(root, "p", "B",
			"package p;	\n"+
			"public class B {}\n");

		incrementalBuild(projectPath);
		expectingCompilingOrder(new String[] { "/Project/src/p/B.java", "/Project/src/p/A.java" });
		expectingNoProblems();
		env.removeProject(projectPath);
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1497
	public void testBinaryRecordClass() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "16");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "R",
			"public record R(int x, int y) {}\n");

		fullBuild(projectPath);
		expectingNoProblems();

		IPath aPath = env.addClass(root, "", "X",
			"public class X extends R {}\n");

		incrementalBuild(projectPath);
		expectingSpecificProblemFor(aPath, new Problem("X", "The record R cannot be the superclass of X; a record is final and cannot be extended", aPath, 23, 24, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.removeProject(projectPath);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1497
	public void testBinaryRecordClassWithGenericSuper() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "16");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "R",
			"interface I<T> {}\n" +
			"public record R(int x, int y) implements I<String> {}\n");

		fullBuild(projectPath);
		expectingNoProblems();

		IPath aPath = env.addClass(root, "", "X",
			"public class X extends R {}\n");

		incrementalBuild(projectPath);
		expectingSpecificProblemFor(aPath, new Problem("X", "The record R cannot be the superclass of X; a record is final and cannot be extended", aPath, 23, 24, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.removeProject(projectPath);
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1497
	public void testBinaryInnerRecordClass() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "16");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "O",
				"public class O {\n" +
			    "    public static record R(int x, int y) {}\n" +
				"}\n");

		fullBuild(projectPath);
		expectingNoProblems();

		IPath aPath = env.addClass(root, "", "X",
			"public class X extends O.R {}\n");

		incrementalBuild(projectPath);
		expectingSpecificProblemFor(aPath, new Problem("X", "The record O.R cannot be the superclass of X; a record is final and cannot be extended", aPath, 23, 26, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.removeProject(projectPath);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1497
	public void testBinaryInnerRecordClassWithGenericSuper() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "16");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "O",
				"public class O {\n" +
		        "    interface I<T> {}\n" +
		        "    public static record R(int x, int y) implements I<String> {}\n" +
				"}\n");

		fullBuild(projectPath);
		expectingNoProblems();

		IPath aPath = env.addClass(root, "", "X",
			"public class X extends O.R {}\n");

		incrementalBuild(projectPath);
		expectingSpecificProblemFor(aPath, new Problem("X", "The record O.R cannot be the superclass of X; a record is final and cannot be extended", aPath, 23, 26, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.removeProject(projectPath);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577872
	// [17] Incremental builder problems with sealed types: Bogus error: The type C that implements a sealed interface Types.B should be a permitted subtype of Types.B
	public void testBug577872() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "19");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "Types",
				"""
				public class Types {
					public sealed interface I permits A, B {}
					public non-sealed interface A extends I {}
					public sealed interface B extends I permits C {}
				}
				""");

		env.addClass(root, "", "C",
				"""
				abstract class D implements Types.A {}
				public final class C extends D implements Types.B {}
				""");

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "", "C",
				"""
				abstract class D implements Types.A {}
				public final class C extends D implements Types.B {}
				""");

		incrementalBuild(projectPath);
		expectingNoProblems();

		env.removeProject(projectPath);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577872
	// [17] Incremental builder problems with sealed types: Bogus error: The type C that implements a sealed interface Types.B should be a permitted subtype of Types.B
	// Test with null analysis and external; annotations involved.
	public void testBug577872_2() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "19");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "Types",
				"""
				public class Types {
					public sealed interface I permits A, B {}
					public non-sealed interface A extends I {}
					public sealed interface B extends I permits C {}
				}
				""");

		env.addClass(root, "", "C",
				"""
				abstract class D implements Types.A {}
				public final class C extends D implements Types.B {}
				""");

		// force annotation encoding into bindings which is necessary to reproduce.
		env.getJavaProject("Project").setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		env.getJavaProject("Project").setOption(JavaCore.CORE_JAVA_BUILD_EXTERNAL_ANNOTATIONS_FROM_ALL_LOCATIONS, JavaCore.ENABLED);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "", "C",
				"""
				abstract class D implements Types.A {}
				public final class C extends D implements Types.B {}
				""");

		incrementalBuild(projectPath);
		expectingNoProblems();

		env.removeProject(projectPath);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577872
	// [17] Incremental builder problems with sealed types: Bogus error: The type C that implements a sealed interface Types.B should be a permitted subtype of Types.B
	// original test case as is.
	public void testBug577872_original() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "19");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "module-info",
				"""
				module m {}
				""");

		env.addClass(root, "p", "Types",
				"""
				package p;

				import q.C;

				public class Types {
				    public sealed interface I permits A, B {}
				    public non-sealed interface A extends I {}
				    public sealed interface B extends I permits C {}
				}
				""");

		env.addClass(root, "q", "C",
				"""
				package q;

				import p.Types.A;
				import p.Types.B;

				abstract class D implements A {}
				public final class C extends D implements B {}
				""");

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "q", "C",
				"""
				package q;

				import p.Types.A;
				import p.Types.B;

				abstract class D implements A {}
				public final class C extends D implements B {}
				""");

		incrementalBuild(projectPath);
		expectingNoProblems();

		env.removeProject(projectPath);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577872
	// [17] Incremental builder problems with sealed types: Bogus error: The type C that implements a sealed interface Types.B should be a permitted subtype of Types.B
	// original test case with null analysis and external annotations
	public void testBug577872_original_with_null_analysis() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "19");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "module-info",
				"""
				module m {}
				""");

		env.addClass(root, "p", "Types",
				"""
				package p;

				import q.C;

				public class Types {
				    public sealed interface I permits A, B {}
				    public non-sealed interface A extends I {}
				    public sealed interface B extends I permits C {}
				}
				""");

		env.addClass(root, "q", "C",
				"""
				package q;

				import p.Types.A;
				import p.Types.B;

				abstract class D implements A {}
				public final class C extends D implements B {}
				""");

		// force annotation encoding into bindings which is necessary to reproduce.
		env.getJavaProject("Project").setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		env.getJavaProject("Project").setOption(JavaCore.CORE_JAVA_BUILD_EXTERNAL_ANNOTATIONS_FROM_ALL_LOCATIONS, JavaCore.ENABLED);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "q", "C",
				"""
				package q;

				import p.Types.A;
				import p.Types.B;

				abstract class D implements A {}
				public final class C extends D implements B {}
				""");

		incrementalBuild(projectPath);
		expectingNoProblems();

		env.removeProject(projectPath);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577787
	// [17] False positive error for explicitly permitted class extending a sealed class
	public void testBug577787() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "19");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "sealedtest", "Parent",
				"""
				package sealedtest;

				public sealed class Parent permits Child {

				}
				""");

		env.addClass(root, "sealedtest", "Child",
				"""
				package sealedtest;

				public final class Child extends Parent {

				}
				""");

		// force annotation encoding into bindings which is necessary to reproduce.
		env.getJavaProject("Project").setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		env.getJavaProject("Project").setOption(JavaCore.CORE_JAVA_BUILD_EXTERNAL_ANNOTATIONS_FROM_ALL_LOCATIONS, JavaCore.ENABLED);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "sealedtest", "Child",
				"""
				package sealedtest;

				public final class Child extends Parent {

				}
				""");

		incrementalBuild(projectPath);
		expectingNoProblems();

		env.removeProject(projectPath);
	}

	public void testExhaustiveness() throws JavaModelException {
		String javaVersion = System.getProperty("java.version");
		if (javaVersion != null && JavaCore.compareJavaVersions(javaVersion, "18") < 0)
			return;

		IPath projectPath = env.addProject("Project", "18");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		IPath pathToX = env.addClass(root, "", "X",
				"""
				public class X {

					public static void main(String[] args) {
						E e = E.getE();

						String s = switch (e) {
							case A -> "A";
							case B -> "B";
							case C -> "C";
						};
						System.out.println(s);
					}
				}
				""");

		env.addClass(root, "", "E",
				"""
				public enum E {
					A, B, C;
					static E getE() {
						return C;
					}
				}
				""");

		fullBuild(projectPath);
		expectingNoProblems();
		executeClass(projectPath, "X", "C", "");

		env.addClass(root, "", "E",
				"""
				public enum E {
					A, B, C, D;
					static E getE() {
						return D;
					}
				}
				""");


		incrementalBuild(projectPath);
		expectingSpecificProblemFor(pathToX, new Problem("E", "A Switch expression should cover all possible values", pathToX, 100, 101, CategorizedProblem.CAT_SYNTAX, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		env.removeProject(projectPath);
	}

}
