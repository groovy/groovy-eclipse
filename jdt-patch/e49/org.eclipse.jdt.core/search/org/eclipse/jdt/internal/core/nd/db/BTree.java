/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Andrew Ferguson (Symbian) - Provide B-tree deletion routine
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.db;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.nd.AbstractTypeFactory;
import org.eclipse.jdt.internal.core.nd.ITypeFactory;
import org.eclipse.jdt.internal.core.nd.Nd;

/**
 * Implements B-Tree search structure.
 */
public class BTree {
	private static final int DEFAULT_DEGREE = 8;
	// Constants for internal deletion routine (see deleteImp doc).
	private static final int DELMODE_NORMAL = 0;
	private static final int DELMODE_DELETE_MINIMUM = 1;
	private static final int DELMODE_DELETE_MAXIMUM = 2;

	public static final int RECORD_SIZE = Database.PTR_SIZE;

	private final Nd nd;
	protected final Database db;
	protected final long rootPointer;

	protected final int degree;
	protected final int maxRecords;
	protected final int maxChildren;
	protected final int minRecords;
	protected final int offsetChildren;
	protected final int medianRecord;

	protected final IBTreeComparator cmp;

	public BTree(Nd nd, long rootPointer, IBTreeComparator cmp) {
		this(nd, rootPointer, DEFAULT_DEGREE, cmp);
	}

	/**
	 * Constructor.
	 *
	 * @param nd the database containing the btree
	 * @param rootPointer offset into database of the pointer to the root node
	 */
	public BTree(Nd nd, long rootPointer, int degree, IBTreeComparator cmp) {
		this.nd = nd;
		if (degree < 2)
			throw new IllegalArgumentException("Illegal degree " + degree + " in tree"); //$NON-NLS-1$ //$NON-NLS-2$

		this.db = nd.getDB();
		this.rootPointer = rootPointer;
		this.cmp = cmp;

		this.degree = degree;
		this.minRecords = this.degree - 1;
		this.maxRecords = 2*this.degree - 1;
		this.maxChildren = 2*this.degree;
		this.offsetChildren = this.maxRecords * Database.INT_SIZE;
		this.medianRecord = this.degree - 1;
	}

	public static ITypeFactory<BTree> getFactory(final IBTreeComparator cmp) {
		return getFactory(8, cmp);
	}

	public static ITypeFactory<BTree> getFactory(final int degree, final IBTreeComparator cmp) {
		return new AbstractTypeFactory<BTree>() {
			@Override
			public BTree create(Nd dom, long address) {
				return new BTree(dom, address, degree, cmp);
			}

			@Override
			public int getRecordSize() {
				return RECORD_SIZE;
			}

			@Override
			public Class<?> getElementClass() {
				return BTree.class;
			}

			@Override
			public void destruct(Nd dom, long address) {
				destructFields(dom, address);
			}

			@Override
			public void destructFields(Nd dom, long address) {
				create(dom, address).destruct();
			}
		};
	}

	protected long getRoot() throws IndexException {
		return this.db.getRecPtr(this.rootPointer);
	}

	protected final void putRecord(Chunk chunk, long node, int index, long record) {
		chunk.putRecPtr(node + index * Database.INT_SIZE, record);
	}

	protected final long getRecord(Chunk chunk, long node, int index) {
		return chunk.getRecPtr(node + index * Database.INT_SIZE);
	}

	protected final void putChild(Chunk chunk, long node, int index, long child) {
		chunk.putRecPtr(node + this.offsetChildren + index * Database.INT_SIZE, child);
	}

	protected final long getChild(Chunk chunk, long node, int index) {
		return chunk.getRecPtr(node + this.offsetChildren + index * Database.INT_SIZE);
	}

	public void destruct() {
		long root = getRoot();

		if (root == 0) {
			return;
		}

		deallocateChildren(root);
	}

	private void deallocateChildren(long record) {
		Chunk chunk = this.db.getChunk(record);

		// Copy all the children pointers to an array of longs so all the reads will happen on the same chunk
		// consecutively
		long[] children = new long[this.maxRecords + 1];

		for (int idx = 0; idx < children.length; idx++) {
			children[idx] = getChild(chunk, record, idx);
		}

		this.db.free(record, Database.POOL_BTREE);

		chunk = null;

		for (long nextChild : children) {
			if (nextChild != 0) {
				deallocateChildren(nextChild);
			}
		}
	}

	/**
	 * Inserts the record into the b-tree. We don't insert if the key was already there,
	 * in which case we return the record that matched. In other cases, we just return
	 * the record back.
	 *
	 * @param record  offset of the record
	 */
	public long insert(long record) throws IndexException {
		long root = getRoot();

		// Is this our first time in.
		if (root == 0) {
			firstInsert(record);
			return record;
		}

		return insert(null, 0, 0, root, record);
	}

	private long insert(Chunk pChunk, long parent, int iParent, long node, long record) throws IndexException {
		Chunk chunk = this.db.getChunk(node);

		// If this node is full (last record isn't null), split it.
		if (getRecord(chunk, node, this.maxRecords - 1) != 0) {
			long median = getRecord(chunk, node, this.medianRecord);
			if (median == record) {
				// Found it, never mind.
				return median;
			} else {
				chunk.makeDirty();
				// Split it.
				// Create the new node and move the larger records over.
				long newnode = allocateNode();
				Chunk newchunk = this.db.getChunk(newnode);
				for (int i = 0; i < this.medianRecord; ++i) {
					putRecord(newchunk, newnode, i, getRecord(chunk, node, this.medianRecord + 1 + i));
					putRecord(chunk, node, this.medianRecord + 1 + i, 0);
					putChild(newchunk, newnode, i, getChild(chunk, node, this.medianRecord + 1 + i));
					putChild(chunk, node, this.medianRecord + 1 + i, 0);
				}
				putChild(newchunk, newnode, this.medianRecord, getChild(chunk, node, this.maxRecords));
				putChild(chunk, node, this.maxRecords, 0);

				if (parent == 0) {
					// Create a new root
					parent = allocateNode();
					pChunk = this.db.getChunk(parent);
					this.db.putRecPtr(this.rootPointer, parent);
					putChild(pChunk, parent, 0, node);
				} else {
					// Insert the median into the parent.
					for (int i = this.maxRecords - 2; i >= iParent; --i) {
						long r = getRecord(pChunk, parent, i);
						if (r != 0) {
							// Re-fetch pChunk since we can only dirty the page that was fetched most recently from
							// the database (anything fetched earlier may have been paged out)
							pChunk = pChunk.getWritableChunk();
							putRecord(pChunk, parent, i + 1, r);
							putChild(pChunk, parent, i + 2, getChild(pChunk, parent, i + 1));
						}
					}
				}
				pChunk = pChunk.getWritableChunk();
				putRecord(pChunk, parent, iParent, median);
				putChild(pChunk, parent, iParent + 1, newnode);

				putRecord(chunk, node, this.medianRecord, 0);

				// Set the node to the correct one to follow.
				if (this.cmp.compare(this.nd, record, median) > 0) {
					node = newnode;
					chunk = newchunk;
				}
			}
		}

		// Binary search to find the insert point.
		int lower= 0;
		int upper= this.maxRecords - 1;
		while (lower < upper && getRecord(chunk, node, upper - 1) == 0) {
			upper--;
		}

		while (lower < upper) {
			int middle= (lower + upper) / 2;
			long checkRec= getRecord(chunk, node, middle);
			if (checkRec == 0) {
				upper= middle;
			} else {
				int compare= this.cmp.compare(this.nd, checkRec, record);
				if (compare > 0) {
					upper= middle;
				} else if (compare < 0) {
					lower= middle + 1;
				} else {
					// Found it, no insert, just return the matched record.
					return checkRec;
				}
			}
		}

		// Note that the call to compare, above, may have paged out and reallocated the chunk so fetch it again now.
		chunk = this.db.getChunk(node);
		final int i= lower;
		long child = getChild(chunk, node, i);
		if (child != 0) {
			// Visit the children.
			return insert(chunk, node, i, child, record);
		} else {
			// We are at the leaf, add us in.
			// First copy everything after over one.
			for (int j = this.maxRecords - 2; j >= i; --j) {
				long r = getRecord(chunk, node, j);
				if (r != 0)
					putRecord(chunk, node, j + 1, r);
			}
			putRecord(chunk, node, i, record);
			return record;
		}
	}

	private void firstInsert(long record) throws IndexException {
		// Create the node and save it as root.
		long root = allocateNode();
		this.db.putRecPtr(this.rootPointer, root);
		// Put the record in the first slot of the node.
		putRecord(this.db.getChunk(root), root, 0, record);
	}

	private long allocateNode() throws IndexException {
		return this.db.malloc((2 * this.maxRecords + 1) * Database.INT_SIZE, Database.POOL_BTREE);
	}

	/**
	 * Deletes the specified record from the B-tree.
	 * <p>
	 * If the specified record is not present then this routine has no effect.
	 * <p>
	 * Specifying a record r for which there is another record q existing in the B-tree
	 * where cmp.compare(r,q)==0 && r!=q will also have no effect
	 * <p>
	 * N.B. The record is not deleted itself - its storage is not deallocated.
	 * The reference to the record in the btree is deleted.
	 *
	 * @param record the record to delete
	 * @throws IndexException
	 */
	public void delete(long record) throws IndexException {
		try {
			deleteImp(record, getRoot(), DELMODE_NORMAL);
		} catch (BTreeKeyNotFoundException e) {
			// Contract of this method is to NO-OP upon this event.
		}
	}

	private class BTreeKeyNotFoundException extends Exception {
		private static final long serialVersionUID = 9065438266175091670L;
		public BTreeKeyNotFoundException(String msg) {
			super(msg);
		}
	}

	/**
	 * Used in implementation of delete routines
	 */
	private class BTNode {
		final long node;
		final int keyCount;
		Chunk chunk;

		BTNode(long node) throws IndexException {
			this.node = node;
			this.chunk = BTree.this.db.getChunk(node);
			int i= 0;
			while (i < BTree.this.maxRecords && getRecord(this.chunk, node, i) != 0)
				i++;
			this.keyCount = i;
		}

		BTNode getChild(int index) throws IndexException {
			if (0 <= index && index < BTree.this.maxChildren) {
				long child = BTree.this.getChild(this.chunk, this.node, index);
				if (child != 0)
					return new BTNode(child);
			}
			return null;
		}

		public void makeWritable() {
			this.chunk = this.chunk.getWritableChunk();
		}
	}

	/**
	 * Implementation for deleting a key/record from the B-tree.
	 * <p>
	 * There is no distinction between keys and records.
	 * <p>
	 * This implements a single downward pass (with minor exceptions) deletion
	 * <p>
	 * @param key the address of the record to delete
	 * @param nodeRecord a node that (directly or indirectly) contains the specified key/record
	 * @param mode one of DELMODE_NORMAL, DELMODE_DELETE_MINIMUM, DELMODE_DELETE_MAXIMUM
	 * 	where DELMODE_NORMAL: locates the specified key/record using the comparator provided
	 *        DELMODE_DELETE_MINIMUM: locates and deletes the minimum element in the subtree rooted at nodeRecord
	 *        DELMODE_DELETE_MAXIMUM: locates and deletes the maximum element in the subtree rooted at nodeRecord
	 * @return the address of the record removed from the B-tree
	 * @throws IndexException
	 */
	private long deleteImp(long key, long nodeRecord, int mode)
	throws IndexException, BTreeKeyNotFoundException {
		BTNode node = new BTNode(nodeRecord);

		// Determine index of key in current node, or -1 if its not in this node.
		int keyIndexInNode = -1;
		if (mode == DELMODE_NORMAL)
			for (int i= 0; i < node.keyCount; i++)
				if (getRecord(node.chunk, node.node, i) == key) {
					keyIndexInNode = i;
					break;
				}

		if (getChild(node.chunk, node.node, 0) == 0) {
			/* Case 1: leaf node containing the key (by method precondition) */
			if (keyIndexInNode != -1) {
				nodeContentDelete(node, keyIndexInNode, 1);
				return key;
			} else {
				if (mode == DELMODE_DELETE_MINIMUM) {
					long subst = getRecord(node.chunk, node.node, 0);
					nodeContentDelete(node, 0, 1);
					return subst;
				} else if (mode == DELMODE_DELETE_MAXIMUM) {
					long subst = getRecord(node.chunk, node.node, node.keyCount - 1);
					nodeContentDelete(node, node.keyCount - 1, 1);
					return subst;
				}
				throw new BTreeKeyNotFoundException("Deletion on absent key " + key + ", mode = " + mode);  //$NON-NLS-1$//$NON-NLS-2$
			}
		} else {
			if (keyIndexInNode != -1) {
				/* Case 2: non-leaf node which contains the key itself */

				BTNode succ = node.getChild(keyIndexInNode + 1);
				if (succ != null && succ.keyCount > this.minRecords) {
					node.makeWritable();
					/* Case 2a: Delete key by overwriting it with its successor (which occurs in a leaf node) */
					long subst = deleteImp(-1, succ.node, DELMODE_DELETE_MINIMUM);
					putRecord(node.chunk, node.node, keyIndexInNode, subst);
					return key;
				}

				BTNode pred = node.getChild(keyIndexInNode);
				if (pred != null && pred.keyCount > this.minRecords) {
					node.makeWritable();
					/* Case 2b: Delete key by overwriting it with its predecessor (which occurs in a leaf node) */
					long subst = deleteImp(-1, pred.node, DELMODE_DELETE_MAXIMUM);
					putRecord(node.chunk, node.node, keyIndexInNode, subst);
					return key;
				}

				/* Case 2c: Merge successor and predecessor */
				// assert(pred != null && succ != null);
				if (pred != null) {
					succ.makeWritable();
					node.makeWritable();
					pred.makeWritable();
					mergeNodes(succ, node, keyIndexInNode, pred);
					return deleteImp(key, pred.node, mode);
				}
				return key;
			} else {
				/* Case 3: non-leaf node which does not itself contain the key */

				/* Determine root of subtree that should contain the key */
				int subtreeIndex;
				switch(mode) {
				case DELMODE_NORMAL:
					subtreeIndex = node.keyCount;
					for (int i= 0; i < node.keyCount; i++)
						if (this.cmp.compare(this.nd, getRecord(node.chunk, node.node, i), key)>0) {
							subtreeIndex = i;
							break;
						}
					break;
				case DELMODE_DELETE_MINIMUM: subtreeIndex = 0; break;
				case DELMODE_DELETE_MAXIMUM: subtreeIndex = node.keyCount; break;
				default: throw new IndexException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, IStatus.OK, "Unknown delete mode " + mode, null)); //$NON-NLS-1$
				}

				BTNode child = node.getChild(subtreeIndex);
				if (child == null) {
					throw new IndexException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, IStatus.OK,
							"BTree integrity error (null child found)", null)); //$NON-NLS-1$
				}

				if (child.keyCount > this.minRecords) {
					return deleteImp(key, child.node, mode);
				} else {
					child.makeWritable();
					node.makeWritable();
					BTNode sibR = node.getChild(subtreeIndex + 1);
					if (sibR != null && sibR.keyCount > this.minRecords) {
						sibR.makeWritable();
						/* Case 3a (i): child will underflow upon deletion, take a key from rightSibling */
						long rightKey = getRecord(node.chunk, node.node, subtreeIndex);
						long leftmostRightSiblingKey = getRecord(sibR.chunk, sibR.node, 0);
						append(child, rightKey, getChild(sibR.chunk, sibR.node, 0));
						nodeContentDelete(sibR, 0, 1);
						putRecord(node.chunk, node.node, subtreeIndex, leftmostRightSiblingKey);
						return deleteImp(key, child.node, mode);
					}

					BTNode sibL = node.getChild(subtreeIndex - 1);
					if (sibL != null && sibL.keyCount > this.minRecords) {
						sibL.makeWritable();
						/* Case 3a (ii): child will underflow upon deletion, take a key from leftSibling */
						long leftKey = getRecord(node.chunk, node.node, subtreeIndex - 1);
						prepend(child, leftKey, getChild(sibL.chunk, sibL.node, sibL.keyCount));
						long rightmostLeftSiblingKey = getRecord(sibL.chunk, sibL.node, sibL.keyCount - 1);
						putRecord(sibL.chunk, sibL.node, sibL.keyCount - 1, 0);
						putChild(sibL.chunk, sibL.node, sibL.keyCount, 0);
						putRecord(node.chunk, node.node, subtreeIndex - 1, rightmostLeftSiblingKey);
						return deleteImp(key, child.node, mode);
					}

					/* Case 3b (i,ii): leftSibling, child, rightSibling all have minimum number of keys */

					if (sibL != null) { // merge child into leftSibling
						mergeNodes(child, node, subtreeIndex - 1, sibL);
						return deleteImp(key, sibL.node, mode);
					}

					if (sibR != null) { // merge rightSibling into child
						mergeNodes(sibR, node, subtreeIndex, child);
						return deleteImp(key, child.node, mode);
					}

					throw new BTreeKeyNotFoundException(
							MessageFormat.format("Deletion of key not in btree: {0} mode={1}", //$NON-NLS-1$
									new Object[]{new Long(key), new Integer(mode)}));
				}
			}
		}
	}

	/**
	 * Merge node 'src' onto the right side of node 'dst' using node
	 * 'keyProvider' as the source of the median key. Bounds checking is not
	 * performed.
	 * @param src the key to merge into dst
	 * @param keyProvider the node that provides the median key for the new node
	 * @param kIndex the index of the key in the node <i>mid</i> which is to become the new node's median key
	 * @param dst the node which is the basis and result of the merge
	 */
	public void mergeNodes(BTNode src, BTNode keyProvider, int kIndex, BTNode dst)
	throws IndexException {
		nodeContentCopy(src, 0, dst, dst.keyCount + 1, src.keyCount + 1);
		long midKey = getRecord(keyProvider.chunk, keyProvider.node, kIndex);
		putRecord(dst.chunk, dst.node, dst.keyCount, midKey);
		long keySucc = kIndex + 1 == this.maxRecords ? 0 : getRecord(keyProvider.chunk, keyProvider.node, kIndex + 1);
		this.db.free(getChild(keyProvider.chunk, keyProvider.node,  kIndex + 1), Database.POOL_BTREE);
		nodeContentDelete(keyProvider, kIndex + 1, 1);
		putRecord(keyProvider.chunk, keyProvider.node, kIndex, keySucc);
		if (kIndex == 0 && keySucc == 0) {
			/*
			 * The root node is excused from the property that a node must have a least MIN keys
			 * This means we must special case it at the point when its had all of its keys deleted
			 * entirely during merge operations (which push one of its keys down as a pivot)
			 */
			long rootNode = getRoot();
			if (rootNode == keyProvider.node) {
				this.db.putRecPtr(this.rootPointer, dst.node);
				this.db.free(rootNode, Database.POOL_BTREE);
			}
		}
	}

	/**
	 * Insert the key and (its predecessor) child at the left side of the specified node. Bounds checking
	 * is not performed.
	 * @param node the node to prepend to
	 * @param key the new leftmost (least) key
	 * @param child the new leftmost (least) subtree root
	 */
	private void prepend(BTNode node, long key, long child) {
		nodeContentCopy(node, 0, node, 1, node.keyCount + 1);
		putRecord(node.chunk, node.node, 0, key);
		putChild(node.chunk, node.node, 0, child);
	}

	/**
	 * Insert the key and (its successor) child at the right side of the specified node. Bounds
	 * checking is not performed.
	 * @param node
	 * @param key
	 * @param child
	 */
	private void append(BTNode node, long key, long child) {
		putRecord(node.chunk, node.node, node.keyCount, key);
		putChild(node.chunk, node.node, node.keyCount + 1, child);
	}

	/**
	 * Overwrite a section of the specified node (dst) with the specified section of the source
	 * node. Bounds checking is not performed. To allow just copying of the final child (which has
	 * no corresponding key) the routine behaves as though there were a corresponding key existing
	 * with value zero.<p>
	 * Copying from a node to itself is permitted.
	 * @param src the node to read from
	 * @param srcPos the initial index to read from (inclusive)
	 * @param dst the node to write to
	 * @param dstPos the initial index to write to (inclusive)
	 * @param length the number of (key,(predecessor)child) nodes to write
	 */
	private void nodeContentCopy(BTNode src, int srcPos, BTNode dst, int dstPos, int length) {
		for (int i=length - 1; i >= 0; i--) { // this order is important when src == dst!
			int srcIndex = srcPos + i;
			int dstIndex = dstPos + i;

			if (srcIndex < src.keyCount + 1) {
				long srcChild = getChild(src.chunk, src.node, srcIndex);
				putChild(dst.chunk, dst.node, dstIndex, srcChild);

				if (srcIndex < src.keyCount) {
					long srcKey = getRecord(src.chunk, src.node, srcIndex);
					putRecord(dst.chunk, dst.node, dstIndex, srcKey);
				}
			}
		}
	}

	/**
	 * Delete a section of node content - (key, (predecessor)child) pairs. Bounds checking
	 * is not performed. To allow deletion of the final child (which has no corresponding key)
	 * the routine behaves as though there were a corresponding key existing with value zero.<p>
	 * Content is deleted and remaining content is moved leftward the appropriate amount.
	 * @param node the node to delete content from
	 * @param i the start index (inclusive) to delete from
	 * @param length the length of the sequence to delete
	 */
	private void nodeContentDelete(BTNode node, int i, int length) {
		for (int index= i; index <= this.maxRecords; index++) {
			long newKey = (index + length) < node.keyCount ? getRecord(node.chunk, node.node, index + length) : 0;
			long newChild = (index + length) < node.keyCount + 1 ? getChild(node.chunk, node.node, index + length) : 0;
			if (index < this.maxRecords) {
				putRecord(node.chunk, node.node, index, newKey);
			}
			if (index < this.maxChildren) {
				putChild(node.chunk, node.node, index, newChild);
			}
		}
	}

	/**
	 * Visit all nodes beginning when the visitor comparator
	 * returns >= 0 until the visitor visit returns falls.
	 *
	 * @param visitor
	 */
	public boolean accept(IBTreeVisitor visitor) throws IndexException {
		return accept(this.db.getRecPtr(this.rootPointer), visitor);
	}

	private boolean accept(long node, IBTreeVisitor visitor) throws IndexException {
		// If found is false, we are still in search mode.
		// Once found is true visit everything.
		// Return false when ready to quit.

		if (node == 0) {
			return true;
		}
		if (visitor instanceof IBTreeVisitor2) {
			((IBTreeVisitor2) visitor).preNode(node);
		}

		try {
			Chunk chunk = this.db.getChunk(node);

			// Binary search to find first record greater or equal.
			int lower= 0;
			int upper= this.maxRecords - 1;
			while (lower < upper && getRecord(chunk, node, upper - 1) == 0) {
				upper--;
			}
			while (lower < upper) {
				int middle= (lower + upper) / 2;
				long checkRec = getRecord(chunk, node, middle);
				if (checkRec == 0) {
					upper= middle;
				} else {
					int compare= visitor.compare(checkRec);
					if (compare >= 0) {
						upper= middle;
					} else {
						lower= middle + 1;
					}
				}
			}

			// Start with first record greater or equal, reuse comparison results.
			int i= lower;
			for (; i < this.maxRecords; ++i) {
				long record = getRecord(chunk, node, i);
				if (record == 0)
					break;

				int compare= visitor.compare(record);
				if (compare > 0) {
					// Start point is to the left.
					return accept(getChild(chunk, node, i), visitor);
				}  else if (compare == 0) {
					if (!accept(getChild(chunk, node, i), visitor))
						return false;
					if (!visitor.visit(record))
						return false;
				}
			}
			return accept(getChild(chunk, node, i), visitor);
		} finally {
			if (visitor instanceof IBTreeVisitor2) {
				((IBTreeVisitor2) visitor).postNode(node);
			}
		}
	}

	/*
	 * TODO: It would be good to move these into IBTreeVisitor and eliminate
	 * IBTreeVisitor2 if this is acceptable.
	 */
	private interface IBTreeVisitor2 extends IBTreeVisitor {
		void preNode(long node) throws IndexException;
		void postNode(long node) throws IndexException;
	}

	/**
	 * Debugging method for checking B-tree invariants
	 * @return the empty String if B-tree invariants hold, otherwise
	 * a human readable report
	 * @throws IndexException
	 */
	public String getInvariantsErrorReport() throws IndexException {
		InvariantsChecker checker = new InvariantsChecker();
		accept(checker);
		return checker.isValid() ? "" : checker.getMsg(); //$NON-NLS-1$
	}

	/**
	 * A B-tree visitor for checking some B-tree invariants.
	 * Note ordering invariants are not checked here.
	 */
	private class InvariantsChecker implements IBTreeVisitor2 {
		boolean valid = true;
		String msg = ""; //$NON-NLS-1$
		Integer leafDepth;
		int depth;

		public InvariantsChecker() {}
		public String getMsg() { return this.msg; }
		public boolean isValid() { return this.valid; }
		@Override
		public void postNode(long node) throws IndexException { this.depth--; }
		@Override
		public int compare(long record) throws IndexException { return 0; }
		@Override
		public boolean visit(long record) throws IndexException { return true; }

		@Override
		public void preNode(long node) throws IndexException {
			this.depth++;

			// Collect information for checking.
			int keyCount = 0;
			int indexFirstBlankKey = BTree.this.maxRecords;
			int indexLastNonBlankKey = 0;
			for (int i= 0; i < BTree.this.maxRecords; i++) {
				if (getRecord(BTree.this.db.getChunk(node), node, i) != 0) {
					keyCount++;
					indexLastNonBlankKey = i;
				} else if (indexFirstBlankKey == BTree.this.maxRecords) {
					indexFirstBlankKey = i;
				}
			}

			int childCount = 0;
			for (int i= 0; i < BTree.this.maxChildren; i++) {
				if (getChild(BTree.this.db.getChunk(node), node, i) != 0) {
					childCount++;
				}
			}

			// Check that non-blank keys are contiguous and blank key terminated.
			if (indexFirstBlankKey != indexLastNonBlankKey + 1) {
				boolean full = indexFirstBlankKey == BTree.this.maxRecords && indexLastNonBlankKey == BTree.this.maxRecords - 1;
				boolean empty = indexFirstBlankKey == 0 && indexLastNonBlankKey == 0;
				if (!full && !empty) {
					this.valid = false;
					this.msg += MessageFormat.format("[{0} blanks inconsistent b={1} nb={2}]", //$NON-NLS-1$
							new Object[] { new Long(node), new Integer(indexFirstBlankKey),
									new Integer(indexLastNonBlankKey) });
				}
			}

			// Check: Key number constrains child numbers
			if (childCount != 0 && childCount != keyCount + 1) {
				this.valid = false;
				this.msg += MessageFormat.format("[{0} wrong number of children with respect to key count]", //$NON-NLS-1$
						new Object[] { new Long(node) });
			}

			// The root node is excused from the remaining node constraints.
			if (node == BTree.this.db.getRecPtr(BTree.this.rootPointer)) {
				return;
			}

			// Check: Non-root nodes must have a keyCount within a certain range
			if (keyCount < BTree.this.minRecords || keyCount > BTree.this.maxRecords) {
				this.valid = false;
				this.msg += MessageFormat.format("[{0} key count out of range]", new Object[] { new Long(node) }); //$NON-NLS-1$
			}

			// Check: All leaf nodes are at the same depth
			if (childCount == 0) {
				if (this.leafDepth == null) {
					this.leafDepth = new Integer(this.depth);
				}
				if (this.depth != this.leafDepth.intValue()) {
					this.valid = false;
					this.msg += "Leaf nodes at differing depths"; //$NON-NLS-1$
				}
			}
		}
	}
}
