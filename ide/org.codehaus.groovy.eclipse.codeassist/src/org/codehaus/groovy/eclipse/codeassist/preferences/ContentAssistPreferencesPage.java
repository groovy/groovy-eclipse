/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.preferences;

import java.util.Arrays;

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.preferences.FieldEditorOverlayPage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

public class ContentAssistPreferencesPage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage {

    private static class DGMValidator implements IInputValidator {
        @Override
        public String isValid(String newText) {
            if (newText.trim().length() == 0) {
                return "";
            }
            String[] complianceLevels = JavaConventionsUtil.getSourceComplianceLevels(null);
            IStatus s = JavaConventions.validateMethodName(newText, complianceLevels[0], complianceLevels[1]);
            if (s.getSeverity() > IStatus.OK) {
                return s.getMessage();
            }
            return null;
        }
    }

    private static class MultiDGMValidator extends DGMValidator {
        @Override
        public String isValid(String newText) {
            String[] splits = newText.split("\\n");
            StringBuilder sb = new StringBuilder();
            for (String split : splits) {
                String res = super.isValid(split);
                if (res != null) {
                    sb.append(res);
                }
            }
            return sb.length() > 0 ? sb.toString() : null;
        }
    }

    private static class CompletionFilterListEditor extends ListEditor {

        private Button addMultipleButton;
        private final Shell shell;

        CompletionFilterListEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
            this.shell = parent.getShell();
            setPreferenceName(GroovyContentAssist.FILTERED_DGMS);
        }

        @Override
        protected String createList(String[] items) {
            StringBuilder sb = new StringBuilder();
            for (String item : items) {
                sb.append(item + ",");
            }
            return sb.toString();
        }

        @Override
        protected String getNewInputObject() {
            InputDialog dialog = new InputDialog(shell, "Add new DGM to filter",
                    "Select the name of a Default Groovy Method to filter from content assist", "", new DGMValidator());
            int res = dialog.open();
            if (res == Window.OK) {
                return dialog.getValue();
            }
            return null;
        }

        @Override
        public Composite getButtonBoxControl(Composite parent) {
            Composite buttonBox = super.getButtonBoxControl(parent);

            if (addMultipleButton == null) {
                addMultipleButton = createAddMultipleButton(buttonBox, "Add multiple...");
            }

            getUpButton().setVisible(false);
            getDownButton().setVisible(false);
            return buttonBox;
        }

        /**
         * Helper method to create a push button.
         *
         * @param parent the parent control
         * @param key the resource name used to supply the button's label text
         */
        private Button createAddMultipleButton(Composite parent, String name) {
            Button button = new Button(parent, SWT.PUSH);
            button.setText(name);
            button.setFont(parent.getFont());
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            int widthHint = convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH);
            data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
            button.setLayoutData(data);
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    addMultiple();
                }
            });
            return button;
        }

        protected void addMultiple() {
            String[] items = getList().getItems();
            StringBuilder sb = new StringBuilder();
            for (String item : items) {
                sb.append(item + "\n");
            }
            InputDialog input = new InputDialog(getShell(), "Add multiple",
                    "Add/remove multiple entries.  Enter one Default Groovy Method name per line.", sb.toString(), new MultiDGMValidator()) {
                @Override
                protected int getInputTextStyle() {
                    return SWT.MULTI | SWT.BORDER;
                }

                @Override
                protected Control createDialogArea(Composite parent) {
                    Control child = super.createDialogArea(parent);
                    getText().setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL));
                    return child;
                }
            };

            int res = input.open();
            if (res == Window.OK) {
                String text = input.getValue();
                items = text.split("\\n");
                Arrays.sort(items);
                getList().setItems(items);
                getList().deselectAll();
                selectionChanged();
            }
        }

        @Override
        protected String[] parseString(String stringList) {
            String[] split = stringList.split(",");
            Arrays.sort(split);
            return split;
        }
    }

    //--------------------------------------------------------------------------

    public ContentAssistPreferencesPage() {
        super(GRID);
        setPreferenceStore(GroovyContentAssist.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected String getPageId() {
        return "org.codehaus.groovy.eclipse.editor.codeassist";
    }

    @Override
    protected void createFieldEditors() {
        //
        Composite fieldGroup = createFieldGroup("Insertion");

        addField(new BooleanFieldEditor(GroovyContentAssist.NAMED_ARGUMENTS,
            "Use named arguments for method calls", fieldGroup));
        addField(new BooleanFieldEditor(GroovyContentAssist.CLOSURE_BRACKETS,
            "Use closure literals for closure arguments", fieldGroup));
        addField(new BooleanFieldEditor(GroovyContentAssist.CLOSURE_NOPARENS,
            "Place trailing closure arguments after closing parenthesis", fieldGroup));

        //
        fieldGroup = createFieldGroup("Filtering");
        ((GridData) fieldGroup.getParent().getLayoutData())
            .verticalIndent = IDialogConstants.VERTICAL_MARGIN;

        addField(new CompletionFilterListEditor("Filtered DGMs",
            "Default Groovy Methods that will be filtered from content assist", fieldGroup));

        //
        insertPageLink("org.eclipse.jdt.ui.preferences.CodeAssistPreferencePage",
            "Additional preferences are inherited from <a>Java Content Assist</a>");
    }

    private Composite createFieldGroup(String label) {
        Group group = new Group(getFieldEditorParent(), SWT.SHADOW_NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(2, 1).applyTo(group);
        group.setFont(group.getParent().getFont());
        group.setLayout(new GridLayout());
        group.setText(label);

        // internal panel for field editors to modify
        Composite panel = new Composite(group, SWT.NONE);
        panel.setLayoutData(new GridData(GridData.FILL_BOTH));
        return panel;
    }

    private void insertPageLink(String page, String text) {
        PreferenceLinkArea linkArea = new PreferenceLinkArea(getFieldEditorParent(),
            SWT.WRAP, page, text, (IWorkbenchPreferenceContainer) getContainer(), null);
        GridDataFactory.fillDefaults().indent(0, 8).applyTo(linkArea.getControl());
    }
}
