/* 
 * Copyright (C) 2008, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.utils;

import org.codehaus.groovy.ast.CodeVisitorSupport;

/**
 * @author mklenk
 *
 */
public abstract class ASTVisitorDecorator<Container> extends CodeVisitorSupport {

	protected Container container;
	
	public ASTVisitorDecorator(Container container) {
		this.container = container;
	}

	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}
	
}
