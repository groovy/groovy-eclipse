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

import junit.framework.*;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * Basic tests of the image builder.
 */
public class MultiSourceFolderAndOutputFolderTests extends BuilderTests {

	public MultiSourceFolderAndOutputFolderTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(MultiSourceFolderAndOutputFolderTests.class);
	}

	public void test0001() throws JavaModelException {
		IPath projectPath = env.addProject("P");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "bin1");
		env.setOutputFolder(projectPath, "bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src1, "", "X",
			"public class X {}"
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(projectPath.append("bin1/X.class"));
		expectingNoPresenceOf(projectPath.append("bin/X.class"));
	}

	public void test0002() throws JavaModelException {
		IPath projectPath = env.addProject("P");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "bin1");
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2");
		env.setOutputFolder(projectPath, "bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src1, "p", "X",
			"package p;"+
			"public class X {}"
			);

		env.addClass(src2, "p", "Y",
			"package p;"+
			"public class Y {}"
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(projectPath.append("bin1/p/X.class"));
		expectingPresenceOf(projectPath.append("bin/p/Y.class"));
		expectingNoPresenceOf(projectPath.append("bin/p/X.class"));
		expectingNoPresenceOf(projectPath.append("bin1/p/Y.class"));
	}

	public void test0003() {
		try {
			IPath projectPath = env.addProject("P");
			env.removePackageFragmentRoot(projectPath, "");
			env.addPackageFragmentRoot(projectPath, "src", null, null);
			env.addPackageFragmentRoot(projectPath, "src/f1", null, null);
			env.setOutputFolder(projectPath, "bin");
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			fullBuild();
			expectingNoProblems();

			assertTrue("JavaModelException", false);
		} catch (JavaModelException e) {
			assertEquals(
				"Cannot nest 'P/src/f1' inside 'P/src'. " +
				"To enable the nesting exclude 'f1/' from 'P/src'",
				e.getMessage()
			);
		}
	}

	public void test0004() {
		try {
			IPath projectPath = env.addProject("P");
			env.removePackageFragmentRoot(projectPath, "");
			env.addPackageFragmentRoot(projectPath, "src/f1", null, null);
			env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1")}, null);
			env.setOutputFolder(projectPath, "bin");
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			fullBuild();
			expectingNoProblems();

			assertTrue("JavaModelException", false);
		} catch (JavaModelException e) {
			assertEquals(
				"End exclusion filter 'f1' with / to fully exclude 'P/src/f1'",
				e.getMessage()
			);
		}
	}

	public void test0005() throws JavaModelException {
		IPath projectPath = env.addProject("P");
		env.removePackageFragmentRoot(projectPath, "");
		env.addPackageFragmentRoot(projectPath, "src/f1", null, null);
		env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1/")}, null);
		env.setOutputFolder(projectPath, "bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		fullBuild();
		expectingNoProblems();
	}

	public void test0006() throws JavaModelException {
		IPath projectPath = env.addProject("P");
		env.removePackageFragmentRoot(projectPath, "");
		IPath srcF1 = env.addPackageFragmentRoot(projectPath, "src/f1", null, null);
		IPath src = env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1/")}, null);
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src, "p", "X",
			"package p;"+
			"public class X extends p2.Y{}"
			);

		env.addClass(srcF1, "p2", "Y",
			"package p2;"+
			"public class Y {}"
			);

		fullBuild();
		expectingNoProblems();
	}

	public void test0007() throws JavaModelException {
		IPath projectPath = env.addProject("P");
		env.removePackageFragmentRoot(projectPath, "");
		IPath srcF1 = env.addPackageFragmentRoot(projectPath, "src/f1", null, null);
		IPath src = env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1/")}, null);
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		IPath xPath = env.addClass(src, "p", "X",
			"package p;"+
			"public class X extends f1.p2.Y{}"
			);

		env.addClass(srcF1, "p2", "Y",
			"package p2;"+
			"public class Y {}"
			);

		fullBuild();
		expectingOnlyProblemsFor(xPath);
	}

	public void test0008() throws JavaModelException {
		IPath projectPath = env.addProject("P");
		env.removePackageFragmentRoot(projectPath, "");
		IPath srcF1 = env.addPackageFragmentRoot(projectPath, "src/f1", null, null);
		IPath src = env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1/")}, null);
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		IPath xPath = env.addClass(src, "p", "X",
			"package p;"+
			"public class X extends p2.Y{}"
			);

		env.addClass(srcF1, "p2", "Y",
			"package p2;"+
			"public abstract class Y {"+
			"  abstract void foo();"+
			"}"
			);

		fullBuild();
		expectingOnlyProblemsFor(xPath);

		env.addClass(srcF1, "p2", "Y",
			"package p2;"+
			"public class Y {}"
			);

		incrementalBuild();

		expectingNoProblems();
	}

	public void test0009() throws JavaModelException {
		IPath projectPath = env.addProject("P");
		env.removePackageFragmentRoot(projectPath, "");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addPackageFragmentRoot(projectPath, "", null, "bin2");
		env.addFolder(projectPath, "bin");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(projectPath, "", "X",
			"public class X {}"
		);


		fullBuild();

		expectingNoProblems();
		expectingPresenceOf(projectPath.append("bin2").append("X.class"));
		expectingNoPresenceOf(projectPath.append("bin").append("X.class"));
		expectingNoPresenceOf(projectPath.append("bin2").append("bin"));
		expectingNoPresenceOf(projectPath.append("bin").append("bin2"));
	}
	public void test0010() throws JavaModelException {
		IPath projectPath = env.addProject("P");
		env.removePackageFragmentRoot(projectPath, "");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addPackageFragmentRoot(projectPath, "", new IPath[]{new Path("src/")}, "bin2");
		IPath src = env.addPackageFragmentRoot(projectPath, "src", null, null);
		env.addFolder(projectPath, "bin");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(projectPath, "", "X",
			"public class X {}"
		);

		env.addClass(src, "", "Y",
			"public class Y {}"
		);


		fullBuild();

		expectingNoProblems();
		expectingPresenceOf(projectPath.append("bin2").append("X.class"));
		expectingNoPresenceOf(projectPath.append("bin").append("X.class"));
		expectingPresenceOf(projectPath.append("bin").append("Y.class"));
		expectingNoPresenceOf(projectPath.append("bin2").append("Y.class"));
		expectingNoPresenceOf(projectPath.append("bin2").append("bin"));
		expectingNoPresenceOf(projectPath.append("bin").append("bin2"));
	}
	public void test0011() throws JavaModelException {
		IPath projectPath = env.addProject("P");
		env.removePackageFragmentRoot(projectPath, "");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addPackageFragmentRoot(projectPath, "", new IPath[]{new Path("src/")}, null);
		IPath src = env.addPackageFragmentRoot(projectPath, "src", null, "bin2");
		env.addFolder(projectPath, "bin");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(projectPath, "", "X",
			"public class X {}"
		);

		env.addClass(src, "", "Y",
			"public class Y {}"
		);


		fullBuild();

		expectingNoProblems();
		expectingPresenceOf(projectPath.append("bin").append("X.class"));
		expectingNoPresenceOf(projectPath.append("bin2").append("X.class"));
		expectingPresenceOf(projectPath.append("bin2").append("Y.class"));
		expectingNoPresenceOf(projectPath.append("bin").append("Y.class"));
		expectingNoPresenceOf(projectPath.append("bin2").append("bin"));
		expectingNoPresenceOf(projectPath.append("bin").append("bin2"));
	}

	/*
	 * Regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=119161
	 */
	public void test0012() throws JavaModelException {
		IPath projectPath = env.addProject("P");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src = env.addPackageFragmentRoot(projectPath, "", new IPath[] {new Path("p1/p2/p3/X.java"), new Path("Y.java")}, null, "");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src, "p1.p2.p3", "X",
			"package p1.p2.p3;\n" +
			"public class X {}"
		);
		fullBuild();
		expectingNoProblems();

		env.addClass(src, "", "Y",
			"import p1.p2.p3.X;\n" +
			"public class Y extends X {}"
		);
		incrementalBuild();
		expectingNoProblems();
	}

}
