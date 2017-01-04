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
 *								Bug 429958 - [1.8][null] evaluate new DefaultLocation attribute of @NonNullByDefault
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SelectionOnParameterizedSingleTypeReference extends ParameterizedSingleTypeReference {
	public SelectionOnParameterizedSingleTypeReference(char[] name, TypeReference[] typeArguments, long pos){
		super(name, typeArguments, 0, pos);
	}

	public TypeBinding resolveType(BlockScope scope, boolean checkBounds, int location) {
		super.resolveType(scope, checkBounds, location);
		throw new SelectionNodeFound(this.resolvedType);
	}

	public TypeBinding resolveType(ClassScope scope, int location) {
		super.resolveType(scope, location);
		throw new SelectionNodeFound(this.resolvedType);
	}

	public StringBuffer printExpression(int indent, StringBuffer output){
		output.append("<SelectOnType:");//$NON-NLS-1$
		output.append(this.token);
		output.append('<');
		int max = this.typeArguments.length - 1;
		for (int i= 0; i < max; i++) {
			this.typeArguments[i].print(0, output);
			output.append(", ");//$NON-NLS-1$
		}
		this.typeArguments[max].print(0, output);
		output.append('>');
		output.append('>');
		return output;
	}
}
