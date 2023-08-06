/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.ui;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.DSLDStore;
import org.codehaus.groovy.eclipse.dsl.DSLDStoreManager;
import org.codehaus.groovy.eclipse.dsl.DSLPreferencesInitializer;
import org.codehaus.groovy.eclipse.dsl.DisabledScriptsCache;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.TreeListDialogField;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.progress.UIJob;

public class DSLPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

    private static final String[] LABELS = {"Edit...", "Recompile Scripts", "Refresh List", "Check All", "Uncheck All"};
    private static final int IDX_EDIT = 0, IDX_RECOMPILE = 1, IDX_REFRESH = 2, IDX_CHECK_ALL = 3, IDX_UNCHECK_ALL = 4;

    DisabledScriptsCache cache;

    Map<String, ProjectContextKey[]> elementsMap;

    private CheckedTreeListDialogField tree;

    private IWorkbenchPage page;

    private Button autoAdd;
    private Button disableDSLDs;

    @Override
    public void init(IWorkbench workbench) {
        cache = new DisabledScriptsCache();
        elementsMap = new HashMap<>();
        try {
            page = workbench.getActiveWorkbenchWindow().getActivePage();
        } catch (NullPointerException e) {
            // there is handling below for either state of page reference
        }
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());

        tree = new CheckedTreeListDialogField(new DSLListAdapter(), LABELS, new DSLLabelProvider());
        tree.setTreeExpansionLevel(2);
        LayoutUtil.doDefaultLayout(composite, new DialogField[] {tree}, true, SWT.DEFAULT, SWT.DEFAULT);
        LayoutUtil.setHorizontalGrabbing(tree.getTreeControl(null));

        refresh();
        PixelConverter converter = new PixelConverter(parent);
        int buttonBarWidth = converter.convertWidthInCharsToPixels(24);
        tree.setButtonsMinWidth(buttonBarWidth);

        autoAdd = new Button(composite, SWT.CHECK);
        autoAdd.setText("Automatically add DSL Support to all Groovy projects");
        autoAdd.setSelection(GroovyDSLCoreActivator.getDefault().getPreferenceStore().getBoolean(DSLPreferencesInitializer.AUTO_ADD_DSL_SUPPORT));

        GridData data = new GridData(SWT.LEFT, SWT.TOP, true, false);
        data.horizontalSpan = 2;
        autoAdd.setLayoutData(data);
        disableDSLDs = new Button(composite, SWT.CHECK);
        disableDSLDs.setText("Disable DSLD support in your workspace. (Requires restart)");
        boolean isDisabled = GroovyDSLCoreActivator.getDefault().getPreferenceStore().getBoolean(DSLPreferencesInitializer.DSLD_DISABLED);
        disableDSLDs.setSelection(isDisabled);
        disableDSLDs.setLayoutData(data);

        if (disableDSLDs.getSelection()) {
            Label l = new Label(composite, SWT.NONE);
            l.setText("NOTE: DSLD support is currently disabled.");
        }

        return composite;
    }

    protected IProject toProject(Object element) {
        if (element instanceof String) {
            String name = (String) element;
            IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
            if (GroovyNature.hasGroovyNature(proj)) {
                return proj;
            }
        }
        return null;
    }

    protected boolean canEdit() {
        List<?> selected = tree.getSelectedElements();
        return selected.size() == 1 && selected.get(0) instanceof ProjectContextKey;
    }

    protected void edit() {
        if (canEdit()) {
            List<?> selected = tree.getSelectedElements();
            ProjectContextKey pck = (ProjectContextKey) selected.get(0);
            IStorage storage = pck.dslFile;
            IEditorInput input = getEditorInput(storage);
            if (input != null) {
                try {
                    if (page != null) {
                        IDE.openEditor(page, input, GroovyEditor.EDITOR_ID, true);
                    }
                } catch (PartInitException e) {
                    if (page != null) {
                        ErrorDialog.openError(page.getWorkbenchWindow().getShell(),
                            "Error opening editor", "See error log: " + e.getLocalizedMessage(), e.getStatus());
                    }
                    GroovyDSLCoreActivator.logException(e);
                }
            } else {
                if (page != null) {
                    ErrorDialog.openError(page.getWorkbenchWindow().getShell(),
                        "Could not open editor", "File " + pck.dslFile + " is not accessible.",
                        new Status(IStatus.ERROR, GroovyDSLCoreActivator.PLUGIN_ID, "Could not open editor"));
                }
            }
        }
    }

    private IEditorInput getEditorInput(IStorage storage) {
        if (storage instanceof IFile && ((IFile) storage).getProject().equals(JavaModelManager.getExternalManager().getExternalFoldersProject())) {
            return new FileStoreEditorInput(new LocalFile(new File(((IFile) storage).getLocationURI())));
        } else {
            return EditorUtility.getEditorInput(storage);
        }
    }

    protected void refresh() {
        DSLDStoreManager manager = GroovyDSLCoreActivator.getDefault().getContextStoreManager();
        String[] projectNames = manager.getProjectNames();
        elementsMap.clear();

        for (String projectName : projectNames) {
            IProject project = toProject(projectName);
            if (project != null) {
                DSLDStore store = manager.getDSLDStore(project);
                if (store != null) {
                    IStorage[] keys = store.getAllContextKeys();
                    Arrays.sort(keys, Comparator.comparing(IStorage::getName));
                    ProjectContextKey[] pck = new ProjectContextKey[keys.length];
                    for (int i = 0, n = pck.length; i < n; i += 1) {
                        pck[i] = new ProjectContextKey(projectName, keys[i]);
                        pck[i].isChecked = !cache.isDisabled(DSLDStore.toUniqueString(pck[i].dslFile));
                    }
                    elementsMap.put(projectName, pck);
                }
            }
        }

        Arrays.sort(projectNames);
        tree.setElements(Arrays.asList(projectNames));
        tree.refresh();

        for (ProjectContextKey[] keys : elementsMap.values()) {
            for (ProjectContextKey key : keys) {
                tree.setChecked(key, key.isChecked);
            }
        }
    }

    void checkAll(boolean newState) {
        for (ProjectContextKey[] keys : elementsMap.values()) {
            for (ProjectContextKey key : keys) {
                key.isChecked = newState;
                tree.setChecked(key, key.isChecked);
            }
        }
    }

    private static final String EVENT = "Recompiling all DSLDs in the workspace.";

    protected void recompile() {
        // re-compile all scripts and then wait for the results to refresh the UIs
        new UIJob("Refresh DSLD launcher") {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                GroovyLogManager.manager.log(TraceCategory.DSL, EVENT);
                GroovyLogManager.manager.logStart(EVENT);

                GroovyDSLCoreActivator activator = GroovyDSLCoreActivator.getDefault();
                if (!activator.isDSLDDisabled()) {
                    activator.getContextStoreManager().initialize(ResourcesPlugin.getWorkspace().getRoot().getProjects(), true);
                }

                if (!DSLPreferencesPage.this.getControl().isDisposed()) {
                    refresh();
                }

                GroovyLogManager.manager.logEnd(EVENT, TraceCategory.DSL);
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        checkAll(true);
        DSLPreferencesInitializer.reset();
        autoAdd.setSelection(true);
    }

    @Override
    public boolean performOk() {
        Set<String> unchecked = new HashSet<>();
        for (ProjectContextKey[] keys : elementsMap.values()) {
            for (ProjectContextKey key : keys) {
                if (!key.isChecked) {
                    unchecked.add(DSLDStore.toUniqueString(key.dslFile));
                }
            }
        }
        cache.setDisabled(unchecked);

        GroovyDSLCoreActivator.getDefault().getPreferenceStore().setValue(DSLPreferencesInitializer.AUTO_ADD_DSL_SUPPORT, autoAdd.getSelection());

        boolean origDisabled = GroovyDSLCoreActivator.getDefault().getPreferenceStore().getBoolean(DSLPreferencesInitializer.DSLD_DISABLED);
        if (origDisabled != disableDSLDs.getSelection()) {
            GroovyDSLCoreActivator.getDefault().getPreferenceStore().setValue(DSLPreferencesInitializer.DSLD_DISABLED, disableDSLDs.getSelection());
            boolean res = MessageDialog.openQuestion(getShell(), "Restart now?", "You have " +
                (disableDSLDs.getSelection() ? "disabled" : "enabled") + " DSLDs in your workspace." +
                "  This will not take effect until a restart has been performed.\n\nDo you want to restart now?");
            if (res) {
                Workbench.getInstance().restart();
            }
        }
        return super.performOk();
    }

    //--------------------------------------------------------------------------

    private class CheckStateListener implements ICheckStateListener {
        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
            Object element = event.getElement();
            if (element instanceof ProjectContextKey) {
                ProjectContextKey key = (ProjectContextKey) element;
                key.isChecked = event.getChecked();
            } else if (element instanceof String) {
                ProjectContextKey[] children = elementsMap.get(element);
                for (ProjectContextKey child : children) {
                    child.isChecked = event.getChecked();
                }
            }
        }
    }

    private class CheckedTreeListDialogField extends TreeListDialogField<String> {
        private ContainerCheckedTreeViewer checkboxViewer;

        private CheckedTreeListDialogField(ITreeListAdapter<String> adapter, String[] buttonLabels, ILabelProvider lprovider) {
            super(adapter, buttonLabels, lprovider);
        }

        @Override
        protected TreeViewer createTreeViewer(Composite parent) {
            Tree treeComponent = new Tree(parent, getTreeStyle() | SWT.CHECK);
            treeComponent.setFont(parent.getFont());
            checkboxViewer = new ContainerCheckedTreeViewer(treeComponent);
            checkboxViewer.addCheckStateListener(new CheckStateListener());
            return checkboxViewer;
        }

        void setChecked(Object child, boolean newState) {
            checkboxViewer.setChecked(child, newState);
        }
    }

    private class DSLLabelProvider extends LabelProvider {

        WorkbenchLabelProvider provider = new WorkbenchLabelProvider();

        @Override
        public void dispose() {
            provider.dispose();
            super.dispose();
        }

        @Override
        public String getText(Object element) {
            if (element instanceof ProjectContextKey) {
                ProjectContextKey pck = (ProjectContextKey) element;
                return pck.dslFile.getName();
            }
            return super.getText(element);
        }

        @Override
        public Image getImage(Object element) {
            IProject proj = toProject(element);
            if (proj != null) {
                return provider.getImage(proj);
            }

            IFile file = null;
            if (element instanceof ProjectContextKey && ((ProjectContextKey) element).dslFile instanceof IFile) {
                file = (IFile) ((ProjectContextKey) element).dslFile;
            }

            if (file != null) {
                return provider.getImage(file);
            }
            return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CFILE);
        }
    }

    private class DSLListAdapter implements ITreeListAdapter<String> {
        @Override
        public void customButtonPressed(TreeListDialogField<String> field, int index) {
            if (index == IDX_EDIT) {
                // edit
                edit();
            } else if (index == IDX_RECOMPILE) {
                // recompile
                recompile();
            } else if (index == IDX_REFRESH) {
                // refresh
                refresh();
            } else if (index == IDX_CHECK_ALL) {
                // check all
                checkAll(true);
            } else if (index == IDX_UNCHECK_ALL) {
                // uncheck all
                checkAll(false);
            }
        }

        @Override
        public void selectionChanged(TreeListDialogField<String> field) {
            if (canEdit()) {
                field.enableButton(IDX_EDIT, true);
            } else {
                field.enableButton(IDX_EDIT, false);
            }
        }

        @Override
        public void doubleClicked(TreeListDialogField<String> field) {
            edit();
        }

        @Override
        public void keyPressed(TreeListDialogField<String> field, KeyEvent event) {
        }

        @Override
        public Object[] getChildren(TreeListDialogField<String> field, Object element) {
            if (element instanceof String) {
                return elementsMap.get(element);
            } else {
                return null;
            }
        }

        @Override
        public Object getParent(TreeListDialogField<String> field, Object element) {
            if (element instanceof ProjectContextKey) {
                return ((ProjectContextKey) element).projectName;
            }
            return null;
        }

        @Override
        public boolean hasChildren(TreeListDialogField<String> field, Object element) {
            Object[] children = getChildren(field, element);
            return children != null && children.length > 0;
        }
    }

    private static class ProjectContextKey {
        final String projectName;
        final IStorage dslFile;
        boolean isChecked; // set later

        ProjectContextKey(String projectName, IStorage dslFile) {
            this.projectName = projectName;
            this.dslFile = dslFile;
        }
    }
}
