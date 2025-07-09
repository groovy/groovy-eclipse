/*******************************************************************************
 * Copyright (c) 2021 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import junit.framework.Test;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

public class Bug571363Test extends BuilderTests {

	public Bug571363Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(Bug571363Test.class);
	}

	/**
	 * Returns the OS path to the directory that contains this plugin.
	 */
	protected String getCompilerTestsPluginDirectoryPath() {
		try {
			URL platformURL = Platform.getBundle("org.eclipse.jdt.core.tests.builder").getEntry("/");
			return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void _testBug571363() throws JavaModelException, Exception {
		IPath projectPath = env.addProject("Bug571364Test", "12"); //$NON-NLS-1$
		env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_11);
		env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);

		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "test", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package test;\n" +
				"public class A {\n" +
				"	org.w3c.dom.Element list;\n" +
				"}\n" //$NON-NLS-1$
				);
		StringBuilder buffer = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		buffer.append("<classpath>\n"); //$NON-NLS-1$
		buffer.append("    <classpathentry kind=\"src\" path=\"src\"/>\n"); //$NON-NLS-1$
        buffer.append("    <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-12\">\n");
        buffer.append("        <attributes>\n");
        buffer.append("            <attribute name=\"module\" value=\"true\"/>\n");
        buffer.append("        </attributes>\n");
        buffer.append("    </classpathentry>\n");
		buffer.append("    <classpathentry kind=\"lib\" path=\"" + getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test571363.jar\"/>");
		buffer.append("    <classpathentry kind=\"output\" path=\"bin\"/>\n"); //$NON-NLS-1$
		buffer.append("</classpath>"); //$NON-NLS-1$
		env.addFile(projectPath, ".classpath", buffer.toString()); //$NON-NLS-1$
		fullBuild();

		expectingNoProblems();
	}
}
