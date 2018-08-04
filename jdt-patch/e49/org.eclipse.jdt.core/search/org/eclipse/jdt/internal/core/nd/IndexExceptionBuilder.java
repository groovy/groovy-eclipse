/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.core.nd.db.ModificationLog.MemoryAccessLog;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.IndexException;
import org.eclipse.jdt.internal.core.nd.db.RelatedAddress;
import org.eclipse.jdt.internal.core.nd.field.IField;

/**
 * Given a set of memory ranges, this class constructs detailed error messages.
 */
public final class IndexExceptionBuilder {
	private final Database db;
	private final List<RelatedAddress> relatedAddresses = new ArrayList<>();

	/**
	 * Constructs a new {@link IndexExceptionBuilder}
	 */
	public IndexExceptionBuilder(Database db) {
		this.db = db;
	}

	/**
	 * Adds an address range to this problem description, given the first address that may be corrupt,
	 * the size of the possibly-corrupt address range, and a custom description for the memory at this
	 * address range.
	 */
	public IndexExceptionBuilder addProblemAddress(String description, long dataBlockAddress, int rangeSize) {
		MemoryAccessLog lastWrite = this.db.getLog().getReportFor(dataBlockAddress, rangeSize);
		this.relatedAddresses.add(new RelatedAddress(description, dataBlockAddress, rangeSize, lastWrite));
		return this;
	}

	/**
	 * Adds an address range to this problem description, given a field that may be corrupt, the base
	 * address for its struct, and a custom description for the field.
	 * 
	 * @return this
	 */
	public IndexExceptionBuilder addProblemAddress(String description, IField field, long address) {
		long offset = field.getOffset();
		int size = field.getRecordSize();
		return addProblemAddress(description, address + offset, size);
	}

	/**
	 * Adds an address range to this problem description, given the field that may be corrupt
	 * and the base address for its struct.
	 * 
	 * @return this
	 */
	public IndexExceptionBuilder addProblemAddress(IField field, long address) {
		return addProblemAddress(field.getFieldName(), field, address);
	}

	/**
	 * Returns a newly constructed {@link IndexException} containing the given message and all the addresses collected
	 * by this object.
	 */
	public IndexException build(String description) {
		IndexException toThrow = new IndexException(description);
		if (this.db.getLog().enabled()) {
			toThrow.setTime(this.db.getLog().getWriteCount());
		}
		attachTo(toThrow);
		return toThrow;
	}

	/**
	 * Attaches the addresses collected by the receiver to the given exception.
	 */
	public void attachTo(IndexException exception) {
		for (RelatedAddress next : this.relatedAddresses) {
			exception.addRelatedAddress(next);
		}
	}
}
