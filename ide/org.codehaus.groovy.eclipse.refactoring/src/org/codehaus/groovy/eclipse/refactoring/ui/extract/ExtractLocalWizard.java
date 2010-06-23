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
package org.codehaus.groovy.eclipse.refactoring.ui.extract;

import org.codehaus.groovy.eclipse.refactoring.core.extract.ExtractGroovyLocalRefactoring;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.TextInputWizardPage;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.ControlContentAssistHelper;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.VariableNamesProcessor;
import org.eclipse.jdt.internal.ui.util.RowLayouter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 *
 * @author andrew
 * @created Jun 3, 2010
 */
public class ExtractLocalWizard extends RefactoringWizard {

    /* package */static final String DIALOG_SETTING_SECTION = "ExtractLocalWizard"; //$NON-NLS-1$

    private static class ExtractLocalPage extends TextInputWizardPage {

        private static final String REPLACE_ALL = "replaceOccurrences"; //$NON-NLS-1$

        private final boolean fInitialValid;

        private static final String DESCRIPTION = RefactoringMessages.ExtractTempInputPage_enter_name;

        private String[] fTempNameProposals;

        private IDialogSettings fSettings;

        public ExtractLocalPage(String[] tempNameProposals) {
            super(DESCRIPTION, true, tempNameProposals.length == 0 ? "" : tempNameProposals[0]); //$NON-NLS-1$
            Assert.isNotNull(tempNameProposals);
            fTempNameProposals = tempNameProposals;
            fInitialValid = tempNameProposals.length > 0;
        }

        public void createControl(Composite parent) {
            loadSettings();
            Composite result = new Composite(parent, SWT.NONE);
            setControl(result);
            GridLayout layout = new GridLayout();
            layout.numColumns = 2;
            layout.verticalSpacing = 8;
            result.setLayout(layout);
            RowLayouter layouter = new RowLayouter(2);

            Label label = new Label(result, SWT.NONE);
            label.setText(RefactoringMessages.ExtractTempInputPage_variable_name);

            Text text = createTextInputField(result);
            text.selectAll();
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            ControlContentAssistHelper.createTextContentAssistant(text, new VariableNamesProcessor(fTempNameProposals));

            layouter.perform(label, text, 1);

            addReplaceAllCheckbox(result, layouter);

            validateTextField(text.getText());

            Dialog.applyDialogFont(result);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaHelpContextIds.EXTRACT_TEMP_WIZARD_PAGE);
        }

        private void loadSettings() {
            fSettings = getDialogSettings().getSection(ExtractLocalWizard.DIALOG_SETTING_SECTION);
            if (fSettings == null) {
                fSettings = getDialogSettings().addNewSection(ExtractLocalWizard.DIALOG_SETTING_SECTION);
                fSettings.put(REPLACE_ALL, true);
            }
            getExtractLocalRefactoring().setReplaceAllOccurrences(fSettings.getBoolean(REPLACE_ALL));
        }

        private void addReplaceAllCheckbox(Composite result, RowLayouter layouter) {
            String title = RefactoringMessages.ExtractTempInputPage_replace_all;
            boolean defaultValue = getExtractLocalRefactoring().isReplaceAllOccurrences();
            final Button checkBox = createCheckbox(result, title, defaultValue, layouter);
            getExtractLocalRefactoring().setReplaceAllOccurrences(checkBox.getSelection());
            checkBox.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    fSettings.put(REPLACE_ALL, checkBox.getSelection());
                    getExtractLocalRefactoring().setReplaceAllOccurrences(checkBox.getSelection());
                }
            });
        }

        /*
         * @see
         * org.eclipse.jdt.internal.ui.refactoring.TextInputWizardPage#textModified
         * (java.lang.String)
         */
        @Override
        protected void textModified(String text) {
            getExtractLocalRefactoring().setLocalName(text);
            super.textModified(text);
        }

        /*
         * @seeorg.eclipse.jdt.internal.ui.refactoring.TextInputWizardPage#
         * validateTextField(String)
         */
        @Override
        protected RefactoringStatus validateTextField(String text) {
            return getExtractLocalRefactoring().checkLocalNameOnChange(text);
        }

        private ExtractGroovyLocalRefactoring getExtractLocalRefactoring() {
            return (ExtractGroovyLocalRefactoring) getRefactoring();
        }

        private static Button createCheckbox(Composite parent, String title, boolean value, RowLayouter layouter) {
            Button checkBox = new Button(parent, SWT.CHECK);
            checkBox.setText(title);
            checkBox.setSelection(value);
            layouter.perform(checkBox);
            return checkBox;
        }

        /*
         * @seeorg.eclipse.jdt.internal.ui.refactoring.TextInputWizardPage#
         * isInitialInputValid()
         */
        @Override
        protected boolean isInitialInputValid() {
            return fInitialValid;
        }
    }

    public ExtractLocalWizard(ExtractGroovyLocalRefactoring ref) {
        super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
        setDefaultPageTitle(RefactoringMessages.ExtractTempWizard_defaultPageTitle);
    }

    @Override
    protected void addUserInputPages() {
        addPage(new ExtractLocalPage(getExtractTempRefactoring().guessLocalNames()));
    }

    private ExtractGroovyLocalRefactoring getExtractTempRefactoring() {
        return (ExtractGroovyLocalRefactoring) getRefactoring();
    }

}
