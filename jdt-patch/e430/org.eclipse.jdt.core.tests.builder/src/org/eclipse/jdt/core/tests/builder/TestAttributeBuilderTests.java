/*******************************************************************************
 * Copyright (c) 2017, 2018 Till Brychcy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Till Brychcy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;
import junit.framework.Test;

public class TestAttributeBuilderTests extends BuilderTests {
	static {
		 // TESTS_NAMES = new String[] { "testIncrementalBuildTestOnlyProject" };
	}


	public TestAttributeBuilderTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(TestAttributeBuilderTests.class);
	}

	public void testWithProjectAsMainDependency() throws JavaModelException {
		IPath project1Path = env.addProject("Project1");
		env.removePackageFragmentRoot(project1Path, "");
		IPath src1 = env.addPackageFragmentRoot(project1Path, "src", null, "bin");
		IPath tests1 = env.addTestPackageFragmentRoot(project1Path, "tests");
		env.addExternalJars(project1Path, Util.getJavaClassLibs());

		env.addClass(src1, "p1", "P1Class",
				"package p1;\n" +
				"\n" +
				"public class P1Class {\n"+
				"}\n"
				);
		env.addClass(src1, "p1", "Production1",
				"package p1;\n" +
				"\n" +
				"public class Production1 {\n" +
				"	void p1() {\n" +
				"		new P1Class(); // ok\n" +
				"		new T1Class(); // forbidden\n" +
				"	}\n" +
				"}\n" +
				""
				);
		env.addClass(tests1, "p1", "T1Class",
				"package p1;\n" +
				"\n" +
				"public class T1Class {\n"+
				"}\n"
				);
		env.addClass(tests1, "p1", "Test1",
				"package p1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	void test1() {\n" +
				"		new P1Class(); // ok\n" +
				"		new T1Class(); // ok\n" +
				"	}\n" +
				"}\n" +
				""
				);

		IPath project2Path = env.addProject("Project2");
		env.removePackageFragmentRoot(project2Path, "");
		IPath src2 = env.addPackageFragmentRoot(project2Path, "src", null, "bin");
		IPath tests2 = env.addTestPackageFragmentRoot(project2Path, "tests");
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path);
		env.addClass(src2, "p2", "P2Class",
				"package p2;\n" +
				"\n" +
				"public class P2Class {\n"+
				"}\n"
				);
		env.addClass(src2, "p2", "Production2",
				"package p2;\n" +
				"\n" +
				"import p1.P1Class;\n" +
				"import p1.T1Class;\n" +
				"\n" +
				"public class Production2 {\n" +
				"	void p2() {\n" +
				"		new P1Class(); // ok\n" +
				"		new P2Class(); // ok\n" +
				"		new T1Class(); // forbidden\n" +
				"		new T2Class(); // forbidden\n" +
				"	}\n" +
				"}\n" +
				""
				);
		env.addClass(tests2, "p2", "T2Class",
				"package p2;\n" +
				"\n" +
				"public class T2Class {\n"+
				"}\n"
				);
		env.addClass(tests2, "p2", "Test2",
				"package p2;\n" +
				"\n" +
				"import p1.P1Class;\n" +
				"import p1.T1Class;\n" +
				"\n" +
				"public class Test2 {\n" +
				"	void test2() {\n" +
				"		new P1Class(); // ok\n" +
				"		new P2Class(); // ok\n" +
				"		new T1Class(); // ok\n" +
				"		new T2Class(); // ok\n" +
				"	}\n" +
				"}\n" +
				""
				);

		fullBuild();
		expectingProblemsFor(env.getWorkspaceRootPath(), "Problem : T1Class cannot be resolved to a type [ resource : </Project1/src/p1/Production1.java> range : <82,89> category : <40> severity : <2>]\n" +
				"Problem : T1Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <144,151> category : <40> severity : <2>]\n" +
				"Problem : T2Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <174,181> category : <40> severity : <2>]\n" +
				"Problem : The import p1.T1Class cannot be resolved [ resource : </Project2/src/p2/Production2.java> range : <39,49> category : <30> severity : <2>]");
	}
	public void testWithProjectAsTestDependency() throws JavaModelException {
		IPath project1Path = env.addProject("Project1");
		env.removePackageFragmentRoot(project1Path, "");
		IPath src1 = env.addPackageFragmentRoot(project1Path, "src", null, "bin");
		IPath tests1 = env.addTestPackageFragmentRoot(project1Path, "tests");
		env.addExternalJars(project1Path, Util.getJavaClassLibs());

		env.addClass(src1, "p1", "P1Class",
				"package p1;\n" +
				"\n" +
				"public class P1Class {\n"+
				"}\n"
				);
		env.addClass(src1, "p1", "Production1",
				"package p1;\n" +
				"\n" +
				"public class Production1 {\n" +
				"	void p1() {\n" +
				"		new P1Class(); // ok\n" +
				"		new T1Class(); // forbidden\n" +
				"	}\n" +
				"}\n" +
				""
				);
		env.addClass(tests1, "p1", "T1Class",
				"package p1;\n" +
				"\n" +
				"public class T1Class {\n"+
				"}\n"
				);
		env.addClass(tests1, "p1", "Test1",
				"package p1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	void test1() {\n" +
				"		new P1Class(); // ok\n" +
				"		new T1Class(); // ok\n" +
				"	}\n" +
				"}\n" +
				""
				);

		IPath project2Path = env.addProject("Project2");
		env.removePackageFragmentRoot(project2Path, "");
		IPath src2 = env.addPackageFragmentRoot(project2Path, "src", null, "bin");
		IPath tests2 = env.addTestPackageFragmentRoot(project2Path, "tests");
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredTestProject(project2Path, project1Path);
		env.addClass(src2, "p2", "P2Class",
				"package p2;\n" +
				"\n" +
				"public class P2Class {\n"+
				"}\n"
				);
		env.addClass(src2, "p2", "Production2",
				"package p2;\n" +
				"\n" +
				"import p1.P1Class;\n" +
				"import p1.T1Class;\n" +
				"\n" +
				"public class Production2 {\n" +
				"	void p2() {\n" +
				"		new P1Class(); // forbidden\n" +
				"		new P2Class(); // ok\n" +
				"		new T1Class(); // forbidden\n" +
				"		new T2Class(); // forbidden\n" +
				"	}\n" +
				"}\n" +
				""
				);
		env.addClass(tests2, "p2", "T2Class",
				"package p2;\n" +
				"\n" +
				"public class T2Class {\n"+
				"}\n"
				);
		env.addClass(tests2, "p2", "Test2",
				"package p2;\n" +
				"\n" +
				"import p1.P1Class;\n" +
				"import p1.T1Class;\n" +
				"\n" +
				"public class Test2 {\n" +
				"	void test2() {\n" +
				"		new P1Class(); // ok\n" +
				"		new P2Class(); // ok\n" +
				"		new T1Class(); // ok\n" +
				"		new T2Class(); // ok\n" +
				"	}\n" +
				"}\n" +
				""
				);

		fullBuild();
		expectingProblemsFor(env.getWorkspaceRootPath(), "Problem : P1Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <98,105> category : <40> severity : <2>]\n" +
				"Problem : T1Class cannot be resolved to a type [ resource : </Project1/src/p1/Production1.java> range : <82,89> category : <40> severity : <2>]\n" +
				"Problem : T1Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <151,158> category : <40> severity : <2>]\n" +
				"Problem : T2Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <181,188> category : <40> severity : <2>]\n" +
				"Problem : The import p1 cannot be resolved [ resource : </Project2/src/p2/Production2.java> range : <20,22> category : <30> severity : <2>]\n" +
				"Problem : The import p1 cannot be resolved [ resource : </Project2/src/p2/Production2.java> range : <39,41> category : <30> severity : <2>]");
	}
	public void testWithProjectAsMainDependencyWithoutTestCode() throws JavaModelException {
		IPath project1Path = env.addProject("Project1");
		env.removePackageFragmentRoot(project1Path, "");
		IPath src1 = env.addPackageFragmentRoot(project1Path, "src", null, "bin");
		IPath tests1 = env.addTestPackageFragmentRoot(project1Path, "tests");
		env.addExternalJars(project1Path, Util.getJavaClassLibs());

		env.addClass(src1, "p1", "P1Class",
				"package p1;\n" +
				"\n" +
				"public class P1Class {\n"+
				"}\n"
				);
		env.addClass(src1, "p1", "Production1",
				"package p1;\n" +
				"\n" +
				"public class Production1 {\n" +
				"	void p1() {\n" +
				"		new P1Class(); // ok\n" +
				"		new T1Class(); // forbidden\n" +
				"	}\n" +
				"}\n" +
				""
				);
		env.addClass(tests1, "p1", "T1Class",
				"package p1;\n" +
				"\n" +
				"public class T1Class {\n"+
				"}\n"
				);
		env.addClass(tests1, "p1", "Test1",
				"package p1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	void test1() {\n" +
				"		new P1Class(); // ok\n" +
				"		new T1Class(); // ok\n" +
				"	}\n" +
				"}\n" +
				""
				);

		IPath project2Path = env.addProject("Project2");
		env.removePackageFragmentRoot(project2Path, "");
		IPath src2 = env.addPackageFragmentRoot(project2Path, "src", null, "bin");
		IPath tests2 = env.addTestPackageFragmentRoot(project2Path, "tests");
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProjectWithoutTestCode(project2Path, project1Path);
		env.addClass(src2, "p2", "P2Class",
				"package p2;\n" +
				"\n" +
				"public class P2Class {\n"+
				"}\n"
				);
		env.addClass(src2, "p2", "Production2",
				"package p2;\n" +
				"\n" +
				"import p1.P1Class;\n" +
				"import p1.T1Class;\n" +
				"\n" +
				"public class Production2 {\n" +
				"	void p2() {\n" +
				"		new P1Class(); // ok\n" +
				"		new P2Class(); // ok\n" +
				"		new T1Class(); // forbidden\n" +
				"		new T2Class(); // forbidden\n" +
				"	}\n" +
				"}\n" +
				""
				);
		env.addClass(tests2, "p2", "T2Class",
				"package p2;\n" +
				"\n" +
				"public class T2Class {\n"+
				"}\n"
				);
		env.addClass(tests2, "p2", "Test2",
				"package p2;\n" +
				"\n" +
				"import p1.P1Class;\n" +
				"import p1.T1Class;\n" +
				"\n" +
				"public class Test2 {\n" +
				"	void test2() {\n" +
				"		new P1Class(); // ok\n" +
				"		new P2Class(); // ok\n" +
				"		new T1Class(); // ok\n" +
				"		new T2Class(); // ok\n" +
				"	}\n" +
				"}\n" +
				""
				);

		fullBuild();
		expectingProblemsFor(env.getWorkspaceRootPath(), "Problem : T1Class cannot be resolved to a type [ resource : </Project1/src/p1/Production1.java> range : <82,89> category : <40> severity : <2>]\n" +
				"Problem : T1Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <144,151> category : <40> severity : <2>]\n" +
				"Problem : T1Class cannot be resolved to a type [ resource : </Project2/tests/p2/Test2.java> range : <141,148> category : <40> severity : <2>]\n" +
				"Problem : T2Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <174,181> category : <40> severity : <2>]\n" +
				"Problem : The import p1.T1Class cannot be resolved [ resource : </Project2/src/p2/Production2.java> range : <39,49> category : <30> severity : <2>]\n" +
				"Problem : The import p1.T1Class cannot be resolved [ resource : </Project2/tests/p2/Test2.java> range : <39,49> category : <30> severity : <2>]");
	}
	public void testWithProjectAsTestDependencyWithoutTestCode() throws JavaModelException {
		IPath project1Path = env.addProject("Project1");
		env.removePackageFragmentRoot(project1Path, "");
		IPath src1 = env.addPackageFragmentRoot(project1Path, "src", null, "bin");
		IPath tests1 = env.addTestPackageFragmentRoot(project1Path, "tests");
		env.addExternalJars(project1Path, Util.getJavaClassLibs());

		env.addClass(src1, "p1", "P1Class",
				"package p1;\n" +
				"\n" +
				"public class P1Class {\n"+
				"}\n"
				);
		env.addClass(src1, "p1", "Production1",
				"package p1;\n" +
				"\n" +
				"public class Production1 {\n" +
				"	void p1() {\n" +
				"		new P1Class(); // ok\n" +
				"		new T1Class(); // forbidden\n" +
				"	}\n" +
				"}\n" +
				""
				);
		env.addClass(tests1, "p1", "T1Class",
				"package p1;\n" +
				"\n" +
				"public class T1Class {\n"+
				"}\n"
				);
		env.addClass(tests1, "p1", "Test1",
				"package p1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	void test1() {\n" +
				"		new P1Class(); // ok\n" +
				"		new T1Class(); // ok\n" +
				"	}\n" +
				"}\n" +
				""
				);

		IPath project2Path = env.addProject("Project2");
		env.removePackageFragmentRoot(project2Path, "");
		IPath src2 = env.addPackageFragmentRoot(project2Path, "src", null, "bin");
		IPath tests2 = env.addTestPackageFragmentRoot(project2Path, "tests");
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredTestProjectWithoutTestCode(project2Path, project1Path);
		env.addClass(src2, "p2", "P2Class",
				"package p2;\n" +
				"\n" +
				"public class P2Class {\n"+
				"}\n"
				);
		env.addClass(src2, "p2", "Production2",
				"package p2;\n" +
				"\n" +
				"import p1.P1Class;\n" +
				"import p1.T1Class;\n" +
				"\n" +
				"public class Production2 {\n" +
				"	void p2() {\n" +
				"		new P1Class(); // forbidden\n" +
				"		new P2Class(); // ok\n" +
				"		new T1Class(); // forbidden\n" +
				"		new T2Class(); // forbidden\n" +
				"	}\n" +
				"}\n" +
				""
				);
		env.addClass(tests2, "p2", "T2Class",
				"package p2;\n" +
				"\n" +
				"public class T2Class {\n"+
				"}\n"
				);
		env.addClass(tests2, "p2", "Test2",
				"package p2;\n" +
				"\n" +
				"import p1.P1Class;\n" +
				"import p1.T1Class;\n" +
				"\n" +
				"public class Test2 {\n" +
				"	void test2() {\n" +
				"		new P1Class(); // ok\n" +
				"		new P2Class(); // ok\n" +
				"		new T1Class(); // ok\n" +
				"		new T2Class(); // ok\n" +
				"	}\n" +
				"}\n" +
				""
				);

		fullBuild();
		expectingProblemsFor(env.getWorkspaceRootPath(), "Problem : P1Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <98,105> category : <40> severity : <2>]\n" +
				"Problem : T1Class cannot be resolved to a type [ resource : </Project1/src/p1/Production1.java> range : <82,89> category : <40> severity : <2>]\n" +
				"Problem : T1Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <151,158> category : <40> severity : <2>]\n" +
				"Problem : T1Class cannot be resolved to a type [ resource : </Project2/tests/p2/Test2.java> range : <141,148> category : <40> severity : <2>]\n" +
				"Problem : T2Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <181,188> category : <40> severity : <2>]\n" +
				"Problem : The import p1 cannot be resolved [ resource : </Project2/src/p2/Production2.java> range : <20,22> category : <30> severity : <2>]\n" +
				"Problem : The import p1 cannot be resolved [ resource : </Project2/src/p2/Production2.java> range : <39,41> category : <30> severity : <2>]\n" +
				"Problem : The import p1.T1Class cannot be resolved [ resource : </Project2/tests/p2/Test2.java> range : <39,49> category : <30> severity : <2>]");
	}

	public void testIncrementalBuildMainChange() throws JavaModelException {
		IPath project1Path = env.addProject("Project1");
		env.removePackageFragmentRoot(project1Path, "");
		IPath src1 = env.addPackageFragmentRoot(project1Path, "src", null, "bin");
		IPath tests1 = env.addTestPackageFragmentRoot(project1Path, "tests");
		env.addExternalJars(project1Path, Util.getJavaClassLibs());

		env.addClass(src1, "p1", "P1Class",
				"package p1;\n" +
				"\n" +
				"class P1Class {\n"+
				"}\n"
				);
		env.addClass(tests1, "p1", "T1Class",
				"package p1;\n" +
				"\n" +
				"public class T1Class extends P1Class {\n"+
				"}\n"
				);

		IPath project2Path = env.addProject("Project2");
		env.removePackageFragmentRoot(project2Path, "");
		IPath src2 = env.addPackageFragmentRoot(project2Path, "src", null, "bin");
		IPath tests2 = env.addTestPackageFragmentRoot(project2Path, "tests");
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path);
		env.addClass(src2, "p2", "P2Class",
				"package p2;\n" +
				"\n" +
				"public class P2Class extends p1.P1Class {\n"+
				"}\n"
				);
		env.addClass(tests2, "p2", "T2Class",
				"package p2;\n" +
				"\n" +
				"public class T2Class {\n"+
				"}\n"
				);
		env.addClass(tests2, "p2", "Test2",
				"package p2;\n" +
				"\n" +
				"public class Test2 {\n" +
				"	void test2() {\n" +
				"		new P2Class();\n" +
				"	}\n" +
				"}\n" +
				""
				);

		fullBuild();
		expectingProblemsFor(env.getWorkspaceRootPath(), "Problem : The type p1.P1Class is not visible [ resource : </Project2/src/p2/P2Class.java> range : <42,52> category : <40> severity : <2>]");

		env.addClass(src1, "p1", "P1Class",
				"package p1;\n" +
				"\n" +
				"public class P1Class {\n"+
				"}\n"
				);
		incrementalBuild();
		expectingNoProblems();
		expectingCompiledClasses(new String[] { "p1.P1Class", "p1.T1Class", "p2.P2Class", "p2.Test2" });
		expectingCompilingOrder(new String[] { "/Project1/src/p1/P1Class.java", "/Project1/tests/p1/T1Class.java",
				"/Project2/src/p2/P2Class.java", "/Project2/tests/p2/Test2.java" });
	}
	public void testIncrementalBuildTestChange() throws JavaModelException {
		IPath project1Path = env.addProject("Project1");
		env.removePackageFragmentRoot(project1Path, "");
		IPath src1 = env.addPackageFragmentRoot(project1Path, "src", null, "bin");
		IPath tests1 = env.addTestPackageFragmentRoot(project1Path, "tests");
		env.addExternalJars(project1Path, Util.getJavaClassLibs());

		env.addClass(src1, "p1", "P1Class",
				"package p1;\n" +
				"\n" +
				"public class P1Class {\n"+
				"}\n"
				);
		env.addClass(tests1, "p1", "T1Class",
				"package p1;\n" +
				"\n" +
				"public class T1Class {\n"+
				"}\n"
				);
		env.addClass(tests1, "p1", "Test1",
				"package p1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	void test1() {\n" +
				"		new P1Class();" +
				"		new T1Class();" +
				"	}\n" +
				"}\n" +
				""
				);

		IPath project2Path = env.addProject("Project2");
		env.removePackageFragmentRoot(project2Path, "");
		IPath src2 = env.addPackageFragmentRoot(project2Path, "src", null, "bin");
		IPath tests2 = env.addTestPackageFragmentRoot(project2Path, "tests");
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path);
		env.addClass(src2, "p2", "P2Class",
				"package p2;\n" +
				"\n" +
				"public class P2Class extends p1.P1Class {\n"+
				"}\n"
				);
		env.addClass(tests2, "p2", "T2Class",
				"package p2;\n" +
				"\n" +
				"public class T2Class extends p1.T1Class {\n"+
				"}\n"
				);
		env.addClass(tests2, "p2", "Test2",
				"package p2;\n" +
				"\n" +
				"public class Test2 extends p2.T2Class {\n" +
				"	void test2(T2Class t) {\n" +
				"	}\n" +
				"}\n" +
				""
				);

		fullBuild();
		expectingNoProblems();

		env.addClass(tests1, "p1", "T1Class",
				"package p1;\n" +
				"\n" +
				"public class T1Class extends P1Class {\n"+
				"}\n"
				);
		incrementalBuild();
		expectingNoProblems();
		expectingCompiledClasses(new String[] { "p1.T1Class", "p1.Test1", "p2.T2Class", "p2.Test2" });
		expectingCompilingOrder(new String[] { "/Project1/tests/p1/T1Class.java", "/Project1/tests/p1/Test1.java",
				"/Project2/tests/p2/T2Class.java", "/Project2/tests/p2/Test2.java" });
	}

	public void testIncrementalBuildTestOnlyProject() throws JavaModelException {
		IPath project1Path = env.addProject("Project1");
		env.removePackageFragmentRoot(project1Path, "");
		IPath tests1 = env.addTestPackageFragmentRoot(project1Path, "tests");
		env.addExternalJars(project1Path, Util.getJavaClassLibs());

		env.addClass(tests1, "p1", "T1Class",
				"package p1;\n" +
				"\n" +
				"public class T1Class {\n"+
				"}\n"
				);
		env.addClass(tests1, "p1", "Test1",
				"package p1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	void test1() {\n" +
				"		new T1Class();" +
				"	}\n" +
				"}\n" +
				""
				);

		fullBuild();
		expectingNoProblems();

		IPath test1 = env.addClass(tests1, "p1", "Test1",
				"package p1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	void test1() {\n" +
				"		new X1Class();" +
				"	}\n" +
				"}\n" +
				""
				);
		incrementalBuild();
		expectingProblemsFor(
				test1,
			"Problem : X1Class cannot be resolved to a type [ resource : </Project1/tests/p1/Test1.java> range : <56,63> category : <40> severity : <2>]"
		);
		expectingCompiledClasses(new String[] { "p1.Test1" });
		expectingCompilingOrder(new String[] { "/Project1/tests/p1/Test1.java"});
	}

	public void testClasspathEntryTestAttributeChanges() throws JavaModelException {
		IPath project1Path = env.addProject("Project1");
		env.removePackageFragmentRoot(project1Path, "");
		IPath src1 = env.addPackageFragmentRoot(project1Path, "src", null, "bin");
		IPath tests1 = env.addTestPackageFragmentRoot(project1Path, "tests");
		env.addExternalJars(project1Path, Util.getJavaClassLibs());

		env.addClass(src1, "p1", "P1Class",
				"package p1;\n" +
				"\n" +
				"public class P1Class {\n"+
				"}\n"
				);
		env.addClass(src1, "p1", "P1Unrelated",
				"package p1;\n" +
				"\n" +
				"public class P1Unrelated {\n"+
				"}\n"
				);
		env.addClass(src1, "p1", "Production1",
				"package p1;\n" +
				"\n" +
				"public class Production1 {\n" +
				"	void p1() {\n" +
				"		new P1Class(); // ok\n" +
				"		new T1Class(); // forbidden\n" +
				"	}\n" +
				"}\n" +
				""
				);
		env.addClass(tests1, "p1", "T1Class",
				"package p1;\n" +
				"\n" +
				"public class T1Class {\n"+
				"}\n"
				);
		env.addClass(tests1, "p1", "Test1",
				"package p1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	void test1() {\n" +
				"		new P1Class(); // ok\n" +
				"		new T1Class(); // ok\n" +
				"	}\n" +
				"}\n" +
				""
				);

		IPath project2Path = env.addProject("Project2");
		env.removePackageFragmentRoot(project2Path, "");
		IPath src2 = env.addPackageFragmentRoot(project2Path, "src", null, "bin");
		IPath tests2 = env.addTestPackageFragmentRoot(project2Path, "tests");
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path);
		env.addClass(src2, "p2", "P2Class",
				"package p2;\n" +
				"\n" +
				"public class P2Class {\n"+
				"}\n"
				);
		env.addClass(src1, "p2", "P2Unrelated",
				"package p2;\n" +
				"\n" +
				"public class P2Unrelated {\n"+
				"}\n"
				);
		env.addClass(src2, "p2", "Production2",
				"package p2;\n" +
				"\n" +
				"import p1.P1Class;\n" +
				"import p1.T1Class;\n" +
				"\n" +
				"public class Production2 {\n" +
				"	void p2() {\n" +
				"		new P1Class(); // ok\n" +
				"		new P2Class(); // ok\n" +
				"		new T1Class(); // forbidden\n" +
				"		new T2Class(); // forbidden\n" +
				"	}\n" +
				"}\n" +
				""
				);
		env.addClass(tests2, "p2", "T2Class",
				"package p2;\n" +
				"\n" +
				"public class T2Class {\n"+
				"}\n"
				);
		env.addClass(tests2, "p2", "Test2",
				"package p2;\n" +
				"\n" +
				"import p1.P1Class;\n" +
				"import p1.T1Class;\n" +
				"\n" +
				"public class Test2 {\n" +
				"	void test2() {\n" +
				"		new P1Class(); // ok\n" +
				"		new P2Class(); // ok\n" +
				"		new T1Class(); // ok\n" +
				"		new T2Class(); // ok\n" +
				"	}\n" +
				"}\n" +
				""
				);

		fullBuild();
		expectingProblemsFor(env.getWorkspaceRootPath(), "Problem : T1Class cannot be resolved to a type [ resource : </Project1/src/p1/Production1.java> range : <82,89> category : <40> severity : <2>]\n" +
				"Problem : T1Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <144,151> category : <40> severity : <2>]\n" +
				"Problem : T2Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <174,181> category : <40> severity : <2>]\n" +
				"Problem : The import p1.T1Class cannot be resolved [ resource : </Project2/src/p2/Production2.java> range : <39,49> category : <30> severity : <2>]");

		env.changePackageFragmentRootTestAttribute(project2Path, tests2, false);
		incrementalBuild();
		expectingProblemsFor(env.getWorkspaceRootPath(), "Problem : T1Class cannot be resolved to a type [ resource : </Project1/src/p1/Production1.java> range : <82,89> category : <40> severity : <2>]\n" +
				"Problem : T1Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <144,151> category : <40> severity : <2>]\n" +
				"Problem : T1Class cannot be resolved to a type [ resource : </Project2/tests/p2/Test2.java> range : <141,148> category : <40> severity : <2>]\n" +
				"Problem : The import p1.T1Class cannot be resolved [ resource : </Project2/src/p2/Production2.java> range : <39,49> category : <30> severity : <2>]\n" +
				"Problem : The import p1.T1Class cannot be resolved [ resource : </Project2/tests/p2/Test2.java> range : <39,49> category : <30> severity : <2>]");
		expectingCompiledClasses(new String[]{"p2.P2Class","p2.Production2","p2.T2Class","p2.Test2"});

		env.changePackageFragmentRootTestAttribute(project1Path, tests1, false);
		incrementalBuild();
		expectingNoProblems();
		expectingCompiledClasses(new String[]{"p1.P1Class", "p1.P1Unrelated","p1.Production1","p1.T1Class","p1.Test1","p2.P2Class","p2.P2Unrelated","p2.Production2","p2.T2Class","p2.Test2"});

		env.changePackageFragmentRootTestAttribute(project2Path, tests2, true);
		incrementalBuild();
		expectingProblemsFor(env.getWorkspaceRootPath(), "Problem : T2Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <174,181> category : <40> severity : <2>]");
		expectingCompiledClasses(new String[]{"p2.P2Class","p2.Production2","p2.T2Class","p2.Test2"});

		env.changePackageFragmentRootTestAttribute(project1Path, tests1, true);
		incrementalBuild();
		expectingProblemsFor(env.getWorkspaceRootPath(), "Problem : T1Class cannot be resolved to a type [ resource : </Project1/src/p1/Production1.java> range : <82,89> category : <40> severity : <2>]\n" +
				"Problem : T1Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <144,151> category : <40> severity : <2>]\n" +
				"Problem : T2Class cannot be resolved to a type [ resource : </Project2/src/p2/Production2.java> range : <174,181> category : <40> severity : <2>]\n" +
				"Problem : The import p1.T1Class cannot be resolved [ resource : </Project2/src/p2/Production2.java> range : <39,49> category : <30> severity : <2>]");
		expectingCompiledClasses(new String[]{"p1.P1Class", "p1.P1Unrelated","p1.Production1","p1.T1Class","p1.Test1","p2.P2Class","p2.P2Unrelated","p2.Production2","p2.T2Class","p2.Test2"});

		env.changePackageFragmentRootTestAttribute(project2Path, tests2, false);
		env.changePackageFragmentRootTestAttribute(project1Path, tests1, false);
		incrementalBuild();
		expectingNoProblems();
		expectingCompiledClasses(new String[]{"p1.P1Class", "p1.P1Unrelated","p1.Production1","p1.T1Class","p1.Test1","p2.P2Class","p2.P2Unrelated","p2.Production2","p2.T2Class","p2.Test2"});
	}

	public void testExternalTestJarChanged() throws CoreException, java.io.IOException {
		IPath projectPath = env.addProject("Project");
		env.removePackageFragmentRoot(projectPath, "");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		IPath tests = env.addTestPackageFragmentRoot(projectPath, "tests");
		IPath classTest = env.addClass(tests, "p", "X",
			"package p;\n"+
			"public class X {\n" +
			"  void foo() {\n" +
			"    new q.Y().bar();\n" +
			"  }\n" +
			"}"
		);
		String externalJar = Util.getOutputDirectory() + java.io.File.separator + "test.jar";
		Util.createJar(
			new String[] {
				"q/Y.java",
				"package q;\n" +
				"public class Y {\n" +
				"}"
			},
			new HashMap<>(),
			externalJar
		);
		fullBuild();
		expectingProblemsFor(
			classTest,
			"Problem : q cannot be resolved to a type [ resource : </Project/tests/p/X.java> range : <51,52> category : <40> severity : <2>]"
		);
		env.addExternalTestJar(projectPath, externalJar, false);

		incrementalBuild();
		expectingProblemsFor(
			classTest,
			"Problem : The method bar() is undefined for the type Y [ resource : </Project/tests/p/X.java> range : <57,60> category : <50> severity : <2>]"
		);

		Util.createJar(
			new String[] {
				"q/Y.java",
				"package q;\n" +
				"public class Y {\n" +
				"  public void bar() {\n" +
				"  }\n" +
				"}"
			},
			new HashMap<>(),
			externalJar
		);

		env.getProject(projectPath).touch(null);

		incrementalBuild();
		expectingNoProblems();
	}

	public void testBug536868() throws JavaModelException {
		IPath project1Path = env.addProject("Project1");
		env.removePackageFragmentRoot(project1Path, "");
		IPath tests1 = env.addTestPackageFragmentRoot(project1Path, "tests");
		env.addExternalJars(project1Path, Util.getJavaClassLibs());

		env.addClass(tests1, "p1", "T1Class",
				"package p1;\n" +
				"\n" +
				"public class T1Class {\n"+
				"}\n"
				);

		// project X just reexports Project1 without test code
		IPath projectXPath = env.addProject("ProjectX");
		env.removePackageFragmentRoot(projectXPath, "");
		env.addRequiredProjectWithoutTestCode(projectXPath, project1Path, /* isExported */ true);


		IPath project2Path = env.addProject("Project2");
		env.removePackageFragmentRoot(project2Path, "");
		IPath tests2 = env.addTestPackageFragmentRoot(project2Path, "tests");
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, projectXPath);
		env.addRequiredTestProject(project2Path, project1Path);
		env.addClass(tests2, "p2", "Test2",
				"package p2;\n" +
				"\n" +
				"import p1.T1Class;\n" +
				"\n" +
				"public class Test2 {\n" +
				"	void test2() {\n" +
				"		new T1Class(); // ok\n" +
				"	}\n" +
				"}\n" +
				""
				);

		fullBuild();
		expectingNoProblems();
	}
	public void testBug559965() throws JavaModelException {
		// Bug 559965 - No recompilation when deleting java file from test-source-folder
		IPath project1Path = env.addProject("Project1");
		env.removePackageFragmentRoot(project1Path, "");
		IPath src1 = env.addPackageFragmentRoot(project1Path, "src", null, "bin");
		assertNotNull(src1);

		IPath tests1 = env.addTestPackageFragmentRoot(project1Path, "tests");
		env.addExternalJars(project1Path, Util.getJavaClassLibs());

		env.addClass(tests1, "p1", "T1Class",
				"package p1;\n" +
				"\n" +
				"public class T1Class {\n"+
				"}\n"
				);
		env.addClass(tests1, "p1", "Test1",
				"package p1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	void test1() {\n" +
				"		new T1Class();" +
				"	}\n" +
				"}\n" +
				""
				);

		fullBuild();
		expectingNoProblems();

		env.removeClass(tests1, "p1/T1Class");
		incrementalBuild();

		expectingProblemsFor(env.getWorkspaceRootPath(), "Problem : T1Class cannot be resolved to a type [ resource : </Project1/tests/p1/Test1.java> range : <56,63> category : <40> severity : <2>]");
	}
}
