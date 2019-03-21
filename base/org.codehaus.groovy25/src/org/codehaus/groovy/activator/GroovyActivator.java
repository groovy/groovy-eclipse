/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.activator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.net.URL;

public class GroovyActivator extends Plugin {

    public static final String PLUGIN_ID = "org.codehaus.groovy";

    public static final String GROOVY_ALL_JAR = "lib/groovy-all-2.5.0-indy.jar";

    public static URL GROOVY_ALL_JAR_URL;

    private static GroovyActivator DEFAULT;

    public GroovyActivator() {
        DEFAULT = this;
    }

    public static GroovyActivator getDefault() {
        return DEFAULT;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        if (Boolean.getBoolean("greclipse.debug.trace_compiler_start")) {
            System.out.println("------------");
            System.out.println("GRECLIPSE-1642: stack trace and other info as Groovy compiler starts");
            printBundleState("org.codehaus.groovy.eclipse.compilerResolver");
            printBundleState("org.eclipse.jdt.core");
            new Exception().printStackTrace();
            System.out.println("------------");
        }

        // enable InvokeDynamic support across the board
        System.setProperty("groovy.target.indy", "true");

        super.start(context);
        try {
            initialize();
        } catch (Exception e) {
            getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Error starting Groovy plugin", e));
        }
    }

    private void printBundleState(String id) {
        Bundle resolverBundle = Platform.getBundle(id);
        if (resolverBundle != null) {
            int state = resolverBundle.getState();
            String stateString;
            switch (state) {
                case Bundle.ACTIVE:
                    stateString = "ACTIVE";
                    break;
                case Bundle.INSTALLED:
                    stateString = "INSTALLED";
                    break;
                case Bundle.RESOLVED:
                    stateString = "RESOLVED";
                    break;
                case Bundle.STOPPING:
                    stateString = "STOPPING";
                    break;
                case Bundle.UNINSTALLED:
                    stateString = "UNINSTALLED";
                    break;
                default:
                    stateString = "UNKNOWN";
                    break;
            }
            System.out.println(id + " state: " + stateString);
        } else {
            System.out.println(id + " is not installed");
        }
    }

    public static void initialize() throws IOException {
        Bundle bundle = GroovyActivator.getDefault().getBundle();
        URL entry = bundle.getEntry(GroovyActivator.GROOVY_ALL_JAR);
        if (entry == null) {
            throw new RuntimeException("Couldn't find '" + GroovyActivator.GROOVY_ALL_JAR + "' in bundle " + bundle.getSymbolicName() + " " + bundle.getVersion());
        }
        GROOVY_ALL_JAR_URL = FileLocator.resolve(entry);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }
}
