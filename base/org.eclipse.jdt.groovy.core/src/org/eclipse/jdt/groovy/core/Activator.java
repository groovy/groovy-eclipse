/*
 * Copyright 2009-2019 the original author or authors.
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
package org.eclipse.jdt.groovy.core;

import static org.eclipse.core.runtime.Platform.getAdapterManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.internal.core.util.Util;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

public class Activator extends Plugin {

    // preference constant that if true means this project uses its own compiler settings
    public static final String USING_PROJECT_PROPERTIES = "org.codehaus.groovy.eclipse.preferences.compiler.project";

    public static final String GROOVY_CHECK_FOR_COMPILER_MISMATCH = "groovy.check.for.compiler.mismatch";

    public static final String GROOVY_COMPILER_LEVEL = "groovy.compiler.level";

    public static final String GROOVY_SCRIPT_FILTERS = "groovy.script.filters";
    public static final String DEFAULT_GROOVY_SCRIPT_FILTER = "**/*.dsld,y,**/*.gradle,n";

    public static final String GROOVY_SCRIPT_FILTERS_ENABLED = "groovy.script.filters.enabled";
    public static final boolean DEFAULT_SCRIPT_FILTERS_ENABLED = true;

    //--------------------------------------------------------------------------

    public static final String PLUGIN_ID = "org.eclipse.jdt.groovy.core";

    private static Activator plugin;

    public static Activator getDefault() {
        return plugin;
    }

    public Activator() {
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        factory = new GroovyResourceAdapter();
        IAdapterManager manager = getAdapterManager();
        manager.registerAdapters(factory, IResource.class);
    }

    private IAdapterFactory factory;

    @Override
    public void stop(BundleContext context) throws Exception {
        IAdapterManager manager = getAdapterManager();
        manager.unregisterAdapters(factory);
        factory = null;

        super.stop(context);
    }

    public static IEclipsePreferences getInstancePreferences() {
        return InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
    }

    public static IEclipsePreferences getProjectPreferences(IProject project) {
        if (project == null) return null;
        return new ProjectScope(project).getNode(PLUGIN_ID);
    }

    //--------------------------------------------------------------------------

    public String getGroovyCompilerLevel(IProject project) {
        IEclipsePreferences preferences = getProjectPreferences(project);
        if (preferences != null) {
            return preferences.get(GROOVY_COMPILER_LEVEL, null);
        }
        return null;
    }

    public void setGroovyCompilerLevel(IProject project, String level) {
        IEclipsePreferences preferences = getProjectPreferences(project);
        if (preferences != null) {
            preferences.put(GROOVY_COMPILER_LEVEL, level);
            try {
                preferences.flush();
            } catch (BackingStoreException e) {
                Util.log(e);
            }
        }
    }

    public List<String> getScriptFilters(IEclipsePreferences preferences) {
        if (preferences == null) preferences = getInstancePreferences();

        String value = preferences.get(GROOVY_SCRIPT_FILTERS, DEFAULT_GROOVY_SCRIPT_FILTER);
        if (value == null || value.trim().length() < 1) {
            return Collections.emptyList();
        }
        String[] tokens = value.split(",");
        return Arrays.asList(tokens);
    }

    public void setScriptFilters(IEclipsePreferences preferences, String value) {
        if (preferences == null) preferences = getInstancePreferences();

        preferences.put(GROOVY_SCRIPT_FILTERS, value);
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            Util.log(e);
        }
    }

    public void setScriptFilters(IEclipsePreferences preferences, List<String> values) {
        String value;
        if (values == null || values.isEmpty()) {
            value = "";
        } else {
            StringBuilder buffer = new StringBuilder();
            for (Iterator<String> it = values.iterator(); it.hasNext();) {
                buffer.append(it.next()); // TODO: escape commas
                if (it.hasNext()) {
                    buffer.append(',');
                }
            }
            value = buffer.toString();
        }
        setScriptFilters(preferences, value);
    }
}
