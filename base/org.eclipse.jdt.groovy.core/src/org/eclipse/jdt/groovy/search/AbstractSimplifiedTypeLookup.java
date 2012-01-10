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
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;

/**
 * @author Andrew Eisenberg
 * @created Nov 20, 2009 A simplified type lookup that targets the general case where a provider wants to add initialization to a
 *          class and add new methods/fields to certain types of objects
 */
public abstract class AbstractSimplifiedTypeLookup implements ITypeLookupExtension {

	public static class TypeAndDeclaration {
		public TypeAndDeclaration(ClassNode type, ASTNode declaration) {
			this.type = type;
			this.declaration = declaration;
			this.declaringType = null;
			this.extraDoc = null;
			this.confidence = null;
		}

		public TypeAndDeclaration(ClassNode type, ASTNode declaration, ClassNode declaringType) {
			this.type = type;
			this.declaration = declaration;
			this.declaringType = declaringType;
			this.extraDoc = null;
			this.confidence = null;
		}

		public TypeAndDeclaration(ClassNode type, ASTNode declaration, ClassNode declaringType, String extraDoc) {
			this.type = type;
			this.declaration = declaration;
			this.declaringType = declaringType;
			this.extraDoc = extraDoc;
			this.confidence = null;
		}

		public TypeAndDeclaration(ClassNode type, ASTNode declaration, ClassNode declaringType, String extraDoc,
				TypeConfidence confidence) {
			this.type = type;
			this.declaration = declaration;
			this.declaringType = declaringType;
			this.extraDoc = extraDoc;
			this.confidence = confidence;
		}

		protected final ClassNode type;
		protected final ClassNode declaringType;
		protected final ASTNode declaration;
		protected final String extraDoc;
		protected final TypeConfidence confidence;
	}

	private boolean isStatic;
	private Expression currentExpression;

	/**
	 * @return true iff the current lookup is in a static scope
	 */
	protected boolean isStatic() {
		return isStatic;
	}

	/**
	 * @return the expression AST node that is currently being inferred.
	 */
	protected Expression getCurrentExpression() {
		return currentExpression;
	}

	/**
	 * @return true iff the current expression being inferred is a quoted string
	 */
	protected boolean isQuotedString() {
		return currentExpression instanceof GStringExpression
				|| currentExpression.getText().length() != currentExpression.getLength();
	}

	// not called, but must be implemented
	public final TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType) {
		return lookupType(node, scope, objectExpressionType, false);
	}

	public final TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType,
			boolean isStaticObjectExpression) {
		ClassNode declaringType;
		if (objectExpressionType != null) {
			declaringType = objectExpressionType;
		} else {
			// Use delegate type if exists
			declaringType = scope.getDelegateOrThis();
			if (declaringType == null) {
				declaringType = scope.getEnclosingTypeDeclaration();
				if (declaringType == null) {
					// part of an import statment
					declaringType = VariableScope.OBJECT_CLASS_NODE;
				}
			}
		}
		// I would have likd to pass this value into lookupTypeAndDeclaration, but
		// I can't break api here
		isStatic = isStaticObjectExpression;
		currentExpression = node;

		TypeAndDeclaration tAndD = null;
		if (node instanceof ConstantExpression || node instanceof GStringExpression || node instanceof VariableExpression) {
			tAndD = lookupTypeAndDeclaration(declaringType, node.getText(), scope);
		}

		if (tAndD != null) {
			return new TypeLookupResult(tAndD.type, tAndD.declaringType == null ? declaringType : tAndD.declaringType,
					tAndD.declaration, tAndD.confidence == null ? confidence() : tAndD.confidence, scope, tAndD.extraDoc);
		}
		return null;
	}

	/**
	 * @return the confidence level of lookup results for this type lookup. Defaults to {@link TypeConfidence#LOOSELY_INFERRED}
	 */
	protected TypeConfidence confidence() {
		return TypeConfidence.LOOSELY_INFERRED;
	}

	public final TypeLookupResult lookupType(FieldNode node, VariableScope scope) {
		return null;
	}

	public final TypeLookupResult lookupType(MethodNode node, VariableScope scope) {
		return null;
	}

	public final TypeLookupResult lookupType(AnnotationNode node, VariableScope scope) {
		return null;
	}

	public final TypeLookupResult lookupType(ImportNode node, VariableScope scope) {
		return null;
	}

	public final TypeLookupResult lookupType(ClassNode node, VariableScope scope) {
		return null;
	}

	public final TypeLookupResult lookupType(Parameter node, VariableScope scope) {
		return null;
	}

	public void lookupInBlock(BlockStatement node, VariableScope scope) {
	}

	/**
	 * Clients should return a {@link TypeAndDeclaration} corresponding to an additional
	 * 
	 * @param declaringType
	 * @param name
	 * @param scope
	 * @return the type and declaration corresponding to the name in the given declaring type. The declaration may be null, but this
	 *         should be avoided in that it prevents the use of navigation and of javadoc hovers
	 */
	protected abstract TypeAndDeclaration lookupTypeAndDeclaration(ClassNode declaringType, String name, VariableScope scope);
}
