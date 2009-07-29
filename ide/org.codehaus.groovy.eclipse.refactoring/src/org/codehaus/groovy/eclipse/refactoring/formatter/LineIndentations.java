/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.formatter;

import antlr.Token;

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
