/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.refactoring.actions;

import java.text.Collator;
import java.util.Comparator;

import org.codehaus.groovy.eclipse.core.GroovyCore;
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
 *
 */
public class OrganizeGroovyImportsAction extends OrganizeImportsAction {

    private static final OrganizeImportComparator ORGANIZE_IMPORT_COMPARATOR = new OrganizeImportComparator();

    private static final class OrganizeImportComparator implements Comparator<Object> {

        public int compare(Object o1, Object o2) {
            if (((String)o1).equals(o2))
                return 0;

            History history= QualifiedTypeNameHistory.getDefault();

            int pos1= history.getPosition(o1);
            int pos2= history.getPosition(o2);

            if (pos1 == pos2)
                return Collator.getInstance().compare(o1, o2);

            if (pos1 > pos2) {
                return -1;
            } else {
                return 1;
            }
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
                boolean success = new OrganizeGroovyImports((GroovyCompilationUnit) cu, createChooseImportQuery(editor))
                    .calculateAndApplyMissingImports();

                if (!success) {
                    IStatusLineManager manager = getStatusLineManager();
                    if (manager != null) {
                        manager.setErrorMessage(Messages.format(ActionMessages.OrganizeImportsAction_multi_error_parse,
                                getLocationString(cu)));
                    }
                }
            } catch (JavaModelException e) {
                GroovyCore.logException("Error with organizing imports for " + cu.getElementName(), e);
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
