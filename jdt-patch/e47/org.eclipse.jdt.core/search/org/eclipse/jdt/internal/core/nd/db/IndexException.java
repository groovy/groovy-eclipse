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
package org.eclipse.jdt.internal.core.nd.db;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This exception indicates corruption in the JDT index database.
 */
public class IndexException extends RuntimeException {

	private IStatus status;

	public IndexException(IStatus status) {
		super(status.getMessage());
		this.status = status;
	}

	public IndexException(String message) {
		this(new Status(IStatus.ERROR, "org.eclipse.jdt.core", message)); //$NON-NLS-1$
	}

	@Override
	public synchronized Throwable getCause() {
		return this.status.getException();
	}

	/**
	 * @return the status
	 */
	public IStatus getStatus() {
		return this.status;
	}

	private static final long serialVersionUID = -6561893929558916225L;

}
