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

import static org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence.EXACT;
import static org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence.INFERRED;
import static org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence.UNKNOWN;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;

/**
 * @author Andrew Eisenberg
 * @created Aug 29, 2009
 * 
 *          Looks at the type associated with the ASTNode for the type
 */
public class SimpleTypeLookup implements ITypeLookup {

	public TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType) {

		TypeConfidence[] confidence = new TypeConfidence[] { EXACT };
		ClassNode declaringType = objectExpressionType != null ? objectExpressionType : findDeclaringType(node, scope, confidence);

		TypeLookupResult result = findType(node, objectExpressionType, declaringType, scope, confidence[0]);

		return result;
	}

	public TypeLookupResult lookupType(FieldNode node, VariableScope scope) {
		return new TypeLookupResult(node.getType(), node.getDeclaringClass(), node, EXACT, scope);
	}

	public TypeLookupResult lookupType(MethodNode node, VariableScope scope) {
		return new TypeLookupResult(node.getReturnType(), node.getDeclaringClass(), node, EXACT, scope);
	}

	public TypeLookupResult lookupType(AnnotationNode node, VariableScope scope) {
		ClassNode baseType = node.getClassNode();
		return new TypeLookupResult(baseType, baseType, baseType, EXACT, scope);
	}

	public TypeLookupResult lookupType(ImportNode node, VariableScope scope) {
		ClassNode baseType = node.getType();
		return new TypeLookupResult(baseType, baseType, baseType, EXACT, scope);
	}

	/**
	 * always return the passed in node
	 */
	public TypeLookupResult lookupType(ClassNode node, VariableScope scope) {
		return new TypeLookupResult(node, node, node, EXACT, scope);
	}

	public TypeLookupResult lookupType(Parameter node, VariableScope scope) {
		return new TypeLookupResult(node.getType(), scope.getEnclosingTypeDeclaration(), node /* should be methodnode? */, EXACT,
				scope);
	}

	/**
	 * @param node
	 * @param scope
	 * @return
	 */
	private ClassNode findDeclaringType(Expression node, VariableScope scope, TypeConfidence[] confidence) {
		if (node instanceof ClassExpression || node instanceof ConstructorCallExpression) {
			return node.getType();

		} else if (node instanceof FieldExpression) {
			return ((FieldExpression) node).getField().getDeclaringClass();

		} else if (node instanceof MethodCallExpression) {
			return ((MethodCallExpression) node).getObjectExpression().getType();

		} else if (node instanceof StaticMethodCallExpression) {
			return ((StaticMethodCallExpression) node).getOwnerType();

		} else if (node instanceof VariableExpression) {
			Variable var = ((VariableExpression) node).getAccessedVariable();
			if (var instanceof DynamicVariable) {
				// this dynamic variable might be a field
				ClassNode type = findField((VariableExpression) node, scope.getEnclosingTypeDeclaration());
				if (type == null) {
					// or it might refer to a method call with no parens
					type = findMethod((VariableExpression) node, scope.getEnclosingTypeDeclaration());
				}
				if (type == null) {
					// might be a property
					type = findProperty((VariableExpression) node, scope.getEnclosingTypeDeclaration());
				}
				confidence[0] = TypeConfidence.findLessPrecise(confidence[0], INFERRED);
				return type == null ? node.getType() : type;
			} else if (var instanceof FieldNode) {
				return ((FieldNode) var).getDeclaringClass();
			}

		} else if (node instanceof DeclarationExpression) {
			// the type declaration of the DeclarationExpression is considered to be the
			// declaring type. This ensures that type declarations are considered
			// to be type references.
			return ((DeclarationExpression) node).getLeftExpression().getType();
		}
		return VariableScope.OBJECT_CLASS_NODE;
	}

	/**
	 * @param node
	 * @param scope
	 * @return
	 */
	private TypeLookupResult findType(Expression node, ClassNode objectExpressionType, ClassNode declaringType,
			VariableScope scope, TypeConfidence confidence) {
		// check first to see if we have this type inferred
		if (node instanceof Variable) {
			ASTNode declaration = node;
			Variable var = (Variable) node;
			Variable accessedVar = null;
			if (node instanceof VariableExpression) {
				accessedVar = ((VariableExpression) node).getAccessedVariable();
				if (accessedVar != null && accessedVar instanceof AnnotatedNode) {
					declaration = (AnnotatedNode) accessedVar;
				}
			}

			VariableInfo info = scope.lookupName(var.getName());
			if (info != null) {
				if (accessedVar instanceof DynamicVariable) {
					// this is actually a reference to a field or method in a type
					// find this reference
					ASTNode maybeDeclaration = findDeclaration(accessedVar.getName(), info.declaringType);
					if (maybeDeclaration != null) {
						declaration = maybeDeclaration;
					}
				}
				confidence = TypeConfidence.findLessPrecise(confidence, INFERRED);
				return new TypeLookupResult(info.type, declaringType, declaration, confidence, scope);
			} else if (var instanceof VariableExpression) {
				if (accessedVar instanceof DynamicVariable) {
					confidence = UNKNOWN;
				}
				return new TypeLookupResult(accessedVar.getType(), declaringType, declaration, confidence, scope);
			}
		}

		if (objectExpressionType != null) {
			// lookup the type in the object's expression type
			if (node instanceof ConstantExpression) {
				ConstantExpression constExpr = (ConstantExpression) node;

				String name = constExpr.getText();
				PropertyNode property = objectExpressionType.getProperty(name);
				if (property != null) {
					return new TypeLookupResult(property.getType(), property.getDeclaringClass(), property, confidence, scope);
				}
				// do not distinguish between method variants
				List<MethodNode> methods = objectExpressionType.getMethods(name);
				if (methods.size() > 0) {
					MethodNode methodNode = methods.get(0);
					return new TypeLookupResult(methodNode.getReturnType(), methodNode.getDeclaringClass(), methodNode, confidence,
							scope);
				}
				if (objectExpressionType.isInterface()) {
					// super interface methods on an interface are not returned by getMethods(), so must explicitly look for them
					MethodNode interfaceMethod = findMethodInInterface(objectExpressionType, name);
					if (interfaceMethod != null) {
						return new TypeLookupResult(interfaceMethod.getReturnType(), interfaceMethod.getDeclaringClass(),
								interfaceMethod, confidence, scope);
					}

					// do the same for properties
					PropertyNode interfaceProperty = findPropertyInInterface(objectExpressionType, name);
					if (interfaceProperty != null) {
						return new TypeLookupResult(interfaceProperty.getType(), interfaceProperty.getDeclaringClass(),
								interfaceProperty, confidence, scope);
					}
				}

				FieldNode field = objectExpressionType.getField(name);
				if (field != null) {
					return new TypeLookupResult(field.getType(), field.getDeclaringClass(), field, confidence, scope);
				}
				confidence = UNKNOWN;
				return new TypeLookupResult(node.getType(), declaringType, null, confidence, scope);
			} else if (node instanceof BinaryExpression && ((BinaryExpression) node).getOperation().getType() == Types.EQUALS) {
				return new TypeLookupResult(objectExpressionType, declaringType, null, confidence, scope);
			}
		}

		// no object expression, look at the kind of expression
		if (node instanceof ConstantExpression) {
			// here, we know that since there is no object expression, this is not part
			// of a dotted anything, so we can safely assume that it is a quoted string or
			// some other constant
			ConstantExpression constExpr = (ConstantExpression) node;

			if (constExpr.isTrueExpression() || constExpr.isFalseExpression()) {
				return new TypeLookupResult(VariableScope.BOOLEAN_CLASS_NODE, null, null, confidence, scope);
			} else if (constExpr.isNullExpression()) {
				return new TypeLookupResult(VariableScope.VOID_CLASS_NODE, null, null, confidence, scope);
			} else if (constExpr.isEmptyStringExpression()) {
				return new TypeLookupResult(VariableScope.STRING_CLASS_NODE, null, null, confidence, scope);
			} else {
				return new TypeLookupResult(node.getType(), null, null, confidence, scope);
			}

		} else if (node instanceof ArgumentListExpression || node instanceof ListExpression) {
			return new TypeLookupResult(VariableScope.LIST_CLASS_NODE, null, null, confidence, scope);

		} else if (node instanceof BinaryExpression) {
			// Object expression was null, so go for the left expression.
			return new TypeLookupResult(((BinaryExpression) node).getLeftExpression().getType(), null, null, confidence, scope);

		} else if (node instanceof BooleanExpression || node instanceof NotExpression) {
			return new TypeLookupResult(VariableScope.BOOLEAN_CLASS_NODE, null, null, confidence, scope);

		} else if (node instanceof GStringExpression) {
			return new TypeLookupResult(VariableScope.GSTRING_CLASS_NODE, null, null, confidence, scope);

		} else if (node instanceof MapExpression) {
			return new TypeLookupResult(VariableScope.MAP_CLASS_NODE, null, null, confidence, scope);

		} else if (node instanceof PostfixExpression || node instanceof PrefixExpression) {
			return new TypeLookupResult(VariableScope.NUMBER_CLASS_NODE, null, null, confidence, scope);

		} else if (node instanceof BitwiseNegationExpression) {
			ClassNode type = ((BitwiseNegationExpression) node).getExpression().getType();
			if (type.getName().equals(VariableScope.STRING_CLASS_NODE.getName())) {
				return new TypeLookupResult(VariableScope.PATTERN_CLASS_NODE, null, null, confidence, scope);
			} else {
				return new TypeLookupResult(type, null, null, confidence, scope);
			}
		} else if (node instanceof ClassExpression) {
			return new TypeLookupResult(node.getType(), declaringType, node.getType(), confidence, scope);
		} else if (node instanceof StaticMethodCallExpression) {
			StaticMethodCallExpression expr = (StaticMethodCallExpression) node;
			List<MethodNode> methods = expr.getOwnerType().getMethods(expr.getMethod());
			if (methods.size() > 0) {
				MethodNode method = methods.get(0);
				return new TypeLookupResult(method.getReturnType(), declaringType, method, confidence, scope);
			}
		}

		if (!(node instanceof MethodCallExpression) && !(node instanceof ConstructorCallExpression)
				&& !(node instanceof MapEntryExpression) && !(node instanceof PropertyExpression)
				&& node.getType().equals(VariableScope.OBJECT_CLASS_NODE)) {
			confidence = UNKNOWN;
		}

		// don't know
		return new TypeLookupResult(node.getType(), declaringType, null, confidence, scope);
	}

	/**
	 * @param objectExpressionType
	 * @param name
	 * @return
	 */
	private MethodNode findMethodInInterface(ClassNode objectExpressionType, String name) {
		Set<ClassNode> allInterfaces = new HashSet<ClassNode>();
		findAllInterfaces(objectExpressionType, allInterfaces);
		for (ClassNode interf : allInterfaces) {
			List<MethodNode> methods = interf.getDeclaredMethods(name);
			if (methods != null && methods.size() > 0) {
				return methods.get(0);
			}
		}
		return null;
	}

	/**
	 * @param objectExpressionType
	 * @param name
	 * @return
	 */
	private PropertyNode findPropertyInInterface(ClassNode objectExpressionType, String name) {
		Set<ClassNode> allInterfaces = new HashSet<ClassNode>();
		findAllInterfaces(objectExpressionType, allInterfaces);
		for (ClassNode interf : allInterfaces) {
			PropertyNode prop = interf.getProperty(name);
			if (prop != null) {
				return prop;
			}
		}
		return null;
	}

	/**
	 * @param interf
	 * @param allInterfaces
	 */
	private void findAllInterfaces(ClassNode interf, Set<ClassNode> allInterfaces) {
		allInterfaces.add(interf);
		if (!allInterfaces.contains(interf) && interf.getInterfaces() != null) {
			for (ClassNode superInterface : interf.getInterfaces()) {
				findAllInterfaces(superInterface, allInterfaces);
			}
		}
	}

	/**
	 * @param name
	 * @param declaringType
	 * @return
	 */
	private ASTNode findDeclaration(String name, ClassNode declaringType) {
		AnnotatedNode maybe = declaringType.getProperty(name);
		if (maybe != null) {
			return maybe;
		}
		maybe = declaringType.getField(name);
		if (maybe != null) {
			return maybe;
		}
		List<MethodNode> maybeMethods = declaringType.getMethods(name);
		if (maybeMethods != null && maybeMethods.size() > 0) {
			return maybeMethods.get(0);
		}
		return null;
	}

	/**
	 * @param node
	 * @param enclosingTypeDeclaration
	 * @return
	 */
	private ClassNode findProperty(VariableExpression var, ClassNode declaringClass) {
		if (declaringClass == null) {
			return var.getType();
		}
		String name = var.getName();
		PropertyNode property = declaringClass.getProperty(name);
		if (property != null) {
			return property.getDeclaringClass();
		} else {
			return null;
		}
	}

	/**
	 * @param name
	 * @param enclosingDeclarationNode
	 * @return
	 */
	private ClassNode findField(VariableExpression var, ClassNode declaringClass) {
		if (declaringClass == null) {
			return var.getType();
		}
		String name = var.getName();
		FieldNode field = declaringClass.getField(name);
		if (field != null) {
			return field.getDeclaringClass();
		} else {
			return null;
		}
	}

	/**
	 * @param name
	 * @param enclosingDeclarationNode
	 * @return
	 */
	private ClassNode findMethod(VariableExpression var, ClassNode declaringClass) {
		if (declaringClass == null) {
			return var.getType();
		}
		String name = var.getName();
		List<MethodNode> methods = declaringClass.getMethods(name);
		if (methods.size() > 0) {
			return methods.get(0).getDeclaringClass();
		} else {
			return null;
		}
	}

	public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
		// do nothing
	}

}
