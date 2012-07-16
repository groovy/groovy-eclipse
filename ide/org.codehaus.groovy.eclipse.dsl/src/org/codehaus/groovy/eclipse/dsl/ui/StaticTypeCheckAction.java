/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.ui;

import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.dsl.checker.IStaticCheckerHandler;
import org.codehaus.groovy.eclipse.dsl.checker.ResourceMarkerHandler;
import org.codehaus.groovy.eclipse.dsl.checker.ResourceTypeChecker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.progress.UIJob;

/**
 * Performs a static type check on all selected groovy files
 * @author andrew
 * @created May 24, 2011
 */
public class StaticTypeCheckAction extends AbstractCheckerAction implements IObjectActionDelegate {

    
    public void run(IAction action) {
        final List<IResource> selectionList = findSelection();
        if (selectionList != null) {
            UIJob job = new UIJob(shell != null ? shell.getDisplay() : null, "Static type checking") {
                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    ISchedulingRule rule = new MultiRule(selectionList.toArray(new IResource[0]));
                    getJobManager().beginRule(rule, monitor);
                    try {
                        perform(selectionList, monitor);
                    } finally {
                        getJobManager().endRule(rule);
                    }
                    return Status.OK_STATUS;
                }
            };
            job.setUser(true);
            job.setPriority(Job.LONG);
            job.schedule();
        }
    }

    public void perform(List<IResource> selectionList, IProgressMonitor monitor) {
        IStaticCheckerHandler handler = new ResourceMarkerHandler();
        ResourceTypeChecker checker = new ResourceTypeChecker(handler, selectionList, null, null, false);
        try {
            checker.doCheck(monitor);
        } catch (CoreException e) {
            GroovyCore.logException("Exception when performing static type checking", e);
        }
    }

    
}
