/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom.rewrite;

/**
 * A tracked node position is returned when a rewrite change is
 * requested to be tracked.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see ASTRewrite#track(org.eclipse.jdt.core.dom.ASTNode)
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITrackedNodePosition {

	/**
	 * Returns the original or modified start position of the tracked node depending if called before
	 * or after the rewrite is applied. <code>-1</code> is returned for removed nodes.
	 *
	 * @return the original or modified start position of the tracked node
	 */
	public int getStartPosition();

	/**
	 * Returns the original or modified length of the tracked node depending if called before
	 * or after the rewrite is applied. <code>-1</code> is returned for removed nodes.
	 *
	 * @return the original or modified length of the tracked node
	 */
	public int getLength();


}
