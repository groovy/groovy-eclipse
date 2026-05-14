/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class DSLPreferencesInitializer extends AbstractPreferenceInitializer {

    public static final String AUTO_ADD_DSL_SUPPORT = GroovyDSLCoreActivator.PLUGIN_ID + ".auto.add.support";
    public static final String PROJECTS_TO_IGNORE = GroovyDSLCoreActivator.PLUGIN_ID + ".projects.ignore";
    public static final String DSLD_DISABLED = GroovyDSLCoreActivator.PLUGIN_ID + ".disabled";

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = GroovyDSLCoreActivator.getDefault().getPreferenceStore();
        store.setDefault(DSLD_DISABLED, Boolean.getBoolean("greclipse.dsld.disabled"));
        store.setDefault(AUTO_ADD_DSL_SUPPORT, true);
        store.setDefault(PROJECTS_TO_IGNORE, "");
    }

    public static void reset() {
        IPreferenceStore store = GroovyDSLCoreActivator.getDefault().getPreferenceStore();
        store.setValue(DSLD_DISABLED, Boolean.getBoolean("greclipse.dsld.disabled"));
        store.setValue(AUTO_ADD_DSL_SUPPORT, true);
    }
}
