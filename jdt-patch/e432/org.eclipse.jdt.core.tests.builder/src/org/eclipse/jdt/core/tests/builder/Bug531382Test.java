/*******************************************************************************
 * Copyright (c) 2018 Simeon Andreev and others.
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.tests.builder.participants.TestCompilationParticipant2;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.builder.AbstractImageBuilder;

import junit.framework.Test;

public class Bug531382Test extends BuilderTests {

	private IPath project;
	private IPath src;
	private IPath srcPackage;

	private int previousLimit;

	public Bug531382Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(Bug531382Test.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.project = env.addProject("TestProjectBug531382");
		env.addExternalJars(this.project, Util.getJavaClassLibs());

		env.removePackageFragmentRoot(this.project, "");
		this.src = env.addPackageFragmentRoot(this.project, "src");
		this.srcPackage = env.addPackage(this.src, "p");

		/*
		 * We can work with the limit "as is", however that would mean creating a lot of classes.
		 * To improve test time we set the limit to a small number and then restore it once the test is done.
		 *
		 * This improvement can be removed if the field is to be hidden or made final.
		 */
		this.previousLimit = AbstractImageBuilder.MAX_AT_ONCE;
		AbstractImageBuilder.MAX_AT_ONCE = 42;
	}

	@Override
	protected void tearDown() throws Exception {
		TestCompilationParticipant2.PARTICIPANT = null;

		AbstractImageBuilder.MAX_AT_ONCE = this.previousLimit;

		env.removeProject(this.project);

		super.tearDown();
	}

	/**
	 * Test for Bug 531382.
	 *
	 * We create {@link AbstractImageBuilder#MAX_AT_ONCE} sources (e.g. 2000 sources).
	 *
	 * A build participant generates one more source during the build.
	 *
	 * We expect that this generated source is also compiled after the build.
	 * To check this we generate the source with an error for it, and we check for the error.
	 */
	public void testBug531382() throws Exception {
		IFolder srcPackageFolder = env.getWorkspace().getRoot().getFolder(this.srcPackage);
		assertTrue("package in source must exist", srcPackageFolder.exists());

		for (int i = 0; i < AbstractImageBuilder.MAX_AT_ONCE; ++i) {
			env.addClass(this.src, "p", "X" + i, "package p;\n public class X" + i + " {}");
		}

		final IFile generatedFile = srcPackageFolder.getFile("Generated.java");
		final String contents = "package p;\n public class NameMismatch {}";

		class GenerateBrokenSource extends CompilationParticipant {
			public void buildStarting(BuildContext[] files, boolean isBatch) {
				if (files.length > 0 && !generatedFile.exists()) {
					BuildContext context = files[0];
					createFile(generatedFile, contents);
					IFile[] generatedFiles = { generatedFile };
					context.recordAddedGeneratedFiles(generatedFiles);
				}
			}
		}
		// Creating this sets the build participant singleton.
		TestCompilationParticipant2.PARTICIPANT = new GenerateBrokenSource();

		assertFalse("source to be generated from build participant should not exist before build", generatedFile.exists());
		fullBuild(this.project);
		assertTrue("expected source to be generated from build participant", generatedFile.exists());

		expectCompileProblem(this.project, "The public type NameMismatch must be defined in its own file");
	}

	protected void createFile(IFile generatedFile, String contents) {
		boolean force = true;
		IProgressMonitor monitor = new NullProgressMonitor();
		try {
			generatedFile.create(new ByteArrayInputStream(contents.getBytes()), force, monitor);
			generatedFile.setDerived(true, monitor);
		} catch (CoreException e) {
			throw new AssertionError("failed to generate file in build participant", e);
		}
	}
}
