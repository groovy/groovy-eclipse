/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.groovy.tests.compiler;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.groovy.tests.builder.ProjectUtils;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector.FileKind;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.CompilationUnit;

/**
 * Tests that the script folder handling works
 * @author Andrew Eisenberg
 * @created Oct 7, 2010
 */
public class ScriptFolderTests extends BuilderTests {
    
    class MockScriptFolderSelector extends ScriptFolderSelector {

        // override to make accessible here
        protected MockScriptFolderSelector(String preferences,
                boolean isDisabled) {
            super(toListOfString(preferences), isDisabled);
        }
    }
    static List<String> toListOfString(String preferences) {
        String[] splits = preferences.split(",");
        return Arrays.asList(splits);
    }
    
    public ScriptFolderTests(String name) {
        super(name);
    }

    public static Test suite() {
        return buildTestSuite(ScriptFolderTests.class);
    }

    private boolean origEnabled;
    private String origPatterns;
    
    @Override
    protected void setUp() throws Exception {
        try {
            super.setUp();
        } finally {
            origEnabled = Activator.getDefault().getBooleanPreference(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, false);
            origPatterns = Activator.getDefault().getStringPreference(Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER);
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            Activator.getDefault().setPreference(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, String.valueOf(origEnabled));
            Activator.getDefault().setPreference(Activator.GROOVY_SCRIPT_FILTERS, origPatterns);
        }
    }
    
    public void testScriptFolderDefaultSettings() throws Exception {
        MockScriptFolderSelector selector = new MockScriptFolderSelector(Activator.DEFAULT_GROOVY_SCRIPT_FILTER, true);
        assertScript("scripts/f/g/Foo.groovy", selector);
        assertScript("src/main/resources/f/g/Foo.groovy", selector);
        assertScript("src/test/resources/f/g/Foo.groovy", selector);

        assertSource("h/scripts/Foo.groovy", selector);
        assertSource("h/src/main/resources/Foo.groovy", selector);
        assertSource("h/src/test/resources/Foo.groovy", selector);
        
        assertSource("scripts/Foo.java", selector);
        assertSource("src/main/resources/Foo.java", selector);
        assertSource("src/test/resources/Foo.java", selector);
    }
    
    public void testScriptFolderDefaultSettingsNoCopy() throws Exception {
        MockScriptFolderSelector selector = new MockScriptFolderSelector(Activator.DEFAULT_GROOVY_SCRIPT_FILTER.replaceAll(",y", ",n"), true);
        assertScriptNoCopy("scripts/f/g/Foo.groovy", selector);
        assertScriptNoCopy("src/main/resources/f/g/Foo.groovy", selector);
        assertScriptNoCopy("src/test/resources/f/g/Foo.groovy", selector);
        
        assertSource("h/scripts/Foo.groovy", selector);
        assertSource("h/src/main/resources/Foo.groovy", selector);
        assertSource("h/src/test/resources/Foo.groovy", selector);
        
        assertSource("scripts/Foo.java", selector);
        assertSource("src/main/resources/Foo.java", selector);
        assertSource("src/test/resources/Foo.java", selector);
    }

    public void testScriptFolderDisabled() throws Exception {
        MockScriptFolderSelector selector = new MockScriptFolderSelector(Activator.DEFAULT_GROOVY_SCRIPT_FILTER, false);
        assertSource("scripts/f/g/Foo.groovy", selector);
        assertSource("src/main/resources/f/g/Foo.groovy", selector);
        assertSource("src/test/resources/f/g/Foo.groovy", selector);
        
        assertSource("h/scripts/Foo.groovy", selector);
        assertSource("h/src/main/resources/Foo.groovy", selector);
        assertSource("h/src/test/resources/Foo.groovy", selector);
        
        assertSource("scripts/Foo.java", selector);
        assertSource("src/main/resources/Foo.java", selector);
        assertSource("src/test/resources/Foo.java", selector);
    }
    
    public void testScriptFolderCustomSettings() throws Exception {
        MockScriptFolderSelector selector = new MockScriptFolderSelector("scri/**/*.groovy,y,scroo/**/*.groovy,n", true);
        assertScript("scri/f/g/Foo.groovy", selector);
        assertScriptNoCopy("scroo/main/resources/f/g/Foo.groovy", selector);
        assertSource("src/test/resources/Foo.java", selector);
    }
    
    // mostly ensure that nothing horrific happens
    public void testScriptFolderInvalidSettings() throws Exception {
        MockScriptFolderSelector selector = new MockScriptFolderSelector("scri/**/*.groovy,scroo/**/*.groovy,n", true);
        assertScriptNoCopy("scri/f/g/Foo.groovy", selector);
        assertSource("scroo/main/resources/f/g/Foo.groovy", selector);
        assertSource("src/test/resources/Foo.java", selector);
    }
    
    private void assertScript(String toCheck, MockScriptFolderSelector selector) {
        assertEquals(toCheck + " should be a script", FileKind.SCRIPT, selector.getFileKind(toCheck.toCharArray()));
    }
    private void assertScriptNoCopy(String toCheck, MockScriptFolderSelector selector) {
        assertEquals(toCheck + " should be a script", FileKind.SCRIPT_NO_COPY, selector.getFileKind(toCheck.toCharArray()));
    }
    private void assertSource(String toCheck, MockScriptFolderSelector selector) {
        assertEquals(toCheck + " should be a script", FileKind.SOURCE, selector.getFileKind(toCheck.toCharArray()));
    }
    
    
    // now that we have tested the settings, let's test that scripts are handled correctly
    public void testScriptInProjectNotCompiled() throws Exception {
        Activator.getDefault().setPreference(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, "true");
        Activator.getDefault().setPreference(Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER);
        createScriptInGroovyProject("Script", "def x", true);
        assertNoExists("Project/bin/Script.class");
        assertExists("Project/bin/Script.groovy");
    }
    public void testScriptInProjectNoCopy() throws Exception {
        Activator.getDefault().setPreference(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, "true");
        Activator.getDefault().setPreference(Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER.replaceAll(",y", ",n"));
        createScriptInGroovyProject("Script", "def x", true);
        assertNoExists("Project/bin/Script.class");
        assertNoExists("Project/bin/Script.groovy");
    }
    public void testScriptInProjectDisabled() throws Exception {
        Activator.getDefault().setPreference(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, "false");
        Activator.getDefault().setPreference(Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER);
        createScriptInGroovyProject("Script", "def x", true);
        assertExists("Project/bin/Script.class");
        assertNoExists("Project/bin/Script.groovy");
    }
    public void testSourceInProjectCompiled() throws Exception {
        Activator.getDefault().setPreference(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, "true");
        Activator.getDefault().setPreference(Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER);
        createScriptInGroovyProject("Script", "class Script { }", false);  // creates a java file
        assertExists("Project/bin/Script.class");
        assertNoExists("Project/bin/Script.groovy");
        assertNoExists("Project/bin/Script.java");
    }
    
    // This is the big test.
    public void testComplexScriptFolderProject() throws Exception {
        Activator.getDefault().setPreference(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, "true");
        Activator.getDefault().setPreference(Activator.GROOVY_SCRIPT_FILTERS, "src1/**/*.groovy,y,src2/**/*.groovy,y,src3/**/*.groovy,y");
        ProjectUtils.createPredefinedProject("ScriptFoldersProject");
        env.fullBuild();
        
        // project root is a source folder, but it is not a script folder
        assertExists("ScriptFoldersProject/bin/NotAScript1.class");
        assertExists("ScriptFoldersProject/bin/p/NotAScript1.class");
        assertNoExists("ScriptFoldersProject/bin/NotAScript1.groovy");
        assertNoExists("ScriptFoldersProject/bin/p/NotAScript1.groovy");
        
        // src1 is a script folder and compiles to default out folder
        assertExists("ScriptFoldersProject/bin/Script1.groovy");
        assertExists("ScriptFoldersProject/bin/p/Script1.groovy");
        assertNoExists("ScriptFoldersProject/bin/Script1.class");
        assertNoExists("ScriptFoldersProject/bin/p/Script1.class");
        
        // src2 is a script folder and compiles to bin2
        assertExists("ScriptFoldersProject/bin2/Script2.groovy");
        assertExists("ScriptFoldersProject/bin2/p/Script2.groovy");
        assertNoExists("ScriptFoldersProject/bin2/Script2.class");
        assertNoExists("ScriptFoldersProject/bin2/p/Script2.class");
        
        // src3 is a script folder, and is its own out folder
        assertExistsNotDerived("ScriptFoldersProject/src3/Script3.groovy");
        assertExistsNotDerived("ScriptFoldersProject/src3/p/Script3.groovy");
        assertNoExists("ScriptFoldersProject/src3/Script3.class");
        assertNoExists("ScriptFoldersProject/src3/p/Script3.class");
    }
    
    // as above, but don't copy
    public void testComplexScriptFolderProjectNoCopy() throws Exception {
        Activator.getDefault().setPreference(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, "true");
        Activator.getDefault().setPreference(Activator.GROOVY_SCRIPT_FILTERS, "src1/**/*.groovy,n,src2/**/*.groovy,n,src3/**/*.groovy,n");
        ProjectUtils.createPredefinedProject("ScriptFoldersProject");
        env.fullBuild();
        
        // project root is a source folder, but it is not a script folder
        assertExists("ScriptFoldersProject/bin/NotAScript1.class");
        assertExists("ScriptFoldersProject/bin/p/NotAScript1.class");
        assertNoExists("ScriptFoldersProject/bin/NotAScript1.groovy");
        assertNoExists("ScriptFoldersProject/bin/p/NotAScript1.groovy");
        
        // src1 is a script folder and compiles to default out folder
        assertNoExists("ScriptFoldersProject/bin/Script1.groovy");
        assertNoExists("ScriptFoldersProject/bin/p/Script1.groovy");
        assertNoExists("ScriptFoldersProject/bin/Script1.class");
        assertNoExists("ScriptFoldersProject/bin/p/Script1.class");
        
        // src2 is a script folder and compiles to bin2
        assertNoExists("ScriptFoldersProject/bin2/Script2.groovy");
        assertNoExists("ScriptFoldersProject/bin2/p/Script2.groovy");
        assertNoExists("ScriptFoldersProject/bin2/Script2.class");
        assertNoExists("ScriptFoldersProject/bin2/p/Script2.class");
        
        // src3 is a script folder, and is its own out folder
        assertExistsNotDerived("ScriptFoldersProject/src3/Script3.groovy");
        assertExistsNotDerived("ScriptFoldersProject/src3/p/Script3.groovy");
        assertNoExists("ScriptFoldersProject/src3/Script3.class");
        assertNoExists("ScriptFoldersProject/src3/p/Script3.class");
    }
    
    
    
    protected CompilationUnit createScriptInGroovyProject(String name, String contents, boolean isGroovy) throws Exception {
        IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
        env.addGroovyNature("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
        env.addPackageFragmentRoot(projectPath, "scripts"); //$NON-NLS-1$
        env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
        IProject project = env.getProject("Project");
        IJavaProject javaProject = JavaCore.create(project);
        javaProject.setOption(CompilerOptions.OPTION_Compliance, "1.5");
        javaProject.setOption(CompilerOptions.OPTION_Source, "1.5");
        javaProject.setOption(CompilerOptions.OPTION_TargetPlatform, "1.5");
        IPath path;
        if (isGroovy) { 
            path = env.addGroovyClass(project.getFolder("scripts").getFullPath(), name, contents);
        } else {
            path = env.addClass(project.getFolder("scripts").getFullPath(), name, contents);
        }
        fullBuild(projectPath);
        return (CompilationUnit) JavaCore.createCompilationUnitFrom(env.getWorkspace().getRoot().getFile(path));
    }
    
    private void assertExists(String projectRelativePath) {
        IWorkspaceRoot root = env.getWorkspace().getRoot();
        IFile file = root.getFile(new Path(projectRelativePath));
        assertTrue("File should exist: " + file, file.exists());
        assertTrue("File should be derived: " + file, file.isDerived());
    }
    
    private void assertExistsNotDerived(String projectRelativePath) {
        IWorkspaceRoot root = env.getWorkspace().getRoot();
        IFile file = root.getFile(new Path(projectRelativePath));
        assertTrue("File should exist: " + file, file.exists());
        assertFalse("File should not be derived: " + file, file.isDerived());
    }
    
    private void assertNoExists(String projectRelativePath) {
        IWorkspaceRoot root = env.getWorkspace().getRoot();
        IFile file = root.getFile(new Path(projectRelativePath));
        assertFalse("File should not exist: " + file, file.exists());
    }
    

}