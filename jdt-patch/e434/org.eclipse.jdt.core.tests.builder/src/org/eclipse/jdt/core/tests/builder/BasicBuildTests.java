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
 *     Stephan Herrmann - Contribution for
 *								Bug 392727 - Cannot compile project when a java file contains $ in its file name
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import junit.framework.Test;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.builder.mockcompiler.MockCompilerFactory;
import org.eclipse.jdt.core.tests.builder.mockcompiler.MockCompilerFactory.MockCompiler;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.CompilerConfiguration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.builder.AbstractImageBuilder;
import org.osgi.framework.Bundle;

/**
 * Basic tests of the image builder.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class BasicBuildTests extends BuilderTests {
	public BasicBuildTests(String name) {
		super(name);
	}
	static {
//		TESTS_NAMES = new String[] { "testBug392727" };
	}
	{
		System.setProperty(JavaModelManager.MAX_COMPILED_UNITS_AT_ONCE, "0");
	}
	public static Test suite() {
		return buildTestSuite(BasicBuildTests.class);
	}

	public void testBuild() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n"+ //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23894
	 */
	public void testToDoMarker() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo"); //$NON-NLS-1$
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL"); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"//todo nothing\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingOnlySpecificProblemFor(pathToA, new Problem("A", "todo nothing", pathToA, 14, 26, -1, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		JavaCore.setOptions(options);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=91426
	 */
	public void testToDoMarker2() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX"); //$NON-NLS-1$
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW"); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"//TODO normal\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public void foo() {\n"+ //$NON-NLS-1$
			"		//FIXME high\n"+ //$NON-NLS-1$
			"	}\n"+ //$NON-NLS-1$
			"	public void foo2() {\n"+ //$NON-NLS-1$
			"		//XXX low\n"+ //$NON-NLS-1$
			"	}\n"+ //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		fullBuild(projectPath);
		IMarker[] markers = env.getTaskMarkersFor(pathToA);
		assertEquals("Wrong size", 3, markers.length);
		try {
			IMarker marker = markers[0];
			Object priority = marker.getAttribute(IMarker.PRIORITY);
			String message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertTrue("Wrong message", message.startsWith("TODO "));
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", Integer.valueOf(IMarker.PRIORITY_NORMAL), priority);

			marker = markers[1];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertTrue("Wrong message", message.startsWith("FIXME "));
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", Integer.valueOf(IMarker.PRIORITY_HIGH), priority);

			marker = markers[2];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertTrue("Wrong message", message.startsWith("XXX "));
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", Integer.valueOf(IMarker.PRIORITY_LOW), priority);
		} catch (CoreException e) {
			assertTrue(false);
		}
		JavaCore.setOptions(options);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=110797
	 */
	public void testTags() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX"); //$NON-NLS-1$
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW"); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"// TODO FIXME need to review the loop TODO should be done\n" + //$NON-NLS-1$
			"public class A {\n" + //$NON-NLS-1$
			"}");

		fullBuild(projectPath);
		IMarker[] markers = env.getTaskMarkersFor(pathToA);
		assertEquals("Wrong size", 3, markers.length);
		try {
			IMarker marker = markers[2];
			Object priority = marker.getAttribute(IMarker.PRIORITY);
			String message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "TODO should be done", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", Integer.valueOf(IMarker.PRIORITY_NORMAL), priority);

			marker = markers[1];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "FIXME need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", Integer.valueOf(IMarker.PRIORITY_HIGH), priority);

			marker = markers[0];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "TODO need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", Integer.valueOf(IMarker.PRIORITY_NORMAL), priority);
		} catch (CoreException e) {
			assertTrue(false);
		}
		JavaCore.setOptions(options);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=110797
	 */
	public void testTags2() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX"); //$NON-NLS-1$
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW"); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"// TODO TODO need to review the loop\n" + //$NON-NLS-1$
			"public class A {\n" + //$NON-NLS-1$
			"}");

		fullBuild(projectPath);
		IMarker[] markers = env.getTaskMarkersFor(pathToA);
		assertEquals("Wrong size", 2, markers.length);
		try {
			IMarker marker = markers[1];
			Object priority = marker.getAttribute(IMarker.PRIORITY);
			String message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "TODO need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", Integer.valueOf(IMarker.PRIORITY_NORMAL), priority);

			marker = markers[0];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "TODO need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", Integer.valueOf(IMarker.PRIORITY_NORMAL), priority);
		} catch (CoreException e) {
			assertTrue(false);
		}
		JavaCore.setOptions(options);
	}

	/*
	 * Ensures that a task tag is not user editable
	 * (regression test for bug 123721 two types of 'remove' for TODO task tags)
	 */
	public void testTags3() throws CoreException {
		Hashtable options = JavaCore.getOptions();

		try {
			Hashtable newOptions = JavaCore.getOptions();
			newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX"); //$NON-NLS-1$
			newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW"); //$NON-NLS-1$

			JavaCore.setOptions(newOptions);

			IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

			IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n"+ //$NON-NLS-1$
				"// TODO need to review\n" + //$NON-NLS-1$
				"public class A {\n" + //$NON-NLS-1$
				"}");

			fullBuild(projectPath);
			IMarker[] markers = env.getTaskMarkersFor(pathToA);
			assertEquals("Marker should not be editable", Boolean.FALSE, markers[0].getAttribute(IMarker.USER_EDITABLE));
		} finally {
			JavaCore.setOptions(options);
		}
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=92821
	 */
	public void testUnusedImport() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.WARNING);

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "util", "MyException", //$NON-NLS-1$ //$NON-NLS-2$
			"package util;\n" +
			"public class MyException extends Exception {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		);

		env.addClass(root, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" +
			"import util.MyException;\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * @throws MyException\n" +
			"	 */\n" +
			"	public void bar() {\n" +
			"	}\n" +
			"}"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		JavaCore.setOptions(options);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=98667
	 */
	public void test98667() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Aaa$Bbb$Ccc", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"\n" +  //$NON-NLS-1$
			"public class Aaa$Bbb$Ccc {\n" + //$NON-NLS-1$
			"}" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();
	}

	/**
	 * bug 164707: ArrayIndexOutOfBoundsException in JavaModelManager if source level == 6.0
	 * test Ensure that AIIOB does not longer happen with invalid source level string
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=164707"
	 */
	public void testBug164707() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		IJavaProject javaProject = env.getJavaProject(projectPath);
		javaProject.setOption(JavaCore.COMPILER_SOURCE, "invalid");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);
		expectingNoProblems();
	}

	/**
	 * bug 75471: [prefs] no re-compile when loading settings
	 * test Ensure that changing project preferences is well taking into account while rebuilding project
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=75471"
	 */
	public void _testUpdateProjectPreferences() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "util", "MyException", //$NON-NLS-1$ //$NON-NLS-2$
			"package util;\n" +
			"public class MyException extends Exception {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		);

		IPath cuPath = env.addClass(root, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" +
			"import util.MyException;\n" +
			"public class Test {\n" +
			"}"
		);

		fullBuild(projectPath);
		expectingSpecificProblemFor(
			projectPath,
			new Problem("", "The import util.MyException is never used", cuPath, 18, 34, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$ //$NON-NLS-2$

		IJavaProject project = env.getJavaProject(projectPath);
		project.setOption(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.IGNORE);
		incrementalBuild(projectPath);
		expectingNoProblems();
	}
	public void _testUpdateWkspPreferences() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "util", "MyException", //$NON-NLS-1$ //$NON-NLS-2$
			"package util;\n" +
			"public class MyException extends Exception {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		);

		IPath cuPath = env.addClass(root, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" +
			"import util.MyException;\n" +
			"public class Test {\n" +
			"}"
		);

		fullBuild();
		expectingSpecificProblemFor(
			projectPath,
			new Problem("", "The import util.MyException is never used", cuPath, 18, 34, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$ //$NON-NLS-2$

		// Save preference
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		IEclipsePreferences preferences = manager.getInstancePreferences();
		String unusedImport = preferences.get(JavaCore.COMPILER_PB_UNUSED_IMPORT, null);
		try {
			// Modify preference
			preferences.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.IGNORE);
			incrementalBuild();
			expectingNoProblems();
		}
		finally {
			if (unusedImport == null) {
				preferences.remove(JavaCore.COMPILER_PB_UNUSED_IMPORT);
			} else {
				preferences.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, unusedImport);
			}
		}
	}

	public void testTags4() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO!,TODO,TODO?"); //$NON-NLS-1$
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "HIGH,NORMAL,LOW"); //$NON-NLS-1$

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"// TODO! TODO? need to review the loop\n" + //$NON-NLS-1$
			"public class A {\n" + //$NON-NLS-1$
			"}");

		fullBuild(projectPath);
		IMarker[] markers = env.getTaskMarkersFor(pathToA);
		assertEquals("Wrong size", 2, markers.length);

		try {
			IMarker marker = markers[1];
			Object priority = marker.getAttribute(IMarker.PRIORITY);
			String message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "TODO? need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", Integer.valueOf(IMarker.PRIORITY_LOW), priority);

			marker = markers[0];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "TODO! need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", Integer.valueOf(IMarker.PRIORITY_HIGH), priority);
		} catch (CoreException e) {
			assertTrue(false);
		}
		JavaCore.setOptions(options);
	}

	// Bug 392727 - Cannot compile project when a java file contains $ in its file name
	public void testBug392727() throws JavaModelException {
		int save = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		try {
			IPath projectPath = env.addProject("Project");
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, "");

			IPath root = env.addPackageFragmentRoot(projectPath, "src");
			env.setOutputFolder(projectPath, "bin");

			// this class is the primary unit during build (see comment below)
			env.addClass(root, "pack",
				"Zork",
				"package pack;\npublic class Zork { Main main; }\n" // pull in Main first
			);

			env.addClass(root, "pack", "Main",
				"package pack;\n" +
				"public class Main {\n" +
				"	Main$Sub sub;\n" + // indirectly pull in Main$Sub
				"}\n"
			);

			env.addClass(root, "pack", "Main$Sub",
				"package pack;\n" +
				"public class Main$Sub { }\n"
			);

			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1;

			// Assumption regarding the order of compilation units:
			// - org.eclipse.core.internal.dtree.AbstractDataTreeNode.assembleWith(AbstractDataTreeNode[], AbstractDataTreeNode[], boolean)
			//   assembles children array in lexical order, so "Zork.java" is last
			// - org.eclipse.core.internal.watson.ElementTreeIterator.doIteration(DataTreeNode, IElementContentVisitor)
			//   iterates children last-to-first so "Zork.java" becomes first in sourceFiles of
			//   org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.addAllSourceFiles(ArrayList)
			// - org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.compile(SourceFile[])
			//   puts only "Zork.java" into 'toCompile' (due to MAX_AT_ONCE=1) and the others into 'remainingUnits'
			// This ensures that NameEnvironment is setup with "Main.java" and "Main$Sub.java" both served from 'additionalUnits'
			// which is essential for reproducing the bug.

			fullBuild(projectPath);
			expectingNoProblems();
		} finally {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = save;
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=386901
	public void testbBug386901() throws JavaModelException {

		int previous = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		try {
			IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

			env.addClass(root, "p", "AB", //$NON-NLS-1$ //$NON-NLS-2$
				"package p;	\n"+ //$NON-NLS-1$
				"public class AB {}	\n"+ //$NON-NLS-1$
				"class AZ {}"); //$NON-NLS-1$

			IPath pathToAA = env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
				"package p;	\n"+ //$NON-NLS-1$
				"public class AA extends AZ {}"); //$NON-NLS-1$

			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1; // units compiled in batches of '1' unit
			fullBuild(projectPath);
			expectingProblemsFor(
					pathToAA,
					"Problem : AZ cannot be resolved to a type [ resource : </Project/src/p/AA.java> range : <36,38> category : <40> severity : <2>]"
				);

			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 0; // All units compiled at once
			fullBuild(projectPath);
			expectingNoProblems();
		} finally {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = previous;
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425420
	public void testBug425420() throws JavaModelException {
		IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
		// don't env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath path = env.addClass(root, "", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"class X {\n" +
			"  void test() {\n" +
			"	int x = 0;\n" +
			"	int y = 0 >= 0 ? x : \"\"; \n" +
			"  }\n" +
			"}");

		fullBuild(projectPath);
		expectingProblemsFor(
				path,
				"Problem : The type java.lang.Object cannot be resolved. It is indirectly referenced from required .class files [ resource : </Project/src/X.java> range : <0,1> category : <10> severity : <2>]"
			);
	}
	public void testBug549942() throws JavaModelException {
		int save = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		try {
			IPath projectPath = env.addProject("Project");
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, "");

			IPath root = env.addPackageFragmentRoot(projectPath, "src");
			env.setOutputFolder(projectPath, "bin");

			env.addClass(root, "test",
				"ARequiresNested",
				"package test;\n" +
				"\n" +
				"public class ARequiresNested {\n" +
				"	Nested n;\n" +
				"}"
			);

			env.addClass(root, "test",
					"BRequiresToplevel",
					"package test;\n" +
					"\n" +
					"public class BRequiresToplevel {\n" +
					"	TopLevel t;\n" +
					"}"
				);

			env.addClass(root, "test",
					"TopLevel",
					"package test;\n" +
					"\n" +
					"public class TopLevel {\n" +
					"\n" +
					"}\n" +
					"\n" +
					"class Nested extends TopLevel {\n" +
					"}"
				);

			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 2;

			fullBuild(projectPath);
			expectingNoProblems();
		} finally {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = save;
		}
	}

	public void testCustomCompilerFactory() throws JavaModelException {
		final String CUSTOM_COMPILER_VALUE = MockCompilerFactory.class.getName();
		try {
			System.setProperty(AbstractImageBuilder.COMPILER_FACTORY_KEY, CUSTOM_COMPILER_VALUE);
			IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

			IPath path = env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
					"""
					package p1;
					public class Hello {
					   public static void main(String args[]) {
					      System.out.println(\"Hello world\");
					   }
					}
					"""
					);

			fullBuild(projectPath);

			expectingProblemsFor(
					path,
					"Problem : Compilation error from MockCompiler [ resource : </Project/src/p1/Hello.java> range : <0,1> category : <60> severity : <2>]"
				);
		} finally {
			System.clearProperty(AbstractImageBuilder.COMPILER_FACTORY_KEY);
		}
	}

	public void testCustomerCompilerFactoryWithAP() throws Exception {
		final String CUSTOM_COMPILER_VALUE = MockCompilerFactory.class.getName();
		List<CompilerConfiguration> configs = new ArrayList<>();
		Consumer<Compiler> listener = (compiler) -> {
			if (compiler instanceof MockCompiler mockCompiler) {
				configs.add(mockCompiler.compilerConfig);
			}
		};
		File projectRoot = null;
		IProject project = null;
		try {
			MockCompilerFactory.addListener(listener);
			System.setProperty(AbstractImageBuilder.COMPILER_FACTORY_KEY, CUSTOM_COMPILER_VALUE);
			projectRoot = copyFiles("autoValueSnippet", true);
			IPath dotProjectPath = new org.eclipse.core.runtime.Path(new File(projectRoot, IProjectDescription.DESCRIPTION_FILE_NAME).getAbsolutePath());
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IProjectDescription descriptor = workspace.loadProjectDescription(dotProjectPath);
			String projectName = descriptor.getName();
			project = workspace.getRoot().getProject(projectName);
			project.create(descriptor, new NullProgressMonitor());
			project.open(IResource.NONE, new NullProgressMonitor());

			// full build
			project.build(IncrementalProjectBuilder.FULL_BUILD, null);

			// It creates compiler 4 times (MAIN full build, TEST full build, MAIN incremental build, TEST incremental build)
			assertFalse(configs.isEmpty());
			CompilerConfiguration config = configs.get(0);
			List<URI> processorPaths = config.annotationProcessorPaths();
			assertEquals(2, processorPaths.size());
			assertTrue(processorPaths.get(0).getPath().endsWith("auto-value-1.6.5.jar"));
			assertTrue(processorPaths.get(1).getPath().endsWith("auto-value-annotations-1.6.5.jar"));

			List<IContainer> generatedSourcePaths = config.generatedSourcePaths();
			assertEquals(1, generatedSourcePaths.size());
			assertEquals(".apt_generated", generatedSourcePaths.get(0).getRawLocation().lastSegment());

			List<IContainer> sourcePaths = config.sourcepaths();
			assertEquals(2, sourcePaths.size());

			assertEquals("src", sourcePaths.get(0).getRawLocation().lastSegment());
			assertEquals(".apt_generated", sourcePaths.get(1).getRawLocation().lastSegment());

			List<URI> classPaths = config.classpaths();
			assertEquals(2, classPaths.size());
			assertTrue(classPaths.get(0).getPath().endsWith("bin"));
			assertTrue(classPaths.get(1).getPath().endsWith("auto-value-annotations-1.6.5.jar"));

			List<IContainer> moduleSourcePaths = config.moduleSourcepaths();
			assertEquals(0, moduleSourcePaths.size());

			List<URI> modulePaths = config.modulepaths();
			assertEquals(0, modulePaths.size());

			Map<IContainer, IContainer> sourceOutputMapping = config.sourceOutputMapping();
			assertEquals(2, sourceOutputMapping.size());

			IFile aptGeneratedFile = project.getFile(".apt_generated/AutoValue_Outer.java");
			assertFalse("The default APT generation should be disabled for custom compiler", aptGeneratedFile.exists());
		} finally {
			MockCompilerFactory.removeListener(listener);
			System.clearProperty(AbstractImageBuilder.COMPILER_FACTORY_KEY);
			if (project != null && project.exists()) {
				project.close(new NullProgressMonitor());
			}
			if (projectRoot != null) {
				deleteDirectory(projectRoot.toPath());
			}
		}
	}

	private File copyFiles(String path, boolean reimportIfExists) throws IOException {
		File from = new File(getSourceProjectDirectory(), path);
		File to = new File(getWorkingProjectDirectory(), path);
		if (to.exists()) {
			if (!reimportIfExists) {
				return to;
			}
			if (to.isFile()) {
				Files.delete(to.toPath());
			} else {
				deleteDirectory(to.toPath());
			}
		}

		if (from.isDirectory()) {
			copyDirectory(from.toPath(), to.toPath());
		} else {
			try {
				Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				ILog.get().error("Failed to copy the file - " + from.getCanonicalPath(), e); //$NON-NLS-1$
				throw e;
			}
		}

		return to;
	}

	private File getSourceProjectDirectory() throws IOException {
		final String pluginId = "org.eclipse.jdt.core.tests.builder";
		return getFileFromPlugin(pluginId, "resources");
	}

	public static File getFileFromPlugin(String pluginId, String relativePath) throws IOException {
		Bundle bundle = Platform.getBundle(pluginId);
		if (bundle == null) {
			throw new IOException("Plugin not found: " + pluginId);
		}

		URL fileURL = bundle.getEntry(relativePath);
		if (fileURL == null) {
			throw new IOException("File not found: " + relativePath);
		}

		URL resolvedFileURL = FileLocator.toFileURL(fileURL);
		return new File(resolvedFileURL.getPath());
	}

	private File getWorkingProjectDirectory() throws IOException {
		File dir = new File("target", "workingProjects");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}

	private static void deleteDirectory(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static void copyDirectory(Path sourceDirectory, Path targetDirectory) throws IOException {
		Files.walk(sourceDirectory)
				.forEach(sourcePath -> {
					Path targetPath = targetDirectory.resolve(sourceDirectory.relativize(sourcePath));
					try {
						Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						// ignore
					}
				});
	}

	public void testFallbackForProblematicCompilerFactory() throws JavaModelException {
		final String CUSTOM_COMPILER_VALUE = "x.y.NotFoundCompilerFactory";
		try {
			System.setProperty(AbstractImageBuilder.COMPILER_FACTORY_KEY, CUSTOM_COMPILER_VALUE);
			IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

			IPath path = env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
					"""
					package p1;
					public class Hello {
					   public static void main(String args[]) {
					      int unUsedVarable;
					   }
					}
					"""
					);

			fullBuild(projectPath);

			// If the custom compiler is not found, should fall back to the default ECJ
			expectingProblemsFor(
					path,
					"Problem : The value of the local variable unUsedVarable is not used [ resource : </Project/src/p1/Hello.java> range : <87,100> category : <120> severity : <1>]"
				);
		} finally {
			System.clearProperty(AbstractImageBuilder.COMPILER_FACTORY_KEY);
		}
	}

}
