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
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;

/**
 * @author Andrew Eisenberg
 * @created Aug 29, 2009
 * 
 *          Looks at the type associated with the ASTNode for the type <br>
 */
public class SimpleTypeLookup implements ITypeLookup {

	private GroovyCompilationUnit unit;

	public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
		this.unit = unit;
	}

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
				// search type hierarchy for declaration
				ASTNode declaration = findDeclaration(var.getName(), scope.getEnclosingTypeDeclaration());
				ClassNode type = declaringTypeFromDeclaration(declaration);
				type = type == null ? var.getType() : type;
				confidence[0] = TypeConfidence.findLessPrecise(confidence[0], INFERRED);
				return type;
			} else if (var instanceof FieldNode) {
				return ((FieldNode) var).getDeclaringClass();
			} else {
				// local variable, no declaring type
				// fall through
			}

		} else if (node instanceof DeclarationExpression) {
			// the type declaration of the DeclarationExpression is considered to be the
			// declaring type. This ensures that type declarations are treated as
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
		if (node instanceof VariableExpression) {
			return findTypeForVariable((VariableExpression) node, scope, confidence, declaringType);
		}

		// if the object type is not null, then we base the
		// type of this node on the object type
		if (objectExpressionType != null) {
			// lookup the type bsed on the object's expression type
			// assume it is a method/property/field in the object expression type's hierarchy

			if (node instanceof ConstantExpression) {
				return findTypeForNameWithKnownObjectExpression(((ConstantExpression) node).getText(), node.getType(),
						objectExpressionType, scope, confidence);

			} else if (node instanceof BinaryExpression && ((BinaryExpression) node).getOperation().getType() == Types.EQUALS) {
				// this is an assignment expression, return the object expression, which is the right hand side
				return new TypeLookupResult(objectExpressionType, declaringType, null, confidence, scope);
			} else if (node instanceof TernaryExpression) {
				// return the object expression type
				return new TypeLookupResult(objectExpressionType, declaringType, null, confidence, scope);
			}
		}

		// no object expression, look at the kind of expression
		// the following expression kinds have a type that is constant
		// no matter what their contents are.
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

		} else if (node instanceof ArgumentListExpression || node instanceof ListExpression || node instanceof RangeExpression) {
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
			// FIXADE RC1 hmmmm...because of operator overloading, we should be looking at the type
			// of the inner expression, but Number will be safe for most of the time.
			return new TypeLookupResult(VariableScope.NUMBER_CLASS_NODE, null, null, confidence, scope);

		} else if (node instanceof BitwiseNegationExpression) {
			ClassNode type = ((BitwiseNegationExpression) node).getExpression().getType();
			if (type.getName().equals(VariableScope.STRING_CLASS_NODE.getName())) {
				return new TypeLookupResult(VariableScope.PATTERN_CLASS_NODE, null, null, confidence, scope);
			} else {
				return new TypeLookupResult(type, null, null, confidence, scope);
			}
		} else if (node instanceof ClassExpression) {
			// check for special case...a bit crude...determine if the actual reference is to Foo.class or to Foo
			if (nodeIsDotClassReference(node)) {
				return new TypeLookupResult(VariableScope.CLASS_CLASS_NODE, VariableScope.CLASS_CLASS_NODE,
						VariableScope.CLASS_CLASS_NODE, TypeConfidence.EXACT, scope);
			} else {
				return new TypeLookupResult(node.getType(), declaringType, node.getType(), confidence, scope);
			}
		} else if (node instanceof StaticMethodCallExpression) {
			StaticMethodCallExpression expr = (StaticMethodCallExpression) node;
			List<MethodNode> methods = expr.getOwnerType().getMethods(expr.getMethod());
			if (methods.size() > 0) {
				MethodNode method = methods.get(0);
				return new TypeLookupResult(method.getReturnType(), declaringType, method, confidence, scope);
			}
		}

		// if we get here, then we can't infer the type. Set to unknown if required.
		if (!(node instanceof MethodCallExpression) && !(node instanceof ConstructorCallExpression)
				&& !(node instanceof MapEntryExpression) && !(node instanceof PropertyExpression)
				&& !(node instanceof TupleExpression) && node.getType().equals(VariableScope.OBJECT_CLASS_NODE)) {
			confidence = UNKNOWN;
		}

		// don't know
		return new TypeLookupResult(node.getType(), declaringType, null, confidence, scope);
	}

	/**
	 * a little crude because will not find if there are spaces between '.' and 'class'
	 * 
	 * @param node
	 * @return
	 */
	private boolean nodeIsDotClassReference(Expression node) {
		int end = node.getEnd();
		int start = node.getStart();
		char[] contents = unit.getContents();
		if (contents.length >= end) {
			char[] realText = new char[end - start];
			System.arraycopy(contents, start, realText, 0, end - start);
			String realTextStr = String.valueOf(realText).trim();
			return realTextStr.endsWith(".class") || realTextStr.endsWith(".class."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return false;
	}

	/**
	 * look for a name within an object expression. It is either in the hierarchy, it is in the variable scope, or it is unknown.
	 * 
	 * @return
	 */
	private TypeLookupResult findTypeForNameWithKnownObjectExpression(String name, ClassNode type, ClassNode declaringType,
			VariableScope scope, TypeConfidence confidence) {
		ClassNode realDeclaringType = declaringType;
		VariableInfo varInfo;
		ASTNode declaration = findDeclaration(name, declaringType);
		if (declaration != null) {
			type = typeFromDeclaration(declaration);
			realDeclaringType = declaringTypeFromDeclaration(declaration);
		} else if (declaringType.equals(scope.getEnclosingTypeDeclaration()) && (varInfo = scope.lookupName(name)) != null) {
			type = varInfo.type;
			realDeclaringType = varInfo.declaringType;
			declaration = varInfo.declaringType;
		} else {
			confidence = UNKNOWN;
		}
		return new TypeLookupResult(type, realDeclaringType, declaration, confidence, scope);
	}

	private TypeLookupResult findTypeForVariable(VariableExpression var, VariableScope scope, TypeConfidence confidence,
			ClassNode declaringType) {
		ASTNode declaration = var;
		Variable accessedVar = var.getAccessedVariable();
		if (accessedVar instanceof ASTNode) {
			// not a DynamicVariable
			declaration = (ASTNode) accessedVar;
		}

		VariableInfo info = scope.lookupName(var.getName());
		TypeConfidence origConfidence = confidence;
		if (accessedVar instanceof DynamicVariable) {
			// this is likely a reference to a field or method in a type in the hierarchy
			// find the declaration
			ASTNode maybeDeclaration = findDeclaration(accessedVar.getName(), info != null ? info.declaringType : declaringType);
			if (maybeDeclaration != null) {
				declaration = maybeDeclaration;
				// declaring type may have changed
				declaringType = declaringTypeFromDeclaration(declaration);
			} else {
				confidence = UNKNOWN;
			}
		}

		ClassNode type;
		if (info != null) {
			confidence = TypeConfidence.findLessPrecise(origConfidence, INFERRED);
			type = info.type;
			declaringType = info.declaringType;
			if (scope.isThisOrSuper(var)) {
				declaration = type;
			}
		} else {

			// we have a variable expression, but it is not
			// declared anywhere in the scope. It is probably a DynamicVariable
			if (accessedVar instanceof DynamicVariable) {
				type = typeFromDeclaration(declaration);
			} else {
				type = var.getType();
			}
		}
		return new TypeLookupResult(type, declaringType, declaration, confidence, scope);
	}

	/**
	 * @param declaration
	 * @return
	 */
	private ClassNode declaringTypeFromDeclaration(ASTNode declaration) {
		ClassNode type;
		if (declaration instanceof FieldNode) {
			type = ((FieldNode) declaration).getDeclaringClass();
		} else if (declaration instanceof MethodNode) {
			type = ((MethodNode) declaration).getDeclaringClass();
		} else if (declaration instanceof PropertyNode) {
			type = ((PropertyNode) declaration).getDeclaringClass();
		} else if (declaration instanceof Expression) {
			// probably object
			type = ((Expression) declaration).getDeclaringClass();
		} else {
			type = VariableScope.OBJECT_CLASS_NODE;
		}
		return type;
	}

	/**
	 * @param declaration
	 * @return
	 */
	private ClassNode typeFromDeclaration(ASTNode declaration) {
		ClassNode type;
		if (declaration instanceof FieldNode) {
			type = ((FieldNode) declaration).getType();
		} else if (declaration instanceof MethodNode) {
			type = ((MethodNode) declaration).getReturnType();
		} else if (declaration instanceof PropertyNode) {
			type = ((PropertyNode) declaration).getType();
		} else if (declaration instanceof Expression) {
			type = ((Expression) declaration).getType();
		} else {
			type = VariableScope.OBJECT_CLASS_NODE;
		}
		return type;
	}

	/**
	 * 
	 * FIXADE RC1 Will this find static fields on interfaces if the interface type is a super interface of declaringType?
	 * 
	 * @param name
	 * @param declaringType
	 * @return
	 */
	private ASTNode findDeclaration(String name, ClassNode declaringType) {
		AnnotatedNode maybe = findPropertyInClass(declaringType, name);
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
		if (declaringType.isInterface()) {
			// super interface methods on an interface are not returned by getMethods(), so must explicitly look for them
			MethodNode interfaceMethod = findMethodInInterface(declaringType, name);
			if (interfaceMethod != null) {
				return interfaceMethod;
			}

			// do the same for properties
			PropertyNode interfaceProperty = findPropertyInInterface(declaringType, name);
			if (interfaceProperty != null) {
				return interfaceProperty;
			}

			// maybe do the same for fields to find constants in super interfaces
		}

		return null;
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
	private PropertyNode findPropertyInClass(ClassNode objectExpressionType, String name) {
		Set<ClassNode> allClasses = new HashSet<ClassNode>();
		findAllClasses(objectExpressionType, allClasses);
		for (ClassNode interf : allClasses) {
			PropertyNode prop = interf.getProperty(name);
			if (prop != null) {
				return prop;
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
		if (!allInterfaces.contains(interf) && interf.getInterfaces() != null) {
			allInterfaces.add(interf);
			for (ClassNode superInterface : interf.getInterfaces()) {
				findAllInterfaces(superInterface, allInterfaces);
			}
		}
	}

	private void findAllClasses(ClassNode clazz, Set<ClassNode> allClasses) {
		if (!allClasses.contains(clazz)) {
			allClasses.add(clazz);
			if (clazz.getSuperClass() != null) {
				findAllClasses(clazz.getSuperClass(), allClasses);
			}
			if (clazz.getInterfaces() != null) {
				for (ClassNode superInterface : clazz.getInterfaces()) {
					findAllInterfaces(superInterface, allClasses);
				}
			}
		}
	}
}