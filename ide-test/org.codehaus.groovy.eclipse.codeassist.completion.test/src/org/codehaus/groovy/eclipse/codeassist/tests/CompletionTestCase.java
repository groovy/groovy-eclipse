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

package org.codehaus.groovy.eclipse.codeassist.tests;

import java.util.List;

import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.codehaus.groovy.eclipse.test.SynchronizationUtils;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.core.tests.util.BuilderTests;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.UIPlugin;

/**
 * @author Andrew Eisenberg
 * @created Jun 5, 2009
 *
 * Includes utilities to help with all Content assist tests
 */
public abstract class CompletionTestCase extends BuilderTests {

    public CompletionTestCase(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        // close all editors
        UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
        super.tearDown();
    }
    
    protected IPath createGenericProject() throws Exception {
        IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyNature("Project");
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
        env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
        return projectPath;
    }
    
    protected boolean genericProjectExists() {
        return env.getProject("Project") != null && env.getProject("Project").exists();
    }

    protected IFile getFile(IPath projectPath, String fileName) {
        return ResourcesPlugin.getWorkspace().getRoot().getFile(projectPath.append(fileName));
    }

    protected IFolder getolder(IPath projectPath, String folderName) {
        return ResourcesPlugin.getWorkspace().getRoot().getFolder(projectPath.append(folderName));
    }

    protected IProject getProject(IPath projectPath) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(projectPath.segment(0));
    }

    public ICompilationUnit getJavaCompilationUnit(IPath sourceRootPath, String qualifiedNameWithSlashesDotJava) {
        IFile file = getFile(sourceRootPath, qualifiedNameWithSlashesDotJava);
        return JavaCore.createCompilationUnitFrom(file);
    }
    public ICompilationUnit getCompilationUnit(IPath fullPathName) {
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(fullPathName);
        return JavaCore.createCompilationUnitFrom(file);
    }
    public GroovyCompilationUnit getGroovyCompilationUnit(IPath sourceRootPath, String qualifiedNameWithSlashesDotGroovy) {
        IFile file = getFile(sourceRootPath, qualifiedNameWithSlashesDotGroovy);
        return (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
    }
    
    protected ICompletionProposal[] performContentAssist(ICompilationUnit unit, int offset, Class<? extends IJavaCompletionProposalComputer> computerClass) 
            throws Exception {
        // ensure opens with Groovy editor
        if (unit instanceof GroovyCompilationUnit) {
            unit.getResource().setPersistentProperty(IDE.EDITOR_KEY, "org.codehaus.groovy.eclipse.editor.GroovyEditor");
        }
        JavaEditor editor = (JavaEditor) EditorUtility.openInEditor(unit);
        JavaSourceViewer viewer = (JavaSourceViewer) editor.getViewer();
        JavaContentAssistInvocationContext context = new JavaContentAssistInvocationContext(viewer, offset, editor);
        
        IJavaCompletionProposalComputer computer = computerClass.newInstance();
        List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
        editor.close(false);
        return proposals.toArray(new ICompletionProposal[proposals.size()]);
    }
    
    protected void proposalExists(ICompletionProposal[] proposals, String name, int expectedCount) {
        boolean isType = name.contains(" - ");
        proposalExists(proposals, name, expectedCount, isType);
    }
    protected void proposalExists(ICompletionProposal[] proposals, String name, int expectedCount, boolean isType) {
        int foundCount = 0;
        for (ICompletionProposal proposal : proposals) {
            
            // if a field
            String propName = proposal.getDisplayString();
            if (propName.startsWith(name + " ")) {
                foundCount ++;
            } else
            // if a method
            if (propName.startsWith(name + "(")) {
                foundCount ++;
            } else
            // if a type
            if (isType && propName.startsWith(name)) {
                foundCount ++;
            }
        }
        
        if (foundCount != expectedCount) {
            StringBuffer sb = new StringBuffer();
            for (ICompletionProposal proposal : proposals) {
                sb.append("\n" + proposal.toString());
            }
            fail("Expected to find proposal '" + name + "' " + expectedCount + " times, but found it " + foundCount + " times.\nAll Proposals:" + sb);
        }
    }
    
    protected void checkReplacementString(ICompletionProposal[] proposals, String expectedReplacement, int expectedCount) {
        int foundCount = 0;
        for (ICompletionProposal proposal : proposals) {
            AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposal;
            String replacement = javaProposal.getReplacementString();
            if (replacement.equals(expectedReplacement)) {
                foundCount ++;
            }
        }
        
        if (foundCount != expectedCount) {
            StringBuffer sb = new StringBuffer();
            for (ICompletionProposal proposal : proposals) {
                AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposal;
                sb.append("\n" + javaProposal.getReplacementString());
            }
            fail("Expected to find proposal '" + expectedReplacement + "' " + expectedCount + " times, but found it " + foundCount + " times.\nAll Proposals:" + sb);
        }
    }

    
    protected void validateProposal(CompletionProposal proposal, String name) {
        assertEquals(proposal.getName(), name);
    }
    
    protected int getIndexOf(String contents, String lookFor) {
        return contents.indexOf(lookFor)+lookFor.length();
    }
    protected int getLastIndexOf(String contents, String lookFor) {
        return contents.lastIndexOf(lookFor)+lookFor.length();
    }

    protected ICompletionProposal[] createProposalsAtOffset(String contents, int completionOffset)
        throws Exception {
        return createProposalsAtOffset(contents, null, completionOffset);
        
    }
    protected ICompletionProposal[] createProposalsAtOffset(String contents, String javaContents, int completionOffset)
            throws Exception {
        IPath projectPath = createGenericProject();
        IPath pack = projectPath.append("src");
        if (javaContents != null) {
            IPath pathToJavaClass = env.addClass(pack, "JavaClass", "public class JavaClass { }\n" + javaContents);
            ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
            unit.becomeWorkingCopy(null);
        }
        
        IPath pathToGroovyClass = env.addGroovyClass(pack, "TransformerTest2", contents);
        fullBuild();
        ICompilationUnit unit = getCompilationUnit(pathToGroovyClass);
        unit.becomeWorkingCopy(null);
        
        // necessary???
        // wait for indexes to be built
        int timeout = 0;
        int timeoutThreshold = 100000;
        while (JavaModelManager.getIndexManager().awaitingJobsCount() > 0 && timeout < timeoutThreshold) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) { }
            timeout++;
        }
        if (timeout == timeoutThreshold) {
            fail("Failure to finish indexing project.  Timed out...");
        }
        SynchronizationUtils.waitForIndexingToComplete();
        return performContentAssist(unit, completionOffset, GroovyCompletionProposalComputer.class);
        
    }
    
    protected ICompilationUnit create(String contents) throws Exception {
        return create(contents, "GroovyClass");
    }
    protected ICompilationUnit create(String contents, String cuName) throws Exception {
        IPath projectPath;
        if (genericProjectExists()) {
            projectPath = env.getProject("Project").getFullPath();
        } else {
            projectPath = createGenericProject();
        }
        IPath src = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(src, cuName, contents);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        return unit;
    }
    
    protected void createJava(String contents, String cuName) throws Exception {
        IPath projectPath;
        if (genericProjectExists()) {
            projectPath = env.getProject("Project").getFullPath();
        } else {
            projectPath = createGenericProject();
        }
        IPath src = projectPath.append("src");
        env.addClass(src, cuName, contents);
    }


    protected String printProposals(ICompletionProposal[] proposals) {
        StringBuilder sb = new StringBuilder();
        sb.append("Incorrect proposals:\n");
        for (ICompletionProposal proposal : proposals) {
            sb.append(proposal.getDisplayString() + "\n");
        }
        return sb.toString();
    }

    public void performDummySearch(IJavaElement element) throws Exception{
        new SearchEngine().searchAllTypeNames(
            null,
            SearchPattern.R_EXACT_MATCH,
            "XXXXXXXXX".toCharArray(), // make sure we search a concrete name. This is faster according to Kent
            SearchPattern.R_EXACT_MATCH,
            IJavaSearchConstants.CLASS,
            SearchEngine.createJavaSearchScope(new IJavaElement[]{element}),
            new Requestor(),
            IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
            null);
    }
    private static class Requestor extends TypeNameRequestor {
    }

}
