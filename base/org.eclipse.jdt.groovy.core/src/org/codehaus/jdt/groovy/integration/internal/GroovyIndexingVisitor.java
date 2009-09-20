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

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
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
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;

/**
 * @author Andrew Eisenberg
 * @created Aug 27, 2009
 * 
 *          Visits a ModuleNode and passes it to an indexing element requestor, thus adding this class to the Java indexes
 */
public class GroovyIndexingVisitor extends ClassCodeVisitorSupport {

	private ISourceElementRequestor requestor;

	public GroovyIndexingVisitor(ISourceElementRequestor requestor) {
		this.requestor = requestor;
	}

	// not used
	@Override
	protected SourceUnit getSourceUnit() {
		return null;
	}

	void doVisit(ModuleNode node, ImportReference pkg) {

		// package is visited by the notifier
		// if (pkg != null) {
		// requestor.acceptPackage(pkg);
		// }
		this.visitImports(node);

		for (ClassNode clazz : (Iterable<ClassNode>) node.getClasses()) {
			this.visitClass(clazz);
		}
	}

	public void visitImports(ModuleNode node) {
		if (node != null) {
			for (ImportNode importNode : (Iterable<ImportNode>) node.getImports()) {
				visitAnnotations(importNode);
				importNode.visit(this);
				handleType(importNode.getType(), false, true);
			}
			for (ClassNode staticImportClasses : (Iterable<ClassNode>) node.getStaticImportClasses().values()) {
				handleType(staticImportClasses, false, true);
			}
			for (ClassNode staticImportAliases : (Iterable<ClassNode>) node.getStaticImportAliases().values()) {
				handleType(staticImportAliases, false, true);
			}
			for (String fieldName : (Iterable<String>) node.getStaticImportFields().values()) {
				requestor.acceptUnknownReference(fieldName.toCharArray(), 0);
			}
		}
	}

	@Override
	public void visitMethodCallExpression(MethodCallExpression call) {
		super.visitMethodCallExpression(call);
		requestor.acceptMethodReference(call.getMethodAsString().toCharArray(), 0, call.getStart());
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
			requestor.acceptFieldReference(expression.getValue().toString().toCharArray(), expression.getStart());
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
		requestor.acceptConstructorReference(call.getType().getName().toCharArray(), 0, call.getStart());
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
		for (Parameter param : node.getParameters()) {
			handleType(param.getType(), false, true);
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
		if (isAnnotation) {
			requestor.acceptAnnotationTypeReference(splitName(node, useQualifiedName), node.getStart(), node.getEnd());
		} else {
			ClassNode componentType = node.getComponentType();
			requestor.acceptTypeReference(splitName(componentType != null ? componentType : node, useQualifiedName), node
					.getStart(), node.getEnd());
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
