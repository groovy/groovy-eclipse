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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This exception indicates corruption in the JDT index database.
 */
public class IndexException extends RuntimeException {

	private IStatus status;
	private List<RelatedAddress> relatedAddresses = new ArrayList<>();
	private long time = -1;

	public IndexException(IStatus status) {
		this.status = status;
	}

	public IndexException(String message) {
		this(new Status(IStatus.ERROR, "org.eclipse.jdt.core", message)); //$NON-NLS-1$
	}

	/**
	 * Sets the time that the exception occurred at (in terms of the write number
	 * from the modification log)
	 * 
	 * @param writeNumber 
	 */
	public void setTime(long writeNumber) {
		this.time = writeNumber;
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

	public void addRelatedAddress(RelatedAddress related) {
		// Don't include dupes
		for (RelatedAddress next : this.relatedAddresses) {
			if (next.isSameAddressAs(related)) {
				return;
			}
		}
		this.relatedAddresses.add(related);
	}

	@Override
	public String getMessage() {
		StringBuilder result = new StringBuilder();
		if (this.time != -1) {
			result.append("(time "); //$NON-NLS-1$
			result.append(this.time);
			result.append(") "); //$NON-NLS-1$
		}
		result.append(this.status.getMessage());

		if (!this.relatedAddresses.isEmpty()) {
			boolean isFirst = true;
			result.append("\nRelated addresses:\n"); //$NON-NLS-1$
			for (RelatedAddress next : this.relatedAddresses) {
				if (!isFirst) {
					result.append("\n"); //$NON-NLS-1$
				}
				isFirst = false;
				result.append(next.toString());
			}
		}
		return result.toString();
	}
}
