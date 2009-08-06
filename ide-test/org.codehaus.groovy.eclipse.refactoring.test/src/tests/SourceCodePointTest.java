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
package tests;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.refactoring.core.utils.SourceCodePoint;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import junit.framework.TestCase;

public class SourceCodePointTest extends TestCase {
	
	private IDocument document;

	@Override
    protected void setUp() throws Exception {
		super.setUp();
		 document = new Document("My Document\nToTest");

	}

	public void testHashCode() {
		SourceCodePoint p1 = new SourceCodePoint(1,2);
		SourceCodePoint p2 = new SourceCodePoint(2,1);
		SourceCodePoint p3 = new SourceCodePoint(1,2);
		
		assertEquals(p1.hashCode(), p3.hashCode());
		assertNotSame(p1.hashCode(), p2.hashCode());
		
	}

	public void testSourceCodePointIntInt() {
		SourceCodePoint p1 = new SourceCodePoint(1,2);
		SourceCodePoint p2 = new SourceCodePoint(0,0);
		SourceCodePoint p3 = new SourceCodePoint(-1,-1);
		assertNotNull(p1);
		assertNotNull(p2);
		assertNotNull(p3);
	}

	public void testSourceCodePointIntIDocument() {
		SourceCodePoint p1 = new SourceCodePoint(5,document);
		SourceCodePoint p2 = new SourceCodePoint(14,document);
		
		assertEquals(1, p1.getRow());
		assertEquals(6, p1.getCol());
		
		assertEquals(2, p2.getRow());
		assertEquals(3, p2.getCol());
	}

	public void testSourceCodePointASTNodeInt() {
		ASTNode node = new ASTNode();
		node.setLineNumber(2);
		node.setColumnNumber(4);
		node.setLastLineNumber(5);
		node.setLastColumnNumber(6);
		
		SourceCodePoint p1 = new SourceCodePoint(node,SourceCodePoint.BEGIN);
		SourceCodePoint p2 = new SourceCodePoint(node,SourceCodePoint.END);
		
		assertEquals(2, p1.getRow());
		assertEquals(4, p1.getCol());
		
		assertEquals(5, p2.getRow());
		assertEquals(6, p2.getCol());
	}

	public void testIsAfter() {
		SourceCodePoint mark = new SourceCodePoint(2,2);
		
		SourceCodePoint equal = new SourceCodePoint(2,2);
		SourceCodePoint before1 = new SourceCodePoint(2,1);
		SourceCodePoint before2 = new SourceCodePoint(1,2);
		SourceCodePoint after1 = new SourceCodePoint(2,3);
		SourceCodePoint after2 = new SourceCodePoint(3,2);
		SourceCodePoint start = new SourceCodePoint(0,0);
		SourceCodePoint invalid = new SourceCodePoint(-1,-1);
		
		assertTrue(after1.isAfter(mark));
		assertTrue(after2.isAfter(mark));
		
		assertTrue(equal.isAfter(mark));
		
		assertFalse(before1.isAfter(mark));
		assertFalse(before2.isAfter(mark));
		
		assertFalse(start.isAfter(mark));
		assertFalse(invalid.isAfter(mark));

	}

	public void testIsBefore() {
		SourceCodePoint mark = new SourceCodePoint(2,2);
		
		SourceCodePoint equal = new SourceCodePoint(2,2);
		SourceCodePoint before1 = new SourceCodePoint(2,1);
		SourceCodePoint before2 = new SourceCodePoint(1,2);
		SourceCodePoint after1 = new SourceCodePoint(2,3);
		SourceCodePoint after2 = new SourceCodePoint(3,2);
		SourceCodePoint start = new SourceCodePoint(0,0);
		SourceCodePoint invalid = new SourceCodePoint(-1,-1);
		
		assertFalse(after1.isBefore(mark));
		assertFalse(after2.isBefore(mark));
		
		assertTrue(equal.isBefore(mark));
		
		assertTrue(before1.isBefore(mark));
		assertTrue(before2.isBefore(mark));
		assertTrue(start.isBefore(mark));
		assertTrue(invalid.isBefore(mark));
	}

	public void testGetCol() {
		SourceCodePoint p1 = new SourceCodePoint(1,2);
		assertEquals(2, p1.getCol());
	}

	public void testGetRow() {
		SourceCodePoint p1 = new SourceCodePoint(1,2);
		assertEquals(1, p1.getRow());
	}

	public void testGetOffsetIDocument() {
		
		SourceCodePoint p1 = new SourceCodePoint(1,6);
		SourceCodePoint p2 = new SourceCodePoint(2,3);
		
		assertEquals(5, p1.getOffset(document));
		assertEquals(14, p2.getOffset(document));
	}

	public void testGetOffsetIntIntIDocument() {
		assertEquals(5, SourceCodePoint.getOffset(1, 6, document));
		assertEquals(14, SourceCodePoint.getOffset(2, 3, document));
	}

	public void testGetRowIntIDocument() {
		assertEquals(1, SourceCodePoint.getRow(5, document));
		assertEquals(2, SourceCodePoint.getRow(14, document));
	}

	public void testGetColIntIDocument() {
		assertEquals(6, SourceCodePoint.getCol(5, document));
		assertEquals(3, SourceCodePoint.getCol(14, document));
	}

	public void testCompareTo() {
		SourceCodePoint mark = new SourceCodePoint(1,2);
		
		SourceCodePoint before = new SourceCodePoint(1,1);
		SourceCodePoint after = new SourceCodePoint(1,3);
		
		SourceCodePoint same = new SourceCodePoint(1,2);
		
		assertEquals(1,mark.compareTo(before));
		assertEquals(-1,mark.compareTo(after));		
		assertEquals(0,mark.compareTo(same));
		
	}

	public void testEqualsObject() {
		SourceCodePoint p1 = new SourceCodePoint(1,2);
		SourceCodePoint p2 = new SourceCodePoint(2,1);
		SourceCodePoint p3 = new SourceCodePoint(1,2);
		
		assertTrue(p1.equals(p3));
		assertFalse(p1.equals(p2));
	}

	public void testIsInvalid() {
		SourceCodePoint p1 = new SourceCodePoint(1,-1);
		SourceCodePoint p2 = new SourceCodePoint(-1,1);
		SourceCodePoint p3 = new SourceCodePoint(0,0);
		SourceCodePoint p4 = new SourceCodePoint(1,1);
		
		assertTrue(p1.isInvalid());
		assertTrue(p2.isInvalid());
		assertTrue(p3.isInvalid());
		assertFalse(p4.isInvalid());
	}

	public void testIsValid() {
		SourceCodePoint p1 = new SourceCodePoint(1,-1);
		SourceCodePoint p2 = new SourceCodePoint(-1,1);
		SourceCodePoint p3 = new SourceCodePoint(0,0);
		SourceCodePoint p4 = new SourceCodePoint(1,1);
		
		assertFalse(p1.isValid());
		assertFalse(p2.isValid());
		assertFalse(p3.isValid());
		assertTrue(p4.isValid());
	}
}
