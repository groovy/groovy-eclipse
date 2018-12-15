/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Dialog for creating and editing script folders in the workspace.
 */
public class ScriptFolderSelectorPreferences {

    private static final int IDX_ADD = 0;
    private static final int IDX_EDIT = 1;
    private static final int IDX_REMOVE = 2;
    private static final int IDX_CHECKALL = 3;
    private static final int IDX_UNCHECKALL = 4;
    private static final String[] BUTTON_LABELS = {"Add", "Edit", "Remove", "Check all", "Uncheck all"};
    private static final ImageDescriptor DESCRIPTOR = JavaPluginImages.DESC_OBJS_INCLUSION_FILTER_ATTRIB;

    private static class ScriptLabelProvider extends LabelProvider {
        private Image fElementImage;
        public ScriptLabelProvider(ImageDescriptor descriptor) {
            ImageDescriptorRegistry registry = JavaPlugin.getImageDescriptorRegistry();
            fElementImage = registry.get(descriptor);
        }
        @Override
        public Image getImage(Object element) {
            return fElementImage;
        }
        @Override
        public String getText(Object element) {
            return BasicElementLabels.getFilePattern((String) element);
        }
    }

    private class ScriptPatternAdapter implements IListAdapter<String>, IDialogFieldListener {
        @Override
        public void customButtonPressed(ListDialogField<String> field, int index) {
            doCustomButtonPressed(field, index);
            hasChanges = true;
        }
        @Override
        public void selectionChanged(ListDialogField<String> field) {
            doSelectionChanged(field);
        }
        @Override
        public void doubleClicked(ListDialogField<String> field) {
            doDoubleClicked(field);
            hasChanges = true;
        }
        @Override
        public void dialogFieldChanged(DialogField field) {
            hasChanges = true;
        }
    }

    private static class BuildJob extends Job {
        private IProject[] projects;
        public BuildJob(IProject...projects) {
            super(getName(projects));
            this.projects = projects;
        }
        private static String getName(IProject...projects) {
            if (projects.length == 1) {
                return "Building project " + projects[0].getName();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Building projects ");
                for (IProject project : projects) {
                    sb.append(project.getName() + " ");
                }
                return sb.toString();
            }
        }
        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                IProgressMonitor submon = SubMonitor.convert(monitor, projects.length);
                for (IProject project : projects) {
                    project.build(IncrementalProjectBuilder.FULL_BUILD, submon);
                    submon.worked(1);
                }
                return Status.OK_STATUS;
            } catch (CoreException e) {
                GroovyCore.logException("Error building groovy project", e);
                return e.getStatus();
            }
        }
    }

    //--------------------------------------------------------------------------

    private final Composite parent;

    private final IEclipsePreferences preferences;

    private final IPreferenceStore store;

    private final IProject project;

    private CheckedListDialogField<String> patternList;

    private BooleanFieldEditor disableButton;

    private boolean hasChanges;

    public ScriptFolderSelectorPreferences(Composite parent, IEclipsePreferences preferences, IPreferenceStore store, IProject project) {
        this.parent = parent;
        this.preferences = preferences;
        this.store = store;
        this.project = project;
    }

    public ListDialogField<String> createListContents() {
        Label label = new Label(parent, SWT.WRAP);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        label.setText("Groovy Script Folders:");
        label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));

        Composite inner = new Composite(parent, SWT.BORDER);
        inner.setFont(parent.getFont());
        GridLayout layout = new GridLayout();
        layout.marginHeight = 3;
        layout.marginWidth = 3;
        layout.numColumns = 1;
        inner.setLayout(layout);
        inner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        disableButton = new BooleanFieldEditor(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, "Enable script folder support", BooleanFieldEditor.DEFAULT, inner);
        disableButton.setPreferenceStore(store);
        disableButton.load();

        // inner composite contains the dialog itself
        final Composite innerInner = new Composite(inner, SWT.NONE);
        innerInner.setFont(parent.getFont());
        layout = new GridLayout();
        layout.marginHeight = 3;
        layout.marginWidth = 3;
        layout.numColumns = 3;
        innerInner.setLayout(layout);
        innerInner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        innerInner.setToolTipText("CHECKED boxes are COPIED to output folder.\nUNCHECKED boxes are NOT copied.");

        // enable/disable pattern list
        disableButton.setPropertyChangeListener(event -> {
            if (event.getProperty() == FieldEditor.VALUE) {
                Object o = event.getNewValue();
                if (o instanceof Boolean) {
                    enablePatternList((Boolean) o);
                }
            }
            hasChanges = true;
        });

        ScriptPatternAdapter adapter = new ScriptPatternAdapter();

        patternList = new CheckedListDialogField<>(adapter, BUTTON_LABELS, new ScriptLabelProvider(DESCRIPTOR));
        patternList.setDialogFieldListener(adapter);
        patternList.setLabelText("Groovy files that match these patterns are treated as scripts, i.e. compiled at run-time.  " +
            "Any script that matches a checked pattern and is in a source folder will be copied as-is to the output folder.\n\n" +
            "CHECKED boxes will be COPIED to the output folder.  UNCHECKED boxes are NOT copied to the output folder.");
        patternList.enableButton(IDX_ADD, true);
        patternList.enableButton(IDX_EDIT, false);
        patternList.setRemoveButtonIndex(IDX_REMOVE);
        patternList.setCheckAllButtonIndex(IDX_CHECKALL);
        patternList.setUncheckAllButtonIndex(IDX_UNCHECKALL);

        patternList.doFillIntoGrid(innerInner, 3);
        Label l = patternList.getLabelControl(innerInner);
        GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        gd.widthHint = 200;
        l.setLayoutData(gd);

        populatePatternList(Activator.getDefault().getScriptFilters(preferences));
        patternList.setViewerComparator(new ViewerComparator());
        enablePatternList(disableButton.getBooleanValue());

        return patternList;
    }

    protected void doCustomButtonPressed(ListDialogField<String> field, int index) {
        if (index == IDX_ADD) {
            addEntry(field);
        } else if (index == IDX_EDIT) {
            editEntry(field);
        }
    }

    protected void doSelectionChanged(ListDialogField<String> field) {
        List<String> selected = field.getSelectedElements();
        field.enableButton(IDX_EDIT, canEdit(selected));
    }

    protected void doDoubleClicked(ListDialogField<String> field) {
        editEntry(field);
    }

    private boolean canEdit(List<String> selected) {
        return selected.size() == 1;
    }

    private void addEntry(ListDialogField<String> field) {
        InputDialog dialog = createInputDialog("");
        if (dialog.open() == Window.OK) {
            field.addElement(dialog.getValue());
        }
    }

    private InputDialog createInputDialog(String initial) {
        InputDialog dialog = new InputDialog(
                parent.getShell(),
                "Add script folder",
                "Enter a pattern for denoting script files in Groovy projects. Allowed wildcards are '*', '?' and '**'. Examples: 'java/util/A*.java', 'java/util/', '**/Test*'.  All patterns are relative to the current project.",
                initial, null);
        return dialog;
    }

    private void editEntry(ListDialogField<String> field) {
        List<String> selElements = field.getSelectedElements();
        if (selElements.size() != 1) {
            return;
        }
        String entry = selElements.get(0);
        InputDialog dialog = createInputDialog(entry);
        if (dialog.open() == Window.OK) {
            field.replaceElement(entry, dialog.getValue());
        }
    }

    public void applyPreferences() {
        if (!hasChanges) {
            return;
        }
        hasChanges = false;
        // must do the store before setting the preference
        // to ensure that the store is flushed
        disableButton.store();

        List<String> elts = patternList.getElements();
        List<String> result = new ArrayList<>(elts.size() * 2);
        for (String elt : elts) {
            result.add(elt);
            result.add(patternList.isChecked(elt) ? "y" : "n");
        }
        Activator.getDefault().setScriptFilters(preferences, result);

        boolean yesNo = MessageDialog.openQuestion(parent.getShell(), "Do full build?", "Script folder preferences have changed.\n" +
                "Must do a full build before they come completely into effect.  Do you want to do a full build now?");
        if (yesNo) {
            if (project != null) {
                new BuildJob(project).schedule();
            } else {
                new BuildJob(GroovyNature.getAllAccessibleGroovyProjects().toArray(new IProject[0])).schedule();
            }
        }
    }

    public void enableControls() {
        enablePatternList(disableButton.getBooleanValue());
    }

    public void restoreDefaults() {
        populatePatternList(Arrays.asList(Activator.DEFAULT_GROOVY_SCRIPT_FILTER.split(",")));
        disableButton.loadDefault();
        hasChanges = true;
    }

    private void enablePatternList(boolean enabled) {
        patternList.getListControl(null).setEnabled(enabled);
        patternList.getLabelControl(null).setEnabled(enabled);
    }

    private void populatePatternList(List<String> elements) {
        List<String> filteredElements = new ArrayList<>(elements.size() / 2);
        List<String> checkedElements = new ArrayList<>(elements.size() / 2);
        for (Iterator<String> eltIter = elements.iterator(); eltIter.hasNext();) {
            String elt = eltIter.next();
            filteredElements.add(elt);
            if (eltIter.hasNext()) {
                String doCopy = eltIter.next();
                if (doCopy.equals("y")) {
                    checkedElements.add(elt);
                }
            }
        }

        patternList.setElements(filteredElements);
        patternList.setCheckedElements(checkedElements);
    }
}
