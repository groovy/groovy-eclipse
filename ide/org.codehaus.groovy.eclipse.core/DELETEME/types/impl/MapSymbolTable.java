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
package org.codehaus.groovy.eclipse.core.types.impl;

import static org.codehaus.groovy.eclipse.core.types.Modifiers.ACC_PUBLIC;
import static org.codehaus.groovy.eclipse.core.util.MapUtil.newMap;
import static org.eclipse.jdt.core.Signature.createTypeSignature;

import java.util.Map;

import org.codehaus.groovy.eclipse.core.types.ClassType;
import org.codehaus.groovy.eclipse.core.types.ISymbolTable;
import org.codehaus.groovy.eclipse.core.types.LocalVariable;
import org.codehaus.groovy.eclipse.core.types.GroovyDeclaration;

/**
 * A simple symbol table wrapping a map.
 * 
 * @author empovazan
 */
public class MapSymbolTable implements ISymbolTable {
	private final Map< String, GroovyDeclaration > map = newMap();

	public GroovyDeclaration lookup(String signature) {
		return map.get(signature);
	}

	/**
	 * Add a type as an instance.
	 * @param signature
	 * @param cls
	 */
	public void addType(String signature, Class cls) {
		addType(signature, cls.getName());
	}

	/**
	 * Add a type.
	 * @param name The name of the type or class.
	 * @param signature Encoded as described in {@link TypeInfo}.
	 */
	public void addType(String name, String signature) {
		map.put(name, new ClassType(createTypeSignature(signature, true), ACC_PUBLIC, name));
	}
	
	public void addVariable(String name, String signature) {
		map.put(name, new LocalVariable(signature, name));
	}
}