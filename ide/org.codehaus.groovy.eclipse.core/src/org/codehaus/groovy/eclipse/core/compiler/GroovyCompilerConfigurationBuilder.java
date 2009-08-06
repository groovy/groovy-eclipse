 /*
 * Copyright 2003-2009 the original author or authors.
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
