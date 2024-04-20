/*******************************************************************************
 * Copyright (c) 2020 Simeon Andreev and others.
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

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;
import junit.framework.Test;

public class Bug564905Test extends BuilderTests {

	private static final String OUTPUT_FOLDER_NAME = "bin";

	private String projectName;
	private IProject project;
	private IPath projectPath;
	private IPath src;
	private IFolder outputFolder;
	private boolean oldAutoBuilding;

	public Bug564905Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(Bug564905Test.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.projectName = "Bug564905Test";
		this.projectPath = env.addProject(this.projectName);
		this.project = env.getWorkspace().getRoot().getProject(this.projectName);
		env.addExternalJars(this.projectPath, Util.getJavaClassLibs());

		env.removePackageFragmentRoot(this.projectPath, "");
		String outputFolderPath = "tmp/" + OUTPUT_FOLDER_NAME;
		this.src = env.addPackageFragmentRoot(this.projectPath, "src", null, outputFolderPath);
		this.outputFolder = env.getWorkspace().getRoot().getFolder(this.projectPath.append(outputFolderPath));

		this.oldAutoBuilding = env.isAutoBuilding();
		env.setAutoBuilding(true);
		waitForAutoBuild();
	}

	@Override
	protected void tearDown() throws Exception {
		env.removeProject(this.projectPath);
		env.setAutoBuilding(this.oldAutoBuilding);
		waitForAutoBuild();

		super.tearDown();
	}

	/**
	 * Test for Bug 564905, with option {@code org.eclipse.jdt.core.builder.recreateModifiedClassFileInOutputFolder} enabled.
	 *
	 * When the output folder of a project is removed in file system, on refresh we expect a build.
	 */
	public void testBug564905_recreateModifiedClassFileInOutputFolder_enabled() throws Exception {
		enableOption_recreateModifiedClassFileInOutputFolder();
		assertOutputFolderEmpty();

		addSourceAndBuild();
		assertOutputFolderNotEmpty();

		deleteOutputFolderAndWaitForAutoBuild();
		// we enabled "recreateModifiedClassFileInOutputFolder", so we expect compile artifacts
		assertOutputFolderNotEmpty();
	}

	/**
	 * Test for Bug 564905, with option {@code org.eclipse.jdt.core.builder.recreateModifiedClassFileInOutputFolder} disabled.
	 *
	 * When the output folder of a project is removed in file system, on refresh we don't expect a build
	 * as we don't use the option {@link JavaCore#CORE_JAVA_BUILD_RECREATE_MODIFIED_CLASS_FILES_IN_OUTPUT_FOLDER}.
	 */
	public void testBug564905_recreateModifiedClassFileInOutputFolder_disabled() throws Exception {
		disableOption_recreateModifiedClassFileInOutputFolder();
		assertOutputFolderEmpty();

		addSourceAndBuild();
		assertOutputFolderNotEmpty();

		deleteOutputFolderAndWaitForAutoBuild();
		// we disabled "recreateModifiedClassFileInOutputFolder", so we don't expect compile artifacts
		assertOutputFolderEmpty();
	}

	private void deleteOutputFolderAndWaitForAutoBuild() throws Exception {
		// close the project, since the bug 564905 occurs when build state is read from disk
		this.project.close(new NullProgressMonitor());
		waitForAutoBuild();
		URI outputFolderUri = this.outputFolder.getLocationURI();
		// delete the output folder with file system API, so that Eclipse resources API "doesn't notice"
		deleteFolderInFileSystem(outputFolderUri);

		// re-open the project, refresh it, then wait for auto-build; expect that something was built
		this.project.open(new NullProgressMonitor());
		this.project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		waitForAutoBuild();
	}

	private void addSourceAndBuild() {
		IPath srcPackage = env.addPackage(this.src, "p");
		IFolder srcPackageFolder = env.getWorkspace().getRoot().getFolder(srcPackage);
		assertTrue("package in source must exist", srcPackageFolder.exists());
		env.addClass(this.src, "p", "X", "package p;\n public interface X { void foo() { /* we want something compiled, anything works */ } }");
		fullBuild(this.projectPath);
	}

	private void enableOption_recreateModifiedClassFileInOutputFolder() throws Exception {
		setJavaProjectOption(JavaCore.CORE_JAVA_BUILD_RECREATE_MODIFIED_CLASS_FILES_IN_OUTPUT_FOLDER, JavaCore.ENABLED);
	}

	private void disableOption_recreateModifiedClassFileInOutputFolder() throws Exception {
		setJavaProjectOption(JavaCore.CORE_JAVA_BUILD_RECREATE_MODIFIED_CLASS_FILES_IN_OUTPUT_FOLDER, JavaCore.DISABLED);
	}

	private void setJavaProjectOption(String optionName, String optionValue) throws Exception {
		IJavaProject javaProject = JavaCore.create(this.project);
		javaProject.setOption(optionName, optionValue);
		this.project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		waitForAutoBuild();
	}

	private void waitForAutoBuild() throws InterruptedException {
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, new NullProgressMonitor());
	}

	private static void deleteFolderInFileSystem(URI uri) throws IOException {
		Files.walkFileTree(Paths.get(uri), new DeleteVisitor());
	}

	private void assertOutputFolderEmpty() throws CoreException {
		assertTrue("output folder must exist", this.outputFolder.exists());
		IResource[] outputFolderContent = this.outputFolder.members();
		assertEquals("output folder must be empty, instead had contents: " + toString(outputFolderContent), 0, outputFolderContent.length);
	}

	private void assertOutputFolderNotEmpty() throws CoreException {
		assertTrue("output folder must exist", this.outputFolder.exists());
		assertTrue("output folder must not be empty", this.outputFolder.members().length > 0);
	}

	private static String toString(IResource[] resources) {
		StringBuilder result = new StringBuilder();
		for (IResource resource : resources) {
			result.append(resource.getName());
			result.append(System.lineSeparator());
		}
		return result.toString();
	}

	static class DeleteVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Files.delete(file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			if (exc != null) {
				throw exc;
			}
			Files.delete(dir);
			return FileVisitResult.CONTINUE;
		}
	}
}
