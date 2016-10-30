/*
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
