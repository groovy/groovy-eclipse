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

import java.util.Hashtable;

import junit.framework.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * Basic tests of the image builder.
 */
public class BasicBuildTests extends BuilderTests {
	public BasicBuildTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(BasicBuildTests.class);
	}

	public void testBuild() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "Hello",
			"package p1;\n"+
			"public class Hello {\n"+
			"   public static void main(String args[]) {\n"+
			"      System.out.println(\"Hello world\");\n"+
			"   }\n"+
			"}\n"
			);

		incrementalBuild(projectPath);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23894
	 */
	public void testToDoMarker() throws JavaModelException {
		Hashtable<String, String> options = JavaCore.getOptions();
		Hashtable<String, String> newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		IPath pathToA = env.addClass(root, "p", "A",
			"package p; \n"+
			"//todo nothing\n"+
			"public class A {\n"+
			"}");

		fullBuild(projectPath);
		expectingOnlySpecificProblemFor(pathToA, new Problem("A", "todo nothing", pathToA, 14, 26, -1, IMarker.SEVERITY_ERROR));

		JavaCore.setOptions(options);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=91426
	 */
	public void testToDoMarker2() throws JavaModelException {
		Hashtable<String, String> options = JavaCore.getOptions();
		Hashtable<String, String> newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW");

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		IPath pathToA = env.addClass(root, "p", "A",
			"package p; \n"+
			"//TODO normal\n"+
			"public class A {\n"+
			"	public void foo() {\n"+
			"		//FIXME high\n"+
			"	}\n"+
			"	public void foo2() {\n"+
			"		//XXX low\n"+
			"	}\n"+
			"}");

		fullBuild(projectPath);
		IMarker[] markers = env.getTaskMarkersFor(pathToA);
		assertEquals("Wrong size", 3, markers.length);
		try {
			IMarker marker = markers[0];
			Object priority = marker.getAttribute(IMarker.PRIORITY);
			String message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertTrue("Wrong message", message.startsWith("TODO "));
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_NORMAL), priority);

			marker = markers[1];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertTrue("Wrong message", message.startsWith("FIXME "));
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_HIGH), priority);

			marker = markers[2];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertTrue("Wrong message", message.startsWith("XXX "));
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_LOW), priority);
		} catch (CoreException e) {
			assertTrue(false);
		}
		JavaCore.setOptions(options);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=110797
	 */
	public void testTags() throws JavaModelException {
		Hashtable<String, String> options = JavaCore.getOptions();
		Hashtable<String, String> newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW");

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		IPath pathToA = env.addClass(root, "p", "A",
			"package p; \n"+
			"// TODO FIXME need to review the loop TODO should be done\n" +
			"public class A {\n" +
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
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_NORMAL), priority);

			marker = markers[1];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "FIXME need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_HIGH), priority);

			marker = markers[0];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "TODO need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_NORMAL), priority);
		} catch (CoreException e) {
			assertTrue(false);
		}
		JavaCore.setOptions(options);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=110797
	 */
	public void testTags2() throws JavaModelException {
		Hashtable<String, String> options = JavaCore.getOptions();
		Hashtable<String, String> newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW");

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		IPath pathToA = env.addClass(root, "p", "A",
			"package p; \n"+
			"// TODO TODO need to review the loop\n" +
			"public class A {\n" +
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
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_NORMAL), priority);

			marker = markers[0];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "TODO need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_NORMAL), priority);
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
		Hashtable<String, String> options = JavaCore.getOptions();
		try {
			Hashtable<String, String> newOptions = JavaCore.getOptions();
			newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
			newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW");

			JavaCore.setOptions(newOptions);

			IPath projectPath = env.addProject("Project");
			env.addExternalJars(projectPath, Util.getJavaClassLibs());

			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, "");

			IPath root = env.addPackageFragmentRoot(projectPath, "src");
			env.setOutputFolder(projectPath, "bin");

			IPath pathToA = env.addClass(root, "p", "A",
				"package p; \n"+
				"// TODO need to review\n" +
				"public class A {\n" +
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
		Hashtable<String, String> options = JavaCore.getOptions();
		Hashtable<String, String> newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.WARNING);

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "util", "MyException",
			"package util;\n" +
			"public class MyException extends Exception {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		);

		env.addClass(root, "p", "Test",
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
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "Aaa$Bbb$Ccc",
			"package p1;\n" +
			"\n" +
			"public class Aaa$Bbb$Ccc {\n" +
			"}"
		);

		fullBuild(projectPath);
		expectingNoProblems();
	}

	/**
	 * @bug 164707: ArrayIndexOutOfBoundsException in JavaModelManager if source level == 6.0
	 * @test Ensure that AIIOB does not longer happen with invalid source level string
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=164707"
	 */
	public void testBug164707() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		IJavaProject javaProject = env.getJavaProject(projectPath);
		javaProject.setOption(JavaCore.COMPILER_SOURCE, "invalid");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);
		expectingNoProblems();
	}

	/**
	 * @bug 75471: [prefs] no re-compile when loading settings
	 * @test Ensure that changing project preferences is well taking into account while rebuilding project
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=75471"
	 */
	public void _testUpdateProjectPreferences() throws JavaModelException {

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "util", "MyException",
			"package util;\n" +
			"public class MyException extends Exception {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		);

		IPath cuPath = env.addClass(root, "p", "Test",
			"package p;\n" +
			"import util.MyException;\n" +
			"public class Test {\n" +
			"}"
		);

		fullBuild(projectPath);
		expectingSpecificProblemFor(
			projectPath,
			new Problem("", "The import util.MyException is never used", cuPath, 18, 34, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING));

		IJavaProject project = env.getJavaProject(projectPath);
		project.setOption(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.IGNORE);
		incrementalBuild(projectPath);
		expectingNoProblems();
	}
	public void _testUpdateWkspPreferences() throws JavaModelException {

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "util", "MyException",
			"package util;\n" +
			"public class MyException extends Exception {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		);

		IPath cuPath = env.addClass(root, "p", "Test",
			"package p;\n" +
			"import util.MyException;\n" +
			"public class Test {\n" +
			"}"
		);

		fullBuild();
		expectingSpecificProblemFor(
			projectPath,
			new Problem("", "The import util.MyException is never used", cuPath, 18, 34, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING));

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
		Hashtable<String, String> options = JavaCore.getOptions();
		Hashtable<String, String> newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO!,TODO,TODO?");
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "HIGH,NORMAL,LOW");

		JavaCore.setOptions(newOptions);

		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		IPath pathToA = env.addClass(root, "p", "A",
			"package p; \n"+
			"// TODO! TODO? need to review the loop\n" +
			"public class A {\n" +
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
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_LOW), priority);

			marker = markers[0];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "TODO! need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_HIGH), priority);
		} catch (CoreException e) {
			assertTrue(false);
		}
		JavaCore.setOptions(options);
	}

}
