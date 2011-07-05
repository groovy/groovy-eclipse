 /*
 * Copyright 2003-2009 the original author or authors.
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

import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.fix.CodeFormatCleanUp;
import org.eclipse.jdt.internal.ui.fix.ImportsCleanUp;
import org.eclipse.jdt.internal.ui.fix.MapCleanUpOptions;
import org.eclipse.jdt.internal.ui.javaeditor.saveparticipant.IPostSaveListener;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUp;

/**
 * Sub class of {@link CleanUpPostSaveListener} so that we can use only
 * groovy-supported post-save cleanups
 *
 * @author Andrew Eisenberg
 * @created Aug 17, 2009
 *
 */
public class GroovyCleanupPostSaveListener extends CleanUpPostSaveListener implements IPostSaveListener {

    /**
     * We only support organize imports for now.
     */
    @Override
    protected ICleanUp[] getCleanUps(Map settings, Set ids) {
        ICleanUp[] javaCleanups = JavaPlugin.getDefault().getCleanUpRegistry().createCleanUps(ids);
        CleanUpOptions options = new MapCleanUpOptions(settings);
        boolean doImports = false;
        boolean doFormat = false;
        boolean doIndent = false;

        for (int i = 0; i < javaCleanups.length; i++) {
            if (javaCleanups[i] instanceof ImportsCleanUp && options.isEnabled(CleanUpConstants.ORGANIZE_IMPORTS)) {
                doImports = true;
            } else if (javaCleanups[i] instanceof CodeFormatCleanUp) {
                if (options.isEnabled(CleanUpConstants.FORMAT_SOURCE_CODE)) {
                    // FIXKDV: commented out option below is ignored, does
                    // formatter have a function to only format portion of file?
                    // options.isEnabled(CleanUpConstants.FORMAT_SOURCE_CODE_CHANGES_ONLY))
                    doFormat = true;
                } else if (options.isEnabled(CleanUpConstants.FORMAT_CORRECT_INDENTATION)) {
                    // FIXKDV: can we support this:
                    // CleanUpConstants.FORMAT_REMOVE_TRAILING_WHITESPACES)) ?
                    doIndent = true;
                }
            }
        }
        List<ICleanUp> result = new ArrayList<ICleanUp>(2);
        if (doImports) {
            result.add(new GroovyImportsCleanup(settings));
        }
        if (doFormat) {
            result.add(new GroovyCodeFormatCleanUp(FormatKind.FORMAT));
        } else if (doIndent) {
            // indent == true && format == false
            result.add(new GroovyCodeFormatCleanUp(FormatKind.INDENT_ONLY));
        }
        return result.toArray(new ICleanUp[result.size()]);
    }
}
