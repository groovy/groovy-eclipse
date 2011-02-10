package org.codehaus.groovy.eclipse.dsl;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.contexts.ContextStoreManager;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class GroovyDSLActivator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.dsl";

    private static BundleContext context;

    private static GroovyDSLActivator plugin;
    
    private final ContextStoreManager contextStoreManager;

    private GDSLResourceListener gdslListener;
    
    public GroovyDSLActivator() {
        plugin = this;
        this.contextStoreManager = new ContextStoreManager();
    }

	
    public static GroovyDSLActivator getDefault() {
        return plugin;
    }
    
	static BundleContext getContext() {
		return context;
	}

    @Override
	public void start(BundleContext bundleContext) throws Exception {
		GroovyDSLActivator.context = bundleContext;
		gdslListener = new GDSLResourceListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(gdslListener);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		GroovyDSLActivator.context = null;
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(gdslListener);
        gdslListener = null;
	}

	public ContextStoreManager getContextStoreManager() {
        return contextStoreManager;
    }
	
    private static void log(int severity, String message, Throwable throwable) {
        final IStatus status = new Status(severity, PLUGIN_ID, 0, message, throwable);
        getDefault().getLog().log(status);
        if (throwable != null) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Exception caught.  See error log.  Message: " + throwable.getLocalizedMessage());
        } else if (message != null) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Message logged.  See error log.  Message: " + message);
        }
    }
    public static void logException(String message, Throwable throwable) {
        log(IStatus.ERROR, message, throwable);
    }
    public static void logException(Throwable throwable) {
        log(IStatus.ERROR, throwable.getLocalizedMessage(), throwable);
    }

}
