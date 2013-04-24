package org.codehaus.groovy.frameworkadapter.util;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;

public class ResolverActivator implements BundleActivator {

    public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.compilerResolver"; 
    private static BundleContext context;
	private static ResolverActivator instance;
	private CompilerChooser chooser;
    private BundleListener listener;

	public ResolverActivator() {
	    instance = this;
    }
	
	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext bundleContext) throws Exception {
		ResolverActivator.context = bundleContext;
		chooser = new CompilerChooser();
		
		// There is a small window where the chooser can be initialized.
		// It has to be after the workspace has started (in order to ensure
		// the choose workspace dialog still shows) but before JDT is initialized
		// (so that the groovy bundles aren't loaded).
		// Best way to do that is through the listener below
		listener = new BundleListener() {
            public void bundleChanged(BundleEvent event) {
                if ((event.getType() == BundleEvent.STARTING || event.getType() == BundleEvent.STARTED) && 
                            event.getBundle().getSymbolicName().equals("org.eclipse.core.resources")) {
                    initializeChooser();
                }
            }
        };
        bundleContext.addBundleListener(listener);
	}


    public void initializeChooser() {
        try {
            context.removeBundleListener(listener);
            chooser.initialize(context);
        } catch (BundleException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		ResolverActivator.context = null;
	}
	
	public static ResolverActivator getDefault() {
	    return instance;
	}
	
	public CompilerChooser getChooser() {
        return chooser;
    }
}
