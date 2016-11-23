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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import junit.extensions.TestSetup
import junit.framework.Test

import org.codehaus.groovy.eclipse.GroovyPlugin
import org.codehaus.groovy.eclipse.test.SynchronizationUtils
import org.codehaus.groovy.eclipse.test.TestProject
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.groovy.tests.builder.SimpleProgressMonitor
import org.eclipse.jdt.core.tests.util.Util
import org.eclipse.jdt.internal.core.CompilationUnit
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility

class BrowsingTestSetup extends TestSetup {

    private static Hashtable<String, String> savedPreferences
    private static TestProject testProject

    BrowsingTestSetup(Test test) {
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

    static void addJUnit4() {
        IClasspathEntry entry = JavaCore.newContainerEntry(new Path('org.eclipse.jdt.junit.JUNIT_CONTAINER/4'))
        testProject.addEntry(testProject.project, entry)
    }

    static CompilationUnit addJavaSource(CharSequence contents, String name = 'Pojo', String pack = '') {
        def type = testProject.createJavaTypeAndPackage(pack, name + '.java', contents.toString())
        return type.compilationUnit
    }

    static GroovyCompilationUnit addGroovySource(CharSequence contents, String name = 'Pogo', String pack = '') {
        def file = testProject.createGroovyTypeAndPackage(pack, name + '.groovy', contents.toString())
        return JavaCore.createCompilationUnitFrom(file)
    }

    static void openInEditor(ICompilationUnit unit) {
        unit.becomeWorkingCopy(null)
        unit.makeConsistent(null)

        EditorUtility.openInEditor(unit)
        SynchronizationUtils.joinBackgroudActivities()
    }

    static void waitForIndex() {
        testProject.waitForIndexer()
    }

    static void removeSources() {
        GroovyPlugin.default.activeWorkbenchWindow.activePage.closeAllEditors(false)

        testProject.deleteWorkingCopies()
        IResource sourceFolder = testProject.sourceFolder.resource
        sourceFolder.members().each { IResource item -> Util.delete(item) }

        SimpleProgressMonitor spm = new SimpleProgressMonitor("$testProject.project.name clean");
        testProject.project.build(IncrementalProjectBuilder.CLEAN_BUILD, spm)
        spm.waitForCompletion()

        testProject.javaProject.resetCaches()
    }
}
