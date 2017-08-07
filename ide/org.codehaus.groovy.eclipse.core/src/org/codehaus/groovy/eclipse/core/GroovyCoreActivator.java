/*
 * Copyright 2009-2017 the original author or authors.
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
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;

public class GroovyCoreActivator extends Plugin {

    public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.core";

    private static GroovyCoreActivator plugin;

    public static GroovyCoreActivator getDefault() {
        return plugin;
    }

    public GroovyCoreActivator() {
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }

    //--------------------------------------------------------------------------

    private IEclipsePreferences preferences;

    public IEclipsePreferences getPreferences() {
        if (preferences == null) {
            preferences = InstanceScope.INSTANCE.getNode(GroovyCoreActivator.PLUGIN_ID);
        }
        return preferences;
    }

    public boolean getPreference(String key, boolean def) {
        return getPreferences().getBoolean(key, def);
    }

    public void setPreference(String key, boolean val) {
        getPreferences().putBoolean(key, val);
    }
}
