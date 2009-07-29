/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.context.impl;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.eclipse.jface.text.IRegion;

public class ClosureScopeContext extends AbstractSourceCodeContext {
	private static final String ID = ISourceCodeContext.CLOSURE_SCOPE;
	
	public String getId() {
		return ID;
	}
	
	public ClosureScopeContext(ISourceBuffer buffer, ASTNode[] astPath, IRegion region) {
		super(buffer, astPath, region);
	}
	
	public String toString() {
		return "Closure Scope";
	}
}
