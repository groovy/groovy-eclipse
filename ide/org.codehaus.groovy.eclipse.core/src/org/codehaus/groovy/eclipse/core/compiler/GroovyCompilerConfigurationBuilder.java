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
 * A builder to ease the creation of a configuration.
 * 
 * @author empovazan
 */
public class GroovyCompilerConfigurationBuilder {
	private GroovyCompilerConfiguration config;

	public GroovyCompilerConfigurationBuilder() {
		this.config = new GroovyCompilerConfiguration();
	}
	
	public GroovyCompilerConfigurationBuilder classPath(String path) {
		config.setClassPath(path);
		return this;
	}

	public GroovyCompilerConfigurationBuilder classLoader(ClassLoader classLoader) {
		config.setClassLoader(classLoader);
		return this;
	}

	public GroovyCompilerConfigurationBuilder outputPath(String path) {
		config.setOutputPath(path);
		return this;
	}

	public GroovyCompilerConfigurationBuilder groovyVersion(String version) {
		config.setGroovyVersion(version);
		return this;
	}

	public GroovyCompilerConfigurationBuilder errorRecovery() {
		config.setErrorRecovery(true);
		return this;
	}
	
	public GroovyCompilerConfigurationBuilder forceBuild() {
		config.setForceBuild(true);
		return this;
	}
	
	public GroovyCompilerConfigurationBuilder buildCST() {
		config.setBuildCST(true);
		return this;
	}

	public GroovyCompilerConfigurationBuilder buildAST() {
		config.setBuildAST(true);
		return this;
	}
	
	public GroovyCompilerConfigurationBuilder resolveAST() {
		config.setResolveAST(true);
		return this;
	}

	public GroovyCompilerConfigurationBuilder buildClasses() {
		config.setBuildClasses(true);
		return this;
	}
	
	public GroovyCompilerConfigurationBuilder doNotResolveAST(){
		config.setUnResolvedAST(true);
		return this;
	}
	
	public GroovyCompilerConfiguration done() {
		return config;
	}
}
