/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 440474 - [null] textual encoding of external null annotations
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationProvider;

public class SignatureWrapper {
	public char[] signature;
	public int start;
	public int end;
	public int bracket;
	private boolean use15specifics;
	private boolean useExternalAnnotations;

	public SignatureWrapper(char[] signature, boolean use15specifics) {
		this.signature = signature;
		this.start = 0;
		this.end = this.bracket = -1;
		this.use15specifics = use15specifics;
	}
	public SignatureWrapper(char[] signature, boolean use15specifics, boolean useExternalAnnotations) {
		this.signature = signature;
		this.start = 0;
		this.end = this.bracket = -1;
		this.use15specifics = use15specifics;
		this.useExternalAnnotations = useExternalAnnotations;
	}
	public SignatureWrapper(char [] signature) {
		this(signature, true);
	}
	public boolean atEnd() {
		return this.start < 0 || this.start >= this.signature.length;
	}
	public int computeEnd() {
		int index = this.start;
		if (this.useExternalAnnotations) {
			// in addition to '[' tokens accept null annotations after the first '['
			skipDimensions: while(true) {
				switch (this.signature[index]) {
					case ExternalAnnotationProvider.NONNULL :
					case ExternalAnnotationProvider.NULLABLE :
					case ExternalAnnotationProvider.NO_ANNOTATION :
						if (index == this.start)
							break skipDimensions;
						//$FALL-THROUGH$
					case '[':
						index++;
						break;
					default:
						break skipDimensions;
				}
			}
		} else {
			while (this.signature[index] == '[')
				index++;
		}
		switch (this.signature[index]) {
			case 'L' :
			case 'T' :
				this.end = CharOperation.indexOf(';', this.signature, this.start);
				if (this.bracket <= this.start) // already know it if its > start
					this.bracket = CharOperation.indexOf('<', this.signature, this.start);

				if (this.bracket > this.start && this.bracket < this.end)
					this.end = this.bracket;
				else if (this.end == -1)
					this.end = this.signature.length + 1;
				break;
			default :
				this.end = index;
		}

		if (this.use15specifics || this.end != this.bracket) {
			this.start = this.end + 1; // skip ';'
		} else {
			this.start = skipAngleContents(this.end) + 1;  // skip <<>*>;
			this.bracket = -1;
		}
		return this.end;
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850, do not expose generics if we shouldn't
	public int skipAngleContents(int i) {
		if (this.signature[i] != '<') {
			return i;
		}
		int depth = 0, length = this.signature.length;
		for (++i; i < length; i++) {
			switch(this.signature[i]) {
				case '<' :
					depth++;
					break;
				case '>' :
					if (--depth < 0)
						return i + 1;
					break;
			}
		}
		return i;
	}
	public char[] nextWord() {
		this.end = CharOperation.indexOf(';', this.signature, this.start);
		if (this.bracket <= this.start) // already know it if its > start
			this.bracket = CharOperation.indexOf('<', this.signature, this.start);
		int dot = CharOperation.indexOf('.', this.signature, this.start);

		if (this.bracket > this.start && this.bracket < this.end)
			this.end = this.bracket;
		if (dot > this.start && dot < this.end)
			this.end = dot;

		return CharOperation.subarray(this.signature, this.start, this.start = this.end); // skip word
	}
	/**  similar to nextWord() but don't stop at '.' */
	public char[] nextName() {
		this.end = CharOperation.indexOf(';', this.signature, this.start);
		if (this.bracket <= this.start) // already know it if its > start
			this.bracket = CharOperation.indexOf('<', this.signature, this.start);

		if (this.bracket > this.start && this.bracket < this.end)
			this.end = this.bracket;

		return CharOperation.subarray(this.signature, this.start, this.start = this.end); // skip name
	}

	/**  answer the next type (incl. type arguments), but don't advance any cursors */
	public char[] peekFullType() {
		int s = this.start, b = this.bracket, e = this.end;
		int peekEnd = skipAngleContents(computeEnd());
		this.start = s;
		this.bracket = b;
		this.end = e;
		return CharOperation.subarray(this.signature, s, peekEnd+1);
	}

	/**
	 * assuming a previously stored start of 's' followed by a call to computeEnd()
	 * now retrieve the content between these bounds including trailing angle content
	 */
	public char[] getFrom(int s) {
		if (this.end == this.bracket) {
			this.end = skipAngleContents(this.bracket);
			this.start = this.end + 1;
		}
		return CharOperation.subarray(this.signature, s, this.end+1);
	}
	public char[] tail() {
		return CharOperation.subarray(this.signature, this.start, this.signature.length);
	}
	public String toString() {
		return new String(this.signature) + " @ " + this.start; //$NON-NLS-1$
	}
}
