/*******************************************************************************
 * Copyright (c) 2019 Andrey Loskutov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * Base class for testing builder related opened file leak tests, see bug 543506
 */
public abstract class AbstractLeakTest extends BuilderTests {

	static boolean WINDOWS;
	static boolean LINUX;
	static boolean MAC;
	static {
		String os = System.getProperty("os.name").toLowerCase();
		WINDOWS = os.contains("windows");
		LINUX = os.contains("linux");
		MAC = os.contains("mac");
	}

	public AbstractLeakTest(String name) {
		super(name);
	}

	protected void testLeaksOnIncrementalBuild() throws Exception {
		if(MAC) {
			return;
		}
		internalTestUsedLibraryLeaks(IncrementalProjectBuilder.INCREMENTAL_BUILD);
	}

	protected void testLeaksOnCleanBuild() throws Exception {
		if(MAC) {
			return;
		}
		internalTestUsedLibraryLeaks(IncrementalProjectBuilder.CLEAN_BUILD);
	}

	protected void testLeaksOnFullBuild() throws Exception {
		if(MAC) {
			return;
		}
		internalTestUsedLibraryLeaks(IncrementalProjectBuilder.FULL_BUILD);
	}

	private void internalTestUsedLibraryLeaks(int kind) throws Exception {
		String projectName = getName();
		IPath projectPath = env.addProject(projectName, getCompatibilityLevel());
		env.setOutputFolder(projectPath, "");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		IPath internalJar = addInternalJar(projectPath);

		createJavaFile(projectPath);

		switch (kind) {
		case IncrementalProjectBuilder.CLEAN_BUILD:
			cleanBuild(projectName);
			assertNotLeaked(internalJar);
			break;
		case IncrementalProjectBuilder.FULL_BUILD:
			fullBuild(projectPath);
			assertNotLeaked(internalJar);
			break;
		case IncrementalProjectBuilder.INCREMENTAL_BUILD:
			incrementalBuild(projectPath);
			changeJavaFile(projectPath);
			incrementalBuild(projectPath);
			assertNotLeaked(internalJar);
			break;
		default:
			fail("Unexpected build kind: " + kind);
		}
	}

	abstract String getCompatibilityLevel();

	private IPath addInternalJar(IPath projectPath) throws IOException, JavaModelException {
		IPath internalJar = addEmptyInternalJar(projectPath, "test.jar");
		return internalJar;
	}

	private void createJavaFile(IPath projectPath) {
		IPath path = env.addClass(projectPath, "a", "Other",
			"package a;\n" +
			"public class Other {\n" +
			"}"
		);
		IFile file = env.getWorkspace().getRoot().getFile(path);
		assertTrue("File should exists: " + path, file.exists());
	}

	private void changeJavaFile(IPath projectPath) throws Exception {
		IPath path = env.addClass(projectPath, "a", "Other",
			"package a;\n" +
			"public class Other {\n" +
			" // an extra comment \n" +
			"}"
		);
		IFile file = env.getWorkspace().getRoot().getFile(path);
		assertTrue("FIle should exists: " + path, file.exists());
	}

	private void assertNotLeaked(IPath path) throws Exception {
		expectingNoProblems();
		IFile file = env.getWorkspace().getRoot().getFile(path);
		assertTrue("FIle should exists: " + path, file.exists());
		if(WINDOWS) {
			tryRemoveFile(file);
		} else if (LINUX) {
			checkOpenDescriptors(file);
		}
	}

	private void tryRemoveFile(IFile file) {
		// Note: this is a lame attempt to check for leaked file descriptor
		// This works on Windows only, because windows does not allow to delete
		// files opened for reading.
		// On Linux we need something like lsof -p <my_process_id> | grep file name
		try {
			file.delete(true, null);
		} catch (CoreException e) {
			try {
				// second attempt to avoid delays on teardown
				Files.deleteIfExists(file.getLocation().toFile().toPath());
			} catch (Exception e2) {
				file.getLocation().toFile().delete();
				// ignore
			}
			throw new IllegalStateException("File leaked during build: " + file, e);
		}
		assertFalse("File should be deleted: " + file, file.exists());
	}

	private void checkOpenDescriptors(IFile file) throws Exception {
		List<String> openDescriptors = getOpenDescriptors();
		assertFalse("Failed to read opened file descriptors", openDescriptors.isEmpty());
		if(openDescriptors.contains(file.getLocation().toOSString())) {
			throw new IllegalStateException("File leaked during build: " + file);
		}
	}

	private static List<String> getOpenDescriptors() throws Exception {
		int pid = getPid();
		if (pid > 0) {
			// -F n : to print only name column (note: all lines start with "n")
			// -a : to "and" all following options
			// -b :to avoid blocking calls
			// -p <pid>: to select process with opened files
			List<String> lines = readLsofLines("lsof -F n -a -p " + pid + " / -b ", true);
			return lines;
		}
		return Collections.emptyList();
	}

	private static int getPid() throws Exception {
		try (BufferedReader rdr = new BufferedReader(new FileReader("/proc/self/stat"));) {
			return Integer.parseInt(new StringTokenizer(rdr.readLine()).nextToken());
		}
	}

	private static List<String> readLsofLines(String cmd, boolean skipFirst) throws Exception {
		List<String> lines = new ArrayList<>();
		Process process = Runtime.getRuntime().exec(cmd);
		try (BufferedReader rdr = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			if (skipFirst) {
				rdr.readLine();
			}
			String line;
			while((line = rdr.readLine())!= null) {
				// remove "n" prefix from lsof output
				if(line.startsWith("n")) {
					line = line.substring(1);
				}
				if(line.trim().length() > 1) {
					lines.add(line);
				}
			}
		}
		lines.sort(null);
		return lines;
	}

}