/*******************************************************************************
 * Copyright (c) 2020 Gayan Perera and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gayan Perera - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.RecordComponent;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class CompletionOnRecordComponentName extends RecordComponent implements CompletionNode {

	public CompletionOnRecordComponentName(char[] name, long posNom, TypeReference tr, int modifiers) {
		super(CharOperation.concat(name, FAKENAMESUFFIX), posNom, tr, modifiers);
		this.realName = name;
	}

	private static final char[] FAKENAMESUFFIX = " ".toCharArray(); //$NON-NLS-1$
	public char[] realName;

	@Override
	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output).append("<CompletionOnRecordComponentName:"); //$NON-NLS-1$
		if (this.type != null)
			this.type.print(0, output).append(' ');
		output.append(this.realName);
		if (this.initialization != null) {
			output.append(" = "); //$NON-NLS-1$
			this.initialization.printExpression(0, output);
		}
		return output.append(">;"); //$NON-NLS-1$
	}

	@Override
	public void resolve(BlockScope scope) {
		super.resolve(scope);
		throw new CompletionNodeFound(this, scope);
	}
}
