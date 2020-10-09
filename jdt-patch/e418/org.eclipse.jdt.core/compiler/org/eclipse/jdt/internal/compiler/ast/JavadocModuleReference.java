/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;


public class JavadocModuleReference extends Expression implements IJavadocTypeReference {

	public int tagSourceStart, tagSourceEnd;
	public TypeReference typeReference;
	public ModuleReference moduleReference;

	public JavadocModuleReference(char[][] sources, long[] pos, int tagStart, int tagEnd) {
		super();
		this.moduleReference = new ModuleReference(sources, pos);
		this.tagSourceStart = tagStart;
		this.tagSourceEnd = tagEnd;
		this.sourceStart = this.moduleReference.sourceStart;
		this.sourceEnd = this.moduleReference.sourceEnd;
		this.bits |= ASTNode.InsideJavadoc;
	}

	/* (non-Javadoc)
	 * Redefine to capture javadoc specific signatures
	 * @see org.eclipse.jdt.internal.compiler.ast.ASTNode#traverse(org.eclipse.jdt.internal.compiler.ASTVisitor, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}

	@Override
	public int getTagSourceStart() {
		return this.tagSourceStart;
	}

	@Override
	public int getTagSourceEnd() {
		return this.tagSourceEnd;
	}

	public TypeReference getTypeReference() {
		return this.typeReference;
	}

	public void setTypeReference(TypeReference typeReference) {
		this.typeReference = typeReference;
		if (this.typeReference != null) {
			this.sourceEnd = this.typeReference.sourceEnd;
		}
	}

	public ModuleReference getModuleReference() {
		return this.moduleReference;
	}

	public void setModuleReference(ModuleReference moduleReference) {
		this.moduleReference = moduleReference;
		this.sourceStart = this.moduleReference.sourceStart;
		this.sourceStart = this.moduleReference.sourceEnd;
	}

	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		if (this.moduleReference != null) {
			output.append(this.moduleReference.moduleName);
		}
		output.append('/');
		if (this.typeReference != null) {
			this.typeReference.printExpression(indent, output);
		}
		return output;
	}

	public ModuleBinding resolve(Scope scope) {
		return this.moduleReference.resolve(scope);
	}

	private ModuleBinding resolveModule(BlockScope scope) {
		return this.moduleReference.resolve(scope);
	}

	private ModuleBinding resolveModule(ClassScope scope) {
		return this.moduleReference.resolve(scope);
	}

	@Override
	public TypeBinding resolveType(BlockScope blockScope) {
		this.resolveModule(blockScope);
		if (this.moduleReference.binding != null
				&& this.typeReference != null) {
			return this.typeReference.resolveType(blockScope);
		}
		return null;
	}

	@Override
	public TypeBinding resolveType(ClassScope classScope) {
		this.resolveModule(classScope);
		assert(this.moduleReference.binding != null);
		if (this.typeReference != null) {
			return this.typeReference.resolveType(classScope, -1);
		}
		return null;
	}
}
