package org.codehaus.groovy.eclipse.preferences;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.eclipse.debug.ui.GroovyDebugOptionsEnforcer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.progress.UIJob;

public class DebuggerPreferencesPage extends FieldEditorOverlayPage implements
        IWorkbenchPreferencePage {
    final static Pattern packagePattern = Pattern.compile("");

    private class PackagePrefixValidator implements IInputValidator {
        public String isValid(String newText) {
            if (newText.trim().length() == 0)
                return ""; //$NON-NLS-1$
            IStatus s = JavaConventions.validatePackageName(newText, CompilerOptions.VERSION_1_3,CompilerOptions.VERSION_1_3);
            if (s.getSeverity() > IStatus.OK) {
                return s.getMessage();
            }
            return null;
        }
    }

    private class PackageChooserListEditor extends ListEditor {

        public PackageChooserListEditor(String name, String labelText,
                Composite parent) {
            super(name, labelText, parent);
            setPreferenceStore(DebuggerPreferencesPage.this.getPreferenceStore());
        }

        @Override
        protected String createList(String[] items) {
            if (items != null && items.length > 0) {
                Arrays.sort(items);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < items.length; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(items[i]);
                }
                return sb.toString();
            }
            return "";
        }

        @Override
        public Composite getButtonBoxControl(Composite parent) {
            Composite c = super.getButtonBoxControl(parent);
            getUpButton().setVisible(false);
            getDownButton().setVisible(false);
            return c;
        }

        @Override
        protected String getNewInputObject() {
            InputDialog dialog = new InputDialog(getFieldEditorParent().getShell(), "Add new package to filter",
                    "Add a package or a package prefix to grey out in the Debug view", "", new PackagePrefixValidator());
            int res = dialog.open();
            if (res == Window.OK) {
                return dialog.getValue();
            }
            return null;
        }

        @Override
        protected String[] parseString(String stringList) {
            String[] split = stringList.split(",");
            Arrays.sort(split);
            return split;
        }
    }

    private boolean doForceOptions = false;

    public DebuggerPreferencesPage() {
        super(GRID);
        setPreferenceStore(GroovyPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected String getPageId() {
        return "org.codehaus.groovy.eclipse.preferences.debugger";
    }

    @Override
    protected void createFieldEditors() {
        addField(new BooleanFieldEditor(PreferenceConstants.GROOVY_DEBUG_FILTER_STACK, "Gray-out internal Groovy stack frames while debugging.",
                getFieldEditorParent()));

        addField(new PackageChooserListEditor(PreferenceConstants.GROOVY_DEBUG_FILTER_LIST,
                "Configure prefixes of the packages to filter out", getFieldEditorParent()));

        PreferenceLinkArea area = new PreferenceLinkArea(getFieldEditorParent(), SWT.WRAP,
                "org.eclipse.jdt.debug.ui.JavaStepFilterPreferencePage",
                " \n\n" +
                "Stack frame filtering works best when it is combined with step filters." +
                "\nUsing step filters, Groovy internal stack frames are ignored\n" +
                "when stepping through instructions in the debugger.  \n\n" +
                "You can <a>edit step filters...</a>\n\n" +
                "Or you can add step filtering automatically by clicking below.",
                (IWorkbenchPreferenceContainer) getContainer(), null);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        area.getControl().setLayoutData(data);
        new Composite(getFieldEditorParent(), 0);

        Button forceDebugOptions = new Button(getFieldEditorParent(), SWT.PUSH);
        forceDebugOptions.setText("Automatically configure common Groovy step filtering");
        forceDebugOptions.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent arg0) {
                doForceOptions = true;
                new GroovyDebugOptionsEnforcer().force();
            }

            public void widgetDefaultSelected(SelectionEvent arg0) {
                doForceOptions = true;
                new GroovyDebugOptionsEnforcer().force();
            }
        });
        new Composite(getFieldEditorParent(), 0);
    }

    public void init(IWorkbench workbench) {}

    @Override
    public boolean performOk() {
        if (doForceOptions) {
            // ensures that this runs after the preference page closes
            // otherwise the forcing might be overridden by the actual Java Step
            // Filter preferences page
            UIJob job = new UIJob(this.getShell().getDisplay(), "Setting Groovy debug options") {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    new GroovyDebugOptionsEnforcer().force();
                    return Status.OK_STATUS;
                }
            };
            job.schedule();
        }
        return super.performOk();
    }
}
