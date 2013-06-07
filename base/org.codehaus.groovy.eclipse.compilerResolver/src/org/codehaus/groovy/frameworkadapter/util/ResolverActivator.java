/*******************************************************************************
 * Copyright (c) 2013 VMWare, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMWare, Inc. - initial API and implementation
 *******************************************************************************/
package org.codehaus.groovy.frameworkadapter.util;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

public class ResolverActivator implements BundleActivator {

    public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.compilerResolver"; 
    private static BundleContext context;
	private static ResolverActivator instance;
	private CompilerChooser chooser;
    private ServiceListener serviceListener;

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
		
		// The service listener is called synchronously as the resources bundle is actived
        String filter = '(' + Constants.OBJECTCLASS + "=org.eclipse.core.resources.IWorkspace)";
        serviceListener = new ServiceListener() {
            public void serviceChanged(ServiceEvent event) {
                if (event.getType() == ServiceEvent.REGISTERED) {
                    initializeChooser();
                }
            }
        };
        bundleContext.addServiceListener(serviceListener, filter);
	}


    public void initializeChooser() {
        try {
            context.removeServiceListener(serviceListener);
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
