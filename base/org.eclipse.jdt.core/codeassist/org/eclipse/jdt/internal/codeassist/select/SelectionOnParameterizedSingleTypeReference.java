/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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
	
	public TypeBinding resolveType(BlockScope scope, boolean checkBounds) {
		super.resolveType(scope, checkBounds);
		throw new SelectionNodeFound(this.resolvedType);
	}
	
	public TypeBinding resolveType(ClassScope scope) {
		super.resolveType(scope);
		throw new SelectionNodeFound(this.resolvedType);
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output){
		output.append("<SelectOnType:");//$NON-NLS-1$
		output.append(token);
		output.append('<');
		int max = typeArguments.length - 1;
		for (int i= 0; i < max; i++) {
			typeArguments[i].print(0, output);
			output.append(", ");//$NON-NLS-1$
		}
		typeArguments[max].print(0, output);
		output.append('>');
		output.append('>'); 
		return output;
	}
}
