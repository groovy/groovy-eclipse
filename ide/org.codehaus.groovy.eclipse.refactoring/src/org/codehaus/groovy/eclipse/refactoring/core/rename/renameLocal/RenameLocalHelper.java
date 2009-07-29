/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal;

import java.util.List;

import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.utils.GroovyConventionsBuilder;
import org.codehaus.groovy.eclipse.refactoring.core.utils.SourceCodePoint;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import antlr.Token;

public class RenameLocalHelper {

	public static int getVariableProxySpecificOffset(VariableProxy proxy,
			IDocument document) {
		//statements that dont have any following AST Nodes have their "last" infos set to null
		if (new SourceCodePoint(proxy, SourceCodePoint.END).isInvalid()) {
			proxy.setLastLineNumber(proxy.getLineNumber());
			proxy.setLastColumnNumber(proxy.getColumnNumber() + proxy.getName().length());
		}
		UserSelection sel = new UserSelection(proxy, document);
		int offset = sel.getOffset();
		String variableString = "";
		try {
			variableString = document.get(sel.getOffset(), sel.getLength());
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
		List<Token> tokenList = GroovyConventionsBuilder.tokenizeString(variableString);
		for(Token currentToken : tokenList){
			if(currentToken.getType() == GroovyTokenTypes.IDENT && 
					currentToken.getText().equals(proxy.getName())) {
				offset += currentToken.getColumn() - 1;
				break;
			}
		}
		return offset;
	}
}
