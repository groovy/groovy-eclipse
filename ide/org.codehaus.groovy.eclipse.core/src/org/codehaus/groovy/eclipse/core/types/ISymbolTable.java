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
package org.codehaus.groovy.eclipse.core.types;

/**
 * @author empovazan
 */
public interface ISymbolTable {
	/**
	 * Get the type of a name in the evaluation scope.
	 * 
	 * @param name The name of an instance including 'this'.
	 * @return Type for the name or null if none was found. This may be a ClassType, LocalVariable, Field or Property.
	 */
	GroovyDeclaration lookup(String name);
}