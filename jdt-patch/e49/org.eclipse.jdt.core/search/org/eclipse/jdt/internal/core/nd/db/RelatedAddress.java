/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
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
package org.eclipse.jdt.internal.core.nd.db;

import java.util.List;

import org.eclipse.jdt.internal.core.nd.db.ModificationLog.MemoryAccessLog;
import org.eclipse.jdt.internal.core.nd.db.ModificationLog.MemoryOperation;

/**
 * Holds information about a memory range that was related to the cause of data corruption.
 */
public class RelatedAddress {
	private final String description;
	private final long address;
	private final int size;
	private final MemoryAccessLog modificationReport;

	public RelatedAddress(String description, long address, int size, MemoryAccessLog lastModification) {
		this.description = description;
		this.address = address;
		this.size = size;
		this.modificationReport = lastModification;
	}

	boolean isSameAddressAs(RelatedAddress other) {
		return other.address == this.address && other.size == this.size;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.description);
		builder.append(" [address "); //$NON-NLS-1$
		builder.append(this.address);
		builder.append(", size "); //$NON-NLS-1$
		builder.append(this.size);
		builder.append("]: "); //$NON-NLS-1$
		MemoryAccessLog reducedReport = this.modificationReport.reduce(5);
		List<MemoryOperation> operations = reducedReport.getOperations();
		if (operations.isEmpty()) {
			builder.append("No modification report"); //$NON-NLS-1$
		} else {
			builder.append("\n"); //$NON-NLS-1$
			for (MemoryOperation nextOperation : operations) {
				nextOperation.printTo(builder, 1);
			}
		}
		return builder.toString();
	}
}
