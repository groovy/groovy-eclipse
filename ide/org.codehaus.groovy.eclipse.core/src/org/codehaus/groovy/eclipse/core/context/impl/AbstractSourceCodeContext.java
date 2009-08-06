 /*
 * Copyright 2003-2009 the original author or authors.
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