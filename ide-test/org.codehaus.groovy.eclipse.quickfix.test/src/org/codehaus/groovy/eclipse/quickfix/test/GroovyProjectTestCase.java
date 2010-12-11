/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.quickfix.test;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;

/**
 * Base test harness for creating Groovy and Java types in a Groovy project and
 * testing quick fixes, including helper methods to get problem markers.
 * 
 * @author Nieraj Singh
 * 
 */
public class GroovyProjectTestCase extends EclipseTestCase {

	public static final String PACKAGE = "com.test";
	private IPackageFragment packageFrag;

	protected void setUp() throws Exception {
		super.setUp();
		// This already adds the Groovy nature, so no need to add it separately
		GroovyRuntime.addGroovyRuntime(testProject.getProject());
		packageFrag = testProject.createPackage(PACKAGE);
	}

	/**
	 * Creates a Groovy Type in the test package.
	 * 
	 * @param unitName
	 * @param contents
	 * @return
	 * @throws Exception
	 */
	protected ICompilationUnit createGroovyTypeInTestPackage(String fileName,
			String contents) throws Exception {
		return createGroovyType(packageFrag, fileName, contents);
	}

	/**
	 * Create a Groovy type in the specified package
	 * 
	 * @param pack
	 * @param unitName
	 * @param contents
	 * @return
	 * @throws Exception
	 */
	protected ICompilationUnit createGroovyType(IPackageFragment pack,
			String fileName, String contents) throws Exception {
		IFile file = testProject.createGroovyType(pack, fileName, contents);
		assertTrue(file.getName() + " should exist", file.exists());
		fullProjectBuild();
		waitForIndexes();
		ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
		return unit;
	}

	protected ICompilationUnit createJavaTypeInTestPackage(String fileName,
			String contents) throws Exception {
		ICompilationUnit unit = testProject.createJavaType(packageFrag,
				fileName, contents).getCompilationUnit();

		fullProjectBuild();
		waitForIndexes();
		IFile file = (IFile) unit.getResource();
		assertTrue(file.getName() + " should exist", file.exists());
		return unit;
	}

	protected IProject getTestProject() {
		return testProject.getProject();
	}

	/**
	 * Java package where Groovy and Java classes should be added.
	 * 
	 * @return
	 */
	protected IPackageFragment getTestPackage() {
		return packageFrag;
	}

	protected IMarker[] getProjectJDTFailureMarkers() throws CoreException {

		return getTestProject().findMarkers(
				IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false,
				IResource.DEPTH_INFINITE);

	}

	protected String[] getMarkerMessages(IMarker marker) throws Exception {
		String message = (String) marker.getAttribute(IMarker.MESSAGE);
		return new String[] { message };
	}

	protected IMarker[] getCompilationUnitJDTFailureMarkers(
			ICompilationUnit unit) throws Exception {
		return unit.getResource().findMarkers(
				IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false,
				IResource.DEPTH_INFINITE);
	}

}
