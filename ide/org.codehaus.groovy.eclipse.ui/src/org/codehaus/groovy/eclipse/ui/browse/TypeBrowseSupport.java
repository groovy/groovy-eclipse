/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.ui.browse;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 *
 * @author Nieraj Singh
 * @created 2011-07-26
 */
public class TypeBrowseSupport {

    private IJavaProject project;

    public static final int FLAG_INTERFACE = 1 << 2;

    public static final int FLAG_CLASS = 1 << 3;

    public static final String TITLE = "Browse for Type";

    public static final String MESSAGE = "Browse and select a type";

    private Shell shell;

    private IBrowseTypeHandler handler;


    public TypeBrowseSupport(Shell shell, IJavaProject project, IBrowseTypeHandler handler) {
        this.shell = shell;
        this.project = project;
        this.handler = handler;
    }

    protected IJavaProject getJavaProject() {
        return project;
    }

    protected boolean isShellValid() {
        return shell != null && !shell.isDisposed();
    }

    protected void browseButtonPressed(Text text) {

        if (!isShellValid()) {
            return;
        }

        String pattern = text != null && !text.isDisposed() ? text.getText() : null;

        int javaSearchType = getJavaSearchType();

        if (javaSearchType == -1) {
            return;
        }

        IJavaElement[] elements = new IJavaElement[] { getJavaProject() };
        IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elements);

        FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(shell, false, null, scope, javaSearchType);
        dialog.setTitle(TITLE);
        dialog.setMessage(MESSAGE);
        dialog.setInitialPattern(pattern);

        final Text finText = text;

        if (dialog.open() == Window.OK) {
            IType type = (IType) dialog.getFirstResult();
            if (type != null) {
                String qualifiedName = type.getFullyQualifiedName();
                finText.setText(qualifiedName);
                if (handler != null) {
                    handler.handleTypeSelection(finText.getText());
                }
            }
        }
    }

    public void applySupport(Button button, Text text) {

        final Text finText = text;

        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                browseButtonPressed(finText);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                browseButtonPressed(finText);
            }

        });
    }

    public static int getJavaSearchType() {
        return IJavaSearchConstants.TYPE;
    }

}
