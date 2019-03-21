// GROOVY PATCHED
/*******************************************************************************
 * Copyright (c) 2015 Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Glassmyer <jogl@google.com> - import group sorting is broken - https://bugs.eclipse.org/430303
 *******************************************************************************/
package org.eclipse.jdt.internal.core.dom.rewrite.imports;

final class ImportDeclarationWriter {
	// GROOVY edit -- change to wrapper type
	private final Boolean insertSpaceBeforeSemicolon;

	ImportDeclarationWriter(Boolean insertSpaceBeforeSemicolon) {
		this.insertSpaceBeforeSemicolon = insertSpaceBeforeSemicolon;
	}

	/**
	 * Writes the Java source for an import declaration of the given name.
	 */
	String writeImportDeclaration(ImportName importName) {
		StringBuilder sb = new StringBuilder();

		sb.append("import "); //$NON-NLS-1$

		if (importName.isStatic) {
			sb.append("static "); //$NON-NLS-1$
		}

		sb.append(importName.qualifiedName);

		// GROOVY add
		if (this.insertSpaceBeforeSemicolon == null)
			return sb.toString();
		// GROOVY end

		if (this.insertSpaceBeforeSemicolon) {
			sb.append(' ');
		}

		sb.append(';');

		return sb.toString();
	}
}
