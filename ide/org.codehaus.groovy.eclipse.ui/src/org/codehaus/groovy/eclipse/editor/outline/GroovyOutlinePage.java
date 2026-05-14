/*
 * Copyright 2009-2024 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor.outline;

import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.internal.ui.javaeditor.JavaOutlinePage;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;

public class GroovyOutlinePage extends JavaOutlinePage {

    private OCompilationUnit outlineCompilationUnit;

    public GroovyOutlinePage(String contextMenuID, GroovyEditor editor, OCompilationUnit unit) {
        super(contextMenuID, editor);
        outlineCompilationUnit = unit;
    }

    public OCompilationUnit getOutlineCompilationUnit() {
        return outlineCompilationUnit;
    }

    public ISourceReference getOutlineElmenetAt(int caretOffset) {
        return getOutlineCompilationUnit().getOutlineElementAt(caretOffset);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        // categories are not supported by the groovy parser and the CategoryFilter filter
        // is causing NotPresentExceptions when the top-level type goes away, so disable it
        for (ViewerFilter filter : getOutlineViewer().getFilters()) {
            if (filter.getClass().getName().contains("CategoryFilter")) {
                getOutlineViewer().removeFilter(filter);
            }
        }
    }

    public void refresh() {
        getOutlineCompilationUnit().refresh();

        JavaOutlineViewer outlineViewer = getOutlineViewer();
        if (outlineViewer != null) {
            outlineViewer.refresh();
        }
    }

    @Override
    protected void contextMenuAboutToShow(IMenuManager menu) {
    }
}
