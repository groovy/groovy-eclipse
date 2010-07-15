/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.debug.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 *
 * @author andrew
 * @created Jul 15, 2010
 */
public class GroovyDebugOptionsEnforcer {

    private final static String[] DEFAULT_GROOVY_STEP_FILTERS = { "groovy.lang.*", "org.codehaus.groovy.*", "java.lang.reflect.*",
            "sun.misc.*", "groovy.ui.*", "sun.reflect.*" };

    private IPreferenceStore preferenceStore;

    public GroovyDebugOptionsEnforcer() {
        preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
    }

    public void force() {
        forceLogicalStructure();
        forceStepThroughFilters();
        forceUseStepFilters();
        forceGroovyStepFilters();
        forceDetailFormatter();
    }

    private void forceDetailFormatter() {
        // ensure that Reference objects in closures are formatted nicely in the
        // variables view
        new ForceDetailFormatter().forceReferenceFormatter();
    }

    private void forceLogicalStructure() {
        // IPresentationContext context = new
        // DebugModelPresentationContext(IDebugUIConstants.ID_VARIABLE_VIEW,
        // null, null);
        // context.setProperty(VariablesView.PRESENTATION_SHOW_LOGICAL_STRUCTURES,
        // true);
    }

    private void forceStepThroughFilters() {
        preferenceStore.setValue(IJDIPreferencesConstants.PREF_STEP_THRU_FILTERS, true);
    }

    private void forceUseStepFilters() {
        DebugPlugin.setUseStepFilters(true);
    }

    private void forceGroovyStepFilters() {
        String active = preferenceStore.getString(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST);
        String[] activeArr = JavaDebugOptionsManager.parseList(active);
        List<String> activeList = new ArrayList<String>(Arrays.asList(activeArr));
        for (String filter : DEFAULT_GROOVY_STEP_FILTERS) {
            if (!activeList.contains(filter)) {
                activeList.add(filter);
            }
        }

        String newActive = JavaDebugOptionsManager.serializeList(activeList.toArray(new String[0]));
        preferenceStore.setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, newActive);

        String inactive = preferenceStore.getString(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST);
        String[] inactiveArr = JavaDebugOptionsManager.parseList(inactive);
        List<String> inactiveList = new ArrayList<String>(Arrays.asList(inactiveArr));
        for (String filter : DEFAULT_GROOVY_STEP_FILTERS) {
            // remove all dups
            while (inactiveList.remove(filter)) {}
        }

        String newInactive = JavaDebugOptionsManager.serializeList(inactiveList.toArray(new String[0]));
        preferenceStore.setValue(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST, newInactive);
    }

    public void maybeForce(IPreferenceStore store) {
        if (store.getBoolean(PreferenceConstants.GROOVY_DEBUG_FORCE_DEBUG_OPTIONS_ON_STARTUP)) {
            force();
            store.setValue(PreferenceConstants.GROOVY_DEBUG_FORCE_DEBUG_OPTIONS_ON_STARTUP, false);
        }
    }
}
