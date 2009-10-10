package org.codehaus.groovy.activator;

import org.eclipse.core.internal.registry.osgi.OSGIUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.internal.baseadaptor.StateManager;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.State;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

public class GroovyActivator extends Plugin {

    public static final String PLUGIN_ID = "org.codehaus.groovy";
    
    public static final String REFRESH_ON_STARTUP = "refresh.on.startup";
    
    
    private static GroovyActivator DEFAULT;
    
    public GroovyActivator() {
        DEFAULT = this;
    }
    
    public static GroovyActivator getDefault() {
        return DEFAULT;
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        if (isRefreshOnStartup()) {
            setRefreshOnStartup(false);
            // do not force a package refresh at next startup
            IStatus status = new RefreshPackages().removeJvmArg();
            if (status.getSeverity() >= IStatus.WARNING) {
                getLog().log(status);
            }
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        logInfo("Stopping Groovy compiler version " + context.getBundle().getVersion());
    }
    
    public void setRefreshOnStartup(boolean doIt) {
        // set the preference
        IEclipsePreferences contextNode = new InstanceScope().getNode(PLUGIN_ID);
        contextNode.putBoolean(REFRESH_ON_STARTUP, doIt);
        try {
            contextNode.flush();
        } catch (BackingStoreException e) {
            logException(e);
        }
    }
    
    private boolean isRefreshOnStartup() {
        IEclipsePreferences contextNode = new InstanceScope().getNode(PLUGIN_ID);
        return contextNode.getBoolean(REFRESH_ON_STARTUP, false);
    }
    
    private void logException(Exception e) {
        getLog().log(new Status(IStatus.ERROR, PLUGIN_ID,e.getMessage(), e));
    }
    
    private void logInfo(String message) {
        getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
    }
}