package org.codehaus.groovy.eclipse.preferences;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.Workbench;

public class CompilerPreferencesPage extends PreferencePage implements
        IWorkbenchPreferencePage {

    protected final boolean isGroovy17Disabled;
    
    public CompilerPreferencesPage() {
        super("Compiler");
        setPreferenceStore(GroovyPlugin.getDefault().getPreferenceStore());
        isGroovy17Disabled = CompilerUtils.isGroovy17DisabledOrMissing();
    }

    @Override
    protected Control createContents(Composite parent) {
        final Composite page = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        page.setLayout(layout);
        page.setFont(parent.getFont());
        
        
        Label compilerVersion = new Label(page, SWT.LEFT | SWT.WRAP);
        compilerVersion.setText("You are currently using Groovy Compiler version " + CompilerUtils.getGroovyVersion() + 
                "\nClick to change to using version " + CompilerUtils.getOtherVersion() + ". Requires restart."); 
        
        Button switchTo = new Button(page, SWT.PUSH);
        switchTo.setText("Switch to " + CompilerUtils.getOtherVersion());
        switchTo.addSelectionListener(new SelectionListener() {
            
        public void widgetSelected(SelectionEvent e) {
            boolean result = MessageDialog.openQuestion(page.getShell(), "Change compiler and restart?", 
                    "Do you want to change the compiler?\n\nIf you select \"Yes\"," +
                    " the compiler will be changed and Eclipse will be restarted.");
                    
                if (result) {
                    // change compiler
                    IStatus status = CompilerUtils.switchVersions(isGroovy17Disabled);
                    if (status == Status.OK_STATUS) {
                        if (MessageDialog.openQuestion(page.getShell(), "Restart?", "Do you want to restart now?\n\n" +
                        		"It is strongly recommended that you do so.")) {
                            Workbench.getInstance().restart();
                        }
                    } else {
                        ErrorDialog error = new ErrorDialog(page.getShell(), 
                                "Error occurred", "Error occurred when trying to enable Groovy " + CompilerUtils.getOtherVersion(), 
                                status, IStatus.ERROR);
                        error.open();
                    }
                }

            }
            
            public void widgetDefaultSelected(SelectionEvent e) {
            
            }
        });
        return page;
    }

    public void init(IWorkbench workbench) {
        
    }

    
}
