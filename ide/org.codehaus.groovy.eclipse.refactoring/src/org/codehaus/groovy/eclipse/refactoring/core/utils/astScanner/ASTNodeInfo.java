/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner;

import org.codehaus.groovy.ast.ASTNode;

/**
 * 
 * Info Class that stores additional information
 * about AST Nodes
 *
 */
public class ASTNodeInfo {
	
	private ASTNode parent;
	private int offset, length;
	
	public ASTNode getParent() {
		return parent;
	}
	
	public void setParent(ASTNode parrent) {
		this.parent = parrent;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
	}
}
