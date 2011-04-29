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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.ResolvedBinaryField;
import org.eclipse.jdt.internal.core.ResolvedBinaryMethod;
import org.eclipse.jdt.internal.core.ResolvedBinaryType;
import org.eclipse.jdt.internal.core.ResolvedSourceField;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;
import org.eclipse.jdt.internal.core.ResolvedSourceType;

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


	protected void assertCodeSelect(String structureContents,
            String groovyContents,
            String toFind) throws Exception, JavaModelException {
        assertCodeSelect(structureContents, null, groovyContents, toFind);
    }

    protected void assertCodeSelect(String structureContents,
            String javaContents, String groovyContents, String toFind)
            throws Exception, JavaModelException {
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind, toFind);
    }

    protected void assertCodeSelect(String structureContents,
            String javaContents, String groovyContents, String toFind,
            String elementName) throws Exception, JavaModelException {

        if (structureContents != null) {
            if (javaContents != null) {
                createJavaUnit("Structure", structureContents);
            } else {
                // this is an array test, use a different file name
                createJavaUnit("XX", structureContents);
            }
        }
        GroovyCompilationUnit groovyUnit = createUnit(groovyContents);
        ICompilationUnit javaUnit = null;
        if (javaContents != null) {
            javaUnit = createJavaUnit("Java", javaContents);
        }
        incrementalBuild();
        expectingNoProblems();

        // check the groovy code select
        IJavaElement[] eltFromGroovy = groovyUnit.codeSelect(
                groovyContents.lastIndexOf(toFind), toFind.length());
        assertEquals("Should have found a selection", 1, eltFromGroovy.length);
        assertEquals("Should have found reference to: " + elementName,
                elementName,
                eltFromGroovy[0].getElementName());

        // check the java code select
        if (javaUnit != null) {
            IJavaElement[] eltFromJava = javaUnit.codeSelect(
                    javaContents.lastIndexOf(toFind), toFind.length());
            assertEquals("Should have found a selection", 1, eltFromJava.length);
            assertEquals("Should have found reference to: " + elementName,
                    elementName,
                    eltFromJava[0].getElementName());

            // now check that the unique keys of each of them are the same
            String groovyUniqueKey = getUniqueKey(eltFromGroovy[0]);
            String javaUniqueKey = getUniqueKey(eltFromJava[0]);
            assertEquals("Invalid unique key from groovy", javaUniqueKey,
                    groovyUniqueKey);
        }

    }

    /**
     * @param iJavaElement
     * @return
     */
    protected String getUniqueKey(IJavaElement elt) {
        if (elt instanceof ResolvedSourceField) {
            return ((ResolvedSourceField) elt).getKey();
        } else if (elt instanceof ResolvedSourceMethod) {
            return ((ResolvedSourceMethod) elt).getKey();
        } else if (elt instanceof ResolvedSourceType) {
            return ((ResolvedSourceType) elt).getKey();
        }
        if (elt instanceof ResolvedBinaryField) {
            return ((ResolvedBinaryField) elt).getKey();
        } else if (elt instanceof ResolvedBinaryMethod) {
            return ((ResolvedBinaryMethod) elt).getKey();
        } else if (elt instanceof ResolvedBinaryType) {
            return ((ResolvedBinaryType) elt).getKey();
        }
        fail("Element " + elt + " is not resolved");
        // won't get here
        return null;
    }

    protected GroovyCompilationUnit createUnit(String contents) throws Exception {
        return createUnit("Hello", contents);
    }

    protected GroovyCompilationUnit createUnit(String name, String contents) throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addGroovyClass(root, "", name, contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, name + ".groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        return unit;
    }

    protected ICompilationUnit createJavaUnit(String className, String contents)
            throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addClass(root, "", className, contents);
        ICompilationUnit unit = getJavaCompilationUnit(root, className
                + ".java");
        assertTrue("Hello groovy unit should exist", unit.exists());
        return unit;
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
