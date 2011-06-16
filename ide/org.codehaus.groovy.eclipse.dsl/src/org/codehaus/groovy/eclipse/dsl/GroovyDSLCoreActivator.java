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
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class GroovyDSLCoreActivator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.dsl";

    private static BundleContext context;

    private static GroovyDSLCoreActivator plugin;
    
    private final DSLDStoreManager contextStoreManager;

    private DSLDResourceListener dsldListener;
    
    private AutoAddContainerSupport containerListener;

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
		dsldListener = new DSLDResourceListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(dsldListener);

		containerListener = new AutoAddContainerSupport();
		containerListener.addContainerToAll();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(containerListener, IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
        super.stop(bundleContext);
		GroovyDSLCoreActivator.context = null;
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(dsldListener);
        dsldListener = null;
        
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
        getDefault().getLog().log(status);
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
    

}
