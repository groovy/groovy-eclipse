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
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		IPath bin1 = env.setOutputFolder(projectPath, "bin1");

		env.addClass(root, "p", "Test",
			"package p;\n" +
			"public class Test {}"
		);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(bin1.append("p/Test.class"));

		IPath bin2 = env.setOutputFolder(projectPath, "bin2");
		incrementalBuild();
		expectingNoProblems();
		expectingPresenceOf(bin2.append("p/Test.class"));
	}

	public void testDeleteOutputFolder() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		IPath root = env.getPackageFragmentRootPath(projectPath, "");
		IPath bin = env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "Test",
			"public class Test {}"
		);
		env.addFile(root, "Test.txt", "");

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[]{
			bin.append("Test.class"),
			bin.append("Test.txt")
		});

		env.removeFolder(bin);
//		incrementalBuild(); currently not detected by the incremental builder... should it?
		fullBuild();
		expectingPresenceOf(new IPath[]{
			bin.append("Test.class"),
			bin.append("Test.txt")
		});
	}
	/*
	 * Ensures that changing the output to be the project (when the project has a source folder src)
	 * doesn't scrub the project on exit/restart.
	 * (regression test for bug 32588 Error saving changed source files; all files in project deleted)
	 */
	public void testInvalidOutput() throws JavaModelException {
		// setup project with 1 src folder and 1 output folder
		IPath projectPath = env.addProject("P");
		env.removePackageFragmentRoot(projectPath, "");
		env.addPackageFragmentRoot(projectPath, "src");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// add cu and build
		env.addClass(projectPath, "src", "A",
			"public class A {}"
			);
		fullBuild();
		expectingNoProblems();

		// set invalid  output foder by editing the .classpath file
		env.addFile(
			projectPath,
			".classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" path=\"src\"/>\n" +
			"    <classpathentry kind=\"var\" path=\"" + Util.getJavaClassLibs() + "\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>"
		);

		// simulate exit/restart
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		JavaProject project = (JavaProject)manager.getJavaModel().getJavaProject("P");
		manager.removePerProjectInfo(project,true);

		// change cu and build
		IPath cuPath = env.addClass(projectPath, "src", "A",
			"public class A { String s;}"
			);
		incrementalBuild();

		expectingPresenceOf(new IPath[] {cuPath});
	}

	public void testSimpleProject() throws JavaModelException {
		IPath projectPath = env.addProject("P1");
		IPath bin = env.setOutputFolder(projectPath, "");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(projectPath, "", "A",
			"public class A {}"
			);
		env.addClass(projectPath, "p", "B",
			"package p;"+
			"public class B {}"
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			bin.append("A.class"),
			bin.append("p/B.class")
		});
	}

	public void testProjectWithBin() throws JavaModelException {
		IPath projectPath = env.addProject("P2");
		IPath src = env.getPackageFragmentRootPath(projectPath, "");
		IPath bin = env.setOutputFolder(projectPath, "bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src, "", "A",
			"public class A {}"
			);
		env.addClass(src, "p", "B",
			"package p;"+
			"public class B {}"
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			bin.append("A.class"),
			bin.append("p/B.class")
		});
	}

	public void testProjectWithSrcBin() throws JavaModelException {
		IPath projectPath = env.addProject("P3");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src = env.addPackageFragmentRoot(projectPath, "src");
		IPath bin = env.setOutputFolder(projectPath, "bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src, "", "A",
			"public class A {}"
			);
		env.addClass(src, "p", "B",
			"package p;"+
			"public class B {}"
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			bin.append("A.class"),
			bin.append("p/B.class")
		});
	}

	public void testProjectWith2SrcBin() throws JavaModelException {
		IPath projectPath = env.addProject("P4");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1");
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2");
		IPath bin = env.setOutputFolder(projectPath, "bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src1, "", "A",
			"public class A {}"
			);
		env.addClass(src2, "p", "B",
			"package p;"+
			"public class B {}"
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			bin.append("A.class"),
			bin.append("p/B.class")
		});
	}

	public void testProjectWith2SrcAsBin() throws JavaModelException {
		IPath projectPath = env.addProject("P5");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "src1");
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "src2");
		/*IPath bin =*/ env.setOutputFolder(projectPath, "bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src1, "", "A",
			"public class A {}"
			);
		env.addClass(src2, "p", "B",
			"package p;"+
			"public class B {}"
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			src1.append("A.class"),
			src2.append("p/B.class")
		});
	}

	public void testProjectWith2Src2Bin() throws JavaModelException {
		IPath projectPath = env.addProject("P6");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "bin1");
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "bin2");
		env.setOutputFolder(projectPath, "bin1");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src1, "", "A",
			"public class A {}"
			);
		env.addClass(src2, "p", "B",
			"package p;"+
			"public class B {}"
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("bin1/A.class"),
			projectPath.append("bin2/p/B.class")
		});
	}

	public void testProjectWith3Src2Bin() throws JavaModelException {
		IPath projectPath = env.addProject("P6");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "bin1");
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "bin2");
		IPath src3 = env.addPackageFragmentRoot(projectPath, "src3", null, "bin2");
		env.setOutputFolder(projectPath, "bin1");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src1, "", "A",
			"public class A {}"
			);
		env.addClass(src2, "p", "B",
			"package p;"+
			"public class B {}"
			);
		env.addClass(src3, "", "C",
			"public class C {}"
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("bin1/A.class"),
			projectPath.append("bin2/p/B.class"),
			projectPath.append("bin2/C.class")
		});
	}

	public void test2ProjectWith1Bin() throws JavaModelException {
		IPath projectPath = env.addProject("P7");
		env.removePackageFragmentRoot(projectPath, "");
		env.addPackageFragmentRoot(projectPath, "src");
		IPath bin = env.setOutputFolder(projectPath, "bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		IPath projectPath2 = env.addProject("P8");
		IPath binLocation = env.getProject(projectPath).getFolder("bin").getLocation();
		env.setExternalOutputFolder(projectPath2, "externalBin", binLocation);
		env.addExternalJars(projectPath2, Util.getJavaClassLibs());
		env.addRequiredProject(projectPath2, projectPath);

		env.addClass(projectPath2, "p", "B",
			"package p;"+
			"public class B {}"
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(bin.append("p/B.class"));
	}
}
