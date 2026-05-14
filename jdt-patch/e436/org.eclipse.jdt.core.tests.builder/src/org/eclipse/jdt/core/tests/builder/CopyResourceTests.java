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
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * Basic tests of the image builder.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CopyResourceTests extends BuilderTests {

	public CopyResourceTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(CopyResourceTests.class);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=117302
	public void testFilteredResources() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(
			projectPath,
			"", //$NON-NLS-1$
			new IPath[] {new org.eclipse.core.runtime.Path("foo/;bar/")}, //$NON-NLS-1$
			new IPath[] {new org.eclipse.core.runtime.Path("foo/ignored/")}, //$NON-NLS-1$
			"bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src, "foo", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package foo;"+ //$NON-NLS-1$
			"public class A extends bar.B {}" //$NON-NLS-1$
		);
		env.addClass(src, "bar", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package bar;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
		);
		env.addFolder(src, "foo/skip"); //$NON-NLS-1$
		IPath ignored = env.addFolder(src, "foo/ignored"); //$NON-NLS-1$
		env.addFile(ignored, "test.txt", "test file"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		env.addFile(src.append("bar"), "test.txt", "test file"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		org.eclipse.jdt.core.IJavaProject p = env.getJavaProject("P");
		java.util.Map options = p.getOptions(true);
		options.put(org.eclipse.jdt.core.JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, "bar*"); //$NON-NLS-1$
		options.put(org.eclipse.jdt.core.JavaCore.CORE_JAVA_BUILD_RECREATE_MODIFIED_CLASS_FILES_IN_OUTPUT_FOLDER, "enabled"); //$NON-NLS-1$
		p.setOptions(options);

		int max = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		try {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1;
			fullBuild();
			expectingNoProblems();
			expectingNoPresenceOf(projectPath.append("bin/foo/skip/")); //$NON-NLS-1$
			expectingNoPresenceOf(projectPath.append("bin/foo/ignored/")); //$NON-NLS-1$
			expectingNoPresenceOf(projectPath.append("bin/bar/test.txt")); //$NON-NLS-1$

			env.removeFolder(projectPath.append("bin/bar")); //$NON-NLS-1$
			env.addClass(src, "x", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package x;"+ //$NON-NLS-1$
				"public class A extends bar.B {}" //$NON-NLS-1$
			);
			env.addFile(src.append("bar"), "test.txt", "changed test file"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			incrementalBuild();
			expectingNoProblems();
			expectingNoPresenceOf(projectPath.append("bin/foo/skip/")); //$NON-NLS-1$
			expectingNoPresenceOf(projectPath.append("bin/foo/ignored/")); //$NON-NLS-1$
			expectingNoPresenceOf(projectPath.append("bin/bar/test.txt")); //$NON-NLS-1$
		} finally {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = max;
		}
	}

	public void testSimpleProject() throws JavaModelException {
		IPath projectPath = env.addProject("P1"); //$NON-NLS-1$
		IPath src = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		env.setOutputFolder(projectPath, ""); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(projectPath.append("z.txt")); //$NON-NLS-1$

		env.removeFile(src.append("z.txt")); //$NON-NLS-1$
		IPath p = env.addFolder(src, "p"); //$NON-NLS-1$
		env.addFile(p, "p.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(projectPath.append("z.txt")); //$NON-NLS-1$
		expectingPresenceOf(p.append("p.txt")); //$NON-NLS-1$
	}

	public void testProjectWithBin() throws JavaModelException {
		IPath projectPath = env.addProject("P2"); //$NON-NLS-1$
		IPath src = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("z.txt"), //$NON-NLS-1$
			projectPath.append("bin/z.txt") //$NON-NLS-1$
		});

		env.removeFile(src.append("z.txt")); //$NON-NLS-1$
		IPath p = env.addFolder(src, "p"); //$NON-NLS-1$
		env.addFile(p, "p.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("z.txt"), //$NON-NLS-1$
			projectPath.append("bin/z.txt") //$NON-NLS-1$
		});
		expectingPresenceOf(new IPath[] {
			projectPath.append("p/p.txt"), //$NON-NLS-1$
			projectPath.append("bin/p/p.txt") //$NON-NLS-1$
		});
	}

	public void testProjectWithSrcBin() throws JavaModelException {
		IPath projectPath = env.addProject("P3"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("src/z.txt"), //$NON-NLS-1$
			projectPath.append("bin/z.txt") //$NON-NLS-1$
		});

		env.removeFile(src.append("z.txt")); //$NON-NLS-1$
		env.addFile(src, "zz.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("src/z.txt"), //$NON-NLS-1$
			projectPath.append("bin/z.txt") //$NON-NLS-1$
		});
		expectingPresenceOf(new IPath[] {
			projectPath.append("src/zz.txt"), //$NON-NLS-1$
			projectPath.append("bin/zz.txt") //$NON-NLS-1$
		});
	}

	public void testProjectWith2SrcBin() throws JavaModelException {
		IPath projectPath = env.addProject("P4"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1"); //$NON-NLS-1$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src1, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$
		env.addFile(src2, "zz.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("src1/z.txt"), //$NON-NLS-1$
			projectPath.append("bin/z.txt"), //$NON-NLS-1$
			projectPath.append("src2/zz.txt"), //$NON-NLS-1$
			projectPath.append("bin/zz.txt") //$NON-NLS-1$
		});

		env.removeFile(src2.append("zz.txt")); //$NON-NLS-1$
		IPath p = env.addFolder(src2, "p"); //$NON-NLS-1$
		env.addFile(p, "p.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("src2/zz.txt"), //$NON-NLS-1$
			projectPath.append("bin/zz.txt") //$NON-NLS-1$
		});
		expectingPresenceOf(new IPath[] {
			projectPath.append("src2/p/p.txt"), //$NON-NLS-1$
			projectPath.append("bin/p/p.txt") //$NON-NLS-1$
		});
	}

	public void testProjectWith2SrcAsBin() throws JavaModelException {
		IPath projectPath = env.addProject("P5"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "src1"); //$NON-NLS-1$ //$NON-NLS-2$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "src2"); //$NON-NLS-1$ //$NON-NLS-2$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src1, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$
		env.addFile(src2, "zz.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("src1/z.txt"), //$NON-NLS-1$
			projectPath.append("src2/zz.txt"), //$NON-NLS-1$
		});
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("src2/z.txt"), //$NON-NLS-1$
			projectPath.append("bin") //$NON-NLS-1$
		});
	}

	public void testProjectWith2Src2Bin() throws JavaModelException {
		IPath projectPath = env.addProject("P6"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "bin1"); //$NON-NLS-1$ //$NON-NLS-2$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "bin2"); //$NON-NLS-1$ //$NON-NLS-2$
		env.setOutputFolder(projectPath, "bin1"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src1, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$
		env.addFile(src2, "zz.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("bin1/z.txt"), //$NON-NLS-1$
			projectPath.append("bin2/zz.txt"), //$NON-NLS-1$
		});
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("bin1/zz.txt"), //$NON-NLS-1$
			projectPath.append("bin2/z.txt"), //$NON-NLS-1$
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

		env.addFile(projectPath2, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(bin.append("z.txt")); //$NON-NLS-1$
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=154693
	public void testBug154693() throws JavaModelException {
		IPath projectPath = env.addProject("P9"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		org.eclipse.jdt.core.IJavaProject p = env.getJavaProject("P9");
		java.util.Map options = p.getOptions(true);
		options.put(org.eclipse.jdt.core.JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, ".svn/"); //$NON-NLS-1$
		p.setOptions(options);

		IPath folder = env.addFolder(src, "p");
		env.addFolder(folder, ".svn");
		env.addFile(folder, "A.java", "package p;\nclass A{}"); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("bin/p/.svn") //$NON-NLS-1$
		});
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=194420
	public void testBug194420() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		IPath folder = env.addFolder(src, "p");
		String testContents = "incremental test contents"; //$NON-NLS-1$
		IPath zPath = env.addFile(folder, "z.txt", testContents); //$NON-NLS-1$
		IPath zBinPath = bin.append("p/z.txt");
		org.eclipse.core.resources.IFile zFile = env.getWorkspace().getRoot().getFile(zPath);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(zBinPath);
		try {
			byte[] contents = new byte[testContents.length()];
			java.io.InputStream stream = zFile.getContents();
			stream.read(contents);
			stream.close();
			assumeEquals("File was not copied", testContents, new String(contents)); //$NON-NLS-1$
		} catch (Exception e) {
			fail("File was not copied"); //$NON-NLS-1$
		}

		java.io.File file = new java.io.File(zFile.getLocation().toOSString());
		file.delete();

		fullBuild();
		expectingNoProblems();
		expectingNoPresenceOf(zBinPath);

		testContents = "incremental test contents"; //$NON-NLS-1$
		env.addFile(folder, "z.txt", testContents); //$NON-NLS-1$

		incrementalBuild();
		expectingNoProblems();
		expectingPresenceOf(zBinPath);
		try {
			byte[] contents = new byte[testContents.length()];
			java.io.InputStream stream = zFile.getContents();
			stream.read(contents);
			stream.close();
			assumeEquals("File was not copied", testContents, new String(contents)); //$NON-NLS-1$
		} catch (Exception e) {
			fail("File was not copied"); //$NON-NLS-1$
		}

		env.addFile(folder, "z.txt", "about to be deleted"); //$NON-NLS-1$ //$NON-NLS-2$
		file.delete();

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(zBinPath);
	}
}
