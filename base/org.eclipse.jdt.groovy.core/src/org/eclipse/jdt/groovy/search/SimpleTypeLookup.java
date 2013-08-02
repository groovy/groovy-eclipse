/*******************************************************************************
 * Copyright (c) 2009-2011 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     SpringSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.groovy.search;

import static org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence.EXACT;
import static org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence.INFERRED;
import static org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence.UNKNOWN;
import static org.eclipse.jdt.groovy.search.VariableScope.NO_GENERICS;
import groovyjarjarasm.asm.Opcodes;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;

/**
 * @author Andrew Eisenberg
 * @created Aug 29, 2009
 * 
 *          Looks at the type associated with the ASTNode for the type <br>
 */
public class SimpleTypeLookup implements ITypeLookupExtension {

	private GroovyCompilationUnit unit;

	public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
		this.unit = unit;
	}

	public TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType) {
		return lookupType(node, scope, objectExpressionType, false);
	}

	public TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType,
			boolean isStaticObjectExpression) {
		TypeConfidence[] confidence = new TypeConfidence[] { EXACT };
		if (ClassHelper.isPrimitiveType(objectExpressionType)) {
			objectExpressionType = ClassHelper.getWrapper(objectExpressionType);
		}
		ClassNode declaringType = objectExpressionType != null ? objectExpressionType : findDeclaringType(node, scope, confidence);
		TypeLookupResult result = findType(node, declaringType, scope, confidence[0], isStaticObjectExpression
				|| (objectExpressionType == null && scope.isStatic()), objectExpressionType == null);

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
		if (baseType != null) {
			return new TypeLookupResult(baseType, baseType, baseType, EXACT, scope);
		} else {
			// this is a * import
			return new TypeLookupResult(VariableScope.OBJECT_CLASS_NODE, VariableScope.OBJECT_CLASS_NODE,
					VariableScope.OBJECT_CLASS_NODE, INFERRED, scope);
		}
	}

	/**
	 * always return the passed in node, unless the declaration of an InnerClassNode
	 */
	public TypeLookupResult lookupType(ClassNode node, VariableScope scope) {
		ClassNode resultType;
		if (node instanceof InnerClassNode && !node.isRedirectNode()) {
			resultType = node.getSuperClass();
			if (resultType.getName().equals(VariableScope.OBJECT_CLASS_NODE.getName()) && node.getInterfaces().length > 0) {
				resultType = node.getInterfaces()[0];
			}
		} else {
			resultType = node;
		}
		return new TypeLookupResult(resultType, resultType, node, EXACT, scope);
	}

	public TypeLookupResult lookupType(Parameter node, VariableScope scope) {
		// look up the type in the current scope to see if the type has
		// has been predetermined (eg- for loop variables)
		VariableInfo info = scope.lookupNameInCurrentScope(node.getName());
		ClassNode type;
		if (info != null) {
			type = info.type;
		} else {
			type = node.getType();
		}
		return new TypeLookupResult(type, scope.getEnclosingTypeDeclaration(), node /* should be methodnode? */, EXACT, scope);
	}

	public void lookupInBlock(BlockStatement node, VariableScope scope) {
	}

	private ClassNode findDeclaringType(Expression node, VariableScope scope, TypeConfidence[] confidence) {
		if (node instanceof ClassExpression || node instanceof ConstructorCallExpression) {
			return node.getType();

		} else if (node instanceof FieldExpression) {
			return ((FieldExpression) node).getField().getDeclaringClass();

		} else if (node instanceof StaticMethodCallExpression) {
			return ((StaticMethodCallExpression) node).getOwnerType();

		} else if (node instanceof ConstantExpression) {
			if (scope.isMethodCall()) {
				// a method call with an implicit this
				return scope.getDelegateOrThis();
			}
		} else if (node instanceof VariableExpression) {
			Variable var = ((VariableExpression) node).getAccessedVariable();
			if (var instanceof DynamicVariable) {
				// search type hierarchy for declaration
				// first look in delegate and hierarchy and then go for this
				ASTNode declaration = null;

				ClassNode delegate = scope.getDelegate();
				if (delegate != null) {
					declaration = findDeclaration(var.getName(), delegate, scope.getMethodCallNumberOfArguments());
				}

				ClassNode thiz = scope.getThis();
				if (thiz == null) {
					thiz = VariableScope.OBJECT_CLASS_NODE;
				}
				if (declaration == null) {
					if (thiz != null && (delegate == null || !thiz.equals(delegate))) {
						// don't go here if this and delegate are the same
						declaration = findDeclaration(var.getName(), thiz, scope.getMethodCallNumberOfArguments());
					}
				}

				ClassNode type;
				if (declaration == null) {
					// this is a dynamic variable that doesn't seem to have a declaration
					// it might be an unknown and a mistake, but it could also be declared by 'this'
					type = thiz;
				} else {
					type = declaringTypeFromDeclaration(declaration, var.getType());
				}
				confidence[0] = TypeConfidence.findLessPrecise(confidence[0], INFERRED);
				return type;
			} else if (var instanceof FieldNode) {
				return ((FieldNode) var).getDeclaringClass();
			} else if (var instanceof PropertyNode) {
				return ((PropertyNode) var).getDeclaringClass();
			} else if (scope.isThisOrSuper((VariableExpression) node)) { // use 'node' because 'var' may be null
				// this or super expression, but it is not bound,
				// probably because concrete ast was requested
				return scope.lookupName(((VariableExpression) node).getName()).declaringType;
			} else {
				// local variable, no declaring type
				// fall through
			}
		}
		return VariableScope.OBJECT_CLASS_NODE;
	}

	private TypeLookupResult findType(Expression node, ClassNode declaringType, VariableScope scope, TypeConfidence confidence,
			boolean isStaticObjectExpression, boolean isPrimaryExpression) {

		// check first to see if we have this type inferred
		if (node instanceof VariableExpression) {
			return findTypeForVariable((VariableExpression) node, scope, confidence, declaringType);
		}

		// if the object type is not null, then we base the
		// type of this node on the object type
		ClassNode nodeType = node.getType();
		if (!isPrimaryExpression || scope.isMethodCall()) {
			// lookup the type based on the object's expression type
			// assume it is a method/property/field in the object expression type's hierarchy

			if (node instanceof ConstantExpression) {
				return findTypeForNameWithKnownObjectExpression(node.getText(), nodeType, declaringType, scope, confidence,
						isStaticObjectExpression, isPrimaryExpression);
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
			} else if (ClassHelper.isNumberType(nodeType) || nodeType == ClassHelper.BigDecimal_TYPE
					|| nodeType == ClassHelper.BigInteger_TYPE) {
				return new TypeLookupResult(nodeType, null, null, confidence, scope);
			} else if (nodeType.equals(VariableScope.STRING_CLASS_NODE)) {
				// likely a proper quoted string constant
				return new TypeLookupResult(nodeType, null, node, confidence, scope);
			} else {
				return new TypeLookupResult(nodeType, null, null, UNKNOWN, scope);
			}

		} else if (node instanceof BooleanExpression || node instanceof NotExpression) {
			return new TypeLookupResult(VariableScope.BOOLEAN_CLASS_NODE, null, null, confidence, scope);

		} else if (node instanceof GStringExpression) {
			// note that we return String type here, not GString so that DGMs will apply
			return new TypeLookupResult(VariableScope.STRING_CLASS_NODE, null, null, confidence, scope);

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
				return new TypeLookupResult(nodeType, declaringType, nodeType, confidence, scope);
			}
		} else if (node instanceof StaticMethodCallExpression) {
			StaticMethodCallExpression expr = (StaticMethodCallExpression) node;
			List<MethodNode> methods = expr.getOwnerType().getMethods(expr.getMethod());
			if (methods.size() > 0) {
				MethodNode method = methods.get(0);
				return new TypeLookupResult(method.getReturnType(), method.getDeclaringClass(), method, confidence, scope);
			}
		} else if (node instanceof ConstructorCallExpression) {
			List<ConstructorNode> declaredConstructors = declaringType.getDeclaredConstructors();
			if (declaredConstructors != null && declaredConstructors.size() > 0) {
				// FIXADE we can do better here and at least match on number of arguments
				return new TypeLookupResult(nodeType, declaringType, declaredConstructors.get(0), confidence, scope);
			}
			return new TypeLookupResult(nodeType, declaringType, declaringType, confidence, scope);
		}

		// if we get here, then we can't infer the type. Set to unknown if required.
		if (!(node instanceof TupleExpression) && nodeType.equals(VariableScope.OBJECT_CLASS_NODE)) {
			confidence = UNKNOWN;
		}

		// don't know
		return new TypeLookupResult(nodeType, declaringType, null, confidence, scope);
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
		if (unit.exists()) { // will return false if unit was moved during inferencing operation
			char[] contents = unit.getContents();
			if (contents.length >= end) {
				char[] realText = new char[end - start];
				System.arraycopy(contents, start, realText, 0, end - start);
				String realTextStr = String.valueOf(realText).trim();
				return realTextStr.endsWith(".class") || realTextStr.endsWith(".class."); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return false;
	}

	/**
	 * look for a name within an object expression. It is either in the hierarchy, it is in the variable scope, or it is unknown.
	 * 
	 * @param isPrimaryExpression
	 * 
	 * @return
	 */
	private TypeLookupResult findTypeForNameWithKnownObjectExpression(String name, ClassNode type, ClassNode declaringType,
			VariableScope scope, TypeConfidence confidence, boolean isStaticObjectExpression, boolean isPrimaryExpression) {
		ClassNode realDeclaringType;
		VariableInfo varInfo;
		TypeConfidence origConfidence = confidence;
		ASTNode declaration = findDeclaration(name, declaringType, scope.getMethodCallNumberOfArguments());

		if (declaration == null && isPrimaryExpression) {
			ClassNode thiz = scope.getThis();
			if (thiz != null && !thiz.equals(declaringType)) {
				// probably in a closure where the delegate has changed
				declaration = findDeclaration(name, thiz, scope.getMethodCallNumberOfArguments());
			}
		}

		// GRECLIPSE-1079
		if (declaration == null && isStaticObjectExpression) {
			// we might have a reference to a property/method defined on java.lang.Class
			declaration = findDeclaration(name, VariableScope.CLASS_CLASS_NODE, scope.getMethodCallNumberOfArguments());
		}

		if (declaration != null) {
			type = typeFromDeclaration(declaration, declaringType);
			realDeclaringType = declaringTypeFromDeclaration(declaration, declaringType);
		} else if (isPrimaryExpression &&
		// make everything from the scopes available
				(varInfo = scope.lookupName(name)) != null) {

			// now try to find the declaration again
			type = varInfo.type;
			realDeclaringType = varInfo.declaringType;
			declaration = findDeclaration(name, realDeclaringType, scope.getMethodCallNumberOfArguments());
			if (declaration == null) {
				declaration = varInfo.declaringType;
			}
		} else if (name.equals("call")) {
			// assume that this is a synthetic call method for calling a closure
			realDeclaringType = VariableScope.CLOSURE_CLASS;
			declaration = realDeclaringType.getMethods("call").get(0);
		} else {
			realDeclaringType = declaringType;
			confidence = UNKNOWN;
		}

		// now check to see if the object expression is static, but the declaration is not
		if (declaration != null && !realDeclaringType.equals(VariableScope.CLASS_CLASS_NODE)) {
			if (declaration instanceof FieldNode) {
				if (isStaticObjectExpression && !((FieldNode) declaration).isStatic()) {
					confidence = UNKNOWN;
				}
			} else if (declaration instanceof PropertyNode) {
				FieldNode underlyingField = ((PropertyNode) declaration).getField();
				if (underlyingField != null) {
					// prefer looking at the underlying field
					if (isStaticObjectExpression && !underlyingField.isStatic()) {
						confidence = UNKNOWN;
					}
				} else if (isStaticObjectExpression && !((PropertyNode) declaration).isStatic()) {
					confidence = UNKNOWN;
				}
			} else if (declaration instanceof MethodNode) {
				if (isStaticObjectExpression && !((MethodNode) declaration).isStatic()) {
					confidence = UNKNOWN;
				}
			}
		}

		if (confidence == UNKNOWN && realDeclaringType.getName().equals(VariableScope.CLASS_CLASS_NODE.getName())) {
			// GRECLIPSE-1544
			// check the type parameter for this class node reference
			// likely a type coming in from STC
			GenericsType[] classTypeParams = realDeclaringType.getGenericsTypes();
			ClassNode typeParam = classTypeParams != null && classTypeParams.length == 1 ? classTypeParams[0].getType() : null;

			if (typeParam != null && !typeParam.getName().equals(VariableScope.CLASS_CLASS_NODE.getName())
					&& !typeParam.getName().equals(VariableScope.OBJECT_CLASS_NODE.getName())) {
				return findTypeForNameWithKnownObjectExpression(name, type, typeParam, scope, origConfidence,
						isStaticObjectExpression, isPrimaryExpression);
			}
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
			ASTNode maybeDeclaration = findDeclaration(accessedVar.getName(), getMorePreciseType(declaringType, info),
					scope.getMethodCallNumberOfArguments());
			if (maybeDeclaration != null) {
				declaration = maybeDeclaration;
				// declaring type may have changed
				declaringType = declaringTypeFromDeclaration(declaration, info != null ? info.declaringType
						: VariableScope.OBJECT_CLASS_NODE);
			} else {
				confidence = UNKNOWN;
			}
		}

		ClassNode type;
		if (info != null) {
			confidence = TypeConfidence.findLessPrecise(origConfidence, INFERRED);
			type = info.type;
			declaringType = getMorePreciseType(declaringType, info);
			if (scope.isThisOrSuper(var)) {
				declaration = type;
			}
		} else {

			// we have a variable expression, but it is not
			// declared anywhere in the scope. It is probably a DynamicVariable
			if (accessedVar instanceof DynamicVariable) {
				type = typeFromDeclaration(declaration, declaringType);
			} else {
				type = var.getType();
			}
		}
		return new TypeLookupResult(type, declaringType, declaration, confidence, scope);
	}

	private ClassNode getMorePreciseType(ClassNode declaringType, VariableInfo info) {
		ClassNode maybeDeclaringType = info != null ? info.declaringType : VariableScope.OBJECT_CLASS_NODE;
		if (maybeDeclaringType.equals(VariableScope.OBJECT_CLASS_NODE) && !VariableScope.OBJECT_CLASS_NODE.equals(declaringType)) {
			return declaringType;
		} else {
			return maybeDeclaringType;
		}
	}

	private ClassNode declaringTypeFromDeclaration(ASTNode declaration, ClassNode resolvedTypeOfDeclaration) {
		ClassNode typeOfDeclaration;
		if (declaration instanceof FieldNode) {
			typeOfDeclaration = ((FieldNode) declaration).getDeclaringClass();
		} else if (declaration instanceof MethodNode) {
			typeOfDeclaration = ((MethodNode) declaration).getDeclaringClass();
		} else if (declaration instanceof PropertyNode) {
			typeOfDeclaration = ((PropertyNode) declaration).getDeclaringClass();
		} else {
			typeOfDeclaration = VariableScope.OBJECT_CLASS_NODE;
		}
		// don't necessarily use the typeOfDeclaration. the resolvedTypeOfDeclaration includes the types of generics
		// so if the names are the same, then used the resolved version
		if (typeOfDeclaration.getName().equals(resolvedTypeOfDeclaration.getName())) {
			return resolvedTypeOfDeclaration;
		} else {
			return typeOfDeclaration;
		}
	}

	/**
	 * @param declaration the declaration to look up
	 * @param resolvedType the unredirected type that declares this declaration somewhere in its hierarchy
	 * @return class node with generics replaced by actual types
	 */
	private ClassNode typeFromDeclaration(ASTNode declaration, ClassNode resolvedType) {
		ClassNode typeOfDeclaration, declaringType = declaringTypeFromDeclaration(declaration, resolvedType);
		if (declaration instanceof PropertyNode) {
			FieldNode field = ((PropertyNode) declaration).getField();
			if (field != null) {
				declaration = field;
			}
		}
		if (declaration instanceof FieldNode) {
			FieldNode fieldNode = (FieldNode) declaration;
			typeOfDeclaration = fieldNode.getType();
			if (VariableScope.OBJECT_CLASS_NODE.equals(typeOfDeclaration)) {
				// check to see if we can do better by looking at the initializer of the field
				if (fieldNode.hasInitialExpression()) {
					typeOfDeclaration = fieldNode.getInitialExpression().getType();
				}
			}
		} else if (declaration instanceof MethodNode) {
			typeOfDeclaration = ((MethodNode) declaration).getReturnType();
		} else if (declaration instanceof Expression) {
			typeOfDeclaration = ((Expression) declaration).getType();
		} else {
			typeOfDeclaration = VariableScope.OBJECT_CLASS_NODE;
		}

		// now try to resolve generics
		// travel up the hierarchy and look for more generics
		// also look for generics on methods...(not doing this yet...)
		GenericsMapper mapper = GenericsMapper.gatherGenerics(resolvedType, declaringType.redirect());
		ClassNode resolvedTypeOfDeclaration = VariableScope.resolveTypeParameterization(mapper,
				VariableScope.clone(typeOfDeclaration));
		return resolvedTypeOfDeclaration;
	}

	protected GenericsType[] unresolvedGenericsForType(ClassNode unresolvedType) {
		ClassNode candidate = unresolvedType;
		GenericsType[] gts = candidate.getGenericsTypes();
		gts = gts == null ? NO_GENERICS : gts;
		List<GenericsType> allGs = new ArrayList<GenericsType>(2);
		while (candidate != null) {
			gts = candidate.getGenericsTypes();
			gts = gts == null ? NO_GENERICS : gts;
			for (GenericsType gt : gts) {
				allGs.add(gt);
			}
			candidate = candidate.getSuperClass();
		}
		return allGs.toArray(NO_GENERICS);
	}

	/**
	 * Looks for the named member in the declaring type. Also searches super types. The result can be a field, method, or property.
	 * 
	 * If numOfArgs is >= 0, then look for a method first, otherwise look for a property and then a field
	 * 
	 * @param name
	 * @param declaringType
	 * @param numOfArgs number of arguments to the associated method call (or -1 if not a method call)
	 * @return
	 */
	private ASTNode findDeclaration(String name, ClassNode declaringType, int numOfArgs) {
		if (declaringType.isArray()) {
			// only length exists on array type
			if (name.equals("length")) {
				return createLengthField(declaringType);
			} else {
				// otherwise search on object
				return findDeclaration(name, VariableScope.OBJECT_CLASS_NODE, numOfArgs);
			}
		}

		AnnotatedNode maybe = null;
		if (numOfArgs >= 0) {
			// this expression is part of a method call expression and so, look for methods first
			maybe = findMethodDeclaration(name, declaringType, numOfArgs, true);
			if (maybe != null) {
				return maybe;
			}
		}

		LinkedHashSet<ClassNode> allClasses = new LinkedHashSet<ClassNode>();
		VariableScope.createTypeHierarchy(declaringType, allClasses, true);

		maybe = findPropertyInClass(name, allClasses);
		if (maybe != null) {
			return maybe;
		}

		maybe = declaringType.getField(name);
		if (maybe != null) {
			return maybe;
		}

		// look for constants declared in super class
		FieldNode constantFromSuper = findConstantInClass(name, allClasses);
		if (constantFromSuper != null) {
			return constantFromSuper;
		}

		if (numOfArgs < 0) {
			// this expression is not part of a method call expression and so, look for methods last
			maybe = findMethodDeclaration(name, declaringType, numOfArgs, true);
			if (maybe != null) {
				return maybe;
			}
		}

		return null;
	}

	/**
	 * Finds a method with the given name in the declaring type. Will prioritize methods with the same number of arguments, but if
	 * multiple methods exist with same name, then will return an arbitrary one.
	 * 
	 * @param name
	 * @param declaringType
	 * @param numOfArgs
	 * @param checkSuperInterfaces potentially look through super interfaces for a declaration to this method
	 * @return
	 */
	private AnnotatedNode findMethodDeclaration(String name, ClassNode declaringType, int numOfArgs, boolean checkSuperInterfaces) {
		// if this is an interface, then we also need to check super interfaces
		// super interface methods on an interface are not returned by getMethods(), so must explicitly look for them
		// do this piece first since findAllInterfaces will return the current interface as well and this will avoid running this
		// method on the same interface twice.
		if (checkSuperInterfaces && declaringType.isInterface()) {
			LinkedHashSet<ClassNode> allInterfaces = new LinkedHashSet<ClassNode>();
			VariableScope.findAllInterfaces(declaringType, allInterfaces, true);
			for (ClassNode interf : allInterfaces) {
				AnnotatedNode candidate = findMethodDeclaration(name, interf, numOfArgs, false);
				if (candidate != null) {
					return candidate;
				}
			}
			return null;
		}

		List<MethodNode> maybeMethods = declaringType.getMethods(name);
		if (maybeMethods != null && maybeMethods.size() > 0) {
			// prefer retrieving the method with the same number of args as specified in the parameter.
			// if none exists, or parameter is -1, then arbitrarily choose the first.
			if (numOfArgs >= 0) {
				for (MethodNode maybeMethod : maybeMethods) {
					Parameter[] parameters = maybeMethod.getParameters();
					if ((parameters != null && parameters.length == numOfArgs) || (parameters == null && numOfArgs == 0)) {
						return maybeMethod.getOriginal();
					}
				}
			}
			return maybeMethods.get(0);
		}

		if (numOfArgs < 0) {
			return AccessorSupport.findAccessorMethodForPropertyName(name, declaringType, false);
		} else {
			return null;
		}

	}

	private ASTNode createLengthField(ClassNode declaringType) {
		FieldNode lengthField = new FieldNode("length", Opcodes.ACC_PUBLIC, VariableScope.INTEGER_CLASS_NODE, declaringType, null);
		lengthField.setType(VariableScope.INTEGER_CLASS_NODE);
		lengthField.setDeclaringClass(declaringType);
		return lengthField;
	}

	private PropertyNode findPropertyInClass(String name, Set<ClassNode> allClasses) {
		for (ClassNode clazz : allClasses) {
			PropertyNode prop = clazz.getProperty(name);
			if (prop != null) {
				return prop;
			}
		}
		return null;
	}

	private FieldNode findConstantInClass(String name, Set<ClassNode> allClasses) {
		for (ClassNode clazz : allClasses) {
			FieldNode field = clazz.getField(name);
			if (field != null && Flags.isFinal(field.getModifiers()) && field.isStatic()) {
				return field;
			}
		}
		return null;
	}
}