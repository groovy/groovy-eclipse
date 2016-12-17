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
 * Represents an import declaration that did not originally occur in the compilation unit.
 */
class NewImportEntry extends ImportEntry {
	NewImportEntry(ImportName importName) {
		super(importName);
	}

	@Override
	public String toString() {
		return String.format("NewImportEntry(%s)", this.importName); //$NON-NLS-1$
	}

	@Override
	boolean isOriginal() {
		return false;
	}

	@Override
	OriginalImportEntry asOriginalImportEntry() {
		throw new UnsupportedOperationException();
	}
}