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

/**
 * Base implementation of a quick fix problem.
 * 
 * @author Nieraj Singh
 * 
 */
public class QuickFixProblemContext implements IQuickFixProblemContext {

	private ICompilationUnit unit;
	private ASTNode coveredNode;
	private ASTNode coveringNode;
	private CompilationUnit root;
	private IProblemDescriptor problemDescriptor;
	private boolean isError;
	private int length;
	private int offset;

	public QuickFixProblemContext(IProblemDescriptor problemDescriptor,
			ICompilationUnit unit, ASTNode coveredNode, ASTNode coveringNode,
			CompilationUnit root, boolean isError, int length, int offset) {
		this.unit = unit;
		this.coveredNode = coveredNode;
		this.coveringNode = coveringNode;
		this.root = root;
		this.problemDescriptor = problemDescriptor;
		this.isError = isError;
		this.length = length;
		this.offset = offset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixProblemContext
	 * #getCompilationUnit()
	 */
	public ICompilationUnit getCompilationUnit() {
		return unit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixProblemContext
	 * #getProblemDescriptor()
	 */
	public IProblemDescriptor getProblemDescriptor() {
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
		return isError;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixProblemContext
	 * #getOffset()
	 */
	public int getOffset() {
		return offset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixProblemContext
	 * #getLength()
	 */
	public int getLength() {
		return length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixProblemContext
	 * #getCoveringNode(org.eclipse.jdt.core.dom.CompilationUnit)
	 */
	public ASTNode getCoveringNode(CompilationUnit astRoot) {
		return coveringNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixProblemContext
	 * #getCoveredNode(org.eclipse.jdt.core.dom.CompilationUnit)
	 */
	public ASTNode getCoveredNode(CompilationUnit astRoot) {
		return coveredNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixProblemContext
	 * #getASTRoot()
	 */
	public CompilationUnit getASTRoot() {
		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixProblemContext
	 * #getResource()
	 */
	public IResource getResource() {
		return getCompilationUnit() != null ? getCompilationUnit()
				.getResource() : null;
	}

}
