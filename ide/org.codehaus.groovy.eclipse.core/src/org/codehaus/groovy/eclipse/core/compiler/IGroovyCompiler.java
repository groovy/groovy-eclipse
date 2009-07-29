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

import java.io.InputStream;

/**
 * Interface to a Groovy compiler.
 * 
 * @author empovazan
 */
public interface IGroovyCompiler {
	void compile(String fileName, InputStream inputStream, IGroovyCompilerConfiguration configuration, IGroovyCompilationReporter reporter);
	
	void compile(String fileName, IGroovyCompilerConfiguration configuration, IGroovyCompilationReporter reporter);
	
	void compile(String[] fileNames, IGroovyCompilerConfiguration configuration, IGroovyCompilationReporter reporter);
}