/*******************************************************************************
 * Copyright (c) 2019 Sebastian Zarnekow and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sebastian Zarnekow - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaModelManager.PerProjectInfo;
import org.eclipse.jdt.internal.core.builder.ClasspathLocation;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;
import org.eclipse.jdt.internal.core.builder.ReferenceCollection;
import org.eclipse.jdt.internal.core.builder.State;

import junit.framework.Test;

public class StateTest extends BuilderTests {

	public StateTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(StateTest.class);
	}

	public void testWriteAndReadState() throws JavaModelException, Exception {
		IPath interfaceProjectPath = env.addProject("Interface"); //$NON-NLS-1$
		env.addExternalJars(interfaceProjectPath, Util.getJavaClassLibs());

		IPath implementationProjectPath = env.addProject("Implementation"); //$NON-NLS-1$
		env.addExternalJars(implementationProjectPath, Util.getJavaClassLibs());
		env.addClassFolder(implementationProjectPath, interfaceProjectPath, false);

		env.addClass(interfaceProjectPath, "a", "Interfaze", //$NON-NLS-1$ //$NON-NLS-2$
			"package a;\n" +
			"public interface Interfaze {\n" +
			"	void callMe();\n" +
			"}" //$NON-NLS-1$
		);

		env.addClass(interfaceProjectPath, "c", "Impl1", //$NON-NLS-1$ //$NON-NLS-2$
			"package c;\n" +
			"import a.Interfaze;\n" +
			"public class Impl1 implements Interfaze {\n" +
			"	@Override\n" +
			"	public void callMe() {\n" +
			"	}\n" +
			"}"
		);

		env.addClass(implementationProjectPath, "b", "Impl2", //$NON-NLS-1$ //$NON-NLS-2$
				"package b;\n" +
				"import a.Interfaze;\n" +
				"public class Impl2 implements Interfaze {\n" +
				"	@Override\n" +
				"	public void callMe() {\n" +
				"	}\n" +
				"}" //$NON-NLS-1$
			);
		fullBuild();

		writeReadAndCompareReferences(interfaceProjectPath);
		writeReadAndCompareReferences(implementationProjectPath);
	}


	public void testBug563546() throws JavaModelException, Exception {
		IPath project = env.addProject("Bug563546"); //$NON-NLS-1$
		env.addExternalJars(project, Util.getJavaClassLibs());

		env.addClass(project, "a", "WithOther", //$NON-NLS-1$ //$NON-NLS-2$
			"package a;\n" +
			"class Other {\n" +
			"}\n" +
			"public class WithOther {\n" +
			"}" //$NON-NLS-1$
		);
		fullBuild();
		env.removePackage(project, "a");
		incrementalBuild();

		writeReadAndCompareReferences(project);
	}

	public void testBug567532() throws JavaModelException, Exception {
		IPath project = env.addProject("Bug567532"); //$NON-NLS-1$
		String[] classLibs = Util.getJavaClassLibs();
		for (String jar : classLibs) {
			env.addEntry(project,
					JavaCore.newLibraryEntry(
							new Path(jar),
							null,
							null,
							new IAccessRule[0],
							new IClasspathAttribute[] {
									JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, "true"),
									JavaCore.newClasspathAttribute(IClasspathAttribute.ADD_EXPORTS,
											"jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED") },
							false));
		}

		env.addClass(project, "a", "WithOther", //$NON-NLS-1$ //$NON-NLS-2$
			"package a;\n" +
			"class Other {\n" +
			"}\n" +
			"public class WithOther {\n" +
			"}" //$NON-NLS-1$
		);
		fullBuild();
		env.removePackage(project, "a");
		incrementalBuild();

		writeReadAndCompareTestBinaryLocations(project);
	}
	public void testSelfAnnotatedJars() throws CoreException, IOException {
		// derived from the same named test in ExternalAnnotation18Test:
		IPath projectPath = env.addProject("PrjTest", "1.8"); //$NON-NLS-1$
		IJavaProject project = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject(projectPath.segment(0)));

		project.setOption(JavaCore.CORE_JAVA_BUILD_EXTERNAL_ANNOTATIONS_FROM_ALL_LOCATIONS, JavaCore.ENABLED);

		String projectLoc = project.getProject().getLocation().toString();
		Util.createJar(new String[] {
				"pgen/CGen.java",
				"package pgen;\n" +
				"public class CGen {\n" +
				"	public String get(String in) { return in; }\n" +
				"}\n"
			},
			new String[] {
				"pgen/CGen.eea",
				"class pgen/CGen\n" +
				"\n" +
				"get\n" +
				" (Ljava/lang/String;)Ljava/lang/String;\n" +
				" (L1java/lang/String;)L1java/lang/String;\n"
			},
			projectLoc+"/lib/prj1.jar",
			"1.8");
		IClasspathEntry entry = JavaCore.newLibraryEntry(
				new Path("/PrjTest/lib/prj1.jar"),
				null/*access rules*/,
				null,
				false/*exported*/);
		env.addEntry(project.getPath(), entry);

		Util.createJar(new String[] {
				"pgen2/CGen2.java",
				"package pgen2;\n" +
				"public class CGen2 {\n" +
				"	public String get2(Exception in) { return in.toString(); }\n" +
				"}\n"
			},
			new String[] {
				"pgen2/CGen2.eea",
				"class pgen2/CGen2\n" +
				"\n" +
				"get2\n" +
				" (Ljava/lang/Exception;)Ljava/lang/String;\n" +
				" (L1java/lang/Exception;)L1java/lang/String;\n",
			},
			projectLoc+"/lib/prj2.jar",
			"1.8");
		entry = JavaCore.newLibraryEntry(
				new Path("/PrjTest/lib/prj2.jar"),
				null/*access rules*/,
				null,
				false/*exported*/);
		env.addEntry(project.getPath(), entry);
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);

		env.addFolder(project.getPath(), "src/p");
		env.addFile(project.getPath().append("src").append("p"), "Use.java",
				"package p;\n" +
				"import pgen.CGen;\n" +
				"import pgen2.CGen2;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"public class Use {\n" +
				"	public @NonNull String test(CGen c) {\n" +
				"		String s = c.get(null);\n" +
				"		return s;\n" +
				"	}\n" +
				"	public @NonNull String test2(CGen2 c) {\n" +
				"		String s = c.get2(null);\n" +
				"		return s;\n" +
				"	}\n" +
				"}\n");
		project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		writeReadAndCompareExternalAnnotationLocations(project.getProject());
	}

	private void writeReadAndCompareTestBinaryLocations(IPath projectPath)
			throws JavaModelException, IOException, CoreException {
		JavaModelManager javaModelManager = JavaModelManager.getJavaModelManager();
		IProject project = env.getProject(projectPath);
		PerProjectInfo info = javaModelManager.getPerProjectInfoCheckExistence(project);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		State savedState = (State) info.savedState;
		JavaBuilder.writeState(savedState, new DataOutputStream(outputStream));
		byte[] bytes = outputStream.toByteArray();
		State readState = JavaBuilder.readState(project, new DataInputStream(new ByteArrayInputStream(bytes)));
		assertEqualBinaryLocations(savedState.testBinaryLocations, readState.testBinaryLocations);

		assertEquals(readState, savedState);
	}

	private void assertEqualBinaryLocations(ClasspathLocation[] a,
			ClasspathLocation[] b) {
		assertEquals(a.length, b.length);
		assertArrayEquals(a, b);
	}

	private void writeReadAndCompareExternalAnnotationLocations(IProject project)
			throws JavaModelException, IOException, CoreException {
		JavaModelManager javaModelManager = JavaModelManager.getJavaModelManager();
		PerProjectInfo info = javaModelManager.getPerProjectInfoCheckExistence(project);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		State savedState = (State) info.savedState;
		JavaBuilder.writeState(savedState, new DataOutputStream(outputStream));
		byte[] bytes = outputStream.toByteArray();
		State readState = JavaBuilder.readState(project, new DataInputStream(new ByteArrayInputStream(bytes)));
		assertArrayEquals(savedState.binaryLocations, readState.binaryLocations);
		// beyond this point we know that both arrays have the same length
		for (int i=0; i < savedState.binaryLocations.length; i++) {
			assertTrue("comparing eea locations of "+savedState.binaryLocations[i], savedState.binaryLocations[i].externalAnnotationsEquals(readState.binaryLocations[i]));
		}

		assertEquals(readState, savedState);
	}

	private void writeReadAndCompareReferences(IPath projectPath)
			throws JavaModelException, IOException, CoreException {
		JavaModelManager javaModelManager = JavaModelManager.getJavaModelManager();
		IProject project = env.getProject(projectPath);
		PerProjectInfo info = javaModelManager.getPerProjectInfoCheckExistence(project);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		State savedState = (State) info.savedState;
		JavaBuilder.writeState(savedState, new DataOutputStream(outputStream));
		byte[] bytes = outputStream.toByteArray();
		State readState = JavaBuilder.readState(project, new DataInputStream(new ByteArrayInputStream(bytes)));
		Map<String, ReferenceCollection> readReferences = readState.getReferences();
		assertEqualLookupTables(savedState.getReferences(), readReferences);
		assertEqualTypeLocators(savedState.typeLocators, readState.typeLocators);

		assertEquals(readState, savedState);
	}

	private void assertEqualTypeLocators(Map<String, String> tl1, Map<String, String> tl2) {
		assertEquals(tl1.size(), tl2.size());
		assertEquals(tl1.toString(), tl2.toString());

	}

	private void assertEqualLookupTables(Map<String, ReferenceCollection> expectation, Map<String, ReferenceCollection> actual) {
		assertEquals(expectation.size(), actual.size());
		Set<String> expectedKeys = expectation.keySet();
		for (String key : expectedKeys) {
			ReferenceCollection actualReferenceCollection = actual.get(key);
			ReferenceCollection expectedReferenceCollection = expectation.get(key);
			assertEqualReferenceCollections(expectedReferenceCollection, actualReferenceCollection);
		}
	}

	private void assertEqualReferenceCollections(ReferenceCollection expectedReferenceCollection,
			ReferenceCollection actualReferenceCollection) {
		{
			char[][] expected = getSimpleNameReferences(expectedReferenceCollection);
			char[][] actual = getSimpleNameReferences(actualReferenceCollection);
			assertArrayEquals(toStringArray(expected), toStringArray(actual));
		}
		{
			char[][] expected = getRootReferences(expectedReferenceCollection);
			char[][] actual = getRootReferences(actualReferenceCollection);
			assertArrayEquals(toStringArray(expected), toStringArray(actual));
		}
		{
			char[][][] expected = getQualifiedNameReferences(expectedReferenceCollection);
			char[][][] actual = getQualifiedNameReferences(actualReferenceCollection);
			assertArrayEquals(toStringArray(expected), toStringArray(actual));
		}
	}

	private static String[] toStringArray(char[][][] qualifiedNameReferences) {
		return Arrays.stream(qualifiedNameReferences).map(CharOperation::toString).toArray(String[]::new);
	}

	private static String[] toStringArray(char[][] qualifiedNameReferences) {
		return Arrays.stream(qualifiedNameReferences).map(CharOperation::charToString).toArray(String[]::new);
	}

	char[][][] getQualifiedNameReferences(ReferenceCollection collection) {
		try {
			Field fld = ReferenceCollection.class.getDeclaredField("qualifiedNameReferences");
			fld.setAccessible(true);
			return (char[][][]) fld.get(collection);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	char[][] getSimpleNameReferences(ReferenceCollection collection) {
		try {
			Field fld = ReferenceCollection.class.getDeclaredField("simpleNameReferences");
			fld.setAccessible(true);
			return (char[][]) fld.get(collection);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	char[][] getRootReferences(ReferenceCollection collection) {
		try {
			Field fld = ReferenceCollection.class.getDeclaredField("rootReferences");
			fld.setAccessible(true);
			return (char[][]) fld.get(collection);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
