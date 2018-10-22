/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.junit.extension;

import junit.framework.Test;
import junit.framework.TestFailure;
/**
 * A Listener for test progress
 */
public interface TestListener extends junit.framework.TestListener {
   /**
 	* An error occurred.
 	*/
	public void addError(Test test, TestFailure testFailure);
   /**
 	* A failure occurred.
 	*/
 	public void addFailure(Test test, TestFailure testFailure);
}
