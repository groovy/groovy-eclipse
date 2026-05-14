/*******************************************************************************
 * Copyright (c) 2020 Stephan Herrmann and others.

 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class Bug562420Test extends BuilderTests {
	public Bug562420Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(Bug562420Test.class);
	}
	public void testBuilderRegression() throws JavaModelException, Exception {
		IPath projectPath = env.addProject("Bug562420Test", "9");
		env.removePackageFragmentRoot(projectPath, "");
		IPath src = env.addPackageFragmentRoot(projectPath, "src");

		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		IPath testAppPath = env.addClass(src, "org.easylibs.test", "TestApp",
				"package org.easylibs.test;\n" +
				"public class TestApp {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"Hello World\");\n" +
				"	}\n" +
				"}\n");
		env.addFile(testAppPath.removeLastSegments(1), "package-info.java",
				"package org.easylibs.test;");
		env.addFile(src, "module-info.java",
				"module test {\n" +
				"	requires java.base;\n" +
				"	exports org.easylibs.test;\n" +
				"}\n");
		fullBuild();
		if (CompilerOptions.versionToJdkLevel(System.getProperty("java.specification.version")) < ClassFileConstants.JDK9) {
			expectingProblemsFor(projectPath,
				"Problem : java.base cannot be resolved to a module [ resource : </Bug562420Test/src/module-info.java> range : <24,33> category : <160> severity : <2>]");
		} else {
			expectingNoProblems();
		}

		env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_SOURCE, "1.8");
		env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, "1.8");
		fullBuild();
		expectingProblemsFor(projectPath,
		"""
		Problem : Syntax error on token ".", = expected [ resource : </Bug562420Test/src/module-info.java> range : <47,48> category : <20> severity : <2>]
		Problem : Syntax error on token "{", ( expected [ resource : </Bug562420Test/src/module-info.java> range : <12,13> category : <20> severity : <2>]
		Problem : Syntax error on token "}", delete this token [ resource : </Bug562420Test/src/module-info.java> range : <63,64> category : <20> severity : <2>]
		Problem : Syntax error on token(s), misplaced construct(s) [ resource : </Bug562420Test/src/module-info.java> range : <0,33> category : <20> severity : <2>]""");
	}
}