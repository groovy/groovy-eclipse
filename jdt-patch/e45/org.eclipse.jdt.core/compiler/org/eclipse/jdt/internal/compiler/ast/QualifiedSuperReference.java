/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								bug 382350 - [1.8][compiler] Unable to invoke inherited default method via I.super.m() syntax
 *								bug 404649 - [1.8][compiler] detect illegal reference to indirect or redundant super
 *								bug 404728 - [1.8]NPE on QualifiedSuperReference error
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class QualifiedSuperReference extends QualifiedThisReference {

public QualifiedSuperReference(TypeReference name, int pos, int sourceEnd) {
	super(name, pos, sourceEnd);
}

public boolean isSuper() {
	return true;
}

public boolean isQualifiedSuper() {
	return true;
}

public boolean isThis() {
	return false;
}

public StringBuffer printExpression(int indent, StringBuffer output) {
	return this.qualification.print(0, output).append(".super"); //$NON-NLS-1$
}

public TypeBinding resolveType(BlockScope scope) {
	if ((this.bits & ParenthesizedMASK) != 0) {
		scope.problemReporter().invalidParenthesizedExpression(this);
		return null;
	}
	super.resolveType(scope);
	if (this.resolvedType != null && !this.resolvedType.isValidBinding()) {
		scope.problemReporter().illegalSuperAccess(this.qualification.resolvedType, this.resolvedType, this);
		return null;
	}
	if (this.currentCompatibleType == null)
		return null; // error case

	if (this.currentCompatibleType.id == T_JavaLangObject) {
		scope.problemReporter().cannotUseSuperInJavaLangObject(this);
		return null;
	}
	return this.resolvedType = (this.currentCompatibleType.isInterface()
			? this.currentCompatibleType
			: this.currentCompatibleType.superclass());
}

int findCompatibleEnclosing(ReferenceBinding enclosingType, TypeBinding type) {
	if (type.isInterface()) {
		// super call to an overridden default method? (not considering outer enclosings)
		ReferenceBinding[] supers = enclosingType.superInterfaces();
		int length = supers.length;
		boolean isLegal = true; // false => compoundName != null && closestMatch != null
		char[][] compoundName = null;
		ReferenceBinding closestMatch = null;
		for (int i = 0; i < length; i++) {
			if (TypeBinding.equalsEquals(supers[i].erasure(), type)) {
				this.currentCompatibleType = closestMatch = supers[i];
			} else if (supers[i].erasure().isCompatibleWith(type)) {
				isLegal = false;
				compoundName = supers[i].compoundName;
				if (closestMatch == null)
					closestMatch = supers[i];
				// keep looking to ensure we always find the referenced type (even if illegal) 
			}
		}
		if (!isLegal) {
			this.currentCompatibleType = null;
			// Please note the slightly unconventional use of the ProblemReferenceBinding:
			// we use the problem's compoundName to report the type being illegally bypassed,
			// whereas the closestMatch denotes the resolved (though illegal) target type
			// for downstream resolving.
			this.resolvedType =  new ProblemReferenceBinding(compoundName, 
					closestMatch, ProblemReasons.AttemptToBypassDirectSuper);
		}
		return 0; // never an outer enclosing type
	}
	return super.findCompatibleEnclosing(enclosingType, type);
}

public void traverse(
	ASTVisitor visitor,
	BlockScope blockScope) {

	if (visitor.visit(this, blockScope)) {
		this.qualification.traverse(visitor, blockScope);
	}
	visitor.endVisit(this, blockScope);
}
public void traverse(
		ASTVisitor visitor,
		ClassScope blockScope) {

	if (visitor.visit(this, blockScope)) {
		this.qualification.traverse(visitor, blockScope);
	}
	visitor.endVisit(this, blockScope);
}
}
