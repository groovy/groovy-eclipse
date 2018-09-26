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

import static org.junit.Assert.assertArrayEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;
import org.junit.internal.ArrayComparisonFailure;

import junit.framework.Test;

public class Bug530366Test extends BuilderTests {

	private IPath project;
	private IPath src;
	private IPath somePackage;

	public Bug530366Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(Bug530366Test.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.project = env.addProject("TestProjectBug530366");
		env.addExternalJars(this.project, Util.getJavaClassLibs());

		env.removePackageFragmentRoot(this.project, "");
		this.src = env.addPackageFragmentRoot(this.project, "src");
		this.somePackage = env.addPackage(this.src, "somepackage");
	}

	@Override
	protected void tearDown() throws Exception {
		env.removeProject(this.project);

		super.tearDown();
	}

	/**
	 * Test for Bug 530366: Changing class name in source leads to missing inner class compilation artifact
	 *
	 * Given classes:
	 *
	 * <ul>
	 * <li>MyClass1, MyClass1$InnerClass, both defined in MyClass1.java</li>
	 * <li>MyClass2, MyClass2$InnerClass, both defined in MyClass2.java</li>
	 * </ul>
	 *
	 * Changing the name of the class in MyClass2.java inside a Java editor, from MyClass2 to MyClass1,
	 * results in overwriting the compiled .class file for MyClass1$InnerClass.
	 *
	 * Changing the name of the class in MyClass2.java inside a Java editor, from MyClass1 back to MyClass2,
	 * results in a missing .class file for MyClass1$InnerClass.
	 */
	public void testBug530366() throws Exception {
		defineNestingClass("MyClass1");
		fullBuild();
		expectingNoProblems();

		IProject testProject = ResourcesPlugin.getWorkspace().getRoot().getProject("TestProjectBug530366");
		IFile myClass1InnerClass = testProject.getFile("bin/somepackage/MyClass1$InnerClass.class");

		URI compilationArtifactUri = myClass1InnerClass.getLocationURI();
		byte[] expectedContents = Files.readAllBytes(Paths.get(compilationArtifactUri));

		String sourceName = "MyClass2";
		String className = "MyClass1"; // deliberately mismatched source and class names
		IPath myClass2 = defineNestingClass(sourceName, className);

		incrementalBuild();
		expectProblems(myClass2);

		byte[] actualContents = Files.readAllBytes(Paths.get(compilationArtifactUri));

		assertEqualContents(expectedContents, actualContents);

		redefineNestingClass("MyClass2");
		incrementalBuild();
		expectingNoProblems();

		assertTrue("Java builder removed compilation artifact, but should not have",
				myClass1InnerClass.exists());
	}

	private IPath redefineNestingClass(String className) {
		env.removeClass(this.somePackage, className);
		return defineNestingClass(className);
	}

	private IPath defineNestingClass(String className) {
		return defineNestingClass(className, className);
	}

	private IPath defineNestingClass(String sourceName, String className) {
		String classContents = String.join("\n"
				, "package somepackage;"
				, ""
				, "public class " + className + " {"
				, ""
				, "    public static class InnerClass {}"
				, "}"
		);
		IPath source = env.addClass(this.src, "somepackage", sourceName, classContents);
		return source;
	}

	private void expectProblems(IPath myClass2) {
		Problem mismatchedSource = new Problem("", "The public type MyClass1 must be defined in its own file", myClass2, 35, 43, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR);
		Problem alreadyDefined = new Problem("", "The type MyClass1 is already defined", myClass2, 35, 43, -1, IMarker.SEVERITY_ERROR);
		Problem[] expectedProblems = { mismatchedSource, alreadyDefined };
		expectingOnlySpecificProblemsFor(myClass2, expectedProblems);
	}

	private void assertEqualContents(byte[] expectedContents, byte[] actualContents) throws ArrayComparisonFailure {
		String failMessage =
				String.join(System.lineSeparator()
						, "Java builder overwrote existing class file, but should not have"
						, "expected class file contents: "
						, new String(expectedContents)
						, "actual class file contents: "
						, new String(actualContents)
				);
		assertArrayEquals(failMessage, expectedContents, actualContents);
	}
}
