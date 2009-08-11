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

import org.codehaus.groovy.antlr.LineColumn;
import org.codehaus.groovy.eclipse.refactoring.core.utils.FilePartReader;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import junit.framework.TestCase;

public class FilePartReaderTest extends TestCase {
	
	private static String words="first second third\nfirst2 second2 third2";
	private IDocument doc;
	
	@Override
	protected void setUp() throws Exception {
	    System.out.println("------------------------------");
	    System.out.println("Starting: " + getName());
		super.setUp();
		doc = new Document(words);
	}

	public void testReadForward() {
		String word;
		word = FilePartReader.readForwardFromCoordinate(doc, new LineColumn(1,5));
		assertEquals("t", word);
		word = FilePartReader.readForwardFromCoordinate(doc, new LineColumn(1,6));
		assertEquals("second", word);
		word = FilePartReader.readForwardFromCoordinate(doc, new LineColumn(1,7));
		assertEquals("second", word);
		word = FilePartReader.readForwardFromCoordinate(doc, new LineColumn(1,8));
		assertEquals("econd", word);	
	}
	
	public void testReadBackwards() {
		String word;
		word = FilePartReader.readBackwardsFromCoordinate(doc, new LineColumn(1,12));
		assertEquals("secon", word);
		word = FilePartReader.readBackwardsFromCoordinate(doc, new LineColumn(1,13));
		assertEquals("second", word);
		word = FilePartReader.readBackwardsFromCoordinate(doc, new LineColumn(1,14));
		assertEquals("second", word);
		word = FilePartReader.readBackwardsFromCoordinate(doc, new LineColumn(1,15));
		assertEquals("t", word);	
	}
	
	public void testIncorrectCoords() {
		String word;
		word = FilePartReader.readBackwardsFromCoordinate(doc, new LineColumn(-1,-2));
		assertEquals(" ", word);
		word = FilePartReader.readForwardFromCoordinate(doc, new LineColumn(-1,-2));
		assertEquals(" ", word);
	}
}
