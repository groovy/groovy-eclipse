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

import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
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
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @author Andrew Eisenberg
 * @created Aug 29, 2009
 * 
 *          Visits a GroovyCompilationUnit in order to determine the type
 */
public class TypeInferencingVisitorWithRequestor extends ClassCodeVisitorSupport {

	public class VisitCompleted extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	private final GroovyCompilationUnit unit;

	// we are going to have to be very careful about the ordering of lookups
	// Simple type lookup must be last because it always returns an answer
	// Assume that if something returns an answer, then we go with that.
	// Later on, should do some ordering of results
	private final ITypeLookup[] lookups;

	private ITypeRequestor requestor;
	private IJavaElement enclosingElement;
	private ASTNode enclosingDeclarationNode;

	private boolean rethrowVisitComplete = false;

	public TypeInferencingVisitorWithRequestor(GroovyCompilationUnit unit, ITypeLookup[] lookups) {
		super();
		this.unit = unit;
		enclosingDeclarationNode = unit.getModuleNode();
		this.lookups = lookups;
	}

	public void visitCompilationUnit(ITypeRequestor requestor) {
		this.requestor = requestor;
		enclosingElement = unit;
		rethrowVisitComplete = true;

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

		} catch (VisitCompleted vc) {
		}
	}

	public void visitPackage(PackageNode p) {
		// do nothing for now
	}

	public void visitJDT(IType type, ITypeRequestor requestor) {
		IJavaElement oldEnclosing = enclosingElement;
		ASTNode oldEnclosingNode = enclosingDeclarationNode;
		boolean oldRethrow = rethrowVisitComplete;
		rethrowVisitComplete = true;
		enclosingElement = type;
		try {
			ClassNode node = findClassWithName(type.getElementName());
			enclosingDeclarationNode = node;
			visitClassInternal(node);

			// visitJDT so that we have the proper enclosing element
			try {
				for (IJavaElement child : type.getChildren()) {
					switch (child.getElementType()) {
						case IJavaElement.METHOD:
							visitJDT((IMethod) child, requestor);
							break;

						case IJavaElement.FIELD:
							visitJDT((IField) child, requestor);
							break;

						default:
							break;
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
		}
	}

	public void visitJDT(IField field, ITypeRequestor requestor) {
		IJavaElement oldEnclosing = enclosingElement;
		ASTNode oldEnclosingNode = enclosingDeclarationNode;
		enclosingElement = field;
		this.requestor = requestor;
		FieldNode fieldNode = findFieldNode(field);
		enclosingDeclarationNode = fieldNode;
		try {
			visitField(fieldNode);
		} catch (VisitCompleted vc) {
			if (rethrowVisitComplete) {
				throw vc;
			}
		} finally {
			enclosingDeclarationNode = oldEnclosingNode;
			enclosingElement = oldEnclosing;
		}
	}

	public void visitJDT(IMethod method, ITypeRequestor requestor) {
		IJavaElement oldEnclosing = enclosingElement;
		ASTNode oldEnclosingNode = enclosingDeclarationNode;
		enclosingElement = method;
		MethodNode methodNode = findMethodNode(method);
		enclosingDeclarationNode = methodNode;
		this.requestor = requestor;
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
		}
	}

	private void visitClassInternal(ClassNode node) {
		TypeLookupResult result = null;
		visitAnnotations(node);

		ClassNode supr = node.getSuperClass();
		for (ITypeLookup lookup : lookups) {
			result = lookup.lookupType(supr);
			if (result != null) {
				break;
			}
		}
		// use unresolved super to maintain source locations
		VisitStatus status = handleRequestor(node.getUnresolvedSuperClass(false), requestor, result);
		switch (status) {
			case CONTINUE:
				break;
			case CANCEL_BRANCH:
				return;
			case STOP_VISIT:
				throw new VisitCompleted();
		}

		for (ClassNode intr : node.getInterfaces()) {
			for (ITypeLookup lookup : lookups) {
				result = lookup.lookupType(intr);
				if (result != null) {
					break;
				}
			}
			status = handleRequestor(intr, requestor, result);
			switch (status) {
				case CONTINUE:
					break;
				case CANCEL_BRANCH:
					return;
				case STOP_VISIT:
					throw new VisitCompleted();
			}
		}

		for (Statement element : (Iterable<Statement>) node.getObjectInitializerStatements()) {
			element.visit(this);
		}

		// don't visit contents, the visitJDT methods are used instead
	}

	@Override
	public void visitField(FieldNode node) {
		TypeLookupResult result = null;
		for (ITypeLookup lookup : lookups) {
			result = lookup.lookupType(node);
			if (result != null) {
				break;
			}
		}
		VisitStatus status = handleRequestor(node, requestor, result);
		switch (status) {
			case CONTINUE:
				super.visitField(node);
			case CANCEL_BRANCH:
				return;
			case STOP_VISIT:
				throw new VisitCompleted();
		}
	}

	@Override
	public void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
		TypeLookupResult result = null;
		for (ITypeLookup lookup : lookups) {
			result = lookup.lookupType(node);
			if (result != null) {
				break;
			}
		}
		VisitStatus status = handleRequestor(node, requestor, result);

		switch (status) {
			case CONTINUE:
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

	public void visitImports(ModuleNode node) {
		for (ImportNode imp : (Iterable<ImportNode>) node.getImports()) {
			TypeLookupResult result = null;
			for (ITypeLookup lookup : lookups) {
				result = lookup.lookupType(imp);
				if (result != null) {
					break;
				}
			}
			VisitStatus status = handleRequestor(imp, requestor, result);
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

	private boolean handleExpression(Expression node) {
		TypeLookupResult result = null;
		for (ITypeLookup lookup : lookups) {
			result = lookup.lookupType(node, enclosingDeclarationNode);
			if (result != null) {
				break;
			}
		}
		VisitStatus status = handleRequestor(node, requestor, result);
		switch (status) {
			case CONTINUE:
				return true;
			case CANCEL_BRANCH:
				return false;
			case STOP_VISIT:
				throw new VisitCompleted();
		}
		// won't get here
		return false;
	}

	private boolean handleParameterList(Parameter[] params) {
		if (params != null) {
			for (Parameter node : params) {
				TypeLookupResult result = null;
				for (ITypeLookup lookup : lookups) {
					result = lookup.lookupType(node.getType());
					if (result != null) {
						break;
					}
				}
				VisitStatus status = handleRequestor(node.getType(), requestor, result);
				switch (status) {
					case CONTINUE:
						break;
					case CANCEL_BRANCH:
						return false;
					case STOP_VISIT:
						throw new VisitCompleted();
				}
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
			super.visitAttributeExpression(node);
		}
	}

	@Override
	public void visitBinaryExpression(BinaryExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitBinaryExpression(node);
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
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			handleParameterList(node.getParameters());
			super.visitClosureExpression(node);
		}
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
			super.visitConstructorCallExpression(node);
		}
	}

	@Override
	public void visitDeclarationExpression(DeclarationExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			// don't call super, or else this expression is handled twice
			node.getLeftExpression().visit(this);
			node.getRightExpression().visit(this);
		}
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
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitMethodCallExpression(node);
		}
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
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitPropertyExpression(node);
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
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitShortTernaryExpression(node);
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
		if (shouldContinue) {
			super.visitStaticMethodCallExpression(node);
		}
	}

	@Override
	public void visitTernaryExpression(TernaryExpression node) {
		boolean shouldContinue = handleExpression(node);
		if (shouldContinue) {
			super.visitTernaryExpression(node);
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
		for (ITypeLookup lookup : lookups) {
			result = lookup.lookupType(node);
			if (result != null) {
				break;
			}
		}
		VisitStatus status = handleRequestor(node, requestor, result);

		switch (status) {
			case CONTINUE:
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
		ClassNode clazz = findClassWithName(method.getDeclaringType().getElementName());
		try {
			if (method.isConstructor()) {
				List<ConstructorNode> constructors = clazz.getDeclaredConstructors();
				for (ConstructorNode constructorNode : constructors) {
					String[] jdtParamTypes = method.getParameterTypes() == null ? new String[0] : method.getParameterTypes();
					Parameter[] groovyParams = constructorNode.getParameters() == null ? new Parameter[0] : constructorNode
							.getParameters();
					if (groovyParams.length != jdtParamTypes.length) {
						continue;
					}
					// FIXDE M2 this is not precise. Doesn't take into account generics
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
					// FIXDE M2 this is not precise. Doesn't take into account generics
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
		throw new IllegalArgumentException("Could not find method " + method.getElementName() + " in class " + clazz.getName()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private FieldNode findFieldNode(IField field) {
		ClassNode clazz = findClassWithName(field.getDeclaringType().getElementName());
		return clazz.getField(field.getElementName());
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

}
