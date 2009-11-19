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
