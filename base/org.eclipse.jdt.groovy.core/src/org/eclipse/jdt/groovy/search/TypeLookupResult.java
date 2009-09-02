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

package org.eclipse.jdt.groovy.search;

import org.codehaus.groovy.ast.ClassNode;
import org.eclipse.jdt.core.IType;

/**
 * @author Andrew Eisenberg
 * @created Aug 29, 2009
 * 
 */
public class TypeLookupResult {
	/**
	 * Specifies the kind of match found for this type
	 */
	public static enum TypeConfidence {
		/**
		 * Match is certain E.g., type is explicitly declared on a variable
		 */
		EXACT,
		/**
		 * Match is potential. E.g., it may be from an interface or from a concrete type, but not possible to tell which one from
		 * the context
		 */
		POTENTIAL,
		/**
		 * The type has been inferred from local or global context. E.g., by looking at assignment statements
		 */
		INFERRED,
		/**
		 * The type has been inferred using less precise means. E.g., by looking at the results of running JUnit tests
		 */
		LOOSELY_INFERRED
	}

	public final TypeConfidence confidence;
	public final IType type;
	public final ClassNode node;

	/**
	 * create a TypeLookupResult with a class node. Clients are expected to convert from class node to IType if required
	 */
	public TypeLookupResult(ClassNode node, TypeConfidence confidence) {
		this.confidence = confidence;
		this.type = null;
		this.node = node;
	}

	/**
	 * create a TypeLookupResult with an IType. Clients are expected to convert from IType to class node if required
	 */
	public TypeLookupResult(IType type, TypeConfidence confidence) {
		this.confidence = confidence;
		this.type = type;
		this.node = null;
	}

	public String getFullyQualifiedName() {
		if (type != null) {
			return type.getFullyQualifiedName();
		} else {
			return node.getName();
		}
	}
}
