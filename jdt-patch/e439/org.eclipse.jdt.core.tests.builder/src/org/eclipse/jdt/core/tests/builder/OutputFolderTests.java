/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;

/**
 * Basic tests of the image builder.
 */
public class OutputFolderTests extends BuilderTests {

	public OutputFolderTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(OutputFolderTests.class);
	}

	public void testChangeOutputFolder() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath bin1 = env.setOutputFolder(projectPath, "bin1"); //$NON-NLS-1$

		env.addClass(root, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" + //$NON-NLS-1$
			"public class Test {}" //$NON-NLS-1$
		);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(bin1.append("p/Test.class")); //$NON-NLS-1$

		IPath bin2 = env.setOutputFolder(projectPath, "bin2"); //$NON-NLS-1$
		incrementalBuild();
		expectingNoProblems();
		expectingPresenceOf(bin2.append("p/Test.class")); //$NON-NLS-1$
	}

	public void testDeleteOutputFolder() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		IPath root = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"public class Test {}" //$NON-NLS-1$
		);
		env.addFile(root, "Test.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[]{
			bin.append("Test.class"), //$NON-NLS-1$
			bin.append("Test.txt") //$NON-NLS-1$
		});

		env.removeFolder(bin);
//		incrementalBuild(); currently not detected by the incremental builder... should it?
		fullBuild();
		expectingPresenceOf(new IPath[]{
			bin.append("Test.class"), //$NON-NLS-1$
			bin.append("Test.txt") //$NON-NLS-1$
		});
	}
	/*
	 * Ensures that changing the output to be the project (when the project has a source folder src)
	 * doesn't scrub the project on exit/restart.
	 * (regression test for bug 32588 Error saving changed source files; all files in project deleted)
	 */
	public void testInvalidOutput() throws JavaModelException {
		// setup project with 1 src folder and 1 output folder
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// add cu and build
		env.addClass(projectPath, "src", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		fullBuild();
		expectingNoProblems();

		// set invalid  output foder by editing the .classpath file
		env.addFile(
			projectPath,
			".classpath",  //$NON-NLS-1$
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //$NON-NLS-1$
			"<classpath>\n" + //$NON-NLS-1$
			"    <classpathentry kind=\"src\" path=\"src\"/>\n" + //$NON-NLS-1$
			"    <classpathentry kind=\"var\" path=\"" + Util.getJavaClassLibs() + "\"/>\n" + //$NON-NLS-1$ //$NON-NLS-2$
			"    <classpathentry kind=\"output\" path=\"\"/>\n" + //$NON-NLS-1$
			"</classpath>" //$NON-NLS-1$
		);

		// simulate exit/restart
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		JavaProject project = manager.getJavaModel().getJavaProject("P"); //$NON-NLS-1$
		manager.removePerProjectInfo(project, true /* remove external jar files indexes and timestamps*/);

		// change cu and build
		IPath cuPath = env.addClass(projectPath, "src", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A { String s;}" //$NON-NLS-1$
			);
		incrementalBuild();

		expectingPresenceOf(new IPath[] {cuPath});
	}

	public void testSimpleProject() throws JavaModelException {
		IPath projectPath = env.addProject("P1"); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, ""); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(projectPath, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		env.addClass(projectPath, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			bin.append("A.class"), //$NON-NLS-1$
			bin.append("p/B.class") //$NON-NLS-1$
		});
	}

	public void testProjectWithBin() throws JavaModelException {
		IPath projectPath = env.addProject("P2"); //$NON-NLS-1$
		IPath src = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		env.addClass(src, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			bin.append("A.class"), //$NON-NLS-1$
			bin.append("p/B.class") //$NON-NLS-1$
		});
	}

	public void testProjectWithSrcBin() throws JavaModelException {
		IPath projectPath = env.addProject("P3"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		env.addClass(src, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			bin.append("A.class"), //$NON-NLS-1$
			bin.append("p/B.class") //$NON-NLS-1$
		});
	}

	public void testProjectWith2SrcBin() throws JavaModelException {
		IPath projectPath = env.addProject("P4"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1"); //$NON-NLS-1$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2"); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		env.addClass(src2, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			bin.append("A.class"), //$NON-NLS-1$
			bin.append("p/B.class") //$NON-NLS-1$
		});
	}

	public void testProjectWith2SrcAsBin() throws JavaModelException {
		IPath projectPath = env.addProject("P5"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "src1"); //$NON-NLS-1$ //$NON-NLS-2$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "src2"); //$NON-NLS-1$ //$NON-NLS-2$
		/*IPath bin =*/ env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		env.addClass(src2, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			src1.append("A.class"), //$NON-NLS-1$
			src2.append("p/B.class") //$NON-NLS-1$
		});
	}

	public void testProjectWith2Src2Bin() throws JavaModelException {
		IPath projectPath = env.addProject("P6"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "bin1"); //$NON-NLS-1$ //$NON-NLS-2$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "bin2"); //$NON-NLS-1$ //$NON-NLS-2$
		env.setOutputFolder(projectPath, "bin1"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		env.addClass(src2, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("bin1/A.class"), //$NON-NLS-1$
			projectPath.append("bin2/p/B.class") //$NON-NLS-1$
		});
	}

	public void testProjectWith3Src2Bin() throws JavaModelException {
		IPath projectPath = env.addProject("P6"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "bin1"); //$NON-NLS-1$ //$NON-NLS-2$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "bin2"); //$NON-NLS-1$ //$NON-NLS-2$
		IPath src3 = env.addPackageFragmentRoot(projectPath, "src3", null, "bin2"); //$NON-NLS-1$ //$NON-NLS-2$
		env.setOutputFolder(projectPath, "bin1"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		env.addClass(src2, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);
		env.addClass(src3, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("bin1/A.class"), //$NON-NLS-1$
			projectPath.append("bin2/p/B.class"), //$NON-NLS-1$
			projectPath.append("bin2/C.class") //$NON-NLS-1$
		});
	}

	public void test2ProjectWith1Bin() throws JavaModelException {
		IPath projectPath = env.addProject("P7"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		IPath projectPath2 = env.addProject("P8"); //$NON-NLS-1$
		IPath binLocation = env.getProject(projectPath).getFolder("bin").getLocation(); //$NON-NLS-1$
		env.setExternalOutputFolder(projectPath2, "externalBin", binLocation); //$NON-NLS-1$
		env.addExternalJars(projectPath2, Util.getJavaClassLibs());
		env.addRequiredProject(projectPath2, projectPath);

		env.addClass(projectPath2, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(bin.append("p/B.class")); //$NON-NLS-1$
	}
}
