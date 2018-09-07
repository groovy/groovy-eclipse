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
package org.eclipse.jdt.internal.core.nd;

import org.eclipse.jdt.internal.core.nd.db.IndexException;

public final class NdLinkedList<T> {
	private final NdRawLinkedList rawList;
	final ITypeFactory<T> elementFactory;

	public static interface ILinkedListVisitor<T> {
		public void visit(T record, short metadataBits, int index) throws IndexException;
	}

	public NdLinkedList(Nd nd, long address, ITypeFactory<T> elementFactory, int recordsInFirstBlock,
			int recordsInSubsequentBlocks) {
		this(nd, address, elementFactory, recordsInFirstBlock, recordsInSubsequentBlocks, 0);
	}

	public NdLinkedList(Nd nd, long address, ITypeFactory<T> elementFactory, int recordsInFirstBlock,
			int recordsInSubsequentBlocks, int metadataBitsPerElement) {
		this.rawList = new NdRawLinkedList(nd, address, elementFactory.getRecordSize(), recordsInFirstBlock,
				recordsInSubsequentBlocks, metadataBitsPerElement);
		this.elementFactory = elementFactory;
	}

	/**
	 * Computes the size of this list. This is an O(n) operation.
	 *
	 * @return the size of this list
	 * @throws IndexException
	 */
	public int size() throws IndexException {
		return this.rawList.size();
	}

	public T addMember(short metadataBits) throws IndexException {
		long address = this.rawList.addMember(metadataBits);

		return this.elementFactory.create(this.rawList.getNd(), address);
	}

	public void accept(final ILinkedListVisitor<T> visitor) throws IndexException {
		final NdRawLinkedList localRawList = this.rawList;
		final ITypeFactory<T> localElementFactory = this.elementFactory;
		localRawList.accept(new NdRawLinkedList.ILinkedListVisitor() {
			@Override
			public void visit(long address, short metadataBits, int index) throws IndexException {
				visitor.visit(localElementFactory.create(localRawList.getNd(),
						address), metadataBits, index);
			}
		});
	}

	public static <T> ITypeFactory<NdLinkedList<T>> getFactoryFor(
			final ITypeFactory<T> elementFactory, final int recordsInFirstBlock, final int recordsInSubsequentBlocks) {
		return getFactoryFor(elementFactory, recordsInFirstBlock, recordsInSubsequentBlocks, 0);
	}

	public static <T> ITypeFactory<NdLinkedList<T>> getFactoryFor(
			final ITypeFactory<T> elementFactory, final int recordsInFirstBlock, final int recordsInSubsequentBlocks,
			final int metadataBitsPerElement) {

		return new AbstractTypeFactory<NdLinkedList<T>>() {
			@Override
			public NdLinkedList<T> create(Nd dom, long address) {
				return new NdLinkedList<T>(dom, address, elementFactory, recordsInFirstBlock, recordsInSubsequentBlocks, metadataBitsPerElement);
			}

			@Override
			public int getRecordSize() {
				return NdRawLinkedList.recordSize(elementFactory.getRecordSize(), recordsInFirstBlock,
						metadataBitsPerElement);
			}

			@Override
			public Class<?> getElementClass() {
				return NdLinkedList.class;
			}

			@Override
			public boolean hasDestructor() {
				return true;
			}

			@Override
			public void destructFields(Nd dom, long address) {
				create(dom, address).destruct();
			}

			@Override
			public void destruct(Nd dom, long address) {
				destructFields(dom, address);
			}
		};
	}

	/**
	 *
	 */
	protected void destruct() {
		if (this.elementFactory.hasDestructor()) {
			final Nd nd = this.rawList.getNd();
			this.rawList.accept(new NdRawLinkedList.ILinkedListVisitor() {
				@Override
				public void visit(long address, short metadataBits, int index) throws IndexException {
					NdLinkedList.this.elementFactory.destruct(nd, address);
				}
			});
		}
		this.rawList.destruct();
	}
}
