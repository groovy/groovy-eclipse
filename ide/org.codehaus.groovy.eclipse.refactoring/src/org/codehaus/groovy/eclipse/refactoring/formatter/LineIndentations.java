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
package org.codehaus.groovy.eclipse.refactoring.formatter;

import groovyjarjarantlr.Token;

/**
 * @author Mike Klenk mklenk@hsr.ch
 *
 */
public class LineIndentations {

	private boolean[] multilineIndentation;
	private int[] indentation;
	private Token[] multilineToken;

	public LineIndentations(int lines) {
		multilineIndentation = new boolean[lines+1];
		indentation = new int[lines+1];
		multilineToken = new Token[lines+1];
	}

	public void setLineIndentation(int line, int ind) {
		indentation[line] = ind;
	}

	public int getLineIndentation(int line) {
		return indentation[line];
	}
	public void setMultilineIndentation(int line, boolean state) {
		multilineIndentation[line] = state;
	}
	public boolean isMultilineIndentation(int line) {
		return multilineIndentation[line];
	}
	public void setMultilineToken(int line, Token node) {
		multilineToken[line] = node;
	}
	public Token getMultiToken(int line) {
		return multilineToken[line];
	}
	public boolean isMultilineStatement(int line) {
		return multilineToken[line] != null;
	}
}
