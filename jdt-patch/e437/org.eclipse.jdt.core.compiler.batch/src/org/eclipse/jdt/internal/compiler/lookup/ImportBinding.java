/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;

public class ImportBinding extends Binding {
	public char[][] compoundName;
	public boolean onDemand;
	public ImportReference reference;

	private Binding resolvedImport;

public ImportBinding(char[][] compoundName, boolean isOnDemand, Binding binding, ImportReference reference) {
	this.compoundName = compoundName;
	this.onDemand = isOnDemand;
	this.resolvedImport = binding;
	this.reference = reference;
}
/* API
* Answer the receiver's binding type from Binding.BindingID.
*/

@Override
public final int kind() {
	return IMPORT;
}
public boolean isStatic() {
	return this.reference != null && this.reference.isStatic();
}
public char[] getSimpleName() {
	if (this.reference != null) {
		return this.reference.getSimpleName();
	} else {
		return this.compoundName[this.compoundName.length - 1];
	}
}
@Override
public char[] readableName() {
	if (this.resolvedImport instanceof ModuleBinding)
		return CharOperation.concat("module ".toCharArray(), CharOperation.concatWith(this.compoundName, '.')); //$NON-NLS-1$
	else if (this.onDemand)
		return CharOperation.concat(CharOperation.concatWith(this.compoundName, '.'), ".*".toCharArray()); //$NON-NLS-1$
	else
		return CharOperation.concatWith(this.compoundName, '.');
}
public int getResolvedBindingKind() {
	if (this.resolvedImport == null)
		return 0;
	return this.resolvedImport.kind() & (Binding.TYPE | Binding.FIELD | Binding.METHOD);
}
public Binding getResolvedImport() {
	return this.resolvedImport;
}

public Binding setResolvedImport(Binding resolvedImport) {
	this.resolvedImport = resolvedImport;
	return resolvedImport;
}

@Override
public String toString() {
	return "import : " + new String(readableName()); //$NON-NLS-1$
}
}
