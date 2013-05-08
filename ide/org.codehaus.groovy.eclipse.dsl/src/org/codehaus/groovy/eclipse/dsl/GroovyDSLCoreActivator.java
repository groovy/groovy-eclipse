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
package org.codehaus.groovy.eclipse.dsl;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.classpath.AutoAddContainerSupport;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class GroovyDSLCoreActivator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.dsl";

    private static BundleContext context;

    private static GroovyDSLCoreActivator plugin;
    
    private final DSLDStoreManager contextStoreManager;

    private DSLDResourceListener dsldResourceListener;
    private DSLDElementListener dsldElementListener;
    
    
    private AutoAddContainerSupport containerListener;

    public final static String MARKER_ID = "org.codehaus.groovy.eclipse.dsl.inferencing_problem";

    public static IPath CLASSPATH_CONTAINER_ID = new Path("GROOVY_DSL_SUPPORT");
    
    public GroovyDSLCoreActivator() {
        plugin = this;
        this.contextStoreManager = new DSLDStoreManager();
    }

	
    public static GroovyDSLCoreActivator getDefault() {
        return plugin;
    }
    
	static BundleContext getContext() {
		return context;
	}

    @Override
	public void start(BundleContext bundleContext) throws Exception {
        super.start(bundleContext);
		GroovyDSLCoreActivator.context = bundleContext;
		dsldElementListener = new DSLDElementListener();
		JavaCore.addElementChangedListener(dsldElementListener, ElementChangedEvent.POST_CHANGE);

		dsldResourceListener = new DSLDResourceListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(dsldResourceListener);
		
		containerListener = new AutoAddContainerSupport();
		containerListener.addContainerToAll();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(containerListener, IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
        super.stop(bundleContext);
		GroovyDSLCoreActivator.context = null;
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(dsldResourceListener);
        dsldResourceListener = null;

        JavaCore.removeElementChangedListener(dsldElementListener);
        dsldElementListener = null;
        
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(containerListener);
        containerListener.dispose();
        containerListener = null;
        
	}
	
	public AutoAddContainerSupport getContainerListener() {
        return containerListener;
    }

	public DSLDStoreManager getContextStoreManager() {
        return contextStoreManager;
    }
	
    private static void log(int severity, String message, Throwable throwable) {
        final IStatus status = new Status(severity, PLUGIN_ID, 0, message, throwable);
        try {
            getDefault().getLog().log(status);
        } catch (NullPointerException e) {
            // plugin starting up or shutting down.  can ignore
        }
        if (GroovyLogManager.manager.hasLoggers()) {
            if (throwable != null) {
                GroovyLogManager.manager.log(TraceCategory.DSL, "Exception caught.  See error log.  Message: " + throwable.getLocalizedMessage());
            } else if (message != null) {
                GroovyLogManager.manager.log(TraceCategory.DSL, "Message logged.  See error log.  Message: " + message);
            }
        }
    }
    public static void logException(String message, Throwable throwable) {
        log(IStatus.ERROR, message, throwable);
    }
    public static void logException(Throwable throwable) {
        log(IStatus.ERROR, throwable.getLocalizedMessage(), throwable);
    }
    public static void logWarning(String message) {
        log(IStatus.WARNING, message, null);
    }
    
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public boolean isDSLDDisabled() {
    	return getPreferenceStore().getBoolean(DSLPreferencesInitializer.DSLD_DISABLED);
    }
    
}
