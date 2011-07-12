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
package org.codehaus.groovy.eclipse.core;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * @author Rene Scheibe
 * @created Jul 11, 2011
 */
public class ClasspathContainerTest extends EclipseTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GroovyCoreActivator.getDefault().setPreference(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, false);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        GroovyCoreActivator.getDefault().setPreference(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, true);
    }

    public void testClassPathContainerContents() throws JavaModelException {

        JavaModelManager javaModelManager = JavaModelManager.getJavaModelManager();
        IPath containerPath = new Path("GROOVY_SUPPORT");
        IClasspathContainer container = javaModelManager.getClasspathContainer(containerPath, testProject.getJavaProject());

        IClasspathEntry[] entries = container.getClasspathEntries();

        List<IClasspathEntry> groovyAllEntries = getGroovyAllEntries(entries);
        List<IClasspathEntry> nonPluginEntries = getNonPluginEntries(entries);

        assertEquals("classpath container class", GroovyClasspathContainer.class, container.getClass());
        assertEquals("Mutiple groovy-all found in the Groovy classpath container: " + groovyAllEntries, 1, groovyAllEntries.size());
        assertEquals("Unexpected entries found in the Groovy classpath container: " + nonPluginEntries, 0, nonPluginEntries.size());
    }

    private List<IClasspathEntry> getGroovyAllEntries(IClasspathEntry[] entries) {
        List<IClasspathEntry> groovyAllEntries = new ArrayList<IClasspathEntry>();
        for (IClasspathEntry entry : entries) {
            if (entry.getPath().toPortableString().contains("groovy-all")) {
                groovyAllEntries.add(entry);
            }
        }
        return groovyAllEntries;
    }

    private List<IClasspathEntry> getNonPluginEntries(IClasspathEntry[] entries) {
        List<IClasspathEntry> nonPluginEntries = new ArrayList<IClasspathEntry>();
        for (IClasspathEntry entry : entries) {
            if (!entry.getPath().toPortableString().contains("/org.codehaus.groovy")) {
                nonPluginEntries.add(entry);
            }
        }
        return nonPluginEntries;
    }
}