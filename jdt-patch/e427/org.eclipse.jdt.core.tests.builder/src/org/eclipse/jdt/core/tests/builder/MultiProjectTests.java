/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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

import java.util.Hashtable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.test.OrderedTestSuite;

import junit.framework.Test;


@SuppressWarnings({"rawtypes", "unchecked"})
public class MultiProjectTests extends BuilderTests {

	public MultiProjectTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new OrderedTestSuite(MultiProjectTests.class);
	}

	public void testCompileOnlyDependent() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B extends A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project3
			//----------------------------
		IPath project3Path = env.addProject("Project3"); //$NON-NLS-1$
		env.addExternalJars(project3Path, Util.getJavaClassLibs());
		IPath root3 = env.getPackageFragmentRootPath(project3Path, ""); //$NON-NLS-1$
		env.addClass(root3, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {\n"+ //$NON-NLS-1$
			"   int x;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.waitForManualRefresh();
		incrementalBuild();
		env.waitForAutoBuild();
		expectingCompiledClasses(new String[]{"A", "B"}); //$NON-NLS-1$ //$NON-NLS-2$
		env.removeProject(project1Path);
		env.removeProject(project2Path);
		env.removeProject(project3Path);
	}

	// 14103 - avoid recompiling unaffected sources in dependent projects
	public void testCompileOnlyStructuralDependent() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.addClass(root1, "", "Unreferenced", //$NON-NLS-1$ //$NON-NLS-2$
			"public class Unreferenced {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B extends A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project3
			//----------------------------
		IPath project3Path = env.addProject("Project3"); //$NON-NLS-1$
		env.addExternalJars(project3Path, Util.getJavaClassLibs());
		IPath root3 = env.getPackageFragmentRootPath(project3Path, ""); //$NON-NLS-1$
		env.addClass(root3, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 2
		//----------------------------
		// non-structural change should not fool dependent projcts
		env.addClass(root1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {\n"+ //$NON-NLS-1$
			"   // add comment (non-structural change)\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		// structural change, but no actual dependents
		env.addClass(root1, "", "Unreferenced", //$NON-NLS-1$ //$NON-NLS-2$
			"public class Unreferenced {\n"+ //$NON-NLS-1$
			"   int x; //structural change\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.waitForManualRefresh();
		incrementalBuild();
		env.waitForAutoBuild();
		expectingCompiledClasses(new String[]{"A", "Unreferenced"}); //$NON-NLS-1$ //$NON-NLS-2$
		env.removeProject(project1Path);
		env.removeProject(project2Path);
		env.removeProject(project3Path);
	}

	public void testRemoveField() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
		JavaCore.setOptions(options);

		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {\n"+ //$NON-NLS-1$
			"   public int x;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		IPath b = env.addClass(root2, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B {\n"+ //$NON-NLS-1$
			"   public void foo(){\n"+ //$NON-NLS-1$
			"      int x = new A().x;\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.waitForManualRefresh();
		incrementalBuild();
		env.waitForAutoBuild();
		expectingSpecificProblemFor(b, new Problem("B.foo()", "x cannot be resolved or is not a field", b, 61, 62, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		env.removeProject(project1Path);
		env.removeProject(project2Path);
	}

	public void testCompileOrder() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING);

		JavaCore.setOptions(newOptions);

		//----------------------------
		//         Project1
		//----------------------------
		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		env.addExternalJars(p1, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
		env.setOutputFolder(p1, "bin"); //$NON-NLS-1$

		IPath c1 = env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"  W w;\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		//----------------------------
		//         Project2
		//----------------------------
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$
		env.addExternalJars(p2, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		env.setOutputFolder(p2, "bin"); //$NON-NLS-1$

		IPath c2 = env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Y {\n"+ //$NON-NLS-1$
			"  W w;\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		//----------------------------
		//         Project3
		//----------------------------
		IPath p3 = env.addProject("P3"); //$NON-NLS-1$
		env.addExternalJars(p3, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
		IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
		env.setOutputFolder(p3, "bin"); //$NON-NLS-1$

		IPath c3 = env.addClass(root3, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class Z {\n"+ //$NON-NLS-1$
			"  W w;\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.setBuildOrder(new String[]{"P1", "P3", "P2"});//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();

		expectingCompilingOrder(new String[] { "/P1/src/p1/X.java", "/P3/src/p3/Z.java", "/P2/src/p2/Y.java" });
		IPath workspaceRootPath = env.getWorkspaceRootPath();
		expectingOnlySpecificProblemsFor(workspaceRootPath,new Problem[]{
				new Problem("p3", "W cannot be resolved to a type", c3, 31, 32, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p2", "W cannot be resolved to a type", c2, 31, 32, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p1", "W cannot be resolved to a type", c1, 31, 32, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)//$NON-NLS-1$ //$NON-NLS-2$
		});
		JavaCore.setOptions(options);
		env.removeProject(p1);
		env.removeProject(p2);
		env.removeProject(p3);
	}

	public void testCyclesCleared() throws JavaModelException {
		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$
		try {
			env.addRequiredProject(p1, p2);
			env.addRequiredProject(p2, p1);

			fullBuild();
			env.waitForAutoBuild();

			printProblems();
			expectingOnlySpecificProblemsFor(p1, new Problem[] {
					new Problem("p1",
							"One or more cycles were detected in the build path of project 'P1'. The paths towards the cycle and cycle are:\n" +
							"->{P1, P2}",
							p1, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR),
					new Problem("p1",
							"The project cannot be built until build path errors are resolved",
							p1, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR)
			});
			expectingOnlySpecificProblemsFor(p2, new Problem[] {
					new Problem("p2",
							"One or more cycles were detected in the build path of project 'P2'. The paths towards the cycle and cycle are:\n" +
							"->{P1, P2}",
							p2, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR),
					new Problem("p2",
							"The project cannot be built until build path errors are resolved",
							p2, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR)
			});

			env.removeRequiredProject(p1, p2);

			fullBuild();
			env.waitForAutoBuild();

			expectingNoProblems();
		} finally {
			env.removeProject(p1);
			env.removeProject(p2);
		}
	}

	public void testCycle1() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING);

		JavaCore.setOptions(newOptions);

		//----------------------------
		//         Project1
		//----------------------------
		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		env.addExternalJars(p1, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
		env.setOutputFolder(p1, "bin"); //$NON-NLS-1$

		env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"import p2.Y;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"  public void bar(Y y){\n"+ //$NON-NLS-1$
			"    y.zork();\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		//----------------------------
		//         Project2
		//----------------------------
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$
		env.addExternalJars(p2, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		env.setOutputFolder(p2, "bin"); //$NON-NLS-1$

		env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.X;\n"+ //$NON-NLS-1$
			"import p3.Z;\n"+ //$NON-NLS-1$
			"public class Y extends Z{\n"+ //$NON-NLS-1$
			"  public X zork(){\n"+ //$NON-NLS-1$
			"    X x = foo();\n"+ //$NON-NLS-1$
			"    x.bar(this);\n"+ //$NON-NLS-1$
			"    return x;\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		//----------------------------
		//         Project3
		//----------------------------
		IPath p3 = env.addProject("P3"); //$NON-NLS-1$
		env.addExternalJars(p3, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
		IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
		env.setOutputFolder(p3, "bin"); //$NON-NLS-1$

		env.addClass(root3, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"import p1.X;\n"+ //$NON-NLS-1$
			"public class Z {\n"+ //$NON-NLS-1$
			"  public X foo(){\n"+ //$NON-NLS-1$
			"    return null;\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		// for Project1
		env.addRequiredProject(p1, p2);
		env.addRequiredProject(p1, p3);
		// for Project2
		env.addRequiredProject(p2, p1);
		env.addRequiredProject(p2, p3);
		// for Project3
		env.addRequiredProject(p3, p1);

		try {
			env.setBuildOrder(new String[]{"P1", "P2", "P3"});//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			env.waitForManualRefresh();
			fullBuild();
			env.waitForAutoBuild();

			expectingCompilingOrder(new String[] { "/P1/src/p1/X.java", "/P2/src/p2/Y.java", "/P3/src/p3/Z.java",
					"/P1/src/p1/X.java", "/P2/src/p2/Y.java", "/P1/src/p1/X.java", "/P3/src/p3/Z.java" });
			expectingOnlySpecificProblemFor(p1, new Problem("p1",
					"One or more cycles were detected in the build path of project 'P1'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2}\n" +
					"->{P1, P2, P3}\n" +
					"->{P1, P3}",
					p1, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING));
			expectingOnlySpecificProblemFor(p2,new Problem("p2",
					"One or more cycles were detected in the build path of project 'P2'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2}\n" +
					"->{P1, P2, P3}",
					p2, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING));
			expectingOnlySpecificProblemFor(p3,new Problem("p3",
					"One or more cycles were detected in the build path of project 'P3'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2, P3}\n" +
					"->{P1, P3}",
					p3, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING));

			JavaCore.setOptions(options);
		} finally {
			env.setBuildOrder(null);
			env.removeProject(p1);
			env.removeProject(p2);
			env.removeProject(p3);
		}
	}

	public void testCycle2() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING);

		JavaCore.setOptions(newOptions);

		//----------------------------
		//         Project1
		//----------------------------
		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		env.addExternalJars(p1, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
		env.setOutputFolder(p1, "bin"); //$NON-NLS-1$

		env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"import p2.Y;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"  public void bar(Y y, int i){\n"+ //$NON-NLS-1$
			"    y.zork();\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		//----------------------------
		//         Project2
		//----------------------------
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$
		env.addExternalJars(p2, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		env.setOutputFolder(p2, "bin"); //$NON-NLS-1$

		IPath c2 = env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.X;\n"+ //$NON-NLS-1$
			"import p3.Z;\n"+ //$NON-NLS-1$
			"public class Y extends Z{\n"+ //$NON-NLS-1$
			"  public X zork(){\n"+ //$NON-NLS-1$
			"    X x = foo();\n"+ //$NON-NLS-1$
			"    x.bar(this);\n"+ //$NON-NLS-1$
			"    return x;\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		//----------------------------
		//         Project3
		//----------------------------
		IPath p3 = env.addProject("P3"); //$NON-NLS-1$
		env.addExternalJars(p3, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
		IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
		env.setOutputFolder(p3, "bin"); //$NON-NLS-1$

		env.addClass(root3, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"import p1.X;\n"+ //$NON-NLS-1$
			"public class Z {\n"+ //$NON-NLS-1$
			"  public X foo(){\n"+ //$NON-NLS-1$
			"    return null;\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		// for Project1
		env.addRequiredProject(p1, p2);
		env.addRequiredProject(p1, p3);
		// for Project2
		env.addRequiredProject(p2, p1);
		env.addRequiredProject(p2, p3);
		// for Project3
		env.addRequiredProject(p3, p1);

		try {
			env.setBuildOrder(new String[]{"P1", "P2", "P3"});//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
			env.waitForManualRefresh();
			fullBuild();
			env.waitForAutoBuild();
			expectingCompilingOrder(new String[] { "/P1/src/p1/X.java", "/P2/src/p2/Y.java", "/P3/src/p3/Z.java",
					"/P1/src/p1/X.java", "/P2/src/p2/Y.java", "/P1/src/p1/X.java", "/P3/src/p3/Z.java" });
			expectingOnlySpecificProblemFor(p1,new Problem("p1",
					"One or more cycles were detected in the build path of project 'P1'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2}\n" +
					"->{P1, P2, P3}\n" +
					"->{P1, P3}",
					p1, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING));
			expectingOnlySpecificProblemsFor(p2,new Problem[]{
					new Problem("p2", "The method bar(Y, int) in the type X is not applicable for the arguments (Y)", c2, 106, 109, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR),//$NON-NLS-1$ //$NON-NLS-2$
					new Problem("p2",
							"One or more cycles were detected in the build path of project 'P2'. The paths towards the cycle and cycle are:\n" +
							"->{P1, P2}\n" +
							"->{P1, P2, P3}",
							p2, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING)
			});
			expectingOnlySpecificProblemFor(p3,new Problem("p3",
					"One or more cycles were detected in the build path of project 'P3'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2, P3}\n" +
					"->{P1, P3}", p3, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING));

			JavaCore.setOptions(options);
		} finally {
			env.setBuildOrder(null);
			env.removeProject(p1);
			env.removeProject(p2);
			env.removeProject(p3);
		}
	}

	public void testCycle3() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING);

		JavaCore.setOptions(newOptions);

		//----------------------------
		//         Project1
		//----------------------------
		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		env.addExternalJars(p1, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
		env.setOutputFolder(p1, "bin"); //$NON-NLS-1$

		env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"import p2.Y;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"  public void bar(Y y){\n"+ //$NON-NLS-1$
			"    y.zork();\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		//----------------------------
		//         Project2
		//----------------------------
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$
		env.addExternalJars(p2, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		env.setOutputFolder(p2, "bin"); //$NON-NLS-1$

		IPath c2 = env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.X;\n"+ //$NON-NLS-1$
			"import p3.Z;\n"+ //$NON-NLS-1$
			"public class Y extends Z{\n"+ //$NON-NLS-1$
			"  public X zork(){\n"+ //$NON-NLS-1$
			"    X x = foo();\n"+ //$NON-NLS-1$
			"    x.bar(this);\n"+ //$NON-NLS-1$
			"    return x;\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		//----------------------------
		//         Project3
		//----------------------------
		IPath p3 = env.addProject("P3"); //$NON-NLS-1$
		env.addExternalJars(p3, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
		IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
		env.setOutputFolder(p3, "bin"); //$NON-NLS-1$

		env.addClass(root3, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"import p1.X;\n"+ //$NON-NLS-1$
			"public class Z {\n"+ //$NON-NLS-1$
			"  public X foo(){\n"+ //$NON-NLS-1$
			"    return null;\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		// for Project1
		env.addRequiredProject(p1, p2);
		env.addRequiredProject(p1, p3);
		// for Project2
		env.addRequiredProject(p2, p1);
		env.addRequiredProject(p2, p3);
		// for Project3
		env.addRequiredProject(p3, p1);

		try {
			env.setBuildOrder(new String[]{"P1", "P2", "P3"});//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
			env.waitForManualRefresh();
			fullBuild();
			env.waitForAutoBuild();

			expectingCompilingOrder(new String[] { "/P1/src/p1/X.java", "/P2/src/p2/Y.java", "/P3/src/p3/Z.java",
					"/P1/src/p1/X.java", "/P2/src/p2/Y.java", "/P1/src/p1/X.java", "/P3/src/p3/Z.java" });
			expectingOnlySpecificProblemFor(p1,new Problem("p1",
					"One or more cycles were detected in the build path of project 'P1'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2}\n" +
					"->{P1, P2, P3}\n" +
					"->{P1, P3}",
					p1, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING));
			expectingOnlySpecificProblemFor(p2,new Problem("p2",
					"One or more cycles were detected in the build path of project 'P2'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2}\n" +
					"->{P1, P2, P3}",
					p2, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING));
			expectingOnlySpecificProblemFor(p3,new Problem("p3",
					"One or more cycles were detected in the build path of project 'P3'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2, P3}\n" +
					"->{P1, P3}",
					p3, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING));

			env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
				"package p1;\n"+ //$NON-NLS-1$
				"import p2.Y;\n"+ //$NON-NLS-1$
				"public class X {\n"+ //$NON-NLS-1$
				"  public void bar(Y y, int i){\n"+ //$NON-NLS-1$
				"    y.zork();\n"+ //$NON-NLS-1$
				"  }\n"+ //$NON-NLS-1$
				"}\n" //$NON-NLS-1$
				);
			incrementalBuild();
			env.waitForAutoBuild();
			expectingCompilingOrder(new String[] { "/P1/src/p1/X.java", "/P2/src/p2/Y.java", "/P3/src/p3/Z.java" });
			expectingOnlySpecificProblemFor(p1,new Problem("p1",
					"One or more cycles were detected in the build path of project 'P1'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2}\n" +
					"->{P1, P2, P3}\n" +
					"->{P1, P3}",
					p1, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING));
			expectingOnlySpecificProblemsFor(p2,new Problem[]{
					new Problem("p2", "The method bar(Y, int) in the type X is not applicable for the arguments (Y)", c2, 106, 109, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR),//$NON-NLS-1$ //$NON-NLS-2$
					new Problem("p2",
							"One or more cycles were detected in the build path of project 'P2'. The paths towards the cycle and cycle are:\n" +
							"->{P1, P2}\n" +
							"->{P1, P2, P3}",
							p2, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING)
			});
			expectingOnlySpecificProblemFor(p3,new Problem("p3",
					"One or more cycles were detected in the build path of project 'P3'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2, P3}\n" +
					"->{P1, P3}",
					p3, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING));

			JavaCore.setOptions(options);
		} finally {
			env.setBuildOrder(null);
			env.removeProject(p1);
			env.removeProject(p2);
			env.removeProject(p3);
		}
	}
	public void testCycle4() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING);

		JavaCore.setOptions(newOptions);

		//----------------------------
		//         Project1
		//----------------------------
		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		env.addExternalJars(p1, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
		env.setOutputFolder(p1, "bin"); //$NON-NLS-1$

		//----------------------------
		//         Project2
		//----------------------------
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$
		env.addExternalJars(p2, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		env.setOutputFolder(p2, "bin"); //$NON-NLS-1$

		IPath c2 = env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.X;\n"+ //$NON-NLS-1$
			"import p3.Z;\n"+ //$NON-NLS-1$
			"public class Y extends Z{\n"+ //$NON-NLS-1$
			"  public X zork(){\n"+ //$NON-NLS-1$
			"    X x = foo();\n"+ //$NON-NLS-1$
			"    x.bar(this);\n"+ //$NON-NLS-1$
			"    return x;\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		//----------------------------
		//         Project3
		//----------------------------
		IPath p3 = env.addProject("P3"); //$NON-NLS-1$
		env.addExternalJars(p3, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
		IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
		env.setOutputFolder(p3, "bin"); //$NON-NLS-1$

		IPath c3 = env.addClass(root3, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"import p1.X;\n"+ //$NON-NLS-1$
			"public class Z {\n"+ //$NON-NLS-1$
			"  public X foo(){\n"+ //$NON-NLS-1$
			"    return null;\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		// for Project1
		env.addRequiredProject(p1, p2);
		env.addRequiredProject(p1, p3);
		// for Project2
		env.addRequiredProject(p2, p1);
		env.addRequiredProject(p2, p3);
		// for Project3
		env.addRequiredProject(p3, p1);

		try {
			env.setBuildOrder(new String[]{"P1", "P2", "P3"});//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
			env.waitForManualRefresh();
			fullBuild();
			env.waitForAutoBuild();

			expectingCompilingOrder(new String[] { "/P2/src/p2/Y.java", "/P3/src/p3/Z.java", "/P2/src/p2/Y.java" });
			expectingOnlySpecificProblemFor(p1,new Problem("p1",
					"One or more cycles were detected in the build path of project 'P1'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2}\n" +
					"->{P1, P2, P3}\n" +
					"->{P1, P3}",
					p1, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING));
			expectingOnlySpecificProblemsFor(p2,new Problem[]{
				new Problem("p2", "X cannot be resolved to a type", c2, 87, 88, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p2", "The method foo() from the type Z refers to the missing type X", c2, 93, 96, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p2", "The import p1 cannot be resolved", c2, 19, 21, CategorizedProblem.CAT_IMPORT, IMarker.SEVERITY_ERROR),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p2", "X cannot be resolved to a type", c2, 73, 74, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p2",
						"One or more cycles were detected in the build path of project 'P2'. The paths towards the cycle and cycle are:\n" +
						"->{P1, P2}\n" +
						"->{P1, P2, P3}",
						p2, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING)
			});
			expectingOnlySpecificProblemsFor(p3,new Problem[]{
				new Problem("p3", "X cannot be resolved to a type", c3, 51, 52, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p3", "The import p1 cannot be resolved", c3, 19, 21, CategorizedProblem.CAT_IMPORT, IMarker.SEVERITY_ERROR),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p3",
						"One or more cycles were detected in the build path of project 'P3'. The paths towards the cycle and cycle are:\n" +
						"->{P1, P2, P3}\n" +
						"->{P1, P3}",
						p3, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING)
			});

			env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
				"package p1;\n"+ //$NON-NLS-1$
				"import p2.Y;\n"+ //$NON-NLS-1$
				"public class X {\n"+ //$NON-NLS-1$
				"  public void bar(Y y){\n"+ //$NON-NLS-1$
				"    y.zork();\n"+ //$NON-NLS-1$
				"  }\n"+ //$NON-NLS-1$
				"}\n" //$NON-NLS-1$
				);
			incrementalBuild();
			env.waitForAutoBuild();
			expectingCompilingOrder(new String[] { "/P1/src/p1/X.java", "/P2/src/p2/Y.java", "/P3/src/p3/Z.java",
					"/P1/src/p1/X.java", "/P2/src/p2/Y.java" });
			expectingOnlySpecificProblemFor(p1,new Problem("p1",
					"One or more cycles were detected in the build path of project 'P1'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2}\n" +
					"->{P1, P2, P3}\n" +
					"->{P1, P3}",
					p1, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING));
			expectingOnlySpecificProblemFor(p2,new Problem("p2",
					"One or more cycles were detected in the build path of project 'P2'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2}\n" +
					"->{P1, P2, P3}",
					p2, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING));
			expectingOnlySpecificProblemFor(p3,new Problem("p3",
					"One or more cycles were detected in the build path of project 'P3'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2, P3}\n" +
					"->{P1, P3}",
					p3, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING));

			JavaCore.setOptions(options);
		} finally {
			env.setBuildOrder(null);
			env.removeProject(p1);
			env.removeProject(p2);
			env.removeProject(p3);
		}
	}

	public void testCycle5() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING); // $NON-NLS-1$

		JavaCore.setOptions(newOptions);

		// ----------------------------
		// Project1
		// ----------------------------
		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		env.addExternalJars(p1, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
		env.setOutputFolder(p1, "bin"); //$NON-NLS-1$

		IPath c1 = env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
				"package p1;\n" + //$NON-NLS-1$
						"import p2.*;\n" + //$NON-NLS-1$
						"import p22.*;\n" + //$NON-NLS-1$
						"public class X {\n" + //$NON-NLS-1$
						"  Y y;\n" + //$NON-NLS-1$
						"}\n" //$NON-NLS-1$
		);

		// ----------------------------
		// Project2
		// ----------------------------
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$
		env.addExternalJars(p2, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		env.setOutputFolder(p2, "bin"); //$NON-NLS-1$

		IPath c2 = env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
				"package p2;\n" + //$NON-NLS-1$
						"import p1.*;\n" + //$NON-NLS-1$
						"import p11.*;\n" + //$NON-NLS-1$
						"public class Y {\n" + //$NON-NLS-1$
						"  X x;\n" + //$NON-NLS-1$
						"}\n" //$NON-NLS-1$
		);

		// for Project1
		env.addRequiredProject(p1, p2);
		// for Project2
		env.addRequiredProject(p2, p1);

		try {
			env.setBuildOrder(new String[] { "P1", "P2" });//$NON-NLS-1$ //$NON-NLS-2$
			env.waitForManualRefresh();
			fullBuild();
			env.waitForAutoBuild();

			expectingCompilingOrder(new String[] { "/P1/src/p1/X.java", "/P2/src/p2/Y.java", "/P1/src/p1/X.java",
					"/P2/src/p2/Y.java" });
			expectingOnlySpecificProblemsFor(p1, new Problem[] {
					new Problem("p1", "The import p22 cannot be resolved", c1, 32, 35, CategorizedProblem.CAT_IMPORT, //$NON-NLS-1$ //$NON-NLS-2$
							IMarker.SEVERITY_ERROR), new Problem("p1",
							"One or more cycles were detected in the build path of project 'P1'. The paths towards the cycle and cycle are:\n" +
							"->{P1, P2}",
							p1, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING) });
			expectingOnlySpecificProblemsFor(p2, new Problem[] {
					new Problem("p2", "The import p11 cannot be resolved", c2, 32, 35, CategorizedProblem.CAT_IMPORT, //$NON-NLS-1$ //$NON-NLS-2$
							IMarker.SEVERITY_ERROR), new Problem("p2",
							"One or more cycles were detected in the build path of project 'P2'. The paths towards the cycle and cycle are:\n" +
							"->{P1, P2}",
							p2, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING) });

			env.addClass(root1, "p11", "XX", //$NON-NLS-1$ //$NON-NLS-2$
					"package p11;\n" + //$NON-NLS-1$
							"public class XX {\n" + //$NON-NLS-1$
							"}\n" //$NON-NLS-1$
			);
			env.addClass(root2, "p22", "YY", //$NON-NLS-1$ //$NON-NLS-2$
					"package p22;\n" + //$NON-NLS-1$
							"public class YY {\n" + //$NON-NLS-1$
							"}\n" //$NON-NLS-1$
			);

			incrementalBuild();
			env.waitForAutoBuild();
			expectingCompilingOrder(new String[] { "/P1/src/p11/XX.java", "/P2/src/p22/YY.java", "/P2/src/p2/Y.java",
					"/P1/src/p1/X.java" });
			expectingOnlySpecificProblemsFor(p1, new Problem[] {
					new Problem("p1", "The import p22 is never used", c1, 32, 35, //$NON-NLS-1$ //$NON-NLS-2$
							CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING), new Problem("p1", //$NON-NLS-1$
									"One or more cycles were detected in the build path of project 'P1'. The paths towards the cycle and cycle are:\n" +
									"->{P1, P2}",
							p1, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING) });
			expectingOnlySpecificProblemsFor(p2, new Problem[] {
					new Problem("p2", "The import p11 is never used", c2, 32, 35, //$NON-NLS-1$ //$NON-NLS-2$
							CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING), new Problem("p2", //$NON-NLS-1$
									"One or more cycles were detected in the build path of project 'P2'. The paths towards the cycle and cycle are:\n" +
									"->{P1, P2}",
							p2, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING) });

			JavaCore.setOptions(options);
		} finally {
			env.setBuildOrder(null);
			env.removeProject(p1);
			env.removeProject(p2);
		}
	}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=114349
// this one fails; compare with testCycle7 (only one change in Object source),
// which passes
public void testCycle6() throws JavaModelException {
	Hashtable options = JavaCore.getOptions();
	Hashtable newOptions = JavaCore.getOptions();
	newOptions.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING);

	JavaCore.setOptions(newOptions);

	//----------------------------
	//         Project1
	//----------------------------
	IPath p1 = env.addProject("P1");
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p1, "");
	IPath root1 = env.addPackageFragmentRoot(p1, "src");
	env.setOutputFolder(p1, "bin");

	env.addClass(root1, "java/lang", "Object",
		"package java.lang;\n" +
		"public class Object {\n" +
		"  Class getClass() { return null; }\n" +
		"  String toString() { return \"\"; }\n" +	// the line that changes
		"}\n"
		);

	//----------------------------
	//         Project2
	//----------------------------
	IPath p2 = env.addProject("P2");
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p2, "");
	IPath root2 = env.addPackageFragmentRoot(p2, "src");
	env.setOutputFolder(p2, "bin");

	env.addClass(root2, "java/lang", "Class",
		"package java.lang;\n" +
		"public class Class {\n" +
		"  String getName() { return \"\"; };\n" +
		"}\n"
		);

	//----------------------------
	//         Project3
	//----------------------------
	IPath p3 = env.addProject("P3");
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p3, "");
	IPath root3 = env.addPackageFragmentRoot(p3, "src");
	env.setOutputFolder(p3, "bin");

	env.addClass(root3, "java/lang", "String",
		"package java.lang;\n" +
		"public class String {\n" +
		"}\n"
		);

	// Dependencies
	IPath[] accessiblePaths = new IPath[] {new Path("java/lang/*")};
	IPath[] forbiddenPaths = new IPath[] {new Path("**/*")};
	env.addRequiredProject(p1, p2, accessiblePaths, forbiddenPaths, false);
	env.addRequiredProject(p1, p3, accessiblePaths, forbiddenPaths, false);
	env.addRequiredProject(p2, p1, accessiblePaths, forbiddenPaths, false);
	env.addRequiredProject(p2, p3, accessiblePaths, forbiddenPaths, false);
	env.addRequiredProject(p3, p1, accessiblePaths, forbiddenPaths, false);
	env.addRequiredProject(p3, p2, accessiblePaths, forbiddenPaths, false);

	try {
		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();
		expectingOnlySpecificProblemsFor(p1,new Problem[]{
			new Problem("p1",
					"One or more cycles were detected in the build path of project 'P1'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2}\n" +
					"->{P1, P2, P3}\n" +
					"P1->{P2, P3}\n" +
					"->{P1, P3}",
					p1, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING)
		});
		expectingOnlySpecificProblemsFor(p2,new Problem[]{
			new Problem("p2",
					"One or more cycles were detected in the build path of project 'P2'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2}\n" +
					"->{P1, P2, P3}\n" +
					"->{P2, P3}",
					p2, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING)
		});
		expectingOnlySpecificProblemsFor(p3,new Problem[]{
			new Problem("p3",
					"One or more cycles were detected in the build path of project 'P3'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2, P3}\n" +
					"->{P2, P3}\n" +
					"->{P1, P3}",
					p3, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING)
		});

	} finally {
		JavaCore.setOptions(options);
		env.removeProject(p1);
		env.removeProject(p2);
		env.removeProject(p3);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=114349
// this one passes; compare with testCycle6 (only one change in Object source),
// which fails
public void testCycle7() throws JavaModelException {
	Hashtable options = JavaCore.getOptions();
	Hashtable newOptions = JavaCore.getOptions();
	newOptions.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING);

	JavaCore.setOptions(newOptions);

	//----------------------------
	//         Project1
	//----------------------------
	IPath p1 = env.addProject("P1");
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p1, "");
	IPath root1 = env.addPackageFragmentRoot(p1, "src");
	env.setOutputFolder(p1, "bin");

	env.addClass(root1, "java/lang", "Object",
		"package java.lang;\n" +
		"public class Object {\n" +
		"  Class getClass() { return null; }\n" +
		"  String toString() { return null; }\n" +	// the line that changes
		"}\n"
		);

	//----------------------------
	//         Project2
	//----------------------------
	IPath p2 = env.addProject("P2");
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p2, "");
	IPath root2 = env.addPackageFragmentRoot(p2, "src");
	env.setOutputFolder(p2, "bin");

	env.addClass(root2, "java/lang", "Class",
		"package java.lang;\n" +
		"public class Class {\n" +
		"  String getName() { return \"\"; };\n" +
		"}\n"
		);

	//----------------------------
	//         Project3
	//----------------------------
	IPath p3 = env.addProject("P3");
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p3, "");
	IPath root3 = env.addPackageFragmentRoot(p3, "src");
	env.setOutputFolder(p3, "bin");

	env.addClass(root3, "java/lang", "String",
		"package java.lang;\n" +
		"public class String {\n" +
		"}\n"
		);

	// Dependencies
	IPath[] accessiblePaths = new IPath[] {new Path("java/lang/*")};
	IPath[] forbiddenPaths = new IPath[] {new Path("**/*")};
	env.addRequiredProject(p1, p2, accessiblePaths, forbiddenPaths, false);
	env.addRequiredProject(p1, p3, accessiblePaths, forbiddenPaths, false);
	env.addRequiredProject(p2, p1, accessiblePaths, forbiddenPaths, false);
	env.addRequiredProject(p2, p3, accessiblePaths, forbiddenPaths, false);
	env.addRequiredProject(p3, p1, accessiblePaths, forbiddenPaths, false);
	env.addRequiredProject(p3, p2, accessiblePaths, forbiddenPaths, false);

	try {
		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();
		expectingOnlySpecificProblemsFor(p1,new Problem[]{
			new Problem("p1",
					"One or more cycles were detected in the build path of project 'P1'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2}\n" +
					"->{P1, P2, P3}\n" +
					"P1->{P2, P3}\n" +
					"->{P1, P3}",
					p1, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING)
		});
		expectingOnlySpecificProblemsFor(p2,new Problem[]{
			new Problem("p2",
					"One or more cycles were detected in the build path of project 'P2'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2}\n" +
					"->{P1, P2, P3}\n" +
					"->{P2, P3}",
					p2, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING)
		});
		expectingOnlySpecificProblemsFor(p3,new Problem[]{
			new Problem("p3",
					"One or more cycles were detected in the build path of project 'P3'. The paths towards the cycle and cycle are:\n" +
					"->{P1, P2, P3}\n" +
					"->{P2, P3}\n" +
					"->{P1, P3}",
					p3, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING)
		});

	} finally {
		JavaCore.setOptions(options);
		env.removeProject(p1);
		env.removeProject(p2);
		env.removeProject(p3);
	}
}
	public void testCycle8() throws JavaModelException {
		// specifically tests projects with (transitive) dependencies on a cycle, i.e., error messages with a non-empty prefix
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING);

		JavaCore.setOptions(newOptions);

		IPath p1 = env.addProject("P1");
		IPath p2 = env.addProject("P2");
		IPath p3 = env.addProject("P3");
		IPath p4 = env.addProject("P4");

		// Dependencies
		IPath[] accessiblePaths = new IPath[] {new Path("java/lang/*")};
		IPath[] forbiddenPaths = new IPath[] {new Path("**/*")};
		env.addRequiredProject(p1, p2, accessiblePaths, forbiddenPaths, false);
		env.addRequiredProject(p2, p3, accessiblePaths, forbiddenPaths, false);
		env.addRequiredProject(p3, p4, accessiblePaths, forbiddenPaths, false);
		env.addRequiredProject(p4, p3, accessiblePaths, forbiddenPaths, false);

		try {
			env.waitForManualRefresh();
			fullBuild();
			env.waitForAutoBuild();
			expectingOnlySpecificProblemsFor(p1,new Problem[]{
				new Problem("p1",
					"One or more cycles were detected in the build path of project 'P1'. The paths towards the cycle and cycle are:\n" +
					"P1, P2->{P3, P4}",
					p1, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING)
			});
			expectingOnlySpecificProblemsFor(p2,new Problem[]{
				new Problem("p2",
					"One or more cycles were detected in the build path of project 'P2'. The paths towards the cycle and cycle are:\n" +
					"P2->{P3, P4}",
					p2, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING),
			});
			expectingOnlySpecificProblemsFor(p3,new Problem[]{
				new Problem("p3",
					"One or more cycles were detected in the build path of project 'P3'. The paths towards the cycle and cycle are:\n" +
					"->{P3, P4}",
					p3, -1,-1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_WARNING)
			});

		} finally {
			JavaCore.setOptions(options);
			env.removeProject(p1);
			env.removeProject(p2);
			env.removeProject(p3);
			env.removeProject(p4);
		}
	}

	/*
	 * Full buid case
	 */
	public void testExcludePartOfAnotherProject1() throws JavaModelException {
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p.api", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.api;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.addClass(root1, "p.internal", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.internal;\n" + //$NON-NLS-1$
			"public class B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path, new IPath[] {}, new IPath[] {new Path("**/internal/")}, false);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C extends p.api.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		IPath d = env.addClass(root2, "", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"public class D extends p.internal.B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();
		expectingSpecificProblemFor(project2Path, new Problem("", "Access restriction: The type 'B' is not API (restriction on required project 'Project1')", d, 23, 35, CategorizedProblem.CAT_RESTRICTION, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		env.removeProject(project1Path);
		env.removeProject(project2Path);
	}

	/*
	 * Incremental buid case
	 */
	public void testExcludePartOfAnotherProject2() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p.api", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.api;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.addClass(root1, "p.internal", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.internal;\n" + //$NON-NLS-1$
			"public class B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path, new IPath[] {}, new IPath[] {new Path("**/internal/")}, false);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C extends p.api.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 2
		//----------------------------
		IPath d = env.addClass(root2, "", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"public class D extends p.internal.B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild();
		env.waitForAutoBuild();
		expectingSpecificProblemFor(project2Path, new Problem("", "Access restriction: The type 'B' is not API (restriction on required project 'Project1')", d, 23, 35, CategorizedProblem.CAT_RESTRICTION, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		env.removeProject(project1Path);
		env.removeProject(project2Path);
	}

	/*
	 * Fix access restriction problem
	 */
	public void testExcludePartOfAnotherProject3() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p.api", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.api;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.addClass(root1, "p.internal", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.internal;\n" + //$NON-NLS-1$
			"public class B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path, new IPath[] {}, new IPath[] {new Path("**/internal/")}, false);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C extends p.api.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		IPath d = env.addClass(root2, "", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"public class D extends p.internal.B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();
		expectingSpecificProblemFor(project2Path, new Problem("", "Access restriction: The type 'B' is not API (restriction on required project 'Project1')", d, 23, 35, CategorizedProblem.CAT_RESTRICTION, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		//----------------------------
		//           Step 2
		//----------------------------
		env.removeRequiredProject(project2Path, project1Path);
		env.addRequiredProject(project2Path, project1Path, new IPath[] {}, new IPath[] {}, false);

		incrementalBuild();
		env.waitForAutoBuild();
		expectingNoProblems();
		env.removeProject(project1Path);
		env.removeProject(project2Path);
	}

	/*
	 * Full buid case
	 */
	public void testIncludePartOfAnotherProject1() throws JavaModelException {
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p.api", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.api;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.addClass(root1, "p.internal", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.internal;\n" + //$NON-NLS-1$
			"public class B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path, new IPath[] {new Path("**/api/")}, new IPath[] {new Path("**")}, false);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C extends p.api.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		IPath d = env.addClass(root2, "", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"public class D extends p.internal.B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild();
		env.waitForAutoBuild();
		expectingSpecificProblemFor(project2Path, new Problem("", "Access restriction: The type 'B' is not API (restriction on required project 'Project1')", d, 23, 35, CategorizedProblem.CAT_RESTRICTION, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		env.removeProject(project1Path);
		env.removeProject(project2Path);
	}

	/*
	 * Incremental buid case
	 */
	public void testIncludePartOfAnotherProject2() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p.api", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.api;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.addClass(root1, "p.internal", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.internal;\n" + //$NON-NLS-1$
			"public class B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path, new IPath[] {new Path("**/api/")}, new IPath[] {new Path("**")}, false);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C extends p.api.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 2
		//----------------------------
		IPath d = env.addClass(root2, "", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"public class D extends p.internal.B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild();
		env.waitForAutoBuild();
		expectingSpecificProblemFor(project2Path, new Problem("", "Access restriction: The type 'B' is not API (restriction on required project 'Project1')", d, 23, 35, CategorizedProblem.CAT_RESTRICTION, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		env.removeProject(project1Path);
		env.removeProject(project2Path);
	}

	/*
	 * Fix access restriction problem
	 */
	public void testIncludePartOfAnotherProject3() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p.api", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.api;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.addClass(root1, "p.internal", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.internal;\n" + //$NON-NLS-1$
			"public class B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path, new IPath[] {new Path("**/api/")}, new IPath[] {new Path("**")}, false);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C extends p.api.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		IPath d = env.addClass(root2, "", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"public class D extends p.internal.B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();
		expectingSpecificProblemFor(project2Path, new Problem("", "Access restriction: The type 'B' is not API (restriction on required project 'Project1')", d, 23, 35, CategorizedProblem.CAT_RESTRICTION, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		//----------------------------
		//           Step 2
		//----------------------------
		env.removeRequiredProject(project2Path, project1Path);
		env.addRequiredProject(project2Path, project1Path, new IPath[] {}, new IPath[] {}, false);

		incrementalBuild();
		env.waitForAutoBuild();
		expectingNoProblems();
		env.removeProject(project1Path);
		env.removeProject(project2Path);
	}

	/*
	 * Ensures that a type matching a ignore-if-better non-accessible rule is further found when accessible
	 * on another classpath entry.
	 * (regression test for bug 98127 Access restrictions started showing up after switching to bundle)
	 */
	public void testIgnoreIfBetterNonAccessibleRule1() throws JavaModelException {
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project3
			//----------------------------
		IPath project3Path = env.addProject("Project3"); //$NON-NLS-1$
		env.addExternalJars(project3Path, Util.getJavaClassLibs());
		env.addRequiredProject(project3Path, project1Path, new Path("**/p/"), IAccessRule.K_NON_ACCESSIBLE | IAccessRule.IGNORE_IF_BETTER);
		env.addRequiredProject(project3Path, project2Path, new Path("**/p/A"), IAccessRule.K_ACCESSIBLE);
		IPath root3 = env.getPackageFragmentRootPath(project3Path, ""); //$NON-NLS-1$
		env.addClass(root3, "p3", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n" + //$NON-NLS-1$
			"public class B extends p.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();
		expectingNoProblems();
		env.removeProject(project1Path);
		env.removeProject(project2Path);
		env.removeProject(project3Path);
	}

	/*
	 * Ensures that a type matching a ignore-if-better non-accessible rule is further found when accessible
	 * on another classpath entry.
	 * (regression test for bug 98127 Access restrictions started showing up after switching to bundle)
	 */
	public void testIgnoreIfBetterNonAccessibleRule2() throws JavaModelException {
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project3
			//----------------------------
		IPath project3Path = env.addProject("Project3"); //$NON-NLS-1$
		env.addExternalJars(project3Path, Util.getJavaClassLibs());
		env.addRequiredProject(project3Path, project1Path, new Path("**/p/"), IAccessRule.K_NON_ACCESSIBLE | IAccessRule.IGNORE_IF_BETTER);
		env.addRequiredProject(project3Path, project2Path, new Path("**/p/A"), IAccessRule.K_DISCOURAGED);
		IPath root3 = env.getPackageFragmentRootPath(project3Path, ""); //$NON-NLS-1$
		IPath b = env.addClass(root3, "p3", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n" + //$NON-NLS-1$
			"public class B extends p.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();
		expectingSpecificProblemFor(project3Path, new Problem("", "Discouraged access: The type 'A' is not API (restriction on required project 'Project2')", b, 35, 38, CategorizedProblem.CAT_RESTRICTION, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$ //$NON-NLS-2$
		env.removeProject(project1Path);
		env.removeProject(project2Path);
		env.removeProject(project3Path);
	}

	public void testMissingRequiredBinaries() throws JavaModelException {

		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$
		IPath p3 = env.addProject("P3"); //$NON-NLS-1$

		env.addExternalJars(p1, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
		env.addRequiredProject(p1, p2);
		env.setOutputFolder(p1, "bin"); //$NON-NLS-1$

		env.addExternalJars(p2, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		env.addRequiredProject(p2, p3);
		env.setOutputFolder(p2, "bin"); //$NON-NLS-1$

		env.addExternalJars(p3, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
		IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
		env.setOutputFolder(p3, "bin"); //$NON-NLS-1$

		IPath x = env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"import p2.*;\n"+ //$NON-NLS-1$
			"public class X extends Y{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p3.*;\n"+ //$NON-NLS-1$
			"public class Y extends Z {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root3, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class Z {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		try {
			env.waitForManualRefresh();
			fullBuild();
			env.waitForAutoBuild();

			expectingOnlySpecificProblemsFor(p1,new Problem[]{
				new Problem("p1", "The type p3.Z cannot be resolved. It is indirectly referenced from required type p2.Y", x, 48, 49, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p1", "The project was not built since its build path is incomplete. Cannot find the class file for p3.Z. Fix the build path then try building this project", p1, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR)//$NON-NLS-1$ //$NON-NLS-2$
			});
		} finally {
			env.setBuildOrder(null);
			env.removeProject(p1);
			env.removeProject(p1);
			env.removeProject(p3);
		}
	}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159118
// contrast this with test101: get an error when the class folder is not
// exported from the target project - P1 here
public void test100_class_folder_exported() throws JavaModelException {
	IPath P1 = env.addProject("P1");
	env.setOutputFolder(P1, "bin");
	env.addExternalJars(P1, Util.getJavaClassLibs());
	env.addClass(
		env.addPackage(
			env.getPackageFragmentRootPath(P1, ""), "p"),
		"A",
		"package p;\n" +
		"public class A {\n" +
		"}\n"
		);
	env.waitForManualRefresh();
	fullBuild();
	env.waitForAutoBuild();
	expectingNoProblems();
	env.removePackageFragmentRoot(P1, "");
	env.addClassFolder(P1, P1.append("bin"), true);
	IPath P2 = env.addProject("P2");
	env.addExternalJars(P2, Util.getJavaClassLibs());
	env.addRequiredProject(P2, P1);
	env.addClass(
		env.getPackageFragmentRootPath(P2, ""),
		"X",
		"import p.A;\n" +
		"public class X {\n" +
		"  A f;\n" +
		"}");
	env.waitForManualRefresh();
	fullBuild();
	env.waitForAutoBuild();
	expectingNoProblems();
	env.removeProject(P1);
	env.removeProject(P2);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159118
// contrast this with test100: get an error when the class folder is not
// exported from the target project - P1 here
public void test101_class_folder_non_exported() throws JavaModelException {
	IPath P1 = env.addProject("P1");
	env.setOutputFolder(P1, "bin");
	env.addExternalJars(P1, Util.getJavaClassLibs());
	env.addClass(
		env.addPackage(
			env.getPackageFragmentRootPath(P1, ""), "p"),
		"A",
		"package p;\n" +
		"public class A {\n" +
		"}\n"
		);
	fullBuild();
	expectingNoProblems();
	env.removePackageFragmentRoot(P1, "");
	env.addClassFolder(P1, P1.append("bin"), false);
	IPath P2 = env.addProject("P2");
	env.addExternalJars(P2, Util.getJavaClassLibs());
	env.addRequiredProject(P2, P1);
	IPath c = env.addClass(
		env.getPackageFragmentRootPath(P2, ""),
		"X",
		"import p.A;\n" +
		"public class X {\n" +
		"  A f;\n" +
		"}");
	env.waitForManualRefresh();
	fullBuild();
	env.waitForAutoBuild();
	expectingSpecificProblemsFor(P2,
		new Problem[] {
			new Problem("", "The import p cannot be resolved",
					c, 7 , 8, CategorizedProblem.CAT_IMPORT, IMarker.SEVERITY_ERROR),
			new Problem("", "A cannot be resolved to a type",
					c, 31 , 32, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)});
	env.removeProject(P1);
	env.removeProject(P2);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=164622
public void test102_missing_required_binaries() throws JavaModelException {

	IPath p1 = env.addProject("P1");
	env.addExternalJars(p1, Util.getJavaClassLibs());
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p1, "");
	IPath root1 = env.addPackageFragmentRoot(p1, "src");
	env.setOutputFolder(p1, "bin");

	IPath p2 = env.addProject("P2");
	env.addExternalJars(p2, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(p2, "");
	IPath root2 = env.addPackageFragmentRoot(p2, "src");
	env.addRequiredProject(p2, p1);
	env.setOutputFolder(p2, "bin");

	IPath p3 = env.addProject("P3");
	env.addExternalJars(p3, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(p3, "");
	IPath root3 = env.addPackageFragmentRoot(p3, "src");
//	env.addRequiredProject(p3, p1); - missing dependency
	env.addRequiredProject(p3, p2);
	env.setOutputFolder(p3, "bin");

	env.addClass(root1, "", "I",
		"public interface I {\n" +
		"}\n"
		);

	env.addClass(root2, "", "X",
		"public class X implements I {\n" +
		"}\n"
		);

	IPath y = env.addClass(root3, "", "Y",
		"public class Y extends X {\n" +
		"  X m = new X() {};\n" +
		"}\n"
		);

	try {
		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();
		expectingOnlySpecificProblemsFor(p3, new Problem[]{
			new Problem("p3",
				"The project was not built since its build path is incomplete. Cannot find the class file for I. Fix the build path then try building this project",
				p3, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR),
			new Problem("p3",
				"The type I cannot be resolved. It is indirectly referenced from required type X",
				y, 23, 24, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR),
		});
	} finally {
		env.setBuildOrder(null);
		env.removeProject(p1);
		env.removeProject(p2);
		env.removeProject(p3);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=164622
public void test103_missing_required_binaries() throws JavaModelException {

	IPath p1 = env.addProject("P1");
	env.addExternalJars(p1, Util.getJavaClassLibs());
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p1, "");
	IPath root1 = env.addPackageFragmentRoot(p1, "src");
	env.setOutputFolder(p1, "bin");

	IPath p2 = env.addProject("P2");
	env.addExternalJars(p2, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(p2, "");
	IPath root2 = env.addPackageFragmentRoot(p2, "src");
	env.addRequiredProject(p2, p1);
	env.setOutputFolder(p2, "bin");

	IPath p3 = env.addProject("P3");
	env.addExternalJars(p3, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(p3, "");
	IPath root3 = env.addPackageFragmentRoot(p3, "src");
//	env.addRequiredProject(p3, p1); - missing dependency
	env.addRequiredProject(p3, p2);
	env.setOutputFolder(p3, "bin");

	env.addClass(root1, "", "I",
		"public interface I {\n" +
		"}\n"
		);

	env.addClass(root2, "", "X",
		"public class X implements I {\n" +
		"}\n"
		);

	IPath y = env.addClass(root3, "", "Y",
		"public class Y {\n" +
		"  X m = new X() {};\n" +
		"  X n = new X() {};\n" +
		"}\n"
		);

	try {
		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();
		expectingOnlySpecificProblemsFor(p3, new Problem[]{
				new Problem("p3",
					"The project was not built since its build path is incomplete. Cannot find the class file for I. Fix the build path then try building this project",
					p3, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR),
				new Problem("p3",
					"The type I cannot be resolved. It is indirectly referenced from required type X",
					y, 0, 1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR),
		});
	} finally {
		env.setBuildOrder(null);
		env.setBuildOrder(null);
		env.removeProject(p1);
		env.removeProject(p2);
		env.removeProject(p3);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=460993
public void test104_missing_required_binaries() throws CoreException {

	IPath p0 = env.addProject("JRE17", "1.7");
	env.addExternalJars(p0, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(p0, "");
	IPath root0 = env.addPackageFragmentRoot(p0, "src");
	env.setOutputFolder(p0, "bin");

	IPath p1 = env.addProject("org.eclipse.jgit", "1.7");
	env.addExternalJars(p1, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(p1, "");
	IPath root1 = env.addPackageFragmentRoot(p1, "src");
	env.addRequiredProject(p1, p0);
	env.setOutputFolder(p1, "bin");

	IPath p2 = env.addProject("org.eclipse.releng.tools", "1.5");
	env.addExternalJars(p2, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(p2, "");
	IPath root2 = env.addPackageFragmentRoot(p2, "src");
//	env.addRequiredProject(p2, p0); - missing dependency
	env.addRequiredProject(p2, p1);
	env.setOutputFolder(p2, "bin");

	env.addClass(root0, "jre17", "AutoClosable",
			"package jre17;\n" +
			"public interface AutoClosable {\n" +
			"	void closeIt();\n" +
			"}\n"
			);

	env.addClass(root1, "org.eclipse.jgit.lib", "Repository",
			"package org.eclipse.jgit.lib;\n" +
			"import jre17.AutoClosable;\n" +
			"public abstract class Repository implements AutoClosable {\n" +
			"	public void resolve(final String revstr) { }\n" +
			"}\n"
			);

	IPath gca = env.addClass(root2, "org.eclipse.releng.tools.git", "GitCopyrightAdapter",
			"package org.eclipse.releng.tools.git;\n" +
			"import org.eclipse.jgit.lib.Repository;\n" +
			"public class GitCopyrightAdapter {\n" +
			"	void foo(Repository repo) {\n" +
			"		repo.resolve(\"Head\");\n" +
			"	}\n" +
			"}\n"
			);
	env.addClass(root2, "org.eclipse.releng.tools.preferences", "Messages",
			"package org.eclipse.releng.tools.preferences;\n" +
			"final class Messages {\n" +
			"	{\n" +
			"		@SuppressWarnings(\"unused\")\n" +
			"		Object o = \"\"; // triggers the bug\n" +
			"	}\n" +
			"}\n"
			);

	try {
		env.waitForManualRefresh();
		fullBuild();
		env.waitForAutoBuild();
		expectingNoProblems();

		IFile gcaFile = (IFile) env.getWorkspace().getRoot().findMember(gca);
		gcaFile.touch(null);
		env.waitForManualRefresh();
		incrementalBuild(p2);
		env.waitForAutoBuild();
		expectingNoProblems();

	} finally {
		env.setBuildOrder(null);
		env.setBuildOrder(null);
		env.removeProject(p1);
		env.removeProject(p2);
		env.removeProject(p0);
	}
}


// https://bugs.eclipse.org/bugs/show_bug.cgi?id=438923, [compiler]Type is inappropriately considered "indirectly referenced"
public void test438923() throws JavaModelException {
	//----------------------------
	//         Project1
	//----------------------------
	IPath p1 = env.addProject("P1"); //$NON-NLS-1$
	env.addExternalJars(p1, Util.getJavaClassLibs());
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
	IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
	env.setOutputFolder(p1, "bin"); //$NON-NLS-1$

	env.addClass(root1, "p1", "P1I0", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" +
			"public interface P1I0 {\n" +
			"  interface II {/*empty*/}\n" +
			"}\n"
		);

	//----------------------------
	//         Project2
	//----------------------------
	IPath p2 = env.addProject("P2"); //$NON-NLS-1$
	env.addExternalJars(p2, Util.getJavaClassLibs());
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
	IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
	env.setOutputFolder(p2, "bin"); //$NON-NLS-1$

	env.addClass(root2, "p2", "P2C0", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n" +
			"import p1.P1I0;\n" +
			"public class P2C0 {\n" +
			"  public void z(final P1I0.II [] ii) {/*empty*/}\n" +
			"}\n"
		);

	//----------------------------
	//         Project3
	//----------------------------
	IPath p3 = env.addProject("P3"); //$NON-NLS-1$
	env.addExternalJars(p3, Util.getJavaClassLibs());
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
	IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
	env.setOutputFolder(p3, "bin"); //$NON-NLS-1$

	env.addClass(root3, "p3", "P3C0", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n" +
			"import p2.P2C0;\n" +
			"public class P3C0\n" +
			"  extends P2C0 {/*empty*/}\n"
		);

	// for Project1
	env.addRequiredProject(p2, p1);
	env.addRequiredProject(p3, p2);
	env.waitForManualRefresh();
	fullBuild();
	env.waitForAutoBuild();
	expectingNoProblems();
	env.setBuildOrder(null);
	env.removeProject(p1);
	env.removeProject(p2);
	env.removeProject(p3);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=461074, "indirectly referenced from required .class files" error for unreachable reference of type in overriding method declaration in a library on classpath
public void test461074() throws JavaModelException {
	//----------------------------
	//         Project1
	//----------------------------
	IPath p1 = env.addProject("SampleMissing"); //$NON-NLS-1$
	env.addExternalJars(p1, Util.getJavaClassLibs());
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
	IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
	env.setOutputFolder(p1, "bin"); //$NON-NLS-1$

	env.addClass(root1, "pack.missing", "MissingType", //$NON-NLS-1$ //$NON-NLS-2$
			"package pack.missing;\n" +
			"public class MissingType {\n" +
			"}\n"
		);

	//----------------------------
	//         Project2
	//----------------------------
	IPath p2 = env.addProject("SampleLib", "1.5"); //$NON-NLS-1$
	env.addExternalJars(p2, Util.getJavaClassLibs());
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
	IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
	env.setOutputFolder(p2, "bin"); //$NON-NLS-1$

	env.addClass(root2, "pack.lib", "TopClass", //$NON-NLS-1$ //$NON-NLS-2$
			"package pack.lib;\n" +
			"public class TopClass {\n" +
			"  Object get() { return null; }\n" +
			"}\n"
		);
	env.addClass(root2, "pack.lib", "SuperClass", //$NON-NLS-1$ //$NON-NLS-2$
			"package pack.lib;\n" +
			"import pack.missing.MissingType;\n" +
			"public class SuperClass extends TopClass {\n" +
			"  @Override\n" +
			"  MissingType get() { return null; }\n" +
			"}\n"
		);

	//----------------------------
	//         Project3
	//----------------------------
	IPath p3 = env.addProject("SampleTest", "1.5"); //$NON-NLS-1$
	env.addExternalJars(p3, Util.getJavaClassLibs());
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
	IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
	env.setOutputFolder(p3, "bin"); //$NON-NLS-1$

	env.addClass(root3, "pack.test", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"package pack.test;\n" +
			"import pack.lib.SuperClass;\n" +
			"public class Test extends SuperClass {/*empty*/}\n"
		);

	// for Project1
	env.addRequiredProject(p2, p1);
	env.addRequiredProject(p3, p2);
	env.waitForManualRefresh();
	fullBuild();
	env.waitForAutoBuild();
	expectingNoProblems();
	env.setBuildOrder(null);
	env.removeProject(p1);
	env.removeProject(p2);
	env.removeProject(p3);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=461074, "indirectly referenced from required .class files" error for unreachable reference of type in overriding method declaration in a library on classpath
public void test461074_error() throws JavaModelException {
	//----------------------------
	//         Project1
	//----------------------------
	IPath p1 = env.addProject("SampleMissing"); //$NON-NLS-1$
	env.addExternalJars(p1, Util.getJavaClassLibs());
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
	IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
	env.setOutputFolder(p1, "bin"); //$NON-NLS-1$

	env.addClass(root1, "pack.missing", "MissingType", //$NON-NLS-1$ //$NON-NLS-2$
			"package pack.missing;\n" +
			"public class MissingType {\n" +
			"}\n"
		);

	//----------------------------
	//         Project2
	//----------------------------
	IPath p2 = env.addProject("SampleLib", "1.5"); //$NON-NLS-1$
	env.addExternalJars(p2, Util.getJavaClassLibs());
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
	IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
	env.setOutputFolder(p2, "bin"); //$NON-NLS-1$

	env.addClass(root2, "pack.lib", "TopClass", //$NON-NLS-1$ //$NON-NLS-2$
			"package pack.lib;\n" +
			"public abstract class TopClass {\n" +
			"  abstract Object get();\n" +
			"}\n"
		);
	env.addClass(root2, "pack.lib", "SuperClass", //$NON-NLS-1$ //$NON-NLS-2$
			"package pack.lib;\n" +
			"import pack.missing.MissingType;\n" +
			"public class SuperClass extends TopClass {\n" +
			"  @Override\n" +
			"  MissingType get() { return null; }\n" +
			"}\n"
		);

	//----------------------------
	//         Project3
	//----------------------------
	IPath p3 = env.addProject("SampleTest", "1.5"); //$NON-NLS-1$
	env.addExternalJars(p3, Util.getJavaClassLibs());
	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
	IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
	env.setOutputFolder(p3, "bin"); //$NON-NLS-1$

	IPath test = env.addClass(root3, "pack.test", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"package pack.test;\n" +
			"import pack.lib.SuperClass;\n" +
			"public class Test extends SuperClass {/*empty*/}\n"
		);

	// for Project1
	env.addRequiredProject(p2, p1);
	env.addRequiredProject(p3, p2);
	env.waitForManualRefresh();
	fullBuild();
	env.waitForAutoBuild();
	expectingOnlySpecificProblemsFor(p3, new Problem[]{
			new Problem("p3",
				"The project was not built since its build path is incomplete. Cannot find the class file for pack.missing.MissingType. Fix the build path then try building this project",
				p3, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR),
			new Problem("p3",
				"The type pack.missing.MissingType cannot be resolved. It is indirectly referenced from required type pack.lib.SuperClass",
				test, 0, 1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR),
	});
	env.setBuildOrder(null);
	env.removeProject(p1);
	env.removeProject(p2);
	env.removeProject(p3);
}
}
