/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.test

import static org.eclipse.jdt.ui.PreferenceConstants.EDITOR_MARK_OCCURRENCES

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import org.codehaus.groovy.eclipse.GroovyPlugin
import org.codehaus.groovy.eclipse.core.compiler.GroovySnippetCompiler
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.codehaus.jdt.groovy.model.GroovyNature
import org.eclipse.core.internal.utils.UniversalUniqueIdentifier
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.resources.ProjectScope
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Platform
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IPackageFragment
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.tests.util.Util
import org.eclipse.jdt.internal.core.CompilationUnit
import org.eclipse.jdt.internal.ui.JavaPlugin
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.TestName

abstract class GroovyEclipseTestSuite {

    private static TestProject testProject

    @BeforeClass
    static final void setUpTestSuite() {
        testProject = new TestProject(autoBuilding: false)
        def projectScope = new ProjectScope(testProject.project)
        projectScope.getNode(Platform.PI_RUNTIME).put(Platform.PREF_LINE_SEPARATOR, '\n')
        projectScope.getNode('org.eclipse.jdt.launching').put('org.eclipse.jdt.launching.PREF_COMPILER_COMPLIANCE_DOES_NOT_MATCH_JRE', JavaCore.IGNORE)
    }

    @AfterClass
    static final void tearDownTestSuite() {
        def defaults = {
            storePreferences.@properties.keys().each { k ->
                if (!isDefault(k)) {
                    println "Resetting '$k' to its default"
                    setToDefault(k)
                }
            }
        }

        GroovyPlugin.default.preferenceStore.with(defaults)
        JavaPlugin.default.preferenceStore.with(defaults)
        JavaCore.options = JavaCore.defaultOptions

        testProject?.dispose()
        testProject = null
    }

    @Rule
    public final TestName test = new TestName()

    @Before
    final void setUpTestCase() {
        println '----------------------------------------'
        println 'Starting: ' + test.methodName
    }

    @After
    final void tearDownTestCase() {
        GroovyPlugin.default.activeWorkbenchWindow.activePage.closeAllEditors(false)
        testProject.deleteWorkingCopies()

        testProject.project.build(IncrementalProjectBuilder.CLEAN_BUILD, null)

        // for some reason the source folder is not always present in getPackageFragmentRoots():
        for (pfr in (testProject.javaProject.packageFragmentRoots + testProject.sourceFolder)) {
            if (pfr.elementName == testProject.sourceFolder.elementName) {
                pfr.resource.members().each(Util.&delete)
            } else if (!pfr.isExternal()) {
                Util.delete(pfr.resource)
            }
        }

        String sourceFolderPath = "/${testProject.project.name}/src"

        Collection<IClasspathEntry> entries = testProject.javaProject.rawClasspath.findAll {
            if (it.entryKind == IClasspathEntry.CPE_LIBRARY) {
                return false
            }
            if (it.entryKind == IClasspathEntry.CPE_PROJECT) {
                return false
            }
            if (it.entryKind == IClasspathEntry.CPE_SOURCE) {
                return it.path.toString() == sourceFolderPath
            }
            return true
        }

        testProject.javaProject.setRawClasspath(entries as IClasspathEntry[], null)
    }

    //--------------------------------------------------------------------------

    protected static final String nextUnitName() {
        "TestUnit_${new UniversalUniqueIdentifier()}"
    }

    protected final void setJavaPreference(String key, String val) {
        if (key.startsWith(JavaCore.PLUGIN_ID)) {
            def options = JavaCore.options
            options.put(key, val)
            JavaCore.options = options
        } else if (key.startsWith(JavaPlugin.pluginId) || JavaPlugin.default.preferenceStore.contains(key)) {
            def prefs = JavaPlugin.default.preferenceStore
            prefs.setValue(key, val)
        } else {
            System.err.println("Unexpected preference: $key")
        }
    }

    protected GroovyCompilationUnit addGroovySource(CharSequence contents, String name = 'Pogo', String pack = '',
            IPackageFragmentRoot root = getPackageFragmentRoot()) {
        testProject.createGroovyType(root.createPackageFragment(pack, true, null), name + '.groovy', contents.toString())
    }

    protected CompilationUnit addJavaSource(CharSequence contents, String name = 'Pojo', String pack = '',
            IPackageFragmentRoot root = getPackageFragmentRoot()) {
        testProject.createJavaType(root.createPackageFragment(pack, true, null), name + '.java', contents.toString())
    }

    protected final IFile addPlainText(CharSequence contents, String name) {
        testProject.createFile(name, contents)
    }

    protected final IPackageFragmentRoot addSourceFolder(String name, IPath... exclusionPatterns) {
        testProject.createSourceFolder(name, null, exclusionPatterns)
    }

    protected final void addProjectReference(IJavaProject project) {
        testProject.addProjectReference(project)
    }

    protected final void addClasspathContainer(IPath path) {
        testProject.addClasspathEntry(JavaCore.newContainerEntry(path))
    }

    protected final void addJUnit(int n) { assert n in 3..5
        addClasspathContainer(new Path("org.eclipse.jdt.junit.JUNIT_CONTAINER/$n"))
    }

    protected final void addNature(String... natures) {
        natures.each(testProject.&addNature)
    }

    protected final void removeNature(String... natures) {
        natures.each(testProject.&removeNature)
    }

    protected final boolean hasGroovyNature() {
        testProject.project.hasNature(GroovyNature.GROOVY_NATURE)
    }

    protected final boolean hasGroovyLibraries() {
        testProject.hasGroovyLibraries()
    }

    protected final GroovySnippetCompiler getGroovySnippetCompiler() {
        new GroovySnippetCompiler(testProject.javaProject)
    }

    protected final IPackageFragmentRoot getPackageFragmentRoot() {
        testProject.sourceFolder
    }

    protected final IPackageFragment getPackageFragment(String name) {
        testProject.createPackage(name)
    }

    protected final JavaEditor openInEditor(ICompilationUnit unit) {
        setJavaPreference(EDITOR_MARK_OCCURRENCES, 'false')
        try {
            EditorUtility.openInEditor(unit)
        } finally {
            SynchronizationUtils.runEventQueue()
        }
    }

    protected final void buildProject() {
        testProject.fullBuild()
    }

    protected final void waitForIndex() {
        testProject.waitForIndexer()
    }

    protected final void withProject(@ClosureParams(value=SimpleType, options='org.eclipse.core.resources.IProject') Closure closure) {
        closure(testProject.project)
    }
}
