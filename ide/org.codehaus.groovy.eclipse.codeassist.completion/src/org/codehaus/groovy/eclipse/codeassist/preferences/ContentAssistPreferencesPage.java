package org.codehaus.groovy.eclipse.codeassist.preferences;

import java.util.Arrays;

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssistActivator;
import org.codehaus.groovy.eclipse.preferences.FieldEditorOverlayPage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ContentAssistPreferencesPage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage {

    private class DGMValidator implements IInputValidator {
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

    private class MultiDGMValidator extends DGMValidator {
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

    private class CompletionFilterListEditor extends ListEditor {

        private Button addMultipleButton;

        public CompletionFilterListEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
            setPreferenceName(GroovyContentAssistActivator.FILTERED_DGMS);
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
            InputDialog dialog = new InputDialog(getFieldEditorParent().getShell(), "Add new DGM to filter",
                    "Select the name of a DefaultGroovyMethod to filter from content assist", "", new DGMValidator());
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
         * @return Button
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
                    "Add/remove multiple entrie.  Enter one DGM name per line.", sb.toString(), new MultiDGMValidator()) {
                @Override
                protected int getInputTextStyle() {
                    return SWT.MULTI | SWT.BORDER;
                }

                @Override
                protected Control createDialogArea(Composite parent) {
                    Control child = super.createDialogArea(parent);
                    getText().setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                            | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL));
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

    public ContentAssistPreferencesPage() {
        super(FLAT);
        setPreferenceStore(GroovyContentAssistActivator.getDefault().getPreferenceStore());
    }

    @Override
    protected String getPageId() {
        return "org.codehaus.groovy.eclipse.codeassist.completion.editor";
    }

    @Override
    protected void createFieldEditors() {
        addField(new CompletionFilterListEditor("Filtered DGMs",
                "Configure which DefaultGroovyMethods will be filtered from content asist.", getFieldEditorParent()));
    }

    public void init(IWorkbench workbench) {}
}
