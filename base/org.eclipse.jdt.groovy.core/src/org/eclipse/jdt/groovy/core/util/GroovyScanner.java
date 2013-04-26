/*
 * Copyright 2003-2010 the original author or authors.
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
package org.eclipse.jdt.groovy.core.util;

import groovyjarjarantlr.Token;
import groovyjarjarantlr.TokenStream;
import groovyjarjarantlr.TokenStreamException;

import java.io.Reader;
import java.io.StringReader;

import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * A wrapper around an Antlr Groovy Scanner, providing a convenient interface to tokenize a snippet of groovy code.
 * 
 * @author kdvolder
 */
public class GroovyScanner {

	private TokenStream stream;
	private GroovyLexer lexer;
	private boolean whiteSpaceIncluded;

	public GroovyScanner(Reader input) {
		this(input, false);
	}

	public GroovyScanner(Reader input, boolean whiteSpaceIncluded) {
		init(input, whiteSpaceIncluded);
	}

	private void init(Reader input, boolean whiteSpaceIncluded) {
		this.whiteSpaceIncluded = whiteSpaceIncluded;
		lexer = new GroovyLexer(input);
		lexer.setWhitespaceIncluded(whiteSpaceIncluded);
		this.stream = (TokenStream) lexer.plumb();
	}

	public GroovyScanner(String text) {
		this(new StringReader(text), false);
	}

	public Token nextToken() throws TokenStreamException {
		return stream.nextToken();
	}

	/**
	 * Attempt to recover after a scanning error. We will recreate the Antlr lexer one character past the place where we got an
	 * error and try to continue scanning from there.
	 * 
	 * @throws BadLocationException
	 */
	public void recover(IDocument document) throws BadLocationException {
		int line = lexer.getInputState().getLine(); // Line and
		int col = lexer.getInputState().getColumn(); // column where error happened.
		int offset = getOffset(document, line, col) + 1; // +1 to skip one character.
		line = document.getLineOfOffset(offset);
		int lineStart = document.getLineOffset(line);
		line = line + 1; // antlr lines start at 1
		col = offset - lineStart + 1; // antlr cols start at 1
		String remainingInput = document.get(offset, document.getLength() - offset);
		init(new StringReader(remainingInput), whiteSpaceIncluded); // Reinitialize with remaining input
		lexer.setLine(line); // Fix antlr line and
		lexer.setColumn(col);// column infos because we are not starting at the start
	}

	/**
	 * Convert antlr line / col position into a IDocument offset.
	 * 
	 * @param document The reference document
	 * @param line antlr style line number (starts at 1)
	 * @param col antlr style col number (starts at 1)
	 * @return
	 * @throws BadLocationException
	 */
	public static int getOffset(IDocument document, int line, int col) throws BadLocationException {
		return document.getLineOffset(line - 1) + col - 1;
	}

}
