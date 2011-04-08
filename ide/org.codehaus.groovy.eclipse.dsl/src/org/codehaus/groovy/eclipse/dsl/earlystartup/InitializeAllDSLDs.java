/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.earlystartup;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.RefreshDSLDJob;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IStartup;

/**
 * Initializes all DSLD scripts in the workspace on startup
 * This will start the Groovy plugin if any groovy projects are found
 * @author andrew
 * @created Nov 25, 2010
 */
public class InitializeAllDSLDs implements IStartup {

    public void earlyStartup() {
        initializeAll();
    }

    public void initializeAll() {
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject project : allProjects) {
            // don't access the GroovyNature class here because we don't want to start
            // the groovy plugin if we don't have to.
            try {
                if (project.isAccessible() && project.hasNature("org.eclipse.jdt.groovy.core.groovyNature")) {
                    Job refreshJob = new RefreshDSLDJob(project);
                    refreshJob.setPriority(Job.LONG);
                    refreshJob.schedule();
                }
            } catch (CoreException e) {
                GroovyDSLCoreActivator.logException(e);
            }
        }
    }
}
