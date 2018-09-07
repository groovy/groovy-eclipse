/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.indexer;

import org.eclipse.jdt.core.IJavaElementDelta;

public class IndexerEvent {
	final IJavaElementDelta delta;

	private IndexerEvent(IJavaElementDelta delta) {
		this.delta = delta;
	}

	public static IndexerEvent createChange(IJavaElementDelta delta) {
		return new IndexerEvent(delta);
	}

	public IJavaElementDelta getDelta() {
		return this.delta;
	}
}
