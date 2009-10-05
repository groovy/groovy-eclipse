package org.codehaus.groovy.activator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class GroovyActivator extends Plugin {

    public GroovyActivator() {
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        getLog().log(new Status(IStatus.INFO, context.getBundle().getSymbolicName(),
                "Starting Groovy compiler version " + context.getBundle().getVersion()));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        getLog().log(new Status(IStatus.INFO, context.getBundle().getSymbolicName(),
                "Stopping Groovy compiler version " + context.getBundle().getVersion()));
    }
}
