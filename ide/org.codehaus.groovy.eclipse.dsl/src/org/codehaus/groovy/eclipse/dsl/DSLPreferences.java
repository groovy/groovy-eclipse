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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * Manipulator of all preferences for DSLD settings.
 */
public class DSLPreferences {

    public static final String AUTO_ADD_DSL_SUPPORT = "org.codehaus.groovy.eclipse.dsl.auto.add.support";

    /**
     * Preference key for all the scripts that are disabled in this workspace.  The value is
     * a comma separated list of {@link IResource#getFullPath()} of DSLD files.  Default value is
     * the empty string
     */
    public static final String DISABLED_SCRIPTS = "org.codehaus.groovy.eclipse.dsl.scripts.disabled";

    private DSLPreferences() {
    }

    public static String[] getDisabledScripts() {
        String disabled = GroovyDSLCoreActivator.getDefault().getPreferenceStore().getString(DISABLED_SCRIPTS);
        if (disabled == null) {
            return CharOperation.NO_STRINGS;
        }
        return disabled.split(",");
    }

    public static Set<String> getDisabledScriptsAsSet() {
        return new HashSet<>(Arrays.asList(getDisabledScripts()));
    }

    /**
     * Persists the set of all the disabled scripts,
     * <p>
     * NOTE: filters on the set, not on the get since the filtering will take extra time and a get is more time sensitive.
     */
    public static void setDisabledScripts(String[] disabled) {
        String[] filtered = filter(disabled);
        GroovyDSLCoreActivator.getDefault().getPreferenceStore().putValue(DISABLED_SCRIPTS, join(filtered));
    }

    private static String join(String[] filtered) {
        StringBuilder sb = new StringBuilder();
        if (filtered.length > 0) {
            for (String s : filtered) {
                sb.append(s);
                sb.append(',');
            }
            sb.replace(sb.length() - 1, sb.length(), "");
            return sb.toString();
        } else {
            return "";
        }
    }

    private static String[] filter(String[] disabled) {
        /* not working now
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        List<String> filtered = new ArrayList<String>(disabled.length);
        for (String string : disabled) {
            IResource r = root.getFile(new Path(string));
            if (r.getType() == IResource.FILE && r.isAccessible()) {
                filtered.add(string);
            }
        }
        return filtered.toArray(new String[filtered.size()]);
        */
        return disabled;
    }
}
