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

/**
 * Abstract implementation with some generally useful methods.
 * 
 * @author emp
 */
public abstract class AbstractPairInPartitionStrategy implements
		IPairInPartitionStrategy {
	/**
	 * Check if the sequence contains any of the characters in the samples.
	 * 
	 * @param seq
	 * @param samples
	 * @return True if a partial match exists, else false.
	 */
	protected boolean partiallyMatchesOne(String seq, char[] samples) {
		for (int i = 0; i < samples.length; ++i) {
			if (seq.indexOf(samples[i]) != -1)
				return true;
		}
		return false;
	}

	/**
	 * Check if the sequence is equal to one of the samples.
	 * 
	 * @param seq
	 * @param samples
	 * @return
	 */
	protected boolean matchesOne(String seq, String[] samples) {
		for (int i = 0; i < samples.length; ++i) {
			if (seq.equals(samples[i]))
				return true;
		}
		return false;
	}
}
