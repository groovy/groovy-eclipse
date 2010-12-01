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
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;

/**
 * @author Andrew Eisenberg
 * @created Nov 20, 2009 A simplified type lookup that targets the general case where a provider wants to add initialization to a
 *          class and add new methods/fields to certain types of objects
 */
public abstract class AbstractSimplifiedTypeLookup implements ITypeLookup {

	public static class TypeAndDeclaration {
		public TypeAndDeclaration(ClassNode type, ASTNode declaration) {
			this.type = type;
			this.declaration = declaration;
			this.declaringType = null;
			this.extraDoc = null;
		}

		public TypeAndDeclaration(ClassNode type, ASTNode declaration, ClassNode declaringType) {
			this.type = type;
			this.declaration = declaration;
			this.declaringType = declaringType;
			this.extraDoc = null;
		}

		public TypeAndDeclaration(ClassNode type, ASTNode declaration, ClassNode declaringType, String extraDoc) {
			this.type = type;
			this.declaration = declaration;
			this.declaringType = declaringType;
			this.extraDoc = extraDoc;
		}

		protected final ClassNode type;
		protected final ClassNode declaringType;
		protected final ASTNode declaration;
		protected final String extraDoc;
	}

	public final TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType) {
		if (node instanceof ConstantExpression) {
			ClassNode declaringType = objectExpressionType != null ? objectExpressionType : scope.getEnclosingTypeDeclaration();
			TypeAndDeclaration tAndD = lookupTypeAndDeclaration(declaringType, ((ConstantExpression) node).getText(), scope);
			if (tAndD != null) {
				return new TypeLookupResult(tAndD.type, tAndD.declaringType == null ? declaringType : tAndD.declaringType,
						tAndD.declaration, TypeConfidence.LOOSELY_INFERRED, scope, tAndD.extraDoc);
			}
		} else if (node instanceof VariableExpression) {
			ClassNode declaringType = objectExpressionType != null ? objectExpressionType : scope.getEnclosingTypeDeclaration();
			TypeAndDeclaration tAndD = lookupTypeAndDeclaration(declaringType, ((VariableExpression) node).getName(), scope);
			if (tAndD != null) {
				return new TypeLookupResult(tAndD.type, tAndD.declaringType == null ? declaringType : tAndD.declaringType,
						tAndD.declaration, TypeConfidence.LOOSELY_INFERRED, scope, tAndD.extraDoc);
			}
		}
		return null;
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
