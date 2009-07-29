/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.editor;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;

/**
 * Interface for handling a specific auto pair strategy when in a specific
 * partition. This makes it easier to add strategies for different partitions.
 * 
 * @author emp
 */
public interface IPairInPartitionStrategy {
	/**
	 * Is this strategy active?
	 * 
	 * @return
	 */
	public boolean isActive();

	/**
	 * The user pressed a key that inserts a character, check for auto pair
	 * completion.
	 * 
	 * @param document
	 * @param command
	 */
	public void doInsert(IDocument document, DocumentCommand command);

	/**
	 * The user pressed the backspace key, check for auto pair deletion.
	 * 
	 * @param document
	 * @param command
	 */
	public void doRemove(IDocument document, DocumentCommand command);
}
