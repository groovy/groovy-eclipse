/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.ui;

import java.io.File;
import java.util.Collections;
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
import org.eclipse.core.resources.IWorkspaceRoot;
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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
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
    
    private static final IWorkspaceRoot ROOT = ResourcesPlugin.getWorkspace().getRoot();

    private final String[] LABELS = { "Edit...", "Recompile Scripts", "Refresh List", "Check All", "Uncheck All" };
    private final int IDX_EDIT = 0;
    private final int IDX_RECOMPILE = 1;
    private final int IDX_REFRESH = 2;
    private final int IDX_CHECK_ALL = 3;
    private final int IDX_UNCHECK_ALL= 4;
    
    private final class CheckStateListener implements ICheckStateListener {

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
    
    /**
     * A {@link TreeListDialogField} that uses check boxes
     * @author andrew
     * @created Feb 26, 2011
     */
    @SuppressWarnings("rawtypes")
    private final class CheckedTreeListDialogField extends TreeListDialogField {
        private ContainerCheckedTreeViewer checkboxViewer;
        
        private CheckedTreeListDialogField(ITreeListAdapter adapter,
                String[] buttonLabels, ILabelProvider lprovider) {
            super(adapter, buttonLabels, lprovider);
        }

        protected TreeViewer createTreeViewer(Composite parent) {
            Tree tree= new Tree(parent, getTreeStyle() | SWT.CHECK);
            tree.setFont(parent.getFont());
            checkboxViewer = new ContainerCheckedTreeViewer(tree);
            checkboxViewer.addCheckStateListener(new CheckStateListener());
            return checkboxViewer;
        }
        
        void setChecked(Object child, boolean newState) {
            checkboxViewer.setChecked(child, newState);
        }
    }

    class ProjectContextKey {
        final String projectName;
        final IStorage dslFile;
        boolean isChecked;  // set later
        public ProjectContextKey(String projectName, IStorage dslFile) {
            this.projectName = projectName;
            this.dslFile = dslFile;
        }
    }
    
    class DSLLabelProvider extends LabelProvider {

        WorkbenchLabelProvider provider = new WorkbenchLabelProvider();
        
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
    
    @SuppressWarnings("rawtypes")
    class DSLListAdapter implements ITreeListAdapter {

        public void customButtonPressed(TreeListDialogField field, int index) {
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

        public void selectionChanged(TreeListDialogField field) {
            if (canEdit()) {
                field.enableButton(IDX_EDIT, true);
            } else {
                field.enableButton(IDX_EDIT, false);
            }
        }

        public void doubleClicked(TreeListDialogField field) {
            edit();
        }

        public void keyPressed(TreeListDialogField field, KeyEvent event) { }

        public Object[] getChildren(TreeListDialogField field, Object element) {
            if (element instanceof String) {
                return elementsMap.get(element);
            } else {
                return null;
            }
        }

        public Object getParent(TreeListDialogField field, Object element) {
            if (element instanceof ProjectContextKey) {
                return ((ProjectContextKey) element).projectName;
            }
            return null;
        }

        public boolean hasChildren(TreeListDialogField field, Object element) {
            Object[] children = getChildren(field, element);
            return children != null && children.length > 0;
        }
    }

    
    DisabledScriptsCache cache;
    
    Map<String, ProjectContextKey[]> elementsMap;
    
    private CheckedTreeListDialogField tree;
    
    private DSLDStoreManager manager;

    private IWorkbenchPage page;
    
    private IPreferenceStore store = GroovyDSLCoreActivator.getDefault().getPreferenceStore();

    private Button autoAdd;

    private Button disableDSLDs;
    
    public DSLPreferencesPage() {
    }

    public DSLPreferencesPage(String title) {
        super(title);
    }

    public DSLPreferencesPage(String title, ImageDescriptor image) {
        super(title, image);
    }

    public void init(IWorkbench workbench) {
        manager = GroovyDSLCoreActivator.getDefault().getContextStoreManager();
        cache = new DisabledScriptsCache();
        elementsMap = new HashMap<String, DSLPreferencesPage.ProjectContextKey[]>();
        try {
            page = workbench.getActiveWorkbenchWindow().getActivePage();
        } catch (NullPointerException e) {
            // try later;
        }
            
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite= new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());

        
        tree = new CheckedTreeListDialogField(new DSLListAdapter(), LABELS, new DSLLabelProvider());
        tree.setTreeExpansionLevel(2);
        LayoutUtil.doDefaultLayout(composite, new DialogField[] { tree }, true, SWT.DEFAULT, SWT.DEFAULT);
        LayoutUtil.setHorizontalGrabbing(tree.getTreeControl(null));

        refresh();
        PixelConverter converter= new PixelConverter(parent);
        int buttonBarWidth= converter.convertWidthInCharsToPixels(24);
        tree.setButtonsMinWidth(buttonBarWidth);
            
        autoAdd = new Button(composite, SWT.CHECK);
        autoAdd.setText("Automatically add DSL Support to all Groovy projects");
        autoAdd.setSelection(store.getBoolean(DSLPreferencesInitializer.AUTO_ADD_DSL_SUPPORT));
        
        GridData data = new GridData(SWT.LEFT, SWT.TOP, true, false);
        data.horizontalSpan = 2;
        autoAdd.setLayoutData(data);
        disableDSLDs = new Button(composite, SWT.CHECK);
        disableDSLDs.setText("Disable DSLD support in your workspace. (Requires restart)");
        boolean isDisabled = store.getBoolean(DSLPreferencesInitializer.DSLD_DISABLED);
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
            IProject proj = ROOT.getProject(name);
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
                        ErrorDialog.openError(page.getWorkbenchWindow().getShell(), "Error opening editor", "See error log: " + e.getLocalizedMessage(), e.getStatus());
                    }
                    GroovyDSLCoreActivator.logException(e);
                }
            } else {
                if (page != null) {
                    ErrorDialog.openError(page.getWorkbenchWindow().getShell(), "Could not open editor", "File " + pck.dslFile
                            + " is not accessible.", new Status(IStatus.ERROR, GroovyDSLCoreActivator.PLUGIN_ID, "Could not open editor"));
                }
            }
        }
    }

    /**
     * @param storage
     * @return
     */
    private IEditorInput getEditorInput(IStorage storage) {
    	if (storage instanceof IFile && ((IFile) storage).getProject().equals(JavaModelManager.getExternalManager().getExternalFoldersProject())) {
    		return new FileStoreEditorInput(new LocalFile(new File(((IFile) storage).getLocationURI())));
    	} else {
    		return EditorUtility.getEditorInput(storage);
    	}
    }

    protected void refresh() {
        List<String> allStores = manager.getAllStores();
        elementsMap.clear();
        for (String element : allStores) {
            IProject project = toProject(element);
            if (project != null) {
                DSLDStore store = manager.getDSLDStore(project);
                if (store != null) {
                    IStorage[] keys = store.getAllContextKeys();
                    ProjectContextKey[] pck = new ProjectContextKey[keys.length];
                    for (int i = 0; i < pck.length; i++) {
                        pck[i] = new ProjectContextKey(element, keys[i]); 
                        pck[i].isChecked = ! cache.isDisabled(DSLDStore.toUniqueString(pck[i].dslFile));
                    }
                    elementsMap.put(element, pck);
                }
            }
        }
        Collections.sort(allStores);
        
        tree.setElements(allStores);
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

    protected void storeChecks() {
        Set<String> unchecked = new HashSet<String>();
        for (ProjectContextKey[] keys : elementsMap.values()) {
            for (ProjectContextKey key : keys) {
                if (! key.isChecked) {
                    unchecked.add(DSLDStore.toUniqueString(key.dslFile));
                }
            }
        }
        cache.setDisabled(unchecked);
    }
    

    private static final String EVENT = "Recompiling all DSLDs in the workspace.";


    protected void recompile() {
    	// re-compile all scripts and then wait for the results to refresh the UIs
    	new UIJob("Refresh DSLD launcher") {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                GroovyLogManager.manager.log(TraceCategory.DSL, EVENT);
                GroovyLogManager.manager.logStart(EVENT);
                GroovyDSLCoreActivator.getDefault().getContextStoreManager().initializeAll(true);
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
        storeChecks();
        store.setValue(DSLPreferencesInitializer.AUTO_ADD_DSL_SUPPORT, autoAdd.getSelection());
        
        boolean origDisabled = store.getBoolean(DSLPreferencesInitializer.DSLD_DISABLED);
        if (origDisabled != disableDSLDs.getSelection()) {
            store.setValue(DSLPreferencesInitializer.DSLD_DISABLED, disableDSLDs.getSelection());
            String newValue = disableDSLDs.getSelection() ? "enabled" : "disabled";
        
            boolean res = MessageDialog.openQuestion(getShell(), "Restart now?", "You have " + newValue + 
                    " DSLDs in your worksoace.  This setting will not coming effect until a restart has " +
                    "been performed.\n\nDo you want to restart now?");
            if (res) {
                Workbench.getInstance().restart();
            }
        }
        return super.performOk();
    }
}
