/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

/*
 * Adapts an IBuffer to IDocument
 */
public class DocumentAdapter extends Document {

	private IBuffer buffer;

	public DocumentAdapter(IBuffer buffer) {
		super(buffer.getContents());
		this.buffer = buffer;
	}

	public void set(String text) {
		super.set(text);
		this.buffer.setContents(text);
	}

	public void replace(int offset, int length, String text) throws BadLocationException {
		super.replace(offset, length, text);
		this.buffer.replace(offset, length, text);
	}

}
