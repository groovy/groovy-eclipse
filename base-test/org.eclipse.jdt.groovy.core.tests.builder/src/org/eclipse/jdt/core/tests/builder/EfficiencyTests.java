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
 * Basic efficiency tests of the image builder.
 */
public class EfficiencyTests extends BuilderTests {
	public EfficiencyTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(EfficiencyTests.class);
	}

	public void testEfficiency() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "Indicted",
			"package p1;\n"+
			"public abstract class Indicted {\n"+
			"}\n"
			);

		env.addClass(root, "p2", "Collaborator",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Collaborator extends Indicted{\n"+
			"}\n"
			);

		fullBuild(projectPath);

		env.addClass(root, "p1", "Indicted",
			"package p1;\n"+
			"public abstract class Indicted {\n"+
			"   public abstract void foo();\n"+
			"}\n"
			);

		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p2.Collaborator", "p1.Indicted"});
		expectingCompilingOrder(new String[]{"p1.Indicted", "p2.Collaborator"});
	}

	public void testMethodAddition() throws JavaModelException {

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"	}\n" +
			"}\n"
			);

		env.addClass(root, "p2", "Y",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Y extends X{\n"+
			"}\n"
			);

		env.addClass(root, "p3", "Z",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Z{\n"+
			"}\n"
			);

		fullBuild(projectPath);

		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void bar(){}	\n" +
			"	void foo() {	\n" +
			"		};	\n" +
			"	}\n" +
			"}\n"
			);

		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p2.Y"});
		expectingCompilingOrder(new String[]{"p1.X", "p2.Y" });
	}

	public void testLocalTypeAddition() throws JavaModelException {

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"	}\n" +
			"}\n"
			);

		env.addClass(root, "p2", "Y",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Y extends X{\n"+
			"}\n"
			);

		env.addClass(root, "p3", "Z",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Z{\n"+
			"}\n"
			);

		fullBuild(projectPath);

		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"		new Object(){	\n" +
			"		};	\n" +
			"	}\n" +
			"}\n"
			);

		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p1.X$1"});
		expectingCompilingOrder(new String[]{"p1.X", "p1.X$1" });
	}

	public void testLocalTypeAddition2() throws JavaModelException {

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"		new X(){	\n" +
			"			void bar(){}	\n" +
			"		};	\n" +
			"	}\n" +
			"}\n"
			);

		env.addClass(root, "p2", "Y",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Y extends X{\n"+
			"}\n"
			);

		env.addClass(root, "p3", "Z",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Z{\n"+
			"}\n"
			);

		fullBuild(projectPath);

		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"		new Object(){	\n" +
			"		};	\n" +
			"		new X(){	\n" +
			"			void bar(){}	\n" +
			"		};	\n" +
			"	}\n" +
			"}\n"
			);

		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p1.X$1", "p1.X$2"});
		expectingCompilingOrder(new String[]{"p1.X", "p1.X$1", "p1.X$2" });
	}

	public void testLocalTypeRemoval() throws JavaModelException {

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"		new Object(){	\n" +
			"		};	\n" +
			"	}\n" +
			"}\n"
			);

		env.addClass(root, "p2", "Y",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Y extends X{\n"+
			"}\n"
			);

		env.addClass(root, "p3", "Z",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Z{\n"+
			"}\n"
			);

		fullBuild(projectPath);

		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"	}\n" +
			"}\n"
			);

		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X"});
		expectingCompilingOrder(new String[]{"p1.X" });
	}

	public void testLocalTypeRemoval2() throws JavaModelException {

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"		new Object(){	\n" +
			"		};	\n" +
			"		new X(){	\n" +
			"			void bar(){}	\n" +
			"		};	\n" +
			"	}\n" +
			"}\n"
			);

		env.addClass(root, "p2", "Y",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Y extends X{\n"+
			"}\n"
			);

		env.addClass(root, "p3", "Z",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Z{\n"+
			"}\n"
			);

		fullBuild(projectPath);

		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"		new X(){	\n" +
			"			void bar(){}	\n" +
			"		};	\n" +
			"	}\n" +
			"}\n"
			);

		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p1.X$1"});
		expectingCompilingOrder(new String[]{"p1.X", "p1.X$1" });
	}
	// http://dev.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
	public void testMissingType001() throws JavaModelException {

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo(p2.Y y) {	\n" +
			"		y.bar(null);" +
			"	}\n" +
			"}\n"
			);
		env.addClass(root, "p2", "Y",
			"package p2;\n"+
			"public class Y {\n"+
			"	public void bar(Z z) {}\n" +
			"}\n"
			);
		fullBuild(projectPath);

		env.addClass(root, "p2", "Z",
			"package p2;\n"+
			"public class Z {\n"+
			"}\n"
			);

		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p2.Y","p2.Z"});
		expectingCompilingOrder(new String[]{"p2.Z", "p2.Y", "p1.X" });
	}
}
