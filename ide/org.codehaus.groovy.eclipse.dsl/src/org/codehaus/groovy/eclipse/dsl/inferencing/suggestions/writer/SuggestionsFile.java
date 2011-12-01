/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.writer;

import java.lang.reflect.InvocationTargetException;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.InferencingSuggestionsManager;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateFileOperation;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-09-06
 */
public class SuggestionsFile {

    private IProject project;

    private SuggestionsFileProperties location;

    public SuggestionsFile(IProject project) {
        this.project = project;
        location = new SuggestionsFileProperties();

    }

    public IProject getProject() {
        return project;
    }

    /**
     * Return an existing file in an accessible project, or attempts to create
     * it if it does not exist
     * returns null if file was not created
     * 
     * @return
     */
    public IFile createFile() {
        String path = getPath();

        if (path != null) {
            IFile suggestionsFile = project.getFile(path);

            // Create it if it doesn't already exist
            if (!suggestionsFile.exists()) {
                suggestionsFile = createNewFile(suggestionsFile);
            }

            return suggestionsFile != null && suggestionsFile.exists() ? suggestionsFile : null;
        }
        return null;
    }

    protected String getPath() {
        return InferencingSuggestionsManager.getInstance().isValidProject(project) && location != null ? location
                .getWritingLocation() + location.getFileName() + '.' + location.getFileType() : null;
    }

    /**
     * Gets an existing file, or null if not such file exists. It does not
     * attempt to create a non existing file
     * 
     * @return
     */
    public IFile getFile() {
        String path = getPath();

        if (path != null) {
            IFile suggestionsFile = project.getFile(path);

            return suggestionsFile != null && suggestionsFile.exists() ? suggestionsFile : null;
        }
        return null;
    }

    /**
     * Return created file or null if file could not be created
     * 
     * @param fileHandle
     * @return
     */
    protected IFile createNewFile(IFile fileHandle) {
        // Do not create file if there is no runnable context

        IWorkbench workBench = PlatformUI.getWorkbench();
        if (workBench == null || fileHandle.exists()) {
            return null;
        }
        IRunnableContext runnableContext = workBench.getActiveWorkbenchWindow();
        final IFile newFile = fileHandle;

        // Run atomically
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) {
                CreateFileOperation op = new CreateFileOperation(newFile, null, null, "Creating Suggestions File");
                try {
                    op.execute(monitor, null);
                } catch (final ExecutionException e) {
                    GroovyDSLCoreActivator.logException(e);
                }
            }
        };
        try {

            runnableContext.run(true, true, op);
            newFile.refreshLocal(0, new NullProgressMonitor());

        } catch (InterruptedException e) {
            GroovyDSLCoreActivator.logException(e);
            return null;
        } catch (InvocationTargetException e) {
            GroovyDSLCoreActivator.logException(e);
            return null;
        } catch (CoreException e) {
            GroovyDSLCoreActivator.logException(e);
            return null;
        }

        return newFile;
    }

}
