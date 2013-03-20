//Modified copy from: org.eclipse.jdt.internal.junit.wizards.NewTestCaseCreationWizard
//All changes in this file are "immaterial" and would disappear if we got a patch
//into JDT.
//Changes: search for // GROOVY
/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package greclipse.org.eclipse.jdt.internal.junit.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.junit.JUnitCorePlugin;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jdt.internal.junit.wizards.JUnitWizard;
import org.eclipse.jdt.internal.junit.wizards.WizardMessages;
import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageOne;
import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageTwo;
import org.eclipse.jdt.ui.text.java.ClasspathFixProcessor;
import org.eclipse.jdt.ui.text.java.ClasspathFixProcessor.ClasspathFixProposal;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * A wizard for creating test cases.
 */
public class NewTestCaseCreationWizard extends JUnitWizard {

    private NewTestCaseWizardPageOne fPage1;

    private NewTestCaseWizardPageTwo fPage2;

    public NewTestCaseCreationWizard() {
        super();
        setWindowTitle(WizardMessages.Wizard_title_new_testcase);
        initDialogSettings();
    }

    @Override
    protected void initializeDefaultPageImageDescriptor() {
        setDefaultPageImageDescriptor(JUnitPlugin.getImageDescriptor("wizban/newtest_wiz.png")); //$NON-NLS-1$
    }

    /*
     * @see Wizard#createPages
     */
    @Override
    public void addPages() {
        super.addPages();
        fPage2= new NewTestCaseWizardPageTwo();
        fPage1= new NewTestCaseWizardPageOne(fPage2);
        addPage(fPage1);
        fPage1.init(getSelection());
        addPage(fPage2);
    }

    /*
     * @see Wizard#performFinish
     */
    @Override
    public boolean performFinish() {
        IJavaProject project= fPage1.getJavaProject();
        IRunnableWithProgress runnable= fPage1.getRunnable();
        try {
            if (fPage1.isJUnit4()) {
                if (project.findType(JUnitCorePlugin.JUNIT4_ANNOTATION_NAME) == null) {
                    runnable= addJUnitToClasspath(project, runnable, true);
                }
            } else {
                if (project.findType(JUnitCorePlugin.TEST_SUPERCLASS_NAME) == null) {
                    runnable= addJUnitToClasspath(project, runnable, false);
                }
            }
        } catch (JavaModelException e) {
            // ignore
        } catch (OperationCanceledException e) {
            return false;
        }

        if (finishPage(runnable)) {
            IType newClass= fPage1.getCreatedType();
            IResource resource= newClass.getCompilationUnit().getResource();
            if (resource != null) {
                selectAndReveal(resource);
                openResource(resource);
            }
            return true;
        }
        return false;
    }

    private IRunnableWithProgress addJUnitToClasspath(IJavaProject project, final IRunnableWithProgress runnable, boolean isJUnit4) {
        String typeToLookup= isJUnit4 ? "org.junit.*" : "junit.awtui.*";  //$NON-NLS-1$//$NON-NLS-2$
        ClasspathFixProposal[] fixProposals= ClasspathFixProcessor.getContributedFixImportProposals(project, typeToLookup, null);

        ClasspathFixSelectionDialog dialog= new ClasspathFixSelectionDialog(getShell(), isJUnit4, project, fixProposals);
        if (dialog.open() != 0) {
            throw new OperationCanceledException();
        }

        final ClasspathFixProposal fix= dialog.getSelectedClasspathFix();
        if (fix != null) {
            return new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    if (monitor == null) {
                        monitor= new NullProgressMonitor();
                    }
                    monitor.beginTask(WizardMessages.NewTestCaseCreationWizard_create_progress, 4);
                    try {
                        Change change= fix.createChange(new SubProgressMonitor(monitor, 1));
                        new PerformChangeOperation(change).run(new SubProgressMonitor(monitor, 1));

                        runnable.run(new SubProgressMonitor(monitor, 2));
                    } catch (OperationCanceledException e) {
                        throw new InterruptedException();
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.done();
                    }
                }
            };
        }
        return runnable;
    }

    private static class ClasspathFixSelectionDialog extends MessageDialog implements SelectionListener, IDoubleClickListener {

        static class ClasspathFixLabelProvider extends LabelProvider {

            @Override
            public Image getImage(Object element) {
                if (element instanceof ClasspathFixProposal) {
                    ClasspathFixProposal classpathFixProposal= (ClasspathFixProposal) element;
                    return classpathFixProposal.getImage();
                }
                return null;
            }

            @Override
            public String getText(Object element) {
                if (element instanceof ClasspathFixProposal) {
                    ClasspathFixProposal classpathFixProposal= (ClasspathFixProposal) element;
                    return classpathFixProposal.getDisplayString();
                }
                return null;
            }
        }


        private final ClasspathFixProposal[] fFixProposals;
        private final IJavaProject fProject;

        private TableViewer fFixSelectionTable;

        private Button fNoActionRadio;
        private Button fOpenBuildPathRadio;
        private Button fPerformFix;

        private ClasspathFixProposal fSelectedFix;

        public ClasspathFixSelectionDialog(Shell parent, boolean isJUnit4, IJavaProject project, ClasspathFixProposal[] fixProposals) {
            super(parent, WizardMessages.Wizard_title_new_testcase, null, getDialogMessage(isJUnit4), MessageDialog.QUESTION, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
            fProject= project;
            fFixProposals= fixProposals;
            fSelectedFix= null;
        }

        @Override
        protected boolean isResizable() {
            return true;
        }

        private static String getDialogMessage(boolean isJunit4) {
            return isJunit4 ? WizardMessages.NewTestCaseCreationWizard_fix_selection_junit4_description : WizardMessages.NewTestCaseCreationWizard_fix_selection_junit3_description;
        }

        @Override
        protected Control createCustomArea(Composite composite) {
            fNoActionRadio= new Button(composite, SWT.RADIO);
            fNoActionRadio.setLayoutData(new GridData(SWT.LEAD, SWT.TOP, false, false));
            fNoActionRadio.setText(WizardMessages.NewTestCaseCreationWizard_fix_selection_not_now);
            fNoActionRadio.addSelectionListener(this);

            fOpenBuildPathRadio= new Button(composite, SWT.RADIO);
            fOpenBuildPathRadio.setLayoutData(new GridData(SWT.LEAD, SWT.TOP, false, false));
            fOpenBuildPathRadio.setText(WizardMessages.NewTestCaseCreationWizard_fix_selection_open_build_path_dialog);
            fOpenBuildPathRadio.addSelectionListener(this);

            if (fFixProposals.length > 0) {

                fPerformFix= new Button(composite, SWT.RADIO);
                fPerformFix.setLayoutData(new GridData(SWT.LEAD, SWT.TOP, false, false));
                fPerformFix.setText(WizardMessages.NewTestCaseCreationWizard_fix_selection_invoke_fix);
                fPerformFix.addSelectionListener(this);

                fFixSelectionTable= new TableViewer(composite, SWT.SINGLE | SWT.BORDER);
                fFixSelectionTable.setContentProvider(new ArrayContentProvider());
                fFixSelectionTable.setLabelProvider(new ClasspathFixLabelProvider());
                fFixSelectionTable.setComparator(new ViewerComparator());
                fFixSelectionTable.addDoubleClickListener(this);
                fFixSelectionTable.setInput(fFixProposals);
                fFixSelectionTable.setSelection(new StructuredSelection(fFixProposals[0]));

                GridData gridData= new GridData(SWT.FILL, SWT.FILL, true, true);
                gridData.heightHint= convertHeightInCharsToPixels(4);
                gridData.horizontalIndent= convertWidthInCharsToPixels(2);

                fFixSelectionTable.getControl().setLayoutData(gridData);

                fNoActionRadio.setSelection(false);
                fOpenBuildPathRadio.setSelection(false);
                fPerformFix.setSelection(true);

            } else {
                fNoActionRadio.setSelection(true);
                fOpenBuildPathRadio.setSelection(false);
            }

            updateEnableStates();

            return composite;
        }

        private void updateEnableStates() {
            if (fPerformFix != null) {
                fFixSelectionTable.getTable().setEnabled(fPerformFix.getSelection());
            }
        }

        private static final String BUILD_PATH_PAGE_ID= "org.eclipse.jdt.ui.propertyPages.BuildPathsPropertyPage"; //$NON-NLS-1$
        private static final Object BUILD_PATH_BLOCK= "block_until_buildpath_applied"; //$NON-NLS-1$

        @Override
        protected void buttonPressed(int buttonId) {
            fSelectedFix= null;
            if (buttonId == 0) {
                if (fNoActionRadio.getSelection()) {
                    // nothing to do
                } else if (fOpenBuildPathRadio.getSelection()) {
                    String id= BUILD_PATH_PAGE_ID;
                    Map input= new HashMap();
                    input.put(BUILD_PATH_BLOCK, Boolean.TRUE);
                    if (PreferencesUtil.createPropertyDialogOn(getShell(), fProject, id, new String[] { id }, input).open() != Window.OK) {
                        return;
                    }
                } else if (fFixSelectionTable != null) {
                    IStructuredSelection selection= (IStructuredSelection) fFixSelectionTable.getSelection();
                    Object firstElement= selection.getFirstElement();
                    if (firstElement instanceof ClasspathFixProposal) {
                        fSelectedFix= (ClasspathFixProposal) firstElement;
                    }
                }
            }
            super.buttonPressed(buttonId);
        }

        public ClasspathFixProposal getSelectedClasspathFix() {
            return fSelectedFix;
        }

        public void widgetDefaultSelected(SelectionEvent e) {
            updateEnableStates();
        }

        public void widgetSelected(SelectionEvent e) {
            updateEnableStates();
        }

        public void doubleClick(DoubleClickEvent event) {
            okPressed();

        }
    }

}
