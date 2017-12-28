/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.eclipse.ui.utils.GroovyResourceUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Rename the file extension of a file to groovy or to java
 */
public abstract class RenameToGroovyOrJavaAction implements IWorkbenchWindowActionDelegate {

    private ISelection selection;

    private String javaOrGroovy;

    public RenameToGroovyOrJavaAction(String javaOrGroovy) {
        this.javaOrGroovy = javaOrGroovy;
    }

    public void run(IAction action) {
        if (selection instanceof IStructuredSelection) {
            GroovyResourceUtil.renameFile(javaOrGroovy, getResources((IStructuredSelection) selection));
        }
    }

    /**
     * @return non-null list of resources in the context selection. May be empty
     */
    protected List<IResource> getResources(IStructuredSelection selection) {
        List<IResource> resources = new ArrayList<>();
        if (selection != null) {
            for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                Object object = iter.next();
                if (object instanceof IAdaptable) {
                    IResource file = Adapters.adapt(object, IResource.class);
                    if (file != null) {
                        if (file.getType() != IResource.FILE) {
                            continue;
                        }
                        resources.add(file);
                    }
                }
            }
        }
        return resources;
    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }

    public void dispose() {
        selection = null;
    }

    public void init(IWorkbenchWindow window) {
    }
}
