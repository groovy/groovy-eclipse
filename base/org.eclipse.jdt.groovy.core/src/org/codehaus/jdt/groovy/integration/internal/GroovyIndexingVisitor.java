/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - Initial API and implementation
 *     Andy Clement     - Additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.integration.internal;

import java.util.SortedSet;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ImportNodeCompatibilityWrapper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @author Andrew Eisenberg
 * @created Aug 27, 2009
 * 
 *          Visits a ModuleNode and passes it to an indexing element requestor, thus adding this class to the Java indexes
 */
public class GroovyIndexingVisitor extends ClassCodeVisitorSupport {

	private ISourceElementRequestor requestor;

	// used for GRECLIPSE-741, remove when issue is solved
	private ModuleNode module;

	public GroovyIndexingVisitor(ISourceElementRequestor requestor) {
		this.requestor = requestor;
	}

	// not used
	@Override
	protected SourceUnit getSourceUnit() {
		return null;
	}

	void doVisit(ModuleNode node, ImportReference pkg) {
		if (node == null) {
			// there is an unrecoverable compile problem.
			return;
		}

		// used for GRECLIPSE-741, remove when issue is solved
		module = node;
		try {
			this.visitImports(node);

			for (ClassNode clazz : (Iterable<ClassNode>) node.getClasses()) {
				this.visitClass(clazz);
			}
		} catch (RuntimeException e) {
			Util.log(e);
		}
	}

	public void visitImports(ModuleNode node) {
		if (node != null) {
			SortedSet<ImportNode> allImports = new ImportNodeCompatibilityWrapper(node).getAllImportNodes();

			for (ImportNode importNode : allImports) {
				visitAnnotations(importNode);
				if (importNode.getType() != null) {
					handleType(importNode.getType(), false, true);
				}
				String importFieldName = ImportNodeCompatibilityWrapper.getFieldName(importNode);
				if (importFieldName != null) {
					requestor.acceptUnknownReference(importFieldName.toCharArray(), 0);
				}
			}
		}
	}

	@Override
	public void visitMethodCallExpression(MethodCallExpression call) {
		super.visitMethodCallExpression(call);
		String methodStr = call.getMethodAsString();
		if (methodStr == null)
			return;

		char[] methodName = methodStr.toCharArray();
		int start = call.getStart();
		// also could be a field reference
		requestor.acceptFieldReference(methodName, start);
		// we don't know how many arguments the method has, so go up to 7.
		for (int i = 0; i < 7; i++) {
			requestor.acceptMethodReference(methodName, i, start);
		}
	}

	@Override
	public void visitFieldExpression(FieldExpression expression) {
		super.visitFieldExpression(expression);
		requestor.acceptFieldReference(expression.getFieldName().toCharArray(), expression.getStart());
	}

	@Override
	public void visitConstantExpression(ConstantExpression expression) {
		if (!(expression.isTrueExpression() || expression.isFalseExpression() || expression.isNullExpression() || expression
				.isEmptyStringExpression())) {
			char[] constName = expression.getValue().toString().toCharArray();
			int start = expression.getStart();
			requestor.acceptFieldReference(constName, start);
			// also could be a method reference
			// we don't know how many arguments the method has, so go up to 7.
			for (int i = 0; i < 7; i++) {
				requestor.acceptMethodReference(constName, i, start);
			}
		}
		super.visitConstantExpression(expression);
	}

	@Override
	public void visitCastExpression(CastExpression expression) {
		handleType(expression.getType(), false, true);
	}

	@Override
	public void visitClassExpression(ClassExpression expression) {
		handleType(expression.getType(), false, true);
	}

	@Override
	public void visitConstructorCallExpression(ConstructorCallExpression call) {
		super.visitConstructorCallExpression(call);
		for (int i = 0; i < 10; i++) {
			requestor.acceptConstructorReference(call.getType().getName().toCharArray(), i, call.getStart());
		}
		// handleType(call.getType(), false);
	}

	@Override
	public void visitDeclarationExpression(DeclarationExpression expression) {
		handleType(expression.getLeftExpression().getType(), false, true);
		expression.getRightExpression().visit(this);
		// super.visitDeclarationExpression(expression);
	}

	@Override
	public void visitVariableExpression(VariableExpression expression) {
		requestor.acceptUnknownReference(expression.getName().toCharArray(), expression.getStart());
	}

	@Override
	public void visitField(FieldNode node) {
		// handleType(node.getType(), false);
		super.visitField(node);
	}

	@Override
	public void visitMethod(MethodNode node) {
		if (!node.isSynthetic()) {
			handleType(node.getReturnType(), false, true);
			for (Parameter param : node.getParameters()) {
				handleType(param.getType(), false, true);
			}
		}
		super.visitMethod(node);
	}

	@Override
	public void visitClass(ClassNode node) {
		if (!node.isSynthetic()) {
			handleType(node, false, false);
			handleType(node.getSuperClass(), false, true);
			for (ClassNode impls : node.getInterfaces()) {
				handleType(impls, false, true);
			}
			// handleType(node, node.isAnnotationDefinition(), false);
		}
		// don't do a super call because it revisits the imports and packages
		// super.visitClass(node);
		visitAnnotations(node);
		node.visitContents(this);
		for (Statement element : (Iterable<Statement>) node.getObjectInitializerStatements()) {
			element.visit(this);
		}

	}

	@Override
	public void visitClosureExpression(ClosureExpression node) {
		if (node.getParameters() != null) {
			for (Parameter param : node.getParameters()) {
				handleType(param.getType(), false, true);
			}
		}
		super.visitClosureExpression(node);
	}

	@Override
	public void visitAnnotations(AnnotatedNode node) {
		for (AnnotationNode an : (Iterable<AnnotationNode>) node.getAnnotations()) {
			handleType(an.getClassNode(), true, true);
		}
		super.visitAnnotations(node);
	}

	// may not be resolved
	private void handleType(ClassNode node, boolean isAnnotation, boolean useQualifiedName) {
		if (node == null) {
			// GRECLIPSE-741
			Util.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "GRECLIPSE-741: module: " + module.getDescription(),
					new RuntimeException()));
			return;
		}
		if (isAnnotation) {
			requestor.acceptAnnotationTypeReference(splitName(node, useQualifiedName), node.getStart(), node.getEnd());
		} else {
			ClassNode componentType = node.getComponentType();
			requestor.acceptTypeReference(splitName(componentType != null ? componentType : node, useQualifiedName),
					node.getStart(), node.getEnd());
		}
		if (node.isUsingGenerics() && node.getGenericsTypes() != null) {
			for (GenericsType gen : node.getGenericsTypes()) {
				ClassNode lowerBound = gen.getLowerBound();
				if (lowerBound != null) {
					handleType(lowerBound, lowerBound.isAnnotationDefinition(), true);
				}
				if (gen.getUpperBounds() != null) {
					for (ClassNode upper : gen.getUpperBounds()) {
						// handle enums where the upper bound is the same as the type
						if (!upper.getName().equals(node.getName())) {
							handleType(upper, upper.isAnnotationDefinition(), true);
						}
					}
				}
				ClassNode genType = gen.getType();
				if (genType != null && gen.getName().charAt(0) != '?') {
					handleType(genType, genType.isAnnotationDefinition(), true);
				}
			}
		}

	}

	private char[][] splitName(ClassNode node, boolean useQualifiedName) {
		String name = useQualifiedName ? node.getName() : node.getNameWithoutPackage();
		String[] nameArr = name.split("\\."); //$NON-NLS-1$
		char[][] nameCharArr = new char[nameArr.length][];
		for (int i = 0; i < nameArr.length; i++) {
			nameCharArr[i] = nameArr[i].toCharArray();
		}
		return nameCharArr;
	}
}
