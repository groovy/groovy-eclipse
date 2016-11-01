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
package org.codehaus.groovy.eclipse.dsl.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
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
    public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.dsl.tests";

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
        return getTestResourceURL(fileName).openStream();
    }

    public String getTestResourceContents(String fileName) throws IOException {
        InputStream stream = getTestResourceStream(fileName);
        return getContents(stream);
    }

    public URL getTestResourceURL(String fileName) throws MalformedURLException {
        IPath path= new Path("testResources").append(fileName);
        return new URL(getBundle().getEntry("/"), path.toString());
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
