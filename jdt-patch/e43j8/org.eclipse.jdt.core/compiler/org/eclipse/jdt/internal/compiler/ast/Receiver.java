/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.ast;

public class Receiver extends Argument {
	public NameReference qualifyingName;
	public Receiver(char[] name, long posNom, TypeReference typeReference, NameReference qualifyingName, int modifiers) {
		super(name, posNom, typeReference, modifiers);
		this.qualifyingName = qualifyingName;
	}
	public boolean isReceiver() {
		return true;
	}
	
	public StringBuffer print(int indent, StringBuffer output) {

		printIndent(indent, output);
		printModifiers(this.modifiers, output);

		if (this.type == null) {
			output.append("<no type> "); //$NON-NLS-1$
		} else {
			this.type.print(0, output).append(' ');
		}
		if (this.qualifyingName != null) {
			this.qualifyingName.print(indent, output);
			output.append('.');
		}
		return output.append(this.name);
	}
}
