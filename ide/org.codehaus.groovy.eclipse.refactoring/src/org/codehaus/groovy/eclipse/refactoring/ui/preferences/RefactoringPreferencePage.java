package org.codehaus.groovy.eclipse.refactoring.ui.preferences;

import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.eclipse.refactoring.Activator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class RefactoringPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public RefactoringPreferencePage() {
        super(GRID);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        Label refactoringLabel = new Label(getFieldEditorParent(), SWT.LEFT | SWT.WRAP);
        refactoringLabel.setText("Check back later for some exciting improvements in Groovy-Eclipse's refactoring support.");
//        refactoringLabel.setText(
//                    "\n\nSome people have been noticing slow downs during Java refactoring when\n" +
//                    "the Groovy plugin is installed.  Uncheck the following to disable joint\n" +
//                    "Java-Groovy refactoring.  Note that this option is a temporary measure\n" +
//                    "while we work on the performance issues and will be removed in future versions.");
//        addField(new BooleanFieldEditor(PreferenceConstants.GROOVY_REFACTORING_ENABLED, 
//                "Enable Groovy refactoring participant for Java refactorings", getFieldEditorParent()));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

}
