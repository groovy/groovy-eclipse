/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.refactoring.core.utils;

import org.codehaus.groovy.ast.ASTNode;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * Represent a cursor position in a source file
 * @author Michael Klenk mklenk@hsr.ch
 *
 */

public class SourceCodePoint implements Comparable<SourceCodePoint>{
	
	
	public static final int BEGIN = 0;
	public static final int END = 1;
	
	private final int row,col;
	public SourceCodePoint(int row,int col) {
		assert row > -1;
		assert col > -1;
		
		this.row = row;
		this.col = col;
	}
	
	/**
	 * Create a point with the given offset in a document
	 * @param offset
	 * @param doc
	 */
	public SourceCodePoint(int offset,IDocument doc) {
		int row = 0,col = 0;	
		try {
				row = doc.getLineOfOffset(offset);
				col = offset - doc.getLineOffset(row);
			} catch (BadLocationException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			this.row = row + 1;
			this.col = col + 1;
	}
	
	public SourceCodePoint(ASTNode node,int pos) {
		switch (pos) {
		case BEGIN:
			row = node.getLineNumber();
			col = node.getColumnNumber();
			break;
		case END:
			row = node.getLastLineNumber();
			col = node.getLastColumnNumber();
			break;

		default:
			row = col = -1;
			break;
		}
	}

	
	/**
	 * Compares two SourceCodePoints.
	 * @param p2
	 * @return true if the calling point is after the given
	 */
	public boolean isAfter(SourceCodePoint p2) {
		if(this.row > p2.row)
			return true;
		if(this.row == p2.row && this.col >= p2.col)
			return true;
		return false;
	}
	
	/**
	 * Compares two SourceCodePoints.
	 * Return true if the calling point is bevore the given
	 * @param p2
	 * @return
	 */
	public boolean isBefore(SourceCodePoint p2) {
		if(this.row < p2.row)
			return true;
		if(this.row == p2.row && this.col <= p2.col)
			return true;
		return false;
	}
	/**
	 * @return Cloumn of the point
	 */
	public int getCol() {
		return col;
	}
	
	/**
	 * @return Row / Linenumber of the point
	 */
	public int getRow() {
		return row;
	}
	
	/**
	 * @param doc
	 * @return Offset in the given document
	 */
	public int getOffset(IDocument doc) {
		try {
			return doc.getLineOffset(row-1) + (col-1);
		} catch (BadLocationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Offset of a SourceCodePoint in a document
	 * @param row
	 * @param col
	 * @param doc
	 * @return
	 */
	public static int getOffset(int row,int col,IDocument doc) {
		return new SourceCodePoint(row,col).getOffset(doc);
	}
	/**
	 * Row / Line of a SourceCodePoint in a document
	 * @param row
	 * @param col
	 * @param doc
	 * @return
	 */	
	public static int getRow(int offset,IDocument doc) {
		return new SourceCodePoint(offset,doc).getRow();
	}
	
	/**
	 * Column of a SourceCodePoint in a document
	 * @param row
	 * @param col
	 * @param doc
	 * @return
	 */	public static int getCol(int offset,IDocument doc) {
		return new SourceCodePoint(offset,doc).getCol();
	}

	public int compareTo(SourceCodePoint arg0) {
		if(this.row > arg0.row)
			return 1;
		if(this.row == arg0.row && this.col > arg0.col)
			return 1;
		if(this.row < arg0.row)
			return -1;
		if(this.row == arg0.row && this.col < arg0.col)
			return -1;
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj instanceof SourceCodePoint) {
			return (((SourceCodePoint)obj).col == col && ((SourceCodePoint)obj).row == row);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (String.valueOf(col) + "/" + String.valueOf(row)).hashCode();
	}
	
	public boolean isInvalid(){
		return (col < 1 || row < 1);
	}
	
	public boolean isValid(){
		return !isInvalid();
	}

	@Override
	public String toString() {
		return "(" + row + " / " + col + ")";
	}

}
