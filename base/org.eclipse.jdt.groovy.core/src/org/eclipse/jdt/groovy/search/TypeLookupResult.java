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
		EXACT(0),
		/**
		 * Match is potential. E.g., it may be from an interface or from a concrete type, but not possible to tell which one from
		 * the context
		 */
		POTENTIAL(1),
		/**
		 * The type has been inferred from local or global context. E.g., by looking at assignment statements
		 */
		INFERRED(2),
		/**
		 * The type has been inferred using less precise means. E.g., by looking at the results of running JUnit tests
		 */
		LOOSELY_INFERRED(3),
		/**
		 * This is an unknown reference
		 */
		UNKNOWN(4);

		private final int val;

		TypeConfidence(int val) {
			this.val = val;
		}

		static TypeConfidence findLessPrecise(TypeConfidence left, TypeConfidence right) {
			return left.val > right.val ? left : right;
		}
	}

	public final TypeConfidence confidence;
	public final ClassNode type;
	public final ClassNode declaringType;

	/**
	 * create a TypeLookupResult with a class node.
	 * 
	 * @param type the type of the expression being analyzed
	 * @param declaringType the declaring type of the expression if the expression is a field, method, or type reference
	 * @param confidence the confidence in this type assertion
	 */
	public TypeLookupResult(ClassNode type, ClassNode declaringType, TypeConfidence confidence) {
		this.confidence = confidence;
		this.type = type;
		this.declaringType = declaringType;
	}
}
