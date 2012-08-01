 /*
 * Copyright 2009-2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.core;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class GroovyCoreActivator extends Plugin {

    public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.core";

    private static GroovyCoreActivator plugin;

    private IEclipsePreferences instanceScope;

    public GroovyCoreActivator() {
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static GroovyCoreActivator getDefault() {
        return plugin;
    }

    public IEclipsePreferences getPreferences() {
        if (instanceScope == null) {
            instanceScope = ((IScopeContext) InstanceScope.INSTANCE).getNode(GroovyCoreActivator.PLUGIN_ID);
        }
        return instanceScope;
    }

    public boolean getPreference(String key, boolean def) {
        return getPreferences().getBoolean(key, def);
    }

    public void setPreference(String key, boolean val) {
        getPreferences().putBoolean(key, val);
    }
}
