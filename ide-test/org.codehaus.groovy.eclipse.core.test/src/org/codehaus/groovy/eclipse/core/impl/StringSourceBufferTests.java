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
package org.codehaus.groovy.eclipse.core.impl;


import junit.framework.TestCase;

public class StringSourceBufferTests extends TestCase {
	static String testString = "Hello\nGroovy\nWorld!";
	static StringSourceBuffer buffer;
	
	@Override
    protected void setUp() throws Exception {
		super.setUp();
		buffer = new StringSourceBuffer(testString);
	}
	
	public void testLength() {
		assertEquals(testString.length(), buffer.length());
	}
	
	public void testGetChar() {
		assertEquals('H', buffer.charAt(0));
		assertEquals('!', buffer.charAt(testString.length() - 1));
	}
	
	public void testGetText() {
		assertEquals("Hello", buffer.subSequence(0, "Hello".length()));
		int start = "Hello\nGroovy\n".length();
		int end = start + "World!".length();
		assertEquals("World!", buffer.subSequence(start, end));
	}
	
	public void testToAndFrom() {
		int[] lineCol = buffer.toLineColumn(0);
		assertEquals(0, buffer.toOffset(lineCol[0], lineCol[1]));
	}
	
	public void testToLineColumn() {
		int[] lineCol = buffer.toLineColumn(0);
		assertEquals(1, lineCol[0]);
		assertEquals(1, lineCol[1]);
		
		lineCol = buffer.toLineColumn("Hello\n".length());
		assertEquals(2, lineCol[0]);
		assertEquals(1, lineCol[1]);
		
		lineCol = buffer.toLineColumn(testString.length() - 1);
		assertEquals(3, lineCol[0]);
		assertEquals("World!".length(), lineCol[1]);
	}
	
	public void testToOffset() {
		assertEquals(0, buffer.toOffset(1, 1));
		assertEquals("Hello\n".length(), buffer.toOffset(2, 1));
		assertEquals("Hello\nGroovy\n".length(), buffer.toOffset(3, 1));
		assertEquals(testString.length() - 1, buffer.toOffset(3, "World!".length()));
	}
	
	public void testWhite() {
		new StringSourceBuffer("hello\r\n");
		new StringSourceBuffer("hello\r");
	}
}
