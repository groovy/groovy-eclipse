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
import org.eclipse.jdt.internal.core.builder.AbstractImageBuilder;

public class Bug561287Test extends BuilderTests {
	public Bug561287Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(Bug561287Test.class);
	}
	public void testBuilderRegression() throws JavaModelException, Exception {
		int maxAtOnce = AbstractImageBuilder.MAX_AT_ONCE;
		AbstractImageBuilder.MAX_AT_ONCE = 2;
		try {
			IPath projectPath = env.addProject("Bug561287Test", "1.8");
			env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
			env.removePackageFragmentRoot(projectPath, "");
			IPath src = env.addPackageFragmentRoot(projectPath, "src");

			env.addExternalJars(projectPath, Util.getJavaClassLibs());
			env.addClass(src, "bug561287", "A0",
					"package bug561287;\n" +
					"import bug561287.sub.B0;\n" +
					"\n" +
					"public class A0 {\n" +
					"    B0 b;\n" +
					"}\n");
			env.addClass(src, "bug561287", "A1",
					"package bug561287;\n" +
					"\n" +
					"public class A1 {\n" +
					"\n" +
					"}\n");
			env.addClass(src, "bug561287/sub", "B0",
					"package bug561287.sub;\n" +
					"\n" +
					"public class B0 {\n" +
					"\n" +
					"}\n" +
					"\n");
			env.addFolder(src, "bug561287/sub/directory");
			env.addFile(src, "bug561287/sub/directory/test.txt", "");
			fullBuild();

			expectingNoProblems();
		} finally {
			AbstractImageBuilder.MAX_AT_ONCE = maxAtOnce;
		}
	}
}