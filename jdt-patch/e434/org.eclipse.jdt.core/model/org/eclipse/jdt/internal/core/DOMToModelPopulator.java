/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Process an AST to populate a tree of IJavaElement->JavaElementInfo.
 * DOM-first approach to what legacy implements through ECJ parser and CompilationUnitStructureRequestor
 */
public class DOMToModelPopulator extends ASTVisitor {

	public static LocalVariable toLocalVariable(SingleVariableDeclaration parameter, JavaElement parent) {
		return toLocalVariable(parameter, parent, parameter.getParent() instanceof MethodDeclaration);
	}

	private static LocalVariable toLocalVariable(SingleVariableDeclaration parameter, JavaElement parent, boolean isParameter) {
		return new LocalVariable(parent,
				parameter.getName().getIdentifier(),
				getStartConsideringLeadingComments(parameter),
				parameter.getStartPosition() + parameter.getLength() - 1,
				parameter.getName().getStartPosition(),
				parameter.getName().getStartPosition() + parameter.getName().getLength() - 1,
				Util.getSignature(parameter.getType()),
				null, // should be populated while navigating children
				toModelFlags(parameter.getModifiers(), false),
				isParameter);
	}

	private static int getStartConsideringLeadingComments(ASTNode node) {
		int start = node.getStartPosition();
		var unit = domUnit(node);
		int index = unit.firstLeadingCommentIndex(node);
		if (index >= 0 && index <= unit.getCommentList().size()) {
			Comment comment = (Comment)unit.getCommentList().get(index);
			start = comment.getStartPosition();
		}
		return start;
	}

	private static org.eclipse.jdt.core.dom.CompilationUnit domUnit(ASTNode node) {
		while (node != null && !(node instanceof org.eclipse.jdt.core.dom.CompilationUnit)) {
			node = node.getParent();
		}
		return (org.eclipse.jdt.core.dom.CompilationUnit)node;
	}

	private static int toModelFlags(int domModifiers, boolean isDeprecated) {
		int res = 0;
		if (Modifier.isAbstract(domModifiers)) res |= Flags.AccAbstract;
		if (Modifier.isDefault(domModifiers)) res |= Flags.AccDefaultMethod;
		if (Modifier.isFinal(domModifiers)) res |= Flags.AccFinal;
		if (Modifier.isNative(domModifiers)) res |= Flags.AccNative;
		if (Modifier.isNonSealed(domModifiers)) res |= Flags.AccNonSealed;
		if (Modifier.isPrivate(domModifiers)) res |= Flags.AccPrivate;
		if (Modifier.isProtected(domModifiers)) res |= Flags.AccProtected;
		if (Modifier.isPublic(domModifiers)) res |= Flags.AccPublic;
		if (Modifier.isSealed(domModifiers)) res |= Flags.AccSealed;
		if (Modifier.isStatic(domModifiers)) res |= Flags.AccStatic;
		if (Modifier.isStrictfp(domModifiers)) res |= Flags.AccStrictfp;
		if (Modifier.isSynchronized(domModifiers)) res |= Flags.AccSynchronized;
		if (Modifier.isTransient(domModifiers)) res |= Flags.AccTransient;
		if (Modifier.isVolatile(domModifiers)) res |= Flags.AccVolatile;
		if (isDeprecated) res |= Flags.AccDeprecated;
		return res;
	}

}
