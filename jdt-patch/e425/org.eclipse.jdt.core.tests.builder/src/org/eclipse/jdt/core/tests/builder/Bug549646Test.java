/*******************************************************************************
 * Copyright (c) 2019 Sebastian Zarnekow and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sebastian Zarnekow - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class Bug549646Test extends BuilderTests {
	public Bug549646Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(Bug549646Test.class);
	}

	public void testCompilerRegression() throws JavaModelException, Exception {
		IPath projectPath = env.addProject("Bug549646Test", "10"); //$NON-NLS-1$
		env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);

		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addClass(projectPath, "test", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package test;\n" +
				"import java.util.Map;\n" +
				"import java.util.Map.Entry;\n" +
				"public class A {\n" +
				"	void a(Map<String,String> a) {\n" +
				"		for (Entry<String, String> iterableElement : a.entrySet()) {\n" +
				"			iterableElement.toString();\n" +
				"		}\n" +
				"	}\n" +
				"}\n" //$NON-NLS-1$
				);
		env.addClass(projectPath, "test", "B", //$NON-NLS-1$ //$NON-NLS-2$
				"package test;\n" +
				"import java.util.HashMap;\n" +
				"public class B {\n" +
				"	void test() {\n" +
				"		new A().a(new HashMap<String, String>());\n" +
				"	}\n" +
				"}\n" //$NON-NLS-1$
				);
		fullBuild();

		boolean isJRE11 = CompilerOptions.VERSION_11.equals(System.getProperty("java.specification.version"));
		if (isJRE11 && env.getProblemsFor(projectPath).length > 0) {
			// bogus class lookup (ignoring modules) due to insufficient data in ct.sym (non-deterministically triggers the below problems)
			// see also https://bugs.eclipse.org/549647
			expectingProblemsFor(projectPath,
					"Problem : Entry cannot be resolved to a type [ resource : </Bug549646Test/test/A.java> range : <120,125> category : <40> severity : <2>]\n" +
					"Problem : The type java.util.Map.Entry is not visible [ resource : </Bug549646Test/test/A.java> range : <43,62> category : <40> severity : <2>]\n" +
					"Problem : Type mismatch: cannot convert from element type Map.Entry<String,String> to Entry [ resource : </Bug549646Test/test/A.java> range : <160,172> category : <40> severity : <2>]");
		} else {
			expectingNoProblems();
		}
	}
}
