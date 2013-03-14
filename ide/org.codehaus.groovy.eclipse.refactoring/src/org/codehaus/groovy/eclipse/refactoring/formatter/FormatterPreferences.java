/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.formatter;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

/**
 * @author Mike Klenk mklenk@hsr.ch
 * @author Kris De Volder <kris.de.volder@gmail.com>
 */
public class FormatterPreferences extends FormatterPreferencesOnStore implements IFormatterPreferences {
    /**
     * Create Formatter Preferences for a given GroovyCompilationUnit. This will
     * only take a "snapshot" of the current preferences for the project.
     * FormatterPreferences is not updated automatically after preferences are
     * changed (e.g. by edits on Preferences page).
     */
    public FormatterPreferences(ICompilationUnit gunit) {
        super(preferencesFor(gunit));
    }

    public FormatterPreferences(IJavaProject project) {
        super(preferencesFor(project));
    }

    private static IPreferenceStore preferencesFor(ICompilationUnit gunit) {
        return preferencesFor(gunit.getJavaProject());
    }

    private static IPreferenceStore preferencesFor(IJavaProject javaProject) {
        IPreferenceStore javaPrefs = new JavaProjectPreferences(javaProject);
        // FIXKDV: the groovyPrefs are "global". The more logical thing to do
        // would be to have project specific prefs that can "override" global
        // settings just as in JDT.

        // FIXKDV: We want to write the following:
        // IPreferenceStore groovyPrefs = GroovyPlugin.getDefault().getPreferenceStore();
        // But unfortunately, we can't get the GroovyPlugin here because that
        // creates a circular build dependency. So we do the following instead:
        IPreferenceStore groovyPrefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.codehaus.groovy.eclipse.ui");
        IPreferenceStore javaUIprefs = JavaPlugin.getDefault().getCombinedPreferenceStore();

        return new ChainedPreferenceStore(new IPreferenceStore[] { groovyPrefs, javaPrefs, javaUIprefs });
    }
}
