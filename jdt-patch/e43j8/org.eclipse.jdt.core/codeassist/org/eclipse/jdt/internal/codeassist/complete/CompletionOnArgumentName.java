/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;


public class CompletionOnArgumentName extends Argument {

	private static final char[] FAKENAMESUFFIX = " ".toCharArray(); //$NON-NLS-1$
	public char[] realName;
	public boolean isCatchArgument = false;

	public CompletionOnArgumentName(char[] name , long posNom , TypeReference tr , int modifiers){

		super(CharOperation.concat(name, FAKENAMESUFFIX), posNom, tr, modifiers);
		this.realName = name;
	}

	public TypeBinding bind(MethodScope scope, TypeBinding typeBinding, boolean used) {

		super.bind(scope, typeBinding, used);
		throw new CompletionNodeFound(this, scope);
	}

	public StringBuffer print(int indent, StringBuffer output) {

		printIndent(indent, output);
		output.append("<CompleteOnArgumentName:"); //$NON-NLS-1$
		if (this.type != null) this.type.print(0, output).append(' ');
		output.append(this.realName);
		if (this.initialization != null) {
			output.append(" = "); //$NON-NLS-1$
			this.initialization.printExpression(0, output);
		}
		return output.append('>');
	}

	public void resolve(BlockScope scope) {

		super.resolve(scope);
		throw new CompletionNodeFound(this, scope);
	}
}

