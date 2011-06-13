package org.codehaus.groovy.eclipse.dsl;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class DSLPreferencesInitializer extends AbstractPreferenceInitializer {

    public static final String AUTO_ADD_DSL_SUPPORT = "org.codehaus.groovy.eclipse.dsl.auto.add.support";
    public static final String PROJECTS_TO_IGNORE = "org.codehaus.groovy.eclipse.dsl.projects.ignore";
    public static final String DSLD_DISABLED = "org.codehaus.groovy.eclipse.dsl.disabled";
    

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = GroovyDSLCoreActivator.getDefault().getPreferenceStore();
        store.setDefault(AUTO_ADD_DSL_SUPPORT, true);
        store.setDefault(DSLD_DISABLED, false);
        store.setDefault(PROJECTS_TO_IGNORE, "");
    }
    
    public static void reset() {
        IPreferenceStore store = GroovyDSLCoreActivator.getDefault().getPreferenceStore();
        store.setValue(AUTO_ADD_DSL_SUPPORT, true);
        store.setValue(DSLD_DISABLED, false);
    }

}
