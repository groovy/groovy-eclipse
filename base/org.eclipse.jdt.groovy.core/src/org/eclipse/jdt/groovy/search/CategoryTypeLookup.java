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

import java.util.ArrayList;
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
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
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
		if (node instanceof ConstantExpression || node instanceof VariableExpression) {
			Set<ClassNode> categories = scope.getCategoryNames();
			ClassNode currentType = objectExpressionType != null ? objectExpressionType : scope.getDelegateOrThis();
			List<MethodNode> possibleMethods = new ArrayList<MethodNode>();
			// go through all categories and look for and look for a method with the given name
			String text = node.getText();
			if (text.startsWith("${") && text.endsWith("}")) {
				text = text.substring(2, text.length() - 1);
			} else if (text.startsWith("$")) {
				text = text.substring(1);
			}
			String getterName = AccessorSupport.GETTER.createAccessorName(text);
			String setterName = AccessorSupport.SETTER.createAccessorName(text);
			for (ClassNode category : categories) {
				List<MethodNode> methods = category.getMethods(text);

				possibleMethods.addAll(methods);

				// also check to see if the getter variant of any name is available
				if (getterName != null) {
					methods = category.getMethods(getterName);
					for (MethodNode method : methods) {
						if (method.isStatic() && AccessorSupport.findAccessorKind(method, true) == AccessorSupport.GETTER) {
							possibleMethods.add(method);
						}
					}
				}
				if (setterName != null) {
					methods = category.getMethods(setterName);
					for (MethodNode method : methods) {
						if (method.isStatic() && AccessorSupport.findAccessorKind(method, true) == AccessorSupport.SETTER) {
							possibleMethods.add(method);
						}
					}
				}
			}
			for (MethodNode methodNode : possibleMethods) {
				Parameter[] params = methodNode.getParameters();
				if (params != null && params.length > 0
						&& isAssignableFrom(VariableScope.maybeConvertFromPrimitive(currentType), params[0].getType())) {
					ClassNode declaringClass = methodNode.getDeclaringClass();
					return new TypeLookupResult(methodNode.getReturnType(), declaringClass, methodNode,
							getConfidence(declaringClass), scope);
				}
			}
		}
		return null;
	}

	/**
	 * DGM and DGSM classes are loosely inferred so that other lookups can provide better solutions
	 * 
	 * @param declaringClass
	 * @return
	 */
	private TypeConfidence getConfidence(ClassNode declaringClass) {
		return VariableScope.ALL_DEFAULT_CATEGORIES.contains(declaringClass) ? TypeConfidence.LOOSELY_INFERRED
				: TypeConfidence.INFERRED;
	}

	/**
	 * @param interf
	 * @param allInterfaces
	 */
	private void findAllSupers(ClassNode clazz, Set<String> allSupers) {
		if (!allSupers.contains(clazz.getName())) {
			allSupers.add(clazz.getName());
			if (clazz.getSuperClass() != null) {
				findAllSupers(clazz.getSuperClass(), allSupers);
			}
			if (clazz.getInterfaces() != null) {
				for (ClassNode superInterface : clazz.getInterfaces()) {
					findAllSupers(superInterface, allSupers);
				}
			}
		}
	}

	/**
	 * can from be assigned to to?
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	private boolean isAssignableFrom(ClassNode from, ClassNode to) {
		if (from == null || to == null) {
			return false;
		}
		Set<String> allSupers = new HashSet<String>();
		allSupers.add("java.lang.Object");
		findAllSupers(from, allSupers);
		for (String supr : allSupers) {
			if (to.getName().equals(supr)) {
				return true;
			}
		}
		return false;
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

	public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
		// do nothing
	}

}
