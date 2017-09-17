/*******************************************************************************
 * Copyright (c) 2015 Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Glassmyer <jogl@google.com> - import group sorting is broken - https://bugs.eclipse.org/430303
 *******************************************************************************/
package org.eclipse.jdt.internal.core.dom.rewrite.imports;

/**
 * Describes an import declaration, encapsulating the imported name and, for an import declaration
 * originally present in the compilation unit, the regions originally occupied by the import
 * declaration and its associated comments.
 * <p>
 * As the Java Language Specification allows duplicate import declarations, a compilation unit
 * may contain multiple {@code ImportEntry}s with equal {@code ImportName}s.
 */
abstract class ImportEntry {
	final ImportName importName;

	protected ImportEntry(ImportName importName) {
		this.importName = importName;
	}

	/**
	 * Returns true if this import declaration occurred originally (before the rewrite).
	 */
	abstract boolean isOriginal();

	/**
	 * If this import declaration occurred originally, returns it as an OriginalImportEntry;
	 * otherwise throws an exception.
	 */
	abstract OriginalImportEntry asOriginalImportEntry();
}
