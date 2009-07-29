/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.formatter.lineWrap;

import antlr.Token;
import org.codehaus.groovy.eclipse.refactoring.formatter.GroovyBeautifier;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * @author Mike Klenk mklenk@hsr.ch
 *
 */
public abstract class CorrectLineWrap {
	
	protected GroovyBeautifier beautifier;
	
	public CorrectLineWrap(GroovyBeautifier beautifier) {
		this.beautifier = beautifier;
	}
	
	public abstract ReplaceEdit correctLineWrap(int pos, Token token) throws BadLocationException;

}
