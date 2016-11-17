/*
 * Copyright 2009-2016 the original author or authors.
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

import java.text.Collator;
import java.util.Comparator;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery;
import org.eclipse.jdt.internal.corext.util.History;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.QualifiedTypeNameHistory;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.dialogs.MultiElementListSelectionDialog;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.util.TypeNameMatchLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.actions.OrganizeImportsAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;

/**
 * @author Andrew Eisenberg
 * @created Aug 14, 2009
 */
public class OrganizeGroovyImportsAction extends OrganizeImportsAction {

    private static final OrganizeImportComparator ORGANIZE_IMPORT_COMPARATOR = new OrganizeImportComparator();

    private static final class OrganizeImportComparator implements Comparator<String> {

        public int compare(String o1, String o2) {
            if (o1.equals(o2))
                return 0;

            History history = QualifiedTypeNameHistory.getDefault();
            int pos1 = history.getPosition(o1);
            int pos2 = history.getPosition(o2);

            if (pos1 == pos2)
                return Collator.getInstance().compare(o1, o2);
            if (pos1 > pos2) {
                return -1;
            }
            return 1;
        }
    }

    private JavaEditor editor;

    public OrganizeGroovyImportsAction(JavaEditor editor) {
        super(editor);
        this.editor = editor;
    }

    @Override
    public void run(ICompilationUnit cu) {
        if (cu instanceof GroovyCompilationUnit) {
            try {
                OrganizeGroovyImports action = new OrganizeGroovyImports((GroovyCompilationUnit) cu, createChooseImportQuery(editor));
                boolean success = action.calculateAndApplyMissingImports();
                if (!success) {
                    IStatusLineManager manager = getStatusLineManager();
                    if (manager != null) {
                        manager.setErrorMessage(Messages.format(ActionMessages.OrganizeImportsAction_multi_error_parse, getLocationString(cu)));
                    }
                }
            } catch (JavaModelException e) {
                GroovyPlugin.getDefault().logException("Error organizing imports for " + cu.getElementName(), e);
            }
        } else {
            super.run(cu);
        }
    }

    private static String getLocationString(final ICompilationUnit cu) {
        return BasicElementLabels.getPathLabel(cu.getPath(), false);
    }

    private IChooseImportQuery createChooseImportQuery(final JavaEditor editor) {
        return new IChooseImportQuery() {
            public TypeNameMatch[] chooseImports(TypeNameMatch[][] openChoices, ISourceRange[] ranges) {
                return doChooseImports(openChoices, ranges, editor);
            }
        };
    }

    private TypeNameMatch[] doChooseImports(TypeNameMatch[][] openChoices, final ISourceRange[] ranges, final JavaEditor editor) {
        // remember selection
        ISelection sel= editor.getSelectionProvider().getSelection();
        TypeNameMatch[] result= null;
        ILabelProvider labelProvider= new TypeNameMatchLabelProvider(TypeNameMatchLabelProvider.SHOW_FULLYQUALIFIED);

        MultiElementListSelectionDialog dialog= new MultiElementListSelectionDialog(getShell(), labelProvider) {
            @Override
            protected void handleSelectionChanged() {
                super.handleSelectionChanged();
                // show choices in editor
                doListSelectionChanged(getCurrentPage(), ranges, editor);
            }
        };
        dialog.setTitle(ActionMessages.OrganizeImportsAction_selectiondialog_title);
        dialog.setMessage(ActionMessages.OrganizeImportsAction_selectiondialog_message);
        dialog.setElements(openChoices);
        dialog.setComparator(ORGANIZE_IMPORT_COMPARATOR);
        if (dialog.open() == Window.OK) {
            Object[] res= dialog.getResult();
            result= new TypeNameMatch[res.length];
            for (int i= 0; i < res.length; i++) {
                Object[] array= (Object[]) res[i];
                if (array.length > 0) {
                    result[i]= (TypeNameMatch) array[0];
                    QualifiedTypeNameHistory.remember(result[i].getFullyQualifiedName());
                }
            }
        }
        // restore selection
        if (sel instanceof ITextSelection) {
            ITextSelection textSelection= (ITextSelection) sel;
            editor.selectAndReveal(textSelection.getOffset(), textSelection.getLength());
        }
        return result;
    }

    private void doListSelectionChanged(int page, ISourceRange[] ranges, JavaEditor editor) {
        if (ranges != null && page >= 0 && page < ranges.length) {
            ISourceRange range= ranges[page];
            editor.selectAndReveal(range.getOffset(), range.getLength());
        }
    }

    private IStatusLineManager getStatusLineManager() {
        if (editor != null) {
            try {
                return editor.getEditorSite().getActionBars().getStatusLineManager();
            } catch (NullPointerException e) {
                // can ignore
            }
        }
        return null;
    }
}
