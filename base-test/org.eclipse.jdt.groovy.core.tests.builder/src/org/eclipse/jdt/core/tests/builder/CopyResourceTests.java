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

import java.util.Map;

import junit.framework.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * Basic tests of the image builder.
 */
public class CopyResourceTests extends BuilderTests {

	public CopyResourceTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(CopyResourceTests.class);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=117302
	public void testFilteredResources() throws JavaModelException {
		IPath projectPath = env.addProject("P");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src = env.addPackageFragmentRoot(
			projectPath,
			"",
			new IPath[] {new org.eclipse.core.runtime.Path("foo/;bar/")},
			new IPath[] {new org.eclipse.core.runtime.Path("foo/ignored/")},
			"bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src, "foo", "A",
			"package foo;"+
			"public class A extends bar.B {}"
		);
		env.addClass(src, "bar", "B",
			"package bar;"+
			"public class B {}"
		);
		env.addFolder(src, "foo/skip");
		IPath ignored = env.addFolder(src, "foo/ignored");
		env.addFile(ignored, "test.txt", "test file");
		env.addFile(src.append("bar"), "test.txt", "test file");

		org.eclipse.jdt.core.IJavaProject p = env.getJavaProject("P");
		Map<String, String> options = p.getOptions(true);
		options.put(org.eclipse.jdt.core.JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, "bar*");
		options.put(org.eclipse.jdt.core.JavaCore.CORE_JAVA_BUILD_RECREATE_MODIFIED_CLASS_FILES_IN_OUTPUT_FOLDER, "enabled");
		p.setOptions(options);

		int max = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		try {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1;
			fullBuild();
			expectingNoProblems();
			expectingNoPresenceOf(projectPath.append("bin/foo/skip/"));
			expectingNoPresenceOf(projectPath.append("bin/foo/ignored/"));
			expectingNoPresenceOf(projectPath.append("bin/bar/test.txt"));

			env.removeFolder(projectPath.append("bin/bar"));
			env.addClass(src, "x", "A",
				"package x;"+
				"public class A extends bar.B {}"
			);
			env.addFile(src.append("bar"), "test.txt", "changed test file");
			incrementalBuild();
			expectingNoProblems();
			expectingNoPresenceOf(projectPath.append("bin/foo/skip/"));
			expectingNoPresenceOf(projectPath.append("bin/foo/ignored/"));
			expectingNoPresenceOf(projectPath.append("bin/bar/test.txt"));
		} finally {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = max;
		}
	}

	public void testSimpleProject() throws JavaModelException {
		IPath projectPath = env.addProject("P1");
		IPath src = env.getPackageFragmentRootPath(projectPath, "");
		env.setOutputFolder(projectPath, "");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src, "z.txt", "");

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(projectPath.append("z.txt"));

		env.removeFile(src.append("z.txt"));
		IPath p = env.addFolder(src, "p");
		env.addFile(p, "p.txt", "");

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(projectPath.append("z.txt"));
		expectingPresenceOf(p.append("p.txt"));
	}

	public void testProjectWithBin() throws JavaModelException {
		IPath projectPath = env.addProject("P2");
		IPath src = env.getPackageFragmentRootPath(projectPath, "");
		env.setOutputFolder(projectPath, "bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src, "z.txt", "");

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("z.txt"),
			projectPath.append("bin/z.txt")
		});

		env.removeFile(src.append("z.txt"));
		IPath p = env.addFolder(src, "p");
		env.addFile(p, "p.txt", "");

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("z.txt"),
			projectPath.append("bin/z.txt")
		});
		expectingPresenceOf(new IPath[] {
			projectPath.append("p/p.txt"),
			projectPath.append("bin/p/p.txt")
		});
	}

	public void testProjectWithSrcBin() throws JavaModelException {
		IPath projectPath = env.addProject("P3");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src, "z.txt", "");

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("src/z.txt"),
			projectPath.append("bin/z.txt")
		});

		env.removeFile(src.append("z.txt"));
		env.addFile(src, "zz.txt", "");

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("src/z.txt"),
			projectPath.append("bin/z.txt")
		});
		expectingPresenceOf(new IPath[] {
			projectPath.append("src/zz.txt"),
			projectPath.append("bin/zz.txt")
		});
	}

	public void testProjectWith2SrcBin() throws JavaModelException {
		IPath projectPath = env.addProject("P4");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1");
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2");
		env.setOutputFolder(projectPath, "bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src1, "z.txt", "");
		env.addFile(src2, "zz.txt", "");

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("src1/z.txt"),
			projectPath.append("bin/z.txt"),
			projectPath.append("src2/zz.txt"),
			projectPath.append("bin/zz.txt")
		});

		env.removeFile(src2.append("zz.txt"));
		IPath p = env.addFolder(src2, "p");
		env.addFile(p, "p.txt", "");

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("src2/zz.txt"),
			projectPath.append("bin/zz.txt")
		});
		expectingPresenceOf(new IPath[] {
			projectPath.append("src2/p/p.txt"),
			projectPath.append("bin/p/p.txt")
		});
	}

	public void testProjectWith2SrcAsBin() throws JavaModelException {
		IPath projectPath = env.addProject("P5");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "src1");
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "src2");
		env.setOutputFolder(projectPath, "bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src1, "z.txt", "");
		env.addFile(src2, "zz.txt", "");

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("src1/z.txt"),
			projectPath.append("src2/zz.txt"),
		});
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("src2/z.txt"),
			projectPath.append("bin")
		});
	}

	public void testProjectWith2Src2Bin() throws JavaModelException {
		IPath projectPath = env.addProject("P6");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "bin1");
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "bin2");
		env.setOutputFolder(projectPath, "bin1");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src1, "z.txt", "");
		env.addFile(src2, "zz.txt", "");

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("bin1/z.txt"),
			projectPath.append("bin2/zz.txt"),
		});
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("bin1/zz.txt"),
			projectPath.append("bin2/z.txt"),
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

		env.addFile(projectPath2, "z.txt", "");

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(bin.append("z.txt"));
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=154693
	public void testBug154693() throws JavaModelException {
		IPath projectPath = env.addProject("P9");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		org.eclipse.jdt.core.IJavaProject p = env.getJavaProject("P9");
		Map<String, String> options = p.getOptions(true);
		options.put(org.eclipse.jdt.core.JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, ".svn/");
		p.setOptions(options);

		IPath folder = env.addFolder(src, "p");
		env.addFolder(folder, ".svn");
		env.addFile(folder, "A.java", "package p;\nclass A{}");

		fullBuild();
		expectingNoProblems();
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("bin/p/.svn")
		});
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=194420
	public void testBug194420() throws JavaModelException {
		IPath projectPath = env.addProject("P");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src = env.addPackageFragmentRoot(projectPath, "src");
		IPath bin = env.setOutputFolder(projectPath, "bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		IPath folder = env.addFolder(src, "p");
		String testContents = "incremental test contents";
		IPath zPath = env.addFile(folder, "z.txt", testContents);
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
			assumeEquals("File was not copied", testContents, new String(contents));
		} catch (Exception e) {
			fail("File was not copied");
		}

		java.io.File file = new java.io.File(zFile.getLocation().toOSString());
		file.delete();

		fullBuild();
		expectingNoProblems();
		expectingNoPresenceOf(zBinPath);

		testContents = "incremental test contents";
		env.addFile(folder, "z.txt", testContents);

		incrementalBuild();
		expectingNoProblems();
		expectingPresenceOf(zBinPath);
		try {
			byte[] contents = new byte[testContents.length()];
			java.io.InputStream stream = zFile.getContents();
			stream.read(contents);
			stream.close();
			assumeEquals("File was not copied", testContents, new String(contents));
		} catch (Exception e) {
			fail("File was not copied");
		}

		env.addFile(folder, "z.txt", "about to be deleted");
		file.delete();

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(zBinPath);
	}
}
