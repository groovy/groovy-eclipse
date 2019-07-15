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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaModelManager.PerProjectInfo;
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
		SimpleLookupTable readReferences = readState.getReferences();
		assertEqualLookupTables(savedState.getReferences(), readReferences);
	}

	private void assertEqualLookupTables(SimpleLookupTable expectation, SimpleLookupTable actual) {
		assertEquals(expectation.elementSize, actual.elementSize);
		Object[] expectedKeys = expectation.keyTable;
		for(int i = 0; i < expectedKeys.length; i++) {
			Object key = expectedKeys[i];
			if (key != null) {
				ReferenceCollection actualReferenceCollection = (ReferenceCollection) actual.get(key);
				ReferenceCollection expectedReferenceCollection = (ReferenceCollection) expectation.valueTable[i];
				assertEqualReferenceCollections(expectedReferenceCollection, actualReferenceCollection);
			}
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
		return Arrays.stream(qualifiedNameReferences).map(a -> CharOperation.toString(a)).toArray(String[]::new);
	}

	private static String[] toStringArray(char[][] qualifiedNameReferences) {
		return Arrays.stream(qualifiedNameReferences).map(a -> CharOperation.charToString(a)).toArray(String[]::new);
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
