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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.MethodParameter;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public class MethodParameterTable extends AbstractControlManager {

    enum ColumnTypes {
        NAME("Name", 300), TYPE("Type", 300);
        private String label;

        private int weight;

        private ColumnTypes(String label, int weight) {
            this.label = label;
            this.weight = weight;
        }

        public String getLabel() {
            return label;
        }

        public int getWeight() {
            return weight;
        }

    }

    /**
     * Should never be null. Use empty list if no arguments are present
     */
    private List<MethodParameter> parameters;

    private TableViewer viewer;

    private IJavaProject project;

    private boolean useNamedArguments;

    public MethodParameterTable(IJavaProject project, List<MethodParameter> parameters, boolean useNamedArguments) {
        this.parameters = parameters;
        if (parameters == null) {
            this.parameters = new ArrayList<MethodParameter>();
        }
        this.project = project;
        this.useNamedArguments = useNamedArguments;
    }

    protected int getLabelRowColumns() {
        return 1;
    }

    protected IDialogueControlDescriptor[] getTableButtonDescriptors() {
        return new IDialogueControlDescriptor[] { ControlTypes.ADD, ControlTypes.REMOVE, ControlTypes.EDIT, ControlTypes.UP,
                ControlTypes.DOWN };
    }

    protected Map<Control, IDialogueControlDescriptor> createOperationButtonArea(Composite parent) {

        Composite buttons = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().align(GridData.CENTER, GridData.BEGINNING).applyTo(buttons);

        GridLayoutFactory.fillDefaults().applyTo(buttons);

        IDialogueControlDescriptor[] types = getTableButtonDescriptors();
        Map<Control, IDialogueControlDescriptor> opButtons = new HashMap<Control, IDialogueControlDescriptor>();
        for (IDialogueControlDescriptor type : types) {
            Button button = createSelectionButton(buttons, type);
            if (button != null) {
                opButtons.put(button, type);
            }
        }
        return opButtons;
    }

    protected Button createSelectionButton(Composite parent, IDialogueControlDescriptor type) {
        if (type == null) {
            return null;
        }

        Button button = new Button(parent, SWT.PUSH);
        button.setText(type.getLabel());
        button.setData(type);

        Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        int widthHint = 0;

        GridDataFactory.fillDefaults().hint(Math.max(widthHint, minSize.x), SWT.DEFAULT).applyTo(button);

        button.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                Object obj = e.getSource();
                if (obj instanceof Button) {
                    IDialogueControlDescriptor descriptor = (IDialogueControlDescriptor) ((Button) obj).getData();
                    handleButtonSelection(descriptor);
                }
            }

        });
        return button;
    }

    protected void handleButtonSelection(IDialogueControlDescriptor type) {
        if (!(type instanceof ControlTypes)) {
            return;
        }
        ControlTypes controlType = (ControlTypes) type;
        switch (controlType) {
            case ADD:
                addElement();
                break;
            case REMOVE:
                removeElement();
                break;
            case EDIT:
                editElement();
                break;
            case UP:
                int selectionIndex = viewer.getTable().getSelectionIndex();
                // can only move selection if it is not already at the top
                if (selectionIndex > 0) {
                    MethodParameter element = parameters.remove(selectionIndex);
                    // selectionIndex will never be less than zero
                    parameters.add(selectionIndex - 1, element);
                    refreshTable();
                }
                break;
            case DOWN:
                selectionIndex = viewer.getTable().getSelectionIndex();
                // Can only move down if the selection is second to last
                if (selectionIndex >= 0 && selectionIndex < parameters.size() - 1) {
                    MethodParameter element = parameters.remove(selectionIndex);
                    parameters.add(selectionIndex + 1, element);
                    refreshTable();
                }
                break;

        }

    }

    protected int getViewerConfiguration() {
        return SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL;
    }

    protected int getViewerHeightHint() {
        return 200;
    }

    protected TableViewer createTableViewer(Composite parent) {
        Composite treeComposite = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(treeComposite);
        GridLayoutFactory.fillDefaults().applyTo(treeComposite);

        Table table = new Table(treeComposite, getViewerConfiguration());

        GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, getViewerHeightHint()).applyTo(table);

        viewer = new TableViewer(table);

        ColumnTypes[] values = ColumnTypes.values();
        for (ColumnTypes column : values) {
            if (column != null) {
                TableColumn tableColumn = new TableColumn(table, SWT.NONE);

                tableColumn.setResizable(true);

                tableColumn.setText(column.getLabel());
                tableColumn.setWidth(column.getWeight());
            }
        }
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        setTableProviders(viewer);

        return viewer;
    }

    protected void setTableProviders(final TableViewer viewer) {

        viewer.setContentProvider(new IStructuredContentProvider() {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

            }

            public void dispose() {

            }

            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof List) {
                    return ((List<?>) inputElement).toArray();
                }
                return null;
            }
        });

        viewer.setLabelProvider(new ColumnLabelProvider() {

            public void update(ViewerCell cell) {

                Object element = cell.getElement();
                int index = cell.getColumnIndex();
                cell.setText(getColumnText(element, index));
            }

            public String getColumnText(Object element, int index) {
                String text = null;
                if (element instanceof MethodParameter) {
                    ColumnTypes[] values = ColumnTypes.values();
                    if (index >= 0 && index < values.length) {
                        ColumnTypes type = values[index];
                        MethodParameter arg = (MethodParameter) element;

                        switch (type) {
                            case NAME:
                                text = arg.getName();
                                break;
                            case TYPE:
                                text = arg.getType();
                                break;

                        }
                    }
                }
                return text;
            }

        });

        viewer.setInput(parameters);
    }

    protected MethodParameter getSelectedElement() {
        ISelection selection = viewer.getSelection();
        if (selection instanceof IStructuredSelection) {
            Object selectObj = ((IStructuredSelection) selection).getFirstElement();
            if (selectObj instanceof MethodParameter) {
                return (MethodParameter) selectObj;
            }
        }

        return null;
    }

    protected MethodParameter getElement(int index) {
        if (index < viewer.getTable().getItemCount()) {
            return getArgumentElementFromSelectionObject(viewer.getElementAt(index));
        }
        return null;
    }

    protected void addElement() {
        MethodParameterDialogue dialogue = new MethodParameterDialogue(getShell(), project, null, parameters);
        if (dialogue.open() == Window.OK) {

            MethodParameter parameter = dialogue.getMethodParameter();
            if (parameter != null) {

                int selectionIndex = viewer.getTable().getSelectionIndex();

                // Add element at given selection index
                if (selectionIndex >= 0) {
                    parameters.add(selectionIndex, parameter);
                } else {
                    parameters.add(parameter);
                }
            }

            refreshTable();
        }

    }

    protected void editElement() {
        MethodParameter selected = getSelectedElement();
        if (selected != null) {
            MethodParameterDialogue dialogue = new MethodParameterDialogue(getShell(), project, selected, parameters);
            if (dialogue.open() == Window.OK) {
                MethodParameter editedParameter = dialogue.getMethodParameter();
                if (editedParameter != null) {
                    int selectionIndex = viewer.getTable().getSelectionIndex();
                    parameters.remove(selected);

                    // Add element at given selection index
                    if (selectionIndex >= 0) {
                        parameters.add(selectionIndex, editedParameter);
                    } else {
                        parameters.add(editedParameter);
                    }
                }

                refreshTable();
            }
        }
    }

    protected void removeElement() {
        MethodParameter selected = getSelectedElement();
        if (selected != null) {
            for (int i = 0; i < parameters.size(); i++) {
                MethodParameter item = parameters.get(i);
                if (item.equals(selected)) {
                    parameters.remove(i);
                }
            }
        }
        refreshTable();
    }

    protected void refreshTable() {
        viewer.getTable().setFocus();
        viewer.setInput(parameters);
        viewer.refresh(true);
    }

    protected MethodParameter getArgumentElementFromSelectionObject(Object element) {
        MethodParameter arg = null;
        if (element instanceof MethodParameter) {
            arg = (MethodParameter) element;

        } else if (element instanceof TableItem) {
            TableItem item = (TableItem) element;
            Object dataOb = item.getData();
            if (dataOb instanceof MethodParameter) {
                arg = (MethodParameter) dataOb;
            }
        }
        return arg;
    }

    public void changeControlValue(ControlSelectionEvent event) {
        // nothing for now
    }

    protected Button createCheckButton(Composite parent, IDialogueControlDescriptor type) {
        if (type == null) {
            return null;
        }

        Button button = new Button(parent, SWT.CHECK);
        button.setText(type.getLabel());
        button.setData(type);

        Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        int widthHint = 0;

        GridDataFactory.fillDefaults().hint(Math.max(widthHint, minSize.x), SWT.DEFAULT).applyTo(button);

        button.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                Object obj = e.getSource();
                if (obj instanceof Button) {
                    notifyControlChange(new Boolean(((Button) obj).getSelection()), (Button) obj);
                }
            }

        });
        return button;
    }

    protected Map<Control, IDialogueControlDescriptor> createManagedControls(Composite parent) {

        Composite viewerArea = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(viewerArea);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewerArea);

        Map<Control, IDialogueControlDescriptor> allControls = new HashMap<Control, IDialogueControlDescriptor>();

        TableViewer viewer = createTableViewer(viewerArea);
        if (viewer != null) {
            allControls.put(viewer.getTable(), ControlTypes.PARAMETERS);
        }

        Map<Control, IDialogueControlDescriptor> buttonControls = createOperationButtonArea(viewerArea);

        if (buttonControls != null) {
            allControls.putAll(buttonControls);
        }

        Button useNamedButton = createCheckButton(parent, ControlTypes.USE_NAMED_ARGUMENTS);
        if (useNamedButton != null) {
            useNamedButton.setSelection(useNamedArguments);
            allControls.put(useNamedButton, ControlTypes.USE_NAMED_ARGUMENTS);
        }

        return allControls;
    }

    public List<MethodParameter> getMethodParameter() {
        return parameters;
    }

}
