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

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovyMethodSuggestion;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovyMethodSuggestion.MethodParameter;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.ui.InferencingContributionDialogue.ControlTypes;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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
public class ParameterTable extends AbstractControl {

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
    private List<ParameterTableElement> arguments;

    private TableViewer viewer;

    public ParameterTable() {
        this.arguments = new ArrayList<ParameterTableElement>();
    }

    public void setInput(List<MethodParameter> arguments) {
        if (arguments != null) {
            this.arguments = new ArrayList<ParameterTable.ParameterTableElement>();
            for (MethodParameter arg : arguments) {
                this.arguments.add(new ParameterTableElement(arg));
            }
        }
    }

    protected int getLabelRowColumns() {
        return 1;
    }

    protected IDialogueControlDescriptor[] getTableButtonDescriptors() {
        return new IDialogueControlDescriptor[] { ControlTypes.ADD, ControlTypes.REMOVE, ControlTypes.UP, ControlTypes.DOWN };
    }

    protected Map<IDialogueControlDescriptor, Button> createOperationButtonArea(Composite parent) {

        Composite buttons = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().align(GridData.CENTER, GridData.BEGINNING).applyTo(buttons);

        GridLayoutFactory.fillDefaults().applyTo(buttons);

        IDialogueControlDescriptor[] types = getTableButtonDescriptors();
        Map<IDialogueControlDescriptor, Button> opButtons = new HashMap<IDialogueControlDescriptor, Button>();
        for (IDialogueControlDescriptor type : types) {
            Button button = createSelectionButton(buttons, type);
            if (button != null) {
                opButtons.put(type, button);
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
                    IDialogueControlDescriptor type = (IDialogueControlDescriptor) ((Button) obj).getData();
                    handleButtonSelection(type);
                }
            }

        });
        return button;
    }

    protected void handleButtonSelection(IDialogueControlDescriptor type) {
        if (type == null) {
            return;
        }

        if (type == ControlTypes.ADD) {
            addElement();
        } else if (type == ControlTypes.REMOVE) {
            removeElement();
        } else if (type == ControlTypes.UP) {

            int selectionIndex = viewer.getTable().getSelectionIndex();
            if (selectionIndex > 0) {
                ParameterTableElement element = arguments.remove(selectionIndex);
                arguments.add(selectionIndex - 1, element);
                setInput();
            }

        } else if (type == ControlTypes.DOWN) {
            int selectionIndex = viewer.getTable().getSelectionIndex();
            if (selectionIndex >= 0 && selectionIndex < arguments.size() - 1) {
                ParameterTableElement element = arguments.remove(selectionIndex);
                arguments.add(selectionIndex + 1, element);
                setInput();
            }
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

        addCellEditorSupport(viewer);

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
                if (element instanceof ParameterTableElement) {
                    ColumnTypes[] values = ColumnTypes.values();
                    if (index >= 0 && index < values.length) {
                        ColumnTypes type = values[index];
                        ParameterTableElement arg = (ParameterTableElement) element;

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

        viewer.getTable().addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.F2 && e.stateMask == SWT.NONE) {

                  
                    e.doit = false;
                }
            }
        });

        viewer.getTable().addMouseListener(new MouseListener() {

            public void mouseUp(MouseEvent e) {

            }

            public void mouseDown(MouseEvent e) {

            }

            public void mouseDoubleClick(MouseEvent e) {

     
            }
        });

        viewer.setInput(arguments);
    }

    protected ParameterTableElement getSelectedElement() {
        ISelection selection = viewer.getSelection();
        if (selection instanceof IStructuredSelection) {
            Object selectObj = ((IStructuredSelection) selection).getFirstElement();
            if (selectObj instanceof ParameterTableElement) {
                return (ParameterTableElement) selectObj;
            }
        }

        return null;
    }

    protected ParameterTableElement getElement(int index) {
        if (index < viewer.getTable().getItemCount()) {
            return getArgumentElementFromSelectionObject(viewer.getElementAt(index));
        }
        return null;
    }



    protected void addElement() {
        ParameterTableElement selected = new ParameterTableElement();

        int selectionIndex = viewer.getTable().getSelectionIndex();

        // Add element at given selection index
        if (selectionIndex >= 0) {
            arguments.add(selectionIndex, selected);
        } else {
            arguments.add(selected);
        }

        viewer.editElement(selected, ColumnTypes.NAME.ordinal());
        setInput();
    }
    
    protected void editElement() {
        ParameterTableElement selected = getSelectedElement();
        if (selected != null) {
            viewer.editElement(selected, ColumnTypes.NAME.ordinal());
        }
        setInput();
    }

    protected void removeElement() {
        ParameterTableElement selected = getSelectedElement();
        if (selected != null) {
            for (int i = 0; i < arguments.size(); i++) {
                ParameterTableElement item = arguments.get(i);
                if (item.equals(selected)) {
                    arguments.remove(i);
                }
            }
        }
        setInput();
    }

    protected void moveUp() {
        ParameterTableElement selected = getSelectedElement();
        if (selected != null) {

        }
    }

    protected void moveDown() {
        ParameterTableElement selected = getSelectedElement();
        if (selected != null) {

        }
    }

    protected void setInput() {
        viewer.getTable().setFocus();
        viewer.setInput(arguments);
        viewer.refresh(true);
    }

    protected ParameterTableElement getArgumentElementFromSelectionObject(Object element) {
        ParameterTableElement arg = null;
        if (element instanceof ParameterTableElement) {
            arg = (ParameterTableElement) element;

        } else if (element instanceof TableItem) {
            TableItem item = (TableItem) element;
            Object dataOb = item.getData();
            if (dataOb instanceof ParameterTableElement) {
                arg = (ParameterTableElement) dataOb;
            }
        }
        return arg;
    }

    /**
     * Mutable element in a table as the argument elements can be modified via
     * cell editors.
     * 
     * @author ns
     * @created 2011-05-11
     */
    static class ParameterTableElement {

        private String name;

        private String type;

        public ParameterTableElement(MethodParameter argument) {
            this.name = argument.getName();
            this.type = argument.getType();
        }

        public ParameterTableElement() {
            this.name = "";
            this.type = "";
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            ParameterTableElement other = (ParameterTableElement) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }

            } else if (!name.equals(other.name)) {
                return false;
            }

            if (type == null) {
                if (other.type != null) {
                    return false;
                }

            } else if (!type.equals(other.type)) {
                return false;
            }

            return true;
        }

    }

    @Override
    public void changeControlValue(ControlSelectionEvent event) {
        // TODO Auto-generated method stub

    }

    protected void addCellEditorSupport(TableViewer viewer) {
        int totalColumns = ColumnTypes.values().length;

        CellEditor[] cellEditors = null;

        if (totalColumns == 2) {
            cellEditors = new CellEditor[ColumnTypes.values().length];
            cellEditors[ColumnTypes.NAME.ordinal()] = new TextCellEditor(viewer.getTable());
            cellEditors[ColumnTypes.TYPE.ordinal()] = new TextCellEditor(viewer.getTable());
        }

        if (cellEditors != null) {
            viewer.setCellEditors(cellEditors);
            viewer.setCellModifier(new ICellModifier() {

                public void modify(Object element, String property, Object value) {
                    ParameterTableElement arg = getArgumentElementFromSelectionObject(element);

                    if (arg != null) {
                        if (property.equals(ColumnTypes.NAME.getLabel())) {
                            arg.setName((String) value);
                        } else if (property.equals(ColumnTypes.TYPE.getLabel())) {
                            arg.setType((String) value);
                        }
                    }

                }

                public Object getValue(Object element, String property) {

                    ParameterTableElement arg = getArgumentElementFromSelectionObject(element);

                    if (arg != null) {
                        if (property.equals(ColumnTypes.NAME.getLabel())) {
                            return arg.getName();
                        } else if (property.equals(ColumnTypes.TYPE.getLabel())) {
                            return arg.getType();
                        }
                    }
                    return null;
                }

                public boolean canModify(Object element, String property) {

                    return getArgumentElementFromSelectionObject(element) != null;
                }
            });

            ColumnTypes[] types = ColumnTypes.values();
            int length = types.length;
            String[] columnProperties = new String[length];

            for (int i = 0; i < length; i++) {
                columnProperties[i] = types[i].getLabel();
            }
            viewer.setColumnProperties(columnProperties);
        }

    }

    @Override
    protected Map<IDialogueControlDescriptor, Control> createManagedControls(Composite parent) {

        Composite viewerArea = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(viewerArea);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewerArea);

        Map<IDialogueControlDescriptor, Control> allControls = new HashMap<IDialogueControlDescriptor, Control>();

        TableViewer viewer = createTableViewer(viewerArea);
        if (viewer != null) {
            allControls.put(ControlTypes.PARAMETERS, viewer.getTable());
        }

        Map<IDialogueControlDescriptor, Button> buttonControls = createOperationButtonArea(viewerArea);

        if (buttonControls != null) {
            allControls.putAll(buttonControls);
        }

        return allControls;
    }

    @Override
    public void setControlValue(Control control, Object value) {
        // TODO Auto-generated method stub

    }

    public List<MethodParameter> getMethodParameter() {
        List<GroovyMethodSuggestion.MethodParameter> methodArguments = new ArrayList<GroovyMethodSuggestion.MethodParameter>();
        if (arguments != null) {
            for (ParameterTableElement element : arguments) {
                methodArguments.add(new MethodParameter(element.getName(), element.getType()));
            }
        }

        return methodArguments;
    }

}
