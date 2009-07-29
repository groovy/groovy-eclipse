/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal.VariableProxy;
import org.codehaus.groovy.eclipse.refactoring.core.utils.SourceCodePoint;
import org.eclipse.jface.text.IDocument;

/**
 * Represents a selection in the GUI
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class UserSelection {
	
	private final int offset,length;
	
	public UserSelection(int offset, int length) {
		this.offset = offset;
		this.length = length;
	}
	
	public UserSelection(SourceCodePoint start, SourceCodePoint end, IDocument doc) {
		offset = start.getOffset(doc);
		length = end.getOffset(doc) - offset;
	}

	public UserSelection(ASTNode node, IDocument document) {
		this(new SourceCodePoint(node.getLineNumber(),node.getColumnNumber()),new SourceCodePoint(node.getLastLineNumber(),node.getLastColumnNumber()),document);
	}
	
	public UserSelection(VariableProxy node, IDocument document) {
		this(new SourceCodePoint(node.getLineNumber(),node.getColumnNumber()),new SourceCodePoint(node.getLastLineNumber(),node.getLastColumnNumber()),document);
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}
	
	public ASTNode getASTNode(IDocument document) {
		ASTNode node = new ASTNode();
		SourceCodePoint start = new SourceCodePoint(offset,document);
		SourceCodePoint end = new SourceCodePoint(offset+length,document);
		node.setColumnNumber(start.getCol());
		node.setLineNumber(start.getRow());
		node.setLastColumnNumber(end.getCol());
		node.setLastLineNumber(end.getRow());
		return node;
	}
	
	public static UserSelection getSelectionOfNode(ASTNode node, IDocument doc) {
		return new UserSelection(node,doc);
	}
	
	@Override
    public boolean equals(Object obj) {
		if(obj != null && obj instanceof UserSelection){
			return offset == ((UserSelection)obj).getOffset() && length == ((UserSelection)obj).getLength();
		}
		return false;
	}
	
	public boolean isInsideOf(UserSelection otherSelection){
		return (this.getOffset() >= otherSelection.getOffset()) &&
				(this.getOffset() + this.getLength() <= otherSelection.getOffset() + otherSelection.getLength()) &&
				(this.getLength() <= otherSelection.getLength());
	}

}
