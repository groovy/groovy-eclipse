/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.preferencepage;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

public class SuggestionsViewer {

    private ContainerCheckedTreeViewer viewer;

    private ITreeViewerColumn[] columns;

    private ColumnSortListener columnListener;

    private ITreeViewerColumn defaultSortColumn;

    public SuggestionsViewer(ITreeViewerColumn[] columns, ITreeViewerColumn defaultSortColumn) {
        this.defaultSortColumn = defaultSortColumn;
        this.columns = columns;
    }

    protected int getConfiguration() {
        return SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK;
    }

    protected ICheckStateProvider getCheckStateProvider() {
        return null;
    }

    public void createControls(Composite parent) {

        Composite treeComposite = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(treeComposite);
        GridLayoutFactory.fillDefaults().applyTo(treeComposite);

        Tree tree = new Tree(treeComposite, getConfiguration());

        GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, getHeightHint()).applyTo(tree);

        viewer = new ContainerCheckedTreeViewer(tree);

        if (columns != null && columns.length > 0) {
            PixelConverter converter = new PixelConverter(treeComposite);
            for (ITreeViewerColumn column : columns) {
                if (column != null) {

                    TreeColumn treeColumn = new TreeColumn(tree, SWT.NONE);
                    treeColumn.setResizable(true);
                    treeColumn.setWidth(converter.convertWidthInCharsToPixels(column.getWidth()));
                    treeColumn.setText(column.getName());
                }
            }
        }

        TreeColumn sortColumn = getDefaultSortColumn();

        if (sortColumn != null) {
            tree.setSortColumn(sortColumn);
            tree.setSortDirection(SWT.UP);
        }
        TreeColumn[] treeColumns = viewer.getTree().getColumns();
        if (columnListener != null) {
            removeListeners();
        }
        columnListener = new ColumnSortListener();
        for (TreeColumn treeColumn : treeColumns) {
            treeColumn.addSelectionListener(columnListener);
        }
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        viewer.refresh();
    }

    public ContainerCheckedTreeViewer getTreeViewer() {
        return viewer;
    }

    protected int getHeightHint() {
        return 200;
    }

    protected TreeColumn getDefaultSortColumn() {
        if (defaultSortColumn != null) {
            String sortColumnName = defaultSortColumn.getName();
            if (sortColumnName != null) {
                Tree tree = viewer.getTree();
                TreeColumn[] treeColumns = tree.getColumns();
                if (treeColumns != null) {
                    for (TreeColumn treeColumn : treeColumns) {
                        if (sortColumnName.equals(treeColumn.getText())) {
                            return treeColumn;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void setChecked(Object child, boolean newState) {
        viewer.setChecked(child, newState);
    }

    public void dispose() {
        removeListeners();
    }

    protected void removeListeners() {
        if (columnListener != null) {
            for (TreeColumn column : viewer.getTree().getColumns()) {
                column.removeSelectionListener(columnListener);
            }
        }
    }

    protected class ColumnSortListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.widget instanceof TreeColumn) {
                Tree tree = viewer.getTree();
                TreeColumn selected = (TreeColumn) e.widget;
                TreeColumn sortColumn = tree.getSortColumn();

                int newDirection = SWT.UP;
                if (sortColumn == selected) {
                    newDirection = tree.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
                } else {
                    tree.setSortColumn(selected);
                }
                tree.setSortDirection(newDirection);

                viewer.refresh();
            }
        }
    }
}
