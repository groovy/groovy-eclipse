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

/*
 * Selection node build by the parser in any case it was intending to
 * reduce a field reference containing the cursor.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      bar().[start]fred[end]
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <SelectOnFieldReference:bar().fred>
 *         }
 *       }
 *
 */
 
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SelectionOnFieldReference extends FieldReference {
	
	public SelectionOnFieldReference(char[] source , long pos) {

		super(source, pos);
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output){
		
		output.append("<SelectionOnFieldReference:");  //$NON-NLS-1$
		return super.printExpression(0, output).append('>');
	}
	
	public TypeBinding resolveType(BlockScope scope) {

		super.resolveType(scope);
		// tolerate some error cases
		if (binding == null || 
				!(binding.isValidBinding() || 
					binding.problemId() == ProblemReasons.NotVisible
					|| binding.problemId() == ProblemReasons.InheritedNameHidesEnclosingName
					|| binding.problemId() == ProblemReasons.NonStaticReferenceInConstructorInvocation
					|| binding.problemId() == ProblemReasons.NonStaticReferenceInStaticContext))
			throw new SelectionNodeFound();
		else
			throw new SelectionNodeFound(binding);
	}
}
