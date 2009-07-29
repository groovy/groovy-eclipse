/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.astviews
/**
 * Interface to implement to be a tree node.
 * @author emp
 */
interface ITreeNode {
	ITreeNode getParent()

	/**
	 * Gets the value this tree node represents. This is the wrapped value.  
	 * @return
	 */
	Object getValue()
	
	String getDisplayName()
	
	ITreeNode[] getChildren()
	
	boolean isLeaf()
}
