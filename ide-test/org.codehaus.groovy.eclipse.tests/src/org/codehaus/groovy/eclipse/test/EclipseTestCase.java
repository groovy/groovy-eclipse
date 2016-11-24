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
package org.codehaus.groovy.eclipse.test;

import java.util.Hashtable;

import junit.framework.TestCase;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;

/**
 * Provides a fresh Groovy project to each test method.
 */
public abstract class EclipseTestCase extends TestCase {

    protected TestProject testProject;

    private Hashtable<String, String> savedPreferences;

    @Override
    protected void setUp() throws Exception {
        System.out.println("----------------------------------------");
        System.out.println("Starting: " + getName());

        super.setUp();
        testProject = new TestProject();
        savedPreferences = JavaCore.getOptions();
    }

    @Override
    protected void tearDown() throws Exception {
        JavaCore.setOptions(savedPreferences);
        testProject.dispose();
        super.tearDown();
    }

    protected void setJavaPreference(String name, String value) {
        Hashtable<String, String> options = JavaCore.getOptions();
        options.put(name, value);
        JavaCore.setOptions(options);
    }

    /**
     * Will test to see if the TestProject instance has Groovy nature.
     *
     * @return {@code true} if the project has the Groovy nature
     */
    protected boolean hasGroovyNature() throws CoreException {
        return testProject.getProject().hasNature(GroovyNature.GROOVY_NATURE);
    }

    /**
     * Performs a full build on the test workspace.
     */
    protected void fullProjectBuild() throws Exception {
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

    public void waitForIndexes() {
        testProject.waitForIndexer();
    }
}
