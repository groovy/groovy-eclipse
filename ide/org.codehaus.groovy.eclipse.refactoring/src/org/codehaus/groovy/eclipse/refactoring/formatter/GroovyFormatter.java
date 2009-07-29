/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.formatter;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.TextEdit;

public abstract class GroovyFormatter {

	protected ITextSelection selection;
	protected IDocument document;

	public GroovyFormatter(ITextSelection sel, IDocument doc) {
		this.selection = sel;
		this.document = doc;
	}

	/**
	 * Format <code>source</code>,
	 * and returns a text edit that correspond to the difference between the given string and the formatted string.
	 * @return the text edit
	 */
	public abstract TextEdit format();

}