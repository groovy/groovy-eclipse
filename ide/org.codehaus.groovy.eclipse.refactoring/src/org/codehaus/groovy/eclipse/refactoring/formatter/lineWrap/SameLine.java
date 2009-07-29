/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.formatter.lineWrap;

import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.eclipse.refactoring.formatter.GroovyBeautifier;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.ReplaceEdit;

import antlr.Token;

/**
 * @author Mike Klenk mklenk@hsr.ch
 *
 */
public class SameLine extends CorrectLineWrap {

	/**
	 * @param beautifier
	 */
	public SameLine(GroovyBeautifier beautifier) {
		super(beautifier);
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.eclipse.refactoring.formatter.lineWrap.CorrectLineWrap#correctLineWrap(antlr.Token)
	 */
	@Override
	public ReplaceEdit correctLineWrap(int pos, Token token) throws BadLocationException {
		ReplaceEdit correctEdit = null;
		if(beautifier.formatter.getPreviousTokenIncludingNLS(pos).getType() == GroovyTokenTypes.NLS) {
			Token lastNotNLSToken = beautifier.formatter.getPreviousToken(pos);
			int replaceStart = beautifier.formatter.getOffsetOfTokenEnd(lastNotNLSToken) ;
			int replaceEnd = beautifier.formatter.getOffsetOfToken(token);
			correctEdit = new ReplaceEdit(replaceStart,replaceEnd-replaceStart," ");
		}
		return correctEdit;
	}

}
