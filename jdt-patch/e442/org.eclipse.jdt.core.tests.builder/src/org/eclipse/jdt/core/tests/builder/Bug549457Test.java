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

import junit.framework.Test;
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

		this.project = env.addProject("Bug549457Test", CompilerOptions.getFirstSupportedJavaVersion());
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

		env.addClass(this.src, "p", "A", "package p;\n sealed class A permits B {}; final class B extends A {}");

		fullBuild(this.project);
		// For this test, the default is not Java 17. If this changes, we can expect no problems here. The test cares only that the source was compiled.
		String firstSupportedJavaVersion = CompilerOptions.getFirstSupportedJavaVersion();
		String expectedProblemMessage = "'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java "
				+ firstSupportedJavaVersion + ","
		+ " Syntax error on token \"permits\", extends expected,"
		+ " Syntax error on token \"sealed\", invalid Modifiers";
		expectCompileProblem(this.project,	expectedProblemMessage);

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject testProject = workspaceRoot.getProject("Bug549457Test");
		IJavaProject javaProject = JavaCore.create(testProject);
		javaProject.setOption(CompilerOptions.OPTION_Compliance, CompilerOptions.getLatestVersion());
		javaProject.setOption(CompilerOptions.OPTION_Source, CompilerOptions.getLatestVersion());
		javaProject.setOption(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.getLatestVersion());
		testProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		waitForAutoBuild();
		fullBuild(this.project);
		expectNoCompileProblems(this.project);

		String jdtCorePreferencesFile = JavaProject.DEFAULT_PREFERENCES_DIRNAME + IPath.SEPARATOR + JavaProject.JAVA_CORE_PREFS_FILE;
		IFile settingsFile = testProject.getFile(jdtCorePreferencesFile);
		assertTrue("expected \"" + jdtCorePreferencesFile + "\" to exist after setting compiler compliance to Java " + firstSupportedJavaVersion, settingsFile.exists());

		String newContents = String.join(
				CompilerOptions.OPTION_Compliance + "=" + firstSupportedJavaVersion + "\n",
				CompilerOptions.OPTION_Source + "=" + firstSupportedJavaVersion + "\n",
				CompilerOptions.OPTION_TargetPlatform + "=" + firstSupportedJavaVersion + "\n");

		settingsFile.setContents(newContents.getBytes(), IResource.FORCE, new NullProgressMonitor());
		waitForAutoBuild();
		expectCompileProblem(this.project, expectedProblemMessage);
	}

	private void waitForAutoBuild() throws InterruptedException {
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, new NullProgressMonitor());
	}
}
