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
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ImportNodeCompatibilityWrapper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.RegexExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.groovy.search.ITypeRequestor.VisitStatus;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @author Andrew Eisenberg
 * @created Aug 29, 2009
 * 
 *          Visits a GroovyCompilationUnit in order to determine the type of expressions contained in it.
 */
@SuppressWarnings("nls")
public class TypeInferencingVisitorWithRequestor extends ClassCodeVisitorSupport {

	public class VisitCompleted extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	private final GroovyCompilationUnit unit;

	private final Stack<VariableScope> scopes;

	// we are going to have to be very careful about the ordering of lookups
	// Simple type lookup must be last because it always returns an answer
	// Assume that if something returns an answer, then we go with that.
	// Later on, should do some ordering of results
	private final ITypeLookup[] lookups;

	private ITypeRequestor requestor;
	private IJavaElement enclosingElement;
	private ASTNode enclosingDeclarationNode;

	private boolean rethrowVisitComplete = false;

	/**
	 * The head of the stack is the current property/attribute/methodcall expression being visited. This stack is used so we can
	 * keep track of the type of the object expressions in these property expressions
	 */
	private Stack<Expression> propertyExpression;

	/**
	 * Keeps track of the type of the object expression corresponding to each frame of the property expression.
	 */
	private Stack<ClassNode> objectExpressionType;
	/**
	 * Keeps track of the type of the type of the property field corresponding to each frame of the property expression.
	 */
	private Stack<ClassNode> propertyExpressionType;

	/**
	 * Use factory to instantiate
	 */
	TypeInferencingVisitorWithRequestor(GroovyCompilationUnit unit, ITypeLookup[] lookups) {
		super();
		this.unit = unit;
		enclosingDeclarationNode = createModuleNode(unit);
		this.lookups = lookups;
		scopes = new Stack<VariableScope>();
		propertyExpression = new Stack<Expression>();
		objectExpressionType = new Stack<ClassNode>();
		propertyExpressionType = new Stack<ClassNode>();
	}

	public void visitCompilationUnit(ITypeRequestor requestor) {
		if (enclosingDeclarationNode == null) {
			// no module node, can't do anything
			return;
		}

		this.requestor = requestor;
		enclosingElement = unit;
		rethrowVisitComplete = true;
		VariableScope topLevelScope = new VariableScope(null, enclosingDeclarationNode);
		scopes.push(topLevelScope);

		for (ITypeLookup lookup : lookups) {
			lookup.initialize(unit, topLevelScope);
		}

		try {
			visitPackage(((ModuleNode) enclosingDeclarationNode).getPackage());
			visitImports((ModuleNode) enclosingDeclarationNode);
			try {
				IType[] types = unit.getTypes();
				for (IType type : types) {
					visitJDT(type, requestor);
				}
			} catch (JavaModelException e) {
				Util.log(e, "Error getting types for " + unit.getElementName());
			}

			scopes.pop();

		} catch (VisitCompleted vc) {
		} catch (Exception e) {
			Util.log(e, "Error performing search for " + unit.getElementName());
		}
	}

	// @Override
	public void visitPackage(PackageNode p) {
		// do nothing for now
	}

	public void visitJDT(IType type, ITypeRequestor requestor) {
		IJavaElement oldEnclosing = enclosingElement;
		ASTNode oldEnclosingNode = enclosingDeclarationNode;
		boolean oldRethrow = rethrowVisitComplete;
		rethrowVisitComplete = true;
		enclosingElement = type;
		ClassNode node = findClassWithName(createName(type));
		if (node == null) {
			// probably some sort of AST transformation is making this node invisible
			return;
		}
		try {

			scopes.push(new VariableScope(scopes.peek(), node));
			enclosingDeclarationNode = node;
			visitClassInternal(node);

			try {
				// visitJDT so that we have the proper enclosing element
				boolean isEnum = type.isEnum();
				for (IJavaElement child : type.getChildren()) {
					// filter out synthetic members for enums
					if (isEnum && shouldFilterEnumMember(child)) {
						continue;
					}
					switch (child.getElementType()) {
						case IJavaElement.METHOD:
							visitJDT((IMethod) child, requestor);
							break;

						case IJavaElement.FIELD:
							visitJDT((IField) child, requestor);
							break;

						case IJavaElement.TYPE:
							visitJDT((IType) child, requestor);
							break;

						default:
							break;
					}
				}

				// visit synthetic default constructor...this is where the object initializers are stuffed
				if (!type.getMethod("<init>", new String[0]).exists()) {
					ConstructorNode defConstructor = findDefaultConstructor(node);
					if (defConstructor != null) {
						visitConstructorOrMethod(defConstructor, true);
					}
				}
			} catch (JavaModelException e) {
				Util.log(e, "Error getting children of " + type.getFullyQualifiedName());
			}

		} catch (VisitCompleted vc) {
			if (oldRethrow) {
				throw vc;
			}
		} finally {
			enclosingElement = oldEnclosing;
			enclosingDeclarationNode = oldEnclosingNode;
			rethrowVisitComplete = oldRethrow;
			scopes.pop();
		}
	}

	/**
	 * @param node
	 * @return
	 */
	private ConstructorNode findDefaultConstructor(ClassNode node) {
		List<ConstructorNode> constructors = node.getDeclaredConstructors();
		for (ConstructorNode constructor : constructors) {
			if (constructor.getParameters() == null || constructor.getParameters().length == 0) {
				return constructor;
			}
		}
		return null;
	}

	/**
	 * @param child
	 * @return
	 */
	private boolean shouldFilterEnumMember(IJavaElement child) {
		int type = child.getElementType();
		String name = child.getElementName();
		if (name.indexOf('$') >= 0) {
			return true;
		} else if (type == IJavaElement.METHOD) {
			if ((name.equals("next") || name.equals("previous")) && ((IMethod) child).getNumberOfParameters() == 0) {
				return true;
			}
		} else if (type == IJavaElement.METHOD) {
			if (name.equals("MIN_VALUE") || name.equals("MAX_VALUE")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create type name taking into account inner types
	 */
	private String createName(IType type) {
		StringBuilder sb = new StringBuilder();
		sb.append(type.getElementName());
		while (type.getParent().getElementType() == IJavaElement.TYPE) {
			sb.insert(0, '$');
			type = (IType) type.getParent();
			sb.insert(0, type.getElementName());
		}
		return sb.toString();
	}

	public void visitJDT(IField field, ITypeRequestor requestor) {
		IJavaElement oldEnclosing = enclosingElement;
		ASTNode oldEnclosingNode = enclosingDeclarationNode;
		enclosingElement = field;
		this.requestor = requestor;
		FieldNode fieldNode = findFieldNode(field);
		if (fieldNode == null) {
			// probably some sort of AST transformation is making this node invisible
			return;
		}

		enclosingDeclarationNode = fieldNode;
		scopes.push(new VariableScope(scopes.peek(), fieldNode));
		try {
			visitField(fieldNode);
		} catch (VisitCompleted vc) {
			if (rethrowVisitComplete) {
				throw vc;
			}
		} finally {
			enclosingDeclarationNode = oldEnclosingNode;
			enclosingElement = oldEnclosing;
			scopes.pop();
		}

		if (isLazy(fieldNode)) {
			// GRECLIPSE-578 the @Lazy annotation forces an AST transformation
			// not sure if we get much here because I think the body of the generated method for
			// @Lazy is filled with binary instructions.
			List<MethodNode> lazyMethods = ((ClassNode) enclosingDeclarationNode).getDeclaredMethods("set$"
					+ field.getElementName());
			if (lazyMethods.size() > 0) {
				MethodNode lazyMethod = lazyMethods.get(0);
				enclosingDeclarationNode = lazyMethod;
				this.requestor = requestor;
				scopes.push(new VariableScope(scopes.peek(), lazyMethod));
				try {
					visitConstructorOrMethod(lazyMethod, lazyMethod instanceof ConstructorNode);
				} catch (VisitCompleted vc) {
					if (rethrowVisitComplete) {
						throw vc;
					}
				} finally {
					enclosingElement = oldEnclosing;
					enclosingDeclarationNode = oldEnclosingNode;
					scopes.pop();
				}
			}
		}
	}

	public void visitJDT(IMethod method, ITypeRequestor requestor) {
		IJavaElement oldEnclosing = enclosingElement;
		ASTNode oldEnclosingNode = enclosingDeclarationNode;
		enclosingElement = method;
		MethodNode methodNode = findMethodNode(method);
		if (methodNode == null) {
			// probably some sort of AST transformation is making this node invisible
			return;
		}

		enclosingDeclarationNode = methodNode;
		this.requestor = requestor;
		scopes.push(new VariableScope(scopes.peek(), methodNode));
		try {
			visitConstructorOrMethod(methodNode, method.isConstructor());
		} catch (VisitCompleted vc) {
			if (rethrowVisitComplete) {
				throw vc;
			}
		} catch (JavaModelException e) {
			Util.log(e, "Exception visiting method " + method.getElementName() + " in class " //$NON-NLS-1$ //$NON-NLS-2$
					+ method.getParent().getElementName());
		} finally {
			enclosingElement = oldEnclosing;
			enclosingDeclarationNode = oldEnclosingNode;
			scopes.pop();
		}
	}

	/**
	 * visit the class itself
	 * 
	 * @param node
	 */
	private void visitClassInternal(ClassNode node) {
		visitAnnotations(node);

		TypeLookupResult result = null;
		VariableScope scope = scopes.peek();
		result = new TypeLookupResult(node, node, node, TypeConfidence.EXACT, scope);
		VisitStatus status = handleRequestor(node, requestor, result);
		switch (status) {
			case CONTINUE:
				break;
			case CANCEL_BRANCH:
				return;
			case STOP_VISIT:
				throw new VisitCompleted();
		}

		if (!node.isEnum()) {
			visitGenerics(node);
			visitClassReference(node.getUnresolvedSuperClass());
		}

		for (ClassNode intr : node.getInterfaces()) {
			visitClassReference(intr);
		}

		// add all methods to the scope because when they are
		// referenced without parens, they appear
		// as VariableExpressions in the code
		VariableScope currentScope = scope;
		// don't use Java 5 style for loop here because Groovy 1.6.x does not
		// have type parameters for its getMethods() method.
		for (Iterator methodIter = node.getMethods().iterator(); methodIter.hasNext();) {
			MethodNode method = (MethodNode) methodIter.next();
			currentScope.addVariable(method.getName(), method.getReturnType(), method.getDeclaringClass());
		}

		// visit <clinit> body because this is where static field initializers are placed
		MethodNode clinit = node.getMethod("<clinit>", new Parameter[0]);
		if (clinit != null && clinit.getCode() instanceof BlockStatement) {
			for (Statement element : (Iterable<Statement>) ((BlockStatement) clinit.getCode()).getStatements()) {
				element.visit(this);
			}
		}

		for (Statement element : (Iterable<Statement>) node.getObjectInitializerStatements()) {
			element.visit(this);
		}

		// visit synthetic no-arg constructors because that's where the non-static initializers are
		for (ConstructorNode constructor : (Iterable<ConstructorNode>) node.getDeclaredConstructors()) {
			if (constructor.isSynthetic() && (constructor.getParameters() == null || constructor.getParameters().length == 0)) {
				visitConstructor(constructor);
			}
		}
		// don't visit contents, the visitJDT methods are used instead
	}

	@Override
	public void visitField(FieldNode node) {
		TypeLookupResult result = null;
		VariableScope scope = scopes.peek();
		for (ITypeLookup lookup : lookups) {
			TypeLookupResult candidate = lookup.lookupType(node, scope);
			if (candidate != null) {
				if (result == null || result.confidence.isLessPreciseThan(candidate.confidence)) {
					result = candidate;
				}
				if (TypeConfidence.LOOSELY_INFERRED.isLessPreciseThan(result.confidence)) {
					break;
				}
			}
		}
		VisitStatus status = handleRequestor(node, requestor, result);
		switch (status) {
			case CONTINUE:
				ClassNode fieldType = node.getType();
				// if two values are == then that means the type
				// is synthetic and doesn't exist in code
				// probably an enum field.
				if (fieldType != node.getDeclaringClass()) {
					visitClassReference(fieldType);
				}
				super.visitField(node);
			case CANCEL_BRANCH:
				return;
			case STOP_VISIT:
				throw new VisitCompleted();
		}
	}

	/**
	 * Visit a return type
	 */
	private void visitClassReference(ClassNode node) {
		// if this is a placeholder, then the type
		// doesn't really exist in the code, so can ignore
		if (node.isGenericsPlaceHolder()) {
			return;
		}

		TypeLookupResult result = null;
		VariableScope scope = scopes.peek();
		for (ITypeLookup lookup : lookups) {
			TypeLookupResult candidate = lookup.lookupType(node, scope);
			if (candidate != null) {
				if (result == null || result.confidence.isLessPreciseThan(candidate.confidence)) {
					result = candidate;
				}
				if (TypeConfidence.LOOSELY_INFERRED.isLessPreciseThan(result.confidence)) {
					break;
				}
			}
		}

		VisitStatus status = handleRequestor(node, requestor, result);
		switch (status) {
			case CONTINUE:
				if (!node.isEnum()) {
					visitGenerics(node);
				}
				// fall through
			case CANCEL_BRANCH:
				return;
			case STOP_VISIT:
				throw new VisitCompleted();
		}
	}

	/**
	 * @param node
	 */
	private void visitGenerics(ClassNode node) {
		if (node.isUsingGenerics() && node.getGenericsTypes() != null) {
			for (GenericsType gen : node.getGenericsTypes()) {
				if (gen.getLowerBound() != null) {
					visitClassReference(gen.getLowerBound());
				}
				if (gen.getUpperBounds() != null) {
					for (ClassNode upper : gen.getUpperBounds()) {
						// handle enums where the upper bound is the same as the type
						if (!upper.getName().equals(node.getName())) {
							visitClassReference(upper);
						}
					}
				}
				if (gen.getType() != null && gen.getName().charAt(0) != '?') {
					visitClassReference(gen.getType());
				}
			}
		}
	}

	@Override
	public void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
		TypeLookupResult result = null;
		VariableScope scope = scopes.peek();
		for (ITypeLookup lookup : lookups) {
			TypeLookupResult candidate = lookup.lookupType(node, scope);
			if (candidate != null) {
				if (result == null || result.confidence.isLessPreciseThan(candidate.confidence)) {
					result = candidate;
				}
				if (TypeConfidence.LOOSELY_INFERRED.isLessPreciseThan(result.confidence)) {
					break;
				}
			}
		}
		VisitStatus status = handleRequestor(node, requestor, result);

		switch (status) {
			case CONTINUE:
				GenericsType[] gens = node.getGenericsTypes();
				if (gens != null) {
					for (GenericsType gen : gens) {
						if (gen.getLowerBound() != null) {
							visitClassReference(gen.getLowerBound());
						}
						if (gen.getUpperBounds() != null) {
							for (ClassNode upper : gen.getUpperBounds()) {
								visitClassReference(upper);
							}
						}
						if (gen.getType() != null && gen.getType().getName().charAt(0) != '?') {
							visitClassReference(gen.getType());
						}
					}
				}

				visitClassReference(node.getReturnType());
				if (node.getExceptions() != null) {
					for (ClassNode e : node.getExceptions()) {
						visitClassReference(e);
					}
				}

				if (handleParameterList(node.getParameters())) {
					super.visitConstructorOrMethod(node, isConstructor);
				}
				// fall through
			case CANCEL_BRANCH:
				return;
			case STOP_VISIT:
				throw new VisitCompleted();
		}
	}

	@Override
	public void visitAnnotations(AnnotatedNode node) {
		for (AnnotationNode annotation : (Iterable<AnnotationNode>) node.getAnnotations()) {
			visitAnnotation(annotation);
		}
		super.visitAnnotations(node);
	}

	// @Override
	public void visitImports(ModuleNode node) {
		for (ImportNode imp : new ImportNodeCompatibilityWrapper(node).getAllImportNodes()) {
			TypeLookupResult result = null;
			IJavaElement oldEnclosingElement = enclosingElement;
			// FIXADE this will not work for static or * imports
			if (imp.getType() != null) {
				enclosingElement = unit.getImport(imp.getClassName());
				if (!enclosingElement.exists()) {
					enclosingElement = oldEnclosingElement;
				}
			}

			VariableScope scope = scopes.peek();
			for (ITypeLookup lookup : lookups) {
				TypeLookupResult candidate = lookup.lookupType(imp, scope);
				if (candidate != null) {
					if (result == null || result.confidence.isLessPreciseThan(candidate.confidence)) {
						result = candidate;
					}
					if (TypeConfidence.LOOSELY_INFERRED.isLessPreciseThan(result.confidence)) {
						break;
					}
				}
			}
			VisitStatus status = handleRequestor(imp, requestor, result);
			enclosingElement = oldEnclosingElement;
			switch (status) {
				case CONTINUE:
					continue;
				case CANCEL_BRANCH:
					return;
				case STOP_VISIT:
					throw new VisitCompleted();
			}
		}
	}

	private boolean handleStatement(Statement node) {
		// don't check the lookups because statements have no type.
		// but individual requestors may choose to end the visit here
		TypeLookupResult noLookup = new TypeLookupResult(VariableScope.OBJECT_CLASS_NODE, VariableScope.OBJECT_CLASS_NODE,
				VariableScope.OBJECT_CLASS_NODE, TypeConfidence.EXACT, scopes.peek());
		VisitStatus status = handleRequestor(node, requestor, noLookup);
		switch (status) {
			case CONTINUE:
				return true;
			case CANCEL_BRANCH:
				return false;
			case STOP_VISIT:
			default:
				throw new VisitCompleted();
		}

	}

	private boolean handleExpression(Expression node) {
		TypeLookupResult result = null;
		ClassNode objectExprType = null;
		if (isProperty(node)) {
			objectExprType = objectExpressionType.pop();
		}

		VariableScope scope = scopes.peek();
		for (ITypeLookup lookup : lookups) {
			TypeLookupResult candidate = lookup.lookupType(node, scope, objectExprType);
			if (candidate != null) {
				if (result == null || result.confidence.isLessPreciseThan(candidate.confidence)) {
					result = candidate;
				}
				if (TypeConfidence.LOOSELY_INFERRED.isLessPreciseThan(result.confidence)) {
					break;
				}
			}
		}
		VisitStatus status = handleRequestor(node, requestor, result);
		switch (status) {
			case CONTINUE:
				if (isObjectExpression(node)) {
					objectExpressionType.push(result.type);
				} else if (isProperty(node)) {
					propertyExpressionType.push(result.type);
				}
				return true;
			case CANCEL_BRANCH:
				if (isObjectExpression(node)) {
					objectExpressionType.push(result.type);
				} else if (isProperty(node)) {
					propertyExpressionType.push(result.type);
				}
				return false;
			case STOP_VISIT:
				throw new VisitCompleted();
		}
		// won't get here
		return false;
	}

	private boolean handleParameterList(Parameter[] params) {
		if (params != null) {
			VariableScope scope = scopes.peek();
			for (Parameter node : params) {
				TypeLookupResult result = null;
				for (ITypeLookup lookup : lookups) {
					// the first lookup is used to store the type of the
					// parameter in the sope
					lookup.lookupType(node, scope);
					result = lookup.lookupType(node.getType(), scope);
					TypeLookupResult candidate = lookup.lookupType(node.getType(), scope);
					if (candidate != null) {
						if (result == null || result.confidence.isLessPreciseThan(candidate.confidence)) {
							result = candidate;
						}
						if (TypeConfidence.LOOSELY_INFERRED.isLessPreciseThan(result.confidence)) {
							break;
						}
					}
				}
				// visit the parameter itself
				TypeLookupResult parameterResult = new TypeLookupResult(result.type, result.declaringType, node,
						TypeConfidence.EXACT, scope);
				VisitStatus status = handleRequestor(node, requestor, parameterResult);
				switch (status) {
					case CONTINUE:
						break;
					case CANCEL_BRANCH:
						return false;
					case STOP_VISIT:
						throw new VisitCompleted();
				}

				// visit the parameter type
				visitClassReference(node.getType());

				visitAnnotations(node);
				Expression init = node.getInitialExpression();
				if (init != null) {
					init.visit(this);
				}
			}
		}
		return true;
	}

	@Override
	public void visitVariableExpression(VariableExpression node) {
		// VariableExpressions not an AnnotatedNode in groovy 1.6, but they are in 1.7+
		Object maybeAnnotatedNode = node;
		if (maybeAnnotatedNode instanceof AnnotatedNode) {
			visitAnnotations((AnnotatedNode) maybeAnnotatedNode);
		}

		// this is a declaration
		if (node.getAccessedVariable() == node) {
			visitClassReference(node.getType());
		}
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitVariableExpression(node);
		}
	}

	@Override
	public void visitArgumentlistExpression(ArgumentListExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitArgumentlistExpression(node);
		}
	}

	@Override
	public void visitArrayExpression(ArrayExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitArrayExpression(node);
		}
	}

	@Override
	public void visitAttributeExpression(AttributeExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			propertyExpression.push(node);
			super.visitAttributeExpression(node);
			propertyExpression.pop();
		}
	}

	@Override
	public void visitBinaryExpression(BinaryExpression node) {
		// BinaryExpressions not an AnnotatedNode in groovy 1.6, but they are in 1.7+
		Object maybeAnnotatedNode = node;
		if (maybeAnnotatedNode instanceof AnnotatedNode) {
			visitAnnotations((AnnotatedNode) maybeAnnotatedNode);
		}

		propertyExpression.push(node);
		node.getRightExpression().visit(this);
		boolean shouldContinue = handleExpression(node);

		// the declaration itself is the property node
		ClassNode propType = propertyExpressionType.pop();
		propertyExpression.pop();

		if (shouldContinue) {
			node.getLeftExpression().visit(this);

			if (isObjectExpression(node)) {
				// returns true if this declaration expression is the property field of another property expression
				objectExpressionType.push(propType);
			}
		} else {
			propertyExpression.pop();

			// not popped earlier because the method field of the expression was not examined
			objectExpressionType.pop();
		}
	}

	@Override
	public void visitBitwiseNegationExpression(BitwiseNegationExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitBitwiseNegationExpression(node);
		}
	}

	@Override
	public void visitBooleanExpression(BooleanExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitBooleanExpression(node);
		}
	}

	@Override
	public void visitBytecodeExpression(BytecodeExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitBytecodeExpression(node);
		}
	}

	@Override
	public void visitCastExpression(CastExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			visitClassReference(node.getType());
			super.visitCastExpression(node);
		}
	}

	@Override
	public void visitClassExpression(ClassExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitClassExpression(node);
		}
	}

	@Override
	public void visitClosureExpression(ClosureExpression node) {
		VariableScope scope = new VariableScope(scopes.peek(), node);
		scopes.push(scope);
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			if (node.getParameters() != null && node.getParameters().length > 0) {
				handleParameterList(node.getParameters());
			}
			if (scope.lookupName("it") == null) {
				scope.addVariable("it", VariableScope.OBJECT_CLASS_NODE, VariableScope.OBJECT_CLASS_NODE);
			}
			super.visitClosureExpression(node);
		}
		scopes.pop();
	}

	@Override
	public void visitBlockStatement(BlockStatement block) {
		scopes.push(new VariableScope(scopes.peek(), block));
		boolean shouldContinue = handleStatement(block);
		if (shouldContinue) {
			super.visitBlockStatement(block);
		}
		scopes.pop();
	}

	@Override
	public void visitReturnStatement(ReturnStatement ret) {
		boolean shouldContinue = handleStatement(ret);
		if (shouldContinue) {
			// special case: AnnotationConstantExpressions do not visit their type.
			// this means that annotations in default expressions are not visited.
			// check that here
			if (ret.getExpression() instanceof AnnotationConstantExpression) {
				visitClassReference(((AnnotationConstantExpression) ret.getExpression()).getType());
			}
			super.visitReturnStatement(ret);
		}
	}

	@Override
	public void visitForLoop(ForStatement node) {
		scopes.push(new VariableScope(scopes.peek(), node));
		Parameter param = node.getVariable();
		if (param != null) {
			handleParameterList(new Parameter[] { param });
		}
		super.visitForLoop(node);
		scopes.pop();
	}

	@Override
	public void visitCatchStatement(CatchStatement node) {
		scopes.push(new VariableScope(scopes.peek(), node));
		Parameter param = node.getVariable();
		if (param != null) {
			handleParameterList(new Parameter[] { param });
		}
		super.visitCatchStatement(node);
		scopes.pop();
	}

	@Override
	public void visitClosureListExpression(ClosureListExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitClosureListExpression(node);
		}
	}

	@Override
	public void visitConstantExpression(ConstantExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitConstantExpression(node);
		}
	}

	@Override
	public void visitConstructorCallExpression(ConstructorCallExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			visitClassReference(node.getType());
			super.visitConstructorCallExpression(node);
		}
	}

	@Override
	public void visitDeclarationExpression(DeclarationExpression node) {
		// this is ok. the variable expression is visited appropriately
		visitBinaryExpression(node);
	}

	@Override
	public void visitFieldExpression(FieldExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitFieldExpression(node);
		}
	}

	@Override
	public void visitGStringExpression(GStringExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitGStringExpression(node);
		}
	}

	@Override
	public void visitListExpression(ListExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitListExpression(node);
		}
	}

	@Override
	public void visitMapEntryExpression(MapEntryExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitMapEntryExpression(node);
		}
	}

	@Override
	public void visitMapExpression(MapExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitMapExpression(node);
		}
	}

	@Override
	public void visitMethodCallExpression(MethodCallExpression node) {
		propertyExpression.push(node);
		node.getObjectExpression().visit(this);
		boolean shouldContinue = handleExpression(node);
		// boolean shouldContinue = true;
		if (shouldContinue) {
			node.getMethod().visit(this);
			// this is the type of this property expression
			ClassNode propType = propertyExpressionType.pop();

			propertyExpression.pop();

			ClassNode catNode = isCategoryDeclaration(node);
			if (catNode != null) {
				addCategoryToBeDeclared(catNode);
			}
			node.getArguments().visit(this);
			if (isObjectExpression(node)) {
				// returns true if this method call expression is the property field of another property expression
				objectExpressionType.push(propType);
			}
		} else {
			propertyExpression.pop();

			// not popped earlier because the method field of the expression was not examined
			objectExpressionType.pop();
		}
	}

	/**
	 * @param node
	 */
	private void addCategoryToBeDeclared(ClassNode catNode) {
		scopes.peek().setCategoryBeingDeclared(catNode);
	}

	/**
	 * @param node
	 * @return
	 */
	private ClassNode isCategoryDeclaration(MethodCallExpression node) {
		String methodAsString = node.getMethodAsString();
		if (methodAsString != null && methodAsString.equals("use")) {
			Expression exprs = node.getArguments();
			if (exprs instanceof ArgumentListExpression) {
				ArgumentListExpression args = (ArgumentListExpression) exprs;
				if (args.getExpressions().size() >= 2 && args.getExpressions().get(1) instanceof ClosureExpression) {
					// really, should be doing inference on the first expression and seeing if it
					// is a class node, but looking up in scope is good enough for now
					Expression expr = ((List<Expression>) args.getExpressions()).get(0);
					if (expr instanceof ClassExpression) {
						return expr.getType();
					} else if (expr instanceof VariableExpression && expr.getText() != null) {
						VariableInfo info = scopes.peek().lookupName(expr.getText());
						if (info != null) {
							return info.type;
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void visitMethodPointerExpression(MethodPointerExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitMethodPointerExpression(node);
		}
	}

	@Override
	public void visitNotExpression(NotExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitNotExpression(node);
		}
	}

	@Override
	public void visitPostfixExpression(PostfixExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitPostfixExpression(node);
		}
	}

	@Override
	public void visitPrefixExpression(PrefixExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitPrefixExpression(node);
		}
	}

	@Override
	public void visitPropertyExpression(PropertyExpression node) {
		propertyExpression.push(node);
		node.getObjectExpression().visit(this);
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			node.getProperty().visit(this);
			// this is the type of this property expression
			ClassNode propType = propertyExpressionType.pop();

			propertyExpression.pop();
			if (isObjectExpression(node)) {
				// returns true if this property expression is the property field of another property expression
				objectExpressionType.push(propType);
			}
		} else {
			propertyExpression.pop();

			// not popped earlier because the property field of the expression was not examined
			objectExpressionType.pop();
		}
	}

	@Override
	public void visitRangeExpression(RangeExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitRangeExpression(node);
		}
	}

	@Override
	public void visitRegexExpression(RegexExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitRegexExpression(node);
		}
	}

	@Override
	public void visitShortTernaryExpression(ElvisOperatorExpression node) {
		// arbitrarily, we choose the if clause to be the type of this expression
		propertyExpression.push(node);
		node.getTrueExpression().visit(this);
		boolean shouldContinue = handleExpression(node);

		// the declaration itself is the property node
		ClassNode exprType = propertyExpressionType.pop();
		propertyExpression.pop();

		if (shouldContinue) {
			node.getFalseExpression().visit(this);

			if (isObjectExpression(node)) {
				// returns true if this declaration expression is the property field of another property expression
				objectExpressionType.push(exprType);
			}
		} else {
			propertyExpression.pop();
		}
	}

	@Override
	public void visitSpreadExpression(SpreadExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitSpreadExpression(node);
		}
	}

	@Override
	public void visitSpreadMapExpression(SpreadMapExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitSpreadMapExpression(node);
		}
	}

	@Override
	public void visitStaticMethodCallExpression(StaticMethodCallExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue && node.getEnd() > 0) {
			visitClassReference(node.getOwnerType());
			super.visitStaticMethodCallExpression(node);
		}
	}

	@Override
	public void visitTernaryExpression(TernaryExpression node) {
		node.getBooleanExpression().visit(this);

		// arbitrarily, we choose the if clause to be the type of this expression
		propertyExpression.push(node);
		node.getTrueExpression().visit(this);
		boolean shouldContinue = handleExpression(node);

		// the declaration itself is the property node
		ClassNode exprType = propertyExpressionType.pop();
		propertyExpression.pop();

		if (shouldContinue) {
			node.getFalseExpression().visit(this);

			if (isObjectExpression(node)) {
				// returns true if this declaration expression is the property field of another property expression
				objectExpressionType.push(exprType);
			}
		} else {
			propertyExpression.pop();

		}
	}

	@Override
	public void visitTupleExpression(TupleExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitTupleExpression(node);
		}
	}

	@Override
	public void visitUnaryMinusExpression(UnaryMinusExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitUnaryMinusExpression(node);
		}
	}

	@Override
	public void visitUnaryPlusExpression(UnaryPlusExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitUnaryPlusExpression(node);
		}
	}

	private void visitAnnotation(AnnotationNode node) {
		TypeLookupResult result = null;
		VariableScope scope = scopes.peek();
		for (ITypeLookup lookup : lookups) {
			TypeLookupResult candidate = lookup.lookupType(node, scope);
			if (candidate != null) {
				if (result == null || result.confidence.isLessPreciseThan(candidate.confidence)) {
					result = candidate;
				}
				if (TypeConfidence.LOOSELY_INFERRED.isLessPreciseThan(result.confidence)) {
					break;
				}
			}
		}
		VisitStatus status = handleRequestor(node, requestor, result);

		switch (status) {
			case CONTINUE:
				visitClassReference(node.getClassNode());
				if (node.getMembers() != null) {
					Collection<Expression> exprs = (Collection<Expression>) node.getMembers().values();
					for (Expression expr : exprs) {
						if (expr instanceof AnnotationConstantExpression) {
							visitClassReference(((AnnotationConstantExpression) expr).getType());
						}
						expr.visit(this);
					}
				}
				break;
			case CANCEL_BRANCH:
				return;
			case STOP_VISIT:
				throw new VisitCompleted();
		}
	}

	private VisitStatus handleRequestor(ASTNode node, ITypeRequestor requestor, TypeLookupResult result) {
		// result is never null because SimpleTypeLookup always returns non-null
		return requestor.acceptASTNode(node, result, enclosingElement);
	}

	private MethodNode findMethodNode(IMethod method) {
		ClassNode clazz = findClassWithName(createName(method.getDeclaringType()));
		try {
			if (method.isConstructor()) {
				List<ConstructorNode> constructors = clazz.getDeclaredConstructors();
				for (ConstructorNode constructorNode : constructors) {
					String[] jdtParamTypes = method.getParameterTypes() == null ? new String[0] : method.getParameterTypes();
					Parameter[] groovyParams = constructorNode.getParameters() == null ? new Parameter[0] : constructorNode
							.getParameters();
					// ignore the implicit constructor parameter of constructors for inner types
					if (groovyParams != null && groovyParams.length > 0 && groovyParams[0].getName().startsWith("$")) {
						Parameter[] newGroovyParams = new Parameter[groovyParams.length - 1];
						System.arraycopy(groovyParams, 1, newGroovyParams, 0, newGroovyParams.length);
						groovyParams = newGroovyParams;
					}
					if (groovyParams.length != jdtParamTypes.length) {
						continue;
					}
					// FIXADE this is not precise. Doesn't take into account generics
					for (int i = 0; i < groovyParams.length; i++) {
						String groovyClassType = groovyParams[i].getType().getName();
						if (!groovyClassType.startsWith("[")) { //$NON-NLS-1$
							groovyClassType = Signature.createTypeSignature(groovyClassType, false);
						}
						if (!groovyClassType.equals(jdtParamTypes[i])) {
							continue;
						}
					}
					return constructorNode;
				}
			} else {
				List<MethodNode> methods = clazz.getMethods(method.getElementName());
				for (MethodNode methodNode : methods) {
					String[] jdtParamTypes = method.getParameterTypes() == null ? new String[0] : method.getParameterTypes();
					Parameter[] groovyParams = methodNode.getParameters() == null ? new Parameter[0] : methodNode.getParameters();
					if (groovyParams.length != jdtParamTypes.length) {
						continue;
					}
					// FIXADE this is not precise. Doesn't take into account generics
					for (int i = 0; i < groovyParams.length; i++) {
						String groovyClassType = groovyParams[i].getType().getName();
						if (!groovyClassType.startsWith("[")) { //$NON-NLS-1$
							groovyClassType = Signature.createTypeSignature(groovyClassType, false);
						}
						if (!groovyClassType.equals(jdtParamTypes[i])) {
							continue;
						}
					}
					return methodNode;
				}
			}
		} catch (JavaModelException e) {
			Util.log(e, "Exception finding method " + method.getElementName() + " in class " + clazz.getName());
		}
		// probably happened due to a syntax error in the code
		// or an AST transformation
		return null;
	}

	private FieldNode findFieldNode(IField field) {
		ClassNode clazz = findClassWithName(createName(field.getDeclaringType()));
		FieldNode fieldNode = clazz.getField(field.getElementName());
		if (fieldNode == null) {
			// GRECLIPSE-578 might be @Lazy. Name is changed
			fieldNode = clazz.getField("$" + field.getElementName());
		}
		return fieldNode;
	}

	private boolean isLazy(FieldNode field) {
		List<AnnotationNode> annotations = field.getAnnotations();
		for (AnnotationNode annotation : annotations) {
			if (annotation.getClassNode().getName().equals("groovy.lang.Lazy")) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected SourceUnit getSourceUnit() {
		return null;
	}

	private ClassNode findClassWithName(String simpleName) {
		for (ClassNode clazz : (Iterable<ClassNode>) getModuleNode().getClasses()) {
			if (clazz.getNameWithoutPackage().equals(simpleName)) {
				return clazz;
			}
		}
		return null;
	}

	/**
	 * Get the module node. Potentially forces creation of a new module node if the working copy owner is non-default. This is
	 * necessary because a non-default working copy owner implies that this may be a search related to refactoring and therefore,
	 * the ModuleNode must be based on the most recent working copies.
	 */
	private ModuleNode createModuleNode(GroovyCompilationUnit unit) {
		if (unit.getOwner() == null || unit.owner == DefaultWorkingCopyOwner.PRIMARY) {
			return unit.getModuleNode();
		} else {
			return unit.getNewModuleNode();
		}
	}

	private ModuleNode getModuleNode() {
		if (enclosingDeclarationNode instanceof ModuleNode) {
			return (ModuleNode) enclosingDeclarationNode;
		} else if (enclosingDeclarationNode instanceof ClassNode) {
			return ((ClassNode) enclosingDeclarationNode).getModule();
		} else if (enclosingDeclarationNode instanceof MethodNode) {
			return ((MethodNode) enclosingDeclarationNode).getDeclaringClass().getModule();
		} else if (enclosingDeclarationNode instanceof FieldNode) {
			return ((FieldNode) enclosingDeclarationNode).getDeclaringClass().getModule();
		} else {
			throw new IllegalArgumentException("Invalid enclosing declaration node: " + enclosingDeclarationNode);
		}
	}

	private boolean isObjectExpression(Expression node) {
		if (!propertyExpression.isEmpty()) {
			Expression maybeProperty = propertyExpression.peek();
			if (maybeProperty instanceof PropertyExpression) {
				PropertyExpression prop = (PropertyExpression) maybeProperty;
				return prop.getObjectExpression() == node;
			} else if (maybeProperty instanceof MethodCallExpression) {
				MethodCallExpression prop = (MethodCallExpression) maybeProperty;
				return prop.getObjectExpression() == node;
			} else if (maybeProperty instanceof BinaryExpression) {
				BinaryExpression prop = (BinaryExpression) maybeProperty;
				return prop.getRightExpression() == node;
			} else if (maybeProperty instanceof AttributeExpression) {
				AttributeExpression prop = (AttributeExpression) maybeProperty;
				return prop.getObjectExpression() == node;
			} else if (maybeProperty instanceof TernaryExpression) {
				TernaryExpression prop = (TernaryExpression) maybeProperty;
				return prop.getTrueExpression() == node;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean isProperty(Expression node) {
		if (!propertyExpression.isEmpty()) {
			Expression maybeProperty = propertyExpression.peek();
			if (maybeProperty instanceof PropertyExpression) {
				PropertyExpression prop = (PropertyExpression) maybeProperty;
				return prop.getProperty() == node;
			} else if (maybeProperty instanceof MethodCallExpression) {
				MethodCallExpression prop = (MethodCallExpression) maybeProperty;
				return prop.getMethod() == node;
			} else if (maybeProperty instanceof BinaryExpression || maybeProperty instanceof TernaryExpression) {
				// note that here it is the binary expression itself that
				// is the property, rather than its LHS
				// this allows the type to be available during the inferencing stage
				return maybeProperty == node;
			} else if (maybeProperty instanceof AttributeExpression) {
				AttributeExpression prop = (AttributeExpression) maybeProperty;
				return prop.getProperty() == node;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
