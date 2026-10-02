/*******************************************************************************
 * Copyright (c) 2026 Eclipse Foundation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Arcadiy Ivanov - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;

/**
 * A search participant for non-Java source files registered under the
 * {@code org.eclipse.jdt.core.javaDerivedSource} content type (e.g. Kotlin,
 * Scala).
 * <p>
 * Subclasses must implement {@link #locateCallees(IMember, SearchDocument,
 * IProgressMonitor)} to enable outgoing call hierarchy for their language.
 * <p>
 * This class is intended to be subclassed by clients contributing to the
 * {@code org.eclipse.jdt.core.derivedSourceSearchParticipant} extension point.
 *
 * @since 3.46
 */
public abstract class DerivedSourceSearchParticipant extends SearchParticipant {

	/**
	 * Locates methods and types invoked by the given member. Called by the call
	 * hierarchy engine when Java AST-based callee analysis is not available
	 * (i.e., the member's source is not Java).
	 *
	 * <p>Each returned {@link SearchMatch} represents a call site within the
	 * member's body:
	 * <ul>
	 *   <li>{@link SearchMatch#getElement()} — an {@link IMember}
	 *       representing the callee. At minimum,
	 *       {@link org.eclipse.jdt.core.IJavaElement#getElementName() getElementName()} and
	 *       {@link org.eclipse.jdt.core.IJavaElement#getElementType() getElementType()} must
	 *       return meaningful values. The call hierarchy engine will attempt to resolve this
	 *       to a full declaration via declaration search.</li>
	 *   <li>{@link SearchMatch#getOffset()} / {@link SearchMatch#getLength()} —
	 *       the call site location in the caller's source.</li>
	 *   <li>{@link SearchMatch#getResource()} — the caller's resource.</li>
	 * </ul>
	 *
	 * @param caller   the member whose callees are requested
	 * @param document the search document for the caller's source file
	 * @param monitor  progress monitor, or {@code null}
	 * @return array of search matches representing call sites (never null)
	 * @throws CoreException if an error occurs during callee analysis
	 * @since 3.46
	 */
	public abstract SearchMatch[] locateCallees(IMember caller, SearchDocument document,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns an ICompilationUnit for the given source file, or null if this
	 * participant does not provide structured models. Called by language
	 * servers to resolve non-Java source files to type roots for features
	 * like document symbols, hover, go-to-definition, and code lenses.
	 *
	 * <p>The default implementation returns null. Subclasses that provide
	 * structured models for their language should override this to return
	 * a compilation unit populated with type/method/field children.
	 *
	 * @param file the workspace file
	 * @return compilation unit, or null
	 * @since 3.46
	 */
	public ICompilationUnit getCompilationUnit(IFile file) {
		return null;
	}
}
