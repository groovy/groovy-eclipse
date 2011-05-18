/*******************************************************************************
 * Copyright (c) 2011 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.groovy.tests.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.Test;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.groovy.tests.compiler.ReconcilerUtils;
import org.eclipse.jdt.core.groovy.tests.compiler.ReconcilerUtils.ReconcileResults;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * These tests are about building and working with complete projects.
 * <p>
 * To add a new project:
 * <ul>
 * <li>Create a folder named after the project in the testdata folder
 * <li>In there create a source.zip containing your source code (call it source.zip)
 * <li>Create a folder 'lib' and copy all the dependencies the source has into there (as jar files)
 * <li>It can be helpful to also create a readme.txt at the project level describing
 * where the source is from, what commit it is (e.g. git commit tag)
 * </ul>
 * <p>
 * Once setup like that it is usable for testing here.
 * 
 * @author Andy CLement
 * @since 2.5.1
 */
public class FullProjectTests extends GroovierBuilderTests {

	public FullProjectTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(FullProjectTests.class);
	}

	public void testReconcilingGPars() throws Exception {
		String fixture = "gpars";

		IPath projectPath = env.addProject(fixture, "1.6");
		
		setupProject(fixture, projectPath);

		// Build the project
		long stime = System.currentTimeMillis();
		incrementalBuild(projectPath);
		System.out.println("Time to build " + fixture + " is "
				+ (System.currentTimeMillis() - stime) + "ms");
		// build times seen: 8205 6628

		// Check the compile result
		expectedCompiledClassCount(370);
		expectingNoErrors();

		IJavaProject p = env.getJavaProject(projectPath);

		// Warm up
		for (int i=0;i<10;i++) {
			ReconcilerUtils.reconcileAllCompilationUnits(p,	true);
		}
		
		// Time the reconcile
		long t = 0;
		for (int i=0;i<20;i++) {
			ReconcileResults rr = ReconcilerUtils.reconcileAllCompilationUnits(p,
				true);
		System.out.println(rr.getTotalTimeSpentReconciling()+"ms");
		t+=rr.getTotalTimeSpentReconciling();
		}
		System.out.println("Average over 20runs:"+(t/20)+"ms");
	}

	private void setupProject(String fixture, IPath projectPath)
			throws JavaModelException, Exception {
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);
		env.addGroovyJars(projectPath);
		addLibraryDependencies(fixture, projectPath);

		// Define the source code
		env.removePackageFragmentRoot(projectPath, "");
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		defineProject(fixture, projectPath, root);
		env.setOutputFolder(projectPath, "bin");
	}

	/**
	 * Discover the jars in the 'lib' subfolder for the test fixture and add
	 * them as dependencies.
	 */
	private void addLibraryDependencies(String fixtureName, IPath projectPath)
			throws Exception {
		File f = getFile("testdata/" + fixtureName + "/lib");
		assertTrue(f.isDirectory());
		File[] libs = f.listFiles();
		for (File lib : libs) {
			String libpath = lib.getPath();
			libpath = libpath.substring(libpath.indexOf("testdata"));
			env.addJar(projectPath, libpath);
		}
	}

	private File getFile(String path) throws Exception {
		URL jar = Platform.getBundle(
				"org.eclipse.jdt.groovy.core.tests.builder").getEntry(path);
		File f = new File("../org.eclipse.jdt.groovy.core.tests.builder/"
				+ jar.toURI().getPath());
		return f;
	}

	/**
	 * Unzip the source.zip from the test fixture folder and define the contents in the project.
	 * @return the number of source files defined
	 */
	private int defineProject(String testfixtureName, IPath projectPath,
			IPath root) throws Exception {
		ZipFile zf = new ZipFile(getFile("testdata/" + testfixtureName
				+ "/source.zip"));
		int count = 0;
		Enumeration<? extends ZipEntry> entries = zf.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = entries.nextElement();
			String n = zipEntry.getName();
//			System.out.println("Contents of " + zipEntry.getName());
			if (!(n.endsWith(".groovy") || n.endsWith(".java"))) {
				continue;
			}
			count++;

			BufferedReader input = new BufferedReader(new InputStreamReader(
					zf.getInputStream(zipEntry)));
			StringBuilder contents = new StringBuilder();
			try {
				String line = null; // not declared within while loop
				while ((line = input.readLine()) != null) {
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			} finally {
				input.close();
			}
			int idx = n.lastIndexOf("/");
			String pn = n.substring(0, idx);
			n = n.substring(idx + 1);
			boolean isGroovy = n.endsWith(".groovy");
			n = n.substring(0, n.indexOf("."));

			if (isGroovy) {
				env.addGroovyClass(root, pn, n, contents.toString());

			} else {
				env.addClass(root, pn, n, contents.toString());
			}
		}
		zf.close();
		return count;
	}

	public List<String> loadFileAsList(URL file) throws Exception {
		List<String> lines = new ArrayList<String>();
		BufferedReader input = new BufferedReader(new InputStreamReader(
				file.openStream()));
		try {
			String line = null; // not declared within while loop
			while ((line = input.readLine()) != null) {
				lines.add(line);
			}
		} finally {
			input.close();
		}
		return lines;
	}
}
