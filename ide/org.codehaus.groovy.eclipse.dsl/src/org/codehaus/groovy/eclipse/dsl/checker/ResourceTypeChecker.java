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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.codehaus.groovy.ast.Comment;
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
import org.eclipse.jdt.internal.core.util.Util;

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
            
            handler.handleResourceStart(resource);
            
            if (resource.getType() == IResource.FILE && ContentTypeUtils.isGroovyLikeFileName(resource.getName())) {
                if (Util.isExcluded(resource, includes, excludes)) {
                    return false;
                }
                
                GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.create((IFile) resource);
                if (unit != null && unit.isOnBuildPath()) {
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    monitor.subTask(resource.getName());
                    handler.setResource((IFile) resource);
                    Map<Integer, String> commentsMap = findComments(unit);
                    StaticTypeCheckerRequestor requestor = new StaticTypeCheckerRequestor(handler, commentsMap, onlyAssertions);
                    TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
                    try {
                        unit.becomeWorkingCopy(monitor);
                        visitor.visitCompilationUnit(requestor);
                    } finally {
                        unit.discardWorkingCopy();
                    }
                }
            }
            return true;
        }

        private Map<Integer, String> findComments(GroovyCompilationUnit unit) {
            List<Comment> comments = unit.getModuleNode().getContext().getComments();
            Map<Integer, String> allComments = new HashMap<Integer, String>(comments.size());
            for (Comment comment : comments) {
                StringTokenizer stok = new StringTokenizer(comment.toString());
                String type = null;
                if (stok.hasMoreTokens()) {
                    // consume the comment start
                    String val = stok.nextToken();
                    int typeIndex = val.indexOf("TYPE:");
                    if (typeIndex > 0) {
                        type = val.substring(typeIndex + "TYPE:".length());
                        if (type.length() == 0) {
                            type = null;
                        }
                    }
                }
                String candidate;
                if (stok.hasMoreTokens() && (candidate = stok.nextToken()).startsWith("TYPE:")) {
                    // may or may not have a space after the colon
                    if (candidate.equals("TYPE:")) {
                        if (stok.hasMoreTokens()) {
                            type = stok.nextToken();
                        }
                    } else {
                        String[] split = candidate.split("\\:");
                        type = split[1];
                    }
                }
                if (type != null) {
                    allComments.put(comment.sline, type);
                }
            }
            return allComments;
        }
    }
    
    private final IStaticCheckerHandler handler;
    private final List<IResource> resources;

    protected boolean onlyAssertions;
    protected final char[][] includes;
    protected final char[][] excludes;
    
    public ResourceTypeChecker(IStaticCheckerHandler handler, String projectName, char[][] includes, char[][] excludes, boolean onlyAssertions) {
        this(handler, createProject(projectName), includes, excludes, onlyAssertions);
    }

    public ResourceTypeChecker(IStaticCheckerHandler handler, List<IResource> resources, char[][] includes, char[][] excludes, boolean onlyAssertions) {
        this.handler = handler;
        this.resources = resources;
        this.includes = includes;
        this.excludes = excludes;
        this.onlyAssertions = onlyAssertions;
    }

    private static List<IResource> createProject(String projectName) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (!GroovyNature.hasGroovyNature(project)) {
            throw new IllegalArgumentException("Invalid project: " + projectName);
        }
        return Collections.<IResource>singletonList(project);
    }

    /**
     * Performs the tpe checking on the selected resources.
     * @param monitor progress monitor, can be null
     * @return true iff no type problems were found
     * @throws CoreException
     */
    public boolean doCheck(IProgressMonitor monitor) throws CoreException {
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
        return handler.finish(null);
    }
}
