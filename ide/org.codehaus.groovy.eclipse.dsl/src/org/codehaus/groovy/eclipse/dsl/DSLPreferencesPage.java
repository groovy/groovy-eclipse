package org.codehaus.groovy.eclipse.dsl;

import java.util.List;

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
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class DSLPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {
    
    private static final IWorkspaceRoot ROOT = ResourcesPlugin.getWorkspace().getRoot();

    private final String[] LABELS = { "Edit...", "Recompile Scripts", "Refresh List" };
    private final int IDX_EDIT = 0;
    private final int IDX_RECOMPILE = 1;
    private final int IDX_REFRESH = 2;
    
    class ProjectContextKey {
        final String projectName;
        final String dslFileName;
        public ProjectContextKey(String projectName, String dslFileName) {
            this.projectName = projectName;
            this.dslFileName = dslFileName;
        }
    }
    
    class DSLLabel extends LabelProvider {

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
    
    class Adapter implements ITreeListAdapter {

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
                IProject project = toProject(element);
                if (project != null) {
                    DSLDStore store = manager.getDSLDStore(project);
                    if (store != null) {
                        String[] keys = store.getAllContextKeys();
                        ProjectContextKey[] pck = new ProjectContextKey[keys.length];
                        for (int i = 0; i < pck.length; i++) {
                            pck[i] = new ProjectContextKey((String) element, keys[i]); 
                        }
                        return pck;
                    }
                }
            }
            return null;
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

    
    
    private TreeListDialogField tree;
    
    private final DSLDStoreManager manager;

    private IWorkbenchPage page;
    
    public DSLPreferencesPage() {
        manager = GroovyDSLCoreActivator.getDefault().getContextStoreManager();
    }

    public DSLPreferencesPage(String title) {
        super(title);
        manager = GroovyDSLCoreActivator.getDefault().getContextStoreManager();
    }

    public DSLPreferencesPage(String title, ImageDescriptor image) {
        super(title, image);
        manager = GroovyDSLCoreActivator.getDefault().getContextStoreManager();
    }

    public void init(IWorkbench workbench) {
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
        
        tree = new TreeListDialogField(new Adapter(), LABELS, new DSLLabel());
        refresh();
        LayoutUtil.doDefaultLayout(composite, new DialogField[] { tree }, true, SWT.DEFAULT, SWT.DEFAULT);
        LayoutUtil.setHorizontalGrabbing(tree.getTreeControl(null));

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
        tree.setElements(manager.getAllStores());
        tree.refresh();
    }
    
    protected void recompile() {
        new InitializeAllDSLDs().initializeAll();
    }
}
