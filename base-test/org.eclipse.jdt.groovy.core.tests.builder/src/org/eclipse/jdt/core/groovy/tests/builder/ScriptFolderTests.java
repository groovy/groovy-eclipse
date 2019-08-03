/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.groovy.tests.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.groovy.tests.MockScriptFolderSelector;
import org.eclipse.jdt.core.groovy.tests.SimpleProgressMonitor;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector.FileKind;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.junit.After;
import org.junit.Test;

/**
 * Tests that the script folder handling works.
 */
public final class ScriptFolderTests extends BuilderTestSuite {

    @After
    public void tearDown() throws Exception {
        Activator.getInstancePreferences().putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, Activator.DEFAULT_SCRIPT_FILTERS_ENABLED);
        Activator.getInstancePreferences().put(Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER);
    }

    @Test
    public void testScriptFolderDefaultSettings() throws Exception {
        ScriptFolderSelector selector = new MockScriptFolderSelector(Activator.DEFAULT_GROOVY_SCRIPT_FILTER, true);
        assertScript("some.dsld", selector);
        assertScriptNoCopy("build.gradle", selector);

        assertSource("scripts/Foo.java", selector);
        assertSource("scripts/Foo.groovy", selector);
        assertSource("src/main/resources/Foo.java", selector);
        assertSource("src/main/resources/Foo.groovy", selector);
        assertSource("src/test/resources/Foo.java", selector);
        assertSource("src/test/resources/Foo.groovy", selector);
        assertSource("src/main/resources/f/g/Foo.groovy", selector);
        assertSource("src/test/resources/f/g/Foo.groovy", selector);

        assertSource("h/scripts/Foo.groovy", selector);
        assertSource("h/src/main/resources/Foo.groovy", selector);
        assertSource("h/src/test/resources/Foo.groovy", selector);
    }

    @Test
    public void testScriptFolderDisabled() throws Exception {
        ScriptFolderSelector selector = new MockScriptFolderSelector(Activator.DEFAULT_GROOVY_SCRIPT_FILTER, false);
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

    @Test
    public void testScriptFolderCustomSettings() throws Exception {
        ScriptFolderSelector selector = new MockScriptFolderSelector("scri/**/*.groovy,y,scroo/**/*.groovy,n", true);
        assertScript("scri/f/g/Foo.groovy", selector);
        assertScriptNoCopy("scroo/main/resources/f/g/Foo.groovy", selector);
        assertSource("src/test/resources/Foo.java", selector);
    }

    @Test // mostly ensure that nothing horrific happens
    public void testScriptFolderInvalidSettings() throws Exception {
        ScriptFolderSelector selector = new MockScriptFolderSelector("scri/**/*.groovy,scroo/**/*.groovy,n", true);
        assertScriptNoCopy("scri/f/g/Foo.groovy", selector);
        assertSource("scroo/main/resources/f/g/Foo.groovy", selector);
        assertSource("src/test/resources/Foo.java", selector);
    }

    @Test // now that we have tested the settings, let's test that scripts are handled correctly
    public void testScriptInProjectNotCompiled() throws Exception {
        Activator.getInstancePreferences().putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, true);
        Activator.getInstancePreferences().put(Activator.GROOVY_SCRIPT_FILTERS, "scripts/*.groovy,y");
        createScriptInGroovyProject("Script", "def x", true);
        assertNotExists("Project/bin/Script.class");
        assertExists("Project/bin/Script.groovy");
    }

    @Test
    public void testScriptInProjectNotCopied() throws Exception {
        Activator.getInstancePreferences().putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, true);
        Activator.getInstancePreferences().put(Activator.GROOVY_SCRIPT_FILTERS, "scripts/*.groovy,n");
        createScriptInGroovyProject("Script", "def x", true);
        assertNotExists("Project/bin/Script.class");
        assertNotExists("Project/bin/Script.groovy");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/901
    public void testScriptInProjectNoMarkers() throws Exception {
        Activator.getInstancePreferences().putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, true);
        Activator.getInstancePreferences().put(Activator.GROOVY_SCRIPT_FILTERS, "scripts/*.groovy,y");
        createScriptInGroovyProject("Script", "import foo.bar.Baz;", true); // error in editor only
        expectingNoProblemsFor(new Path("Project/scripts/Script.groovy"));
        assertNotExists("Project/bin/Script.class");
        assertExists("Project/bin/Script.groovy");
    }

    @Test
    public void testScriptInProjectDisabled() throws Exception {
        Activator.getInstancePreferences().putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, false);
        Activator.getInstancePreferences().put(Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER);
        createScriptInGroovyProject("Script", "def x", true);
        assertExists("Project/bin/Script.class");
        assertNotExists("Project/bin/Script.groovy");
    }

    @Test
    public void testSourceInProjectCompiled() throws Exception {
        Activator.getInstancePreferences().putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, true);
        Activator.getInstancePreferences().put(Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER);
        createScriptInGroovyProject("Script", "class Script {}", false);  // creates a java file
        assertExists("Project/bin/Script.class");
        assertNotExists("Project/bin/Script.groovy");
        assertNotExists("Project/bin/Script.java");
    }

    @Test
    public void testComplexScriptFolderProject() throws Exception {
        Activator.getInstancePreferences().putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, true);
        Activator.getInstancePreferences().put(Activator.GROOVY_SCRIPT_FILTERS, "src1/**/*.groovy,y,src2/**/*.groovy,y,src3/**/*.groovy,y");
        createPredefinedProject("ScriptFoldersProject");
        env.cleanBuild();
        env.fullBuild();

        // project root is a source folder, but it is not a script folder
        assertExists("ScriptFoldersProject/bin/NotAScript1.class");
        assertExists("ScriptFoldersProject/bin/p/NotAScript1.class");
        assertNotExists("ScriptFoldersProject/bin/NotAScript1.groovy");
        assertNotExists("ScriptFoldersProject/bin/p/NotAScript1.groovy");

        // src1 is a script folder and compiles to default out folder
        assertExists("ScriptFoldersProject/bin/Script1.groovy");
        assertExists("ScriptFoldersProject/bin/p/Script1.groovy");
        assertNotExists("ScriptFoldersProject/bin/Script1.class");
        assertNotExists("ScriptFoldersProject/bin/p/Script1.class");

        // src2 is a script folder and compiles to bin2
        assertExists("ScriptFoldersProject/bin2/Script2.groovy");
        assertExists("ScriptFoldersProject/bin2/p/Script2.groovy");
        assertNotExists("ScriptFoldersProject/bin2/Script2.class");
        assertNotExists("ScriptFoldersProject/bin2/p/Script2.class");

        // src3 is a script folder, and is its own out folder
        assertExistsNotDerived("ScriptFoldersProject/src3/Script3.groovy");
        assertExistsNotDerived("ScriptFoldersProject/src3/p/Script3.groovy");
        assertNotExists("ScriptFoldersProject/src3/Script3.class");
        assertNotExists("ScriptFoldersProject/src3/p/Script3.class");
    }

    @Test // as above, but don't copy
    public void testComplexScriptFolderProjectNoCopy() throws Exception {
        Activator.getInstancePreferences().putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, true);
        Activator.getInstancePreferences().put(Activator.GROOVY_SCRIPT_FILTERS, "src1/**/*.groovy,n,src2/**/*.groovy,n,src3/**/*.groovy,n");
        createPredefinedProject("ScriptFoldersProject");
        env.fullBuild();

        // project root is a source folder, but it is not a script folder
        assertExists("ScriptFoldersProject/bin/NotAScript1.class");
        assertExists("ScriptFoldersProject/bin/p/NotAScript1.class");
        assertNotExists("ScriptFoldersProject/bin/NotAScript1.groovy");
        assertNotExists("ScriptFoldersProject/bin/p/NotAScript1.groovy");

        // src1 is a script folder and compiles to default out folder
        assertNotExists("ScriptFoldersProject/bin/Script1.groovy");
        assertNotExists("ScriptFoldersProject/bin/p/Script1.groovy");
        assertNotExists("ScriptFoldersProject/bin/Script1.class");
        assertNotExists("ScriptFoldersProject/bin/p/Script1.class");

        // src2 is a script folder and compiles to bin2
        assertNotExists("ScriptFoldersProject/bin2/Script2.groovy");
        assertNotExists("ScriptFoldersProject/bin2/p/Script2.groovy");
        assertNotExists("ScriptFoldersProject/bin2/Script2.class");
        assertNotExists("ScriptFoldersProject/bin2/p/Script2.class");

        // src3 is a script folder, and is its own out folder
        assertExistsNotDerived("ScriptFoldersProject/src3/Script3.groovy");
        assertExistsNotDerived("ScriptFoldersProject/src3/p/Script3.groovy");
        assertNotExists("ScriptFoldersProject/src3/Script3.class");
        assertNotExists("ScriptFoldersProject/src3/p/Script3.class");
    }

    @Test
    public void testComplexScriptFolderProjectProjectSettings() throws Exception {
        IProject project = createPredefinedProject("ScriptFoldersProject");
        createPredefinedProject("ScriptFoldersProject2");

        IEclipsePreferences preferences = Activator.getProjectPreferences(project);
        preferences.putBoolean(Activator.USING_PROJECT_PROPERTIES, true);
        preferences.putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, true);
        preferences.put(Activator.GROOVY_SCRIPT_FILTERS, "src1/**/*.groovy,y,src2/**/*.groovy,y,src3/**/*.groovy,y");
        env.fullBuild();

        // project root is a source folder, but it is not a script folder
        assertExists("ScriptFoldersProject/bin/NotAScript1.class");
        assertExists("ScriptFoldersProject/bin/p/NotAScript1.class");
        assertNotExists("ScriptFoldersProject/bin/NotAScript1.groovy");
        assertNotExists("ScriptFoldersProject/bin/p/NotAScript1.groovy");

        // src1 is a script folder and compiles to default out folder
        assertExists("ScriptFoldersProject/bin/Script1.groovy");
        assertExists("ScriptFoldersProject/bin/p/Script1.groovy");
        assertNotExists("ScriptFoldersProject/bin/Script1.class");
        assertNotExists("ScriptFoldersProject/bin/p/Script1.class");

        // src2 is a script folder and compiles to bin2
        assertExists("ScriptFoldersProject/bin2/Script2.groovy");
        assertExists("ScriptFoldersProject/bin2/p/Script2.groovy");
        assertNotExists("ScriptFoldersProject/bin2/Script2.class");
        assertNotExists("ScriptFoldersProject/bin2/p/Script2.class");

        // src3 is a script folder, and is its own out folder
        assertExistsNotDerived("ScriptFoldersProject/src3/Script3.groovy");
        assertExistsNotDerived("ScriptFoldersProject/src3/p/Script3.groovy");
        assertNotExists("ScriptFoldersProject/src3/Script3.class");
        assertNotExists("ScriptFoldersProject/src3/p/Script3.class");

        // now check another project
        assertExists("ScriptFoldersProject2/bin/NotAScript1.class");
        assertExists("ScriptFoldersProject2/bin/p/NotAScript1.class");
        assertNotExists("ScriptFoldersProject2/bin/NotAScript1.groovy");
        assertNotExists("ScriptFoldersProject2/bin/p/NotAScript1.groovy");

        assertNotExists("ScriptFoldersProject2/bin/Script1.groovy");
        assertNotExists("ScriptFoldersProject2/bin/p/Script1.groovy");
        assertExists("ScriptFoldersProject2/bin/Script1.class");
        assertExists("ScriptFoldersProject2/bin/p/Script1.class");

        assertNotExists("ScriptFoldersProject2/bin2/Script2.groovy");
        assertNotExists("ScriptFoldersProject2/bin2/p/Script2.groovy");
        assertExists("ScriptFoldersProject2/bin2/Script2.class");
        assertExists("ScriptFoldersProject2/bin2/p/Script2.class");

        assertExistsNotDerived("ScriptFoldersProject2/src3/Script3.groovy");
        assertExistsNotDerived("ScriptFoldersProject2/src3/p/Script3.groovy");
        assertExists("ScriptFoldersProject2/src3/Script3.class");
        assertExists("ScriptFoldersProject2/src3/p/Script3.class");

        // now disable
        preferences.put(Activator.USING_PROJECT_PROPERTIES, "false");
        env.fullBuild();
        assertExists("ScriptFoldersProject/bin/NotAScript1.class");
        assertExists("ScriptFoldersProject/bin/p/NotAScript1.class");
        assertNotExists("ScriptFoldersProject/bin/NotAScript1.groovy");
        assertNotExists("ScriptFoldersProject/bin/p/NotAScript1.groovy");

        assertNotExists("ScriptFoldersProject/bin/Script1.groovy");
        assertNotExists("ScriptFoldersProject/bin/p/Script1.groovy");
        assertExists("ScriptFoldersProject/bin/Script1.class");
        assertExists("ScriptFoldersProject/bin/p/Script1.class");

        assertNotExists("ScriptFoldersProject/bin2/Script2.groovy");
        assertNotExists("ScriptFoldersProject/bin2/p/Script2.groovy");
        assertExists("ScriptFoldersProject/bin2/Script2.class");
        assertExists("ScriptFoldersProject/bin2/p/Script2.class");

        assertExistsNotDerived("ScriptFoldersProject/src3/Script3.groovy");
        assertExistsNotDerived("ScriptFoldersProject/src3/p/Script3.groovy");
        assertExists("ScriptFoldersProject/src3/Script3.class");
        assertExists("ScriptFoldersProject/src3/p/Script3.class");

        // other project should be the same
        assertExists("ScriptFoldersProject2/bin/NotAScript1.class");
        assertExists("ScriptFoldersProject2/bin/p/NotAScript1.class");
        assertNotExists("ScriptFoldersProject2/bin/NotAScript1.groovy");
        assertNotExists("ScriptFoldersProject2/bin/p/NotAScript1.groovy");

        assertNotExists("ScriptFoldersProject2/bin/Script1.groovy");
        assertNotExists("ScriptFoldersProject2/bin/p/Script1.groovy");
        assertExists("ScriptFoldersProject2/bin/Script1.class");
        assertExists("ScriptFoldersProject2/bin/p/Script1.class");

        assertNotExists("ScriptFoldersProject2/bin2/Script2.groovy");
        assertNotExists("ScriptFoldersProject2/bin2/p/Script2.groovy");
        assertExists("ScriptFoldersProject2/bin2/Script2.class");
        assertExists("ScriptFoldersProject2/bin2/p/Script2.class");

        assertExistsNotDerived("ScriptFoldersProject2/src3/Script3.groovy");
        assertExistsNotDerived("ScriptFoldersProject2/src3/p/Script3.groovy");
        assertExists("ScriptFoldersProject2/src3/Script3.class");
        assertExists("ScriptFoldersProject2/src3/p/Script3.class");

        // now enable the workspace settings, add back project settings, but disable filters on the project
        Activator.getInstancePreferences().putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, true);
        Activator.getInstancePreferences().put(Activator.GROOVY_SCRIPT_FILTERS, "src1/**/*.groovy,y,src2/**/*.groovy,y,src3/**/*.groovy,y");
        preferences.putBoolean(Activator.USING_PROJECT_PROPERTIES, true);
        preferences.putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, false);
        env.fullBuild();

        assertExists("ScriptFoldersProject/bin/NotAScript1.class");
        assertExists("ScriptFoldersProject/bin/p/NotAScript1.class");
        assertNotExists("ScriptFoldersProject/bin/NotAScript1.groovy");
        assertNotExists("ScriptFoldersProject/bin/p/NotAScript1.groovy");

        assertNotExists("ScriptFoldersProject/bin/Script1.groovy");
        assertNotExists("ScriptFoldersProject/bin/p/Script1.groovy");
        assertExists("ScriptFoldersProject/bin/Script1.class");
        assertExists("ScriptFoldersProject/bin/p/Script1.class");

        assertNotExists("ScriptFoldersProject/bin2/Script2.groovy");
        assertNotExists("ScriptFoldersProject/bin2/p/Script2.groovy");
        assertExists("ScriptFoldersProject/bin2/Script2.class");
        assertExists("ScriptFoldersProject/bin2/p/Script2.class");

        assertExistsNotDerived("ScriptFoldersProject/src3/Script3.groovy");
        assertExistsNotDerived("ScriptFoldersProject/src3/p/Script3.groovy");
        assertExists("ScriptFoldersProject/src3/Script3.class");
        assertExists("ScriptFoldersProject/src3/p/Script3.class");

        // Other project now has scripts
        // project root is a source folder, but it is not a script folder
        assertExists("ScriptFoldersProject2/bin/NotAScript1.class");
        assertExists("ScriptFoldersProject2/bin/p/NotAScript1.class");
        assertNotExists("ScriptFoldersProject2/bin/NotAScript1.groovy");
        assertNotExists("ScriptFoldersProject2/bin/p/NotAScript1.groovy");

        // src1 is a script folder and compiles to default out folder
        assertExists("ScriptFoldersProject2/bin/Script1.groovy");
        assertExists("ScriptFoldersProject2/bin/p/Script1.groovy");
        assertNotExists("ScriptFoldersProject2/bin/Script1.class");
        assertNotExists("ScriptFoldersProject2/bin/p/Script1.class");

        // src2 is a script folder and compiles to bin2
        assertExists("ScriptFoldersProject2/bin2/Script2.groovy");
        assertExists("ScriptFoldersProject2/bin2/p/Script2.groovy");
        assertNotExists("ScriptFoldersProject2/bin2/Script2.class");
        assertNotExists("ScriptFoldersProject2/bin2/p/Script2.class");

        // src3 is a script folder, and is its own out folder
        assertExistsNotDerived("ScriptFoldersProject2/src3/Script3.groovy");
        assertExistsNotDerived("ScriptFoldersProject2/src3/p/Script3.groovy");
        assertNotExists("ScriptFoldersProject2/src3/Script3.class");
        assertNotExists("ScriptFoldersProject2/src3/p/Script3.class");

        preferences.put(Activator.USING_PROJECT_PROPERTIES, "false");
    }

    //--------------------------------------------------------------------------

    private static IProject createPredefinedProject(final String projectName) throws Exception {
        // copy files in project from source workspace to target workspace
        URL bundleLocation = Platform.getBundle("org.eclipse.jdt.groovy.core.tests.builder").getEntry("/");
        String sourceWorkspacePath = new File(FileLocator.toFileURL(bundleLocation).getFile()).getAbsolutePath() + File.separator + "workspace";
        String targetWorkspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getCanonicalPath();

        // return null if source directory does not exist
        if (!copyDirectory(new File(sourceWorkspacePath, projectName), new File(targetWorkspacePath, projectName))) {
            return null;
        }

        // create project
        final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (!project.exists()) {
            SimpleProgressMonitor spm = new SimpleProgressMonitor("creation of project " + projectName);
            ResourcesPlugin.getWorkspace().run(monitor -> project.create(monitor), spm);
            spm.waitForCompletion();
        }

        // ensure open
        SimpleProgressMonitor spm = new SimpleProgressMonitor("opening project " + projectName);
        project.open(spm);
        spm.waitForCompletion();

        IJavaProject jp = JavaCore.create(project);
        if (jp == null) {
            // project was not found
            return null;
        }
        try {
            jp.setOption("org.eclipse.jdt.core.compiler.problem.missingSerialVersion", "ignore");
        } catch (NullPointerException ignore) {
        }
        return jp.getProject();
    }

    private CompilationUnit createScriptInGroovyProject(String name, String contents, boolean isGroovy) throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addGroovyJars(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");
        env.addPackageFragmentRoot(projectPath, "scripts");
        env.setOutputFolder(projectPath, "bin");
        IProject project = env.getProject("Project");
        IPath path;
        if (isGroovy) {
            path = env.addGroovyClass(project.getFolder("scripts").getFullPath(), name, contents);
        } else {
            path = env.addClass(project.getFolder("scripts").getFullPath(), name, contents);
        }
        fullBuild(projectPath);
        return (CompilationUnit) JavaCore.createCompilationUnitFrom(env.getWorkspace().getRoot().getFile(path));
    }

    private static void assertExists(String projectRelativePath) {
        IWorkspaceRoot root = env.getWorkspace().getRoot();
        IFile file = root.getFile(new Path(projectRelativePath));
        assertTrue("File should exist: " + file, file.exists());
        assertTrue("File should be derived: " + file, file.isDerived());
    }

    private static void assertExistsNotDerived(String projectRelativePath) {
        IWorkspaceRoot root = env.getWorkspace().getRoot();
        IFile file = root.getFile(new Path(projectRelativePath));
        assertTrue("File should exist: " + file, file.exists());
        assertFalse("File should not be derived: " + file, file.isDerived());
    }

    private static void assertNotExists(String projectRelativePath) {
        IWorkspaceRoot root = env.getWorkspace().getRoot();
        IFile file = root.getFile(new Path(projectRelativePath));
        assertFalse("File should not exist: " + file, file.exists());
    }

    private static void assertScript(String toCheck, ScriptFolderSelector selector) {
        assertEquals(toCheck + " should be a script", FileKind.SCRIPT, selector.getFileKind(toCheck.toCharArray()));
    }

    private static void assertScriptNoCopy(String toCheck, ScriptFolderSelector selector) {
        assertEquals(toCheck + " should be a script", FileKind.SCRIPT_NO_COPY, selector.getFileKind(toCheck.toCharArray()));
    }

    private static void assertSource(String toCheck, ScriptFolderSelector selector) {
        assertEquals(toCheck + " should be a script", FileKind.SOURCE, selector.getFileKind(toCheck.toCharArray()));
    }

    /**
     * Copy the given source directory (and all its contents) to the given target directory.
     */
    private static boolean copyDirectory(File source, File target) throws Exception {
        if (!source.exists()) {
            return false;
        }
        if (!target.exists()) {
            target.mkdirs();
        }
        File[] files = source.listFiles();
        if (files == null) return true;
        for (int i = 0; i < files.length; i++) {
            File sourceChild = files[i];
            String name =  sourceChild.getName();
            if ("CVS".equals(name)) continue;
            File targetChild = new File(target, name);
            if (sourceChild.isDirectory()) {
                copyDirectory(sourceChild, targetChild);
            } else {
                copy(sourceChild, targetChild);
            }
        }
        return true;
    }

    /**
     * Copy file from src (path to the original file) to dest (path to the destination file).
     */
    public static void copy(File src, File dest) throws Exception {
        String text = ResourceGroovyMethods.getText(src);
        if (convertToIndependantLineDelimiter(src)) {
            text = convertToIndependantLineDelimiter(text);
        }
        ResourceGroovyMethods.write(dest, text);
    }

    private static boolean convertToIndependantLineDelimiter(File file) {
        String name = file.getName();
        return (name.endsWith(".java") || name.endsWith(".aj"));
    }

    private static String convertToIndependantLineDelimiter(String source) {
        if (source.indexOf('\n') == -1 && source.indexOf('\r') == -1) return source;
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, length = source.length(); i < length; i += 1) {
            char car = source.charAt(i);
            if (car == '\r') {
                buffer.append('\n');
                if (i < length - 1 && source.charAt(i + 1) == '\n') {
                    i += 1; // skip \n after \r
                }
            } else {
                buffer.append(car);
            }
        }
        return buffer.toString();
    }
}
