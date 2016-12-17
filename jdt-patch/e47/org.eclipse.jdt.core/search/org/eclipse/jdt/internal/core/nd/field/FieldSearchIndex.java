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
package org.eclipse.jdt.internal.core.nd.field;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.core.nd.ITypeFactory;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.db.BTree;
import org.eclipse.jdt.internal.core.nd.db.IBTreeComparator;
import org.eclipse.jdt.internal.core.nd.db.IBTreeVisitor;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.db.IndexException;

/**
 * Declares a field representing a case-insensitive search tree over elements which are a subtype of NdNode.
 */
public class FieldSearchIndex<T extends NdNode> implements IField, IDestructableField {
	private int offset;
	private final ITypeFactory<BTree> btreeFactory;
	FieldSearchKey<?> searchKey;
	private static IResultRank anything = new IResultRank() {
		@Override
		public long getRank(Nd nd, long address) {
			return 1;
		}
	};
 
	public static final class SearchCriteria {
		private boolean matchCase = true;
		private boolean isPrefix = false;
		private char[] searchString;
		private short requiredNodeType = -1;
		private boolean matchingParentNodeAddress = false;

		private SearchCriteria(char[] searchString) {
			this.searchString = searchString;
		}

		public static SearchCriteria create(String searchString) {
			return create(searchString.toCharArray());
		}

		public static SearchCriteria create(char[] searchString) {
			return new SearchCriteria(searchString);
		}

		public SearchCriteria requireNodeType(short type) {
			this.requiredNodeType = type;
			return this;
		}

		public SearchCriteria allowAnyNodeType() {
			this.requiredNodeType = -1;
			return this;
		}

		public SearchCriteria matchCase(boolean match) {
			this.matchCase = match;
			return this;
		}

		public SearchCriteria prefix(boolean isPrefixSearch) {
			this.isPrefix = isPrefixSearch;
			return this;
		}
//
//		public SearchCriteria requireParentNode(long parentNameAddress) {
//			this.requiredParentNodeAddress = parentNameAddress;
//			return this;
//		}

		public boolean isMatchingParentNodeAddress() {
			return this.matchingParentNodeAddress;
		}

		public boolean isMatchingCase() {
			return this.matchCase;
		}

		public boolean isPrefixSearch() {
			return this.isPrefix;
		}

		public char[] getSearchString() {
			return this.searchString;
		}
//
//		public long getRequiredParentAddress() {
//			return this.requiredParentNodeAddress;
//		}

		public boolean acceptsNodeType(short nodeType) {
			return this.requiredNodeType == -1 || this.requiredNodeType == nodeType;
		}

		public boolean requiresSpecificNodeType() {
			return this.requiredNodeType != -1;
		}
	}

	public static interface IResultRank {
		public long getRank(Nd nd, long address);
	}

	private abstract class SearchCriteriaToBtreeVisitorAdapter implements IBTreeVisitor {
		private final SearchCriteria searchCriteria;
		private final Nd nd;

		public SearchCriteriaToBtreeVisitorAdapter(SearchCriteria searchCriteria, Nd nd) {
			this.searchCriteria = searchCriteria;
			this.nd = nd;
		}

		@Override
		public int compare(long address) throws IndexException {
			IString key = FieldSearchIndex.this.searchKey.get(this.nd, address);

			if (this.searchCriteria.isPrefixSearch()) {
				return key.comparePrefix(this.searchCriteria.getSearchString(), false);
			} else {
				return key.compareCompatibleWithIgnoreCase(this.searchCriteria.getSearchString());
			}
		}

		@Override
		public boolean visit(long address) throws IndexException {
			if (this.searchCriteria.requiresSpecificNodeType()) {
				short nodeType = NdNode.NODE_TYPE.get(this.nd, address);

				if (!this.searchCriteria.acceptsNodeType(nodeType)) {
					return true;
				}
			}

			IString key = FieldSearchIndex.this.searchKey.get(this.nd, address);

			if (this.searchCriteria.isMatchingCase()) {
				if (this.searchCriteria.isPrefixSearch()) {
					if (key.comparePrefix(this.searchCriteria.getSearchString(), true) != 0) {
						return true;
					}
				} else {
					if (key.compare(this.searchCriteria.getSearchString(), true) != 0) {
						return true;
					}
				}
			}

			return acceptResult(address);
		}

		protected abstract boolean acceptResult(long address);
	}

	private FieldSearchIndex(FieldSearchKey<?> searchKey) {
		this.btreeFactory = BTree.getFactory(new IBTreeComparator() {
			@Override
			public int compare(Nd nd, long record1, long record2) {
				IString key1 = FieldSearchIndex.this.searchKey.get(nd, record1);
				IString key2 = FieldSearchIndex.this.searchKey.get(nd, record2);

				int cmp = key1.compareCompatibleWithIgnoreCase(key2);
				if (cmp == 0) {
					cmp = Long.signum(record1 - record2);
				}

				return cmp;
			}
		});

		if (searchKey != null) {
			if (searchKey.searchIndex != null && searchKey.searchIndex != this) {
				throw new IllegalArgumentException(
					"Attempted to construct a FieldSearchIndex referring to a search key that " //$NON-NLS-1$
					+ "is already in use by a different index"); //$NON-NLS-1$
			}
			searchKey.searchIndex = this;
		}
		this.searchKey = searchKey;
	}

	public static <T extends NdNode, B> FieldSearchIndex<T> create(StructDef<B> builder,
			final FieldSearchKey<B> searchKey) {

		FieldSearchIndex<T> result = new FieldSearchIndex<T>(searchKey);

		builder.add(result);
		builder.addDestructableField(result);

		return result;
	}

	public BTree get(Nd nd, long address) {
		return this.btreeFactory.create(nd, address + this.offset);
	}

	@Override
	public void destruct(Nd nd, long address) {
		this.btreeFactory.destruct(nd, address);
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public int getRecordSize() {
		return this.btreeFactory.getRecordSize();
	}

	public T findFirst(final Nd nd, long address, final SearchCriteria searchCriteria) {
		return findBest(nd, address, searchCriteria, anything);
	}

	@SuppressWarnings("unchecked")
	public T findBest(final Nd nd, long address, final SearchCriteria searchCriteria, final IResultRank rankFunction) {
		final long[] resultRank = new long[1];
		final long[] result = new long[1];
		get(nd, address).accept(new SearchCriteriaToBtreeVisitorAdapter(searchCriteria, nd) {
			@Override
			protected boolean acceptResult(long resultAddress) {
				long rank = rankFunction.getRank(nd, resultAddress);
				if (rank >= resultRank[0]) {
					resultRank[0] = rank;
					result[0] = resultAddress;
				}
				return true;
			}
		});

		if (result[0] == 0) {
			return null;
		}
		return (T)NdNode.load(nd, result[0]);
	}

	public interface Visitor<T> {
		boolean visit(T toVisit);
	}

	public boolean visitAll(final Nd nd, long address, final SearchCriteria searchCriteria, final Visitor<T> visitor) {
		return get(nd, address).accept(new SearchCriteriaToBtreeVisitorAdapter(searchCriteria, nd) {
			@SuppressWarnings("unchecked")
			@Override
			protected boolean acceptResult(long resultAddress) {
				return visitor.visit((T)NdNode.load(nd, resultAddress));
			}
		});
	}

	public List<T> findAll(final Nd nd, long address, final SearchCriteria searchCriteria) {
		final List<T> result = new ArrayList<T>();
		get(nd, address).accept(new SearchCriteriaToBtreeVisitorAdapter(searchCriteria, nd) {
			@SuppressWarnings("unchecked")
			@Override
			protected boolean acceptResult(long resultAddress) {
				result.add((T)NdNode.load(nd, resultAddress));
				return true;
			}
		});

		return result;
	}

	public List<T> findAll(final Nd nd, long address, final SearchCriteria searchCriteria, final int count) {
		final List<T> result = new ArrayList<T>();
		get(nd, address).accept(new SearchCriteriaToBtreeVisitorAdapter(searchCriteria, nd) {

			int remainingCount = count;

			@SuppressWarnings("unchecked")
			@Override
			protected boolean acceptResult(long resultAddress) {
				result.add((T) NdNode.load(nd, resultAddress));
				this.remainingCount--;
				return this.remainingCount > 0;
			}
		});
		return result;
	}

	/**
	 * Returns the entire contents of the index as a single list.
	 */
	public List<T> asList(final Nd nd, long address) {
		final List<T> result = new ArrayList<T>();
		get(nd, address).accept(new IBTreeVisitor() {
			@Override
			public int compare(long record) {
				return 0;
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean visit(long resultAddress) {
				result.add((T)NdNode.load(nd, resultAddress));
				return true;
			}
		});

		return result;
	}
}
