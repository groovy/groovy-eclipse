/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;

import junit.framework.Test;

@SuppressWarnings({"unchecked", "rawtypes"})
public class BuildpathTests extends BuilderTests {

public BuildpathTests(String name) {
	super(name);
}

public static Test suite() {
	return buildTestSuite(BuildpathTests.class);
}
@Override
protected void setUp() throws Exception {
	this.indexDisabledForTest = false;
	super.setUp();
}

private String getJdkLevelProblem(String expectedRuntime, String path, int severity) {
	Object target = JavaModel.getTarget(new Path(path).makeAbsolute(), true);
	long libraryJDK = org.eclipse.jdt.internal.core.util.Util.getJdkLevel(target);
	String jclRuntime = CompilerOptions.versionFromJdkLevel(libraryJDK);
	StringBuilder jdkLevelProblem = new StringBuilder("Problem : Incompatible .class files version in required binaries. Project 'Project' is targeting a ");
	jdkLevelProblem.append(expectedRuntime);
	jdkLevelProblem.append(" runtime, but is compiled against '");
	jdkLevelProblem.append(path);
	jdkLevelProblem.append("' which requires a ");
	jdkLevelProblem.append(jclRuntime);
	jdkLevelProblem.append(" runtime [ resource : </Project> range : <-1,-1> category : <10> severity : <");
	jdkLevelProblem.append(severity);
	jdkLevelProblem.append(">]");
	return jdkLevelProblem.toString();
}

public void testClasspathFileChange() throws JavaModelException {
	// create project with src folder, and alternate unused src2 folder
	IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	IPath classTest1 = env.addClass(root, "p1", "Test1", //$NON-NLS-1$ //$NON-NLS-2$
		"package p1;\n"+ //$NON-NLS-1$
		"public class Test1 extends Zork1 {}" //$NON-NLS-1$
	);
	// not yet on the classpath
	IPath src2Path = env.addFolder(projectPath, "src2"); //$NON-NLS-1$
	IPath src2p1Path = env.addFolder(src2Path, "p1"); //$NON-NLS-1$
	env.addFile(src2p1Path, "Zork1.java", //$NON-NLS-1$
		"package p1;\n"+ //$NON-NLS-1$
		"public class Zork1 {}" //$NON-NLS-1$
	);

	fullBuild();
	expectingSpecificProblemFor(classTest1, new Problem("src", "Zork1 cannot be resolved to a type", classTest1,39, 44, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

	//----------------------------
	//           Step 2
	//----------------------------
	StringBuilder buffer = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
	buffer.append("<classpath>\n"); //$NON-NLS-1$
	buffer.append("    <classpathentry kind=\"src\" path=\"src\"/>\n"); //$NON-NLS-1$
	buffer.append("    <classpathentry kind=\"src\" path=\"src2\"/>\n"); // add src2 on classpath through resource change //$NON-NLS-1$
	String[] classlibs = Util.getJavaClassLibs();
	for (int i = 0; i < classlibs.length; i++) {
		buffer.append("    <classpathentry kind=\"lib\" path=\"").append(classlibs[i]).append("\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	buffer.append("    <classpathentry kind=\"output\" path=\"bin\"/>\n"); //$NON-NLS-1$
	buffer.append("</classpath>"); //$NON-NLS-1$
	boolean wasAutoBuilding = env.isAutoBuilding();
	try {
		// turn autobuild on
		env.setAutoBuilding(true);
		// write new .classpath, will trigger autobuild
		env.addFile(projectPath, ".classpath", buffer.toString()); //$NON-NLS-1$
		// ensures the builder did see the classpath change
		env.waitForAutoBuild();
		expectingNoProblems();
	} finally {
		env.setAutoBuilding(wasAutoBuilding);
	}
}

public void testClosedProject() throws JavaModelException, IOException {
	IPath project1Path = env.addProject("CP1"); //$NON-NLS-1$
	env.addExternalJars(project1Path, Util.getJavaClassLibs());
	IPath jarPath = addEmptyInternalJar(project1Path, "temp.jar");

	IPath project2Path = env.addProject("CP2"); //$NON-NLS-1$
	env.addExternalJars(project2Path, Util.getJavaClassLibs());
	env.addRequiredProject(project2Path, project1Path);

	IPath project3Path = env.addProject("CP3"); //$NON-NLS-1$
	env.addExternalJars(project3Path, Util.getJavaClassLibs());
	env.addExternalJar(project3Path, jarPath.toString());

	fullBuild();
	expectingNoProblems();

	//----------------------------
	//           Step 2
	//----------------------------
	env.closeProject(project1Path);

	incrementalBuild();
	expectingOnlyProblemsFor(new IPath[] {project2Path, project3Path});
	expectingOnlySpecificProblemsFor(project2Path,
		new Problem[] {
			new Problem("", "The project cannot be built until build path errors are resolved", project2Path, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR), //$NON-NLS-1$ //$NON-NLS-2$
			new Problem("Build path", "Project 'CP2' is missing required Java project: 'CP1'", project2Path, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR) //$NON-NLS-1$ //$NON-NLS-2$
		}
	);
	expectingOnlySpecificProblemsFor(project3Path,
		new Problem[] {
			new Problem("", "The project cannot be built until build path errors are resolved", project3Path, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR), //$NON-NLS-1$ //$NON-NLS-2$
			new Problem("Build path", "Project 'CP3' is missing required library: '/CP1/temp.jar'", project3Path, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR) //$NON-NLS-1$ //$NON-NLS-2$
		}
	);

	env.openProject(project1Path);
	incrementalBuild();
	expectingNoProblems();

	//----------------------------
	//           Step 3
	//----------------------------
	Hashtable<String, String> options = JavaCore.getOptions();
	options.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.IGNORE);
	JavaCore.setOptions(options);
	env.closeProject(project1Path);
	env.waitForManualRefresh();
	incrementalBuild();
	env.waitForAutoBuild();
	expectingOnlyProblemsFor(new IPath[] {project2Path, project3Path});
	expectingOnlySpecificProblemFor(project2Path,
		new Problem("Build path", "Project 'CP2' is missing required Java project: 'CP1'", project2Path, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR) //$NON-NLS-1$ //$NON-NLS-2$
	);
	expectingOnlySpecificProblemFor(project3Path,
		new Problem("Build path", "Project 'CP3' is missing required library: '/CP1/temp.jar'", project3Path, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR) //$NON-NLS-1$ //$NON-NLS-2$
	);

	env.openProject(project1Path);
	incrementalBuild();
	expectingNoProblems();
	env.removeProject(project1Path);
}

public void testCorruptBuilder() throws JavaModelException {
	IPath project1Path = env.addProject("P1"); //$NON-NLS-1$
	env.addExternalJars(project1Path, Util.getJavaClassLibs());

	env.addClass(project1Path, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
		"package p;" + //$NON-NLS-1$
		"public class Test {}" //$NON-NLS-1$
	);

	fullBuild();
	expectingNoProblems();

	IPath outputFolderPackage = env.getOutputLocation(project1Path).append("p"); //$NON-NLS-1$
	env.removeBinaryClass(outputFolderPackage, "Test"); //$NON-NLS-1$

	IPath subTest = env.addClass(project1Path, "", "SubTest", //$NON-NLS-1$ //$NON-NLS-2$
		"public class SubTest extends p.Test {}" //$NON-NLS-1$
	);

	incrementalBuild();
	expectingOnlySpecificProblemFor(subTest, new Problem("", "p.Test cannot be resolved to a type", subTest, 29, 35, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$)

	env.addClass(project1Path, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
		"package p;" + //$NON-NLS-1$
		"public class Test {}" //$NON-NLS-1$
	);

	fullBuild();
	expectingNoProblems();

	Hashtable<String, String> options = JavaCore.getOptions();
	options.put(JavaCore.CORE_JAVA_BUILD_RECREATE_MODIFIED_CLASS_FILES_IN_OUTPUT_FOLDER, JavaCore.ENABLED);
	JavaCore.setOptions(options);

	env.removeBinaryClass(outputFolderPackage, "Test"); //$NON-NLS-1$
	env.waitForManualRefresh();
	incrementalBuild();
	env.waitForAutoBuild();
	expectingNoProblems();
	env.removeProject(project1Path);
}

public void testCorruptBuilder2() throws JavaModelException {
	IPath project1Path = env.addProject("P2"); //$NON-NLS-1$
	env.addExternalJars(project1Path, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(project1Path, ""); //$NON-NLS-1$
	IPath src = env.addPackageFragmentRoot(project1Path, "src"); //$NON-NLS-1$
	IPath bin = env.setOutputFolder(project1Path, "bin"); //$NON-NLS-1$

	env.addClass(src, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
		"package p;" + //$NON-NLS-1$
		"public class Test {}" //$NON-NLS-1$
	);

	fullBuild();
	env.waitForAutoBuild();
	expectingNoProblems();

	IPath outputFolderPackage = bin.append("p"); //$NON-NLS-1$
	env.removeBinaryClass(outputFolderPackage, "Test"); //$NON-NLS-1$

	IPath subTest = env.addClass(src, "p2", "SubTest", //$NON-NLS-1$ //$NON-NLS-2$
		"package p2;" + //$NON-NLS-1$
		"public class SubTest extends p.Test {}" //$NON-NLS-1$
	);

	incrementalBuild();
	env.waitForAutoBuild();
	expectingOnlySpecificProblemFor(subTest, new Problem("", "p.Test cannot be resolved to a type", subTest, 40, 46, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$)

	env.addClass(src, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
		"package p;" + //$NON-NLS-1$
		"public class Test {}" //$NON-NLS-1$
	);

	fullBuild();
	env.waitForAutoBuild();
	expectingNoProblems();

	Hashtable<String, String> options = JavaCore.getOptions();
	options.put(JavaCore.CORE_JAVA_BUILD_RECREATE_MODIFIED_CLASS_FILES_IN_OUTPUT_FOLDER, JavaCore.ENABLED);
	JavaCore.setOptions(options);

	env.removeBinaryClass(outputFolderPackage, "Test"); //$NON-NLS-1$
	env.waitForManualRefresh();
	incrementalBuild();
	expectingNoProblems();
	env.removeProject(project1Path);
}

/*
 * Ensures that changing a type in an external folder and refreshing triggers a rebuild
 */
public void testChangeExternalFolder() throws CoreException {
	String externalLib = Util.getOutputDirectory() + File.separator + "externalLib";
	IPath projectPath = env.addProject("Project");
	try {
		new File(externalLib).mkdirs();
		Util.compile(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"  public void foo() {\n" +
				"  }\n" +
				"}"
			},
			new HashMap<>(),
			externalLib
		);

		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalFolders(projectPath, new String[] {externalLib});

		IPath root = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "");

		IPath classY = env.addClass(root, "q", "Y",
			"package q;\n"+
			"public class Y {\n" +
			"  void bar(p.X x) {\n" +
			"    x.foo();\n" +
			"  }\n" +
			"}"
		);

		fullBuild(projectPath);
		env.waitForAutoBuild();
		expectingNoProblems();

		String externalClassFile = externalLib + File.separator + "p" + File.separator + "X.class";
		long lastModified = new java.io.File(externalClassFile).lastModified();
		try {
			Thread.sleep(1000);
		} catch(InterruptedException e) {
		}
		Util.compile(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}"
			},
			new HashMap<>(),
			externalLib
		);
		new java.io.File(externalClassFile).setLastModified(lastModified + 1000); // to be sure its different

		env.getProject(projectPath).refreshLocal(IResource.DEPTH_INFINITE, null);
		env.waitForManualRefresh();

		incrementalBuild(projectPath);
		env.waitForAutoBuild();
		expectingProblemsFor(
			classY,
			"Problem : The method foo() is undefined for the type X [ resource : </Project/q/Y.java> range : <54,57> category : <50> severity : <2>]"
		);
	} finally {
		new File(externalLib).delete();
		env.removeProject(projectPath);
	}
}

/*
 * Ensures that changing a type in an external ZIP archive and refreshing triggers a rebuild
 */
public void testChangeZIPArchive1() throws Exception {
	String externalLib = Util.getOutputDirectory() + File.separator + "externalLib.abc";
	IPath projectPath = env.addProject("Project");
	try {
		org.eclipse.jdt.core.tests.util.Util.createJar(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"  public void foo() {\n" +
				"  }\n" +
				"}"
			},
			externalLib,
			"1.4");

		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJars(projectPath, new String[] {externalLib});

		IPath root = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "");

		IPath classY = env.addClass(root, "q", "Y",
			"package q;\n"+
			"public class Y {\n" +
			"  void bar(p.X x) {\n" +
			"    x.foo();\n" +
			"  }\n" +
			"}"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		org.eclipse.jdt.core.tests.util.Util.createJar(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}"
			},
			externalLib,
			"1.4");

		IJavaProject p = env.getJavaProject(projectPath);
		p.getJavaModel().refreshExternalArchives(new IJavaElement[] {p}, null);

		incrementalBuild(projectPath);
		expectingProblemsFor(
			classY,
			"Problem : The method foo() is undefined for the type X [ resource : </Project/q/Y.java> range : <54,57> category : <50> severity : <2>]"
		);
	} finally {
		new File(externalLib).delete();
		env.removeProject(projectPath);
	}
}

/*
 * Ensures that changing a type in an internal ZIP archive and refreshing triggers a rebuild
 */
public void testChangeZIPArchive2() throws Exception {
	IPath projectPath = env.addProject("Project");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	String internalLib = env.getProject("Project").getLocation().toOSString() + File.separator + "internalLib.abc";
	org.eclipse.jdt.core.tests.util.Util.createJar(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}"
		},
		internalLib,
		"1.4");
	env.getProject(projectPath).refreshLocal(IResource.DEPTH_INFINITE, null);
	env.addEntry(projectPath, JavaCore.newLibraryEntry(new Path("/Project/internalLib.abc"), null, null));

	IPath root = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
	env.setOutputFolder(projectPath, "");

	IPath classY = env.addClass(root, "q", "Y",
		"package q;\n"+
		"public class Y {\n" +
		"  void bar(p.X x) {\n" +
		"    x.foo();\n" +
		"  }\n" +
		"}"
	);

	fullBuild(projectPath);
	expectingNoProblems();

	if (Util.isMacOS()) {
		// Wait a moment so the jar timestamp will be different
		Thread.sleep(2000);
	}

	org.eclipse.jdt.core.tests.util.Util.createJar(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		},
		internalLib,
		"1.4");

	env.getProject(projectPath).refreshLocal(IResource.DEPTH_INFINITE, null);

	incrementalBuild(projectPath);
	expectingProblemsFor(
		classY,
		"Problem : The method foo() is undefined for the type X [ resource : </Project/q/Y.java> range : <54,57> category : <50> severity : <2>]"
	);
	env.removeProject(projectPath);
}

/*
 * Ensures that changing an external jar and refreshing the projects triggers a rebuild
 * (regression test for bug 50207 Compile errors fixed by 'refresh' do not reset problem list or package explorer error states)
 */
public void testExternalJarChange() throws JavaModelException, IOException {
	// setup
	IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	IPath root = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
	IPath classTest = env.addClass(root, "p", "X", //$NON-NLS-1$ //$NON-NLS-2$
		"package p;\n"+ //$NON-NLS-1$
		"public class X {\n" + //$NON-NLS-1$
		"  void foo() {\n" + //$NON-NLS-1$
		"    new q.Y().bar();\n" + //$NON-NLS-1$
		"  }\n" + //$NON-NLS-1$
		"}" //$NON-NLS-1$
	);
	String externalJar = Util.getOutputDirectory() + File.separator + "test.jar"; //$NON-NLS-1$
	Util.createJar(
		new String[] {
			"q/Y.java", //$NON-NLS-1$
			"package q;\n" + //$NON-NLS-1$
			"public class Y {\n" + //$NON-NLS-1$
			"}" //$NON-NLS-1$
		},
		new HashMap<>(),
		externalJar
	);
	env.addExternalJar(projectPath, externalJar);

	// build -> expecting problems
	fullBuild();
	expectingProblemsFor(
		classTest,
		"Problem : The method bar() is undefined for the type Y [ resource : </Project/p/X.java> range : <57,60> category : <50> severity : <2>]"
	);

	// fix jar
	Util.createJar(
		new String[] {
			"q/Y.java", //$NON-NLS-1$
			"package q;\n" + //$NON-NLS-1$
			"public class Y {\n" + //$NON-NLS-1$
			"  public void bar() {\n" + //$NON-NLS-1$
			"  }\n" + //$NON-NLS-1$
			"}" //$NON-NLS-1$
		},
		new HashMap<>(),
		externalJar
	);

	// refresh project and rebuild -> expecting no problems
	IJavaProject project = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject("Project")); //$NON-NLS-1$
	project.getJavaModel().refreshExternalArchives(new IJavaElement[] {project}, null);
	incrementalBuild();
	expectingNoProblems();
	env.removeProject(projectPath);
}

public void testMissingBuilder() throws Exception {
	IPath project1Path = env.addProject("P1"); //$NON-NLS-1$
	env.addExternalJars(project1Path, Util.getJavaClassLibs());

	IPath project2Path = env.addProject("P2"); //$NON-NLS-1$
	env.addExternalJars(project2Path, Util.getJavaClassLibs());
	env.addRequiredProject(project2Path, project1Path);

	env.addClass(project1Path, "", "Test", //$NON-NLS-1$ //$NON-NLS-2$
		"public class Test {}" //$NON-NLS-1$
	);

	IPath sub = env.addClass(project2Path, "", "SubTest", //$NON-NLS-1$ //$NON-NLS-2$
		"public class SubTest extends Test {}" //$NON-NLS-1$
	);

	fullBuild();
	expectingNoProblems();

	env.removeRequiredProject(project2Path, project1Path);

	incrementalBuild();
	expectingOnlySpecificProblemFor(sub, new Problem("", "Test cannot be resolved to a type", sub, 29, 33, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$)

	env.addRequiredProject(project2Path, project1Path);

	JavaProject p = (JavaProject) env.getJavaProject(project1Path);
	p.getProject().getNature(JavaCore.NATURE_ID).deconfigure();
	JavaModelManager.getJavaModelManager().setLastBuiltState(p.getProject(), null);

	env.addClass(project2Path, "", "SubTest", //$NON-NLS-1$ //$NON-NLS-2$
		"public class SubTest extends Test {}" //$NON-NLS-1$
	);

	incrementalBuild();
	expectingNoProblems();
	env.removeProject(project1Path);
	env.removeProject(project2Path);
}

public void testMissingFieldType() throws JavaModelException {
	IPath projectPath = env.addProject("Project1"); //$NON-NLS-1$
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	IPath root = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
	env.addClass(root, "p1", "Test", //$NON-NLS-1$ //$NON-NLS-2$
		"package p1;\n"+ //$NON-NLS-1$
		"public class Test {}" //$NON-NLS-1$
	);

	fullBuild();
	expectingNoProblems();

	IPath projectPath2 = env.addProject("Project2"); //$NON-NLS-1$
	env.addExternalJars(projectPath2, Util.getJavaClassLibs());
	env.addRequiredProject(projectPath2, projectPath);
	IPath root2 = env.getPackageFragmentRootPath(projectPath2, ""); //$NON-NLS-1$
	env.addClass(root2, "p2", "Test2", //$NON-NLS-1$ //$NON-NLS-2$
		"package p2;\n"+ //$NON-NLS-1$
		"public class Test2 {\n" + //$NON-NLS-1$
		"	public static p1.Test field;\n" + //$NON-NLS-1$
		"}" //$NON-NLS-1$
	);

	incrementalBuild();
	expectingNoProblems();

	IPath projectPath3 = env.addProject("Project3"); //$NON-NLS-1$
	env.addExternalJars(projectPath3, Util.getJavaClassLibs());
	env.addRequiredProject(projectPath3, projectPath2);
	IPath root3 = env.getPackageFragmentRootPath(projectPath3, ""); //$NON-NLS-1$
	env.addClass(root3, "p3", "Test3", //$NON-NLS-1$ //$NON-NLS-2$
		"package p3;\n"+ //$NON-NLS-1$
		"public class Test3 extends p2.Test2 {\n" + //$NON-NLS-1$
		"	static Object field;\n" + //$NON-NLS-1$
		"}" //$NON-NLS-1$
	);

	incrementalBuild();
	expectingNoProblems();
	env.removeProject(projectPath);
}

public void testMissingLibrary1() throws JavaModelException {
	IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	IPath classTest1 = env.addClass(root, "p1", "Test1", //$NON-NLS-1$ //$NON-NLS-2$
		"package p1;\n"+ //$NON-NLS-1$
		"public class Test1 {}" //$NON-NLS-1$
	);

	fullBuild();
	expectingOnlyProblemsFor(new IPath[] {projectPath, classTest1});
	expectingOnlySpecificProblemsFor(projectPath,
		new Problem[] {
			new Problem("", "The project was not built since its build path is incomplete. Cannot find the class file for java.lang.Object. Fix the build path then try building this project", projectPath, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR), //$NON-NLS-1$ //$NON-NLS-2$
			new Problem("p1", "The type java.lang.Object cannot be resolved. It is indirectly referenced from required .class files", classTest1, 0, 1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR) //$NON-NLS-1$ //$NON-NLS-2$
		}
	);

	//----------------------------
	//           Step 2
	//----------------------------
	env.addExternalJars(projectPath, Util.getJavaClassLibs());

	incrementalBuild();
	expectingNoProblems();
	expectingPresenceOf(new IPath[]{
		bin.append("p1").append("Test1.class"), //$NON-NLS-1$ //$NON-NLS-2$
	});
	env.removeProject(projectPath);
}

public void testMissingLibrary2() throws JavaModelException {
	IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	IPath classTest1 = env.addClass(root, "p2", "Test1", //$NON-NLS-1$ //$NON-NLS-2$
		"package p2;\n"+ //$NON-NLS-1$
		"public class Test1 {}" //$NON-NLS-1$
	);
	IPath classTest2 = env.addClass(root, "p2", "Test2", //$NON-NLS-1$ //$NON-NLS-2$
		"package p2;\n"+ //$NON-NLS-1$
		"public class Test2 {}" //$NON-NLS-1$
	);
	IPath classTest3 = env.addClass(root, "p3", "Test3", //$NON-NLS-1$ //$NON-NLS-2$
		"package p3;\n"+ //$NON-NLS-1$
		"public class Test3 {}" //$NON-NLS-1$
	);

	fullBuild();
	env.waitForAutoBuild();
	expectingSpecificProblemFor(
		projectPath,
		new Problem("", "The project was not built since its build path is incomplete. Cannot find the class file for java.lang.Object. Fix the build path then try building this project", projectPath, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

	Problem[] prob1 = env.getProblemsFor(classTest1);
	Problem[] prob2 = env.getProblemsFor(classTest2);
	Problem[] prob3 = env.getProblemsFor(classTest3);
	assertEquals("too many problems", prob1.length + prob2.length + prob3.length, 1); //$NON-NLS-1$
	if(prob1.length == 1) {
		expectingSpecificProblemFor(classTest1, new Problem("p2", "The type java.lang.Object cannot be resolved. It is indirectly referenced from required .class files", classTest1, 0, 1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
	} else if (prob2.length == 1) {
		expectingSpecificProblemFor(classTest2, new Problem("p2", "The type java.lang.Object cannot be resolved. It is indirectly referenced from required .class files", classTest2, -1, -1, -1, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
	} else {
		expectingSpecificProblemFor(classTest3, new Problem("p3", "The type java.lang.Object cannot be resolved. It is indirectly referenced from required .class files", classTest3, -1, -1, -1, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	//----------------------------
	//           Step 2
	//----------------------------
	env.addExternalJars(projectPath, Util.getJavaClassLibs());

	incrementalBuild();
	env.waitForAutoBuild();
	expectingNoProblems();
	expectingPresenceOf(new IPath[]{
		bin.append("p2").append("Test1.class"), //$NON-NLS-1$ //$NON-NLS-2$
		bin.append("p2").append("Test2.class"), //$NON-NLS-1$ //$NON-NLS-2$
		bin.append("p3").append("Test3.class") //$NON-NLS-1$ //$NON-NLS-2$
	});
	env.removeProject(projectPath);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=172345
public void testMissingLibrary3() throws JavaModelException {
	this.abortOnFailure = false; // this test is failing on some releng boxes => do not abort on failures
	IPath projectPath = env.addProject("Project");
	IJavaProject project = env.getJavaProject(projectPath);
	fullBuild();
	expectingNoProblems();
	project.setOption(JavaCore.CORE_INCOMPLETE_CLASSPATH, CompilerOptions.WARNING);
	env.waitForManualRefresh();
	env.addLibrary(projectPath, projectPath.append("/lib/dummy.jar"), null, null);
	fullBuild();
	env.waitForAutoBuild();
	expectingSpecificProblemFor(
		projectPath,
		new Problem("Build path", "Project 'Project' is missing required library: 'lib/dummy.jar'", projectPath, -1, -1, CategorizedProblem.CAT_BUILDPATH,
				IMarker.SEVERITY_WARNING));
	project.setOption(JavaCore.CORE_INCOMPLETE_CLASSPATH, CompilerOptions.ERROR);
	env.waitForManualRefresh();
	// force classpath change delta - should not have to do this
	IClasspathEntry[] classpath = project.getRawClasspath();
	IPath outputLocation;
	project.setRawClasspath(null, outputLocation = project.getOutputLocation(), false, null);
	project.setRawClasspath(classpath, outputLocation, false, null);
	fullBuild();
	env.waitForAutoBuild();
	expectingSpecificProblemFor(
		projectPath,
		new Problem("", "The project cannot be built until build path errors are resolved", projectPath, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR));
	expectingSpecificProblemFor(
		projectPath,
		new Problem("Build path", "Project 'Project' is missing required library: 'lib/dummy.jar'", projectPath, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR));
	env.removeProject(projectPath);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=172345
public void testMissingLibrary4() throws JavaModelException {
	this.abortOnFailure = false; // this test is failing on some releng boxes => do not abort on failures
	IPath projectPath = env.addProject("Project");
	IJavaProject project = env.getJavaProject(projectPath);
	fullBuild();
	expectingNoProblems();
	env.addLibrary(projectPath, projectPath.append("/lib/dummy.jar"), null, null);
	env.waitForManualRefresh();
	fullBuild();
	env.waitForAutoBuild();
	expectingSpecificProblemFor(
		projectPath,
		new Problem("", "The project cannot be built until build path errors are resolved", projectPath, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR));
	expectingSpecificProblemFor(
		projectPath,
		new Problem("Build path", "Project 'Project' is missing required library: 'lib/dummy.jar'", projectPath, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR));
	project.setOption(JavaCore.CORE_INCOMPLETE_CLASSPATH, CompilerOptions.WARNING);
	env.waitForManualRefresh();
	incrementalBuild();
	env.waitForManualRefresh();
	expectingSpecificProblemFor(
		projectPath,
		new Problem("Build path", "Project 'Project' is missing required library: 'lib/dummy.jar'", projectPath, -1, -1, CategorizedProblem.CAT_BUILDPATH,
				IMarker.SEVERITY_WARNING));
	env.removeProject(projectPath);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=172345
public void testIncompatibleJdkLEvelOnProject() throws JavaModelException {

	// Create project
	IPath projectPath = env.addProject("Project");
	IJavaProject project = env.getJavaProject(projectPath);
	String[] classlibs = Util.getJavaClassLibs();
	env.addExternalJars(projectPath, classlibs);
	Arrays.sort(classlibs);

	// Build it expecting no problem
	fullBuild();
	expectingNoProblems();

	// Build incompatible jdk level problem string
	String projectRuntime = project.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true);

	// Change project incompatible jdk level preferences to warning, perform incremental build and expect 1 problem
	project.setOption(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, CompilerOptions.WARNING);
	env.waitForManualRefresh();
	incrementalBuild();
	env.waitForAutoBuild();
	long projectRuntimeJDKLevel = CompilerOptions.versionToJdkLevel(projectRuntime);
	int max = classlibs.length;
	List expectedProblems = new ArrayList();
	for (int i = 0; i < max; i++) {
		String path = project.getPackageFragmentRoot(classlibs[i]).getPath().makeRelative().toString();
		Object target = JavaModel.getTarget(new Path(path).makeAbsolute(), true);
		long libraryJDK = org.eclipse.jdt.internal.core.util.Util.getJdkLevel(target);
		if (libraryJDK > projectRuntimeJDKLevel) {
			expectedProblems.add(getJdkLevelProblem(projectRuntime, path, IMarker.SEVERITY_WARNING));
		}
	}
	expectingProblemsFor(projectPath, expectedProblems);

	// Change project incompatible jdk level preferences to error, perform incremental build and expect 2 problems
	project.setOption(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, CompilerOptions.ERROR);
	env.waitForManualRefresh();
	incrementalBuild();
	env.waitForAutoBuild();

	expectedProblems = new ArrayList();
	for (int i = 0; i < max; i++) {
		String path = project.getPackageFragmentRoot(classlibs[i]).getPath().makeRelative().toString();
		Object target = JavaModel.getTarget(new Path(path).makeAbsolute(), true);
		long libraryJDK = org.eclipse.jdt.internal.core.util.Util.getJdkLevel(target);
		if (libraryJDK > projectRuntimeJDKLevel) {
			expectedProblems.add(getJdkLevelProblem(projectRuntime, path, IMarker.SEVERITY_ERROR));
		}
	}
	expectedProblems.add("Problem : The project cannot be built until build path errors are resolved [ resource : </Project> range : <-1,-1> category : <10> severity : <2>]");
	expectingProblemsFor(projectPath, expectedProblems);

	// Remove project to avoid side effect on other tests
	env.removeProject(projectPath);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=172345
public void testIncompatibleJdkLEvelOnWksp() throws JavaModelException {

	// Save preference
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	IEclipsePreferences preferences = manager.getInstancePreferences();
	String incompatibleJdkLevel = preferences.get(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, null);
	try {

		// Create project
		IPath projectPath = env.addProject("Project");
		IJavaProject project = env.getJavaProject(projectPath);
		String[] classlibs = Util.getJavaClassLibs();
		env.addExternalJars(projectPath, classlibs);

		// Build it expecting no problem
		fullBuild();
		env.waitForAutoBuild();
		expectingNoProblems();

		// Build incompatible jdk level problem string
		String wkspRuntime = JavaCore.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM);
		long wkspRuntimeJDKLevel = CompilerOptions.versionToJdkLevel(wkspRuntime);
		// sort classlibs
		Arrays.sort(classlibs);
		// Change workspace  incompatible jdk level preferences to warning, perform incremental build and expect 1 problem
		preferences.put(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, JavaCore.WARNING);
		env.waitForManualRefresh();
		incrementalBuild();
		env.waitForAutoBuild();
		List expectedProblems = new ArrayList();
		int max = classlibs.length;
		for (int i = 0; i < max; i++) {
			String path = project.getPackageFragmentRoot(classlibs[i]).getPath().makeRelative().toString();
			Object target = JavaModel.getTarget(new Path(path).makeAbsolute(), true);
			long libraryJDK = org.eclipse.jdt.internal.core.util.Util.getJdkLevel(target);
			if (libraryJDK > wkspRuntimeJDKLevel) {
				expectedProblems.add(getJdkLevelProblem(wkspRuntime, path, IMarker.SEVERITY_WARNING));
			}
		}
		expectingProblemsFor(projectPath, expectedProblems);

		// Change workspace incompatible jdk level preferences to error, perform incremental build and expect 2 problems
		preferences.put(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, JavaCore.ERROR);
		env.waitForManualRefresh();
		incrementalBuild();
		env.waitForAutoBuild();
		expectedProblems = new ArrayList();
		for (int i = 0; i < max; i++) {
			String path = project.getPackageFragmentRoot(classlibs[i]).getPath().makeRelative().toString();
			Object target = JavaModel.getTarget(new Path(path).makeAbsolute(), true);
			long libraryJDK = org.eclipse.jdt.internal.core.util.Util.getJdkLevel(target);
			if (libraryJDK > wkspRuntimeJDKLevel) {
				expectedProblems.add(getJdkLevelProblem(wkspRuntime, path, IMarker.SEVERITY_ERROR));
			}
		}
		expectedProblems.add("Problem : The project cannot be built until build path errors are resolved [ resource : </Project> range : <-1,-1> category : <10> severity : <2>]");
		expectingProblemsFor(projectPath, expectedProblems);

		// Remove project to avoid side effect on other tests
		env.removeProject(projectPath);
	} finally {
		// Put back workspace preferences same as before running the test
		if (incompatibleJdkLevel == null) {
			preferences.remove(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL);
		} else {
			preferences.put(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, incompatibleJdkLevel);
		}
	}
}

public void testMissingProject() throws JavaModelException {
	IPath project1Path = env.addProject("MP1"); //$NON-NLS-1$
	env.addExternalJars(project1Path, Util.getJavaClassLibs());

	IPath project2Path = env.addProject("MP2"); //$NON-NLS-1$
	env.addExternalJars(project2Path, Util.getJavaClassLibs());
	env.addRequiredProject(project2Path, project1Path);

	fullBuild();
	expectingNoProblems();

	//----------------------------
	//           Step 2
	//----------------------------
	env.removeProject(project1Path);

	incrementalBuild();
	env.waitForAutoBuild();
	expectingOnlyProblemsFor(project2Path);
	expectingOnlySpecificProblemsFor(project2Path,
		new Problem[] {
			new Problem("", "The project cannot be built until build path errors are resolved", project2Path, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR), //$NON-NLS-1$ //$NON-NLS-2$
			new Problem("Build path", "Project 'MP2' is missing required Java project: 'MP1'", project2Path, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR) //$NON-NLS-1$ //$NON-NLS-2$
		}
	);

	project1Path = env.addProject("MP1"); //$NON-NLS-1$
	env.addExternalJars(project1Path, Util.getJavaClassLibs());

	incrementalBuild();
	env.waitForAutoBuild();
	expectingNoProblems();

	//----------------------------
	//           Step 3
	//----------------------------
	Hashtable<String, String> options = JavaCore.getOptions();
	options.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.IGNORE);
	JavaCore.setOptions(options);
	env.waitForManualRefresh();
	env.removeProject(project1Path);

	incrementalBuild();
	env.waitForAutoBuild();
	expectingOnlyProblemsFor(project2Path);
	expectingOnlySpecificProblemFor(project2Path,
		new Problem("Build path", "Project 'MP2' is missing required Java project: 'MP1'", project2Path, -1, -1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR) //$NON-NLS-1$ //$NON-NLS-2$
	);

	project1Path = env.addProject("MP1"); //$NON-NLS-1$
	env.addExternalJars(project1Path, Util.getJavaClassLibs());

	incrementalBuild();
	env.waitForAutoBuild();
	expectingNoProblems();
	env.removeProject(project1Path);
	env.removeProject(project2Path);
}

public void testMissingOptionalProject() throws JavaModelException {
	IPath project1Path = env.addProject("MP1"); //$NON-NLS-1$
	env.addExternalJars(project1Path, Util.getJavaClassLibs());

	IPath project2Path = env.addProject("MP2"); //$NON-NLS-1$
	env.addExternalJars(project2Path, Util.getJavaClassLibs());
	env.addRequiredProject(project2Path, project1Path, true/*optional*/);

	fullBuild();
	expectingNoProblems();

	//----------------------------
	//           Step 2
	//----------------------------
	env.removeProject(project1Path);

	incrementalBuild();
	expectingNoProblems();

	project1Path = env.addProject("MP1"); //$NON-NLS-1$
	env.addExternalJars(project1Path, Util.getJavaClassLibs());

	incrementalBuild();
	expectingNoProblems();

	//----------------------------
	//           Step 3
	//----------------------------
	Hashtable<String, String> options = JavaCore.getOptions();
	options.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.IGNORE);
	JavaCore.setOptions(options);
	env.waitForManualRefresh();
	env.removeProject(project1Path);

	incrementalBuild();
	env.waitForAutoBuild();
	expectingNoProblems();

	project1Path = env.addProject("MP1"); //$NON-NLS-1$
	env.addExternalJars(project1Path, Util.getJavaClassLibs());

	incrementalBuild();
	expectingNoProblems();
	env.removeProject(project1Path);
	env.removeProject(project2Path);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=160132
public void test0100() throws JavaModelException {
	if (!AbstractCompilerTest.isJRELevel(AbstractCompilerTest.F_1_5)) {
		// expected to run only in 1.5 mode on top of a jre 1.5 or above
		return;
	}
	IPath projectPath = env.addProject("P", "1.5");
	IPath defaultPackagePath = env.addPackage(projectPath, "");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.addClass(defaultPackagePath, "X",
		"public interface X<E extends Object & X.Entry> {\n" +
		"  interface Entry {\n" +
		"    interface Internal extends Entry {\n" +
		"      Internal createEntry();\n" +
		"    }\n" +
		"  }\n" +
		"}"
	);
	fullBuild();
	expectingNoProblems();
	env.addClass(defaultPackagePath, "Y",
		"public class Y implements X.Entry.Internal {\n" +
		"  public Internal createEntry() {\n" +
		"    return null;\n" +
		"  }\n" +
		"}");
	incrementalBuild();
	expectingNoProblems();
	env.removeProject(projectPath);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=143025
public void testMissingOutputFolder() throws JavaModelException {
	IPath projectPath = env.addProject("P"); //$NON-NLS-1$
	env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
	IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

	fullBuild();
	expectingNoProblems();

	env.removeFolder(bin);

	incrementalBuild();
	expectingNoProblems();
	expectingPresenceOf(bin); // check that bin folder was recreated and is marked as derived
	if (!env.getProject(projectPath).getFolder("bin").isDerived())
		fail("output folder is not derived");
	env.removeProject(projectPath);
}
@Override
protected void tearDown() throws Exception {
	super.tearDown();
}
}
