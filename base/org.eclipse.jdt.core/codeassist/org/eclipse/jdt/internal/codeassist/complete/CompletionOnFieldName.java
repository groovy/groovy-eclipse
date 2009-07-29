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
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

public class CompletionOnFieldName extends FieldDeclaration {
	private static final char[] FAKENAMESUFFIX = " ".toCharArray(); //$NON-NLS-1$
	public char[] realName;
	public CompletionOnFieldName(char[] name, int sourceStart, int sourceEnd) {
		super(CharOperation.concat(name, FAKENAMESUFFIX), sourceStart, sourceEnd);
		this.realName = name;
	}
	
	public StringBuffer printStatement(int tab, StringBuffer output) {
		
		printIndent(tab, output).append("<CompleteOnFieldName:"); //$NON-NLS-1$
		if (type != null) type.print(0, output).append(' ');
		output.append(realName);
		if (initialization != null) {
			output.append(" = "); //$NON-NLS-1$
			initialization.printExpression(0, output); 
		}
		return output.append(">;"); //$NON-NLS-1$
	}	

	public void resolve(MethodScope initializationScope) {
		super.resolve(initializationScope);
		
		throw new CompletionNodeFound(this, initializationScope);
	}
}

