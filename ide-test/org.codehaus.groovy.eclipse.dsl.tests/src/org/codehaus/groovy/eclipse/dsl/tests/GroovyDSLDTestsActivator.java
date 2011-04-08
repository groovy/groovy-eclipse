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
package org.codehaus.groovy.eclipse.dsl.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class GroovyDSLDTestsActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.dsl.tests"; //$NON-NLS-1$

	// The shared instance
	private static GroovyDSLDTestsActivator plugin;
	
	/**
	 * The constructor
	 */
	public GroovyDSLDTestsActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static GroovyDSLDTestsActivator getDefault() {
		return plugin;
	}
	
    public InputStream getTestResourceStream(String fileName) throws IOException {
        IPath path= new Path("testResources").append(fileName);
        URL url= new URL(getBundle().getEntry("/"), path.toString());
        return url.openStream();
    }

    public String getTestResourceContents(String fileName) throws IOException {
        InputStream stream = getTestResourceStream(fileName);
        return getContents(stream);
    }
    
    public String getContents(InputStream in) throws IOException {
        BufferedReader br= new BufferedReader(new InputStreamReader(in));

        StringBuffer sb= new StringBuffer(300);
        try {
            int read= 0;
            while ((read= br.read()) != -1)
                sb.append((char) read);
        } finally {
            br.close();
        }
        return sb.toString();
    }



}
