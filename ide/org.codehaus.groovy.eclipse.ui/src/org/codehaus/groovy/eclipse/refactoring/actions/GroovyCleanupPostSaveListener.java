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
        ICleanUp[] result= JavaPlugin.getDefault().getCleanUpRegistry().createCleanUps(ids);
        CleanUpOptions options = new MapCleanUpOptions(settings);
        boolean doImports = false;
        boolean doFormat = false;
        for (int i= 0; i < result.length; i++) {
            if (result[i] instanceof ImportsCleanUp && options.isEnabled(CleanUpConstants.ORGANIZE_IMPORTS)) {
                doImports = true;
            } else if (result[i] instanceof CodeFormatCleanUp && 
                    (options.isEnabled(CleanUpConstants.FORMAT_SOURCE_CODE) || 
                            options.isEnabled(CleanUpConstants.FORMAT_SOURCE_CODE_CHANGES_ONLY))) {
                doFormat = true;
            }
        }
        if (doImports && doFormat) {
            return new ICleanUp[] { new GroovyImportsCleanup(settings), new GroovyCodeFormatCleanUp(FormatKind.FORMAT) };
        } else if (doImports) {
            return new ICleanUp[] { new GroovyImportsCleanup(settings) };
        } else if (doFormat) {
            return new ICleanUp[] { new GroovyCodeFormatCleanUp(FormatKind.FORMAT) };
        } else {
            return new ICleanUp[0];
        }
    }
}
