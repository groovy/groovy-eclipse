/*******************************************************************************
 * Copyright (c) 2014 Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Glassmyer <jogl@google.com> - import group sorting is broken - https://bugs.eclipse.org/430303
 *******************************************************************************/
package org.eclipse.jdt.internal.core.dom.rewrite.imports;

import java.util.Collection;
import java.util.List;

interface ImportAdder {
	/**
	 * Returns a new list containing the elements of {@code existingImports} and also containing
	 * each element of {@code importsToAdd} for which {@code existingImports} does not contain an
	 * equal element.
	 */
	List<ImportName> addImports(Collection<ImportName> existingImports, Collection<ImportName> importsToAdd);
}
