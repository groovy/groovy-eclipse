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
