/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * Basic execution tests of the image builder.
 */
public class ExecutionTests extends BuilderTests {
	public ExecutionTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(ExecutionTests.class);
	}

	public void testSuccess() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "Hello",
			"package p1;\n"+
			"public class Hello {\n"+
			"   public static void main(String args[]) {\n"+
			"      System.out.print(\"Hello world\");\n"+
			"   }\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
		executeClass(projectPath, "p1.Hello", "Hello world", "");
	}

	public void testFailure() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		IPath helloPath = env.addClass(root, "p1", "Hello",
			"package p1;\n"+
			"public class Hello {\n"+
			"   public static void main(String args[]) {\n"+
			"      System.out.println(\"Hello world\")\n"+
			"   }\n"+
			"}\n"
			);
		// public static void main(String args[]) {
		//    System.out.println("Hello world") <-- missing ";"
		// }

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(helloPath);
		executeClass(projectPath, "p1.Hello", "",
			"java.lang.Error: Unresolved compilation problem: \n" +
			"	Syntax error, insert \";\" to complete BlockStatements\n"
		);
	}
}
