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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
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
import org.codehaus.groovy.ast.expr.EmptyExpression;
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
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.groovy.search.ITypeRequestor.VisitStatus;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope.CallAndType;
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

		public final VisitStatus status;

		public VisitCompleted(VisitStatus status) {
			super();
			this.status = status;
		}

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
	private BinaryExpression enclosingAssignment;

	/**
	 * The head of the stack is the current property/attribute/methodcall/binary expression being visited. This stack is used so we
	 * can keep track of the type of the object expressions in these property expressions
	 */
	private Stack<ASTNode> propertyExpression;

	/**
	 * Keeps track of the type of the object expression corresponding to each frame of the property expression.
	 */
	private Stack<ClassNode> objectExpressionType;
	/**
	 * Keeps track of the type of the type of the property field corresponding to each frame of the property expression.
	 */
	private Stack<ClassNode> propertyExpressionType;

	/**
	 * Keeps track of the type of the declaring type of the property field corresponding to each frame of the property expression.
	 * Only makes sense for PropertyExpression and MethodCallExpression
	 */
	private Stack<ClassNode> propertyExpressionDeclaringType;

	/**
	 * Use factory to instantiate
	 */
	TypeInferencingVisitorWithRequestor(GroovyCompilationUnit unit, ITypeLookup[] lookups) {
		super();
		this.unit = unit;
		enclosingDeclarationNode = createModuleNode(unit);
		this.lookups = lookups;
		scopes = new Stack<VariableScope>();
		propertyExpression = new Stack<ASTNode>();
		objectExpressionType = new Stack<ClassNode>();
		propertyExpressionType = new Stack<ClassNode>();
		propertyExpressionDeclaringType = new Stack<ClassNode>();
	}

	public void visitCompilationUnit(ITypeRequestor requestor) {
		if (enclosingDeclarationNode == null) {
			// no module node, can't do anything
			return;
		}

		this.requestor = requestor;
		enclosingElement = unit;
		VariableScope topLevelScope = new VariableScope(null, enclosingDeclarationNode, false);
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
			// can ignore
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
		enclosingElement = type;
		ClassNode node = findClassWithName(createName(type));
		if (node == null) {
			// probably some sort of AST transformation is making this node invisible
			return;
		}
		try {

			scopes.push(new VariableScope(scopes.peek(), node, false));
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
				// this constructor has no JDT counterpart since it doesn't exist in the source code
				if (!type.getMethod(type.getElementName(), new String[0]).exists()) {
					ConstructorNode defConstructor = findDefaultConstructor(node);
					if (defConstructor != null) {
						visitConstructorOrMethod(defConstructor, true);
					}
				}

			} catch (JavaModelException e) {
				Util.log(e, "Error getting children of " + type.getFullyQualifiedName());
			}

		} catch (VisitCompleted vc) {
			if (vc.status == VisitStatus.STOP_VISIT) {
				throw vc;
			}
		} finally {
			enclosingElement = oldEnclosing;
			enclosingDeclarationNode = oldEnclosingNode;
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
		scopes.push(new VariableScope(scopes.peek(), fieldNode, fieldNode.isStatic()));
		try {
			visitField(fieldNode);
		} catch (VisitCompleted vc) {
			if (vc.status == VisitStatus.STOP_VISIT) {
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
				scopes.push(new VariableScope(scopes.peek(), lazyMethod, lazyMethod.isStatic()));
				try {
					visitConstructorOrMethod(lazyMethod, lazyMethod instanceof ConstructorNode);
				} catch (VisitCompleted vc) {
					if (vc.status == VisitStatus.STOP_VISIT) {
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
		scopes.push(new VariableScope(scopes.peek(), methodNode, methodNode.isStatic()));
		try {
			visitConstructorOrMethod(methodNode, method.isConstructor());
		} catch (VisitCompleted vc) {
			if (vc.status == VisitStatus.STOP_VISIT) {
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
	@SuppressWarnings("cast")
	private void visitClassInternal(ClassNode node) {
		if (unit.getResolver() != null) {
			unit.getResolver().currentClass = node;
		}
		VariableScope scope = scopes.peek();
		scope.addVariable("this", node, node);

		visitAnnotations(node);

		TypeLookupResult result = null;
		result = new TypeLookupResult(node, node, node, TypeConfidence.EXACT, scope);
		VisitStatus status = handleRequestor(node, requestor, result);
		switch (status) {
			case CONTINUE:
				break;
			case CANCEL_BRANCH:
				return;
			case CANCEL_MEMBER:
			case STOP_VISIT:
				throw new VisitCompleted(status);
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
		for (@SuppressWarnings("rawtypes")
		Iterator methodIter = node.getMethods().iterator(); methodIter.hasNext();) {
			MethodNode method = (MethodNode) methodIter.next();
			// ignore all the synthetics
			if (!method.getName().contains("$")) {
				currentScope.addVariable(method.getName(), method.getReturnType(), method.getDeclaringClass());
			}
		}

		// visit <clinit> body because this is where static field initializers are placed
		// only visit field initializers here.
		// it is important here to get the right variable scope for the initializer.
		// need to ensure that the field is one of the enclosing nodes
		MethodNode clinit = node.getMethod("<clinit>", new Parameter[0]);
		if (clinit != null && clinit.getCode() instanceof BlockStatement) {
			for (Statement element : (Iterable<Statement>) ((BlockStatement) clinit.getCode()).getStatements()) {
				// only visit the static initialization of a field
				if (element instanceof ExpressionStatement
						&& ((ExpressionStatement) element).getExpression() instanceof BinaryExpression) {
					BinaryExpression bexpr = (BinaryExpression) ((ExpressionStatement) element).getExpression();
					if (bexpr.getLeftExpression() instanceof FieldExpression) {
						FieldNode f = ((FieldExpression) bexpr.getLeftExpression()).getField();
						if (f != null && f.isStatic() && bexpr.getRightExpression() != null) {
							// create the field scope so that it looks like we are visiting within the context of the field
							VariableScope fieldScope = new VariableScope(currentScope, f, true);
							scopes.push(fieldScope);
							try {
								bexpr.getRightExpression().visit(this);
							} finally {
								scopes.pop();
							}
						}
					}
				}
			}
		}

		// I'm not actually sure that there will be anything here. I think these
		// will all be moved to a constructor
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
				visitAnnotations(node);
				// FIXADE should we be visiting the initializer here? It may already have been visited in the <clinit>. See also
				// GRECLIPSE-1008
				Expression init = node.getInitialExpression();
				if (init != null)
					init.visit(this);
			case CANCEL_BRANCH:
				return;
			case CANCEL_MEMBER:
			case STOP_VISIT:
				throw new VisitCompleted(status);
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
			case CANCEL_MEMBER:
			case STOP_VISIT:
				throw new VisitCompleted(status);
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
			case CANCEL_MEMBER:
			case STOP_VISIT:
				throw new VisitCompleted(status);
		}
	}

	@SuppressWarnings("cast")
	@Override
	public void visitAnnotations(AnnotatedNode node) {
		for (AnnotationNode annotation : (Iterable<AnnotationNode>) node.getAnnotations()) {
			visitAnnotation(annotation);
		}
		super.visitAnnotations(node);
	}

	@Override
	public void visitImports(ModuleNode node) {
		for (ImportNode imp : new ImportNodeCompatibilityWrapper(node).getAllImportNodes()) {
			TypeLookupResult result = null;
			IJavaElement oldEnclosingElement = enclosingElement;
			// FIXADE this will not work for static or * imports
			ClassNode type = imp.getType();

			if (type != null) {
				enclosingElement = unit.getImport(imp.getClassName().replace('$', '.'));
				if (!enclosingElement.exists()) {
					enclosingElement = oldEnclosingElement;
				}
			}

			try {
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

				switch (status) {
					case CONTINUE:
						try {
							if (type != null) {
								visitClassReference(type);
							}
						} catch (VisitCompleted e) {
							if (e.status == VisitStatus.STOP_VISIT) {
								throw e;
							}
						}
						continue;
					case CANCEL_BRANCH:
					case CANCEL_MEMBER:
						// assume that import statements are not interesting
						return;
					case STOP_VISIT:
						throw new VisitCompleted(status);
				}
			} finally {
				enclosingElement = oldEnclosingElement;
			}
		}
	}

	private boolean handleStatement(Statement node) {
		// don't check the lookups because statements have no type.
		// but individual requestors may choose to end the visit here
		VariableScope currentScope = scopes.peek();
		VariableInfo info = currentScope.lookupName("this");
		ClassNode declaring = info == null ? VariableScope.OBJECT_CLASS_NODE : info.declaringType;

		TypeLookupResult noLookup = new TypeLookupResult(declaring, declaring, declaring, TypeConfidence.EXACT, currentScope);
		VisitStatus status = handleRequestor(node, requestor, noLookup);
		switch (status) {
			case CONTINUE:
				return true;
			case CANCEL_BRANCH:
				return false;
			case CANCEL_MEMBER:
			case STOP_VISIT:
			default:
				throw new VisitCompleted(status);
		}

	}

	private boolean handleExpression(Expression node) {
		TypeLookupResult result = null;
		ClassNode objectExprType;
		boolean isStatic;
		VariableScope scope = scopes.peek();
		if (isProperty(node)) {
			objectExprType = objectExpressionType.pop();
			isStatic = hasStaticObjectExpression(node);
			scope.methodCallNumberOfArguments = getMethodCallArgs();
		} else {
			objectExprType = null;
			isStatic = false;
		}

		for (ITypeLookup lookup : lookups) {
			TypeLookupResult candidate;
			if (lookup instanceof ITypeLookupExtension) {
				candidate = ((ITypeLookupExtension) lookup).lookupType(node, scope, objectExprType, isStatic);
			} else {
				candidate = lookup.lookupType(node, scope, objectExprType);
			}
			if (candidate != null) {
				if (result == null || result.confidence.isLessPreciseThan(candidate.confidence)) {
					result = candidate;
				}
				if (TypeConfidence.LOOSELY_INFERRED.isLessPreciseThan(result.confidence)) {
					break;
				}
			}
		}
		result.enclosingAssignment = enclosingAssignment;
		VisitStatus status = handleRequestor(node, requestor, result);

		scope.methodCallNumberOfArguments = -1; // forget the number of arguments

		// when there is a category method, we don't want to store it
		// as the declaring type since this will mess things up inside closures
		ClassNode rememberedDeclaringType = result.declaringType;
		if (scope.getCategoryNames().contains(rememberedDeclaringType)) {
			rememberedDeclaringType = objectExprType;
		}
		if (rememberedDeclaringType == null) {
			rememberedDeclaringType = VariableScope.OBJECT_CLASS_NODE;
		}
		switch (status) {
			case CONTINUE:
				if (isObjectExpression(node)) {
					objectExpressionType.push(result.type);
				} else if (isProperty(node)) {
					propertyExpressionType.push(result.type);
					propertyExpressionDeclaringType.push(rememberedDeclaringType);
				}
				return true;
			case CANCEL_BRANCH:
				if (isObjectExpression(node)) {
					objectExpressionType.push(result.type);
				} else if (isProperty(node)) {
					propertyExpressionType.push(result.type);
					propertyExpressionDeclaringType.push(rememberedDeclaringType);
				}
				return false;
			case CANCEL_MEMBER:
			case STOP_VISIT:
				throw new VisitCompleted(status);
		}
		// won't get here
		return false;
	}

	/**
	 * Finds the number of arguments of the current method call. Returns -1 if not a method call. Returns 0 if no arguments else
	 * returns the number of arguments.
	 * 
	 * @return
	 */
	private int getMethodCallArgs() {
		ASTNode peek = propertyExpression.peek();
		if (peek instanceof MethodCallExpression) {
			MethodCallExpression call = (MethodCallExpression) peek;
			Expression arguments = call.getArguments();
			if (arguments instanceof ArgumentListExpression) {
				ArgumentListExpression list = (ArgumentListExpression) arguments;
				List<Expression> expressions = list.getExpressions();
				return expressions != null ? expressions.size() : 0;
			} else {
				return 0;
			}
		}
		return -1;
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
					case CANCEL_MEMBER:
					case STOP_VISIT:
						throw new VisitCompleted(status);
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
		scopes.peek().setCurrentNode(node);

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
		scopes.peek().forgetCurrentNode();
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

		// don't visit binary expressions in a constructor that have no source location.
		// the reason is that these were copied from the field initializer.
		// we want to visit them under the field initializer, not the construcor
		if (node.getEnd() == 0) {
			return;
		}

		// BinaryExpressions not an AnnotatedNode in groovy 1.6, but they are in 1.7+
		Object maybeAnnotatedNode = node;
		if (maybeAnnotatedNode instanceof AnnotatedNode) {
			visitAnnotations((AnnotatedNode) maybeAnnotatedNode);
		}

		// keep track of the enclosing assignment statement when visiting the RHS.
		boolean isAssignment = node.getOperation().getType() == Types.EQUALS;
		BinaryExpression oldEnclosingAssignment = enclosingAssignment;
		if (isAssignment) {
			enclosingAssignment = node;
		}

		propertyExpression.push(node);

		Expression toVisitFirst;
		Expression toVisitSecond;

		if (isAssignment) {
			toVisitFirst = node.getRightExpression();
			toVisitSecond = node.getLeftExpression();
		} else {
			toVisitFirst = node.getLeftExpression();
			toVisitSecond = node.getRightExpression();
		}

		toVisitFirst.visit(this);

		// must get this now, because this value is popped during handlExpreession.
		ClassNode objExprType = objectExpressionType.peek();

		boolean shouldContinue = handleExpression(node);

		// the declaration itself is the property node
		ClassNode propType = propertyExpressionType.pop();
		// don't care about the declaring type
		propertyExpressionDeclaringType.pop();

		if (shouldContinue) {
			toVisitSecond.visit(this);

			propertyExpression.pop();

			// returns true if this binary expression is the property part of another property expression
			if (isObjectExpression(node)) {
				// over here, we should try to find the binary expr's inferred type.
				// 1. convert operator into a method name
				// 2. convert that into a constant expression
				// 3. call handle expression on it
				// 4. then make sure that the result is pushed onto objectExpressionType
				objectExpressionType.push(findTypeOfBinaryExpression(node.getOperation().getText(), objExprType, propType));
			}
		} else {
			propertyExpression.pop();

			// not popped earlier because the method field of the expression was not examined
			objectExpressionType.pop();
		}
		// put this in a finally block?
		enclosingAssignment = oldEnclosingAssignment;
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

	// Groovy 1.8+ only
	// @Override
	public void visitEmptyExpression(EmptyExpression node) {
		handleExpression(node);
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
		VariableScope scope = new VariableScope(scopes.peek(), node, false);
		scopes.push(scope);
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			ClassNode[] implicitParamType = findImplicitParamType(scope, node);
			if (node.getParameters() != null && node.getParameters().length > 0) {
				handleParameterList(node.getParameters());

				// only set the implicit param type of the parametrers if it is not explicitly defined
				for (int i = 0; i < node.getParameters().length; i++) {
					Parameter parameter = node.getParameters()[i];
					if (implicitParamType[i] != VariableScope.OBJECT_CLASS_NODE
							&& parameter.getType().equals(VariableScope.OBJECT_CLASS_NODE)) {
						parameter.setType(implicitParamType[i]);
						scope.addVariable(parameter);
					}
				}
			} else
			// it variable only exists if there are no explicit parameters
			if (implicitParamType[0] != VariableScope.OBJECT_CLASS_NODE && !scope.containsInThisScope("it")) {
				scope.addVariable("it", implicitParamType[0], VariableScope.OBJECT_CLASS_NODE);
			}
			CallAndType cat = scope.getEnclosingMethodCallExpression();
			if (cat != null) {
				scope.addVariable("this", cat.declaringType, cat.declaringType);
				scope.addVariable("super", cat.declaringType.getUnresolvedSuperClass(), cat.declaringType.getUnresolvedSuperClass());
			}
			super.visitClosureExpression(node);
		}
		scopes.pop();
	}

	/**
	 * Determine if the parameter type can be implicitly determined We look for DGM method calls that take closures and see what
	 * kind of type they expect.
	 * 
	 * @param scope
	 * @return am array of {@link ClassNode}s specifying the inferred type of each of the closure's parameters
	 */
	private ClassNode[] findImplicitParamType(VariableScope scope, ClosureExpression closure) {
		int numParams = closure.getParameters() == null ? 0 : closure.getParameters().length;
		if (numParams == 0) {
			// implicit parameter
			numParams++;
		}
		ClassNode[] allInferred = new ClassNode[numParams];

		CallAndType call = scope.getEnclosingMethodCallExpression();
		if (call != null) {
			String methodName = call.call.getMethodAsString();
			ClassNode inferredType;

			if (dgmClosureMethods.contains(methodName)) {
				inferredType = VariableScope.extractElementType(call.declaringType);
			} else if (dgmClosureIdentityMethods.contains(methodName)) {
				inferredType = VariableScope.clone(call.declaringType);
			} else {
				// inferredType might be null
				inferredType = dgmClosureMethodsMap.get(methodName);
			}

			if (inferredType != null) {
				Arrays.fill(allInferred, inferredType);
				// special cases: eachWithIndex has last element an integer
				if (methodName.equals("eachWithIndex") && allInferred.length > 1) {
					allInferred[allInferred.length - 1] = VariableScope.INTEGER_CLASS_NODE;
				}
				// if declaring type is a map and
				if (call.declaringType.getName().equals(VariableScope.MAP_CLASS_NODE.getName())) {
					if ((dgmClosureMaybeMap.contains(methodName) && numParams == 2)
							|| (methodName.equals("eachWithIndex") && numParams == 3)) {
						GenericsType[] typeParams = inferredType.getGenericsTypes();
						if (typeParams != null && typeParams.length == 2) {
							allInferred[0] = typeParams[0].getType();
							allInferred[1] = typeParams[1].getType();
						}
					}

				}
				return allInferred;
			}
		}
		Arrays.fill(allInferred, VariableScope.OBJECT_CLASS_NODE);
		return allInferred;
	}

	/**
	 * We hard code the list of methods that take a closure and expect to iterate over that closure
	 */
	private static final Set<String> dgmClosureMethods = new HashSet<String>();
	static {
		dgmClosureMethods.add("find");
		dgmClosureMethods.add("each");
		dgmClosureMethods.add("reverseEach");
		dgmClosureMethods.add("eachWithIndex");
		dgmClosureMethods.add("unique");
		dgmClosureMethods.add("every");
		dgmClosureMethods.add("collect");
		dgmClosureMethods.add("collectEntries");
		dgmClosureMethods.add("collectNested");
		dgmClosureMethods.add("collectMany");
		dgmClosureMethods.add("findAll");
		dgmClosureMethods.add("groupBy");
		dgmClosureMethods.add("groupEntriesBy");

		dgmClosureMethods.add("inject");
		dgmClosureMethods.add("count");
		dgmClosureMethods.add("countBy");
		dgmClosureMethods.add("findResult");
		dgmClosureMethods.add("findResults");
		dgmClosureMethods.add("grep");
		dgmClosureMethods.add("split");
		dgmClosureMethods.add("sum");
		dgmClosureMethods.add("any");
		dgmClosureMethods.add("flatten");
		dgmClosureMethods.add("findIndexOf");
		dgmClosureMethods.add("findIndexValues");
		dgmClosureMethods.add("findLastIndexOf");
		dgmClosureMethods.add("collectAll");
		dgmClosureMethods.add("min");
		dgmClosureMethods.add("max");
		dgmClosureMethods.add("eachPermutation");
		dgmClosureMethods.add("sort");
		dgmClosureMethods.add("withDefault");

		// these don't take collections, but can be handled in the same way
		dgmClosureMethods.add("identity");
		dgmClosureMethods.add("times");
		dgmClosureMethods.add("upto");
		dgmClosureMethods.add("downto");
		dgmClosureMethods.add("step");
		dgmClosureMethods.add("eachFile");
		dgmClosureMethods.add("eachDir");
		dgmClosureMethods.add("eachFileRecurse");
		dgmClosureMethods.add("eachDirRecurse");
		dgmClosureMethods.add("traverse");
	}

	// These methods have a type for the closure argument that is the same as the declaring type
	private static final Set<String> dgmClosureIdentityMethods = new HashSet<String>();
	static {
		dgmClosureIdentityMethods.add("with");
		dgmClosureIdentityMethods.add("addShutdownHook");
	}

	// these methods can be called with a collection or a map.
	// When called with a map and there are 2 closure arguments, then
	// the types are the key/value of the map entry
	private static final Set<String> dgmClosureMaybeMap = new HashSet<String>();
	static {
		dgmClosureMaybeMap.add("any");
		dgmClosureMaybeMap.add("every");
		dgmClosureMaybeMap.add("each");
		dgmClosureMaybeMap.add("collect");
		dgmClosureMaybeMap.add("collectEntries");
		dgmClosureMaybeMap.add("findResult");
		dgmClosureMaybeMap.add("findResults");
		dgmClosureMaybeMap.add("findAll");
		dgmClosureMaybeMap.add("groupBy");
		dgmClosureMaybeMap.add("groupEntriesBy");
		dgmClosureMaybeMap.add("inject");
		dgmClosureMaybeMap.add("withDefault");
	}

	// These methods have a fixed type for the closure argument
	private static final Map<String, ClassNode> dgmClosureMethodsMap = new HashMap<String, ClassNode>();
	static {
		dgmClosureMethodsMap.put("eachLine", VariableScope.STRING_CLASS_NODE);
		dgmClosureMethodsMap.put("splitEachLine", VariableScope.STRING_CLASS_NODE);
		dgmClosureMethodsMap.put("withObjectOutputStream", VariableScope.OBJECT_OUTPUT_STREAM);
		dgmClosureMethodsMap.put("withObjectInputStream", VariableScope.OBJECT_INPUT_STREAM);
		dgmClosureMethodsMap.put("withDataOutputStream", VariableScope.DATA_OUTPUT_STREAM_CLASS);
		dgmClosureMethodsMap.put("withDataInputStream", VariableScope.DATA_INPUT_STREAM_CLASS);
		dgmClosureMethodsMap.put("withOutputStream", VariableScope.OUTPUT_STREAM_CLASS);
		dgmClosureMethodsMap.put("withInputStream", VariableScope.INPUT_STREAM_CLASS);
		dgmClosureMethodsMap.put("withStream", VariableScope.OUTPUT_STREAM_CLASS);
		dgmClosureMethodsMap.put("metaClass", ClassHelper.METACLASS_TYPE);
		dgmClosureMethodsMap.put("eachFileMatch", VariableScope.FILE_CLASS_NODE);
		dgmClosureMethodsMap.put("eachDirMatch", VariableScope.FILE_CLASS_NODE);
		dgmClosureMethodsMap.put("withReader", VariableScope.BUFFERED_READER_CLASS_NODE);
		dgmClosureMethodsMap.put("withWriter", VariableScope.BUFFERED_WRITER_CLASS_NODE);
		dgmClosureMethodsMap.put("withWriterAppend", VariableScope.BUFFERED_WRITER_CLASS_NODE);
		dgmClosureMethodsMap.put("withPrintWriter", VariableScope.PRINT_WRITER_CLASS_NODE);
		dgmClosureMethodsMap.put("transformChar", VariableScope.STRING_CLASS_NODE);
		dgmClosureMethodsMap.put("transformLine", VariableScope.STRING_CLASS_NODE);
		dgmClosureMethodsMap.put("filterLine", VariableScope.STRING_CLASS_NODE);
		dgmClosureMethodsMap.put("eachMatch", VariableScope.STRING_CLASS_NODE);
	}

	@Override
	public void visitBlockStatement(BlockStatement block) {
		scopes.push(new VariableScope(scopes.peek(), block, false));
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
		propertyExpression.push(node);
		node.getCollectionExpression().visit(this);
		propertyExpression.pop();

		// the type of the collection
		ClassNode collectionType = objectExpressionType.pop();

		scopes.push(new VariableScope(scopes.peek(), node, false));
		Parameter param = node.getVariable();
		if (param != null) {
			// visit the original parameter, so that requestors relying on
			// object equality will work
			handleParameterList(new Parameter[] { param });

			// now update the type of the parameter with the collection type
			if (param.getType().equals(VariableScope.OBJECT_CLASS_NODE)) {
				ClassNode extractedElementType = VariableScope.extractElementType(collectionType);
				scopes.peek().addVariable(param.getName(), extractedElementType, null);
			}
		}

		node.getLoopBlock().visit(this);

		scopes.pop();
	}

	@Override
	public void visitCatchStatement(CatchStatement node) {
		scopes.push(new VariableScope(scopes.peek(), node, false));
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
		scopes.peek().setCurrentNode(node);
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitConstantExpression(node);
		}
		scopes.peek().forgetCurrentNode();
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
		scopes.peek().setCurrentNode(node);
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitGStringExpression(node);
		}
		scopes.peek().forgetCurrentNode();
	}

	@Override
	public void visitListExpression(ListExpression node) {
		scopes.peek().setCurrentNode(node);
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitListExpression(node);
		}
		scopes.peek().forgetCurrentNode();
	}

	@Override
	public void visitMapEntryExpression(MapEntryExpression node) {
		scopes.peek().setCurrentNode(node);
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitMapEntryExpression(node);
		}
		scopes.peek().forgetCurrentNode();
	}

	@Override
	public void visitMapExpression(MapExpression node) {
		scopes.peek().setCurrentNode(node);
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitMapExpression(node);
		}
		scopes.peek().forgetCurrentNode();
	}

	@Override
	public void visitMethodCallExpression(MethodCallExpression node) {
		scopes.peek().setCurrentNode(node);
		propertyExpression.push(node);
		node.getObjectExpression().visit(this);

		boolean shouldContinue = handleExpression(node);
		// boolean shouldContinue = true;
		if (shouldContinue) {
			if (node.isSpreadSafe()) {
				// most find the component type of the object expression type
				ClassNode objType = objectExpressionType.pop();
				objectExpressionType.push(VariableScope.extractElementType(objType));
			}

			node.getMethod().visit(this);
			// this is the inferred return type of this method
			// must pop now before visiting any other nodes
			ClassNode propType = propertyExpressionType.pop();

			// this is the inferred declaring type of this method
			ClassNode propDeclaringType = propertyExpressionDeclaringType.pop();
			CallAndType call = new CallAndType(node, propDeclaringType);

			// don't care about this
			propertyExpression.pop();

			ClassNode catNode = isCategoryDeclaration(node);
			if (catNode != null) {
				addCategoryToBeDeclared(catNode);
			}
			VariableScope scope = scopes.peek();

			// remember that we are inside a method call while analyzing the arguments
			scope.addEnclosingMethodCall(call);
			node.getArguments().visit(this);

			scope.forgetEnclosingMethodCall();

			// returns true if this method call expression is the property field of another property expression
			if (isObjectExpression(node)) {
				if (node.isSpreadSafe()) {
					ClassNode list = VariableScope.clonedList();
					list.getGenericsTypes()[0].setType(propType);
					list.getGenericsTypes()[0].setName(propType.getName());
					propType = list;
				}
				objectExpressionType.push(propType);
			}
		} else {
			propertyExpression.pop();

			// not popped earlier because the method field of the expression was not examined
			objectExpressionType.pop();
		}
		scopes.peek().forgetCurrentNode();
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
		scopes.peek().setCurrentNode(node);
		propertyExpression.push(node);
		node.getObjectExpression().visit(this);
		boolean shouldContinue;
		// don't visit the property expression itself if
		// it is a synthetic PropertyExpression (eg- synthetic 'this.'
		// or synthetic access to statically imported field or method
		if (node.getObjectExpression().getLength() > 0) {
			shouldContinue = handleExpression(node);
		} else {
			shouldContinue = true;
		}
		if (shouldContinue) {
			if (node.isSpreadSafe()) {
				// most find the component type of the object expression type
				ClassNode objType = objectExpressionType.pop();
				objectExpressionType.push(VariableScope.extractElementType(objType));
			}

			node.getProperty().visit(this);

			// don't care about either of these
			propertyExpressionDeclaringType.pop();
			propertyExpression.pop();

			// this is the type of this property expression
			ClassNode propType = propertyExpressionType.pop();
			if (isObjectExpression(node)) {
				if (node.isSpreadSafe()) {
					ClassNode list = VariableScope.clonedList();
					list.getGenericsTypes()[0].setType(propType);
					list.getGenericsTypes()[0].setName(propType.getName());
					propType = list;
				}
				// returns true if this property expression is the property field of another property expression
				objectExpressionType.push(propType);
			}

		} else {
			propertyExpression.pop();

			// not popped earlier because the property field of the expression was not examined
			objectExpressionType.pop();
		}
		scopes.peek().forgetCurrentNode();
	}

	@Override
	public void visitRangeExpression(RangeExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitRangeExpression(node);
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
		propertyExpressionDeclaringType.pop();
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

		// don't care about these
		propertyExpressionDeclaringType.pop();
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
					@SuppressWarnings("cast")
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
			case CANCEL_MEMBER:
			case STOP_VISIT:
				throw new VisitCompleted(status);
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
				if (constructors.size() == 0) {
					return null;
				}
				outer: for (ConstructorNode constructorNode : constructors) {
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
					for (int i = 0; i < groovyParams.length; i++) {
						String groovyClassType = groovyParams[i].getType().getName();
						if (!groovyClassType.startsWith("[")) { //$NON-NLS-1$
							groovyClassType = Signature.createTypeSignature(groovyClassType, false);
						}
						if (!groovyClassType.equals(jdtParamTypes[i])) {
							continue outer;
						}
					}
					return constructorNode;
				}
				// no match found, just return the first
				return constructors.get(0);
			} else {
				List<MethodNode> methods = clazz.getMethods(method.getElementName());
				if (methods.size() == 0) {
					return null;
				}

				outer: for (MethodNode methodNode : methods) {
					String[] jdtParamTypes = method.getParameterTypes() == null ? new String[0] : method.getParameterTypes();
					Parameter[] groovyParams = methodNode.getParameters() == null ? new Parameter[0] : methodNode.getParameters();
					if (groovyParams.length != jdtParamTypes.length) {
						continue;
					}
					inner: for (int i = 0; i < groovyParams.length; i++) {
						String simpleGroovyClassType = groovyParams[i].getType().getNameWithoutPackage();
						if (!simpleGroovyClassType.startsWith("[")) { //$NON-NLS-1$
							simpleGroovyClassType = Signature.createTypeSignature(simpleGroovyClassType, false);
						}
						if (simpleGroovyClassType.equals(jdtParamTypes[i])) {
							continue inner;
						}

						String groovyClassType = groovyParams[i].getType().getName();
						if (!groovyClassType.startsWith("[")) { //$NON-NLS-1$
							groovyClassType = Signature.createTypeSignature(groovyClassType, false);
						}
						if (!groovyClassType.equals(jdtParamTypes[i])) {
							continue outer;
						}
					}
					return methodNode;
				}

				// no match found, just return the first
				return methods.get(0);
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

	/**
	 * Since AST transforms are turned off for reconcile operations, this method will always return null. But keep it here just in
	 * case we decide to re-enable transforms for reconciles.
	 * 
	 * @param field
	 * @return
	 */
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

	@SuppressWarnings("cast")
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
			ASTNode maybeProperty = propertyExpression.peek();
			if (maybeProperty instanceof PropertyExpression) {
				PropertyExpression prop = (PropertyExpression) maybeProperty;
				return prop.getObjectExpression() == node;
			} else if (maybeProperty instanceof MethodCallExpression) {
				MethodCallExpression prop = (MethodCallExpression) maybeProperty;
				return prop.getObjectExpression() == node;
			} else if (maybeProperty instanceof BinaryExpression) {
				BinaryExpression prop = (BinaryExpression) maybeProperty;
				// for assignments, the right expression is property
				// for others, the left
				boolean isAssignment = prop.getOperation().getText().equals("=");
				if (isAssignment) {
					return prop.getRightExpression() == node;
				} else {
					return prop.getLeftExpression() == node;
				}
			} else if (maybeProperty instanceof AttributeExpression) {
				AttributeExpression prop = (AttributeExpression) maybeProperty;
				return prop.getObjectExpression() == node;
			} else if (maybeProperty instanceof TernaryExpression) {
				TernaryExpression prop = (TernaryExpression) maybeProperty;
				return prop.getTrueExpression() == node;
			} else if (maybeProperty instanceof ForStatement) {
				// this check is used to store the type of the collection expression so that it can be assigned to the for loop
				// variable
				ForStatement prop = (ForStatement) maybeProperty;
				return prop.getCollectionExpression() == node;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean isProperty(Expression node) {
		if (!propertyExpression.isEmpty()) {
			ASTNode maybeProperty = propertyExpression.peek();
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

	/**
	 * 
	 * @param node
	 * @return true iff the object expression associated with node is a static reference to a class declaration
	 */
	private boolean hasStaticObjectExpression(Expression node) {
		if (!propertyExpression.isEmpty()) {
			ASTNode maybeProperty = propertyExpression.peek();
			if (maybeProperty instanceof PropertyExpression) {
				PropertyExpression prop = (PropertyExpression) maybeProperty;
				return prop.getObjectExpression() instanceof ClassExpression ||
				// check to see if in a static scope
						(prop.isImplicitThis() && scopes.peek().isStatic());
			} else if (maybeProperty instanceof MethodCallExpression) {
				MethodCallExpression prop = (MethodCallExpression) maybeProperty;
				return prop.getObjectExpression() instanceof ClassExpression ||
				// check to see if in a static scope
						(prop.isImplicitThis() && scopes.peek().isStatic());
			} else if (maybeProperty instanceof AttributeExpression) {
				AttributeExpression prop = (AttributeExpression) maybeProperty;
				return prop.getObjectExpression() instanceof ClassExpression ||
				// check to see if in a static scope
						(prop.isImplicitThis() && scopes.peek().isStatic());
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * @param operation the operation of this binary expression
	 * @param lhs the type of the lhs of the binary expression
	 * @param rhs the type of the rhs of the binary expression
	 * @return the determined type of the binary expression
	 */
	private ClassNode findTypeOfBinaryExpression(String operation, ClassNode lhs, ClassNode rhs) {
		char op = operation.charAt(0);
		switch (op) {

			case '[':
				// GRECLIPSE-742: does the LHS type have a 'getAt' method?
				List<MethodNode> getAts = lhs.getMethods("getAt");
				for (MethodNode getAt : getAts) {
					if (getAt.getParameters() != null && getAt.getParameters().length == 1) {
						return getAt.getReturnType();
					}
				}

				GenericsType[] lhsGenericsTypes = lhs.getGenericsTypes();
				if (VariableScope.MAP_CLASS_NODE.equals(lhs) && lhsGenericsTypes != null && lhsGenericsTypes.length == 2) {
					// for maps, always use the type of value
					return lhsGenericsTypes[1].getType();
				}

				// deref...get component type of lhs
				return VariableScope.extractElementType(lhs);
			case '*':
				if (operation.equals("*.") || operation.equals("*.@")) {
					// can we do better and parameterize the list?
					return VariableScope.clonedList();
				}
				// fall through
			case '-':
			case '/':
			case '%':
				// arithmetic operation
				// lhs, if number type, else rhs if number type, else number
				return isNumeric(lhs) ? lhs : (isNumeric(rhs) ? rhs : VariableScope.NUMBER_CLASS_NODE);

			case '+':
				// lhs
				return lhs;

			case '~':
				// regex pattern
				return VariableScope.STRING_CLASS_NODE;

			case '!':
				// includes != and !== and !!
			case '<':
			case '>':
				if (operation.length() > 1) {
					if (operation.equals("<<")) {
						// list of rhs type
						ClassNode listType = VariableScope.clone(VariableScope.LIST_CLASS_NODE);
						listType.getGenericsTypes()[0].setType(rhs);
						listType.getGenericsTypes()[0].setName(rhs.getName());
						return listType;
					} else if (operation.equals("<=>")) {
						return VariableScope.INTEGER_CLASS_NODE;
					}
				}
				// all booleans
				return VariableScope.BOOLEAN_CLASS_NODE;

			case '&':
			case '^':
			case '|':
				// bitwse operations, return lhs
				return lhs;

			case 'i':
				if (operation.equals("is") || operation.equals("in")) {
					return VariableScope.BOOLEAN_CLASS_NODE;
				} else {
					// unknown
					return rhs;
				}

			case '.':
				if (operation.equals(".&")) {
					return ClassHelper.CLOSURE_TYPE;
				} else {
					// includes ".", "?:", "?.", ".@"
					return rhs;
				}

			case '=':
				if (operation.length() > 1) {
					if (operation.charAt(1) == '=') {
						return VariableScope.BOOLEAN_CLASS_NODE;
					} else if (operation.charAt(1) == '~') {
						// consider regex to be string
						return VariableScope.MATCHER_CLASS_NODE;
					}
				}
				// drop through

			default:
				// "as"
				// rhs by default
				return rhs;
		}
	}

	private boolean isNumeric(ClassNode c) {
		if (c == null || c.isInterface() || c == VariableScope.OBJECT_CLASS_NODE) {
			return false;
		}
		if (c.equals(VariableScope.NUMBER_CLASS_NODE)) {
			return true;
		} else {
			return isNumeric(c.getSuperClass());
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
					@SuppressWarnings("cast")
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

}
