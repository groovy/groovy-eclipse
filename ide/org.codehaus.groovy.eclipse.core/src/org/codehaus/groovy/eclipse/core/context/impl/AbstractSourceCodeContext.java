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

/**
 * Base class for source code contexts. Derived classes rarely need to implement more than a contructor.
 * 
 * @author empovazan
 */
public abstract class AbstractSourceCodeContext implements ISourceCodeContext {
	protected ISourceBuffer buffer;

	protected ASTNode[] astPath;

	protected IRegion region;

	public AbstractSourceCodeContext(ISourceBuffer buffer, ASTNode[] astPath, IRegion region) {
		this.buffer = buffer;
		this.astPath = astPath;
		this.region = region;
	}

	public ASTNode[] getASTPath() {
		ASTNode[] ret = new ASTNode[astPath.length];
		System.arraycopy(astPath, 0, ret, 0, astPath.length);
		return ret;
	}

	public IRegion getRegion() {
		return region;
	}
	
	public int getOffset() {
		return region.getOffset();
	}

	public int getLength() {
		return region.getLength();
	}
}