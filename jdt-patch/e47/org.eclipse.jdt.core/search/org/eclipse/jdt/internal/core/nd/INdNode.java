/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

import org.eclipse.jdt.internal.core.nd.db.IndexException;

/**
 * Interface for all nodes that can be visited by a {@link INdVisitor}.
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface INdNode {

	/**
	 * Visits the children of this node.
	 */
	public void accept(INdVisitor visitor) throws IndexException;
}
