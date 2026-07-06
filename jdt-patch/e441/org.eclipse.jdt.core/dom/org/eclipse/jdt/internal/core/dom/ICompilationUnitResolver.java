/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core.dom;

import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;

/**
 * This interface is used to resolve a jdt dom tree from source files.
 * It is contributed to via the compilationUnitResolver extension point.
 * This interface is currently internal only, and is not considered API.
 * This interface may be modified, changed, or removed at any time.
 *
* <p>
* <strong>EXPERIMENTAL</strong>. This class or interface has been added as
* part of a work in progress. There is no guarantee that this API will
* work or that it will remain the same. Please do not use this API without
* consulting with the Red Hat team.
* </p>
*
* See https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2641 for discussion on possible
* changes to this interface
*/
public interface ICompilationUnitResolver {
	/**
	 * Parse the ASTs and resolve the bindings for the given source files using the following options.
	 *
	 * @param sourceFilePaths the compilation units to create ASTs for
	 * @param encodings the given encoding for the source units
	 * @param bindingKeys the binding keys to create bindings for
	 * @param requestor the AST requestor that collects abstract syntax trees and bindings
	 * @param apiLevel Level of AST API desired.
	 * @param compilerOptions Compiler options. Defaults to JavaCore.getOptions().
	 * @param classpathList A list of classpaths to use during this operation
	 * @param flags Flags to to be used during this operation
	 * @param monitor A progress monitor
	 */
	void resolve(String[] sourceFilePaths, String[] encodings, String[] bindingKeys, FileASTRequestor requestor,
			int apiLevel, Map<String, String> compilerOptions, List<Classpath> classpathList, int flags,
			IProgressMonitor monitor);

	/**
	 * Parse the ASTs for the given source units using the following options.
	 *
	 * @param compilationUnits the compilation units to create ASTs for
	 * @param requestor the AST requestor that collects abstract syntax trees and bindings
	 * @param apiLevel Level of AST API desired.
	 * @param compilerOptions Compiler options. Defaults to JavaCore.getOptions().
	 * @param flags Flags to to be used during this operation
	 * @param monitor A progress monitor
	 */
	void parse(ICompilationUnit[] compilationUnits, ASTRequestor requestor, int apiLevel,
			Map<String, String> compilerOptions, int flags, IProgressMonitor monitor);

	/**
	 * Parse the given source paths with the following options.
	 *
	 * @param sourceFilePaths the compilation units to create ASTs for
	 * @param encodings the given encoding for the source units
	 * @param requestor the AST requester that collects abstract syntax trees and bindings
	 * @param apiLevel Level of AST API desired.
	 * @param compilerOptions Compiler options. Defaults to JavaCore.getOptions().
	 * @param flags Flags to to be used during this operation
	 * @param monitor A progress monitor
	 */
	void parse(String[] sourceFilePaths, String[] encodings, FileASTRequestor requestor, int apiLevel,
			Map<String, String> compilerOptions, int flags, IProgressMonitor monitor);

	/**
	 * Parse and resolve bindings for the given compilation units with the following options.
	 *
	 * @param compilationUnits the compilation units to create ASTs for
	 * @param bindingKeys the binding keys to create bindings for
	 * @param requestor the AST requester that collects abstract syntax trees and bindings
	 * @param apiLevel Level of AST API desired.
	 * @param compilerOptions Compiler options. Defaults to JavaCore.getOptions().
	 * @param project The project providing the context of the resolution
	 * @param workingCopyOwner  The owner of the working copy
	 * @param flags Flags to to be used during this operation
	 * @param monitor A progress monitor
	 */
	void resolve(ICompilationUnit[] compilationUnits, String[] bindingKeys, ASTRequestor requestor, int apiLevel,
			Map<String, String> compilerOptions, IJavaProject project, WorkingCopyOwner workingCopyOwner, int flags,
			IProgressMonitor monitor);



	/**
	 * Convert the given source unit into a CompilationUnit using the following options.
	 *
	 * @param sourceUnit A source unit
	 * @param initialNeedsToResolveBinding Initial guess as to whether we need to resolve bindings
	 * @param project The project providing the context of the conversion
	 * @param classpaths A list of classpaths to use during this operation
	 * @param focalPosition a position to focus on, or -1 if N/A
	 * @param apiLevel Level of AST API desired.
	 * @param compilerOptions Compiler options. Defaults to JavaCore.getOptions().
	 * @param parsedUnitWorkingCopyOwner The working copy owner of the unit
	 * @param typeRootWorkingCopyOwner The working copy owner of the type
	 * @param flags Flags to to be used during this operation
	 * @param monitor A progress monitor
	 * @return A CompilationUnit
	 */
	CompilationUnit toCompilationUnit(org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit, final boolean initialNeedsToResolveBinding, IJavaProject project, List<Classpath> classpaths, int focalPosition,
			int apiLevel, Map<String, String> compilerOptions, WorkingCopyOwner parsedUnitWorkingCopyOwner, WorkingCopyOwner typeRootWorkingCopyOwner, int flags, IProgressMonitor monitor);
}
