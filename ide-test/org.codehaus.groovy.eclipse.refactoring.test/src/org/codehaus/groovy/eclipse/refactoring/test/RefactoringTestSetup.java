/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.testplugin.JavaProjectHelper;

/**
 * Sets up an 1.5 project with rtstubs15.jar and compiler, code formatting, code generation, and template options.
 */
public class RefactoringTestSetup extends AbstractRefactoringTestSetup {

	public RefactoringTestSetup(Test test) {
		super(test);
	}

	public static final String CONTAINER= "src";
	private static IPackageFragmentRoot fgRoot;
	private static IPackageFragment fgPackageP;
	private static IJavaProject fgJavaTestProject;
	private static IPackageFragmentRoot[] fgJRELibraries;
	private static IPackageFragmentRoot fgGroovyLibrary;

	public static IPackageFragmentRoot getDefaultSourceFolder() throws Exception {
		if (fgRoot != null)
			return fgRoot;
		throw new Exception(RefactoringTestSetup.class.getName() + " not initialized");
	}

    public static IPackageFragmentRoot[] getJRELibraries() throws Exception {
        if (fgJRELibraries != null)
            return fgJRELibraries;
        throw new Exception(RefactoringTestSetup.class.getName() + " not initialized");
    }
    public static IClasspathEntry[] getJRELibrariesAsRawClasspathEntry() throws Exception {
        if (fgJRELibraries != null) {
            IClasspathEntry[] entries = new IClasspathEntry[fgJRELibraries.length];
            for (int i = 0; i < fgJRELibraries.length; i++) {
                entries[i] = fgJRELibraries[i].getRawClasspathEntry();
            }
            return entries;
        }
        throw new Exception(RefactoringTestSetup.class.getName() + " not initialized");
    }



	public static IJavaProject getProject()throws Exception {
		if (fgJavaTestProject != null)
			return fgJavaTestProject;
		throw new Exception(RefactoringTestSetup.class.getName() + " not initialized");
	}

	public static IPackageFragment getPackageP()throws Exception {
		if (fgPackageP != null)
			return fgPackageP;
		throw new Exception(RefactoringTestSetup.class.getName() + " not initialized");
	}

	protected void setUp() throws Exception {
		super.setUp();
		fgJavaTestProject= JavaProjectHelper.createGroovyProject("TestProject"+System.currentTimeMillis(), "bin");
		fgJRELibraries= addRTJars(fgJavaTestProject);
		fgGroovyLibrary= addGroovyJar(fgJavaTestProject);
		// just in case, remove the source root that is the root of the project (if it exists)
		JavaProjectHelper.removeFromClasspath(fgJavaTestProject, fgJavaTestProject.getProject().getFullPath());
		fgRoot= JavaProjectHelper.addSourceContainer(fgJavaTestProject, CONTAINER);
		fgPackageP= fgRoot.createPackageFragment("p", true, null);
	}


	public IPackageFragmentRoot getGroovyLibrary() {
	    return fgGroovyLibrary;
	}
	protected IPackageFragmentRoot[] addRTJars(IJavaProject project) throws CoreException {
		return JavaProjectHelper.addRTJars(project);
	}
	protected IPackageFragmentRoot addGroovyJar(IJavaProject project) throws CoreException {
	    return JavaProjectHelper.addGroovyJar(project);
	}

	protected void tearDown() throws Exception {
		JavaProjectHelper.delete(fgJavaTestProject);
		super.tearDown();
	}
}

