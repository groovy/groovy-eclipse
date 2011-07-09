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
package org.codehaus.groovy.eclipse.refactoring.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.refactoring.PreferenceConstants;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.fix.CodeFormatCleanUp;
import org.eclipse.jdt.internal.ui.fix.ImportsCleanUp;
import org.eclipse.jdt.internal.ui.fix.MapCleanUpOptions;
import org.eclipse.jdt.internal.ui.javaeditor.saveparticipant.IPostSaveListener;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Sub class of {@link CleanUpPostSaveListener} so that we can use only
 * groovy-supported post-save cleanups
 *
 * @author Andrew Eisenberg
 * @created Aug 17, 2009
 */
public class GroovyCleanupPostSaveListener extends CleanUpPostSaveListener implements IPostSaveListener {

    @Override
    protected ICleanUp[] getCleanUps(Map settings, Set ids) {
        ICleanUp[] javaCleanUps = JavaPlugin.getDefault().getCleanUpRegistry().createCleanUps(ids);
        CleanUpOptions options = new MapCleanUpOptions(settings);
        boolean doImports = false;
        boolean doFormat = false;
        boolean doIndent = false;

        IPreferenceStore groovyPreferences = GroovyPlugin.getDefault().getPreferenceStore();
        boolean doSemicolonRemoval = groovyPreferences.getBoolean(PreferenceConstants.GROOVY_SAVE_ACTION_REMOVE_UNNECESSARY_SEMICOLONS);
        boolean doWhitespaceRemoval = options.isEnabled(CleanUpConstants.FORMAT_REMOVE_TRAILING_WHITESPACES);

        for (ICleanUp cleanup : javaCleanUps) {
            if (cleanup instanceof ImportsCleanUp && options.isEnabled(CleanUpConstants.ORGANIZE_IMPORTS)) {
                doImports = true;
            } else if (cleanup instanceof CodeFormatCleanUp) {
                if (options.isEnabled(CleanUpConstants.FORMAT_SOURCE_CODE)) {
                    // FIXKDV: commented out option below is ignored, does
                    // formatter have a function to only format portion of file?
                    // options.isEnabled(CleanUpConstants.FORMAT_SOURCE_CODE_CHANGES_ONLY))
                    doFormat = true;
                } else if (options.isEnabled(CleanUpConstants.FORMAT_CORRECT_INDENTATION)) {
                    doIndent = true;
                }
            }
        }

        List<ICleanUp> groovyCleanUps = new ArrayList<ICleanUp>();

        if (doImports) {
            groovyCleanUps.add(new GroovyImportsCleanUp());
        }
        if (doFormat) {
            groovyCleanUps.add(new GroovyCodeFormatCleanUp(FormatKind.FORMAT));
        } else if (doIndent) {
            // indent == true && format == false
            groovyCleanUps.add(new GroovyCodeFormatCleanUp(FormatKind.INDENT_ONLY));
        }

        if (doSemicolonRemoval) {
            groovyCleanUps.add(new UnnecessarySemicolonsCleanUp());
        }

        if (doWhitespaceRemoval) {
            groovyCleanUps.add(new TrailingWhitespacesCleanUp());
        }

        return groovyCleanUps.toArray(new ICleanUp[groovyCleanUps.size()]);
    }
}
