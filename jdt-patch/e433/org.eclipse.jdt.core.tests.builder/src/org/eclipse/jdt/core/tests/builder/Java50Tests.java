/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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

import junit.framework.Test;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;

public class Java50Tests extends BuilderTests {

	public Java50Tests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(Java50Tests.class);
	}

	public void testAnnotation() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "");

		IPath usePath = env.addClass(projectPath, "p", "Use",
			"package p;\n" +
			"@q.Ann\n" +
			"public class Use {\n" +
			"}"
		);
		env.addClass(projectPath, "q", "Ann",
			"package q;\n" +
			"public @interface Ann {\n" +
			"}"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "q", "Ann",
			"package q;\n" +
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.METHOD)\n" +
			"public @interface Ann {\n" +
			"}"
		);

		incrementalBuild(projectPath);
		expectingProblemsFor(
			usePath,
			"Problem : The annotation @Ann is disallowed for this location [ resource : </Project/p/Use.java> range : <11,17> category : <40> severity : <2>]"
		);
	}

	public void testHierarchyCycle() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "");

		env.addClass(projectPath, "", "A",
			"interface A<T extends C> {}\n" +
			"interface B extends A<D> {}\n" +
			"interface D extends C {}"
		);
		env.addClass(projectPath, "", "C",
			"interface C extends B {}"
		);

		fullBuild(projectPath);
		expectingNoProblems();
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=214237, dupe of
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=205235
	public void testHierarchyCycleInstanceof() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "");

		env.addClass(projectPath, "", "A",
			"import java.util.Collection;\n" +
			"public abstract class A\n" +
			"<T extends A<T,S>,S extends Collection<T>> {}\n" +
			// a specific grouping of classes is needed to hit the problem using
			// the test framework
			"abstract class B extends A<D,Collection<D>> {\n" +
			"  boolean isD() {return this instanceof D;}\n" +
			"}\n"+
			"final class D extends C {}\n"
		);
		env.addClass(projectPath, "", "C",
			"public abstract class C extends B {\n" +
			"  boolean isD() {return this instanceof D;}\n" +
			"}\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=231293
	public void testMissingRequiredBinaries() throws JavaModelException {

		IPath p1 = env.addProject("P1", "1.5"); //$NON-NLS-1$
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$

		env.addExternalJars(p1, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
		env.setOutputFolder(p1, "bin"); //$NON-NLS-1$

		env.addExternalJars(p2, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		IPath p2bin = env.setOutputFolder(p2, "bin"); //$NON-NLS-1$

		env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Y {\n"+ //$NON-NLS-1$
			"	public void foo(int i) {}\n"+ //$NON-NLS-1$
			"	public void foo(int i, Z z) {}\n"+ //$NON-NLS-1$
			"}\n"+ //$NON-NLS-1$
			"class Z {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();

		env.addClassFolder(p1, p2bin, false);
		env.removeFile(p2bin.append("p2/Z.class")); //$NON-NLS-1$

		env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void test(p2.Y y) { y.foo(1); }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(p1);
		expectingNoProblems();

		IPath xx = env.addClass(root1, "p1", "XX", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class XX {\n"+ //$NON-NLS-1$
			"	void test(p2.Y y) { y.foo('c', null); }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(p1);
		expectingOnlySpecificProblemsFor(p1,new Problem[]{
				new Problem("p1", "The project was not built since its build path is incomplete. Cannot find the class file for p2.Z. Fix the build path then try building this project", p1, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p1", "The type p2.Z cannot be resolved. It is indirectly referenced from required type p2.Y", xx, 51, 67, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR)//$NON-NLS-1$ //$NON-NLS-2$
			});
	}

	public void testParameterizedMemberType() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "");

		IPath xPath = env.addClass(projectPath, "", "X",
			"class X<T> extends A<T> {}"
		);

		IPath aPath = env.addClass(projectPath, "", "A",
			"class A<T> extends B<B<T>.M> {}"
		);

		IPath bPath = env.addClass(projectPath, "", "B",
			"class B<T> extends Missing<T> {\n" +
			"	class M{}\n" +
			"}\n" +
			"class Missing<T> {}"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "", "B",
			"class B<T> extends Missing<T> {\n" +
			"	class M{}\n" +
			"}"
		);

		incrementalBuild(projectPath);
		expectingSpecificProblemFor(xPath, new Problem("X", "The hierarchy of the type X is inconsistent", xPath, 6, 7, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(aPath, new Problem("A", "The hierarchy of the type A is inconsistent", aPath, 6, 7, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(bPath, new Problem("B", "Missing cannot be resolved to a type", bPath, 19, 26, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(projectPath, "", "X",
			"class X<T> extends A<T> {}"
		);

		incrementalBuild(projectPath);
		expectingSpecificProblemFor(xPath, new Problem("X", "The hierarchy of the type X is inconsistent", xPath, 6, 7, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(aPath, new Problem("A", "The hierarchy of the type A is inconsistent", aPath, 6, 7, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(bPath, new Problem("B", "Missing cannot be resolved to a type", bPath, 19, 26, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(projectPath, "", "B",
			"class B<T> extends Missing<T> {\n" +
			"	class M{}\n" +
			"}"
		);

		incrementalBuild(projectPath);
		expectingSpecificProblemFor(xPath, new Problem("X", "The hierarchy of the type X is inconsistent", xPath, 6, 7, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(aPath, new Problem("A", "The hierarchy of the type A is inconsistent", aPath, 6, 7, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(bPath, new Problem("B", "Missing cannot be resolved to a type", bPath, 19, 26, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(projectPath, "", "B",
			"class B<T> extends Missing<T> {\n" +
			"	class M{}\n" +
			"}\n" +
			"class Missing<T> {}"
		);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testParameterizedType1() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "");

		IPath usePath = env.addClass(projectPath, "p", "Use",
			"package p;\n" +
			"import java.util.ArrayList;\n" +
			"import q.Other;\n" +
			"public class Use {\n" +
			"	public Use() {\n" +
			"		new Other().foo(new ArrayList<String>());\n" +
			"	}\n" +
			"}"
		);
		env.addClass(projectPath, "q", "Other",
			"package q;\n" +
			"import java.util.List;\n" +
			"public class Other {\n" +
			"	public void foo(List<String> ls) {}\n" +
			"}"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "q", "Other",
			"package q;\n" +
			"import java.util.List;\n" +
			"public class Other {\n" +
			"	public void foo(List<Object> ls) {}\n" +
			"}"
		);

		incrementalBuild(projectPath);
		expectingProblemsFor(
			usePath,
			"Problem : The method foo(List<Object>) in the type Other is not applicable for the arguments (ArrayList<String>) [ resource : </Project/p/Use.java> range : <104,107> category : <50> severity : <2>]"
		);
	}

	public void testParameterizedType2() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "");

		IPath usePath = env.addClass(projectPath, "p", "Use",
			"package p;\n" +
			"import java.util.ArrayList;\n" +
			"import q.Other;\n" +
			"public class Use {\n" +
			"	public Use() {\n" +
			"		new Other().foo(new ArrayList<String>());\n" +
			"	}\n" +
			"}"
		);
		env.addClass(projectPath, "q", "Other",
			"package q;\n" +
			"import java.util.List;\n" +
			"public class Other {\n" +
			"	public void foo(List<String> ls) {}\n" +
			"}"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "q", "Other",
			"package q;\n" +
			"import java.util.List;\n" +
			"public class Other {\n" +
			"	public void foo(List<String> ls) throws Exception {}\n" +
			"}"
		);

		incrementalBuild(projectPath);
		expectingProblemsFor(
			usePath,
			"Problem : Unhandled exception type Exception [ resource : </Project/p/Use.java> range : <92,132> category : <40> severity : <2>]"
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=294057
	public void testHierarchyNonCycle() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "");

		env.addClass(projectPath, "superint", "SuperInterface",
				"package superint;\n" +
				"public interface SuperInterface<G extends SuperInterface.SuperInterfaceGetter,\n" +
				"								 S extends SuperInterface.SuperInterfaceSetter> {\n" +
				"    public interface SuperInterfaceGetter {}\n" +
				"    public interface SuperInterfaceSetter {}\n" +
				"}\n"
		);
		env.addClass(projectPath, "subint", "SubInterface",
				"package subint;\n" +
				"import superint.SuperInterface;\n" +
				"public interface SubInterface extends\n" +
				"    SuperInterface<SubInterface.SubInterfaceGetter,\n" +
				"                   SubInterface.SubInterfaceSetter> {\n" +
				"        public interface SubInterfaceGetter extends SuperInterfaceGetter {}\n" +
				"        public interface SubInterfaceSetter extends SuperInterfaceSetter {}\n" +
				"}\n"
		);

		fullBuild(projectPath);
		expectingProblemsFor(
				projectPath,
				"Problem : Bound mismatch: The type SubInterface.SubInterfaceGetter is not a valid substitute for the bounded parameter <G extends SuperInterface.SuperInterfaceGetter> of the type SuperInterface<G,S> [ resource : </Project/subint/SubInterface.java> range : <105,136> category : <40> severity : <2>]\n" +
				"Problem : Bound mismatch: The type SubInterface.SubInterfaceSetter is not a valid substitute for the bounded parameter <S extends SuperInterface.SuperInterfaceSetter> of the type SuperInterface<G,S> [ resource : </Project/subint/SubInterface.java> range : <157,188> category : <40> severity : <2>]\n" +
				"Problem : SuperInterfaceGetter cannot be resolved to a type [ resource : </Project/subint/SubInterface.java> range : <244,264> category : <40> severity : <2>]\n" +
				"Problem : SuperInterfaceSetter cannot be resolved to a type [ resource : </Project/subint/SubInterface.java> range : <320,340> category : <40> severity : <2>]"
			);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=294057 (variation)
	public void testHierarchyNonCycle2() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "");

		env.addClass(projectPath, "superint", "SuperInterface",
				"package superint;\n" +
				"public interface SuperInterface<G extends SuperInterface.SuperInterfaceGetter,\n" +
				"								 S extends SuperInterface.SuperInterfaceSetter> {\n" +
				"    public interface SuperInterfaceGetter {}\n" +
				"    public interface SuperInterfaceSetter {}\n" +
				"}\n"
		);
		env.addClass(projectPath, "subint", "SubInterface",
				"package subint;\n" +
				"import superint.SuperInterface;\n" +
				"import superint.SuperInterface.SuperInterfaceGetter;\n" +
				"import superint.SuperInterface.SuperInterfaceSetter;\n" +
				"public interface SubInterface extends\n" +
				"    SuperInterface<SubInterface.SubInterfaceGetter,\n" +
				"                   SubInterface.SubInterfaceSetter> {\n" +
				"        public interface SubInterfaceGetter extends SuperInterfaceGetter {}\n" +
				"        public interface SubInterfaceSetter extends SuperInterfaceSetter {}\n" +
				"}\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();
	}

}
