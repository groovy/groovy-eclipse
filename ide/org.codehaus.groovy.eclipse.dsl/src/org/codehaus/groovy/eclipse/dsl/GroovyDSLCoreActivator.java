/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    public static final String MARKER_ID = GroovyDSLCoreActivator.PLUGIN_ID + ".inferencing_problem";

    public static final IPath CLASSPATH_CONTAINER_ID = new Path("GROOVY_DSL_SUPPORT");

    private static GroovyDSLCoreActivator plugin;

    private final DSLDStoreManager contextStoreManager = new DSLDStoreManager();

    private DSLDElementListener dsldElementListener;

    private DSLDResourceListener dsldResourceListener;

    private AutoAddContainerSupport containerListener;

    public static GroovyDSLCoreActivator getDefault() {
        return plugin;
    }

    public GroovyDSLCoreActivator() {
        plugin = this;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        super.start(bundleContext);
        startListening();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        super.stop(bundleContext);
        stopListening();
    }

    public void startListening() {
        if (dsldElementListener != null || isDSLDDisabled()) {
            return;
        }

        dsldElementListener = new DSLDElementListener();
        JavaCore.addElementChangedListener(dsldElementListener, ElementChangedEvent.POST_CHANGE);

        dsldResourceListener = new DSLDResourceListener();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(dsldResourceListener);

        containerListener = new AutoAddContainerSupport();
        containerListener.addContainer(ResourcesPlugin.getWorkspace().getRoot().getProjects());
        ResourcesPlugin.getWorkspace().addResourceChangeListener(containerListener, IResourceChangeEvent.POST_CHANGE);
    }

    public void stopListening() {
        if (dsldElementListener != null) {
            JavaCore.removeElementChangedListener(dsldElementListener);
            dsldElementListener = null;
        }

        if (dsldResourceListener != null) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(dsldResourceListener);
            dsldResourceListener = null;
        }

        if (containerListener != null) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(containerListener);
            containerListener.dispose();
            containerListener = null;
        }
    }

    public AutoAddContainerSupport getContainerListener() {
        return containerListener;
    }

    public DSLDStoreManager getContextStoreManager() {
        return contextStoreManager;
    }

    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public boolean isDSLDDisabled() {
        return getPreferenceStore().getBoolean(DSLPreferencesInitializer.DSLD_DISABLED);
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
}
