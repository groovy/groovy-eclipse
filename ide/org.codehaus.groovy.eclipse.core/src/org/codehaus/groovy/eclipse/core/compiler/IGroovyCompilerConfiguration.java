/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.compiler;

/**
 * Interface used to control a build. All boolean properties are set to false by default. All paths are set to "."
 * <p>
 * Implementations are not expected to be thread safe - instances must not change while in use by a compiler.
 * 
 * @author empovazan
 */
public interface IGroovyCompilerConfiguration {
	/**
	 * Get the class path. The path is specified using the platform specific path delimiter. If
	 * {@link #getClassLoader()} does not return null, it overrides this class path.
	 * 
	 * @return The class path or '.' if not set.
	 */
	String getClassPath();

	/**
	 * The class loader to use for compiling. The class loader overrides the class path available in
	 * {@link #getClassPath()}.
	 * 
	 * @return The class loader or null.
	 */
	ClassLoader getClassLoader();

	/**
	 * Get the output path in which to store generated classes.
	 * @return An OS dependent path to a directory.
	 */
	String getOutputPath();

	/**
	 * @return The Groovy version that is to be compiled.
	 */
	String getGroovyVersion();

	/**
	 * @return True to enable error recovering compilation.
	 */
	boolean isErrorRecovery();
	
	/**
	 * @return If true, and if {@link #isErrorRecovery()} is true, then the compiler will attempt to force the
	 *         generation of class files. This method should not be used for final class building, however may be useful
	 *         for some kinds of analysis tools.
	 */
	boolean isForceBuild();

	/**
	 * @return If true, build and report parse trees (concrete syntax tree).
	 */
	boolean getBuildCST();

	/**
	 * @return If true, build and report abstract syntax trees.
	 */
	boolean getBuildAST();
	
	/**
	 * @return If true, the built ASTs are resolved with class information.
	 */
	boolean getResolveAST();
	
	/**
	 * @return If true the AST is only built till phase 3 (CONVERSION), class information are not resolved
	 */
	boolean getUnResolvedAST();

	/**
	 * @return If true, build class files.
	 */
	boolean getBuildClasses();
}