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
 * Represents an actual problem encountered in a Java or Groovy resource, and
 * therefore contains invocation context for the problem (for example, a marker
 * handled), a location for the problem, ASTNodes, compilation unit and the
 * associated IResource containing the problem
 * 
 * @author Nieraj Singh
 * 
 */
public interface IQuickFixProblemContext {

	/**
	 * 
	 * @return compilation unit containing the problem. Never null.
	 */
	public ICompilationUnit getCompilationUnit();

	/**
	 * 
	 * @return non-null problem descriptor for the problem that requires a quick
	 *         fix
	 */
	public IProblemDescriptor getProblemDescriptor();

	/**
	 * Returns if the problem has error severity.
	 * 
	 * @return <code>true</code> if the problem has error severity
	 */
	public boolean isError();

	/**
	 * Returns the start offset of the problem.
	 * 
	 * @return the start offset of the problem
	 */
	public int getOffset();

	/**
	 * Returns the length of the problem.
	 * 
	 * @return the length of the problem
	 */
	public int getLength();

	/**
	 * Convenience method to evaluate the AST node covering this problem.
	 * 
	 * @param astRoot
	 *            The root node of the current AST
	 * @return Returns the node that covers the location of the problem
	 */
	public ASTNode getCoveringNode(CompilationUnit astRoot);

	/**
	 * Convenience method to evaluate the AST node covered by this problem.
	 * 
	 * @param astRoot
	 *            The root node of the current AST
	 * @return Returns the node that is covered by the location of the problem
	 */
	public ASTNode getCoveredNode(CompilationUnit astRoot);

	/**
	 * Returns an AST of the compilation unit, possibly only a partial AST
	 * focused on the selection offset (see
	 * {@link org.eclipse.jdt.core.dom.ASTParser#setFocalPosition(int)}). The
	 * returned AST is shared and therefore protected and cannot be modified.
	 * The client must check the AST API level and do nothing if they are given
	 * an AST they can't handle. (see
	 * {@link org.eclipse.jdt.core.dom.AST#apiLevel()}).
	 * 
	 * @return Returns the root of the AST corresponding to the current
	 *         compilation unit.
	 */
	public CompilationUnit getASTRoot();

	/**
	 * 
	 * @return the resource containing the problem.
	 */
	public IResource getResource();

}
