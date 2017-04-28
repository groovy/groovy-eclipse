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
package org.codehaus.groovy.eclipse.test;

import java.util.Hashtable;

import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.groovy.tests.builder.SimpleProgressMonitor;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * Provides a fresh Groovy project to each test method.
 */
public abstract class EclipseTestCase {

    @Rule
    public TestName test = new TestName();

    protected TestProject testProject;

    @Before
    public final void setUpTestCase() throws Exception {
        System.out.println("----------------------------------------");
        System.out.println("Starting: " + test.getMethodName());

        testProject = new TestProject();
        TestProject.setAutoBuilding(false);
    }

    @After
    public final void tearDownTestCase() throws Exception {
        try {
            testProject.dispose();
            testProject = null;
        } finally {
            JavaCore.setOptions(JavaCore.getDefaultOptions());
        }
    }

    protected final void setJavaPreference(String key, String val) {
        if (key.startsWith(JavaCore.PLUGIN_ID)) {
            Hashtable<String, String> options = JavaCore.getOptions();
            options.put(key, val);
            JavaCore.setOptions(options);

        } else if (key.startsWith(JavaPlugin.getPluginId()) ||
                JavaPlugin.getDefault().getPreferenceStore().contains(key)) {
            JavaPlugin.getDefault().getPreferenceStore().setValue(key, val);

        } else {
            System.err.println("Unexpected preference: " + key);
        }
    }

    /**
     * Will test to see if the TestProject instance has Groovy nature.
     *
     * @return {@code true} if the project has the Groovy nature
     */
    protected final boolean hasGroovyNature() throws CoreException {
        return testProject.getProject().hasNature(GroovyNature.GROOVY_NATURE);
    }

    /**
     * Performs a full build on the test workspace.
     */
    protected final void buildAll() throws CoreException {
        SimpleProgressMonitor monitor = new SimpleProgressMonitor("Plug-in test workspace build");
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
        monitor.waitForCompletion();
    }

    protected final void waitForIndex() {
        testProject.waitForIndexer();
    }
}
