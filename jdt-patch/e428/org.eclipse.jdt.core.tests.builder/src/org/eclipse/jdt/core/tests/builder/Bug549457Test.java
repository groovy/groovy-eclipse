/*******************************************************************************
 * Copyright (c) 2019 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaProject;

import junit.framework.Test;

public class Bug549457Test extends BuilderTests {

	private IPath project;
	private IPath src;
	private IPath srcPackage;
	private boolean oldAutoBuilding;

	public Bug549457Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(Bug549457Test.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.project = env.addProject("Bug549457Test");
		env.addExternalJars(this.project, Util.getJavaClassLibs());

		env.removePackageFragmentRoot(this.project, "");
		this.src = env.addPackageFragmentRoot(this.project, "src");
		this.srcPackage = env.addPackage(this.src, "p");

		this.oldAutoBuilding = env.isAutoBuilding();
		env.setAutoBuilding(true);
		waitForAutoBuild();
	}

	@Override
	protected void tearDown() throws Exception {
		env.removeProject(this.project);
		env.setAutoBuilding(this.oldAutoBuilding);
		waitForAutoBuild();
		super.tearDown();
	}

	/**
	 * Test for Bug 549457.
	 *
	 * We expect that a JDT compiler setting change triggers a build, resulting in a build problem.
	 */
	public void testBug549457() throws Exception {
		IFolder srcPackageFolder = env.getWorkspace().getRoot().getFolder(this.srcPackage);
		assertTrue("package in source must exist", srcPackageFolder.exists());

		env.addClass(this.src, "p", "X", "package p;\n public interface X { default void foo() { /* cause an error with Java 7 */ } }");

		fullBuild(this.project);
		// For this test, the default is not Java 8. If this changes, we can expect no problems here. The test cares only that the source was compiled.
		expectCompileProblem(this.project, "Default methods are allowed only at source level 1.8 or above");

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject testProject = workspaceRoot.getProject("Bug549457Test");
		IJavaProject javaProject = JavaCore.create(testProject);
		javaProject.setOption(CompilerOptions.OPTION_Compliance, "1.8");
		javaProject.setOption(CompilerOptions.OPTION_Source, "1.8");
		testProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		waitForAutoBuild();
		fullBuild(this.project);
		expectNoCompileProblems(this.project);

		String jdtCorePreferencesFile = JavaProject.DEFAULT_PREFERENCES_DIRNAME + IPath.SEPARATOR + JavaProject.JAVA_CORE_PREFS_FILE;
		IFile settingsFile = testProject.getFile(jdtCorePreferencesFile);
		assertTrue("expected \"" + jdtCorePreferencesFile + "\" to exist after setting compiler compliance to Java 1.7", settingsFile.exists());

		String newContents = String.join(
				CompilerOptions.OPTION_Compliance + "=1.7",
				CompilerOptions.OPTION_Source + "=1.7");

		settingsFile.setContents(new ByteArrayInputStream(newContents.getBytes()), IResource.FORCE, new NullProgressMonitor());
		waitForAutoBuild();
		expectCompileProblem(this.project, "Default methods are allowed only at source level 1.8 or above");
	}

	private void waitForAutoBuild() throws InterruptedException {
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, new NullProgressMonitor());
	}
}
