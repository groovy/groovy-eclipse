/*******************************************************************************
 * Copyright (c) 2015, 2025 Google Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Glassmyer <jogl@google.com> - import group sorting is broken - https://bugs.eclipse.org/430303
 *******************************************************************************/
package org.eclipse.jdt.internal.core.dom.rewrite.imports;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * Encapsulates an import's fully qualified name, whether it is on-demand, and whether it is static.
 * <p>
 * The fully qualified name is divided into two parts:
 * <ul>
 * <li>a container name, which is everything preceding the last dot ('.').
 * <li>a simple name, which is the part following the last dot ("*" for an on-demand import).
 * </ul>
 */
public final class ImportName {
	static ImportName createFor(ImportDeclaration importDeclaration) {
		String declName = importDeclaration.getName().getFullyQualifiedName();
		if (Modifier.isModule(importDeclaration.getModifiers())) {
			return createFor(importDeclaration.isStatic(), Modifier.isModule(importDeclaration.getModifiers()), declName);
		} else if (importDeclaration.isOnDemand()) {
			return createOnDemand(importDeclaration.isStatic(), declName);
		}
		return createFor(importDeclaration.isStatic(), Modifier.isModule(importDeclaration.getModifiers()), declName);
	}

	static ImportName createOnDemand(boolean isStatic, String containerName) {
		return new ImportName(isStatic, false, containerName, "*"); //$NON-NLS-1$
	}

	public static ImportName createFor(boolean isStatic, boolean isModule, String qualifiedName) {
		String containerName = Signature.getQualifier(qualifiedName);
		String simpleName = Signature.getSimpleName(qualifiedName);
		return new ImportName(isStatic, isModule, containerName, simpleName);
	}

	public final boolean isStatic;
	public final boolean isModule;
	public final String containerName;
	public final String simpleName;
	public final String qualifiedName;

	private ImportName(boolean isStatic, boolean isModule, String containerName, String simpleName) {
		this.isStatic = isStatic;
		this.isModule = isModule;
		assert(!(this.isStatic && this.isModule));
		this.containerName = containerName;
		this.simpleName = simpleName;

		this.qualifiedName = containerName.isEmpty() ? simpleName : containerName + "." + simpleName; //$NON-NLS-1$;
	}

	@Override
	public String toString() {
		String template = this.isStatic ? "staticImport(%s)" : this.isModule ? "moduleImport(%s)" : "typeImport(%s)"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return String.format(template, this.qualifiedName);
	}

	@Override
	public int hashCode() {
		int result = this.qualifiedName.hashCode();
		result = 31 * result + (this.isStatic ? 1 : 0) + (this.isModule ? 3 : 0);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ImportName)) {
			return false;
		}

		ImportName other = (ImportName) obj;

		return this.qualifiedName.equals(other.qualifiedName) && this.isStatic == other.isStatic && this.isModule == other.isModule;
	}

	public boolean isOnDemand() {
		return this.simpleName.equals("*"); //$NON-NLS-1$
	}

	/**
	 * Returns an on-demand ImportName with the same isStatic and containerName as this ImportName.
	 */
	ImportName getContainerOnDemand() {
		if (this.isOnDemand()) {
			return this;
		}

		return ImportName.createOnDemand(this.isStatic, this.containerName);
	}
}