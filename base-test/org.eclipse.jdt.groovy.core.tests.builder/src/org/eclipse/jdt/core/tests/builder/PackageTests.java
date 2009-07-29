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

import java.io.File;

import junit.framework.*;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
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
	public void testPackageProblem() throws JavaModelException {
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


