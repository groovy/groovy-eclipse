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

import java.util.Hashtable;
import java.util.Map;

import org.codehaus.groovy.eclipse.core.util.ReflectionUtils;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.ui.actions.MultiFormatAction;
import org.eclipse.jdt.ui.actions.FormatAllAction;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;

/**
 * @author Andrew Eisenberg
 * @created Aug 18, 2009
 *
 */
public class FormatAllGroovyAction extends FormatAllAction {
    
    public static enum FormatKind { INDENT_ONLY, FORMAT }
    
    
    public static class GroovyMultiFormatAction extends MultiFormatAction {
        final FormatKind kind;
        public GroovyMultiFormatAction(IWorkbenchSite site, FormatKind kind) {
            super(site);
            this.kind = kind;
        }

        /*
         * @see org.eclipse.jdt.internal.ui.actions.CleanUpAction#createCleanUps(org.eclipse.jdt.core.ICompilationUnit[])
         */
        protected ICleanUp[] getCleanUps(ICompilationUnit[] units) {
            Map settings= new Hashtable();
            settings.put(CleanUpConstants.FORMAT_SOURCE_CODE, CleanUpOptions.TRUE);

            return new ICleanUp[] {
                    new GroovyCodeFormatCleanUp(kind)
            };
        }

    }

    public FormatAllGroovyAction(IWorkbenchSite site, FormatKind kind) {
        super(site);
        ReflectionUtils.setPrivateField(FormatAllAction.class, "fCleanUpDelegate", this, new GroovyMultiFormatAction(site, kind));
    }
    
    /* (non-Javadoc)
     * Method declared on SelectionDispatchAction.
     */
    public void run(ITextSelection selection) {
        if (getSite() instanceof IEditorSite) {
            IWorkbenchPart part = ((IEditorSite) getSite()).getPart();
            if (part instanceof GroovyEditor) {
                GroovyCompilationUnit unit = (GroovyCompilationUnit) part.getAdapter(GroovyCompilationUnit.class);
                if (unit != null) {
                    super.run(new StructuredSelection(unit));
                }
            }
        }
    }

}
