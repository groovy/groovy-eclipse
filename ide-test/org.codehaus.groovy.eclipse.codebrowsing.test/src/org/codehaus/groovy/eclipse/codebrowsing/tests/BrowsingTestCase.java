/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.codebrowsing.tests;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.core.tests.util.BuilderTests;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;

/**
 * @author Andrew Eisenberg
 * @created Jun 3, 2009
 *
 * Includes utilities to help with all Code Browsing tests
 */
public abstract class BrowsingTestCase extends BuilderTests {

    public BrowsingTestCase(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ICompilationUnit[] wcs = new ICompilationUnit[0];
        int i = 0;
        do {
            wcs = JavaCore.getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY);
            for (ICompilationUnit workingCopy : wcs) {
                try {
                    workingCopy.discardWorkingCopy();
                    workingCopy.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            i++;
            if (i > 20) {
                fail("Could not delete working copies " + wcs);
            }
        } while (wcs.length > 0);
    }



    protected IPath createGenericProject() throws Exception {
        IPath projectPath;
        if (!ResourcesPlugin.getWorkspace().getRoot().getProject("Project").exists()) {
            projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$
            // remove old package fragment root so that names don't collide
            env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
            env.addExternalJars(projectPath, Util.getJavaClassLibs());
            env.addGroovyNature("Project");
            env.addGroovyJars(projectPath);
            fullBuild(projectPath);
            env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
            env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
        } else {
            projectPath = env.getJavaProject("Project").getPath();
        }
        return projectPath;
    }

    protected IFile getFile(IPath projectPath, String fileName) {
        return ResourcesPlugin.getWorkspace().getRoot().getFile(projectPath.append(fileName));
    }
    protected IFile getFile(IPath filePath) {
        return ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
    }

    protected IFolder getFolder(IPath projectPath, String folderName) {
        return ResourcesPlugin.getWorkspace().getRoot().getFolder(projectPath.append(folderName));
    }

    protected IProject getProject(IPath projectPath) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(projectPath.segment(0));
    }

    public ICompilationUnit getJavaCompilationUnit(IPath sourceRootPath, String qualifiedNameWithSlashesDotJava) {
        IFile file = getFile(sourceRootPath, qualifiedNameWithSlashesDotJava);
        return JavaCore.createCompilationUnitFrom(file);
    }
    public GroovyCompilationUnit getGroovyCompilationUnit(IPath sourceRootPath, String qualifiedNameWithSlashesDotGroovy) throws Exception {
        IFile file = getFile(sourceRootPath, qualifiedNameWithSlashesDotGroovy);
        waitUntilIndexesReady();
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        unit.becomeWorkingCopy(null);
        return unit;
    }

    public GroovyCompilationUnit getGroovyCompilationUnit(IPath pathToCU) throws Exception {
        IFile file = getFile(pathToCU);
        waitUntilIndexesReady();
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
        unit.becomeWorkingCopy(null);
        return unit;
    }

    /**
     * @param contents
     * @throws Exception
     */
    protected GroovyCompilationUnit getCompilationUnitFor(String contents) throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addGroovyClass(root, "", "File", contents);
        return getGroovyCompilationUnit(root, "File.groovy");
    }


	public static void waitUntilIndexesReady() {
		// dummy query for waiting until the indexes are ready
		SearchEngine engine = new SearchEngine();
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		try {
			engine.searchAllTypeNames(
				null,
				SearchPattern.R_EXACT_MATCH,
				"!@$#!@".toCharArray(),
				SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
				IJavaSearchConstants.CLASS,
				scope,
				new TypeNameRequestor() {
					@Override
                    public void acceptType(
						int modifiers,
						char[] packageName,
						char[] simpleTypeName,
						char[][] enclosingTypeNames,
						String path) {}
				},
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
		} catch (CoreException e) {
		}
	}
}
