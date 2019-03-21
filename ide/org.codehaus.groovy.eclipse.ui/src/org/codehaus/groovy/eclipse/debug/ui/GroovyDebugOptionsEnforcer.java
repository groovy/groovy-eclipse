/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.debug.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugOptionsManager;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Checks that the enhanced Groovy debug options are all enabled.
 *
 * See: GRECLIPSE-791
 *
 * Step filtering,
 * Logical structure
 * Grey-out stack frames
 */
public class GroovyDebugOptionsEnforcer {

    private final IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();

    public void force() {
        forceDetailFormatter();
        forceLogicalStructure();
        forceStepThroughFilters();
    }

    public void maybeForce(IPreferenceStore store) {
        if (store.getBoolean(PreferenceConstants.GROOVY_DEBUG_FORCE_DEBUG_OPTIONS_ON_STARTUP)) {
            force();
            store.setValue(PreferenceConstants.GROOVY_DEBUG_FORCE_DEBUG_OPTIONS_ON_STARTUP, false);
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Ensures that Reference objects in closures are formatted nicely in the variables view.
     */
    private void forceDetailFormatter() {
        new ForceDetailFormatter().forceReferenceFormatter();
    }

    /**
     * TODO
     */
    private void forceLogicalStructure() {
        //IPresentationContext context = new DebugModelPresentationContext(IDebugUIConstants.ID_VARIABLE_VIEW, null, null);
        //context.setProperty(VariablesView.PRESENTATION_SHOW_LOGICAL_STRUCTURES, true);
    }

    /**
     * TODO
     */
    private void forceStepThroughFilters() {
        String groovyInternalPackages = GroovyPlugin.getDefault().getPreferenceStore().getDefaultString(PreferenceConstants.GROOVY_DEBUG_FILTER_LIST);
        Set<String> defaultFilters = Arrays.stream(JavaDebugOptionsManager.parseList(groovyInternalPackages)).map(p -> p + ".*").collect(Collectors.toSet());

        // add defaults to active list
        Set<String> activeFilters = new TreeSet<>(defaultFilters);
        Collections.addAll(activeFilters, JavaDebugOptionsManager.parseList(
            preferenceStore.getString(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST)));
        activeFilters.remove("java.lang.ClassLoader");
        activeFilters.remove("java.lang.reflect.*");
        activeFilters.add("java.*");

        // remove defaults from inactive list
        Set<String> inactiveFilters = new TreeSet<>();
        Collections.addAll(inactiveFilters, JavaDebugOptionsManager.parseList(
            preferenceStore.getString(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST)));
        inactiveFilters.removeAll(activeFilters);

        //

        DebugPlugin.setUseStepFilters(true);

        preferenceStore.setValue(IJDIPreferencesConstants.PREF_STEP_THRU_FILTERS, true);

        preferenceStore.setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST,
            JavaDebugOptionsManager.serializeList(activeFilters.toArray(new String[activeFilters.size()])));

        preferenceStore.setValue(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST,
            JavaDebugOptionsManager.serializeList(inactiveFilters.toArray(new String[inactiveFilters.size()])));
    }
}
