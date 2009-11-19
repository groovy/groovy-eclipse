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
package org.codehaus.groovy.eclipse.core.context;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.context.impl.SourceCodeContextFactory;
import org.eclipse.jface.text.IRegion;

/**
 * A source code context identifies some interesting location in source code.<br>
 * It is the answer to the question "where are we" e.g. "in a class", "in a closure", "in a method".<br>
 * The available contexts are:
 * <ul>
 * <li>Module - anywhere in the current source code.<br>
 * id = org.codehaus.groovy.eclipse.core.context.module</li>
 * <li>Module scope - any place outside of classes<br>
 * id = org.codehaus.groovy.eclipse.core.context.moduleScope</li>
 * <li>Class - anywhere inside a class.<br>
 * id = org.codehaus.groovy.eclipse.core.context.class</li>
 * <li>Class scope - where fields, properties and methods are declared.<br>
 * id = org.codehaus.groovy.eclipse.core.context.classScope</li>
 * <li>Constructor scope - inside any constructor scope.<br>
 * id = org.codehaus.groovy.eclipse.core.context.constructorScope</li>
 * <li>Contructor parameters - inside the parameter list of a contructor declaration.<br>
 * id = org.codehaus.groovy.eclipse.core.context.contructorParameters</li>
 * <li>Method scope - inside any method scope.<br>
 * id = org.codehaus.groovy.eclipse.core.context.methodScope</li>
 * <li>Method parameters - inside the parameter list of a method declaration.<br>
 * id = org.codehaus.groovy.eclipse.core.context.methodParameters</li>
 * <li>Closure scope - inside any closure.<br>
 * id = org.codehaus.groovy.eclipse.core.context.closureScope</li>
 * </ul>
 * <p>
 * This interface is not to be implemented by clients.
 * 
 * @see SourceCodeContextFactory
 * 
 * @author empovazan
 */
public interface ISourceCodeContext {
	public static final String MODULE = "org.codehaus.groovy.eclipse.core.context.module";

	public static final String MODULE_SCOPE = "org.codehaus.groovy.eclipse.core.context.moduleScope";

	public static final String CLASS = "org.codehaus.groovy.eclipse.core.context.class";

	public static final String CLASS_SCOPE = "org.codehaus.groovy.eclipse.core.context.classScope";

	public static final String CONSTRUCTOR_SCOPE = "org.codehaus.groovy.eclipse.core.context.constructorScope";

	public static final String CONSTRUCTOR_PARAMETERS = "org.codehaus.groovy.eclipse.core.context.constructorParameters";

	public static final String METHOD_SCOPE = "org.codehaus.groovy.eclipse.core.context.methodScope";

	public static final String METHOD_PARAMETERS = "org.codehaus.groovy.eclipse.core.context.methodParameters";

	public static final String CLOSURE_SCOPE = "org.codehaus.groovy.eclipse.core.context.closureScope";
	
	/**
	 * The unique ID of a context. IDs are used to bind a type extension to contexts.
	 */
	String getId();
	
	/**
	 * Get the path from this context to the root of the AST, typically the ModuleNode.
	 * 
	 * @return
	 */
	ASTNode[] getASTPath();

	public IRegion getRegion();
	
	public int getOffset();

	public int getLength();
	
	public ISourceBuffer getBuffer();
}
