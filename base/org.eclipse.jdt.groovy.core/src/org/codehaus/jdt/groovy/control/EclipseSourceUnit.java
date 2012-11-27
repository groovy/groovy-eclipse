/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kris De Volder - Initial API and implementation
 *******************************************************************************/
package org.codehaus.jdt.groovy.control;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import org.eclipse.core.resources.IFile;

/**
 * Eclipse specific subclass of SourceUnit, attaches extra information to a SourceUnit that is specific to compilation in an Eclipse
 * context.
 * 
 * @author Kris De Volder
 * @since 2.5.2
 */
public class EclipseSourceUnit extends SourceUnit {

	final private IFile file;

	public EclipseSourceUnit(IFile resource, String filepath, String string, CompilerConfiguration groovyCompilerConfig,
			GroovyClassLoader classLoader, ErrorCollector errorCollector) {
		super(filepath, string, groovyCompilerConfig, classLoader, errorCollector);
		this.file = resource;
	}

	/**
	 * Will be null if workspace is closed (ie- batch compilation mode)
	 */
	public IFile getEclipseFile() {
		return file;
	}

	@Override
	public void convert() throws CompilationFailedException {
		super.convert();
		super.cst = null;
	}

}
