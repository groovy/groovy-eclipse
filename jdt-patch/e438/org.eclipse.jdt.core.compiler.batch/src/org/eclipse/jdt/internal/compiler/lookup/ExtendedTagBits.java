/*******************************************************************************
 * Copyright (c) 2020, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;

public interface ExtendedTagBits {

	int HasUnresolvedPermittedSubtypes = ASTNode.Bit2;

	/** From Java 16
	 *  Flag used to identify the annotation jdk.internal.ValueBased
	 */
	int AnnotationValueBased = ASTNode.Bit3;

	// Java 16 Records
	int IsCanonicalConstructor = ASTNode.Bit4; // constructor

	// @Owning / closing
	int IsClosingMethod = ASTNode.Bit1; // method

	int HasMissingOwningAnnotation = ASTNode.Bit2; // method/ctor or field

	int AnnotationResolved = ASTNode.Bit6;
	int DeprecatedAnnotationResolved = ASTNode.Bit7;
	int NullDefaultAnnotationResolved = ASTNode.Bit8; // package, type, method or variable
	int AllAnnotationsResolved = ExtendedTagBits.AnnotationResolved | ExtendedTagBits.DeprecatedAnnotationResolved | ExtendedTagBits.NullDefaultAnnotationResolved;
	static boolean areAllAnnotationsResolved(long extendedTagBits) {
		return (extendedTagBits & AllAnnotationsResolved) == AllAnnotationsResolved;
	}

	int IsNullAnnotationPackage = ASTNode.Bit1; // package
}
