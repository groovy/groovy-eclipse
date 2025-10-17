/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "bin1"); //$NON-NLS-1$ //$NON-NLS-2$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src1, "", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"public class X {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(projectPath.append("bin1/X.class")); //$NON-NLS-1$
		expectingNoPresenceOf(projectPath.append("bin/X.class")); //$NON-NLS-1$
	}

	public void test0002() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "bin1"); //$NON-NLS-1$ //$NON-NLS-2$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src1, "p", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class X {}" //$NON-NLS-1$
			);

		env.addClass(src2, "p", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class Y {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(projectPath.append("bin1/p/X.class")); //$NON-NLS-1$
		expectingPresenceOf(projectPath.append("bin/p/Y.class")); //$NON-NLS-1$
		expectingNoPresenceOf(projectPath.append("bin/p/X.class")); //$NON-NLS-1$
		expectingNoPresenceOf(projectPath.append("bin1/p/Y.class")); //$NON-NLS-1$
	}

	public void test0003() {
		try {
			IPath projectPath = env.addProject("P"); //$NON-NLS-1$
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
			env.addPackageFragmentRoot(projectPath, "src", null, null); //$NON-NLS-1$
			env.addPackageFragmentRoot(projectPath, "src/f1", null, null); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			fullBuild();
			expectingNoProblems();

			assertTrue("JavaModelException", false); //$NON-NLS-1$
		} catch (JavaModelException e) {
			assertEquals(
				"Cannot nest 'P/src/f1' inside 'P/src'. " + //$NON-NLS-1$
				"To enable the nesting exclude 'f1/' from 'P/src'", //$NON-NLS-1$
				e.getMessage()
			);
		}
	}

	public void test0004() {
		try {
			IPath projectPath = env.addProject("P"); //$NON-NLS-1$
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
			env.addPackageFragmentRoot(projectPath, "src/f1", null, null); //$NON-NLS-1$
			env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1")}, null); //$NON-NLS-1$ //$NON-NLS-2$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			fullBuild();
			expectingNoProblems();

			assertTrue("JavaModelException", false); //$NON-NLS-1$
		} catch (JavaModelException e) {
			assertEquals(
				"End exclusion filter 'f1' with / to fully exclude 'P/src/f1'", //$NON-NLS-1$
				e.getMessage()
			);
		}
	}

	public void test0005() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		env.addPackageFragmentRoot(projectPath, "src/f1", null, null); //$NON-NLS-1$
		env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1/")}, null); //$NON-NLS-1$ //$NON-NLS-2$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		fullBuild();
		expectingNoProblems();
	}

	public void test0006() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath srcF1 = env.addPackageFragmentRoot(projectPath, "src/f1", null, null); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1/")}, null); //$NON-NLS-1$ //$NON-NLS-2$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src, "p", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class X extends p2.Y{}" //$NON-NLS-1$
			);

		env.addClass(srcF1, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;"+ //$NON-NLS-1$
			"public class Y {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
	}

	public void test0007() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath srcF1 = env.addPackageFragmentRoot(projectPath, "src/f1", null, null); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1/")}, null); //$NON-NLS-1$ //$NON-NLS-2$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		IPath xPath = env.addClass(src, "p", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class X extends f1.p2.Y{}" //$NON-NLS-1$
			);

		env.addClass(srcF1, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;"+ //$NON-NLS-1$
			"public class Y {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingOnlyProblemsFor(xPath);
	}

	public void test0008() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath srcF1 = env.addPackageFragmentRoot(projectPath, "src/f1", null, null); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1/")}, null); //$NON-NLS-1$ //$NON-NLS-2$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		IPath xPath = env.addClass(src, "p", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class X extends p2.Y{}" //$NON-NLS-1$
			);

		env.addClass(srcF1, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;"+ //$NON-NLS-1$
			"public abstract class Y {"+ //$NON-NLS-1$
			"  abstract void foo();"+ //$NON-NLS-1$
			"}" //$NON-NLS-1$
			);

		fullBuild();
		expectingOnlyProblemsFor(xPath);

		env.addClass(srcF1, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;"+ //$NON-NLS-1$
			"public class Y {}" //$NON-NLS-1$
			);

		incrementalBuild();

		expectingNoProblems();
	}

	public void test0009() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addPackageFragmentRoot(projectPath, "", null, "bin2"); //$NON-NLS-1$ //$NON-NLS-2$
		env.addFolder(projectPath, "bin"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(projectPath, "", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"public class X {}" //$NON-NLS-1$
		);


		fullBuild();

		expectingNoProblems();
		expectingPresenceOf(projectPath.append("bin2").append("X.class")); //$NON-NLS-1$ //$NON-NLS-2$
		expectingNoPresenceOf(projectPath.append("bin").append("X.class")); //$NON-NLS-1$ //$NON-NLS-2$
		expectingNoPresenceOf(projectPath.append("bin2").append("bin")); //$NON-NLS-1$ //$NON-NLS-2$
		expectingNoPresenceOf(projectPath.append("bin").append("bin2")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void test0010() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addPackageFragmentRoot(projectPath, "", new IPath[]{new Path("src/")}, "bin2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		IPath src = env.addPackageFragmentRoot(projectPath, "src", null, null); //$NON-NLS-1$ //$NON-NLS-2$
		env.addFolder(projectPath, "bin"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(projectPath, "", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"public class X {}" //$NON-NLS-1$
		);

		env.addClass(src, "", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"public class Y {}" //$NON-NLS-1$
		);


		fullBuild();

		expectingNoProblems();
		expectingPresenceOf(projectPath.append("bin2").append("X.class")); //$NON-NLS-1$ //$NON-NLS-2$
		expectingNoPresenceOf(projectPath.append("bin").append("X.class")); //$NON-NLS-1$ //$NON-NLS-2$
		expectingPresenceOf(projectPath.append("bin").append("Y.class")); //$NON-NLS-1$ //$NON-NLS-2$
		expectingNoPresenceOf(projectPath.append("bin2").append("Y.class")); //$NON-NLS-1$ //$NON-NLS-2$
		expectingNoPresenceOf(projectPath.append("bin2").append("bin")); //$NON-NLS-1$ //$NON-NLS-2$
		expectingNoPresenceOf(projectPath.append("bin").append("bin2")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void test0011() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addPackageFragmentRoot(projectPath, "", new IPath[]{new Path("src/")}, null); //$NON-NLS-1$ //$NON-NLS-2$
		IPath src = env.addPackageFragmentRoot(projectPath, "src", null, "bin2"); //$NON-NLS-1$ //$NON-NLS-2$
		env.addFolder(projectPath, "bin"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(projectPath, "", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"public class X {}" //$NON-NLS-1$
		);

		env.addClass(src, "", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"public class Y {}" //$NON-NLS-1$
		);


		fullBuild();

		expectingNoProblems();
		expectingPresenceOf(projectPath.append("bin").append("X.class")); //$NON-NLS-1$ //$NON-NLS-2$
		expectingNoPresenceOf(projectPath.append("bin2").append("X.class")); //$NON-NLS-1$ //$NON-NLS-2$
		expectingPresenceOf(projectPath.append("bin2").append("Y.class")); //$NON-NLS-1$ //$NON-NLS-2$
		expectingNoPresenceOf(projectPath.append("bin").append("Y.class")); //$NON-NLS-1$ //$NON-NLS-2$
		expectingNoPresenceOf(projectPath.append("bin2").append("bin")); //$NON-NLS-1$ //$NON-NLS-2$
		expectingNoPresenceOf(projectPath.append("bin").append("bin2")); //$NON-NLS-1$ //$NON-NLS-2$
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
