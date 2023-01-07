/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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

import java.io.File;

import junit.framework.*;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;

public class PackageTests extends BuilderTests {

	public PackageTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(PackageTests.class);
	}

	/**
	 * Bugs 6564
	 */
	public void testNoPackageProblem() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(src, "pack", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package pack;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(src2, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(src2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Y extends p1.X {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(src2, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class Z extends p2.Y {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);


		fullBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 2
		//----------------------------
		env.removeClass(env.getPackagePath(src, "pack"), "X"); //$NON-NLS-1$ //$NON-NLS-2$
		env.removePackage(src2, "p3"); //$NON-NLS-1$

		incrementalBuild();
		expectingNoProblems();
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=251690
	 */
	public void testPackageProblem() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath aPath = env.addClass(src, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n"+ //$NON-NLS-1$
			"public class A {}" //$NON-NLS-1$
		);

		IPath bPath = env.addClass(src, "p.A", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B {}" //$NON-NLS-1$
		);

		fullBuild();
		expectingOnlySpecificProblemFor(aPath,
			new Problem("", "The type A collides with a package", aPath, 24, 25, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingOnlySpecificProblemFor(bPath,
			new Problem("", "The declared package \"\" does not match the expected package \"p.A\"", bPath, 0, 1, CategorizedProblem.CAT_INTERNAL, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testNoFolderProblem() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(src, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n"+ //$NON-NLS-1$
			"public class A {}" //$NON-NLS-1$
		);

		// create folder & contained non-java file (which don't establish package p.A!):
		env.addFolder(src, "p/A"); //$NON-NLS-1$
		env.addFile(src, "p/A/some.properties", //$NON-NLS-1$
			"name=Some\n" //$NON-NLS-1$
		);

		fullBuild();
		expectingNoProblems();
	}

	public void testNoFolderProblem2() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2");

		// create folder & contained non-java file (which don't establish package p.A!):
		env.addFolder(src, "p/A"); //$NON-NLS-1$
		env.addFile(src, "p/A/some.properties", //$NON-NLS-1$
			"name=Some\n" //$NON-NLS-1$
		);

		env.addClass(src2, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n"+ //$NON-NLS-1$
			"public class A {}" //$NON-NLS-1$
		);

		IPath project2Path = env.addProject("Project2");
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(project2Path, ""); //$NON-NLS-1$
		IPath srcP2 = env.addPackageFragmentRoot(project2Path, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addRequiredProject(project2Path, projectPath, false);

		env.addClass(srcP2, "q", "Main",
			"package q;\n" +
			"import p.A;\n" +
			"public class Main {\n" +
			"	A field;\n" +
			"}\n");

		fullBuild();
		expectingNoProblems();
	}

	public void testNestedPackageProblem() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath aPath = env.addClass(src, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n"+ //$NON-NLS-1$
			"public class A {}\n" //$NON-NLS-1$
		);

		// a class in a sub-package of p.A seems to establish package p.A, too, causing a conflict indeed:
		env.addClass(src, "p.A.c", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.A.c;\n" +
			"public class B {}\n" //$NON-NLS-1$
		);

		fullBuild();
		expectingOnlySpecificProblemFor(aPath,
			new Problem("", "The type A collides with a package", aPath, 24, 25, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
	}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=117092
// simplistic linked subfolder used as package, external case (not in workspace)
public void test001() throws CoreException {
	IPath projectPath = env.addProject("P");
	try {
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath src = env.addPackageFragmentRoot(projectPath, "src");
		IPath bin = env.setOutputFolder(projectPath, "bin");
		env.addClass(src, "p", "X",
			"package p;\n" +
			"public class X {\n" +
			"}\n"
			);
		File tmpDir = env.getTmpDirectory();
		File externalPackageDir = new File(tmpDir.getAbsolutePath() + File.separator + "q");
		externalPackageDir.mkdir();
		IFolder folder = env.getWorkspace().getRoot().getFolder(src.append("p/q"));
		folder.createLink(externalPackageDir.toURI(), 0, null);
		env.addClass(src, "p.q", "Y",
			"package p.q;\n" +
			"public class Y extends p.X {\n" +
			"}\n"
			);
		env.addClass(src, "p.q.r", "Z",
				"package p.q.r;\n" +
				"public class Z extends p.q.Y {\n" +
				"}\n"
				);
		assertTrue(new File(externalPackageDir.getAbsolutePath() +
				File.separator + "r" + File.separator + "Z.java").exists());
		fullBuild();
		expectingPresenceOf(bin.append("p/q/r/Z.class"));

		expectingNoProblems();
		env.removeClass(env.getPackagePath(src, "p.q.r"), "Z");
		env.removePackage(src, "p.q.r");
		incrementalBuild();
		expectingNoProblems();
	} finally {
		env.deleteTmpDirectory();
		env.removeProject(projectPath);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=117092
// simplistic linked subfolder used as package, internal case (in workspace)
public void test002() throws CoreException {
	IPath projectPath = env.addProject("P");
	IPath externalProjectPath = env.addProject("EP");
	try {
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath src = env.addPackageFragmentRoot(projectPath, "src");
		IPath bin = env.setOutputFolder(projectPath, "bin");
		env.addClass(src, "p", "X",
			"package p;\n" +
			"public class X {\n" +
			"}\n"
			);
		IProject externalProject = env.getProject(externalProjectPath);
		IFolder externalFolder = externalProject.getFolder("q");
		externalFolder.create(false /* no need to force */, true /*local */,
				null /* no progress monitor */);
		IFolder folder = env.getWorkspace().getRoot().getFolder(src.append("p/q"));
		folder.createLink(externalFolder.getLocationURI(), 0, null);
		env.addClass(src, "p.q", "Y",
			"package p.q;\n" +
			"public class Y extends p.X {\n" +
			"}\n"
			);
		env.addClass(src, "p.q.r", "Z",
				"package p.q.r;\n" +
				"public class Z extends p.q.Y {\n" +
				"}\n"
				);
		assertTrue(new File(externalFolder.getLocation() +
				File.separator + "r" + File.separator + "Z.java").exists());
		env.incrementalBuild(projectPath);
		expectingPresenceOf(bin.append("p/q/r/Z.class"));

		expectingNoProblems();
		env.removeClass(env.getPackagePath(src, "p.q.r"), "Z");
		env.removePackage(src, "p.q.r");
		env.incrementalBuild(projectPath);
		expectingNoProblems();
	} finally {
		env.deleteTmpDirectory();
		env.removeProject(projectPath);
		env.removeProject(externalProjectPath);
	}
}
}


