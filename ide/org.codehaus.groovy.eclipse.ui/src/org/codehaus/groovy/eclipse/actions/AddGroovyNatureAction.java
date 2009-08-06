 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.actions;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * AddGroovyNatureAction is responsible for managing the addition of the Groovy nature to a java
 * project.
 * 
 * @author Andrew
 */
public class AddGroovyNatureAction implements IObjectActionDelegate {
    private List<IProject> currSelected = new LinkedList<IProject>();

    public void run(final IAction action) {
        
        if (currSelected != null && currSelected.size() > 0) {
            GroovyCore.trace("AddGroovySupportAction.run()");
            
            for (IProject project : currSelected) {
                GroovyCore.trace("   to " + project.getName());
                GroovyRuntime.addGroovyRuntime(project);
            }
        }
    }

    /**
     * @see IObjectActionDelegate#selectionChanged
     */
    public void selectionChanged(final IAction action, final ISelection selection) {
        currSelected.clear();
        List<IProject> newSelected = new LinkedList<IProject>();
        boolean enabled = true;
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection newSelection = (IStructuredSelection) selection;
            for (Iterator<?> iter = newSelection.iterator(); iter.hasNext();) {
                Object object = iter.next();
                if (object instanceof IAdaptable) {
                    IProject project = (IProject) ((IAdaptable)object).getAdapter(IProject.class);  
                    if(project != null) {
                        newSelected.add(project);
                    } else {
                        enabled = false;
                        break;
                    }
                } else {
                    enabled = false;
                    break;
                }
            }
            if (action != null) {
                action.setEnabled(enabled);
            }
        }

        if (enabled) {
            this.currSelected = newSelected;
        }
    }

    /**
     * @see IObjectActionDelegate#setActivePart
     */
    public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
    }
}
