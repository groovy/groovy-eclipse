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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;

/**
 * @author Andrew Eisenberg
 * @created Oct 25, 2009
 * 
 *          Looks up the type of an expression in the currently applicable categories. Note that DefaultGroovyMethods are always
 *          considered to be an applicable category. This lookup is not being used yet
 */
public class CategoryTypeLookup implements ITypeLookup {

	/**
	 * Looks up method calls to see if they are declared in any current categories
	 */
	public TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType) {
		if (node instanceof ConstantExpression) {
			ConstantExpression constExpr = (ConstantExpression) node;
			Set<ClassNode> categories = scope.getCategoryNames();
			ClassNode currentType = objectExpressionType != null ? objectExpressionType : scope.getEnclosingTypeDeclaration();
			Set<MethodNode> possibleMethods = new HashSet<MethodNode>();
			// go through all categories and look for and look for a method with the given name
			for (ClassNode category : categories) {
				List<?> methods = category.getMethods(constExpr.getText()); // use List<?> because groovy 1.6.5 does not
				// have type parameters on this method
				possibleMethods.addAll((Collection<? extends MethodNode>) methods);
			}
			for (MethodNode methodNode : possibleMethods) {
				Parameter[] params = methodNode.getParameters();
				if (params != null && params.length > 0
						&& isAssignableFrom(VariableScope.maybeConvertFromPrimitive(currentType), params[0].getType())) {
					// found it! There may be more, but this is good enough
					return new TypeLookupResult(methodNode.getReturnType(), methodNode.getDeclaringClass(), methodNode,
							TypeConfidence.EXACT, scope);
				}
			}
		}
		return null;
	}

	/**
	 * can from be assigned to to?
	 * 
	 * FIXADE M2 ensure we don't visit interfaces more than once
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	private boolean isAssignableFrom(ClassNode from, ClassNode to) {
		if (from == null || to == null) {
			return false;
		} else if (from.equals(to)) {
			return true;
		} else if (from.getName().equals("java.lang.Object")) {
			return false;
		} else if (isAssignableFrom(from.getSuperClass(), to)) {
			return true;
		} else {
			if (to.isInterface()) {
				// checking super interfaces here may mean that there are duplicated checks
				for (ClassNode superInterface : from.getInterfaces()) {
					if (isAssignableFrom(superInterface, to)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	public TypeLookupResult lookupType(FieldNode node, VariableScope scope) {
		return null;
	}

	public TypeLookupResult lookupType(MethodNode node, VariableScope scope) {
		return null;
	}

	public TypeLookupResult lookupType(AnnotationNode node, VariableScope scope) {
		return null;
	}

	public TypeLookupResult lookupType(ImportNode node, VariableScope scope) {
		return null;
	}

	public TypeLookupResult lookupType(ClassNode node, VariableScope scope) {
		return null;
	}

	public TypeLookupResult lookupType(Parameter node, VariableScope scope) {
		return null;
	}

}
