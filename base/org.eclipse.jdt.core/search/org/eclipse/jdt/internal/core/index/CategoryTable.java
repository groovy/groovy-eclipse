/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.index;

import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;


/**
 * Hashtable of {char[] --> Object } typically used for the category table
 * stored in the {@link DiskIndex disk index}.
 * <p>
 * This is a more specific {@link HashtableOfObject hashtable of objects} where
 * the hash code is computed by using twice more characters than the hash
 * code computation of the {@link HashtableOfObject original hashtable}.
 * </p><p>
 * This is typically important for type declaration category table for which the
 * key postfix might have more than 16 identical characters...
 * </p>
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=289057"
 */
public final class CategoryTable extends HashtableOfObject {

	public CategoryTable() {
		super();
	}
	public CategoryTable(int size) {
		super(size);
	}
	
	/* (non-Javadoc)
	 * 
	 * The hash code computation is done by using half of the 32 characters last
	 * characters of the given array instead of only half of the 16 last ones for the
	 * super implementation...
	 * 
	 * @see org.eclipse.jdt.internal.compiler.util.HashtableOfObject#hashCode(char[])
	 */
	protected int hashCode(char[] array) {
		int length = array.length;
		int hash = length == 0 ? 31 : array[0];
		if (length < 16) {
			for (int i = length; --i > 0;)
				hash = (hash * 31) + array[i];
		} else {
			// 16 characters is enough to compute a decent hash code, don't waste time examining every character
			for (int i = length - 1, last = i > 32 ? i - 32 : 0; i > last; i -= 2)
				hash = (hash * 31) + array[i];
			
		}
		return hash & 0x7FFFFFFF;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.util.HashtableOfObject#rehash()
	 */
	protected void rehash() {
		CategoryTable newHashtable = new CategoryTable(this.elementSize * 2);		// double the number of expected elements
		char[] currentKey;
		for (int i = this.keyTable.length; --i >= 0;)
			if ((currentKey = this.keyTable[i]) != null)
				newHashtable.putUnsafely(currentKey, this.valueTable[i]);
	
		this.keyTable = newHashtable.keyTable;
		this.valueTable = newHashtable.valueTable;
		this.threshold = newHashtable.threshold;
	}
}
