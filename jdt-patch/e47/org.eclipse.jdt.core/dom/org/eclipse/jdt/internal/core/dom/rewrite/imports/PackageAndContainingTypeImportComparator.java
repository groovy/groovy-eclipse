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

import java.util.Comparator;

/**
 * Sorts imports according to a lexicographic comparison of their full container names (including
 * package name and any containing type names).
 * <p>
 * The alternative is {@link PackageImportComparator}. See https://bugs.eclipse.org/194358.
 */
final class PackageAndContainingTypeImportComparator implements Comparator<ImportName> {
	@Override
	public int compare(ImportName o1, ImportName o2) {
		return o1.containerName.compareTo(o2.containerName);
	}
}
