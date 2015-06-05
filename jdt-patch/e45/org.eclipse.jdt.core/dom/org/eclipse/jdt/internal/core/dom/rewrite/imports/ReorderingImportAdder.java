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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Totally sorts new and existing imports together, discarding the order of existing imports.
 */
final class ReorderingImportAdder implements ImportAdder {
	private final Comparator<ImportName> importComparator;

	ReorderingImportAdder(Comparator<ImportName> importComparator) {
		this.importComparator = importComparator;
	}

	@Override
	public List<ImportName> addImports(Collection<ImportName> existingImports, Collection<ImportName> importsToAdd) {
		Set<ImportName> existingImportsSet = new HashSet<ImportName>(existingImports);

		List<ImportName> importsWithAdditions = new ArrayList<ImportName>(existingImports.size() + importsToAdd.size());
		importsWithAdditions.addAll(existingImports);
		for (ImportName importToAdd : importsToAdd) {
			if (!existingImportsSet.contains(importToAdd)) {
				importsWithAdditions.add(importToAdd);
			}
		}

		Collections.sort(importsWithAdditions, this.importComparator);

		return importsWithAdditions;
	}
}
