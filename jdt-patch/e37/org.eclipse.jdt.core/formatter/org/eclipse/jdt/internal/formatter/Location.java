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
package org.eclipse.jdt.internal.formatter;

/**
 * A location maintains positional information both in original source and in the output source.
 * It remembers source offsets, line/column and indentation level.
 * @since 2.1
 */
public class Location {

	public int inputOffset;
	/** deprecated */
	public int inputColumn;
	public int outputLine;
	public int outputColumn;
	public int outputIndentationLevel;
	public boolean needSpace;
	public boolean pendingSpace;
	public int nlsTagCounter;
	public int lastLocalDeclarationSourceStart;
	public int numberOfIndentations;

	// chunk management
	public int lastNumberOfNewLines;

	// edits management
	int editsIndex;
	OptimizedReplaceEdit textEdit;

	public Location(Scribe scribe, int sourceRestart){
		update(scribe, sourceRestart);
	}

	public void update(Scribe scribe, int sourceRestart){
		this.outputColumn = scribe.column;
		this.outputLine = scribe.line;
		this.inputOffset = sourceRestart;
		this.inputColumn = scribe.getCurrentIndentation(sourceRestart) + 1;
		this.outputIndentationLevel = scribe.indentationLevel;
		this.lastNumberOfNewLines = scribe.lastNumberOfNewLines;
		this.needSpace = scribe.needSpace;
		this.pendingSpace = scribe.pendingSpace;
		this.editsIndex = scribe.editsIndex;
		this.nlsTagCounter = scribe.nlsTagCounter;
		this.numberOfIndentations = scribe.numberOfIndentations;
		this.textEdit = scribe.getLastEdit();
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("output (column="+this.outputColumn); //$NON-NLS-1$
		buffer.append(", line="+this.outputLine); //$NON-NLS-1$
		buffer.append(", indentation level="+this.outputIndentationLevel); //$NON-NLS-1$
		buffer.append(") input (offset="+this.inputOffset); //$NON-NLS-1$
		buffer.append(", column="+this.inputColumn); //$NON-NLS-1$
		buffer.append(')');
		return buffer.toString();
	}
}
