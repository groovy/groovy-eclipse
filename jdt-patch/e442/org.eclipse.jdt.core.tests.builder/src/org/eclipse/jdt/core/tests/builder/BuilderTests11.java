/*******************************************************************************
 * Copyright (c) 2022, 2026 Andrey Loskutov (loskutov@gmx.de) and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.util.Hashtable;
import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * Test tries to compile trivial snippet with --release option on Java 11 as host
 */
public class BuilderTests11 extends BuilderTests {

	public BuilderTests11(String name) {
		super(name);
	}

	public static Test suite() {
		return AbstractCompilerTest.buildUniqueComplianceTestSuite(BuilderTests11.class, ClassFileConstants.JDK11);
	}

	public void testBuildWithRelease_1_8() throws JavaModelException, Exception {
		String compliance = "1.8";
		runTest(compliance);
	}

	// TODO: this test fails in 4.25 M1, probably also before.
	// Cannot find the class file for java.lang.Object
	public void XtestBuilderWithRelease_9() throws JavaModelException, Exception {
		String compliance = "9";
		runTest(compliance);
	}

	public void testBuildWithRelease_10() throws JavaModelException, Exception {
		String compliance = "10";
		runTest(compliance);
	}

	public void testBuildWithRelease_11() throws JavaModelException, Exception {
		String compliance = "11";
		runTest(compliance);
	}

	/**
	 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5281
	 * The release option is enabled only at the workspace level (not per project).
	 * The builder must still honor it and restrict the JRT system library to the
	 * targeted release's API. Here we target release 8 and reference {@code List.of()},
	 * which was only added in Java 9 - so it must be flagged as an error.
	 * If the workspace-level release option is ignored (the bug), the full JDK 11 JRT
	 * is visible, {@code List.of()} resolves and no problem is reported.
	 */
	public void testReleaseOptionFromWorkspace() throws JavaModelException, Exception {
		Hashtable<String, String> defaultOptions = JavaCore.getOptions();
		try {
			// Enable the release option only at the workspace level
			Hashtable<String, String> wkspOptions = JavaCore.getOptions();
			wkspOptions.put(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
			JavaCore.setOptions(wkspOptions);

			// Create a project targeting release 8, WITHOUT setting the release option per project
			IPath projectPath = env.addProject("ReleaseFromWorkspace", "1.8");
			env.removePackageFragmentRoot(projectPath, "");
			IPath src = env.addPackageFragmentRoot(projectPath, "src");
			env.addExternalJars(projectPath, Util.getJavaClassLibs());
			IPath classA = env.addClass(src, "bug", "X",
					"package bug;\n" +
					"import java.util.List;\n" +
					"public class X {\n" +
					"	List<String> l = List.of(\"a\");\n" + // List.of() is Java 9+
					"}\n");
			fullBuild();

			// The Java 9+ API must not be resolvable when targeting release 8
			expectingProblemsFor(classA,
					"Problem : The method of(String) is undefined for the type List [ resource : </ReleaseFromWorkspace/src/bug/X.java> range : <76,78> category : <50> severity : <2>]");
		} finally {
			JavaCore.setOptions(defaultOptions);
		}
	}

	/**
	 * Test tries to compile trivial snippet with --release option on Java 11 as host
	 */
	private void runTest(String compliance) throws JavaModelException {
		IPath projectPath = env.addProject("BugTest", compliance);
		env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		env.removePackageFragmentRoot(projectPath, "");
		IPath src = env.addPackageFragmentRoot(projectPath, "src");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addClass(src, "bug", "A1",
				"package bug;\n" +
						"\n" +
						"public class A1 {\n" +
						"\n" +
				"}\n");
		fullBuild();
		expectingNoProblems();
	}
}
