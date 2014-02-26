/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter.align;

/**
 * Exception used to backtrack and break available alignments
 * When the exception is thrown, it is assumed that some alignment will be changed.
 *
 * @since 2.1
 */
public class AlignmentException extends RuntimeException {

	public static final int LINE_TOO_LONG = 1;
	public static final int ALIGN_TOO_SMALL = 2;
	private static final long serialVersionUID = -3324134986466253314L; // backward compatible

	int reason;
	int value;
	public int relativeDepth;

	public AlignmentException(int reason, int relativeDepth) {
		this(reason, 0, relativeDepth);
	}

	public AlignmentException(int reason, int value, int relativeDepth) {
		this.reason = reason;
		this.value = value;
		this.relativeDepth = relativeDepth;
	}

	public String toString(){
		StringBuffer buffer = new StringBuffer(10);
		switch(this.reason){
			case LINE_TOO_LONG :
				buffer.append("LINE_TOO_LONG");	//$NON-NLS-1$
				break;
			case ALIGN_TOO_SMALL :
				buffer.append("ALIGN_TOO_SMALL");	//$NON-NLS-1$
				break;
		}
		buffer
			.append("<relativeDepth: ")	//$NON-NLS-1$
			.append(this.relativeDepth)
			.append(">\n");	//$NON-NLS-1$
		return buffer.toString();
	}
}
