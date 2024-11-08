/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings("rawtypes")
public class PackageInfoTest extends BuilderTests {

public PackageInfoTest(String name) {
	super(name);
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "testBug374063" };
//	TESTS_NUMBERS = new int[] { 3 };
//	TESTS_RANGE = new int[] { 21, 50 };
}
public static Test suite() {
	return buildTestSuite(PackageInfoTest.class);
}
public void test001() throws JavaModelException {
	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

	IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	env.addClass(root, "pack", "Annot", //$NON-NLS-1$ //$NON-NLS-2$
		"package pack;\n" + //$NON-NLS-1$
		"public @interface Annot {}" //$NON-NLS-1$
	);

	incrementalBuild(projectPath);

	IPath packageInfoPath = env.addFile(root, "pack/package-info.java", //$NON-NLS-1$
		"@Annot package p1" //$NON-NLS-1$
	);

	incrementalBuild(projectPath);
	expectingOnlyProblemsFor(packageInfoPath);
	final Problem[] problems = env.getProblems();
	Arrays.sort(problems);
	assertNotNull(problems);
	final StringWriter stringWriter = new StringWriter();
	final PrintWriter writer = new PrintWriter(stringWriter);
	final int problemsLength = problems.length;
	if (problemsLength == 1) {
		writer.print(problems[0].getMessage());
	} else {
		for (int i = 0; i < problemsLength - 1; i++)
			writer.println(problems[i].getMessage());
		writer.print(problems[problemsLength - 1].getMessage());
	}
	writer.close();
	final String expectedOutput =
		"Syntax error on token \"p1\", ; expected after this token\n" +
		"The declared package \"p1\" does not match the expected package \"pack\"";
	assertSourceEquals("Different messages", expectedOutput, stringWriter.toString());
}
public void test002() throws JavaModelException {
	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

	IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	env.addClass(root, "testcase", "Main", //$NON-NLS-1$ //$NON-NLS-2$
		"package testcase;\n" +
		"\n" +
		"public class Main {\n" +
		"    public static void main(String[] argv) throws Exception {\n" +
		"		Package pkg = Package.getPackage(\"testcase\");\n" +
		"		System.out.print(pkg.getAnnotation(TestAnnotation.class));\n" +
		"		pkg = Class.forName(\"testcase.package-info\").getPackage();\n" +
		"		System.out.print(pkg.getAnnotation(TestAnnotation.class));\n" +
		"    }\n" +
		"}"
	);

	env.addClass(root, "testcase", "TestAnnotation", //$NON-NLS-1$ //$NON-NLS-2$
		"package testcase;\n" +
		"\n" +
		"import static java.lang.annotation.ElementType.PACKAGE;\n" +
		"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" +
		"\n" +
		"import java.lang.annotation.Retention;\n" +
		"import java.lang.annotation.Target;\n" +
		"\n" +
		"@Target(PACKAGE)\n" +
		"@Retention(RUNTIME)\n" +
		"public @interface TestAnnotation {\n" +
		"}"
	);

	env.addFile(root, "testcase/package-info.java", //$NON-NLS-1$
		"@TestAnnotation package testcase;" //$NON-NLS-1$
	);
	incrementalBuild(projectPath);
	String javaVersion = System.getProperty("java.version");
	if (javaVersion != null && JavaCore.compareJavaVersions(javaVersion, "9") >= 0) {
		expectingProblemsFor(new Path("/Project/src/testcase/Main.java"),
				"Problem : The method getPackage(String) from the type Package is deprecated [ resource : </Project/src/testcase/Main.java> range : <125,147> category : <110> severity : <1>]");
	} else {
		expectingNoProblems();
	}
	executeClass(projectPath, "testcase.Main", "@testcase.TestAnnotation()@testcase.TestAnnotation()", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
}
public void test003() throws JavaModelException {
	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

	IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	env.addPackage(root, "testcase");
	IPath packageInfoPath = env.addFile(root, "testcase/package-info.java", //$NON-NLS-1$
		"" //$NON-NLS-1$
	);

	incrementalBuild(projectPath);
	expectingOnlySpecificProblemFor(
		packageInfoPath,
		new Problem("testcase/package-info.java", "The declared package \"\" does not match the expected package \"testcase\"", packageInfoPath, 0, 0, CategorizedProblem.CAT_INTERNAL, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
}
// test for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=252555 : NPE
// on duplicate package-info
public void test004() throws JavaModelException {
	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

	IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	IPath otherRoot = env.addPackageFragmentRoot(projectPath, "test"); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	env.addPackage(root, "my.foo");
	env.addPackage(otherRoot, "my.foo");

	env.addFile(root, "my/foo/package-info.java", //$NON-NLS-1$
		"/**\n" +
		"* A demo package for foo.\n" +
		"*/\n" +
		"package my.foo;\n"
	);

	IPath otherPackageInfoPath = env.addFile(otherRoot,
		"my/foo/package-info.java", //$NON-NLS-1$
		"/**\n" +
		"* A demo package for foo.\n" +
		"*/\n" +
		"package my.foo;\n"
	);

	incrementalBuild(projectPath);
	expectingOnlySpecificProblemFor(
		otherPackageInfoPath,
		new Problem("my/foo/package-info.java", "The type package-info is already defined", otherPackageInfoPath, 0, 0, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
}
// test for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=258145 : JME
// on duplicate package-info
public void test258145() throws JavaModelException {
	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

	IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	IPath otherRoot = env.addPackageFragmentRoot(projectPath, "test"); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	env.addPackage(root, "my.foo");
	env.addFile(root, "my/foo/package-info.java", //$NON-NLS-1$
		"/**\n" +
		"* A demo package for foo.\n" +
		"*/\n" +
		"package my.foo;\n"
	);

	fullBuild(projectPath);

	env.addPackage(otherRoot, "my.foo");

	IPath otherPackageInfoPath = env.addFile(otherRoot,
		"my/foo/package-info.java", //$NON-NLS-1$
		"/**\n" +
		"* A demo package for foo.\n" +
		"*/\n" +
		"package my.foo;\n"
	);

	incrementalBuild(projectPath);
	expectingOnlySpecificProblemFor(
		otherPackageInfoPath,
		new Problem("my/foo/package-info.java", "The type package-info is already defined", otherPackageInfoPath, 0, 0, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=323785
// (NPE upon creation/deletion of package-info.java in default package)
public void test323785 () throws JavaModelException {

	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, "");

	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	env.setOutputFolder(projectPath, "bin");

	fullBuild(projectPath);

	env.addFile(root, "package-info.java",	"");

	incrementalBuild(projectPath);
	expectingNoProblems();

}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=323785
// verify that changes to package info containing secondary types do trigger incremental build.
public void test323785a () throws JavaModelException {

	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, "");

	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	env.setOutputFolder(projectPath, "bin");

	IPath xJavaPath = env.addFile(root, "X.java",	"class X extends Y {}\n");
	fullBuild(projectPath);
	env.addFile(root, "package-info.java",	"class Y {}\n");
	incrementalBuild(projectPath);
	expectingNoProblems();
	env.addFile(root, "package-info.java",	"final class Y {}\n");
	incrementalBuild(projectPath);
	expectingOnlySpecificProblemFor(
			xJavaPath,
			new Problem("X.java", "The type X cannot subclass the final class Y", xJavaPath, 16, 17, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=372012
// test missing default nullness annotation for a package without package-info
// test when the package-info is added with the default annotation, the problem disappears
public void testBug372012() throws JavaModelException {

	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, "");

	IPath srcRoot = env.addPackageFragmentRoot(projectPath, "src");
	env.setOutputFolder(projectPath, "bin");
	// prepare the project:
	setupProjectForNullAnnotations(projectPath);
	env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.ERROR);
	env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.ERROR);
	String test1Code = "package p1;\n"	+
		"public class Test1 {\n" +
		"    public void foo() {\n" +
		"        new Test2().bar(\"\");\n" +
		"    }\n" +
		"	 class Test1Inner{}\n" +
		"}";
	String test2Code = "package p1;\n" +
		"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
		"public class Test2 {\n" +
		"    public void bar(String str) {}\n" +
		"}";
	String test3Code = "package p1;\n" +
			"public class Test3 {\n" +
			"    public void bar(String str) {}\n" +
			"}";

	IPath test1Path = env.addClass(srcRoot, "p1", "Test1", test1Code);
	env.addClass(srcRoot, "p1", "Test2", test2Code);
	env.addClass(srcRoot, "p1", "Test3", test3Code);

	fullBuild(projectPath);
	expectingNoProblemsFor(test1Path);
	// should have only one marker
	expectingProblemsFor(srcRoot,
			"Problem : A default nullness annotation has not been specified for the package p1 [ resource : </Project/src/p1> range : <8,10> category : <90> severity : <2>]");

	// add package-info.java with default annotation
	String packageInfoCode = "@org.eclipse.jdt.annotation.NonNullByDefault\n" +
		"package p1;\n";
	env.addClass(srcRoot, "p1", "package-info", packageInfoCode);
	incrementalBuild(projectPath);
	expectingProblemsFor(projectPath,
			"Problem : Nullness default is redundant with a default specified for the enclosing package p1 [ resource : </Project/src/p1/Test2.java> range : <12,56> category : <120> severity : <2>]");

	// verify that all package CU's were recompiled
	expectingUniqueCompiledClasses(new String[] { "p1.Test1", "p1.Test1$Test1Inner", "p1.Test2", "p1.Test3", "p1.package-info" });
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=372012
// test missing default nullness annotation for a package without package-info
// test when the the default annotations are added to all top level types, the problem stays
public void testBug372012a() throws JavaModelException {

	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, "");

	IPath srcRoot = env.addPackageFragmentRoot(projectPath, "src");
	env.setOutputFolder(projectPath, "bin");
	// prepare the project:
	setupProjectForNullAnnotations(projectPath);
	env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.ERROR);
	env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.ERROR);
	String test1Code = "package p1;\n"	+
		"public class Test1 {\n" +
		"    public void foo() {\n" +
		"        new Test2().bar(\"\");\n" +
		"    }\n" +
		"	 class Test1Inner{}\n" +
		"}";
	String test2Code = "package p1;\n" +
		"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
		"public class Test2 {\n" +
		"    public void bar(String str) {}\n" +
		"}";

	IPath test1Path = env.addClass(srcRoot, "p1", "Test1", test1Code);
	env.addClass(srcRoot, "p1", "Test2", test2Code);

	fullBuild(projectPath);
	expectingNoProblemsFor(test1Path);
	// should have only one marker
	expectingProblemsFor(srcRoot,
			"Problem : A default nullness annotation has not been specified for the package p1 [ resource : </Project/src/p1> range : <8,10> category : <90> severity : <2>]");

	// add default annotation to Test1
	test1Code = "package p1;\n"	+
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class Test1 {\n" +
			"    public void foo() {\n" +
			"        new Test2().bar(\"\");\n" +
			"    }\n" +
			"	 class Test1Inner{}\n" +
			"}";
	env.addClass(srcRoot, "p1", "Test1", test1Code);
	incrementalBuild(projectPath);
	// should have only one marker
	expectingProblemsFor(srcRoot,
			"Problem : A default nullness annotation has not been specified for the package p1 [ resource : </Project/src/p1> range : <8,10> category : <90> severity : <2>]");

	// verify that all package CU's were recompiled
	expectingUniqueCompiledClasses(new String[] { "p1.Test1", "p1.Test1$Test1Inner"});
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=372012
// test missing default nullness annotation for a package without package-info
// test when the the default annotations is added to only 1 top level type, the problem stays
public void testBug372012b() throws JavaModelException {

	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, "");

	IPath srcRoot = env.addPackageFragmentRoot(projectPath, "src");
	env.setOutputFolder(projectPath, "bin");
	// prepare the project:
	setupProjectForNullAnnotations(projectPath);
	env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.ERROR);
	env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.ERROR);
	String test1Code = "package p1;\n"	+
		"public class Test1 {\n" +
		"    public void foo() {\n" +
		"        new Test2().bar(\"\");\n" +
		"    }\n" +
		"	 class Test1Inner{}\n" +
		"}";
	String test2Code = "package p1;\n" +
		"public class Test2 {\n" +
		"    public void bar(String str) {}\n" +
		"}";

	IPath test1Path = env.addClass(srcRoot, "p1", "Test1", test1Code);
	env.addClass(srcRoot, "p1", "Test2", test2Code);

	fullBuild(projectPath);
	expectingNoProblemsFor(test1Path);
	// should have only one marker
	expectingProblemsFor(srcRoot,
			"Problem : A default nullness annotation has not been specified for the package p1 [ resource : </Project/src/p1> range : <8,10> category : <90> severity : <2>]");

	// add default annotation to Test1
	test1Code = "package p1;\n"	+
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class Test1 {\n" +
			"    public void foo() {\n" +
			"        new Test2().bar(\"\");\n" +
			"    }\n" +
			"	 class Test1Inner{}\n" +
			"}";
	env.addClass(srcRoot, "p1", "Test1", test1Code);
	incrementalBuild(projectPath);
	// should have only one marker
	expectingProblemsFor(srcRoot,
			"Problem : A default nullness annotation has not been specified for the package p1 [ resource : </Project/src/p1> range : <8,10> category : <90> severity : <2>]");

	// verify that only Test1's CU's were recompiled
	expectingUniqueCompiledClasses(new String[] { "p1.Test1", "p1.Test1$Test1Inner"});
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=372012
// test missing default nullness annotation for a package with package-info
// test when the the default annotation is removed from package-info, the problem comes back
public void testBug372012c() throws JavaModelException {

	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, "");

	IPath srcRoot = env.addPackageFragmentRoot(projectPath, "src");
	env.setOutputFolder(projectPath, "bin");
	// prepare the project:
	setupProjectForNullAnnotations(projectPath);
	env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.ERROR);
	env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.ERROR);
	String test1Code = "package p1;\n"	+
		"public class Test1 {\n" +
		"    public void foo() {\n" +
		"        new Test2().bar(\"\");\n" +
		"    }\n" +
		"	 class Test1Inner{}\n" +
		"}";
	String test2Code = "package p1;\n" +
		"public class Test2 {\n" +
		"    public void bar(String str) {}\n" +
		"}";
	// add package-info.java with default annotation
	String packageInfoCode = "@org.eclipse.jdt.annotation.NonNullByDefault\n" +
		"package p1;\n";
	env.addClass(srcRoot, "p1", "package-info", packageInfoCode);

	env.addClass(srcRoot, "p1", "Test1", test1Code);
	env.addClass(srcRoot, "p1", "Test2", test2Code);
	env.addClass(srcRoot, "p1", "package-info", packageInfoCode);

	fullBuild(projectPath);
	// default annotation present, so no problem
	expectingNoProblemsFor(srcRoot);

	// add package-info.java with default annotation
	packageInfoCode =
		"package p1;\n";
	env.addClass(srcRoot, "p1", "package-info", packageInfoCode);
	incrementalBuild(projectPath);
	expectingProblemsFor(projectPath,
			"Problem : A default nullness annotation has not been specified for the package p1 [ resource : </Project/src/p1/package-info.java> range : <8,10> category : <90> severity : <2>]");

	// verify that all package CU's were recompiled
	expectingUniqueCompiledClasses(new String[] { "p1.Test1", "p1.Test1$Test1Inner", "p1.Test2", "p1.package-info" });
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=367836
 */
public void testBug367836() throws JavaModelException {
	IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	env.addClass(src, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
		"package p;\n"+ //$NON-NLS-1$
		"public class A {}" //$NON-NLS-1$
	);

	IPath bPath = env.addClass(src, "p", "package-info", //$NON-NLS-1$ //$NON-NLS-2$
		"" //$NON-NLS-1$
	);

	fullBuild();
	expectingOnlySpecificProblemFor(bPath,
		new Problem("", "The declared package \"\" does not match the expected package \"p\"", bPath, 0, 0, CategorizedProblem.CAT_INTERNAL, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=374063
// verify that markers are created on the correct resource
public void testBug374063() throws JavaModelException {

	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, "");

	IPath srcRoot = env.addPackageFragmentRoot(projectPath, "src");
	env.setOutputFolder(projectPath, "bin");
	// prepare the project:
	setupProjectForNullAnnotations(projectPath);
	env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.ERROR);
	env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.ERROR);
	String test1Code = "package p1;\n"	+
		"public class Test1 {\n" +
		"    public String output(List<Integer> integers) {\n" +
		"        return \"\";\n" +
		"    }\n" +
		"	 public void output(List<Double> doubles) {}\n" +
		"}";

	env.addClass(srcRoot, "p1", "Test1", test1Code);

	fullBuild(projectPath);
	// resource for compile errors should be Test1 and not p1
	expectingProblemsFor(projectPath,
			"Problem : A default nullness annotation has not been specified for the package p1 [ resource : </Project/src/p1> range : <8,10> category : <90> severity : <2>]\n" +
			"Problem : List cannot be resolved to a type [ resource : </Project/src/p1/Test1.java> range : <130,134> category : <40> severity : <2>]\n" +
			"Problem : List cannot be resolved to a type [ resource : </Project/src/p1/Test1.java> range : <58,62> category : <40> severity : <2>]");

	// add package-info.java with default annotation
	String packageInfoCode = "@org.eclipse.jdt.annotation.NonNullByDefault\n" +
		"package p1;\n";
	env.addClass(srcRoot, "p1", "package-info", packageInfoCode);
	incrementalBuild(projectPath);
	expectingProblemsFor(projectPath,
			"Problem : List cannot be resolved to a type [ resource : </Project/src/p1/Test1.java> range : <130,134> category : <40> severity : <2>]\n" +
			"Problem : List cannot be resolved to a type [ resource : </Project/src/p1/Test1.java> range : <58,62> category : <40> severity : <2>]");

	// verify that all package CU's were recompiled
	expectingUniqueCompiledClasses(new String[] { "p1.Test1", "p1.package-info" });
}
// 382960
public void testBug382960() throws JavaModelException, CoreException {
	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	env.removePackageFragmentRoot(projectPath, "");
	IPath srcRoot = env.addPackageFragmentRoot(projectPath, "src");
	env.setOutputFolder(projectPath, "bin");
	// prepare the project:
	setupProjectForNullAnnotations(projectPath);
	env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.ERROR);
	String test1Code = "package p1;\n" +
						"public class Test1 {\n" +
						"    public String output(List<Integer> integers) {\n" +
						"        return \"\";\n" +
						"    }\n" +
						"	 public void output(List<Double> doubles) {}\n" +
						"}";

	env.addClass(srcRoot, "p1", "Test1", test1Code);
	String packageInfoCode = "package p1;\n";
	env.addClass(srcRoot, "p1", "package-info", packageInfoCode);
	fullBuild(projectPath);
	expectingProblemsFor(projectPath,
			"Problem : A default nullness annotation has not been specified for the package p1 [ resource : </Project/src/p1/package-info.java> range : <8,10> category : <90> severity : <2>]\n" +
			"Problem : List cannot be resolved to a type [ resource : </Project/src/p1/Test1.java> range : <130,134> category : <40> severity : <2>]\n" +
			"Problem : List cannot be resolved to a type [ resource : </Project/src/p1/Test1.java> range : <58,62> category : <40> severity : <2>]");

	packageInfoCode = "@org.eclipse.jdt.annotation.NonNullByDefault\n" +
					   "package p1;\n";
	env.addClass(srcRoot, "p1", "package-info", packageInfoCode);
	incrementalBuild(projectPath);
	expectingProblemsFor(projectPath,
			"Problem : List cannot be resolved to a type [ resource : </Project/src/p1/Test1.java> range : <130,134> category : <40> severity : <2>]\n" +
			"Problem : List cannot be resolved to a type [ resource : </Project/src/p1/Test1.java> range : <58,62> category : <40> severity : <2>]");
	expectingUniqueCompiledClasses(new String[] { "p1.Test1", "p1.package-info" });

	IProject project = env.getProject(projectPath);
	IFile packageInfo = project.getFile("/src/p1/package-info.java");
	packageInfo.touch(null);

	incrementalBuild(projectPath);
	expectingProblemsFor(projectPath,
			"Problem : List cannot be resolved to a type [ resource : </Project/src/p1/Test1.java> range : <130,134> category : <40> severity : <2>]\n" +
			"Problem : List cannot be resolved to a type [ resource : </Project/src/p1/Test1.java> range : <58,62> category : <40> severity : <2>]");

	// verify that only package-info was recompiled
	expectingUniqueCompiledClasses(new String[] { "p1.package-info" });
}
// test that when a package-info.java has been created, markers on the
// package fragments in all source folders are removed.
public void testBug525469() throws JavaModelException {

	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, "");

	IPath srcRoot1 = env.addPackageFragmentRoot(projectPath, "src1");
	IPath srcRoot2 = env.addPackageFragmentRoot(projectPath, "src2");
	env.setOutputFolder(projectPath, "bin");
	// prepare the project:
	setupProjectForNullAnnotations(projectPath);
	env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.ERROR);
	env.getJavaProject(projectPath).setOption(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.ERROR);

	String test1Code = "package p1;\n"	+
		"public class Test1 {\n" +
		"}";
	env.addClass(srcRoot1, "p1", "Test1", test1Code);

	String otherClassCode = "package p2;\n"	+
		"public class OtherClass {\n" +
		"}";
	env.addClass(srcRoot1, "p2", "OtherClass", otherClassCode);

	String packageInfoCode2 = "@org.eclipse.jdt.annotation.NonNullByDefault\n" +
		"package p2;\n";
	env.addClass(srcRoot1, "p2", "package-info", packageInfoCode2);

	fullBuild(projectPath);

	String test2Code = "package p1;\n"	+
		"public class Test2 {\n" +
		"}";

	env.addClass(srcRoot2, "p1", "Test2", test2Code);
	incrementalBuild(projectPath);

	// after the incremental build, as there is no package-info.java for p1, the error is visible in both source directories on the directory for the package p1
	expectingProblemsFor(projectPath,
			"Problem : A default nullness annotation has not been specified for the package p1 [ resource : </Project/src1/p1> range : <8,10> category : <90> severity : <2>]\n" +
			"Problem : A default nullness annotation has not been specified for the package p1 [ resource : </Project/src2/p1> range : <8,10> category : <90> severity : <2>]");

	// add package-info.java with default annotation
	String packageInfoCode1 = "@org.eclipse.jdt.annotation.NonNullByDefault\n" +
		"package p1;\n";
	env.addClass(srcRoot1, "p1", "package-info", packageInfoCode1);

	// an incremental build is requested, but it will switch to a full build
	incrementalBuild(projectPath);

	// verify the expected behaviour: the error marker in the src2 directory must be gone, too
	expectingProblemsFor(projectPath,
			"");

	// verify the implementation by doing a full build: all files have been recompiled
	expectingUniqueCompiledClasses(new String[] { "p1.Test1", "p1.Test2", "p1.package-info", "p2.OtherClass", "p2.package-info" });
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/803
// [BUG] Syntax error, modifiers are not allowed here on a @deprecated javadoc tag in package-info.java
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=569780
public void testIssue803() throws JavaModelException {
	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	env.addClass(src, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
		"package p;\n"+ //$NON-NLS-1$
		"public class A {}" //$NON-NLS-1$
	);

	env.addClass(src, "p", "package-info", //$NON-NLS-1$ //$NON-NLS-2$
		"/**\n"
		+ " * @deprecated\n"
		+ " */\n"
		+ "@java.lang.Deprecated\n"
		+ "package p;" //$NON-NLS-1$
	);

	fullBuild();
	expectingNoProblems();
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/803
// [BUG] Syntax error, modifiers are not allowed here on a @deprecated javadoc tag in package-info.java
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=569780
public void testIssue803_2() throws JavaModelException {
	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	env.addClass(src, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
		"package p;\n"+ //$NON-NLS-1$
		"public class A {}" //$NON-NLS-1$
	);

	env.addClass(src, "p", "package-info", //$NON-NLS-1$ //$NON-NLS-2$
		"/**\n"
		+ " * @deprecated\n"
		+ " */\n"
		+ "package p;" //$NON-NLS-1$
	);

	fullBuild();
	expectingNoProblems();
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/803
// [BUG] Syntax error, modifiers are not allowed here on a @deprecated javadoc tag in package-info.java
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=569780
public void testIssue803_3() throws JavaModelException {
	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	env.addClass(src, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
		"package p;\n"+ //$NON-NLS-1$
		"public class A {}" //$NON-NLS-1$
	);

	env.addClass(src, "p", "package-info", //$NON-NLS-1$ //$NON-NLS-2$
			"@java.lang.Deprecated\n"
			+ "package p;"
	);

	fullBuild();
	expectingNoProblems();
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/803
// [BUG] Syntax error, modifiers are not allowed here on a @deprecated javadoc tag in package-info.java
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=569780
public void testIssue803_4() throws JavaModelException {
	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	env.addClass(src, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
		"package p;\n"+ //$NON-NLS-1$
		"public class A {}" //$NON-NLS-1$
	);

	env.addClass(src, "p", "package-info", //$NON-NLS-1$ //$NON-NLS-2$
		"/**\n"
		+ " * @deprecated\n"
		+ " */\n"
		+ "public package p;" //$NON-NLS-1$
	);

	fullBuild();
	expectingProblemsFor(projectPath,
			"Problem : Syntax error, modifiers are not allowed here [ resource : </Project/src/p/package-info.java> range : <23,29> category : <60> severity : <2>]");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/803
// [BUG] Syntax error, modifiers are not allowed here on a @deprecated javadoc tag in package-info.java
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=569780
public void testIssue803_5() throws JavaModelException {
	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	env.addClass(src, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
		"package p;\n"+ //$NON-NLS-1$
		"public class A {}" //$NON-NLS-1$
	);

	env.addClass(src, "p", "package-info", //$NON-NLS-1$ //$NON-NLS-2$
		"public package p;" //$NON-NLS-1$
	);

	fullBuild();
	expectingProblemsFor(projectPath,
			"Problem : Syntax error, modifiers are not allowed here [ resource : </Project/src/p/package-info.java> range : <0,6> category : <60> severity : <2>]");
}

void setupProjectForNullAnnotations(IPath projectPath) throws JavaModelException {
	// add the org.eclipse.jdt.annotation library (bin/ folder or jar) to the project:
	File bundleFile = FileLocator.getBundleFileLocation(Platform.getBundle("org.eclipse.jdt.annotation")).get();
	String annotationsLib = bundleFile.isDirectory() ? bundleFile.getPath()+"/bin" : bundleFile.getPath();
	IJavaProject javaProject = env.getJavaProject(projectPath);
	IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
	int len = rawClasspath.length;
	System.arraycopy(rawClasspath, 0, rawClasspath = new IClasspathEntry[len+1], 0, len);
	rawClasspath[len] = JavaCore.newLibraryEntry(new Path(annotationsLib), null, null);
	javaProject.setRawClasspath(rawClasspath, null);

	javaProject.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
}

protected void assertSourceEquals(String message, String expected, String actual) {
	if (actual == null) {
		assertEquals(message, expected, null);
		return;
	}
	actual = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(actual);
	if (!actual.equals(expected)) {
		System.out.print(org.eclipse.jdt.core.tests.util.Util.displayString(actual.toString(), 0));
	}
	assertEquals(message, expected, actual);
}
public static Class testClass() {
	return PackageInfoTest.class;
}
}
