/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com>
 *          - Bug 44162 [Wizards]  Define constants for wizard ids of new.file, new.folder, and new.project
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;


// Initially taken from org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard
/**
 * Standard workbench wizard that create a new file resource in the workspace.
 * <p>
 * This class may be instantiated and used without further configuration;
 * this class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * IWorkbenchWizard wizard = new BasicNewFileResourceWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, a file resource at the user-specified
 * workspace path is created, the dialog closes, and the call to
 * <code>open</code> returns.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class NewDSLDWizard extends BasicNewResourceWizard {
    
    class NewDSLDWizardPage extends WizardNewFileCreationPage {

        public NewDSLDWizardPage(String pageName, IStructuredSelection selection) {
            super(pageName, selection);
        }
        
        
        @Override
        protected InputStream getInitialContents() {
            return new StringInputStream(
                    "// this is a DSLD file\n" +
            		"// start off creating a custom DSL Descriptor for your Groovy DSL\n" +
            		"\n" +
            		"// The following snippet adds the 'newProp' to all types that are a subtype of GroovyObjects\n" +
            		"// contribute(currentType(subType('groovy.lang.GroovyObject'))) {\n" +
            		"//   property name : 'newProp', type : String, provider : 'Sample DSL', doc : 'This is a sample.  You should see this in content assist for GroovyObjects: <pre>newProp</pre>'\n" +
            		"// }\n");
        }
        
        /**
         * Check that containing project is a groovy project (if not---error).
         * Check that containing folder is in a source folder (if not---warning).
         */
        @Override
        protected boolean validatePage() {
            if (!super.validatePage()) {
                return false;
            }
            
            IPath path = getContainerFullPath();
            IProject project; 
            if (path.segmentCount() > 1) {
                IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
                project = folder.getProject();
            } else if (path.segmentCount() == 1) {
                project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.lastSegment());
            } else {
                project = null;
                setErrorMessage("No folder is selected");
                return false;
            }
            if (!project.exists()) {
                setErrorMessage("Project " + project.getName() + " does not exist.");
                return false;
            }
            if (!GroovyNature.hasGroovyNature(project)) {
                setErrorMessage("Project " + project.getName() + " is not a groovy project.");
                return false;
            }
            
            IJavaProject javaProject = JavaCore.create(project);
            try {
                // check that the folder is inside of a source folder
                IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
                boolean inSourceFolder = false;
                for (IClasspathEntry entry : rawClasspath) {
                    IPath sourcePath = entry.getPath();
                    if (sourcePath.isPrefixOf(path)) {
                        inSourceFolder = true;
                        break;
                    }
                }
                if (!inSourceFolder) {
                    setMessage("Path is not in a source folder.  It is significantly easier to edit DSLDs when they are in source folders", 
                            IMessageProvider.WARNING);
                }
            } catch (JavaModelException e) {
                GroovyCore.logException("Exception while creating DSLD file", e);
                setErrorMessage(e.getMessage());
                return false;
            }
            return true;
        }
    }
    
    class StringInputStream extends InputStream {

        private Reader reader;
        
        public StringInputStream(String contents){
            this.reader = new StringReader(contents);
        }
        
        /* (non-Javadoc)
         * @see java.io.InputStream#read()
         */
        public int read() throws IOException {
            return reader.read();
        }

        
        public void close() throws IOException {
            reader.close();
        } 
        
    }


    private WizardNewFileCreationPage mainPage;

    /**
     * Creates a wizard for creating a new file resource in the workspace.
     */
    public NewDSLDWizard() {
        super();
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    public void addPages() {
        super.addPages();
        mainPage = new NewDSLDWizardPage("newDSLDFilePage", getSelection());//$NON-NLS-1$
        mainPage.setTitle("DSLD File");
        mainPage.setDescription("Create a new Groovy DSL Descriptor"); 
        mainPage.setFileExtension("dsld");
        addPage(mainPage);
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchWizard.
     */
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        super.init(workbench, currentSelection);
        setWindowTitle("New DSLD File");
        setNeedsProgressMonitor(true);
    }

    /* (non-Javadoc)
     * Method declared on BasicNewResourceWizard.
     */
    protected void initializeDefaultPageImageDescriptor() {
       ImageDescriptor desc = GroovyDSLCoreActivator.imageDescriptorFromPlugin(GroovyDSLCoreActivator.PLUGIN_ID, "icons/GROOVY.png");
	   setDefaultPageImageDescriptor(desc);
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    public boolean performFinish() {
        IFile file = mainPage.createNewFile();
        if (file == null) {
			return false;
		}

        selectAndReveal(file);

        // Open editor on new file.
        IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
        try {
            if (dw != null) {
                IWorkbenchPage page = dw.getActivePage();
                if (page != null) {
                    IDE.openEditor(page, file, true);
                }
            }
        } catch (PartInitException e) {
            DialogUtil.openError(dw.getShell(), ResourceMessages.FileResource_errorMessage, 
                    e.getMessage(), e);
        }

        return true;
    }
}
