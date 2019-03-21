/*
 * Copyright 2009-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.quickfix.proposals;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

public class QuickFixProblemContext {

    private final ProblemDescriptor problemDescriptor;
    private final IInvocationContext context;
    private final IProblemLocation location;

    public QuickFixProblemContext(ProblemDescriptor problemDescriptor, IInvocationContext context, IProblemLocation location) {
        this.problemDescriptor = problemDescriptor;
        this.context = context;
        this.location = location;
    }

    public CompilationUnit getASTRoot() {
        return context.getASTRoot();
    }

    public ICompilationUnit getCompilationUnit() {
        return context.getCompilationUnit();
    }

    public IInvocationContext getContext() {
        return context;
    }

    public ASTNode getCoveredNode(CompilationUnit astRoot) {
        return context.getCoveredNode();
    }

    public ASTNode getCoveringNode(CompilationUnit astRoot) {
        return context.getCoveringNode();
    }

    public IProblemLocation getLocation() {
        return location;
    }

    public boolean isError() {
        return (location != null ? location.isError() : true);
    }

    public int getLength() {
        return (location != null ? location.getLength() : context.getSelectionLength());
    }

    public int getOffset() {
        return (location != null ? location.getOffset() : context.getSelectionOffset());
    }

    public ProblemDescriptor getProblemDescriptor() {
        return problemDescriptor;
    }

    public IResource getResource() {
        return Adapters.adapt(getCompilationUnit(), IResource.class);
    }
}
