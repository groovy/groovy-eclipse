/*******************************************************************************
 * Copyright (c) 2022, Andrey Loskutov (loskutov@gmx.de) and others.
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
