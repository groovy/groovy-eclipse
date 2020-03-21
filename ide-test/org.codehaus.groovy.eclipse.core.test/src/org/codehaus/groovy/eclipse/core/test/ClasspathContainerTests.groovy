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
package org.codehaus.groovy.eclipse.core.test

import static org.codehaus.groovy.eclipse.core.GroovyCoreActivator.getDefault as getGroovyPlugin
import static org.eclipse.jdt.internal.core.JavaModelManager.getJavaModelManager

import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.jdt.core.IClasspathContainer
import org.junit.After
import org.junit.Before
import org.junit.Test

final class ClasspathContainerTests extends GroovyEclipseTestSuite {

    @Before
    void setUp() {
        groovyPlugin.setPreference(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, false)
    }

    @After
    void tearDown() {
        groovyPlugin.setPreference(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, true )
    }

    IClasspathContainer getContainer() {
        packageFragmentRoot.javaProject.with { jp ->
            javaModelManager.getClasspathContainer(GroovyRuntime.findClasspathEntry(jp, { it.path.segment(0) == GroovyClasspathContainer.ID }).get().path, jp)
        }
    }

    @Test
    void testClasspathContainerType() {
        assert container.class == GroovyClasspathContainer
    }

    @Test
    void testMinimalClasspathContainerContents() {
        packageFragmentRoot.javaProject.with { jp ->
            GroovyRuntime.removeGroovyClasspathContainer(jp)
            GroovyRuntime.addGroovyClasspathContainer(jp, true)
        }

        def entries = container.classpathEntries
        def groovyAllEntries = entries.findAll { it.path.toPortableString() =~ /\bgroovy(-all)?-\d/ }
        def nonPluginEntries = entries.findAll { !it.path.toPortableString().contains('/org.codehaus.groovy') }

        assert groovyAllEntries.size() == 1 : "Mutiple groovy-all found in the Groovy classpath container: $groovyAllEntries"
        assert nonPluginEntries.isEmpty()   : "Unexpected entries found in the Groovy classpath container: $nonPluginEntries"
    }

    @Test
    void testMaximalClasspathContainerContents() {
        packageFragmentRoot.javaProject.with { jp ->
            GroovyRuntime.removeGroovyClasspathContainer(jp)
            GroovyRuntime.addGroovyClasspathContainer(jp, false)
        }

        def entries = container.classpathEntries
        def groovyAllEntries = entries.findAll { it.path.toPortableString() =~ /\bgroovy(-all)?-\d/ }

        assert groovyAllEntries.size() == 1 : "Mutiple groovy-all found in the Groovy classpath container: $groovyAllEntries"
        assert entries.any { it.path.toPortableString().contains('ivy-2') } : 'ivy.jar (for Grab support) should be included'
    }
}
