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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.dsl.DSLDStore;
import org.codehaus.groovy.eclipse.dsl.DSLDStoreManager;
import org.codehaus.groovy.eclipse.dsl.DSLPreferences;
import org.codehaus.groovy.eclipse.dsl.DisabledScriptsCache;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.earlystartup.InitializeAllDSLDs;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.TreeListDialogField;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.PixelConverter;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.WorkbenchLabelProvider;

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
        final String dslFileName;
        boolean isChecked;  // set later
        public ProjectContextKey(String projectName, String dslFileName) {
            this.projectName = projectName;
            this.dslFileName = dslFileName;
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
                IFile file = toFile(element);
                if (file != null) {
                    return file.getProjectRelativePath().toPortableString();
                }
            }
            return super.getText(element);
        }
        
        public Image getImage(Object element) {
            IProject proj = toProject(element);
            if (proj != null) {
                return provider.getImage(proj);
            }
            return provider.getImage(toFile(element));
        }
    }
    
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
        
        PixelConverter converter= new PixelConverter(parent);
        
        Composite composite= new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        
        tree = new CheckedTreeListDialogField(new DSLListAdapter(), LABELS, new DSLLabelProvider());
        tree.setTreeExpansionLevel(2);
        LayoutUtil.doDefaultLayout(composite, new DialogField[] { tree }, true, SWT.DEFAULT, SWT.DEFAULT);
        
        LayoutUtil.setHorizontalGrabbing(tree.getTreeControl(null));

        refresh();
        int buttonBarWidth= converter.convertWidthInCharsToPixels(24);
        tree.setButtonsMinWidth(buttonBarWidth);
            
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
    
    protected IFile toFile(Object element) {
        IFile file = ROOT.getFile(new Path(((ProjectContextKey) element).dslFileName));
        if (file.isAccessible()) {
            return file;
        } else {
            return null;
        }
    }
    
    
    
    protected boolean canEdit() {
        List<?> selected = tree.getSelectedElements();
        return selected.size() == 1 && selected.get(0) instanceof ProjectContextKey;
    }
    
    protected void edit() {
        if (canEdit()) {
            List<?> selected = tree.getSelectedElements();
            ProjectContextKey pck = (ProjectContextKey) selected.get(0);
            IFile file = ROOT.getFile(new Path(pck.dslFileName));
            if (file.isAccessible()) {
                try {
                    if (page != null) {
                        IDE.openEditor(page, file, true, true);
                    }
                } catch (PartInitException e) {
                    ErrorDialog.openError(page.getWorkbenchWindow().getShell(), "Error opening editor", "See error log: " + e.getLocalizedMessage(), e.getStatus());
                    GroovyDSLCoreActivator.logException(e);
                }
            } else {
                ErrorDialog.openError(page.getWorkbenchWindow().getShell(), "Could not open editor", "File " + pck.dslFileName
                        + " is not accessible.", new Status(IStatus.ERROR, GroovyDSLCoreActivator.PLUGIN_ID, "Could not open editor"));
            }
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
                    String[] keys = store.getAllContextKeys();
                    ProjectContextKey[] pck = new ProjectContextKey[keys.length];
                    for (int i = 0; i < pck.length; i++) {
                        pck[i] = new ProjectContextKey(element, keys[i]); 
                        pck[i].isChecked = ! cache.isDisabled(pck[i].dslFileName);
                    }
                    elementsMap.put(element, pck);
                }
            }
        }

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
        List<String> unchecked = new ArrayList<String>();
        for (ProjectContextKey[] keys : elementsMap.values()) {
            for (ProjectContextKey key : keys) {
                if (! key.isChecked) {
                    unchecked.add(key.dslFileName);
                }
            }
        }
        DSLPreferences.setDisabledScripts(unchecked.toArray(new String[0]));
    }
    
    protected void recompile() {
        new InitializeAllDSLDs().initializeAll();
    }
    
    @Override
    protected void performDefaults() {
        super.performDefaults();
        checkAll(true);
    }

    @Override
    public boolean performOk() {
        storeChecks();
        return super.performOk();
    }
}
