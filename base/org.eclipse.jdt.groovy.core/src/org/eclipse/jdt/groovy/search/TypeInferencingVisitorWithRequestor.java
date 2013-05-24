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
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.ITypeRequestor.VisitStatus;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope.CallAndType;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.SourceType;
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

	class Tuple {
		ClassNode declaringType;
		ASTNode declaration;

		Tuple(ClassNode declaringType, ASTNode declaration) {
			this.declaringType = declaringType;
			this.declaration = declaration;
		}
	}

	/**
	 * Set to true if debug mode is desired. Any exceptions will be spit to syserr. Also, after a visit, there will be a sanity
	 * check to ensure that all stacks are empty Only set to true if using a visitor that always visits the entire file
	 */
	public boolean DEBUG = false;

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
	private ConstructorCallExpression enclosingConstructorCall;

	/**
	 * The head of the stack is the current property/attribute/methodcall/binary expression being visited. This stack is used so we
	 * can keep track of the type of the object expressions in these property expressions
	 */
	private Stack<ASTNode> completeExpressionStack;

	/**
	 * Keeps track of the type of the object expression corresponding to each frame of the property expression.
	 */
	private Stack<ClassNode> primaryTypeStack;

	/**
	 * Keeps track of the declaring type of the current dependent expression. Dependent expressions are dependent on a primary
	 * expression to find type information. this field is only applicable for {@link PropertyExpression}s and
	 * {@link MethodCallExpression}s.
	 */
	private Stack<Tuple> dependentDeclarationStack;

	/**
	 * Keeps track of the type of the type of the property field corresponding to each frame of the property expression.
	 */
	private Stack<ClassNode> dependentTypeStack;

	private final JDTResolver resolver;

	private final AssignmentStorer assignmentStorer = new AssignmentStorer();

	/**
	 * Use factory to instantiate
	 */
	TypeInferencingVisitorWithRequestor(GroovyCompilationUnit unit, ITypeLookup[] lookups) {
		super();
		this.unit = unit;
		ModuleNodeInfo info = createModuleNode(unit);
		this.enclosingDeclarationNode = info != null ? info.module : null;
		this.resolver = info != null ? info.resolver : null;
		this.lookups = lookups;
		scopes = new Stack<VariableScope>();
		completeExpressionStack = new Stack<ASTNode>();
		primaryTypeStack = new Stack<ClassNode>();
		dependentTypeStack = new Stack<ClassNode>();
		dependentDeclarationStack = new Stack<Tuple>();
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
			if (lookup instanceof ITypeResolver) {
				((ITypeResolver) lookup).setResolverInformation((ModuleNode) enclosingDeclarationNode, resolver);
			}
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
			Util.log(e, "Error in inferencing engine for " + unit.getElementName());
			if (DEBUG) {
				System.err.println("Excpetion thrown from inferencing engine");
				e.printStackTrace();
			}
		}
		if (DEBUG) {
			postVisitSanityCheck();
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
		while (type != null) {
			if (sb.length() > 0) {
				sb.insert(0, '$');
			}
			if (type instanceof SourceType && type.getElementName().isEmpty()) {
				int count;
				try {
					count = (Integer) ReflectionUtils.throwableGetPrivateField(SourceType.class, "localOccurrenceCount",
							(SourceType) type);
				} catch (Exception e) {
					// localOccurrenceCount does not exist in 3.7
					count = type.getOccurrenceCount();
				}
				sb.insert(0, count);
			} else {
				sb.insert(0, type.getElementName());
			}
			type = (IType) type.getParent().getAncestor(IJavaElement.TYPE);
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

			// check for anonymous inner types
			IJavaElement[] children = method.getChildren();
			for (IJavaElement child : children) {
				if (child.getElementType() == IJavaElement.TYPE) {
					visitJDT((IType) child, requestor);
				}
			}
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
		if (resolver != null) {
			resolver.currentClass = node;
		}
		VariableScope scope = scopes.peek();
		scope.addVariable("this", node, node);

		visitAnnotations(node);

		TypeLookupResult result = null;
		result = new TypeLookupResult(node, node, node, TypeConfidence.EXACT, scope);
		VisitStatus status = notifyRequestor(node, requestor, result);
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

		VariableScope currentScope = scope;
		// don't think I need to do this...
		// // add all methods to the scope because when they are
		// // referenced without parens, they appear
		// // as VariableExpressions in the code
		// // don't use Java 5 style for loop here because Groovy 1.6.x does not
		// // have type parameters for its getMethods() method.
		// for (@SuppressWarnings("rawtypes")
		// Iterator methodIter = node.getMethods().iterator(); methodIter.hasNext();) {
		// MethodNode method = (MethodNode) methodIter.next();
		// // ignore all the synthetics
		// if (!method.getName().contains("$")) {
		// currentScope.addVariable(method.getName(), method.getReturnType(), method.getDeclaringClass());
		// }
		// }

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
		assignmentStorer.storeField(node, scope);
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
		scope.setPrimaryNode(false);

		VisitStatus status = notifyRequestor(node, requestor, result);
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
				Expression init = node.getInitialExpression();
				if (init != null) {
					init.visit(this);
				}
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
		scope.setPrimaryNode(false);

		VisitStatus status = notifyRequestor(node, requestor, result);
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
		scope.setPrimaryNode(false);
		VisitStatus status = notifyRequestor(node, requestor, result);

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

			visitAnnotations(imp);

			// this will not work for static or * imports, but that's OK because
			// as of now, there is no reason to do that.
			ClassNode type = imp.getType();

			if (type != null) {
				String importName = imp.getClassName().replace('$', '.')
						+ (imp.getFieldName() != null ? "." + imp.getFieldName() : "");
				enclosingElement = unit.getImport(importName);
				if (!enclosingElement.exists()) {
					enclosingElement = oldEnclosingElement;
				}
			}

			try {
				VariableScope scope = scopes.peek();
				scope.setPrimaryNode(false);
				assignmentStorer.storeImport(imp, scope);
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
				VisitStatus status = notifyRequestor(imp, requestor, result);

				switch (status) {
					case CONTINUE:
						try {
							if (type != null) {
								visitClassReference(type);
								// FIXADE this is a bit messy, shoud use existing infra to push and pop
								completeExpressionStack.push(imp);
								if (imp.getFieldNameExpr() != null) {
									primaryTypeStack.push(type);
									imp.getFieldNameExpr().visit(this);
									dependentDeclarationStack.pop();
									dependentTypeStack.pop();
								}

								// if (imp.getAliasExpr() != null) {
								// primaryTypeStack.push(type);
								// imp.getAliasExpr().visit(this);
								// }
								completeExpressionStack.pop();
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

	@Override
	public void visitVariableExpression(VariableExpression node) {
		scopes.peek().setCurrentNode(node);
		visitAnnotations(node);
		if (node.getAccessedVariable() == node) {
			// this is a declaration
			visitClassReference(node.getType());
		}
		handleSimpleExpression(node);
		scopes.peek().forgetCurrentNode();
	}

	@Override
	public void visitArgumentlistExpression(ArgumentListExpression node) {
		visitTupleExpression(node);
	}

	@Override
	public void visitArrayExpression(ArrayExpression node) {
		boolean shouldContinue = handleSimpleExpression(node);
		if (shouldContinue) {
			super.visitArrayExpression(node);
		}
	}

	@Override
	public void visitAttributeExpression(AttributeExpression node) {
		visitPropertyExpression(node);
	}

	@Override
	public void visitBinaryExpression(BinaryExpression node) {
		if (isDependentExpression(node)) {
			primaryTypeStack.pop();
		}

		// don't visit binary expressions in a constructor that have no source location.
		// the reason is that these were copied from the field initializer.
		// we want to visit them under the field initializer, not the construcor
		if (node.getEnd() == 0) {
			return;
		}

		visitAnnotations(node);

		boolean isAssignment = node.getOperation().getType() == Types.EQUALS;
		BinaryExpression oldEnclosingAssignment = enclosingAssignment;
		if (isAssignment) {
			enclosingAssignment = node;
		}

		completeExpressionStack.push(node);

		// visit order is dependent on whether or not assignment statement
		Expression toVisitPrimary;
		Expression toVisitDependent;
		if (isAssignment) {
			toVisitPrimary = node.getRightExpression();
			toVisitDependent = node.getLeftExpression();
		} else {
			toVisitPrimary = node.getLeftExpression();
			toVisitDependent = node.getRightExpression();
		}

		toVisitPrimary.visit(this);

		ClassNode primaryExprType;

		primaryExprType = primaryTypeStack.pop();
		if (isAssignment) {
			assignmentStorer.storeAssignment(node, scopes.peek(), primaryExprType);
		}

		toVisitDependent.visit(this);

		completeExpressionStack.pop();
		// type of the entire expression
		ClassNode completeExprType = primaryExprType;

		ClassNode dependentExprType = primaryTypeStack.pop();

		if (!isAssignment) {
			// type of RHS of binary expression
			// find the type of the complete expression
			String associatedMethod = findBinaryOperatorName(node.getOperation().getText());
			if (isArithmeticOperationOnNumberOrStringOrList(node.getOperation().getText(), primaryExprType, dependentExprType)) {
				// another special case.
				// In 1.8 and later, Groovy will not go through the
				// MOP for standard arithmetic operations on numbers
				completeExprType = dependentExprType.equals(VariableScope.STRING_CLASS_NODE) ? VariableScope.STRING_CLASS_NODE
						: primaryExprType;
			} else if (associatedMethod != null) {
				// there is an overloadable method associated with this operation
				// convert to a constant expression and infer type
				TypeLookupResult result = lookupExpressionType(new ConstantExpression(associatedMethod), primaryExprType, false,
						scopes.peek());
				completeExprType = result.type;
				if (associatedMethod.equals("getAt") && result.declaringType.equals(VariableScope.DGM_CLASS_NODE)) {
					// special case getAt coming from DGM.
					// problem is that DGM has too many overloaded variants of getAt.
					// do better by looking at the rhs.
					if (primaryExprType.getName().equals("java.util.BitSet")) {
						completeExprType = VariableScope.BOOLEAN_CLASS_NODE;
					} else {
						GenericsType[] lhsGenericsTypes = primaryExprType.getGenericsTypes();
						ClassNode elementType;
						if (VariableScope.MAP_CLASS_NODE.equals(primaryExprType) && lhsGenericsTypes != null
								&& lhsGenericsTypes.length == 2) {
							// for maps, always use the type of value
							elementType = lhsGenericsTypes[1].getType();
						} else {
							// deref...get component type of lhs
							elementType = VariableScope.extractElementType(primaryExprType);
						}
						// if rhs is a range or list type, then result is a list parameterized by lhs type
						if (dependentExprType.isArray()
								|| dependentExprType.getName().equals(VariableScope.LIST_CLASS_NODE.getName())
								|| dependentExprType.implementsInterface(VariableScope.LIST_CLASS_NODE)) {
							completeExprType = createParameterizedList(elementType);
						} else {
							completeExprType = elementType;
						}
					}
				}
			} else {
				// no overloadable associated method
				completeExprType = findTypeOfBinaryExpression(node.getOperation().getText(), primaryExprType, dependentExprType);
			}
		}
		handleCompleteExpression(node, completeExprType, null);
		enclosingAssignment = oldEnclosingAssignment;
	}

	/**
	 * Make assumption that no one has overloaded the basic arithmetic operations on numbers These operations will bypass the mop in
	 * most situations anyway
	 * 
	 * @param text
	 * @param primaryExprType
	 * @param dependentExprType
	 * @return
	 */
	private boolean isArithmeticOperationOnNumberOrStringOrList(String text, ClassNode lhs, ClassNode rhs) {
		if (text.length() != 1) {
			return false;
		}

		switch (text.charAt(0)) {
			case '+':
			case '-':
				// lists, numbers or string
				return VariableScope.STRING_CLASS_NODE.equals(lhs) || lhs.isDerivedFrom(VariableScope.NUMBER_CLASS_NODE)
						|| VariableScope.NUMBER_CLASS_NODE.equals(lhs) || VariableScope.LIST_CLASS_NODE.equals(lhs)
						|| lhs.implementsInterface(VariableScope.LIST_CLASS_NODE);
			case '*':
			case '/':
			case '%':
				// numbers or string
				return VariableScope.STRING_CLASS_NODE.equals(lhs) || lhs.isDerivedFrom(VariableScope.NUMBER_CLASS_NODE)
						|| VariableScope.NUMBER_CLASS_NODE.equals(lhs);
			default:
				return false;
		}
	}

	/**
	 * @param text
	 * @return the method name associated with this binary operator
	 */
	private String findBinaryOperatorName(String text) {
		char op = text.charAt(0);
		switch (op) {
			case '+':
				return "plus";
			case '-':
				return "minus";
			case '*':
				if (text.length() > 1 && text.equals("**")) {
					return "power";
				}
				return "multiply";
			case '/':
				return "div";
			case '%':
				return "mod";
			case '&':
				return "and";
			case '|':
				return "or";
			case '^':
				return "xor";
			case '>':
				if (text.length() > 1 && text.equals(">>")) {
					return "rightShift";
				}
				break;
			case '<':
				if (text.length() > 1 && text.equals("<<")) {
					return "leftShift";
				}
				break;
			case '[':
				return "getAt";
		}
		return null;
	}

	/**
	 * Not used yet, but could be used for PostFix and PreFix operators
	 * 
	 * @param text
	 * @return the method name associated with this unary operator
	 */
	private String findUnaryOperatorName(String text) {
		char op = text.charAt(0);
		switch (op) {
			case '+':
				if (text.length() > 1 && text.equals("++")) {
					return "next";
				}
				return "positive";
			case '-':
				if (text.length() > 1 && text.equals("--")) {
					return "previous";
				}
				return "negative";
			case ']':
				return "putAt";
			case '~':
				return "bitwiseNegate";
		}
		return null;
	}

	@Override
	public void visitBitwiseNegationExpression(BitwiseNegationExpression node) {
		visitUnaryExpression(node, node.getExpression(), "~");
	}

	@Override
	public void visitBooleanExpression(BooleanExpression node) {
		boolean shouldContinue = handleSimpleExpression(node);
		if (shouldContinue) {
			super.visitBooleanExpression(node);
		}
	}

	// Groovy 1.8+ only
	// @Override
	public void visitEmptyExpression(EmptyExpression node) {
		handleSimpleExpression(node);
	}

	@Override
	public void visitBytecodeExpression(BytecodeExpression node) {
		boolean shouldContinue = handleSimpleExpression(node);
		if (shouldContinue) {
			super.visitBytecodeExpression(node);
		}
	}

	@Override
	public void visitCastExpression(CastExpression node) {
		boolean shouldContinue = handleSimpleExpression(node);
		if (shouldContinue) {
			visitClassReference(node.getType());
			super.visitCastExpression(node);
		}
	}

	@Override
	public void visitClassExpression(ClassExpression node) {
		boolean shouldContinue = handleSimpleExpression(node);
		if (shouldContinue) {
			super.visitClassExpression(node);
		}
	}

	@Override
	public void visitClosureExpression(ClosureExpression node) {
		VariableScope parent = scopes.peek();
		ClosureExpression enclosingClosure = parent.getEnclosingClosure();

		VariableScope scope = new VariableScope(parent, node, false);
		scopes.push(scope);
		boolean shouldContinue = handleSimpleExpression(node);
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

			// Delegate is the declaring type of the enclosing call if one exists, or it is 'this'
			CallAndType cat = scope.getEnclosingMethodCallExpression();
			if (cat != null) {
				ClassNode declaringType = cat.declaringType;
				if (cat.delegatesToClosures.containsKey(node)) {
					declaringType = cat.delegatesToClosures.get(node);
				}
				scope.addVariable("delegate", declaringType, VariableScope.CLOSURE_CLASS);
				scope.addVariable("getDelegate", declaringType, VariableScope.CLOSURE_CLASS);
			} else {
				ClassNode thisType = scope.getThis();
				// GRECLIPSE-1348 someone is silly enough to have a variable named "owner".
				// don't override that
				if (scope.lookupName("delegate") == null) {
					scope.addVariable("delegate", thisType, VariableScope.CLOSURE_CLASS);
				}
				scope.addVariable("getDelegate", thisType, VariableScope.CLOSURE_CLASS);
			}

			// Owner is 'this' if no enclosing closure, or 'Closure' if there is
			if (enclosingClosure != null) {
				scope.addVariable("getOwner", VariableScope.CLOSURE_CLASS, VariableScope.CLOSURE_CLASS);
				scope.addVariable("owner", VariableScope.CLOSURE_CLASS, VariableScope.CLOSURE_CLASS);
			} else {
				ClassNode thisType = scope.getThis();
				// GRECLIPSE-1348 someone is silly enough to have a variable named "owner".
				// don't override that
				if (scope.lookupName("owner") == null) {
					scope.addVariable("owner", thisType, VariableScope.CLOSURE_CLASS);
				}
				scope.addVariable("getOwner", thisType, VariableScope.CLOSURE_CLASS);

				// only do this if we are not already in a closure
				// no need to add twice
				scope.addVariable("thisObject", VariableScope.OBJECT_CLASS_NODE, VariableScope.CLOSURE_CLASS);
				scope.addVariable("getThisObject", VariableScope.OBJECT_CLASS_NODE, VariableScope.CLOSURE_CLASS);
				scope.addVariable("resolveStategy", VariableScope.INTEGER_CLASS_NODE, VariableScope.CLOSURE_CLASS);
				scope.addVariable("getResolveStategy", VariableScope.INTEGER_CLASS_NODE, VariableScope.CLOSURE_CLASS);
				scope.addVariable("directive", VariableScope.INTEGER_CLASS_NODE, VariableScope.CLOSURE_CLASS);
				scope.addVariable("getDirective", VariableScope.INTEGER_CLASS_NODE, VariableScope.CLOSURE_CLASS);
				scope.addVariable("maximumNumberOfParameters", VariableScope.INTEGER_CLASS_NODE, VariableScope.CLOSURE_CLASS);
				scope.addVariable("getMaximumNumberOfParameters", VariableScope.INTEGER_CLASS_NODE, VariableScope.CLOSURE_CLASS);
				scope.addVariable("parameterTypes", VariableScope.CLASS_ARRAY_CLASS_NODE, VariableScope.CLOSURE_CLASS);
				scope.addVariable("getParameterTypes", VariableScope.CLASS_ARRAY_CLASS_NODE, VariableScope.CLOSURE_CLASS);
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

			ClassNode delegateType = call.declaringType;
			if (dgmClosureMethods.contains(methodName)) {
				inferredType = VariableScope.extractElementType(delegateType);
			} else if (dgmClosureIdentityMethods.contains(methodName)) {
				inferredType = VariableScope.clone(delegateType);
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
				if (delegateType.getName().equals(VariableScope.MAP_CLASS_NODE.getName())) {
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
		completeExpressionStack.push(node);
		node.getCollectionExpression().visit(this);
		completeExpressionStack.pop();

		// the type of the collection
		ClassNode collectionType = primaryTypeStack.pop();

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
		boolean shouldContinue = handleSimpleExpression(node);
		if (shouldContinue) {
			super.visitClosureListExpression(node);
		}
	}

	@Override
	public void visitConstantExpression(ConstantExpression node) {
		scopes.peek().setCurrentNode(node);
		handleSimpleExpression(node);
		scopes.peek().forgetCurrentNode();
	}

	@Override
	public void visitConstructorCallExpression(ConstructorCallExpression node) {
		boolean shouldContinue = handleSimpleExpression(node);
		if (shouldContinue) {
			visitClassReference(node.getType());
			if (node.getArguments() instanceof TupleExpression
					&& ((TupleExpression) node.getArguments()).getExpressions().size() == 1) {
				Expression arg = ((TupleExpression) node.getArguments()).getExpressions().get(0);
				if (arg instanceof MapExpression) {
					// this is a constructor call that is instantiated by a map.
					// remember this, so that when visiting the map, we can
					// infer field names
					enclosingConstructorCall = node;
				}
			}
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
		boolean shouldContinue = handleSimpleExpression(node);
		if (shouldContinue) {
			super.visitFieldExpression(node);
		}
	}

	@Override
	public void visitGStringExpression(GStringExpression node) {
		scopes.peek().setCurrentNode(node);
		boolean shouldContinue = handleSimpleExpression(node);
		if (shouldContinue) {
			super.visitGStringExpression(node);
		}
		scopes.peek().forgetCurrentNode();
	}

	@Override
	public void visitListExpression(ListExpression node) {
		if (isDependentExpression(node)) {
			primaryTypeStack.pop();
		}
		scopes.peek().setCurrentNode(node);
		completeExpressionStack.push(node);
		super.visitListExpression(node);
		ClassNode eltType;
		if (node.getExpressions().size() > 0) {
			eltType = primaryTypeStack.pop();
		} else {
			eltType = VariableScope.OBJECT_CLASS_NODE;
		}
		completeExpressionStack.pop();
		ClassNode exprType = createParameterizedList(eltType);
		handleCompleteExpression(node, exprType, null);
		scopes.peek().forgetCurrentNode();
	}

	@Override
	public void visitMapEntryExpression(MapEntryExpression node) {
		if (isDependentExpression(node)) {
			primaryTypeStack.pop();
		}
		scopes.peek().setCurrentNode(node);
		completeExpressionStack.push(node);
		node.getKeyExpression().visit(this);

		// use the types of the key and value expressions
		ClassNode k = primaryTypeStack.pop();
		node.getValueExpression().visit(this);
		ClassNode v = primaryTypeStack.pop();
		completeExpressionStack.pop();
		// really, we don't need to do this if not the first entry of a map literal
		ClassNode exprType;
		if (isPrimaryExpression(node)) {
			exprType = createParameterizedMap(k, v);
		} else {
			exprType = VariableScope.OBJECT_CLASS_NODE;
		}
		handleCompleteExpression(node, exprType, null);
		scopes.peek().forgetCurrentNode();
	}

	@Override
	public void visitMapExpression(MapExpression node) {
		if (isDependentExpression(node)) {
			primaryTypeStack.pop();
		}
		ClassNode newType;
		if (enclosingConstructorCall != null) {
			newType = enclosingConstructorCall.getType();
			enclosingConstructorCall = null;
		} else {
			newType = null;
		}

		scopes.peek().setCurrentNode(node);
		completeExpressionStack.push(node);
		if (newType == null) {
			// do a regular visit
			for (MapEntryExpression entry : node.getMapEntryExpressions()) {
				entry.visit(this);
			}
		} else {
			for (MapEntryExpression entry : node.getMapEntryExpressions()) {
				// visit the key as a field reference if we can find the field
				Expression key = entry.getKeyExpression();
				if (key instanceof ConstantExpression) {
					String fieldName = key.getText();
					FieldNode field = newType.getField(fieldName);
					if (field != null) {
						TypeLookupResult result = new TypeLookupResult(field.getType(), field.getDeclaringClass(), field,
								TypeConfidence.EXACT, scopes.peek());
						handleRequestor(key, newType, result);
					} else {
						handleSimpleExpression(key);
					}
				} else {
					handleSimpleExpression(key);
				}
				// and visit the value as normal
				handleSimpleExpression(entry.getValueExpression());
			}
		}
		completeExpressionStack.pop();

		// we can only have a parameterization for a non-empty map
		// also, if this map is part of a constructor call, then
		// we cannot parameterize since we did not perform the visit in the right way
		ClassNode exprType;
		if (node.getMapEntryExpressions().size() > 0 && newType == null) {
			exprType = primaryTypeStack.pop();
		} else {
			exprType = createParameterizedMap(VariableScope.OBJECT_CLASS_NODE, VariableScope.OBJECT_CLASS_NODE);
		}
		handleCompleteExpression(node, exprType, null);
		scopes.peek().forgetCurrentNode();
	}

	@Override
	public void visitMethodCallExpression(MethodCallExpression node) {
		scopes.peek().setCurrentNode(node);
		if (isDependentExpression(node)) {
			primaryTypeStack.pop();
		}
		completeExpressionStack.push(node);
		node.getObjectExpression().visit(this);

		if (node.isSpreadSafe()) {
			// most find the component type of the object expression type
			ClassNode objType = primaryTypeStack.pop();
			primaryTypeStack.push(VariableScope.extractElementType(objType));
		}

		node.getMethod().visit(this);
		// this is the inferred return type of this method
		// must pop now before visiting any other nodes
		ClassNode exprType = dependentTypeStack.pop();

		// this is the inferred declaring type of this method
		Tuple t = dependentDeclarationStack.pop();
		CallAndType call = new CallAndType(node, t.declaringType, t.declaration);

		completeExpressionStack.pop();

		ClassNode catNode = isCategoryDeclaration(node);
		if (catNode != null) {
			addCategoryToBeDeclared(catNode);
		}
		VariableScope scope = scopes.peek();

		// remember that we are inside a method call while analyzing the arguments
		scope.addEnclosingMethodCall(call);
		node.getArguments().visit(this);

		scope.forgetEnclosingMethodCall();

		// if this method call is the primary of a larger expression,
		// then pass the inferred type onwards
		if (node.isSpreadSafe()) {
			exprType = createParameterizedList(exprType);
		}
		handleCompleteExpression(node, exprType, t.declaringType);
		scopes.peek().forgetCurrentNode();
	}

	@Override
	public void visitMethodPointerExpression(MethodPointerExpression node) {
		boolean shouldContinue = handleSimpleExpression(node);
		if (shouldContinue) {
			super.visitMethodPointerExpression(node);
		}
	}

	@Override
	public void visitNotExpression(NotExpression node) {
		boolean shouldContinue = handleSimpleExpression(node);
		if (shouldContinue) {
			super.visitNotExpression(node);
		}
	}

	@Override
	public void visitPostfixExpression(PostfixExpression node) {
		visitUnaryExpression(node, node.getExpression(), node.getOperation().getText());
	}

	@Override
	public void visitPrefixExpression(PrefixExpression node) {
		visitUnaryExpression(node, node.getExpression(), node.getOperation().getText());
	}

	private void visitUnaryExpression(Expression node, Expression expression, String operation) {
		scopes.peek().setCurrentNode(node);
		completeExpressionStack.push(node);
		if (isDependentExpression(node)) {
			primaryTypeStack.pop();
		}
		expression.visit(this);

		ClassNode primaryType = primaryTypeStack.pop();
		// now infer the type of the operator. It could have been overloaded
		String associatedMethod = findUnaryOperatorName(operation);
		ClassNode completeExprType;
		if (associatedMethod == null && primaryType.equals(VariableScope.NUMBER_CLASS_NODE)
				|| primaryType.isDerivedFrom(VariableScope.NUMBER_CLASS_NODE)) {
			completeExprType = primaryType;
		} else {
			// there is an overloadable method associated with this operation
			// convert to a constant expression and infer type
			TypeLookupResult result = lookupExpressionType(new ConstantExpression(associatedMethod), primaryType, false,
					scopes.peek());
			completeExprType = result.type;
		}
		completeExpressionStack.pop();
		handleCompleteExpression(node, completeExprType, null);
	}

	@Override
	public void visitPropertyExpression(PropertyExpression node) {
		scopes.peek().setCurrentNode(node);
		completeExpressionStack.push(node);
		node.getObjectExpression().visit(this);
		ClassNode objType;
		if (isDependentExpression(node)) {
			primaryTypeStack.pop();
		}
		if (node.isSpreadSafe()) {
			objType = primaryTypeStack.pop();
			// must find the component type of the object expression type
			primaryTypeStack.push(objType = VariableScope.extractElementType(objType));
		} else {
			objType = primaryTypeStack.peek();
		}

		node.getProperty().visit(this);

		// this is the type of this property expression
		ClassNode exprType = dependentTypeStack.pop();

		// don't care about either of these
		dependentDeclarationStack.pop();
		completeExpressionStack.pop();

		// if this property expression is the primary of a larger expression,
		// then remember the inferred type
		if (node.isSpreadSafe()) {
			// if we are dealing with a map, then a spread dot will return a list of values,
			// so use the type of the value.
			if (objType.equals(VariableScope.MAP_CLASS_NODE) && objType.getGenericsTypes() != null
					&& objType.getGenericsTypes().length == 2) {
				exprType = objType.getGenericsTypes()[1].getType();
			}
			exprType = createParameterizedList(exprType);
		}
		handleCompleteExpression(node, exprType, null);
		scopes.peek().forgetCurrentNode();
	}

	@Override
	public void visitRangeExpression(RangeExpression node) {
		if (isDependentExpression(node)) {
			primaryTypeStack.pop();
		}
		scopes.peek().setCurrentNode(node);
		completeExpressionStack.push(node);
		super.visitRangeExpression(node);
		ClassNode eltType = primaryTypeStack.pop();
		completeExpressionStack.pop();
		ClassNode rangeType = createParameterizedRange(eltType);
		handleCompleteExpression(node, rangeType, null);
		scopes.peek().forgetCurrentNode();
	}

	@Override
	public void visitShortTernaryExpression(ElvisOperatorExpression node) {
		if (isDependentExpression(node)) {
			primaryTypeStack.pop();
		}
		// arbitrarily, we choose the if clause to be the type of this expression
		completeExpressionStack.push(node);
		node.getTrueExpression().visit(this);

		// the declaration itself is the property node
		ClassNode exprType = primaryTypeStack.pop();
		completeExpressionStack.pop();
		node.getFalseExpression().visit(this);
		handleCompleteExpression(node, exprType, null);
	}

	@Override
	public void visitSpreadExpression(SpreadExpression node) {
		boolean shouldContinue = handleSimpleExpression(node);
		if (shouldContinue) {
			super.visitSpreadExpression(node);
		}
	}

	@Override
	public void visitSpreadMapExpression(SpreadMapExpression node) {
		boolean shouldContinue = handleSimpleExpression(node);
		if (shouldContinue) {
			super.visitSpreadMapExpression(node);
		}
	}

	@Override
	public void visitStaticMethodCallExpression(StaticMethodCallExpression node) {
		boolean shouldContinue = handleSimpleExpression(node);
		if (shouldContinue && node.getEnd() > 0) {
			visitClassReference(node.getOwnerType());
			super.visitStaticMethodCallExpression(node);
		}
	}

	@Override
	public void visitTernaryExpression(TernaryExpression node) {
		if (isDependentExpression(node)) {
			primaryTypeStack.pop();
		}
		completeExpressionStack.push(node);

		node.getBooleanExpression().visit(this);

		// arbitrarily, we choose the if clause to be the type of this expression
		node.getTrueExpression().visit(this);

		// arbirtrarily choose the 'true' expression
		// to hold the type of the ternary expression
		ClassNode exprType = primaryTypeStack.pop();

		node.getFalseExpression().visit(this);

		completeExpressionStack.pop();

		// if the ternary expression is a primary expression
		// of a larger expression, use the exprType as the
		// primary of the next expression
		handleCompleteExpression(node, exprType, null);
	}

	// Do not treat tuple expressions like a list since
	// they are only created as LHS of a multi assignment statement
	@Override
	public void visitTupleExpression(TupleExpression node) {
		boolean shouldContinue = handleSimpleExpression(node);
		if (shouldContinue) {
			super.visitTupleExpression(node);
		}
	}

	@Override
	public void visitUnaryMinusExpression(UnaryMinusExpression node) {
		visitUnaryExpression(node, node.getExpression(), "-");
	}

	@Override
	public void visitUnaryPlusExpression(UnaryPlusExpression node) {
		visitUnaryExpression(node, node.getExpression(), "+");
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
		VisitStatus status = notifyRequestor(node, requestor, result);

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

	private boolean handleStatement(Statement node) {
		// don't check the lookups because statements have no type.
		// but individual requestors may choose to end the visit here
		VariableScope scope = scopes.peek();
		ClassNode declaring = scope.getDelegateOrThis();
		scope.setPrimaryNode(false);

		if (node instanceof BlockStatement) {
			for (ITypeLookup lookup : lookups) {
				if (lookup instanceof ITypeLookupExtension) {
					// must ensure that declaring type information at the start of the block is invoked
					((ITypeLookupExtension) lookup).lookupInBlock((BlockStatement) node, scope);
				}
			}
		}

		TypeLookupResult noLookup = new TypeLookupResult(declaring, declaring, declaring, TypeConfidence.EXACT, scope);
		VisitStatus status = notifyRequestor(node, requestor, noLookup);
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

	private boolean handleSimpleExpression(Expression node) {
		ClassNode primaryType;
		boolean isStatic;
		VariableScope scope = scopes.peek();
		if (isDependentExpression(node)) {
			// debugging help: objectExpressionType.push(objectExprType)
			primaryType = primaryTypeStack.pop();
			// implicit this expressions do not have a primary type
			if (isImplicitThis()) {
				primaryType = null;
			}
			isStatic = hasStaticObjectExpression(node);
			scope.setMethodCallNumberOfArguments(getMethodCallArgs());
		} else {
			primaryType = null;
			isStatic = false;
		}
		scope.setPrimaryNode(primaryType == null);

		TypeLookupResult result = lookupExpressionType(node, primaryType, isStatic, scope);
		return handleRequestor(node, primaryType, result);
	}

	protected boolean isImplicitThis() {
		return completeExpressionStack.peek() instanceof MethodCallExpression
				&& ((MethodCallExpression) completeExpressionStack.peek()).isImplicitThis();
	}

	private void handleCompleteExpression(Expression node, ClassNode exprType, ClassNode exprDeclaringType) {
		VariableScope scope = scopes.peek();
		scope.setPrimaryNode(false);
		handleRequestor(node, exprDeclaringType, new TypeLookupResult(exprType, exprDeclaringType, node, TypeConfidence.EXACT,
				scope));
	}

	private void postVisit(Expression node, ClassNode type, ClassNode declaringType, ASTNode declaration) {
		if (isPrimaryExpression(node)) {
			primaryTypeStack.push(type);
		} else if (isDependentExpression(node)) {
			dependentTypeStack.push(type);
			dependentDeclarationStack.push(new Tuple(declaringType, declaration));
		}
	}

	private TypeLookupResult lookupExpressionType(Expression node, ClassNode objectExprType, boolean isStatic, VariableScope scope) {
		TypeLookupResult result = null;
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
		return result;
	}

	/**
	 * Finds the number of arguments of the current method call. Returns -1 if not a method call. Returns 0 if no arguments else
	 * returns the number of arguments.
	 * 
	 * @return
	 */
	private int getMethodCallArgs() {
		ASTNode peek = completeExpressionStack.peek();
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
				assignmentStorer.storeParameterType(node, scope);
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
				scope.setPrimaryNode(false);
				VisitStatus status = notifyRequestor(node, requestor, parameterResult);
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

	private boolean handleRequestor(Expression node, ClassNode primaryType, TypeLookupResult result) {
		result.enclosingAssignment = enclosingAssignment;
		VisitStatus status = requestor.acceptASTNode(node, result, enclosingElement);
		VariableScope scope = scopes.peek();
		// forget the number of arguments
		scope.setMethodCallNumberOfArguments(-1);

		// when there is a category method, we don't want to store it
		// as the declaring type since this will mess things up inside closures
		ClassNode rememberedDeclaringType = result.declaringType;
		if (scope.getCategoryNames().contains(rememberedDeclaringType)) {
			rememberedDeclaringType = primaryType != null ? primaryType : scope.getDelegateOrThis();
		}
		if (rememberedDeclaringType == null) {
			rememberedDeclaringType = VariableScope.OBJECT_CLASS_NODE;
		}
		switch (status) {
			case CONTINUE:
				postVisit(node, result.type, rememberedDeclaringType, result.declaration);
				return true;
			case CANCEL_BRANCH:
				postVisit(node, result.type, rememberedDeclaringType, result.declaration);
				return false;
			case CANCEL_MEMBER:
			case STOP_VISIT:
				throw new VisitCompleted(status);
		}
		// won't get here
		return false;

	}

	private VisitStatus notifyRequestor(ASTNode node, ITypeRequestor requestor, TypeLookupResult result) {
		// result is never null because SimpleTypeLookup always returns non-null
		return requestor.acceptASTNode(node, result, enclosingElement);
	}

	private MethodNode findMethodNode(IMethod method) {
		// FIXADE TODO pass this in as a parameter
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
	 * @param propType
	 * @return a list parameterized by propType
	 */
	private ClassNode createParameterizedList(ClassNode propType) {
		ClassNode list = VariableScope.clonedList();
		list.getGenericsTypes()[0].setType(propType);
		list.getGenericsTypes()[0].setName(propType.getName());
		return list;
	}

	/**
	 * @param propType
	 * @return a list parameterized by propType
	 */
	private ClassNode createParameterizedRange(ClassNode propType) {
		ClassNode range = VariableScope.clonedRange();
		range.getGenericsTypes()[0].setType(propType);
		range.getGenericsTypes()[0].setName(propType.getName());
		return range;
	}

	/**
	 * @param propType
	 * @return a list parameterized by propType
	 */
	private ClassNode createParameterizedMap(ClassNode k, ClassNode v) {
		ClassNode map = VariableScope.clonedMap();
		map.getGenericsTypes()[0].setType(k);
		map.getGenericsTypes()[0].setName(k.getName());
		map.getGenericsTypes()[1].setType(v);
		map.getGenericsTypes()[1].setName(v.getName());
		return map;
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
		// ummm...do I *have* to implement this method??
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
	 * 
	 */
	private ModuleNodeInfo createModuleNode(GroovyCompilationUnit unit) {
		if (unit.getOwner() == null || unit.owner == DefaultWorkingCopyOwner.PRIMARY) {
			return unit.getModuleInfo(true);
		} else {
			return unit.getNewModuleInfo();
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

	/**
	 * Primary expressions are:
	 * <ul>
	 * <li>left part of a non-assignment binary expression
	 * <li>right part of a assignment expression
	 * <li>object (ie- left part) of a property expression
	 * <li>object (ie- left part) of a method call expression
	 * <li>object (ie- left part) of an attribute expression
	 * <li>collection expression (ie- right part) of a for statement
	 * <li>true expression (ie- right part) of a ternary expression
	 * <li>first element of a list expression (the first element is assumed to be representative of all list elements)
	 * <li>first element of a range expression (the first element is assumed to be representative of the range)
	 * <li>Either the key OR the value expression of a {@link MapEntryExpression}
	 * <li>The first {@link MapEntryExpression} of a {@link MapExpression}
	 * <li>The expression of a {@link PrefixExpression}, a {@link PostfixExpression}, a {@link UnaryMinusExpression}, a
	 * {@link UnaryPlusExpression}, or a {@link BitwiseNegationExpression}
	 * </ul>
	 * 
	 * @param node expression node to check
	 * @return true iff the node is the primary expression in an expression pair.
	 */
	private boolean isPrimaryExpression(Expression node) {
		if (!completeExpressionStack.isEmpty()) {
			ASTNode complete = completeExpressionStack.peek();
			if (complete instanceof PropertyExpression) {
				PropertyExpression prop = (PropertyExpression) complete;
				return prop.getObjectExpression() == node;
			} else if (complete instanceof MethodCallExpression) {
				MethodCallExpression prop = (MethodCallExpression) complete;
				return prop.getObjectExpression() == node;
			} else if (complete instanceof BinaryExpression) {
				BinaryExpression prop = (BinaryExpression) complete;
				// both sides of the binary expression are primary since we need
				// access to both of them when inferring binary expression types
				return prop.getRightExpression() == node || prop.getLeftExpression() == node;
			} else if (complete instanceof AttributeExpression) {
				AttributeExpression prop = (AttributeExpression) complete;
				return prop.getObjectExpression() == node;
			} else if (complete instanceof TernaryExpression) {
				TernaryExpression prop = (TernaryExpression) complete;
				return prop.getTrueExpression() == node;
			} else if (complete instanceof ForStatement) {
				// this check is used to store the type of the collection expression so that it can be assigned to the for loop
				// variable
				ForStatement prop = (ForStatement) complete;
				return prop.getCollectionExpression() == node;
			} else if (complete instanceof ListExpression) {
				return ((ListExpression) complete).getExpressions().size() > 0
						&& ((ListExpression) complete).getExpression(0) == node;
			} else if (complete instanceof RangeExpression) {
				return ((RangeExpression) complete).getFrom() == node;
			} else if (complete instanceof MapEntryExpression) {
				return ((MapEntryExpression) complete).getKeyExpression() == node
						|| ((MapEntryExpression) complete).getValueExpression() == node;
			} else if (complete instanceof MapExpression) {
				return ((MapExpression) complete).getMapEntryExpressions().size() > 0
						&& ((MapExpression) complete).getMapEntryExpressions().get(0) == node;
			} else if (complete instanceof PrefixExpression) {
				return ((PrefixExpression) complete).getExpression() == node;
			} else if (complete instanceof PostfixExpression) {
				return ((PostfixExpression) complete).getExpression() == node;
			} else if (complete instanceof UnaryPlusExpression) {
				return ((UnaryPlusExpression) complete).getExpression() == node;
			} else if (complete instanceof UnaryMinusExpression) {
				return ((UnaryMinusExpression) complete).getExpression() == node;
			} else if (complete instanceof BitwiseNegationExpression) {
				return ((BitwiseNegationExpression) complete).getExpression() == node;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Dependent expressions are expressions whose type depends on another expression.
	 * 
	 * Dependent expressions are:
	 * <ul>
	 * <li>right part of a non-assignment binary expression
	 * <li>left part of a assignment expression
	 * <li>propery (ie- right part) of a property expression
	 * <li>method (ie- right part) of a method call expression
	 * <li>property/field (ie- right part) of an attribute expression
	 * </ul>
	 * 
	 * Note that for statements and ternary expressions do not have any dependent expression even though they have primary
	 * expressions
	 * 
	 * @param node expression node to check
	 * @return true iff the node is the primary expression in an expression pair.
	 */
	private boolean isDependentExpression(Expression node) {
		if (!completeExpressionStack.isEmpty()) {
			ASTNode complete = completeExpressionStack.peek();
			if (complete instanceof PropertyExpression) {
				PropertyExpression prop = (PropertyExpression) complete;
				return prop.getProperty() == node;
			} else if (complete instanceof MethodCallExpression) {
				MethodCallExpression prop = (MethodCallExpression) complete;
				return prop.getMethod() == node;
			} else if (complete instanceof ImportNode) {
				ImportNode imp = (ImportNode) complete;
				return node == imp.getAliasExpr() || node == imp.getFieldNameExpr();
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
		if (!completeExpressionStack.isEmpty()) {
			ASTNode maybeProperty = completeExpressionStack.peek();
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
	 * Only handle operations that are not handled in {@link #findBinaryOperatorName(String)}
	 * 
	 * @param operation the operation of this binary expression
	 * @param lhs the type of the lhs of the binary expression
	 * @param rhs the type of the rhs of the binary expression
	 * @return the determined type of the binary expression
	 */
	private ClassNode findTypeOfBinaryExpression(String operation, ClassNode lhs, ClassNode rhs) {
		char op = operation.charAt(0);
		switch (op) {
			case '*':
				if (operation.equals("*.") || operation.equals("*.@")) {
					// can we do better and parameterize the list?
					return VariableScope.clonedList();
				}
			case '~':
				// regex pattern
				return VariableScope.STRING_CLASS_NODE;

			case '!':
				// includes != and !== and !!
			case '<':
			case '>':
				if (operation.length() > 1) {
					if (operation.equals("<=>")) {
						return VariableScope.INTEGER_CLASS_NODE;
					}
				}
				// all booleans
				return VariableScope.BOOLEAN_CLASS_NODE;

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

	private void addCategoryToBeDeclared(ClassNode catNode) {
		scopes.peek().setCategoryBeingDeclared(catNode);
	}

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

	/**
	 * For testing only, ensures that after a visit is complete,
	 */
	private void postVisitSanityCheck() {
		Assert.isTrue(completeExpressionStack.isEmpty(),
				"Inferencing engine in invalid state after visitor completed.  All stacks should be empty after visit completed.");
		Assert.isTrue(primaryTypeStack.isEmpty(),
				"Inferencing engine in invalid state after visitor completed.  All stacks should be empty after visit completed.");
		Assert.isTrue(dependentDeclarationStack.isEmpty(),
				"Inferencing engine in invalid state after visitor completed.  All stacks should be empty after visit completed.");
		Assert.isTrue(dependentTypeStack.isEmpty(),
				"Inferencing engine in invalid state after visitor completed.  All stacks should be empty after visit completed.");
		Assert.isTrue(scopes.isEmpty(),
				"Inferencing engine in invalid state after visitor completed.  All stacks should be empty after visit completed.");
	}
}
