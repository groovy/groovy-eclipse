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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;

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
		 * The type has been inferred using less precise means. E.g., from an extending ITypeLookup All
		 * AbstractSimplifiedTypeLookups return this type confidence.
		 * <p>
		 * Furthermore, a type confidence of this will not cause the Inferencing engine to end its lookup. It will continue and try
		 * to find a more confident type using other lookups.
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

		boolean isLessPreciseThan(TypeConfidence other) {
			return this.val > other.val;
		}
	}

	public final TypeConfidence confidence;
	public final ClassNode type;
	public final ClassNode declaringType;
	/**
	 * the type should be AnnotatedNode, but in Groovy 1.6.5, this type is not compatible with expression nodes
	 */
	public final/* AnnotatedNode */ASTNode declaration;
	public final VariableScope scope;

	/**
	 * Extra Javadoc that should appear in hovers
	 */
	public final String extraDoc;

	/**
	 * The assignment statement that encloses this expression, or null if there is none
	 */
	BinaryExpression enclosingAssignment;

	/**
	 * create a TypeLookupResult with a class node.
	 * 
	 * @param type the type of the expression being analyzed
	 * @param declaringType the declaring type of the expression if the expression is a field, method, or type reference
	 * @param confidence the confidence in this type assertion
	 * @param the declaration that this node refers to, or null if none (ie- the method, field, class, or property node)
	 * @param scope the variable scope at this location
	 */
	public TypeLookupResult(ClassNode type, ClassNode declaringType, ASTNode declaration, TypeConfidence confidence,
			VariableScope scope) {
		this(type, declaringType, declaration, confidence, scope, null);
	}

	/**
	 * create a TypeLookupResult with a class node.
	 * 
	 * @param type the type of the expression being analyzed
	 * @param declaringType the declaring type of the expression if the expression is a field, method, or type reference
	 * @param confidence the confidence in this type assertion
	 * @param the declaration that this node refers to, or null if none (ie- the method, field, class, or property node)
	 * @param scope the variable scope at this location
	 * @param extraDoc extra javadoc to be shown in hovers
	 */
	public TypeLookupResult(ClassNode type, ClassNode declaringType, ASTNode declaration, TypeConfidence confidence,
			VariableScope scope, String extraDoc) {
		this.confidence = confidence;
		this.type = ClassHelper.isPrimitiveType(type) ? ClassHelper.getWrapper(type) : type;
		this.declaringType = declaringType;
		this.declaration = declaration;
		this.scope = scope;
		this.extraDoc = extraDoc;
	}

	public BinaryExpression getEnclosingAssignment() {
		return enclosingAssignment;
	}
}
