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
package org.eclipse.jdt.internal.formatter;

import org.eclipse.jdt.internal.formatter.comment.IJavaDocTagConstants;

/**
 * Represents text inside a javadoc comment block.
 * <p>
 * Text may be simple as <code>Line inside a javadoc comment block</code>
 * or may be a html tag. Note that to minimize memory footprint, only text
 * positions are stored.
 * </p><p>
 * Simple text may have one or several lines. When it has several lines, the
 * positions of the line breaks are also stored in the {@link #separators} array.
 * </p><p>
 * When text has html tags, then they are stored in {@link #htmlNodes} array
 * in a recursive way.
 * </p>
 */
public class FormatJavadocText extends FormatJavadocNode implements IJavaDocTagConstants {

	long[] separators;
	int separatorsPtr = -1;
	private int htmlTagIndex = -1;
	boolean immutable = false;
	FormatJavadocNode[] htmlNodes;
	int[] htmlIndexes;
	int htmlNodesPtr = -1;
	int depth = 0;

public FormatJavadocText(int start, int end, int line, int htmlIndex, int htmlDepth) {
	super(start, end, line);
	this.htmlTagIndex = htmlIndex;
	this.depth = htmlDepth;
}

/*
 * Append a text to current one.
 * If the given text is not an html tag or is a closing tag, then just append to
 * the current text recording the separators. Otherwise, create a new html tag
 * child node.
 */
void appendText(FormatJavadocText text) {
	text.immutable = this.immutable;
	if (this.depth == text.depth) {
		addSeparator(text);
		this.sourceEnd = text.sourceEnd;
		if (text.isClosingHtmlTag()) {
			// close the tag
			this.htmlTagIndex = text.htmlTagIndex;
		}
	} else {
		appendNode(text);
	}
	if (text.isHtmlTag()) {
		switch (text.htmlTagIndex & JAVADOC_TAGS_ID_MASK) {
			case JAVADOC_CODE_TAGS_ID:
				text.linesBefore = this.htmlNodesPtr == -1 ? 0 : 2;
				break;
			case JAVADOC_SEPARATOR_TAGS_ID:
				text.linesBefore = 1;
				break;
	    	case JAVADOC_SINGLE_BREAK_TAG_ID:
				if (!text.isClosingHtmlTag()) text.linesBefore = 1;
				break;
	    	case JAVADOC_BREAK_TAGS_ID:
				if (!text.isClosingHtmlTag()) text.linesBefore = 1;
		}
	}
}
void appendNode(FormatJavadocNode node) {
	if (++this.htmlNodesPtr == 0) { // lazy initialization
		this.htmlNodes = new FormatJavadocNode[DEFAULT_ARRAY_SIZE];
	} else {
		if (this.htmlNodesPtr == this.htmlNodes.length) {
			int size = this.htmlNodesPtr + DEFAULT_ARRAY_SIZE;
			System.arraycopy(this.htmlNodes, 0, (this.htmlNodes= new FormatJavadocNode[size]), 0, this.htmlNodesPtr);
		}
	}
	addSeparator(node);
	this.htmlNodes[this.htmlNodesPtr] = node;
	this.sourceEnd = node.sourceEnd;
}

private void addSeparator(FormatJavadocNode node) {
	// Just append the text
	if (++this.separatorsPtr == 0) { // lazy initialization
		this.separators = new long[DEFAULT_ARRAY_SIZE];
		this.htmlIndexes = new int[DEFAULT_ARRAY_SIZE];
	} else { // resize if needed
		if (this.separatorsPtr == this.separators.length) {
			int size = this.separatorsPtr + DEFAULT_ARRAY_SIZE;
			System.arraycopy(this.separators, 0, (this.separators = new long[size]), 0, this.separatorsPtr);
			System.arraycopy(this.htmlIndexes, 0, (this.htmlIndexes = new int[size]), 0, this.separatorsPtr);
		}
	}
	this.separators[this.separatorsPtr] = (((long) this.sourceEnd) << 32) + node.sourceStart;
	this.htmlIndexes[this.separatorsPtr] = node.isText() ? ((FormatJavadocText)node).htmlTagIndex : -1;
}

void clean() {
	int length = this.separators == null ? 0 : this.separators.length;
	if (this.separatorsPtr != (length-1)) {
		System.arraycopy(this.separators, 0, this.separators = new long[this.separatorsPtr+1], 0, this.separatorsPtr+1);
		System.arraycopy(this.htmlIndexes, 0, this.htmlIndexes = new int[this.separatorsPtr+1], 0, this.separatorsPtr+1);
	}
	length = this.htmlNodes == null ? 0 : this.htmlNodes.length;
	if (this.htmlNodesPtr != (length-1)) {
		System.arraycopy(this.htmlNodes, 0, this.htmlNodes = new FormatJavadocNode[this.htmlNodesPtr+1], 0, this.htmlNodesPtr+1);
		for (int i=0; i<=this.htmlNodesPtr; i++) {
			this.htmlNodes[i].clean();
		}
	}
}

void closeTag() {
	this.htmlTagIndex |= JAVADOC_CLOSED_TAG;
}

int getHtmlTagIndex() {
	return this.htmlTagIndex & JAVADOC_TAGS_INDEX_MASK;
}

int getHtmlTagID() {
	return this.htmlTagIndex & JAVADOC_TAGS_ID_MASK;
}

FormatJavadocNode getLastNode() {
	if (this.htmlNodes != null) {
		return this.htmlNodes[this.htmlNodesPtr];
	}
	return null;
}

/**
 * Returns whether the text is a closing html tag or not.
 *
 * @return <code>true</code> if the node is an html tag and has '/' before its
 * 	name (e.g. </bla>), <code>false</code> otherwise.
 */
public boolean isClosingHtmlTag() {
	return this.htmlTagIndex != -1 && (this.htmlTagIndex & JAVADOC_CLOSED_TAG) != 0;
}

/**
 * Returns whether the text is a html tag or not.
 *
 * @return <code>true</code> if the node is a html tag, <code>false</code>
 * 	otherwise.
 */
public boolean isHtmlTag() {
	return this.htmlTagIndex != -1;
}

/**
 * Returns whether the node is an immutable html tag or not.
 * <p>
 * The text in an immutable tags is <b>never</b> formatted.
 * </p>
 *
 * @return <code>true</code> if the node is an immutable tag,
 *		<code>false</code> otherwise.
 */
public boolean isImmutableHtmlTag() {
	return this.htmlTagIndex != -1 && (this.htmlTagIndex & JAVADOC_TAGS_ID_MASK) == JAVADOC_IMMUTABLE_TAGS_ID;

}

/**
 * Returns whether the text is immutable or not.
 * <p>
 * A text in considered as immutable when it  belongs to an immutable block
 * or when it's an immutable html tag.
 * </p>
 *
 * @return <code>true</code> if the node is an immutable tag,
 *		<code>false</code> otherwise.
 */
public boolean isImmutable() {
	return this.immutable || (this.htmlTagIndex != -1 && (this.htmlTagIndex & JAVADOC_TAGS_ID_MASK) == JAVADOC_IMMUTABLE_TAGS_ID);

}

/**
 * Returns whether the text at the given separator index position is after a
 * separator tag or not.
 *
 * @return <code>true</code> if the text is after a separator tag,
 *		<code>false</code> otherwise or if the given index is out the range of
 *		the text separators.
 */
public boolean isTextAfterHtmlSeparatorTag(int separatorIndex) {
	int ptr = separatorIndex;
	if (ptr > this.separatorsPtr) return false;
	int tagIndex = this.htmlIndexes[ptr] & JAVADOC_TAGS_ID_MASK;
	return tagIndex != -1 && tagIndex == JAVADOC_SEPARATOR_TAGS_ID;

}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.formatter.FormatJavadocNode#isText()
 */
public boolean isText() {
	return true;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.formatter.FormatJavadocNode#setHeaderLine(int)
 */
void setHeaderLine(int javadocLineStart) {
	for (int i=0; i<this.htmlNodesPtr; i++) {
		FormatJavadocNode node = this.htmlNodes[i];
		if (!node.isText()) {
			((FormatJavadocBlock) node).setHeaderLine(javadocLineStart);
		}
	}
}

protected void toString(StringBuffer buffer) {
	StringBuffer indentation = new StringBuffer();
	for (int t=0; t<=this.depth; t++) indentation.append('\t');
	buffer.append(indentation);
	if (isImmutable()) {
		buffer.append("immutable "); //$NON-NLS-1$
	}
	buffer.append("text"); //$NON-NLS-1$
	super.toString(buffer);
	buffer.append(" ("); //$NON-NLS-1$
	buffer.append(this.separatorsPtr+1).append(" sections, "); //$NON-NLS-1$
	buffer.append(this.htmlNodesPtr+1).append(" html tags, "); //$NON-NLS-1$
	buffer.append(this.depth).append(" depth, "); //$NON-NLS-1$
	buffer.append(this.linesBefore).append(" before, "); //$NON-NLS-1$
	String tagID = "no"; //$NON-NLS-1$
	switch (getHtmlTagID()) {
		case JAVADOC_TAGS_ID_MASK:
			tagID = "mask"; //$NON-NLS-1$
			break;
		case JAVADOC_SINGLE_BREAK_TAG_ID:
			tagID = "single break"; //$NON-NLS-1$
			break;
		case JAVADOC_CODE_TAGS_ID:
			tagID = "code"; //$NON-NLS-1$
			break;
		case JAVADOC_BREAK_TAGS_ID:
			tagID = "break"; //$NON-NLS-1$
			break;
		case JAVADOC_IMMUTABLE_TAGS_ID:
			tagID = "immutable"; //$NON-NLS-1$
			break;
		case JAVADOC_SEPARATOR_TAGS_ID:
			tagID = "separator"; //$NON-NLS-1$
			break;
	}
	buffer.append(tagID).append(" tag id)"); //$NON-NLS-1$
	buffer.append('\n');
}

public void toStringDebug(StringBuffer buffer, char[] source) {
	if (buffer.length() > 0) {
		for (int l=0; l<this.linesBefore; l++) {
			buffer.append('\n');
			for (int t=0; t<this.depth; t++) buffer.append('\t');
		}
	}
	if (this.separatorsPtr == -1) {
		super.toStringDebug(buffer, source);
		return;
	}
	int ptr = 0;
	int nextStart = this.sourceStart;
	int idx = 0;
	while (idx<=this.separatorsPtr || (this.htmlNodesPtr != -1 && ptr <= this.htmlNodesPtr)) {
		if (idx > this.separatorsPtr) {
			// last node
			FormatJavadocNode node = this.htmlNodes[ptr++];
			node.toStringDebug(buffer, source);
			return;
		}
		int end = (int) (this.separators[idx] >>> 32);
		if (this.htmlNodesPtr >= 0 && ptr <= this.htmlNodesPtr && end > this.htmlNodes[ptr].sourceStart) {
			FormatJavadocNode node = this.htmlNodes[ptr++];
			node.toStringDebug(buffer, source);
		} else {
			if (idx > 1 && source[nextStart] != '<') {
				buffer.append('\n');
				for (int t=0; t<this.depth; t++) buffer.append('\t');
			}
			buffer.append(source, nextStart, end - nextStart + 1);
		}
		nextStart = (int) this.separators[idx++];
	}
	if (source[nextStart] == '<') {
		switch (getHtmlTagID()) {
			case JAVADOC_CODE_TAGS_ID:
				buffer.append('\n');
				for (int t=0; t<this.depth; t++) buffer.append('\t');
				break;
		}
	}
	buffer.append(source, nextStart, this.sourceEnd-nextStart+1);
	buffer.append(' ');
}
}
