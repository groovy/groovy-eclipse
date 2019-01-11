package org.eclipse.jdt.internal.core.nd.db;

public class LargeBlock {
	public static final int SIZE_OFFSET = 0;
	public static final int SIZE_OF_SIZE_FIELD = Database.INT_SIZE;
	/**
	 * Size of the header for a large block. The header consists of a int which holds the number of chunks in the block.
	 * It is negative for an allocated block and positive for an unallocated block. The header is located at the start
	 * of the large block.
	 */
	public static final int HEADER_SIZE = Math.max(Database.INT_SIZE, Database.BLOCK_SIZE_DELTA);

	public static final int ENTRIES_IN_CHILD_TABLE = SIZE_OF_SIZE_FIELD * 8;
	public static final int CHILD_TABLE_OFFSET = HEADER_SIZE;
	public static final int PARENT_OFFSET = CHILD_TABLE_OFFSET + (Database.INT_SIZE * ENTRIES_IN_CHILD_TABLE);
	public static final int PREV_BLOCK_OFFSET = PARENT_OFFSET + Database.INT_SIZE;
	public static final int NEXT_BLOCK_OFFSET = PREV_BLOCK_OFFSET + Database.INT_SIZE;

	public static final int UNALLOCATED_HEADER_SIZE = NEXT_BLOCK_OFFSET + Database.INT_SIZE;

	/**
	 * The large block footer is located at the end of the last chunk in the large block. It is an exact copy of the
	 * header.
	 */
	public static final int FOOTER_SIZE = HEADER_SIZE;
}
