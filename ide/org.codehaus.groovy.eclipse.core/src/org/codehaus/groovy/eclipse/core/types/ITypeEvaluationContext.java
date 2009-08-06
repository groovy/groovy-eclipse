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
 * A context for type evaluation. The type evaluation context provides a type evaluator with symbol types, fields,
 * properties and methods for use in evaluating expressions.
 * 
 * @author empovazan
 */
public interface ITypeEvaluationContext {
	ClassLoader getClassLoader();
	
	String[] getImports();
	
	GroovyDeclaration lookupSymbol(String name);

	Field lookupField(String type, String name, boolean accessible, boolean staticAccess);

	Property lookupProperty(String type, String name, boolean accessible, boolean staticAccess);

	Method lookupMethod(String type, String name, String[] paramTypes, boolean accessible,
			boolean staticAccess);
}