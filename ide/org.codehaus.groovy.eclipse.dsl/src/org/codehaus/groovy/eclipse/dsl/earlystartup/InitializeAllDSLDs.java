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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.dsl.DSLPreferencesInitializer;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.RefreshDSLDJob;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * Initializes all DSLD scripts in the workspace on startup
 * This will start the Groovy plugin if any groovy projects are found
 * @author andrew
 * @created Nov 25, 2010
 */
public class InitializeAllDSLDs implements IStartup {
    final Bundle m2eJdtBundle = Platform.getBundle("org.eclipse.m2e.jdt");
    boolean isStarted() {
        return m2eJdtBundle == null || m2eJdtBundle.getState() == Bundle.ACTIVE;
    }
    public void earlyStartup() {
        // https://jira.codehaus.org/browse/GRECLIPSE-1602
        // check for the org.eclipse.m2e.jdt bundle and if exists, ensure started before initializing
        
        if (isStarted()) {
            // m2e not installed, just go ahead
            initializeAll();
        } else {
            // m2e installed, wait until bundle has been started
            // run in a worker thread to avoid locking the early startup thread
            new Job("Wait for m2e to start") {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        m2eJdtBundle.start(Bundle.START_TRANSIENT);
                        initializeAll();
                        return Status.OK_STATUS;
                    } catch (BundleException e) {
                        return new Status(IStatus.ERROR, PLUGIN_ID, "Could not start m2e plugin. See details for more information", e);
                    }
                }

            }.schedule();
        }
    }

    public void initializeAll() {
        IPreferenceStore prefStore = getPreferenceStore();
        if (prefStore.getBoolean(DSLPreferencesInitializer.DSLD_DISABLED)) {
            return;
        }

        
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        List<IProject> toRefresh = new ArrayList<IProject>(allProjects.length);
        for (IProject project : allProjects) {
            // don't access the GroovyNature class here because we don't want to start
            // the groovy plugin if we don't have to.
            try {
                if (project.isAccessible() && project.hasNature("org.eclipse.jdt.groovy.core.groovyNature")) {
                    toRefresh.add(project);
                }
            } catch (CoreException e) {
                logException(e);
            }
        }
        Job refreshJob = new RefreshDSLDJob(toRefresh);
        refreshJob.setPriority(Job.LONG);
        refreshJob.schedule();
    }

    /**
     * Must keep this in a different method to avoid accidentally starting the DSLD plugin (and hence all of the groovy plugins).
     * @param e
     */
    private void logException(Exception e) {
        GroovyDSLCoreActivator.logException(e);
    }
    
    static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.dsl";
    
    /**
     * Avoids accidentally loading the plugin
     * @return
     */
    @SuppressWarnings("deprecation")
    public IPreferenceStore getPreferenceStore() {
        return new ScopedPreferenceStore(new InstanceScope(), PLUGIN_ID);
    }
}