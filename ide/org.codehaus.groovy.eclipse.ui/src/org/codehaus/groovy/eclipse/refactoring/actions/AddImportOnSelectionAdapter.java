/*
 * Copyright 2009-2020 the original author or authors.
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

import java.lang.reflect.InvocationTargetException;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.corext.codemanipulation.AddImportsOperation.IChooseImportQuery;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.util.ElementValidator;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.IEditingSupport;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

public abstract class AddImportOnSelectionAdapter extends org.eclipse.jdt.internal.ui.javaeditor.AddImportOnSelectionAction {

    public AddImportOnSelectionAdapter(final CompilationUnitEditor editor) {
        super(editor);
    }

    protected abstract AddImportOperation newAddImportOperation(GroovyCompilationUnit cu, ITextSelection ts, IChooseImportQuery iq);

    @Override
    public final void run() {
        ICompilationUnit cu = getCompilationUnit();
        if (!(cu instanceof GroovyCompilationUnit)) {
            super.run();
        } else { //super.run() with newImportOperation override

            CompilationUnitEditor editor = getCompilationUnitEditor();
            Shell shell = editor.getSite().getShell();

            if (cu == null || editor == null)
                return;
            if (!editor.validateEditorInputState())
                return;
            if (!ElementValidator.checkValidateEdit(cu, shell, "Add Import"))
                return;

            ISelection selection = editor.getSelectionProvider().getSelection();
            if (selection instanceof ITextSelection) {
                ITextSelection textSelection = (ITextSelection) selection;
                IChooseImportQuery typeQuery = newChooseImportQuery(shell);
                IEditingSupport helper = newEditingSupport(textSelection, typeQuery);
                AddImportOperation operation = newAddImportOperation((GroovyCompilationUnit) cu, textSelection, typeQuery);
                try {
                    register(helper);
                    IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
                    progressService.runInUI(editor.getSite().getWorkbenchWindow(), operation, cu.getJavaProject().getResource());
                    IStatus status = operation.getStatus();
                    if (!status.isOK()) {
                        IStatusLineManager manager = editor.getEditorSite().getActionBars().getStatusLineManager();
                        if (manager != null) {
                            manager.setMessage(status.getMessage());
                        }
                    }
                } catch (InvocationTargetException e) {
                    ExceptionHandler.handle(e, shell, "Add Import", null);
                } catch (InterruptedException e) {
                    // Do nothing. Operation has been canceled.
                } finally {
                    unregister(helper);
                }
            }
        }
    }

    private ICompilationUnit getCompilationUnit() {
        CompilationUnitEditor editor = getCompilationUnitEditor();
        return editor == null ? null : JavaPlugin.getDefault()
            .getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
    }

    private CompilationUnitEditor getCompilationUnitEditor() {
        CompilationUnitEditor editor = ReflectionUtils.getPrivateField(
            org.eclipse.jdt.internal.ui.javaeditor.AddImportOnSelectionAction.class, "fEditor", this);
        return editor;
    }

    private IChooseImportQuery newChooseImportQuery(final Shell shell) {
        return ReflectionUtils.invokeConstructor(SELECT_TYPE_QUERY, new Class[] {Shell.class}, new Object[] {shell});
    }

    private IEditingSupport newEditingSupport(final ITextSelection textSelection, final IChooseImportQuery typeQuery) {
        return ReflectionUtils.executePrivateMethod(
            org.eclipse.jdt.internal.ui.javaeditor.AddImportOnSelectionAction.class,
            "createViewerHelper", new Class[] {ITextSelection.class, SELECT_TYPE_QUERY}, this, new Object[] {textSelection, typeQuery});
        /*return new IEditingSupport() {
            public boolean isOriginator(DocumentEvent event, IRegion subjectRegion) {
                return subjectRegion.getOffset() <= textSelection.getOffset() + textSelection.getLength() &&
                        textSelection.getOffset() <= subjectRegion.getOffset() + subjectRegion.getLength();
            }
            public boolean ownsFocusShell() {
                return typeQuery.isShowing();
            }
        };*/
    }

    private void register(IEditingSupport helper) {
        ReflectionUtils.executePrivateMethod(
            org.eclipse.jdt.internal.ui.javaeditor.AddImportOnSelectionAction.class,
            "registerHelper", new Class[] {IEditingSupport.class}, this, new Object[] {helper});
        /*ISourceViewer viewer = editor.getViewer();
        if (viewer instanceof IEditingSupportRegistry) {
            IEditingSupportRegistry registry = (IEditingSupportRegistry) viewer;
            registry.register(helper);
        }*/
    }

    private void unregister(IEditingSupport helper) {
        ReflectionUtils.executePrivateMethod(
            org.eclipse.jdt.internal.ui.javaeditor.AddImportOnSelectionAction.class,
            "deregisterHelper", new Class[] {IEditingSupport.class}, this, new Object[] {helper});
        /*ISourceViewer viewer = editor.getViewer();
        if (viewer instanceof IEditingSupportRegistry) {
            IEditingSupportRegistry registry = (IEditingSupportRegistry) viewer;
            registry.unregister(helper);
        }*/
    }

    private static Class<? extends IChooseImportQuery> SELECT_TYPE_QUERY;
    static {
        try {
            SELECT_TYPE_QUERY = Class.class.cast(Class.forName(
                "org.eclipse.jdt.internal.ui.javaeditor.AddImportOnSelectionAction$SelectTypeQuery"));
        } catch (Exception e) {
            GroovyPlugin.getDefault().logError("Failed to locate SelectTypeQuery", e);
        }
    }

    //--------------------------------------------------------------------------

    public interface AddImportOperation extends org.eclipse.jface.operation.IRunnableWithProgress {
        IStatus getStatus();
    }
}
