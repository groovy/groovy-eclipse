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
package org.codehaus.groovy.eclipse.core.types.impl;

import static org.codehaus.groovy.eclipse.core.types.Modifiers.ACC_PUBLIC;
import static org.codehaus.groovy.eclipse.core.util.MapUtil.newMap;
import static org.eclipse.jdt.core.Signature.createTypeSignature;

import java.util.Map;

import org.codehaus.groovy.eclipse.core.types.ClassType;
import org.codehaus.groovy.eclipse.core.types.ISymbolTable;
import org.codehaus.groovy.eclipse.core.types.LocalVariable;
import org.codehaus.groovy.eclipse.core.types.Type;

/**
 * A simple symbol table wrapping a map.
 * 
 * @author empovazan
 */
public class MapSymbolTable implements ISymbolTable {
	private final Map< String, Type > map = newMap();

	public Type lookup(String signature) {
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