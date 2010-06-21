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

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.eclipse.jdt.internal.core.util.Util;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;

/**
 * A wrapper around an Antlr Groovy Scanner, providing a convenient interface to tokenize a snippet of groovy code.
 * 
 * @author kdvolder
 */
public class GroovyScanner {

	private TokenStream stream;

	public GroovyScanner(Reader input) {
		this(input, false);
	}

	public GroovyScanner(Reader input, boolean whiteSpaceIncluded) {
		GroovyLexer lexer = new GroovyLexer(input);
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
	 * Read all remaining tokens in the inputstream until end of file.
	 * 
	 * @return A list of all the tokens, excluding the EOF token.
	 */
	public List<Token> getTokens() {
		List<Token> result = new LinkedList<Token>();
		try {
			Token t = nextToken();
			while (t.getType() != GroovyTokenTypes.EOF) {
				result.add(t);
				t = nextToken();
			}
		} catch (TokenStreamException e) {
			Util.log(e);
		}
		return result;
	}

	/**
	 * Read all remaining tokens in the inputstream until end of file.
	 * 
	 * @return A list of all the tokens, including the EOF token.
	 */
	public List<Token> getTokensIncludingEOF() {
		List<Token> result = new ArrayList<Token>();
		try {
			Token t = nextToken();
			while (t.getType() != GroovyTokenTypes.EOF) {
				result.add(t);
				t = nextToken();
			}
			result.add(t);
		} catch (TokenStreamException e) {
			Util.log(e);
		}
		return result;
	}

	public static List<Token> getTokens(String text) {
		List<Token> tokens = new GroovyScanner(new StringReader(text)).getTokens();
		return tokens;
	}

}
