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

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;

import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.actions.CleanUpAction;
import org.eclipse.jdt.internal.ui.actions.MultiFormatAction;
import org.eclipse.jdt.internal.ui.util.ElementValidator;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.actions.FormatAllAction;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;

/**
 * @author Andrew Eisenberg
 * @created Aug 18, 2009
 */
public class FormatAllGroovyAction extends FormatAllAction {

    public static class GroovyMultiFormatAction extends MultiFormatAction {
        final FormatKind kind;
        public GroovyMultiFormatAction(IWorkbenchSite site, FormatKind kind) {
            super(site);
            this.kind = kind;
        }

        private void showUnexpectedError(CoreException e) {
            String message2= Messages.format(ActionMessages.CleanUpAction_UnexpectedErrorMessage, e.getStatus().getMessage());
            IStatus status= new Status(IStatus.ERROR, JavaUI.ID_PLUGIN, IStatus.ERROR, message2, null);
            ErrorDialog.openError(getShell(), getActionName(), null, status);
        }

        // Copied from super, but comment out section to test if on classpath
        private void run(ICompilationUnit cu) {
            //            if (!ActionUtil.isEditable(fEditor, getShell(), cu))
            //                return;
            if (cu.isReadOnly()) {
                return;
            }

            ICleanUp[] cleanUps= getCleanUps(new ICompilationUnit[] {
                    cu
            });
            if (cleanUps == null)
                return;

            if (!ElementValidator.check(cu, getShell(), getActionName(), true /* always in editor */))
                return;

            try {
                performRefactoring(new ICompilationUnit[] {
                        cu
                }, cleanUps);
            } catch (InvocationTargetException e) {
                JavaPlugin.log(e);
                if (e.getCause() instanceof CoreException)
                    showUnexpectedError((CoreException)e.getCause());
            }
        }

        @Override
        public void run(IStructuredSelection selection) {
            ICompilationUnit[] cus= getCompilationUnits(selection);
            if (cus.length == 0) {
                MessageDialog.openInformation(getShell(), getActionName(), ActionMessages.CleanUpAction_EmptySelection_description);
            } else if (cus.length == 1) {
                run(cus[0]);
            } else {
                ReflectionUtils.executePrivateMethod(CleanUpAction.class, "runOnMultuple", new Class[] { ICompilationUnit.class }, this, new Object[] { cus });
            }
        }

        /*
         * @see org.eclipse.jdt.internal.ui.actions.CleanUpAction#createCleanUps(org.eclipse.jdt.core.ICompilationUnit[])
         */
        @Override
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

        if (kind == FormatKind.INDENT_ONLY) {
            setText("Indent");
            setToolTipText("Indent Groovy file");
            setDescription("Indent Groovy file");
        } else if (kind == FormatKind.FORMAT) {
            setText("Format");
            setToolTipText("Format Groovy file");
            setDescription("Format Groovy file");
        }
    }

    /* (non-Javadoc)
     * Method declared on SelectionDispatchAction.
     */
    @Override
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
