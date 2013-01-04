/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.wizards;

import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.preferences.CompilerSwitchUIHelper;
import org.codehaus.groovy.frameworkadapter.util.SpecifiedVersion;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * 
 * @author Andrew Eisenberg
 * @since 3.0.0
 */
public class ListMessageDialog extends MessageDialog {

    class TableContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof IProject[]) {
                return (IProject[]) inputElement;
            }
            return null;
        }

        public void dispose() { }
        public void inputChanged(Viewer viewer2, Object oldInput, Object newInput) { }
    }
    private static final String TITLE = "Fix compiler level mismatches"; //$NON-NLS-1$

    private final IProject[] mismatchedProjects;
    private IProject[] checkedMismatchedProjects;

    private CheckboxTableViewer viewer;

    /**
     * Opens the mismatched compiler dialog focusing on the selected projects
     * @param mismatchProjects
     * @return
     */
    public static IProject[] openViewer(Shell shell, IProject[] mismatchProjects) {
        ListMessageDialog dialog = new ListMessageDialog(shell, mismatchProjects);
        int res = dialog.open();
        if (res == 0D) {
            return dialog.getAllChecked();
        } else {
            return null;
        }
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == 0) {
            Object[] checkedElements = viewer.getCheckedElements();
            checkedMismatchedProjects = new IProject[checkedElements.length];
            System.arraycopy(checkedElements, 0, checkedMismatchedProjects, 0, checkedElements.length);
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    public ListMessageDialog(Shell shell, IProject[] mismatchedProjects) {
        super(shell, TITLE, null, createMessage(mismatchedProjects), QUESTION, new String[] { IDialogConstants.YES_LABEL,
            IDialogConstants.NO_LABEL }, 0);
        this.mismatchedProjects = mismatchedProjects;
    }

    @Override
    protected Control createCustomArea(Composite parent) {
        ((GridLayout) parent.getLayout()).numColumns = 2;
        ((GridLayout) parent.getLayout()).makeColumnsEqualWidth = false;

        viewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 150;
        gd.verticalSpan = 2;
        viewer.getTable().setLayoutData(gd);
        viewer.setContentProvider(new TableContentProvider());
        viewer.setLabelProvider(new WorkbenchLabelProvider() {
            @Override
            protected String decorateText(String input, Object element) {
                String label = super.decorateText(input, element);
                if (element instanceof IProject) {
                    SpecifiedVersion version = CompilerUtils.getCompilerLevel((IProject) element);
                    return label + " -- " + version.toReadableVersionString();
                } else {
                    return label;
                }
            }
        });
        viewer.setInput(mismatchedProjects);
        viewer.setAllChecked(true);
        applyDialogFont(viewer.getControl());
        createButton(parent, "Select all", new SelectionAdapter() { //$NON-NLS-1$
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(true);
            }
        });
        createButton(parent, "Select none", new SelectionAdapter() { //$NON-NLS-1$
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(false);
            }
        });

        Composite compilerBlock = CompilerSwitchUIHelper.createCompilerSwitchBlock(parent);
        compilerBlock.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));

        return parent;
    }

    protected Button createButton(Composite parent, String label, SelectionListener listener) {
        return createButton(parent, label, SWT.PUSH, listener);
    }

    protected Button createButton(Composite parent, String label, int style, SelectionListener listener) {
        Button button= new Button(parent, SWT.PUSH);
        button.setFont(parent.getFont());
        button.setText(label);
        button.addSelectionListener(listener);
        GridData gd= new GridData();
        gd.horizontalAlignment= GridData.FILL;
        gd.grabExcessHorizontalSpace= false;
        gd.verticalAlignment= GridData.BEGINNING;
        gd.widthHint = 100;

        button.setLayoutData(gd);

        return button;
    }

    IProject[] getAllChecked() {
        return checkedMismatchedProjects;
    }

    private static String createMessage(IProject[] allMismatchedProjects) {
        StringBuilder sb = new StringBuilder();
        if (allMismatchedProjects.length > 1) {
            sb.append("The following Groovy compiler mismatches have been found."); //$NON-NLS-1$
        } else {
            sb.append("The following Groovy compiler mismatch has been found."); //$NON-NLS-1$
        }
        if (allMismatchedProjects.length > 1) {
            sb.append(" These projects may not compile until their compiler level matches the workspace level.\n\n"); //$NON-NLS-1$
        } else {
            sb.append(" This project may not compile until its compiler level matches the workspace level.\n\n"); //$NON-NLS-1$
        }
        sb.append("Do you want to change the project compiler levels to match the workspace level?"); //$NON-NLS-1$
        return sb.toString();
    }
}
