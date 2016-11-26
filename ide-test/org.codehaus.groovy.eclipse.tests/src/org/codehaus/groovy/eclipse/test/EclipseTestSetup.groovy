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
package org.codehaus.groovy.eclipse.test

import junit.extensions.TestSetup
import junit.framework.Test
import org.codehaus.groovy.eclipse.GroovyPlugin
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.groovy.tests.builder.SimpleProgressMonitor
import org.eclipse.jdt.core.tests.util.Util
import org.eclipse.jdt.internal.core.CompilationUnit
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor

/**
 * Provides a fresh Groovy project to each the supplied test.
 * <p>
 * NOTE: removeSources() can be called in tearDown() to prep for next test case.
 */
class EclipseTestSetup extends TestSetup {

    private static Hashtable<String, String> savedPreferences
    private static TestProject testProject

    EclipseTestSetup(Test test) {
        super(test)
    }

    protected void setUp() {
        savedPreferences = JavaCore.options
        testProject = new TestProject()
        testProject.autoBuilding = false
    }

    protected void tearDown() {
        JavaCore.options = savedPreferences
        testProject.dispose()
        testProject = null
    }

    static void setJavaPreference(String name, String value) {
        def opts = JavaCore.options
        opts.put(name, value)
        JavaCore.options = opts
    }

    static GroovyCompilationUnit addGroovySource(CharSequence contents, String name = 'Pogo', String pack = '') {
        testProject.createGroovyTypeAndPackage(pack, name + '.groovy', contents.toString())
    }

    static CompilationUnit addJavaSource(CharSequence contents, String name = 'Pojo', String pack = '') {
        testProject.createJavaTypeAndPackage(pack, name + '.java', contents.toString())
    }

    static IFile addPlainText(CharSequence contents, String name) {
        testProject.createFile(name, contents)
    }

    static void addJUnit4() {
        addClasspathContainer(new Path('org.eclipse.jdt.junit.JUNIT_CONTAINER/4'))
    }

    static void addClasspathContainer(IPath path) {
        testProject.addEntry(testProject.project, JavaCore.newContainerEntry(path))
    }

    static void addNature(String natureId) {
        testProject.addNature(natureId);
    }

    static void buildProject() {
        testProject.fullBuild();
    }

    static JavaEditor openInEditor(ICompilationUnit unit) {
        unit.makeConsistent(null)
        try {
            EditorUtility.openInEditor(unit)
        } finally {
            SynchronizationUtils.runEventQueue()
        }
    }

    static void waitForIndex() {
        testProject.waitForIndexer()
    }

    static void withProject(Closure<IProject> closure) {
        closure(testProject.project)
    }

    static void removeSources() {
        GroovyPlugin.default.activeWorkbenchWindow.activePage.closeAllEditors(false)

        testProject.deleteWorkingCopies()
        IFolder sourceFolder = testProject.sourceFolder.resource
        sourceFolder.members().each { IResource item -> Util.delete(item) }

        SimpleProgressMonitor spm = new SimpleProgressMonitor("$testProject.project.name clean");
        testProject.project.build(IncrementalProjectBuilder.CLEAN_BUILD, spm)
        spm.waitForCompletion()
    }
}
