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
package org.codehaus.groovy.eclipse.core;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class GroovyCoreActivator extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.core";

    public static final String SUPPORT_GROOVY = "support.groovy";

    // The shared instance
    private static GroovyCoreActivator plugin;

    private ServiceTracker tracker = null;

    private static BundleContext context = null;

    /**
     * The constructor
     */
    public GroovyCoreActivator() {
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        GroovyCoreActivator.context = context;
        tracker = new ServiceTracker(context, PlatformAdmin.class.getName(),
                null);
        tracker.open();
        
        // I don't like this, but we need to ensure that the code browsing bundle is
        // started with the rest of th e
        Platform.getBundle("org.codehaus.groovy.eclipse.codebrowsing").start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
        if (tracker != null)
            tracker.close();
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static GroovyCoreActivator getDefault() {
        return plugin;
    }

    /**
     * Facade to get to the workspace
     * 
     * @return Returns the workspace instance.
     */
    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    public static BundleContext context() {
        return context;
    }

    public static Bundle bundle() {
        return getDefault().getBundle();
    }

    public static Bundle getBundle(final long id) {
        return context.getBundle(id);
    }

    public static Bundle bundle(final long id) {
        return getBundle(id);
    }

    public static Bundle getBundle(final String symbolicName) {
        final BundleDescription desc = bundleDescription(symbolicName);
        if (desc == null)
            return null;
        return bundle(desc.getBundleId());
    }

    public static Bundle bundle(final String symbolicName) {
        return getBundle(symbolicName);
    }

    public static PlatformAdmin getPlatformAdmin() {
        return (PlatformAdmin) getDefault().tracker.getService();
    }

    public static PlatformAdmin platformAdmin() {
        return getPlatformAdmin();
    }

    public static BundleDescription getBundleDescription(final long id) {
        return platformAdmin().getState().getBundle(id);
    }

    public static BundleDescription bundleDescription(final long id) {
        return getBundleDescription(id);
    }

    public static BundleDescription getBundleDescription(final String name) {
        return platformAdmin().getState().getBundle(name, null);
    }

    public static BundleDescription bundleDescription(final String name) {
        return getBundleDescription(name);
    }

    public static BundleDescription getBundleDescription() {
        return getBundleDescription(bundle().getBundleId());
    }

    public static BundleDescription bundleDescription() {
        return getBundleDescription();
    }

}
