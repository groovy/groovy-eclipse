/*
 * Copyright 2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.proposals;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

/**
 * Base implementation of a quick fix problem.
 * 
 * @author Nieraj Singh
 * 
 */
public class QuickFixProblemContext {

	
	private final ProblemDescriptor problemDescriptor;
    private final IInvocationContext context;
    private final IProblemLocation location;

	public QuickFixProblemContext(ProblemDescriptor problemDescriptor, IInvocationContext context,
            IProblemLocation location) {
                this.problemDescriptor = problemDescriptor;
                this.context = context;
                this.location = location;
    }

    /**
     * 
     * @return compilation unit containing the problem. Never null.
     */
	public ICompilationUnit getCompilationUnit() {
		return context.getCompilationUnit();
	}

    /**
     * 
     * @return non-null problem descriptor for the problem that requires a quick
     *         fix
     */
	public ProblemDescriptor getProblemDescriptor() {
		return problemDescriptor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixProblemContext
	 * #isError()
	 */
	public boolean isError() {
		return location != null ? location.isError() : true;
	}

    /**
     * Returns the start offset of the problem.
     * 
     * @return the start offset of the problem
     */
	public int getOffset() {
		return location != null ? location.getOffset() : context.getSelectionOffset();
	}

    /**
     * Returns the length of the problem.
     * 
     * @return the length of the problem
     */
	public int getLength() {
        return location != null ? location.getLength() : context.getSelectionLength();
	}

	public ASTNode getCoveringNode(CompilationUnit astRoot) {
		return context.getCoveringNode();
	}

	public ASTNode getCoveredNode(CompilationUnit astRoot) {
		return context.getCoveredNode();
	}

	public CompilationUnit getASTRoot() {
		return context.getASTRoot();
	}

    /**
     * 
     * @return the resource containing the problem.
     */
	public IResource getResource() {
		return getCompilationUnit() != null ? getCompilationUnit()
				.getResource() : null;
	}

	
	public IInvocationContext getContext() {
        return context;
    }
	
	public IProblemLocation getLocation() {
        return location;
    }
}
