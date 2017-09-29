/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.junit.extension;

/**
 * A test case that is being sent stop() when the user presses 'Stop' or 'Exit'.
 */
public interface StopableTestCase {
	/**
	 * Invoked when this test needs to be stoped.
	 */
	public void stop();
}
