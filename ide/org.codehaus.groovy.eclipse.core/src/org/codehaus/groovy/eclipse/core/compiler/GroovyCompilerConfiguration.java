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
 * Default implementation of IGroovyCompilerConfiguration.
 * 
 * @author empovazan
 */
public class GroovyCompilerConfiguration implements IGroovyCompilerConfiguration {
	public static final int BUILD_CST = 1;

	public static final int BUILD_AST = 2;

	public static final int BUILD_CLASSES = 3;
	
	private String groovyVersion = "1.5.4";
	private boolean buildCST;
	private boolean buildAST;
	private boolean buildClasses;
	private boolean resolveAST;
	private boolean doNotResolveAST;
	private boolean errorRecovery;
	private boolean forceBuild;
	private String classPath = ".";
	private ClassLoader classLoader;
	private String outputPath = ".";
	
	public GroovyCompilerConfiguration() {
	}
	
	public GroovyCompilerConfiguration(int buildDefault) {
		setBuildDefault(buildDefault);
	}
	
	public void setGroovyVersion(String groovyVersion) {
		this.groovyVersion = groovyVersion;
	}
	
	private void setBuildDefault(int buildDefault) {
		if (buildDefault == BUILD_CST) {
			setBuildCST(true);
		} else if (buildDefault == BUILD_AST) {
			setBuildAST(true);
		} else if (buildDefault == BUILD_CLASSES) {
			setBuildClasses(true);
		}
	}
	
	public void setBuildCST(boolean buildCST) {
		this.buildCST = buildCST;
	}
	
	public void setBuildAST(boolean buildAST) {
		this.buildAST = buildAST;
	}
	
	public void setBuildClasses(boolean buildClasses) {
		this.buildClasses = buildClasses;
		setResolveAST(true);
	}
	
	public void setResolveAST(boolean resolveAST) {
		this.resolveAST = resolveAST;
	}
	
	public void setErrorRecovery(boolean errorRecovery) {
		this.errorRecovery = errorRecovery;
	}
	
	public void setForceBuild(boolean forceBuild) {
		this.forceBuild = forceBuild;
	}
	
	public void setClassPath(String classPath) {
		if (classPath == null || classPath.equals("")) {
			classPath = ".";
		}
		this.classPath = classPath;
	}
	
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	public void setOutputPath(String outputPath) {
		if (outputPath == null || outputPath.equals("")) {
			outputPath = ".";
		}
		this.outputPath = outputPath;
	}
	
	public String getGroovyVersion() {
		return groovyVersion;
	}
	
	public boolean getBuildCST() {
		return buildCST;
	}

	public boolean getBuildAST() {
		return buildAST;
	}

	public boolean getBuildClasses() {
		return buildClasses;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public String getClassPath() {
		return classPath;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public boolean getResolveAST() {
		return resolveAST;
	}

	public boolean isErrorRecovery() {
		return errorRecovery;
	}

	public boolean isForceBuild() {
		return forceBuild;
	}

	public boolean getUnResolvedAST() {
		return doNotResolveAST;
	}
	
	public void setUnResolvedAST(boolean doNotResolveAST) {
		this.doNotResolveAST = doNotResolveAST;
	}
}
