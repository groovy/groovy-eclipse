/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.IndexException;
import org.eclipse.jdt.internal.core.nd.field.FieldShort;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * This is a basic polymorphic node in the network database. Pointers to NdNode or any of their
 * subclasses will be resolved to the correct subclass of NdNode such that the correct version of an
 * overloaded method will be invoked.
 */
public abstract class NdNode extends NdStruct implements IDestructable {
	public static final FieldShort NODE_TYPE;

	@SuppressWarnings("hiding")
	public static final StructDef<NdNode> type;

	static {
		type = StructDef.create(NdNode.class, NdStruct.type);
		NODE_TYPE = type.addShort();
		type.done();
	}

	/**
	 * Load a node from the specified address in the given database.  Return null if a node cannot
	 * be loaded.
	 *
	 * @param nd The {@link Nd} from which to load the node.
	 * @param address The address of the node in the given {@link Nd}.
	 * @return The {@link NdNode} at the specified location or null if a node cannot be loaded.
	 * @When there is a problem reading the given {@link Nd}'s Database
	 */
	public static NdNode load(Nd nd, long address) {
		if (address == 0) {
			return null;
		}

		try {
			return nd.getNode(address, NODE_TYPE.get(nd, address));
		} catch (IndexException e) {
			// Add metadata to the exception describing where we obtained the node type from
			nd.describeProblem().addProblemAddress(NODE_TYPE, address).attachTo(e);
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends INdStruct> T load(Nd nd, long address, StructDef<T> typeToLoad) {
		if (address == 0) {
			return null;
		}

		NdNode result;
		try {
			// Polymorphic types (that subclass NdNode) store a header with their type ID
			result = nd.getNode(address, NODE_TYPE.get(nd, address));
		} catch (IndexException e) {
			// Add metadata to the exception describing where we obtained the node type from
			nd.describeProblem().addProblemAddress(NODE_TYPE, address).attachTo(e);
			throw e;
		}

		Class<T> clazz = typeToLoad.getStructClass();
		if (!clazz.isAssignableFrom(result.getClass())) {
			throw nd.describeProblem()
				.addProblemAddress(NODE_TYPE, address)
				.build("Found wrong data type at address " + address + ". Expected a subclass of " +  //$NON-NLS-1$//$NON-NLS-2$
					clazz + " but found " + result.getClass()); //$NON-NLS-1$
		}

		return (T) result;
	}

	/**
	 * Invokes the destructor on this node and frees up its memory
	 */
	public final void delete() {
		getNd().delete(this.address);
	}

	protected NdNode(Nd nd, long address) {
		super(nd, address);
	}

	protected NdNode(Nd nd) {
		super(nd, 0);
		Database db = nd.getDB();

		short nodeType = nd.getNodeType(getClass());
		ITypeFactory<? extends NdNode> factory1 = nd.getTypeFactory(nodeType);

		this.address = db.malloc(factory1.getRecordSize(), (short)(Database.POOL_FIRST_NODE_TYPE + nodeType));

		NODE_TYPE.put(nd, this.address, nodeType);
	}

	/**
	 * Return a value to uniquely identify the node within the factory that is responsible for loading
	 * instances of this node from the {@link Nd}.
	 * <b>
	 */
	public short getNodeType() {
		return this.nd.getNodeType(getClass());
	}

	@Override
	public final long getAddress() {
		return this.address;
	}

	public final long getBindingID() {
		return this.address;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof NdNode) {
			NdNode other = (NdNode) obj;
			return getNd() == other.getNd() && this.address == other.address;
		}

		return super.equals(obj);
	}

	@Override
	public final int hashCode() {
		return (int) (this.address >> Database.BLOCK_SIZE_DELTA_BITS);
	}

	/**
	 * Return an value to globally identify the given node within the given linkage.  This value
	 * can be used for comparison with other {@link NdNode}s.
	 */
	public static int getNodeId(int linkageID, int nodeType) {
		return (linkageID << 16) | (nodeType & 0xffff);
	}

	/**
	 * Convenience method for fetching a byte from the database.
	 * @param offset Location of the byte.
	 * @return a byte from the database.
	 */
	protected byte getByte(long offset) {
		return getDB().getByte(offset);
	}

	/**
	 * Returns the bit at the specified offset in a bit vector.
	 * @param bitVector Bits.
	 * @param offset The position of the desired bit.
	 * @return the bit at the specified offset.
	 */
	protected static boolean getBit(int bitVector, int offset) {
		int mask = 1 << offset;
		return (bitVector & mask) != 0;
	}

	/**
	 * Dispose this {@link NdNode}. Subclasses should extend this method to perform any high-level node-specific cleanup.
	 * This will be invoked prior to disposing the fields. Implementations must invoke their parent's destruct method
	 * and should not destruct the fields.
	 * <p>
	 * If an external object wants to destroy a node, they should invoke {@link NdNode#delete} rather than this
	 * method.
	 */
	@Override
	public void destruct() {
		// Nothing to do by default. Subclasses will provide an implementation if necessary.
	}

}