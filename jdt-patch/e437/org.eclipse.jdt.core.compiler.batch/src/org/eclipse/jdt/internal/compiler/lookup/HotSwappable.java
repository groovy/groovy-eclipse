/*******************************************************************************
 * Copyright (c) 2025 SSI and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SSI - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.lookup;

interface HotSwappable {
	void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType, LookupEnvironment env);
}
