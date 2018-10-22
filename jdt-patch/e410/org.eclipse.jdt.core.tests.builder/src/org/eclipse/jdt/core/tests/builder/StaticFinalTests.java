/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

public class StaticFinalTests extends BuilderTests {

	public StaticFinalTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(StaticFinalTests.class);
	}

	public void testBoolean() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final boolean VAR = true; }" //$NON-NLS-1$
		);

		env.addClass(projectPath, "p1", "Main", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class Main {\n" + //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(A.VAR);\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "true", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final boolean VAR = false; }" //$NON-NLS-1$
		);

		incrementalBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "false", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testByte() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final byte VAR = (byte) 0; }" //$NON-NLS-1$
		);

		env.addClass(projectPath, "p1", "Main", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class Main {\n" + //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(A.VAR);\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "0", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final byte VAR = (byte) 1; }" //$NON-NLS-1$
		);

		incrementalBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "1", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testChar() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final String VAR = \"Hello\"; }" //$NON-NLS-1$
		);

		env.addClass(projectPath, "p1", "Main", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class Main {\n" + //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(A.VAR);\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "Hello", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final String VAR = \"Bye\"; }" //$NON-NLS-1$
		);

		incrementalBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "Bye", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testDouble() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final double VAR = (double) 2; }" //$NON-NLS-1$
		);

		env.addClass(projectPath, "p1", "Main", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class Main {\n" + //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(A.VAR);\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "2", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final double VAR = (double) 3; }" //$NON-NLS-1$
		);

		incrementalBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "3", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testFloat() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final float VAR = (float) 4; }" //$NON-NLS-1$
		);

		env.addClass(projectPath, "p1", "Main", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class Main {\n" + //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(A.VAR);\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "4", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final float VAR = (float) 5; }" //$NON-NLS-1$
		);

		incrementalBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "5", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testInt() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final int VAR = (int) 6; }" //$NON-NLS-1$
		);

		env.addClass(projectPath, "p1", "Main", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class Main {\n" + //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(A.VAR);\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "6", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final int VAR = (int) 7; }" //$NON-NLS-1$
		);

		incrementalBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "7", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testLong() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final long VAR = (long) 8; }" //$NON-NLS-1$
		);

		env.addClass(projectPath, "p1", "Main", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class Main {\n" + //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(A.VAR);\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "8", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final long VAR = (long) 9; }" //$NON-NLS-1$
		);

		incrementalBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "9", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testShort() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final short VAR = (short) 10; }" //$NON-NLS-1$
		);

		env.addClass(projectPath, "p1", "Main", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class Main {\n" + //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(A.VAR);\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "10", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final short VAR = (short) 11; }" //$NON-NLS-1$
		);

		incrementalBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "11", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testString() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final String VAR = \"Hello\"; }" //$NON-NLS-1$
		);

		env.addClass(projectPath, "p1", "Main", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class Main {\n" + //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(A.VAR);\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "Hello", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		env.addClass(projectPath, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public class A { public static final String VAR = \"Bye\"; }" //$NON-NLS-1$
		);

		incrementalBuild();
		expectingNoProblems();
		executeClass(projectPath, "p1.Main", "Bye", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
