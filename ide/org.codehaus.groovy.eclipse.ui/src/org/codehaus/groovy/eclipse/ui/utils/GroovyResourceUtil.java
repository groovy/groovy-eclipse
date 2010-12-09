/*
 * Copyright 2009, 2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.ui.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.editor.actions.RenameToGroovyOrJavaAction;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceChange;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

/**
 *
 * @author Andrew Eisenberg
 * @author Nieraj Singh
 * @created 2010-11-18
 */
public class GroovyResourceUtil {

    public static final String GROOVY = ".groovy";

    public static final String JAVA = ".java";

    private GroovyResourceUtil() {
        // Utility
    }

    public static IStatus renameFile(String type, List<IResource> resources) {
        UIJob renameTo = new RenameToGroovyOrJavaJob(type, resources);
        renameTo.schedule();
        return Status.OK_STATUS;
    }

    /**
     * @author Andrew Eisenberg
     * @created Oct 17, 2009
     *
     */
    public static class RenameToGroovyOrJavaJob extends UIJob {

        private List<IResource> resources;

        private String javaOrGroovy;

        private RenameToGroovyOrJavaJob(String javaOrGroovy, List<IResource> resources) {
            super(getJobName(javaOrGroovy));
            this.resources = resources;
            this.javaOrGroovy = javaOrGroovy;
        }

        protected static String getJobName(String javaOrGroovy) {
            return "Rename to " + javaOrGroovy;
        }

        @Override
        public boolean belongsTo(Object family) {
            return RenameToGroovyOrJavaAction.class == family;
        }

        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            Set<IProject> affectedProjects = new HashSet<IProject>();
            final Set<IResource> filesAlreadyOpened = new HashSet<IResource>();

            for (IResource resource : resources) {
                if (resource != null) {
                    if (resource.getType() != IResource.FILE) {
                        continue;
                    }
                    IDE.saveAllEditors(new IFile[] { (IFile) resource }, true);
                    String name = convertName(resource);
                    RenameResourceChange change = new RenameResourceChange(resource.getFullPath(), name); //$NON-NLS-1$
                    try {
                        if (isOpenInEditor(resource)) {
                            filesAlreadyOpened.add(resource);
                        }

                        change.perform(monitor);

                        IProject project = resource.getProject();
                        if (!GroovyNature.hasGroovyNature(project)) {
                            affectedProjects.add(project);
                        }
                    } catch (CoreException e) {
                        String message = "Error converting file extension to " + javaOrGroovy + " for file " + resource.getName();
                        GroovyCore.logException(message, e);
                        return new Status(IStatus.ERROR, GroovyPlugin.PLUGIN_ID, message, e);
                    }
                }
            }

            if (!affectedProjects.isEmpty() && javaOrGroovy.equals(GROOVY)) {
                askToConvert(affectedProjects, this.getDisplay().getActiveShell());
            }

            reopenFiles(filesAlreadyOpened);
            return Status.OK_STATUS;
        }

        /**
         * @param file
         * @return
         */
        protected String convertName(IResource file) {
            String name = file.getName();
            name = name.substring(0, name.lastIndexOf('.'));
            name = name + javaOrGroovy;
            return name;
        }

        /**
         * Reopen files that were closed
         *
         * @param filesAlreadyOpened
         */
        protected void reopenFiles(Set<IResource> filesAlreadyOpened) {
            for (IResource origResource : filesAlreadyOpened) {
                String name = convertName(origResource);
                IFile newFile = origResource.getParent().getFile(new Path(name));
                try {
                    IDE.openEditor(getWorkbenchPage(), newFile);
                } catch (PartInitException e) {
                    GroovyCore.logException("Exception thrown when opening " + name + " in an editor", e);
                }
            }
        }

        /**
         * @return
         */
        private IWorkbenchPage getWorkbenchPage() {
            return PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage();
        }

        protected void askToConvert(Set<IProject> affectedProjects, Shell shell) {
            if (affectedProjects.size() == 0) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            if (affectedProjects.size() > 1) {
                sb.append("Projects ");
                for (IProject project : affectedProjects) {
                    sb.append(project.getName()).append(", ");
                }
                sb.replace(sb.length() - 2, 2, " do ");
            } else {
                sb.append("Project ").append(affectedProjects.iterator().next().getName()).append(" does ");
            }
            sb.append("have the Groovy nature.  Do you want to add it?");

            boolean yes = MessageDialog.openQuestion(shell, "Convert to Groovy?", sb.toString());
            if (yes) {
                for (IProject project : affectedProjects) {
                    GroovyRuntime.addGroovyRuntime(project);
                }
            }
        }

        /**
         * @param file
         * @return
         */
        protected boolean isOpenInEditor(IResource file) {
            try {
                IEditorReference[] refs = getWorkbenchPage().getEditorReferences();
                for (IEditorReference ref : refs) {
                    try {
                        if (ref.getEditorInput() instanceof IFileEditorInput) {
                            IFileEditorInput input = (IFileEditorInput) ref.getEditorInput();
                            if (input.getFile().equals(file)) {
                                return true;
                            }
                        }
                    } catch (PartInitException e) {
                        // Safe to ignore. This can happen when a class file
                        // editor
                        // was open, but its underlying resource was deleted.
                    }
                }
            } catch (NullPointerException npe) {
                // workbench is shutting down, editors cannot be reached
                // ok to ignore.
            } catch (IndexOutOfBoundsException e) {
                // no open workbench windows. OK to ignore
            }
            return false;
        }
    }

}
