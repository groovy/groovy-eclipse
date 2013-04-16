package org.codehaus.groovy.frameworkadapter.util;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ResolverActivator implements BundleActivator {

    public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.compilerResolver"; 
    private static BundleContext context;
	private static ResolverActivator instance;
	private CompilerChooser chooser;

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
	public void start(BundleContext bundleContext) throws Exception {
		ResolverActivator.context = bundleContext;
		chooser = new CompilerChooser();
		chooser.initialize(bundleContext);
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
