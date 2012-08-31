/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor.outline;

import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.javaeditor.JavaOutlinePage;
import org.eclipse.jdt.internal.ui.viewsupport.SourcePositionComparator;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPageSite;

/**
 * @author Maxime Hamm
 * @created 7 avr. 2011
 */
public class GroovyOutlinePage extends JavaOutlinePage {

    private OCompilationUnit outlineUnit = null;

    public GroovyOutlinePage(String contextMenuID, GroovyEditor editor, OCompilationUnit unit) {
        super(contextMenuID, editor);
        outlineUnit = unit;
    }

    public void refresh() {
        initializeViewer();

        outlineUnit.refresh();

        JavaOutlineViewer outlineViewer = getOutlineViewer();
        if (outlineViewer != null) {
            outlineViewer.refresh();
        }
    }

    public OCompilationUnit getOutlineCompilationUnit() {
        return outlineUnit;
    }

    @Override
    protected void contextMenuAboutToShow(IMenuManager menu) {
        // none
    }

    private boolean isInitialized = false;
    private void initializeViewer() {
        if (isInitialized) {
            return;
        }
        // remove actions
        IPageSite site = getSite();
        if (site != null) {
            IActionBars actionBars = site.getActionBars();
            if (actionBars != null) {
                IToolBarManager toolBarManager = actionBars.getToolBarManager();
                if (toolBarManager != null) {
                    toolBarManager.removeAll();
                    toolBarManager.add(new GroovyLexicalSortingAction());
                    toolBarManager.update(true);
                }
            }
        }

        isInitialized = true;
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        // categories are not supported by the groovy parser.
        // and the CategoryFilter filter is causing NotPresentExceptions when the
        // top level type goes away, so disable it.
        ViewerFilter[] filters = getOutlineViewer().getFilters();
        for (ViewerFilter filter : filters) {
            if (filter.getClass().getName().contains("CategoryFilter")) {
                getOutlineViewer().removeFilter(filter);

            }
        }
    }

    /**
     * @param caretOffset
     * @return
     */
    public ISourceReference getOutlineElmenetAt(int caretOffset) {
        return getOutlineCompilationUnit().getOutlineElementAt(caretOffset);
    }

    /****************************************************************
     * @author Maxime HAMM
     * @created 7 avr. 2011
     */
    public class GroovyLexicalSortingAction extends Action {

        private JavaElementComparator fComparator = new JavaElementComparator();

        private SourcePositionComparator fSourcePositonComparator = new SourcePositionComparator();

        public GroovyLexicalSortingAction() {
            super();
            PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.LEXICAL_SORTING_OUTLINE_ACTION);
            setText("Link with Editor");
            JavaPluginImages.setLocalImageDescriptors(this, "alphab_sort_co.gif"); //$NON-NLS-1$

            boolean checked = JavaPlugin.getDefault().getPreferenceStore().getBoolean("LexicalSortingAction.isChecked"); //$NON-NLS-1$
            valueChanged(checked, false);
        }

        @Override
        public void run() {
            valueChanged(isChecked(), true);
        }

        private void valueChanged(final boolean on, boolean store) {
            setChecked(on);
            BusyIndicator.showWhile(getOutlineViewer().getControl().getDisplay(), new Runnable() {
                public void run() {
                    if (on) {
                        getOutlineViewer().setComparator(fComparator);
                    } else {
                        getOutlineViewer().setComparator(fSourcePositonComparator);
                    }
                }
            });

            if (store)
                JavaPlugin.getDefault().getPreferenceStore().setValue("LexicalSortingAction.isChecked", on); //$NON-NLS-1$
        }
    }

}
