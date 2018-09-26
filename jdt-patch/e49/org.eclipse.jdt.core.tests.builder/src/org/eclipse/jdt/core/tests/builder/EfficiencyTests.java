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

/**
 * Basic efficiency tests of the image builder.
 */
public class EfficiencyTests extends BuilderTests {
	public EfficiencyTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(EfficiencyTests.class);
	}

	public void testProjectAsClassFolder() throws JavaModelException {
		IPath projectPath1 = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(projectPath1, Util.getJavaClassLibs());

		IPath projectPath2 = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(projectPath2, Util.getJavaClassLibs());
		env.addClassFolder(projectPath2, projectPath1, false);

		env.addClass(projectPath2, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public abstract class X {}\n" //$NON-NLS-1$
		);

		env.addClass(projectPath2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Y {}\n" //$NON-NLS-1$
		);

		fullBuild();

		env.addClass(projectPath2, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath2);

		// if a full build happens instead of an incremental, then both types will be recompiled
		expectingCompiledClasses(new String[]{"p1.X"}); //$NON-NLS-1$
	}

	public void testEfficiency() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Indicted", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public abstract class Indicted {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root, "p2", "Collaborator", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Collaborator extends Indicted{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild(projectPath);

		env.addClass(root, "p1", "Indicted", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public abstract class Indicted {\n"+ //$NON-NLS-1$
			"   public abstract void foo();\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p2.Collaborator", "p1.Indicted"}); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(new String[] { "/Project/src/p1/Indicted.java", "/Project/src/p2/Collaborator.java" });
	}

	public void testMethodAddition() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Y extends X{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Z{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild(projectPath);

		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void bar(){}	\n" + //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p2.Y"}); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(new String[] { "/Project/src/p1/X.java", "/Project/src/p2/Y.java" });
	}

	public void testLocalTypeAddition() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Y extends X{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Z{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild(projectPath);

		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		new Object(){	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p1.X$1"}); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(new String[] { "/Project/src/p1/X.java" });
	}

	public void testLocalTypeAddition2() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		new X(){	\n" + //$NON-NLS-1$
			"			void bar(){}	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Y extends X{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Z{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild(projectPath);

		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		new Object(){	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"		new X(){	\n" + //$NON-NLS-1$
			"			void bar(){}	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p1.X$1", "p1.X$2"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		expectingCompilingOrder(new String[] { "/Project/src/p1/X.java" });
	}

	public void testLocalTypeRemoval() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		new Object(){	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Y extends X{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Z{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild(projectPath);

		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X"}); //$NON-NLS-1$
		expectingCompilingOrder(new String[] { "/Project/src/p1/X.java" });
	}

	public void testLocalTypeRemoval2() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		new Object(){	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"		new X(){	\n" + //$NON-NLS-1$
			"			void bar(){}	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Y extends X{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Z{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild(projectPath);

		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		new X(){	\n" + //$NON-NLS-1$
			"			void bar(){}	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p1.X$1"}); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(new String[] { "/Project/src/p1/X.java" });
	}
	// http://dev.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
	public void testMissingType001() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo(p2.Y y) {	\n" + //$NON-NLS-1$
			"		y.bar(null);" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.addClass(root, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Y {\n"+ //$NON-NLS-1$
			"	public void bar(Z z) {}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		fullBuild(projectPath);

		env.addClass(root, "p2", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Z {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p2.Y","p2.Z"}); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(
				new String[] { "/Project/src/p2/Z.java", "/Project/src/p2/Y.java", "/Project/src/p1/X.java" });
	}
}
