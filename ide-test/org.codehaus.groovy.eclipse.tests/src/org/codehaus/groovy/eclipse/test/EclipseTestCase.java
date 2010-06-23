 /*
 * Copyright 2003-2009 the original author or authors.
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

import junit.framework.TestCase;

import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragment;

/**
 * Base test case for all Groovy eclipse plugin test cases.
 *
 * Not used
 *
 * @author MelamedZ
 */
public abstract class EclipseTestCase extends TestCase {

    protected TestProject testProject;

    protected IPackageFragment pack;

    protected GroovyCoreActivator plugin;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("------------------------------");
        System.out.println("Starting: " + getName());
        testProject = new TestProject();
    }

    @Override
    protected void tearDown() throws Exception {
        testProject.dispose();
    }

    public EclipseTestCase() {
        super();
    }

    public EclipseTestCase(String name) {
        super(name);
    }
    /**
     * Will test to see if the TestProject instance has Groovy nature.
     *
     * @return Returns true if the project has the Groovy nature.
     * @throws CoreException
     */
    protected boolean hasGroovyNature() throws CoreException {
        return testProject.getProject().hasNature(GroovyNature.GROOVY_NATURE);
    }

    /**
     * Does a full build on files in the test project.
     *
     * @throws Exception
     */
    protected void fullProjectBuild() throws Exception {
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }


    protected void waitForIndexes() {
    	SynchronizationUtils.waitForIndexingToComplete();
    }

    protected IMarker[] getFailureMarkers() throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        return root.findMarkers("org.codehaus.groovy.eclipse.groovyFailure",
                false, IResource.DEPTH_INFINITE);
    }

}
