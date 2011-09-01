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
package org.codehaus.groovy.eclipse.dsl.checker;

import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;

/**
 * Performs static checking on all groovy files contained in the resource passed in
 * @author andrew
 * @created Aug 29, 2011
 */
public class ResourceTypeChecker {
    
    class CheckerVisitor implements IResourceVisitor {
        private IProgressMonitor monitor;

        CheckerVisitor(IProgressMonitor monitor) {
            this.monitor = monitor;
        }

        public boolean visit(IResource resource) throws CoreException {
            if (resource.isDerived()) {
                return false;
            }
            
            // first delete old markers
            resource.deleteMarkers(GroovyDSLCoreActivator.MARKER_ID, true, IResource.DEPTH_ZERO);
            
            if (resource.getType() == IResource.FILE && ContentTypeUtils.isGroovyLikeFileName(resource.getName())) {
                GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.create((IFile) resource);
                if (unit != null && unit.isOnBuildPath()) {
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    monitor.subTask(resource.getName());
                    handler.setResource((IFile) resource);
                    StaticTypeCheckerRequestor requestor = new StaticTypeCheckerRequestor(handler);
                    TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
                    visitor.visitCompilationUnit(requestor);
                }
            }
            return true;
        }
    }
    
    private final IStaticCheckerHandler handler;
    private final List<IResource> resources;

    public ResourceTypeChecker(IStaticCheckerHandler handler, String projectName) {
        this(handler, createProject(projectName));
    }

    /**
     * @param projectName
     * @return
     */
    private static List<IResource> createProject(String projectName) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (!GroovyNature.hasGroovyNature(project)) {
            throw new IllegalArgumentException("Invalid project: " + projectName);
        }
        return Collections.<IResource>singletonList(project);
    }

    public ResourceTypeChecker(IStaticCheckerHandler handler, List<IResource> resources) {
        this.handler = handler;
        this.resources = resources;
    }
    
    public void doCheck(IProgressMonitor monitor) throws CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.beginTask("Static type analysis", resources.size());
        for (IResource resource : resources) {
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
            CheckerVisitor visitor = new CheckerVisitor(monitor);
            resource.accept(visitor);
            monitor.worked(1);
        }
    }
}
