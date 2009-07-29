/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.select;

/*
 * Scanner aware of a selection range. If finding an identifier which source range is exactly
 * the same, then will record it so that the parser can make use of it.
 *
 * Source positions are zero-based and inclusive.
 */
import org.eclipse.jdt.internal.compiler.parser.Scanner;

public class SelectionScanner extends Scanner {

	public char[] selectionIdentifier;
	public int selectionStart, selectionEnd;
/* 
 * Truncate the current identifier if it is containing the cursor location. Since completion is performed
 * on an identifier prefix.
 *
 */
 
public SelectionScanner(long sourceLevel) {
	super(false /*comment*/, false /*whitespace*/, false /*nls*/, sourceLevel, null /*taskTags*/, null/*taskPriorities*/, true/*taskCaseSensitive*/);
}

public char[] getCurrentIdentifierSource() {

	if (selectionIdentifier == null){
		if (selectionStart == startPosition && selectionEnd == currentPosition-1){
			if (withoutUnicodePtr != 0){			// check unicode scenario
				System.arraycopy(withoutUnicodeBuffer, 1, selectionIdentifier = new char[withoutUnicodePtr], 0, withoutUnicodePtr);
			} else {
				int length = currentPosition - startPosition;
				// no char[] sharing around completionIdentifier, we want it to be unique so as to use identity checks	
				System.arraycopy(source, startPosition, (selectionIdentifier = new char[length]), 0, length);
			}
			return selectionIdentifier;
		}
	}
	return super.getCurrentIdentifierSource();
}
/*
 * In case we actually read a keyword which corresponds to the selected
 * range, we pretend we read an identifier.
 */
public int scanIdentifierOrKeyword() {

	int id = super.scanIdentifierOrKeyword();

	// convert completed keyword into an identifier
	if (id != TokenNameIdentifier
		&& startPosition == selectionStart 
		&& currentPosition == selectionEnd+1){
		return TokenNameIdentifier;
	}
	return id;
}
}
