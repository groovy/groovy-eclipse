/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;


/**
 * Basic errors tests of the image builder.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ErrorsTests extends BuilderTests {
	private static final IClasspathAttribute ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE = JavaCore.newClasspathAttribute(IClasspathAttribute.IGNORE_OPTIONAL_PROBLEMS, "true");

	private static final Comparator COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			IResource resource1 = (IResource) o1;
			IResource resource2 = (IResource) o2;
			String path1 = resource1.getFullPath().toString();
			String path2 = resource2.getFullPath().toString();
			int length1 = path1.length();
			int length2 = path2.length();

			if (length1 != length2) {
				return length1 - length2;
			}
			return path1.toString().compareTo(path2.toString());
		}
	};

	public ErrorsTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(ErrorsTests.class);
	}

	public void testErrors() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Indicted", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public abstract class Indicted {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		IPath collaboratorPath =  env.addClass(root, "p2", "Collaborator", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Collaborator extends Indicted{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "Indicted", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public abstract class Indicted {\n"+ //$NON-NLS-1$
			"   public abstract void foo();\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);

		expectingOnlyProblemsFor(collaboratorPath);
		expectingOnlySpecificProblemFor(collaboratorPath, new Problem("Collaborator", "The type Collaborator must implement the inherited abstract method Indicted.foo()", collaboratorPath, 38, 50, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * Regression test for bug 2857 Renaming .java class with errors to .txt leaves errors in Task list (1GK06R3)
	 */
	public void testRenameToNonJava() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath cuPath = env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X extends Y {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild(projectPath);
		expectingOnlyProblemsFor(cuPath);
		expectingOnlySpecificProblemFor(cuPath, new Problem("X", "Y cannot be resolved to a type", cuPath, 35, 36, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$


		env.renameCU(root.append("p1"), "X.java", "X.txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		incrementalBuild(projectPath);
		expectingNoProblems();
	}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158611
// Checking the GENERATED_BY attribute
public void test0100() throws JavaModelException {
	IPath projectPath = env.addProject("Project");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(projectPath, "");
	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	IPath classTest1 = env.addClass(root, "p1", "Test1",
		"package p1;\n" +
		"public class Test1 extends Test2 {}"
	);
	fullBuild();
	Problem[] prob1 = env.getProblemsFor(classTest1);
	expectingSpecificProblemFor(classTest1, new Problem("p1", "Test2 cannot be resolved to a type", classTest1, 39, 44, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
	assertEquals(JavaBuilder.SOURCE_ID, prob1[0].getSourceId());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158611
// Checking the GENERATED_BY attribute
public void test0101() throws JavaModelException {
	IPath projectPath = env.addProject("Project");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(projectPath, "");
	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	IPath classTest1 = env.addClass(root, "p1", "Test1",
		"package p1;\n" +
		"public class Test1 extends {}"
	);
	fullBuild();
	Problem[] prob1 = env.getProblemsFor(classTest1);
	expectingSpecificProblemFor(classTest1, new Problem("p1", "Syntax error on token \"extends\", Type expected after this token", classTest1, 31, 38, CategorizedProblem.CAT_SYNTAX, IMarker.SEVERITY_ERROR));
	assertEquals(JavaBuilder.SOURCE_ID, prob1[0].getSourceId());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158611
// Checking the GENERATED_BY attribute
public void test0102() throws JavaModelException {
	IPath projectPath = env.addProject("Project");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(projectPath, "");
	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	IPath classTest1 = env.addClass(root, "p1", "Test1",
		"package p1;\n" +
		"public class Test1 {\n" +
		"  private static int i;\n" +
		"  int j = i;\n" +
		"}\n" +
		"class Test2 {\n" +
		"  static int i = Test1.i;\n" +
		"}\n"
	);
	fullBuild();
	Problem[] prob1 = env.getProblemsFor(classTest1);
	expectingSpecificProblemFor(classTest1, new Problem("p1", "The field Test1.i is not visible", classTest1, 109, 110, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));
	assertEquals(JavaBuilder.SOURCE_ID, prob1[0].getSourceId());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158611
// Checking the GENERATED_BY attribute
public void test0103() throws JavaModelException {
	IPath projectPath = env.addProject("Project");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(projectPath, "");
	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	IPath classTest1 = env.addClass(root, "p1", "Test1",
		"package p1;\n" +
		"public class Test1 {\n" +
		"  // TODO: marker only\n" +
		"}\n"
	);
	fullBuild();
	Problem[] prob1 = env.getProblemsFor(classTest1);
	expectingSpecificProblemFor(classTest1, new Problem("p1", "TODO: marker only", classTest1, 38, 55, -1, IMarker.SEVERITY_ERROR));
	assertEquals(JavaBuilder.SOURCE_ID, prob1[0].getSourceId());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158611
// Checking the GENERATED_BY attribute
public void test0104() throws JavaModelException {
	IPath projectPath = env.addProject("Project");
	env.removePackageFragmentRoot(projectPath, "");
	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	IPath classTest1 = env.addClass(root, "p1", "Test1",
		"package p1;\n" +
		"public class Test1 {}"
	);
	fullBuild();
	Problem[] prob1 = env.getProblemsFor(classTest1);
	expectingSpecificProblemFor(classTest1,
		new Problem("p1", "The type java.lang.Object cannot be resolved. It is indirectly referenced from required .class files", classTest1, 0, 1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR));
	assertEquals(JavaBuilder.SOURCE_ID, prob1[0].getSourceId());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97998
// Improving the error message in case of a read-only file in output
// directory. Beware: this test only works under Linux - execution on other
// platforms always succeeds, but the result is not significant.
public void _test0105() throws JavaModelException, CoreException, IOException { // FIXME: re-enable!
	if ("Linux".equals(System.getProperty("os.name"))) {
		IPath projectPath = env.addProject("P");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		IPath root = env.getPackageFragmentRootPath(projectPath, "");
		IPath outputFolderPath = env.getOutputLocation(projectPath);
		File outputFolder = env.getWorkspaceRootPath().append(outputFolderPath).toFile();
		env.addClass(root, "p1",
				"X",
				"package p1;\n" +
				"public class X {\n" +
				"}\n"
			);
		try {
			fullBuild(projectPath);
			expectingNoProblems();
			outputFolder.setReadOnly();
			// outputFolder.setReadable(false);
			// PREMATURE no appropriate solution for Windows/NTFS/JDK 1.4
			System.err.println("\n\n=== EXPECTED EXCEPTION =========================================================");
			System.err.println("ErrorsTests#test0105 will emit an expected exception below");
			cleanBuild();
			System.err.println("=== END OF EXPECTED EXCEPTION ==================================================\n\n");
			expectingOnlySpecificProblemFor(env.getWorkspaceRootPath(),
					new Problem("",
						"The project was not built due to \"Could not delete \'" +
						env.getWorkspaceRootPath() + "/P/bin/.classpath\'.\". " +
						"Fix the problem, then try refreshing this project and building " +
						"it since it may be inconsistent", projectPath, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR));
		} finally {
			// waiting for JDK 6: outputFolder.setWritable(true); -- workaround:
			Process process = null;
			try {
				process = Runtime.getRuntime().exec("chmod -R a+w " + outputFolder.getAbsolutePath());
				process.waitFor();
			} catch (InterruptedException e) {
				// go ahead
			} finally {
				if (process != null) {
					process.destroy();
				}
			}
		}
		try {
			cleanBuild();
			expectingNoProblems();
		} catch (Throwable t) {
			Process process = null;
			try {
				process = Runtime.getRuntime().exec("chmod -R a+w " + outputFolder.getAbsolutePath());
				process.waitFor();
			} catch (InterruptedException ie) {
				// go ahead
			} finally {
				if (process != null) {
					process.destroy();
				}
			}
			fail(t.getMessage());
		}
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=224715
public void test0106() throws JavaModelException {
	IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

	IPath root = env.addPackageFragmentRoot(projectPath, "src", null, null); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	IPath classTest1 = env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
		"package p1;\n"+ //$NON-NLS-1$
		"public class X implements I {\n" +
		"}\n" //$NON-NLS-1$
	);

	env.addClass(root, "p1", "I", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public interface I {\n" +
			"   public void foo() {\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

	incrementalBuild(projectPath);

	expectingSpecificProblemFor(classTest1, new Problem("p1", "The type X must implement the inherited abstract method I.foo()", classTest1, 25, 26, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));

	IJavaProject project = env.getJavaProject(projectPath);
	IRegion region = JavaCore.newRegion();
	region.add(project);
	IResource[] resources = JavaCore.getGeneratedResources(region, false);
	assertEquals("Wrong size", 2, resources.length);//$NON-NLS-1$
	Arrays.sort(resources, COMPARATOR);
	String actualOutput = getResourceOuput(resources);
	String expectedOutput =
		"/Project/bin/p1/I.class\n" +
		"/Project/bin/p1/X.class\n";
	assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);

	assertEquals("Wrong type", IResource.FILE, resources[1].getType());
	IFile classFile = (IFile) resources[1];
	IClassFileReader classFileReader = null;
	InputStream stream = null;
	try {
		stream = classFile.getContents();
		classFileReader = ToolFactory.createDefaultClassFileReader(stream, IClassFileReader.ALL);
	} catch (CoreException e) {
		e.printStackTrace();
	} finally {
		if (stream != null) {
			try {
				stream.close();
			} catch(IOException e) {
				// ignore
			}
		}
	}
	assertNotNull("No class file reader", classFileReader);
	IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
	IMethodInfo found = null;
	loop: for (int i = 0, max = methodInfos.length; i < max; i++) {
		IMethodInfo methodInfo = methodInfos[i];
		if (CharOperation.equals(methodInfo.getName(), "foo".toCharArray())) {
			found = methodInfo;
			break loop;
		}
	}
	assertNotNull("No method found", found);
	assertTrue("Not a synthetic method", found.isSynthetic());
	env.removeProject(projectPath);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=225563
public void test0107() throws JavaModelException {
	IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	fullBuild(projectPath);

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

	IPath root = env.addPackageFragmentRoot(projectPath, "src", null, null); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	IPath classTest1 = env.addClass(root, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
		"public class C implements None {\n" +
		"        public String toString(Arg a) {\n" +
		"                return null;\n" +
		"        }\n" +
		"        public String toString(Arg[] a) {\n" +
		"                return null;\n" +
		"        }\n" +
		"}" //$NON-NLS-1$
	);

	incrementalBuild(projectPath);

	expectingOnlySpecificProblemsFor(classTest1, new Problem[] {
		new Problem("", "None cannot be resolved to a type", classTest1, 26, 30, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR),
		new Problem("", "Arg cannot be resolved to a type", classTest1, 64, 67, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR),
		new Problem("", "Arg cannot be resolved to a type", classTest1, 143, 146, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)
	});

	IJavaProject project = env.getJavaProject(projectPath);
	IRegion region = JavaCore.newRegion();
	region.add(project);
	IResource[] resources = JavaCore.getGeneratedResources(region, false);
	assertEquals("Wrong size", 1, resources.length);//$NON-NLS-1$
	Arrays.sort(resources, COMPARATOR);
	String actualOutput = getResourceOuput(resources);
	String expectedOutput =
		"/Project/bin/C.class\n";
	assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);

	assertEquals("Wrong type", IResource.FILE, resources[0].getType());
	IFile classFile = (IFile) resources[0];
	InputStream stream = null;
	try {
		stream = classFile.getContents();
		ClassFileReader.read(stream, "C.java");
	} catch (Exception e) {
		e.printStackTrace();
		assertTrue("Should not happen", false);
	} finally {
		if (stream != null) {
			try {
				stream.close();
			} catch(IOException e) {
				// ignore
			}
		}
	}
}
private String getResourceOuput(IResource[] resources) {
	StringWriter stringWriter = new StringWriter();
	PrintWriter writer = new PrintWriter(stringWriter);
	for (int i = 0, max = resources.length; i < max; i++) {
		writer.println(resources[i].getFullPath().toString());
	}
	writer.flush();
	writer.close();
	return Util.convertToIndependantLineDelimiter(String.valueOf(stringWriter));
}

// ignore optional errors
public void test0108() throws JavaModelException {
	Hashtable options = JavaCore.getOptions();
	Hashtable newOptions = JavaCore.getOptions();
	newOptions.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);

	JavaCore.setOptions(newOptions);

	IPath projectPath = env.addProject("P");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, "");

	env.setOutputFolder(projectPath, "bin");
	IPath root = new Path("/P/src");
	env.addEntry(projectPath, JavaCore.newSourceEntry(root, null, null,
			null, new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE }));

	env.addClass(root, "p", "X",
			"package p;\n" +
			"public class X {\n" +
			"	public void foo() {\n" +
			"		int i;\n" +
			"	}\n" +
			"}");

	fullBuild(projectPath);
	expectingNoProblems();

	JavaCore.setOptions(options);
}

// two different source folders ignore only from one
public void test0109() throws JavaModelException {
	Hashtable options = JavaCore.getOptions();
	Hashtable newOptions = JavaCore.getOptions();
	newOptions.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);

	JavaCore.setOptions(newOptions);

	IPath projectPath = env.addProject("P");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, "");

	env.setOutputFolder(projectPath, "bin");
	IPath src = new Path("/P/src");
	IPath src2 = new Path("/P/src2");
	env.addEntry(projectPath, JavaCore.newSourceEntry(src, null, null,
			null, new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE }));
	env.addEntry(projectPath, JavaCore.newSourceEntry(src2));

	env.addClass(src, "p", "X",
			"package p;\n" +
			"public class X {\n" +
			"	public void foo() {\n" +
			"		int i;\n" +
			"	}\n" +
			"}");

	IPath classY = env.addClass(src2, "q", "Y",
			"package q;\n" +
			"public class Y {\n" +
			"	public void foo() {\n" +
			"		int i;\n" +
			"	}\n" +
			"}");

	fullBuild(projectPath);
	expectingNoProblemsFor(src);
	expectingOnlySpecificProblemFor(classY, new Problem("q", "The value of the local variable i is not used", classY, 55, 56, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_ERROR));

	JavaCore.setOptions(options);
}

// two different source folders ignore from both
public void test0110() throws JavaModelException {
	Hashtable options = JavaCore.getOptions();
	Hashtable newOptions = JavaCore.getOptions();
	newOptions.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);

	JavaCore.setOptions(newOptions);

	IPath projectPath = env.addProject("P");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, "");

	env.setOutputFolder(projectPath, "bin");
	IPath src = new Path("/P/src");
	IPath src2 = new Path("/P/src2");
	env.addEntry(projectPath, JavaCore.newSourceEntry(src, null, null,
			null, new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE }));
	env.addEntry(projectPath, JavaCore.newSourceEntry(src2, null, null,
			null, new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE }));

	env.addClass(src, "p", "X",
			"package p;\n" +
			"public class X {\n" +
			"	public void foo() {\n" +
			"		int i;\n" +
			"	}\n" +
			"}");

	env.addClass(src2, "q", "Y",
			"package q;\n" +
			"public class Y {\n" +
			"	public void foo() {\n" +
			"		int i;\n" +
			"	}\n" +
			"}");

	fullBuild(projectPath);
	expectingNoProblems();

	JavaCore.setOptions(options);
}

//non-optional errors cannot be ignored
public void test0111() throws JavaModelException {
	Hashtable options = JavaCore.getOptions();
	Hashtable newOptions = JavaCore.getOptions();
	newOptions.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);

	JavaCore.setOptions(newOptions);

	IPath projectPath = env.addProject("P");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, "");

	env.setOutputFolder(projectPath, "bin");
	IPath root = new Path("/P/src");
	env.addEntry(projectPath, JavaCore.newSourceEntry(root, null, null,
			null, new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE }));

	IPath classX = env.addClass(root, "p", "X",
			"package p;\n" +
			"public class X {\n" +
			"	public void foo() {\n" +
			"		int i;\n" +
			"	}\n" +
			"	public void bar() {\n" +
			"		a++;\n" +
			"	}\n" +
			"}");

	fullBuild(projectPath);
	expectingOnlySpecificProblemFor(classX, new Problem("p", "a cannot be resolved to a variable", classX, 84, 85, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));

	JavaCore.setOptions(options);
}

//task tags cannot be ignored
public void test0112() throws JavaModelException {
	Hashtable options = JavaCore.getOptions();
	Hashtable newOptions = JavaCore.getOptions();
	newOptions.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);
	newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO");
	newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL");

	JavaCore.setOptions(newOptions);

	IPath projectPath = env.addProject("P");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());

	// remove old package fragment root so that names don't collide
	env.removePackageFragmentRoot(projectPath, "");

	env.setOutputFolder(projectPath, "bin");
	IPath root = new Path("/P/src");
	env.addEntry(projectPath, JavaCore.newSourceEntry(root, null, null,
			null, new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE }));

	IPath classX = env.addClass(root, "p", "X",
			"package p;\n" +
			"public class X {\n" +
			"	public void foo() {\n" +
			"		int i;\n" +
			"	}\n" +
			"	public void bar() {\n" +
			"		// TODO nothing\n" +
			"	}\n" +
			"}");

	fullBuild(projectPath);
	expectingOnlySpecificProblemFor(classX, new Problem("p", "TODO nothing", classX, 87, 99, -1, IMarker.SEVERITY_ERROR));

	JavaCore.setOptions(options);
}
}
