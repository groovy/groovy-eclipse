/*
 * Copyright 2009-2026 the original author or authors.
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

import static org.codehaus.groovy.eclipse.GroovyPlugin.getDefault as getGroovyPlugin
import static org.eclipse.jdt.internal.ui.JavaPlugin.getDefault as getJavaPlugin
import static org.eclipse.ltk.core.refactoring.RefactoringCore.getUndoManager

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import org.codehaus.groovy.eclipse.core.compiler.GroovySnippetCompiler
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime
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
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.dialogs.ErrorDialog
import org.eclipse.jface.util.SafeRunnable
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
        ErrorDialog.@AUTOMATED_MODE = true
        SafeRunnable.setIgnoreErrors(true)
        this.testProject = new TestProject()
        def projectScope = new ProjectScope(testProject.project)
        projectScope.getNode(Platform.PI_RUNTIME).put(Platform.PREF_LINE_SEPARATOR, '\n')
        projectScope.getNode('org.eclipse.jdt.launching').put('org.eclipse.jdt.launching.PREF_COMPILER_COMPLIANCE_DOES_NOT_MATCH_JRE', JavaCore.IGNORE)
    }

    @AfterClass
    static final void tearDownTestSuite() {
        def defaults = {
            storePreferences.keys().each { k ->
                if (!isDefault(k)) {
                    println "Resetting '$k' to its default"
                    setToDefault(k)
                }
            }
        }

        JavaCore.setOptions(JavaCore.getDefaultOptions())
        groovyPlugin.preferenceStore.with(defaults)
        javaPlugin.preferenceStore.with(defaults)

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
        groovyPlugin.activeWorkbenchWindow?.activePage?.closeAllEditors(false)
        undoManager?.flush()

        testProject.discardWorkingCopies()
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

    protected final String nextUnitName() {
        "TestUnit_${new UniversalUniqueIdentifier()}"
    }

    protected final void setJavaPreference(String key, Object val) {
        if (key.startsWith(JavaCore.PLUGIN_ID)) {
            testProject.javaProject.setOption(key, val as String)
        } else if (key.startsWith(javaPlugin.pluginId) || javaPlugin.preferenceStore.contains(key) || key == 'smart_semicolon') {
            javaPlugin.preferenceStore.setValue(key, val as String)
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
        GroovyRuntime.hasGroovyClasspathContainer(testProject.javaProject)
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
        setJavaPreference(PreferenceConstants.EDITOR_MARK_OCCURRENCES, false)
        EditorUtility.openInEditor(unit).tap {
            final reconcile = new java.util.concurrent.CountDownLatch(1)
            //addReconcileListener(reconcile.&countDown as ReconcileListener)
            org.eclipse.jdt.groovy.core.util.ReflectionUtils.executePrivateMethod(CompilationUnitEditor, 'addReconcileListener',
                [org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener] as Class[], it, reconcile.&countDown as ReconcileListener)
            reconcile.await(1500, java.util.concurrent.TimeUnit.MILLISECONDS)

            SynchronizationUtils.joinBackgroundActivities()
        }
    }

    protected final void buildProject() {
        testProject.project.build(IncrementalProjectBuilder.FULL_BUILD, null)
    }

    protected final void withProject(@ClosureParams(value=SimpleType, options='org.eclipse.core.resources.IProject') Closure closure) {
        closure(testProject.project)
    }

    //--------------------------------------------------------------------------

    abstract static class ReconcileListener implements org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener {

        @Override
        void aboutToBeReconciled() {
        }

        @Override
        void reconciled(org.eclipse.jdt.core.dom.CompilationUnit ast, boolean forced, org.eclipse.core.runtime.IProgressMonitor monitor) {
            update()
        }

        abstract update() // bound to CountDownLatch
    }
}
