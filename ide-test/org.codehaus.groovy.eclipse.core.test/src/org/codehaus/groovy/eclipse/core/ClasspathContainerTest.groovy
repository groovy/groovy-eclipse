/*
 * Copyright 2009-2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.core

import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants
import org.codehaus.groovy.eclipse.test.EclipseTestCase
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.internal.core.JavaModelManager

/**
 * @author Rene Scheibe
 * @created Jul 11, 2011
 */
class ClasspathContainerTest extends EclipseTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GroovyCoreActivator.getDefault().setPreference(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, false) }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        GroovyCoreActivator.getDefault().setPreference(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, true) }

    void testClassPathContainerContents() {

        def javaModelManager = JavaModelManager.javaModelManager
        def containerPath = new Path('GROOVY_SUPPORT')
        def container = javaModelManager.getClasspathContainer(containerPath, testProject.getJavaProject())

        def entries = container.getClasspathEntries()

        def groovyAllEntries = entries.findAll { it.path.toPortableString().contains('groovy-all') }
        def nonPluginEntries = entries.findAll { it.path.toPortableString().contains('/org.codehaus.groovy') == false }

        assertEquals("classpath container class", GroovyClasspathContainer.class, container.getClass())
        assertEquals("Mutiple groovy-all found in the Groovy classpath container: $groovyAllEntries", 1, groovyAllEntries.size())
        assertEquals("Unexpected entries found in the Groovy classpath container: $nonPluginEntries", 0, nonPluginEntries.size()) } }
