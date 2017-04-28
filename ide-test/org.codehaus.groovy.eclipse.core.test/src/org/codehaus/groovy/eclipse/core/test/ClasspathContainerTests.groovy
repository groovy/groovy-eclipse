/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.test

import org.codehaus.groovy.eclipse.core.GroovyCoreActivator
import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants
import org.codehaus.groovy.eclipse.test.EclipseTestCase
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathContainer
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.internal.core.JavaModelManager
import org.junit.After
import org.junit.Before
import org.junit.Test

final class ClasspathContainerTests extends EclipseTestCase {

    @Before
    void setUp() {
        GroovyCoreActivator.getDefault().setPreference(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, false)
    }

    @After
    void tearDown() {
        GroovyCoreActivator.getDefault().setPreference(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, true)
    }

    @Test
    void testClassPathContainerContents() {
        IClasspathContainer container = JavaModelManager.getJavaModelManager().getClasspathContainer(new Path('GROOVY_SUPPORT'), testProject.javaProject)
        IClasspathEntry[] entries = container.getClasspathEntries()
        List<IClasspathEntry> groovyAllEntries = entries.findAll { it.path.toPortableString().contains('groovy-all') }
        List<IClasspathEntry> nonPluginEntries = entries.findAll { !it.path.toPortableString().contains('/org.codehaus.groovy') }

        assert container.getClass() == GroovyClasspathContainer : 'classpath container class'
        assert groovyAllEntries.size() == 1 : 'Mutiple groovy-all found in the Groovy classpath container: ' + groovyAllEntries
        assert nonPluginEntries.isEmpty() : 'Unexpected entries found in the Groovy classpath container: ' + nonPluginEntries
    }
}
