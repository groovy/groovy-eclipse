/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.actions;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.actions.CleanUpAction;
import org.eclipse.jdt.internal.ui.actions.MultiOrganizeImportAction;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.util.ElementValidator;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.actions.OrganizeImportsAction;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPartSite;

public class OrganizeGroovyImportsAction extends OrganizeImportsAction {

    public OrganizeGroovyImportsAction(IWorkbenchPartSite site) {
        super(site);
    }

    public OrganizeGroovyImportsAction(JavaEditor editor) {
        super(editor);
    }

    public void run(ICompilationUnit unit) {
        if (!(unit instanceof GroovyCompilationUnit)) {
            super.run(unit);
        } else {
            try {
                JavaEditor editor = getEditor();
                if (editor == null) {
                    IEditorPart openEditor = EditorUtility.isOpenInEditor(unit);
                    if (!(openEditor instanceof JavaEditor)) {
                        getDelegate().run(new StructuredSelection(unit));
                        return;
                    }
                    editor = (JavaEditor) openEditor;
                }
                if (!ElementValidator.check(unit, getShell(), ActionMessages.OrganizeImportsAction_error_title, false))
                    return;

                OrganizeGroovyImports action = new OrganizeGroovyImports((GroovyCompilationUnit) unit, newChooseImportQuery(editor));
                boolean success = action.calculateAndApplyMissingImports();
                if (!success) {
                    IStatusLineManager manager = editor.getEditorSite().getActionBars().getStatusLineManager();
                    if (manager != null)
                        manager.setMessage(Messages.format(ActionMessages.OrganizeImportsAction_multi_error_parse, BasicElementLabels.getPathLabel(unit.getPath(), false)));
                }
            } catch (Exception e) {
                GroovyPlugin.getDefault().logError("Error organizing imports for " + unit.getElementName(), e);
            }
        }
    }

    public void run(IStructuredSelection selection) {
        MultiOrganizeImportAction delegate = getDelegate();
        ICompilationUnit[] units = delegate.getCompilationUnits(selection);
        if (units.length <= 1) {
            super.run(selection);
        } else { // avoid calling getCompilationUnits again by calling runOnMultiple directly
            ReflectionUtils.executePrivateMethod(CleanUpAction.class, "runOnMultiple", new Class[] {ICompilationUnit[].class}, delegate, new Object[] {units});
        }
    }

    protected JavaEditor getEditor() {
        return (JavaEditor) ReflectionUtils.getPrivateField(OrganizeImportsAction.class, "fEditor", this);
    }

    protected MultiOrganizeImportAction getDelegate() {
        MultiOrganizeImportAction delegate = (MultiOrganizeImportAction) ReflectionUtils.getPrivateField(OrganizeImportsAction.class, "fCleanUpDelegate", this);
        // override the final field's MultiOrganizeImportAction with our import clean-up
        MultiOrganizeImportAction override = new MultiOrganizeImportAction(getSite()) {
            protected ICleanUp[] getCleanUps(ICompilationUnit[] units) {
                return new ICleanUp[] {new GroovyImportsCleanUp()};
            }
        };
        override.setEnabled(delegate.isEnabled());
        return override;
    }

    protected IChooseImportQuery newChooseImportQuery(JavaEditor editor) {
        return (IChooseImportQuery) ReflectionUtils.executePrivateMethod(OrganizeImportsAction.class, "createChooseImportQuery", new Class[] {JavaEditor.class}, this, new Object[] {editor});
    }
}
