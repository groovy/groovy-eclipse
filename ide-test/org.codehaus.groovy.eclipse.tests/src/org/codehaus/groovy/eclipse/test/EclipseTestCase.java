/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
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
        testProject = new TestProject();
    }

    @Override
    protected void tearDown() throws Exception {
        testProject.dispose();
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
        testProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD,
                null);
    }

    protected IMarker[] getFailureMarkers() throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        return root.findMarkers("org.codehaus.groovy.eclipse.groovyFailure",
                false, IResource.DEPTH_INFINITE);
    }

}
